package com.local.matholickiosk.kiosk.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface StudentDao {
    @Insert
    fun insert(student: StudentEntity)

    @Update
    fun update(student: StudentEntity)

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    fun findById(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE qrTokenHash = :hash AND isActive = 1 LIMIT 1")
    fun findActiveByQrHash(hash: ByteArray): StudentEntity?

    @Query(
        """
        SELECT DISTINCT s.* FROM students s
        LEFT JOIN class_memberships cm
          ON cm.studentId = s.studentId AND cm.classId = :classId
        LEFT JOIN session_students ss
          ON ss.studentId = s.studentId AND ss.sessionId = :sessionId
        WHERE s.qrTokenHash = :hash
          AND s.isActive = 1
          AND (cm.classId IS NOT NULL OR ss.sessionId IS NOT NULL)
        LIMIT 1
        """,
    )
    fun findEligibleByQrHash(hash: ByteArray, classId: String, sessionId: String): StudentEntity?

    @Query(
        """
        SELECT DISTINCT s.* FROM students s
        LEFT JOIN class_memberships cm
          ON cm.studentId = s.studentId AND cm.classId = :classId
        LEFT JOIN session_students ss
          ON ss.studentId = s.studentId AND ss.sessionId = :sessionId
        WHERE s.studentId = :studentId
          AND s.isActive = 1
          AND (cm.classId IS NOT NULL OR ss.sessionId IS NOT NULL)
        LIMIT 1
        """,
    )
    fun findEligibleById(studentId: String, classId: String, sessionId: String): StudentEntity?

    @Query(
        """
        SELECT DISTINCT s.* FROM students s
        LEFT JOIN class_memberships cm
          ON cm.studentId = s.studentId AND cm.classId = :classId
        LEFT JOIN session_students ss
          ON ss.studentId = s.studentId AND ss.sessionId = :sessionId
        WHERE s.isActive = 1
          AND (cm.classId IS NOT NULL OR ss.sessionId IS NOT NULL)
        ORDER BY s.displayNameExact
        """,
    )
    fun listEligibleForSession(classId: String, sessionId: String): List<StudentEntity>

    @Query(
        """
        SELECT s.* FROM students s
        INNER JOIN class_memberships cm ON cm.studentId = s.studentId
        WHERE cm.classId = :classId AND s.isActive = 1
        ORDER BY s.displayNameExact
        """,
    )
    fun listActiveForClass(classId: String): List<StudentEntity>

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY displayNameExact")
    fun listAllActive(): List<StudentEntity>

    @Query(
        """
        UPDATE students
        SET usernameCiphertext = X'',
            usernameIv = X'',
            usernameEncryptionVersion = 0,
            passwordCiphertext = X'',
            passwordIv = X'',
            passwordEncryptionVersion = 0,
            qrTokenHash = :revokedReplacementHash,
            isActive = 0,
            updatedAtEpochMs = :updatedAtEpochMs
        WHERE studentId = :studentId AND isActive = 1
        """,
    )
    fun deactivateAndPurgeCredentials(
        studentId: String,
        revokedReplacementHash: ByteArray,
        updatedAtEpochMs: Long,
    ): Int
}

@Dao
interface QrCardStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(status: QrCardStatusEntity)

    @Query("SELECT * FROM qr_card_status WHERE studentId = :studentId LIMIT 1")
    fun find(studentId: String): QrCardStatusEntity?

    @Query(
        """
        SELECT q.* FROM qr_card_status q
        INNER JOIN students s ON s.studentId = q.studentId
        WHERE s.isActive = 1
        ORDER BY s.displayNameExact
        """,
    )
    fun listForActiveStudents(): List<QrCardStatusEntity>

    @Query(
        """
        UPDATE qr_card_status
        SET lastUsedAtEpochMs = :usedAtEpochMs
        WHERE studentId = :studentId
        """,
    )
    fun markUsed(studentId: String, usedAtEpochMs: Long): Int

    @Query(
        """
        UPDATE qr_card_status
        SET lastDeliveredAtEpochMs = :deliveredAtEpochMs, needsPrint = 0
        WHERE studentId IN (:studentIds)
        """,
    )
    fun markPdfSavedToPc(studentIds: Set<String>, deliveredAtEpochMs: Long): Int

    @Query(
        """
        UPDATE qr_card_status
        SET needsPrint = 1, lastDeliveredAtEpochMs = NULL
        WHERE studentId = :studentId
        """,
    )
    fun markCardPdfNeeded(studentId: String): Int
}

@Dao
interface ClassDao {
    @Upsert
    fun upsert(group: ClassGroupEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addMembership(membership: ClassMembershipEntity): Long

    @Query("DELETE FROM class_memberships WHERE classId = :classId")
    fun clearMemberships(classId: String)

    @Query("DELETE FROM class_memberships WHERE studentId = :studentId")
    fun clearStudentMemberships(studentId: String)

    @Query("SELECT studentId FROM class_memberships WHERE classId = :classId")
    fun listMembershipStudentIds(classId: String): List<String>

    @Query("DELETE FROM class_groups WHERE classId = :classId")
    fun deleteById(classId: String): Int

    @Query("SELECT * FROM class_groups WHERE isActive = 1 ORDER BY className")
    fun listActive(): List<ClassGroupEntity>

    @Query("SELECT * FROM class_groups WHERE classId = :classId AND isActive = 1 LIMIT 1")
    fun findActiveById(classId: String): ClassGroupEntity?

    @Query(
        """
        SELECT * FROM class_groups
        WHERE isActive = 1 AND className = :className COLLATE NOCASE
        LIMIT 1
        """,
    )
    fun findActiveByName(className: String): ClassGroupEntity?
}

@Dao
interface SessionDao {
    @Upsert
    fun save(session: ActiveSessionEntity)

    @Query("SELECT * FROM active_session WHERE singletonId = 1 LIMIT 1")
    fun get(): ActiveSessionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addTemporaryStudent(student: SessionStudentEntity): Long

    @Query("DELETE FROM session_students WHERE sessionId = :sessionId")
    fun clearTemporaryStudents(sessionId: String)
}

@Dao
interface AuditDao {
    @Insert
    fun insert(event: AuditEventEntity): Long

    @Query("SELECT * FROM audit_events ORDER BY createdAtEpochMs DESC LIMIT :limit")
    fun latest(limit: Int): List<AuditEventEntity>

    @Query("DELETE FROM audit_events WHERE createdAtEpochMs < :cutoffEpochMs")
    fun deleteOlderThan(cutoffEpochMs: Long): Int
}

@Dao
interface AdminDao {
    @Upsert
    fun save(credential: AdminCredentialEntity)

    @Query("SELECT * FROM admin_credential WHERE singletonId = 1 LIMIT 1")
    fun get(): AdminCredentialEntity?

    @Query("SELECT COUNT(*) FROM admin_credential WHERE singletonId = 1")
    fun enrolledCount(): Int

    @Query("SELECT pinLength FROM admin_credential WHERE singletonId = 1 LIMIT 1")
    fun enrolledPinLength(): Int?
}
