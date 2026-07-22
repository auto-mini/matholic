# Web Gate 3 준비 검증

검증일: 2026-07-22 (Asia/Seoul)

## 판정

- Gate 3 runner 구현: **PASS**
- 전체 저장소 clean build·unit·lint: **PASS**
- A 기기 비자격정보 계측시험: **25/25 PASS**
- A 설치 version `0.3.2` / code `14`, 최종 `IDLE`: **PASS**
- 실제 시험계정 A↔B 시도 1: **82/100 뒤 `LOGIN_TIMEOUT`, G301 FAIL**
- 실제 시험계정 A↔B 시도 2: **74/100 뒤 `LOGIN_TIMEOUT`, G301 FAIL**
- 실제 시험계정 A↔B 시도 3: **100/100, A 50회·B 50회, G301 PASS**
- timeout 실패 폐쇄·자동 재시도 0·다음 계정 차단: **PASS**
- Web Gate 3 최종 판정: **아직 PASS 아님**

## 구현

- 기존 단일계정 Gate 2 화면과 상태 머신을 유지한다.
- Gate 3 화면에서 서로 다른 표시명의 시험계정 A·B를 한 번만 입력한다.
- 두 계정은 `CharArray` 기반 런타임 객체에만 유지하고 완료·실패·중단·Activity 종료 시 덮어쓴다.
- A/B 슬롯을 완료 회수의 parity로 결정해 정확히 교대한다.
- 로그인과 실제 표시명 확인 뒤 자동 로그아웃하며, 로그아웃 후 로그인 화면·빈 입력·checkbox 해제·session 삭제 재검증이 끝난 경우에만 회수를 증가시킨다.
- terminal 실패에서 runner와 메모리 자격정보를 폐기한다.
- 안전 중단은 현재 웹 상태를 신뢰하지 않고 recovery 검사를 실행한다.
- 비민감 진행값만 preferences에 동기 기록한다.

## 자동 검증

Web POC 단위시험 19개, 실패 0:

- 기존 보안정책·DOM·상태 머신 6개
- A/B 교대와 성공 뒤에만 회수가 증가하는 Gate 3 session 시험
- wipe 뒤 session 재사용 거부
- 입력 객체의 문자열 표현에 표시명·아이디·비밀번호가 포함되지 않음
- 실패한 attempt는 완료 회수와 A/B 슬롯을 전진시키지 않음
- 늦은 WebView 실패 callback, 제한된 DNS 재시도와 오류 문서 완료 경합 정책
- 비민감 network 사유·호스트 분류와 루프백 CONNECT 대상 parser/allowlist

A `SM-P610`, Android 13 계측시험 18개, 실패 0:

- 기존 공개 로그인 preflight·DOM·복구·보안 시험 15개
- 중단된 `RUNNING`을 `ABORTED`로 바꾸고 자동 재개하지 않음
- 정규화 후 같은 A/B 예상 표시명을 로그인 전에 거부
- 여섯 입력란 모두 state saving·autofill·가려진 touch 차단

0.2.3 보완 뒤 A `SM-P610`, Android 13 계측시험 22개, 실패 0:

- background/foreground 뒤 여섯 합성 입력값 삭제
- Gate 3 `LOGIN_SUBMIT` 중 종료: `ABORTED`, 완료 17 유지, 자동 재개 0
- Gate 3 `ACTIVE` 중 종료: `ABORTED`, 완료 17 유지, 자동 재개 0
- Gate 3 `LOGOUT_SUBMIT` 중 종료: `ABORTED`, 완료 17 유지, 자동 재개 0

전체 `scripts/build.ps1` clean 결과:

- Gradle 150 tasks 성공
- Probe 8/8, 잠긴 POC 1/1, Web POC 19/19
- 세 모듈 lint PASS
- 세 debug APK 생성 성공

## 보안검사

- applicationId: `com.local.matholickiosk.webpoc`
- 최신 versionName/versionCode: `0.3.2` / `14`
- 권한: `android.permission.INTERNET` 1개
- APK Signature Scheme v2: true, signer 1
- WebView debugging, ADB input, uiautomator, 접근성 gesture, JavaScript interface와 앱 로그 호출 없음
- preferences write는 상태·사유·Gate 3 status/completed/target/duration에 한정
- 실제 자격정보는 빌드·시험·ADB·문서에 사용하지 않음

## APK

- 파일: `artifacts/matholic-webpoc-gate3-0.3.2-debug.apk`
- 크기: 3,646,698 bytes
- SHA-256: `7EE595294C94AF69EC00C422819CE821852007AC12EF72A5A6F520081547D127`
- build 출력과 전달본 SHA-256 일치: PASS

## 실제 시험 전 조건

1. 채팅에 노출된 기존 시험계정 비밀번호 변경
2. 서로 다른 실제 표시명을 가진 두 번째 학생용 시험계정 준비
3. 두 계정 모두 실제 학생 데이터·권한이 없는 시험용임을 확인
4. 태블릿 화면에서만 두 계정 입력

## 실제 시도 1

- 종료: 2026-07-22 14:14:05 (Asia/Seoul)
- 정상 완료: 82/100, A 41회·B 41회
- 소요시간: 1,357,305 ms (22.62분)
- 최종 상태: `FAILED / LOCKED / LOGIN_TIMEOUT`
- 로그인 자동 재시도: 0
- 실패 뒤 추가 cycle: 0

서버 응답 지연, 일시 네트워크 지연 또는 연속 인증 빈도 중 원인은 확정하지 않았다. 실패 폐쇄는 정상 동작했지만 G301의 100/100 기준은 충족하지 못했다. 세부 기록은 `WEB_GATE3_RUN_LOG.md`에 있다.

0.2.1은 로그인 timeout을 완화하지 않고 cycle 사이 휴지시간만 5초로 늘렸다. 전체 clean build, A 계측 18/18과 설치 `IDLE`을 통과한 뒤 시도 2를 수행했다.

## 실제 시도 2

- 종료: 2026-07-22 14:49:33 (Asia/Seoul)
- 정상 완료: 74/100, A 37회·B 37회
- 소요시간: 1,548,210 ms (25.80분)
- 최종 상태: `FAILED / LOCKED / LOGIN_TIMEOUT`
- 로그인 자동 재시도: 0
- 실패 뒤 추가 cycle: 0
- 종료 2초 뒤 상태 고정: PASS

0.2.1의 5초 cycle 휴지에도 같은 종료 사유가 재현됐다. 두 시도 모두 70회 이상 정상 교차한 뒤 제출 후 로그인 결과 확인이 30초를 넘었다. 서버 응답 지연과 일시 네트워크 지연 중 원인은 확정하지 않으며, 5초 휴지만으로는 G301을 충족하지 못했다.

## 0.2.2 재시험 준비

- Gate 3 제출 후 결과 확인 timeout: 30초 → 60초
- 기존 단일계정 결과 timeout: 30초 유지
- 로그인 페이지 준비 timeout: 30초 유지
- Gate 3 cycle 휴지: 5초 유지
- 표시명 완전일치·로그인 자동 재시도 0·실패 폐쇄: 유지
- 전체 clean build: 150/150 tasks PASS
- A 계측시험: 18/18 PASS
- A 재설치 뒤 version `0.2.2` / code `4`, `IDLE`, reason 없음: PASS
- APK 빌드 출력·전달본 SHA 일치, v2 서명 true, signer 1, INTERNET 권한만 존재: PASS

## 실제 시도 3

- 종료: 2026-07-22 16:04:05 (Asia/Seoul)
- 정상 완료: 100/100, A 50회·B 50회
- 소요시간: 2,137,404 ms (35.62분)
- 최종 상태: `PASSED / IDLE / 사유 없음`
- 로그인 자동 재시도: 0
- 종료 직후와 2초 뒤 동일 상태 고정: PASS
- 저장 키: `state`, Gate 3 status/completed/target/duration만 존재
- G301 정상 A↔B 교차: **PASS**
- G308 완료 직후 앱·기기 재시작: **PASS**

시도 3의 성공으로 정상 교차 기준은 충족했다. 다만 성공 1회만으로 앞선 timeout의 단일 원인을 확정하지 않는다. G302·G303과 실계정 session 기반 G304·G305가 끝나기 전 Web Gate 3 최종 판정은 **아직 PASS가 아니다**.

완료 뒤 앱 프로세스 강제종료·재실행과 A 전체 재부팅·unlock·앱 재실행을 수행했다. 두 경우 모두 `PASSED / 100/100 / IDLE / 사유 없음`, 동일 소요시간과 자격정보 없는 저장 키 5개만 유지돼 G308을 통과했다.

## 0.2.3 가시성 상실 보완

- `onStop`에서 여섯 입력란 즉시 삭제
- 진행 중 Gate 3는 `ABORTED` 기록 후 런타임 자격정보 삭제·recovery 시작
- 진행 중 단일계정 민감 상태도 recovery 시작
- 합성 입력값으로 background/foreground 뒤 여섯 입력란 empty 계측: PASS
- 전체 clean build: 150/150 tasks PASS
- A 계측시험: 22/22 PASS
- A 재설치 뒤 version `0.2.3` / code `5`, `IDLE`, reason 없음: PASS
- APK 빌드 출력·전달본 SHA 일치, v2 서명 true, signer 1, INTERNET 권한만 존재: PASS

이 자동시험으로 G307의 background/foreground 부분을 통과했다. 이어 A에서 Gate 3 첫 입력란에 비자격정보 임시 문자만 넣고 실제 전원 버튼으로 화면 off/on·unlock한 뒤 값이 사라진 것을 사용자가 확인했다. 따라서 G307은 **PASS**다.

## 사람 손 없는 추가 실패주입

| ID | 검증 | 결과 | 범위 |
|---|---|---|---|
| G304 | `ACTIVE`+Gate 3 `RUNNING` 상태 재시작 | 자동 계약 PASS | 실제 인증 cookie를 의도적으로 잔류시킨 실계정 시험은 미수행 |
| G305 | `LOGIN_SUBMIT`·`ACTIVE`·`LOGOUT_SUBMIT` 상태 재시작 | 자동 계약 PASS | 세 상태 모두 `ABORTED`, 완료 17 유지, 자동 재개 0 |
| G306 | A Wi-Fi 실제 단절 공개 preflight | PASS | `LOCKED / NETWORK_ERROR`, Gate 3 실행·완료값 없음 |
| G306 | A Wi-Fi 실제 단절 + Gate 3 로그인 제출 중 상태 | PASS | 비자격정보 계측, `ABORTED`, 완료 17 유지, 자동 재개 0 |

G306 뒤 A Wi-Fi 연결·공식 로그인 host 도달을 확인하고 앱 데이터만 초기화해 version `0.2.3`, `IDLE`, reason 없음으로 복구했다. 실제 자격정보·가짜 로그인 제출·ADB 입력은 사용하지 않았다.

G304·G305의 상태 복구 계약은 자동 검증됐지만, 실제 인증 session을 의도적으로 남기거나 실계정 로그인·로그아웃 도중 프로세스를 죽이는 시험은 사용자가 계정을 다시 입력해야 하므로 별도 미수행으로 남긴다. 또한 G301 실제 100/100은 0.2.2에서 수행했으므로, 네트워크 경로가 변경된 최종 0.3.2의 정상 회귀 100회도 실계정 재입력 항목으로 남긴다.

## 추가 A 연결 검증

- 0.3.2 cold-start 20/20에서 실제 `PREFLIGHT → IDLE`, terminal 0, reason 0
- 시작 7,553~8,236 ms
- PSS 119,397~121,017 KB로 누적 증가 없음
- 파일디스크립터 254~261, 누적 증가 없음
- 손상된 저장 state + Gate 3 `RUNNING`: `ABORTED`, 완료 23 유지, 안전 복구
- 기존 `LOCKED` + Gate 3 `RUNNING`: `LOCKED`, `ABORTED`, 자동 재개 0
- 기존 `MAINTENANCE_REQUIRED` + Gate 3 `RUNNING`: 최초 시험에서 `LOCKED / PREVIOUS_LOCK`으로 의미가 축약되는 결함 발견
- 0.2.4에서 `MAINTENANCE_REQUIRED / PREVIOUS_MAINTENANCE` 보존으로 수정 후 A 계측 25/25 PASS

## 0.3.2 A WebView DNS 대응

- A의 WebView 150.0.7871.124에서 셸 DNS와 앱 Java DNS·HTTPS는 정상이지만 직접 WebView만 `ERROR_HOST_LOOKUP`을 지속 재현
- WebView 공급자 데이터 초기화, 동일 150 APK 재설치·재부팅, 단일 프로세스 시험은 효과 없음
- 공급자 구버전 롤백은 보안 저하가 있어 수행하지 않음
- 공식 프로세스별 WebView proxy override와 `127.0.0.1` 임의 포트 CONNECT 터널로 DNS만 Java 계층에 위임
- 터널은 제한된 HTTPS 목적지만 허용하고 TLS를 종료·복호화·기록하지 않음
- A 로그인 DOM 진단, 제품 preflight, 계측 25/25, cold-start 20/20 PASS

## 최종 무인 점검 상태

- A: `SM-P610`, Web POC `0.3.2` / code `14`
- 앱 상태: `IDLE`, reason 없음, preferences 키는 `state` 하나
- Wi-Fi: enabled, 공식 로그인 host 도달 PASS
- 설치 APK SHA-256과 전달본 SHA-256 일치: PASS
- 계측시험 패키지 제거: PASS
- 남은 실계정 절차와 무자격정보 감시·프로세스 종료 도구: `WEB_GATE3_REMAINING_MANUAL.md`

실제 정상 100회와 나머지 실패주입이 끝나기 전 Gate 3 PASS 또는 실제 학생 적용으로 보고하지 않는다.
