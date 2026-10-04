package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.JobListing

@Entity(tableName = "job_listings")
data class JobListingEntity(
    @PrimaryKey val id: String,
    val companyName: String,
    val roleTitle: String,
    val location: String,
    val salaryRange: String?,
    val skillsJson: String,
    val descriptionSnippet: String,
    val sourceUrl: String,
    val status: String,
    val fundingStage: String,
    val matchScore: String,
    val notes: String,
    val dateAdded: String,
    val outreachSent: String,
    val response: String,
    val updatedAtTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): JobListing = JobListing(
        id = id,
        companyName = companyName,
        roleTitle = roleTitle,
        location = location,
        salaryRange = salaryRange,
        skills = if (skillsJson.isBlank()) emptyList() else skillsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        descriptionSnippet = descriptionSnippet,
        sourceUrl = sourceUrl,
        status = status,
        fundingStage = fundingStage,
        matchScore = matchScore,
        notes = notes,
        dateAdded = dateAdded,
        outreachSent = outreachSent,
        response = response
    )

    companion object {
        fun fromDomain(domain: JobListing): JobListingEntity = JobListingEntity(
            id = domain.id,
            companyName = domain.companyName,
            roleTitle = domain.roleTitle,
            location = domain.location,
            salaryRange = domain.salaryRange,
            skillsJson = domain.skills.joinToString(","),
            descriptionSnippet = domain.descriptionSnippet,
            sourceUrl = domain.sourceUrl,
            status = domain.status,
            fundingStage = domain.fundingStage,
            matchScore = domain.matchScore,
            notes = domain.notes,
            dateAdded = domain.dateAdded,
            outreachSent = domain.outreachSent,
            response = domain.response
        )
    }
}
