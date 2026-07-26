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
    fun `portal recognition accepts only the course document without fragments`() {
        assertTrue(WebSecurityPolicy.isPortalUrl("https://im.matholic.com/course"))
        assertTrue(WebSecurityPolicy.isPortalUrl("https://im.matholic.com/course?from=login"))

        assertFalse(WebSecurityPolicy.isPortalUrl("https://im.matholic.com/"))
        assertFalse(WebSecurityPolicy.isPortalUrl("https://im.matholic.com/userInfo"))
        assertFalse(WebSecurityPolicy.isPortalUrl("https://im.matholic.com/workbook"))
        assertFalse(WebSecurityPolicy.isPortalUrl("https://im.matholic.com/course#/userInfo"))

        assertTrue(WebSecurityPolicy.isLearningHostUrl("https://im.matholic.com/workbook"))
        assertFalse(WebSecurityPolicy.isLearningHostUrl("https://login.matholic.com/course"))
    }

    @Test
    fun `login recognition accepts only the root document without fragments`() {
        assertTrue(WebSecurityPolicy.isLoginUrl("https://login.matholic.com/"))
        assertTrue(WebSecurityPolicy.isLoginUrl("https://login.matholic.com/?from=logout"))

        assertFalse(WebSecurityPolicy.isLoginUrl("https://login.matholic.com/course"))
        assertFalse(WebSecurityPolicy.isLoginUrl("https://login.matholic.com/#/alternate"))
        assertFalse(WebSecurityPolicy.isLoginUrl("https://auth.matholic.com/"))
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

    @Test
    fun `student routes reject path traversal and ambiguous encoded separators`() {
        val rejected = listOf(
            "https://im.matholic.com/workbook/../course",
            "https://im.matholic.com/workbook/%2e%2e/course",
            "https://im.matholic.com/workbook/%2E%2E/course",
            "https://im.matholic.com/workbook/%2f../course",
            "https://im.matholic.com/workbook/%5c..%5ccourse",
            "https://im.matholic.com/learningV2/../course",
            "https://im.matholic.com/learningV2/%2e%2e/course",
            "https://im.matholic.com/workbook/%252e%252e/course",
        )

        rejected.forEach { url ->
            assertFalse(url, WebSecurityPolicy.isAllowedStudentUrl(url))
            assertTrue(url, WebSecurityPolicy.pathOf(url) == null)
        }
    }

    @Test
    fun `student routes reject fragments that can select a different SPA view`() {
        val rejected = listOf(
            "https://im.matholic.com/workbook#/course",
            "https://im.matholic.com/diagnostic#userInfo",
            "https://im.matholic.com/learningV2/answer/123#../course",
            "https://im.matholic.com/workbook#",
        )

        rejected.forEach { url ->
            assertFalse(url, WebSecurityPolicy.isAllowedStudentUrl(url))
            assertTrue(url, WebSecurityPolicy.pathOf(url) == null)
        }

        assertTrue(
            WebSecurityPolicy.isAllowedStudentUrl(
                "https://im.matholic.com/workbook?tab=assigned&id=1",
            ),
        )
    }
}
