# Web POC 검증 보고서

검증일: 2026-07-21, A 이관 재검증 2026-07-22 (Asia/Seoul)

## 판정

- Web POC 구현·빌드·정적 보안검사: **PASS**
- SM-P613 가상 DOM·복구 계측시험: **12/12 PASS**
- SM-P610 가상 DOM·복구 계측시험: **15/15 PASS**
- 공식 공개 로그인 페이지 preflight와 입력값 정리: **PASS**
- 오프라인 실패 폐쇄와 복구 후 초기화: **PASS**
- 실제 시험계정 단일 자동 로그인→표시명 검증→`채점 끝내기`→자동 로그아웃: **20/20 PASS**
- 실제 실패주입 W03~W10과 각 안전 복구: **PASS**
- A 태블릿 전체 재부팅 뒤 Web POC 실행·안전상태 검사: **PASS**
- 정의된 POC 범위의 Web Gate 2: **PASS**

실제 자격정보는 코드, ADB 명령, 테스트 runner, 파일과 로그로 전달하지 않았다. 사용자가 A 화면에서 직접 입력한 시험계정으로 정상 cycle 20회와 자격정보가 필요한 실패주입을 수행했다.

## 구현 검증

`scripts/build.ps1`의 외부 ASCII build 경로 clean 실행:

- 총 150 Gradle tasks 성공
- Probe 단위 테스트 8개: 실패 0, 오류 0
- 잠긴 Android POC 단위 테스트 1개: 실패 0, 오류 0
- Web POC 단위 테스트 6개: 실패 0, 오류 0
- 세 모듈 lint: 모두 `No issues found.`
- 세 debug APK 생성 성공

Web POC 단위시험 범위:

- 공식 HTTPS 상위 탐색 host·scheme·port·userinfo allowlist
- 표시명 NFKC·공백 정규화와 완전 일치
- 민감 상태 재시작 복구 분류
- login/portal/logout DOM script의 의미 selector와 좌표 selector 부재

## B 기기 계측시험

대상: SM-P613, Android 14

`scripts/test-webpoc-device.ps1` 실행 결과 12/12 성공:

1. 가상 로그인 DOM의 아이디·비밀번호·저장 checkbox 잔존값 정리
2. 승인되지 않은 form action fingerprint 거부
3. 의미 기반 username/password 입력과 form 1회 submit
4. 가상 portal의 닉네임 fingerprint, 계정 메뉴, 로그아웃 실행
5. 교차 origin의 `/userInfo` 위장 링크 거부
6. 표시된 정확한 로그아웃 제어가 둘 이상이면 실행 거부
7. 공식 공개 로그인 페이지에서 IDLE preflight
8. `ACTIVE` 재시작 표시가 민감 상태로 자동 복귀하지 않음
9. 명시적 `LOCKED`가 자동 해제되지 않음
10. 승인 외 상위 탐색이 `LOCKED`
11. Activity `FLAG_SECURE`
12. WebView 디스크 cache·로컬 파일·content·form 저장 차단

계측시험은 실제 계정과 무관한 가상 문자열만 사용했다. DOM fixture에는 실제 학생정보·계정정보가 없다.

보강 직후 첫 실행에서는 12개 중 중복 로그아웃 시험 1개가 실패했다. 시험이 계정 메뉴를 열지 않아 원래 로그아웃 항목이 숨겨진 채 추가 항목만 보였던 fixture 준비 오류였다. 실제 흐름과 같이 계정 메뉴를 먼저 열도록 시험을 수정한 뒤 전체 12/12를 다시 실행해 통과했다.

## 추가 실기

- Web POC 최초 설치·공식 로그인 preflight 뒤 `IDLE`: PASS
- Wi-Fi를 끈 깨끗한 앱 시작: `LOCKED` PASS
- 이 시험에서 지연된 페이지 콜백이 처음에는 `MAINTENANCE_REQUIRED`로 terminal 분류를 덮어쓰는 경쟁조건을 발견했다.
- terminal 상태 이후의 지연 callback을 무시하도록 수정한 뒤 동일 시험에서 `LOCKED` 재현: PASS
- Wi-Fi는 시험 전후 활성 상태로 복구
- 비어 있는 Web POC 데이터만 초기화하고 재시작한 뒤 `IDLE`: PASS
- `IDLE`에서 force-stop/relaunch: `IDLE` PASS
- 홈 전환·앱 재전면화: `IDLE` PASS
- 최종 설치 상태: versionName `0.1.0`, `IDLE`, terminal reason 없음

## 기기 A 이관 결과

- `install-webpoc.ps1`, `test-webpoc-device.ps1`, `collect-device-baseline.ps1`에 `-Serial`·`-ExpectedModel` 대상 고정을 추가
- A 전용 `scripts/prepare-a-webpoc.ps1` 추가: `SM-P610` 선택→전체 빌드→비민감 baseline→15개 계측→재설치→`IDLE` 확인
- A와 B가 함께 연결돼도 `ANDROID_SERIAL`을 선택한 A로 한정하도록 구현
- 전체 PowerShell 스크립트 구문검사: PASS
- 연결된 B를 `SM-P613`로 명시한 일반화 스크립트 회귀시험: 12/12 PASS, 재설치 후 `IDLE` PASS
- B만 연결된 상태에서 A 전용 명령의 모델 오인 방지: 설치 전 중단 PASS
- A `SM-P610`, Android 13, SDK 33 모델·환경 확인: PASS
- A 비민감 기준정보 재수집: PASS, 일련번호·계정·화면·logcat 미수집
- A 전체 빌드 결과 재확인: Probe 8/8, 잠긴 POC 1/1, Web POC 6/6, 세 모듈 lint PASS
- A Web POC 계측시험 결과 파일: 종료코드 0, 개별 시험 15/15 PASS
- A 본체 versionName `0.1.0` 재설치·실행과 최종 `IDLE`: PASS
- 새 빌드 APK와 전달 artifact SHA-256 일치: PASS

## A 실제 시험계정 cycle

1. 사용자가 A 화면에 시험계정 정보를 직접 입력했다.
2. 자동 로그인과 예상 표시명 완전 일치 뒤 비민감 상태 `ACTIVE`: PASS
3. 문제화면에서 `채점 끝내기` 실행 뒤 최초 자동 로그아웃: `LOCKED / LOGOUT_CONTROL`
4. 실패 폐쇄로 다음 로그인 차단: PASS
5. 구조 진단값 `C0_A2_V0_S0`: 검증된 account submenu 안의 exact 로그아웃 부모·자식 구조 2개는 존재하지만 표시 요소 0개, submenu 표시 상태 false
6. 계정 submenu 안에서 exact 텍스트인 유일한 최하위 요소 하나만 허용하는 bubbling click 복구를 추가했다. 부재·중복은 계속 차단한다.
7. 데이터·cookie를 지우지 않는 업데이트 설치 후 안전 복구: 계정 입력화면과 `IDLE`, 종료 사유 없음 PASS
8. 보강 뒤 A 가상 DOM·복구 계측시험: 최종 15/15 PASS
9. 보강 뒤 실제 시험계정 자동 로그인→표시명 검증→`채점 끝내기`→자동 로그아웃→입력화면: PASS
10. 정상 cycle 직후 force-stop/relaunch: 전후 모두 `IDLE`, 종료 사유 없음 PASS

보강 계측시험 첫 실행에서는 중복 로그아웃 거부 시험 1개가 과거 반환값 `2`를 기대해 실패했다. 새 계약은 중복 최하위 요소 2개를 진단하되 실행 후보를 0개로 반환한다. 시험 기대값을 계약에 맞게 수정한 뒤 전체 15/15를 재실행해 통과했다. 실제 정상 반복은 최종 **20/20 PASS**이며 세부 회차는 `WEB_POC_CYCLE_LOG.md`에 기록한다.

## APK 보안검사

- applicationId: `com.local.matholickiosk.webpoc`
- versionName/versionCode: `0.1.0` / `1`
- minSdk/targetSdk: 33 / 37
- 권한: `android.permission.INTERNET` 1개만 요청
- `allowBackup=false`, `fullBackupContent=false`, data extraction exclusion
- cleartext 차단과 network security config 적용
- `FLAG_SECURE`, activity `stateNotNeeded=true`, 최근 앱 목록 제외
- WebView debugging·디스크 cache·form 저장·autofill·로컬 파일/content 접근 차단
- 외부 top-level 탐색·download·TLS 오류·Safe Browsing 경고 실패 폐쇄
- 좌표 selector, 접근성 gesture, uiautomator, ADB 입력, JavaScript interface, 앱 로그 호출 없음
- generic 자격정보 형태 소스 검사: 발견 0
- APK Signature Scheme v2 검증 성공, signer 1

## APK

- 파일: `artifacts/matholic-webpoc-0.1.0-debug.apk`
- 크기: 2,541,773 bytes
- SHA-256: `05E5F59A8F935AD41BA4DC190C7E123931940ACC49E8E2B2E2EE44A6CF7CE4AB`
- `artifacts/SHA256SUMS.txt` 대조: PASS

## 사용자 실기 완료 상태

- 정상 cycle 20/20과 계획된 실제 실패주입을 완료했다.
- 최종 정상 cycle 직후 force-stop/relaunch에서도 `IDLE`, 종료 사유 없음이 유지됐다.
- A 태블릿 전체 재부팅 뒤 부팅 경과 359초 시점에 앱 실행 중, `IDLE`, 종료 사유 없음이 확인됐다.

## 실제 실패주입

- W03 잘못된 비밀번호: `LOCKED / LOGIN_NOT_VERIFIED`, 자동 재시도·학생 화면 진입 없음 — PASS
- W03에서 전환 중 빈 로그인 DOM을 잘못 평가하는 안정성 결함을 재현했다. 로그인 URL 새 로드와 800 ms 간격 유효 fingerprint 2회 확인을 추가한 뒤 동일 실패와 안전 복구를 반복해 PASS했다.
- W04 예상 표시명 불일치: 학습 허용 전 자동 로그아웃, `LOCKED / STUDENT_MISMATCH` — PASS
- W05 `ACTIVE` Wi-Fi 단절 뒤 필수 네트워크 이동: `LOCKED / NETWORK_ERROR`, Wi-Fi 복원·안전 복구 — PASS
- W06 실제 `LOGIN_SUBMIT` 프로세스 종료: 검증 없는 자동 복귀 없이 `RECOVERY_REQUIRED → LOGOUT_VERIFY → IDLE` — PASS
- W07 실제 `ACTIVE` 프로세스 종료: 기존 세션을 복구 로그아웃한 뒤 `IDLE` — PASS
- W08 실제 `LOGOUT_NAVIGATE` 프로세스 종료: 미완료 전환을 재검증해 `IDLE` — PASS
- W09 실제 `ACTIVE` 화면 off/on·background/foreground: 웹 화면과 `ACTIVE` 유지, 자격정보 입력 화면 재노출 없음 — PASS
- W10 단일 WebView 렌더러 Activity Manager VM crash: `LOCKED / WEB_PROCESS_GONE`, 안전 복구 뒤 `IDLE` — PASS
- 모든 복구 완료는 화면 복귀만으로 판정하지 않고 2초 뒤 `IDLE`, 종료 사유 없음으로 재확인했다.
- 세부 조건과 비성립 주입은 `WEB_POC_FAILURE_LOG.md`에 기록했다.

시험계정 자격정보는 테스트 완료 뒤 변경한다.
