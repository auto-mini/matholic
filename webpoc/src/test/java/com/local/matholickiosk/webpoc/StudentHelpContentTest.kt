package com.local.matholickiosk.webpoc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentHelpContentTest {
    @Test
    fun `student paths select the matching help context`() {
        assertEquals(StudentHelpContext.WORKBOOK, StudentHelpContext.fromPath("/workbook"))
        assertEquals(StudentHelpContext.WORKBOOK, StudentHelpContext.fromPath("/workbook/123"))
        assertEquals(StudentHelpContext.DIAGNOSTIC, StudentHelpContext.fromPath("/diagnostic"))
        assertEquals(StudentHelpContext.PROBLEM, StudentHelpContext.fromPath("/learningV2/123"))
        assertEquals(StudentHelpContext.NONE, StudentHelpContext.fromPath("/course"))
    }

    @Test
    fun `dom contract values fail closed and cover review`() {
        assertEquals(StudentHelpContext.REVIEW, StudentHelpContext.fromContract("REVIEW"))
        assertEquals(StudentHelpContext.PROBLEM, StudentHelpContext.fromContract("problem"))
        assertEquals(StudentHelpContext.NONE, StudentHelpContext.fromContract("unexpected"))
        assertEquals(StudentHelpContext.NONE, StudentHelpContext.fromContract(null))
    }

    @Test
    fun `every interactive student context has actionable copy`() {
        StudentHelpContext.entries.filterNot { it == StudentHelpContext.NONE }.forEach { context ->
            val copy = StudentHelpContent.forContext(context)
            assertNotNull(copy)
            assertTrue(copy!!.title.isNotBlank())
            assertTrue(copy.lead.isNotBlank())
            assertTrue(copy.steps.contains("1."))
            assertTrue(copy.caution.isNotBlank())
        }
        assertNull(StudentHelpContent.forContext(StudentHelpContext.NONE))
    }
}
