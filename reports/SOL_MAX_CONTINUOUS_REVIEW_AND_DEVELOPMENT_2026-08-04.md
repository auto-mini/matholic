# Sol Max 연속 리뷰·개발 누적 보고서

## 현재 상태

- `last_updated`: 2026-08-13 03:49:10 +09:00
- 현재 branch: `codex/sol-continuous-development-20260804`
- 보고서 갱신 직전 branch tip / upstream:
  `6cdbd953f052f2dcb9ce61cd782dd13f1752f3b0` /
  `6cdbd953f052f2dcb9ce61cd782dd13f1752f3b0`; ahead/behind `0/0`.
- 마지막 push 성공 commit:
  `6cdbd953f052f2dcb9ce61cd782dd13f1752f3b0`
  (`fix(kiosk): serialize CSV intake with sessions (SOL-0018)`). 이 상태기록 문서
  commit은 위 스냅샷 다음에 생성·push하므로 최종 원격 tip은 Git tracking
  상태를 따른다.
- 최초 보존 기준선: `master`의
  `ccf410d6b9758c7594a07e94e459bf7e83c554bc`; 당시 `origin/master`보다
  24 commits ahead.
- 현재 보존 대상: Goal 시작 전부터 있던 미추적 `outputs/`. 수정·stage·삭제하지
  않는다.
- 실제 A 마지막 package·전면·Lock Task 독립 확인: 2026-08-13 03:49 +09:00.
  승인 ADB device는 serial
  `R54TB029FHZ`, model `SM-P610` 한 대뿐이다.
  - Kiosk `0.6.0-rc89`/code 94, UID 10288, first install
    `2026-07-24 12:52:28`, last update `2026-08-13 03:37:37`.
  - A에서 다시 읽은 Kiosk 설치 APK는 RC89 release artifact와 같은 36,754,057 bytes·
    SHA-256
    `B67B2BCF8D81FCF7D770627F62B14930D94DA4CA519390F3FC59945D25D2B93F`다.
    v2 signer SHA-256은 기존 release signer
    `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`와
    일치한다.
  - 변경되지 않은 Web POC `0.4.0-rc137`/code 154 설치 APK도 현재 artifact와
    3,393,698 bytes·SHA-256
    `D49BFB81A727CA94D7D94E9D5A5D88C4151EB63F22F742F1C06C59972ABC9651`·
    signer가 일치한다. Web은 재설치하지 않았다.
  - Device Owner와 preferred HOME은 각각
    `com.local.matholickiosk.kiosk/.admin.KioskDeviceAdminReceiver`,
    `com.local.matholickiosk.kiosk/.MainActivity`로 유지됐다.
  - 설치 package는 `com.local.matholickiosk.kiosk/.print.QrPdfFileProvider`를
    등록한다. Kiosk가 top resumed·실행 중이고 Lock Task `LOCKED`, test package와
    ADB forward/reverse가 없다. 원격 지원과 device/local 임시 캡처는 모두
    `INACTIVE`/부재이며 Kiosk crash buffer와 exit-info의 crash/ANR은 0건이다.
  - RC89 보존 설치 재시작으로 남은 세션이 `RECOVERY_REQUIRED`가 됐다. 정확한 확인창의
    데이터 보존 문구를 확인하고 원버튼 안전 복구로 현재 세션과 임시 명단만 종료했다.
    선택 반 인원 4명과 신규용 카드 무료 4장이 유지됐고, Web 사전점검을 거쳐 실제
    전면 카메라 QR 대기로 복원했다. 캡처에서 잘림·겹침·오류 표시는 없었다.
- 운영 PC에는 PC 수신기 0.1.8을 같은 설치 path에 보존 교체했다. 설치 EXE와
  artifact는 23,187,199 bytes·SHA-256
  `55BF10A5AB6E41B94A18478D38CFD4C9F1B4ACDCA0609667C8B9ED857C92D3A0`로
  일치한다. 독립 smoke exit 0, background parent/child 2개,
  `0.0.0.0:48129` listener 1개, established 0개다. Startup shortcut,
  `Private`/TCP 48129 방화벽 rule과 기존 DPAPI config를 보존했다.
- 현재 작업 중 finding: `SOL-0018` 증거 체크포인트. 지정 PC 학생 CSV 가져오기가
  네트워크·parse·미리보기 동안 공통 관리자 gate 밖에 남은 것을 수정했다. 수정 전
  지연 PC 계측 실패, 수정 후 focused·unit/lint/assemble·전체 78/78, 공식 RC89 release,
  구현 commit·push와 A 보존 설치·기본 흐름을 완료했다. 정확한 CSV A/운영 PC 분기는
  실제 학생정보를 바꾸지 않기 위해 수행하지 않아 판정은 `자동검증 완료`다.
- 다음 우선 큐: 과거 `LUNA-0025`의 활성 수업 중 반 소속 변경·pending undo 우회
  후보를 현재 repository active-session guard, 즉시 UI fail-close와 계측으로 독립
  재검증한다. 과거 해결 표시는 현재 증거로 다시 확인하기 전 결론으로 사용하지 않는다.

### 열린 finding과 제약

- 열린 P0/P1: 없음. 과거 보고서 후보는 현재 source와 독립 재검증 전에는 열린
  결함으로 승격하지 않는다.
- 자동검증 완료 P2: `SOL-0009`, `SOL-0013`, `SOL-0014` 3건. QR 고정 불변조건,
  무응답 PC startup 격리와 PDF write timeout은 전체 계측으로 확인했고 RC87까지
  A에 설치했지만 실제 운영 카드의 QR을 변경하거나 운영 PC를 고의로 stall시켜
  정확한 영향 분기를 재현하지 않았다.
- 현장검증 완료 P2: 기존 `SOL-0003`, `SOL-0005`, `SOL-0006`, `SOL-0008`과
  신규 `SOL-0010` 5건.
- 완료 P3: `SOL-0001`; 자동검증 완료 P3: `SOL-0011`, `SOL-0012`, `SOL-0015`,
  `SOL-0016`, `SOL-0018`.
- 자동검증 완료 P4: `SOL-0007`; 기각 `SOL-0002`; 이미 수정됨 `SOL-0004`,
  `SOL-0017`.
- 신규용 카드 실제 이름·ID·PW 입력, 배정, 회수, 일반 QR 전환은 실제 학생
  데이터를 바꾸므로 수행하지 않았다. RC84에서 메뉴 목록과 배정 폼 진입을 확인해
  입력 없이 취소했고, RC85에서는 무료 4장 메뉴까지 다시 확인했다.
- 저장 미확인 슬롯이 0장이어서 RC86의 기존 QR 폐기·새 QR 전송 경고창은 A에서
  강제로 만들지 않았다. 최종 source compile, lint, release와 전체 계측은 통과했다.
- 현재 A에는 비활성 재사용 슬롯이 없어 SOL-0012 복구 분기를 고의로 만들지
  않았다. 해당 분기는 수정 전 실패·수정 후 통과 focused 시험과 전체 71개
  instrumentation으로 확인했으며 A 실물 통과로 확대하지 않는다.
- 현재 A에는 새로 준비할 재사용 카드가 없어 SOL-0013의 자동 PDF 전송이 시작되지
  않았다. loopback receiver가 연결만 받고 ACK를 보류한 수정 전 실패·수정 후
  통과 시험과 전체 72개 instrumentation으로 확인했으며, A의 설치·인증·관리자·
  복구·Web 사전점검·QR 대기 통과를 정확한 무응답 PC 분기 통과로 확대하지 않는다.
- SOL-0014의 “연결 뒤 읽지 않는 receiver”도 운영 PC에서 고의 재현하지 않았다.
  최대 5MiB frame·1KiB receive buffer의 JVM에서 수정 전 무기한 대기, JVM·Android
  수정 후 bounded timeout을 확인했고 전체 73개 instrumentation을 통과했다.
  실제 Windows PC·Wi-Fi·disk의 장시간 partial stall 현장 통과로 확대하지 않는다.
- SOL-0015의 정확한 공유 중 process kill·1시간 경과·외부 URI 재접근은 실제 A에서
  강제하지 않았다. 합성 비-QR 파일의 수정 전 실패·수정 후 남은 수명 삭제,
  manifest provider 교체와 provider 초기화 선행 삭제, 전체 76개 계측으로 확인했다.
  process가 계속 종료된 동안 private cache 파일이 물리적으로 남는 것과 이미 읽은
  외부 수신기 복사본은 앱이 회수하지 못하는 잔여 경계다.
- SOL-0016의 정확한 운영 전송 중 tray Quit·Windows 로그오프·종료는 실제 PC에서
  강제하지 않았다. 수정 전 partial socket 잔존 실패, 수정 후 socket close·slot
  회수와 state operation 50ms bounded fallback, app cleanup 순서를 loopback으로
  확인했다. Computer Use에는 background tray의 targetable window가 없어 Explorer
  좌표를 추측하지 않았고 앱·연결·파일 상태를 바꾸지 않았다.
- SOL-0017은 제품 source가 이미 `d5589373`에서 idle session 선투영으로 수정돼
  있었다. 신규 fault-injection은 종료 commit 뒤 snapshot refresh만 실패시켜 DB·
  Activity·버튼·scanner의 idle 일치를 확인했다. 첫 전체 77개는 기존 무응답-PC
  시험의 null adapter polling race 한 건으로 실패했고, 격리 1/1 통과로 비결정성을
  확인한 뒤 null-safe readiness로 보강했다. 최종 focused 2/2와 전체 77/77은
  통과했다. androidTest-only 변경이라 release build·A 재설치는 수행하지 않았다.
- SOL-0018의 정확한 운영 PC/A CSV 수신은 수행하지 않았다. 운영 PC에 대기 CSV가
  없고 실제 학생정보를 변경하지 않기 위해 지연 loopback PC와 합성 pairing으로
  요청 중 공통 gate, 수업 시작·학생·반 제어 차단과 종료 후 복구를 검증했다. 첫
  전체 78개는 기존 빠른 반 geometry 시험의 spinner readiness race 한 건으로
  실패했고, 격리 1/1 통과 후 adapter·버튼 readiness를 보강해 focused 2/2와 최종
  78/78을 통과했다. A는 RC89 보존 설치와 기본 운영 흐름 통합 회귀까지만 확인했다.
- 이번 RC89에서도 신규용 카드의 실제 프린터 출력·절단·코팅·부착과 카메라 광학
  왕복은 수행하지 않았다. 과거 RC61 일반 QR 실물 통과를 신규 카드 실물 통과로
  확대하지 않는다.
- 첫 최종 계측 소스 빌드는 import 결과에 preview 전용 필드명을 사용한 시험 코드
  오타로 compile 실패했다. 실제 `cardsNeedingPdf`로 고친 뒤 최종 소스 전체
  70/70을 다시 실행해 통과했다.
- SOL-0012 회귀시험은 수정 전 활성 슬롯 3장 상태에서 기대 복구 1장·실제 0장으로
  실패했다. 구현 뒤 focused `OK (1 test)`와 최종 전체 `OK (71 tests)`로 통과했다.
- 롤백은 `git revert 59c9181144ba8758a6710ff793dbcdac7a24a57b` 후 Kiosk unit,
  lint, AndroidTest assemble·전체 계측과 공식 release build를 다시 실행한다.
  A는 이미 Room v5·code 94이므로 APK 삭제나 downgrade를 하지 않는다. 되돌린
  기능 source에서도 v5를 읽을 수 있게 유지하거나 명시적 forward-compatible
  migration을 마련하고, 같은 signer·code 95 이상의 복구 release를
  `adb install -r`로 설치해야 한다.
- SOL-0012만 되돌리려면
  `git revert e7c558da18152795d11427d13d8d61b5323e1640` 후 Kiosk unit, lint,
  AndroidTest assemble·전체 계측과 공식 release를 다시 실행한다. A는 이미
  code 94이므로 같은 signer·code 95 이상의 forward rollback을 사용하며 APK
  삭제·data clear·downgrade를 하지 않는다. Room schema는 계속 v5다.
- SOL-0013만 되돌리려면
  `git revert b8ce2d394e5f02655a4bbe70842d4dde7bde1466` 후 Kiosk unit, lint,
  AndroidTest assemble·전체 계측과 공식 release를 다시 실행한다. A는 이미
  code 94이므로 되돌린 source에서 같은 signer·code 95 이상의 forward rollback
  release를 만들어 `adb install -r`로 설치하며 APK 삭제·data clear·downgrade를
  하지 않는다.
- SOL-0014만 되돌리려면
  `git revert 51d1b03c7694f685e91421c0c8953fc9b8aae329` 후 Kiosk unit, lint,
  AndroidTest assemble·전체 계측과 공식 release를 다시 실행한다. A는 이미
  code 94이므로 되돌린 source에서 같은 signer·code 95 이상의 forward rollback
  release를 만들어 `adb install -r`로 설치하며 APK 삭제·data clear·downgrade를
  하지 않는다.
- SOL-0015만 되돌리려면
  `git revert a5905540cd9a74d589bb78fee7ea247bb35a56ac` 후 Kiosk unit, lint,
  AndroidTest assemble·전체 계측과 공식 release를 다시 실행한다. A는 이미
  code 94이므로 되돌린 source에서 같은 signer·code 95 이상의 forward rollback
  release를 만들어 `adb install -r`로 설치하며 APK 삭제·data clear·downgrade를
  하지 않는다.
- SOL-0016만 되돌리려면
  `git revert 850b6f31ad272c71175cb724d6d4058b9d295752` 후 PC receiver 전체 pytest,
  compileall, PyInstaller package와 독립 smoke를 다시 실행한다. 운영 PC는 active
  연결 0개를 확인한 뒤
  `%LOCALAPPDATA%\MatholicPdfReceiver\backup\MatholicPdfReceiver-before-0.1.8-20260813-024559.exe`
  의 SHA-256
  `E88C7DBB3443F53EBB47DFB44B25D91F868DEA72B62C7BC9031D83915813CEF2`를
  확인해 같은 설치 path에 복원하고, smoke·listener·자동시작·방화벽을 재확인한다.
- SOL-0017 시험 체크포인트를 되돌리려면
  `git revert 57aa2f108d65e69a7645f75b623fed1274bdb661` 후 Kiosk unit, lint,
  AndroidTest assemble·focused 두 건과 전체 계측을 다시 실행한다. 이 commit은 제품
  source·version·A 설치본을 바꾸지 않았으므로 기기 rollback은 필요 없다.
- SOL-0018을 되돌리려면
  `git revert 6cdbd953f052f2dcb9ce61cd782dd13f1752f3b0` 후 Kiosk unit, lint,
  AndroidTest assemble·CSV/빠른 반 focused·전체 계측과 공식 release를 다시
  실행한다. A는 code 94이므로 되돌린 source에서 같은 signer·code 95 이상의
  forward rollback release를 만들어 `adb install -r`로 설치하며 APK 삭제·data
  clear·downgrade를 하지 않는다.

## 2026-08-13 RC89 학생 CSV intake·수업 시작 공통 gate

- `SOL-0018` — Kiosk 관리자 데이터/session 동시성, P3, 신뢰도 높음, 상태
  `자동검증 완료`.
- 사용자 영향·재현 조건: 지정 PC의 CSV 요청이 느리거나 preview가 열린 동안 기존
  구현은 import 버튼만 잠갔다. 사용자가 수업 시작 또는 다른 학생·반 작업을 누르면
  같은 관리자 의도 수명 안에서 작업이 겹칠 수 있었다. repository의 활성 session
  guard는 잘못된 CSV DB 적용을 제한하지만, 느린 요청이 끝난 뒤 오래된 preview가
  나타나거나 서로 다른 작업 안내·제어가 교차하는 UI/운영 혼선을 막지는 못했다.
- 기대 결과는 fetch 시작부터 취소·실패·적용 완료까지 공통 관리자 데이터 operation
  하나가 활성이고 수업 시작·Web recovery 및 다른 학생·반 작업이 제출되지 않는
  것이다. 수정 전 실제 결과는 PC 연결을 수락해 응답을 보류한 동안
  `adminDataOperationGate.isActive=false`였고 신규 계측이
  `CSV fetch did not hold the common admin operation gate`로 1/1 실패했다.
- 정적 근거: 수정 전 `fetchStudentCsvFromPc()`는 session·pairing만 확인하고
  `pcControlExecutor`에 바로 제출했으며 import 버튼의 별도 boolean만 바꿨다.
  `applyStudentCsv()`가 preview 확인 뒤에야 공통 gate를 잡아 network·parse·preview가
  보호되지 않았다. 이번 수정은 fetch 직전에 `beginAdminDataOperation()`을 호출하고
  no-download, network/parse/preview/submit/apply failure, preview dismiss와 성공 후
  refresh까지 모든 terminal branch에서 정확히 해제한다. 취소·실패 시 parsed
  credential owner도 기존 계약대로 지운다.
- 동적 근거: loopback server가 TCP 연결만 수락한 뒤 latch로 응답을 지연했다. 합성
  pairing을 Activity에 적용해 fetch를 시작한 뒤 gate active와 학생 등록·반 생성·
  수업 시작 disabled를 확인했다. 수업 시작 함수를 직접 호출해 Web recovery gate가
  inactive로 남고 `다른 학생·반 작업이 끝날 때까지 기다리세요.`가 표시됨을
  확인했다. server 해제 뒤 gate가 끝나고 세 제어가 다시 enabled가 됐다. 실제 학생
  CSV·QR·운영 pairing secret은 사용하지 않았다.
- 반대 근거·심각도: 단일 PC executor와 repository transaction/active-session guard는
  실제 DB corruption 가능성을 낮춘다. 명시적 사용자 조작과 느린 PC가 함께 필요하고
  수정 전 데이터 손상은 재현하지 않았으므로 P2가 아닌 P3로 판정했다. 과거
  `LUNA-0035/0036`의 공통 gate 교정이 CSV fetch 단계에는 부분 잔존했던 것으로
  정정한다.
- 자동검증:
  - 수정 전 focused 1/1 failure, 수정 후 신규 focused 1/1 PASS.
  - 첫 전체 78개에서 신규 CSV case는 7.087초 PASS였으나 기존 빠른 반 geometry
    시험이 spinner adapter 준비 전 진행돼 1건 실패했다. 격리 1/1 PASS로 timing
    race를 확인하고 readiness를 adapter 12개·`월1` enabled까지 강화했다.
  - CSV gate와 빠른 반 focused 2/2는 41초, 최종 API 33 전체는 78/78,
    failure/error/skip 0, XML 91.854초, Gradle 1분 49초 PASS다.
  - 최종 unit/lint/debug·AndroidTest assemble은 84 tasks·32초 PASS, JVM 99/99,
    failure/error/skip 0·0.912초다. 세 release script parser 오류 0건이다.
  - 공식 release는 158 tasks·2분 16초 PASS했다. RC89 artifact는 36,754,057 bytes,
    SHA-256 `B67B2BCF8D81FCF7D770627F62B14930D94DA4CA519390F3FC59945D25D2B93F`,
    versionName/code `0.6.0-rc89`/94, v2 signer SHA-256
    `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`다.
    checksum·명시적 artifact verify도 통과했고 Web RC137 payload는 동일하다.
- A/PC 현장 상태:
  - 승인 A 한 대의 RC88/code 93 설치본 byte·hash·signer와 UID 10288, first install,
    Device Owner·HOME·Lock Task·QR 대기 안전 상태를 먼저 확인했다. 같은 signer
    RC89/code 94를 `adb install -r`로 설치했고 UID·first install·data·관리 정책을
    보존했다. 설치 APK는 RC89 artifact와 byte·SHA-256이 정확히 일치한다.
  - 재시작 `RECOVERY_REQUIRED`를 지정 PIN 도구와 정확한 데이터 보존 확인창으로
    안전 종료했다. `ADMIN_IDLE`, 선택 반 인원 4명·신규용 카드 무료 4장·관리자 제어
    복구를 확인한 뒤 Web 사전점검으로 전면 QR 대기를 복원했다. 실제 화면은 잘림·
    겹침·오류가 없고 최종 Kiosk top/실행 중, Lock Task `LOCKED`, 원격 지원
    `INACTIVE`, test package·ADB tunnel·임시 파일·crash/ANR 0건이다.
  - 운영 PC에 대기 CSV가 없고 실제 학생정보 변경을 피하기 위해 정확한 A/운영 PC
    CSV 수신은 수행하지 않았다. 따라서 A 결과를 해당 지연 CSV 분기 현장 통과로
    확대하지 않는다.
- 변경 파일: `MainActivity.kt`, `MainActivityInstrumentedTest.kt`, Kiosk version과
  세 release script. 구현·원격 복구점은
  `6cdbd953f052f2dcb9ce61cd782dd13f1752f3b0`, 전용 origin branch push 성공.
- source rollback은 `git revert 6cdbd953f052f2dcb9ce61cd782dd13f1752f3b0` 후 위
  focused·unit·lint·assemble·전체 계측과 공식 release를 재실행한다. A는 code 94라
  downgrade·삭제·data clear를 하지 않고, 되돌린 source에서 같은 signer·code 95+
  forward rollback을 만들어 `adb install -r`한다.

## 2026-08-13 Kiosk 수업 종료 idle projection 독립 재검증

- `SOL-0017` — Kiosk session/UI, P4, 신뢰도 높음, 상태 `이미 수정됨`.
- 사용자 영향 후보는 종료 transaction이 성공했는데 후속 관리자 snapshot 읽기만
  실패할 때 이전 active session·종료/재개 control·scanner가 남아 DB idle과 화면이
  어긋나는 것이었다. 과거 `LUNA-0026`은 2026-08-02 source를 근거로 한 후보였다.
- 현재 정적 근거: `StudentRepository.endSession()`은 transaction에서 temporary row와
  active session을 정리하고 `ADMIN_IDLE` entity를 반환한다. `MainActivity`는
  `d5589373fe6951451839c43ac75578ac22472b5b7`부터 그 반환값을
  `currentSession`에 저장하고 session·roster control을 idle로 먼저 갱신한 뒤
  `refreshAdminData()`를 호출한다. 따라서 후속 read 실패 branch가 stale active
  session을 다시 그릴 근거가 없다.
- 동적 근거: 합성 반·학생 session을 시작하고 `completeSessionEnd()`를 호출한 뒤
  별도 watcher가 DB의 `sessionId=null` commit을 확인했다. UI callback 전에 repository
  접근만 fault-injection으로 실패시켜 snapshot 오류 안내를 만들었고, DB와 Activity
  sessionId null, 시작 문구, resume/recover/scanner `GONE`을 함께 확인했다.
- 신규 focused 1/1, 이후 관련 두 시험 focused 2/2를 통과했다. unit 99/99,
  debug lint, debug·AndroidTest assemble도 성공했다. 첫 전체 계측은 기존
  무응답-PC 시험이 adapter 초기화 전 null을 역참조해 1/77 실패했으나 해당 시험
  격리 실행은 변경 전 1/1 통과해 시험 race임을 확인했다. polling을 null-safe하게
  만든 뒤 최종 API 33 전체는 77/77, XML 83.474초, Gradle 1분 41초 PASS다.
- 반대 근거·제약: 실제 A에서 session 종료 직후 DB snapshot read fault를 강제하지
  않았다. 다만 동일 Activity·Room·UI 경로의 API 33 fault-injection이 정확한 순서를
  검증했다. 제품 source·APK·version은 바꾸지 않았고 A 재설치는 하지 않았다. A는
  읽기 전용 점검에서 RC88/code 93, UID·설치 시각, top resumed Kiosk와 Lock Task
  `LOCKED`를 유지했으며 시험 AVD는 종료했다.
- 시험 commit·원격 복구점:
  `57aa2f108d65e69a7645f75b623fed1274bdb661`, 전용 origin branch push 성공.

## 2026-08-13 PC 수신기 0.1.8 active handler 종료 barrier·운영 설치

- SOL-0004는 connection 32개·frame 30초·event queue 상한을 교정했지만 daemon
  request handler를 종료 시 개별 회수하지 않는 경계를 bounded process 종료로
  남겼다. 현재 `server_close()`는 listener만 닫고 active socket을 추적하지 않아
  tray 종료나 Tk mainloop 반환 뒤에도 partial client가 연결된 상태였다.
- 수정 전 loopback client가 1 byte만 보낸 상태에서 `shutdown()`과
  `server_close()`를 호출한 focused test는 0.78초에 실패했다. close 반환 뒤
  `recv()`가 250ms timeout이어서 handler/client socket 잔존을 직접 확인했다.
- 0.1.8은 admission 직후 socket을 condition 아래 추적하고 handler `finally`에서
  회수한다. listener를 닫은 뒤 모든 active socket을 shutdown/close하고 최대 2초
  drain한다. handler가 회수된 뒤에만 pending CSV memory를 닫고, 이미 state 작업
  내부에서 응답하지 않는 handler는 bounded fallback 뒤 process 종료를 계속한다.
- 자동·패키지 증거:
  - partial close·slot 회수, state operation 50ms fallback, state cleanup 순서·
    non-drain 보존 focused 4/4는 1.40초, partial 단건은 0.09초 PASS.
  - source 전체 pytest 27/27, 1.98초, compileall 오류 0. 공식 build environment
    pytest 27/27, 2.59초, PyInstaller 6.15.0 package와 독립 인증·저장·ACK·정리
    smoke PASS.
  - artifact 23,187,199 bytes, SHA-256
    `55BF10A5AB6E41B94A18478D38CFD4C9F1B4ACDCA0609667C8B9ED857C92D3A0`.
- 운영 PC:
  - 설치 전 0.1.7은 보관 artifact와 23,185,198 bytes·SHA-256
    `E88C7DBB3443F53EBB47DFB44B25D91F868DEA72B62C7BC9031D83915813CEF2`로
    일치했고 background parent/child 2개, listener 1개, established 0개였다.
  - exact 설치 EXE를 위 timestamped backup에 복사·hash 확인한 뒤 exact process만
    종료하고 0.1.8을 같은 path에 교체했다. 설치본 hash=artifact hash, 독립 smoke
    exit 0, background parent/child 2개, listener 1개, established 0개를 두 번
    확인했다.
  - Startup target/`--background`, 기존 `Private`/TCP 48129 단일 방화벽 rule,
    DPAPI config 818 bytes·last write `2026-08-13 02:23:48`, pending CSV store 부재를
    보존했다. 현재 비관리자 세션이라 이미 정확한 방화벽 rule을 재작성하지 않았다.
  - Computer Use로 실제 tray Quit를 시도할 targetable receiver/Explorer window가
    반환되지 않아 좌표를 추측하지 않았다. 실제 수신 PDF·페어링·방화벽·수신 폴더,
    전송 중 tray Quit·Windows 로그오프·강제 process kill은 변경·수행하지 않았다.
- 구현·복구점:
  `850b6f31ad272c71175cb724d6d4058b9d295752`, 전용 origin branch push 성공.

## 2026-08-13 RC88 공유 QR PDF 재시작 만료 복구·A 설치

- RC15의 공유 시작 1시간·정상 복귀 30초 삭제 예약은 process가 종료되면 사라졌다.
  다음 시작 정리는 이미 만료된 파일만 지웠고 아직 젊은 orphan의 남은 수명을
  재예약하지 않아, 한 번 재시작한 뒤 살아 있는 process에서도 다음 export·재시작
  전까지 로그인 가능한 QR PDF가 cache에 남을 수 있었다.
- 시작 정리는 만료 파일을 즉시 삭제하고 젊은 파일은 마지막 수정 시각 기준 남은
  시간만 process Handler에 다시 예약한다. 전용 `QrPdfFileProvider`가 같은 정리를
  `onCreate()`에서 마친 뒤 기존 FileProvider 동작을 제공하므로 Activity 없이 URI
  요청으로 재기동돼도 만료 파일을 먼저 삭제한다. 기존 non-exported authority,
  일시 read grant와 `qr_exports/` 범위는 유지했다.
- 수정 전·후 자동 증거:
  - 만료까지 약 1.5초 남은 합성 비-QR orphan은 수정 전 5.033초 뒤에도 남아 1/1
    failure, 수정 후 `Time: 1.134`, `OK (1 test)`였다.
  - provider manifest 계약은 수정 전 AndroidX 기본 provider라 0.027초 1/1 failure,
    전용 provider 뒤 0.018초 `OK (1 test)`였다. 실제 manifest `ProviderInfo`로 provider를
    초기화한 만료 선행 삭제도 0.032초 `OK (1 test)`였다.
  - Kiosk unit·debug lint·debug/AndroidTest assemble 84 tasks PASS. API 33 전체
    instrumentation `Time: 77.511`, `OK (76 tests)`, 실패·skip 0.
  - 공식 release 158 tasks, `BUILD SUCCESSFUL in 2m 5s`; Kiosk JVM 99개·Web JVM
    67개, release lint, signed assemble, version·non-debuggable·동일 signer PASS.
- RC88 artifact:
  `artifacts/matholic-kiosk-0.6.0-rc88-release.apk`, 36,754,057 bytes, SHA-256
  `00EE705C788720D80BF90203F8F02EAEF1D69143F9C3816683344C4F5BE85C28`,
  signer SHA-256
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.
- A 실기:
  - 승인 A 한 대의 RC87 설치 APK와 artifact byte·SHA-256·signer 일치를 먼저
    확인하고 같은 signer RC88/code 93을 `adb install -r`로 보존 설치했다. UID 10288,
    firstInstallTime, Device Owner, preferred HOME·data·Lock Task를 유지했다.
  - A에서 다시 읽은 설치 APK는 RC88 artifact와 byte·SHA-256·v2 signer가 정확히
    같고 새 provider가 package에 등록됐다. 변경 없는 Web RC137은 재설치하지 않았다.
  - 원격을 중지한 exact PIN 화면에서 지정 DPAPI 도구만 사용했다. 확인창의 현재
    수업·보강 명단만 종료하고 학생·반·QR은 삭제하지 않는 범위를 검증해
    `ADMIN_IDLE`로 안전 복구했다. 카드 메뉴 `전체 4장 · 무료 4장 · 사용 중 0장`,
    exact Web 사전점검과 전면 카메라 QR 대기를 확인했다. 카드·계정·QR은 바꾸지
    않았다.
  - 첫 preinstall signer verifier는 실제 `V2 Signer` 라벨 대신 `Signer #1`만
    기대해 APK pull·hash 뒤 실패했다. 임시 경로를 정리하고 공개 인증서 출력에 맞게
    parser를 보정해 재검증했다. recovery 버튼 탐색도 `visible-to-user=true`가 반드시
    출력된다고 가정해 첫 네 번 scroll 뒤 실패했지만 아무 버튼도 누르지 않았고,
    resource-id·bounds로 실제 버튼을 찾아 exact 확인 뒤 진행했다. 예상 대화상자 제목
    한 건도 소스와 대조해 고친 뒤에만 안전 복구를 눌렀다. 제품 실패가 아니다.
  - 실제 QR 공유·process kill·1시간 대기·외부 앱 URI 재접근은 하지 않았다. 최종
    Kiosk top resumed, Lock Task `LOCKED`, 원격 `INACTIVE`, ADB forward/reverse 0,
    crash/ANR buffer 일치 0, 검증 임시 파일 0이다.
- 구현·복구점:
  `a5905540cd9a74d589bb78fee7ea247bb35a56ac`, 전용 origin branch push 성공.

## 2026-08-13 RC87 지정 PC PDF write timeout·A 설치

- `Socket.soTimeout`은 ACK read만 제한해, TCP 연결 뒤 PDF를 읽지 않는 수신기에는
  최대 5MiB 암호화 frame의 write가 무기한 대기할 수 있었다. 단일
  `pcControlExecutor`가 점유돼 이후 명시적 PC 작업도 진행되지 않았다.
- blocking `SocketChannel` write를 짧은 daemon writer에서 실행하고 호출자가 기존
  read timeout과 같은 기본 10초 절대 제한만 기다리게 했다. timeout·interrupt는
  channel을 닫아 write를 해제하고, 정상 write는 channel을 보존해 기존 인증 ACK를
  그대로 읽는다. 암호화·상한·endpoint 복구·ACK·secret 정리는 바꾸지 않았다.
- 수정 전 최대 payload loopback JVM 시험은 2초 안에 끝나지 않아 실패했다. 첫
  nonblocking selector 구현은 JVM/Kiosk를 통과했으나 API 33 전체 72개 중
  quick-class·SOL-0013 startup 2개 timeout과 executor thread 96~100% CPU를 보여
  commit하지 않고 폐기했다. blocking writer 첫 보정의 정상 ACK socket close race도
  `SocketException` 회귀로 잡아 정상 완료와 timeout close를 분리했다.
- 최종 자동·릴리스 검증:
  - JVM sender 정상 ACK·stall 2/2, 0.579초; stall 0.552초, 정상 0.027초.
  - Android 최대 5MiB stall focused 1/1, SOL-0013 startup focused 1/1.
  - Kiosk unit·debug lint·debug/AndroidTest assemble 84 tasks PASS.
  - API 33 전체 instrumentation 73/73, 실패·skip 0,
    `BUILD SUCCESSFUL in 1m 55s`.
  - 공식 release 158 tasks, `BUILD SUCCESSFUL in 2m 4s`; Kiosk/Web JVM,
    release lint, signed assemble, version·non-debuggable·동일 signer 검증 PASS.
- RC87 artifact:
  `artifacts/matholic-kiosk-0.6.0-rc87-release.apk`, 36,754,045 bytes,
  SHA-256
  `DE13ECB75EA784328D4954B81375387A1F415DA9BC428A6AB4D3984BB74A1724`,
  signer SHA-256
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.
- A 실기:
  - RC86/code 91 설치본과 보관 artifact의 byte·SHA-256·signer 일치를 먼저
    확인하고 RC87/code 92를 `adb install -r`로 보존 설치했다. UID 10288,
    firstInstallTime, Device Owner, preferred HOME·data·Lock Task를 유지했다.
  - A에서 다시 읽은 설치 APK는 RC87 artifact와 byte·SHA-256·v2 signer가 정확히
    일치했다. 변경 없는 Web RC137은 재설치하지 않았다.
  - 보안 PIN 화면에서 remote capture가 거부돼 즉시 지원을 중지하고 UI hierarchy와
    지정 DPAPI 도구만 사용했다. `RECOVERY_REQUIRED`를 학생·반·QR을 삭제하지 않는
    원버튼으로 `ADMIN_IDLE`에 복구했고 카드 메뉴 `전체 4장 · 무료 4장 · 사용 중
    0장`을 확인했다. 카드·계정·QR은 변경하지 않았다.
  - 첫 수업 시작 판별은 명시적 `웹 검사 후 시작` 확인창에서 버튼을 누르지 않고
    45초 대기해 timeout이었다. 제품 실패가 아닌 절차 누락임을 실제 화면으로
    확인하고 exact 확인 버튼을 누른 뒤 15초 안에 전면 카메라 QR 대기로 갔다.
  - 최종 Kiosk top resumed, Lock Task `LOCKED`, 원격 지원 `INACTIVE`, ADB
    forward/reverse 없음, Kiosk crash buffer 일치 항목 없음이다.
- 운영 PC를 일부러 partial stall시키거나 신규 QR을 만들지 않았다. 정확한
  SOL-0014 분기는 JVM·Android loopback 자동검증이며 A에서는 설치·인증·복구·메뉴·
  Web 사전점검·QR 대기 통합 회귀까지만 확인했다.
- 구현·복구점:
  `51d1b03c7694f685e91421c0c8953fc9b8aae329`, 전용 origin branch push 성공.

## 2026-08-13 RC86 시작 PDF 전송 격리·A 설치

- 신규 카드 bootstrap은 지정 PC가 TCP 연결만 받고 ACK를 주지 않으면 기존
  `ioExecutor`를 timeout 동안 점유해 관리자 인증과 관리자 데이터 준비를 함께
  지연시켰다. 핵심 DB·PIN·세션·카드 준비가 성공하면 인증 UI를 먼저 표시하고,
  PDF 전달만 별도 `pcControlExecutor`에 제출하도록 최소 수정했다.
- 무응답 loopback PC 회귀시험은 수정 전 `Time: 6.048`, 1 failure로 인증 UI
  2초 제한을 넘겼다. 수정 후에는 연결을 계속 보류한 채 인증 UI 2초, PIN 뒤
  관리자 패널·반 데이터 3초 제한을 통과했고 최종 focused는 `Time: 7.447`,
  `OK (1 test)`였다.
- 최종 자동·릴리스 검증:
  - Kiosk unit·debug lint·debug/AndroidTest assemble 84 tasks PASS.
  - API 33 전용 AVD 전체 instrumentation:
    `Time: 90.795`, `OK (72 tests)`.
  - 공식 release: 158 tasks, `BUILD SUCCESSFUL in 2m 15s`; Kiosk/Web JVM,
    release lint, signed assemble, version·non-debuggable·동일 signer 검증 PASS.
- RC86 artifact:
  `artifacts/matholic-kiosk-0.6.0-rc86-release.apk`, 36,754,045 bytes,
  SHA-256
  `0979D4A9688AE60DDF95A40238455A16D63C1752356425043574159A90BBAAC1`,
  signer SHA-256
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.
- A 실기:
  - RC85/code 90에서 RC86/code 91로 `adb install -r` 성공. UID 10288,
    firstInstallTime, Device Owner, preferred HOME과 앱 데이터가 유지됐다.
  - A에서 다시 읽은 Kiosk 설치 APK는 RC86 artifact와 byte count·SHA-256·v2
    signer가 정확히 일치했다. 변경 없는 Web RC137 설치 APK도 현재 artifact와
    byte count·SHA-256·signer가 일치했다.
  - masked PIN·`RECOVERY_REQUIRED`를 확인하고 지정 DPAPI 도구로 인증했다.
    학생·반·QR을 삭제하지 않는 원버튼 안전 복구 뒤 `ADMIN_IDLE`, 신규용 카드
    `무료 4장`을 확인했고 카드·계정·QR은 바꾸지 않았다.
  - Web 사전점검 뒤 실제 전면 카메라 QR 대기를 확인했다. 첫 자동 화면 판별은
    scanner에서 관리자 `status_text`가 보이지 않아 45초 뒤 실패했지만 즉시 실제
    캡처와 UI 계층으로 정상 QR 안내임을 확인했다. 첫 recovery bounds helper도
    문자열 좌표 계산으로 화면 밖을 눌러 상태 변화가 없었고, 정수 좌표와 label을
    재확인한 뒤 성공했다.
  - 최종 Kiosk top resumed, Lock Task `LOCKED`, 원격 지원 `INACTIVE`, ADB
    forward/reverse 없음, Kiosk crash buffer 일치 항목 없음이다.
- A에는 신규 준비 대상 카드가 없어 운영 QR을 폐기하거나 운영 PC를 고의로
  stall시키지 않았다. 정확한 SOL-0013 분기는 loopback 계측으로 확인했고 A에서는
  설치·인증·관리자·복구·Web 사전점검·QR 대기 통합 회귀까지만 확인했다.
- 구현·복구점:
  `b8ce2d394e5f02655a4bbe70842d4dde7bde1466`, 전용 origin branch push 성공.

## 2026-08-13 RC85 비활성 신규용 카드 슬롯 복구·A 설치

- RC84 시작 준비는 재사용 슬롯 행이 하나도 없을 때만 4장을 만들었다. 과거
  비활성 슬롯이 남아 활성 수가 3장인 DB에서는 목표 4장으로 돌아오지 않는
  결함을 회귀시험으로 재현했다.
- RC85는 활성 슬롯 부족분만 계산하고 비활성 슬롯을 먼저 같은 student ID·라벨,
  새 QR·암호화된 빈 자격정보, 무료 상태로 복구한다. 이전 반 소속을 지우고 새
  PDF가 필요함을 표시하며, 그래도 부족하면 충돌 없는 새 슬롯을 만든다.
- 자동·릴리스 검증:
  - 수정 전 focused 계측: 기대 1장, 실제 0장으로 실패.
  - 수정 후 focused 계측: `OK (1 test)`.
  - Kiosk unit·debug lint·debug/AndroidTest assemble 84 tasks PASS.
  - API 33 전용 AVD 최종 전체 instrumentation:
    `Time: 86.062`, `OK (71 tests)`.
  - `scripts/build-release.ps1`: 158 tasks, `BUILD SUCCESSFUL`; Kiosk/Web JVM,
    release lint, signed assemble, version·non-debuggable·동일 signer 검증 PASS.
- RC85 artifact:
  `artifacts/matholic-kiosk-0.6.0-rc85-release.apk`, 36,754,045 bytes,
  SHA-256
  `5E8F629715E04A3A6A6C56897D55BE5AD75B94DF5EA1E3B47663E7F8560397F7`,
  signer SHA-256
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.
- A 실기:
  - RC84/code 89에서 RC85/code 90으로 `adb install -r` 성공. UID 10288,
    firstInstallTime, Device Owner, preferred HOME과 앱 데이터가 유지됐다.
  - A에서 다시 읽은 설치 APK와 artifact의 byte count·SHA-256·v2 signer가
    정확히 일치했다.
  - `RECOVERY_REQUIRED`를 원버튼 안전 복구한 뒤 신규용 카드 메뉴의
    `전체 4장 · 무료 4장 · 사용 중 0장`을 확인했다. A에는 비활성 슬롯이 없어
    자동 복구 분기는 고의로 만들지 않았고 실제 계정·카드 상태도 변경하지 않았다.
  - 새 수업을 시작해 전면 카메라 QR 대기, Kiosk top resumed, Lock Task
    `LOCKED`로 복원했다. 원격 지원 `INACTIVE`, ADB forward/reverse 없음, Kiosk
    crash buffer 일치 항목 없음이다.
- 구현·복구점:
  `e7c558da18152795d11427d13d8d61b5323e1640`, 전용 origin branch push 성공.

## 2026-08-12 RC84 신규용 재사용 QR 카드 교정·A 설치

- Room v5에 신규카드1~4 재사용 슬롯을 추가했다. 무료 슬롯은 암호화된 빈
  자격정보로 로그인되지 않으며 일반 학생·반 구성에서 숨긴다. 배정은 QR hash를
  유지하고 이름·ID·PW만 바꾸며 현재 수업 보강에 자동 추가하지 않는다.
- 실제 카드 전환은 새 일반 학생·새 QR을 만들고 기존 반 소속을 옮긴 뒤, 원래
  재사용 슬롯을 같은 QR·빈 자격정보로 초기화한다.
- 시작 때 이미 존재하는 저장 미확인 슬롯의 QR을 자동 교체하지 않는다. 재사용
  카드는 개별·선택·반 전체 일반 재발급에서 거부하고, 이름·CSV 변경도 카드 PDF
  필요 상태나 QR hash를 바꾸지 않는다.
- 관리자 `신규용 카드 관리` AlertDialog에서 목록 항목이 실제 표시되도록
  message와 item 구성을 분리했다. 실제 카드 전환은 사용 중 카드를 명시적으로
  선택하게 했다. 저장 미확인 슬롯의 수동 교체는 같은 QR 재전송이 아니며 기존
  QR·이전 파일·인쇄물이 무효가 됨을 확인한 뒤에만 실행한다.
- 자동검증:
  - `:kiosk:testDebugUnitTest :kiosk:lintDebug :kiosk:assembleDebug
    :kiosk:assembleDebugAndroidTest`: 84 tasks, `BUILD SUCCESSFUL`.
  - API 33 전용 AVD 최종 소스 전체 instrumentation:
    `Time: 89.815`, `OK (70 tests)`.
  - `scripts/build-release.ps1`: 158 tasks, Kiosk/Web JVM 시험, release lint,
    signed assemble, version·non-debuggable·동일 signer 이중 검증 PASS.
  - PowerShell parser: `build-release.ps1`, `verify-release-apks.ps1`,
    `provision-release-device-owner.ps1` 오류 0건. `git diff --check` PASS.
- RC84 release artifact:
  `artifacts/matholic-kiosk-0.6.0-rc84-release.apk`, 36,754,045 bytes,
  SHA-256
  `F703EE8BD349A89E4A781025C22E6311F0999D844AF58036A27410A3440C7D98`,
  signer SHA-256
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.
- A 실기:
  - RC83/code 88에서 RC84/code 89로 `adb install -r` 성공. UID 10288,
    firstInstallTime, Device Owner, preferred HOME과 앱 데이터가 유지됐다.
  - A에서 읽은 설치 APK와 artifact의 byte count·SHA-256이 정확히 일치했다.
  - 설치 후 안전 복구, 신규용 카드 관리 메뉴 목록, 무료 4장 표시를 확인했다.
    실제 계정정보 입력·배정 없이 닫고 새 수업을 시작해 QR 대기·Lock Task
    `LOCKED`로 복원했다.
  - 첫 원격 Start/Capture는 관리자 PIN 보안 화면의 screenshot 차단 때문에
    실패했다. 지원 상태를 즉시 Stop한 뒤 UI XML에서 PIN 화면임만 확인하고
    허용된 DPAPI PIN 입력 도구를 사용했다. 최종 원격 지원은 `INACTIVE`다.
- 구현·복구점: `59c9181144ba8758a6710ff793dbcdac7a24a57b`, 전용 origin branch
  push 성공. Room v5 설치 뒤 기기 rollback은 위 현재 상태의 forward rollback
  주의사항을 따른다.

## 2026-08-11 이전 상태 스냅샷

- `last_updated`: 2026-08-11 10:19:25 +09:00
- 현재 branch: `codex/sol-continuous-development-20260804`
- 보고서 갱신 직전 branch tip / upstream:
  `b9b91dd05e68105073d9aa7bfb92a33ff8d9ed9c` /
  `b9b91dd05e68105073d9aa7bfb92a33ff8d9ed9c`.
- 보고서 상태기록 직전 branch와 upstream은 behind 0, ahead 0이다.
- 마지막 push 성공 commit은
  `b9b91dd05e68105073d9aa7bfb92a33ff8d9ed9c`이다. 이 상태기록 문서 commit은
  위 스냅샷 다음에 생성·push하므로 최종 원격 tip은 Git tracking 상태를 따른다.
- 최초 보존 기준선: `master`의
  `ccf410d6b9758c7594a07e94e459bf7e83c554bc`; 당시 `origin/master`보다
  24 commits ahead
- 현재 보존 대상: Goal 시작 전부터 있던 미추적 `outputs/`. 수정·stage·삭제하지
  않는다.
- 실제 A 마지막 독립 확인: 2026-08-11 10:18 +09:00. 승인 ADB device는
  serial `R54TB029FHZ`, model `SM-P610` 한 대뿐이다.
  - Kiosk `0.6.0-rc79`/code 84, UID 10288, first install
    `2026-07-24 12:52:28`, last update `2026-08-11 10:02:13`.
  - Web POC `0.4.0-rc137`/code 154, UID 10293, first install
    `2026-07-28 13:12:16`, last update `2026-08-11 10:02:09`.
  - 설치본과 release artifact signer SHA-256은
    `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`로
    일치한다. 보존형 `adb install -r` 뒤 UID와 first install이 유지됐다.
  - Device Owner와 preferred HOME은 각각
    `com.local.matholickiosk.kiosk/.admin.KioskDeviceAdminReceiver`,
    `com.local.matholickiosk.kiosk/.MainActivity`로 유지됐다.
  - Kiosk가 top resumed이고 Lock Task `LOCKED`; 현재 토2 수업의 전면 카메라
    QR 대기 화면이다.
  - 반 전환·보충 추가 성공 안내의 유지·자동 소멸과 RC137 종료 경고를 실물
    확인했다. 시험용 임시 학생은 정상 로그아웃 뒤 반 전환으로 정리됐다.
- 실제 운영 PC 마지막 독립 확인: 2026-08-09 11:20 +09:00. 설치 실행 파일은 보관
  0.1.7 artifact와 같은 23,185,198 bytes·SHA-256
  `E88C7DBB3443F53EBB47DFB44B25D91F868DEA72B62C7BC9031D83915813CEF2`이다.
  정식 설치 path의 PyInstaller process 2개와 `0.0.0.0:48129` listener 1개를
  재확인했다. 기존 Startup·방화벽·부하·네트워크 복구 검증은 아래 SOL-0008
  기록을 따른다.
  32×5MiB 동시 전송 32/32 내용 검증, 32개 partial connection과 33번째 즉시
  거부, 실제 Wi-Fi disconnect/reconnect, DHCP release/renew, 실제 Wi-Fi와 가상
  interface 두 주소의 listener 도달까지 통과했다.
  Windows를 실제 재부팅한 뒤 로그인 Startup 자동실행도 통과했다. 새 boot time,
  process 2개, listener 1개, hash·Startup target/argument·listener owner 일치,
  smoke exit 0과 현재 Wi-Fi Internet 연결을 확인했다.
- 현재 작업 완료: Kiosk RC79/Web RC137과 PC 수신기 0.1.7 구현·자동·릴리스
  검증, 운영 설치와 A 실물 확인을 완료했다.
- 다음 우선 큐: 새 재현 이슈나 운영 피드백이 생기기 전에는 필수 교정 없음.

### 열린 finding과 제약

- 열린 P0/P1: 없음. 과거 보고서의 후보는 현재 source와 독립 재검증 전에는
  열린 결함으로 승격하지 않는다.
- 현장검증 완료 P2: `SOL-0003`, `SOL-0005`, `SOL-0006`, `SOL-0008` 4건.
- 열린 P2와 자동검증에만 머문 P2는 없다.
- 완료 P3: `SOL-0001` 1건.
- 운영 P3 교정 완료: 관리자 권한으로 Matholic 수신기 광범위 방화벽 규칙
  6개를 삭제하고 설치본 `Private`/TCP 48129 단일 규칙을 독립 확인했다.
- 자동검증 완료 P4: `SOL-0007` 1건.
- 기각: `SOL-0002` 1건.
- 이미 수정됨: `SOL-0004` 1건.
- 무입력 자동 로그아웃은 별도 Windows 운영 알림 없이 QR로 복귀하는 현재
  정책으로 확정해 열린 finding으로 세지 않는다.
- 현재 제약:
  - Kiosk `MainActivityInstrumentedTest`는 기본 AVD, 2000×1200/240dpi·font
    scale 1.3, 같은 해상도·font scale 0.85·180도 반전에서 각각 16/16, 총
    48/48 PASS다. 지원 A와 임의의 모든 Android 화면 호환성으로 확대하지 않는다.
  - 절단용 PDF의 실제 흑백 출력·절단·코팅 전 카메라 인식은 사용자가 통과를
    확인했다. 가능한 모든 조명·거리·반사 조건으로 확대하지 않는다.
  - PC 수신기 0.1.7은 0.1.6의 실제 Wi-Fi 단절·재연결, DHCP 주소 반납·재할당,
    실제 Wi-Fi+가상 interface 두 주소의 listener 도달과 Windows 재부팅 뒤 Startup
    자동실행을 통과했다. 활성 VPN adapter가 없어 VPN 경유 주소 선택은 수행하지
    않았다. 현재 운영 설치본은 검증 산출물과 같은 0.1.7이다.
  - A에는 실제 외장 키보드가 없고 접근성 서비스가 활성화돼 있지 않아 이 두
    입력원은 직접 검증하지 않았다. 기본 Samsung IME·Matholic 키패드·ADB text와
    key event의 네트워크 pause 차단은 실제 A에서 통과했다.
  - 설치 APK signer 대조용으로 만든 로컬 임시 디렉터리
    `%LOCALAPPDATA%\Temp\MatholicSolSignerCheck-019fcc38`의 삭제 명령이 실행
    정책에 의해 거부됐다. 내부에는 A에서 읽기 전용으로 가져온 현재 설치 APK
    두 개만 있으며 저장소 밖이다. 정책을 우회해 삭제하지 않았다.

## 2026-08-11 RC79/RC137 A 배포·실기 완료

- 승인 A 한 대에 Web RC137 뒤 Kiosk RC79를 `adb install -r`로 설치했다.
  Kiosk/Web UID 10288/10293과 각 firstInstallTime, Kiosk DB, Device Owner,
  preferred HOME과 Lock Task allowlist를 보존했다.
- A에서 다시 읽은 설치 APK는 각 보관 artifact와 SHA-256이 정확히 일치했고
  release signer도 동일했다. 최종 `verify-gate5-device-owner.ps1`은 RC79/code
  84, Device Owner와 Lock Task `LOCKED`를 통과했다.
- 실제 PIN 메뉴에서 반 전환과 임시 보충 성공 안내가 수동 QR 안내에 덮이지
  않고 유지된 뒤 자동 소멸함을 확인했다. `테스트` 계정으로 Web RC137에 들어가
  강화된 채점 종료 경고와 문제 화면 복귀를 확인했다.
- 시험 로그인은 정상 종료했고 임시 보충은 반 왕복으로 정리했다. 최종 A는
  `토2` QR 대기, Kiosk top resumed, Lock Task `LOCKED`, 원격 점검 `INACTIVE`다.
  ADB forward/reverse와 두 앱 crash/ANR 이력은 없다.

## 2026-08-09 최종 정밀검수 후 교정

- 확인된 P0/P1/P2는 없다. PC 방화벽 운영 규칙 과다 개방과 보안 의존성 고정,
  QR 성공 안내의 수동 안내 덮어쓰기 가능성을 교정했다.
- 방화벽 스크립트는 정확한 실행 파일의 기존 인바운드 규칙 제거와 단일
  Private/TCP 48129 검증을 포함한다. 최초 비관리자 실행 실패 뒤 사용자 승인
  UAC 관리자 실행으로 광범위 규칙 6개를 삭제했고, 네 실행 파일 경로 전체에
  설치본용 정확 규칙 1개만 남았음을 독립 확인했다.
- PC 수신기 0.1.7은 `cryptography 50.0.0`, `Pillow 12.3.0`, 23 pytest,
  독립 packaged smoke, `pip-audit` 0건을 통과했다. 0.1.6을 백업하고 운영
  설치했으며 artifact·설치본 해시 일치와 `0.0.0.0:48129`를 확인했다.
- Kiosk RC79는 성공 안내 동안 거리·밝기 안내만 억제하고 실제 QR 판정은
  계속 허용한다. 실질 QR 승인·거부가 시작되면 notice를 무효화한다.
- 정식 release 158 tasks와 서명·non-debuggable 검증을 통과했다. A 미연결로
  RC79/RC137 설치와 실물 확인은 수행하지 않았다.
- 전체 Android debug 단위시험 173개, Kiosk·Web debug lint와 계측시험 소스
  컴파일 68 tasks를 추가로 통과했다. 설치 수신기에도 실제 인증 합성 PDF를
  전송해 ACK·내용 일치·시험 파일 정리를 확인했다.
- Kiosk RC79 APK SHA-256:
  `E45CC552105532024A83C8E535E1D73DAAA6273083164FDCEC9E74035A9CB0AD`.
- Web RC137 APK SHA-256:
  `D49BFB81A727CA94D7D94E9D5A5D88C4151EB63F22F742F1C06C59972ABC9651`.
- 구현·복구점: `03e1c2b`, `4b5a7e0`, `4061920`, `dd25a7e`, `221c6ec`.

## 2026-08-09 QR 안내·채점 종료 경고

- Kiosk RC78은 반 변경·보충 학생 추가 성공 안내를 3초 뒤 자동으로 지우며,
  세대·현재 문구 검사를 통해 이전 타이머가 새 안내를 지우지 못하게 한다.
- Web RC137은 `채점 끝내기` 첫 터치에서 답안 입력 버튼이 아님을 제목으로
  경고하고, 문제 화면의 `입력` 버튼 안내와 빨간 `채점 종료·로그아웃` 버튼을
  표시한다. 첫 터치만으로 종료하지 않는 기존 이중 확인 경계는 유지한다.
- 대상 단위시험과 두 앱 계측시험 Kotlin 소스 컴파일, 158개 작업의 전체 릴리스
  빌드·lint·서명·동일 signer 검증을 통과했다. A가 연결되지 않아 설치와 실물
  확인은 수행하지 않았다.
- Kiosk RC78 APK SHA-256:
  `26E62C3BD50AB97868573F55A358657EA41439B9820AB0EA64C46922F0D2399D`.
- Web RC137 APK SHA-256:
  `D49BFB81A727CA94D7D94E9D5A5D88C4151EB63F22F742F1C06C59972ABC9651`.

## 2026-08-06 후속 배포·현장 확인

- 학생 로그인 PC 알림 1회, 실제 채점 결과의 오답 개수·문제번호 PC 알림,
  도움말 같은 `?` 버튼 재터치 닫기, QR 대기 PIN 반 전환과 현재 수업 한정 보충
  학생 추가를 구현하고 각각 커밋·릴리스 빌드했다.
- RC75 실제 A 실기에서 반·보충 선택 대화상자의 목록이 가려지는 문제를 발견해
  RC76에서 교정했다. 이어 보충 후보 조회 문구가 취소 뒤 남는 문제를 RC77에서
  교정했다. Kiosk 94개·Web 67개 단위시험, release lint, signed APK와 동일 signer
  검증을 포함한 158개 Gradle 작업을 통과했다.
- RC77/RC136을 A에 보존 설치했고 UID, firstInstallTime, Kiosk DB, Device Owner,
  전용 HOME과 Lock Task를 유지했다. 11개 전환 대상 반, 13개 보충 후보,
  목2→금1→목2 전환과 임시 보충 명단 정리를 실제 A에서 확인했다.
- 사용자는 실제 운영에서 학생 로그인 Windows 알림 1회와 채점 상세 결과 알림이
  정상임을 확인했다. 이는 사용자 현장 확인이며 Codex의 독립 Windows UI 캡처로
  확대하지 않는다.
- 사용자는 무입력 자동 종료 때 별도 로그아웃 Windows 알림이 없음을 확인했다.
  Web은 이 경로에서 채점 결과를 명시적으로 비우고 공식 로그아웃하며, Kiosk는
  결과 메시지가 있을 때만 완료 알림을 요청한다. 따라서 현행 제품 결정과
  일치하는 의도된 동작이다.
- Kiosk RC77 APK는 36,704,813 bytes, SHA-256
  `F053929FD768C6D3F082DEF5D0C3F8A5110093ACD944AB26FA81F8EA27E551AE`이고,
  Web RC136 APK는 3,393,442 bytes, SHA-256
  `616914ABEE78052B7DE27C5600084080E477A69660DD33E51E12E72BFCE30491`이다.

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
- 상태: 현장검증 완료
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
- 동적 근거:
  - 승인 A에서 정확히 `테스트` 계정만 포함한 일회성 반을 만들고 최신 QR을
    발급·지정 PC 전송한 뒤 공식 PDF 원격 제출 경로로 로그인했다.
  - 답안 현황 `0/25`인 미제출 과제의 빈 2번 주관식에 focus를 둔 상태에서
    A Wi-Fi를 실제로 껐다. 불투명한 인터넷 복구 대기 화면이 나타난 뒤 ADB text,
    key event, 가려진 Matholic 키패드 좌표 tap, Enter와 Delete를 주입했다.
  - Wi-Fi 복구 뒤 3초 안에 같은 문항으로 돌아왔고 주관식 값은 빈 상태, 답안
    현황은 `0/25`였다. 기본 입력기는 Samsung IME였고 pause 전 입력기 활성과
    Matholic 수식 키패드 표시를 확인했다.
- 반대 근거·제약: 실제 외장 keyboard가 연결돼 있지 않고 접근성 service도
  활성화돼 있지 않아 이 두 입력원은 직접 주입하지 않았다. 시험 편의를 위해
  접근성 보안 설정을 변경하지 않았다.
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
- 현장 정리: 답안을 한 글자도 저장·제출하지 않은 채 정상 `채점 끝내기`, Kiosk
  수업 안전 종료를 수행했다. 일회성 반을 삭제하고 전체 반 목록에서 부재를
  확인했으며 관리자 인증 잠금, Lock Task `LOCKED`, 원격 지원 `INACTIVE`로
  복원했다.
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
  - 운영 설치본 0.1.6에 실제 5MiB PDF 32개를 동시에 보냈고 32/32 인증 ACK와
    수신 파일 전체 내용 hash 일치를 확인했다. 총 1.125초, 단일 요청 최대
    0.890초, RSS 59.3→59.6MiB였으며 시험 파일 잔존은 0개다.
  - partial connection 32개가 slot을 점유한 상태에서 33번째 연결이 즉시
    닫혔다. 32개를 닫은 뒤 listener 1개와 설치본 smoke exit 0을 확인했다.
- 반대 근거·제약: Windows 로그오프·종료 순간의 실제 전송과 더 저사양 PC의
  장시간 peak RSS는 수행하지 않았다. daemon request handler를 종료 시 개별
  join하지 않는 설계는 bounded process 종료 경계이며 현재 사용자 영향이나
  데이터 오적용 증거는 없다.
- 판정: 과거 무제한 연결·queue·read 후보는
  `47b76ec1d78b1bf3f96c9bab071762de040e3ac2`에서 이미 교정됐고 현재 source와
  동적 probe에 유지된다. 추가 resource-limit 변경은 근거가 없어 하지 않는다.
- 실행한 검증:
  - `cd pc_receiver; python -m pytest -q`
  - 결과: `17 passed in 1.35s`.
  - `python -` loopback probe로 cumulative deadline, admission semaphore,
    event queue eviction을 검증; 세 항목 모두 PASS.
- 수행하지 않은 검증: Windows 종료 순간의 실제 전송과 실제 A 여러 대의 동시
  PDF 전송은 수행하지 않았다. 운영 PC의 loopback 인증 전송으로 서버·암호화·
  저장·ACK·32 connection limit을 검증한 것이며 복수 물리 A의 무선 부하로
  확대하지 않는다.
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
  - 후속 현장검증에서 기본 1080×1920/420dpi, A 대응 2000×1200/240dpi·font
    scale 1.3, 같은 해상도·font scale 0.85·180도 반전 가로 방향의 세 조건으로
    전체 16개를 각각 재실행해 총 48/48 PASS했다. 각 실행은 물리 A가 아닌
    `emulator-5554`로 고정했고 AVD 설정을 복원한 뒤 종료했다.
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

### SOL-0008 — PC 수신기가 LAN 부재 때 종료되고 주소 변경을 반영하지 못함

- 영역: PC receiver 시작·LAN 복구·페어링 QR
- 심각도: P2
- 신뢰도: 높음
- 상태: 현장검증 완료
- 사용자 영향: Windows 로그인 시 Wi-Fi가 아직 준비되지 않으면 수신기가 종료돼
  운영자가 수동 재시작하기 전까지 A의 PDF·상태·CSV 전송을 받을 수 없다. 이미
  실행 중인 PC 주소만 바뀐 경우 서버는 계속 열려 있고 Kiosk RC46+의 같은 `/24`
  자동 복구가 성공할 수 있지만, 수신기는 이전 주소의 QR을 계속 표시해 최초·복구
  페어링이 stale endpoint를 저장할 수 있고 운영자가 주소 변경을 알 수 없었다.
- 재현 조건:
  - 사설 RFC1918 IPv4 주소를 얻지 못하는 상태에서 자동 시작 수신기를 실행한다.
  - 또는 수신기를 시작한 뒤 기본 경로의 사설 IPv4 주소를 다른 값으로 바꾼다.
- 기대 결과:
  - LAN 주소를 아직 얻지 못해도 TCP 서버와 트레이는 먼저 살아 있고 연결을
    기다린다.
  - 사설 주소가 생기거나 바뀌면 현재 주소의 페어링 QR을 자동 생성·갱신한다.
- 실제 결과(수정 전):
  - `ReceiverApplication._initialize()`가 Tk 창과 TCP 서버를 만들기 전에
    `current_lan_ipv4()`를 호출했다. 주소가 없으면 `RuntimeError`가 `main()`까지
    올라가 시작 실패로 종료됐다.
  - host·주소 문구·QR은 시작할 때 한 번만 계산해 실행 중 주소 변경을 감지하거나
    복구하는 경로가 없었다.
- 정적·동적 근거:
  - 서버는 이미 `0.0.0.0`에 bind하도록 구현돼 있어 NIC 주소 확정은 서버 시작의
    필수조건이 아니라 페어링 QR 생성 조건뿐이었다.
  - Startup shortcut은 설치 실행 파일을 `--background`로 실행하므로 Windows
    로그인과 네트워크 준비 순서가 엇갈리는 조건이 현실적으로 존재한다.
  - 수정 전 LAN 회귀 5개 묶음의 첫 실행은 3 failed·2 passed였고, 실패 3개는
    새 refresh method가 없어 발생한 `AttributeError`였다. 제품 코드 수정 뒤 같은
    대상은 5/5 PASS했고, 초기화 순서와 동일 주소 복구 회귀를 추가한 최종
    `test_app.py`는 6/6 PASS했다.
- 원인·결정:
  - LAN 주소 탐색을 시작 필수조건으로 둔 수명주기 결합이 원인이다.
  - server·tray를 먼저 시작하고 Tk `after(0, ...)`에서 주소를 확인한다. 주소가
    없으면 QR을 비우고 대기 문구를 표시하며 5초 뒤 재시도한다.
  - 주소가 처음 생기거나 outage 뒤 복구되거나 값이 바뀌면 QR을 다시 만든다.
    값이 실제로 바뀐 경우 창 상태와 Windows 알림으로 새 QR을 안내한다.
  - Kiosk RC46 이상은 같은 `/24`에서 기존 secret challenge-response로 주소를
    자동 복구할 수 있다. 그 복구가 성공하면 수동 재페어링은 필요 없고, 새 QR은
    최초 페어링 또는 자동 복구 실패 때 사용할 수 있다.
- 변경 파일:
  - `pc_receiver/src/matholic_pdf_receiver/app.py`
  - `pc_receiver/tests/test_app.py`
  - 버전 0.1.6으로 맞춘 `pc_receiver/pyproject.toml`,
    `pc_receiver/src/matholic_pdf_receiver/__init__.py`
  - artifact 이름·checksum과 hidden smoke 실행을 맞춘
    `pc_receiver/build-receiver.ps1`
- 관련 commit: `a7ac871573c31ec81c212087734bfd5815546367`
  (`fix(receiver): recover pairing after LAN changes (SOL-0008)`). 전용 origin
  branch push 성공, 증거 갱신 직전 ahead/behind `0/0`.
- 자동검증:
  - `python -m pytest .\tests -q` (`pc_receiver`에서 실행): 구현 직후 22/22 PASS
    (`0.83s`), 최종 증거 재실행도 22/22 PASS (`1.06s`).
  - `python -m compileall .\src .\tests`: 성공.
  - package version import: 정확히 `0.1.6`.
  - `pc_receiver/build-receiver.ps1`의 PowerShell parser 오류: 0건.
  - `pc_receiver\build-receiver.ps1`: 전용 venv에서 pytest 22/22 PASS
    (`1.43s`), PyInstaller 6.15.0/Python 3.11.9 package build 성공,
    packaged `--smoke-check`의 인증된 loopback PDF 저장·ACK·정리 성공.
- artifact:
  - `artifacts/matholic-pdf-receiver-0.1.6.exe`, 22,785,740 bytes
  - SHA-256
    `1BAF483BEBE2FD8FFD968A425CBED8B784B074FEE796F0EE4AEF5F973EE3718F`
  - `artifacts/PC_RECEIVER_SHA256.txt`가 같은 파일·hash를 가리킨다.
    `artifacts/`는 ignore 상태이고 Git에 stage하지 않았다.
- 실제 운영 PC의 package 실행 검증:
  - 기준 설치본은 0.1.5이며
    `%LOCALAPPDATA%\MatholicPdfReceiver\app\MatholicPdfReceiver.exe`에서
    PyInstaller parent/child 2개와 `0.0.0.0:48129` listener가 실행 중이었다.
    Startup shortcut target과 `--background` 인자도 설치본을 가리켰다.
  - 정확한 설치본 process만 잠시 종료하고 0.1.6 artifact를 `--background` hidden
    상태로 실행했다. listener owner가 정확한 artifact path인 것을 확인했고,
    별도 artifact `--smoke-check`도 exit 0이었다.
  - 첫 현장 검사 script는 DPAPI `secret_protected` 암호문이 저장 전후 byte
    identical이어야 한다는 잘못된 진단 가정 때문에 최종 exit 1이었다. DPAPI는
    같은 평문도 보호할 때마다 무작위 ciphertext를 만들므로 이것은 제품 실패가
    아니다. script의 `finally`가 운영 0.1.5를 즉시 복구했고 listener도 돌아왔다.
  - 후속 비파괴 검사에서 secret 평문 fingerprint를 외부에 출력하지 않고 내부
    비교해 보존을 확인했다. receiver ID·endpoint fields도 보존됐고 DPAPI
    ciphertext rotation은 예상 동작이었다.
  - 당시 후속 검사는 process 이름을 정확히 `MatholicPdfReceiver.exe`로만 필터링해
    artifact 파일명 `matholic-pdf-receiver-0.1.6.exe`의 parent/child process를
    놓쳤다. 따라서 artifact process 0개라는 이전 판정은 잘못이었고 아래 운영
    설치에서 현재 path 전체를 기준으로 정정했다.
- 2026-08-05 운영 설치:
  - 사용자의 후속 설치 지시 뒤 설치 파일을 다시 대조했을 때 정식 설치 path와
    Startup shortcut은 0.1.5였지만, 별도 artifact path의 0.1.6 parent/child가
    2026-08-04 21:37부터 남아 있었다. 설치본 0.1.5 process를 종료한 첫 교체
    시도는 이 artifact process가 TCP 48129 listener를 유지해 파일 복사 전에
    `Installed receiver did not stop cleanly`로 중단됐다. 제품 파일·설정·방화벽은
    그 시점에 바뀌지 않았다.
  - 두 실행 path의 process를 모두 정확히 종료하고 기존 0.1.5 실행 파일을
    `%LOCALAPPDATA%\MatholicPdfReceiver\backup\MatholicPdfReceiver-before-0.1.6-20260805-000829.exe`에
    hash 일치 상태로 보존한 뒤, 검증된 0.1.6 artifact를 정식 설치 path에 복사했다.
  - 설치본 hash·process 2개·정확한 listener owner, 인증 `--smoke-check` exit 0,
    receiver ID·DPAPI secret 평문·port·표시명·수신 폴더 보존, Startup shortcut과
    기존 Private/Inbound/Allow/TCP 48129/설치 EXE 방화벽 경계, smoke PDF 잔존
    0개를 확인했다. 별도 artifact path process는 0개다.
- 실제 운영 네트워크 전이:
  - 별도 40초 자동 재연결 guard를 먼저 실행하고 PC Wi-Fi를 실제 disconnect했다.
    1초 안에 단절을 확인했고 그동안 설치 수신기 process, listener 1개와
    `--smoke-check` exit 0이 유지됐다. 같은 저장 profile로 reconnect한 뒤 Internet
    연결은 1초 안에 복구됐고 listener와 smoke도 다시 통과했다.
  - DHCP 활성 Wi-Fi에서 별도 복구 guard 뒤 `ipconfig /release "Wi-Fi"`를 실행해
    non-link-local IPv4가 0개가 됨을 확인했다. `/renew`까지 둘 다 exit 0이었고
    Internet·IPv4는 1초 안에 복구됐다. 이후 listener 1개와 smoke exit 0이었다.
  - 실제 Wi-Fi 1개와 활성 가상 interface 1개의 IPv4로 TCP 48129에 각각 연결해
    2/2 도달을 확인했다. 활성 default IPv4 route는 1개였다.
- Windows 재부팅·자동실행:
  - 재부팅 전 OS boot time을 보존하고 일회성 Startup 검증을 등록한 뒤 강제 종료
    옵션 없이 Windows를 실제 재부팅했다. 재개 뒤 boot time은
    `2026-08-05T01:10:24.5714280+09:00`으로 전진해 새 부팅을 확인했다.
  - 로그인 시 정식 Startup shortcut의 `--background` 수신기가 자동 실행됐다.
    일회성 결과는 실행 파일 hash, Startup target·argument, process 2개,
    listener 1개, listener owner의 정확한 설치 path와 smoke exit 0을 모두
    만족해 `success=true`, `error=null`이었다.
  - 일회성 검증 shortcut은 실행 즉시 제거됐다. Codex 재개 뒤 같은 상태를 다시
    조회하고 smoke를 별도로 실행해 process 2개, listener 1개, hash·owner 일치,
    smoke exit 0과 Wi-Fi IPv4 Internet 연결을 재확인했다.
- 남은 미검증:
  - 활성 VPN adapter가 0개라 VPN 경유 adapter 선택은 수행할 수 없었다.
  - PC GUI pairing QR의 실제 A 카메라 재촬영은 수행하지 않았다. 시험 QR PDF의
    공식 원격 제출과 PC 수신기 통신은 성공했지만 GUI pairing QR 광학 촬영으로
    확대하지 않는다.
- rollback:
  - 코드: `git revert a7ac871573c31ec81c212087734bfd5815546367` 후 receiver
    pytest 22개, compileall, package build와 packaged smoke를 다시 실행한다.
  - 운영 PC rollback은 수신기 process를 정확한 설치 path 기준으로 종료하고 위
    backup의 SHA-256이 기존 0.1.5 hash와 일치하는지 확인한 뒤 정식 설치 path로
    복원·`--background` 실행한다. receiver ID·DPAPI secret·수신 파일은 보존하고
    설정 삭제나 새 identity 생성으로 우회하지 않는다.

### SOL-0009 — 재사용 카드 QR이 시작·일반 갱신 경로에서 무효화될 수 있음

- 영역: Kiosk QR 식별자·학생 저장소·Room migration
- 심각도: P2
- 신뢰도: 높음
- 상태: 자동검증 완료
- 사용자 영향: 학생에게 배정한 재사용 실물 카드의 QR이 앱 재시작, 일반 QR
  재발급 또는 이름·CSV 갱신 뒤 바뀌면 이미 출력·부착한 카드가 로그인되지 않는다.
  운영자는 외관상 정상인 카드를 다시 만들어야 하고 새 학생 처리 흐름이 중단된다.
- 재현 조건(수정 전 구현):
  - 저장 ACK가 없는 재사용 슬롯이 있는 상태에서 앱을 재시작한다.
  - 배정된 재사용 학생을 개별·선택·반 전체 일반 QR 재발급에 포함한다.
  - 배정된 재사용 학생의 이름을 직접 또는 CSV로 변경한 뒤 일반 미출력 카드
    처리 경로를 사용한다.
- 기대 결과: 재사용 슬롯은 배정·이름·ID·PW·수업 소속 변화와 앱 재시작에도
  QR hash를 유지한다. 새 QR은 관리자가 실제 일반 QR 카드 전환 또는 명시적인
  저장 미확인 카드 폐기·교체를 선택할 때만 발급한다.
- 실제 결과(수정 전 구현): 시작의 `ensureReusableCardSlots()`가 pending 슬롯을
  다시 발급했고, 일반 단건·선택·반 일괄 재발급이 재사용 슬롯을 구분하지 않았다.
  이름·CSV 변경도 `needsPrint`를 다시 세워 후속 일반 재발급 대상으로 만들었다.
- 정적·동적 근거:
  - `StudentRepository`의 startup ensure와 manual prepare를 분리하고 startup에는
    `reissuePendingSlots=false`를 사용했다.
  - 단건·선택·반 일괄 재발급에서 `reusableCardLabel != null`을 거부한다.
  - 직접 이름·CSV 갱신은 재사용 카드의 `needsPrint`를 세우지 않고 CSV preview
    예상 수에도 넣지 않는다.
  - API 33 AVD 전체 70/70 PASS. `RepositoryInstrumentedTest`의 재사용 카드
    회귀는 최초 hash를 시작 ensure, 직접 이름, CSV, 단건·선택·반 전체 재발급
    시도 전후로 대조해 동일함을 확인한다.
- 반대 근거·미검증: A의 운영 DB와 실물 카드 hash를 의도적으로 회전시키는
  수정 전 재현은 데이터·실물 상태를 해치므로 수행하지 않았다. RC84 설치,
  Room 4→5 migration과 관리자 메뉴는 실제 A에서 확인했지만 실제 계정 배정·
  회수·전환은 하지 않았다.
- 원인·결정: 재사용 카드의 불변조건을 일반 학생 QR lifecycle과 분리하지 않은
  것이 원인이다. 저장소 경계에서 모든 일반 재발급을 거부하고 startup은 기존
  hash를 유지한다. UI 우회가 생겨도 저장소가 최종 방어한다.
- 변경 파일: `StudentRepository.kt`, `Daos.kt`, `Entities.kt`,
  `KioskDatabase.kt`, Room schema 5, 관련 repository·migration 시험,
  `MainActivity.kt`, `activity_main.xml`, RC84 version/release scripts.
- 관련 commit: `59c9181144ba8758a6710ff793dbcdac7a24a57b`; 전용 origin branch
  push 성공, 구현 push 직후 ahead/behind `0/0`.
- 자동검증:
  - Kiosk unit·debug lint·debug/AndroidTest assemble 84 tasks PASS.
  - 최종 전체 instrumentation `OK (70 tests)`, 89.815초.
  - 공식 release 158 tasks와 signer·non-debuggable·version 검증 PASS.
- A 현장검증: RC84/code 89 보존 설치, UID·firstInstallTime·Device Owner·HOME·
  data 유지, 설치 APK/artifact hash 일치, QR 대기·Lock Task `LOCKED` 복원.
- rollback: 코드 `git revert 59c9181144ba8758a6710ff793dbcdac7a24a57b` 후 위
  unit/lint/전체 계측/release를 재실행한다. A는 Room v5·code 91이므로 단순
  downgrade하지 않고 v5 호환 source와 같은 signer의 code 92+ forward rollback을
  만든다. 앱 삭제·data clear로 우회하지 않는다.

### SOL-0010 — 신규용 카드 관리 대화상자에서 작업 목록이 보이지 않음

- 영역: Kiosk 관리자 UI·AlertDialog
- 심각도: P2
- 신뢰도: 높음
- 상태: 현장검증 완료
- 사용자 영향: 관리자 버튼과 무료 4장 수치는 보이지만 팝업에 배정·전환·회수
  작업 항목이 표시되지 않아 신규용 카드 핵심 기능을 실제로 시작할 수 없다.
- 재현 조건(수정 전 RC82): 신규용 카드 관리 버튼을 눌러 message와 item 목록을
  함께 설정한 AlertDialog를 연다.
- 기대/실제: 작업 목록과 닫기 제어가 모두 보여야 하지만 제목·설명·닫기만
  표시되고 item 목록은 보이지 않았다.
- 정적 근거: 같은 AlertDialog에 message와 item adapter를 함께 구성해 list가
  표시되지 않는 구조였다. 상태 요약은 title로 옮기고 작업은 `setItems`만으로
  구성했다. 실제 카드 전환은 현재 student spinner에 의존하지 않고 별도 사용 중
  카드 선택 목록을 연다.
- 동적 근거:
  - RC83 실제 A에서 `신규용 QR 카드 · 전체 4장 · 무료 4장 · 사용 중 0장`과
    `무료 카드 학생에게 배정` item을 확인하고 item을 직접 눌렀다.
  - 이어 배정 dialog의 제목·확인·취소와 이름·ID·PW·PW 확인 4개 입력칸을
    확인한 뒤 입력 없이 취소했다.
  - 최종 RC84 실제 A에서도 같은 메뉴 제목·무료 4장·배정 item 표시를 다시
    확인했다. 실제 학생정보나 카드 상태는 변경하지 않았다.
- 반대 근거: 지원 A 한 대의 현재 2000×1200 landscape에서만 확인했다. 모든
  Android display·font scale로 확대하지 않는다.
- 관련 commit·변경 파일: `59c9181144ba8758a6710ff793dbcdac7a24a57b`,
  `MainActivity.kt`, `activity_main.xml`; 전용 origin branch push 성공.
- 자동검증: MainActivity source compile, debug lint, AndroidTest assemble,
  전체 instrumentation 70/70, official release 158 tasks PASS.
- rollback: SOL-0009와 같은 commit revert·검증·forward device rollback을 따른다.

### SOL-0011 — 저장 미확인 더미 QR 재전송이 기존 QR 폐기를 알리지 않음

- 영역: Kiosk 관리자 UI·QR 재발급 안전
- 심각도: P3
- 신뢰도: 높음
- 상태: 자동검증 완료
- 사용자 영향: PC 저장 ACK가 유실됐지만 파일이 저장·출력된 경계에서 관리자가
  “다시 전송”을 같은 QR 재전송으로 이해할 수 있다. 실제 동작은 새 QR 발급이므로
  이전 PDF·인쇄물이 예고 없이 무효가 된다.
- 재현 조건(수정 전 구현): 무료 재사용 슬롯의 `needsPrint=true` 상태에서
  `미출력 더미 QR 다시 지정 PC 전송`을 누른다.
- 기대 결과: 동일 QR을 재전송할 수 없으면 기존 QR 폐기와 이전 파일·인쇄물
  무효화를 명시하고 별도 확인을 받은 뒤 실행한다.
- 실제 결과(수정 전 구현): QR 원문은 저장하지 않고 hash만 보유하면서 action
  label은 단순 재전송으로 표시했고, 확인 없이 `prepareReusableCardSlots()`가
  QR을 회전했다.
- 원인·결정: bearer QR 원문을 reversible 저장해 공격면을 넓히지 않는다. 대신
  action을 `저장 미확인 카드 QR 폐기·새 QR 전송`으로 바꾸고, 같은 QR 재전송이
  불가능한 이유·기존 QR 폐기·이전 파일/인쇄물 폐기를 설명하는 확인창 뒤에만
  실행한다. 저장소의 명시적 manual prepare에서만 pending QR 회전을 허용한다.
- 동적·반대 근거: 현재 A는 무료 4장 모두 저장 완료 상태라 pending action이
  없었다. 운영 QR을 일부러 pending으로 만들지 않았으므로 A 경고창 실물 확인은
  수행하지 않았다.
- 관련 commit·변경 파일: `59c9181144ba8758a6710ff793dbcdac7a24a57b`,
  `MainActivity.kt`, 운영·제한 문서; 전용 origin branch push 성공.
- 자동검증: RC84 source compile, Kiosk unit/debug lint/AndroidTest assemble,
  전체 instrumentation 70/70, official release 158 tasks와 script parser PASS.
- rollback: SOL-0009와 같은 commit revert·검증·forward device rollback을 따른다.

### SOL-0012 — 비활성 재사용 슬롯이 있으면 목표 4장으로 복구되지 않음

- 영역: Kiosk 시작 bootstrap·학생 저장소·신규용 카드 복구
- 심각도: P3
- 신뢰도: 높음
- 상태: 자동검증 완료
- 사용자 영향: 과거 버전에서 신규용 카드 슬롯 한 장이 비활성화된 DB는 앱을
  재시작해도 활성 신규용 카드가 3장으로 남는다. 운영자는 4장이라고 안내된
  카드 풀보다 적은 수만 배정할 수 있고, UI에 이를 복구하는 별도 경로가 없다.
- 재현 조건(수정 전 RC84): 재사용 슬롯 4장을 준비한 뒤 한 슬롯을 과거 상태처럼
  `isActive=false`, `reusableCardAssigned=true`로 만들고 startup
  `ensureReusableCardSlots()`를 실행한다.
- 기대 결과: 활성 슬롯 부족분 1장을 안전한 무료 슬롯으로 복구하고 새 QR PDF를
  저장하도록 표시해 활성 슬롯을 목표 4장으로 만든다.
- 실제 결과(수정 전): 활성 슬롯은 3장이지만 비활성 슬롯 행이 존재해
  `allSlots.isEmpty()`가 거짓이었다. ensure 결과는 0장이고 활성 슬롯도 3장으로
  유지됐다.
- 정적·동적 근거:
  - 수정 전 focused instrumentation은 기대 복구 1장·실제 0장으로 실패했다.
  - 활성 슬롯 수로 부족분을 계산하고 비활성 슬롯을 먼저 같은 student ID·라벨로
    복구한다. 새 QR·암호화된 빈 ID/PW를 발급하고 반 소속을 제거하며 활성·무료·
    `needsPrint=true` 상태로 만든다. 남은 부족분만 새 슬롯으로 생성한다.
  - 수정 후 focused `OK (1 test)`, 최종 API 33 전체 `OK (71 tests)`로 통과했다.
    시험은 student ID·라벨 유지, QR hash 교체, 빈 자격정보, 소속 제거와 활성 4장을
    모두 단언한다.
- 반대 근거·미검증: 현재 A에는 이미 활성 무료 슬롯 4장이어서 비활성 슬롯을
  고의로 만들거나 운영 QR을 폐기하지 않았다. RC85 설치·메뉴 4장·최종 QR 대기는
  실물 확인했지만 자동 복구 분기 자체는 A 현장검증 완료로 확대하지 않는다.
- 원인·결정: 슬롯 “행 존재”와 “활성 운영 풀 충족”을 같은 조건으로 취급한 것이
  원인이다. 기존 비활성 슬롯 ID·라벨을 재사용해 불필요한 새 행을 피하되, 이전
  자격정보·소속·QR은 복구하지 않고 새 무료 카드로 실패폐쇄한다. 이전 QR·PDF·
  인쇄물은 무효이므로 실제 수량·라벨로 새 PDF를 전송한다.
- 변경 파일: `StudentRepository.kt`, `RepositoryInstrumentedTest.kt`,
  `MainActivity.kt`, Kiosk RC85 version/release scripts.
- 관련 commit:
  `e7c558da18152795d11427d13d8d61b5323e1640`; 전용 origin branch push 성공,
  구현 push 직후 ahead/behind `0/0`.
- 자동검증:
  - focused 수정 전 실패와 수정 후 `OK (1 test)`.
  - Kiosk unit·debug lint·debug/AndroidTest assemble 84 tasks PASS.
  - 전체 instrumentation `OK (71 tests)`, 86.062초.
  - 공식 release 158 tasks와 signer·non-debuggable·version 검증 PASS.
- A 현장 상태: RC85/code 90 보존 설치, UID·firstInstallTime·Device Owner·HOME·
  data 유지, 설치 APK/artifact hash·signer 일치, 무료 4장 메뉴와 QR 대기·Lock Task
  `LOCKED` 확인. 비활성 복구 분기는 수행하지 않음.
- rollback: 코드
  `git revert e7c558da18152795d11427d13d8d61b5323e1640` 후 위 unit/lint/전체
  계측/release를 재실행한다. A는 code 93이므로 같은 signer·code 94+의 forward
  rollback을 만들고 앱 삭제·data clear·downgrade를 하지 않는다. Room v5는
  유지한다.

### SOL-0013 — 무응답 지정 PC가 시작 관리자 화면을 지연시킴

- 영역: Kiosk startup·executor·신규용 카드 PDF 전송·복구 UX
- 심각도: P2
- 신뢰도: 높음
- 상태: 자동검증 완료
- 사용자 영향: 최초 실행이나 비활성 슬롯 복구로 카드 PDF가 준비된 시점에 저장된
  지정 PC가 연결만 받고 응답하지 않으면 Kiosk가 `초기 상태 확인 중`에 머물고
  관리자 PIN과 반 데이터가 나타나지 않는다. 네트워크 장애가 인증·수업 복구와
  무관한 핵심 시작 흐름까지 지연시킨다.
- 재현 조건(수정 전 RC85): 빈 DB에 관리자 PIN과 loopback PC pairing을 만들고,
  receiver가 TCP 연결은 수락하지만 PDF 응답을 보내지 않게 한 뒤 MainActivity를
  시작한다.
- 기대 결과: DB·PIN·세션·재사용 카드 준비가 끝나면 관리자 인증을 즉시 표시하고,
  PDF 전송은 독립적으로 완료·실패해 관리자 데이터 준비를 막지 않는다.
- 실제 결과(수정 전): `loadInitialState()`가 핵심 초기화와 같은 `ioExecutor`에서
  `deliverReusableQrCards()`의 connect/read timeout·주소 복구·재시도를 기다린 뒤
  `showAuthentication()`을 게시했다. focused 시험은 연결 성립 뒤 2초 UI 제한을
  넘겨 `Time: 6.048`, `Tests run: 1, Failures: 1`로 실패했다.
- 정적·동적 근거:
  - DB open, 감사 정리, PIN·세션 복구, `ensureReusableCardSlots()`는 기존
    `ioExecutor` 직렬 경계에 유지했다. 성공 snapshot을 UI에 먼저 게시한 뒤 실제
    PDF 전달만 `pcControlExecutor`에 제출한다.
  - 전달 완료 메시지는 UI thread에서 관리자 패널과 operation gate 상태를 확인해
    즉시 안전 refresh하거나 다음 관리자 진입까지 보관한다. 카드가 없거나 초기화가
    실패하면 별도 PC task를 만들지 않는다.
  - 수정 후 focused 시험은 receiver 연결을 계속 보류한 상태에서도 인증 UI를 2초
    안에 표시하고 PIN 뒤 관리자 패널·반 데이터를 3초 안에 준비해
    `Time: 7.447`, `OK (1 test)`로 통과했다.
  - 최종 Kiosk 84 tasks, API 33 전체 `Time: 90.795`, `OK (72 tests)`, 공식
    release 158 tasks와 RC86 APK 이중 검증을 통과했다.
- 반대 근거·미검증: 시험 receiver는 실제 Windows PC가 아닌 Android emulator의
  loopback socket이다. 현재 A에는 신규 준비 대상 카드가 없어 운영 QR을 폐기하거나
  운영 PC를 고의로 stall시키지 않았다. A RC86 설치·인증·관리자·복구·Web
  사전점검·QR 대기는 통과했지만 정확한 무응답 bootstrap 분기의 현장 통과로
  확대하지 않는다.
- 원인·결정: 카드 PDF 전달을 필수 DB 초기화의 성공 조건처럼 같은 executor에서
  기다린 것이 원인이다. 카드 발급의 DB 원자성과 기존 전송 보안·ACK 정책은
  유지하고, 외부 PC I/O만 이미 존재하는 전용 executor로 격리했다. PC 실패는
  카드 준비 자체를 되돌리지 않고 기존 수동 재준비 안내로 복구한다.
- 변경 파일: `MainActivity.kt`, `MainActivityInstrumentedTest.kt`, Kiosk RC86
  version/release scripts.
- 관련 commit:
  `b8ce2d394e5f02655a4bbe70842d4dde7bde1466`; 전용 origin branch push 성공,
  구현 push 직후 ahead/behind `0/0`.
- A/PC 현장 상태: Kiosk RC86/code 91과 기존 Web RC137 설치 APK가 각 artifact와
  byte·SHA-256·signer 일치한다. UID·firstInstallTime·Device Owner·HOME·data를
  유지했고 안전 복구 뒤 QR 대기·Lock Task `LOCKED`, 원격 `INACTIVE`, 포트 매핑과
  crash buffer 일치 항목 없음이다. 운영 PC 수신기와 pairing은 변경하지 않았다.
- rollback: 코드
  `git revert b8ce2d394e5f02655a4bbe70842d4dde7bde1466` 후 Kiosk unit/lint,
  AndroidTest assemble·전체 계측과 공식 release를 재실행한다. A는 code 93이므로
  같은 signer·code 94 이상의 forward rollback을 `adb install -r`로 설치하고
  APK 삭제·data clear·downgrade를 하지 않는다.

### SOL-0014 — 연결 뒤 읽지 않는 지정 PC가 PDF write를 무기한 점유함

- 영역: Kiosk PC PDF sender·socket timeout·executor/resource bound
- 심각도: P2
- 신뢰도: 높음
- 상태: 자동검증 완료
- 사용자 영향: 지정 PC가 TCP 연결은 수락하지만 process stall·disk/보안 제품 지연
  등으로 payload를 읽지 않으면 명시적 PDF 전송이 끝나지 않는다. 단일
  `pcControlExecutor`가 점유돼 이후 PC 상태·파일·알림 작업도 queue에서 진행되지
  않으며 관리자에게 완료·실패 결과가 돌아오지 않는다.
- 재현 조건(수정 전 RC86): loopback `ServerSocket`의 receive buffer를 1KiB로
  제한하고 연결만 수락한 채 읽지 않는다. Kiosk sender에서 허용 상한 5MiB PDF를
  전송하고 2초 안에 종료되는지 확인한다.
- 기대 결과: connect·write·ACK read 각각이 유한한 시간 안에 성공 또는 실패하고,
  peer가 읽지 않아도 sender와 전용 executor가 회수된다.
- 실제 결과(수정 전): `Socket.soTimeout`은 input read에만 적용되고
  `getOutputStream().write(frame)`에는 제한이 없었다. focused JVM 시험은 2초
  future 제한을 넘겨 `PDF send exceeded its bounded I/O timeout`으로 실패했다.
  peer를 finally에서 닫은 뒤에야 worker가 풀렸고 해당 Gradle 실행은 약 53.3초였다.
- 정적·동적 근거:
  - `PcPdfSender`는 blocking `SocketChannel` writer를 daemon thread에서 실행하고
    호출자가 기본 10초 write timeout만 기다린다. timeout·interrupt에서 channel을
    닫고 writer를 정리한다. 정상 완료에는 socket을 보존해 기존 인증 ACK를 읽는다.
  - 최종 JVM 정상 frame+ACK와 max payload stall 2/2는 0.579초, Android API 33
    max payload stall focused 1/1은 250ms 설정·총 2초 미만으로 통과했다.
  - Kiosk 84 tasks, 전체 API 33 73/73, 공식 release 158 tasks를 통과했다.
- 반대 근거·중간 실패:
  - 첫 nonblocking selector 구현은 JVM/Kiosk를 통과했지만 API 33 전체 72개 중
    quick-class·startup 2개 timeout과 높은 executor CPU를 보여 폐기했다. quick-class
    focused 자체는 통과했으나 startup의 PIN 뒤 관리자 데이터 3초 제한이 반복
    실패했다. 이 구현은 commit되지 않았다.
  - blocking writer 첫 보정도 write latch 직후 정상 socket을 닫는 race로 정상 ACK
    시험이 `SocketException` 실패했고, timeout 여부와 write 완료를 분리해 수정했다.
  - 현재 A에는 새로 보낼 PDF가 없어 실제 Windows 운영 PC를 일부러 partial
    stall시키지 않았다. A RC87 설치·인증·복구·Web 사전점검·QR 대기는 통과했지만
    정확한 영향 분기의 현장 통과로 확대하지 않는다.
- 원인·결정: Java socket의 read timeout이 write까지 제한한다고 가정한 것이
  원인이다. Android/JVM 양쪽에서 close로 blocking write가 해제됨을 시험하고,
  전체 channel을 nonblocking으로 바꾸는 대신 bounded helper thread로 기존
  connect·ACK stream 호환성과 CPU idle을 유지했다.
- 변경 파일: `PcPdfSender.kt`, JVM·Android sender 회귀시험,
  `MainActivityInstrumentedTest.kt`, Kiosk RC87 version/release scripts.
- 관련 commit:
  `51d1b03c7694f685e91421c0c8953fc9b8aae329`; 전용 origin branch push 성공,
  구현 push 직후 ahead/behind `0/0`.
- A/PC 현장 상태: Kiosk RC87/code 92 설치 APK가 artifact와 byte·SHA-256·signer
  일치한다. UID·firstInstallTime·Device Owner·HOME·data를 유지했고 안전 복구 뒤
  QR 대기·Lock Task `LOCKED`, 원격 `INACTIVE`, 포트 매핑과 crash buffer 일치 항목
  없음이다. 운영 PC 수신기와 pairing은 변경하지 않았다.
- rollback: 코드
  `git revert 51d1b03c7694f685e91421c0c8953fc9b8aae329` 후 Kiosk unit/lint,
  AndroidTest assemble·전체 계측과 공식 release를 재실행한다. A는 code 93이므로
  같은 signer·code 94 이상의 forward rollback을 `adb install -r`로 설치하고
  APK 삭제·data clear·downgrade를 하지 않는다.

### SOL-0015 — process 재시작 뒤 공유 QR PDF가 만료 상한을 넘길 수 있음

- 영역: Kiosk QR PDF cache·FileProvider·process lifecycle·민감 임시파일 회수
- 심각도: P3
- 신뢰도: 높음
- 상태: 자동검증 완료
- 사용자 영향: 사용자가 신규용 QR 카드 PDF를 공유하는 동안 Kiosk process가
  종료되면 메모리의 삭제 예약도 사라진다. 1시간이 되기 전에 앱이 한 번
  재시작되면 아직 젊은 orphan은 지워지지 않았고 남은 만료도 다시 예약되지 않아,
  로그인 가능한 QR PDF가 다음 export·재시작·OS cache 회수 전까지 앱 private
  cache에 의도한 1시간 상한보다 오래 남을 수 있었다.
- 재현 조건(수정 전 RC87): `qr_exports/`에 만료까지 약 1.5초 남은 합성 비-QR
  PDF를 두고 startup `cleanupExpired()`를 실행한 뒤 process를 계속 살려 5초 이상
  기다린다. 별도로 manifest provider가 AndroidX 기본 `FileProvider`인지 확인한다.
- 기대 결과: 이미 만료된 파일은 즉시 삭제하고, 아직 젊은 파일은 마지막 수정
  시각 기준 남은 수명만 다시 예약한다. 외부 URI 요청으로 process가 시작돼도
  provider가 파일을 내주기 전에 같은 만료 정리를 수행한다.
- 실제 결과(수정 전): startup 정리는 당시 1시간 이상인 파일만 삭제했다. 젊은
  파일은 5.033초 뒤에도 남아 focused test 1/1 failure였고, manifest provider는
  `androidx.core.content.FileProvider`라 Activity 없는 URI 재기동에는 앱 정리가
  없었다. provider 계약 test도 0.027초 1/1 failure였다.
- 정적·동적 근거:
  - `QrPdfExporter.cleanupExpired()`가 파일 나이를 한 번 계산해 만료 파일은 즉시
    삭제하고, 젊은 파일은 남은 수명만 process Handler에 예약한다. 0 이하
    `lastModified`는 만료로, 미래 시각은 재시작 뒤 최대 1시간으로 제한한다.
  - `QrPdfFileProvider.onCreate()`가 `super` 초기화 뒤 같은 cleanup을 실행한다.
    기존 non-exported authority, 일시 read grant와 `qr_exports/` path 범위는
    바꾸지 않았다.
  - 젊은 orphan focused test는 수정 후 `Time: 1.134`, `OK (1 test)`였다.
    전용 provider manifest 계약은 `Time: 0.018`, 실제 manifest `ProviderInfo`로
    초기화한 만료 선행 삭제는 `Time: 0.032`, 각각 `OK (1 test)`였다.
  - Kiosk unit·debug lint·debug/AndroidTest assemble 84 tasks PASS. API 33 전체
    instrumentation `Time: 77.511`, `OK (76 tests)`, 실패·skip 0.
  - 공식 release 158 tasks, `BUILD SUCCESSFUL in 2m 5s`; Kiosk JVM 99개·Web JVM
    67개, release lint, signed assemble, version·non-debuggable·동일 signer PASS.
- 반대 근거·미검증:
  - 파일은 app private cache와 non-exported provider 아래 있고 share target은
    사용자가 명시적으로 선택한다. 따라서 일반 파일 공개 결함은 아니며 영향은
    선택한 로그인 가능 QR의 잔존 기간에 한정된다.
  - process가 계속 죽어 있으면 앱은 정확히 1시간에 private 파일을 물리 삭제할 수
    없다. 다만 FileProvider URI 재접근이 process를 시작하면 만료 파일을 제공하기
    전에 삭제한다. 외부 수신기가 이미 읽어 복사한 파일은 앱이 회수할 수 없다.
  - 실제 A에서 공유 중 process kill·1시간 대기·외부 URI 재접근을 강제하지 않았다.
    위 시간 경계는 합성 비-QR API 33 계측이며 현장검증 완료로 확대하지 않는다.
- 원인·결정: RC12/RC15의 정상 복귀 30초·공유 시작 1시간 삭제가 process-local
  Handler에만 의존했고, startup sweep이 젊은 파일의 남은 수명을 재구성하지 않은
  부분 잔존이었다. 별도 AlarmManager/WorkManager를 추가하지 않고 현재 cache
  계약 안에서 재시작 시 예약을 복구하며, 외부 접근 경계에는 전용 provider의
  fail-closed 선행 정리를 추가했다.
- 변경 파일: `QrPdfExporter.kt`, 신규 `QrPdfFileProvider.kt`, Android manifest,
  `QrPrintDocumentAdapterInstrumentedTest.kt`, Kiosk RC88 version/release scripts.
- 관련 commit:
  `a5905540cd9a74d589bb78fee7ea247bb35a56ac`; 전용 origin branch push 성공,
  구현 push 직후 ahead/behind `0/0`.
- A/PC 현장 상태: 같은 signer RC88/code 93을 승인 A 한 대에 `adb install -r`로
  보존 설치했다. 설치 APK와 artifact byte·SHA-256·signer, 새 provider 등록을
  다시 확인했다. UID·firstInstallTime·Device Owner·HOME·data·Lock Task를
  유지했고 안전 복구, 카드 메뉴, Web 사전점검과 QR 대기까지 통과했다. 실제
  공유·process kill은 수행하지 않았고 운영 PC 수신기는 변경하지 않았다.
- rollback: 코드
  `git revert a5905540cd9a74d589bb78fee7ea247bb35a56ac` 후 Kiosk unit/lint,
  AndroidTest assemble·전체 계측과 공식 release를 재실행한다. A는 code 93이므로
  되돌린 source에서 같은 signer·code 94 이상의 forward rollback release를
  `adb install -r`로 설치하며 APK 삭제·data clear·downgrade를 하지 않는다.

### SOL-0016 — 앱 종료 뒤 active PC receiver handler가 남을 수 있음

- 영역: PC receiver·tray/Tk shutdown·active socket/thread·민감 state lifetime
- 심각도: P3
- 신뢰도: 높음
- 상태: 자동검증 완료
- 사용자 영향: 운영자가 알림 영역의 종료를 누르거나 Tk mainloop가 반환해도 이미
  연결된 partial request handler와 client socket이 최대 30초 deadline 동안 남을 수
  있었다. process가 먼저 끝나면 in-flight 작업은 ACK/event 완료 경계를 거치지
  못하고, handler가 state lock 안에서 지연되면 앱 cleanup의 pending CSV memory
  정리도 종료를 무기한 붙잡을 가능성이 있었다.
- 재현 조건(수정 전 0.1.7): active connection 상한을 1로 둔 loopback server에
  연결해 request magic 1 byte만 보낸다. handler가 slot을 점유한 뒤 listener
  `shutdown()`과 `server_close()`를 호출하고 client 연결이 닫혔는지 확인한다.
- 기대 결과: 새 연결 admission을 먼저 막고 active socket을 닫아 network handler를
  깨운다. handler가 유한한 시간 안에 회수된 뒤에만 공유 receiver state를 닫으며,
  state 내부 지연 때문에 앱 종료가 무기한 멈추지 않는다.
- 실제 결과(수정 전): `daemon_threads=True`라 Python `ThreadingMixIn`은 handler를
  join 목록에 넣지 않았고 server는 active socket을 추적하지 않았다. focused test는
  `server_close()` 반환 뒤 client `recv()`가 250ms timeout이 되어 0.78초 1/1
  failure였다. connection slot도 handler deadline 전에는 회수되지 않았다.
- 정적·동적 근거:
  - `ThreadedReceiverServer`는 admission 성공 직후 socket을 condition 아래 추적하고
    handler `finally`에서 제거·알림한다. `server_close()`는 listener close 뒤 active
    socket을 shutdown/close하고 최대 2초 condition wait한다.
  - partial handler 수정 후 단건은 0.09초 PASS이고 client EOF/reset과 connection
    slot 회수를 확인했다. state operation이 시험용 50ms보다 오래 머무는 경우에는
    0.5초 안에 `active_handlers_drained=false`로 반환했다.
  - 앱 cleanup은 listener thread도 종료되고 `active_handlers_drained=true`일 때만
    `ReceiverState.close()`를 호출한다. drain 실패 때 state close를 생략하는 순서와
    정상 순서를 mock 시험 2개로 고정했다. focused 4/4는 1.40초 PASS다.
  - source 전체 pytest 27/27, 1.98초, compileall 오류 0. 공식 build environment
    pytest 27/27, 2.59초, PyInstaller 6.15.0 package와 독립 인증·저장·ACK·정리
    smoke를 통과했다.
- 반대 근거·미검증:
  - 기존 각 handler는 최대 32개·30초, recv 10초와 payload 상한이 있어 일반적인
    무제한 resource leak은 아니었다. PDF 저장은 fsync·atomic replace와 persistent
    replay receipt를 사용하고 ACK 유실 재시도도 idempotent하다.
  - 실제 운영 PC에서 전송 중 tray Quit, Windows 로그오프·종료, 강제 process kill은
    수행하지 않았다. Computer Use가 background tray와 Explorer의 targetable window를
    반환하지 않아 좌표를 추측하지 않았고 exact 영향 분기는 loopback synthetic
    request다. 실제 PDF·페어링·학생·CSV 데이터는 변경하지 않았다.
  - 2초 뒤에도 state operation이 회수되지 않으면 daemon handler와 in-memory CSV는
    process exit에 맡긴다. 이는 무기한 shutdown보다 bounded fail-safe를 우선한
    잔여 경계이며 정상 application-level ACK 완료를 보장하는 drain은 아니다.
- 원인·결정: SOL-0004는 admission/frame/event resource 상한을 교정했지만 daemon
  handler의 process 종료 가정을 실제 socket close barrier로 만들지 않은 부분
  잔존이었다. non-daemon handler로 바꿔 모든 disk/OS 지연을 무기한 join하지 않고,
  active socket close와 bounded wait를 명시해 정상 network handler는 회수하고
  비정상 state 지연에서는 공유 state를 먼저 지우지 않도록 했다.
- 변경 파일: PC receiver `server.py`, `app.py`, `test_server.py`, `test_app.py`,
  0.1.8 version/build metadata와 receiver README.
- 관련 commit:
  `850b6f31ad272c71175cb724d6d4058b9d295752`; 전용 origin branch push 성공,
  구현 push 직후 ahead/behind `0/0`.
- 운영 PC 상태: 0.1.7 exact artifact/hash를 timestamped backup에 보존한 뒤 active
  연결 0개 상태에서 exact installed process만 종료하고 0.1.8을 같은 path에
  교체했다. 설치본 23,187,199 bytes·SHA-256
  `55BF10A5AB6E41B94A18478D38CFD4C9F1B4ACDCA0609667C8B9ED857C92D3A0`,
  independent smoke exit 0, background parent/child 2개, listener 1개,
  established 0개다. Startup·Private firewall·DPAPI config를 보존했다.
- rollback: 코드
  `git revert 850b6f31ad272c71175cb724d6d4058b9d295752` 후 pytest, compileall,
  package와 독립 smoke를 재실행한다. 운영 PC는 active 연결 0개를 확인하고 위
  0.1.7 backup hash를 검증한 뒤 같은 설치 path에 복원해 smoke·listener·Startup·
  firewall을 다시 확인한다. 페어링 config와 수신 PDF는 삭제하지 않는다.

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
- `SOL-0007` 전체 계측·unit·lint 증거 commit:
  `13e46747ad7cbc805c4c8fd7af49ad0f0a14573e`.
- `SOL-0008` PC receiver LAN 복구 구현·회귀·0.1.6 package 준비 commit:
  `a7ac871573c31ec81c212087734bfd5815546367`.
- `SOL-0009`~`SOL-0011` 재사용 QR 카드 구현·QR 고정·관리 UI·RC84 release
  metadata commit:
  `59c9181144ba8758a6710ff793dbcdac7a24a57b`.
- `SOL-0009`~`SOL-0011` RC84 자동·A 현장검증 증거 commit:
  `b76fdf8043f34e9abb063d683db655fe6280d882`.
- `SOL-0012` 비활성 재사용 슬롯 복구·회귀·RC85 release metadata commit:
  `e7c558da18152795d11427d13d8d61b5323e1640`.
- `SOL-0013` 시작 PDF 전송 격리·회귀·RC86 release metadata commit:
  `b8ce2d394e5f02655a4bbe70842d4dde7bde1466`.
- `SOL-0014` 지정 PC PDF write timeout·JVM/Android 회귀·RC87 release metadata
  commit: `51d1b03c7694f685e91421c0c8953fc9b8aae329`.
- `SOL-0015` 공유 QR PDF 재시작 만료 복구·provider 선행 정리·API 33 회귀·RC88
  release metadata commit: `a5905540cd9a74d589bb78fee7ea247bb35a56ac`.
- `SOL-0016` PC receiver active socket 종료 barrier·state cleanup 순서·0.1.8
  package metadata commit: `850b6f31ad272c71175cb724d6d4058b9d295752`.
- 운영 PC·실제 A의 부하·입력·Wi-Fi·DHCP·다중 interface·AVD 확장 증거 commit:
  `2b766a176128700865afd8741a4f23fb3107fbda`.
- 위 구현·증거 commit은 모두 전용 원격 branch push 성공. 과거 SOL-0008 뒤
  Goal 종료 기록은 당시 스냅샷이며, 사용자가 2026-08-12 연속 리뷰·개발을 다시
  명시해 현재 Goal은 활성 상태다. 사용자가 명시적으로 중단할 때까지 완료로
  처리하지 않는다.
- 과거 Goal 종료 A 안전점검의 첫 wrapper는 `adb devices -l`의 model token
  `SM_P610`을 `SM-P610`으로 직접 비교해 승인 A 판정을 false로 냈고, 원격 임시
  파일 검사에는 Android shell 인용 오류가 있었다. 제품 실패나 상태 변경은
  없었다. `getprop ro.product.model`과 단순 file existence 검사로 각각 승인 A
  한 대와 임시 캡처 없음이 확인됐다.
- rollback 수행 없음.
