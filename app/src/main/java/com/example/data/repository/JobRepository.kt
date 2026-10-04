package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.JobListingDao
import com.example.data.local.JobListingEntity
import com.example.data.local.SeedDataProvider
import com.example.data.model.JobListing
import com.example.data.remote.ApiClient
import com.example.data.remote.JobApiService
import com.example.data.remote.UpdateStatusRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Stack

data class UndoAction(
    val jobId: String,
    val previousStatus: String,
    val timestamp: Long = System.currentTimeMillis()
)

class JobRepository(
    private val dao: JobListingDao,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lockin_prefs", Context.MODE_PRIVATE)

    private val undoStack = Stack<UndoAction>()

    // Configurable API & Sheet settings
    var webhookBaseUrl: String
        get() = prefs.getString("webhook_base_url", "https://api.jobmatch.internal/") ?: "https://api.jobmatch.internal/"
        set(value) = prefs.edit().putString("webhook_base_url", value.trim()).apply()

    var googleSheetCsvUrl: String
        get() = prefs.getString(
            "sheet_csv_url",
            "https://docs.google.com/spreadsheets/d/1Qd9U55P-Fe1g5DD5_3x_zK4HM9EjZQwdcVv7dqDKoXA/export?format=csv&gid=0"
        ) ?: "https://docs.google.com/spreadsheets/d/1Qd9U55P-Fe1g5DD5_3x_zK4HM9EjZQwdcVv7dqDKoXA/export?format=csv&gid=0"
        set(value) = prefs.edit().putString("sheet_csv_url", value.trim()).apply()

    var lastSyncTimestamp: Long
        get() = prefs.getLong("last_sync_timestamp", 0L)
        set(value) = prefs.edit().putLong("last_sync_timestamp", value).apply()

    private fun getApiService(): JobApiService {
        return ApiClient.createService(webhookBaseUrl)
    }

    val pendingJobs: Flow<List<JobListing>> = dao.getPendingJobs().map { entities ->
        entities.map { it.toDomain() }
    }

    val appliedJobs: Flow<List<JobListing>> = dao.getAppliedJobs().map { entities ->
        entities.map { it.toDomain() }
    }

    val rejectedJobs: Flow<List<JobListing>> = dao.getRejectedJobs().map { entities ->
        entities.map { it.toDomain() }
    }

    val pendingCount: Flow<Int> = dao.getPendingCount()
    val appliedCount: Flow<Int> = dao.getAppliedCount()
    val rejectedCount: Flow<Int> = dao.getRejectedCount()

    suspend fun initializeDataIfNeeded() = withContext(Dispatchers.IO) {
        val count = dao.getJobCount()
        if (count == 0) {
            val entities = SeedDataProvider.initialJobs.map { JobListingEntity.fromDomain(it) }
            dao.insertAll(entities)
            lastSyncTimestamp = System.currentTimeMillis()
            Log.d("JobRepository", "Seeded ${entities.size} initial startup jobs into Room.")
        }
    }

    suspend fun updateJobStatus(jobId: String, newStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = dao.getJobById(jobId)
        val oldStatus = existing?.status ?: JobListing.STATUS_PENDING

        // Push to undo stack
        undoStack.push(UndoAction(jobId, oldStatus))

        // Optimistic local update
        dao.updateStatus(jobId, newStatus)

        // Asynchronously post to webhook endpoint in background
        try {
            val api = getApiService()
            val response = api.updateJobStatus(UpdateStatusRequest(id = jobId, status = newStatus))
            if (response.isSuccessful) {
                Log.d("JobRepository", "Synced status '$newStatus' for $jobId to webhook.")
            } else {
                Log.w("JobRepository", "Webhook update responded with code ${response.code()}")
            }
        } catch (e: Exception) {
            Log.w("JobRepository", "Background webhook sync failed (offline or endpoint inactive): ${e.message}")
        }

        Result.success(Unit)
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    suspend fun undoLastAction(): JobListing? = withContext(Dispatchers.IO) {
        if (undoStack.isEmpty()) return@withContext null
        val lastAction = undoStack.pop()
        dao.updateStatus(lastAction.jobId, lastAction.previousStatus)

        // Try syncing undo to webhook
        try {
            val api = getApiService()
            api.updateJobStatus(UpdateStatusRequest(id = lastAction.jobId, status = lastAction.previousStatus))
        } catch (e: Exception) {
            Log.w("JobRepository", "Undo webhook sync failed: ${e.message}")
        }

        dao.getJobById(lastAction.jobId)?.toDomain()
    }

    suspend fun syncFreshJobs(): Result<Int> = withContext(Dispatchers.IO) {
        var newJobsAdded = 0
        try {
            // 1. First attempt to pull live CSV from Google Sheets URL
            val csvUrl = googleSheetCsvUrl
            if (csvUrl.isNotBlank()) {
                try {
                    val api = getApiService()
                    val response = api.fetchCsvUrl(csvUrl)
                    if (response.isSuccessful) {
                        val body = response.body()?.string()
                        if (!body.isNullOrBlank()) {
                            val parsedJobs = parseLiveCsv(body)
                            if (parsedJobs.isNotEmpty()) {
                                dao.insertNewOnly(parsedJobs.map { JobListingEntity.fromDomain(it) })
                                newJobsAdded += parsedJobs.size
                                Log.d("JobRepository", "Live Google Sheet synced ${parsedJobs.size} candidate jobs.")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.i("JobRepository", "Live Google Sheet sync skipped or not public: ${e.message}")
                }
            }

            // 2. Attempt Webhook GET /jobs if configured with an active host
            if (!webhookBaseUrl.contains("jobmatch.internal")) {
                try {
                    val api = getApiService()
                    val resp = api.getPendingJobs()
                    if (resp.isSuccessful) {
                        val remoteList = resp.body().orEmpty()
                        val entities = remoteList.mapNotNull { dto ->
                            val id = dto.id ?: return@mapNotNull null
                            val company = dto.companyName ?: "Unknown"
                            val role = dto.roleTitle ?: "Engineer"
                            JobListingEntity(
                                id = id,
                                companyName = company,
                                roleTitle = role,
                                location = dto.location ?: "Remote",
                                salaryRange = dto.salaryRange,
                                skillsJson = (dto.skills ?: SeedDataProvider.extractSkills(role, "")).joinToString(","),
                                descriptionSnippet = dto.descriptionSnippet ?: "",
                                sourceUrl = dto.sourceUrl ?: "",
                                status = JobListing.STATUS_PENDING,
                                fundingStage = dto.fundingStage ?: "Venture Funded",
                                matchScore = dto.matchScore ?: "9/10",
                                notes = dto.notes ?: "",
                                dateAdded = dto.dateAdded ?: "2026-10-04",
                                outreachSent = "No",
                                response = "No"
                            )
                        }
                        if (entities.isNotEmpty()) {
                            dao.insertNewOnly(entities)
                            newJobsAdded += entities.size
                        }
                    }
                } catch (e: Exception) {
                    Log.i("JobRepository", "Webhook GET /jobs endpoint not active: ${e.message}")
                }
            }

            lastSyncTimestamp = System.currentTimeMillis()
            Result.success(newJobsAdded)
        } catch (e: Exception) {
            Log.e("JobRepository", "Sync failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun parseLiveCsv(csvText: String): List<JobListing> {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        return lines.drop(1).mapIndexedNotNull { index, line ->
            val tokens = splitCsvLine(line)
            if (tokens.size < 5) return@mapIndexedNotNull null

            val date = tokens.getOrElse(0) { "" }
            val company = tokens.getOrElse(1) { "" }
            val funding = tokens.getOrElse(2) { "" }
            val location = tokens.getOrElse(3) { "" }
            val role = tokens.getOrElse(4) { "" }
            val matchScore = tokens.getOrElse(5) { "9/10" }
            val applyLink = tokens.getOrElse(6) { "" }
            val outreach = tokens.getOrElse(7) { "No" }
            val response = tokens.getOrElse(8) { "No" }
            val notes = tokens.getOrElse(9) { "" }

            if (company.isBlank() || role.isBlank()) return@mapIndexedNotNull null

            // Stable ID based on company + role
            val cleanId = "job_sync_${company.lowercase().replace(Regex("[^a-z0-9]"), "")}_$index"
            val skills = SeedDataProvider.extractSkills(role, notes)

            JobListing(
                id = cleanId,
                companyName = company,
                roleTitle = role,
                location = location,
                salaryRange = "Competitive + Equity",
                skills = skills,
                descriptionSnippet = notes,
                sourceUrl = applyLink,
                status = JobListing.STATUS_PENDING,
                fundingStage = funding,
                matchScore = matchScore,
                notes = notes,
                dateAdded = date,
                outreachSent = outreach,
                response = response
            )
        }
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString().trim())
        return result
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        dao.clearAll()
        undoStack.clear()
        val entities = SeedDataProvider.initialJobs.map { JobListingEntity.fromDomain(it) }
        dao.insertAll(entities)
        lastSyncTimestamp = System.currentTimeMillis()
    }
}
