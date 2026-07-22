package com.local.matholickiosk.probe

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {
    private lateinit var reportStore: ReportStore
    private lateinit var statusText: TextView
    private lateinit var reportText: TextView
    private var pendingExport: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_main)
        reportStore = ReportStore(this)
        statusText = findViewById(R.id.statusText)
        reportText = findViewById(R.id.reportText)

        findViewById<Button>(R.id.openAccessibilitySettingsButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.openMatholicButton).setOnClickListener {
            openMatholic()
        }
        findViewById<Button>(R.id.exportButton).setOnClickListener {
            exportLatestReport()
        }
        findViewById<Button>(R.id.deleteReportsButton).setOnClickListener {
            confirmDeleteReports()
        }
    }

    override fun onResume() {
        super.onResume()
        ProbeAccessibilityService.hideOverlayIfConnected()
        refreshStatus()
    }

    @Deprecated("Activity result API is used to avoid an AndroidX runtime dependency in the Probe.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != EXPORT_REQUEST_CODE || resultCode != RESULT_OK) return
        val destination = data?.data ?: return
        val source = pendingExport ?: return
        try {
            reportStore.export(source, destination)
            Toast.makeText(this, R.string.report_exported, Toast.LENGTH_SHORT).show()
        } catch (error: Exception) {
            Toast.makeText(
                this,
                getString(R.string.export_failed, error.javaClass.simpleName),
                Toast.LENGTH_LONG,
            ).show()
        } finally {
            pendingExport = null
        }
    }

    private fun openMatholic() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, R.string.enable_service_first, Toast.LENGTH_LONG).show()
            return
        }
        val launchIntent = packageManager.getLaunchIntentForPackage(ProbeConstants.TARGET_PACKAGE)
        if (launchIntent == null) {
            Toast.makeText(this, R.string.target_not_found, Toast.LENGTH_LONG).show()
            return
        }
        ProbeAccessibilityService.showOverlayIfConnected()
        startActivity(launchIntent)
    }

    private fun exportLatestReport() {
        val latest = reportStore.latest()
        if (latest == null) {
            Toast.makeText(this, R.string.no_report_to_export, Toast.LENGTH_SHORT).show()
            return
        }
        pendingExport = latest
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, latest.name)
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, EXPORT_REQUEST_CODE)
    }

    private fun confirmDeleteReports() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_reports_title)
            .setMessage(R.string.delete_reports_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                val count = reportStore.deleteAll()
                Toast.makeText(this, getString(R.string.reports_deleted, count), Toast.LENGTH_SHORT).show()
                refreshStatus()
            }
            .show()
    }

    private fun refreshStatus() {
        val installed = try {
            packageManager.getPackageInfo(ProbeConstants.TARGET_PACKAGE, 0)
        } catch (_: Exception) {
            null
        }
        val versionOk = installed?.versionName == ProbeConstants.EXPECTED_VERSION_NAME &&
            installed.longVersionCode == ProbeConstants.EXPECTED_VERSION_CODE
        val enabled = isAccessibilityServiceEnabled()
        val connected = ProbeAccessibilityService.isConnected()
        statusText.text = getString(
            R.string.status_summary,
            ProbeConstants.TARGET_PACKAGE,
            installed?.versionName ?: getString(R.string.not_installed),
            installed?.longVersionCode ?: -1L,
            getString(if (versionOk) R.string.yes else R.string.no),
            getString(if (enabled) R.string.enabled else R.string.disabled),
            getString(if (connected) R.string.connected else R.string.waiting_connection),
        )
        val reports = reportStore.list()
        reportText.text = getString(
            R.string.report_status,
            reports.size,
            reports.firstOrNull()?.name ?: getString(R.string.none),
        )
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = ComponentName(this, ProbeAccessibilityService::class.java).flattenToString()
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    companion object {
        private const val EXPORT_REQUEST_CODE = 2001
    }
}
