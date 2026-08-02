package com.local.matholickiosk.kiosk

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.data.KioskDatabase
import com.local.matholickiosk.kiosk.data.StudentRepository
import com.local.matholickiosk.kiosk.data.StudentCsvRow
import com.local.matholickiosk.kiosk.data.ValidatedStudent
import com.local.matholickiosk.kiosk.domain.KioskState
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
                    "",
                    rejectedUsername,
                    rejectedPassword,
                )
            }.isFailure,
        )
        assertTrue(rejectedUsername.all { it == '\u0000' })
        assertTrue(rejectedPassword.all { it == '\u0000' })

        val username = "synthetic-user".toCharArray()
        val password = "synthetic-password".toCharArray()
        val registered = repository.registerStudent(
            "가상학생-가",
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
    fun sameQrWorksAcrossClassesAndClassDeletePreservesStudent() {
        val classA = repository.createClass("가상반-A")
        val classB = repository.createClass("가상반-B")
        val registered = repository.registerStudent(
            "가상학생-가",
            "user-a".toCharArray(),
            "password-a".toCharArray(),
        )
        repository.replaceClassMemberships(classA, setOf(registered.studentId))
        repository.replaceClassMemberships(classB, setOf(registered.studentId))

        repository.startSession(classA)
        assertNotNull(repository.validateForActiveSession(registered.issuedQr.hash))
        repository.endSession()
        repository.startSession(classB)
        assertNotNull(repository.validateForActiveSession(registered.issuedQr.hash))
        repository.endSession()

        val originalStudent = database.studentDao().findById(registered.studentId)!!
        repository.deleteClass(classA)
        assertTrue(repository.listClasses().none { it.classId == classA })
        assertTrue(repository.membershipStudentIds(classA).isEmpty())
        assertEquals(setOf(registered.studentId), repository.membershipStudentIds(classB))
        val preserved = database.studentDao().findById(registered.studentId)!!
        assertTrue(preserved.isActive)
        assertArrayEquals(originalStudent.qrTokenHash, preserved.qrTokenHash)
        assertArrayEquals(originalStudent.usernameCiphertext, preserved.usernameCiphertext)
        assertArrayEquals(originalStudent.usernameIv, preserved.usernameIv)
        assertArrayEquals(originalStudent.passwordCiphertext, preserved.passwordCiphertext)
        assertArrayEquals(originalStudent.passwordIv, preserved.passwordIv)
        repository.decryptCredentials(registered.studentId).use { credentials ->
            assertArrayEquals("user-a".toCharArray(), credentials.username)
            assertArrayEquals("password-a".toCharArray(), credentials.password)
        }

        repository.startSession(classB)
        assertNotNull(repository.validateForActiveSession(registered.issuedQr.hash))
    }

    @Test
    fun requiredDisplayNameRejectsAnotherEligibleStudentWithoutMarkingQrUsed() {
        val classId = repository.createClass("원격시험반")
        val testStudent = repository.registerStudent(
            "테스트",
            "test-user".toCharArray(),
            "test-password".toCharArray(),
        )
        val anotherStudent = repository.registerStudent(
            "다른학생",
            "another-user".toCharArray(),
            "another-password".toCharArray(),
        )
        repository.replaceClassMemberships(
            classId,
            setOf(testStudent.studentId, anotherStudent.studentId),
        )
        repository.startSession(classId)

        assertNull(
            repository.validateForActiveSession(
                tokenHash = anotherStudent.issuedQr.hash,
                requiredDisplayNameExact = "테스트",
            ),
        )
        assertNull(database.qrCardStatusDao().find(anotherStudent.studentId)?.lastUsedAtEpochMs)
        assertNotNull(
            repository.validateForActiveSession(
                tokenHash = testStudent.issuedQr.hash,
                requiredDisplayNameExact = "테스트",
            ),
        )
        assertNotNull(database.qrCardStatusDao().find(testStudent.studentId)?.lastUsedAtEpochMs)

        val auditEvents = database.auditDao().latest(20)
        assertTrue(
            auditEvents.any {
                it.eventType == "QR_REJECTED" &&
                    it.reasonCode == "REQUIRED_DISPLAY_NAME_MISMATCH" &&
                    it.subjectStudentId == anotherStudent.studentId
            },
        )
        assertFalse(
            auditEvents.any {
                it.eventType == "QR_ACCEPTED" &&
                    it.subjectStudentId == anotherStudent.studentId
            },
        )
    }

    @Test
    fun activeClassNamesAreUniqueButDeletedNamesCanBeReused() {
        val originalClassId = repository.createClass("Synthetic Class")

        assertTrue(
            runCatching {
                repository.createClass("  synthetic class  ")
            }.isFailure,
        )
        assertEquals(1, repository.listClasses().size)

        repository.deleteClass(originalClassId)
        val replacementClassId = repository.createClass("synthetic class")

        assertTrue(replacementClassId != originalClassId)
        assertEquals(
            listOf("synthetic class"),
            repository.listClasses().map { it.className },
        )
    }

    @Test
    fun reissueRevokesOldQrAndTemporaryStudentIsSessionOnly() {
        val classId = repository.createClass("가상반")
        val registered = repository.registerStudent(
            "가상학생-가",
            "user-a".toCharArray(),
            "password-a".toCharArray(),
        )
        repository.startSession(classId, setOf(registered.studentId))

        assertNotNull(repository.validateForActiveSession(registered.issuedQr.hash))

        val replacement = repository.reissueQr(registered.studentId)
        assertNull(repository.validateForActiveSession(registered.issuedQr.hash))
        assertNotNull(repository.validateForActiveSession(replacement.hash))
        assertTrue(QrTokenCodec().parse(replacement.payload) is QrParseResult.Valid)

        repository.endSession()
        assertTrue(runCatching { repository.startSession(classId) }.isFailure)
        assertNull(repository.currentSession()?.sessionId)
    }

    @Test
    fun temporaryStudentBatchDoesNotLeavePartialMembershipOnFailure() {
        val classId = repository.createClass("가상반")
        val classStudent = repository.registerStudent(
            "가상학생-반",
            "class-user".toCharArray(),
            "class-password".toCharArray(),
        )
        val temporaryStudent = repository.registerStudent(
            "가상학생-보강",
            "temporary-user".toCharArray(),
            "temporary-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(classStudent.studentId))
        val session = repository.startSession(classId)

        assertTrue(
            runCatching {
                repository.addTemporaryStudents(
                    requireNotNull(session.sessionId),
                    linkedSetOf(temporaryStudent.studentId, "missing-student"),
                )
            }.isFailure,
        )

        assertNull(repository.validateForActiveSession(temporaryStudent.issuedQr.hash))
        repository.addTemporaryStudents(
            requireNotNull(session.sessionId),
            setOf(temporaryStudent.studentId),
        )
        assertNotNull(repository.validateForActiveSession(temporaryStudent.issuedQr.hash))
    }

    @Test
    fun sessionLifecycleRejectsOverwriteAndDuplicateEnd() {
        val classA = repository.createClass("가상반-A")
        val classB = repository.createClass("가상반-B")
        val registered = repository.registerStudent(
            "가상학생-가",
            "user-a".toCharArray(),
            "password-a".toCharArray(),
        )
        repository.replaceClassMemberships(classA, setOf(registered.studentId))
        repository.replaceClassMemberships(classB, setOf(registered.studentId))

        val active = repository.startSession(classA)

        assertTrue(runCatching { repository.startSession(classB) }.isFailure)
        assertEquals(active.sessionId, repository.currentSession()?.sessionId)
        assertEquals(classA, repository.currentSession()?.classId)

        repository.endSession()

        assertTrue(runCatching { repository.endSession() }.isFailure)
        assertNull(repository.currentSession()?.sessionId)
    }

    @Test
    fun staleWebResultCannotUnlockRestartedOrCompletedSession() {
        val classId = repository.createClass("가상반")
        val registered = repository.registerStudent(
            "가상학생-가",
            "user-a".toCharArray(),
            "password-a".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(registered.studentId))

        repository.startSession(classId)
        repository.transitionSession(
            expectedState = KioskState.QR_READY,
            state = KioskState.PRELOGIN_CHECK,
            currentStudentId = registered.studentId,
        )
        assertEquals(KioskState.LOCKED, repository.applyRestartPolicy())

        assertTrue(
            runCatching {
                repository.transitionSession(
                    expectedState = KioskState.PRELOGIN_CHECK,
                    state = KioskState.QR_READY,
                )
            }.isFailure,
        )
        assertEquals(KioskState.LOCKED.name, repository.currentSession()?.state)

        repository.endSession()
        repository.startSession(classId)
        repository.transitionSession(
            expectedState = KioskState.QR_READY,
            state = KioskState.PRELOGIN_CHECK,
            currentStudentId = registered.studentId,
        )
        repository.transitionSession(
            expectedState = KioskState.PRELOGIN_CHECK,
            state = KioskState.QR_READY,
        )

        assertTrue(
            runCatching {
                repository.transitionSession(
                    expectedState = KioskState.PRELOGIN_CHECK,
                    state = KioskState.QR_READY,
                )
            }.isFailure,
        )
        assertEquals(KioskState.QR_READY.name, repository.currentSession()?.state)
        assertNull(repository.currentSession()?.currentStudentId)
    }

    @Test
    fun profileCredentialUpdateAndDeactivationAreAuditedAndFailClosed() {
        val classId = repository.createClass("가상반")
        val registered = repository.registerStudent(
            "가상학생-이전",
            "old-user".toCharArray(),
            "old-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(registered.studentId))
        val original = database.studentDao().findById(registered.studentId)!!

        repository.updateStudentProfile(
            registered.studentId,
            "가상학생-변경",
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
        assertEquals("가상학생-변경", updated.displayNameMasked)
        assertFalse(original.usernameIv.contentEquals(updated.usernameIv))
        assertFalse(original.passwordIv.contentEquals(updated.passwordIv))
        repository.decryptCredentials(registered.studentId).use { decrypted ->
            assertArrayEquals("new-user".toCharArray(), decrypted.username)
            assertArrayEquals("new-password".toCharArray(), decrypted.password)
        }

        val session = repository.startSession(classId)
        assertNotNull(repository.validateForActiveSession(registered.issuedQr.hash))
        repository.recordQrExportRequested(registered.studentId)
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
        assertTrue(auditEvents.any { it.eventType == "QR_PDF_EXPORT_REQUESTED" })
        assertTrue(auditEvents.any { it.eventType == "QR_REVOKED" })
        assertTrue(auditEvents.any { it.eventType == "STUDENT_DEACTIVATED" })
        assertTrue(auditEvents.any { it.sessionId == session.sessionId })
        val auditText = auditEvents.joinToString()
        assertFalse(auditText.contains("new-user"))
        assertFalse(auditText.contains("new-password"))
    }

    @Test
    fun fixedClassesAreSeededIdempotentlyAndExtraClassesRemainSupported() {
        val fixed = listOf("월1", "월2", "화1")

        repository.ensureClasses(fixed)
        repository.ensureClasses(fixed)
        repository.createClass("테스트반")

        val names = repository.listClasses().map { it.className }
        assertEquals(4, names.size)
        assertTrue(names.containsAll(fixed + "테스트반"))
    }

    @Test
    fun manualStudentSelectionAcceptsOnlyCurrentClassAndTemporaryStudents() {
        val classId = repository.createClass("월1")
        val member = repository.registerStudent(
            "가상학생-소속",
            "member-user".toCharArray(),
            "member-password".toCharArray(),
        )
        val temporary = repository.registerStudent(
            "가상학생-보강",
            "temporary-user".toCharArray(),
            "temporary-password".toCharArray(),
        )
        val outsider = repository.registerStudent(
            "가상학생-외부",
            "outsider-user".toCharArray(),
            "outsider-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(member.studentId))
        repository.startSession(classId, setOf(temporary.studentId))

        val eligible = repository.listEligibleStudentsForActiveSession()
        assertEquals(
            setOf(member.studentId, temporary.studentId),
            eligible.mapTo(mutableSetOf(), ValidatedStudent::studentId),
        )
        assertNotNull(repository.validateManualStudentForActiveSession(member.studentId))
        assertNotNull(repository.validateManualStudentForActiveSession(temporary.studentId))
        assertNull(repository.validateManualStudentForActiveSession(outsider.studentId))
    }

    @Test
    fun classBatchReissueRotatesEveryMemberQrAtomically() {
        val classId = repository.createClass("월1")
        val first = repository.registerStudent(
            "가상학생-1",
            "first-user".toCharArray(),
            "first-password".toCharArray(),
        )
        val second = repository.registerStudent(
            "가상학생-2",
            "second-user".toCharArray(),
            "second-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(first.studentId, second.studentId))

        val issued = repository.reissueClassQrBatch(classId)

        assertEquals(2, issued.size)
        assertFalse(
            database.studentDao().findById(first.studentId)!!
                .qrTokenHash.contentEquals(first.issuedQr.hash),
        )
        assertFalse(
            database.studentDao().findById(second.studentId)!!
                .qrTokenHash.contentEquals(second.issuedQr.hash),
        )
        issued.forEach {
            assertTrue(QrTokenCodec().parse(it.issuedQr.payload) is QrParseResult.Valid)
        }
        val audit = database.auditDao().latest(10)
        assertTrue(audit.any { it.eventType == "CLASS_QR_BATCH_REISSUED" })
        assertFalse(audit.joinToString().contains("가상학생"))
    }

    @Test
    fun csvImportUpdatesByLoginIdAndTracksOnlyCardsNeedingPrint() {
        val classA = repository.createClass("월1")
        val classB = repository.createClass("화2")
        val existing = repository.registerStudent(
            "기존 이름",
            "existing-user".toCharArray(),
            "old-password".toCharArray(),
        )
        repository.replaceClassMemberships(classA, setOf(existing.studentId))
        repository.markCardsDelivered(setOf(existing.studentId))

        val previewRows = listOf(
            StudentCsvRow(
                "변경 이름",
                "existing-user".toCharArray(),
                "new-password".toCharArray(),
                setOf("화2"),
            ),
            StudentCsvRow(
                "신규 학생",
                "new-user".toCharArray(),
                "new-user-password".toCharArray(),
                setOf("월1", "화2"),
            ),
        )
        val preview = repository.previewStudentImport(previewRows)
        assertEquals(1, preview.created)
        assertEquals(1, preview.updated)
        assertEquals(1, preview.renamed)

        val result = repository.importStudents(previewRows)
        assertEquals(1, result.created)
        assertEquals(1, result.updated)
        assertTrue(previewRows.all { row ->
            row.username.all { it == '\u0000' } && row.password.all { it == '\u0000' }
        })
        val updated = repository.listStudents().single { it.studentId == existing.studentId }
        assertEquals("변경 이름", updated.displayNameExact)
        repository.decryptCredentials(existing.studentId).use {
            assertArrayEquals("new-password".toCharArray(), it.password)
        }
        assertFalse(existing.studentId in repository.membershipStudentIds(classA))
        assertTrue(existing.studentId in repository.membershipStudentIds(classB))
        val pending = repository.listQrCardStatuses().filter { it.needsPrint }
        assertEquals(2, pending.size)

        val issued = repository.reissueQrBatch(pending.mapTo(mutableSetOf()) { it.studentId })
        repository.markCardsDelivered(issued.mapTo(mutableSetOf()) { it.studentId })
        assertTrue(repository.listQrCardStatuses().none { it.needsPrint })
        issued.forEach { item -> item.issuedQr.hash.fill(0) }
    }
}
