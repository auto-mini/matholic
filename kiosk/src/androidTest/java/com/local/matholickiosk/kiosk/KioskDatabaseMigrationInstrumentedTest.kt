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

    @Test
    fun migration2To3PreservesCredentialAndAddsUnknownPinLength() {
        helper.createDatabase(databaseName, 2).apply {
            execSQL(
                """
                INSERT INTO admin_credential (
                    singletonId, salt, derivedKey, iterations, verifierVersion,
                    consecutiveFailures, lockedUntilEpochMs, updatedAtEpochMs
                ) VALUES (1, X'01', X'02', 3, 1, 0, 0, 1000)
                """.trimIndent(),
            )
            close()
        }

        helper.runMigrationsAndValidate(
            databaseName,
            3,
            true,
            KioskDatabase.MIGRATION_2_3,
        ).use { database ->
            database.query(
                "SELECT pinLength, updatedAtEpochMs " +
                    "FROM admin_credential WHERE singletonId = 1",
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
                assertEquals(1000L, cursor.getLong(1))
            }
        }
    }

    @Test
    fun migration3To4PurgesOnlyInactiveCredentialBlobs() {
        helper.createDatabase(databaseName, 3).apply {
            execSQL(
                """
                INSERT INTO students (
                    studentId, displayNameExact, displayNameMasked,
                    usernameCiphertext, usernameIv, usernameEncryptionVersion,
                    passwordCiphertext, passwordIv, passwordEncryptionVersion,
                    qrTokenHash, isActive, createdAtEpochMs, updatedAtEpochMs
                ) VALUES
                    ('inactive', '비활성', '비활성', X'0102', X'03', 1,
                     X'0405', X'06', 1, X'07', 0, 1000, 2000),
                    ('active', '활성', '활성', X'1112', X'13', 1,
                     X'1415', X'16', 1, X'17', 1, 1000, 2000)
                """.trimIndent(),
            )
            close()
        }

        helper.runMigrationsAndValidate(
            databaseName,
            4,
            true,
            KioskDatabase.MIGRATION_3_4,
        ).use { database ->
            database.query(
                "SELECT length(usernameCiphertext), length(usernameIv), " +
                    "usernameEncryptionVersion, length(passwordCiphertext), " +
                    "length(passwordIv), passwordEncryptionVersion " +
                    "FROM students WHERE studentId = 'inactive'",
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
                assertEquals(0, cursor.getInt(1))
                assertEquals(0, cursor.getInt(2))
                assertEquals(0, cursor.getInt(3))
                assertEquals(0, cursor.getInt(4))
                assertEquals(0, cursor.getInt(5))
            }
            database.query(
                "SELECT hex(usernameCiphertext), hex(passwordCiphertext) " +
                    "FROM students WHERE studentId = 'active'",
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals("1112", cursor.getString(0))
                assertEquals("1415", cursor.getString(1))
            }
        }
    }
}
