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
}
