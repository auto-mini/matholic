package com.local.matholickiosk.webpoc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebDomScriptsTest {
    @Test
    fun `login contract uses semantic controls and approved auth target`() {
        val script = WebDomScripts.sanitizeLoginAndFingerprint
        assertTrue(script.contains("input"))
        assertTrue(script.contains("username"))
        assertTrue(script.contains("password"))
        assertTrue(script.contains("checkbox"))
        assertTrue(script.contains("button[type=\"submit\"]"))
        assertTrue(script.contains("auth.matholic.com"))
        assertTrue(script.contains("/token/signin"))
    }

    @Test
    fun `portal and logout contracts do not contain coordinate selectors`() {
        val scripts = listOf(
            WebDomScripts.portalFingerprint,
            WebDomScripts.openAccountMenu,
            WebDomScripts.clickLogout,
        )
        scripts.forEach { script ->
            assertFalse(script.contains("getBoundingClientRect"))
            assertFalse(script.contains("elementFromPoint"))
            assertFalse(script.contains("dispatchTouchEvent"))
        }
        assertTrue(WebDomScripts.portalFingerprint.contains("/userInfo"))
        assertTrue(WebDomScripts.portalFingerprint.contains("/userAccessLog"))
        assertTrue(WebDomScripts.clickLogout.contains("로그아웃"))
    }
}
