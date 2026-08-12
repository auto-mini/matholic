package com.local.matholickiosk.kiosk

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.data.ClassMembershipEntity
import com.local.matholickiosk.kiosk.data.KioskDatabase
import com.local.matholickiosk.kiosk.data.StudentRepository
import com.local.matholickiosk.kiosk.data.StudentCsvRow
import com.local.matholickiosk.kiosk.data.ValidatedStudent
import com.local.matholickiosk.kiosk.domain.KioskState
import com.local.matholickiosk.kiosk.qr.IssuedQrToken
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
        assertArrayEquals(qrHash(registered.issuedQr.payload), stored.qrTokenHash)

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
    fun repositoryKeepsQrHashOwnershipAndWipesEveryIssuedTemporary() {
        val trackingCodec = TrackingQrTokenCodec()
        val trackingRepository = StudentRepository(
            database,
            AndroidKeystoreCredentialCipher(keyAlias),
            trackingCodec,
            "instrumented-test",
        )

        val registered = trackingRepository.registerStudent(
            "가상학생-hash",
            "hash-user".toCharArray(),
            "hash-password".toCharArray(),
        )
        assertTrue(trackingCodec.issuedHashes.single().all { it == 0.toByte() })
        assertArrayEquals(
            qrHash(registered.issuedQr.payload),
            database.studentDao().findById(registered.studentId)!!.qrTokenHash,
        )

        trackingRepository.reissueQr(registered.studentId)
        assertTrue(trackingCodec.issuedHashes.all { hash -> hash.all { it == 0.toByte() } })

        trackingRepository.deactivateStudent(registered.studentId)
        assertTrue(trackingCodec.hashOnlyValues.single().all { it == 0.toByte() })
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
        assertNotNull(repository.validateForActiveSession(qrHash(registered.issuedQr.payload)))
        repository.endSession()
        repository.startSession(classB)
        assertNotNull(repository.validateForActiveSession(qrHash(registered.issuedQr.payload)))
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
        assertNotNull(repository.validateForActiveSession(qrHash(registered.issuedQr.payload)))
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
                tokenHash = qrHash(anotherStudent.issuedQr.payload),
                requiredDisplayNameExact = "테스트",
            ),
        )
        assertNull(database.qrCardStatusDao().find(anotherStudent.studentId)?.lastUsedAtEpochMs)
        assertNotNull(
            repository.validateForActiveSession(
                tokenHash = qrHash(testStudent.issuedQr.payload),
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

        assertNotNull(repository.validateForActiveSession(qrHash(registered.issuedQr.payload)))

        val replacement = repository.reissueQr(registered.studentId)
        assertNull(repository.validateForActiveSession(qrHash(registered.issuedQr.payload)))
        assertNotNull(repository.validateForActiveSession(qrHash(replacement.payload)))
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

        assertNull(repository.validateForActiveSession(qrHash(temporaryStudent.issuedQr.payload)))
        repository.addTemporaryStudents(
            requireNotNull(session.sessionId),
            setOf(temporaryStudent.studentId),
        )
        assertNotNull(repository.validateForActiveSession(qrHash(temporaryStudent.issuedQr.payload)))
    }

    @Test
    fun classSwitchStartsANewSessionAndClearsPreviousTemporaryStudents() {
        val classA = repository.createClass("가상반-전환-A")
        val classB = repository.createClass("가상반-전환-B")
        val memberA = repository.registerStudent(
            "가상학생-A",
            "switch-a".toCharArray(),
            "switch-a-password".toCharArray(),
        )
        val memberB = repository.registerStudent(
            "가상학생-B",
            "switch-b".toCharArray(),
            "switch-b-password".toCharArray(),
        )
        val temporary = repository.registerStudent(
            "가상학생-보강",
            "switch-temporary".toCharArray(),
            "switch-temporary-password".toCharArray(),
        )
        repository.replaceClassMemberships(classA, setOf(memberA.studentId))
        repository.replaceClassMemberships(classB, setOf(memberB.studentId))
        val original = repository.startSession(classA, setOf(temporary.studentId))

        assertEquals(
            listOf(memberB.studentId),
            repository.listTemporaryStudentCandidatesForActiveSession(
                requireNotNull(original.sessionId),
            ).map(ValidatedStudent::studentId),
        )

        val replacement = repository.switchSessionClass(
            expectedSessionId = requireNotNull(original.sessionId),
            targetClassId = classB,
        )

        assertTrue(replacement.sessionId != original.sessionId)
        assertEquals(classB, replacement.classId)
        assertEquals(KioskState.QR_READY.name, replacement.state)
        assertNull(repository.validateForActiveSession(qrHash(temporary.issuedQr.payload)))
        assertNotNull(repository.validateForActiveSession(qrHash(memberB.issuedQr.payload)))
        assertEquals(
            setOf(memberA.studentId, temporary.studentId),
            repository.listTemporaryStudentCandidatesForActiveSession(
                requireNotNull(replacement.sessionId),
            ).mapTo(mutableSetOf(), ValidatedStudent::studentId),
        )
    }

    @Test
    fun rejectedClassSwitchLeavesTheCurrentSessionUntouched() {
        val classA = repository.createClass("가상반-유지-A")
        val emptyClass = repository.createClass("가상반-빈반")
        val member = repository.registerStudent(
            "가상학생-유지",
            "switch-keep".toCharArray(),
            "switch-keep-password".toCharArray(),
        )
        repository.replaceClassMemberships(classA, setOf(member.studentId))
        val original = repository.startSession(classA)

        assertTrue(
            runCatching {
                repository.switchSessionClass(
                    expectedSessionId = requireNotNull(original.sessionId),
                    targetClassId = emptyClass,
                )
            }.isFailure,
        )
        assertEquals(original, repository.currentSession())
        assertTrue(
            runCatching {
                repository.switchSessionClass(
                    expectedSessionId = "stale-session",
                    targetClassId = emptyClass,
                )
            }.isFailure,
        )
        assertEquals(original, repository.currentSession())
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
    fun staleStudentCallbackCannotValidateOrTransitionReplacementSession() {
        val classId = repository.createClass("가상반")
        val registered = repository.registerStudent(
            "가상학생-가",
            "user-a".toCharArray(),
            "password-a".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(registered.studentId))

        val originalSessionId = requireNotNull(repository.startSession(classId).sessionId)
        repository.endSession()
        val replacementSessionId = requireNotNull(repository.startSession(classId).sessionId)

        assertNull(
            repository.validateManualStudentForActiveSession(
                registered.studentId,
                originalSessionId,
            ),
        )
        assertTrue(
            runCatching {
                repository.transitionSession(
                    expectedState = KioskState.QR_READY,
                    state = KioskState.PRELOGIN_CHECK,
                    expectedSessionId = originalSessionId,
                    currentStudentId = registered.studentId,
                )
            }.isFailure,
        )
        assertEquals(replacementSessionId, repository.currentSession()?.sessionId)
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
        assertNotNull(repository.validateForActiveSession(qrHash(registered.issuedQr.payload)))
        repository.recordQrExportRequested(registered.studentId)
        repository.deactivateStudent(registered.studentId)

        assertNull(repository.validateForActiveSession(qrHash(registered.issuedQr.payload)))
        assertTrue(repository.listStudents().none { it.studentId == registered.studentId })
        assertFalse(
            database.studentDao().findById(registered.studentId)!!
                .qrTokenHash.contentEquals(qrHash(registered.issuedQr.payload)),
        )
        val deactivated = database.studentDao().findById(registered.studentId)!!
        assertTrue(deactivated.usernameCiphertext.isEmpty())
        assertTrue(deactivated.usernameIv.isEmpty())
        assertEquals(0, deactivated.usernameEncryptionVersion)
        assertTrue(deactivated.passwordCiphertext.isEmpty())
        assertTrue(deactivated.passwordIv.isEmpty())
        assertEquals(0, deactivated.passwordEncryptionVersion)
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
    fun activeSessionBlocksClassMembershipReplacement() {
        val classId = repository.createClass("월1")
        val member = repository.registerStudent(
            "가상학생-소속잠금",
            "locked-member-user".toCharArray(),
            "locked-member-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(member.studentId))
        repository.startSession(classId)

        assertTrue(
            runCatching {
                repository.replaceClassMemberships(classId, emptySet())
            }.isFailure,
        )
        assertEquals(setOf(member.studentId), repository.membershipStudentIds(classId))
    }

    @Test
    fun inactiveMembershipsCannotPoisonClassMembershipEdits() {
        val classId = repository.createClass("월1")
        val inactive = repository.registerStudent(
            "비활성 학생",
            "inactive-membership-user".toCharArray(),
            "inactive-membership-password".toCharArray(),
        )
        val active = repository.registerStudent(
            "활성 학생",
            "active-membership-user".toCharArray(),
            "active-membership-password".toCharArray(),
        )
        repository.replaceClassMemberships(classId, setOf(inactive.studentId))

        repository.deactivateStudent(inactive.studentId)

        database.openHelper.readableDatabase.query(
            "SELECT COUNT(*) FROM class_memberships WHERE studentId = ?",
            arrayOf(inactive.studentId),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        assertEquals(emptySet<String>(), repository.membershipStudentIds(classId))

        // Simulate a stale row created by an older app version. It must be hidden
        // from the admin selection and removed by the next valid replacement.
        database.classDao().addMembership(ClassMembershipEntity(classId, inactive.studentId))
        assertEquals(emptySet<String>(), repository.membershipStudentIds(classId))

        repository.replaceClassMemberships(classId, setOf(active.studentId))

        assertEquals(setOf(active.studentId), repository.membershipStudentIds(classId))
        database.openHelper.readableDatabase.query(
            "SELECT COUNT(*) FROM class_memberships WHERE studentId = ?",
            arrayOf(inactive.studentId),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
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
                .qrTokenHash.contentEquals(qrHash(first.issuedQr.payload)),
        )
        assertFalse(
            database.studentDao().findById(second.studentId)!!
                .qrTokenHash.contentEquals(qrHash(second.issuedQr.payload)),
        )
        issued.forEach {
            assertTrue(QrTokenCodec().parse(it.issuedQr.payload) is QrParseResult.Valid)
        }
        val audit = database.auditDao().latest(10)
        assertTrue(audit.any { it.eventType == "CLASS_QR_BATCH_REISSUED" })
        assertFalse(audit.joinToString().contains("가상학생"))
    }

    @Test
    fun cardPdfStateTracksCardContentInsteadOfPhysicalDelivery() {
        val firstClassId = repository.createClass("월1")
        val secondClassId = repository.createClass("화2")
        val registered = repository.registerStudent(
            "가상학생-카드상태",
            "card-state-user".toCharArray(),
            "card-state-password".toCharArray(),
        )
        assertTrue(repository.listQrCardStatuses().single().needsCardPdf)

        repository.markCardPdfsSavedToPc(setOf(registered.studentId))
        val saved = repository.listQrCardStatuses().single()
        assertFalse(saved.needsCardPdf)
        assertNotNull(saved.lastPdfSavedAtEpochMs)

        repository.updateStudentCredentials(
            registered.studentId,
            "card-state-user-2".toCharArray(),
            "card-state-password-2".toCharArray(),
        )
        repository.replaceClassMemberships(firstClassId, setOf(registered.studentId))
        repository.replaceClassMemberships(firstClassId, emptySet())
        repository.replaceClassMemberships(secondClassId, setOf(registered.studentId))
        assertFalse(repository.listQrCardStatuses().single().needsCardPdf)

        repository.updateStudentProfile(registered.studentId, "가상학생-이름변경")
        assertTrue(repository.listQrCardStatuses().single().needsCardPdf)

        repository.markCardPdfsSavedToPc(setOf(registered.studentId))
        val oldHash = database.studentDao().findById(registered.studentId)!!.qrTokenHash.copyOf()
        val reissued = repository.reissueQr(registered.studentId)
        val reissuedHash = qrHash(reissued.payload)
        assertTrue(repository.listQrCardStatuses().single().needsCardPdf)
        assertFalse(oldHash.contentEquals(reissuedHash))

        repository.deactivateStudent(registered.studentId)
        assertTrue(repository.listQrCardStatuses().isEmpty())
        assertFalse(
            database.studentDao().findById(registered.studentId)!!
                .qrTokenHash.contentEquals(reissuedHash),
        )
        oldHash.fill(0)
        reissuedHash.fill(0)
    }

    @Test
    fun reusableCardSlotKeepsQrAcrossAssignmentAndRelease() {
        val prepared = repository.prepareReusableCardSlots()
        assertEquals(4, prepared.size)
        val preparedHashes = prepared.associate { item ->
            item.studentId to database.studentDao().findById(item.studentId)!!.qrTokenHash.copyOf()
        }
        assertEquals(0, repository.ensureReusableCardSlots().size)
        preparedHashes.forEach { (studentId, hash) ->
            assertArrayEquals(hash, database.studentDao().findById(studentId)!!.qrTokenHash)
            hash.fill(0)
        }
        repository.markCardPdfsSavedToPc(prepared.mapTo(mutableSetOf()) { it.studentId })

        val slot = repository.listReusableCardSlots().first()
        val originalHash = database.studentDao().findById(slot.studentId)!!.qrTokenHash.copyOf()
        assertFalse(slot.isAssigned)
        assertFalse(slot.needsCardPdf)
        assertTrue(repository.listStudents().none { it.studentId == slot.studentId })
        repository.decryptCredentials(slot.studentId).use {
            assertTrue(it.username.isEmpty())
            assertTrue(it.password.isEmpty())
        }

        val classId = repository.createClass("재사용 카드 수업")
        val regular = repository.registerStudent(
            "기존 학생",
            "existing-user".toCharArray(),
            "existing-password".toCharArray(),
        )
        repository.markCardPdfsSavedToPc(setOf(regular.studentId))
        repository.replaceClassMemberships(classId, setOf(regular.studentId))
        val session = repository.startSession(classId)
        assertNull(
            repository.validateForActiveSession(
                qrHash(prepared.first { it.studentId == slot.studentId }.issuedQr.payload),
            ),
        )

        val username = "new-user".toCharArray()
        val password = "new-password".toCharArray()
        val assigned = repository.assignReusableCardSlot(
            slotStudentId = slot.studentId,
            displayNameExact = "신규 학생",
            username = username,
            password = password,
        )
        assertTrue(username.all { it == '\u0000' })
        assertTrue(password.all { it == '\u0000' })
        assertEquals(slot.slotLabel, assigned.slotLabel)
        assertArrayEquals(
            originalHash,
            database.studentDao().findById(slot.studentId)!!.qrTokenHash,
        )
        repository.updateStudentProfile(slot.studentId, "신규 학생 이름변경")
        assertFalse(database.qrCardStatusDao().find(slot.studentId)!!.needsPrint)
        assertArrayEquals(
            originalHash,
            database.studentDao().findById(slot.studentId)!!.qrTokenHash,
        )
        assertNull(
            repository.validateForActiveSession(
                qrHash(prepared.first { it.studentId == slot.studentId }.issuedQr.payload),
            ),
        )
        assertTrue(
            repository.listTemporaryStudentCandidatesForActiveSession(requireNotNull(session.sessionId))
                .any { it.studentId == slot.studentId },
        )
        assertTrue(runCatching { repository.reissueQr(slot.studentId) }.isFailure)
        assertArrayEquals(
            originalHash,
            database.studentDao().findById(slot.studentId)!!.qrTokenHash,
        )
        assertEquals(0, repository.ensureReusableCardSlots().size)
        assertEquals(4, repository.listReusableCardSlots().size)
        repository.endSession()
        val csvRow = StudentCsvRow(
            displayNameExact = "신규 학생 CSV 이름",
            username = "new-user".toCharArray(),
            password = "new-password-csv".toCharArray(),
            classNames = setOf("재사용 카드 수업"),
        )
        val preview = repository.previewStudentImport(listOf(csvRow))
        assertEquals(1, preview.updated)
        assertEquals(1, preview.renamed)
        assertEquals(0, preview.cardsNeedingPdfAfterImport)
        val imported = repository.importStudents(listOf(csvRow))
        assertEquals(0, imported.cardsNeedingPdf)
        assertFalse(database.qrCardStatusDao().find(slot.studentId)!!.needsPrint)
        assertArrayEquals(
            originalHash,
            database.studentDao().findById(slot.studentId)!!.qrTokenHash,
        )
        repository.replaceClassMemberships(
            classId,
            setOf(regular.studentId, slot.studentId),
        )
        assertTrue(runCatching { repository.reissueQrBatch(setOf(slot.studentId)) }.isFailure)
        assertTrue(runCatching { repository.reissueClassQrBatch(classId) }.isFailure)
        assertArrayEquals(
            originalHash,
            database.studentDao().findById(slot.studentId)!!.qrTokenHash,
        )

        repository.releaseReusableCardSlot(slot.studentId)
        val released = repository.listReusableCardSlots()
            .first { it.studentId == slot.studentId }
        assertFalse(released.isAssigned)
        assertTrue(repository.listStudents().none { it.studentId == slot.studentId })
        assertTrue(repository.membershipStudentIds(classId).none { it == slot.studentId })
        repository.decryptCredentials(slot.studentId).use {
            assertTrue(it.username.isEmpty())
            assertTrue(it.password.isEmpty())
        }
        assertArrayEquals(
            originalHash,
            database.studentDao().findById(slot.studentId)!!.qrTokenHash,
        )
        assertEquals(0, repository.prepareReusableCardSlots().size)
    }

    @Test
    fun ensureReusableCardSlotsRepairsInactiveLegacySlot() {
        val prepared = repository.prepareReusableCardSlots()
        repository.markCardPdfsSavedToPc(prepared.mapTo(mutableSetOf()) { it.studentId })
        val slot = repository.listReusableCardSlots().first()
        val legacyHash = ByteArray(32) { index -> (index + 1).toByte() }
        val classId = repository.createClass("비활성 슬롯 복구 수업")
        database.classDao().addMembership(ClassMembershipEntity(classId, slot.studentId))
        val entity = requireNotNull(database.studentDao().findById(slot.studentId))
        database.studentDao().update(
            entity.copy(
                displayNameExact = "과거 배정 학생",
                displayNameMasked = "과거 배정 학생",
                qrTokenHash = legacyHash,
                isActive = false,
                reusableCardAssigned = true,
            ),
        )
        assertEquals(3, repository.listReusableCardSlots().size)

        val repaired = repository.ensureReusableCardSlots()

        assertEquals(1, repaired.size)
        assertEquals(slot.studentId, repaired.single().studentId)
        assertEquals(slot.slotLabel, repaired.single().displayNameExact)
        val restored = requireNotNull(database.studentDao().findById(slot.studentId))
        assertTrue(restored.isActive)
        assertFalse(restored.reusableCardAssigned)
        assertEquals(slot.slotLabel, restored.displayNameExact)
        assertFalse(legacyHash.contentEquals(restored.qrTokenHash))
        val repairedHash = qrHash(repaired.single().issuedQr.payload)
        assertArrayEquals(repairedHash, restored.qrTokenHash)
        assertEquals(4, repository.listReusableCardSlots().size)
        assertTrue(database.qrCardStatusDao().find(slot.studentId)!!.needsPrint)
        assertFalse(repository.membershipStudentIds(classId).contains(slot.studentId))
        repository.decryptCredentials(slot.studentId).use {
            assertTrue(it.username.isEmpty())
            assertTrue(it.password.isEmpty())
        }
        legacyHash.fill(0)
        repairedHash.fill(0)
    }

    @Test
    fun assignedReusableCardMovesToNewQrAndResetsOldDummySlot() {
        val prepared = repository.prepareReusableCardSlots()
        repository.markCardPdfsSavedToPc(prepared.mapTo(mutableSetOf()) { it.studentId })
        val slot = repository.listReusableCardSlots().first()
        val classId = repository.createClass("실제 카드 전환 수업")
        val username = "move-user".toCharArray()
        val password = "move-password".toCharArray()
        repository.assignReusableCardSlot(
            slotStudentId = slot.studentId,
            displayNameExact = "전환 학생",
            username = username,
            password = password,
        )
        repository.replaceClassMemberships(classId, setOf(slot.studentId))
        val oldHash = database.studentDao().findById(slot.studentId)!!.qrTokenHash.copyOf()

        val moved = repository.moveReusableCardToRegularStudent(slot.studentId)

        assertFalse(moved.studentId == slot.studentId)
        assertEquals(slot.slotLabel, moved.slotLabel)
        assertEquals("전환 학생", moved.displayNameExact)
        assertFalse(oldHash.contentEquals(qrHash(moved.issuedQr.payload)))
        assertArrayEquals(
            oldHash,
            database.studentDao().findById(slot.studentId)!!.qrTokenHash,
        )
        val resetSlot = repository.listReusableCardSlots().first { it.studentId == slot.studentId }
        assertFalse(resetSlot.isAssigned)
        repository.decryptCredentials(slot.studentId).use {
            assertTrue(it.username.isEmpty())
            assertTrue(it.password.isEmpty())
        }
        repository.decryptCredentials(moved.studentId).use {
            assertArrayEquals("move-user".toCharArray(), it.username)
            assertArrayEquals("move-password".toCharArray(), it.password)
        }
        assertEquals(setOf(moved.studentId), repository.membershipStudentIds(classId))
        assertTrue(repository.listStudents().any { it.studentId == moved.studentId })
        assertTrue(repository.listStudents().none { it.studentId == slot.studentId })
        assertFalse(database.qrCardStatusDao().find(slot.studentId)!!.needsPrint)
        assertTrue(database.qrCardStatusDao().find(moved.studentId)!!.needsPrint)
        oldHash.fill(0)
    }

    @Test
    fun csvImportUpdatesByLoginIdAndTracksOnlyCardsNeedingPdf() {
        val classA = repository.createClass("월1")
        val classB = repository.createClass("화2")
        val existing = repository.registerStudent(
            "기존 이름",
            "existing-user".toCharArray(),
            "old-password".toCharArray(),
        )
        repository.replaceClassMemberships(classA, setOf(existing.studentId))
        repository.markCardPdfsSavedToPc(setOf(existing.studentId))

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
        val pending = repository.listQrCardStatuses().filter { it.needsCardPdf }
        assertEquals(2, pending.size)

        val issued = repository.reissueQrBatch(pending.mapTo(mutableSetOf()) { it.studentId })
        repository.markCardPdfsSavedToPc(issued.mapTo(mutableSetOf()) { it.studentId })
        assertTrue(repository.listQrCardStatuses().none { it.needsCardPdf })
    }

    private fun qrHash(payload: String): ByteArray {
        val parsed = QrTokenCodec().parse(payload)
        return requireNotNull((parsed as? QrParseResult.Valid)?.hash)
    }

    private class TrackingQrTokenCodec : QrTokenCodec() {
        val issuedHashes = mutableListOf<ByteArray>()
        val hashOnlyValues = mutableListOf<ByteArray>()

        override fun issue(): IssuedQrToken = super.issue().also { issuedHashes += it.hash }

        override fun issueHashOnly(): ByteArray =
            super.issueHashOnly().also(hashOnlyValues::add)
    }
}
