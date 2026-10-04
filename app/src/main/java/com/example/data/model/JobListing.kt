package com.example.data.model

data class JobListing(
    val id: String,
    val companyName: String,
    val roleTitle: String,
    val location: String,
    val salaryRange: String? = null,
    val skills: List<String> = emptyList(),
    val descriptionSnippet: String = "",
    val sourceUrl: String = "",
    val status: String = STATUS_PENDING,
    val fundingStage: String = "",
    val matchScore: String = "9/10",
    val notes: String = "",
    val dateAdded: String = "",
    val outreachSent: String = "No",
    val response: String = "No"
) {
    companion object {
        const val STATUS_PENDING = "Pending"
        const val STATUS_APPLIED = "Applied"
        const val STATUS_REJECTED = "Rejected"
    }

    val isPending: Boolean get() = status == STATUS_PENDING
    val isApplied: Boolean get() = status == STATUS_APPLIED
    val isRejected: Boolean get() = status == STATUS_REJECTED
}
