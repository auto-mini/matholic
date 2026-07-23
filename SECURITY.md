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

## Gate 4 데이터 처리

- `kiosk`의 관리자 PIN은 기기별 salt와 PBKDF2-HMAC-SHA256 verifier로만 저장한다.
- 학생 매쓰홀릭 아이디·비밀번호는 Android Keystore AES-256-GCM으로 필드별 암호화한다.
- AAD에는 학생 내부 UUID와 필드 종류를 넣고 레코드·필드마다 새 IV를 사용한다.
- QR은 `MQR1:` 256비트 난수이며 DB에는 SHA-256 hash만 저장한다.
- 학생 비활성화 시 기존 QR hash를 새 무작위 hash로 교체하고 활성 조회에서 제외한다. 이는 논리적 비활성화이며 암호화된 자격정보 레코드의 보안 삭제는 아니다.
- QR 카드 인쇄에는 현재 화면에 표시된 1회성 원문만 사용하고, 마스킹 이름만 함께 표시한다. 인쇄 요청을 감사기록에 남기고 인쇄 서비스 전달 직후 화면 원문과 bitmap을 지운다.
- Android 인쇄 서비스, 선택한 프린터·PDF 대상과 대기열은 앱 밖의 추가 신뢰 경계다. 운영에서는 통제된 로컬 프린터만 사용하고 잔류 작업·분실 카드는 폐기한다.
- `kiosk`에서 `webpoc`으로 자격정보를 넘길 때 Intent extra, 파일, clipboard와 로그를 사용하지 않는다.
- 앱 간 브리지는 signature 권한·호출 package allowlist·30초 TTL·1회 조회를 모두 적용한 메모리 전용 provider다.
- 외부 알림은 현재 요구사항에서 제외한다. 감사기록에는 자격정보, QR 원문, 답안, 점수와 학습지 내용을 저장하지 않는다.
- `FLAG_SECURE`, backup/device transfer 전면 제외와 cleartext 차단을 적용한다.

## Gate 5 전용기기 통제

- `kiosk`를 Device Owner와 전용 HOME으로 등록한 공장초기화 기기에서만 완전 잠금으로 판정한다.
- Lock Task allowlist는 `kiosk`와 동일 서명의 검증된 `webpoc` 두 패키지로 제한한다.
- QR 대기, 관리자 PIN과 Web 채점 구간은 Lock Task를 유지하고 홈·최근 앱·알림창 기능을 허용하지 않는다.
- 관리자 PIN 성공 뒤에만 Lock Task를 종료한다. 관리자 화면 이탈 시 PIN 화면과 Lock Task를 다시 적용한다.
- 수업 잠금 중 다른 앱의 overlay 창 생성을 제한한다.
- Device Owner 제거와 관리자 PIN 분실 복구는 공장초기화로만 수행한다. 기존 Keystore 키·자격정보·QR을 외부로 우회 백업하지 않는다.

## 신고 및 대응

비밀정보가 커밋되거나 보고서에 노출된 경우:

1. 해당 파일의 공유·배포를 중단한다.
2. 노출된 자격정보를 즉시 변경한다.
3. 원인을 수정한 뒤 새 redacted 자료를 생성한다.
4. 공유 Git 이력 제거가 필요하면 사용자 승인과 저장소 정책을 확인한 뒤 별도 절차로 진행한다.

## 범위 제한

비공개 API, 트래픽 복호화, 앱 변조, 루팅, 인증 우회, 좌표 기반 자동화는 지원하지 않는다. `webpoc`은 공개 공식 웹 UI의 DOM 의미 구조만 사용한다.
