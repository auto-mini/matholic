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
    version = 5,
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

        @Volatile
        private var instance: KioskDatabase? = null

        fun get(context: Context): KioskDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KioskDatabase::class.java,
                    DATABASE_NAME,
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                    )
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
                enableSecureDelete(db)
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

        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `students` ADD COLUMN `reusableCardLabel` TEXT DEFAULT NULL",
                )
                db.execSQL(
                    "ALTER TABLE `students` ADD COLUMN `reusableCardAssigned` INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val SECURE_DELETE_CALLBACK = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                enableSecureDelete(db)
            }
        }

        private fun enableSecureDelete(db: SupportSQLiteDatabase) {
            db.query("PRAGMA secure_delete = ON").use { cursor ->
                check(cursor.moveToFirst() && cursor.getInt(0) == 1) {
                    "SQLite secure_delete could not be enabled"
                }
            }
        }
    }
}
