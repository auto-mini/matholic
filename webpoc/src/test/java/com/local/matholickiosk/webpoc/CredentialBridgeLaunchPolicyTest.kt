package com.local.matholickiosk.webpoc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialBridgeLaunchPolicyTest {
    @Test
    fun `credential handle accepts exactly 32 base64url characters`() {
        assertTrue(
            CredentialBridgeLaunchPolicy.isValidHandleId(
                "0123456789abcdefghijklmnopqrstuv",
            ),
        )
        assertTrue(
            CredentialBridgeLaunchPolicy.isValidHandleId(
                "ABCD_efgh-IJKL_mnop-QRST_uvwx123",
            ),
        )
        assertFalse(CredentialBridgeLaunchPolicy.isValidHandleId(null))
        assertFalse(CredentialBridgeLaunchPolicy.isValidHandleId(""))
        assertFalse(
            CredentialBridgeLaunchPolicy.isValidHandleId(
                "0123456789abcdefghijklmnopqrstu",
            ),
        )
        assertFalse(
            CredentialBridgeLaunchPolicy.isValidHandleId(
                "0123456789abcdefghijklmnopqrstuvw",
            ),
        )
        assertFalse(
            CredentialBridgeLaunchPolicy.isValidHandleId(
                "0123456789abcdefghijklmnopqrstu+",
            ),
        )
    }
}
