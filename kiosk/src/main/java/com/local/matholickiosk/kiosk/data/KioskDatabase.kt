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
    version = 2,
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
                    .addMigrations(MIGRATION_1_2)
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
    }
}
