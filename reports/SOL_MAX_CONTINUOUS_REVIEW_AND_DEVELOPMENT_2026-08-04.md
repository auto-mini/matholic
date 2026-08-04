# Sol Max 연속 리뷰·개발 누적 보고서

## 현재 상태

- `last_updated`: 2026-08-04 19:22:51 +09:00
- 현재 branch: `codex/sol-continuous-development-20260804`
- 현재 branch tip / upstream: `abb23855fa5d8db908d318772826198ac5172877` /
  `origin/codex/sol-continuous-development-20260804`
- 마지막 push 성공 commit: `abb23855fa5d8db908d318772826198ac5172877`
- 최초 보존 기준선: `master`의
  `ccf410d6b9758c7594a07e94e459bf7e83c554bc`; 당시 `origin/master`보다
  24 commits ahead
- 현재 보존 대상: Goal 시작 전부터 있던 미추적 `outputs/`. 수정·stage·삭제하지
  않는다.
- 현재 작업 중: `SOL-0005` — 실제 A 전체 흐름·화면 품질 재검증
- 다음 우선 큐:
  1. 실제 A의 관리자→QR→시험계정→문제→종료 흐름과 화면 품질 재검증
  2. PC receiver의 Windows 시작 시 LAN 주소 탐색·복구 경계 재감사

### 열린 finding과 제약

- 열린 P0/P1: 없음. 과거 보고서의 후보는 현재 source와 독립 재검증 전에는
  열린 결함으로 승격하지 않는다.
- 현재 검토 P2 후보: `SOL-0005` 1건. 아직 결함으로 확정하지 않았다.
- 완료 P3: `SOL-0001` 1건.
- 기각: `SOL-0002` 1건.
- 이미 수정됨: `SOL-0003`, `SOL-0004` 2건.
- 사용자 판단 대기: 없음.
- 현재 제약:
  - 비민감 화면 확인을 위해 원격 지원 Start→Capture를 시도했으나 태블릿이
    screenshot을 거부했다. 실패 직후 Stop을 실행해 `REMOTE_SUPPORT=INACTIVE`를
    확인했다. 화면 내용과 세션 상태는 미검증이며 성공으로 기록하지 않는다.
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

### SOL-0005 — 실제 A 전체 흐름·화면 품질 재검증

- 영역: 실제 A·핵심 사용자 흐름·UI/UX
- 심각도: P2 후보
- 신뢰도: 낮음
- 상태: 후보
- 사용자 영향 후보: 관리자→수업→QR 대기→시험계정 로그인→문제풀이→종료·재개
  중 화면 잘림·입력 가림·상태 불일치나 답안 잔존이 있으면 현장 채점 흐름이
  중단되거나 잘못된 계정·과제 상태를 이어갈 수 있다.
- 현재 근거: 비민감 원격 캡처가 태블릿에서 거부돼 현재 화면은 아직 확인하지
  못했다. 원격 지원을 짧게 다시 시도하고 실패하면 상태를 안전하게 정리한 뒤,
  가능한 비파괴 ADB/UI 증거와 독립 코드 경계를 검토한다.

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
- 위 commit 모두 전용 원격 branch push 성공.
- rollback 수행 없음.
