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

data class BatchIssuedQr(
    val studentId: String,
    val displayNameExact: String,
    val issuedQr: IssuedQrToken,
)

data class QrCardStatusSummary(
    val studentId: String,
    val displayNameExact: String,
    val issuedAtEpochMs: Long,
    val lastUsedAtEpochMs: Long?,
    val lastDeliveredAtEpochMs: Long?,
    val needsPrint: Boolean,
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
    fun ensureClasses(classNames: List<String>) {
        val normalized = classNames.map(String::trim)
        require(normalized.all(String::isNotEmpty)) { "Class names are required" }
        require(normalized.distinct().size == normalized.size) {
            "Class names must be unique"
        }
        database.runInTransaction {
            normalized.forEach { className ->
                if (database.classDao().findActiveByName(className) == null) {
                    val now = nowEpochMs()
                    database.classDao().upsert(
                        ClassGroupEntity(
                            classId = UUID.randomUUID().toString(),
                            className = className,
                            isActive = true,
                            createdAtEpochMs = now,
                            updatedAtEpochMs = now,
                        ),
                    )
                    audit("FIXED_CLASS_CREATED", null, null, null)
                }
            }
        }
    }

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
                database.qrCardStatusDao().upsert(
                    QrCardStatusEntity(
                        studentId = studentId,
                        issuedAtEpochMs = now,
                        lastUsedAtEpochMs = null,
                        lastDeliveredAtEpochMs = null,
                        needsPrint = true,
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
            val now = nowEpochMs()
            database.studentDao().update(
                student.copy(qrTokenHash = issued.hash, updatedAtEpochMs = now),
            )
            database.qrCardStatusDao().upsert(
                QrCardStatusEntity(
                    studentId = studentId,
                    issuedAtEpochMs = now,
                    lastUsedAtEpochMs =
                        database.qrCardStatusDao().find(studentId)?.lastUsedAtEpochMs,
                    lastDeliveredAtEpochMs = null,
                    needsPrint = true,
                ),
            )
            audit("QR_REISSUED", null, studentId, null)
        }
        return issued
    }

    fun reissueClassQrBatch(classId: String): List<BatchIssuedQr> {
        require(database.sessionDao().get()?.sessionId == null) {
            "수업 중에는 반 QR을 일괄 재발급할 수 없습니다."
        }
        val group = requireNotNull(database.classDao().findActiveById(classId)) {
            "Active class not found"
        }
        val students = database.studentDao().listActiveForClass(group.classId)
        require(students.isNotEmpty()) { "선택한 반에 소속 학생이 없습니다." }
        val issued = students.map { student ->
            BatchIssuedQr(
                studentId = student.studentId,
                displayNameExact = student.displayNameExact,
                issuedQr = qrCodec.issue(),
            )
        }
        try {
            database.runInTransaction {
                val now = nowEpochMs()
                issued.forEach { item ->
                    val student = requireNotNull(
                        database.studentDao().findById(item.studentId),
                    ) {
                        "Student not found"
                    }
                    require(student.isActive) { "Student is inactive" }
                    database.studentDao().update(
                        student.copy(
                            qrTokenHash = item.issuedQr.hash,
                            updatedAtEpochMs = now,
                        ),
                    )
                    database.qrCardStatusDao().upsert(
                        QrCardStatusEntity(
                            studentId = item.studentId,
                            issuedAtEpochMs = now,
                            lastUsedAtEpochMs =
                                database.qrCardStatusDao().find(item.studentId)?.lastUsedAtEpochMs,
                            lastDeliveredAtEpochMs = null,
                            needsPrint = true,
                        ),
                    )
                }
                audit("CLASS_QR_BATCH_REISSUED", issued.size.toString(), null, null)
            }
            return issued
        } catch (failure: Throwable) {
            issued.forEach { it.issuedQr.hash.fill(0) }
            throw failure
        }
    }

    fun reissueQrBatch(studentIds: Set<String>): List<BatchIssuedQr> {
        require(database.sessionDao().get()?.sessionId == null) {
            "수업 중에는 QR을 일괄 재발급할 수 없습니다."
        }
        require(studentIds.isNotEmpty()) { "재발급할 학생을 선택하세요." }
        val students = database.studentDao().listAllActive()
            .filter { it.studentId in studentIds }
        require(students.size == studentIds.size) {
            "비활성화되었거나 존재하지 않는 학생이 포함되어 있습니다."
        }
        val issued = students.map { student ->
            BatchIssuedQr(
                studentId = student.studentId,
                displayNameExact = student.displayNameExact,
                issuedQr = qrCodec.issue(),
            )
        }
        try {
            database.runInTransaction {
                val now = nowEpochMs()
                issued.forEach { item ->
                    val student = requireNotNull(database.studentDao().findById(item.studentId))
                    require(student.isActive) { "Student is inactive" }
                    database.studentDao().update(
                        student.copy(
                            qrTokenHash = item.issuedQr.hash,
                            updatedAtEpochMs = now,
                        ),
                    )
                    database.qrCardStatusDao().upsert(
                        QrCardStatusEntity(
                            studentId = item.studentId,
                            issuedAtEpochMs = now,
                            lastUsedAtEpochMs =
                                database.qrCardStatusDao().find(item.studentId)?.lastUsedAtEpochMs,
                            lastDeliveredAtEpochMs = null,
                            needsPrint = true,
                        ),
                    )
                }
                audit("SELECTED_QR_BATCH_REISSUED", issued.size.toString(), null, null)
            }
            return issued
        } catch (failure: Throwable) {
            issued.forEach { it.issuedQr.hash.fill(0) }
            throw failure
        }
    }

    fun listQrCardStatuses(): List<QrCardStatusSummary> {
        val statuses = database.qrCardStatusDao().listForActiveStudents()
            .associateBy(QrCardStatusEntity::studentId)
        return database.studentDao().listAllActive().map { student ->
            val status = statuses[student.studentId] ?: QrCardStatusEntity(
                studentId = student.studentId,
                issuedAtEpochMs = student.updatedAtEpochMs,
                lastUsedAtEpochMs = null,
                lastDeliveredAtEpochMs = null,
                needsPrint = false,
            )
            QrCardStatusSummary(
                studentId = student.studentId,
                displayNameExact = student.displayNameExact,
                issuedAtEpochMs = status.issuedAtEpochMs,
                lastUsedAtEpochMs = status.lastUsedAtEpochMs,
                lastDeliveredAtEpochMs = status.lastDeliveredAtEpochMs,
                needsPrint = status.needsPrint,
            )
        }
    }

    fun importStudents(rows: List<StudentCsvRow>): StudentCsvImportResult {
        require(database.sessionDao().get()?.sessionId == null) {
            "수업 중에는 학생 CSV를 가져올 수 없습니다."
        }
        require(rows.isNotEmpty()) { "가져올 학생이 없습니다." }
        val classesByName = database.classDao().listActive()
            .associateBy(ClassGroupEntity::className)
        val unknownClasses = rows.flatMap(StudentCsvRow::classNames)
            .filterNot(classesByName::containsKey)
            .distinct()
        require(unknownClasses.isEmpty()) {
            "등록되지 않은 반이 있습니다: ${unknownClasses.joinToString(", ")}"
        }
        val existing = database.studentDao().listAllActive()
        val existingUsernames = existing.map { student ->
            student to cipher.decrypt(
                student.studentId,
                CredentialField.USERNAME,
                EncryptedValue(
                    student.usernameCiphertext,
                    student.usernameIv,
                    student.usernameEncryptionVersion,
                ),
            )
        }
        try {
            var created = 0
            var updated = 0
            database.runInTransaction {
                val now = nowEpochMs()
                rows.forEach { row ->
                    val matched = existingUsernames.firstOrNull { (_, username) ->
                        username.contentEquals(row.username)
                    }?.first
                    val studentId = matched?.studentId ?: UUID.randomUUID().toString()
                    val usernameEncrypted = cipher.encrypt(
                        studentId,
                        CredentialField.USERNAME,
                        row.username,
                    )
                    val passwordEncrypted = cipher.encrypt(
                        studentId,
                        CredentialField.PASSWORD,
                        row.password,
                    )
                    if (matched == null) {
                        database.studentDao().insert(
                            StudentEntity(
                                studentId = studentId,
                                displayNameExact = row.displayNameExact,
                                displayNameMasked = row.displayNameExact,
                                usernameCiphertext = usernameEncrypted.ciphertext,
                                usernameIv = usernameEncrypted.iv,
                                usernameEncryptionVersion = usernameEncrypted.version,
                                passwordCiphertext = passwordEncrypted.ciphertext,
                                passwordIv = passwordEncrypted.iv,
                                passwordEncryptionVersion = passwordEncrypted.version,
                                qrTokenHash = qrCodec.issueHashOnly(),
                                isActive = true,
                                createdAtEpochMs = now,
                                updatedAtEpochMs = now,
                            ),
                        )
                        database.qrCardStatusDao().upsert(
                            QrCardStatusEntity(
                                studentId = studentId,
                                issuedAtEpochMs = now,
                                lastUsedAtEpochMs = null,
                                lastDeliveredAtEpochMs = null,
                                needsPrint = true,
                            ),
                        )
                        created += 1
                    } else {
                        database.studentDao().update(
                            matched.copy(
                                displayNameExact = row.displayNameExact,
                                displayNameMasked = row.displayNameExact,
                                usernameCiphertext = usernameEncrypted.ciphertext,
                                usernameIv = usernameEncrypted.iv,
                                usernameEncryptionVersion = usernameEncrypted.version,
                                passwordCiphertext = passwordEncrypted.ciphertext,
                                passwordIv = passwordEncrypted.iv,
                                passwordEncryptionVersion = passwordEncrypted.version,
                                updatedAtEpochMs = now,
                            ),
                        )
                        if (matched.displayNameExact != row.displayNameExact) {
                            check(database.qrCardStatusDao().setNeedsPrint(studentId, true) == 1)
                        }
                        updated += 1
                    }
                    database.classDao().clearStudentMemberships(studentId)
                    row.classNames.forEach { className ->
                        database.classDao().addMembership(
                            ClassMembershipEntity(
                                classId = requireNotNull(classesByName[className]).classId,
                                studentId = studentId,
                            ),
                        )
                    }
                }
                audit(
                    "STUDENT_CSV_IMPORTED",
                    "C$created-U$updated",
                    null,
                    null,
                )
            }
            val needsPrint = listQrCardStatuses().count(QrCardStatusSummary::needsPrint)
            return StudentCsvImportResult(created, updated, needsPrint)
        } finally {
            existingUsernames.forEach { (_, username) -> username.fill('\u0000') }
            rows.forEach(StudentCsvRow::clearSensitiveData)
        }
    }

    fun previewStudentImport(rows: List<StudentCsvRow>): StudentCsvImportPreview {
        require(database.sessionDao().get()?.sessionId == null) {
            "수업 중에는 학생 CSV를 가져올 수 없습니다."
        }
        require(rows.isNotEmpty()) { "가져올 학생이 없습니다." }
        val classesByName = database.classDao().listActive()
            .associateBy(ClassGroupEntity::className)
        val unknownClasses = rows.flatMap(StudentCsvRow::classNames)
            .filterNot(classesByName::containsKey)
            .distinct()
        require(unknownClasses.isEmpty()) {
            "등록되지 않은 반이 있습니다: ${unknownClasses.joinToString(", ")}"
        }
        val existing = database.studentDao().listAllActive()
        val existingUsernames = existing.map { student ->
            student to cipher.decrypt(
                student.studentId,
                CredentialField.USERNAME,
                EncryptedValue(
                    student.usernameCiphertext,
                    student.usernameIv,
                    student.usernameEncryptionVersion,
                ),
            )
        }
        try {
            var created = 0
            var updated = 0
            var renamed = 0
            rows.forEach { row ->
                val matched = existingUsernames.firstOrNull { (_, username) ->
                    username.contentEquals(row.username)
                }?.first
                if (matched == null) {
                    created += 1
                } else {
                    updated += 1
                    if (matched.displayNameExact != row.displayNameExact) renamed += 1
                }
            }
            val alreadyPending = listQrCardStatuses().count(QrCardStatusSummary::needsPrint)
            return StudentCsvImportPreview(
                created = created,
                updated = updated,
                renamed = renamed,
                cardsNeedingPrintAfterImport = alreadyPending + created + renamed,
            )
        } finally {
            existingUsernames.forEach { (_, username) -> username.fill('\u0000') }
        }
    }

    fun markCardsDelivered(studentIds: Set<String>) {
        require(studentIds.isNotEmpty()) { "전달 완료 학생이 필요합니다." }
        database.runInTransaction {
            val updated = database.qrCardStatusDao().markDelivered(studentIds, nowEpochMs())
            require(updated == studentIds.size) { "일부 QR 카드 상태를 갱신하지 못했습니다." }
            audit("QR_CARDS_DELIVERED", studentIds.size.toString(), null, null)
        }
    }

    fun recordClassQrBatchPrintRequested(count: Int) {
        require(count > 0) { "Batch QR count must be positive" }
        audit("CLASS_QR_BATCH_PRINT_REQUESTED", count.toString(), null, null)
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
            check(database.qrCardStatusDao().setNeedsPrint(studentId, true) == 1) {
                "QR card status not found"
            }
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

    fun restoreClass(
        classId: String,
        className: String,
        studentIds: Set<String>,
    ) {
        require(database.sessionDao().get()?.sessionId == null) {
            "수업 중에는 삭제한 반을 복원할 수 없습니다."
        }
        require(database.classDao().findActiveByName(className) == null) {
            "같은 이름의 반이 이미 있어 복원할 수 없습니다."
        }
        val activeStudentIds = database.studentDao().listAllActive()
            .mapTo(mutableSetOf(), StudentEntity::studentId)
        require(studentIds.all(activeStudentIds::contains)) {
            "복원할 반에 비활성 학생이 포함되어 있습니다."
        }
        val now = nowEpochMs()
        database.runInTransaction {
            database.classDao().upsert(
                ClassGroupEntity(
                    classId = classId,
                    className = className,
                    isActive = true,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now,
                ),
            )
            studentIds.forEach { studentId ->
                database.classDao().addMembership(
                    ClassMembershipEntity(classId, studentId),
                )
            }
            audit("CLASS_DELETE_UNDONE", studentIds.size.toString(), null, null)
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

    fun addTemporaryStudents(sessionId: String, studentIds: Set<String>) {
        require(studentIds.isNotEmpty()) { "Temporary students are required" }
        database.runInTransaction {
            val session = requireNotNull(database.sessionDao().get()) { "No active session" }
            require(session.sessionId == sessionId) { "Session mismatch" }
            require(session.state == KioskState.QR_READY.name) {
                "Session is not ready for temporary students"
            }
            val activeStudentIds = database.studentDao().listAllActive()
                .mapTo(mutableSetOf(), StudentEntity::studentId)
            require(studentIds.all(activeStudentIds::contains)) {
                "Inactive or unknown temporary student selected"
            }
            val now = nowEpochMs()
            studentIds.forEach { studentId ->
                database.sessionDao().addTemporaryStudent(
                    SessionStudentEntity(sessionId, studentId, now),
                )
            }
            audit(
                "TEMPORARY_STUDENTS_ADDED",
                studentIds.size.toString(),
                null,
                sessionId,
            )
        }
    }

    fun validateForActiveSession(
        tokenHash: ByteArray,
        requiredDisplayNameExact: String? = null,
    ): ValidatedStudent? {
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
        if (
            requiredDisplayNameExact != null &&
            student.displayNameExact != requiredDisplayNameExact
        ) {
            audit(
                "QR_REJECTED",
                "REQUIRED_DISPLAY_NAME_MISMATCH",
                student.studentId,
                session.sessionId,
            )
            return null
        }
        audit("QR_ACCEPTED", null, student.studentId, session.sessionId)
        check(database.qrCardStatusDao().markUsed(student.studentId, nowEpochMs()) == 1) {
            "QR card status not found"
        }
        return ValidatedStudent(student.studentId, student.displayNameExact)
    }

    fun validateManualStudentForActiveSession(studentId: String): ValidatedStudent? {
        val session = database.sessionDao().get()
        if (
            session?.sessionId == null ||
            session.classId == null ||
            session.state != KioskState.QR_READY.name
        ) {
            audit("MANUAL_STUDENT_REJECTED", "SESSION_NOT_READY", null, session?.sessionId)
            return null
        }
        val student = database.studentDao().findEligibleById(
            studentId = studentId,
            classId = session.classId,
            sessionId = session.sessionId,
        )
        if (student == null) {
            audit("MANUAL_STUDENT_REJECTED", "OUTSIDE_CURRENT_CLASS", null, session.sessionId)
            return null
        }
        audit("MANUAL_STUDENT_ACCEPTED", null, student.studentId, session.sessionId)
        return ValidatedStudent(student.studentId, student.displayNameExact)
    }

    fun listEligibleStudentsForActiveSession(): List<ValidatedStudent> {
        val session = database.sessionDao().get()
        require(
            session?.sessionId != null &&
                session.classId != null &&
                session.state == KioskState.QR_READY.name
        ) {
            "수동 선택이 가능한 수업 상태가 아닙니다."
        }
        return database.studentDao().listEligibleForSession(
            classId = session.classId,
            sessionId = session.sessionId,
        ).map { ValidatedStudent(it.studentId, it.displayNameExact) }
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
        expectedState: KioskState,
        state: KioskState,
        currentStudentId: String? = null,
        automationStep: String? = null,
        lockedReason: String? = null,
    ) {
        database.runInTransaction {
            val current = requireNotNull(database.sessionDao().get()) { "No session state" }
            require(current.sessionId != null) { "No active session" }
            require(current.state == expectedState.name) {
                "Session state changed before transition"
            }
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
