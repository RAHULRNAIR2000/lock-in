package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SeedDataProvider
import com.example.data.model.JobListing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Lock In", appName)
    }

    @Test
    fun `verify seed data provider loads startup opportunities`() {
        val jobs = SeedDataProvider.initialJobs
        assertTrue("Seed data should contain 100+ jobs", jobs.size >= 100)
        val first = jobs.first()
        assertEquals("TrueFoundry", first.companyName)
        assertEquals(JobListing.STATUS_PENDING, first.status)
        assertTrue(first.skills.isNotEmpty())
    }
}
