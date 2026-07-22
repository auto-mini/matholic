package com.local.matholickiosk.poc

import org.junit.Assert.assertFalse
import org.junit.Test

class GatePolicyTest {
    @Test
    fun `automation remains locked before Gate 1 approval`() {
        assertFalse(GatePolicy.GATE1_APPROVAL_RECORDED)
        assertFalse(GatePolicy.canRunAutomation())
    }
}
