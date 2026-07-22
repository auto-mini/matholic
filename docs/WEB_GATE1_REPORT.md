# Web Gate 1 결과 보고서

검증일: 2026-07-21

## 결론

공식 웹 경로를 사용하는 키오스크의 기술 타당성은 **PASS**다. 기존 Android 앱 접근성 Gate 1 FAIL과 별개 판정이며, 기존 Android POC의 잠금을 해제하지 않는다. 구현은 별도의 Web POC로 진행해야 한다.

## 검증 범위와 결과

- 공식 로그인 `https://login.matholic.com/`: 아이디 textbox, 비밀번호 textbox, `아이디 저장` checkbox, `로그인` submit button이 의미 있는 DOM 요소로 노출됨
- 시험계정 수동 로그인: PASS
- 공식 학습 웹 `https://im.matholic.com/course`: 학습 홈과 문제풀이 화면 진입 PASS
- 문제풀이 화면에서 Android 뒤로가기로 과제/학습 화면 복귀: PASS
- 계정 메뉴: 화면 우측 상단 끝의 닉네임·화살표 영역으로 열리며 `개인정보`, `로그인정보`, `로그아웃` 항목이 제공됨
- 수동 로그아웃: PASS
- 로그아웃 후 학습 주소 재진입: 공식 로그인 화면으로 차단되고 학습 화면 표지가 나타나지 않아 세션 제거 PASS
- 로그아웃 후 비밀번호: empty
- 로그아웃 후 `아이디 저장`: unchecked
- 로그아웃 후 아이디: 이전 아이디가 남고 강제 새로고침 뒤에도 다시 채워짐
- 페이지 로드 후 `input[name="username"]`을 비우고 input/change 이벤트를 전달하는 의미 기반 처리: 아이디 empty, 비밀번호 empty, 저장 checkbox unchecked로 검증 PASS

검증 중 아이디와 비밀번호 값 자체, 쿠키, 브라우저 저장 비밀번호 및 로컬 저장소 내용은 읽지 않았다. DOM 값은 비어 있는지 여부와 길이만 확인했다. 좌표 입력, 화면 캡처, 원문 DOM 덤프는 사용하지 않았다.

## Web POC 필수 조건

1. 공식 HTTPS 도메인 allowlist는 `login.matholic.com`, `auth.matholic.com`, `im.matholic.com`으로 제한한다.
2. 로그인 페이지가 로드될 때마다 아이디와 비밀번호를 먼저 비우고 `아이디 저장`을 해제한 뒤 상태를 다시 읽어 검증한다.
3. QR 선택 전에는 공식 로그인 폼을 학생에게 직접 노출하지 않는다.
4. 로그인 자동화는 좌표가 아니라 `name=username`, `name=password`, checkbox, submit button의 DOM 의미 구조를 사용한다.
5. 로그아웃은 우측 상단 계정 메뉴의 `로그아웃` 항목을 의미 기반으로 실행한다.
6. 로그아웃 성공은 `login.matholic.com` 복귀, 두 입력칸 empty, 저장 checkbox unchecked, 학습 화면 표지 부재를 모두 확인한다.
7. 로그아웃 직후 사이트가 아이디를 다시 채울 수 있으므로 로그인 페이지의 모든 load/visibility 복귀 시 필수 조건 2를 반복한다.
8. 페이지 구조 fingerprint가 달라지거나 요소가 유일하지 않으면 자동 입력·로그아웃을 중단하고 잠금 화면을 표시한다.
9. 자격정보, QR 원문, 학생 이름과 아이디를 로그·파일·알림에 기록하지 않는다.
10. 당시에는 Web POC를 별도 사용자 승인 전까지 시작하지 않기로 했다. 이후 사용자가 승인했으며 현재 구현·비자격정보 검증까지 완료됐다.

## 남은 검증

- WebView에서 실제 QR 입력으로 시험계정 자동 로그인
- WebView 재시작·백그라운드 복귀·네트워크 장애 후 잔존 세션/입력값 정리
- 과제 완료 판정 신호와 자동 로그아웃 트리거
- Android 키오스크/Lock Task 적용 및 복구 절차
