package com.local.matholickiosk.probe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RedactionPolicyTest {
    private val fixedSalt = ByteArray(32) { it.toByte() }

    @Test
    fun `known fixed UI label is preserved`() {
        val policy = RedactionPolicy(fixedSalt)
        assertEquals("로그인", policy.redact(" 로그인 "))
    }

    @Test
    fun `unknown text is never returned verbatim`() {
        val policy = RedactionPolicy(fixedSalt)
        val raw = "개인식별가능문자열"
        val redacted = requireNotNull(policy.redact(raw))
        assertFalse(redacted.contains(raw))
        assertTrue(redacted.startsWith("<REDACTED_TEXT"))
    }

    @Test
    fun `editable or password value is always sensitive even if label is known`() {
        val policy = RedactionPolicy(fixedSalt)
        val redacted = requireNotNull(policy.redact("로그인", sensitive = true))
        assertFalse(redacted.contains("로그인"))
        assertTrue(redacted.startsWith("<REDACTED_SENSITIVE"))
    }

    @Test
    fun `same session can correlate equal redacted values`() {
        val policy = RedactionPolicy(fixedSalt)
        assertEquals(policy.redact("동일값"), policy.redact("동일값"))
    }

    @Test
    fun `different session salt produces different fingerprint`() {
        val first = RedactionPolicy(ByteArray(32) { 1 }).redact("동일값")
        val second = RedactionPolicy(ByteArray(32) { 2 }).redact("동일값")
        assertNotEquals(first, second)
    }

    @Test
    fun `fingerprint has a fixed lowercase hex width`() {
        val redacted = requireNotNull(RedactionPolicy(fixedSalt).redact("형식검증"))
        assertTrue(redacted.matches(Regex("<REDACTED_TEXT len=4 fp=[0-9a-f]{16}>")))
    }

    @Test
    fun `null remains null and empty remains empty`() {
        val policy = RedactionPolicy(fixedSalt)
        assertNull(policy.redact(null))
        assertEquals("", policy.redact(""))
    }
}
