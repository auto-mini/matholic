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

    fun currentSession(): ActiveSessionEntity? = database.sessionDao().get()

    fun registerStudent(
        classId: String,
        displayNameExact: String,
        displayNameMasked: String,
        username: CharArray,
        password: CharArray,
    ): RegisteredStudent {
        try {
            require(database.classDao().findActiveById(classId) != null) {
                "Active class not found"
            }
            val exact = displayNameExact.trim()
            val masked = displayNameMasked.trim()
            require(exact.isNotEmpty()) { "Exact display name is required" }
            require(masked.isNotEmpty()) { "Masked display name is required" }
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
                        displayNameMasked = masked,
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
                database.classDao().addMembership(ClassMembershipEntity(classId, studentId))
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
        displayNameMasked: String,
    ) {
        val student = requireNotNull(database.studentDao().findById(studentId)) { "Student not found" }
        require(student.isActive) { "Student is inactive" }
        val exact = displayNameExact.trim()
        val masked = displayNameMasked.trim()
        require(exact.isNotEmpty()) { "Exact display name is required" }
        require(masked.isNotEmpty()) { "Masked display name is required" }
        database.runInTransaction {
            database.studentDao().update(
                student.copy(
                    displayNameExact = exact,
                    displayNameMasked = masked,
                    updatedAtEpochMs = nowEpochMs(),
                ),
            )
            audit("STUDENT_PROFILE_UPDATED", null, studentId, null)
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

    fun startSession(classId: String): ActiveSessionEntity {
        require(database.classDao().findActiveById(classId) != null) { "Active class not found" }
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
            database.sessionDao().save(session)
            audit("SESSION_STARTED", null, null, session.sessionId)
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
        val current = requireNotNull(database.sessionDao().get()) { "No session state" }
        val sessionId = current.sessionId
        database.runInTransaction {
            if (sessionId != null) database.sessionDao().clearTemporaryStudents(sessionId)
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
