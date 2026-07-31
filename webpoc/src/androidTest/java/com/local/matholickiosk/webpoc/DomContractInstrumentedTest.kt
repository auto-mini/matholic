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
    fun testStudentExperienceMakesObjectiveChoicesLargeAndWrapSafe() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main style="width:220px">
                <div class="ant-radio-group" id="choices">
                  <label class="ant-radio-button-wrapper" tabindex="0">1</label>
                  <label class="ant-radio-button-wrapper">2</label>
                  <label class="ant-radio-button-wrapper">3</label>
                  <label class="ant-radio-button-wrapper">4</label>
                  <label class="ant-radio-button-wrapper">5</label>
                </div>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => {
                  const group = document.getElementById('choices');
                  const choice = group.firstElementChild;
                  const groupStyle = getComputedStyle(group);
                  const choiceStyle = getComputedStyle(choice);
                  choice.classList.add('ant-radio-button-wrapper-checked');
                  const selectedStyle = getComputedStyle(choice);
                  const selectedBackground = selectedStyle.backgroundColor;
                  const selectedColor = selectedStyle.color;
                  choice.classList.remove('ant-radio-button-wrapper-checked');
                  choice.focus();
                  const clearedStyle = getComputedStyle(choice);
                  return JSON.stringify({
                    display: groupStyle.display,
                    wrap: groupStyle.flexWrap,
                    groupWidth: group.getBoundingClientRect().width,
                    groupHeight: group.getBoundingClientRect().height,
                    choiceWidth: choice.getBoundingClientRect().width,
                    choiceHeight: choice.getBoundingClientRect().height,
                    radius: choiceStyle.borderRadius,
                    selectedBackground,
                    selectedColor,
                    clearedBackground: clearedStyle.backgroundColor,
                    clearedColor: clearedStyle.color,
                    separatorHidden:
                      getComputedStyle(choice, '::before').display === 'none'
                  });
                })()
                """.trimIndent(),
            )
            assertEquals("flex", proof.getString("display"))
            assertEquals("wrap", proof.getString("wrap"))
            assertTrue(proof.getDouble("choiceWidth") >= 64.0)
            assertTrue(proof.getDouble("choiceHeight") >= 56.0)
            assertTrue(proof.getDouble("groupWidth") <= 220.0)
            assertTrue(proof.getDouble("groupHeight") > proof.getDouble("choiceHeight"))
            assertEquals("12px", proof.getString("radius"))
            assertEquals("rgb(21, 101, 192)", proof.getString("selectedBackground"))
            assertEquals("rgb(255, 255, 255)", proof.getString("selectedColor"))
            assertEquals("rgb(255, 255, 255)", proof.getString("clearedBackground"))
            assertEquals("rgb(16, 42, 67)", proof.getString("clearedColor"))
            assertTrue(proof.getBoolean("separatorHidden"))
        }
    }

    @Test
    fun testStudentExperienceMovesProblemSelectorAndEnlargesUnlabelledIconButtons() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div id="problem-navigation" style="display:flex">
                  <button id="previous"><svg><path d="M 8 4 L 2 8"></path></svg></button>
                  <div id="problem-number">
                    <div class="ant-select">
                      <div id="problem-selector" class="ant-select-selector"
                           role="combobox"
                           onclick="document.body.dataset.selectorOpened='yes'">
                        <span class="ant-select-selection-item">4</span>
                      </div>
                    </div>
                    <span>/ 10</span>
                  </div>
                  <button id="next"><svg><path d="M 2 4 L 8 8"></path></svg></button>
                </div>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            assertEquals(3, result.getInt("problemNavigationEnhancements"))
            val proof = evaluate(
                webView,
                """
                (() => {
                  document.getElementById('problem-number').click();
                  const previous = document.getElementById('previous');
                  const next = document.getElementById('next');
                  const number = document.getElementById('problem-number');
                  const numberStyle = getComputedStyle(number);
                  return JSON.stringify({
                    previousWidth: previous.style.width,
                    previousHeight: previous.style.height,
                    nextWidth: next.style.width,
                    nextHeight: next.style.height,
                    previousLabel: previous.getAttribute('aria-label'),
                    nextLabel: next.getAttribute('aria-label'),
                    numberClass:
                      number.classList.contains('matholic-kiosk-problem-number'),
                    numberPosition: numberStyle.position,
                    numberTop: numberStyle.top,
                    numberDisplayLabel:
                      number.dataset.matholicKioskLabel,
                    numberLabel: number.getAttribute('aria-label'),
                    mapOpen: document.querySelector(
                      '.matholic-kiosk-problem-map'
                    )?.dataset.open,
                    selectorOpened: document.body.dataset.selectorOpened === 'yes',
                    noHorizontalOverflow:
                      document.documentElement.scrollWidth <=
                        document.documentElement.clientWidth
                  });
                })()
                """.trimIndent(),
            )
            assertEquals("52px", proof.getString("previousWidth"))
            assertEquals("58px", proof.getString("previousHeight"))
            assertEquals("52px", proof.getString("nextWidth"))
            assertEquals("58px", proof.getString("nextHeight"))
            assertEquals("이전 문제", proof.getString("previousLabel"))
            assertEquals("다음 문제", proof.getString("nextLabel"))
            assertTrue(proof.getBoolean("numberClass"))
            assertEquals("relative", proof.getString("numberPosition"))
            assertEquals("0px", proof.getString("numberTop"))
            assertEquals("4 ↓/10", proof.getString("numberDisplayLabel"))
            assertEquals("문제 목록 열기", proof.getString("numberLabel"))
            assertEquals("true", proof.getString("mapOpen"))
            assertFalse(proof.getBoolean("selectorOpened"))
            assertTrue(proof.getBoolean("noHorizontalOverflow"))

            val moving = evaluate(
                webView,
                """
                (() => {
                  document.getElementById('next').click();
                  return JSON.stringify({
                    label: document.getElementById('problem-number')
                      .dataset.matholicKioskLabel,
                    busy: document.getElementById('problem-number')
                      .getAttribute('aria-busy'),
                    target: window.__matholicKioskDirectionFeedback?.target
                  });
                })()
                """.trimIndent(),
            )
            assertEquals("4 →5/10", moving.getString("label"))
            assertEquals("true", moving.getString("busy"))
            assertEquals(5, moving.getInt("target"))

            evaluate(webView, WebDomScripts.applyStudentExperience)
            val maintained = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  label: document.getElementById('problem-number')
                    .dataset.matholicKioskLabel
                }))()
                """.trimIndent(),
            )
            assertEquals("4 →5/10", maintained.getString("label"))

            evaluate(
                webView,
                """
                (() => {
                  document.querySelector('.ant-select-selection-item')
                    .textContent = '5';
                  return JSON.stringify({ ok: true });
                })()
                """.trimIndent(),
            )
            evaluate(webView, WebDomScripts.applyStudentExperience)
            val settled = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  label: document.getElementById('problem-number')
                    .dataset.matholicKioskLabel,
                  busy: document.getElementById('problem-number')
                    .hasAttribute('aria-busy'),
                  feedbackPresent: !!window.__matholicKioskDirectionFeedback
                }))()
                """.trimIndent(),
            )
            assertEquals("5 ↓/10", settled.getString("label"))
            assertFalse(settled.getBoolean("busy"))
            assertFalse(settled.getBoolean("feedbackPresent"))
        }
    }

    @Test
    fun testStudentExperienceLocalizesEmptyWorkbookAndDiagnosticLists() {
        listOf(
            "https://im.matholic.com/workbook" to "학습지가 없습니다",
            "https://im.matholic.com/diagnostic" to "진단평가가 없습니다",
        ).forEach { (url, expected) ->
            withFixture(
                url,
                """
                <!doctype html><html><body>
                  <main>
                    <div class="ant-empty">
                      <div class="ant-empty-description">No data</div>
                    </div>
                  </main>
                </body></html>
                """.trimIndent(),
            ) { webView ->
                val result = evaluate(webView, WebDomScripts.applyStudentExperience)
                assertTrue(result.getBoolean("ok"))
                assertEquals(1, result.getInt("emptyStateLocalizations"))
                val proof = evaluate(
                    webView,
                    """
                    (() => JSON.stringify({
                      text: document.querySelector('.ant-empty-description')
                        .textContent.trim()
                    }))()
                    """.trimIndent(),
                )
                assertEquals(expected, proof.getString("text"))
            }
        }
    }

    @Test
    fun testStudentExperienceHidesDirectionsForSingleProblem() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div id="problem-navigation" style="display:flex">
                  <button id="previous"><svg></svg></button>
                  <div id="problem-number">
                    <div class="ant-select">
                      <div class="ant-select-selector" role="combobox">
                        <span class="ant-select-selection-item">1</span>
                      </div>
                    </div>
                    <span>/ 1</span>
                  </div>
                  <button id="next"><svg></svg></button>
                </div>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  label: document.getElementById('problem-number')
                    .dataset.matholicKioskLabel,
                  singleProblem: document.getElementById('problem-navigation')
                    .dataset.matholicKioskSingleProblem,
                  previousVisibility: getComputedStyle(
                    document.getElementById('previous')
                  ).visibility,
                  nextVisibility: getComputedStyle(
                    document.getElementById('next')
                  ).visibility,
                  mapPresent: !!document.querySelector(
                    '.matholic-kiosk-problem-map'
                  )
                }))()
                """.trimIndent(),
            )
            assertEquals("1/1", proof.getString("label"))
            assertEquals("true", proof.getString("singleProblem"))
            assertEquals("hidden", proof.getString("previousVisibility"))
            assertEquals("hidden", proof.getString("nextVisibility"))
            assertFalse(proof.getBoolean("mapPresent"))
        }
    }

    @Test
    fun testProblemMapUsesNativeDirectionButtonsWhenAntSelectorIgnoresClick() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div id="problem-navigation" style="display:flex;width:330px">
                  <button id="previous" aria-label="이전 문제"
                    onclick="window.moveProblem(-1)">&lt;</button>
                  <div id="problem-number">
                    <div class="ant-select">
                      <div class="ant-select-selector" role="combobox">
                        <span id="selected-problem"
                          class="ant-select-selection-item">1</span>
                      </div>
                    </div>
                    <span>/ 5</span>
                  </div>
                  <button id="next" aria-label="다음 문제"
                    onclick="window.moveProblem(1)">&gt;</button>
                </div>
                <div id="answer-input-form-1"><input value="7"></div>
                <script>
                  window.problemNumber = 1;
                  window.directionClicks = 0;
                  window.problemMovePending = false;
                  window.moveProblem = function(delta) {
                    if (window.problemMovePending) return;
                    window.problemMovePending = true;
                    window.directionClicks += 1;
                    setTimeout(function() {
                      window.problemNumber = Math.max(
                        1,
                        Math.min(5, window.problemNumber + delta)
                      );
                      document.getElementById('selected-problem').textContent =
                        String(window.problemNumber);
                      window.problemMovePending = false;
                    }, 600);
                  };
                </script>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            val started = evaluate(
                webView,
                """
                (() => {
                  const map = document.querySelector('.matholic-kiosk-problem-map');
                  map.firstElementChild.click();
                  map.querySelectorAll(
                    '.matholic-kiosk-problem-map-grid button'
                  )[3].click();
                  return JSON.stringify({
                    started: true,
                    pendingLabel: document.getElementById('problem-number')
                      .dataset.matholicKioskLabel,
                    numberBusy: document.getElementById('problem-number')
                      .getAttribute('aria-busy')
                  });
                })()
                """.trimIndent(),
            )
            assertEquals("1 →4/5", started.getString("pendingLabel"))
            assertEquals("true", started.getString("numberBusy"))
            Thread.sleep(3_000)
            val proof = evaluate(
                webView,
                """
                (() => {
                  const navigation = document.getElementById('problem-navigation');
                  const previous = document.getElementById('previous');
                  const next = document.getElementById('next');
                  const navigationRect = navigation.getBoundingClientRect();
                  const previousRect = previous.getBoundingClientRect();
                  const nextRect = next.getBoundingClientRect();
                  return JSON.stringify({
                    selected: document.getElementById('selected-problem').textContent,
                    directionClicks: window.directionClicks,
                    pendingTarget:
                      window.__matholicKioskProblemNavigationController.target,
                    finalLabel: document.getElementById('problem-number')
                      .dataset.matholicKioskLabel,
                    numberBusy: document.getElementById('problem-number')
                      .hasAttribute('aria-busy'),
                    previousVisible:
                      previousRect.left >= navigationRect.left &&
                      previousRect.right <= navigationRect.right,
                    nextVisible:
                      nextRect.left >= navigationRect.left &&
                      nextRect.right <= navigationRect.right,
                    noHorizontalOverflow:
                      document.documentElement.scrollWidth <=
                        document.documentElement.clientWidth
                  });
                })()
                """.trimIndent(),
            )
            assertEquals("4", proof.getString("selected"))
            assertEquals(3, proof.getInt("directionClicks"))
            assertEquals(0, proof.getInt("pendingTarget"))
            assertEquals("4 ↓/5", proof.getString("finalLabel"))
            assertFalse(proof.getBoolean("numberBusy"))
            assertTrue(proof.getBoolean("previousVisible"))
            assertTrue(proof.getBoolean("nextVisible"))
            assertTrue(proof.getBoolean("noHorizontalOverflow"))
        }
    }

    @Test
    fun testProblemMapUsesAntSelectorForDirectJump() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div id="problem-navigation" style="display:flex;width:330px">
                  <button id="previous" aria-label="이전 문제"
                    onclick="window.directionClicks += 1">&lt;</button>
                  <div id="problem-number">
                    <div class="ant-select">
                      <div id="problem-selector" class="ant-select-selector"
                           role="combobox">
                        <span id="selected-problem"
                          class="ant-select-selection-item">1</span>
                      </div>
                    </div>
                    <span>/ 5</span>
                  </div>
                  <button id="next" aria-label="다음 문제"
                    onclick="window.directionClicks += 1">&gt;</button>
                </div>
                <div id="answer-input-form-1"><input value="7"></div>
                <script>
                  window.directionClicks = 0;
                  window.directSelections = 0;
                  window.hiddenSelections = 0;
                  const selector = document.getElementById('problem-selector');
                  selector['__reactProps${'$'}fixture'] = {
                    onMouseDown: function() {
                      if (document.querySelector('.rc-virtual-list-holder')) return;
                      const hiddenOption = document.createElement('div');
                      hiddenOption.setAttribute('role', 'option');
                      hiddenOption.style.width = '0';
                      hiddenOption.style.height = '30px';
                      hiddenOption.textContent = '5';
                      hiddenOption.onclick = function() {
                        window.hiddenSelections += 1;
                      };
                      document.body.appendChild(hiddenOption);
                      const holder = document.createElement('div');
                      holder.className = 'rc-virtual-list-holder';
                      holder.style.width = '120px';
                      holder.style.height = '200px';
                      for (let number = 1; number <= 5; number += 1) {
                        const option = document.createElement('div');
                        option.className = 'ant-select-item-option';
                        option.style.width = '100px';
                        option.style.height = '30px';
                        option.textContent = String(number);
                        option.onclick = function() {
                          document.getElementById('selected-problem').textContent =
                            String(number);
                          window.directSelections += 1;
                          holder.remove();
                        };
                        holder.appendChild(option);
                      }
                      document.body.appendChild(holder);
                    }
                  };
                </script>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            evaluate(
                webView,
                """
                (() => {
                  document.querySelectorAll(
                    '.matholic-kiosk-problem-map-grid button'
                  )[4].click();
                  return JSON.stringify({ started: true });
                })()
                """.trimIndent(),
            )
            Thread.sleep(1_000)
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  selected: document.getElementById('selected-problem').textContent,
                  directSelections: window.directSelections,
                  hiddenSelections: window.hiddenSelections,
                  directionClicks: window.directionClicks,
                  pendingTarget:
                    window.__matholicKioskProblemNavigationController.target
                }))()
                """.trimIndent(),
            )
            assertEquals("5", proof.getString("selected"))
            assertEquals(1, proof.getInt("directSelections"))
            assertEquals(0, proof.getInt("hiddenSelections"))
            assertEquals(0, proof.getInt("directionClicks"))
            assertEquals(0, proof.getInt("pendingTarget"))
        }
    }

    @Test
    fun testStudentExperienceShowsWorkingInFlowProblemStateMap() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div id="problem-navigation" style="display:flex">
                  <button aria-label="이전 문제">&lt;</button>
                  <div id="problem-number">
                    <select id="problem-selector">
                      <option>1</option>
                      <option selected>2</option>
                      <option>3</option>
                    </select>
                    <span>/ 3</span>
                  </div>
                  <button aria-label="다음 문제">&gt;</button>
                </div>
                <div id="answer-input-form-2">
                  <input id="subjective-answer" value="7">
                  <button id="unknown">모름</button>
                </div>
                <button id="submit">답안제출</button>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            assertEquals(1, result.getInt("problemStateMapEnhancements"))
            val proof = evaluate(
                webView,
                """
                (() => {
                  const map = document.querySelector(
                    '.matholic-kiosk-problem-map'
                  );
                  const toggle = map.firstElementChild;
                  toggle.click();
                  const buttons = Array.from(map.querySelectorAll(
                    '.matholic-kiosk-problem-map-grid button'
                  ));
                  const before = buttons[1].dataset.state;
                  document.getElementById('unknown').click();
                  const unansweredButtons = Array.from(map.querySelectorAll(
                    '.matholic-kiosk-problem-map-unanswered button'
                  ));
                  unansweredButtons[1].click();
                  return JSON.stringify({
                    mapCount: document.querySelectorAll(
                      '.matholic-kiosk-problem-map'
                    ).length,
                    buttonCount: buttons.length,
                    current: buttons[1].dataset.current,
                    before,
                    trackedUnknown:
                      window.__matholicKioskProblemStates[2],
                    unansweredButtonCount: unansweredButtons.length,
                    selectedAfterNextUnanswered:
                      document.getElementById('problem-selector').value,
                    guidePresent: !!document.querySelector(
                      '.matholic-kiosk-answer-guide'
                    ),
                    mapPosition: getComputedStyle(map).position,
                    mapFollowsNavigation:
                      document.getElementById('problem-navigation')
                        .nextElementSibling === map,
                    numberLabel: document.getElementById('problem-number')
                      .dataset.matholicKioskLabel,
                    legendItemCount: map.querySelectorAll(
                      '.matholic-kiosk-problem-map-legend > span'
                    ).length,
                    open: map.dataset.open
                  });
                })()
                """.trimIndent(),
            )
            assertEquals(1, proof.getInt("mapCount"))
            assertEquals(3, proof.getInt("buttonCount"))
            assertEquals("true", proof.getString("current"))
            assertEquals("answered", proof.getString("before"))
            assertEquals("unknown", proof.getString("trackedUnknown"))
            assertEquals(2, proof.getInt("unansweredButtonCount"))
            assertEquals("3", proof.getString("selectedAfterNextUnanswered"))
            assertFalse(proof.getBoolean("guidePresent"))
            assertEquals("relative", proof.getString("mapPosition"))
            assertTrue(proof.getBoolean("mapFollowsNavigation"))
            assertEquals("2 ↓/3", proof.getString("numberLabel"))
            assertEquals(3, proof.getInt("legendItemCount"))
            assertEquals("false", proof.getString("open"))

            evaluate(
                webView,
                """
                (() => {
                  document.getElementById('problem-navigation').outerHTML = `
                    <div id="problem-navigation-new" style="display:flex">
                      <button aria-label="이전 문제">&lt;</button>
                      <div id="problem-number-new">
                        <select id="problem-selector-new">
                          <option selected>1</option>
                          <option>2</option>
                          <option>3</option>
                        </select>
                        <span>/ 3</span>
                      </div>
                      <button aria-label="다음 문제">&gt;</button>
                    </div>`;
                  return JSON.stringify({ replaced: true });
                })()
                """.trimIndent(),
            )
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val rerendered = evaluate(
                webView,
                """
                (() => {
                  const map = document.querySelector(
                    '.matholic-kiosk-problem-map'
                  );
                  map.querySelectorAll(
                    '.matholic-kiosk-problem-map-grid button'
                  )[2].click();
                  return JSON.stringify({
                    selected:
                      document.getElementById('problem-selector-new').value,
                    mapCount: document.querySelectorAll(
                      '.matholic-kiosk-problem-map'
                    ).length,
                    followsCurrentNavigation:
                      document.getElementById('problem-navigation-new')
                        .nextElementSibling === map
                  });
                })()
                """.trimIndent(),
            )
            assertEquals("3", rerendered.getString("selected"))
            assertEquals(1, rerendered.getInt("mapCount"))
            assertTrue(rerendered.getBoolean("followsCurrentNavigation"))
        }
    }

    @Test
    fun testStudentExperienceDoesNotRevealLoadingLearningShell() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div role="progressbar">로딩 중</div>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            assertTrue(result.getBoolean("learningPage"))
            assertFalse(result.getBoolean("contentReady"))
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
            assertTrue(result.getBoolean("contentReady"))
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
                  basicVisible: getComputedStyle(document.getElementById('basic')).display !== 'none',
                  fractionVisible:
                    getComputedStyle(document.getElementById('fraction')).display !== 'none',
                  mathClicked: document.getElementById('math').dataset.clicked === 'yes',
                  inputMenuVisible:
                    getComputedStyle(document.getElementById('input-menu')).display !== 'none',
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
            assertTrue(proof.getBoolean("basicVisible"))
            assertTrue(proof.getBoolean("fractionVisible"))
            assertTrue(proof.getBoolean("mathClicked"))
            assertTrue(proof.getBoolean("inputMenuVisible"))
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
                    <div id="late-math-toolbar">
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
                  inputMenuVisible:
                    getComputedStyle(document.getElementById('late-input-menu')).display !== 'none',
                  inputMenuPrehidden:
                    getComputedStyle(document.getElementById('late-input-menu')).visibility === 'hidden',
                  toolbarPrehidden: Array.from(
                    document.querySelectorAll('#late-math-toolbar button')
                  ).every(button => getComputedStyle(button).visibility === 'hidden'),
                  mathClicked:
                    document.getElementById('late-math').dataset.clicked === 'yes',
                  basicVisible:
                    getComputedStyle(document.getElementById('late-basic')).display !== 'none',
                  fractionVisible:
                    getComputedStyle(document.getElementById('late-fraction')).display !== 'none'
                }))()
                """.trimIndent(),
            )
            assertTrue(lateProof.getBoolean("hidden"))
            assertTrue(lateProof.getBoolean("reportHidden"))
            assertTrue(lateProof.getBoolean("mathTooltipHidden"))
            assertTrue(lateProof.getBoolean("chromeHidden"))
            assertTrue(lateProof.getBoolean("inputMenuVisible"))
            assertTrue(lateProof.getBoolean("inputMenuPrehidden"))
            assertTrue(lateProof.getBoolean("toolbarPrehidden"))
            assertTrue(lateProof.getBoolean("mathClicked"))
            assertTrue(lateProof.getBoolean("basicVisible"))
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
    fun testStudentExperienceKeepsAnswerInputInteractiveWhileMathFieldSettles() {
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
                  inputEnabled:
                    getComputedStyle(document.getElementById('answer-input-form-0'))
                      .pointerEvents !== 'none',
                  notMarkedBusy:
                    document.getElementById('answer-input-form-0')
                      .getAttribute('aria-busy') !== 'true'
                }))()
                """.trimIndent(),
            )
            assertTrue(initialProof.getBoolean("toolbarMounted"))
            assertTrue(initialProof.getBoolean("fieldNotReady"))
            assertTrue(initialProof.getBoolean("inputEnabled"))
            assertTrue(initialProof.getBoolean("notMarkedBusy"))

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
    fun testStudentExperienceRestoresRc47BlockedScopeAndKeepsMathControlsTouchable() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div id="answer-input-form-0"
                  data-matholic-kiosk-math-pending="true"
                  data-matholic-kiosk-previous-pointer-events=""
                  data-matholic-kiosk-previous-pointer-priority=""
                  data-matholic-kiosk-previous-aria-busy="__missing__"
                  aria-busy="true"
                  style="pointer-events:none!important">
                  <span id="math-editor" class="mq-editable-field mq-math-mode">
                    <span class="mq-textarea"><textarea></textarea></span>
                    <span class="mq-root-block"></span>
                  </span>
                  <button id="root" onclick="this.dataset.clicked='yes'">루트</button>
                  <button id="fraction" onclick="this.dataset.clicked='yes'">분수</button>
                  <button id="pi" onclick="this.dataset.clicked='yes'">파이</button>
                  <button id="input-menu">입력기</button>
                </div>
                <script>
                  window.rc47Commands = [];
                  const editor = document.getElementById('math-editor');
                  let latex = '';
                  editor.fieldApi = {
                    latex: value => {
                      if (value !== undefined) latex = value;
                      return latex;
                    },
                    write: value => { latex += value; },
                    cmd: value => {
                      window.rc47Commands.push(value);
                      latex += value;
                    },
                    keystroke: key => {
                      if (key === 'Backspace') latex = latex.slice(0, -1);
                    },
                    focus: () => {}
                  };
                  window.MathQuill = {
                    getInterface: () => element => element.fieldApi || null
                  };
                </script>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => {
                  const editor = document.getElementById('math-editor');
                  editor.dispatchEvent(
                    new Event('pointerdown', { bubbles: true })
                  );
                  const structureControls = Array.from(
                    document.querySelectorAll(
                      '.matholic-kiosk-keypad-structure-grid button'
                    )
                  );
                  structureControls.forEach(control => control.click());
                  const scope = document.getElementById('answer-input-form-0');
                  const editorStyle = getComputedStyle(editor);
                  const originalControls = ['root', 'fraction', 'pi'].map(id => {
                    const control = document.getElementById(id);
                    const style = getComputedStyle(control);
                    return {
                      display: style.display,
                      clicked: control.dataset.clicked === 'yes'
                    };
                  });
                  return JSON.stringify({
                    scopePointerEvents: getComputedStyle(scope).pointerEvents,
                    busyRemoved: !scope.hasAttribute('aria-busy'),
                    pendingMarkerRemoved:
                      !scope.hasAttribute('data-matholic-kiosk-math-pending'),
                    editorMinHeight: parseFloat(editorStyle.minHeight),
                    editorPointerEvents: editorStyle.pointerEvents,
                    originalControls,
                    structureControlCount: structureControls.length,
                    structureControlsTouchable: structureControls.every(control =>
                      getComputedStyle(control).pointerEvents !== 'none'
                    ),
                    commands: window.rc47Commands,
                    navigationCount:
                      document.querySelectorAll('.matholic-kiosk-math-nav').length
                  });
                })()
                """.trimIndent(),
            )
            assertFalse(proof.getString("scopePointerEvents") == "none")
            assertTrue(proof.getBoolean("busyRemoved"))
            assertTrue(proof.getBoolean("pendingMarkerRemoved"))
            assertTrue(proof.getDouble("editorMinHeight") >= 56.0)
            assertEquals("auto", proof.getString("editorPointerEvents"))
            val originalControls = proof.getJSONArray("originalControls")
            repeat(originalControls.length()) { index ->
                val control = originalControls.getJSONObject(index)
                assertEquals("none", control.getString("display"))
                assertFalse(control.getBoolean("clicked"))
            }
            assertEquals(3, proof.getInt("structureControlCount"))
            assertTrue(proof.getBoolean("structureControlsTouchable"))
            assertEquals("\\sqrt", proof.getJSONArray("commands").getString(0))
            assertEquals("\\frac", proof.getJSONArray("commands").getString(1))
            assertEquals("\\pi", proof.getJSONArray("commands").getString(2))
            assertEquals(1, proof.getInt("navigationCount"))
        }
    }

    @Test
    fun testStudentExperienceExpandsLazySubjectiveEditorInsideAnswerReview() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div class="ant-modal" role="dialog">
                <div class="ant-modal-title">전체답안</div>
                <div id="answer-input-form-review"
                  style="height:8px;min-height:0;overflow:hidden">
                  <span id="lazy-review-editor" class="mq-math-mode"
                    style="display:inline;height:4px;min-height:0;width:12px"
                    onclick="this.dataset.clicked='yes'">
                    <span class="mq-textarea"><textarea></textarea></span>
                    <span class="mq-root-block"></span>
                  </span>
                </div>
                <div id="answer-input-form-hidden" style="display:none">
                  <span id="hidden-review-editor"
                    class="mq-editable-field mq-math-mode">
                    <span class="mq-textarea"><textarea></textarea></span>
                    <span class="mq-root-block"></span>
                  </span>
                </div>
              </div>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            assertTrue(result.getInt("subjectiveTouchTargets") >= 1)
            val proof = evaluate(
                webView,
                """
                (() => {
                  const scope = document.getElementById(
                    'answer-input-form-review'
                  );
                  const editor = document.getElementById('lazy-review-editor');
                  const hiddenEditor = document.getElementById(
                    'hidden-review-editor'
                  );
                  editor.click();
                  const editorStyle = getComputedStyle(editor);
                  const scopeStyle = getComputedStyle(scope);
                  return JSON.stringify({
                    display: editorStyle.display,
                    minWidth: parseFloat(editorStyle.minWidth),
                    minHeight: parseFloat(editorStyle.minHeight),
                    actualWidth: editor.getBoundingClientRect().width,
                    actualHeight: editor.getBoundingClientRect().height,
                    pointerEvents: editorStyle.pointerEvents,
                    marked:
                      editor.dataset.matholicKioskSubjectiveTouchTarget ===
                        'true',
                    clicked: editor.dataset.clicked === 'yes',
                    scopeMinHeight: parseFloat(scopeStyle.minHeight),
                    scopeOverflow: scopeStyle.overflow,
                    hiddenEditorStillHidden:
                      getComputedStyle(hiddenEditor).display === 'none' ||
                      getComputedStyle(hiddenEditor.parentElement).display ===
                        'none'
                  });
                })()
                """.trimIndent(),
            )
            assertEquals("inline-block", proof.getString("display"))
            assertTrue(proof.getDouble("minWidth") >= 220.0)
            assertTrue(proof.getDouble("minHeight") >= 56.0)
            assertTrue(proof.getDouble("actualWidth") >= 220.0)
            assertTrue(proof.getDouble("actualHeight") >= 56.0)
            assertEquals("auto", proof.getString("pointerEvents"))
            assertTrue(proof.getBoolean("marked"))
            assertTrue(proof.getBoolean("clicked"))
            assertTrue(proof.getDouble("scopeMinHeight") >= 64.0)
            assertEquals("visible", proof.getString("scopeOverflow"))
            assertTrue(proof.getBoolean("hiddenEditorStillHidden"))
        }
    }

    @Test
    fun testStudentExperienceExpandsPreTextareaMathShellAndHidesClearControl() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div id="answer-input-form-pretextarea">
                <div>
                  <button type="button">루트</button>
                  <button type="button">분수</button>
                  <button type="button">파이</button>
                </div>
                <div id="pretextarea-shell" style="position:relative">
                  <span id="pretextarea-editor"
                    style="display:inline;width:160px;padding:8px;
                      border-radius:6px;border:1px solid #d9d9d9;
                      font-size:1.2em;text-align:center"></span>
                  <span id="pretextarea-clear" class="clear-control"
                    style="position:absolute">
                    <button type="button" aria-label="지우기"></button>
                  </span>
                </div>
              </div>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => {
                  const editor = document.getElementById('pretextarea-editor');
                  const clear = document.querySelector(
                    '#pretextarea-clear button'
                  );
                  const style = getComputedStyle(editor);
                  const rect = editor.getBoundingClientRect();
                  return JSON.stringify({
                    hasTextarea: !!editor.querySelector('textarea'),
                    display: style.display,
                    actualWidth: rect.width,
                    actualHeight: rect.height,
                    pointerEvents: style.pointerEvents,
                    marked:
                      editor.dataset.matholicKioskSubjectiveTouchTarget ===
                        'true',
                    clearHidden: getComputedStyle(clear).display === 'none'
                  });
                })()
                """.trimIndent(),
            )
            assertFalse(proof.getBoolean("hasTextarea"))
            assertEquals("inline-block", proof.getString("display"))
            assertTrue(proof.getDouble("actualWidth") >= 220.0)
            assertTrue(proof.getDouble("actualHeight") >= 56.0)
            assertEquals("auto", proof.getString("pointerEvents"))
            assertTrue(proof.getBoolean("marked"))
            assertTrue(proof.getBoolean("clearHidden"))
        }
    }

    @Test
    fun testStudentExperiencePreloadsMathQuillRuntimeSequentially() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head>
              <script>
                window.matholicRuntimeOrder = [];
                const nativeAppendChild =
                  document.head.appendChild.bind(document.head);
                document.head.appendChild = node => {
                  if (
                    node.tagName === 'SCRIPT' &&
                    node.id.startsWith('matholic-kiosk-')
                  ) {
                    window.matholicRuntimeOrder.push(node.id);
                    node.src = 'data:text/javascript,';
                    const result = nativeAppendChild(node);
                    setTimeout(() => {
                      if (node.id === 'matholic-kiosk-jquery-runtime') {
                        window.jQuery = {};
                      } else if (
                        node.id === 'matholic-kiosk-mathquill-runtime'
                      ) {
                        window.MathQuill = {};
                      }
                      node.dispatchEvent(new Event('load'));
                    }, 20);
                    return result;
                  }
                  return nativeAppendChild(node);
                };
              </script>
            </head><body></body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            Thread.sleep(300)
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  order: window.matholicRuntimeOrder,
                  jqueryReady: !!window.jQuery,
                  mathQuillReady: !!window.MathQuill,
                  promiseReused:
                    !!window.__matholicKioskMathQuillRuntimePromise
                }))()
                """.trimIndent(),
            )
            val order = proof.getJSONArray("order")
            assertEquals(2, order.length())
            assertEquals("matholic-kiosk-jquery-runtime", order.getString(0))
            assertEquals("matholic-kiosk-mathquill-runtime", order.getString(1))
            assertTrue(proof.getBoolean("jqueryReady"))
            assertTrue(proof.getBoolean("mathQuillReady"))
            assertTrue(proof.getBoolean("promiseReused"))
        }
    }

    @Test
    fun testStudentExperienceRemountsFailedMathQuillIntoOfficialTempState() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div id="failed-answer-form">
                <div>
                  <button id="failed-root" type="button">루트</button>
                  <button type="button">분수</button>
                  <button type="button">파이</button>
                </div>
                <div style="position:relative">
                  <span id="failed-editor"
                    style="display:inline;width:160px;padding:8px;
                      border-radius:6px;border:1px solid #d9d9d9;
                      font-size:1.2em;text-align:center"></span>
                  <span style="position:absolute">
                    <button type="button" aria-label="지우기"></button>
                  </span>
                </div>
                <button type="button">입력기</button>
              </div>
              <script>
                const failedShell = document.getElementById('failed-editor');
                window.remountedAnswerState = {
                  number: 4,
                  value: '28',
                  type: 'EQ',
                  originalType: 'EQ'
                };
                window.remountedTempAnswer = {
                  number: 4,
                  userAnswerValue: '28'
                };
                window.remountCalls = [];
                const officialOnChange = (number, update) => {
                  window.remountCalls.push({
                    number,
                    type: update.type || null,
                    value: update.value
                  });
                  window.remountedAnswerState = {
                    ...window.remountedAnswerState,
                    ...update,
                    value: update.value === null ? null :
                      (update.value ?? '')
                  };
                  window.remountedTempAnswer = {
                    number,
                    userAnswerValue:
                      window.remountedAnswerState.value
                  };
                  if (update.type === 'ONE') {
                    failedShell.remove();
                    const input = document.createElement('input');
                    input.id = 'temporary-standard-input';
                    document.getElementById('failed-answer-form')
                      .appendChild(input);
                  } else if (update.type === 'EQ') {
                    document.getElementById(
                      'temporary-standard-input'
                    )?.remove();
                    const editor = document.createElement('span');
                    editor.id = 'official-remounted-editor';
                    editor.className =
                      'mq-editable-field mq-math-mode';
                    editor.dataset.latex =
                      window.remountedAnswerState.value || '';
                    document.getElementById('failed-answer-form')
                      .appendChild(editor);
                  }
                };
                failedShell['__reactFiber${'$'}fixture'] = {
                  return: {
                    memoizedProps: {
                      userAnswer: window.remountedAnswerState,
                      onChange: officialOnChange
                    },
                    return: null
                  }
                };
                window.simulateOfficialMathEdit = value => {
                  officialOnChange(4, {
                    value: value === '' ? null : value
                  });
                };
                window.MathQuill = {};
              </script>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val initial = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(initial.getBoolean("ok"))
            assertEquals(0, initial.getInt("mathModeRemounted"))
            val agedFailure = evaluate(
                webView,
                """
                (() => {
                  const shell = document.getElementById('failed-editor');
                  shell.dataset.matholicKioskMathShellSeenAt =
                    String(Date.now() - 2000);
                  return JSON.stringify({ ok: true });
                })()
                """.trimIndent(),
            )
            assertTrue(agedFailure.getBoolean("ok"))
            val recovered = evaluate(
                webView,
                WebDomScripts.applyStudentExperience,
            )
            assertEquals(1, recovered.getInt("mathModeRemounted"))
            Thread.sleep(300)
            val remounted = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  calls: window.remountCalls,
                  state: window.remountedAnswerState,
                  temp: window.remountedTempAnswer,
                  editorLatex: document.getElementById(
                    'official-remounted-editor'
                  )?.dataset.latex || null
                }))()
                """.trimIndent(),
            )
            val calls = remounted.getJSONArray("calls")
            assertEquals(2, calls.length())
            assertEquals("ONE", calls.getJSONObject(0).getString("type"))
            assertEquals("EQ", calls.getJSONObject(1).getString("type"))
            assertEquals("28", calls.getJSONObject(0).getString("value"))
            assertEquals("28", calls.getJSONObject(1).getString("value"))
            assertEquals(
                "EQ",
                remounted.getJSONObject("state").getString("type"),
            )
            assertEquals(
                "28",
                remounted.getJSONObject("temp").getString("userAnswerValue"),
            )
            assertEquals("28", remounted.getString("editorLatex"))

            evaluate(
                webView,
                """
                (() => {
                  window.simulateOfficialMathEdit('35');
                  return JSON.stringify({ ok: true });
                })()
                """.trimIndent(),
            )
            val edited = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  state: window.remountedAnswerState,
                  temp: window.remountedTempAnswer
                }))()
                """.trimIndent(),
            )
            assertEquals(
                "35",
                edited.getJSONObject("state").getString("value"),
            )
            assertEquals(
                "35",
                edited.getJSONObject("temp").getString("userAnswerValue"),
            )

            evaluate(
                webView,
                """
                (() => {
                  window.simulateOfficialMathEdit('');
                  return JSON.stringify({ ok: true });
                })()
                """.trimIndent(),
            )
            val cleared = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  state: window.remountedAnswerState,
                  temp: window.remountedTempAnswer
                }))()
                """.trimIndent(),
            )
            assertTrue(cleared.getJSONObject("state").isNull("value"))
            assertTrue(
                cleared.getJSONObject("temp").isNull("userAnswerValue"),
            )
        }
    }

    @Test
    fun testStudentExperienceInitializesSubjectiveEditorWhenScrolledIntoView() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head>
              <style>
                #answer-input-form-offscreen {
                  display:none;
                  height:4px;
                  min-height:0;
                  overflow:hidden;
                }
              </style>
            </head>
            <body style="margin:0">
              <div id="answer-input-form-offscreen">
                <button id="lazy-input-menu" type="button">입력기</button>
              </div>
              <script>
                document.getElementById('lazy-input-menu')
                  .addEventListener('click', function () {
                    this.dataset.clicked = 'yes';
                    if (document.getElementById('scrolled-editor')) return;
                    const scope = document.getElementById(
                      'answer-input-form-offscreen'
                    );
                    const editor = document.createElement('span');
                    editor.id = 'scrolled-editor';
                    editor.className = 'mq-editable-field mq-math-mode';
                    editor.style.cssText =
                      'display:inline;height:4px;min-height:0;width:12px';
                    editor.innerHTML =
                      '<span class="mq-textarea"><textarea></textarea></span>' +
                      '<span class="mq-root-block"></span>';
                    scope.appendChild(editor);
                    ['루트', '분수', '파이'].forEach(label => {
                      const button = document.createElement('button');
                      button.type = 'button';
                      button.textContent = label;
                      scope.appendChild(button);
                    });
                  });
              </script>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val initial = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(initial.getBoolean("ok"))
            val beforeScroll = evaluate(
                webView,
                """
                (() => {
                  const scope = document.getElementById(
                    'answer-input-form-offscreen'
                  );
                  return JSON.stringify({
                    clicked:
                      document.getElementById('lazy-input-menu').dataset
                        .clicked === 'yes',
                    scopeMinHeight:
                      parseFloat(getComputedStyle(scope).minHeight),
                    scopeOverflow: getComputedStyle(scope).overflow,
                    scopeMarked:
                      scope.dataset.matholicKioskSubjectiveTouchScope ===
                        'true'
                  });
                })()
                """.trimIndent(),
            )
            assertFalse(beforeScroll.getBoolean("clicked"))
            assertTrue(beforeScroll.getDouble("scopeMinHeight") >= 64.0)
            assertEquals("visible", beforeScroll.getString("scopeOverflow"))
            assertTrue(beforeScroll.getBoolean("scopeMarked"))

            evaluate(
                webView,
                """
                (() => {
                  document.styleSheets[0].cssRules[0].style.display = 'block';
                  document.dispatchEvent(new Event('scroll'));
                  return JSON.stringify({ ok: true });
                })()
                """.trimIndent(),
            )
            Thread.sleep(300)

            val afterScroll = evaluate(
                webView,
                """
                (() => {
                  const editor = document.getElementById('scrolled-editor');
                  const editorStyle = editor && getComputedStyle(editor);
                  const rect = editor && editor.getBoundingClientRect();
                  return JSON.stringify({
                    clicked:
                      document.getElementById('lazy-input-menu').dataset
                        .clicked === 'yes',
                    editorExists: !!editor,
                    display: editorStyle && editorStyle.display,
                    actualWidth: rect && rect.width,
                    actualHeight: rect && rect.height,
                    pointerEvents: editorStyle && editorStyle.pointerEvents
                  });
                })()
                """.trimIndent(),
            )
            assertTrue(afterScroll.getBoolean("clicked"))
            assertTrue(afterScroll.getBoolean("editorExists"))
            assertEquals("inline-block", afterScroll.getString("display"))
            assertTrue(afterScroll.getDouble("actualWidth") >= 220.0)
            assertTrue(afterScroll.getDouble("actualHeight") >= 56.0)
            assertEquals("auto", afterScroll.getString("pointerEvents"))
        }
    }

    @Test
    fun testStudentExperienceAllowsDeletingExistingAnswerAfterVendorIgnoredEdits() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div id="answer-input-form-existing">
                <div>
                  <button type="button">루트</button>
                  <button type="button">분수</button>
                  <button type="button">파이</button>
                </div>
                <span id="existing-editor"
                  class="mq-editable-field mq-math-mode">
                  <span class="mq-textarea"><textarea></textarea></span>
                  <span class="mq-root-block"></span>
                </span>
                <button type="button">입력기</button>
              </div>
              <script>
                window.existingAnswerState = {
                  prop: '28',
                  latex: '28',
                  ignoredEdits: 2,
                  editCount: 0
                };
                window.recordExistingAnswerEdit = () => {
                  const state = window.existingAnswerState;
                  state.editCount += 1;
                  if (state.ignoredEdits > 0) {
                    state.ignoredEdits -= 1;
                  } else {
                    state.prop = state.latex === '' ? null : state.latex;
                  }
                };
                const editor = document.getElementById('existing-editor');
                editor.fieldApi = {
                  latex: value => {
                    if (value !== undefined) {
                      window.existingAnswerState.latex = value;
                    }
                    return window.existingAnswerState.latex;
                  },
                  write: value => {
                    window.existingAnswerState.latex += value;
                    window.recordExistingAnswerEdit();
                  },
                  keystroke: key => {
                    if (key === 'Backspace') {
                      window.existingAnswerState.latex =
                        window.existingAnswerState.latex.slice(0, -1);
                    }
                    window.recordExistingAnswerEdit();
                  }
                };
                window.MathQuill = {
                  getInterface: () => element => element.fieldApi || null
                };
                window.clearExistingAnswer = () => {
                  const state = window.existingAnswerState;
                  state.latex = '';
                  window.recordExistingAnswerEdit();
                  if (state.prop !== null && state.latex !== state.prop) {
                    state.latex = state.prop;
                  }
                };
              </script>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertTrue(result.getBoolean("ok"))
            val beforeClear = evaluate(
                webView,
                """
                (() => JSON.stringify(window.existingAnswerState))()
                """.trimIndent(),
            )
            assertEquals("28", beforeClear.getString("prop"))
            assertEquals("28", beforeClear.getString("latex"))
            assertEquals(0, beforeClear.getInt("ignoredEdits"))
            assertEquals(2, beforeClear.getInt("editCount"))

            evaluate(
                webView,
                """
                (() => {
                  window.clearExistingAnswer();
                  return JSON.stringify({ ok: true });
                })()
                """.trimIndent(),
            )
            val afterClear = evaluate(
                webView,
                """
                (() => JSON.stringify(window.existingAnswerState))()
                """.trimIndent(),
            )
            assertTrue(afterClear.isNull("prop"))
            assertEquals("", afterClear.getString("latex"))
            assertEquals(3, afterClear.getInt("editCount"))
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
            assertEquals("none", proof.getString("inputMode"))
        }
    }

    @Test
    fun testStudentExperiencePrimesEveryMountedMathFieldAndHidesOnlyLocalClear() {
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
            evaluate(webView, WebDomScripts.applyStudentExperience)
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
    fun testStudentExperienceAddsIdempotentBottomMathKeypad() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <main>
                <div id="answer-input-form-0">
                  <div id="math-toolbar">
                    <button>루트</button>
                    <button>분수</button>
                    <button>파이</button>
                  </div>
                  <span id="math-editor" class="mq-editable-field mq-math-mode">
                    <span class="mq-textarea"><textarea></textarea></span>
                    <span class="mq-root-block"></span>
                  </span>
                  <button id="input-menu">입력기</button>
                </div>
                <script>
                  window.cursorKeys = [];
                  window.mathCommands = [];
                  window.cursorFocusCount = 0;
                  const editor = document.getElementById('math-editor');
                  let latex = '';
                  let selected = false;
                  editor.fieldApi = {
                    latex: value => {
                      if (value !== undefined) latex = value;
                      return latex;
                    },
                    write: value => { latex += value; },
                    cmd: value => {
                      window.mathCommands.push(value);
                      latex += value;
                    },
                    select: () => { selected = true; },
                    keystroke: key => {
                      if (key === 'Backspace') {
                        latex = selected ? '' : latex.slice(0, -1);
                        selected = false;
                      } else {
                        window.cursorKeys.push(key);
                      }
                    },
                    focus: () => { window.cursorFocusCount += 1; }
                  };
                  window.MathQuill = {
                    getInterface: () => element => element.fieldApi || null
                  };
                </script>
              </main>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            evaluate(webView, WebDomScripts.applyStudentExperience)
            evaluate(webView, WebDomScripts.applyStudentExperience)
            val proof = evaluate(
                webView,
                """
                (() => {
                  const navigation = document.querySelector(
                    '.matholic-kiosk-math-nav'
                  );
                   const buttons = Array.from(document.querySelectorAll(
                     '.matholic-kiosk-math-nav ' +
                     '[data-matholic-kiosk-key-action]'
                   ));
                   const hiddenBeforeDirectInput =
                     getComputedStyle(navigation).display === 'none';
                   document.getElementById('math-editor').dispatchEvent(
                     new Event('focusin', { bubbles: true })
                   );
                   const hiddenAfterProgrammaticFocus =
                     getComputedStyle(navigation).display === 'none';
                   document.getElementById('math-editor').dispatchEvent(
                     new Event('pointerdown', { bubbles: true })
                   );
                   const visibleWhileDirectlyEditing =
                     getComputedStyle(navigation).display === 'block';
                   const sectionRects = Array.from(
                     navigation.querySelectorAll(
                       '.matholic-kiosk-keypad-section'
                     )
                   ).map(section => section.getBoundingClientRect());
                   const numericHeading = navigation.querySelector(
                     '.matholic-kiosk-keypad-numeric ' +
                     '.matholic-kiosk-keypad-heading'
                   ).getBoundingClientRect();
                   const numericGrid = navigation.querySelector(
                     '.matholic-kiosk-keypad-numeric-grid'
                   ).getBoundingClientRect();
                   const key = value => Array.from(navigation.querySelectorAll(
                     '[data-matholic-kiosk-key-value]'
                   )).find(button =>
                     button.dataset.matholicKioskKeyValue === value
                   );
                   key('1').click();
                   key('2').click();
                   navigation.querySelector(
                     '[data-matholic-kiosk-key-value="Backspace"]'
                   ).click();
                   key('-').click();
                   key('.').click();
                   key('\\sqrt').click();
                   key('\\frac').click();
                   key('\\pi').click();
                   navigation.querySelectorAll(
                     '.matholic-kiosk-keypad-arrows button'
                   ).forEach(button => button.click());
                   const clearButton = navigation.querySelector(
                     '[data-matholic-kiosk-key-action="clear"]'
                   );
                   clearButton.click();
                   const clearArmed =
                     clearButton.dataset.matholicKioskClearArmed === 'true';
                   const answerBeforeConfirmedClear = editor.fieldApi.latex();
                   clearButton.click();
                   const answerAfterConfirmedClear = editor.fieldApi.latex();
                   const undoButton = navigation.querySelector(
                     '[data-matholic-kiosk-key-action="undo"]'
                   );
                   const redoButton = navigation.querySelector(
                     '[data-matholic-kiosk-key-action="redo"]'
                   );
                   undoButton.click();
                   const answerAfterUndo = editor.fieldApi.latex();
                   redoButton.click();
                   const answerAfterRedo = editor.fieldApi.latex();
                   document.body.dispatchEvent(
                     new Event('pointerdown', { bubbles: true })
                   );
                   return JSON.stringify({
                     count: buttons.length,
                     numericCount: navigation.querySelectorAll(
                       '.matholic-kiosk-keypad-numeric-grid button'
                     ).length,
                     structureCount: navigation.querySelectorAll(
                       '.matholic-kiosk-keypad-structure-grid button'
                     ).length,
                     arrowCount: navigation.querySelectorAll(
                       '.matholic-kiosk-keypad-arrows button'
                     ).length,
                     actionCount: navigation.querySelectorAll(
                       '.matholic-kiosk-keypad-actions button'
                     ).length,
                     actionLabels: Array.from(navigation.querySelectorAll(
                       '.matholic-kiosk-keypad-actions button'
                     )).map(button => button.textContent),
                     headings: Array.from(navigation.querySelectorAll(
                       '.matholic-kiosk-keypad-heading'
                     )).map(heading => heading.textContent),
                     keys: window.cursorKeys,
                     commands: window.mathCommands,
                     focusCount: window.cursorFocusCount,
                     parentIsBody: navigation.parentElement === document.body,
                     position: getComputedStyle(navigation).position,
                     left: parseFloat(getComputedStyle(navigation).left),
                     right: parseFloat(getComputedStyle(navigation).right),
                     bottom: parseFloat(getComputedStyle(navigation).bottom),
                     firstGap: sectionRects[1].left - sectionRects[0].right,
                     secondGap: sectionRects[2].left - sectionRects[1].right,
                     numericHeaderLeftDelta:
                       numericHeading.left - numericGrid.left,
                     numericHeaderRightDelta:
                       numericHeading.right - numericGrid.right,
                     inputMode: document.querySelector(
                       '#math-editor textarea'
                     ).getAttribute('inputmode'),
                     originalToolbarHidden: Array.from(
                       document.querySelectorAll('#math-toolbar button')
                     ).every(button =>
                       getComputedStyle(button).display === 'none'
                     ),
                     navCount: document.querySelectorAll(
                       '.matholic-kiosk-math-nav'
                     ).length,
                     hiddenBeforeDirectInput,
                     hiddenAfterProgrammaticFocus,
                     visibleWhileDirectlyEditing,
                     clearArmed,
                     answerBeforeConfirmedClear,
                     answerAfterConfirmedClear,
                     answerAfterUndo,
                     answerAfterRedo,
                     hiddenAfterOutsideTouch:
                       getComputedStyle(navigation).display === 'none'
                   });
                })()
                """.trimIndent(),
            )
            assertEquals(23, proof.getInt("count"))
            assertEquals(12, proof.getInt("numericCount"))
            assertEquals(3, proof.getInt("structureCount"))
            assertEquals(4, proof.getInt("arrowCount"))
            assertEquals(4, proof.getInt("actionCount"))
            assertEquals("실행 취소", proof.getJSONArray("actionLabels").getString(0))
            assertEquals("다시 실행", proof.getJSONArray("actionLabels").getString(1))
            assertEquals("숫자 · 소수점 · 부호", proof.getJSONArray("headings").getString(0))
            assertEquals("수식 구조", proof.getJSONArray("headings").getString(1))
            assertEquals("이동 · 수정", proof.getJSONArray("headings").getString(2))
            assertEquals(1, proof.getInt("navCount"))
            assertTrue(proof.getInt("focusCount") >= 12)
            assertTrue(proof.getBoolean("parentIsBody"))
            assertEquals("fixed", proof.getString("position"))
            assertEquals(14.0, proof.getDouble("left"), 0.6)
            assertEquals(218.0, proof.getDouble("right"), 0.6)
            assertEquals(12.0, proof.getDouble("bottom"), 0.6)
            assertEquals(
                proof.getDouble("firstGap"),
                proof.getDouble("secondGap"),
                0.6,
            )
            assertEquals(0.0, proof.getDouble("numericHeaderLeftDelta"), 0.6)
            assertEquals(0.0, proof.getDouble("numericHeaderRightDelta"), 0.6)
            assertEquals("none", proof.getString("inputMode"))
            assertTrue(proof.getBoolean("originalToolbarHidden"))
            assertEquals("\\sqrt", proof.getJSONArray("commands").getString(0))
            assertEquals("\\frac", proof.getJSONArray("commands").getString(1))
            assertEquals("\\pi", proof.getJSONArray("commands").getString(2))
            assertEquals("Up", proof.getJSONArray("keys").getString(0))
            assertEquals("Left", proof.getJSONArray("keys").getString(1))
            assertEquals("Down", proof.getJSONArray("keys").getString(2))
            assertEquals("Right", proof.getJSONArray("keys").getString(3))
            assertTrue(proof.getBoolean("hiddenBeforeDirectInput"))
            assertTrue(proof.getBoolean("hiddenAfterProgrammaticFocus"))
            assertTrue(proof.getBoolean("visibleWhileDirectlyEditing"))
            assertTrue(proof.getBoolean("clearArmed"))
            assertEquals(
                "1-.\\sqrt\\frac\\pi",
                proof.getString("answerBeforeConfirmedClear"),
            )
            assertEquals("", proof.getString("answerAfterConfirmedClear"))
            assertEquals(
                proof.getString("answerBeforeConfirmedClear"),
                proof.getString("answerAfterUndo"),
            )
            assertEquals("", proof.getString("answerAfterRedo"))
            assertTrue(proof.getBoolean("hiddenAfterOutsideTouch"))
        }
    }

    @Test
    fun testStudentExperienceDismissesMathNavigationWhenAnswerSubmitIsTouched() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div id="answer-input-form-0">
                <div>
                  <button>루트</button>
                  <button>분수</button>
                  <button>파이</button>
                </div>
                <span id="math-editor" class="mq-editable-field mq-math-mode">
                  <span class="mq-textarea"><textarea></textarea></span>
                  <span class="mq-root-block"></span>
                </span>
                <button>입력기</button>
              </div>
              <button id="answer-submit">답안제출</button>
              <script>
                const editor = document.getElementById('math-editor');
                let latex = '';
                editor.fieldApi = {
                  latex: value => {
                    if (value !== undefined) latex = value;
                    return latex;
                  },
                  write: value => { latex += value; },
                  keystroke: key => {
                    if (key === 'Backspace') latex = latex.slice(0, -1);
                  },
                  focus: () => {}
                };
                window.MathQuill = {
                  getInterface: () => element => element.fieldApi || null
                };
              </script>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => {
                  const navigation = document.querySelector(
                    '.matholic-kiosk-math-nav'
                  );
                  document.getElementById('math-editor').dispatchEvent(
                    new Event('pointerdown', { bubbles: true })
                  );
                   const visibleDuringEdit =
                     getComputedStyle(navigation).display === 'block';
                  document.getElementById('answer-submit').dispatchEvent(
                    new Event('pointerdown', { bubbles: true })
                  );
                  return JSON.stringify({
                    visibleDuringEdit,
                    hiddenOnSubmitTouch:
                      getComputedStyle(navigation).display === 'none'
                  });
                })()
                """.trimIndent(),
            )
            assertTrue(proof.getBoolean("visibleDuringEdit"))
            assertTrue(proof.getBoolean("hiddenOnSubmitTouch"))
        }
    }

    @Test
    fun testStudentExperienceKeepsNativeMathInputWhenCustomKeypadCannotBeBuilt() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div id="answer-input-form-0">
                <span id="math-editor" class="mq-editable-field mq-math-mode">
                  <span class="mq-textarea">
                    <textarea inputmode="decimal"></textarea>
                  </span>
                  <span class="mq-root-block"></span>
                </span>
              </div>
              <script>
                const editor = document.getElementById('math-editor');
                let latex = '';
                editor.fieldApi = {
                  latex: value => {
                    if (value !== undefined) latex = value;
                    return latex;
                  },
                  write: value => { latex += value; },
                  keystroke: key => {
                    if (key === 'Backspace') latex = latex.slice(0, -1);
                  },
                  focus: () => {}
                };
                window.MathQuill = {
                  getInterface: () => element => element.fieldApi || null
                };
              </script>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  keypadCount: document.querySelectorAll(
                    '.matholic-kiosk-math-nav'
                  ).length,
                  inputMode: document.querySelector(
                    '#math-editor textarea'
                  ).getAttribute('inputmode'),
                  pointerEvents: getComputedStyle(
                    document.getElementById('math-editor')
                  ).pointerEvents
                }))()
                """.trimIndent(),
            )
            assertEquals(0, proof.getInt("keypadCount"))
            assertEquals("decimal", proof.getString("inputMode"))
            assertEquals("auto", proof.getString("pointerEvents"))
        }
    }

    @Test
    fun testStudentExperienceScrollsFocusedMathFieldAboveBottomKeypad() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <div id="scroll-root" style="height:700px;overflow-y:auto">
                <div style="height:600px"></div>
                <div id="answer-input-form-0">
                  <div>
                    <button>루트</button>
                    <button>분수</button>
                    <button>파이</button>
                  </div>
                  <span id="math-editor" class="mq-editable-field mq-math-mode">
                    <span class="mq-textarea"><textarea></textarea></span>
                    <span class="mq-root-block"></span>
                  </span>
                </div>
                <div style="height:600px"></div>
              </div>
              <script>
                const editor = document.getElementById('math-editor');
                let latex = '';
                editor.fieldApi = {
                  latex: value => {
                    if (value !== undefined) latex = value;
                    return latex;
                  },
                  write: value => { latex += value; },
                  keystroke: key => {
                    if (key === 'Backspace') latex = latex.slice(0, -1);
                  },
                  focus: () => {}
                };
                window.MathQuill = {
                  getInterface: () => element => element.fieldApi || null
                };
              </script>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            evaluate(
                webView,
                """
                (() => {
                  document.getElementById('math-editor').dispatchEvent(
                    new Event('pointerdown', { bubbles: true })
                  );
                  return JSON.stringify({ activated: true });
                })()
                """.trimIndent(),
            )
            Thread.sleep(100)
            val proof = evaluate(
                webView,
                """
                (() => {
                  const editor = document.getElementById('math-editor');
                  const keypad = document.querySelector(
                    '.matholic-kiosk-math-nav'
                  );
                  if (!editor || !keypad) {
                    return JSON.stringify({
                      editorPresent: !!editor,
                      keypadPresent: !!keypad,
                      scrollTop: document.getElementById(
                        'scroll-root'
                      )?.scrollTop || 0
                    });
                  }
                  const editorRect = editor.getBoundingClientRect();
                  const keypadRect = keypad.getBoundingClientRect();
                  return JSON.stringify({
                    editorPresent: true,
                    keypadPresent: true,
                    scrollTop: document.getElementById('scroll-root').scrollTop,
                    editorBottom: editorRect.bottom,
                    keypadTop: keypadRect.top,
                    clearGap: keypadRect.top - editorRect.bottom
                  });
                })()
                """.trimIndent(),
            )
            assertTrue("editor missing: $proof", proof.getBoolean("editorPresent"))
            assertTrue("keypad missing: $proof", proof.getBoolean("keypadPresent"))
            assertTrue("scroll did not move: $proof", proof.getDouble("scrollTop") > 0.0)
            assertTrue(
                "editor remains behind keypad: $proof",
                proof.getDouble("editorBottom") <= proof.getDouble("keypadTop") - 15.0,
            )
            assertTrue("keypad gap too small: $proof", proof.getDouble("clearGap") >= 15.0)
        }
    }

    @Test
    fun testStudentExperienceBlocksSubmitTapThroughIntoNewReviewModal() {
        withFixture(
            "https://im.matholic.com/learningV2/answer/virtual",
            """
            <!doctype html><html><head></head><body>
              <button id="answer-submit" onclick="
                document.body.dataset.answerSubmits =
                  String(Number(document.body.dataset.answerSubmits || '0') + 1);
                document.getElementById('review').style.display = 'block';
              ">답안제출</button>
              <div id="review" class="ant-modal" role="dialog" style="display:none">
                <div class="ant-modal-title">전체답안</div>
                <button id="final-submit" onclick="
                  document.body.dataset.finalSubmits =
                    String(Number(document.body.dataset.finalSubmits || '0') + 1);
                ">답안 제출</button>
              </div>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            assertTrue(evaluate(webView, WebDomScripts.applyStudentExperience).getBoolean("ok"))
            val proof = evaluate(
                webView,
                """
                (() => {
                  const first = document.getElementById('answer-submit');
                  const finalButton = document.getElementById('final-submit');
                  first.click();
                  finalButton.click();
                  const immediateFinalSubmits =
                    Number(document.body.dataset.finalSubmits || '0');
                  window.__matholicKioskLastAnswerSubmitAt = Date.now() - 2000;
                  finalButton.click();
                  return JSON.stringify({
                    answerSubmits: Number(document.body.dataset.answerSubmits || '0'),
                    immediateFinalSubmits,
                    finalSubmits: Number(document.body.dataset.finalSubmits || '0'),
                    reentryBlocked:
                      finalButton.dataset.matholicKioskSubmitReentryBlocked === 'true'
                  });
                })()
                """.trimIndent(),
            )
            assertEquals(1, proof.getInt("answerSubmits"))
            assertEquals(0, proof.getInt("immediateFinalSubmits"))
            assertEquals(1, proof.getInt("finalSubmits"))
            assertTrue(proof.getBoolean("reentryBlocked"))
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
    fun testWrongAnswerSummaryAcceptsOneProblemOnlyWhenScoreboardProvesOne() {
        withFixture(
            "https://im.matholic.com/learningV2/result/virtual",
            """
            <!doctype html><html data-matholic-kiosk-result-hydrated="true"><body>
              <h2>종합분석</h2>
              <table>
                <thead><tr><th>문항수</th></tr></thead>
                <tbody><tr><td>1</td></tr></tbody>
              </table>
              <section class="ant-alert-error"><h3>1번 문제</h3></section>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val result = evaluate(webView, WebDomScripts.wrongAnswerSummary)
            assertTrue(result.toString(), result.getBoolean("ok"))
            assertEquals(1, result.getInt("totalProblems"))
            assertEquals(1, result.getInt("expectedProblems"))
            assertEquals(1, result.getInt("classifiedCount"))
            assertEquals(1, result.getJSONArray("wrongNumbers").length())
            assertEquals(1, result.getJSONArray("wrongNumbers").getInt(0))
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

    @Test
    fun testClippedProblemImageExtendsRootScrollAndResetsOnProblemChange() {
        withFixture(
            "https://im.matholic.com/learningV2/virtual",
            """
            <!doctype html><html><body style="margin:0;overflow:hidden">
              <div id="root" style="height:800px;overflow-y:auto">
                <div>
                  <button aria-label="이전 문제">&lt;</button>
                  <div>
                    <select aria-label="문제 번호">
                      <option selected>15</option>
                      <option>16</option>
                    </select>
                    <span>/25</span>
                  </div>
                  <button aria-label="다음 문제">&gt;</button>
                </div>
                <main style="height:820px">
                  <picture class="no-select" style="display:block;width:560px;pointer-events:none;touch-action:none">
                    <img id="problem-image" class="no-select"
                      src="https://image.matholic.com/units/virtual.webp"
                      style="display:block;width:560px;height:900px;pointer-events:none;touch-action:none">
                  </picture>
                </main>
              </div>
            </body></html>
            """.trimIndent(),
        ) { webView ->
            val initial = evaluate(webView, WebDomScripts.applyStudentExperience)
            assertEquals(1, initial.getInt("longProblemScrollEnhancements"))
            val scrollable = evaluate(
                webView,
                """
                (() => {
                  const root = document.getElementById('root');
                  const image = document.getElementById('problem-image');
                  root.scrollTop = root.scrollHeight;
                  return JSON.stringify({
                    marked: root.dataset.matholicKioskLongPageScroll === 'true',
                    extra: Number(root.dataset.matholicKioskLongPageExtra || '0'),
                    maxScroll: root.scrollHeight - root.clientHeight,
                    rootTouchAction: getComputedStyle(root).touchAction,
                    mediaTouchAction: getComputedStyle(image).touchAction,
                    mediaPointerEvents: getComputedStyle(image).pointerEvents,
                    controllerExists: !!window
                      .__matholicKioskLongPageScrollController,
                    scrollTop: root.scrollTop
                  });
                })()
                """.trimIndent(),
            )
            assertTrue(scrollable.getBoolean("marked"))
            assertTrue(scrollable.getInt("extra") > 0)
            assertTrue(scrollable.getInt("maxScroll") > 20)
            assertEquals("pan-y", scrollable.getString("rootTouchAction"))
            assertEquals("pan-y", scrollable.getString("mediaTouchAction"))
            assertEquals("auto", scrollable.getString("mediaPointerEvents"))
            assertTrue(scrollable.getBoolean("controllerExists"))
            assertTrue(scrollable.getInt("scrollTop") > 0)

            evaluate(webView, WebDomScripts.applyStudentExperience)
            val stableAtBottom = evaluate(
                webView,
                """
                (() => {
                  const root = document.getElementById('root');
                  return JSON.stringify({
                    marked: root.dataset.matholicKioskLongPageScroll === 'true',
                    extra: Number(root.dataset.matholicKioskLongPageExtra || '0'),
                    scrollTop: root.scrollTop
                  });
                })()
                """.trimIndent(),
            )
            assertTrue(stableAtBottom.getBoolean("marked"))
            assertEquals(scrollable.getInt("extra"), stableAtBottom.getInt("extra"))
            assertTrue(stableAtBottom.getInt("scrollTop") > 0)

            val drag = evaluate(
                webView,
                """
                (() => {
                  const root = document.getElementById('root');
                  const controller = window
                    .__matholicKioskLongPageScrollController;
                  root.scrollTop = 0;
                  controller.start(root, 700);
                  controller.move(300);
                  controller.end();
                  return JSON.stringify({ scrollTop: root.scrollTop });
                })()
                """.trimIndent(),
            )
            assertTrue(drag.getInt("scrollTop") > 0)

            evaluate(
                webView,
                """
                (() => {
                  document.querySelector('select').selectedIndex = -1;
                  return JSON.stringify({ ok: true });
                })()
                """.trimIndent(),
            )
            evaluate(webView, WebDomScripts.applyStudentExperience)
            val transientlyMissingNumber = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  scrollTop: document.getElementById('root').scrollTop
                }))()
                """.trimIndent(),
            )
            assertTrue(transientlyMissingNumber.getInt("scrollTop") > 0)

            evaluate(
                webView,
                """
                (() => {
                  const select = document.querySelector('select');
                  select.selectedIndex = 1;
                  select.dispatchEvent(new Event('change', { bubbles: true }));
                  document.getElementById('problem-image').style.height = '400px';
                  return JSON.stringify({ ok: true });
                })()
                """.trimIndent(),
            )
            evaluate(webView, WebDomScripts.applyStudentExperience)
            val reset = evaluate(
                webView,
                """
                (() => JSON.stringify({
                  marked: document.getElementById('root').dataset
                    .matholicKioskLongPageScroll === 'true',
                  scrollTop: document.getElementById('root').scrollTop
                }))()
                """.trimIndent(),
            )
            assertFalse(reset.getBoolean("marked"))
            assertEquals(0, reset.getInt("scrollTop"))
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
