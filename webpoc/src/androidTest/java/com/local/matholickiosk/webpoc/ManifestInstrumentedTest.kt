package com.local.matholickiosk.webpoc

import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestInstrumentedTest {
    @Test
    fun mainActivityAllowsBothLandscapeOrientationsOnly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val activity = context.packageManager.getActivityInfo(
            ComponentName(context, MainActivity::class.java),
            0,
        )

        assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, activity.screenOrientation)
    }

    @Test
    fun mainActivityDoesNotResizeFinishButtonAboveTheKeyboard() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val activity = context.packageManager.getActivityInfo(
            ComponentName(context, MainActivity::class.java),
            0,
        )

        assertEquals(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING,
            activity.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST,
        )
    }

    @Test
    fun remoteSupportReceiversKeepAdbAndKioskTrustBoundariesSeparate() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val adbReceiver = context.packageManager.getReceiverInfo(
            ComponentName(context, AdbRemoteSupportReceiver::class.java),
            PackageManager.ComponentInfoFlags.of(0),
        )
        val kioskReceiver = context.packageManager.getReceiverInfo(
            ComponentName(context, KioskRemoteSupportReceiver::class.java),
            PackageManager.ComponentInfoFlags.of(0),
        )

        assertTrue(adbReceiver.exported)
        assertEquals("android.permission.DUMP", adbReceiver.permission)
        assertTrue(kioskReceiver.exported)
        assertEquals(
            "com.local.matholickiosk.permission.REMOTE_SUPPORT_CONTROL",
            kioskReceiver.permission,
        )
    }
}
