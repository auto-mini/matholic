package com.local.matholickiosk.kiosk

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.admin.KioskDeviceAdminReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Gate5ManifestInstrumentedTest {
    @Test
    fun deviceAdminReceiverAndDedicatedHomeAreDeclared() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageManager = context.packageManager
        val receiver = packageManager.getReceiverInfo(
            ComponentName(context, KioskDeviceAdminReceiver::class.java),
            PackageManager.GET_META_DATA,
        )

        assertEquals(
            "android.permission.BIND_DEVICE_ADMIN",
            receiver.permission,
        )
        assertTrue(receiver.exported)
        assertTrue(
            receiver.metaData.getInt(DeviceAdminReceiver.DEVICE_ADMIN_META_DATA) != 0,
        )

        val homeActivities = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setPackage(context.packageName),
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        assertTrue(
            homeActivities.any {
                it.activityInfo.name == MainActivity::class.java.name &&
                    it.activityInfo.exported
            },
        )
    }
}
