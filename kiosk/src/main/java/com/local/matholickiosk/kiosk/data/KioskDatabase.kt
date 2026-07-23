package com.local.matholickiosk.kiosk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentEntity::class,
        ClassGroupEntity::class,
        ClassMembershipEntity::class,
        ActiveSessionEntity::class,
        SessionStudentEntity::class,
        AuditEventEntity::class,
        AdminCredentialEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class KioskDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
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
                ).build().also { instance = it }
            }
    }
}
