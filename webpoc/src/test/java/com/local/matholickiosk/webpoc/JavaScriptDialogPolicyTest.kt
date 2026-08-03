package com.local.matholickiosk.webpoc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JavaScriptDialogPolicyTest {
    @Test
    fun `native dialog actions stay disarmed past a duplicate tap sequence`() {
        assertTrue(JavaScriptDialogPolicy.ACTION_ARM_DELAY_MS >= 500L)
    }

    @Test
    fun `active student confirmation can use native origin-free dialog`() {
        assertTrue(
            JavaScriptDialogPolicy.canReplaceBrowserDialog(
                WebPocState.ACTIVE,
                "https://im.matholic.com/learningV2/answer/virtual",
                "모든 답이 입력되지 않았습니다. 그래도 답안을 제출하시겠습니까?",
            ),
        )
    }

    @Test
    fun `dialog replacement rejects non-student routes and non-active states`() {
        assertFalse(
            JavaScriptDialogPolicy.canReplaceBrowserDialog(
                WebPocState.ACTIVE,
                "https://im.matholic.com/course",
                "확인",
            ),
        )
        assertFalse(
            JavaScriptDialogPolicy.canReplaceBrowserDialog(
                WebPocState.LOGIN_SUBMIT,
                "https://im.matholic.com/learningV2/answer/virtual",
                "확인",
            ),
        )
    }

    @Test
    fun `dialog replacement rejects empty and oversized messages`() {
        assertFalse(
            JavaScriptDialogPolicy.canReplaceBrowserDialog(
                WebPocState.ACTIVE,
                WebSecurityPolicy.WORKBOOK_URL,
                "",
            ),
        )
        assertFalse(
            JavaScriptDialogPolicy.canReplaceBrowserDialog(
                WebPocState.ACTIVE,
                WebSecurityPolicy.WORKBOOK_URL,
                "x".repeat(JavaScriptDialogPolicy.MAX_MESSAGE_LENGTH + 1),
            ),
        )
    }
}
