package com.local.matholickiosk.kiosk.data

import com.local.matholickiosk.kiosk.domain.KioskState
import com.local.matholickiosk.kiosk.qr.IssuedQrToken
import com.local.matholickiosk.kiosk.qr.QrTokenCodec
import com.local.matholickiosk.kiosk.security.CredentialCipher
import com.local.matholickiosk.kiosk.security.CredentialField
import com.local.matholickiosk.kiosk.security.EncryptedValue
import java.io.Closeable
import java.util.UUID

data class RegisteredStudent(
    val studentId: String,
    val issuedQr: IssuedQrToken,
)

data class ValidatedStudent(
    val studentId: String,
    val displayNameExact: String,
)

class DecryptedCredentials(
    val username: CharArray,
    val password: CharArray,
) : Closeable {
    override fun close() {
        username.fill('\u0000')
        password.fill('\u0000')
    }
}

class StudentRepository(
    private val database: KioskDatabase,
    private val cipher: CredentialCipher,
    private val qrCodec: QrTokenCodec = QrTokenCodec(),
    private val appVersion: String,
    private val nowEpochMs: () -> Long = System::currentTimeMillis,
) {
    fun createClass(className: String): String {
        val normalized = className.trim()
        require(normalized.isNotEmpty()) { "Class name is required" }
        require(database.classDao().findActiveByName(normalized) == null) {
            "같은 이름의 반이 이미 있습니다."
        }
        val now = nowEpochMs()
        val classId = UUID.randomUUID().toString()
        database.classDao().upsert(
            ClassGroupEntity(classId, normalized, true, now, now),
        )
        audit("CLASS_CREATED", null, null, null)
        return classId
    }

    fun listClasses(): List<ClassGroupEntity> = database.classDao().listActive()

    fun listStudents(): List<StudentEntity> = database.studentDao().listAllActive()

    fun listStudentsForClass(classId: String): List<StudentEntity> =
        database.studentDao().listActiveForClass(classId)

    fun membershipStudentIds(classId: String): Set<String> =
        database.classDao().listMembershipStudentIds(classId).toSet()

    fun currentSession(): ActiveSessionEntity? = database.sessionDao().get()

    fun registerStudent(
        displayNameExact: String,
        username: CharArray,
        password: CharArray,
    ): RegisteredStudent {
        try {
            val exact = displayNameExact.trim()
            require(exact.isNotEmpty()) { "Exact display name is required" }
            require(username.isNotEmpty() && password.isNotEmpty()) { "Credentials are required" }

            val studentId = UUID.randomUUID().toString()
            val issued = qrCodec.issue()
            val usernameEncrypted = cipher.encrypt(studentId, CredentialField.USERNAME, username)
            val passwordEncrypted = cipher.encrypt(studentId, CredentialField.PASSWORD, password)
            val now = nowEpochMs()
            database.runInTransaction {
                database.studentDao().insert(
                    StudentEntity(
                        studentId = studentId,
                        displayNameExact = exact,
                        // Schema-v1 compatibility only. Masked names are no longer
                        // collected or displayed, so the exact name is mirrored here.
                        displayNameMasked = exact,
                        usernameCiphertext = usernameEncrypted.ciphertext,
                        usernameIv = usernameEncrypted.iv,
                        usernameEncryptionVersion = usernameEncrypted.version,
                        passwordCiphertext = passwordEncrypted.ciphertext,
                        passwordIv = passwordEncrypted.iv,
                        passwordEncryptionVersion = passwordEncrypted.version,
                        qrTokenHash = issued.hash,
                        isActive = true,
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now,
                    ),
                )
                audit("STUDENT_REGISTERED", null, studentId, null)
                audit("QR_ISSUED", null, studentId, null)
            }
            return RegisteredStudent(studentId, issued)
        } finally {
            username.fill('\u0000')
            password.fill('\u0000')
        }
    }

    fun reissueQr(studentId: String): IssuedQrToken {
        val student = requireNotNull(database.studentDao().findById(studentId)) { "Student not found" }
        require(student.isActive) { "Student is inactive" }
        val issued = qrCodec.issue()
        database.runInTransaction {
            database.studentDao().update(
                student.copy(qrTokenHash = issued.hash, updatedAtEpochMs = nowEpochMs()),
            )
            audit("QR_REISSUED", null, studentId, null)
        }
        return issued
    }

    fun updateStudentProfile(
        studentId: String,
        displayNameExact: String,
    ) {
        val student = requireNotNull(database.studentDao().findById(studentId)) { "Student not found" }
        require(student.isActive) { "Student is inactive" }
        val exact = displayNameExact.trim()
        require(exact.isNotEmpty()) { "Exact display name is required" }
        database.runInTransaction {
            database.studentDao().update(
                student.copy(
                    displayNameExact = exact,
                    displayNameMasked = exact,
                    updatedAtEpochMs = nowEpochMs(),
                ),
            )
            audit("STUDENT_PROFILE_UPDATED", null, studentId, null)
        }
    }

    fun replaceClassMemberships(classId: String, studentIds: Set<String>) {
        require(database.classDao().findActiveById(classId) != null) {
            "Active class not found"
        }
        val activeStudentIds = database.studentDao().listAllActive()
            .mapTo(mutableSetOf(), StudentEntity::studentId)
        require(studentIds.all(activeStudentIds::contains)) {
            "Inactive or unknown student selected"
        }
        database.runInTransaction {
            database.classDao().clearMemberships(classId)
            studentIds.forEach { studentId ->
                database.classDao().addMembership(
                    ClassMembershipEntity(classId = classId, studentId = studentId),
                )
            }
            audit("CLASS_MEMBERSHIPS_REPLACED", studentIds.size.toString(), null, null)
        }
    }

    fun deleteClass(classId: String) {
        requireNotNull(database.classDao().findActiveById(classId)) {
            "Active class not found"
        }
        val current = database.sessionDao().get()
        require(current?.sessionId == null || current.classId != classId) {
            "Active session class cannot be deleted"
        }
        database.runInTransaction {
            check(database.classDao().deleteById(classId) == 1) {
                "Class delete failed"
            }
            audit("CLASS_DELETED", null, null, null)
        }
    }

    fun updateStudentCredentials(
        studentId: String,
        username: CharArray,
        password: CharArray,
    ) {
        try {
            val student = requireNotNull(database.studentDao().findById(studentId)) {
                "Student not found"
            }
            require(student.isActive) { "Student is inactive" }
            require(username.isNotEmpty() && password.isNotEmpty()) { "Credentials are required" }

            val usernameEncrypted = cipher.encrypt(studentId, CredentialField.USERNAME, username)
            val passwordEncrypted = cipher.encrypt(studentId, CredentialField.PASSWORD, password)
            database.runInTransaction {
                database.studentDao().update(
                    student.copy(
                        usernameCiphertext = usernameEncrypted.ciphertext,
                        usernameIv = usernameEncrypted.iv,
                        usernameEncryptionVersion = usernameEncrypted.version,
                        passwordCiphertext = passwordEncrypted.ciphertext,
                        passwordIv = passwordEncrypted.iv,
                        passwordEncryptionVersion = passwordEncrypted.version,
                        updatedAtEpochMs = nowEpochMs(),
                    ),
                )
                audit("STUDENT_CREDENTIALS_UPDATED", null, studentId, null)
            }
        } finally {
            username.fill('\u0000')
            password.fill('\u0000')
        }
    }

    fun deactivateStudent(studentId: String) {
        val student = requireNotNull(database.studentDao().findById(studentId)) { "Student not found" }
        require(student.isActive) { "Student is inactive" }
        val revokedReplacementHash = qrCodec.issueHashOnly()
        database.runInTransaction {
            database.studentDao().update(
                student.copy(
                    qrTokenHash = revokedReplacementHash,
                    isActive = false,
                    updatedAtEpochMs = nowEpochMs(),
                ),
            )
            audit("QR_REVOKED", null, studentId, null)
            audit("STUDENT_DEACTIVATED", null, studentId, null)
        }
    }

    fun recordQrPrintRequested(studentId: String) {
        val student = requireNotNull(database.studentDao().findById(studentId)) { "Student not found" }
        require(student.isActive) { "Student is inactive" }
        audit("QR_PRINT_REQUESTED", null, studentId, null)
    }

    fun recordQrExportRequested(studentId: String) {
        val student = requireNotNull(database.studentDao().findById(studentId)) { "Student not found" }
        require(student.isActive) { "Student is inactive" }
        audit("QR_PDF_EXPORT_REQUESTED", null, studentId, null)
    }

    fun startSession(
        classId: String,
        temporaryStudentIds: Set<String> = emptySet(),
    ): ActiveSessionEntity {
        val now = nowEpochMs()
        val session = ActiveSessionEntity(
            sessionId = UUID.randomUUID().toString(),
            classId = classId,
            startedAtEpochMs = now,
            state = KioskState.QR_READY.name,
            currentStudentId = null,
            automationStep = null,
            lockedReason = null,
            previousCheckpoint = KioskState.ADMIN_IDLE.name,
            updatedAtEpochMs = now,
        )
        database.runInTransaction {
            require(database.classDao().findActiveById(classId) != null) {
                "Active class not found"
            }
            val activeStudentIds = database.studentDao().listAllActive()
                .mapTo(mutableSetOf(), StudentEntity::studentId)
            require(temporaryStudentIds.all(activeStudentIds::contains)) {
                "Inactive or unknown temporary student selected"
            }
            require(
                database.studentDao().listActiveForClass(classId).isNotEmpty() ||
                    temporaryStudentIds.isNotEmpty(),
            ) {
                "수업에는 반 학생 또는 보강 학생이 한 명 이상 필요합니다."
            }
            require(database.sessionDao().get()?.sessionId == null) {
                "이미 진행 중인 수업이 있습니다."
            }
            database.sessionDao().save(session)
            temporaryStudentIds.forEach { studentId ->
                database.sessionDao().addTemporaryStudent(
                    SessionStudentEntity(session.sessionId!!, studentId, now),
                )
            }
            audit("SESSION_STARTED", null, null, session.sessionId)
            if (temporaryStudentIds.isNotEmpty()) {
                audit(
                    "TEMPORARY_STUDENTS_ADDED",
                    temporaryStudentIds.size.toString(),
                    null,
                    session.sessionId,
                )
            }
        }
        return session
    }

    fun addTemporaryStudent(sessionId: String, studentId: String) {
        val session = requireNotNull(database.sessionDao().get()) { "No active session" }
        require(session.sessionId == sessionId) { "Session mismatch" }
        val student = requireNotNull(database.studentDao().findById(studentId)) { "Student not found" }
        require(student.isActive) { "Student is inactive" }
        database.runInTransaction {
            database.sessionDao().addTemporaryStudent(
                SessionStudentEntity(sessionId, studentId, nowEpochMs()),
            )
            audit("TEMPORARY_STUDENT_ADDED", null, studentId, sessionId)
        }
    }

    fun validateForActiveSession(tokenHash: ByteArray): ValidatedStudent? {
        val session = database.sessionDao().get()
        if (
            session?.sessionId == null ||
            session.classId == null ||
            session.state != KioskState.QR_READY.name
        ) {
            audit("QR_REJECTED", "SESSION_NOT_READY", null, session?.sessionId)
            return null
        }
        val student = database.studentDao().findEligibleByQrHash(
            tokenHash,
            session.classId,
            session.sessionId,
        )
        if (student == null) {
            val known = database.studentDao().findActiveByQrHash(tokenHash)
            audit(
                "QR_REJECTED",
                if (known == null) "UNKNOWN_OR_REVOKED" else "OUTSIDE_CURRENT_CLASS",
                known?.studentId,
                session.sessionId,
            )
            return null
        }
        audit("QR_ACCEPTED", null, student.studentId, session.sessionId)
        return ValidatedStudent(student.studentId, student.displayNameExact)
    }

    fun recordQrRejection(reasonCode: String) {
        val sessionId = database.sessionDao().get()?.sessionId
        audit("QR_REJECTED", reasonCode, null, sessionId)
    }

    fun applyRestartPolicy(): KioskState {
        val current = database.sessionDao().get() ?: return KioskState.ADMIN_IDLE
        val previous = runCatching { KioskState.valueOf(current.state) }
            .getOrDefault(KioskState.RECOVERY_REQUIRED)
        val recovered = com.local.matholickiosk.kiosk.domain.KioskStatePolicy
            .afterProcessRestart(previous)
        if (recovered != previous) {
            database.runInTransaction {
                database.sessionDao().save(
                    current.copy(
                        state = recovered.name,
                        previousCheckpoint = previous.name,
                        lockedReason = if (recovered == KioskState.LOCKED) {
                            "PROCESS_RESTART_DURING_SENSITIVE_STATE"
                        } else {
                            current.lockedReason
                        },
                        updatedAtEpochMs = nowEpochMs(),
                    ),
                )
                audit("PROCESS_RESTART_RECOVERY", recovered.name, current.currentStudentId, current.sessionId)
            }
        }
        return recovered
    }

    fun decryptCredentials(studentId: String): DecryptedCredentials {
        val student = requireNotNull(database.studentDao().findById(studentId)) { "Student not found" }
        require(student.isActive) { "Student is inactive" }
        val username = cipher.decrypt(
            studentId,
            CredentialField.USERNAME,
            EncryptedValue(
                student.usernameCiphertext,
                student.usernameIv,
                student.usernameEncryptionVersion,
            ),
        )
        return try {
            val password = cipher.decrypt(
                studentId,
                CredentialField.PASSWORD,
                EncryptedValue(
                    student.passwordCiphertext,
                    student.passwordIv,
                    student.passwordEncryptionVersion,
                ),
            )
            DecryptedCredentials(username, password)
        } catch (failure: Throwable) {
            username.fill('\u0000')
            throw failure
        }
    }

    fun transitionSession(
        state: KioskState,
        currentStudentId: String? = null,
        automationStep: String? = null,
        lockedReason: String? = null,
    ) {
        val current = requireNotNull(database.sessionDao().get()) { "No session state" }
        database.sessionDao().save(
            current.copy(
                state = state.name,
                currentStudentId = currentStudentId,
                automationStep = automationStep,
                lockedReason = lockedReason,
                previousCheckpoint = current.state,
                updatedAtEpochMs = nowEpochMs(),
            ),
        )
    }

    fun endSession() {
        database.runInTransaction {
            val current = requireNotNull(database.sessionDao().get()) { "No session state" }
            val sessionId = requireNotNull(current.sessionId) { "진행 중인 수업이 없습니다." }
            database.sessionDao().clearTemporaryStudents(sessionId)
            database.sessionDao().save(
                ActiveSessionEntity(
                    state = KioskState.ADMIN_IDLE.name,
                    updatedAtEpochMs = nowEpochMs(),
                    sessionId = null,
                    classId = null,
                    startedAtEpochMs = null,
                    currentStudentId = null,
                    automationStep = null,
                    lockedReason = null,
                    previousCheckpoint = current.state,
                ),
            )
            audit("SESSION_ENDED", null, null, sessionId)
        }
    }

    private fun audit(
        eventType: String,
        reasonCode: String?,
        studentId: String?,
        sessionId: String?,
    ) {
        database.auditDao().insert(
            AuditEventEntity(
                eventType = eventType,
                reasonCode = reasonCode,
                subjectStudentId = studentId,
                sessionId = sessionId,
                appVersion = appVersion,
                createdAtEpochMs = nowEpochMs(),
            ),
        )
    }
}
