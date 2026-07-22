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

    @SuppressLint("SetJavaScriptEnabled")
    private fun withFixture(baseUrl: String, html: String, block: (WebView) -> Unit) {
        val loaded = CountDownLatch(1)
        val reference = AtomicReference<WebView>()
        instrumentation.runOnMainSync {
            val webView = WebView(instrumentation.targetContext)
            webView.settings.javaScriptEnabled = true
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
