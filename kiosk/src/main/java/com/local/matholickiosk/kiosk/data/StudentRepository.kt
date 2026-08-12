package com.local.matholickiosk.kiosk.data

import com.local.matholickiosk.kiosk.domain.KioskState
import com.local.matholickiosk.kiosk.qr.IssuedQrToken
import com.local.matholickiosk.kiosk.qr.QrTokenCodec
import com.local.matholickiosk.kiosk.security.CredentialCipher
import com.local.matholickiosk.kiosk.security.CredentialField
import com.local.matholickiosk.kiosk.security.EncryptedValue
import java.io.Closeable
import java.util.UUID

data class IssuedQrPayload(
    val payload: String,
)

data class RegisteredStudent(
    val studentId: String,
    val issuedQr: IssuedQrPayload,
)

data class ValidatedStudent(
    val studentId: String,
    val displayNameExact: String,
)

data class BatchIssuedQr(
    val studentId: String,
    val displayNameExact: String,
    val issuedQr: IssuedQrPayload,
)

data class ReusableCardSlotSummary(
    val studentId: String,
    val slotLabel: String,
    val displayNameExact: String,
    val isAssigned: Boolean,
    val needsCardPdf: Boolean,
)

data class AssignedReusableCard(
    val studentId: String,
    val slotLabel: String,
)

data class MovedReusableCard(
    val previousSlotStudentId: String,
    val studentId: String,
    val slotLabel: String,
    val displayNameExact: String,
    val issuedQr: IssuedQrPayload,
)

private data class PendingBatchIssuedQr(
    val result: BatchIssuedQr,
    val token: IssuedQrToken,
)

data class QrCardStatusSummary(
    val studentId: String,
    val displayNameExact: String,
    val issuedAtEpochMs: Long,
    val lastUsedAtEpochMs: Long?,
    val lastPdfSavedAtEpochMs: Long?,
    val needsCardPdf: Boolean,
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
    private var auditEventsSinceMaintenance = AUDIT_MAINTENANCE_INTERVAL

    fun maintainAuditRetention() {
        val now = nowEpochMs()
        database.auditDao().deleteOlderThan(now - AUDIT_RETENTION_MS)
        database.auditDao().deleteBeyondLatest(MAX_AUDIT_ROWS - 1)
        auditEventsSinceMaintenance = 0
    }

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

    fun listReusableCardSlots(): List<ReusableCardSlotSummary> {
        val statuses = database.qrCardStatusDao().listForReusableCardSlots()
            .associateBy(QrCardStatusEntity::studentId)
        return database.studentDao().listReusableCardSlots().map { student ->
            ReusableCardSlotSummary(
                studentId = student.studentId,
                slotLabel = requireNotNull(student.reusableCardLabel),
                displayNameExact = student.displayNameExact,
                isAssigned = student.reusableCardAssigned,
                needsCardPdf = statuses[student.studentId]?.needsPrint ?: true,
            )
        }
    }

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
            qrCodec.issue().use { issued ->
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
                return RegisteredStudent(studentId, IssuedQrPayload(issued.payload))
            }
        } finally {
            username.fill('\u0000')
            password.fill('\u0000')
        }
    }

    fun reissueQr(studentId: String): IssuedQrPayload {
        val student = requireNotNull(database.studentDao().findById(studentId)) { "Student not found" }
        require(student.isActive) { "Student is inactive" }
        require(student.reusableCardLabel == null) {
            "신규용 재사용 카드는 QR을 유지해야 합니다. 실제 QR 카드 전환을 사용하세요."
        }
        qrCodec.issue().use { issued ->
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
            return IssuedQrPayload(issued.payload)
        }
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
        require(students.none { it.reusableCardLabel != null }) {
            "신규용 재사용 카드가 포함된 반은 QR 전체 재발급을 할 수 없습니다. " +
                "재사용 카드의 QR은 유지해야 합니다."
        }
        val issued = mutableListOf<PendingBatchIssuedQr>()
        try {
            students.forEach { student ->
                val token = qrCodec.issue()
                issued += PendingBatchIssuedQr(
                    result = BatchIssuedQr(
                        studentId = student.studentId,
                        displayNameExact = student.displayNameExact,
                        issuedQr = IssuedQrPayload(token.payload),
                    ),
                    token = token,
                )
            }
            database.runInTransaction {
                val now = nowEpochMs()
                issued.forEach { item ->
                    val student = requireNotNull(
                        database.studentDao().findById(item.result.studentId),
                    ) {
                        "Student not found"
                    }
                    require(student.isActive) { "Student is inactive" }
                    database.studentDao().update(
                        student.copy(
                            qrTokenHash = item.token.hash,
                            updatedAtEpochMs = now,
                        ),
                    )
                    database.qrCardStatusDao().upsert(
                        QrCardStatusEntity(
                            studentId = item.result.studentId,
                            issuedAtEpochMs = now,
                            lastUsedAtEpochMs =
                                database.qrCardStatusDao().find(item.result.studentId)?.lastUsedAtEpochMs,
                            lastDeliveredAtEpochMs = null,
                            needsPrint = true,
                        ),
                    )
                }
                audit("CLASS_QR_BATCH_REISSUED", issued.size.toString(), null, null)
            }
            return issued.map(PendingBatchIssuedQr::result)
        } finally {
            issued.forEach { it.token.close() }
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
        require(students.none { it.reusableCardLabel != null }) {
            "신규용 재사용 카드는 선택 QR 재발급 대상에 포함할 수 없습니다. " +
                "재사용 카드의 QR은 유지해야 합니다."
        }
        val issued = mutableListOf<PendingBatchIssuedQr>()
        try {
            students.forEach { student ->
                val token = qrCodec.issue()
                issued += PendingBatchIssuedQr(
                    result = BatchIssuedQr(
                        studentId = student.studentId,
                        displayNameExact = student.displayNameExact,
                        issuedQr = IssuedQrPayload(token.payload),
                    ),
                    token = token,
                )
            }
            database.runInTransaction {
                val now = nowEpochMs()
                issued.forEach { item ->
                    val student = requireNotNull(database.studentDao().findById(item.result.studentId))
                    require(student.isActive) { "Student is inactive" }
                    database.studentDao().update(
                        student.copy(
                            qrTokenHash = item.token.hash,
                            updatedAtEpochMs = now,
                        ),
                    )
                    database.qrCardStatusDao().upsert(
                        QrCardStatusEntity(
                            studentId = item.result.studentId,
                            issuedAtEpochMs = now,
                            lastUsedAtEpochMs =
                                database.qrCardStatusDao().find(item.result.studentId)?.lastUsedAtEpochMs,
                            lastDeliveredAtEpochMs = null,
                            needsPrint = true,
                        ),
                    )
                }
                audit("SELECTED_QR_BATCH_REISSUED", issued.size.toString(), null, null)
            }
            return issued.map(PendingBatchIssuedQr::result)
        } finally {
            issued.forEach { it.token.close() }
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
                lastPdfSavedAtEpochMs = status.lastDeliveredAtEpochMs,
                needsCardPdf = status.needsPrint,
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
                        withIssuedHashOnly { qrTokenHash ->
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
                                    qrTokenHash = qrTokenHash,
                                    isActive = true,
                                    createdAtEpochMs = now,
                                    updatedAtEpochMs = now,
                                ),
                            )
                        }
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
                        if (
                            matched.displayNameExact != row.displayNameExact &&
                            matched.reusableCardLabel == null
                        ) {
                            check(database.qrCardStatusDao().markCardPdfNeeded(studentId) == 1)
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
            val needsCardPdf = listQrCardStatuses().count(QrCardStatusSummary::needsCardPdf)
            return StudentCsvImportResult(created, updated, needsCardPdf)
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
            var renamedRequiringCardPdf = 0
            rows.forEach { row ->
                val matched = existingUsernames.firstOrNull { (_, username) ->
                    username.contentEquals(row.username)
                }?.first
                if (matched == null) {
                    created += 1
                } else {
                    updated += 1
                    if (matched.displayNameExact != row.displayNameExact) {
                        renamed += 1
                        if (matched.reusableCardLabel == null) {
                            renamedRequiringCardPdf += 1
                        }
                    }
                }
            }
            val alreadyPending = listQrCardStatuses().count(QrCardStatusSummary::needsCardPdf)
            return StudentCsvImportPreview(
                created = created,
                updated = updated,
                renamed = renamed,
                cardsNeedingPdfAfterImport = alreadyPending + created + renamedRequiringCardPdf,
            )
        } finally {
            existingUsernames.forEach { (_, username) -> username.fill('\u0000') }
        }
    }

    fun markCardPdfsSavedToPc(studentIds: Set<String>) {
        require(studentIds.isNotEmpty()) { "PDF 저장 완료 학생이 필요합니다." }
        database.runInTransaction {
            val updated = database.qrCardStatusDao().markPdfSavedToPc(studentIds, nowEpochMs())
            require(updated == studentIds.size) { "일부 QR 카드 상태를 갱신하지 못했습니다." }
            audit("QR_CARD_PDFS_SAVED_TO_PC", studentIds.size.toString(), null, null)
        }
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
            if (student.reusableCardLabel == null) {
                check(database.qrCardStatusDao().markCardPdfNeeded(studentId) == 1) {
                    "QR card status not found"
                }
            }
            audit("STUDENT_PROFILE_UPDATED", null, studentId, null)
        }
    }

    fun replaceClassMemberships(classId: String, studentIds: Set<String>) {
        database.runInTransaction {
            require(database.sessionDao().get()?.sessionId == null) {
                "수업 중에는 반 학생 구성을 변경할 수 없습니다."
            }
            require(database.classDao().findActiveById(classId) != null) {
                "Active class not found"
            }
            val activeStudentIds = database.studentDao().listAllActive()
                .mapTo(mutableSetOf(), StudentEntity::studentId)
            require(studentIds.all(activeStudentIds::contains)) {
                "활성 상태인 등록 학생만 반에 소속할 수 있습니다."
            }
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
        val student = requireNotNull(database.studentDao().findById(studentId)) {
            "Student not found"
        }
        require(student.reusableCardLabel == null) {
            "신규용 카드는 학생 비활성화 대신 카드 회수·초기화를 사용하세요."
        }
        withIssuedHashOnly { revokedReplacementHash ->
            database.runInTransaction {
                check(
                    database.studentDao().deactivateAndPurgeCredentials(
                        studentId,
                        revokedReplacementHash,
                        nowEpochMs(),
                    ) == 1,
                ) {
                    "Student not found or inactive"
                }
                database.classDao().clearStudentMemberships(studentId)
                audit("QR_REVOKED", null, studentId, null)
                audit("STUDENT_DEACTIVATED", null, studentId, null)
            }
        }
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

    /** Initializes the reusable-card pool once without rotating an existing slot QR. */
    fun ensureReusableCardSlots(
        targetCount: Int = REUSABLE_CARD_TARGET_COUNT,
    ): List<BatchIssuedQr> = prepareReusableCardSlotsInternal(
        targetCount = targetCount,
        allowDuringActiveSession = true,
        reissuePendingSlots = false,
    )

    fun prepareReusableCardSlots(
        targetCount: Int = REUSABLE_CARD_TARGET_COUNT,
    ): List<BatchIssuedQr> = prepareReusableCardSlotsInternal(
        targetCount = targetCount,
        allowDuringActiveSession = false,
        reissuePendingSlots = true,
    )

    private fun prepareReusableCardSlotsInternal(
        targetCount: Int,
        allowDuringActiveSession: Boolean,
        reissuePendingSlots: Boolean,
    ): List<BatchIssuedQr> {
        require(targetCount in 1..MAX_REUSABLE_CARD_TARGET_COUNT) {
            "재사용 카드 목표 수는 1~${MAX_REUSABLE_CARD_TARGET_COUNT}장이어야 합니다."
        }
        val issued = mutableListOf<PendingBatchIssuedQr>()
        try {
            database.runInTransaction {
                if (!allowDuringActiveSession) {
                    require(database.sessionDao().get()?.sessionId == null) {
                        "수업 중에는 신규용 카드를 준비할 수 없습니다."
                    }
                }
                val allSlots = database.studentDao().listAllReusableCardSlots()
                val slots = allSlots.filter(StudentEntity::isActive)
                val available = slots.filterNot(StudentEntity::reusableCardAssigned)
                val statuses = database.qrCardStatusDao().listForReusableCardSlots()
                    .associateBy(QrCardStatusEntity::studentId)
                val pending = if (reissuePendingSlots) {
                    available.filter { statuses[it.studentId]?.needsPrint ?: true }
                } else {
                    emptyList()
                }
                val usedLabels = allSlots
                    .mapNotNull(StudentEntity::reusableCardLabel)
                    .toMutableSet()
                val now = nowEpochMs()

                fun nextSlotLabel(): String {
                    var index = 1
                    while (true) {
                        val label = "신규카드$index"
                        if (usedLabels.add(label)) return label
                        index += 1
                    }
                }

                pending.forEach { student ->
                    val token = qrCodec.issue()
                    val emptyCredentials = encryptedEmptyCredentials(student.studentId)
                    issued += PendingBatchIssuedQr(
                        result = BatchIssuedQr(
                            studentId = student.studentId,
                            displayNameExact = requireNotNull(student.reusableCardLabel),
                            issuedQr = IssuedQrPayload(token.payload),
                        ),
                        token = token,
                    )
                    database.studentDao().update(
                        student.copy(
                            displayNameExact = requireNotNull(student.reusableCardLabel),
                            displayNameMasked = requireNotNull(student.reusableCardLabel),
                            usernameCiphertext = emptyCredentials.first.ciphertext,
                            usernameIv = emptyCredentials.first.iv,
                            usernameEncryptionVersion = emptyCredentials.first.version,
                            passwordCiphertext = emptyCredentials.second.ciphertext,
                            passwordIv = emptyCredentials.second.iv,
                            passwordEncryptionVersion = emptyCredentials.second.version,
                            qrTokenHash = token.hash,
                            reusableCardAssigned = false,
                            updatedAtEpochMs = now,
                        ),
                    )
                    database.qrCardStatusDao().upsert(
                        QrCardStatusEntity(
                            studentId = student.studentId,
                            issuedAtEpochMs = now,
                            lastUsedAtEpochMs = statuses[student.studentId]?.lastUsedAtEpochMs,
                            lastDeliveredAtEpochMs = null,
                            needsPrint = true,
                        ),
                    )
                }

                if (allSlots.isEmpty()) repeat(targetCount) {
                    val studentId = UUID.randomUUID().toString()
                    val slotLabel = nextSlotLabel()
                    val token = qrCodec.issue()
                    issued += PendingBatchIssuedQr(
                        result = BatchIssuedQr(
                            studentId = studentId,
                            displayNameExact = slotLabel,
                            issuedQr = IssuedQrPayload(token.payload),
                        ),
                        token = token,
                    )
                    val emptyCredentials = encryptedEmptyCredentials(studentId)
                    database.studentDao().insert(
                        StudentEntity(
                            studentId = studentId,
                            displayNameExact = slotLabel,
                            displayNameMasked = slotLabel,
                            usernameCiphertext = emptyCredentials.first.ciphertext,
                            usernameIv = emptyCredentials.first.iv,
                            usernameEncryptionVersion = emptyCredentials.first.version,
                            passwordCiphertext = emptyCredentials.second.ciphertext,
                            passwordIv = emptyCredentials.second.iv,
                            passwordEncryptionVersion = emptyCredentials.second.version,
                            qrTokenHash = token.hash,
                            isActive = true,
                            createdAtEpochMs = now,
                            updatedAtEpochMs = now,
                            reusableCardLabel = slotLabel,
                            reusableCardAssigned = false,
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
                }
                if (issued.isNotEmpty()) {
                    audit(
                        "REUSABLE_QR_CARDS_PREPARED",
                        issued.size.toString(),
                        null,
                        null,
                    )
                }
            }
            return issued.map(PendingBatchIssuedQr::result)
        } finally {
            issued.forEach { it.token.close() }
        }
    }

    fun assignReusableCardSlot(
        slotStudentId: String,
        displayNameExact: String,
        username: CharArray,
        password: CharArray,
    ): AssignedReusableCard {
        try {
            val exact = displayNameExact.trim()
            require(exact.isNotEmpty()) { "Exact display name is required" }
            require(username.isNotEmpty() && password.isNotEmpty()) {
                "Credentials are required"
            }
            val usernameEncrypted = cipher.encrypt(
                slotStudentId,
                CredentialField.USERNAME,
                username,
            )
            val passwordEncrypted = cipher.encrypt(
                slotStudentId,
                CredentialField.PASSWORD,
                password,
            )
            var assignedSlotLabel = ""
            database.runInTransaction {
                val student = requireNotNull(database.studentDao().findById(slotStudentId)) {
                    "재사용 카드 슬롯을 찾지 못했습니다."
                }
                val slotLabel = requireNotNull(student.reusableCardLabel) {
                    "선택한 학생은 신규용 카드 슬롯이 아닙니다."
                }
                require(student.isActive && !student.reusableCardAssigned) {
                    "선택한 신규용 카드가 이미 사용 중입니다."
                }
                require(
                    database.qrCardStatusDao().find(slotStudentId)?.needsPrint == false,
                ) {
                    "먼저 신규용 QR 카드를 지정 PC에 저장하고 출력하세요."
                }
                val now = nowEpochMs()
                database.studentDao().update(
                    student.copy(
                        displayNameExact = exact,
                        displayNameMasked = exact,
                        usernameCiphertext = usernameEncrypted.ciphertext,
                        usernameIv = usernameEncrypted.iv,
                        usernameEncryptionVersion = usernameEncrypted.version,
                        passwordCiphertext = passwordEncrypted.ciphertext,
                        passwordIv = passwordEncrypted.iv,
                        passwordEncryptionVersion = passwordEncrypted.version,
                        reusableCardAssigned = true,
                        updatedAtEpochMs = now,
                    ),
                )
                assignedSlotLabel = slotLabel
                audit(
                    "REUSABLE_QR_CARD_ASSIGNED",
                    slotLabel,
                    slotStudentId,
                    null,
                )
            }
            return AssignedReusableCard(
                studentId = slotStudentId,
                slotLabel = assignedSlotLabel,
            )
        } finally {
            username.fill('\u0000')
            password.fill('\u0000')
        }
    }

    fun releaseReusableCardSlot(slotStudentId: String) {
        val student = requireNotNull(database.studentDao().findById(slotStudentId)) {
            "재사용 카드 슬롯을 찾지 못했습니다."
        }
        val slotLabel = requireNotNull(student.reusableCardLabel) {
            "선택한 학생은 신규용 카드 슬롯이 아닙니다."
        }
        require(student.isActive && student.reusableCardAssigned) {
            "선택한 신규용 카드가 사용 중이 아닙니다."
        }
        val emptyCredentials = encryptedEmptyCredentials(slotStudentId)
        database.runInTransaction {
            require(database.sessionDao().get()?.sessionId == null) {
                "수업을 종료한 뒤 신규용 카드를 회수·초기화하세요."
            }
            val current = requireNotNull(database.studentDao().findById(slotStudentId)) {
                "재사용 카드 슬롯을 찾지 못했습니다."
            }
            require(current.isActive && current.reusableCardAssigned) {
                "선택한 신규용 카드가 이미 초기화되었습니다."
            }
            database.classDao().clearStudentMemberships(slotStudentId)
            database.studentDao().update(
                current.copy(
                    displayNameExact = slotLabel,
                    displayNameMasked = slotLabel,
                    usernameCiphertext = emptyCredentials.first.ciphertext,
                    usernameIv = emptyCredentials.first.iv,
                    usernameEncryptionVersion = emptyCredentials.first.version,
                    passwordCiphertext = emptyCredentials.second.ciphertext,
                    passwordIv = emptyCredentials.second.iv,
                    passwordEncryptionVersion = emptyCredentials.second.version,
                    reusableCardAssigned = false,
                    updatedAtEpochMs = nowEpochMs(),
                ),
            )
            audit(
                "REUSABLE_QR_CARD_RELEASED",
                slotLabel,
                slotStudentId,
                null,
            )
        }
    }

    /**
     * Gives an assigned temporary student a normal QR card. The old printed QR
     * remains attached to its slot and is immediately reset to an empty dummy;
     * the new regular student receives a new QR and the existing class roster.
     */
    fun moveReusableCardToRegularStudent(slotStudentId: String): MovedReusableCard {
        require(database.sessionDao().get()?.sessionId == null) {
            "수업을 종료한 뒤 실제 QR 카드로 전환하세요."
        }
        val slot = requireNotNull(database.studentDao().findById(slotStudentId)) {
            "재사용 카드 슬롯을 찾지 못했습니다."
        }
        val slotLabel = requireNotNull(slot.reusableCardLabel) {
            "선택한 학생은 신규용 카드 슬롯이 아닙니다."
        }
        require(slot.isActive && slot.reusableCardAssigned) {
            "선택한 신규용 카드가 사용 중이 아닙니다."
        }
        val credentials = decryptCredentials(slotStudentId)
        try {
            require(credentials.username.isNotEmpty() && credentials.password.isNotEmpty()) {
                "먼저 신규 학생 정보를 더미 카드에 등록하세요."
            }
            qrCodec.issue().use { issued ->
                val newStudentId = UUID.randomUUID().toString()
                val usernameEncrypted = cipher.encrypt(
                    newStudentId,
                    CredentialField.USERNAME,
                    credentials.username,
                )
                val passwordEncrypted = cipher.encrypt(
                    newStudentId,
                    CredentialField.PASSWORD,
                    credentials.password,
                )
                val now = nowEpochMs()
                database.runInTransaction {
                    val current = requireNotNull(database.studentDao().findById(slotStudentId)) {
                        "재사용 카드 슬롯을 찾지 못했습니다."
                    }
                    require(current.isActive && current.reusableCardAssigned) {
                        "선택한 신규용 카드가 이미 초기화되었습니다."
                    }
                    val memberships = database.classDao().listMembershipClassIds(slotStudentId)
                    database.studentDao().insert(
                        StudentEntity(
                            studentId = newStudentId,
                            displayNameExact = current.displayNameExact,
                            displayNameMasked = current.displayNameMasked,
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
                    memberships.forEach { classId ->
                        database.classDao().addMembership(
                            ClassMembershipEntity(classId, newStudentId),
                        )
                    }
                    database.classDao().clearStudentMemberships(slotStudentId)
                    val emptyCredentials = encryptedEmptyCredentials(slotStudentId)
                    database.studentDao().update(
                        current.copy(
                            displayNameExact = slotLabel,
                            displayNameMasked = slotLabel,
                            usernameCiphertext = emptyCredentials.first.ciphertext,
                            usernameIv = emptyCredentials.first.iv,
                            usernameEncryptionVersion = emptyCredentials.first.version,
                            passwordCiphertext = emptyCredentials.second.ciphertext,
                            passwordIv = emptyCredentials.second.iv,
                            passwordEncryptionVersion = emptyCredentials.second.version,
                            reusableCardAssigned = false,
                            updatedAtEpochMs = now,
                        ),
                    )
                    val oldStatus = database.qrCardStatusDao().find(slotStudentId)
                    if (oldStatus != null) {
                        database.qrCardStatusDao().upsert(oldStatus.copy(needsPrint = false))
                    }
                    database.qrCardStatusDao().upsert(
                        QrCardStatusEntity(
                            studentId = newStudentId,
                            issuedAtEpochMs = now,
                            lastUsedAtEpochMs = null,
                            lastDeliveredAtEpochMs = null,
                            needsPrint = true,
                        ),
                    )
                    audit(
                        "REUSABLE_QR_CARD_MOVED_TO_REGULAR",
                        slotLabel,
                        newStudentId,
                        null,
                    )
                }
                return MovedReusableCard(
                    previousSlotStudentId = slotStudentId,
                    studentId = newStudentId,
                    slotLabel = slotLabel,
                    displayNameExact = slot.displayNameExact,
                    issuedQr = IssuedQrPayload(issued.payload),
                )
            }
        } finally {
            credentials.close()
        }
    }

    fun listTemporaryStudentCandidatesForActiveSession(
        expectedSessionId: String,
    ): List<ValidatedStudent> {
        val session = database.sessionDao().get()
        require(
            session?.sessionId == expectedSessionId &&
                session.classId != null &&
                session.state == KioskState.QR_READY.name
        ) {
            "보강 학생을 추가할 수 있는 수업 상태가 아닙니다."
        }
        val alreadyEligible = database.studentDao().listEligibleForSession(
            classId = session.classId,
            sessionId = expectedSessionId,
        ).mapTo(mutableSetOf(), StudentEntity::studentId)
        return database.studentDao().listAllActive()
            .filterNot { it.studentId in alreadyEligible }
            .map { ValidatedStudent(it.studentId, it.displayNameExact) }
    }

    fun switchSessionClass(
        expectedSessionId: String,
        targetClassId: String,
    ): ActiveSessionEntity = database.runInTransaction<ActiveSessionEntity> {
        val current = requireNotNull(database.sessionDao().get()) { "No active session" }
        val currentSessionId = requireNotNull(current.sessionId) { "진행 중인 수업이 없습니다." }
        require(currentSessionId == expectedSessionId) { "Session mismatch" }
        require(current.state == KioskState.QR_READY.name && current.currentStudentId == null) {
            "학생 채점이 끝난 QR 대기 상태에서만 반을 변경할 수 있습니다."
        }
        require(current.classId != targetClassId) { "이미 현재 수업으로 선택된 반입니다." }
        require(database.classDao().findActiveById(targetClassId) != null) {
            "Active class not found"
        }
        require(database.studentDao().listActiveForClass(targetClassId).isNotEmpty()) {
            "선택한 반에 활성 소속 학생이 없습니다. 먼저 관리자 화면에서 소속을 설정하세요."
        }

        val now = nowEpochMs()
        val replacement = ActiveSessionEntity(
            sessionId = UUID.randomUUID().toString(),
            classId = targetClassId,
            startedAtEpochMs = now,
            state = KioskState.QR_READY.name,
            currentStudentId = null,
            automationStep = null,
            lockedReason = null,
            previousCheckpoint = current.state,
            updatedAtEpochMs = now,
        )
        database.sessionDao().clearTemporaryStudents(currentSessionId)
        database.sessionDao().save(replacement)
        audit("SESSION_ENDED", "CLASS_SWITCH", null, currentSessionId)
        audit("SESSION_STARTED", "CLASS_SWITCH", null, replacement.sessionId)
        replacement
    }

    fun validateForActiveSession(
        tokenHash: ByteArray,
        requiredDisplayNameExact: String? = null,
        expectedSessionId: String? = null,
    ): ValidatedStudent? {
        val session = database.sessionDao().get()
        if (
            session?.sessionId == null ||
            (expectedSessionId != null && session.sessionId != expectedSessionId) ||
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

    fun validateManualStudentForActiveSession(
        studentId: String,
        expectedSessionId: String? = null,
    ): ValidatedStudent? {
        val session = database.sessionDao().get()
        if (
            session?.sessionId == null ||
            (expectedSessionId != null && session.sessionId != expectedSessionId) ||
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
        expectedSessionId: String? = null,
        currentStudentId: String? = null,
        automationStep: String? = null,
        lockedReason: String? = null,
    ) {
        database.runInTransaction {
            val current = requireNotNull(database.sessionDao().get()) { "No session state" }
            require(current.sessionId != null) { "No active session" }
            require(expectedSessionId == null || current.sessionId == expectedSessionId) {
                "Session identity changed before transition"
            }
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

    fun endSession(): ActiveSessionEntity =
        database.runInTransaction<ActiveSessionEntity> {
            val current = requireNotNull(database.sessionDao().get()) { "No session state" }
            val sessionId = requireNotNull(current.sessionId) { "진행 중인 수업이 없습니다." }
            database.sessionDao().clearTemporaryStudents(sessionId)
            val idleSession = ActiveSessionEntity(
                state = KioskState.ADMIN_IDLE.name,
                updatedAtEpochMs = nowEpochMs(),
                sessionId = null,
                classId = null,
                startedAtEpochMs = null,
                currentStudentId = null,
                automationStep = null,
                lockedReason = null,
                previousCheckpoint = current.state,
            )
            database.sessionDao().save(idleSession)
            audit("SESSION_ENDED", null, null, sessionId)
            idleSession
        }

    private fun encryptedEmptyCredentials(studentId: String): Pair<EncryptedValue, EncryptedValue> {
        val empty = CharArray(0)
        return try {
            cipher.encrypt(studentId, CredentialField.USERNAME, empty) to
                cipher.encrypt(studentId, CredentialField.PASSWORD, empty)
        } finally {
            empty.fill('\u0000')
        }
    }

    private fun audit(
        eventType: String,
        reasonCode: String?,
        studentId: String?,
        sessionId: String?,
    ) {
        val now = nowEpochMs()
        auditEventsSinceMaintenance += 1
        if (auditEventsSinceMaintenance >= AUDIT_MAINTENANCE_INTERVAL) {
            maintainAuditRetention()
        }
        database.auditDao().insert(
            AuditEventEntity(
                eventType = eventType,
                reasonCode = reasonCode,
                subjectStudentId = studentId,
                sessionId = sessionId,
                appVersion = appVersion,
                createdAtEpochMs = now,
            ),
        )
    }

    private inline fun <T> withIssuedHashOnly(block: (ByteArray) -> T): T {
        val hash = qrCodec.issueHashOnly()
        return try {
            block(hash)
        } finally {
            hash.fill(0)
        }
    }

    private companion object {
        const val REUSABLE_CARD_TARGET_COUNT = 4
        const val MAX_REUSABLE_CARD_TARGET_COUNT = 4
        const val AUDIT_MAINTENANCE_INTERVAL = 256
        const val MAX_AUDIT_ROWS = 10_000
        const val AUDIT_RETENTION_MS = 90L * 24 * 60 * 60 * 1000
    }
}
