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

    @Test
    fun `student experience is route scoped and uses semantic answer controls`() {
        val script = WebDomScripts.applyStudentExperience
        assertTrue(script.contains("/workbook"))
        assertTrue(script.contains("/diagnostic"))
        assertTrue(script.contains("/learningV2/"))
        assertTrue(script.contains("답안제출"))
        assertTrue(script.contains("모름"))
        assertTrue(script.contains("다음 문제"))
        assertTrue(script.contains("전체답안"))
        assertFalse(script.contains("elementFromPoint"))
        assertTrue(script.contains("MutationObserver"))
        assertTrue(script.contains(".ant-tooltip"))
        assertTrue(script.contains("hideLateStudentContent"))
        assertTrue(script.contains(".ant-modal-wrap"))
        assertTrue(script.contains("scrollIntoView"))
        assertTrue(script.contains("matholicKioskReviewScrollStableReads"))
        assertTrue(script.contains("matholicKioskReviewScrollUserOverride"))
        assertTrue(script.contains("touchstart"))
        assertTrue(script.contains("resetHiddenReviewScrollState"))
        assertTrue(script.contains("matholicKioskAnswerSubmitReentryGuard"))
        assertTrue(script.contains("matholicKioskSubmitReentryBlocked"))
        assertTrue(script.contains("stopImmediatePropagation"))
        assertTrue(script.contains("restoreMathInputInteraction"))
        assertTrue(script.contains("min-height: 56px"))
        assertFalse(script.contains(
            "scope.style.setProperty('pointer-events', 'none', 'important')",
        ))
        assertFalse(script.contains(".mq-editable-field ~ button"))
        assertTrue(script.contains("hideMathClearControls"))
        assertTrue(script.contains("matholicKioskMathNavigationDismissGuard"))
        assertTrue(script.contains("data-matholic-kiosk-active"))
        assertTrue(script.contains("ensureSubjectiveTouchTargets"))
        assertTrue(script.contains("matholicKioskSubjectiveTouchTarget"))
        assertTrue(script.contains("matholicKioskSubjectiveTouchScope"))
        assertTrue(script.contains("important(target, 'display', 'inline-block')"))
        assertTrue(script.contains(
            "'.mq-editable-field,.mq-math-mode'",
        ))
        assertTrue(script.contains("ensureMathQuillRuntime"))
        assertTrue(script.contains("/js/mathquill/jquery-3.2.1.min.js"))
        assertTrue(script.contains("/js/mathquill/mathquill.min.js"))
        assertTrue(script.contains("matholicKioskMathShell"))
        assertTrue(script.contains("inline.width !== '160px'"))
        assertTrue(script.contains("remountUninitializedMathShells"))
        assertTrue(script.contains("mathAnswerBindingFor"))
        assertTrue(script.contains("mathModeRemounted"))
        assertTrue(script.contains("matholicKioskMathShellSeenAt"))
        assertTrue(script.contains("now - firstSeen < 1500"))
        assertTrue(script.contains("scheduleViewportMaintenance"))
        assertTrue(script.contains("'scroll',"))
        assertTrue(script.contains("enhanceProblemNavigation"))
        assertTrue(script.contains("matholic-kiosk-problem-number"))
        assertTrue(script.contains("width: 112px"))
        assertTrue(script.contains("height: 96px"))
        assertTrue(script.contains("현재 문제 번호 선택"))
        assertTrue(script.contains(".ant-select-selection-item"))
        assertTrue(script.contains("visibleButtonInside"))
    }

    @Test
    fun `result summary requires semantic analysis heading and alert cards`() {
        val script = WebDomScripts.wrongAnswerSummary
        assertTrue(script.contains("종합분석"))
        assertTrue(script.contains(".ant-alert-error"))
        assertTrue(script.contains(".ant-alert-success"))
        assertTrue(script.contains("wrongNumbers"))
    }
}
