# Security policy

## 저장 금지

다음 값은 소스, Git, 테스트 fixture, 로그, 진단 보고서, 스크린샷에 저장하지 않는다.

- 실제 또는 시험계정 아이디·비밀번호
- 학생 실명과 전체 학생 명단
- QR 원문 토큰
- Telegram 또는 기타 외부 알림 자격정보
- 매쓰홀릭 답안·점수·학습지 내용

## Probe 데이터 처리

- 접근성 이벤트는 `com.matholic.mathapp`만 허용한다.
- editable/password 노드의 text는 항상 마스킹한다.
- 허용된 고정 UI 문구 외 text/contentDescription은 길이와 세션 한정 digest로 대체한다.
- raw `uiautomator dump`를 생성하지 않는다.
- 보고서는 앱 private storage에 저장하고 사용자가 명시적으로 내보낼 때만 redacted JSON으로 복사한다.
- 앱 Activity에는 `FLAG_SECURE`를 적용한다.

## Web POC 데이터 처리

- `webpoc`에는 시험계정만 태블릿 화면에서 런타임 입력한다.
- 자격정보와 예상 표시명은 파일, preference, saved state, 로그와 autofill에 저장하지 않는다.
- WebView debugging, 디스크 cache, form data 저장, backup, 화면 캡처와 최근 앱 미리보기를 차단한다.
- 상위 탐색은 공식 HTTPS의 `login.matholic.com`, `auth.matholic.com`, `im.matholic.com`만 허용한다.
- 정상 로그아웃 뒤 WebView Cookie, WebStorage, form data와 cache를 삭제하고 빈 로그인 화면을 다시 검증한다.
- DOM fingerprint, 실제 표시명 정확 일치와 로그아웃 검증 중 하나라도 실패하면 다음 로그인을 허용하지 않는다.

## 신고 및 대응

비밀정보가 커밋되거나 보고서에 노출된 경우:

1. 해당 파일의 공유·배포를 중단한다.
2. 노출된 자격정보를 즉시 변경한다.
3. 원인을 수정한 뒤 새 redacted 자료를 생성한다.
4. 공유 Git 이력 제거가 필요하면 사용자 승인과 저장소 정책을 확인한 뒤 별도 절차로 진행한다.

## 범위 제한

비공개 API, 트래픽 복호화, 앱 변조, 루팅, 인증 우회, 좌표 기반 자동화는 지원하지 않는다. `webpoc`은 공개 공식 웹 UI의 DOM 의미 구조만 사용한다.
