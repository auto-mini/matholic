package com.local.matholickiosk.webpoc

import org.json.JSONObject

object WebDomScripts {
    const val CONTRACT_VERSION = "web-2026-07-30.8"

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
            html, body { min-height: 100% !important; }
            header, [role="banner"] { display: none !important; }
            *, *::before, *::after { box-sizing: border-box !important; }
            body {
              box-sizing: border-box !important;
              max-width: 100vw !important;
              overflow-x: hidden !important;
              padding-top: 36px !important;
              padding-bottom: 0 !important;
            }
            main { padding-top: 24px !important; }
            button, [role="button"] {
              min-height: 52px !important;
              max-width: 100% !important;
              padding: 10px 10px !important;
              font-size: 18px !important;
              line-height: 1.25 !important;
              touch-action: manipulation !important;
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
              left: calc(16px + 35mm) !important;
              top: calc(88px + 35mm) !important;
              z-index: 2147482500 !important;
              display: none !important;
              grid-template-columns: repeat(3, 54px) !important;
              grid-template-rows: repeat(2, 54px) !important;
              grid-template-areas:
                ". up ."
                "left down right" !important;
              gap: 6px !important;
              width: 190px !important;
              margin: 0 !important;
              padding: 8px !important;
              border: 1px solid rgba(16, 42, 67, 0.35) !important;
              border-radius: 14px !important;
              background: rgba(255, 255, 255, 0.96) !important;
              box-shadow: 0 5px 18px rgba(16, 42, 67, 0.28) !important;
              overflow: visible !important;
            }
            .matholic-kiosk-math-nav[data-matholic-kiosk-active="true"] {
              display: grid !important;
            }
            .matholic-kiosk-math-nav > button {
              width: 54px !important;
              min-width: 54px !important;
              max-width: 54px !important;
              height: 54px !important;
              min-height: 54px !important;
              padding: 4px !important;
              font-size: 28px !important;
              line-height: 1 !important;
            }
          ` : `
            header, nav, [role="navigation"] {
              display: none !important;
            }
            main { margin-top: 0 !important; padding-top: 16px !important; }
          `;

          let enhancedButtons = 0;
          let hiddenControls = 0;
          let mathModeSelections = 0;
          let mathModePending = 0;
          let mathModeReady = 0;
          let mathModeRescued = 0;
          let subjectiveTouchTargets = 0;
          if (isLearning) {
            const mathQuillRuntimePromise = ensureMathQuillRuntime();
            const exactButtons = Array.from(
              document.querySelectorAll('button,[role="button"]')
            ).filter(visible);
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

            const mathBindingFor = shell => {
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
                    typeof props.onLatexChange === 'function' &&
                    Object.prototype.hasOwnProperty.call(props, 'latex')
                  ) return props;
                }
                fiber = fiber.return;
              }
              return null;
            };
            const bindRescuedMathToolbar = (shell, field) => {
              const commandFor = new Map([
                ['루트', 'sqrt'],
                ['분수', 'frac'],
                ['파이', 'pi']
              ]);
              let scope = shell.parentElement;
              for (let depth = 0; scope && depth < 4; depth += 1) {
                const buttons = Array.from(
                  scope.querySelectorAll('button,[role="button"]')
                ).filter(button => commandFor.has(normalize(button.textContent)));
                const labels = new Set(
                  buttons.map(button => normalize(button.textContent))
                );
                if ([...commandFor.keys()].every(label => labels.has(label))) {
                  buttons.forEach(button => {
                    if (
                      button.dataset.matholicKioskRescuedMathBound === 'true'
                    ) return;
                    button.addEventListener('click', () => {
                      const command = commandFor.get(
                        normalize(button.textContent)
                      );
                      if (!command) return;
                      try {
                        field.cmd(command);
                        field.focus?.();
                      } catch (_) {}
                    });
                    button.dataset.matholicKioskRescuedMathBound = 'true';
                  });
                  return;
                }
                scope = scope.parentElement;
              }
            };
            const rescueUninitializedMathShells = () => {
              const factory = window.MathQuill?.getInterface?.(2);
              if (
                typeof factory !== 'function' ||
                typeof factory.MathField !== 'function'
              ) return 0;
              let rescued = 0;
              Array.from(document.querySelectorAll(
                '[data-matholic-kiosk-math-shell="true"]'
              )).filter(shell =>
                visible(shell) &&
                !shell.classList.contains('mq-editable-field') &&
                shell.dataset.matholicKioskMathRescuePending !== 'true'
              ).forEach(shell => {
                const binding = mathBindingFor(shell);
                if (!binding) return;
                shell.dataset.matholicKioskMathRescuePending = 'true';
                try {
                  let suppressEdit = true;
                  let field = null;
                  field = factory.MathField(shell, {
                    restrictMismatchedBrackets: true,
                    handlers: {
                      edit: () => {
                        if (suppressEdit || !field) return;
                        const currentBinding = mathBindingFor(shell) || binding;
                        currentBinding.onLatexChange(field.latex());
                      }
                    }
                  });
                  field.latex(binding.latex == null ? '' : String(binding.latex));
                  suppressEdit = false;
                  shell.dataset.matholicKioskMathRescued = 'true';
                  shell.dataset.matholicKioskMathStabilized = 'true';
                  bindRescuedMathToolbar(shell, field);
                  rescued += 1;
                } catch (_) {
                  delete shell.dataset.matholicKioskMathRescuePending;
                }
              });
              return rescued;
            };

            const hideLateStudentContent = () => {
              let hidden = 0;
              const exactHiddenTexts = new Set([
                '오류신고', '오류 신고', '문제지',
                '답안필기입력', '답안 필기 입력',
                '유형동영상', '유형 동영상', '문항 동영상', '대표 유형 동영상',
                '풀이과정', '풀이 과정', '풀이 업로드', '풀이과정 업로드'
              ]);
              Array.from(document.querySelectorAll(
                'button,a,[role="button"],label,span,div'
              )).forEach(element => {
                if (!exactHiddenTexts.has(normalize(element.textContent))) return;
                const nestedMatch = Array.from(
                  element.querySelectorAll('button,a,[role="button"]')
                ).find(child => exactHiddenTexts.has(normalize(child.textContent)));
                if (nestedMatch && nestedMatch !== element) return;
                const control = element.closest('button,a,[role="button"]') || element;
                if (hide(control)) hidden += 1;
              });

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
                  if (hide(control)) hidden += 1;
                }
              });
              Array.from(document.querySelectorAll(
                'input[placeholder*="주관식 답"]'
              )).forEach(input => {
                const wrapper = input.closest('.ant-input-affix-wrapper') ||
                  input.parentElement;
                const handwritingControl = wrapper
                  ?.querySelector('.ant-input-suffix')
                  ?.firstElementChild;
                if (handwritingControl && hide(handwritingControl)) hidden += 1;
              });

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
                if (hide(videoPanel || image)) hidden += 1;
              });

              Array.from(document.querySelectorAll(
                '.ant-tooltip,.ant-popover,[role="tooltip"],' +
                '[class*="tooltip"],[class*="popover"]'
              )).forEach(overlay => {
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
                delete navigation.dataset.matholicKioskActive;
                navigation.setAttribute('aria-hidden', 'true');
              };
              const activateMathNavigation = (navigation, scope) => {
                navigation.matholicKioskScope = scope;
                navigation.dataset.matholicKioskActive = 'true';
                navigation.setAttribute('aria-hidden', 'false');
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
                  editor.addEventListener('focusin', activate);
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
              const ensureMathNavigation = scope => {
                const editor = scope?.querySelector('.mq-editable-field');
                if (!editor) return false;
                let navigation = document.querySelector(
                  '.matholic-kiosk-math-nav'
                );
                if (navigation) {
                  bindMathNavigationActivation(editor, scope, navigation);
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
                navigation.className = 'matholic-kiosk-math-nav';
                navigation.setAttribute('role', 'group');
                navigation.setAttribute('aria-label', '수식 커서 이동');
                navigation.setAttribute('aria-hidden', 'true');
                [
                  ['↑', 'Up', '커서 위쪽', 'up'],
                  ['←', 'Left', '커서 왼쪽', 'left'],
                  ['↓', 'Down', '커서 아래쪽', 'down'],
                  ['→', 'Right', '커서 오른쪽', 'right']
                ].forEach(([symbol, key, label, area]) => {
                  const button = document.createElement('button');
                  button.type = 'button';
                  button.textContent = symbol;
                  button.setAttribute('aria-label', label);
                  button.setAttribute('title', label);
                  button.dataset.matholicKioskCursorKey = key;
                  button.dataset.matholicKioskGridArea = area;
                  button.style.setProperty('grid-area', area, 'important');
                  button.addEventListener('pointerdown', event => {
                    event.preventDefault();
                  });
                  button.addEventListener('click', event => {
                    event.preventDefault();
                    event.stopPropagation();
                    try {
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
                      if (!field || typeof field.keystroke !== 'function') return;
                      field.keystroke(key);
                      field.focus?.();
                    } catch (_) {}
                  });
                  navigation.appendChild(button);
                });
                document.body.appendChild(navigation);
                bindMathNavigationActivation(editor, scope, navigation);
                return true;
              };
              const prepareMathEditor = scope => {
                const editor = scope?.querySelector('.mq-editable-field');
                if (!editor) return false;
                const textarea =
                  editor.querySelector('textarea') ||
                  scope.querySelector('.mq-textarea textarea');
                if (textarea) {
                  textarea.setAttribute('inputmode', 'decimal');
                  textarea.setAttribute('enterkeyhint', 'done');
                  textarea.setAttribute('autocomplete', 'off');
                  textarea.setAttribute('autocapitalize', 'none');
                  textarea.setAttribute('spellcheck', 'false');
                }
                if (!ensureMathNavigation(scope)) return false;
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
              ).filter(element => normalize(element.textContent) === '입력기');
              const observedScopes = new Set();
              inputMenuButtons.forEach(button => {
                const wasVisible = visible(button);
                const answerScope = answerScopeFor(button);
                if (answerScope) observedScopes.add(answerScope);
                const componentMounted = mathComponentMounted(answerScope);
                const editorReady = mathEditorReady(answerScope);
                const editorPrepared =
                  editorReady && prepareMathEditor(answerScope);
                restoreMathInputInteraction(answerScope);
                if (editorPrepared) {
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
              Array.from(document.querySelectorAll(
                '[data-matholic-kiosk-math-pending="true"]'
              )).forEach(scope => {
                restoreMathInputInteraction(scope);
                if (observedScopes.has(scope)) return;
                if (mathEditorReady(scope)) {
                  if (prepareMathEditor(scope)) {
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
              hiddenControls += hideLateStudentContent();
              hiddenControls += hideDirectMathHandwriting();
              subjectiveTouchTargets = ensureSubjectiveTouchTargets();
              mathModeRescued += rescueUninitializedMathShells();
              const lateMathMode = enforceMathAnswerMode();
              hiddenControls += lateMathMode.hidden;
              mathModeSelections += lateMathMode.selectedCount;
              mathModePending = lateMathMode.pendingCount;
              mathModeReady = lateMathMode.readyCount;
              protectAnalysisDetails();
            };
            maintainLateStudentControls();
            mathQuillRuntimePromise.then(runtimeReady => {
              if (runtimeReady && document.body) {
                maintainLateStudentControls();
              }
            });
            if (!window.__matholicKioskExperienceViewportGuard) {
              let viewportMaintenanceFrame = 0;
              const scheduleViewportMaintenance = () => {
                if (viewportMaintenanceFrame) return;
                viewportMaintenanceFrame = requestAnimationFrame(() => {
                  viewportMaintenanceFrame = 0;
                  maintainLateStudentControls();
                });
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
              const observer = new MutationObserver(maintainLateStudentControls);
              observer.observe(document.body, {
                childList: true,
                subtree: true,
                characterData: true,
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

          return JSON.stringify({
            version, ok: true, path,
            listPage: isWorkbook || isDiagnostic,
            learningPage: isLearning,
            enhancedButtons, hiddenChrome, hiddenControls, mathModeSelections,
            mathModePending, mathModeReady, mathModeRescued,
            subjectiveTouchTargets,
            resultHydrated:
              document.documentElement.dataset.matholicKioskResultHydrated === 'true'
          });
        })()
        """.trimIndent()

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
