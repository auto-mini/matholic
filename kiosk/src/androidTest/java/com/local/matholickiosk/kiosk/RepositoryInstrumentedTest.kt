package com.local.matholickiosk.kiosk

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.data.KioskDatabase
import com.local.matholickiosk.kiosk.data.StudentRepository
import com.local.matholickiosk.kiosk.qr.QrParseResult
import com.local.matholickiosk.kiosk.qr.QrTokenCodec
import com.local.matholickiosk.kiosk.security.AndroidKeystoreCredentialCipher
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RepositoryInstrumentedTest {
    private lateinit var database: KioskDatabase
    private lateinit var repository: StudentRepository
    private lateinit var keyAlias: String

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KioskDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        keyAlias = "test-${UUID.randomUUID()}"
        repository = StudentRepository(
            database,
            AndroidKeystoreCredentialCipher(keyAlias),
            QrTokenCodec(),
            "instrumented-test",
        )
    }

    @After
    fun teardown() {
        database.close()
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(keyAlias)
    }

    @Test
    fun registrationStoresNoPlaintextAndCredentialReadIsWipeable() {
        val rejectedUsername = "rejected-user".toCharArray()
        val rejectedPassword = "rejected-password".toCharArray()
        assertTrue(
            runCatching {
                repository.registerStudent(
                    "missing-class",
                    "가상학생-거절",
                    "가상학생-*",
                    rejectedUsername,
                    rejectedPassword,
                )
            }.isFailure,
        )
        assertTrue(rejectedUsername.all { it == '\u0000' })
        assertTrue(rejectedPassword.all { it == '\u0000' })

        val classId = repository.createClass("가상반")
        val username = "synthetic-user".toCharArray()
        val password = "synthetic-password".toCharArray()
        val registered = repository.registerStudent(
            classId,
            "가상학생-가",
            "가상학생-*",
            username,
            password,
        )

        assertTrue(username.all { it == '\u0000' })
        assertTrue(password.all { it == '\u0000' })
        val stored = database.studentDao().findById(registered.studentId)!!
        assertFalse(stored.usernameCiphertext.contentEquals("synthetic-user".toByteArray()))
        assertFalse(stored.passwordCiphertext.contentEquals("synthetic-password".toByteArray()))
        assertArrayEquals(registered.issuedQr.hash, stored.qrTokenHash)

        val decrypted = repository.decryptCredentials(registered.studentId)
        assertArrayEquals("synthetic-user".toCharArray(), decrypted.username)
        assertArrayEquals("synthetic-password".toCharArray(), decrypted.password)
        decrypted.close()
        assertTrue(decrypted.username.all { it == '\u0000' })
        assertTrue(decrypted.password.all { it == '\u0000' })

        val auditText = database.auditDao().latest(100).joinToString()
        assertFalse(auditText.contains(registered.issuedQr.payload))
        assertFalse(auditText.contains("synthetic-user"))
        assertFalse(auditText.contains("synthetic-password"))
    }

    @Test
    fun reissueRevokesOldQrAndTemporaryStudentIsSessionOnly() {
        val classA = repository.createClass("가상반-A")
        val classB = repository.createClass("가상반-B")
        val registered = repository.registerStudent(
            classA,
            "가상학생-가",
            "가상학생-*",
            "user-a".toCharArray(),
            "password-a".toCharArray(),
        )
        val session = repository.startSession(classB)

        assertNull(repository.validateForActiveSession(registered.issuedQr.hash))
        repository.addTemporaryStudent(session.sessionId!!, registered.studentId)
        assertNotNull(repository.validateForActiveSession(registered.issuedQr.hash))

        val replacement = repository.reissueQr(registered.studentId)
        assertNull(repository.validateForActiveSession(registered.issuedQr.hash))
        assertNotNull(repository.validateForActiveSession(replacement.hash))
        assertTrue(QrTokenCodec().parse(replacement.payload) is QrParseResult.Valid)

        repository.endSession()
        val nextSession = repository.startSession(classB)
        assertNotNull(nextSession.sessionId)
        assertNull(repository.validateForActiveSession(replacement.hash))
    }

    @Test
    fun profileCredentialUpdateAndDeactivationAreAuditedAndFailClosed() {
        val classId = repository.createClass("가상반")
        val registered = repository.registerStudent(
            classId,
            "가상학생-이전",
            "가상학생-*",
            "old-user".toCharArray(),
            "old-password".toCharArray(),
        )
        val original = database.studentDao().findById(registered.studentId)!!

        repository.updateStudentProfile(
            registered.studentId,
            "가상학생-변경",
            "가상학생-○",
        )
        val newUsername = "new-user".toCharArray()
        val newPassword = "new-password".toCharArray()
        repository.updateStudentCredentials(
            registered.studentId,
            newUsername,
            newPassword,
        )

        assertTrue(newUsername.all { it == '\u0000' })
        assertTrue(newPassword.all { it == '\u0000' })
        val updated = database.studentDao().findById(registered.studentId)!!
        assertEquals("가상학생-변경", updated.displayNameExact)
        assertEquals("가상학생-○", updated.displayNameMasked)
        assertFalse(original.usernameIv.contentEquals(updated.usernameIv))
        assertFalse(original.passwordIv.contentEquals(updated.passwordIv))
        repository.decryptCredentials(registered.studentId).use { decrypted ->
            assertArrayEquals("new-user".toCharArray(), decrypted.username)
            assertArrayEquals("new-password".toCharArray(), decrypted.password)
        }

        val session = repository.startSession(classId)
        assertNotNull(repository.validateForActiveSession(registered.issuedQr.hash))
        repository.recordQrPrintRequested(registered.studentId)
        repository.deactivateStudent(registered.studentId)

        assertNull(repository.validateForActiveSession(registered.issuedQr.hash))
        assertTrue(repository.listStudents().none { it.studentId == registered.studentId })
        assertFalse(
            database.studentDao().findById(registered.studentId)!!
                .qrTokenHash.contentEquals(registered.issuedQr.hash),
        )
        val rejectedUsername = "inactive-user".toCharArray()
        val rejectedPassword = "inactive-password".toCharArray()
        assertTrue(
            runCatching {
                repository.updateStudentCredentials(
                    registered.studentId,
                    rejectedUsername,
                    rejectedPassword,
                )
            }.isFailure,
        )
        assertTrue(rejectedUsername.all { it == '\u0000' })
        assertTrue(rejectedPassword.all { it == '\u0000' })

        val auditEvents = database.auditDao().latest(100)
        assertTrue(auditEvents.any { it.eventType == "STUDENT_PROFILE_UPDATED" })
        assertTrue(auditEvents.any { it.eventType == "STUDENT_CREDENTIALS_UPDATED" })
        assertTrue(auditEvents.any { it.eventType == "QR_PRINT_REQUESTED" })
        assertTrue(auditEvents.any { it.eventType == "QR_REVOKED" })
        assertTrue(auditEvents.any { it.eventType == "STUDENT_DEACTIVATED" })
        assertTrue(auditEvents.any { it.sessionId == session.sessionId })
        val auditText = auditEvents.joinToString()
        assertFalse(auditText.contains("new-user"))
        assertFalse(auditText.contains("new-password"))
    }
}
