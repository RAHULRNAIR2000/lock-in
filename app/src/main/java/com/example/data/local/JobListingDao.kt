package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JobListingDao {
    @Query("SELECT * FROM job_listings WHERE status = 'Pending' ORDER BY updatedAtTimestamp ASC")
    fun getPendingJobs(): Flow<List<JobListingEntity>>

    @Query("SELECT * FROM job_listings WHERE status = 'Applied' ORDER BY updatedAtTimestamp DESC")
    fun getAppliedJobs(): Flow<List<JobListingEntity>>

    @Query("SELECT * FROM job_listings WHERE status = 'Rejected' ORDER BY updatedAtTimestamp DESC")
    fun getRejectedJobs(): Flow<List<JobListingEntity>>

    @Query("SELECT * FROM job_listings ORDER BY updatedAtTimestamp DESC")
    fun getAllJobs(): Flow<List<JobListingEntity>>

    @Query("SELECT * FROM job_listings WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: String): JobListingEntity?

    @Query("SELECT COUNT(*) FROM job_listings")
    suspend fun getJobCount(): Int

    @Query("SELECT COUNT(*) FROM job_listings WHERE status = 'Pending'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM job_listings WHERE status = 'Applied'")
    fun getAppliedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM job_listings WHERE status = 'Rejected'")
    fun getRejectedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<JobListingEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewOnly(jobs: List<JobListingEntity>)

    @Query("UPDATE job_listings SET status = :status, updatedAtTimestamp = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM job_listings")
    suspend fun clearAll()
}
