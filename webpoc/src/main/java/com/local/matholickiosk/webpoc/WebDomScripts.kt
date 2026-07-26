package com.local.matholickiosk.webpoc

import org.json.JSONObject

object WebDomScripts {
    const val CONTRACT_VERSION = "web-2026-07-26.2"

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
              page.password === '';
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
                  page.password === '';
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
          const ok = exactImOrigin(page) && page.hash === '' &&
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

          let style = document.getElementById('matholic-kiosk-student-style');
          if (!style) {
            style = document.createElement('style');
            style.id = 'matholic-kiosk-student-style';
            (document.head || document.documentElement).appendChild(style);
          }
          style.textContent = isLearning ? `
            html, body { min-height: 100% !important; }
            header, [role="banner"] { display: none !important; }
            body {
              box-sizing: border-box !important;
              padding-top: 36px !important;
              padding-bottom: 0 !important;
            }
            main { padding-top: 24px !important; }
            button, [role="button"] {
              min-width: 52px !important;
              min-height: 52px !important;
              padding: 10px 16px !important;
              font-size: 18px !important;
              line-height: 1.25 !important;
              touch-action: manipulation !important;
            }
            input, textarea, [contenteditable="true"] {
              min-height: 48px !important;
              font-size: 18px !important;
              touch-action: manipulation !important;
            }
          ` : `
            header, nav, [role="navigation"] {
              display: none !important;
            }
            main { margin-top: 0 !important; padding-top: 16px !important; }
          `;

          let enhancedButtons = 0;
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

            const reviewHeading = Array.from(
              document.querySelectorAll('h1,h2,h3,h4,h5,h6,[role="heading"]')
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
                important(finalButton, 'position', 'fixed');
                important(finalButton, 'right', '230px');
                important(finalButton, 'bottom', '24px');
                important(finalButton, 'z-index', '2147483000');
                important(finalButton, 'box-shadow', '0 6px 18px rgba(0,0,0,.35)');
              }
            }

            const protectAnalysisDetails = () => {
              const analysisHeading = Array.from(
                document.querySelectorAll('h1,h2,h3,h4,h5,h6,[role="heading"]')
              ).find(element =>
                visible(element) && normalize(element.textContent) === '종합분석'
              );
              const resultCards = document.querySelectorAll(
                '.ant-alert-error,.ant-alert-success'
              );
              if (!analysisHeading || resultCards.length === 0 ||
                  document.getElementById('matholic-kiosk-result-shield')) return;
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
            };
            protectAnalysisDetails();
            if (!window.__matholicKioskExperienceObserver && document.body) {
              const observer = new MutationObserver(protectAnalysisDetails);
              observer.observe(document.body, { childList: true, subtree: true });
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
            enhancedButtons
          });
        })()
        """.trimIndent()

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
          const base = {
            version, ok: false, path: rawPath, wrongNumbers: [],
            errorCardCount: 0, successCardCount: 0
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
          ).filter(visible);
          const successCards = Array.from(
            document.querySelectorAll('.ant-alert-success')
          ).filter(visible);
          if (errorCards.length + successCards.length === 0) return JSON.stringify(base);

          const wrongNumbers = [];
          for (const card of errorCards) {
            const heading = Array.from(
              card.querySelectorAll('h1,h2,h3,h4,h5,h6,[role="heading"]')
            ).find(visible);
            const match = normalize(heading ? heading.textContent : '').match(/(?:^|\D)(\d{1,3})(?:\D|${'$'})/);
            const number = match ? Number(match[1]) : NaN;
            if (!Number.isInteger(number) || number < 1 || number > 999) {
              return JSON.stringify({
                ...base,
                errorCardCount: errorCards.length,
                successCardCount: successCards.length
              });
            }
            if (!wrongNumbers.includes(number)) wrongNumbers.push(number);
          }
          wrongNumbers.sort((a, b) => a - b);
          return JSON.stringify({
            version, ok: true, path, wrongNumbers,
            errorCardCount: errorCards.length,
            successCardCount: successCards.length
          });
        })()
        """.trimIndent()
}
