package com.local.matholickiosk.kiosk

import java.util.Base64

internal object RemoteQrTestPayload {
    private const val HASH_BYTES = 32

    fun decode(encodedHash: String?, supportActive: Boolean): ByteArray? {
        if (!supportActive || encodedHash == null || encodedHash.length != 44) return null
        val decoded = runCatching { Base64.getDecoder().decode(encodedHash) }.getOrNull()
            ?: return null
        if (decoded.size != HASH_BYTES) {
            decoded.fill(0)
            return null
        }
        return decoded
    }
}

internal object RemoteQrTestBridge {
    private var owner: Any? = null
    private var listener: ((ByteArray) -> Unit)? = null

    @Synchronized
    fun register(owner: Any, listener: (ByteArray) -> Unit) {
        this.owner = owner
        this.listener = listener
    }

    @Synchronized
    fun unregister(owner: Any) {
        if (this.owner === owner) {
            this.owner = null
            listener = null
        }
    }

    fun deliver(tokenHash: ByteArray): Boolean {
        val target = synchronized(this) { listener } ?: return false
        return runCatching {
            target(tokenHash)
            true
        }.getOrDefault(false)
    }
}
