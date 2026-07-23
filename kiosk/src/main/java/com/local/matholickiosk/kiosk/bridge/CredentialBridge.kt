package com.local.matholickiosk.kiosk.bridge

import android.net.Uri
import android.os.SystemClock
import java.io.Closeable
import java.security.SecureRandom
import java.util.Base64

class CredentialBridgeHandle internal constructor(
    val id: String,
    val uri: Uri,
)

class BridgePayload internal constructor(
    val expectedDisplayName: String,
    val username: CharArray,
    val password: CharArray,
) : Closeable {
    override fun close() {
        username.fill('\u0000')
        password.fill('\u0000')
    }
}

object CredentialBridgeContract {
    const val AUTHORITY = "com.local.matholickiosk.kiosk.credentials"
    const val PERMISSION = "com.local.matholickiosk.permission.CREDENTIAL_BRIDGE"
    const val TRUSTED_CONSUMER_PACKAGE = "com.local.matholickiosk.webpoc"
    const val ACTION_START_SECURE_SESSION =
        "com.local.matholickiosk.action.START_SECURE_WEB_SESSION"
    const val COLUMN_EXPECTED_NAME = "expected_name"
    const val COLUMN_USERNAME = "username"
    const val COLUMN_PASSWORD = "password"
    const val EXTRA_FAILURE_REASON = "failure_reason"

    fun uri(id: String): Uri = Uri.Builder()
        .scheme("content")
        .authority(AUTHORITY)
        .appendPath("v1")
        .appendPath(id)
        .build()
}

object OneTimeCredentialBroker {
    private const val TTL_MILLIS = 30_000L
    private const val NONCE_BYTES = 24
    private val secureRandom = SecureRandom()
    private var entry: Entry? = null

    @Synchronized
    fun publish(
        expectedDisplayName: String,
        username: CharArray,
        password: CharArray,
        nowElapsedMs: Long = SystemClock.elapsedRealtime(),
    ): CredentialBridgeHandle {
        require(expectedDisplayName.isNotBlank()) { "Expected display name is required" }
        require(username.isNotEmpty() && password.isNotEmpty()) { "Credentials are required" }
        wipeEntry()
        val idBytes = ByteArray(NONCE_BYTES).also(secureRandom::nextBytes)
        val id = try {
            Base64.getUrlEncoder().withoutPadding().encodeToString(idBytes)
        } finally {
            idBytes.fill(0)
        }
        entry = Entry(
            id = id,
            expiresAtElapsedMs = nowElapsedMs + TTL_MILLIS,
            expectedDisplayName = expectedDisplayName,
            username = username.copyOf(),
            password = password.copyOf(),
        )
        return CredentialBridgeHandle(id, CredentialBridgeContract.uri(id))
    }

    @Synchronized
    fun consume(id: String, nowElapsedMs: Long = SystemClock.elapsedRealtime()): BridgePayload? {
        val current = entry ?: return null
        entry = null
        if (current.id != id || nowElapsedMs > current.expiresAtElapsedMs) {
            current.wipe()
            return null
        }
        return BridgePayload(
            current.expectedDisplayName,
            current.username.copyOf(),
            current.password.copyOf(),
        ).also { current.wipe() }
    }

    @Synchronized
    fun revoke(id: String) {
        if (entry?.id == id) wipeEntry()
    }

    @Synchronized
    fun clear() {
        wipeEntry()
    }

    private fun wipeEntry() {
        entry?.wipe()
        entry = null
    }

    private class Entry(
        val id: String,
        val expiresAtElapsedMs: Long,
        val expectedDisplayName: String,
        val username: CharArray,
        val password: CharArray,
    ) {
        fun wipe() {
            username.fill('\u0000')
            password.fill('\u0000')
        }
    }
}
