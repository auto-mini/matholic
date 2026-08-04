# Sol Max 연속 리뷰·개발 누적 보고서

## 현재 상태

- `last_updated`: 2026-08-04 21:28:40 +09:00
- 현재 branch: `codex/sol-continuous-development-20260804`
- 보고서 갱신 직전 branch tip / upstream:
  `219d0c45f813bfce00c4dc070a442c7c2c552520` /
  `origin/codex/sol-continuous-development-20260804`
- 마지막 push 성공 commit: `219d0c45f813bfce00c4dc070a442c7c2c552520`
- 최초 보존 기준선: `master`의
  `ccf410d6b9758c7594a07e94e459bf7e83c554bc`; 당시 `origin/master`보다
  24 commits ahead
- 현재 보존 대상: Goal 시작 전부터 있던 미추적 `outputs/`. 수정·stage·삭제하지
  않는다.
- 실제 A 마지막 확인: 2026-08-04 21:28 +09:00. 승인 ADB device는
  serial `R54TB029FHZ`, model `SM-P610` 한 대뿐이다.
  - Kiosk `0.6.0-rc71`/code 76, UID 10288, first install
    `2026-07-24 12:52:28`, last update `2026-08-04 21:01:14`.
  - Web POC `0.4.0-rc132`/code 149, UID 10293, first install
    `2026-07-28 13:12:16`.
  - 기존 설치본과 RC71 artifact signer SHA-256은
    `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`로
    일치한다. 보존형 `adb install -r` 뒤 UID와 first install이 유지됐다.
  - Device Owner와 preferred HOME은 각각
    `com.local.matholickiosk.kiosk/.admin.KioskDeviceAdminReceiver`,
    `com.local.matholickiosk.kiosk/.MainActivity`로 유지됐다.
  - Kiosk가 top resumed이고 Lock Task `LOCKED`; 화면은 정확히 `관리자 인증`,
    `ADMIN_IDLE`, `보안 적용`이며 PIN 입력란은 값 대신 `관리자 PIN` hint 상태다.
  - 시험용 반 `SOL-TEST-0804-2105`는 목록 위·아래 끝을 안정화해 확인한 전체
    13개 반에 없고, 원격 지원은 `INACTIVE`다. 로컬·A 임시 캡처도 없다.
  - 현재 Kiosk process의 logcat에서 `FATAL EXCEPTION`과 Kiosk ANR는 각각 0건이다.
- 현재 작업 중: `SOL-0007` — 계측시험 환경 정정 증거 checkpoint
- 다음 우선 큐:
  1. PC receiver의 Windows 시작 시 LAN 주소 탐색·복구 경계 재감사
  2. Kiosk/Web protocol의 PC 전송 재시도·주소 변경 후 복구 경계 재검토

### 열린 finding과 제약

- 열린 P0/P1: 없음. 과거 보고서의 후보는 현재 source와 독립 재검증 전에는
  열린 결함으로 승격하지 않는다.
- 현장검증 완료 P2: `SOL-0005`, `SOL-0006` 2건. 열린 P2는 없다.
- 완료 P3: `SOL-0001` 1건.
- 자동검증 완료 P4: `SOL-0007` 1건.
- 기각: `SOL-0002` 1건.
- 이미 수정됨: `SOL-0003`, `SOL-0004` 2건.
- 사용자 판단 대기: 없음.
- 현재 제약:
  - Kiosk `MainActivityInstrumentedTest`는 SOL-0007 정정 뒤 전체 16/16 PASS다.
    도움말 layout 회귀는 지원 A의 정확한 2000×1128·240dpi·font scale 1.1을
    결정적으로 측정한다. Pixel 2형 420dpi AVD나 다른 크기의 제품 호환성을
    검증한 것으로 확대하지 않는다.
  - 시험 QR은 공식 PDF를 허용된 원격 제출 도구로 전달했다. 카메라 활성·중단·
    재연결은 실제 A에서 확인했지만 인쇄 카드의 광학 인식, 조명·거리·반사는
    시험하지 않았다.
  - 설치 APK signer 대조용으로 만든 로컬 임시 디렉터리
    `%LOCALAPPDATA%\Temp\MatholicSolSignerCheck-019fcc38`의 삭제 명령이 실행
    정책에 의해 거부됐다. 내부에는 A에서 읽기 전용으로 가져온 현재 설치 APK
    두 개만 있으며 저장소 밖이다. 정책을 우회해 삭제하지 않았다.

## 시작 기준선 — 2026-08-04 19:07 +09:00

### 지침·자료 확인

- 현재 대화에서 사용자가 제공한 `AGENTS.md`와
  `docs/SOL_MAX_CONTINUOUS_REVIEW_AND_DEVELOPMENT_GOAL.md` 전체를 다시 읽었다.
- 저장소 안에는 별도 `AGENTS.md`가 없다.
- 제목이 정확히 `생성 파일 검토 및 역질문`인 과거 Codex 작업을 읽기 전용으로
  확인했다. 최근 핵심 정정은 시험 답안을 화면에서만 해제하지 말고 공식
  `unanswered` 저장, 완전 종료, 동일 과제 재진입과 최소 60초 무복원까지
  확인해야 한다는 점이다.
- `README.md`, `SECURITY.md`, `docs/PRODUCT_DECISIONS.md`,
  `docs/KNOWN_LIMITATIONS.md`, `docs/DEVICE_A_WEBPOC_HANDOFF.md`,
  `docs/REMOTE_ADMIN_PIN.md`, `docs/CONTINUOUS_DEVELOPMENT_GOAL_PROMPT.md`,
  `docs/RELEASE_OPERATIONS.md`, 최신 `docs/BUILD_VERIFICATION.md` 구간과
  `docs/CONTINUOUS_DEVELOPMENT_GOAL.md`의 현행 운영 구간을 확인했다.
- `reports/LUNA_MAX_READ_ONLY_REVIEW_2026-08-02.md`는 상단 최신 정정·요약·ID별
  처리 결과만 과거 감사 자료로 확인했다. 오래된 후보는 현재 수정 근거로
  사용하지 않는다.

### Git·원격

- 시작 status: 전용 branch가 같은 이름의 origin branch를 추적하며 ahead/behind
  `0/0`; 미추적 `outputs/` 외 tracked diff 없음.
- `git fetch --prune origin`: 성공. pull·merge·rebase는 수행하지 않았다.
- `master`, `origin/master`와 공유 이력은 변경하지 않았다.

### 실제 A와 artifact

- 마지막 확인: 2026-08-04 19:04~19:07 +09:00
- 승인 ADB device: 정확히 한 대, serial `R54TB029FHZ`, model `SM-P610`,
  Android 13 / SDK 33.
- Kiosk: `0.6.0-rc69`/code 74, UID 10288, first install
  `2026-07-24 12:52:28`, dataDir 유지.
- Web POC: `0.4.0-rc132`/code 149, UID 10293, first install
  `2026-07-28 13:12:16`, dataDir 유지.
- 두 설치 APK와 대응 artifact의 signer SHA-256은 모두
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`로
  일치했다.
- Device Owner:
  `com.local.matholickiosk.kiosk/.admin.KioskDeviceAdminReceiver`.
- preferred HOME: `com.local.matholickiosk.kiosk/.MainActivity`.
- `topResumedActivity`: Kiosk `MainActivity`; Lock Task: `LOCKED`.
- 원격 지원: 최종 `INACTIVE`.
- 화면 내용, 현재 DB session state, 시험계정 상태는 변경 없이 증명하지 못해
  미검증으로 남긴다.

## Finding

### SOL-0001 — release 운영 문서의 현재 버전 기준선 불일치

- 영역: 빌드·릴리스·운영 문서
- 심각도: P3
- 신뢰도: 높음
- 상태: 현장검증 완료
- 사용자 영향: 운영자가 `docs/RELEASE_OPERATIONS.md`만 따르면 현재 A와 보관
  artifact를 RC67/RC125로 오인해 설치·검증·롤백 대상을 잘못 선택할 수 있다.
- 재현 조건: `docs/RELEASE_OPERATIONS.md`의 `현재 상태`를 현재 source, artifact,
  실제 A와 대조한다.
- 기대 결과: 운영 문서의 현재 A·검증 묶음이 실제 설치본과 최신 검증 artifact를
  가리킨다.
- 실제 결과: 문서는 Kiosk RC67/code 72와 Web RC125/code 142를 현재값으로
  기록하지만 source, release script, checksum, 최신 빌드 기록과 실제 A는
  Kiosk RC69/code 74와 Web RC132/code 149다.
- 정적 근거:
  - `kiosk/build.gradle.kts`: RC69/code 74
  - `webpoc/build.gradle.kts`: RC132/code 149
  - `scripts/build-release.ps1`, `scripts/verify-release-apks.ps1`: RC69/RC132
  - `artifacts/RELEASE_SHA256SUMS.txt`: RC69/RC132
  - `docs/RELEASE_OPERATIONS.md`: RC67/RC125
- 동적 근거: 승인 A의 package dump에서 RC69/code 74와 RC132/code 149 확인;
  설치 APK와 보관 artifact signer 일치.
- 반대 근거: `README.md`와 `docs/BUILD_VERIFICATION.md` 최신 절은 이미 실제값을
  기록해 모든 문서가 잘못된 것은 아니다.
- 원인·결정: RC69/RC132 배포 뒤 운영 문서 상단이 함께 갱신되지 않은 문서 drift로
  판단한다. 현재 사실만 최소 교정하고 과거 release 이력은 바꾸지 않는다.
- 변경 파일: `docs/RELEASE_OPERATIONS.md`.
- 관련 commit: `b5b6fe4ec2a2506af7002dabf5204f7de2cc7545`.
- 실행한 검증:
  - source·release script·checksum의 버전 문자열 대조
  - A package version/UID/firstInstallTime, Device Owner, HOME, Lock Task 확인
  - 설치 APK와 artifact signer SHA-256 대조
- 수행하지 않은 검증: 새 build·APK 설치·실제 화면 조작은 이 문서 drift 확정에
  필요하지 않아 수행하지 않았다.
- rollback: `git revert b5b6fe4ec2a2506af7002dabf5204f7de2cc7545` 후
  source·artifact·A 버전과 문서 문자열을 다시 대조한다. A 설치본에는 영향을
  주지 않는다.

### SOL-0002 — Kiosk↔Web 세션 결과의 중복·지연 상태 전이 재감사

- 영역: 데이터·세션 상태·프로세스 수명주기
- 심각도: P2 후보
- 신뢰도: 높음
- 상태: 기각
- 사용자 영향 후보: 이전 Web launch의 늦은 결과나 중복 완료 callback이 새
  수업 또는 이미 종료된 session에 적용되면 학생·과제 연결, QR 재개 상태와
  종료 결과가 불일치할 수 있다.
- 정적 근거:
  - Web launch 직전에 현재 session ID를 `pendingWebSessionId`에 보관하고 결과
    callback 첫 진입에서 꺼낸 뒤 즉시 null로 만들어 중복 결과를 폐기한다.
  - 결과 저장 전 현재 DB session ID를 대조하고, 실제 상태 변경은 Room
    transaction 안에서 expected session ID와 `PRELOGIN_CHECK` 상태를 모두
    비교한 뒤에만 수행한다.
  - 교체 session이면 결과를 버리고, 같은 session의 상태가 이미 바뀌었으면
    성공으로 오인하지 않고 실패 폐쇄한다.
  - `RepositoryInstrumentedTest`에는 process restart 뒤 stale 결과와 종료·재시작
    뒤 원래 session callback이 replacement session을 변경하지 못하는 회귀가 있다.
- 반대 근거: process death·Activity result 실제 전달과 Room runtime 동작을 이번
  주기에 A에서 새로 실행하지는 않았다.
- 판정: 현재 가정한 중복·지연 결과의 다른 session 적용 경로는 source와 기존
  회귀 계약으로 반증됐다. 새 결함이나 수정 필요성을 확정할 근거가 없어 기각한다.
- 실행한 검증:
  - `gradlew.bat :kiosk:testDebugUnitTest --tests
    com.local.matholickiosk.kiosk.WebSessionResultPersistenceTest --tests
    com.local.matholickiosk.kiosk.KioskStatePolicyTest
    :kiosk:compileDebugAndroidTestKotlin --no-daemon --stacktrace`
  - 결과: BUILD SUCCESSFUL, 33 tasks 중 9 executed·24 up-to-date.
  - `WebSessionResultPersistenceTest` 4/4, `KioskStatePolicyTest` 3/3 PASS;
    failures/errors/skipped 0. AndroidTest Kotlin compile PASS.
- 수행하지 않은 검증: A에는 같은 applicationId의 release 앱이 설치돼 있어 debug
  signer 계측 설치가 운영 앱·Device Owner에 영향을 줄 수 있으므로 instrumentation
  실행을 생략했다. 실제 Web 왕복도 답안·세션 상태를 바꾸지 않기 위해 수행하지
  않았다.
- 변경 파일·commit·rollback: source 변경 없음. rollback 불필요.

### SOL-0003 — 네트워크 단절 중 Web 입력 차단 경계 재검증

- 영역: 답안 무결성·offline 복구·입력 경계
- 심각도: P2 후보
- 신뢰도: 높음
- 상태: 이미 수정됨
- 사용자 영향 후보: 네트워크 단절 overlay가 보이는 동안 이미 focus된 Web
  수식 입력기나 IME·hardware key가 답안을 바꾸면 사용자는 차단 화면 뒤의
  변경을 인지하지 못할 수 있다.
- 정적 근거:
  - `setNetworkPauseProtection(true)`는 불투명 panel을 최상단에 놓은 뒤 DOM
    active element blur, WebView focus·descendant focus 차단, IME 숨김,
    accessibility subtree `NO_HIDE_DESCENDANTS`와 panel focus를 함께 적용한다.
  - `dispatchTouchEvent`, `onKeyDown`, `onKeyUp`은 pause 동안 입력을 소비하며
    학생 header·탐색·종료 control도 숨긴다.
  - 네트워크 복귀 때 현재 URL 정책으로 학생 chrome을 다시 계산하며, ACTIVE가
    아닌 상태에서는 pause 보호를 해제하고 blocker/terminal UI가 WebView를
    별도로 숨긴다.
- 반대 근거: 실제 제조사 IME의 이미 대기 중인 composition commit, hardware
  keyboard와 접근성 service 입력을 이번 주기에 A에서 직접 주입하지 않았다.
- 판정: 과거 단순 panel만으로 입력 차단이 불충분했던 경계는
  `d2f69ccfe9b77613da9d4a3ae061750ebb37dd13`에서 이미 교정됐고 현재 source에도
  유지된다. 새 결함 증거가 없어 추가 변경하지 않는다.
- 실행한 검증:
  - `gradlew.bat :webpoc:testDebugUnitTest --tests
    com.local.matholickiosk.webpoc.NetworkFailureReasonTest
    :webpoc:compileDebugAndroidTestKotlin --no-daemon --stacktrace`
  - 결과: BUILD SUCCESSFUL, 30 tasks 중 1 executed·29 up-to-date.
  - `NetworkFailureReasonTest` 1/1 PASS; failures/errors/skipped 0.
  - `NetworkPauseLayoutInstrumentedTest`를 포함한 AndroidTest Kotlin compile PASS.
- 수행하지 않은 검증: A의 Wi-Fi·보안 설정과 진행 상태를 변경하지 않았고 실제
  network disconnect·IME·hardware key·접근성 fault injection은 수행하지 않았다.
  따라서 실제 현장 입력 차단 PASS로 확대하지 않는다.
- 변경 파일·commit·rollback: 이번 source 변경 없음. 기존 교정 rollback은
  `git revert d2f69ccfe9b77613da9d4a3ae061750ebb37dd13`이지만 입력 무결성 보호를
  제거하므로 현재 rollback 사유가 없다.

### SOL-0004 — PC receiver 연결·frame·queue resource bound 재감사

- 영역: PC receiver·통신 프로토콜·장시간 운용
- 심각도: P2 후보
- 신뢰도: 높음
- 상태: 이미 수정됨
- 사용자 영향 후보: 느린·부분 연결이나 동시 요청이 connection thread·memory·UI
  queue를 고갈시키면 카드 PDF 저장과 상태 알림이 지연되거나 수신기가 응답하지
  않을 수 있다.
- 정적 근거:
  - body를 할당하기 전에 protocol header에서 PDF 5MB·control 1MB 상한을
    검증한다.
  - 각 handler는 monotonic 기준 30초 누적 deadline과 recv당 10초 timeout을
    공유해 작은 조각을 계속 보내는 연결도 무기한 유지되지 않는다.
  - `BoundedSemaphore(32)`가 활성 handler 수를 제한하고 초과 연결은 thread를
    만들지 않고 즉시 닫는다. listen backlog도 16으로 제한된다.
  - UI event queue는 256개, poll당 처리는 64개로 제한되며 queue가 찼을 때 가장
    오래된 event 하나를 버리고 최신 event를 보존한다.
  - listener와 tray worker는 handle을 보관해 shutdown·server_close 뒤 2초
    bounded join한다. request handler는 daemon thread라 별도 join하지 않지만
    최대 32개·30초로 제한되고 앱 종료와 함께 프로세스에서 제거된다.
- 동적 근거:
  - PC receiver 전체 pytest 17/17 PASS.
  - loopback 실제 socket에서 총 deadline을 시험용 0.20초로 낮추고 1 byte만
    전송했을 때 0.218초에 거부 event가 발생했다.
  - 활성 연결 한도를 시험용 1개로 낮춘 상태에서 첫 partial 연결이 slot을 점유한
    동안 두 번째 연결이 즉시 닫혔고, 첫 연결 종료 뒤 slot이 다시 1로 복구됐다.
  - 크기 2의 event queue에 3개 event를 넣었을 때 `two`, `three`만 남아 bounded
    최신 이벤트 정책을 확인했다.
- 반대 근거·제약: 32개의 실제 5MB 암호화 frame을 동시에 보내는 부하시험,
  저사양 운영 PC의 peak RSS 측정, Windows 로그오프·종료 중 실제 전송은 이번
  주기에 수행하지 않았다. daemon request handler를 종료 시 개별 join하지 않는
  설계는 확인했지만 bounded process 종료 경계이며 현재 사용자 영향이나 데이터
  오적용 증거는 없다.
- 판정: 과거 무제한 연결·queue·read 후보는
  `47b76ec1d78b1bf3f96c9bab071762de040e3ac2`에서 이미 교정됐고 현재 source와
  동적 probe에 유지된다. 추가 resource-limit 변경은 근거가 없어 하지 않는다.
- 실행한 검증:
  - `cd pc_receiver; python -m pytest -q`
  - 결과: `17 passed in 1.35s`.
  - `python -` loopback probe로 cumulative deadline, admission semaphore,
    event queue eviction을 검증; 세 항목 모두 PASS.
- 수행하지 않은 검증: 실제 운영 PC 부하·Windows 종료, 실제 A→PC 동시 PDF
  전송은 수행하지 않았으며 그 조건의 PASS로 확대하지 않는다.
- 변경 파일·commit·rollback: 이번 source 변경 없음. 기존 교정 rollback은
  `git revert 47b76ec1d78b1bf3f96c9bab071762de040e3ac2`이지만 PDF idempotency와
  resource 보호를 함께 제거하므로 현재 rollback 사유가 없다.

### SOL-0005 — 원격 시험 QR 거부 후 스캐너 카메라가 재연결되지 않음

- 영역: 실제 A·Kiosk scanner·카메라 lifecycle 복구
- 심각도: P2
- 신뢰도: 높음
- 상태: 현장검증 완료
- 사용자 영향: 관리자가 허용된 원격 시험 QR로 stale·사용 불가 카드를 점검했을
  때 앱은 QR을 정상 거부하지만 카메라를 다시 연결하지 않았다. 화면은 scanner에
  남아도 다음 정상 카드를 읽을 수 없어 수업 QR 흐름이 관리자 개입 전까지
  중단된다.
- 재현 조건:
  1. active session의 QR 대기 scanner에서 원격 지원을 짧게 활성화한다.
  2. 정확히 `테스트`인 공식 PDF 카드 하나를
     `scripts/submit-test-qr-from-pdf.py`로 전달한다.
  3. 카드 검증이 null을 반환하는 stale·사용 불가 경로에서 카메라 client가
     다시 활성화되는지 확인한다.
- 기대 결과: 거부 기록과 cooldown 뒤 현재 lifecycle이 `STARTED` 이상이면
  scanner camera를 다시 bind하고, 이미 bind된 경우에만 analyzer를 재활성화한다.
- 실제 결과(수정 전 RC69): 원격 QR broadcast는 Kiosk에서 수락됐고 Web session
  event는 없었으며 DB session은 QR 대기 상태를 유지했다. 그러나 scanner camera
  client는 중단된 뒤 돌아오지 않아 다음 QR을 읽을 수 없었다.
- 정적 근거:
  - `handleRemoteQrTest()`는 scanner를 표시한 뒤 `stopCamera()`를 호출한다.
  - `stopCamera()`는 analyzer를 비활성화하고 `cameraProvider = null`로 만든 뒤
    모든 use case를 unbind한다.
  - null QR validation branch는 `resumeScannerAfterCooldown()`을 호출했지만 수정
    전 구현은 `qrAnalyzer?.setEnabled(true)`만 수행했다. provider가 없으므로
    analyzer를 켜도 camera frame이 공급되지 않는다.
- 수정 전 동적 근거:
  - 실제 A RC69에서 공식 PDF 원격 시험 QR이 수락된 뒤 camera client가 중단됐고
    cooldown 뒤에도 재연결되지 않았다. Web launch·답안 변경은 발생하지 않았다.
  - AVD `matholic_rc03_api33`의 임시 직접 회귀시험
    `scannerCooldownRebindsCameraAfterRemoteTestStopsIt`는 수정 전 1/1 실패했다.
    `cameraBindGeneration`이 증가하지 않아 UI 조건 timeout으로 끝났다.
- 원인·결정: camera가 이미 bind됐다는 잘못된 전제로 analyzer만 다시 켠 것이
  원인이다. UI callback에서 직접 lifecycle·provider 조합을 복제하지 않고
  `ScannerCameraResumePolicy`로 다음 세 동작을 결정하도록 최소 수정했다.
  - 관리자 수동 모드: `NONE`
  - provider가 이미 존재: `ENABLE_ANALYZER`
  - provider가 없고 lifecycle이 `STARTED` 이상: `REBIND_CAMERA` 후 `ensureCamera()`
  - background: `NONE`; 다음 `onStart()`가 camera를 복구
- 변경 파일:
  - `kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt`
  - `kiosk/src/main/java/com/local/matholickiosk/kiosk/domain/ScannerCameraResumePolicy.kt`
  - `kiosk/src/test/java/com/local/matholickiosk/kiosk/ScannerCameraResumePolicyTest.kt`
  - RC70/code 75와 release 경로를 맞춘 `kiosk/build.gradle.kts`, `README.md`,
    `scripts/build-release.ps1`, `scripts/provision-release-device-owner.ps1`,
    `scripts/verify-release-apks.ps1`
- 관련 commit: `485834859919319ee078e405d54b2a278edd889f`
  (`fix(kiosk): restore scanner after rejected remote QR (SOL-0005)`). 전용 origin
  branch push 성공, 갱신 직전 ahead/behind `0/0`.
- 자동검증:
  - 임시 AVD 직접 회귀시험은 수정 전 1/1 timeout 실패, 수정 후 1/1 PASS
    (`4.462s`). CAMERA permission이 다른 시험에 남는 부작용을 피하기 위해 이
    임시 Activity 시험은 source에서 완전히 제거했고, 순수 policy 회귀시험을
    영구 보존했다.
  - `ScannerCameraResumePolicyTest` 3/3 PASS.
  - Kiosk JVM unit 87/87, Web POC JVM unit 65/65 PASS;
    failures/errors/skipped 0.
  - `:kiosk:compileDebugAndroidTestKotlin` PASS.
  - `MainActivityInstrumentedTest` 전체는 15/16 PASS. 기존
    `qrHelpFitsTheScannerViewportAndUsesTheBadgeSimulation`이 UI condition timeout으로
    실패했고 단독 실행도 1/1 실패(`16.943s`)했다. 임시 회귀시험이 CAMERA
    permission을 남긴 첫 전체 실행의 추가 실패는 시험 제거와 permission revoke
    뒤 사라졌고, scanner header 시험은 통과했다. 따라서 전체 계측 PASS로
    기록하지 않는다.
- release 검증:
  - 첫 release build의 Gradle tasks는 성공했지만 Windows PowerShell 5.1이
    BOM 없는 UTF-8 verifier의 한글 app label literal을 직접 parse하지 못해
    artifact verification이 실패했다. label을 Unicode code point 조합으로 바꾼
    뒤 Windows PowerShell 5.1 직접 parse와 수동 verifier가 모두 통과했다.
  - `scripts/build-release.ps1`를 처음부터 다시 실행해 158 tasks를 완료했고
    `BUILD SUCCESSFUL in 2m 29s`; Kiosk/Web unit, release lint, assemble, 최초·보관
    artifact verifier가 모두 통과했다.
  - RC70 APK:
    `artifacts/matholic-kiosk-0.6.0-rc70-release.apk`, 36,671,949 bytes,
    SHA-256
    `1B78497A836DB0749ABAF395424B94717BAD1DCFCBB20CC48D7226925247149F`,
    signer SHA-256
    `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.
    `artifacts/`는 ignore 상태이고 Git에 stage하지 않았다.
- A 설치·현장검증:
  - 설치 전 승인 A 한 대, Kiosk RC69/code 74, Web RC132/code 149, signer 일치,
    UID/first install, Device Owner, HOME, Lock Task와 정확한 `ADMIN_IDLE` 안전 지점을
    확인했다.
  - `adb install -r artifacts\matholic-kiosk-0.6.0-rc70-release.apk`는 `Success`.
    Kiosk는 RC70/code 75로 올라갔고 UID 10288·first install·관리자 등록·Device
    Owner·HOME가 보존됐다. 현재 process fatal/ANR는 0/0이다.
  - 임시 반 `SOL-TEST-0804-2030`을 만들고 정확히 `테스트` 한 명만 구성한 뒤
    session을 시작했다. 실제 학생·답안·점수는 사용하거나 변경하지 않았다.
  - 원격 지원 15분 window에서 공식 PDF 하나를 허용된 도구로 제출했다.
    표시명 exact check와 remote QR acceptance가 성공했고, camera는 제출 전 active
    → 원격 시험 중 stop → cooldown 뒤 reconnect → 최종 active로 전이했다.
    Web session event는 없었고 Kiosk top·Lock Task `LOCKED`를 유지했다.
  - 원격 지원을 Stop하고 정상 관리자 경로로 session을 안전 종료했다. 임시 반을
    삭제한 뒤 spinner를 위·아래 끝까지 안정화해 전체 13개 반에 fixture가 없음을
    확인했다. 최종 화면은 `관리자 인증`·`ADMIN_IDLE`·`보안 적용`, Lock Task
    `LOCKED`; 원격 지원 `INACTIVE`, Settings task와 원격 임시 캡처는 없다.
- 실패·제약·반대 근거:
  - 두 사전점검 script가 각각 PowerShell 예약/읽기 전용 변수 `$home`, `$pid`를
    사용해 실패했고, 한 script는 Samsung HOME resolver의 두 줄 출력을 한 줄로
    가정해 실패했다. 변수명과 parser를 진단 script에서만 고쳐 재확인했으며 그
    실패 시점에는 install·PIN·제품 상태 변경이 없었다.
  - Windows PowerShell 5.1로 관리자 PIN 도구를 호출한 한 번은
    `SHA256.HashData` API 부재로 PIN 전송 전에 실패했다. 현재 PowerShell에서
    전용 도구를 직접 실행해 성공했으며 잘못된 PIN 시도는 없었다.
  - session preflight 확인과 UI polling 일부는 기대 문자열을 과도하게 제한하거나
    camera analyzer 중 `uiautomator` idle을 기다려 timeout했다. 실제 state와 source
    계약을 다시 확인한 뒤 안전하게 계속했으며 제품 실패로 세지 않았다.
  - 공식 PDF 원격 hash 경로는 camera reconnect를 증명하지만 실물 카드의 광학
    인식·인쇄 품질·반사·거리·조명을 증명하지 않는다. 답안 입력·제출·60초 지연
    복원 시험도 이번 finding에 필요하지 않아 수행하지 않았다.
- rollback:
  - 코드: `git revert 485834859919319ee078e405d54b2a278edd889f` 후 policy unit,
    Kiosk/Web unit, AndroidTest compile, release build/verifier를 다시 실행한다.
  - A는 code 75에서 downgrade 설치할 수 없다. 기기 rollback이 필요하면 revert된
    source를 같은 signer와 versionCode 75보다 높은 복구 release로 만들어
    `adb install -r`하는 forward rollback을 사용한다. uninstall·`pm clear`·서명이
    다른 APK는 사용하지 않는다.

### SOL-0006 — scanner에서 원격 점검 활성 배지가 숨겨짐

- 영역: Kiosk scanner·원격 지원 고지·개인정보 UI
- 심각도: P2
- 신뢰도: 높음
- 상태: 현장검증 완료
- 사용자 영향: 원격 지원이 활성인 동안 QR·학생 이름·학습 화면을 캡처할 수
  있지만 scanner에서는 활성 표시가 전혀 보이지 않았다. 교사와 주변 사용자는
  캡처 가능 상태를 즉시 알아차릴 수 없어 README와 확인 대화상자의 명시적 고지
  계약을 위반한다.
- 재현 조건: active session의 QR 대기 scanner에서 원격 지원을 활성화한다.
- 기대 결과: README와 `원격 점검 30분 시작` 대화상자 설명처럼 화면 우측 상단에
  `원격 점검 중` 배지가 계속 보이고 scanner 제어·안내와 겹치지 않는다.
- 실제 결과(수정 전): 원격 지원 상태 자체와 캡처는 활성인데 scanner에는 배지가
  보이지 않았다. RC69 실제 A에서 관찰했고 RC70 source에도 같은 구조가 유지됐다.
- 정적 근거:
  - `remote_support_badge`는 `app_header`의 child였다.
  - `showScanner()`와 PC pairing scanner는 `appHeader.visibility = View.GONE`으로
    전체 header를 숨긴다.
  - `RemoteSupportWindowController`는 header 안의 단일 badge visibility만 바꿔,
    active 상태여도 숨겨진 parent를 벗어나 표시할 수 없었다.
- 회귀시험 선행 증거:
  - `Gate5ManifestInstrumentedTest.remoteSupportBadgeRemainsVisibleWhenScannerHidesHeader`
    를 scanner 전용 badge resource가 없으면 실패하도록 먼저 추가했다.
  - `ANDROID_SERIAL=emulator-5556`과 Android SDK 경로를 고정한 수정 전 단일 실행은
    1/1 `AssertionError`로 실패했다. 첫 재시도는 SDK 환경변수 누락으로 시험 전
    Gradle 구성에서 실패했으며 제품 실패로 세지 않는다.
- 원인·결정:
  - 기존 header badge와 정렬을 유지하고, scanner panel의 마지막 overlay child로
    같은 모양의 `scanner_remote_support_badge`를 추가했다.
  - controller가 active/inactive 전이 때 두 badge를 함께 `VISIBLE`/`GONE`으로
    바꾼다. scanner overlay는 top/end 20dp, elevation 32dp로 도움말 overlay 위에도
    고지가 유지되며 기존 하단 동작 제어와 위치가 분리된다.
- 변경 파일:
  - `kiosk/src/main/res/layout/activity_main.xml`
  - `kiosk/src/main/java/com/local/matholickiosk/kiosk/RemoteSupportWindowController.kt`
  - `kiosk/src/androidTest/java/com/local/matholickiosk/kiosk/Gate5ManifestInstrumentedTest.kt`
  - RC71/code 76 및 release 경로를 맞춘 `kiosk/build.gradle.kts`, `README.md`,
    `scripts/build-release.ps1`, `scripts/provision-release-device-owner.ps1`,
    `scripts/verify-release-apks.ps1`
- 관련 commit: `c982874c49b319722c7c9bb366dee333af6db43d`
  (`fix(kiosk): keep remote support badge visible in scanner (SOL-0006)`). 전용 origin
  branch push 성공, 증거 갱신 직전 ahead/behind `0/0`.
- 자동검증:
  - 수정 후 `Gate5ManifestInstrumentedTest` 전체 8/8 PASS. 첫 수정 후 실행은
    detached root에서 `View.isShown`을 사용한 시험 하니스 때문에 7/8이었고,
    ancestor visibility를 직접 검사하도록 고친 뒤 통과했다.
  - `MainActivityInstrumentedTest.scannerHidesHeaderAndUsesAccessibleIconControls` 1/1
    PASS.
  - 활성화와 비활성화를 모두 검사하는
    `remoteSupportControllerUpdatesHeaderAndScannerBadgesTogether` 최종 단독 1/1 PASS.
  - Kiosk JVM unit 87/87, Web POC JVM unit 65/65 PASS;
    failures/errors/skipped 0.
  - `scripts/build-release.ps1`: 158 tasks, `BUILD SUCCESSFUL in 2m 38s`; Kiosk/Web
    unit, release lint, assemble, 최초·보관 artifact verification PASS.
  - Windows PowerShell 5.1에서 `scripts/verify-release-apks.ps1`를 직접 실행해
    별도로 PASS했다.
  - 이 cycle에서는 기존 실패가 있는 `MainActivityInstrumentedTest` 전체 class를
    다시 실행하지 않았다. 직전 cycle의 15/16과 `qrHelp...` 단독 timeout은 열린
    제약으로 그대로 유지한다.
- RC71 artifact:
  - `artifacts/matholic-kiosk-0.6.0-rc71-release.apk`
  - versionName `0.6.0-rc71`, versionCode 76, 36,672,045 bytes
  - SHA-256
    `0CAC0CF2B3485CE53C0BF413BFC9418971FA0A220B4A8AB343B885091248F933`
  - signer SHA-256
    `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
  - `artifacts/`는 ignore 상태이고 stage하지 않았다.
- A 설치·현장검증:
  - 설치 전 승인 A 한 대, RC70/code 75→RC71/code 76 증가, signer·artifact hash,
    UID 10288·first install, Device Owner, HOME, Lock Task와 `ADMIN_IDLE` 안전 화면을
    모두 확인했다.
  - `adb install -r artifacts\matholic-kiosk-0.6.0-rc71-release.apk`는 `Success`.
    설치 직후 전용 HOME process 교체 동안 Samsung Recents가 전면이고 Lock Task가
    `NONE`여서 첫 postcheck를 실패로 처리했다. 정상 HOME key로 Kiosk를 재진입한
    뒤 RC71/code 76, UID·first install·관리자 등록·Device Owner·HOME 보존,
    `관리자 인증`·`ADMIN_IDLE`·Lock Task `LOCKED`와 process fatal/ANR 0/0을 확인했다.
  - 임시 반 `SOL-TEST-0804-2105`에 정확히 `테스트` 한 명만 구성하고 session을
    시작했다. 답안·점수·QR 제출은 하지 않았다.
  - scanner camera active 상태에서 원격 지원 15분 window를 시작했다. 실제 A
    2000×1128 캡처를 직접 확인한 결과 `원격 점검 중`이 우측 상단에 완전히
    표시됐고 도움말, 렌즈 안내, 중앙 문구, 하단 카메라·관리자 제어와 겹치거나
    잘리지 않았다. 캡처는 보고서·Git에 보존하지 않고 즉시 Stop·삭제했다.
  - session을 정상 안전 종료하고 fixture를 삭제했다. spinner 위·아래 끝이 모두
    안정될 때까지 확인한 13개 반에 fixture가 없었다. 최근 앱→HOME lifecycle로
    다시 잠가 최종 `관리자 인증`·`ADMIN_IDLE`·`보안 적용`, Kiosk top, Lock Task
    `LOCKED`, 원격 지원 `INACTIVE`, 임시 캡처 없음, fatal/ANR 0/0을 확인했다.
- 도구 실패·반대 근거:
  - membership 저장 좌표 계산에서 존재하지 않는 `ValueAsInt` 속성을 사용해
    `(0,0)`을 한 번 탭했지만 대화상자와 checked 상태가 그대로임을 확인한 뒤
    정확한 저장 버튼을 한 번 눌렀다.
  - scroll 안정화 뒤 낡은 좌표로 session 시작 대신 보강 학생 대화상자를 열었으나
    `SOL-TEST-0804-2105 이번 수업 보강 학생`임을 확인하고 Back으로 취소했다.
    fresh bounds와 정확한 preflight를 확인한 뒤 시작했으며 보강 명단 변경은 없다.
  - 실제 A의 한 landscape 해상도에서 검증했다. 다른 화면 크기·회전, 접근성
    글자 확대와 Web POC 화면의 badge는 이번 변경 범위에서 새로 검증하지 않았다.
- rollback:
  - 코드: `git revert c982874c49b319722c7c9bb366dee333af6db43d` 후 Gate5 계측,
    scanner Activity 계측, Kiosk/Web unit과 release build/verifier를 다시 실행한다.
  - A는 code 76을 downgrade할 수 없다. 기기 rollback은 revert source에서 같은
    signer와 versionCode 76보다 높은 forward-recovery release를 만들어
    `adb install -r`한다. uninstall·`pm clear`·서명이 다른 APK는 사용하지 않는다.

### SOL-0007 — QR 도움말 layout 시험이 실행 AVD를 지원 A로 오인함

- 영역: Kiosk 계측시험·UI layout 검증 재현성
- 심각도: P4
- 신뢰도: 높음
- 상태: 자동검증 완료
- 사용자 영향: release 앱 동작 결함은 아니지만, 일반 Pixel 2형 AVD에서 지원 A용
  도움말 layout 시험이 항상 timeout으로 실패해 전체 계측 결과를 15/16으로
  만들었다. 실제 회귀와 시험 환경 불일치를 구분하기 어렵고 검증 신뢰도가 낮아진다.
- 재현 조건:
  - AVD `matholic_rc03_api33`은 1080×1920·420dpi Pixel 2 profile이며 landscape
    앱 영역은 1920×1080, 논리 폭은 약 731dp다.
  - 이 AVD에서
    `MainActivityInstrumentedTest#qrHelpFitsTheScannerViewportAndUsesTheBadgeSimulation`
    을 단독 실행한다.
- 기대 결과: 시험 이름과 제품 기준처럼 지원 A의 2000×1128·240dpi·font scale
  1.1 viewport에서 도움말 경고·시뮬레이션·렌즈 보정이 화면 안에 있는지를 실행
  기기 profile과 무관하게 검증한다.
- 실제 결과(수정 전): 시험은 현재 Activity window와 density를 암묵적으로 사용했다.
  420dpi AVD의 약 731×411dp viewport에 1180dp 도움말 카드를 측정하면서 마지막
  경고 view 높이가 0이 됐고 15초 조건 대기가 timeout했다.
- 정적·동적 근거:
  - source는 `ActivityScenario` 실행 직후 scanner/help visibility만 직접 바꾸고
    실제 runner window에서 자식 크기를 기다렸다. A 기준 configuration이나 exact
    viewport 측정은 없었다.
  - 수정 전 단독 실행은 1/1 실패, `BUILD FAILED in 36s`; 정확한 실패는
    `UI condition was not met before timeout`이다.
  - 초기 상태 로딩 race가 원인이라는 첫 가설에 따라 PIN UI 완료 대기를 임시로
    추가했지만 다시 1/1 실패, `BUILD FAILED in 54s`였다. 이 임시 수정은 최종
    diff에 남기지 않았고 초기화 race를 단독 원인으로 기각했다.
  - 실패 대기 중 emulator UI hierarchy를 읽은 결과 scanner/help panel과
    시뮬레이션은 표시됐지만 경고 view는 hierarchy에 없었다. AVD 420dpi와 실제 A
    240dpi를 각각 `wm density`로 대조했다.
- 원인·결정:
  - 지원 기기 A 전용 layout 계약을 임의 instrumentation runner의 display profile로
    측정한 시험 하니스 결함이다.
  - A 기준 `Configuration`(240dpi, font scale 1.1, landscape, 1333×752dp,
    smallest width 800dp)을 만든 뒤 layout을 off-screen inflate한다. 실제 scanner
    상태처럼 header/auth/admin을 숨기고 scanner/help를 보인 다음 2000×1128 exact
    measure/layout으로 경고·시뮬레이션 크기, 경고 하단 경계, 그림·접근성 문구와
    렌즈 13dp 보정을 검사하도록 교정했다.
- 변경 파일:
  - `kiosk/src/androidTest/java/com/local/matholickiosk/kiosk/MainActivityInstrumentedTest.kt`
- 관련 commit: `219d0c45f813bfce00c4dc070a442c7c2c552520`
  (`test(kiosk): measure QR help at device A viewport (SOL-0007)`). 전용 origin branch
  push 성공, 증거 갱신 직전 ahead/behind `0/0`.
- 자동검증:
  - 정정 후 같은 420dpi AVD에서 대상 단독 1/1 PASS,
    `BUILD SUCCESSFUL in 36s`.
  - `MainActivityInstrumentedTest` 전체 16/16 PASS, failures/errors/skipped 0,
    `BUILD SUCCESSFUL in 1m 43s`; 결과 XML 실행 시간 85.213초.
  - Kiosk JVM unit 87/87 PASS, failures/errors/skipped 0.
  - `:kiosk:lintDebug` PASS, unit과 함께 `BUILD SUCCESSFUL in 1m 18s`.
- 기기·릴리스 검증:
  - 제품 source, resource, version과 APK가 바뀌지 않은 test-only 정정이므로 새
    release build·A 설치를 수행하지 않았다.
  - A에 debug signer 계측 APK를 설치하지 않았다. AVD를 종료한 뒤 승인 물리 A
    한 대만 남고 Kiosk RC71/code 76, top resumed, Lock Task `LOCKED`, 원격 지원
    `INACTIVE`임을 확인했다.
  - RC69 실제 A에서 QR 도움말 그림과 아래 문구가 잘리지 않았다는 기존 현장 증거는
    반대 근거로 유지하지만 이번 cycle에 도움말을 다시 열어 캡처하지는 않았다.
- 제약·정리:
  - 이 시험은 지원 A 기준을 결정적으로 검증하며 작은 phone viewport의 제품
    호환성을 증명하지 않는다. 420dpi AVD에서 실제 layout이 잘린 사실을 A의 결함
    또는 다른 기기 지원 PASS로 확대하지 않는다.
  - emulator 진단 XML은 `/sdcard/sol-layout-diagnostic.xml`에서 즉시 삭제했고,
    AVD `emulator-5556`도 종료했다. 저장소에는 사용자 소유 `outputs/` 외 새
    미추적 파일이 없다.
- rollback: `git revert 219d0c45f813bfce00c4dc070a442c7c2c552520` 후 대상
  단독·MainActivity 전체 계측, Kiosk unit과 debug lint를 다시 실행한다. A 설치본과
  release artifact에는 영향이 없다.

## 최근 변경·검증·전달

- 누적 보고서 기준선 commit:
  `e24a7d5cb9b73305feb7c82d226346e63b986e12`.
- `SOL-0001` 운영 문서 교정 commit:
  `b5b6fe4ec2a2506af7002dabf5204f7de2cc7545`.
- `SOL-0001` 증거·다음 큐 commit:
  `9a76515ec5caf39a803264512d33a47fffee93c0`.
- `SOL-0002` 기각·다음 큐 commit:
  `efe86575646e1d27e517a22fdf6151c0fff82478`.
- `SOL-0003` 이미 수정됨·다음 큐 commit:
  `abb23855fa5d8db908d318772826198ac5172877`.
- `SOL-0004` 이미 수정됨·다음 큐 commit:
  `9b9b4fe698d19a8a4b0e0d4df884040d462dfab5`.
- `SOL-0005` scanner camera 복구 구현·회귀·RC70 release 준비 commit:
  `485834859919319ee078e405d54b2a278edd889f`.
- `SOL-0005` 실제 A 설치·현장검증 증거 commit:
  `a5dcc3e3de1bc76089231e8adb272e7f7b3c88ae`.
- `SOL-0006` scanner 원격 지원 배지 구현·회귀·RC71 release 준비 commit:
  `c982874c49b319722c7c9bb366dee333af6db43d`.
- `SOL-0006` 실제 A 설치·현장검증·release 운영 기준 증거 commit:
  `8916c80ab3a20fb564c5b3ca2563050399c48e8b`.
- `SOL-0007` 지원 A 기준 QR 도움말 layout 계측 정정 commit:
  `219d0c45f813bfce00c4dc070a442c7c2c552520`.
- 위 구현·증거 commit은 모두 전용 원격 branch push 성공. 이 보고서의 SOL-0007
  검증 근거와 다음 큐는 이 문서 checkpoint에서 정렬한다.
- rollback 수행 없음.
