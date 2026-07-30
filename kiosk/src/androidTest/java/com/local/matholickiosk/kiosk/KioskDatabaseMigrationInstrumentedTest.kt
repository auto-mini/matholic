package com.local.matholickiosk.kiosk

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.local.matholickiosk.kiosk.data.KioskDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KioskDatabaseMigrationInstrumentedTest {
    private val databaseName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        KioskDatabase::class.java,
    )

    @Test
    fun migration1To2PreservesStudentsAndAddsDeliveredCardState() {
        helper.createDatabase(databaseName, 1).apply {
            execSQL(
                """
                INSERT INTO students (
                    studentId, displayNameExact, displayNameMasked,
                    usernameCiphertext, usernameIv, usernameEncryptionVersion,
                    passwordCiphertext, passwordIv, passwordEncryptionVersion,
                    qrTokenHash, isActive, createdAtEpochMs, updatedAtEpochMs
                ) VALUES (
                    'student-1', '테스트', '테스트',
                    X'01', X'02', 1,
                    X'03', X'04', 1,
                    X'05', 1, 1000, 2000
                )
                """.trimIndent(),
            )
            close()
        }

        helper.runMigrationsAndValidate(
            databaseName,
            2,
            true,
            KioskDatabase.MIGRATION_1_2,
        ).use { database ->
            database.query(
                "SELECT issuedAtEpochMs, lastDeliveredAtEpochMs, needsPrint " +
                    "FROM qr_card_status WHERE studentId = 'student-1'",
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals(2000L, cursor.getLong(0))
                assertEquals(2000L, cursor.getLong(1))
                assertEquals(0, cursor.getInt(2))
            }
        }
    }
}
