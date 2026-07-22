package com.local.matholickiosk.webpoc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectTargetPolicyTest {
    @Test
    fun `parses allowed HTTPS CONNECT targets`() {
        val target = ConnectTargetPolicy.parseRequestLine("CONNECT login.matholic.com:443 HTTP/1.1")
        assertEquals(ConnectTarget("login.matholic.com", 443), target)
        assertTrue(ConnectTargetPolicy.isAllowed(target!!))
        assertTrue(ConnectTargetPolicy.isAllowed(ConnectTarget("wcs.naver.net", 443)))
    }

    @Test
    fun `rejects non CONNECT malformed and disallowed targets`() {
        assertNull(ConnectTargetPolicy.parseRequestLine("GET https://login.matholic.com/ HTTP/1.1"))
        assertNull(ConnectTargetPolicy.parseRequestLine("CONNECT bad..host:443 HTTP/1.1"))
        assertFalse(ConnectTargetPolicy.isAllowed(ConnectTarget("matholic.com.attacker.test", 443)))
        assertFalse(ConnectTargetPolicy.isAllowed(ConnectTarget("login.matholic.com", 80)))
    }
}
