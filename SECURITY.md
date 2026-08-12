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
- WebView debugging, 디스크 cache, form data 저장, backup과 최근 앱 미리보기를 차단한다.
- 화면 캡처는 기본적으로 `FLAG_SECURE`로 차단한다. 관리자 또는
  `android.permission.DUMP`를 가진 승인 ADB shell이 시간 제한 원격 점검을
  명시적으로 시작한 동안에만 `FLAG_SECURE`를 해제하고 화면에 상태를 표시한다.
- 상위 탐색은 공식 HTTPS의 `login.matholic.com`, `auth.matholic.com`, `im.matholic.com`만 허용한다.
- 학생 사용 중 상위 탐색은 `im.matholic.com`의 학습지, 진단평가와 문제 경로로 더 좁게 제한하고 Android 뒤로가기를 소비한다.
- 종합분석 DOM 계약을 확인한 경우에만 상세 결과를 불투명하게 가리고 틀린 문제 번호만 기기 화면에 표시한다. 계약이 바뀌면 상세 결과를 임의로 해석하지 않는다.
- 정상 로그아웃 뒤 WebView Cookie, WebStorage, form data와 cache를 삭제하고 빈 로그인 화면을 다시 검증한다.
- DOM fingerprint, 실제 표시명 정확 일치와 로그아웃 검증 중 하나라도 실패하면 다음 로그인을 허용하지 않는다.

## Gate 4 데이터 처리

- `kiosk`의 관리자 PIN은 기기별 salt와 PBKDF2-HMAC-SHA256 verifier로만 저장한다.
- 학생 매쓰홀릭 아이디·비밀번호는 Android Keystore AES-256-GCM으로 필드별 암호화한다.
- AAD에는 학생 내부 UUID와 필드 종류를 넣고 레코드·필드마다 새 IV를 사용한다.
- QR은 `MQR1:` 256비트 난수이며 DB에는 SHA-256 hash만 저장한다.
- 학생 비활성화 시 기존 QR hash를 새 무작위 hash로 교체하고 활성 조회에서 제외한 뒤 live DB 행의 자격정보 암호문·IV를 즉시 폐기한다. SQLite `secure_delete`를 사용하지만 WAL·저장장치 사본의 물리적 보안 삭제까지 보장하지는 않는다.
- QR 카드 PDF에는 발급 직후의 1회성 원문과 학생 전체 이름을 사용한다. PDF 생성·지정 PC 전송 요청을 감사기록에 남기고 외부 전달 직후 화면 원문과 bitmap을 지운다.
- PDF는 앱 cache에 임시 생성하고 지정 PC 저장 응답 또는 공유 화면 복귀 뒤 삭제한다. 선택적 공유 중 process가 사라져도 다음 앱·FileProvider 시작에서 만료 파일을 먼저 삭제하고 아직 유효한 파일의 남은 최대 1시간 수명을 다시 예약한다. process가 계속 종료된 동안에는 private cache 파일이 물리적으로 남을 수 있지만 외부 URI 요청으로 provider가 시작되면 만료 검사를 마치기 전에는 파일을 제공하지 않는다. Android 직접 인쇄 경로는 제거했다. 지정 PC, 선택적 PDF 공유 대상, PC 프린터와 대기열은 앱 밖의 추가 신뢰 경계이므로 통제된 대상만 사용하고 잔류 파일·작업·분실 카드는 폐기한다.
- `kiosk`에서 `webpoc`으로 자격정보를 넘길 때 Intent extra, 파일, clipboard와 로그를 사용하지 않는다.
- 앱 간 브리지는 signature 권한·호출 package allowlist·30초 TTL·1회 조회를 모두 적용한 메모리 전용 provider다.
- 외부 알림은 현재 요구사항에서 제외한다. 감사기록에는 자격정보, QR 원문, 답안, 점수와 학습지 내용을 저장하지 않는다.
- `FLAG_SECURE`, backup/device transfer 전면 제외와 cleartext 차단을
  기본 적용한다. 원격 점검은 같은 부팅에서 최대 2시간만 유효하고 만료·명시
  종료 시 캡처 차단을 복원한다.

## Gate 5 전용기기 통제

- `kiosk`를 Device Owner와 전용 HOME으로 등록한 공장초기화 기기에서만 완전 잠금으로 판정한다.
- Lock Task allowlist는 `kiosk`와 동일 서명의 검증된 `webpoc` 두 패키지로 제한한다.
- QR 대기, 관리자 PIN과 Web 채점 구간은 Lock Task를 유지하고 홈·최근 앱·알림창 기능을 허용하지 않는다.
- 관리자 PIN 성공 뒤에만 Lock Task를 종료한다. 관리자 화면 이탈 시 PIN 화면과 Lock Task를 다시 적용한다.
- 수업 잠금 중 다른 앱의 overlay 창 생성을 제한한다.
- 원격 점검은 네트워크 포트를 열지 않고 기존에 승인된 USB ADB 또는 관리자
  PIN 화면에서만 시작한다. 캡처 파일은 PC 로컬 임시 경로의 최신 파일 하나로
  덮어쓰며 점검 종료 시 삭제한다. 관리자 PIN·비밀번호 입력 중에는 사용하지
  않는다.
- 원격 QR 실기 입력은 같은 `android.permission.DUMP` 경계, 활성 원격 점검과
  정확한 `QR_READY` 상태를 모두 요구한다. QR 원문 대신 32바이트 해시 한 건만
  기존 학생·반 검증 경로에 넘기고 실패·완료 경로에서 메모리를 지운다.
- Device Owner 제거와 관리자 PIN 분실 복구는 공장초기화로만 수행한다. 기존 Keystore 키·자격정보·QR을 외부로 우회 백업하지 않는다.

## Release signing

- release 키와 DPAPI 자격정보는 저장소 밖 `%LOCALAPPDATA%`에 보관하고 Git, 로그와 대화에 출력하지 않는다.
- release 빌드는 configuration cache와 장기 Gradle daemon을 사용하지 않으며 종료 후 비밀번호 환경변수를 제거한다.
- debug signer, 다중 signer, `debuggable=true`, 두 APK signer 불일치와 서명 환경 없는 release 빌드를 거부한다.
- release Device Owner 등록 전에 키 파일의 별도 복사와 복구 비밀번호의 분리 보관을 확인한다.
- Android 폰 복구본은 암호화된 PKCS12 키만 저장하고 DPAPI 자격정보나 복구 비밀번호를 함께 복사하지 않는다.
- 키 복구본을 클라우드·공유 폴더 등 새 외부 대상으로 복사하는 것은 사용자가 정확한 대상을 선택한 경우에만 수행한다.
- 키 또는 비밀번호 분실 시 기존 applicationId의 업데이트를 보장할 수 없으므로 release 기기 배포를 중단한다.

## 신고 및 대응

비밀정보가 커밋되거나 보고서에 노출된 경우:

1. 해당 파일의 공유·배포를 중단한다.
2. 노출된 자격정보를 즉시 변경한다.
3. 원인을 수정한 뒤 새 redacted 자료를 생성한다.
4. 공유 Git 이력 제거가 필요하면 사용자 승인과 저장소 정책을 확인한 뒤 별도 절차로 진행한다.

## 범위 제한

비공개 API, 트래픽 복호화, 앱 변조, 루팅, 인증 우회, 좌표 기반 자동화는 지원하지 않는다. `webpoc`은 공개 공식 웹 UI의 DOM 의미 구조만 사용한다.
