package com.local.matholickiosk.webpoc

import org.json.JSONObject

object WebDomScripts {
    const val CONTRACT_VERSION = "web-2026-07-28.2"

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
            .mq-editable-field ~ div[style*="position: absolute"] > button {
              display: none !important;
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
          if (isLearning) {
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
              const inputMenuButtons = Array.from(
                document.querySelectorAll('button,[role="button"]')
              ).filter(element => normalize(element.textContent) === '입력기');
              inputMenuButtons.forEach(button => {
                const wasVisible = visible(button);
                const answerScope = button.closest('[id^="answer-input-form-"]') ||
                  button.parentElement;
                const alreadyMath = !!answerScope?.querySelector(
                  '.mq-editable-field,.mq-math-mode,[class*="mathquill"]'
                );
                if (hide(button)) hidden += 1;
                if (
                  !alreadyMath &&
                  wasVisible &&
                  button.dataset.matholicKioskMenuOpened !== 'true'
                ) {
                  button.dataset.matholicKioskMenuOpened = 'true';
                  button.click();
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
              answerModeButtons
                .filter(element => normalize(element.textContent) === '기본')
                .forEach(element => {
                  if (hide(element)) hidden += 1;
                });
              return { hidden, selectedCount };
            };
            const initialMathMode = enforceMathAnswerMode();
            hiddenControls += initialMathMode.hidden;
            mathModeSelections += initialMathMode.selectedCount;

            const reviewHeading = Array.from(
              document.querySelectorAll(
                '.ant-modal-title,.ant-drawer-title,' +
                'h1,h2,h3,h4,h5,h6,[role="heading"]'
              )
            ).find(element => visible(element) && normalize(element.textContent) === '전체답안');
            if (reviewHeading) {
              const scope = reviewHeading.closest(
                'main,section,[role="dialog"],.ant-modal,.ant-drawer'
              ) || document.body;
              const scrollCandidates = Array.from(scope.querySelectorAll(
                '.ant-modal-body,.ant-drawer-body,[style*="overflow"]'
              ));
              const scrollingElement = scrollCandidates.find(element =>
                element.scrollHeight > element.clientHeight + 2
              ) || document.scrollingElement || document.documentElement;
              const maxReviewScroll = Math.max(
                0,
                scrollingElement.scrollHeight - scrollingElement.clientHeight
              );
              if (scrollingElement.scrollTop < maxReviewScroll - 2) {
                scrollingElement.scrollTop = scrollingElement.scrollHeight;
                if (typeof scrollingElement.scrollTo === 'function') {
                  scrollingElement.scrollTo(0, scrollingElement.scrollHeight);
                }
              } else {
                document.documentElement.dataset.matholicKioskReviewScrolled = 'true';
              }
              const finalButton = Array.from(
                scope.querySelectorAll('button,[role="button"]')
              ).filter(visible).find(button => {
                const text = normalize(button.textContent);
                return text === '답안제출' || text === '답안 제출' || text === '완료하기';
              });
              if (finalButton) {
                if (
                  finalButton.dataset.matholicKioskReviewScrollRequested !== 'true'
                ) {
                  finalButton.dataset.matholicKioskReviewScrollRequested = 'true';
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
                }
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
              hiddenChrome += hideStudentChrome();
              hiddenControls += hideLateStudentContent();
              hiddenControls += hideDirectMathHandwriting();
              const lateMathMode = enforceMathAnswerMode();
              hiddenControls += lateMathMode.hidden;
              mathModeSelections += lateMathMode.selectedCount;
              protectAnalysisDetails();
            };
            maintainLateStudentControls();
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
          const completeSequence = totalProblems >= 2 &&
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
