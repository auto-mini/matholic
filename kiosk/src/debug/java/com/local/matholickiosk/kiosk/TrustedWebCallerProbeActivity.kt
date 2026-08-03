package com.local.matholickiosk.kiosk

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.local.matholickiosk.kiosk.bridge.CredentialBridgeContract

/**
 * Debug-only harness for verifying the real cross-app Activity caller identity.
 *
 * It accepts only fixed product actions, always uses a known-missing handle for secure sessions,
 * and writes a non-sensitive result for the emulator-only verification script.
 */
class TrustedWebCallerProbeActivity : ComponentActivity() {
    private val webResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val reason = result.data
            ?.getStringExtra(CredentialBridgeContract.EXTRA_FAILURE_REASON)
        writeResult(result.resultCode, reason)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return
        deleteFile(RESULT_FILE)

        val action = intent.getStringExtra(EXTRA_ACTION) ?: run {
            writeResult(Activity.RESULT_CANCELED, "PROBE_ACTION_MISSING")
            finish()
            return
        }
        if (action !in ALLOWED_ACTIONS) {
            writeResult(Activity.RESULT_CANCELED, "PROBE_ACTION_INVALID")
            finish()
            return
        }
        val webIntent = Intent(action)
            .setComponent(ComponentName(WEB_PACKAGE, WEB_ACTIVITY))
            .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        if (action == CredentialBridgeContract.ACTION_START_SECURE_SESSION) {
            webIntent.putExtra(
                CredentialBridgeContract.EXTRA_CREDENTIAL_HANDLE,
                MISSING_HANDLE,
            )
        }
        runCatching { webResultLauncher.launch(webIntent) }
            .onFailure {
                writeResult(
                    Activity.RESULT_CANCELED,
                    "PROBE_LAUNCH_${it.javaClass.simpleName.take(40)}",
                )
                finish()
            }
    }

    private fun writeResult(resultCode: Int, failureReason: String?) {
        openFileOutput(RESULT_FILE, MODE_PRIVATE).bufferedWriter().use { writer ->
            writer.appendLine(resultCode.toString())
            writer.appendLine(failureReason.orEmpty())
        }
    }

    companion object {
        private const val EXTRA_ACTION = "probe_action"
        private const val RESULT_FILE = "trusted-web-caller-result.txt"
        private const val WEB_PACKAGE = "com.local.matholickiosk.webpoc"
        private const val WEB_ACTIVITY = "com.local.matholickiosk.webpoc.MainActivity"
        private const val MISSING_HANDLE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        private val ALLOWED_ACTIONS = setOf(
            CredentialBridgeContract.ACTION_START_SECURE_SESSION,
            CredentialBridgeContract.ACTION_RECOVER_WEB_SESSION,
        )
    }
}
