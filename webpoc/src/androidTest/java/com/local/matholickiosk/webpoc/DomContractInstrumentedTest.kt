package com.local.matholickiosk.webpoc

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject
import org.json.JSONTokener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class DomContractInstrumentedTest {
    private val instrumentation
        get() = InstrumentationRegistry.getInstrumentation()

    @Test
    fun testLoginSanitizeClearsAllResidualFields() {
        withFixture(
            "https://login.matholic.com/",
            loginFixture("https://auth.matholic.com/token/signin"),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.sanitizeLoginAndFingerprint)
            assertTrue(result.getBoolean("ok"))
            assertTrue(result.getBoolean("usernameEmpty"))
            assertTrue(result.getBoolean("passwordEmpty"))
            assertFalse(result.getBoolean("rememberChecked"))
            assertEquals(WebDomScripts.CONTRACT_VERSION, result.getString("version"))
        }
    }

    @Test
    fun testLoginFingerprintRejectsUnexpectedAuthTarget() {
        withFixture(
            "https://login.matholic.com/",
            loginFixture("https://example.invalid/signin"),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.sanitizeLoginAndFingerprint)
            assertFalse(result.getBoolean("ok"))
            assertFalse(result.getBoolean("actionOk"))
            assertFalse(result.getBoolean("usernameEmpty"))
            assertFalse(result.getBoolean("passwordEmpty"))
            assertTrue(result.getBoolean("rememberChecked"))
        }
    }

    @Test
    fun testLoginScriptsRejectNonRootLoginDocumentWithoutTouchingCredentials() {
        withFixture(
            "https://login.matholic.com/not-a-login-document",
            loginFixture(
                "https://auth.matholic.com/token/signin",
                preventSubmit = true,
            ),
        ) { webView ->
            assertFalse(
                evaluate(webView, WebDomScripts.sanitizeLoginAndFingerprint)
                    .getBoolean("ok"),
            )
            assertFalse(
                evaluate(webView, WebDomScripts.login("virtual-user", "virtual-pass"))
                    .getBoolean("ok"),
            )
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  submitted: document.body.dataset.submitted === 'yes',
                  usernameUnchanged:
                    document.querySelector('input[name="username"]').value === 'residual-user',
                  passwordUnchanged:
                    document.querySelector('input[name="password"]').value === 'residual-pass'
                }))()
                """.trimIndent(),
            )
            assertFalse(proof.getBoolean("submitted"))
            assertTrue(proof.getBoolean("usernameUnchanged"))
            assertTrue(proof.getBoolean("passwordUnchanged"))
        }
    }

    @Test
    fun testLoginFingerprintRejectsUnsafeAuthEndpointDetails() {
        listOf(
            "https://auth.matholic.com:444/token/signin",
            "https://user:pass@auth.matholic.com/token/signin",
            "https://auth.matholic.com/token/signin?alternate=true",
            "https://auth.matholic.com/token/signin#alternate",
        ).forEach { action ->
            withFixture(
                "https://login.matholic.com/",
                loginFixture(action),
            ) { webView ->
                val result = evaluate(webView, WebDomScripts.sanitizeLoginAndFingerprint)
                assertFalse(action, result.getBoolean("ok"))
                assertFalse(action, result.getBoolean("actionOk"))
                assertFalse(action, result.getBoolean("usernameEmpty"))
                assertFalse(action, result.getBoolean("passwordEmpty"))
            }
        }
    }

    @Test
    fun testLoginSubmitNeverPopulatesCredentialsForUnsafeAuthEndpoint() {
        withFixture(
            "https://login.matholic.com/",
            loginFixture(
                "https://auth.matholic.com:444/token/signin",
                preventSubmit = true,
            ),
        ) { webView ->
            assertFalse(
                evaluate(webView, WebDomScripts.login("virtual-user", "virtual-pass"))
                    .getBoolean("ok"),
            )
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  submitted: document.body.dataset.submitted === 'yes',
                  usernameUnchanged:
                    document.querySelector('input[name="username"]').value === 'residual-user',
                  passwordUnchanged:
                    document.querySelector('input[name="password"]').value === 'residual-pass'
                }))()
                """.trimIndent(),
            )
            assertFalse(proof.getBoolean("submitted"))
            assertTrue(proof.getBoolean("usernameUnchanged"))
            assertTrue(proof.getBoolean("passwordUnchanged"))
        }
    }

    @Test
    fun testLoginSubmitUsesSemanticFormControls() {
        withFixture(
            "https://login.matholic.com/",
            loginFixture("https://auth.matholic.com/token/signin", preventSubmit = true),
        ) { webView ->
            val submit = evaluate(webView, WebDomScripts.login("virtual-user", "virtual-pass"))
            assertTrue(submit.getBoolean("ok"))

            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  submitted: document.body.dataset.submitted === 'yes',
                  usernameSet: document.querySelector('input[name="username"]').value === 'virtual-user',
                  passwordSet: document.querySelector('input[name="password"]').value === 'virtual-pass',
                  rememberChecked: document.querySelector('input[type="checkbox"]').checked
                }))()
                """.trimIndent(),
            )
            assertTrue(proof.getBoolean("submitted"))
            assertTrue(proof.getBoolean("usernameSet"))
            assertTrue(proof.getBoolean("passwordSet"))
            assertFalse(proof.getBoolean("rememberChecked"))
        }
    }

    @Test
    fun testPortalFingerprintMenuAndLogoutAreSemantic() {
        withFixture("https://im.matholic.com/course", portalFixture()) { webView ->
            val fingerprint = evaluate(webView, WebDomScripts.portalFingerprint)
            assertTrue(fingerprint.getBoolean("ok"))
            assertEquals("가상학생", fingerprint.getString("actualName"))
            assertEquals(1, fingerprint.getInt("userInfoCount"))
            assertEquals(1, fingerprint.getInt("accessLogCount"))

            assertTrue(evaluate(webView, WebDomScripts.openAccountMenu).getBoolean("ok"))
            assertTrue(evaluate(webView, WebDomScripts.clickLogout).getBoolean("ok"))
            val proof = evaluate(
                webView,
                "(() => JSON.stringify({ loggedOut: document.body.dataset.loggedOut === 'yes' }))()",
            )
            assertTrue(proof.getBoolean("loggedOut"))
        }
    }

    @Test
    fun testPortalFingerprintRejectsCrossOriginLookalikeLinks() {
        val fixture = portalFixture()
            .replace("href=\"/userInfo\"", "href=\"https://example.invalid/userInfo\"")
        withFixture("https://im.matholic.com/course", fixture) { webView ->
            val fingerprint = evaluate(webView, WebDomScripts.portalFingerprint)
            assertFalse(fingerprint.getBoolean("ok"))
            assertEquals(0, fingerprint.getInt("userInfoCount"))
        }
    }

    @Test
    fun testPortalActionsRejectNonDefaultPortOrigin() {
        withFixture("https://im.matholic.com:444/course", portalFixture()) { webView ->
            assertFalse(evaluate(webView, WebDomScripts.portalFingerprint).getBoolean("ok"))
            assertFalse(evaluate(webView, WebDomScripts.openAccountMenu).getBoolean("ok"))
            assertFalse(evaluate(webView, WebDomScripts.clickLogout).getBoolean("ok"))
        }
    }

    @Test
    fun testPortalActionsAcceptSemanticShellAtAlternatePath() {
        withFixture("https://im.matholic.com/userInfo", portalFixture()) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.portalFingerprint).getBoolean("ok"))
            assertTrue(evaluate(webView, WebDomScripts.openAccountMenu).getBoolean("ok"))
            assertTrue(evaluate(webView, WebDomScripts.clickLogout).getBoolean("ok"))
        }
    }

    @Test
    fun testPortalFingerprintRejectsCredentialedSemanticLink() {
        val fixture = portalFixture().replace(
            "href=\"/userInfo\"",
            "href=\"https://user:pass@im.matholic.com/userInfo\"",
        )
        withFixture("https://im.matholic.com/course", fixture) { webView ->
            val fingerprint = evaluate(webView, WebDomScripts.portalFingerprint)
            assertFalse(fingerprint.getBoolean("ok"))
            assertEquals(0, fingerprint.getInt("userInfoCount"))
        }
    }

    @Test
    fun testLogoutRejectsMultipleVisibleExactControls() {
        val fixture = portalFixture(
            "<button onclick=\"document.body.dataset.loggedOut='wrong'\">로그아웃</button>",
        )
        withFixture("https://im.matholic.com/course", fixture) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.openAccountMenu).getBoolean("ok"))
            val result = evaluate(webView, WebDomScripts.clickLogout)
            assertFalse(result.getBoolean("ok"))
            assertEquals(0, result.getInt("count"))
            assertEquals(2, result.getInt("leafExactCount"))
        }
    }

    @Test
    fun testLogoutSupportsExactSemanticLeafInsideListItem() {
        val fixture = portalFixture(
            "<li onclick=\"document.body.dataset.loggedOut='yes'\"><p>로그아웃</p></li>",
            includeDefaultLogout = false,
        )
        withFixture("https://im.matholic.com/course", fixture) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.openAccountMenu).getBoolean("ok"))
            val result = evaluate(webView, WebDomScripts.clickLogout)
            assertTrue(result.getBoolean("ok"))
            assertEquals(1, result.getInt("count"))
            val proof = evaluate(
                webView,
                "(() => JSON.stringify({ loggedOut: document.body.dataset.loggedOut === 'yes' }))()",
            )
            assertTrue(proof.getBoolean("loggedOut"))
        }
    }

    @Test
    fun testLogoutUsesUniqueHiddenSemanticFallbackInsideValidatedSubmenu() {
        withFixture("https://im.matholic.com/course", portalFixture()) { webView ->
            val result = evaluate(webView, WebDomScripts.clickLogout)
            assertTrue(result.getBoolean("ok"))
            assertEquals(1, result.getInt("count"))
            assertTrue(result.getInt("exactAllCount") >= 1)
            assertEquals(0, result.getInt("visibleExactCount"))
            assertEquals(1, result.getInt("leafExactCount"))
            assertFalse(result.getBoolean("submenuVisible"))
            assertTrue(result.getBoolean("usedHiddenFallback"))
            val proof = evaluate(
                webView,
                "(() => JSON.stringify({ loggedOut: document.body.dataset.loggedOut === 'yes' }))()",
            )
            assertTrue(proof.getBoolean("loggedOut"))
        }
    }

    @Test
    fun testLogoutRejectsMultipleHiddenSemanticLeaves() {
        val fixture = portalFixture(
            "<button onclick=\"document.body.dataset.loggedOut='wrong'\">로그아웃</button>",
        )
        withFixture("https://im.matholic.com/course", fixture) { webView ->
            val result = evaluate(webView, WebDomScripts.clickLogout)
            assertFalse(result.getBoolean("ok"))
            assertEquals(0, result.getInt("count"))
            assertEquals(2, result.getInt("leafExactCount"))
            assertFalse(result.getBoolean("usedHiddenFallback"))
        }
    }

    @Test
    fun testStudentExperienceEnlargesSemanticLearningControls() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <button id="unknown">모름</button>
                <button id="next">&gt;</button>
                <h2>전체답안</h2>
                <button id="submit">답안 제출</button>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            assertTrue(result.getBoolean("learningPage"))
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  stylePresent: !!document.getElementById('matholic-kiosk-student-style'),
                  unknownWidth: document.getElementById('unknown').style.minWidth,
                  nextWidth: document.getElementById('next').style.minWidth,
                  submitWidth: document.getElementById('submit').style.minWidth,
                  submitPosition: document.getElementById('submit').style.position,
                  submitRight: document.getElementById('submit').style.right,
                  submitBottom: document.getElementById('submit').style.bottom
                }))()
                """.trimIndent(),
            )
            assertTrue(proof.getBoolean("stylePresent"))
            assertEquals("104px", proof.getString("unknownWidth"))
            assertEquals("72px", proof.getString("nextWidth"))
            assertEquals("190px", proof.getString("submitWidth"))
            assertEquals("", proof.getString("submitPosition"))
            assertEquals("", proof.getString("submitRight"))
            assertEquals("", proof.getString("submitBottom"))
        }
    }

    @Test
    fun testStudentExperienceHidesGlobalChromeAndUnwantedAnswerControls() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div id="global-line">
                <a href="/course">학습실</a>
                <button>학습활동</button>
                <a href="/userInfo">개인정보</a>
                <a href="/userAccessLog">로그인 정보</a>
              </div>
              <main id="problem">
                <button id="report">오류신고</button>
                <button id="paper">문제지</button>
                <button id="handwriting">답안필기입력</button>
                <div id="answer-modes">
                  <div class="ant-input-affix-wrapper">
                    <input id="short-answer" placeholder="주관식 답은 여기에">
                    <span class="ant-input-suffix">
                      <button id="handwriting-icon"><svg></svg></button>
                      <button id="answer-help">도움말</button>
                    </span>
                  </div>
                  <button id="input-menu"
                    onclick="document.getElementById('mode-menu').style.display='block'">입력기</button>
                  <ul id="mode-menu" style="display:none">
                    <li id="basic">기본</li>
                    <li id="fraction">분수</li>
                    <li id="math" onclick="this.dataset.clicked='yes'">수식</li>
                  </ul>
                </div>
                <div id="math-entry">
                  <div>
                    <button>루트</button>
                    <button>분수</button>
                    <button>파이</button>
                  </div>
                  <div style="position: relative">
                    <span></span>
                    <div style="position: absolute; top: 8px; right: 8px">
                      <button id="direct-handwriting"><svg></svg></button>
                    </div>
                  </div>
                </div>
                <aside id="video-panel">
                  <p>문제가 어렵나요? 이 문제의 대표 유형 해설 강의를 들어보세요!</p>
                  <iframe></iframe>
                </aside>
                <div id="video-image-panel">
                  <p>문제가 어렵나요? 이 문제의 해설 강의를 들어보세요!</p>
                  <div><img alt="문항 동영상"></div>
                </div>
                <button id="submit">답안 제출</button>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  globalHidden: getComputedStyle(document.getElementById('global-line')).display === 'none',
                  problemVisible: getComputedStyle(document.getElementById('problem')).display !== 'none',
                  reportHidden: getComputedStyle(document.getElementById('report')).display === 'none',
                  paperHidden: getComputedStyle(document.getElementById('paper')).display === 'none',
                  handwritingHidden:
                    getComputedStyle(document.getElementById('handwriting')).display === 'none',
                  handwritingIconHidden:
                    getComputedStyle(document.getElementById('handwriting-icon')).display === 'none',
                  answerHelpVisible:
                    getComputedStyle(document.getElementById('answer-help')).display !== 'none',
                  basicHidden: getComputedStyle(document.getElementById('basic')).display === 'none',
                  fractionVisible:
                    getComputedStyle(document.getElementById('fraction')).display !== 'none',
                  mathClicked: document.getElementById('math').dataset.clicked === 'yes',
                  inputMenuHidden:
                    getComputedStyle(document.getElementById('input-menu')).display === 'none',
                  directHandwritingHidden:
                    getComputedStyle(document.getElementById('direct-handwriting')).display === 'none',
                  videoHidden: getComputedStyle(document.getElementById('video-panel')).display === 'none',
                  imageVideoHidden:
                    getComputedStyle(document.getElementById('video-image-panel')).display === 'none',
                  submitSpacing: document.getElementById('submit').style.marginBottom
                }))()
                """.trimIndent(),
            )
            assertTrue(proof.getBoolean("globalHidden"))
            assertTrue(proof.getBoolean("problemVisible"))
            assertTrue(proof.getBoolean("reportHidden"))
            assertTrue(proof.getBoolean("paperHidden"))
            assertTrue(proof.getBoolean("handwritingHidden"))
            assertTrue(proof.getBoolean("handwritingIconHidden"))
            assertTrue(proof.getBoolean("answerHelpVisible"))
            assertTrue(proof.getBoolean("basicHidden"))
            assertTrue(proof.getBoolean("fractionVisible"))
            assertTrue(proof.getBoolean("mathClicked"))
            assertTrue(proof.getBoolean("inputMenuHidden"))
            assertTrue(proof.getBoolean("directHandwritingHidden"))
            assertTrue(proof.getBoolean("videoHidden"))
            assertTrue(proof.getBoolean("imageVideoHidden"))
            assertEquals("19px", proof.getString("submitSpacing"))

            evaluate(
                webView,
                """
                (() => {
                  const lateChrome = document.createElement('div');
                  lateChrome.id = 'late-global-line';
                  lateChrome.innerHTML = `
                    <a href="/course">학습실</a>
                    <button>학습활동</button>
                    <a href="/userInfo">개인정보</a>
                    <a href="/userAccessLog">로그인 정보</a>`;
                  document.body.insertBefore(lateChrome, document.getElementById('problem'));
                  const late = document.createElement('div');
                  late.innerHTML = `
                    <button id="late-report">오류신고</button>
                    <div id="late-math-tooltip" class="ant-tooltip" role="tooltip">수식</div>
                    <div id="answer-input-form-late">
                      <button id="late-input-menu"
                        onclick="document.getElementById('late-mode-menu').style.display='block'">입력기</button>
                      <ul id="late-mode-menu" style="display:none">
                        <li id="late-basic">기본</li>
                        <li id="late-fraction">분수</li>
                        <li id="late-math"
                          onclick="this.dataset.clicked='yes'">수식</li>
                      </ul>
                    </div>
                    <div>
                      <button>루트</button>
                      <button>분수</button>
                      <button>파이</button>
                    </div>
                    <div style="position: relative">
                      <span></span>
                      <div style="position: absolute; top: 8px; right: 8px">
                        <button id="late-direct-handwriting"><svg></svg></button>
                      </div>
                    </div>`;
                  document.getElementById('problem').appendChild(late);
                  return JSON.stringify({ appended: true });
                })()
                """.trimIndent(),
            )
            Thread.sleep(100)
            val lateProof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  hidden:
                    getComputedStyle(document.getElementById('late-direct-handwriting')).display === 'none',
                  reportHidden:
                    getComputedStyle(document.getElementById('late-report')).display === 'none',
                  mathTooltipHidden:
                    getComputedStyle(document.getElementById('late-math-tooltip')).display === 'none',
                  chromeHidden:
                    getComputedStyle(document.getElementById('late-global-line')).display === 'none',
                  inputMenuHidden:
                    getComputedStyle(document.getElementById('late-input-menu')).display === 'none',
                  mathClicked:
                    document.getElementById('late-math').dataset.clicked === 'yes',
                  basicHidden:
                    getComputedStyle(document.getElementById('late-basic')).display === 'none',
                  fractionVisible:
                    getComputedStyle(document.getElementById('late-fraction')).display !== 'none'
                }))()
                """.trimIndent(),
            )
            assertTrue(lateProof.getBoolean("hidden"))
            assertTrue(lateProof.getBoolean("reportHidden"))
            assertTrue(lateProof.getBoolean("mathTooltipHidden"))
            assertTrue(lateProof.getBoolean("chromeHidden"))
            assertTrue(lateProof.getBoolean("inputMenuHidden"))
            assertTrue(lateProof.getBoolean("mathClicked"))
            assertTrue(lateProof.getBoolean("basicHidden"))
            assertTrue(lateProof.getBoolean("fractionVisible"))

            evaluate(
                webView,
                """
                (() => {
                  [
                    document.getElementById('late-report'),
                    document.getElementById('late-math-tooltip'),
                    document.getElementById('late-direct-handwriting')
                  ].forEach(element => {
                    element.style.setProperty('display', 'block', 'important');
                    element.removeAttribute('aria-hidden');
                  });
                  return JSON.stringify({ resurfaced: true });
                })()
                """.trimIndent(),
            )
            Thread.sleep(100)
            val resurfacedProof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  reportHidden:
                    getComputedStyle(document.getElementById('late-report')).display === 'none',
                  mathTooltipHidden:
                    getComputedStyle(document.getElementById('late-math-tooltip')).display === 'none',
                  directHandwritingHidden:
                    getComputedStyle(document.getElementById('late-direct-handwriting')).display === 'none'
                }))()
                """.trimIndent(),
            )
            assertTrue(resurfacedProof.getBoolean("reportHidden"))
            assertTrue(resurfacedProof.getBoolean("mathTooltipHidden"))
            assertTrue(resurfacedProof.getBoolean("directHandwritingHidden"))
        }
    }

    @Test
    fun testStudentExperienceBlocksAnswerInputUntilMathFieldIsReady() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div id="answer-input-form-0">
                  <input id="basic-answer" placeholder="주관식 답은 여기에">
                  <button id="input-menu"
                    onclick="document.getElementById('mode-menu').style.display='block'">
                    입력기
                  </button>
                </div>
                <ul id="mode-menu" style="display:none">
                  <li>기본</li>
                  <li>분수</li>
                  <li id="math-mode" onclick="
                    const scope = document.getElementById('answer-input-form-0');
                    scope.innerHTML = `
                      <div>
                        <button>루트</button>
                        <button>분수</button>
                        <button>파이</button>
                      </div>
                      <span id='math-editor'></span>
                      <button id='settled-input-menu'>입력기</button>`;
                  ">수식</li>
                </ul>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val initial = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertEquals(1, initial.getInt("mathModePending"))
            assertEquals(0, initial.getInt("mathModeReady"))

            val initialProof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  toolbarMounted: ['루트', '분수', '파이'].every(label =>
                    Array.from(document.querySelectorAll(
                      '#answer-input-form-0 button'
                    )).some(button => button.textContent.trim() === label)
                  ),
                  fieldNotReady:
                    !document.getElementById('math-editor')
                      .classList.contains('mq-editable-field'),
                  inputBlocked:
                    getComputedStyle(document.getElementById('answer-input-form-0'))
                      .pointerEvents === 'none'
                }))()
                """.trimIndent(),
            )
            assertTrue(initialProof.getBoolean("toolbarMounted"))
            assertTrue(initialProof.getBoolean("fieldNotReady"))
            assertTrue(initialProof.getBoolean("inputBlocked"))

            evaluate(
                webView,
                """
                (() => {
                  const editor = document.getElementById('math-editor');
                  let latex = '';
                  editor.innerHTML =
                    '<span class="mq-textarea"><textarea></textarea></span>' +
                    '<span class="mq-root-block"></span>';
                  editor.fieldApi = {
                    latex: value => {
                      if (value !== undefined) latex = value;
                      return latex;
                    },
                    write: value => { latex += value; },
                    keystroke: key => {
                      if (key === 'Backspace') latex = latex.slice(0, -1);
                    }
                  };
                  window.MathQuill = {
                    getInterface: () => element => element.fieldApi || null
                  };
                  editor.classList.add('mq-editable-field');
                  return JSON.stringify({ settled: true });
                })()
                """.trimIndent(),
            )
            Thread.sleep(100)
            val settled = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertEquals(0, settled.getInt("mathModePending"))
            assertEquals(1, settled.getInt("mathModeReady"))
            val settledProof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  fieldReady:
                    document.getElementById('math-editor')
                      .classList.contains('mq-editable-field'),
                  inputEnabled:
                    getComputedStyle(document.getElementById('answer-input-form-0'))
                      .pointerEvents !== 'none'
                }))()
                """.trimIndent(),
            )
            assertTrue(settledProof.getBoolean("fieldReady"))
            assertTrue(settledProof.getBoolean("inputEnabled"))
        }
    }

    @Test
    fun testStudentExperiencePrimesMathFieldSoFirstUserEditPersists() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div id="answer-input-form-0">
                  <input id="basic-answer" placeholder="주관식 답은 여기에">
                  <button id="input-menu"
                    onclick="document.getElementById('mode-menu').style.display='block'">
                    입력기
                  </button>
                </div>
                <ul id="mode-menu" style="display:none">
                  <li>기본</li>
                  <li>분수</li>
                  <li id="math-mode" onclick="mountMathField()">수식</li>
                </ul>
                <script>
                  window.mathLatex = '';
                  window.persistedLatex = null;
                  window.ignoredMathEdits = 2;
                  window.mathEditCount = 0;
                  window.recordMathEdit = () => {
                    window.mathEditCount += 1;
                    if (window.ignoredMathEdits > 0) {
                      window.ignoredMathEdits -= 1;
                    } else {
                      window.persistedLatex = window.mathLatex || null;
                    }
                  };
                  window.mountMathField = () => {
                    const scope = document.getElementById('answer-input-form-0');
                    scope.innerHTML = `
                      <div>
                        <button>루트</button>
                        <button>분수</button>
                        <button>파이</button>
                      </div>
                      <span id="math-editor" class="mq-editable-field mq-math-mode">
                        <span class="mq-textarea"><textarea></textarea></span>
                        <span class="mq-root-block"></span>
                      </span>
                      <button id="settled-input-menu">입력기</button>`;
                    const editor = document.getElementById('math-editor');
                    editor.fieldApi = {
                      latex: () => window.mathLatex,
                      write: value => {
                        window.mathLatex += value;
                        window.recordMathEdit();
                      },
                      keystroke: key => {
                        if (key === 'Backspace') {
                          window.mathLatex = window.mathLatex.slice(0, -1);
                        }
                        window.recordMathEdit();
                      }
                    };
                  };
                  window.MathQuill = {
                    getInterface: () => element => element.fieldApi || null
                  };
                  window.typeFirstStudentDigit = digit => {
                    window.mathLatex += digit;
                    window.recordMathEdit();
                  };
                </script>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            evaluate(webView, WebDomScripts.applyStudentExperience)
            Thread.sleep(100)
            evaluate(webView, WebDomScripts.applyStudentExperience)
            val proof = evaluate(
                webView,
                """
                (() => {
                  window.typeFirstStudentDigit('7');
                  const textarea = document.querySelector(
                    '#answer-input-form-0 .mq-textarea textarea'
                  );
                  return JSON.stringify({
                    latex: window.mathLatex,
                    persisted: window.persistedLatex,
                    ignoredEdits: window.ignoredMathEdits,
                    editCount: window.mathEditCount,
                    inputEnabled:
                      getComputedStyle(document.getElementById('answer-input-form-0'))
                        .pointerEvents !== 'none',
                    stabilized:
                      document.getElementById('math-editor')
                        .dataset.matholicKioskMathStabilized === 'true',
                    inputMode: textarea ? textarea.getAttribute('inputmode') : null
                  });
                })()
                """.trimIndent(),
            )
            assertEquals("7", proof.getString("latex"))
            assertEquals("7", proof.getString("persisted"))
            assertEquals(0, proof.getInt("ignoredEdits"))
            assertEquals(3, proof.getInt("editCount"))
            assertTrue(proof.getBoolean("inputEnabled"))
            assertTrue(proof.getBoolean("stabilized"))
            assertEquals("decimal", proof.getString("inputMode"))
        }
    }

    @Test
    fun testStudentExperiencePrimesEveryMountedMathFieldAndHidesClearControl() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div id="answer-input-form-0"></div>
                <script>
                  window.installMathField = () => {
                    const scope = document.getElementById('answer-input-form-0');
                    scope.innerHTML = `
                      <div>
                        <button>루트</button>
                        <button>분수</button>
                        <button>파이</button>
                      </div>
                      <div id="math-wrap" style="position:relative">
                        <span id="math-editor" class="mq-editable-field mq-math-mode">
                          <span class="mq-textarea"><textarea></textarea></span>
                          <span class="mq-root-block"></span>
                        </span>
                      </div>
                      <button id="input-menu">입력기</button>`;
                    const editor = document.getElementById('math-editor');
                    const state = {
                      latex: '',
                      persisted: null,
                      ignoredEdits: 2,
                      editCount: 0
                    };
                    const recordEdit = () => {
                      state.editCount += 1;
                      if (state.ignoredEdits > 0) {
                        state.ignoredEdits -= 1;
                        return;
                      }
                      state.persisted = state.latex || null;
                      if (!document.getElementById('math-clear')) {
                        const clear = document.createElement('button');
                        clear.id = 'math-clear';
                        clear.textContent = '×';
                        clear.onclick = () => {
                          state.latex = '';
                          state.persisted = null;
                        };
                        document.getElementById('math-wrap').appendChild(clear);
                      }
                    };
                    editor.fieldApi = {
                      latex: value => {
                        if (value !== undefined) state.latex = value;
                        return state.latex;
                      },
                      write: value => {
                        state.latex += value;
                        recordEdit();
                      },
                      keystroke: key => {
                        if (key === 'Backspace') {
                          state.latex = state.latex.slice(0, -1);
                        }
                        recordEdit();
                      }
                    };
                    window.currentMathState = state;
                  };
                  window.MathQuill = {
                    getInterface: () => element => element.fieldApi || null
                  };
                  window.typeStudentDigit = digit => {
                    const state = window.currentMathState;
                    state.latex += digit;
                    const field = document.getElementById('math-editor').fieldApi;
                    field.write('');
                  };
                  window.installMathField();
                </script>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            evaluate(webView, WebDomScripts.applyStudentExperience)
            evaluate(
                webView,
                "window.typeStudentDigit('4'); JSON.stringify({typed:true})",
            )
            val first = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  latex: window.currentMathState.latex,
                  persisted: window.currentMathState.persisted,
                  ignoredEdits: window.currentMathState.ignoredEdits,
                  editCount: window.currentMathState.editCount,
                  clearDisplay: document.getElementById('math-clear') ?
                    getComputedStyle(document.getElementById('math-clear')).display :
                    'missing'
                }))()
                """.trimIndent(),
            )
            assertEquals("4", first.getString("latex"))
            assertEquals("4", first.getString("persisted"))
            assertEquals(0, first.getInt("ignoredEdits"))
            assertEquals(3, first.getInt("editCount"))
            assertEquals("none", first.getString("clearDisplay"))

            evaluate(
                webView,
                "window.installMathField(); JSON.stringify({remounted:true})",
            )
            evaluate(webView, WebDomScripts.applyStudentExperience)
            evaluate(
                webView,
                "window.typeStudentDigit('8'); JSON.stringify({typed:true})",
            )
            val remounted = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  latex: window.currentMathState.latex,
                  persisted: window.currentMathState.persisted,
                  ignoredEdits: window.currentMathState.ignoredEdits,
                  editCount: window.currentMathState.editCount,
                  stabilized:
                    document.getElementById('math-editor')
                      .dataset.matholicKioskMathStabilized === 'true'
                }))()
                """.trimIndent(),
            )
            assertEquals("8", remounted.getString("latex"))
            assertEquals("8", remounted.getString("persisted"))
            assertEquals(0, remounted.getInt("ignoredEdits"))
            assertEquals(3, remounted.getInt("editCount"))
            assertTrue(remounted.getBoolean("stabilized"))
        }
    }

    @Test
    fun testStudentExperienceScrollsReviewHidesUploadAndKeepsFinalSubmitInFlow() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div class="ant-modal" role="dialog">
                <div class="ant-modal-title">전체답안</div>
                <div id="review-scroll" class="ant-modal-body"
                     style="height:220px;overflow-y:auto">
                  <div style="height:1200px">
                    <div id="upload">
                      <h4>풀이 과정</h4>
                      <div class="ant-upload-wrapper"><button>풀이 업로드</button></div>
                    </div>
                  </div>
                </div>
                <div class="ant-modal-footer">
                  <button id="final-submit"
                          style="position:fixed!important;right:230px!important;
                                 bottom:24px!important;z-index:2147483000!important;
                                 box-shadow:0 6px 18px rgba(0,0,0,.35)!important">
                    답안 제출
                  </button>
                </div>
              </div>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            evaluate(
                webView,
                """
                (() => {
                  document.getElementById('final-submit').scrollIntoView = () => {
                    document.body.dataset.finalSubmitScrolledIntoView = 'yes';
                  };
                  return JSON.stringify({ installed: true });
                })()
                """.trimIndent(),
            )
            repeat(2) {
                assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            }
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  scrollMarked:
                    document.documentElement.dataset.matholicKioskReviewScrolled === 'true',
                  uploadHidden:
                    getComputedStyle(document.getElementById('upload')).display === 'none',
                  movedToBottom: document.getElementById('review-scroll').scrollTop > 0,
                  submitInFlow:
                    document.getElementById('final-submit').style.position === '' &&
                    document.getElementById('final-submit').style.right === '' &&
                    document.getElementById('final-submit').style.bottom === '' &&
                    document.getElementById('final-submit').style.zIndex === '' &&
                    document.getElementById('final-submit').style.boxShadow === '',
                  submitWidth:
                    document.getElementById('final-submit').style.minWidth,
                  submitScrolledIntoView:
                    document.body.dataset.finalSubmitScrolledIntoView === 'yes',
                  stableReads:
                    document.querySelector('.ant-modal').dataset
                      .matholicKioskReviewScrollStableReads,
                  scrollComplete:
                    document.querySelector('.ant-modal').dataset
                      .matholicKioskReviewScrollComplete,
                  finalRect:
                    document.getElementById('final-submit').getBoundingClientRect().toJSON(),
                  viewportHeight: window.innerHeight
                }))()
                """.trimIndent(),
            )
            assertTrue(proof.toString(), proof.getBoolean("scrollMarked"))
            assertTrue(proof.toString(), proof.getBoolean("uploadHidden"))
            assertTrue(proof.toString(), proof.getBoolean("movedToBottom"))
            assertTrue(proof.toString(), proof.getBoolean("submitInFlow"))
            assertEquals("190px", proof.getString("submitWidth"))
            assertTrue(proof.getBoolean("submitScrolledIntoView"))
        }
    }

    @Test
    fun testStudentExperienceScrollsAntModalWrapAgainAfterLateReviewLayout() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div id="review-wrap" class="ant-modal-wrap"
                   style="height:220px;overflow-y:auto;position:relative">
                <div class="ant-modal" role="dialog">
                  <div class="ant-modal-content">
                    <div class="ant-modal-header">
                      <div class="ant-modal-title">전체답안</div>
                    </div>
                    <div class="ant-modal-body">
                      <div id="answers" style="height:900px"></div>
                      <div id="upload">
                        <h4>풀이 과정</h4>
                        <div class="ant-upload-wrapper"><button>풀이 업로드</button></div>
                      </div>
                    </div>
                    <div class="ant-modal-footer">
                      <button id="final-submit">답안 제출</button>
                    </div>
                  </div>
                </div>
              </div>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            evaluate(
                webView,
                """
                (() => {
                  const button = document.getElementById('final-submit');
                  button.scrollIntoView = () => {
                    document.body.dataset.reviewScrollRequests =
                      String(Number(document.body.dataset.reviewScrollRequests || '0') + 1);
                  };
                  return JSON.stringify({ installed: true });
                })()
                """.trimIndent(),
            )
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            evaluate(
                webView,
                """
                (() => {
                  document.getElementById('answers').style.height = '1400px';
                  document.getElementById('review-wrap').scrollTop = 0;
                  return JSON.stringify({ changed: true });
                })()
                """.trimIndent(),
            )
            repeat(2) {
                assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            }
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  wrapperMovedToBottom:
                    document.getElementById('review-wrap').scrollTop > 0,
                  scrollRequestCount:
                    Number(document.body.dataset.reviewScrollRequests || '0'),
                  scrollMarked:
                    document.documentElement.dataset.matholicKioskReviewScrolled === 'true',
                  uploadHidden:
                    getComputedStyle(document.getElementById('upload')).display === 'none',
                  submitInFlow:
                    getComputedStyle(document.getElementById('final-submit')).position !== 'fixed'
                }))()
                """.trimIndent(),
            )
            assertTrue(proof.getBoolean("wrapperMovedToBottom"))
            assertTrue(proof.getInt("scrollRequestCount") >= 2)
            assertTrue(proof.getBoolean("scrollMarked"))
            assertTrue(proof.getBoolean("uploadHidden"))
            assertTrue(proof.getBoolean("submitInFlow"))
            val requestsBeforeManualScroll = proof.getInt("scrollRequestCount")
            evaluate(
                webView,
                """
                (() => {
                  document.getElementById('review-wrap').scrollTop = 0;
                  return JSON.stringify({ changed: true });
                })()
                """.trimIndent(),
            )
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val afterManualScroll = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  wrapperScrollTop: document.getElementById('review-wrap').scrollTop,
                  scrollRequestCount:
                    Number(document.body.dataset.reviewScrollRequests || '0')
                }))()
                """.trimIndent(),
            )
            assertEquals(0, afterManualScroll.getInt("wrapperScrollTop"))
            assertEquals(
                requestsBeforeManualScroll,
                afterManualScroll.getInt("scrollRequestCount"),
            )
            evaluate(
                webView,
                """
                (() => {
                  document.getElementById('review-wrap').style.display = 'none';
                  return JSON.stringify({ hidden: true });
                })()
                """.trimIndent(),
            )
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            evaluate(
                webView,
                """
                (() => {
                  const wrapper = document.getElementById('review-wrap');
                  wrapper.style.display = 'block';
                  wrapper.scrollTop = 0;
                  return JSON.stringify({ reopened: true });
                })()
                """.trimIndent(),
            )
            repeat(2) {
                assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            }
            val afterReopen = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  wrapperMovedToBottom:
                    document.getElementById('review-wrap').scrollTop > 0,
                  scrollRequestCount:
                    Number(document.body.dataset.reviewScrollRequests || '0')
                }))()
                """.trimIndent(),
            )
            assertTrue(afterReopen.getBoolean("wrapperMovedToBottom"))
            assertTrue(
                afterReopen.getInt("scrollRequestCount") > requestsBeforeManualScroll,
            )
        }
    }

    @Test
    fun testStudentTouchStopsReviewAutoScrollBeforeStableCompletion() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div id="review-wrap" class="ant-modal-wrap"
                   style="height:220px;overflow-y:auto;position:relative">
                <div class="ant-modal" role="dialog">
                  <div class="ant-modal-content">
                    <div class="ant-modal-header">
                      <div class="ant-modal-title">전체답안</div>
                    </div>
                    <div class="ant-modal-body">
                      <div id="answers" style="height:1400px"></div>
                    </div>
                    <div class="ant-modal-footer">
                      <button id="final-submit">답안 제출</button>
                    </div>
                  </div>
                </div>
              </div>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            evaluate(
                webView,
                """
                (() => {
                  const button = document.getElementById('final-submit');
                  button.scrollIntoView = () => {
                    document.body.dataset.reviewScrollRequests =
                      String(Number(document.body.dataset.reviewScrollRequests || '0') + 1);
                  };
                  return JSON.stringify({ installed: true });
                })()
                """.trimIndent(),
            )
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val beforeTouch = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  wrapperMovedToBottom:
                    document.getElementById('review-wrap').scrollTop > 0,
                  scrollRequestCount:
                    Number(document.body.dataset.reviewScrollRequests || '0'),
                  scrollComplete:
                    document.querySelector('.ant-modal').dataset
                      .matholicKioskReviewScrollComplete === 'true'
                }))()
                """.trimIndent(),
            )
            assertTrue(beforeTouch.getBoolean("wrapperMovedToBottom"))
            assertFalse(beforeTouch.getBoolean("scrollComplete"))
            val requestsBeforeTouch = beforeTouch.getInt("scrollRequestCount")

            evaluate(
                webView,
                """
                (() => {
                  const answers = document.getElementById('answers');
                  answers.dispatchEvent(new Event('touchstart', {
                    bubbles: true,
                    cancelable: true
                  }));
                  document.getElementById('review-wrap').scrollTop = 0;
                  return JSON.stringify({ touched: true });
                })()
                """.trimIndent(),
            )
            repeat(3) {
                assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            }
            val afterTouch = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  wrapperScrollTop: document.getElementById('review-wrap').scrollTop,
                  scrollRequestCount:
                    Number(document.body.dataset.reviewScrollRequests || '0'),
                  userOverride:
                    document.querySelector('.ant-modal').dataset
                      .matholicKioskReviewScrollUserOverride === 'true'
                }))()
                """.trimIndent(),
            )
            assertEquals(0, afterTouch.getInt("wrapperScrollTop"))
            assertEquals(requestsBeforeTouch, afterTouch.getInt("scrollRequestCount"))
            assertTrue(afterTouch.getBoolean("userOverride"))

            evaluate(
                webView,
                """
                (() => {
                  document.getElementById('review-wrap').style.display = 'none';
                  return JSON.stringify({ hidden: true });
                })()
                """.trimIndent(),
            )
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            evaluate(
                webView,
                """
                (() => {
                  const wrapper = document.getElementById('review-wrap');
                  wrapper.style.display = 'block';
                  wrapper.scrollTop = 0;
                  return JSON.stringify({ reopened: true });
                })()
                """.trimIndent(),
            )
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val afterReopen = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  wrapperMovedToBottom:
                    document.getElementById('review-wrap').scrollTop > 0,
                  scrollRequestCount:
                    Number(document.body.dataset.reviewScrollRequests || '0'),
                  userOverride:
                    document.querySelector('.ant-modal').dataset
                      .matholicKioskReviewScrollUserOverride === 'true'
                }))()
                """.trimIndent(),
            )
            assertTrue(afterReopen.getBoolean("wrapperMovedToBottom"))
            assertTrue(afterReopen.getInt("scrollRequestCount") > requestsBeforeTouch)
            assertFalse(afterReopen.getBoolean("userOverride"))
        }
    }

    @Test
    fun testStudentExperienceShieldsAnalysisBeforeNativeSummary() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html><head></head><body>
              <h2>종합분석</h2>
              <section class="ant-alert-error">
                <h3>3번 문제</h3>
                <p id="sensitive-analysis">풀이와 정답</p>
              </section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => {
                  const shield = document.getElementById('matholic-kiosk-result-shield');
                  return JSON.stringify({
                    shieldPresent: !!shield,
                    shieldCoversViewport: !!shield &&
                      getComputedStyle(shield).position === 'fixed' &&
                      getComputedStyle(shield).zIndex === '2147483646',
                    sensitiveContentStillBehindShield:
                      !!document.getElementById('sensitive-analysis')
                  });
                })()
                """.trimIndent(),
            )
            assertTrue(proof.getBoolean("shieldPresent"))
            assertTrue(proof.getBoolean("shieldCoversViewport"))
            assertTrue(proof.getBoolean("sensitiveContentStillBehindShield"))
        }
    }

    @Test
    fun testStudentExperienceHydratesLazyResultCardsBehindShield() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html><head></head><body>
              <h2>종합분석</h2>
              <section class="ant-alert-error"><h3>1번 문제</h3></section>
              <section class="ant-alert-success"><h3>2번 문제</h3></section>
              <div style="height:2400px"></div>
              <script>
                window.addEventListener('scroll', () => {
                  if (window.scrollY < 300 || document.getElementById('lazy-three')) return;
                  const card = document.createElement('section');
                  card.id = 'lazy-three';
                  card.className = 'ant-alert-error';
                  card.innerHTML = '<h3>3번 문제</h3>';
                  document.body.appendChild(card);
                });
              </script>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            repeat(8) {
                assertTrue(
                    evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"),
                )
                Thread.sleep(100)
            }
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  shieldPresent: !!document.getElementById('matholic-kiosk-result-shield'),
                  lazyCardLoaded: !!document.getElementById('lazy-three'),
                  movedDown: (document.scrollingElement || document.documentElement).scrollTop > 0
                }))()
                """.trimIndent(),
            )
            assertTrue(proof.getBoolean("shieldPresent"))
            assertTrue(proof.getBoolean("lazyCardLoaded"))
            assertTrue(proof.getBoolean("movedDown"))
        }
    }

    @Test
    fun testStudentExperienceHydratesTwentyFiveCardsInInternalScrollContainer() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html><head></head><body>
              <div id="result-scroll" style="height:360px;overflow-y:auto">
                <h2>종합분석</h2>
                <table>
                  <thead><tr><th>문항수</th></tr></thead>
                  <tbody><tr><td>25</td></tr></tbody>
                </table>
                <section class="ant-alert-error"><h3>1번 문제</h3></section>
                <section class="ant-alert-error"><h3>2번 문제</h3></section>
                <section class="ant-alert-error"><h3>3번 문제</h3></section>
                <section class="ant-alert-error"><h3>4번 문제</h3></section>
                <div style="height:2800px"></div>
              </div>
              <script>
                let nextProblem = 5;
                document.getElementById('result-scroll').addEventListener('scroll', () => {
                  for (let count = 0; count < 4 && nextProblem <= 25; count += 1) {
                    const card = document.createElement('section');
                    card.className = 'ant-alert-error';
                    card.innerHTML = '<h3>' + nextProblem + '번 문제</h3>';
                    document.getElementById('result-scroll').appendChild(card);
                    nextProblem += 1;
                  }
                });
              </script>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            repeat(24) {
                assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
                Thread.sleep(400)
            }
            val summary = evaluate(webView, WebDomScripts.wrongAnswerSummary)
            assertTrue(summary.toString(), summary.getBoolean("ok"))
            assertEquals(25, summary.getInt("expectedProblems"))
            assertEquals(25, summary.getInt("classifiedCount"))
            assertEquals(25, summary.getJSONArray("wrongNumbers").length())
        }
    }

    @Test
    fun testStudentExperienceRejectsAmbiguousEncodedSpaPath() {
        withFixture(
            "https://im.matholic.com/learningV2/%252e%252e/result",
            """
            <!doctype html><html><body>
              <h2>종합분석</h2>
              <section class="ant-alert-error"><h3>3번 문제</h3></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val experience = evaluate(webView, WebDomScripts.applyStudentExperience)
            val summary = evaluate(webView, WebDomScripts.wrongAnswerSummary)
            assertFalse(experience.getBoolean("ok"))
            assertFalse(summary.getBoolean("ok"))
        }
    }

    @Test
    fun testStudentExperienceRejectsFragmentSelectedSpaView() {
        withFixture(
            "https://im.matholic.com/workbook#/course",
            """
            <!doctype html><html><body>
              <h2>학습지처럼 보이는 다른 SPA 화면</h2>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertFalse(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
        }
    }

    @Test
    fun testStudentExperienceAndSummaryRejectNonDefaultPortOrigin() {
        withFixture(
            "https://im.matholic.com:444/learningV2/result/virtual",
            """
            <!doctype html><html><body>
              <h2>종합분석</h2>
              <section class="ant-alert-error"><h3>3번 문제</h3></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertFalse(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            assertFalse(evaluate(webView, WebDomScripts.wrongAnswerSummary).getBoolean("ok"))
        }
    }

    @Test
    fun testStudentNavigationGuardRejectsFragmentOnlyRoute() {
        withFixture(
            "https://im.matholic.com/workbook",
            """
            <!doctype html><html><body>
              <a id="fragment-route" href="/workbook#/course">다른 SPA 화면</a>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => {
                  const event = new MouseEvent('click', { bubbles: true, cancelable: true });
                  document.getElementById('fragment-route').dispatchEvent(event);
                  return JSON.stringify({ prevented: event.defaultPrevented });
                })()
                """.trimIndent(),
            )
            assertTrue(proof.getBoolean("prevented"))
        }
    }

    @Test
    fun testStudentNavigationGuardRejectsCredentialedAndNonDefaultPortTargets() {
        withFixture(
            "https://im.matholic.com/workbook",
            """
            <!doctype html><html><body>
              <a id="credentialed" href="https://user:pass@im.matholic.com/workbook">자격 포함</a>
              <a id="non-default-port" href="https://im.matholic.com:444/workbook">변형 포트</a>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => {
                  const dispatch = id => {
                    const event = new MouseEvent('click', { bubbles: true, cancelable: true });
                    document.getElementById(id).dispatchEvent(event);
                    return event.defaultPrevented;
                  };
                  return JSON.stringify({
                    credentialedPrevented: dispatch('credentialed'),
                    nonDefaultPortPrevented: dispatch('non-default-port')
                  });
                })()
                """.trimIndent(),
            )
            assertTrue(proof.getBoolean("credentialedPrevented"))
            assertTrue(proof.getBoolean("nonDefaultPortPrevented"))
        }
    }

    @Test
    fun testStudentSectionNavigationUsesUniqueExactSpaLink() {
        withFixture(
            "https://im.matholic.com/workbook",
            """
            <!doctype html><html><body data-target="">
              <a href="/workbook">학습지</a>
              <a href="/diagnostic"
                 onclick="event.preventDefault();document.body.dataset.target='diagnostic'">
                진단평가
              </a>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(
                webView,
                WebDomScripts.navigateStudentSection(StudentWebPolicy.DIAGNOSTIC_PATH),
            )
            assertTrue(result.getBoolean("ok"))
            val proof = evaluate(
                webView,
                "(() => JSON.stringify({ target: document.body.dataset.target }))()",
            )
            assertEquals("diagnostic", proof.getString("target"))
        }
    }

    @Test
    fun testStudentSectionNavigationUsesTrustedPathWithResponsiveDuplicateLinks() {
        withFixture(
            "https://im.matholic.com/workbook",
            """
            <!doctype html><html><body>
              <a href="/diagnostic"
                 onclick="event.preventDefault();document.body.dataset.target='diagnostic-1'">
                진단평가 1
              </a>
              <a href="/diagnostic"
                 onclick="event.preventDefault();document.body.dataset.target='diagnostic-2'">
                진단평가 2
              </a>
              <a href="https://user:pass@im.matholic.com/workbook">변형 학습지</a>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertTrue(
                evaluate(
                    webView,
                    WebDomScripts.navigateStudentSection(StudentWebPolicy.DIAGNOSTIC_PATH),
                ).getBoolean("ok"),
            )
            assertEquals(
                "diagnostic-1",
                evaluate(
                    webView,
                    "(() => JSON.stringify({ target: document.body.dataset.target }))()",
                ).getString("target"),
            )
            assertFalse(
                evaluate(
                    webView,
                    WebDomScripts.navigateStudentSection(StudentWebPolicy.WORKBOOK_PATH),
                ).getBoolean("ok"),
            )
        }
    }

    @Test
    fun testWrongAnswerSummaryRejectsFragmentSelectedSpaView() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual#/analysis",
            """
            <!doctype html><html><body>
              <h2>종합분석</h2>
              <section class="ant-alert-error"><h3>3번 문제</h3></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertFalse(evaluate(webView, WebDomScripts.wrongAnswerSummary).getBoolean("ok"))
        }
    }

    @Test
    fun testWrongAnswerSummaryReturnsOnlyVerifiedProblemNumbers() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html data-matholic-kiosk-result-hydrated="true"><body>
              <h2>종합분석</h2>
              <table>
                <thead><tr><th>문항수</th></tr></thead>
                <tbody><tr><td>12</td></tr></tbody>
              </table>
              <section class="ant-alert-success"><h3>1번 문제</h3></section>
              <section class="ant-alert-success"><h3>2번 문제</h3></section>
              <section class="ant-alert-error"><h3>3번 문제</h3></section>
              <section class="ant-alert-success"><h3>4번 문제</h3></section>
              <section class="ant-alert-success"><h3>5번 문제</h3></section>
              <section class="ant-alert-success"><h3>6번 문제</h3></section>
              <section class="ant-alert-success"><h3>7번 문제</h3></section>
              <section class="ant-alert-success"><h3>8번 문제</h3></section>
              <section class="ant-alert-success"><h3>9번 문제</h3></section>
              <section class="ant-alert-success"><h3>10번 문제</h3></section>
              <section class="ant-alert-success"><h3>11번 문제</h3></section>
              <section class="ant-alert-error"><h3>12번 문제</h3></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.wrongAnswerSummary)
            assertTrue(result.getBoolean("ok"))
            assertEquals(2, result.getInt("errorCardCount"))
            assertEquals(12, result.getInt("totalProblems"))
            assertEquals(12, result.getInt("expectedProblems"))
            assertEquals(3, result.getJSONArray("wrongNumbers").getInt(0))
            assertEquals(12, result.getJSONArray("wrongNumbers").getInt(1))
        }
    }

    @Test
    fun testWrongAnswerSummaryRejectsContiguousSubsetWhenScoreboardExpectsMore() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html data-matholic-kiosk-result-hydrated="true"><body>
              <h2>종합분석</h2>
              <table>
                <thead><tr><th>문항수</th></tr></thead>
                <tbody><tr><td>25</td></tr></tbody>
              </table>
              <section class="ant-alert-error"><h3>1번 문제</h3></section>
              <section class="ant-alert-error"><h3>2번 문제</h3></section>
              <section class="ant-alert-error"><h3>3번 문제</h3></section>
              <section class="ant-alert-error"><h3>4번 문제</h3></section>
              <section class="ant-alert-error"><h3>5번 문제</h3></section>
              <section class="ant-alert-error"><h3>6번 문제</h3></section>
              <section class="ant-alert-error"><h3>7번 문제</h3></section>
              <section class="ant-alert-error"><h3>8번 문제</h3></section>
              <section class="ant-alert-error"><h3>9번 문제</h3></section>
              <section class="ant-alert-error"><h3>10번 문제</h3></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.wrongAnswerSummary)
            assertFalse(result.getBoolean("ok"))
            assertEquals(25, result.getInt("expectedProblems"))
            assertEquals(10, result.getInt("classifiedCount"))
        }
    }

    @Test
    fun testWrongAnswerSummaryFailsClosedForOnlyCurrentlyRenderedProblem() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html><body>
              <h2>종합분석</h2>
              <section class="ant-alert-error"><h3>5번 문제</h3></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.wrongAnswerSummary)
            assertFalse(result.getBoolean("ok"))
            assertTrue(result.getBoolean("analysisFound"))
            assertEquals("INCOMPLETE_RESULT", result.getString("reason"))
            assertEquals(5, result.getInt("totalProblems"))
        }
    }

    @Test
    fun testWrongAnswerSummaryUsesHiddenCardsButRequiresCompleteSequence() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html data-matholic-kiosk-result-hydrated="true"><body>
              <h2>종합분석</h2>
              <section class="ant-alert-error"><h3>1번 문제</h3></section>
              <section class="ant-alert-error" style="display:none"><h3>2번 문제</h3></section>
              <section class="ant-alert-success" style="display:none"><h3>3번 문제</h3></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.wrongAnswerSummary)
            assertTrue(result.getBoolean("ok"))
            assertEquals(3, result.getInt("totalProblems"))
            assertEquals(1, result.getInt("visibleCardCount"))
            assertEquals(2, result.getJSONArray("wrongNumbers").length())
        }
    }

    @Test
    fun testWrongAnswerSummaryIncludesUnknownAndExcludedProblemsInCompletenessProof() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html data-matholic-kiosk-result-hydrated="true"><body>
              <h2>종합분석</h2>
              <section class="ant-alert-success"><h3>1번 문제</h3></section>
              <section><div><h3>2번 문제</h3><span>모름</span></div></section>
              <section><div><h3>3번 문제</h3><span>제외</span></div></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.wrongAnswerSummary)
            assertTrue(result.getBoolean("ok"))
            assertEquals(3, result.getInt("totalProblems"))
            assertEquals(1, result.getJSONArray("wrongNumbers").length())
            assertEquals(2, result.getJSONArray("wrongNumbers").getInt(0))
        }
    }

    @Test
    fun testWrongAnswerSummaryReportsNoWrongProblemsWhenAllCardsSucceed() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html data-matholic-kiosk-result-hydrated="true"><body>
              <h2>종합분석</h2>
              <section class="ant-alert-success"><h3>1번 문제</h3></section>
              <section class="ant-alert-success"><h3>2번 문제</h3></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.wrongAnswerSummary)
            assertTrue(result.getBoolean("ok"))
            assertEquals(0, result.getInt("errorCardCount"))
            assertEquals(2, result.getInt("successCardCount"))
            assertEquals(0, result.getJSONArray("wrongNumbers").length())
        }
    }

    @Test
    fun testWrongAnswerSummaryFailsClosedWhenErrorNumberIsMissing() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html><body>
              <h2>종합분석</h2>
              <section class="ant-alert-error"><h3>오답</h3><p>풀이와 정답</p></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertFalse(evaluate(webView, WebDomScripts.wrongAnswerSummary).getBoolean("ok"))
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun withFixture(baseUrl: String, html: String, block: (WebView) -> Unit) {
        val loaded = CountDownLatch(1)
        val reference = AtomicReference<WebView>()
        instrumentation.runOnMainSync {
            val webView = WebView(instrumentation.targetContext)
            webView.settings.javaScriptEnabled = true
            webView.layout(0, 0, 1_200, 800)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    loaded.countDown()
                }
            }
            reference.set(webView)
            webView.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)
        }
        assertTrue("fixture did not load", loaded.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))
        try {
            block(reference.get())
        } finally {
            instrumentation.runOnMainSync { reference.get().destroy() }
        }
    }

    private fun evaluate(webView: WebView, script: String): JSONObject {
        val latch = CountDownLatch(1)
        val raw = AtomicReference<String>()
        instrumentation.runOnMainSync {
            webView.evaluateJavascript(script) {
                raw.set(it)
                latch.countDown()
            }
        }
        assertTrue("JavaScript result timed out", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))
        val outer = JSONTokener(raw.get()).nextValue()
        return when (outer) {
            is JSONObject -> outer
            is String -> JSONObject(outer)
            else -> throw AssertionError("unexpected JavaScript result")
        }
    }

    private fun loginFixture(action: String, preventSubmit: Boolean = false): String {
        val submitHandler = if (preventSubmit) {
            "onsubmit=\"event.preventDefault();document.body.dataset.submitted='yes'\""
        } else {
            ""
        }
        return """
            <!doctype html><html><body data-submitted="no">
            <form action="$action" method="post" $submitHandler>
              <input name="username" value="residual-user">
              <input name="password" type="password" value="residual-pass">
              <label><input type="checkbox" checked>아이디 저장</label>
              <button type="submit">로그인</button>
            </form>
            </body></html>
        """.trimIndent()
    }

    private fun portalFixture(
        extraLogout: String = "",
        includeDefaultLogout: Boolean = true,
    ): String {
        val defaultLogout = if (includeDefaultLogout) {
            "<div id=\"logout\" onclick=\"document.body.dataset.loggedOut='yes'\">로그아웃</div>"
        } else {
            ""
        }
        return """
        <!doctype html><html><body data-logged-out="no">
          <a href="/course">학습실</a>
          <div id="account">
            <div id="trigger" onclick="document.getElementById('submenu').style.visibility='visible'">
              <span>가상학생</span><svg></svg>
            </div>
            <div id="submenu" style="visibility:hidden">
              <div><a href="/userInfo">개인정보</a></div>
              <div><a href="/userAccessLog">로그인정보</a></div>
              $defaultLogout
              $extraLogout
            </div>
          </div>
        </body></html>
        """.trimIndent()
    }

    private companion object {
        const val TIMEOUT_SECONDS = 10L
    }
}
