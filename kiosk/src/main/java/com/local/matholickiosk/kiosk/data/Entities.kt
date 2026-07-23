package com.local.matholickiosk.kiosk.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "students",
    indices = [Index(value = ["qrTokenHash"], unique = true)],
)
data class StudentEntity(
    @androidx.room.PrimaryKey val studentId: String,
    val displayNameExact: String,
    val displayNameMasked: String,
    val usernameCiphertext: ByteArray,
    val usernameIv: ByteArray,
    val usernameEncryptionVersion: Int,
    val passwordCiphertext: ByteArray,
    val passwordIv: ByteArray,
    val passwordEncryptionVersion: Int,
    val qrTokenHash: ByteArray,
    val isActive: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

@Entity(tableName = "class_groups")
data class ClassGroupEntity(
    @androidx.room.PrimaryKey val classId: String,
    val className: String,
    val isActive: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

@Entity(
    tableName = "class_memberships",
    primaryKeys = ["classId", "studentId"],
    foreignKeys = [
        ForeignKey(
            entity = ClassGroupEntity::class,
            parentColumns = ["classId"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("studentId")],
)
data class ClassMembershipEntity(
    val classId: String,
    val studentId: String,
)

@Entity(tableName = "active_session")
data class ActiveSessionEntity(
    @androidx.room.PrimaryKey val singletonId: Int = SINGLETON_ID,
    val sessionId: String?,
    val classId: String?,
    val startedAtEpochMs: Long?,
    val state: String,
    val currentStudentId: String?,
    val automationStep: String?,
    val lockedReason: String?,
    val previousCheckpoint: String?,
    val updatedAtEpochMs: Long,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}

@Entity(
    tableName = "session_students",
    primaryKeys = ["sessionId", "studentId"],
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("studentId")],
)
data class SessionStudentEntity(
    val sessionId: String,
    val studentId: String,
    val addedAtEpochMs: Long,
)

@Entity(
    tableName = "audit_events",
    indices = [Index("createdAtEpochMs"), Index("eventType")],
)
data class AuditEventEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val auditId: Long = 0,
    val eventType: String,
    val reasonCode: String?,
    val subjectStudentId: String?,
    val sessionId: String?,
    val appVersion: String,
    val createdAtEpochMs: Long,
)

@Entity(tableName = "admin_credential")
data class AdminCredentialEntity(
    @androidx.room.PrimaryKey val singletonId: Int = SINGLETON_ID,
    val salt: ByteArray,
    val derivedKey: ByteArray,
    val iterations: Int,
    val verifierVersion: Int,
    val consecutiveFailures: Int,
    val lockedUntilEpochMs: Long,
    val updatedAtEpochMs: Long,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
