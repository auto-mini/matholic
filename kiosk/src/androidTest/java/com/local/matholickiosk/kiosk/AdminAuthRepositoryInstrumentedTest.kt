package com.local.matholickiosk.kiosk

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.data.AdminAuthRepository
import com.local.matholickiosk.kiosk.data.AdminAuthResult
import com.local.matholickiosk.kiosk.data.KioskDatabase
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminAuthRepositoryInstrumentedTest {
    private lateinit var database: KioskDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KioskDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun scalarEnrollmentQueriesAndAuthenticationRemainConsistent() {
        val repository = AdminAuthRepository(database)
        assertFalse(repository.isEnrolled())

        repository.enroll("654321".toCharArray())

        assertTrue(repository.isEnrolled())
        assertEquals(6, repository.enrolledPinLength())
        assertEquals(
            AdminAuthResult.Success,
            repository.authenticate("654321".toCharArray()),
        )
    }

    @Test
    fun pinInputsAreClearedAcrossDuplicateRejectedLockedAndSuccessfulBranches() {
        var nowEpochMs = 10_000L
        val repository = AdminAuthRepository(database) { nowEpochMs }
        val enrolledPin = "654321".toCharArray()
        repository.enroll(enrolledPin)
        assertArrayEquals(CharArray(enrolledPin.size), enrolledPin)

        val duplicatePin = "123456".toCharArray()
        assertThrows(IllegalStateException::class.java) {
            repository.enroll(duplicatePin)
        }
        assertArrayEquals(CharArray(duplicatePin.size), duplicatePin)

        val rejectedPin = "654322".toCharArray()
        assertEquals(
            AdminAuthResult.Rejected(1_000L),
            repository.authenticate(rejectedPin),
        )
        assertArrayEquals(CharArray(rejectedPin.size), rejectedPin)

        val lockedPin = "654321".toCharArray()
        assertEquals(
            AdminAuthResult.Rejected(1_000L),
            repository.authenticate(lockedPin),
        )
        assertArrayEquals(CharArray(lockedPin.size), lockedPin)

        nowEpochMs += 1_000L
        val acceptedPin = "654321".toCharArray()
        assertEquals(AdminAuthResult.Success, repository.authenticate(acceptedPin))
        assertArrayEquals(CharArray(acceptedPin.size), acceptedPin)
    }
}
