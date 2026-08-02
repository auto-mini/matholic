package com.local.matholickiosk.kiosk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        StudentEntity::class,
        QrCardStatusEntity::class,
        ClassGroupEntity::class,
        ClassMembershipEntity::class,
        ActiveSessionEntity::class,
        SessionStudentEntity::class,
        AuditEventEntity::class,
        AdminCredentialEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class KioskDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun qrCardStatusDao(): QrCardStatusDao
    abstract fun classDao(): ClassDao
    abstract fun sessionDao(): SessionDao
    abstract fun auditDao(): AuditDao
    abstract fun adminDao(): AdminDao

    companion object {
        private const val DATABASE_NAME = "matholic-kiosk.db"
        private const val AUDIT_RETENTION_MS = 90L * 24 * 60 * 60 * 1000
        private const val MAX_AUDIT_ROWS = 10_000

        @Volatile
        private var instance: KioskDatabase? = null

        fun get(context: Context): KioskDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KioskDatabase::class.java,
                    DATABASE_NAME,
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(SECURE_DELETE_CALLBACK)
                    .build()
                    .also { instance = it }
            }

        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `qr_card_status` (
                        `studentId` TEXT NOT NULL,
                        `issuedAtEpochMs` INTEGER NOT NULL,
                        `lastUsedAtEpochMs` INTEGER,
                        `lastDeliveredAtEpochMs` INTEGER,
                        `needsPrint` INTEGER NOT NULL,
                        PRIMARY KEY(`studentId`),
                        FOREIGN KEY(`studentId`) REFERENCES `students`(`studentId`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `qr_card_status` (
                        `studentId`,
                        `issuedAtEpochMs`,
                        `lastUsedAtEpochMs`,
                        `lastDeliveredAtEpochMs`,
                        `needsPrint`
                    )
                    SELECT
                        `studentId`,
                        `updatedAtEpochMs`,
                        NULL,
                        `updatedAtEpochMs`,
                        0
                    FROM `students`
                    """.trimIndent(),
                )
            }
        }

        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `admin_credential` " +
                        "ADD COLUMN `pinLength` INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        internal val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA secure_delete = ON")
                db.execSQL(
                    """
                    UPDATE `students`
                    SET `usernameCiphertext` = X'',
                        `usernameIv` = X'',
                        `usernameEncryptionVersion` = 0,
                        `passwordCiphertext` = X'',
                        `passwordIv` = X'',
                        `passwordEncryptionVersion` = 0
                    WHERE `isActive` = 0
                    """.trimIndent(),
                )
            }
        }

        private val SECURE_DELETE_CALLBACK = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA secure_delete = ON")
                db.execSQL(
                    "DELETE FROM audit_events WHERE createdAtEpochMs < ?",
                    arrayOf(System.currentTimeMillis() - AUDIT_RETENTION_MS),
                )
                db.execSQL(
                    """
                    DELETE FROM audit_events
                    WHERE auditId NOT IN (
                        SELECT auditId FROM audit_events
                        ORDER BY createdAtEpochMs DESC, auditId DESC
                        LIMIT $MAX_AUDIT_ROWS
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
