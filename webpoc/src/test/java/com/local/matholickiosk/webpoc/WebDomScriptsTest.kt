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
    fun `logout contract does not delete website draft storage`() {
        val script = WebDomScripts.clickLogout
        assertFalse(script.contains("localStorage"))
        assertFalse(script.contains("sessionStorage"))
        assertFalse(script.contains("indexedDB"))
        assertFalse(script.contains("deleteDatabase"))
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
        assertTrue(script.contains("reviewHelpOpen"))
        assertTrue(script.contains("helpContext"))
        assertTrue(script.contains("reviewHelpOpen ? 'REVIEW'"))
        assertTrue(script.contains("problemMapHelpOpen ? 'PROBLEM_MAP'"))
        assertTrue(script.contains("gradingHelpBlocked ? 'NONE'"))
        assertTrue(script.contains("isWorkbook ? 'WORKBOOK'"))
        assertTrue(script.contains("isDiagnostic ? 'DIAGNOSTIC'"))
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
        assertFalse(script.contains("숫자 · 소수점 · 부호"))
        assertTrue(script.contains("toggle-sign"))
        assertTrue(script.contains("matholic-kiosk-key-icon"))
        assertTrue(script.contains("한 칸 삭제"))
        assertTrue(script.contains("실행 취소"))
        assertFalse(script.contains("다시 실행"))
        assertTrue(script.contains("matholicKioskProblemMapTransition"))
        assertTrue(script.contains("matholic-kiosk-objective-choice-zone"))
        assertTrue(script.contains("setInterval"))
        assertFalse(script.contains("다시 눌러 지움"))
        assertFalse(script.contains("window.confirm"))
        assertTrue(script.contains("matholicKioskKeypadActive"))
        assertTrue(script.contains("textarea.setAttribute('inputmode', 'none')"))
        assertFalse(script.contains("textarea.setAttribute('inputmode', 'decimal')"))
        assertTrue(script.contains("mathKeypadEnhancements"))
        assertTrue(script.contains("ensureSubjectiveTouchTargets"))
        assertTrue(script.contains("matholicKioskSubjectiveTouchTarget"))
        assertTrue(script.contains("matholicKioskSubjectiveTouchScope"))
        assertTrue(script.contains(
            "div:has(> .ant-input-affix-wrapper input[placeholder*=\"주관식 답\"]",
        ))
        assertFalse(script.contains(
            "div:has(input[placeholder*=\"주관식 답\"]",
        ))
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
        assertTrue(script.contains("현재 문제 번호"))
        assertTrue(script.contains(".ant-select-selection-item"))
        assertTrue(script.contains("visibleButtonInside"))
        assertTrue(script.contains("matholic-kiosk-problem-map"))
        assertTrue(script.contains("border: 1px solid #c7d4e3"))
        assertTrue(script.contains("box-shadow: 0 4px 12px"))
        assertTrue(script.contains("matholicKioskProblemStates"))
        assertTrue(script.contains("답안 현황"))
        assertTrue(script.contains("이전 미입력"))
        assertTrue(script.contains("다음 미입력"))
        assertTrue(script.contains("matholicKioskProblemMapDismissGuard"))
        assertTrue(script.contains("activePointerId"))
        assertTrue(script.contains("swallowNextClick"))
        assertTrue(script.contains("consumeDismissEvent"))
        assertTrue(script.contains("document.addEventListener('pointerup'"))
        assertTrue(script.contains("navigateToUnanswered"))
        assertTrue(script.contains("matholicKioskNavigateToProblem"))
        assertTrue(script.contains("matholicKioskProblemNavigationController"))
        assertTrue(script.contains("continueProblemNavigation"))
        assertTrue(script.contains("key.startsWith('__reactProps')"))
        assertTrue(script.contains(".rc-virtual-list-holder"))
        assertTrue(script.contains("rect.width > 0 && rect.height > 0"))
        assertTrue(script.contains("chooseProblemOption(attempt + 1)"))
        assertTrue(script.contains("scheduleProblemNavigation(150)"))
        assertTrue(script.contains("matholic-kiosk-current-problem-badge"))
        assertTrue(script.contains("left: 96px !important"))
        assertTrue(script.contains("matholicKioskPressed"))
        assertTrue(script.contains("--matholic-kiosk-keypad-top"))
        assertTrue(script.contains("editorRect ? editorRect.bottom + 16"))
        assertTrue(script.contains("finishButtonClearance = 88"))
        assertTrue(script.contains("matholicKioskBottomClearance"))
        assertFalse(script.contains("const contentOverlap = occludedContent.reduce"))
        assertTrue(script.contains("matholicKioskDirectProblemSelect"))
        assertTrue(script.contains("opacity: 0 !important"))
        assertTrue(script.contains("waitForSelectorToClose"))
        assertTrue(script.contains("setTimeout(waitForSelectorToClose, 100)"))
        assertFalse(script.contains(
            "combobox?.dispatchEvent(new KeyboardEvent",
        ))
        assertFalse(script.contains("matholicKioskDirectionFeedback"))
        assertFalse(script.contains("matholicKioskDirectionFeedbackBound"))
        assertTrue(script.contains("localizeEmptyListState"))
        assertTrue(script.contains("진단평가가 없습니다"))
        assertTrue(script.contains("학습지가 없습니다"))
        assertTrue(script.contains("aria-busy"))
        assertTrue(script.contains("matholicKioskSingleProblem"))
        assertFalse(script.contains("풀지 못한 문제는 빈칸으로 두지 말고"))
        assertTrue(script.contains("problemStateMapEnhancements"))
        assertTrue(script.contains("matholicKioskLongPageScroll"))
        assertTrue(script.contains("matholicKioskLongPageMedia"))
        assertTrue(script.contains("--matholic-kiosk-long-page-extra"))
        assertTrue(script.contains("image.matholic.com"))
        assertTrue(script.contains("requiredMaxScroll"))
        assertTrue(script.contains("enhanceLongProblemScrolling"))
        assertTrue(script.contains("ensureLongProblemTouchGuard"))
        assertTrue(script.contains("matholicKioskLongPageScrollController"))
        assertTrue(script.contains("event.stopImmediatePropagation()"))
        assertTrue(script.contains("longProblemScrollEnhancements"))
        assertTrue(script.contains("detectObjectiveChoiceMarkers"))
        assertTrue(script.contains("relaxedCandidates"))
        assertTrue(script.contains("expectedCount !== 2"))
        assertTrue(script.contains("alignedHorizontally"))
        assertTrue(script.contains(".ant-radio-group .ant-radio-wrapper"))
        assertTrue(script.contains("ant-radio-wrapper-disabled"))
        assertTrue(script.contains("matholicKioskObjectiveTapReady"))
        assertTrue(script.contains("objectiveImageTapEnhancements"))
        assertTrue(script.contains("touch-action: pan-y"))
        assertTrue(script.contains("pointer-events: auto"))
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

    @Test
    fun `student help tracks real controls and separates answer types`() {
        val objective = WebDomScripts.showStudentHelp("PROBLEM_OBJECTIVE")
        val subjective = WebDomScripts.showStudentHelp("PROBLEM_SUBJECTIVE")
        val workbook = WebDomScripts.showStudentHelp("WORKBOOK")
        listOf(objective, subjective, workbook).forEach { script ->
            assertTrue(script.contains("matholic-kiosk-live-help"))
            assertTrue(script.contains("getBoundingClientRect"))
            assertTrue(script.contains("requestAnimationFrame(update)"))
            assertTrue(script.contains("실제 화면 안내"))
            assertTrue(script.contains("pointer-events:none"))
        }
        assertTrue(objective.contains("객관식 문제"))
        assertTrue(objective.contains(".ant-radio-group"))
        assertTrue(subjective.contains("주관식 문제"))
        assertTrue(subjective.contains(".mq-editable-field"))
        assertTrue(subjective.contains("분수·소수 형식을 지정"))
        assertFalse(workbook.contains("학습지가 선택됐는지"))
        assertTrue(workbook.contains("학습지 제목과 단원"))
        assertTrue(workbook.contains("지금 채점할 학습지가 없습니다"))
        assertTrue(workbook.contains("‘진단평가’로 이동하세요"))
        assertTrue(subjective.contains("matholicKioskKeypadActive"))
        assertTrue(subjective.contains("document.activeElement?.blur?.()"))
        val review = WebDomScripts.showStudentHelp("REVIEW")
        assertTrue(review.contains("reviewSubmit"))
        assertTrue(review.contains("reviewRoot.contains(control)"))
        assertTrue(review.contains("getBoundingClientRect().bottom"))
        assertFalse(review.contains("reviewFooter"))
        assertTrue(review.contains("누르면 실제 채점이 시작됩니다"))
        val map = WebDomScripts.showStudentHelp("PROBLEM_MAP")
        assertTrue(map.contains("data-open=\"true\""))
        assertTrue(map.contains("현황판 안의 문제 번호"))
        assertTrue(WebDomScripts.closeStudentHelp.contains("controller?.close?.()"))
    }
}
