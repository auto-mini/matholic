package com.local.matholickiosk.poc

object GatePolicy {
    const val GATE1_APPROVAL_RECORDED = false

    fun canRunAutomation(): Boolean = GATE1_APPROVAL_RECORDED
}
