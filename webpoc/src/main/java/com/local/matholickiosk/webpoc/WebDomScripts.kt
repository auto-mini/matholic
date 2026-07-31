package com.local.matholickiosk.webpoc

import org.json.JSONObject

object WebDomScripts {
    const val CONTRACT_VERSION = "web-2026-08-01.21"

    val sanitizeLoginAndFingerprint: String =
        """
        (() => {
          const inputs = Array.from(document.querySelectorAll('input'));
          const usernames = inputs.filter(el => el.name === 'username');
          const passwords = inputs.filter(el => el.name === 'password' && el.type === 'password');
          const checkboxes = inputs.filter(el => el.type === 'checkbox');
          const forms = Array.from(document.querySelectorAll('form'));
          const buttons = Array.from(document.querySelectorAll('button[type="submit"]'));
          const form = forms.length === 1 ? forms[0] : null;
          let actionOk = false;
          let pageOriginOk = false;
          try {
            const action = new URL(form ? form.action : '', location.href);
            actionOk = action.protocol === 'https:' &&
              action.hostname === 'auth.matholic.com' &&
              action.port === '' &&
              action.username === '' &&
              action.password === '' &&
              action.pathname === '/token/signin' &&
              action.search === '' &&
              action.hash === '';
            const page = new URL(location.href);
            pageOriginOk = page.protocol === 'https:' &&
              page.hostname === 'login.matholic.com' &&
              page.port === '' &&
              page.username === '' &&
              page.password === '' &&
              page.pathname === '/' &&
              page.hash === '';
          } catch (_) {}
          const contractOk = pageOriginOk &&
            usernames.length === 1 && passwords.length === 1 &&
            checkboxes.length === 1 && buttons.length === 1 && actionOk;
          if (contractOk) {
            const valueSetter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
            valueSetter.call(usernames[0], '');
            usernames[0].dispatchEvent(new Event('input', { bubbles: true }));
            usernames[0].dispatchEvent(new Event('change', { bubbles: true }));
            valueSetter.call(passwords[0], '');
            passwords[0].dispatchEvent(new Event('input', { bubbles: true }));
            passwords[0].dispatchEvent(new Event('change', { bubbles: true }));
            const checkedSetter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'checked').set;
            checkedSetter.call(checkboxes[0], false);
            checkboxes[0].dispatchEvent(new Event('change', { bubbles: true }));
          }
          return JSON.stringify({
            version: '${CONTRACT_VERSION}', kind: 'login', ok: contractOk,
            usernameCount: usernames.length, passwordCount: passwords.length,
            checkboxCount: checkboxes.length, submitCount: buttons.length,
            formCount: forms.length, actionOk,
            usernameEmpty: usernames.length === 1 && usernames[0].value.length === 0,
            passwordEmpty: passwords.length === 1 && passwords[0].value.length === 0,
            rememberChecked: checkboxes.length === 1 ? checkboxes[0].checked : null
          });
        })()
        """.trimIndent()

    fun login(username: String, password: String): String {
        val usernameJson = JSONObject.quote(username)
        val passwordJson = JSONObject.quote(password)
        return """
            (() => {
              const usernames = Array.from(document.querySelectorAll('input[name="username"]'));
              const passwords = Array.from(document.querySelectorAll('input[name="password"]'))
                .filter(el => el.type === 'password');
              const checkboxes = Array.from(document.querySelectorAll('input[type="checkbox"]'));
              const buttons = Array.from(document.querySelectorAll('button[type="submit"]'));
              const forms = Array.from(document.querySelectorAll('form'));
              let actionOk = false;
              let pageOriginOk = false;
              try {
                const action = new URL(forms.length === 1 ? forms[0].action : '', location.href);
                actionOk = action.protocol === 'https:' &&
                  action.hostname === 'auth.matholic.com' &&
                  action.port === '' &&
                  action.username === '' &&
                  action.password === '' &&
                  action.pathname === '/token/signin' &&
                  action.search === '' &&
                  action.hash === '';
                const page = new URL(location.href);
                pageOriginOk = page.protocol === 'https:' &&
                  page.hostname === 'login.matholic.com' &&
                  page.port === '' &&
                  page.username === '' &&
                  page.password === '' &&
                  page.pathname === '/' &&
                  page.hash === '';
              } catch (_) {}
              const ok = pageOriginOk &&
                usernames.length === 1 && passwords.length === 1 &&
                checkboxes.length === 1 && buttons.length === 1 &&
                forms.length === 1 && actionOk;
              if (!ok) return JSON.stringify({ version: '${CONTRACT_VERSION}', ok: false });
              const valueSetter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
              valueSetter.call(usernames[0], $usernameJson);
              usernames[0].dispatchEvent(new Event('input', { bubbles: true }));
              usernames[0].dispatchEvent(new Event('change', { bubbles: true }));
              valueSetter.call(passwords[0], $passwordJson);
              passwords[0].dispatchEvent(new Event('input', { bubbles: true }));
              passwords[0].dispatchEvent(new Event('change', { bubbles: true }));
              const checkedSetter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'checked').set;
              checkedSetter.call(checkboxes[0], false);
              checkboxes[0].dispatchEvent(new Event('change', { bubbles: true }));
              if (typeof forms[0].requestSubmit === 'function') forms[0].requestSubmit(buttons[0]);
              else buttons[0].click();
              return JSON.stringify({ version: '${CONTRACT_VERSION}', ok: true });
            })()
        """.trimIndent()
    }

    val portalFingerprint: String =
        """
        (() => {
          const anchors = Array.from(document.querySelectorAll('a'));
          const exactImOrigin = url =>
            url.protocol === 'https:' &&
            url.hostname === 'im.matholic.com' &&
            url.port === '' &&
            url.username === '' &&
            url.password === '';
          const pathOf = el => {
            try {
              const url = new URL(el.href, location.href);
              return exactImOrigin(url) && url.hash === '' ? url.pathname : '';
            } catch (_) { return ''; }
          };
          const page = new URL(location.href);
          const userInfo = anchors.filter(el => pathOf(el) === '/userInfo');
          const accessLog = anchors.filter(el => pathOf(el) === '/userAccessLog');
          const course = anchors.filter(el => pathOf(el) === '/course');
          let submenu = null;
          let wrapper = null;
          let trigger = null;
          if (userInfo.length === 1 && accessLog.length === 1) {
            submenu = userInfo[0].parentElement;
            while (submenu && !submenu.contains(accessLog[0])) submenu = submenu.parentElement;
            wrapper = submenu ? submenu.parentElement : null;
            trigger = wrapper ? Array.from(wrapper.children).find(el =>
              el !== submenu && el.querySelector('svg') && (el.innerText || '').trim().length > 0
            ) : null;
          }
          const actualName = trigger ? (trigger.innerText || '').normalize('NFKC').trim().replace(/\s+/g, ' ') : '';
          const ok = exactImOrigin(page) &&
            page.hash === '' &&
            userInfo.length === 1 && accessLog.length === 1 &&
            course.length >= 1 && !!submenu && !!wrapper && !!trigger && actualName.length > 0;
          return JSON.stringify({
            version: '${CONTRACT_VERSION}', kind: 'portal', ok,
            userInfoCount: userInfo.length, accessLogCount: accessLog.length,
            courseCount: course.length, hasSubmenu: !!submenu,
            hasAccountTrigger: !!trigger, actualName
          });
        })()
        """.trimIndent()

    val openAccountMenu: String =
        """
        (() => {
          const anchors = Array.from(document.querySelectorAll('a'));
          const exactImOrigin = url =>
            url.protocol === 'https:' &&
            url.hostname === 'im.matholic.com' &&
            url.port === '' &&
            url.username === '' &&
            url.password === '';
          const pathOf = el => {
            try {
              const url = new URL(el.href, location.href);
              return exactImOrigin(url) && url.hash === '' ? url.pathname : '';
            } catch (_) { return ''; }
          };
          const page = new URL(location.href);
          if (!exactImOrigin(page) || page.hash !== '') {
            return JSON.stringify({ version: '${CONTRACT_VERSION}', ok: false });
          }
          const userInfo = anchors.filter(el => pathOf(el) === '/userInfo');
          const accessLog = anchors.filter(el => pathOf(el) === '/userAccessLog');
          if (userInfo.length !== 1 || accessLog.length !== 1) {
            return JSON.stringify({ version: '${CONTRACT_VERSION}', ok: false });
          }
          let submenu = userInfo[0].parentElement;
          while (submenu && !submenu.contains(accessLog[0])) submenu = submenu.parentElement;
          const wrapper = submenu ? submenu.parentElement : null;
          const trigger = wrapper ? Array.from(wrapper.children).find(el =>
            el !== submenu && el.querySelector('svg') && (el.innerText || '').trim().length > 0
          ) : null;
          if (!trigger) return JSON.stringify({ version: '${CONTRACT_VERSION}', ok: false });
          const interactive = Array.from(
            trigger.querySelectorAll('[aria-haspopup="true"],button,a,[role="button"]')
          ).filter(el => (el.innerText || '').trim().length > 0 || el.querySelector('svg'));
          const svgs = Array.from(trigger.querySelectorAll('svg'));
          const target = interactive.length === 1 ? interactive[0] :
            (svgs.length === 1 ? svgs[0] : trigger);
          target.dispatchEvent(new MouseEvent('click', {
            bubbles: true, cancelable: true, view: window
          }));
          return JSON.stringify({ version: '${CONTRACT_VERSION}', ok: true });
        })()
        """.trimIndent()

    val clickLogout: String =
        """
        (() => {
          const anchors = Array.from(document.querySelectorAll('a'));
          const exactImOrigin = url =>
            url.protocol === 'https:' &&
            url.hostname === 'im.matholic.com' &&
            url.port === '' &&
            url.username === '' &&
            url.password === '';
          const pathOf = el => {
            try {
              const url = new URL(el.href, location.href);
              return exactImOrigin(url) && url.hash === '' ? url.pathname : '';
            } catch (_) { return ''; }
          };
          const page = new URL(location.href);
          if (!exactImOrigin(page) || page.hash !== '') {
            return JSON.stringify({
              version: '${CONTRACT_VERSION}', ok: false, count: -1,
              exactAllCount: -1, visibleExactCount: -1, leafExactCount: -1,
              submenuVisible: false, usedHiddenFallback: false
            });
          }
          const userInfo = anchors.filter(el => pathOf(el) === '/userInfo');
          const accessLog = anchors.filter(el => pathOf(el) === '/userAccessLog');
          if (userInfo.length !== 1 || accessLog.length !== 1) {
            return JSON.stringify({
              version: '${CONTRACT_VERSION}', ok: false, count: -1,
              exactAllCount: -1, visibleExactCount: -1, leafExactCount: -1,
              submenuVisible: false, usedHiddenFallback: false
            });
          }
          let submenu = userInfo[0].parentElement;
          while (submenu && !submenu.contains(accessLog[0])) submenu = submenu.parentElement;
          if (!submenu) {
            return JSON.stringify({
              version: '${CONTRACT_VERSION}', ok: false, count: -1,
              exactAllCount: -1, visibleExactCount: -1, leafExactCount: -1,
              submenuVisible: false, usedHiddenFallback: false
            });
          }
          const normalizedText = el => (el.textContent || '').normalize('NFKC').trim().replace(/\s+/g, ' ');
          const visible = el => {
            const style = getComputedStyle(el);
            return style.display !== 'none' && style.visibility !== 'hidden' &&
              (el.offsetWidth > 0 || el.offsetHeight > 0);
          };
          const elements = [submenu, ...Array.from(submenu.querySelectorAll('*'))];
          const exactAll = elements.filter(el => normalizedText(el) === '로그아웃');
          const exactVisible = exactAll.filter(visible);
          const leafExact = exactAll.filter(el =>
            !Array.from(el.children).some(child =>
              normalizedText(child) === '로그아웃'
            )
          );
          const visibleCandidates = leafExact.filter(visible);
          const hiddenFallbackAllowed = visibleCandidates.length === 0 && leafExact.length === 1;
          const candidates = visibleCandidates.length === 1 ? visibleCandidates :
            (hiddenFallbackAllowed ? leafExact : []);
          if (candidates.length !== 1) {
            return JSON.stringify({
              version: '${CONTRACT_VERSION}', ok: false, count: candidates.length,
              exactAllCount: exactAll.length, visibleExactCount: exactVisible.length,
              leafExactCount: leafExact.length, submenuVisible: visible(submenu),
              usedHiddenFallback: false
            });
          }
          candidates[0].click();
          return JSON.stringify({
            version: '${CONTRACT_VERSION}', ok: true, count: candidates.length,
            exactAllCount: exactAll.length, visibleExactCount: exactVisible.length,
            leafExactCount: leafExact.length, submenuVisible: visible(submenu),
            usedHiddenFallback: hiddenFallbackAllowed
          });
        })()
        """.trimIndent()

    val applyStudentExperience: String =
        """
        (() => {
          const version = '${CONTRACT_VERSION}';
          const safeStudentPath = value => {
            if (!value || /%(?:2e|2f|5c|25)/i.test(value) ||
                value.includes('\\') || /[\u0000-\u001f\u007f]/.test(value)) {
              return null;
            }
            const segments = value.split('/');
            return segments.some(segment => segment === '.' || segment === '..') ?
              null : value;
          };
          const rawPath = location.pathname || '';
          const path = safeStudentPath(rawPath);
          let page = null;
          try { page = new URL(location.href); } catch (_) {}
          const isWorkbook = path !== null &&
            (path === '/workbook' || path.startsWith('/workbook/'));
          const isDiagnostic = path !== null &&
            (path === '/diagnostic' || path.startsWith('/diagnostic/'));
          const isLearning = path !== null && path.startsWith('/learningV2/');
          const allowed = page !== null &&
            page.protocol === 'https:' &&
            page.hostname === 'im.matholic.com' &&
            page.port === '' &&
            page.username === '' &&
            page.password === '' &&
            page.hash === '' &&
            path !== null &&
            (isWorkbook || isDiagnostic || isLearning);
          if (!allowed) {
            return JSON.stringify({
              version, ok: false, path: rawPath, listPage: false,
              learningPage: false, enhancedButtons: 0
            });
          }

          const normalize = value =>
            (value || '').normalize('NFKC').trim().replace(/\s+/g, ' ');
          const visible = element => {
            const style = getComputedStyle(element);
            return style.display !== 'none' && style.visibility !== 'hidden' &&
              (element.offsetWidth > 0 || element.offsetHeight > 0);
          };
          const important = (element, name, value) => {
            if (element.style.getPropertyValue(name) !== value ||
                element.style.getPropertyPriority(name) !== 'important') {
              element.style.setProperty(name, value, 'important');
            }
          };
          const hide = element => {
            if (!element || element === document.body || element === document.documentElement) {
              return false;
            }
            important(element, 'display', 'none');
            if (element.getAttribute('aria-hidden') !== 'true') {
              element.setAttribute('aria-hidden', 'true');
            }
            return true;
          };
          const loadRuntimeScript = (id, source, ready) => {
            if (ready()) return Promise.resolve(true);
            return new Promise(resolve => {
              let settled = false;
              let poll = 0;
              let timeout = 0;
              const finish = result => {
                if (settled) return;
                settled = true;
                clearInterval(poll);
                clearTimeout(timeout);
                resolve(result);
              };
              let script = document.getElementById(id);
              if (!script) {
                script = document.createElement('script');
                script.id = id;
                script.src = source;
                script.async = false;
                (document.head || document.documentElement).appendChild(script);
              }
              const check = () => {
                if (ready()) finish(true);
              };
              script.addEventListener('load', check, { once: true });
              script.addEventListener(
                'error',
                () => finish(false),
                { once: true }
              );
              poll = setInterval(check, 50);
              timeout = setTimeout(() => finish(ready()), 8000);
              check();
            });
          };
          const ensureMathQuillRuntime = () => {
            if (window.MathQuill) return Promise.resolve(true);
            if (window.__matholicKioskMathQuillRuntimePromise) {
              return window.__matholicKioskMathQuillRuntimePromise;
            }
            const stylesheetId = 'matholic-kiosk-mathquill-style';
            if (!document.getElementById(stylesheetId)) {
              const stylesheet = document.createElement('link');
              stylesheet.id = stylesheetId;
              stylesheet.rel = 'stylesheet';
              stylesheet.href = '/js/mathquill/mathquill.css';
              (document.head || document.documentElement)
                .appendChild(stylesheet);
            }
            const runtimePromise = loadRuntimeScript(
              'matholic-kiosk-jquery-runtime',
              '/js/mathquill/jquery-3.2.1.min.js',
              () => !!window.jQuery
            ).then(jqueryReady => {
              if (!jqueryReady) return false;
              return loadRuntimeScript(
                'matholic-kiosk-mathquill-runtime',
                '/js/mathquill/mathquill.min.js',
                () => !!window.MathQuill
              );
            }).catch(() => false);
            window.__matholicKioskMathQuillRuntimePromise = runtimePromise;
            return runtimePromise;
          };
          const hrefPath = element => {
            if (!element || !element.matches('a[href]')) return '';
            try {
              const target = new URL(element.href, location.href);
              return target.origin === page.origin ? target.pathname : '';
            } catch (_) {
              return '';
            }
          };
          const chromeTexts = new Set([
            '학습실', '학습활동', '매뉴얼', '개인정보',
            '내 교재', '로그인 정보', '로그인정보', '로그아웃'
          ]);
          const chromePaths = new Set([
            '/course', '/userInfo', '/userAccessLog'
          ]);
          const isChromeSignal = element =>
            chromeTexts.has(normalize(element.textContent)) ||
            chromePaths.has(hrefPath(element));
          let hiddenChrome = 0;
          const hiddenChromeShells = new Set();
          const hideStudentChrome = () => {
            let hidden = 0;
            const chromeSignals = Array.from(
              document.querySelectorAll('a[href],button,[role="button"]')
            ).filter(isChromeSignal);
            chromeSignals.forEach(signal => {
              let candidate = signal.closest(
                'header,nav,[role="banner"],[role="navigation"]'
              );
              let ancestor = signal;
              while (!candidate && ancestor && ancestor.parentElement) {
                ancestor = ancestor.parentElement;
                if (ancestor === document.body || ancestor === document.documentElement) break;
                const signalCount = Array.from(
                  ancestor.querySelectorAll('a[href],button,[role="button"]')
                ).filter(isChromeSignal).length;
                if (
                  signalCount >= 3 &&
                  !ancestor.querySelector('main,[role="main"]')
                ) {
                  candidate = ancestor;
                  break;
                }
              }
              if (candidate && hide(candidate)) {
                if (!hiddenChromeShells.has(candidate)) {
                  hiddenChromeShells.add(candidate);
                  hidden += 1;
                }
              }
            });
            return hidden;
          };
          hiddenChrome += hideStudentChrome();

          let style = document.getElementById('matholic-kiosk-student-style');
          if (!style) {
            style = document.createElement('style');
            style.id = 'matholic-kiosk-student-style';
            (document.head || document.documentElement).appendChild(style);
          }
          style.textContent = isLearning ? `
            html, body {
              min-height: 100% !important;
              width: 100% !important;
              max-width: 100% !important;
              overflow-x: hidden !important;
              overscroll-behavior-x: none !important;
            }
            header, [role="banner"] { display: none !important; }
            *, *::before, *::after { box-sizing: border-box !important; }
            body {
              box-sizing: border-box !important;
              width: auto !important;
              max-width: 100vw !important;
              overflow-x: hidden !important;
              padding-top: 36px !important;
              padding-bottom: 0 !important;
            }
            main {
              max-width: 100% !important;
              overflow-x: hidden !important;
              padding-top: 24px !important;
            }
            #root[data-matholic-kiosk-long-page-scroll="true"] {
              overflow-y: auto !important;
              overflow-x: hidden !important;
              overscroll-behavior-y: contain !important;
              touch-action: pan-y !important;
              -webkit-overflow-scrolling: touch !important;
            }
            #root[data-matholic-kiosk-long-page-scroll="true"]::after {
              content: "" !important;
              display: block !important;
              width: 1px !important;
              height: var(--matholic-kiosk-long-page-extra, 0px) !important;
              pointer-events: none !important;
            }
            [data-matholic-kiosk-long-page-media="true"] {
              touch-action: pan-y !important;
              pointer-events: auto !important;
            }
            [title*="답안 필기"],
            [aria-label*="답안 필기"],
            .ant-input-affix-wrapper:has(input[placeholder*="주관식 답"])
              .ant-input-suffix,
            img[alt="문항 동영상"],
            img[alt="대표 유형 동영상"] {
              display: none !important;
            }
            button, [role="button"] {
              min-height: 52px !important;
              max-width: 100% !important;
              padding: 10px 10px !important;
              font-size: 18px !important;
              line-height: 1.25 !important;
              touch-action: manipulation !important;
            }
            .ant-radio-group:has(.ant-radio-button-wrapper) {
              display: flex !important;
              flex-wrap: wrap !important;
              align-items: stretch !important;
              gap: 8px !important;
              width: 100% !important;
              max-width: 100% !important;
            }
            .ant-radio-group:has(.ant-radio-button-wrapper)
              .ant-radio-button-wrapper {
              display: inline-flex !important;
              align-items: center !important;
              justify-content: center !important;
              min-width: 64px !important;
              min-height: 56px !important;
              height: auto !important;
              margin-left: 0 !important;
              padding: 8px 16px !important;
              border: 2px solid #9fb3c8 !important;
              border-radius: 12px !important;
              background: #fff !important;
              color: #102a43 !important;
              font-size: 20px !important;
              font-weight: 700 !important;
              line-height: 1 !important;
              text-align: center !important;
              touch-action: manipulation !important;
            }
            .ant-radio-group:has(.ant-radio-button-wrapper)
              .ant-radio-button-wrapper::before {
              display: none !important;
            }
            .ant-radio-group:has(.ant-radio-button-wrapper)
              .ant-radio-button-wrapper-checked:not(
                .ant-radio-button-wrapper-disabled
              ) {
              border-color: #1565c0 !important;
              background: #1565c0 !important;
              color: #fff !important;
            }
            input, textarea, [contenteditable="true"] {
              min-height: 48px !important;
              min-width: 0 !important;
              max-width: 100% !important;
              font-size: 18px !important;
              touch-action: manipulation !important;
            }
            input[placeholder*="주관식 답"] {
              width: 220px !important;
              max-width: 100% !important;
            }
            input[placeholder*="주관식 답"]:not([
              data-matholic-kiosk-basic-fallback="true"
            ]),
            [id^="answer-input-form-"]:has(input[placeholder*="주관식 답"]:not([
              data-matholic-kiosk-basic-fallback="true"
            ])) button.ant-dropdown-trigger {
              opacity: 0 !important;
              pointer-events: none !important;
            }
            .mq-editable-field {
              min-width: 220px !important;
              max-width: 100% !important;
              min-height: 56px !important;
              padding: 12px 14px !important;
              font-size: 22px !important;
              line-height: 1.4 !important;
              cursor: text !important;
              touch-action: manipulation !important;
              pointer-events: auto !important;
              vertical-align: middle !important;
            }
            .mq-editable-field .mq-root-block {
              min-width: 1em !important;
              min-height: 28px !important;
            }
            .matholic-kiosk-math-nav {
              position: fixed !important;
              left: 14px !important;
              right: 218px !important;
              bottom: 12px !important;
              top: auto !important;
              z-index: 2147482500 !important;
              display: none !important;
              width: auto !important;
              max-width: none !important;
              margin: 0 !important;
              padding: 12px !important;
              border: 2px solid #9fb3c8 !important;
              border-radius: 16px !important;
              background: rgba(255, 255, 255, 0.985) !important;
              box-shadow: 0 8px 28px rgba(16, 42, 67, 0.32) !important;
              overflow: visible !important;
            }
            .matholic-kiosk-math-nav[data-matholic-kiosk-active="true"] {
              display: block !important;
            }
            html[data-matholic-kiosk-keypad-active="true"] body {
              padding-bottom: 278px !important;
            }
            .matholic-kiosk-keypad-inner {
              display: grid !important;
              grid-template-columns:
                minmax(0, 480fr) minmax(0, 270fr) minmax(0, 310fr) !important;
              gap: 12px !important;
              width: 100% !important;
              max-width: none !important;
              margin: 0 auto !important;
            }
            html[data-matholic-kiosk-keypad-preset="left"]
              .matholic-kiosk-keypad-inner {
              grid-template-columns:
                minmax(0, 310fr) minmax(0, 270fr) minmax(0, 480fr) !important;
            }
            html[data-matholic-kiosk-keypad-preset="left"]
              .matholic-kiosk-keypad-edit {
              order: 1 !important;
            }
            html[data-matholic-kiosk-keypad-preset="left"]
              .matholic-kiosk-keypad-structure {
              order: 2 !important;
            }
            html[data-matholic-kiosk-keypad-preset="left"]
              .matholic-kiosk-keypad-numeric {
              order: 3 !important;
            }
            html[data-matholic-kiosk-keypad-preset="center"]
              .matholic-kiosk-keypad-inner {
              grid-template-columns:
                minmax(0, 450fr) minmax(0, 310fr) minmax(0, 300fr) !important;
            }
            html[data-matholic-kiosk-keypad-preset="center"]
              .matholic-kiosk-keypad-numeric {
              order: 1 !important;
            }
            html[data-matholic-kiosk-keypad-preset="center"]
              .matholic-kiosk-keypad-edit {
              order: 2 !important;
            }
            html[data-matholic-kiosk-keypad-preset="center"]
              .matholic-kiosk-keypad-structure {
              order: 3 !important;
            }
            .matholic-kiosk-keypad-section {
              display: flex !important;
              flex-direction: column !important;
              min-width: 0 !important;
            }
            .matholic-kiosk-keypad-heading {
              display: flex !important;
              align-items: center !important;
              justify-content: center !important;
              width: 100% !important;
              height: 34px !important;
              min-height: 34px !important;
              border-radius: 9px !important;
              font-size: 16px !important;
              font-weight: 800 !important;
              line-height: 1 !important;
            }
            .matholic-kiosk-keypad-numeric
              .matholic-kiosk-keypad-heading {
              background: #dbeafe !important;
              color: #173f6d !important;
            }
            .matholic-kiosk-keypad-structure
              .matholic-kiosk-keypad-heading {
              background: #e8f5e9 !important;
              color: #1b5e20 !important;
            }
            .matholic-kiosk-keypad-edit
              .matholic-kiosk-keypad-heading {
              background: #fff3e0 !important;
              color: #8a4b08 !important;
            }
            .matholic-kiosk-keypad-numeric-grid,
            .matholic-kiosk-keypad-structure-grid {
              display: grid !important;
              margin-top: 8px !important;
              gap: 8px !important;
            }
            .matholic-kiosk-keypad-numeric-grid {
              grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
              grid-template-rows: repeat(3, 52px) !important;
            }
            .matholic-kiosk-keypad-structure-grid {
              grid-template-columns: minmax(0, 1fr) !important;
              grid-template-rows: repeat(3, 52px) !important;
            }
            .matholic-kiosk-keypad-edit-body {
              position: relative !important;
              height: 190px !important;
              margin-top: 8px !important;
            }
            .matholic-kiosk-keypad-arrows {
              position: absolute !important;
              top: 0 !important;
              left: 0 !important;
              right: 0 !important;
              display: grid !important;
              grid-template-columns: repeat(3, minmax(0, 1fr)) !important;
              grid-template-rows: repeat(2, 44px) !important;
              grid-template-areas:
                ". up ."
                "left down right" !important;
              gap: 7px !important;
            }
            .matholic-kiosk-keypad-actions {
              position: absolute !important;
              left: 0 !important;
              right: 0 !important;
              bottom: 0 !important;
              display: grid !important;
              grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
              grid-template-rows: repeat(2, 39px) !important;
              gap: 8px !important;
              height: 86px !important;
            }
            .matholic-kiosk-math-nav button {
              width: 100% !important;
              min-width: 0 !important;
              max-width: none !important;
              height: 100% !important;
              min-height: 0 !important;
              margin: 0 !important;
              padding: 8px !important;
              border-width: 2px !important;
              border-style: solid !important;
              border-radius: 11px !important;
              font-size: 24px !important;
              font-weight: 800 !important;
              line-height: 1 !important;
              touch-action: manipulation !important;
            }
            .matholic-kiosk-keypad-numeric-grid > button {
              border-color: #9fb3c8 !important;
              background: #f8fafc !important;
              color: #102a43 !important;
            }
            .matholic-kiosk-keypad-structure-grid > button {
              border-color: #81c784 !important;
              background: #f1f8f2 !important;
              color: #1b5e20 !important;
              font-size: 20px !important;
            }
            .matholic-kiosk-keypad-arrows > button,
            .matholic-kiosk-keypad-actions > button {
              border-color: #f59e0b !important;
              background: #fff8ed !important;
              color: #8a4b08 !important;
            }
            .matholic-kiosk-keypad-arrows > button {
              font-size: 30px !important;
            }
            .matholic-kiosk-keypad-actions > button {
              font-size: 14px !important;
            }
            .matholic-kiosk-keypad-actions > button[
              data-matholic-kiosk-clear-armed="true"
            ] {
              border-color: #c2410c !important;
              background: #ffedd5 !important;
              color: #9a3412 !important;
              font-size: 17px !important;
            }
            .matholic-kiosk-problem-number {
              position: relative !important;
              top: auto !important;
              left: auto !important;
              z-index: 2 !important;
              display: flex !important;
              align-items: center !important;
              justify-content: center !important;
              flex: 0 0 auto !important;
              gap: 0 !important;
              width: 100% !important;
              min-width: 0 !important;
              min-height: 62px !important;
              margin: 0 !important;
              padding: 8px 6px !important;
              border: 2px solid #173f6d !important;
              border-radius: 16px !important;
              background: #173f6d !important;
              color: #fff !important;
              box-shadow: 0 6px 18px rgba(16, 42, 67, 0.28) !important;
              font-size: 0 !important;
              font-weight: 800 !important;
              line-height: 1 !important;
              cursor: pointer !important;
              transform: none !important;
              touch-action: manipulation !important;
            }
            .matholic-kiosk-problem-number::before {
              content: attr(data-matholic-kiosk-label) !important;
              color: #fff !important;
              font-size: 20px !important;
              font-weight: 800 !important;
              line-height: 1 !important;
              white-space: pre !important;
              pointer-events: none !important;
            }
            .matholic-kiosk-problem-number > * {
              position: absolute !important;
              inset: 0 !important;
              width: 100% !important;
              height: 100% !important;
              opacity: 0 !important;
              pointer-events: none !important;
            }
            .matholic-kiosk-problem-number .ant-select,
            .matholic-kiosk-problem-number .ant-select-selector,
            .matholic-kiosk-problem-number [role="combobox"],
            .matholic-kiosk-problem-number select {
              min-width: 100% !important;
              min-height: 100% !important;
              border: 0 !important;
              background: transparent !important;
              color: #fff !important;
              box-shadow: none !important;
              font-size: 28px !important;
              font-weight: 800 !important;
              line-height: 1 !important;
              cursor: pointer !important;
            }
            .matholic-kiosk-problem-number .ant-select-selection-item,
            .matholic-kiosk-problem-number .ant-select-arrow {
              color: #fff !important;
              font-size: 28px !important;
              font-weight: 800 !important;
            }
            [data-matholic-kiosk-problem-navigation="true"] {
              display: grid !important;
              grid-template-columns: 52px minmax(0, 1fr) 52px !important;
              align-items: center !important;
              width: 100% !important;
              max-width: 100% !important;
              justify-content: stretch !important;
              gap: 6px !important;
              padding-inline: 0 !important;
              overflow: visible !important;
              box-sizing: border-box !important;
            }
            html[data-matholic-kiosk-direct-problem-select="true"]
              .ant-select-dropdown {
              opacity: 0 !important;
            }
            [data-matholic-kiosk-problem-direction-host] {
              width: 52px !important;
              min-width: 52px !important;
              max-width: 52px !important;
              overflow: visible !important;
            }
            [data-matholic-kiosk-problem-direction] {
              width: 52px !important;
              min-width: 52px !important;
              max-width: 52px !important;
              height: 58px !important;
              min-height: 58px !important;
              max-height: 58px !important;
              padding: 6px !important;
              border: 2px solid #9fb3c8 !important;
              border-radius: 14px !important;
              background: #fff !important;
              color: #173f6d !important;
              font-size: 28px !important;
              line-height: 1 !important;
              touch-action: manipulation !important;
              box-sizing: border-box !important;
            }
            [data-matholic-kiosk-problem-direction] svg {
              width: 30px !important;
              height: 30px !important;
            }
            [data-matholic-kiosk-problem-navigation="true"][
              data-matholic-kiosk-single-problem="true"
            ] [data-matholic-kiosk-problem-direction] {
              visibility: hidden !important;
              pointer-events: none !important;
            }
            .matholic-kiosk-problem-map {
              position: relative !important;
              top: auto !important;
              right: auto !important;
              z-index: 2 !important;
              width: min(318px, calc(100% - 24px)) !important;
              max-width: calc(100% - 24px) !important;
              margin: 10px auto 18px !important;
              padding: 10px !important;
              border: 1px solid #c7d4e3 !important;
              border-radius: 16px !important;
              background: rgba(255, 255, 255, 0.98) !important;
              box-shadow: 0 4px 12px rgba(16, 42, 67, 0.14) !important;
            }
            .matholic-kiosk-problem-map > button {
              width: 100% !important;
              min-height: 52px !important;
              padding: 8px 12px !important;
              border: 0 !important;
              border-radius: 10px !important;
              background: #173f6d !important;
              color: #fff !important;
              box-shadow: none !important;
              font-size: 18px !important;
              font-weight: 800 !important;
              appearance: none !important;
            }
            .matholic-kiosk-problem-map-grid {
              display: none !important;
              grid-template-columns: repeat(6, 1fr) !important;
              gap: 6px !important;
              padding-top: 8px !important;
            }
            .matholic-kiosk-problem-map[data-open="true"]
              .matholic-kiosk-problem-map-grid {
              display: grid !important;
            }
            .matholic-kiosk-problem-map-grid > button {
              min-width: 0 !important;
              min-height: 44px !important;
              padding: 4px !important;
              border: 2px solid #9fb3c8 !important;
              border-radius: 10px !important;
              background: #fff !important;
              color: #102a43 !important;
              font-size: 17px !important;
              font-weight: 800 !important;
            }
            .matholic-kiosk-problem-map-grid > button[data-state="answered"] {
              border-color: #0d47a1 !important;
              background: #1565c0 !important;
              color: #fff !important;
            }
            .matholic-kiosk-problem-map-grid > button[data-state="unknown"] {
              border-color: #4b5563 !important;
              background: #6b7280 !important;
              color: #fff !important;
            }
            .matholic-kiosk-problem-map-grid > button[data-current="true"] {
              outline: 4px solid #f59e0b !important;
              outline-offset: 1px !important;
            }
            .matholic-kiosk-problem-map-legend {
              display: none !important;
              padding-top: 8px !important;
              color: #334e68 !important;
              font-size: 13px !important;
              font-weight: 700 !important;
              text-align: center !important;
            }
            .matholic-kiosk-problem-map-legend > span {
              display: inline-flex !important;
              align-items: center !important;
              gap: 4px !important;
              margin: 0 6px !important;
            }
            .matholic-kiosk-problem-map-legend > span::before {
              content: '' !important;
              width: 12px !important;
              height: 12px !important;
              border-radius: 3px !important;
              border: 2px solid #9fb3c8 !important;
              background: #fff !important;
            }
            .matholic-kiosk-problem-map-legend
              > span[data-state="answered"]::before {
              border-color: #0d47a1 !important;
              background: #1565c0 !important;
            }
            .matholic-kiosk-problem-map-legend
              > span[data-state="unknown"]::before {
              border-color: #4b5563 !important;
              background: #6b7280 !important;
            }
            .matholic-kiosk-problem-map-legend
              > span[data-state="current"]::before {
              outline: 3px solid #f59e0b !important;
              outline-offset: 1px !important;
            }
            .matholic-kiosk-problem-map[data-open="true"]
              .matholic-kiosk-problem-map-legend {
              display: block !important;
            }
            .matholic-kiosk-problem-map-unanswered {
              display: none !important;
              grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
              gap: 8px !important;
              padding-top: 8px !important;
            }
            .matholic-kiosk-problem-map[data-open="true"]
              .matholic-kiosk-problem-map-unanswered {
              display: grid !important;
            }
            .matholic-kiosk-problem-map-unanswered > button {
              min-height: clamp(52px, 7vh, 64px) !important;
              padding: 8px !important;
              border: 2px solid #1565c0 !important;
              border-radius: 10px !important;
              background: #eef7ff !important;
              color: #173f6d !important;
              font-size: 16px !important;
              font-weight: 800 !important;
              touch-action: manipulation !important;
            }
          ` : `
            header, nav, [role="navigation"] {
              display: none !important;
            }
            main { margin-top: 0 !important; padding-top: 16px !important; }
          `;

          const localizeEmptyListState = () => {
            const message = isDiagnostic ?
              '진단평가가 없습니다' :
              isWorkbook ? '학습지가 없습니다' : '';
            if (!message) return 0;
            let localized = 0;
            Array.from(document.querySelectorAll(
              '.ant-empty-description'
            )).filter(visible).forEach(element => {
              const text = normalize(element.textContent).toLowerCase();
              if (text !== '' && text !== 'no data') return;
              if (normalize(element.textContent) !== message) {
                element.textContent = message;
              }
              localized += 1;
            });
            return localized;
          };

          let enhancedButtons = 0;
          let hiddenControls = 0;
          let emptyStateLocalizations = localizeEmptyListState();
          let mathModeSelections = 0;
          let mathModePending = 0;
          let mathModeReady = 0;
          let mathModeRemounted = 0;
          let subjectiveTouchTargets = 0;
          let problemNavigationEnhancements = 0;
          let problemStateMapEnhancements = 0;
          let longProblemScrollEnhancements = 0;
          let mathKeypadEnhancements = 0;
          if (isLearning) {
            const mathQuillRuntimePromise = ensureMathQuillRuntime();
            const navigationDirection = control => {
              const text = normalize(control.textContent);
              const identity = normalize([
                text,
                control.getAttribute('aria-label') || '',
                control.getAttribute('title') || ''
              ].join(' ')).toLowerCase();
              if (
                ['<', '‹', '〈', '이전', '이전 문제'].includes(text) ||
                identity.includes('이전 문제') ||
                identity.includes('previous problem')
              ) return 'previous';
              if (
                ['>', '›', '〉', '다음', '다음 문제'].includes(text) ||
                identity.includes('다음 문제') ||
                identity.includes('next problem')
              ) return 'next';
              return null;
            };
            const directChildInside = (container, descendant) => {
              let candidate = descendant;
              while (
                candidate?.parentElement &&
                candidate.parentElement !== container
              ) {
                candidate = candidate.parentElement;
              }
              return candidate?.parentElement === container ? candidate : null;
            };
            const applyProblemNavigation = (
              navigation,
              previous,
              next,
              numberCluster,
              selectorRoot
            ) => {
              if (
                !navigation ||
                !previous ||
                !next ||
                previous === next ||
                !numberCluster ||
                numberCluster.contains(previous) ||
                numberCluster.contains(next)
              ) return false;
              navigation.dataset.matholicKioskProblemNavigation = 'true';
              const previousHost = directChildInside(navigation, previous);
              const nextHost = directChildInside(navigation, next);
              if (previousHost) {
                previousHost.dataset.matholicKioskProblemDirectionHost =
                  'previous';
              }
              if (nextHost) {
                nextHost.dataset.matholicKioskProblemDirectionHost = 'next';
              }
              [
                [previous, 'previous', '이전 문제'],
                [next, 'next', '다음 문제']
              ].forEach(([button, direction, label]) => {
                button.dataset.matholicKioskProblemDirection = direction;
                button.setAttribute('aria-label', label);
                button.setAttribute('title', label);
                important(button, 'width', '52px');
                important(button, 'min-width', '52px');
                important(button, 'max-width', '52px');
                important(button, 'height', '58px');
                important(button, 'min-height', '58px');
                important(button, 'max-height', '58px');
                if (
                  button.dataset
                    .matholicKioskDirectionFeedbackBound !== 'true'
                ) {
                  button.addEventListener('click', () => {
                    const selected = readCurrentProblemNumber();
                    const tokens = normalize(
                      currentProblemNumberCluster()?.textContent
                    ).match(/\d+/g) || [];
                    const total = Number(tokens[tokens.length - 1] || '0');
                    const target = direction === 'next' ?
                      selected + 1 : selected - 1;
                    if (
                      !Number.isInteger(selected) || selected < 1 ||
                      !Number.isInteger(total) || total < 1 ||
                      target < 1 || target > total
                    ) return;
                    window.__matholicKioskDirectionFeedback = {
                      selected,
                      target,
                      startedAt: Date.now()
                    };
                    updateProblemNumberLabel(selected, total, target);
                  });
                  button.dataset.matholicKioskDirectionFeedbackBound =
                    'true';
                }
              });
              numberCluster.classList.add(
                'matholic-kiosk-problem-number'
              );
              numberCluster.setAttribute(
                'aria-label',
                '문제 목록 열기'
              );
              const selectorSurface = numberCluster.querySelector(
                '.ant-select-selector,[role="combobox"],select'
              ) || selectorRoot;
              selectorSurface.setAttribute(
                'aria-label',
                '이동할 문제 번호'
              );
              if (
                numberCluster.dataset
                  .matholicKioskProblemNumberBound !== 'true'
              ) {
                numberCluster.addEventListener('click', event => {
                  const problemMap = document.querySelector(
                    '.matholic-kiosk-problem-map'
                  );
                  if (problemMap) {
                    event.preventDefault();
                    event.stopPropagation();
                    problemMap.dataset.open =
                      problemMap.dataset.open === 'true' ? 'false' : 'true';
                    return;
                  }
                  if (
                    event.target?.closest?.(
                      '.ant-select-selector,[role="combobox"],select'
                    )
                  ) return;
                  const target = numberCluster.querySelector(
                    '.ant-select-selector,[role="combobox"],select'
                  ) || selectorRoot;
                  target.click();
                }, true);
                numberCluster.dataset
                  .matholicKioskProblemNumberBound = 'true';
              }
              const modalOpen = Array.from(
                document.querySelectorAll(
                  '.ant-modal-wrap,.ant-modal[role="dialog"]'
                )
              ).some(visible);
              important(
                numberCluster,
                'visibility',
                modalOpen ? 'hidden' : 'visible'
              );
              problemNavigationEnhancements = Math.max(
                problemNavigationEnhancements,
                3
              );
              return true;
            };
            const visibleButtonInside = element => {
              if (!element) return null;
              if (
                element.matches('button,[role="button"]') &&
                visible(element)
              ) return element;
              return Array.from(
                element.querySelectorAll('button,[role="button"]')
              ).find(visible) || null;
            };
            const enhanceProblemNavigation = () => {
              const selectorRoots = Array.from(
                document.querySelectorAll(
                  '.ant-select,select,[role="combobox"]'
                )
              ).filter(visible);
              for (const selectorRoot of selectorRoots) {
                const selectedItem = selectorRoot.querySelector(
                  '.ant-select-selection-item'
                );
                const selectedText = normalize(
                  selectedItem?.textContent ||
                  selectorRoot.value ||
                  selectorRoot.textContent
                );
                if (!/^\d+$/.test(selectedText)) continue;
                let navigation = selectorRoot.parentElement;
                for (
                  let depth = 0;
                  navigation &&
                    navigation !== document.body &&
                    navigation !== document.documentElement &&
                    depth < 6;
                  depth += 1
                ) {
                  const numberCluster = directChildInside(
                    navigation,
                    selectorRoot
                  );
                  const children = Array.from(navigation.children);
                  const clusterIndex = children.indexOf(numberCluster);
                  const numberTokens = normalize(
                    numberCluster?.textContent
                  ).match(/\d+/g) || [];
                  if (clusterIndex > 0 && numberTokens.length >= 2) {
                    const previous = children
                      .slice(0, clusterIndex)
                      .reverse()
                      .map(visibleButtonInside)
                      .find(Boolean);
                    const next = children
                      .slice(clusterIndex + 1)
                      .map(visibleButtonInside)
                      .find(Boolean);
                    if (
                      applyProblemNavigation(
                        navigation,
                        previous,
                        next,
                        numberCluster,
                        selectorRoot
                      )
                    ) return;
                  }
                  navigation = navigation.parentElement;
                }
              }

              const controls = Array.from(
                document.querySelectorAll('button,[role="button"]')
              ).filter(visible);
              const previousControls = controls.filter(
                control => navigationDirection(control) === 'previous'
              );
              const nextControls = controls.filter(
                control => navigationDirection(control) === 'next'
              );
              for (const previous of previousControls) {
                let navigation = previous.parentElement;
                for (
                  let depth = 0;
                  navigation &&
                    navigation !== document.body &&
                    navigation !== document.documentElement &&
                    depth < 6;
                  depth += 1
                ) {
                  const next = nextControls.find(control =>
                    navigation.contains(control)
                  );
                  const selectorRoot = navigation.querySelector(
                    '.ant-select,select,[role="combobox"]'
                  );
                  if (next && selectorRoot) {
                    const previousChild = directChildInside(navigation, previous);
                    const nextChild = directChildInside(navigation, next);
                    const numberCluster = directChildInside(
                      navigation,
                      selectorRoot
                    );
                    if (
                      previousChild &&
                      nextChild &&
                      numberCluster &&
                      numberCluster !== previousChild &&
                      numberCluster !== nextChild
                    ) {
                      if (
                        applyProblemNavigation(
                          navigation,
                          previous,
                          next,
                          numberCluster,
                          selectorRoot
                        )
                      ) return;
                    }
                  }
                  navigation = navigation.parentElement;
                }
              }
            };
            enhanceProblemNavigation();
            const problemNumberCluster = document.querySelector(
              '.matholic-kiosk-problem-number'
            );
            const currentProblemNumberCluster = () => document.querySelector(
              '.matholic-kiosk-problem-number'
            );
            const currentProblemSelectorRoot = () => {
              const cluster = currentProblemNumberCluster();
              return cluster?.querySelector(
                '.ant-select,select,[role="combobox"]'
              ) || null;
            };
            const readCurrentProblemNumber = () => Number(normalize(
                document.querySelector(
                  '.matholic-kiosk-problem-number .ant-select-selection-item'
                )?.textContent ||
                document.querySelector(
                  '.matholic-kiosk-problem-number select'
                )?.value ||
                document.querySelector(
                  '.matholic-kiosk-problem-number [role="combobox"]'
                )?.textContent
              ).match(/\d+/)?.[0] || '0');
            const ensureLongProblemTouchGuard = () => {
              if (window.__matholicKioskLongPageScrollController) return;
              const controller = {
                root: null,
                startY: 0,
                startScrollTop: 0,
                start(root, clientY) {
                  if (!root || !Number.isFinite(clientY)) return false;
                  this.root = root;
                  this.startY = clientY;
                  this.startScrollTop = root.scrollTop;
                  return true;
                },
                move(clientY) {
                  if (!this.root || !Number.isFinite(clientY)) return false;
                  this.root.scrollTop =
                    this.startScrollTop + this.startY - clientY;
                  return true;
                },
                end() {
                  this.root = null;
                }
              };
              document.addEventListener('touchstart', event => {
                const media = event.target?.closest?.(
                  '[data-matholic-kiosk-long-page-media="true"]'
                );
                const root = media?.closest?.(
                  '#root[data-matholic-kiosk-long-page-scroll="true"]'
                );
                if (!root || event.touches.length !== 1) return;
                controller.start(root, event.touches[0].clientY);
              }, { capture: true, passive: false });
              document.addEventListener('touchmove', event => {
                if (
                  event.touches.length !== 1 ||
                  !controller.move(event.touches[0].clientY)
                ) return;
                event.preventDefault();
                event.stopImmediatePropagation();
              }, { capture: true, passive: false });
              const finishTouch = () => controller.end();
              document.addEventListener(
                'touchend',
                finishTouch,
                { capture: true, passive: true }
              );
              document.addEventListener(
                'touchcancel',
                finishTouch,
                { capture: true, passive: true }
              );
              window.__matholicKioskLongPageScrollController = controller;
            };
            ensureLongProblemTouchGuard();
            const enhanceLongProblemScrolling = () => {
              const current = readCurrentProblemNumber();
              document.querySelectorAll(
                '[data-matholic-kiosk-long-page-media="true"]'
              ).forEach(element => {
                delete element.dataset.matholicKioskLongPageMedia;
              });
              const root = document.getElementById('root');
              if (!root) return 0;
              const previousProblem = Number(
                root.dataset.matholicKioskLongPageProblemNumber || '0'
              );
              if (Number.isInteger(current) && current > 0) {
                if (previousProblem !== current) root.scrollTop = 0;
                root.dataset.matholicKioskLongPageProblemNumber =
                  String(current);
              }
              const problemImages = Array.from(
                root.querySelectorAll('picture.no-select > img.no-select')
              ).filter(image => {
                if (!visible(image)) return false;
                const rect = image.getBoundingClientRect();
                if (rect.width < 300 || rect.height < 300) return false;
                try {
                  return new URL(image.currentSrc || image.src, location.href)
                    .hostname === 'image.matholic.com';
                } catch (_) {
                  return false;
                }
              });
              const clippedImage = problemImages
                .filter(image =>
                  image.getBoundingClientRect().bottom + root.scrollTop >
                    window.innerHeight + 16
                )
                .sort((left, right) =>
                  right.getBoundingClientRect().bottom + root.scrollTop -
                    (left.getBoundingClientRect().bottom + root.scrollTop)
                )[0] || null;
              if (!clippedImage) {
                delete root.dataset.matholicKioskLongPageScroll;
                delete root.dataset.matholicKioskLongPageExtra;
                root.style.removeProperty(
                  '--matholic-kiosk-long-page-extra'
                );
                return 0;
              }
              const previousExtra = Number(
                root.dataset.matholicKioskLongPageExtra || '0'
              );
              const baseMaxScroll = Math.max(
                0,
                root.scrollHeight - root.clientHeight - previousExtra
              );
              const requiredMaxScroll = Math.max(
                0,
                Math.ceil(
                  clippedImage.getBoundingClientRect().bottom -
                    window.innerHeight + 24 + root.scrollTop
                )
              );
              const extra = Math.max(
                0,
                requiredMaxScroll - baseMaxScroll
              );
              root.dataset.matholicKioskLongPageScroll = 'true';
              root.dataset.matholicKioskLongPageExtra = String(extra);
              const extraCss = `${'$'}{extra}px`;
              if (
                root.style.getPropertyValue(
                  '--matholic-kiosk-long-page-extra'
                ) !== extraCss
              ) {
                root.style.setProperty(
                  '--matholic-kiosk-long-page-extra',
                  extraCss
                );
              }
              clippedImage.dataset.matholicKioskLongPageMedia = 'true';
              const picture = clippedImage.closest('picture.no-select');
              if (picture) {
                picture.dataset.matholicKioskLongPageMedia = 'true';
              }
              return 1;
            };
            const currentProblemNumber = readCurrentProblemNumber();
            const numberTokens = normalize(
              problemNumberCluster?.textContent
            ).match(/\d+/g) || [];
            const totalProblems = Number(
              numberTokens[numberTokens.length - 1] || '0'
            );
            const updateProblemNumberLabel = (current, total, target = 0) => {
              const cluster = currentProblemNumberCluster();
              if (
                !cluster ||
                !Number.isInteger(current) ||
                current < 1 ||
                !Number.isInteger(total) ||
                total < 1
              ) return;
              const moving = Number.isInteger(target) &&
                target >= 1 && target <= total && target !== current;
              const navigation = cluster.closest(
                '[data-matholic-kiosk-problem-navigation="true"]'
              );
              if (navigation) {
                navigation.dataset.matholicKioskSingleProblem =
                  total === 1 ? 'true' : 'false';
              }
              cluster.dataset.matholicKioskLabel = moving
                ? `${'$'}{current} →${'$'}{target}/${'$'}{total}`
                : total > 1
                  ? `${'$'}{current} ↓/${'$'}{total}`
                  : `${'$'}{current}/${'$'}{total}`;
              if (moving) {
                cluster.setAttribute('aria-busy', 'true');
              } else {
                cluster.removeAttribute('aria-busy');
              }
            };
            if (
              problemNumberCluster &&
              Number.isInteger(currentProblemNumber) &&
              currentProblemNumber > 0 &&
              Number.isInteger(totalProblems) &&
              totalProblems > 0
            ) {
              const directionFeedback =
                window.__matholicKioskDirectionFeedback;
              let directionTarget = 0;
              if (
                directionFeedback &&
                Date.now() - Number(directionFeedback.startedAt || 0) <=
                  5_000 &&
                Number(directionFeedback.selected) === currentProblemNumber &&
                Number(directionFeedback.target) !== currentProblemNumber
              ) {
                directionTarget = Number(directionFeedback.target);
              } else if (directionFeedback) {
                delete window.__matholicKioskDirectionFeedback;
              }
              updateProblemNumberLabel(
                currentProblemNumber,
                totalProblems,
                Number(
                  window.__matholicKioskProblemNavigationController?.target || 0
                ) || directionTarget
              );
            }
            const problemStates =
              window.__matholicKioskProblemStates ||
              (window.__matholicKioskProblemStates = {});
            const answerState = scope => {
              if (!scope) return 'unanswered';
              const unknownControl = Array.from(scope.querySelectorAll(
                'button,[role="button"]'
              )).find(control => {
                if (normalize(control.textContent) !== '모름') return false;
                return control.getAttribute('aria-pressed') === 'true' ||
                  control.getAttribute('aria-selected') === 'true' ||
                  /(?:^|\s)(?:active|selected|checked)(?:\s|${'$'})/i.test(
                    control.className || ''
                  );
              });
              if (unknownControl) return 'unknown';
              const valuedInput = Array.from(scope.querySelectorAll(
                'input:not([type="hidden"]):not([type="radio"]):not([type="checkbox"]),textarea'
              )).some(input => normalize(input.value).length > 0);
              const checkedChoice = !!scope.querySelector(
                'input:checked,.ant-radio-checked,.ant-checkbox-checked,' +
                '[aria-checked="true"]'
              );
              const mathValue = Array.from(scope.querySelectorAll(
                '.mq-editable-field'
              )).some(editor => {
                const field = (() => {
                  try {
                    return window.MathQuill?.getInterface?.(2)?.(editor) ||
                      window.MathQuill?.getInterface?.(1)?.(editor);
                  } catch (_) {
                    return null;
                  }
                })();
                try {
                  return normalize(field?.latex?.()).length > 0;
                } catch (_) {
                  return normalize(editor.textContent).length > 0;
                }
              });
              return valuedInput || checkedChoice || mathValue ?
                'answered' : 'unanswered';
            };
            const currentAnswerScope = Array.from(document.querySelectorAll(
              '[id^="answer-input-form-"]'
            )).find(visible) || document.querySelector('main') || document.body;
            if (
              Number.isInteger(currentProblemNumber) &&
              currentProblemNumber > 0 &&
              currentProblemNumber <= 999
            ) {
              const detected = answerState(currentAnswerScope);
              if (
                problemStates[currentProblemNumber] !== 'unknown' ||
                detected === 'unknown'
              ) {
                problemStates[currentProblemNumber] = detected;
              }
            }
            const reviewForms = Array.from(document.querySelectorAll(
              '[id^="answer-input-form-"]'
            )).filter(visible);
            if (reviewForms.length > 1) {
              reviewForms.forEach((form, index) => {
                problemStates[index + 1] = answerState(form);
              });
            }
            const selectProblemDirectly = number => {
              const liveSelectorRoot = currentProblemSelectorRoot();
              const liveNumberCluster = currentProblemNumberCluster();
              const nativeSelect = liveSelectorRoot?.matches?.('select') ?
                liveSelectorRoot :
                liveSelectorRoot?.querySelector?.('select');
              if (nativeSelect) {
                const option = Array.from(nativeSelect.options).find(candidate =>
                  Number(normalize(candidate.textContent)) === number
                );
                if (!option) return;
                nativeSelect.value = option.value;
                nativeSelect.dispatchEvent(new Event('input', { bubbles: true }));
                nativeSelect.dispatchEvent(new Event('change', { bubbles: true }));
                return true;
              }
              const surface = liveNumberCluster?.querySelector(
                '.ant-select-selector,[role="combobox"]'
              ) || liveSelectorRoot;
              if (!surface) return false;
              const reactPropsKey = Object.keys(surface).find(key =>
                key.startsWith('__reactProps')
              );
              const reactProps = reactPropsKey ? surface[reactPropsKey] : null;
              if (
                typeof reactProps?.onMouseDown !== 'function' &&
                typeof reactProps?.onClick !== 'function'
              ) return false;
              const maskRoot = document.documentElement;
              const maskToken = Number(
                window.__matholicKioskDirectProblemSelectToken || 0
              ) + 1;
              window.__matholicKioskDirectProblemSelectToken = maskToken;
              maskRoot.dataset.matholicKioskDirectProblemSelect = 'true';
              const releaseSelectorMask = () => {
                const waitForSelectorToClose = () => {
                  if (
                    Number(window.__matholicKioskDirectProblemSelectToken) !==
                      maskToken
                  ) return;
                  const selectorOpen = Array.from(
                    document.querySelectorAll('.ant-select-dropdown')
                  ).some(element => {
                    if (!visible(element)) return false;
                    const rect = element.getBoundingClientRect();
                    return rect.width > 0 && rect.height > 0;
                  });
                  if (!selectorOpen) {
                    delete maskRoot.dataset.matholicKioskDirectProblemSelect;
                    return;
                  }
                  setTimeout(waitForSelectorToClose, 100);
                };
                setTimeout(waitForSelectorToClose, 1_000);
              };
              const syntheticPointer = {
                button: 0,
                target: surface,
                currentTarget: surface,
                preventDefault() {},
                stopPropagation() {}
              };
              try {
                reactProps.onMouseDown?.(syntheticPointer);
                reactProps.onClick?.(syntheticPointer);
              } catch (_) {
                delete maskRoot.dataset.matholicKioskDirectProblemSelect;
                return false;
              }
              releaseSelectorMask();
              const clickMatchingOption = () => {
                const option = Array.from(document.querySelectorAll(
                  '.ant-select-item-option,[role="option"]'
                )).filter(candidate => {
                  if (!visible(candidate)) return false;
                  const rect = candidate.getBoundingClientRect();
                  return rect.width > 0 && rect.height > 0;
                }).find(candidate =>
                  Number(normalize(candidate.textContent)) === number
                );
                if (!option) return false;
                option.click();
                return true;
              };
              const chooseProblemOption = attempt => {
                if (clickMatchingOption()) return;
                const virtualHolder = Array.from(
                  document.querySelectorAll('.rc-virtual-list-holder')
                ).find(visible);
                if (virtualHolder) {
                  const maxScroll = Math.max(
                    0,
                    virtualHolder.scrollHeight - virtualHolder.clientHeight
                  );
                  virtualHolder.scrollTop = totalProblems > 1 ?
                    maxScroll * (number - 1) / (totalProblems - 1) : 0;
                  virtualHolder.dispatchEvent(
                    new Event('scroll', { bubbles: true })
                  );
                }
                if (attempt < 6) {
                  setTimeout(() => chooseProblemOption(attempt + 1), 50);
                }
              };
              setTimeout(() => chooseProblemOption(0), 0);
              return true;
            };
            const problemNavigationController =
              window.__matholicKioskProblemNavigationController ||
              (window.__matholicKioskProblemNavigationController = {
                target: 0,
                attempts: 0,
                lastSelected: 0,
                staleChecks: 0,
                timer: 0
              });
            const clearProblemNavigation = () => {
              if (problemNavigationController.timer) {
                clearTimeout(problemNavigationController.timer);
              }
              problemNavigationController.target = 0;
              problemNavigationController.attempts = 0;
              problemNavigationController.lastSelected = 0;
              problemNavigationController.staleChecks = 0;
              problemNavigationController.timer = 0;
              updateProblemNumberLabel(
                readCurrentProblemNumber(),
                totalProblems
              );
            };
            const scheduleProblemNavigation = delay => {
              if (problemNavigationController.timer) {
                clearTimeout(problemNavigationController.timer);
              }
              problemNavigationController.timer = setTimeout(
                continueProblemNavigation,
                delay
              );
            };
            const continueProblemNavigation = () => {
              problemNavigationController.timer = 0;
              const target = Number(problemNavigationController.target || 0);
              const selected = readCurrentProblemNumber();
              const liveTotalTokens = normalize(
                currentProblemNumberCluster()?.textContent
              ).match(/\d+/g) || [];
              const liveTotal = Number(
                liveTotalTokens[liveTotalTokens.length - 1] || totalProblems
              );
              if (
                !Number.isInteger(target) ||
                target < 1 ||
                !Number.isInteger(selected) ||
                selected < 1 ||
                !Number.isInteger(liveTotal) ||
                target > liveTotal
              ) {
                clearProblemNavigation();
                return;
              }
              if (selected === target) {
                clearProblemNavigation();
                return;
              }
              if (
                problemNavigationController.attempts >=
                  Math.max(liveTotal * 3, 8)
              ) {
                selectProblemDirectly(target);
                clearProblemNavigation();
                return;
              }
              if (
                problemNavigationController.attempts > 0 &&
                problemNavigationController.lastSelected === selected
              ) {
                problemNavigationController.staleChecks += 1;
                if (problemNavigationController.staleChecks <= 6) {
                  scheduleProblemNavigation(200);
                  return;
                }
              } else {
                problemNavigationController.lastSelected = selected;
                problemNavigationController.staleChecks = 0;
              }
              const direction = target > selected ? 'next' : 'previous';
              const directionButton = document.querySelector(
                `[data-matholic-kiosk-problem-direction="${'$'}{direction}"]`
              );
              if (
                !directionButton ||
                directionButton.disabled ||
                directionButton.getAttribute('aria-disabled') === 'true'
              ) {
                selectProblemDirectly(target);
                clearProblemNavigation();
                return;
              }
              problemNavigationController.lastSelected = selected;
              problemNavigationController.staleChecks = 0;
              problemNavigationController.attempts += 1;
              directionButton.click();
              scheduleProblemNavigation(250);
            };
            const navigateToProblem = number => {
              if (
                !Number.isInteger(number) ||
                number < 1 ||
                number > totalProblems
              ) return;
              if (number === readCurrentProblemNumber()) {
                clearProblemNavigation();
                return;
              }
              const selectorRoot = currentProblemSelectorRoot();
              if (
                selectorRoot?.matches?.('select') ||
                selectorRoot?.querySelector?.('select')
              ) {
                clearProblemNavigation();
                selectProblemDirectly(number);
                return;
              }
              clearProblemNavigation();
              problemNavigationController.target = number;
              updateProblemNumberLabel(
                readCurrentProblemNumber(),
                totalProblems,
                number
              );
              if (selectProblemDirectly(number)) {
                scheduleProblemNavigation(800);
                return;
              }
              continueProblemNavigation();
            };
            const navigateToUnanswered = direction => {
              const selectedProblemNumber = readCurrentProblemNumber();
              if (
                !Number.isInteger(totalProblems) ||
                totalProblems < 1 ||
                !Number.isInteger(selectedProblemNumber) ||
                selectedProblemNumber < 1
              ) return;
              for (let offset = 1; offset <= totalProblems; offset += 1) {
                const zeroBased = (
                  selectedProblemNumber - 1 +
                  direction * offset +
                  totalProblems * 2
                ) % totalProblems;
                const candidate = zeroBased + 1;
                const candidateState =
                  problemStates[candidate] || 'unanswered';
                if (candidateState === 'unanswered' || candidateState === 'unchecked') {
                  navigateToProblem(candidate);
                  return;
                }
              }
            };
            let problemMap = document.querySelector(
              '.matholic-kiosk-problem-map'
            );
            if (
              problemNumberCluster &&
              Number.isInteger(totalProblems) &&
              totalProblems > 1 &&
              totalProblems <= 100
            ) {
              if (!problemMap) {
                problemMap = document.createElement('section');
                problemMap.className = 'matholic-kiosk-problem-map';
                problemMap.dataset.open = 'false';
                const toggle = document.createElement('button');
                toggle.type = 'button';
                toggle.addEventListener('click', () => {
                  problemMap.dataset.open =
                    problemMap.dataset.open === 'true' ? 'false' : 'true';
                });
                const grid = document.createElement('div');
                grid.className = 'matholic-kiosk-problem-map-grid';
                const unanswered = document.createElement('div');
                unanswered.className =
                  'matholic-kiosk-problem-map-unanswered';
                const previousUnanswered = document.createElement('button');
                previousUnanswered.type = 'button';
                previousUnanswered.textContent = '이전 미입력';
                previousUnanswered.setAttribute(
                  'aria-label',
                  '이전 미입력 문제로 이동'
                );
                previousUnanswered.addEventListener('click', () => {
                  problemMap.dataset.open = 'false';
                  problemMap.matholicKioskNavigateToUnanswered?.(-1);
                });
                const nextUnanswered = document.createElement('button');
                nextUnanswered.type = 'button';
                nextUnanswered.textContent = '다음 미입력';
                nextUnanswered.setAttribute(
                  'aria-label',
                  '다음 미입력 문제로 이동'
                );
                nextUnanswered.addEventListener('click', () => {
                  problemMap.dataset.open = 'false';
                  problemMap.matholicKioskNavigateToUnanswered?.(1);
                });
                unanswered.append(previousUnanswered, nextUnanswered);
                const legend = document.createElement('div');
                legend.className = 'matholic-kiosk-problem-map-legend';
                [
                  ['answered', '답변'],
                  ['unknown', '모름'],
                  ['current', '현재 문제']
                ].forEach(([state, label]) => {
                  const item = document.createElement('span');
                  item.dataset.state = state;
                  item.textContent = label;
                  legend.appendChild(item);
                });
                problemMap.append(toggle, grid, unanswered, legend);
              }
              problemMap.matholicKioskNavigateToProblem = navigateToProblem;
              problemMap.matholicKioskNavigateToUnanswered =
                navigateToUnanswered;
              const navigationRow = currentProblemNumberCluster()?.closest(
                '[data-matholic-kiosk-problem-navigation="true"]'
              );
              if (
                navigationRow?.parentElement &&
                navigationRow.nextElementSibling !== problemMap
              ) {
                navigationRow.insertAdjacentElement('afterend', problemMap);
              } else if (!problemMap.isConnected) {
                document.body.appendChild(problemMap);
              }
              const modalOpen = Array.from(document.querySelectorAll(
                '.ant-modal-wrap,.ant-modal[role="dialog"]'
              )).some(visible);
              important(problemMap, 'display', modalOpen ? 'none' : 'block');
              const toggle = problemMap.firstElementChild;
              const answeredCount = Array.from(
                { length: totalProblems },
                (_, index) => problemStates[index + 1]
              ).filter(state => state === 'answered' || state === 'unknown').length;
              toggle.textContent = `답안 현황 ${'$'}{answeredCount}/${'$'}{totalProblems}`;
              const grid = problemMap.querySelector(
                '.matholic-kiosk-problem-map-grid'
              );
              if (grid.children.length !== totalProblems) {
                grid.replaceChildren();
                for (let number = 1; number <= totalProblems; number += 1) {
                  const button = document.createElement('button');
                  button.type = 'button';
                  button.textContent = String(number);
                  button.setAttribute('aria-label', `${'$'}{number}번 문제로 이동`);
                  button.addEventListener('click', () => {
                    problemMap.dataset.open = 'false';
                    problemMap.matholicKioskNavigateToProblem?.(number);
                  });
                  grid.appendChild(button);
                }
              }
              Array.from(grid.children).forEach((button, index) => {
                const number = index + 1;
                button.dataset.state = problemStates[number] || 'unchecked';
                button.dataset.current =
                  number === currentProblemNumber ? 'true' : 'false';
              });
              problemStateMapEnhancements = 1;
            }
            if (!window.__matholicKioskProblemStateTracking) {
              document.addEventListener('click', event => {
                const control = event.target?.closest?.(
                  'button,[role="button"]'
                );
                if (normalize(control?.textContent) !== '모름') return;
                const selected = readCurrentProblemNumber();
                if (selected > 0) problemStates[selected] = 'unknown';
              }, true);
              const markCurrentAnswered = event => {
                const scope = event.target?.closest?.(
                  '[id^="answer-input-form-"]'
                ) || event.target?.closest?.(
                  '[id^="answer-input-form-"],main'
                );
                if (!scope) return;
                const selected = readCurrentProblemNumber();
                if (selected > 0) {
                  problemStates[selected] = answerState(scope);
                }
              };
              document.addEventListener('input', markCurrentAnswered, true);
              document.addEventListener('change', markCurrentAnswered, true);
              window.__matholicKioskProblemStateTracking = true;
            }
            const exactButtons = Array.from(
              document.querySelectorAll('button,[role="button"]')
            ).filter(visible);
            document.querySelectorAll('.matholic-kiosk-answer-guide')
              .forEach(guide => guide.remove());
            exactButtons.forEach(button => {
              const text = normalize(button.textContent);
              if (text === '답안제출' || text === '답안 제출' || text === '완료하기') {
                important(button, 'min-width', '190px');
                important(button, 'min-height', '60px');
                important(button, 'font-size', '20px');
                important(button, 'margin-bottom', '19px');
                let actionLine = button.parentElement;
                for (let depth = 0; actionLine && depth < 4; depth += 1) {
                  const texts = Array.from(
                    actionLine.querySelectorAll('button,[role="button"]')
                  ).map(control => normalize(control.textContent));
                  if (
                    texts.includes('문제지') &&
                    (texts.includes('답안제출') || texts.includes('답안 제출'))
                  ) {
                    important(actionLine, 'margin-bottom', '19px');
                    break;
                  }
                  actionLine = actionLine.parentElement;
                }
                enhancedButtons += 1;
              } else if (text === '모름') {
                important(button, 'min-width', '104px');
                important(button, 'min-height', '56px');
                enhancedButtons += 1;
              } else if (text === '다음 문제' || text === '>') {
                important(button, 'min-width', text === '>' ? '72px' : '170px');
                important(button, 'min-height', '58px');
                enhancedButtons += 1;
              }
            });

            const hideExactControls = texts => {
              const expected = new Set(texts);
              Array.from(document.querySelectorAll(
                'button,a,[role="button"],label,span,div'
              )).forEach(element => {
                if (!expected.has(normalize(element.textContent))) return;
                const nestedMatch = Array.from(
                  element.querySelectorAll('button,a,[role="button"]')
                ).find(child => expected.has(normalize(child.textContent)));
                if (nestedMatch && nestedMatch !== element) return;
                const control = element.closest('button,a,[role="button"]') || element;
                if (hide(control)) hiddenControls += 1;
              });
            };
            hideExactControls([
              '오류신고', '오류 신고', '문제지',
              '답안필기입력', '답안 필기 입력'
            ]);
            Array.from(document.querySelectorAll(
              '[title*="답안 필기"],[aria-label*="답안 필기"],[aria-describedby]'
            )).forEach(element => {
              const describedBy = element.getAttribute('aria-describedby');
              const described = describedBy ? document.getElementById(describedBy) : null;
              if (
                normalize(element.getAttribute('title')).includes('답안 필기') ||
                normalize(element.getAttribute('aria-label')).includes('답안 필기') ||
                normalize(described ? described.textContent : '').includes('답안 필기')
              ) {
                const control = element.closest('button,[role="button"]') || element;
                if (hide(control)) hiddenControls += 1;
              }
            });
            Array.from(document.querySelectorAll(
              'input[placeholder*="주관식 답"]'
            )).forEach(input => {
              const wrapper = input.closest('.ant-input-affix-wrapper') ||
                input.parentElement;
              const suffix = wrapper?.querySelector('.ant-input-suffix');
              const handwritingControl = suffix?.firstElementChild;
              if (handwritingControl && hide(handwritingControl)) {
                hiddenControls += 1;
              }
            });
            const hideDirectMathHandwriting = () => {
              let hidden = 0;
              const toolbarLabels = new Set(['루트', '분수', '파이']);
              const toolbarButtons = Array.from(
                document.querySelectorAll('button,[role="button"]')
              ).filter(button => toolbarLabels.has(normalize(button.textContent)));
              toolbarButtons.forEach(toolbarButton => {
                let scope = toolbarButton.parentElement;
                for (let depth = 0; scope && depth < 6; depth += 1) {
                  if (scope === document.body || scope === document.documentElement) break;
                  const labels = new Set(
                    Array.from(scope.querySelectorAll('button,[role="button"]'))
                      .map(button => normalize(button.textContent))
                      .filter(text => toolbarLabels.has(text))
                  );
                  const directCandidates = Array.from(scope.querySelectorAll(
                    'div[style*="position: absolute"] > button'
                  )).filter(button => {
                    const containerStyle = button.parentElement?.style;
                    return (
                      normalize(button.textContent) === '' &&
                      containerStyle?.position === 'absolute' &&
                      Number.parseFloat(containerStyle.top) === 8 &&
                      Number.parseFloat(containerStyle.right) === 8
                    );
                  });
                  if (
                    [...toolbarLabels].every(label => labels.has(label)) &&
                    directCandidates.length > 0
                  ) {
                    directCandidates.forEach(button => {
                      if (hide(button)) {
                        hidden += 1;
                      }
                    });
                    break;
                  }
                  scope = scope.parentElement;
                }
              });
              return hidden;
            };
            hiddenControls += hideDirectMathHandwriting();

            const ensureSubjectiveTouchTargets = () => {
              const targets = new Set();
              const containers = new Set();
              const answerScopes = Array.from(document.querySelectorAll(
                '[id^="answer-input-form-"]'
              ));
              answerScopes.forEach(answerScope => {
                important(answerScope, 'box-sizing', 'border-box');
                important(answerScope, 'min-width', '220px');
                important(answerScope, 'max-width', '100%');
                important(answerScope, 'min-height', '64px');
                important(answerScope, 'overflow', 'visible');
                answerScope.dataset.matholicKioskSubjectiveTouchScope = 'true';
                Array.from(answerScope.querySelectorAll(
                  'input:not([type="hidden"]):not([type="radio"]):not([type="checkbox"]),' +
                  'textarea,[contenteditable="true"],[role="textbox"]'
                )).forEach(candidate => {
                  if (!candidate.closest('.mq-textarea')) targets.add(candidate);
                });
              });
              Array.from(document.querySelectorAll(
                'input[placeholder*="주관식 답"]'
              )).forEach(input => {
                targets.add(input);
                const wrapper = input.closest('.ant-input-affix-wrapper');
                if (wrapper) containers.add(wrapper);
              });
              Array.from(document.querySelectorAll('.mq-textarea')).forEach(
                textareaWrapper => {
                  const editor = textareaWrapper.closest(
                    '.mq-editable-field,.mq-math-mode,[class*="mathquill"]'
                  ) || textareaWrapper.parentElement;
                  if (editor) targets.add(editor);
                }
              );
              Array.from(document.querySelectorAll(
                '.mq-editable-field,.mq-math-mode'
              ))
                .forEach(editor => targets.add(editor));
              Array.from(document.querySelectorAll('[class*="mathquill"]'))
                .filter(editor =>
                  editor.querySelector('.mq-root-block,.mq-textarea')
                )
                .forEach(editor => targets.add(editor));
              Array.from(document.querySelectorAll('span')).filter(span => {
                const inline = span.style;
                const wrapper = span.parentElement;
                if (
                  inline.width !== '160px' ||
                  inline.padding !== '8px' ||
                  inline.borderRadius !== '6px' ||
                  inline.textAlign !== 'center' ||
                  !wrapper ||
                  getComputedStyle(wrapper).position !== 'relative'
                ) return false;
                let toolbarScope = wrapper.parentElement;
                for (let depth = 0; toolbarScope && depth < 3; depth += 1) {
                  const labels = new Set(
                    Array.from(toolbarScope.querySelectorAll(
                      'button,[role="button"]'
                    )).map(button => normalize(button.textContent))
                  );
                  if (
                    labels.has('루트') &&
                    labels.has('분수') &&
                    labels.has('파이')
                  ) return true;
                  toolbarScope = toolbarScope.parentElement;
                }
                return false;
              }).forEach(shell => {
                shell.dataset.matholicKioskMathShell = 'true';
                targets.add(shell);
              });

              targets.forEach(target => {
                const targetStyle = getComputedStyle(target);
                if (
                  targetStyle.display !== 'none' &&
                  targetStyle.visibility !== 'hidden'
                ) {
                  important(target, 'display', 'inline-block');
                }
                important(target, 'box-sizing', 'border-box');
                important(target, 'min-width', '220px');
                important(target, 'max-width', '100%');
                important(target, 'min-height', '56px');
                important(target, 'font-size', '22px');
                important(target, 'line-height', '1.4');
                important(target, 'pointer-events', 'auto');
                important(target, 'touch-action', 'manipulation');
                important(target, 'vertical-align', 'middle');
                target.dataset.matholicKioskSubjectiveTouchTarget = 'true';
              });
              containers.forEach(container => {
                important(container, 'box-sizing', 'border-box');
                important(container, 'min-width', '220px');
                important(container, 'max-width', '100%');
                important(container, 'min-height', '56px');
                important(container, 'overflow', 'visible');
                container.dataset.matholicKioskSubjectiveTouchTarget = 'true';
              });
              [...targets, ...containers].forEach(target => {
                const answerScope = target.closest(
                  '[id^="answer-input-form-"]'
                );
                if (answerScope) {
                  important(answerScope, 'box-sizing', 'border-box');
                  important(answerScope, 'min-width', '220px');
                  important(answerScope, 'max-width', '100%');
                  important(answerScope, 'min-height', '64px');
                  important(answerScope, 'overflow', 'visible');
                }
              });
              return targets.size + containers.size + answerScopes.length;
            };
            subjectiveTouchTargets = ensureSubjectiveTouchTargets();

            const mathAnswerBindingFor = shell => {
              const fiberKey = Object.getOwnPropertyNames(shell).find(
                key => key.startsWith('__reactFiber${'$'}') ||
                  key.startsWith('__reactInternalInstance${'$'}')
              );
              let fiber = fiberKey ? shell[fiberKey] : null;
              for (let depth = 0; fiber && depth < 8; depth += 1) {
                const candidates = [fiber, fiber.alternate].filter(Boolean);
                for (const candidate of candidates) {
                  const props =
                    candidate.memoizedProps || candidate.pendingProps;
                  if (
                    props &&
                    typeof props.onChange === 'function' &&
                    props.userAnswer &&
                    props.userAnswer.number !== undefined
                  ) {
                    return {
                      onChange: props.onChange,
                      userAnswer: props.userAnswer
                    };
                  }
                }
                fiber = fiber.return;
              }
              return null;
            };
            const remountUninitializedMathShells = () => {
              if (!window.MathQuill) return 0;
              let remounted = 0;
              Array.from(document.querySelectorAll(
                '[data-matholic-kiosk-math-shell="true"]'
              )).forEach(shell => {
                if (
                  !visible(shell) ||
                  shell.classList.contains('mq-editable-field')
                ) {
                  delete shell.dataset.matholicKioskMathShellSeenAt;
                  return;
                }
                const now = Date.now();
                const firstSeen = Number(
                  shell.dataset.matholicKioskMathShellSeenAt || now
                );
                if (!shell.dataset.matholicKioskMathShellSeenAt) {
                  shell.dataset.matholicKioskMathShellSeenAt = String(now);
                }
                if (
                  now - firstSeen < 1500 ||
                  shell.dataset.matholicKioskMathRemountPending === 'true'
                ) return;
                const binding = mathAnswerBindingFor(shell);
                if (!binding) return;
                const answer = binding.userAnswer;
                shell.dataset.matholicKioskMathRemountPending = 'true';
                try {
                  binding.onChange(answer.number, {
                    value: answer.value == null ? null : answer.value,
                    type: 'ONE'
                  });
                  const startedAt = Date.now();
                  const restoreMathType = () => {
                    if (!shell.isConnected) {
                      binding.onChange(answer.number, {
                        value: answer.value == null ? null : answer.value,
                        type: 'EQ'
                      });
                      return;
                    }
                    if (Date.now() - startedAt < 2000) {
                      setTimeout(restoreMathType, 40);
                    } else {
                      delete shell.dataset.matholicKioskMathRemountPending;
                    }
                  };
                  setTimeout(restoreMathType, 40);
                  remounted += 1;
                } catch (_) {
                  delete shell.dataset.matholicKioskMathRemountPending;
                }
              });
              return remounted;
            };

            const elementsWithin = (root, selector) => {
              const scope = root?.querySelectorAll ? root : document;
              const elements = Array.from(scope.querySelectorAll(selector));
              if (scope !== document && scope.matches?.(selector)) {
                elements.unshift(scope);
              }
              return elements;
            };
            const hideLateStudentContent = (root = document) => {
              let hidden = 0;
              const exactHiddenTexts = new Set([
                '오류신고', '오류 신고', '문제지',
                '답안필기입력', '답안 필기 입력',
                '유형동영상', '유형 동영상', '문항 동영상', '대표 유형 동영상',
                '풀이과정', '풀이 과정', '풀이 업로드', '풀이과정 업로드'
              ]);
              elementsWithin(root,
                 'button,a,[role="button"],label,span,div'
              ).forEach(element => {
                if (!exactHiddenTexts.has(normalize(element.textContent))) return;
                const nestedMatch = Array.from(
                  element.querySelectorAll('button,a,[role="button"]')
                ).find(child => exactHiddenTexts.has(normalize(child.textContent)));
                if (nestedMatch && nestedMatch !== element) return;
                const control = element.closest('button,a,[role="button"]') || element;
                if (hide(control)) hidden += 1;
              });

              elementsWithin(root,
                 '[title*="답안 필기"],[aria-label*="답안 필기"],[aria-describedby]'
              ).forEach(element => {
                const describedBy = element.getAttribute('aria-describedby');
                const described = describedBy ? document.getElementById(describedBy) : null;
                if (
                  normalize(element.getAttribute('title')).includes('답안 필기') ||
                  normalize(element.getAttribute('aria-label')).includes('답안 필기') ||
                  normalize(described ? described.textContent : '').includes('답안 필기')
                ) {
                  const control = element.closest('button,[role="button"]') || element;
                  if (hide(control)) hidden += 1;
                }
              });
              elementsWithin(root,
                 'input[placeholder*="주관식 답"]'
              ).forEach(input => {
                const wrapper = input.closest('.ant-input-affix-wrapper') ||
                  input.parentElement;
                const handwritingControl = wrapper
                  ?.querySelector('.ant-input-suffix')
                  ?.firstElementChild;
                if (handwritingControl && hide(handwritingControl)) hidden += 1;
              });

              elementsWithin(root,
                 'img[alt="문항 동영상"],img[alt="대표 유형 동영상"]'
              ).forEach(image => {
                let videoPanel = image.parentElement;
                let ancestor = image.parentElement;
                for (let depth = 0; ancestor && depth < 5; depth += 1) {
                  const text = normalize(ancestor.textContent);
                  if (
                    text.includes('문제가 어렵나요?') &&
                    text.includes('해설 강의를 들어보세요')
                  ) {
                    videoPanel = ancestor;
                    break;
                  }
                  ancestor = ancestor.parentElement;
                }
                if (hide(videoPanel || image)) hidden += 1;
              });

              elementsWithin(root,
                 '.ant-tooltip,.ant-popover,[role="tooltip"],' +
                 '[class*="tooltip"],[class*="popover"]'
              ).forEach(overlay => {
                if (normalize(overlay.textContent) === '수식' && hide(overlay)) {
                  hidden += 1;
                }
              });
              return hidden;
            };
            hiddenControls += hideLateStudentContent();

            const explanationMessage = Array.from(
              document.querySelectorAll('p,span,div')
            ).find(element =>
              normalize(element.textContent).startsWith('문제가 어렵나요?') &&
              normalize(element.textContent).includes('해설 강의를 들어보세요') &&
              !Array.from(element.children).some(child =>
                normalize(child.textContent).startsWith('문제가 어렵나요?') &&
                normalize(child.textContent).includes('해설 강의를 들어보세요')
              )
            );
            if (explanationMessage) {
              let explanationPanel = explanationMessage.closest(
                'aside,[role="complementary"]'
              );
              let ancestor = explanationMessage;
              while (!explanationPanel && ancestor && ancestor.parentElement) {
                ancestor = ancestor.parentElement;
                if (ancestor === document.body || ancestor.matches('main,[role="main"]')) break;
                if (ancestor.querySelector('video,iframe')) explanationPanel = ancestor;
              }
              if (explanationPanel) {
                if (hide(explanationPanel)) hiddenControls += 1;
              } else if (hide(explanationMessage)) {
                hiddenControls += 1;
              }
            }
            Array.from(document.querySelectorAll(
              'img[alt="문항 동영상"],img[alt="대표 유형 동영상"]'
            )).forEach(image => {
              let videoPanel = image.parentElement;
              let ancestor = image.parentElement;
              for (let depth = 0; ancestor && depth < 5; depth += 1) {
                const text = normalize(ancestor.textContent);
                if (
                  text.includes('문제가 어렵나요?') &&
                  text.includes('해설 강의를 들어보세요')
                ) {
                  videoPanel = ancestor;
                  break;
                }
                ancestor = ancestor.parentElement;
              }
              if (hide(videoPanel || image)) hiddenControls += 1;
            });
            hideExactControls([
              '유형동영상', '유형 동영상', '문항 동영상', '대표 유형 동영상'
            ]);

            const mathAnswerScopeForControl = control => {
              const identified = control?.closest?.(
                '[id^="answer-input-form-"]'
              );
              if (identified) return identified;
              let scope = control?.parentElement || null;
              const fallback = scope;
              for (let depth = 0; scope && depth < 5; depth += 1) {
                if (
                  scope === document.body ||
                  scope === document.documentElement
                ) break;
                if (scope.querySelector(
                  'input[placeholder*="주관식 답"],' +
                  '.mq-editable-field,.mq-math-mode,[class*="mathquill"]'
                )) return scope;
                scope = scope.parentElement;
              }
              return fallback;
            };
            const primeMathAnswerScope = scope => {
              if (
                !scope ||
                scope.dataset.matholicKioskAnswerScopePrimed === 'true'
              ) return false;
              scope.dataset.matholicKioskAnswerScopePrimed = 'true';
              scope.dataset.matholicKioskPreviousAnswerOpacity =
                scope.style.getPropertyValue('opacity');
              scope.dataset.matholicKioskPreviousAnswerOpacityPriority =
                scope.style.getPropertyPriority('opacity');
              scope.dataset.matholicKioskPreviousAnswerPointerEvents =
                scope.style.getPropertyValue('pointer-events');
              scope.dataset.matholicKioskPreviousAnswerPointerPriority =
                scope.style.getPropertyPriority('pointer-events');
              scope.dataset.matholicKioskPreviousAnswerAriaHidden =
                scope.hasAttribute('aria-hidden') ?
                  scope.getAttribute('aria-hidden') || '' : '__missing__';
              scope.querySelectorAll(
                'input[placeholder*="주관식 답"]'
              ).forEach(input => {
                delete input.dataset.matholicKioskBasicFallback;
              });
              important(scope, 'opacity', '0');
              important(scope, 'pointer-events', 'none');
              scope.setAttribute('aria-hidden', 'true');
              return true;
            };
            const restorePrimedMathAnswerScope = scope => {
              if (
                !scope ||
                scope.dataset.matholicKioskAnswerScopePrimed !== 'true'
              ) return false;
              const restoreStyle = (
                property,
                valueKey,
                priorityKey
              ) => {
                const value = scope.dataset[valueKey] || '';
                const priority = scope.dataset[priorityKey] || '';
                if (value) {
                  scope.style.setProperty(property, value, priority);
                } else {
                  scope.style.removeProperty(property);
                }
                delete scope.dataset[valueKey];
                delete scope.dataset[priorityKey];
              };
              restoreStyle(
                'opacity',
                'matholicKioskPreviousAnswerOpacity',
                'matholicKioskPreviousAnswerOpacityPriority'
              );
              restoreStyle(
                'pointer-events',
                'matholicKioskPreviousAnswerPointerEvents',
                'matholicKioskPreviousAnswerPointerPriority'
              );
              const previousAriaHidden =
                scope.dataset.matholicKioskPreviousAnswerAriaHidden;
              if (previousAriaHidden === '__missing__') {
                scope.removeAttribute('aria-hidden');
              } else if (previousAriaHidden !== undefined) {
                scope.setAttribute('aria-hidden', previousAriaHidden);
              }
              delete scope.dataset.matholicKioskPreviousAnswerAriaHidden;
              delete scope.dataset.matholicKioskAnswerScopePrimed;
              return true;
            };

            const primeAnswerModeChrome = (root = document) => {
              let hidden = 0;
              let selectedCount = 0;
              let primedScopeCount = 0;
              const toolbarLabels = new Set(['루트', '분수', '파이']);
              const isInputMenuText = text =>
                text === '입력기' || text.startsWith('입력기 ');
              const modeControls = elementsWithin(
                root,
                'button,[role="button"],li,[role="menuitem"]'
              ).filter(element => {
                const text = normalize(element.textContent);
                return text === '기본' || text === '분수' || text === '수식';
              });
              const isStructureControl = element => {
                if (element.closest('.matholic-kiosk-math-nav')) return true;
                const parent = element.parentElement;
                if (!parent) return false;
                const siblingLabels = new Set(
                  Array.from(parent.querySelectorAll(
                    'button,[role="button"]'
                  )).map(candidate => normalize(candidate.textContent))
                );
                return siblingLabels.has('루트') && siblingLabels.has('파이');
              };
              const structureControls = elementsWithin(
                root,
                'button,[role="button"]'
              ).filter(element => {
                if (element.closest('.matholic-kiosk-math-nav')) return false;
                if (!toolbarLabels.has(normalize(element.textContent))) {
                  return false;
                }
                const parent = element.parentElement;
                if (!parent) return false;
                const siblingLabels = new Set(
                  Array.from(parent.querySelectorAll(
                    'button,[role="button"]'
                  )).map(candidate => normalize(candidate.textContent))
                );
                return [...toolbarLabels].every(label =>
                  siblingLabels.has(label)
                );
              });
              const inputMenuControls = elementsWithin(
                root,
                'button,[role="button"]'
              ).filter(element => {
                if (!isInputMenuText(normalize(element.textContent))) {
                  return false;
                }
                if (element.closest('[id^="answer-input-form-"]')) {
                  return true;
                }
                let scope = element.parentElement;
                for (let depth = 0; scope && depth < 6; depth += 1) {
                  const labels = new Set(
                    Array.from(scope.querySelectorAll(
                      'button,[role="button"]'
                    )).map(candidate => normalize(candidate.textContent))
                  );
                  if ([...toolbarLabels].every(label => labels.has(label))) {
                    return true;
                  }
                  scope = scope.parentElement;
                }
                return false;
              });
              Array.from(new Set(
                inputMenuControls
                  .map(mathAnswerScopeForControl)
                  .filter(Boolean)
              )).forEach(scope => {
                if (primeMathAnswerScope(scope)) primedScopeCount += 1;
              });
              modeControls
                .filter(element =>
                  normalize(element.textContent) === '수식' &&
                  !isStructureControl(element)
                )
                .forEach(mathButton => {
                  const selected =
                    mathButton.getAttribute('aria-pressed') === 'true' ||
                    mathButton.getAttribute('aria-selected') === 'true' ||
                    mathButton.getAttribute('data-state') === 'active' ||
                    /(?:^|\s)(?:active|selected|checked)(?:\s|${'$'})/i.test(
                      mathButton.className || ''
                    );
                  if (
                    !selected &&
                    mathButton.dataset.matholicKioskActivated !== 'true'
                  ) {
                    mathButton.dataset.matholicKioskActivated = 'true';
                    mathButton.click();
                    selectedCount += 1;
                  }
                });
              Array.from(new Set([
                ...modeControls.filter(element => !isStructureControl(element)),
                ...structureControls,
                ...inputMenuControls
              ]))
                .forEach(element => {
                  if (
                    element.dataset.matholicKioskModePrehidden !== 'true'
                  ) {
                    element.dataset.matholicKioskModePrehidden = 'true';
                    element.dataset.matholicKioskPreviousVisibility =
                      element.style.getPropertyValue('visibility');
                    element.dataset.matholicKioskPreviousVisibilityPriority =
                      element.style.getPropertyPriority('visibility');
                    important(element, 'visibility', 'hidden');
                    element.setAttribute('aria-hidden', 'true');
                    hidden += 1;
                  }
                });
              if (hidden > 0 || primedScopeCount > 0) {
                setTimeout(() => {
                  const editorReady = Array.from(
                    document.querySelectorAll('.mq-editable-field')
                  ).some(visible);
                  document.querySelectorAll(
                    '[data-matholic-kiosk-mode-prehidden="true"]'
                  ).forEach(element => {
                    if (editorReady) {
                      hide(element);
                    } else {
                      const previous =
                        element.dataset.matholicKioskPreviousVisibility || '';
                      const priority =
                        element.dataset
                          .matholicKioskPreviousVisibilityPriority || '';
                      if (previous) {
                        element.style.setProperty(
                          'visibility',
                          previous,
                          priority
                        );
                      } else {
                        element.style.removeProperty('visibility');
                      }
                      element.removeAttribute('aria-hidden');
                    }
                    delete element.dataset.matholicKioskModePrehidden;
                    delete element.dataset
                      .matholicKioskPreviousVisibility;
                    delete element.dataset
                      .matholicKioskPreviousVisibilityPriority;
                  });
                  document.querySelectorAll(
                    '[data-matholic-kiosk-answer-scope-primed="true"]'
                  ).forEach(scope => {
                    const scopedEditorReady = Array.from(
                      scope.querySelectorAll('.mq-editable-field')
                    ).some(visible);
                    if (!scopedEditorReady) {
                      scope.querySelectorAll(
                        'input[placeholder*="주관식 답"]'
                      ).forEach(input => {
                        input.dataset.matholicKioskBasicFallback = 'true';
                      });
                    }
                    restorePrimedMathAnswerScope(scope);
                  });
                }, 500);
              }
              return { hidden, selectedCount, primedScopeCount };
            };

            const enforceMathAnswerMode = () => {
              let hidden = 0;
              let selectedCount = 0;
              let pendingCount = 0;
              let readyCount = 0;
              const toolbarLabels = new Set(['루트', '분수', '파이']);
              const answerScopeFor = button => {
                const identified = button.closest('[id^="answer-input-form-"]');
                if (identified) return identified;
                let scope = button.parentElement;
                const fallback = scope;
                for (let depth = 0; scope && depth < 6; depth += 1) {
                  if (scope === document.body || scope === document.documentElement) break;
                  const labels = new Set(
                    Array.from(scope.querySelectorAll('button,[role="button"]'))
                      .map(element => normalize(element.textContent))
                      .filter(text => toolbarLabels.has(text))
                  );
                  if (
                    scope.querySelector('input[placeholder*="주관식 답"]') ||
                    scope.querySelector(
                      '.mq-editable-field,.mq-math-mode,[class*="mathquill"]'
                    ) ||
                    [...toolbarLabels].every(label => labels.has(label))
                  ) return scope;
                  scope = scope.parentElement;
                }
                return fallback;
              };
              const mathComponentMounted = scope => {
                if (!scope) return false;
                const labels = new Set(
                  Array.from(scope.querySelectorAll('button,[role="button"]'))
                    .map(element => normalize(element.textContent))
                    .filter(text => toolbarLabels.has(text))
                );
                return [...toolbarLabels].every(label => labels.has(label));
              };
              const mathEditorReady = scope => Array.from(
                scope?.querySelectorAll('.mq-editable-field') || []
              ).some(visible);
              const hideMathClearControls = scope => {
                let hiddenCount = 0;
                Array.from(
                  scope?.querySelectorAll(
                    '.mq-editable-field,.mq-math-mode,' +
                    '[data-matholic-kiosk-math-shell="true"]'
                  ) || []
                ).forEach(editor => {
                  const parent = editor.parentElement;
                  if (!parent) return;
                  const siblings = Array.from(parent.children);
                  const editorIndex = siblings.indexOf(editor);
                  if (editorIndex < 0) return;
                  siblings.slice(editorIndex + 1).forEach(sibling => {
                    const directControl = sibling.matches?.(
                      'button,[role="button"]'
                    );
                    const siblingIdentity = normalize(
                      typeof sibling.className === 'string' ?
                        sibling.className : ''
                    ).toLowerCase();
                    const localAuxiliaryWrapper =
                      sibling.style?.position === 'absolute' ||
                      /(?:clear|close|remove|delete|지우|삭제)/i.test(
                        siblingIdentity
                      );
                    const candidates = directControl ? [sibling] :
                      localAuxiliaryWrapper ? Array.from(
                        sibling.querySelectorAll?.(
                          'button,[role="button"]'
                        ) || []
                      ) : [];
                    candidates.forEach(control => {
                      if (control.dataset.matholicKioskCursorKey) return;
                      const text = normalize(control.textContent);
                      if (
                        toolbarLabels.has(text) ||
                        ['입력기', '답안제출', '답안 제출', '완료하기']
                          .includes(text)
                      ) return;
                      const identity = normalize([
                        control.getAttribute('aria-label') || '',
                        control.getAttribute('title') || '',
                        typeof control.className === 'string' ?
                          control.className : '',
                        typeof sibling.className === 'string' ?
                          sibling.className : ''
                      ].join(' ')).toLowerCase();
                      const explicitClear =
                        ['×', '✕', '지우기', '삭제', '입력 지우기']
                          .includes(text) ||
                        /(?:clear|close|remove|delete|지우|삭제)/i.test(identity);
                      const emptyLocalAuxiliary =
                        text === '' &&
                        (
                          sibling === control ||
                          sibling.style?.position === 'absolute'
                        );
                      if (
                        (explicitClear || emptyLocalAuxiliary) &&
                        hide(control)
                      ) {
                        hiddenCount += 1;
                      }
                    });
                  });
                });
                return hiddenCount;
              };
              const dismissMathNavigation = () => {
                const navigation = document.querySelector(
                  '.matholic-kiosk-math-nav'
                );
                if (!navigation) return;
                const clearButton = navigation.querySelector(
                  '[data-matholic-kiosk-key-action="clear"]'
                );
                if (clearButton) {
                  delete clearButton.dataset.matholicKioskClearArmed;
                  clearButton.textContent = '전체 지움';
                }
                if (navigation.matholicKioskClearTimer) {
                  clearTimeout(navigation.matholicKioskClearTimer);
                  navigation.matholicKioskClearTimer = 0;
                }
                delete navigation.dataset.matholicKioskActive;
                navigation.setAttribute('aria-hidden', 'true');
                delete document.documentElement.dataset
                  .matholicKioskKeypadActive;
              };
              const scrollMathEditorAboveKeypad = (
                editor,
                navigation
              ) => {
                requestAnimationFrame(() => {
                  if (
                    !editor?.isConnected ||
                    navigation.dataset.matholicKioskActive !== 'true'
                  ) return;
                  const editorRect = editor.getBoundingClientRect();
                  const keypadRect = navigation.getBoundingClientRect();
                  const overlap = editorRect.bottom - (keypadRect.top - 16);
                  if (overlap <= 0) return;
                  let scrollingParent = editor.parentElement;
                  let remaining = overlap;
                  while (
                    scrollingParent &&
                    scrollingParent !== document.body &&
                    scrollingParent !== document.documentElement
                  ) {
                    const parentStyle = getComputedStyle(scrollingParent);
                    const overflowY =
                      parentStyle.overflowY || parentStyle.overflow || '';
                    if (
                      /^(auto|scroll|overlay)$/.test(overflowY) &&
                      scrollingParent.scrollHeight >
                        scrollingParent.clientHeight + 2
                    ) {
                      const previousScrollTop = scrollingParent.scrollTop;
                      scrollingParent.scrollTop += remaining;
                      remaining -= Math.max(
                        0,
                        scrollingParent.scrollTop - previousScrollTop
                      );
                      if (remaining <= 0.5) return;
                    }
                    scrollingParent = scrollingParent.parentElement;
                  }
                  if (remaining > 0.5) {
                    try {
                      window.scrollBy({
                        top: remaining,
                        behavior: 'auto'
                      });
                    } catch (_) {
                      window.scrollBy(0, remaining);
                    }
                  }
                });
              };
              const activateMathNavigation = (navigation, scope) => {
                const editor = scope?.querySelector('.mq-editable-field');
                if (!editor?.isConnected || !visible(editor)) return;
                navigation.matholicKioskScope = scope;
                navigation.dataset.matholicKioskActive = 'true';
                navigation.setAttribute('aria-hidden', 'false');
                document.documentElement.dataset
                  .matholicKioskKeypadActive = 'true';
                scrollMathEditorAboveKeypad(editor, navigation);
              };
              const bindMathNavigationActivation = (
                editor,
                scope,
                navigation
              ) => {
                if (
                  editor.dataset.matholicKioskNavigationBound !== 'true'
                ) {
                  const activate = () =>
                    activateMathNavigation(navigation, scope);
                  editor.addEventListener('pointerdown', activate);
                  editor.addEventListener('click', activate);
                  editor.dataset.matholicKioskNavigationBound = 'true';
                }
                if (!window.__matholicKioskMathNavigationDismissGuard) {
                  document.addEventListener('pointerdown', event => {
                    if (
                      event.target?.closest?.(
                        '.mq-editable-field,.matholic-kiosk-math-nav'
                      )
                    ) return;
                    dismissMathNavigation();
                  }, true);
                  window.__matholicKioskMathNavigationDismissGuard = true;
                }
              };
              const hideOriginalMathToolbar = scope => {
                if (!scope) return false;
                const toolbarButtons = Array.from(
                  scope.querySelectorAll('button,[role="button"]')
                ).filter(button =>
                  toolbarLabels.has(normalize(button.textContent))
                );
                const labels = new Set(
                  toolbarButtons.map(button => normalize(button.textContent))
                );
                if (![...toolbarLabels].every(label => labels.has(label))) {
                  return false;
                }
                toolbarButtons.forEach(button => {
                  button.dataset.matholicKioskOriginalMathTool = 'true';
                  hide(button);
                });
                return true;
              };
              const ensureMathNavigation = scope => {
                const editor = scope?.querySelector('.mq-editable-field');
                if (!editor) return false;
                let navigation = document.querySelector(
                  '.matholic-kiosk-math-nav'
                );
                if (
                  navigation &&
                  navigation.dataset.matholicKioskKeypadVersion !== version
                ) {
                  navigation.remove();
                  navigation = null;
                }
                if (navigation) {
                  hideOriginalMathToolbar(scope);
                  bindMathNavigationActivation(editor, scope, navigation);
                  mathKeypadEnhancements = Math.max(
                    mathKeypadEnhancements,
                    navigation.querySelectorAll(
                      '[data-matholic-kiosk-key-action]'
                    ).length
                  );
                  return true;
                }
                let toolbarButtons = Array.from(
                  scope.querySelectorAll('button,[role="button"]')
                ).filter(button => toolbarLabels.has(normalize(button.textContent)));
                let labels = new Set(
                  toolbarButtons.map(button => normalize(button.textContent))
                );
                if (![...toolbarLabels].every(label => labels.has(label))) {
                  toolbarButtons = Array.from(
                    document.querySelectorAll('button,[role="button"]')
                  ).filter(button =>
                    visible(button) &&
                    toolbarLabels.has(normalize(button.textContent))
                  );
                  labels = new Set(
                    toolbarButtons.map(button => normalize(button.textContent))
                  );
                }
                if (![...toolbarLabels].every(label => labels.has(label))) return false;
                const toolbar = toolbarButtons[0]?.parentElement;
                if (!toolbar?.parentElement) return false;
                navigation = document.createElement('div');
                navigation.className =
                  'matholic-kiosk-math-nav matholic-kiosk-math-keypad';
                navigation.dataset.matholicKioskKeypadVersion = version;
                navigation.setAttribute('role', 'group');
                navigation.setAttribute('aria-label', '주관식 수식 입력 키패드');
                navigation.setAttribute('aria-hidden', 'true');
                const inner = document.createElement('div');
                inner.className = 'matholic-kiosk-keypad-inner';
                const createSection = (className, headingText, label) => {
                  const section = document.createElement('section');
                  section.className =
                    `matholic-kiosk-keypad-section ${'$'}{className}`;
                  section.setAttribute('aria-label', label);
                  const heading = document.createElement('div');
                  heading.className = 'matholic-kiosk-keypad-heading';
                  heading.textContent = headingText;
                  heading.setAttribute('aria-hidden', 'true');
                  section.appendChild(heading);
                  inner.appendChild(section);
                  return section;
                };
                const resetClearArm = () => {
                  const clearButton = navigation.querySelector(
                    '[data-matholic-kiosk-key-action="clear"]'
                  );
                  if (clearButton) {
                    delete clearButton.dataset.matholicKioskClearArmed;
                    clearButton.textContent = '전체 지움';
                  }
                  if (navigation.matholicKioskClearTimer) {
                    clearTimeout(navigation.matholicKioskClearTimer);
                    navigation.matholicKioskClearTimer = 0;
                  }
                };
                const activeMathField = () => {
                  const focusedEditor = document.activeElement?.closest?.(
                    '.mq-editable-field'
                  );
                  const currentEditor = focusedEditor ||
                    navigation.matholicKioskScope?.querySelector(
                      '.mq-editable-field'
                    ) ||
                    Array.from(document.querySelectorAll('.mq-editable-field'))
                      .find(visible);
                  const factory = window.MathQuill?.getInterface?.(2);
                  const field = currentEditor &&
                    typeof factory === 'function' ?
                    factory(currentEditor) : null;
                  return { field, editor: currentEditor };
                };
                const editHistories =
                  window.__matholicKioskEditHistories ||
                  (window.__matholicKioskEditHistories = new WeakMap());
                const historyFor = (editor, field) => {
                  let history = editHistories.get(editor);
                  if (!history) {
                    let initial = '';
                    try { initial = field.latex(); } catch (_) {}
                    history = { undo: [], redo: [], last: initial };
                    editHistories.set(editor, history);
                  }
                  return history;
                };
                const setMathValue = (field, editor, value) => {
                  field.latex(value);
                  field.focus?.();
                  editor.dispatchEvent(new Event('input', { bubbles: true }));
                  editor.dispatchEvent(new Event('change', { bubbles: true }));
                };
                const runKeyAction = (button, action, value) => {
                  if (action === 'clear') {
                    if (
                      button.dataset.matholicKioskClearArmed !== 'true'
                    ) {
                      resetClearArm();
                      button.dataset.matholicKioskClearArmed = 'true';
                      button.textContent = '다시 눌러 지움';
                      navigation.matholicKioskClearTimer = setTimeout(
                        resetClearArm,
                        2000
                      );
                      return;
                    }
                  }
                  const { field, editor } = activeMathField();
                  if (!field) {
                    resetClearArm();
                    return;
                  }
                  try {
                    const history = historyFor(editor, field);
                    const before = field.latex();
                    if (action === 'undo') {
                      const previous = history.undo.pop();
                      if (previous === undefined) return;
                      history.redo.push(before);
                      setMathValue(field, editor, previous);
                      history.last = previous;
                      return;
                    } else if (action === 'redo') {
                      const next = history.redo.pop();
                      if (next === undefined) return;
                      history.undo.push(before);
                      setMathValue(field, editor, next);
                      history.last = next;
                      return;
                    } else if (action === 'write' && typeof field.write === 'function') {
                      field.write(value);
                    } else if (action === 'command') {
                      if (typeof field.cmd === 'function') {
                        field.cmd(value);
                      } else if (typeof field.write === 'function') {
                        field.write(value);
                      }
                    } else if (
                      action === 'keystroke' &&
                      typeof field.keystroke === 'function'
                    ) {
                      field.keystroke(value);
                    } else if (
                      action === 'clear' &&
                      typeof field.keystroke === 'function'
                    ) {
                      resetClearArm();
                      if (typeof field.select === 'function') {
                        field.select();
                        field.keystroke('Backspace');
                      } else {
                        field.keystroke('Ctrl-A');
                        field.keystroke('Backspace');
                      }
                    }
                    const after = field.latex();
                    if (after !== before) {
                      history.undo.push(before);
                      if (history.undo.length > 50) history.undo.shift();
                      history.redo.length = 0;
                      history.last = after;
                    }
                    field.focus?.();
                  } catch (_) {
                    resetClearArm();
                  }
                };
                const createKey = (
                  container,
                  text,
                  label,
                  action,
                  value,
                  area
                ) => {
                  const button = document.createElement('button');
                  button.type = 'button';
                  button.textContent = text;
                  button.setAttribute('aria-label', label);
                  button.setAttribute('title', label);
                  button.dataset.matholicKioskKeyAction = action;
                  if (value) button.dataset.matholicKioskKeyValue = value;
                  if (area) {
                    button.dataset.matholicKioskGridArea = area;
                    button.style.setProperty(
                      'grid-area',
                      area,
                      'important'
                    );
                  }
                  let repeatDelay = 0;
                  let repeatTimer = 0;
                  let repeated = false;
                  const stopRepeat = () => {
                    clearTimeout(repeatDelay);
                    clearInterval(repeatTimer);
                    repeatDelay = 0;
                    repeatTimer = 0;
                  };
                  button.addEventListener('pointerdown', event => {
                    event.preventDefault();
                    event.stopPropagation();
                    if (action === 'keystroke' && value === 'Backspace') {
                      repeated = false;
                      repeatDelay = setTimeout(() => {
                        repeated = true;
                        runKeyAction(button, action, value);
                        repeatTimer = setInterval(
                          () => runKeyAction(button, action, value),
                          85
                        );
                      }, 420);
                    }
                  });
                  ['pointerup', 'pointercancel', 'pointerleave'].forEach(type => {
                    button.addEventListener(type, stopRepeat);
                  });
                  button.addEventListener('click', event => {
                    event.preventDefault();
                    event.stopPropagation();
                    stopRepeat();
                    if (repeated) {
                      repeated = false;
                      return;
                    }
                    runKeyAction(button, action, value);
                  });
                  container.appendChild(button);
                  return button;
                };

                const numericSection = createSection(
                  'matholic-kiosk-keypad-numeric',
                  '숫자 · 소수점 · 부호',
                  '숫자와 부호'
                );
                const numericGrid = document.createElement('div');
                numericGrid.className = 'matholic-kiosk-keypad-numeric-grid';
                [
                  ['1', '숫자 1', '1'], ['2', '숫자 2', '2'],
                  ['3', '숫자 3', '3'], ['부호', '음수 부호', '-'],
                  ['4', '숫자 4', '4'], ['5', '숫자 5', '5'],
                  ['6', '숫자 6', '6'], ['.', '소수점', '.'],
                  ['7', '숫자 7', '7'], ['8', '숫자 8', '8'],
                  ['9', '숫자 9', '9'], ['0', '숫자 0', '0']
                ].forEach(([text, label, value]) =>
                  createKey(
                    numericGrid,
                    text,
                    label,
                    'write',
                    value,
                    ''
                  )
                );
                numericSection.appendChild(numericGrid);

                const structureSection = createSection(
                  'matholic-kiosk-keypad-structure',
                  '수식 구조',
                  '수식 구조'
                );
                const structureGrid = document.createElement('div');
                structureGrid.className =
                  'matholic-kiosk-keypad-structure-grid';
                [
                  ['루트  √□', '루트 입력', '\\sqrt'],
                  ['분수  □/□', '분수 입력', '\\frac'],
                  ['파이  π', '파이 입력', '\\pi']
                ].forEach(([text, label, value]) =>
                  createKey(
                    structureGrid,
                    text,
                    label,
                    'command',
                    value,
                    ''
                  )
                );
                structureSection.appendChild(structureGrid);

                const editSection = createSection(
                  'matholic-kiosk-keypad-edit',
                  '이동 · 수정',
                  '커서 이동과 수정'
                );
                const editBody = document.createElement('div');
                editBody.className = 'matholic-kiosk-keypad-edit-body';
                const arrows = document.createElement('div');
                arrows.className = 'matholic-kiosk-keypad-arrows';
                [
                  ['↑', '커서 위쪽', 'Up', 'up'],
                  ['←', '커서 왼쪽', 'Left', 'left'],
                  ['↓', '커서 아래쪽', 'Down', 'down'],
                  ['→', '커서 오른쪽', 'Right', 'right']
                ].forEach(([text, label, value, area]) =>
                  createKey(
                    arrows,
                    text,
                    label,
                    'keystroke',
                    value,
                    area
                  )
                );
                const actions = document.createElement('div');
                actions.className = 'matholic-kiosk-keypad-actions';
                createKey(
                  actions,
                  '실행 취소',
                  '마지막 입력 실행 취소',
                  'undo',
                  '',
                  ''
                );
                createKey(
                  actions,
                  '다시 실행',
                  '취소한 입력 다시 실행',
                  'redo',
                  '',
                  ''
                );
                createKey(
                  actions,
                  '한 칸 삭제',
                  '한 칸 삭제',
                  'keystroke',
                  'Backspace',
                  ''
                );
                createKey(
                  actions,
                  '전체 지움',
                  '전체 지움',
                  'clear',
                  '',
                  ''
                );
                editBody.appendChild(arrows);
                editBody.appendChild(actions);
                editSection.appendChild(editBody);

                navigation.appendChild(inner);
                document.body.appendChild(navigation);
                hideOriginalMathToolbar(scope);
                bindMathNavigationActivation(editor, scope, navigation);
                mathKeypadEnhancements = Math.max(
                  mathKeypadEnhancements,
                  navigation.querySelectorAll(
                    '[data-matholic-kiosk-key-action]'
                  ).length
                );
                return true;
              };
              const prepareMathEditor = scope => {
                const editor = scope?.querySelector('.mq-editable-field');
                if (!editor) return false;
                const textarea =
                  editor.querySelector('textarea') ||
                  scope.querySelector('.mq-textarea textarea');
                if (!ensureMathNavigation(scope)) return false;
                if (textarea) {
                  textarea.setAttribute('inputmode', 'none');
                  textarea.setAttribute('enterkeyhint', 'done');
                  textarea.setAttribute('autocomplete', 'off');
                  textarea.setAttribute('autocapitalize', 'none');
                  textarea.setAttribute('spellcheck', 'false');
                }
                if (editor.dataset.matholicKioskMathStabilized === 'true') {
                  return true;
                }
                try {
                  const factory = window.MathQuill?.getInterface?.(2);
                  const field = typeof factory === 'function' ?
                    factory(editor) : null;
                  if (
                    !field ||
                    typeof field.latex !== 'function' ||
                    typeof field.write !== 'function' ||
                    typeof field.keystroke !== 'function'
                  ) return false;
                  const originalLatex = field.latex();
                  field.write('0');
                  field.keystroke('Backspace');
                  if (field.latex() !== originalLatex) {
                    field.latex(originalLatex);
                    return false;
                  }
                  editor.dataset.matholicKioskMathStabilized = 'true';
                  return true;
                } catch (_) {
                  return false;
                }
              };
              const restoreMathInputInteraction = scope => {
                if (!scope) return;
                if (scope.dataset.matholicKioskMathPending !== 'true') return;
                const previousPointer =
                  scope.dataset.matholicKioskPreviousPointerEvents || '';
                const previousPriority =
                  scope.dataset.matholicKioskPreviousPointerPriority || '';
                if (previousPointer) {
                  scope.style.setProperty(
                    'pointer-events',
                    previousPointer,
                    previousPriority
                  );
                } else {
                  scope.style.removeProperty('pointer-events');
                }
                const previousAriaBusy =
                  scope.dataset.matholicKioskPreviousAriaBusy;
                if (previousAriaBusy === '__missing__') {
                  scope.removeAttribute('aria-busy');
                } else if (previousAriaBusy !== undefined) {
                  scope.setAttribute('aria-busy', previousAriaBusy);
                }
                delete scope.dataset.matholicKioskMathPending;
                delete scope.dataset.matholicKioskPreviousPointerEvents;
                delete scope.dataset.matholicKioskPreviousPointerPriority;
                delete scope.dataset.matholicKioskPreviousAriaBusy;
              };
              const inputMenuButtons = Array.from(
                document.querySelectorAll('button,[role="button"]')
              ).filter(element => {
                const text = normalize(element.textContent);
                return text === '입력기' || text.startsWith('입력기 ');
              });
              const observedScopes = new Set();
              inputMenuButtons.forEach(button => {
                const wasVisible =
                  visible(button) ||
                  button.dataset.matholicKioskModePrehidden === 'true';
                const answerScope = answerScopeFor(button);
                if (answerScope) observedScopes.add(answerScope);
                const componentMounted = mathComponentMounted(answerScope);
                const editorReady = mathEditorReady(answerScope);
                const editorPrepared =
                  editorReady && prepareMathEditor(answerScope);
                restoreMathInputInteraction(answerScope);
                if (editorPrepared) {
                  restorePrimedMathAnswerScope(answerScope);
                  readyCount += 1;
                } else {
                  pendingCount += 1;
                }
                if (
                  !componentMounted &&
                  wasVisible &&
                  button.dataset.matholicKioskMenuOpened !== 'true'
                ) {
                  button.dataset.matholicKioskMenuOpened = 'true';
                  button.click();
                }
                if ((componentMounted || editorReady) && hide(button)) hidden += 1;
              });
              Array.from(
                document.querySelectorAll('.mq-editable-field')
              ).filter(visible).forEach(editor => {
                const answerScope = answerScopeFor(editor);
                if (!answerScope || observedScopes.has(answerScope)) return;
                observedScopes.add(answerScope);
                if (prepareMathEditor(answerScope)) {
                  restorePrimedMathAnswerScope(answerScope);
                  readyCount += 1;
                } else {
                  pendingCount += 1;
                }
              });
              Array.from(document.querySelectorAll(
                '[data-matholic-kiosk-math-pending="true"]'
              )).forEach(scope => {
                restoreMathInputInteraction(scope);
                if (observedScopes.has(scope)) return;
                if (mathEditorReady(scope)) {
                  if (prepareMathEditor(scope)) {
                    restorePrimedMathAnswerScope(scope);
                    readyCount += 1;
                  } else {
                    pendingCount += 1;
                  }
                }
              });

              const answerModeButtons = Array.from(
                document.querySelectorAll(
                  'button,[role="button"],li,[role="menuitem"]'
                )
              ).filter(element => {
                const text = normalize(element.textContent);
                return text === '기본' || text === '분수' || text === '수식';
              });
              answerModeButtons
                .filter(element =>
                  visible(element) && normalize(element.textContent) === '수식'
                )
                .forEach(mathButton => {
                  const selected = mathButton.getAttribute('aria-pressed') === 'true' ||
                    mathButton.getAttribute('aria-selected') === 'true' ||
                    mathButton.getAttribute('data-state') === 'active' ||
                    /(?:^|\s)(?:active|selected|checked)(?:\s|${'$'})/i.test(
                      mathButton.className || ''
                    );
                  if (
                    !selected &&
                    mathButton.dataset.matholicKioskActivated !== 'true'
                  ) {
                    mathButton.dataset.matholicKioskActivated = 'true';
                    mathButton.click();
                    selectedCount += 1;
                  }
                });
              if (
                Array.from(document.querySelectorAll('.mq-editable-field'))
                  .some(visible)
              ) {
                answerModeButtons
                  .filter(element => normalize(element.textContent) === '기본')
                  .forEach(element => {
                    if (hide(element)) hidden += 1;
                  });
              }
              hidden += hideMathClearControls(document);
              if (
                !Array.from(document.querySelectorAll('.mq-editable-field')).some(visible)
              ) {
                dismissMathNavigation();
              }
              return { hidden, selectedCount, pendingCount, readyCount };
            };
            const initialMathMode = enforceMathAnswerMode();
            hiddenControls += initialMathMode.hidden;
            mathModeSelections += initialMathMode.selectedCount;
            mathModePending += initialMathMode.pendingCount;
            mathModeReady += initialMathMode.readyCount;

            const reviewHeadingSelector =
              '.ant-modal-title,.ant-drawer-title,' +
              'h1,h2,h3,h4,h5,h6,[role="heading"]';
            const visibleReviewScopeContaining = target => {
              if (!target || !target.closest) return null;
              const headings = Array.from(
                document.querySelectorAll(reviewHeadingSelector)
              ).filter(element =>
                visible(element) && normalize(element.textContent) === '전체답안'
              );
              for (const heading of headings) {
                const scope = heading.closest(
                  'main,section,[role="dialog"],.ant-modal,.ant-drawer'
                ) || document.body;
                if (scope.contains(target)) return scope;
              }
              return null;
            };
            if (!window.__matholicKioskAnswerSubmitReentryGuard) {
              const submitLabels = new Set([
                '답안제출', '답안 제출', '완료하기'
              ]);
              document.addEventListener('click', event => {
                const control = event.target?.closest?.('button,[role="button"]');
                if (!control || !submitLabels.has(normalize(control.textContent))) return;
                const now = Date.now();
                const previous = Number(
                  window.__matholicKioskLastAnswerSubmitAt || '0'
                );
                if (now - previous < 1500) {
                  event.preventDefault();
                  event.stopImmediatePropagation();
                  control.dataset.matholicKioskSubmitReentryBlocked = 'true';
                  return;
                }
                window.__matholicKioskLastAnswerSubmitAt = now;
              }, true);
              window.__matholicKioskAnswerSubmitReentryGuard = true;
            }
            if (!window.__matholicKioskReviewScrollIntentGuard) {
              const stopReviewAutoScroll = event => {
                const scope = visibleReviewScopeContaining(event.target);
                if (!scope) return;
                scope.dataset.matholicKioskReviewScrollUserOverride = 'true';
                scope.dataset.matholicKioskReviewScrollComplete = 'true';
                delete scope.dataset.matholicKioskReviewScrollGeometry;
                delete scope.dataset.matholicKioskReviewScrollStableReads;
                document.documentElement.dataset.matholicKioskReviewScrolled = 'true';
              };
              ['touchstart', 'pointerdown', 'wheel'].forEach(type => {
                document.addEventListener(type, stopReviewAutoScroll, {
                  capture: true,
                  passive: true
                });
              });
              window.__matholicKioskReviewScrollIntentGuard = true;
            }

            const resetHiddenReviewScrollState = () => {
              const completedScopes = Array.from(document.querySelectorAll(
                '[data-matholic-kiosk-review-scroll-complete="true"],' +
                '[data-matholic-kiosk-review-scroll-user-override="true"]'
              ));
              completedScopes.forEach(scope => {
                const visibleReviewHeading = Array.from(scope.querySelectorAll(
                  reviewHeadingSelector
                )).find(element =>
                  visible(element) && normalize(element.textContent) === '전체답안'
                );
                if (visible(scope) && visibleReviewHeading) return;
                delete scope.dataset.matholicKioskReviewScrollComplete;
                delete scope.dataset.matholicKioskReviewScrollUserOverride;
                delete scope.dataset.matholicKioskReviewScrollGeometry;
                delete scope.dataset.matholicKioskReviewScrollStableReads;
              });
              if (!document.querySelector(
                '[data-matholic-kiosk-review-scroll-complete="true"]'
              )) {
                delete document.documentElement.dataset.matholicKioskReviewScrolled;
              }
            };
            resetHiddenReviewScrollState();

            const reviewHeading = Array.from(
              document.querySelectorAll(reviewHeadingSelector)
            ).find(element => visible(element) && normalize(element.textContent) === '전체답안');
            if (reviewHeading) {
              const scope = reviewHeading.closest(
                'main,section,[role="dialog"],.ant-modal,.ant-drawer'
              ) || document.body;
              const finalButton = Array.from(
                scope.querySelectorAll('button,[role="button"]')
              ).filter(visible).find(button => {
                const text = normalize(button.textContent);
                return text === '답안제출' || text === '답안 제출' || text === '완료하기';
              });
              if (finalButton) {
                const legacyFloatingStyle =
                  finalButton.style.getPropertyValue('position') === 'fixed' &&
                  finalButton.style.getPropertyPriority('position') === 'important' &&
                  finalButton.style.getPropertyValue('right') === '230px' &&
                  finalButton.style.getPropertyPriority('right') === 'important' &&
                  finalButton.style.getPropertyValue('bottom') === '24px' &&
                  finalButton.style.getPropertyPriority('bottom') === 'important' &&
                  finalButton.style.getPropertyValue('z-index') === '2147483000' &&
                  finalButton.style.getPropertyPriority('z-index') === 'important';
                if (legacyFloatingStyle) {
                  [
                    'position', 'right', 'bottom', 'z-index', 'box-shadow'
                  ].forEach(name => finalButton.style.removeProperty(name));
                }
              }
              const uploadSignals = Array.from(
                scope.querySelectorAll(
                  'h1,h2,h3,h4,h5,h6,button,a,[role="button"],label,span,div'
                )
              ).filter(element => {
                const text = normalize(element.textContent);
                return text === '풀이과정' || text === '풀이 과정' ||
                  text === '풀이 업로드' ||
                  text === '풀이과정 업로드';
              });
              uploadSignals.forEach(signal => {
                const control = signal.closest('button,a,[role="button"]');
                let uploadContainer = signal.closest(
                  '.ant-upload-wrapper,label,section,[role="group"],.ant-upload'
                );
                let ancestor = signal.parentElement;
                for (let depth = 0; !uploadContainer && ancestor && depth < 5; depth += 1) {
                  if (ancestor === scope || ancestor.matches('.ant-modal-body,.ant-drawer-body')) {
                    break;
                  }
                  const hasUpload = !!ancestor.querySelector(
                    '.ant-upload,.ant-upload-wrapper,[class*="upload"]'
                  ) || normalize(ancestor.textContent).includes('풀이 업로드');
                  if (hasUpload) {
                    uploadContainer = ancestor;
                    break;
                  }
                  ancestor = ancestor.parentElement;
                }
                if (hide(control || uploadContainer || signal)) hiddenControls += 1;
              });
              if (
                finalButton &&
                scope.dataset.matholicKioskReviewScrollComplete !== 'true' &&
                scope.dataset.matholicKioskReviewScrollUserOverride !== 'true'
              ) {
                const scrollRoots = [];
                const addScrollRoot = element => {
                  if (!element || scrollRoots.includes(element)) return;
                  const style = getComputedStyle(element);
                  const overflowY = style.overflowY || style.overflow || '';
                  const knownReviewRoot = element.matches(
                    '.ant-modal-wrap,.ant-modal-body,.ant-drawer,' +
                    '.ant-drawer-body,.ant-drawer-content-wrapper'
                  );
                  const documentRoot =
                    element === document.scrollingElement ||
                    element === document.documentElement ||
                    element === document.body;
                  if (
                    element.scrollHeight > element.clientHeight + 2 &&
                    (
                      documentRoot ||
                      knownReviewRoot ||
                      /^(auto|scroll|overlay)$/.test(overflowY)
                    )
                  ) {
                    scrollRoots.push(element);
                  }
                };
                Array.from(scope.querySelectorAll(
                  '.ant-modal-body,.ant-drawer-body,[style*="overflow"],[class*="scroll"]'
                )).forEach(addScrollRoot);
                let reviewAncestor = scope;
                while (reviewAncestor && reviewAncestor !== document.body) {
                  addScrollRoot(reviewAncestor);
                  reviewAncestor = reviewAncestor.parentElement;
                }
                addScrollRoot(document.scrollingElement || document.documentElement);

                scrollRoots.forEach(element => {
                  const maxScroll = Math.max(
                    0,
                    element.scrollHeight - element.clientHeight
                  );
                  if (element.scrollTop < maxScroll - 2) {
                    element.scrollTop = element.scrollHeight;
                    if (typeof element.scrollTo === 'function') {
                      try {
                        element.scrollTo(0, element.scrollHeight);
                      } catch (_) {}
                    }
                  }
                });
                try {
                  finalButton.scrollIntoView({
                    behavior: 'auto',
                    block: 'end',
                    inline: 'nearest'
                  });
                } catch (_) {
                  try {
                    finalButton.scrollIntoView(false);
                  } catch (_) {}
                }

                const finalRect = finalButton.getBoundingClientRect();
                let finalButtonVisible =
                  finalRect.width > 0 &&
                  finalRect.height > 0 &&
                  finalRect.top >= -2 &&
                  finalRect.bottom <= window.innerHeight + 2;
                scrollRoots.forEach(element => {
                  if (
                    !finalButtonVisible ||
                    element === document.body ||
                    element === document.documentElement ||
                    !element.contains(finalButton)
                  ) return;
                  const rootRect = element.getBoundingClientRect();
                  finalButtonVisible =
                    finalRect.top >= rootRect.top - 2 &&
                    finalRect.bottom <= rootRect.bottom + 2;
                });
                const allRootsAtBottom = scrollRoots.every(element => {
                  const maxScroll = Math.max(
                    0,
                    element.scrollHeight - element.clientHeight
                  );
                  return element.scrollTop >= maxScroll - 2;
                });
                const geometrySignature = [
                  scope.scrollHeight,
                  finalButton.offsetTop,
                  ...scrollRoots.map(element =>
                    `${'$'}{element.scrollHeight}:${'$'}{element.clientHeight}`
                  )
                ].join('|');
                const previousSignature =
                  scope.dataset.matholicKioskReviewScrollGeometry || '';
                let stableReads = Number(
                  scope.dataset.matholicKioskReviewScrollStableReads || '0'
                );
                stableReads = finalButtonVisible && allRootsAtBottom ?
                  (previousSignature === geometrySignature ? stableReads + 1 : 1) : 0;
                scope.dataset.matholicKioskReviewScrollGeometry = geometrySignature;
                scope.dataset.matholicKioskReviewScrollStableReads = String(stableReads);
                if (stableReads >= 2) {
                  scope.dataset.matholicKioskReviewScrollComplete = 'true';
                  document.documentElement.dataset.matholicKioskReviewScrolled = 'true';
                } else {
                  delete document.documentElement.dataset.matholicKioskReviewScrolled;
                }
              }
            }

            const protectAnalysisDetails = () => {
              const analysisHeading = Array.from(
                document.querySelectorAll('h1,h2,h3,h4,h5,h6,[role="heading"]')
              ).find(element =>
                visible(element) && normalize(element.textContent) === '종합분석'
              );
              if (!analysisHeading) return;
              if (!document.getElementById('matholic-kiosk-result-shield')) {
                const shield = document.createElement('div');
                shield.id = 'matholic-kiosk-result-shield';
                shield.textContent = '채점 결과를 정리하고 있습니다';
                shield.setAttribute('aria-live', 'polite');
                shield.style.cssText = [
                  'position:fixed', 'inset:0', 'z-index:2147483646',
                  'display:flex', 'align-items:center', 'justify-content:center',
                  'background:#102A43', 'color:white', 'font-size:28px',
                  'font-weight:700'
                ].join(';');
                document.documentElement.appendChild(shield);
              }

              const lastStep = Number(
                document.documentElement.dataset.matholicKioskResultLastStep || '0'
              );
              const now = Date.now();
              if (now - lastStep < 350) return;
              document.documentElement.dataset.matholicKioskResultLastStep = String(now);

              let scrollingElement = analysisHeading.parentElement;
              while (
                scrollingElement &&
                scrollingElement !== document.body &&
                scrollingElement !== document.documentElement
              ) {
                const style = getComputedStyle(scrollingElement);
                if (
                  /(auto|scroll)/.test(style.overflowY || '') &&
                  scrollingElement.scrollHeight > scrollingElement.clientHeight + 2
                ) {
                  break;
                }
                scrollingElement = scrollingElement.parentElement;
              }
              if (
                !scrollingElement ||
                scrollingElement === document.body ||
                scrollingElement === document.documentElement
              ) {
                scrollingElement = document.scrollingElement || document.documentElement;
              }
              const viewportHeight = scrollingElement === document.scrollingElement ||
                scrollingElement === document.documentElement ?
                window.innerHeight : scrollingElement.clientHeight;
              const maxScroll = Math.max(
                0,
                scrollingElement.scrollHeight - viewportHeight
              );
              const currentScroll = scrollingElement.scrollTop;
              const previousMax = Number(
                document.documentElement.dataset.matholicKioskResultMaxScroll || '-1'
              );
              const resultCardCount = document.querySelectorAll(
                '.ant-alert-error,.ant-alert-success'
              ).length;
              const previousCardCount = Number(
                document.documentElement.dataset.matholicKioskResultCardCount || '-1'
              );
              let stableBottomReads = Number(
                document.documentElement.dataset.matholicKioskResultBottomReads || '0'
              );
              if (
                currentScroll >= maxScroll - 2 &&
                maxScroll === previousMax &&
                resultCardCount === previousCardCount
              ) {
                stableBottomReads += 1;
              } else {
                stableBottomReads = 0;
              }
              document.documentElement.dataset.matholicKioskResultMaxScroll =
                String(maxScroll);
              document.documentElement.dataset.matholicKioskResultCardCount =
                String(resultCardCount);
              document.documentElement.dataset.matholicKioskResultBottomReads =
                String(stableBottomReads);
              if (currentScroll < maxScroll - 2) {
                const step = Math.max(
                  240,
                  Math.min(720, Math.floor(viewportHeight * 0.85))
                );
                const nextScroll = Math.min(maxScroll, currentScroll + step);
                scrollingElement.scrollTop = nextScroll;
                if (typeof scrollingElement.scrollTo === 'function') {
                  scrollingElement.scrollTo(0, nextScroll);
                }
              } else if (stableBottomReads >= 4) {
                document.documentElement.dataset.matholicKioskResultHydrated = 'true';
              }
            };
            const maintainLateStudentControls = () => {
              resetHiddenReviewScrollState();
              hiddenChrome += hideStudentChrome();
              emptyStateLocalizations = Math.max(
                emptyStateLocalizations,
                localizeEmptyListState()
              );
              hiddenControls += hideLateStudentContent();
              hiddenControls += hideDirectMathHandwriting();
              subjectiveTouchTargets = ensureSubjectiveTouchTargets();
              mathModeRemounted += remountUninitializedMathShells();
              const lateMathMode = enforceMathAnswerMode();
              hiddenControls += lateMathMode.hidden;
              mathModeSelections += lateMathMode.selectedCount;
              mathModePending = lateMathMode.pendingCount;
              mathModeReady = lateMathMode.readyCount;
              protectAnalysisDetails();
              enhanceProblemNavigation();
              longProblemScrollEnhancements = Math.max(
                longProblemScrollEnhancements,
                enhanceLongProblemScrolling()
              );
            };
            maintainLateStudentControls();
            mathQuillRuntimePromise.then(runtimeReady => {
              if (runtimeReady && document.body) {
                maintainLateStudentControls();
              }
            });
            if (!window.__matholicKioskExperienceViewportGuard) {
              let viewportMaintenanceTimer = 0;
              const scheduleViewportMaintenance = () => {
                if (viewportMaintenanceTimer) {
                  clearTimeout(viewportMaintenanceTimer);
                }
                viewportMaintenanceTimer = setTimeout(() => {
                  viewportMaintenanceTimer = 0;
                  maintainLateStudentControls();
                }, 150);
              };
              document.addEventListener(
                'scroll',
                scheduleViewportMaintenance,
                true
              );
              window.addEventListener('resize', scheduleViewportMaintenance);
              window.addEventListener(
                'orientationchange',
                scheduleViewportMaintenance
              );
              window.__matholicKioskExperienceViewportGuard = true;
            }
            if (!window.__matholicKioskExperienceObserver && document.body) {
              let mutationMaintenanceTimer = 0;
              const observer = new MutationObserver(records => {
                let addedStudentContent = false;
                records.forEach(record => {
                  if (record.type !== 'childList') return;
                  Array.from(record.addedNodes).forEach(node => {
                    if (node.nodeType !== Node.ELEMENT_NODE) return;
                    addedStudentContent = true;
                    hiddenControls += hideLateStudentContent(node);
                  });
                });
                if (addedStudentContent) {
                  hiddenControls += hideDirectMathHandwriting();
                  const primedMathMode = primeAnswerModeChrome(document);
                  hiddenControls += primedMathMode.hidden;
                  mathModeSelections += primedMathMode.selectedCount;
                }
                const relevantChange = records.some(record =>
                  (
                    record.type === 'childList' &&
                    (record.addedNodes.length > 0 || record.removedNodes.length > 0)
                  ) ||
                  record.type === 'attributes'
                );
                if (!relevantChange) return;
                if (mutationMaintenanceTimer) return;
                mutationMaintenanceTimer = setTimeout(() => {
                  mutationMaintenanceTimer = 0;
                  maintainLateStudentControls();
                }, 100);
              });
              observer.observe(document.body, {
                childList: true,
                subtree: true,
                attributes: true,
                attributeFilter: ['class', 'style', 'hidden']
              });
              window.__matholicKioskExperienceObserver = observer;
            }
          }

          if (!window.__matholicKioskNavigationGuard) {
            document.addEventListener('click', event => {
              const anchor = event.target && event.target.closest ?
                event.target.closest('a[href]') : null;
              if (!anchor) return;
              try {
                const target = new URL(anchor.href, location.href);
                if (target.protocol !== 'https:' ||
                    target.hostname !== 'im.matholic.com' ||
                    target.port !== '' ||
                    target.username !== '' ||
                    target.password !== '' ||
                    target.hash !== '') {
                  event.preventDefault();
                  event.stopImmediatePropagation();
                  return;
                }
                const targetPath = safeStudentPath(target.pathname || '');
                const targetAllowed = targetPath !== null && (
                  targetPath === '/workbook' || targetPath.startsWith('/workbook/') ||
                  targetPath === '/diagnostic' || targetPath.startsWith('/diagnostic/') ||
                  targetPath.startsWith('/learningV2/')
                );
                if (!targetAllowed) {
                  event.preventDefault();
                  event.stopImmediatePropagation();
                }
              } catch (_) {
                event.preventDefault();
                event.stopImmediatePropagation();
              }
            }, true);
            window.__matholicKioskNavigationGuard = true;
          }

          const analysisReady = Array.from(
            document.querySelectorAll('h1,h2,h3,h4,[role="heading"]')
          ).some(element =>
            visible(element) && normalize(element.textContent).includes('종합분석')
          );
          const contentReady = !isLearning ||
            enhancedButtons > 0 ||
            subjectiveTouchTargets > 0 ||
            problemNavigationEnhancements > 0 ||
            analysisReady;
          return JSON.stringify({
            version, ok: true, path,
            listPage: isWorkbook || isDiagnostic,
            learningPage: isLearning, contentReady,
            enhancedButtons, hiddenChrome, hiddenControls, mathModeSelections,
            emptyStateLocalizations,
            mathModePending, mathModeReady, mathModeRemounted,
            subjectiveTouchTargets, problemNavigationEnhancements,
            problemStateMapEnhancements, longProblemScrollEnhancements,
            mathKeypadEnhancements,
            resultHydrated:
              document.documentElement.dataset.matholicKioskResultHydrated === 'true'
          });
        })()
        """.trimIndent()

    fun applyStudentExperience(keypadPreset: String): String {
        val normalized = keypadPreset.takeIf { it in setOf("right", "left", "center") }
            ?: "right"
        return """
            (() => {
              document.documentElement.dataset.matholicKioskKeypadPreset =
                '$normalized';
            })();
            $applyStudentExperience
        """.trimIndent()
    }

    fun navigateStudentSection(targetPath: String): String {
        require(targetPath == "/workbook" || targetPath == "/diagnostic")
        return """
            (() => {
              const version = '${CONTRACT_VERSION}';
              let page = null;
              try { page = new URL(location.href); } catch (_) {}
              if (
                page === null ||
                page.protocol !== 'https:' ||
                page.hostname !== 'im.matholic.com' ||
                page.port !== '' ||
                page.username !== '' ||
                page.password !== '' ||
                page.hash !== ''
              ) return JSON.stringify({ version, ok: false, count: 0 });
              const normalize = value =>
                (value || '').normalize('NFKC').trim().replace(/\s+/g, ' ');
              const visible = element => {
                const style = getComputedStyle(element);
                return style.display !== 'none' && style.visibility !== 'hidden' &&
                  (element.offsetWidth > 0 || element.offsetHeight > 0);
              };
              const candidates = Array.from(document.querySelectorAll('a[href]')).filter(anchor => {
                try {
                  const target = new URL(anchor.href, location.href);
                  return target.protocol === 'https:' &&
                    target.hostname === 'im.matholic.com' &&
                    target.port === '' &&
                    target.username === '' &&
                    target.password === '' &&
                    target.pathname === '$targetPath' &&
                    target.search === '' &&
                    target.hash === '';
                } catch (_) {
                  return false;
                }
              });
              if (candidates.length === 0) {
                return JSON.stringify({ version, ok: false, count: 0 });
              }
              const targetLabel = '$targetPath' === '/workbook' ? '학습지' : '진단평가';
              const semantic = candidates.filter(anchor =>
                normalize(anchor.textContent).startsWith(targetLabel)
              );
              const preferred = semantic.find(visible) || semantic[0] ||
                candidates.find(visible) || candidates[0];
              preferred.click();
              return JSON.stringify({
                version, ok: true, count: candidates.length,
                semanticCount: semantic.length
              });
            })()
        """.trimIndent()
    }

    val wrongAnswerSummary: String =
        """
        (() => {
          const version = '${CONTRACT_VERSION}';
          const safeStudentPath = value => {
            if (!value || /%(?:2e|2f|5c|25)/i.test(value) ||
                value.includes('\\') || /[\u0000-\u001f\u007f]/.test(value)) {
              return null;
            }
            const segments = value.split('/');
            return segments.some(segment => segment === '.' || segment === '..') ?
              null : value;
          };
          const rawPath = location.pathname || '';
          const path = safeStudentPath(rawPath);
          let page = null;
          try { page = new URL(location.href); } catch (_) {}
          const normalize = value =>
            (value || '').normalize('NFKC').trim().replace(/\s+/g, ' ');
          const visible = element => {
            const style = getComputedStyle(element);
            return style.display !== 'none' && style.visibility !== 'hidden' &&
              (element.offsetWidth > 0 || element.offsetHeight > 0);
          };
          const expectedProblems = (() => {
            const tables = Array.from(document.querySelectorAll('table'));
            for (const table of tables) {
              const headers = Array.from(table.querySelectorAll('thead th'));
              const problemCountIndex = headers.findIndex(header =>
                normalize(header.textContent) === '문항수'
              );
              if (problemCountIndex < 0) continue;
              const rows = Array.from(table.querySelectorAll('tbody tr'));
              for (const row of rows) {
                const cells = Array.from(row.querySelectorAll('td'));
                const match = normalize(
                  cells[problemCountIndex]?.textContent
                ).match(/\d{1,3}/);
                const count = match ? Number(match[0]) : 0;
                if (Number.isInteger(count) && count > 0 && count <= 999) {
                  return count;
                }
              }
            }
            return 0;
          })();
          const base = {
            version, ok: false, path: rawPath, wrongNumbers: [],
            errorCardCount: 0, successCardCount: 0,
            visibleCardCount: 0, totalProblems: 0,
            expectedProblems, classifiedCount: 0,
            analysisFound: false, reason: 'NOT_RESULT'
          };
          if (
            page === null ||
            page.protocol !== 'https:' ||
            page.hostname !== 'im.matholic.com' ||
            page.port !== '' ||
            page.username !== '' ||
            page.password !== '' ||
            page.hash !== '' ||
            path === null ||
            !path.startsWith('/learningV2/')
          ) return JSON.stringify(base);

          const analysisHeading = Array.from(
            document.querySelectorAll('h1,h2,h3,h4,h5,h6,[role="heading"]')
          ).find(element => visible(element) && normalize(element.textContent) === '종합분석');
          if (!analysisHeading) return JSON.stringify(base);

          const errorCards = Array.from(
            document.querySelectorAll('.ant-alert-error')
          );
          const successCards = Array.from(
            document.querySelectorAll('.ant-alert-success')
          );
          const resultBase = {
            ...base,
            analysisFound: true,
            reason: 'INCOMPLETE_RESULT',
            errorCardCount: errorCards.length,
            successCardCount: successCards.length,
            visibleCardCount: [...errorCards, ...successCards].filter(visible).length
          };
          if (errorCards.length + successCards.length === 0) {
            const neutralResultHeadings = Array.from(
              document.querySelectorAll('h3,[role="heading"]')
            ).filter(heading => {
              const text = normalize(heading.parentElement?.textContent);
              return text.includes('모름') || text.includes('제외');
            });
            if (neutralResultHeadings.length === 0) return JSON.stringify(resultBase);
          }
          const scrollingElement = document.scrollingElement || document.documentElement;
          const hydrationComplete =
            document.documentElement.dataset.matholicKioskResultHydrated === 'true' ||
            scrollingElement.scrollHeight <= window.innerHeight + 2;
          if (!hydrationComplete) {
            return JSON.stringify({ ...resultBase, reason: 'HYDRATING_RESULT' });
          }

          const classifications = new Map();
          const parseCard = (card, wrong) => {
            const heading = Array.from(
              card.querySelectorAll('h1,h2,h3,h4,h5,h6,[role="heading"]')
            )[0];
            const match = normalize(heading ? heading.textContent : '').match(/(?:^|\D)(\d{1,3})(?:\D|${'$'})/);
            const number = match ? Number(match[1]) : NaN;
            if (!Number.isInteger(number) || number < 1 || number > 999) {
              return false;
            }
            if (classifications.has(number) && classifications.get(number) !== wrong) {
              return false;
            }
            classifications.set(number, wrong);
            return true;
          };
          for (const card of errorCards) {
            if (!parseCard(card, true)) return JSON.stringify(resultBase);
          }
          for (const card of successCards) {
            if (!parseCard(card, false)) return JSON.stringify(resultBase);
          }
          const classifiedHeadings = new Set(
            [...errorCards, ...successCards].flatMap(card =>
              Array.from(card.querySelectorAll(
                'h1,h2,h3,h4,h5,h6,[role="heading"]'
              ))
            )
          );
          const neutralHeadings = Array.from(
            document.querySelectorAll('h3,[role="heading"]')
          ).filter(heading => !classifiedHeadings.has(heading));
          for (const heading of neutralHeadings) {
            const match = normalize(heading.textContent).match(/(?:^|\D)(\d{1,3})(?:\D|${'$'})/);
            const number = match ? Number(match[1]) : NaN;
            if (!Number.isInteger(number) || number < 1 || number > 999) continue;
            let card = heading.parentElement;
            let state = null;
            for (let depth = 0; card && depth < 6; depth += 1) {
              const cardText = normalize(card.textContent);
              const headingCount = card.querySelectorAll('h3,[role="heading"]').length;
              if (headingCount === 1 && cardText.includes('모름')) {
                state = true;
                break;
              }
              if (headingCount === 1 && cardText.includes('제외')) {
                state = false;
                break;
              }
              card = card.parentElement;
            }
            if (state === null) continue;
            if (classifications.has(number) && classifications.get(number) !== state) {
              return JSON.stringify(resultBase);
            }
            classifications.set(number, state);
          }

          const allNumbers = Array.from(classifications.keys()).sort((a, b) => a - b);
          const totalProblems = allNumbers.length > 0 ? allNumbers[allNumbers.length - 1] : 0;
          const singleProblemProven =
            totalProblems === 1 && expectedProblems === 1;
          const completeSequence = totalProblems >= 1 &&
            (totalProblems >= 2 || singleProblemProven) &&
            allNumbers.length === totalProblems &&
            allNumbers.every((number, index) => number === index + 1) &&
            (expectedProblems === 0 || totalProblems === expectedProblems);
          if (!completeSequence) {
            return JSON.stringify({
              ...resultBase,
              totalProblems,
              classifiedCount: allNumbers.length
            });
          }
          const wrongNumbers = allNumbers.filter(number => classifications.get(number));
          wrongNumbers.sort((a, b) => a - b);
          return JSON.stringify({
            version, ok: true, path, wrongNumbers, totalProblems,
            errorCardCount: errorCards.length,
            successCardCount: successCards.length,
            visibleCardCount: resultBase.visibleCardCount,
            expectedProblems,
            classifiedCount: allNumbers.length,
            analysisFound: true, reason: 'VERIFIED_COMPLETE'
          });
        })()
        """.trimIndent()
}
