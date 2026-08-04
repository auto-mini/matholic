package com.local.matholickiosk.kiosk

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.local.matholickiosk.kiosk.admin.KioskDeviceAdminReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Gate5ManifestInstrumentedTest {
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

    @Test
    fun kioskNetworkPermissionsRemainReadOnlyAndPairedOutboundOnly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        )
        val permissions = packageInfo.requestedPermissions.orEmpty().toSet()

        assertTrue("android.permission.INTERNET" in permissions)
        assertTrue("android.permission.ACCESS_NETWORK_STATE" in permissions)
    }

    @Test
    fun remoteSupportReceiverIsAdbOnlyAndControlIsVisibleInsideAdminPanel() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val receiver = context.packageManager.getReceiverInfo(
            ComponentName(context, AdbRemoteSupportReceiver::class.java),
            0,
        )
        assertTrue(receiver.exported)
        assertEquals("android.permission.DUMP", receiver.permission)

        var reachable = false
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val root = LayoutInflater.from(context).inflate(R.layout.activity_main, null, false)
            reachable = root.findViewById<View>(R.id.remote_support_button)
                .hasScrollViewAncestor()
        }
        assertTrue(reachable)
    }

    @Test
    fun remoteSupportBadgeUsesReservedHeaderSpaceWithoutCoveringStatus() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var badgeInsideHeader = false
        var statusEndsBeforeBadge = false
        var badgeInsideBounds = false
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val root = LayoutInflater.from(context).inflate(R.layout.activity_main, null, false)
            val header = root.findViewById<ViewGroup>(R.id.app_header)
            val status = root.findViewById<TextView>(R.id.status_text).apply {
                text = "CAMERA_PERMISSION_REQUIRED_AND_INITIALIZATION_FAILED"
            }
            val badge = root.findViewById<TextView>(R.id.remote_support_badge).apply {
                visibility = View.VISIBLE
            }
            root.measure(
                View.MeasureSpec.makeMeasureSpec(2_000, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1_200, View.MeasureSpec.EXACTLY),
            )
            root.layout(0, 0, root.measuredWidth, root.measuredHeight)

            badgeInsideHeader = badge.parent === header
            statusEndsBeforeBadge = status.right <= badge.left
            badgeInsideBounds = badge.right <= header.width && badge.left >= 0
        }

        assertTrue(badgeInsideHeader)
        assertTrue(statusEndsBeforeBadge)
        assertTrue(badgeInsideBounds)
    }

    @Test
    fun remoteSupportBadgeRemainsVisibleWhenScannerHidesHeader() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var scannerBadgeExists = false
        var headerBadgeHidden = false
        var scannerBadgeVisible = false
        var scannerBadgeInsideBounds = false
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val root = LayoutInflater.from(context).inflate(R.layout.activity_main, null, false)
            val header = root.findViewById<View>(R.id.app_header).apply {
                visibility = View.GONE
            }
            root.findViewById<View>(R.id.scanner_panel).visibility = View.VISIBLE
            root.findViewById<View>(R.id.remote_support_badge).visibility = View.VISIBLE
            val scannerBadgeId = context.resources.getIdentifier(
                "scanner_remote_support_badge",
                "id",
                context.packageName,
            )
            scannerBadgeExists = scannerBadgeId != 0
            if (scannerBadgeExists) {
                val scannerBadge = root.findViewById<TextView>(scannerBadgeId).apply {
                    visibility = View.VISIBLE
                }
                root.measure(
                    View.MeasureSpec.makeMeasureSpec(2_000, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1_200, View.MeasureSpec.EXACTLY),
                )
                root.layout(0, 0, root.measuredWidth, root.measuredHeight)
                headerBadgeHidden = !header.isVisibleThroughAncestors()
                scannerBadgeVisible = scannerBadge.isVisibleThroughAncestors()
                scannerBadgeInsideBounds =
                    scannerBadge.left >= 0 &&
                        scannerBadge.top >= 0 &&
                        scannerBadge.right <= root.width &&
                        scannerBadge.bottom <= root.height
            }
        }

        assertTrue(scannerBadgeExists)
        assertTrue(headerBadgeHidden)
        assertTrue(scannerBadgeVisible)
        assertTrue(scannerBadgeInsideBounds)
    }

    @Test
    fun remoteSupportControllerUpdatesHeaderAndScannerBadgesTogether() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = RemoteSupportStore(context)
        store.enable(60_000L)
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    assertEquals(
                        View.VISIBLE,
                        activity.findViewById<View>(R.id.remote_support_badge).visibility,
                    )
                    assertEquals(
                        View.VISIBLE,
                        activity.findViewById<View>(R.id.scanner_remote_support_badge).visibility,
                    )
                }
                store.disable()
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                scenario.onActivity { activity ->
                    assertEquals(
                        View.GONE,
                        activity.findViewById<View>(R.id.remote_support_badge).visibility,
                    )
                    assertEquals(
                        View.GONE,
                        activity.findViewById<View>(R.id.scanner_remote_support_badge).visibility,
                    )
                }
            }
        } finally {
            store.disable()
        }
    }

    @Test
    fun pcTransferControlsRemainReachableInsideScrollableCardPanel() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var pairReachable = false
        var sendReachable = false
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val root = LayoutInflater.from(context).inflate(R.layout.activity_main, null, false)
            pairReachable = root.findViewById<View>(R.id.pair_pc_button)
                .hasScrollViewAncestor()
            sendReachable = root.findViewById<View>(R.id.send_pc_pdf_button)
                .hasScrollViewAncestor()
        }

        assertTrue(pairReachable)
        assertTrue(sendReachable)
    }

    private fun View.hasScrollViewAncestor(): Boolean {
        var ancestor = parent
        while (ancestor is View) {
            if (ancestor is ScrollView) return true
            ancestor = ancestor.parent
        }
        return false
    }

    private fun View.isVisibleThroughAncestors(): Boolean {
        var current: View? = this
        while (current != null) {
            if (current.visibility != View.VISIBLE) return false
            current = current.parent as? View
        }
        return true
    }
}
