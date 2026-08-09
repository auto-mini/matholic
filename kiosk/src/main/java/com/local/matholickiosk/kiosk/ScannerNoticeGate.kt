package com.local.matholickiosk.kiosk

internal class ScannerNoticeGate {
    private var generation = 0
    private var passiveGuidanceSuppressed = false

    fun begin(): Int {
        passiveGuidanceSuppressed = true
        return ++generation
    }

    fun finish(expectedGeneration: Int): Boolean {
        if (expectedGeneration != generation) return false
        passiveGuidanceSuppressed = false
        return true
    }

    fun invalidate() {
        generation += 1
        passiveGuidanceSuppressed = false
    }

    fun suppressesPassiveGuidance(): Boolean = passiveGuidanceSuppressed
}
