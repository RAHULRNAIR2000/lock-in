package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LockInDatabase
import com.example.data.model.JobListing
import com.example.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JobUiState(
    val pendingDeck: List<JobListing> = emptyList(),
    val appliedList: List<JobListing> = emptyList(),
    val rejectedList: List<JobListing> = emptyList(),
    val pendingCount: Int = 0,
    val appliedCount: Int = 0,
    val rejectedCount: Int = 0,
    val isSyncing: Boolean = false,
    val canUndo: Boolean = false,
    val selectedJobForDetails: JobListing? = null,
    val lastSwipedAppliedJob: JobListing? = null,
    val toastMessage: String? = null,
    val webhookUrl: String = "",
    val sheetCsvUrl: String = "",
    val lastSyncFormatted: String = ""
)

private data class JobLists(
    val pending: List<JobListing>,
    val applied: List<JobListing>,
    val rejected: List<JobListing>
)

private data class JobControls(
    val isSyncing: Boolean,
    val canUndo: Boolean,
    val selected: JobListing?,
    val lastApplied: JobListing?,
    val toast: String?
)

class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val database = LockInDatabase.getInstance(application)
    private val repository = JobRepository(database.jobListingDao(), application)

    private val _isSyncing = MutableStateFlow(false)
    private val _canUndo = MutableStateFlow(false)
    private val _selectedJob = MutableStateFlow<JobListing?>(null)
    private val _lastAppliedJob = MutableStateFlow<JobListing?>(null)
    private val _toastMessage = MutableStateFlow<String?>(null)

    private val jobListsFlow = combine(
        repository.pendingJobs,
        repository.appliedJobs,
        repository.rejectedJobs
    ) { pending, applied, rejected ->
        JobLists(pending, applied, rejected)
    }

    private val jobControlsFlow = combine(
        _isSyncing,
        _canUndo,
        _selectedJob,
        _lastAppliedJob,
        _toastMessage
    ) { syncing, canUndo, selected, lastApplied, toast ->
        JobControls(syncing, canUndo, selected, lastApplied, toast)
    }

    val uiState: StateFlow<JobUiState> = combine(
        jobListsFlow,
        jobControlsFlow
    ) { lists, controls ->
        JobUiState(
            pendingDeck = lists.pending,
            appliedList = lists.applied,
            rejectedList = lists.rejected,
            pendingCount = lists.pending.size,
            appliedCount = lists.applied.size,
            rejectedCount = lists.rejected.size,
            isSyncing = controls.isSyncing,
            canUndo = controls.canUndo,
            selectedJobForDetails = controls.selected,
            lastSwipedAppliedJob = controls.lastApplied,
            toastMessage = controls.toast,
            webhookUrl = repository.webhookBaseUrl,
            sheetCsvUrl = repository.googleSheetCsvUrl,
            lastSyncFormatted = formatTimestamp(repository.lastSyncTimestamp)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JobUiState()
    )

    init {
        viewModelScope.launch {
            repository.initializeDataIfNeeded()
            _canUndo.value = repository.canUndo()
            // Auto-sync on startup as preferred by user
            syncFreshOpportunities()
        }
    }

    fun onSwipeRight(job: JobListing) {
        viewModelScope.launch {
            repository.updateJobStatus(job.id, JobListing.STATUS_APPLIED)
            _lastAppliedJob.value = job
            _canUndo.value = repository.canUndo()
        }
    }

    fun onSwipeLeft(job: JobListing) {
        viewModelScope.launch {
            repository.updateJobStatus(job.id, JobListing.STATUS_REJECTED)
            _canUndo.value = repository.canUndo()
        }
    }

    fun onUndo() {
        viewModelScope.launch {
            val restored = repository.undoLastAction()
            _canUndo.value = repository.canUndo()
            if (restored != null) {
                _toastMessage.value = "Restored ${restored.companyName}"
            }
        }
    }

    fun onSelectJob(job: JobListing?) {
        _selectedJob.value = job
    }

    fun clearLastAppliedSnackbar() {
        _lastAppliedJob.value = null
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun onMoveJobStatus(jobId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateJobStatus(jobId, newStatus)
            _canUndo.value = repository.canUndo()
            _toastMessage.value = "Moved to $newStatus"
        }
    }

    fun syncFreshOpportunities() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncFreshJobs()
            _isSyncing.value = false
            if (result.isSuccess) {
                val added = result.getOrDefault(0)
                _toastMessage.value = if (added > 0) "Synced $added new jobs!" else "Up to date with Sheet."
            } else {
                _toastMessage.value = "Sync finished (Offline or Local mode active)"
            }
        }
    }

    fun updateSettings(webhook: String, csvUrl: String) {
        repository.webhookBaseUrl = webhook
        repository.googleSheetCsvUrl = csvUrl
        _toastMessage.value = "Settings updated"
        syncFreshOpportunities()
    }

    fun resetData() {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.resetDatabase()
            _canUndo.value = false
            _isSyncing.value = false
            _toastMessage.value = "Reset to 100+ fresh startup opportunities!"
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return "Never"
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / (1000 * 60)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            else -> "${minutes / 60} hours ago"
        }
    }
}
