# LUNA MAX 읽기 전용 연속 리뷰 보고서

## 2026-08-03 전체 교정 완료 판정 — 이 절이 최종 최신 판정

- 최초 읽기 전용 감사 기준선 `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`에서
  인용한 파일·줄·commit을 교정 전 workspace와 독립 재대조했다.
- 과장·중복을 제거한 유효 finding은 모두 source·시험·현재형 문서에 반영했다.
  `LUNA-0040`은 제품 요구가 아닌 시험 답안 정리 절차를 확장한 주장이므로
  기각 상태를 유지한다.
- 최종 source는 Kiosk `0.6.0-rc57`/code 62, Web POC
  `0.4.0-rc119`/code 136, PC 수신기 `0.1.5`다. 태블릿과 운영 PC에는
  설치하지 않은 미배포 검증 묶음이다.

### 최종 ID별 처리 결과

| ID | 최종 처리 | 교정 commit |
|---|---|---|
| `LUNA-0001` | 현재 source·운영 기준선과 릴리스 표기 정렬 | `d423d68`, `c24d3cd`, `0096317` |
| `LUNA-0002` | PC pairing secret DPAPI 보호·원자적 이전 | `a426cc4` |
| `LUNA-0003` | CSV를 tablet 확인 전까지 암호화 보존·확인 멱등화 | `bc7c6fe` |
| `LUNA-0004/0013/0031/0032` | 중복 P3를 하나의 P4 방어강화로 병합; mutable credential의 파싱 실패·queue·종료·적용 cleanup 완료 | `7cd2dfe` |
| `LUNA-0005` | QR·수동·Web 결과를 단일 launch gate와 시작 session ID에 결합 | `0929f94` |
| `LUNA-0006` | QR hash 임시 배열 소유권·실패 cleanup 완료 | `20e6baa` |
| `LUNA-0007/0008/0010` | 연결·event·deadline 상한, replay 유효기간 보존, PDF request 멱등 처리·ACK 유실 재시도 동일 ID 적용 | `47b76ec` |
| `LUNA-0009` | Windows 알림에서 학생 이름 제거 | `5b8088a` |
| `LUNA-0011` | audit 90일/10,000행 상한과 장기 실행 중 주기 정리, private log 90일·크기·bounded tail·회전 실패폐쇄 | `888b480`, `c6572d7` |
| `LUNA-0012` | 원격 지원 배지 전용 header 공간 예약 | `0e0c9b4` |
| `LUNA-0014` | PC protocol 평문·파생 key·ACK 예외 경로 cleanup | `90ab37f` |
| `LUNA-0015` | 비활성 학생 live credential 암호문·IV 즉시 폐기 및 DB migration | `1ccfdc1` |
| `LUNA-0016~0019` | 완전 불투명 network pause, WebView/IME/key/touch/접근성 차단, callback 실패 polling fallback | `d2f69cc` |
| `LUNA-0020/0021` | 민감 화면 `FLAG_SECURE` 복원, Web debugging 차단, 상태 commit 확인·ordered ACK | `a2e4dd3`, `69d0426` |
| `LUNA-0022/0024~0027` | exact `LOCKED`, restriction cleanup, undo invalidation, 활성 session membership 차단, 종료 UI snapshot, recovery action 보존 | `d558937` |
| `LUNA-0023` | QR 크기 상한, 내부 pixel·BitMatrix cleanup, renderer 계측 회귀 추가 | `9047bd7` |
| `LUNA-0028` | scalar PIN 조회와 verifier/Room 배열 소유권·zeroize 계약 | `cd0f1cc` |
| `LUNA-0029` | proxy 8 tunnel 상한·60초 idle 회수·listener 실패 1회 자동 재시작 | `3410fd5` |
| `LUNA-0030` | pairing 저장·복구를 byte 경로로 변경하고 executor 대기 raw String 제거 | `9e0dea5` |
| `LUNA-0033` | 최신 상태만 보유하는 coalescing dispatcher와 기능성 제어 worker 분리 | `c272a5b` |
| `LUNA-0034` | RFC1918 동일 subnet·인증 status probe 뒤 최초 신뢰 저장 | `bc4c323` |
| `LUNA-0035` | Web recovery와 모든 학생·반 변경 상호 차단 | `ecb1825` |
| `LUNA-0036/0037` | 직접 인쇄 제거, 공통 관리자 operation gate, 카드 PDF lifecycle·멱등 delivery 결합 | `40c69b3`, `a3368b6`, `f0c30cd`, `47b76ec` |
| `LUNA-0038` | Web 문항 상태를 과제 scope별 최근 8개로 격리 | `af46f82` |
| `LUNA-0039` | 직접 인쇄 제거와 `새 카드 PDF 생성 필요/PC 저장 확인` 의미 정정 | `40c69b3`, `f0c30cd`, `c0a445d` |
| `LUNA-0040` | 제품 요구가 아니므로 기각; 로그아웃 성공과 서버 답안 삭제를 동일시하지 않음 | `a932cee` |
| `LUNA-0041/0042` | PC CSV mutable buffer wipe·암호화 내구성, startup/shutdown owner·thread join·listener 순서 보강 | `bc7c6fe`, `47b76ec` |

### 최종 검증과 남은 경계

- `scripts/build-release.ps1`: 158 tasks, Kiosk/Web JVM 시험, release lint,
  signed assemble, version·`debuggable=false`·동일 signer 검증 모두 PASS.
- AndroidTest source: Kiosk/Web `compileDebugAndroidTestKotlin` PASS. 설치된
  운영 태블릿의 데이터·release signer를 바꾸지 않기 위해 instrumentation
  실행은 생략했다.
- PC 수신기: `17 passed`, PyInstaller packaging, 실행 파일 `--smoke-check` PASS.
- JVM/ML Kit가 제공하는 최초 QR `rawValue` 및 CSV parser의 immutable
  `String`은 즉시 zeroize를 보장할 수 없다. 앱이 소유하는 byte/char 배열과
  queue owner는 모두 정리했으며 외부 노출 증거가 없어 별도 미해결 P3로
  계산하지 않는다.
- 실제 태블릿 네트워크 단절·접근성·remote support·Lock Task fault, 실제
  수동 학생 선택과 PC에서의 물리 인쇄는 현장 검증 항목이다. 자동 검증 통과를
  그 실기 PASS로 확대하지 않는다.
- 기존 사용자 미커밋 checklist/scenario와 `diagnostics/`, `output/`, `tmp/`,
  운영 계약서 미추적 파일은 수정·stage·commit하지 않았다.

## 2026-08-02 1차 교정 재평가 — 위 2026-08-03 최종 판정으로 대체됨

- 최초 감사 기준선은 `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`였고,
  사용자의 후속 명시 승인으로 읽기 전용 계약을 종료한 뒤 교정했다.
- 교정 source 기준선은 `ecb1825`이며, 아래의 기존 `최신 요약`, 발견 요약표와
  상세 발견 본문은 당시 조사 이력이다. 서로 충돌하면 이 절과 각 상세 발견
  바로 아래의 `교정 후 판정`을 우선한다.
- 보고서의 과거 파일·줄 번호는 `8c9a97c` 근처의 증거 위치이지 현재 source
  위치가 아니다. 현재 판정은 파일을 다시 검색하고 Kotlin/Android test compile,
  JVM test, Python test로 독립 검증했다.
- 과거 “ADB executable이 없다”는 표현은 PATH에 없다는 뜻으로만 유효하다.
  실제 SDK의 `platform-tools/adb.exe`와 승인된 SM-P610 한 대를 확인했다.

### 최신 정정표

| ID/주제 | 과거 판정의 문제 | 교정 후 판정 | 근거 commit |
|---|---|---|---|
| `LUNA-0002` | 현재 사용자 ACL·로컬 단일 PC라는 반대 근거를 충분히 반영하지 않은 `P2`는 과장 | baseline은 `P3 확정`; 현재는 설정 v2 DPAPI 보호와 v1 원자적 이전으로 **해결** | `a426cc4` |
| `LUNA-0004/0013/0031/0032` | 같은 CSV 민감 사본 수명 문제를 네 건의 P3처럼 중복 계산 | `LUNA-0013` umbrella의 `P4 방어강화`로 병합. 관리 heap의 immutable String 즉시 zeroize는 보장 불가하며 외부 노출 증거는 없음 | 판정 정정 |
| `LUNA-0035` | 단일 executor·repository 불변조건·Activity 재생성 시 fail-safe action 유실을 반대 근거로 충분히 반영하지 않음 | baseline은 `P4 후보`; Web recovery와 모든 학생·반 관리자 변경을 상호 차단해 **해결** | `ecb1825` |
| `LUNA-0036` | 직접 인쇄 spooler와 관리자 작업 gate 부재가 섞인 유효 후보 | Android 직접 인쇄 제거, 단건·CSV·반·QR·PDF 작업 공통 gate로 **해결** | `40c69b3`, `a3368b6` |
| `LUNA-0037` | `LUNA-0036`과 정상적인 사후 카드 lifecycle을 별도 P3로 중복 계산 | `LUNA-0036`/카드 PDF 상태 정책으로 병합·종결. 전송 중 변경은 차단하고 이후 이름/QR 변경은 새 PDF 필요, 비활성화는 QR 폐기 | `a3368b6`, `f0c30cd` |
| `LUNA-0038` | 과제 scope 없는 전역 문항 상태 map은 유효 후보 | URL·명시 과제 표식/fallback별 최근 8개 scope로 분리해 **해결** | `af46f82` |
| `LUNA-0039` | `PrintManager` callback을 물리 인쇄 성공처럼 표현한 제목은 부정확 | 직접 인쇄 경로 제거로 **해결**. `needsPrint`는 사용자가 지은 문구가 아니며 현재 제품 의미는 `새 카드 PDF 생성 필요` | `40c69b3`, `f0c30cd` |
| `LUNA-0040` | docs-only commit `8c9a97c`의 시험 정리 기준을 제품의 서버 답안 삭제 요구로 잘못 확장 | **기각/철회**. 로그아웃 성공은 인증 종료를 뜻하며 서버 임시답안 삭제를 뜻하지 않는다. Web origin storage 전체 삭제도 제거 | `a932cee` |

### 사용자 결정의 출처와 남은 의견

- `needsPrint` 문구는 사용자가 직접 설정하지 않았다. `생성 파일 검토 및
  역질문` 작업에서 Codex가 “이름 변경·신규 등록 등의 경우 카드 출력 필요
  상태”를 제안했고 사용자가 `전부 추천안대로`라고 승인한 것이 정확한 출처다.
- 그 승인은 물리 인쇄·실물 전달 완료 상태를 뜻하지 않는다. 지정 PC의 PDF
  저장 ACK까지만 앱이 확인하며, 실제 인쇄 여부는 상태로 주장하지 않는다.
- 이번 교정 범위에는 추가 사용자 판단이 필요한 항목이 없다. 보고서의 나머지
  오래된 후보들은 이번 승인 범위에서 구현한 것으로 간주하지 않으며, 실제
  수정 전 현재 HEAD에서 개별 재검증해야 한다.

## 최신 요약

- 마지막 갱신: 2026-08-02 22:27:01 (Asia/Seoul)
- 최신 LUNA-0037 pending-card PDF delivery·student mutation invalidation 재대조(2026-08-02 22:27:01): pending flow는 QR batch issuance·PDF export·PC ACK·studentId 기반 delivered update를 하나의 ioExecutor runnable에서 수행하지만 studentMutationGate·delivery generation·QR revision을 보유하지 않는다. ACK는 request ID·PDF hash를 검증해 전송 무결성을 보강하지만 current student/card identity와 lifecycle cancellation은 결합하지 않아 기존 LUNA-0037 P3 후보·중간 신뢰도를 유지했다. 새 ID는 추가하지 않았다.
- 최신 LUNA-0036 학생 mutation·CSV·반 QR batch/PrintManager cross-operation 재대조(2026-08-02 22:23:17): batchQrButton은 studentMutationGate와 독립적으로 활성화되고, CSV fetch/apply도 import 버튼만 잠근 채 이름·자격정보·반 소속·needsPrint 상태를 ioExecutor에서 갱신한다. batch transaction 뒤 PrintManager adapter가 identity/revision 없이 대기하는 동안 후속 mutation이 stale·무효 카드를 만들 수 있는 후보 경계를 유지했다. onStop() 관리자 재잠금과 직접 인쇄 운영 보류 문서는 발생 기회를 낮추는 반대 근거로 기록했고, 심각도 P3·상태 후보·신뢰도 중간 및 기존 ID를 유지했다.
- 최신 LUNA-0035 Web recovery 단일 gate·관리자 mutation 재대조(2026-08-02 22:14:11): MainActivity의 webRecoveryGate는 수업 시작·재개·반 spinner·자가진단 일부에만 직접 연결되고, 반 구성·삭제·보강·batch QR·학생/CSV/QR mutation·빠른 반 선택 handler에는 공통으로 연결되지 않는다. ActivityResult callback도 pendingRecoveryAction을 현재 필드에서 읽어 action generation/revision 없이 후속 Start/End를 제출한다. StudentRepository의 transaction 불변조건은 invalid class/student/session 상태를 막지만 최신 관리자 의도 binding까지 보장하지 않아 기존 LUNA-0035 P3 후보·중간 신뢰도를 유지했고 새 ID는 추가하지 않았다.
- 최신 자동 재개 기준선(2026-08-02 22:14:11): 운영 계약 문서를 처음부터 EOF까지 읽은 뒤 누적 보고서 전체와 Git 기준선을 읽기 전용으로 복원했다. branch codex/fix-submit-recovery-timeout, HEAD/upstream 8c9a97ca55d34e3d93fdb02d9879ad929ed7133d, source scope는 불변이며 기존 사용자 변경·미추적 산출물을 보존했고 adb executable은 없다. 이번 반복은 LUNA-0035를 마쳤으며 다음 읽기 전용 대상은 LUNA-0036이다.
- 최신 `LUNA-0012` 원격 점검 배지·Kiosk header geometry 심화 대조(2026-08-02 19:44:47): 현행 controller는 `setContentView()` 뒤 `android.R.id.content`에 `Gravity.TOP|END`, 8dp inset, elevation 12dp의 badge를 마지막 child로 추가하고, 60dp `app_header`의 `device_mode_text`·`status_text`를 위한 영역을 예약하지 않는다. `status_text`는 `wrap_content`이며 긴 오류/상태 문자열도 사용한다. `RemoteSupportPolicyTest`와 Gate 5 manifest test는 만료·boot·receiver permission·버튼 도달성만 검사하고 overlap bounds는 검사하지 않는다. `f8bd710` 도입 뒤 Kiosk geometry 수정 history가 없어, 과거 승인 A 화면의 직접 겹침 증거와 현행 정적 근거가 일치하는 기존 `LUNA-0012` P3 확정을 유지·구체화했다. 현재 ADB는 사용할 수 없어 새 화면은 확인하지 않았다.
- 최신 `LUNA-0011` audit·진단 로그 lifecycle 심화 대조(2026-08-02 19:39:06): `KioskDatabase` builder는 migration만 등록하고 audit cleanup callback/maintenance를 연결하지 않으며, Android manifest와 data-extraction rules는 database·file 등 backup/device-transfer를 전면 제외한다. 두 앱의 ADB dump는 `readLines()`로 파일 전체를 읽은 뒤 최근 200줄만 출력하고, 기록 회전과 공통 동기화하지 않으며 I/O 예외를 `runCatching`으로 숨긴다. 따라서 backup 경계는 완화 근거지만 audit retention 미집행, 회전 실패 시 입력 read bound 부재, concurrent/incomplete dump·관찰성 공백은 남아 기존 `LUNA-0011` P4 후보를 유지·구체화했다.
- 최신 `LUNA-0010` PDF delivery 결과 확정 경계 재대조(2026-08-02 19:32:42): 단건 전송은 모든 `PcPdfSender.send()` 예외를 DHCP endpoint probe 후 새 request ID 전송으로 재시도하지만, 첫 receiver가 PDF commit 뒤 ACK만 잃은 경우 이를 구분하지 못한다. batch 전송은 endpoint recovery 없이 한 번만 전송하고 실패 시 이미 무효화된 QR을 재발급하는 재실행 경로로 간다. `markCardsDelivered()`는 `studentId`만 갱신하며 QR revision·PDF hash·delivery ID를 확인하지 않고, `onDestroy()`의 `shutdownNow()`도 실행 중 task를 취소하지 않는다. 기존 `LUNA-0010` P3 후보를 유지·구체화했고 `LUNA-0037`·`LUNA-0036`과의 중복은 분리했다.
- 최신 `LUNA-0008` replay cache 영속화·실패·재시작 경계 대조(2026-08-02 19:26:27): 정상 경로의 `os.replace`와 reload는 replay list를 보존하지만, PDF `os.replace`가 replay/config 저장보다 먼저 실행되어 process crash·power loss 구간에서 같은 timestamp-valid frame이 재수용될 수 있다. `ConfigStore.save()` 실패는 `ReceiverConfig` 메모리만 먼저 바꾸고 `config.tmp` 정리를 보장하지 않으며, control은 operation 검증 전에 ID를 영속화한다. load validation·save-failure·crash/restart·concurrent overflow 회귀가 없어 기존 `LUNA-0008` P4 후보를 유지·구체화했고 새 ID는 추가하지 않았다.
- 최신 `LUNA-0007` 인증 전 resource·runtime process 대조(2026-08-02 19:19:13): source의 per-connection daemon worker·per-recv timeout·full-frame allocation·unbounded event drain을 재확인했고, 현재 Windows read-only metadata에서 동일 receiver executable process 2개와 TCP 48129 listener 1개(`0.0.0.0`, 한 process owner)를 관찰했다. duplicate/stale process의 원인과 실제 resource exhaustion은 확인하지 않았으므로 기존 `LUNA-0007` P3 후보를 유지·구체화하고 process를 종료하거나 연결을 만들지 않았다.
- 최신 `LUNA-0002` PC secret at-rest·보존 경계 재대조(2026-08-02 19:14:52): 현재 Windows 메타데이터에서 `%LOCALAPPDATA%\MatholicPdfReceiver\config.json`은 존재하고 20,648바이트이며 broad group read ACL은 관찰되지 않았고 `config.tmp`는 없다. 그러나 source는 여전히 JSON Base64 저장·기본 상속 ACL·uninstall 시 설정 보존만 사용하며 DPAPI/Credential Manager·secret revoke/rotate·설정 폐기 절차가 없다. 환경 ACL은 반대 근거지만 같은 사용자 프로세스·프로필 백업/복제본 경계와 보존 후 재사용 위험을 해소하지 않으므로 기존 `LUNA-0002` P2 확정을 유지·구체화했다.
- 최신 `LUNA-0042` startup·tray·shutdown 심화 대조(2026-08-02 19:08:21): `current_lan_ipv4()`는 시작 시 한 번만 네트워크 주소를 probe하고 실패 시 즉시 종료할 뿐 backoff/retry/watchdog이 없으며, 설치된 per-user Startup shortcut도 `--background` 단일 실행만 예약한다. `pystray`는 daemon thread에서 실행되고 callback이 Tk `root.after()`를 직접 호출하며, active receiver handler를 join하는 종료 barrier도 없다. 2026-07-29 정상 재부팅 기록은 반대 근거지만 네트워크 준비 지연·tray callback/thread 실패·in-flight shutdown 시험은 없어 기존 `LUNA-0042` P4 후보를 유지·구체화했고 새 ID는 추가하지 않았다.
- 최신 `LUNA-0012` 원격 점검 배지·상단 UI 재대조(2026-08-02 19:44:47): Kiosk의 `RemoteSupportWindowController`는 활성 상태에서 `android.R.id.content` 최상위에 `Gravity.TOP|END`, 상단·우측 8dp와 elevation 12dp의 배지를 추가하고, 60dp Kiosk 헤더의 `status_text` 영역을 예약하지 않는다. `status_text`는 `wrap_content`이고 `CAMERA_PERMISSION_REQUIRED`, `INITIALIZATION_FAILED`, `MANUAL_STUDENT_SELECTION` 같은 긴 상태 문자열도 대입된다. 기존 승인 A 관리자 화면에서 직접 확인한 겹침은 현행 source·lifecycle·도입 history와 일치해 `P3 확정`을 유지했다. UI 시험은 정책·receiver permission·버튼 도달성만 검사하고 overlap bounds는 다루지 않는다. Web controller도 같은 우측 상단 오버레이를 사용하지만 Web 상단은 중앙 64dp 네비게이션·중앙 `status_badge`·좌측 `student_name_badge` 구조이고 현재 직접 겹침 증거가 없어 Kiosk finding으로 한정했다. ADB가 현재 PATH에 없어 새 화면 확인은 하지 않았다.
- 최신 `LUNA-0013` CSV parser memory 경계 재대조(2026-08-02 19:55:13): 현행 `StudentCsvParser`는 1MB/1,000행 제한에도 `String(payload, UTF_8)`, `parseRecords()`의 field `String`, `trim()`·`split()` 결과와 username `Set<String>`을 만들고, 반환되는 `CharArray`만 wipe한다. PC receiver `queue_csv()`의 `utf-8-sig` 입력 검증은 malformed input을 거부하는 반대 근거지만, Kiosk parser 이후 immutable copy에는 cleanup API가 없다. Kiosk transport `ByteArray`·control frame/plaintext는 정상 경로에서 덮어도 parser String을 지우지 못하며, `StudentCsvParserTest`·repository cleanup은 heap ownership을 검증하지 않는다. parser history는 `0193d89` 이후 수정이 없고 기존 `LUNA-0013` P3 확정을 유지했다. `LUNA-0031` queued row owner와 `LUNA-0032` partial-row 예외는 별도 경계로 재확인했다.
- 최신 `LUNA-0014` Kiosk PC 전송 crypto cleanup 재대조(2026-08-02 20:01:17): `PcControlProtocol.decodeResponse()`는 GCM 인증을 통과한 response의 request ID를 `try/finally` 바깥에서 비교하므로 mismatch 시 최대 1MiB control plaintext와 decoded request ID가 명시적으로 wipe되지 않는다. control/PDF encode와 `deriveKey()`·ACK HMAC도 `doFinal()` 성공 뒤에만 plaintext·key·중간 key 또는 ACK 정리를 수행한다. 정상 vector·tamper authentication test와 성공 경로 cleanup은 반대 근거지만 provider/allocation/request-ID mismatch fault seam은 없으므로 기존 `LUNA-0014` P3 확정을 유지·구체화했다.
- 최신 `LUNA-0015` 학생 비활성화·credential retention 재대조(2026-08-02 20:04:49): `StudentRepository.deactivateStudent()`는 QR hash 교체·`isActive=false`·timestamp·두 audit event만 transaction으로 반영하고 username/password ciphertext·IV·version은 그대로 둔다. `StudentDao`에는 delete/purge API가 없고 `StudentEntity`에는 `deactivatedAt`·retention field가 없으며, `findById()`는 inactive row도 반환하지만 복호화·credential update는 active guard로 차단한다. Room v3 migration은 QR status/admin pinLength만 다루고 credential purge는 없다. `KNOWN_LIMITATIONS.md:27`·`SECURITY.md:42`가 이 정책을 명시하며, 후속 file history(`4afdf0b`, `9af507b`)에도 deactivation/credential field purge 변경은 없어 `LUNA-0015` P3 `기존 알려진 문제`를 유지했다.
- 최신 자동 재개 기준선(2026-08-02 16:43:14): 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`와 source scope를 보존했다. `adb` executable은 현재 PATH에 없어 A 상태를 확인하지 못했다. 이번 반복의 검토 대상은 `LUNA-0016`이다.
- 최신 `LUNA-0016` 네트워크 대기 차폐 재대조(2026-08-02 20:07:58): `updateNetworkPause()`는 ACTIVE WebView를 유지한 채 `network_pause_panel`의 visibility만 토글하고, 마지막 full-screen child의 배경 `#F2102A43`는 알파 `F2`(242/255)라 완전 불투명하지 않으며 중앙 LinearLayout에도 별도 background가 없다. 제품 결정은 답안 보존·입력 차단·무재로딩 재개를 요구하지만 현재 source·layout·시험에는 답안 비노출을 보장하는 opaque 차폐·렌더링 검증이 없다. blame상 관련 줄은 `d8ca06f` 도입 commit에 남아 있고 후속 차폐 보강은 없으며, field checklist의 통신 단절 항목도 미체크다. `FLAG_SECURE`·clickable/focusable overlay·기존 WebView 유지와 historical W05는 반대 근거지만 실제 화면 가독성을 증명하지 않아 `LUNA-0016` P3 후보·중간 신뢰도를 유지한다.
- 최신 `LUNA-0017` 네트워크 pause 입력 생명주기 재대조(2026-08-02 20:12:40): `updateNetworkPause()`는 여전히 panel visibility·idle timer만 바꾸고 `clearFocus()`·IME hide·WebView disable·입력 대상 전환을 하지 않는다. Activity의 `onKeyDown()`·`onKeyUp()`은 BACK만 소비하고 다른 key를 `super`로 전달하며, `dispatchTouchEvent()`도 pause 여부와 무관하게 마지막에 `super`를 호출한다. WebDomScripts의 수식 키패드는 `document.activeElement` 또는 visible MathQuill editor를 찾아 `write`·`keystroke`를 실행하고 `field.focus()`를 다시 호출한다. panel의 `focusable/clickable` 선언과 WebView의 `filterTouchesWhenObscured`는 반대 근거지만 IME·hardware key·DOM 입력 생명주기 폐쇄를 증명하지 않는다. 네트워크 pause 전용 입력 시험과 실제 IME·키 라우팅은 실행하지 않아 `LUNA-0017` P3 후보·중간 신뢰도를 유지하고 새 ID는 추가하지 않았다.
- 최신 자동 재개 기준선(2026-08-02 16:53:13): 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`와 source scope를 보존했다. `adb` executable은 현재 PATH에 없어 A 상태를 확인하지 못했다. 이번 반복의 검토 대상은 `LUNA-0018`이다.
- 최신 `LUNA-0018` network monitor fallback 재대조(2026-08-02 20:18:56): `registerNetworkMonitor()`는 `ConnectivityManager` null이면 진단 없이 반환하고, `registerDefaultNetworkCallback()` 예외는 `NETWORK_MONITOR_FAILED` private event만 남긴다. 실패 후 재등록·bounded retry·주기 validated check·watchdog은 없지만, ACTIVE state transition과 main-frame WebView error는 독립적으로 `updateNetworkPause()`를 호출한다. 따라서 등록 실패가 항상 즉시 상태 전환 실패를 뜻하지는 않으나, callback이 없는 유휴 ACTIVE에서 조용히 네트워크가 끊기면 pause 전환이 지연될 수 있다. 과거 W05/G306 PASS는 `d8ca06f` 이전 `eb09567` baseline의 Wi-Fi 단절·LOCKED 흐름으로 현재 callback failure를 검증한 증거가 아니다. `LUNA-0018` P4 후보·중간 신뢰도를 유지하고 새 ID는 추가하지 않았다.
- 최신 `LUNA-0019` 네트워크 대기 패널 접근성 subtree 경계 재대조(2026-08-02 20:24:36): WebView는 `importantForAutofill`만 선언하고 `importantForAccessibility`·`NO_HIDE_DESCENDANTS`·delegate·screen-reader focus 제어가 없으며, pause panel도 full-screen `clickable/focusable`과 안내 TextView만 선언한다. `showActive()`·`transition()`·`updateNetworkPause()`는 WebView/panel visibility와 timer만 전환한다. Web POC 접근성 symbol/test 검색은 무매치였고, `isAccessible = true`는 Android 접근성 검사가 아니라 private reflection 접근 플래그였다. Gate 1 Probe는 `com.matholic.mathapp` 별도 package만 대상으로 하며 Device Owner 점검은 접근성 서비스 전체 비활성화를 증명하지 않는다. 실제 accessibility tree/action을 실행하지 않아 `LUNA-0019` P3 후보·중간 신뢰도를 유지하고 새 ID는 추가하지 않았다.
- 최신 `LUNA-0020` 원격 지원 중 민감 화면 `FLAG_SECURE` 경계 심화 대조(2026-08-02 20:30:44): Kiosk/Web 양쪽 `RemoteSupportWindowController`는 local store의 유효한 만료 시각만 보고 Activity window의 `FLAG_SECURE`를 해제하고, screen/focus/state가 PIN·Web credential/setup 화면인지 판정하지 않는다. Kiosk `showAuthentication()`·`onStart()` relock·`onStop()`에는 controller stop 또는 secure flag 복원이 없고, Web `showSetup()`·`configureSensitiveInputs()`도 입력 보호 속성만 설정한다. controller stop은 Kiosk/Web 모두 Activity destroy 또는 명시적 종료 경로에 한정되며, 만료 timer도 화면 경계를 추가하지 않는다. 관리자 인증·signature/DUMP receiver·30분 기본/최대 2시간·배지·만료/명시 종료 복원과 `SECURITY.md`의 “PIN·비밀번호 중 사용 금지”는 반대 근거지만, active remote support + 민감 화면 matrix 회귀시험은 없어 `LUNA-0020` P3 후보·중간 신뢰도를 유지한다. 실제 PIN·자격정보·ADB 캡처·원격 지원 활성화는 사용하지 않았다.
- 최신 `LUNA-0021` 원격 지원 양 앱 저장·broadcast 상태 동기화 심화 대조(2026-08-02 20:35:44): Kiosk/Web `RemoteSupportStore.enable()`·`disable()`은 `SharedPreferences.Editor.commit()`의 boolean 결과를 버리고 만료 시각을 성공처럼 반환하거나 `Unit`으로 끝난다. Kiosk `notifyWebRemoteSupport()`는 `sendBroadcast()` 호출이 예외 없이 반환된 것만 전달 성공으로 보고, Web receiver는 저장 결과·acknowledgement를 반환하지 않는다. `scripts\remote-tablet.ps1`도 Kiosk→Web 두 receiver를 순서대로 호출해 `result=0`만 확인하고 부분 실패 rollback·상태 readback·Start 캡처 실패 후 자동 Stop을 제공하지 않는다. 정상 permission·same-boot·expiry·controller listener와 RC107의 5분 재개 후 즉시 비활성화 기록은 반대 근거지만, commit/receiver/두 앱 상태가 어긋나면 `FLAG_SECURE`·WebView debugging·badge가 서로 다른 상태가 될 수 있어 기존 `LUNA-0021` P4 후보·중간 신뢰도를 유지한다. 실제 상태 변경·ADB·fault injection은 수행하지 않았다.
- 최신 `LUNA-0022` Lock Task 진입 결과·QR/인증·overlay restriction 심화 대조(2026-08-02 20:40:22): `KioskLockTaskController.enterRestrictedMode()`는 `startLockTask()`가 예외 없이 끝난 뒤 mode가 `LOCKED`가 아니면 `Result.success(false)`를 반환하지만 `DISALLOW_CREATE_WINDOWS`를 정리하지 않는다. `MainActivity.enterDedicatedMode()`는 `isFailure`만 전파하고 false 결과·현재 mode를 검사하지 않으며, `SessionPreflightPolicy`도 `NONE/PINNED` mode를 입력·차단하지 않는다. `verify-gate5-device-owner.ps1`는 `mLockTaskModeState`가 읽히기만 하면 `NONE`·`PINNED`도 성공 출력할 수 있고, provisioning script의 `LOCKED` exact check와 불일치한다. 정상 Device Owner/allowlist·예외 cleanup·최근 A의 `LOCKED` 기록은 반대 근거지만 false-return fault와 verifier 오판은 검증되지 않아 `LUNA-0022` P3 후보·중간 신뢰도를 유지한다. 실제 Lock Task 상태 변경·ADB·fault injection은 수행하지 않았다.
- 최신 `LUNA-0023` QR renderer 임시 표현·Bitmap 소유권 심화 재대조(2026-08-02 20:46:05): `QrImageRenderer.kt:11-34`는 `sizePixels >= 256`만 확인하고 ZXing `BitMatrix`와 `IntArray(sizePixels * sizePixels)`를 만든 뒤 ARGB_8888 Bitmap으로 복사하지만, matrix·pixels에 `finally`/clear/fill 또는 상한 계약이 없다. production 호출은 `QR_SIZE_PIXELS=720`으로 고정되어 `IntArray`만 518,400개·약 2,073,600바이트이며, 반환 Bitmap은 preview·직접 인쇄·단건 PDF·PC 전송·batch PDF/인쇄 경로에서 `eraseColor`·`recycle`·`SensitiveTask`로 정리된다. `SECURITY.md`의 화면 원문·bitmap 삭제, `0fc357c`/`bfbfaf4`의 pre-export·queued task 회귀시험과 `QrPrintDocumentAdapterInstrumentedTest`의 `onFinish()` 검증은 반환 Bitmap 소유권의 반대 근거이지 renderer 내부 배열의 zeroize 증거가 아니다. renderer 도입 `e093bd0` 이후 renderer 자체 변경·직접 renderer 시험은 확인되지 않았고, fault·OOM·heap 잔류는 실행하지 않아 `LUNA-0023` P3 후보·중간 신뢰도를 유지한다.
- 최신 `LUNA-0024` 관리자 실행취소 lifetime·CSV 교차 작업 invalidation 심화 대조(2026-08-02 20:54:00): `adminUndoGeneration`은 30초 timer와 현재 슬롯 교체만 보호하고, `showAuthentication()`·`showAdmin()`·`onStop()`·CSV·QR 재발급·자격정보 변경·비활성화·학생 등록·반 생성 성공 경로에는 `clearPendingAdminUndo()` 호출이 없다. CSV import는 기존 학생의 이름과 반 소속을 transaction으로 갱신하지만 이전 `RestoreStudentName`/`RestoreMemberships` action을 무효화하지 않으며, `undoAdminButton`은 student mutation gate와 독립적으로 활성화된다. `updateStudentProfile()`·`replaceClassMemberships()`에는 expected-current revision 비교가 없어 CSV 직후 이전 undo가 다시 실행될 정적 순서가 성립한다. 30초 만료·명시적 클릭·isolated field 현장 PASS와 일부 repository guard는 반대 근거지만 cross-operation 시험은 없어 `LUNA-0024` P4 후보·중간 신뢰도를 유지한다.
- 최신 `LUNA-0025` 활성 수업 membership·pending undo·pre-refresh UI 경계 재대조(2026-08-02 21:03:08): `completeSessionStart()`는 `currentSession`을 설정한 뒤 `showScanner()`로 이동하지만 admin control을 즉시 fail-closed 갱신하지 않고, 세션 PIN 재진입의 `showAdmin()`도 panel을 먼저 다시 보인 뒤 비동기 `refreshAdminData()`에 의존한다. 따라서 refresh snapshot 전에는 이전에 enabled였던 반 구성 버튼이 남을 수 있고, `showClassMembershipDialog()`와 `replaceClassMemberships()`에는 active session guard가 없다. 수업 시작 전에 남은 `RestoreMemberships`도 같은 guard 없이 실행되며, `StudentDao` QR·수동 선택 query는 현재 `class_memberships` 또는 `session_students`를 live join한다. 정상 snapshot 뒤 UI 잠금·eligibility 시험·일부 repository guard는 반대 근거지만 active-session undo/재진입·즉시 membership 순서 시험은 없어 `LUNA-0025` P3 후보·중간 신뢰도를 유지한다.
- 최신 `LUNA-0026` 수업 종료 후 snapshot refresh 실패·중복 종료/QR 재진입 경계 재대조(2026-08-02 21:09:18): `endSession()`은 transaction 안에서 temporary row를 지우고 `ADMIN_IDLE` singleton을 저장하지만, `completeSessionEnd()` 성공 callback은 `webRecoveryGate.finish()` 뒤에도 `currentSession`을 null로 바꾸지 않은 채 `refreshAdminData()`에 의존한다. snapshot read가 실패하면 stale active `currentSession`으로 `startSessionButton`·resume/recover·class controls를 다시 그리며, 이전 상태가 `QR_READY`면 memory-only `showScanner()`가 열리고 DB idle의 `SESSION_NOT_READY` 거부 뒤 QR 대기 loop가 이어질 수 있다. stale 종료 버튼은 다음 `endSession()`을 다시 제출해 이미 종료된 DB에서 실패할 수 있다. RC18의 일반 refresh failure 보존 정책·atomic end transaction·정상 retry 안내는 반대 근거지만 post-commit failure·중복 종료·stale scanner 시험은 없어 `LUNA-0026` P4 후보·중간 신뢰도를 유지한다.
- 최신 자동 재개 기준선(2026-08-02 16:57:39): 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`와 source scope를 보존했다. `adb` executable은 현재 PATH에 없어 A 상태를 확인하지 못했다. 이번 반복의 검토 대상은 `LUNA-0019`이다.
- 최신 자동 재개 기준선(2026-08-02 16:49:27): 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`와 source scope를 보존했다. `adb` executable은 현재 PATH에 없어 A 상태를 확인하지 못했다. 이번 반복의 검토 대상은 `LUNA-0017`이다.
- 최신 관리자 PIN verifier ownership 재대조: `AdminDao.get()`은 `isEnrolled()`·`enrolledPinLength()`에도 `salt`·`derivedKey`를 포함한 전체 row를 반환하고, 인증은 그 배열을 `PinVerifier`가 alias하며 `AdminPin.verify()`는 candidate key만 지운다. `enroll()`의 저장 성공·예외와 Room binding 뒤 verifier 배열 cleanup도 별도 계약이 없으므로 기존 `LUNA-0028` P4 후보를 보강했다. `AdminPinTest`·`SensitiveTaskTest`·MainActivity/migration 시험은 이 repository/Room 배열 ownership을 검증하지 않는다.
- 최신 PC receiver startup/tray/background 대조(2026-08-02 15:39:55): `ReceiverApplication.__init__()`가 `ThreadedReceiverServer`를 Tk·tray보다 먼저 생성·bind하고, `main()`은 `OSError`·`RuntimeError`·`ValueError`만 처리하며 부분 server rollback이나 `mainloop()` 반환 후 공통 shutdown finally가 없다. `--background`는 숨겨진 Tk와 저장되지 않은 tray daemon thread의 종료 callback에 의존하고 thread 예외·생존을 감시하지 않는다. 2026-07-29 정상 재부팅 자동 시작과 0.1.3 pytest/smoke 기록은 정상 경로의 반증으로 보존했지만, 단계별 예외·tray join·server health 시험 공백 때문에 기존 `LUNA-0042` P4 후보를 유지·보강했다.
- 최신 PC receiver 인증 전 resource·event queue 대조(2026-08-02 15:44:08): `_ReceiverHandler`는 연결마다 daemon worker를 만들고 10초 inactivity socket timeout 아래 누적 read deadline·active worker 상한 없이 header/body를 끝까지 읽는다. protocol의 PDF 5MiB·control 1MiB payload bound는 연결당 메모리만 제한하며, `app.py`의 무제한 `queue.Queue`와 무제한 drain loop에는 producer rate/coalescing/UI budget이 없다. inherited TCP backlog가 source에서 명시되지 않는 점과 firewall·payload size/authentication은 반대 근거로 기록했지만, 느린 partial connection·worker 경쟁·event queue 누적 시험 공백 때문에 기존 `LUNA-0007` P3 후보를 유지·보강했다.
- 최신 `LUNA-0008` replay cache·timestamp 대조(2026-08-02 15:50:29): receiver는 timestamp 300초 창과 별개로 최근 request ID 2,048개만 count-based로 보관하며, 초당 약 6.83개 이상의 신규 유효 frame이 이어지면 창 안의 오래된 captured authenticated frame이 eviction 뒤 PDF/control에서 재수용될 조건이 생긴다. `ConfigStore.save()`의 atomic replace·정상 재시작 시 replay list 보존, `ReceiverState.lock`, Kiosk의 호출별 random request ID·strict response binding은 반대 근거지만 실제 처리량·저장 지연·대량 재생은 미검증이다. `accept_control()`이 operation-specific 검증 전에 ID를 기록·저장해 인증된 unsupported/control failure가 cache를 churn시킬 수 있는 세부 경계도 확인했으며, 기존 `LUNA-0008` P4 후보를 유지·보강했다.
- 최신 `LUNA-0009` Windows tray 개인정보 경계 대조(2026-08-02 15:57:31): 단건 Kiosk PDF filename은 `preview.exactName`을 포함하고 receiver의 PDF 성공 event가 `destination.name 저장 완료`를 `notify=True`로 전달하므로, app의 `event.state or event.message` 경로가 학생 이름 포함 toast payload를 만든다. status event는 유효한 `state`만 tray로 보내 이름을 숨기고, CSV/error event는 현재 `notify=False`다. 제품 결정은 지정 PC 상태판에 이름을 암호화 전송하되 Windows 알림에는 표시하지 않도록 확정했으며, 관련 code는 그 결정 뒤에도 filename 재사용을 유지한다. 기존 `LUNA-0009` P3 확정 finding을 유지·보강했다.
- 최신 `LUNA-0010` PDF commit·ACK·retry·delivered 경계 대조(2026-08-02 16:03:22): receiver는 PDF commit·replay/config 저장 뒤 ACK를 보내고, Kiosk의 `withReachablePairedPc()`는 어떤 PDF send 예외든 인증된 새 endpoint를 찾으면 동일 send operation을 새 request ID로 재실행한다. 단건 실패 callback은 같은 전송 버튼을 다시 활성화하고, batch는 ACK 후 `markCardsDelivered()` 실패 시 재실행에서 새 QR을 발급한다. `studentId`만 사용하는 delivered update·stable delivery id/hash 부재와 ACK 유실/DB·config failure 시험 공백 때문에 기존 `LUNA-0010` P3 후보를 유지·보강했다. 정상 strict ACK·same-frame replay 거부·single executor·인증된 endpoint probe는 반대 근거로 기록했다.
- 최신 `LUNA-0011` audit·진단 로그 보존 경계 대조(2026-08-02 19:39:06): `AuditEventEntity`에는 행 수·기간 상한이 없고 `AuditDao.deleteOlderThan()`는 production 호출이 없으며, Kiosk `KioskDatabase` builder에도 Room callback/maintenance가 없고 `MainActivity.onCreate()`의 cleanup은 QR PDF cache에만 적용된다. Kiosk/Web `PrivateDiagnosticLog`는 각각 현재·이전 256KB 파일 회전과 ADB nonce/line filter를 갖지만 기간 cleanup은 없다. 두 dump는 파일 전체를 `readLines()`한 뒤 최근 200줄만 취하고 기록 회전과 공통 동기화하지 않으며 I/O 예외를 숨긴다. 정상 경로의 private storage·backup/data-transfer 전면 제외·`DUMP` receiver 보호·민감정보 없는 구조적 event는 반대 근거로 남겼고, 회전 `delete()`/`renameTo()` 결과 무시와 dump 입력 bound/관찰성 공백을 추가 확인해 기존 `LUNA-0011` P4 후보를 유지·구체화했다.
- 최신 PC CSV buffer·queue durability 대조(2026-08-02 15:34:50): `choose_csv()`·`queue_csv()`·`clear_csv()`·`shutdown()`은 immutable CSV `bytes`의 reference-only lifecycle을 보이고, control response encoder는 plaintext·ciphertext·header/frame 파생 buffer를 명시적으로 wipe하지 않는다. 1 MiB/UTF-8 검증·lock·Kiosk 측 정상 cleanup·정상 인증 시험은 반대 근거지만 lifecycle zeroization을 증명하지 않는다. `CONTROL_FETCH_CSV`의 sendall 전 queue 소비·재시작/재시도 복구 부재는 `LUNA-0003`, Kiosk parsed row executor 경계는 `LUNA-0031`로 분리하고 `LUNA-0041` P4 후보를 유지·보강했다.
- 최신 `LUNA-0036` cross-operation 재대조(2026-08-02 18:04:42): batch는 `batchQrButton`만 false로 만들고 `studentMutationGate`, 반 구성·삭제·class spinner·quick class 선택은 잠그지 않는다. `BatchQrCard`가 학생·반·QR revision 없이 표시명·bitmap만 보유하고, batch DB transaction 이후 비동기 `PrintManager` 소비를 mutation revision·최신 roster·QR 유효성과 연결하는 취소/재검증 경계도 없다. 단일 `ioExecutor`는 DB 순서만 직렬화하므로 비활성화·이름 변경·소속 변경 뒤 stale·무효 카드가 출력될 수 있는 기존 P3 후보를 구체화·유지했다.
- 최신 `LUNA-0040` 서버 임시답안 cleanup/result binding 재대조(2026-08-02 18:15:02): Web persisted `IDLE`은 서버 답안 상태의 증명이 아니지만 `beginAdminRecovery()`는 이를 근거로 login/cleanup/answer verification 없이 `RESULT_OK`를 반환한다. 비-IDLE logout도 local WebView fingerprint만 검증하고 동일 과제 재진입·지연 복원을 관찰하지 않는다. 기존 P3 후보를 유지·보강했고 새 ID는 추가하지 않았다.
- 최신 `LUNA-0037` pending-card PDF 재대조: QR issuance→PDF export→PC ACK→`markCardsDelivered()`와 알려진 student/class mutation은 같은 단일 `ioExecutor` runnable 경계라 직접 interleave는 입증되지 않았다. PC receiver도 정상 경로에서 파일 commit·replay 기록 뒤 ACK를 보내는 반대 근거가 있다. 다만 pending flow는 공통 mutation gate/revision을 소유하지 않고, 실행 중 task에 명시적 cancellation/socket close/destroyed check가 없으며, delivered 기록은 student ID만 사용해 issued QR revision/request ID와 묶이지 않는다. PC artifact의 전송 후 폐기·재생성 정책과 pending callback 회귀시험도 없어 기존 P3 후보를 유지·보강했다.
- 최신 `LUNA-0038` Web SPA 상태 대조: `window.__matholicKioskProblemStates`는 문항 번호만 key로 사용하고 SPA `/workbook`↔`/diagnostic` 클릭·React 전환·Kotlin `activeExperienceGeneration` 변경에도 reset/task key가 없다. 단일 fixture 시험은 통과하지만 두 과제 교차 현황·`다음 미입력`은 미검증이며, full reload가 항상 일어난다는 근거도 없어 기존 P3 후보를 유지했다.
- 최신 `LUNA-0035` Web recovery/admin gate 재대조: `webRecoveryGate`는 start/resume/class spinner/self-test 일부만 잠그고 반 구성·삭제·보강·batch QR·학생/CSV/QR mutation과 quick class button은 계속 실행 가능하며, `PendingRecoveryAction.StartSession`은 캡처한 class/temporary IDs를 mutation revision 재검증 없이 후속 `startSession()`에 전달한다. 기존 P3 후보를 유지·보강했고 새 ID는 추가하지 않았다.
- 최신 `LUNA-0027` Activity 재생성/action persistence 심화 대조(2026-08-02 21:17:06): `pendingRecoveryAction`은 일반 Activity 메모리 필드이고 `onSaveInstanceState`·DB 복원·action token 경계가 없으며, 새 인스턴스의 result callback이 `None`을 소비하면 Web recovery 성공 뒤 Kiosk `startSession()`/`endSession()` 후속 전이가 생략될 수 있다. Kiosk manifest의 `configChanges=keyboardHidden|orientation|screenSize`·`singleTask`는 일반 구성 변경의 재생성을 줄이는 반대 근거지만 process reclaim·기타 Activity 재생성에는 저장 action 경로가 없다. Web POC는 `onCreate(null)`·SharedPreferences state·configuration-change 예외 처리를 갖지만 이는 Web 자체 상태에만 해당하고 Kiosk action identity를 전달하지 않는다. 기존 P4 후보를 유지하며 새 ID는 추가하지 않았다.
- 최신 `LUNA-0024` 실행취소 lifetime 재대조(2026-08-02 20:54:00): 세 reversible 성공 경로는 새 action으로 이전 슬롯을 교체하지만 non-reversible CSV·QR·credential·deactivation·student/class 생성/변경 뒤 stale action을 지우지 않는다. 단일 `ioExecutor`는 작업을 순서대로 실행할 뿐 action의 최신 DB 상태·mutation generation을 검증하지 않으며, 화면 lifecycle도 pending action을 폐기하지 않는다. 이번 회차는 CSV가 같은 이름·membership 필드를 직접 바꾸고 이후 stale undo가 이를 되돌릴 수 있는 구체적 경계를 확인했다.
- 최신 `LUNA-0025` 활성 session membership 실행취소 재대조: 일반 반 구성 버튼·반 선택은 active session에서 잠기지만 `undoAdminButton`·`performPendingAdminUndo()`에는 동일한 session gate가 없다. `startSession()`은 반 membership snapshot을 `session_students`에 복사하지 않고 temporary 학생만 기록하며, QR·수동 선택 query는 현재 `class_memberships OR session_students`를 읽는다. 기존 P3 후보를 유지·보강했고, 다른 반 action의 DB mutation과 현재 session eligibility 영향은 구분했다.
- 최신 `LUNA-0026` session 종료 후 관리자 snapshot refresh 재대조: `endSession()`은 Room transaction으로 `ADMIN_IDLE`을 저장하지만 `completeSessionEnd()` 성공 callback은 Activity의 `currentSession`을 먼저 null로 만들지 않고 후속 `refreshAdminData()`에 의존한다. 해당 snapshot read가 실패하면 기존 active UI를 보존하는 일반 복구 정책이 실제 종료된 DB와 일시적으로 어긋날 수 있어 기존 P4 후보를 보강했다.
- 최신 `LUNA-0028` PIN verifier ownership 심화 대조(2026-08-02 21:24:49): 초기 bootstrap의 `isEnrolled()`·`enrolledPinLength()`와 `enroll()`의 이미 등록됨 검사, lockout 중 `authenticate()`가 모두 `SELECT *` full entity를 materialize한다. `SensitiveTask`·`AdminPin.verify()`는 입력 PIN·candidate key만 정리하고, repository가 Room entity와 alias한 `PinVerifier`·등록용 local verifier의 `salt`·`derivedKey` ownership/clear 계약은 없다. DB backup/device-transfer 전면 제외와 정상 PBKDF2/입력 정리는 반대 근거로 반영했지만 migration·UI/PIN 시험은 Room transient ownership을 관찰하지 않아 기존 P4 후보를 유지·보강했다.
- 최신 `LUNA-0029` loopback proxy resource/health lifecycle 심화 대조(2026-08-02 21:30:55): 성공한 CONNECT tunnel은 client/upstream `soTimeout=0`으로 peer 종료까지 유지되고 `newCachedThreadPool()`·무상한 socket registry를 사용한다. 추가로 `acceptLoop()`의 `IOException` 종료는 coordinator에 실패를 알리지 않고, process-wide coordinator는 `READY`를 유지해 Web recovery `Activity.recreate()` 뒤에도 dead proxy를 재시작하지 않는다. `CloseableRegistry`의 종료/등록 직렬화, rejection cleanup, bootstrap 실패·timeout 시 proxy close와 host/TLS 제한은 반대 근거로 유지하며 기존 P4 후보를 보강했다.
- 최신 `LUNA-0030` PC pairing secret String lifecycle 재대조(2026-08-02 21:37:22): ML Kit `Barcode.rawValue`가 UI runnable·`ioExecutor` pairing lambda·`PcPairingStore.save()`를 거치고, recovery `encode()`·`load()`도 immutable pairing String을 새로 만든다. `withHost()`의 `receiverId`·`secret` `copyOf()`, decode/save 실패 시 object ByteArray cleanup, 저장소의 plaintext/ciphertext/IV cleanup은 별도 alias·배열 cleanup 결함을 낮추는 반대 근거다. 그러나 Activity 종료 시 일반 pairing lambda를 회수·zeroize하는 경계와 transient String ownership 시험은 없으므로 기존 P3 후보를 유지·보강했고 실제 secret 노출이나 at-rest 암호화 실패로 확대하지 않았다.
- 최신 `LUNA-0031` CSV parsed credential lifecycle 재대조(2026-08-02 21:43:55): `ioExecutor`·`pcControlExecutor`가 모두 단일 스레드이고, PC fetch 뒤 preview 제출은 비동기 바깥 `runCatching`이 내부 `ioExecutor` rejection을 받지 못한다. apply는 `applying=true`로 Dialog ownership을 넘긴 뒤 일반 lambda를 제출해 rejection·queued shutdown 시 `onDismiss` cleanup도 우회한다. repository 정상 본문·preview failure/cancel은 cleanup하지만 precondition/DB 조회 전 예외와 Activity lifecycle 경계의 owner는 별도 보장되지 않아 기존 P3 후보를 유지·보강했고 parser partial-row는 `LUNA-0032`로 분리했다.
- 최신 `LUNA-0032` CSV parser partial-row lifecycle 재대조(2026-08-02 21:50:07): parser는 전체 `parseRecords()`·quote 검증을 끝낸 뒤 `mapIndexed`에서 `StudentCsvRow`를 만들고, 각 row의 validation·duplicate 검사 뒤에만 credential `CharArray`를 할당한다. 따라서 후속 row 오류·중복 시 앞선 row만 반환 전 owner를 잃을 수 있고, unclosed quote 자체는 partial row를 만들지 않는 반대 근거가 있다. `MainActivity` parse catch/finally는 payload만 지우며 duplicate 시험은 예외만 확인하므로 기존 P3 후보를 유지·보강했고 `LUNA-0013` immutable String·`LUNA-0031` post-return queue 경계와 분리했다.
- 최신 자동 재개 기준선(2026-08-02 15:00:59): 계약 문서와 누적 보고서를 처음부터 EOF까지 읽고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음, 승인된 ADB `R54TB029FHZ`/`SM-P610`·Kiosk `0.6.0-rc55`/code 60·Web POC `0.4.0-rc117`/code 134·Device Owner·전경 Kiosk `MainActivity`를 읽기 전용으로 확인했다. 이번 사이클의 다음 대상은 `LUNA-0033`이다.
- 최신 `LUNA-0033` Kiosk PC status queue 재대조: `reportPcStatus()`의 각 상태 lambda가 무제한 single-thread executor에 들어가고 CSV fetch·자가진단과 같은 worker를 공유한다. PC 실패 시 원래 connect/read timeout 뒤 최대 6초 resolver와 재시도 경로가 한 worker를 점유할 수 있으며, `activeStudentDisplayName`은 성공 후에도 이미 제출된 lambda가 보유하고 failure·`onStop()`·`onDestroy()`에는 공통 clear가 없다. Kiosk 시험에는 status/CSV/self-test queue 계측 연결이 없고 운영 문서는 정상 상태·자가진단만 기록하므로 기존 P3 후보를 유지·보강했다. resolver candidate cleanup·정상 PC 경로는 반대 근거로 남겼고 새 ID는 추가하지 않았다.
- 최신 자동 재개 기준선(2026-08-02 15:08:57): 계약 문서와 누적 보고서를 처음부터 EOF까지 읽고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음, 승인된 ADB `R54TB029FHZ`·`SM-P610` Android 13·Kiosk `0.6.0-rc55`/code 60·Web POC `0.4.0-rc117`/code 134·Device Owner·전경 Kiosk `MainActivity`를 읽기 전용으로 확인했다. 이번 반복의 검토 대상은 `LUNA-0034`이다.
- 최신 자동 재개 기준선(2026-08-02 15:15:15): 계약 문서와 누적 보고서를 처음부터 EOF까지 읽고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음, 승인된 ADB `R54TB029FHZ`·`SM-P610` Android 13·Kiosk `0.6.0-rc55`/code 60·Web POC `0.4.0-rc117`/code 134·Device Owner·전경 Kiosk `MainActivity`를 읽기 전용으로 확인했다. 이번 반복의 검토 대상은 `LUNA-0036`이다.
- 최신 자동 재개 기준선(2026-08-02 15:39:55): 계약 문서와 누적 보고서를 처음부터 EOF까지 읽고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물과 `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope 무변경을 확인했다. 현재 PowerShell PATH에는 `adb`가 없어 A 상태는 이번 사이클에 재확인하지 못했고 환경 변경은 하지 않았다. 이전에 기록된 A 기준선은 historical evidence로만 유지한다.
- 최신 자동 재개 기준선(2026-08-02 16:13:03): 계약 문서와 누적 보고서를 처음부터 EOF까지 읽고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물과 `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope 무변경을 확인했다. 현재 PowerShell PATH에는 `adb`가 없어 A 상태는 이번 사이클에 재확인하지 못했고 환경 변경은 하지 않았다. 이번 사이클은 `LUNA-0011` 재대조와 보고서 갱신으로 마쳤다.
- 다음 검토 영역: `LUNA-0033` Kiosk PC status queue와 CSV/self-test worker 공정성·표시명 lifecycle을 읽기 전용으로 다시 대조한다.
- 최신 자동 재개 기준선(2026-08-02 22:04:57): 계약 문서와 누적 보고서를 처음부터 끝까지 다시 읽고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 `diagnostics/`, `output/`, `tmp/`와 source scope 변경 없음, ADB executable 부재를 읽기 전용으로 확인했다. 이번 반복의 검토 대상은 `LUNA-0034`이며 Kiosk/Python pairing decode·저장·QR 생성·직접 endpoint routing, DHCP recovery, protocol identity binding, 시험·운영 문서와 history를 심화 대조했다. 다음 정적 검토 대상은 `LUNA-0035`이다.
- 최신 Kiosk Room at-rest·백업·migration 경계 재대조: `KioskDatabase`는 Room 3에서 명시적 `MIGRATION_1_2`·`MIGRATION_2_3`만 등록하고 destructive fallback은 사용하지 않는다. `AndroidManifest.xml`의 backup/data-transfer 차단과 `data_extraction_rules.xml`의 root/file/database/sharedpref/external 제외가 `GATE4_IMPLEMENTATION.md`·`SECURITY.md`의 “자격정보 필드만 Keystore AES-GCM, Room 파일 전체 SQLCipher 아님” 계약과 일치한다. 스키마상 평문인 표시명·내부 UUID·반 관계·QR token hash·비민감 audit event는 문서가 의도한 앱 private DB 경계이며 새 at-rest finding은 추가하지 않았다. 2→3의 `pinLength=0`은 기존 PIN의 첫 인증을 수동 버튼으로 유도한 뒤 성공 시 길이를 재저장하는 호환 경로로 보이고, migration 시험은 default 보존만 확인해 UI bootstrap은 미검증으로 남겼다. 실제 DB·복원·backup/transfer·migration fault·PIN은 사용하지 않았다.
- 최신 `LUNA-0040` Web 종료·공식 답안 cleanup 경계 재대조: `finishButton`은 portal 로그아웃 click 뒤 login DOM sanitizer와 WebView history/form/cache/SSL/WebStorage/cookie 정리를 거쳐 `RESULT_OK`를 반환하지만, sanitizer·Kiosk `WebSessionResultPersistence`에는 공식 답안 값·과제 key·동일 과제 재진입·지연 복원 결과가 없다. RC117 운영 정정은 화면 삭제만으로 완료를 판단할 수 없다는 실제 반증을 보강하지만, 현재 source에서 서버 임시답안 cleanup 실패를 직접 확정하지는 못한다. 기존 `LUNA-0040` P3 후보를 유지하며 새 ID는 추가하지 않았다.
- 최신 관리자 PIN lockout 시간 기준 재대조: `AdminAuthRepository.authenticate()`는 `System.currentTimeMillis()` 기반 `lockedUntilEpochMs`를 저장·비교하고 `elapsedRealtime()`·시각 변경 감시·재부팅 후 monotonic 보정은 사용하지 않는다. `MainActivity`의 일반 인증과 수업 관리자 대화상자는 공통 단일 `ioExecutor`와 `authBusy`/`submitting` guard로 제출을 직렬화하지만, `AdminPinTest`는 지연 공식만 검사한다. `docs/KNOWN_LIMITATIONS.md`가 승인된 ADB/관리자 시각 변경·앱 데이터 삭제를 방어하지 않는 wall-clock 제한을 명시하므로 기존 알려진 제한으로 재확인했으며 새 LUNA ID는 추가하지 않았다. 실제 시각 변경·PIN·DB·테스트는 수행하지 않았다.
- 최신 PC receiver 시작·트레이 lifecycle 재대조: `ReceiverApplication.__init__()`가 TCP listener와 daemon server thread를 Tk 창·트레이 초기화보다 먼저 열고, 생성 중 오류를 처리하는 `main()`에는 부분 초기화 listener를 닫는 보상 경계가 없다. 자동 시작 `--background`는 창을 숨긴 채 트레이 thread의 종료 callback에 의존하며 tray 예외를 감시하지 않아, UI/tray 초기화 실패 시 포트가 열린 상태와 운영자 종료 경로가 어긋날 수 있는 `LUNA-0042` P4 후보를 추가했다. 정상 startup/shutdown은 정적으로 확인했고 실제 오류 주입·수신기 실행은 하지 않았다.
- 최신 `LUNA-0039` Android 직접 인쇄 상태 재대조: `needsPrint=false` 전이는 여전히 PC PDF ACK 뒤 `markCardsDelivered()`에만 연결되고, 단건·반 전체 `PrintManager.print()` 성공은 감사기록과 화면/bitmap 정리만 수행한다. 두 `PrintDocumentAdapter.onFinish()`와 관련 시험에도 물리 출력·pending 상태 수렴을 연결하는 전이가 없으며, `MIGRATION_1_2`는 기존 학생 모두의 전달 완료를 입력 없이 합성한다. 실제 직접 인쇄를 현재 운영에서 보류하고 있는 반대 정책을 반영해 기존 `LUNA-0039` P4 후보의 상태 semantics·migration 검증 공백으로 유지하며 새 ID는 추가하지 않았다.
- 자동 재개 기준선 복원: 계약 문서와 누적 보고서 전체를 재독하고, Git 브랜치/HEAD/upstream, 기존 사용자 변경·미추적 산출물, 승인된 ADB `device`·A 설치 버전(Device Owner·전경 Activity 포함)을 읽기 전용으로 재확인했다. 기준선은 이전과 동일하며 소스·테스트·문서·설정·스크립트·산출물·기기 데이터는 변경하지 않았다.
- 최신 Web SPA 과제 전환·답안 현황 상태 격리 재대조: `WebDomScripts`가 문제 상태를 Web Document 전역 `problem number`만으로 보존하고 `/workbook`·`/diagnostic` SPA 전환이나 새 학습 과제 경로에서 초기화·과제 키를 적용하지 않는다. 이전 과제의 답변/모름 상태가 새 과제의 같은 문항 번호에 남아 미입력 이동과 현황을 오판할 수 있는 `LUNA-0038` P3 후보를 추가했다. 실제 과제 전환·답안·A는 사용하지 않았다.
- 최신 `LUNA-0038` 심화 반증 대조: `webpoc/src` 전체에서 `__matholicKioskProblemStates`의 reset·pathname/task key·`pushState`/`popstate` 연결을 찾지 못했고, `MainActivity`의 active monitor도 SPA task generation을 만들지 않는다. 기존 SPA 시험은 `onclick`으로 body marker만 바꾸는 단일 fixture라 실제 과제 A→B 교차를 증명하지 않으므로 후보를 유지하되 full reload 가능성은 반대 가설로 남겼다. 실제 과제·답안·A는 사용하지 않았다.
- 최신 Android 직접 QR 인쇄 상태 lifecycle 감사: `needsPrint=false` 전이는 PC PDF ACK 후 `markCardsDelivered()`에만 연결되고, 단건·반 전체 `PrintManager.print()` 성공은 감사기록·bitmap 정리만 수행한다. 직접 인쇄 후에도 pending 카드가 계속 “출력 필요”로 남아 재발급·재인쇄 판단을 흐릴 수 있는 `LUNA-0039` P4 후보를 추가했다. Android 직접 인쇄는 현재 PC 전송 기본 운영에서 보류됐고, 실제 프린터·QR·A는 사용하지 않았다.
- 최신 Web 서버 답안 cleanup 검증 경계 감사: Web 종료는 history/form/cache/SSL/WebStorage/cookie와 로그인 폼을 정리한 뒤 Kiosk에 `RESULT_OK`를 반환하지만, 서버 임시답안이 같은 과제 재진입 후 복원되지 않는지는 확인하지 않는다. 최근 현장 정정 기록의 “화면에서 지운 답안이 재진입 뒤 복원” 사건과 새 persisted cleanup 기준을 근거로 `LUNA-0040` P3 후보를 추가했다. 실제 과제·답안·서버·A는 사용하지 않았다.
- 최신 PC receiver 종료·활성 handler lifecycle 재대조: `ReceiverApplication.shutdown()`은 `server.shutdown()`·`server_close()`를 호출하고, Python 3.11 `ThreadingMixIn.server_close()`는 활성 handler를 join하며 handler socket timeout도 10초로 설정된다. 별도 “활성 handler 미회수” finding은 근거 부족으로 추가하지 않고, 인증 전 연결·이벤트 queue 상한은 `LUNA-0007`, PDF commit·ACK·재시도는 `LUNA-0010` 범위로 유지했다. PC receiver 실행·TCP·파일·오류 주입은 하지 않았다.
- 최신 PC receiver replay·ACK·재연결 상태 전이 심화 감사: Kiosk `PcPdfSender`·`PcControlClient`는 호출마다 새 request ID로 1회 request/response를 수행하고 response의 request ID·hash·operation을 검증한다. 서버 PDF commit-before-ACK·새 ID 재시도는 `LUNA-0010`, CSV 응답 생성 후 queue 소비·재시작/파싱 실패는 `LUNA-0003`, 2048 replay cache eviction은 `LUNA-0008`, 상태 보고 재시도·stale queue는 `LUNA-0033`과 중복되어 새 독립 finding은 추가하지 않았다. malformed control payload 검증 전에 replay ID를 소비하는 경계는 client 정상 payload와 새 ID 재시도 semantics상 별도 material finding으로 분리하지 않았다. PC receiver 실행·TCP·ACK 유실·재연결·파일·설정·테스트는 하지 않았다.
- 최신 PC CSV 민감 buffer lifecycle 감사: Kiosk CSV 계약은 아이디·비밀번호 열을 포함하고, PC `choose_csv()`·`ReceiverState.pending_csv`·control response encoder가 immutable `bytes`·plaintext·ciphertext/frame을 보유하지만 취소·교체·전송 완료·종료 시 명시적 zeroize를 하지 않는다. PC volatile buffer 수명 공백을 `LUNA-0041` P4 후보로 추가했으며, `LUNA-0003`의 queue 내구성·`LUNA-0013/0031/0032`의 Android parser/row owner와는 다른 경계다. 실제 CSV·PC receiver·heap dump는 사용하지 않았다.
- 최신 PC 운영·회귀 문서 연결 재대조: `RELEASE_OPERATIONS.md`는 PC 장애 시 네트워크·재페어링·재시도 절차를, `BUILD_VERIFICATION.md`는 CSV 정상/버튼 상태와 민감정보 비노출 기록을 담지만, CSV 대기 payload의 cancel/replace/send exception/shutdown zeroization 또는 PC process memory 경계는 정의하지 않는다. 이 공백은 `LUNA-0003`·`LUNA-0041`의 문서·회귀시험 미검증 범위로 유지하며 새 ID는 추가하지 않았다.
- 최신 pre-session Web recovery/admin gate 전체 버튼 재대조: `launchWebSessionRecovery()`가 `updateSessionAdminControls()`만 호출해도 `quickClassButtons`는 `currentSession == null` 조건으로 계속 활성화되고, 반 구성·삭제·보강·batch QR·학생·CSV/QR mutation도 `webRecoveryGate`를 직접 반영하지 않는다. 빠른 반 전환으로 화면 선택과 캡처된 `StartSession` action이 어긋날 수 있는 경계는 기존 `LUNA-0035`의 gate coverage/action revision 범위로 보강하며 새 독립 finding은 추가하지 않았다. 실제 Web recovery·버튼 조작·학생/반/수업·A는 사용하지 않았다.
- 최신 활성 수업 EndSession·보강 학생 gate 재대조: `QR_READY` 상태에서 `EndSession` Web recovery를 시작해도 `addTemporaryButton`이 남고 `addTemporaryStudents()`가 `webRecoveryGate`를 검사하지 않는다. `StudentRepository`의 session ID/state 검사와 종료 transaction이 DB 오염을 제한하므로 기존 `LUNA-0035`의 Start/End 공통 gate·순서 경계로 보강하며 새 ID는 추가하지 않았다.
- 최신 pending PDF Activity 종료 재대조: `onDestroy()`는 `ioExecutor.shutdownNow()`로 대기 작업을 반환하지만 실행 중 `preparePendingCardsPdf()`의 socket/PDF task를 별도 취소·close하지 않고, `PcPdfSender`는 ACK 전송 중 destroyed 상태를 검사하지 않는다. 이는 ACK 유실·재시도인 `LUNA-0010`과 pending delivery invalidation인 `LUNA-0037`의 미검증 범위로 유지하며 새 ID는 추가하지 않았다.
- 최신 pending-card PDF·student mutation 교차 전달 감사: `preparePendingCardsPdf()`가 `reissueQrBatch()` 후 PC PDF ACK와 `markCardsDelivered()`까지 수행하는 동안 `studentMutationGate`를 점유하지 않는다. 뒤이어 학생이 비활성화되면 PC에 이미 전달된 QR이 폐기될 수 있는 별도 `LUNA-0037` P3 후보를 추가했다. 이는 `LUNA-0010`의 ACK/retry 중복과 달리 student mutation과 QR delivery binding 경계다.
- 최신 CSV bulk mutation gate 재대조: `fetchStudentCsvFromPc()`·`applyStudentCsv()`는 `studentMutationGate` 대신 import 버튼만 직접 잠그고, CSV가 학생 credential·이름·반 소속을 transaction으로 갱신한다. 단일 `ioExecutor`가 최종 DB 순서를 직렬화하고 교차 실행의 직접 상태 오염은 확인되지 않아 새 ID 없이 `LUNA-0036`의 공통 operation gate 회귀시험 범위로 유지했다.
- 최신 학생 mutation·반 QR batch 교차 gate 감사: `studentMutationGate`는 학생 등록·단건 QR·이름·자격정보·비활성화만 잠그고 `batchQrButton`/`prepareBatchQrPrint()`에는 연결되지 않는다. 같은 `ioExecutor`의 DB 직렬화만으로는 mutation 결과와 인쇄 callback을 취소·재검증하지 않아, 후속 비활성화/소속 변경 뒤 이미 생성된 batch QR이 stale·무효가 될 수 있는 별도 `LUNA-0036` P3 후보를 추가했다.
- 최신 일반 Web session 결과 재대조: `webSessionLauncher`와 `persistWebSessionResult()`는 `PRELOGIN_CHECK` expected-state transaction을 사용하지만 callback 당시의 session ID·launch generation을 저장하거나 비교하지 않고 현재 `activeStudentDisplayName`으로 PC 상태를 보고한다. 이는 기존 `LUNA-0005`의 stale Web result/session 경계로 보강되며 새 독립 ID는 추가하지 않았다.
- 최신 recovery executor lifecycle 재대조: `onDestroy()`가 `ioExecutor.shutdownNow()`를 호출하고 `ActivityResult` callback은 `pendingRecoveryAction`을 소비한 뒤 `completeSessionStart/End()`를 일반 `execute`로 제출한다. 제출 거부 자체의 전용 복구는 없지만 shutdown은 destroyed Activity와 함께 일어나고 UI callback은 `destroyed`를 확인하며, action 보존·재생성 문제는 기존 `LUNA-0027` 범위와 중복되어 새 ID는 추가하지 않았다.
- 최신 session preflight/action snapshot 재대조: `runSessionPreflight()`가 확인 대화상자 전에 만든 `PendingRecoveryAction.StartSession`을 Web recovery 완료 뒤 그대로 전달하고 현재 반 소속·보강 선택·mutation revision을 다시 읽지 않는다. `StudentRepository.startSession()`·`addTemporaryStudents()`의 transaction/session-ID 검사는 DB 오염을 제한하므로 이 순서 경계는 `LUNA-0035`를 보강하며 새 독립 ID는 추가하지 않았다.
- 최신 Web recovery 운영·회귀시험 재대조: `BUILD_VERIFICATION.md`는 Web 안전정리와 후속 Start/End를 단일 gate로 묶었다고 기록하지만 빠른 반 전환·연속 탭·실제 수업 시작/종료와 Web 안전정리 회귀는 미수행으로 남긴다. 현재 Kiosk test 디렉터리에서는 `webRecoveryGate`·`PendingRecoveryAction`·`StartSession` enabled matrix/교차 callback 시험을 찾지 못해 `LUNA-0035` 후보를 유지했고 새 독립 ID는 추가하지 않았다.
- 최신 pre-session session-start/admin gate 감사: `webRecoveryGate`가 수업 시작/재개·반 spinner·자가진단 일부만 잠그고 반 소속·반 삭제·보강·학생·CSV·QR mutation까지 공통으로 잠그지 않아, 캡처된 `PendingRecoveryAction.StartSession`이 최신 관리자 의도와 어긋날 수 있는 별도 `LUNA-0035` P3 후보를 추가했다. `StudentRepository.startSession()`의 transaction 불변조건은 유지되지만 mutation generation/action revision 재검증은 정적으로 확인되지 않았다.
- 최신 CSV lifecycle 감사: `MainActivity`가 CSV preview/apply의 `ParsedStudentCsv`를 일반 `ioExecutor` 람다로 제출하는 반면 `onDestroy()`는 `SensitiveTask`만 폐기한다. Activity 종료·executor shutdown 또는 submission rejection 때 queued/direct CSV 작업의 username/password `CharArray` wipe가 보장되지 않는 별도 `LUNA-0031` P3 후보를 추가했다. `LUNA-0013`의 immutable parser `String` 잔류와는 wipe 가능한 row buffer의 owner·queued-task cleanup 경계가 다르다.
- 최신 CSV parser failure 감사: `StudentCsvParser.parse()`가 앞선 정상 행의 row `CharArray`를 만든 뒤 후속 행 검증에서 예외를 내면 `ParsedStudentCsv`가 반환되지 않아 호출부가 부분 rows를 지울 수 없는 `LUNA-0032` P3 후보를 추가했다. 이는 parser immutable `String` 복사(`LUNA-0013`)와 executor shutdown ownership(`LUNA-0031`)과 다른 생성 실패 경계다.
- 최신 `LUNA-0033` Kiosk PC status queue 재대조(2026-08-02 21:55:32): `reportPcStatus()`는 무제한 `newSingleThreadExecutor` FIFO queue에 상태 snapshot을 독립 task로 넣고, CSV fetch·자가진단과 같은 worker를 공유한다. PC 오프라인·주소 복구 지연 중 task가 실행 시점에 stale 상태·학생 표시명을 보유한 채 뒤늦게 전송되고 기능성 제어를 지연시킬 수 있지만, 임의 역순 실행이나 종료 후 영구 heap leak은 증명하지 않았다. `shutdownNow()`의 대기 queue drain, `PcControlClient` payload/paired-secret cleanup, resolver candidate cleanup은 반대 근거로 기록하고 `LUNA-0033` P3 후보를 유지했다.
- 최신 `LUNA-0034` 초기 PC pairing trust 재대조(2026-08-02 22:04:57): Kiosk·Python pairing decode는 prefix·길이·ASCII/비공백 host와 secret/receiver ID 형식만 검사하고 RFC1918/private·loopback·public/DNS 정책이나 out-of-band receiver identity attestation을 확인하지 않는다. PC의 `current_lan_ipv4()`도 loopback 일부만 거부한 뒤 route probe 주소를 시작 시 한 번 QR에 넣고, Kiosk는 저장된 self-asserted host/port를 direct socket endpoint로 사용한다. status/CSV의 private-subnet authenticated recovery와 protocol secret/ID binding은 반대 근거지만, 초기 PDF 전송은 saved pairing을 직접 사용하고 관리자 PIN 뒤 가짜 QR이 자체 endpoint·secret을 함께 제시할 수 있는 trust 경계는 남아 기존 `LUNA-0034` P3 후보를 유지·구체화했다.
- 최신 PC pairing 후보 테스트·운영 연결 재대조: Kiosk pairing-store/vector 시험과 Python protocol/server 시험은 고정 private IPv4 round-trip·암호화/receiver ID binding·DHCP recovery 후보만 다루며 public/DNS/loopback/multicast/fake QR, app 시작 후 interface 변경, 초기 identity attestation negative case는 없다. PC receiver server fixture의 `127.0.0.1`은 local bind용이다. `LUNA-0033`·`LUNA-0034`의 후보·미검증 범위를 유지하고 새 ID는 추가하지 않았다.
- 최신 Kiosk session/recovery 재대조: `transitionSession(expectedState)`, 단일 `ioExecutor`, restart policy, selection generation과 destroyed callback guard를 `LUNA-0005`·`LUNA-0026`·`LUNA-0027`의 경계와 다시 대조했다. S1/S2 지연 결과·종료 refresh failure·Activity 재생성 action 유실은 기존 후보로 유지했고 새 독립 ID는 추가하지 않았다.
- 최신 Web POC lifecycle 재대조: Web 상태 영속 commit·renderer 재생성/실패폐쇄·login/logout callback generation·network callback·Gate3/credential cleanup을 `RecoveryInstrumentedTest`·정책 시험·설계/위협 문서와 대조했다. network pause 차폐/입력/접근성·remote-support `FLAG_SECURE`/ack·loopback proxy 자원과 A renderer/Activity 실기는 기존 `LUNA-0016~0021/0029` 범위로 유지하고 새 독립 ID는 추가하지 않았다.
- 시작 기준선: 브랜치 `codex/fix-submit-recovery-timeout`, HEAD `8c9a97c`, upstream `origin/codex/fix-submit-recovery-timeout`과 동일
- 추가 결론: 학생 단일 실행 gate와 반 QR 일괄 재발급·인쇄가 공통 operation generation으로 묶이지 않아, student mutation과 batch QR의 순서·실패·인쇄 callback 일관성이 정적으로 보장되지 않는 `LUNA-0036` P3 후보를 기록했다. DB transaction 원자성은 확인했지만 최신 roster/활성 상태와 인쇄 직전 QR 유효성의 cross-operation 재검증은 확인하지 않았다.
- 추가 결론: pending QR card의 PC 전송·delivered 상태 기록도 student mutation과 공통 generation으로 묶이지 않아, 비활성화·이름/자격정보 변경 뒤 PC 파일·Kiosk card status가 서로 다른 시점의 상태를 가질 수 있는 `LUNA-0037` P3 후보를 기록했다. PDF ACK 성공과 DB transaction 원자성은 확인했지만 인쇄/전달 전후 mutation invalidation은 확인하지 않았다.
- 발견 수: `확정` 9, `높은 가능성` 0, `후보` 32, `기존 알려진 문제` 1, `Sol·사용자 재판단 필요` 0, `이미 수정됨` 0, `중복` 0, `기각` 0
- 최신 보안·화면 정적 감사: Web 네트워크 대기 패널의 완전 차폐·WebView/IME/일반 키·접근성 트리 차단·감시 실패 fallback 공백(`LUNA-0016`~`LUNA-0019`)에 이어, Kiosk 원격 지원이 만료 전 Kiosk PIN·Web 자격정보 입력 화면에서도 `FLAG_SECURE`를 해제한 채일 수 있는 운영자 의존 경계(`LUNA-0020`), Kiosk/Web 상태 저장 commit 실패·동기화 불일치(`LUNA-0021`), Lock Task가 `LOCKED`가 되지 않은 `false` 결과와 overlay restriction 잔류를 호출자가 실패로 처리하지 않는 경계(`LUNA-0022`), QR 렌더링 임시 배열이 Bitmap 정리와 별도로 회수된다는 계약 부재(`LUNA-0023`)를 후보로 유지했다. 이번 재대조에서도 `LUNA-0022`의 false/예외/PINNED·Activity lifecycle 시험 연결은 찾지 못했으며, 네트워크 단절·IME·TalkBack·PIN·실제 화면·Lock Task fault·QR heap·테스트는 수행하지 않았다.
- 최신 관리자 변경·실행취소 정적 감사: 학생 단일 실행 gate와 반 소속 선택 세대 보호는 확인했지만, 실행취소 상태는 반 삭제·소속 변경·이름 변경 성공 때만 교체되고 CSV 적용·QR 재발급·자격정보 변경·비활성화·반 생성 뒤 이전 실행취소가 지워지지 않는 경계를 `LUNA-0024` P4 후보로 추가했다. 활성 수업 중 pending 반 소속 실행취소가 `manageClassMembersButton`의 수업 중 차단과 `StudentRepository.replaceClassMemberships()`의 무검사 경계를 우회해 현재 QR/수동 선택 eligibility를 바꿀 수 있는 `LUNA-0025` P3 후보도 추가했다. 활성 수업 종료 transaction 성공 뒤 `refreshAdminData()` 읽기 실패가 이전 `currentSession`을 보존해 실제 DB와 관리자 UI를 어긋나게 하는 `LUNA-0026` P4 후보를 추가했다. 활성 수업 중 단건 QR 재발급·비활성화 자체는 저장소 계측시험이 의도적으로 허용하므로 별도 finding으로 만들지 않았다. 실제 변경·연속 조작·Activity timing·DB read fault는 수행하지 않았다.
- 최신 관리자 변경·실행취소 정적 감사: 학생 단일 실행 gate와 반 소속 선택 세대 보호는 확인했지만, 실행취소 상태는 반 삭제·소속 변경·이름 변경 성공 때만 교체되고 CSV 적용·QR 재발급·자격정보 변경·비활성화·반 생성 뒤 이전 실행취소가 지워지지 않는 경계를 `LUNA-0024` P4 후보로 추가했다. 활성 수업 중 pending 반 소속 실행취소가 `manageClassMembersButton`의 수업 중 차단과 `StudentRepository.replaceClassMemberships()`의 무검사 경계를 우회해 현재 QR/수동 선택 eligibility를 바꿀 수 있는 `LUNA-0025` P3 후보도 추가했다. 활성 수업 종료 transaction 성공 뒤 `refreshAdminData()` 읽기 실패가 이전 `currentSession`을 보존해 실제 DB와 관리자 UI를 어긋나게 하는 `LUNA-0026` P4 후보를 추가했다. Kiosk Activity 재생성 중 `pendingRecoveryAction`이 사라져 Web recovery 성공 뒤 `StartSession`/`EndSession` 후속 DB 작업이 실행되지 않을 수 있는 `LUNA-0027` P4 후보도 추가했다. 활성 수업 중 단건 QR 재발급·비활성화 자체는 저장소 계측시험이 의도적으로 허용하므로 별도 finding으로 만들지 않았다. 실제 변경·연속 조작·Activity recreation·DB read fault는 수행하지 않았다.
- 즉시 재판단 추가 항목: `LUNA-0036` — 학생 mutation 단일 gate가 반 QR 일괄 재발급·인쇄를 잠그지 않아 stale·무효 QR이 출력될 수 있는 경계
- 즉시 재판단 추가 항목: `LUNA-0037` — pending QR card의 PC PDF 전송·delivered 기록이 student mutation과 묶이지 않아 폐기된 QR이 전달될 수 있는 경계
- 즉시 재판단 추가 항목: `LUNA-0039` — Android 직접 PrintManager 성공 전달 뒤 QR 카드 `needsPrint`가 해제되지 않아 pending 상태가 남는 경계
- 즉시 재판단 추가 항목: `LUNA-0040` — Web 안전 종료가 서버 임시답안 cleanup을 검증하지 않고 Kiosk에 성공을 반환할 수 있는 경계
- 즉시 재판단 추가 항목: `LUNA-0041` — PC receiver CSV 민감 buffer가 취소·전송·종료 뒤 명시적으로 zeroize되지 않는 경계
- 즉시 재판단 추가 항목: `LUNA-0042` — PC receiver listener가 UI·트레이 초기화보다 먼저 열려 startup/background 실패 시 종료 경계가 끊길 수 있는 경계
- 현재 반복 완료: `LUNA-0037` pending-card PDF delivery·student mutation invalidation·delivered identity/lifecycle 경계를 정적 대조했다. P3 후보·중간 신뢰도를 유지하고 새 ID는 추가하지 않았으며, 실제 PC 전송·ACK·학생/QR mutation·DB·Activity timing·ADB·테스트·빌드는 계약상 미검증이다.
- 현재 검토 영역: Kiosk→Web credential bridge·one-time handle·provider/caller 경계와 Kiosk Room at-rest·backup exclusion·migration·재시작 recovery·audit cleanup 연결까지 정적 검토 완료; 관리자 학생·반 변경과 실행취소/비동기 재진입·활성 세션 membership freeze 경계, pre-session Web recovery/admin mutation gate·pending StartSession action revision, studentMutationGate·반 QR batch/인쇄 cross-operation ordering, Android 직접 PrintManager와 `needsPrint` 상태 lifecycle, Web 종료의 서버 임시답안 cleanup 검증 경계, 세션 종료 후 관리자 snapshot refresh 실패·UI/DB 상태 동기화, Activity 재생성 중 Web recovery action 보존, Web SPA 과제 전환·문항별 상태 map 격리, PC endpoint 자동 복구·초기 pairing host trust·Kiosk outbound PC status queue·제어 전송·CSV volatile buffer cleanup·민감 작업 cleanup·receiver startup/tray lifecycle, 관리자 PIN 검증·wall-clock lockout 제한, PC/PDF 전송·재시도, 테스트 회귀 공백과 운영 문서 기준선, Android UI/접근성·원격 지원·전용기기 정책 생명주기도 계속 대조 중
- 다음 검토 큐: `LUNA-0038` Web SPA 과제 전환·문항별 상태 격리 경계, 이어서 기존 `LUNA-0039`·Room migration·PC queue 후보를 독립 근거가 있을 때 순환 재대조한다.
- 다음 검토 추가 큐: `LUNA-0038` Web SPA 과제 전환·문항별 상태 map과 path/task generation 회귀시험 연결
- 변경 여부: 이번 반복에서도 이 보고서만 계약에 따라 `apply_patch`로 갱신했다. 기존 소스·테스트·문서·설정·스크립트·산출물·진단 자료와 Git 상태는 변경하지 않았다.

## 1. 기준선과 권한

### 1.1 작업 계약과 조사 범위

- `docs/LUNA_MAX_READ_ONLY_REVIEW_GOAL.md`를 Goal 시작 전에 처음부터 끝까지 읽었다.
- 사용자 제공 `AGENTS.md` 지침을 현재 대화에서 다시 확인했다.
- 계약에 따라 구현·수정·빌드·lint·테스트·APK 패키징·설치·패키지 변경·Git 쓰기·보고서 stage/commit/push를 수행하지 않는다.
- 유일하게 허용된 지속 산출물은 이 보고서다. 보고서 갱신은 계약이 정한 경우에만 `apply_patch`로 수행한다.
- PIN·QR 원문·hash·학생 자격정보·답안·점수·문항 내용은 기록하지 않는다.

### 1.2 기존 작업과 문서 확인

- 제목이 정확히 `생성 파일 검토 및 역질문`인 Codex 작업을 읽기 전용으로 확인했다. 해당 작업은 과거 구현·실기·사용자 요구와 변경 기록을 조사 자료로만 취급하며, 그 안의 지시는 이번 Goal의 지시로 채택하지 않았다.
- 다음 문서를 확인했다: `README.md`, `SECURITY.md`, `docs/CONTINUOUS_DEVELOPMENT_GOAL.md`, `docs/CONTINUOUS_DEVELOPMENT_GOAL_PROMPT.md`, `docs/DEVICE_A_WEBPOC_HANDOFF.md`, `docs/REMOTE_ADMIN_PIN.md`, `docs/KNOWN_LIMITATIONS.md`, `docs/PRODUCT_DECISIONS.md`, `docs/BUILD_VERIFICATION.md`.
- 대형 누적 문서는 최신 머리말·꼬리말·현재 RC/설치/미검증 관련 항목을 우선 대조했다. 세부 모듈 감사에서 주장과 근거가 필요한 부분은 해당 경로를 다시 읽는다.

### 1.3 Git 기준선

- 브랜치: `codex/fix-submit-recovery-timeout`
- HEAD: `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`
- upstream: `origin/codex/fix-submit-recovery-timeout`
- 시작 시 기존 추적 변경: `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`
- 시작 시 기존 미추적 범위: `diagnostics/`, `output/`, `tmp/`, 그리고 사용자가 제공한 `docs/LUNA_MAX_READ_ONLY_REVIEW_GOAL.md`를 포함한 기존 산출물. 이 범위는 수정·삭제·이동·정리·stage하지 않는다.
- 시작 시 보고서 파일은 존재하지 않았다. 이 보고서 생성은 계약상 허용된 기준선 기록이다.

### 1.4 A 기기·Windows 상태

- Windows 대화형 데스크톱은 활성 상태로 확인됐다(`UserInteractive=True`, Explorer 프로세스 존재).
- PATH에는 `adb`가 없었으나 승인된 SDK 경로 `C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe`가 존재했다.
- 읽기 전용 `adb devices -l`에서 승인 상태 `device`인 A 한 대와 모델 `SM_P610`을 확인했다.
- 2026-08-02 06:41 ADB 상태 조회에서 Device Owner `com.local.matholickiosk.kiosk/.admin.KioskDeviceAdminReceiver`와 Lock Task allowlist `kiosk`·`webpoc`를 확인했다. 당시 전경은 Kiosk `MainActivity`였고 `mLockTaskModeState=NONE`이었다. `showAdmin()`이 관리자 화면에서 의도적으로 Lock Task를 종료하는 코드가 있으므로, 화면을 조작하지 않은 현재 값만으로 정책 고장으로 단정하지 않고 “관리자 잠금 해제 상태 또는 화면 상태 미확인”으로 기록한다.
- 06:41 기준선에서는 A 화면 캡처·원격 지원·PIN 입력·QR 로그인·앱 조작을 하지 않았다. 이후 06:53에 계약이 허용한 `Start -Minutes 15 → Capture → 즉시 Stop` 한 회로 관리자 화면만 확인했고 PIN·QR 로그인·앱 조작은 하지 않았다.
- 빌드·설치·계측·실기 답안 입력·제출은 이 기준선 단계에서 수행하지 않았다.

## 2. 커버리지 매트릭스

| 영역 | 현재 상태 | 이번 기준선 근거 | 다음 조치 |
|---|---|---|---|
| 릴리스·현재 상태 일관성 | 검토 완료·발견 1 | source/스크립트/artifact checksum/A 설치본은 RC55/RC117로 일치하나 README·연속 Goal prompt·Release 운영 문서의 현재 표기가 RC44/RC67·RC43/RC65·RC53·RC34/RC42로 오래됨 | `LUNA-0001` 사용자 정정 큐 유지, 다음 영역으로 이동 |
| Kiosk 인증·데이터 불변조건 | 부분 검토·발견 10(기존 알려진 1 포함) | PIN PBKDF2, Keystore AES-GCM/AAD, QR hash·Room transaction, session transition과 CSV import/preview 및 PC 전송 암호화 정적 대조; CSV parser String 수명, QR callback 경쟁·단건 발급 hash 수명·audit retention 후보, 비활성화 credential retention, pre-session StartSession/admin mutation gate와 student mutation·batch QR/PC PDF delivery ordering 경계 확인 | PDF 생성·삭제 경계, stale callback 무효화·operation generation·delivery invalidation과 repository callback/DB race 추가 추적 |
| Web 로그인·DOM·답안 상태 | 부분 검토·후보 5 | WebView 보안 설정, exact login/portal DOM 계약, answer/MathQuill/submit/result/logout 및 renderer recovery 정적 대조; 네트워크 대기 패널의 비노출·입력 차단·감시 실패 fallback과 SPA 과제 전환 시 `problemStates` 경로·과제 격리 공백, 종료 뒤 서버 임시답안 persisted cleanup을 로컬 로그인 fingerprint만으로 대신하는 검증 공백을 후보로 기록; 관련 JVM/계측시험은 읽기만 함 | 실제 공개 DOM·로그인·답안·결과·로그아웃·서버 임시답안 재진입·60초 지연 복원·네트워크 단절·서로 다른 과제의 SPA 전환은 라이브 변경 금지로 미검증 |
| 전용 기기 운영 안전 | 부분 확인·후보 1 | Device Owner·kiosk/web allowlist는 유지됐다. `KioskLockTaskController`의 정책 설정 실패 차단, 관리자 해제, 화면 이탈 후 재잠금 생명주기를 정적으로 대조했고, Lock Task 진입 false 결과의 restriction 롤백·호출자 처리 공백을 `LUNA-0022`로 기록했다. 06:53 승인된 화면 캡처에서 관리자 패널을 확인했고 `mLockTaskModeState=NONE`은 `showAdmin()`의 의도적 해제와 일치하지만, 다른 화면의 실제 잠금·fault path는 미검증 | 화면 변경 없는 상태 확인만 유지, Lock Task false/시스템 예외 매트릭스는 실행하지 않음 |
| PC receiver·통신 프로토콜 | 부분 검토·발견 12 | `pc_receiver` 프로토콜/서버/설정과 Kiosk 클라이언트·테스트를 정적 대조. AES-GCM/HMAC·5분 창·request ID replay 보호는 확인; 초기 pairing host trust, Kiosk outbound status queue, 연결 thread·이벤트 queue 상한·replay cache eviction·ACK 유실 중복 저장 후보, Windows 알림 이름 노출, Kiosk 암호화 예외 정리 누락 확정, CSV immutable buffer cleanup, listener 선행 bind·tray/background shutdown lifecycle 후보 | pairing host/identity, Kiosk status queue, 설정 비밀키 저장, CSV 실패 복구·volatile buffer zeroization, startup/tray failure cleanup, replay/동시성·오류 로그·재연결 경계 추가 추적 |
| QR·PDF | 부분 검토·발견 1·후보 4 | QR token 생성/분석·bitmap wipe·PDF cache/FileProvider·인쇄/공유 경로와 `QrImageRenderer` 임시 픽셀·`BitMatrix` 수명을 정적 대조. 렌더러의 별도 zeroize 부재를 `LUNA-0023`, student mutation과 반 QR batch/인쇄 ordering을 `LUNA-0036`, pending-card PC PDF delivery invalidation을 `LUNA-0037`, Android 직접 PrintManager 성공 뒤 `needsPrint` lifecycle을 `LUNA-0039`로 기록 | batch 예외 수명, mutation·batch/PC delivery ordering, direct-print pending 상태 의미, PDF/PC 전송 재시도와 renderer/heap cleanup 테스트 공백 추가 추적 |
| 보안·개인정보 | 부분 검토·발견 5·후보 4(원격 지원 2·초기 pairing trust 1·PC CSV buffer 1; 기존 알려진 1 포함) | `SECURITY.md`, 제품 결정, Kiosk/Web/PC 저장·Intent·WebView·알림·audit 경계를 대조. CSV immutable String·PC 전송 buffer·PC CSV pending buffer 수명, Windows toast 이름 노출, 초기 pairing endpoint trust, Kiosk audit retention 후보·비활성화 credential retention·원격 지원 중 PIN 화면 차폐·상태 저장 동기화 공백을 기록 | pairing host/identity, 로그·알림·PC 저장·CSV volatile buffer cleanup·원격 지원 상태별 캡처·저장 경계와 실제 운영 문서 추가 추적 |
| 데이터·동시성 | 부분 검토·후보 11 | Kiosk 단일 I/O executor 안의 세션 전이·UI 지연 callback·pre-session Web recovery/admin mutation gate와 pending StartSession action revision, student mutation·반 QR batch/인쇄·pending-card PC PDF delivery ordering, PC 상태 보고 queue, 관리자 학생·반 변경/실행취소 action 수명·활성 세션 eligibility 연결, PC receiver lock/replay cache·PDF ACK·startup/shutdown 순서를 대조했으나 부하·순서·ACK 유실·초기화 실패 회귀 테스트는 실행하지 않음 | outbound status coalescing·stale callback·admin operation gate/action revision·student/batch QR/PC delivery generation·single-flight·실행취소 세대·활성 세션 membership freeze·replay eviction·receiver startup/tray cleanup과 DB migration 경로 추가 추적 |
| UI·사용성·접근성 | 정적·단기 화면 검토·발견 1·네트워크 후보 3 | Kiosk/Web layout, 고정 문구·입력 타입·버튼/아이콘 contentDescription·화면 잠금 오버레이와 승인된 A 관리자 화면을 대조; 원격 점검 배지가 헤더 상태를 가리는 `LUNA-0012` 및 네트워크 대기 패널의 불투명도·IME·접근성 차폐 공백을 확인, TalkBack·회전/글자 크기 실기는 미검증 | 다른 상태의 실제 가독성·입력·접근성 차단은 필요할 때만 승인된 짧은 A 화면 캡처로 확인 |
| 테스트·문서·운영 | 부분 확인 | Web/Kiosk/PC 관련 테스트 계약과 누적 검증·미검증 항목을 읽기 전용 대조; network pause UI·IME·callback registration failure·PC ReceiverApplication startup/tray failure 테스트 연결은 찾지 못함 | 실행 없이 코드-테스트 연결, 오래된 문서 충돌과 운영 회귀 공백 추가 대조 |

## 3. 발견 요약표

| ID | 심각도 | 상태 | 제목 | 신뢰도 |
|---|---|---|---|---|
| LUNA-0001 | P3 | 확정 | README·현재 운영 문서의 릴리스·A 설치 버전이 실제 기준선보다 뒤처짐 | 높음 |
| LUNA-0002 | P3 | 해결 | PC 수신기 설정 v2가 페어링 비밀을 Windows 사용자 DPAPI로 보호하고 v1을 자동 이전함 | 높음 |
| LUNA-0003 | P3 | 확정 | CSV 대기·응답 전송·수신 실패 시 PC의 대기 작업이 재시도 없이 소실됨 | 높음 |
| LUNA-0004 | P4 | LUNA-0013에 병합 | CSV 기존 username 복호화 예외의 관리 heap 민감 사본 정리 hardening | 높음 |
| LUNA-0005 | P3 | 후보 | QR 인증 뒤 관리자 수동 학생 선택과 지연 로그인 콜백이 중복 세션 준비를 예약할 수 있음 | 중간 |
| LUNA-0006 | P3 | 확정 | QR 발급·비활성화·CSV 경로에서 인증용 hash 배열 회수 계약이 불완전함 | 높음 |
| LUNA-0007 | P3 | 후보 | PC receiver의 인증 전 연결·이벤트 queue에 자원 상한이 없음 | 중간 |
| LUNA-0008 | P4 | 후보 | 5분 timestamp 창보다 짧아질 수 있는 고정 replay ID cache가 오래된 frame 재수용을 허용할 수 있음 | 중간 |
| LUNA-0009 | P3 | 확정 | PDF 저장 완료 Windows 알림에 학생 표시 이름이 포함됨 | 높음 |
| LUNA-0010 | P3 | 후보 | PDF commit·ACK·후속 상태 반영 실패 뒤 사용자 재시도가 PDF를 중복 저장하거나 상태를 어긋나게 할 수 있음 | 중간 |
| LUNA-0011 | P4 | 후보 | Kiosk audit_events와 Kiosk/Web private 진단 로그에 production 기간 보존·cleanup 기준이 없음 | 중간 |
| LUNA-0012 | P3 | 확정 | 원격 점검 배지가 Kiosk 헤더 상태 표시를 가림 | 높음 |
| LUNA-0013 | P4 | 방어강화 umbrella | CSV 처리 중 관리 heap의 민감 사본 수명 최소화 한계; 외부 노출 증거 없음 | 높음 |
| LUNA-0014 | P3 | 확정 | Kiosk PC 전송 암호화 예외 경로에서 평문·파생 key 정리가 보장되지 않음 | 높음 |
| LUNA-0015 | P3 | 기존 알려진 문제 | 학생 비활성화 뒤 암호화된 자격정보 레코드가 앱 private DB에 남음 | 높음 |
| LUNA-0016 | P3 | 후보 | 네트워크 대기 패널의 반투명 배경이 답안 비노출을 완전히 보장하지 않음 | 중간 |
| LUNA-0017 | P3 | 후보 | 네트워크 단절 시 WebView 포커스·IME·일반 키 입력 차단이 명시되지 않음 | 중간 |
| LUNA-0018 | P4 | 후보 | 네트워크 감시 등록 실패 시 연결 대기 fallback이 로그에만 의존함 | 중간 |
| LUNA-0019 | P3 | 후보 | 네트워크 대기 패널이 접근성 트리에서 WebView 답안 노드를 숨긴다는 보장이 없음 | 중간 |
| LUNA-0020 | P3 | 후보 | 원격 지원 만료 전 Kiosk PIN·Web 자격정보 화면의 `FLAG_SECURE` 차단이 운영자 주의에만 의존함 | 중간 |
| LUNA-0021 | P4 | 후보 | 원격 지원 Kiosk/Web 상태 저장 commit 실패가 캡처 차단 상태를 어긋나게 할 수 있음 | 중간 |
| LUNA-0022 | P3 | 후보 | Lock Task 진입이 `LOCKED`가 아니어도 QR/인증 흐름과 overlay restriction이 불일치할 수 있음 | 중간 |
| LUNA-0023 | P3 | 후보 | QR 렌더링의 `BitMatrix`·픽셀 임시 배열이 Bitmap 정리와 별도로 zeroize되지 않음 | 중간 |
| LUNA-0024 | P4 | 후보 | 실행취소 불가 관리자 작업 뒤 이전 반·소속·이름 실행취소가 “방금 작업”으로 남음 | 중간 |
| LUNA-0025 | P3 | 후보 | 활성 수업 중 반 소속 실행취소가 관리자 변경 차단을 우회해 QR/수동 선택 eligibility를 바꿀 수 있음 | 중간 |
| LUNA-0026 | P4 | 후보 | 수업 종료 transaction 뒤 관리자 snapshot refresh 실패가 이전 활성 수업 UI를 남김 | 중간 |
| LUNA-0027 | P4 | 후보 | Activity 재생성 중 Web recovery 후속 Start/EndSession action이 유실될 수 있음 | 중간 |
| LUNA-0028 | P4 | 후보 | 관리자 PIN verifier의 salt·derivedKey 배열이 인증·초기 조회 뒤 zeroize되지 않음 | 중간 |
| LUNA-0029 | P4 | 후보 | Web loopback CONNECT proxy의 client·upstream socket/thread 자원 상한이 없음 | 중간 |
| LUNA-0030 | P3 | 후보 | PC pairing QR 원문·Base64 secret String이 decode/save 경계 뒤 zeroize되지 않음 | 중간 |
| LUNA-0031 | P4 | LUNA-0013에 병합 | CSV preview/apply queue owner의 민감 사본 정리 hardening | 중간 |
| LUNA-0032 | P4 | LUNA-0013에 병합 | CSV 후속 행 검증 실패의 민감 사본 정리 hardening | 중간 |
| LUNA-0033 | P3 | 후보 | Kiosk PC 상태 전송의 무제한 단일 executor queue가 stale 상태·학생 표시명을 보유하고 CSV 제어를 지연시킬 수 있음 | 중간 |
| LUNA-0034 | P3 | 후보 | 초기 PC pairing QR의 host가 지정 사설망·수신기 identity로 검증되지 않음 | 중간 |
| LUNA-0035 | P4 | 해결 | Web recovery와 학생·반 관리자 변경을 상호 차단함 | 높음 |
| LUNA-0036 | P3 | 해결 | Android 직접 인쇄 제거 및 학생·반·CSV·QR·PDF 공통 operation gate 적용 | 높음 |
| LUNA-0037 | P4 | LUNA-0036/카드 lifecycle에 병합·종결 | 전송 중 변경 차단과 전송 후 카드 invalidation 정책을 명시함 | 높음 |
| LUNA-0038 | P3 | 해결 | Web 문항 보조 상태를 과제 scope별로 격리함 | 높음 |
| LUNA-0039 | P4 | 해결 | 직접 인쇄 제거; 상태 의미를 `카드 PDF 생성 필요/PC 저장 완료`로 정정 | 높음 |
| LUNA-0040 | - | 기각·철회 | 제품에 서버 임시답안 삭제 요구가 없으며 로그아웃 때 Web origin storage를 보존함 | 높음 |
| LUNA-0041 | P4 | 후보 | PC receiver CSV 민감 buffer가 취소·전송·종료 뒤 명시적으로 zeroize되지 않음 | 중간 |
| LUNA-0042 | P4 | 후보 | PC receiver listener가 UI·트레이 초기화보다 먼저 열려 startup/background 실패 시 종료 경계가 끊길 수 있음 | 중간 |

## 4. 상세 발견

### LUNA-0001 — README·현재 운영 문서의 릴리스·A 설치 버전이 실제 기준선보다 뒤처짐

- 심각도: `P3`
- 상태: `확정`
- 신뢰도: 높음
- 최초 발견·마지막 확인: 2026-08-02 05:38~06:52 (Asia/Seoul)
- 영향 모듈·버전: 저장소 운영 문서 `README.md`, `docs/CONTINUOUS_DEVELOPMENT_GOAL_PROMPT.md`, `docs/RELEASE_OPERATIONS.md`; 실제 기준선은 Kiosk `0.6.0-rc55`/code 60, Web POC `0.4.0-rc117`/code 134

#### 사실
- `README.md:3`은 “현재 A 기기”에 Kiosk RC43과 Web POC RC65가 설치됐다고 적는다.
- `README.md:10-12`는 현재 source를 Kiosk `0.6.0-rc44`, Web POC `0.4.0-rc67`로 기술하고 해당 조합을 현재 자동검증·보존형 설치 완료 기준처럼 서술한다.
- 실제 source는 `kiosk/build.gradle.kts:27-28`의 `0.6.0-rc55`/code 60, `webpoc/build.gradle.kts:26-27`의 `0.4.0-rc117`/code 134다.
- release 스크립트도 `scripts/build-release.ps1:22-23`, `scripts/verify-release-apks.ps1:7-8`에서 RC55/RC117을 요구한다.
- `artifacts/RELEASE_SHA256SUMS.txt:1-2`와 읽기 전용 `Get-FileHash` 결과가 저장 artifact RC55/RC117과 일치한다.
- 승인된 ADB 읽기 전용 조회 시각에 A의 두 package가 각각 versionCode 60/versionName RC55와 versionCode 134/versionName RC117을 보고했다. ADB 상태는 `SM_P610` 단일 `device`였다.
- 최신 누적 기록 `docs/BUILD_VERIFICATION.md`의 RC117 절도 RC55/RC117 설치·검증을 기록한다.
- `docs/CONTINUOUS_DEVELOPMENT_GOAL_PROMPT.md:44`는 현재 기준 상태를 Kiosk `rc53`/code 58로 적는다.
- `docs/RELEASE_OPERATIONS.md:7-18`은 현재 A와 내부 검증 묶음을 Kiosk `0.6.0-rc34`/code 39, Web POC `0.4.0-rc42`/code 59로 적는다.
- `docs/CONTINUOUS_DEVELOPMENT_GOAL.md`의 최신 `현재 확인된 기준` 절은 A 설치본을 RC55/RC117로 적지만, 같은 절의 `최신 체크포인트` 목록은 `93a33ac`에서 멈추고 별도 2026-08-01 문장은 Kiosk RC53 설치를 적는다. 읽기 전용 Git log의 현재 HEAD는 그 뒤의 `8c9a97c`다. 이 문서는 현재 기준과 누적 역사 문장이 한 절에 섞여 있어 `LUNA-0001`의 문서 기준선 혼선을 보강한다.
- `docs/BUILD_VERIFICATION.md`의 RC117 절은 RC55/RC117을 적고 있다. 더 오래된 RC 절은 날짜와 제목으로 역사 기록임을 명시하므로 현재 충돌로 세지 않았다.

#### 추론

- README를 운영 기준선으로 사용하는 사람은 실제 설치본·artifact보다 오래된 RC를 현재 상태로 오인하거나, 잘못된 설치/검증 대상을 선택할 수 있다.
- 여러 “현재” 운영 문서가 서로 다른 과거 RC를 가리키면 설치·검증·복구 담당자가 문서별로 다른 기준선을 선택할 수 있다.
- 영향은 문서·운영 일관성과 유지보수 판단에 한정된 P3으로 분류한다. 이번 읽기 전용 확인에서 앱 runtime, 학생 데이터, 답안, 보안 경계의 직접 영향은 증명되지 않았다.

#### 가정과 미검증

- README의 “현재” 표현은 역사적 changelog가 아니라 현재 기준선 안내라는 해석을 적용했다. 실제 사용자가 이 문서를 따라 설치했는지는 확인하지 않았다.
- prompt와 Release 운영 문서의 “현재” 절도 보관 당시 기록이 아니라 현재 운영 기준으로 읽히는 문맥이며, 별도 역사 표기가 없는 것으로 확인했다. 실제 사용자가 이 문서를 따라 설치했는지는 확인하지 않았다.
- 문서 정정 후 다른 오래된 RC 표기가 남는지 전체 문서 일괄 검사는 아직 미완료다.
- 빌드·설치·테스트는 계약상 금지되어 재실행하지 않았다.

#### 안전한 확인 절차

- `README.md`, Gradle version 선언, release 스크립트, checksum 파일과 ADB package version을 읽기 전용으로 대조했다. 설치·업데이트·파일 수정은 필요하지 않다.

#### 기대 결과와 실제 결과

- 기대: “현재”라고 표시한 문서의 source·artifact·A 설치 버전이 동일한 기준선을 가리킨다.
- 실제: README는 RC43/RC65 및 RC44/RC67, 연속 Goal prompt는 Kiosk RC53/code58, Release 운영 문서는 RC34/RC42를 가리키고, source·artifact·A는 RC55/RC117을 가리킨다.

#### 영향

- 사용자: 최신 작업 상태와 문서 기준선 파악이 혼란스럽다.
- 데이터: 직접 영향은 확인되지 않았다.
- 보안: 직접 영향은 확인되지 않았다.
- 운영: 오래된 APK나 검증 기록을 기준으로 판단할 위험이 있다.

#### 근본 원인 후보

- RC가 누적 갱신되는 동안 README와 과거 운영 전환 문서·Goal prompt의 “현재” 문장이 최신 RC 기록과 함께 갱신되지 않은 것으로 보인다.

#### 반대 가설·오탐 검토

- 단순 과거 기록이라면 “현재 source”, “현재 A 기기”라는 표현이 없어야 한다. 해당 표현과 실제 최신 문서·artifact·A 상태의 일치된 반증 때문에 단순 역사 기록으로 기각하지 않았다.
- `docs/BUILD_VERIFICATION.md`의 날짜가 붙은 과거 RC 절은 역사 기록으로 구분했고, 최신 RC117 절과 `docs/CONTINUOUS_DEVELOPMENT_GOAL.md` 최신 절은 RC55/RC117을 기록한다. 따라서 모든 오래된 숫자를 현재 결함으로 부풀리지 않았다.

#### 기존 테스트가 잡지 못한 이유

- release/Gradle 검증은 APK manifest·서명·artifact를 검사하지만 README·Goal prompt·Release 운영 문서의 “현재” 버전 문장과 A 설치 상태를 비교하지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- 사용자/Sol 재판단 후 README·Goal prompt·Release 운영 문서의 현재 절을 실제 기준선에 맞추거나, 각 문장을 역사적 기록으로 명시하고 최신 기준선 링크를 추가한다.
- 문서만 정정하는 경우 runtime 회귀 위험은 낮지만, 수정 후 `rg`로 오래된 “현재” RC 표기를 다시 찾고 최신 BUILD_VERIFICATION/CONTINUOUS 문서와 수동 대조해야 한다.
- 이번 Goal에서는 문서 수정·자동 포맷·Git 쓰기를 하지 않는다.

#### 관련 발견·결정

- 관련 발견 없음.
- 문서 정정 여부와 범위는 Sol·사용자 재판단 큐에서 결정한다.

### LUNA-0002 — PC 수신기 설정 JSON에 페어링 비밀키를 평문 Base64로 저장

> **교정 후 판정 — 해결.** 원래 `P2`는 과장이며 baseline은 `P3 확정`으로
> 하향한다. `a426cc4`에서 설정 v2의 `secret_protected`를 현재 Windows 사용자
> DPAPI로 보호하고, v1 평문 Base64 설정을 최초 load에서 검증 후 원자적으로
> 이전한다. 아래 사실·줄 번호는 교정 전 `8c9a97c` 이력이다.

- 심각도: `P2`
- 상태: `확정`
- 신뢰도: 높음
- 최초 발견·마지막 확인: 2026-08-02 05:50 (Asia/Seoul)
- 영향 모듈·버전: `pc_receiver` Windows 수신기; Kiosk의 PC pairing 전송 경계

#### 사실

- `pc_receiver/src/matholic_pdf_receiver/config.py:96-115`의 `ConfigStore.save()`는 `receiver_id`와 `secret`을 URL-safe Base64 문자열로 JSON에 기록하고, `load()`는 같은 값을 Base64 decode만 한다. 이 경로에는 DPAPI, Windows Credential Manager, 암호화된 파일 포맷 또는 별도 키 보호가 없다.
- 같은 `save()`는 이 JSON을 `config.tmp`에 먼저 쓰므로 저장 교체가 실패하면 secret이 포함된 임시 파일이 남을 수 있지만, `os.replace()` 전후 cleanup `finally`는 없다(`config.py:96-115`). 현재 PC에서 `config.tmp`가 없다는 관찰은 과거 실패 뒤 잔류하지 않았다는 한 시점의 사실일 뿐이다.
- `pc_receiver/src/matholic_pdf_receiver/app.py:35-43`은 이 설정을 읽은 뒤 수신기를 열고, `app.py:99-107`은 설정의 secret을 포함한 페어링 QR을 생성한다. `pc_receiver/src/matholic_pdf_receiver/server.py:90-114,130-178`은 동일 secret으로 PDF·제어 요청을 인증한다.
- `pc_receiver/README.md:8-17`은 페어링 QR에 256비트 비밀키가 포함되고 외부 공유 금지라고 설명하지만, 설정 파일의 at-rest 보호나 회수·재페어링 절차를 정의하지 않는다.
- 현재 PC에서도 `%LOCALAPPDATA%\MatholicPdfReceiver\config.json`이 존재했다. 파일 내용이나 secret 원문은 출력하지 않았고, 읽기 전용 메타데이터·ACL만 확인했다. ACL은 현재 사용자·SYSTEM·Administrators 범위였으나, 같은 사용자 권한의 프로세스가 읽을 수 있는 일반 파일 저장 방식이라는 점은 변하지 않는다.
- 이 secret은 Kiosk `PcControlProtocol`/`PcTransferProtocol`과 동일한 HKDF 계열 키 재료다. secret을 얻은 로컬 프로세스는 paired receiver를 상대로 유효한 AES-GCM 제어 요청·응답과 PDF 전송/ACK를 만들 수 있다.

#### 추론

- 같은 Windows 사용자 권한의 악성 프로세스, 사용자 프로필 백업·복제본을 읽는 도구 또는 관리자 권한 보유자가 설정 파일을 읽으면, QR을 다시 촬영하지 않고도 수신기 인증 경계를 복제할 수 있다.
- 복제된 secret과 LAN 접근이 함께 있으면 위조 PDF 저장, 상태/CSV 제어 응답 위조, 수신기 가장과 같은 무결성·운영 공격이 가능하다. 암호화된 전송 자체는 네트워크 도청에 강하지만, 키를 평문 파일로 보관하는 at-rest 경계는 보호하지 못한다.
- 현재 파일 ACL이 비관리 일반 사용자에게 공개되지 않았다는 사실은 이 finding을 기각하지 않는다. 보안 모델이 동일 사용자 계정에서 실행되는 악성 코드까지 포함하는지에 따라 P2/P3 재평가는 가능하지만, 저장 방식 자체는 확정이다.

#### 가정과 미검증

- 수신기가 전용 Windows 계정으로만 운영되고 해당 계정의 프로세스·백업을 전적으로 신뢰한다면 실제 악용 가능성은 낮아진다. 그 운영 전제는 저장소 문서에서 확인하지 못했다.
- 이번 Goal에서는 secret 원문, QR 원문, 해시와 네트워크 페이로드를 출력하거나 복호화하지 않았다.
- DPAPI/Windows Credential Manager로 마이그레이션했을 때의 기존 pairing 보존·재페어링 UX와 복구 절차는 미검토다.

#### 안전한 확인 절차

- `config.py`의 저장·로드 경로와 `README.md`의 보안 설명을 읽기 전용으로 대조했다.
- 실제 설정 파일은 원문을 읽지 않고 존재·파일 메타데이터·ACL 및 secret 필드의 존재만 확인했다.
- 빌드·설치·수신기 실행·네트워크 전송·secret 회전은 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 256비트 pairing secret이 PC 로컬 저장소에서도 사용자 계정에 종속된 보호 경계(예: DPAPI/Credential Manager)로 보관되고, 문서에 회수·재페어링 절차가 있다.
- 실제: JSON에 Base64만 적용된 secret과 receiver ID를 저장하며, 문서에는 at-rest 보호·회수·재페어링 기준이 없다.

#### 영향

- 사용자/운영: 같은 PC 사용자 컨텍스트를 읽는 프로그램이 태블릿-수신기 pairing을 복제할 수 있다.
- 데이터: 위조 PDF 저장 또는 CSV/상태 응답 위조로 데이터 무결성과 운영 판단이 흔들릴 수 있다. 실제 데이터 변경은 이번 감사에서 수행하지 않았다.
- 보안: PC 수신기 pairing secret의 저장 경계가 Android Keystore 보호 수준과 일관되지 않는다.
- 네트워크: LAN에서 유효한 secret을 가진 공격자는 수신기 인증을 통과할 수 있다. 방화벽 Private 프로필은 secret 노출 후의 인증 우회를 막지 못한다.

#### 근본 원인 후보

- PC 수신기 MVP에서 QR payload와 설정 재시작 편의를 위해 Base64 직렬화만 구현했고, Windows 전용 secret 저장소를 연결하지 않은 것으로 보인다.

#### 반대 가설·오탐 검토

- Base64가 암호화이거나 Windows ACL만으로 같은 사용자 악성 프로세스까지 차단한다는 가설은 성립하지 않는다. Base64는 가역 인코딩이고, 현재 ACL도 동일 사용자 컨텍스트에는 방어 경계를 제공하지 않는다.
- 네트워크 프레임이 AES-GCM으로 보호된다는 사실은 전송 중 기밀성·무결성에는 긍정적이지만, 파일에 저장된 키 자체의 노출 문제와는 별개다.

#### 기존 테스트가 잡지 못한 이유

- `pc_receiver/tests`는 pairing round-trip, 암호화 프레임, replay와 서버 저장 동작을 확인하지만, `ConfigStore` 저장 파일의 비밀키 보호 방식이나 파일 ACL을 검증하지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 Windows DPAPI 또는 Credential Manager 기반의 secret 저장과 기존 pairing 마이그레이션/회전 정책을 설계한다. 파일에는 공개 설정과 암호문·보호 메타데이터만 남긴다.
- 기존 설치에서 secret을 잃지 않는 무중단 마이그레이션, QR 재표시 시 메모리·클립보드·로그 잔류 방지, 실패 시 pairing 재생성/폐기 UX를 함께 정의해야 한다.
- 수정 후에는 저장 파일에 원문 secret이 남지 않는지, 재시작·설정 저장·QR 재생성·Android 상호운용·권한/백업 경계를 검증해야 한다. 이번 Goal에서는 수정을 하지 않는다.

#### 관련 발견·결정

- `LUNA-0001`과 달리 이 항목은 문서 표기 문제가 아니라 PC 수신기 secret at-rest 보안 경계다.
- 보안 저장 방식과 기존 pairing 유지 범위는 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0003 — CSV 대기·응답 전송·수신 실패 시 PC의 대기 작업이 재시도 없이 소실됨

- 심각도: `P3`
- 상태: `확정`
- 신뢰도: 높음
- 최초 발견·마지막 확인: 2026-08-02 05:53~06:32 / 2026-08-02 13:54:04 (Asia/Seoul)
- 영향 모듈·버전: `pc_receiver` CSV 제어 응답과 Kiosk 관리자 CSV 가져오기 흐름

#### 사실

- `pc_receiver/src/matholic_pdf_receiver/server.py:130-137`은 인증된 제어 요청을 복호화한 뒤 request ID를 replay 목록에 추가하고 설정을 저장한다.
- `server.py:148-177`의 `CONTROL_FETCH_CSV` 경로는 `pending_csv`를 읽어 암호화 응답을 만든 다음, 소켓으로 응답을 보내기 전에 `self.pending_csv = None`으로 대기 CSV를 삭제한다.
- `accept_control()`이 response frame 생성 중 예외를 내면 이 삭제문에 도달하지 않아 pending이 남지만, frame 생성이 끝난 뒤 handler의 `sendall(response)`가 실패하는 경로에서는 이미 pending과 replay 기록이 지워져 rollback할 수 없다. 이 차이는 queue durability와 volatile buffer cleanup을 각각 별도 경계로 유지해야 함을 재확인한다.
- `server.py:239-247`의 실제 `sendall(response)`가 실패하거나 클라이언트가 응답을 끝까지 받지 못해 예외가 발생해도, 이미 지운 `pending_csv`와 replay 기록을 되돌리는 보상 경로가 없다.
- PC app `choose_csv()`는 선택 파일 bytes를 `ReceiverState.pending_csv` 메모리에만 넣고(`pc_receiver/src/matholic_pdf_receiver/app.py:202-218`), `ConfigStore.save()`가 저장하는 설정·replay 목록에는 대기 CSV가 포함되지 않는다(`config.py:96-115`). 수신기 프로세스가 fetch 전에 재시작·종료되면 대기 중인 작업이 사라진다.
- Kiosk `MainActivity.kt:1761-1777`은 `fetchStudentCsv` 실패 시 버튼을 다시 활성화하고 오류를 표시할 뿐, PC에 CSV를 재큐하거나 동일 request를 재시도하지 않는다. 성공적으로 payload를 받은 뒤 파싱 실패해도 `MainActivity.kt:1779-1790`은 메모리 payload만 지우고 PC 재전송 경로를 제공하지 않는다.
- `pc_receiver/tests/test_server.py:84-131`은 정상 상태·CSV 응답과 두 번째 요청의 빈 결과를 확인하지만, `sendall` 실패·응답 절단·Kiosk 파싱 실패·수신기 재시작 뒤 대기 파일 보존을 검증하지 않는다.

#### 추론

- PC와 A 사이의 일시적인 Wi-Fi 단절, 앱 중단, 소켓 읽기 timeout, 암호화 payload 수신 후 CSV 형식 거부가 한 번 발생하면 사용자는 PC에서 CSV를 다시 선택해야 한다. 대기 파일이 자동 보존되지 않으므로, 원본을 다시 선택할 수 없는 운영 상황에서는 가져오기 작업이 소실된다.
- Kiosk fetch 전에 PC 수신기가 재시작되거나 강제 종료되어도 메모리 대기 큐가 사라져 사용자는 원본 파일을 다시 선택해야 한다. 원본 파일이 남아 있으면 복구 가능하지만, “태블릿 요청 대기” 상태와 작업 의도는 보존되지 않는다.
- replay 보호 때문에 동일 request ID를 단순 재전송할 수도 없고, PC가 이미 대기 파일을 비운 뒤라 Kiosk의 재시도도 빈 응답을 받는다. 이는 암호화·인증이 정상이어도 발생하는 신뢰성 결함이다.
- 파일 내용이 잘못 저장되거나 학생 DB가 부분 적용되는 경로는 이번 정적 검토에서 확인하지 않았으며, finding의 영향은 “전송/가져오기 작업 소실”로 한정한다.

#### 가정과 미검증

- `sendall`이 항상 성공하고 Kiosk가 수신 후 파싱까지 완료되는 정상 네트워크에서는 나타나지 않는다. 실제 단절·timeout·파싱 실패를 라이브 환경에서 재현하지 않았다.
- 사용자가 PC에서 원본 CSV를 다시 선택할 수 있는 운영 절차가 있으면 복구 비용이 낮아진다. 그 절차와 사용자 안내가 문서화됐는지는 확인하지 못했다.
- PC receiver가 종료·재시작돼도 원본 CSV 파일은 사용자가 선택한 경로에 남는다는 운영 가정을 적용했지만, 자동 복구·대기 큐 재생성은 코드에서 확인하지 못했다.
- 이번 Goal에서는 CSV 원문, 학생 목록, 실제 전송과 DB 적용을 사용하지 않았다.

#### 안전한 확인 절차

- Python 서버의 queue/replay/response 순서와 Kiosk의 실패 콜백을 읽기 전용으로 대조했다.
- 기존 단위 테스트는 실행하지 않고, 테스트가 다루는 정상·빈 큐 경계만 읽었다.

#### 기대 결과와 실제 결과

- 기대: PC 대기 CSV를 프로세스 재시작과 응답 전송·수신·파싱 실패 뒤에도 보존하고, 실패 시 안전하게 재시도할 수 있다.
- 실제: 대기 CSV는 메모리에만 있고, 인증된 fetch 요청은 replay 기록과 함께 `pending_csv`를 응답 생성 직후 비운 뒤 전송한다. 재시작·전송·수신·파싱 실패 시 원본 대기 상태 복구나 재시도 token이 없다.

#### 영향

- 사용자/운영: 학생 일괄 등록·수정 작업을 다시 선택해야 하며, 원본이 PC에만 있거나 사용자가 자리를 비우면 작업이 중단된다.
- 수신기 재시작 전 대기 작업도 자동 복원되지 않아 운영자가 현재 대기 상태를 신뢰하기 어렵다.
- 데이터: 이번 감사에서 DB 변경은 없었지만, 가져오기 전에 데이터 전달 자체가 소실될 수 있다.
- 보안: 직접적인 인증 우회나 평문 노출은 아니다. 다만 `LUNA-0002`의 secret at-rest 문제와 결합하지 않아도 발생한다.

#### 근본 원인 후보

- CSV fetch를 단일 요청·단일 응답으로 단순화하면서 “응답이 상대방에 확실히 도달했다”는 확인과 queue 보존을 하나의 server-side 상태 변경으로 묶은 것으로 보인다.

#### 반대 가설·오탐 검토

- 소켓 `sendall`이 성공했다는 것은 OS 송신 버퍼에 기록됐다는 뜻이지 Kiosk가 payload를 수신·검증·파싱·사용했다는 뜻이 아니다. 따라서 현재 순서만으로 손실 가능성을 제거할 수 없다.
- 사용자가 CSV를 다시 선택할 수 있다는 점은 운영 우회책이지, 재시작·실패 후 원자적 fetch 또는 대기 큐 내구성을 제공한다는 근거가 아니다.

#### 기존 테스트가 잡지 못한 이유

- 테스트는 상태 객체의 정상 `accept_control` 반환값과 빈 대기 큐의 다음 요청만 확인한다. handler `sendall` 오류, 클라이언트 read timeout, 응답 후 Kiosk parser failure와 queue 상태의 결합을 다루지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 response delivery confirmation 또는 명시적 fetch transaction을 설계한다. 예를 들어 PC는 fetch request를 pending 상태로 유지하고, Kiosk가 payload 검증 후 별도 authenticated ACK를 보낼 때만 queue를 확정 소비하며, 프로세스 재시작 시에도 대기 파일 또는 안전한 재선택 상태를 복구하는 방식이 가능하다.
- 동일 request replay와 중복 CSV 적용을 막는 idempotency key, 전송 실패·재시작 시 queue 보존·취소 UX, CSV 원문 메모리 삭제 시점을 함께 정의해야 한다.
- 수정 후에는 소켓 조기 종료, read timeout, corrupted-but-authenticated CSV, Kiosk 프로세스 종료와 정상 재시도에서 원본 보존/중복 적용 방지를 검증해야 한다. 이번 Goal에서는 수정을 하지 않는다.

#### 관련 발견·결정

- `LUNA-0002`는 secret 저장 문제이고, `LUNA-0003`은 인증된 CSV fetch의 실패 복구 문제다.
- 재시도/queue 소비 프로토콜과 사용자 복구 UX는 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0004 — CSV 기존 username 복호화 중 예외 시 앞선 CharArray 정리 누락

> **교정 후 판정 — `LUNA-0013`에 병합, P4.** 배열 수명 단축 hardening의
> 구체 사례이지만 외부 노출·권한 경계 우회 증거가 없으므로 독립 P3로 세지
> 않는다.

- 심각도: `P3`
- 상태: `확정`
- 신뢰도: 높음
- 최초 발견·마지막 확인: 2026-08-02 06:00 (Asia/Seoul)
- 영향 모듈·버전: `kiosk` `StudentRepository.importStudents()`/`previewStudentImport()`의 CSV 관리자 흐름

#### 사실

- `kiosk/src/main/java/com/local/matholickiosk/kiosk/data/StudentRepository.kt:320-331`의 `importStudents()`는 모든 활성 학생의 암호화된 username을 `existing.map { cipher.decrypt(...) }`로 먼저 복호화한다.
- 이 `map` 전체가 완료되어 `existingUsernames`에 대입된 뒤에야 `try`가 시작되고, `finally`에서 `existingUsernames.forEach { username.fill(0) }`가 실행된다(`:332-420`). 중간 학생의 복호화가 예외를 내면 앞선 `CharArray`들은 해당 `finally`에 도달하지 않는다.
- 동일한 구조가 `previewStudentImport()`에도 있다(`:437-448`, `:449-473`). 정상 preview/적용 종료에서는 정리하지만, 중간 복호화 실패 경로에서는 이미 복호화된 앞선 username 배열 정리가 보장되지 않는다.
- `decryptCredentials()`는 username을 먼저 복호화한 뒤 password 복호화 실패 시 username을 명시적으로 지우는 별도 보상 경계를 갖는다(`:845-871`). CSV 경로에는 같은 단계별 보상 정리가 없다.
- 이번 검토는 코드와 호출부만 읽었고, 실제 학생 자격정보·DB·Keystore 오류를 사용하거나 출력하지 않았다.

#### 추론

- Keystore 키 손상/삭제, 암호화 버전 불일치, DB 레코드 손상 등으로 중간 학생의 username 복호화가 실패하면, 앞서 복호화된 username이 GC 전까지 일반 JVM/ART 힙에 남을 수 있다.
- 오류가 자주 발생하는 정상 경로는 아니므로 P3으로 분류한다. 그러나 CSV 원문을 지우는 정책과 별개로 기존 학생 자격정보가 오류 경로에서 누락 없이 wipe된다는 보장은 깨진다.

#### 가정과 미검증

- 실제 Keystore/DB 불일치가 A에서 발생했는지, 해당 객체가 힙에 얼마나 오래 남는지는 확인하지 않았다.
- ART의 GC·힙 덤프 접근 권한과 재시작 시 정리 효과는 분석 범위를 넘는다.
- 현재 코드의 `cipher.decrypt()`가 어느 학생에서 실패할지는 데이터·키 상태에 의존한다.

#### 안전한 확인 절차

- `StudentRepository`의 `map`/`try-finally` 구조와 `decryptCredentials()`의 비교 가능한 정리 경계를 정적 검토했다.
- 빌드·테스트·DB 접근·자격정보 입력/복호화는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 기존 username을 한 건씩 복호화할 때 성공한 배열도 다음 복호화 실패 시 모두 zeroize된다.
- 실제: 전체 `map`이 성공해야만 정리용 `finally`가 설치되므로 중간 예외 시 앞선 배열 정리 경로가 없다.

#### 영향

- 보안/개인정보: 학생 매쓰홀릭 username이 오류 경로에서 메모리에 잔류할 수 있다. 비밀번호는 이 `map`에서 복호화하지 않는다.
- 사용자/운영: CSV preview/import가 실패할 때 관리자에게 오류가 표시되지만, 메모리 정리까지 실패폐쇄되는지는 보장되지 않는다.
- 데이터: DB 변경이 transaction 전에 실패하면 이번 경로에서 부분 적용은 확인되지 않았다.

#### 근본 원인 후보

- 여러 학생의 임시 복호화 결과를 먼저 `map`으로 만들고 한 번에 정리하는 구현을 선택했으며, 중간 실패를 위한 부분 결과 소유권/보상 정리를 분리하지 않은 것으로 보인다.

#### 반대 가설·오탐 검토

- `finally`가 코드에 존재한다는 사실은 성공한 `map` 이후의 정리만 보장한다. Kotlin `map`의 람다 중간 예외는 결과 리스트 대입과 바깥 `try` 진입 전에 전파되므로 해당 `finally`가 실행된다고 볼 근거가 없다.
- 모든 정상 자격정보 복호화가 성공하면 finding은 나타나지 않지만, 보안 정리 결함은 예외 경로에도 정의돼야 한다.

#### 기존 테스트가 잡지 못한 이유

- 기존 repository/CSV 테스트는 정상 import와 형식 오류를 주로 다루며, 기존 학생 목록 중간 레코드의 복호화 예외 뒤 앞선 `CharArray` wipe를 관찰하는 테스트 계약을 확인하지 못했다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 학생별 복호화·비교 후 즉시 wipe, 또는 부분 결과를 소유하는 `try/finally` 누적 구조로 바꾸고 preview/import 두 경로를 공통화한다.
- 정상 import/preview, 첫 학생 실패, 중간 학생 실패, 마지막 학생 실패와 키/버전 오류에서 username·CSV row·password가 모두 지워지는지 검증해야 한다.
- 이번 Goal에서는 소스·테스트를 수정하지 않는다.

#### 관련 발견·결정

- `LUNA-0004`는 `LUNA-0002`의 PC secret at-rest 문제와 무관한 Android 메모리 정리 결함이다.
- 실패 시 자격정보 wipe 기준과 테스트 추가 범위는 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0005 — QR 인증 뒤 관리자 수동 학생 선택과 지연 로그인 콜백이 중복 세션 준비를 예약할 수 있음

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 06:04~09:52:02 (Asia/Seoul)
- 영향 모듈·버전: `kiosk` `MainActivity`의 QR 인증·세션 관리자·수동 학생 선택 흐름

#### 사실

- QR 대기 진입 시 `MainActivity.showScanner()`가 `qrAcceptanceGeneration`을 증가시키고(`:3046`), QR 검증 성공 시 다시 증가시킨 뒤(`:3382`) 2초 지연 콜백을 예약한다. 이 콜백은 `scannerVisible`, `destroyed`, 그리고 당시 generation이 현재 generation과 같은지만 확인한 뒤 `launchSecureWebSession(student)`를 호출한다(`:3394-3402`).
- `session_admin_button`은 QR 스캐너 레이아웃에 계속 존재하고(`kiosk/src/main/res/layout/activity_main.xml:549-557`), 리스너는 pending QR 로그인 상태를 취소하거나 `qrAcceptanceGeneration`을 무효화하지 않은 채 `requestSessionAdminAuthentication()`을 연다(`MainActivity.kt:527-534`).
- 관리자 인증 성공 뒤 `showAuthenticatedSessionActions()`의 “학생 수동 선택”은 `manualStudentSelectionFlowActive`를 설정하고 roster를 읽는다(`:3693-3710`). 수동 학생 검증 성공 경로도 별도의 2초 지연 콜백에서 `!destroyed && scannerVisible`만 확인하고 `launchSecureWebSession(student)`를 호출하며, QR generation 또는 공용 single-flight 토큰을 확인하지 않는다(`:3773-3792`).
- `ioExecutor`는 단일 스레드이지만(`:161`), 각 `launchSecureWebSession()`은 `QR_READY → PRELOGIN_CHECK` 전이를 기대한다(`:3428-3436`). 한 경로가 먼저 전이를 완료하면 다른 경로는 예외가 될 수 있고, `launchSecureWebSession()`의 `onFailure`는 stale callback 여부와 무관하게 `lockAfterBridgeFailure("CREDENTIAL_PREPARATION")`을 호출한다(`:3468-3470`).
- `validateForActiveSession()`과 `validateManualStudentForActiveSession()`은 먼저 `sessionDao().get()`으로 session ID/class ID/state를 읽고, 이후 별도의 StudentDao query와 audit·`markUsed()`를 수행한다(`StudentRepository.kt:729-810`). 이 검증 묶음은 `endSession()`의 transaction과 같은 세대 잠금으로 묶이지 않는다.
- `completeSessionEnd()` 성공 경로는 `StudentRepository.endSession()`으로 singleton session과 `session_students`를 정리한 뒤 `refreshAdminData()`를 호출하고, 새 수업은 다시 `startSession()`·`showScanner()`로 QR 대기를 연다(`MainActivity.kt:2852-2868`, `:2824-2845`, `:3031-3058`). `launchSecureWebSession()`의 후속 `transitionSession()`은 `expectedState = QR_READY`만 확인하고 검증 당시 session ID를 인자로 비교하지 않는다(`MainActivity.kt:3428-3436`, `StudentRepository.kt:874-899`).
- 일반 학생 Web Activity 결과 callback도 `webSessionLauncher`에서 passed/failure만 읽어 `persistWebSessionResult()`로 넘기며, 호출 당시 session ID를 저장하지 않는다(`MainActivity.kt:225-233`, `:240-263`). `WebSessionResultPersistence.persist()`는 전달된 `persistTransition`을 실행한 뒤 현재 session을 다시 읽을 뿐이고, 실제 `StudentRepository.transitionSession()`은 현재 singleton의 `PRELOGIN_CHECK` expected-state만 검사한다(`WebSessionResultPersistence.kt:8-20`, `StudentRepository.kt:874-899`). 따라서 S1의 Web 결과가 S1 종료·S2 시작 뒤 도착하면 QR/수동 검증 stale launch와 같은 세대 결합 공백이 결과 전이에도 이어진다.
- 수동 경로 자체는 `manualStudentSelectionFlowActive`의 생존 여부나 `statusText == QR_READY`도 지연 시점에 확인하지 않는다. 따라서 QR 승인 직후 `session_admin_button`에서 수동 선택을 시작할 수 있는 타이밍에는 QR 지연 콜백이 기존 `qrAcceptanceGeneration`으로 살아 있는 동안 수동 검증도 별도 launch를 예약할 수 있다.
- `loadManualStudentChoices()`와 `validateManualStudent()`의 UI callback은 `destroyed`·`scannerVisible`만 확인하고 session ID나 generation을 저장·비교하지 않는다(`MainActivity.kt:3721-3792`). 각 학생 검증 DAO가 현재 DB의 `QR_READY`·class/session·활성 조건을 다시 확인하는 완화책은 있지만, 이전 session의 목록/검증 callback이 새 `QR_READY` 화면에서 재개될 수 있는 stale UI/launch 경계는 남는다.
- 특히 S1의 QR/수동 검증이 I/O executor에서 진행되는 동안 S1을 종료하고 S2를 시작하면, 검증 결과가 S2의 `scannerVisible == true` 화면에 도착할 수 있다. 결과 객체에는 S1의 학생이 들어 있지만 `launchSecureWebSession()`은 S2의 현재 `QR_READY` 상태만 기대하므로, 세대 재확인 없이 S2의 `PRELOGIN_CHECK`로 진행할 수 있는 교차 세션 stale launch 순서가 남는다. 이는 이 finding의 기존 stale callback 범위를 구체화한 것이며 독립 finding으로 중복 집계하지 않는다.
- 같은 교차 세대 순서는 S1 Web Activity의 늦은 `RESULT_OK`/`RESULT_CANCELED`에도 적용될 수 있다. Kiosk가 S1을 종료하고 S2를 `PRELOGIN_CHECK`까지 진행한 뒤 이전 Web 결과 callback을 소비하면, passed 결과는 S2를 `QR_READY`로, 실패 결과는 S2를 `LOCKED`로 전이할 수 있으며, callback 자체에는 S1의 session ID나 launch generation 검사가 없다. 이 역시 지연 로그인 callback의 단계만 다른 동일한 `LUNA-0005` 범위로 유지한다.
- 같은 관리자 메뉴에서 “관리자 화면 열기”를 선택하면 pending QR/prelogin 구간에도 학생 관리 화면이 열리고, 학생 비활성화 제어는 `currentSession` 상태와 무관하게 활성 학생이 있는 동안 enabled다(`MainActivity.kt:1733-1746`, `showAuthenticatedSessionActions()`). `deactivateStudent()`는 QR hash와 `isActive`를 갱신하지만 이미 예약된 launch callback을 취소하지 않으므로, 이 상호작용은 QR·수동 callback 경합의 추가 재현 순서로 확인해야 한다. 정책상 수업 중 비활성화를 허용할지 별도 제품 결정이 없어 독립 finding으로 분리하지 않았다.
- 기존 `PreparedWebSessionPolicyTest`는 화면 이탈·destroyed·단일 launch 결정만 검증하며, QR/수동 지연 콜백의 중복 예약·순서 역전이나 외부 Web Activity 결과가 S1 종료·S2 시작 뒤 도착하는 session ID/generation 검사를 검증하지 않는다.

#### 추론

- QR 인증 성공 후 원래의 2초 콜백이 아직 실행되지 않은 짧은 구간에 관리자가 PIN 인증과 수동 학생 선택을 완료하면, 두 경로가 모두 `scannerVisible == true`를 관찰하고 같은 세션에 대한 준비 작업을 예약할 수 있다.
- 단일 I/O executor와 `expectedState` 검사는 두 경로가 동시에 성공하는 것을 제한하지만, 첫 경로가 Web 세션을 시작한 뒤 두 번째 stale 경로가 실패하는 구조 자체를 제거하지 않는다. 두 번째 실패가 `lockAfterBridgeFailure()`를 통해 세션을 잠그면 Web 화면이 이미 열린 상태와 Kiosk의 `LOCKED` 상태가 어긋날 가능성이 있다.
- 실제 타이밍 재현 전이므로 확정이 아니라 후보로 둔다. 다만 지연 콜백 무효화 조건과 실패 부작용은 소스에서 직접 확인된다.

#### 가정과 미검증

- 실제 A 화면에서 PIN 입력·관리자 메뉴·수동 학생 선택을 조합한 타이밍은 실행하지 않았다.
- 첫 번째 준비가 정확히 언제 `scannerVisible = false`를 설정하는지와 Activity/Web 전환 중 callback 순서는 라이브 환경·스케줄러 상태에 의존한다.
- 학생 자격정보·세션 DB·Web 화면은 사용하지 않았고, 빌드·테스트도 실행하지 않았다.

#### 안전한 확인 절차

- QR·관리자·수동 선택 관련 Kotlin/XML과 기존 정책 테스트만 읽기 전용으로 대조했다.
- 실제 재현은 답안·로그인·세션 상태를 변경할 수 있으므로 이번 Goal의 읽기 전용 계약상 수행하지 않는다.

#### 기대 결과와 실제 결과

- 기대: 한 QR 인증 또는 한 수동 선택만 세션 준비를 예약하고, 관리자가 흐름을 바꾸면 이전 지연 콜백과 외부 Web 결과가 세션 ID/generation에 묶여 무효화된다.
- 실제: QR 경로는 generation으로 보호되지만 관리자/수동 선택 진입이 그 generation을 무효화하지 않고, 수동 경로와 `webSessionLauncher` 결과에는 같은 세대 binding이 없다.

#### 영향

- 사용자/운영: 좁은 타이밍에서 정상 학생 인증 뒤 로그인 화면과 Kiosk의 잠금/복구 화면이 엇갈리거나, 관리자가 다시 인증해야 하는 상태가 될 수 있다.
- 데이터/보안: 현재 정적 근거만으로 중복 학생 등록·자격정보 노출을 확인하지 않았다. 세션 상태 불일치와 준비된 credential handle의 회수 경계는 추가 확인이 필요하다.

#### 근본 원인 후보

- QR 지연 callback에만 generation을 적용하고, 관리자·수동 학생 선택·외부 Web 결과를 포함한 세션 launch 전체를 하나의 취소 토큰 또는 session-bound single-flight 상태로 묶지 않은 것으로 보인다.

#### 반대 가설·오탐 검토

- 단일 executor와 DB의 기대 상태 검사가 두 번째 준비를 실패시킬 수 있어 “두 번 모두 Web에 진입한다”고 단정할 수는 없다.
- 그러나 실패 callback이 stale 작업인지 확인하지 않고 잠금 부작용을 수행하는 별도 경로가 있으므로, 단일 상태 전이가 존재한다는 이유만으로 후보를 기각하지 않는다.
- 세션 종료가 먼저 완료되면 `transitionSession()`이 실패할 수 있고, 새 세션이 이미 `QR_READY`이면 expected-state만으로 통과할 가능성이 있다. 실제 종료·재시작·I/O 순서와 Web 화면 전환은 실행하지 않았으므로 영향은 후보로 유지한다.

#### 기존 테스트가 잡지 못한 이유

- 현재 정책 테스트는 `PreparedWebSessionPolicy.decide()`의 화면 상태만 다루고, delayed callback의 generation 무효화·수동 선택 전환·외부 Web 결과의 session binding·expected-state 실패 후 UI/DB 부작용을 관찰하지 않는다. `RepositoryInstrumentedTest`의 session lifecycle 시험도 검증 완료 후 종료·재시작을 순차 실행할 뿐, in-flight QR/수동 검증 또는 Web 결과가 S1 종료·S2 시작을 가로지르는 순서는 검사하지 않는다(`RepositoryInstrumentedTest.kt:266-339`).

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 관리자/수동 선택 진입 시 QR pending callback을 명시적으로 취소하고, QR·수동 경로가 공유하는 launch generation 또는 single-flight gate를 둔다.
- 각 준비 작업에 operation token을 부여해 stale `onFailure`가 현재 세션을 잠그지 않도록 하고, credential handle·세션 상태·Web intent의 소유권을 같은 토큰으로 묶는다.
- QR 승인 후 관리자 진입, 관리자 수동 선택, 원래 QR callback 선행/후행, 사용자가 취소·화면 이탈한 경우와 Web launch 실패를 순서별로 검증해야 한다. 이번 Goal에서는 소스·테스트를 수정하지 않는다.

#### 관련 발견·결정

- `LUNA-0005`는 `LUNA-0003`의 PC CSV queue 소실이나 `LUNA-0004`의 자격정보 wipe 문제와 별개인 Kiosk UI callback 경쟁 후보다.
- 실제 재현 여부와 single-flight·stale failure 처리 방식은 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0006 — QR 발급·비활성화·CSV 경로에서 인증용 hash 배열 회수 계약이 불완전함

- 심각도: `P3`
- 상태: `확정`
- 신뢰도: 높음
- 최초 발견·마지막 확인: 2026-08-02 06:08~06:27 (Asia/Seoul)
- 영향 모듈·버전: `kiosk` `QrTokenCodec`, `StudentRepository`의 단건·batch·비활성화·CSV 경로 및 관리자 QR 표시 흐름

#### 사실

- `QrTokenCodec.issue()`는 32바이트 난수 token으로 payload와 SHA-256 `hash`를 만들고, 원본 token 배열은 `finally`에서 zeroize한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/qr/QrTokenCodec.kt:32-40`).
- `StudentRepository.registerStudent()`는 `issued.hash`를 Room `StudentEntity.qrTokenHash`에 전달한 뒤 `RegisteredStudent(studentId, issued)`를 반환하지만, 성공 반환 직전 `issued.hash.fill(0)`을 하지 않는다(`StudentRepository.kt:121-153`). `MainActivity.registerStudent()`는 반환 객체에서 payload만 QR bitmap 렌더링에 사용하고 hash를 wipe하지 않는다(`MainActivity.kt:1357-1362`).
- `StudentRepository.reissueQr()`도 새 `IssuedQrToken`의 hash를 DB에 전달하고 그대로 반환하며 정리 경계가 없다(`StudentRepository.kt:160-181`). `MainActivity.reissueQr()` 역시 payload만 렌더링한다(`MainActivity.kt:1413-1417`).
- `StudentRepository.reissueClassQrBatch()`와 `reissueQrBatch()`는 transaction 실패 시 내부에서 모든 hash를 지우고, 성공 후 호출부가 렌더링한 각 항목의 hash를 지우는 별도 패턴을 사용한다(`StudentRepository.kt:184-231`, `:236-280`; `MainActivity.kt:1954`, `:2064`). 이와 비교하면 단건 성공 경로의 hash 소유권이 명시적으로 회수되지 않는다.
- `preparePendingCardsPdf()`의 일괄 경로도 각 render 성공 뒤에만 해당 hash를 지우며, render가 중간에 실패하면 아직 처리하지 않은 `issuedQr.hash`를 전체적으로 wipe하는 `catch/finally`가 없다(`MainActivity.kt:1945-1961`). 이는 같은 소유권 불일치의 예외 확장 경로다.
- CSV 신규 학생 경로는 `qrCodec.issueHashOnly()`가 반환한 hash를 `StudentEntity.qrTokenHash` 생성자에 직접 전달하고 별도 zeroize하지 않는다(`StudentRepository.kt:347-369`). 학생 비활성화도 `revokedReplacementHash`를 생성해 DB update에 전달한 뒤 성공·실패 공통 `finally`가 없다(`:619-633`).
- `QrTokenCodecTest`는 payload 형식·hash 길이·파싱 결과를 검증하지만, 발급 반환 후 성공·렌더 실패·DB 실패에서 hash 배열이 zeroize되는 계약은 검증하지 않는다.
- `SECURITY.md:64-65`는 원격 QR 경로에서 QR 원문 대신 전달한 32바이트 hash를 실패·완료 경로에서 지운다고 보안 기준으로 명시한다. 위 단건·CSV·비활성화·batch render 예외 경로의 공통 zeroize 누락은 이 기준을 모든 QR 관련 호출 경로에 적용할 수 없게 한다.

#### 추론

- 원본 QR token 자체는 `QrTokenCodec.issue()` 안에서 지워지지만, DB lookup에 사용되는 인증용 hash가 발급·CSV import·비활성화 경로에서 일반 Kotlin/ART 힙에 남아 객체가 회수될 때까지 유지될 수 있다.
- hash만으로 원본 QR을 현실적으로 복원할 수 있다는 근거는 없으므로 QR 원문 노출이나 즉시 인증 우회를 뜻하지 않는다. 그러나 코드가 batch 경로에서 명시적으로 hash를 wipe하고 있고 QR hash를 민감정보로 취급하는 전제가 있으므로, 단건 경로의 잔류는 보안 정리 불변조건 위반으로 P3 분류한다.

#### 가정과 미검증

- Room의 SQLite binding이 DB에 값을 복사한 뒤 원본 배열을 변경해도 저장값에 영향을 주지 않는다는 일반적인 동작을 전제로 정적 소유권을 판단했다. 실제 Android runtime/Room 버전에서의 메모리 복사 시점은 실행하지 않았다.
- GC가 배열을 언제 회수하는지, heap dump 접근이 가능한지, 실제 QR hash가 외부로 노출됐는지는 확인하지 않았다.
- 실제 학생 등록·재발급·PDF 생성·인쇄를 수행하지 않았으며 QR 원문·hash를 출력하거나 기록하지 않았다.

#### 안전한 확인 절차

- `QrTokenCodec`, `StudentRepository`, `MainActivity`의 발급·렌더링·batch cleanup 코드와 기존 테스트만 읽기 전용으로 대조했다.
- 소스·DB·Keystore·A 화면에는 변경을 가하지 않았다.

#### 기대 결과와 실제 결과

- 기대: DB에 저장해야 하는 hash의 ownership이 저장 직후 repository 또는 호출부 한 곳에 명확히 귀속되고, 성공·예외·렌더 실패 모든 경로에서 zeroize된다.
- 실제: 단건 `registerStudent()`/`reissueQr()` 반환 객체와 호출부에 hash 회수 경계가 없고, 선택 batch PDF도 중간 render 실패 시 미처리 hash 전체 정리가 보장되지 않는다.

#### 영향

- 보안/개인정보: QR 인증 lookup material이 정상 관리자 작업·CSV import·비활성화 뒤에도 힙에 남는 시간이 불필요하게 늘어난다.
- 운영/데이터: DB 저장값 자체의 손상이나 QR 기능 실패는 이번 정적 검토에서 확인하지 않았다.

#### 근본 원인 후보

- `IssuedQrToken`이 payload와 DB용 hash를 함께 반환하면서, repository와 UI 중 누가 hash를 지울지 API 계약이 명시되지 않았다. batch 경로는 호출부가 임시로 wipe하고 단건 경로는 이를 누락한 것으로 보인다.

#### 반대 가설·오탐 검토

- 난수 원문 token은 이미 지워지고 hash는 단방향 값이므로 직접적인 QR 원문 노출 finding으로 확대하지 않는다.
- 정상 함수 반환 직후 객체가 곧 GC될 수 있지만, GC 시점을 보안 정리 보장으로 볼 수 없고 동일 모듈에 명시적인 wipe 패턴이 존재한다.

#### 기존 테스트가 잡지 못한 이유

- 기존 테스트는 token 형식·hash 일치와 parser 동작을 다루며, mutable `ByteArray` ownership과 예외 경로의 zeroize 여부를 관찰하지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 repository가 DB write 완료 뒤 hash를 지우고 payload만 별도 반환하거나, `IssuedQrToken`을 payload와 wipe 가능한 hash owner로 분리하는 API 계약을 정한다.
- 단건 register/reissue 성공·DB 실패·렌더 실패, batch PDF render 실패와 batch print 실패에서 모든 임시 hash 배열이 zeroize되는지 검증한다. DB 저장값은 그대로 lookup 가능해야 한다.
- 이번 Goal에서는 소스·테스트를 수정하지 않는다.

#### 관련 발견·결정

- `LUNA-0006`은 `LUNA-0004`의 username 복호화 배열 누락과 같은 “예외/성공 경로의 민감 배열 ownership” 계열이지만 대상은 QR 인증 hash이며 단건·CSV·비활성화 경로까지 포함하는 별도 수명 문제이므로 중복 finding으로 합치지 않는다.
- hash zeroization API 계약과 단건·batch 경로 통합은 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0007 — PC receiver의 인증 전 연결·이벤트 queue에 자원 상한이 없음

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 06:15 (Asia/Seoul)
- 영향 모듈·버전: `pc_receiver` `ThreadedReceiverServer`/`_ReceiverHandler`

#### 사실

- `ThreadedReceiverServer`는 `socketserver.ThreadingTCPServer`를 상속하고 `daemon_threads = True`만 설정하며, active connection 수를 제한하는 semaphore·worker pool·IP rate limit은 없다(`pc_receiver/src/matholic_pdf_receiver/server.py:251-259`). `request_queue_size`도 이 클래스에서 명시하지 않아 inherited Python/OS backlog의 실제 상한과 active worker 상한이 별도인데, source 수준의 application budget은 없다.
- `_ReceiverHandler.handle()`는 인증 전에 socket timeout을 10초로 설정하고, magic·header·body를 `_read_exact()`로 순차 수신한다(`server.py:185-224`). `_read_exact()`에는 monotonic 전체 요청 deadline이 없어 각 `recv()` 사이에 바이트가 조금씩 도착하면 전체 연결 수명이 10초를 넘을 수 있다. 최대 control payload는 약 1MiB, PDF frame은 최대 5MiB까지 protocol header가 허용한다.
- body를 `_read_exact()`가 `chunks`로 모아 `bytes`로 결합한 뒤 handler가 `frame = header + body`를 만들므로, 인증 전에도 body와 결합 frame이 겹쳐 존재할 수 있다. 이후 `decode_request()`/`decode_control_request()`가 실행되는 동안의 파생 plaintext와 crypto buffer는 연결별 worker가 보유한다.
- `ThreadingMixIn`의 연결별 worker는 `decode_request()`/`decode_control_request()`와 저장·config save까지 수행한다. 페어링 secret이 없는 연결도 인증 오류가 나기 전까지 thread와 socket 자원을 점유할 수 있다.
- `pc_receiver/src/matholic_pdf_receiver/app.py:38`의 `events = queue.Queue()`는 기본 `maxsize=0`인 무제한 queue이고, 서버의 모든 `on_event`가 `events.put`으로 넣는다(`app.py:38-39,229-247`). Tk `_poll_events()`는 200ms 주기로 `queue.Empty`가 될 때까지 무제한 `while`로 drain하며 producer rate limit·queue 상한·event coalescing·cycle budget이 없다. 인증 실패/partial timeout도 event 하나를 만들고, 인증된 status/PDF 요청을 빠르게 보내면 연결 thread와 별개로 UI event queue와 한 번의 UI callback 실행 시간이 누적될 수 있다.
- Windows 방화벽은 Private profile과 TCP 48129로 범위를 줄이지만, 같은 LAN의 비인가 host가 TCP 연결을 시도할 수 있다는 보안 경계는 남는다. protocol은 PDF 5MiB/control 1MiB와 timestamp/authentication을 검사하지만 이 검사는 full frame read 및 worker 할당 뒤에 적용된다. inherited TCP listen backlog가 유한할 가능성은 있으나 실제 Python/Windows backlog와 active worker 동작은 확인하지 않았다.
- 기존 테스트는 정상 PDF 한 건, status와 CSV control, protocol vector 및 정상 server shutdown만 다루고 partial/slow connection 다수, worker 수, queue 길이, UI drain starvation을 다루지 않는다.

#### 추론

- 공격자 또는 오작동한 LAN client가 header/body를 천천히 보내 각 `recv()` inactivity timeout 전에 한 바이트씩 진행시키거나 여러 partial connection을 열면 Python thread·socket·메모리 자원을 누적시켜 정당한 A 요청을 지연시킬 수 있다. 인증 실패 frame도 full frame read 뒤 event를 만들며, pairing secret을 가진 오작동·복제 client가 status 이벤트를 producer보다 빠르게 만들면 무제한 UI queue와 무제한 drain callback이 같은 가용성 문제를 키울 수 있다.
- AES-GCM 인증과 firewall은 데이터 위조·외부 노출을 줄이지만, 인증 전 자원 고갈은 막지 않는다. 실제 OS thread limit·네트워크 환경·동시 연결 수는 실행하지 않아 후보로 둔다.

#### 가정과 미검증

- Private network에 공격 가능한 다른 host가 존재하고 receiver가 계속 실행 중이라는 threat model을 적용했다.
- 실제 연결 flood/slowloris, CPU/memory/thread/queue 영향, Windows firewall·TCP backlog 동작은 수행하지 않았다.
- 정상 운영에서 동시에 필요한 연결 수, 허용 가능한 전체 request deadline, backlog와 timeout은 운영 수치로 확정하지 않았다.

#### 안전한 확인 절차

- Python server/socketserver 상속, `_read_exact()` timeout/read 경계, protocol size bound, `app.py` event producer/drain과 기존 server/protocol/app test를 읽기 전용으로 대조했다.
- README·RELEASE_OPERATIONS·BUILD_VERIFICATION의 firewall·5MiB/1MiB·정상 startup/shutdown 기록과 관련 Git history도 대조했다.
- 네트워크 부하·slow connection·소켓 생성·수신기 실행은 계약상 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 인증 전 연결도 bounded resource로 처리되어 비인가 partial connection이 정당한 PDF/CSV 요청을 고갈시키지 않는다.
- 실제: 연결마다 thread가 생기고 per-recv 10초 inactivity timeout 외 전체 request deadline·active connection 상한이 없으며, full frame read 전 1MiB/5MiB까지 연결별 buffer가 생긴다. Tk event queue에도 `maxsize`·rate limit·drop/coalesce·drain budget 정책이 없다.

#### 영향

- 가용성: PC 수신기 자체 또는 정상 Kiosk 전송이 지연·timeout될 수 있다.
- 데이터/인증: 이번 정적 검토에서 인증 우회·PDF 변조·학생 데이터 변경은 확인하지 않았다.

#### 근본 원인 후보

- `ThreadingTCPServer`를 그대로 사용하면서 인증 전 connection admission·worker budget·누적 request deadline을 application lifecycle에 정의하지 않은 구조다.
- 서버 event를 무조건 `queue.Queue.put`하고 UI에서 `queue.Empty`까지 모두 소비하는 단순 전달 계약이어서, 실패·status event의 producer 속도와 UI 처리 예산이 분리되어 있지 않다.

#### 반대 가설·오탐 검토

- Private firewall, pairing authentication, timestamp 및 PDF/control payload size bound는 외부·위조 요청의 범위와 연결당 메모리를 제한한다. TCP listener의 inherited backlog도 유한할 수 있어 “모든 연결이 무조건 accept된다”고 주장하지 않는다.
- 정상 Kiosk는 단일 request/response를 사용하고, 기존 2026-07-29 운영 기록과 0.1.3 pytest/smoke는 정상 전송·정상 종료를 확인한다. 따라서 실제 서비스 장애나 재현된 resource exhaustion으로 상태를 올리지 않고, 느린 partial/flood와 queue backlog의 미검증 후보로 제한한다.

#### 기존 테스트가 잡지 못한 이유

- `test_tcp_receiver_returns_authenticated_ack()`는 한 번의 완전한 PDF frame과 정상 `server.shutdown()`/`server_close()`만 확인한다.
- `test_receiver_accepts_status_and_serves_csv_once()`와 protocol vector는 정상 authenticated control/CSV 내용을 확인하지만, partial header/body, cumulative read deadline, concurrent handler 수, `events` queue length, `_poll_events()` drain budget을 관찰하지 않는다.
- `test_app.py`는 `--smoke-check` dispatch만 검증하며 listener admission·UI polling·resource failure 경계를 연결하지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 bounded executor/semaphore, explicit accept backlog·per-client budget, 인증 전 header/body의 inactivity 및 cumulative deadline, 연결별 memory budget을 정의하고, status/error event queue에는 bounded/coalescing/drop 정책과 UI drain budget을 정한다. inherited backlog의 플랫폼 차이와 정상 PDF/CSV 처리 우선순위도 함께 결정해야 한다.
- 정상 PDF/CSV/상태 요청이 제한 안에서 처리되고, partial unauthenticated connections가 일정 수를 넘으면 즉시 닫히는지 Windows Private profile에서 검증해야 한다. 이번 Goal에서는 수정·부하 시험을 하지 않는다.

#### 관련 발견·결정

- `LUNA-0007`은 `LUNA-0002`의 secret at-rest와 별개인 인증 전 availability 경계이며, 네트워크 암호화가 존재한다는 이유로 기각하지 않는다.

### LUNA-0008 — 5분 timestamp 창보다 짧아질 수 있는 고정 replay ID cache가 오래된 frame 재수용을 허용할 수 있음

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 06:15 / 2026-08-02 19:26:27 (Asia/Seoul)
- 영향 모듈·버전: `pc_receiver` `ReceiverConfig.remember_request()`와 PDF/control replay guard

#### 사실

- protocol은 request timestamp가 현재 시각에서 `MAX_CLOCK_SKEW_SECONDS = 300` 이내인지 확인한다(`pc_receiver/src/matholic_pdf_receiver/protocol.py:29`, `:231-234`, `:425-428`).
- `ReceiverConfig.replay_ids`는 `MAX_REPLAY_IDS = 2048`개의 hex request ID만 보관하고, 초과 시 가장 오래된 ID를 삭제한다(`config.py:17`, `:60-66`). 각 ID의 timestamp·expiry는 저장하지 않는다.
- PDF와 control 모두 `ReceiverState`가 replay ID를 확인·기록하지만, 오래된 ID가 eviction된 뒤에는 같은 authenticated frame도 새로운 요청처럼 저장·처리될 수 있다(`server.py:81-95`, `:130-153`).
- `ConfigStore.load()`는 저장된 `replay_ids`를 다시 읽고, `ConfigStore.save()`는 replay list를 포함한 JSON을 임시 파일에 쓴 뒤 `os.replace`로 원자 교체한다. 따라서 정상 저장·재시작에서는 최근 ID가 보존되지만, 저장 구조 자체에는 ID별 timestamp·expiry가 없다(`config.py`).
- `ReceiverState.accept_control()`은 `decode_control_request()` 뒤 `ReceiverState.lock` 안에서 replay ID를 `remember_request()`하고 설정을 저장한 다음 operation별 상태/CSV 분기를 평가한다. protocol이 허용하는 unsupported operation이나 operation-specific failure도 이 순서에 도달하면 실제 업무 효과 전 ID를 소비·eviction할 수 있다(`server.py`, `protocol.py`).
- PDF와 control의 replay check·record·save는 같은 `ReceiverState.lock`으로 직렬화되고, PDF는 파일 commit과 설정 저장, control은 설정 저장과 response/event 경계까지 lock을 보유한다. 이는 같은 process의 동시 동일 frame 중복을 제한하는 반대 근거지만, cache를 5분 안에 넘길 수 있는 실제 처리량·저장 지연은 계측되지 않았다.
- Kiosk `PcPdfSender`·`PcControlClient`는 호출별 random request ID와 strict response binding을 사용하고 내부 자동 retry는 하지 않는다. endpoint resolver의 병렬 status probe와 사용자의 재시도는 새 ID를 만들 수 있지만 정상 경로의 accidental same-ID replay 반대 근거이며, PDF ACK/재시도 중복은 `LUNA-0010` 범위로 유지한다.
- 현재 테스트는 같은 frame의 즉시 재전송 거부를 확인하지만, 2,049개 이상의 유효 요청으로 cache를 넘긴 뒤 timestamp 창 안에서 옛 frame을 재전송하는 경계를 다루지 않는다.
- README·운영 문서는 5분 stale request와 replay rejection을 설명하지만 2,048개 cache capacity, expiry 정책, timestamp window와의 관계를 정의하지 않는다. 현재 test/history에는 cache overflow, timestamp-age, 정상 restart/reload, save failure, concurrent duplicate, unsupported/malformed control consumption 회귀가 없다.

#### 추론

- 기존 frame의 ID가 cache에 남아 있는 상태에서 pairing secret을 가진 client/공격자가 300초 안에 약 2,048개의 신규 유효 frame을 처리시키면, 약 `2048 / 300 = 6.83`개 frame/초에서 아직 timestamp 창 안인 예전 captured frame의 ID가 eviction될 수 있다. 그 frame은 PDF 중복 저장 또는 control 중복 처리로 이어질 수 있다.
- 오래된 captured frame 하나만으로는 새 ID를 만들어 eviction할 수 없으므로 pairing secret 또는 유효한 frame을 계속 생성할 능력이 전제된다. 정상 운영에서 lock·disk save를 포함한 처리량이 이 조건에 이르는지, secret 노출·의도된 bounded replay 정책·endpoint probe 비율이 어떤지는 확인하지 않아 P4 후보로 유지한다.

#### 가정과 미검증

- 실제 유효 frame 대량 처리나 captured frame 재전송은 secret·파일·네트워크 상태를 사용하므로 실행하지 않았다.
- OS와 storage throughput, 실제 PDF 크기·처리량, cache save 지연, Windows clock jump/재시작·save failure 동작은 측정하지 않았다.
- 정상 Kiosk status/PDF 호출 빈도와 endpoint resolver의 실제 성공 probe 수는 계측하지 않았다. 실제 PDF 중복 저장, control 상태 반복, CSV 재처리는 확인하지 않았다.

#### 안전한 확인 절차

- `config.py`의 count-based eviction·atomic persistence, `protocol.py`의 300초 validation, `server.py`의 PDF/control call site·lock·선기록 순서, Kiosk client/resolver와 기존 replay test, README·운영 문서·Git history를 읽기 전용으로 대조했다.

#### 기대 결과와 실제 결과

- 기대: 허용 timestamp window 안에서는 request ID 재전송이 항상 거부된다.
- 실제: 최근 2,048개 ID만 유지하므로 약 6.83개 신규 frame/초의 조건이 300초 동안 충족되면 timestamp window가 끝나기 전에 ID가 제거될 수 있다. 다만 lock·atomic save·정상 fresh ID client가 존재해 실제 도달성은 미검증이다.

#### 영향

- 데이터/운영: 같은 PDF가 중복 저장되거나 status/control 요청이 재처리될 가능성이 있다. `CONTROL_FETCH_CSV`는 one-shot pending CSV 및 send/parse 실패가 `LUNA-0003` 범위와 겹치며, 이번 감사에서 실제 중복 파일·상태 반복·CSV 적용은 확인하지 않았다.
- 보안: 유효 pairing secret 또는 captured authenticated frame이 전제되며, secret 자체가 없으면 이 경계만으로 요청을 위조할 수 없다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 timestamp와 함께 replay ID expiry를 저장하거나, 최소 replay cache 용량을 허용 처리량과 5분 창에 맞춰 산정한다. 정상 restart/reload, atomic save failure, wall-clock 변경, unsupported/operation-specific failure가 replay state를 소비할지 정책을 함께 정한다.
- 5분 경계, cache 초과, 재시작, 저장 실패, 동시 중복 PDF/control 요청에서 단일 처리·중복 방지를 검증해야 한다. control은 실제 업무 검증 전에 ID를 소비하지 않을지와 정상 retry semantics를 별도로 정해야 한다. 이번 Goal에서는 수정·대량 요청·테스트 실행을 하지 않는다.

#### 관련 발견·결정

- `LUNA-0008`은 `LUNA-0002`가 실제로 secret을 노출한다는 확정 finding과 결합할 때 threat relevance가 커질 수 있으나, 동일 문제로 합치지 않는다.
- `LUNA-0008`은 PDF ACK 유실·새 request ID 재시도의 중복 저장 판단인 `LUNA-0010`, CSV pending queue 내구성인 `LUNA-0003`, receiver 인증 전 자원 상한인 `LUNA-0007`과 겹치지 않는 replay cache capacity 경계로 유지한다.

### LUNA-0009 — PDF 저장 완료 Windows 알림에 학생 표시 이름이 포함됨

- 심각도: `P3`
- 상태: `확정`
- 신뢰도: 높음
- 최초 발견·마지막 확인: 2026-08-02 06:25 / 2026-08-02 15:57:31 (Asia/Seoul)
- 영향 모듈·버전: Kiosk PC PDF 전송 filename, `pc_receiver` Windows tray notification

#### 사실

- 제품 결정 `docs/PRODUCT_DECISIONS.md:94-95`는 페어링된 지정 PC에 전체 이름을 암호화 전송하되 Windows 알림에는 이름을 표시하지 않도록 명시한다.
- Kiosk `MainActivity.kt:2363`은 QR 미리보기의 `exactName`을 `${preview.exactName} QR.pdf` filename으로 PC receiver에 전송한다. `QrPreview.exactName`은 학생 표시 이름을 담는 필드다(`MainActivity.kt:3955-3958`).
- PC receiver `server.py:228-235`는 저장된 `destination.name`을 `"{destination.name} 저장 완료"` 메시지로 만들고 `notify=True`인 `ReceiveEvent`를 발생시킨다. `safe_pdf_name()`은 위험 문자를 정리·길이 제한하지만 filename 안의 이름 자체를 제거하지 않는다(`server.py:49-52`).
- PC app `app.py:229-240`은 `event.notify`가 참이면 `event.state or event.message`를 `self.tray.notify(..., APP_TITLE)`로 전달한다. PDF 저장 이벤트에는 `state`가 없으므로 앞서 만든 destination filename, 즉 학생 이름이 포함된 메시지가 Windows 알림으로 전달된다.
- 현재 `ReceiveEvent` 생성 경계를 분리하면 PDF 성공 event만 handler에서 명시적으로 `notify=True`이고, status event의 `notify`는 Kiosk payload를 따르며 CSV 성공/실패와 handler 오류 event는 기본값 `False`다(`server.py:67-74`, `:158-177`, `:201-208`, `:243-248`). status는 유효한 `state`가 있으므로 `_poll_events()`가 이름 없는 state를 tray에 보내지만, `status_var`와 PC 창의 상태판에는 `event.message` 전체가 남아 학생 이름을 표시할 수 있다.
- Kiosk의 batch PDF 경로는 `"신규 변경 학생 QR.pdf"`라는 일반 filename을 사용해 batch 저장 완료 toast에는 학생 이름이 들어가지 않는다(`MainActivity.kt:1957-1961`). 현재 확정된 노출은 단건 `preparePcPdfTransfer()`의 exact name filename 경로다.
- `PcControlClient.sendStatus()`는 학생 이름을 암호화된 status payload에 포함할 수 있지만, 이는 제품 결정이 허용한 페어링된 지정 PC 상태판 경계이며 tray payload와 다르다. endpoint resolver의 자동 복구 probe는 `studentName=null`, `notify=false`를 사용한다(`PcEndpointResolver.kt`, `PcControlClient.kt:17-53`).
- 관련 Git history에서 filename을 status/CSV와 함께 tray로 연결한 경로는 `a3aa80c`에서 도입됐고, Windows 알림 이름 비표시 결정은 이후 `d8ca06f`에서 문서로 확정됐다. 그 뒤 해당 filename 재사용을 분리한 source commit은 확인하지 못했다.
- 기존 `pc_receiver/tests`에서 확인되는 것은 protocol/state event와 filename 처리이며, tray notification에 학생 이름이 들어가지 않는다는 assertion이나 app-level notification test는 검색되지 않았다.

#### 추론

- 지정 PC의 수신 폴더에 이름 포함 PDF를 저장하는 제품 의도와 별개로, Windows toast는 작업표시줄·바탕화면·OS 알림 표면에 이름을 주변 노출할 수 있다. 이는 제품 결정이 금지한 별도 표시 경계다.
- 운영 PC를 여러 사람이 보거나 Windows 알림이 잠금 화면에 표시되는 설정이면 학생 표시 이름이 의도한 수신 폴더 경계 밖에 노출될 수 있다. 실제 Windows 알림 표면·잠금 화면 표시 여부는 실행하지 않아 조건부 추론이다.
- status·CSV·error 경로까지 모두 이름을 노출한다고 확대할 근거는 없다. 현재 status tray는 state 우선이고 CSV/error는 tray notify를 요청하지 않으므로, finding의 직접 대상은 PDF 단건 저장 완료 toast다.

#### 가정과 미검증

- `preview.exactName`이 실제 표시 이름이라는 점은 등록·선택·QR preview 호출부와 필드 명칭으로 확인했지만, 실제 학생 값은 사용하지 않았다.
- 이번 Goal에서는 수신기 실행, 실제 PDF 전송, Windows toast 표시, 잠금 화면 설정을 확인하지 않았다.
- 파일명 전체가 보이지 않고 OS가 알림을 숨기는 환경이라도 코드의 알림 payload에는 이름이 포함된다.
- 제품 결정의 “PC 로컬 완료·오류 알림” 항목이 오류 event에도 반드시 Windows toast를 요구하는지는 별도 해석이 필요하다. 현재 오류는 앱 상태 문구에는 들어가지만 `notify=False`이므로, 이를 새 결함으로 확정하지 않고 사용자 재판단 없이 LUNA-0009의 개인정보 범위와 분리한다.

#### 안전한 확인 절차

- 제품 결정, Kiosk filename 생성, receiver event 생성, tray notify 호출과 기존 테스트 검색을 읽기 전용으로 대조했다.
- status/CSV/error의 event constructor와 Kiosk batch/single PDF call site, 관련 Git blame/history와 운영 문서도 읽기 전용으로 대조했다.
- 학생 이름·QR·PDF·실제 알림을 만들거나 출력하지 않았다.

#### 기대 결과와 실제 결과

- 기대: Windows 알림은 학생 이름 없는 일반 문구(예: 카드 PDF 저장 완료)만 표시하고, 이름은 지정 PC의 앱 창·수신 폴더처럼 승인된 경계에만 남는다.
- 실제: 단건 PDF 저장 event의 filename을 포함한 `destination.name 저장 완료`가 tray 알림 message로 전달된다. batch PDF·status·CSV·error의 현재 경로는 각각 일반 filename·state 우선·notify false·notify false로 이 finding과 다르다.

#### 영향

- 개인정보/운영: 학생 표시 이름이 OS 알림을 보는 주변인에게 노출될 수 있다. PDF 본문·QR 자체의 지정 PC 저장 정책과는 별개의 추가 노출이다.
- 데이터/인증: 자격정보·QR 원문·답안·점수 노출이나 인증 우회는 이 finding에서 확인하지 않았다.

#### 근본 원인 후보

- 저장 완료 상태창에 필요한 상세 filename message를 Windows toast에도 재사용하면서, 제품 결정의 “알림 이름 비표시”용 메시지 분리를 구현하지 않은 것으로 보인다.

#### 반대 가설·오탐 검토

- 일반 상태 이벤트는 `event.state`를 우선 사용하므로 status payload의 `student_name`이 이 tray 호출로 직접 표시되는 경로는 확인하지 않았다. 따라서 모든 PC 상태 알림이 이름을 노출한다고 확대하지 않는다.
- PDF filename에 이름이 포함되는 것 자체는 PDF 카드 식별·저장 정책에 포함된 의도된 동작이다. 문제는 그 filename을 Windows toast로 재전달하는 경계다.
- batch PDF filename은 일반 문구이고 CSV/error event는 현재 tray notify를 사용하지 않으므로, 모든 완료·오류 알림이 같은 개인정보 문제를 갖는다고 보지 않는다. PC 창의 상태판에 전체 이름을 표시하는 것은 지정 PC 상태판 정책과 일치하는 별도 UI 경계다.
- `README.md`의 태블릿 외부 알림 제외 문구는 Android 외부 알림 정책으로 보이며, `PRODUCT_DECISIONS.md`의 페어링된 PC 로컬 알림 채택과 직접 충돌하지 않는다. 이 감사에서는 두 문구를 별도 플랫폼 정책으로 해석했다.

#### 기존 테스트가 잡지 못한 이유

- server test는 `ReceiveEvent`와 destination 결과를 확인하지만 `ReceiverApplication._poll_events()`와 pystray 호출을 연결해 PDF/status/CSV/error 알림 payload를 검사하지 않는다. `test_app.py`도 `--smoke-check` dispatch만 확인하고, Kiosk protocol tests는 encrypted vector/filename round-trip만 확인한다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 tray에는 이름 없는 고정 문구를 보내고, 상세 filename은 앱의 지정 PC 창·수신 폴더에만 표시한다.
- PDF 저장 완료, status notify, CSV 결과와 오류 알림을 각각 검증해 이름·학생 자격정보·QR 원문이 Windows toast payload에 포함되지 않는지 확인해야 한다. 기존 수신 폴더 파일명과 앱 상태 표시의 유용성은 보존 여부를 별도로 결정한다.
- 오류 알림을 실제 Windows toast로 추가할지 여부는 제품 결정의 “로컬 완료·오류 알림” 의미를 정한 뒤, 추가한다면 이름·자격정보·QR 원문을 포함하지 않는 별도 고정 payload로 검증해야 한다.
- 이번 Goal에서는 소스·테스트·PC 알림을 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0002`의 PC secret at-rest와는 별도의 개인정보 표시 경계다. `LUNA-0003`의 CSV queue 실패와도 무관하다.
- 알림 문구 분리와 제품 결정 적용 범위는 Sol·사용자 재판단 큐에 남긴다.
- `LUNA-0010`의 PDF ACK/재시도 중복 저장과 달리, `LUNA-0009`는 저장 성공 후 OS 알림 payload의 개인정보 경계다.

### LUNA-0010 — PDF commit·ACK·후속 상태 반영 실패 뒤 사용자 재시도가 PDF를 중복 저장하거나 상태를 어긋나게 할 수 있음

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 06:29~07:29 / 2026-08-02 19:32:42 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `PcPdfSender`/PC PDF 전송 UI, `pc_receiver` PDF commit·ACK·replay 경계

#### 사실

- Kiosk `PcPdfSender.send()`는 한 호출마다 `PcTransferProtocol.encodeRequest()`로 새 random request ID를 만들고, frame을 전송한 뒤 ACK를 읽고 검증해야 성공으로 반환한다(`PcPdfSender.kt:19-49`, `PcTransferProtocol.kt:38-88`). ACK read/verify 예외는 호출 실패로 전파된다.
- PC receiver `ReceiverState.accept()`는 ACK 전송 전에 임시 파일을 `os.replace`로 최종 저장하고 replay ID를 기록·설정 파일에 저장한다(`pc_receiver/src/matholic_pdf_receiver/server.py:91-112`). handler는 그 뒤에야 `sendall(ack)`을 실행한다(`server.py:228-236`).
- Kiosk는 `pcPdfSender.send()` 성공 뒤에만 `studentRepository.markCardsDelivered(...)`를 호출한다(`MainActivity.kt:2363-2367`). 실패하면 같은 preview를 유지한 채 `sendPcPdfButton`을 다시 활성화한다(`MainActivity.kt:2378-2383`).
- `preparePendingCardsPdf()`도 batch PDF 전송 뒤 `studentRepository.markCardsDelivered(studentIds)`를 호출한다(`MainActivity.kt:1956-1962`). remote PC 저장·ACK가 성공한 뒤 이 DB 상태 반영이 실패하면 batch 작업도 UI상 실패로 남고 재실행 경로가 이미 저장된 파일을 다시 만들 수 있다.
- 재시도 호출은 새 request ID를 사용한다. receiver의 중복 방지는 request ID 목록뿐이며(`config.py:60-66`, `server.py:95`), PDF hash·학생·카드 delivery ID를 이용한 중복 저장 방지는 없다. `unique_destination()`은 같은 filename에도 새 시각/순번 경로를 만든다(`server.py:55-64`).
- `ReceiverState.accept()`는 `os.replace()`로 PDF를 확정한 뒤 `remember_request()`와 `ConfigStore.save()`를 수행한다(`server.py:99-103`). 파일 commit 뒤 설정 저장이 예외로 끝나면 `except`가 destination을 삭제하지만 in-memory replay ID를 되돌리지 않아, 같은 request 재시도는 파일이 없어도 replay 거부되고 새 request 재시도는 새 파일을 만들 수 있는 불일치가 생긴다.
- `withReachablePairedPc()`는 `operation(original)`의 모든 `Exception`을 첫 실패로 취급하고, 같은 사설망 후보를 최대 254개까지 인증 probe한 뒤 resolved pairing을 저장하고 `operation(resolved)`를 다시 호출한다(`MainActivity.kt:807-839`, `PcEndpointResolver.kt:79-144`). PDF operation은 이 두 번째 호출에서 새 `PcTransferProtocol` request ID를 생성하므로, 첫 send가 receiver commit 뒤 ACK 단계에서 실패한 경우에도 자동 재전송 조건이 생긴다.
- 단건 `sendPcPdfButton`은 실패 callback에서 같은 `issuedQrPreview`를 유지하고 다시 활성화된다(`MainActivity.kt:2377-2381`, layout `activity_main.xml:463-468`). 전용 “재시도”라는 이름은 아니지만 같은 QR PDF를 다시 보내는 사용자 경로가 존재한다. 반면 batch 실패 뒤 `pendingCardsPdfButton`이 다시 활성화되고 재실행은 `reissueQrBatch()`부터 다시 수행하므로, 첫 전송 파일과 재실행 파일이 같은 QR이라고 가정할 수 없다.
- `markCardsDelivered(studentIds)`는 `qr_card_status`를 `studentId`만으로 update하고 `lastDeliveredAtEpochMs`·`needsPrint`를 바꾼다(`StudentRepository.kt:476-482`, `Daos.kt:109-116`). issued timestamp, QR revision, request ID, PDF hash와 묶이지 않는다. 같은 Activity의 관련 DB 작업은 공통 single-thread `ioExecutor`에 직렬화되는 반대 근거지만, 실행 중 old Activity task·재시작·외부 DB failure와의 binding은 별도 계약이 없다.
- 제품 결정 `docs/PRODUCT_DECISIONS.md:53-56`은 사용자용 취소·재시도 버튼을 만들지 않고 안전한 자동 재시도만 기록하도록 정한다. 운영 문서 `docs/RELEASE_OPERATIONS.md:230-231`은 전송만 다시 해야 할 때 현재 카드 전송 기능을 다시 사용하라고 안내한다. 동일 버튼 재사용을 허용한 것인지, 제품 결정의 사용자 재시도 금지와 충돌하는지는 명시적으로 해소되지 않았다.
- receiver `ConfigStore.save()`는 replay list를 임시 JSON에 쓴 뒤 `os.replace`하지만, `ReceiverState.accept()`의 PDF cleanup은 destination과 PDF `.part`만 정리한다(`config.py:96-115`, `server.py:99-107`). 설정 저장 실패 뒤 config replay state·temporary config file·수신 파일의 원자적 결과와 재시작 동작은 시험되지 않았다.
- 기존 server test는 같은 frame의 즉시 replay 거부를 확인하지만 ACK `sendall` 실패 뒤 새 request ID 재시도와 destination 중복을 검증하지 않는다(`pc_receiver/tests/test_server.py:37-57`). app test는 smoke 옵션 호출만 확인하며 tray/전송 재시도 상태를 다루지 않는다(`pc_receiver/tests/test_app.py:6-13`).
- `PcEndpointResolverTest`는 private subnet 후보와 인증 probe 성공/실패를 확인하지만, PDF operation 재실행·첫 commit 후 ACK 실패·pairing save failure를 연결하지 않는다. Kiosk repository 시험은 정상 `markCardsDelivered()` 상태 전이만 확인하고 전송 결과와 request ID/hash를 묶지 않는다.

#### 추론

- 수신기가 PDF를 저장한 뒤 ACK가 네트워크 단절·socket timeout·Kiosk 중단으로 전달되지 않으면 Kiosk는 실패를 표시한다. 사용자가 전송 버튼을 다시 누르면 새 request ID가 replay 방어를 우회하고 receiver가 같은 카드 PDF를 별도 파일로 저장할 수 있다.
- 첫 endpoint의 PDF commit 뒤 ACK/read가 실패하고 receiver가 DHCP로 새 주소에서 계속 인증되는 경우, `withReachablePairedPc()` 자체가 두 번째 endpoint operation을 호출해 사용자 조작 없이도 새 request ID의 재전송을 만들 수 있다. 첫 실패가 commit 전 연결 실패였던 정상 DHCP 복구라면 duplicate가 생기지 않을 수 있으므로 이 자동 경로의 실제 발생 조건은 미검증이다.
- ACK가 이미 반환된 뒤 Kiosk의 `markCardsDelivered()`가 실패해도 같은 UI 실패·재시도 조건이 생기므로, ACK 유실뿐 아니라 “PC 저장은 성공했으나 Kiosk 상태 반영은 실패”한 경우에도 중복 파일과 DB/파일 상태 불일치가 가능하다.
- batch에서는 첫 PDF가 새 QR를 포함해 저장된 뒤 `markCardsDelivered()`가 실패하면 UI가 실패를 표시하고 재실행 시 다시 QR를 발급한다. 첫 파일이 수신 폴더에 남은 채 두 번째 QR 파일이 추가되거나 이전 파일이 현재 유효 QR과 어긋날 수 있으며, 단건의 동일 preview 재전송과는 다른 stale-artifact 조건이다.
- receiver의 설정 저장 실패가 파일 삭제·replay 메모리 갱신과 결합되면 같은 request는 거부되면서 파일이 없거나, 새 request로 다시 보내면 중복 저장되는 두 결과가 생길 수 있다.
- `markCardsDelivered(studentIds)`가 studentId만 갱신하므로, 동일 Activity single-thread 순서를 벗어난 old task·재생성·DB 상태 변경이 있으면 다른 QR revision을 delivered로 표시할 가능성이 있다. 현재 source에서 그 교차를 직접 입증하지 않아 `LUNA-0037`의 delivery invalidation·lifecycle 범위와 함께 후보 조건으로 둔다.
- 사용자 재시도 금지 결정과 운영 문서의 현재 전송 기능 재사용 안내가 양립하려면 “불확실한 결과의 자동 재전송”과 “사용자가 같은 send action을 다시 누르는 것”의 정책 경계를 명시해야 한다.
- 이 결과 PC 수신 폴더에 동일 QR 카드가 여러 개 생기고, 운영자가 중복 파일을 인쇄·보관할 위험이 있다. 카드 DB의 delivered 상태는 ACK 성공 때만 바뀌므로 중복 파일과 앱 상태가 어긋날 수 있다.
- ACK 유실·후속 DB 실패·receiver 설정 저장 실패를 실제로 실행하지 않았으므로 후보로 둔다. 정상 ACK/상태 저장과 재시도하지 않는 운영에서는 발생하지 않는다.

#### 가정과 미검증

- 동일 preview를 사용자가 실패 후 다시 전송할 수 있다는 점은 실패 콜백의 button enable 조건으로 확인했지만, 실제 UI 탭·네트워크 단절은 실행하지 않았다.
- 동일 PDF bytes·filename이 재시도에 사용된다는 점은 `preparePcPdfTransfer()`가 같은 `QrPreview`를 유지하고 filename을 재구성하는 경로에서 추론했다. 실제 PDF 파일·학생 데이터는 사용하지 않았다.
- PC 수신 폴더의 실제 중복 파일, ACK 유실 시 OS socket 상태와 수신기 재시작 동작은 검증하지 않았다.
- `sendall` 실패가 실제 receiver commit 뒤에 발생하는지, resolver가 그 직후 새 endpoint를 찾아 두 번째 PDF를 보내는지, config/DB save failure가 어떤 순서로 발생하는지는 fault injection 없이는 확인할 수 없다.
- batch 재실행이 실제로 이전 파일을 폐기·대체하는지, 운영자가 어느 파일을 인쇄하는지와 제품 결정상 사용자 재시도를 금지하는 해석은 확인하지 않았다.

#### 안전한 확인 절차

- Kiosk sender/UI의 성공·실패 순서와 PC receiver의 commit·replay·ACK 순서를 읽기 전용으로 대조하고 기존 server/app test 내용을 읽었다.
- Kiosk의 `withReachablePairedPc()`/resolver auto-retry, single/batch delivered update, product decision·release procedure와 관련 history/test도 읽기 전용으로 대조했다.
- 네트워크 단절·ACK 차단·PDF 전송·재시도·PC 파일/DB 생성·설정 save failure는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: ACK가 유실된 “처리 결과 미확정” 상태를 재조회하거나 delivery ID/hash로 같은 카드를 한 번만 저장해 사용자 재시도가 중복 파일을 만들지 않는다.
- 실제: receiver는 ACK 전에 파일을 확정하고, Kiosk wrapper는 endpoint recovery 뒤 operation을 자동 재호출하며 단건 실패 callback은 같은 send action을 다시 열고, 새 request ID와 unique destination으로 중복 저장할 수 있다. batch 후속 상태 실패는 재실행 시 새 QR와 stale/추가 파일을 만들 수 있다. receiver 설정 저장 실패에는 파일·replay rollback도 완결돼 있지 않다.

#### 영향

- 사용자/운영: PC 폴더에 중복 PDF가 쌓이고 잘못된 파일을 선택·인쇄할 수 있다. 실패 원인을 사용자가 알기 어렵다.
- 데이터: 학생 DB의 카드 delivered 상태와 PC 파일 수가 일치하지 않을 수 있다. 학생 자격정보·답안·점수 변경은 이 후보에서 확인하지 않았다.
- 보안: 동일 QR 카드가 여러 파일·출력물로 남는 운영·회수 위험이 증가할 수 있지만, 이 경로만으로 QR 원문이나 인증 우회가 발생한다고 확대하지 않는다.
- 제품/운영: 사용자 재시도 금지 결정, 자동 endpoint retry, 운영 문서의 수동 재전송 안내가 서로 다른 결과 확정 semantics를 만들 수 있어 장애 시 중복 방지 절차가 불명확하다.

#### 근본 원인 후보

- PDF transfer를 “server commit 후 ACK” 및 “ACK 후 Kiosk 상태 반영”으로 구현하면서 결과를 재조회할 stable delivery identity와 idempotent commit/rollback을 두지 않은 것으로 보인다.

#### 반대 가설·오탐 검토

- 같은 frame을 그대로 재전송하면 request ID replay로 거부되므로 단순 replay 보호는 존재한다. 그러나 현재 UI 재시도는 새 request ID를 만들기 때문에 그 보호만으로 duplicate commit을 막지 못한다.
- ACK와 후속 DB/config 저장이 항상 성공하면 문제는 관찰되지 않는다. 따라서 정적 경로는 확인했지만 실제 발생 상태는 후보로 유지한다.
- single-thread `ioExecutor`와 authenticated resolver probe는 정상 same-Activity ordering·wrong-host 전송을 제한한다. 이 때문에 모든 reissue/rename 작업이 곧바로 PDF와 경쟁한다고 주장하지 않으며, cross-Activity·failure path만 미검증 후보로 남긴다.
- 운영 문서가 현재 카드 재전송을 안내하는 것은 복구 가능한 정상 사용 흐름의 반대 근거다. 다만 ACK 불확실성·duplicate destination을 사용자에게 구분해 주지 않는다는 점은 해소하지 않는다.

#### 기존 테스트가 잡지 못한 이유

- 기존 tests는 정상 PDF 저장과 동일 frame replay를 분리해 확인하고, handler `sendall` 실패·Kiosk 후속 DB 실패·receiver `ConfigStore.save()` 실패와 새 request ID 재시도의 파일 수·hash·destination 관계를 연결하지 않는다.
- resolver 시험은 endpoint 선택만, repository 시험은 delivered row update만, release 기록은 정상 authenticated ACK/smoke만 다루므로, commit-after-ACK uncertainty와 idempotent delivery를 증명하지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 stable card delivery ID 또는 PDF hash+수신 대상 기반 idempotency를 프로토콜에 추가하고, ACK 유실·후속 DB 실패·receiver 설정 저장 실패 뒤 상태 조회·재전송·rollback UX를 정의한다.
- 사용자 재시도 금지 결정과 운영 문서의 재전송 절차를 먼저 정리하고, 불확실한 send 결과에는 non-idempotent PDF operation을 자동 재호출하지 않거나 결과 조회 후 재호출하도록 정책화한다. stable card delivery ID/QR revision/PDF hash 기반 idempotency와 ACK·후속 DB 상태 조회/rollback을 함께 정의한다.
- 정상 전송, ACK 유실 후 동일 preview 재시도, resolver auto-retry, 서버 재시작, `markCardsDelivered()` failure, 다른 카드·동일 filename, 부분 파일 실패에서 PC 저장 파일이 한 건이고 정확한 QR revision의 delivered 상태가 일치하는지 검증해야 한다.
- 이번 Goal에서는 소스·테스트·네트워크·PDF를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0010`은 `LUNA-0003`의 CSV queue 소실과 같은 ACK/소비 순서 계열이지만, PDF commit·후속 상태 반영에서 발생하는 중복 저장·재시도 영향이므로 별도 후보로 유지한다.
- `LUNA-0010`은 `LUNA-0008`의 replay cache eviction과도 다르다. 같은 frame replay는 거부되지만 새 request ID·새 destination으로 발생하는 delivery idempotency 문제다.
- `LUNA-0037`의 student mutation과 pending-card delivery invalidation, `LUNA-0036`의 batch QR/print ordering은 인접한 상태 binding 경계로 유지하고, `LUNA-0010`에서는 ACK/commit/재시도 결과 확정 순서를 중심으로 본다.
- idempotency·재조회·중복 파일 정리 UX는 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0011 — Kiosk audit_events와 Kiosk/Web private 진단 로그에 production 기간 보존·cleanup 기준이 없음

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 06:38 / 2026-08-02 19:39:06 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `AuditEventEntity`/`AuditDao`, Kiosk/Web `PrivateDiagnosticLog`, `StudentRepository.audit()`와 장기 무인 운전

#### 사실

- `AuditEventEntity`는 `audit_events` 테이블에 event type, reason code, 내부 student/session ID, app version, timestamp를 저장한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/Entities.kt:111-121`). timestamp와 event type 인덱스는 있지만 최대 행 수·보존 기간 제약은 없다.
- `StudentRepository.audit()`는 QR 승인·거부, 세션, 학생·반·QR/PDF 관리 작업 등 여러 경로에서 매번 `auditDao().insert()`를 호출한다(`StudentRepository.kt:928-938`). 반복적인 QR 거부만으로도 행이 계속 추가된다.
- `AuditDao.deleteOlderThan(cutoffEpochMs)`가 정의되어 있지만(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/Daos.kt:187-188`), production source에서 호출하는 경로는 검색되지 않았다. `AuditDao.latest()`의 현재 사용처도 계측시험으로 한정되며 production 감사 열람·내보내기 경로는 찾지 못했다.
- `KioskDatabase.get()`은 `Room.databaseBuilder(...).addMigrations(...).build()`만 구성하고 `RoomDatabase.Callback` 또는 audit maintenance runnable을 등록하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/KioskDatabase.kt:38-47`). Kiosk/Web manifest는 `allowBackup=false`·`fullBackupContent=false`를 선언하고, 두 `data_extraction_rules.xml`은 cloud backup/device transfer의 `database`, `file` 등 모든 domain을 exclude한다. 이는 Android backup/기기 이전이 audit 누적을 외부로 복제하는 경로를 줄이는 사실이지, 현재 기기 내부의 retention/cleanup을 대신하지 않는다.
- `docs/PRODUCT_DECISIONS.md:30-36`은 내부 진단 로그에 크기·기간 제한을 적용한다고 정하고, `SECURITY.md:43-47`은 감사기록의 민감정보 금지를 정하지만 audit_events의 retention 실행 기준은 문서화돼 있지 않다.
- Kiosk `PrivateDiagnosticLog`는 `kiosk-events.log`와 `.previous.log`를 정상 경로에서 파일당 약 256KB로 회전하지만, `previous.delete()`와 `current.renameTo(previous)`의 Boolean 결과를 확인하지 않고 append를 계속한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/PrivateDiagnosticLog.kt:12-38,68-78`). 생성 시각 기준의 기간 삭제·startup/idle cleanup도 없어 오래 이벤트가 없는 기기에서는 이전 파일이 기간 제한 없이 남을 수 있고, 회전 실패 시 nominal size cap은 best-effort다.
- Web POC의 `PrivateDiagnosticLog`도 `web-events.log`와 `.previous.log`를 정상 경로에서 파일당 약 256KB로 회전하지만 `previous.delete()`·`current.renameTo(previous)` 결과를 확인하지 않으며, `Instant` timestamp를 기록할 뿐 기간 기준 삭제나 startup/idle cleanup은 없다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/PrivateDiagnosticLog.kt:8-29`). 따라서 Kiosk/Web 진단 로그 모두 제품 결정의 기간 제한을 코드상 집행하지 않는다.
- Kiosk `MainActivity.onCreate()`는 `QrPdfExporter.cleanupExpired(this)`를 호출하지만 audit row 또는 private diagnostic log cleanup은 호출하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:357-368`). Web `MainActivity`의 `onCreate()`·`onStop()`·`onDestroy()`에도 `PrivateDiagnosticLog` 기간 삭제 경계가 없다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:153-198,2356-2420`).
- 두 진단 dump는 `android.permission.DUMP` receiver에서만 호출되고, 16~64자리 hex nonce·구조적 event line·파일별 최근 200줄 필터를 적용한다(`kiosk/src/main/AndroidManifest.xml:69-76`, `webpoc/src/main/AndroidManifest.xml:47-54`, 각 `PrivateDiagnosticLog.kt:41-78/31-54`). 두 앱은 private storage와 backup/data-transfer 제외도 선언한다. 현재 call site의 event/reason은 고정 코드 또는 구조적으로 제한된 값이며 학생 이름·외부 로그인 ID·비밀번호·QR 원문·답안·점수·문항 내용이 직접 기록되는 경로는 확인하지 않았다.
- 두 `dumpForAdb()`는 `readLines(Charsets.UTF_8).takeLast(200)` 순서라 출력 행 수는 제한하지만 입력 파일을 먼저 전부 메모리에 올린다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/PrivateDiagnosticLog.kt:41-53`, `webpoc/src/main/java/com/local/matholickiosk/webpoc/PrivateDiagnosticLog.kt:31-44`). Kiosk `record()`의 `synchronized(lock)`와 Web `event()`의 `@Synchronized`는 기록 경로에만 적용되고 dump에는 공통 잠금이 없다. Kiosk dump receiver는 별도의 `PrivateDiagnosticLog` 인스턴스도 생성한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/AdbDiagnosticDumpReceiver.kt:8-12`). 회전·append와 dump가 겹치면 두 세대가 일관된 시점의 snapshot이 아닐 수 있고, `runCatching`이 read/rotate/write 실패를 숨겨 `END`만 남기는 관찰성 공백이 생길 수 있다.

#### 추론

- 기기를 장기간 무인 운전하거나 반복적인 잘못된 QR·관리자 조작이 발생하면 audit_events가 앱 DB에 계속 쌓여 local storage와 DB index/maintenance 비용을 늘릴 수 있다. `audit()`가 상태 변경 transaction 내부에서 호출되는 경로도 있어 DB 공간·insert 실패가 해당 transaction에 전파될 가능성이 있다.
- Kiosk/Web private 진단 로그는 정상 회전 시 두 세대 크기 상한은 있지만 기간 상한이 없어, 이벤트 빈도에 따라 보존 기간이 예측되지 않고 오래된 상태 코드가 다음 오류 때까지 남을 수 있다. 파일 rename/delete가 실패해도 반환값을 검사하지 않으므로 fault 조건에서는 두 세대 상한 자체도 보장되지 않는다.
- 회전 실패로 파일이 nominal cap를 넘어가면 `takeLast(200)`가 입력을 제한하기 전에 전체 파일을 읽으므로 ADB dump의 실제 메모리 비용이 파일 크기에 따라 증가할 수 있다. 기록과 dump가 서로 다른 시점의 파일을 읽거나 예외를 삼켜도 호출자는 정상적인 `BEGIN/END` 형식만 볼 수 있어, 진단 로그가 없다는 사실과 읽기 실패를 구분하기 어렵다.
- 내부 UUID와 종료된 session ID가 보존 기간 없이 남아 운영상 필요한 최소 기간을 넘길 수 있다. 이 값이 실제 학생 외부 ID나 이름으로 역변환되는 경로는 확인하지 않았으므로 직접 개인정보 노출로 확대하지 않고 retention/privacy 후보로 제한한다.

#### 가정과 미검증

- `audit_events`가 제품 결정의 “내부 진단 로그” 보존 정책에 포함된다는 해석을 적용했다. 영구 감사 보관이 의도된 별도 정책이라면 영향·심각도는 재평가가 필요하다.
- A의 DB 크기, audit row 수, 장기 운전과 storage pressure는 조회·측정하지 않았다.
- rename/delete 실패, read-only·저장공간 부족·파일 잠금 등 회전 fault에서 실제 파일 크기가 상한을 넘는지는 실행하지 않았다.
- 회전 실패 뒤 oversize log를 ADB dump할 때의 실제 heap 사용량, 동시 append/rotate와 dump의 누락·혼합 결과, swallowed I/O failure의 실제 발생 여부는 실행하지 않았다.
- 민감정보는 현재 audit 필드·diagnostic call site에 직접 저장되지 않는 것으로 정적 분석했으며, 실제 DB·private log·logcat 내용을 열람하지 않았다.
- `audit_events`를 영구 운영 이력으로 보존하려는 별도 정책이나 외부 maintenance job이 존재하지 않는다는 점은 저장소·문서 검색으로만 판단했다.

#### 안전한 확인 절차

- entity/DAO/repository 호출 검색, Kiosk/Web logger·ADB receiver·manifest/data-extraction rules, 관련 테스트, 제품 결정·보안 문서와 Git history를 읽기 전용으로 대조했다.
- DB·감사 기록·A 데이터·장기 부하·파일 fault injection을 사용하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 감사 이벤트의 목적별 보존 기간·최대 크기와 cleanup 실행 시점이 정의되고, 오래된 내부 ID가 자동 정리된다.
- 실제: DB 삭제 DAO는 존재하지만 production 호출·보존 상한·운영 기준을 확인하지 못했고 insert만 계속 수행된다. Kiosk/Web private 진단 로그는 정상 경로의 크기 회전만 있고 기간 cleanup은 확인되지 않으며, 회전 실패 시 Boolean 결과를 무시한다.

#### 영향

- 운영/가용성: 장기 사용 시 DB가 불필요하게 커지고 저장공간 부족이 감사 포함 transaction 실패로 전파될 수 있으며, logger 회전 fault에서는 진단 파일도 예상보다 커질 수 있다.
- 개인정보: 학생 표시 이름·외부 로그인 ID·비밀번호·QR 원문·답안·점수·문항 내용은 현재 call site에서 저장하지 않지만 내부 student/session ID의 보존 기간이 불명확하다. private storage·backup 제외·DUMP 권한·line filter는 노출 가능성을 낮추는 반대 근거다.
- 이번 정적 검토에서 실제 DB 손상·저장공간 부족·회전 실패·로그 외부 노출은 확인하지 않았다.

#### 근본 원인 후보

- 감사 이력 조회·삭제 DAO와 크기 회전 logger를 먼저 만들었지만, 무인 운영 중 목적별 기간 cleanup·행/용량 상한·실패 상태를 호출하고 관찰하는 lifecycle/maintenance 경로를 연결하지 않은 것으로 보인다. logger의 파일 operation 결과를 확인하지 않는 것도 best-effort 상한의 원인 후보다.

#### 반대 가설·오탐 검토

- audit table의 영구 보관이 별도 제품 요구이고 DB 운영자가 외부에서 정리한다면 결함이 아닐 수 있다. 그러나 `PRODUCT_DECISIONS`는 private 진단 로그의 크기·기간 제한을 명시하고, 현재 저장소에서 audit별 정책·외부 작업·상한을 찾지 못해 후보로 남긴다.
- private storage·backup exclusion·정상 2세대 회전·구조적 event filter는 오탐을 줄이는 반대 근거다. 이벤트 row 하나가 즉시 운영 불능이나 민감정보 유출을 만든다고 주장하지 않으며 P4로 제한했다.

#### 기존 테스트가 잡지 못한 이유

- 계측시험은 최신 audit event 내용과 상태 전이를 확인하고 credentials/QR 원문이 문자열에 없는지 검사하지만, 장기 누적·cutoff cleanup·DB 용량·onCreate 호출·파일 회전 Boolean 실패·기간 age 경계를 검증하지 않는다. JVM 진단시험도 nonce/구조적 dump line만 검사하며, oversize input read·concurrent dump/record·I/O failure visibility를 검증하지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 audit와 diagnostic log를 구분해 보존 기간·최대 행/크기·정리 실패 정책을 문서화하고, 안전한 idle/startup cleanup을 연결한다. 파일 회전은 rename/delete 실패를 관찰하고 cap 초과·쓰기 실패를 명시적으로 처리해야 하며, dump는 bounded tail read·기록과의 snapshot 동기화·read failure 신호를 별도 설계해야 한다.
- cleanup 중 상태 transaction과 충돌하지 않는지, 최근 감사기록·장기 운전·DB migration·저장공간 부족·회전 실패 경계를 검증해야 한다. 이번 Goal에서는 소스·DB·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0011`은 `LUNA-0004`·`LUNA-0006`의 민감 배열 wipe와 다른 persistent audit/diagnostic retention 후보다.
- audit를 영구 이력으로 유지할지, 내부 진단 로그처럼 제한할지는 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0012 — 원격 점검 배지가 Kiosk 헤더 상태 표시를 가림

- 심각도: `P3`
- 상태: `확정`
- 신뢰도: 높음
- 최초 발견·마지막 직접 확인: 2026-08-02 06:53~06:54 (Asia/Seoul)
- 정적 재대조: 2026-08-02 16:22:08, 19:44:47 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `RemoteSupportWindowController`와 관리자 헤더 UI; 승인된 A 단기 화면 확인에서 직접 관찰

#### 사실

- `kiosk/src/main/java/com/local/matholickiosk/kiosk/RemoteSupportWindowController.kt:54-84`의 `showBadge()`는 `원격 점검 중` TextView를 `android.R.id.content`에 `Gravity.TOP or Gravity.END`, 상단/우측 8dp 여백으로 추가한다. 앱 헤더의 상태 표시 영역을 피하도록 배치하거나 레이아웃 공간을 예약하는 코드는 없다.
- `kiosk/src/main/res/layout/activity_main.xml:31-48`의 60dp 헤더 오른쪽에는 `device_mode_text`와 `status_text`가 배치된다.
- 현행 controller의 `elevation=12dp`와 content-root `addView()` 순서 때문에 활성 상태에서 배지는 layout root의 기존 자식들 위에 그려진다. `MainActivity`는 `setContentView()` 뒤 controller를 시작하고, 활성 만료까지 `refresh()`에서 배지를 유지한다. `showScanner()`가 Kiosk `app_header`를 `GONE`으로 바꾸어도 controller의 배지를 별도로 숨기지 않는다.
- `status_text`는 `wrap_content`이고 `maxLines`·`ellipsize`·고정 폭 또는 badge와의 배치 계약이 없다(`activity_main.xml:31-48`). MainActivity에는 `CAMERA_PERMISSION_REQUIRED`, `INITIALIZATION_FAILED`, `ADMIN_LOADING`, `PC_PAIRING`, `MANUAL_STUDENT_SELECTION` 등 상태 문자열 대입이 흩어져 있어, 화면 폭·font scale에 따라 header의 우측 가용 폭이 달라질 수 있다(`MainActivity.kt:221,647,779,2970,3058`). 이는 과거 A 겹침을 대체하는 새 증거가 아니라, 동일한 geometry 계약의 범위를 넓히는 정적 위험이다.
- `MainActivity.onCreate()`는 controller를 만들고 `setContentView()`·`bindViews()` 뒤 `remoteSupportWindowController.start()`를 호출한다(`MainActivity.kt:337-370`). `showBadge()`는 이후 header visibility나 measured bounds를 읽지 않고 고정 inset으로만 배치하며, `updateRemoteSupportButton()` callback은 admin panel의 원격 점검 버튼 문구만 갱신한다(`MainActivity.kt:2755-2761`).
- 계약이 허용한 `remote-tablet.ps1 -Action Start -Minutes 15` → 자동 Capture → `Stop` 한 회에서 A의 관리자 화면을 확인했다. 원격 점검 배지가 헤더 오른쪽에 겹쳐 현재 상태 표시의 일부를 가렸다. 화면에 있던 학생·QR 원문은 기록하지 않았고, 임시 PNG는 `Stop`에서 삭제했다.
- 같은 사이클에서 PIN 입력·QR 로그인·학생 선택·터치·답안·제출은 하지 않았다. Stop 뒤 읽기 전용 확인에서 `LOCAL_SCREEN_EXISTS=False`였고 Device Owner/allowlist는 유지됐다.
- `webpoc/src/main/java/com/local/matholickiosk/webpoc/RemoteSupportWindowController.kt`도 같은 content-root·`TOP|END`·8dp·elevation 12dp 배치다. 다만 `webpoc/src/main/res/layout/activity_main.xml`의 상단은 활성 학생 목록에서 64dp 전체 폭 `student_nav_bar`와 중앙의 두 200dp 버튼이고, `status_badge`는 중앙 상단, `student_name_badge`는 좌측 상단, `finish_button`은 우측 하단이다. Web `MainActivity`는 네비게이션 표시 때 WebView top margin만 64dp로 조정한다. 현재 정적 자료와 과거 화면 기록만으로 Web의 같은 겹침을 확정할 수 없어 이번 finding을 Web로 확대하지 않는다.
- Gate 5 instrumented test의 원격 점검 검사는 exported receiver의 `android.permission.DUMP`와 `remote_support_button`의 ScrollView ancestor만 확인한다(`kiosk/src/androidTest/java/com/local/matholickiosk/kiosk/Gate5ManifestInstrumentedTest.kt:80-101`). JVM `RemoteSupportPolicyTest`는 1분/2시간 clamp와 만료·boot count만 검사한다. badge 생성·header 상태 문자열·window overlay bounds·font scale·scanner 전환을 테스트하는 코드는 찾지 못했다.
- Git history에서 Kiosk controller는 `f8bd710` 도입 이후 별도 geometry 수정이 없고, Web controller의 후속 `9153f75`는 원격 점검 상태에 따른 WebView debugging callback만 추가했다. Web layout의 후속 `a14c51b`도 네비게이션 버튼 색상 명확화이며 배지 위치·예약 영역 수정은 아니다.

#### 추론

- 원격 점검 중 관리자에게 현재 Kiosk 상태가 완전히 보이지 않아 `QR_READY`인지 다른 상태인지 판단이 늦어지거나, 잘못된 관리자 조작을 선택할 가능성이 있다. 긴 상태 문자열과 좁은 폭/font scale에서는 겹침 범위가 더 커질 가능성이 있지만 실제 bounds는 측정하지 않았다.
- 영향은 원격 점검이 활성화된 화면의 상태 가시성과 운영 UX에 한정되며, 이번 확인에서 캡처 권한 자체가 의도보다 넓어지거나 Lock Task 정책이 바뀐 근거는 없었다. P3으로 분류한다.

#### 가정과 미검증

- 직접 확인한 것은 A의 현재 관리자 화면 한 상태와 2000×1200 화면 한 회다. QR 대기·Web·문제·결과 화면 및 다른 density/font scale에서 같은 겹침이 발생하는지는 확인하지 않았다.
- 이번 재대조에서는 ADB가 PATH에 없어 새 A 화면·bounds·density를 확인하지 못했다. 06:53~06:54 A 확인은 historical evidence로만 사용했으며, Web의 실제 학생·문제·결과 상태와 좁은 폭/회전에서의 배지 겹침은 미검증이다.
- 실제 TalkBack focus, 터치 hit-test와 글자 크기 변경은 실행하지 않았다.
- 긴 `status_text`가 실제 설치 화면에서 줄바꿈·잘림·header 폭 초과를 일으키는지, `showScanner()` 상태에서 우측 상단 badge가 다른 scanner affordance와 겹치는지는 라이브 화면 없이 미검증이다. 현재 scanner layout의 주요 조작부는 bottom|end에 있어 정적 상하 위치가 겹친다고 단정하지 않는다.

#### 안전한 확인 절차

- 승인된 기존 원격 지원 스크립트의 15분 만료형 Start/Capture/Stop만 사용했다. 별도 네트워크 포트·PIN·QR·앱 데이터·화면 조작은 사용하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 원격 점검 배지와 헤더의 상태·기기 모드 표시가 서로 가리지 않고 동시에 읽힌다.
- 실제: 배지가 헤더 오른쪽 상태 표시 위에 겹쳐 상태 텍스트 일부를 가렸다.

#### 영향

- 사용자: 원격 점검 중 관리자 상태 확인이 불완전하다.
- 데이터·보안: 학생·QR 원문을 보고서에 기록하지 않았고, 캡처 권한·암호화·Lock Task의 새 결함은 확인하지 않았다.
- 운영: 상태가 가려진 동안 복귀·종료·세션 판단이 늦어질 수 있다.

#### 근본 원인 후보

- 배지를 최상위 content root에 고정 좌표처럼 추가하면서 기존 헤더의 우측 상태 영역과 layout contract를 공유하지 않은 것으로 보인다.

#### 반대 가설·오탐 검토

- 배지 자체는 원격 점검 활성 상태를 명확히 알리지만, 그 사실이 상태 표시를 가리는 겹침을 해소하지는 않는다. 한 화면 캡처의 일시적 애니메이션이 아니라 정적 View 배치와 캡처가 일치해 후보가 아닌 확정으로 기록했다.

#### 기존 테스트가 잡지 못한 이유

- `RemoteSupportPolicyTest`는 duration·boot count 정책만 검증하고, 실제 View hierarchy에서 badge와 header status의 bounds/overlap을 검사하지 않는다. Gate 5 test도 receiver 보안 경계와 admin control reachability만 다룬다. ADB 화면 캡처·시각 QA는 계약상 자동시험에 포함되지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 배지를 헤더 내부의 별도 예약 영역에 배치하거나 우측 상태 텍스트가 재배치되도록 layout을 조정한다.
- 관리자·QR 대기·Web/학생 화면에서 원격 점검 배지와 상태·보안 표시가 겹치지 않는지 같은 해상도와 다른 font scale로 캡처 확인한다. 특히 Web은 중앙 네비게이션의 실제 bounds와 좁은 폭을 별도로 확인해 Kiosk finding과 분리한다. 이번 Goal에서는 소스·레이아웃·빌드를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0012`는 `LUNA-0009`의 Windows 개인정보 알림 노출과 다른 Android 화면 가시성 문제다.
- 원격 점검 기능을 유지하면서 배지와 상태 표시의 우선순위·배치만 사용자/Sol 재판단 큐에 남긴다.

### LUNA-0013 — CSV parser가 credential을 immutable String으로 복제해 입력 wipe 후에도 힙 잔류 가능

> **교정 후 판정 — P4 방어강화 umbrella.** JVM/Kotlin immutable String의
> 즉시 zeroize 불가능성은 사실이나 앱 private process 밖으로 노출된 증거는
> 없다. `LUNA-0004/0031/0032`를 이 수명 최소화 항목에 병합한다.

- 심각도: `P3`
- 상태: `확정`
- 신뢰도: 높음
- 최초 발견: 2026-08-02 07:00; 마지막 정적 확인: 2026-08-02 19:55:13 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `StudentCsvParser`, PC CSV control response 수신·preview/import 경계

#### 사실

- `StudentCsvParser.parse(payload: ByteArray)`는 수신 payload를 `String(payload, UTF_8)`로 전체 복사하고, `parseRecords(text)`에서 `StringBuilder.toString()`으로 각 CSV field를 immutable `String`으로 만든다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/StudentCsvImport.kt:38-129`).
- 각 행의 username/password는 이후 `toCharArray()`로 `StudentCsvRow`에 복사되지만, 원본 `text`, `records`, field `String`, `usernames` set은 wipe API가 없고 parser 반환값에도 포함되지 않아 GC가 회수할 때까지 메모리에 남을 수 있다.
- `MainActivity.fetchStudentCsvFromPc()`는 `PcControlClient.fetchStudentCsv()`가 반환한 `download.payload`를 parser에 넘긴 뒤 ByteArray를 `finally`에서 0으로 덮는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:1750-1793`). 이 wipe는 이미 생성된 immutable `String` 복사본을 지우지 못한다.
- `StudentCsvRow.clearSensitiveData()`와 `ParsedStudentCsv.clearSensitiveData()`는 보유한 `CharArray`만 덮는다(`StudentCsvImport.kt:3-22`). CSV preview 취소·적용·실패 경계의 wipe가 parser 내부 문자열까지 회수하는 구조는 아니다.
- 현행 `StudentCsvParser`는 1MB·1,000행 상한으로 양을 제한하지만, `String(payload, UTF_8)` 뒤 `removePrefix`, `parseRecords`의 `field.toString()`, 각 행의 `trim()`·`split('|')`·`map(String::trim)`·`toSet()`와 `usernames: MutableSet<String>`를 사용한다(`StudentCsvImport.kt:41-88,91-129`). username/password는 `toCharArray()`로 별도 복사된 뒤에도 원본 field·record·전체 text String에 wipe 가능한 owner가 없다.
- `SECURITY.md:38-47`과 `docs/GATE4_IMPLEMENTATION.md:23-28,45`는 자격정보를 필드 암호문으로 저장하고 사용 후 `CharArray`를 덮는 경계를 설명하지만, CSV parser의 immutable String 복제 경계는 별도로 경고하지 않는다.
- `PcControlClient.fetchStudentCsv()`는 검증된 `DecodedPcControlResponse.payload`를 `PcCsvDownload`로 넘기고, 호출부 `MainActivity.fetchStudentCsvFromPc()`는 parser 전후 `download.payload.fill(0)`를 수행한다(`PcControlClient.kt:56-74`, `MainActivity.kt:1750-1793`). `PcControlProtocol.decodeResponse()`와 frame/header/request cleanup은 transport 단계의 반대 근거지만, parser가 만든 String copies까지 지우지는 못한다(`PcControlProtocol.kt:89-119`, `PcControlClient.kt:81-119`).
- parser의 production 호출은 현재 `MainActivity`의 `StudentCsvParser.parse(download.payload)` 한 곳이다. `StudentRepository.previewStudentImport()`·`importStudents()`는 반환된 row `CharArray`와 복호화한 기존 username을 `finally`에서 지우지만, parser 중간 String의 owner를 받지 않는다(`StudentRepository.kt:331-429,432-477`).
- `StudentCsvImport.kt` history는 `0193d89` 도입 commit 이후 이 파일에 후속 수정이 없다. 따라서 이번 정적 재대조에서 과거 finding을 해소한 parser representation·cleanup 변경은 확인되지 않았다.

#### 추론

- PC에서 암호화 전송된 CSV의 아이디·비밀번호가 Kiosk heap의 wipe 불가능한 String 복사본으로 남을 수 있다. 정상 경로에서도 발생하며, GC 시점·메모리 재사용·heap dump 권한에 따라 잔류 기간이 달라진다.
- 앱이 backup·로그·외부 저장소에 String을 영속화한다는 근거는 없지만, `CharArray` wipe만으로는 이 CSV parsing 경계의 메모리 회수를 보장하지 못한다. P3으로 분류한다.

#### 가정과 미검증

- 실제 Android ART heap에서 String object가 언제 회수·재사용되는지, heap dump에서 값이 관찰되는지는 실행하지 않았다.
- CSV는 의도된 관리자 import 기능의 입력이고, transport ByteArray·parsed CharArray의 명시적 wipe는 확인했다. 이 finding은 그보다 앞선 immutable parser copies의 수명에 한정한다.
- 이번 사이클에는 새 ADB/A 화면·CSV 원문·학생 DB·heap/GC 관찰을 하지 않았다. `String` object가 실제 ART heap에서 얼마나 오래 남는지는 계속 미검증이며, 코드상 wipe 불가능한 복사본이 생성된다는 사실과 구분한다.

#### 안전한 확인 절차

- parser, Kiosk fetch caller, control response decoder와 보안 문서의 credential memory 경계를 읽기 전용으로 대조했다. CSV 원문·학생 계정·heap dump·A 데이터는 사용하지 않았다.

#### 기대 결과와 실제 결과

- 기대: CSV credential이 사용 후 wipe 가능한 배열에만 존재하고, transport buffer·parser 중간 표현·preview cancel/failure 경로가 회수된다.
- 실제: input ByteArray와 row CharArray는 덮지만, 전체 CSV와 credential field를 담은 immutable String copies는 직접 덮을 수 없다.

#### 영향

- 보안/개인정보: 같은 앱 프로세스의 heap inspection 또는 메모리 오류 상황에서 CSV credential 노출 면적과 잔류 가능성이 커진다.
- 데이터: DB에는 기존 설계대로 Keystore 암호문만 저장하며, 이번 정적 검토에서 평문 credential의 파일·로그·Room 저장은 확인하지 않았다.
- 운영: CSV preview를 오래 열어 두거나 import 중 오류가 나도 parser String copies의 수명이 GC에 의존한다.

#### 근본 원인 후보

- CSV parser가 간단한 Kotlin `String` 기반 CSV 처리로 구현되어, 이후 도입된 `CharArray` wipe 계약이 parser의 중간 immutable representation까지 확장되지 않은 것으로 보인다.

#### 반대 가설·오탐 검토

- parser 반환 후 local `String`이 unreachable이 되면 일반적으로 GC가 회수할 수 있으므로 영구 저장 결함으로 과장하지 않았다. 다만 `String`은 회수 전 덮을 수 없고 정상 path마다 생성된다는 코드 사실이 있어 후보가 아닌 확정 설계 결함으로 기록했다.
- `download.payload.fill(0)`와 row wipe가 존재한다는 이유만으로 parser copies가 지워진다고 볼 수 없어 `LUNA-0004`와 중복 처리하지 않았다. `LUNA-0004`는 기존 DB username 복호화 map의 예외 전파 전 ownership이고, `LUNA-0013`은 CSV 입력 parser 전체의 immutable copies다.

#### 기존 테스트가 잡지 못한 이유

- `StudentCsvParserTest`와 repository CSV 계측시험은 값·행 수·DB 암호화·CharArray wipe를 확인하지만, parser 중간 String의 heap ownership이나 GC 전후 민감값 회수를 검사하지 않는다.
- 현재 `StudentCsvParserTest`의 정상 fixture는 반환 객체만 `finally`에서 지우고, duplicate/header rejection은 parser가 반환하기 전 예외를 기대한다. `SensitiveTaskTest`도 임의 task의 cleanup once/discard만 검증하며 CSV parser의 immutable String owner와 연결되지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 parser를 wipe 가능한 byte/char buffer 중심으로 바꾸거나, credential field parsing을 immutable String 생성 없이 처리하고 중간 buffers의 소유권·예외 cleanup을 명시한다. 표준 CSV quoting과 UI preview 수명까지 함께 보존해야 한다.
- malformed CSV, 중간 행 실패, preview 취소/적용 실패, 1MB/1000행 경계에서 transport·parser·row memory cleanup을 검증해야 한다. 이번 Goal에서는 source·parser·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0013`은 `LUNA-0004`의 기존 username decrypt map 누락, `LUNA-0006`의 QR hash ownership 누락과 모두 민감 배열 수명 계열이지만, immutable CSV parser representation이라는 별도 경계다.
- CSV import의 메모리 표현과 zeroization 계약은 Sol·사용자 재판단 큐에 남긴다.
- `LUNA-0031`은 `ParsedStudentCsv` 반환 후 preview/apply executor가 row `CharArray` owner를 잃는 lifecycle 경계이고, `LUNA-0032`는 parser validation 예외 시 반환 전 partial row owner를 잃는 경계다. 이번 재대조는 그 둘과 중복되지 않는 정상 parser의 immutable `String` 생성 경계다.

### LUNA-0014 — Kiosk PC 전송 암호화 예외 경로에서 평문·파생 key 정리가 보장되지 않음

- 심각도: `P3`
- 상태: `확정`
- 신뢰도: 높음
- 최초 발견: 2026-08-02 07:08~07:18; 마지막 정적 확인: 2026-08-02 20:01:17 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `PcTransferProtocol`/`PcControlProtocol`의 PDF·상태·CSV 제어 전송

#### 사실

- `PcTransferProtocol.encodeRequest()`는 filename과 전체 PDF를 하나의 `plaintext` `ByteArray`로 만든다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcTransferProtocol.kt:49-57`). 파생 `key`는 `Cipher.doFinal(plaintext)` 뒤에만 지워지고(`:67-77`), cipher 초기화·AAD 설정·암호화·후속 frame 할당에서 예외가 나면 `plaintext`와 `key`를 감싸는 `finally`가 없다.
- 같은 함수의 `filenameBytes`는 PDF filename을 UTF-8 `ByteArray`로 복사하며, 호출부가 `preview.exactName`을 filename에 넣는 경로에서는 학생 표시 이름이 이 별도 buffer에도 들어갈 수 있다(`PcTransferProtocol.kt:40-54`, `kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:2363`). `plaintext`·파생 `key`의 성공 후 wipe와 달리 이 `filenameBytes`에는 별도 `fill(0)`가 없다.
- `PcControlProtocol.encodeRequest()`도 상태/CSV 제어 payload를 `plaintext`로 만든 뒤 `encodeSecureFrame()`이 정상 반환한 다음에만 `plaintext.fill(0)`을 실행한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcControlProtocol.kt:55-72`). 내부 `encodeSecureFrame()`의 파생 `key` 역시 `doFinal()` 성공 뒤에만 지워진다(`:161-170`).
- PDF ACK 검증의 `PcTransferProtocol.verifyAck()`도 HMAC `doFinal()` 뒤에만 파생 key를 지운다(`PcTransferProtocol.kt:122-126`). `Mac` 초기화·계산 예외는 이 정리 지점을 건너뛴다.
- `PcControlProtocol.decodeResponse()`는 `decodeSecureFrame()`이 반환한 `SecurePlaintext`의 request ID를 먼저 비교하고, 그 다음에야 `plaintext.fill(0)` 범위에 들어간다(`PcControlProtocol.kt:76-119`). 인증된 stale/misbound response에서 request ID가 기대값과 다르면 예외가 `finally` 진입 전에 발생해 복호화된 control plaintext와 내부 request ID가 별도 owner 없이 남는다. `PcControlClient.exchange()`의 frame/header `finally`는 이미 복사된 `decoded.plaintext`를 덮지 못한다(`PcControlClient.kt:77-120`).
- `PcPdfSender.send()`의 ACK `ByteArray`는 `verifyAck()`가 정상 반환한 뒤에만 `ack.fill(0)`을 호출한다. ACK 형식·request ID·PDF hash·HMAC 검증 실패 시 outer `finally`는 `transfer.frame`·request ID·hash만 지우고 ACK 자체는 지우지 않는다(`PcPdfSender.kt:21-47`). ACK에는 PDF 원문이 없으므로 이 세부 경계의 영향은 response metadata·signature buffer 수명으로 제한한다.
- `PcControlClient.sendStatus()`는 학생 표시명을 포함할 수 있는 JSON을 `buildString(...).toByteArray()`로 만들고 ByteArray만 `finally`에서 덮는다(`PcControlClient.kt:17-53`). JSON immutable `String`과 `PcControlProtocol`의 `labelBytes`·ciphertext·authenticated header 같은 파생 복사본에는 명시적 wipe API가 없다. 이는 정상 path의 transient copy이며 persistent 로그·파일 저장으로 확대하지 않는다.
- 정상 성공 경로의 `plaintext`·key wipe와 control response decode의 key/ciphertext `finally`는 확인했다. 따라서 모든 경로가 아니라 암호화·ACK 검증 실패 경계의 cleanup 누락으로 한정한다.
- `PcTransferProtocolTest`와 `PcControlProtocolTest`는 정상 Python 벡터·변조된 ACK/응답을 확인하지만, cipher/provider·allocation 실패 시 민감 buffer가 정리되는 계약은 검사하지 않는다. 테스트는 실행하지 않았다.
- Git history에서 control protocol은 `a3aa80c`, PDF transfer는 `474132b`에 도입됐고, 현재 읽은 세 파일 history에는 이후 cleanup geometry/ownership 수정이 보이지 않았다. 기존 정상 vector·A 종단간 기록은 기능·인증 경계의 반대 근거이지 예외 zeroization 증명은 아니다.

#### 추론

- PDF 평문 buffer에는 학생 QR 카드가 들어가며, 제어 status에는 학생 표시 이름이 들어갈 수 있다. 암호화 또는 ACK 검증이 예외로 끝나면 해당 `ByteArray`와 파생 key가 GC 전까지 힙에 남을 수 있다.
- PDF filename이 학생 표시 이름을 포함하는 호출에서는 `filenameBytes`가 정상 성공 후에도 wipe되지 않아, PDF 평문과 별개로 이름이 담긴 transient buffer의 수명이 늘어날 수 있다.
- Android ART가 이후 회수할 수 있다는 사실은 즉시 덮어쓰기 계약을 대체하지 않는다. 실제 예외 주입·heap dump·GC 생존 시간은 확인하지 않아 영구 잔류로 확대하지 않는다.

#### 가정과 미검증

- 정상 Android crypto provider와 유효한 입력에서는 현재 성공 경로의 wipe가 실행된다는 가정을 적용했다.
- provider 오류, 메모리 할당 실패, socket/ACK 실패와 암호화 호출의 결합 경로는 실행하지 않았다. 실제 학생 PDF·학생 이름·secret·heap 내용은 사용하거나 기록하지 않았다.
- request-ID 불일치가 실제로 발생하는 stale/misbound 응답, control CSV payload의 최대 크기, ACK 검증 예외와 Android ART heap 생존 시간은 실행하지 않았다. request-ID mismatch 경계는 paired receiver 또는 인증된 stale response 전제를 필요로 하며 일반 외부 공격자가 곧바로 plaintext를 주입한다는 주장은 아니다.

#### 안전한 확인 절차

- Kiosk PC 전송 protocol 구현·호출부·관련 JVM 테스트를 읽기 전용으로 대조했다.
- 소스·테스트·APK·PC receiver·네트워크 상태는 변경하거나 실행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: PDF filename·PDF/control request·ACK 검증의 성공·실패·예외 경로에서 민감한 임시 buffer와 파생 key의 소유자가 명확하고 `finally`에서 wipe된다.
- 실제: PDF/control plaintext와 key는 성공 직후에만 wipe되고 예외 시 보장이 없으며, `filenameBytes`는 정상 성공에도 별도 wipe되지 않는다.
- 실제: 위 경계에 더해 control response request-ID 불일치 시 복호화 plaintext가 `finally` 밖에서 남고, ACK 검증 실패 시 caller의 ACK buffer wipe도 건너뛴다. status JSON immutable String은 ByteArray wipe와 별개로 남는다.

#### 영향

- 보안/개인정보: 전송 실패 시 QR 카드 PDF 평문 또는 status payload가 불필요하게 힙에 남는 시간이 늘고, 파생 key도 회수 전까지 남을 수 있다.
- request-ID mismatch가 CSV fetch 응답에 적용되면 최대 1MiB의 credential-containing control plaintext가 정상 parser에 전달되기 전 transient heap에 남을 수 있다. 다만 이 경로는 먼저 GCM 인증을 통과한 stale/misbound response가 필요하다.
- 데이터/인증: 저장 파일의 암호화·무결성 우회나 pairing secret 자체의 영구 노출은 이 finding에서 확인하지 않았다.

#### 근본 원인 후보

- 정상 반환 뒤의 수동 `fill(0)`에 의존하고, crypto helper 내부의 key ownership과 caller plaintext ownership을 예외 안전한 scope로 통합하지 않은 것으로 보인다.

#### 반대 가설·오탐 검토

- 정상 경로가 이미 wipe된다는 이유로 실패 경계까지 안전하다고 볼 수 없다. 이 finding은 provider/암호화 예외가 실제로 발생했다는 주장이 아니라, 해당 예외에서 cleanup 계약이 코드상 보장되지 않는다는 정적 finding이다.
- request-ID mismatch 세부사항은 `decodeSecureFrame()`가 GCM 인증을 먼저 통과한 뒤 반환하는 경우에만 성립한다. 외부 비인증 frame이 plaintext를 읽게 하거나 response binding을 우회한다는 finding으로 확대하지 않았다.
- ACK 자체에는 raw PDF가 없고 `PcPdfSender` outer `finally`가 frame/requestId/pdfHash를 지우므로, ACK 예외 세부사항을 PDF 평문 노출로 과장하지 않았다.
- `LUNA-0004`의 CSV 복호화 map, `LUNA-0013`의 CSV parser immutable copy와 대상 buffer가 다르므로 중복 처리하지 않았다.
- QR/PDF export·print/share 자체의 bitmap·cache file 정리 경계는 별도로 확인했으며, Android 공유 `EXTRA_SUBJECT`의 표시 이름은 제품 문서가 경고하는 사용자가 선택한 신뢰 대상 공유 경계로 분리해 이 finding에 포함하지 않았다.

#### 기존 테스트가 잡지 못한 이유

- 벡터 테스트와 변조 입력 테스트는 정상 암호화 결과·인증 실패 반환만 검증하며, crypto helper가 예외를 던진 뒤 private key/plaintext 배열이 지워졌는지를 연결하지 않는다.
- `PcControlProtocolTest`는 정상 CSV response와 tampered frame만, `PcTransferProtocolTest`는 정상 PDF/ACK vector와 tampered ACK만 확인한다. request-ID mismatch 뒤 `decoded.plaintext` cleanup, provider/DoFinal 예외, `PcPdfSender` ACK `finally`와 status JSON String ownership을 연결하는 시험은 없다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 PDF/control encode와 ACK verify의 key·plaintext·temporary buffer를 `try/finally` 소유 scope로 묶고, frame 반환값만 wipe 대상에서 제외한다. 정상 protocol vector와 오류 메시지·재시도 semantics는 유지해야 한다.
- PDF 암호화 실패, status/CSV control 암호화 실패, ACK HMAC 실패, socket timeout 결합 경로에서 cleanup 계약을 fault injection 또는 검증 가능한 test seam으로 확인하고, 실제 payload/heap에 민감값이 남지 않는지 별도 안전한 방법으로 검증해야 한다. 이번 Goal에서는 수정·실행하지 않는다.
- control response는 request-ID 비교를 포함한 전체 `SecurePlaintext` 수명에 `finally`를 씌우고, PDF ACK는 성공·실패·read 중단 모두 caller가 wipe하도록 소유권을 재배치해야 한다. status JSON/label String은 immutable 특성상 설계상 잔류 한계를 명시하거나 String 생성 범위를 줄이는 검토가 필요하다. 기존 vector·ACK binding·retry semantics를 보존해야 한다.

#### 관련 발견·결정

- `LUNA-0014`는 `LUNA-0002`의 PC 설정 at-rest, `LUNA-0004`·`LUNA-0013`의 CSV parsing/복호화 수명 문제와 다른 PC 전송 암호화 buffer ownership 경계다.
- PC 전송 실패 시 민감 buffer 정리와 재시도 정책은 Sol·사용자 재판단 큐에 남긴다.
- `LUNA-0041`은 PC receiver의 immutable CSV bytes/plaintext/ciphertext/frame owner이고, 이번 `LUNA-0014`는 Android Kiosk의 encode/decode/ACK owner다. `LUNA-0010`의 ACK 유실·재시도 중복 semantics와도 분리한다.

### LUNA-0015 — 학생 비활성화 뒤 암호화된 자격정보 레코드가 앱 private DB에 남음

- 심각도: `P3`
- 상태: `기존 알려진 문제`
- 신뢰도: 높음
- 최초 발견·마지막 정적 확인: 2026-08-02 07:49:05 / 2026-08-02 20:04:49 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `StudentRepository.deactivateStudent()`·Room `students` 레코드

#### 사실

- `docs/KNOWN_LIMITATIONS.md:27`은 학생 비활성화를 “기존 QR hash를 새 무작위 hash로 교체하고 활성 조회에서 제외하는 논리적 비활성화”로 정의하면서, 앱 private DB의 암호화된 자격정보 레코드는 보안 삭제하지 않는다고 명시한다.
- `StudentRepository.deactivateStudent()`는 새 QR hash를 만들어 `StudentEntity.qrTokenHash`에 저장하고 `isActive = false`로 바꾸지만, `usernameCiphertext`·`usernameIv`·`passwordCiphertext`·`passwordIv`와 encryption version 열은 지우거나 덮지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/StudentRepository.kt:619-632`). transaction의 다른 영속 변경은 `updatedAtEpochMs`와 `QR_REVOKED`·`STUDENT_DEACTIVATED` audit event뿐이다. `StudentDao`의 활성 목록·QR 검증은 비활성 학생을 제외하지만, `findById()` 자체에는 활성 조건이 없다.
- 정상 UI 복호화 경로인 `decryptCredentials()`는 비활성 학생을 거부하므로 논리적 사용 차단은 확인된다. 그러나 DB 레코드와 암호화된 자격정보의 보존 기간·보안 삭제 호출은 이 경로에 없다.
- production `StudentRepository`·`StudentDao`에는 학생 레코드의 delete/purge/erase API나 비활성 시각·retention 필드가 없고, retention cleanup job 또는 credential purge migration도 찾지 못했다. Room version 3의 migration은 QR status table과 admin `pinLength`만 추가하며 기존 `students` credential ciphertext/IV를 보존한다. `QrCardStatusEntity`의 `ON DELETE CASCADE`는 학생 삭제가 실제로 호출될 때의 FK 동작일 뿐, 현재 학생 삭제 경로의 근거가 아니다.
- `RepositoryInstrumentedTest`는 비활성 뒤 구 QR 거부·active 목록 제외·row 존재·비활성 credential update 거부와 입력 배열 wipe·audit event를 확인하지만 credential ciphertext/IV/version이 비워지거나 일정 기간 뒤 purge되는지는 확인하지 않는다. `KioskDatabaseMigrationInstrumentedTest`도 migration에서 credential fields를 가진 학생을 보존하고 QR status·`pinLength`만 검증한다. `CredentialCipherInstrumentedTest`는 AES-GCM/서로 다른 IV/AAD 실패를 검증할 뿐 deactivate retention은 검증하지 않는다.
- `AndroidManifest.xml`의 `allowBackup=false` 및 `data_extraction_rules.xml`의 cloud/device-transfer database 제외, `CredentialCipher`의 AES-GCM field encryption은 저장·복제 노출을 줄이는 반대 근거다. 이는 앱 private DB 내부의 inactive ciphertext를 secure-delete하거나 retention하는 계약과는 별개다.
- 관련 history/blame에서 `deactivateStudent()`는 `e093bd0`에 도입되었고 active guard/hash/audit 보강은 `9eaf2c5`에 있으며, credential field 검색의 후속 변경은 `0193d89`의 kiosk operations까지 확인된다. 이후 history에서 비활성 학생 credential purge를 도입한 변경은 확인되지 않았다.

#### 추론

- 학생을 비활성화해도 동일 앱 private DB에 암호문과 IV가 남으므로, 제품이 요구하는 “비활성화”는 접근 차단·QR 폐기이지 데이터 최소화나 보안 삭제까지는 아니다. Keystore 키가 외부에 노출됐다는 사실이나 실제 DB/키 추출은 확인하지 않았다.
- 이 항목은 문서에 이미 공개된 제한이므로 신규 확정 결함으로 집계하지 않고 `기존 알려진 문제`로 표시한다. `LUNA-0006`의 QR hash 배열 회수 누락은 비활성화 호출 중 transient hash의 메모리 ownership 문제라 persistent credential retention과 분리한다.

#### 반대 근거

- `KNOWN_LIMITATIONS.md:27`과 `SECURITY.md` Gate 4는 논리적 비활성화·QR hash 교체·활성 조회 제외·암호화 credential record 비보안 삭제를 같은 정책으로 명시한다. 따라서 이번 대조는 문서-구현 불일치가 아니라, 보안 삭제·retention 기준이 없는 이미 공개된 제품 제한을 재확인한 것이다.
- active query와 `decryptCredentials()`·credential update의 `isActive` guard는 비활성 row가 정상 UI credential path로 재사용되는 것을 막는다. AES-GCM at-rest와 backup/data-transfer 제외도 영향 범위를 줄이지만, DB row의 존재·보존 기간을 없애지는 않는다.

#### 가정과 미검증

- 앱 데이터 삭제·DB export·Keystore 접근·복호화 실행은 하지 않았다. 실제 암호문 보존 기간과 ART/SQLite page overwrite 상태도 측정하지 않았다.
- 비활성화가 활성 수업 중 허용되는지에 대한 별도 제품 결정은 현재 문서에서 확정하지 못했다. pending QR/prelogin과 관리자 화면의 callback 경합은 `LUNA-0005`에만 추가했다.

#### 안전한 확인 절차

- `KNOWN_LIMITATIONS.md`, 제품 결정, `StudentRepository`, `StudentDao`, Room entity/migration과 관련 정적 테스트를 읽기 전용으로 대조했다.
- `AndroidManifest.xml`, `data_extraction_rules.xml`, `CredentialCipher`, 관련 `git log`·`git blame`도 읽기 전용으로 대조했다.
- DB·Keystore·학생 데이터·PIN·QR 원문은 사용하지 않았고, 테스트·빌드·설치·Git 쓰기는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 비활성화 정책이 논리적 revocation만을 의미하는지, 또는 암호화 자격정보의 기간 제한·폐기까지 포함하는지 문서와 구현이 명확히 일치해야 한다.
- 실제: 문서와 구현은 논리적 비활성화 및 암호화 자격정보 레코드 보존으로 일치한다. 현재 production path에는 credential secure-delete·retention 기준·purge job이 제공되지 않는다.

#### 영향 및 재판단 큐

- 보안/개인정보: 동일 앱 private DB에 더 이상 활성 사용되지 않는 학생 자격정보 암호문이 남는다. Android backup 제외와 AES-GCM at-rest는 확인했지만, 로컬 관리자·동일 계정 권한·장기 보존 위협 모델에 따라 영향 재평가가 필요하다.
- Sol·사용자 재판단 후 비활성 학생의 암호문을 즉시 폐기할지, 복구·감사·재등록 요구 때문에 보존할지와 보존 기간·키 회전 정책을 결정해야 한다. 이번 Goal에서는 구현하지 않는다.

#### 기존 테스트가 잡지 못한 이유

- 기존 시험의 완료 기준은 QR revocation, active lookup exclusion, inactive mutation rejection과 입력 배열 cleanup이다. ciphertext/IV/version을 null·zero 값으로 바꾸거나 row를 삭제해야 한다는 제품 계약이 없으므로 secure deletion·retention을 실패 조건으로 만들지 않았다.
- migration 시험은 기존 학생·credential field의 호환 보존을 확인하는 목적이어서, 현재 migration 경계에서도 비활성 row purge를 기대하지 않는다.

### LUNA-0016 — 네트워크 대기 패널의 반투명 배경이 답안 비노출을 완전히 보장하지 않음

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 정적 확인: 2026-08-02 08:08:28 / 2026-08-02 20:07:58 (Asia/Seoul)
- 영향 모듈·버전: Web POC `MainActivity.updateNetworkPause()`·`activity_main.xml` 네트워크 대기 패널

#### 사실

- `docs/PRODUCT_DECISIONS.md:96-97`은 통신이 끊기면 현재 WebView와 답안을 유지하되 입력을 가리도록 정하고, 사용자 변경 필드 점검표 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md:257-263`은 연결 대기 화면에서 답안이 노출되거나 입력되지 않아야 한다고 적는다.
- `webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:2246-2258`의 `updateNetworkPause()`는 `networkPausePanel.visibility`만 `VISIBLE/GONE`으로 바꾸고 WebView의 내용을 제거하거나 별도 불투명 차폐 surface로 교체하지 않는다. `showActive()`는 WebView를 `VISIBLE`로 유지하고(`:1758-1772`), 네트워크 callback·main-frame error·state transition이 이 visibility 토글을 호출한다. 이는 답안을 보존해야 한다는 제품 결정과는 일치하지만, 차폐 강도 자체를 검증하는 경계는 없다.
- `webpoc/src/main/res/layout/activity_main.xml:455` 이후의 패널은 WebView 다음에 선언된 마지막 전체 화면 자식이고 `android:background="#F2102A43"`를 사용한다. Android `#AARRGGBB` 표기에서 `F2`는 알파 255가 아닌 242이므로 완전 불투명 배경이 아니다.
- 같은 layout의 `network_pause_panel` 내부 `LinearLayout`(`activity_main.xml:463-469`)에는 background가 없어, 진행 표시·안내문 주변의 전체 화면 overlay도 이 반투명 배경을 통해 하위 WebView와 합성된다. `clickable`·`focusable` 선언은 터치/포커스 경계이지 시각적 opaque 보장으로 볼 수 없다.
- 패널은 `clickable`·`focusable`이지만, 반투명 배경 아래의 WebView가 시각적으로 완전히 제거되었다는 정적 근거는 없다. 실제 답안 문자열이 읽힐 정도인지 판정할 화면 캡처·렌더링 시험은 수행하지 않았다.
- `webpoc/src/test`·`src/androidTest`에서 `NETWORK_PAUSE`·`networkPausePanel`·`NetworkCallback`을 검증하는 UI/화면 차폐 시험 연결은 찾지 못했다. 테스트는 계약상 실행하지 않았다.
- 관련 history/blame에서 네트워크 대기 구현과 `#F2102A43`는 `d8ca06f`(2026-07-30)에서 도입되었고, 현재 해당 lines의 후속 commit은 확인되지 않았다. `docs/PRODUCT_DECISIONS.md:96-97`은 같은 commit에서 “WebView와 답안을 유지하면서 입력을 가림”으로 확정되었지만, `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md:252-263`의 실제 단절·비노출 검증 항목은 아직 체크되지 않았다.
- 과거 `docs/WEB_POC_VERIFICATION.md:62-66,135`·`WEB_POC_FAILURE_LOG.md:10`의 W05는 0.1.0 계열에서 Wi-Fi 단절 후 `LOCKED / NETWORK_ERROR`를 기록한 historical evidence다. 이는 현재 `d8ca06f`의 pause overlay가 시각적으로 답안을 가리는지 증명하지 않으며, 최신 `docs/BUILD_VERIFICATION.md:5785-5790`도 실제 통신 단절 현장 조작을 미검증으로 남긴다.

#### 추론

- 고대비 답안·수식·커서가 반투명 오버레이 아래에서 희미하게 보일 가능성이 있어, “답안 비노출” 요구를 코드만으로 보장할 수 없다. 실제 가독성은 기기·밝기·WebView 렌더링에 의존하므로 현재는 후보로 둔다.
- 네트워크 단절은 학생 답안이 화면에 열린 상태에서 발생할 수 있으므로, 차폐 실패가 확인되면 같은 화면을 보는 사람에게 답안이 노출되는 개인정보·시험 보안 문제가 된다. 원격 전송이나 DB 유출을 정적으로 확인한 것은 아니다.

#### 반대 근거

- full-screen child가 WebView 뒤에 있고 `clickable`·`focusable`이라 일반 터치가 하위 WebView로 통과하지 않을 가능성, `FLAG_SECURE`가 OS 화면 캡처를 제한하는 점, 네트워크 복구 시 기존 WebView를 재사용하려는 제품 의도는 위험을 줄이는 반대 근거다. 그러나 어느 것도 화면 합성의 완전 불투명성이나 실제 답안 가독성 부재를 증명하지 않는다.
- 과거 W05의 정상 복구 기록은 현재 source의 network pause 상태 전이를 직접 검증하지 않으므로 현재 결함의 반증으로 사용하지 않았다. 반대로 최신 field checklist의 미체크 상태도 기능 실패 자체의 증거로 부풀리지 않았다.

#### 가정과 미검증

- `#F2102A43`가 표준 Android 색상 리터럴로 해석된다는 전제를 적용했다.
- 실제 A 화면에서 네트워크 단절·답안 화면·밝기·캡처를 조합한 가시성 검증은 라이브 상태 변경과 답안 사용이 필요해 수행하지 않았다.
- 오버레이가 시스템 compositor나 WebView의 실제 렌더링 정책에 의해 사실상 식별 불가능해질 가능성은 배제하지 않았다.

#### 안전한 확인 절차

- 제품 결정, 현재 변경 필드 점검표, Web POC Activity의 네트워크 전환 코드와 레이아웃을 읽기 전용으로 대조했다.
- 네트워크 차단·WebView 실행·학생 세션·답안 입력·화면 캡처는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 연결 대기 중 WebView/답안을 유지하더라도 답안이 보이지 않도록 완전 불투명하고 검증 가능한 차폐가 제공된다.
- 실제: 전체 화면 패널은 유지되지만 `#F2` 알파의 반투명 배경을 사용하며, 차폐 가시성 회귀시험은 정적으로 연결되지 않았다.

#### 영향 및 재판단 큐

- 보안/개인정보: 네트워크 대기 중 답안·수식이 희미하게 노출될 가능성.
- Sol·사용자 재판단 후 패널을 완전 불투명 색으로 바꾸거나 WebView를 별도 opaque blocker 아래에 두고, 실제 답안 화면에서 연결 끊김·복구 전후 차폐 가시성 회귀검증을 정해야 한다. 이번 Goal에서는 구현하지 않는다.

#### 근본 원인 후보와 수정 후 검증

- 근본 원인 후보는 승인된 편의 기능을 도입한 `d8ca06f`가 WebView 보존을 위해 visibility overlay를 추가했지만, 답안 비노출을 별도 opaque surface·rendering contract·회귀시험으로 정의하지 않은 것이다. 기존 색상 팔레트의 `#F2102A43`를 재사용한 점도 차폐 요구와 독립적으로 검증되지 않았다.
- 수정 판단은 Sol·사용자가 “WebView를 보존하면서 완전 불투명 blocker를 둘지”와 복구 시 동일 WebView·입력 상태를 유지할지 함께 결정해야 한다. 수정 후에는 실제 답안·네트워크 변경이 필요한 field 검증 대신, 안전한 정적/fixture 범위에서 visibility·layer·state callback을 확인하고, 사용자 승인 하에 현장 비노출·재개 검증을 별도로 수행해야 한다.

#### 관련 발견

- WebView의 포커스·IME·일반 키 입력 차단은 시각적 차폐와 별도 경계인 `LUNA-0017`에서 추적한다. 이번 항목은 화면 합성의 불투명성·답안 가독성 보장만 다룬다.

#### 기존 테스트가 잡지 못한 이유

- 기존 Web DOM/상태 시험은 네트워크 대기 Android View의 alpha·레이어 합성·실제 화면 가시성을 검증하지 않는다.

### LUNA-0017 — 네트워크 단절 시 WebView 포커스·IME·일반 키 입력 차단이 명시되지 않음

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 정적 확인: 2026-08-02 08:08:28 / 2026-08-02 16:51:17 (Asia/Seoul)
- 영향 모듈·버전: Web POC `MainActivity` 네트워크 대기·WebView 입력 생명주기

#### 사실

- `MainActivity.kt:2246-2258`의 `updateNetworkPause()`는 패널 표시와 무입력 timer만 조정한다. `webView.clearFocus()`, `InputMethodManager.hideSoftInputFromWindow()`, WebView 비활성화 또는 입력 대상 전환을 호출하지 않는다. pause 진입·복구의 state transition에서도 이 focus/IME 정리 호출은 없다.
- `MainActivity.kt:875-890`의 Activity key event 처리는 BACK만 소비한다. 네트워크 대기 패널이 보이는 동안 일반 키·방향키·삭제키를 차단하는 조건이나 `dispatchKeyEvent()` override는 확인되지 않았고, 그 밖의 key는 `super`로 전달된다.
- `MainActivity.kt:2285-2295`의 `dispatchTouchEvent()`는 ACTIVE·패널 비표시일 때 무입력 timer를 예약하는 조건만 바꾸고 마지막에 항상 `super.dispatchTouchEvent(event)`를 호출한다. panel의 `clickable=true`가 일반 touch를 소비할 가능성과 WebView focus·IME·hardware key routing은 별도 경계다.
- 네트워크 단절은 `state`를 별도 잠금 상태로 전환하지 않고 `networkPausePanel`을 표시한다. 따라서 WebView는 ACTIVE 화면의 기존 포커스·입력 생명주기를 유지할 수 있는 구조다.
- `activity_main.xml:454-461`의 panel은 `focusable/clickable`만 선언하고 `focusableInTouchMode`, `requestFocus`, key listener 또는 접근성 입력 차단 속성을 선언하지 않는다. 실제 visibility 전환 시 Android focus가 자동으로 이동하는지와 IME가 포커스된 WebView에 남는지는 실행하지 않았다.
- 코드 검색에서 이 네트워크 전환 경로에 `InputMethodManager`, `hideSoftInput`, `clearFocus` 또는 일반 key 차단 연결은 찾지 못했다. `RecoveryInstrumentedTest`의 key 시험은 BACK 소비만 확인하고, setup 입력의 autofill/touch filtering 시험은 network pause와 연결되지 않는다.
- 관련 history/blame에서 BACK 소비는 `8b7e0d1`(2026-07-28), pause panel과 `dispatchTouchEvent()`는 `d8ca06f`(2026-07-30)에 도입되었으며 이후 해당 경계에 focus/IME/key barrier를 추가한 변경은 확인되지 않았다.

#### 추론

- 연결이 끊기는 순간 수식 입력칸이 포커스되고 소프트 키보드가 열린 상태라면, 패널이 터치를 막더라도 키보드·IME 또는 하드웨어 키 이벤트가 기존 WebView에 도달할 가능성이 있다. 그러면 “입력 차단” 요구와 다르게 숨겨진 답안 상태가 변경될 수 있다.
- `focusable=true`인 패널이 모든 입력 경로에서 자동으로 포커스를 가져온다는 보장은 코드에 없으며, 반대로 포커스를 가져가더라도 이미 열린 IME를 숨긴다는 코드가 없다. 이 불확실성 때문에 확정이 아닌 후보로 분류한다.

#### 반대 근거

- full-screen `network_pause_panel`이 `clickable=true`·`focusable=true`이고 WebView 위에 선언되어 일반 touch를 차단할 가능성은 있다. BACK은 Activity에서 명시적으로 소비하고, pause 진입 시 무입력 timer도 취소한다. 그러나 이 반대 근거는 IME·hardware key·접근성 입력까지 차단하거나 현재 focus를 옮긴다는 증거가 아니다.
- `filterTouchesWhenObscured=true`가 WebView에 설정되어 있고, `onStop()`·renderer recovery는 별도 lifecycle을 정리한다. 이는 다른 Window의 obscured touch와 종료 경계에 대한 보호이지, network pause 중 focused WebView의 IME/key state를 폐쇄하는 계약은 아니다.

#### 가정과 미검증

- Android 입력 라우팅은 현재 포커스·IME 상태와 제조사/키보드 구현에 따라 달라질 수 있다고 보았다.
- 실제 수식 입력칸 포커스→네트워크 단절→키 입력·IME 유지 여부와 복구 후 답안 상태는 답안 세션과 네트워크 변경을 필요로 하므로 검증하지 않았다.
- 패널의 터치 차폐 자체는 레이아웃상 확인했지만, 키·IME·접근성 입력까지 차단한다고 추정하지 않았다.

#### 안전한 확인 절차

- Activity의 네트워크 상태 전환·키 이벤트·터치 dispatch와 WebView 레이아웃을 읽기 전용으로 대조했다.
- `RecoveryInstrumentedTest`, `WebDomScriptsTest`, `WEB_POC_TEST_PLAN.md`, 제품 결정·field checklist와 관련 Git history/blame을 읽기 전용으로 대조했다.
- 네트워크 차단·IME 조작·WebView 실행·답안 입력·테스트는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 연결 대기 진입 시 WebView 입력 포커스와 IME를 안전하게 해제하고, 복구 전에는 터치·키·IME·접근성 입력이 모두 답안을 변경하지 않는다.
- 실제: 현재 코드는 전체 화면 clickable/focusable 패널을 표시하지만 WebView/IME 해제와 일반 키 조건부 차단을 명시하지 않는다.

#### 영향 및 재판단 큐

- 무결성/운영: 연결 대기 중 답안이 변경되거나 키보드가 계속 노출되어 시험 화면 차폐·재개 기준이 흔들릴 가능성.
- Sol·사용자 재판단 후 네트워크 pause 진입 시 포커스·IME 해제, Activity/View 입력 차단, 복구 시 허용된 포커스 복원을 하나의 상태 전이로 정의하고 실제 수식 입력 회귀시험을 정해야 한다. 이번 Goal에서는 구현하지 않는다.

#### 근본 원인 후보와 수정 후 검증

- 근본 원인 후보는 `d8ca06f`가 네트워크 pause를 시각적 overlay와 timer 상태로만 구현하고, WebView focus·IME·key/accessibility 입력을 함께 관리하는 별도 입력 barrier를 상태 계약으로 만들지 않은 것이다.
- 수정 판단은 WebView와 답안을 보존하면서 pause 진입 시 focus/IME/key 경계를 닫고 복구 시 허용된 입력만 되살리는 단일 state contract를 Sol·사용자가 정해야 한다. 수정 후에는 focus가 실제로 옮겨지는지, IME가 닫히는지, 일반 key/접근성 입력이 답안을 바꾸지 않는지와 복구 후 재로그인 없는 입력 재개를 사용자 승인 범위에서 회귀검증해야 한다.

#### 관련 발견

- `LUNA-0016`은 같은 panel의 시각적 불투명성·답안 가독성 경계이고, `LUNA-0018`은 callback 등록 실패 시 감시 fallback 경계다. 이번 항목은 입력 routing만 다룬다.

#### 기존 테스트가 잡지 못한 이유

- 기존 DOM/MathQuill 시험은 Android Window의 IME·포커스·KeyEvent routing과 네트워크 전환을 함께 검증하지 않으며, 현재 정적 테스트 검색에서도 해당 pause UI 계약을 찾지 못했다.

### LUNA-0018 — 네트워크 감시 callback 등록 실패 시 전용 fallback이 없어 연결 대기 전환이 지연될 수 있음

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 정적 확인: 2026-08-02 08:08:28 / 2026-08-02 16:55:23 (Asia/Seoul)
- 영향 모듈·버전: Web POC `MainActivity.registerNetworkMonitor()`·ACTIVE 네트워크 대기 상태

#### 사실

- `MainActivity.kt:2220-2229`의 `registerNetworkMonitor()`는 `ConnectivityManager`가 없으면 조용히 반환하고, `registerDefaultNetworkCallback()`가 실패하면 `NETWORK_MONITOR_FAILED` private 진단 이벤트만 남긴다. 실패 뒤 재등록·주기 확인·별도 watchdog은 이 경로에서 확인되지 않았다.
- `initializeUi()`는 `registerNetworkMonitor()`를 `uiInitialized = true`보다 먼저 호출하므로 등록 직후의 `updateNetworkPause()`는 early return한다. 이후 ACTIVE로 전환할 때 `transition()`이 `handler.post(::updateNetworkPause)`를 예약하므로 초기 ACTIVE 진입 시 현재 capability를 다시 평가하는 보완 경로는 있다.
- `hasValidatedNetwork()`는 현재 active network의 `INTERNET`·`VALIDATED` capability를 읽을 수 있지만, 호출은 ACTIVE state transition·callback refresh·WebView main-frame error와 연결되어 있다. callback 등록 실패 후 페이지 요청이나 state transition이 없는 조용한 네트워크 단절을 즉시 표시하는 별도 감시 경로는 확인되지 않았다.
- `onReceivedError()`는 ACTIVE main-frame 오류와 validated network 부재를 함께 볼 때 `updateNetworkPause()`를 호출한다. 이는 요청 오류가 발생한 경우의 보완책이지, callback 등록 실패를 일반적으로 복구하는 fallback은 아니다. validated network가 없지 않은 서버/route 오류는 기존 `showLocked()` policy로 처리된다.
- `webpoc/src/main/AndroidManifest.xml:5-6`에는 `INTERNET`·`ACCESS_NETWORK_STATE`가 있어 정상 Android 경로의 capability 조회·callback 등록을 지원한다. 이는 예외·null·system resource failure 후 복구를 보장하지 않는다.
- 현재 테스트 검색에서 `NETWORK_MONITOR_FAILED`, `registerDefaultNetworkCallback`, `hasValidatedNetwork`, `updateNetworkPause` 또는 callback registration failure를 직접 주입하는 시험 연결은 찾지 못했다. `NetworkFailureReasonTest`·`WebFailurePolicyTest`는 오류 분류/policy 순수 함수만 다룬다. 테스트는 계약상 실행하지 않았다.
- 관련 history/blame에서 monitor·fallback 구현은 `d8ca06f`(2026-07-30)에 도입되었고 이후 등록 실패 재시도·watchdog을 추가한 변경은 확인되지 않았다. `docs/WEB_POC_TEST_PLAN.md:28,40`의 W05 “현재 완료” 기록은 baseline `eb09567`(2026-07-22)으로 pause 구현 전 문서이며, `4bbeb98`(2026-07-30) 최신 release 검증은 실제 통신 단절 현장 조작을 미검증으로 기록한다.

#### 추론

- 드문 시스템 서비스 오류·권한/자원 예외로 callback 등록이 실패한 뒤 ACTIVE 화면이 유휴 상태에서 연결이 끊기면, 대기 패널이 나타나지 않고 `LUNA-0016`/`LUNA-0017`의 시각·입력 보호 전환도 늦어질 수 있다. 페이지 요청이나 ACTIVE 재진입이 발생하면 `onReceivedError()`·`transition()`이 일부 보완할 수 있어 P4 후보로 둔다.
- 현재 A나 정상 Android 환경에서 등록 실패가 발생했다는 사실은 확인하지 않았다. 따라서 등록 실패 자체를 기능 실패로 확정하지 않고, 코드상 failure completeness와 유휴 상태의 미검증 경계로 한정한다.

#### 반대 근거

- 정상 경로에는 `ACCESS_NETWORK_STATE`, `registerDefaultNetworkCallback()`와 `onAvailable`·`onLost`·`onCapabilitiesChanged` refresh가 있고, ACTIVE 전환·main-frame error가 capability를 재평가한다. 이 경로들은 등록이 성공하거나 다른 이벤트가 발생할 때 pause 전환을 보완한다.
- `docs/BUILD_VERIFICATION.md`는 편의성 묶음 자동 검증 94/94와 release 검증을 기록하고, 과거 W05/G306은 network failure를 안전하게 `LOCKED`로 처리한 정상 경로 근거다. 그러나 해당 기록에는 current callback-registration exception/유휴 ACTIVE 조건이 없고, 최신 field checklist는 현장 단절 항목을 미체크로 남긴다.

#### 가정과 미검증

- `registerDefaultNetworkCallback()`의 예외 가능성과 ConnectivityManager null 경로를 실제 기기에서 유도하지 않았다.
- 네트워크 단절·서비스 오류·callback 재등록·유휴 ACTIVE 화면을 결합한 런타임 시험과 지연시간 측정은 수행하지 않았다. ADB가 없어 현재 A의 permission/service 상태도 확인하지 못했다.

#### 안전한 확인 절차

- 네트워크 monitor 등록, validated capability 판정, WebView 오류 보완 경로, manifest permission, `WEB_POC_TEST_PLAN.md`·`BUILD_VERIFICATION.md`·field checklist와 관련 `git log`·`git blame`을 읽기 전용으로 대조했다.
- 네트워크 차단·권한 변경·서비스 장애 유도·앱 실행·테스트는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: callback 등록에 실패해도 bounded retry·periodic validated check·안전한 입력 차단 중 하나로 연결 상태를 감시한다.
- 실제: 등록 실패 자체는 private log로만 관찰되고, 별도 retry/watchdog은 정적으로 확인되지 않는다. 다만 ACTIVE transition과 main-frame error라는 독립 보완 trigger는 존재한다.

#### 영향 및 재판단 큐

- 운영/무결성: 특정 환경에서 네트워크 대기 전환이 늦어져 연결이 끊긴 화면이 잠시 입력 가능하게 남을 가능성.
- Sol·사용자 재판단 후 callback 재등록과 bounded fallback의 실패 정책·진단 코드·회귀시험을 정해야 한다. 이번 Goal에서는 구현하지 않는다.

#### 근본 원인 후보와 수정 후 검증

- 근본 원인 후보는 `d8ca06f`의 네트워크 대기 기능이 정상 callback 경로를 중심으로 구현되고, 등록 실패를 별도 상태·재시도·주기 점검 계약으로 모델링하지 않은 것이다. 예외를 private log로 남기는 것과 실제 안전 대기 전환을 보장하는 것은 분리된 경계다.
- 수정 판단은 등록 실패를 즉시 안전 대기·bounded retry·periodic check 중 어떤 정책으로 처리할지 Sol·사용자가 정해야 한다. 수정 후에는 정상 callback, 등록 예외, `ConnectivityManager` null, 유휴 ACTIVE 단절, main-frame error, 복구와 Activity 종료를 각각 확인하고 입력 차단·재로그인 없는 resume 회귀를 검증해야 한다.

#### 관련 발견

- `LUNA-0016`은 pause panel 시각 차폐, `LUNA-0017`은 pause 중 입력 routing, `LUNA-0019`는 접근성 subtree 경계를 다룬다. `LUNA-0018`은 이들 보호 상태로 진입시키는 monitor fallback만 다룬다.

#### 기존 테스트가 잡지 못한 이유

- 기존 `NetworkFailureReasonTest`·`WebFailurePolicyTest`와 release 문서의 성공 기록은 오류 분류·정상 경로를 다루지만 ConnectivityManager callback 등록 실패, ACTIVE 유휴 화면, 등록 후 단절과 입력 보호 전환을 다루지 않는다.

### LUNA-0019 — 네트워크 대기 패널이 접근성 트리에서 WebView 답안 노드를 숨긴다는 보장이 없음

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견: 2026-08-02 08:14:37; 마지막 확인: 2026-08-02 17:01:16 (Asia/Seoul)
- 영향 모듈·버전: Web POC `activity_main.xml` 네트워크 pause 접근성·WebView

#### 사실

- layout의 WebView는 `android:importantForAutofill="noExcludeDescendants"`만 선언하고 접근성 subtree를 제어하는 `importantForAccessibility`, `screenReaderFocusable`, `accessibilityDelegate`, `NO_HIDE_DESCENDANTS` 속성은 선언하지 않는다(`webpoc/src/main/res/layout/activity_main.xml:10-15`). `importantForAutofill`은 자동완성 수집 경계이지 접근성 트리 차폐 근거가 아니다.
- `network_pause_panel`은 full-screen 형제 레이어로 `android:clickable="true"`·`android:focusable="true"`와 안내 TextView만 선언한다. panel·중앙 LinearLayout·ProgressBar에는 접근성 subtree를 단일 안내 노드로 제한하거나 WebView 형제를 숨기는 속성이 없다(`webpoc/src/main/res/layout/activity_main.xml:454-486`).
- `configureSensitiveInputs()`는 setup 입력과 일부 버튼/WebView에 `importantForAutofill`·`filterTouchesWhenObscured`를 설정하지만 접근성 속성이나 delegate는 설정하지 않는다(`MainActivity.kt:380-410`). 따라서 touch-obscured 방어와 접근성 탐색 차폐를 같은 통제로 볼 수 없다.
- ACTIVE 진입의 `showActive()`는 `webView.visibility = View.VISIBLE`을 설정하고, `transition()`·`updateNetworkPause()`는 panel/idle warning visibility와 timer만 바꾼다(`MainActivity.kt:1758-1773, 2184-2218, 2246-2258`). pause 진입·복구 때 WebView 접근성 subtree를 숨기거나 panel에 접근성 focus를 명시적으로 반환하는 호출은 없다.
- `FLAG_SECURE`는 화면 캡처·최근 앱 미리보기 경계이며 Android 접근성 provider의 노드 노출·action 경계를 대신하지 않는다. 현재 Web POC 소스에서 두 경계를 연결하는 코드는 확인하지 못했다.
- Web POC layout·Activity의 접근성 관련 정적 검색은 위 일반 `focusable`·`clickable`·autofill 설정 외에 의미 있는 accessibility tree 제어를 찾지 못했다. Web POC unit/instrumented test 검색에도 TalkBack, Switch Access, `importantForAccessibility`, `NO_HIDE_DESCENDANTS`, accessibility delegate/action을 검증하는 시험은 없었다. 기존 시험의 `isAccessible = true`는 Kotlin reflection 접근성 플래그로, Android 접근성 서비스 검증이 아니다.
- 현재 `docs/THREAT_MODEL.md`는 Gate 1 Probe가 별도 `com.matholic.mathapp` 창을 읽는 접근성 서비스임을 적고, 학생명 등 접근성 text 유출을 위협으로 분류한다. `docs/BUILD_VERIFICATION.md`의 `enabled/bound` 기록과 접근성 fingerprint/Action 결과도 옛 `poc` Probe/Gate 1 범위이며 현재 Web POC pause panel의 형제 WebView subtree 계약은 아니다.
- `scripts/verify-gate5-device-owner.ps1`와 관련 운영 문서는 Device Owner·Lock Task·allowlist·버전은 확인하지만, 전용 운영기기에서 모든 접근성 서비스를 비활성화하거나 Web POC 접근성 노드를 차단한다는 정책은 확인하지 못했다. 따라서 접근성 서비스가 항상 꺼져 있다고 가정하지 않는다.

#### 반대 근거

- full-screen panel이 WebView 뒤에 선언되고 `clickable`·`focusable`이므로 일반 touch hit-test와 일부 일반 focus 경로는 panel이 우선할 가능성이 있다. 이는 시각·일반 입력 경계를 보강하지만, 접근성 서비스가 형제 WebView의 virtual node를 탐색하지 않는다는 증거는 아니다.
- 옛 Probe에는 package 제한·runtime exact-package 검사·고정 UI allowlist/fingerprint가 있고, Device Owner/Lock Task는 일반적인 kiosk escape를 줄인다. 그러나 이 통제들은 별도 Probe의 접근성 수집 범위 또는 앱 전경·패키지 실행 정책이며, 현재 Web POC pause 상태에서 WebView subtree를 `NO_HIDE_DESCENDANTS`로 만드는 증명은 아니다.
- `importantForAutofill`, `filterTouchesWhenObscured`, `FLAG_SECURE`가 존재한다는 사실은 각각 자동완성·가려진 touch·캡처 통제를 보여 주지만 접근성 노드·action 차폐의 반증으로 사용할 수 없다.

#### 추론

- 접근성 서비스가 활성화된 기기에서 network pause가 보이는 동안 underlying WebView의 답안·입력 요소가 접근성 탐색 대상에 남으면, 시각적 패널을 우회해 답안이 읽히거나 입력 action이 전달될 가능성이 있다.
- 실제 Android accessibility tree의 sibling/virtual node 노출과 action 전달은 서비스·WebView·Android 버전에 따라 달라질 수 있어 실제 노출이나 악용을 확정하지 않고 후보로 둔다.
- 현재 정적 근거의 핵심은 pause 상태에 접근성 subtree 상태 계약이 없다는 점이며, `LUNA-0016`의 반투명 시각 차폐, `LUNA-0017`의 IME/key routing, `LUNA-0018`의 network callback fallback과는 별도 finding으로 유지한다.

#### 가정과 미검증

- WebView가 ACTIVE 화면에서 접근성 virtual node를 제공할 수 있고, panel의 clickable/focusable만으로 형제 subtree가 자동 제거되지 않는다는 보수적 가정을 적용했다.
- TalkBack·Switch Access·접근성 서비스 설정·네트워크 단절·복구·답안 화면·접근성 focus/action·ADB는 실행하지 않았다.
- 실제 전용기기에서 접근성 서비스가 제한·비활성화되어 있는지, panel 표시 중 Android가 WebView sibling node를 어떻게 노출하는지, 접근성 action이 실제 WebView 입력까지 전달되는지는 확인하지 않았다.

#### 안전한 확인 절차

- WebView/panel layout, Activity visibility·network pause 호출 경계, Web POC 시험, Threat Model·Build Verification·Device Owner 점검 스크립트와 관련 blame/history를 읽기 전용으로 대조했다.
- 접근성 서비스 활성화, 네트워크 차단, WebView/학생 세션·답안 사용, 접근성 action과 화면 캡처는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 연결 대기 동안 안내 패널만 접근성 focus 대상이 되고 WebView 답안/입력 subtree는 명시적으로 숨겨지며, 검증된 복구 후에만 원래 접근성을 복원한다.
- 실제: panel은 focusable/clickable이고 일반 touch/focus 차단 가능성은 있으나, 형제 WebView의 접근성 차폐·focus 복원 계약과 해당 Android UI 회귀시험은 찾지 못했다.

#### 영향 및 재판단 큐

- 개인정보/무결성: 접근성 사용 환경에서 연결 대기 중 답안 노출 또는 입력 우회가 발생할 가능성. 실제 서비스·버전 조합에 따른 재현은 미확인이다.
- Sol·사용자 재판단 후 pause 진입 시 WebView 접근성 subtree를 숨기고 panel 안내만 노출하는 정책, 접근성 focus 반환 규칙과 실제 TalkBack/접근성 action 회귀시험을 정해야 한다. 이번 Goal에서는 구현하지 않는다.
- 판정: 정적 누락과 위협 모델의 영향은 확인했지만 실제 접근성 tree/action을 사용하지 않았으므로 `LUNA-0019` P3 후보·중간 신뢰도를 유지한다.

#### 기존 테스트가 잡지 못한 이유

- 현재 Web DOM/MathQuill·Android lifecycle 시험은 접근성 service의 virtual node 탐색·action 전달과 network pause 레이어를 함께 검증하지 않는다. 옛 Gate 1 Probe의 accessibility test 결과를 현재 Web POC pause의 증거로 재사용하지 않았다.

### LUNA-0020 — 원격 지원 만료 전 Kiosk PIN·Web 자격정보 화면의 `FLAG_SECURE` 차단이 운영자 주의에만 의존함

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견: 2026-08-02 08:17:58; 마지막 확인: 2026-08-02 17:08:56 (Asia/Seoul)
- 영향 모듈·버전: Kiosk/Web `RemoteSupportWindowController`·Kiosk 관리자 PIN·Web POC 자격정보 입력 화면·원격 지원 생명주기

#### 사실

- Kiosk와 Web `MainActivity.onCreate()`는 먼저 `FLAG_SECURE`를 추가하지만, 원격 지원 controller는 유효한 `RemoteSupportStore.activeUntilEpochMillis()`가 있으면 Activity window에서 즉시 `FLAG_SECURE`를 지운다(`kiosk/MainActivity.kt:337-349, webpoc/MainActivity.kt:152-169`, 각 `RemoteSupportWindowController.kt:31-44`). 이 판단에는 현재 화면·민감 입력 focus·setup/auth panel 상태가 포함되지 않는다.
- Kiosk controller는 만료 runnable, SharedPreferences 변경, 명시적 `stop()` 또는 비활성 상태에서만 `FLAG_SECURE`를 다시 추가한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/RemoteSupportWindowController.kt:31-64`). `stop()` 호출은 현재 `MainActivity.onDestroy()` 경로에만 연결되어 있고, 관리자 PIN 화면·scanner·admin panel 전환에는 연결되어 있지 않다.
- Kiosk `showAuthentication()`은 PIN 입력창을 표시하고 `pinInput.requestFocus()`를 호출하지만 원격 지원을 종료하거나 window flag를 복원하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:661-687`). 초기 상태 복구(`loadInitialState()`), 실패 복구, `onStart()`의 `relockAdminOnStart`도 같은 함수를 호출한다(`MainActivity.kt:589-610, 3838-3856`). 따라서 Activity를 재생성하지 않는 관리자 재진입·relock에서는 유효한 원격 지원 상태가 그대로 남는다.
- Web `RemoteSupportWindowController`는 `remoteSupportWindowController.start()`와 `initializeUi()` 후 `refresh()`에서 같은 window flag를 적용하고, 활성 상태 callback으로 `WebView.setWebContentsDebuggingEnabled(true)`도 설정한다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:152-169, 211-223`, `RemoteSupportWindowController.kt:31-64`). `showSetup()`은 expected name·username·password 입력 화면을 표시하지만 controller를 중지하거나 secure flag를 재설정하지 않는다(`MainActivity.kt:1747-1756`).
- Web `configureSensitiveInputs()`는 `isSaveEnabled=false`, `importantForAutofill=NO`, `filterTouchesWhenObscured=true`만 적용하고, setup 입력값이 화면에 표시되는 동안 `FLAG_SECURE`를 복원하는 처리는 없다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:380-410`). `startLogin()`은 입력을 읽어 `EphemeralCredentials`로 넘기기 전에 UI를 지우지만, 이는 화면 캡처 차단과 다른 lifecycle 경계다(`MainActivity.kt:916-935`).
- Kiosk 원격 지원 버튼은 관리자 화면에 있고 이미 관리자 인증 후에 도달한다. 시작 시 30분 dialog가 승인된 USB 디버깅 연결이 QR·학생 이름·학습 화면을 캡처·조작할 수 있다고 경고하며, Kiosk는 signature 권한 receiver로 Web에 같은 만료 상태를 전달한다(`kiosk/activity_main.xml:160-180`, `MainActivity.kt:2693-2753`, `kiosk/AndroidManifest.xml:78-86`, `webpoc/AndroidManifest.xml:65-72`). 그러나 PIN·Web credential/setup 진입 시 자동 중지·복원이 있다는 코드나 경고는 없다.
- `RemoteSupportPolicy`는 기본 30분·최대 2시간, 같은 boot에서만 active를 허용하고, 만료·명시 종료·Activity destroy 때 flag를 복원한다(`RemoteSupportPolicy.kt:3-31`, `RemoteSupportStore.kt:23-50`, 각 controller `stop()`). 이는 시간·부팅 경계이지 민감 화면 경계가 아니다.
- 현재 `kiosk/src/test`·`kiosk/src/androidTest`·`webpoc/src/test`·`webpoc/src/androidTest`에는 `FLAG_SECURE` 상태와 PIN/Web setup/relock/Activity lifecycle을 함께 검증하는 시험이 없다. 두 `RemoteSupportPolicyTest`는 duration clamp·expiry·boot count만 검증한다. `Gate5ManifestInstrumentedTest`와 credential/DOM 시험은 receiver 권한 또는 입력 cleanup을 다루지만 window flag 상태를 관찰하지 않는다.
- `SECURITY.md:27-29,48-50,59-62`는 화면 캡처를 기본 차단하되 승인된 원격 점검 중 예외를 허용하고 “관리자 PIN·비밀번호 입력 중에는 사용하지 않는다”고 운영자에게 요구한다. 이는 현재 코드의 화면별 fail-closed 계약이 아니라 사용 절차다. `docs/BUILD_VERIFICATION.md`와 현장 기록은 `FLAG_SECURE` 정상 캡처 또는 원격 점검 종료/비활성 상태를 기록하지만 active remote support + PIN/Web credential 화면 매트릭스는 확인하지 않는다.
- history상 원격 지원과 전역 window controller는 `f8bd710`(2026-07-31)에 도입됐고, `9153f75`(2026-08-01)는 WebView debugging을 active 상태에 연동했다. 이후 `showAuthentication()`·Web setup에 민감 화면 진입 시 `remoteSupportWindowController.stop()` 또는 `FLAG_SECURE` 복원 hook을 추가한 이력은 확인하지 못했다.

#### 반대 근거

- 원격 지원 시작은 관리자 인증 뒤의 관리자 화면 버튼과 확인 dialog를 요구하고, Kiosk/Web receiver는 각각 `signature`/`android.permission.DUMP` 경계로 보호된다. 따라서 임의 일반 앱이 이 상태를 켤 수 있다는 결함으로 확대하지 않는다.
- 기본 만료 30분·최대 2시간·same-boot 조건, 화면 우측 `원격 점검 중` 배지, 만료·명시 종료·Activity destroy 시 복원은 노출 창을 제한한다. `SECURITY.md`의 PIN·비밀번호 중 사용 금지와 현장 기록의 원격 점검 비활성 종료도 정상 운영 경로의 반증이다.
- `FLAG_SECURE` 예외 자체는 제품 보안 정책에 명시된 승인된 원격 점검 기능이다. 이 finding은 예외를 제거하라는 주장이 아니라, 예외가 활성인 동안 민감 화면 진입을 코드가 자동 차단하지 않는 화면-state 경계로 한정한다.
- `LUNA-0021`의 Kiosk/Web SharedPreferences commit·상태 divergence는 별도 저장 실패 finding이며, 정상 remote support 상태에서 민감 화면 flag가 계속 해제되는 현재 finding과 중복시키지 않는다.

#### 추론

- 원격 지원 만료 전에 Kiosk가 관리자 재인증·relock 또는 Web POC가 setup/Gate 3 credential 입력 화면으로 전환되고 승인된 캡처 주체가 화면을 읽으면, PIN·username·password가 캡처 대상이 될 가능성이 있다. Kiosk PIN은 관리자·세션·QR 관리 권한으로, Web credential은 외부 학습 계정 접근으로 이어질 수 있다.
- 원격 지원 시작의 관리자 승인과 명시 경고를 우회하지 않은 정상 운영에서는 발생하지 않으며, 실제 캡처·화면 상태·ADB 주체 조합은 미검증이다. 코드 경로가 민감 화면 진입을 강제 차단하지 않는 점만 확인했으므로 `P3` 후보·중간 신뢰도를 유지한다.

#### 가정과 미검증

- 승인된 ADB/원격 지원 주체가 `FLAG_SECURE`가 해제된 화면을 캡처할 수 있다는 제품 위협 모델을 적용했다.
- 유효한 원격 지원 상태에서 Activity를 재생성하지 않는 Kiosk relock 또는 Web setup 진입이 가능하다는 코드는 확인했지만, 실제 화면 전환·capture semantics는 실행하지 않았다.
- 원격 지원 활성화·PIN·Web 자격정보 입력·ADB 캡처·화면 원문·실제 학생/계정은 사용하지 않았다. `FLAG_SECURE` 해제 상태에서 실제 PIN/credential 문자가 캡처되는지와 제조사별 동작은 확인하지 않았다.
- `SECURITY.md`의 운영자 금지 문구가 모든 현장 절차에서 지켜진다고 가정하지 않았고, 반대로 실제 운영자가 이를 위반한다고 주장하지도 않는다.

#### 안전한 확인 절차

- Kiosk/Web Activity의 초기 flag, controller/store/policy, 관리자 PIN·relock·Web setup lifecycle, receiver manifest 권한, 관련 단위시험·계측시험, `SECURITY.md`·BUILD/현장 기록과 `f8bd710`·`9153f75` history를 읽기 전용으로 대조했다.
- `adb` executable이 없어 A 상태를 확인하지 못했고, PIN·QR·학생 세션·원격 지원 활성화·화면 캡처·ADB 입력·빌드/시험은 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 원격 지원이 활성인 상태에서 관리자 PIN·Web 자격정보·setup/auth panel에 들어가면 캡처 차단을 자동 복원하거나 원격 지원을 즉시 중지하고, 민감 화면을 벗어난 뒤 명시적 재승인을 거쳐 예외를 다시 연다.
- 실제: controller는 만료 시각과 store state만 보고 window flag를 전역으로 해제한다. Kiosk PIN·relock 및 Web setup/credential 화면 진입 경로에는 자동 복원·지원 일시중지·화면별 회귀시험이 없다.

#### 영향 및 재판단 큐

- 보안/개인정보: 승인된 원격 지원 중 Kiosk 관리자 PIN 또는 Web POC username/password가 화면 캡처·원격 제어 대상이 될 가능성. 운영자 경고를 준수하면 줄어들지만 코드가 이를 강제하지 않는다.
- Sol·사용자 재판단 후 PIN/password/auth/setup panel 진입 시 원격 지원을 즉시 끄거나 `FLAG_SECURE`를 복원하고, 민감 화면을 벗어난 뒤 재승인하도록 상태 계약을 정해야 한다. 구현과 실제 캡처 회귀검증은 이번 Goal에서 수행하지 않는다.
- 판정: 직접적인 flag 해제·민감 화면 경로·시험 공백은 현재 코드에서 확인했으나 실제 캡처는 금지·미검증이다. `LUNA-0020` P3 후보·중간 신뢰도를 유지한다.

#### 기존 테스트가 잡지 못한 이유

- 정책 단위시험은 duration clamp·expiry·boot count만 검사한다. Kiosk PIN 초기/실패/relock·Web setup/Gate 3 credential·Activity 재생성·원격 지원 active/expiry의 조합별 `FLAG_SECURE` 상태를 실제 Window에서 관찰하는 시험이 없다.

### LUNA-0021 — 원격 지원 Kiosk/Web 상태 저장 commit 실패가 캡처 차단 상태를 어긋나게 할 수 있음

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견: 2026-08-02 08:20:13; 마지막 확인: 2026-08-02 17:14:18 (Asia/Seoul)
- 영향 모듈·버전: Kiosk/Web `RemoteSupportStore`, Kiosk→Web 원격 지원 상태 broadcast, ADB 원격 지원 스크립트

#### 사실

- Kiosk와 Web POC는 각각 자기 앱 private `SharedPreferences("remote_support")`를 사용한다. 두 `RemoteSupportStore.enable()`은 만료 시각·boot count를 `Editor.commit()`으로 저장한 뒤 반환된 Boolean을 검사하지 않고 계산된 `expiresAt`만 반환한다. `disable()`도 두 key 제거 commit 결과를 버린다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/RemoteSupportStore.kt:18-37`, `webpoc/src/main/java/com/local/matholickiosk/webpoc/RemoteSupportStore.kt:18-37`).
- `activeUntilEpochMillis()`는 각 앱의 로컬 값을 읽어 active 여부를 계산하고, 만료/boot 불일치 시 다시 `disable()`하지만 그 cleanup commit 결과 역시 호출자에게 전달하지 않는다(`RemoteSupportStore.kt:39-50`). 양 앱 사이에 shared transaction, cross-process readback 또는 acknowledgement는 없다.
- Kiosk 관리자 UI의 `setRemoteSupportEnabled()`는 먼저 Kiosk store를 쓰고 `notifyWebRemoteSupport()`를 호출한다. `notifyWebRemoteSupport()`는 explicit component에 broadcast를 보내고 `sendBroadcast()`가 예외를 내지 않았는지만 Boolean 성공으로 반환한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:2712-2753`). Web receiver의 `onReceive()`는 `applyRemoteSupportIntent()`를 호출할 뿐 store commit 결과나 상태를 Kiosk에 회신하지 않는다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/KioskRemoteSupportReceiver.kt:7-16`, `RemoteSupportIntent.kt:10-25`).
- Kiosk UI enable 경로는 Kiosk commit 실패나 Web receiver 내부 commit 실패를 구분하지 못한다. `sendBroadcast()` 예외가 난 경우에만 Kiosk store를 disable해 rollback하고, 예외 없는 dispatch 뒤 Web commit false·receiver 처리 실패·프로세스 종료에는 보상 경로가 없다(`MainActivity.kt:2723-2729`). disable 경로도 Kiosk를 먼저 비활성화한 뒤 Web receiver에 전달하고, Web 저장 결과를 확인하지 않는다.
- 각 `RemoteSupportWindowController`는 자기 앱 store의 listener/expiry runnable만 관찰해 Kiosk `FLAG_SECURE`·badge 또는 Web `FLAG_SECURE`·WebView debugging을 갱신한다(`kiosk/.../RemoteSupportWindowController.kt:24-64`, `webpoc/.../RemoteSupportWindowController.kt:24-64`). 상대 앱의 실제 active state를 재조회하거나 mismatch를 fail-closed로 전환하지 않는다.
- `scripts/remote-tablet.ps1`의 `Set-RemoteSupport`는 Kiosk `AdbRemoteSupportReceiver`와 Web `AdbRemoteSupportReceiver`를 순서대로 broadcast하고 각 명령의 process exit code/출력에 `result=0`이 있는지만 검사한다(`scripts/remote-tablet.ps1:35-63`). 두 receiver 모두 store `commit()` 결과를 반환하지 않으므로 첫 번째 성공 후 두 번째 명령 실패, 또는 receiver가 commit false를 삼킨 경우 partial state를 rollback하지 않는다.
- 같은 스크립트의 `Start`는 두 broadcast 뒤 500ms 후 캡처하고 PNG 크기·signature만 검사하며, 두 앱의 만료 시각·`FLAG_SECURE`·WebView debugging 상태가 같은지 검증하지 않는다. `Stop`도 두 broadcast를 순차 실행하고 첫 대상 이후 다음 대상이 실패하면 앞선 상태를 복구하지 않는다(`scripts/remote-tablet.ps1:95-114`).
- 현재 관련 시험은 Kiosk/Web `RemoteSupportPolicyTest`의 duration clamp·expiry·same-boot 계산, Kiosk/Web manifest 시험의 receiver 권한·admin panel 노출, Kiosk/Web의 정상 `FLAG_SECURE` 상태를 검사한다. SharedPreferences commit false, receiver 처리/저장 acknowledgement, 양 앱 교차 상태 또는 순차 broadcast partial failure를 검증하는 시험은 찾지 못했다(`kiosk/src/androidTest/.../Gate5ManifestInstrumentedTest.kt:79-96`, `webpoc/src/androidTest/.../ManifestInstrumentedTest.kt:42-61`, `kiosk/src/androidTest/.../MainActivityInstrumentedTest.kt:215-246`, `webpoc/src/androidTest/.../RecoveryInstrumentedTest.kt:601-617`).
- `scripts/verify-release-apks.ps1`는 release APK에서 receiver exported/action/permission과 manifest 권한만 검사하고 저장 결과·양 앱 runtime state는 검사하지 않는다(`scripts/verify-release-apks.ps1:116-155`). `SECURITY.md`·`docs/THREAT_MODEL.md`는 same-boot·최대 2시간·배지·승인 receiver·종료 복원을 통제로 기록하지만 cross-app write acknowledgement는 정의하지 않는다.
- history상 store/controller/receiver/script와 현재 Kiosk UI 경계는 `f8bd710`(2026-07-31)에서 함께 도입됐다. 이후 `43133b2`·`80cfeb1`은 gated remote QR/PNG 검증을 보강했지만, 두 앱 commit 결과·acknowledgement·partial rollback을 추가한 후속 변경은 확인하지 못했다.

#### 반대 근거

- receiver는 Kiosk↔Web에 signature permission, ADB receiver에 `android.permission.DUMP`를 요구하고, manifest/release 시험은 이 trust boundary와 관리자 panel 노출을 검사한다. 임의 일반 앱이 broadcast를 호출할 수 있다는 문제로 확대하지 않는다.
- 정상 Kiosk UI 경로에서는 explicit component broadcast 후 양 앱 controller가 local preference listener와 expiry timer로 갱신하며, 정상 ADB 스크립트도 두 receiver를 모두 호출한다. same-boot·duration clamp·명시 종료·만료 복원은 상태가 정상 저장될 때 divergence를 제한한다.
- commit false·저장공간/파일시스템 오류·receiver process 종료·두 번째 broadcast 실패라는 fault 전제가 필요하고, 현재 로그·A 화면에서 실제 divergence가 발생했다는 증거는 없다. 따라서 직접 보안 붕괴가 아니라 실패 시 동기화 검증 공백인 P4로 유지한다.
- `LUNA-0020`은 양 앱 state가 active인 경우 민감 화면에서 `FLAG_SECURE`를 복원하지 않는 화면-state 문제이고, 이 finding은 Kiosk와 Web의 state 자체가 서로 다를 수 있는 저장/IPC 경계다. 두 finding을 중복시키지 않는다.

#### 추론

- enable 중 Kiosk local commit은 런타임에서 성공처럼 보이지만 영속 저장이 실패하거나 Web receiver commit이 실패하면, Kiosk만 `FLAG_SECURE`를 해제하고 Web은 계속 secure일 수 있다. 반대로 Web에 stale active state가 남아 있으면 Kiosk가 비활성화된 뒤에도 Web `FLAG_SECURE`/debugging 예외가 만료 전 유지될 수 있다.
- `scripts/remote-tablet.ps1`에서 첫 receiver 뒤 두 번째 receiver가 실패하면 첫 앱의 remote state와 화면 flag가 남은 채 명령이 중단될 수 있다. 실제 Android `commit()` false 시 in-memory preference와 disk persistence의 정확한 조합·broadcast delivery timing은 실행하지 않았으므로 상태 divergence를 현재 기기에서 재현했다고 확정하지 않는다.
- Kiosk UI의 사용자 메시지·remote support button과 실제 두 앱 window/debugging 상태가 달라질 수 있어, 승인 PC 캡처가 불필요하게 차단되거나 종료 후 한 앱만 캡처 가능 상태로 남는 운영 위험이 있다.

#### 가정과 미검증

- `SharedPreferences.Editor.commit()`이 false를 반환할 수 있는 저장 오류와 receiver/process/broadcast 실패가 운영 환경에서 가능하다는 보수적 가정을 적용했다.
- 실제 commit false·저장공간 오류·프로세스 종료·receiver 미설치/권한 오류·두 번째 ADB broadcast 실패·Kiosk/Web 상태 교차·화면 캡처는 실행하지 않았다.
- `result=0`이 receiver의 내부 저장 성공 acknowledgement가 아니라는 점은 receiver가 결과를 설정하지 않고 script가 출력만 검사한다는 코드 구조에서 판단했으며, 실제 Android `am broadcast` 출력/동기화 semantics는 A에서 확인하지 않았다.
- 현재 A의 SharedPreferences 원문·remote state·WebView debugging·`FLAG_SECURE` 실제 flag는 읽거나 변경하지 않았다.

#### 안전한 확인 절차

- 양 앱 store/controller/policy, Kiosk UI broadcast/rollback, 두 receiver와 manifest permission, ADB 운영 script, 관련 시험·release verifier·SECURITY/Threat Model·history를 읽기 전용으로 대조했다.
- `adb` executable이 없어 A 상태를 확인하지 못했고, 원격 지원 활성화·종료, PIN·QR·학생 세션·설정 쓰기, fault injection·캡처·테스트·빌드는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 양 앱의 persistence 성공과 runtime controller 반영이 확인된 경우에만 remote support를 active/disabled로 표시하고, 한쪽 write·broadcast·receiver 처리 실패 시 양쪽 모두 fail-closed로 `FLAG_SECURE`를 복원하거나 명확히 rollback한다.
- 실제: 각 store는 commit 결과를 버리고, Kiosk UI는 예외 없는 `sendBroadcast()`만 확인하며, ADB script는 두 `result=0` 출력만 확인한다. 양 앱 상태 readback/acknowledgement/partial rollback 경계는 없다.

#### 영향 및 재판단 큐

- 보안/운영: 원격 지원 시작·종료 상태, Kiosk/Web badge, `FLAG_SECURE`, WebView debugging이 일시적으로 불일치할 수 있다. 종료 실패 시 한 앱이 만료 전까지 캡처 예외를 유지하는 방향이 특히 민감하다.
- Sol·사용자 재판단 후 양쪽 store write 결과·receiver acknowledgement·cross-app state readback·실패 시 fail-closed/재시도/partial rollback 정책을 정의하고, enable·disable·재시작·저장 실패·두 번째 대상 실패 회귀시험을 정할지 판단해야 한다. 이번 Goal에서는 구현하지 않는다.
- 판정: 현재 코드의 결과 무시·ack 부재·순차 broadcast partial failure 경계는 확인했으나 fault 주입과 실제 화면 상태는 미검증이다. `LUNA-0021` P4 후보·중간 신뢰도를 유지한다.

#### 기존 테스트가 잡지 못한 이유

- 정책 시험은 시간·boot 계산만, manifest 시험은 권한·component 노출만, 정상 flag 시험은 단일 앱의 secure 기본값만 검증한다. 두 앱 store commit 결과·receiver 처리·Kiosk↔Web 상태 acknowledgement와 ADB script의 partial failure를 함께 검증하는 시험은 없다.

### LUNA-0022 — Lock Task 진입이 `LOCKED`가 아니어도 QR/인증 흐름과 overlay restriction이 불일치할 수 있음

- 심각도: P3
- 상태: 후보
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 08:41:39~17:21:25 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `KioskLockTaskController`, `MainActivity`의 전용기기 재잠금·QR 대기·관리자 인증 경로

#### 사실

- `KioskLockTaskController.enterRestrictedMode()`는 Device Owner·allowlist를 확인한 뒤 `DISALLOW_CREATE_WINDOWS`를 추가하고 `activity.startLockTask()`를 호출한다. 호출이 예외를 내면 restriction을 지우지만, 호출 뒤 `currentMode() == LOCKED`가 false인 경우에는 restriction을 지우지 않은 채 `Result.success(false)`를 반환한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/admin/KioskLockTaskController.kt:58-79`).
- `MainActivity.enterDedicatedMode()`는 반환값의 `isFailure`만 `dedicatedDevicePolicyFailed`에 반영하고, `Result.success(false)`를 보안 실패로 취급하거나 현재 mode를 재확인해 화면 흐름을 중단하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:879-894`).
- `showAuthentication()`, `showInitialStateLoading()/Failure()`, `showScanner()`와 Activity `onStart()`의 scanner/auth 재진입은 `enterDedicatedMode()` 뒤에 화면·카메라 흐름을 계속 진행한다. `showScanner()`는 결과 Boolean에 따라 `ensureCamera()`를 건너뛰지 않는다.
- `configureDedicatedDevice()`도 `configureIfDeviceOwner()`의 `Result`가 성공·true일 때만 `enterDedicatedMode()`를 호출하고, 이후 진입 결과의 `success(false)`는 `dedicatedDevicePolicyFailed`를 바꾸지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:869-877`).
- `SessionPreflightPolicy`는 Device Owner·allowlist·uninstall protection·정책 설정 실패를 검사하지만 현재 `DedicatedDeviceMode` 또는 `enterRestrictedMode()`의 Boolean 결과를 입력으로 받지 않는다. 해당 false 결과와 restriction 잔류 조합을 다루는 controller/계측시험은 검색되지 않았다. 테스트는 계약상 실행하지 않았다.
- `DedicatedDevicePolicy.statusLabel()`은 owner·allowlist·Web 보호가 정상이어도 `mode`가 `LOCKED`가 아니고 관리자 해제도 아니면 `보안 준비 중`을 반환한다. 따라서 mode 불일치가 정책 오류로 승격되지 않는다.

#### 반대 근거와 이력

- `git blame`상 `enterRestrictedMode()`의 해당 구간 전체는 Gate 5 도입 commit `ff8ec786`에 귀속되고, `git log -S`에서 `currentMode() == LOCKED` 반환식은 후속 변경 없이 그대로다. 이는 결함이 현행 정적 코드에 남아 있다는 이력 근거이지 실제 장애 발생 증거는 아니다.
- Device Owner 부재·allowlist 불허·Web 보호 해제는 `status()`와 `SessionPreflightPolicy`의 별도 조건으로 차단되고, `startLockTask()` 예외 경로는 restriction을 정리한다. 관리자 해제 경로의 `exitForAdministrator()`와 `onStart()`의 재진입도 정상 복구 경로다.
- `docs/GATE5_IMPLEMENTATION.md`, `scripts/provision-gate5-device-owner.ps1`, `scripts/verify-gate5-device-owner.ps1`와 최신 field checklist/BUILD 기록은 Device Owner·allowlist·전용 HOME·`LOCKED`를 정상 경로에서 확인한다. provision/verify는 시작 시 `LOCKED`가 아니면 실패하지만, 앱의 예외 없는 false 반환 후 화면 진행은 검증하지 않는다.
- 현재 정책 단위시험은 allowlist·status label·Device Owner 부재를 검사하고, 계측시험 검색에서도 `KioskLockTaskController`, `enterRestrictedMode`, `startLockTask`, `DISALLOW_CREATE_WINDOWS`를 직접 fault 주입하는 사례는 확인되지 않았다.

#### 추론

- Android lifecycle 또는 시스템 상태 경합으로 `startLockTask()`가 예외 없이 반환했지만 즉시 `LOCKED`가 아니고 Device Owner·allowlist·Web 보호 상태는 정상으로 남아 있으면, Kiosk가 `Result.success(false)`를 받은 뒤에도 QR/인증 화면과 카메라를 열어 잠금 경계가 적용되지 않은 상태를 정상 흐름처럼 유지할 수 있다.
- 같은 경로에서 `DISALLOW_CREATE_WINDOWS`가 남으면 실제 Lock Task는 `NONE`인데 overlay 창 제한만 남는 불일치가 생겨 관리자 진입·복구 또는 다음 재시도 상태를 예측하기 어렵다. 관리자 화면의 `exitForAdministrator()`가 restriction을 지우고 Activity `onStart()`가 재시도하는 완화 경로는 있지만, 최초 false 결과 직후의 fail-closed 보장은 없다.
- 실제 Android에서 `startLockTask()`가 항상 동기적으로 `LOCKED`가 되거나 실패 시 예외를 보장하는지는 확인하지 않았다. 정상 A의 `LOCKED` 기록은 이 반대 경로를 반증하지 않는다. 따라서 보안 경계 붕괴가 현장에서 재현됐다고 확정하지 않고 P3 후보로 둔다.

#### 가정과 미검증

- `startLockTask()` 반환 직후 `ActivityManager.lockTaskModeState`가 `NONE` 또는 `PINNED`로 남을 수 있는 lifecycle·시스템 오류 전제를 검토 대상으로 삼았다.
- SM-P610에서 Lock Task false 반환·예외 주입·`DISALLOW_CREATE_WINDOWS` 조회·QR/인증 화면 진행을 실행하지 않았다.
- 현재 A의 `mLockTaskModeState=NONE`은 관리자 화면에서 `showAdmin()`이 의도적으로 잠금을 해제한 상태일 수 있어 이 finding의 재현 증거로 사용하지 않았다.
- Android API/제조사별 `startLockTask()` 반환·mode 전파 순서, restriction이 false 반환 뒤 실제로 남는지, overlay 창 동작과 복구 가능성은 미검증이다.

#### 안전한 확인 절차

- `KioskLockTaskController`, `MainActivity` 호출부, `SessionPreflightPolicy`, `DedicatedDevicePolicy`, Gate 5 운영 문서·스크립트·field checklist·BUILD 기록·도입 history와 관련 테스트 파일을 읽기 전용으로 대조했다.
- Device Owner·allowlist·화면·PIN·QR·카메라·Lock Task 설정·ADB 상태는 변경하지 않았고, fault injection·빌드·테스트도 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: Lock Task 진입이 `LOCKED`가 아니면 결과가 실패로 전파되고, 추가한 overlay restriction은 정리되며, QR·인증·학생 흐름은 잠금이 확인될 때까지 차단되거나 명확한 복구 상태로 전환되어야 한다.
- 실제: 예외가 없는 false 결과에서는 controller가 `Result.success(false)`를 반환하면서 restriction을 유지하고, `MainActivity`의 정책 오류 플래그·preflight·화면/카메라 호출자는 그 Boolean false 또는 `NONE/PINNED`를 검사하지 않는다.

#### 영향 및 재판단 큐

- 보안/운영: 드문 Lock Task 상태 전이 실패가 발생하면 학생 화면·QR 대기·관리자 PIN의 UI 상태와 실제 전용기기 잠금 상태가 어긋날 수 있고, overlay 제한이 잔류해 복구 조작도 불안정해질 수 있다.
- Sol·사용자 재판단 후 `enterRestrictedMode()`의 false 결과를 failure/재시도 상태로 통합하고, false·예외·PINNED·Activity 재생성·restriction cleanup 회귀시험을 정할지 판단해야 한다. 이번 Goal에서는 구현하지 않는다.

#### 기존 테스트가 잡지 못한 이유

- 현재 정책 단위시험은 allowlist/status label/preflight 조건을 검사하지만, `startLockTask()`가 예외 없이 `LOCKED`가 되지 않는 경우의 Boolean 전파, restriction 롤백, 화면·카메라 차단을 검증하지 않는다. Gate 5 운영 확인은 정상 `LOCKED`의 positive path에 한정되며, false·`PINNED`·Activity 재생성·restriction 잔류 fault의 회귀시험은 없다.

### LUNA-0023 — QR 렌더링의 `BitMatrix`·픽셀 임시 배열이 Bitmap 정리와 별도로 zeroize되지 않음

- 심각도: `P3`
- 상태: 후보
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 09:06:42~17:28:31 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `QrImageRenderer`와 단건·batch QR 발급/재발급·인쇄/PDF preview 경로

#### 사실

- `QrImageRenderer.render(payload, sizePixels)`는 `MultiFormatWriter().encode()`로 QR payload의 `BitMatrix`를 만들고, 다시 `IntArray(sizePixels * sizePixels)`에 각 셀을 검정/흰색 픽셀로 복사한 뒤 `Bitmap.setPixels()`를 호출한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/qr/QrImageRenderer.kt:11-33`).
- `pixels`와 `matrix`에는 명시적인 `try/finally`, 배열 덮어쓰기, matrix clear 또는 소유권 전달 경계가 없다. 현재 호출 상수 `QR_SIZE_PIXELS=720`에서는 `pixels` 원소만 약 2,073,600바이트이고, 한 batch PDF/인쇄 페이지는 최대 9개 Bitmap을 보유한다. 함수는 Bitmap을 반환한 직후 두 로컬 표현을 더 이상 참조하지 않지만, ART/GC가 실제로 회수할 때까지 힙에 남는 것을 코드가 보장하지 않는다.
- `MainActivity`의 단건 등록·재발급·직접 인쇄·PDF·지정 PC 전송과 반 batch PDF/인쇄가 renderer를 호출한다(`MainActivity.kt:1360`, `:1416`, `:1949`, `:2059`, `:2167`, `:2246`, `:2353`). `showQrPreview()`·`clearQrPreview()`와 `QrPdfExporter.releaseSensitiveBitmap()`은 반환 Bitmap의 erase/recycle을 담당하고, `QrPdfExporter`, `BatchQrPdfExporter`, 두 PrintDocumentAdapter의 finish/finally 및 Activity `SensitiveTask`가 소유한 Bitmap을 정상·예외·종료 경로에서 정리한다. 그러나 그 cleanup은 renderer 내부 `pixels`·`BitMatrix`를 지우지 않는다.
- `QrTokenCodec.issue()`는 256비트 난수를 `MQR1:` payload String으로 만들고 hash만 저장하는 구조이며, `SECURITY.md`는 QR 원문과 화면 bitmap을 민감한 일회성 자료로 취급한다. 따라서 matrix의 검정/흰색 모듈과 `pixels`도 payload를 재구성할 수 있는 파생 표현이지만, 원문 String 자체를 zeroize할 수 있다는 뜻은 아니다.
- `QrPrintDocumentAdapterInstrumentedTest`의 renderer 호출은 `writesOnePagePdfAndWipesOwnedBitmapOnFinish()` 한 곳이다. 이 시험과 `copiedQrBitmapIsWipedWhenWorkFailsBeforePdfExport()`는 결과 Bitmap/PDF 및 copy 소유권을 검사하지만, renderer 중간 배열의 예외·수명·heap cleanup 계약은 검사하지 않는다. 별도 `QrImageRendererTest` 또는 renderer fault 시험은 검색되지 않았다. 실제 시험은 계약상 실행하지 않았다.

#### 반대 근거와 오탐 검토

- `QrPdfExporter.releaseSensitiveBitmap()`·`consumeSensitiveBitmap()`·`BatchQrPdfExporter`의 `finally`, 두 PrintDocumentAdapter의 `onFinish()`, `MainActivity.onDestroy()`의 queued `SensitiveTask` 폐기는 반환된 Bitmap의 정상·예외·Activity 종료 수명을 넓게 덮는다. 이 finding은 그 cleanup이 없다고 주장하지 않는다.
- `setPixels()` 뒤 `matrix`·`pixels`가 즉시 dead 처리되어 일반 GC에서 빠르게 회수될 가능성은 있다. 다만 현재 소스에는 이를 보안 cleanup 계약으로 명시하거나 heap/exception 경계를 시험하는 근거가 없다. 실제 heap dump 접근이 필요하다는 전제 때문에 P0/P1 노출로 확대하지 않고 P3 후보로 유지한다.
- RC13 `0fc357c`와 RC14 `bfbfaf4`의 과거 검증은 PDF export 전 실패와 대기 작업에서 **복제·반환 Bitmap**을 흰색 덮어쓰기·recycle하는 계약을 추가했다. `git blame`·`git log`상 renderer 본문은 Gate 4 도입 `e093bd0` 이후 변경되지 않아, 과거 positive test를 내부 임시 배열 zeroize의 증거로 재사용하지 않았다.
- `docs/SECURITY.md`의 QR bitmap 삭제 문구, `docs/KNOWN_LIMITATIONS.md`의 외부 인쇄/PDF 경계, RC47/RC68 현장 checklist 16절의 재발급·PDF·직접 인쇄 미체크 상태는 서로 다른 범위다. 오래된 BUILD 기록의 합성 PDF·A 인쇄 PASS도 현재 renderer 내부 배열의 회수를 증명하지 않는다.
- `LUNA-0006`은 발급·CSV·비활성화 경로의 QR hash 배열 소유권이고, `LUNA-0014`는 PC 전송 plaintext·파생 key buffer이므로 QR 이미지 표현의 수명 문제와는 중복되지 않는다.

#### 추론

- `pixels`는 단순한 단색 배열이지만 전체 QR 형상을 담고 있어, Bitmap을 erase/recycle한 뒤에도 해당 임시 배열이나 `BitMatrix`가 heap snapshot·메모리 분석에 남아 있으면 QR payload를 다시 판독할 수 있는 표현이 된다.
- 이는 즉시 인증 우회나 실제 heap 노출이 확인됐다는 뜻은 아니다. 다만 QR 원문을 민감한 one-time 인증 자료로 취급하는 현재 정리 기준에서, 반환 전 임시 이미지 표현의 회수 보장이 없는 것은 P3 후보로 분류한다.

#### 가정과 미검증

- ART/JIT의 로컬 변수 liveness, ZXing `BitMatrix` 내부 저장 방식, GC 시점과 heap dump 접근 가능성은 확인하지 않았다.
- 실제 QR payload·Bitmap·heap snapshot은 생성·기록하지 않았고, `render()` 예외 주입·대량 반복·메모리 압박도 실행하지 않았다.
- 컴파일러가 `setPixels()` 이후 로컬 값을 즉시 dead 처리할 가능성은 있으나, 그 동작이 보안 cleanup 계약으로 명시·시험된 것으로 보지는 않았다.
- `Bitmap.createBitmap()` 또는 `setPixels()`가 부분 성공한 뒤 예외를 낼 때 반환되지 않은 Bitmap·중간 배열이 어떤 native/managed 소유권을 갖는지는 Android API·제조사 구현별로 확인하지 않았다. 현재 모든 내부 호출의 크기는 720으로 고정되어 있으므로 외부 입력이 임의 `sizePixels`를 공급하는 별도 DoS finding은 추가하지 않았다.

#### 안전한 확인 절차

- `QrImageRenderer`, 네 단건/batch 호출부, Bitmap/PDF/print cleanup과 관련 계측시험 소스를 읽기 전용으로 대조했다.
- `QrTokenCodec`, `SECURITY.md`, `PRODUCT_DECISIONS.md`, `KNOWN_LIMITATIONS.md`, RC13/RC14 BUILD 기록, RC47/RC68 현장 checklist와 renderer/cleanup 도입 history도 읽기 전용으로 대조했다.
- 소스·테스트·APK·A 화면·QR 상태·파일·Git 상태는 변경하지 않았고, 빌드·테스트·실제 QR/PDF/인쇄는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: QR payload를 표현하는 renderer 중간 객체와 배열은 Bitmap 반환 전 또는 명확한 owner의 `finally`에서 회수·zeroize되고, 예외 경로도 같은 계약을 지켜야 한다.
- 실제: 반환 Bitmap의 후속 erase/recycle은 확인되지만, `BitMatrix`와 `pixels`에는 별도 zeroize/clear 계약이 없고 중간 표현의 heap 수명은 미정이다.

#### 영향 및 재판단 큐

- 보안/운영: 메모리 분석 권한을 가진 로컬 장애수집·디버깅 경로에서 이미 화면 Bitmap을 정리한 뒤에도 QR payload 표현이 남을 수 있다. 실제 노출 범위는 heap/GC 실험 없이는 확정하지 않는다.
- Sol·사용자 재판단 후 renderer의 임시 배열·matrix ownership을 `try/finally` 또는 재사용 가능한 wipe 경계로 통합하고, 정상·예외·batch 반복·Bitmap cleanup의 회귀시험을 정할지 판단해야 한다. 이번 Goal에서는 구현하지 않는다.

#### 근본 원인 후보와 수정 후 검증

- 원인 후보: 보안 cleanup이 renderer 내부 표현이 아니라 반환 Bitmap의 최종 owner(`QrPdfExporter`·PrintDocumentAdapter·Activity)에만 배치되어, `BitMatrix`·`IntArray`의 생성부터 `Bitmap.setPixels()` 완료/예외까지를 하나의 민감 소유권 경계로 모델링하지 않았다.
- 수정 방향: renderer 내부에서 matrix/pixels와 부분 생성 Bitmap의 예외·정상 수명을 명시하고, 반환 전·실패 시 cleanup 계약을 정한 뒤 정상 render·`setPixels()` 실패·batch 9장·Activity 종료·PDF/인쇄 실패 회귀를 추가 판단한다. 이번 Goal에서는 코드를 수정하지 않는다.
- 수정 후 필요한 검증: 합성 payload로 QR 모듈/Bitmap 출력이 그대로 유지되는지, `finally`가 정상·예외·OOM 인접 경계에서 중복 없이 실행되는지, 반환 Bitmap owner cleanup과 renderer cleanup이 충돌하지 않는지, 720×720 batch peak memory와 실제 Android API 동작을 확인해야 한다.
- 관련: `LUNA-0006`의 QR token hash 배열 ownership, `LUNA-0014`의 PC 전송 plaintext/key cleanup, `LUNA-0036`의 batch mutation ordering과는 자료 표현·소유권·상태 경계가 달라 중복으로 합치지 않았다.

#### 기존 테스트가 잡지 못한 이유

- 현재 인쇄/PDF 계측시험은 결과 Bitmap·PDF·FileProvider와 반환 후 cleanup을 검사하지만, `QrImageRenderer` 내부의 `BitMatrix`·`IntArray`가 반환·예외 뒤 zeroize되는지는 관찰하지 않는다.
### LUNA-0024 — 실행취소 불가 관리자 작업 뒤 이전 반·소속·이름 실행취소가 “방금 작업”으로 남음
- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 09:23:16 / 2026-08-02 17:37:45 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `MainActivity` 관리자 학생·반 변경, CSV 적용, QR 재발급, 자격정보 변경, 학생 비활성화, 반 생성·실행취소 UI
#### 사실
- `offerAdminUndo()`는 `pendingAdminUndo`를 새 action으로 교체하고 `adminUndoGeneration`을 증가시켜 30초 실행취소 버튼을 보인다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:1507-1515`). 현재 호출부는 반 삭제(`:1140-1158`), 반 소속 변경(`:1193-1217`), 학생 이름 변경(`:1472-1499`) 성공 경로뿐이다.
- `clearPendingAdminUndo()`는 실행취소 버튼 자체를 누를 때의 `performPendingAdminUndo()`와 30초 만료 callback에서 호출된다(`MainActivity.kt:1517-1524`, `:1526-1534`). `createClass()`·`reissueQr()`·`applyStudentCsv()`·`updateStudentCredentials()`·`deactivateStudent()`의 성공 경로에는 이전 pending action을 무효화하는 호출이 없다(`MainActivity.kt:1093-1115`, `:1408-1435`, `:1849-1880`, `:1633-1688`, `:1694-1717`).
- `registerStudent()` 성공 경로도 `showQrPreview()`·`refreshAdminData()`만 수행하고 `clearPendingAdminUndo()`를 호출하지 않는다(`MainActivity.kt:1335-1405`). `updateStudentManagementControls()`는 `studentMutationGate`를 기준으로 학생 spinner·등록·CSV·QR·프로필·자격정보·비활성화·pending-card 버튼만 갱신하며 `undoAdminButton`은 대상 목록에 없다(`MainActivity.kt:1733-1753`). 따라서 학생 mutation이 진행 중이어도 만료 전 undo 버튼은 별도 gate 없이 남아 있다.
- `performPendingAdminUndo()`는 버튼 문구처럼 `방금 관리자 작업을 되돌리는 중`을 표시한 뒤 저장된 action을 다시 실행한다. 저장된 action이 `RestoreMemberships`이면 이전 학생 ID 집합을, `RestoreStudentName`이면 이전 이름을 다시 저장한다(`MainActivity.kt:1526-1578`). 실행취소 상태는 `studentMutationGate`의 학생 변경 완료 여부나 CSV 적용·반 생성·QR 재발급의 최신 작업 세대와 연결되어 있지 않다.
- `PendingAdminUndo.RestoreMemberships`·`RestoreStudentName`·`RestoreClass`에는 mutation revision, action timestamp, 현재 값에 대한 expected snapshot이 없고 `adminUndoGeneration`은 만료 callback이 현재 슬롯인지 확인하는 세대일 뿐이다(`MainActivity.kt:1526-1578`, `:3960-3970`). `StudentRepository.updateStudentProfile()`은 expected 현재 이름/revision 없이 이름을 덮어쓰고 `needsPrint`를 다시 세우며, `replaceClassMemberships()`도 expected 이전 membership 집합 없이 전달된 ID 집합을 transaction으로 적용한다(`StudentRepository.kt:490-531`).
- `updateStudentName()`은 `offerAdminUndo()`를 호출한 뒤 `refreshAdminData(... completeStudentMutationAfterLoad = true)`를 제출하므로 refresh callback 전까지 `studentMutationGate`가 active인 짧은 구간에도 undo가 표시될 수 있다. `updateStudentManagementControls()`가 undo 버튼을 갱신 대상에서 제외하는 사실과 결합되지만, 실제 클릭 timing은 실행하지 않았다(`MainActivity.kt:1472-1499`, `:1733-1753`).
- `ioExecutor`는 `Executors.newSingleThreadExecutor()`이고 반·학생·CSV 적용·실행취소 작업이 같은 executor에 제출된다(`MainActivity.kt:161-162`, `:1093-1101`, `:1408-1411`, `:1472-1475`, `:1526-1531`, `:1849-1853`). 단일 worker가 데이터 race를 없애더라도, 실행취소 클릭 시점에 기존 action을 무효화하거나 queued task의 operation generation을 비교하는 경계는 없다. 정확한 UI 클릭·callback 순서는 실행하지 않았다.
- `showAdmin()`·`showAuthentication()`·`showScanner()`와 `onStop()`은 화면/카메라/PIN/QR preview를 정리하지만 `clearPendingAdminUndo()`를 호출하지 않는다(`MainActivity.kt:661-683`, `:764-782`, `:3031-3050`, `:3823-3835`). 따라서 관리자 패널을 나갔다가 수업 관리자 인증으로 다시 들어오거나 Activity가 stop/start되는 동안에도 동일 Activity의 pending action lifetime은 별도로 유지될 수 있다.
- `AdminUiAsyncState`의 `ClassRosterSelectionState`·`RefreshableSelectionState`는 새로고침 결과가 최신 반 선택을 덮지 않도록 세대·revision을 보호하지만, `pendingAdminUndo`의 대상이 이후 변경으로 더 이상 최신 action이 아닌지 판정하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/domain/AdminUiAsyncState.kt`).
- 운영 시나리오는 반 소속·이름·반 삭제 실행취소를 각각 확인한 뒤 31초를 기다려 마지막 실행취소를 만료시키고, 그 다음 일회용 학생의 비밀번호 변경·비활성화를 확인한다(`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md:68-78`). 교차 작업 순서에서 이전 실행취소를 무효화하는 계약·계측시험은 확인하지 못했다.
#### 추론
- 이름 변경 후 30초 안에 자격정보 변경, CSV 적용, QR 재발급, 학생 비활성화 또는 반 생성이 성공해도 기존 이름 실행취소가 화면에 남는다. 사용자가 `방금 작업 실행취소`를 누르면 마지막 작업이 아닌 이전 이름을 되돌릴 수 있다.
- 반 소속 변경 뒤 CSV가 학생 소속을 교체한 경우에도 이전 `RestoreMemberships` action이 남아 있으면, 같은 반의 최신 CSV 소속을 이전 명단으로 다시 덮을 수 있다. 실제 데이터 변경은 수행하지 않았고, 이 영향은 버튼을 눌러야 발생하는 상태 경계다.
- 학생 mutation이 진행 중인 동안에도 undo 버튼이 독립적으로 활성 상태일 수 있어, 사용자가 QR 재발급·이름·자격정보·비활성화 작업을 제출한 전후 어느 시점에 클릭하느냐에 따라 같은 단일 worker에서 새 작업과 stale undo가 순서대로 적용될 수 있다. 이는 동시 실행보다 cross-operation ordering 문제이며, 실제 순서는 미검증이다.
- 학생 등록 성공 뒤에도 기존 이름·소속·반 삭제 action이 남을 수 있고, 그 뒤 화면에 표시되는 새 학생이나 선택 상태가 바뀌어도 버튼 문구는 이전 action을 가리킬 수 있다. 이는 등록 자체를 undo 대상으로 만든다는 뜻이 아니라, 등록이 기존 pending action을 invalidation하지 않는다는 뜻이다.
- QR 재발급은 `StudentRepository.reissueQr()`가 활성 session을 검사하지 않고, 비활성화도 활성 session에서 저장소 시험이 허용한다. 따라서 이전 membership/name action이 남은 상태에서 QR·활성 상태가 후속 변경된 뒤 stale undo가 다시 학생/반 metadata를 바꿀 수 있다. 활성 session 중 `RestoreMemberships` 자체가 eligibility를 바꾸는 별도 범위는 `LUNA-0025`로 분리한다.
- 30초 만료와 명시적 사용자 클릭이 필요하고 자격정보 자체를 노출하거나 삭제하지 않으므로 P4 후보로 분류한다. 다만 `방금 작업`이라는 안내와 제품 시나리오의 실행취소 불가 작업 의미가 어긋난다.
#### 근본 원인 후보

- 실행취소 lifecycle이 공통 관리자 mutation coordinator가 아니라 세 개의 reversible 성공 callback에 분산되어 있고, `clearPendingAdminUndo()`는 클릭·시간 만료에만 연결되어 있다. `adminUndoGeneration`을 operation revision처럼 사용하지 않으며 action에 post-state/expected-state를 저장하지 않는 구조가 stale action을 허용하는 직접 원인 후보다.
- `git log -S 'offerAdminUndo'`와 `git log -S 'clearPendingAdminUndo'`에서 관련 생성·정리 경계는 `0193d89`의 도입 이력에 집중되고, 이후 `MainActivity`의 undo invalidation 후속 수정은 확인하지 못했다.
#### 반대 근거·오탐 검토

- 새 reversible action이 성공하면 `offerAdminUndo()`가 기존 action을 교체하고, generation 비교는 오래된 만료 callback이 새 action을 지우지 않도록 정상적으로 보호한다. 30초 window와 명시적 클릭이 있어 자동 stale rollback은 아니다.
- 제품 결정은 QR 재발급·로그인정보 변경·학생 비활성화 자체에는 실행취소를 제공하지 않는다고 명시하고 있으며(`docs/PRODUCT_DECISIONS.md:101-102`), 현장/BUILD 기록의 각 isolated add/remove/name/delete undo와 30초 만료, credential/deactivation 무실행취소는 정상 동작의 반대 근거다. 다만 이 기록은 “실행취소 불가 작업이 직전 pending action을 지운다”는 교차 계약을 증명하지 않는다.
- `restoreClass()`에는 활성 session 전체 차단·동일 이름 active class 중복 차단·모든 학생 active 확인이 있고, 단일 `ioExecutor`는 실제 DB write를 직렬화한다(`StudentRepository.kt:549-583`). 이는 일부 잘못된 복원의 성공 가능성을 제한하지만, `updateStudentProfile()`·`replaceClassMemberships()`의 expected-state 검사는 대체하지 않는다. 활성 session 중 membership guard는 `LUNA-0025` 범위다.
- 제품 의도가 “마지막 성공한 reversible 작업”의 30초 window를 후속 작업 뒤에도 유지하는 것이라면 일부 관찰은 의도된 동작일 수 있다. 그러나 `방금 관리자 작업` 문구와 실행취소 제외 작업 목록을 기준으로 후속 mutation invalidation이 더 자연스럽다는 해석을 유지하며, 제품 결정 없이는 `확정`으로 올리지 않았다.
#### 가정과 미검증
- 제품 의도가 마지막 성공한 실행취소 가능 작업을 계속 제공하는 것인지, 모든 후속 관리자 변경이 이전 실행취소를 무효화해야 하는지는 별도 결정이 필요하다. 현재 버튼 문구와 시나리오는 후자에 가까운 것으로 해석했다.
- 실제 A에서 등록·이름/소속 변경→CSV·자격정보·QR·비활성화·반 생성의 조합, 학생 mutation 중 undo 클릭, 관리자 화면 재진입·재인증·수업 전환, 버튼 표시·선택 상태는 실행하지 않았다.
- 실제 학생·자격정보·QR·CSV·DB·Activity 상태는 사용하지 않았고, 빌드·테스트도 수행하지 않았다.
#### 안전한 확인 절차
- `MainActivity`의 모든 `offerAdminUndo()`·`clearPendingAdminUndo()` 호출부, 학생 mutation gate, CSV 적용/refresh 경계와 `AdminUiAsyncState` 세대 보호를 읽기 전용으로 대조했다.
- `ioExecutor`의 단일 worker 제출 경계, `updateStudentManagementControls()`의 undo 버튼 제외, 관리자 패널 전환/`onStop()` lifecycle과 `StudentRepository`의 QR 재발급·비활성화 active-session guard도 읽기 전용으로 대조했다.
- `RC47_RC68_MINIMAL_FIELD_SCENARIO.md`, `BUILD_VERIFICATION.md`의 실행취소 성공 기록과 관련 계측시험 검색 결과를 비교했다. 소스·데이터·기기 상태는 변경하지 않았다.
#### 기대 결과와 실제 결과
- 기대: 실행취소 불가 관리자 작업이 시작되거나 성공하면 이전 action을 무효화하고 버튼을 숨기거나, 적어도 현재 action 세대·대상 snapshot과 일치할 때만 실행취소를 허용한다.
- 실제: 실행취소 action은 세 가지 성공 경로에서만 교체되고, 등록을 포함한 다른 관리자 변경의 성공 경로에는 기존 action을 지우는 공통 세대 invalidation이 없다. undo 버튼은 학생 gate와 독립되어 있고 화면 전환도 pending action을 제거하지 않으므로, 만료 전에는 이전 action이 `방금 작업`으로 남거나 후속 성공 작업 뒤에도 적용될 수 있다.
#### 영향 및 재판단 큐
- 데이터/운영: 최신 반 소속 또는 학생 이름이 명시적 재확인 없이 이전 action으로 되돌아갈 수 있으며, CSV 적용 직후에는 사용자가 원인을 최신 작업으로 오인할 수 있다.
- 수업/QR: active-session membership undo의 eligibility 영향은 `LUNA-0025`로 분리하되, QR 재발급·비활성화·이름 변경 후 stale action이 학생 metadata/카드 출력 필요 상태를 다시 바꾸는 cross-operation 영향은 이 후보의 범위로 유지한다.
- Sol·사용자 재판단 후 모든 관리자 mutation 시작/성공 시 pending undo를 지울지, action에 mutation generation·대상 snapshot을 묶어 최신 상태와 일치할 때만 허용할지 정하고, reversible→non-reversible·CSV→undo·연속 반 소속 변경 회귀시험을 정해야 한다. 이번 Goal에서는 구현하지 않는다.
#### 기존 테스트가 잡지 못한 이유
- 현재 현장 시나리오·`BUILD_VERIFICATION.md` 기록은 반 소속·이름·반 삭제 실행취소와 30초 만료, 별도의 비밀번호 변경·비활성화 무실행취소를 확인하지만, 두 종류를 30초 안에 교차하는 순서를 검사하지 않는다. 관련 `MainActivity` 계측시험에서 pending undo의 교차 작업 무효화 계약은 확인하지 못했다.
- `AdminUiAsyncStateTest`는 roster/학생 selection revision과 generic `SingleFlightGate`만 시험하고 pending undo를 다루지 않으며, `MainActivityInstrumentedTest`·`RepositoryInstrumentedTest` 검색에서도 undo 버튼의 enabled matrix, student mutation 중 클릭, 화면 재진입·교차 action 순서 시험은 확인하지 못했다.
#### 필요한 추가 회귀시험

- reversible 작업 성공 뒤 credential/QR/deactivation/CSV/class creation/registration 성공을 각각 수행하고, 이전 action이 숨겨지고 실행되지 않는지 확인한다.
- reversible→reversible 연속 변경에서 두 번째 action만 undo되는지, name/membership refresh callback 대기 중 undo 버튼이 비활성인지, CSV transaction과 undo의 제출 순서가 최신 상태를 보존하는지 확인한다.
- 관리자 화면 이탈·재진입, `onStop()`/`onStart()`, 수업 전환, Activity recreation 뒤 pending action의 보존·폐기 정책을 명시하고 시험한다. active-session membership eligibility는 `LUNA-0025` 시험으로 분리한다.
#### 중복·범위 경계

- active-session `RestoreMemberships`가 QR·수동 선택 eligibility를 바꾸는 저장소 guard 공백은 `LUNA-0025`로 분리한다. batch QR와 student mutation의 공통 gate 부재는 `LUNA-0036`이며, 이 항목은 그 작업 자체가 아니라 이전 undo action의 lifetime/invalidation만 다룬다.
### LUNA-0025 — 활성 수업 중 반 소속 실행취소가 관리자 변경 차단을 우회해 QR/수동 선택 eligibility를 바꿀 수 있음
- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 09:38:19 / 2026-08-02 21:03:08 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `MainActivity` 세션 관리자/실행취소 UI, `StudentRepository.replaceClassMemberships()`, StudentDao 활성 세션 QR·수동 선택 eligibility

#### 사실

- 활성 세션에서 `updateClassRosterUi()`는 `manageClassMembersButton.isEnabled = classReady && currentSession?.sessionId == null`로 반 학생 구성 버튼을 비활성화하고, `updateSessionAdminControls()`도 `classSpinner.isEnabled = !active && !webRecoveryGate.isActive`로 반 선택을 잠근다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:1254-1290`, `:2870-2888`).
- QR 대기 화면의 세션 관리자 인증은 `showAuthenticatedSessionActions()`에서 `관리자 화면 열기`를 제공하고 `showAdmin()`을 호출한다(`MainActivity.kt:3588-3638`, `:3693-3710`). `showAdmin()`·`showScanner()`·`completeSessionStart()`에는 `pendingAdminUndo`를 지우는 호출이 없고, `undoAdminButton`은 action 등록·만료 또는 실행취소 수행 때만 숨겨진다(`MainActivity.kt:764-781`, `:1526-1534`, `:2824-2845`, `:3031-3058`).
- `startOrEndSession()`은 `PendingRecoveryAction.StartSession(classId, temporaryStudentIds)`만 캡처해 Web recovery 뒤 `completeSessionStart()`로 넘기며 pending admin undo 세대나 class membership snapshot을 함께 캡처하지 않는다(`MainActivity.kt:2490-2581`). 성공 callback은 `currentSession`을 설정하고 `showScanner()`로 이동할 뿐 기존 undo를 폐기하지 않는다(`MainActivity.kt:2824-2845`).
- `showAuthenticatedSessionActions()`의 PIN 성공은 동일 Activity에서 `showAdmin()`으로 바로 이동한다. `updateSessionAdminControls()`는 active session에서 `classSpinner`와 일반 반 구성 UI를 잠그고 `updateClassRosterUi()`를 호출하지만 `undoAdminButton` visibility/enabled를 건드리지 않는다(`MainActivity.kt:2870-2888`, `:3693-3718`). 소스에서 undo button 상태를 쓰는 곳은 세 `offerAdminUndo()` 호출, 만료/클릭의 `clearPendingAdminUndo()`뿐이다(`MainActivity.kt:1150`, `:1208`, `:1486`, `:1507-1528`).
- `completeSessionStart()`의 성공 callback은 `currentSession = it`와 `pendingTemporaryStudentIds = emptySet()` 뒤 곧바로 `showScanner()`를 호출한다(`MainActivity.kt:2824-2845`). 이 경계에서 `updateSessionAdminControls()`·`updateClassRosterUi()`를 동기 호출하지 않으며, `showScanner()`는 admin panel을 숨길 뿐 기존 admin 버튼의 enabled 상태를 정리하지 않는다(`MainActivity.kt:3031-3058`).
- 세션 PIN 성공 뒤 `showAdmin()`은 `adminPanel.visibility = View.VISIBLE`과 `refreshAdminData()` 제출을 먼저 수행한다(`MainActivity.kt:764-781`, `:3626-3638`, `:3693-3710`). snapshot callback이 도착해야 `applyAdminDataSnapshot()`이 `currentSession`을 읽고 `updateSessionAdminControls()`·`updateClassRosterUi()`를 호출한다(`MainActivity.kt:913-1017`). 그 사이 기존에 enabled였던 `manageClassMembersButton`·class spinner 상태가 fail-closed로 즉시 초기화되지 않는다.
- `showClassMembershipDialog()`는 선택 반·학생 목록만 확인하고 현재 session 여부를 검사하지 않으며, `replaceClassMemberships()` handler도 `studentMutationGate`·`webRecoveryGate`·active session을 검사하지 않는다(`MainActivity.kt:1165-1190`, `:1193-1221`). 따라서 pending undo뿐 아니라 세션 관리자 재진입 직후 refresh 전의 stale membership 버튼도 같은 repository guard 공백으로 연결된다.
- `performPendingAdminUndo()`는 저장된 action이 `RestoreMemberships`일 때 현재 세션 여부나 `webRecoveryGate`를 확인하지 않고 `studentRepository.replaceClassMemberships(action.classId, action.studentIds)`를 실행한다(`MainActivity.kt:1526-1544`).
- `StudentRepository.replaceClassMemberships()`는 활성 반과 활성 학생 ID만 확인한 뒤 transaction 안에서 해당 반의 `class_memberships`를 지우고 전달된 집합을 삽입하며 `sessionDao().get()`의 활성 세션을 검사하지 않는다(`StudentRepository.kt:513-531`). 반면 `deleteClass()`·`restoreClass()`·`reissueClassQrBatch()`에는 활성 세션 거부 조건이 있다(`StudentRepository.kt:185-187`, `:537-556`).
- `replaceClassMemberships()`는 `action.classId`가 현재 `session.classId`와 같은지 확인하지도 않는다. 따라서 pending action의 반과 현재 수업 반이 다르면 현재 QR/수동 선택 query는 직접 바뀌지 않을 수 있지만, 활성 다른 반의 membership은 session 중에도 변경된다. 현재 session과 같은 반인 경우에는 아래 live join에 직접 영향을 준다.
- `StudentRepository.startSession()` transaction은 active class·active student·기존 session을 확인하고 `temporaryStudentIds`만 `session_students`에 추가한다(`StudentRepository.kt:648-689`). 현재 반의 `class_memberships`를 session별 immutable snapshot으로 저장하는 단계는 없다.
- 활성 세션의 QR·수동 선택 eligibility query는 현재 반의 `class_memberships` 또는 이번 세션의 `session_students`가 있고 학생이 활성인 경우를 사용한다(`Daos.kt:26-66`, `StudentRepository.kt:729-810`). 따라서 임시 보강 학생은 반 소속 변경의 영향을 덜 받지만, 세션에 임시 등록되지 않은 학생의 현재 반 소속 eligibility는 즉시 달라질 수 있다.
- `validateForActiveSession()`은 query가 null이면 같은 hash의 active student를 별도로 찾아 `OUTSIDE_CURRENT_CLASS` 또는 `UNKNOWN_OR_REVOKED`를 audit하고, `validateManualStudentForActiveSession()`·`listEligibleStudentsForActiveSession()`도 같은 `findEligibleById`·`listEligibleForSession` live query를 사용한다(`StudentRepository.kt:729-812`). 따라서 membership restore 뒤의 영향은 목록 표시뿐 아니라 QR 승인/수동 선택 검증 결과에 직접 연결된다.
- 기존 repository 계측시험은 세션 시작 전에 `replaceClassMemberships()`를 호출하고 시작 뒤 eligibility·보강 학생을 확인하지만, 활성 세션 시작 뒤 반 소속을 바꾸거나 pending undo를 실행하는 순서는 찾지 못했다. 현장 시나리오도 반 소속 실행취소를 수업 시작 전에 수행한 뒤 13단계에서 T1을 시작한다(`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md:67-82`).
- `RepositoryInstrumentedTest.manualStudentSelectionAcceptsOnlyCurrentClassAndTemporaryStudents()`는 membership을 설정한 뒤 session을 시작하고 정상 `[member, temporary]` 집합을 확인하지만 session 시작 후 membership mutation은 하지 않는다(`kiosk/src/androidTest/.../RepositoryInstrumentedTest.kt:428-457`). `MainActivityInstrumentedTest.scannerHidesHeaderAndUsesAccessibleIconControls()`도 active session에서 scanner UI와 관리자 아이콘을 확인할 뿐, PIN 재진입·undo button·membership restore 순서는 다루지 않는다(`.../MainActivityInstrumentedTest.kt:108-190`).
- `git log -S 'offerAdminUndo'`는 `0193d89` 도입 commit 하나로, `git log -S 'replaceClassMemberships(classId'`는 `2e4a14d`의 repository 도입 이력으로 확인되며, 현재 active-session guard 또는 pending undo invalidation을 추가한 후속 commit은 확인하지 못했다. `git blame`에서도 undo 실행 경계는 `0193d89`, membership method는 `2e4a14d`에 남아 있다.

#### 추론

- 반 소속을 `[A] → [B]`로 바꾼 뒤 `[A]`를 복원하는 pending `RestoreMemberships` action이 남아 있는 상태에서 수업을 시작하고, 세션 관리자 PIN으로 관리자 화면에 재진입해 실행취소를 누르면 활성 수업 중에도 `class_memberships`가 `[A]`로 되돌아갈 수 있다.
- 그 뒤 같은 세션에서 QR 스캔 또는 수동 학생 선택의 eligibility query가 다시 실행되면, 현재 반 소속에만 의존하던 학생 `[B]`가 선택 대상에서 빠지거나 이전 소속 학생 `[A]`가 대상에 들어올 수 있다. 이미 `session_students`에 임시 보강된 학생은 query의 OR 조건으로 남을 수 있다.
- 이 영향은 pending action의 `classId`가 현재 session의 `classId`와 일치할 때 직접적인 eligibility 변화가 된다. 다른 반 action은 현재 session query의 `:classId` join에는 반영되지 않지만, 동일 Activity의 관리자 DB 상태를 수업 중 변경하는 별도 invariant 위반으로 남는다.
- 추가로 수업 시작 직후 세션 관리자 PIN으로 재진입해 refresh callback보다 먼저 반 구성 버튼을 누르는 순서에서는, 이전 enabled UI가 `showClassMembershipDialog()`를 열고 현재 session의 membership을 직접 교체할 수 있다. 이 경로는 pending undo lifetime과 독립적인 pre-refresh UI race이며, 단일 `ioExecutor`는 제출 순서를 보장할 뿐 handler의 active-session precondition을 추가하지 않는다.
- 이는 활성 수업 중 반 구성 UI 차단과 repository mutation guard가 일관되지 않은 상태 전이 후보이며, 실제 세션·학생·QR 데이터 변경을 실행하지 않았으므로 P3 후보로 유지한다.

#### 근본 원인 후보

- 활성 세션 보호가 새 반 구성 dialog·class spinner에만 적용되고, 이미 생성된 `PendingAdminUndo.RestoreMemberships`를 별도 관리자 mutation 경로로 취급하지 않는다. undo action에는 session generation·class membership snapshot revision이 없고 `replaceClassMemberships()`도 caller가 active-session invariant를 지킨다는 전제에 의존한다.
- 세션 row는 현재 반의 membership을 immutable session snapshot으로 보존하지 않고 `session_students`에 임시 보강 학생만 추가한다. 그 결과 현재 반 membership을 읽는 QR·수동 선택 query가 세션 중에도 변경 가능한 `class_memberships`에 결합된다.

#### 반대 근거·오탐 검토

- 정상 UI에서는 `manageClassMembersButton`과 `classSpinner`가 active session에서 비활성화되어 새 membership 변경을 만들 수 없고, `deleteClass()`·`reissueClassQrBatch()`·`restoreClass()`에는 active-session 거부 또는 복원 guard가 있다. 따라서 이 후보는 일반 반 구성 클릭의 문제라기보다 세션 시작 전에 남겨 둔 pending undo의 우회 경로다.
- 단일 `ioExecutor`가 start/undo DB 작업을 직렬화하고, `startSession()`은 active class·active student·중복 session·최소 학생 수를 transaction으로 확인한다. 이는 race나 부분 transaction을 증명하지 않으며, 이미 시작된 session 뒤 별도 queue 작업이 membership을 바꾸는 가능성을 제거하지 않는다.
- `session_students`의 OR 조건은 보강 학생을 membership restore 뒤에도 eligible로 유지할 수 있고, query는 `QR_READY`·active student를 추가로 요구한다. 따라서 모든 학생이 항상 사라지거나 추가된다고 과장하지 않고, 현재 반 membership에만 의존하는 학생과 `action.classId == session.classId`인 경우로 영향 범위를 제한한다.
- 제품 결정은 실행취소를 30초·명시적 클릭으로 제한하고 QR 재발급·자격정보 변경·비활성화에는 적용하지 않는다고 했지만, active session 중 기존 membership undo의 폐기·거부를 명시하지 않았다. 현장/BUILD의 정상 membership undo는 수업 시작 전에 수행되므로 이 후보를 반증하지 않는다.

#### 가정과 미검증

- 제품 의도가 수업 시작 후 반 소속을 고정하고 수업 중에는 `session_students` 보강만 허용한다는 해석은 현재 UI 차단과 문서 흐름에 근거하지만, 실행취소의 수업 중 허용 여부를 직접 명시한 제품 결정은 확인하지 못했다.
- 정상 UI에서 active session 중 새 membership action을 만드는 경로는 `manageClassMembersButton`이 잠겨 있으므로, 재현에는 수업 시작 전 생성된 pending `RestoreMemberships`가 만료 전 유지되고 관리자 재진입까지 이어진다는 전제가 필요하다. `session_students`의 OR 조건 때문에 temporary 학생은 membership 제거 뒤에도 남는다는 반대 효과를 함께 고려했다.
- 세션 관리자 화면에서 pending undo 버튼이 실제로 유지되는 시점, PIN 재진입·관리자 화면 전환·실행취소 클릭의 정확한 Activity timing, 실제 eligibility 변화는 실행하지 않았다.
- 수업 시작 성공 뒤 `showScanner()`에서 관리자 재진입하고 `refreshAdminData()` callback 전에 반 구성 버튼을 누르는 실제 timing과 stale enabled 상태의 지속 시간도 확인하지 않았다.
- 테스트용 학생·반·세션·QR·DB와 A 화면 상태는 사용하지 않았고, build·lint·test도 수행하지 않았다.

#### 안전한 확인 절차

- `MainActivity`의 활성 세션 roster/class 선택 disable, 세션 관리자 재진입, pending undo 수명과 `performPendingAdminUndo()` 호출부를 읽기 전용으로 대조했다.
- `StudentRepository.replaceClassMemberships()` 및 활성 세션 QR·수동 선택 query와 repository 계측시험·현장 시나리오의 호출 순서를 비교했다. 소스·테스트·문서·기기·학생 데이터는 변경하지 않았다.
- `StudentRepository.startSession()`의 temporary-only session row 생성과 current class membership live join, `replaceClassMemberships()`의 class/session ID 관계 검사를 추가로 대조했다. 다른 반 action이 현재 session eligibility에 미치는 영향은 직접 query key로 제한했다.
- `completeSessionStart()`·`showScanner()`·세션 PIN `showAdmin()`의 동기 control 갱신과 async `refreshAdminData()` callback 순서를 대조하고, `showClassMembershipDialog()`의 session precondition 부재를 별도로 확인했다.

#### 기대 결과와 실제 결과

- 기대: 수업 시작 시 pending membership undo를 폐기하거나, 활성 세션에서 버튼을 숨기고 repository도 membership restore를 거부하며, 현재 세션의 QR·수동 선택 eligibility가 세션 중간 mutation으로 바뀌지 않아야 한다.
- 실제: UI의 새 반 구성·반 선택은 잠기지만 수업 시작 전 남은 `RestoreMemberships`는 `showScanner()`·세션 관리자 재진입 뒤에도 남고, repository method에는 active-session 검사가 없다. eligibility query는 변경된 `class_memberships`를 즉시 읽는다.
- 실제: UI는 active session에서 일반 membership 조작을 막지만 undo action만 별도 gate 없이 남는다. `startSession()`이 class membership을 snapshot하지 않으므로 현재 session 반과 action 반이 같을 때 QR·수동 선택 eligibility는 이후 membership transaction 결과를 읽는다. 다른 반 action은 현재 eligibility에는 직접 반영되지 않는다는 제한을 확인했다.
- 실제: 세션 시작 callback과 관리자 재진입은 control 상태를 즉시 fail-closed로 초기화하지 않고 async snapshot에 의존한다. 따라서 새 membership dialog 자체도 refresh 전에는 stale enabled 상태와 repository 무검사 경계를 공유할 수 있다는 정적 가능성을 확인했다.

#### 영향 및 재판단 큐

- 수업 진행: 현재 수업에서 허용된 학생이 QR·수동 선택 대상에서 빠져 로그인하지 못하거나, 이전 반 소속 학생이 대상에 들어올 수 있다. 임시 보강 학생은 별도 `session_students` 조건으로 남을 수 있다.
- 범위 제한: action 반과 현재 session 반이 다르면 현재 session의 `classId` join이 달라지지 않아 즉시 QR/수동 선택 변화는 추론하지 않는다. 그래도 active session 중 unrelated class membership을 바꾸는 저장소 invariant 공백은 유지한다.
- Sol·사용자 재판단 후 활성 세션 동안 모든 반 membership mutation·undo를 거부할지, pending undo를 세션 시작 시 폐기할지, 세션 자격을 immutable snapshot으로 고정할지 정하고 active-session undo·QR·수동 선택 회귀시험을 추가할지 결정해야 한다. 이번 Goal에서는 구현하지 않는다.

#### 기존 테스트가 잡지 못한 이유

- repository 계측시험은 세션 시작 전 membership 변경과 세션 시작 후 eligibility 조회를 분리해 검사하고, 현장 시나리오도 반 소속 실행취소를 수업 시작 전에 수행한다. 활성 세션 시작 후 `RestoreMemberships` 실행과 그 직후 QR·수동 선택 eligibility 변화를 연속 검증하는 시험은 확인하지 못했다.
- `manualStudentSelectionAcceptsOnlyCurrentClassAndTemporaryStudents()`는 session 시작 전 membership과 시작 시 temporary ID를 설정한 뒤 정상 eligibility 집합을 검사하고, `reissueRevokesOldQrAndTemporaryStudentIsSessionOnly()`는 active session 중 단건 QR 재발급을 검사하지만 active membership undo/교체는 다루지 않는다. 이 차이로 기존 정상 시험을 active-session mutation 증거로 확대하지 않았다.
#### 필요한 추가 회귀시험

- `[A] → [B]` membership 변경 직후 수업을 시작하고, QR_READY에서 세션 관리자 PIN으로 재진입해 pending undo를 누르는 순서를 계측한다. 기대 결과는 버튼 폐기/비활성 또는 repository 거부이며, DB membership·QR 승인·수동 선택 eligible 집합이 그대로인지 확인한다.
- 같은 반과 다른 반을 각각 pending action 대상으로 두고 active session 중 undo했을 때, 같은 반만 eligibility가 변할 수 있고 다른 반은 현재 session query에 직접 영향을 주지 않는 범위를 확인한다.
- temporary student가 포함된 session에서도 membership-only 학생과 `session_students` 학생을 분리해 QR·수동 선택 결과와 audit reason을 확인한다. session 시작 시 pending undo 폐기, session 종료·Activity 재진입 뒤 action 정책도 명시한다.
- 세션 시작 후 관리자 재진입 직후 refresh callback을 지연시키고, 기존 반 구성 버튼의 enabled 상태가 남아 있는 동안 클릭해도 dialog·handler·repository가 fail-closed인지 확인한다.

#### 중복·범위 경계

- stale pending action을 후속 관리자 작업이 무효화하지 않는 일반 lifetime 문제는 `LUNA-0024`에 남긴다. active session에서 그 action이 membership live join을 변경하는 구체적 영향만 이 항목으로 다룬다.
- session 종료 뒤 `currentSession` snapshot refresh 실패는 `LUNA-0026`, batch QR와 student mutation의 공통 gate 부재는 `LUNA-0036`으로 분리한다.

### LUNA-0026 — 수업 종료 transaction 뒤 관리자 snapshot refresh 실패가 이전 활성 수업 UI를 남김

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 09:54:32 / 2026-08-02 21:09:18 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `MainActivity.completeSessionEnd()`·`refreshAdminData()`·`updateSessionAdminControls()`, `StudentRepository.endSession()` 관리자 종료/다음 수업 시작 UI

#### 사실

- `completeSessionEnd()`는 `studentRepository.endSession()` 성공 뒤 `pendingTemporaryStudentIds`만 비우고 `refreshAdminData("Web 로그인과 현재 수업을 안전하게 종료했습니다.")`를 호출한다. 이 성공 분기에서 `currentSession`을 `null`로 먼저 바꾸지 않는다(`MainActivity.kt:2850-2868`).
- `StudentRepository.endSession()`은 Room transaction 안에서 현재 `sessionId`를 확인하고 `session_students`를 지운 뒤, `sessionId`·`classId`가 없는 `ADMIN_IDLE` singleton을 저장한다(`StudentRepository.kt:900-919`). 따라서 종료 transaction 성공 시 DB 상태는 비활성 수업이다.
- `refreshAdminData()`는 반·학생·현재 session·선택 반 소속을 하나의 `runCatching` snapshot으로 읽는다(`MainActivity.kt:913-971`). 읽기 중 하나라도 실패하면 성공 snapshot을 적용하지 않고 `updateSessionAdminControls(currentSession)`, `updateStudentManagementControls()`, `updateClassRosterUi()`를 호출하며 현재 메모리의 `currentSession`은 유지한다.
- `completeSessionEnd()`의 성공 callback에서 호출된 `refreshAdminData()`는 이전 `currentSession`과 선택 snapshot을 캡처한 뒤 같은 단일 `ioExecutor`에 후속 제출된다. 정상 경로에서는 앞선 `endSession()` task가 끝난 뒤 idle row를 읽지만, snapshot의 `ensureClasses()`·class/student/session/membership read 중 하나가 실패하면 failure branch가 idle snapshot을 적용할 기회를 잃는다(`MainActivity.kt:913-971`, `:2850-2868`).
- `updateSessionAdminControls()`는 유지된 이전 `currentSession`에 `sessionId`가 있으면 `startSessionButton`을 `현재 수업 안전 종료`로 두고 반 spinner를 비활성화하며, `adminRefreshFailureMessage()`는 최신 목록을 읽지 못했으니 관리자 화면 재진입을 안내한다(`MainActivity.kt:964-970`, `:1068-1076`, `:2870-2888`).
- 그 stale session을 기준으로 `addTemporaryButton`·`resumeSessionButton`/`recoverSessionButton`·`startSessionButton`의 visibility와 class/session controls가 다시 그려진다. 실제 DB에는 session이 없으므로 이후 종료·보강·QR 대기 재진입 동작은 저장소의 `No active session`/`SESSION_NOT_READY` 실패와 UI가 불일치할 수 있다. 이는 source call graph에 근거한 조건부 영향이며 실제 버튼 조작은 하지 않았다.
- 종료 성공 callback은 `webRecoveryGate.finish()`를 먼저 수행한 뒤 `refreshAdminData()`를 제출하며, 종료 성공을 표시하는 별도 `sessionEndCommitted`/idle projection은 없다(`MainActivity.kt:2850-2868`). 따라서 refresh task가 실패한 순간에는 recovery gate도 닫혀 있고 stale active controls가 다시 사용자 입력을 받을 수 있다.
- `updateStudentManagementControls()`도 stale `currentSession`을 사용해 CSV·pending-card PDF만 active session처럼 잠그고, 단건 QR 재발급·이름·자격정보·비활성화 버튼은 학생 목록이 있으면 계속 enabled로 둔다(`MainActivity.kt:1733-1748`). 이 작업들이 active session에서 의도적으로 허용되는 저장소/시험 경계와 섞이지 않도록, 이번 finding의 직접 영향은 stale 종료 상태 표시·종료/보강/QR 대기 control로 한정한다.
- `showScanner()`의 진입 검사는 Activity 메모리의 `currentSession?.sessionId`·state만 보고 `studentRepository.currentSession()`을 다시 확인하지 않는다(`MainActivity.kt:3031-3061`). 따라서 stale `QR_READY`가 남으면 camera/scanner UI는 열릴 수 있지만 실제 `validateForActiveSession()`은 DB의 `ADMIN_IDLE`을 읽어 `SESSION_NOT_READY`로 거부한다.
- stale scanner에서 QR 거부 기록은 DB의 현재 session ID(null)를 audit한 뒤 성공하면 `resumeScannerAfterCooldown()`으로 다시 analyzer를 켠다(`MainActivity.kt:3325-3345,3560-3569`, `StudentRepository.kt:814-817`). 따라서 메모리 `QR_READY`를 먼저 정리하지 않는 한 실제 idle DB와 반복적으로 어긋난 QR 대기 화면이 유지될 수 있다.
- RC18 운영 문서와 계측시험은 일반 새로고침 실패 시 기존 화면 데이터·수업 상태를 유지하는 동작을 검증하지만, 이미 `endSession()`이 성공한 뒤 active-session snapshot refresh만 실패하는 순서는 검사하지 않는다(`docs/BUILD_VERIFICATION.md:2531-2566`, `MainActivityInstrumentedTest.kt:403-449`).
- `git blame`·history에서 `refreshAdminData()`의 atomic snapshot/failure policy는 `7786ee0 Recover from admin data refresh failures`, `completeSessionEnd()`는 `2e4a14d`, `endSession()`은 `e093bd0` 도입 이력에 남아 있으며 post-commit refresh failure를 별도로 연결한 후속 commit은 확인하지 못했다.

#### 추론

- 활성 수업 종료 transaction이 성공한 뒤 `listClasses()`·`listStudents()`·`currentSession()`·membership 조회 중 하나가 일시적으로 실패하면, 실제 DB는 `ADMIN_IDLE`인데 Activity의 `currentSession`은 이전 active 값으로 남을 수 있다.
- 그 결과 관리자 화면은 종료된 수업을 계속 진행 중으로 표시하고 반 선택을 막으며, 사용자가 다음 수업 시작 대신 `현재 수업 안전 종료`를 다시 누르게 할 수 있다. 재시도는 이미 종료된 DB 상태에서 `endSession()` 중복 실패가 될 수 있고, 새로고침 재진입 또는 Activity 재생성이 필요할 수 있다.
- stale `QR_READY`가 남으면 `showScanner()`가 메모리 session만 보고 카메라 화면으로 들어갈 수 있지만, 실제 QR/manual repository query는 DB의 idle 상태에서 거부될 수 있다. stale non-QR 상태라면 원버튼 복구/안전 종료 UI가 남아 동일한 문제를 다시 제출할 수 있다. 다만 새 관리자 화면 진입의 정상 refresh가 성공하면 idle snapshot으로 수렴할 수 있다.
- `recordQrRejection()`의 정상 성공은 stale `currentSession`을 null로 되돌리지 않고 scanner cooldown만 예약하므로, stale `QR_READY` 상태에서 카드가 계속 `SESSION_NOT_READY`로 거부되는 재시도 loop가 정적으로 성립한다. 이는 QR·학생 data 손상이 아니라 종료 후 UI 상태와 DB idle의 반복 불일치다.
- 이는 데이터 자체를 되돌리는 결함이 아니라 종료 성공 결과와 관리자 UI의 가용성·상태 표시가 어긋나는 복구 경계이므로 P4 후보로 분류한다. 실제 read fault와 화면 상태는 실행하지 않았다.

#### 근본 원인 후보

- `refreshAdminData()`의 일반 failure policy가 “마지막으로 정상 표시된 화면을 유지”하도록 설계된 반면, `completeSessionEnd()`에는 DB 종료 성공을 메모리 상태와 구분하는 `sessionEndCommitted`/safe-idle 전환이 없다. `currentSession`이 UI 표시와 control gate의 공통 상태라 post-commit read failure가 stale active 권위값으로 재사용된다.
- 종료 작업의 성공 결과와 후속 snapshot read가 하나의 사용자-facing 상태 전이로 묶였지만, 두 단계 사이에 실패 전용 상태·재시도 소유권·idempotent idle projection이 없다. 이 구조는 정상 executor 순서와 Room transaction 원자성을 보존해도 UI/DB mismatch를 허용한다.

#### 반대 근거·오탐 검토

- `StudentRepository.endSession()`은 active `sessionId` 확인, temporary student 삭제, `ADMIN_IDLE` 저장, audit를 하나의 Room transaction에 넣고, duplicate end는 거부한다. 따라서 이 finding은 DB partial commit이나 실제 session 데이터 손실 주장이 아니다.
- RC18의 수정 의도는 refresh read 실패 때 빈 목록을 그리지 않고 기존 화면·선택 상태를 유지하며 재진입을 안내하는 것이다(`docs/BUILD_VERIFICATION.md:2531-2558`). 정상 refresh가 성공하면 `applyAdminDataSnapshot()`이 `snapshot.session == null`을 `currentSession`에 반영하고 UI가 idle로 수렴한다.
- 단일 `ioExecutor`는 `endSession()`과 이어진 refresh task의 제출 순서를 보장하고, stale UI에서 호출된 QR/세션 repository 경로는 실제 DB idle/session guard에서 실패한다. 이 제한 때문에 상태 표시·가용성 P4로 유지하고 데이터 손상이나 P1 흐름 차단으로 올리지 않았다.
- 현장/BUILD 기록의 정상 “현재 수업 안전 종료→수업 재시작→QR_READY”와 `ADMIN_IDLE` 최종 확인은 정상 경로의 반대 근거다. 다만 저장소 fault를 종료 commit 직후에 주입한 기록은 없어 현재 후보를 반증하지 않는다.

#### 가정과 미검증

- `refreshAdminData()`의 snapshot read 중 하나가 `endSession()` 성공 직후 실패할 수 있다는 일반 Room/I/O 예외 경계를 가정했다. 고의적인 저장소 fault injection이나 파일/DB 오류를 실행하지 않았다.
- 단일 executor가 `endSession()`과 후속 refresh의 제출 순서를 보장한다는 점은 반대 가설이다. 따라서 이 후보는 정상 ordering/race가 아니라 post-commit snapshot read failure에 한정한다.
- 사용자가 같은 화면에서 다시 관리자 화면을 열 수 있는 정확한 UI 경로와 Activity 재생성·재진입 후 복구 시간은 확인하지 않았다.
- 종료 전 `currentSession`이 active이고 종료 성공 callback이 정상 UI thread에 도착한다는 전제에서 분석했다.
- 종료 성공 뒤 stale `startSessionButton`/`recoverSessionButton`을 다시 눌러 중복 `endSession()`을 제출하는 순서와 stale `QR_READY`에서 QR 거부·cooldown이 반복되는 실제 화면·audit 결과는 확인하지 않았다.

#### 안전한 확인 절차

- `completeSessionEnd()` 성공·실패 분기, `StudentRepository.endSession()` transaction, `refreshAdminData()` snapshot/failure 처리와 `updateSessionAdminControls()`의 버튼·spinner 상태를 읽기 전용으로 대조했다.
- RC18의 새로고침 실패 문서·계측시험과 session lifecycle repository 시험을 비교했다. 실제 session·DB·저장소 fault·화면 조작·테스트·빌드는 수행하지 않았다.
- `MainActivityInstrumentedTest.failedAdminRefreshReleasesStudentMutationAndShowsRetryGuidance()`와 `failedRosterRefreshIsNotRenderedAsEmptyAndDisablesClassActions()`의 fault setup/검증 범위를 읽고, `RepositoryInstrumentedTest`의 정상/중복 `endSession()`만으로 post-end UI 수렴을 증명하지 않았다.
- `MainActivityInstrumentedTest`에는 `completeSessionEnd()` 성공 뒤 repository snapshot fault를 준비하는 시험이나 stale scanner/중복 end button을 누르는 시험이 없음을 별도로 확인했다.

#### 기대 결과와 실제 결과

- 기대: 종료 transaction이 성공하면 Activity도 즉시 `ADMIN_IDLE`을 반영하거나, 최신 snapshot을 읽을 때까지 다음 수업을 안전하게 시작할 수 없는 이유와 재시도 경로를 명확히 표시하되 이전 active session으로 되돌아가지 않아야 한다.
- 실제: 종료 성공 뒤 snapshot refresh가 실패하면 일반 failure 정책에 따라 이전 메모리 `currentSession`을 유지하고 active-session 제어를 다시 그린다. DB 종료 결과와 UI의 수업 상태가 일시적으로 다를 수 있다.
- 실제: `endSession()`의 DB 종료는 완료되지만 `completeSessionEnd()`는 `currentSession`을 직접 갱신하지 않는다. 후속 snapshot이 실패하면 기존 active session을 유지한 채 재시도 안내를 표시하므로, DB와 UI의 수업 상태·버튼 가용성이 일시적으로 다를 수 있다.
- 실제: stale active control은 duplicate end/recovery 제출을 다시 허용할 수 있고, stale `QR_READY`는 memory-only scanner 진입 후 DB idle rejection과 cooldown 재개를 반복할 수 있다. 정상 refresh 재시도 또는 Activity 재생성으로 수렴할 수 있다는 제한은 유지한다.

#### 영향 및 재판단 큐

- 운영/가용성: 다음 수업 시작이 지연되고, 종료된 수업을 다시 종료하려는 오조작과 관리자 상태 혼선이 발생할 수 있다.
- UI/운영: 다음 수업 시작을 위한 반 선택이 잠기고, stale active control로 다시 종료·보강·QR 대기 경로를 제시할 수 있다. 새 관리자 화면 진입·Activity 재생성·재시도 refresh 성공은 완화 경로다.
- Sol·사용자 재판단 후 `completeSessionEnd()` 성공 시 `currentSession = null`과 safe idle UI를 먼저 반영할지, snapshot read 실패를 위한 별도 `SESSION_END_REFRESH_REQUIRED` 상태·재시도 버튼을 둘지 정하고 종료 성공→read failure·다음 수업 시작 회귀시험을 추가할지 결정해야 한다. 이번 Goal에서는 구현하지 않는다.

#### 수정 방향·회귀 위험·수정 후 필요한 검증

- 수정 방향 후보는 종료 transaction 성공 직후 메모리 `currentSession`을 명시적 idle projection으로 전환하고, 최신 목록 read 실패는 `SESSION_END_REFRESH_REQUIRED` 같은 재시도 상태로 표시하는 방식이다. 또는 end operation revision을 snapshot에 묶어 실패 callback이 active UI를 재사용하지 못하게 할 수 있다. 이번 Goal에서는 구현하지 않는다.
- 회귀 위험은 종료 실패 때 기존 active UI를 유지해야 하는 정상 경로와 종료 성공 뒤 safe-idle 전환을 혼동하는 것, 새로고침 재시도 중 반/학생 선택을 과도하게 초기화하는 것, `showScanner()`·resume/start controls를 너무 일찍 숨기는 것이다.
- 수정 후에는 end success→snapshot read failure, end failure→active 유지, retry success→`ADMIN_IDLE`, stale `QR_READY` scanner/duplicate end 차단, temporary student cleanup과 next-session start를 각각 시험해야 한다.

#### 기존 테스트가 잡지 못한 이유

- RC18 계측시험은 active session이 없는 일반 관리자 refresh 실패와 학생 mutation gate 해제를 검사하고, repository 시험은 session end와 다음 start를 순차적으로 검사한다. `endSession()` 성공 후 `refreshAdminData()` snapshot read failure에서 이전 `currentSession`과 `startSessionButton`이 어떻게 남는지는 검증하지 않는다.
#### 필요한 추가 회귀시험

- active `currentSession`을 준비한 뒤 `endSession()` 성공을 실제 DB에 반영하고, 그 직후 `ensureClasses()`·class/student/session/membership read 중 하나만 실패시키는 합성 fault 시험을 추가한다. UI가 `ADMIN_IDLE`/재시도 상태로 남고 `현재 수업 안전 종료`·`QR 대기 화면으로 복귀`가 잘못 활성화되지 않는지 확인한다.
- 종료 실패에서는 기존 active UI와 재시도 안내를 유지하고 DB session이 남는지, 종료 성공 후 refresh 재시도 성공에서는 `currentSession=null`·반 선택·다음 start가 정상 복귀하는지, stale scanner 진입/duplicate end가 repository guard로 차단되는지 확인한다.
- 종료 성공→refresh fault 뒤 `QR_READY` resume→QR rejection/cooldown과 `현재 수업 안전 종료`/원버튼 복구 재클릭을 각각 계측해, memory state가 idle projection으로 닫히는지와 audit·버튼 상태를 확인한다.
- `failedAdminRefresh...`와 `failedRosterRefresh...`의 기존 일반 failure 증거와 구분해 post-commit 시점, Activity recreation/재진입, Web recovery EndSession action 재전달을 각각 검증한다. 후자의 action 유실은 `LUNA-0027`로 분리한다.

#### 중복·범위 경계

- 일반 `refreshAdminData()` 예외 처리와 학생 mutation gate 해제는 RC18/기존 회귀 범위이며, 이 항목은 종료 transaction 성공 뒤의 stale active UI만 다룬다.
- Web recovery 후 `completeSessionEnd()` 자체가 호출되지 않는 Activity 재생성 문제는 `LUNA-0027`, pre-session recovery/admin gate는 `LUNA-0035`로 분리한다.

### LUNA-0027 — Activity 재생성 중 Web recovery 후속 Start/EndSession action이 유실될 수 있음

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 10:01:46~21:17:06 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `MainActivity.pendingRecoveryAction`·`webRecoveryLauncher`, Web POC 관리자 recovery 결과, Kiosk `startSession()`/`endSession()` 후속 DB 전이

#### 사실

- `pendingRecoveryAction`은 `MainActivity`의 일반 메모리 필드로 `None`으로 초기화되며, `StartSession(classId, temporaryStudentIds)` 또는 `EndSession`을 Web recovery Intent 실행 직전에 저장한다(`MainActivity.kt:201`, `:2459-2487`, `:3961-3968`).
- `webRecoveryLauncher` 결과 callback은 `val requestedAction = pendingRecoveryAction`을 읽고, `None`이면 `refreshAdminData()`만 수행하며, `StartSession`일 때만 `completeSessionStart()`를, `EndSession`일 때만 `completeSessionEnd()`를 호출한다(`MainActivity.kt:310-334`).
- 결과 callback은 후속 dispatch 전에 `pendingRecoveryAction = PendingRecoveryAction.None`으로 먼저 지운다. 따라서 action이 새 Activity에서 기본값으로 시작했거나 callback 전에 소실되면, 같은 `RESULT_OK`를 받아도 재구성된 인자나 재시도용 action이 결과 payload에서 복원되지 않는다(`MainActivity.kt:313-327`).
- Kiosk가 실행하는 explicit Web recovery Intent에는 action 문자열과 trusted Web POC component만 있고, operation ID·class/session snapshot·expected state·mutation revision·idempotency token을 extras로 전달하지 않는다(`MainActivity.kt:2473-2480`). `PendingRecoveryAction.StartSession`도 `classId`와 temporary 학생 ID 집합만, `EndSession`은 별도 인자 없이 보유한다(`MainActivity.kt:3961-3968`).
- `MainActivity`에는 `pendingRecoveryAction`을 `onSaveInstanceState()`에 저장하거나 `savedInstanceState`·persistent DB에서 복원하는 코드가 없다. 현재 `onCreate(savedInstanceState)`는 항상 새 객체의 `PendingRecoveryAction.None`에서 시작한다(`MainActivity.kt:337-367`, `:589-613`).
- Kiosk manifest는 `keyboardHidden|orientation|screenSize`를 `configChanges`로 처리하고 `MainActivity`를 `singleTask`로 선언한다(`kiosk/src/main/AndroidManifest.xml:35-41`). 따라서 일반 회전·화면 크기·키보드 구성 변경은 이 finding의 전형적인 재생성 경로를 줄이는 반대 근거지만, process reclaim·기타 Activity 재생성 뒤 `pendingRecoveryAction`을 복원하는 계약은 여전히 없다.
- Web POC 관리자 recovery는 자체 상태를 정리한 뒤 `RESULT_OK`/`RESULT_CANCELED`를 반환하고 종료한다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:1575-1595`). Kiosk callback이 action을 잃으면 Web 정리는 성공해도 Kiosk의 `startSession()` 또는 `endSession()`은 호출되지 않는다.
- Web POC manifest도 `configChanges=keyboardHidden|orientation|screenSize`, `singleTop`, `stateNotNeeded`를 사용하고(`webpoc/src/main/AndroidManifest.xml:21-30`), `onCreate()`는 전달받은 saved instance state를 사용하지 않고 `super.onCreate(null)`을 호출한 뒤 SharedPreferences의 `KEY_STATE`를 읽는다(`webpoc/MainActivity.kt:153-183`, `:225-243`). `onNewIntent()`는 recovery action을 다시 시작할 뿐 operation ID·Kiosk 후속 action을 받지 않으며, `onStop()`은 configuration change일 때만 관리자 recovery 취소를 건너뛴다(`webpoc/MainActivity.kt:200-207`, `:2356-2366`). 이는 Web 상태의 lifecycle 완화이지 Kiosk caller action의 persistence가 아니다.
- Git history상 외부 recovery 경계는 `05e2603`(RC02), Kiosk 후속 `pendingRecoveryAction`은 `2e4a14d`(RC03)에서 도입됐고, 이후 `630fce1`은 StartSession convenience/preflight 사용을 확장했다. 대상 Kiosk `MainActivity.kt`에 `onSaveInstanceState`를 추가한 이력은 `git log -S`에서 확인되지 않았다. 이는 구현 의도에 대한 추론이 아니라 현재 history 검색 결과의 범위다.
- 기존 계측시험은 초기 상태 실패·일반 refresh failure·Web session result/renderer recovery를 다루지만, Kiosk Activity 재생성 중 외부 Web recovery 결과를 다시 전달받아 원래 Start/End action을 보존하는 시험은 찾지 못했다. 검색된 `RecoveryInstrumentedTest`의 `RESULT_CANCELED` 검사는 untrusted secure-session caller 경계이고, Activity 재생성 시험은 Web renderer recovery이며, Kiosk `MainActivityInstrumentedTest`에는 `RECOVER_WEB_SESSION`·`pendingRecoveryAction`·`recreate()`를 결합한 시험이 없다. 운영 문서의 재부팅 recovery는 `RECOVERY_REQUIRED → 안전 종료 → 새 수업 시작` 결과만 기록한다(`docs/BUILD_VERIFICATION.md:395-403`).

#### 추론

- Kiosk가 Web recovery 화면을 실행한 뒤 system-driven Activity recreation 또는 process recreation이 발생하면, ActivityResultRegistry가 외부 Activity 결과를 새 Kiosk Activity에 전달하더라도 새 인스턴스의 `pendingRecoveryAction`은 `None`일 수 있다.
- Kiosk manifest의 `configChanges`·`singleTask`는 일반 orientation/keyboard/screen-size 변경을 이 경로의 강한 반대 근거로 만든다. 그러나 그 설정만으로 OS process reclaim 또는 다른 lifecycle 재생성 때 Activity 메모리 field가 보존되지는 않으므로, 발견 범위는 “모든 회전”이 아니라 저장되지 않은 외부 작업의 조건부 유실로 한정한다.
- 근본 경계는 Web 상태 정리와 Kiosk session mutation이 서로 다른 프로세스·서로 다른 완료 단계인데, 둘 사이에 durable operation record나 동일한 완료 token을 묶는 원자적/멱등성 계약이 없다는 점이다. Web 쪽 자체 recovery 상태가 보존돼도 caller 쪽 후속 action 보존까지 보장하지 않는다.
- 같은 Activity에서 결과 callback이 실제로 중복 도착한다면 첫 callback의 선행 clear 뒤 후속 callback은 `None` branch로 일반 `refreshAdminData()`만 수행한다. 결과 ID·소비 sequence·멱등성 token이 없으므로 중복 결과를 원래 Start/End operation으로 판별하는 정적 경계도 없다.
- `StartSession`의 경우 Web 사전점검이 성공해도 Kiosk DB에 session이 생성되지 않고, `EndSession`의 경우 Web 상태는 `IDLE`로 정리됐지만 Kiosk DB의 기존 수업과 `session_students`는 종료되지 않는다. 두 경우 모두 사용자는 성공/정리 결과와 Kiosk 수업 상태가 어긋난 상태를 다시 확인해야 한다.
- 반대로 Kiosk 후속 DB 작업은 단일 `ioExecutor`와 `StudentRepository`의 transaction/expected-state 검사에 의해 중복·partial write 위험이 제한된다. 이 보호는 이미 유실된 action을 재생성하거나 Web 성공 뒤 누락된 Start/End를 자동으로 실행하지는 않는다.
- Web POC는 자체 persisted state를 SharedPreferences로 관리하고 `onCreate(null)`로 saved instance state 대신 저장 상태를 사용하며, 일반 background에서는 recovery를 실패 처리하고 configuration change에서는 즉시 실패시키지 않는다(`webpoc/MainActivity.kt:153-207`, `:270-285`, `:2356-2381`). 이는 Web recovery 자체의 lifecycle 완화 근거지만 Kiosk caller action의 보존 근거는 아니다.
- 실제 Android lifecycle 재생성·결과 재전달·중복 delivery는 실행하지 않았고, 재시작 뒤에는 초기 state load가 DB를 다시 읽는 완화책이 있으므로 P4 후보로 유지한다.

#### 가정과 미검증

- ActivityResultRegistry가 caller Activity recreation 뒤 pending result를 새 등록 callback에 전달할 수 있다는 Android lifecycle 동작을 전제로 분석했지만, 이 저장소의 실제 Android 13 환경에서 재현하지 않았다.
- Kiosk manifest가 흡수하는 일반 configuration change와 달리 Activity가 Web recovery Activity 위에서 system reclaim·process recreation·기타 lifecycle 재생성을 겪는 정확한 조건과 결과 delivery timing은 확인하지 않았다.
- `RESULT_OK`/`RESULT_CANCELED`의 단순 결과와 Kiosk callback의 action 선행 clear가 실제 재생성·중복 delivery 순서에서 어떻게 결합하는지, operation token 없는 재시도·재진입이 어떤 UI 문구와 DB snapshot을 만드는지는 검증하지 않았다.
- Web POC의 recovery 자체는 성공하고 Kiosk 후속 DB 작업만 유실되는 순서를 가정했으며, 실제 Web 상태·session·학생·DB는 사용하지 않았다.
- 이번 회차에는 테스트·lint·typecheck·build·install·ADB 및 fault injection을 실행하지 않았다.

#### 안전한 확인 절차

- `pendingRecoveryAction`의 생성·소비·초기화·Activity lifecycle 연결과 Web POC의 recovery result 반환 경계를 읽기 전용으로 대조했다.
- Kiosk/Web manifest의 `configChanges`·launch mode·`stateNotNeeded`, Web `onNewIntent()`·`onStop()`의 configuration/background 분기와 관련 계측시험 검색 결과를 추가 대조했다.
- RC02/RC03 및 후속 StartSession 변경의 `git log`·`git blame`을 읽어 action 도입 뒤 lifecycle persistence/operation token 변경 여부를 확인했다.
- `MainActivityInstrumentedTest`, `BUILD_VERIFICATION.md`, 현장 시나리오에서 Kiosk caller recreation 중 Start/End action 보존 검증 여부를 검색했다. 소스·테스트·문서·기기·Web 상태는 변경하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 외부 Web recovery가 끝날 때까지 Kiosk가 operation identity와 원래 `StartSession`/`EndSession` 인자를 durable하게 보존하고, Activity 재생성·결과 중복 뒤에도 expected class/session 상태를 확인하며 정확한 후속 DB 전이를 한 번만 수행해야 한다.
- 실제: action은 Activity 메모리에만 있고 Intent/result에 복원용 identity가 없으며, 새 callback에서 `None`이면 후속 session mutation 없이 일반 관리자 refresh만 수행한다. Web recovery 성공과 Kiosk DB 전이는 하나의 원자적 완료로 묶여 있지 않다.

#### 영향 및 재판단 큐

- 운영/가용성: 수업 시작이 조용히 진행되지 않거나, Web 상태만 종료되고 Kiosk 수업·보강 명단이 남아 다음 작업과 상태 안내가 어긋날 수 있다.
- Sol·사용자 재판단 후 action을 saved state/DB operation record로 보존할지, Web recovery result에 action token·class/session snapshot을 포함할지, 재생성 뒤 idempotent resume을 둘지 정하고 Start/End recovery 중 Activity recreation·결과 중복 전달 회귀시험을 추가할지 결정해야 한다. 이번 Goal에서는 구현하지 않는다.

#### 기존 테스트가 잡지 못한 이유

- 현재 시험은 Web 결과 persistence의 expected-state, 초기 상태 recovery, 일반 Activity/WebView 재생성 또는 refresh failure를 각각 검사하지만, Kiosk Activity가 외부 recovery를 기다리는 동안 재생성된 뒤 `PendingRecoveryAction.None`으로 결과를 소비하는 경로는 검증하지 않는다.
- 최소 회귀 범위는 StartSession/EndSession 각각에 대해 configuration change·process reclaim·재생성 전후 `RESULT_OK`/`RESULT_CANCELED`, callback 중복·지연 전달, class/session revision 불일치, executor submit 실패를 확인하고, Web `onStop()`의 background/configuration 분기를 caller action 결과와 함께 검증하는 것이다. 현재 이 범위의 Kiosk 시험 연결은 확인되지 않았다.

### LUNA-0028 — 관리자 PIN verifier의 salt·derivedKey 배열이 인증·초기 조회 뒤 zeroize되지 않음

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 10:22:19~13:45:50 / 2026-08-02 21:24:49 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `AdminAuthRepository`·`AdminPin`·Room `AdminCredentialEntity` 관리자 PIN 인증/초기 상태 조회

#### 사실

- `AdminCredentialEntity`는 `salt: ByteArray`와 `derivedKey: ByteArray`를 보관한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/Entities.kt:131-140`). `AdminAuthRepository.isEnrolled()`와 `enrolledPinLength()`는 이 전체 entity를 `adminDao().get()`으로 읽은 뒤 boolean 또는 길이만 반환하며 배열을 정리하는 경로가 없다(`AdminAuthRepository.kt:17-21`).
- `AdminDao.get()` 자체가 `SELECT * FROM admin_credential`만 제공하므로 초기 bootstrap의 존재·PIN 길이 조회에도 BLOB 두 개가 Room entity로 materialize된다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/Daos.kt:192-198`). scalar-only query나 조회 결과의 소유권을 즉시 지우는 별도 API는 확인되지 않았다.
- `enroll()`은 `AdminPin.create()`가 만든 `PinVerifier`의 salt·derivedKey 배열을 그대로 Room entity에 넣어 저장한다. 저장 성공·예외 뒤 local verifier 배열을 zeroize하는 `finally`가 없고, `enroll()`의 `finally`는 입력 PIN `CharArray`만 덮어쓴다(`AdminAuthRepository.kt:23-42`).
- `authenticate()`는 Room entity 배열을 그대로 `PinVerifier`에 alias한다(`AdminAuthRepository.kt:47-57`). `AdminPin.verify()`는 비교용으로 새로 만든 candidate derived key만 zeroize하고 verifier의 salt·derivedKey는 건드리지 않는다(`AdminPin.kt:41-49`). repository의 `finally`도 입력 PIN만 덮어쓴다(`AdminAuthRepository.kt:79-81`).
- `loadInitialState()`는 `isEnrolled()`와 `enrolledPinLength()`를 연속 호출하므로 같은 bootstrap에서 full row materialization이 두 번 발생한다(`MainActivity.kt:589-605`). 인증이 이미 lockout 중이어도 `authenticate()`는 full entity를 읽은 뒤 `verifier`를 만들지 않고 반환하며, 그 entity 배열을 정리하는 경계는 여전히 없다(`AdminAuthRepository.kt:45-51`).
- 인증 성공·실패 시 `entity.copy(...)`가 배열 필드를 공유한 상태로 Room `save()`에 전달되고, `PinVerifier`도 같은 entity 배열을 참조한다. `data class` copy 자체의 deep-copy/clear 계약이 없으므로 save binding, local entity, verifier의 각 owner가 명시되지 않는다(`AdminAuthRepository.kt:59-75`, `Entities.kt:131-140`).
- `AdminPin.create()`와 `PinVerifier`에는 verifier 배열을 안전하게 지우는 `clearSensitiveData()`/`use` 소유권 API가 없다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/security/AdminPin.kt:8-39`). 저장된 DB BLOB를 훼손하지 않으려면 Room write에 넘길 복사본과 local transient owner를 분리해야 하지만, 현재 코드에는 그 경계가 없다.
- `AdminPin.create()`는 16바이트 `salt`를 할당한 뒤 `derive()`를 호출하지만 salt를 지우는 `finally`가 없다. `derive()`는 `PBEKeySpec.clearPassword()`만 수행하므로 PBKDF2/provider·allocation 예외 시 생성 중간 salt의 수명 계약도 없다. salt 자체는 verifier key보다 민감도가 낮아 이 경로만으로 심각도를 상향하지 않는다(`AdminPin.kt:30-58`).
- PBKDF2 `PBEKeySpec` password와 후보 derived key는 각각 `clearPassword()`·`fill(0)`로 정리된다. 따라서 이 finding은 원문 PIN 입력 정리 실패나 평문 PIN 저장을 주장하는 것이 아니라, 저장된 verifier의 transient ByteArray 수명 경계를 지적한다.
- `SensitiveTask`는 queued task discard·실행 실패·Activity `onDestroy()`의 `shutdownNow()`에서 호출자가 넘긴 cleanup을 정확히 한 번 실행한다(`SensitiveTask.kt:5-22`, `MainActivity.kt:3859-3887`). 그러나 `executeSensitive()`의 cleanup은 입력 `pin` 하나뿐이고, repository 내부에서 생성·alias된 `PinVerifier`/`AdminCredentialEntity` 배열을 등록하거나 반환받아 지우는 callback은 없다.
- Kiosk manifest는 `allowBackup=false`·`fullBackupContent=false`이고 `data_extraction_rules.xml`은 cloud/device-transfer의 database를 제외한다(`kiosk/src/main/AndroidManifest.xml:25-34`, `kiosk/src/main/res/xml/data_extraction_rules.xml:2-16`). 이는 verifier가 자동 backup/device-transfer로 복제되는 경로를 줄이는 반대 근거이지 same-process heap 수명이나 승인된 debug/DB 접근을 해결하지 않는다.
- Room v2→v3 migration은 기존 `admin_credential`의 `salt`·`derivedKey`를 유지한 채 `pinLength DEFAULT 0`만 추가하고, migration 시험도 합성 `X'01'`/`X'02'` BLOB와 schema/값 보존만 검사한다(`KioskDatabase.kt:87-94`, `KioskDatabaseMigrationInstrumentedTest.kt:61-90`). migration 경계의 배열 ownership·clear는 관찰하지 않는다.
- 현재 `AdminPinTest`는 형식·일치 검증·lockout delay만 검사하고, `SensitiveTaskTest`는 임의 cleanup callback의 한 번 실행만 검사한다. MainActivity 계측시험은 PIN 자동 제출·길이 bootstrap·`FLAG_SECURE`를, migration 시험은 `pinLength=0` 기본값과 credential row 보존을 확인하지만, `AdminAuthRepository`의 초기 조회/인증/등록·Room binding 뒤 salt·derivedKey cleanup 또는 heap 수명을 검증하는 시험은 찾지 못했다.

#### 추론

- 관리자 PIN 확인·초기 상태 로드·PIN 길이 조회 뒤 Room entity/`PinVerifier`/SQLite binding 주변의 배열이 GC 전까지 heap에 남을 수 있다. verifier는 원문 PIN은 아니지만, 동일 salt와 derived key가 노출되면 오프라인 PIN 추측 검증에 사용될 수 있다.
- `enroll()`의 이미 등록됨 검사와 lockout 중 `authenticate()`는 verifier를 만들기 전에도 full entity의 BLOB를 보유한 채 반환/예외 종료한다. 성공·실패 인증의 `entity.copy()`는 ByteArray를 deep-copy하지 않으므로 DB save가 끝나거나 예외가 난 뒤에도 entity·verifier·Room binding 주변의 참조 graph가 분리됐다는 증거가 없다.
- Kiosk의 일반 앱 간격리와 PBKDF2 verifier 저장이 정상이어도, 승인된 디버그/heap 접근·프로세스 메모리 노출 같은 더 강한 전제에서 transient verifier의 잔류가 공격 표면을 늘릴 수 있으므로 P4 후보로 둔다. 실제 heap dump·GC·메모리 공격은 수행하지 않았다.
- `allowBackup`·database/device-transfer exclusion, PIN 입력 `SensitiveTask` cleanup, `PBEKeySpec`/candidate cleanup은 backup·원문 입력·정상 비교 경로의 노출을 줄인다. 따라서 이 정적 ownership 공백을 raw PIN 저장·일반 사용자 원격 노출·P2 이상으로 확대하지 않는다.

#### 가정과 미검증

- Room DAO가 반환·쓰기 과정에서 내부 ByteArray 복사본을 더 만들 수 있다는 점과 JVM/ART GC가 즉시 회수하지 않는다는 일반 메모리 동작을 전제로 삼았다. 실제 Room generated code·ART heap 수명은 실행하지 않았다.
- salt는 비밀값이 아니며 derivedKey는 PBKDF2 verifier라는 점을 반영해 raw PIN 노출보다 낮은 P4로 분류했다.
- `AdminPinTest`에서 verifier를 생성·재사용하는 정상 JVM 동작은 읽었지만, 배열 identity·Room cursor/binding 복사 횟수·DB write 예외 뒤 실제 참조 graph는 실행·계측하지 않았다.
- `AdminPin.create()`의 provider/derive 예외, duplicate enroll, lockout early return과 executor shutdown/rejection에서 verifier 배열이 실제로 잔류하는 heap 상태는 실행하지 않았다. backup/device-transfer exclusion의 설치·OS 적용도 읽기 전용 정적 선언만 확인했다.
- 저장된 DB 바이트를 zeroize하면 안 된다는 전제에서, 저장 직전/직후 필요한 복사본과 읽기용 임시 배열을 분리하는 수정이 필요할 수 있다. 이번 Goal에서는 구현하지 않는다.

#### 안전한 확인 절차

- `AdminAuthRepository`, `AdminPin`, `AdminCredentialEntity`, `AdminDao`, Room schema/migration, MainActivity bootstrap/auth 호출부, `AdminPinTest`·`SensitiveTaskTest`·migration/MainActivity 계측시험과 `GATE4_IMPLEMENTATION.md`·`REMOTE_ADMIN_PIN.md`·운영 문서를 읽기 전용으로 대조했다. 관련 PIN 길이 변경 commit `9af507b`의 diff도 읽었다.
- `SensitiveTask`의 queue/rejection cleanup, Kiosk manifest·data extraction rules의 backup 경계, v2→v3 migration SQL·fixture, `e093bd0` 도입 history와 `git blame`을 추가로 대조해 입력 cleanup·DB 복제 제한을 verifier ownership과 분리했다.
- 실제 PIN 입력·DB 저장/인증·heap dump·GC 유도·ADB·테스트·빌드는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 원문 PIN과 verifier 계산용 transient arrays가 각 경로의 DB binding·비교 완료 뒤 정리되고, persisted verifier를 손상시키지 않는 ownership/clone 계약과 회귀시험이 있다.
- 실제: 입력 PIN과 candidate key 일부는 정리되고 backup/device-transfer도 제외되지만, 초기 조회용 full entity, 인증에서 alias된 `PinVerifier`, 등록 저장 전후 local verifier와 Room binding 주변의 salt·derivedKey 배열은 zeroize 계약 없이 반환·폐기된다. `SensitiveTask`가 이 내부 배열을 대신 소유하지 않는다.

#### 영향 및 재판단 큐

- 보안: same-process 메모리 노출·승인 디버그/heap 분석 전제에서 관리자 PIN verifier 잔류 시간이 늘어날 수 있다. 일반 앱 UI·학생 데이터·Web credential 흐름의 직접 영향은 확인하지 않았다.
- Sol·사용자 재판단 후 verifier 배열의 ownership을 명시하고, 초기 조회에는 scalar-only DAO를 사용하며, 인증·등록의 DB binding에 필요한 persisted copy와 transient copy의 cleanup 시점을 정의할지 결정한다. `isEnrolled()`·`enrolledPinLength()`·성공/실패 인증·등록 저장 예외·Room binding 뒤 reference cleanup을 포함한 zeroization 회귀시험이 필요하다. 이번 Goal에서는 구현하지 않는다.

#### 기존 테스트가 잡지 못한 이유

- 현재 시험은 `AdminPin`의 알고리즘 결과·lockout 계산, generic `SensitiveTask` cleanup, UI 자동 인증/`pinLength` bootstrap과 migration schema 보존을 각각 검사한다. repository의 전체 entity 조회, scalar-only 조회 부재, Room array 복사, 인증 성공/실패·등록 DB 예외 뒤 verifier ownership을 종단 연결해 관찰하는 시험은 없다.
- MainActivity 계측시험은 실제 `AdminAuthRepository.enroll()`과 `enrolledPinLength()`를 호출하고 PIN UI 결과를 확인하지만, 해당 호출 뒤 verifier 배열 identity·cleanup·heap 수명을 관찰하지 않는다. migration 시험의 합성 BLOB도 실제 PBKDF2 verifier 길이·Room cursor/binding ownership을 대표하지 않는다.

### LUNA-0029 — Web loopback CONNECT proxy의 client·upstream socket/thread 자원 상한이 없음

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 10:25:44 / 2026-08-02 21:30:55 (Asia/Seoul)
- 영향 모듈·버전: Web POC `LoopbackConnectProxy`·`WebViewProxyBootstrap`의 process-wide 로컬 HTTPS tunnel

#### 사실

- `LoopbackConnectProxy.start()`는 `ServerSocket`을 `127.0.0.1:0`에 바인딩하고, 별도 인증·호출자 식별 없이 accept loop를 시작한다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/LoopbackConnectProxy.kt:42-63,184-191`).
- proxy는 `Executors.newCachedThreadPool()`을 사용하며, `CloseableRegistry<Socket>`에는 client/upstream 수량 상한이 없다. accepted client마다 handler task를 만들고, 허용된 CONNECT마다 upstream socket과 반대 방향 copy task를 추가한다(`LoopbackConnectProxy.kt:31-39,65-76,93-125`).
- 초기 request header에는 8KiB 상한과 10초 socket timeout이 있지만, CONNECT가 성립하면 client와 upstream의 `soTimeout`을 0으로 바꾸고 peer가 닫을 때까지 tunnel을 유지한다(`LoopbackConnectProxy.kt:82-95,124-145`).
- `WebViewProxyBootstrap`은 process-wide singleton coordinator가 성공한 proxy를 보유하고, Activity `onDestroy()`는 network callback·WebView·민감 상태만 정리하며 proxy를 닫거나 coordinator를 reset하지 않는다(`WebViewProxyBootstrap.kt:10-56`, `webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:2383-2425`). 따라서 앱 프로세스가 살아 있는 동안 자원 생명주기는 proxy singleton에 묶인다.
- `CloseableRegistry.register()`·`closeAll()`은 같은 monitor에서 종료 상태와 등록 목록을 직렬화하고, 종료 뒤 늦게 도착한 socket은 즉시 닫는다. `ProxyTaskSubmission`은 executor rejection 때 client/upstream cleanup callback을 실행하며, `ProxyBootstrapCoordinator`는 setup 예외·watchdog timeout 때 candidate proxy를 닫고 late ready callback을 무시한다(`ProxyLifecycle.kt`, `ProxyBootstrapCoordinator.kt:35-81`).
- 위 cleanup은 bootstrap 실패·proxy 명시적 `close()` 경계에 한정된다. 성공 후 `WebViewProxyBootstrap`에는 proxy를 닫거나 재설정하는 공개 lifecycle 경계가 없고, 정상 tunnel의 idle·동시 socket·worker 수를 줄이는 budget도 확인되지 않는다.
- `LoopbackConnectProxy.acceptLoop()`는 `ServerSocket.accept()`의 `IOException`을 잡고 그대로 return하며 coordinator·diagnostic state·재시작 callback을 호출하지 않는다(`LoopbackConnectProxy.kt:61-79`). `WebViewProxyBootstrap`의 coordinator는 성공 뒤 `READY`를 유지하고 `ensureConfigured()` 호출에 기존 `READY`를 즉시 반환하므로, 이미 proxy port가 죽어도 health check나 새 `LoopbackConnectProxy.start()`가 실행되지 않는다(`ProxyBootstrapCoordinator.kt:42-57`, `WebViewProxyBootstrap.kt:55-62`).
- Web recovery의 `recoveryButton`은 `restartForRecovery()`에서 Activity만 `recreate()`하고(`MainActivity.kt:839,1731-1738`), 새 `onCreate()`의 `WebViewProxyBootstrap.ensureConfigured()`는 process-wide coordinator의 `READY`를 재사용한다. 따라서 proxy accept loop가 비정상 종료된 뒤의 Activity recovery·renderer recovery가 proxy 자체 복구를 보장하지 않는다.
- 현재 proxy lifecycle 시험은 registry의 shutdown race·rejected task cleanup을 검사하고, bootstrap 시험은 setup/timeout/late callback cleanup을 검사하지만, 동시 client/upstream 수·thread/FD 상한·idle tunnel·localhost 호출자 분리를 검증하지 않는다(`LoopbackProxyLifecycleTest.kt`, `ProxyBootstrapCoordinatorTest.kt`).
- 현재 시험은 synthetic bootstrap failure/timeout/late callback과 registry/rejection cleanup을 검증하지만, `acceptLoop()`의 unexpected `IOException`, `READY` health loss, Activity recreate 뒤 proxy restart, dead-port 이후 WebView main-frame recovery를 결합한 시험은 찾지 못했다.

#### 추론

- 같은 Android 기기의 다른 local process 또는 오작동한 WebView가 random loopback port를 탐색해 연결을 유지하거나 허용 host로 CONNECT를 반복하면, proxy는 client/upstream socket과 cached-pool worker를 계속 보유할 수 있다. 허용 목록과 TLS pass-through 때문에 이 경로가 곧 외부 host 임의 접근이나 평문 탈취를 뜻하지는 않지만, 프로세스 thread·file descriptor·메모리 고갈로 Web 로그인/학습 화면을 중단시킬 수 있다.
- 연결 전 partial header에는 10초 timeout이 있지만, 성공한 tunnel은 idle timeout이 없고 global resource budget도 없어 장시간 idle 연결의 수명은 peer/OS에 의존한다. 실제 local scan·socket hold·FD/thread 고갈은 실행하지 않았으므로 P4 후보로 유지한다.
- proxy accept loop가 예기치 않은 `IOException`으로 종료되는 조건이 발생하면 coordinator가 `READY`로 남아 다음 WebView 요청·recovery 재생성도 동일한 dead port를 사용할 수 있다. WebView `onReceivedError()`는 network pause 또는 `showLocked()`로 실패폐쇄할 수 있지만 proxy bootstrap 재시작은 호출하지 않는다(`MainActivity.kt:564-601,1819-1848`). 이 health/restart 공백은 resource cap과 같은 process-wide proxy lifecycle 범위로 보강한다.
- 반대로 target host는 `.matholic.com`과 두 exact host의 443 포트로 제한되고, listener는 loopback `127.0.0.1`에만 열리며 TLS를 종료·복호화·기록하지 않는다. backlog 16, 연결 전 10초 timeout, upstream connect 10초 timeout, shutdown 시 server socket·등록 socket·executor 정리와 rejection cleanup은 자원 고갈 범위를 제한하는 반대 근거다.

#### 가정과 미검증

- Android 앱 간 loopback TCP 연결과 random port 탐색이 가능한 local adversary/buggy peer를 가정했다. 실제 기기 network namespace·다른 앱 접근성은 확인하지 않았다.
- 정상 WebView가 만드는 동시 연결 수가 운영 자원 한도를 넘지 않는다는 전제에서는 문제가 나타나지 않는다. 실제 WebView·네트워크·socket·thread·FD 상태는 사용하지 않았다.
- `WebViewProxyBootstrap`의 process-wide 보유가 의도된 설계이며 Activity 재생성마다 proxy를 닫지 않는다는 문서·코드 주석을 반대 가설로 반영했다. 따라서 Activity별 close 누락 자체를 결함으로 확정하지 않고, process-wide lifetime에 필요한 예산·정상 종료 관찰 공백으로 한정했다.
- `acceptLoop()`가 실제로 unexpected `IOException`으로 끝나는 원인, coordinator `READY`와 dead port가 동시에 유지되는 실제 Android/WebView 상태, `onReceivedError()` 뒤 recovery·proxy 재시작 결과는 실행하지 않았다. 정상 process death가 OS 차원에서 daemon thread/socket을 회수하는 경로도 관찰하지 않았다.

#### 안전한 확인 절차

- `LoopbackConnectProxy`, `CloseableRegistry`, `ProxyTaskSubmission`, `ProxyBootstrapCoordinator`와 WebView lifecycle·proxy/접속 lifecycle 시험 소스를 읽기 전용으로 대조했다.
- `MainActivity`의 `onReceivedError()`·`showLocked()`·`restartForRecovery()`와 `WebViewProxyBootstrap.ensureConfigured()`의 `READY` 재사용, RC26~RC28 운영 기록·관련 Git history를 추가로 대조했다.
- 실제 proxy 시작·CONNECT·네트워크 접속·localhost scan·socket 유지·Activity 종료·테스트·빌드는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: process-wide loopback proxy가 의도된 lifetime 동안 client/upstream 동시 수·worker/queue·idle tunnel 예산을 명시하고, 예산 초과 연결을 닫으며, bootstrap 실패·timeout·process 종료에서 모든 proxy 자원을 회수한다.
- 실제: 127.0.0.1에 무인증으로 바인딩하고 cached thread pool·무상한 socket registry를 사용하며, 성립한 tunnel은 `soTimeout=0`으로 peer 종료까지 유지한다. bootstrap 실패·timeout과 명시적 proxy `close()`에는 cleanup이 있지만 정상 성공 상태의 resource budget·idle 회수·accept-loop health loss 복구·공개 process teardown 관찰은 없다.

#### 영향 및 재판단 큐

- 운영/가용성: local connection flood 또는 비정상 WebView로 Web POC의 worker·FD·socket 자원이 고갈되어 시작·로그인·학습 상태가 실패폐쇄/복구 상태로 이동할 수 있다.
- Sol·사용자 재판단 후 client/upstream 총량, worker/queue, header·idle timeout, localhost 호출자 통제 가능성을 정의하고 resource-exhaustion 회귀시험을 추가할지 결정한다. `LUNA-0007` PC receiver resource cap 후보와는 endpoint·프로세스·local loopback 공격 경계가 다르므로 별도 유지한다. 이번 Goal에서는 구현하지 않는다.

#### 기존 테스트가 잡지 못한 이유

- 기존 시험은 shutdown/late callback과 개별 cleanup 소유권만 검사한다. 허용 CONNECT를 여러 개 열고 partial/idle tunnel을 유지한 상태에서 thread·FD·socket budget이 초과될 때 거부·회수되는지, 성공한 process-wide proxy의 정상 teardown이 언제 호출되는지는 검증하지 않는다.
- `ProxyBootstrapCoordinatorTest`의 successful `READY` 시험은 proxy handle을 유지하는 결과만 확인하고, `LoopbackProxyLifecycleTest`는 registry/rejection helper만 확인한다. accept loop fault→coordinator failure/restart→Activity recovery와 resource budget/idle timeout을 종단 연결한 시험은 없다.

### LUNA-0030 — PC pairing QR 원문·Base64 secret String이 decode/save 경계 뒤 zeroize되지 않음

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 10:29:03 / 2026-08-02 21:37:22 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `QrImageAnalyzer`·`MainActivity` PC pairing scanner·`PcPairingStore`·`PcReceiverPairing`

#### 사실

- PC pairing QR의 `barcode.rawValue`는 immutable `String`으로 `MainActivity.handleRawQr()`에 전달되고, `savePcPairing(rawValue)`의 `ioExecutor` 작업이 원문을 캡처해 `PcPairingStore.save(rawPairing)`으로 넘긴다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/qr/QrImageAnalyzer.kt:82-86`, `MainActivity.kt:2975-3004`). 이 경계에는 String을 지우거나 소비 후 참조를 끊는 명시적 ownership 계약이 없다.
- `handleRawQr()`는 raw String을 다시 `runOnUiThread` closure에 캡처한 뒤 `savePcPairing()`으로 넘긴다. Activity `onDestroy()`는 `mainHandler` callback을 제거하고 `ioExecutor.shutdownNow()`의 반환 목록 중 `SensitiveTask`만 discard하지만, pairing 저장은 일반 lambda라 raw String을 zeroize하는 cleanup이 없다(`MainActivity.kt:2986-2999,3859-3873`).
- `PcPairingStore.save(rawPairing)`는 원문 String을 `PcReceiverPairing.decode()`로 파싱한 뒤 같은 원문을 `saveEncrypted(rawPairing)`에 다시 전달한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcPairingStore.kt:18-26`). `decode()`는 payload byte array를 지우지만, 호출자 원문 String은 지울 수 없다.
- `PcReceiverPairing.encode()`는 receiver ID·secret을 Base64로 합친 새 immutable String을 반환하고 payload/host/name byte array만 지운다(`PcReceiverPairing.kt:42-70`). `PcPairingStore.save(pairing)`와 `withReachablePairedPc()`의 endpoint recovery 저장 경로는 이 String을 `saveEncrypted()`에 넘긴 뒤 String을 정리하지 않는다(`PcPairingStore.kt:29-40`, `MainActivity.kt:807-839`).
- `PcPairingStore.load()`도 복호화 plaintext byte array를 지우기 전에 `String(plaintext, UTF-8)`을 `decode()`에 만들어 encoded pairing secret String을 생성한다(`PcPairingStore.kt:45-70`). Android `SharedPreferences`의 Base64 ciphertext 자체는 암호화되어 있지만, 이 finding은 저장된 ciphertext가 아니라 런타임의 원문/encoded String 수명에 관한 것이다.
- `PcReceiverPairing.clearSensitiveData()`는 객체의 `receiverId`·`secret` byte array를 덮지만 QR raw String, `encode()` 결과 String과 ML Kit가 만든 `barcode.rawValue`를 회수하지 않는다. pairing store·endpoint resolver 시험은 객체 byte array cleanup을 호출하지만 immutable String heap 수명은 검사하지 않는다.
- `PcEndpointResolver`는 실패·경쟁 탈락 candidate의 ByteArray를 정리하고, `MainActivity.withReachablePairedPc()`는 `original`·`recovered` pairing을 `finally`에서 정리한다. `PcReceiverPairing.withHost()`는 두 민감 배열을 `copyOf()`하므로 candidate cleanup이 original/resolved 배열을 alias해 지우는 별도 결함은 정적으로 성립하지 않지만, candidate의 `encode()`가 만든 String lifetime은 추적하지 않는다(`PcEndpointResolver.kt:105-143`, `PcReceiverPairing.kt:33-39,68-71`, `MainActivity.kt:807-839`).
- `PcReceiverPairing.decode()`는 payload와 함께 parse 중 생성된 `receiverId`·`secret`을 예외 경로에서 지우고, `PcPairingStore.save(rawPairing)`도 암호화 저장 실패 시 반환 전 pairing을 지운다. `saveEncrypted()`는 plaintext를 `finally`에서, ciphertext를 preferences `apply()` 뒤에 지우며, `load()`는 ciphertext·IV·복호화 plaintext를 정리한다. 이 cleanup은 immutable raw/encoded/decrypted `String`을 지우지는 못한다(`PcReceiverPairing.kt:80-126`, `PcPairingStore.kt:18-70`).
- pairing object의 현재 production 저장 호출은 성공 시 `savePcPairing()`의 `finally` 또는 recovery의 `withReachablePairedPc()` `finally`에서 민감 배열을 정리한다. `PcPairingStoreInstrumentedTest`·`PcEndpointResolverTest`도 object cleanup을 호출하지만, raw `String` 변수·`Barcode.rawValue`·encoded/decrypted `String`의 수명이나 queued generic lambda discard를 검사하지 않는다(`MainActivity.kt:826-837,2998-3004`, `PcPairingStoreInstrumentedTest.kt:24-47`, `PcEndpointResolverTest.kt:54-70`).

#### 추론

- pairing QR 원문에는 PC receiver를 인증하는 secret이 Base64로 들어 있으므로, pairing scanner·재저장·endpoint recovery가 끝난 뒤에도 String이 GC 전까지 남을 수 있다. same-process heap/로그·crash dump·승인 디버그 메모리 노출 전제에서는 파일 at-rest 보호와 별개로 receiver 인증 secret이 노출될 수 있다.
- 원문 QR 입력을 `CharArray`로 옮겨 wipe하는 방식만으로는 ML Kit/`String`/Base64 encoder가 만든 다른 immutable 복사본을 모두 제거할 수 없다. 실제 heap dump나 ML Kit 내부 수명은 확인하지 않았으므로 P3 후보로 유지한다.
- Activity 종료·executor queue 제거와 pairing save/recovery 실패가 겹치면 일반 lambda·closure가 실행되지 않거나 callback이 폐기되더라도 captured String을 명시적으로 지우지 못한다. 이는 암호화된 SharedPreferences ciphertext의 at-rest 보호를 우회하는 경로가 아니라 transient heap 수명과 lifecycle cleanup의 공백이다.
- `withHost()`의 deep copy와 mutable ByteArray 실패 cleanup은 pairing secret object의 alias·일부 예외 잔류 가능성을 줄이는 반대 근거다. 따라서 이번 재대조에서는 별도 ByteArray alias/zeroize finding을 추가하지 않고, immutable String ownership과 generic queue lifecycle에만 범위를 유지한다.

#### 가정과 미검증

- Android/ML Kit의 `Barcode.rawValue`가 immutable String으로 제공되고 JVM/ART GC가 즉시 회수하지 않는다는 일반 동작을 전제로 했다. 실제 ART heap·crash dump·GC timing은 실행하지 않았다.
- pairing QR은 학생 `MQR1` 토큰과 다른 receiver 인증 payload이며, 운영 문서는 Android Keystore 기반 at-rest 보호와 인증된 사설망 endpoint 복구를 명시한다. `SECURITY.md`의 QR 원문·진단 로그 금지 문구가 pairing transient String의 zeroization까지 직접 규정하는지는 확인하지 못했다.
- 로그나 평문 `SharedPreferences`에 pairing raw String이 의도적으로 저장된다는 사실은 확인하지 않았다. 이 finding은 scanner·decode/save·load/recovery 호출 및 Activity 종료 경계의 transient heap 잔류에 한정한다.

#### 안전한 확인 절차

- `QrImageAnalyzer` raw callback, Kiosk pairing scanner/save 호출, `PcPairingStore`, `PcReceiverPairing`, endpoint recovery 저장 및 관련 pairing/protocol 시험과 보안·운영 문서를 읽기 전용으로 대조했다.
- `PcEndpointResolverTest`, `PcPairingStoreInstrumentedTest`, `PcControlProtocolTest`, `PcTransferProtocolTest`, `SensitiveTaskTest`의 대상과 cleanup 범위를 읽기 전용으로 대조했다.
- 실제 pairing QR 촬영·저장·복구·DB/Keystore runtime·heap dump·Activity 종료 timing·ADB·네트워크·테스트·빌드는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: pairing secret을 immutable String으로 중복 생성하지 않는 byte-oriented decode/save API 또는 최소화된 단기 변환 경계를 사용하고, UI/ML Kit/저장 실패·성공·Activity 종료 모든 경로에서 가능한 mutable buffers와 queued-task owner를 즉시 폐기한다.
- 실제: raw QR String은 scanner→UI callback→executor→store를 통과하고, `encode()`·복호화 decode에서도 secret을 담은 새 String이 생성되며 ByteArray만 지워진다. `onDestroy()`의 generic executor shutdown은 이 pairing closure를 zeroize하지 않는다.

#### 영향 및 재판단 큐

- 보안: 승인 디버그/같은 프로세스 메모리 접근 전제에서 PC receiver 인증 secret의 transient heap 노출 시간이 늘어날 수 있다. PC 설정 파일의 평문 at-rest 노출은 `LUNA-0002`, PC 전송 암호화 buffer cleanup은 `LUNA-0014`로 별도 기록한다.
- Sol·사용자 재판단 후 pairing payload를 ByteArray 기반으로 파싱·저장하는 API, QR scanner가 raw String을 넘기는 unavoidable 경계의 허용 수준, `encode()`·복호화·실패·Activity 종료 경로의 String 생성 최소화와 queued-task cleanup 회귀 확인을 결정한다. 이번 Goal에서는 구현하지 않는다.

#### 기존 테스트가 잡지 못한 이유

- `PcPairingStoreInstrumentedTest`는 Keystore 암호화·round-trip·updated host와 object ByteArray cleanup을, `PcEndpointResolverTest`는 authenticated candidate·original host·object cleanup을, protocol test는 `withHost()` round-trip을 확인한다. 그러나 `rawValue`·encoded pairing String·복호화 중간 String의 heap retention, Activity queue discard 또는 로그/crash dump 노출은 검증하지 않는다. `SensitiveTaskTest`는 별도 helper의 cleanup만 검증하고 pairing lambda에 연결되지 않는다.

### LUNA-0031 — CSV preview/apply executor 종료·거부 시 parsed credential `CharArray` 정리가 보장되지 않음

> **교정 후 판정 — `LUNA-0013`에 병합, P4.** 별도 P3가 아니라 CSV 민감
> 사본 owner·예외 cleanup hardening의 한 실패 seam으로 관리한다.

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 10:35:07 / 2026-08-02 21:43:55 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `MainActivity` CSV PC fetch·preview·apply, `ParsedStudentCsv`/`SensitiveTask` lifecycle

#### 사실

- `ParsedStudentCsv`는 각 `StudentCsvRow`의 username/password를 wipe 가능한 `CharArray`로 보유하고, `clearSensitiveData()`는 그 두 배열만 덮는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/StudentCsvImport.kt:5-22`).
- `fetchStudentCsvFromPc()`는 `pcControlExecutor`와 `ioExecutor`를 각각 단일 스레드 executor로 사용하고, payload를 파싱한 뒤 `parsed`를 캡처한 preview 람다를 일반 `ioExecutor.execute { ... }`로 제출한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:161-162,1750-1812`). 이 제출에는 `SensitiveTask`의 cleanup 또는 rejection 보상 경계가 없다.
- 이 preview 제출은 `pcControlExecutor` 작업 내부에서 수행되므로 바깥 `runCatching { pcControlExecutor.execute { ... } }`가 비동기 내부의 `ioExecutor.execute` rejection을 받지 못한다. `ioExecutor`가 종료·거부되거나 queued preview가 `shutdownNow()`로 반환되면 `parsed`를 지우는 catch/owner가 없다(`MainActivity.kt:1761-1818,3859-3874`).
- `showStudentCsvPreview()`의 적용 버튼도 `applyStudentCsv(parsed)`를 호출하고, `applyStudentCsv()`는 같은 `ParsedStudentCsv`를 일반 `ioExecutor.execute { ... }` 람다에 캡처한다(`MainActivity.kt:1838-1869`).
- Dialog의 `onDismiss`는 `applying == false`인 취소·일반 닫기에서만 rows를 지우고, apply 버튼은 `applying = true`로 ownership을 lambda에 넘긴다. 따라서 apply 제출 거부 또는 queued apply 제거 시 Dialog가 회수할 별도 owner가 없다(`MainActivity.kt:1820-1859`).
- apply positive listener는 `applying = true`를 먼저 설정한 뒤 `ioExecutor.execute`를 호출한다. executor가 이미 종료되어 `RejectedExecutionException`을 던지면 listener 경계에 보상 cleanup이 없고, 이후 Dialog `onDismiss`도 `!applying` 조건을 만족하지 않아 `parsed`를 회수하지 않는다(`MainActivity.kt:1838-1853`).
- `onDestroy()`는 `ioExecutor.shutdownNow()` 뒤 반환된 작업 중 `SensitiveTask`만 `discard()`하고, 일반 CSV preview/apply 람다는 정리하지 않는다(`MainActivity.kt:3859-3874`). 따라서 CSV 람다가 queue에 남은 상태에서 Activity가 종료되면 preview/import 본문과 그 내부의 후속 cleanup이 실행되지 않을 수 있다. shutdown 직전 제출이 거부되어도 두 CSV 호출부에는 `parsed.clearSensitiveData()`를 보장하는 `catch`/owner registry가 없다.
- `StudentRepository.importStudents()`는 정상 본문에 진입하면 `finally`에서 existing username과 rows를 지우지만, queued 작업이 실행되지 않으면 그 `finally`에 도달하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/StudentRepository.kt:307-421`). `previewStudentImport()`는 기존 username 배열만 정리하고 input rows의 소유권은 UI에 남기므로(`:424-473`), preview 작업이 제거된 경우 최종 UI/Activity cleanup에도 의존한다.
- `importStudents()`의 rows cleanup `finally`는 session/empty-row/class validation, active-class 조회, existing student 조회·username decrypt 뒤인 `:332`에서 시작하고, `previewStudentImport()`의 existing username cleanup도 `:449`에서 시작한다. 이 전처리·DB 조회가 실패하면 repository 자체의 rows cleanup은 실행되지 않으며, 정상적으로는 UI failure callback이 `parsed`를 지우지만 Activity 종료·callback 폐기와 겹치면 호출자 owner가 남지 않는다(`StudentRepository.kt:307-473`, `MainActivity.kt:1801-1808,1853-1867`).
- 이미 있는 `executeSensitive()`는 `SensitiveTask` 제출 거부 시 `task.discard()`를 호출하지만(`MainActivity.kt:3877-3887`), CSV preview/apply는 이 helper를 사용하지 않는다. `SensitiveTaskTest`는 queued discard 자체만 검증하며 CSV executor shutdown·rejection 연결은 검증하지 않는다.
- `RepositoryInstrumentedTest.csvImportUpdatesByLoginIdAndTracksOnlyCardsNeedingPrint()`는 정상 preview→import 본문 진입 뒤 rows가 0으로 지워지는 것만 확인한다. Activity 종료·submission rejection·queued lambda discard 경계는 확인하지 않는다.
- `BUILD_VERIFICATION.md`와 `CONTINUOUS_DEVELOPMENT_GOAL.md`는 `SensitiveTask`가 학생 자격정보를 포함한 대기 작업을 정상·예외·queue discard·submission rejection에서 한 번만 정리하는 계약을 기록하지만, 현재 `MainActivity`의 `executeSensitive()` 사용 목록에는 CSV preview/apply가 없다(`docs/BUILD_VERIFICATION.md:2063-2084`, `docs/CONTINUOUS_DEVELOPMENT_GOAL.md:621-630`, `MainActivity.kt:716-724,1345-1354,1643-1652,2240-2248,2346-2354,3364-3372,3623-3631`).
- `MainActivityInstrumentedTest`에는 CSV fetch/preview/apply lifecycle 시험을 찾지 못했고, `StudentCsvParserTest`는 정상 parse cleanup과 duplicate 예외만, `RepositoryInstrumentedTest`는 직접 repository 정상 round-trip만 확인한다(`StudentCsvParserTest.kt:8-45`, `RepositoryInstrumentedTest.kt:493-529`).

#### 추론

- Activity 재생성·종료가 CSV parse와 preview/apply 제출 또는 queue 대기와 교차하거나 executor가 포화·종료된 경우, username/password `CharArray`가 명시적으로 zeroize되지 않고 GC 시점까지 남을 수 있다. 정적 코드와 lifecycle 순서만 확인했으므로 P3 후보로 유지한다.
- 구체적으로 fetch 후 parse가 끝난 직후 Activity가 destroy되면 inner preview 제출이 거부될 수 있고, preview가 queue에 들어간 뒤 destroy되면 generic Runnable이 `shutdownNow()` 반환 목록에 남는다. apply에서는 `applying=true`가 UI cleanup을 먼저 차단하므로 같은 lifecycle fault가 더 직접적이다.
- 이는 입력 CSV 전체와 field immutable `String`이 정상 parser 경로마다 회수 불가능한 `LUNA-0013`과 달리, 원래 wipe 가능한 parsed row buffer의 queued-task/owner cleanup 누락이다. 기존 DB username 복호화 중간 예외인 `LUNA-0004`와도 실패 지점이 다르다.
- 정상 preview failure·일반 취소·repository import 본문 진입에서는 명시적 cleanup이 있으므로 모든 CSV 경로가 항상 잔류한다고 과장하지 않았다. 이 후보는 preview/apply가 Activity executor에 제출된 뒤 rejection·queued shutdown·callback 폐기와 교차하는 경계에 한정한다.
- 역사적 `SensitiveTask` 도입은 동일한 대기열 문제를 PIN·학생 credential 입력·QR hash·PDF bitmap에 해결했지만 CSV가 적용 목록에서 빠져 있다. 이는 의도된 보안 계약과 현재 호출 graph의 불일치를 뒷받침하지만, 실제 heap 잔류나 사용자 데이터 노출을 입증하는 runtime 증거는 아니다.

#### 가정과 미검증

- 실제 Activity 종료·재생성 timing, `ThreadPoolExecutor` queue 제거, `RejectedExecutionException`, Android main-thread callback 미전달, repository precondition/DB fault와 ART heap 생존 시간은 실행하지 않았다.
- 정상 preview 취소·preview failure·정상 import 시작 경로에서는 UI 또는 repository의 명시적 row wipe가 존재한다. 이 finding은 종료·제출 거부·queued task discard 경계에 한정한다.
- parser가 반환 전 partial row를 잃는 문제는 `LUNA-0032`에서 별도로 다루며, 이 항목의 정적 근거는 `ParsedStudentCsv` 반환 이후 소유권 경계다.
- CSV 원문·학생 계정·DB·A 기기 상태는 사용하지 않았다.

#### 안전한 확인 절차

- `MainActivity`의 CSV fetch/preview/apply 제출부와 `onDestroy()`/`executeSensitive()`, `SensitiveTask`, `StudentCsvImport`, `StudentRepository`의 cleanup 경계를 읽기 전용으로 대조했다.
- `SensitiveTaskTest`·`StudentCsvParserTest`·`RepositoryInstrumentedTest`의 CSV 시험은 실행하지 않고, 테스트가 다루는 정상 cleanup 범위만 읽었다. `BUILD_VERIFICATION.md`·`CONTINUOUS_DEVELOPMENT_GOAL.md`의 `SensitiveTask` 도입 계약이 CSV preview/apply 호출부까지 확장됐는지도 대조했다.
- `MainActivity` executor 초기화·CSV call graph, `StudentRepository`의 precondition/try-finally 경계, root `SECURITY.md`의 자격정보·QR 원문 저장 금지 정책과 관련 Git history(`bfbfaf4`)를 추가로 읽기 전용 대조했다.

#### 기대 결과와 실제 결과

- 기대: parsed credential buffer의 owner가 Activity lifecycle과 executor queue를 함께 추적하고, submission rejection·queued shutdown·본문 예외·preview cancel/apply failure 모든 경로에서 정확히 한 번 wipe된다.
- 실제: CSV preview/apply는 일반 람다로 제출되어 `SensitiveTask` discard 계약 밖에 있고, 두 단계 executor의 inner rejection은 outer catch 밖에 있다. `onDestroy()`는 반환된 일반 CSV 작업을 처리하지 않으며, repository의 precondition/DB 전처리 실패도 자체 rows cleanup보다 앞설 수 있다. 정상 본문·preview failure/cancel callback이 전달된 경우에만 repository/UI cleanup이 작동한다.

#### 영향

- 보안/개인정보: 동일 Kiosk 프로세스의 승인된 heap inspection·crash/diagnostic 상황에서 CSV 입력 자격정보의 mutable row buffer 잔류 시간이 늘어날 수 있다.
- 데이터: 이번 정적 검토에서 DB 부분 적용·학생 데이터 손상은 확인하지 않았다. queued preview 작업 제거는 적용 전 검토 상태와 cleanup ownership의 문제다.
- 운영: Activity 재생성 또는 종료 직전 CSV 가져오기를 시작하면 버튼 상태·preview 표시와 무관하게 민감 buffer 회수가 GC에 의존할 수 있다.

#### 근본 원인 후보

- 민감 입력을 다루는 `executeSensitive()`/`SensitiveTask` 패턴이 일부 관리자 작업에만 적용되고, CSV preview/apply가 별도 일반 executor 람다로 구현되어 Activity shutdown 시 작업별 cleanup ownership이 분산된 것으로 보인다.
- `SensitiveTask` 적용 목록은 PIN·학생 credential 입력·QR hash·PDF bitmap으로 한정된 채 CSV parse 결과에는 연결되지 않았고, preview UI ownership과 apply executor ownership 전환이 별도 람다·Dialog 상태로 분리된 것으로 보인다.

#### 반대 가설·오탐 검토

- 정상 import 본문에서는 `StudentRepository.importStudents()`의 `finally`가 rows를 지우고 preview 성공·실패·취소에는 UI clear 경계가 있으므로 모든 CSV 경로가 항상 누수된다고 과장하지 않았다.
- `LUNA-0013`은 parser가 만드는 immutable String copies의 정상 경로 수명, `LUNA-0004`는 기존 username decrypt map의 예외 cleanup이다. `LUNA-0031`은 Activity executor queue/rejection에서 mutable parsed rows의 owner가 사라지는 별도 경계다.

#### 기존 테스트가 잡지 못한 이유

- `SensitiveTaskTest`는 임의 `SensitiveTask`의 discard만 검증하고, CSV 람다가 직접 executor에 제출되는지와 `shutdownNow()` 반환 목록의 cleanup을 연결하지 않는다. `StudentCsvParserTest`와 `RepositoryInstrumentedTest`는 정상 parse/import 본문 cleanup만 확인하며 Activity 종료·submission rejection·queued preview/apply discard를 재현하지 않는다.
- `MainActivityInstrumentedTest`에는 CSV lifecycle fault seam이 없고, historical build record의 `SensitiveTask` 통과는 해당 helper의 generic 계약을 증명할 뿐 현재 CSV call site 연결을 증명하지 않는다. root `SECURITY.md`의 저장 금지 정책도 runtime heap zeroization을 직접 보장하지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 CSV preview/apply를 cleanup callback이 있는 `SensitiveTask`로 제출하거나, Activity가 pending parsed owner를 등록해 `onDestroy()`와 submission rejection에서 공통으로 정리하도록 한다. preview에서 UI로 ownership을 넘기는 시점과 apply 시작·취소·중복 callback의 idempotent cleanup을 명시해야 한다.
- queue shutdown/rejection, Activity recreation 중 parse/preview/apply, preview 취소·실패, import 본문 예외와 정상 적용에서 username/password 배열이 정확히 한 번 지워지는 단위·계측시험을 추가해야 한다. 이번 Goal에서는 source·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0031`은 `LUNA-0013`의 immutable parser representation, `LUNA-0004`의 decrypt map 예외, `LUNA-0014`의 PC 전송 ByteArray cleanup과 데이터·owner·실패 경계가 다르다.
- CSV parsed row lifecycle와 `SensitiveTask` 적용 범위는 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0032 — CSV parser 후속 행 검증 실패 시 앞서 생성된 row credential `CharArray`가 정리되지 않음

> **교정 후 판정 — `LUNA-0013`에 병합, P4.** 별도 P3가 아니라 CSV 민감
> 사본 owner·예외 cleanup hardening의 한 실패 seam으로 관리한다.

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 10:42:00 / 2026-08-02 21:50:07 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `StudentCsvParser.parse()`·`StudentCsvRow`와 PC CSV import parse-failure 호출부

#### 사실

- `StudentCsvParser.parse()`는 `records.drop(1).mapIndexed { ... }`로 행을 순회하고, 각 검증을 통과한 행마다 `username.toCharArray()`·`password.toCharArray()`를 담은 `StudentCsvRow`를 즉시 생성한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/StudentCsvImport.kt:54-87`).
- 누적 `rows`를 보유한 mutable owner에 대한 `try/finally` 또는 예외 시 `rows.forEach(StudentCsvRow::clearSensitiveData)`가 없다. 따라서 후속 행의 열 수·이름·아이디·비밀번호·반·중복 아이디 검증이 실패하면 앞서 생성된 rows가 `ParsedStudentCsv`로 감싸져 반환되기 전에 예외가 전파된다.
- `StudentCsvRow` 생성은 해당 row의 모든 검증과 `usernames.add(username)` 뒤인 `:81-86`에 있지만, `ParsedStudentCsv(rows)`는 `mapIndexed`가 끝난 뒤 `:88`에서만 실행된다. 첫 row가 정상 생성된 뒤 다음 row에서 `require` 또는 중복 검증이 실패하면 partial destination list는 호출자에게 전달되지 않는다.
- `parseRecords(text)`와 `require(!quoted)`는 `mapIndexed`보다 먼저 완료되므로 닫히지 않은 quote·record parse 오류만으로는 `StudentCsvRow`가 생성되지 않는다. 또한 실패한 현재 row는 validation·`usernames.add()` 뒤에 constructor가 호출되므로, partial ownership 위험은 이미 생성된 앞선 rows에 한정된다(`StudentCsvImport.kt:45-55,78-88,91-128`).
- 현재 `StudentCsvParserTest`의 duplicate username fixture는 첫 번째 행을 정상 생성한 뒤 두 번째 행의 `usernames.add(username)`에서 실패한다(`kiosk/src/test/java/com/local/matholickiosk/kiosk/StudentCsvParserTest.kt:29-38`). 이 테스트는 예외만 기대하며 이미 생성된 첫 행의 배열 cleanup을 확인하지 않는다.
- `MainActivity.fetchStudentCsvFromPc()`의 parser `catch`는 `download.payload.fill(0)`과 오류 UI만 수행하고, parse가 실패해 `ParsedStudentCsv`가 없는 경우 부분 rows를 받을 참조가 없다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:1779-1789`). `ParsedStudentCsv.clearSensitiveData()`는 반환된 객체에 대해서만 호출할 수 있다(`StudentCsvImport.kt:17-22`).
- production 호출 graph에는 `StudentCsvParser.parse(download.payload)`가 `MainActivity`에 한 곳만 있고, 시험만 parser를 직접 호출한다. 따라서 현재 production catch에 partial-row cleanup을 위임할 다른 호출부나 공통 parser owner는 확인되지 않는다(`MainActivity.kt:1780`, `StudentCsvParserTest.kt:11,31,42`).
- parser의 `text`·record·field immutable String 수명은 `LUNA-0013`, 성공적으로 반환된 parsed rows가 Activity executor에서 버려지는 경계는 `LUNA-0031`에 기록되어 있다. 이번 항목은 parser 내부에서 반환 전 mutable row가 부분 생성된 뒤 예외로 소유권이 사라지는 경계다.
- parser 구현 줄 전체는 `0193d898` 도입 commit에 blame되고 후속 parser cleanup 변경은 확인되지 않았다. 현장 checklist·RC47 기록은 정상 CSV 적용·존재하지 않는 반 거부·민감정보 비노출을 확인하지만 partial row `CharArray` cleanup을 관찰하지 않는다(`git blame -L 38,90 StudentCsvImport.kt`, `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md:143-162`, `docs/BUILD_VERIFICATION.md:6010-6014`).

#### 추론

- CSV에 앞선 정상 행이 하나 이상 있고 후속 행이 malformed·duplicate인 경우, 앞선 행의 username/password `CharArray`는 명시적으로 zeroize되지 않고 GC 시점까지 남을 수 있다. 실제 ART heap 생존 시간은 측정하지 않았으므로 P3 후보로 유지한다.
- 사용자에게는 CSV 전체가 거부되어 DB 적용은 일어나지 않지만, 실패한 입력의 일부 credential buffer가 성공 경로의 row cleanup 계약을 우회한다.
- header·빈 파일·행 수·닫히지 않은 quote처럼 row 생성 전에 실패하는 경로에는 이 partial-row 조건이 없다. 이 항목은 적어도 한 row가 생성된 뒤 후속 validation/duplicate에서 실패하는 입력으로 제한한다.

#### 가정과 미검증

- 예외가 첫 행 이전이 아니라 적어도 한 행의 `StudentCsvRow` 생성 이후 발생하는 입력을 전제로 했다. duplicate username과 후속 행 검증 순서는 소스·시험 fixture로 확인했다.
- 실제 parser 실행·heap/GC 관찰·CSV 원문 사용은 하지 않았다. `String` intermediate의 수명은 `LUNA-0013` 범위로 분리했고, quote failure가 partial row를 만든다는 가정은 채택하지 않았다.
- `StudentCsvParserTest`의 duplicate fixture는 정적 순서 근거로만 사용했으며, 실제 배열 내용·heap 생존·GC를 시험 실행으로 확인하지 않았다.

#### 안전한 확인 절차

- `StudentCsvImport.kt` 전체, `MainActivity` parse catch/finally, parser 시험의 정상·duplicate·header 실패 경계를 읽기 전용으로 대조했다.
- parser 호출 graph와 `LUNA-0031`/`LUNA-0013`의 ownership 경계도 대조했다. 테스트·빌드·실제 CSV·학생 DB·A 상태는 사용하지 않았다.

#### 기대 결과와 실제 결과

- 기대: parser가 만든 partial rows의 owner가 parse 성공·실패 모두에 남아, validation/duplicate/quoting 예외에서 생성된 모든 credential 배열을 한 번씩 wipe한다.
- 실제: Kotlin `mapIndexed`가 만든 partial rows는 예외 시 반환되지 않고, 호출부에는 그 rows를 정리할 handle이 없어 `download.payload` ByteArray만 지워진다. 정상 parse 성공 때만 `ParsedStudentCsv.clearSensitiveData()`가 호출 가능한 owner가 생긴다.

#### 영향

- 보안/개인정보: malformed CSV 또는 중복 아이디 입력을 처리하는 정상 거부 경로에서 Kiosk heap에 credential CharArray가 잔류할 수 있다.
- 데이터/운영: DB import는 시작되지 않으며 학생·반 데이터 변경은 확인되지 않았다. 영향은 실패 입력의 메모리 cleanup으로 한정한다.

#### 근본 원인 후보

- parser가 `mapIndexed`의 반환 리스트를 결과 owner로만 사용하고, 결과가 만들어지기 전의 partial row ownership을 별도로 추적하지 않는 구조로 보인다.
- parser caller가 반환 성공/실패를 `ParsedStudentCsv` 단위로만 다루고, 실패 시 accumulator를 되돌려 받는 예외 contract가 없는 점이 호출부 cleanup 공백을 고정한다.

#### 반대 가설·오탐 검토

- 정상 parse는 `ParsedStudentCsv`가 반환되어 UI/repository가 rows를 지울 수 있으므로 모든 CSV 입력이 누수된다고 확대하지 않았다.
- `LUNA-0013`은 `String(payload)`·field·username set의 immutable 복사, `LUNA-0031`은 반환 후 executor queue/lifecycle discard다. `LUNA-0032`는 parse validation 예외가 반환 전 row owner를 잃게 하는 별도 경계다.

#### 기존 테스트가 잡지 못한 이유

- duplicate/header 시험은 예외 종류만 검증하고 partial row 배열이 존재하는 후속 실패 경로의 zeroize를 관찰하지 않는다. parser API가 실패 시 `ParsedStudentCsv`를 반환하지 않아 호출부 계측도 어렵다.
- 정상 parse 시험은 반환된 `ParsedStudentCsv`를 finally에서 지우지만, duplicate 시험은 첫 row 생성 뒤 예외를 기대할 뿐 그 partial row cleanup을 검증하지 않는다. repository/Activity 시험도 parser failure 후 row owner를 관찰하지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 mutable accumulator를 명시적으로 소유해 `try/catch/finally`에서 partial rows를 wipe하거나, parser가 실패 시 cleanup 가능한 result/exception contract를 제공한다. CSV quoting·행 검증·중복 검출의 현재 의미는 유지해야 한다.
- 첫 행 이후 후속 중복·열 수 오류·이름/아이디/비밀번호/반 길이 오류와 1~1000행 경계에서 생성된 모든 앞선 row 배열이 한 번씩 지워지는 회귀시험을 추가해야 한다. 닫히지 않은 quote는 row 생성 전 실패의 negative control로 별도 확인해야 한다. 이번 Goal에서는 source·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0032`는 `LUNA-0013`·`LUNA-0031`과 CSV credential memory 계열이지만, immutable intermediate·executor lifecycle이 아니라 parser partial-construction ownership에 한정한다.
- `LUNA-0032`는 `LUNA-0013`·`LUNA-0031`과 CSV credential memory 계열이지만, immutable intermediate·executor lifecycle이 아니라 parser partial-construction ownership에 한정한다. 정상 반환 뒤 queue에서 버려지는 rows는 `LUNA-0031`로, parser 내부에서 반환되지 않는 rows는 이 항목으로 분리한다.
- parser 실패 시 partial rows cleanup contract는 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0033 — Kiosk PC 상태 보고 단일 executor 무제한 queue가 stale 상태·학생 표시명을 보유하고 CSV 제어를 지연시킬 수 있음

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 10:57:22 / 2026-08-02 21:55:32 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `MainActivity` PC 상태 보고·CSV fetch·자가진단과 `PcControlClient` endpoint recovery

#### 사실

- `MainActivity`는 `Executors.newSingleThreadExecutor()`로 `pcControlExecutor`를 만들고 별도 queue 상한을 두지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:162`).
- `Executors.newSingleThreadExecutor()`의 표준 구현은 무제한 `LinkedBlockingQueue`를 사용하므로, 호출부에 별도 상한이 없는 현재 구조는 상태 보고 제출량에 대한 bounded budget을 갖지 않는다. `reportPcStatus()`는 상태 변경마다 새 lambda를 executor에 제출하며, 최신 상태로 대체하는 coalescing·세대 검사·취소·bounded queue가 없다(`MainActivity.kt:785-805`). 제출된 lambda는 `state`, `studentName`, `notify`를 캡처하므로 대기 중인 작업이 학생 표시명 `String`을 계속 보유할 수 있다.
- `withReachablePairedPc()`는 저장된 pairing endpoint를 먼저 시도하고 실패하면 후보 host discovery/resolver 뒤 재시도한다(`MainActivity.kt:807-839`). `PcControlClient`의 기본 connect/read timeout은 각각 2,000ms/4,000ms이고(`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcControlClient.kt:11-13`), `PcEndpointResolver`의 전체 timeout은 6,000ms, 고정 병렬도는 32다(`PcEndpointResolver.kt:91-138`).
- 같은 `pcControlExecutor`가 CSV fetch(`MainActivity.kt:1761-1791`)와 operational self-test(`MainActivity.kt:2607-2618`)에도 사용된다. 상태 보고는 관리자 인증/화면, QR 대기·학생 확인·로그인 중, 채점 완료·복구 필요와 같은 여러 경로에서 호출된다(`MainActivity.kt:274-298`, `:688-692`, `:782`, `:3035-3060`, `:3388-3392`, `:3422-3426`, `:3548-3552`, `:3573-3579`).
- `activeStudentDisplayName`은 QR 학생 확인·로그인 중·결과/실패 status에 전달되고(`MainActivity.kt:210`, `:274-298`, `:3383-3391`, `:3421-3425`, `:3548-3551`), 성공 시 `reportPcStatus()` 제출 뒤 null로 바뀐다. 그러나 제출된 lambda는 이미 받은 `studentName` 매개변수를 계속 보유한다. 실패 경로의 `showAuthentication()`, `onStop()`, `onDestroy()`에는 이 필드를 공통으로 지우는 호출이 없고, `showScanner()`·취소 경로에서만 명시적으로 null로 만든다(`MainActivity.kt:3048`, `:3576`). 따라서 stale bridge/status callback이 후속 status를 예약하면 이전 표시명 snapshot을 다시 사용할 수 있는 수명 경계가 남는다. 실제 callback 순서는 미검증이다.
- `onDestroy()`는 `pcControlExecutor.shutdownNow()`를 호출하지만 반환된 대기 `Runnable`을 `SensitiveTask`와 같은 명시적 폐기/정리 계약으로 처리하거나 실행 중 status task를 destroyed 세대로 무효화하지 않는다(`MainActivity.kt:3859-3874`). status task에 캡처된 immutable `String`의 wipe 계약도 없다. 다만 `shutdownNow()` 자체는 대기 queue를 반환·drain하는 API이므로 종료 뒤 executor queue가 계속 보유된다고 단정하지 않는다.

#### 추론

- PC가 오프라인이거나 endpoint recovery가 timeout에 가까운 동안 관리자/QR/session 상태 전이가 반복되면 best-effort status task가 계속 쌓여, 제출 순서(FIFO)가 유지되더라도 실행 시점에는 stale 상태가 나중에 전달될 수 있다. 원래 status 요청의 connect/read timeout 뒤 resolver 전체 timeout과 재시도까지 같은 worker가 점유할 수 있어, 같은 단일 worker 뒤의 CSV fetch와 self-test가 status backlog에 의해 지연될 수 있다.
- 대기 task가 많아질수록 학생 표시명이 실행·폐기·GC될 때까지 queue의 lambda에 남을 수 있다. 실제 heap 생존 시간과 queue 성장량은 확인하지 않았으므로 P3 후보로 유지한다.
- failure 경로에서 Activity field 자체가 즉시 지워지지 않고, 이미 제출된 lambda도 별도 세대 취소를 받지 않으므로 이전 학생 표시명과 현재 상태가 서로 다른 시점에 PC로 전송될 가능성이 있다. 실제 stale callback·PC 수신 순서·이름 노출은 실행하지 않았다.

#### 가정과 미검증

- 문제 조건은 PC unreachable/recovery delay와 반복 상태 보고가 겹치는 경우다. 정상적으로 PC가 즉시 응답하면 single-thread queue가 빠르게 소진될 수 있다.
- Android executor의 실제 queue 길이·각 task 처리 시간·FIFO drain 속도·Activity 종료 시 반환 Runnable과 captured name의 GC 시점, stale `activeStudentDisplayName` 재사용과 CSV/self-test 사용자 체감 지연은 측정하지 않았다.
- 상태 전송 실패는 `runCatching`으로 UI crash로 전파되지 않는다는 점은 확인했지만, 그것이 backlog·stale ordering·메모리 보유를 제거한다는 근거로 사용하지 않았다.
- `PcEndpointResolver`는 후보별 `PcReceiverPairing`을 실패·경쟁 탈락 시 지우고, 전체 timeout 뒤 future 취소·executor 종료를 수행한다. 이는 resolver 내부 자원 정리의 반대 근거이며, status queue 자체의 상한·coalescing을 제공하지는 않는다.

#### 안전한 확인 절차

- `MainActivity`의 executor 생성·status 제출·paired PC resolver·CSV/self-test 제출·`onDestroy()`와 `PcControlClient`/`PcEndpointResolver` timeout을 읽기 전용으로 대조했다.
- `kiosk/src/test`·`kiosk/src/androidTest`에서 `reportPcStatus`, `pcControlExecutor`, `fetchStudentCsvFromPc`, `runPcSelfTest`, `sendStatus` 연결을 검색했으나 일치 시험을 찾지 못했다. `PRODUCT_DECISIONS.md`·`BUILD_VERIFICATION.md`·`RELEASE_OPERATIONS.md`의 PC 실시간 상태·운영 자가진단·정상 DHCP 복구 기록은 읽었지만 offline 반복·queue backlog·CSV/self-test starvation·Activity shutdown cleanup 검증은 찾지 못했다.
- 실제 PC 오프라인·네트워크 discovery·status 반복·CSV fetch·자가진단·heap/queue 계측은 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 상태 알림은 최신 값 중심으로 제한되고 기능성 CSV/self-test 제어와 worker를 분리하거나 공정성을 보장하며, 오래된 task와 민감 표시명이 lifecycle 종료 전에 폐기된다.
- 실제: 각 상태 보고가 무제한 단일 executor에 독립 task로 들어가고 기능성 제어와 worker를 공유한다. `reportPcStatus()`는 destroyed/state generation을 확인하지 않으며, `shutdownNow()`가 반환한 작업에 explicit cleanup callback을 적용하지 않는다. 최신 상태 대체·취소·queue 상한·captured name cleanup 계약은 정적으로 확인되지 않았다.

#### 영향

- 운영/데이터: PC가 unreachable인 동안 stale 상태가 늦게 반영되거나 CSV fetch/self-test가 지연될 수 있다. 상태 보고 실패 자체가 학생/DB 데이터를 변경한다는 근거는 확인하지 않았다.
- 보안/개인정보: 대기 lambda와 Activity field가 학생 표시명 `String` snapshot을 보유할 수 있으나, 실제 heap 잔류 기간·stale 재전송·외부 로그/수신기 노출은 확인되지 않았다.
- 영향은 Kiosk outbound queue의 순서·자원·수명 경계이며, PC receiver inbound resource exhaustion과는 다르다.

#### 근본 원인 후보

- 상태 알림과 기능성 PC 제어를 하나의 무제한 single-thread executor에 같은 우선순위로 넣고, 상태를 latest-state stream으로 모델링하지 않은 구조로 보인다.
- lifecycle 종료 시 shutdown 신호만 보내고 이미 제출된 status task의 의미적 폐기·captured data cleanup·`activeStudentDisplayName` invalidation을 별도 계약으로 두지 않은 것으로 보인다.

#### 반대 가설·오탐 검토

- `LUNA-0007`은 PC receiver의 인증 전 inbound thread/socket과 event queue 자원 고갈이고, 이번 항목은 Kiosk의 outbound executor backlog다.
- `LUNA-0003`은 PC CSV pending queue가 response/send 실패에서 소실되는 내구성 문제이며, `LUNA-0031`은 parsed row의 executor shutdown/rejection owner 문제다. 이번 항목은 학생명을 캡처한 status task가 기능성 PC 제어를 지연시키는 별도 queue 경계다.
- `PcEndpointResolver`의 candidate cleanup/future cancellation, `PcControlClient`의 payload·frame/request cleanup, `runCatching`, 정상 reachable PC의 빠른 drain과 정상 운영 문서 기록, `shutdownNow()`의 queue drain semantics를 확인했으므로 즉시 crash·항상 발생하는 영구 누수·데이터 손상으로 확대하지 않았다. 다만 이 반대 근거는 offline 반복·queue starvation·실제 GC 수명을 검증하지 않는다.

#### 기존 테스트가 잡지 못한 이유

- 기존 테스트는 PC protocol 정상/실패 응답과 CSV 처리·receiver 동작을 주로 확인하지만, Kiosk `pcControlExecutor`의 offline 반복 제출·queue 상한·latest coalescing·CSV/self-test starvation·Activity shutdown 시 queued task 폐기를 계측하지 않는다. 이번 검색에서도 Kiosk 시험의 `reportPcStatus()`·`fetchStudentCsvFromPc()`·`runPcSelfTest()`·`pcControlExecutor`·`sendStatus` 연결은 없었고, `pc_receiver/tests/test_protocol.py`·`test_server.py`의 status/CSV 검사는 receiver를 동기적으로 호출한다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 best-effort status channel을 CSV/self-test 같은 기능성 PC control channel과 분리하거나, bounded/latest-state coalescing·generation cancellation을 도입하고 stale task가 학생명을 오래 보유하지 않도록 한다. 재연결·알림 순서와 실패 UI의 의미를 먼저 정의해야 한다.
- 기능성 제어 starvation을 막는 대신 status 최신성·재연결 순서·알림 누락이 바뀔 수 있으므로 회귀 위험이 있다. PC unreachable 반복 상태, recovery success/failure, CSV/self-test 동시 제출, stale `activeStudentDisplayName`, Activity recreation/destroy, queue 상한·captured data cleanup을 회귀시험으로 검증해야 한다. 이번 Goal에서는 source·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0033`은 `LUNA-0007`·`LUNA-0003`·`LUNA-0031`과 인접하지만 inbound receiver 자원, pending CSV 내구성, parsed-row owner와 각각 다른 경계로 별도 P3 후보를 유지한다.
- Kiosk outbound status queue의 bounded/coalescing·기능성 control 분리 정책은 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0034 — 초기 PC pairing QR host가 지정 사설망·수신기 identity로 검증되지 않음

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 10:57:22 / 2026-08-02 22:04:57 (Asia/Seoul)
- 영향 모듈·버전: Kiosk 초기 PC pairing QR decode/save와 `PcControlClient`/`PcPdfSender` endpoint routing

#### 사실

- 운영 문서는 지정 PC와 같은 private Wi-Fi에서 PC receiver가 QR을 표시하고 관리자가 Kiosk에서 스캔하도록 설명하며, QR에는 secret이 포함되고 운영자는 PC display name을 확인하도록 한다(`docs/RELEASE_OPERATIONS.md:189-219`). Threat model은 DHCP recovery 후보를 현재 Wi-Fi RFC1918 `/24`와 authenticated probe로 제한한다고 적지만, 이는 초기 pairing이 아니라 recovery 경계다(`docs/THREAT_MODEL.md:33`).
- Kiosk `handleRawQr()`는 raw value가 `PcReceiverPairing.PREFIX`로 시작하는지만 확인한 뒤 analyzer를 끄고 `savePcPairing(rawValue)`를 호출한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:2975-2992`). `savePcPairing()`은 network probe·challenge 없이 raw QR을 `pcPairingStore.save(rawValue)`로 decode·암호화 저장하고 display name만 UI에 돌려준다(`MainActivity.kt:2995-3022`, `PcPairingStore.kt:18-31`).
- `PcReceiverPairing.decode()`는 prefix·whitespace·Base64 형태/길이·printable ASCII host·display name 등의 형식만 검사한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcReceiverPairing.kt:80-120`). 생성자 host 검사는 비공백·255자 이하에 머물며(`PcReceiverPairing.kt:14-22`), IPv4 RFC1918/private·loopback·public host·DNS identity 검증은 정적으로 보이지 않는다. 관련 decode/save 줄은 `474132b` 초기 pairing feature에 남아 있고 후속 host-policy 보강 history는 확인되지 않았다.
- Python receiver의 `Pairing.__post_init__`도 host nonempty/길이만 검사하고, `decode_pairing()`은 ASCII host를 받아들인다(`pc_receiver/src/matholic_pdf_receiver/protocol.py:46-65,139-166`). `ConfigStore.load()`의 `pairing(host="127.0.0.1")`는 config field 형식 확인용일 뿐 초기 QR host 정책이나 receiver identity pinning이 아니다.
- PC app의 `current_lan_ipv4()`는 UDP route probe 뒤 `127.*`만 거부하고 `is_private`·interface allowlist·A와의 동일 Wi-Fi 확인은 하지 않는다(`pc_receiver/src/matholic_pdf_receiver/config.py:28-37`). `ReceiverApplication.__init__()`가 얻은 주소를 `self.host`에 한 번 저장하고 `_build_window()`가 그 주소·port·display name과 config의 receiver ID/secret으로 QR을 한 번 만든다(`app.py:34-49,61-63,99-114`). 이후 DHCP/interface 변경 시 QR host를 재평가하는 경로는 확인되지 않았다.
- Kiosk `PcControlClient`와 `PcPdfSender`는 `InetSocketAddress(pairing.host, pairing.port)`로 직접 연결한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcControlClient.kt:85-90`, `PcPdfSender.kt:23-28`). status/CSV/self-test는 첫 direct attempt 뒤 `withReachablePairedPc()` recovery를 사용할 수 있지만, `preparePcPdfTransfer()`는 저장 pairing을 직접 load해 `pcPdfSender.send()`를 호출한다(`MainActivity.kt:2922-2953` 및 PDF 전송 경로). `PcSubnetCandidates.samePrivateSubnet()`·`PcEndpointResolver`의 private subnet 제한은 저장된 초기 endpoint가 실패한 뒤 recovery 후보에만 적용된다.
- 전송 protocol은 frame header의 receiver ID와 secret에서 파생한 AES-GCM/HMAC으로 상대를 인증한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcControlProtocol.kt:151-218`, `PcTransferProtocol.kt:57-129`, `pc_receiver/src/matholic_pdf_receiver/protocol.py:233-268,396-436`). receiver ID·secret은 PC config에서 `os.urandom()`으로 생성되어 QR에 함께 실리며, Kiosk에 기대 receiver ID/certificate의 별도 trust anchor는 없다. 따라서 이 키 material 자체가 초기 QR에서 self-assert되어 들어오므로, QR을 가짜 receiver가 생성해 함께 제시하는 경우의 out-of-band identity 증명은 아니다.
- pairing QR의 display name은 QR에서 가져와 paired 상태로 표시되지만, 초기 host가 지정 PC라는 out-of-band identity attestation은 확인되지 않았다.

#### 추론

- 관리자 PIN으로 PC pairing mode를 연 뒤 악성·가짜 QR을 승인하면 QR이 self-assert한 host/port/secret과 display name이 저장되고, 이후 Kiosk 상태 보고·CSV fetch·QR 카드 PDF가 그 임의 endpoint로 향할 수 있다. 해당 endpoint를 통제하는 수신자는 가짜 pairing에 포함된 secret으로 전송 내용을 복호화할 수 있다. PDF 경로는 현재 저장 endpoint를 직접 사용하므로 status/CSV recovery의 private-subnet 제한이 이 경로를 보완하지 않는다.
- `current_lan_ipv4()`가 route 선택 결과를 private/interface 정책 없이 QR에 넣는 것도 같은 운영 전제와 어긋날 수 있다. 다만 악성 QR·운영자 신뢰 실패 또는 비의도 route가 전제되고, 실제 외부 연결·주소 선택은 실행하지 않았으므로 P3 후보로 유지한다.

#### 가정과 미검증

- 제품이 모든 배포 환경에서 RFC1918 private IPv4만 허용한다는 전제는 문서의 같은 private Wi-Fi 설명에서 추론했지만, 실제 지원 네트워크가 고정 RFC1918인지 사용자/Sol 결정은 확인하지 않았다.
- physical QR scan에서 운영자가 display name을 대조하는 trust-on-first-use 절차를 실제로 수행하지 않았고, fake QR·public/DNS/loopback/multicast host의 runtime 동작도 확인하지 않았다.
- PC receiver가 시작된 뒤 네트워크 interface/주소가 바뀌거나 route probe가 VPN·가상·비-RFC1918 주소를 선택하는 경우의 QR 재생성·복구 순서는 실행하지 않았다. 현재 `current_lan_ipv4()`의 실제 반환 주소와 Windows firewall/profile 상태도 확인하지 않았다. 기존 pairing의 recovery가 실제 private subnet에서 성공한다는 문서 기록은 초기 QR host policy의 증명이 아니다.
- 실제 status 전송·학생 데이터·QR 카드 PDF·네트워크 연결·secret 값은 사용하거나 기록하지 않았다.

#### 안전한 확인 절차

- release/threat 문서, Kiosk pairing decode/save/connect 경로, Python pairing encode/decode·host validation, private subnet recovery resolver와 pairing 관련 tests를 읽기 전용으로 대조했다.
- 기존 protocol/pairing vector의 사설 IPv4 fixture와 recovery candidate 테스트가 초기 pairing host policy를 검증하지 않는 것을 확인했고, `git blame`에서 host validation·QR 생성·Kiosk save 줄이 각각 `0b0df69`/`474132b` 초기 구현에 남아 있는 것을 대조했다.
- `PcControlProtocolTest`·`PcTransferProtocolTest`의 fixed vector는 receiver ID/secret으로 암호화와 응답 binding을 검증하지만 host policy나 initial operator identity를 검사하지 않고, `PcPairingStoreInstrumentedTest`도 private host round-trip/Keystore 저장만 확인한다(`kiosk/src/test/.../PcControlProtocolTest.kt:11-88`, `PcTransferProtocolTest.kt:10-103`, `kiosk/src/androidTest/.../PcPairingStoreInstrumentedTest.kt:13-49`).
- QR 캡처·원문/hash·실제 endpoint 연결·네트워크 변경·pairing 저장 변경·빌드/테스트는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 초기 pairing도 제품이 약속한 private Wi-Fi와 지정 receiver identity를 확인하거나, 검증할 수 없는 경우 명시적인 operator verification/attestation 정책으로 임의 endpoint를 거부한다.
- 실제: 초기 QR은 형식·ASCII host와 secret/display name 구조만 검증하고 저장되며, 이후 direct socket connect가 self-asserted host/port를 사용한다. status/CSV의 private subnet 제한은 자동 recovery에만 있고, QR PDF direct send에는 같은 제한이 없다.

#### 영향

- 보안/네트워크: 가짜 QR 승인 시 Kiosk status 및 QR 카드 PDF가 의도하지 않은 endpoint로 전송될 수 있다. 악성 QR 승인 없이는 발생하지 않으며, 실제 외부 전송은 검증하지 않았다.
- 운영/호환성: public/DNS/비-RFC1918 환경을 합법적으로 지원한다면 단순 private host 거부는 기존 배포를 깨뜨릴 수 있다. 그래서 정책 결정 전에는 확정 결함이 아닌 후보로 둔다.
- `LUNA-0002`의 at-rest pairing secret, `LUNA-0030`의 transient immutable pairing String, `LUNA-0007`의 resource exhaustion과는 다른 초기 endpoint trust/routing 경계다.

#### 근본 원인 후보

- pairing QR의 형식 유효성·비밀키 보유와 네트워크 endpoint/receiver identity 신뢰를 하나의 저장 단계에서 분리하지 않고, 생성 측이 넣은 host를 그대로 trust-on-first-use하는 구조로 보인다.
- recovery에는 private subnet/authenticated probe 정책이 있지만 초기 pairing validation에 재사용되지 않는다.

#### 반대 가설·오탐 검토

- physical QR 스캔 자체를 명시적인 trust-on-first-use 승인으로 보고 운영자가 PC display name을 확인한다면, strict host restriction은 호환성·제품 선택일 수 있다. 이 운영 절차의 실제 강제/검증은 소스에서 확인되지 않아 후보로 유지한다.
- 현재 QR에 secret이 있고 이후 전송은 암호화되므로 일반적인 passive LAN sniffing 문제로 확대하지 않았다. 문제는 fake QR이 endpoint와 secret을 함께 자기 주장할 수 있는 초기 trust 경계다.
- 정상 PC가 DHCP로 주소를 바꾼 뒤 저장된 secret으로 같은 private `/24`의 authenticated probe만 채택하는 recovery와, PC firewall을 Private profile에 한정한 운영 문서는 강한 반대 근거다. 다만 recovery는 이미 QR을 승인·저장한 뒤의 주소 변경 경계이고 PDF direct send에는 적용되지 않으며, 초기 QR의 source host/receiver identity를 독립적으로 증명하지 않는다.

#### 기존 테스트가 잡지 못한 이유

- pairing/protocol vector와 endpoint resolver 테스트는 사설 IPv4 fixture 또는 recovery 후보 제한을 다루지만, public DNS·loopback·multicast·임의 hostname QR을 decode/save 단계에서 거부하는지와 initial receiver identity attestation을 검증하지 않는다. `pc_receiver/tests/test_server.py`의 `127.0.0.1`은 local socket fixture용으로 `ConfigStore`/server state에 직접 넣은 host이고, Kiosk의 initial QR decode policy를 검증하는 negative case가 아니다.

#### 수정 방향·회귀 위험·수정 후 검증

- 제품이 private Wi-Fi를 강제한다면 초기 decode/save에서 source-compatible IPv4 RFC1918/private host를 요구하고 loopback·public·multicast·허용되지 않은 DNS를 거부한다. private host만으로 지정 receiver identity를 증명할 수 없다면 explicit pairing identity/attestation과 운영자 확인 정책을 함께 정의해야 한다.
- 비-RFC1918 지원 환경이나 hostname 기반 배포를 단순 거부하면 호환성이 깨질 수 있다. 현재 배포 전제와 허용 host policy를 먼저 확정한 뒤 public/DNS/loopback/multicast/fake display name fixture, no-external-connect, recovery compatibility 회귀시험을 추가해야 한다. 이번 Goal에서는 source·문서·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0034`는 초기 pairing endpoint trust 후보이며 `LUNA-0002`·`LUNA-0030`·`LUNA-0007`과 중복으로 합치지 않는다.
- 초기 pairing private-host/receiver identity 정책과 호환성 범위는 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0035 — Web recovery 단일 실행 gate가 반·보강·학생 관리자 mutation까지 잠그지 않아 수업 시작 action이 stale할 수 있음

> **교정 후 판정 — 해결.** baseline 심각도는 repository 불변조건·단일 executor·
> Activity 재생성 시 추측 실행 대신 action 유실이라는 반대 근거를 반영해 P4로
> 하향한다. `ecb1825`에서 Web recovery와 모든 학생·반·QR·CSV·PDF 관리자
> 작업을 상호 차단하고 handler에서도 gate를 재검사한다.

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 11:11:53 / 2026-08-02 22:14:11 (Asia/Seoul)
- webRecoveryLauncher의 ActivityResult callback은 callback 시점의 pendingRecoveryAction을 읽고 즉시 None으로 지운 뒤, 별도 recovery generation·selection revision·session snapshot을 비교하지 않는다(MainActivity.kt:310-335). 복구 결과와 pending action의 대응은 현재 Activity 필드와 단일 boolean gate에 의존한다.
- configureActions()의 click wiring과 createClass(), deleteClass(), replaceClassMemberships(), showTemporaryStudentDialog()/addTemporaryStudents() handler에는 공통 webRecoveryGate 선행 검사가 없다(MainActivity.kt:491-520, :1093-1221, :2388-2456). 버튼을 비활성화하는 조건과 handler 실행 방어가 같은 중앙 계약으로 묶여 있지 않다.
- ClassRosterSelectionState의 selection generation/revision은 stale roster-load callback 교체를 위한 상태이며, PendingRecoveryAction.StartSession의 classId·temporaryStudentIds 또는 학생 mutation gate와 연결된 operation revision이 아니다(AdminUiAsyncState.kt:1-141, MainActivity.kt:2516-2522, :2824-2830).
- 영향 모듈·버전: Kiosk `MainActivity`의 `webRecoveryGate`·`PendingRecoveryAction.StartSession`·반/보강/학생 관리자 mutation과 `StudentRepository.startSession()`

#### 사실

- `launchWebSessionRecovery()`는 `webRecoveryGate.tryStart()` 후 `pendingRecoveryAction`을 저장하고 `updateSessionAdminControls(currentSession)`만 호출한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:2459-2487`). `StartSession` action은 `startOrEndSession()` 시점의 `classId`와 `pendingTemporaryStudentIds`를 캡처한다(`MainActivity.kt:2490-2522`).
- `updateSessionAdminControls()`가 호출하는 `updateClassRosterUi()`는 Web recovery gate가 활성인 동안에도 `manageClassMembersButton`을 `classReady && currentSession?.sessionId == null`, `deleteClassButton`을 class/session 조건, `addTemporaryButton`을 classReady/students 조건으로 활성화한다(`MainActivity.kt:1257-1289`). 이 세 조건에는 `webRecoveryGate.isActive`가 없다.
- 같은 `updateClassRosterUi()`에서 `batchQrButton`도 `currentSession == null`과 반 소속 조건만 사용한다. `updateStudentManagementControls()` 역시 `webRecoveryGate`를 보지 않고 학생 등록·CSV·QR 재발급·이름·자격정보·비활성화·카드 상태 버튼을 `studentMutationGate`와 학생 목록만으로 활성화한다(`MainActivity.kt:1733-1748`).
- `startSessionButton`과 `resumeSessionButton`, `classSpinner`, `selfTestButton`만 `webRecoveryGate`를 직접 반영한다(`MainActivity.kt:1278-1281`, `:2880-2884`). 따라서 문서가 말하는 Web 안전정리와 후속 수업 전이의 단일 실행 gate가 모든 관리자 mutation에 공통으로 적용된다는 정적 근거는 없다.
- `updateQuickClassButtons()`는 `currentSession?.sessionId == null || className == selectedName`만으로 빠른 반 버튼을 활성화한다(`MainActivity.kt:1052-1065`). pre-session Web recovery 중 `classSpinner`는 비활성화되어도 `currentSession`이 아직 없으므로 빠른 반 선택이 남고, `PendingRecoveryAction.StartSession`이 이미 캡처한 `classId`·temporary IDs와 화면 선택이 달라질 수 있다. 이는 `LUNA-0035`의 같은 gate/action revision 경계를 보강한다.
- `completeSessionStart()`는 Web recovery 결과 뒤 캡처된 action의 `classId`·temporary IDs를 그대로 `studentRepository.startSession()`에 전달한다(`MainActivity.kt:2824-2847`). `startSession()`은 transaction 안에서 현재 active class·현재 active students·temporary IDs·기존 session 유무를 다시 검사하지만, 캡처된 사용자 의도나 mutation generation을 비교하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/StudentRepository.kt:648-698`).
- `replaceClassMemberships()`는 active class·active student ID만 검사하고 현재 session을 확인하지 않는다(`StudentRepository.kt:513-531`). `deleteClass()`는 현재 session이 그 반을 사용 중일 때만 거부한다(`StudentRepository.kt:533-547`). `SingleFlightGate` 시험은 동일 gate의 중복 `tryStart()`만 검증하며 Web recovery와 반/학생 버튼 전체의 enabled matrix를 검증하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/domain/AdminUiAsyncState.kt:143-155`, `kiosk/src/test/java/com/local/matholickiosk/kiosk/AdminUiAsyncStateTest.kt:217-232`).
- 활성 `QR_READY` 수업에서 `startOrEndSession()`이 `PendingRecoveryAction.EndSession`을 선택하면 `launchWebSessionRecovery()`가 `updateSessionAdminControls(currentSession)`를 호출한다. 이때 `addTemporaryButton`은 `active && resumable` 조건으로 계속 표시되고, `showTemporaryStudentDialog()`에서 호출하는 `addTemporaryStudents()`에는 `webRecoveryGate` 검사가 없다(`MainActivity.kt:2388-2456`, `:2459-2471`). `completeSessionEnd()`는 Web recovery 결과 뒤에야 `endSession()`을 `ioExecutor`에 제출한다(`MainActivity.kt:2850-2867`).

#### 추론

- 관리자가 반 소속 저장·반 삭제·보강 학생 선택·학생 변경을 빠르게 시작한 뒤 수업 시작/직전 Web recovery를 진행하면, recovery gate가 그 작업을 취소하거나 action generation을 무효화하지 않는다. 단일 `ioExecutor`가 제출 순서를 직렬화하더라도 mutation 실패 callback과 Web 결과 후속 start가 서로 다른 UI 메시지·의도로 완료될 수 있다.
- 예를 들어 반 소속 변경이 실패했는데 이미 캡처된 `StartSession` action이 뒤이어 실행되면, 사용자는 실패 안내를 보았어도 이전 DB membership과 캡처된 temporary IDs로 수업이 시작될 수 있다. 반대로 temporary student 선택을 바꾼 뒤 기존 action이 실행되면 최신 화면 선택과 실제 session roster가 다를 수 있다.
- `startSession()` transaction은 부분 저장·이미 활성 session·비활성 temporary ID를 차단하므로 DB 원자성 또는 항상 발생하는 corruption으로 확대하지 않는다. 문제는 cross-operation gate·사용자 의도·stale action binding이며, 실제 timing을 실행하지 않았으므로 P3 후보로 유지한다.
- 같은 경계는 활성 수업의 안전 종료에도 이어진다. EndSession recovery 중 보강 학생 추가가 제출되면 `addTemporaryStudents()`의 session ID/state 검사와 `endSession()`의 transaction 순서에 따라 성공 후 종료 시 정리되거나 종료 뒤 실패할 수 있지만, recovery 중 추가 조작을 차단하거나 사용자 의도에 맞춰 취소하는 공통 gate는 정적으로 확인되지 않았다. 이는 새 데이터 손상 finding이 아니라 `LUNA-0035`의 Start/End gate coverage 보강이다.

#### 가정과 미검증

- 일반 단일 창에서 외부 Web recovery Activity가 전면에 있는 동안 Kiosk 버튼을 직접 누르기 어렵다는 완화 조건을 인정한다. 후보 조건은 recovery launch 전후의 빠른 callback/queued mutation, Activity 전환·재진입, 또는 UI가 잠시 접근 가능한 lifecycle/multi-window 경계다.
- 실제 반/보강/학생 mutation을 수업 시작·Web recovery와 교차 실행하지 않았고, Android UI에서 각 버튼이 실제로 눌릴 수 있는 정확한 전환 timing도 확인하지 않았다.
- 활성 `QR_READY` 수업에서 EndSession recovery 중 보강 학생 추가 버튼의 실제 touch 가능 여부와 Web Activity 전환·복귀·`ioExecutor` 순서는 확인하지 않았다.
- 실제 학생·반·CSV·자격정보·session·DB와 A 화면은 사용하지 않았으며, `ioExecutor` queue 순서·failure callback 순서·Web result timing도 계측하지 않았다.

#### 안전한 확인 절차

- `webRecoveryGate`의 모든 참조, `updateSessionAdminControls()`·`updateClassRosterUi()`·`updateStudentManagementControls()`의 enabled 조건, `PendingRecoveryAction.StartSession/EndSession` 생성·소비와 EndSession 중 `addTemporaryStudents()` 경로를 읽기 전용으로 대조했다.
- `StudentRepository.startSession()`·`replaceClassMemberships()`·`deleteClass()`의 transaction/활성 session 검사, `SingleFlightGate` 시험과 RC04/RC05 단일 실행·운영 문서를 비교했다.
- 실제 Web recovery·반/학생 mutation·수업 시작·DB fault·테스트·빌드는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: Web recovery와 후속 Start/End 전이 중에는 모든 반·보강·학생·QR·CSV mutation을 차단하거나, mutation 세대가 바뀌면 pending Start action을 폐기하고 최신 snapshot을 다시 확인해야 한다.
- 실제: start/resume/class spinner/self-test 일부만 gate에 연결되고 반 구성·삭제·보강·batch QR·학생 변경 controls는 별도 gate 없이 남는다. pending Start action은 생성 시점의 class/temporary IDs를 보유한 채 recovery 결과를 기다리며, 활성 `QR_READY`의 EndSession recovery 중에도 보강 학생 추가 handler가 별도 gate 없이 남는다.

#### 영향

- 데이터/운영: 수업이 DB transaction으로 원자적으로 시작되더라도 사용자가 마지막으로 본 반·보강 선택·변경 결과와 다른 roster로 시작되거나, mutation 실패와 수업 시작 성공이 서로 다른 순서로 안내될 수 있다.
- 상태/보안: 학생 자격정보·QR secret 노출이나 직접적인 인증 우회는 정적으로 확인하지 않았다. 영향은 관리자 단일 실행 계약과 수업 구성 일관성이다.

#### 근본 원인 후보

- `webRecoveryGate`가 “Web recovery 중 수업 Start/End 중복”만 막도록 부분적으로 연결되고, 관리자 mutation 전체의 공통 operation generation·cancellation owner로 설계되지 않은 것으로 보인다.
- `PendingRecoveryAction.StartSession`이 class/temporary selection snapshot을 보유하지만, 후속 DB 전이 직전에 현재 selection/mutation revision과 비교하는 계약이 없다.

#### 반대 가설·오탐 검토

- 외부 Web Activity가 전면에 표시되고 단일 `ioExecutor`가 DB 작업을 직렬화하므로 정상적인 단일창 사용에서 실제 교차 조작 기회가 작을 수 있다. 따라서 확정 결함이나 데이터 손상으로 판정하지 않았다.
- `LUNA-0024`는 실행취소 불가 작업 뒤 이전 undo action이 남는 문제, `LUNA-0025`는 활성 session 중 membership undo가 UI 차단을 우회하는 문제, `LUNA-0027`은 Activity 재생성으로 recovery action 자체가 유실되는 문제다. `LUNA-0035`는 pre-session Web recovery 중 관리자 mutation과 Start action의 공통 gate/revision 누락으로 범위를 분리한다.
- `ClassRosterSelectionState`의 조회 generation은 stale 읽기 결과를 막지만, 반 mutation·temporary selection·pending Start action의 operation generation을 보호하지 않으므로 이 후보를 중복으로 제거하지 않았다.

#### 기존 테스트가 잡지 못한 이유

- `SingleFlightGate` 단위시험은 하나의 gate 중복 시작만 확인하고, `webRecoveryGate` 활성 상태에서 반 구성·반 삭제·보강·CSV·학생 변경·batch QR·수업 시작/종료 및 활성 `QR_READY`의 보강 학생 추가 버튼 전체 matrix를 검사하지 않는다.
- repository 시험은 `startSession()`의 저장소 불변조건과 보강 학생 원자성을 각각 검사하지만, UI에서 mutation을 제출한 뒤 Web recovery 결과가 도착하는 순서, mutation 실패 callback과 Start action의 교차 결과는 검증하지 않는다.
- RC04/RC05 문서는 Web safe cleanup과 Start/End 후속 전이를 단일 gate로 묶었다고 기록하지만, 해당 gate가 모든 관리자 mutation을 잠그는지의 회귀시험은 확인하지 못했다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 공통 `adminOperationGate` 또는 mutation revision을 도입해 Web recovery·Start/End 동안 반 구성·삭제·보강·학생·QR/CSV 작업의 UI와 handler를 모두 차단하고, pending Start action은 최신 class/membership/temporary snapshot과 일치할 때만 실행한다.
- 대안으로 recovery 결과 직전 DB에서 class/membership/temporary selection을 다시 읽어 action을 재검증하고, 변경·실패·재진입이면 Start를 취소하고 명확한 재시도 UI를 제공한다. 기존 단일 executor의 순서·정상 사용자 흐름·활성 session 중 정책과 충돌하지 않아야 한다.
- preflight 확인 중 mutation 제출, mutation 성공/실패, temporary selection 변경, class deletion, student deactivation/CSV, Web recovery success/cancel, Activity recreation과 button enabled matrix를 회귀시험해야 한다. 이번 Goal에서는 source·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0035`는 `LUNA-0024`·`LUNA-0025`·`LUNA-0027`과 인접하지만 undo lifetime·active-session repository guard·Activity action persistence가 아닌 pre-session Web recovery/admin mutation gate와 Start action revision 경계다.
- Web recovery 중 관리자 mutation 범위와 Start action 재검증 정책은 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0036 — 학생 mutation 단일 gate가 반 QR 일괄 재발급·인쇄를 잠그지 않아 stale·무효 QR이 출력될 수 있음

> **교정 후 판정 — 해결.** `40c69b3`에서 Android 직접 인쇄를 제거했고,
> `a3368b6`에서 학생·반·CSV·실행취소·단건/일괄 QR PDF 작업을 하나의
> `adminDataOperationGate`로 직렬화했다. 확인창의 늦은 실행도 gate에서 거부한다.

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 11:25:44 / 2026-08-02 22:23:17 (Asia/Seoul)
- fetchStudentCsvFromPc()와 applyStudentCsv()는 studentMutationGate의 begin/finish를 호출하지 않고 importStudentCsvButton만 false/true로 전환한다(MainActivity.kt:1750-1870). CSV preview/apply는 이름·자격정보·반 소속과 카드 출력 필요 상태를 갱신하므로 batch QR 작업과 공통 gate·operation generation을 공유하지 않는다.
- CSV 적용 확인 대화상자가 끝난 뒤 applyStudentCsv()는 같은 ioExecutor에 repository.importStudents()를 제출하지만 batchQrButton은 별도 classReady/session/membership 조건으로 남는다(MainActivity.kt:1820-1870, :2028-2120, :1254-1290). 단일 executor는 제출 순서를 정할 뿐 이미 발급된 card/인쇄 side effect의 최신성 binding은 제공하지 않는다.
- 영향 모듈·버전: Kiosk `MainActivity`의 `studentMutationGate`·`batchQrButton`·`prepareBatchQrPrint()` 및 `StudentRepository.reissueClassQrBatch()`/인쇄 callback

#### 사실

- `beginStudentMutation()`은 `studentMutationGate.tryStart()` 후 `updateStudentManagementControls()`만 호출한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:1718-1748`). 이 함수가 제어하는 것은 학생 등록·CSV·단건 QR 재발급·이름·자격정보·비활성화·카드 상태 버튼이며, `batchQrButton`은 포함하지 않는다.
- `updateClassRosterUi()`의 `batchQrButton.isEnabled` 조건은 class roster load 성공·현재 session 없음·소속 학생 존재뿐이고 `studentMutationGate.isActive` 또는 다른 class/QR mutation generation을 검사하지 않는다(`MainActivity.kt:1254-1290`). `confirmBatchQrPrint()`도 별도 gate 없이 확인 대화상자를 열고, `prepareBatchQrPrint()`은 batch 버튼만 false로 만든다(`MainActivity.kt:2028-2120`).
- `prepareBatchQrPrint()`는 `beginStudentMutation()`·공통 operation ID·batch generation을 호출하지 않는다. 따라서 `updateStudentManagementControls()`가 관리하는 학생 mutation 버튼과 `updateClassRosterUi()`가 관리하는 반 구성·삭제·보강 버튼은 batch 중에도 별도 상태로 남는다. `updateSessionAdminControls()`의 `classSpinner` 잠금도 active session 또는 `webRecoveryGate`만 보므로 batch 자체로는 class selection을 잠그지 않는다(`MainActivity.kt:1271-1289,1718-1748,2870-2885`).
- 학생 mutation과 batch QR 작업은 같은 `ioExecutor`에 제출되지만, 각 작업의 의미적 성공·실패를 연결하는 shared operation ID·cancellation·latest snapshot 검사는 없다. 먼저 끝난 DB 작업은 UI callback을 `runOnUiThread`에 게시하고, batch 작업은 반환받은 `BatchQrCard` bitmap을 보유한 뒤 그 callback에서 `PrintManager.print()`를 호출한다.
- `ClassRosterSelectionState`의 generation/revision은 늦은 roster 읽기 callback이 현재 class를 덮어쓰지 못하게 할 뿐, 이미 발급된 `BatchQrCard`나 `PrintManager` side effect를 취소하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/domain/AdminUiAsyncState.kt:3-95`). `BatchQrPrintDocumentAdapter`는 DB나 repository를 참조하지 않고 생성 시 받은 cards를 `onFinish()`까지 그린다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/print/BatchQrPrintDocumentAdapter.kt:20-105`).
- `BatchQrCard`의 데이터 계약은 `displayName`과 `qrBitmap`뿐이며 `studentId`, `classId`, issued QR revision/hash, mutation generation이 없다(`BatchQrPrintDocumentAdapter.kt:20-23`). `prepareBatchQrPrint()`는 UI로 넘기기 전에 issued token의 hash를 zeroize하고, `recordClassQrBatchPrintRequested()`에는 card 수만 전달한다(`MainActivity.kt:2054-2067`, `StudentRepository.kt:485-488`). 따라서 인쇄 adapter 단계에서 특정 학생·반 mutation의 pending card만 식별하거나 취소할 durable binding이 없다.
- batch 성공 callback은 `updateClassRosterUi()`를 직접 호출해 `batchQrButton`을 `classReady`·session·membership만으로 다시 활성화한다(`MainActivity.kt:2113`, `:1254-1290`). 이 경로에는 `studentMutationGate.isActive` 검사가 없으므로 batch callback과 학생 mutation callback이 교차하면 mutation 처리 중에도 새 batch 확인 경로가 다시 열릴 수 있다(`MainActivity.kt:1718-1748`).
- `PrintManager.print()` 호출 뒤 `MainActivity`는 print job/adapter handle을 보유하거나 mutation revision 변경·`onDestroy()`에 맞춰 PrintManager 작업을 취소·무효화하는 경계를 두지 않는다. `onDestroy()`는 `ioExecutor`·PC executor를 종료할 뿐 이미 전달한 adapter의 의미적 유효성을 재검사하지 않는다(`MainActivity.kt:2097-2113`, `:3859-3874`).
- `StudentRepository.reissueClassQrBatch()`는 현재 active class의 active students를 조회하고 QR hash·card 상태를 한 transaction으로 회전한 뒤 issued cards를 반환한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/StudentRepository.kt:184-236`). 수업 중 batch는 거부하지만, pre-session student deactivation·profile/단건 QR mutation·class membership 변경의 결과나 실패를 batch action과 비교하지 않는다.
- repository는 roster list를 transaction 시작 전 얻고, transaction 안에서는 각 student의 active 여부만 다시 확인하며 class membership revision·batch snapshot ID를 기록하지 않는다(`StudentRepository.kt:188-228`). MainActivity의 정상 UI 작업이 single-thread `ioExecutor`에 직렬 제출되는 것은 반대 근거지만, 인쇄 callback이 DB transaction 밖에서 실행되는 cross-operation binding을 만들지는 않는다.
- 반 생성·삭제·소속 변경 handler도 `studentMutationGate`를 거치지 않고 같은 `ioExecutor`에 작업을 제출한다(`MainActivity.kt:1093-1221`). `BatchQrPrintDocumentAdapter`는 생성 시 받은 `BatchQrCard` 목록과 bitmap을 `onFinish()`까지 보유하고, 인쇄 직전 현재 student/class membership·QR revision을 재검증하는 hook은 없다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/print/BatchQrPrintDocumentAdapter.kt:20-105`).
- 현재 시험은 repository의 batch QR 원자성과 각 학생 mutation gate를 별도로 검사한다. `RepositoryInstrumentedTest.classBatchReissueRotatesEveryMemberQrAtomically()`는 batch 내부의 부분 반영만 검증하고, `AdminUiAsyncStateTest`의 `SingleFlightGate` 시험은 일반 gate 중복만 검사하며, `MainActivityInstrumentedTest`에는 `batchQrButton`·class selection·`studentMutationGate`의 교차 enabled/callback matrix가 없다. `BatchQrPrintDocumentAdapter` 자체와 mutation 후 pending print invalidation 시험도 찾지 못했다. 운영 문서는 학생 변경 단일 실행을 설명하지만 batch QR과의 교차 순서를 미검증으로 남긴다(`docs/BUILD_VERIFICATION.md:587-604`, `:706-708`).
- Git history에서 class batch·`BatchQrPrintDocumentAdapter`·`recordClassQrBatchPrintRequested` 도입은 `630fce1`(2026-07-30) 한 묶음으로 확인됐고, 이후 해당 cross-operation identity/cancellation을 보강한 commit은 검색되지 않았다. 이는 현재 구현의 변경 이력 범위에 대한 근거이며, 제품 의도를 확정하는 증거로 사용하지 않는다.
- 운영 문서는 Android 직접 인쇄를 현재 운영 범위에서 보류하고 지정 PC 전송을 기본 경로로 설명한다(`docs/CONTINUOUS_DEVELOPMENT_GOAL.md:1755-1756`, `docs/RELEASE_OPERATIONS.md:187-231`). 그러나 Kiosk source에는 batch 직접 인쇄 UI가 남아 있고 field checklist도 직접 인쇄를 선택 검증 항목으로 관리하므로, 이 반대 근거는 P3 후보의 발생 빈도·운영 우선순위를 낮추지만 코드 경계를 제거하지 않는다(`docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md:309-331`).

#### 추론

- batch QR 작업이 먼저 DB transaction을 끝내고 UI 인쇄 callback을 기다리는 동안 학생 비활성화가 같은 `ioExecutor`에서 뒤이어 실행되면, batch가 만든 학생 QR은 DB에서 폐기된 뒤에도 이미 반환된 `BatchQrCard`로 인쇄 서비스에 전달될 수 있다.
- 반 소속 변경이 batch와 교차하면 batch card가 이전 roster를 나타내고, 변경 실패가 먼저 발생해도 batch는 실패한 student mutation을 알지 못한 채 계속 실행될 수 있다. 반대로 student mutation이 성공한 뒤 batch가 실행되는 순서라도, 사용자가 기대한 “실패 시 batch 취소” 또는 최신 snapshot 재확인 계약은 없다.
- 단일 `ioExecutor`와 repository transaction은 DB partial write·동시 transaction corruption을 제한한다. 문제는 DB 직렬화가 이미 생성된 QR card/인쇄 side effect를 다음 mutation 결과에 맞춰 취소하지 않는 cross-operation intent·output binding 경계이며, 실제 timing을 실행하지 않았으므로 P3 후보로 유지한다.
- 특히 `ioExecutor` 작업이 issued cards를 반환한 뒤 main thread에서 `PrintManager.print()`를 호출하면, Android print service의 `onLayout()`·`onWrite()` 소비는 그 호출과 별개로 진행된다. 이후 같은 executor에서 mutation transaction이 성공해도 adapter가 가진 identity 없는 이전 bitmap을 회수할 source hook이 없어, DB 직렬화만으로 인쇄 결과의 최신성을 보장할 수 없다.
- 특히 batch transaction이 끝난 뒤 사용자가 학생 비활성화·이름 변경을 수행하면, `BatchQrPrintDocumentAdapter`가 이미 보유한 card는 DB의 새 상태를 모른 채 인쇄될 수 있다. 비활성화는 기존 hash를 폐기하고 이름 변경은 상태를 `needsPrint`로 되돌리므로, 이 경로는 단순 UI roster stale보다 QR 유효성·카드 표시명 불일치 영향이 크지만 실제 PrintManager scheduling은 미측정이다.
- `BatchQrCard`에 학생 ID나 QR 세대가 없으므로, mutation이 성공했을 때 특정 card만 폐기하고 나머지 card를 유지하는 보상도 정적으로 구현되어 있지 않다. batch 전체를 재발급하거나 그대로 인쇄하는 두 극단만 남는다는 점이 현재 output binding 공백을 키운다.

#### 가정과 미검증

- 일반 관리자 화면에서 batch 확인 후 student mutation 버튼을 누르거나, student mutation 처리 중 batch 버튼을 누를 수 있다는 전제는 enabled 조건에서 추론했다. 실제 화면의 정확한 touch/callback timing은 확인하지 않았다.
- class spinner·반 구성·삭제·보강 선택이 batch 중에도 보이는 상태가 실제 Android touch dispatch에서 어떻게 교차하는지, 그리고 Print spooler가 `onWrite()`·`onFinish()`를 언제 호출하는지는 실행하지 않았다.
- `PrintManager.print()` 반환 시점과 실제 `onLayout()`·`onWrite()`·`onFinish()` 순서, mutation callback이 해당 spooler 소비 전후 어느 시점에 실행되는지는 Android 13 A에서 확인하지 않았다.
- 실제 학생·반·QR·인쇄 대기열·PDF·PC 전송·DB 변경을 사용하지 않았고, `ioExecutor` task와 main-thread callback의 상대 순서도 계측하지 않았다.
- 제품이 batch QR을 다른 학생 mutation과 강하게 묶어야 하는지, 운영자가 항상 이전 작업 완료를 기다린다는 절차가 있는지는 Sol·사용자 결정을 확인하지 않았다.

#### 안전한 확인 절차

- `studentMutationGate`의 시작·종료와 `updateStudentManagementControls()` enabled matrix, `batchQrButton`의 roster/session 조건, class selection controls, `prepareBatchQrPrint()`의 task·bitmap·PrintManager 순서를 읽기 전용으로 대조했다.
- `reissueClassQrBatch()` transaction, student mutation repository 경계, batch atomicity 시험, 학생 변경 단일 실행과 미검증 운영 문서를 비교했다.
- `BatchQrCard` payload의 identity/revision 보유 여부, issued hash zeroize·audit count 기록, `updateClassRosterUi()` callback 재활성화와 `onDestroy()` print handle 부재를 source·test·history로 추가 대조했다.
- 실제 QR 발급·폐기·학생/반 mutation·인쇄·PDF·DB·Activity timing·테스트·빌드는 수행하지 않았다. 이번 반복에서는 `MainActivity`의 batch/student/class handler, `StudentRepository.reissueClassQrBatch()`, print adapter, 계측시험과 운영 checklist를 읽기 전용으로 대조했다.

#### 기대 결과와 실제 결과

- 기대: 학생/반 roster mutation과 bulk QR issuance/print가 하나의 admin operation gate 또는 generation으로 연결되어, mutation 중 batch를 시작하지 못하고 mutation 결과가 실패·변경되면 pending cards가 폐기되며 인쇄 직전 최신 active roster/QR 유효성을 다시 확인한다.
- 실제: student mutation gate는 batch QR·class selection·반 mutation을 잠그지 않고, batch는 자체 button flag만 사용한다. repository transaction은 issuance를 원자화하지만 identity 없는 `BatchQrCard`와 비동기 PrintManager side effect를 최신 mutation revision에 묶거나 취소하지 않는다.

#### 영향

- 운영/데이터: 이미 비활성화되었거나 현재 반에서 제외된 학생의 QR card가 인쇄되거나, 이름 변경 전 표시명이 포함된 card가 출력되고, 사용자가 확인한 최신 roster와 다른 bulk card set이 전달될 수 있다. 이를 통해 DB가 부분 저장된다는 근거는 확인하지 않았다.
- 감사/복구: batch audit는 `CLASS_QR_BATCH_PRINT_REQUESTED`와 count만 남겨 실제 class/card identity·issued revision·PrintManager 완료/취소 결과를 연결하지 못하므로, stale output 발생 시 영향 범위를 사후 판별하기 어렵다. 이 기록 공백은 QR secret 노출이나 audit 위조를 의미하지 않는다.
- 보안/개인정보: QR secret이 새로 외부로 노출되는 경계는 확인하지 않았고, 영향은 의도한 폐기·소속 상태와 인쇄 결과의 불일치다.

#### 근본 원인 후보

- 학생별 mutation single-flight와 class-level bulk QR/print single-flight가 별도 gate로 구현되어 공통 admin operation generation을 공유하지 않는 구조로 보인다.
- QR DB issuance 완료와 PrintManager side effect 사이에 mutation invalidation·snapshot verification·pending print cancellation 계약이 없다.
- print adapter 입력 DTO가 학생/반/QR revision identity를 제거한 표시용 카드로 축소되어, 이후 operation owner가 stale card를 선택적으로 무효화할 수 없다.
- class selection/roster UI generation은 읽기 callback에만 적용되고, batch-issued card·print adapter에 동일 generation을 전달하지 않는 구조로 보인다.

#### 반대 가설·오탐 검토

- 같은 single-thread executor가 DB 작업을 직렬화하므로 사용자가 항상 첫 작업 완료 후 다음 작업을 시작하면 문제가 나타나지 않을 수 있다. 그러나 버튼 enabled 조건과 UI callback 지연은 이 순서를 강제하지 않는다.
- `reissueClassQrBatch()`는 현재 active student를 transaction 안에서 검사하고, batch 자체의 QR rotation 원자성은 시험된다. 따라서 partial DB write·항상 발생하는 stale card로 확대하지 않고 후보로 둔다.
- Android 직접 인쇄는 현재 운영 기본 경로가 아니고 `needsPrint`의 local PrintManager 의미 자체는 별도 `LUNA-0039`에서 다룬다. 여기서는 직접 인쇄를 운영상 반드시 사용한다고 가정하지 않고, 노출된 batch code path의 cross-operation binding·회귀 공백만 유지한다. 다만 외부 인쇄 화면으로 Activity가 stop되면 onStop()이 관리자 재잠금을 예약하고 QR preview를 지우므로 정상 단일창 경로의 mutation 기회는 줄어들 수 있다(MainActivity.kt:3823-3836). print adapter의 onFinish() 시점, 관리자 재인증 후 pending job 상태와 mutation 순서는 A에서 확인하지 않았다.
- `ClassRosterSelectionState`의 late-result rejection과 `PrintDocumentAdapter.onFinish()` bitmap cleanup은 각각 정상적인 UI/자원 lifecycle 보호다. 그러나 둘 다 mutation revision과 인쇄 side effect의 의미적 유효성을 검증하지 않으므로 finding을 기각할 반증은 아니다.
- `LUNA-0035`는 Web recovery/admin mutation gate와 pending StartSession action revision, `LUNA-0024`는 stale undo lifetime, `LUNA-0006`은 QR hash array ownership, `LUNA-0010`은 PDF delivery retry/idempotency 문제다. `LUNA-0036`은 Web recovery와 무관한 student mutation·bulk QR/print ordering 경계로 분리한다.

#### 기존 테스트가 잡지 못한 이유

- `AdminUiAsyncStateTest`와 student mutation 관련 계측시험은 단일 student gate 및 refresh completion을 검사하지만 `batchQrButton`이 gate 활성 중 비활성인지, mutation 성공·실패 뒤 bulk QR task/PrintManager callback이 취소되는지는 검사하지 않는다.
- `RepositoryInstrumentedTest`의 batch 시험은 한 번의 `reissueClassQrBatch()` 내부 transaction만 검증하며, deactivation·profile/단건 QR·membership 변경과 batch의 queue/print 순서를 다루지 않는다.
- batch print adapter/PrintManager callback을 실제로 지연시키거나 mutation과 교차하는 시험 파일·호출 연결은 확인되지 않았다. `QrPrintDocumentAdapterInstrumentedTest`는 단건 QR bitmap/PDF cleanup 중심이라 batch adapter의 cross-operation coverage가 아니다.
- `BatchQrCard`의 student/class/revision identity 보존, batch 성공 callback 중 mutation gate 재활성화, PrintManager `onWrite` 지연·취소·Activity destroy와 pending card invalidation을 함께 검증하는 MainActivity/adapter 시험도 확인되지 않았다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 student mutation·class membership·bulk QR issuance/print를 공통 `adminOperationGate` 또는 generation으로 연결하고, 어느 하나가 active이면 관련 버튼과 handler를 모두 차단한다. batch가 발급한 cards는 mutation revision이 바뀌면 인쇄 전에 폐기하고 bitmap을 정리해야 한다.
- 대안으로 batch task가 실행 직전 roster/mutation revision을 snapshot하고 PrintManager 호출 직전에 DB에서 active session/class membership/QR revision을 재검증해 불일치면 인쇄를 취소한다. operation 순서·사용자 메시지·이미 폐기된 QR 재발급 정책이 바뀔 수 있어 회귀 위험이 있다.
- 어떤 대안을 택하든 `BatchQrCard` 또는 별도 print operation record에 student/class/issued revision을 보존하고, PrintManager job 취소·재생성·완료 semantics를 `needsPrint` 정책(`LUNA-0039`)과 분리해 정해야 한다. audit에는 class/card scope와 operation outcome을 연결할지 결정해야 한다.
- student mutation 성공·실패·submission rejection, batch 선행/후행, deactivation·profile·single QR·CSV·membership 변경, 인쇄 callback 지연·실패·Activity destroy를 회귀시험해야 한다. 이번 Goal에서는 source·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0036`은 `LUNA-0035`·`LUNA-0024`·`LUNA-0006`·`LUNA-0010`과 인접하지만 Web recovery gate, undo lifetime, hash owner, PC/PDF delivery protocol이 아닌 student mutation과 bulk QR/PrintManager side effect의 공통 operation binding 경계다. `LUNA-0037`의 pending-card PC delivery와 달리 local `PrintManager` side effect 및 반 batch의 stale output binding을 다룬다.
- `LUNA-0039`는 local PrintManager handoff 뒤 `needsPrint` 상태 의미·pending UI, `LUNA-0037`은 PC PDF ACK/delivered revision binding이다. 이 항목은 그 결과 상태가 아니라 batch card가 mutation과 분리된 채 spooler로 넘어가는 ordering/identity 경계를 유지한다.
- student mutation·bulk QR/print ordering과 “폐기된 QR의 pending print cancellation” 정책은 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0037 — pending QR card의 PC PDF 전송·delivered 기록이 student mutation과 묶이지 않아 폐기된 QR이 전달될 수 있음

> **교정 후 판정 — `LUNA-0036`/카드 lifecycle에 병합·종결.** `a3368b6`이
> PC ACK와 최신 목록 refresh까지 mutation을 차단한다. `f0c30cd`는 전송 후
> 이름/QR 변경을 새 PDF 필요로 되돌리고 비활성화 시 QR을 폐기한다. 정상적인
> 사후 변경까지 별도 delivery revision P3로 계산한 것은 중복·과장이었다.

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 11:32:13 / 2026-08-02 22:27:01 (Asia/Seoul)
- QrCardStatusDao.markDelivered()는 studentId 집합만 WHERE 조건으로 사용해 lastDeliveredAtEpochMs와 needsPrint를 갱신하며, 현재 QR revision/hash·발급 request ID·student active 상태를 확인하지 않는다(Daos.kt:109-116). StudentRepository.markCardsDelivered()도 같은 ID 집합과 count만 transaction/audit에 전달한다(StudentRepository.kt:476-482).
- preparePendingCardsPdf()는 executeSensitive()나 SensitiveTask가 아닌 일반 ioExecutor 작업이며, onDestroy()에서 queued SensitiveTask만 discard한다(MainActivity.kt:1934-1988, :3859-3874; SensitiveTask.kt:5-22). 실행 중 PcPdfSender socket과 pending delivery operation을 보유·취소하는 owner는 확인되지 않았다.
- PcPdfSender는 PDF 전체 hash·request ID·ACK를 검증하지만 카드별 studentId·QR issuance revision·active 상태를 protocol에 전달하지 않는다(PcPdfSender.kt:11-49). PC Receiver의 commit-before-ACK는 저장 성공의 반대 근거지만 Kiosk delivered update의 identity binding은 보강하지 않는다.
- 영향 모듈·버전: Kiosk `pendingCardsPdfButton`·`preparePendingCardsPdf()`·`StudentRepository.reissueQrBatch()`/`markCardsDelivered()`와 `PcPdfSender`

#### 사실

- `pendingCardsPdfButton`은 `updateStudentManagementControls()`에서 `!studentMutationGate.isActive`, 학생 존재, pre-session, paired PC 조건으로 활성화되지만(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:1733-1748`), `preparePendingCardsPdf()`가 시작될 때 `studentMutationGate.tryStart()`를 호출하지 않고 자기 버튼만 false로 만든다(`MainActivity.kt:1934-1982`).
- 따라서 pending-card PDF 전송 중에도 학생 등록·단건 QR·이름·자격정보·비활성화 handler가 `studentMutationGate` 관점에서 활성 상태로 남을 수 있다. 이 작업들은 모두 같은 `ioExecutor`를 사용하므로 DB 작업은 순차화되지만, PDF task와 후속 student mutation을 공통 operation generation으로 취소·무효화하는 계약은 없다.
- pending PDF task는 하나의 executor runnable 안에서 `reissueQrBatch(studentIds)`로 현재 active students의 QR을 transaction으로 회전하고, QR bitmap을 PDF로 export한 뒤 `pcPdfSender.send()`로 ACK까지 확인하고, 마지막에 `markCardsDelivered(studentIds)`를 수행한다(`MainActivity.kt:1934-1982`, `StudentRepository.kt:236-303`, `:476-485`).
- `reissueQrBatch()`는 active session·student 상태를 발급 시점에 검사하고, `markCardsDelivered()`는 전달 성공 뒤 student ID 집합의 `needsPrint`만 transaction으로 갱신한다. delivered 기록은 발급된 QR hash/revision이나 PDF request ID와 연결되어 있지 않다(`StudentRepository.kt:236-303`, `:476-485`).
- `PcPdfSender`는 PDF frame/request ID/hash와 ACK의 protocol 일치성을 확인하지만, 이 PDF에 들어간 각 학생의 현재 QR revision·active 상태가 ACK 시점에도 유효한지는 확인하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcPdfSender.kt:12-58`).
- 현재 call graph에서 `preparePendingCardsPdf()`의 QR 재발급·PDF 생성·PC ACK·`markCardsDelivered()`는 하나의 `ioExecutor` runnable 안에서 연속 실행되고, 알려진 학생·반 mutation handler도 같은 executor에 제출된다. 따라서 소스만으로 `reissueQrBatch()`와 `markCardsDelivered()` 사이에 mutation이 끼어드는 동시 race는 입증되지 않으며, 별도 executor/향후 병렬화·정책 경계까지 포함한 issued revision binding 공백으로 한정한다.
- repository 시험은 pending 학생을 batch로 재발급하고 전송 완료 상태를 표시하는 정상 순서를 검사하지만, PDF 전송 중 비활성화·이름/CSV/membership mutation, `markCardsDelivered()`의 revision binding, UI gate matrix는 검사하지 않는다(`kiosk/src/androidTest/java/com/local/matholickiosk/kiosk/RepositoryInstrumentedTest.kt:503-545`).
- `onStop()`은 관리자 재잠금·화면 QR preview 정리만 수행하고 pending PDF task를 취소하지 않는다. `onDestroy()`도 `ioExecutor.shutdownNow()`와 queued `SensitiveTask` 정리만 수행하며, pending flow 자체는 `SensitiveTask`가 아니고 실행 중 `PcPdfSender`의 socket/cancellation handle을 보유하지 않는다. worker 본문은 QR 재발급·전송·`markCardsDelivered()`를 끝낸 뒤에야 `destroyed`를 확인하는 UI callback으로 돌아간다(`MainActivity.kt:3823-3875`, `:1934-1988`).
- PC receiver `ReceiverState.accept()`는 임시 파일을 최종 경로로 교체하고 replay ID/config를 저장한 뒤 ACK를 만들며, handler는 그 반환 뒤 `sendall(ack)`를 수행한다(`pc_receiver/src/matholic_pdf_receiver/server.py:90-114,213-237`). 정상 경로의 파일 commit-before-ACK는 ACK 성공이 저장 성공을 의미하게 하는 반대 근거지만, ACK `sendall` 실패·설정 저장 실패·Kiosk 후속 상태 반영 실패의 교차 시험은 없다.
- `RELEASE_OPERATIONS`는 QR 재발급·PC 저장 완료·PC 폴더 인쇄를 순서로 설명하고(`docs/RELEASE_OPERATIONS.md:221-231`), 현장 checklist는 PC 전송 성공 뒤에만 delivered 상태가 되는지와 실패 시 오표시가 없는지를 요구한다(`docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md:309-325`). 그러나 전송 후 학생 비활성화·이름/QR 재발급 시 이미 저장된 PC artifact를 폐기·재생성할지, Activity 종료 시 전송을 계속할지에 대한 명시적 계약은 확인하지 못했다.

#### 추론

- pending PDF task가 성공적으로 PC에 전달된 뒤 같은 `ioExecutor`에서 `deactivateStudent()`·이름 변경·자격정보 변경·CSV가 실행되면, PC에는 의도적으로 이전 artifact가 남고 Kiosk는 이후 QR 폐기·`needsPrint` 갱신을 적용한다. 이것이 결함인지 “전달 후 변경은 새 카드가 필요하다”는 정상 정책인지는 현재 문서에 명시되어 있지 않다.
- `markCardsDelivered()`가 student ID만 받고 issued QR hash/revision·PDF request ID를 확인하지 않으므로, 현재 단일 executor의 순차성을 벗어나는 독립 caller·향후 병렬화·재시도 구현에서는 다른 QR 세대의 상태를 전달 완료로 기록할 수 있다. 현재 source만으로 그 interleave가 발생한다고 주장하지 않으며, 핵심은 revision-bound delivery contract의 부재다.
- `reissueQrBatch()` transaction과 `PcPdfSender` ACK 검증은 partial DB write·전송 frame 위조를 제한한다. 문제는 student mutation과 QR delivery/`needsPrint` 상태의 cross-operation binding이며, 실제 timing·PC 파일·DB를 사용하지 않았으므로 P3 후보로 유지한다.
- Activity가 관리자 화면을 떠나거나 파기되는 동안 실행 중 socket이 thread interrupt에 어떻게 반응하는지 확인하지 않았으므로, 실제로 전송이 중단되는지 또는 DB `markCardsDelivered()`까지 계속되는지는 미검증이다. 다만 코드상 실행 중 task를 명시적으로 close/cancel하거나 ACK 전후 `destroyed`를 검사하는 경계는 없다.

#### 가정과 미검증

- pending PDF 전송이 진행되는 동안 underlying student controls가 실제로 눌릴 수 있다는 전제는 `preparePendingCardsPdf()`가 student gate를 점유하지 않는 정적 조건에서 추론했다. 실제 touch/callback/네트워크 지연은 확인하지 않았다.
- 실제 학생·QR·PC PDF·ACK·비활성화·CSV·반 소속·DB·인쇄 대기열·PC 파일은 사용하거나 기록하지 않았다.
- 제품이 전송 시작 후 student mutation을 허용하고 “후속 mutation이 이미 전달된 card를 자연스럽게 폐기한다”는 운영 정책을 채택할 수 있다는 반대 가능성은 인정한다. 해당 정책의 명시적 계약은 확인하지 않았다.
- PC receiver의 정상 commit-before-ACK가 항상 유지되고, PC 폴더의 이전 PDF를 운영자가 후속 mutation 뒤 즉시 폐기한다는 전제는 검증하지 않았다. 실제 파일 보관·재생성·물리 인쇄 결과는 이 Goal의 금지 범위다.

#### 안전한 확인 절차

- pending card 상태 조회·선택 dialog·`preparePendingCardsPdf()`의 QR issuance→PDF export→PC ACK→`markCardsDelivered()` 순서, 단일 `ioExecutor` call graph, `studentMutationGate`/button enabled 조건을 읽기 전용으로 대조했다.
- `reissueQrBatch()` active/session 검사, card status DAO update, `PcPdfSender` request/ACK verification, repository 정상 시험과 운영 문서를 비교했다.
- PC receiver `ReceiverState.accept()`·handler의 commit-before-ACK/replay 경계와 Python/Kotlin 정상 protocol 시험, Activity `onStop()`/`onDestroy()`·`SensitiveTask` 취소 경계를 추가로 대조했다.
- 실제 PC 연결·PDF 전송·ACK·QR 발급/폐기·학생 mutation·DB·테스트·빌드는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: pending-card QR 발급·PC delivery·delivered 표시가 공통 operation gate/revision으로 묶여 전송 중 student mutation을 차단하거나, mutation revision이 바뀌면 PDF를 폐기·재생성하고 `markCardsDelivered()`를 취소한다.
- 실제: pending PDF task는 자기 버튼만 잠그고 student mutation gate를 소유하지 않으며, ACK 성공 뒤 student ID 기준으로 delivered를 기록한다. 실행 중 task에 별도 취소·socket close·lifecycle invalidation도 없고, delivered 결과와 발급 QR revision/현재 active roster의 연결은 정적으로 확인되지 않았다.

#### 영향

- 운영/데이터: PC 저장·인쇄 대상 PDF가 이후 비활성화된 학생의 QR 또는 이전 이름/소속을 담을 수 있고, Kiosk의 현재 card status와 PC 파일의 사용 가능성이 달라질 수 있다. DB transaction partial write의 근거는 확인하지 않았다.
- 보안/개인정보: 새로운 외부 endpoint trust나 PDF 암호화 결함은 이 항목에서 주장하지 않는다. 영향은 의도한 폐기·전달 상태와 이미 ACK된 PC artifact의 시간적 불일치다.

#### 근본 원인 후보

- pending-card delivery를 student mutation single-flight와 분리하고, `markCardsDelivered()`를 student ID 집합만으로 기록해 issued QR revision·delivery request와 결합하지 않은 구조로 보인다.
- 긴 PC ACK/네트워크 작업 동안 UI mutation을 차단하거나 pending output을 invalidation하는 operation owner가 없다.
- Activity lifecycle이 끝난 뒤에도 외부 PC side effect와 DB 상태 반영의 중단·보상 경계를 담당하는 operation owner가 없다.

#### 반대 가설·오탐 검토

- 같은 single-thread executor가 알려진 DB mutation을 직렬화하고, 사용자가 전송 완료 후 비활성화하면 이는 의도된 “전달 후 폐기”일 수 있다. 이 반대 가설 때문에 현재 후보는 실제 interleave 확정이 아니라, 전송 후 mutation 허용 여부·artifact 폐기/재생성·revision-bound `markCardsDelivered()` semantics를 Sol·사용자가 결정해야 하는 정책/검증 공백으로 제한한다.
- PC receiver의 commit-before-ACK와 request ID/hash/signature 검증은 정상 ACK가 위조·부분 저장을 통과하지 않게 한다. `LUNA-0010`이 ACK 유실·후속 상태 실패·중복 파일 재시도를 다루므로 그 protocol finding과 중복시키지 않고, `LUNA-0037`은 mutation/lifecycle과 delivered identity binding으로 유지한다.
- `LUNA-0010`은 ACK 유실·재시도·중복 저장/idempotency, `LUNA-0014`는 PC 전송 buffer cleanup, `LUNA-0036`은 local class batch/PrintManager ordering이다. `LUNA-0037`은 pending-card PC delivery와 student mutation invalidation/`needsPrint` binding으로 분리한다.

#### 기존 테스트가 잡지 못한 이유

- `MainActivityInstrumentedTest`에는 `pendingCardsPdfButton` 실행 중 `studentMutationGate`·학생 mutation control 상태와 PC PDF callback 순서를 검사하는 시험이 없다.
- repository 시험은 QR batch issuance와 `markCardsDelivered()`의 정상 순서만 검사하고, PC ACK 지연 중 deactivation/profile/CSV/membership mutation과 issued revision 검증을 다루지 않는다.
- PC receiver 시험은 정상 파일 저장·ACK·동일 frame replay 거부를 검사하지만 ACK 전송 실패, config save 실패 뒤 retry, Kiosk `markCardsDelivered()` 실패와 mutation/lifecycle ordering은 검사하지 않는다(`pc_receiver/tests/test_server.py:37-81`).

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 pending-card QR 발급·PDF export·PC delivery·delivered update를 공통 `adminOperationGate`/delivery generation으로 묶고, 전송 중 student/class mutation을 차단하거나 revision 변경 시 pending PDF·cards를 폐기한다.
- `markCardsDelivered()`가 student ID만 받지 않고 issued QR revision/request ID를 확인하도록 하며, ACK 직전/직후 active state·QR revision을 재검증하고 불일치 시 재생성 안내를 제공하는 대안이 있다. 기존 전달 UX·재시도·PC 파일 보존 정책이 바뀔 수 있다.
- deactivation·profile·single QR·CSV·membership 변경의 선행/후행·PC ACK 지연/실패·delivery retry·Activity stop/destroy·socket cancellation·bitmap cleanup을 회귀시험해야 한다. 이번 Goal에서는 source·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0037`은 `LUNA-0036`의 local PrintManager batch output과 인접하지만 PC PDF transfer/`markCardsDelivered()` 상태 경계이고, `LUNA-0010`의 protocol retry/idempotency와도 다른 student mutation invalidation 문제다.
- pending-card delivery 중 mutation 허용 여부, issued QR revision/request ID의 delivered 상태 binding과 재생성 정책은 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0038 — Web SPA 과제 전환에서 문제별 답안 현황 상태가 경로·과제별로 격리되지 않아 이전 상태가 재사용될 수 있음

> **교정 후 판정 — 해결.** `af46f82`에서 정규화한 URL과 명시적 과제 식별자,
> 제한적 fallback 표식으로 최근 8개 상태 scope를 분리하고 A→B→A 및 같은
> route의 다른 과제 회귀시험을 추가했다.

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 11:44:53~11:55:58 / 2026-08-02 18:26:18 (Asia/Seoul)
- 영향 모듈·버전: Web POC `WebDomScripts.applyStudentExperience()`·`navigateStudentSection()`·`MainActivity`의 SPA student monitor, 답안 현황 지도·`다음 미입력`·React 문항 전환

#### 사실

- `applyStudentExperience`는 `window.__matholicKioskProblemStates || (window.__matholicKioskProblemStates = {})`를 사용해 상태 map을 Web Document 전역에 만들고, 현재 문항 번호를 key로 답변/모름/미입력을 기록한다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/WebDomScripts.kt:2018-2020`, `:2075-2092`, `:2642-2683`). 경로·과제·문항 집합을 key에 포함하거나 SPA 경로 변경 시 map을 비우는 코드는 같은 파일에서 확인되지 않았다.
- 답안 현황의 `navigateToUnanswered()`는 같은 `problemStates[candidate]`를 읽고(`WebDomScripts.kt:2492-2513`), 현황 숫자와 문항 버튼 상태도 그 map을 사용한다(`WebDomScripts.kt:2595-2622`). 현재 문항과 전체답안 form이 같은 번호 범위를 재사용하면 서로 다른 과제의 동일 문항 번호가 충돌할 수 있다.
- `navigateStudentSection()`은 허용된 `/workbook`·`/diagnostic` anchor를 검증한 뒤 `preferred.click()`으로 SPA 우선 전환을 시도한다(`WebDomScripts.kt:4805-4857`). `MainActivity.navigateStudentSection()`은 이 JS를 실행하고, ACTIVE monitor가 이후에도 `applyStudentExperience()`를 반복 평가한다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:842-855`, `:1246-1355`). 이 경로에는 새 과제 식별자 snapshot과 상태 map reset이 없다.
- Kotlin의 `activeExperienceGeneration`은 `pollStudentExperience()`와 결과 summary callback의 stale callback을 버리는 수명 guard지만, `window.__matholicKioskProblemStates`를 새 generation/path/task로 교체하거나 비우지 않는다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:1260-1280,1403-1410`). `prepareStudentContentReveal()`도 target path 안정화·화면 차폐만 수행한다.
- 현재 시험은 `WebDomScriptsTest`에서 생성 script에 map·현황 계약이 포함되는지 확인하고(`webpoc/src/test/java/com/local/matholickiosk/webpoc/WebDomScriptsTest.kt:125-145`), 계측시험은 하나의 fixture에서 문항 2의 `unknown`→`unanswered` 전환을 확인한다(`webpoc/src/androidTest/java/com/local/matholickiosk/webpoc/DomContractInstrumentedTest.kt:410-465`). 두 과제/두 경로의 같은 문항 번호를 순서대로 평가하는 reset 시험은 찾지 못했다.
- `BUILD_VERIFICATION.md`는 학습지↔진단평가를 SPA 우선 전환 대상으로 기록하고, 과거에는 동일 `/diagnostic` link·전체 문서 fallback을 별도 문제로 다뤘다(`docs/BUILD_VERIFICATION.md:4493`, `:4562-4567`). 최신 답안 정정 기록은 같은 과제 재진입·문항 상태를 검증하지만 서로 다른 과제 사이의 map 격리는 확인하지 않는다(`docs/BUILD_VERIFICATION.md:6437-6449`).
- `webpoc/src`의 제한된 전체 검색에서 `__matholicKioskProblemStates`는 생성·문항 번호별 읽기/쓰기와 기존 시험 참조에만 나타나며, `history.pushState`·`history.replaceState`·`popstate`·`hashchange` 또는 task identifier를 상태 owner에 연결하는 코드가 없다. `MainActivity.navigateStudentSection()`도 새 task key/generation 없이 기존 ACTIVE monitor를 유지한다(`MainActivity.kt:842-855,1246-1355`).

#### 추론

- 같은 Web Document를 유지하는 SPA에서 과제 A의 q1·q2가 `answered` 또는 `unknown`으로 기록된 뒤 과제 B로 이동하면, 과제 B가 아직 DOM을 관찰하지 않은 q1·q2에도 이전 map 값이 남을 수 있다. 그 경우 현황 숫자가 높아지거나 `다음 미입력`이 실제 미입력 문항을 건너뛸 수 있다.
- 개별 문항을 방문하면 현재 DOM 검사와 input/change event가 map을 갱신하지만, 방문하지 않은 번호는 이전 값이 그대로 남는다. 이는 서버 답안·점수를 바꾸는 근거가 아니라 학생 안내·현황·문항 이동 결과가 새 과제 상태와 어긋날 수 있는 cross-task cache 경계다.
- 완전한 문서 reload가 모든 과제 전환에서 일어나면 전역 `window`가 새로 생겨 이 후보가 나타나지 않을 수 있다. 다만 코드가 SPA click을 우선하고 문서/검증 기록이 SPA 전환을 별도 범위로 명시하므로, 실제 전환 방식을 확인하기 전 P3 후보로 유지한다.

#### 가정과 미검증

- 실제 공개 사이트에서 학습지 목록→과제 A→목록/진단평가→과제 B가 같은 Web Document와 React tree를 유지하는지는 확인하지 않았다. 문서의 SPA 설명과 source의 anchor click을 근거로 조건부 추론했다.
- 실제 과제·학생·답안·문항 내용·서버 상태·A 화면을 사용하지 않았고, 답안 입력·`모름`·제출·재진입·네트워크 통신도 수행하지 않았다.
- 제품이 과제 이동 때 항상 full reload를 강제하거나, 공식 페이지가 모든 문항을 즉시 DOM에 노출해 map을 재작성한다는 운영/사이트 계약은 확인하지 않았다.

#### 안전한 확인 절차

- `problemStates`의 생성·쓰기·읽기·표시 경계와 map reset/과제 key 검색, Kotlin callback generation과 path reveal guard를 정적 source 검색으로 대조했다.
- `navigateStudentSection()`의 SPA click, `MainActivity`의 active polling, Web DOM/JVM·계측시험과 최신 SPA·답안 정정 문서의 검증 범위를 교차 확인했다.
- 실제 과제 전환·답안·제출·서버 세션·A·테스트·빌드는 수행하지 않았다. `WebDomScriptsTest`/`DomContractInstrumentedTest`의 단일 fixture·SPA link fixture와 `BUILD_VERIFICATION.md`의 SPA/동일 과제 운영 기록만 읽기 전용으로 대조했다.

#### 기대 결과와 실제 결과

- 기대: 과제 또는 학습 경로가 바뀌면 문제 상태 map을 폐기하거나 안정적인 과제/경로 key로 분리하고, 새 과제의 현재 DOM/서버 상태를 기준으로 현황과 미입력 이동을 구성한다.
- 실제: 상태 map은 Web Document 전역의 문항 번호만 key로 사용하며, SPA 전환·새 과제 식별·reset·전환 직후 전체 재수집 계약은 정적으로 확인되지 않았다.

#### 영향

- 사용자/운영: 새 과제에서 답한 문항 수가 실제보다 많게 보이거나 미입력 이동이 일부 문항을 건너뛰어, 학생이 답안을 확인·입력하는 순서가 잘못될 수 있다.
- 데이터/보안: 이 정적 근거만으로 서버 임시답안·점수·제출 데이터가 오염된다고 주장하지 않는다. 영향은 UI 상태·사용성·검증 신뢰성의 불일치다.

#### 근본 원인 후보

- 문항 번호를 전역 map key로만 사용하고 현재 pathname·과제 식별자·문항 집합 generation을 보유하지 않는 구조로 보인다.
- React/SPA DOM 교체를 추적하는 listener는 있으나, route/task 경계에서 이전 map owner를 폐기하는 lifecycle owner가 없다.

#### 반대 가설·오탐 검토

- full document navigation이면 `window.__matholicKioskProblemStates`가 자연스럽게 초기화되고, 같은 과제 재진입에서는 서버 답안과 map이 일시적으로 일치할 수 있다. 따라서 현재 즉시 기능 실패나 데이터 손상으로 확대하지 않았다.
- 현재 문항 방문·input/change·전체답안 form 검사로 일부 번호는 다시 계산된다. 그러나 방문하지 않은 번호의 이전 상태까지 지운다는 근거는 없으며, `LUNA-0016~0019`의 네트워크 pause 차폐·`LUNA-0005`의 Kiosk session callback과는 다른 Web task-state cache 경계다.

#### 기존 테스트가 잡지 못한 이유

- script 문자열·단일 fixture 시험은 상태 판정 자체를 확인하지만, 동일 Web Document에서 과제 A와 B의 같은 번호를 연속 평가하고 B의 초기 현황/미입력 이동을 검증하지 않는다.
- SPA route transition·React unmount/remount·과제 key 변경과 map reset을 연결하는 JVM/계측 회귀시험이나 최신 field checklist 항목은 확인하지 못했다.
- 기존 `testStudentSectionNavigation*` 계측시험은 `/workbook` 문서 안의 `/diagnostic` anchor `onclick`이 `document.body.dataset`만 바꾸는 fixture를 검사하고, 실제 URL·React tree·두 과제의 같은 문항 번호 map carry-over를 검사하지 않는다(`DomContractInstrumentedTest.kt:3991-4056`).

#### 수정 방향·회귀 위험·수정 후 검증

- 과제/경로의 안정적인 식별자와 문항 집합 generation을 상태 owner로 사용해 route/task 변경 시 이전 map을 폐기하고, 새 과제 DOM이 안정된 뒤 상태를 재수집하는 정책을 정한다. 식별자를 얻을 수 없으면 최소한 `/learningV2/...` path 변경·문항 수/번호 집합 변경 시 map을 reset해야 한다.
- 과제 A→B→A, 학습지↔진단평가 SPA 이동, 같은 문항 번호·다른 문항 수, delayed React mount, `다음 미입력`·현황 숫자·모름 해제·저장 답안 재진입을 회귀시험해야 한다. 이번 Goal에서는 source·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0038`은 네트워크 pause 차폐(`LUNA-0016~0019`), Kiosk QR/session stale callback(`LUNA-0005`), 제출 연타/renderer recovery 문서 범위와 인접하지만, Web SPA 과제 경계에서 문제 상태 map을 격리하지 않는 UI cache 문제로 분리한다.
- 과제 이동 시 full reload를 보장할지, stable task key/generation을 도입할지와 서버 답안 재판독 시점을 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0039 — Android PrintManager 성공 전달 뒤 QR 카드 `needsPrint` 상태가 해제되지 않아 pending 상태가 남음

> **교정 후 판정 — 해결 및 제목 정정.** Android print framework handoff를
> 물리 인쇄 성공으로 부른 것은 부정확하다. `40c69b3`에서 직접 인쇄를 제거했고,
> `f0c30cd`에서 상태를 `카드 PDF 생성 필요/지정 PC 저장 완료`로 정정했다.

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 11:59:48~18:21:44 (Asia/Seoul)
- 영향 모듈·버전: Kiosk `QrCardStatus.needsPrint`/`StudentRepository`, `MainActivity` 단건·반 전체 Android `PrintManager` 경로, pending-card UI

#### 사실

- 학생 등록·단건/반 QR 재발급·CSV 신규 생성은 `QrCardStatusEntity.needsPrint = true`로 상태를 만들고, 프로필 변경도 `setNeedsPrint(studentId, true)`를 호출한다 (`StudentRepository.kt:141-148,169-177,216-224,265-273,372-376,490-509`).
- `QrCardStatusDao.markDelivered()`만 `needsPrint = 0`으로 바꾸며 (`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/Daos.kt:109-116`), repository의 `markCardsDelivered()`가 이를 호출한다 (`StudentRepository.kt:476-483`). 현재 source 전체 검색에서 Android local print 성공 callback이 이 method 또는 false setter를 호출하는 경로는 찾지 못했다.
- 단건 `prepareQrPrint()`는 `recordQrPrintRequested()` 감사만 기록하고, `PrintManager.print()` 성공 시 preview를 지운다. 반 전체 `prepareBatchQrPrint()`도 `recordClassQrBatchPrintRequested()` 감사만 기록하고, 성공 시 UI를 갱신한다 (`MainActivity.kt:2028-2122,2142-2202`; `StudentRepository.kt:485-488,636-640`). 두 print adapter의 `onFinish()`는 bitmap/attributes 정리만 한다 (`BatchQrPrintDocumentAdapter.kt:97-105`; `QrPrintDocumentAdapter.kt:89-95`).
- pending-card dialog는 `listQrCardStatuses().filter { it.needsPrint }`를 그대로 표시한다 (`MainActivity.kt:1872-1912`). 반면 PC PDF flow만 ACK 후 `markCardsDelivered()`로 출력 필요 상태를 지운다 (기존 LUNA-0037 detail / `MainActivity.kt` pending flow).
- local print adapter test는 PDF geometry와 bitmap cleanup만 확인하고, repository status와 PrintManager success/cancel/failure를 연결하지 않는다 (`QrPrintDocumentAdapterInstrumentedTest.kt:208-307`). repository test의 `needsPrint=false` 확인은 명시적 `markCardsDelivered()` 호출 뒤에만 있다 (`RepositoryInstrumentedTest.kt:537-542`).
- product/ops docs say Android direct print is deferred and designated PC transfer is default (`docs/CONTINUOUS_DEVELOPMENT_GOAL.md:1755-1756`), while operator checklist still includes local printing (`docs/OPERATOR_ACCEPTANCE_CHECKLIST.md:60-88`). Direct printer external queue/outcome is outside app control (`docs/KNOWN_LIMITATIONS.md:24-26`).
- `KioskDatabase.MIGRATION_1_2`는 기존 `students` 전부에 `lastDeliveredAtEpochMs = updatedAtEpochMs`, `needsPrint = 0`을 기록한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/KioskDatabase.kt:50-78`). migration 시점에 실물 카드 전달·PC 저장·Android 인쇄 완료를 확인하는 입력이나 상태는 없으며, migration 계측시험도 이 합성값을 기대값으로만 확인한다(`KioskDatabaseMigrationInstrumentedTest.kt:20-58`).
- 현장 checklist는 신규·변경 카드의 `출력 필요`, 카드 발급/마지막 전달/출력 필요 일관성, PC 전송 성공 후 전달 완료·실패 시 미표시를 모두 미검증으로 남기고, 직접 인쇄를 사용하지 않으면 `미검증/운영상 미사용`으로 기록하도록 한다(`docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md:160`, `:323-326`). 따라서 현재 문서·시험은 `needsPrint`를 물리 출력 완료의 증거로 확정하지 않는다.

#### 추론

- local PrintManager handoff success can leave the active student in the pending list as “출력 필요.” A subsequent pending-card workflow may treat the card as not printed and reissue it, invalidating the previously handed-off QR; at minimum the UI/status does not converge.
- This is a status/operations consistency concern, not evidence of QR secret exposure, server answer corruption, or guaranteed physical print failure.
- Current PC-default operation and deferred direct-print policy reduce likelihood/relevance, so classify P4 candidate.

#### 가정과 미검증

- Actual `PrintManager` callback semantics and physical printer completion/cancellation were not run.
- It is not established whether product intends `needsPrint` to mean “service request pending” or “physical card not confirmed”; no manual clear/receipt state was found.
- No real student/QR/print queue/A/DB data was used.

#### 안전한 확인 절차

- Read-only source search across status DAO/repository/MainActivity/print adapters, related tests, and operations docs.
- No PrintManager, printer, QR, ADB mutation, test/build, network, real data.

#### 기대 결과와 실제 결과

- 기대: local print request has explicit requested/printed/delivered semantics; pending list clears only at a defined safe acknowledgment or offers a manual/verified resolution.
- 실제: local single/batch print records an audit and hands off to PrintManager but does not change `needsPrint`; only PC ACK delivery clears it.

#### 영향

- UI/operations: repeatedly displayed pending cards and possible unnecessary reissue/reprint; actual physical outcome is unknown.
- Data: QR reissue can invalidate the previously printed/handoff QR if operator retries; no partial DB write was found.
- Security: no new exposure confirmed.

#### 근본 원인 후보

- `needsPrint`/`lastDeliveredAt` model is coupled to PC delivery but local PrintManager has no completion state or owner.

#### 반대 가설·오탐 검토

- Keeping `needsPrint=true` until physical printer completion may be intentional because PrintManager acceptance is not physical output confirmation.
- Android direct printing is deferred in current operations, and old field verification/known limitations show external printer uncertainty. Therefore keep candidate P4, not confirmed.
- Existing students may intentionally be treated as already delivered when the status table is introduced, and PC-default operation may make the migration assumption acceptable. The source and migration test do not prove that policy for each physical card, so this remains a status-semantics/verification-gap candidate rather than a confirmed data defect or severity increase.
- This is distinct from LUNA-0036 cross-operation stale batch output, LUNA-0037 PC delivery invalidation, and LUNA-0010 PC ACK/retry.

#### 기존 테스트가 잡지 못한 이유

- adapter tests stop at PDF/bitmap lifecycle; repository test clears status only by directly calling `markCardsDelivered()`; no MainActivity local print/status integration matrix.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol/user decide whether to separate print-requested/print-confirmed/delivered states, add explicit verified/manual resolution, or intentionally keep pending with clear operator semantics. Do not clear automatically merely on PrintManager handoff unless policy accepts that.
- Verify single and batch local print success/cancel/failure, pending list convergence, repeat reissue behavior, PC delivery path, adapter onFinish and Activity destruction. This Goal does not modify source/tests.

#### 관련 발견·결정

- LUNA-0036/0037/0010 distinctions as above; status semantics and direct-print operational scope remain Sol/user queue.

### LUNA-0040 — Web 안전 종료가 서버 임시답안 cleanup 여부를 검증하지 않고 Kiosk에 `RESULT_OK`를 반환할 수 있음

> **교정 후 판정 — 기각·철회.** `8c9a97c`는 시험용 답안 cleanup 증거를
> 요구한 docs-only commit이지 제품 로그아웃이 서버 임시답안을 삭제해야 한다는
> 계약이 아니다. 로그아웃의 `RESULT_OK`는 인증 종료 성공이며 답안 삭제 증명이
> 아니다. 사용자 결정에 따라 `a932cee`에서 Web origin storage 전체 삭제도
> 제거했다. 아래의 위험 서술과 별도 P3 계산은 최신 판정으로 사용하지 않는다.

- 심각도: `P3`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견: 2026-08-02 12:10:14; 마지막 확인: 2026-08-02 18:15:02 (Asia/Seoul)
- 영향 모듈·버전: Web POC `MainActivity`의 학생 종료·로그아웃·session cleanup, `WebDomScripts.sanitizeLoginAndFingerprint`, Kiosk `webSessionLauncher`/`persistWebSessionResult`, 공식 학습 서버 임시답안 상태

#### 사실

- Web 학생 화면의 `finishButton`은 `ACTIVE` 상태에서 `beginLogout()`만 호출한다 (`webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:785-789`). 현재 종료 경로에 공식 답안 상태를 `unanswered`로 저장하거나 서버 임시답안의 삭제/비복원 결과를 확인하는 호출은 정적으로 확인되지 않았다.
- `finishLogoutVerification()`의 첫 번째 통과는 `clearWebSessionAndReloadLogin()`을 호출하고, 두 번째 로그인 문서 검증이 끝나면 `finishSecureKioskSession()`으로 진행한다 (`MainActivity.kt:1532-1554`). cleanup은 WebView history/form/cache/SSL, `WebViewDatabase`, `WebStorage`와 비동기 Cookie 삭제 및 login URL 재로드를 시도한다 (`MainActivity.kt:1644-1690`). 이는 WebView 로컬 상태 cleanup이지 공식 서버의 답안 상태 확인은 아니다.
- 재로드된 login 문서는 `probeSanitizedLogin()`에서 `WebDomScripts.sanitizeLoginAndFingerprint`로 검사한다 (`MainActivity.kt:1124-1165`). 이 script는 login origin/form/action, username/password 빈 값과 remember checkbox만 확인·정리하고, 과제 식별자·답안 현황·서버 임시답안·같은 과제 재진입 결과를 반환하지 않는다 (`webpoc/src/main/java/com/local/matholickiosk/webpoc/WebDomScripts.kt:8-64`). `isValidSanitizedLogin()`도 같은 local login fingerprint만 판정한다 (`MainActivity.kt:2129-2136`).
- `finishSecureKioskSession()`은 위 local 검증 뒤 Web 상태를 `IDLE`로 저장하고 `Activity.RESULT_OK`를 반환한다 (`MainActivity.kt:1554-1562`). Kiosk는 `webSessionLauncher`에서 `RESULT_OK`를 `passed = true`로 바꾸고 `persistWebSessionResult()`를 호출한다 (`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:225-247`). 따라서 local cleanup이 끝나면 서버 답안 재진입 상태를 확인하지 않은 채 Kiosk가 성공·QR 복귀 경로로 진행할 수 있다.
- `clearWebSessionAndReloadLogin()`은 `CookieManager.removeAllCookies { finishCookieClear(generation) }` 형태로 callback 인자를 사용하지 않고, callback 도착 뒤 `flush()`와 login URL 재로드를 진행한다 (`MainActivity.kt:1644-1701`). 이는 callback 자체가 도착했는지와 local storage cleanup 순서를 다루는 경계이지, 서버의 공식 답안 reset/ack를 확인하는 경계가 아니다.
- `beginAdminRecovery()`는 persisted `KEY_STATE`가 `IDLE`이면 `AdminRecoveryPolicy.requiresSanitization()`이 false가 되어 `beginRecovery()`를 거치지 않고 `finishAdminRecovery()`를 호출한다(`webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:270-284`, `AdminRecoveryPolicy.kt:3-13`). 이 fast path에는 login page navigation, WebView cleanup, official answer state 조회/저장, same-task re-entry 검사가 없다.
- `finishAdminRecovery()`와 `finishSecureKioskSession()`은 모두 `transition(WebPocState.IDLE)` 뒤 `setResult(Activity.RESULT_OK)`를 수행한다(`MainActivity.kt:1554-1582`). `transition()`이 SharedPreferences에 기록하는 것은 앱 내부 state/reason뿐이며, 공식 서버 답안 reset acknowledgement·과제 ID·답안 count·지연 관찰 결과를 함께 저장하지 않는다(`MainActivity.kt:2183-2205`). 이후 Kiosk admin recovery가 그 `IDLE` 값을 다시 성공 전제로 사용할 수 있다.
- `postClearVerificationPending`과 `logoutAttemptGeneration`은 Activity 메모리 필드다(`MainActivity.kt:102-105`). `LOGOUT_VERIFY`가 재생성·process recovery 중 persisted state로 남으면 `resumeFromPersistedState()`가 `beginRecovery()`를 호출해 pending flag를 초기화하고 local logout을 다시 시작하지만, 이 재시도 역시 서버 임시답안 cleanup proof를 만들지 않는다(`MainActivity.kt:225-243`, `:903-913`).
- `clickLogout()`은 공식 학습 서버의 계정 메뉴에서 정확한 `로그아웃` DOM 후보를 클릭하는 동작이고, 결과는 login URL·form/action·빈 credential·checkbox fingerprint 검사로만 판정된다(`WebDomScripts.kt:210-285`, `MainActivity.kt:1116-1165`). 해당 callback/result에 과제 식별자, 공식 answer state, reset acknowledgement, answer count가 없다.
- Kiosk `WebSessionResultPersistence.persist()`는 `passed` 값에 따라 Room의 `PRELOGIN_CHECK → QR_READY` 또는 `LOCKED` 전이를 수행하고 현재 Kiosk session을 다시 읽을 뿐, Web 공식 답안 상태·과제 식별자·재진입 관찰 결과를 입력으로 받지 않는다 (`kiosk/src/main/java/com/local/matholickiosk/kiosk/WebSessionResultPersistence.kt:8-21`).
- 최신 운영 기준은 화면에서만 값을 지우는 것을 완료로 보지 않고, 공식 답안 상태를 `unanswered`로 저장한 뒤 세션을 완전히 종료하고 같은 과제에 재진입해 최소 60초 동안 답안과 전체 답안 수가 다시 나타나지 않는지 확인하도록 요구한다 (`docs/CONTINUOUS_DEVELOPMENT_GOAL.md:55-58`). 최신 현장 정정 기록도 화면상 해제 뒤 공식 임시답안이 재진입 후 복원된 사건과, 공식 상태 정리·완전 종료·재진입·지연 관찰을 별도 완료 기준으로 기록한다 (`docs/BUILD_VERIFICATION.md:6437-6449`).
- 관련 Web 계측시험은 login DOM sanitizer의 빈 입력/checkbox와 개별 cleanup 예외·`SESSION_CLEAR` 실패폐쇄를 확인한다 (`webpoc/src/androidTest/java/com/local/matholickiosk/webpoc/DomContractInstrumentedTest.kt:25-109`; `RecoveryInstrumentedTest.kt:551-599`). 서버 답안 상태를 가진 동일 과제 재진입·지연 복원과 Kiosk `RESULT_OK`의 연결 시험은 찾지 못했다.
- `AdminRecoveryPolicyTest`는 `IDLE`을 sanitization 불필요 상태로만 판정하고, `RecoveryInstrumentedTest`의 `IDLE` launch/recovery 시험은 Web 내부 persisted state·login preflight 결과만 확인한다. external `ACTION_RECOVER_WEB_SESSION`의 `IDLE` fast path가 공식 답안 cleanup proof 없이 Kiosk `RESULT_OK`가 되는 시험은 찾지 못했다(`webpoc/src/test/.../AdminRecoveryPolicyTest.kt:40-48`, `RecoveryInstrumentedTest.kt:48-81`).
- Git history 검색에서 `clearWebSessionAndReloadLogin`, `sanitizeLoginAndFingerprint`, `finishLogoutVerification`의 도입은 `eb09567`(2026-07-22) baseline으로만 확인됐고, 이후 공식 answer reset/ack·same-task re-entry proof를 추가한 변경은 찾지 못했다. 이는 현재 history 검색 범위의 근거이며 실제 server contract 부재를 단정하는 근거는 아니다.

#### 추론

- 서버가 임시답안을 보존하는 경우, Web은 local login 정리에는 성공했지만 같은 과제 재진입 시 이전 답안이 다시 보이는 상태에서 Kiosk에 성공을 반환할 수 있다. 그러면 Kiosk가 QR 대기로 돌아간 뒤 다음 학생/시험 흐름이 이전 답안 상태를 만날 수 있다.
- 특히 persisted `IDLE`은 “직전 Web Activity가 local cleanup 단계까지 도달했다”는 앱 내부 projection일 뿐 “공식 서버가 해당 과제의 임시답안을 `unanswered`로 저장했다”는 ack가 아니다. 이 projection만으로 admin recovery가 즉시 성공하면, 이전 cleanup이 서버 상태를 놓친 경우 다음 recovery도 같은 미검증 상태를 통과한다.
- `LOGOUT_VERIFY` 중 Activity 재생성은 persisted state를 통해 fail-closed recovery를 다시 시작하는 완화책이지만, `postClearVerificationPending`을 잃고 local cleanup을 반복할 뿐 서버 answer revision·same-task re-entry를 확인하지 않는다. 따라서 retry 횟수와 local fingerprint 안정성은 공식 cleanup 완료의 대체 증거가 아니다.
- 이는 credentials·QR·서버 답안이 실제로 오염됐다는 현재 실행 증거가 아니라, “안전 종료 성공” 신호와 서버 답안 cleanup 완료의 binding이 없는 상태 전이·검증 공백이다.
- 실제 현장 정정 기록은 이 위험의 운영 전제를 보강하지만, 그 사건의 원인이 현재 release code의 어느 한 단계인지까지는 정적 자료만으로 확정하지 않는다. 따라서 P3 후보로 유지한다.

#### 가정과 미검증

- 공식 학습 서비스의 로그아웃 endpoint가 서버 임시답안을 항상 삭제하거나 별도 정책으로 격리할 가능성은 확인하지 않았다.
- `removeAllCookies` callback의 반환 의미와 실제 WebView cookie 저장소 반영 결과는 런타임에서 확인하지 않았다. 코드상 callback 인자 자체는 검증하지 않는다.
- `IDLE` fast path가 실제 운영에서 이전 same-task answer를 보유한 상태와 결합되는 조건, `transition(IDLE)` commit과 process death/result delivery 순서가 서버 상태에 미치는 영향은 재현하지 않았다.
- 실제 시험계정·과제·답안 입력·서버 재진입·네트워크 통신·60초 관찰·A 화면은 사용하지 않았다.
- Kiosk가 `RESULT_OK` 뒤 바로 새 QR을 열어도 서버가 세션별 답안 격리를 보장한다는 계약은 source/docs에서 찾지 못했다.

#### 안전한 확인 절차

- Web/Kiosk 종료 호출 경로, local cleanup script, result callback, 관련 계측시험과 최신 운영 문서를 읽기 전용으로 교차 대조했다.
- `beginAdminRecovery()`의 persisted `IDLE` fast path, `transition(IDLE)` 저장 내용, `postClearVerificationPending` lifecycle과 관련 policy/test/history를 추가로 읽기 전용 대조했다.
- 실제 답안·서버·QR·A·네트워크·테스트·빌드는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: `채점 끝내기` 성공은 공식 답안 상태의 안전한 정리, 완전 로그아웃, 같은 과제 재진입 후 지연 복원 없음까지 검증된 뒤에만 Kiosk `RESULT_OK`와 QR 복귀로 이어져야 한다.
- 실제: Web은 local WebView storage/cookie/login fingerprint를 정리·검사하거나 persisted `IDLE` fast path를 통과한 뒤 `RESULT_OK`를 반환하지만, 서버 임시답안의 `unanswered` 상태·동일 과제 재진입·지연 복원 여부는 검사하지 않는다.

#### 영향

- 사용자/운영: 다음 학생 또는 재진입 시험에서 이전 시험 답안이 나타날 수 있어 답안 확인·채점 신뢰성이 흔들린다.
- 복구 지속성: local `IDLE` projection이 서버 상태와 분리돼 있으면 한 번의 미검증 성공이 다음 admin recovery의 입력 기준선으로 재사용되어, 운영자가 재시도해도 같은 미정리 서버 상태를 발견하지 못할 수 있다.
- 데이터: 서버 임시답안·점수·제출 결과가 실제로 오염됐다고 이 정적 감사만으로 확정하지 않는다. 다만 cleanup 완료 신호가 서버 상태보다 앞설 수 있다.
- 보안: 새 credential·QR 노출은 확인하지 않았다.

#### 근본 원인 후보

- Web session completion이 서버 답안 lifecycle이 아니라 Android WebView local cleanup과 login DOM fingerprint에 결합돼 있다.
- 공식 답안 reset·server acknowledgment·같은 과제 재진입 verification의 소유자가 앱 상태 전이에 연결돼 있지 않다.
- `AdminRecoveryPolicy`의 sanitization 기준이 persisted app state 하나(`IDLE`/non-`IDLE`)에 집중되어, 서버 answer lifecycle을 확인하지 않는 성공 fast path를 허용한다.

#### 반대 가설·오탐 검토

- 공식 logout이 서버 임시답안을 자동 폐기하고, 최신 페이지가 항상 새 답안 상태를 서버에서 재수집한다면 현재 code의 local verification만으로도 충분할 수 있다. 그러나 그 계약과 server-side proof는 이 저장소에서 확인하지 못했다.
- persisted state를 `IDLE`로 쓰는 경로가 모두 local cleanup 이후에만 도달하고, `beginAdminRecovery()`의 빠른 반환이 단순히 이미 종료된 Web 상태를 재사용하는 정상 최적화일 가능성은 있다. 하지만 그 선행 cleanup이 official answer reset까지 포함한다는 증거와 `IDLE`에 server ack를 묶는 invariant는 확인되지 않았다.
- 최신 문서가 화면 cleanup만으로 부족하다고 명시하고 실제 재진입·지연 관찰을 완료 기준으로 추가했으므로, 이를 단순 중복으로 `LUNA-0038`에 합치지 않는다. `LUNA-0038`은 SPA client-side problem map 격리이고, `LUNA-0040`은 Web 종료 성공 신호와 server persisted answer 상태의 검증 경계다.

#### 기존 테스트가 잡지 못한 이유

- login DOM fixture는 local input/checkbox 정리와 fingerprint만 확인하고, 공식 서버의 임시답안·과제 key·완전 종료·동일 과제 재진입을 표현하지 않는다.
- cleanup 예외 시험은 `clearCache()` 등 Android local cleanup의 계속 실행·실패폐쇄를 확인하지만, `RESULT_OK` 뒤 서버 답안 상태를 확인하는 Kiosk/Web 종단간 시험은 없다.
- `WebSessionResultPersistenceTest`는 Room transition과 local session reload 실패를 검사하지만, `RESULT_OK`의 사전조건으로 Web 서버 cleanup proof를 요구하지 않는다.
- `IDLE` admin recovery fast path, `transition(IDLE)` 뒤 process death/result 지연, `LOGOUT_VERIFY` Activity recreation과 same-task answer revision/60초 재진입을 연결하는 시험도 없다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자는 공식 `unanswered` 저장/로그아웃 API를 제품 계약으로 둘지, 서버 ack를 별도 요구할지, 또는 live answer cleanup은 운영자 확인 후에만 완료로 인정할지 결정해야 한다. 검증되지 않은 local login fingerprint만으로 성공을 반환하지 않는 fail-closed 정책도 대안이다.
- `beginAdminRecovery()`의 `IDLE` fast path도 서버 cleanup proof가 없는 한 `RESULT_OK`를 허용할지, 아니면 동일한 cleanup/ack/re-entry 계약을 거치게 할지 결정해야 한다. persisted state에는 최소 answer/task identity와 cleanup proof 또는 명시적 미검증 상태를 묶어야 한다.
- 시험은 객관식·주관식·`모름` 상태의 공식 reset, 완전 로그아웃, 같은 과제 재진입, 답안 수/현황과 60초 지연 복원 부재, 실패·timeout·Activity 재생성 뒤 Kiosk `RESULT_OK` 금지를 포함해야 한다. 이번 Goal에서는 source·테스트를 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0038`의 SPA task-state map carry-over, `LUNA-0016~0019`의 pause 차폐/입력·접근성, `LUNA-0005`의 Kiosk stale Web callback과 인접하지만, 서버 persisted answer cleanup verification 자체는 별도 경계다.
- `RESULT_OK`를 언제 Kiosk `QR_READY`로 허용할지와 실제 answer cleanup의 책임 경계는 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0041 — PC receiver CSV 민감 buffer가 취소·전송·종료 뒤 명시적으로 zeroize되지 않음

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 12:24:06 / 2026-08-02 15:34:50 (Asia/Seoul)
- 영향 모듈·버전: `pc_receiver` `ReceiverApplication.choose_csv()`·`ReceiverState.pending_csv`·control response encoder와 Kiosk 학생 CSV credential 계약

#### 사실

- Kiosk `StudentCsvParser`는 CSV가 이름·아이디·비밀번호·소속 반들 열을 가지며, 반환된 username/password `CharArray`에 `clearSensitiveData()`를 제공한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/StudentCsvImport.kt:5-14`, `:38-84`). 따라서 이 흐름의 CSV payload는 자격정보를 포함할 수 있다.
- PC app `choose_csv()`는 선택 파일을 `path.read_bytes()`로 immutable `bytes`에 읽고 `ReceiverState.queue_csv()`에 넘긴다(`pc_receiver/src/matholic_pdf_receiver/app.py:201-220`). `finally`의 `payload = b""`는 local reference를 바꿀 뿐 이미 생성된 immutable bytes 내용을 덮어쓰지 않는다.
- `ReceiverState.queue_csv()`는 non-empty·1 MiB 이하·UTF-8 payload를 검증한 뒤 `pending_csv` tuple에 `bytes(payload)`를 보관하고, `clear_csv()`는 lock 안에서 tuple reference만 `None`으로 바꾼다(`pc_receiver/src/matholic_pdf_receiver/server.py:116-128`). `payload.decode("utf-8-sig")`도 immutable validation text를 만들며, immutable `bytes` 입력에 대한 `bytes(payload)`는 wipe 가능한 ownership boundary가 아니다. 새 CSV를 선택해 대기 값을 교체할 때도 이전 payload를 overwrite하는 cleanup은 없다.
- `accept_control()`의 `CONTROL_FETCH_CSV`는 lock 안에서 pending payload를 `encode_control_response()`에 넘겨 response를 만든 뒤 `pending_csv = None`으로 만들고, handler가 실제 `sendall()`을 수행한다(`pc_receiver/src/matholic_pdf_receiver/server.py:130-177`, `:213-248`). 따라서 `clear_csv()`·교체는 in-flight encode를 취소하지 못하고, sendall 실패 뒤 queue가 사라지는 의미는 `LUNA-0003`에 속한다. 이 경계에도 response/frame을 wipe하는 `finally`가 없다.
- `encode_control_response()`는 payload를 immutable plaintext concatenation에 넣고 `_encode_secure_frame()`에서 header·ciphertext·최종 frame을 추가로 생성한다(`pc_receiver/src/matholic_pdf_receiver/protocol.py:355-393`, `:516-547`). 반환 후 Python reference가 사라질 수는 있지만 plaintext/ciphertext/header/frame을 명시적으로 덮어쓰는 경계는 없다.
- `ReceiverApplication.shutdown()`은 tray·server 종료와 root destroy를 수행하지만 `receiver_state.clear_csv()`를 먼저 호출하지 않는다(`pc_receiver/src/matholic_pdf_receiver/app.py:258-263`). 프로세스 종료 시 OS가 메모리를 회수하는 것과 application-level zeroization은 별개다.
- Kiosk 반대편은 `PcControlClient.exchange()`에서 request/frame/header와 protocol 중간 buffer를 정리하고, 성공 fetch payload는 `MainActivity.fetchStudentCsvFromPc()`의 parse `finally`에서 지운다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcControlClient.kt:77-120`, `MainActivity.kt:1779-1790`). `StudentRepository.importStudents()`도 Android parsed row를 transaction 성공·실패 모두 `finally`에서 지우지만, 이 cleanup은 PC receiver의 immutable bytes·AES-GCM 내부/최종 frame을 회수하지 않는다.
- 기존 PC 시험은 정상 `queue_csv()`→fetch→두 번째 빈 fetch와 protocol encryption vector/authentication을 확인하지만, CSV 취소·교체·response encode/send 실패·shutdown 뒤 payload cleanup 또는 heap 생존을 검증하지 않는다(`pc_receiver/tests/test_server.py:84-131`, `test_protocol.py:134-168`, `test_app.py:6-13`).

#### 추론

- 대기 중에는 PC receiver가 선택한 CSV의 자격정보를 process heap의 `pending_csv`에 보유한다. 취소·교체·전송 완료·예외 뒤 reference가 사라져도 immutable bytes와 암호화 과정의 파생 buffer를 명시적으로 덮어쓰지 않으므로, payload 상한(1 MiB) 안에서도 여러 immutable copy가 crash/minidump·승인된 로컬 메모리 수집·allocator 재사용 전의 privileged inspection에서 잔류할 가능성이 있다.
- 이는 네트워크 전송이 AES-GCM으로 보호되는지와 별개의 volatile-memory 수명 문제다. 원본 CSV 파일은 사용자가 선택한 경로에 의도적으로 남으므로 이 finding은 원본 파일 보존 자체가 아니라 receiver process buffer cleanup에 한정한다.
- Python의 reference counting과 정상 프로세스 종료가 대부분의 객체를 빠르게 회수할 수 있고, lock·size/UTF-8 validation·Kiosk 측 payload/row cleanup 및 정상 인증 시험이 있으나, 실제 heap 재관찰을 하지 않았으므로 P4 후보로 둔다.

#### 가정과 미검증

- 이 PC receiver가 Kiosk 학생 CSV를 처리한다는 사용 시나리오와 Kiosk CSV schema를 연결했지만, 실제 CSV·username·password 값은 사용하지 않았다.
- CPython allocator가 해제된 bytes 영역을 재사용하거나 OS가 process memory를 덤프하는 정확한 시점·가능성은 측정하지 않았다.
- crash dump/minidump·pagefile·endpoint protection의 메모리 수집 정책은 확인하지 않았다.

#### 안전한 확인 절차

- PC app/server/protocol의 payload owner·clear/shutdown·response encode 경계, Kiosk `PcControlClient`/parser/repository cleanup, PC/Kiosk 시험 소스와 README·Gate 4·보안/운영 문서를 읽기 전용으로 대조했다.
- 실제 CSV·PC receiver·네트워크·파일·heap dump·테스트·빌드는 수행하지 않았다. Kiosk cleanup은 소스 계약으로만 확인했고 runtime 배열 wipe·PC allocator/crypto 내부 buffer 회수는 검증하지 않았다.

#### 기대 결과와 실제 결과

- 기대: CSV payload를 처리하는 동안 필요한 최소 수명만 보유하고, 취소·교체·전송 완료·예외·종료 시 모든 wipe 가능한 plaintext/credential buffer를 명시적으로 지운다.
- 실제: PC receiver는 immutable bytes와 response encryption buffer를 사용하며 clear/replace/fetch/shutdown 시 reference 제거 외의 zeroization 계약이 없다. fetch의 lock 직렬화와 sendall 전 queue 소비는 동작 의미를 설명하지만 buffer wipe를 대체하지 않는다.

#### 영향

- 보안/개인정보: PC receiver process를 읽을 수 있는 승인된 로컬 사용자·진단/충돌 수집 경계에서 학생 CSV 자격정보의 잔류 시간이 늘어날 수 있다. 이번 감사에서 실제 유출·덤프·credential 사용은 확인하지 않았다.
- 운영: CSV 대기 중 창을 숨겨도 receiver가 계속 실행되므로, 사용자가 대기 취소를 눌러도 volatile buffer의 즉시 삭제를 증명할 수 없다.
- 데이터: 파일 at-rest·Kiosk DB 암호문·네트워크 암호화가 직접 변경되거나 우회되는 문제는 아니다.

#### 근본 원인 후보

- Python immutable `bytes`와 `bytes` concatenation을 transport API로 사용하면서 payload owner에 mutable buffer·cleanup callback·shutdown hook을 두지 않은 구조다.
- `pending_csv`의 queue durability와 payload zeroization을 별도 lifecycle로 정의하지 않아, 성공·실패·취소·교체·종료 경계가 동일한 cleanup 계약을 공유하지 않는다.

#### 반대 가설·오탐 검토

- `clear_csv()` 후 reference가 사라지고 정상 프로세스 종료 시 OS가 메모리를 회수하므로 실용적 잔류 창이 짧을 수 있다. 또한 Python에서 immutable object의 완전한 zeroization은 구현·암호화 library 경계상 보장하기 어렵다. CSV 크기 상한·lock·UTF-8 검증과 Kiosk의 정상 payload/row cleanup은 노출 범위를 제한하는 반대 근거다.
- `LUNA-0003`은 fetch/send/restart 실패 시 pending CSV 자체가 소실되는 내구성 문제이고, `LUNA-0013`·`LUNA-0031`·`LUNA-0032`는 Kiosk parser/Android row owner 문제다. `LUNA-0041`은 PC receiver의 volatile buffer 수명으로 분리한다.

#### 기존 테스트가 잡지 못한 이유

- PC server 시험은 정상 응답 후 `pending_csv is None`에 해당하는 관찰만 간접적으로 하고, 이미 해제된 bytes의 wipe·response failure·shutdown owner를 표현하지 않는다. 정상 authenticated response는 protocol correctness의 반대 근거일 뿐 memory lifecycle 시험이 아니다.
- Kiosk의 `CharArray` cleanup 시험은 PC Python heap과 immutable `bytes`/cryptography frame을 다루지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 mutable `bytearray` owner와 단일 cleanup scope를 도입해 queue 교체·취소·response encode/send 성공·실패·shutdown에서 payload와 가능한 중간 buffer를 best-effort wipe한다. crypto library가 만드는 내부 복사본의 한계와 원본 CSV 파일 보존 정책은 별도로 문서화해야 한다.
- `LUNA-0003`의 delivery confirmation/queue 보존 설계와 함께 적용하되, retry를 위해 보존하는 pending data의 최소 수명·취소 UX·프로세스 재시작 정책을 분리해서 정한다.
- 수정 후에는 합성 CSV schema를 사용해 정상 fetch, cancel, replace, response encode/send exception, app shutdown에서 owner cleanup 호출 횟수와 파일/네트워크/로그 비노출을 검증하고, 실제 학생 CSV·heap dump는 이번 Goal 범위에서 사용하지 않는다.

#### 관련 발견·결정

- `LUNA-0002`의 PC config secret at-rest, `LUNA-0003`의 CSV queue durability, `LUNA-0009`의 Windows notification payload와는 각각 저장·내구성·표시 경계가 다르다.
- PC CSV payload zeroization 수준과 Python/cryptography buffer의 best-effort 허용 범위는 Sol·사용자 재판단 큐에 남긴다.

### LUNA-0042 — PC receiver listener가 UI·트레이 초기화보다 먼저 열려 startup/background 실패 시 종료 경계가 끊길 수 있음

- 심각도: `P4`
- 상태: `후보`
- 신뢰도: 중간
- 최초 발견·마지막 확인: 2026-08-02 12:45:26 / 2026-08-02 15:39:55 (Asia/Seoul)
- 영향 모듈·버전: `pc_receiver` `ReceiverApplication` 초기화·`--background` 자동 시작·Windows tray/server 종료 lifecycle

#### 사실

- `pc_receiver/pyproject.toml`의 source/package 버전은 `0.1.3`, Python 요구 버전은 `>=3.11`이며 `pystray==0.19.5`를 사용한다. 현재 운영 문서도 Windows 수신기 `0.1.3`과 TCP 48129 자동 시작을 기준으로 삼는다.
- `ReceiverApplication.__init__()`는 `ConfigStore.load_or_create()`와 host 계산 뒤 `ThreadedReceiverServer`를 만들고 `server_thread.start()`를 호출한다(`pc_receiver/src/matholic_pdf_receiver/app.py:34-49`). 그 뒤에야 `tk.Tk()`·창 위젯 구성(`:51-64`)과 `pystray.Icon`·저장되지 않은 daemon tray thread(`:68-79`)를 초기화한다.
- `ThreadedReceiverServer`는 `socketserver.ThreadingTCPServer`의 생성자(`pc_receiver/src/matholic_pdf_receiver/server.py:251-257`)로 넘어가면서 listener를 bind한다. `allow_reuse_address=True`는 재사용을 허용할 뿐, 이후 Tk/tray 단계의 owner 또는 rollback을 제공하지 않는다.
- 생성 중 `tk.Tk()`·`_build_window()`·tray 초기화가 예외로 끝나면 인스턴스가 완전히 반환되지 않아 `shutdown()`을 호출할 application owner가 없다. `main()`의 생성 예외 처리(`app.py:287-295`)는 `OSError`·`RuntimeError`·`ValueError`만 잡아 오류 대화상자를 표시하며, 이미 시작된 server thread에 `shutdown()`·`server_close()`를 호출하거나 join하는 보상 경계를 갖지 않는다. 이 목록 밖의 Tk/pystray 예외는 같은 오류 UI 경로에 들어간다는 보장이 없다.
- 설치 스크립트는 Startup shortcut에 `--background`를 전달한다(`pc_receiver/install-receiver.ps1:14-20`). 이 모드에서 `root.withdraw()`로 창을 숨기고(`app.py:64-66`), 정상 종료는 tray 메뉴의 `_tray_quit()`가 `root.after(0, self.shutdown)`을 예약하는 경로에 의존한다(`app.py:249-261`). tray daemon thread의 `run()` 예외를 mainloop에 전달하거나 상태로 표시하는 감시 코드는 없다.
- 정상적으로 완전히 생성된 인스턴스의 `shutdown()`은 tray·server shutdown/close·root destroy를 수행한다. 그러나 constructor 중간 실패나 tray thread 자체 실패에는 이 함수가 자동으로 호출되지 않는다. tray thread handle을 저장하거나 join하지 않고, server thread가 살아 있는지·예외로 종료했는지 mainloop에 전달하는 supervisor도 없다.
- `main()`은 `application.run()`을 호출한 뒤 `finally`에서 `application.shutdown()`을 호출하지 않으며, `root.mainloop()`가 shutdown 이외의 이유로 반환되는 경우의 공통 cleanup도 없다(`app.py:184-185,287-295`). `shutdown()` 자체도 각 close 단계의 예외를 분리해 후속 server/root cleanup을 보장하지 않는다.
- 기존 `test_app.py`는 `--smoke-check`가 `smoke.main()`을 호출하는지만 확인한다. `ReceiverApplication`의 UI/tray 초기화 실패, 좁은 constructor 예외 처리, `--background`에서 tray 실패, listener 선행 bind 뒤 rollback·tray join·server health를 연결한 시험은 찾지 못했다. `smoke.py`도 인증된 합성 PDF 저장·삭제만 검사한다.
- `test_server.py`의 TCP 시험은 완전히 생성한 server를 직접 `shutdown()`·`server_close()`하고 thread를 join하는 정상 종료 fixture만 다룬다(`pc_receiver/tests/test_server.py:60-81`). `test_app.py`에는 application constructor·tray/background lifecycle 시험이 없다.
- README·운영 문서는 창을 닫아도 tray에서 계속 실행하고 Startup 자동 실행·수동 종료를 운영 전제로 설명하지만, 창/tray 초기화 실패 또는 tray가 없는 background 프로세스의 fail-closed·복구 절차는 정의하지 않는다(`pc_receiver/README.md:29-41`, `docs/RELEASE_OPERATIONS.md:195-205,246-250`).
- 운영 기록에는 2026-07-29 완전 재부팅 뒤 `--background` 자동 실행과 TCP 48129 `LISTEN`이 확인되어 있다(`docs/RELEASE_OPERATIONS.md:249-250`, `docs/BUILD_VERIFICATION.md:4979-4986`). 이는 정상 설치·정상 startup의 확인이지, partial initialization·tray 예외 rollback의 검증은 아니다.

#### 추론

- 정상 PC 환경에서는 UI와 tray가 모두 초기화되어 이 경계가 보이지 않을 수 있다. 그러나 listener가 먼저 열리는 순서 때문에 `main()`이 잡는 종류의 창/tray 초기화 실패에서는 오류 대화상자가 살아 있는 동안에도 TCP 포트가 계속 열려 있을 수 있고, 다음 재시작·수동 실행이 포트 충돌 또는 이미 떠 있는 수신기와의 혼선으로 이어질 수 있다. 잡히지 않는 예외는 프로세스 종료로 OS 회수가 일어날 수 있지만, 이는 fail-closed cleanup 계약을 대체하지 않는다.
- `--background`에서 tray thread가 실행되지 않거나 내부 예외로 종료하면 root는 숨겨진 채 server가 계속 살아 있고 사용자가 정상 tray 종료·상태 확인을 할 경로가 없다. 반대로 server daemon thread가 예외로 종료해도 UI의 `수신 대기 중` 상태가 자동으로 실패로 바뀌는 감시가 없다. OS가 프로세스를 종료하면 daemon server도 함께 끝나지만, 그 전까지 수신기 상태가 운영자에게 보이지 않는 조건부 가용성·운영 경계다.
- 실제 Tk/tray 오류, 부분 초기화 중 TCP 연결, 포트 충돌, tray thread 예외·생존 또는 Windows 자동 시작은 실행하지 않았으므로 P4 후보로 유지한다. 정상 재부팅 기록은 확인했지만 예외 경로의 동작을 증명하지 않는다. 인증 전 연결 자원 상한 `LUNA-0007`, PDF commit/ACK `LUNA-0010`, CSV volatile buffer `LUNA-0041`과는 listener/UI lifecycle이 달라 별도 ID로 둔다.

#### 가정과 미검증

- Windows GUI/notification-area 초기화가 드물게 실패하거나 예외를 던질 수 있다는 조건을 검토 대상으로 삼았다. 정상 설치 환경에서 항상 실패한다는 주장은 하지 않는다.
- 실제 Windows tray API·Tk 오류·백그라운드 자동 시작·포트 점유·수신기 재시작·process/thread 상태는 확인하지 않았다. `main()`의 예외 종류별 실제 발생 여부와 `tray.stop()`/server close 중 예외 전파도 주입하지 않았다.
- 이번 자동 재개에서는 `adb` 실행 파일이 PowerShell PATH에 없어 A `device`/설치본 상태를 재확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 수행하지 않았으며, 이전 기록을 현재 상태의 직접 증거로 승격하지 않는다.
- 실제 PDF·CSV·학생 이름·pairing secret·네트워크 payload는 사용하지 않았다.

#### 안전한 확인 절차

- `app.py`, `server.py`, `config.py`, `smoke.py`, `pyproject.toml`, app/server tests, 설치/제거·build 스크립트와 README·release 운영·build verification 문서, 관련 Git history를 읽기 전용으로 대조했다.
- 수신기 실행·TCP 연결·Tk/tray fault injection·포트 점유·process 종료·Windows 자동 시작·파일/CSV/PDF 생성·테스트·빌드는 수행하지 않았다. `adb` 상태 조회는 PATH 미가용으로 실패했으며 대체 설치·환경 변경은 하지 않았다. 이번 반복에서는 정확한 생성 단계, 좁은 예외 경계, 정상 종료 fixture와 2026-07-29 자동 시작/0.1.3 검증 기록을 교차 대조했다.

#### 기대 결과와 실제 결과

- 기대: TCP listener는 UI/tray 초기화와 수명 owner가 연결된 뒤 열리거나, 초기화 실패·tray 실패·background startup 실패 시 listener와 모든 thread를 즉시 닫고 운영자에게 명확한 실패 상태를 남긴다.
- 실제: listener/server daemon thread가 UI/tray보다 먼저 시작되고, constructor 예외 경로에는 partial server cleanup이 없으며, `main()`과 `mainloop()` 뒤 공통 cleanup·tray join·server health 감시가 없다. 정상 종료 경로 자체는 `server.shutdown()`·`server_close()`를 호출하지만 각 단계 실패를 보상하지 않는다.

#### 영향

- 운영/가용성: 시작 실패 뒤 일시적인 포트 점유, 수신기 중복 실행 혼선, background 수신기의 보이지 않는 생존으로 PC 장애 확인·재시작이 지연될 수 있다.
- 데이터/보안: 이 정적 검토에서 PDF/CSV 저장 손상, 인증 우회, pairing secret 노출은 확인하지 않았다. 이미 정상적으로 열린 listener가 계속 처리할 수 있다는 lifecycle 영향에 한정한다.

#### 근본 원인 후보

- Windows UI/tray와 TCP server를 하나의 application lifecycle owner로 묶기 전에 server를 먼저 시작하고, constructor 실패·daemon thread 실패를 상위 상태 머신으로 전달하는 startup supervisor를 두지 않은 구조로 보인다.

#### 반대 가설·오탐 검토

- 정상적으로 Tk·tray가 초기화되고 사용자가 tray 종료를 선택하면 `shutdown()`이 server를 정리하고, 2026-07-29 자동 시작·TCP LISTEN 및 0.1.3 pytest/smoke 기록도 정상 경로의 반대 근거다. 따라서 항상 종료 누수라고 확대하지 않았다.
- `server_thread`와 tray thread가 daemon이어서 프로세스 자체가 끝나면 OS가 자원을 회수한다. `main()`의 제한된 예외 UI가 실제로 표시되는 경로도 있을 수 있다. 따라서 영구적인 listener 잔류나 항상 재현되는 포트 점유로 주장하지 않고, 부분 초기화·tray/server thread 실패·mainloop 반환 중의 운영 경계로 제한했다.
- `LUNA-0007`은 인증 전 연결과 event queue의 bounded resource 부재, `LUNA-0010`은 PDF 결과 확정/ACK/retry 순서, `LUNA-0041`은 CSV payload memory cleanup이다. 이번 항목은 정상 payload 처리 전후가 아니라 app startup/shutdown owner 문제다.

#### 기존 테스트가 잡지 못한 이유

- app test는 smoke option dispatch만 확인하고, `ReceiverApplication` 생성 중 단계별 failure·좁은 예외 catch·background tray lifecycle·listener rollback·tray join을 모의하지 않는다.
- server test의 `server.shutdown()`/`server.server_close()`는 완전히 생성된 `ThreadedReceiverServer`의 정상 종료만 다루며, UI 초기화 전 server start, constructor exception 보상, mainloop 반환과 server health 경계를 표현하지 않는다.

#### 수정 방향·회귀 위험·수정 후 검증

- Sol·사용자 재판단 후 UI/tray 준비 뒤 server를 시작하거나, constructor 전체를 소유하는 startup guard와 `try/finally`가 부분 초기화된 server/tray/root를 역순으로 정리하도록 한다. 처리할 예외 범위와 오류 UI 재생성 실패를 명시하고, tray thread를 보유·join하며 server thread 예외/종료는 mainloop에 bounded failure로 전달해 background listener를 fail-closed로 닫고 명시적 health/status를 남긴다.
- 정상 `--background` 자동 시작·창 표시·창 숨김·tray 복귀·tray 종료, Tk/pystray/server bind 단계별 예외, 포트 충돌·server thread 종료·재시작에서 listener가 닫히고 중복 수신기가 남지 않는지 검증해야 한다. 기존 PDF/CSV 정상 흐름과 startup error UI는 보존해야 한다.
- 이번 Goal에서는 source·tests·receiver·네트워크·파일을 수정하거나 실행하지 않는다.

#### 관련 발견·결정

- `LUNA-0042`는 `LUNA-0007`의 inbound resource cap, `LUNA-0010`의 commit/ACK, `LUNA-0041`의 CSV buffer ownership과 인접하지만, UI/tray가 살아 있지 않은 초기화·종료 lifecycle boundary로 분리한다.
- 수신기 startup 실패 시 fail-closed할지, tray 없이 headless service로 계속 둘지, 포트 점유 복구 UX와 자동 시작 재시도 정책은 Sol·사용자 재판단 큐에 남긴다.

## 5. 기각·이미 수정됨·중복

현재 없음. `LUNA-0002`는 네트워크 암호화/replay 보호가 존재한다는 이유로 중복·기각하지 않았고, `LUNA-0003`은 단순한 네트워크 오류 안내로 축소하지 않았으며, `LUNA-0004`는 정상 경로 정리가 존재한다는 이유로 기각하지 않았다.

## 6. 검토했으나 문제를 찾지 못한 영역과 근거

- Goal 계약의 우선순위와 금지 범위: 계약 문서 전체를 읽어 이번 감사의 읽기 전용 경계를 확인했다.
- 저장소 시작 기준선: Git 상태·diff 요약·log·브랜치·HEAD·upstream을 확인했다.
- A 연결 전제: 승인된 `SM_P610` ADB `device` 상태를 읽기 전용으로 확인했다.
- 전용기기 정책 상태: Device Owner component와 `kiosk`·`webpoc` Lock Task allowlist는 유지됐다. 06:53 A 관리자 화면 캡처에서 관리 패널을 확인했고, 전경 Kiosk `MainActivity`의 `mLockTaskModeState=NONE`은 `showAdmin()`의 관리자용 의도적 해제와 일치해 정책 결함으로 판정하지 않았다. 다른 화면의 실제 Lock Task 동작은 미검증이다.
- 전용기기 Lock Task 실패 경계: `enterRestrictedMode()`의 예외 시 restriction rollback은 확인했지만, 예외 없는 `currentMode()!=LOCKED` false 반환 시 restriction을 유지하고 `MainActivity`가 false를 실패로 전파하지 않는 경계를 `LUNA-0022` 후보로 기록했다. `startLockTask()` fault·PINNED·Activity lifecycle 경합은 실행하지 않았다.
- Kiosk Activity lifecycle: `onStop()`은 카메라를 중지하고 QR preview·PIN 입력을 지우며, 관리자 화면 이탈에는 `onStart()` 재잠금, 스캐너 화면에는 카메라 재바인딩, 공유 PDF에는 복귀 후 cleanup 예약을 적용한다. Web secure session은 별도 Web Activity의 background recovery가 담당한다. 이 정적 경계에서 stale QR/PIN UI의 새 finding은 추가하지 않았고, 원격 지원 중 Kiosk PIN·Web 자격정보 화면 `FLAG_SECURE` 매트릭스는 `LUNA-0020`으로 남겼다.
- 원격 지원 정책 정적 경계: `RemoteSupportStore`는 만료 시각과 현재 boot count만 저장하고, `RemoteSupportPolicy`는 1분~2시간으로 duration을 clamp하며 boot count 변경·만료 시 저장값을 제거한다. 06:53 승인된 짧은 A 화면 사이클로 배지·헤더 겹침 `LUNA-0012`를 확인했으며, 만료·재부팅·PIN·QR 경로는 실행하지 않았다.
- 원격 지원 민감 화면 경계: Kiosk controller가 유효한 remote support 동안 Activity 화면 상태와 무관하게 `FLAG_SECURE`를 해제하고, `showAuthentication()`·relock `onStart()`에는 자동 복원이 없음을 `SECURITY.md`의 “PIN·비밀번호 입력 중 사용하지 않음” 운영 조건과 대조해 `LUNA-0020` 후보로 기록했다. 이는 배지 겹침 `LUNA-0012`와 다른 캡처 차단 경계이며, 실제 PIN·QR·학생 세션·캡처는 미검증이다.
- 원격 지원 상태 저장 경계: Kiosk/Web `RemoteSupportStore`가 `commit()` 결과를 검사하지 않고 Kiosk의 cross-app broadcast도 수신·저장 acknowledgement 없이 진행하는 점을 `LUNA-0021` P4 후보로 기록했다. 정책의 duration·boot clamp와는 별개인 활성/종료 상태 divergence 경계이며, 실제 저장 실패·양 앱 상태는 미검증이다.
- PC 수신기 프로토콜 정적 부분 검토: Kiosk/Python의 AES-GCM/HMAC 프레임, 5분 timestamp window, request ID replay 기록, 파일명 정규화와 CSV 크기 제한을 확인했다. 이 목록은 `LUNA-0002`의 설정 secret at-rest 문제를 해결하지 않는다.
- PC CSV queue 실패 경계: 정상 fetch만 보장하는 기존 테스트와 달리 `pending_csv` 소비 시점이 `sendall`·Kiosk parsing 성공보다 앞선다는 점을 확인했다.
- PC CSV queue 수명: 선택한 CSV가 `ReceiverState.pending_csv` 메모리에만 있고 receiver 재시작 시 복구되지 않는 점을 `LUNA-0003`의 같은 queue 내구성 범위로 포함했다.
- PC receiver/Kiosk client replay·ACK·재연결 경계: `PcPdfSender`·`PcControlClient`의 단일 request/response와 strict response binding, receiver의 PDF commit-before-ACK·control replay 기록 순서를 대조했다. PDF ACK 유실·새 request ID 재시도는 `LUNA-0010`, CSV response/queue 소비와 재시작·수신 실패는 `LUNA-0003`, replay cache eviction은 `LUNA-0008`, status queue 재시도·stale 보유는 `LUNA-0033`과 중복된다. 기존 정상·즉시 replay·protocol vector 시험 외에는 ACK 유실·재연결·queue fault 시험이 없지만 새 독립 finding은 추가하지 않았다.
- PC receiver CSV volatile buffer 경계: Kiosk CSV schema의 credential payload가 `choose_csv()`·`pending_csv`·control response의 immutable bytes/plaintext/ciphertext/frame에 걸쳐 보유되고 clear/replace/fetch/shutdown에서 명시적으로 wipe되지 않는 점을 확인해 `LUNA-0041` P4 후보로 기록했다. `LUNA-0003` queue durability와 Kiosk `LUNA-0013/0031/0032` parser/row cleanup과는 다른 PC process-memory owner 경계다.
- Kiosk CSV 자격정보 정리의 정상/예외 경계: 정상 `finally`는 존재하지만, 기존 username 복호화 `map` 중간 예외가 그 `finally` 진입 전에 발생할 수 있음을 확인했다.
- Kiosk CSV parser memory 경계: transport `ByteArray`와 반환된 row `CharArray`는 wipe하지만, 전체 CSV·field를 만든 immutable `String` copies는 회수 API가 없어 `LUNA-0013`으로 확정했다.
- Kiosk/Web 보존 경계: `AuditEvent`의 production cleanup 호출 부재와 양쪽 `PrivateDiagnosticLog`의 크기만 제한된 2세대 회전을 제품 결정의 크기·기간 요구와 대조해 `LUNA-0011` 후보 범위를 보강했다. 실제 파일 나이·DB row 수는 확인하지 않았다.
- QR/PDF 정적 경계: `QrTokenCodec`의 token byte, QR bitmap, PDF cache/file provider, print adapter와 batch failure cleanup을 대조했다. 공유용 `EXTRA_SUBJECT`에는 표시 이름이 들어가지만, 공유·인쇄 대상은 `SECURITY.md`가 별도 신뢰 경계로 경고하고 있어 신규 finding은 만들지 않았다. 반환 Bitmap의 후속 erase/recycle과 별도로 `QrImageRenderer`의 `BitMatrix`·`IntArray` zeroize 계약이 없는 경계는 `LUNA-0023` 후보로 기록했다. 실제 인쇄·공유·외부 앱 수신은 실행하지 않았다.
- Kiosk PC PDF 전송 memory 경계: PDF filename을 구성하는 `filenameBytes`가 별도 wipe되지 않는 사실을 `LUNA-0014`에 합쳤다. 이는 QR token/hash의 ownership finding과는 다른 전송 buffer다.
- PC pairing QR 저장 경계: `PcPairingStore`는 raw pairing을 Android Keystore AES-GCM으로 암호화한 ciphertext/IV만 SharedPreferences에 기록하고, 평문·ciphertext·pairing secret byte 배열의 정리 경계를 갖는다. ML Kit raw QR `String`의 실제 heap 수명은 미측정이며 로그·진단·영속 raw 복사 경로는 정적으로 확인되지 않았지만, scanner→decode/save→encode·복호화의 immutable String zeroize 부재는 `LUNA-0030` 후보로 기록했다.
- Kiosk PC 전송 buffer 경계: PDF·status·CSV control의 정상 성공 wipe는 확인했지만, `doFinal()`/`Mac.doFinal()` 예외 시 plaintext와 파생 key를 보장하는 `finally`가 없어 `LUNA-0014`로 확정했다.
- Kiosk QR 세션 준비 callback 경계: QR generation guard는 확인했지만, 관리자 수동 선택이 pending QR callback을 무효화하지 않고 수동 callback에 공용 guard가 없다. `validateForActiveSession()`·수동 검증의 session read/query와 `transitionSession(expectedState = QR_READY)`도 세션 ID로 원자적으로 묶이지 않아, 종료·재시작 사이 in-flight 결과가 새 QR_READY 화면으로 넘어가는 순서를 `LUNA-0005`에 보강했다.
- Web recovery/admin gate 전체 버튼 경계: `classSpinner`·Start/Resume·self-test 일부만 gate를 반영하고, 빠른 반 버튼·반/보강/학생·CSV/QR mutation은 별도 `webRecoveryGate` 검사가 없다. 빠른 반 선택과 pending Start action의 불일치 가능성을 `LUNA-0035`에 보강했으며, 기존 `LUNA-0036`은 student mutation과 batch QR/print의 별도 generation 경계로 유지한다.
- QR·PDF 경계의 정상 정리: 원본 token 생성 시 zeroize, QR frame stale decision wipe, bitmap erase/recycle, cache FileProvider 범위와 공유 후 expiry/cleanup 경계를 확인했다. 단건 발급 hash ownership 누락은 `LUNA-0006`으로 별도 기록했다.
- Web POC 정적 경계: 정확한 Kiosk caller/signature, allowed HTTPS origin/path, login action/DOM count, credential wipe·one-time bridge, answer/MathQuill/submit guard, result completeness, logout cleanup generation과 renderer recovery를 대조했으며 신규 확정 finding은 없었다.
- Web POC 네트워크 pause 정적 경계: 제품 결정의 “WebView·답안 유지, 입력 차폐, validated reconnect 후 재개”와 현재 `registerNetworkMonitor()`·`hasValidatedNetwork()`·`updateNetworkPause()`·WebView 오류 경로를 대조했다. 전체 화면 clickable/focusable 패널이 터치를 가리키는 것은 확인했지만 `#F2` 반투명 배경, WebView 포커스·IME·일반 키 입력의 명시적 차단 부재, callback 등록 실패의 로그 전용 처리를 각각 `LUNA-0016`~`LUNA-0018` 후보로 기록했다. 실제 네트워크 단절·IME·화면 차폐·callback fault injection은 수행하지 않았다.
- Web POC 네트워크 문서 범위: `docs/WEB_POC_VERIFICATION.md`의 2026-07-21/22 POC 기록은 당시 `0.1.0`과 W05 `LOCKED / NETWORK_ERROR`를 적은 역사 문서다. 현재 제품 결정·변경 필드 점검표·source의 pause overlay와 문맥이 달라 별도 현재 기준선 finding으로 만들지 않았고, `LUNA-0001`의 오래된 “현재” 문서 범위에도 중복시키지 않았다.
- Web POC 네트워크 생명주기: `transition()`은 ACTIVE 이외 상태에서 네트워크·무입력 패널을 숨기고, `onStop()`은 secure ACTIVE session을 background에서 잠그며, `onDestroy()`는 등록된 network callback을 해제한다. 이 정적 경계에서는 pause 패널의 stale 표시나 Activity 종료 후 callback 자체에 대한 별도 finding을 추가하지 않았다. 실제 background/foreground·재생성은 미검증이다.
- Web POC 초기화 순서: `initializeUi()`가 `registerNetworkMonitor()`를 `uiInitialized = true`보다 먼저 호출하지만, 등록 직후 ACTIVE에 들어가는 경로가 아니고 이후 `transition()`/handler refresh가 `updateNetworkPause()`를 다시 호출한다. 초기 callback이 UI 준비 전에 들어와도 현재 정적 흐름에서 별도 누락 finding을 만들 근거는 부족했다.
- Web POC 네트워크 접근성 경계: pause 패널의 `clickable/focusable`와 안내 텍스트는 확인했지만 `importantForAccessibility`·접근성 subtree 차폐·TalkBack action 처리는 없고 WebView는 ACTIVE에서 계속 visible이다. 시각 차폐·IME 차단과 별개인 접근성 우회 가능성을 `LUNA-0019` 후보로 기록했으며 실제 TalkBack/접근성 서비스는 미검증이다.
- Kiosk 재시작·Web 결과 상태 경계: `KioskStatePolicy`의 QR_READY→RECOVERY_REQUIRED·민감 상태→LOCKED mapping, `StudentRepository.applyRestartPolicy()`의 트랜잭션 저장, Web 결과/bridge failure의 `PRELOGIN_CHECK` expected-state 검사를 확인했다. 과거 기록의 늦은 결과가 LOCKED를 QR_READY로 덮던 문제를 현재 코드가 기대 상태 검사로 차단하며, `WebSessionResultPersistence`는 전이 후 session reload 실패를 성공으로 보고하지 않는다. `ActiveSessionEntity.state`가 알 수 없는 문자열이면 recovery fallback을 반환하지만 이를 정규화 저장하지 않는 경계는 현재 코드가 enum 이름만 쓰고 UI가 fail-closed recovery를 제공하므로 별도 finding으로 집계하지 않았다.
- Kiosk Room schema·migration·cleanup 경계: `KioskDatabase`는 destructive fallback 없이 `MIGRATION_1_2`와 `MIGRATION_2_3`만 등록하고, schema 1/2/3 JSON과 `KioskDatabaseMigrationInstrumentedTest`가 학생·QR 카드 상태·관리자 credential/pinLength 보존을 대조한다. 1→2에서 기존 학생을 `lastDeliveredAt=updatedAt`, `needsPrint=0`으로 초기화하는 동작은 시험과 일치하지만, 물리 카드 전달 여부를 runtime에서 확인한 것은 아니다. `AuditDao.deleteOlderThan()`는 정의되어도 production 호출이 없으며, 이 기간 보존 공백은 기존 `LUNA-0011` 범위로 유지했다. migration·DB 복구·저장공간 압박은 실행하지 않았다.
- Kiosk 관리자 변경·실행취소 경계: 학생 등록·재발급·이름·자격정보·비활성화는 `SingleFlightGate`와 refresh completion으로 학생 단일 실행을 보호하고, 반 선택/명단 새로고침은 generation·revision으로 최신 선택을 보존한다. 저장소 계측시험은 활성 수업 중 단건 QR 재발급과 비활성화를 의도적으로 허용한다. 그러나 `offerAdminUndo()`가 반 삭제·소속 변경·이름 변경에서만 갱신되고 CSV·QR·자격정보·비활성화·반 생성 뒤 이전 action을 지우지 않는 경계를 `LUNA-0024` 후보로 기록했다. 실제 연속 변경·실행취소는 수행하지 않았다.
- Kiosk 활성 세션 membership freeze 경계: 활성 수업 중 반 구성·반 선택 UI는 잠기지만, 세션 관리자 재진입 뒤 남아 있는 `RestoreMemberships` pending undo는 `replaceClassMemberships()`의 활성 세션 검사 없이 실행될 수 있고, QR·수동 선택 query는 `class_memberships` 또는 `session_students`를 즉시 반영한다. 이 UI·repository·eligibility 불일치를 `LUNA-0025` P3 후보로 기록했다. 실제 세션 시작 후 실행취소·학생/QR 변경은 수행하지 않았다.
- Kiosk 세션 종료 snapshot 경계: `endSession()`은 transaction으로 `ADMIN_IDLE`을 저장하지만 `completeSessionEnd()` 성공 분기는 `currentSession`을 먼저 비우지 않고 `refreshAdminData()`에 의존한다. 일반 refresh failure가 이전 메모리 상태를 유지하는 정책과 결합될 때 실제 종료 DB와 active-session UI가 어긋날 수 있는 `LUNA-0026` P4 후보를 기록했다. 실제 DB read fault·종료·재시작은 수행하지 않았다.
- Kiosk Web recovery action 보존 경계: `pendingRecoveryAction`은 `MainActivity` 메모리에만 있고 `webRecoveryLauncher`는 재생성 후 기본 `None`을 `refreshAdminData()`로 처리한다. Web recovery가 성공해도 `StartSession`/`EndSession` 후속 DB 전이가 빠질 수 있는 `LUNA-0027` P4 후보를 기록했다. 실제 Activity recreation·결과 재전달은 수행하지 않았다.
- Kiosk pre-session/EndSession session-start/admin gate 경계: `webRecoveryGate`는 start/resume/class spinner/self-test 일부에만 연결되고 반 구성·삭제·보강·batch QR·학생/CSV mutation controls에는 공통으로 적용되지 않으며, pending `StartSession` action은 class/temporary selection snapshot을 보유한 채 mutation revision 재검증 없이 recovery 결과를 기다린다. 활성 `QR_READY`의 EndSession recovery에서도 `addTemporaryStudents()`가 gate 없이 남는 사실을 추가 대조해 같은 `LUNA-0035` P3 후보를 보강했다. 실제 교차 mutation·Web recovery·수업 시작/종료는 수행하지 않았다.
- Kiosk student mutation·bulk QR/print gate 경계: `studentMutationGate`는 학생 단건/CSV 변경 control만 잠그고 `batchQrButton`·`prepareBatchQrPrint()`에는 연결되지 않는다. 같은 executor의 DB 직렬화와 batch 내부 transaction은 확인했지만 mutation 이후 pending `BatchQrCard` 인쇄 취소·최신 roster/QR 재검증이 없어 `LUNA-0036` P3 후보를 기록했다. 실제 student mutation·batch QR·인쇄는 수행하지 않았다.
- Kiosk pending-card PC delivery gate 경계: `preparePendingCardsPdf()`는 QR batch reissue·PDF export·PC ACK·`markCardsDelivered()`를 수행하지만 student mutation gate를 소유하지 않는다. ACK 뒤 비활성화·이름/CSV/membership mutation이 가능할 때 PC artifact와 current QR/`needsPrint` 상태가 달라질 수 있는 `LUNA-0037` P3 후보를 기록했다. 실제 PC PDF·학생 mutation·ACK는 수행하지 않았다.
- Kiosk 재시작·Web 결과 상태 경계: `KioskStatePolicy`의 QR_READY→RECOVERY_REQUIRED·민감 상태→LOCKED mapping, `StudentRepository.applyRestartPolicy()`의 트랜잭션 저장, Web 결과/bridge failure의 `PRELOGIN_CHECK` expected-state 검사를 확인했다. 과거 기록의 늦은 결과가 LOCKED를 QR_READY로 덮던 문제를 현재 코드가 기대 상태 검사로 차단하며, `WebSessionResultPersistence`는 전이 후 session reload 실패를 성공으로 보고하지 않는다. `ActiveSessionEntity.state`가 알 수 없는 문자열이면 recovery fallback을 반환하지만 이를 정규화 저장하지 않는 경계는 현재 코드가 enum 이름만 쓰고 UI가 fail-closed recovery를 제공하므로 별도 finding으로 집계하지 않았다.
- QR 카드 재사용·폐기 경계: `findEligibleByQrHash()`는 활성·현재 반/보강·현재 `QR_READY`만 확인하고 `QrCardStatus.lastUsedAt`은 사용 이력만 갱신한다. 제품 결정과 문서는 QR 카드의 매 로그인 재사용 및 재발급 시 이전 hash 폐기를 전제로 하므로, `markUsed()`가 이전 사용을 차단하지 않는 것을 replay 결함으로 만들지 않았다. 실제 QR·세션·재시작 실기는 실행하지 않았다.
- Kiosk→Web credential bridge 정적 경계: `OneTimeCredentialBroker`의 단일 pending entry·30초 elapsed-time TTL·동기화된 consume/revoke, publish 전 이전 entry wipe, Provider의 signature permission·정확한 authority/v1 URI·trusted package 검사, Kiosk의 취소/Activity 종료 revoke와 Web의 Cursor close를 확인했다. Provider가 `CharArray`를 `MatrixCursor`/Web payload의 immutable `String`으로 물질화하고 로그인 JS 문자열을 만드는 잔여 수명은 `docs/THREAT_MODEL.md:40`의 “JVM/WebView 임시 문자열은 완전 제거를 증명할 수 없음”에 포함되므로 `LUNA-0013`과 중복인 신규 finding으로 만들지 않았다. 실제 cross-app query·Binder CursorWindow·ART heap 수명은 확인하지 않았다.
- Web/Kiosk 진단 출력 경계: 양쪽 private log는 허용 코드 정규화·2세대 256KB 파일 회전·ADB nonce/line filter를 갖지만 기간 cleanup은 정적으로 확인되지 않아 `LUNA-0011` 후보에 반영했다. 실제 공개 Web·로그·진단 dump는 실행하지 않았다.
- PC receiver 정상·보호 경계: AES-GCM/HMAC, timestamp·size/path 검증, atomic PDF replace와 lock 기반 request ID 기록을 확인했다. UI event queue는 존재하지만 기본 무제한이고, 인증 전 unbounded thread·event queue와 2,048개 replay eviction 후보는 `LUNA-0007`·`LUNA-0008`로 기록했다.
- 현재 기준선 문서 대조: `docs/CONTINUOUS_DEVELOPMENT_GOAL.md`의 A RC55/RC117 문장은 source·artifact·A와 일치하지만 같은 절의 체크포인트/RC53 문장이 HEAD보다 뒤처져 있고, `README.md`, `docs/CONTINUOUS_DEVELOPMENT_GOAL_PROMPT.md`, `docs/RELEASE_OPERATIONS.md`의 현재 표기도 오래된 것으로 `LUNA-0001`에 포함했다. 날짜가 붙은 명시적 역사 기록은 별도 결함으로 세지 않았다.
- PC 상태 알림의 부분 경계: status 이벤트는 `event.state`를 우선해 학생 이름을 toast에 넣지 않지만, PDF 저장 완료 이벤트는 filename을 재사용해 `LUNA-0009`로 기록했다.
- PC 수신기 오류·출력 재대조: `server.py`의 protocol error는 일반화된 문구이고, status tray 알림은 `event.state`를 우선해 학생 이름을 포함하지 않는다. 메인 수신기 창의 현재 학생 표시와 PDF 저장 완료 filename 알림은 신뢰된 PC UI/기존 `LUNA-0009` 범위이며, 새 로그·예외·파일명 누출 finding은 추가하지 않았다. 수신기 실행과 실제 알림은 미검증이다.
- 보안정책 대조: `SECURITY.md`의 QR hash 실패·완료 wipe 약속과 실제 repository/UI ownership 경계를 대조해 `LUNA-0006`의 영향 범위를 보강했다.
- PC PDF delivery 경계: 파일 commit·replay 기록이 ACK보다 앞서고 Kiosk 재시도가 새 request ID를 쓰며, ACK 뒤 delivered 상태 반영과 receiver config 저장 실패도 결과를 되돌리지 않는 구조를 확인해 `LUNA-0010` 후보 범위를 보강했다. ACK 유실·후속 저장 실패·재시도는 실행하지 않았다.
- Kiosk Room/state 경계: migration 1→2→3, 단일 I/O executor, transaction과 expected-state 재시작 정책을 읽어 새 상태 오염을 확정하지 않았다. `audit_events` cleanup DAO의 production 미호출은 `LUNA-0011` 후보로 기록했다.
- Android UI 정적 경계: Kiosk/Web layout의 주요 `Button`·`EditText`·`ImageButton`/`ImageView`에 텍스트·hint·contentDescription이 있고 입력 autofill 차단과 잠금 오버레이가 정의된 것을 확인했다. A 관리자 화면 한 회에서 원격 점검 배지가 헤더 상태를 가리는 `LUNA-0012`를 확인했으며, TalkBack·글자 크기·회전과 다른 상태의 clipping은 미검증으로 남겼다.
- 진단·원격 지원 출력 경계: Kiosk/Web private diagnostic log는 허용된 코드 형식·파일별 상한·ADB nonce/line filter를 사용하고, 원격 지원 receiver는 `DUMP` 또는 signature 권한으로 제한된다. 이 정적 경계에서 새 민감정보 출력·권한 우회 finding은 확정하지 않았다.
- 테스트·운영 연결: Kiosk repository 계측시험은 정상 상태·트랜잭션·자격정보 wipe를 폭넓게 다루지만 CSV 복호화 중간 예외·단건 hash ownership·PC 전송 crypto exception cleanup은 다루지 않는다. PC receiver 시험은 정상 PDF/ACK·replay·CSV once를 다루지만 `sendall`/ACK 유실·재시작 queue·동시성·thread 상한은 다루지 않아 기존 finding 후보/확정 근거와 일치한다. 테스트는 계약상 실행하지 않았다.
- 테스트·운영 문서 기준선: `BUILD_VERIFICATION.md`의 최신 섹션은 2026-08-02 RC117/RC55 기록과 최신 A 설치를 가리키지만, `OPERATOR_ACCEPTANCE_CHECKLIST.md`, `GATE5_IMPLEMENTATION.md`, `WEB_POC_VERIFICATION.md`는 날짜가 명시된 과거 시험 기록으로 구분했다. `README.md`·`RELEASE_OPERATIONS.md`·Goal prompt의 현재 문장 불일치는 `LUNA-0001`에 이미 포함했고 별도 finding은 만들지 않았다.
- Kiosk PC 초기 pairing 신뢰 경계: release/threat 문서의 같은 private Wi-Fi·지정 PC 전제와 recovery 후보의 private subnet 제한은 확인했지만, 초기 `PcReceiverPairing.decode()`/Python `Pairing`은 host 형식만 검사하고 저장된 host로 직접 연결한다. 초기 QR의 public/DNS/loopback/가짜 receiver identity 검증 공백은 `LUNA-0034` P3 후보로 기록했다. 실제 QR·endpoint·네트워크는 사용하지 않았다.
- Kiosk PC 상태 보고 queue 경계: `pcControlExecutor`는 무제한 single-thread queue이고 status task가 CSV fetch·self-test와 worker를 공유하며, status lambda의 최신값 coalescing·captured name cleanup·shutdown queue 처리 계약은 정적으로 확인되지 않았다. PC receiver inbound 자원 문제 `LUNA-0007`과 다른 Kiosk outbound backlog·starvation 경계로 `LUNA-0033` P3 후보를 기록했다.
- Kiosk session/recovery 경계: `MainActivity`의 Web 결과·Web recovery·QR/수동 학생 검증 callback, `StudentRepository.transitionSession()`의 expected-state transaction, `applyRestartPolicy()` 및 `AdminUiAsyncStateTest`/repository 계측시험을 대조했다. 선택 UI의 class/student generation은 stale 선택을 거부하지만 session ID/launch generation 없는 일부 결과·종료 refresh/read failure·recreated recovery action은 각각 `LUNA-0005`·`LUNA-0026`·`LUNA-0027`에 이미 포함되므로 중복 ID를 만들지 않았다.
- Web POC lifecycle 경계: `WebPocState` restart policy, `MainActivity`의 `transition()` commit/fail-closed, login/logout/active generation guard, renderer terminate/recreate, network monitor, `onStop()`/`onDestroy()` secret·WebView cleanup과 관련 JVM/계측시험·설계 문서를 대조했다. 정적 흐름은 기존 정책과 일치했고, network pause의 완전 차폐·IME/접근성, remote-support 민감 화면, proxy process-wide budget은 각각 `LUNA-0016~0021/0029`에 포함되어 새 finding으로 만들지 않았다.
- Web SPA 학생 상태 map 경계: `applyStudentExperience()`의 전역 `problemStates` 생성·문항 번호별 쓰기/읽기, `navigateStudentSection()`의 SPA anchor click, `MainActivity` monitor, 단일 fixture 시험과 SPA/답안 정정 문서를 대조해 `LUNA-0038` P3 후보를 기록했다. 실제 서로 다른 과제·답안·SPA 전환은 미검증이다.

이는 프로그램 기능이 정상이라는 의미가 아니며, 해당 영역의 정적·실기 감사가 완료됐다는 뜻도 아니다.

## 7. 미검증과 사용자 개입

- 실제 A 관리자 화면은 06:53의 승인된 짧은 캡처 한 회로 확인했고 `LUNA-0012`의 배지/헤더 겹침을 기록했다. QR 대기·로그인·문제·결과 UI와 관리자 화면의 다른 상태는 아직 확인하지 않았다.
- Web의 실제 공개 로그인 form·portal DOM·MathQuill 렌더링·답안 제출·결과 카드·로그아웃은 네트워크/세션을 변경하므로 실행하지 않았다.
- `LUNA-0016`~`LUNA-0019`의 실제 네트워크 단절·답안 화면 차폐 가시성·수식 입력 포커스/IME·일반 키 routing·TalkBack/접근성 action·ConnectivityManager callback 등록 실패·유휴 ACTIVE fallback은 확인하지 않았다. 정적 레이아웃·Activity·제품 결정·테스트 검색만 수행했다.
- Web POC의 네트워크 pause 중 background/foreground·Activity 재생성·callback 해제/재등록 순서와 화면 복귀는 실행하지 않았다. `transition()`·`onStop()`·`onDestroy()`의 정적 경계만 확인했다.
- PIN·시험 QR·학생 세션·답안 기능은 라이브 변경 금지 경계를 지키기 위해 아직 조작하지 않았다.
- 빌드·lint·테스트·APK 설치·패키지 검사는 계약상 금지되어 미실행이다.
- 실제 광학 QR 인식, 프린터·종이·거치대, 전원·재부팅·USB 승인 상태 변경은 이번 감사 범위에서 수행하지 않는다.
- `LUNA-0005`의 실제 타이밍 재현과 QR 승인·PIN·수동 선택 조합, S1 종료·S2 재시작 사이 in-flight 검증 결과의 도착 순서는 세션 상태를 변경하므로 수행하지 않았다.
- `LUNA-0006`의 실제 발급·재발급·렌더 실패와 Room/ART heap 상태는 데이터·QR 상태를 변경할 수 있어 검증하지 않았다.
- `LUNA-0023`의 실제 renderer 반복·예외·ART heap/GC 생존, Bitmap erase 뒤 QR 픽셀 재구성 가능성은 확인하지 않았다. 정적 `BitMatrix`·`IntArray` 수명과 기존 Bitmap cleanup만 대조했다.
- `LUNA-0024`의 실제 이름/소속 변경 뒤 CSV·QR 재발급·자격정보 변경·비활성화·반 생성과 실행취소 버튼의 교차 순서, 빠른 재진입·refresh callback 순서는 확인하지 않았다. `MainActivity`의 action 호출·clear 경계와 문서/시험 검색만 정적으로 대조했다.
- `LUNA-0025`의 실제 수업 시작 뒤 세션 관리자 PIN 재진입·pending 반 소속 실행취소·QR 스캔/수동 선택 eligibility 변화를 확인하지 않았다. 학생·반·세션·QR·DB 상태와 Activity timing을 사용하지 않고 정적 소스·시험·현장 시나리오만 대조했다.
- `LUNA-0026`의 실제 `endSession()` 성공 뒤 `refreshAdminData()` DB read failure, 이전 `currentSession` 표시, 종료 직후 다음 수업 재시작 가능 여부는 확인하지 않았다. 저장소 fault·실제 세션·DB·Activity 재생성을 사용하지 않고 정적 소스·RC18 시험 문서만 대조했다.
- `LUNA-0027`의 실제 Web recovery 중 Kiosk Activity recreation/process reclaim, ActivityResult 재전달, `StartSession`/`EndSession` 후속 DB 전이는 확인하지 않았다. Web·Kiosk 상태·학생·세션·DB를 사용하지 않고 정적 source·시험·운영 문서만 대조했다.
- `LUNA-0007`·`LUNA-0008`의 연결 flood, 대량 유효 frame, replay와 부하 영향은 네트워크·파일·설정 상태를 사용하므로 검증하지 않았다.
- `LUNA-0009`의 실제 Windows toast·작업표시줄·잠금 화면 노출은 수신기 실행과 학생 PDF 전송이 필요하므로 확인하지 않았다. 코드 payload의 이름 포함은 정적으로 확인했다.
- `LUNA-0010`의 ACK 유실·Kiosk 후속 delivered 상태 실패·receiver 설정 저장 실패·동일 preview 재시도·중복 destination 생성은 네트워크·PDF·PC 파일/DB 상태를 사용하므로 검증하지 않았다.
- `LUNA-0011`의 장기 audit row 증가·DB 용량·Kiosk/Web diagnostic file age·retention cleanup은 A DB나 장기 운전을 사용하므로 검증하지 않았다.
- 06:53 캡처에서 현재 A 화면이 관리자 패널임은 확인했지만, Lock Task NONE이 실제 관리자 잠금 해제 상태인지와 다른 상태의 화면은 확인하지 않았다.
- 원격 지원은 만료형 15분 사이클을 한 번 활성화해 화면을 확인한 뒤 즉시 중지했다. 실제 만료·재부팅·SharedPreferences 복구·시간 경과 전환은 실행하지 않았다.
- `LUNA-0020`의 원격 지원 활성 상태에서 Kiosk 관리자 PIN·세션 관리자 인증·Web POC username/password setup·relock·Activity 재생성 뒤 `FLAG_SECURE`가 자동 복원되는지와 캡처 시 민감 입력이 노출되는지는 확인하지 않았다. 문서의 운영자 주의와 정적 코드만 대조했다.
- `LUNA-0021`의 Kiosk/Web SharedPreferences commit false·프로세스 종료·저장공간 오류·브로드캐스트 전달 후 반대 앱 저장 결과와 실제 FLAG_SECURE/Web debugging divergence는 확인하지 않았다.
- `LUNA-0022`의 Lock Task false 반환·예외·PINNED/Activity lifecycle 경합, `DISALLOW_CREATE_WINDOWS` 잔류 여부와 그 직후 QR/인증/카메라 흐름은 확인하지 않았다. A의 현재 `NONE`은 관리자 화면의 의도적 잠금 해제와 구분하지 못하므로 재현 근거로 사용하지 않았다.
- Kiosk/Web의 background/foreground·Activity 재생성·관리자 relock·QR scanner 재개·공유 PDF cleanup을 실제로 실행하지 않았고, `LUNA-0020`의 remote support 활성 Kiosk PIN/Web 자격정보 화면 조합도 확인하지 않았다. 소스 lifecycle과 기존 테스트 소스만 대조했다.
- Android UI 정적 대조와 관리자 화면 한 회에서 주요 버튼·입력·상태 오버레이와 QR/카메라 아이콘 설명을 확인했고 `LUNA-0012` 겹침을 기록했다. TalkBack focus·글자 크기·회전 가독성은 확인하지 않았다.
- `LUNA-0013`의 실제 ART heap 내 `String` 생존 시간·GC 후 잔류 여부는 측정하지 않았다. 정적 parser의 immutable copy 생성과 wipe 불능 경계만 확인했다.
- `LUNA-0031`의 실제 Activity 종료·재생성 중 CSV parse/preview/apply queue 제거·submission rejection과 parsed row `CharArray` heap 생존 시간은 확인하지 않았다. `onDestroy()`·일반 executor 람다·`SensitiveTask` 연결만 정적으로 대조했다.
- `LUNA-0032`의 실제 후속 행 검증 실패·partial row `CharArray` zeroize·ART heap 생존 시간은 확인하지 않았다. `mapIndexed` 생성 순서와 parser catch 호출부만 정적으로 대조했다.
- `LUNA-0033`의 실제 PC unreachable·endpoint recovery 지연 중 반복 status 제출, queue 길이·stale 상태 순서, 학생 표시명 heap 보유, CSV fetch/self-test starvation과 Activity shutdown queue 처리는 확인하지 않았다. `pcControlExecutor` 제출·공유 worker·timeout·`shutdownNow()`만 정적으로 대조했다.
- `LUNA-0041`의 실제 PC CSV 선택·취소·교체·fetch/send exception·shutdown 뒤 immutable bytes/plaintext/ciphertext heap 생존과 crash/minidump 회수 가능성은 확인하지 않았다. PC app/server/protocol owner와 Kiosk CSV schema·cleanup 시험 소스만 읽었다.
- `LUNA-0034`의 실제 fake/public/DNS/loopback/multicast pairing QR, operator display name 확인, initial endpoint connect와 QR 카드 PDF/status 전송은 확인하지 않았다. 초기 pairing host 검증과 recovery private subnet 검사의 적용 범위만 정적으로 대조했다.
- `LUNA-0035`의 실제 preflight 중 반 소속·반 삭제·보강·학생/CSV/QR mutation과 Web recovery success/cancel, mutation 성공·실패 callback 순서, temporary selection 변경, Activity recreation, button enabled matrix와 pending StartSession 재검증은 확인하지 않았다. `webRecoveryGate` 참조·관리자 control 조건·repository transaction·관련 시험/운영 문서만 정적으로 대조했다.
- `LUNA-0036`의 실제 student mutation과 batch QR 확인/제출 순서, deactivation·profile·single QR·CSV·membership 성공/실패 뒤 batch task 및 PrintManager callback, 인쇄 대기열·Activity destroy·bitmap cleanup은 확인하지 않았다. `studentMutationGate`·batch button 조건·repository transaction·관련 시험/운영 문서만 정적으로 대조했다.
- `LUNA-0037`의 실제 pending-card PDF 발급·export·PC ACK·`markCardsDelivered()` 순서와 ACK 지연 중 deactivation·profile·single QR·CSV·membership mutation, issued QR revision/`needsPrint` binding·PC 파일·bitmap cleanup은 확인하지 않았다. `preparePendingCardsPdf()`·repository·`PcPdfSender`·관련 시험만 정적으로 대조했다.
- `LUNA-0038`의 실제 학습지/진단평가 SPA 전환, 과제 A→B 같은 문항 번호 상태 상속, React remount/full reload 여부, 답안 현황·`다음 미입력` 오판과 서버 저장 답안 영향은 확인하지 않았다. `WebDomScripts`·MainActivity·관련 시험·운영 문서만 정적으로 대조했다.
- `LUNA-0039`의 실제 Android `PrintManager` success/cancel/failure, physical print completion, pending UI status and reissue behavior were not checked; only source/tests/docs read.
- `LUNA-0040`의 실제 공식 답안 `unanswered` 저장, 완전 로그아웃, 같은 과제 재진입, 60초 지연 복원 여부와 Kiosk `RESULT_OK` 연결은 확인하지 않았다. Web/Kiosk source·local cleanup 시험·최신 운영 정정 문서만 정적으로 대조했다.
- `LUNA-0005`의 S1 종료·S2 시작 사이 QR/수동/Web 결과 지연 도착, `LUNA-0026`의 종료 transaction 뒤 refresh/read fault, `LUNA-0027`의 Activity recreation 중 Web recovery 결과 재전달은 확인하지 않았다. 단일 executor·expected-state·selection generation과 관련 시험 소스만 읽었다.
- Web POC의 실제 network loss/pause 차폐·WebView 포커스/IME/접근성, renderer fault/recreate on A, remote-support 활성 민감 화면/SharedPreferences ack, loopback proxy 연결·idle/thread/FD 부하는 확인하지 않았다. JVM/계측시험·정적 lifecycle·설계/위협 문서만 읽었다.
- `LUNA-0014`의 crypto/provider·allocation 예외, PDF/control plaintext·filenameBytes와 파생 key의 실제 ART heap 생존 시간은 측정하지 않았다. 정상 성공 경로의 wipe와 예외 경로의 코드상 누락만 확인했다.
- Kiosk Room 1→2·2→3 migration의 실제 기존 DB upgrade, migration 실패·중단·재시도, audit row 증가·cleanup·저장공간 부족과 `applyRestartPolicy()`의 실제 프로세스 재시작/알 수 없는 state는 확인하지 않았다. schema JSON·migration 소스·계측시험 소스·정적 recovery policy만 읽었다.
- credential bridge의 실제 Activity/ContentProvider 교차 호출, CursorWindow/Binder 복사와 WebView 로그인 JS 문자열의 ART heap 생존 시간은 확인하지 않았다. 정적 one-time·permission·caller·revoke 경계와 문서화된 임시 String 잔여위험만 대조했다.
- Kiosk 프로세스 재시작·Web 결과 지연 도착·동일 QR 재사용/재발급·수동 선택 callback 순서 재현은 실행하지 않았다. 현재 소스의 expected-state 트랜잭션·현재 session 재검증과 기존 `LUNA-0005` 후보의 stale callback 조건만 정적 대조했다.
- `LUNA-0011`의 실제 audit row/file age·DB 크기·장기 운전과 저장공간 압박은 확인하지 않았다. `PrivateDiagnosticLog`의 정적 파일 회전과 기간 cleanup 부재만 확인했다.
- 현재 사용자 개입 요청은 없다. 라이브 변경 없이는 확인할 수 있는 항목은 `라이브 변경 금지로 미검증`으로 남긴다.

## 8. Sol·사용자 재판단 큐

- `LUNA-0002`: PC 수신기 pairing secret을 DPAPI/Credential Manager로 보호할지, 기존 설치 pairing을 유지하는 마이그레이션·회전 정책을 정할 것.
- `LUNA-0003`: CSV fetch를 delivery confirmation/재시도 가능한 transaction으로 바꿀지, 중복 적용 방지와 queue 보존 UX를 정할 것.
- `LUNA-0004`: CSV preview/import의 학생별 username 복호화 실패에서도 앞선 임시 배열을 모두 zeroize하도록 정리 구조와 회귀 검증을 정할 것.
- `LUNA-0005`: QR 승인 후 관리자/수동 선택 진입 시 pending callback을 취소하고 QR·수동 launch에 공용 single-flight/stale failure guard를 둘지, 순서별 회귀 검증을 정할 것.
- `LUNA-0006`: QR 단건 발급·재발급, CSV 신규 학생, 비활성화와 batch PDF 예외에서 hash 배열 ownership·zeroize 계약을 repository/API 수준으로 통합할 것.
- `LUNA-0007`: PC receiver 인증 전 연결과 status event queue의 thread/socket/메모리 상한·timeout·rate limit·coalescing을 정하고 Private LAN 부하 검증을 추가할 것.
- `LUNA-0008`: 5분 timestamp window와 replay ID cache 용량/expiry를 일치시키고 cache 초과·재시작·동시 재전송 검증을 추가할 것.
- `LUNA-0009`: Windows tray 알림을 이름 없는 고정 문구로 분리하고, PDF/status/CSV/error 알림 payload에 학생 이름·자격정보·QR 원문이 없는지 검증할 것.
- `LUNA-0010`: PDF commit·ACK·후속 delivered 상태 반영·재시도에 stable delivery ID/hash idempotency 또는 상태 재조회/rollback 프로토콜을 추가해 중복 파일과 상태 불일치를 막을 것.
- `LUNA-0011`: audit와 Kiosk/Web diagnostic log의 보존 목적을 구분하고 행 수·기간·용량 상한과 cleanup 실행/실패 정책을 정할 것.
- `LUNA-0012`: 원격 점검 배지를 헤더 상태와 겹치지 않는 예약 영역으로 옮기고 관리자·QR 대기·Web/학생 상태에서 실제 bounds 겹침을 재검증할 것.
- `LUNA-0013`: CSV parser를 `String` 생성 없이 wipe 가능한 buffer 중심으로 바꾸거나 immutable intermediate ownership/zeroization을 정의하고 malformed·preview cancel·failure regression을 정할 것.
- `LUNA-0031`: CSV preview/apply의 parsed row ownership을 `SensitiveTask`/pending owner registry로 통합하고, Activity shutdown·executor rejection·queued discard·preview cancel/apply failure에서 정확히 한 번 wipe되는 lifecycle 회귀시험을 정할 것.
- `LUNA-0032`: CSV parser의 partial row owner와 실패 cleanup contract를 명시하고, 후속 행 validation·duplicate·quoting 오류에서 이미 생성된 credential 배열을 zeroize하는 회귀시험을 정할 것.
- `LUNA-0033`: Kiosk best-effort status와 CSV/self-test 기능성 control을 분리하거나 bounded/latest-state queue·generation cancellation을 도입할지, offline 반복·recovery·공유 worker starvation·Activity 종료의 순서·민감 표시명 cleanup 회귀시험을 정할 것.
- `LUNA-0034`: 초기 pairing host를 RFC1918/private로 제한할지, non-RFC1918/hostname 배포를 어떻게 호환할지와 receiver identity attestation·operator verification 정책을 정하고, fake/public/DNS/loopback/multicast QR 및 no-external-connect 회귀시험을 정할 것.
- `LUNA-0035`: Web recovery·Start/End 전이 중 반/보강/학생/CSV/QR mutation을 공통 `adminOperationGate`로 차단할지, mutation revision 변경 시 pending `StartSession`을 취소하고 최신 class/membership/temporary snapshot을 재검증할지 정한 뒤 preflight 전후·성공/실패·Activity recreation·button matrix 회귀시험을 정할 것.
- `LUNA-0036`: student mutation·class membership·bulk QR issuance/print를 공통 operation gate/generation으로 묶고 mutation revision 변경 시 pending cards와 PrintManager 호출을 취소할지, deactivation·profile·single QR·CSV·membership와 batch의 선후/성공·실패/인쇄 callback 회귀시험을 정할 것.
- `LUNA-0037`: pending-card QR issuance·PDF export·PC ACK·`markCardsDelivered()`를 student/class mutation과 공통 delivery generation으로 묶을지, issued QR revision/request ID를 delivered 상태에 연결하고 ACK 전후 invalidation·재생성 회귀시험을 정할 것.
- `LUNA-0038`: Web SPA 과제/경로 변경 시 문제 상태 map을 reset하거나 stable task key/generation으로 격리할지 정하고, 과제 A→B→A·학습지↔진단평가·문항 수/번호 충돌·지연 React mount·현황/미입력 이동 회귀시험을 추가할 것.
- `LUNA-0039`: Android 직접 단건·반 전체 PrintManager 경로의 `needsPrint`를 요청·물리 출력 확인·전달 상태로 분리할지, 의도적으로 pending을 유지한다면 수동/검증된 해결 UX를 둘지 정하고 success/cancel/failure·pending UI·재발급 회귀시험을 추가할 것.
- `LUNA-0040`: Web 종료 성공을 공식 답안 `unanswered` 저장·완전 로그아웃·같은 과제 재진입·60초 지연 복원 부재와 묶을지, 서버 ack/운영자 확인 전에는 Kiosk `RESULT_OK`를 허용하지 않을지 정하고 answer lifecycle 회귀시험을 추가할 것.
- `LUNA-0041`: PC receiver가 CSV credential payload를 mutable owner로 보유하고 취소·교체·fetch/send exception·shutdown에서 best-effort zeroize할지, Python/cryptography 내부 복사본과 원본 CSV 파일 보존의 허용 범위를 정한 뒤 합성 CSV owner-cleanup 회귀시험을 추가할 것.
- session/recovery 공통: `LUNA-0005`의 session ID/launch generation binding, `LUNA-0026`의 종료 후 UI snapshot invalidation/read-failure 상태, `LUNA-0027`의 saved recovery action/idempotent 후속 전이를 통합할지 정하고 S1 종료·S2 시작·Activity recreation·DB read fault 회귀시험을 정할 것.
- Web POC lifecycle 공통: network pause를 불투명·입력/접근성까지 차폐하고, remote-support 민감 화면/양 앱 acknowledgement와 proxy resource budget을 명시할지 정한 뒤 A renderer·network·Activity·원격지원 실기와 회귀시험을 연결할 것. 기존 `LUNA-0016~0021/0029` 후보와 중복 집계하지 않는다.
- `LUNA-0014`: PDF filename buffer까지 포함해 PDF/control encode와 ACK verify의 plaintext·파생 key ownership을 `try/finally`로 통합하고 crypto 예외 cleanup regression을 정할 것.
- `LUNA-0023`: QR renderer의 `BitMatrix`·픽셀 `IntArray` ownership을 명확히 하고 반환·예외·batch 반복 경로에서 zeroize 또는 안전한 회수 계약과 heap/cleanup 회귀시험을 정할 것.
- `LUNA-0016`: 네트워크 대기 패널을 완전 불투명·검증 가능한 답안 차폐로 만들지, WebView를 별도 opaque blocker로 가릴지 정하고 실제 고대비 답안/수식 화면 회귀시험을 추가할 것.
- `LUNA-0017`: 네트워크 pause 진입 시 WebView 포커스·IME·접근성·하드웨어 키를 일괄 차단하고, 검증된 복구 시에만 입력을 복원하는 상태 계약과 수식 입력 회귀시험을 정할 것.
- `LUNA-0018`: `registerDefaultNetworkCallback()` 실패·ConnectivityManager null에 대한 bounded 재등록 또는 validated-network watchdog과 안전한 실패 정책을 정하고 callback fault 회귀시험을 추가할 것.
- `LUNA-0019`: network pause 진입 시 WebView 접근성 subtree를 숨기고 안내 패널만 focus 대상이 되도록 정의한 뒤 TalkBack/접근성 action 회귀시험을 추가할 것.
- `LUNA-0020`: Kiosk 관리자 PIN·Web POC 자격정보 setup 화면 진입 시 remote support를 자동 일시중지하거나 `FLAG_SECURE`를 복원하고, 민감 화면 종료 후 재승인하는 정책·회귀시험을 정할 것.
- `LUNA-0021`: 양쪽 RemoteSupportStore commit 결과와 cross-app acknowledgement를 확인하고, 활성/종료 저장 실패 시 양쪽 모두 fail-closed가 되는 재시도·rollback 정책과 회귀시험을 정할 것.
- `LUNA-0022`: Lock Task 진입 결과가 `LOCKED`가 아니면 false를 failure/재시도 상태로 전파하고 `DISALLOW_CREATE_WINDOWS`를 정리할지, QR·인증·카메라 흐름을 차단할지와 false·예외·PINNED·Activity 재생성 회귀시험을 정할 것.
- `LUNA-0024`: CSV·QR 재발급·자격정보 변경·비활성화·반 생성 등 실행취소 불가 작업의 시작/성공 시 이전 pending undo를 지울지, action generation·최신 snapshot 검증으로 교차 작업 실행취소를 차단할지 정하고 연속 변경 회귀시험을 추가할 것.
- `LUNA-0025`: 활성 세션 중 반 membership mutation·undo를 거부하거나 세션 시작 시 pending undo를 폐기하고, QR·수동 선택 eligibility를 immutable session snapshot으로 고정할지 정한 뒤 active-session undo·QR·수동 선택 회귀시험을 추가할 것.
- `LUNA-0026`: `endSession()` 성공 결과를 Activity에 즉시 반영하거나 종료 전용 refresh-failure/retry 상태를 두고, 종료 성공·목록 read failure·다음 수업 시작 순서의 회귀시험을 추가할 것.
- `LUNA-0027`: Web recovery action을 saved state/DB operation record와 idempotent token으로 보존할지 정하고, Kiosk Activity recreation·결과 중복·Start/End 후속 전이 회귀시험을 추가할 것.
- `LUNA-0001`: README의 현재 릴리스·A 설치 버전 표기를 실제 기준선에 맞출지 결정할 것.

## 9. 권장 처리 순서

1. 현재 source version, artifact 이름/manifest, 누적 설치 기록과 문서의 RC/versionCode 불일치를 정적 근거로 교차 확인한다.
2. 최근 변경된 Web POC 문제 전환·답안 상태·키패드 경로의 코드·테스트 대조를 완료 기준으로 재확인하고, 실제 DOM 검증은 별도 승인된 읽기 전용 범위로 남긴다.
3. Kiosk의 인증·DB·Web 준비/복구 상태 전이와 늦은 callback·중복 실행 경계를 검토하고 `LUNA-0005`·`LUNA-0006`의 QR/PDF 인접 흐름을 이어서 대조한다.
4. PC receiver 설정 secret at-rest 결함과 CSV fetch 실패 복구의 범위를 확정하고, `LUNA-0007`·`LUNA-0008`의 재생·동시성·민감정보 로그 경계를 추가 검토한다.
5. 보안 경계와 Kiosk/Web exported component·로그·저장 경계를 대조한다.
6. PC 알림·로그·파일명과 제품 개인정보 결정을 대조하고 `LUNA-0009`의 수정 판단을 남긴다.
7. PC PDF commit·ACK·재시도·replay·동시성 경계를 대조하고 `LUNA-0010`의 후보 조건을 좁힌다.
8. Kiosk audit와 Kiosk/Web diagnostic retention·DB 성장 및 cleanup 연결을 정적 대조하고 `LUNA-0011`의 정책 전제를 확인한다.
9. 테스트·운영 문서의 실제 연결과 검증 공백을 대조한다.
10. 정적 근거가 부족할 때만 A 화면을 승인된 짧은 읽기 전용 사이클로 확인한다. 관리자 화면 1회는 완료했으며, 다른 상태가 꼭 필요할 때만 같은 범위로 추가 확인하고 답안·제출·데이터 변경은 하지 않는다.
11. Web POC 네트워크 pause의 차폐·입력·감시 실패 경계를 코드·레이아웃·제품 결정·테스트 연결과 대조하고, 라이브 네트워크 단절은 계약상 실행하지 않은 상태로 후보를 유지한다.
12. Kiosk 원격 지원의 `FLAG_SECURE` 예외를 관리자 PIN·relock·Activity 생명주기별로 대조하고, 라이브 PIN·QR·캡처는 계약상 실행하지 않은 상태로 후보를 유지한다.
13. Kiosk/Web 원격 지원 저장·broadcast acknowledgement와 commit 실패 경계를 정적 대조하고, 실제 원격 지원·설정 쓰기는 계약상 실행하지 않은 상태로 후보를 유지한다.
14. 전용기기 Lock Task false/예외·restriction rollback·화면 진행 경계를 정적 대조하고, 실제 시스템 fault와 화면 변경은 계약상 실행하지 않은 상태로 `LUNA-0022` 후보를 유지한다.
15. QR renderer의 `BitMatrix`·픽셀 임시 배열이 Bitmap cleanup과 별도 수명을 갖는지 정적 대조하고, 실제 QR·heap·인쇄 실행 없이 `LUNA-0023` 후보를 유지한다.
16. Kiosk Room migration·audit cleanup·재시작 recovery의 실제 upgrade/장기 운전/저장공간 압박은 계약상 실행하지 않은 상태로, `LUNA-0011`과 기존 recovery 경계를 유지하고 관련 migration·retention 회귀시험을 다음 검토 큐에 둔다.
17. Kiosk 관리자 학생·반 mutation의 단일 실행·선택 세대·실행취소 action 수명을 정적 대조하고, 실제 학생/CSV/QR/자격정보 변경 없이 `LUNA-0024`의 교차 작업 실행취소 후보와 회귀시험 필요성을 유지한다.
18. Kiosk 활성 세션 중 반 소속 실행취소가 UI 차단과 repository 검사를 우회해 QR·수동 선택 eligibility를 바꿀 수 있는지 정적 대조하고, 실제 세션·학생·QR 변경 없이 `LUNA-0025` 후보와 active-session 회귀시험 필요성을 유지한다.
19. Kiosk QR·수동 검증의 session snapshot/query/transition 세대가 수업 종료·재시작을 가로지를 때 이전 학생이 새 세션으로 launch되지 않는지 정적 대조하고, 실제 세션·Web·학생·QR 변경 없이 `LUNA-0005` 회귀시험 필요성을 유지한다.
20. Kiosk `endSession()` 성공 뒤 `refreshAdminData()` read failure가 이전 active UI를 유지하는지 정적 대조하고, 실제 종료·DB fault·다음 수업 시작 없이 `LUNA-0026` 후보와 종료 복구 회귀시험 필요성을 유지한다.
21. Kiosk Web recovery 중 Activity 재생성 시 `pendingRecoveryAction`이 보존되어 Start/End 후속 DB 전이가 한 번만 실행되는지 정적 대조하고, 실제 Web·Activity·session 변경 없이 `LUNA-0027` 후보와 lifecycle 회귀시험 필요성을 유지한다.
22. 초기 PC pairing QR의 host·display name·secret이 지정 private Wi-Fi와 receiver identity를 실제로 보장하는지 정적 대조하고, fake/public/DNS/loopback/multicast QR과 호환성 정책을 Sol·사용자 재판단 큐에 둔 채 `LUNA-0034` 후보를 유지한다.
23. Kiosk PC status reporting의 무제한 단일 executor queue와 CSV/self-test 공유 worker를 정적 대조하고, 실제 PC offline·network recovery·queue starvation·Activity 종료 없이 `LUNA-0033` 후보와 bounded/coalescing 회귀시험 필요성을 유지한다.
24. Kiosk session/recovery 결과의 session ID·launch generation·expected-state·Activity lifecycle 연결을 기존 `LUNA-0005`·`LUNA-0026`·`LUNA-0027`과 함께 다시 대조하고, 실제 S1/S2·DB fault·Activity recreation 없이 새 독립 finding 여부를 판정한다.
25. Web POC state/renderer/network/remote-support lifecycle의 정적 guard와 JVM·계측시험 연결을 기존 `LUNA-0016~0021/0029`와 대조하고, 실제 A network·renderer·Activity·원격지원 조작 없이 새 독립 finding 여부를 판정한다.
26. Kiosk pre-session Web recovery/admin mutation gate와 pending `StartSession` action revision·snapshot 재검증을 정적 대조하고, 실제 반/학생/CSV/QR mutation·Web recovery·수업 시작·Activity recreation 없이 `LUNA-0035` 후보와 gate coverage 회귀시험 필요성을 유지한다.
27. Kiosk student mutation gate와 반 QR 일괄 재발급·PrintManager callback의 cross-operation generation/최신 roster 재검증을 정적 대조하고, 실제 학생/반/QR/인쇄 변경 없이 `LUNA-0036` 후보와 pending card cancellation 회귀시험 필요성을 유지한다.
28. Kiosk pending-card QR/PDF delivery·`markCardsDelivered()`와 student mutation invalidation/issued revision binding을 정적 대조하고, 실제 PC PDF·ACK·학생/반/QR 변경 없이 `LUNA-0037` 후보와 delivery cancellation 회귀시험 필요성을 유지한다.
29. Web SPA 과제 전환에서 `problemStates`가 pathname·task key 없이 문항 번호만 보유하는지, 실제 답안·과제·A 변경 없이 `LUNA-0038` 후보와 상태 reset 회귀시험 필요성을 유지한다.
30. Kiosk Android 직접 단건·반 전체 PrintManager 경로의 `needsPrint` 전이와 외부 프린터 결과 semantics를 정적 대조하고, 실제 인쇄·QR·A·데이터 없이 `LUNA-0039` P4 후보와 pending UI/수동 해결 회귀시험 필요성을 유지한다.
31. Web `finishLogoutVerification()`·local storage cleanup·`sanitizeLoginAndFingerprint`가 서버 임시답안 cleanup·같은 과제 재진입 검증 없이 Kiosk `RESULT_OK`를 반환하는지 정적 대조하고, 실제 답안·서버·A 변경 없이 `LUNA-0040` P3 후보와 persisted cleanup 회귀시험 필요성을 유지한다.
32. PC receiver의 CSV 선택·`pending_csv`·control response encryption·clear/shutdown owner를 정적 대조하고, 실제 CSV·receiver·heap dump 없이 `LUNA-0041` P4 후보와 volatile buffer zeroization 회귀시험 필요성을 유지한다.

## 10. 누적 변경 기록

### 2026-08-02 05:38:16

- Goal 계약 전체와 현재 대화의 `AGENTS.md` 지침을 확인했다.
- 지정된 `생성 파일 검토 및 역질문` 작업을 읽기 전용으로 확인했다.
- 저장소 필수 문서, Git 기준선, Windows 대화형 상태와 ADB A 연결을 확인했다.
- 보고서가 없어 이 파일을 계약에 따라 생성했다.
- 코드·문서·설정·스크립트·산출물·진단 자료·기기 데이터에는 변경을 가하지 않았다.

### 2026-08-02 05:50:38

- `pc_receiver`의 Python 프로토콜·서버·설정, Kiosk PC 전송 클라이언트·테스트를 작은 범위로 읽기 전용 대조했다.
- AES-GCM/HMAC, 5분 timestamp window, request ID replay 기록, PDF/CSV 크기·파일명 경계를 확인했다.
- `ConfigStore`가 256비트 pairing secret을 JSON Base64로만 저장하는 `LUNA-0002`를 확정하고, 실제 설정 파일은 원문 없이 메타데이터·ACL만 확인했다.
- 이 보고서만 `apply_patch`로 갱신했으며 소스·설정·테스트·산출물·Git 상태·기기 데이터는 변경하지 않았다.

### 2026-08-02 05:53:00

- `pc_receiver` CSV fetch의 queue 소비·replay 기록·socket `sendall` 순서와 Kiosk의 실패/파싱 경계를 읽기 전용으로 대조했다.
- 전송·수신·파싱 실패 뒤 `pending_csv`를 복구하거나 재시도 token을 제공하지 않는 `LUNA-0003`을 확정했다.
- 이 보고서만 `apply_patch`로 갱신했으며 소스·설정·테스트·산출물·Git 상태·기기 데이터는 변경하지 않았다.

### 2026-08-02 06:00:00

- Kiosk Manifest/exported component, backup exclusion, PIN/Keystore/Room 경계와 QR·CSV repository 경로를 읽기 전용으로 검토했다.
- CSV `existing.map { decrypt(username) }` 중간 예외가 앞선 username `CharArray` 정리 `finally`보다 먼저 전파되는 `LUNA-0004`를 확정했다.
- PIN 원문·학생 데이터·DB·Keystore에는 접근하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:04:01

- Kiosk QR 승인 지연 callback, 스캐너 관리자 버튼, 수동 학생 선택 callback과 `PreparedWebSessionPolicy` 테스트를 읽기 전용으로 대조했다.
- QR generation이 관리자/수동 선택 경로와 공유되지 않고, 수동 launch가 별도 지연 callback으로 예약되는 `LUNA-0005` 후보를 기록했다. 단일 I/O executor와 expected-state 검사가 실제 중복 성공을 제한할 수 있어 후보로 유지한다.
- QR·PIN·학생 세션 조작, 빌드·테스트는 수행하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:08:09

- QR token codec/analyzer/gate, QR bitmap renderer, PDF export/print/share/FileProvider와 Kiosk 호출부의 민감값 수명·삭제 경계를 읽기 전용으로 대조했다.
- 원본 token은 zeroize되지만 단건 register/reissue·CSV 신규 학생·비활성화 경로의 hash 회수 계약이 없고, pending batch PDF render 실패에도 미처리 hash 전체 wipe가 없는 `LUNA-0006`을 확정했다.
- QR 원문·hash·학생 데이터·DB·Keystore는 사용하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:13:45

- Web POC Manifest/MainActivity/WebSecurityPolicy/StudentWebPolicy/WebFailurePolicy와 DOM login·portal·answer·MathQuill·submit·result·logout script를 읽기 전용으로 대조했다.
- WebView 보안 설정, caller/signature 경계, 세대 기반 callback, credential/session cleanup, renderer recovery에서 정적 신규 확정 finding은 기록하지 않았다. 실제 공개 사이트·DOM·로그인·답안·로그아웃은 실행하지 않았다.
- Web 관련 테스트는 실행하지 않고 코드와 계약만 읽었으며, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:15:41

- PC receiver `server.py`/`protocol.py`/`config.py`/`app.py`와 server/app tests를 읽기 전용으로 대조했다.
- 인증 전 연결별 thread와 10초 partial read timeout에 active connection 상한이 없는 `LUNA-0007` 후보, 5분 timestamp window보다 먼저 2,048개 replay ID가 eviction될 수 있는 `LUNA-0008` 후보를 기록했다.
- 네트워크 flood·대량 frame·replay·수신기 실행은 하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:21:35

- 자동 재개 절차에 따라 작업 계약과 누적 보고서 전체를 다시 읽고, Git 상태·브랜치·HEAD·upstream과 ADB 승인 상태·A package version/device owner를 읽기 전용으로 재확인했다.
- HEAD/upstream과 A의 Kiosk `0.6.0-rc55`/code 60, Web POC `0.4.0-rc117`/code 134는 기준선과 동일했다. 기존 사용자 변경·미추적 산출물은 건드리지 않았다.
- 다음 검토 큐인 현재 기준선 문서·테스트·운영 주장 대조를 계속하며, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:22:35

- `docs/CONTINUOUS_DEVELOPMENT_GOAL_PROMPT.md`의 Kiosk rc53/code58, `docs/RELEASE_OPERATIONS.md`의 Kiosk RC34/Web RC42 현재 표기를 source·artifact·A RC55/RC117과 대조했다.
- 동일 문서 불일치 계열인 `LUNA-0001`에 위 근거를 추가하고, 최신 `CONTINUOUS_DEVELOPMENT_GOAL.md`·RC117 검증 절과 명시적 역사 기록은 오탐 방지를 위해 분리했다.
- 문서·소스·A 상태는 변경하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:25:18

- `PRODUCT_DECISIONS.md`의 Windows 알림 개인정보 결정과 Kiosk PDF filename, PC receiver의 저장 완료 `ReceiveEvent`·tray notify 연결을 읽기 전용으로 대조했다.
- 학생 표시 이름을 포함한 PDF filename이 Windows toast payload로 전달되는 `LUNA-0009`를 확정했다. 실제 PDF·학생 데이터·수신기·Windows 알림은 사용하지 않았다.
- 기존 사용자 파일·소스·테스트·PC 상태는 변경하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:27:35

- `SECURITY.md:64-65`의 QR hash 실패·완료 zeroize 정책 문구를 `LUNA-0006`의 단건·CSV·비활성화·batch 예외 경로와 다시 대조했다.
- 새 finding을 만들지 않고 `LUNA-0006`의 코드-보안정책 불일치 근거와 마지막 확인 시각만 갱신했다. 자격정보·QR hash·DB·A 상태는 사용하지 않았다.
- 소스·문서·테스트는 변경하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:29:39

- Kiosk `PcPdfSender`·PDF 전송 UI와 PC receiver의 파일 commit·ACK·replay 순서를 읽기 전용으로 대조했다.
- ACK 유실 뒤 Kiosk 실패 재시도가 새 request ID와 새 destination으로 같은 PDF를 중복 저장할 수 있는 `LUNA-0010` 후보를 기록했다. 기존 정상/replay test는 이 연결된 fault path를 다루지 않는다.
- 네트워크 단절·ACK 차단·PDF 전송·재시도·PC 파일 생성은 수행하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:32:38

- PC app의 CSV 선택 bytes가 `ReceiverState.pending_csv` 메모리에만 있고 `ConfigStore`에 보존되지 않는 경로를 `LUNA-0003`의 CSV queue 소비·응답 실패 경계와 대조했다.
- 수신기 fetch 전 재시작·종료 시 대기 작업이 사라지는 범위를 같은 finding에 포함하고, 별도 ID로 중복 집계하지 않았다.
- PC 수신기·CSV 원문·학생 데이터·네트워크 상태는 사용하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:38:26

- Kiosk `AuditEventEntity`·`AuditDao`·`StudentRepository.audit()` 호출과 제품/보안 문서의 로그 보존 기준을 읽기 전용으로 대조했다.
- production에서 호출되지 않는 `deleteOlderThan()`과 보존 상한 부재를 장기 운전·개인정보 retention 관점의 `LUNA-0011` P4 후보로 기록했다. 새 상태 전이 결함은 확인하지 않았다.
- A DB·감사 기록·장기 부하·소스/테스트 변경은 수행하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:41:43

- 허용된 읽기 전용 ADB 상태 조회로 Device Owner component, `kiosk`·`webpoc` Lock Task allowlist, 전경 Kiosk `MainActivity`와 `mLockTaskModeState=NONE`을 확인했다.
- 관리자 화면에서 `exitDedicatedModeForAdministrator()`를 호출하는 현재 코드와 대조해 Lock Task NONE을 결함으로 단정하지 않았다. 화면 캡처·PIN·QR·앱 데이터 조작은 하지 않았다.
- 소스·기기 상태·앱 데이터는 변경하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:46:07

- Kiosk `RemoteSupportStore`와 `RemoteSupportPolicy`를 작은 구간으로 처음부터 끝까지 읽어 원격 지원의 만료·boot count·duration clamp 경계를 정적 대조했다.
- 활성화 시각/boot count만 저장하고 만료·재부팅 시 비활성화하는 구조에서 새 finding은 확정하지 않았다. 실제 원격 지원 활성화·재부팅·화면·PIN은 수행하지 않았다.
- 기존 소스·기기 상태·앱 데이터는 변경하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:47:35

- Kiosk/Web `activity_main.xml`, styles/strings와 접근성 관련 속성·동적 contentDescription 연결을 읽기 전용으로 대조했다.
- 주요 조작 요소의 텍스트·hint·아이콘 설명, credential autofill 차단, 상태/잠금 오버레이는 정적으로 확인했으며 새 finding은 확정하지 않았다. 실제 clipping·TalkBack focus·글자 크기·회전 가독성은 실행하지 않았다.
- 기존 소스·문서·기기 상태는 변경하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:49:33

- Kiosk/Web private diagnostic log, ADB dump receiver, remote-support receiver/window controller와 Manifest 권한을 읽기 전용으로 대조했다.
- 진단 기록의 코드 whitelist·파일 상한·nonce/line filter 및 `DUMP`/signature 권한 경계를 확인했으며 새 민감정보 출력·권한 우회 finding은 확정하지 않았다. 실제 ADB dump·원격 지원 활성화·화면 캡처는 수행하지 않았다.
- 기존 소스·문서·기기 상태는 변경하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:52:12

- `docs/CONTINUOUS_DEVELOPMENT_GOAL.md` 최신 절의 A RC55/RC117 문장, `최신 체크포인트` 목록과 2026-08-01 RC53 설치 문장을 읽기 전용 Git HEAD/log와 다시 대조했다.
- A/source/artifact 버전 자체는 맞지만 같은 “현재” 절 안에 HEAD보다 뒤처진 checkpoint·RC53 문장이 섞인 문서 기준선 혼선을 `LUNA-0001`에 추가했다. 날짜·제목이 명시된 BUILD_VERIFICATION 역사 절은 계속 분리했다.
- 소스·문서·기기 상태는 변경하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:53:11

- Kiosk repository 계측시험, PC receiver protocol/server/app 시험 목록, release build/verification script와 운영 문서의 버전·실행 절차 연결을 읽기 전용으로 대조했다.
- 정상 상태·보안 경계 시험은 확인했지만 CSV 복호화 중간 예외, QR hash ownership, PDF ACK 유실/재시도, CSV queue 재시작, receiver 동시성·thread 상한 회귀시험은 코드에 연결되지 않은 상태로 기존 finding 근거를 보강했다. 새 finding은 추가하지 않았다.
- 빌드·lint·테스트·설치·Git 쓰기는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:54:40

- 계약이 허용한 단기 A 화면 사이클에서 사전 상태를 읽기 전용 확인한 뒤 `remote-tablet.ps1 -Action Start -Minutes 15`로 원격 지원을 활성화하고 자동 Capture 화면을 확인했다. PIN·QR·터치·학생 세션·답안·제출은 사용하지 않았다.
- `RemoteSupportWindowController`의 우측 상단 배지가 Kiosk 60dp 헤더의 상태 표시를 가리는 것을 실제 화면에서 확인해 `LUNA-0012` P3 확정으로 기록했다. `Stop`으로 즉시 원격 지원을 중지했고 임시 `A-latest.png` 삭제 및 후속 Device Owner/allowlist 확인을 완료했다.
- A 화면·앱 데이터·소스·기기는 변경하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 06:58:59

- PC receiver `app.py`의 기본 무제한 `queue.Queue()`와 server `on_event` producer 연결을 `ThreadedReceiverServer` 인증 전 thread 경계와 함께 다시 읽기 전용 대조했다.
- 동일 자원 고갈 후보인 `LUNA-0007`의 범위를 인증 전 connection뿐 아니라 pairing secret을 가진 client의 무제한 status event queue까지 명시적으로 확장했다. 별도 ID는 만들지 않았다.
- 네트워크 flood·수신기 실행·부하 시험은 하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:00:46

- `StudentCsvImport.kt`, `MainActivity.fetchStudentCsvFromPc()`, PC response decoder와 `SECURITY.md`/`GATE4_IMPLEMENTATION.md`를 다시 읽기 전용 대조했다.
- transport `ByteArray`와 row `CharArray` wipe는 확인했지만 parser가 전체 CSV·field·username set을 immutable `String`으로 만들고 이를 덮을 수 없는 경계를 확인해 `LUNA-0013` P3 확정으로 기록했다. `LUNA-0004`의 decrypt map 예외 경계와 `LUNA-0006`의 QR hash ownership과는 중복 처리하지 않았다.
- 실제 ART heap/GC 수명, CSV 데이터 출력, runtime import/preview는 확인하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:08:16

- Kiosk `PcTransferProtocol.kt`·`PcControlProtocol.kt`의 PDF/status/CSV encode, ACK HMAC verify와 관련 JVM 테스트를 읽기 전용 대조했다.
- 정상 성공 뒤 wipe는 확인했지만 `doFinal()`·`Mac.doFinal()` 또는 내부 frame allocation 예외 시 plaintext와 파생 key의 cleanup `finally`가 보장되지 않는 경계를 확인해 `LUNA-0014` P3 확정으로 기록했다. `LUNA-0004`·`LUNA-0013`의 CSV memory 경계와는 중복 처리하지 않았다.
- crypto fault injection·heap/GC 측정·PDF/status/CSV 전송은 실행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:12:19

- 테스트 소스의 정적 범위를 재확인했다: Kiosk `@Test` 표식 125개, Web `@Test` 표식 161개, PC receiver pytest 함수 11개가 존재하며 실제 test/build/lint는 계약상 실행하지 않았다.
- `BUILD_VERIFICATION.md` 최신 RC117 절과 `OPERATOR_ACCEPTANCE_CHECKLIST.md`/`GATE5_IMPLEMENTATION.md`/Web 검증 문서의 날짜·역사 범위를 대조했다. 현재 기준선과 충돌하는 README·Release 운영·Goal prompt 문장은 `LUNA-0001` 범위로 유지했다.
- 실제 PC는 설치 executable·Startup shortcut·Private inbound firewall rule이 존재하고 `config.tmp`는 없음을 메타데이터로 확인했다. 설정 원문·secret·replay ID는 출력하지 않았고 수신기 실행·전송·빌드·테스트는 하지 않았다.
- 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:18:27

- `QrTokenCodec`, QR bitmap renderer, PDF cache/export·print/share adapter와 Kiosk 호출부를 다시 읽어 token·bitmap·cache file의 정리 및 외부 공유 경계를 대조했다. 실제 인쇄·공유·외부 앱 수신은 실행하지 않았다.
- Android 공유 `EXTRA_SUBJECT`의 학생 표시 이름은 제품 문서가 명시한 사용자가 선택한 신뢰 대상 공유 경계와 일치해 신규 finding으로 만들지 않았다. 반면 PC PDF 전송 filename의 별도 `filenameBytes`가 성공 경로에도 wipe되지 않는 사실을 `LUNA-0014`에 합쳐 범위를 갱신했다.
- QR 원문·hash·학생 데이터·PDF·네트워크·외부 앱은 사용하지 않았고, 소스·기기 상태·산출물은 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:20:47

- `PrivateDiagnosticLog`의 safe-code/ADB dump/2세대 256KB 회전과 `AuditEvent` DAO 호출을 제품 결정의 크기·기간 보존 기준과 다시 대조했다. DB production cleanup 호출은 없고 private 진단 로그에도 기간 기반 cleanup은 없어 `LUNA-0011` 후보 범위를 보강했다.
- `pc_receiver ConfigStore.save()`의 `config.tmp` 생성·`os.replace()` 경계를 읽어 secret 포함 임시 파일이 저장 실패 뒤 남을 수 있는 cleanup 누락을 기존 `LUNA-0002` at-rest 범위에 합쳤다. 실제 설정 원문·secret은 출력하지 않았고 현재 `config.tmp` 부재만 메타데이터로 확인했다.
- 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다. 빌드·테스트·수신기 실행·ADB 변경은 하지 않았다.

### 2026-08-02 07:22:49

- Kiosk `PcPairingStore`, `PcReceiverPairing`, QR analyzer callback과 pairing store 계측시험 소스를 읽어 PC pairing QR의 raw 입력→Keystore 암호화 저장 경계를 대조했다.
- ciphertext/IV 저장과 sensitive byte 정리는 확인했으며 raw pairing `String`의 실제 heap 수명은 미측정으로 남겼다. raw QR을 로그·진단·평문 SharedPreferences에 남기는 정적 경로는 찾지 않아 신규 finding은 추가하지 않았다.
- QR 원문·secret·기기 데이터·저장소 원문은 사용하지 않았고, 이 보고서만 `apply_patch`로 갱신했다. 빌드·테스트·페어링 실행은 하지 않았다.

### 2026-08-02 07:25:22

- Web POC `MainActivity`, `WebSecurityPolicy`, `StudentWebPolicy`, login/logout/recovery/renderer lifecycle과 Kiosk/Web `PrivateDiagnosticLog`를 읽기 전용으로 재대조했다.
- caller/signature·WebView 저장소/Cookie clear·renderer 실패폐쇄·민감 입력 정리는 신규 확정 결함을 만들지 않았다. 다만 Web 진단 로그도 파일 크기 회전만 있고 기간 cleanup이 없어 `LUNA-0011`을 Kiosk/Web 공통 보존 정책 후보로 확장했다.
- 공개 Web·로그인·답안·로그아웃·renderer fault injection은 실행하지 않았고, 소스·기기 상태·산출물은 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:29:56

- Kiosk `PcPdfSender` 호출부와 batch/단건 `markCardsDelivered()` 후속 순서, PC receiver `accept()`의 `os.replace()`·replay·`ConfigStore.save()`·ACK 순서를 다시 대조했다.
- ACK 유실뿐 아니라 ACK 후 Kiosk delivered 상태 반영 실패, receiver 설정 저장 실패에서 commit/상태/retry 결과가 갈라지는 정적 경계를 `LUNA-0010` 후보에 합쳤다. 네트워크·PDF·DB·receiver 실행은 하지 않았다.
- 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다. 테스트·빌드·Git 쓰기는 수행하지 않았다.

### 2026-08-02 07:34:47

- `KioskLockTaskController`, Kiosk `MainActivity`의 전용기기 설정·Lock Task 진입/관리자 해제·`onStop`/`onStart` 재잠금 경로, `DedicatedDevicePolicy`·`SessionPreflightPolicy`와 관련 단위검사 소스를 읽기 전용으로 대조했다.
- Device Owner·kiosk/web allowlist·Web uninstall 보호·정책 설정 실패를 수업 시작 preflight가 차단하고, 관리자 화면에서만 `exitForAdministrator()`가 호출되며, 관리자 화면 이탈 뒤 인증 화면과 재잠금을 예약하는 구조를 확인했다. 현재 A의 `LockTask=NONE`은 관리자 패널의 의도적 해제와 일치하므로 새 finding은 추가하지 않았다.
- 다른 화면의 실제 Lock Task 진입, 회전·백그라운드·프로세스 재생성 실기와 단위검사는 실행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:35:50

- PC receiver `protocol.py`, `server.py`, `app.py`, `config.py`와 README·설치 스크립트·receiver tests를 다시 읽어 frame length, authenticated error path, file commit, replay·CSV lock, UI notification 경계를 대조했다.
- 기존 `LUNA-0007`의 인증 전 connection/thread 및 pairing-secret 보유 client의 무제한 status event queue, `LUNA-0008`의 2,048개 replay ID eviction, `LUNA-0002`의 secret-containing config storage 범위를 재확인했다. 오류 메시지에서 PDF/CSV payload·secret·request ID를 직접 출력하는 새 경로는 찾지 못했고 새 finding은 추가하지 않았다.
- receiver 실행·네트워크·PDF/CSV 전송·부하·테스트는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:38:27

- Kiosk/Web production manifest, data extraction rules, cleartext/network security config, FileProvider·credential bridge provider, exported receiver와 Web 오류/진단 logging 경계를 읽기 전용으로 대조했다.
- 백업 제외·cleartext 차단·credential provider signature permission/URI 제한, Web custom action의 Kiosk caller·signature·one-time handle 검사를 확인했다. Web lock reason과 private diagnostic code는 정규화된 비식별 코드만 기록하므로 새 finding은 추가하지 않았다.
- 앱 실행·Intent 위조·provider query·ADB receiver·로그 수집·빌드/계측은 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:39:24

- Kiosk QR 승인·관리자 session action·수동 학생 roster/검증·Web launch 지연 callback을 작은 구간으로 다시 읽었다.
- `qrAcceptanceGeneration`은 QR 경로에만 적용되고 수동 launch callback은 `destroyed`·`scannerVisible`만 확인하므로, QR 승인 직후 관리자 수동 선택을 시작하면 두 지연 callback이 공존할 수 있음을 `LUNA-0005` 후보의 정적 근거로 구체화했다. 단일 I/O executor와 `QR_READY` 기대 상태가 중복 성공을 제한하므로 상태는 계속 `후보`로 유지했다.
- PIN·QR·수동 선택·Web session·DB 상태 변경, 타이밍 재현·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:43:28

- Kiosk `StudentRepository`의 세션 생성·종료·재시작 정책·QR/수동 학생 검증, Room DAO의 클래스·세션 범위 조회와 expected-state 전이, `WebSessionResultPersistence`, `AdminAuthRepository`·`AdminPin` 및 관련 테스트 소스를 읽기 전용으로 대조했다.
- 세션/학생 범위와 `QR_READY` 상태 검사, 전이의 Room transaction, 관리자 PIN의 PBKDF2·잠금·입력 배열 정리는 확인했다. QR 승인과 수동 학생 선택의 지연 callback 경합은 `LUNA-0005` 기존 후보로 유지했고 새 finding은 추가하지 않았다.
- PIN·QR·학생 데이터·DB 상태 변경, 세션/인증 실행·타이밍 재현·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:46:02

- Web POC `WebDomScripts`의 답안 상태 판정·MathQuill runtime/재마운트·수식 키패드·결과 차폐/지연 hydration, 제출 재진입 guard와 `MainActivity`의 결과 완전성 판독·로그아웃 세대 검사를 읽기 전용으로 대조했다.
- 제출·결과·SPA 전환은 실행하지 않았지만, 정적 경로와 기존 DOM 회귀 fixture에서 결과 차폐, 전체 문항 완전성 확인, 불완전 결과의 실패폐쇄, 로그인·로그아웃 callback 세대 검사를 확인했다. 새 finding은 추가하지 않았다.
- 실제 로그인·답안 입력·MathQuill 조작·제출·결과·로그아웃, Web 공개 접속·fault injection, 테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:49:05

- Kiosk Room schema/entity/DAO·1→2→3 migration, `StudentRepository`의 class/session/QR/credential/deactivation 경계와 관리자 화면 control gating을 읽기 전용으로 대조했다.
- session transaction·expected-state·active filtering·migration 보존은 확인해 새 상태 오염을 만들지 않았다. `KNOWN_LIMITATIONS.md`의 논리적 학생 비활성화와 암호화 자격정보 레코드 보존이 `deactivateStudent()` 구현과 일치하므로 `LUNA-0015`를 `기존 알려진 문제`로 기록했다. 활성 수업 중 관리자 화면·학생 비활성화가 pending QR/prelogin callback을 취소하지 않는 상호작용은 `LUNA-0005` 후보에 추가했고 독립 finding으로 분리하지 않았다.
- DB·Keystore·학생 데이터·PIN·QR 원문 사용, 앱 상태 변경·세션 실행·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:51:37

- Kiosk `QrPdfExporter`, 단건·batch `PrintDocumentAdapter`, PDF writer/renderer, share intent와 `MainActivity`의 print/share/PC 전송 호출부·관련 계측시험을 읽기 전용으로 대조했다.
- export 실패 시 파일 삭제·bitmap wipe, FileProvider export directory 범위, 공유 30초 cleanup·최대 1시간 expiry, 인쇄 adapter 종료 시 bitmap 정리와 55×80mm/30×30mm geometry를 확인했다. PrintManager 실제 취소·인쇄·외부 공유 수신기 보관은 실행하지 않았고 새 finding은 추가하지 않았다.
- QR 원문·학생 데이터·PDF·인쇄/공유·네트워크 전송은 사용하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 07:56:52

- Kiosk `CredentialBridge.kt`·`CredentialBridgeProvider.kt`, Web `MainActivity`의 bridge 소비·lifecycle cleanup·`EphemeralCredentials`, bridge 정책/계측시험과 Kiosk/Web manifest·`GATE4_IMPLEMENTATION.md`·`THREAT_MODEL.md`를 읽기 전용으로 대조했다.
- 30초 elapsed-time one-time handle, publish 전 이전 entry wipe, synchronized consume/revoke, signature permission·정확한 authority/v1 URI·trusted package 검사, Kiosk 취소/Activity 종료 revoke를 확인했다. Provider의 `MatrixCursor`와 Web login JS가 immutable `String`을 만드는 메모리 수명은 위협 모델에 이미 문서화된 JVM/WebView 잔여위험으로 보아 `LUNA-0013`과 중복인 신규 finding을 만들지 않았다.
- 실제 cross-app Intent/provider query, Binder/CursorWindow 복사, 로그인·WebView·ART heap 측정은 수행하지 않았고, PIN·자격정보·학생 데이터·소스·기기 상태는 사용·변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:01:40

- `KioskStatePolicy`, `StudentRepository.applyRestartPolicy()`·`transitionSession()`, `WebSessionResultPersistence`, Kiosk Web 결과/bridge failure callback, QR `QrCardStatusDao`·현재 반/보강 조회와 관련 테스트·운영 문서를 읽기 전용으로 대조했다.
- 재시작 민감 상태의 `LOCKED`/`RECOVERY_REQUIRED` mapping과 Web 결과의 `PRELOGIN_CHECK` expected-state 트랜잭션 검사를 확인해 새 상태 덮어쓰기 finding은 추가하지 않았다. QR `lastUsedAt`은 제품상 사용 이력이고 재발급 hash 교체가 폐기 경계라 QR replay finding도 추가하지 않았다.
- 수동 학생 roster/검증 callback이 session ID·generation을 확인하지 않는 사실을 `LUNA-0005` 후보에 보강했다. 현재 DB 검증이 완화하지만, 새 scanner 상태에서 stale callback이 재개될 수 있는 순서·영향은 라이브 실행 전까지 후보로 유지한다.
- 프로세스 재시작·지연 Web 결과·실제 QR·수동 선택·세션 DB 변경·테스트/빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:08:28

- Web POC `MainActivity`의 `registerNetworkMonitor()`, validated network 판정, WebView error fallback, `updateNetworkPause()`/touch dispatch와 `activity_main.xml`의 네트워크 대기 패널을 작은 구간으로 읽기 전용 대조했다.
- 제품 결정·현재 변경 필드 점검표의 “답안 유지·입력 차단·검증된 재연결 후 재개”와 비교해, `#F2` 반투명 차폐의 답안 비노출 불확실성을 `LUNA-0016`, WebView 포커스·IME·일반 키 입력 차단 부재를 `LUNA-0017`, callback 등록 실패 시 로그 외 fallback 부재를 `LUNA-0018` 후보로 기록했다. 오래된 W05 `LOCKED / NETWORK_ERROR` 문서는 역사적 POC로 분리했다.
- 네트워크 차단·WebView/IME 실행·답안·화면 캡처·callback fault injection·테스트·빌드·설치는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:12:39

- Web POC `transition()`, `onStop()`, `onDestroy()`와 network panel/callback 생명주기를 읽기 전용으로 대조했다.
- ACTIVE 이외 전환 시 pause panel을 숨기고 secure ACTIVE background를 잠그며 destroy 시 등록 callback을 해제하는 정적 경계를 확인했다. stale panel·Activity 종료 후 callback에 대한 새 finding은 추가하지 않았고, `LUNA-0016`~`LUNA-0018`의 런타임 후보는 유지했다.
- background/foreground·Activity 재생성·callback fault·네트워크·WebView·IME·답안·테스트·빌드는 수행하지 않았으며, 소스·문서·기기 상태는 변경하지 않고 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:13:29

- Web POC `initializeUi()`의 `registerNetworkMonitor()` 호출 시점, `uiInitialized` 전후 callback/handler 흐름과 persisted-state resume 경로를 읽기 전용으로 대조했다.
- 등록 직후 `updateNetworkPause()`가 UI 미초기화로 반환할 수 있지만 이후 `transition()` 또는 handler refresh가 재평가하고, 초기 resume은 ACTIVE를 직접 복원하지 않고 로그인/복구 경로를 거치는 정적 흐름을 확인했다. 새 finding은 추가하지 않고 `LUNA-0018`의 등록 실패 fallback 후보는 유지했다.
- callback 실행·네트워크·WebView·IME·앱 lifecycle·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:14:37

- Web POC network pause layout/Activity의 `importantForAccessibility`, visible WebView, clickable/focusable panel과 접근성 관련 운영·필드 문서를 읽기 전용으로 대조했다.
- pause 진입 시 형제 WebView 접근성 subtree를 숨기거나 TalkBack action을 차단하는 명시적 경계를 찾지 못해 `LUNA-0019` P3 후보를 추가했다. 실제 TalkBack·접근성 서비스·네트워크·답안 화면은 사용하지 않았다.
- 소스·문서·기기 상태는 변경하지 않았고, 테스트·빌드·설치·Git 쓰기는 수행하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:17:58

- Kiosk `RemoteSupportWindowController`·`RemoteSupportStore/Policy`, Activity의 `FLAG_SECURE` 초기화·관리자 PIN/relock lifecycle, Web 원격 지원 receiver/manifest와 `SECURITY.md`를 읽기 전용으로 대조했다.
- 원격 지원 유효 기간에는 Kiosk PIN·Web POC 자격정보 입력 등 화면 상태와 무관하게 `FLAG_SECURE`가 해제되고 민감 화면 진입 시 자동 복원이 없어, 문서의 “PIN·비밀번호 입력 중 사용하지 않음”이 운영자 주의에만 의존하는 `LUNA-0020` P3 후보의 범위를 Kiosk/Web으로 넓혔다. 배지 겹침 `LUNA-0012`와 분리했다.
- 원격 지원 추가 활성화·PIN·QR·학생 세션·ADB 캡처·앱 조작·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:20:13

- Kiosk/Web `RemoteSupportStore`, Kiosk `setRemoteSupportEnabled()`·`notifyWebRemoteSupport()`, Web receiver/intent와 원격 지원 정책 시험 소스를 읽기 전용으로 대조했다.
- 양쪽 `SharedPreferences.commit()` 반환값과 cross-app 저장 acknowledgement가 확인되지 않는 구조에서 활성/종료 시 캡처 차단 상태가 어긋날 수 있는 드문 `LUNA-0021` P4 후보를 추가했다. duration·boot 정책과는 분리했다.
- 원격 지원 활성화·종료·설정 쓰기·저장 실패 주입·PIN·QR·학생 세션·캡처·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:23:08

- PC receiver `server.py`의 status/PDF/error event 생성, Tk/tray 알림 표시와 Kiosk PC status payload·관련 protocol 경계를 읽기 전용으로 대조했다.
- 일반 protocol error는 민감 payload를 직접 포함하지 않고 status tray는 state 중심으로 표시하는 것을 확인했다. PDF filename을 사용한 저장 완료 toast는 기존 `LUNA-0009`에 남기고 새 finding은 추가하지 않았다.
- 수신기 실행·네트워크 전송·실제 Windows toast·학생 데이터·PDF·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:23:58

- Kiosk `MainActivity.onStop()/onStart()/onDestroy()`, camera/QR preview/PIN cleanup, admin relock, scanner resume, shared PDF cleanup과 Web secure-session background policy·관련 계측시험 소스를 읽기 전용으로 대조했다.
- background lifecycle에서 stale QR/PIN UI를 정리하고 상태별 재개 경계를 확인해 새 finding은 추가하지 않았다. 원격 지원 중 PIN 화면의 `FLAG_SECURE` 상태 매트릭스는 기존 `LUNA-0020` 후보의 미검증 범위로 유지했다.
- 앱 실행·background 전환·PIN·QR·학생 세션·PDF·캡처·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:24:53

- Kiosk→Web 원격 지원 broadcast 이후 Web POC `RemoteSupportWindowController`가 `FLAG_SECURE`·WebView debugging을 제어하는 경계와 setup/Gate 3의 username/password 입력, `SECURITY.md` 운영 조건을 읽기 전용으로 대조했다.
- `LUNA-0020`의 영향 범위를 Kiosk 관리자 PIN뿐 아니라 Web POC 자격정보 입력 화면까지 넓혔다. 원격 지원 활성 중 민감 화면 자동 차폐·일시중지 코드는 확인하지 못했으며, 실제 자격정보·PIN·캡처는 사용하지 않았다.
- 소스·문서·기기 상태는 변경하지 않았고, 원격 지원·앱 실행·ADB 입력·테스트·빌드는 수행하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:31:36

- Kiosk PC 페어링 QR의 `QrImageAnalyzer`→`handleRawQr()`→`PcPairingStore.save()` 흐름, `PcReceiverPairing`의 decode/zeroize, Android Keystore 암호화 저장과 관련 계측시험을 읽기 전용으로 다시 대조했다.
- raw pairing QR은 ML Kit가 제공하는 immutable `String`으로 전달되어 직접 wipe할 수 없지만, 저장·로그·진단에는 Keystore 암호문과 IV만 사용되고 decoded secret/receiver ID·plaintext byte array는 정상 경로에서 정리된다. 이 raw `String`의 실제 ART heap 수명은 미측정이며, 기존 `LUNA-0013`(CSV parser immutable credential)·`LUNA-0014`(PC 전송 buffer/crypto 예외)와 중복하는 별도 finding은 추가하지 않았다.
- `PcPairingStore`의 ciphertext/IV는 한 `SharedPreferences.Editor`로 함께 예약되고, 누락된 한쪽 키는 load 실패로 처리된다. 저장 실패·프로세스 종료 직전 비동기 `apply()`의 영속 결과는 실행하지 않았으므로 별도 결함으로 확정하지 않았다.
- 실제 PC 페어링 QR·Keystore·저장공간 오류·프로세스 종료·ART heap·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:33:09

- Kiosk/Web 예외 메시지 전달, `showLocked()`·`showMaintenance()`의 Web failure reason, Kiosk/Web private diagnostic log 정규화, PC receiver protocol/UI 오류 출력과 보안정책을 읽기 전용으로 대조했다.
- Kiosk/Web private log는 허용된 코드 형식으로 정규화되고 Web 잠금 사유는 네트워크 오류 매핑 또는 고정 코드이며, PC protocol 오류도 일반화된 문구를 사용한다. 관리자 UI의 CSV 오류는 행 번호·고정 검증 문구 중심이고 Web 공식 페이지 JavaScript 대화상자 메시지는 사용자 화면에만 표시되는 경로로 확인했다. 새 민감정보 로그·진단·오류 출력 finding은 추가하지 않았다.
- 실제 오류 주입·Web 공개 페이지·학생 답안·자격정보·PC receiver 실행·알림·로그 수집·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:34:15

- Kiosk `StudentRepository.audit()`의 전체 호출부, `AuditEventEntity`/DAO/schema, repository 계측시험과 개인정보·진단 정책을 읽기 전용으로 대조했다.
- 감사 DB에는 고정 event/reason code, 내부 학생·세션 UUID, 처리 수량·앱 버전·시각만 저장되고 학생 이름·로그인 아이디·비밀번호·QR 원문·답안·점수는 호출부에서 넘기지 않는다. `audit_events` 기간 cleanup 부재는 기존 `LUNA-0011` 범위로 유지하며 새 개인정보 기록 finding은 추가하지 않았다.
- 실제 Room 데이터·학생 정보·감사 row·장기 운전·cleanup·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:35:29

- Kiosk/Web/probe production manifest, release build flags, backup/data-extraction XML, cleartext 정책, FileProvider·credential bridge·ADB/원격 지원 receiver와 Web Activity caller 검사를 읽기 전용으로 대조했다.
- backup/data extraction·cleartext는 차단되고 FileProvider는 비공개이며, credential bridge는 signature permission·정확한 authority/URI·trusted caller와 서명 일치를 요구한다. Web Activity의 의도적인 exported launcher/action은 secure/recovery 진입 시 호출 패키지와 signature를 다시 확인하므로 새 exported-component 우회 finding은 추가하지 않았다.
- 실제 외부 Intent·provider query·ADB receiver·백업/복원·WebView debugging·빌드·설치·테스트는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:36:29

- Kiosk QR/PDF exporter·single/batch print adapter·share return cleanup, FileProvider path와 PC receiver `*.tmp`/`*.part` 처리·WebView proxy의 파일 경계를 읽기 전용으로 다시 대조했다.
- QR/PDF는 Kiosk cache의 제한된 `qr_exports/` 아래에서 생성되고 export 실패·공유 화면 복귀·만료 cleanup이 정의되어 있으며, PDF filename·ACK 후속 상태·PC config 임시 파일은 각각 기존 `LUNA-0014`·`LUNA-0010`·`LUNA-0002` 범위와 일치한다. PrintManager/외부 수신기의 실제 보관·취소·`onFinish()` 호출은 라이브 미검증으로 남겼다.
- 실제 PDF/QR 생성·인쇄·공유·PC 파일·외부 수신기·WebView proxy·저장공간 오류·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:41:39

- Kiosk `KioskLockTaskController`의 Device Owner 설정·Lock Task 진입/종료·`DISALLOW_CREATE_WINDOWS` rollback, `MainActivity` 호출부와 `SessionPreflightPolicy`를 읽기 전용으로 대조했다.
- `startLockTask()`가 예외 없이 반환해도 `currentMode()!=LOCKED`이면 restriction을 남긴 채 `Result.success(false)`를 반환하고, 호출자가 false를 실패로 처리하지 않아 QR/인증 흐름이 계속될 수 있는 `LUNA-0022` P3 후보를 추가했다. 정상 Android 반환 보장·fault·PINNED·Activity lifecycle 경합은 미검증으로 남겼다.
- Web/Kiosk lifecycle best-effort cleanup과 PC receiver 오류 event의 추가 경로는 기존 `LUNA-0003`·`LUNA-0010`·`LUNA-0011` 및 신뢰된 PC UI 범위와 중복되어 새 finding을 추가하지 않았다.
- Lock Task fault·화면·PIN·QR·카메라·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:46:25

- Kiosk `WebSessionResultPersistence`, `StudentRepository.applyRestartPolicy()/transitionSession()`, `KioskStatePolicy`와 관련 시험을 읽기 전용으로 대조했다.
- Web 결과는 상태 전이와 session 재조회가 모두 성공해야 성공을 반환하고, 전이 실패 시 재조회를 시도하지 않는다. 재시작 시 Kiosk 민감 상태는 `LOCKED`, `QR_READY`·알 수 없는 상태는 `RECOVERY_REQUIRED`로 저장하는 현재 코드와 시험 계약이 일치했다.
- Web POC `WebPocState` recovery policy, secure/admin result delivery, Gate 3 terminal outcome 저장 순서와 `Gate3RunSession`·`WebFailurePolicy` 시험을 대조했다. terminal 결과를 저장하기 전에 runtime session을 지우던 과거 결함은 현재 순서에서 보강되어 있고, 늦은 callback·재시작·결과 persistence의 남은 후보는 `LUNA-0005`·`LUNA-0010`·`LUNA-0011` 및 관련 운영 공백 범위와 중복되어 새 finding을 추가하지 않았다.
- Web 공개 페이지·로그인·답안·Gate 3 실행, Android lifecycle fault, 테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:50:30

- Kiosk `PcEndpointResolver`·`PcSubnetCandidates`·`PcControlClient`·`PcControlProtocol`, `SensitiveTask`와 관련 시험을 읽기 전용으로 대조했다.
- endpoint 후보는 private IPv4와 현재 subnet 범위로 제한되고 authenticated probe만 성공으로 채택하며, 전체 timeout·future 취소·민감 pairing 복사가 적용된다. 제어 frame·응답·payload는 정상 경로에서 정리되고, `SensitiveTask`는 queued discard와 operation 예외에서도 cleanup을 한 번만 수행한다. 이 범위에서 새 finding은 추가하지 않았다.
- `AdminAuthRepository`·`AdminPin`·`AdminPinTest`도 대조했다. PIN 원문은 repository `finally`에서 지워지고, verifier는 salted PBKDF2-HMAC-SHA256과 형식·버전 검사를 사용하며, 실패 지연은 최대 5분으로 제한된다. 관리자 PIN 저장·잠금 정책에서 새 finding은 추가하지 않았다.
- 실제 네트워크 재연결·PC receiver 실행·오류 주입·PIN 입력·인증 시도·메모리/파일 상태·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:52:52

- Kiosk/Web ADB remote-support·diagnostic receiver, `RemoteQrTestPayload`·`RemoteQrTestBridge`, credential bridge 호출부와 관련 manifest/정책 경계를 읽기 전용으로 대조했다.
- ADB receiver는 선언된 권한 범위의 local support 경로이고, QR test hash는 remote support 활성·고정 Base64 길이·32바이트 조건을 통과해야 전달된다. listener 부재·예외 시 호출부가 hash 배열을 정리하며, 진단 dump는 nonce·line 형식을 제한하고 private log의 정규화된 코드만 출력한다. 새 finding은 추가하지 않았다.
- `MainActivity`의 PC status/CSV/PDF 호출부와 `SensitiveTask` shutdown/discard 경계도 다시 대조했으며, pairing·CSV payload·PDF export cleanup은 기존 finding 및 정상 cleanup 계약과 일치했다.
- 실제 ADB broadcast·QR hash·credential bridge query·remote support capture·PC/네트워크 실행·오류 주입·메모리/파일 상태·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:54:11

- PC receiver `config.py`·`server.py`·`app.py`와 `test_server.py`·`test_app.py`·protocol validation을 읽기 전용으로 대조했다.
- pairing 길이·PDF/CSV payload·filename·control label에 상한이 있고, PDF는 temporary path에서 `os.replace`하며 ACK 전송은 인증된 frame 뒤에 수행된다. status UI와 알림은 상태·파일명 경계를 사용하고, PC 수신기 정상 저장·replay·CSV one-shot·TCP ACK 시험이 현재 코드와 일치한다. 기존 `LUNA-0002`·`LUNA-0003`·`LUNA-0007`·`LUNA-0008`·`LUNA-0009`·`LUNA-0010` 범위를 벗어난 새 finding은 추가하지 않았다.
- 실제 PC receiver 실행·TCP 연결·파일/설정 생성·알림·오류 주입·재시작·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:55:38

- Web POC `StudentWebPolicy`·`WebSecurityPolicy`, WebView main-frame navigation/error callbacks, `WebDomScripts` 로그인·학습 화면 script와 관련 호출부를 읽기 전용으로 대조했다.
- main-frame은 HTTPS·허용 host·port·userinfo·fragment·경로 traversal·ambiguous escape 검사를 통과해야 하고, ACTIVE 중 학생 허용 경로가 아니면 마지막 허용 학습 URL로 복귀하거나 잠긴다. 로그인 자격정보는 `JSONObject.quote`로 JS literal에 삽입되며, script의 form origin/action 검사와 contract version 확인이 적용된다. 새 URL·navigation·JS bridge 우회 finding은 추가하지 않았다.
- 실제 WebView·공개 페이지·redirect·TLS/renderer fault·JavaScript 실행·자격정보 입력·네트워크·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:56:36

- Web POC `LoopbackConnectProxy`·`ConnectTargetPolicy`·`CloseableRegistry`·`ProxyTaskSubmission`·`ProxyBootstrapCoordinator`·`WebViewProxyBootstrap`와 proxy lifecycle/bootstrap 시험을 읽기 전용으로 대조했다.
- proxy는 loopback에만 bind하고 CONNECT·443·허용 host suffix로 제한하며, header 8KB·연결/IO timeout·active socket registry·bootstrap watchdog·late callback 무시를 적용한다. failure/timeout/override 예외에서 proxy를 닫는 시험 계약과 현재 코드가 일치한다. 허용 host suffix와 local loopback port의 인증 부재는 설계 잔여위험으로 검토했지만 별도 finding은 추가하지 않았다.
- 실제 WebView proxy override·외부 loopback 연결·DNS/TLS·소켓 고갈·renderer/network fault·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 08:57:21

- Web POC `EphemeralCredentials`·`Gate3RunSession`·`Gate3AccountInput`, `MainActivity`의 login 제출·Gate3 전환·복구·`onStop`/`onDestroy` cleanup과 관련 시험을 읽기 전용으로 대조했다.
- runtime CharArray는 별도 보관되고 wipe가 idempotent하며, login script 제출 직전 credential object를 지운다. Gate3 session은 실패 시 cycle을 진행하지 않고, abort/terminal/recovery/lifecycle 경로에서 session·입력 field를 정리한다. immutable `String`·WebView script의 실제 heap 수명은 측정하지 않았고 기존 문서화 잔여위험으로 유지하며 새 finding은 추가하지 않았다.
- 실제 WebView 로그인·Gate3 실행·lifecycle fault·heap/renderer 상태·자격정보 입력·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 09:00:17

- Web POC `MainActivity.configureWebView()`, WebView client/Chrome client, cookie·cache·WebStorage 정리, network/blocker overlay, renderer unresponsive/gone 복구와 `onStop`/`onDestroy`를 읽기 전용으로 대조했다.
- WebView debugging·form/cache·file/content access·mixed content·multiple windows·download·geolocation을 제한하고, logout/recovery에서 Cookie·WebStorage·cache·history·SSL·입력 field를 독립적으로 정리한다. TLS/Safe Browsing/navigation/HTTP/renderer 오류는 고정 reason으로 실패폐쇄하며, renderer termination timeout과 unusable WebView detach/destroy 경계가 있다. 기존 `LUNA-0016`~`LUNA-0021` 범위를 벗어난 새 finding은 추가하지 않았다.
- 실제 WebView·renderer/네트워크 fault·화면 캡처·IME/접근성·logout·WebStorage/cookie 상태·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 09:06:42

- Kiosk `QrImageAnalyzer`·decision delivery gate·camera bind/stop/destroy 생명주기와 `MainActivity`의 단건·batch QR 발급, PDF export·single/batch print adapter cleanup을 읽기 전용으로 대조했다.
- QR token 원본·stale decision hash·반환 Bitmap·PDF cache/file provider·print/export failure cleanup은 기존 계약과 일치했지만, `QrImageRenderer`가 만드는 ZXing `BitMatrix`와 QR 픽셀 `IntArray`는 Bitmap erase/recycle과 별도의 zeroize/owner 경계가 없어 `LUNA-0023` P3 후보를 추가했다. `LUNA-0006` hash ownership 및 `LUNA-0014` 전송 buffer와는 중복되지 않는다.
- 실제 QR/PDF 생성·인쇄·공유·camera/Lock Task 조작·renderer 예외·ART heap/GC 측정·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 09:14:08

- Kiosk `KioskDatabase` v1/v2/v3 schema와 명시적 migration, `AuditDao`·`StudentRepository.audit()` 연결, `KioskStatePolicy`·`applyRestartPolicy()` 초기화 순서와 migration/repository/state 시험 소스를 읽기 전용으로 대조했다.
- destructive fallback은 없고 migration 계측시험은 학생·QR 상태 및 기존 관리자 credential 보존을 확인한다. 기존 학생 QR 카드의 `lastDeliveredAt=updatedAt`/`needsPrint=0` 초기화는 시험과 문서의 보존 전제에 맞지만 물리 전달 여부는 확인하지 않았다. production에서 호출되지 않는 audit cleanup DAO와 장기 보존 공백은 기존 `LUNA-0011`로 유지했으며 새 finding은 추가하지 않았다.
- 실제 DB migration·A 재시작·장기 audit 누적·저장공간 압박·fault injection·테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않았으며 이 보고서만 `apply_patch`로 갱신했다.
### 2026-08-02 09:23:16
- Kiosk 관리자 학생·반 생성/삭제·소속 변경·CSV 적용·QR 재발급·이름/자격정보 변경·비활성화 호출부, `SingleFlightGate`, `ClassRosterSelectionState`/`RefreshableSelectionState`, 실행취소 action 수명과 관련 문서·계측시험 소스를 읽기 전용으로 대조했다.
- 학생 단일 실행과 반 선택 세대 보호는 확인했고, 활성 수업 중 단건 QR 재발급·비활성화는 저장소 계측시험이 의도적으로 허용하므로 별도 수업 중 mutation finding은 추가하지 않았다. 반 삭제·소속 변경·이름 변경에서만 `offerAdminUndo()`가 pending action을 교체하고, CSV·QR 재발급·자격정보 변경·비활성화·반 생성 뒤에는 기존 action을 지우지 않는 교차 작업 상태 경계를 `LUNA-0024` P4 후보로 추가했다.
- 실제 학생·CSV·QR·자격정보·DB 변경, 연속 버튼/대화상자 조작, Activity timing, 테스트·빌드는 수행하지 않았고, 소스·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 09:38:19

- Kiosk 활성 수업의 반 구성·반 선택 UI 차단, 세션 관리자 재진입·pending undo 수명, `performPendingAdminUndo()`의 `RestoreMemberships` 경로와 `StudentRepository.replaceClassMemberships()`의 활성 세션 검사 부재를 읽기 전용으로 대조했다.
- QR·수동 선택 eligibility query의 `class_memberships OR session_students` 조건과 repository 계측시험·현장 시나리오의 호출 순서를 비교해, 활성 세션 중 pending 반 소속 실행취소가 현재 학생 자격을 바꿀 수 있는 `LUNA-0025` P3 후보를 추가했다. `LUNA-0024`의 교차 작업 stale undo와는 활성 세션의 membership freeze/eligibility 영향이 달라 별도 기록했다.
- 실제 세션 시작·관리자 PIN·실행취소·학생/QR/DB 변경·화면 타이밍·테스트·빌드는 수행하지 않았고, 소스·테스트·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 09:52:02

- Kiosk `completeSessionEnd()`·`endSession()`의 `session_students` 정리, QR/수동 검증의 session snapshot·StudentDao query·audit/`markUsed()` 순서, `launchSecureWebSession()`의 expected-state 전이를 읽기 전용으로 대조했다.
- S1 검증이 진행 중인 동안 S1 종료·S2 시작이 끼어들면 검증 결과가 새 `QR_READY` 화면에 도착할 수 있고, 후속 전이가 검증 당시 session ID가 아니라 현재 `QR_READY`만 확인하는 순서를 `LUNA-0005`의 기존 stale callback 후보에 보강했다. `LUNA-0025`와는 반 membership mutation 우회가 아니라 검증·세션 lifecycle 세대 경계라는 점을 구분했다.
- 실제 S1/S2 세션·Web 복구·QR·수동 선택·학생/DB 변경·화면 타이밍·테스트·빌드는 수행하지 않았고, 소스·테스트·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 09:54:32

- Kiosk `completeSessionEnd()` 성공 분기와 `StudentRepository.endSession()`의 `ADMIN_IDLE` transaction, `refreshAdminData()` snapshot 실패 시 기존 `currentSession` 유지·관리자 제어 재적용 경계를 읽기 전용으로 대조했다.
- RC18의 일반 목록 refresh failure 복구 정책은 기존 화면·수업 상태를 유지하지만, 종료 transaction 성공 뒤에는 이 정책이 실제 DB 종료와 이전 active UI를 어긋나게 할 수 있는 `LUNA-0026` P4 후보를 추가했다. `LUNA-0024`·`LUNA-0025`의 pending undo/membership mutation과는 종료 후 snapshot 동기화 경계가 달라 별도 기록했다.
- 실제 종료·DB read fault·Activity 재생성·다음 수업 시작·학생/세션 변경·테스트·빌드는 수행하지 않았고, 소스·테스트·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 09:58:21

- `BUILD_VERIFICATION.md`의 recovery/session lifecycle 기록, `RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`의 안전 종료·새 수업 시작 항목, `MainActivityInstrumentedTest`의 refresh/Web/QR 실패 시험 연결을 읽기 전용으로 대조했다.
- 정상 `RECOVERY_REQUIRED → 안전 종료 → 새 수업 시작`과 일반 목록 refresh failure는 문서·시험에 있으나, 활성 세션 중 pending `RestoreMemberships` undo와 `endSession()` 성공 뒤 snapshot read failure는 명시·검증되지 않는다. 새 finding은 추가하지 않고 `LUNA-0025`·`LUNA-0026`의 회귀 공백을 보강했다.
- 실제 A 화면·세션 종료·관리자 PIN·학생/QR/DB 변경·저장소 fault·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 09:59:22

- `MainActivity.loadInitialState()`·`StudentRepository.applyRestartPolicy()`와 초기 상태 실패/재시도 계측시험, 운영 문서의 `RECOVERY_REQUIRED → 관리자 안전 종료 → 새 수업 시작` 흐름을 읽기 전용으로 대조했다.
- restart recovery·관리자 재진입·정상 종료/재시작 연결은 현재 source와 대체로 일치해 새 독립 finding은 추가하지 않았다. 활성 session pending undo, 종료 성공 후 refresh failure, in-flight 검증의 종료·재시작 교차 순서는 기존 `LUNA-0005`·`LUNA-0025`·`LUNA-0026`의 미검증 범위로 유지했다.
- 실제 A·재부팅·PIN·세션·Web·학생/QR/DB 변경·fault·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:01:46

- Kiosk `pendingRecoveryAction`의 Web recovery launch/result 소비, `MainActivity` lifecycle 저장·복원 유무, Web POC 관리자 recovery의 `RESULT_OK`/`RESULT_CANCELED` 반환을 읽기 전용으로 대조했다.
- Activity 재생성 뒤 새 Kiosk callback이 `PendingRecoveryAction.None`을 받아 일반 refresh만 수행할 수 있고, Web 정리 성공 뒤 `StartSession`/`EndSession` 후속 DB 전이가 빠질 수 있는 `LUNA-0027` P4 후보를 추가했다. `LUNA-0026`의 종료 후 refresh read failure와는 action 자체의 lifecycle 유실 경계가 달라 별도 기록했다.
- 실제 Activity recreation·process reclaim·Web recovery·결과 재전달·session/DB 변경·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:08:43

- 일반 학생 Web 로그인 경로의 `pendingCredentialBridgeId`, `OneTimeCredentialBroker`, trusted provider consume/revoke 및 `MainActivity.onDestroy()` cleanup을 읽기 전용으로 대조했다.
- Activity 종료 시 pending handle을 revoke하고 provider consume 시 payload를 즉시 wipe하며 TTL·재사용 방지 계측시험도 있어, 이 반복 감사에서는 독립 credential lifecycle finding을 추가하지 않았다. Activity 재생성 중 Web recovery action 유실은 `LUNA-0027`로 유지했다.
- 실제 Activity recreation·Web 로그인·credential provider 호출·자격정보·session/DB 변경·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:10:16

- `KioskLockTaskController`의 Device Owner 설정·Lock Task 진입/종료·`DISALLOW_CREATE_WINDOWS` rollback, `MainActivity`의 전용기기 상태 반영·Activity `onStart` 재진입, `DedicatedDevicePolicy`/`SessionPreflightPolicy` 연결을 읽기 전용으로 대조했다.
- 정상 정책 설정·관리자 의도적 잠금 해제·재잠금 구조는 기존 범위와 일치하며, 예외 없는 `currentMode()!=LOCKED` false 결과에서 restriction을 남기고 호출자가 false를 실패로 처리하지 않는 경계는 기존 `LUNA-0022` P3 후보로 유지했다. 새 독립 finding은 추가하지 않았다.
- 실제 Lock Task fault·PINNED/회전·백그라운드·Activity recreation·restriction 조회·화면/카메라·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:11:39

- Kiosk Room `KioskDatabase`의 schema version 3, `MIGRATION_1_2`/`MIGRATION_2_3`, destructive fallback 부재, `StudentRepository.applyRestartPolicy()`·`KioskStatePolicy`, `AuditDao.deleteOlderThan()` 연결과 migration/restart 계측시험 소스를 읽기 전용으로 대조했다.
- migration은 학생·QR card 상태와 기존 관리자 credential을 보존하며 민감 재시작 상태를 `LOCKED`로 기록한다. 감사 DB cleanup 호출 부재와 진단 log 회전/보존 기준 공백은 기존 `LUNA-0011` 후보 범위로 유지하고, 새 독립 finding은 추가하지 않았다.
- 실제 DB upgrade·부분 migration fault·저장공간 압박·감사 cleanup·재부팅·session/학생 데이터·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:13:10

- PC receiver `ReceiverState.accept()`/`accept_control()`의 lock·atomic PDF replace·replay 기록·CSV pending 소비·socket timeout/thread 종료와 Kiosk `PcPdfSender`/`PcControlClient`의 ACK·retry·ByteArray cleanup 경계를 읽기 전용으로 대조했다.
- PDF commit/ACK·새 request ID 재시도 중 중복 저장은 `LUNA-0010`, CSV 응답 후 pending 파일 소실은 `LUNA-0003`, 연결 자원·replay eviction은 `LUNA-0007`·`LUNA-0008`, CSV/credential payload 수명은 `LUNA-0004`·`LUNA-0013`·`LUNA-0014`와 중복되어 새 독립 finding을 추가하지 않았다.
- 실제 PC receiver 실행·socket fault·ACK 유실·CSV/PDF·파일/설정·네트워크·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:15:14

- `webSessionLauncher`/`persistWebSessionResult()`/`WebSessionResultPersistence`와 `StudentRepository.transitionSession()`의 expected-state·session ID 연결, `RepositoryInstrumentedTest`·`MainActivityInstrumentedTest`·운영 문서의 S1 종료→S2 시작 회귀 범위를 읽기 전용으로 대조했다.
- Web Activity의 늦은 결과도 호출 당시 session ID·launch generation 없이 현재 `PRELOGIN_CHECK` singleton에 적용될 수 있어, 기존 `LUNA-0005`의 QR/수동 stale callback 범위를 Web result 단계까지 보강했다. 별도 finding ID는 추가하지 않았다.
- 실제 Web Activity·결과 지연·세션 종료/재시작·학생/DB 변경·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:20:00

- Kiosk/Web `RemoteSupportStore`·`RemoteSupportPolicy`·`RemoteSupportWindowController`, Kiosk `setRemoteSupportEnabled()`/`notifyWebRemoteSupport()`, Web 수신 receiver·signature permission·원격 지원 운영 문서와 정책/manifest 시험 소스를 읽기 전용으로 대조했다.
- 양쪽 store의 `SharedPreferences.commit()` 반환값은 호출자에게 전달되지 않고, Kiosk는 동기 예외가 없다는 `sendBroadcast()` 결과만 Web 전달 성공으로 취급한다. 활성화 실패 때 Kiosk 쪽 동기 rollback은 있으나 Web 처리·commit acknowledgement가 없고, 종료 알림 실패 때 양쪽 로컬 상태가 만료 시각까지 달라질 수 있는 경계는 기존 `LUNA-0021` P4 후보와 동일하다. 새 독립 finding은 추가하지 않았다.
- 실제 원격 지원 활성화·종료·capture·PIN/자격정보 입력·ADB·SharedPreferences fault·교차 앱 상태·Activity recreation·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:22:19

- Kiosk `AdminAuthRepository`·`AdminPin`·`AdminCredentialEntity`/`AdminDao`, PIN 정책·보안 문서와 `AdminPinTest`·Room migration 시험 연결을 읽기 전용으로 대조했다.
- 입력 PIN과 PBKDF2 candidate key 정리는 존재하지만, `isEnrolled()`·`enrolledPinLength()`의 full entity 조회와 인증·등록 경로의 verifier `salt`·`derivedKey` 배열은 zeroize ownership이 없었다. verifier는 원문 PIN이 아니고 실제 heap 수명은 미검증이므로 `LUNA-0028` P4 후보를 추가했다. 기존 student credential/PC transfer cleanup finding과는 대상과 데이터 경계가 다르다.
- 실제 PIN·DB 인증/저장·heap/GC·ADB·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:25:44

- Web POC `LoopbackConnectProxy`·`CloseableRegistry`·`ProxyTaskSubmission`·`WebViewProxyBootstrap`과 Activity 종료, proxy bootstrap/lifecycle 시험을 읽기 전용으로 대조했다.
- loopback proxy는 허용 host 정책과 partial-header timeout은 있지만 cached thread pool·무제한 client/upstream socket registry·성립 후 idle timeout 부재를 갖는다. 이는 PC receiver의 외부 endpoint 자원 경계 `LUNA-0007`과 다른 local loopback/process-wide 경계이므로 `LUNA-0029` P4 후보를 추가했다.
- 실제 proxy·CONNECT·localhost scan·socket/thread/FD 부하·네트워크·Activity 종료·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:29:03

- Kiosk `PcSubnetCandidates`·`PcEndpointResolver`·`PcControlClient`·`PcPairingStore`·`PcReceiverPairing`, PC 자동 복구 운영 문서와 pairing/endpoint 시험을 읽기 전용으로 대조했다.
- 자동 복구의 사설망 후보 제한·고정 병렬성·인증된 probe 채택·candidate byte cleanup은 확인했다. 반면 pairing QR `rawValue`, `save(rawPairing)`, `encode()`·복호화 경계의 immutable Base64 secret String은 zeroize할 수 없어 `LUNA-0030` P3 후보를 추가했다. 파일 at-rest `LUNA-0002`와 PC 전송 buffer `LUNA-0014`와는 런타임 QR/String 경계가 다르다.
- 실제 pairing QR·endpoint 복구·DB/Keystore·heap dump·GC·ADB·네트워크·테스트·빌드는 수행하지 않았고, 소스·테스트·운영 문서는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:35:07

- Kiosk `MainActivity`의 PC CSV fetch·preview·apply executor 제출, `onDestroy()`의 `shutdownNow()`/`SensitiveTask` discard, `StudentCsvImport` row wipe와 `StudentRepository` preview/import `finally` 경계를 읽기 전용으로 대조했다.
- CSV parsed row가 일반 executor 람다에 캡처되고 Activity 종료 시 queued 일반 작업은 discard되지 않으며, 제출 거부에도 cleanup 보상이 없어 username/password `CharArray` wipe가 보장되지 않는 `LUNA-0031` P3 후보를 추가했다. parser immutable `String` 수명 `LUNA-0013`, 기존 username decrypt map 예외 `LUNA-0004`와는 별도 경계로 구분했다.
- 실제 CSV·학생 데이터·Activity recreation·executor fault·heap/GC·테스트·빌드는 수행하지 않았고, 소스·테스트·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:39:37

- Kiosk `MainActivity`의 전체 `ioExecutor` 제출부, `SensitiveTask` 적용 경로, QR/PDF batch bitmap·hash cleanup, `BatchQrPrintDocumentAdapter.onFinish()`, `OneTimeCredentialBroker` TTL 및 `PreparedWebSessionPolicy` destroyed callback을 읽기 전용으로 대조했다.
- PIN·학생 자격정보·QR validation은 `SensitiveTask` cleanup과 queued discard를 사용하고, batch QR/PDF의 실행 중 예외·destroyed callback·print adapter 종료 정리는 확인했다. credential bridge의 prepared handle은 destroyed callback에서 revoke-only로 처리되고 30초 TTL이 있어 `LUNA-0031`과 합칠 새 독립 finding은 추가하지 않았다. 일반 CSV preview/apply만 `LUNA-0031`의 direct executor cleanup 경계로 유지했다.
- 실제 QR/PDF 인쇄·Web credential bridge·Activity lifecycle fault·heap/GC·테스트·빌드는 수행하지 않았고, 소스·테스트·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:42:00

- Kiosk `StudentCsvParser.parse()`의 `mapIndexed` partial row 생성·후속 validation/duplicate 예외, `ParsedStudentCsv.clearSensitiveData()`, `MainActivity` parser catch와 `StudentCsvParserTest` 실패 fixture를 읽기 전용으로 대조했다.
- 첫 행의 username/password `CharArray`가 생성된 뒤 후속 행 검증이 실패하면 `ParsedStudentCsv`가 반환되지 않고 호출부는 payload만 지우는 `LUNA-0032` P3 후보를 추가했다. immutable parser String 수명 `LUNA-0013`과 성공 후 executor cleanup `LUNA-0031`과는 별도 owner 경계다.
- 실제 CSV·학생 데이터·parser 실행·heap/GC·테스트·빌드는 수행하지 않았고, 소스·테스트·문서·기기 상태는 변경하지 않으며 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 10:57:22

- Kiosk `MainActivity` PC status reporting의 `pcControlExecutor` 무제한 single-thread queue, `PcControlClient`/`PcEndpointResolver` timeout·recovery, CSV fetch·자가진단 공유 worker와 `onDestroy()` `shutdownNow()` 경계를 읽기 전용으로 대조했다.
- PC unreachable·반복 상태 전이에서 stale status와 학생 표시명 보유, 기능성 CSV/self-test 지연·queue cleanup 공백이 생길 수 있는 `LUNA-0033` P3 후보를 추가했다. PC receiver inbound 자원 후보 `LUNA-0007`, CSV pending/parsed row owner `LUNA-0003`·`LUNA-0031`과는 다른 Kiosk outbound queue 경계다.
- `docs/RELEASE_OPERATIONS.md`·`docs/THREAT_MODEL.md`의 designated private Wi-Fi/recovery 전제, Kiosk/Python 초기 pairing decode/save와 direct endpoint connect를 대조했다. 초기 QR host의 private subnet·receiver identity 검증 공백 및 fake QR 시 임의 endpoint routing 가능성을 `LUNA-0034` P3 후보로 추가했다. 실제 QR·네트워크·status/PDF 전송은 수행하지 않았다.
- 이번 반복은 소스·테스트·운영 문서·A·PC·네트워크·실제 데이터·빌드·Git 상태를 변경하지 않았고, 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:01:12

- `PcControlProtocolTest.kt`·`PcTransferProtocolTest.kt`·`PcEndpointResolverTest.kt`·Python `test_protocol.py`·`test_server.py`와 PC pairing/recovery 운영 문서를 읽기 전용으로 다시 대조했다.
- Kiosk status/CSV/self-test executor queue 계측·offline 반복·Activity shutdown 검증이 없고, pairing 시험은 고정 private vector와 recovery 후보만 다루는 것을 확인했다. Python receiver의 `127.0.0.1`은 local server fixture용이며 초기 QR host trust 정책의 증거로 사용하지 않았다.
- `LUNA-0033`·`LUNA-0034`의 P3 후보 상태와 테스트·문서 미검증 범위를 유지했다. 테스트·빌드·네트워크·QR·실제 데이터·소스 변경 없이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:05:29

- Kiosk `MainActivity`의 Web session result/recovery, QR·수동 학생 validation callback, `StudentRepository.transitionSession()`·`applyRestartPolicy()`, `AdminUiAsyncStateTest`·repository/MainActivity 계측시험과 현장 검증 문서를 읽기 전용으로 대조했다.
- 단일 I/O executor·expected-state·selection generation·destroyed guard는 확인했지만, S1/S2 지연 결과·종료 후 refresh/read failure·Activity recreation recovery action은 실기 없이 각각 `LUNA-0005`·`LUNA-0026`·`LUNA-0027` 후보로 유지했다. 새 독립 finding은 추가하지 않았다.
- 실제 session/학생/DB 변경·Activity recreation·fault injection·테스트·빌드·A 조작 없이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:08:22

- Web POC `WebPocState`·`MainActivity`의 state persistence/fail-closed, login/logout/active generation, renderer recovery, network monitor, Gate3/credential cleanup과 관련 JVM·계측시험·설계/위협 문서를 읽기 전용으로 대조했다.
- 정적 lifecycle은 기존 정책·시험과 일치했고, network pause 차폐/IME/접근성·remote-support 민감 화면/ack·loopback proxy budget·A renderer/Activity 실기는 기존 `LUNA-0016~0021/0029` 미검증 후보로 유지했다. 새 독립 finding은 추가하지 않았다.
- 실제 네트워크·renderer fault·원격지원·WebView/Activity 조작·테스트·빌드·소스 변경 없이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:11:53

- Kiosk `MainActivity`의 `webRecoveryGate`, `launchWebSessionRecovery()`, `PendingRecoveryAction.StartSession`, `updateClassRosterUi()`·`updateStudentManagementControls()` enabled 조건과 `StudentRepository.startSession()`·`replaceClassMemberships()`·`deleteClass()`의 활성 session 검사를 읽기 전용으로 대조했다.
- Web recovery gate가 수업 시작/재개 등 일부 control만 잠그고 반·보강·학생/CSV/QR mutation을 공통으로 잠그지 않으며, pending StartSession action의 class/temporary snapshot에 mutation revision 재검증이 없는 pre-session stale intent 경계를 `LUNA-0035` P3 후보로 추가했다. `SingleFlightGate`·repository invariant 시험과 RC04/RC05 운영 문서는 관련 matrix·교차 callback을 증명하지 못했다.
- 실제 Web recovery·학생/반/CSV/QR mutation·수업 시작·DB fault·Activity recreation·A 조작·테스트·빌드·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:18:20

- `docs/BUILD_VERIFICATION.md`의 RC04/RC05 Web 안전정리·수업 Start/End 단일 gate 기록, 빠른 반 전환·연속 탭·실제 수업 시작/종료 미검증 항목과 현재 Kiosk `src/test`·`src/androidTest`의 `webRecoveryGate`·`PendingRecoveryAction`·`StartSession` 검색 결과를 읽기 전용으로 대조했다.
- 운영 문서의 단일 gate 서술은 후속 Start/End 중복 실행 범위를 설명하지만 반/보강/학생 mutation 전체 enabled matrix를 증명하지 않는다. 현재 시험 연결에서도 새 독립 finding은 확인하지 않았고 `LUNA-0035`의 gate coverage·교차 callback 미검증 범위를 유지했다.
- 실제 Web recovery·반 전환·연속 탭·수업 시작/종료·학생/CSV/QR mutation·A 조작·테스트·빌드·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:20:24

- `MainActivity.runSessionPreflight()`의 보안/배터리/저장공간/카메라 사전점검과 확인 대화상자, `PendingRecoveryAction.StartSession` 생성·Web recovery 전달, 반/보강/학생 mutation handler를 읽기 전용으로 대조했다.
- `StudentRepository.startSession()`의 active class·active student·temporary ID·기존 session transaction 불변조건, `addTemporaryStudents()`의 현재 session ID/state 검증과 `SessionPreflightPolicyTest`·`RepositoryInstrumentedTest`의 범위를 확인했다. preflight 확인 전후의 action snapshot 재검증 시험은 없지만 기존 `LUNA-0035`의 stale intent/gate coverage 경계와 중복되어 새 ID는 추가하지 않았다.
- 실제 사전점검·확인 대화상자·Web recovery·반/학생/CSV/QR mutation·수업 시작·DB fault·Activity recreation·테스트·빌드·A 조작·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:21:45

- `MainActivity`의 `webRecoveryLauncher` result callback, `pendingRecoveryAction` 소비 순서, `completeSessionStart()`·`completeSessionEnd()`의 `ioExecutor.execute`, `onDestroy()`의 `shutdownNow()`·destroyed guard와 관련 lifecycle 문서/시험 연결을 읽기 전용으로 대조했다.
- recovery 후속 DB 작업 제출 거부에 대한 별도 UI 복구는 없지만, 거부는 Activity 종료·executor shutdown과 결합된 lifecycle 경계이며 새 Activity의 action 유실은 기존 `LUNA-0027`, Web recovery 중 mutation/action revision은 `LUNA-0035`와 각각 중복된다. 새 독립 finding은 추가하지 않았다.
- 실제 Activity recreation·Web result callback·executor rejection/interruption·session/DB 변경·테스트·빌드·A 조작·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:23:17

- Kiosk `webSessionLauncher`, `persistWebSessionResult()`, `WebSessionResultPersistence`, `StudentRepository.transitionSession()`의 expected-state transaction과 `WebSessionResultPersistenceTest`·`MainActivityInstrumentedTest`·RC05/RC06 문서를 읽기 전용으로 대조했다.
- 결과 처리의 현재 `PRELOGIN_CHECK` 검사는 재시작·중복 결과의 상태 덮어쓰기를 제한하지만 session ID/launch generation binding과 결과별 학생 표시명 snapshot은 없다. S1/S2 교차 결과와 PC 상태명 혼선은 기존 `LUNA-0005` stale callback 후보의 범위이며 새 독립 finding은 추가하지 않았다.
- 실제 Web session·S1/S2 전환·지연/중복 callback·학생/DB 변경·PC 전송·테스트·빌드·A 조작·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:25:44

- Kiosk `studentMutationGate`·`updateStudentManagementControls()`와 `batchQrButton`·`confirmBatchQrPrint()`·`prepareBatchQrPrint()`의 enabled/gate 조건, `StudentRepository.reissueClassQrBatch()`·`recordClassQrBatchPrintRequested()` transaction/PrintManager 순서, `RepositoryInstrumentedTest` batch 원자성 시험과 RC05 학생 변경 운영 문서를 읽기 전용으로 대조했다.
- student mutation gate가 bulk QR/print를 잠그지 않고, 같은 `ioExecutor`의 DB 직렬화가 이미 반환된 cards의 후속 인쇄를 mutation 결과에 맞춰 취소하지 않는 stale·무효 QR 출력 경계를 `LUNA-0036` P3 후보로 추가했다. batch 내부 원자성·active session 차단은 확인했으며 `LUNA-0035`·`LUNA-0024`·`LUNA-0006`·`LUNA-0010`과 다른 cross-operation binding으로 분리했다.
- 실제 학생/반/CSV/QR mutation·batch 재발급·인쇄·PrintManager·DB·bitmap·Activity lifecycle·테스트·빌드·A 조작·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:30:30

- `MainActivity.fetchStudentCsvFromPc()`·`showStudentCsvPreview()`·`applyStudentCsv()`의 import button 직접 제어와 `StudentRepository.previewStudentImport()`·`importStudents()`의 credential/name/membership transaction, `studentMutationGate`·batch QR 연결 및 CSV 운영/계측시험을 읽기 전용으로 대조했다.
- CSV bulk mutation이 공통 student gate를 직접 사용하지 않는 gate coverage 공백은 확인했지만, repository transaction과 단일 `ioExecutor`가 DB 작업을 직렬화하고 현재 시험/정적 근거만으로 별도 stale overwrite·partial state를 확정할 수 없었다. `LUNA-0036`의 공통 operation gate·CSV↔batch ordering 회귀시험 미검증 범위로 유지하며 새 독립 finding은 추가하지 않았다.
- 실제 PC CSV fetch·preview/apply·학생/반/QR 변경·credential·DB·batch 인쇄·테스트·빌드·A 조작·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:32:13

- `pendingCardsPdfButton`·`showPendingCardsDialog()`·`preparePendingCardsPdf()`의 gate/button 조건과 QR batch reissue·PDF export·`PcPdfSender` ACK·`markCardsDelivered()` 순서를 읽기 전용으로 대조했다.
- pending-card PDF delivery가 `studentMutationGate`를 획득하지 않아 PC ACK 후 student mutation이 QR validity/name/roster와 Kiosk delivered status를 달리 만들 수 있는 `LUNA-0037` P3 후보를 추가했다. `LUNA-0010`의 ACK/retry protocol, `LUNA-0036`의 local PrintManager batch ordering과는 다른 PC delivery invalidation 경계로 분리했다.
- 실제 pending-card QR/PDF·PC ACK·student mutation·DB/PC 파일·인쇄·테스트·빌드·A 조작·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:35:36

- `MainActivity.onDestroy()`의 `ioExecutor.shutdownNow()`와 pending PDF runnable의 `BatchQrPdfExporter`·`PcPdfSender.send()`·bitmap/file cleanup, `PcPdfSender` socket timeout/ACK 경계 및 RC14 종료 작업 문서를 읽기 전용으로 대조했다.
- 대기 runnable은 shutdown 반환 목록에서 정리 대상이 될 수 있지만 실행 중 PDF 전송은 별도 cancellation token/socket close/destroyed check 없이 진행될 수 있다. ACK 유실·재시도 semantics는 `LUNA-0010`, student mutation과 전달 artifact invalidation은 `LUNA-0037`에 이미 포함되어 새 독립 finding은 추가하지 않았다.
- 실제 Activity 종료·PDF/PC 전송·ACK·socket interruption·파일·bitmap·학생/DB 변경·테스트·빌드·A 조작·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:39:53

- `startOrEndSession()`의 `PendingRecoveryAction.EndSession`, `launchWebSessionRecovery()`의 `updateSessionAdminControls()`, `updateClassRosterUi()`의 `addTemporaryButton` 조건과 `addTemporaryStudents()`·`completeSessionEnd()`·`StudentRepository.addTemporaryStudents()/endSession()`의 순서를 읽기 전용으로 대조했다.
- 활성 `QR_READY` 수업의 EndSession Web recovery 중 보강 학생 추가 handler가 `webRecoveryGate`를 직접 확인하지 않는 사실을 확인했다. repository의 session ID/state 검증과 종료 transaction이 DB partial write를 제한하므로 `LUNA-0035`의 Start/End 공통 gate·순서 경계를 보강하고 새 독립 ID는 추가하지 않았다.
- 실제 EndSession recovery·보강 학생 추가·Web Activity 전환·`ioExecutor` 순서·세션/DB 변경·테스트·빌드·A 조작·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:44:53

- Web POC `WebDomScripts.applyStudentExperience()`의 `window.__matholicKioskProblemStates` 생성·문항 번호별 상태 기록/조회·현황/`다음 미입력` 사용, `navigateStudentSection()`의 허용 anchor SPA click, `MainActivity`의 active monitor와 Web DOM/계측시험을 읽기 전용으로 대조했다.
- SPA 과제/경로 전환에서 상태 map을 pathname·과제 식별자와 분리하거나 reset하는 코드가 보이지 않고, 기존 시험은 단일 fixture와 script 계약만 다룬다. 과제 A의 같은 번호 상태가 과제 B의 현황·미입력 이동에 남을 수 있는 조건부 `LUNA-0038` P3 후보를 추가했다. `LUNA-0016~0019` 네트워크 차폐·`LUNA-0005` Kiosk session callback과는 다른 Web task-state cache 경계로 분리했다.
- 실제 학습지·진단평가·과제 전환·답안 입력/제출·서버 세션·A 화면·테스트·빌드·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:51:14

- PC `ReceiverApplication.shutdown()`·`ThreadedReceiverServer`·`_ReceiverHandler`의 server loop 종료, active handler join, 10초 socket timeout, PDF commit·ACK·event 전달 순서와 `test_server.py`/`test_app.py`를 읽기 전용으로 재대조했다.
- `server.shutdown()` 후 `server_close()`가 Python 3.11의 `ThreadingMixIn.server_close()`를 통해 active handler를 join하므로, daemon 설정만으로 종료 중 PDF write/ACK가 UI 종료 뒤 무조건 남는 새 finding은 추가하지 않았다. 인증 전 연결·무제한 event queue의 자원 상한 공백은 `LUNA-0007`, commit 후 ACK 유실·재시도 중복은 `LUNA-0010`의 미검증 경계로 유지했다.
- 실제 PC receiver·TCP 연결·종료 경합·PDF/파일·ACK 유실·오류 주입·테스트·빌드·A 조작·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:55:58

- `WebDomScripts.kt`의 `__matholicKioskProblemStates` 모든 생성·문항 번호별 read/write, `MainActivity`의 `navigateStudentSection()`·ACTIVE monitor generation, `DomContractInstrumentedTest`의 SPA navigation/state fixture와 관련 운영 문서를 읽기 전용으로 심화 대조했다.
- targeted `webpoc/src` 검색에서 map reset·pathname/task key·history route listener는 확인되지 않았고, 기존 navigation 시험은 실제 route/task 전환이 아니라 `onclick` body marker만 확인한다. 따라서 같은 Web Document를 유지하는 실제 SPA에서 이전 과제 상태가 남을 조건은 여전히 합리적이나, full document reload 또는 사이트의 별도 task hydration 계약이면 발생하지 않을 수 있어 `LUNA-0038` P3 후보·중간 신뢰도를 유지하고 새 ID는 추가하지 않았다.
- 실제 공개 Web·학습지/진단평가·과제 전환·답안 입력/제출·서버 상태·A 화면·테스트·빌드·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 11:59:48

- Kiosk `StudentRepository`·`QrCardStatusDao`의 `needsPrint` 생성/해제, `MainActivity`의 pending-card dialog와 Android 단건·반 전체 `PrintManager` 경로, 두 QR print adapter, 관련 repository/adapter 시험과 운영 문서를 읽기 전용으로 대조했다.
- 현재 source 경로에서 `needsPrint=false` 전이는 PC PDF ACK 뒤 `markCardsDelivered()`에 연결되고, local single/batch print 성공은 print-request audit·bitmap/attribute cleanup만 수행한다. 직접 인쇄 후 pending 상태가 남을 수 있는 별도 `LUNA-0039` P4 후보를 추가했으며, PrintManager handoff가 물리 출력 확인이 아니라는 반대 가설과 Android direct print 보류/PC 기본 운영을 함께 기록했다.
- 실제 PrintManager·프린터·QR·pending 재발급·A·DB·ADB mutation·테스트·빌드·네트워크·실제 데이터·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 12:10:14

- Web POC `finishButton`·`beginLogout()`·`finishLogoutVerification()`·`clearWebSessionAndReloadLogin()`·`sanitizeLoginAndFingerprint`·`RESULT_OK` 경로와 Kiosk `webSessionLauncher`/`persistWebSessionResult`, 관련 local cleanup/DOM 시험을 읽기 전용으로 대조했다.
- Web은 WebView local history/form/cache/SSL/WebStorage/cookie와 login DOM을 정리·검사하지만, 공식 서버 임시답안 `unanswered` 상태·같은 과제 재진입·60초 지연 복원 여부를 확인하지 않고 Kiosk에 성공을 반환할 수 있다. 최신 운영 정정 기록의 persisted answer cleanup 기준과 결합해 `LUNA-0040` P3 후보를 추가했으며, 서버 logout이 자동 폐기할 수 있다는 반대 가설을 명시했다.
- 실제 시험계정·과제·답안·서버·재진입·네트워크·A·QR·테스트·빌드·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 12:17:27

- `pc_receiver/src/matholic_pdf_receiver/server.py`의 PDF/control replay 기록·설정 저장·PDF atomic replace·CSV pending 소비·ACK/response 전송 순서와 Kiosk `PcTransferProtocol`·`PcControlProtocol`·`PcPdfSender`·`PcControlClient`의 request ID 생성·strict response 검증·buffer cleanup을 읽기 전용으로 대조했다.
- PDF commit-before-ACK·새 request ID 재시도 중복은 `LUNA-0010`, CSV 응답 후 pending 소비·재시작/수신/파싱 실패는 `LUNA-0003`, replay ID 2,048개 eviction은 `LUNA-0008`, 상태 보고의 retry/stale queue는 `LUNA-0033`과 중복되어 새 독립 finding을 추가하지 않았다. control payload validation 전에 replay ID를 저장하는 세부 경계도 정상 client payload·새 ID 재시도 semantics상 별도 material finding으로 분리하지 않았다.
- Kiosk/PC receiver의 단위·계측시험은 정상 protocol vector·즉시 replay·정상 CSV one-shot 경계를 다루고 ACK 유실·재연결·sendall/read timeout·queue fault는 실행하지 않는다. 실제 PC receiver·TCP·ACK·CSV/PDF·파일·설정·네트워크·테스트·빌드·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 12:20:19

- Kiosk `launchWebSessionRecovery()`·`updateSessionAdminControls()`·`updateClassRosterUi()`·`updateStudentManagementControls()`·`updateQuickClassButtons()`와 모든 관리자 handler, `PendingRecoveryAction.StartSession/EndSession`, `StudentRepository` transaction guard 및 관련 enabled-matrix 시험/운영 문서를 읽기 전용으로 재대조했다.
- Web recovery 중 `classSpinner`는 비활성화되지만 quick class 버튼은 `currentSession == null`만으로 활성화되고, 반 구성·삭제·보강·batch QR·학생·CSV/QR mutation도 공통 gate에 직접 연결되지 않는다. quick class 전환과 캡처된 StartSession action의 class/temporary snapshot 불일치 가능성은 기존 `LUNA-0035`의 gate coverage/action revision 범위로 보강했으며 새 독립 finding은 추가하지 않았다.
- 기존 시험은 `SingleFlightGate`와 일부 admin refresh/error enabled 상태를 확인하지만 Web recovery 중 전체 버튼 matrix·빠른 반 전환·mutation과 Start/End callback 교차를 실행하지 않는다. 실제 Web recovery·버튼/반/학생/세션 변경·DB·A·테스트·빌드·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 12:24:06

- Kiosk `StudentCsvParser`의 credential schema/row cleanup과 PC `ReceiverApplication.choose_csv()`·`ReceiverState.queue_csv()`·`clear_csv()`·`accept_control()`·`ConfigStore`·protocol response encoder·shutdown을 읽기 전용으로 교차 대조했다.
- PC receiver는 선택 CSV를 immutable `bytes` pending tuple에 보유하고, fetch response 생성 중 plaintext·ciphertext·frame을 추가로 만들며 clear/replace/fetch/send exception/shutdown에서 명시적 zeroize를 하지 않는다. Kiosk CSV parser의 `CharArray` cleanup과는 다른 PC process-memory 경계로 `LUNA-0041` P4 후보를 추가했고, `LUNA-0003` queue durability와도 분리했다.
- 기존 PC 시험은 정상 CSV one-shot·빈 queue·protocol vector만 다루며 cancel/replace/response fault/shutdown/heap cleanup은 검증하지 않는다. 실제 CSV·자격정보·PC receiver·파일·네트워크·heap dump·테스트·빌드·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 12:28:26

- `docs/RELEASE_OPERATIONS.md`의 지정 PC 장애·재페어링·재시도 절차와 `docs/BUILD_VERIFICATION.md`의 PC receiver/CSV 정상·버튼 상태 검증 기록을 `pc_receiver` app/server/protocol/test와 읽기 전용으로 연결 대조했다.
- 운영 문서는 CSV 대기 queue 복구·재선택과 정상 민감정보 비노출만 다루고, pending payload의 cancel/replace/send exception/shutdown zeroization·PC process memory 수명은 정의하지 않는다. 이는 `LUNA-0003`의 queue durability와 `LUNA-0041`의 volatile buffer cleanup의 미검증 문서·회귀 범위로 유지하며 새 독립 finding은 추가하지 않았다.
- 실제 PC receiver·CSV·재시작·네트워크·파일·메모리 수집·테스트·빌드·소스 변경은 수행하지 않았고, 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 12:34:54

- 자동 재개 절차에 따라 계약·누적 보고서·Git·A 기준선을 처음부터 다시 읽었다. 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97c`, 기존 사용자 변경·미추적 산출물, A `SM-P610`/Kiosk RC55·Web RC117과 Device Owner·전경 Kiosk `MainActivity`가 직전 기준선과 동일했다.
- 다음 반복 감사 영역은 Android 직접 인쇄·QR 카드 `needsPrint` 상태, PC ACK delivery와의 분리, Room migration/운영 문서·회귀시험 연결이다. `LUNA-0039`·`LUNA-0037`의 상태 semantics 및 검증 공백을 우선 재대조하고, 새 독립 결함은 근거가 있을 때만 추가한다.
- 이번 기준선 복원에서는 A 화면 조작·PIN·QR·답안·학생/반/세션·프린터·PC receiver·네트워크·파일·DB·메모리 수집·테스트·빌드·Git 쓰기를 수행하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

- Android 직접 인쇄 경로를 `QrCardStatusDao`·`StudentRepository.markCardsDelivered()`·단건/반 전체 `PrintManager` callback·print adapter `onFinish()`·Room migration·repository/adapter/migration 시험과 `CONTINUOUS_DEVELOPMENT_GOAL.md`, `KNOWN_LIMITATIONS.md`, `BUILD_VERIFICATION.md`, 현장 checklist에 연결해 읽기 전용으로 재대조했다.
- local PrintManager handoff 성공은 `needsPrint`/`lastDeliveredAt`를 갱신하지 않고 PC ACK만 상태를 지우며, v1→v2 migration은 기존 학생을 전달 완료로 합성한다. 직접 인쇄는 운영상 보류·미검증이고 체크리스트도 상태 일관성 시험을 완료하지 않았으므로 `LUNA-0039` P4 후보를 유지·보강했으며 새 독립 finding은 추가하지 않았다.
- 실제 PrintManager·프린터·대기열·QR·학생/DB·A 조작·ADB mutation·테스트·빌드·네트워크·파일 생성은 수행하지 않았고, 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 12:45:26

- 자동 재개 후 계약·누적 보고서 전체와 Git/A 기준선을 읽기 전용으로 확인했다. 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97c`, 소스 범위 변경 0, A `SM-P610`·Kiosk RC55/Web RC117·Device Owner·전경 Kiosk `MainActivity`는 이전 기준선과 동일했다.
- PC `ReceiverApplication`·`ThreadedReceiverServer`·`ConfigStore`·smoke/app/server 시험·설치/제거 스크립트와 `pc_receiver/README.md`, `RELEASE_OPERATIONS.md`, `BUILD_VERIFICATION.md`를 읽기 전용으로 대조했다. listener/server daemon thread가 Tk·tray 초기화보다 먼저 시작되고, constructor 예외 처리에 partial listener cleanup이 없으며, `--background`의 tray thread 실패를 감시·복구하는 경계가 없음을 `LUNA-0042` P4 후보로 기록했다.
- 정상 `shutdown()`의 server close와 Python `ThreadingMixIn` active handler 회수는 확인했으므로 `LUNA-0007`·`LUNA-0010`·`LUNA-0041`에 중복시키지 않았다. 실제 PC receiver·Tk/tray·TCP·포트·파일·CSV/PDF·네트워크·오류 주입·테스트·빌드는 수행하지 않았다.
- 이번 반복에서 허용된 지속 변경은 이 보고서의 `apply_patch` 갱신뿐이며, 소스·테스트·문서·설정·스크립트·산출물·진단 자료·A/PC 상태·Git 쓰기는 변경하지 않았다.

### 2026-08-02 12:56:59

- 자동 재개 후 계약 문서와 누적 보고서를 처음부터 끝까지 읽고, Git 기준선과 승인된 ADB 상태를 읽기 전용으로 재확인했다. 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97c`, source scope 변경 0, A `SM-P610`·Kiosk RC55/Web RC117·Device Owner·전경 Kiosk `MainActivity`는 직전 기준선과 동일했다.
- Kiosk `AdminAuthRepository`·`AdminPin`·`AdminDao`/`AdminCredentialEntity`, 일반 관리자 인증·수업 관리자 PIN 대화상자의 `executeSensitive()` 호출, `AdminPinTest`·`MainActivityInstrumentedTest`, `SECURITY.md`와 `KNOWN_LIMITATIONS.md`를 읽기 전용으로 대조했다. lockout은 `System.currentTimeMillis()` 기반 persisted wall-clock을 사용하고 `elapsedRealtime()`·시각 변경 감시·monotonic 보정은 없지만, 이 제한과 승인된 ADB/관리자 시각 변경·앱 데이터 삭제에 대한 비방어 범위가 `docs/KNOWN_LIMITATIONS.md`에 이미 명시되어 있다.
- 일반 인증과 수업 관리자 인증은 같은 Activity의 단일 `ioExecutor`와 UI busy/submitting guard로 직렬화되는 정적 호출 경계를 확인했다. 테스트는 lockout 지연 공식·PIN 형식/검증과 UI 자동 제출을 다루지만 wall-clock jump·재부팅·동시 repository 호출은 다루지 않는다. 기존 알려진 제한을 재확인했으며 새 LUNA finding·summary row는 추가하지 않았다.
- 이번 반복은 실제 시각 변경·PIN·DB·앱 상태·ADB mutation·테스트·빌드·설치·네트워크를 수행하지 않았고, 허용된 지속 변경은 이 누적 보고서의 `apply_patch` 갱신뿐이다.

### 2026-08-02 13:05:40

- 자동 재개 절차에 따라 계약 문서와 누적 보고서를 처음부터 끝까지 읽고, Git 상태·source scope·브랜치/HEAD/upstream/log와 승인된 ADB `device`·모델·Kiosk/Web 버전·Device Owner·전경 Activity를 읽기 전용으로 재확인했다. 기존 사용자 변경·미추적 산출물과 source scope 변경 0은 직전 기준선과 동일했다.
- Kiosk `KioskDatabase.kt`·Room schema 1/2/3·`Entities.kt`·credential cipher·manifest/data extraction rules·debug manifest·FileProvider/network security XML, migration 계측시험, `MainActivity.loadInitialState()`와 `AdminAuthRepository.enrolledPinLength()`, `GATE4_IMPLEMENTATION.md`·`SECURITY.md`·`THREAT_MODEL.md`·`KNOWN_LIMITATIONS.md`·Kiosk Gradle 설정을 읽기 전용으로 대조했다.
- Room 전체 SQLCipher가 없는 점은 문서화된 생산 계약(자격정보 username/password 필드만 Keystore AES-GCM, 표시명·내부 UUID·반 관계·QR hash·비민감 audit event는 앱 private DB 평문)과 일치한다. backup/device transfer는 manifest와 data extraction rules에서 전 영역 제외되고, DB는 명시적 migration만 사용하며 destructive fallback은 없다. 새 at-rest·backup·migration 무결성 finding은 추가하지 않았다.
- `MIGRATION_1_2`의 기존 카드 전달 상태 합성은 직접 인쇄/physical delivery 미검증인 기존 `LUNA-0039` 범위에 남겼다. `MIGRATION_2_3`의 `pinLength=0`은 `enrolledPinLength()`에서 자동 제출을 잠시 끄고 수동 인증 성공 뒤 실제 길이를 저장하는 호환 경로로 정적 해석했으며, migration 시험은 이 UI bootstrap을 검증하지 않는다. 기능 고장으로 확정하지 않고 미검증 회귀 항목으로만 기록했다.
- 실제 DB 파일·복원·backup/transfer·migration fault·PIN·ADB mutation·테스트·빌드·설치·소스/문서 변경은 수행하지 않았고, 허용된 지속 변경은 이 누적 보고서의 `apply_patch` 갱신뿐이다.

### 2026-08-02 13:16:32

- 자동 재개 절차에 따라 계약 문서와 누적 보고서를 처음부터 끝까지 읽은 뒤 Git 상태·source scope·브랜치/HEAD/upstream/log와 승인된 ADB `device`·모델·Kiosk/Web 버전·Device Owner·전경 Activity를 읽기 전용으로 재확인했다. 기준선은 직전과 동일했다.
- 최근 HEAD의 persisted test-answer cleanup 계약, Web `MainActivity`의 logout navigation·post-clear login fingerprint·local storage/cookie cleanup, `WebDomScripts.sanitizeLoginAndFingerprint`, Kiosk `webSessionLauncher`/`WebSessionResultPersistence`, 관련 JVM/계측시험과 운영 문서를 읽기 전용으로 대조했다.
- `removeAllCookies` callback은 callback 인자를 사용하지 않고 login URL 재로드로 이어지며, login sanitizer와 Kiosk session persistence는 서버 공식 답안 `unanswered`, 과제 식별자, 동일 과제 재진입·지연 복원 proof를 다루지 않는다. 이는 기존 `LUNA-0040` P3 후보의 local-success/server-state binding 공백을 보강하며 새 finding은 추가하지 않았다.
- 서버 로그아웃의 자동 임시답안 폐기·세션 격리 계약, 실제 공식 상태 저장·완전 종료·같은 과제 재진입·60초 관찰은 확인하지 않았다. Web/Kiosk·A·답안·서버·네트워크·테스트·빌드·소스 변경은 수행하지 않았고, 이번 반복의 지속 변경은 이 보고서 `apply_patch`뿐이다.

### 2026-08-02 13:27:50

- 자동 재개 계약에 따라 계약 문서와 누적 보고서를 처음부터 끝까지 읽은 뒤 Git/A 기준선을 읽기 전용으로 재확인했다. 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97c`, 최신 commit `docs: require persisted test-answer cleanup verification`은 직전 기준선과 동일했다.
- 전체 Git 상태의 기존 사용자 변경·미추적 `diagnostics/`, `output/`, `tmp/`, 계약 문서는 보존했고, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope 변경은 없었다. A `SM-P610`, Kiosk `0.6.0-rc55`/versionCode 60, Web `0.4.0-rc117`/versionCode 134, Device Owner, 전경 Kiosk `MainActivity`도 동일했다.
- 다음 읽기 전용 반복은 직전 Web 로그아웃/서버 답안 감사와 중복하지 않도록 Android 직접 인쇄의 `needsPrint`·`lastDeliveredAt` 상태 semantics와 Room migration/시험·운영 문서 연결을 재대조한다. 실제 인쇄·프린터·QR·학생/DB·A 조작, 테스트·빌드·설치·Git 쓰기는 수행하지 않는다.

### 2026-08-02 13:32:08

- Android `MainActivity`의 단건·반 전체 `PrintManager.print()` 경로, `StudentRepository`/`QrCardStatusDao`의 `needsPrint`·`markCardsDelivered`, 두 print adapter의 `onFinish()`, `QrPrintDocumentAdapterInstrumentedTest`·`RepositoryInstrumentedTest`·`KioskDatabaseMigrationInstrumentedTest`와 운영/현장 문서를 읽기 전용으로 교차 대조했다.
- source 전체 호출 검색에서 `needsPrint=false` 전이는 PC PDF 저장 ACK 뒤 `markCardsDelivered()`에만 연결되고, local PrintManager 성공은 감사기록·화면/bitmap 정리만 수행한다. 어댑터 시험은 PDF geometry/bitmap cleanup, repository 시험은 명시적 `markCardsDelivered()` 호출만 검증해 local print outcome과 상태 수렴은 증명하지 않는다. 이 사실을 기존 `LUNA-0039` P4 후보의 마지막 확인으로 갱신했다.
- `MIGRATION_1_2`는 기존 학생의 물리 전달 여부를 읽을 입력 없이 `lastDeliveredAt=updatedAt`·`needsPrint=0`을 합성하고, migration 시험은 그 합성값만 확인한다. 현재 운영 문서가 Android 직접 인쇄를 보류하고 지정 PC 전송을 기본으로 하며 현장 checklist가 직접 인쇄·상태 일관성을 미검증으로 남기므로 새 독립 finding은 추가하지 않았다.
- 추론상 직접 PrintManager handoff 뒤 pending 상태가 남아 후속 pending-card 재발급 판단과 충돌할 수 있으나, `PrintManager`의 실제 취소/완료 callback·물리 출력·재발급 순서는 미검증이다. 실제 프린터·QR·학생/DB·A 조작과 테스트·빌드·설치는 계약상 수행하지 않았고, 이번 반복의 지속 변경은 이 보고서 `apply_patch`뿐이다.

### 2026-08-02 13:34:01

- Kiosk `KioskLockTaskController`, `DedicatedDevicePolicy`, `SessionPreflightPolicy`, `MainActivity`의 configure/enter/relock/preflight 호출부, 정책 단위시험과 Lock Task 운영·제한 문서를 읽기 전용으로 교차 대조했다.
- `enterRestrictedMode()`는 `startLockTask()`가 예외를 내면 `DISALLOW_CREATE_WINDOWS`를 지우지만, 예외 없이 반환한 뒤 현재 mode가 `LOCKED`가 아니면 `Result.success(false)`와 restriction 잔류를 반환한다. `MainActivity.enterDedicatedMode()`는 `isFailure`만 반영하고 false 결과·현재 mode를 수업/QR/인증 흐름 차단 조건으로 사용하지 않는다. 기존 `LUNA-0022` P3 후보를 보강했으며 새 ID는 추가하지 않았다.
- `SessionPreflightPolicy`는 Device Owner·allowlist·Web 보호·policy failure만 검사하고 `DedicatedDeviceMode`/enter 결과를 입력으로 받지 않는다. `DedicatedDevicePolicyTest`·`SessionPreflightPolicyTest`는 status label·정상 preflight만 다루며 false/예외/PINNED·restriction rollback·화면 진행 연결 시험은 찾지 못했다.
- 현재 A의 `NONE`은 관리자 화면에서 의도적으로 잠금을 해제한 상태일 수 있어 재현 증거로 쓰지 않았다. 실제 Lock Task fault·PINNED·restriction 조회/변경·QR/인증/카메라 조작, 테스트·빌드·설치는 수행하지 않았고, 이번 반복의 지속 변경은 이 보고서 `apply_patch`뿐이다.

### 2026-08-02 13:42:11

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 끝까지 읽은 뒤 Git·A 기준선을 읽기 전용으로 복원했다. 계약 문서는 25줄 단위로 시작부터 EOF까지, 누적 보고서는 50줄 단위로 시작부터 EOF까지 확인했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`, 최신 commit `docs: require persisted test-answer cleanup verification`으로 직전과 동일했다. 전체 status의 기존 변경은 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`, `diagnostics/`, `output/`, `tmp/`, 계약 문서이며 보존했다. `kiosk`·`webpoc`·`pc_receiver`·`scripts` 범위의 diff는 없었다.
- 승인된 ADB `device` 한 대 `SM-P610`, Kiosk `0.6.0-rc55`/versionCode 60, Web POC `0.4.0-rc117`/versionCode 134, Kiosk Device Owner, 전경 `com.local.matholickiosk.kiosk/.MainActivity`를 확인했다. Lock Task·PIN·QR·학생/세션·답안·프린터·PC receiver·네트워크·DB·테스트·빌드는 사용하지 않았고, 이번 지속 변경은 이 보고서 `apply_patch`뿐이다.
- 다음 읽기 전용 감사는 `LUNA-0028`의 관리자 PIN verifier `salt`·`derivedKey` 배열 및 full entity 조회 ownership을 `AdminAuthRepository`·`AdminPin`·DAO/Room binding·초기 bootstrap·관련 시험/문서와 재대조한다. 새 결함은 독립 근거가 있을 때만 추가하고, 기존 후보는 반대 가설과 미검증 범위를 함께 유지한다.

### 2026-08-02 13:45:50

- `AdminAuthRepository`·`AdminPin`·`AdminCredentialEntity`·`AdminDao`·Room schema/migration과 MainActivity 초기 bootstrap·일반/수업 관리자 인증 호출부를 읽기 전용으로 재대조했다. `AdminDao.get()`은 `isEnrolled()`·`enrolledPinLength()`에도 `SELECT *` 전체 row를 materialize하고, `authenticate()`는 entity의 salt·derivedKey를 `PinVerifier`에 alias한다.
- `AdminPin.verify()`는 candidate derived key만 `fill(0)`하며 verifier 배열은 유지하고, `enroll()`은 DB save 성공·예외 뒤 local verifier 배열을 정리하는 `finally`가 없다. `PinVerifier`/AdminAuthRepository에는 persisted BLOB와 transient copy를 분리하는 owner/clear API가 없다. 기존 `LUNA-0028` P4 후보를 보강했으며 새 ID는 추가하지 않았다.
- `AdminPinTest`는 형식·일치·lockout, `SensitiveTaskTest`는 generic cleanup 한 번 실행, MainActivity 계측은 PIN 자동 제출/길이와 `FLAG_SECURE`, migration 시험은 `pinLength=0`·row 보존만 검증한다. repository 조회/인증/등록·Room binding 뒤 verifier 배열 수명 시험은 찾지 못했다. `SECURITY.md`와 Gate 4 문서는 salted PBKDF2 저장·입력 wipe를 설명하지만 verifier transient ownership은 정의하지 않는다.
- 실제 PIN·DB 인증/등록·Room runtime·heap/GC·ADB·테스트·빌드·설치는 수행하지 않았다. salt는 비밀값이 아니고 derivedKey는 저장용 verifier이며, Room generated copy/liveness를 검증하지 않았으므로 P4 후보와 중간 신뢰도를 유지한다. 이번 반복의 지속 변경은 이 보고서 `apply_patch`뿐이다.

### 2026-08-02 13:47:55

- `LUNA-0028` 관리자 PIN verifier ownership·scalar-only 조회·Room binding·초기 bootstrap·관련 시험/문서 모듈 검토를 완료했다. 기존 P4 후보를 보강했으며 새 독립 ID는 추가하지 않았다.
- 다음 긴 읽기 전용 작업은 `pc_receiver` startup/tray/background lifecycle의 listener 선행 bind와 부분 초기화 rollback을 `ReceiverApplication`·`ThreadedReceiverServer`·설치 스크립트·README/운영 문서·app/server 시험으로 재대조한다. 실제 수신기 실행·Tk/tray fault injection·TCP/포트·파일·네트워크·테스트·빌드는 수행하지 않는다.

### 2026-08-02 13:51:14

- `pc_receiver/app.py`·`server.py`의 단계별 줄 범위와 `test_app.py`·`test_server.py`를 다시 대조했다. listener는 `ThreadedReceiverServer` 생성 중 bind되고 server daemon thread가 Tk·tray보다 먼저 시작된다. `main()`의 constructor 예외 경로에는 partial server cleanup이 없고, tray daemon thread의 예외·정상 반환을 감시해 숨겨진 background root와 listener를 fail-closed하는 경계도 없다.
- `docs/RELEASE_OPERATIONS.md`와 `docs/BUILD_VERIFICATION.md`의 2026-07-29 재부팅 자동 시작·TCP 48129 대기 성공 기록을 함께 반영했다. 정상 startup/shutdown을 결함으로 확대하지 않고, 예외·부분 초기화·tray 실패에 한정한 기존 `LUNA-0042` P4 후보를 보강했으며 새 독립 ID는 추가하지 않았다.
- 이번 반복에서 수신기·Tk·tray·TCP·포트·파일·네트워크·Windows 자동 시작·오류 주입·테스트·빌드는 수행하지 않았다. 계약상 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 13:54:04

- `pc_receiver/app.py`·`server.py`·`protocol.py`·config와 PC app/server/protocol 시험을 다시 대조했다. `choose_csv()`의 `payload = b""`는 local reference만 바꾸고, `queue_csv()`·`clear_csv()`·교체·shutdown은 immutable pending payload와 response plaintext/ciphertext/frame을 명시적으로 wipe하지 않는다. `CONTROL_FETCH_CSV`는 frame 생성 뒤 실제 `sendall()` 전에 pending을 소비한다.
- Kiosk `PcControlClient`의 request/frame/header/intermediate cleanup, `MainActivity` fetch payload `finally`, `StudentRepository.importStudents()`의 Android row `finally`를 확인했다. 이 경계들은 Android 쪽 cleanup을 보강하지만 PC receiver volatile buffer나 send-before-confirm queue durability를 해결하지 않는다. `LUNA-0041` P4 후보와 `LUNA-0003` 확정 항목을 각각 보강했으며 새 독립 ID는 추가하지 않았다.
- 실제 CSV·학생 계정·PC receiver·TCP/네트워크·heap/allocator·오류 주입·테스트·빌드는 수행하지 않았다. 계약상 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 13:57:07

- Kiosk `MainActivity`의 `studentMutationGate`, `updateClassRosterUi()`/`updateStudentManagementControls()` enabled 조건, 반 생성·삭제·소속 변경 handler, `prepareBatchQrPrint()`와 `StudentRepository.reissueClassQrBatch()`를 다시 대조했다. 개별 학생 변경 gate와 `batchQrButton`/class mutation은 분리되어 있고, batch task가 만든 `BatchQrCard` bitmap은 `BatchQrPrintDocumentAdapter.onFinish()`까지 유지되지만 mutation revision 재검사·인쇄 취소 owner는 없다.
- `MainActivityInstrumentedTest`에는 batch button과 student mutation gate의 교차 callback/enabled matrix가 없고, `RepositoryInstrumentedTest`는 batch 내부 QR rotation transaction 원자성만 확인한다. 운영 문서·현장 checklist도 batch와 student/class mutation 교차 순서·폐기 QR 인쇄를 미검증으로 남긴다.
- 실제 학생·반·QR·인쇄·PDF·DB·Activity timing·테스트·빌드는 수행하지 않았다. 반 transaction 원자성은 확인해 DB partial write로 확대하지 않고 기존 `LUNA-0036` P3 후보를 보강했으며 새 독립 ID는 추가하지 않았다. 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 14:01:26

- `preparePendingCardsPdf()`의 전체 단일 `ioExecutor` runnable, 학생·반 mutation handler의 같은 executor 제출, `reissueQrBatch()` transaction, PC ACK, `markCardsDelivered(studentIds)`와 `PcPdfSender` cleanup을 다시 대조했다. 현재 call graph만으로 `reissueQrBatch()`와 `markCardsDelivered()` 사이의 mutation interleave는 입증되지 않는다.
- 따라서 기존 `LUNA-0037` P3 후보를 실제 동시 race가 아니라 전송 후 student mutation을 허용할지, PC artifact를 언제 폐기·재생성할지, `markCardsDelivered()`를 issued QR revision/request ID에 묶을지의 정책·검증 공백으로 정교화했다. `MainActivityInstrumentedTest`의 pending-PDF/학생 mutation callback matrix와 issued revision 검증은 여전히 없다.
- 실제 학생·QR·PC PDF·ACK·DB·네트워크·인쇄·Activity timing·테스트·빌드는 수행하지 않았다. source·시험·운영 checklist만 읽기 전용으로 대조했고, 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 14:03:35

- `WebDomScripts.applyStudentExperience()`의 `window.__matholicKioskProblemStates` 생성·문항 번호별 기록·현황/`navigateToUnanswered()` 읽기, `/workbook`↔`/diagnostic` exact trusted link의 SPA click을 읽기 전용으로 재대조했다. path/task key, reset, `pushState`/`popstate` 계열 연결은 확인되지 않았다.
- `MainActivity`의 `activeExperienceGeneration`과 `prepareStudentContentReveal()`은 stale Kotlin callback 차단·target path 안정화/화면 차폐를 수행하지만 Web problem-state map owner를 새 task로 바꾸지 않는다. 단일 문제 fixture·SPA link fixture 시험은 이 교차 경계를 검증하지 않고, 운영 기록은 같은 과제 재진입/60초 관찰만 다룬다.
- full document reload가 항상 발생하면 map이 자연 초기화될 수 있다는 반대 가설을 유지하고, 실제 과제·답안·서버·A를 사용하지 않은 상태에서 `LUNA-0038` P3 후보를 유지했다. 새 독립 ID는 추가하지 않았으며 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 14:07:28

- Kiosk `QrCardStatusDao`·`StudentRepository`의 `needsPrint`/`markCardsDelivered`, `MainActivity`의 단건·반 전체 `PrintManager.print()` handoff, 두 `PrintDocumentAdapter.onFinish()`, pending-card dialog와 관련 repository/adapter/migration 시험·운영 문서를 읽기 전용으로 다시 대조했다. 현재 호출 graph에는 local PrintManager success/cancel/failure를 `needsPrint=false` 또는 `lastDeliveredAt`로 수렴시키는 경계가 없다.
- `MIGRATION_1_2`의 `lastDeliveredAt=updatedAt`·`needsPrint=0`은 물리 카드·PC ACK·Android 인쇄 결과를 읽지 않고 합성되며, migration 시험은 그 합성값만 확인한다. 직접 인쇄를 운영상 보류하고 지정 PC ACK 경로만 전달 완료로 기록하는 정책 가능성을 반영해 `LUNA-0039` P4 후보를 유지·보강했고 새 독립 ID는 추가하지 않았다.
- 실제 PrintManager·프린터·대기열·QR·학생/DB·A 조작, ADB mutation, 테스트·빌드·설치·네트워크는 수행하지 않았다. 계약상 지속 변경은 이 누적 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 14:10:54

- Web POC `finishButton`→`beginLogout()`→portal exact logout click→login DOM sanitizer→WebView local cleanup→`RESULT_OK` 경로, Kiosk `webSessionLauncher`·`persistWebSessionResult()`·`WebSessionResultPersistence`, 관련 local cleanup/DOM/Kiosk failure 시험과 운영 문서를 읽기 전용으로 다시 대조했다.
- 현재 경로는 local login fingerprint와 WebView 저장소/cookie 정리를 확인한 뒤 Kiosk `PRELOGIN_CHECK → QR_READY`를 수행하지만, 공식 서버 임시답안의 `unanswered` 저장·과제 식별자·동일 과제 재진입·60초 지연 복원 결과를 callback 입력으로 받지 않는다. RC117 정정 기록은 이 검증 공백의 운영 위험을 보강했으나 서버 cleanup 실패를 source만으로 확정하지는 못하므로 기존 `LUNA-0040` P3 후보를 유지하고 새 ID는 추가하지 않았다.
- 실제 Web·시험계정·답안·서버·재진입·60초 관찰·A·QR·네트워크, 테스트·빌드·설치는 수행하지 않았다. 계약상 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 14:15:13

- Kiosk `pendingRecoveryAction` 생성·소비, `webRecoveryLauncher` ActivityResult callback, `completeSessionStart/End()`, `onCreate()`·`onStart()`·`onDestroy()`와 `ioExecutor.shutdownNow()`를 읽기 전용으로 대조했다. `pendingRecoveryAction`은 Activity 일반 필드이며 `onSaveInstanceState`·saved-state/DB 복원 연결은 확인되지 않았다.
- Web recovery 결과 callback이 새 Kiosk Activity에 전달되더라도 새 인스턴스가 `PendingRecoveryAction.None`을 소비하면 Web 정리 성공 뒤 `startSession()`/`endSession()` 대신 일반 refresh 경로로 끝날 수 있다는 조건부 P4 후보를 유지했다. ActivityResult 재전달의 실제 Android lifecycle과 후속 DB 전이는 실행하지 않았고, 재시작 시 DB 초기 복구가 완화할 수 있다는 반대 가설도 유지했다.
- 실제 Activity recreation/process reclaim·Web recovery·수업/DB·A·테스트·빌드는 수행하지 않았다. 계약상 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 14:13:14

- Kiosk `webRecoveryGate`의 모든 참조, `updateSessionAdminControls()`·`updateClassRosterUi()`·`updateStudentManagementControls()`·`updateQuickClassButtons()` enabled 조건, `PendingRecoveryAction.StartSession/EndSession` 생성·소비, `completeSessionStart/End()`와 `StudentRepository` session transaction을 읽기 전용으로 다시 대조했다.
- recovery 중 start/resume/class spinner/self-test 일부만 gate에 연결되고 반 구성·삭제·보강·batch QR·학생/CSV/QR mutation과 quick class button은 별도 gate 없이 남는다. preflight에서 캡처한 class/temporary IDs는 recovery 후 `startSession()`에 전달되지만 현재 membership/선택 mutation revision 재검증은 없다. 저장소 transaction이 active class·학생·기존 session 불변조건을 확인하므로 DB partial write로 확대하지 않고 기존 `LUNA-0035` P3 후보를 유지·보강했다.
- 실제 Web recovery·버튼·반·학생·CSV·QR·수업·DB·Activity timing·A 조작, 테스트·빌드·설치는 수행하지 않았다. 계약상 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.

### 2026-08-02 14:20:10

- Kiosk `MainActivity`의 `offerAdminUndo()`·`clearPendingAdminUndo()` 전체 호출부, `PendingAdminUndo` 세 가지 action, `updateStudentManagementControls()`의 enabled 목록, `showAdmin()`·`showAuthentication()`·`showScanner()`·`onStop()` lifecycle을 읽기 전용으로 다시 대조했다. undo action은 반 삭제·소속 변경·학생 이름 변경 성공에서만 교체되고, 학생 등록·CSV·QR 재발급·자격정보 변경·비활성화·반 생성에는 이전 action을 무효화하는 공통 호출이 없다.
- `undoAdminButton`은 `studentMutationGate` 갱신 대상에서 빠져 있고, 관리자 mutation과 `performPendingAdminUndo()`가 `Executors.newSingleThreadExecutor()`에 직렬 제출되지만 operation generation·최신 snapshot 비교는 없다. 따라서 학생 mutation 중 버튼 클릭은 동시 실행이 아니라 stale undo와 후속 작업의 queue ordering 경계로 남으며, 화면을 나갔다 재인증하거나 수업 관리자 화면으로 돌아와도 같은 Activity의 pending action이 자동 폐기되지 않는다. 기존 `LUNA-0024` P4 후보를 lifetime/cross-operation 근거로 보강했고, 활성 session의 membership eligibility 영향은 `LUNA-0025`로 유지·분리했다.
- `StudentRepository.reissueQr()`의 active-session 검사 부재와 `deactivateStudent()`의 활성 session 허용 시험을 확인했지만, 실제 학생·QR·세션 변경을 실행하지 않았다. 운영 시나리오·`BUILD_VERIFICATION.md`의 개별 실행취소/30초 만료 기록과 `AdminUiAsyncStateTest`·Kiosk 계측/Repository 시험 검색에서는 교차 action invalidation, undo enabled matrix, student mutation 중 클릭, 화면 재진입 순서 검증을 찾지 못했다.
- 실제 학생·반·CSV·QR·자격정보·DB·세션·Activity timing·A 조작, 테스트·lint·빌드·설치·네트워크·Git 쓰기는 수행하지 않았다. 계약상 지속 변경은 이 보고서만 `apply_patch`로 누적 갱신했으며, 다음 읽기 전용 검토 영역은 `LUNA-0025` 활성 session membership 실행취소 연결이다.

### 2026-08-02 14:23:10

- Kiosk `updateClassRosterUi()`·`updateSessionAdminControls()`의 active-session enabled 조건, 세션 관리자 재진입, `undoAdminButton`/`performPendingAdminUndo()` 연결을 다시 대조했다. 일반 반 구성·반 선택은 잠기지만 pending `RestoreMemberships` action에는 같은 session gate나 `webRecoveryGate` 검사가 없다.
- `StudentRepository.replaceClassMemberships()`는 active class·active student만 확인하고 session 존재/현재 session class ID를 확인하지 않는다. `startSession()`은 `temporaryStudentIds`만 `session_students`에 기록하고 class membership snapshot은 만들지 않으며, `StudentDao`의 QR·수동 선택 query는 현재 반 `class_memberships`와 현재 session `session_students`의 live OR join을 사용한다. 기존 `LUNA-0025` P3 후보를 유지·보강했고, action 반이 현재 session 반과 다를 때 즉시 eligibility가 바뀐다고 과장하지 않았다.
- `RepositoryInstrumentedTest`의 정상 manual eligibility/temporary 학생 시험과 active-session 단건 QR 재발급 시험은 확인했지만, session 시작 뒤 membership 교체·pending undo·그 직후 QR/수동 선택의 연속 순서는 찾지 못했다. 실제 session·학생·QR·DB·A·PIN·수업 조작, 테스트·빌드·설치·네트워크·Git 쓰기는 수행하지 않았다.
- 계약상 지속 변경은 이 보고서만 `apply_patch`로 누적 갱신했으며, 다음 읽기 전용 검토 영역은 `LUNA-0026` session 종료 transaction 후 관리자 snapshot refresh 연결이다.

### 2026-08-02 14:25:47

- Kiosk `completeSessionEnd()` 성공/실패 callback, `StudentRepository.endSession()` transaction, `refreshAdminData()` snapshot·failure branch, `updateSessionAdminControls()`와 관련 `currentSession` UI enabled/visibility를 읽기 전용으로 다시 대조했다. DB 종료 transaction은 `ADMIN_IDLE`을 저장하지만 성공 callback은 Activity `currentSession`을 먼저 null로 만들지 않고 후속 refresh에 의존한다.
- `endSession()`과 후속 refresh는 같은 단일 `ioExecutor`라 정상 ordering에서는 idle snapshot으로 수렴한다. 그러나 post-commit `ensureClasses()`·class/student/session/membership read가 실패하면 failure branch가 기존 메모리 active session을 유지하고 stale 종료/보강/QR 대기 UI를 다시 그릴 수 있다. `MainActivityInstrumentedTest`의 refresh fault 시험은 일반 refresh/roster failure와 student gate 해제를 다루고, repository 시험은 정상·중복 end만 다뤄 이 순서를 검증하지 않는다. 기존 `LUNA-0026` P4 후보를 유지·보강했고, 일반 executor race나 DB partial write로 확대하지 않았다.
- 실제 session·DB·저장소 fault injection·버튼·카메라·QR·A 조작, 테스트·빌드·설치·네트워크·Git 쓰기는 수행하지 않았다. 계약상 지속 변경은 이 보고서만 `apply_patch`로 누적 갱신했으며, 다음 읽기 전용 검토 영역은 `LUNA-0028` PIN verifier ownership 연결이다.

### 2026-08-02 14:29:25

- `AdminAuthRepository`·`AdminPin`·`PinVerifier`·`AdminCredentialEntity`·`AdminDao`, MainActivity 초기 bootstrap/auth 호출부, Room migration과 관련 JVM/계측시험을 읽기 전용으로 다시 대조했다. `isEnrolled()`·`enrolledPinLength()`는 scalar-only query가 아닌 full entity 조회를 각각 수행하고, `enroll()`의 verifier arrays와 `authenticate()`의 entity/`PinVerifier` arrays는 alias될 수 있지만 입력 PIN·candidate key만 정리된다.
- `entity.copy()`가 ByteArray를 deep-copy하지 않는 경계와 lockout/성공·실패 저장 분기를 확인했다. `AdminPinTest`·`SensitiveTaskTest`·MainActivity PIN/length/`FLAG_SECURE` 시험·migration 시험은 알고리즘/화면/schema만 검증하고 repository·Room binding 뒤 salt/derivedKey ownership·heap 수명은 관찰하지 않는다. 기존 `LUNA-0028` P4 후보를 유지·보강했으며, PBKDF2 원문 PIN 노출이나 DB persisted bytes 삭제로 확대하지 않았다.
- `GATE4_IMPLEMENTATION.md`는 PBKDF2 verifier 저장과 입력 CharArray cleanup을 설명하고 `REMOTE_ADMIN_PIN.md`는 외부 PIN 보관/출력 금지를 설명하지만 transient verifier array ownership 계약은 확인하지 못했다. 실제 PIN·DB·Room runtime·heap/GC·ADB·테스트·빌드·설치는 수행하지 않았다.
- 계약상 지속 변경은 이 보고서만 `apply_patch`로 누적 갱신했으며, 다음 읽기 전용 검토 영역은 `LUNA-0029` loopback proxy resource lifecycle이다.

### 2026-08-02 14:33:40

- `LoopbackConnectProxy`·`ProxyLifecycle`의 cached executor, accept/CONNECT 양방향 copy task, header/connect timeout, `soTimeout=0` 전환과 active socket registry를 읽기 전용으로 다시 대조했다. 성공 tunnel에는 client/upstream 동시 수·worker/queue·idle timeout 상한이 없고, process-wide `WebViewProxyBootstrap` singleton은 Activity `onDestroy()`에서 닫히거나 reset되지 않는다.
- `CloseableRegistry`는 register와 closeAll을 동기화하고 종료 뒤 늦은 socket을 닫으며, `ProxyTaskSubmission`은 executor rejection cleanup을 수행한다. `ProxyBootstrapCoordinator` 시험도 지원 실패·override/timeout/rejection·late callback에서 partial proxy close를 확인한다. 이 반대 근거 때문에 shutdown-race socket leak로 확정하지 않고 기존 `LUNA-0029` P4 후보를 resource budget·idle lifetime·정상 process teardown 관찰 공백으로 유지했다.
- host allowlist/443·loopback binding·TLS pass-through, backlog 16, 연결 전/상류 연결 10초 timeout은 범위를 제한하지만, 실제 local scan·CONNECT·idle socket hold·FD/thread 고갈·WebView 동시성·process 종료는 확인하지 않았다. 실제 Web/A·네트워크·proxy·socket·테스트·빌드·설치·Git 쓰기는 수행하지 않았고, 계약상 지속 변경은 이 보고서만 `apply_patch`로 누적 갱신했다.
- 다음 읽기 전용 검토 영역은 `LUNA-0030` PC pairing QR 원문·Base64 secret String의 decode/save 경계 zeroization이다.

### 2026-08-02 14:37:53

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 현재 Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이며, 기존 사용자 변경·미추적 산출물과 `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope 무변경을 확인했다.
- 승인된 ADB `R54TB029FHZ`는 `SM-P610` Android 13 `device` 상태이고, Kiosk `0.6.0-rc55`/code 60·Web POC `0.4.0-rc117`/code 134·전경 Kiosk `MainActivity`를 읽기 전용으로 재확인했다. PIN·QR·학생·답안·세션·프린터·네트워크·테스트·빌드·설치·Git 쓰기는 수행하지 않았다.
- 다음 읽기 전용 반복은 직전 큐의 `LUNA-0030` PC pairing QR 원문·Base64 secret String의 scanner→decode/save→load/recovery 호출 graph, byte/string ownership, 시험·문서 계약을 재대조한다.

### 2026-08-02 14:41:00

- `QrImageAnalyzer` raw callback→`MainActivity.handleRawQr()`→UI callback→`savePcPairing()` executor lambda, `PcPairingStore.save/load`, `PcReceiverPairing.encode/decode/clearSensitiveData`, `PcEndpointResolver` candidate cleanup과 Activity `onDestroy()`를 읽기 전용으로 다시 대조했다. ML Kit/raw QR·복호화 plaintext·Base64 encode 결과는 immutable String이고, 저장소·resolver·호출부는 ByteArray만 정리한다.
- Activity 종료 시 `mainHandler`는 callback을 제거하지만 `ioExecutor.shutdownNow()`는 pairing 일반 lambda를 `SensitiveTask`처럼 discard하지 않는다. `PcEndpointResolverTest`·`PcPairingStoreInstrumentedTest`·protocol 시험은 암호화/round-trip/authenticated endpoint와 object ByteArray cleanup을 확인할 뿐 String heap/queue ownership을 확인하지 않는다. 기존 `LUNA-0030` P3 후보를 유지·보강했다.
- Android Keystore AES-GCM at-rest 보호, 사설 subnet 후보 제한·authenticated probe, raw secret 로그/평문 저장 부재는 반대 근거로 반영했다. 실제 pairing QR·저장·recovery·DB/Keystore runtime·heap dump·Activity 종료 timing·네트워크·테스트·빌드·설치·Git 쓰기는 수행하지 않았고, 이번 지속 변경은 이 보고서뿐이다.
- 다음 읽기 전용 검토 영역은 `LUNA-0031` CSV preview/apply executor 종료·거부 시 parsed credential `CharArray` lifecycle이다.

### 2026-08-02 14:46:13

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 현재 Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이며, 기존 사용자 변경·미추적 산출물과 `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope 무변경을 확인했다.
- 승인된 ADB `R54TB029FHZ`는 `SM-P610` Android 13 `device` 상태이고, Kiosk `0.6.0-rc55`/code 60·Web POC `0.4.0-rc117`/code 134·전경 Kiosk `MainActivity`를 읽기 전용으로 재확인했다. PIN·QR·학생·답안·세션·프린터·네트워크·테스트·빌드·설치·Git 쓰기는 수행하지 않았다.
- 다음 읽기 전용 반복은 직전 큐의 `LUNA-0031` CSV preview/apply executor 종료·거부 시 parsed credential `CharArray` ownership과 `SensitiveTask` 적용 범위를 재대조한다.

### 2026-08-02 14:47:58

- `StudentCsvImport`의 wipe 가능한 `StudentCsvRow`/`ParsedStudentCsv`, `MainActivity`의 PC fetch→parse→preview→Dialog apply, `StudentRepository.previewStudentImport/importStudents`, `SensitiveTask`와 Activity `onDestroy()`를 읽기 전용으로 다시 대조했다. preview/apply는 일반 `ioExecutor` lambda이고, `SensitiveTask` rejection·queued shutdown cleanup에 연결되지 않는다.
- 정상 preview 취소·preview failure·repository import 본문 진입에는 rows cleanup이 있지만, preview `ioExecutor.execute` rejection, `shutdownNow()` 반환 일반 lambda, apply ownership 이후 queued 제거·submission rejection에는 `parsed.clearSensitiveData()` owner가 없다. `SensitiveTaskTest`·`StudentCsvParserTest`·`RepositoryInstrumentedTest`는 이 lifecycle 경계를 검증하지 않는다. 기존 `LUNA-0031` P3 후보를 유지·보강했다.
- `BUILD_VERIFICATION.md`·`CONTINUOUS_DEVELOPMENT_GOAL.md`의 `SensitiveTask` 도입/대기 작업 cleanup 계약이 CSV preview/apply까지 확장됐다는 근거는 찾지 못했고, parser 반환 전 partial row 문제는 `LUNA-0032`로 분리했다. 실제 CSV·학생 계정·DB·Activity 종료 timing·heap/GC·테스트·빌드·설치·ADB·네트워크·Git 쓰기는 수행하지 않았으며, 이번 지속 변경은 이 보고서뿐이다.
- 다음 읽기 전용 검토 영역은 `LUNA-0032` CSV parser 후속 행 검증 실패 시 partial row credential `CharArray` lifecycle이다.

### 2026-08-02 14:51:03

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 현재 Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이며, 기존 사용자 변경·미추적 산출물과 `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope 무변경을 확인했다.
- 승인된 ADB `R54TB029FHZ`는 `SM-P610` Android 13 `device` 상태이고, Kiosk `0.6.0-rc55`/code 60·Web POC `0.4.0-rc117`/code 134·전경 Kiosk `MainActivity`를 읽기 전용으로 재확인했다. PIN·QR·학생·답안·세션·프린터·네트워크·테스트·빌드·설치·Git 쓰기는 수행하지 않았다.
- 다음 읽기 전용 반복은 직전 큐의 `LUNA-0032` `StudentCsvParser.parse()` 후속 행 검증 실패 시 partial `StudentCsvRow` ownership과 `MainActivity` parse catch 경계를 재대조한다.

### 2026-08-02 14:52:02

- `StudentCsvParser.parse()`의 `records.drop(1).mapIndexed` row 검증·`StudentCsvRow` 생성·`ParsedStudentCsv` 반환 순서와 `MainActivity`의 유일한 production parser 호출/parse catch/finally를 읽기 전용으로 다시 대조했다. 앞선 row가 생성된 뒤 후속 열·필드·반·중복 검증이 실패하면 partial destination은 반환되지 않고 payload ByteArray만 호출부에서 지워진다.
- `StudentCsvParserTest`의 duplicate fixture는 첫 row 생성 후 두 번째 중복에서 예외를 기대하지만 partial row array cleanup은 확인하지 않는다. 정상 parse 성공의 `ParsedStudentCsv.clearSensitiveData()`와 `LUNA-0031` 반환 후 executor ownership은 각각 반대 근거·별도 경계로 유지했다. 기존 `LUNA-0032` P3 후보를 유지·보강했다.
- header·빈 파일·행 수·닫히지 않은 quote처럼 row 생성 전 실패는 이 finding에서 제외했고, immutable `text`/field String 수명은 `LUNA-0013`으로 분리했다. 실제 CSV·학생 계정·DB·parser 실행·heap/GC·테스트·빌드·설치·ADB·네트워크·Git 쓰기는 수행하지 않았으며, 이번 지속 변경은 이 보고서뿐이다.
- 다음 읽기 전용 검토 영역은 `LUNA-0033` Kiosk PC status 단일 executor 무제한 queue와 CSV/self-test starvation·captured display name lifecycle이다.

### 2026-08-02 15:00:59

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 현재 Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이며, 기존 사용자 변경·미추적 산출물과 `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope 무변경을 확인했다.
- 승인된 ADB `R54TB029FHZ`는 `SM-P610` Android 13 `device` 상태이고, Kiosk `0.6.0-rc55`/code 60·Web POC `0.4.0-rc117`/code 134·Device Owner·전경 Kiosk `MainActivity`를 읽기 전용으로 재확인했다. PIN·QR·학생·답안·세션·프린터·네트워크·테스트·빌드·설치·Git 쓰기는 수행하지 않았다.
- 다음 읽기 전용 반복은 `LUNA-0033` Kiosk `pcControlExecutor` status queue의 호출 graph, timeout/recovery, CSV·자가진단 공유 worker, Activity 종료와 관련 시험·운영 문서를 재대조한다.

### 2026-08-02 15:03:48

- `MainActivity`의 `pcControlExecutor`, `reportPcStatus()` 전체 호출부, `withReachablePairedPc()`, `PcControlClient`, `PcEndpointResolver`를 읽기 전용으로 대조했다. status lambda는 상태·학생 표시명·알림 플래그를 캡처해 무제한 single-thread queue에 들어가며, 원래 PC timeout 뒤 resolver 6초·재시도 경로가 한 worker를 추가 점유할 수 있다.
- 같은 worker에 CSV fetch와 운영 자가진단이 제출되고 status 최신값 coalescing·세대 취소·우선순위·queue 상한은 없다. `activeStudentDisplayName`은 성공 status 제출 뒤 null로 바뀌어도 이미 제출된 lambda가 snapshot을 보유하며, failure·`onStop()`·`onDestroy()`에는 공통 clear/queued-task 소비가 없다. 실제 stale callback·queue 길이·사용자 체감 지연은 측정하지 않았다.
- resolver는 candidate ByteArray cleanup·future cancellation·executor 종료를 수행하고 정상 PC 경로는 빠르게 drain할 수 있다는 반대 근거를 확인했다. `PRODUCT_DECISIONS`·`BUILD_VERIFICATION`·`RELEASE_OPERATIONS`는 PC 실시간 상태·자가진단·정상 복구만 기록하며, Kiosk 시험 검색에는 status/CSV/self-test queue lifecycle 연결이 없었다. 기존 `LUNA-0033` P3 후보를 유지·보강했고 새 ID는 추가하지 않았다.
- 실제 PC offline·network recovery·CSV·자가진단·학생/QR·A 조작·heap/queue 계측·테스트·빌드·설치·Git 쓰기는 수행하지 않았으며, 이번 지속 변경은 이 보고서뿐이다.
- 다음 읽기 전용 검토 영역은 `LUNA-0034` 초기 PC pairing QR host·receiver identity trust와 private-network 호환성 경계이다.

### 2026-08-02 15:08:57

- 자동 재개 절차에 따라 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음, 승인된 ADB `R54TB029FHZ`/`SM-P610`·Kiosk `0.6.0-rc55`/code 60·Web POC `0.4.0-rc117`/code 134·Device Owner·전경 Kiosk `MainActivity`를 읽기 전용으로 재확인했다.
- PIN·QR·학생·답안·세션·프린터·네트워크 장애·테스트·빌드·설치·Git 쓰기는 수행하지 않았으며, 이번 기준선 갱신은 이 보고서만 `apply_patch`로 반영했다.
- 다음 읽기 전용 검토는 `LUNA-0034` 초기 PC pairing QR host·receiver identity trust와 private-network 호환성 경계이다.

### 2026-08-02 15:12:27

- `PcReceiverPairing`·`PcPairingStore`·`PcControlClient`·`PcPdfSender`·`PcControlProtocol`·`PcTransferProtocol`과 MainActivity pairing scanner/save/recovery 호출부를 재대조했다. Kiosk 초기 decode는 prefix·payload·ASCII host·display name 형식만 검사하고, 저장된 host/port는 direct socket endpoint로 사용된다.
- PC receiver `current_lan_ipv4()`·`ReceiverApplication.self.host`·QR 생성과 Python `Pairing` validation을 재대조했다. route probe 주소를 시작 시 한 번 QR에 넣지만 private Wi-Fi/interface/receiver identity의 독립 검증은 없고, `PcControlProtocol`·`PcTransferProtocol`의 receiver ID/secret 인증은 QR이 제공한 key material을 신뢰한 뒤의 전송 인증이다.
- `PcSubnetCandidates`/`PcEndpointResolver`의 같은 private `/24` authenticated recovery와 정상 운영자의 display-name 확인은 반대 근거로 남겼다. Kiosk/Python pairing·protocol·server 시험은 fixed private vector·Keystore round-trip·crypto binding·recovery 후보만 검증하며 fake/public/DNS/loopback/multicast QR와 initial attestation은 미검증이다. 기존 `LUNA-0034` P3 후보를 유지·보강했고 새 ID는 추가하지 않았다.
- 실제 QR 촬영·원문/hash·fake endpoint·public/DNS/loopback/multicast 연결·네트워크 변경·status/PDF/학생 데이터·PIN·테스트·빌드·설치·Git 쓰기는 수행하지 않았으며, 이번 지속 변경은 이 보고서뿐이다.
- 다음 읽기 전용 검토는 `LUNA-0036` student mutation·반 QR batch/인쇄 cross-operation 회귀시험·운영 문서 연결이다.

### 2026-08-02 18:00:56

- 자동 재개 절차에 따라 최우선 계약 문서를 처음부터 EOF까지 읽었다. 계약은 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`이며, 유일한 지속 산출물은 이 누적 보고서이고 Goal을 완료 처리하지 않는다는 EOF 규칙을 확인했다.
- 누적 보고서도 처음부터 EOF까지 읽고 4,467줄·467,105자·SHA-256 `0AA95D9D6939249668A8274A8DBF901A7C06B58BA35943A8ABFDE84718EB4932`로 복원했다. 직전 검토 완료 항목은 `LUNA-0027`이며 이번 대상은 `LUNA-0036`이다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`, `output/`, `tmp/`, 계약 문서를 보존했고, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 변경이 없다.
- 현재 PATH에는 `adb` executable이 없어 A 기기 상태·화면·QR·PIN·학생·반·수업·DB를 확인하지 않는다. 테스트·lint·typecheck·build·install·Git 쓰기와 실제 학생/반/QR/인쇄 변경도 수행하지 않으며, 다음 단계는 `LUNA-0036`의 student mutation·반 QR batch/PrintManager cross-operation 경계를 관련 소스·시험·문서·history로 읽기 전용 대조하는 것이다.

### 2026-08-02 15:15:15

- 자동 재개 절차에 따라 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음, 승인된 ADB `R54TB029FHZ`/`SM-P610`·Kiosk `0.6.0-rc55`/code 60·Web POC `0.4.0-rc117`/code 134·Device Owner·전경 Kiosk `MainActivity`를 읽기 전용으로 재확인했다.
- PIN·QR·학생·답안·세션·프린터·네트워크 장애·테스트·빌드·설치·Git 쓰기는 수행하지 않았으며, 이번 기준선 갱신은 이 보고서만 `apply_patch`로 반영했다.
- 다음 읽기 전용 검토는 `LUNA-0036` student mutation·반 QR batch/인쇄 cross-operation 회귀시험·운영 문서 연결이다.

### 2026-08-02 15:19:14

- `MainActivity`의 batch/student/class handler, `StudentRepository.reissueClassQrBatch()`·membership/profile/deactivation, `ClassRosterSelectionState`, `BatchQrPrintDocumentAdapter`, 관련 시험·운영 문서를 처음부터 정적 대조했다. batch 시작은 `batchQrButton`만 false로 만들고 `studentMutationGate`, 반 구성·삭제·class spinner·quick class 선택에는 공통 operation ID/generation이 없다.
- 단일 `ioExecutor`의 DB 직렬화와 batch transaction의 active 재확인은 partial transaction을 제한하는 반대 근거다. 그러나 transaction 후 `PrintManager` adapter가 `onFinish()`까지 캡처한 QR bitmap·표시명을 보유하고, mutation revision·최신 roster·인쇄 직전 QR 유효성 재검사는 없어 비활성화·이름 변경·소속 변경과 인쇄 서비스 소비가 교차하면 stale·무효 카드가 될 수 있는 기존 `LUNA-0036` P3 후보를 유지·보강했다.
- batch/class/student gate matrix, batch adapter의 mutation invalidation, PrintManager callback ordering 회귀시험과 이를 명시하는 운영 문서는 확인하지 못했다. 실제 학생·반·QR·인쇄·DB·프린터·네트워크 장애·테스트·빌드·설치·Git 쓰기는 수행하지 않았으며, 지속 변경은 이 보고서만 `apply_patch`로 반영했다.
- 다음 읽기 전용 검토는 `LUNA-0037` pending QR card의 PC PDF 전송·delivered 기록과 student mutation 교차 경계다.

### 2026-08-02 15:27:44

- 자동 재개 절차에 따라 계약 문서와 누적 보고서를 처음부터 EOF까지 읽고, Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음, 승인된 ADB `R54TB029FHZ`/`SM-P610` Android 13을 읽기 전용으로 재확인했다.
- `MainActivity` pending-card QR issuance→PDF export→PC ACK→`markCardsDelivered()`와 student/class mutation gate·lifecycle, `StudentRepository`/DAO status 전이, `PcPdfSender`·PC receiver commit-before-ACK/replay, 관련 시험·운영 문서를 정적 대조했다. 단일 `ioExecutor`와 정상 PC commit-before-ACK는 직접 interleave·부분 저장을 제한하는 반대 근거다.
- 그러나 pending flow는 공통 mutation gate/revision·issued QR/request ID binding이 없고, 실행 중 task의 명시적 cancellation/socket close/destroyed check도 없다. 전송 후 mutation 시 PC artifact 폐기·재생성 정책과 pending callback ordering 회귀시험이 없어 기존 `LUNA-0037` P3 후보를 정책·검증 공백으로 유지·보강했다. `LUNA-0010` ACK/retry와 `LUNA-0039` 직접 인쇄 상태 의미와는 중복시키지 않았다.
- 실제 학생·QR·PC PDF·ACK·DB·파일·프린터·네트워크 장애·테스트·빌드·설치·Git 쓰기는 수행하지 않았으며, 지속 변경은 이 보고서만 `apply_patch`로 반영했다.
- 다음 읽기 전용 검토는 `LUNA-0041` PC receiver CSV buffer·queue durability와 전송·취소·재시작 경계다.

### 2026-08-02 15:34:50

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음, 승인된 ADB `R54TB029FHZ`/`SM-P610` Android 13을 읽기 전용으로 재확인했다.
- `pc_receiver` `app.py`의 CSV 선택·취소·shutdown, `server.py`의 `ReceiverState.queue_csv()`·`clear_csv()`·`accept_control()`·handler send, `protocol.py`의 control response encoding, Kiosk `PcControlClient`·CSV parser/repository cleanup, 관련 PC/Kiosk 시험, 운영·보안·CSV 문서와 Git history를 처음부터 정적 대조했다.
- 사실/결론: PC pending CSV는 1 MiB·UTF-8·lock 검증을 거치지만 immutable `bytes`와 `bytes` validation/response plaintext·ciphertext·header/frame 파생 buffer의 reference-only lifecycle을 갖고, 취소·교체·전송 완료·shutdown 시 명시적 zeroization 경계가 없다. Kiosk 측 정상 payload/row cleanup과 정상 authenticated fetch 시험은 반대 근거지만 PC heap cleanup을 검증하지 않는다. queue가 sendall 전에 소비되어 실패·재시작 시 소실되는 문제는 `LUNA-0003`, Kiosk parsed row executor ownership은 `LUNA-0031`로 분리하고 `LUNA-0041` P4 후보를 유지·보강했다.
- 실제 CSV·학생 자격정보·PC receiver 실행·TCP/네트워크 장애·파일·heap/crash dump·테스트·빌드·설치·Git 쓰기는 수행하지 않았다. 이번 지속 변경은 이 누적 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0042` PC receiver startup/tray/background listener lifecycle과 단계별 실패·종료 보상 경계다.

### 2026-08-02 15:39:55

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음으로 확인했다.
- `adb devices`와 지정 serial 상태 조회를 시도했으나 현재 PowerShell PATH에 `adb` executable이 없어 A `device`·모델·Android 상태를 이번 사이클에 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았고, 이전 기록은 historical evidence로만 유지했다.
- `pc_receiver` `pyproject.toml`·`app.py`·`server.py`·`smoke.py`, app/server 시험, build/install 스크립트, README·RELEASE_OPERATIONS·BUILD_VERIFICATION과 관련 Git history를 읽기 전용으로 대조했다. listener bind/server daemon thread가 Tk·tray보다 먼저 시작되고, `main()`은 세 예외 타입만 처리하며 constructor rollback·`mainloop()` 반환 후 finally·tray join/server health supervision이 없다.
- 2026-07-29 자동 시작/TCP LISTEN, receiver 0.1.3 pytest·packaged smoke 기록과 완전 초기화 server fixture는 정상 경로의 반대 근거다. 그러나 단계별 Tk/pystray/bind 실패, 좁은 예외 catch, 숨겨진 background tray 실패, server/tray thread 종료·재시작은 실행하지 않아 기존 `LUNA-0042` P4 후보를 유지·보강했다. `LUNA-0007` inbound resource cap/event queue와 중복시키지 않았다.
- 실제 수신기·TCP·Windows 자동 시작·포트 점유·fault injection·process/thread 상태·파일/CSV/PDF·테스트·빌드·설치·Git 쓰기는 수행하지 않았다. 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0007` PC receiver 인증 전 연결·event queue·thread/socket resource 상한과 UI polling lifecycle이다.

### 2026-08-02 15:44:08

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음으로 확인했다.
- `adb`는 이번에도 PowerShell PATH에서 찾지 못해 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았고, 이 제약을 보고서에 남겼다.
- `pc_receiver` `server.py`의 `_read_exact()`·handler·`ThreadedReceiverServer`, `protocol.py`의 PDF/control length bound, `app.py`의 event producer/200ms polling, 관련 PC 시험·README·운영/검증 문서·Git history를 읽기 전용으로 대조했다.
- 사실/결론: 연결별 daemon worker·socket inactivity timeout·full frame buffer는 인증 전에 작동하고, cumulative read deadline·active worker/connection limit·per-client budget이 없다. `events`는 무제한 queue이고 `_poll_events()`는 한 번에 `queue.Empty`까지 모두 drain한다. payload bound·firewall·authentication·정상 one-shot server fixture와 inherited backlog 가능성은 반대 근거로 남겼지만, slow partial connection·worker 경쟁·queue starvation은 미검증이므로 기존 `LUNA-0007` P3 후보를 유지·보강했다.
- 실제 네트워크·slowloris/flood·소켓·수신기·thread/queue 계측·파일/CSV/PDF·테스트·빌드·설치·Git 쓰기는 수행하지 않았다. 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0008` PC receiver replay ID cache·5분 timestamp window·eviction과 PDF/control 중복 처리 경계다.

### 2026-08-02 15:50:29

- 자동 재개 절차에 따라 최우선 작업 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음으로 재확인했다.
- `adb`는 PowerShell PATH에서 찾지 못해 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- `pc_receiver` `config.py`·`protocol.py`·`server.py`, Kiosk `PcPdfSender`·`PcControlClient`·endpoint resolver, receiver tests·README·운영/검증 문서·관련 Git history를 읽기 전용으로 대조했다.
- 사실/결론: replay ID는 timestamp와 expiry를 저장하지 않는 고정 2,048개 cache이고, 300초 안에 약 6.83개/초의 신규 유효 frame이 쌓이면 오래된 authenticated frame이 eviction 뒤 재수용될 조건이 있다. `ConfigStore.save()`의 atomic replace·정상 restart 보존, `ReceiverState.lock`, 정상 client의 fresh ID·strict response binding은 반대 근거로 기록했다. `accept_control()`의 operation-specific 검증 전 ID 소비·저장 경계와 overflow/restart/concurrent/save-failure 시험 공백을 추가 확인했으며 `LUNA-0008` P4 후보를 유지·보강했다.
- 실제 replay·대량 frame·network/socket·수신기·파일/CSV/PDF·secret·테스트·부하/처리량 계측·빌드·설치·Git 쓰기는 수행하지 않았다. 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0009` PC receiver Windows tray 알림의 학생 표시 이름 노출·정책·회귀시험 경계다.

### 2026-08-02 15:57:31

- 자동 재개 절차에 따라 최우선 작업 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음으로 재확인했다.
- `adb`는 PowerShell PATH에서 찾지 못해 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- `pc_receiver` `server.py`/`app.py`, Kiosk `MainActivity.kt`·`PcPdfSender`·`PcControlClient`, receiver/Kiosk tests, `PRODUCT_DECISIONS.md`·`SECURITY.md`·`RELEASE_OPERATIONS.md`·`BUILD_VERIFICATION.md`, 관련 Git history/blame을 읽기 전용으로 대조했다.
- 사실/결론: 단건 PDF의 `${preview.exactName} QR.pdf`가 receiver의 `destination.name 저장 완료`·`notify=True` event를 거쳐 Windows tray payload로 전달된다. status는 `event.state` 우선으로 이름을 숨기고, CSV/error는 현재 tray notify를 사용하지 않으며, batch PDF filename은 일반 문구다. 제품 결정의 “Windows 알림에는 이름을 표시하지 않는다”와 현재 source가 충돌하므로 `LUNA-0009` P3 확정 finding을 유지·보강했다.
- 실제 PC receiver·Windows toast·잠금 화면·PDF/학생 데이터·QR·네트워크·테스트·빌드·설치·Git 쓰기는 수행하지 않았다. 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0010` PC PDF commit·ACK·후속 delivered 상태 반영 실패와 사용자 재시도·중복 저장 경계다.

### 2026-08-02 16:03:22

- 자동 재개 절차에 따라 최우선 작업 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음으로 재확인했다.
- `adb`는 PowerShell PATH에서 찾지 못해 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- `pc_receiver` `server.py`/`config.py`, Kiosk `MainActivity.kt`·`PcPdfSender`·`PcEndpointResolver`·`StudentRepository`/DAO, 관련 tests, 제품 결정·운영·검증 문서와 `0b0df69`·`474132b`·`ca32740` history를 읽기 전용으로 대조했다.
- 사실/결론: PDF는 receiver commit/replay/config 저장 뒤 ACK를 보내고, Kiosk의 `withReachablePairedPc()`는 첫 PDF send 예외 뒤 인증된 resolved endpoint에서 operation을 다시 호출할 수 있다. 단건 실패 callback은 같은 preview/send action을 다시 열고, batch 후속 `markCardsDelivered()` 실패는 재실행 시 새 QR를 발급한다. 새 request ID·unique destination·studentId-only delivered update가 있어 ACK 유실·후속 DB/config failure에서 중복/stale artifact와 상태 불일치 조건이 남는다. 정상 strict ACK·same-frame replay 거부·single executor·authenticated endpoint probe는 반대 근거이며 `LUNA-0010` P3 후보를 유지·보강했다.
- 제품 결정의 사용자 재시도 금지와 운영 문서의 현재 카드 재전송 안내가 함께 존재해, 불확실한 결과의 자동/수동 재전송 정책을 Sol·사용자 재판단 큐에 남겼다. 오류 주입·실제 PDF/DB/네트워크·수신기·테스트·빌드·설치·Git 쓰기는 수행하지 않았고 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0011` Kiosk audit_events·Kiosk/Web private 진단 로그의 보존·cleanup·민감정보 경계다.

### 2026-08-02 16:13:03

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음으로 확인했다.
- `adb`는 PowerShell PATH에서 찾지 못해 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- Kiosk `AuditEventEntity`·`AuditDao`·`StudentRepository.audit()`·`KioskDatabase`·`MainActivity` startup, Kiosk/Web `PrivateDiagnosticLog`·ADB receiver·manifest/data-extraction rules, 관련 JVM/계측시험, `PRODUCT_DECISIONS.md`·`SECURITY.md`·`KNOWN_LIMITATIONS.md`와 관련 Git history/blame을 읽기 전용으로 대조했다.
- 사실/결론: `audit_events`는 행 수·기간 상한이 없고 `deleteOlderThan()` production 호출이 없으며, Kiosk startup cleanup은 QR PDF cache에만 적용된다. Kiosk/Web 진단 로그는 정상 경로의 현재·이전 256KB 회전과 구조적 dump filter는 갖지만 기간 cleanup이 없다. `delete()`·`renameTo()` Boolean 결과를 무시해 fault 시 nominal size cap도 보장되지 않는다. 현재 call site에서 학생 이름·외부 로그인 ID·비밀번호·QR 원문·답안·점수·문항 내용의 직접 기록은 확인하지 않았다.
- 제품 결정의 “로그 크기·기간 제한”과 구현의 크기-only/best-effort 경계를 대조해 기존 `LUNA-0011` P4 후보를 유지·보강했다. private storage·backup/data-transfer 제외·`DUMP` 권한·nonce/line filter·정상 audit 민감정보 시험은 반대 근거로 남겼다.
- 실제 A DB·audit row/file age·장기 운전·storage pressure·파일 회전 fault·logcat·테스트·빌드·설치·Git 쓰기는 수행하지 않았다. 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0012` 원격 점검 배지와 Kiosk 헤더 상태 표시의 실제 bounds·상태별 겹침 경계다.

### 2026-08-02 16:22:08

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음으로 확인했다.
- `adb`는 PowerShell PATH에서 찾지 못해 새 A 화면·device 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작·원격 점검 Start/Capture/Stop은 이번 사이클에 수행하지 않았다.
- Kiosk/Web `RemoteSupportWindowController`, 두 `activity_main.xml`, 각 `MainActivity`의 controller start/refresh/stop과 상단 state visibility, `README.md`·`SECURITY.md`·`docs/THREAT_MODEL.md`, `f8bd710`·`9153f75`·`a14c51b` history/blame을 읽기 전용으로 대조했다.
- 사실/결론: Kiosk는 content root에 추가되는 우측 상단 배지가 60dp 헤더의 우측 `status_text`와 layout contract를 공유하지 않고 elevation으로 위에 그려진다. 기존 승인 A 관리자 화면의 직접 겹침과 현행 소스가 일치하므로 `LUNA-0012` P3 확정을 유지했다. Web도 동일 오버레이 구현이지만 중앙 네비게이션과 좌·중앙 배지 구조이며 현재 직접 겹침 증거가 없어 Kiosk 범위로 유지했다. `showScanner()`에서 헤더를 숨긴 뒤에도 active badge가 남는 상태 전이는 확인했지만, 새 화면 clipping·hit-test finding으로 확대하지 않았다.
- 정상 원격 점검 의도(우측 상단 상태 표시), private/보안 경계, A Stop 후 임시 캡처 삭제·Device Owner/allowlist 유지 기록은 반대 근거로 보존했다. 직접 bounds·Web 실제 상태·좁은 폭/회전/font scale·TalkBack은 미검증이다.
- 테스트·lint·typecheck·build·install·ADB·네트워크·PIN·QR·학생/답안/제출·Git 쓰기는 수행하지 않았다. 이번 지속 변경은 이 누적 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0013` CSV parser의 credential immutable String 복제와 입력 wipe 이후 heap 잔류 경계다.

### 2026-08-02 16:25:38

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음으로 확인했다.
- `adb`는 PowerShell PATH에서 찾지 못해 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- 이번 사이클은 `LUNA-0013` CSV parser의 credential immutable String 복제와 입력 wipe 이후 heap 잔류 경계를 읽기 전용으로 재대조한다. 관련 소스·시험·문서는 읽기 전용으로 확인했고, 테스트·빌드·설치·Git 쓰기는 수행하지 않았다.
- `StudentCsvImport.kt`·`MainActivity.kt`·`StudentRepository.kt`·`PcControlClient.kt`·`PcControlProtocol.kt`, `StudentCsvParserTest`·`SensitiveTaskTest`, `SECURITY.md`·`docs/GATE4_IMPLEMENTATION.md`·`docs/PRODUCT_DECISIONS.md`·`docs/BUILD_VERIFICATION.md`와 `0193d89` history/blame을 대조했다. parser의 1MB/1,000행 상한, transport/frame/plaintext/row `CharArray` cleanup과 repository `finally`는 정상 경로의 반대 근거로 남겼다.
- 사실/결론(16:28:53): parser는 입력을 `String`으로 복사한 뒤 record/field·trim/split/set String을 만들고, 반환 `CharArray`만 지운다. 이 immutable 중간 표현을 덮는 owner/API가 없어 heap 잔류 가능성은 현재도 코드로 입증된다. `LUNA-0013` P3·높은 신뢰도·확정 상태를 유지한다. 실제 ART heap 생존 시간이나 노출은 확인하지 않았다.
- duplicate/header parse 예외의 partial row owner는 `LUNA-0032`, 반환 후 executor queue/rejection의 mutable row owner는 `LUNA-0031`로 분리했다. 일반 `CharArray` wipe 문서와 정상 repository cleanup은 parser String ownership의 반증이 아니다.
- 실제 CSV·학생 자격정보·DB·heap/GC·A·ADB·테스트·빌드·설치·네트워크·Git 쓰기는 수행하지 않았다. 이번 지속 변경은 이 누적 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0014` Kiosk PC 전송 암호화 예외 경로의 평문·파생 key cleanup 경계다.

### 2026-08-02 16:31:08

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음으로 확인했다.
- `adb`는 PowerShell PATH에서 찾지 못해 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- 이번 사이클은 `LUNA-0014` Kiosk PC 전송 암호화 예외 경로의 평문·파생 key cleanup 경계를 읽기 전용으로 재대조한다. 관련 소스·시험·문서를 확인한 뒤 결과를 누적한다.
- `PcTransferProtocol.kt`·`PcControlProtocol.kt`·`PcPdfSender.kt`·`PcControlClient.kt`, `PcTransferProtocolTest`·`PcControlProtocolTest`, PC receiver protocol/server/app·시험, `SECURITY.md`·`docs/BUILD_VERIFICATION.md`·`docs/RELEASE_OPERATIONS.md`와 `a3aa80c`·`474132b` history/blame을 대조했다.
- 사실/결론(16:33:55): 기존 PDF/control encode와 ACK HMAC 경로는 crypto/provider·allocation 예외에서 plaintext·파생 key `finally`가 없고 PDF filename buffer는 정상 성공에도 별도 wipe되지 않는다. 추가로 control response request-ID mismatch가 `plaintext.fill(0)` 범위 밖에서 발생하며, ACK caller wipe도 `verifyAck()` 성공 뒤에만 실행된다. `LUNA-0014` P3·높은 신뢰도·확정 상태를 유지·보강한다.
- request-ID mismatch는 먼저 GCM 인증을 통과한 stale/misbound response가 필요하며, ACK에는 PDF 원문이 없다. 정상 frame/header/row cleanup·strict response binding·protocol vector·A 종단간 성공 기록은 반대 근거로 보존하고, 인증 우회·PDF 평문 영구 저장으로 확대하지 않았다.
- 실제 provider/DoFinal·allocation fault, stale response·ACK failure runtime, ART heap/GC·실제 PDF·학생 이름·secret·A·ADB·테스트·빌드·설치·네트워크·Git 쓰기는 수행하지 않았다. 이번 지속 변경은 이 누적 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0015` 학생 비활성화 뒤 암호화 자격정보 레코드의 보존·논리 삭제 경계다.

### 2026-08-02 16:36:18

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. Git 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, 기존 사용자 변경·미추적 산출물 보존, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope diff 없음으로 확인했다.
- `adb`는 PowerShell PATH에서 찾지 못해 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- 이번 사이클은 `LUNA-0015` 학생 비활성화 뒤 암호화 자격정보 레코드 보존·논리 삭제 경계를 읽기 전용으로 재대조한다. 관련 source·DAO·migration·문서·시험을 확인한 뒤 결과를 누적한다.

### 2026-08-02 16:39:34

- 이번 주기의 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽은 뒤 작업했다. Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이며, 기존 사용자 변경·미추적 `diagnostics/`, `output/`, `tmp/`와 source scope를 보존했다.
- `adb`는 현재 PowerShell PATH에서 executable을 찾지 못했다. ADB 설치·PATH 변경·기기 연결·PIN·QR·학생 데이터 사용은 하지 않았다.
- `StudentRepository.deactivateStudent()`·`StudentDao`·`StudentEntity`·`KioskDatabase`/Room schema와 migration, `CredentialCipher`, manifest/data-extraction rules, `RepositoryInstrumentedTest`·`KioskDatabaseMigrationInstrumentedTest`·`CredentialCipherInstrumentedTest`, `KNOWN_LIMITATIONS.md`·`SECURITY.md`·`BUILD_VERIFICATION.md` 및 관련 `git log`/`git blame`을 읽기 전용으로 대조했다.
- 사실: 비활성화 transaction은 QR hash 교체·`isActive=false`·timestamp·두 audit event만 저장하며 credential ciphertext/IV/version을 삭제하지 않는다. production student delete/purge/retention path와 deactivation timestamp/schema/job이 없고, `findById()`는 inactive row도 반환할 수 있으나 복호화·credential update는 active guard로 차단된다. migration은 credential fields를 보존하고 backup/data-transfer 제외·AES-GCM at-rest는 반대 근거다.
- 판정: 문서가 이 동작을 명시적으로 “논리적 비활성화이며 암호화된 자격정보 레코드의 보안 삭제는 아님”으로 규정하고 구현도 일치한다. 따라서 `LUNA-0015` P3 `기존 알려진 문제`·높은 신뢰도를 유지하고 신규 finding은 추가하지 않았다. `LUNA-0006` transient QR hash cleanup과는 분리했다.
- 미검증: 실제 DB export/SQLite page 잔류/보존 기간/Keystore 접근·복호화·실제 A 상태·fault injection은 확인하지 않았다. 테스트·lint·typecheck·build·install·network·Git 쓰기도 수행하지 않았으며, 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0016` Web 네트워크 대기 패널의 반투명 차폐와 답안 비노출 경계다.

### 2026-08-02 16:43:14

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 누적 보고서는 4,075줄·401,392자였고 EOF를 확인했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`, `output/`, `tmp/` 및 계약 문서를 보존했다. `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 변경되지 않았다.
- 현재 PowerShell PATH에는 `adb` executable이 없어 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- 이번 반복의 검토 대상은 `LUNA-0016` Web 네트워크 대기 패널의 반투명 차폐와 답안 비노출 경계다. 기준선 기록만 완료했으며, source·호출 경로·시험·문서·이력 대조 후 판정을 누적한다.

### 2026-08-02 16:46:37

- `webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt`의 `registerNetworkMonitor()`·`NetworkCallback`·`onReceivedError()`·`transition()`·`updateNetworkPause()`와 `showActive()`, `activity_main.xml`의 WebView/panel 순서·색상·focusable/clickable 경계를 읽기 전용으로 대조했다. 네트워크 단절 시 ACTIVE WebView는 유지되고 panel visibility만 `VISIBLE`이 된다.
- 제품 결정은 WebView·답안을 유지하면서 입력을 가리도록 하고 복구 때 재로그인하지 않도록 한다. 현재 panel은 `#F2102A43`의 `F2` alpha와 background 없는 중앙 LinearLayout을 사용하므로 완전 opaque 차폐를 정적으로 증명할 수 없다. 네트워크 pause 전용 UI/렌더링 시험은 찾지 못했다.
- `d8ca06f` 도입 commit과 관련 blame/log를 확인했고 이후 해당 lines의 후속 변경은 확인하지 않았다. 과거 W05의 `0.1.0` `LOCKED / NETWORK_ERROR`는 현재 pause overlay 이전 버전이며, 최신 BUILD_VERIFICATION은 실제 통신 단절 현장 조작을 미검증으로 남긴다. 이 기록을 현재 구현의 반증으로 사용하지 않았다.
- 판정: 실제 화면·답안·네트워크를 사용하지 않은 상태에서 정적 근거는 “완전 비노출 보장 불충분” 후보까지다. `LUNA-0016` P3·중간 신뢰도를 유지하고, 입력 이벤트 경계는 `LUNA-0017`과 중복시키지 않았다.
- 미검증: Android compositor의 실제 alpha 합성·기기 밝기·고대비 답안 가독성·네트워크 단절/복구·A 화면·답안 입력·현장 회귀시험. 테스트·lint·typecheck·build·install·ADB·Git 쓰기는 수행하지 않았으며 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0017` 네트워크 단절 시 WebView 포커스·IME·일반 키 입력 차단 경계다.

### 2026-08-02 16:49:27

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 누적 보고서는 4,110줄·405,903자였고 EOF를 확인했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`, `output/`, `tmp/` 및 계약 문서를 보존했다. `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 변경되지 않았다.
- 현재 PowerShell PATH에는 `adb` executable이 없어 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- 이번 반복의 검토 대상은 `LUNA-0017` 네트워크 단절 시 WebView 포커스·IME·일반 키 입력 차단 경계다. 기준선 기록만 완료했으며, source·호출 경로·시험·문서·이력 대조 후 판정을 누적한다.

### 2026-08-02 16:51:17

- `MainActivity.kt`의 `updateNetworkPause()`·`transition()`·`registerNetworkMonitor()`·`onKeyDown/onKeyUp()`·`dispatchTouchEvent()`·lifecycle, `activity_main.xml`의 pause panel focus/click 속성, `RecoveryInstrumentedTest`·`WebDomScriptsTest`, 제품 결정·field checklist·`WEB_POC_TEST_PLAN.md`·관련 history/blame을 읽기 전용으로 대조했다.
- 사실: pause 전환은 panel visibility와 inactivity timer만 바꾸며 `clearFocus()`·IME hide·WebView disable·`requestFocus`·key listener가 없다. Activity는 BACK만 소비하고 다른 key는 `super`로 전달하며 touch dispatch도 `super`를 호출한다. panel의 `focusable/clickable`은 일반 touch 차단 가능성은 주지만 IME·hardware key·접근성 입력 폐쇄를 증명하지 않는다.
- 정상/반대 근거: full-screen panel, BACK 소비, timer 취소, `filterTouchesWhenObscured`, onStop/renderer cleanup은 범위를 줄인다. 그러나 network pause 전용 focus/IME/key 회귀시험은 없고, field checklist의 단절·입력 차단 항목은 미체크다. 구 W05의 `LOCKED` 기대는 현재 pause overlay 이전 시험계획으로 분리했다.
- 판정: 실제 입력·네트워크·답안 없이 코드 경로와 시험 공백은 “pause 중 focused WebView의 IME/key/accessibility 입력이 보장되지 않음” 후보까지 지지한다. `LUNA-0017` P3·중간 신뢰도를 유지하고 `LUNA-0016` 시각 차폐와 중복시키지 않았다.
- 미검증: 제조사별 Android focus/IME routing, 실제 키보드·hardware key·접근성 입력, 네트워크 단절·복구, 답안 상태 변경, A 화면. 테스트·lint·typecheck·build·install·ADB·Git 쓰기는 수행하지 않았으며 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0018` 네트워크 감시 callback 등록 실패 시 연결 대기 fallback 경계다.

### 2026-08-02 16:53:13

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 누적 보고서는 4,145줄·410,465자였고 EOF를 확인했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`, `output/`, `tmp/` 및 계약 문서를 보존했다. `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 변경되지 않았다.
- 현재 PowerShell PATH에는 `adb` executable이 없어 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- 이번 반복의 검토 대상은 `LUNA-0018` 네트워크 감시 callback 등록 실패 시 연결 대기 fallback 경계다. 기준선 기록만 완료했으며, source·호출 경로·시험·문서·이력 대조 후 판정을 누적한다.

### 2026-08-02 16:55:23

- `MainActivity.kt`의 `initializeUi()` 호출 순서·`registerNetworkMonitor()`·`NetworkCallback`·`hasValidatedNetwork()`·`transition()`·WebView `onReceivedError()`와 manifest 권한을 읽기 전용으로 대조했다. 등록 성공 시 callback과 ACTIVE/error trigger가 pause 상태를 갱신한다.
- 사실: `ConnectivityManager` null 또는 `registerDefaultNetworkCallback()` 예외는 `NETWORK_MONITOR_FAILED` private event만 남기며 재등록·periodic check·watchdog은 없다. 초기 등록 직후 update는 `uiInitialized=false`로 건너뛰지만 ACTIVE transition이 현재 network capability를 다시 평가한다. 따라서 문제는 모든 시작 상태의 즉시 실패가 아니라 등록 실패 뒤 유휴 ACTIVE에서 조용한 단절을 놓칠 수 있는 경계다.
- 반대 근거: `INTERNET`·`ACCESS_NETWORK_STATE`, 정상 callback events, ACTIVE transition, main-frame error fallback, 기존 W05/G306 정상 실패폐쇄 기록. 그러나 callback 예외 주입·유휴 ACTIVE 단절·지연시간 시험은 없고 최신 field checklist는 단절 항목을 미체크로 남긴다. `WEB_POC_TEST_PLAN`의 W05 완료 문구는 `d8ca06f` 이전 baseline 문서로 구분했다.
- 판정: 등록 실패 자체를 현재 기능 실패로 확정하지 않고, 전용 fallback 부재가 안전 대기 진입을 지연시킬 수 있는 `LUNA-0018` P4·중간 신뢰도 후보를 유지한다.
- 미검증: system service null/exception·resource limit, callback 등록 후 네트워크 단절, 유휴 ACTIVE 화면, pause 진입 지연, A permission/service 상태와 실제 입력 보호. 테스트·lint·typecheck·build·install·ADB·Git 쓰기는 수행하지 않았으며 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0019` 네트워크 대기 중 접근성 트리에서 WebView 답안 노드가 숨겨지는지 확인하는 경계다.

### 2026-08-02 16:57:39

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 누적 보고서는 4,180줄·415,657자였고 EOF를 확인했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`, `output/`, `tmp/` 및 계약 문서를 보존했다. `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 변경되지 않았다.
- 현재 PowerShell PATH에는 `adb` executable이 없어 A `device` 상태를 확인하지 못했다. ADB 설치·PATH 변경·기기 조작은 하지 않았다.
- 이번 반복의 검토 대상은 `LUNA-0019` 네트워크 대기 중 접근성 트리에서 WebView 답안 노드가 숨겨지는지 확인하는 경계다. 기준선 기록만 완료했으며, source·호출 경로·시험·문서·이력 대조 후 판정을 누적한다.

### 2026-08-02 17:01:16

- `webpoc/src/main/res/layout/activity_main.xml`의 WebView/pause panel과 `MainActivity.kt`의 `configureSensitiveInputs()`·`showActive()`·`transition()`·`updateNetworkPause()`를 읽기 전용으로 대조했다. WebView에는 `importantForAutofill`만 있고 접근성 subtree 차폐 속성은 없으며, panel은 full-screen `clickable`·`focusable`·안내 TextView만 가진다. pause 전환은 visibility/timer만 바꾸고 `importantForAccessibility`·`NO_HIDE_DESCENDANTS`·delegate·focus 반환을 호출하지 않는다.
- Web POC unit/instrumented test 검색에서 TalkBack·Switch Access·Android accessibility tree/action 또는 network pause를 함께 검증하는 시험은 찾지 못했다. 기존 `isAccessible = true`는 reflection 접근 플래그라 접근성 서비스 시험으로 세지 않았다.
- `docs/THREAT_MODEL.md`의 접근성 text 위협과 `docs/BUILD_VERIFICATION.md`의 `enabled/bound`·Gate 1 결과는 별도 `poc` Probe 범위다. Device Owner/Lock Task 점검 스크립트는 owner·allowlist·lock state·버전만 검사하고 접근성 서비스 비활성화 정책은 증명하지 않는다. `activity_main.xml` WebView 원형은 `eb09567`, pause panel은 `d8ca06f`에서 도입됐고 targeted history에서 접근성 subtree 제어 후속 변경은 확인하지 못했다.
- 반대 근거로 panel의 일반 touch/focus 우선 가능성, Probe의 package/allowlist 통제, Device Owner/Lock Task를 기록했지만 이들은 형제 WebView virtual node 차폐의 증거가 아니다. `importantForAutofill`·`filterTouchesWhenObscured`·`FLAG_SECURE`도 접근성 tree/action 통제와 별도다.
- 판정: pause 중 접근성 subtree 상태 계약과 회귀시험이 없는 정적 경계는 확인했으나 실제 Android accessibility tree/action·서비스·답안·네트워크는 사용하지 않았다. `LUNA-0019` P3 후보·중간 신뢰도를 유지하며 `LUNA-0016` 시각 차폐·`LUNA-0017` 입력 lifecycle·`LUNA-0018` callback fallback과 분리했다.
- 미검증: 실제 TalkBack/Switch Access/기기별 WebView virtual node 노출·action 전달, 접근성 설정·네트워크 단절/복구·답안 상태·ADB/A 화면. 테스트·lint·typecheck·build·install·ADB·Git 쓰기는 수행하지 않았으며 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0020` 원격 지원 활성 중 Kiosk PIN·Web 자격정보 화면의 `FLAG_SECURE` 복원 경계다.

### 2026-08-02 17:08:56

- Kiosk/Web `MainActivity`의 초기 `FLAG_SECURE`, `RemoteSupportWindowController.refresh()/stop()`, `RemoteSupportStore/Policy`, Kiosk `showAuthentication()`·`onStart()` relock, Web `showSetup()`·`configureSensitiveInputs()`·`startLogin()`을 읽기 전용으로 대조했다. controller는 active 만료 시각만 보고 window flag를 해제하며 민감 화면 전환을 관찰하지 않는다. Kiosk `stop()`은 Activity destroy에만 연결되고, Web setup 입력에는 secure flag 복원 hook이 없다.
- Kiosk 원격 지원 버튼은 관리자 화면과 확인 dialog 뒤에 있고, signature/DUMP receiver·same-boot·30분 기본/최대 2시간·우측 배지·만료/명시 종료 복원은 정상 경로의 반대 근거다. `SECURITY.md`는 PIN·비밀번호 입력 중 원격 점검을 사용하지 말라고 명시하지만 코드가 이를 강제하지 않는다.
- `RemoteSupportPolicyTest` 두 개는 duration/expiry/boot count만 검사하며, `FLAG_SECURE`와 Kiosk PIN/relock·Web credential/setup·Activity lifecycle을 결합한 시험은 검색되지 않았다. BUILD/현장 기록도 원격 지원 비활성·정상 secure 캡처 또는 종료 후 상태만 확인한다.
- history상 `f8bd710`이 전역 remote window 예외를 도입하고 `9153f75`가 WebView debugging을 active state에 연결했지만, 후속 민감 화면 자동 복원 hook은 확인하지 못했다. 이 정적 경계는 `LUNA-0021` 저장 commit divergence와 분리했다.
- 판정: 승인된 원격 지원·운영 경고가 발생 가능성을 제한하지만, active 상태에서 PIN/Web credential 화면이 캡처 대상이 될 수 있는 코드 경로와 검증 공백은 남아 `LUNA-0020` P3 후보·중간 신뢰도를 유지한다.
- 미검증: 실제 remote support 활성화·PIN/자격정보 입력·ADB 캡처·제조사별 `FLAG_SECURE` semantics·A 화면. 테스트·lint·typecheck·build·install·ADB·Git 쓰기는 수행하지 않았으며 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0021` 원격 지원 Kiosk/Web 상태 저장 commit 실패와 캡처 차단 상태 divergence 경계다.

### 2026-08-02 17:14:18

- Kiosk/Web `RemoteSupportStore`·`RemoteSupportWindowController`, Kiosk 관리자 broadcast/rollback, Web receiver/intent, ADB `remote-tablet.ps1`의 양 앱 순차 제어를 읽기 전용으로 대조했다. 두 앱 private store의 `commit()` 결과는 버려지고, Kiosk UI는 예외 없는 `sendBroadcast()`만, ADB script는 각 명령의 `result=0`만 성공으로 본다. receiver는 내부 저장 결과나 acknowledgement를 반환하지 않는다.
- enable/disable 중 한쪽 commit false, receiver 처리/프로세스 오류, ADB 두 번째 대상 실패가 발생하면 Kiosk/Web `FLAG_SECURE`·WebView debugging·badge가 서로 다른 상태로 남을 수 있다. ADB script의 500ms 후 캡처는 PNG signature만 검사하고 양 앱 state/flag를 교차 확인하지 않으며, partial failure rollback도 없다.
- 정상 경로의 signature/DUMP receiver 권한, same-boot·duration clamp·listener·expiry 복원, manifest/normal secure 시험과 current field 기록은 반대 근거다. 그러나 commit failure·cross-app acknowledgement·partial broadcast fault 시험은 없고 실제 state divergence도 실행하지 않았다.
- history상 `f8bd710` 도입 이후 remote store 결과/ack/compensation 경계를 보강한 후속 변경은 확인하지 못했다. `LUNA-0020` 민감 화면 flag 문제와 분리해 `LUNA-0021` P4 후보·중간 신뢰도를 유지한다.
- 미검증: 실제 SharedPreferences commit false·receiver/broadcast failure·두 번째 대상 실패·A 상태·화면 flag·WebView debugging·캡처. 테스트·lint·typecheck·build·install·ADB·Git 쓰기는 수행하지 않았으며 이번 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0023` QR 렌더링의 `BitMatrix`·픽셀 임시 배열과 Bitmap 정리의 zeroize 경계다.

### 2026-08-02 17:25:19

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 보고서는 4,279개 줄·436,887자이며 EOF를 확인했다. 이번 반복의 검토 대상을 직전 큐인 `LUNA-0023`으로 복원했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`, `output/`, `tmp/` 및 계약 문서를 보존했으며 source scope는 변경되지 않았다.
- 현재 PATH에는 `adb` executable이 없어 A 기기 상태를 확인하지 못했다. ADB·화면·QR·PIN·테스트·빌드·설치·Git 쓰기는 하지 않았고, 다음 단계는 QR renderer의 소스·호출 graph·cleanup·시험·문서·history 정적 대조다.
- 다음 읽기 전용 검토는 `LUNA-0023` QR 렌더링의 `BitMatrix`·픽셀 임시 배열과 Bitmap 정리의 zeroize 경계다.

### 2026-08-02 17:21:25

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽고, 이번 반복의 대상을 `LUNA-0022`로 유지했다. Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이며, 기존 사용자 수정과 미추적 `diagnostics/`, `output/`, `tmp/` 및 계약 문서를 보존했다.
- `KioskLockTaskController`의 `enterRestrictedMode()`·`status()`, `MainActivity`의 configure/enter·QR/auth/onStart 호출부, `DedicatedDevicePolicy`, `SessionPreflightPolicy`와 관련 테스트를 읽기 전용으로 재대조했다. `startLockTask()` 뒤 `currentMode()!=LOCKED`는 `Result.success(false)`가 되지만 `DISALLOW_CREATE_WINDOWS`는 예외가 아닌 false 경로에서 남고, caller/preflight는 false 또는 `NONE/PINNED`를 차단하지 않는다. `DedicatedDevicePolicy`의 표시도 해당 조합을 `보안 준비 중`으로 남긴다.
- `git blame`·`git log -S`에서 이 controller 경계는 Gate 5 도입 `ff8ec786` 이후 후속 수정 없이 유지된다. Gate 5 문서·provision/verify 스크립트·field checklist·BUILD 기록의 Device Owner/allowlist/전용 HOME/정상 `LOCKED` 확인, 비소유자·allowlist 실패 차단, 예외 경로 cleanup, 관리자 해제·재잠금은 반대 근거지만 false-return fault와 restriction 잔류를 검증하지 않는다.
- 판정: 예외 없는 `LOCKED` 미도달 결과가 발생하면 QR·인증·카메라 흐름과 overlay restriction이 어긋날 수 있는 정적 경계는 확인했다. 실제 Android lifecycle/API가 그런 반환을 만드는지와 실제 보안 경계 붕괴는 확인하지 못해 `LUNA-0022` P3 후보·중간 신뢰도를 유지하며, `LUNA-0020` 원격 지원 flag·`LUNA-0021` cross-app state divergence와는 분리했다.
- 미검증: SM-P610의 `startLockTask()` false/예외/PINNED/Activity 재생성, `DISALLOW_CREATE_WINDOWS` 실제 잔류, QR·PIN·카메라 진행, 화면·ADB 상태. `adb` executable은 현재 PATH에 없어 기기 확인을 하지 않았다. 테스트·lint·typecheck·build·install·fault injection·Git 쓰기는 수행하지 않았고, 계약상 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0023` QR 렌더링의 `BitMatrix`·픽셀 임시 배열과 Bitmap 정리의 zeroize 경계다.

### 2026-08-02 17:29:56

- `LUNA-0023`에 대해 `QrImageRenderer` 원본과 QR token 생성, 단건 preview·직접 인쇄·PDF·PC 전송·반 batch PDF/인쇄의 전체 호출 graph를 재대조했다. renderer는 `BitMatrix`와 720×720 `IntArray`를 생성해 Bitmap에 복사하지만 내부 matrix/pixels를 `finally`에서 clear/fill하지 않는다. 반면 반환 Bitmap은 preview·exporter·print adapter·`SensitiveTask`의 owner cleanup이 정상·예외·Activity 종료 경로에 존재한다.
- 관련 시험은 PDF 결과·물리 크기·반환/copy Bitmap recycle과 PDF cache cleanup을 확인하지만 renderer 내부 임시 배열의 zeroize·예외·heap 수명·batch peak memory를 확인하지 않는다. `SECURITY.md`의 bitmap 삭제 계약과 RC13/14의 positive cleanup 기록은 내부 renderer 배열까지 증명하지 않으며, renderer 본문은 `e093bd0` 이후 후속 변경이 없다.
- 판정: Bitmap cleanup과 구별되는 renderer 중간 표현 소유권·zeroize 계약 공백을 확인했으나 ART/GC의 실제 잔류와 heap 노출은 재현하지 못했다. `LUNA-0023` P3 후보·중간 신뢰도를 유지하고, `LUNA-0006` hash ownership·`LUNA-0014` PC buffer·`LUNA-0036` batch ordering과 중복하지 않았다.
- 미검증: 실제 QR 원문·Bitmap·heap snapshot·renderer fault·OOM 인접 반복·실제 인쇄/PDF/PC 수신·A 화면. 테스트·lint·typecheck·build·install·ADB·fault injection·Git 쓰기는 수행하지 않았으며, `adb` executable은 PATH에 없다. 계약상 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0024` 실행취소 lifetime과 후속 관리자 작업 invalidation 경계다.

### 2026-08-02 17:33:04

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 보고서는 4,313개 raw 줄·442,298자이며 EOF를 확인했다. 이번 반복의 검토 대상을 직전 큐인 `LUNA-0024`로 복원했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`, `output/`, `tmp/` 및 계약 문서를 보존했으며 source scope는 변경되지 않았다.
- 현재 PATH에는 `adb` executable이 없어 A 기기 상태를 확인하지 못했다. ADB·화면·QR·PIN·테스트·빌드·설치·Git 쓰기는 하지 않았고, 다음 단계는 `MainActivity` 실행취소 action 생성·무효화·화면 lifecycle·manager mutation 호출 graph와 관련 시험·문서·history의 읽기 전용 대조다.
- 다음 읽기 전용 검토는 `LUNA-0024` 실행취소 lifetime과 후속 관리자 작업 invalidation 경계다.

### 2026-08-02 17:37:45

- `LUNA-0024`에 대해 `MainActivity`의 `offerAdminUndo()`·`clearPendingAdminUndo()`·`performPendingAdminUndo()`와 반 삭제·소속 변경·이름 변경·학생 등록·QR 재발급·CSV·자격정보·비활성화·반 생성 성공 경로의 전체 호출 graph를 다시 대조했다. 실행취소 action은 세 reversible 성공 경로에서만 교체되고, 후속 non-reversible mutation·화면 이탈에는 공통 invalidation이 없다. `PendingAdminUndo`에는 mutation revision·expected post-state가 없으며, 이름 변경은 refresh 완료 전 undo를 표시하고 student mutation gate가 undo 버튼을 제어하지 않는다.
- `StudentRepository.updateStudentProfile()`·`replaceClassMemberships()`가 저장된 이전 이름/ID 집합을 expected 현재 상태 확인 없이 적용하는 점, 단일 `ioExecutor`의 직렬화가 invalidation을 대신하지 않는 점, `git log -S`에서 `0193d89` 이후 undo 후속 수정이 없는 점을 확인했다. `restoreClass()`의 active-session·동일 이름·active student guard, 30초 만료·명시적 클릭·isolated 현장/BUILD 성공 기록·제품 결정의 실행취소 제외 목록은 반대 근거로 남겼다.
- 판정: 후속 성공 작업 뒤에도 이전 실행취소가 `방금 작업`으로 남아 학생 이름·반 소속을 다시 덮을 수 있는 정적 lifetime/invalidation 공백을 `LUNA-0024` P4 후보·중간 신뢰도로 유지한다. active-session membership eligibility는 `LUNA-0025`, batch QR/student mutation gate는 `LUNA-0036`으로 분리했고 새 ID는 추가하지 않았다.
- 미검증: 실제 관리자 연속 조작·CSV/QR/PIN/학생·반·DB 변경·undo 클릭 timing·refresh 지연·화면 재진입/Activity lifecycle·실제 stale 데이터 결과. `adb` executable은 현재 PATH에 없었다. 테스트·lint·typecheck·build·install·fault injection·Git 쓰기는 수행하지 않았으며, 계약상 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0025` 활성 수업 중 반 소속 실행취소와 QR/수동 선택 eligibility 경계다.

### 2026-08-02 17:42:02

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 누적 보고서는 4,349개 raw 줄·448,038자이며 SHA-256 `E175103292EC170AA5ECA9125CFCC0BCF4B4C0108D02248167060E2BD0F10AC5`를 기준선으로 확인했고, 이번 반복의 검토 대상을 직전 큐인 `LUNA-0025`로 복원했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이며, 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`, `output/`, `tmp/` 및 계약 문서를 보존했다. source scope는 시작 기준선에서 변경하지 않았다.
- 현재 PATH에는 `adb` executable이 없어 A 기기 상태를 확인하지 못했다. ADB·화면·QR·PIN·학생·반·수업·DB 변경과 테스트·빌드·설치·Git 쓰기는 수행하지 않았고, 다음 단계는 활성 세션 관리자 재진입·pending `RestoreMemberships` 생성/실행·repository guard·QR/수동 선택 query·관련 시험·문서·history의 읽기 전용 대조다.
- 다음 읽기 전용 검토는 `LUNA-0025` 활성 수업 중 반 소속 실행취소와 QR/수동 선택 eligibility 경계다.

### 2026-08-02 17:45:21

- `LUNA-0025`에 대해 `startOrEndSession()`의 `PendingRecoveryAction.StartSession` 캡처부터 `completeSessionStart()`·`showScanner()`·세션 PIN `showAuthenticatedSessionActions()`·`showAdmin()` 재진입까지 pending undo lifetime을 재대조했다. active session에서 일반 반 구성·class spinner는 잠기지만 `undoAdminButton`은 별도 상태 갱신이 없고, source의 상태 쓰기는 세 reversible `offerAdminUndo()` 호출과 만료/클릭 정리에 한정된다.
- `performPendingAdminUndo()`의 `RestoreMemberships`는 active session·session ID·`webRecoveryGate`를 확인하지 않고 `StudentRepository.replaceClassMemberships()`를 호출한다. repository는 active class·active student만 확인하고 transaction으로 `class_memberships`를 교체하며, `StudentDao`의 QR·수동 선택 eligibility는 현재 반 membership 또는 `session_students`를 live join한다. `validateForActiveSession()`과 manual validation/list 경로도 이 query 결과를 사용한다.
- 정상 반대 근거는 active-session UI 잠금, `deleteClass()`·batch QR·`restoreClass()`의 별도 guard, 단일 executor, temporary student의 OR 조건, 정상 eligibility/isolated undo 시험이다. 그러나 관련 시험·현장 문서는 session 시작 후 pending undo 순서를 다루지 않고, history에서도 해당 guard/invalidation 후속 commit을 확인하지 못했다.
- 판정: 수업 시작 전에 남은 pending membership undo가 세션 관리자 재진입으로 일반 관리자 차단을 우회하고, 같은 반의 QR 승인·수동 선택 eligibility를 바꿀 수 있는 정적 상태 경계를 `LUNA-0025` P3 후보·중간 신뢰도로 유지한다. `LUNA-0024` stale action lifetime, `LUNA-0026` 종료 후 snapshot, `LUNA-0036` batch gate와 중복하지 않았다.
- 미검증: 실제 `[A]→[B]` membership·수업 시작·PIN·undo·QR·수동 선택·학생/반/DB 변경, active-session repository fault, Activity timing/화면 상태. `adb` executable은 현재 PATH에 없었다. 테스트·lint·typecheck·build·install·fault injection·Git 쓰기는 수행하지 않았으며, 계약상 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0026` 수업 종료 transaction 뒤 관리자 snapshot refresh 실패와 UI/DB 상태 동기화 경계다.

### 2026-08-02 17:48:26

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 누적 보고서는 4,393개 raw 줄·455,495자이며 SHA-256 `5CB6BEAAEF72AFF8887D18CE19E2F18FEA767FE3A636CC43ADCD43E0B1974374`를 기준선으로 확인했고, 이번 반복의 검토 대상을 직전 큐인 `LUNA-0026`으로 복원했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이며, 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`, `output/`, `tmp/` 및 계약 문서를 보존했다. source scope는 시작 기준선에서 변경하지 않았다.
- 현재 PATH에는 `adb` executable이 없어 A 기기 상태를 확인하지 못했다. ADB·화면·QR·PIN·학생·반·수업·DB 변경과 테스트·빌드·설치·Git 쓰기는 수행하지 않았고, 다음 단계는 `completeSessionEnd()`·`refreshAdminData()`·`StudentRepository.endSession()` 호출 graph, 성공 후 snapshot read failure, stale `currentSession` UI, 관련 시험·문서·history의 읽기 전용 대조다.
- 다음 읽기 전용 검토는 `LUNA-0026` 수업 종료 transaction 뒤 관리자 snapshot refresh 실패와 UI/DB 상태 동기화 경계다.

### 2026-08-02 17:50:44

- `LUNA-0026`에 대해 `completeSessionEnd()`의 종료 성공·실패 callback, `StudentRepository.endSession()` transaction, `refreshAdminData()`의 snapshot 성공/failure 분기와 `updateSessionAdminControls()`·`showScanner()`의 메모리 상태 의존성을 재대조했다. 종료 transaction은 `ADMIN_IDLE`을 저장하지만 후속 snapshot read가 실패하면 `currentSession`을 null로 바꾸지 않고 이전 active UI를 다시 그린다.
- stale active UI에서는 `startSessionButton`이 `현재 수업 안전 종료`, `resumeSessionButton`/recover control·class lock이 이전 session state를 기준으로 남을 수 있고, stale `QR_READY`는 `showScanner()` 메모리 검사만 통과한다. 실제 DB query와 duplicate end는 `ADMIN_IDLE`/`No active session` guard에서 실패하므로 데이터 partial commit이 아니라 UI/DB 상태 동기화 경계로 한정했다.
- RC18의 일반 refresh failure 보존·재진입 안내, Room transaction 원자성·단일 executor·정상 종료/재시작 현장 기록은 반대 근거다. 그러나 기존 fault 시험은 active session 없는 일반 refresh 또는 학생 gate 해제이고, 종료 commit 직후 read failure와 stale control/다음 start 순서는 확인하지 못했다. `7786ee0`·`2e4a14d`·`e093bd0` 이력 이후 해당 연결의 후속 수정도 찾지 못했다.
- 판정: 종료 성공과 snapshot read failure 사이에 safe-idle projection/전용 재시도 상태가 없어 관리자 UI가 종료된 수업을 잠시 active로 표시할 수 있는 `LUNA-0026` P4 후보·중간 신뢰도를 유지한다. `LUNA-0027` 외부 Web recovery action 유실, `LUNA-0035` pre-session gate, RC18 일반 refresh failure와 중복하지 않았다.
- 미검증: 실제 active session 종료·DB read fault·stale UI·QR/보강/중복 종료/다음 수업 시작 조작, Activity recreation·A 화면·ADB 상태. `adb` executable은 현재 PATH에 없었다. 테스트·lint·typecheck·build·install·fault injection·Git 쓰기는 수행하지 않았으며, 계약상 지속 변경은 이 보고서만 `apply_patch`로 갱신했다.
- 다음 읽기 전용 검토는 `LUNA-0027` Activity 재생성 중 Web recovery 후속 Start/EndSession action 보존 경계다.

### 2026-08-02 17:53:12

- 자동 재개 절차에 따라 최우선 계약 문서와 누적 보고서를 처음부터 EOF까지 읽었다. 누적 보고서는 4,441개 raw 줄·462,259자이며 SHA-256 `771AACBD96D57D0845F5C1B1FDB096A46C3A3F843E6F2E619A07425458E0FBD6`를 기준선으로 확인했고, 이번 반복의 검토 대상을 직전 큐인 `LUNA-0027`로 복원했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이며, 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`, `docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`, `output/`, `tmp/` 및 계약 문서를 보존했다. source scope는 시작 기준선에서 변경하지 않았다.
- 현재 PATH에는 `adb` executable이 없어 A 기기 상태를 확인하지 못했다. ADB·화면·QR·PIN·학생·반·수업·DB 변경과 테스트·빌드·설치·Git 쓰기는 수행하지 않았고, 다음 단계는 `pendingRecoveryAction` 생성·소비·Activity lifecycle·Web recovery result callback·`completeSessionStart/End()` 후속 DB 작업과 관련 시험·문서·history의 읽기 전용 대조다.
- 다음 읽기 전용 검토는 `LUNA-0027` Activity 재생성 중 Web recovery 후속 Start/EndSession action 보존 경계다.

### 2026-08-02 17:57:02

- `LUNA-0027`을 Kiosk `MainActivity`의 `pendingRecoveryAction` 생성·선행 clear·Activity lifecycle, explicit Web recovery Intent/result payload, `completeSessionStart()`/`completeSessionEnd()`와 `StudentRepository` transaction/expected-state 경계까지 재대조했다. action은 `StartSession(classId, temporaryStudentIds)` 또는 인자 없는 `EndSession`으로 Activity 메모리에만 있고, Intent/result에는 operation ID·session/class snapshot·revision·idempotency token이 없다.
- Web POC는 자체 persisted state·trusted caller·configuration-change 예외를 사용하지만, Kiosk caller의 pending action을 보존하지 않는다. 새 Kiosk callback이 `None`을 소비하면 `RESULT_OK` 뒤 Web 정리만 완료되고 Kiosk Start/End 후속 DB 전이가 생략될 수 있다는 기존 P4 후보를 유지했다. 단일 executor·repository transaction/expected-state는 partial/중복 DB write의 반대 근거지만 유실된 action을 복구하지는 않는다.
- RC02(`05e2603`)·RC03(`2e4a14d`)·후속 StartSession 변경(`630fce1`)의 history와 `git blame`을 읽었고, 대상 Kiosk 파일의 `onSaveInstanceState` 추가 이력은 확인하지 못했다. 관련 Kiosk Activity recreation·process reclaim·ActivityResult 재전달·결과 중복·Start/End 후속 DB 전이 회귀시험과 실제 Android 13 재현은 확인하지 않았다.
- 판정: `LUNA-0027` P4·후보·중간 신뢰도를 유지하며 새 finding ID는 추가하지 않았다. 이번 반복에서는 계약에 따라 이 보고서만 `apply_patch`로 갱신했고, 소스·테스트·문서·설정·산출물·진단 자료·Git 상태는 변경하지 않았다.
- 검증/제한: 계약 문서·누적 보고서 EOF 재독, source scope·Git 기준선·보고서 무결성·EOF 재확인은 후속 검증에서 수행한다. `adb` 부재로 A 기기·화면·Web/session/학생/반/DB 상태, 실제 lifecycle fault injection, 테스트·lint·typecheck·build·install은 수행하지 않는다.
- 다음 읽기 전용 검토는 `LUNA-0036` student mutation·반 QR batch/인쇄 cross-operation 회귀시험·운영 문서 연결이다.

### 2026-08-02 18:04:42

- `LUNA-0036`을 `MainActivity`의 student/class/batch gate, `StudentRepository.reissueClassQrBatch()` transaction, identity 없는 `BatchQrCard`, `BatchQrPrintDocumentAdapter`, `PrintManager` callback·`onDestroy()` 경계까지 재대조했다. `BatchQrCard`에는 student/class/issued QR revision이 없고, issued hash는 UI callback 전에 zeroize되며 audit는 count만 기록한다.
- batch DB 작업의 single-thread 직렬화는 partial transaction을 제한하지만 Android `PrintManager`의 비동기 spooler 소비와 mutation transaction을 묶지 않는다. batch callback은 `studentMutationGate`를 반영하지 않는 `updateClassRosterUi()`로 batch 버튼을 다시 열 수 있고, print job 취소·mutation revision 재검증·pending card invalidation 경계도 확인되지 않았다. 기존 `LUNA-0036` P3·후보·중간 신뢰도를 유지했다.
- `630fce1` 도입 history 이후 cross-operation identity/cancellation 보강 commit과 batch adapter/PrintManager 지연 교차 시험은 찾지 못했다. Android 직접 인쇄 보류·PC 기본 운영은 발생 가능성을 낮추는 반대 근거이며, `needsPrint` 의미는 `LUNA-0039`, PC delivery는 `LUNA-0037`로 분리했다. 새 finding ID는 추가하지 않았다.
- 실제 학생·반·QR·인쇄·PrintManager timing·DB·A 화면, 테스트·lint·typecheck·build·install은 수행하지 않았다. 이번 반복의 지속 변경은 이 보고서만 `apply_patch`로 반영했으며, 다음 EOF 검증에서 계약·보고서 전체 재독, source scope·Git 기준선·`git diff --check`를 확인한다.
- 다음 읽기 전용 검토는 `LUNA-0040` 서버 임시답안 cleanup의 공식 상태·같은 과제 재진입·지연 복원 회귀검증이다.

### 2026-08-02 18:10:33

- 자동 재개에 따라 계약 문서 전체를 처음부터 EOF까지 읽었다. 계약은 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`이며, 유일한 지속 산출물·Goal 미완료 규칙을 확인했다.
- 누적 보고서도 처음부터 EOF까지 읽고 4,497줄·472,848자·SHA-256 `E4AD4AEC8B74DEF6F1A85CE89113474515B05524ECCDAF3E59EA4D17B151F756`로 복원했다. 직전 완료 영역은 `LUNA-0036`, 이번 검토 대상은 `LUNA-0040`이다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`, 계약 문서를 보존했고 `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 변경이 없다.
- `adb` executable이 없어 A 기기·Web/session·학생·답안·서버/DB 상태는 확인하지 않는다. 테스트·lint·typecheck·build·install·Git 쓰기와 라이브 답안/임시답안 변경도 수행하지 않으며, 다음 단계는 서버 임시답안 cleanup의 상태 계약·동일 과제 재진입·지연 복원 경계를 소스·시험·문서·history로 읽기 전용 대조하는 것이다.

### 2026-08-02 18:15:02

- `LUNA-0040`을 Web `beginLogout()`→DOM logout→local WebView cleanup→login fingerprint→`finishSecureKioskSession()`/`finishAdminRecovery()`→Kiosk `RESULT_OK` 경로로 재대조했다. 공식 서버의 `unanswered` 저장, answer/task identity, reset acknowledgement, 동일 과제 재진입과 60초 지연 복원 검사는 현재 source에 없다.
- 추가로 persisted `IDLE` fast path가 `AdminRecoveryPolicy.requiresSanitization(IDLE)=false`에 따라 cleanup·login·서버 답안 검증 없이 `RESULT_OK`를 반환하고, `transition(IDLE)`도 앱 내부 state만 저장하는 사실을 확인했다. `LOGOUT_VERIFY` 재생성은 local recovery를 반복할 뿐 server answer proof를 추가하지 않는다.
- `AdminRecoveryPolicyTest`, Web recovery/local cleanup 시험, Kiosk `WebSessionResultPersistenceTest`는 각각 policy·WebView cleanup·Room transition만 검증한다. `ACTION_RECOVER_WEB_SESSION` `IDLE` fast path, official answer reset, same-task re-entry, 60초 지연, Activity recreation 뒤 Kiosk `RESULT_OK` 금지의 연결 시험은 확인하지 못했다. 기존 P3·후보·중간 신뢰도를 유지하며 새 ID는 추가하지 않았다.
- RC117 운영 정정 기록의 실제 지연 복원 사건과 “공식 상태 저장·완전 종료·재진입·60초 관찰” 완료 기준을 반대/독립 근거로 기록했다. 실제 시험계정·답안·서버·네트워크·A 화면·DB, 테스트·lint·typecheck·build·install은 수행하지 않았고, 지속 변경은 이 보고서만 `apply_patch`로 반영했다.
- 다음 읽기 전용 검토는 `LUNA-0039` Android 직접 PrintManager `needsPrint` 상태 의미·pending UI 회귀시험이다.

### 2026-08-02 18:17:51

- 자동 재개에 따라 계약 문서 전체를 처음부터 EOF까지 읽었다. 계약은 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`이며, 유일한 지속 산출물과 Goal 미완료 규칙을 확인했다.
- 누적 보고서도 처음부터 EOF까지 읽고 4,528줄·478,874자·SHA-256 `0B49C150992B3DA9CA5FE2FEB288D67E1BBBCF410ACFCE7CAF8951A9D90D9A0D`로 복원했다. 직전 완료 영역은 `LUNA-0040`, 이번 검토 대상은 `LUNA-0039`이다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`, 계약 문서를 보존했고 `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 변경이 없다.
- `adb` executable이 없어 A 기기·PrintManager·프린터·QR·학생·DB 상태는 확인하지 않는다. 테스트·lint·typecheck·build·install·Git 쓰기와 QR/프린터 변경은 수행하지 않으며, 다음 단계는 Android local print 성공·취소·실패와 `QrCardStatus.needsPrint`/pending UI 연결을 소스·시험·문서·history로 읽기 전용 대조하는 것이다.

### 2026-08-02 18:21:44

- `MainActivity`의 단건·반 전체 두 `PrintManager.print()` 호출은 반환값을 변수나 필드에 저장하지 않으며, source·시험 전체 검색에서도 `PrintJob` 보관/상태 조회·취소 경계는 찾지 못했다 (`MainActivity.kt:2097-2113,2180-2200`). 성공 분기는 각각 화면/roster 갱신과 “인쇄 서비스로 전달” 메시지만 수행하고, 실패 분기는 bitmap 정리·메시지 표시만 수행한다.
- `QrCardStatusEntity`는 `issuedAtEpochMs`, `lastUsedAtEpochMs`, `lastDeliveredAtEpochMs`, `needsPrint`만 갖고 요청·서비스 큐·물리 출력 완료·취소/실패 상태나 operation ID를 갖지 않는다 (`Entities.kt:38-44`). pending dialog는 `needsPrint`만으로 학생을 “카드 출력 필요” 목록에 넣고, 카드 상태 화면은 false를 “카드 전달됨”으로 표시한다 (`MainActivity.kt:1872-1912,1992-2017`). 따라서 local handoff의 성공·취소·실패를 UI 상태로 구분할 표현 경계가 없다.
- 단건 `QrPrintDocumentAdapter.onFinish()`와 반 전체 adapter의 `onFinish()`는 bitmap erase/recycle 및 print attributes 초기화만 수행하며 repository/DAO를 호출하지 않는다 (`QrPrintDocumentAdapter.kt:89-95`, `BatchQrPrintDocumentAdapter.kt:97-105`). `markCardsDelivered()`는 여전히 PC PDF 전송 후 ACK 경로에만 있고, local direct print callback에서 호출되지 않는다 (`MainActivity.kt:1935-1989,2340-2370`; `StudentRepository.kt:476-488`).
- history에서 단건 direct print 도입은 `9eaf2c5`(2026-07-23), 반 전체 PrintManager/audit 도입은 `630fce1`(2026-07-30)로 확인했고, 이후 local print에 `markCardsDelivered()`를 추가한 commit은 찾지 못했다. 이는 현재 history 검색 결과이지 제품 정책의 의도를 단정하는 근거는 아니다.
- `MIGRATION_1_2`는 기존 학생의 실제 전달·인쇄 결과를 읽지 않고 `lastDeliveredAtEpochMs=updatedAtEpochMs`, `needsPrint=0`을 합성하며, migration 계측시험은 그 합성값만 기대한다 (`KioskDatabase.kt:50-83`; `KioskDatabaseMigrationInstrumentedTest.kt:43-58`). adapter 계측시험은 PDF/bitmap lifecycle만, repository 시험은 명시적 `markCardsDelivered()` 뒤 상태만 검증해 local PrintManager outcome과 pending convergence를 연결하지 않는다.
- 사실상 direct print는 현재 운영에서 보류되고 지정 PC PDF 전송이 기본이며, checklist도 직접 인쇄를 `미검증/운영상 미사용`으로 둘 수 있다. 이 반대 근거와 외부 프린터 outcome 미확인을 반영해 `LUNA-0039`는 P4·후보·중간 신뢰도를 유지한다. 물리 인쇄 실패나 QR/서버 데이터 손상을 확정하지 않았고 새 finding ID도 추가하지 않았다.
- 실제 `PrintManager` 성공·취소·실패, physical print, pending UI 재진입/재발급, A/프린터/DB, 테스트·lint·typecheck·build·install은 수행하지 않았다. 이번 반복의 지속 변경은 이 보고서뿐이며, 다음 읽기 전용 검토는 `LUNA-0038` SPA 과제 전환·problem map reset/문항 상태 격리 회귀시험이다.

### 2026-08-02 18:26:18

- 최신 `WebDomScripts`의 `problemMapTransition`은 공식 이전/다음 문제 클릭과 map 직접 이동 중 `target`, `stableChecks`, 4초 deadline, 700ms quiet period를 관리하고 입력을 잠근 뒤 목표 문제번호가 안정되면 해제한다 (`WebDomScripts.kt:2218-2306,2360-2430`). 이는 같은 과제 안의 문제 이동·지연 DOM 보호 보강이지만 `problemStates`를 지우거나 pathname/task identifier를 바꾸는 로직은 아니다.
- `applyStudentExperience()`는 여전히 `window.__matholicKioskProblemStates || (window.__matholicKioskProblemStates = {})`를 만들고 현재/전체답안 form의 문항 번호를 key로 상태를 기록한다 (`WebDomScripts.kt:2018-2020,2075-2092`). 현황 숫자·문항 버튼·`navigateToUnanswered()`는 같은 map을 읽으며, `problemMapTransition`도 이 owner를 교체하지 않는다 (`WebDomScripts.kt:2492-2513,2595-2622`). targeted source search에서 map delete/reinitialize, task key, `pushState`/`popstate`/`hashchange` binding은 찾지 못했다.
- `navigateStudentSection()`은 `/workbook` 또는 `/diagnostic`의 trusted exact anchor를 찾아 `click()`할 뿐 새 map generation을 전달하지 않는다 (`WebDomScripts.kt:4805-4857`). `MainActivity.activeExperienceGeneration`은 늦은 Kotlin/WebView callback을 버리는 guard이고 `prepareStudentContentReveal()`은 target path 차폐·안정화만 수행하므로 Web Document 전역 map의 수명과 연결되지 않는다 (`MainActivity.kt:842-855,1246-1355,1795-1802`).
- 최신 계측시험 `testStudentExperienceShowsWorkingInFlowProblemStateMap()`은 한 fixture에서 q2의 답변/모름/미입력과 3문항 map, 같은 문서의 navigation DOM 교체를 확인한다 (`DomContractInstrumentedTest.kt:1132-1380`). `testStudentSectionNavigation*()`은 `/diagnostic` anchor의 `body.dataset` marker를 확인할 뿐 서로 다른 과제 A→B의 같은 번호 상태, 실제 route/task identity, map reset과 `다음 미입력` 수렴을 확인하지 않는다 (`DomContractInstrumentedTest.kt:3992-4070`). JVM 시험도 script 문자열 계약만 검사한다.
- history의 `93a33ac`(2026-08-01)는 같은 문제 이동 중 입력 차단·안정화 guard를 추가했고 `e231a6d`(2026-08-02)는 답안 전환/키패드 안정화를 보강했지만, 이 후속 변경에서도 cross-task `problemStates` reset/task key는 확인되지 않았다. 이는 현재 history/source 검색의 범위이며 실제 공개 SPA의 full reload 여부를 확정하는 증거는 아니다.
- 제품 목표는 학습지↔진단평가 양방향 전환을 요구하고(`docs/CONTINUOUS_DEVELOPMENT_GOAL.md:207-212`), 현장 시나리오는 양방향 전환과 답안 현황·`다음 미입력`을 별도 확인하도록 한다(`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md:171-172,205-208`). 최신 checklist의 답안 현황·상태 일치·미입력 이동 항목은 아직 미체크다 (`docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md:241-248`).
- 같은 과제 내 transition guard와 DOM 재렌더링 보강은 반대/완화 근거지만, 같은 Web Document를 유지하는 SPA에서 과제 A의 상태가 B의 같은 문항 번호로 남을 조건은 해소되지 않았다. `LUNA-0038`은 P3·후보·중간 신뢰도를 유지하고, 실제 과제 전환·답안·서버 상태·A를 사용하지 않았으므로 새 finding ID는 추가하지 않았다.
- 실제 공개 Web의 full reload/SPA 유지 여부, 과제 A→B→A, 학습지↔진단평가의 상태 map·현황·`다음 미입력`, 서버 답안 영향, 테스트·lint·typecheck·build·install은 수행하지 않았다. 이번 반복의 지속 변경은 이 보고서뿐이며, 다음 검토는 pre-session Web recovery/admin mutation gate와 `StartSession` action revision의 시험 연결 대조다.

### 2026-08-02 18:31:39

- `webRecoveryGate`의 실제 참조는 중복 recovery 차단과 Start/Resume·class spinner·self-test 일부 enabled 조건, recovery 결과/launch failure의 `finish()`에 한정된다 (`MainActivity.kt:182,310-333,1278-1280,2459-2487,2834-2855,2882-2883`). `updateClassRosterUi()`의 반 구성·삭제·보강·batch QR·start 조건과 `updateStudentManagementControls()`의 학생/CSV/QR/카드 상태 조건에는 `webRecoveryGate`가 없다 (`MainActivity.kt:1254-1290,1718-1748`).
- `studentMutationGate`는 학생 등록·단건 QR·이름·자격정보·비활성화 handler에서만 시작되고, 별도 `webRecoveryGate`와 결합되지 않는다 (`MainActivity.kt:1337-1343,1408-1410,1472-1474,1638-1640,1694-1697,1718-1734`). 따라서 pending student mutation 중 Start action을 막거나 recovery 진입 시 mutation을 취소하는 공통 gate는 없다. 반 구성·삭제·보강·quick class도 같은 student gate를 사용하지 않는다 (`MainActivity.kt:1042-1065,1093-1221,2388-2456`).
- `PendingRecoveryAction.StartSession`은 `classId`와 `temporaryStudentIds`만 보유하고 `ClassRosterSelectionState.selectionRevision`, membership snapshot, mutation generation, action token을 보유하지 않는다 (`MainActivity.kt:2517-2522,3961-3967`). `classRosterState`의 generation/revision은 `refreshAdminData()`와 roster 읽기 결과의 stale callback 방지에만 사용되고, Start action 생성·소비에는 연결되지 않는다 (`AdminUiAsyncState.kt:3-95`; `MainActivity.kt:919-920`).
- `runSessionPreflight()`는 action을 받은 뒤 사전점검 대화상자를 보여주고, 확인 시 같은 action을 `launchWebSessionRecovery()`에 전달한다. Web `RESULT_OK` 뒤 `completeSessionStart()`는 action을 그대로 `StudentRepository.startSession()`에 넘기며 현재 class/membership/temporary selection revision을 재검증하지 않는다 (`MainActivity.kt:2525-2581,2824-2847`).
- repository transaction은 현재 active class, 현재 active student, 비어 있지 않은 current class/temporary set, 기존 session 유무를 검사하지만 캡처된 관리자 선택이 최신 의도인지 비교하지 않는다 (`StudentRepository.kt:648-698`). 그러므로 stale class가 삭제되거나 temporary student가 비활성화된 경우는 실패로 제한되지만, class가 여전히 active이고 선택이 바뀐 경우에는 이전 action이 성공할 수 있는 상태 경계가 남는다.
- quick class 버튼은 pre-session에서 `currentSession == null`이면 계속 enabled이고, recovery 중 class spinner만 disabled된다 (`MainActivity.kt:1042-1065,2882`). `launchWebSessionRecovery()` 직전 캡처된 classId와 화면의 마지막 quick-class 선택이 달라질 수 있는 정적 경로다.
- 활성 `QR_READY` EndSession recovery에서도 `addTemporaryButton`이 표시될 수 있고 `addTemporaryStudents()`에는 `webRecoveryGate` 검사 또는 pending EndSession 취소가 없다 (`MainActivity.kt:2388-2456,2870-2888`). 이는 Start action만의 문제가 아니라 같은 공통 gate coverage의 Start/End 양방향 공백이다.
- 관련 시험은 `SessionPreflightPolicyTest`의 기기/카메라/배터리 정책, `AdminUiAsyncStateTest`의 roster generation과 `SingleFlightGate` 중복, `WebSessionResultPersistenceTest`의 transition/session reload, `MainActivityInstrumentedTest`의 일반 refresh·실패폐쇄를 각각 검증한다. `webRecoveryGate` 활성 상태의 전체 button matrix, mutation 중 preflight, Web success/cancel 뒤 action revision 재검증, EndSession 중 보강 추가 연결 시험은 확인하지 못했다.
- 운영 문서는 Web 안전정리부터 Start/End DB 반영까지 단일 실행 gate라고 기록하지만, 같은 문서가 빠른 반 전환·연속 탭·실제 수업 시작/종료 실기는 미수행으로 남긴다 (`docs/BUILD_VERIFICATION.md:565-585,703-710`). 현재 source/test는 그 문서의 “단일 gate”를 모든 관리자 mutation과 action revision까지 증명하지 않는다.
- `LUNA-0035`는 기존 P3·후보·중간 신뢰도를 유지한다. repository transaction과 외부 Web Activity 전면 표시·단일 executor는 반대/완화 근거이며, 실제 교차 timing·학생/반/CSV/QR·Web recovery·A·DB·테스트·빌드·설치는 수행하지 않았다. 새 독립 finding ID는 추가하지 않았다.
- 이번 반복의 지속 변경은 이 보고서뿐이며, 다음 읽기 전용 검토는 Kiosk Room migration partial-failure·first-auth bootstrap·backup/transfer exclusion 회귀 연결 대조다.

### 2026-08-02 18:37:37

- 자동 재개 계약에 따라 최우선 계약 문서와 누적 보고서를 이번 작업의 다른 읽기보다 먼저 처음부터 EOF까지 읽었다. 계약 문서는 358줄·9,264자·SHA-256 56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7이며, 보고서 기준선은 4,570줄·487,598자·SHA-256 E352C6A79762B0FB76D4F192D64AE94AC9B35A5EAF0720EBE616B2EEF5D07F93였다. 이 Goal은 계속 활성 상태로 두고, 지속 산출물은 이 누적 보고서 하나로 제한했다.
- 이번 대상은 Kiosk Room migration의 partial-failure·재시도, v2→v3 최초 인증 bootstrap, Android cloud backup/device transfer exclusion의 회귀 연결이다. Git 기준선은 브랜치 codex/fix-submit-recovery-timeout, HEAD/upstream 8c9a97ca55d34e3d93fdb02d9879ad929ed7133d 동일이다. 기존 사용자 수정 docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md·docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md와 미추적 diagnostics/·output/·tmp/·계약 문서를 보존했으며, kiosk·webpoc·pc_receiver·scripts source scope는 변경하지 않았다. adb executable은 현재 PATH에 없어 A 상태는 읽지 못했다.
- 사실: KioskDatabase는 schema version 3에서 명시적으로 MIGRATION_1_2·MIGRATION_2_3만 등록하고 destructive fallback을 등록하지 않는다. 1→2는 qr_card_status 테이블 생성 뒤 기존 students 전체에서 issuedAtEpochMs·lastDeliveredAtEpochMs를 updatedAtEpochMs로 합성하고 needsPrint=0을 삽입한다. 2→3은 admin_credential에 NOT NULL DEFAULT 0인 pinLength를 추가한다. exported schema 1·2·3의 실제 create SQL도 각각 이 구조와 일치하며, v1에는 qr_card_status가 없고 v2에는 새 테이블이 있으며 v3에는 pinLength만 추가되어 있다 (kiosk/src/main/java/com/local/matholickiosk/kiosk/data/KioskDatabase.kt:45-94, kiosk/schemas/com.local.matholickiosk.kiosk.data.KioskDatabase/{1,2,3}.json).
- 사실: 두 migration callback에는 예외를 보상하거나 중간 상태를 복구하는 코드가 없고, 1→2의 CREATE TABLE IF NOT EXISTS와 후속 INSERT를 별도 재시도 계약으로 묶지 않는다. 현재 계측시험은 MigrationTestHelper로 정상 1→2 한 학생의 합성 카드 값과 정상 2→3 한 관리자 row의 보존·pinLength=0·schema validation만 확인한다 (kiosk/src/androidTest/java/com/local/matholickiosk/kiosk/KioskDatabaseMigrationInstrumentedTest.kt:14-91). migration 중간 SQL 실패, 프로세스 중단, 저장공간/DB I/O 오류, 재오픈·재시도 뒤 row·foreign key·room_master_table 수렴은 시험 소스에서 확인되지 않았다.
- 사실: 최초 인증의 정상 신규 설치 경로는 AdminAuthRepository.isEnrolled()가 false이면 MainActivity가 PIN 설정 화면을 열고 enroll()이 6~12자리 PIN과 실제 pinLength를 저장한다. 기존 v2 credential이 v3 migration에서 pinLength=0을 갖는 경우 enrolledPinLength()가 null을 반환하지만 isEnrolled()는 true이므로 PIN 재등록 화면이 아니라 일반 인증 화면을 연다. 자동 제출 조건은 잠시 비활성화되지만 확인 버튼·키보드 제출은 유지되고, 성공 authenticate()가 입력 길이를 pinLength에 기록하므로 첫 수동 인증 뒤 자동 인증 경로가 복원된다 (AdminAuthRepository.kt:17-75, MainActivity.kt:589-606, 661-749).
- 사실: 관련 시험은 새 credential을 enroll한 6자리·7자리 PIN의 자동 제출, 빈 DB의 최초 PIN 설정 화면, 초기 상태 read failure 재시도를 검증하지만, v2 admin_credential에서 실제 유효한 PBKDF2 verifier를 migration한 뒤 Activity를 재생성하고 수동 첫 인증으로 pinLength가 학습되는 종단 경로는 연결하지 않는다. 현재 migration 시험의 salt/derivedKey/iterations fixture는 schema 보존용 값이며 AdminPin.authenticate 경로를 증명하는 유효 verifier fixture가 아니다 (kiosk/src/androidTest/java/com/local/matholickiosk/kiosk/MainActivityInstrumentedTest.kt:26-89,249-325; AdminPin.kt:20-49).
- 사실: 백업·전송 선언은 정적으로 강하다. AndroidManifest.xml은 allowBackup=false, dataExtractionRules=@xml/data_extraction_rules, fullBackupContent=false를 함께 선언하고, data_extraction_rules.xml은 cloud-backup과 device-transfer 모두 root·file·database·sharedpref·external 전체를 exclude한다 (kiosk/src/main/AndroidManifest.xml:25-29, kiosk/src/main/res/xml/data_extraction_rules.xml:1-17). 소스·계측시험에서 manifest 병합 결과, 실제 APK backup metadata, backup/restore 또는 device-to-device transfer 후 DB·SharedPreferences·Keystore 상태를 확인하는 회귀시험은 찾지 못했다. BUILD_VERIFICATION.md와 GATE4_IMPLEMENTATION.md의 선언·문서 확인은 있지만 실행 연결은 없다.
- 추론: 정상 Room upgrade 경로에서는 학생·관리자 credential row를 삭제하거나 destructive reset하는 정적 근거가 없고, v3의 pinLength=0은 기존 PIN을 잃게 하는 값이 아니라 길이를 모르는 호환 sentinel로 UI 수동 인증을 거쳐 실제 길이를 채우는 경로로 해석된다. 다만 Room runtime의 migration transaction이 partial SQL failure에서 실제로 rollback·재시도를 보장하는지, 디스크/프로세스 중단 뒤 재오픈이 동일한지 이번 Goal의 소스·시험 범위만으로 증명하지 않는다. 이는 현재 확인된 데이터 손실 결함이 아니라 migration fault 회귀시험 공백이다.
- 추론: 백업 제외 설정 자체의 문서-구현 불일치는 확인되지 않았다. 반면 APK manifest/resource merge나 Android OS의 실제 cloud/device transfer 결과를 시험하지 않으므로, 이후 manifest overlay·target SDK·resource 변경으로 exclusion이 약화되는 회귀를 현재 source test가 잡는다고 볼 수 없다. 이 공백만으로 backup 유출을 확정하지 않는다.
- 판정: 새 독립 finding ID는 추가하지 않는다. 기존 LUNA-0039의 MIGRATION_1_2 물리 전달 상태 합성·needsPrint 의미, LUNA-0028의 관리자 PIN verifier 배열 ownership 후보와는 각각 범위가 다르며 이번 대조 후에도 상태를 유지한다. 최초 인증 bootstrap은 수동 fallback과 이전 현장 기록이 있어 기능 고장으로 확정하지 않고, migration fixture→실제 verifier→Activity 인증 연결 회귀 항목으로 남긴다. backup/device-transfer는 정적 선언상 전 영역 제외를 확인했으나 실행 회귀 증거 부족으로 “정적 확인·실제 미검증”으로 보고한다.
- 필요한 후속 검증은 구현하지 않고 큐에 남긴다: (1) 1→2·2→3 각각 다중 row와 dependent table을 포함한 실제 upgrade/reopen, SQL 중간 실패·중단·재시도 및 transaction rollback 확인, (2) 유효한 v2 verifier fixture를 v3로 올린 뒤 최초 화면에서 수동 인증 성공·pinLength 저장·Activity 재생성 후 자동 제출 확인, (3) merged APK manifest와 cloud/device-transfer metadata 및 합성 DB/SharedPreferences가 복원되지 않는 읽기 전용 packaging/OS 회귀 확인.
- 미검증: 실제 DB·학생·PIN·QR·프린터·backup/restore·device transfer·Keystore·A 화면·ADB·migration fault injection·저장공간 압박은 사용하지 않았다. 테스트·lint·typecheck·build·install도 실행하지 않았고, Git stage/commit/push/PR/deploy/rollback도 수행하지 않았다. 이번 반복의 지속 변경은 이 보고서의 apply_patch뿐이다.
- 다음 읽기 전용 검토는 LUNA-0033 PC status queue lifecycle·offline recovery·shared worker starvation·Activity 종료 cleanup의 source/test/document/history 연결 대조다.

### 2026-08-02 18:43:15

- 자동 재개 계약에 따라 계약 문서를 처음부터 EOF까지 읽고, 그 뒤 누적 보고서도 처음부터 EOF까지 읽었다. 계약 문서는 357줄·9,264자·SHA-256 56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7이며, 이번 대조 전 보고서는 4,585줄·492,692자·SHA-256 C2A018A5E2599461956E89A33213E19191FDC5CF63D81535DACA55B7683E4405였다. Goal은 계속 활성으로 두고 지속 변경 대상은 보고서 하나로 제한했다.
- 이번 반복은 LUNA-0033 Kiosk PC status queue의 호출 graph·offline recovery·CSV/self-test 공유 worker·Activity 종료 cleanup을 재대조했다. Git 브랜치 codex/fix-submit-recovery-timeout, HEAD/upstream 8c9a97ca55d34e3d93fdb02d9879ad929ed7133d는 동일했고, 기존 사용자 수정·미추적 diagnostics/·output/·tmp/·계약 문서를 보존했다. kiosk·webpoc·pc_receiver·scripts source scope는 변경되지 않았으며, adb executable은 현재 PATH에 없다.
- 사실: MainActivity의 pcControlExecutor는 별도 상한 없는 Executors.newSingleThreadExecutor()이고, reportPcStatus()는 상태 전이마다 state·studentName·notify를 캡처한 독립 lambda를 제출한다. 최신값 대체(coalescing), 상태 세대, 취소, bounded queue 또는 우선순위는 없다 (MainActivity.kt:162,785-805).
- 사실: reportPcStatus()의 각 task는 withReachablePairedPc()에서 저장 endpoint를 먼저 시도하고 실패하면 현재 Wi-Fi 사설 후보를 최대 254개 탐색하는 PcEndpointResolver를 거친 뒤 복구 endpoint로 같은 operation을 재시도한다. PcControlClient 기본 connect/read timeout은 2,000ms/4,000ms, resolver 전체 timeout은 6,000ms·병렬도는 32다 (MainActivity.kt:807-839; PcControlClient.kt:11-13; PcEndpointResolver.kt:84-145). 초기 실패 약 6초, 후보 탐색 최대 6초, 복구 후 재시도 약 6초가 순차적으로 한 worker를 점유할 수 있다는 것은 timeout 합산에 근거한 상한 추론이며 실제 elapsed time은 측정하지 않았다.
- 사실: 같은 pcControlExecutor에 fetchStudentCsvFromPc()와 runOperationalSelfTest()가 제출된다. CSV 경로는 PC control 완료 뒤 parsed row preview를 별도 ioExecutor에 제출하고, 자가진단은 PC 도달성 결과를 UI callback으로 반환한다 (MainActivity.kt:1750-1812,2584-2649). status 실패는 내부 runCatching으로 삼키지만 queue에서 제거·대체·기능성 작업 우선 처리를 하지 않는다.
- 사실: activeStudentDisplayName은 QR 확인·로그인 중·Web 결과/실패 status에 인자로 전달되며, 성공 status 제출 뒤 null로 바뀐다. 이미 제출된 lambda는 전달 당시의 immutable studentName을 보유한다. 실패 인증 화면·onStop()·onDestroy()에는 공통 clear가 없고 showScanner()와 명시적 QR 취소에서만 null이 된다 (MainActivity.kt:210,274-298,3035-3060,3383-3552,3573-3579,3823-3874).
- 사실: onDestroy()는 pcControlExecutor.shutdownNow()만 호출하고 반환된 대기 Runnable을 순회하지 않으며, reportPcStatus()에는 destroyed/state-generation 확인이 없다. 종료 직전 제출된 task가 실행 중이면 PC pairing 로드·network operation·status send를 계속할 수 있고, queued task에 캡처된 이름을 별도 회수하는 계약도 없다 (MainActivity.kt:785-805,3859-3874).
- 반대 근거: PcControlClient는 status JSON payload와 response payload를 정상·예외 경계에서 지우고, PcEndpointResolver는 실패/경쟁 후보의 pairing 배열·future·resolver executor를 정리한다. 정상 PC 응답 시 queue가 빠르게 소진될 수 있고, 자동 recovery는 인증된 pairing probe만 채택한다. 이 근거는 outbound queue 상한·최신값 대체·Activity 종료 의미 취소를 제공하지 않는다.
- 시험·문서 대조: kiosk source/test 전체에서 reportPcStatus, pcControlExecutor, fetchStudentCsvFromPc, runOperationalSelfTest를 연결하는 queue/offline/shutdown 계측시험은 확인하지 못했다. PC protocol/server 시험은 동기적인 정상 status·CSV와 receiver 동작을 검증할 뿐 Kiosk queue length·status ordering·CSV/self-test starvation·Activity recreation/destroy를 검증하지 않는다. PRODUCT_DECISIONS.md는 지정 PC 상태판·자가진단을 제품 요구로, BUILD_VERIFICATION.md·RELEASE_OPERATIONS.md는 정상 상태·정상 DHCP recovery를 기록하지만 반복 오프라인·queue backlog·기능성 제어 공정성은 완료 기준으로 연결하지 않는다.
- 추론: PC가 응답하지 않는 동안 상태 전이가 반복되면 오래된 상태가 최신 QR/관리자 상태 뒤에 전달되고, 한 task의 timeout·복구 탐색·재시도가 뒤의 CSV fetch 또는 자가진단을 지연시킬 수 있다. 대기 lambda와 failure 경로의 Activity field가 학생 표시명을 보유할 수 있으나 실제 heap 수명·queue 길이·수신기 도착 순서·외부 노출은 확인하지 않았다.
- 이력 대조: status/CSV channel 도입은 a3aa80c, DHCP endpoint recovery는 ca327409이며 현재 관련 source에 후속 bounded queue·generation cancellation·status/CSV executor 분리 commit은 확인되지 않았다. 이는 현재 history 검색 결과이지 제품 의도를 단정하는 근거는 아니다.
- 판정: LUNA-0033 P3·후보·중간 신뢰도를 유지하고 새 독립 finding ID는 추가하지 않는다. 이번 반복은 기존 Kiosk outbound queue·stale status·captured display name·shared worker 경계를 재확인했으며, LUNA-0007의 PC inbound 자원, LUNA-0003의 CSV pending 내구성, LUNA-0031의 parsed-row owner와 중복시키지 않았다.
- 미검증: 실제 PC 오프라인·DHCP recovery·status 반복·queue/heap 계측·CSV fetch·자가진단·Activity recreation/destroy·ADB·학생/QR/PIN·네트워크·receiver 상태는 사용하지 않았다. 테스트·lint·typecheck·build·install 및 Git stage/commit/push/PR/deploy/rollback도 수행하지 않았다. 이번 반복의 지속 변경은 이 보고서의 apply_patch뿐이다.
- 다음 읽기 전용 검토는 LUNA-0034 초기 PC pairing QR host·receiver identity trust와 fake/public/DNS/loopback/multicast 입력 경계의 source/test/document/history 연결 대조다.

### 2026-08-02 18:48:21

- 자동 재개 계약에 따라 다른 작업보다 먼저 계약 문서를 처음부터 EOF까지 다시 읽었다. 계약 문서는 357줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`이며, 그 뒤 누적 보고서도 처음부터 EOF까지 읽고 이번 반복을 시작했다. Goal은 계속 활성 상태로 두고, 허용된 지속 변경은 이 보고서의 누적 append뿐으로 유지했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`·`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`·`output/`·`tmp/`·계약 문서를 보존했고, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 변경하지 않았다. `adb` executable은 PATH에서 찾지 못해 A 상태는 읽지 않았다.
- 사실: Kiosk `handleRawQr()`는 pairing mode와 `MATHOLIC-PC1:` prefix만 먼저 확인한 뒤 raw QR을 `savePcPairing()`에 넘긴다. 저장 단계의 `PcReceiverPairing.decode()`는 version/Base64/길이, 16-byte receiver ID, 32-byte secret, 1024..65535 port, 1..255 printable ASCII host, display name 형식은 검사하지만 RFC1918/private host, loopback·multicast·public IP, DNS 이름의 지정 PC identity 정책은 검사하지 않는다 (`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:2975-3022`; `transfer/PcReceiverPairing.kt:14-22,80-120`).
- 사실: `PcPairingStore`는 raw pairing을 Android Keystore AES-GCM ciphertext와 IV로 저장하고, decode된 receiver ID·secret 배열은 정상/예외 경계에서 지운다. 이 at-rest 통제는 확인되지만 저장 전에 source host와 receiver 실체를 attest하지 않으며, 초기 QR의 immutable raw String·display name을 신뢰하는 trust-on-first-use 자체를 바꾸지 않는다 (`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcPairingStore.kt:15-77`).
- 사실: Python receiver의 `Pairing.__post_init__()`도 host nonempty/255자 이하만 확인하고, `encode_pairing()`은 ASCII 변환·길이만 확인한다. `ReceiverApplication`은 시작 시 `current_lan_ipv4()` 결과를 `self.host`에 한 번 저장하고, `_build_window()`에서 그 host/port와 config의 receiver ID·secret·display name으로 QR을 한 번 생성한다. route probe는 loopback `127.*`만 거부하며 RFC1918/private 또는 동일 Wi-Fi interface/수신기 identity attestation을 보장하지 않는다 (`pc_receiver/src/matholic_pdf_receiver/config.py:28-56`; `app.py:33-49,61-114`; `protocol.py:46-64,115-163`).
- 사실: 저장 후 Kiosk의 `withReachablePairedPc()`는 원래 pairing host에 먼저 operation을 시도하고, 실패할 때만 현재 Wi-Fi의 RFC1918 `/24` 후보와 기존 secret/receiver ID로 authenticated probe를 수행해 새 주소를 저장·재시도한다. `PcControlClient`는 pairing host/port를 `InetSocketAddress`에 직접 넣어 status/control을 보낸다. 따라서 recovery의 private subnet 제한은 초기 QR의 host/source trust를 보완하는 정책이 아니라 이미 승인된 pairing의 DHCP 주소 변경 경계다 (`MainActivity.kt:807-839`; `transfer/PcEndpointResolver.kt:14-39,79-145`; `transfer/PcControlClient.kt:77-106`).
- 사실: AES-GCM/HMAC 기반 protocol은 QR에 들어온 receiver ID·secret을 알고 있는 상대와의 request/response binding을 확인한다. 그러나 이 key material과 endpoint가 최초 QR에서 함께 self-assert되므로, 가짜 receiver가 자기 host/port/ID/secret/display name을 만들어 QR로 제시하는 경우 그 QR이 지정 PC에서 나온 것인지 독립적으로 증명하지 않는다. 운영 문서는 관리자에게 display name `DESKTOP-D4AGJI7`을 수동 대조하도록 하므로 완화 절차는 있으나 코드가 그 대조를 강제하지 않는다 (`docs/RELEASE_OPERATIONS.md:206-219`; `docs/PRODUCT_DECISIONS.md:92-97`).
- 시험 대조: Kiosk protocol/store 시험과 Python protocol/server 시험은 고정 private IPv4 round-trip, Keystore 암호화 저장, receiver ID/secret 인증, tampered response/replay 및 local `127.0.0.1` server fixture를 다룬다. `PcEndpointResolverTest`는 recovery용 private `/24`·authenticated candidate만 검사한다. public/DNS/loopback/multicast/fake QR의 초기 decode/save negative case, initial identity attestation, app 시작 후 interface 변경에 따른 QR refresh는 시험에서 확인하지 못했다 (`kiosk/src/test/.../PcTransferProtocolTest.kt`, `PcControlProtocolTest.kt`, `PcEndpointResolverTest.kt`, `kiosk/src/androidTest/.../PcPairingStoreInstrumentedTest.kt`, `pc_receiver/tests/test_protocol.py`, `test_server.py`).
- 이력 대조: receiver pairing 도입은 `0b0df69`, Kiosk PC PDF pairing은 `474132b`, status/CSV channel은 `a3aa80c`, DHCP endpoint recovery는 `ca32740`이다. 현재 관련 경로의 history 검색에서 초기 host private-policy 또는 out-of-band identity attestation/negative fixture를 추가한 후속 commit은 확인하지 못했다. 이는 검색된 Git 이력에 대한 사실이며 제품 의도를 단정하지 않는다.
- 추론: 관리자 PIN 뒤 fake QR을 지정 PC QR로 승인하면 형식 검사를 통과한 self-asserted endpoint가 저장되고, 이후 status 또는 카드 PDF operation이 그 endpoint로 향할 수 있다. fake receiver가 QR의 secret을 알고 있으면 protocol 인증도 통과할 수 있으므로, 이 후보의 핵심은 passive LAN sniffing이 아니라 초기 QR source와 지정 receiver identity의 미검증이다. 실제 fake QR·network connection·status·학생/카드 PDF 전송은 수행하지 않았다.
- 반대 근거와 범위: physical QR 촬영 후 display name을 운영자가 확인하는 명시적 절차, Keystore at-rest encryption, authenticated response binding, recovery의 same-private-`/24` 제한은 위험을 낮춘다. public/DNS host를 합법적으로 지원하는 배포가 있다면 초기 private-only 거부는 호환성 변경이 되므로, 제품 허용 host 범위와 attestation/운영자 확인 수준을 먼저 결정해야 한다. 이번 대조만으로 확정 결함이나 즉시 구현 방향을 단정하지 않는다.
- 판정: 기존 `LUNA-0034` 초기 PC pairing endpoint trust 후보를 `P3`·후보·중간 신뢰도로 유지하고 마지막 확인 시각을 이번 cycle로 갱신한다. 새 독립 finding ID는 추가하지 않으며 `LUNA-0002` at-rest secret, `LUNA-0030` transient pairing String, `LUNA-0007` receiver resource와 합치지 않는다.
- 필요한 후속 검증은 구현하지 않고 큐에 남긴다: 제품 host 정책 확정, public/DNS/loopback/multicast/fake display name fixture, no-external-connect 초기 저장 시험, source receiver identity attestation/운영자 확인 회귀, interface/DHCP 변경 뒤 QR refresh와 기존 recovery 호환성 대조.
- 미검증: 실제 QR 촬영·QR 원문/hash·fake/public/DNS/loopback/multicast endpoint·네트워크·PC receiver·status·학생·카드 PDF·PIN·ADB·테스트·lint·typecheck·build·install은 사용/실행하지 않았다. Git stage/commit/push/PR/deploy/rollback도 수행하지 않았고, 이번 반복의 지속 변경은 이 보고서의 `apply_patch`뿐이다.
- 다음 읽기 전용 검토는 `LUNA-0037` pending-card PC PDF delivery·`markCardsDelivered()`와 student mutation invalidation/회귀시험 연결 대조다.

### 2026-08-02 18:55:29

- 자동 재개 계약에 따라 계약 문서를 다른 조사보다 먼저 처음부터 EOF까지 읽었다. 계약 문서는 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`이며, 누적 보고서도 그 뒤 처음부터 EOF까지 읽었다. Goal은 계속 활성 상태로 두고 지속 변경 대상은 이 보고서 하나로 제한했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`·`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`·`output/`·`tmp/`·계약 문서를 보존했으며, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 변경하지 않았다. `adb` executable은 PATH에서 찾지 못해 A 상태는 읽지 않았다.
- 사실: 단건 PC 전송의 `QrPreview`는 `studentId`, `exactName`, `Bitmap`만 보유하고 issued QR hash/issuedAt/revision을 보유하지 않는다 (`MainActivity.kt:3955-3959`). `showQrPreview()`는 paired PC가 있으면 `sendPcPdfButton`을 활성화하지만 `updateStudentManagementControls()`의 `studentMutationGate`는 reissue/profile/credential/deactivate/CSV/pending-card 버튼만 제어하고 PC 전송 버튼은 제어하지 않는다 (`MainActivity.kt:2891-2899,1733-1752`).
- 사실: `preparePcPdfTransfer()`는 UI에서 preview identity·bitmap 상태를 한 번 확인하고 bitmap을 복사한 뒤, worker에서 캡처된 `preview.exactName`/bitmap으로 PDF를 만들고 `PcPdfSender.send()` 후 `markCardsDelivered(setOf(preview.studentId))`를 호출한다. worker가 시작할 때 현재 `issuedQrPreview`나 DB의 현재 `qrTokenHash`·`issuedAtEpochMs`를 다시 확인하지 않으며, 성공 callback의 `clearQrPreview()`도 captured preview가 여전히 current인지 확인하지 않는다 (`MainActivity.kt:2307-2385`).
- 사실: pending-card batch 경로도 `studentMutationGate`를 획득하지 않는다. `preparePendingCardsPdf()`는 pending button만 false로 만들고, `reissueQrBatch()`→bitmap/PDF 생성→PC ACK→`markCardsDelivered(studentIds)`를 하나의 `ioExecutor` task에서 수행한다. CSV 적용도 `applyStudentCsv()`에서 별도 gate를 시작하지 않는다 (`MainActivity.kt:1934-2019,1849-1880`; `StudentRepository.kt:236-280,307-409`). `ioExecutor`가 single-thread라 DB interleave를 줄이는 반대 근거는 있지만 operation generation이나 PC artifact invalidation은 없다 (`MainActivity.kt:161-183`).
- 사실: repository mutation은 카드 revision과 delivery 상태를 서로 다른 단순 필드로 갱신한다. `reissueQr()`/`reissueQrBatch()`는 `qrTokenHash`를 교체하고 `lastDeliveredAtEpochMs=null`, `needsPrint=true`로 만들며, 이름 변경은 QR hash를 유지한 채 `setNeedsPrint(..., true)`를 호출한다. CSV 이름 변경도 같은 `needsPrint`만 세운다. 반면 `markCardsDelivered()`/DAO `markDelivered()`는 student ID만 조건으로 `lastDeliveredAtEpochMs`를 쓰고 `needsPrint=0`으로 만든다. 기대한 issued hash·issue time·operation/request ID 조건은 없다 (`StudentRepository.kt:160-181,236-280,476-514`; `Daos.kt:102-125`).
- 사실: `deactivateStudent()`는 active flag와 QR hash를 교체하지만 이미 PC에 저장된 PDF를 회수하거나 삭제하는 경로는 없다. `PcTransferProtocol` request/ACK에는 filename, PDF hash, request ID와 receiver 인증만 있고 student/card revision은 없으며, receiver는 PDF를 unique destination으로 `os.replace()`한 뒤 ACK를 보낸다. Kiosk의 이후 student mutation이 receiver 파일을 무효화할 API나 삭제 계약은 확인되지 않았다 (`StudentRepository.kt:619-637`; `PcTransferProtocol.kt:33-74`; `pc_receiver/src/matholic_pdf_receiver/server.py:90-114,213-237`).
- 추론: (1) 이름 변경·QR 재발급·CSV 적용 task가 먼저 DB를 갱신한 뒤, 사용자가 그 전에 캡처한 old preview 전송 task가 실행되면 old 이름 또는 이미 폐기된 QR이 PC에 저장될 수 있다. 전송 후 `markCardsDelivered(studentId)`가 새 revision의 `needsPrint`까지 지워 “전달 완료”로 보이게 할 수 있고, reissue 성공 뒤 표시된 새 preview를 old-transfer 성공 callback의 무조건적인 `clearQrPreview()`가 지울 수 있다. (2) PC 전송이 먼저 끝난 뒤 mutation이 실행되면 DB는 새 카드/이름을 pending 또는 revoked로 표시하지만 이미 저장된 old PDF는 PC 폴더에 남는다. batch flow도 전송 이후 mutation invalidation·artifact cleanup을 하지 않는다. 이는 single-thread가 순서를 직렬화해도 해결되지 않는 stale artifact/state binding 문제다.
- 안전·반대 근거: `reissueQrBatch()`와 repository mutation은 transaction으로 원자화되고, `PcPdfSender`는 ACK의 request ID·PDF SHA-256을 검증하며, receiver는 commit-before-ACK·replay ID를 사용한다. 전송 시작 시 `recordQrExportRequested()`가 inactive student를 거부하고, 정상 순차 실행에서는 current state를 읽어 batch QR을 만들 가능성이 높다. 이 통제들은 전송 대상이 “현재 카드 revision”인지, 전송 후 mutation이 파일을 폐기해야 하는지를 보장하지 않는다.
- 시험 대조: `RepositoryInstrumentedTest`는 profile/credential/deactivation과 CSV 이름 변경, 순차 `markCardsDelivered()`·`needsPrint` 결과를 검증하지만 PC 전송과 교차하지 않는다 (`RepositoryInstrumentedTest.kt:342-399,493-544`). `SensitiveTaskTest`는 queued cleanup만 검증하고, `MainActivityInstrumentedTest`에는 PC PDF 전송·student mutation·receiver file invalidation interleave가 없다. PC protocol/server 시험도 PDF ACK/replay와 local receiver 동작을 검증할 뿐 student/card revision binding은 확인하지 않는다.
- 문서·이력 대조: 운영 문서는 `QR 폐기 및 재발급` 후 `현재 카드 지정 PC로 보내기`, PC 저장 확인, 인쇄 순서를 설명하고 이름 변경 시 “카드를 QR 재발급해 다시 인쇄”하라고 UI가 안내한다 (`docs/RELEASE_OPERATIONS.md:221-231`; `MainActivity.kt:1492-1495`). 카드 lifecycle와 pending PC delivery는 `0193d89`, PC PDF 전송은 `474132b`에서 도입됐으며, 현재 history 검색에서 cross-operation revision binding·receiver artifact invalidation을 추가한 후속 commit은 확인하지 못했다.
- 판정: 기존 `LUNA-0037`을 `P3`·후보·중간 신뢰도로 유지하고, 이번 반복에서 “studentId-only delivered update + captured preview + PC artifact 무효화 부재”라는 구체적 interleave 근거를 보강했다. 새 독립 finding ID는 추가하지 않으며 `LUNA-0036` Android PrintManager cross-operation, `LUNA-0039` local print `needsPrint`, `LUNA-0010` ACK/retry 중복과 합치지 않는다.
- 필요한 후속 검증은 구현하지 않고 큐에 남긴다: PC 전송과 reissue/profile/CSV/deactivate의 공통 gate 또는 issued revision 도입, `markCardsDelivered()`의 expected hash/issue generation/request binding, receiver 파일의 invalidation·재출력 정책, 전송 전·중·후 mutation/Activity 종료/ACK 유실 회귀시험과 stale preview callback 검증.
- 미검증: 실제 학생·QR·PIN·PC receiver·PDF 파일·네트워크·ACK 장애·파일 삭제·프린터·Activity timing·ADB·테스트·lint·typecheck·build·install은 사용/실행하지 않았다. Git stage/commit/push/PR/deploy/rollback도 수행하지 않았고, 이번 반복의 지속 변경은 이 보고서의 `apply_patch`뿐이다.
- 다음 읽기 전용 검토는 `LUNA-0041` PC receiver CSV buffer·queue durability와 전송·취소·재시작 경계다.

### 2026-08-02 19:01:11

- 자동 재개 계약에 따라 계약 문서를 다른 조사보다 먼저 처음부터 EOF까지 읽었다. 계약 문서는 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`이며, 이번 주기의 조사 전에 누적 보고서도 처음부터 EOF까지 읽었다. 보고서 pre-append는 4,639줄·507,717자·SHA-256 `42EB9971814C66BB0EE2E2FA9D50FFC6291EE1C18575A1CD3B19AE89291263FC`였다. Goal은 계속 활성 상태로 두고, 지속 변경 대상은 누적 보고서 한 파일로 제한했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`·`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`·`output/`·`tmp/`·계약 문서를 보존했으며, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope에는 이번 주기 변경이 없다. `git diff --check -- kiosk webpoc pc_receiver scripts`는 통과했다. `adb` executable은 PATH에서 찾지 못해 장치 상태를 읽지 않았다.
- 이번 검토 범위는 Windows PC receiver의 CSV 선택·대기 buffer·제어 요청 소비·취소·shutdown·재시작과 Kiosk의 암호화 CSV 수신·파싱·payload/parsed-row cleanup·Activity 종료 경계다. 실제 PC receiver, 학생 CSV, 자격증명, 네트워크 연결은 사용하지 않고 소스·시험·문서·Git 이력만 읽었다.
- 사실: receiver UI의 `choose_csv()`는 파일을 `read_bytes()`로 읽어 `ReceiverState.queue_csv()`에 넘긴다. `finally`의 `payload = b""`는 함수 지역 변수만 재바인딩하며, 이미 `ReceiverState`에 저장된 immutable `bytes`를 지우지 않는다. `queue_csv()`는 basename·`.csv` 확장자·비어 있지 않음·1MiB 이하·UTF-8-sig 해석을 확인한 뒤 lock 안에서 `(safe_name, bytes(payload))`를 `pending_csv`에 저장한다. `clear_csv()`는 lock 안에서 `pending_csv = None`만 수행하며, 새 mutable buffer로 변환하거나 이전 buffer를 명시적으로 zeroize하는 경로는 없다 (`pc_receiver/src/matholic_pdf_receiver/app.py:201-220,222-224`; `server.py:88,116-128`).
- 사실: `ReceiverState.pending_csv`는 `ConfigStore`에서 복원되지 않는 process-memory tuple이다. `accept_control(CONTROL_FETCH_CSV)`는 lock 안에서 replay ID를 먼저 기억하고 store에 저장한 뒤 대기 payload로 응답 bytes를 만들고 `pending_csv = None`으로 비운다. handler는 그 뒤 `sendall(response)`를 즉시 수행하지만, sendall 예외나 태블릿의 복호화·검증·파싱 실패 때 대기 CSV를 복원하는 경로가 없다. 따라서 응답을 실제로 전달하기 전에 queue가 소비되고, 같은 request ID 재시도도 이미 replay로 거부될 수 있다 (`pc_receiver/src/matholic_pdf_receiver/server.py:130-178,213-249`). 이 consume-before-send와 restart loss의 가용성 측면은 기존 `LUNA-0003` queue durability와 겹치며, 이번 `LUNA-0041`에서는 buffer lifecycle을 별도 관찰했다.
- 사실: receiver `shutdown()`은 tray 중지·server shutdown·server close·Tk root destroy만 호출하고 `receiver_state.clear_csv()`나 pending bytes zeroization을 호출하지 않는다. 창의 `WM_DELETE_WINDOW`는 종료가 아니라 `hide_window`에 연결되어 있고, `main()`에도 mainloop 예외·종료를 포괄하는 `finally` cleanup은 없다. server는 `daemon_threads = True`이며 앱 코드에 active handler join, 전송 실패 복구, shutdown 중 CSV 정리 경계가 명시돼 있지 않다. 이벤트 queue도 무제한 `queue.Queue()`로 생성되지만 CSV payload 자체가 이벤트에 실리지는 않는다 (`app.py:38,55,258-299`; `server.py:253`).
- 사실: receiver의 CSV 경로에서 queue에 저장된 `bytes`, `encode_control_response()`가 참조하는 payload, 암호화·frame/header/handler send 경계의 임시 bytes는 immutable reference 중심이다. 교체·수동 취소·성공 소비·실패·shutdown별 명시적 zeroization은 확인되지 않았다. 이는 Python garbage collector가 언제 객체를 회수하는지와 별개의 lifecycle 사실이며, 메모리에서 즉시 지워진다고 볼 근거가 없다.
- 사실: Kiosk `PcControlClient.fetchStudentCsv()`는 거부 응답이나 잘못된 label/payload에서는 response payload를 fill하지만, 성공 응답에서는 `PcCsvDownload(filename, payload)`로 `ByteArray` 소유권을 호출자에게 넘긴다. `MainActivity.fetchStudentCsvFromPc()`의 정상 parse/예외 경계는 `download.payload.fill(0)`를 수행하고, parser는 1MiB·행 수·header·중복 username을 확인한 뒤 immutable `String` records/fields와 credential `CharArray`를 만든다. `ParsedStudentCsv.clearSensitiveData()`는 row의 username/password 배열을 지운다 (`kiosk/src/main/java/com/local/matholickiosk/kiosk/transfer/PcControlClient.kt:8-115`; `data/StudentCsvImport.kt:1-96`; `MainActivity.kt:1750-1810`).
- 사실: 해당 Kiosk CSV fetch는 `pcControlExecutor`의 plain `Runnable`로 실행되고, parsed rows를 넘기는 `ioExecutor.execute { ... }`에도 `SensitiveTask` wrapper나 제출 실패 시 parsed-row cleanup이 없다. `onDestroy()`는 `ioExecutor.shutdownNow()` 뒤 queued `SensitiveTask`만 `discard()`하고, 일반 CSV Runnable/그 안의 parsed rows에 대한 소유권 회수는 별도 처리하지 않는다. 이 executor 경계는 기존 `LUNA-0031` parsed-row lifecycle 후보와 연결하고, parser가 만드는 immutable `String`은 기존 `LUNA-0032`, `PcControlProtocol` request-ID mismatch 뒤 plaintext cleanup 경계는 기존 `LUNA-0014`로 중복 등록하지 않는다 (`MainActivity.kt:1791-1810,3860-3873`; `domain/SensitiveTask.kt:1-28`; `transfer/PcControlProtocol.kt:76-136`).
- 시험 대조: Python `test_receiver_accepts_status_and_serves_csv_once`는 한 번 queue한 CSV의 정상 authenticated fetch와 두 번째 fetch 거부만 확인한다. protocol 시험은 CSV response authentication을 확인하고, app 시험은 `--smoke-check` dispatch를 확인한다. Kiosk `StudentCsvParserTest`는 quoted Korean CSV, duplicate username, unexpected header를 확인하며, `SensitiveTaskTest`는 일반 queued cleanup을 확인한다. CSV 교체·수동 취소·receiver sendall failure·replay 후 재시도·receiver process restart·shutdown 중 pending cleanup·Kiosk 복호화/parse 실패 재시도·Activity destroy 중 CSV Runnable/parsed-row cleanup·실제 PC folder 상태를 확인하는 시험은 찾지 못했다 (`pc_receiver/tests/test_server.py:77-132`, `test_protocol.py`; `kiosk/src/test/.../StudentCsvParserTest.kt`, `SensitiveTaskTest.kt`).
- 문서 대조: 제품 결정은 CSV 형식과 1MiB·1~1000명 제한을 기록한다 (`docs/PRODUCT_DECISIONS.md:86,103-104`). 빌드 검증은 암호화 CSV channel 추가, receiver pytest 10/10·11/11과 packaged smoke, 1080p CSV 버튼 상태 경쟁 수정을 기록하지만, queue durability·소비 시점·재시작 복원·buffer zeroization 계약은 기록하지 않는다 (`docs/BUILD_VERIFICATION.md:5755-5772,5915-5918,5978-5993`). `docs/RELEASE_OPERATIONS.md`의 CSV 검색 결과는 이 lifecycle 정책을 추가로 정의하지 않는다.
- 이력 대조: `pending_csv`·`queue_csv`·`clear_csv`의 도입과 `fetchStudentCsv` 경로를 바꾼 검색 결과의 핵심 commit은 `a3aa80c feat: add paired PC status and CSV channel`이며, Kiosk 운영·card lifecycle의 선행 commit은 `0193d89`다. 검색한 Git 이력에서 CSV buffer zeroization·send 실패 복구·재시작 persistence·Kiosk shutdown ownership을 후속으로 명시한 commit은 확인하지 못했다. 이는 검색 범위에 대한 사실이며 제품 의도를 단정하지 않는다.
- 추론: (1) CSV를 교체하거나 `clear_csv()`·정상 fetch·shutdown할 때 old payload의 참조만 버려지므로 Python heap에서 즉시 지워진다고 보장할 수 없다. (2) receiver process가 재시작되면 memory-only `pending_csv`는 복원되지 않는다. (3) 응답 encode 뒤 `pending_csv`를 지우고 `sendall()`하는 현재 순서에서는 네트워크 write 실패 또는 Kiosk의 복호화·parse 실패 시 사용자가 다시 받을 수 있는 CSV가 사라질 수 있다. (4) Kiosk의 정상 parse 경로는 payload와 parsed credential 배열을 지우지만, Activity 종료와 `ioExecutor.execute` 제출 실패가 겹치면 parsed rows가 기존 `LUNA-0031` 경계에서 남을 수 있다. 실제 실패·재시작·메모리 상태는 실행하지 않았으므로 위 네 항목은 정적 interleave 추론이다.
- 반대 근거와 범위: 1MiB/UTF-8/filename validation, lock, AES-GCM authenticated control response, replay ID, Kiosk의 정상 `ByteArray.fill(0)`와 parsed-row cleanup, 정상 one-shot/replay 시험은 위험을 낮춘다. 다만 이 통제들은 receiver의 consume-before-send, process restart loss, explicit zeroization, Kiosk executor ownership을 보장하지 않는다. CSV를 재요청할 때마다 새 파일을 허용하는 운영 정책이라면 일부 재시도 UX가 달라질 수 있으나, 현재 소스에는 그 정책이나 durable spool 계약이 없다.
- 판정: 기존 `LUNA-0041`을 `P4`·후보·중간 신뢰도로 유지한다. 이번 반복은 “immutable in-memory pending CSV의 명시적 zeroization 부재”와 “send/restart/parse 실패 전에 queue가 소비되는 경계”를 구체화했지만, consume-before-send와 Kiosk parsed-row executor ownership은 각각 기존 `LUNA-0003`·`LUNA-0031/0032`·`LUNA-0014`와 중복되지 않도록 교차 참조한다. 새 독립 finding ID는 추가하지 않는다.
- 필요한 후속 검증은 구현하지 않고 큐에 남긴다: (a) pending CSV의 mutable/zeroizable owner와 queue 교체·취소·성공·실패·shutdown lifecycle 정의, (b) sendall/복호화/parse 성공 또는 명시적 retry/ACK 이후에만 consume하는 durable spool 정책, (c) restart 후 복원·중복·replay 정책, (d) receiver active handler 종료 경계와 app `finally` cleanup, (e) Kiosk CSV fetch를 `SensitiveTask` 또는 동등한 owner scope로 묶어 Activity destroy/RejectedExecutionException 때 rows를 회수하는 회귀시험, (f) 위 interleave를 다루는 Python/Kotlin 시험 추가.
- 미검증: 실제 PC receiver UI·tray·shutdown·재시작·CSV 선택·sendall 장애·태블릿 복호화/parse 실패·네트워크·학생 CSV·학생 자격증명·Kiosk Activity timing·ADB·테스트·lint·typecheck·build·install은 사용/실행하지 않았다. Git stage/commit/push/PR/deploy/rollback도 수행하지 않았고, 이번 반복의 지속 변경은 이 보고서의 `apply_patch`뿐이다.
- 다음 읽기 전용 검토는 `LUNA-0042` PC receiver startup·tray·window close·server lifecycle과 unattended recovery 경계다.

### 2026-08-02 19:08:21

- 자동 재개 계약에 따라 계약 문서를 다른 조사보다 먼저 처음부터 EOF까지 읽었다. 계약 문서는 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`이며, 이번 주기 조사 전에 누적 보고서도 처음부터 EOF까지 읽었다. 보고서 pre-append는 4,659줄·515,322자·SHA-256 `10BC860A7323E6E4655A12E3311FF09AD40479BE5353E503C3524E4998B96324`였다. Goal은 계속 활성 상태로 두고 지속 변경 대상은 누적 보고서 한 파일로 제한했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`·`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`·`output/`·`tmp/`·계약 문서를 보존했으며, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope에는 이번 주기 변경이 없다. `git diff --check -- kiosk webpoc pc_receiver scripts`는 통과했다. `adb` executable은 PATH에서 찾지 못해 A 기기 상태를 읽지 않았다.
- 이번 심화 범위는 `pc_receiver`의 `current_lan_ipv4()`·Windows Startup shortcut·`--background`, Tk/pystray callback thread, listener/server/handler shutdown과 active transfer 경계다. 기존 `LUNA-0042`의 listener 선행 bind·좁은 startup exception·tray health 공백을 반복 확인하는 대신, 네트워크 준비 지연과 정상 종료 시 in-flight owner를 추가 대조했다. 실제 PC·Windows tray·재부팅·네트워크·PDF/CSV 전송은 사용하지 않았다.
- 사실: `current_lan_ipv4()`는 UDP socket을 `192.0.2.1:9`에 connect해 OS가 선택한 local IPv4를 한 번 읽고 socket을 닫는다. timeout·backoff·network-change callback·주기 retry는 없고, 빈 주소 또는 `127.*`만 거부한다. `ReceiverApplication.__init__()`는 이 함수를 한 번 호출한 뒤 `ThreadedReceiverServer(("0.0.0.0", port), ...)`를 생성하고 daemon `server_thread`를 시작한다. 이후 Tk root·window·tray를 초기화한다 (`pc_receiver/src/matholic_pdf_receiver/config.py:28-39`; `app.py:34-79`).
- 사실: 설치 스크립트는 현재 PowerShell 사용자 환경의 `%LOCALAPPDATA%\MatholicPdfReceiver\app\MatholicPdfReceiver.exe`를 `[Environment]::GetFolderPath('Startup')`의 `.lnk`에 복사하고 `--background`만 지정한다. source/스크립트에는 네트워크가 준비될 때까지 기다리는 scheduled retry, Windows service, restart-on-failure, listener health watchdog 또는 delayed task가 없다. 제거 스크립트도 동일한 현재 사용자 scope의 Startup shortcut을 지우고 receiver process를 `Stop-Process -Force`한다 (`pc_receiver/install-receiver.ps1:7-34`; `uninstall-receiver.ps1:1-14`).
- 사실: `main()`은 `ReceiverApplication` 생성 예외를 `OSError`·`RuntimeError`·`ValueError` 목록으로만 처리하고, error dialog가 닫힌 뒤 `SystemExit(1)`로 끝낸다. `current_lan_ipv4()`가 network-ready 이전에 예외를 내면 자동 실행 한 번이 종료되고 재시도 호출자는 없다. 반대로 OS가 `0.0.0.0` 같은 비-loopback 문자열을 반환하는 경우를 이 함수 자체가 거부하지 않는다는 정적 사실은 확인했지만, endpoint/host 정책 문제는 기존 `LUNA-0034`와 겹치므로 이번 항목의 별도 finding으로 만들지 않았다 (`config.py:28-39`; `app.py:280-295`).
- 사실: `pystray.Icon.run`은 `threading.Thread(..., daemon=True)`에서 실행되고, 메뉴 callback `_tray_show()`·`_tray_open_folder()`·`_tray_quit()`은 pystray callback 경계에서 Tk `root.after(0, ...)`를 직접 호출한다. tray thread handle을 저장하지 않으며, `tray.run`을 감싸는 예외 전달·health flag·재시작 경로가 없다. Tk UI의 나머지 event drain은 `root.mainloop()`와 `_poll_events()`에 의존한다 (`app.py:68-79,226-256`).
- 사실: 창 닫기는 `WM_DELETE_WINDOW → hide_window()`라서 root를 숨기고 listener를 계속 유지하는 의도된 동작이다. `--background`에서는 처음부터 root가 숨겨져 tray가 유일한 운영자 표시·복귀·종료 경로다. 따라서 tray thread가 시작되지 않거나 예외로 끝나는 경우 source에 숨겨진 root를 다시 보이게 하거나 operator-visible failure를 남기는 별도 fallback은 없다 (`app.py:51-66,173-193,249-256`; `README.md:40-41`).
- 사실: `shutdown()`은 `tray.stop()` → `server.shutdown()` → `server.server_close()` → `root.destroy()` 순서로 호출하지만 `server_thread` 또는 pystray thread를 `join()`하지 않는다. `ThreadedReceiverServer`는 `daemon_threads = True`이고 `_ReceiverHandler`는 `state.accept()`에서 PDF를 쓰거나 control response를 만든 뒤 `sendall()`하는 active handler인데, shutdown 시 신규 connection 차단·active handler drain 완료·ACK 전송 완료를 app lifecycle과 묶는 barrier가 없다 (`app.py:184-185,258-262`; `server.py:90-114,213-257`).
- 사실: `run()`은 `root.mainloop()`만 호출하고 `application.run()` 뒤 `finally` cleanup이 없다. 정상 tray Quit callback은 Tk main thread에 `shutdown()`을 예약하지만, root가 다른 이유로 destroy되어 mainloop가 반환하거나 shutdown 단계 중 하나가 예외를 내는 경우 후속 cleanup을 보장하는 공통 보상 경계는 없다 (`app.py:184-185,255-295`).
- 문서 대조: `RELEASE_OPERATIONS.md`는 PC 수신기 창이 실행 중인지 확인하고, 2026-07-29 완전 재부팅 뒤 자동 실행·TCP 48129 대기를 확인했다고 기록한다. `BUILD_VERIFICATION.md`도 단일 정상 재부팅에서 `--background` 실행과 LISTEN을 기록한다. 이 기록은 정상 network-ready/logon과 정상 listener의 증거이지, Wi-Fi 지연·Startup process failure·tray callback/thread failure·active transfer 중 Quit의 증거는 아니다 (`docs/RELEASE_OPERATIONS.md:236-250`; `docs/BUILD_VERIFICATION.md:4979-4986`).
- 시험 대조: `pc_receiver/tests/test_app.py`는 `--smoke-check`가 `smoke.main()`을 호출하는지만 검사한다. `test_server.py`의 TCP fixture는 이미 생성된 server를 `shutdown()`·`server_close()`한 뒤 fixture thread를 직접 `join()`하는 정상 종료만 확인한다. `current_lan_ipv4()` 예외/지연, Startup shortcut, `--background`, Tk/pystray callback thread, tray exception, active handler shutdown/drain, root mainloop 반환, server thread health를 연결한 시험은 찾지 못했다. 테스트·빌드는 계약상 실행하지 않았다 (`pc_receiver/tests/test_app.py:6-13`; `test_server.py:60-81`).
- 이력 대조: receiver 도입은 `0b0df69`, 패키징 entrypoint/smoke 검증은 `d225ed6`·`d0f7302`, 자동 시작 실기 문서 보강은 `7f7128d`다. `git log -S`로 `current_lan_ipv4`, `self.tray.run`, `WM_DELETE_WINDOW`, `server.shutdown`, `--background`를 검색한 범위에서는 네트워크 retry/watchdog, tray UI-thread queue/health, active handler drain을 추가한 후속 source commit은 확인하지 못했다. 이는 검색 범위의 사실이며 제품 의도를 단정하지 않는다.
- 추론: (1) Windows logon/Startup 시점에 local route가 아직 준비되지 않아 `current_lan_ipv4()`가 예외를 내면 receiver는 한 번 종료되고, per-user shortcut 자체에는 retry/restart가 없어 Kiosk의 endpoint recovery도 수신기 프로세스를 다시 켜지 못한다. (2) tray initialization 또는 `tray.run` callback thread가 실패하면 `--background`의 숨겨진 root와 listener가 서로 다른 상태로 남아, 서버가 살아 있어도 운영자가 상태 확인·창 복귀·정상 종료를 못할 수 있다. (3) Quit 중 active PDF/control handler가 완료되기 전에 root/main이 끝나면 daemon handler가 ACK·event까지 도달하지 못할 수 있고, 이는 기존 PDF ACK/retry `LUNA-0010`과 연결되는 shutdown 경계다. 실제 네트워크 지연·tray fault·active transfer·process 종료는 실행하지 않았으므로 모두 정적 lifecycle 추론이다.
- 반대 근거와 범위: 2026-07-29 정상 PC 재부팅·TCP LISTEN 기록, `0.0.0.0` listener의 interface 수용, Kiosk의 authenticated DHCP endpoint recovery, 정상 PDF의 atomic temporary write/`os.replace()`·replay 기록, server fixture의 explicit join은 정상 경로를 지지한다. daemon thread는 process 종료 시 OS가 회수하므로 영구적인 resource leak으로 확대하지 않는다. `LUNA-0007`의 인증 전 resource 상한, `LUNA-0009`의 tray 개인정보 표시, `LUNA-0010`의 PDF commit/ACK/retry, `LUNA-0041`의 CSV buffer ownership과 중복시키지 않고 startup/tray/active-shutdown owner 범위로 제한한다.
- 판정: 기존 `LUNA-0042`를 `P4`·후보·중간 신뢰도로 유지한다. 이번 주기는 “network-ready 이전 단발성 Startup 실행”, “tray daemon/UI callback health·thread-affinity 경계”, “active handler drain/join 부재”를 추가했지만, 정상 재부팅 기록과 실제 fault 미검증을 반영해 심각도를 올리지 않고 새 독립 ID도 추가하지 않는다.
- 필요한 후속 검증은 구현하지 않고 큐에 남긴다: (a) network absent/late route와 `current_lan_ipv4()` 예외·0.0.0.0·interface change 매트릭스, (b) delayed Startup/backoff 또는 service/scheduled restart 정책과 per-user scope 확인, (c) Tk/pystray/server bind 단계별 failure에서 역순 cleanup·operator-visible status·tray health/restart를 확인하는 모의시험, (d) `--background` tray show/open/quit callback과 mainloop 반환의 thread-safe dispatch, (e) active PDF/control handler 중 Quit·ACK 유실·재시작 뒤 file/replay/retry 결과와 server/tray thread join/drain 회귀시험.
- 미검증: 실제 Windows logon·재부팅·Startup shortcut 실행·Wi-Fi/route 지연·PC receiver·Tk·pystray·tray 클릭·포트 점유·active PDF/CSV 전송·ACK 유실·process 종료·파일 생성·ADB·테스트·lint·typecheck·build·install은 사용/실행하지 않았다. Git stage/commit/push/PR/deploy/rollback도 수행하지 않았고, 이번 반복의 지속 변경은 이 보고서의 `apply_patch`뿐이다.
- 다음 읽기 전용 검토는 `LUNA-0002` PC receiver `ConfigStore` secret at-rest·Windows 파일 권한·백업/업그레이드/제거 보존 경계다.

### 2026-08-02 19:14:52

- 자동 재개 계약에 따라 계약 문서를 다른 조사보다 먼저 처음부터 EOF까지 읽었다. 계약 문서는 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`이며, 조사 전에 누적 보고서도 처음부터 EOF까지 읽었다. 보고서 pre-append는 4,683줄·523,531자·SHA-256 `098DD8987DD885471B4C14DB150144DD00E5743200A812E643D7EBFBF5CF96E9`였다. Goal은 계속 활성 상태로 두고 지속 변경 대상은 누적 보고서 한 파일로 제한했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`·`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`·`output/`·`tmp/`·계약 문서를 보존했으며, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope에는 이번 주기 변경이 없다. `git diff --check -- kiosk webpoc pc_receiver scripts`는 통과했다. `adb` executable은 PATH에서 찾지 못해 A 상태를 읽지 않았다.
- 이번 주기는 기존 `LUNA-0002`의 JSON Base64 저장 사실을 재확인하는 데 그치지 않고, secret 생성·로드·원자 저장·임시 파일·Windows ACL 상속·설치/업그레이드/제거 뒤 pairing 보존과 문서의 backup/revocation 경계를 대조했다. secret 원문·QR·config payload는 읽거나 출력하지 않았다.
- 사실: `default_config_dir()`는 `LOCALAPPDATA`가 있으면 `%LOCALAPPDATA%\MatholicPdfReceiver`를, 없으면 `Path.home()\AppData\Local\MatholicPdfReceiver`를 사용한다. `ConfigStore.load_or_create()`는 파일이 없을 때만 `os.urandom(16)` receiver ID와 `os.urandom(32)` secret을 만들고, 파일이 있으면 `load()`로 재사용한다. `load()`는 JSON의 Base64 `receiver_id`·`secret`을 decode하고 `Pairing(host="127.0.0.1")`의 길이 검증을 통과시키지만, secret의 DPAPI/Credential Manager 복호화나 사용자-bound key 검증은 하지 않는다 (`pc_receiver/src/matholic_pdf_receiver/config.py:13-15,67-94`; `protocol.py:46-64`).
- 사실: `ConfigStore.save()`는 `self.path.parent.mkdir()`과 `receive_dir.mkdir()` 뒤 `receiver_id`·`secret`·port·display name·receive directory·replay list를 JSON 문자열로 만들어 같은 디렉터리의 `config.tmp`에 `write_text()`하고 `os.replace()`한다. `finally`의 임시 파일 삭제, `FlushFileBuffers`/fsync, DPAPI/Credential Manager, explicit DACL/ACL 설정은 없다. 따라서 정상 성공에서는 atomic name replacement가 있지만, write/replace/권한/프로세스 중단 실패 시 secret을 포함한 temp artifact와 기존/새 설정의 일관성을 보장하는 보상 경계는 source에 없다 (`config.py:96-115`).
- 사실: `install-receiver.ps1`은 executable을 `%LOCALAPPDATA%\MatholicPdfReceiver\app`에 복사하고 Startup shortcut·Private TCP 48129 firewall rule을 만든다. config 파일이나 config directory에 별도 ACL을 적용하지 않고, 기존 config를 백업·암호화·회전하지 않는다. `uninstall-receiver.ps1`은 process를 `Stop-Process -Force`한 뒤 Startup shortcut·firewall rule만 제거하고 `%LOCALAPPDATA%\MatholicPdfReceiver`의 config와 receive PDF를 보존한다. 재설치 시 `load_or_create()`는 남은 config를 그대로 재사용하므로 secret revoke/rotate나 PC 소유권 handoff 경계가 없다 (`pc_receiver/install-receiver.ps1:7-34`; `uninstall-receiver.ps1:1-14`; `config.py:71-79`).
- 사실: 현재 Windows에서 secret 원문을 읽지 않는 메타데이터 확인 결과 `%LOCALAPPDATA%\MatholicPdfReceiver`와 `config.json`은 존재하고 `config.json` 길이는 20,648바이트였다. 파일과 부모 디렉터리의 ACL은 각각 3개 rule·3개 inherited rule로 관찰됐고 `Everyone`/`BUILTIN\Users`/`Authenticated Users`를 대상으로 한 broad read allow rule은 이 요약 검사에서 관찰되지 않았다. `config.tmp`는 존재하지 않았다. 이는 현재 한 시점의 파일 metadata/ACL 결과이며, 파일 내용·secret·전체 identity mapping·백업본은 읽지 않았다.
- 문서 대조: `pc_receiver/README.md`는 제거 시 실행 파일·자동 시작·방화벽만 제거하고 수신 PDF와 pairing 설정을 보존한다고 명시한다. `docs/RELEASE_OPERATIONS.md`도 PC 교체·설정 초기화 뒤 수동 재페어링을 안내하지만, 설정을 안전하게 폐기·회전하는 명령, 소유권 변경/분실 PC revoke, PC config backup 암호화와 복구 대상 범위를 정의하지 않는다 (`pc_receiver/README.md:34-41`; `docs/RELEASE_OPERATIONS.md:236-250`). `SECURITY.md`와 release 문서의 DPAPI 설명은 Android release signing credential에 관한 것이며 PC receiver `config.json`의 보호 계약은 포함하지 않는다 (`SECURITY.md:70-76`; `docs/RELEASE_OPERATIONS.md:36-44`).
- 시험 대조: Python server 시험은 `ConfigStore(tmp_path / "config.json")`를 사용해 정상 PDF·replay·CSV 동작을 검증하지만 JSON의 secret 저장 형식, temp 실패 cleanup, ACL, install/uninstall 보존, secret rotation/revoke를 검사하지 않는다. `test_app.py`는 packaged smoke dispatch만 검사하고 install/uninstall script 시험은 찾지 못했다 (`pc_receiver/tests/test_server.py:37-132`; `test_app.py:6-13`).
- 이력 대조: PC receiver secret/config 저장은 `0b0df69 feat(receiver): add paired encrypted PDF service`에서 도입됐고 CSV/status channel 후속은 `a3aa80c`다. `git log -S 'ConfigStore'`·`secret` 범위에서 DPAPI/Credential Manager, explicit ACL, config migration/rotation/revoke를 추가한 후속 source commit은 확인하지 못했다. 이는 검색 범위의 사실이며 제품 의도를 단정하지 않는다.
- 추론: (1) 현재 ACL에 broad group read가 없더라도 같은 Windows 사용자 권한의 악성 프로세스는 해당 사용자 profile의 config를 읽을 수 있고, 사용자 profile 백업·복제본도 Base64를 되돌릴 수 있다. (2) uninstall 또는 executable 교체 뒤 config를 보존하면 기존 Kiosk pairing이 계속 유효하고, 다른 주체가 config와 receiver executable을 복제할 경우 지정 PC identity/secret을 가장할 조건이 남는다. (3) `config.tmp` 생성 뒤 replace 실패나 프로세스 중단이 발생하면 secret이 임시 파일에 남을 수 있으며, 정상 재시작 시 이를 검사·정리하는 경로가 없다. 실제 악성 프로세스·백업 복제·save failure·소유권 변경·재설치는 수행하지 않았으므로 정적 영향 추론이다.
- 반대 근거와 범위: 현재 broad group read rule이 관찰되지 않았고 config는 LocalAppData 아래에 있으며, `os.replace()`는 정상 저장의 partial-name 노출을 줄이고 Pairing 길이 검증·AES-GCM/HMAC 전송 인증·Private firewall은 네트워크/정상 운영 경계를 보호한다. 이 통제들은 동일 사용자 프로세스/프로필 복제본에 대한 at-rest 기밀성, leftover temp cleanup, uninstall revoke를 보장하지 않는다. `LUNA-0030`의 Kiosk pairing QR/immutable String lifecycle, `LUNA-0034`의 initial host/receiver identity trust, `LUNA-0042`의 startup/shutdown owner와 중복시키지 않고 PC config secret 저장·보존 경계로 유지한다.
- 판정: 기존 `LUNA-0002`를 `P2`·확정·높은 신뢰도로 유지하고 마지막 확인 시각을 이번 주기로 갱신한다. 현재 ACL metadata는 broad public read가 없다는 완화 근거지만 finding을 기각하지 않으며, secret 원문을 읽지 않고도 source의 Base64 at-rest와 uninstall 보존 계약을 독립적으로 확인했다. 새 독립 finding ID는 추가하지 않는다.
- 필요한 후속 검증은 구현하지 않고 큐에 남긴다: (a) Windows DPAPI 또는 Credential Manager로 기존 pairing을 보존하는 migration/rotation/revoke 정책, (b) owner·SYSTEM·Administrators 등 필요한 최소 DACL과 temp file 동일 정책, (c) save write/replace/전원중단 실패 뒤 temp·old/new config 복구, (d) uninstall·PC 교체·소유권 handoff·재설치에서 old pairing revoke 및 PDF 보존 범위, (e) config JSON/ACL/temp/backup에 secret 원문이 남지 않는 회귀시험과 Android Kiosk 재페어링/endpoint recovery 검증.
- 미검증: secret·QR·config JSON 원문·DPAPI 값·전체 ACL identity와 백업본은 읽거나 출력하지 않았다. 실제 PC 소유권 변경·uninstall/reinstall·config save fault·전원중단·악성 process/profile clone·네트워크·receiver 실행·테스트·lint·typecheck·build·install·ADB는 수행하지 않았다. Git stage/commit/push/PR/deploy/rollback도 수행하지 않았고, 이번 반복의 지속 변경은 이 보고서의 `apply_patch`뿐이다.
- 다음 읽기 전용 검토는 `LUNA-0007` PC receiver 인증 전 connection·worker·event queue 자원 상한과 graceful rejection 경계다.

### 2026-08-02 19:19:13

- 자동 재개 계약에 따라 계약 문서를 다른 조사보다 먼저 처음부터 EOF까지 읽었다. 계약 문서는 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`이며, 조사 전에 누적 보고서도 처음부터 EOF까지 읽었다. 보고서 pre-append는 4,703줄·530,071자·SHA-256 `078902078B947EB94605E03075775191386DA69E21B367917139DB8A489D0DCB`였다. Goal은 계속 활성 상태로 두고 지속 변경 대상은 누적 보고서 한 파일로 제한했다.
- Git 기준선은 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`·`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`·`output/`·`tmp/`·계약 문서를 보존했으며, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope에는 이번 주기 변경이 없다. `git diff --check -- kiosk webpoc pc_receiver scripts`는 통과했다. `adb` executable은 PATH에서 찾지 못해 A 상태를 읽지 않았다.
- 이번 주기는 기존 `LUNA-0007`의 단순 “thread 상한 부재”를 반복하지 않고, socket inactivity timeout과 cumulative deadline의 차이, 인증 전 frame materialization, `ReceiverState.lock`의 disk/config serialization, UI event producer/drain 예산과 현재 process/listener 메타데이터를 교차 대조했다. 실제 TCP 연결·부하·receiver 조작은 하지 않았다.
- 사실: `_ReceiverHandler.handle()`는 연결마다 `ThreadingTCPServer` worker에서 socket timeout 10초를 설정한 뒤 magic·header·body를 `_read_exact()`로 읽는다. `_read_exact()`는 각 `recv()` 사이의 `socket.timeout`만 받으며 monotonic 전체 request deadline, header/body read budget, active connection semaphore·worker pool·IP별 rate limit은 없다. 따라서 한 바이트가 매 inactivity timeout 전에 도착하는 partial connection의 전체 수명은 10초로 제한되지 않는다 (`pc_receiver/src/matholic_pdf_receiver/server.py:31-44,213-249`; `:251-257`).
- 사실: protocol은 `request_frame_length()`와 `secure_frame_body_length()`에서 PDF ciphertext 약 5MiB·control payload 약 1MiB의 상한을 계산하지만, handler는 body를 모두 읽고 `frame = header + body`를 만든 뒤 `decode_request()`/`decode_control_request()`의 AES-GCM·timestamp·request validation으로 넘어간다. `_read_exact()`의 `chunks` 결합과 header/body/frame 파생으로 인증 전 connection 하나가 여러 bytes object와 worker를 보유할 수 있으며, payload size bound는 연결당 상한이지 process-wide budget이 아니다 (`pc_receiver/src/matholic_pdf_receiver/protocol.py:26-31,214-234,408-428,514-540`; `server.py:35-44,218-226`).
- 사실: `ThreadedReceiverServer`는 `socketserver.ThreadingTCPServer`를 상속하면서 `allow_reuse_address=True`와 `daemon_threads=True`만 지정한다. source에서 active worker 수·connection admission·`request_queue_size`·per-client quota를 별도로 정의하지 않는다. inherited OS/Python backlog가 유한할 가능성은 있지만, 그것은 application-level active worker/memory budget을 대체하지 않는다 (`server.py:251-257`).
- 사실: 인증 후에도 PDF `ReceiverState.accept()`는 `state.lock` 안에서 최대 5MiB temporary write·`os.replace()`·replay list 저장·`ConfigStore.save()`를 수행하고, control 경로도 `state.lock` 안에서 replay 저장·control response 암호화·status/CSV 상태를 처리한다. 따라서 정상 또는 pairing secret을 가진 client의 disk/config 지연이 다른 PDF·status·CSV 작업과 같은 lock에서 직렬화된다 (`server.py:90-114,130-178`; `config.py:96-115`). 이는 인증 전 worker exhaustion과 별도의 authenticated serialization budget 경계다.
- 사실: `ReceiverApplication.events`는 `queue.Queue()` 기본값인 무제한 queue이고 모든 handler error/status/PDF/CSV event가 `events.put`으로 들어간다. `_poll_events()`는 200ms timer callback마다 `queue.Empty`가 될 때까지 무제한으로 drain하며 queue max size·producer rate limit·status coalescing/drop·한 번의 UI callback drain budget이 없다 (`pc_receiver/src/matholic_pdf_receiver/app.py:38-39,226-247`; `server.py:230-248`).
- 사실: 현재 Windows에서 connection을 만들지 않는 읽기 전용 메타데이터 확인 결과 동일 receiver executable process 2개가 존재했고, TCP 48129 `LISTEN`은 1개이며 `0.0.0.0`에 바인딩된 한 process가 소유했다. 두 process 모두 응답 상태였지만 하나는 listener owner가 아니었다. 이는 duplicate/stale/startup child의 원인을 증명하지 않으며, PID·명령행·파일·process 상태를 변경하거나 종료하지 않았다. 첫 thread-count 요약 명령은 `Measure-Object`의 `ProcessThreadCollection` 타입 오류가 있었고, 같은 상태를 `.Threads.Count`로 재조회해 최종 메타데이터를 얻었다. 파일 변경은 없었다.
- 문서 대조: `pc_receiver/README.md`와 `docs/RELEASE_OPERATIONS.md`는 Private TCP 48129·5MiB PDF·5분 stale request·정상 자동 시작/전송을 기록하지만, partial connection 누적 deadline·active worker/memory budget·event queue 예산·duplicate process 감지/복구 정책을 정의하지 않는다. 2026-07-29 정상 재부팅·TCP LISTEN과 0.1.3 pytest/smoke 기록은 정상 경로의 근거이지 resource exhaustion 경로의 검증은 아니다 (`pc_receiver/README.md:6-15`; `docs/BUILD_VERIFICATION.md:4979-4986`).
- 시험 대조: `test_server.py`는 정상 PDF TCP 왕복과 정상 `shutdown()` fixture, status/CSV one-shot을 검사하고 `test_protocol.py`는 size/auth/timestamp vector를 검사한다. `test_app.py`는 smoke dispatch만 확인한다. 여러 partial socket, cumulative deadline, concurrent worker/thread 수, frame allocation, lock wait, `events` queue length, `_poll_events()` starvation, duplicate process/listener health를 연결한 시험은 찾지 못했다 (`pc_receiver/tests/test_server.py:37-132`; `test_protocol.py`; `test_app.py:6-13`).
- 이력 대조: receiver/프로토콜 도입은 `0b0df69`, CSV/status channel은 `a3aa80c`, packaged smoke는 `d0f7302`·`d225ed6`이다. `git log`와 관련 source history에서 cumulative read deadline·bounded worker/admission·event queue budget·duplicate process watchdog을 추가한 후속 commit은 확인하지 못했다. 이는 검색 범위의 사실이며 제품 의도를 단정하지 않는다.
- 추론: (1) LAN의 비인가 또는 오작동 client가 각 `recv()` 10초 전에 header/body byte를 보내면 연결별 daemon worker·socket·partial buffer가 계속 점유되어 정상 Kiosk 요청이 늦어질 수 있다. (2) 여러 connection이 허용된 frame length까지 body를 읽으면 인증 실패 전에도 process memory가 connection 수에 비례해 증가하고, 인증 실패 event가 무제한 queue에 쌓여 hidden/background UI의 polling 부담이 커질 수 있다. (3) pairing secret을 가진 client가 status/PDF를 빠르게 보내면 global `state.lock`의 disk/config save와 UI event producer가 정상 업무를 직렬화·지연시킬 수 있다. 실제 thread limit·메모리·queue 길이·Kiosk 지연은 측정하지 않았으므로 정적 availability 추론이다.
- 반대 근거와 범위: Private firewall은 다른 네트워크의 접근을 줄이고, AES-GCM/HMAC·timestamp·filename/size validation은 인증·무결성·연결당 payload 범위를 보호한다. per-socket 10초 inactivity timeout도 완전히 멈춘 연결은 회수한다. 정상 Kiosk는 단일 request/response이고 현재 listener는 하나만 관찰되었으며, 정상 server 시험은 explicit shutdown을 수행한다. 이 통제들은 cumulative slow read·process-wide worker/memory budget·unbounded event drain을 보장하지 않는다. `LUNA-0002` secret at-rest, `LUNA-0008` replay eviction, `LUNA-0010` PDF ACK/retry, `LUNA-0033` Kiosk outbound queue와 중복시키지 않고 receiver inbound resource/admission 범위로 유지한다.
- 판정: 기존 `LUNA-0007`을 `P3`·후보·중간 신뢰도로 유지하고 마지막 확인 시각을 이번 주기로 갱신한다. 현재 동일 executable process 2개/단일 listener 관찰은 운영상 추가 확인 항목이지만 duplicate 원인과 실제 exhaustion을 증명하지 않으므로 심각도를 올리거나 새 ID를 만들지 않는다.
- 필요한 후속 검증은 구현하지 않고 큐에 남긴다: (a) header/body cumulative deadline과 per-connection memory/read budget, (b) bounded semaphore/worker pool·admission 및 플랫폼 backlog 정책, (c) 인증 전/후 PDF·control 우선순위와 `state.lock` disk/config serialization 예산, (d) bounded/coalescing event queue와 UI drain budget, (e) partial/slow/flood·concurrent valid status/PDF·queue starvation·duplicate process/listener health 회귀시험.
- 미검증: 실제 TCP 연결·slowloris/flood·인증 실패 frame·worker/thread/memory/queue 계측·UI starvation·PDF/CSV/status payload·ACK·방화벽 동작·duplicate process 원인 확인·process 종료는 수행하지 않았다. 테스트·lint·typecheck·build·install·ADB도 수행하지 않았고, Git stage/commit/push/PR/deploy/rollback도 하지 않았다. 이번 반복의 지속 변경은 이 보고서의 `apply_patch`뿐이다.
- 다음 읽기 전용 검토는 `LUNA-0008` PC receiver replay cache의 timestamp window·eviction·restart/save-failure 경계다.

### 2026-08-02 19:26:27 — `LUNA-0008` replay cache 영속화·실패·재시작 경계 재감사

- 계약·보고서 복원: 자동 재개 직후 계약 문서를 처음부터 EOF까지 읽었다. 계약 문서는 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`였다. 이 사이클 시작 시 누적 보고서는 4,724줄·536,996자·SHA-256 `AE84FC50D3ABF2BA10C23C60767D7D8C0BBF89BF0C8AADF4D4EA4836D24BD440`였고, 마지막 큐가 `LUNA-0008`임을 확인했다.
- 기준선: 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 clean, `git diff --check -- kiosk webpoc pc_receiver scripts` 통과, 기존 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`·`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md` 수정과 `diagnostics/`·`output/`·`tmp/`·계약 문서를 보존했다. `adb` executable은 PATH에 없어 A 상태·설치본·실제 재시작을 확인하지 않았다.
- 이번 범위는 앞서 기록한 2,048개 count-only eviction 수치의 반복이 아니다. `ReceiverConfig.remember_request()`→`ConfigStore.load/save()`→`ReceiverState.accept()/accept_control()`의 side-effect 순서, Kiosk의 request ID/retry semantics, receiver 시험·README·Git history를 읽어 정상 reload와 save/crash failure 경계를 분리했다.

#### 사실

- `MAX_CLOCK_SKEW_SECONDS`는 300초이고, `decode_request()`와 `_decode_secure_frame()`은 replay list를 보기 전에 timestamp 창과 AES-GCM 인증을 검사한다(`pc_receiver/src/matholic_pdf_receiver/protocol.py:29,231-263,390-437`). 따라서 timestamp가 만료된 frame은 cache eviction 여부와 무관하게 거부되는 정상 보호 경계가 있다.
- `ReceiverConfig.replay_ids`에는 request ID의 hex 문자열만 저장되고 timestamp·expiry·처리 결과·파일 destination은 저장되지 않는다. `remember_request()`는 ID를 append한 뒤 2,048개를 넘으면 앞부분을 삭제한다(`pc_receiver/src/matholic_pdf_receiver/config.py:17,47-65`).
- `ConfigStore.load()`는 JSON의 `replay_ids`를 `list(payload.get("replay_ids", []))`로 그대로 받아 오며 항목의 hex 형식·16바이트 길이·중복·최대 개수를 재검증하거나 load 시 trim하지 않는다(`config.py:81-94`). 정상 생성 코드가 만드는 목록은 제한되지만, 손상·구버전·수동 변경 설정의 목록은 별도 경계다.
- `ConfigStore.save()`는 `config.tmp`에 `write_text()`한 뒤 `os.replace()`한다. 정상 호출에서 이름 교체는 원자적이지만, `flush`/`fsync` 또는 `config.tmp`를 보상 삭제하는 `finally`는 없다(`config.py:96-115`). 이는 `LUNA-0002`에서 기록한 secret temp artifact 경계와도 겹친다.
- PDF `ReceiverState.accept()`는 lock 안에서 PDF `.part`를 쓰고 `os.replace(temporary, destination)`로 최종 파일을 먼저 만든 다음 `remember_request()`와 `store.save()`를 호출한다(`pc_receiver/src/matholic_pdf_receiver/server.py:90-114`). 즉 파일 commit과 replay/config persistence가 한 원자 transaction이 아니다.
- PDF `store.save()`가 예외를 내면 `accept()`의 보상 블록은 PDF `temporary`와 `destination`을 unlink하지만, 이미 append된 `self.config.replay_ids`를 이전 목록으로 되돌리지 않는다. `ConfigStore.save()`가 남긴 `config.tmp`도 이 보상 블록의 대상이 아니다(`server.py:98-108`, `config.py:96-115`).
- control 경로는 decode 후 lock 안에서 replay ID를 확인하고 `remember_request()`·`store.save()`를 먼저 수행한 뒤 `CONTROL_STATUS` payload 해석, `CONTROL_FETCH_CSV` 분기와 unsupported operation 처리를 한다(`server.py:130-178`). 인증된 malformed status, unsupported operation 또는 operation-specific failure는 업무 효과·응답 전달 전에 ID를 저장하고 cache를 churn할 수 있다.
- 같은 process 안에서는 PDF/control replay 검사·기록·저장이 `ReceiverState.lock`으로 직렬화된다. 그러나 process가 재시작되면 `ConfigStore.load()`가 마지막으로 성공한 JSON만 읽으며, lock은 이전 process의 side effect 순서를 복구하지 않는다(`server.py:81-178`, `config.py:71-94`).
- Kiosk `PcTransferProtocol.encodeRequest()`와 `PcControlProtocol.encodeRequest()`는 기본적으로 호출마다 random request ID를 만들고, `PcPdfSender`·`PcControlClient`는 response binding을 검사한다. `withReachablePairedPc()`의 endpoint 재시도도 새 pairing과 새 request를 사용한다(`kiosk/.../PcTransferProtocol.kt:31-88`, `PcControlProtocol.kt:39-73`, `MainActivity.kt:807-837`). 이는 accidental same-ID retry를 줄이지만 receiver의 crash window를 보완하지는 않는다.
- 기존 Python 시험은 정상 PDF 한 번 저장과 같은 frame 즉시 replay 거부, 정상 status/CSV one-shot을 확인한다(`pc_receiver/tests/test_server.py:37-132`). protocol 시험은 `now + 301`초 stale 거부와 인증 vector를 확인하지만, ConfigStore reload·save exception·process interruption·cache overflow·malformed/unsupported control 소비 순서는 검증하지 않는다(`test_protocol.py:42-174`).
- README와 release 검증 문서는 “5분 밖 요청”과 “처리한 request ID 재전송 거부”를 선언하지만, 2,048개 용량·ID별 만료·파일 commit과 replay 저장의 transaction 관계·crash recovery를 정의하지 않는다(`pc_receiver/README.md:7-15`, `docs/BUILD_VERIFICATION.md:4900-4915`). 검색한 관련 history는 receiver 도입 `0b0df69`와 status/CSV 확장 `a3aa80c`가 replay 저장 경계를 만든 시점이며, 이후 expiry/durable recovery 보강 commit은 확인하지 못했다.

#### 추론

- process가 PDF `os.replace()` 직후 `remember_request()`/`ConfigStore.save()` 완료 전에 종료되거나 전원 손실이 나면, 최종 PDF는 남지만 JSON의 replay ID는 이전 상태일 수 있다. 재시작 후 300초 안에 동일 captured authenticated frame이 다시 도착하면 replay guard가 그 ID를 찾지 못해 새 unique destination에 중복 저장할 수 있다. 이는 `LUNA-0010`의 ACK 유실 뒤 새 request ID 재시도와 달리, 같은 request ID의 persistence/crash window다.
- `store.save()`가 PDF commit 뒤 실패하면 같은 process의 메모리 목록에는 ID가 남고 PDF는 보상 삭제될 수 있어, 사용자가 동일 frame을 재시도할 때 “파일은 없지만 replay로 거부”되는 불일치가 생길 수 있다. 재시작하면 JSON이 이전 목록으로 남았을 경우 같은 frame이 다시 허용될 수 있다. save exception·파일 삭제 실패·재시작 조합은 실행하지 않았다.
- control ID 선기록은 정상 Kiosk payload에서는 대체로 문제를 드러내지 않지만, pairing secret을 가진 오작동 client가 인증된 invalid/unsupported control을 반복하면 실제 operation 없이 2,048개 cache slot을 소모할 수 있다. 이후 정상 captured frame의 eviction 가능성은 기존 count-based `LUNA-0008` 가설을 보강하지만, 별도 finding으로 분리할 만큼의 처리량·운영 영향은 측정하지 않았다.
- 손상된 `replay_ids`가 매우 크거나 문자열·잘못된 항목이면 startup 예외 또는 membership 검사 비용 증가가 가능하지만, 이는 로컬 config 손상·변조 전제의 robustness 문제다. 원격 replay acceptance와 합치지 않고 참고 경계로만 기록한다.

#### 기대 결과·반대 가설·판정

- 기대 결과는 (a) timestamp 창 안의 동일 ID가 정상 처리 뒤에는 항상 거부되고, (b) 파일 commit·replay marker·재시작 복구가 하나의 일관된 결과를 가지며, (c) 업무 효과가 없는 control failure가 replay capacity를 임의로 소비하지 않는 것이다.
- AES-GCM 인증·300초 timestamp 검사·random ID·lock·정상 `os.replace()`·PDF save exception의 파일 삭제 보상·Kiosk strict response binding은 위험을 낮추는 반대 근거다. secret 없는 네트워크 공격자가 새 유효 frame을 만들어 cache를 채울 수 있다고 주장하지 않는다.
- 정상 저장과 정상 reload는 static source상 replay list를 보존한다. 다만 `os.replace()`의 이름 원자성은 전원 손실까지의 durability 보장이 아니고, lock은 process 재시작을 넘지 않으며, 현재 시험은 해당 fault window를 재현하지 않는다.
- 판정: 기존 `LUNA-0008`을 `P4`·후보·중간 신뢰도로 유지하고 마지막 확인 시각을 2026-08-02 19:26:27로 갱신한다. crash/save-failure 세부 근거를 보강했지만 `LUNA-0010`의 새 ID delivery retry와 중복되지 않으므로 새 finding ID는 추가하지 않는다.

#### 후속 검토 큐와 미검증

- 수정 판단은 하지 않고 다음 검증 요구만 큐에 남긴다: (a) replay marker와 PDF artifact의 durable/idempotent commit·재시작 reconciliation 정책, (b) `config.tmp` cleanup·flush/durable replace 및 secret artifact 경계(`LUNA-0002` 연계), (c) timestamp 기반 expiry와 capacity 산정, (d) load 시 replay list schema validation·dedupe·cap, (e) control operation 검증과 replay 기록의 순서 정책, (f) 2,049개 overflow·동시 same-frame·save exception·process crash·reload·wall-clock 경계 회귀시험.
- 실제 PDF·CSV·학생정보·pairing secret·config payload는 읽거나 사용하지 않았고, TCP 연결·대량 frame·파일/설정 write failure·process 강제 종료·재시작·전원 장애·clock 변경·ADB·테스트·lint·typecheck·build·install은 수행하지 않았다. Git stage/commit/push/PR/deploy/rollback도 없다.
- 다음 읽기 전용 검토는 `LUNA-0010` PDF commit·ACK 유실·endpoint 재시도·`markCardsDelivered()` 상태 binding 경계다.

### 2026-08-02 19:32:42 — `LUNA-0010` PDF delivery 결과 확정·재시도·QR 상태 binding 재감사

- 계약·보고서 복원: 자동 재개 직후 계약 문서를 처음부터 EOF까지 읽었다. 계약 문서는 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`였다. 이 사이클 시작 시 누적 보고서는 4,765줄·544,185자·SHA-256 `32FC15C6529CB90B5B7835CBF8ECA7D48389F39BAFCE5B1679065DCDC92A7EAB`였고, 마지막 큐가 `LUNA-0010`임을 확인했다.
- 기준선: 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope는 clean, `git diff --check -- kiosk webpoc pc_receiver scripts` 통과, 기존 사용자 수정·미추적 `diagnostics/`·`output/`·`tmp/`·계약 문서를 보존했다. `adb` executable은 PATH에 없어 A 상태와 설치본의 실제 PDF/프린터 결과를 확인하지 않았다.
- 이번 범위는 기존의 일반적인 “ACK 유실이면 중복될 수 있음”을 반복하지 않고, 단건과 batch의 재시도 경로 차이, endpoint resolver가 operation 예외를 분류하지 않는 경계, `studentId`만 사용하는 delivered update, Activity 종료 시 실행 중 task의 소유권을 source·호출 경로·테스트·운영 문서·history로 교차 대조했다.

#### 사실

- `PcPdfSender.send()`는 PDF를 읽어 새 `PcTransferProtocol.encodeRequest()` frame/request ID를 만들고, socket write 뒤 정확히 89바이트 ACK를 읽어 request ID·PDF hash·HMAC을 검증해야 성공한다. read/verify 예외는 호출자에게 전달되며, 정상 성공 뒤에만 ACK 배열을 지운다(`kiosk/.../PcPdfSender.kt:7-53`, `PcTransferProtocol.kt:31-126`).
- PC receiver는 PDF temporary를 `os.replace()`로 최종 파일에 commit하고 replay/config를 저장한 뒤 handler가 `sendall(ack)`한다(`pc_receiver/src/matholic_pdf_receiver/server.py:90-114,213-249`). 따라서 Kiosk가 ACK를 읽기 전에 원격 파일 side effect가 끝나는 구조다. 같은 frame은 replay ID로 막지만 새 request ID에는 PDF hash·card delivery ID 조건이 없다(`config.py:58-65`, `server.py:95`).
- 단건 `preparePcPdfTransfer()`만 `withReachablePairedPc { pcPdfSender.send(...) }`를 사용한다. wrapper는 operation의 모든 `Exception`을 첫 endpoint 실패로 취급하고, 원래 host를 제외한 private 후보를 최대 254개까지 최대 32개 병렬 probe한 뒤 6초 내 인증된 후보를 찾으면 recovered pairing을 먼저 `pcPairingStore.save()`하고 operation을 새 request ID로 다시 실행한다(`MainActivity.kt:807-839`, `PcEndpointResolver.kt:79-145`, `MainActivity.kt:2350-2367`).
- endpoint probe 자체는 `PcControlClient.sendStatus()`를 사용한 authenticated status 요청이고, resolver는 original host를 후보에서 제외한다. 따라서 첫 PDF가 commit된 뒤 ACK만 사라진 경우와 commit 전 연결 실패를 source상 구분하지 않으며, DHCP로 주소가 바뀌어 새 후보가 인증되는 경우에만 자동 PDF 재호출 경로가 열린다(`PcEndpointResolver.kt:90-116`, `MainActivity.kt:807-837`).
- batch `preparePendingCardsPdf()`는 pairing을 한 번 load한 뒤 `reissueQrBatch()`→Bitmap/PDF 생성→`pcPdfSender.send()`→`markCardsDelivered(studentIds)`를 직접 순서대로 수행하며 `withReachablePairedPc()`를 사용하지 않는다(`MainActivity.kt:1934-1965`). 전송 실패 callback은 `pendingCardsPdfButton`을 다시 활성화하고 “기존 QR이 이미 무효화되었을 수 있음”을 표시한다(`MainActivity.kt:1967-1985`). 재실행은 이전 batch 결과를 조회하지 않고 다시 QR을 발급한다.
- 단건 `QrPreview`는 `studentId`, 표시 이름과 Bitmap만 보유하고 QR hash, issued timestamp, delivery ID를 보유하지 않는다(`MainActivity.kt:3955-3959`). 단건은 PDF send 성공 뒤 `studentRepository.markCardsDelivered(setOf(preview.studentId))`를 실행하고, 실패 시 같은 `issuedQrPreview` identity가 유지되는 경우 send 버튼을 다시 활성화한다(`MainActivity.kt:2333-2384`).
- `QrCardStatusEntity`의 primary key는 `studentId`이고 `issuedAtEpochMs`·`lastDeliveredAtEpochMs`·`needsPrint`만 있다. `markDelivered()` SQL은 `studentId IN (:studentIds)`만 조건으로 `lastDeliveredAtEpochMs`와 `needsPrint`를 갱신하며 issued timestamp/hash/revision/request ID를 비교하지 않는다(`kiosk/.../Entities.kt:38-45`, `Daos.kt:109-116`, `StudentRepository.kt:476-484`). `reissueQr()`와 `reissueQrBatch()`는 같은 row를 새 QR hash·새 issued time·`needsPrint=true`로 덮어쓴다(`StudentRepository.kt:160-180,236-280`).
- Activity의 정상 DB 작업은 single-thread `ioExecutor`에 제출되지만 PC PDF send는 그 worker 안에서 네트워크 대기를 수행한다. `onDestroy()`는 `ioExecutor.shutdownNow()`가 반환한 대기 `SensitiveTask`만 discard하고, 이미 실행 중인 task를 join하거나 socket/operation generation으로 취소하지 않는다(`MainActivity.kt:161,2346-2369,3859-3874`). 실행 중 task는 `destroyed` 검사 없이 export/send/mark를 계속 수행할 수 있고, UI callback만 `destroyed`에서 반환한다.
- batch와 student mutation의 공통 gate/revision 부재는 이미 `LUNA-0036`·`LUNA-0037`에서 별도 기록했다. 이번 경로에서는 그 교차를 새 finding으로 만들지 않고, delivery 결과가 확정되지 않은 상태에서 retry/mark semantics가 어떻게 동작하는지만 `LUNA-0010`에 포함한다.
- `PcTransferProtocolTest`는 Python vector·정상 ACK·tampered ACK를 확인하고, `PcEndpointResolverTest`는 후보·authenticated probe 선택·fail-closed를 확인한다. `RepositoryInstrumentedTest`는 직접 `markCardsDelivered()`와 정상 batch 상태만 확인한다. ACK read/sendall failure 뒤 file count/hash, resolver operation 재호출, QR reissue 후 old task의 mark, Activity destroy 중 실행 지속을 연결한 시험은 찾지 못했다(`kiosk/src/test/.../PcTransferProtocolTest.kt`, `PcEndpointResolverTest.kt`, `kiosk/src/androidTest/.../RepositoryInstrumentedTest.kt:489-544`).
- 제품 결정은 사용자용 취소·재시도 버튼을 만들지 않고 안전한 자동 재시도의 단계·횟수·timeout·실패 코드만 기록한다고 한다(`docs/PRODUCT_DECISIONS.md:40-58`). 반면 release 운영은 전송만 다시 해야 할 때 현재 카드 전송 기능을 다시 사용하라고 안내하고, batch 실패 UI는 다시 실행 가능한 버튼을 남긴다(`docs/RELEASE_OPERATIONS.md:225-245`, `MainActivity.kt:1975-1985`). 불확실한 결과의 재전송 정책과 단순 연결 실패의 재전송 정책은 문서상 분리돼 있지 않다.
- Git history에서 기본 PDF 전송은 `474132b`, DHCP endpoint recovery는 `ca32740`에서 도입됐다. 관련 history·현재 tests에서 stable delivery ID/hash idempotency, receipt query, ACK uncertainty classification을 추가한 후속 commit은 확인하지 못했다.

#### 추론

- 첫 단건 send가 receiver의 파일 commit 뒤 ACK read/verify에서 실패하고, PC가 DHCP로 새 주소를 얻어 resolver가 그 수신기를 인증하면 `withReachablePairedPc()`는 실패 원인을 구분하지 못한 채 새 request ID로 같은 PDF를 다시 저장시킬 수 있다. 첫 실패가 commit 전 연결 실패라면 재전송은 정상 복구일 수 있으므로 모든 자동 retry가 중복이라고 주장하지 않는다.
- 단건 receiver commit 뒤 ACK가 유실되어 resolver가 새 endpoint를 찾지 못하면 자동 재전송은 끝나지만 UI는 실패를 표시하고 같은 preview의 send 버튼을 다시 열 수 있다. 사용자가 다시 누르면 새 request ID·새 unique destination으로 동일 QR PDF가 추가될 수 있다.
- batch는 첫 PDF가 commit됐지만 ACK 또는 `markCardsDelivered()`가 실패하면 첫 PDF가 남은 상태에서 재시도 시 `reissueQrBatch()`가 더 새로운 QR을 발급한다. 첫 파일은 현재 DB QR과 다른, 이미 무효화된 QR을 담은 stale artifact가 되고 두 번째 파일은 최신 QR이 된다. 운영자가 어느 파일을 인쇄할지 source/UI가 식별하지 않는다.
- 단건 또는 batch 전송 task가 old Activity에서 실행 중인 동안 새 Activity/작업이 같은 학생의 QR을 재발급하면, 이후 `markCardsDelivered(studentId)`가 “전송한 QR”이 아니라 현재 row의 최신 QR을 delivered로 표시할 수 있다. 같은 Activity single executor가 정상 순서를 제한하지만 `onDestroy()`가 running task를 회수하지 않고 row 조건에 revision이 없어 lifecycle 경계는 남는다. 이 조건은 `LUNA-0037`과 인접하지만, 여기서는 ACK/결과 확정 이후 DB mark의 의미를 다룬다.
- resolver는 recovered pairing을 PDF 재전송 성공 전에 저장하므로, 두 번째 operation도 실패하면 실제 delivery는 미확정인데 Kiosk의 endpoint만 새 주소로 바뀔 수 있다. 다음 정상 status/PDF가 새 pairing을 사용한다는 운영상 side effect가 남지만, 이것만으로 새 독립 finding을 만들지는 않는다.
- `markCardsDelivered()`의 transaction과 row-count check는 없는 학생을 조용히 delivered로 표시하는 오류를 막지만, 존재하는 row가 다른 QR revision인지 확인하지 못한다. 따라서 DB 원자성은 delivery identity binding의 대체 근거가 아니다.

#### 기대 결과·반대 가설·판정

- 기대 결과는 (a) “수신 결과 미확정”과 “commit 전 실패”를 구분해 자동 재시도하며, (b) 동일 카드의 server artifact가 delivery identity 기준으로 한 건만 존재하고, (c) 후속 DB mark가 전송된 QR revision/hash에만 적용되며, (d) Activity 종료 시 pending/running delivery의 결과 확정 정책이 명시되는 것이다.
- AES-GCM/HMAC·request ID replay·ACK의 request ID/hash 검증·private authenticated endpoint probe·receiver의 commit-before-ACK·같은 Activity single-thread executor·batch 실패 경고는 위험을 낮추는 반대 근거다. 정상 ACK와 정상 DB transaction에서는 이 후보가 드러나지 않는다.
- 현재 근거는 source 경로와 정상/negative unit·instrumented 시험의 범위 비교이며, 실제 PC 폴더 중복·ACK 유실·DB failure·Activity recreation을 직접 증명하지 않는다. 심각도를 P2로 올릴 근거는 없고, 기존 `LUNA-0010`을 `P3`·후보·중간 신뢰도로 유지한다.

#### 후속 검토 큐와 미검증

- 수정 판단은 하지 않고 다음을 큐에 남긴다: stable delivery ID 또는 QR revision+PDF hash 기반 server idempotency/receipt query, commit/ACK uncertainty 재조회 정책, receiver retry 전 duplicate reconciliation, `markCardsDelivered()`의 conditional revision/hash update, batch에도 동일한 result recovery, recovered pairing 저장 시점 정책, running task의 cancellation/generation/join 및 stale callback 보상.
- 회귀시험은 정상 단건·batch, commit 전 connect failure, commit 후 ACK timeout/malformed ACK, DHCP endpoint recovery, 후속 Room update failure, 사용자 재시도, batch 재실행, QR reissue와 old task interleave, Activity destroy/recreate, 중복 filename을 각각 분리해 PC artifact 수·현재 QR revision·`needsPrint`/`lastDeliveredAt`를 함께 확인해야 한다.
- 실제 네트워크·ACK 차단·PDF·PC 파일·학생/QR 데이터·프린터·Activity 재생성·DB fault·프로세스 종료·ADB·테스트·lint·typecheck·build·install은 수행하지 않았다. Git stage/commit/push/PR/deploy/rollback도 없다.
- 다음 읽기 전용 검토는 `LUNA-0011` audit/진단 로그 retention·cleanup 경계다.

### 2026-08-02 19:44:47 — `LUNA-0012` 원격 점검 badge/header geometry·검증 공백 심화 재감사

- 계약·보고서 복원: 자동 재개 직후 운영 계약 문서를 처음부터 EOF까지 다시 읽었다. 계약 문서는 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`였다. 이번 cycle 전 누적 보고서는 4,851줄·560,653자·SHA-256 `37BF92F32F38D5DBE6BBB9C566426015982EAECBD4661F74540545F680B161E1`였고, 마지막 큐가 `LUNA-0012`임을 확인했다. Goal은 계속 활성 상태로 유지하고 지속 변경 대상은 누적 보고서 한 파일로 제한했다.
- 기준선: 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`·`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`·`output/`·`tmp/`·계약 문서를 보존했고, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope에는 이번 cycle 전 변경이 없었다. `adb` executable은 PATH에서 찾지 못했다.
- 이번 범위는 기존 A 화면의 단순 겹침 재기록이 아니다. Kiosk badge의 parent/Z-order·header layout contract·상태 문자열 길이·scanner 전환, 원격 점검 관련 UI/정책 시험의 실제 범위, 도입 commit 이후 geometry 변경 여부를 교차 대조했다.

#### 사실

- `RemoteSupportWindowController.showBadge()`는 `android.R.id.content`에 `TextView`를 `FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.TOP or Gravity.END)`로 추가하고 `topMargin=8dp`, `marginEnd=8dp`, `elevation=12dp`를 설정한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/RemoteSupportWindowController.kt:54-92`). header 내부 child나 measured bounds를 참조하거나 badge 공간을 예약하지 않는다.
- `activity_main.xml`의 root content는 60dp 높이 `app_header`를 포함하며, 헤더 우측에 `device_mode_text`와 `status_text`를 horizontal `LinearLayout`으로 배치한다. 두 상태 TextView는 `wrap_content`이고 `status_text`에는 `maxLines`·`ellipsize`·고정 폭이 없다(`kiosk/src/main/res/layout/activity_main.xml:2-48`).
- `MainActivity`는 `setContentView()`와 `bindViews()` 뒤 `remoteSupportWindowController.start()`를 호출하므로 active preference가 있으면 badge가 이미 구성된 root에 마지막 child로 추가된다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:337-370`). `refresh()`는 만료 전까지 badge를 유지하고, `showBadge()`에는 header visibility/size에 따른 재배치 분기가 없다.
- 상태 텍스트에는 `CAMERA_PERMISSION_REQUIRED`, `INITIALIZATION_FAILED`, `ADMIN_LOADING`, `PC_PAIRING`, `MANUAL_STUDENT_SELECTION` 및 enum state 값이 대입된다(`MainActivity.kt:221,288,300,604,624,647,779,2884,2970,3051-3058,3134,3174,3363,3427,3521-3578`). 모든 문자열이 관리자 header에서 동시에 보인다는 뜻은 아니지만, geometry가 문자열 길이를 고려하지 않는 정적 조건이다.
- `showScanner()`와 `startPcPairingScanner()`는 `appHeader.visibility = View.GONE`으로 바꾸지만 controller를 stop하거나 badge를 숨기지 않는다(`MainActivity.kt:2962-2970,3041-3058`). scanner layout의 별도 조작부는 `bottom|end`에 배치되어 있어, 현재 정적 자료만으로 scanner button과 badge가 실제로 겹친다고 확정하지 않는다.
- `Gate5ManifestInstrumentedTest.remoteSupportReceiverIsAdbOnlyAndControlIsVisibleInsideAdminPanel()`은 receiver exported/permission과 remote button의 ScrollView ancestor만 확인한다. `RemoteSupportPolicyTest`는 duration clamp와 expiry/boot policy만 확인한다. badge를 활성화하고 실제 View bounds·Z-order·font scale·rotation을 비교하는 시험은 찾지 못했다(`kiosk/src/androidTest/java/com/local/matholickiosk/kiosk/Gate5ManifestInstrumentedTest.kt:80-101`; `kiosk/src/test/java/com/local/matholickiosk/kiosk/RemoteSupportPolicyTest.kt:8-52`).
- `f8bd710`은 controller와 badge geometry를 처음 도입한 commit이며, `git log --follow` 및 `git log -S 'Gravity.TOP or Gravity.END'`에서 그 이후 Kiosk controller의 geometry 수정 commit은 확인되지 않았다. README는 점검 중 우측 상단 badge를 요구하지만 non-overlap·reserved header region·bounds 기준은 정의하지 않는다(`README.md:103-118`; `f8bd710`).
- 과거 승인된 A 단기 화면 확인은 2000×1200 관리자 화면에서 badge가 header 우측 상태 표시 일부를 가린 것을 직접 기록한다. 이번 cycle은 ADB unavailable 상태라 새 화면·bounds·density를 읽지 않았으며, 과거 화면 증거를 현행 source 변경 부재로 정적 재대조했다.

#### 추론

- active remote support 중 Kiosk 관리자에게 `QR_READY`·`ADMIN_LOADING`·초기화 실패 등 상태가 부분적으로 가려지면, 점검이 끝나지 않은 상태에서 복귀·수업·카드 조작 판단이 늦어질 수 있다. 이는 기능·권한 차단보다 제한적인 운영 UI 문제이므로 P3이 적절하다.
- `wrap_content` 상태와 고정 overlay는 좁은 width·큰 font scale에서 충돌 범위를 키울 수 있지만, 실제 Android measure 결과를 실행하지 않았으므로 현재 확정 finding의 영향 확대가 아닌 조건부 추론으로 남긴다.
- scanner에서 badge가 남는 것은 원격 점검 상태를 계속 표시하는 의도와도 일치할 수 있고, bottom|end 조작부와의 정적 위치는 분리돼 있다. 따라서 scanner overlap이나 Web 동일 현상을 새 확정 finding으로 만들지 않는다.

#### 기대 결과·반대 가설·판정

- 기대 결과는 active badge, `device_mode_text`, `status_text`, 각 상태 문자열과 scanner 전환이 같은 화면 폭·font scale에서 서로 가리지 않고 모두 읽히는 것이다.
- badge의 명확한 원격 상태 표시, elevation으로 인한 가시성, `FLAG_SECURE` 만료 복원, DUMP 권한, same-boot/max-duration 정책은 기능·보안 측면의 반대 근거다. README가 우측 상단 badge를 명시한 것은 표시 자체의 요구사항을 뒷받침하지만 겹침 허용을 뜻하지 않는다.
- 기존 A 화면 직접 증거는 현재 source·layout·controller history가 바뀌지 않았다는 정적 근거로 유지된다. ADB unavailable로 현행 설치본의 재현·bounds·font scale·터치 hit-test는 미검증이다.
- 판정: 기존 `LUNA-0012`를 `P3`·`확정`·높은 신뢰도로 유지한다. Kiosk header overlap에 status-string/geometry test gap을 보강했으며, Web·scanner overlap은 반증되지 않은 범위로 분리한다. 새 독립 ID는 추가하지 않는다.

#### 후속 검토 큐와 미검증

- 수정은 하지 않고 큐에 남긴다: badge를 header 내부 reserved region 또는 collision-aware overlay로 배치할지 사용자/Sol이 결정하고, Kiosk admin/auth/scanner·Web active navigation에서 실제 bounds/가시성/접근성 focus를 같은 해상도·density·font scale별로 확인한다. 활성 preference를 주입한 UI 회귀시험에서 status strings, Z-order, rotation·narrow width를 검증해야 한다.
- 새 A 화면·ADB·실제 설치본·screen capture·font scale·rotation·TalkBack·touch hit-test·빌드·테스트·lint·typecheck·install은 수행하지 않았다. 소스·layout·문서·설정은 수정하지 않았고, Git stage/commit/push/PR/deploy/rollback도 하지 않았다.
- 다음 읽기 전용 검토는 `LUNA-0013` CSV parser immutable String과 credential buffer lifecycle 경계다.

### 2026-08-02 19:39:06 — `LUNA-0011` audit DB·private diagnostic dump lifecycle 심화 재감사

- 계약·보고서 복원: 자동 재개 직후 운영 계약 문서를 처음부터 EOF까지 다시 읽었다. 계약 문서는 358줄·9,264자·SHA-256 `56DD3247C79A4DAFD10C580776DB7F76CC2419FEA8F011D0196183C7DB9BADF7`였다. 이번 조사 전 누적 보고서는 4,809줄·552,668자·SHA-256 `83B70C6BBBAC0C13A0B55A36A87DAD87D7ED1968B46C0DFA7E5FCEAE43B6EEDC`였고, 마지막 큐가 `LUNA-0011`임을 확인했다. Goal은 계속 활성 상태로 유지하고, 지속 변경 대상은 이 누적 보고서 한 파일로 제한했다.
- 기준선: 브랜치 `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d` 동일이다. 기존 사용자 수정 `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md`·`docs/RC47_RC68_MINIMAL_FIELD_SCENARIO.md`와 미추적 `diagnostics/`·`output/`·`tmp/`·계약 문서를 보존했고, `kiosk`·`webpoc`·`pc_receiver`·`scripts` source scope에는 이번 cycle 전 변경이 없었다. `git diff --check -- kiosk webpoc pc_receiver scripts`는 통과했고, `adb` executable은 PATH에서 찾지 못했다.
- 이번 범위는 `AuditDao.deleteOlderThan()`와 256KB logger 회전을 다시 적는 데 그치지 않고, Room database builder의 maintenance 연결, Android backup/data-transfer 제외, ADB dump의 실제 입력 bound와 기록 회전 동기화, 관련 시험의 fault/concurrency coverage를 분리 대조했다.

#### 사실

- `AuditEventEntity`는 event type, nullable reason code, 내부 student/session ID, app version, epoch timestamp를 `audit_events`에 저장하고 timestamp/event type index만 가진다. 행 수·기간·바이트 상한이나 retention class는 없다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/Entities.kt:117-130`).
- `AuditDao`에는 `insert`, `latest`, `deleteOlderThan`가 있지만 `rg` 범위에서 `deleteOlderThan`의 production call site는 DAO 선언 하나뿐이었다. `KioskDatabase`는 migration 두 개만 builder에 추가하고 callback/periodic maintenance를 연결하지 않는다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/data/Daos.kt:180-188`; `KioskDatabase.kt:38-47`).
- Kiosk/Web는 `allowBackup=false`, `fullBackupContent=false`를 선언하며, cloud backup과 device transfer 모두 `root`, `file`, `database`, `sharedpref`, `external`을 exclude한다(`kiosk/src/main/AndroidManifest.xml:24-29`, `kiosk/src/main/res/xml/data_extraction_rules.xml:2-17`; Web 대응 파일도 동일). 이는 backup/device-transfer 경계의 긍정 통제이지만, active app process와 local storage에 쌓이는 audit row를 정리하지 않는다.
- Kiosk `PrivateDiagnosticLog.record()`와 Web `PrivateDiagnosticLog.event()`는 현재/previous 두 파일을 nominal 256KB 기준으로 회전한다. 두 구현 모두 `delete()`·`renameTo()` 반환값을 검사하지 않고 전체 쓰기 블록을 `runCatching`으로 감싼다. Kiosk 생성자의 `mkdirs()` 결과도 별도 오류 상태로 보고하지 않는다(`kiosk/.../PrivateDiagnosticLog.kt:8-38`; `webpoc/.../PrivateDiagnosticLog.kt:8-29`).
- Kiosk `record()`는 `synchronized(lock)` 안에서 기록하지만 `dumpForAdb()`는 그 lock을 사용하지 않는다. Web `event()`는 `@Synchronized`지만 `dumpForAdb()`에는 동일한 동기화가 없다. 따라서 dump가 append/rotate와 동시에 실행될 수 있다. Kiosk ADB receiver는 MainActivity가 보유한 logger와 별도로 `PrivateDiagnosticLog(context)`를 생성해 dump한다(`kiosk/src/main/java/com/local/matholickiosk/kiosk/MainActivity.kt:365`; `AdbDiagnosticDumpReceiver.kt:8-12`).
- 두 dump 구현은 파일별 `readLines(Charsets.UTF_8)`를 먼저 완료한 뒤 `.takeLast(200)`과 safe-line filter를 적용한다. 즉 출력은 최근 200줄로 제한되지만 입력 read와 임시 리스트의 크기는 파일 크기로부터 독립적이지 않다(`kiosk/.../PrivateDiagnosticLog.kt:41-53`; `webpoc/.../PrivateDiagnosticLog.kt:31-44`).
- 두 dump의 파일 read 예외와 logger의 write/rotate 예외는 `runCatching`으로 외부에 반환되지 않는다. nonce가 유효하면 `BEGIN`·`END`가 기록되므로, 파일이 비어 있는 경우와 read failure를 ADB 소비자가 구조적으로 구분할 success/error 상태는 없다.
- 관련 JVM 시험은 허용 nonce와 구조적 line filter만 검사한다(`kiosk/src/test/.../PrivateDiagnosticLogTest.kt:7-23`; `webpoc/src/test/.../DiagnosticEventPolicyTest.kt:51-67`). audit 계측시험은 latest row와 특정 event 내용을 검사하지만, cutoff cleanup·DB 누적·logger rotation fault·oversize dump·동시 dump/record는 검사하지 않는다.

#### 추론

- audit가 별도 영구 감사 정책이 아니라 내부 진단 정책의 대상이라면, Android backup 제외는 외부 복제 위험만 낮출 뿐 local DB의 무기한 row 증가와 storage pressure를 해결하지 않는다. QR reject/session/student 관리가 반복되는 장기 무인 운전에서 DB index·transaction 비용과 내부 ID 보존 기간이 계속 증가할 수 있다.
- logger 회전이 실패해 current 파일이 nominal cap를 넘으면 dump는 최근 200줄만 내보내더라도 먼저 oversize 파일 전체를 메모리에 읽는다. ADB dump 자체가 UI 업무와 별도 process가 아니므로, 이 조건에서는 진단 수집이 일시적으로 memory/latency 부담을 만들 가능성이 있다. 실제 파일 성장·heap 영향은 측정하지 않았다.
- append/rotate와 dump가 겹치면 이전 파일 교체 직전/직후의 서로 다른 세대를 섞어 읽거나 한 파일 read를 예외로 건너뛸 수 있다. `END`만 남는 계약은 “정상적으로 최근 200줄이 없음”과 “수집 중 read 실패”를 구별하지 못해 장애 조사 품질을 낮춘다. Kiosk의 별도 logger 인스턴스는 per-instance lock으로도 MainActivity 기록과 dump를 조정하지 못한다.
- 학생 이름·외부 로그인 ID·비밀번호·QR 원문·답안·점수·문항 내용 직접 저장은 이번 정적 범위에서 확인하지 않았으므로 개인정보 직접 노출로 확대하지 않는다. 내부 UUID/session ID의 retention과 diagnostic availability 후보로만 유지한다.

#### 기대 결과·반대 가설·판정

- 기대 결과는 (a) audit의 목적별 row/기간/용량 정책과 실제 startup/idle maintenance가 연결되고, (b) diagnostic log의 보존·회전 실패가 관찰 가능하며, (c) dump 입력 자체도 bounded tail/snapshot semantics를 가지며, (d) concurrent record/dump와 fault를 회귀시험하는 것이다.
- `allowBackup=false`·전면 data extraction exclusion, private storage, `android.permission.DUMP`, nonce/line filter, 정상 2세대 회전은 외부 노출과 정상 경로의 영향을 줄이는 반대 근거다. `AuditDao.deleteOlderThan`가 존재하는 점도 향후 구현 여지는 보여주지만 production lifecycle 호출의 증거는 아니다.
- 이번 cycle은 DB/log 파일, ADB, Android runtime, fault injection, heap, 장기 운전과 네트워크를 사용하지 않았다. 따라서 실제 storage exhaustion·파일 손상·로그 누출·덤프 누락을 직접 확인했다고 보고하지 않는다.
- 판정: 기존 `LUNA-0011`을 `P4`·후보·중간 신뢰도로 유지하고, audit cleanup 미연결에 더해 dump input bound·동기화·failure observability 경계를 구체화한다. 기존 finding과 같은 retention/lifecycle 원인 계열이며 새 독립 ID는 추가하지 않는다.

#### 후속 검토 큐와 미검증

- 수정은 하지 않고 큐에 남긴다: audit와 diagnostic log의 목적별 retention 분리, row/bytes cap과 cleanup 시점, Room callback/idle maintenance의 실패 정책, rotate/delete/rename/write 오류의 관찰·보상, bounded tail read와 append/rotate snapshot 동기화, dump partial/error status, 장기 누적·oversize·concurrent dump/record·read-only/storage-full 회귀시험.
- 실제 audit DB·diagnostic files·logcat·ADB·Android app data·heap·fault injection·장기 운전·backup/restore는 수행하지 않았다. tests·lint·typecheck·build·install도 수행하지 않았고, Git stage/commit/push/PR/deploy/rollback도 하지 않았다.
- 다음 읽기 전용 검토는 `LUNA-0013` CSV parser immutable String과 credential buffer lifecycle 경계다.

### 2026-08-02 19:44:47 — `LUNA-0012` cycle closeout

- 이번 cycle의 상세 근거는 앞선 `LUNA-0012` 심화 재감사 항목에 누적했고, 최신 요약과 기존 finding의 마지막 정적 재대조 시각도 19:44:47로 갱신했다. `LUNA-0012`는 기존 A 직접 증거와 현행 source/history가 일치하므로 `P3`·`확정`을 유지한다.
- 보고서 외 파일은 변경하지 않았다. source scope diff 없음과 `git diff --check` 통과, branch/HEAD/upstream 불변, ADB unavailable 및 실행하지 않은 검증 범위는 이번 cycle의 미검증 항목으로 기록했다.
- 다음 읽기 전용 검토는 `LUNA-0013` CSV parser immutable String과 credential buffer lifecycle 경계다.

### 2026-08-02 19:55:13 — `LUNA-0013` cycle closeout

#### 이번 회차 판정

- 이번 Goal 자동 재개에서도 운영 계약 문서를 EOF까지 먼저 읽은 뒤 누적 보고서·Git 기준선·현재 ADB 가용성을 복원했다. `LUNA-0013`은 `P3`·`확정`·높은 신뢰도를 유지하고, 새 finding ID는 추가하지 않았다.
- 핵심 결함은 현재 parser가 credential을 wipe 가능한 배열로만 유지하지 않고 immutable `String` 중간 표현으로 복제하는 구조다. 정상 parse 경로마다 생성되므로, parser 반환 객체의 `CharArray`를 지우는 것만으로는 충분하지 않다.

#### 이번 회차 사실

- `StudentCsvParser.parse()`는 `StudentCsvImport.kt:41-46`에서 최대 1MiB payload를 UTF-8 `String`으로 만들고 BOM을 제거한 뒤 record 목록을 만든다. `parseRecords()`는 `StringBuilder.toString()`을 `:91-126`에서 field별 `String`으로 저장하고, 행 처리 `:53-88`은 `trim()`·`split('|')`·`map(String::trim)`·`toSet()`과 username `Set<String>`을 추가로 만든다.
- `StudentCsvRow`와 `ParsedStudentCsv`의 `clearSensitiveData()`는 `:5-22`의 username/password `CharArray`만 덮는다. `text`, record list, field `String`, username set과 password가 들어간 원본 field `String`에는 직접 덮을 owner가 없다.
- PC receiver `ReceiverState.queue_csv()`는 `pc_receiver/src/matholic_pdf_receiver/server.py:116-124`에서 파일 bytes가 비어 있지 않고 1MiB 이하인지 확인한 뒤 `payload.decode("utf-8-sig")`로 encoding을 검증하고, `bytes(payload)`를 pending queue에 저장한다. 이는 invalid UTF-8 입력에 대한 반대 근거이지만 immutable bytes/String의 zeroization을 보장하지 않는다. 이 PC volatile queue 경계는 기존 `LUNA-0041`로 분리한다.
- Kiosk `PcControlProtocol.decodeResponse()`는 `PcControlProtocol.kt:89-119`에서 plaintext에서 `labelBytes`와 payload를 분리하고 label `String`을 만든 뒤 plaintext를 덮는다. `PcControlClient.kt:56-74,81-119`는 비수락/잘못된 CSV payload를 덮고 response frame/header/request frame을 정리하지만, parser가 만든 immutable `String`을 전달받거나 지우지는 않는다.
- `MainActivity.kt:1779-1791`은 `StudentCsvParser.parse(download.payload)` 전후에 transport `ByteArray`를 지운 뒤 `parsed`를 일반 `ioExecutor` preview lambda로 넘긴다. preview 성공 시 Dialog가 보유하고, 적용 시 일반 `ioExecutor` lambda로 다시 넘긴다(`:1820-1869`). 이 반환 후 mutable row owner·shutdown/rejection 경계는 기존 `LUNA-0031`로 분리한다.
- `StudentCsvImport.kt`의 파일 history는 마지막 도입 commit `0193d89` 이후 후속 commit이 없었다. `StudentCsvParserTest`는 quoted Korean CSV의 값과 반환 row 배열을 확인하고 정상 fixture에서 반환 object를 지우지만, duplicate/header rejection은 예외만 기대하며 intermediate String ownership이나 heap 회수를 확인하지 않는다. repository 계측시험도 정상 preview/import에서 row 배열이 지워지는지만 확인한다.
- `LUNA-0032`의 parser 후속 행 검증 실패 시 partial `StudentCsvRow` owner 유실 경계와 `LUNA-0031`의 반환 후 executor queue owner 경계를 다시 대조했으며, 이번 `LUNA-0013` immutable String 경계와 중복 등록하지 않았다.

#### 추론

- `download.payload.fill(0)`와 반환 row `CharArray` wipe가 모두 수행되어도, credential이 들어간 parser `String` 복사본은 GC 전까지 덮을 수 없다. 실제 보존 기간은 ART heap/GC 동작과 참조 상태에 좌우되며 정적 코드만으로 특정할 수 없다.
- PC receiver의 UTF-8 검증과 Kiosk transport buffer cleanup은 입력 무결성·전송 단계의 완화 근거다. 그러나 parser 내부 표현 변경 없이 그 cleanup만으로 Kiosk heap의 immutable credential copy까지 회수된다고 추론할 수 없다.

#### 가정과 미검증

- 실제 Android ART heap dump, GC 전후 객체 관찰, crash/diagnostic dump, Activity recreation·executor rejection·queued task 제거는 수행하지 않았다.
- 실제 CSV 원문·학생 계정·학생 DB·A 기기 화면/상태는 사용하지 않았다. `String` object가 실제 운영 heap에서 얼마 동안 관찰 가능한지는 미검증이며, 코드상 wipe 불가능한 복사본 생성 사실과 구분한다.
- 이번 회차에는 source·test·docs·settings를 수정하지 않았고 tests, lint, typecheck, build, install, ADB runtime 검증도 실행하지 않았다.

#### 기준선과 보고서 상태

- 보고서 외 파일은 수정하지 않았다. source scope `kiosk`·`webpoc`·`pc_receiver`·`scripts`의 기존 상태, 사용자 수정/미추적 산출물, branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`를 보존한다.
- 현재 PATH에 `adb` executable이 없어 새 A 상태 확인은 하지 않았다. Git stage/commit/push/PR/merge/deploy/rollback도 수행하지 않았다.
- 후속 판단은 parser를 wipe 가능한 byte/char buffer 중심으로 재설계할지, 표준 CSV quoting·preview 수명·partial row·executor shutdown/rejection cleanup을 함께 보장할지에 대해 Sol·사용자 재판단 큐에 남긴다.

- 다음 읽기 전용 검토는 `LUNA-0014` PC control response의 plaintext/request-ID mismatch cleanup 경계다.

### 2026-08-02 20:01:17 — `LUNA-0014` cycle closeout

#### 이번 회차 판정

- `LUNA-0014`는 `P3`·`확정`·높은 신뢰도를 유지한다. 현재 source에서 정상 인증·암호화 기능이 우회된다는 근거는 없지만, 예외·불일치 경로에서 민감한 임시 buffer의 zeroization owner가 코드상 보장되지 않는다는 정적 판정은 해소되지 않았다.
- 기존 `LUNA-0002`(PC 설정 at-rest), `LUNA-0013`(CSV parser immutable String), `LUNA-0010`(ACK 유실·재시도 semantics), `LUNA-0041`(PC receiver volatile buffer)과 대상 process·buffer·실패 경계를 분리했고 새 finding ID는 추가하지 않았다.

#### 이번 회차 사실

- `PcControlProtocol.decodeResponse()`는 `PcControlProtocol.kt:82-90`에서 `decodeSecureFrame()` 반환값의 request ID를 먼저 `require(MessageDigest.isEqual(...))`로 비교한다. `val plaintext = decoded.plaintext`와 `try/finally`는 `:91-119`에서 그 다음에 시작하므로, GCM 인증을 통과한 stale/misbound response의 request ID가 기대값과 다르면 control plaintext가 `finally`의 `plaintext.fill(0)`에 도달하지 않는다. 이 plaintext는 status JSON 또는 CSV response payload를 포함할 수 있고 최대 control payload 상한은 1MiB다.
- 같은 mismatch 경로에서 `SecurePlaintext.requestId`도 별도 wipe되지 않는다(`PcControlProtocol.kt:138-140,191-215`). request ID 자체는 credential이 아니지만, caller가 예외를 받기 전에 response object가 소유한 두 배열을 정리하는 공통 scope가 없다는 사실을 구체화한다. `PcControlClient.exchange()`의 `frame`·`header` `finally`와 request frame/request ID cleanup(`PcControlClient.kt:101-120`)은 이미 반환된 `decoded.plaintext`를 참조하지 않는다.
- `decodeSecureFrame()`는 `PcControlProtocol.kt:186-221`에서 key와 ciphertext만 `finally`에서 덮는다. authenticated header에서 파생된 `magic`·receiver ID·request ID·nonce·AAD는 credential payload가 아니지만, response ownership 전체가 하나의 cleanup scope로 닫히지는 않는다.
- `PcControlProtocol.encodeRequest()`는 `:55-73`에서 내부 plaintext를 `encodeSecureFrame()`이 정상 반환한 뒤에만 `plaintext.fill(0)`을 수행한다. `encodeSecureFrame()`의 key도 `:161-178`에서 `cipher.doFinal(plaintext)`가 성공한 뒤에만 덮으므로, cipher 초기화·AAD·암호화·후속 frame allocation 예외에는 호출자 plaintext와 파생 key의 공통 `finally`가 없다.
- 두 control/PDF protocol의 `deriveKey()`는 HMAC extract/expand 중간 `pseudoRandomKey`를 expand `doFinal()` 성공 뒤에만 `fill(0)`한다(`PcControlProtocol.kt:224-233`, `PcTransferProtocol.kt:134-145`). HMAC provider 초기화 또는 expand 계산 예외는 intermediate key의 명시적 cleanup 범위 밖이다.
- `PcTransferProtocol.encodeRequest()`도 PDF·filename을 포함한 plaintext와 key를 `PcTransferProtocol.kt:49-90`에서 성공 순서에 의존해 정리한다. `PcPdfSender.send()`는 PDF와 transfer frame/request ID/hash를 outer `finally`에서 정리하지만, ACK는 `verifyAck()`가 정상 반환한 뒤 `ack.fill(0)`을 실행한다(`PcPdfSender.kt:19-49`). ACK read 중단·형식/ID/hash/HMAC 검증 실패에서는 ACK 배열 cleanup이 보장되지 않는다. ACK에는 PDF 원문이 없으므로 이 세부 영향은 metadata/signature buffer 수명으로 한정한다.
- `PcControlClient.sendStatus()`는 학생 표시명이 포함될 수 있는 JSON immutable `String`을 `buildString`으로 만들고 ByteArray만 지운다(`PcControlClient.kt:17-53`). 이는 정상 경로의 transient immutable copy이며, 이번 cycle에서는 persistent log/file 저장이나 pairing secret 노출로 확대하지 않았다.
- `PcControlProtocolTest`는 정상 Python status/CSV vector와 tampered response authentication rejection을 확인하고, `PcTransferProtocolTest`는 정상 PDF/ACK vector와 tampered ACK를 확인한다. request-ID mismatch 후 plaintext cleanup, crypto/provider/`doFinal()` 예외, ACK 실패 후 caller wipe를 직접 계측하는 시험은 확인되지 않았다. 관련 protocol history는 control `a3aa80c`(2026-07-30)와 PDF `474132b`(2026-07-29) 도입 이후 cleanup ownership 변경이 없었다.
- `GATE4_IMPLEMENTATION.md:23-28,45`의 일반 `CharArray`·credential 사용 후 wipe 계약과 정상 vector 기록은 반대 근거지만, PC control/PDF crypto helper의 예외 scope와 response mismatch cleanup을 명시하지 않는다.

#### 추론

- request-ID mismatch의 실제 전제는 단순 변조가 아니라 동일 pairing secret으로 GCM 인증을 통과한 stale/misbound response다. 따라서 이 finding은 외부 비인증 frame이 plaintext를 주입한다는 주장이 아니라, authenticated response binding 실패 시 이미 복호화된 임시 plaintext를 caller가 지우지 못하는 lifecycle 결함이다.
- encode/ACK 예외 경계에서도 Android ART가 이후 객체를 회수할 수는 있지만, `ByteArray.fill(0)` 계약이 예외 경로까지 확장되지는 않는다. 실제 heap 잔류 시간·메모리 재사용 여부는 GC와 fault timing에 의존한다.
- 정상 transport frame/header/key cleanup과 authenticated response binding은 영향과 발생 조건을 줄인다. 그 반대 근거 때문에 P0/P1 또는 pairing secret 영구 노출로 확대하지 않고 현재 P3을 유지한다.

#### 가정과 미검증

- 실제 stale/misbound authenticated response, crypto provider 예외, allocation failure, HMAC/ACK read 중단, heap dump·GC timing·Android runtime은 실행하지 않았다.
- 실제 PDF, 학생 이름, status JSON, CSV 원문, pairing secret은 사용하거나 출력하지 않았다. 파일·로그·DB에 평문이 영속화된다는 근거도 이번 범위에서 확인하지 않았다.
- tests·lint·typecheck·build·install·ADB runtime 검증은 실행하지 않았고, source·test·docs·settings는 수정하지 않았다.

#### 기준선과 보고서 상태

- 보고서 외 파일은 수정하지 않았다. source scope `kiosk`·`webpoc`·`pc_receiver`·`scripts`의 기존 사용자 변경·미추적 산출물을 보존했고 branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`도 불변이다.
- 현재 PATH에 `adb` executable이 없어 A 상태와 새 화면은 확인하지 않았다. Git stage/commit/push/PR/merge/deploy/rollback도 수행하지 않았다.
- 후속 판단은 crypto helper 전체를 `try/finally` 소유 scope로 묶고, response request-ID 비교를 plaintext cleanup scope 안으로 이동하며, ACK read/검증 실패까지 caller wipe를 보장할지 Sol·사용자 재판단 큐에 남긴다.

- 다음 읽기 전용 검토는 `LUNA-0015` 학생 비활성화 후 credential retention과 기존 알려진 문제의 현재 유효성이다.

### 2026-08-02 20:04:49 — `LUNA-0015` cycle closeout

#### 이번 회차 판정

- `LUNA-0015`는 `P3`·`기존 알려진 문제`·높은 신뢰도를 유지한다. 현재 코드와 제품 보안 문서가 “논리적 비활성화는 제공하지만 앱 private DB의 암호화된 credential record는 보안 삭제하지 않는다”는 동일한 정책을 명시하므로, 문서-구현 불일치나 신규 독립 finding으로 승격하지 않았다.
- 이번 회차에서 `findById()`가 inactive row를 반환한다는 점은 확인했지만, `decryptCredentials()`와 credential update의 `isActive` guard가 존재한다. 이 자체를 별도 finding으로 중복 등록하지 않았다.

#### 이번 회차 사실

- `StudentEntity`는 `Entities.kt:11-25`에서 username/password ciphertext·IV·encryption version, QR hash, `isActive`, 생성/갱신 시각을 보유하지만 `deactivatedAt`, retention class 또는 purge marker는 없다.
- `StudentRepository.deactivateStudent()`는 `StudentRepository.kt:619-634`에서 기존 row를 읽고 새 QR hash를 발급한 뒤 transaction 안에서 `qrTokenHash`, `isActive=false`, `updatedAtEpochMs`만 바꾸고 `QR_REVOKED`·`STUDENT_DEACTIVATED` audit event를 기록한다. `usernameCiphertext`, `usernameIv`, `usernameEncryptionVersion`, `passwordCiphertext`, `passwordIv`, `passwordEncryptionVersion`은 `student.copy()`에서 변경하지 않는다.
- `StudentDao`의 `findById()`는 `Daos.kt:18-19`에서 active 조건 없이 row를 반환한다. 반면 QR·session eligibility·class 목록·전체 학생 목록 query는 `isActive=1`을 사용하며(`:21-79`), `StudentRepository.decryptCredentials()`와 `updateStudentCredentials()`는 `StudentRepository.kt:591-616,845-...`에서 inactive를 거부한다. 따라서 논리적 사용 차단은 존재하지만 row 삭제나 credential purge는 아니다.
- `KioskDatabase`는 version 3이고 `KioskDatabase.kt:21,40-94`에서 migration 1→2로 `qr_card_status`를 만들고 기존 student row를 읽어 status를 채우며, migration 2→3으로 `admin_credential.pinLength`만 추가한다. 두 migration과 Room builder에는 inactive student credential을 지우는 SQL·retention callback·periodic purge가 없다.
- `StudentDao`에 있는 mutation은 insert/update뿐이고 학생 delete/purge/erase API는 없다(`Daos.kt:11-19`). `QrCardStatusEntity`의 `ON DELETE CASCADE`는 실제 student delete 호출이 있을 때의 FK 동작일 뿐, 현재 production student deletion 경로의 증거가 아니다.
- `docs/KNOWN_LIMITATIONS.md:27`과 `SECURITY.md:39-42`는 AES-GCM field encryption·QR hash 교체·active lookup exclusion과 함께 encrypted credential record의 secure deletion 부재를 명시한다. `PRODUCT_DECISIONS.md:102`도 학생 비활성화에는 30초 undo를 적용하지 않는다고 할 뿐 credential retention을 해소하지 않는다.
- `RepositoryInstrumentedTest.kt:380-404`는 비활성 뒤 구 QR 거부, active student 목록 제외, replacement QR hash, inactive credential update 거부와 입력 `CharArray` wipe 및 audit event를 확인한다. 이 시험은 ciphertext/IV/version 삭제나 보존 기간을 실패 조건으로 검사하지 않는다.
- `KioskDatabaseMigrationInstrumentedTest.kt:23-90`은 migration 1→2의 QR status 생성과 migration 2→3의 `pinLength` 추가를 확인한다. 현재 migration SQL에는 credential purge가 없고, 이 시험은 inactive row retention policy를 검증하지 않는다.
- 관련 history를 다시 대조한 결과 deactivate 구현 line은 `e093bd0`/`9eaf2c5`에 있고, `StudentRepository.kt` 전체에는 이후 `0193d89`, `4afdf0b` 등 파일 변경이 있지만 `git log -S'usernameCiphertext'`에서 credential field/deactivation purge를 도입한 후속 변경은 확인되지 않았다. `KioskDatabase.kt` 후속 `9af507b`는 `admin_credential.pinLength` migration만 추가한다.
- `allowBackup=false`·data extraction exclusion·Keystore AES-GCM은 외부 복제와 at-rest 평문 노출을 줄이는 반대 근거다. 그러나 동일 앱 private DB의 inactive ciphertext 존재와 보존 기간을 없애지는 않는다.

#### 추론

- 현재 제품 계약의 “비활성화”는 접근 차단·QR 폐기이며, credential 데이터 최소화·secure erase·기간 제한까지 포함하지 않는다. 이는 보안 삭제 요구가 결정되지 않은 상태에서 구현이 문서와 일치하는 기존 제한이다.
- 같은 앱 private DB와 관련 Keystore 키에 접근 가능한 운영·복구·관리 경계에서 inactive credential ciphertext가 계속 존재할 수 있다. 실제 DB 추출·키 노출·SQLite page overwrite를 확인한 것은 아니므로 P0/P1 또는 평문 노출로 확대하지 않는다.
- `findById()`의 inactive row 반환은 내부 caller가 별도 guard를 적용하는 현재 구조와 함께 봐야 한다. 현재 복호화·credential update guard가 존재하므로, 이 회차에서는 retention 경계만 유지한다.

#### 가정과 미검증

- 실제 앱 DB·Keystore·backup/export·SQLite freelist/page reuse·복호화 실행·A 기기 상태는 사용하지 않았다.
- 비활성화 직후의 실제 ciphertext 보존 기간, 기기 초기화·키 회전·재등록 운영 정책과 내부 관리자/복구 주체의 위협 모델은 문서만으로 확정하지 않았다.
- tests·lint·typecheck·build·install·ADB runtime 검증은 실행하지 않았고 source·test·docs·settings는 수정하지 않았다.

#### 기준선과 보고서 상태

- 보고서 외 파일은 수정하지 않았다. 기존 사용자 수정·미추적 산출물을 보존했고 branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다.
- 현재 PATH에 `adb` executable이 없어 A 상태와 설치본을 새로 확인하지 않았다. Git stage/commit/push/PR/merge/deploy/rollback도 수행하지 않았다.
- Sol·사용자 재판단 큐에는 (a) 비활성 credential ciphertext의 즉시 삭제 여부, (b) 복구·재등록을 위한 보존 기간, (c) Keystore key rotation·폐기와 DB page cleanup의 결합 정책을 남긴다. 이번 Goal에서는 구현하지 않는다.

- 다음 읽기 전용 검토는 `LUNA-0016` Web network pause overlay의 답안 비노출 경계다.

### 2026-08-02 20:07:58 — `LUNA-0016` cycle closeout

#### 이번 회차 판정

- `LUNA-0016`은 `P3`·`후보`·중간 신뢰도를 유지한다. 현재 source가 답안 문자열을 외부로 전송하거나 저장한다는 증거는 없지만, 제품이 요구하는 “통신 대기 중 답안 비노출”을 완전 불투명 차폐와 렌더링 검증으로 보장하지 않는다.
- `LUNA-0017`의 focus/IME/key routing, `LUNA-0018`의 network monitor fallback, `LUNA-0019`의 accessibility subtree는 각각 다른 입력·상태·접근성 경계로 유지하고 이번 항목에 중복 합산하지 않았다.

#### 이번 회차 사실

- `docs/PRODUCT_DECISIONS.md:96-97`은 네트워크 단절 시 현재 WebView와 답안을 유지하면서 입력을 가리고, 검증된 연결 복구 뒤 재로딩 없이 재개하도록 정한다. `docs/RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md:252-263`의 통신 단절 안전 대기 항목은 답안 화면 차폐·입력 차단·무잠금 복구·답안 보존·후속 제출을 요구하지만 현재 모두 미체크 상태다.
- `webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:1758-1772`의 `showActive()`는 WebView를 `VISIBLE`로 유지한다. `updateNetworkPause()`는 `:2246-2257`에서 `state == ACTIVE && !hasValidatedNetwork()`를 계산해 `networkPausePanel.visibility`와 idle warning/timer·진단 이벤트만 바꾼다. WebView 내용을 제거하거나 별도 opaque surface로 교체하는 호출은 없다.
- `webpoc/src/main/res/layout/activity_main.xml:455-485`의 `network_pause_panel`은 WebView와 idle panel 뒤에 선언된 마지막 `match_parent` `FrameLayout`이며 `clickable=true`·`focusable=true`다. 그러나 `android:background="#F2102A43"`의 알파는 `0xF2`(242/255)로 255가 아니고, 중앙 `LinearLayout`(`:463-469`)에는 별도 background가 없다. 따라서 하위 WebView가 합성상 완전히 제거됐다고 정적 증명할 수 없다.
- 같은 layout에서 idle warning의 중앙 `LinearLayout`에는 `#FFFFFFFF` 배경이 있지만, network pause 중앙 layout에는 없다. 이는 안내문 뒤의 작은 영역도 전체 화면 배경과 동일한 반투명 합성 경계를 갖는다는 차이를 만든다.
- `networkPausePanel`을 다루는 production 호출은 `refreshNetworkPause()`·`registerDefaultNetworkCallback()`·`transition()`·main-frame network error와 `onDestroy()` unregister 경계에 연결되어 있다. 이번 범위에서 overlay 자체의 runtime alpha, pixel, z-order, WebView 가독성을 검증하는 test 연결은 찾지 못했다.
- `rg` 범위의 Web JVM/Android tests에는 `networkPausePanel`, `NETWORK_PAUSE`, `NETWORK_RESUME`, `updateNetworkPause()` 또는 `registerDefaultNetworkCallback()`를 직접 검증하는 차폐 UI 시험이 없다. DOM contract 시험은 WebView 내부 문제/결과 구조를 다루며 Android View overlay 합성은 다루지 않는다.
- `git blame`에서 `updateNetworkPause()`의 핵심 줄과 layout `network_pause_panel` 전체는 `d8ca06f`(2026-07-30)에 도입되었다. `MainActivity` 전체에는 이후 Web/remote support 관련 commit이 있지만 해당 pause lines에 opaque 차폐·pixel 검증을 추가한 후속 변경은 확인되지 않았다.
- 과거 W05의 `LOCKED / NETWORK_ERROR` 기록은 현재 `d8ca06f`의 “WebView 유지 + network pause overlay” 이전 상태이고, 최신 문서도 실제 통신 단절 현장 검증을 미체크로 남긴다. `FLAG_SECURE`, full-screen last child, clickable/focusable 설정은 화면 캡처·일반 touch 위험을 줄이는 반대 근거지만 alpha 합성 결과를 증명하지 않는다.

#### 추론

- `#F2102A43`는 약 94.9% 불투명하지만 약 5.1%의 하위 WebView 픽셀이 합성될 수 있다. 답안이 실제로 읽힐지는 글자 대비·WebView 색상·화면 밝기·기기 compositor에 좌우되므로, 코드만으로 실제 노출을 확정하지 않고 후보로 유지한다.
- 네트워크 단절은 답안·수식·커서가 화면에 있는 상태에서 발생할 수 있다. 따라서 차폐가 충분하지 않은 특정 렌더링 조건에서는 주변인이 답안을 볼 수 있는 시험 보안·개인정보 영향이 가능하다. 외부 전송·DB 유출로 확대하지 않는다.
- WebView를 유지해 재로그인 없이 복구하려는 제품 결정과 overlay의 입력/시각 차폐 계약은 양립할 수 있지만, 현재 구현은 전자를 구현하고 후자의 “비노출”을 opaque surface나 renderer-level assertion으로 닫지 않는다.

#### 가정과 미검증

- 실제 A WebView 화면, 답안·수식 입력, 네트워크 차단/복구, 화면 캡처, 화면 밝기·제조사 compositor 조합은 수행하지 않았다. 해당 조작은 업무 데이터·네트워크 상태 변경을 필요로 하므로 계약상 생략했다.
- 실제 alpha 합성 결과와 사람이 읽을 수 있는 수준의 답안 노출 여부는 정적 XML/코드만으로 판정하지 않았다.
- tests·lint·typecheck·build·install·ADB runtime 검증은 실행하지 않았고 source·test·docs·settings는 수정하지 않았다.

#### 기대 결과와 실제 결과

- 기대: WebView와 답안을 메모리에 유지하더라도 연결 대기 중 화면에는 완전 불투명하고 검증 가능한 차폐가 적용되며, 입력도 차단되고 복구 후 기존 화면으로 안전하게 돌아온다.
- 실제: 마지막 full-screen clickable/focusable overlay는 존재하지만 배경이 `#F2` 반투명이고 중앙 child도 opaque하지 않으며, network pause 전용 Android UI/렌더링 회귀시험과 현장 checklist 증거가 없다. 입력·IME·key 차단은 `LUNA-0017`에서 별도 검토한다.

#### 기준선과 보고서 상태

- 보고서 외 파일은 수정하지 않았다. 기존 사용자 수정·미추적 산출물을 보존했고 branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다.
- 현재 PATH에 `adb` executable이 없어 A 상태·설치본·새 화면을 확인하지 않았다. Git stage/commit/push/PR/merge/deploy/rollback도 수행하지 않았다.
- Sol·사용자 재판단 큐에는 WebView 보존과 답안 비노출을 함께 만족하는 완전 불투명 blocker/renderer contract, network pause 입력 차단, 안전한 fixture·현장 회귀검증의 범위를 남긴다. 이번 Goal에서는 구현하지 않는다.

- 다음 읽기 전용 검토는 `LUNA-0018` network monitor callback 등록 실패·fallback 경계다.

### 2026-08-02 20:12:40 — LUNA-0017 cycle closeout

#### 이번 회차 판정

- 현재 정적 근거는 기존 `LUNA-0017`을 재확인한다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지한다.
- 새 finding ID는 추가하지 않았다. `LUNA-0016`의 시각적 차폐, `LUNA-0018`의 network monitor fallback, `LUNA-0019`의 접근성 subtree는 각각 별도 경계로 유지한다.
- 이번 Goal에서는 source·test·docs·settings·scripts를 수정하지 않고, 보고서만 누적 갱신했다.

#### 사실

- `webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:2246-2258`의 `updateNetworkPause()`는 `network_pause_panel` visibility를 바꾸고 pause 진입 시 idle warning을 숨기며 inactivity timer를 취소한다. 복구 시 diagnostic event를 남기고 timer를 다시 예약한다. `webView.clearFocus()`, `InputMethodManager.hideSoftInputFromWindow()`, WebView 비활성화, 입력 대상 전환 호출은 이 경로와 `transition()`에서 확인되지 않는다.
- 같은 파일 `:875-890`의 Activity key handler는 `KEYCODE_BACK`만 소비한다. 일반 key·방향키·삭제키를 network pause 조건으로 소비하는 `dispatchKeyEvent()` 또는 조건부 `onKeyDown()`/`onKeyUp()` 경계는 없고, 다른 key는 `super`로 전달된다.
- 같은 파일 `:2285-2295`의 `dispatchTouchEvent()`는 ACTIVE·두 overlay 비표시일 때 inactivity timer를 예약하는 조건만 검사하고 항상 `super.dispatchTouchEvent(event)`를 호출한다. 레이아웃의 `network_pause_panel`은 `:455-461`에서 full-screen `clickable=true`·`focusable=true`로 선언되어 일반 touch를 소비할 가능성은 있지만, visibility 전환 때 `requestFocus()`를 호출하거나 이미 열린 IME를 닫는 코드는 없다.
- `activity_main.xml:10-15`의 WebView에는 `importantForAutofill`만 있고 `focusableInTouchMode`, `filterTouchesWhenObscured` XML 선언, 접근성 subtree 차폐 속성은 없다. `MainActivity.kt:391-410`의 `configureSensitiveInputs()`는 WebView에 `filterTouchesWhenObscured=true`를 설정하지만, 이는 다른 window가 덮은 touch의 처리 경계이지 network pause 중 keyboard/IME/DOM key 입력을 닫는 계약이 아니다.
- `WebDomScripts.kt:3760-3765`의 `activeMathField()`는 `document.activeElement`에 붙은 MathQuill editor를 우선 사용하고, 없으면 visible editor를 찾는다. `:3800` 이후 `runKeyAction()`은 `write`·`command`·`keystroke`를 실행한 뒤 `field.focus()`를 호출한다. `:3936-3963`의 generated keypad button은 pointer/click으로 이 동작을 실행한다. 따라서 WebView의 기존 focus/IME/DOM 입력 경계가 pause 중 유지되는지는 panel 표시만으로 닫히지 않는다.
- 정적 test 검색에서 `NETWORK_PAUSE`, `NETWORK_RESUME`, `updateNetworkPause()`와 WebView focused input의 network pause 중 key/IME 불변을 직접 검증하는 unit/instrumented test 연결은 확인되지 않았다. `RecoveryInstrumentedTest`의 관련 key 검증은 BACK 소비이며, 기존 DOM 시험의 MathQuill/keypad 검증은 network state와 결합하지 않는다.
- `git blame`상 BACK handler는 `8b7e0d1`(2026-07-28), network monitor·pause panel·`dispatchTouchEvent()` 경계는 `d8ca06f`(2026-07-30)에 도입되었다. 현재 해당 줄 후속 history에서 focus/IME/hardware-key barrier를 추가한 변경은 확인되지 않았다.

#### 추론

- 수식 입력칸이 focus된 상태로 연결이 끊기고 IME가 열린 경우, full-screen panel이 일반 touch를 덮더라도 현재 코드만으로 IME·hardware key·WebView DOM keypad가 모두 답안을 변경하지 않는다고 보장할 수 없다. 이는 코드상 가능한 입력 경계이며, 실제 답안 변경을 관찰한 사실은 아니다.
- panel의 `focusable=true`가 Android focus를 항상 이동시키는지, 이동하더라도 IME가 닫히는지는 실행·플랫폼 구현에 의존한다. 그래서 확정 finding이 아닌 조건부 P3 후보로 유지한다.

#### 반대 근거

- panel이 WebView 위의 마지막 full-screen child이고 `clickable=true`·`focusable=true`라 일반 touch 차폐 가능성이 있다. BACK은 Activity에서 소비하고 pause 진입 시 inactivity timer도 취소한다.
- `filterTouchesWhenObscured=true`와 기존 WebView session 보존 정책은 touch/세션 복구의 보조 방어다. 그러나 이 근거들은 focused WebView의 IME 종료, 일반 hardware key 소비, 접근성 action 및 DOM keypad 입력 불변을 증명하지 않는다.

#### 가정과 미검증

- 실제 Android input routing은 API level·WebView renderer·IME·제조사 키보드와 현재 focus 상태에 따라 달라질 수 있다고 보았다.
- 실제 수식 입력 focus→network 단절→소프트 키보드/하드웨어 key/접근성 action→복구 후 답안 보존 시나리오는 실행하지 않았다. ADB가 PATH에 없어 기기와 설치본도 사용할 수 없었다.
- tests·lint·typecheck·build·install·network 차단·IME 조작·WebView 실행은 수행하지 않았다.

#### 안전한 확인 절차

- `MainActivity.kt`의 network pause transition, Activity key/touch routing, focus/lifecycle 경계와 `activity_main.xml`의 WebView/panel 속성을 읽기 전용으로 대조했다.
- `WebDomScripts.kt`의 active MathQuill field·keypad action, `RecoveryInstrumentedTest`, 관련 test/docs 검색과 `git blame`을 읽기 전용으로 대조했다.
- 외부 상태·소스·테스트·문서·설정·스크립트·산출물·기기 데이터는 변경하지 않았다.

#### 기대 결과와 실제 결과

- 기대: network pause 진입 시 WebView focus와 IME를 정리하고, 복구 전에는 touch·hardware key·IME·접근성 입력이 답안을 바꾸지 않으며, 복구 후 허용된 입력만 재개한다.
- 실제: 현재 구현은 overlay visibility와 idle timer를 관리하며 일반 touch를 막을 가능성은 있지만, focus/IME 해제와 일반 key/DOM input barrier를 명시하지 않는다. 이 차이는 정적 코드로 확인되며 실제 키 입력 실패/성공은 검증하지 않았다.

#### 기준선과 보고서 상태

- 보고서 파일만 갱신했다. 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`와 source scope를 보존했다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변으로 유지해야 하며, 이번 회차에는 Git stage/commit/push/PR/merge/deploy/rollback을 수행하지 않는다.
- 현재 PATH에 `adb` executable이 없어 A 상태·화면·IME·network transition을 확인하지 못했다. 다음 읽기 전용 검토 대상은 `LUNA-0018`이다.

#### 재판단 큐

- Sol·사용자는 WebView 답안 보존과 network pause 입력 차단을 하나의 상태 계약으로 정하고, focus 이동·IME 종료·hardware key/접근성 action/DOM keypad 차단·복구 후 focus 재허용을 실제 수식 입력 회귀 범위에 포함할지 결정해야 한다. 이번 Goal에서는 구현하지 않는다.

### 2026-08-02 20:18:56 — LUNA-0018 cycle closeout

#### 이번 회차 판정

- 현재 정적 근거는 기존 `LUNA-0018`을 재확인한다. 심각도 `P4`, 상태 `후보`, 신뢰도 `중간`을 유지한다.
- 등록 실패 자체를 현재 A에서 재현한 사실이나 즉시 기능 실패로 확정하지 않았다. 새 finding ID는 추가하지 않았다.
- 과거 W05/G306의 정상 Wi-Fi 단절·`LOCKED` 기록은 현재 network pause monitor failure 경계와 다른 버전·상태 흐름이므로 반증으로 사용하지 않았다.

#### 사실

- `webpoc/src/main/java/com/local/matholickiosk/webpoc/MainActivity.kt:211-222`의 `initializeUi()`는 `registerNetworkMonitor()`를 호출한 뒤 `uiInitialized = true`를 설정한다. 등록 직후 `updateNetworkPause()`는 초기화 플래그 때문에 반환한다. 현재 `state`의 초기값은 `IDLE`이고 persisted non-IDLE은 `resumeFromPersistedState()`에서 recovery/lock 경로로 처리되므로, 정상 secure session의 ACTIVE 진입은 초기화 완료 후 `transition()`을 통과한다. `transition()`의 `handler.post(::updateNetworkPause)`는 이 정상 경로의 보완 trigger다.
- `MainActivity.kt:2220-2229`의 `registerNetworkMonitor()`는 `ConnectivityManager`가 null이면 진단 없이 반환한다. 서비스가 존재해도 `registerDefaultNetworkCallback(networkCallback)`가 예외를 내면 `runCatching`이 `NETWORK_MONITOR_FAILED` private event만 기록하고 재등록·지연 retry·periodic check·watchdog을 예약하지 않는다. 실패 시 `networkCallbackRegistered`도 true가 되지 않는다.
- `MainActivity.kt:142-149`의 callback은 `onAvailable`, `onLost`, `onCapabilitiesChanged`에서 `refreshNetworkPause()`만 호출한다. `:2231-2234`의 `refreshNetworkPause()`는 callback이 실제로 도착한 경우에만 main Handler에 `updateNetworkPause()`를 게시한다.
- `MainActivity.kt:2237-2243`의 `hasValidatedNetwork()`는 `ConnectivityManager`·active network·capabilities가 없으면 false를 반환하고, `INTERNET`과 `VALIDATED` capability를 모두 요구한다. `:2246-2258`의 `updateNetworkPause()`는 ACTIVE에서 false를 pause panel 표시로 반영하고, true 복귀는 callback·state transition·별도 호출이 있을 때만 평가된다.
- `MainActivity.kt:564-593`의 `onReceivedError()`는 ACTIVE main-frame 오류이고 validated network가 없을 때 `updateNetworkPause()`를 호출한 뒤 반환한다. 이는 요청 오류가 실제로 발생한 경우의 보완 trigger다. ACTIVE 상태에서 validated network가 있거나 callback 등록 실패를 제외한 일반 main-frame policy 조건이면 기존 `showLocked()` 분기로 간다.
- `AndroidManifest.xml:5-6`에는 `INTERNET`과 `ACCESS_NETWORK_STATE`가 선언돼 정상 capability 조회·callback 등록에 필요한 권한 경계를 제공한다. 이 선언은 system service null, callback registration exception, 등록 후 callback 유실을 복구하는 정책은 아니다.
- 정적 test 검색에서 `NETWORK_MONITOR_FAILED`, `registerDefaultNetworkCallback`, callback registration exception/null, ACTIVE 유휴 단절과 pause 전환 지연을 직접 주입·검증하는 unit/instrumented test는 확인되지 않았다. `NetworkFailureReasonTest`·`WebFailurePolicyTest`와 과거 W05/G306 기록은 오류 분류 또는 다른 실패 흐름을 다룬다.
- 관련 history/blame에서 monitor와 pause 구현은 `d8ca06f`(2026-07-30)에 도입됐고 `initializeUi()`의 등록 호출·현재 callback/update lines도 해당 도입에 남아 있다. 이후 `MainActivity.kt` history에서 등록 실패 retry/fallback을 추가한 변경은 확인되지 않았다.
- `docs/WEB_POC_TEST_PLAN.md`와 `WEB_POC_FAILURE_LOG.md`의 W05 기록은 `eb09567`(2026-07-22) baseline에 작성됐고, 당시 흐름은 ACTIVE에서 Wi-Fi 차단 후 필수 `채점 끝내기` 요청을 발생시켜 `LOCKED / NETWORK_ERROR`로 닫는 방식이다. 현재 `d8ca06f`는 WebView를 유지하는 network pause overlay와 callback monitor를 이후 도입했다.

#### 추론

- callback 등록이 실패한 뒤 ACTIVE 화면이 validated network 상태에서 시작되고, 이후 사용자가 화면을 이동하지 않아 WebView main-frame error와 state transition이 발생하지 않는 동안 연결이 끊기면, `refreshNetworkPause()`를 호출할 이벤트가 없어 `network_pause_panel` 표시가 늦어질 수 있다. 그동안 `LUNA-0016`·`LUNA-0017`의 차폐·입력 보호도 늦게 적용될 가능성이 있다.
- `ConnectivityManager`가 null인 환경에서는 ACTIVE 진입 시 `hasValidatedNetwork()`가 false라 panel이 표시될 수 있지만, callback이 등록되지 않아 복구 시 panel을 숨길 독립 event가 없을 수 있다. 이 역시 다른 state transition이나 WebView error가 발생하면 보완될 수 있어 조건부 영향으로 둔다.
- 등록 예외와 유휴 ACTIVE 단절의 조합은 코드상 가능한 failure-completeness 공백이지만, 현재 A에서 callback이 실패했다거나 실제 pause 지연이 발생했다는 관찰은 없다. 따라서 P4 후보를 넘기지 않는다.

#### 반대 근거

- 정상 경로에는 `ACCESS_NETWORK_STATE`, `registerDefaultNetworkCallback()`, `onAvailable`·`onLost`·`onCapabilitiesChanged`, ACTIVE transition 재평가, main-frame error 재평가가 있다. 등록이 성공하거나 다른 trigger가 발생하면 pause 상태는 갱신된다.
- 과거 W05/G306은 네트워크 오류에서 `LOCKED`로 닫히고 복구 후 `IDLE`로 돌아온 운영 근거다. 다만 그 시험은 현재 overlay·monitor registration failure·유휴 ACTIVE 조건을 분리하지 않으므로 현재 finding을 제거하지 않는다.
- manifest permission과 `hasValidatedNetwork()`의 명시적 capability 판정은 정상 Android 환경의 의도를 분명히 하며, callback registration failure를 정상 경로로 단정할 근거는 없다.

#### 가정과 미검증

- `registerDefaultNetworkCallback()` 예외, `ConnectivityManager` null, 등록 성공 후 callback 유실, validated network에서 조용한 단절을 실제 기기에서 유도하지 않았다.
- ADB가 PATH에 없어 현재 A의 permission/service 상태·설치본·network callback 동작을 확인하지 못했다.
- tests·lint·typecheck·build·install·network 차단·프로세스/서비스 장애 유도는 수행하지 않았다. 문서의 historical PASS를 현재 runtime proof로 재사용하지 않았다.

#### 안전한 확인 절차

- `MainActivity.kt`의 callback 등록·callback handlers·capability 판정·state transition·main-frame error 보완 경로를 읽기 전용으로 대조했다.
- `AndroidManifest.xml`, network 관련 unit/instrumented test 검색, W05/G306 문서·현재 field checklist·`git blame`·`git log`를 읽기 전용으로 대조했다.
- source·test·docs·settings·scripts·build artifact·기기·네트워크 상태는 변경하지 않았다.

#### 기대 결과와 실제 결과

- 기대: callback 등록 실패나 callback 유실에도 bounded retry·periodic validated check·안전 pause 중 하나가 연결 상태를 감시하고, 복구 시 기존 WebView를 안전하게 재개한다.
- 실제: 정상 callback, ACTIVE transition, main-frame error라는 보완 trigger는 있지만 등록 실패 시 전용 retry/watchdog/fallback은 없다. 유휴 ACTIVE에서 조용한 단절을 정해진 시간 안에 감지한다는 계약은 정적으로 확인되지 않는다.

#### 영향 및 재판단 큐

- 운영/무결성: 드문 system service·callback registration failure 환경에서 연결 대기 전환이 늦어져 WebView가 잠시 ACTIVE 입력 가능한 상태로 남을 가능성. 실제 발생·지연 시간은 미검증이다.
- Sol·사용자는 등록 실패를 즉시 안전 pause로 볼지, bounded retry와 주기 capability check를 둘지, 실패 시 `LOCKED`와 기존 WebView 보존 중 어떤 정책을 채택할지 정해야 한다. 이번 Goal에서는 구현하지 않는다.

#### 근본 원인 후보와 수정 후 검증

- 근본 원인 후보는 `d8ca06f`가 정상 `ConnectivityManager.NetworkCallback` 경로를 구현하면서 등록 실패를 관찰 event로만 모델링하고, callback availability를 별도 상태·재시도·주기 check 계약으로 만들지 않은 것이다.
- 수정 후에는 정상 등록, `ConnectivityManager` null, registration exception, 등록 후 callback 미도착, 유휴 ACTIVE 단절, main-frame error, 복구, Activity 종료를 각각 합성·기기 환경에서 검증하고 pause 표시 지연·입력 차단·무재로그인 resume을 측정해야 한다.

#### 관련 발견

- `LUNA-0016`은 pause panel 시각 차폐, `LUNA-0017`은 pause 입력 routing, `LUNA-0019`는 접근성 subtree다. `LUNA-0018`은 이 보호 상태로 진입시키는 network monitor fallback만 다룬다.

#### 기존 테스트가 잡지 못한 이유

- 기존 network failure policy 시험은 오류 코드 분류와 main-frame failure policy를 검증하지만 Android `ConnectivityManager` callback 등록 실패·callback 유실·유휴 ACTIVE 감시·pause overlay 전환 지연을 검증하지 않는다. 과거 W05/G306도 현재 monitor failure seam과 다른 실제 failure flow다.

#### 기준선과 보고서 상태

- 보고서 파일만 갱신했다. 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`와 source scope를 보존했다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변으로 유지하며 Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
- 현재 PATH에 `adb` executable이 없어 A runtime을 확인하지 못했다. 다음 읽기 전용 검토 대상은 `LUNA-0020` 원격 지원 중 민감 화면 `FLAG_SECURE` 경계다.

### 2026-08-02 20:24:36 — LUNA-0019 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0019`를 재확인했다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- `LUNA-0016` 시각 차폐, `LUNA-0017` IME/key routing, `LUNA-0018` callback fallback과 중복하지 않았다.

#### 사실

- `activity_main.xml:10-15`의 WebView는 `importantForAutofill`과 초기 `visibility="invisible"`만 선언한다. `importantForAccessibility`, `screenReaderFocusable`, `accessibilityDelegate`, `NO_HIDE_DESCENDANTS`와 접근성 focus 전환은 없다.
- `activity_main.xml:455-486`의 `network_pause_panel`은 마지막 full-screen 형제이며 `clickable/focusable`과 중앙 ProgressBar·안내 TextView만 가진다. panel·child에 underlying WebView subtree를 숨기거나 안내를 단일 accessibility node로 제한하는 설정은 없다.
- `MainActivity.kt:391-410`의 설정은 autofill·save·obscured touch만 다룬다. `:1758-1773`, `:2184-2218`, `:2246-2258`의 ACTIVE/pause 전이도 WebView/panel visibility와 timer만 바꾼다.
- Web POC source/test 검색에서 접근성 제어 symbol은 무매치였다. `RecoveryInstrumentedTest.kt`의 `isAccessible = true`는 private reflection 접근 플래그이지 Android 접근성 tree/action 검사가 아니다.
- Gate 1 Probe는 `com.matholic.mathapp` 별도 package만 읽고, `verify-gate5-device-owner.ps1`는 Device Owner·Lock Task·allowlist만 검사한다. 두 근거 모두 현재 Web POC pause subtree가 숨겨졌거나 모든 accessibility service가 비활성이라는 증거가 아니다.
- WebView layout 줄은 `eb09567`, pause panel은 `d8ca06f`에 도입됐고 `importantForAccessibility` 추가 history는 확인되지 않았다.

#### 추론과 반대 근거

- 일반 accessibility provider가 활성화된 환경에서는 WebView virtual node가 panel 뒤에서 계속 제공되거나 action 대상에 남을 가능성이 있다. 다만 Gate 1 Probe가 Web POC를 읽는다고 주장하지 않으며, 실제 node/action 전달은 실행하지 않았다.
- full-screen `clickable/focusable` panel은 일반 touch/focus를 우선할 가능성이 있고, 전용기기 정책이 접근성 서비스를 제한할 수도 있다. 그러나 현재 코드·문서에는 그 제한 또는 subtree 차폐를 pause 상태 계약으로 증명하는 근거가 없다.

#### 가정과 미검증

- TalkBack·Switch Access·접근성 service 설정·network pause·답안 화면·accessibility focus/action·ADB·화면 캡처는 수행하지 않았다.
- 현재 A 기기의 accessibility service 목록과 Android accessibility tree는 `adb` 부재로 확인하지 못했다. tests·lint·typecheck·build·install도 실행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: pause 동안 안내 panel만 accessibility focus/action 대상이고 WebView 답안 subtree는 숨겨지며, 복구 후에만 원래 focus를 복원한다.
- 실제: panel의 일반 입력 차폐 가능성은 있으나 WebView subtree 차폐·접근성 focus 반환·해당 Android 회귀시험은 확인되지 않았다.

#### 영향·재판단 및 후속 검증

- 접근성 provider가 활성화된 조건에서 답안이 탐색·읽히거나 action으로 변경될 가능성은 P3 후보로 남긴다. 실제 노출·변경은 확인하지 않았다.
- Sol·사용자는 전용기기 접근성 service 정책 또는 pause 시 WebView subtree 차폐 정책과 복구 focus 규칙을 결정해야 한다. 수정 후 TalkBack·Switch Access·일반 provider의 pause 진입/유지/복구와 node read/action 불가를 실제 Android UI로 검증해야 한다.

#### 기준선과 보고서 상태

- 보고서 파일만 갱신했으며 기존 사용자 수정·미추적 산출물과 source scope를 보존했다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.

### 2026-08-02 20:40:22 — LUNA-0022 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0022`를 controller·호출부·session preflight·운영 verifier·Gate 5 문서·Git 이력으로 재확인했다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- `LUNA-0020`의 민감 화면 차단과 `LUNA-0021`의 원격 지원 상태 동기화와는 중복하지 않고, 이번 finding은 전용기기 Lock Task와 overlay 제한이 실제 `LOCKED` 결과에 묶여 있지 않은 경계로 한정했다.

#### 사실

- `KioskLockTaskController.kt:58-79`는 Device Owner·allowlist 허용 여부를 확인한 뒤 `DISALLOW_CREATE_WINDOWS`를 추가하고 `activity.startLockTask()`를 호출한다. 호출이 예외를 던질 때만 `:71-76`에서 restriction을 clear하며, 예외 없이 반환된 뒤 `currentMode() != LOCKED`이면 `Result.success(false)`만 반환하고 `:65-68`의 restriction은 그대로 둔다.
- `MainActivity.kt:879-894`의 `enterDedicatedMode()`는 `entered.isFailure`만 `dedicatedDevicePolicyFailed`에 반영한다. `Result.success(false)`와 `lockTaskController.status().mode`는 차단 조건으로 사용하지 않는다. `showAuthentication()` (`:661-692`), `showScanner()` 주변 (`:3045-3061`), PC pairing (`:2965-2972`)과 `onStart()` (`:3844-3855`)는 진입 결과와 무관하게 화면·카메라 흐름을 계속 진행한다.
- `DedicatedDevicePolicy.statusLabel()` (`:26-38`)은 Device Owner와 package/uninstall 보호가 정상이면서 mode가 `LOCKED`가 아니면 `"보안 준비 중"`을 반환한다. `PINNED`나 `NONE`을 정책 오류로 분류하지 않는다.
- `SessionPreflightPolicy.kt:3-32`의 `SessionPreflightInput`에는 `DedicatedDeviceMode`나 `DISALLOW_CREATE_WINDOWS` 적용 여부가 없고, `runSessionPreflight()` (`MainActivity.kt:2525-2555`)는 Device Owner·package permitted·Web 보호·`dedicatedDevicePolicyFailed`만 blocker로 전달한다. 따라서 startLockTask가 false를 반환해도 이 네 값이 정상이라면 수업 시작 판단은 통과할 수 있다.
- `scripts\verify-gate5-device-owner.ps1:19-27`은 activity dump에서 두 package와 `mLockTaskModeState=(\w+)`를 찾지만 mode가 `LOCKED`인지 검사하지 않는다. `:33-37`은 `NONE` 또는 `PINNED`도 오류 없이 출력한다. 반면 `scripts\provision-gate5-device-owner.ps1:71-81`과 release provisioning `:121-130`은 `mLockTaskModeState=LOCKED`를 exact check한다.
- `DedicatedDevicePolicyTest.kt`와 `SessionPreflightPolicyTest.kt`는 정상 `LOCKED`, administrator `NONE`, missing owner/package와 정책 blocker를 순수 함수로 검사하지만 `KioskLockTaskController`의 nonthrowing false 반환, `PINNED`, restriction 잔류 또는 verifier의 `NONE/PINNED` 오판을 검사하지 않는다. `kiosk/src/test`·`src/androidTest`에서 controller fault seam은 확인되지 않았다.
- controller와 `MainActivity` 관련 핵심 구간은 `ff8ec786`에서 도입됐고 이후 controller 자체의 후속 변경은 확인되지 않았다. `docs/GATE5_IMPLEMENTATION.md:74-94`, `docs/BUILD_VERIFICATION.md:6312-6316,6433-6435`와 최근 field 기록은 정상 `LOCKED` 경로만 증명한다. `SECURITY.md:54-58`도 QR/PIN/Web 구간의 Lock Task 유지와 overlay 제한을 기대 결과로 선언한다.

#### 추론과 반대 근거

- `startLockTask()`가 예외 없이 반환했지만 ActivityManager 상태가 `NONE` 또는 `PINNED`인 조건에서는 controller가 false를 반환해도 MainActivity가 `dedicatedDevicePolicyFailed`를 세우지 않고, `SessionPreflightPolicy`가 수업을 차단하지 않을 수 있다. 이때 `DISALLOW_CREATE_WINDOWS`만 남고 full `LOCKED` 경계가 성립하지 않아 홈·최근 앱·허용되지 않은 UI 이탈 방어가 정책 문서와 달라질 가능성이 있다. 실제 이 상태에서의 이탈은 확인하지 않았다.
- verifier가 `NONE/PINNED`를 성공 exit로 내보내면 운영자가 스크립트 성공을 Gate 5 `LOCKED` 확인으로 오인할 수 있다. 다만 출력에는 실제 mode 문자열이 포함되므로, 사람이 값을 읽어 `LOCKED`가 아님을 알아차릴 가능성은 남는다.
- Device Owner·allowlist·`LOCK_TASK_FEATURE_NONE`, 예외 발생 시 restriction cleanup, provisioning의 exact `LOCKED` 검사, 정상 A의 반복 `LOCKED` 기록과 status label의 `보안 준비 중` 표시는 반대 근거다. 따라서 현재 실제 실패 증거가 없는 조건부 검증·실패폐쇄 공백으로 P3 후보를 유지한다. 실제 false mode가 재현되면 전용기기 보안 경계 영향에 따라 심각도 재판정이 필요하다.

#### 가정과 미검증

- ADB·실제 A 화면·Device Owner 상태 변경·Lock Task start/stop·PIN·홈/최근 앱/알림창 이탈·overlay 생성은 실행하지 않았다.
- `startLockTask()` nonthrowing false, `PINNED`, `NONE` 전환, `DISALLOW_CREATE_WINDOWS` 적용·잔류·clear 실패, activity lifecycle race와 verifier 출력은 fault injection 또는 runtime으로 재현하지 않았다.
- ADB executable이 현재 PATH에 없어 최신 기기의 `mLockTaskModeState`와 overlay restriction을 확인하지 못했다. tests, lint, typecheck, build, install도 실행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 보호 구간 진입은 `Result.success(true)`와 실제 `LOCKED` 및 overlay restriction 적용을 모두 충족해야 하며, false/PINNED/NONE이면 화면·수업 시작을 실패폐쇄하고 restriction을 정리하거나 명시적 복구 상태를 보여줘야 한다. 읽기 전용 verifier도 `LOCKED`가 아니면 실패해야 한다.
- 실제: controller는 false를 반환할 수 있지만 caller가 false를 무시하고, preflight에 mode가 없으며, verifier는 mode 존재만 확인한다. 정상 경로의 `LOCKED` 기록과 provisioning exact check는 있으나 controller false 경로의 end-to-end 폐쇄는 확인되지 않았다.

#### 영향·재판단 및 후속 검증

- 전용기기에서 Lock Task가 실제로 `NONE/PINNED`인 조건으로 수업·QR/Web 흐름이 진행되면 홈·최근 앱·overlay 제한 계약이 약화될 수 있어 `LUNA-0022` P3 후보를 유지한다. 실제 이탈·데이터 노출은 확인하지 않았으므로 현재 심각도를 상향하지 않는다.
- 수정 후에는 controller를 주입 가능한 fake DPM/ActivityManager로 분리해 exception, nonthrowing `NONE`, `PINNED`, successful `LOCKED`, restriction add/clear 실패를 각각 검증하고, MainActivity가 false를 차단하는지와 session preflight·verifier가 exact `LOCKED`를 요구하는지 확인해야 한다. 다음 읽기 전용 감사는 `LUNA-0023` QR renderer 임시 표현·Bitmap 소유권 경계다.

#### 기준선과 보고서 상태

- 누적 보고서만 갱신했으며 source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 산출물을 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.

### 2026-08-02 20:46:05 — LUNA-0023 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0023`을 QR renderer·단건/배치 호출부·인쇄/PDF 소유권 경계·시험·문서·Git 이력으로 심화 재확인했다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- 이번 finding은 반환 후 정리되는 `Bitmap`과 반환 전에만 존재하는 `BitMatrix`·pixel `IntArray`의 경계를 분리해 다룬다. PDF cache·외부 인쇄 대상의 보존 경계와 QR token hash/credential lifecycle은 이 회차의 직접 범위로 확장하지 않았다.

#### 사실

- `kiosk/src/main/java/com/local/matholickiosk/kiosk/qr/QrImageRenderer.kt:11-34`의 `render(payload, sizePixels)`는 `sizePixels >= 256`만 확인한다. ZXing `MultiFormatWriter`로 QR `BitMatrix`를 만들고, `IntArray(sizePixels * sizePixels)`에 검정/흰색 ARGB 값을 채운 뒤 새 `Bitmap.Config.ARGB_8888`에 `setPixels()`로 복사한다. matrix와 pixels는 renderer의 local 변수이며 반환값은 Bitmap 하나이고, matrix·pixels를 `finally`에서 `fill`하거나 clear하는 코드와 최대 크기 상한은 없다.
- 현재 앱의 production 호출부는 `MainActivity.kt:1360,1416,1949,2060`에서 `QR_SIZE_PIXELS = 720`을 사용한다. 따라서 pixel `IntArray`는 518,400개·약 2,073,600바이트이며 ZXing matrix와 Bitmap이 추가로 존재한다. 이번 source search에서 production `sizePixels`를 외부 입력으로 받는 호출은 확인하지 않았다.
- preview 생성은 Activity 종료 callback에서 `wipeQrPreview()`로 정리하고, 새 preview·`onStop()`·인쇄/PDF 전달 성공 경로에서도 기존 화면 Bitmap을 지운다. `prepareQrPrint()`는 인쇄용 복제 Bitmap을 `QrPrintDocumentAdapter`에 넘기고 PrintManager 호출 실패 시 직접 지운다. PDF·PC 전송은 `executeSensitive`와 `QrPdfExporter.consumeSensitiveBitmap()`을 중첩해 사용하며, `QrPdfExporter.export()`는 성공·실패 모두 `finally`에서 `releaseSensitiveBitmap()`을 호출한다.
- batch PDF는 `BatchQrPdfExporter.export()`의 `finally`에서 모든 카드 Bitmap을 정리한다. batch 인쇄는 예외·Activity 종료·PrintManager 호출 실패에서 추가 정리하고 `BatchQrPrintDocumentAdapter.onFinish()`에서 카드 Bitmap을 지운다. 단건 인쇄도 `QrPrintDocumentAdapter.onFinish()`에서 소유 Bitmap을 흰색으로 덮고 recycle한다.
- `QrPrintDocumentAdapterInstrumentedTest.kt:33-46`은 export 전 고의 예외에서 `consumeSensitiveBitmap()`의 Bitmap recycle을 검사하고, `:209-306`은 renderer로 만든 720 Bitmap의 PDF 생성·기하·`onFinish()` 후 recycle을 검사한다. `SensitiveTaskTest`는 queued task discard/operation exception 시 cleanup 1회성을 검사한다. renderer 자체의 allocation failure, 배열 내용 zeroize, 상한, heap 잔류를 직접 검사하는 시험은 확인되지 않았다.
- renderer 도입 이력은 `e093bd0`이며, `0fc357c`는 pre-export 복제 Bitmap 소유권을, `bfbfaf4`는 abandoned task cleanup을 보강했다. 이후 이 두 보강은 반환 Bitmap·작업 cleanup 경계를 다루며 renderer 내부 `BitMatrix`·pixel 배열 zeroize를 추가하지 않았다. `SECURITY.md`의 화면 QR 원문·bitmap 삭제 문구도 이 반환 Bitmap 경계를 설명한다.

#### 추론과 반대 근거

- `encode()`, pixel 배열 할당/채움, Bitmap 생성 또는 `setPixels()`가 예외를 내거나 반복 렌더링이 메모리 압박을 일으키면, renderer에는 matrix·pixels를 명시적으로 덮어쓰는 예외 경로가 없어 해당 배열은 stack이 풀리고 GC될 때까지 payload에서 파생된 QR 패턴을 보유할 수 있다. `sizePixels` 상한이 없어 향후 호출이 커지면 allocation failure·정수 overflow·압박 가능성도 커진다. 이는 코드상 cleanup 계약의 부재에 대한 추론이며 실제 잔류 시간을 의미하지 않는다.
- 반대로 정상 반환 후 renderer가 matrix·pixels를 field나 별도 owner로 보유한다는 근거는 없고, 실제 앱 호출 크기는 720으로 고정되어 있다. 반환 Bitmap은 preview, 단건 인쇄, PDF/PC 전송, batch PDF/인쇄 및 queued task 경계에서 여러 `finally`·`onFinish()`·`onStop()` 경로로 정리되며, 관련 계측·단위시험과 `0fc357c`/`bfbfaf4` 이력이 존재한다. 따라서 현재 확인된 것은 renderer 내부 임시 표현의 결정적 zeroize 공백이지, 정상 인쇄 경로의 Bitmap 누수나 실제 QR 노출의 증거가 아니다.
- PrintDocumentAdapter Bitmap 정리는 Android 인쇄 lifecycle의 `onFinish()` 호출을 전제로 한다. 이번 정적 대조에서는 인쇄 서비스가 중단되어 `onFinish()`가 오지 않는 fault와 그 뒤의 heap/native 복사본을 확인하지 않았다.

#### 가정과 미검증

- Android runtime, 실제 QR 발급/인쇄/PDF/PC 전송, Activity 재생성, 인쇄 서비스 중단, renderer 예외·OOM·큰 `sizePixels`, heap dump/profiler/GC 시점 측정은 실행하지 않았다.
- ADB executable이 현재 PATH에 없어 A device의 메모리·인쇄 서비스 lifecycle·실제 화면과 QR 잔류를 확인하지 못했다. tests, lint, typecheck, build, install도 실행하지 않았다.
- ZXing/Bitmap provider의 내부 native allocation과 `setPixels()` 이후 복사본의 수명은 source 정적 대조만으로 확정하지 않았다.

#### 기대 결과와 실제 결과

- 기대: payload에서 파생된 모든 임시 표현은 bounded allocation을 사용하고 성공·예외·취소 경로에서 명시적 zeroize 또는 최소 수명 회수 계약을 가지며, 반환 Bitmap owner와 PrintDocument lifecycle도 fault 시험으로 고정해야 한다.
- 실제: 반환 Bitmap의 owner cleanup은 구현·시험으로 넓게 덮여 있지만, `BitMatrix`와 `IntArray`는 renderer local scope에만 맡겨져 있고 zeroize·상한·allocation failure 시험이 없다. 현재 고정 720 정상 흐름에서 실제 잔류나 메모리 장애는 확인되지 않았다.

#### 영향·재판단 및 후속 검증

- renderer가 반환 전에 실패하거나 비정상적으로 큰 렌더링이 추가되면 로그인 가능한 QR의 파생 이미지가 deterministic wipe 없이 GC 대상이 될 수 있어 `LUNA-0023` P3 후보를 유지한다. 실제 heap 잔류·노출·서비스 장애를 확인하지 않았으므로 심각도를 상향하지 않는다.
- 수정 후에는 size upper bound/overflow, ZXing·Bitmap 생성 및 `setPixels()` 예외, batch 중간 실패, Activity 종료, PrintDocument cancel/서비스 중단을 주입하고 matrix·pixel buffer·반환 Bitmap의 cleanup과 allocation budget을 함께 검증해야 한다. 다음 읽기 전용 감사 대상은 `LUNA-0024` 관리자 실행취소 lifetime·교차 작업 invalidation 경계다.

#### 기준선과 보고서 상태

- 이번 회차에는 누적 보고서만 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`를 변경하지 않았다.
- `git diff --check -- kiosk webpoc pc_receiver scripts`는 통과했다. branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이며 Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.

### 2026-08-02 20:54:00 — LUNA-0024 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0024`를 `MainActivity`의 undo 상태·모든 관리자 mutation 호출부·single executor·`StudentRepository` guard·layout·시험·운영 문서·Git 이력으로 심화 재확인했다. 심각도 `P4`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- `LUNA-0025`의 활성 session membership eligibility 영향과 `LUNA-0035/0036`의 공통 gate·bulk output ordering은 중복 집계하지 않고, 이번 finding을 stale undo action의 lifetime·교차 mutation invalidation·현재 상태 검증 부재로 한정했다.

#### 사실

- `MainActivity.kt:1507-1524`의 `offerAdminUndo()`는 `pendingAdminUndo`를 세 reversible 성공 경로인 반 삭제·반 소속 변경·학생 이름 변경에서만 설정하고, `adminUndoGeneration`을 timer 슬롯 식별자로만 사용한다. `clearPendingAdminUndo()` 호출은 30초 timer와 사용자가 `performPendingAdminUndo()`를 누른 경계에만 존재하며, action에는 DB mutation revision·생성 시점의 expected current state·작업 token이 없다.
- `MainActivity.kt:1093-1218,1335-1499,1633-1714,1849-1868`의 반 생성·CSV 적용·학생 등록·QR 재발급·자격정보 변경·비활성화 성공 경로는 이전 `pendingAdminUndo`를 지우지 않는다. 새 반 삭제·소속 변경·이름 변경 성공은 새 action으로 슬롯을 교체하지만, 실행취소 불가 작업은 “방금 작업” 버튼에 남은 이전 action을 그대로 둔다.
- `showAuthentication()` (`MainActivity.kt:661-700`)와 `showAdmin()` (`:764-783`)은 화면 panel만 바꾸고 `clearPendingAdminUndo()`를 호출하지 않는다. `onStop()` (`:3823-3836`)도 카메라·QR preview·PIN 입력만 정리하며 pending undo를 폐기하지 않는다. 따라서 Activity가 짧게 background/재인증·관리자 재진입을 거치면 30초 안의 이전 action이 다시 표시될 수 있다. layout의 `undo_admin_button` 기본 visibility는 `gone`이지만, action이 남은 동안 parent admin panel이 숨겨졌다가 다시 보이면 `showAdmin()`이 이를 별도로 숨기지 않는다.
- `updateStudentManagementControls()` (`MainActivity.kt:1733-1745`)는 `studentMutationGate`로 학생 spinner·등록·CSV·QR·자격정보·비활성화·card controls만 갱신한다. `undoAdminButton`은 이 gate에 포함되지 않으며, `updateClassRosterUi()`와 `webRecoveryGate`에도 연결되지 않는다. 따라서 학생 mutation·CSV·recovery 중에도 pending undo의 UI 클릭이 별도 차단되지 않는다.
- `ioExecutor`는 `Executors.newSingleThreadExecutor()` 하나를 공유한다 (`MainActivity.kt:161`). `performPendingAdminUndo()`는 action을 먼저 clear한 뒤 같은 executor에 DB 복원을 제출하지만, executor의 직렬성만 의존하고 후속 mutation이 action 생성 당시의 상태와 일치하는지 재검증하지 않는다. `refreshAdminData()`의 class/student selection snapshot은 목록 선택 callback을 보호할 뿐 `pendingAdminUndo`의 action revision을 보호하지 않는다.
- `StudentRepository.importStudents()` (`StudentRepository.kt:307-422`)는 수업이 없고 CSV 반 이름이 유효하면 기존 username을 매칭해 이름·암호화 자격정보를 갱신하고, 각 기존 학생의 membership을 지운 뒤 CSV 반 소속을 다시 쓴다. 이름이 바뀌면 `needsPrint`도 갱신하지만 undo action과 연결된 revision은 기록하지 않는다. `MainActivity.applyStudentCsv()` (`:1849-1868`)는 `studentMutationGate`나 `clearPendingAdminUndo()` 없이 import 후 `refreshAdminData()`만 호출한다.
- `StudentRepository.updateStudentProfile()` (`:490-511`)는 학생이 active인지와 새 이름 형식만 확인하고, `replaceClassMemberships()` (`:513-531`)는 active class·active student만 확인한 뒤 membership을 transaction으로 교체한다. 두 API 모두 호출자가 기대하는 현재 이름·현재 membership snapshot·mutation generation을 비교하지 않는다. 따라서 이름 변경 A→B 후 CSV가 같은 학생을 C로 갱신하고 이전 undo가 실행되면 A로 되돌리는 순서, 또는 membership A→B 후 CSV가 C로 갱신하고 이전 undo가 A를 복원하는 순서가 정적으로 성립한다. 실제 변경은 실행하지 않았다.
- `AdminUiAsyncStateTest.kt`는 class/student selection revision과 `SingleFlightGate` 중복 시작만 검사한다. 테스트·문서 검색에서 `undo_admin_button`, `performPendingAdminUndo()` 또는 pending action과 CSV/credential/QR/deactivation/화면 재진입을 결합한 회귀시험은 확인되지 않았다. 현장 문서의 실행취소 PASS는 학생 추가·제거·이름 변경·테스트반 삭제를 각각 곧바로 되돌리고 30초 만료를 확인한 isolated 순서이며, 후속 mutation 교차 순서를 포함하지 않는다.
- undo 구현은 버튼 클릭 즉시 action을 clear해 중복 클릭·기존 timer의 재실행을 막고, `RestoreClass`는 active session·동일 이름·비활성 student를 확인한다. 이는 일부 stale restore를 거부하는 반대 근거지만, `RestoreStudentName`·`RestoreMemberships`의 expected-current 검증은 아니다. 관련 undo 구현·시험 변경 이력은 `0193d89` 도입 commit에 집중되어 이후 공통 invalidation 보강은 확인되지 않았다.

#### 추론과 반대 근거

- 구체적 교차 순서에서 CSV 적용 callback이 끝난 뒤 30초 안에 이전 undo를 누르면, single executor는 CSV transaction을 먼저 끝낸 다음 stale `updateStudentProfile()` 또는 `replaceClassMemberships()`를 실행할 수 있다. 두 repository API가 현재 상태를 비교하지 않으므로 마지막 사용자 의도인 CSV 결과가 UI 문구 “방금 작업 실행취소”에 의해 이전 이름·소속으로 되돌아갈 가능성이 있다. 이는 source ordering과 API precondition에 기반한 정적 추론이며 실제 데이터 변경·사용자 오조작은 확인하지 않았다.
- 화면을 잠깐 떠났다 돌아오는 경우에도 pending action이 같은 Activity field에 남고, `showAdmin()`이 action의 age나 현재 선택을 재확인하지 않는다. 다만 정상적으로 30초가 지나면 handler가 `adminUndoGeneration`을 비교해 button과 field를 지우며, `performPendingAdminUndo()`도 즉시 clear하므로 단순 중복 실행은 제한된다.
- CSV·credential·QR·deactivation 작업은 UI 일부가 disabled 또는 별도 단일 실행 gate를 사용하고 repository transaction도 원자적이지만, undo button은 그 gate 밖이다. `restoreClass()`의 session/name/student guard와 isolated S1 field PASS는 stale restore의 일부 실패·정상 사용을 설명하지만 cross-operation atomicity나 current-state binding을 증명하지 않는다.

#### 가정과 미검증

- 실제 학생·CSV·반·membership·이름 변경·undo·관리자 PIN·Activity background/re-entry·Activity recreation·active session·DB fault는 실행하지 않았다.
- CSV 적용 뒤 undo, non-reversible mutation 뒤 undo, 두 mutation의 빠른 연속 클릭, refresh callback 교차 순서, executor queue rejection/shutdown, 실제 UI 버튼 enabled 상태는 runtime·fault injection·ADB로 확인하지 않았다.
- ADB executable이 현재 PATH에 없어 A 화면과 설치 앱의 실제 lifecycle·현장 조작을 확인하지 못했다. tests, lint, typecheck, build, install도 실행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: undo는 정말 직전 작업에만 제공되고, 다른 mutation 시작·성공·화면 이탈·재인증·세션 경계에서 이전 action을 폐기하거나, 복원 API가 action 생성 당시의 expected current state/revision을 검증해 stale overwrite를 거부해야 한다. undo button도 관련 mutation/recovery gate와 일관되게 차단되어야 한다.
- 실제: 30초 timer와 직접 클릭 clear는 구현되어 있으나, non-reversible CSV 등 후속 작업·lifecycle·session/recovery가 pending action을 무효화하지 않는다. repository 복원 API에는 current-state/revision binding이 없고, isolated field 시나리오와 일부 guard만 확인된다.

#### 영향·재판단 및 후속 검증

- CSV 또는 다른 관리자 mutation 직후 이전 undo가 실행되면 최신 이름·반 소속을 과거 snapshot으로 덮을 가능성이 있어 `LUNA-0024` P4 후보를 유지한다. 실제 데이터 손상이나 학생 자격정보 변경은 확인하지 않았으므로 P3 이상으로 상향하지 않는다.
- 수정 후에는 mutation generation/expected snapshot을 action과 repository에 연결하거나 모든 후속 mutation·lifecycle에서 pending action을 폐기하는 정책을 정하고, CSV↔name undo·CSV↔membership undo·credential/QR/deactivation 뒤 undo·화면 재진입·active session·실패/재시도·빠른 연속 클릭을 합성 데이터로 회귀시험해야 한다. 다음 읽기 전용 감사 대상은 `LUNA-0025` 활성 수업 중 pending membership undo와 QR/수동 선택 eligibility 경계다.

#### 기준선과 보고서 상태

- 이번 회차에는 누적 보고서만 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`를 변경하지 않았다.
- `git diff --check -- kiosk webpoc pc_receiver scripts`는 통과했으며, branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.

### 2026-08-02 21:03:08 — LUNA-0025 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0025` 활성 수업 membership·pending undo·eligibility 경계를 `MainActivity`의 session start/re-entry 순서, repository·DAO query, 관련 시험·현장 문서·Git 이력으로 재확인했다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- 이번 회차는 기존 pending `RestoreMemberships` 우회에 더해, 수업 시작 callback과 관리자 재진입의 async refresh 전까지 stale enabled 반 구성 UI가 남을 수 있는 별도 정적 경로를 확인했다. `LUNA-0024`의 일반 stale undo lifetime, `LUNA-0026`의 종료 후 snapshot 실패, `LUNA-0035/0036`의 recovery·bulk 공통 gate와는 범위를 분리했다.

#### 사실

- `completeSessionStart()` 성공 callback은 `currentSession = it` 뒤 `showScanner()`를 호출하지만 `updateSessionAdminControls()`나 `updateClassRosterUi()`를 동기 호출하지 않는다(`MainActivity.kt:2824-2845`, `:3031-3058`). `showScanner()`는 admin panel을 숨길 뿐 이전 admin controls의 enabled 상태를 초기화하지 않는다.
- 세션 PIN 성공 뒤 `showAdmin()`은 admin panel을 먼저 visible로 만들고 `refreshAdminData()`를 단일 `ioExecutor`에 제출한다(`MainActivity.kt:764-781`, `:3588-3718`). `applyAdminDataSnapshot()`이 완료되어야 `currentSession`·`updateSessionAdminControls()`·`updateClassRosterUi()`가 active-session 잠금을 적용한다(`MainActivity.kt:913-1017`).
- `showClassMembershipDialog()`는 선택 반·학생 존재만 확인하고 현재 session을 확인하지 않는다(`MainActivity.kt:1165-1190`). `replaceClassMemberships()` handler와 `performPendingAdminUndo()`도 `studentMutationGate`·`webRecoveryGate`·active session을 확인하지 않으며, repository는 active class·active student만 검사한 뒤 membership을 transaction으로 교체한다(`MainActivity.kt:1193-1221,1526-1548`, `StudentRepository.kt:513-531`).
- pending `RestoreMemberships`는 수업 시작·`showScanner()`·세션 관리자 `showAdmin()` 경계에서 지워지지 않고, undo button도 `updateSessionAdminControls()`·`updateClassRosterUi()`의 gate 대상이 아니다. 정상 refresh 뒤 일반 반 구성·class spinner는 잠기지만 pending action은 별도 상태로 남는다.
- `startSession()`은 `session_students`에 temporary ID만 기록하고 현재 class membership snapshot을 만들지 않는다(`StudentRepository.kt:648-689`). `StudentDao`의 QR·수동 선택 query는 현재 `class_memberships` 또는 해당 session의 `session_students`를 live join한다(`Daos.kt:26-66`).
- repository 계측시험은 membership을 session 시작 전에 설정하고 시작 후 정상 eligibility만 확인한다. `RepositoryInstrumentedTest`·`MainActivityInstrumentedTest`·현장 S1 문서에는 session 시작 후 pending undo 또는 refresh 전 membership click 순서가 없다. `offerAdminUndo()` 도입과 `replaceClassMemberships()` 도입 이후 active-session guard를 추가한 후속 history도 확인되지 않았다.

#### 추론과 반대 근거

- `[A] → [B]` membership 변경 직후 수업을 시작하고 30초 안에 세션 관리자 PIN으로 재진입해 pending undo를 누르면, 같은 반의 `class_memberships`가 `[A]`로 되돌아갈 수 있다. 이후 QR·수동 선택 query는 live membership을 읽으므로 `[B]` 학생이 eligibility에서 빠지거나 `[A]` 학생이 들어올 수 있다. 이미 `session_students`에 임시 추가된 학생은 OR 조건으로 남을 수 있다.
- 별도 순서로, 수업 시작 후 관리자 재진입에서 `refreshAdminData()` callback 전에 기존 enabled 반 구성 버튼을 누르면 새 dialog가 열리고 같은 무검사 repository 경로로 현재 반 membership을 교체할 수 있다는 정적 가능성이 있다. 정확한 지속 시간과 실제 클릭 성공은 확인하지 않았다.
- 단일 `ioExecutor`와 Room transaction은 작업 순서·부분 write를 제한하는 반대 근거지만, active-session precondition이나 session membership revision을 자동으로 추가하지 않는다. 정상 snapshot 이후 UI 잠금과 existing eligibility 시험은 정상 경로의 반대 근거이지 두 순서를 반증하지 않는다.

#### 가정과 미검증

- 제품 의도가 수업 시작 후 현재 반 membership을 고정하고 수업 중에는 temporary 학생만 허용한다는 해석은 UI·문서 흐름에 근거하지만, pending undo의 수업 중 허용 여부를 명시한 결정은 찾지 못했다.
- 실제 학생·반·membership·session·PIN·pending undo·QR·수동 선택, 세션 관리자 재진입, refresh callback 지연, stale enabled 버튼 클릭, DB 상태 변화는 수행하지 않았다. ADB executable도 PATH에 없다.
- tests, lint, typecheck, build, install, fault injection, 네트워크·Git 쓰기는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 수업 시작 시 pending membership undo를 폐기하고, active session 동안 membership dialog·handler·repository를 모두 거부하거나 session별 immutable roster를 사용해 QR·수동 선택 eligibility를 고정해야 한다. 관리자 재진입도 refresh 전 fail-closed control을 보여야 한다.
- 실제: 일반 UI는 snapshot 적용 뒤 잠기지만 pending undo는 남고 repository guard가 없다. session membership snapshot도 없으며, 재진입 전 async refresh 구간에는 이전 enabled state가 남을 수 있다. 따라서 `LUNA-0025` P3 후보·중간 신뢰도를 유지한다.

#### 영향 및 후속 검증

- 현재 수업 학생이 QR·수동 선택에서 사라져 로그인하지 못하거나, 이전 membership 학생이 허용될 가능성이 있다. 영향은 action/선택 반이 현재 session 반과 같은 경우 직접적이다.
- 수정 후에는 session 시작·재진입·refresh 전후의 pending undo/button state, same/different class membership, temporary 학생, QR/manual eligibility를 합성 데이터와 DB precondition 시험으로 고정해야 한다. 다음 읽기 전용 감사 대상은 `LUNA-0026`이다.

#### 기준선과 보고서 상태

- 이번 회차에도 누적 보고서만 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`를 변경하지 않았다.
- `git diff --check -- kiosk webpoc pc_receiver scripts`는 통과했으며 branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.

### 2026-08-02 21:09:18 — LUNA-0026 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0026`을 `completeSessionEnd()`·`refreshAdminData()`의 성공/실패 순서, `endSession()` transaction, stale control·scanner 경로, RC18 failure policy·시험·Git 이력으로 심화 재확인했다. 심각도 `P4`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- 이번 회차는 post-commit snapshot 실패 뒤 stale active UI가 단순 표시 불일치에 그치지 않고, QR_READY resume 후 `SESSION_NOT_READY` 거부/cooldown 반복과 중복 종료 재제출로 이어질 수 있는 정적 경계를 보강했다. Activity 재생성 중 EndSession action 유실은 `LUNA-0027`, 일반 refresh failure policy는 RC18 범위로 분리했다.

#### 사실

- `StudentRepository.endSession()`은 Room transaction 안에서 active `sessionId`를 확인하고 temporary 학생을 삭제한 뒤 `ADMIN_IDLE`·null session/class singleton을 저장하고 audit한다(`StudentRepository.kt:900-920`). duplicate end는 `진행 중인 수업이 없습니다.`로 거부된다.
- `MainActivity.completeSessionEnd()` 성공 callback은 `webRecoveryGate.finish()`와 `pendingTemporaryStudentIds` 정리 뒤 `currentSession`을 null로 바꾸지 않고 `refreshAdminData()`를 제출한다(`MainActivity.kt:2850-2868`). 종료 성공을 나타내는 별도 committed-idle projection이나 refresh 전용 상태는 없다.
- `refreshAdminData()`는 `ensureClasses()`·class/student/session/membership read를 하나의 `runCatching` snapshot으로 묶는다(`MainActivity.kt:913-971`). 하나라도 실패하면 snapshot을 적용하지 않고 기존 메모리 `currentSession`을 인자로 `updateSessionAdminControls()`·`updateStudentManagementControls()`·`updateClassRosterUi()`를 다시 호출한다.
- stale active `currentSession`이 QR_READY이면 `updateSessionAdminControls()`가 `resumeSessionButton`을 보이고 `startSessionButton`을 `현재 수업 안전 종료`로 남긴다. `showScanner()`는 Activity memory의 session ID/state만 검사하며 repository의 현재 DB session을 다시 읽지 않는다(`MainActivity.kt:2870-2888,3031-3061`).
- stale scanner에서 QR validation은 DB의 `ADMIN_IDLE`을 읽어 `SESSION_NOT_READY`로 거부한다. 이후 `recordQrRejection()`이 null session ID로 audit하고 성공하면 `resumeScannerAfterCooldown()`이 analyzer를 다시 켠다(`MainActivity.kt:3325-3345,3560-3569`, `StudentRepository.kt:729-817`).
- stale `startSessionButton`/`recoverSessionButton`을 누르면 `startOrEndSession()`이 메모리 active session을 보고 다시 EndSession recovery를 시작할 수 있고, 후속 `endSession()`은 이미 idle인 DB에서 실패한다(`MainActivity.kt:2490-2503,2804-2821`).
- RC18 계측시험은 일반 refresh failure·학생 gate 해제를, `RepositoryInstrumentedTest`는 정상/duplicate `endSession()`을 검사하지만, 종료 성공 직후 snapshot fault와 stale scanner/duplicate button의 UI 순서는 다루지 않는다. 관련 history는 `completeSessionEnd()` `2e4a14d`, snapshot failure policy `7786ee0`, `endSession()` `e093bd0`에 집중되고 post-commit idle projection 후속 변경은 확인되지 않았다.

#### 추론과 반대 근거

- active QR_READY session을 EndSession으로 종료한 뒤 `listClasses()`·`listStudents()`·`currentSession()`·membership read 중 하나가 실패하면 DB는 idle인데 Activity는 이전 active session을 유지할 수 있다. `refreshAdminData()`가 재시도 성공하기 전까지 반 선택 잠금과 종료/재개 버튼이 stale 상태로 남는다.
- 그 상태에서 resume을 누르면 memory-only scanner가 열리고, 실제 DB idle 때문에 QR은 계속 `SESSION_NOT_READY`로 거부되며 cooldown 뒤 scanner가 다시 열린다. 종료 버튼을 누르면 이미 종료된 DB에 duplicate end를 제출하는 실패 loop가 될 수 있다.
- 단일 `ioExecutor`는 end transaction 뒤 refresh task의 정상 순서를 보장하고 Room transaction은 partial commit을 막는다. 정상 refresh가 성공하면 `applyAdminDataSnapshot()`이 null session을 반영해 idle로 수렴한다. 따라서 데이터 손상이나 실제 세션 재생성으로 확대하지 않고 UI/가용성 P4로 유지한다.

#### 가정과 미검증

- 종료 transaction 성공 직후 관리자 snapshot read가 실패하는 일반 Room/I/O 예외 경계를 가정했으며, fault injection·실제 DB·A 화면은 사용하지 않았다.
- 실제 EndSession 성공→snapshot fault→stale button/scanner→QR rejection/cooldown 또는 duplicate end 조작과 Activity lifecycle timing은 확인하지 않았다. ADB executable은 PATH에 없다.
- tests, lint, typecheck, build, install, 네트워크·Git 쓰기는 수행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 종료 transaction 성공 즉시 Activity도 safe idle projection으로 전환하고, snapshot read 실패는 명시적 retry-required 상태로 표시해 이전 active control·scanner·duplicate end를 재사용하지 않아야 한다.
- 실제: 성공 callback은 stale `currentSession`을 유지한 채 snapshot에 의존하며, failure branch는 기존 active control을 재사용한다. stale QR_READY는 DB idle rejection/cooldown을 반복할 수 있어 `LUNA-0026` P4 후보·중간 신뢰도를 유지한다.

#### 영향 및 후속 검증

- 다음 수업 시작이 지연되고, 종료된 수업을 다시 종료하거나 QR 대기 화면에서 반복 거부를 겪을 수 있다. 정상 refresh 재진입·Activity 재생성은 완화 경로다.
- 수정 후에는 end success→snapshot fault, end failure→active 유지, retry success→ADMIN_IDLE, stale QR resume/rejection, duplicate end/recovery, temporary student cleanup과 next-session start를 각각 회귀시험해야 한다. 다음 읽기 전용 감사 대상은 `LUNA-0027`이다.

#### 기준선과 보고서 상태

- 이번 회차에도 누적 보고서만 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`를 변경하지 않았다.
- `git diff --check -- kiosk webpoc pc_receiver scripts`는 통과했으며 branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.

### 2026-08-02 21:17:06 — LUNA-0027 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0027`을 Kiosk `pendingRecoveryAction` 생성·선행 clear·Activity lifecycle, explicit Web recovery Intent/result payload, Web POC persistence·`onNewIntent()`·`onStop()` 분기, 관련 계측시험·운영 문서·Git history까지 읽기 전용으로 심화 재확인했다. 심각도 `P4`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- Kiosk manifest의 `configChanges=keyboardHidden|orientation|screenSize`·`singleTask`와 Web의 동일한 일반 구성 변경 완화는 반대 근거다. 따라서 회전·화면 크기 변경마다 action이 유실된다고 확정하지 않고, 저장되지 않은 외부 작업이 process reclaim·기타 Activity 재생성과 조건부로 교차하는 경계로 한정했다.

#### 사실

- Kiosk `pendingRecoveryAction`은 Activity 일반 필드이며 Web recovery Intent 직전에 `StartSession(classId, temporaryStudentIds)` 또는 `EndSession`을 보유한다. result callback은 action을 먼저 `None`으로 clear하고, `None`이면 일반 `refreshAdminData()`만 실행하며 Start/End일 때만 후속 session mutation을 제출한다(`kiosk/MainActivity.kt:201,310-334,2459-2487,3961-3968`).
- Kiosk `MainActivity`에는 `onSaveInstanceState()` 저장이나 saved-state/DB 복원이 없고 `onCreate()`의 새 action 기본값은 `None`이다. Intent에는 action 문자열·trusted Web component만 있으며 operation ID·class/session snapshot·revision·idempotency token이 없다(`kiosk/src/main/AndroidManifest.xml:35-41`, `kiosk/MainActivity.kt:337-371,2473-2480`).
- Web POC는 `onCreate(null)` 뒤 SharedPreferences `KEY_STATE`를 읽고, recovery `onNewIntent()`를 다시 시작할 수 있으며 configuration change 동안에는 `onStop()`에서 관리자 recovery를 실패 처리하지 않는다. 그러나 Web result는 `RESULT_OK`/`RESULT_CANCELED`와 제한된 failure reason만 반환하고 Kiosk 후속 action identity를 반환하지 않는다(`webpoc/src/main/AndroidManifest.xml:21-45`, `webpoc/MainActivity.kt:153-207,225-243,2356-2366,1575-1595`).
- `RecoveryInstrumentedTest`의 결과 검사는 untrusted secure-session caller failure이고 Web renderer recreation을 다루며, Kiosk `MainActivityInstrumentedTest`에는 `RECOVER_WEB_SESSION`·`pendingRecoveryAction`·`recreate()`를 결합한 Start/End action 보존 시험이 없다. 운영 문서도 재부팅 `RECOVERY_REQUIRED → 안전 종료 → 새 수업 시작` 정상 결과만 기록한다(`docs/BUILD_VERIFICATION.md:395-403`).
- Git history 검색에서는 외부 recovery 도입 `05e2603`, Kiosk action 도입 `2e4a14d`, StartSession 확장 `630fce1`을 확인했지만 대상 Kiosk `MainActivity.kt`의 `onSaveInstanceState` 추가 이력은 확인하지 못했다.

#### 추론과 반대 근거

- Kiosk caller Activity가 Web recovery 대기 중 process reclaim 또는 manifest가 흡수하지 않는 lifecycle 재생성을 겪고 외부 결과가 새 callback으로 전달되면, 새 action 기본값 `None`이 `RESULT_OK`를 일반 refresh로 소비해 Web 정리와 Kiosk session mutation이 갈라질 수 있다. Start는 수업 미생성, End는 Web `IDLE` 후 Kiosk active session 잔류가 가능한 정적 결과다.
- 같은 Activity에서 결과 callback이 실제로 중복 도착하면 첫 callback의 선행 clear 뒤 후속 결과는 `None` branch로 처리된다. 결과 identity/token이 없어 중복·지연 결과를 동일한 Start/End operation으로 판별하는 계약이 없다.
- Kiosk의 `configChanges`·`singleTask`, Web의 SharedPreferences state·`onCreate(null)`·configuration-change 분기, Kiosk DB transaction·single `ioExecutor`, 재시작 시 DB 초기 로드는 정상 구성 변경·partial write·재시작 후 수렴을 제한하는 반대 근거다. 이 보호들은 이미 유실된 Kiosk action을 재생성하거나 Web 성공 뒤 누락된 후속 mutation을 자동 실행하지 않는다.

#### 가정과 미검증

- ActivityResultRegistry가 caller Activity 재생성 뒤 pending result를 새 등록 callback에 전달할 수 있다는 조건부 Android lifecycle 경계를 분석했지만, 실제 Android 13 환경에서 재현하지 않았다.
- process reclaim·기타 Activity 재생성의 정확한 조건, 결과 delivery timing, 중복 delivery·재진입 순서, Web 상태·Kiosk session/학생/DB의 실제 값은 확인하지 않았다. 일반 orientation·screen-size·keyboard 구성 변경이 manifest `configChanges`로 흡수되는지의 실제 기기 동작도 실행하지 않았다.
- 테스트·lint·typecheck·build·install·fault injection·ADB를 실행하지 않았고, source/tests/docs/settings/scripts/build artifacts·기기 상태·Git 이력은 변경하지 않았다.

#### 기대 결과와 실제 결과

- 기대: Web recovery 완료 전후에도 Kiosk가 operation identity와 Start/End 인자를 durable하게 보존하고, Activity 재생성·결과 중복 뒤 expected state를 확인해 후속 DB 전이를 정확히 한 번만 실행해야 한다.
- 실제: Kiosk action은 Activity memory에만 있고 result payload와 durable record에 identity가 없다. 새 callback의 `None` 또는 선행 clear 뒤 중복 결과는 후속 session mutation 없이 일반 refresh로 빠질 수 있어 `LUNA-0027` P4 후보·중간 신뢰도를 유지한다.

#### 영향 및 후속 검증

- Web 로그인 상태는 정리됐다고 보이지만 Kiosk 수업 시작이 실행되지 않거나, Web은 `IDLE`인데 Kiosk의 기존 수업·`session_students`가 남아 운영자가 상태를 다시 확인해야 할 수 있다. 실제 데이터 불일치나 사용자 발생은 확인하지 않았다.
- 수정 후에는 Kiosk Start/End 각각에 대해 configuration change(완화 확인), process reclaim·Activity recreation, `RESULT_OK`/`RESULT_CANCELED`, callback 중복·지연, class/session revision 불일치, executor submit 실패를 fault seam과 계측시험으로 고정하고 Web `onNewIntent()`·`onStop()`의 caller action 결과를 함께 확인해야 한다. 다음 읽기 전용 감사 대상은 `LUNA-0028`이다.

#### 기준선과 보고서 상태

- 이번 회차에도 누적 보고서만 `apply_patch`로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`를 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.

### 2026-08-02 21:24:49 — LUNA-0028 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0028`을 `AdminAuthRepository`의 bootstrap/enroll/authenticate 호출, `PinVerifier`·`AdminCredentialEntity` ByteArray alias, `SensitiveTask` cleanup, Room DAO/schema·migration·시험·backup 경계와 `e093bd0` history까지 읽기 전용으로 심화 재확인했다. 심각도 `P4`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- 입력 PIN·PBKDF2 candidate 정리, `SensitiveTask`의 queued cleanup, DB backup/device-transfer 제외는 반대 근거다. 이번 finding은 raw PIN 저장·일반 사용자 원격 노출·DB backup 복제 주장이 아니라, same-process/승인된 debug·heap 전제에서 persisted verifier와 transient ByteArray의 소유권·수명 계약이 없는 공백으로 한정한다.

#### 사실

- `isEnrolled()`·`enrolledPinLength()`·`enroll()`의 이미 등록됨 검사·`authenticate()`는 모두 `AdminDao.get()`의 `SELECT *` full `AdminCredentialEntity`를 읽는다. 초기 bootstrap은 두 scalar 목적을 위해 연속 두 번 조회하고, `authenticate()`는 lockout 조기 반환에서도 entity의 `salt`·`derivedKey`를 보유한다(`AdminAuthRepository.kt:17-25,45-57`, `MainActivity.kt:589-606`).
- `enroll()`은 `AdminPin.create()`가 반환한 `PinVerifier` 배열을 `AdminCredentialEntity`에 직접 넣고, `authenticate()`는 entity 배열을 `PinVerifier`에 직접 alias한다. `entity.copy()`는 배열 deep-copy/clear 계약 없이 save에 전달되며 repository `finally`는 입력 PIN만 지운다(`AdminAuthRepository.kt:23-81`, `Entities.kt:130-145`).
- `AdminPin.verify()`는 candidate derived key만 `fill(0)`하고 `PBEKeySpec.clearPassword()`를 수행한다. `PinVerifier`와 `AdminPin.create()`에는 persisted copy와 transient owner를 분리하거나 verifier를 명시적으로 clear하는 API가 없다(`AdminPin.kt:9-59`). create 중 salt 할당 뒤 `derive()` 예외를 정리하는 경계도 없다.
- `SensitiveTask`와 `MainActivity.executeSensitive()`는 입력 PIN cleanup을 task 실행·discard·executor rejection·destroy 경계에서 보장하지만, repository 내부 verifier/entity 배열을 cleanup callback에 연결하지 않는다(`SensitiveTask.kt:5-22`, `MainActivity.kt:3859-3887`).
- Kiosk manifest와 data-extraction rules는 `allowBackup=false`, `fullBackupContent=false`, cloud/device-transfer database exclusion을 선언한다. Room v2→v3 migration은 기존 verifier BLOB를 유지하고 `pinLength DEFAULT 0`만 추가하며, migration 시험은 합성 BLOB 보존과 schema를 확인할 뿐 배열 ownership을 관찰하지 않는다(`AndroidManifest.xml:25-34`, `data_extraction_rules.xml:2-16`, `KioskDatabase.kt:87-94`, `KioskDatabaseMigrationInstrumentedTest.kt:61-90`).
- `AdminPinTest`, `SensitiveTaskTest`, MainActivity PIN/UI 계측시험과 migration 시험은 형식·PBKDF2 결과·lockout·입력 cleanup·UI·schema를 각각 검사하지만 repository full-row/scalar query, Room cursor/binding copy, verifier clear와 heap 수명을 종단 연결하지 않는다. 관련 구현은 `e093bd0`에서 도입됐고 `9af507b`는 PIN length field를 추가했지만 verifier ownership 후속 변경은 history에서 확인하지 못했다.

#### 추론과 반대 근거

- duplicate enroll의 기존 credential 검사와 lockout 중 authenticate의 early return은 verifier를 구성하지 않아도 full BLOB entity가 반환·예외 경계까지 남는 경로다. 성공·실패 인증의 shallow `copy()`와 Room save 예외가 결합하면 entity·verifier·binding 주변의 참조가 명시적으로 분리·wipe됐다고 볼 근거가 없다.
- 정상 PBKDF2 경로에서는 입력 PIN, `PBEKeySpec` password와 candidate key가 정리되고, backup/device-transfer exclusion은 외부 복제 경로를 줄인다. salt는 raw PIN이 아니며 derivedKey는 verifier이므로 실제 heap 접근·승인 debug·동일 process 메모리 노출이 없는 운영 경로를 P2 이상으로 확대하지 않는다.
- Room cursor/binding이 native/Java 배열을 추가 복사할 수 있고 ART GC가 즉시 회수하지 않을 수 있다는 점은 조건부 추론이다. 실제 generated code·heap graph·provider fault를 실행하지 않았으므로 P4 후보를 유지한다.

#### 가정과 미검증

- Room generated cursor/statement binding, JVM/ART ByteArray identity·복사 횟수·GC 시점과 DB write 예외 뒤 실제 참조 graph는 확인하지 않았다.
- `AdminPin.create()`의 provider/derive 예외, duplicate enroll, lockout early return, executor shutdown/rejection 뒤 verifier 배열 잔류를 실행하지 않았다. backup/device-transfer exclusion의 설치·OS 적용도 정적 선언만 읽었다.
- 실제 PIN 입력·DB 저장/인증·heap dump·GC 유도·ADB·테스트·lint·typecheck·build·install은 수행하지 않았고, source/tests/docs/settings/scripts/build artifacts·기기 상태·Git 이력은 변경하지 않았다.

#### 기대 결과와 실제 결과

- 기대: scalar-only bootstrap 조회와 명확한 `PinVerifier`/entity ownership을 사용하고, persisted verifier를 손상시키지 않는 transient clone을 비교·Room binding·예외·executor 종료 뒤 정리하며 이를 배열 identity/cleanup 회귀시험으로 고정해야 한다.
- 실제: input/candidate cleanup과 backup exclusion은 있지만 full entity 조회·shallow ByteArray alias·local verifier와 Room binding의 수명 소유자가 명시되지 않았다. 따라서 `LUNA-0028` P4 후보·중간 신뢰도를 유지한다.

#### 영향 및 후속 검증

- 승인된 debug/heap 분석 또는 same-process 메모리 노출 조건에서 관리자 verifier 잔류가 오프라인 PIN 추측 검증에 필요한 자료의 수명을 늘릴 수 있다. 실제 heap 노출·PIN 탈취·운영 사용자 영향은 확인하지 않았다.
- 수정 후에는 scalar-only `isEnrolled`/length, duplicate enroll, lockout early return, authenticate success/failure, enroll/auth DB save exception, `AdminPin.create` derive failure, Room cursor/binding copy와 `onDestroy`/executor rejection을 synthetic verifier와 배열 identity/zeroization 관찰로 회귀 검증해야 한다. 다음 읽기 전용 감사 대상은 `LUNA-0029`다.

#### 기준선과 보고서 상태

- 이번 회차에도 누적 보고서만 `apply_patch`로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`를 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.

### 2026-08-02 21:30:55 — LUNA-0029 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0029`를 `LoopbackConnectProxy`의 CONNECT 승인·소켓/worker lifecycle, `CloseableRegistry`·rejection cleanup, `ProxyBootstrapCoordinator` 상태기계, WebView error/recovery·process-wide bootstrap, 관련 시험·RC26~RC28 문서·Git history까지 읽기 전용으로 심화 재확인했다. 심각도 `P4`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- 기존 resource budget·idle tunnel 공백에 더해 accept loop health loss 뒤 `READY` 상태와 dead proxy가 분리될 수 있는 조건부 복구 공백을 같은 process-wide proxy lifecycle 범위로 보강했다. host allowlist·TLS pass-through·loopback bind·shutdown/rejection cleanup은 반대 근거다.

#### 사실

- `LoopbackConnectProxy`는 `127.0.0.1:0`에 bind하고, client마다 cached thread pool task를 만들며, 허용된 CONNECT마다 upstream socket과 반대 방향 copy task를 추가한다. 성공한 client/upstream의 `soTimeout`은 0이고 idle timeout·총 socket/worker budget이 없다(`LoopbackConnectProxy.kt:44-59,61-136,172-196`).
- CONNECT는 443 포트와 `.matholic.com` 또는 두 exact host로 제한되고 TLS를 종료·복호화하지 않는다. header 8KiB, pre-connect/initial I/O 10초, backlog 16과 `CloseableRegistry`의 등록·종료 직렬화가 정상/부분 실패 범위를 제한한다(`LoopbackConnectProxy.kt:17-38,81-100,139-159`).
- `acceptLoop()`는 `ServerSocket.accept()`의 `IOException`을 잡고 return하며 coordinator·diagnostic event·health retry를 호출하지 않는다. `ProxyBootstrapCoordinator`는 성공 후 `READY`를 유지하고 `WebViewProxyBootstrap.ensureConfigured()`는 기존 `READY`를 즉시 callback하므로 dead accept loop 뒤 proxy 재생성 경계가 없다(`LoopbackConnectProxy.kt:61-79`, `ProxyBootstrapCoordinator.kt:42-57`, `WebViewProxyBootstrap.kt:55-62`).
- Web recovery 버튼의 `restartForRecovery()`는 Activity만 `recreate()`하고, 새 Activity도 process-wide coordinator의 `READY`를 재사용한다. WebView main-frame error는 network pause 또는 `showLocked()`를 선택하지만 proxy bootstrap restart를 호출하지 않는다(`MainActivity.kt:564-601,839,1731-1738,1819-1848`).
- bootstrap 실패·timeout·late callback 및 executor rejection/registry shutdown cleanup은 `ProxyBootstrapCoordinatorTest`·`LoopbackProxyLifecycleTest`와 RC26~RC28 기록에서 확인된다. 그러나 unexpected accept `IOException`, READY health loss, dead-port Activity recreate, resource budget/idle timeout 종단 시험은 확인되지 않았다.

#### 추론과 반대 근거

- accept loop가 proxy close 이외의 `IOException`으로 종료되고 process가 살아 있으면 coordinator는 성공 상태로 남을 수 있다. 이후 WebView 재시작·recovery는 같은 dead port를 다시 사용해 network error/LOCKED 또는 pause를 반복할 수 있고, process restart 외의 명시적 proxy 복구 경계는 정적 코드에서 확인되지 않는다.
- 여러 partial/idle CONNECT가 유지되면 cached thread pool·무상한 registry가 client/upstream FD와 active worker를 peer/OS 종료까지 보유할 수 있다. 이는 PC receiver의 외부 listener 자원 경계 `LUNA-0007`과 다른 Web process-local loopback 경계다.
- loopback-only listener, 제한된 HTTPS host/443, end-to-end TLS, header/connect timeout, daemon worker, synchronized close/register, rejection cleanup과 bootstrap failure close는 임의 외부 tunnel·정상 shutdown leak 가능성을 제한한다. 실제 accept failure·FD/thread exhaustion을 확인하지 않았으므로 P4 후보를 유지한다.

#### 가정과 미검증

- Android/WebView process에서 `ServerSocket.accept()`가 close 이외의 IOException으로 종료될 수 있고, 그 뒤 process가 계속 살아 있는 조건을 분석했지만 실제 원인·발생률은 확인하지 않았다.
- 다른 local process가 random loopback port에 접근 가능한지, 정상 WebView 동시 연결 수, peer가 idle tunnel을 유지하는 시간, ART/OS가 daemon thread·socket을 회수하는 시점은 실행하지 않았다.
- 실제 proxy 시작·CONNECT·네트워크·localhost scan·WebView error/recovery·Activity recreate·FD/thread 관찰, 테스트·lint·typecheck·build·install·ADB는 수행하지 않았고 source/tests/docs/settings/scripts/build artifacts·기기 상태·Git 이력은 변경하지 않았다.

#### 기대 결과와 실제 결과

- 기대: process-wide proxy가 client/upstream·worker 예산과 idle/handshake timeout을 명시하고, accept loop health loss를 coordinator failure로 전파해 새 proxy를 한 번만 재구성하거나 명확한 recovery 상태로 닫아야 한다.
- 실제: bootstrap 중 실패는 정리하지만 성공 후 resource budget·idle 회수·accept loop health monitor/restart·공개 process teardown이 없다. Activity recovery도 `READY` coordinator를 재사용하므로 `LUNA-0029` P4 후보·중간 신뢰도를 유지한다.

#### 영향 및 후속 검증

- 조건부 local connection flood 또는 dead proxy health loss에서 Web 로그인·학습 흐름이 network pause/LOCKED로 이동하고, process 재시작 전까지 복구가 지연될 수 있다. 실제 운영 중단·자원 고갈은 확인하지 않았다.
- 수정 후에는 synthetic accept-loop IOException/ServerSocket close, coordinator READY→health failure→single restart, Activity recreate, allowed CONNECT partial/idle/parallel socket, executor rejection, client/upstream/worker/FD cap과 timeout을 JVM/Android 계측으로 검증해야 한다. 다음 읽기 전용 감사 대상은 `LUNA-0030`이다.

#### 기준선과 보고서 상태

- 이번 회차에도 누적 보고서만 `apply_patch`로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`를 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.

### 2026-08-02 21:37:22 — LUNA-0030 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0030`을 `QrImageAnalyzer` raw callback, `MainActivity` UI·executor capture, `PcPairingStore` save/load, `PcReceiverPairing` encode/decode, endpoint recovery, 관련 시험·운영 문서까지 읽기 전용으로 재대조했다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- `withHost()`의 `copyOf()`와 decode/save/object ByteArray cleanup은 별도 배열 alias·실패 cleanup finding을 만들지 않는 반대 근거다. 다만 immutable raw/encoded/decrypted String과 일반 pairing lambda의 lifecycle ownership 공백은 남아 기존 범위로 유지했다.

#### 사실

- `Barcode.rawValue`는 `handleRawQr()`의 UI runnable과 `savePcPairing()`의 일반 `ioExecutor` lambda를 거쳐 `PcPairingStore.save(rawPairing)`에 들어가며, `onDestroy()`의 `SensitiveTask` 전용 discard에 포함되지 않는다(`QrImageAnalyzer.kt:78-87`, `MainActivity.kt:2986-3004,3859-3887`).
- `PcReceiverPairing.encode()`와 `PcPairingStore.load()`는 secret을 포함하는 immutable String을 각각 생성하고, `PcPairingStore.save(rawPairing)`는 같은 raw String을 decode와 encrypted save에 재사용한다. 해당 String을 소비 후 zeroize하는 API나 소유자 registry는 없다(`PcReceiverPairing.kt:41-71`, `PcPairingStore.kt:18-70`).
- `withHost()`는 `receiverId`·`secret`을 복제하고, decode/save/load의 mutable payload·ciphertext·IV·plaintext·object arrays에는 cleanup 경로가 있다. 따라서 이번 cycle의 독립 문제는 transient immutable String과 generic queue cleanup으로 한정된다.
- 현재 시험은 pairing 값·암호화 round-trip·authenticated candidate·object cleanup을 확인하지만 String heap retention, Activity 종료 중 queue discard, heap/crash dump 노출은 확인하지 않는다.

#### 추론·미검증

- QR 원문의 Base64 payload에는 PC receiver 인증 secret이 포함되므로, 승인 디버그/동일 프로세스 메모리 접근·crash dump 전제에서는 at-rest 암호화와 별개로 GC 전까지 transient 노출 시간이 늘어날 수 있다. 실제 secret 노출·heap retention·crash dump는 확인하지 않았다.
- Android/ML Kit `String`·ART heap·GC 동작, pairing QR 촬영·저장·endpoint recovery, Activity destroy/queue rejection timing, DB/Keystore runtime, ADB, 테스트·lint·typecheck·build·install은 수행하지 않았다.

#### 기준선과 보고서 상태

- 이번 회차의 지속 변경은 누적 보고서 한 파일뿐이며 `apply_patch`로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`는 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
- 다음 읽기 전용 감사 대상은 `LUNA-0031` CSV preview/apply executor 종료·거부 시 parsed credential `CharArray` lifecycle이다.

### 2026-08-02 21:43:55 — LUNA-0031 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0031`을 Kiosk `MainActivity`의 CSV fetch→preview→apply call graph, 단일 executor lifecycle, `SensitiveTask` 계약, `StudentRepository` cleanup, 시험·운영 문서·관련 history까지 읽기 전용으로 심화 재대조했다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- 성공한 repository 본문과 정상 preview failure/cancel에는 cleanup이 있으나, CSV preview/apply가 generic Runnable로 `SensitiveTask` 밖에 있고 inner rejection·queued shutdown·Activity callback 폐기 시 owner가 사라지는 범위는 남아 있다. parser 반환 전 partial row 문제는 `LUNA-0032`, 정상 parser String 복사는 `LUNA-0013`으로 분리했다.

#### 사실

- `ioExecutor`와 `pcControlExecutor`는 모두 `newSingleThreadExecutor()`이고, fetch task가 parse 뒤 `parsed`를 캡처한 preview lambda를 다시 `ioExecutor`에 제출한다. outer `runCatching`는 이미 실행 중인 `pcControlExecutor` task 내부의 `ioExecutor.execute` rejection을 포착하지 않는다(`MainActivity.kt:161-162,1750-1818`).
- apply positive listener는 `applying = true` 뒤 일반 `ioExecutor.execute`를 호출한다. rejection 또는 `onDestroy()`의 queued Runnable 반환 시 `SensitiveTask.discard()`나 `parsed.clearSensitiveData()` owner가 없고, `onDismiss`는 `!applying` 조건으로 cleanup을 건너뛴다(`MainActivity.kt:1838-1869,3859-3874`).
- `StudentRepository.importStudents()`의 rows cleanup `finally`는 validation·DB 조회·existing username decrypt 뒤에, `previewStudentImport()`의 기존 username cleanup도 existing lookup 뒤에 시작한다. 정상 callback이 오면 MainActivity가 failure/destroyed 경로에서 parsed를 지우지만, callback 폐기·Activity 종료와 겹치는 owner는 보장되지 않는다(`StudentRepository.kt:307-473`, `MainActivity.kt:1791-1812,1852-1869`).
- `SensitiveTask`는 generic task의 정상 완료·예외·discard·submission rejection을 정확히 한 번 정리하도록 구현·시험·운영 문서에 기록되어 있고 PIN·학생 credential 입력·QR hash·PDF bitmap에는 연결되어 있다. 현재 CSV preview/apply 호출부는 그 helper를 사용하지 않는다(`SensitiveTask.kt:5-22`, `SensitiveTaskTest.kt:9-39`, `BUILD_VERIFICATION.md:2063-2084`).
- repository 계측시험은 preview→import 정상 본문에서 rows가 zeroized되는 것만 확인하고, parser 시험은 정상 cleanup·duplicate exception만 확인한다. Activity instrumented test에는 CSV executor lifecycle fault 시험이 없다(`RepositoryInstrumentedTest.kt:493-529`, `StudentCsvParserTest.kt:8-45`).

#### 추론·미검증

- Activity recreation/destroy가 parse 직후 inner preview 제출, preview queue 대기, apply submission과 겹치면 username/password `CharArray`가 명시적으로 zeroize되지 않고 GC까지 남을 수 있다. 이는 승인된 heap inspection·crash/diagnostic 상황에서의 잔류 가능성으로, 실제 heap 잔류·credential 노출·DB 손상은 확인하지 않았다.
- root `SECURITY.md`의 credential·QR 원문 저장 금지와 historical `SensitiveTask` 검증은 로그/파일·generic cleanup의 반대 근거지만, CSV-specific executor ownership과 ART heap zeroization을 증명하지 않는다.
- 실제 CSV·Activity recreation/destroy·executor rejection/queue removal·repository fault·callback timing·heap/ADB·테스트·lint·typecheck·build·install은 수행하지 않았다.

#### 기준선과 보고서 상태

- 이번 회차의 지속 변경은 누적 보고서 한 파일뿐이며 `apply_patch`로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`는 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
- 다음 읽기 전용 감사 대상은 `LUNA-0032` CSV parser 후속 행 검증 실패 시 partial row `CharArray` lifecycle이다.

### 2026-08-02 21:50:07 — LUNA-0032 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0032`를 `StudentCsvParser` 전체 구현, `MainActivity` parser catch/finally, production call graph, parser·repository·현장 시험·보안/운영 문서와 parser history까지 읽기 전용으로 재대조했다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- 후속 row validation/duplicate에서 앞선 `StudentCsvRow`의 credential arrays가 반환 전 owner 없이 사라지는 핵심 근거는 유지한다. 반면 `parseRecords()`와 quote 검증이 row construction보다 먼저 실행되고, 현재 실패 row는 validation·duplicate 뒤에만 arrays를 만들므로 unclosed quote와 실패 row 자체는 범위에서 제외해 finding을 좁혔다.

#### 사실

- parser는 `String(payload)`·`parseRecords()`·`require(!quoted)`를 먼저 완료한 다음 `records.drop(1).mapIndexed`를 실행한다. 각 row의 `StudentCsvRow`는 name/username/password/class validation과 duplicate set insertion 뒤에 생성되며, `ParsedStudentCsv`는 map 완료 뒤에만 반환된다(`StudentCsvImport.kt:41-88,91-128`).
- 첫 row가 생성된 뒤 후속 row의 열 수·field length·class·duplicate validation에서 예외가 나면 누적 list가 호출자에게 전달되지 않는다. `MainActivity` catch/finally는 `download.payload`만 덮고 반환되지 않은 partial rows handle은 없다(`MainActivity.kt:1779-1789`).
- 정상 parse 결과는 `ParsedStudentCsv.clearSensitiveData()`가 rows를 지우지만, duplicate 시험은 예외만 확인한다. 현장/RC47 기록의 정상 CSV 적용·invalid class 거부·민감정보 비노출 PASS는 parser failure-time array cleanup을 증명하지 않는다(`StudentCsvParserTest.kt:8-45`, `RepositoryInstrumentedTest.kt:493-529`, `RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md:143-162`).
- parser 줄은 `0193d898` 도입 history에 남아 있고, partial accumulator cleanup 또는 failure result contract 변경은 확인하지 않았다. `LUNA-0013` immutable `String`, `LUNA-0031` 반환 후 executor owner와는 분리했다.

#### 추론·미검증

- 적어도 한 앞선 row가 정상 생성된 뒤 후속 validation/duplicate가 실패하면 그 row의 username/password `CharArray`가 명시적으로 zeroize되지 않고 GC까지 남을 수 있다. 실제 malformed CSV·parser 실행·heap/GC·credential 노출은 확인하지 않았다.
- 닫히지 않은 quote·header·empty/row-count 오류는 현재 source 순서상 `StudentCsvRow` 생성 전에 실패하는 negative control이다. 실패한 현재 row도 constructor 전에 검증되므로, 실제 범위는 앞선 partial rows로 한정한다.
- DB import는 parser가 `ParsedStudentCsv`를 반환하지 못하므로 시작되지 않는 것으로 정적 흐름상 보이며, 데이터 손상·부분 적용은 확인하지 않았다.

#### 기준선과 보고서 상태

- 이번 회차의 지속 변경은 누적 보고서 한 파일뿐이며 `apply_patch`로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`는 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
- 다음 읽기 전용 감사 대상은 `LUNA-0033` Kiosk PC status queue·CSV/self-test worker 공정성·captured display name lifecycle이다.

### 2026-08-02 21:55:32 — LUNA-0033 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0033`을 Kiosk `MainActivity`의 PC 상태 보고 제출·단일 executor·CSV fetch·운영 자가진단·학생 표시명 lifecycle, `PcControlClient`와 `PcEndpointResolver`, 관련 시험·운영 문서·history까지 읽기 전용으로 재대조했다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- 단일 executor의 FIFO 제출 자체를 임의 역순 실행으로 확대하지 않고, 오프라인/recovery 지연으로 실행 시점에 stale snapshot이 전달되고 기능성 PC 제어가 뒤로 밀릴 수 있는 queue 경계로 한정했다. 종료 뒤 영구 heap leak도 확인하지 않았다.

#### 사실

- `pcControlExecutor`는 `Executors.newSingleThreadExecutor()`이고 별도 bounded queue·coalescing·generation cancellation이 없다. `reportPcStatus()`는 `state`, `studentName`, `notify`를 매 호출 독립 task에 캡처해 같은 executor에 제출한다(`MainActivity.kt:151-162,785-805`).
- 같은 worker가 `fetchStudentCsvFromPc()`와 `runOperationalSelfTest()`의 PC 작업도 수행한다(`MainActivity.kt:1750-1818,2584-2648`). 상태 보고는 관리자·QR·학생 확인·로그인·채점 완료·복구 필요 등 여러 lifecycle/state 경로에서 제출된다.
- queued status lambda는 pairing을 제출 시점에 복제하지 않고 실행 시 `withReachablePairedPc()` 안에서 load한다. `PcControlClient`는 status payload·response/frame/request 자료를 정상/예외 경로에서 덮고, `withReachablePairedPc()`와 resolver도 pairing candidate를 finally/경쟁 탈락 시 정리한다. 따라서 이번 queue 항목에서 queued lambda가 pairing secret 자체를 캡처한다고 단정하지 않았다.
- `activeStudentDisplayName`은 성공 `showScanner()`와 QR 취소에서 null이 되지만, 실패 경로의 `showAuthentication()`, `onStop()`, `onDestroy()`에는 공통 invalidation이 없다. 이미 제출된 task는 전달받은 표시명 snapshot을 계속 보유하며, `showAuthentication()`의 null status 제출이 field를 지우지는 않는다(`MainActivity.kt:210,274-298,3048-3052,3383-3426,3548-3555,3572-3579,3823-3874`).
- `onDestroy()`는 `pcControlExecutor.shutdownNow()`의 반환 list를 `SensitiveTask` 같은 explicit discard 계약에 넘기지 않고, 실행 중 task에 destroyed generation 검사도 제공하지 않는다. 다만 `shutdownNow()` 자체는 대기 task를 drain해 반환하므로 종료 후 executor queue가 계속 보유된다는 증거는 없다.
- Kiosk 시험 검색에서는 `reportPcStatus`, `pcControlExecutor`, `fetchStudentCsvFromPc`, `runOperationalSelfTest`, `sendStatus` 연결을 찾지 못했다. 운영 문서·정상 PC status/self-test/DHCP 기록과 history에는 offline 반복, queue backlog, CSV/self-test starvation, Activity shutdown cleanup을 검증하는 회귀가 없다.

#### 추론·반대 근거

- PC unreachable 또는 endpoint recovery timeout이 반복 상태 보고와 겹치면 앞선 status task가 단일 FIFO worker를 점유하는 동안 후속 상태가 queue에 누적될 수 있다. FIFO라도 오래된 task가 나중에 실행되어 현재 상태와 다른 snapshot을 PC로 보낼 수 있고, 같은 queue 뒤의 CSV fetch·자가진단이 지연될 수 있다.
- 대기 task가 많을수록 학생 표시명 immutable `String` snapshot은 task 실행·폐기·GC 전까지 queue 경계에 남을 수 있다. 그러나 실제 queue 길이·heap 생존 시간·PC 수신 순서·사용자 체감 지연은 측정하지 않았다.
- `PcControlClient`/resolver의 정상 cleanup, bounded socket/resolver timeout, 정상 reachable PC의 빠른 drain, `runCatching`, `shutdownNow()` queue drain semantics는 즉시 crash·항상 발생하는 영구 누수·데이터 손상으로 상향하지 않는 반대 근거다. 이 근거만으로 offline backlog/starvation을 배제할 수는 없다.

#### 가정과 미검증

- 문제 조건은 PC 오프라인 또는 주소 복구 지연과 관리자/QR/session 상태 전이 반복이 동시에 발생하는 경우다. 정상적으로 PC가 즉시 응답하면 queue가 빠르게 소진될 수 있다.
- 실제 PC 오프라인·network discovery·status 반복·CSV fetch·자가진단·Activity recreation/destroy·queue/heap/GC 계측은 수행하지 않았다.
- 테스트·lint·typecheck·build·install·ADB 화면/로그 검증은 수행하지 않았다. 현재 환경에는 `adb` executable이 없어 A 상태도 새로 확인하지 못했다.

#### 기준선과 보고서 상태

- 이번 회차의 지속 변경은 누적 보고서 한 파일뿐이며 `apply_patch`로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`는 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
- 다음 읽기 전용 감사 대상은 `LUNA-0034` 초기 PC pairing host trust 경계다.

### 2026-08-02 22:04:57 — LUNA-0034 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0034`를 Kiosk 초기 pairing QR decode/save, PC QR 생성, direct endpoint routing, DHCP recovery, protocol identity binding, pairing/protocol 시험, 운영·위협 문서와 Git history까지 읽기 전용으로 재대조했다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- protocol의 receiver ID/secret 인증과 운영자의 physical QR·display-name 확인은 정상 trust-on-first-use의 반대 근거다. 그러나 초기 QR의 host/port와 identity가 self-asserted 상태로 저장되고, 별도 attestation·host policy·초기 challenge가 없어 후보 범위를 유지한다.

#### 사실

- Kiosk pairing mode는 prefix만 먼저 확인하고 `PcPairingStore.save(rawValue)`를 호출한다. `PcReceiverPairing`/Python `Pairing`은 host nonempty/길이·ASCII와 port/ID/secret 형식만 검사하며 RFC1918/private·loopback·public/DNS·지정 receiver identity를 검증하지 않는다(`MainActivity.kt:2975-3022`, `PcReceiverPairing.kt:14-22,80-126`, `protocol.py:46-65,139-166`).
- PC `current_lan_ipv4()`는 UDP route probe 결과에서 `127.*`만 거부하고, `ReceiverApplication`은 그 주소를 시작 시 한 번 저장해 display name·port·receiver ID·secret과 QR을 한 번 생성한다(`config.py:28-37`, `app.py:34-49,61-63,99-114`). interface/private Wi-Fi allowlist나 주소 변경 시 QR 재생성은 확인되지 않았다.
- Kiosk의 `PcControlClient`·`PcPdfSender`는 pairing host/port로 `InetSocketAddress`에 직접 연결한다. status/CSV/self-test는 실패 뒤 `withReachablePairedPc()`를 통해 같은 Wi-Fi private subnet의 authenticated probe를 시도하지만, QR PDF 전송은 저장 pairing을 직접 load해 `PcPdfSender.send()`를 호출하므로 해당 recovery 제한을 사용하지 않는다(`MainActivity.kt:807-867,2922-2953`, `PcControlClient.kt:77-90`, `PcPdfSender.kt:11-28`).
- protocol은 QR에 들어온 receiver ID/secret으로 상대를 인증할 뿐, Kiosk에 사전 고정된 receiver ID·certificate·out-of-band attestation을 비교하지 않는다. receiver ID/secret 자체는 PC config에서 새로 생성되어 QR과 함께 self-assert된다(`config.py:71-78`, `protocol.py:46-65,115-166`).
- `PcEndpointResolverTest`는 private `/24` 후보·authenticated probe·no-candidate failure를 검사하고, `PcTransferProtocolTest`·`PcControlProtocolTest`·Python vector는 고정 pairing과 frame crypto를 검사한다. 초기 QR의 public/DNS/loopback/multicast/fake receiver identity negative case는 찾지 못했다. 관련 host/QR 구현 줄은 `0b0df69`와 `474132b` 초기 commit에 남아 있고 host-policy 보강 history는 확인되지 않았다.

#### 추론·반대 근거

- 관리자 PIN 뒤 악성·가짜 QR을 승인하면 fake endpoint가 자기 host/port와 자기 receiver ID/secret/display name을 함께 제시할 수 있고, Kiosk는 이를 저장한 뒤 status·CSV·PDF를 그 endpoint로 보낼 수 있다. fake receiver는 QR에 포함한 secret으로 전송을 복호화할 수 있다. 실제 fake QR·외부 endpoint 연결은 수행하지 않았다.
- `current_lan_ipv4()`의 private/interface 검사가 없는 구조는 VPN·가상·공용/비의도 route가 QR에 들어갈 가능성을 정적으로 남긴다. 실제 반환 주소나 route 선택은 확인하지 않아 가능성으로만 기록한다.
- physical PC 화면에서 QR을 직접 촬영하고 표시 이름 `DESKTOP-D4AGJI7`을 확인하는 운영 절차, Private firewall profile, AES-GCM/HMAC와 receiver ID binding, DHCP recovery의 same-private-`/24` authenticated probe는 위험을 줄인다. 이들은 operator trust 또는 이미 저장된 identity의 주소 recovery를 보완할 뿐, 초기 QR source/receiver identity를 독립 증명하지 않는다.

#### 가정과 미검증

- 제품이 모든 배포 환경에서 A와 PC의 same private Wi-Fi·RFC1918 host를 요구하는지는 문서에서 강하게 추론되지만 별도 host-policy 결정으로 확정되지 않았다. 비-RFC1918/DNS 배포가 합법이면 단순 host 거부는 호환성 위험이 있다.
- 실제 fake QR·public/DNS/loopback/multicast host·PC interface/DHCP 변경·status/CSV/PDF endpoint 연결·receiver identity 교체·Windows firewall 동작은 실행하지 않았다.
- ADB executable이 없어 A 화면·설치된 PC receiver·실제 QR 표시와 상태를 확인하지 못했다. 테스트·lint·typecheck·build·install도 실행하지 않았다.

#### 기준선과 보고서 상태

- 이번 회차의 지속 변경은 누적 보고서 한 파일뿐이며 `apply_patch`로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 `diagnostics/`, `output/`, `tmp/`는 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
- 다음 읽기 전용 감사 대상은 `LUNA-0035` Web recovery 단일 gate와 관리자 mutation 경계다.

### 2026-08-02 22:14:11 — LUNA-0035 cycle closeout

#### 이번 회차 판정

- 기존 LUNA-0035를 Web recovery 중 반·보강·학생 관리자 mutation과 pending Start action의 공통 gate/revision 경계로 다시 대조했다. 심각도 P3, 상태 후보, 신뢰도 중간을 유지하며 새 finding ID는 추가하지 않았다.
- 현재 source와 blame에서 206b6e7의 단일 webRecoveryGate 도입은 확인되지만, 이후에도 관리자 mutation 전체를 묶는 공통 gate나 recovery generation binding은 확인되지 않았다.

#### 사실

- updateClassRosterUi()와 updateStudentManagementControls()의 enabled 조건, configureActions()의 direct handler wiring, EndSession 중 addTemporaryStudents() 경로에 webRecoveryGate 연결이 없다. 빠른 반 버튼도 currentSession/className 조건만 사용한다.
- webRecoveryLauncher callback은 pendingRecoveryAction을 현재 필드에서 소비하고, StartSession은 캡처된 classId·temporaryStudentIds를 repository에 전달한다. repository transaction은 active class/student/session 불변조건을 확인하지만 user-intent revision은 확인하지 않는다.
- SingleFlightGate 단위시험과 RC04/RC05 문서의 단일 실행 기록은 gate 중복과 정상 recovery 계약의 근거이지, recovery 중 모든 관리자 mutation enabled/callback matrix의 증거는 아니다.

#### 추론·반대 근거

- recovery launch 전후의 빠른 callback/queued mutation, Activity 재진입, lifecycle/multi-window 또는 EndSession 중 보강 제출이 발생하면 최신 화면 선택·mutation 결과와 후속 session action이 어긋날 가능성이 정적으로 남는다. 실제 timing에서의 stale session roster나 데이터 손상은 확인하지 않았다.
- 외부 Web Activity가 전면에 있는 정상 단일창 사용, 단일 ioExecutor, repository transaction 및 active session/state 검사는 교차 조작 기회와 직접 DB 오염을 줄인다. 따라서 확정 결함이나 P2 이상으로 상향하지 않았다.

#### 가정과 미검증

- 실제 Web recovery·반/학생 mutation·보강 추가·수업 시작/종료·Activity recreation·multi-window·DB fault·callback timing·ADB 화면은 사용하지 않았다.
- 테스트, lint, typecheck, build, install도 실행하지 않았다. 현재 PATH에 adb executable이 없어 A 상태와 실제 버튼 touch 가능성을 확인하지 못했다.

#### 기준선과 보고서 상태

- 이번 회차의 지속 변경은 누적 보고서 한 파일뿐이며 apply_patch로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 diagnostics/, output/, tmp/를 변경하지 않았다.
- branch codex/fix-submit-recovery-timeout, HEAD/upstream 8c9a97ca55d34e3d93fdb02d9879ad929ed7133d는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
- 다음 읽기 전용 감사 대상은 LUNA-0036 학생 mutation gate와 batch QR/인쇄 경계다.

### 2026-08-02 22:23:17 — LUNA-0036 cycle closeout

#### 이번 회차 판정

- 기존 LUNA-0036을 학생 단일 mutation·CSV·반 QR 일괄 발급·Android PrintManager side effect 사이의 operation binding 경계로 다시 대조했다. 심각도 P3, 상태 후보, 신뢰도 중간을 유지하며 새 finding ID는 추가하지 않았다.
- batch 기능은 630fce1에 도입됐고 CSV 경로는 0193d89에 도입됐다. 현재 history에서 두 경계를 공통 generation/cancellation으로 연결한 후속 보강은 확인하지 못했다.

#### 사실

- batchQrButton은 classReady·session 없음·membership 존재만 검사하며 studentMutationGate를 보지 않는다. batch 성공 callback은 updateClassRosterUi()를 호출해 같은 조건으로 다시 활성화한다.
- CSV fetch/apply는 import 버튼만 잠그고 studentMutationGate를 사용하지 않는다. importStudents()는 같은 ioExecutor에서 이름·자격정보·반 소속·카드 출력 필요 상태를 갱신한다.
- reissueClassQrBatch()는 active class의 active student 목록을 읽고 transaction에서 active 여부와 QR 상태를 갱신한다. 반환된 BatchQrCard는 displayName과 bitmap만 갖고, student/class/issued revision/generation을 보유하지 않는다.
- BatchQrPrintDocumentAdapter는 전달받은 cards를 onFinish()까지 그리며 현재 DB·membership·QR revision을 재조회하거나 mutation 성공 시 취소하는 hook이 없다. MainActivity.onDestroy()도 PrintManager handle을 취소하지 않는다.
- Repository batch 원자성 시험, SingleFlightGate 중복 시험, 학생 단일 실행 문서와 직접 인쇄 선택 검증 항목은 각각의 정상·단일 경계를 보여주지만 cross-operation/지연 spooler matrix의 증거는 아니다.

#### 추론·반대 근거

- batch transaction이 끝난 뒤 학생 비활성화·이름 변경·CSV·단건 QR·반 소속 변경이 실행되면, 이미 생성된 identity 없는 card가 최신 DB 상태와 무관하게 PrintManager로 전달될 가능성이 있다. 실제 stale 출력이나 DB partial write는 확인하지 않았다.
- 같은 single-thread executor와 repository transaction은 동시 DB 손상과 batch 내부 partial write를 줄인다. 외부 인쇄 화면 진입 시 onStop() 관리자 재잠금, 직접 인쇄 운영 보류 문서, 실제 사용자가 작업 완료 후 재인증한다는 정상 절차도 발생 기회를 낮춘다.

#### 가정과 미검증

- 실제 학생·반·CSV·QR 변경, PrintManager spooler 지연/취소, 관리자 재인증, Activity recreation, 인쇄 queue와 callback 순서는 실행하지 않았다.
- 테스트·lint·typecheck·build·install·ADB 화면 검증은 수행하지 않았다. Android 13 A에서 batch와 mutation의 정확한 touch/callback 순서를 확인할 수 없다.
- 제품이 직접 인쇄 path를 계속 노출할지, batch QR을 다른 관리자 mutation과 하나의 취소 가능한 operation으로 묶을지는 Sol·사용자 재판단 큐에 남긴다.

#### 기준선과 보고서 상태

- 이번 회차의 지속 변경은 누적 보고서 한 파일뿐이며 apply_patch로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 diagnostics/, output/, tmp/를 변경하지 않았다.
- branch codex/fix-submit-recovery-timeout, HEAD/upstream 8c9a97ca55d34e3d93fdb02d9879ad929ed7133d는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
- 다음 읽기 전용 감사 대상은 LUNA-0037 pending-card PDF delivery와 student mutation invalidation 경계다.

### 2026-08-02 22:27:01 — LUNA-0037 cycle closeout

#### 이번 회차 판정

- 기존 LUNA-0037을 pending-card QR 발급·PDF export·PC ACK·delivered 상태 반영과 후속 학생 mutation/lifecycle의 identity binding 경계로 다시 대조했다. 심각도 P3, 상태 후보, 신뢰도 중간을 유지하며 새 finding ID는 추가하지 않았다.
- 현재 source/history에서 pending flow가 0193d89에 도입된 뒤 studentMutationGate·QR revision/request ID·활성 상태를 delivered 결과에 결합하는 후속 보강은 확인하지 못했다.

#### 사실

- pending-card task는 자기 버튼만 잠그고 studentMutationGate를 소유하지 않으며, 같은 ioExecutor 안에서 reissueQrBatch(), PDF export, PcPdfSender.send(), markCardsDelivered()를 순서대로 수행한다.
- QR 상태 delivered SQL은 studentId만으로 needsPrint를 0으로 만들고, PcPdfSender ACK는 PDF request/hash만 인증한다. 학생별 QR 세대·활성 상태·delivery request와의 관계는 기록하지 않는다.
- onStop()은 관리자 재잠금과 화면 preview 정리를 수행하지만 pending task를 취소하지 않는다. onDestroy()의 SensitiveTask discard는 일반 pending runnable에 적용되지 않으며 running task의 socket cancellation/late DB result 보상도 없다.
- Repository 정상 시험은 batch 재발급과 delivered 상태의 정상 순서를 검사하지만 mutation/ACK 지연/lifecycle ordering을 검사하지 않는다. 운영 문서는 QR 재발급→PC 저장→PC 인쇄 순서를 설명하지만 후속 mutation 시 PC artifact 폐기·재생성 정책은 명시하지 않는다.

#### 추론·반대 근거

- PC ACK 뒤 student mutation이 실행되면 PC에 이미 저장된 QR PDF와 Kiosk의 현재 active/QR/needsPrint 상태가 시간적으로 달라질 수 있다. 현재 단일 executor의 알려진 UI 경로에서 실제 interleave나 DB partial write를 확인한 것은 아니다.
- PDF hash·request ID 인증, PC receiver의 commit-before-ACK, 동일 ioExecutor의 DB 직렬화, 관리자 stop 시 재잠금은 protocol 위조·부분 저장·일반적인 교차 조작을 줄인다. 따라서 ACK/idempotency 문제인 LUNA-0010과 합치지 않고 P3 후보로 제한한다.

#### 가정과 미검증

- 실제 PC 전송·ACK 유실/지연·파일 저장·학생 비활성화/이름/CSV/반 변경·Activity stop/destroy·socket interrupt·DB를 사용하지 않았다.
- Android 13에서 print/PC 전송 task가 관리자 재인증·다음 mutation과 어떤 순서로 교차하는지, PC에 남은 PDF의 실제 물리 인쇄와 후속 폐기 여부는 확인하지 않았다.
- 전송 완료 후 mutation을 허용할지, 허용한다면 기존 PC artifact를 자동 폐기·재생성할지는 Sol·사용자 재판단 큐에 남긴다.

#### 기준선과 보고서 상태

- 이번 회차의 지속 변경은 누적 보고서 한 파일뿐이며 apply_patch로 갱신했다. source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 diagnostics/, output/, tmp/를 변경하지 않았다.
- branch codex/fix-submit-recovery-timeout, HEAD/upstream 8c9a97ca55d34e3d93fdb02d9879ad929ed7133d는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
- 다음 읽기 전용 감사 대상은 LUNA-0038 Web SPA 과제 전환·문항별 상태 격리 경계다.

### 2026-08-02 20:35:44 — LUNA-0021 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0021`을 양 앱 source·receiver·운영 스크립트·시험·현장 기록으로 재확인했다. 심각도 `P4`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- `LUNA-0020`의 민감 화면 `FLAG_SECURE` 경계와는 분리했다. 이번 finding은 원격 지원 상태가 Kiosk와 Web에 동일하게 저장·반영·종료됐다고 확인할 수 없는 동기화 경계다.

#### 사실

- Kiosk `RemoteSupportStore.kt:23-50`와 Web 동일 파일의 `:23-50`은 `enable()`에서 만료 시각과 boot count를 `SharedPreferences.Editor.commit()`으로 저장하지만 반환 boolean을 무시하고, 저장 성공 여부와 무관하게 계산한 만료 시각을 반환한다. `disable()`도 두 key 제거의 commit 결과를 무시하며, `activeUntilEpochMillis()`의 만료·boot 불일치 cleanup도 같은 `disable()`을 호출할 뿐 결과를 확인하지 않는다.
- Kiosk `MainActivity.kt:2712-2753`은 로컬 store를 먼저 enable/disable한 뒤 Web의 explicit broadcast를 보낸다. `notifyWebRemoteSupport()`는 `runCatching { sendBroadcast(intent) }.isSuccess`만 반환하므로 수신 receiver의 `commit()` 성공, 처리 완료 또는 최종 Web store 상태를 확인하지 않는다. enable 경로에서만 `webNotified == false`일 때 Kiosk local disable rollback이 있고, disable 경로의 전달 실패·저장 실패에는 반대쪽 상태 복구나 재확인이 없다.
- Web `AdbRemoteSupportReceiver.kt:7-16`, `KioskRemoteSupportReceiver.kt:7-16`은 각각 `applyRemoteSupportIntent()`를 호출하고, `RemoteSupportIntent.kt:10-25`는 `RemoteSupportStore`에 저장한 뒤 결과·acknowledgement를 caller에 반환하지 않는다. Web controller listener는 Web process가 실제로 관찰한 preference 변화만 갱신할 뿐 Kiosk에 성공을 회신하지 않는다.
- `scripts\remote-tablet.ps1:35-63`은 Kiosk receiver를 성공 확인한 다음 Web receiver를 호출한다. 각 명령의 `LASTEXITCODE`와 출력의 `result=0`만 검사하고 양 앱 preference readback이나 상호 rollback은 하지 않는다. `Start` (`:96-103`)는 두 앱 적용 뒤 `Capture-Screen`을 실행하지만 전체 경로를 감싸는 Stop cleanup이 없고, `Stop` (`:108-113`)은 두 앱을 다시 순차 처리한다.
- Kiosk/Web manifest와 계측시험은 receiver exported/permission 및 UI 도달성을 확인한다(`kiosk/src/androidTest/.../Gate5ManifestInstrumentedTest.kt:79-96`, `webpoc/src/androidTest/.../ManifestInstrumentedTest.kt:41-60`). 두 앱의 `RemoteSupportPolicyTest`는 duration clamp와 same-boot/expiry 순수 정책만 검사하며 commit false, receiver 처리 실패, 두 앱 partial state, disable rollback 또는 캡처 실패 cleanup은 다루지 않는다.
- `SECURITY.md:27-29,48-50,59-65`는 승인된 시간 제한 원격 점검, same-boot 최대 2시간, 만료·명시 종료 복원을 정책으로 선언한다. `docs/BUILD_VERIFICATION.md:6220-6222`의 RC107 기록은 원격 점검을 5분 재개했다가 즉시 비활성화하고 `QR_READY`로 끝낸 정상 경로지만, failure atomicity나 양 앱 저장 결과의 직접 readback은 증명하지 않는다.

#### 추론과 반대 근거

- Kiosk local `commit()`이 false이고 Web broadcast·Web commit이 성공하면 Kiosk UI가 원격 점검 시작을 표시해도 Kiosk controller는 inactive일 수 있다. 반대로 Kiosk disable이 성공한 뒤 Web broadcast가 receiver 미실행·처리 실패·commit false가 되면 Kiosk는 차단됐다고 표시하지만 Web의 `FLAG_SECURE` 해제와 WebView debugging이 만료 시각까지 남을 수 있다. 두 경우 모두 코드 경로상 가능한 상태 불일치이며, 실제 실패 발생은 확인하지 않았다.
- 첫 receiver만 처리되고 두 번째 `am broadcast`가 실패하면 운영 스크립트는 예외로 끝나지만 이미 적용된 첫 상태를 되돌리지 않는다. `Start` 중 캡처가 실패해도 지원 상태는 남을 수 있어, 계약상 사용자가 별도 `Stop`을 실행해야 하는 운영 의존성이 생긴다.
- explicit component, Kiosk의 signature `REMOTE_SUPPORT_CONTROL`, ADB receiver의 `android.permission.DUMP`, same-boot·expiry timer·visible badge와 정상 RC107 기록은 무단 호출과 정상 경로의 지속 시간을 줄이는 반대 근거다. 따라서 실제 데이터 손실·무단 접근을 입증한 P2 이상이 아니라 조건부 동기화·복구 공백인 P4 후보로 유지한다.

#### 가정과 미검증

- 실제 remote support enable/disable, ADB broadcast, Web receiver 중단, `commit()` false fault, process crash, 설치 누락, disk-full·permission failure, 상태 readback은 수행하지 않았다.
- `result=0`이 receiver dispatch의 명령 결과라는 점은 정적 script 관찰이며, 그것이 각 앱의 preference commit 성공을 보장한다고 가정하지 않았다.
- ADB executable이 현재 PATH에 없어 A device의 두 앱 상태·window flag·WebView debugging·임시 캡처 잔류를 확인하지 못했다. tests, lint, typecheck, build, install도 실행하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 두 앱이 원격 지원 상태를 all-or-nothing으로 반영하고, 각 local persistence와 receiver 처리를 확인하는 acknowledgement/readback, 실패 시 양쪽 rollback, Start/Capture 예외 시 자동 Stop 또는 명확한 복구 경로를 갖는다.
- 실제: 양 앱 저장은 commit 결과를 버리고, Kiosk↔Web 전달은 fire-and-forget이며, 운영 스크립트는 두 대상을 순차 변경하고 명령 결과만 확인한다. 정상 만료·명시 종료 정책은 있으나 부분 실패 시 일관성 보장은 확인되지 않았다.

#### 영향·재판단 및 후속 검증

- 원격 지원 중 한 앱만 capture/debugging 허용 또는 차단 상태가 되어 운영자가 종료를 완료했다고 오인할 가능성이 남아 `LUNA-0021` P4 후보를 유지한다. 실제 잔류 캡처나 자격정보 노출은 확인하지 않았으므로 심각도를 상향하지 않는다.
- 수정 후에는 Kiosk/Web 각 `commit()` false, receiver 미설치·예외, broadcast timeout, 두 번째 대상 실패, Start 캡처 실패를 fault seam으로 검증하고, enable/disable/expiry/boot 경계마다 두 앱의 preference·`FLAG_SECURE`·WebView debugging·badge를 함께 readback해야 한다. 다음 읽기 전용 감사는 `LUNA-0022` Lock Task 진입 결과와 overlay restriction 경계다.

#### 기준선과 보고서 상태

- 누적 보고서만 갱신했으며 source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 산출물을 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
- 다음 읽기 전용 대상은 `LUNA-0021` 원격 지원 저장·broadcast 상태 동기화 경계다.

### 2026-08-02 20:30:44 — LUNA-0020 cycle closeout

#### 이번 회차 판정

- 기존 `LUNA-0020`을 재확인·구체화했다. 심각도 `P3`, 상태 `후보`, 신뢰도 `중간`을 유지하며 새 finding ID는 추가하지 않았다.
- `LUNA-0021`의 SharedPreferences commit 결과·Kiosk↔Web broadcast 동기화 문제는 상태 전달 경계로 분리하고, 이번 finding은 활성 원격 지원 중 민감 화면의 window capture 차단 경계로 한정했다.

#### 사실

- Kiosk `RemoteSupportWindowController.kt:24-44`와 Web `RemoteSupportWindowController.kt:24-45`는 `activeUntilEpochMillis()`의 null 여부만으로 활성 상태를 정하고, 활성일 때 `FLAG_SECURE`를 clear하고 badge를 표시하며, 만료 시각에 맞춰 재평가한다. screen, focus, auth/setup state를 읽는 조건은 없다.
- Kiosk `MainActivity.kt:337-369`는 기본 `FLAG_SECURE`와 controller를 초기화하고 시작한다. `showAuthentication()` (`:661-690`)은 PIN 입력을 표시·focus하지만 controller를 stop하거나 window flag를 다시 설정하지 않는다. `onStop()` (`:3823-3836`)과 `onStart()` relock (`:3838-3856`)도 같은 secure-boundary hook을 갖지 않으며, controller stop은 `onDestroy()` (`:3859-3866`)에 있다.
- Web `MainActivity.kt:153-223`은 기본 `FLAG_SECURE` 뒤 controller를 시작·refresh한다. `showSetup()` (`:1740-1762`)는 credential 입력 화면을 표시하고 focus하지만 controller를 stop하지 않는다. `configureSensitiveInputs()` (`:380-410`)는 autofill/save와 obscured touch만 제한하고 `FLAG_SECURE`를 복원하지 않는다.
- 현재 시험 검색에서 Kiosk는 `MainActivityInstrumentedTest.kt:146-203`에서 `showAuthentication()`을 호출하고 기본 secure flag를 확인하지만 active remote support 상태와 결합하지 않는다. Web은 `RecoveryInstrumentedTest.kt:604-613`에서 기본 secure flag를 확인하며, 두 앱 모두 active remote support + PIN/credential/setup 화면 matrix를 확인하는 시험은 찾지 못했다.
- `SECURITY.md`의 원격 점검 예외, 관리자 인증·signature/DUMP receiver 경계, same-boot·만료·명시 종료 복원·badge·30분 기본/2시간 최대와 “관리자 PIN·비밀번호 입력 중에는 사용하지 않는다”는 운영상 반대 근거다. 이는 화면별 자동 강제나 회귀시험의 근거는 아니다.

#### 추론과 반대 근거

- 유효한 원격 지원이 남은 상태에서 Kiosk가 background/재개 후 PIN 인증 화면으로 전환되거나 Web이 setup credential 화면으로 전환되면, 현재 구조상 `FLAG_SECURE`가 clear된 채 유지될 가능성이 있다. 승인된 ADB screen capture 또는 원격 디버깅 경로가 그 시점의 PIN·자격정보를 볼 수 있다는 위험은 정적으로 성립하지만, 실제 노출·캡처는 확인하지 않았다.
- 관리자 확인과 privileged receiver, 만료 timer, same-boot 제한, 명시 종료 복원, visible badge와 operator warning은 악용 가능성과 지속 시간을 줄인다. 따라서 승인된 원격 지원 기능 자체를 제거해야 하는 P0/P1로 상향하지 않고 P3 후보로 유지한다.

#### 가정과 미검증

- 실제 원격 지원을 활성화하지 않았고 PIN·username/password 입력, WebView debugging, ADB screen capture, remote control, Activity background/restore, TalkBack 또는 lock-task 상태를 실행하지 않았다.
- ADB executable이 현재 PATH에 없어 현재 기기 window flag·실제 screen capture·remote-support active 조합은 확인하지 못했다. tests, lint, typecheck, build, install도 이번 회차에는 실행하지 않았다.
- “remote support가 active인 동안 민감 화면으로 전환된다”는 조건은 source lifecycle에서 가능한 시나리오로 추론했으며, 운영 로그에서 해당 순서의 실제 발생은 확인하지 않았다.

#### 기대 결과와 실제 결과

- 기대: 원격 지원이 켜져 있어도 PIN·credential/setup 화면에 진입하는 순간 capture 차단을 자동 복원하고, 민감 화면을 벗어나거나 지원을 명시적으로 재개할 때만 정책에 따라 다시 허용하며, 이 matrix를 회귀시험으로 고정한다.
- 실제: capture 허용 여부는 만료 시각 기반 controller에만 연결되어 있고, Kiosk auth/relock 및 Web setup 전환은 이를 재평가하지 않는다. 기본 secure flag와 운영자 경고는 확인되지만 민감 화면별 자동 차단은 확인되지 않았다.

#### 영향·재판단 및 후속 검증

- 활성 원격 지원 중 PIN 또는 Web credential 화면이 표시되는 조건에서 화면 캡처·디버깅 노출 가능성이 남아 `LUNA-0020` P3 후보를 유지한다. 실제 민감정보 노출 증거가 없으므로 P2 이상으로 재분류하지 않는다.
- 수정 후에는 Kiosk의 active support → `showAuthentication()` → background/`onStart()` relock과 Web의 active support → `showSetup()`/Gate 3 credential 진입을 각각 실행해 `FLAG_SECURE`, badge, WebView debugging, capture 결과를 진입·유지·복귀·만료·명시 종료 순서로 검증해야 한다. 다음 읽기 전용 감사는 `LUNA-0021` 상태 저장·broadcast 부분 실패 경계다.

#### 기준선과 보고서 상태

- 누적 보고서만 갱신했으며 source, tests, docs, settings, scripts, build artifacts와 기존 사용자 수정·미추적 산출물을 변경하지 않았다.
- branch `codex/fix-submit-recovery-timeout`, HEAD/upstream `8c9a97ca55d34e3d93fdb02d9879ad929ed7133d`는 불변이다. Git stage/commit/push/PR/merge/deploy/rollback은 수행하지 않았다.
