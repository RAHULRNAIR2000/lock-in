package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateStatusRequest(
    @Json(name = "id") val id: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class UpdateStatusResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "message") val message: String = "Status updated successfully"
)

@JsonClass(generateAdapter = true)
data class RemoteJobDto(
    @Json(name = "id") val id: String?,
    @Json(name = "companyName") val companyName: String?,
    @Json(name = "roleTitle") val roleTitle: String?,
    @Json(name = "location") val location: String?,
    @Json(name = "salaryRange") val salaryRange: String?,
    @Json(name = "skills") val skills: List<String>?,
    @Json(name = "descriptionSnippet") val descriptionSnippet: String?,
    @Json(name = "sourceUrl") val sourceUrl: String?,
    @Json(name = "status") val status: String?,
    @Json(name = "fundingStage") val fundingStage: String?,
    @Json(name = "matchScore") val matchScore: String?,
    @Json(name = "notes") val notes: String?,
    @Json(name = "dateAdded") val dateAdded: String?
)
