package com.local.matholickiosk.probe

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetPackagePolicyTest {
    @Test
    fun `only confirmed package is allowed`() {
        assertTrue(TargetPackagePolicy.isAllowed("com.matholic.mathapp"))
        assertFalse(TargetPackagePolicy.isAllowed("com.example.other"))
        assertFalse(TargetPackagePolicy.isAllowed(null))
    }
}
