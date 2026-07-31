package com.local.matholickiosk.webpoc

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings

internal class RemoteSupportStore(
    context: Context,
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
    private val bootCount: () -> Int = {
        Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.BOOT_COUNT,
            -1,
        )
    },
) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    fun enable(durationMillis: Long): Long {
        val expiresAt = RemoteSupportPolicy.expiresAt(nowEpochMillis(), durationMillis)
        preferences.edit()
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .putInt(KEY_BOOT_COUNT, bootCount())
            .commit()
        return expiresAt
    }

    fun disable() {
        preferences.edit()
            .remove(KEY_EXPIRES_AT)
            .remove(KEY_BOOT_COUNT)
            .commit()
    }

    fun activeUntilEpochMillis(): Long? {
        val expiresAt = preferences.getLong(KEY_EXPIRES_AT, 0L)
        val active = RemoteSupportPolicy.isActive(
            nowEpochMillis = nowEpochMillis(),
            currentBootCount = bootCount(),
            storedBootCount = preferences.getInt(KEY_BOOT_COUNT, -1),
            expiresAtEpochMillis = expiresAt,
        )
        if (!active && (expiresAt != 0L || preferences.contains(KEY_BOOT_COUNT))) {
            disable()
        }
        return expiresAt.takeIf { active }
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        internal const val KEY_EXPIRES_AT = "remote_support_expires_at"
        internal const val KEY_BOOT_COUNT = "remote_support_boot_count"
        private const val PREFERENCES_NAME = "remote_support"
    }
}
