package com.local.matholickiosk.webpoc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebSecurityPolicyTest {
    @Test
    fun `only approved HTTPS top-level origins are allowed`() {
        assertTrue(WebSecurityPolicy.isAllowedTopLevelUrl("https://login.matholic.com/"))
        assertTrue(WebSecurityPolicy.isAllowedTopLevelUrl("https://auth.matholic.com/token/signin"))
        assertTrue(WebSecurityPolicy.isAllowedTopLevelUrl("https://im.matholic.com/course"))

        assertFalse(WebSecurityPolicy.isAllowedTopLevelUrl("http://login.matholic.com/"))
        assertFalse(WebSecurityPolicy.isAllowedTopLevelUrl("https://evil.matholic.com/"))
        assertFalse(WebSecurityPolicy.isAllowedTopLevelUrl("https://login.matholic.com.evil.test/"))
        assertFalse(WebSecurityPolicy.isAllowedTopLevelUrl("https://user@login.matholic.com/"))
        assertFalse(WebSecurityPolicy.isAllowedTopLevelUrl("https://login.matholic.com:444/"))
        assertFalse(WebSecurityPolicy.isAllowedTopLevelUrl("javascript:alert(1)"))
    }

    @Test
    fun `display name comparison is exact after narrow normalization`() {
        assertTrue(WebSecurityPolicy.displayNamesMatch(" 홍길동 ", "홍길동"))
        assertTrue(WebSecurityPolicy.displayNamesMatch("홍  길동", "홍 길동"))
        assertTrue(WebSecurityPolicy.displayNamesMatch("Ａ학생", "A학생"))

        assertFalse(WebSecurityPolicy.displayNamesMatch("홍길동", "홍길동1"))
        assertFalse(WebSecurityPolicy.displayNamesMatch("홍길동", "홍길동 학생"))
        assertFalse(WebSecurityPolicy.displayNamesMatch("홍길동", "홍길순"))
        assertFalse(WebSecurityPolicy.displayNamesMatch("", "홍길동"))
    }

    @Test
    fun `student mode allows only worksheet diagnostic and learning routes`() {
        assertTrue(WebSecurityPolicy.isAllowedStudentUrl("https://im.matholic.com/workbook"))
        assertTrue(WebSecurityPolicy.isAllowedStudentUrl("https://im.matholic.com/workbook/assigned?id=1"))
        assertTrue(WebSecurityPolicy.isAllowedStudentUrl("https://im.matholic.com/diagnostic"))
        assertTrue(WebSecurityPolicy.isAllowedStudentUrl("https://im.matholic.com/diagnostic/list"))
        assertTrue(WebSecurityPolicy.isAllowedStudentUrl("https://im.matholic.com/learningV2/answer/123"))

        assertFalse(WebSecurityPolicy.isAllowedStudentUrl("https://im.matholic.com/course"))
        assertFalse(WebSecurityPolicy.isAllowedStudentUrl("https://im.matholic.com/userInfo"))
        assertFalse(WebSecurityPolicy.isAllowedStudentUrl("https://im.matholic.com/workbook-other"))
        assertFalse(WebSecurityPolicy.isAllowedStudentUrl("https://login.matholic.com/workbook"))
        assertFalse(WebSecurityPolicy.isAllowedStudentUrl("http://im.matholic.com/workbook"))
        assertFalse(WebSecurityPolicy.isAllowedStudentUrl("https://user@im.matholic.com/workbook"))
    }
}
