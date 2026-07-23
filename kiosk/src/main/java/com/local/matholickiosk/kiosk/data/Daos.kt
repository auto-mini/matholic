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
        SELECT s.* FROM students s
        INNER JOIN class_memberships cm ON cm.studentId = s.studentId
        WHERE cm.classId = :classId AND s.isActive = 1
        ORDER BY s.displayNameExact
        """,
    )
    fun listActiveForClass(classId: String): List<StudentEntity>

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY displayNameExact")
    fun listAllActive(): List<StudentEntity>
}

@Dao
interface ClassDao {
    @Upsert
    fun upsert(group: ClassGroupEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addMembership(membership: ClassMembershipEntity): Long

    @Query("SELECT * FROM class_groups WHERE isActive = 1 ORDER BY className")
    fun listActive(): List<ClassGroupEntity>

    @Query("SELECT * FROM class_groups WHERE classId = :classId AND isActive = 1 LIMIT 1")
    fun findActiveById(classId: String): ClassGroupEntity?
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
}
