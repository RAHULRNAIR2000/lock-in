package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [JobListingEntity::class], version = 1, exportSchema = false)
abstract class LockInDatabase : RoomDatabase() {
    abstract fun jobListingDao(): JobListingDao

    companion object {
        @Volatile
        private var INSTANCE: LockInDatabase? = null

        fun getInstance(context: Context): LockInDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LockInDatabase::class.java,
                    "lockin_jobs.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
