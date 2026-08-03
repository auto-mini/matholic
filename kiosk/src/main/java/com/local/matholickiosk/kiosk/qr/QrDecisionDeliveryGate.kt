package com.local.matholickiosk.kiosk.qr

internal class QrDecisionDeliveryGate {
    private val lock = Any()
    private var enabled = true
    private var generation = 0L

    fun setEnabled(value: Boolean) {
        synchronized(lock) {
            generation += 1
            enabled = value
        }
    }

    fun currentFrameGeneration(): Long? = synchronized(lock) {
        generation.takeIf { enabled }
    }

    fun isEnabled(): Boolean = synchronized(lock) { enabled }

    fun deliverIfCurrent(
        frameGeneration: Long,
        decision: QrFrameDecision,
        deliver: (QrFrameDecision) -> Unit,
    ): Boolean = synchronized(lock) {
        if (enabled && generation == frameGeneration) {
            deliver(decision)
            true
        } else {
            decision.clearSensitiveData()
            false
        }
    }
}

internal fun QrFrameDecision.clearSensitiveData() {
    if (this is QrFrameDecision.Accept) tokenHash.fill(0)
}
