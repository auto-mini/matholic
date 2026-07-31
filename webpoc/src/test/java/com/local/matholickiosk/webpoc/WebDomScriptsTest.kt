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
        assertTrue(script.contains("contentReady"))
        assertTrue(script.contains("analysisReady"))
        assertTrue(script.contains("mutationMaintenanceTimer"))
        assertTrue(script.contains("record.addedNodes.length"))
        assertTrue(script.contains("record.removedNodes.length"))
        assertTrue(script.contains("if (mutationMaintenanceTimer) return"))
        assertTrue(script.contains("hideLateStudentContent(node)"))
        assertTrue(script.contains(
            "attributeFilter: ['class', 'style', 'hidden']",
        ))
        assertFalse(script.contains("characterData: true"))
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
        assertTrue(script.contains("matholic-kiosk-keypad-inner"))
        assertTrue(script.contains("matholic-kiosk-keypad-numeric-grid"))
        assertTrue(script.contains("matholic-kiosk-keypad-structure-grid"))
        assertTrue(script.contains("matholic-kiosk-keypad-actions"))
        assertTrue(script.contains("숫자 · 소수점 · 부호"))
        assertTrue(script.contains("한 칸 삭제"))
        assertTrue(script.contains("실행 취소"))
        assertTrue(script.contains("다시 실행"))
        assertTrue(script.contains("setInterval"))
        assertTrue(script.contains("다시 눌러 지움"))
        assertFalse(script.contains("window.confirm"))
        assertTrue(script.contains("matholicKioskKeypadActive"))
        assertTrue(script.contains("textarea.setAttribute('inputmode', 'none')"))
        assertFalse(script.contains("textarea.setAttribute('inputmode', 'decimal')"))
        assertTrue(script.contains("mathKeypadEnhancements"))
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
        assertTrue(script.contains("clearTimeout(viewportMaintenanceTimer)"))
        assertTrue(script.contains("enhanceProblemNavigation"))
        assertTrue(script.contains("matholic-kiosk-problem-number"))
        assertTrue(script.contains("width: 52px"))
        assertTrue(script.contains("height: 58px"))
        assertTrue(script.contains("grid-template-columns: 52px minmax(0, 1fr) 52px"))
        assertTrue(script.contains("matholicKioskLabel"))
        assertTrue(script.contains("문제 목록 열기"))
        assertTrue(script.contains(".ant-select-selection-item"))
        assertTrue(script.contains("visibleButtonInside"))
        assertTrue(script.contains("matholic-kiosk-problem-map"))
        assertTrue(script.contains("border: 1px solid #c7d4e3"))
        assertTrue(script.contains("box-shadow: 0 4px 12px"))
        assertTrue(script.contains("matholicKioskProblemStates"))
        assertTrue(script.contains("답안 현황"))
        assertTrue(script.contains("이전 미입력"))
        assertTrue(script.contains("다음 미입력"))
        assertTrue(script.contains("navigateToUnanswered"))
        assertTrue(script.contains("matholicKioskNavigateToProblem"))
        assertTrue(script.contains("matholicKioskProblemNavigationController"))
        assertTrue(script.contains("continueProblemNavigation"))
        assertTrue(script.contains("aria-busy"))
        assertTrue(script.contains("matholicKioskSingleProblem"))
        assertFalse(script.contains("풀지 못한 문제는 빈칸으로 두지 말고"))
        assertTrue(script.contains("problemStateMapEnhancements"))
        val leftPreset = WebDomScripts.applyStudentExperience("left")
        assertTrue(leftPreset.contains("matholicKioskKeypadPreset"))
        assertTrue(leftPreset.contains("'left'"))
        val invalidPreset = WebDomScripts.applyStudentExperience("unexpected")
        assertTrue(invalidPreset.contains("'right'"))
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
