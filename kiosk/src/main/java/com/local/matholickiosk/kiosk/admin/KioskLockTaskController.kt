package com.local.matholickiosk.kiosk.admin

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager
import com.local.matholickiosk.kiosk.MainActivity
import com.local.matholickiosk.kiosk.bridge.CredentialBridgeContract
import com.local.matholickiosk.kiosk.domain.DedicatedDeviceMode
import com.local.matholickiosk.kiosk.domain.DedicatedDevicePolicy
import com.local.matholickiosk.kiosk.domain.DedicatedDeviceStatus

class KioskLockTaskController(
    private val activity: Activity,
) {
    private val devicePolicyManager =
        activity.getSystemService(DevicePolicyManager::class.java)
    private val activityManager =
        activity.getSystemService(ActivityManager::class.java)
    private val admin = ComponentName(activity, KioskDeviceAdminReceiver::class.java)

    fun configureIfDeviceOwner(): Result<Boolean> = runCatching {
        if (!isDeviceOwner()) return@runCatching false
        devicePolicyManager.setLockTaskPackages(
            admin,
            DedicatedDevicePolicy.allowlistedPackages(
                kioskPackage = activity.packageName,
                webPocPackage = CredentialBridgeContract.TRUSTED_CONSUMER_PACKAGE,
            ),
        )
        devicePolicyManager.setLockTaskFeatures(
            admin,
            DevicePolicyManager.LOCK_TASK_FEATURE_NONE,
        )
        check(devicePolicyManager.setKeyguardDisabled(admin, true)) {
            "Device keyguard could not be disabled"
        }
        devicePolicyManager.addPersistentPreferredActivity(
            admin,
            IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            },
            ComponentName(activity, MainActivity::class.java),
        )
        true
    }

    fun enterRestrictedMode(): Result<Boolean> = runCatching {
        if (!isDeviceOwner()) return@runCatching false
        configureIfDeviceOwner().getOrThrow()
        if (!devicePolicyManager.isLockTaskPermitted(activity.packageName)) {
            return@runCatching false
        }
        if (currentMode() == DedicatedDeviceMode.LOCKED) return@runCatching true
        devicePolicyManager.addUserRestriction(
            admin,
            UserManager.DISALLOW_CREATE_WINDOWS,
        )
        try {
            activity.startLockTask()
        } catch (failure: Throwable) {
            devicePolicyManager.clearUserRestriction(
                admin,
                UserManager.DISALLOW_CREATE_WINDOWS,
            )
            throw failure
        }
        currentMode() == DedicatedDeviceMode.LOCKED
    }

    fun exitForAdministrator(): Result<Boolean> = runCatching {
        if (!isDeviceOwner()) return@runCatching false
        if (currentMode() != DedicatedDeviceMode.NONE) {
            activity.stopLockTask()
        }
        devicePolicyManager.clearUserRestriction(
            admin,
            UserManager.DISALLOW_CREATE_WINDOWS,
        )
        currentMode() == DedicatedDeviceMode.NONE
    }

    fun status(): DedicatedDeviceStatus {
        val owner = isDeviceOwner()
        return DedicatedDeviceStatus(
            isDeviceOwner = owner,
            isKioskPackagePermitted = owner &&
                devicePolicyManager.isLockTaskPermitted(activity.packageName),
            mode = currentMode(),
        )
    }

    private fun isDeviceOwner(): Boolean =
        devicePolicyManager.isDeviceOwnerApp(activity.packageName)

    private fun currentMode(): DedicatedDeviceMode =
        when (activityManager.lockTaskModeState) {
            ActivityManager.LOCK_TASK_MODE_LOCKED -> DedicatedDeviceMode.LOCKED
            ActivityManager.LOCK_TASK_MODE_PINNED -> DedicatedDeviceMode.PINNED
            else -> DedicatedDeviceMode.NONE
        }
}
