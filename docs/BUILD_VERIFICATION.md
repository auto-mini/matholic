# 빌드·보안 검증 기록

검증일: 2026-07-21, A 재검증 2026-07-22 (Asia/Seoul)

## 도구 기준

- JDK 17.0.19+10
- Android Gradle Plugin 9.3.0
- Gradle 9.6.1, wrapper 배포 SHA-256 고정
- Android 17 SDK API 37 / Build Tools 37.0.0
- AndroidX Test Runner 1.7.0 / Ext JUnit 1.3.0
- AndroidX WebKit 1.16.0
- minSdk 33, compileSdk/targetSdk 37

Android 17/API 37과 AGP 9.3.0은 검증일의 최신 안정 공식 문서를 기준으로 했다.

## 실행 결과

`scripts/build.ps1`로 외부 ASCII build 경로에서 clean 실행했다.

- Gradle: `BUILD SUCCESSFUL`, 150 tasks
- Probe 단위 테스트: 8개, 실패 0, 오류 0
  - redaction 7개
  - exact package allowlist 1개
- 잠긴 POC 단위 테스트: 1개, 실패 0, 오류 0
- Web POC 단위 테스트: 19개, 실패 0, 오류 0
- Probe lint: `No issues found.`
- POC lint: `No issues found.`
- Web POC lint: `No issues found.`
- Probe/POC/Web POC debug APK 생성 성공
- 세 APK 모두 APK Signature Scheme v2 검증 성공, signer 1

## Probe APK 검사

- applicationId: `com.local.matholickiosk.probe.debug`
- versionName/versionCode: `0.1.1-gate1-debug` / `2`
- minSdk/targetSdk: 33 / 37
- 요청 `<uses-permission>`: 0개
- 접근성 서비스 bind 보호: `android.permission.BIND_ACCESSIBILITY_SERVICE`
- target query/service package: `com.matholic.mathapp`
- `allowBackup=false`, `fullBackupContent=false`, data extraction exclusion, cleartext false
- INTERNET, CAMERA, SYSTEM_ALERT_WINDOW, external storage 권한 없음
- `performAction`, `dispatchGesture`, `uiautomator`, ADB tap/text, 앱 로그 호출 없음

## APK SHA-256

- Probe: `37D7BD8BBC8DB963F901563B12FB00A0B09F1A5ED2E4447676CD024EC1550023`
- 잠긴 POC: `E9755CE88330F0D2BC61E904D72F2C3197B6201B4E7FBDC90708D2A1518A3755`
- Web Gate 2 POC 0.1.0: `05E5F59A8F935AD41BA4DC190C7E123931940ACC49E8E2B2E2EE44A6CF7CE4AB` (2,541,773 bytes)
- Web Gate 3 POC 0.2.0: `5BE8691838CE33158EF80147E102D6FDF425765E439EEE43442C83D530F4DCD1` (2,560,469 bytes)
- Web Gate 3 POC 0.2.1: `06A2267025BF4971715B28C5E98098CD0C41136B94F88A97030C1C530C1040EA` (2,560,469 bytes)
- Web Gate 3 POC 0.2.2: `133751C0EB1728B1397E1D601F3E457898C252D71A7ABF5E1BE798FFA8277E95` (2,560,597 bytes)
- Web Gate 3 POC 0.2.3: `35F0A672511F85EE54316169F2AE5AF18692184735CF00782AF6B6BC5B23E6A5` (2,560,801 bytes)
- Web Gate 3 POC 0.2.4: `8B1CFA179E3BCC27F56E0FE20CA7FA15A80EADCE8DD469C8AC156DA59D74E886` (2,560,837 bytes)
- Web Gate 3 POC 0.3.2: `7EE595294C94AF69EC00C422819CE821852007AC12EF72A5A6F520081547D127` (3,646,698 bytes)
- Web Gate 3 POC 0.3.3: `26038D7F8205889E54342F3B01FE77E70D1E9013CC06B9AD33B9A09DF941896F` (3,646,986 bytes)

나열한 전달 파일은 ignored `artifacts/`에 복사했으며 `SHA256SUMS.txt`와 일치한다. 이전 Probe 0.1.0 APK는 복구 가능한 참고본으로 `artifacts/archive/`에 이동했으며 현재 SHA 목록 대상에서 제외했다.

## Web POC APK 검사

- applicationId: `com.local.matholickiosk.webpoc`
- 최신 versionName/versionCode: `0.3.3` / `15`
- minSdk/targetSdk: 33 / 37
- 요청 권한: `android.permission.INTERNET` 1개
- backup/data extraction/cleartext 차단 적용
- 공식 세 HTTPS host의 상위 탐색 allowlist
- 프로세스 전용 루프백 CONNECT는 제한된 HTTPS host만 허용하고 TLS를 종료·복호화·기록하지 않음
- WebView debugging, form 저장, autofill, screenshot/최근 앱 preview 차단
- 좌표 selector, 접근성 gesture, uiautomator, ADB 입력, JavaScript interface와 앱 로그 호출 없음
- generic 자격정보 형태 소스 검사 발견 0
- SM-P613 Android 14 계측시험 12개, 실패 0
- SM-P610 Android 13 계측시험 25개, 실패 0

## 기기 설치 smoke check

- SM-P610 ADB 인증: 성공
- Probe `adb install -r`: 성공
- MainActivity 시작: 성공
- 설치 package/version/SDK 재확인: 일치
- 접근성 서비스: 사용자 제한 설정 승인 후 enabled/bound 확인
- 비공개 앱 UID redacted 캡처 요청: 성공, 매쓰홀릭 클릭·입력 0

## Gate 1 기기 검증 요약

- 로그인 및 로그아웃 후 화면에서 아이디·비밀번호 EditText 2개 empty 확인
- 로그아웃 뒤 Android 홈 전환·매쓰홀릭 재전면화 후에도 비로그인 구조 fingerprint 유지
- 개인정보 평문 0개, 좌표 selector 0개
- 기억하기 시각 상태를 접근성 트리에서 읽을 수 없음
- 로그인 직후/홈 화면에서 의미 기반 `CLICK` Action 0개
- 문제풀이 좌상단 `<` 복귀 아이콘과 홈 제목줄 계정 진입점이 접근성 Action으로 미노출
- 프로필 팝업과 `로그아웃` Button 탐지 및 수동 로그아웃·빈 입력란 검증은 성공

## 판정

Gate 0은 완료됐다. Android 앱 접근성 Gate 1은 복수의 필수 의미 기반 동작을 구현할 수 없어 **최종 FAIL**이며 기존 `poc`는 잠금 상태다. 별도로 승인된 공식 웹 경로의 `webpoc` 구현과 비자격정보 검증, 실제 시험계정 정상 cycle 20/20과 필수 실패주입은 통과했다. 따라서 정의된 POC 범위의 Web Gate 2는 **PASS**다. Gate 3 runner 구현과 비자격정보 검증은 통과했고 0.2.2와 0.3.2 실제 정상 100/100, G302~G308 실계정·기기 실패주입을 통과했다. 최신 0.3.3은 G303에서 발견된 실패 결과 영속화 순서를 수정하고 전체 build 150/150, A 계측 27/27, 깨끗한 설치 `IDLE`, 해시·v2 서명·단일 INTERNET 권한 검사, G303~G305 실기 재검증과 최종 실제 정상 100/100 회귀를 통과했다. 따라서 정의된 Web Gate 3 범위도 **PASS**다. 실제 학생 DB, QR, Device Owner와 생산 자동화는 이 판정 범위가 아니다. 상세 결과는 `docs/WEB_POC_VERIFICATION.md`와 `docs/WEB_GATE3_VERIFICATION.md`에 기록했다.

---

## Gate 4 alpha 추가 검증 — 2026-07-23

### 빌드

- `scripts/build.ps1` clean 전체 빌드: `BUILD SUCCESSFUL`, 204 tasks
- Probe 단위 8개, 잠긴 POC 단위 1개, Web POC 단위 19개, Kiosk 단위 9개: 실패 0
- 네 모듈 lint: 오류 0
  - 기존 세 모듈은 경고 0~3개
  - `kiosk`는 한국어 전용 alpha UI의 hardcoded/localization 등 비차단 경고 62개
- 네 debug APK assemble 성공

### A 기기 계측

- 대상: SM-P610, Android 13/API 33
- `webpoc` 0.3.4 회귀 계측: 27/27 통과
- `kiosk` 0.4.0-alpha02 계측: 6/6 통과
- Keystore AES-GCM fresh IV/AAD, Room 암호문 저장, QR 폐기·재발급, 현재반·보강 수명, 관리자 PIN UI, `FLAG_SECURE`, 1회용 bridge를 검증
- 외부 shell UID의 credential provider 조회: signature permission denial 확인
- 존재하지 않는 1회용 handle로 `webpoc` 실행: `CREDENTIAL_BRIDGE_EMPTY` + `LOCKED` 후 호출 앱으로 자동 복귀 확인
- 실패주입 뒤 `webpoc` app data를 비우고 다시 설치해 잔여 잠금 상태를 제거

### 최종 설치 상태

- `kiosk` 0.4.0-alpha02/code 2와 `webpoc` 0.3.4/code 16 설치 성공
- 두 APK signer SHA-256 일치:
  - `0b6bef1c18a3beb397b655e895d30412aa749b712fd801c26a9b9e386e8579f8`
- 두 APK 모두 APK Signature Scheme v2, signer 1
- signature permission registry와 `webpoc` grant 확인
- `kiosk` 요청 권한:
  - `android.permission.CAMERA`
  - AndroidX 내부 `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`
  - 전이 manifest의 `INTERNET`·`ACCESS_NETWORK_STATE`는 명시 제거
- `webpoc` 요청 권한:
  - `android.permission.INTERNET`
  - `com.local.matholickiosk.permission.CREDENTIAL_BRIDGE`
- 설치 뒤 `kiosk`가 `ADMIN_IDLE`의 관리자 PIN 최초 설정 화면으로 시작함
- PIN, 학생정보와 계정정보는 설정하지 않은 깨끗한 상태로 전달

### APK SHA-256

- Kiosk 0.4.0-alpha02: `85B682223FF794C306716A800A5CF28CCF80B668784DC00EB66D7B5747BE77E2` (42,268,290 bytes)
- Web POC 0.3.4: `70801648B92BB3651E1327367B65465EB3B93654F662912D6356245BB1B1B951` (3,652,834 bytes)

### 판정

Gate 4의 agent-only 구현·자동 검증·설치는 완료했다. 실제 QR 카메라 인식과 시험계정 1회 Web 왕복은 사람 손이 필요한 수동 검증이 남아 있으므로 Gate 4 전체 판정은 아직 **미완료**다. Device Owner/Lock Task는 Gate 5 승인 전이므로 수행하지 않았다.

---

## Gate 4 alpha03 실제 수동 검증 — 2026-07-23

### 카메라 사용성 변경

- `kiosk` `0.4.0-alpha03`/code 3
- 전면 카메라를 기본 선택
- QR 대기 화면에서 `후면으로`/`전면으로` 전환
- 선호 렌즈가 없으면 사용 가능한 렌즈로 fallback
- 오래된 비동기 CameraX bind 요청 무효화
- 카메라 정책 JVM 테스트 4개 추가

### 코드 검증

- clean 전체 빌드: `BUILD SUCCESSFUL`, 204 tasks
- 전체 단위 테스트: Probe 8, 잠긴 POC 1, Web POC 19, Kiosk 13; 총 41개, 실패 0
- 네 모듈 lint 오류: 0
- `kiosk` 단위 테스트: 13/13, 실패 0
- `kiosk` lint: 오류 0
- `kiosk` debug assemble: 성공
- A에 데이터 유지 덮어설치: 성공
- 설치 확인: `versionName=0.4.0-alpha03`, `versionCode=3`

기존 `kiosk` 계측 6/6과 `webpoc` 계측 27/27은 alpha02/0.3.4에서 통과했다. alpha03 설치 뒤에는 시험계정 DB를 보존하기 위해 `clearAllTables()`를 수행하는 기존 기기 계측 suite를 다시 실행하지 않았다.

### A 실제 QR·Web 왕복

- 후면 실제 QR → 시험계정 확인 완료 → 문제 화면 → 채점 끝내기 → `QR_READY`: PASS
- 전면 실제 QR → 시험계정 확인 완료 → 채점 끝내기 → `QR_READY`: PASS
- 후면 전환 → 전면 복귀 CameraX bind: PASS
- 재발급 전 QR 거부 → 자동 `QR_READY`: PASS
- 재발급 신 QR 로그인·로그아웃: PASS
- 구 QR+신 QR 동시 노출 → 복수 QR 거부, Web 미실행: PASS
- 현재 반 외 QR → 거부, Web 미실행: PASS
- 존재하지 않는 합성 Web 계정 → `WEB_SESSION_NOT_CLEAN` + `LOCKED`: PASS
- 앱 종료 실패주입 → 실패폐쇄, 관리자 안전 종료·재시작: PASS

### 시험 후 정리와 최종 기기 상태

- 반외·WEBFAIL 촬영 QR은 재발급해 사진 속 token을 폐기
- 시험계정 신 QR은 유지
- 활성 수업: `GATE4-TEST`
- 상태: `QR_READY`
- 활성 렌즈: 전면 (`후면으로` 버튼 표시)
- `stay_on_while_plugged_in`: 시험 전 값 `0`으로 복구

### alpha03 APK

- 파일: `artifacts/matholic-kiosk-gate4-0.4.0-alpha03-debug.apk`
- 크기: 42,284,782 bytes
- SHA-256: `D1625CD8F8D5FA7DF635C4F440384FAEE277B0925E3451E87C58349CD539E363`
- A 설치본 SHA-256과 아티팩트 SHA-256 일치
- Kiosk/Web POC 모두 APK Signature Scheme v2, signer 1
- 두 APK signer SHA-256 일치: `0b6bef1c18a3beb397b655e895d30412aa749b712fd801c26a9b9e386e8579f8`

### 판정

정의된 시험계정 기반 **Gate 4 alpha 범위는 PASS**다. QR 인쇄, 실제 학생 파일럿, 장시간 성능 통계, 단일 release 패키징과 Device Owner/Lock Task는 이 판정에 포함하지 않는다.

---

## Gate 4 alpha04 운영 보강 — 2026-07-23

### 구현

- `kiosk` `0.4.0-alpha04`/code 4
- 학생 표시명·마스킹 이름 갱신
- 학생 자격정보 fresh-IV 재암호화와 성공·실패 경로 입력 배열 덮어쓰기
- 학생 논리적 비활성화, 활성 목록 제외, 이전 QR hash 폐기
- 명시적 경고 뒤 Android 인쇄 서비스에 마스킹 이름+QR의 ISO A4 1페이지 전달
- 인쇄 요청 감사기록과 서비스 전달 직후 앱의 QR 원문·bitmap 제거

### 코드·기기 검증

- `kiosk` JVM 단위 테스트 14개: 통과
- A 운영 DB를 지우지 않는 선택 계측 4개: 통과
  - 저장소 계측 3개
  - QR 인쇄 PDF 계측 1개
- 기존 전체 `kiosk` 계측 suite는 테스트 시작 시 `clearAllTables()`를 호출하므로 실제 시험계정 DB 보존을 위해 실행하지 않음
- A 데이터 유지 덮어설치와 기존 반·학생 보존: 통과
- 합성 학생 표시정보·자격정보 갱신과 합성 학생 3명 비활성화: 통과
- Android 인쇄 미리보기 ISO A4 1페이지: 통과
- 앱 복귀 시 표시 QR 즉시 제거와 인쇄 버튼 비활성화: 통과
- 실제 프린터 출력·종이 결과물: 수행하지 않음
- 이전에 의도적으로 남긴 Web POC 잠금 상태로 첫 시도가 `WEB_SESSION_NOT_CLEAN`에 실패한 뒤 관리자 안전 확인으로 `IDLE` 복구
- 같은 새 시험 QR로 로그인 → 정확한 학생 확인 → 문제 화면 → 채점 끝내기 → 로그아웃 → `QR_READY`: 통과

### 최종 결과

- clean 전체 빌드: `BUILD SUCCESSFUL`, 204 tasks
- 전체 JVM 단위 테스트: Probe 8, 잠긴 POC 1, Web POC 19, Kiosk 14; 총 42개, 실패 0
- 네 모듈 lint 오류: 0
- A 선택 계측 최종 재실행: 4/4 통과
- 최종 APK: `artifacts/matholic-kiosk-gate4-0.4.0-alpha04-debug.apk`
- 크기: 42,318,005 bytes
- SHA-256: `BA86DE70D3BD383150C00B71A1598220F10A8126B3C331B419DD363CC6457376`
- 빌드 APK·전달 APK·A 설치본 SHA-256 일치
- APK Signature Scheme v2: 통과, signer 1
- signer SHA-256: `0b6bef1c18a3beb397b655e895d30412aa749b712fd801c26a9b9e386e8579f8`
- 설치 확인: `versionName=0.4.0-alpha04`, `versionCode=4`

---

## Gate 5 alpha05 전용기기 잠금 — 2026-07-24

### 구현·자동 검증

- `kiosk` `0.5.0-alpha05`/code 5
- `webpoc` `0.3.5`/code 17
- Device Admin receiver, Device Owner 정책, 전용 HOME과 Lock Task allowlist 구현
- allowlist: `com.local.matholickiosk.kiosk`, `com.local.matholickiosk.webpoc`만 포함
- Lock Task 기능 `NONE`, 잠금 중 overlay 창 생성 제한, keyguard 비활성화
- QR·PIN·Web 구간 `KEEP_SCREEN_ON`, 관리자 PIN 성공 뒤에만 Lock Task 종료
- clean 전체 빌드: `BUILD SUCCESSFUL`, 204 tasks
- 전체 JVM 단위 테스트: Probe 8, 잠긴 POC 1, Web POC 19, Kiosk 17; 총 45개, 실패 0
- 네 모듈 lint 오류: 0
- 네 debug APK assemble: 성공
- 공장초기화 전 A 전체 Kiosk 계측 9/9: 통과
- Device Owner 등록 후 test APK를 생산 allowlist에 추가하지 않았으므로 전체 계측 suite는 재실행하지 않음

### A 프로비저닝

- 대상: Samsung SM-P610, Android 13/API 33
- 공장초기화 뒤 Android 사용자 1명, Android 계정 0개 확인
- `dpm set-device-owner`: 성공
- Device Owner: `com.local.matholickiosk.kiosk/.admin.KioskDeviceAdminReceiver`
- 전용 HOME resolve: `com.local.matholickiosk.kiosk/.MainActivity`
- 설치 버전: Kiosk `0.5.0-alpha05`/5, Web POC `0.3.5`/17
- 최종 클린 Kiosk APK `adb install -r`: 앱 데이터와 Device Owner 보존
- 빌드 Kiosk APK와 A 설치본 SHA-256: 일치
- 빌드 Web POC APK와 A 설치본 SHA-256: 일치

### 실제 잠금·Web·재부팅 검증

- QR/PIN 화면 Lock Task: `LOCKED`
- 홈·최근 앱·알림창: Kiosk 밖으로 전환되지 않음
- 잠금 중 Android 설정 실행: 차단
- 관리자 PIN 성공: `NONE`, 관리자 화면 유지
- 관리자 화면 뒤로 가기: PIN 화면과 `LOCKED`로 복귀
- 전면 새 QR → Web POC `시험계정 확인 완료`: 통과
- Web 문제 화면에서도 Lock Task `LOCKED`, 화면 켜짐, keyguard 비활성: 유지
- `채점 끝내기` → 약 8초 뒤 Kiosk `QR_READY`: 통과
- 재부팅 뒤 Device Owner·전용 HOME·Kiosk 자동 실행·`LOCKED`: 유지
- 재부팅 후 `RECOVERY_REQUIRED` 관리자 복구와 기존 수업 안전 종료: 통과

실기 중 발견한 관리자 잠금 해제 lifecycle 경쟁 조건과 카메라 정지 뒤 화면/keyguard 회귀를 수정한 뒤 위 항목을 다시 확인했다.

### Gate 5 전달 APK

- Kiosk: `artifacts/matholic-kiosk-gate5-0.5.0-alpha05-debug.apk`
  - 크기: 42,335,691 bytes
  - SHA-256: `BE8020B0A8166A894756ECBBE77F1F16DA3A41895D30A287257CE04B19794056`
- Web POC: `artifacts/matholic-webpoc-0.3.5-debug.apk`
  - 크기: 3,652,838 bytes
  - SHA-256: `38AD870586CFF1D106928093E47BE75EC4881DCCBDF4686F26F21C5CC15EED30`
- 두 APK 모두 APK Signature Scheme v2: 통과, signer 1
- 두 APK signer SHA-256 일치: `0b6bef1c18a3beb397b655e895d30412aa749b712fd801c26a9b9e386e8579f8`
- `artifacts/SHA256SUMS.txt` 갱신 및 전달 APK와 일치 확인

### 판정

정의된 시험계정 기반 **Gate 5 alpha 범위는 PASS**다. 실제 학생 파일럿, 실제 프린터 출력, 장시간 무인 운전, USB 디버깅 제거 후 회귀, release signing·업데이트 채널과 생산 배포는 미검증이다.

---

## Release RC 패키징 — 2026-07-24

### 구성

- Kiosk `0.5.0-rc01`/code 6
- Web POC `0.3.5-rc01`/code 18
- 저장소 밖 PKCS12 RSA 4096 release signer 생성
- 서명 비밀번호는 현재 Windows 사용자 DPAPI로 보호
- Gradle release는 외부 환경 서명이 없으면 실패폐쇄
- configuration cache와 장기 daemon을 끈 전용 빌드 스크립트 사용

### 빌드·검증

- `scripts/build-release.ps1`: `BUILD SUCCESSFUL`, 158 tasks
- Kiosk JVM 17/17, Web POC JVM 19/19: 실패 0
- Kiosk/Web release lint 오류: 0
- 두 release APK assemble: 성공
- applicationId·versionName·필수/금지 권한: 일치
- `debuggable=false`: 통과
- APK Signature Scheme v2: 통과
- signer: 각 1개, 두 APK 일치
- Android Debug signer 거부: 통과
- zipalign: 통과
- 서명 환경 제거 후 별도 build root의 `:kiosk:assembleRelease`: `Release signing is required`로 실패
- 새 PowerShell 프로세스의 한글 APK 경로 분석 실패를 발견해 임시 ASCII 경로 staging으로 수정하고 child-process 검증 재통과

### RC APK

- Kiosk: `artifacts/matholic-kiosk-0.5.0-rc01-release.apk`
  - 크기: 38,316,454 bytes
  - SHA-256: `A7BEAA146247D245D33C13792B62E5784B65AE50A5387701205A35B6A9FD43F1`
- Web POC: `artifacts/matholic-webpoc-0.3.5-rc01-release.apk`
  - 크기: 3,057,056 bytes
  - SHA-256: `1462755BC90C35CD1B91F8922357B90C334001C21A61A8A01F7142D63A3F12BF`
- signer SHA-256: `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### 판정

release RC 빌드와 정적 검증은 **PASS**다. SM-S918N Android 폰에 암호화된 PKCS12 키만 복사하고 PC 원본·폰 복사본·marker SHA-256과 release signer 일치를 확인했다. 사용자는 별도 복구 비밀번호 보관을 로컬 창에서 확인했다. signer가 다른 현재 debug Device Owner A에는 아직 설치하지 않았으므로 release 실기와 생산 배포는 **미완료**다.

---

## Release RC02 A 운영 전환 — 2026-07-24

### 변경

- Kiosk `0.5.0-rc02`/code 7
- Web POC `0.3.5-rc02`/code 19
- 관리자 화면에 `Web 세션 안전 정리` 추가
- 복구 Intent는 정확한 Kiosk package와 같은 signer만 허용
- 정리 성공 시 Web 상태를 `IDLE`로 확정하고 관리자 화면으로 자동 복귀
- 실패 시 사유를 Kiosk에 반환하고 잠금 상태 유지

### 자동 검증

- 네 모듈 전체 debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: Probe 8, 잠긴 POC 1, Web POC 21, Kiosk 17; 총 47개, 실패 0
- 네 모듈 debug lint 오류: 0
- 네 debug APK assemble: 성공
- 최종 release 빌드: `BUILD SUCCESSFUL`, 158 tasks
- Kiosk/Web release lint 오류: 0
- applicationId·versionName·권한·`debuggable=false`: 통과
- APK Signature Scheme v2, signer 1, 두 앱 signer 일치, Debug signer 거부: 통과
- zipalign: 통과

### 최종 RC02 APK

- Kiosk: `artifacts/matholic-kiosk-0.5.0-rc02-release.apk`
  - 크기: 38,316,550 bytes
  - SHA-256: `628BF4D5A4F25BCDC556835A671D1F06CCEB00787584239FC7AAB33C83230A6B`
- Web POC: `artifacts/matholic-webpoc-0.3.5-rc02-release.apk`
  - 크기: 3,059,360 bytes
  - SHA-256: `23487BC235ABAC2C4ED22689AEAB0C9C6388E4C4AABEBEC120F398125F25EE01`
- signer SHA-256: `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A release 실기

- 대상: Samsung SM-P610, Android 13/API 33
- 두 번째 공장초기화 뒤 사용자 1명, Android 계정 0개 확인
- release Kiosk Device Owner 등록과 두 앱 설치: 성공
- 관리자 PIN·시험 반·시험 학생·신규 QR 재등록: 완료
- 최종 전달 APK와 A의 설치 base APK SHA-256: 두 앱 모두 일치
- QR → 자동 로그인 → 실제 문제 화면 → `채점 끝내기` → `QR_READY`: 통과
- Web POC 강제 종료 → `WEB_SESSION_FAILED`·`LOCKED`: 실패폐쇄 확인
- 관리자 PIN → 자체 `Web 세션 안전 정리` → 자동 로그아웃·관리자 복귀: 통과
- 복구 뒤 같은 신규 QR 정상 왕복: 통과
- 홈·최근 앱·알림창: 다른 앱·설정으로 이탈하지 않음
- 완전 재부팅 뒤 Device Owner·전용 HOME·자동 실행·`LOCKED`: 유지
- 재부팅의 `RECOVERY_REQUIRED` → 관리자 자체 Web 정리 → 기존 수업 안전 종료·새 수업 시작: 통과
- 개발자 옵션·USB 디버깅 끄기 → ADB 기기 목록에서 A 제거: 통과
- 케이블 분리 뒤 물리 재부팅 → 자동 Kiosk·`RECOVERY_REQUIRED` → 관리자 복구·`QR_READY`: 통과
- ADB 없는 상태의 알림창 차단, 홈·최근 앱 버튼 미노출, 뒤로가기 반복 무반응: 통과
- `QR_READY` 120분 연속 운전, 30분 간격 네 관찰 시점의 화면·카메라·오류 없음: 통과
- 연속 운전 뒤 QR → 문제 화면 → `채점 끝내기` → `QR_READY`: 통과
- 실제 프린터 2종 크기 출력과 종이 QR → 문제 화면 왕복: 통과
- 최종 상태: `QR_READY`, 전면 카메라, `LOCKED`

### 판정과 남은 항목

release RC02의 정의된 자동 검증, 핵심 정상 왕복, 비정상 Web 세션 자체 복구, 재부팅 복구, USB 디버깅 제거, ADB 없는 물리 잠금, 120분 연속 운전과 실제 프린터·종이 QR 왕복은 모두 **PASS**다.

---

## RC03 사용성 개선 내부 보관본 — 2026-07-25

### 버전

- Kiosk `0.6.0-rc03`/code 8
- Web POC `0.4.0-rc03`/code 20

### 자동 검증

- 네 모듈 전체 debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: Probe 8, 잠긴 POC 1, Web POC 24, Kiosk 17;
  총 50개, 실패 0
- 네 모듈 debug lint 오류: 0
- 네 debug APK assemble: 성공
- Kiosk/Web 계측시험 소스 컴파일: 성공
- release 빌드: `BUILD SUCCESSFUL`, 158 tasks
- release lint 오류: 0
- applicationId·versionName·필수/금지 권한·`debuggable=false`: 통과
- APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부: 통과
- zipalign: 통과

### RC03 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc03-release.apk`
  - 크기: 34,941,240 bytes
  - SHA-256: `5546B399361227A38039AC0464A18DAE51DC17093B6DB530BEEFD0ABBD022AE6`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc03-release.apk`
  - 크기: 3,079,332 bytes
  - SHA-256: `64A8C7D6DFE97B8BBBD0591CBABF1E538E082EC86AC4721D8350C97B64944A3E`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 안전 정책

- 설치 전 A: Kiosk `0.5.0-rc02`/code 7, Web POC `0.3.5-rc02`/code 19
- Web POC, Kiosk 순서의 `adb install -r`: 둘 다 성공
- 설치 후 A: Kiosk `0.6.0-rc03`/code 8, Web POC `0.4.0-rc03`/code 20
- 두 package UID, firstInstallTime, dataDir: 설치 전후 동일
- 설치된 APK signer, Device Owner, 전용 HOME와 Kiosk 실행: 유지
- HOME·최근 앱·뒤로가기 입력: Kiosk `MainActivity`와 Lock Task `LOCKED` 유지
- 설치 직후 치명적 AndroidRuntime 예외: 없음

### 에뮬레이터 계측시험

- Android 13 AOSP ATD 일회용 에뮬레이터에서 A 설치 시점 소스:
  Web 30개, Kiosk 10개, 총 40개 실패 0
- 결과 페이지 선차폐와 SPA 인코딩 경로 차단 보강 뒤 Web 33개 실패 0
- 비대칭 프린터 DPI 보강 뒤 Kiosk 11개 실패 0

### 미검증

- 실제 사이트의 자동 학습지 진입, 두 탭 제한, 문제 입력 확대와 오답 번호 요약은 미실기다.
- 미리보기 없는 전면·후면 QR 분석 단독 바인딩, PDF Quick Share와 직접 프린터 실패 로그도 미실기다.
- 결과 페이지 선차폐, 비대칭 프린터 DPI와 SPA 인코딩 경로 차단은 RC04에
  포함해 아래와 같이 A에 반영했다.

### A 직접 인쇄 읽기 전용 진단

- Android 내장 IPP 서비스: 설치·활성·바인딩
- 이전 QR 인쇄 작업 한 건: Android `STATE_STARTED`(3),
  `is_canceling=true` 상태에서 장시간 정체
- 스풀 PDF: 아직 존재
- A→프린터 ICMP와 IPP TCP, PC→프린터 IPP TCP: 연결 성공
- BIPS 프로세스 로그: 인증·TLS·소켓·IPP 오류를 확정할 기록 없음

네트워크 도달 실패는 주원인에서 제외할 수 있다. 작업이 IPP 처리 또는 취소
완료 단계에서 정체된 사실은 확인했지만, 사용자 부재 중 대기열·스풀러를
변경하거나 새 비밀 QR을 전송하지 않았다. 사용자 복귀 뒤 기존 작업을 통제된
방법으로 제거하고 대기열 비움을 확인한 다음, 새 시험 작업 한 건의 BIPS와
Print Spooler 로그를 동시에 수집해야 정확한 원인을 확정할 수 있다. 프린터
주소와 작업 ID는 문서·Git에 기록하지 않았다.

---

## RC04 무인 검증본 — 2026-07-25

### 버전과 보강 범위

- Kiosk `0.6.0-rc04`/code 9
- Web POC `0.4.0-rc04`/code 21
- RC03 뒤에 추가한 결과 페이지 선차폐, SPA 인코딩 경로 차단과 비대칭
  프린터 DPI 계산을 포함

### 자동 검증

- 네 모듈 전체 debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release 빌드와 단위시험·release lint·서명 검사:
  `BUILD SUCCESSFUL`, 158 tasks
- Android 13 AOSP ATD 일회용 에뮬레이터 계측시험:
  - Web POC 33개
  - Kiosk 11개
  - 총 44개, 실패 0

### RC04 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc04-release.apk`
  - 크기: 34,941,240 bytes
  - SHA-256: `5A99CFCDEDA3206D19D51884B44BF2D224259059967F44AB16315B2507D2437B`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc04-release.apk`
  - 크기: 3,080,480 bytes
  - SHA-256: `2E955D1DD52F5989DA3219C7F3FB5D2C8DC036FDAA2546A612F33FE315742CDD`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 대상: Samsung SM-P610, Android 13/API 33
- 설치 전 ADB `device`, serial·모델, 배터리 100%·USB 전원, 설치 버전,
  signer와 Device Owner를 읽기 전용으로 확인
- Web POC, Kiosk 순서의 `adb install -r`: 둘 다 성공
- 설치 후 A: Kiosk `0.6.0-rc04`/code 9,
  Web POC `0.4.0-rc04`/code 21
- 두 package UID, firstInstallTime, dataDir: RC03 설치 때와 동일
- 설치된 두 APK signer: 보관 RC04와 일치
- Device Owner:
  `com.local.matholickiosk.kiosk/.admin.KioskDeviceAdminReceiver` 유지
- 전용 HOME: `com.local.matholickiosk.kiosk/.MainActivity` 유지
- Lock Task: `LOCKED` 유지
- 설치 직후 Matholic 관련 치명적 AndroidRuntime 예외: 없음
- 설치 전부터 화면은 꺼져 있었고 설치 뒤에도 `mAwake=false`를 유지했다.
  물리 화면을 깨우거나 관리자 PIN을 입력하지 않았으므로 현재 UI 상태는
  확인하지 않았다.

### 미검증

- RC04 실제 사이트의 자동 학습지 진입, 두 탭 제한, 문제 입력 확대,
  결과 상세 선차폐와 오답 번호 요약
- 미리보기 없는 전면·후면 QR 분석과 실물 QR 왕복
- PDF Quick Share, 65×90mm 카드·30×30mm QR 실제 크기와 종이 QR 재인식
- 정체된 기존 인쇄 작업을 안전하게 제거한 뒤의 직접 인쇄 재시험
- 관리자 PIN이 필요한 현재 UI 확인과 정상 `QR_READY` 복귀

---

## RC04 이후 관리자 비동기 안전성 보강 — 2026-07-25

### 확인한 문제

- 반을 바꾼 직후 이전 반의 명단이 새 조회 완료 전까지 UI 판단에 남을 수 있었다.
- 늦게 도착한 이전 반 조회·저장 결과가 현재 반 명단을 일시적으로 덮을 수 있었다.
- Web 안전정리와 후속 수업 시작·종료 DB 반영이 끝나기 전에 같은 요청을 다시
  시작할 수 있었다.

### 변경

- 반 선택이 바뀌면 이전 명단을 즉시 비우고 명단 의존 버튼을 조회 완료까지
  비활성화한다.
- 반 조회마다 세대 토큰을 발급해 현재 선택의 최신 결과만 반영한다. 같은 반을
  다시 선택한 경우에도 이전 요청 결과를 거부한다.
- Web 안전정리부터 후속 수업 시작·종료 DB 반영까지 단일 실행 gate로 묶고,
  성공·실패 뒤에만 관련 버튼을 다시 활성화한다.

### 검증

- 커밋: `206b6e7`
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험:
  - Probe 8개
  - 잠긴 POC 1개
  - Web POC 25개
  - Kiosk 23개
  - 총 57개, 실패 0
- 네 모듈 debug lint와 debug APK assemble: 성공
- Android 13 AOSP ATD 일회용 에뮬레이터 Kiosk 계측시험:
  11개, 실패 0

### 배포 상태

이 보강은 아래 RC05에 포함해 A에 설치했다. A에서 빠른 반 전환과 Web
안전정리 중 연속 탭 실기는 아직 미수행이다.

### QR 폐기 확인과 학생 변경 단일 실행

- 커밋: `ea6ad72`
- `QR 폐기·재발급`은 학생 이름, 기존 QR의 즉시 무효화와 되돌릴 수 없다는
  경고를 확인한 뒤에만 실행한다.
- 학생 등록·이름 변경·로그인 정보 변경·비활성화·QR 재발급은 한 번에 하나만
  실행한다. 성공 뒤 새 학생 목록이 화면에 반영되거나 실패가 확정되기 전에는
  학생 선택과 다른 학생 변경 버튼을 다시 활성화하지 않는다.
- 학생 변경 대상은 대화상자를 연 시점의 student ID로 고정되며, 자격정보
  `CharArray`는 성공·실패 모두 저장소 `finally`에서 지우는 것을 재확인했다.
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: 총 57개, 실패 0
- Android 13 AOSP ATD 일회용 에뮬레이터 Kiosk 계측시험:
  12개, 실패 0
  - 새 회귀시험은 재발급 버튼만 눌렀을 때 저장된 QR hash가 바뀌지 않음을 확인

이 변경도 아래 RC05에 포함해 A에 설치했다. 실제 관리자 화면의 확인 문구와
연속 탭 동작은 사용자 복귀 뒤 확인한다.

### 활성 반 이름 중복 차단

- 커밋: `461ccd3`
- 앞뒤 공백을 제거한 활성 반 이름은 SQLite `NOCASE` 비교로 중복 생성을
  거부한다. 빠른 연속 생성으로 구분할 수 없는 같은 이름의 반이 생기지 않는다.
- 반 삭제는 기존처럼 반과 소속 관계만 삭제하며, 삭제한 반의 이름은 새 반에서
  다시 사용할 수 있다.
- 스키마와 기존 데이터는 변경하지 않아 보존형 업데이트에 마이그레이션이
  필요하지 않다.
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: 총 57개, 실패 0
- Android 13 AOSP ATD 일회용 에뮬레이터 Kiosk 계측시험:
  13개, 실패 0

기존 A 데이터에 이미 같은 이름의 활성 반이 있는지는 화면을 깨우지 않고
확인하지 않았다. 새 중복 생성 차단은 아래 RC05 설치본부터 적용된다.

### 수업 저장소 불변조건

- 커밋: `8881cd8`
- 수업 시작 트랜잭션은 정규 반 학생 또는 일회성 보강 학생을 최소 한 명
  요구한다.
- 이미 `sessionId`가 있는 상태에서는 새 수업을 저장하지 않아 기존 활성
  수업을 덮어쓰지 않는다.
- 수업 종료 트랜잭션은 활성 `sessionId`가 있을 때만 보강 명단과 수업 상태를
  종료한다. 이미 종료된 수업의 중복 종료를 거부한다.
- 학생·반·현재 수업 검증과 상태 변경을 같은 Room 트랜잭션 안에서 수행한다.
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: 총 57개, 실패 0
- Android 13 AOSP ATD 일회용 에뮬레이터 Kiosk 계측시험:
  14개, 실패 0

이 저장소 제약은 아래 RC05에 포함해 A에 설치했다. 실제 관리자 수업
시작·종료 회귀는 사용자 복귀 뒤 수행한다.

---

## RC05 관리자 안전성 검증본 — 2026-07-25

### 버전과 포함 변경

- Kiosk `0.6.0-rc05`/code 10
- Web POC는 소스와 A 설치본 모두 `0.4.0-rc04`/code 21 유지
- 포함 커밋:
  - `206b6e7`: 관리자 명단 조회 세대와 수업 Web 작업 단일 실행
  - `ea6ad72`: QR 폐기 확인과 학생 변경 단일 실행
  - `461ccd3`: 활성 반 이름 중복 차단
  - `8881cd8`: 수업 시작·종료 저장소 불변조건
  - `0025029`: RC05 versionCode와 릴리스 검증본

### 자동 검증

- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: Probe 8, 잠긴 POC 1, Web POC 25, Kiosk 23;
  총 57개, 실패 0
- 네 모듈 debug lint와 debug APK assemble: 성공
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- Android 13 AOSP ATD 일회용 에뮬레이터 Kiosk 계측시험:
  14개, 실패 0

### RC05 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc05-release.apk`
  - 크기: 34,941,244 bytes
  - SHA-256:
    `345CE31E094860D9A7D104955DC86D7FD4126A616EB5F370E972563A7B35D326`
- 같은 파이프라인에서 재빌드한 Web POC RC04 보관본:
  `artifacts/matholic-webpoc-0.4.0-rc04-release.apk`
  - 크기: 3,080,480 bytes
  - SHA-256:
    `759B7E1B2A7373F24BCA6363F133F13AF297ED37ABC3018CFF10E7428F9EE694`
  - 같은 소스·버전·release signer지만 A 설치 Web APK의 바이트 해시와는
    다르며, Web은 변경이 없어 A에 다시 설치하지 않음
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc04`/code 9,
  Web POC `0.4.0-rc04`/code 21
- 정확한 serial·SM-P610 모델, ADB `device`, 배터리 100%·USB 전원,
  화면 `Dozing`, 설치 버전·signer·Device Owner·HOME·Lock Task를 확인
- Kiosk만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc05`/code 10,
  Web POC `0.4.0-rc04`/code 21
- Kiosk package UID `10288`, firstInstallTime와 dataDir: 설치 전후 동일
- 설치된 Kiosk base APK와 RC05 보관본 SHA-256·release signer: 일치
- Web POC UID·firstInstallTime·dataDir와 설치 버전: 변경 없음
- Device Owner와 전용 HOME: 유지
- 설치 직후 Kiosk 프로세스가 종료돼 Lock Task가 일시 `NONE`이었으나,
  화면을 깨우지 않는 명시적 HOME 시작으로 프로세스와 `LOCKED` 복구
- 설치 전후 화면: `Dozing` 유지
- 최근 Matholic 관련 치명적 AndroidRuntime 예외: 없음

### 미검증

- 관리자 PIN 입력 뒤 RC05 관리자 화면의 확인 문구·버튼 활성 조건
- 빠른 반 전환, 학생 변경·QR 재발급 연속 탭과 중복 반 이름 실기
- 기존 A 데이터의 중복 활성 반 이름 존재 여부
- 실제 수업 시작·종료와 Web 안전정리 회귀
- RC04에서 이어진 실제 사이트·카메라·PDF·직접 인쇄 실기
- 현재 물리 화면 상태와 정상 `QR_READY` 복귀

---

## RC05 이후 Web 결과 상태 전이 보강 — 2026-07-25

### 확인한 문제

Kiosk가 Web POC를 실행한 뒤 프로세스가 재시작되면 저장소는 민감 상태를
`LOCKED`로 바꾼다. 기존 결과 처리에는 기대 이전 상태 검사가 없어, 재시작
전에 시작한 Web POC의 성공 결과가 늦게 도착하면 `LOCKED`를 다시
`QR_READY`로 바꿀 수 있었다. 이미 처리한 결과가 중복 도착하는 경우도
같은 문제가 있었다.

### 변경

- 커밋: `4526072`
- Web 시작은 활성 수업의 현재 상태가 `QR_READY`일 때만
  `PRELOGIN_CHECK`로 전이한다.
- Web 정상·실패 결과와 credential bridge 실패는 활성 수업의 현재 상태가
  `PRELOGIN_CHECK`일 때만 반영한다.
- 활성 수업 존재와 기대 이전 상태 검사를 Room 트랜잭션 안에서 수행해
  검사와 저장 사이의 상태 변경을 허용하지 않는다.

### 검증과 배포 상태

- 수정 전 신규 계측 회귀시험 1개 실패로 잠금 우회 상태 전이를 재현
- 수정 뒤 같은 단일 시험 1개: 통과
- Android 13 AOSP ATD 일회용 에뮬레이터 Kiosk 전체 계측시험:
  15개, 실패 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: 총 57개, 실패 0
- 네 모듈 debug lint와 debug APK assemble: 성공
- 이 보강은 아래 RC06에 포함해 A에 설치했다. 재시작 중 늦은 Web 결과 실기는
  사용자 복귀 뒤 수행한다.

---

## RC05 이후 보강 학생 배치 원자성 — 2026-07-25

### 확인한 문제

현재 수업에 보강 학생 여러 명을 추가할 때 UI가 학생별 저장 함수를 반복
호출했다. 앞 학생 저장 뒤 뒤 학생이 비활성·누락 등으로 실패하면 앞 학생만
수업에 남지만 UI는 전체 요청 실패로 표시할 수 있었다.

### 변경

- 커밋: `6da40a0`
- UI는 선택한 학생 집합을 저장소에 한 번만 전달한다.
- 저장소는 활성 session ID와 `QR_READY` 상태, 선택 학생 전체의 활성 상태를
  Room 트랜잭션 안에서 검증한다.
- 전체 검증 뒤 모든 학생을 추가하고 배치 감사기록을 남긴다. 하나라도
  유효하지 않으면 추가와 감사기록을 모두 롤백한다.

### 검증과 배포 상태

- 수정 전 신규 계측 회귀시험 1개에서 앞 학생만 남는 부분 반영 재현
- 수정 뒤 같은 단일 시험: 통과
- Android 13 AOSP ATD 일회용 에뮬레이터 Kiosk 전체 계측시험:
  16개, 실패 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: 총 57개, 실패 0
- 네 모듈 debug lint와 debug APK assemble: 성공
- 이 보강은 아래 RC06에 포함해 A에 설치했다. 현재 수업의 복수 보강 학생
  추가 실기는 사용자 복귀 뒤 수행한다.

---

## RC06 상태 전이·보강 학생 원자성 검증본 — 2026-07-25

### 버전과 포함 변경

- Kiosk `0.6.0-rc06`/code 11
- Web POC는 소스와 A 설치본 모두 `0.4.0-rc04`/code 21 유지
- 포함 커밋:
  - `4526072`: 재시작·중복 Web 결과의 상태 덮어쓰기 차단
  - `6da40a0`: 현재 수업 보강 학생 배치 추가 원자성
  - `fa9cb15`: RC06 versionCode와 릴리스 검증본

### 자동 검증

- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: Probe 8, 잠긴 POC 1, Web POC 25, Kiosk 23;
  총 57개, 실패 0
- 네 모듈 debug lint와 debug APK assemble: 성공
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- Android 13 AOSP ATD 일회용 에뮬레이터 Kiosk 계측시험:
  16개, 실패 0

### RC06 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc06-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `522A668BBE70F87BA5B5D7677C59428D662A662D3F326EF64CA7DBD1939B0ACB`
- 같은 파이프라인에서 재빌드한 Web POC RC04 보관본:
  `artifacts/matholic-webpoc-0.4.0-rc04-release.apk`
  - 크기: 3,080,480 bytes
  - SHA-256:
    `C0E48C5CD6128B18D5D45AC8080D576F0887E57451E13B3460A6A472D81B5311`
  - A 설치 Web APK와 같은 소스·버전·signer지만 바이트 해시는 다르며,
    Web은 변경이 없어 A에 다시 설치하지 않음
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc05`/code 10,
  Web POC `0.4.0-rc04`/code 21
- 정확한 serial·SM-P610 모델, ADB `device`, 배터리 100%·USB 전원,
  화면 `Dozing`, 설치 버전·signer·Device Owner·HOME·Lock Task를 확인
- Kiosk만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc06`/code 11,
  Web POC `0.4.0-rc04`/code 21
- Kiosk package UID `10288`, firstInstallTime와 dataDir: 설치 전후 동일
- 설치된 Kiosk base APK와 RC06 보관본 SHA-256·release signer: 일치
- Web POC UID·firstInstallTime·dataDir와 설치 버전: 변경 없음
- Device Owner와 전용 HOME: 유지
- 설치 직후 Kiosk 프로세스 종료로 Lock Task가 일시 `NONE`이었으나,
  화면을 깨우지 않는 명시적 HOME 시작으로 프로세스와 `LOCKED` 복구
- 설치 전후 화면: `Dozing` 유지
- 최근 Matholic 관련 치명적 AndroidRuntime 예외: 없음

### 미검증

- 재시작 중 늦게 반환되는 Web 성공·실패 결과의 A 실기
- 현재 수업에 복수 보강 학생 추가와 후반 실패의 A 실기
- 관리자 PIN 입력 뒤 관리자 UI와 정상 `QR_READY` 복귀
- 실제 사이트·카메라·PDF·직접 인쇄 실기

---

## Web POC RC05 secure-session 호출자 경계 — 2026-07-25

### 확인한 문제와 변경

Web POC의 secure session 진입은 credential bridge URI를 읽기 전에 호출
주체를 검증하지 않았다. 비신뢰 앱이 명시적으로 진입점을 호출하는 회귀시험을
추가했을 때 수정 전에는 호출자 거부가 아니라 `CREDENTIAL_BRIDGE_EMPTY`까지
진행했다.

- 구현 커밋: `ce6458f`
- Web POC는 호출 package가 정확히 `com.local.matholickiosk.kiosk`이고
  자신의 signer와 같은 경우에만 credential bridge URI를 읽는다.
- 그 밖의 호출은 `RESULT_CANCELED`와 `SECURE_SESSION_CALLER`로 거부하고
  영속 상태를 `IDLE`로 유지한다.
- RC05 버전·릴리스 검증본 커밋: `a0c438c`
- Web POC `0.4.0-rc05`/code 22

### 자동 검증

- 수정 전 신규 비신뢰 호출 회귀시험: 실패, 기존
  `CREDENTIAL_BRIDGE_EMPTY` 경로 재현
- 수정 뒤 같은 단일 시험: 통과
- Android 13 AOSP ATD 일회용 에뮬레이터 Web POC 전체 계측시험:
  34개, 실패·오류 0, 실행 149.799초
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: 총 57개, 실패 0
- 네 모듈 debug lint와 debug APK assemble: 성공
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·권한·`debuggable=false`, APK Signature
  Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와 zipalign: 통과

### 릴리스 APK

- Web POC:
  `artifacts/matholic-webpoc-0.4.0-rc05-release.apk`
  - 크기: 3,080,528 bytes
  - SHA-256:
    `E09399F0F90A8353D2EBD396647D4436394CA739BCFD65107ABBD66E4E161A0C`
- 같은 파이프라인에서 재빌드한 Kiosk RC06 보관본:
  `artifacts/matholic-kiosk-0.6.0-rc06-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `E3F072A1550DD986245749C0768BC85DF84F7BA2D27F8D93522419D1D0E746AC`
  - A 설치 Kiosk와 같은 소스·버전·release signer지만 바이트 해시는 다르며,
    Kiosk는 변경이 없어 다시 설치하지 않음
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc06`/code 11,
  Web POC `0.4.0-rc04`/code 21
- 정확한 serial·SM-P610 모델, 유일한 ADB `device`, 배터리 100%·USB 전원,
  화면 `Dozing`, 설치 버전·signer·Device Owner·HOME·Lock Task를 확인
- Web POC만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc06`/code 11,
  Web POC `0.4.0-rc05`/code 22
- Web POC package UID `10287`, firstInstallTime
  `2026-07-24 12:52:24`, dataDir: 설치 전후 동일
- 설치된 Web POC base APK SHA-256은 RC05 보관본과 일치
- Kiosk package UID `10288`, firstInstallTime
  `2026-07-24 12:52:28`, dataDir와 설치 APK SHA-256
  `522A668BBE70F87BA5B5D7677C59428D662A662D3F326EF64CA7DBD1939B0ACB`:
  변경 없음
- 설치된 두 APK signer, Device Owner, 전용 HOME, Kiosk 프로세스와
  Lock Task `LOCKED`: 유지
- 설치 전후 화면: `Dozing` 유지
- crash buffer의 Matholic package 일치 항목: 0

### 미검증

- 실제 Kiosk가 RC05 secure session을 정상 호출하는 QR→Web→QR 왕복
- 실제 사이트의 자동 학습지 진입, 두 탭·경로 제한, 문제 입력 확대와
  오답 번호 전용 결과 회귀
- 관리자 PIN 입력 뒤 현재 물리 UI와 정상 `QR_READY`

---

## RC07/RC06 secure-session 전송 계약 복구 — 2026-07-26

### 재현한 통합 결함

Kiosk는 explicit Web Activity intent의 `data`에 보호된 credential bridge
`content://` URI를 넣었다. Android 13 일회용 에뮬레이터에서 실제 debug
Kiosk Activity가 이 intent를 실행하면 Web Activity가 설치되어 있어도
`ActivityNotFoundException`이 발생했다. recovery action은 data URI가 없어
실행됐으므로 호출자 검증 이전의 secure 전송 계약 결함으로 좁혔다.

- 수정 전 에뮬레이터 교차 앱 시험:
  `PROBE_LAUNCH_ActivityNotFoundException`
- A의 기존 RC06/RC05 조합은 사용자 부재 중 실제 QR을 촬영하지 않았으므로
  이 결함의 실기 재현 여부를 주장하지 않는다.

### 변경

- 구현 커밋: `adf8545`
- Kiosk는 1회용 handle ID만 explicit intent extra로 전달한다.
- Web POC는 handle이 정확히 32자 Base64URL 형식인지 확인한 뒤 고정된
  `com.local.matholickiosk.kiosk.credentials` authority의 URI를 내부 구성한다.
- Web manifest에 secure와 recovery action 계약을 명시했다.
- 기존 exact Kiosk package·same signer 호출자 검사는 credential handle을
  읽기 전에 그대로 적용한다.
- release에는 포함되지 않는 debug probe와 QEMU 전용 스크립트로 실제
  Kiosk Activity→Web Activity 결과 반환을 검증한다. 스크립트는
  `emulator-*` serial과 `ro.kernel.qemu=1`을 모두 요구한다.
- 버전·릴리스 커밋: `f0fe25a`
- Kiosk `0.6.0-rc07`/code 12, Web POC `0.4.0-rc06`/code 23

### 자동 검증

- handle 형식 신규 JVM 시험: 통과
- debug Kiosk→Web secure 호출: 호출자 검사를 통과해
  `CREDENTIAL_BRIDGE_EMPTY` 반환
- 이어진 trusted Web recovery: `RESULT_OK`
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- JVM 단위시험: Probe 8, 잠긴 POC 1, Web POC 26, Kiosk 23;
  총 58개, 실패·오류 0
- Android 13 AOSP ATD 일회용 에뮬레이터:
  - Web POC 전체 34개, 실패·오류·건너뜀 0, 152.422초
  - Kiosk 전체 16개, 실패·오류·건너뜀 0, 18.496초
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- release Web manifest의 두 action 계약과 release Kiosk manifest에서 debug
  probe Activity 제외: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc07-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `7B423D6AFC1ECB7F53805259F218DBDB0DC689E822F83E4637273BA76B8451E4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc06-release.apk`
  - 크기: 3,081,400 bytes
  - SHA-256:
    `ECFA865715427B32D5308B92135A75D8652811AFB3813C63FE47D7B6AED55544`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc06`/code 11,
  Web POC `0.4.0-rc05`/code 22
- 정확한 serial·SM-P610 모델, 유일한 ADB `device`, 배터리 100%·USB 전원,
  화면 `Dozing`, 설치 해시·signer·Device Owner·HOME·Lock Task를 확인
- Web POC RC06, Kiosk RC07 순서로 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc07`/code 12,
  Web POC `0.4.0-rc06`/code 23
- Kiosk UID `10288`, firstInstallTime `2026-07-24 12:52:28`, dataDir:
  설치 전후 동일
- Web POC UID `10287`, firstInstallTime `2026-07-24 12:52:24`, dataDir:
  설치 전후 동일
- 설치된 두 base APK SHA-256과 signer는 보관본과 일치
- Device Owner와 전용 HOME 유지
- Kiosk 설치 직후 프로세스 교체로 Lock Task가 일시 `NONE`이었으나 화면을
  깨우지 않는 명시적 HOME 시작으로 Kiosk 프로세스와 `LOCKED` 복구
- 설치 전후 화면 `Dozing`, 배터리 100%·USB 전원 유지
- 설치된 Web manifest에서 secure/recovery action 해석: 둘 다 MainActivity
- crash buffer의 Matholic package 일치 항목: 0

### 미검증

- 실제 종이 QR을 사용하는 RC07→RC06 secure session과 QR→Web→QR 왕복
- 실제 사이트 자동 학습지 진입, 두 탭·경로 제한, 문제 입력 확대와 오답 번호
  전용 결과
- 관리자 PIN 입력 뒤 현재 물리 UI와 정상 `QR_READY`

---

## RC08 QR PDF 공유 권한·artifact 보존 — 2026-07-26

### 확인한 계약 공백

Kiosk의 PDF 공유 intent는 FileProvider URI를 `EXTRA_STREAM`에만 넣고
`FLAG_GRANT_READ_URI_PERMISSION`을 설정했다. Android의 URI 권한 플래그는
intent의 `data`와 `ClipData` URI에 적용되므로 일부 chooser·수신 앱에서는
`EXTRA_STREAM` URI 읽기 권한 전달을 보장하기 어려웠다. 실제 A 전송 실패로
재현한 것은 아니며 정적 계약과 합성 계측에서 확인한 호환성 공백이다.

### 변경

- 구현·회귀시험 커밋: `f0d3e1a`
- Kiosk `0.6.0-rc08`/code 13 준비 커밋: `2c98eb5`
- `QrPdfShareIntentFactory`가 같은 URI를 `EXTRA_STREAM`과 단일 `ClipData`
  item에 넣고 읽기 권한만 부여한다.
- subject의 학생 전체 이름과 `application/pdf` MIME 계약은 유지한다.

### 자동 검증

- Kiosk Kotlin·AndroidTest 컴파일, debug 단위시험·lint: 통과
- Android 13 일회용 에뮬레이터:
  - PDF intent·chooser 권한을 포함한 대상 시험 3개 통과
  - Kiosk 전체 계측 17개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과

### 같은 버전 artifact 덮어쓰기 방지

첫 RC08 release 빌드에서 변경하지 않은 Web RC06의 새 APK SHA-256이
`5531E29694338E9E97D956E02283F04B71E7CADDFA09771BC0F2A0B6B15F3F29`로
달라져 기존 보관본을 같은 파일명으로 덮어썼다. A에서 설치 Web APK를
읽기 전용으로 회수해 기존 검증 SHA-256
`ECFA865715427B32D5308B92135A75D8652811AFB3813C63FE47D7B6AED55544`를
복원했다.

- 두 Web APK의 ZIP 항목 이름과 timestamp: 전부 동일
- 내용이 다른 항목:
  `META-INF/version-control-info.textproto` 1개
- 실제 Web payload가 아니라 빌드 시점 Git 커밋 메타데이터 차이로 판정
- 릴리스 스크립트 보강 커밋: `a1c8159`
- 동일 버전 파일이 이미 있으면 위 Git 메타데이터를 제외한 모든 ZIP entry
  이름·길이·SHA-256을 비교한다.
- payload가 같으면 기존 검증 artifact를 보존하고, 다르면 버전 상향을
  요구하며 실패한다.
- 보관 artifact 쌍을 다시 독립 검증한 뒤에만 checksum 파일을 쓴다.
- 보강된 clean release 파이프라인 158 tasks와 build/stored APK 이중 검증:
  통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc08-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `509919229E1230E6E7F28BEED46362D8EF67502A3ACF01E161F7DA4152B0E998`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc06-release.apk`
  - 크기: 3,081,400 bytes
  - SHA-256:
    `ECFA865715427B32D5308B92135A75D8652811AFB3813C63FE47D7B6AED55544`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc07`/code 12,
  Web POC `0.4.0-rc06`/code 23
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, 설치 해시·UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·Lock Task를 확인
- Kiosk RC08만 `adb install -r`: 성공
- 설치 직후 프로세스 교체로 Lock Task가 일시 `NONE`이었으나 화면을 깨우지
  않는 명시적 HOME 시작으로 Kiosk pid와 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc06`/code 23
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 두 base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- crash buffer의 Matholic package 일치 항목: 0

### 미검증

- A에서 실제 Quick Share 대상 선택, 수신 PC PDF 열기와 실제 인쇄
- 실제 종이 QR을 사용하는 RC08→RC06 session과 QR→Web→QR 왕복
- 관리자 PIN 입력 뒤 현재 물리 UI와 정상 `QR_READY`

---

## Web RC07 fragment SPA 경로 차단 — 2026-07-26

### 재현한 정책 결함

기존 학생 URL 정책은 scheme·host·port와 path를 엄격히 확인했지만 fragment를
검사하지 않았다. 따라서 `/workbook#/course`처럼 허용 path를 유지한 채
fragment만으로 다른 SPA 화면을 선택하는 URL을 허용하고 `pathOf`도
`/workbook`으로 반환했다. 같은 문서 안의 hash 전환은 새 main-frame load를
발생시키지 않을 수 있어 WebViewClient만으로 차단을 보장할 수 없다.

- 신규 JVM 회귀시험을 먼저 추가
- 수정 전 WebSecurityPolicyTest 5개 중 신규 1개 실패
- 정상 `/workbook?tab=assigned&id=1` query 경로는 계속 허용

### 변경

- 구현·회귀시험 커밋: `86d6cc5`
- Web POC `0.4.0-rc07`/code 24 준비 커밋: `59c9520`
- `StudentWebPolicy.safePath`는 raw fragment가 존재하면 null을 반환한다.
- DOM 학생 화면 보조는 현재 URL에 `#`가 있으면 적용하지 않는다.
- DOM 링크 클릭 가드는 허용 path라도 target URL에 fragment가 있으면
  기본 동작과 후속 handler를 차단한다.
- 오답 번호 추출도 fragment가 있는 결과 화면에서는 실패폐쇄한다.

### 자동 검증

- 수정 뒤 WebSecurityPolicyTest 5개: 통과
- 네 모듈 JVM 단위시험: 총 59개, 실패·오류 0
- Web debug lint·AndroidTest 컴파일: 통과
- Android 13 일회용 에뮬레이터:
  - Web DOM 대상 시험 18개, 실패·오류·건너뜀 0
  - Web 전체 계측 37개, 실패·오류·건너뜀 0, 159.4초
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc08-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,957,624 bytes
  - SHA-256:
    `509919229E1230E6E7F28BEED46362D8EF67502A3ACF01E161F7DA4152B0E998`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc07-release.apk`
  - 크기: 3,081,628 bytes
  - SHA-256:
    `1B7995D52FBF969757BE0E29258A38862CB1ADB449B8939601AD5ACFF3B3731A`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc06`/code 23
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, 설치 해시·UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·Lock Task를 확인
- Web POC RC07만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc07`/code 24
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 두 base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- crash buffer의 Matholic package 일치 항목: 0

### 미검증

- 실제 사이트에서 정상 query 경로 사용과 fragment 링크 차단
- 실제 종이 QR을 사용하는 RC08→RC07 session과 QR→Web→QR 왕복
- 관리자 PIN 입력 뒤 현재 물리 UI와 정상 `QR_READY`

---

## Web RC08 로그인 form endpoint 검증 — 2026-07-26

### 재현한 자격정보 전송 경계 결함

로그인 DOM sanitizer와 실제 submit은 form action의 protocol·hostname·path만
검사했다. 그 결과 `https://auth.matholic.com:444/token/signin`,
userinfo가 있는 URL, query 또는 fragment가 붙은 URL도 인증 endpoint로
인정해 자격정보를 입력할 수 있었다.

- 신규 Web DOM 계측시험으로 변형 endpoint 거부와 자격정보 미입력을 먼저 추가
- 수정 전 대상 시험 20개 중 신규 2개 실패
- 실제 네트워크 전송이나 A의 공개 사이트에서 재현한 것은 아니며 합성 DOM
  fixture에서 경계 결함을 확인

### 변경

- 구현·회귀시험 커밋: `95c7d23`
- Web POC `0.4.0-rc08`/code 25 준비 커밋: `ab8dc62`
- sanitizer와 실제 submit은 다음 조건을 모두 만족할 때만 form을 사용한다.
  - 정확한 HTTPS와 인증 hostname
  - 기본 port와 빈 URL username·password
  - 정확한 `/token/signin`
  - 빈 query와 fragment
- 현재 로그인 문서도 정확한 HTTPS 기본 origin과 빈 URL
  username·password를 요구한다.
- DOM 계약 버전은 `web-2026-07-26.1`로 갱신했다.

### 자동 검증

- 수정 뒤 Web DOM 대상 계측 20개, 실패·오류·건너뜀 0
- Android 13 일회용 에뮬레이터 Web 전체 계측 39개,
  실패·오류·건너뜀 0, 170.2초
- 네 모듈 JVM 단위시험 총 59개, 실패·오류 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc08-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,957,624 bytes
  - SHA-256:
    `509919229E1230E6E7F28BEED46362D8EF67502A3ACF01E161F7DA4152B0E998`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc08-release.apk`
  - 크기: 3,082,472 bytes
  - SHA-256:
    `AEFE475F1B9AED658FDE6F2DDC32D4CC1DDAEC5DE83E34DACA48C8384BC8E3C1`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc07`/code 24
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, 설치 해시·UID·firstInstallTime·dataDir, 기존·신규
  release signer, Device Owner·전용 HOME·Lock Task를 확인
- Web POC RC08만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc08`/code 25
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 두 base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- crash buffer의 Matholic package 일치 항목: 0

### 미검증

- 실제 공개 사이트의 로그인 form action이 새 계약과 일치하는지
- 실제 종이 QR을 사용하는 RC08→RC08 session과 QR→Web→QR 왕복
- 관리자 PIN 입력 뒤 현재 물리 UI와 정상 `QR_READY`

---

## Web RC09 DOM origin 일치 보강 — 2026-07-26

### 재현한 DOM 방어 불일치

네이티브 학생 URL 정책은 비표준 port와 userinfo를 이미 거부했지만, 포털
지문·로그아웃 동작, 학생 UI·결과 요약과 링크 가드는 protocol·hostname
중심으로 검사했다. 네이티브 main-frame 콜백에만 의존하지 않고 DOM 보조
자체도 실패폐쇄해야 하므로 합성 fixture로 독립 경계를 확인했다.

- 신규 Web DOM 계측 4개를 먼저 추가
- 수정 전 대상 시험 24개 중 신규 4개 실패
- 허용된 fixture:
  - 비표준 port의 포털 current 문서
  - userinfo가 포함된 포털 의미 링크
  - 비표준 port의 학생 current 문서와 결과 문서
  - userinfo 또는 비표준 port가 포함된 학생 링크
- 실제 A나 공개 사이트에서 변형 URL을 탐색한 것은 아니다.

### 변경

- 구현·회귀시험 커밋: `e6919e8`
- Web POC `0.4.0-rc09`/code 26 준비 커밋: `6b5c3ee`
- 포털 지문·계정 메뉴·로그아웃은 기본 HTTPS `im.matholic.com` origin,
  빈 userinfo·fragment인 current 문서와 의미 링크만 사용한다.
- 학생 UI·결과 요약은 같은 exact origin의 current 문서에서만 적용한다.
- 학생 링크 가드는 비표준 port, userinfo와 fragment를 동기적으로 차단한다.
- DOM 계약 버전은 `web-2026-07-26.2`로 갱신했다.

### 자동 검증

- 수정 뒤 Web DOM 대상 계측 24개, 실패·오류·건너뜀 0
- Android 13 일회용 에뮬레이터 Web 전체 계측 43개,
  실패·오류·건너뜀 0, 177.9초
- 네 모듈 JVM 단위시험 총 59개, 실패·오류 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- 첫 release 빌드 158 tasks는 성공했지만 후단 검증기의 Web RC08 기대값이
  RC09를 거부해 artifact 게시 전 실패
- 릴리스 운영 설정 커밋 `f8fc9e3`에서 build artifact명, 독립 검증 기본값과
  초기 provisioning 기본 APK 경로를 RC09 현재 묶음으로 갱신
- 재실행한 release 단위시험·lint·두 APK assemble:
  `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc08-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,957,624 bytes
  - SHA-256:
    `509919229E1230E6E7F28BEED46362D8EF67502A3ACF01E161F7DA4152B0E998`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc09-release.apk`
  - 크기: 3,083,756 bytes
  - SHA-256:
    `EE3349ED05D371FBF172A9221D6A0CE14F1DC5C6E642E79C987CD0EB415DEBDD`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc08`/code 25
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, 설치 해시·UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·Lock Task를 확인
- Web POC RC09만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc09`/code 26
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 두 base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- crash buffer의 Matholic package 일치 항목: 0

### 미검증

- 실제 공개 사이트의 포털·학생 문서와 링크가 exact origin 계약과 일치하는지
- 실제 종이 QR을 사용하는 RC08→RC09 session과 QR→Web→QR 왕복
- 관리자 PIN 입력 뒤 현재 물리 UI와 정상 `QR_READY`

---

## Web RC10 포털 `/course` 경로 제한 — 2026-07-26

### 재현한 포털 판정 과허용

`WebSecurityPolicy.isPortalUrl`은 허용 origin을 검사한 뒤
`im.matholic.com` host만 비교해 `/userInfo`, `/workbook` 등 모든 경로를
포털로 분류했다. 포털 DOM 지문·계정 메뉴·로그아웃도 exact origin만 확인해
공통 계정 메뉴가 있는 비-course 문서에서 통과할 수 있었다.

- 신규 JVM 정책시험과 Web DOM 계측시험을 먼저 추가
- 수정 전 JVM 정책 6개 중 신규 1개 실패
- 수정 전 Web DOM 대상 25개 중 신규 1개 실패
- 실제 A나 공개 사이트에서 비-course 탐색을 주입한 것은 아니다.

### 변경

- 구현·회귀시험 커밋: `02550ca`
- Web POC `0.4.0-rc10`/code 27과 릴리스 운영 경로 준비 커밋: `2d8482f`
- `isPortalUrl`은 exact origin에 더해 raw path가 `/course`이고 fragment가
  없을 때만 true다. query는 허용한다.
- 학생 문서 처리와 로그인 전 잔여 세션 탐지는 별도 learning-host 판정을
  사용해 기존 복구·ACTIVE 흐름을 유지한다.
- 로그인 확인 중 비-course 문서는 `PORTAL_ROUTE` 유지보수 상태로
  실패폐쇄한다.
- 로그아웃 이동 중 비-course 문서는 기존 제한 재시도·잠금 정책으로 넘긴다.
- 포털 지문·계정 메뉴·로그아웃 DOM도 current pathname `/course`를 요구한다.
- DOM 계약 버전은 `web-2026-07-26.3`으로 갱신했다.

### 자동 검증

- 수정 뒤 WebSecurityPolicyTest 6개, 실패·오류 0
- 수정 뒤 Web DOM 대상 계측 25개, 실패·오류·건너뜀 0
- Android 13 일회용 에뮬레이터 Web 전체 계측 44개,
  실패·오류·건너뜀 0, 168.4초
- 네 모듈 JVM 단위시험 총 60개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc08-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,957,624 bytes
  - SHA-256:
    `509919229E1230E6E7F28BEED46362D8EF67502A3ACF01E161F7DA4152B0E998`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc10-release.apk`
  - 크기: 3,084,104 bytes
  - SHA-256:
    `5D5503D4EE5D63B1EB872C27B9A12516177C7A56C9ED502BD8F3A66A7D723814`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc09`/code 26
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, 설치 해시·UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·Lock Task를 확인
- Web POC RC10만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc10`/code 27
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 두 base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- crash buffer의 Matholic package 일치 항목: 0

### 미검증

- 실제 공개 사이트의 로그인 성공 후 URL이 `/course`이며 query만 사용하는지
- 실제 종이 QR을 사용하는 RC08→RC10 session과 QR→Web→QR 왕복
- 관리자 PIN 입력 뒤 현재 물리 UI와 정상 `QR_READY`

---

## Web RC11 로그인 `/` 문서 경로 제한 — 2026-07-26

### 재현한 로그인 판정 과허용

`WebSecurityPolicy.isLoginUrl`과 로그인 DOM은 허용 origin을 확인한 뒤
`login.matholic.com` host만 비교했다. 같은 host의 다른 path가 같은 로그인
form을 노출하면 잔존값 정리와 새 자격정보 입력 대상이 될 수 있었다.

- 공개 `https://login.matholic.com/not-a-login-document`가 자격정보 없이
  HTTP 200으로 응답함을 확인
- 신규 JVM 정책시험과 Web DOM 계측시험을 먼저 추가
- 수정 전 JVM 정책 7개 중 신규 1개 실패
- 실제 A나 공개 사이트에 자격정보를 입력해 변형 path를 시험하지는 않았다.

### 변경

- 구현·회귀시험 커밋: `e67b655`
- Web POC `0.4.0-rc11`/code 28과 릴리스 운영 경로 준비 커밋: `e1599f8`
- native 로그인 판정과 DOM sanitizer·submit은 정확한 기본 HTTPS
  `login.matholic.com` origin, 루트 path `/`, fragment 없음 조건을 요구한다.
- 정상 로그아웃 복귀 호환성을 위해 query는 허용한다.
- 비루트 문서에서는 잔존 입력값을 지우거나 새 아이디·비밀번호를 입력하지
  않는다.
- DOM 계약 버전은 `web-2026-07-26.4`로 갱신했다.

### 자동 검증

- 수정 뒤 WebSecurityPolicyTest 7개, 실패·오류 0
- 신규 비루트 로그인 DOM 계측 1개, 실패·오류·건너뜀 0
- Android 13 일회용 에뮬레이터 Web 전체 계측 45개,
  실패·오류·건너뜀 0, 173.1초
- 네 모듈 JVM 단위시험 총 61개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- 첫 release 단위시험·lint·두 APK assemble 158 tasks와 APK 검증은
  통과했으나 artifact 게시 전 PowerShell hex 변환 호환성 오류로 전체
  스크립트는 실패
- Windows PowerShell 호환성 수정 커밋: `37deb21`
  - `Convert.ToHexString` 대신 `BitConverter` 기반 대문자 hex 사용
  - PowerShell AST 구문 분석, 합성 byte 변환과 실제 파일 SHA-256 비교 통과
- 수정 뒤 release 단위시험·lint·두 APK assemble:
  `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc08-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,957,624 bytes
  - SHA-256:
    `509919229E1230E6E7F28BEED46362D8EF67502A3ACF01E161F7DA4152B0E998`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc11-release.apk`
  - 크기: 3,084,304 bytes
  - SHA-256:
    `2376DE8B4D68FCC9F40A9D3E2D0CC1D66743A3347ABB2731AB6769BEDC0CA37B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc10`/code 27
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, 설치 해시·UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·Lock Task를 확인
- Web POC RC11만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc11`/code 28
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Web base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- crash buffer의 Matholic package 일치 항목: 0

### 미검증

- 실제 공개 사이트의 로그인 문서가 루트 `/`와 허용 query 계약을 유지하는지
- 실제 종이 QR을 사용하는 RC08→RC11 session과 QR→Web→QR 왕복
- 관리자 PIN 입력 뒤 현재 물리 UI와 정상 `QR_READY`

---

## Kiosk RC09 QR 카드 PDF 실물 좌표계 수정 — 2026-07-26

### 재현한 과대 출력

Android 공식 `PrintedPdfDocument` 구현은 page와 content rectangle을
1인치당 72 PostScript point로 만든다. 기존 renderer는 이 좌표를
`PrintAttributes.Resolution`의 300·600 DPI pixel로 해석해 카드와 QR을
확대한 뒤 A4 content rectangle에 맞춰 다시 축소했다.

- 공식 근거:
  [Android PrintedPdfDocument source](https://android.googlesource.com/platform/frameworks/base/+/c80f952/core/java/android/print/pdf/PrintedPdfDocument.java#70)
- 실제 생성 PDF 외곽선을 재는 계측시험과 point 계산시험을 먼저 보강
- 수정 전 PDF 대상 계측 3개 중 2개 실패
  - 계산 카드 폭: 65mm 기대, 541.7mm
  - 생성 PDF 외곽선 폭: 65mm 기대, 약 172.5mm
- 실제 프린터 작업이나 실제 QR을 생성하지 않고 합성 QR만 사용했다.

### 변경

- 구현·회귀시험 커밋: `c5acc5a`
- Kiosk `0.6.0-rc09`/code 14와 릴리스 운영 경로 준비 커밋: `561baff`
- 카드 65×90mm, QR 30×30mm, 여백·글자·테두리를 모두
  72 PostScript point/inch로 변환한다.
- 프린터의 수평·수직 DPI는 PDF 물리 좌표에 사용하지 않는다.
- 인쇄 가능 영역이 카드보다 작으면 크기를 임의 축소하지 않고
  `65×90mm`가 포함된 오류로 실패한다.

### 자동 검증

- 수정 뒤 PDF/공유 대상 계측 4개, 실패·오류·건너뜀 0
  - 65×90mm 카드와 30×30mm QR point 계산
  - 600×300 비대칭 printer resolution으로 생성한 PDF 외곽선 65×90mm
  - 작은 printable area의 무단 축소 거부
  - PDF 1쪽·비트맵 폐기·공유 URI 권한
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 18개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 61개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc09-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `68DB8304B87528DDE006B86A27E9B282899069D3F3F74165718455F2640A967D`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc11-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,084,304 bytes
  - SHA-256:
    `2376DE8B4D68FCC9F40A9D3E2D0CC1D66743A3347ABB2731AB6769BEDC0CA37B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc08`/code 13,
  Web POC `0.4.0-rc11`/code 28
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, 설치 해시·UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·Lock Task를 확인
- Kiosk RC09만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc09`/code 14,
  Web POC `0.4.0-rc11`/code 28
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- crash buffer의 Matholic package 일치 항목: 0

### 미검증

- 실제 A의 PDF 공유·PC 열기와 인쇄물 자 측정
- 실제 종이 QR의 30×30mm 인식과 QR→Web→QR 왕복
- 관리자 PIN 입력 뒤 현재 물리 UI와 정상 `QR_READY`

---

## Kiosk RC10 오래된 QR 분석 결과 차단 — 2026-07-26

### 재현한 비동기 경계

관리자 PIN 대화상자를 열면 카메라 분석기는 중지되지만, 그 직전에 ML Kit에
넘긴 프레임은 계속 처리되어 결과가 늦게 도착할 수 있었다. 기존 UI 전달부는
scanner 화면이 보이는지만 확인했고 PIN 대화상자가 열린 동안에도 scanner
화면 자체는 남아 있어, 승인 결과가 Web 로그인을 시작할 가능성이 있었다.

### 변경

- 구현·회귀시험 커밋: `32e41b9`
- Kiosk `0.6.0-rc10`/code 15와 릴리스 운영 경로 준비 커밋: `4e9fe30`
- `QrDecisionDeliveryGate`가 각 처리 프레임에 분석 세대를 부여한다.
- 분석 중지와 재개는 세대를 바꾸므로 이미 처리 중이던 이전 프레임 결과도
  전달되지 않는다.
- 폐기하는 승인 결과의 QR token hash를 즉시 덮어쓴다.
- `MainActivity`도 UI 전달 직전에 분석 활성·scanner 화면·Activity
  생명주기를 다시 확인하고 조건이 바뀌었으면 민감 결과를 폐기한다.

### 자동 검증

- 신규 JVM 회귀시험 3개: 실패·오류 0
  - 현재 세대 결과 전달
  - 중지·재개 전 프레임 폐기와 hash 덮어쓰기
  - 분석 중지 중 새 프레임 세대 발급 거부
- 네 모듈 JVM 단위시험 총 64개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 18개,
  실패·오류·건너뜀 0
- release Kiosk/Web JVM 보고서 55개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc10-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `7BFF17A3D74C9DB2C699BE4EE4F3AEE3175F4A39C72B5032D590EAE4C7870187`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc11-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,084,304 bytes
  - SHA-256:
    `2376DE8B4D68FCC9F40A9D3E2D0CC1D66743A3347ABB2731AB6769BEDC0CA37B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc09`/code 14,
  Web POC `0.4.0-rc11`/code 28
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, 설치 해시·UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·Lock Task를 확인
- Kiosk RC10만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc10`/code 15,
  Web POC `0.4.0-rc11`/code 28
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- A에서 QR 해석과 관리자 PIN 화면 진입을 의도적으로 겹치는 실기
- 실제 QR→Web→QR 왕복, 관리자 화면과 정상 `QR_READY`
- 실제 PDF 공유·인쇄물 크기·종이 QR 재인식

---

## Web RC12 renderer 종료 복구 — 2026-07-26

### 재현한 계약 위반

Android는 renderer process가 종료된 WebView를 다시 사용할 수 없으므로
뷰 계층에서 제거하고 파기한 뒤 Activity의 참조도 해제하도록 요구한다.
기존 `onRenderProcessGone`은 `WEB_PROCESS_GONE` 잠금만 표시하고 죽은
WebView를 계층과 필드에 남긴 채 `true`를 반환했다.

- 공식 근거:
  [Android WebView termination handling](https://developer.android.com/develop/ui/views/layout/webapps/managing-webview)
- Android 13 일회용 에뮬레이터에서 공식 시험용 `chrome://crash`를 사용
- 수정 전 신규 계측시험 실패:
  상태와 사유는 `LOCKED`·`WEB_PROCESS_GONE`이었지만 `web_view`가 계층에
  그대로 남음
- 실제 A에는 renderer 충돌을 주입하지 않았다.

### 변경

- 구현·회귀시험 커밋: `4d54dee`
- Web POC `0.4.0-rc12`/code 29와 릴리스 운영 경로 준비 커밋: `0695240`
- renderer 종료 콜백의 WebView를 부모 계층에서 제거하고 `destroy()`한 뒤
  Activity 참조를 `null`로 해제한다.
- 죽은 WebView가 없는 상태에서도 잠금 UI와 Kiosk 실패 반환을 유지한다.
- 잠금 화면의 복구 버튼으로 Activity를 재생성하면 새 WebView를 구성한다.
- 일반 Activity 종료 시에도 활성 WebView 참조를 먼저 해제하고 정리해
  renderer 종료 경로와 중복 사용하지 않는다.

### 자동 검증

- 수정 뒤 renderer 충돌·계층 제거·새 WebView 복구 대상 계측 1개 통과
- Android 13 일회용 에뮬레이터 Web 전체 계측 46개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 64개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 55개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc10-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,957,624 bytes
  - SHA-256:
    `7BFF17A3D74C9DB2C699BE4EE4F3AEE3175F4A39C72B5032D590EAE4C7870187`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc12-release.apk`
  - 크기: 3,084,508 bytes
  - SHA-256:
    `469493E02F1554279F3C6F7ACFBA0A149568225524013A55FA5CA20CDC45C880`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc10`/code 15,
  Web POC `0.4.0-rc11`/code 28
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, 설치 해시·UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·Lock Task를 확인
- Web POC RC12만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc10`/code 15,
  Web POC `0.4.0-rc12`/code 29
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Web base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- A의 고의 Web renderer 충돌과 그 뒤 관리자 복구
- 실제 QR→Web→QR 왕복과 공개 사이트 DOM 계약
- 관리자 화면·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC11 준비 완료 Web 세션 경합 차단 — 2026-07-26

### 재현한 비동기 경계

QR 검증 뒤 Kiosk는 세션을 `PRELOGIN_CHECK`로 바꾸고 암호화 자격정보를
복호화해 1회용 handle을 준비한다. 이 작업 중 학생 화면의 교사 관리 버튼으로
전환하면 scanner는 닫히지만, 기존 준비 완료 콜백은 Activity 파기 여부만
검사했다. 따라서 PIN 화면이 열린 뒤에도 준비된 Web 세션을 시작할 수 있었다.

- 현재 동작을 결정 정책으로 추출하고 회귀시험을 먼저 추가
- 수정 전 정책 JVM 3개 중 scanner 이탈 취소 시험 1개 실패:
  기대 `CANCEL_AND_RESTORE`, 실제 `LAUNCH`
- 실제 QR·자격정보·PIN은 사용하지 않았다.

### 변경

- 구현·회귀시험 커밋: `f613467`
- Kiosk `0.6.0-rc11`/code 16과 릴리스 운영 경로 준비 커밋: `a82f606`
- 준비 완료 시 Activity가 파기됐으면 handle만 폐기하고 기존 재시작
  실패폐쇄 정책에 맡긴다.
- Activity는 살아 있지만 scanner 화면을 떠났으면 handle을 폐기한다.
- 이 경우 현재 DB 상태가 정확히 `PRELOGIN_CHECK`일 때만
  `PRELOGIN_CHECK→QR_READY`로 복원한다.
- 다른 Web 결과·잠금·복구 상태가 먼저 반영됐으면 기대 상태 검사가 복원을
  거부하므로 늦은 콜백이 새 상태를 덮어쓰지 않는다.

### 자동 검증

- 수정 뒤 준비 완료 정책 JVM 3개, 실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 67개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 18개,
  실패·오류·건너뜀 0
  - 기존 저장소 시험이 `PRELOGIN_CHECK→QR_READY` 복원 시 학생 ID 제거와
    중복·늦은 복원 거부를 포함
- release Kiosk/Web JVM 보고서 58개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc11-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `E7E094EA353E924E151885C068559BD61FDC7DCAA8B2A314686F982CCD9788A9`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc12-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,084,508 bytes
  - SHA-256:
    `469493E02F1554279F3C6F7ACFBA0A149568225524013A55FA5CA20CDC45C880`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc10`/code 15,
  Web POC `0.4.0-rc12`/code 29
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, 설치 해시·UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·Lock Task를 확인
- Kiosk RC11만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc11`/code 16,
  Web POC `0.4.0-rc12`/code 29
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·최종 `LOCKED`, 화면 `Dozing`, USB·100% 유지
- 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0
- 설치 스크립트의 마지막 전원 상태 출력 구문 오타로 설치 직후의 일시
  Lock Task 값은 보존되지 않았으며 최종 `LOCKED`만 독립 재확인

### 미검증

- A에서 실제 QR 승인 직후 교사 관리 버튼을 빠르게 누르는 경합
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 화면·카메라·PDF 공유·실물 인쇄 회귀

---

## Web RC13 오래된 문서 완료 콜백 차단 — 2026-07-26

### 재현한 비동기 경계

WebView는 새 최상위 문서 이동이 시작된 뒤에도 이전 문서의
`onPageFinished` 콜백을 늦게 전달할 수 있다. 기존 코드는 콜백 URL이 허용
origin인지와 현재 상태만 확인했으며, WebView가 실제로 표시 중인 최상위
URL과 일치하는지는 확인하지 않았다. 따라서 현재 포털 위의 이전 로그인
완료가 로그아웃 검증 상태를 잘못 시작하거나, 현재 학습지 위의 이전 포털
완료가 학생 화면 준비를 잘못 완료할 수 있었다.

- 현재 동작을 `WebFailurePolicy`의 완료 콜백 정책으로 분리하고 회귀시험을
  먼저 추가
- 수정 전 신규 정책 시험 실패:
  - 현재 포털 위의 이전 로그인 완료를 처리
  - 현재 학습지 위의 이전 포털 완료를 처리
- 실제 A의 사이트 이동이나 자격정보는 사용하지 않았다.

### 변경

- 구현·회귀시험 커밋: `8895d5f`
- Web POC `0.4.0-rc13`/code 30과 릴리스 운영 경로 준비 커밋: `1ae4b6c`
- `onPageFinished`의 콜백 URL과 WebView의 현재 최상위 URL이 정확히
  일치할 때만 로그인·포털 상태 전이를 처리한다.
- 이전 문서, 파기된 WebView와 현재 URL을 확인할 수 없는 콜백은 상태와
  DOM을 변경하지 않고 폐기한다.
- 기존 허용 origin·경로 검사와 preflight DNS 재시도 보호는 그대로 유지한다.

### 자동 검증

- 수정 전 신규 JVM 회귀시험 1개 실패, 수정 뒤 Web JVM 30개 통과
- 네 모듈 JVM 단위시험 총 68개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- Android 13 일회용 에뮬레이터 Web 전체 계측 46개,
  실패·오류·건너뜀 0
- release Kiosk/Web JVM 보고서 59개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc11-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,957,624 bytes
  - SHA-256:
    `E7E094EA353E924E151885C068559BD61FDC7DCAA8B2A314686F982CCD9788A9`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc13-release.apk`
  - 크기: 3,084,708 bytes
  - SHA-256:
    `377C824C3900CA05F96F5C5A8C9F6FA2A85EA8AB8B94B07379F2BBA1869B4BDD`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc11`/code 16,
  Web POC `0.4.0-rc12`/code 29
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·`LOCKED`를 확인
- Web POC RC13만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc11`/code 16,
  Web POC `0.4.0-rc13`/code 30
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Web base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- A의 실제 빠른 로그인·포털·학습지 전환 경합
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 화면·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC12 QR PDF 지연 삭제 수명주기 — 2026-07-26

### 확인한 정리 누락 경로

QR PDF 공유 화면에서 Kiosk로 복귀하면 기존 코드는 `pendingSharedPdf`를
비운 뒤 30초 삭제를 `MainActivity.mainHandler`에 예약했다. Activity가
그 30초 안에 파기되면 `onDestroy`의 `removeCallbacksAndMessages(null)`가
UI 콜백과 함께 파일 삭제까지 취소했다. 파일은 앱 전용 cache에 남고 다음
실행·내보내기의 1시간 만료 정리 전까지 삭제가 보장되지 않았다.

- 실제 QR 원문이나 학생 정보 없이 합성 PDF fixture만 사용
- 수정 전 신규 계측 계약은 `scheduleSharedFileCleanup`이 없어 컴파일 실패
- 기존 수명주기와 삭제 취소 경로는 소스 정적으로 확인

### 변경

- 구현·계측시험 커밋: `acae022`
- Kiosk `0.6.0-rc12`/code 17과 릴리스 운영 경로 준비 커밋: `4ca2048`
- 공유 복귀 뒤 삭제 예약을 Activity 공용 Handler에서 `QrPdfExporter`의
  process 범위 Handler로 옮겼다.
- 지연 작업은 정규화한 export 파일만 캡처하며 Activity를 보유하지 않는다.
- 삭제 대상의 canonical parent가 앱 cache의 `qr_exports`와 정확히
  일치하지 않으면 예약을 거부한다.
- 프로세스 자체가 종료되면 기존 1시간 만료 정리가 재시작 안전망으로 남는다.

### 자동 검증

- 합성 PDF 50ms 지연 삭제와 export 디렉터리 외부 파일 거부 계측 2개 통과
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 20개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 68개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 59개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc12-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `64E28E61822D5C5A01887DA47C7ECBAEB3C9D99358D7EF52F1DC5CCD19626D71`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc13-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,084,708 bytes
  - SHA-256:
    `377C824C3900CA05F96F5C5A8C9F6FA2A85EA8AB8B94B07379F2BBA1869B4BDD`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc11`/code 16,
  Web POC `0.4.0-rc13`/code 30
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·`LOCKED`를 확인
- Kiosk RC12만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc12`/code 17,
  Web POC `0.4.0-rc13`/code 30
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- A에서 실제 PDF 공유 복귀 뒤 30초 파일 삭제
- Quick Share 수신 PC에서 PDF 열기와 실제 인쇄
- 실제 QR→Web→QR 왕복, 관리자 화면·카메라·실물 인쇄 회귀

---

## Kiosk RC13 QR bitmap 내보내기 전 실패 정리 — 2026-07-26

### 재현한 민감 메모리 정리 공백

`MainActivity.prepareQrPdfExport`는 화면의 QR bitmap을 복제한 뒤 감사기록을
저장하고 `QrPdfExporter.export`를 호출했다. 감사기록 저장이 예외로 끝나면
exporter가 복제본의 소유권을 받지 못했다. 그 사이 Activity까지 파기되면
UI 실패 콜백도 실행되지 않아 로그인 가능한 QR bitmap 복제본이 GC 전까지
명시적으로 덮어쓰기·폐기되지 않았다.

- 실제 QR 원문과 학생 정보 없이 합성 bitmap과 고의 예외만 사용
- 회귀 계측시험을 먼저 추가했고, 수정 전에는
  `consumeSensitiveBitmap` 소유권 계약이 없어 컴파일 실패
- 정적 경로와 회귀시험으로 Activity 생명주기와 무관한 누락을 확인

### 변경

- 구현·계측시험 커밋: `0fc357c`
- Kiosk `0.6.0-rc13`/code 18과 릴리스 운영 경로 준비 커밋: `9c94991`
- 감사기록부터 PDF export까지 복제 bitmap 작업 전체를
  `consumeSensitiveBitmap`의 단일 소유권 경계 안에서 실행한다.
- 작업 성공·실패 여부와 관계없이 `finally`에서 mutable bitmap을 흰색으로
  덮어쓰고 recycle한다.
- 실제 PDF exporter도 같은 `releaseSensitiveBitmap`을 사용해 정리 동작이
  서로 달라지지 않게 했다.

### 자동 검증

- 내보내기 전 고의 예외 뒤 복제 bitmap 덮어쓰기·recycle 계측 1개 통과
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 21개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 68개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 59개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc13-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `8DA89E1A4086CDAB2CF4BA2E676DD04E44053F9F1197947C35696E833E68C9CC`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc13-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,084,708 bytes
  - SHA-256:
    `377C824C3900CA05F96F5C5A8C9F6FA2A85EA8AB8B94B07379F2BBA1869B4BDD`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc12`/code 17,
  Web POC `0.4.0-rc13`/code 30
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·`LOCKED`를 확인
- Kiosk RC13만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc13`/code 18,
  Web POC `0.4.0-rc13`/code 30
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 QR PDF 내보내기와 Quick Share 전달
- 수신 PC에서 PDF 열기와 실물 인쇄
- 실제 QR→Web→QR 왕복, 관리자 화면·카메라 회귀

---

## Kiosk RC14 종료 시 대기 민감 작업 정리 — 2026-07-26

### 재현한 대기열 정리 공백

Kiosk는 카메라 분석과 DB 작업에 단일 `ioExecutor`를 사용한다. Activity가
종료될 때 `shutdownNow()`는 아직 실행을 시작하지 않은 Runnable 목록을
반환하지만 기존 코드는 이를 무시했다. 그 결과 작업 본문이나 repository의
`finally`에만 있던 정리 코드가 호출되지 않아 다음 값이 대기 Runnable에
캡처된 채 GC 전까지 남을 수 있었다.

- 관리자 인증·설정 PIN
- 학생 등록·로그인 정보 변경의 아이디와 비밀번호
- QR 검증 token hash
- PDF 내보내기용 복제 QR bitmap

회귀 JVM 시험을 먼저 추가했고 시험 import를 기존 JUnit 4 방식으로 맞춘 뒤
수정 전에는 `SensitiveTask` 계약 부재로 컴파일 실패를 확인했다.

### 변경

- 구현·회귀시험 커밋: `bfbfaf4`
- Kiosk `0.6.0-rc14`/code 19와 릴리스 운영 경로 준비 커밋: `5a99e73`
- `SensitiveTask`가 실행과 폐기 중 먼저 소유권을 얻은 한 경로만 허용하고,
  정상 완료·예외·대기열 폐기에서 정리 동작을 정확히 한 번 실행한다.
- `shutdownNow()`가 돌려준 대기 작업 중 `SensitiveTask`를 즉시 폐기한다.
- executor가 작업 제출을 거부한 경로도 민감 값을 먼저 정리한다.
- QR bitmap 정리는 기존 흰색 덮어쓰기·recycle 구현을 공통 사용한다.

### 자동 검증

- 대기 작업 폐기 후 미실행·정리 1회, 실행 중 예외 뒤 정리 1회 JVM 시험 통과
- 네 모듈 JVM 단위시험 총 70개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 21개,
  실패·오류·건너뜀 0
- release Kiosk/Web JVM 보고서 61개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc14-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `1F2D2EF7F124DCAE6F91C5A6132C8F135CDD5E1D5F11642509C4278532C6CFED`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc13-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,084,708 bytes
  - SHA-256:
    `377C824C3900CA05F96F5C5A8C9F6FA2A85EA8AB8B94B07379F2BBA1869B4BDD`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc13`/code 18,
  Web POC `0.4.0-rc13`/code 30
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·`LOCKED`를 확인
- Kiosk RC14만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc14`/code 19,
  Web POC `0.4.0-rc13`/code 30
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- 설치된 Kiosk와 RC14 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- A에서 Activity 종료와 PIN·자격정보·QR 검증·PDF 대기 작업을 겹치는
  고의 실패주입
- 실제 QR→Web→QR 왕복과 관리자 PIN·카메라·PDF·실물 인쇄 회귀

---

## Web RC14 로그아웃 재시도 콜백 세대 차단 — 2026-07-26

### 재현한 동일 상태 비동기 경합

로그아웃 첫 시도의 portal fingerprint, 계정 메뉴 열기, 로그아웃 클릭과
지연 재탐색 콜백은 `LOGOUT_NAVIGATE` 상태만 확인했다. 20초 timeout 뒤
두 번째 시도가 같은 상태로 시작되면 첫 시도의 늦은 콜백도 상태 검사를
통과해 새 문서에서 메뉴나 로그아웃을 중복 실행할 수 있었다. 첫 시도의
`LOGOUT_SUBMIT` timeout과 cookie 삭제 완료 콜백도 같은 ABA 경계를 가졌다.

- 실제 계정이나 공개 사이트 요청 없이 상태·세대 합성값만 사용
- 회귀 JVM 정책을 먼저 추가했고 수정 전
  `shouldProcessLogoutCallback` 부재로 컴파일 실패
- 동일 상태·이전 세대 거부와 동일 상태·현재 세대 허용을 함께 검증

### 변경

- 구현·회귀시험 커밋: `1b2bc92`
- Web POC `0.4.0-rc14`/code 31과 릴리스 운영 경로 준비 커밋: `ad7edbb`
- 로그아웃 시도 시작마다 증가하는 세대 토큰을 만든다.
- portal fingerprint, 계정 메뉴, 로그아웃 클릭과 재탐색, 두 timeout,
  cookie 삭제 완료가 기대 상태와 현재 세대를 모두 확인한다.
- 이전 시도의 콜백은 상태가 우연히 같아도 Web DOM·상태·timeout을 변경하지
  않고 폐기한다.

### 자동 검증

- 현재 세대 허용·이전 세대와 다른 상태 거부 JVM 정책 1개 통과
- 네 모듈 JVM 단위시험 총 71개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- Android 13 일회용 에뮬레이터 Web 전체 계측 46개,
  실패·오류·건너뜀 0
- release Kiosk/Web JVM 보고서 62개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc14-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,957,624 bytes
  - SHA-256:
    `1F2D2EF7F124DCAE6F91C5A6132C8F135CDD5E1D5F11642509C4278532C6CFED`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc14-release.apk`
  - 크기: 3,085,508 bytes
  - SHA-256:
    `476BAC1C0546EE9876F7B7985E567E596E478838A0D01327C974193335492EFD`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc14`/code 19,
  Web POC `0.4.0-rc13`/code 30
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, release signer,
  Device Owner·전용 HOME·`LOCKED`를 확인
- Web POC RC14만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc14`/code 19,
  Web POC `0.4.0-rc14`/code 31
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Web base APK SHA-256과 보관본 일치
- 설치된 Web과 RC14 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 공개 사이트에서 첫 로그아웃 timeout과 두 번째 시도의 늦은 콜백 경합
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 화면·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC15 방치된 공유 QR PDF 수명 상한 — 2026-07-26

### 재현한 공유 수명주기 누락

기존 구현은 PDF 공유 화면에서 Kiosk로 정상 복귀한 `onStart`에서만 30초
삭제를 예약했다. 공유 화면에 있는 동안 Kiosk Activity가 파기되면
`pendingSharedPdf` 참조가 사라져, 다음 생성·내보내기 때의 1시간 만료
정리까지 로그인 가능한 PDF가 앱 캐시에 남을 수 있었다.

- 실제 QR 원문 없이 합성 PDF만 사용
- 회귀 계측시험을 먼저 추가했고 수정 전 `scheduleSharedFileExpiry` 부재로
  Android test Kotlin 컴파일 실패
- 공유 시작 시 수명 상한 예약이 실제 파일을 삭제하는지 50ms 합성 지연으로
  검증

### 변경

- 구현·회귀시험 커밋: `0b9b746`
- Kiosk `0.6.0-rc15`/code 20과 릴리스 운영 경로 준비 커밋: `17992f0`
- 공유 선택기를 열기 전에 process 범위 Handler에 최대 1시간 삭제를 예약
- 공유 화면에서 정상 복귀하면 기존 30초 삭제도 추가 예약
- chooser 실행 또는 만료 예약 실패 시 기존처럼 공유를 중단하고 파일을 즉시
  삭제
- 예약 콜백은 Activity를 캡처하지 않고 `qr_exports` 직계 파일만 삭제

### 자동 검증

- 신규 방치 공유 만료 계측시험 1개 통과
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 22개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 71개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 62개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc15-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `7F56D96A6C1EF3A54B58AA07EB75829C2392BE5A1DC1F22E409C79A10D5AB92B`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc14-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,508 bytes
  - SHA-256:
    `476BAC1C0546EE9876F7B7985E567E596E478838A0D01327C974193335492EFD`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc14`/code 19,
  Web POC `0.4.0-rc14`/code 31
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시,
  release signer, Device Owner·전용 HOME·`LOCKED`를 확인
- Kiosk RC15만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc15`/code 20,
  Web POC `0.4.0-rc14`/code 31
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- 설치된 Kiosk와 RC15 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 Quick Share 중 Kiosk Activity 파기 뒤 1시간 이내 자동 삭제
- 정상 공유 복귀 뒤 30초 삭제와 수신 PC에서 PDF 열기
- 실제 QR→Web→QR 왕복, 관리자 PIN·카메라·실물 인쇄 회귀

---

## Web RC15 로그인 확인 콜백 세대 차단 — 2026-07-26

### 재현한 동일 상태 DOM 경합

로그인 뒤 같은 `/course` 문서가 연속 완료되면 `handlePortalPage`가
`LOGIN_VERIFY` 상태에서 portal fingerprint 확인을 다시 시작할 수 있다.
기존 fingerprint 평가와 지연 재시도 콜백은 상태만 확인하므로, 이전 확인
시도의 늦은 결과도 새 확인 시도와 같은 상태를 통과할 수 있었다.

- 실제 계정이나 공개 사이트 요청 없이 상태·세대 합성값만 사용
- 회귀 JVM 정책을 먼저 추가했고 수정 전
  `shouldProcessStateGenerationCallback` 부재로 컴파일 실패
- 동일 상태·현재 세대 허용, 동일 상태·이전 세대와 다른 상태 거부를 함께 검증

### 변경

- 구현·회귀시험 커밋: `9dabba0`
- Web POC `0.4.0-rc15`/code 32와 릴리스 운영 경로 준비 커밋: `5b531dc`
- 포털 로그인 확인 시작마다 기존 login probe 세대를 증가
- fingerprint 평가 시작 전과 완료 뒤, 모든 지연 재시도에서
  `LOGIN_VERIFY` 상태와 현재 세대를 함께 확인
- 이전 확인 시도의 콜백은 상태가 우연히 같아도 학생 이름·상태·재시도 횟수를
  변경하지 않고 폐기

### 자동 검증

- 현재 세대 허용·이전 세대와 다른 상태 거부 JVM 정책 1개 통과
- 네 모듈 JVM 단위시험 총 72개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- Android 13 일회용 에뮬레이터 Web 전체 계측 46개,
  실패·오류·건너뜀 0
- release Kiosk/Web JVM 보고서 63개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,957,624 bytes
  - SHA-256:
    `7F56D96A6C1EF3A54B58AA07EB75829C2392BE5A1DC1F22E409C79A10D5AB92B`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc15`/code 20,
  Web POC `0.4.0-rc14`/code 31
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시,
  release signer, Device Owner·전용 HOME·`LOCKED`를 확인
- Web POC RC15만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc15`/code 20,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Web base APK SHA-256과 보관본 일치
- 설치된 Web과 RC15 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 공개 사이트에서 같은 `/course` 연속 완료와 fingerprint 경합
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 화면·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC16 관리자 반 선택 새로고침 경합 차단 — 2026-07-26

### 재현한 관리자 선택 경합

반 생성·삭제나 Web 정리 뒤 `refreshAdminData`가 목록을 읽는 동안 교사가
다른 반을 선택하면, 기존 구현은 새로고침 시작 때 캡처한 반과 명단을 완료
콜백에서 무조건 다시 적용했다. 그 결과 방금 선택한 반이 이전 반이나 새로
생성한 반으로 되돌아갈 수 있었다.

- 실제 학생·관리자 PIN 없이 반 선택 세대와 합성 명단만 사용
- 회귀 JVM 시험을 먼저 추가했고 수정 전 `snapshotSelection`과
  `resolveRefresh` 계약 부재로 Kotlin test 컴파일 실패
- 새 선택 보존, 활성 수업 반 강제, 삭제된 새 선택의 안전한 fallback을 각각
  검증

### 변경

- 구현·회귀시험 커밋: `30ec5e3`
- Kiosk `0.6.0-rc16`/code 21과 릴리스 운영 경로 준비 커밋: `ee87b98`
- 관리자 새로고침 시작 때 반 선택 revision을 캡처
- 활성 수업이 없고 완료 시점의 더 새로운 선택 반이 여전히 존재하면 이전
  목록 결과가 그 선택과 명단 로딩 상태를 덮지 않음
- 활성 수업 반은 계속 강제하고, 새 선택 반이 삭제됐으면 새로 읽은 유효 반으로
  안전하게 fallback

### 자동 검증

- 관리자 비동기 상태 JVM 시험 9개 통과
- 네 모듈 JVM 단위시험 총 75개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 22개,
  실패·오류·건너뜀 0
- release Kiosk/Web JVM 보고서 66개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc16-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `080FE9AF7865740551B7D2FFF6A0788C9FECDC3261C5F5901CFDA8838ADA1A87`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc15`/code 20,
  Web POC `0.4.0-rc15`/code 32
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시,
  release signer, Device Owner·전용 HOME·`LOCKED`를 확인
- Kiosk RC16만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc16`/code 21,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- 설치된 Kiosk와 RC16 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 설치 시점 이후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 관리자 화면에서 목록 새로고침과 빠른 반 전환을 겹치는 실기
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC17 관리자 학생 선택 새로고침 경합 차단 — 2026-07-26

### 재현한 학생 선택 경합

Web 정리 등으로 `refreshAdminData`가 학생 목록을 다시 읽는 동안 교사가 다른
학생을 선택하면, 기존 구현은 새로고침 시작 때 캡처한 학생을 완료 콜백에서
다시 선택했다. 학생 선택에는 반 선택과 달리 별도 상태·revision 추적이 없어
방금 선택한 학생이 이전 학생으로 되돌아갈 수 있었다.

- 실제 학생·관리자 PIN 없이 학생 ID와 선택 revision 합성값만 사용
- 회귀 JVM 시험을 먼저 추가했고 수정 전 `RefreshableSelectionState` 부재로
  Kotlin test 컴파일 실패
- 새 선택 보존, 학생 등록·수정의 명시적 선호 적용, 삭제된 새 선택의 안전한
  fallback을 각각 검증

### 변경

- 구현·회귀시험 커밋: `984e3fb`
- Kiosk `0.6.0-rc17`/code 22와 릴리스 운영 경로 준비 커밋: `7f3e924`
- 학생 Spinner의 실제 선택을 ID와 revision으로 추적
- 새로고침 완료 시 더 새로운 선택 학생이 여전히 존재하면 이전 선호값이
  선택을 덮지 않음
- 학생 등록·이름/계정정보 수정은 기존 명시적 대상 학생을 계속 선택하고,
  비활성화 등으로 선택 학생이 사라지면 남은 첫 학생으로 안전하게 fallback

### 자동 검증

- 관리자 비동기 상태 JVM 시험 12개 통과
- 네 모듈 JVM 단위시험 총 78개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 22개,
  실패·오류·건너뜀 0
- release Kiosk/Web JVM 보고서 69개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc17-release.apk`
  - 크기: 34,957,624 bytes
  - SHA-256:
    `CCCAC3270E8C0D879330A7EC2EF33EA4C1B033EDD0A4BAB6145BD2522EF1B352`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc16`/code 21,
  Web POC `0.4.0-rc15`/code 32
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시,
  release signer, Device Owner·전용 HOME·`LOCKED`를 확인
- Kiosk RC17만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc17`/code 22,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- 설치된 Kiosk와 RC17 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 설치 시점 이후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 관리자 화면에서 목록 새로고침과 빠른 학생 전환을 겹치는 실기
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC18 관리자 목록 새로고침 실패 복구 — 2026-07-26

### 재현한 실패

`refreshAdminData`의 반·학생·수업·소속 목록 DB 읽기는 예외 경계 밖에서
실행됐다. 이 중 하나가 실패하면 executor 예외가 프로세스를 종료할 수 있었고,
학생 변경 자체가 저장된 뒤 후속 목록 새로고침만 실패한 경우에는
`studentMutationGate`가 해제되지 않아 관리자 UI가 영구 대기 상태로 남을
수 있었다.

- 실제 A 데이터나 관리자 PIN을 사용하지 않고 Android 13 일회용
  에뮬레이터의 합성 학생과 고의로 사용할 수 없게 만든 저장소만 사용
- 회귀 계측시험을 먼저 추가했고 수정 전
  `UninitializedPropertyAccessException`이 `MainActivity.kt:560`에서 발생해
  시험 앱 프로세스가 종료되는 것을 재현
- 수정 뒤 같은 시험에서 기존 학생 목록 유지, 학생 변경 gate 해제,
  관리자 입력 재활성화와 저장 성공·목록 실패를 함께 설명하는 재시도 안내를
  검증

### 변경

- 구현·회귀시험 커밋: `7786ee0`
- Kiosk `0.6.0-rc18`/code 23과 릴리스 운영 경로 준비 커밋: `8fc6e97`
- 관리자 목록 전체를 하나의 `AdminDataSnapshot`으로 읽고 단일
  `runCatching` 실패 경계에서 처리
- 성공한 snapshot만 반·학생·수업·소속 상태에 일괄 반영
- 실패 시 기존 화면 데이터와 선택·수업 상태를 유지하고, 필요한 경우
  학생 변경 단일 실행 gate를 해제한 뒤 관리자 화면 재진입 안내를 표시

### 자동 검증

- 수정 전 신규 관리자 새로고침 실패 계측시험: 프로세스 종료 실패 재현
- 수정 뒤 대상 계측시험과 Android 13 일회용 에뮬레이터 Kiosk 전체 계측
  23개, 실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 78개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 69개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc18-release.apk`
  - 크기: 34,974,012 bytes
  - SHA-256:
    `48E03E8CBD14016C337A9DC9548139B4BD0F49FC4149015255504F601E274CDE`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc17`/code 22,
  Web POC `0.4.0-rc15`/code 32
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시,
  release signer, Device Owner·전용 HOME·`LOCKED`를 확인
- Kiosk RC18만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc18`/code 23,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- 설치된 Kiosk와 RC18 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 설치 시점 이후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 관리자 화면에서 DB 읽기 실패와 후속 재시도 안내를 확인하는 실기
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC19 반 소속 명단 조회 실패의 빈 명단 오인 차단 — 2026-07-26

### 확인한 데이터 손실 위험

반 Spinner 전환 뒤 `membershipStudentIds` 조회가 실패하면 기존 구현은
`getOrDefault(emptySet())`로 예외를 빈 명단으로 바꿨다. 화면에는 실제 빈
반과 같은 `소속 학생 없음`이 표시되고 반 학생 구성·반 삭제·보강 선택·수업
시작이 활성화됐다. 교사가 반 학생 구성 창을 그대로 저장하면 기존 소속
관계를 전부 지울 수 있는 상태였다.

- 실제 A 데이터나 관리자 PIN을 사용하지 않고 합성 반과 일회용 Android 13
  에뮬레이터만 사용
- 회귀 JVM 시험을 먼저 추가했고 수정 전 `hasLoadFailure`와 `fail` 계약
  부재로 Kotlin test 컴파일 실패
- 현재 요청 실패와 이전 요청의 늦은 실패를 각각 시험해 최신 반 선택 보호
- UI 계측시험에서 실패 안내와 반 구성·삭제·보강·수업 시작 비활성화를 확인

### 변경

- 구현·회귀시험 커밋: `74549c4`
- Kiosk `0.6.0-rc19`/code 24와 릴리스 운영 경로 준비 커밋: `2cb6617`
- `ClassRosterSelectionState`가 로딩·성공·실패를 구분하고 성공/재시도 시
  실패 상태를 해제
- 현재 반과 generation이 일치하는 실패만 적용해 늦은 이전 요청의 실패는
  무시
- 실패를 빈 명단으로 적용하지 않고 명시적인 오류와 관리자 화면 재진입
  안내를 표시
- 명단이 성공적으로 확인되기 전에는 반 구성·삭제·보강·수업 시작을
  실패폐쇄

### 자동 검증

- 수정 전 신규 관리자 비동기 상태 JVM 시험: 계약 부재 컴파일 실패 재현
- 수정 뒤 관리자 비동기 상태 대상 JVM 시험과 네 모듈 JVM 단위시험 총
  80개, 실패·오류·건너뜀 0
- Android 13 일회용 에뮬레이터 대상 UI 계측 1개와 Kiosk 전체 계측 24개,
  실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 71개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc19-release.apk`
  - 크기: 34,974,008 bytes
  - SHA-256:
    `13BAD1F3408B355B25A5CCC021B8EA6C9483F6E2B6CABA1EE7B64D29B524624F`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc18`/code 23,
  Web POC `0.4.0-rc15`/code 32
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시,
  release signer, Device Owner·전용 HOME·`LOCKED`를 확인
- Kiosk RC19만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc19`/code 24,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- 설치된 Kiosk와 RC19 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 설치 시점 이후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 관리자 화면에서 반 소속 조회 실패와 후속 재시도 안내를 확인하는 실기
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC20 앱 시작 초기 상태 확인 실패 복구 — 2026-07-26

### 재현한 시작 중 프로세스 종료

`loadInitialState`는 관리자 PIN 등록 여부 조회와 재시작 정책 적용을 예외
경계 밖에서 실행했다. DB 또는 저장소 접근이 실패하면 앱 프로세스가 종료되고
교사에게 원인이나 재시도 경로를 제공하지 못했다.

- 실제 A 데이터나 관리자 PIN을 사용하지 않고 Android 13 일회용
  에뮬레이터의 빈 DB와 고의로 사용할 수 없게 만든 저장소만 사용
- 수정 전 대상 계측시험에서 `UninitializedPropertyAccessException`이
  `MainActivity.kt:398`에서 발생해 앱 프로세스와 계측 실행이 종료됨을 재현
- 수정 뒤 같은 시험에서 실패폐쇄 화면, PIN 입력 비표시, 관리자·스캐너 화면
  차단과 `다시 시도` 활성화를 확인
- 저장소를 복원한 뒤 `다시 시도`로 정상 PIN 설정 화면까지 복귀함을 확인

### 변경

- 구현·회귀시험 커밋: `e4f9ee7`
- Kiosk `0.6.0-rc20`/code 25와 릴리스 운영 경로 준비 커밋: `3af93e9`
- 초기 상태 조회와 재시작 정책 적용을 하나의 snapshot과 단일
  `runCatching` 실패 경계에서 처리
- 초기 확인 중 PIN 입력과 다른 화면을 숨기고 확인 버튼을 비활성화
- 실패 시 `INITIALIZATION_FAILED` 상태와 데이터 미변경 안내를 표시하고
  전용 잠금 안에서 `다시 시도`만 허용
- 재시도 성공 시 기존 PIN 설정 또는 인증 흐름으로 정상 복귀

### 자동 검증

- 수정 전 신규 초기 상태 실패 계측시험: 앱 프로세스 종료 실패 재현
- 수정 뒤 대상 실패·재시도 시험과 Android 13 일회용 에뮬레이터 Kiosk
  전체 계측 25개, 실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 80개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 71개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc20-release.apk`
  - 크기: 34,974,008 bytes
  - SHA-256:
    `313B01CCE83ECD6C7FF480C1013F36DFB2098E6E551B809B081F55EC65FEFC61`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc19`/code 24,
  Web POC `0.4.0-rc15`/code 32
- 유일한 ADB `device`, 정확한 serial·SM-P610, USB 전원·배터리 100%,
  화면 `Dozing`, UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시,
  release signer, Device Owner·전용 HOME·`LOCKED`를 확인
- Kiosk RC20만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc20`/code 25,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 보관본 일치
- 설치된 Kiosk와 RC20 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB·100% 유지
- 설치 시점 이후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 초기 상태 조회 실패와 `다시 시도` 복구를 확인하는 실기
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC21 Web 복귀 결과 확인 실패폐쇄 — 2026-07-26

### 확인한 상태 불일치 위험

Web 화면에서 Kiosk로 돌아온 뒤 수업 상태 변경과 현재 세션 재조회가 별도
결과로 처리됐다. 상태 변경이 성공해도 재조회 예외는 `null`로 버려졌고,
성공 복귀라면 스캐너 전환을 계속 시도해 저장 상태와 화면 상태가 어긋날 수
있었다.

- 신규 JVM 계약 4개를 먼저 추가했으며 기존 코드에는
  `WebSessionResultPersistence`가 없어 컴파일 실패함을 확인
- 상태 변경과 활성 세션 재조회를 하나의 `Result` 성공 조건으로 결합
- 재조회 예외 또는 활성 세션 부재를 성공 복귀로 처리하지 않음
- 결과 확인 실패 시 현재 화면 세션을 제거하고 스캐너·관리자 화면을 숨긴
  관리자 PIN 복구 화면과 재확인 안내를 표시
- 실제 A 데이터 대신 Android 13 일회용 에뮬레이터에서 저장소를 고의로
  사용할 수 없게 한 계측시험을 사용
- 첫 수정안은 저장소 메서드 참조를 실패 경계 밖에서 평가해
  `UninitializedPropertyAccessException`과 앱 프로세스 종료가 발생
- 저장소 접근 전체를 실패 경계 안으로 옮긴 뒤 같은 계측시험에서 프로세스
  종료 없이 닫힌 복구 화면을 확인

### 변경

- 구현·회귀시험 커밋: `14a939d`
- Kiosk `0.6.0-rc21`/code 26과 릴리스 운영 경로 준비 커밋: `fc81f9e`
- 상태 전이 실패 시 세션 재조회를 시도하지 않고, 상태 전이와 재조회가 모두
  성공한 경우에만 성공/실패 Web 결과 UI를 적용
- 성공 결과는 실제 `QR_READY` 활성 세션이 다시 로드된 뒤에만 스캐너로 복귀
- 결과 저장 또는 재조회 실패는 DB를 추가 변경하지 않고 관리자 PIN 확인으로
  전환

### 자동 검증

- 수정 전 신규 JVM 계약: 구현 부재 컴파일 실패 확인
- 수정 중 신규 계측시험: 실패 경계 밖 저장소 접근으로 앱 프로세스 종료 재현
- 수정 뒤 신규 JVM 4개와 실패 UI 계측시험: 통과
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 26개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 첫 clean debug 호출은 시간 제한 뒤 남은 빌드와 재호출이 겹쳐
  `:kiosk:clean` 파일 잠금으로 무효화; Gradle daemon을 정상 종료하고 단일
  실행으로 재검증
- 네 모듈 단일 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc21-release.apk`
  - 크기: 34,974,008 bytes
  - SHA-256:
    `2C2938FC960173DE2EF1FF86065322B0FBE16F2EACB4E8757C1EEC7985C77081`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc20`/code 25,
  Web POC `0.4.0-rc15`/code 32
- 유일한 물리 ADB `device`, 정확한 serial·SM-P610, UID·firstInstallTime·
  dataDir, 설치본·신규 artifact 해시와 release signer, Device Owner·전용
  HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을 확인
- Kiosk RC21만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 3초 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc21`/code 26,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 RC21 보관본 일치
- 설치된 Kiosk와 RC21 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 Web 복귀 결과 저장·재조회 실패와 관리자 PIN 복구를 확인하는
  고의 실패주입
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC22 취소된 Web 준비 상태 복구 실패폐쇄 — 2026-07-26

### 재현한 무응답 복구 실패

QR 승인 뒤 Kiosk가 자격정보 handle을 준비하는 동안 스캐너 화면을 떠나면,
Web을 실행하지 않고 `PRELOGIN_CHECK`를 `QR_READY`로 되돌린다. 기존 코드는
이 복구의 상태 전이 또는 현재 세션 조회가 실패하면 결과를 `null`로 바꾼 뒤
아무 안내도 하지 않았다. DB가 준비 상태에 남거나 화면이 이전 상태를 계속
표시할 수 있었다.

- Android 13 일회용 에뮬레이터의 빈 DB와 고의로 사용할 수 없게 만든
  저장소만 사용
- 신규 계측시험을 먼저 추가했고 수정 전 3초 안에 기대한 복구 오류 안내가
  나타나지 않는 timeout 실패를 재현
- 상태 전이와 현재 세션 조회를 하나의 실패 경계로 결합
- 조회된 세션에 활성 session ID가 있고 상태가 정확히 `QR_READY`인 경우만
  복구 성공으로 처리
- 예외·세션 부재·잘못된 상태에서는 현재 화면 세션을 제거하고 스캐너와
  관리자 화면을 숨긴 관리자 PIN 복구 화면을 표시

### 변경

- 구현·회귀시험 커밋: `a970c7d`
- Kiosk `0.6.0-rc22`/code 27과 릴리스 운영 경로 준비 커밋: `555f4ec`
- 실패 시 DB를 추가 변경하거나 성공 상태로 가정하지 않고 교사가 PIN 인증
  뒤 최신 세션 상태를 다시 읽도록 안내

### 자동 검증

- 수정 전 신규 계측시험: 오류 안내 미표시 timeout 실패 재현
- 수정 뒤 대상 계측시험: 통과
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 27개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc22-release.apk`
  - 크기: 34,974,008 bytes
  - SHA-256:
    `F391504A1B1FAFAB309C43EE3F345D1FF6784EEEDD0400E0C4F2386D8556B5E4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc21`/code 26,
  Web POC `0.4.0-rc15`/code 32
- 유일한 물리 ADB `device`, 정확한 serial·SM-P610, UID·firstInstallTime·
  dataDir, 설치본·신규 artifact 해시와 release signer, Device Owner·전용
  HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을 확인
- Kiosk RC22만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 3초 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc22`/code 27,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 RC22 보관본 일치
- 설치된 Kiosk와 RC22 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 QR 준비 중 관리자 화면 전환과 복구 실패를 겹치는 고의 실패주입
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC23 QR 검증 오류 뒤 스캐너 재활성화 차단 — 2026-07-26

### 재현한 일시 잠금 뒤 자동 재개

기존 QR 검증 실패 분기는 “채점기가 잠겼습니다”와 `LOCKED`를 표시하면서도
정상적인 미사용 카드 거부와 같은 cooldown 재개를 예약했다. 약 1.5초 뒤
`resumeScannerAfterCooldown`이 `QR_READY`를 다시 표시하고 QR 분석기를
활성화하므로 저장소 오류 뒤 잠금이 실제로 유지되지 않았다.

- 실제 A·실제 QR·실제 학생을 사용하지 않음
- Android 13 일회용 에뮬레이터에서 앱 시작과 PIN 인증을 마친 뒤 합성
  반·학생·수업과 합성 32-byte QR hash를 사용
- QR 검증 직전에 저장소를 고의로 사용할 수 없게 해 검증 예외를 발생
- 첫 시험 구성 두 번은 앱 시작 재시작 정책과 시험 준비 순서 때문에 목표
  분기 전에 timeout됐고, 앱 시작·PIN 인증 뒤 합성 수업을 만드는 방식으로
  격리
- 수정 전 정확한 QR 실패 분기에서 3초 안에 관리자 복구 안내가 나타나지
  않는 실패를 재현

### 변경

- 구현·회귀시험 커밋: `bad5d03`
- Kiosk `0.6.0-rc23`/code 28과 릴리스 운영 경로 준비 커밋: `fd12b23`
- QR 검증 예외에서 cooldown 재개를 예약하지 않음
- 현재 화면 세션 참조를 지우고 카메라·스캐너를 중지한 관리자 PIN 복구
  화면으로 전환
- 일반적인 미사용·폐기·다른 반 QR의 `null` 거부는 기존 cooldown 재개
  동작을 유지

### 자동 검증

- 수정 전 신규 계측시험: 정확한 실패 분기에서 관리자 복구 안내 미표시 재현
- 수정 뒤 대상 계측시험: 추가 2초 대기 후에도 `LOCKED`, 관리자 PIN 화면
  표시, 스캐너 숨김 유지
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 28개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc23-release.apk`
  - 크기: 34,974,008 bytes
  - SHA-256:
    `647CB5932F503DF5AB312AD994C9F18C2488A09F7C798C43F78B96B20564CB97`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc22`/code 27,
  Web POC `0.4.0-rc15`/code 32
- 유일한 물리 ADB `device`, 정확한 serial·SM-P610, UID·firstInstallTime·
  dataDir, 설치본·신규 artifact 해시와 release signer, Device Owner·전용
  HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을 확인
- Kiosk RC23만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 3초 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc23`/code 28,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 RC23 보관본 일치
- 설치된 Kiosk와 RC23 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 QR 검증 저장소 오류와 관리자 PIN 복구를 확인하는 고의 실패주입
- 실제 QR→Web→QR 왕복과 정상 `QR_READY`
- 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC24 카메라 시작 오류 실패폐쇄 — 2026-07-26

### 발견한 앱 종료 위험

카메라 제공자 future 조회, 카메라 유무 확인, 분석기 구성, 기존 use case
해제와 lifecycle 바인딩이 예외 경계 밖에 있었다. CameraX 초기화나
바인딩이 예외를 던지면 main executor callback에서 앱 프로세스까지 예외가
전파되고, QR 스캐너가 안전한 복구 화면으로 전환된다는 보장이 없었다.

- 실제 A 카메라와 수업에는 오류를 주입하지 않음
- Android 13 일회용 에뮬레이터에서 합성 PIN·반·학생·수업을 사용
- 스캐너를 연 뒤 카메라 바인딩 실패 복구 경로를 호출하는 계측시험을 먼저
  추가
- 수정 전 복구 메서드가 없어 시험이 `NoSuchMethodException`으로 실패함을
  확인

### 변경

- 구현·회귀시험 커밋: `90c50fa`
- Kiosk `0.6.0-rc24`/code 29와 릴리스 운영 경로 준비 커밋: `3ec467b`
- 카메라 제공자 조회 뒤 유무 확인·분석기 구성·unbind·bind를 하나의 예외
  경계로 묶음
- 현재 바인딩 세대에서 발생한 오류만 처리하고, 오래된 비동기 callback은
  기존처럼 무시
- 카메라 해제 자체가 예외를 내더라도 QR 분석기를 비활성화하고 제공자 참조를
  제거한 뒤 스캐너를 닫은 관리자 PIN 복구 화면과 `CAMERA_ERROR`를 표시

### 자동 검증

- 수정 전 신규 계측시험: 카메라 실패폐쇄 복구 경로 부재 재현
- 수정 뒤 대상 계측시험: `CAMERA_ERROR`, 관리자 PIN 화면 표시, 스캐너
  숨김 확인
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 29개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc24-release.apk`
  - 크기: 34,974,008 bytes
  - SHA-256:
    `2B7D7B95746E906D4E32D598C3CF673B85BE208F12EE55F5FDFB67F180488950`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc23`/code 28,
  Web POC `0.4.0-rc15`/code 32
- 유일한 물리 ADB `device`, 정확한 serial·SM-P610, UID·firstInstallTime·
  dataDir, 설치본·신규 artifact 해시와 release signer, Device Owner·전용
  HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을 확인
- Kiosk RC24만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 3초 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc24`/code 29,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 RC24 보관본 일치
- 설치된 Kiosk와 RC24 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A 카메라 초기화·바인딩 오류와 관리자 PIN 복구를 확인하는 고의
  실패주입
- 실제 전면·후면 카메라 전환과 QR→Web→QR 정상 왕복
- 관리자 PIN·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC25 QR 거부 감사기록 오류 실패폐쇄 — 2026-07-26

### 재현한 백그라운드 프로세스 종료

잘못된 QR 또는 복수 QR을 인식한 분기는 거부 사유 감사기록을
`ioExecutor`에서 저장했지만 예외를 처리하지 않았고, 저장 결과를 기다리지
않은 채 scanner cooldown도 예약했다. DB 오류가 나면 백그라운드 미처리
예외가 앱 프로세스를 종료하고 스캐너 재개 예약과 실제 상태가 어긋날 수
있었다.

- 실제 A·실제 QR·실제 학생을 사용하지 않음
- 첫 계측 fixture는 일회용 에뮬레이터의 실제 카메라 분석기 활성화를 기다리다
  목표 분기 전에 timeout되어 시험 준비를 수정
- 합성 PIN·반·학생·수업, 합성 활성 QR 분석기와 scanner UI 상태로 카메라
  의존성을 제거
- 저장소를 고의로 사용할 수 없게 한 뒤 `INVALID_QR` 결정을 전달
- 수정 전 `UninitializedPropertyAccessException`이 executor에서 발생해
  instrumentation 대상 프로세스가 종료됨을 재현

### 변경

- 구현·회귀시험 커밋: `5026daf`
- Kiosk `0.6.0-rc25`/code 30과 릴리스 운영 경로 준비 커밋: `abbd256`
- 거부 감사기록 저장을 예외 경계 안에서 수행하고 성공한 경우에만 scanner
  cooldown 재개를 예약
- 저장 실패나 executor 작업 예약 실패 시 현재 화면 세션 참조를 제거하고
  카메라·스캐너를 닫은 관리자 PIN 복구 화면과 명시적 오류를 표시
- 사용자가 이미 scanner를 떠났거나 Activity가 파기된 늦은 완료 결과는
  화면을 변경하지 않음

### 자동 검증

- 수정 전 신규 계측시험: 감사기록 저장 예외와 앱 프로세스 종료 재현
- 수정 뒤 대상 계측시험: 추가 2초 뒤에도 `LOCKED`, 관리자 PIN 화면 표시,
  스캐너 숨김 유지
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 30개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc25-release.apk`
  - 크기: 34,974,012 bytes
  - SHA-256:
    `6DEC70E58BE3B61500E92671301670296CBCCF412C439C720F568C2D900DF4B9`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc24`/code 29,
  Web POC `0.4.0-rc15`/code 32
- 유일한 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Kiosk RC25만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 3초 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc25`/code 30,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 RC25 보관본 일치
- 설치된 Kiosk와 RC25 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 감사기록 저장 실패와 관리자 PIN 복구를 확인하는 고의 실패주입
- 실제 잘못된 QR·복수 QR의 정상 거부와 scanner cooldown
- 실제 QR→Web→QR 왕복과 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Kiosk RC26 QR 분석기 프레임 오류 복구 — 2026-07-26

### 재현한 프레임 누수와 분석 중단

`QrImageAnalyzer`는 processing 소유권을 얻은 뒤 Android 이미지 조회,
`InputImage` 생성과 ML Kit 처리 시작을 예외 경계 밖에서 수행했다. 이 중
하나가 동기 예외를 내면 processing 플래그를 해제하지 않고 `ImageProxy`도
닫지 않아 이후 프레임을 계속 거부하며, 미처리 예외가 카메라 분석 executor로
전파될 수 있었다. 프레임 닫기 자체의 예외도 그대로 전파됐다.

- 실제 A 카메라·실제 QR을 사용하지 않음
- Android 13 일회용 에뮬레이터에서 이미지 조회 시 의도적으로 예외를 내고
  `close()` 횟수를 세는 합성 `ImageProxy`를 사용
- 수정 전 첫 `analyze` 호출이 `IllegalStateException`을 그대로 전파해 시험
  실패

### 변경

- 구현·회귀시험 커밋: `4609e2f`
- Kiosk `0.6.0-rc26`/code 31과 릴리스 운영 경로 준비 커밋: `60b8304`
- 프레임마다 독립적인 일회성 완료 경계를 두어 동기 준비 예외, 비동기 ML
  결과 성공·실패와 프레임 close 예외에서 processing 플래그와 프레임
  소유권을 정확히 한 번 해제
- ML Kit task는 하나의 완료 callback에서 성공 결과만 해석하고 실패 frame은
  다음 frame을 막지 않음
- 분석 비활성·다른 frame 처리 중에 들어온 frame의 close 예외도 분석
  executor를 종료시키지 않음

### 자동 검증

- 수정 전 신규 계측시험: 합성 이미지 조회 예외 전파 재현
- 수정 뒤 대상 계측시험: 연속 실패 frame 2개가 각각 한 번 닫히고 두 번째
  frame도 처리됨을 확인
- Android 13 일회용 에뮬레이터 Kiosk 전체 계측 31개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc15-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 3,085,840 bytes
  - SHA-256:
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc25`/code 30,
  Web POC `0.4.0-rc15`/code 32
- 유일한 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Kiosk RC26만 `adb install -r`: 성공
- 설치 직후 HOME 프로세스 종료로 Lock Task `NONE`; 화면을 깨우지 않는
  명시적 HOME 시작 3초 뒤 `LOCKED` 복구
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc15`/code 32
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Kiosk base APK SHA-256과 RC26 보관본 일치
- 설치된 Kiosk와 RC26 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A 카메라의 이미지 조회·ML Kit 처리 오류와 자동 frame 복구
- 실제 전면·후면 QR 인식과 camera 전환
- 실제 QR→Web→QR 왕복과 관리자 PIN·PDF 공유·실물 인쇄 회귀

---

## Web POC RC16 자바스크립트 평가 시작 오류 복구 — 2026-07-26

### 재현한 Web 프로세스 종료 경로

WebView renderer 종료나 lifecycle 경계에서 `evaluateJavascript` 호출 자체가
동기 예외를 내면 기존 코드는 이를 main thread 밖으로 전파했다. 이 경우
안전한 잠금 상태를 저장하기 전에 Web 앱 프로세스가 종료될 수 있었다.

- 실제 A 사이트나 자격정보를 사용하지 않음
- Android 13 일회용 에뮬레이터에서 평가 시작 시 의도적으로 예외를 내는
  합성 WebView를 사용
- 수정 전 `IllegalStateException: synthetic evaluation failure`가
  `MainActivity.evaluate` 밖으로 전파돼 대상 계측시험 실패

### 변경

- 구현·회귀시험 커밋: `e793539`
- Web POC `0.4.0-rc16`/code 33과 릴리스 운영 경로 준비 커밋: `fe22659`
- 자바스크립트 평가 시작의 동기 `RuntimeException`을 포착
- Activity가 살아 있고 아직 terminal 상태가 아니면 `WEB_EVALUATION`으로
  실패폐쇄해 Kiosk의 기존 관리자 복구 흐름으로 반환 가능하게 함

### 자동 검증

- 수정 전 신규 계측시험: 합성 평가 시작 예외 전파 재현
- 수정 뒤 대상 계측시험: 예외가 빠져나오지 않고 `LOCKED`,
  이유 `WEB_EVALUATION` 저장 확인
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 47개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc16-release.apk`
  - 크기: 3,085,908 bytes
  - SHA-256:
    `0072A825EB1B3F488F6C5FA58DF35FCABBDAAFC95E8A48723638D71970347291`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc15`/code 32
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC16만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc16`/code 33
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC16 보관본 일치
- 설치된 Web POC와 RC16 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 뒤 최근 10분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 WebView 평가 시작 오류와 Kiosk 관리자 복구를 확인하는 고의
  실패주입
- 실제 QR→Web→QR 왕복과 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Web POC RC17 자바스크립트 평가 완료 콜백 오류 복구 — 2026-07-26

### 재현한 비동기 콜백 예외

`evaluateJavascript` 호출이 성공해 반환된 뒤 WebView가 main thread에서
나중에 전달하는 결과 콜백은 평가 시작을 감싼 바깥 `try` 범위에 포함되지
않았다. 결과 파싱 뒤 DOM 후속 처리에서 동기 예외가 나면 Web 프로세스 종료로
전파될 수 있었다.

- 실제 A 사이트나 자격정보를 사용하지 않음
- Android 13 일회용 에뮬레이터에서 평가 결과 callback을 저장했다가 평가
  호출 종료 뒤 별도로 전달하는 합성 WebView를 사용
- 수정 전 `IllegalStateException: synthetic callback failure`가 결과
  callback 밖으로 전파되고 잠금 상태가 저장되지 않아 대상 계측시험 실패

### 변경

- 구현·회귀시험 커밋: `d0497be`
- Web POC `0.4.0-rc17`/code 34와 릴리스 운영 경로 준비 커밋: `27c5fb1`
- WebView 결과 파싱과 후속 callback 호출을 별도 예외 경계로 감쌈
- Activity가 살아 있고 아직 terminal 상태가 아니면 `WEB_CALLBACK`으로
  실패폐쇄

### 자동 검증

- 수정 전 신규 계측시험: 평가 호출 뒤 별도 전달한 callback 예외 전파 재현
- 수정 뒤 대상 계측시험: 예외가 빠져나오지 않고 `LOCKED`,
  이유 `WEB_CALLBACK` 저장 확인
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 48개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc17-release.apk`
  - 크기: 3,085,980 bytes
  - SHA-256:
    `1B3B53CA99270BAACD7B47F3D676CDF7C5EBE99CED334EC107F6085CD93B6BF4`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc16`/code 33
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC17만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc17`/code 34
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC17 보관본 일치
- 설치된 Web POC와 RC17 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 뒤 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 Web callback 오류와 Kiosk 관리자 복구를 확인하는 고의
  실패주입
- 실제 QR→Web→QR 왕복과 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Web POC RC18 renderer 정리 오류 격리 — 2026-07-26

### 재현한 실패폐쇄 중단

renderer 종료 callback은 사용할 수 없는 WebView 참조를 해제한 뒤 뷰에서
제거하고 파기하지만, 제거·파기 자체의 예외를 처리하지 않았다. 이 예외가
전파되면 바로 다음 `WEB_PROCESS_GONE` 잠금 전환에 도달하지 못했다.

- 실제 A renderer나 사이트를 사용하지 않음
- Android 13 일회용 에뮬레이터에서 `destroy()`가 예외를 내는 합성
  WebView를 renderer 종료 callback에 전달
- 수정 전 `IllegalStateException: synthetic destroy failure`가 callback
  밖으로 전파되고 잠금 상태가 저장되지 않아 대상 계측시험 실패

### 변경

- 구현·회귀시험 커밋: `868705f`
- Web POC `0.4.0-rc18`/code 35와 릴리스 운영 경로 준비 커밋: `43dd12d`
- Activity의 WebView 참조를 먼저 해제
- 뷰 계층 제거와 WebView 파기를 독립 예외 경계로 분리해 어느 정리 단계의
  오류도 renderer 실패폐쇄 상태 전환을 막지 않게 함

### 자동 검증

- 수정 전 신규 계측시험: 합성 WebView 파기 예외 전파와 잠금 미전환 재현
- 수정 뒤 대상 계측시험: 예외가 빠져나오지 않고 WebView 참조가 제거되며
  `LOCKED`, 이유 `WEB_PROCESS_GONE` 저장 확인
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 49개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc18-release.apk`
  - 크기: 3,086,020 bytes
  - SHA-256:
    `5A05BB0AD7ADFEBB2F203E9AE109C4AD2B0B496B2AC3FA756D7259D4596EC433`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc17`/code 34
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC18만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc18`/code 35
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC18 보관본 일치
- 설치된 Web POC와 RC18 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 뒤 최근 5분 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 renderer 정리 오류와 Kiosk 관리자 복구를 확인하는 고의
  실패주입
- 실제 renderer 종료 복구·QR→Web→QR 왕복과 관리자 PIN·카메라·PDF 공유·
  실물 인쇄 회귀

---

## Web POC RC19 Activity 종료 정리 오류 격리 — 2026-07-26

### 재현한 프로세스 종료

Activity 종료 시 WebView의 로딩 중지, 빈 문서 전환, 기록·캐시·SSL 정리와
파기를 한 연속 호출로 수행했다. 앞 단계가 예외를 내면 뒤 정리 단계와
`super.onDestroy()`에 도달하지 못했다.

- 실제 A, 공개 사이트와 자격정보를 사용하지 않음
- Android 13 일회용 에뮬레이터에서 `stopLoading()`이
  `IllegalStateException`을 내는 합성 WebView로 기존 Activity 종료 실행
- 수정 전 `Unable to destroy activity`와 Web POC 프로세스 crash를 재현

### 변경

- 구현·회귀시험 커밋: `e9a017e`
- Web POC `0.4.0-rc19`/code 36과 릴리스 운영 경로 준비 커밋: `d93a4dd`
- WebView 정리 일곱 단계를 각각 독립 예외 경계로 실행
- Activity의 WebView 참조는 정리 전에 해제하고, 상위 `onDestroy()`는
  `finally`에서 항상 실행

### 자동 검증

- 수정 전 신규 대상 계측시험: 합성 `stopLoading()` 예외가 Activity 종료와
  Web 프로세스 밖으로 전파되는 실패 재현
- 수정 뒤 대상 계측시험: 첫 정리 오류가 있어도 빈 문서 전환과 `destroy()`를
  계속 실행하고 예외가 빠져나오지 않음
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 50개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc19-release.apk`
  - 크기: 3,088,588 bytes
  - SHA-256:
    `77176D0778A71E98DEABF453F2615781C5D7CC30DEA1FA31206E8D66EA1B7531`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc18`/code 35
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC19만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc19`/code 36
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC19 보관본 일치
- 설치된 Web POC와 RC19 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 시각 이후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 Activity 종료 정리 오류와 Kiosk 관리자 복구를 확인하는 고의
  실패주입
- 실제 QR→Web→QR 왕복과 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Web POC RC20 세션 정리 오류 실패폐쇄 — 2026-07-26

### 재현한 정리 중단

로그아웃 확인 뒤 WebView 기록·form·cache·SSL과 WebViewDatabase,
WebStorage, cookie를 연속 정리했다. 한 단계가 예외를 내면 뒤 정리와
명시적인 실패폐쇄 전환에 도달하지 못했다.

- 실제 A, 공개 사이트·cookie와 자격정보를 사용하지 않음
- Android 13 일회용 에뮬레이터에서 `clearHistory()`가
  `IllegalStateException`을 내는 합성 WebView로 정리 함수 실행
- 수정 전 예외가 함수 밖으로 전파되고 후속 `clearCache()` 호출 횟수가
  0인 대상 계측시험 실패

### 변경

- 구현·회귀시험 커밋: `d9601b7`
- Web POC `0.4.0-rc20`/code 37과 릴리스 운영 경로 준비 커밋: `e08a32e`
- 동기 Web·storage 정리 단계와 cookie 삭제 요청을 독립 예외 경계로 실행
- 일부 단계가 실패해도 남은 정리를 시도한 뒤 `SESSION_CLEAR`로 잠금
- cookie flush·지연 로그인 재로딩 오류와 Handler 예약 거부도 현재 로그아웃
  세대에서만 실패폐쇄

### 자동 검증

- 수정 전 신규 대상 계측시험: 합성 `clearHistory()` 예외 전파와 후속
  `clearCache()` 미실행 재현
- 수정 뒤 대상 계측시험: 예외가 빠져나오지 않고 `clearCache()`를 계속
  실행하며 상태 `LOCKED`, 이유 `SESSION_CLEAR` 저장 확인
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 51개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc20-release.apk`
  - 크기: 3,091,724 bytes
  - SHA-256:
    `862FD9F6173531A14D49FD2F9F35727C40DE5C65BD9CBE4B06302322F5C518AF`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc19`/code 36
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC20만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc20`/code 37
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC20 보관본 일치
- 설치된 Web POC와 RC20 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 시각 이후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 세션 정리 오류와 Kiosk 관리자 복구를 확인하는 고의 실패주입
- cookie flush·지연 로그인 재로딩 오류의 합성 실패주입
- 실제 QR→Web→QR 왕복과 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Web POC RC21 허용되지 않은 이동 중지 오류 실패폐쇄 — 2026-07-26

### 재현한 잠금 전환 중단

허용되지 않은 최상위 문서가 시작되면 WebView 로딩을 중지한 뒤
`NAVIGATION_BLOCKED`로 잠갔다. 죽어가는 renderer 경계에서
`stopLoading()`이 동기 예외를 내면 잠금 전환에 도달하지 못했다.

- 실제 A, 공개 사이트와 자격정보를 사용하지 않음
- Android 13 일회용 에뮬레이터에서 `stopLoading()`이
  `IllegalStateException`을 내는 합성 WebView로 `onPageStarted` 실행
- 수정 전 예외가 WebViewClient 콜백 밖으로 전파되는 대상 계측시험 실패

### 변경

- 구현·회귀시험 커밋: `ce95b42`
- Web POC `0.4.0-rc21`/code 38과 릴리스 운영 경로 준비 커밋: `f4469bf`
- 허용되지 않은 문서의 로딩 중지 오류를 격리
- 중지 성공 여부와 무관하게 `NAVIGATION_BLOCKED` 상태를 저장하고
  실패폐쇄 UI로 전환

### 자동 검증

- 수정 전 신규 대상 계측시험: 합성 `stopLoading()` 예외 전파 재현
- 수정 뒤 대상 계측시험: 예외가 빠져나오지 않고 상태 `LOCKED`, 이유
  `NAVIGATION_BLOCKED` 저장 확인
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 52개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc21-release.apk`
  - 크기: 3,091,972 bytes
  - SHA-256:
    `182743349523E8A7353BE12193DC5B9F45BA5376B245A4E639FBF4E2CDD2AF7A`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc20`/code 37
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC21만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc21`/code 38
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC21 보관본 일치
- 설치된 Web POC와 RC21 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 시각 이후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 허용 외 최상위 이동과 WebView 중지 오류를 확인하는 고의
  실패주입
- 실제 QR→Web→QR 왕복과 관리자 PIN·카메라·PDF 공유·실물 인쇄 회귀

---

## Web POC RC22 학생 학습지 복귀 제어 오류 실패폐쇄 — 2026-07-26

### 재현한 학생 경로 복귀 중단

학생 `ACTIVE` 상태에서 포털 등 허용된 최상위 문서이지만 학생용이 아닌
경로가 시작되면 기존 코드는 로딩을 중지하고 마지막 허용 학습지로 되돌렸다.
이때 WebView 제어가 동기 예외를 내면 잠금 전환 없이 콜백 밖으로 전파됐다.

- 실제 A, 공개 사이트와 자격정보를 사용하지 않음
- Android 13 일회용 에뮬레이터에서 첫 `loadUrl()`이
  `IllegalStateException`을 내는 합성 WebView로 학생 상태의
  `onPageStarted` 실행
- 수정 전 예외가 WebViewClient 콜백 밖으로 전파되는 대상 계측시험 실패

### 변경

- 구현·회귀시험 커밋: `084d6b9`
- Web POC `0.4.0-rc22`/code 39와 릴리스 운영 경로 준비 커밋: `4548069`
- 학생 경로 복귀의 `stopLoading()`과 마지막 허용 URL `loadUrl()`을 단일
  오류 경계로 실행
- WebView 부재나 어느 제어 단계의 오류든 `NAVIGATION_BLOCKED` 상태를
  저장하고 실패폐쇄 UI로 전환

### 자동 검증

- 수정 전 신규 대상 계측시험: 합성 첫 `loadUrl()` 예외 전파 재현
- 수정 뒤 대상 계측시험: 예외가 빠져나오지 않고 상태 `LOCKED`, 이유
  `NAVIGATION_BLOCKED` 저장 확인
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 53개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc22-release.apk`
  - 크기: 3,092,092 bytes
  - SHA-256:
    `3EAA30E6BC793A0FB1896F09B043387ABFD1082FA65E08E931BF7C80A5297E43`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc21`/code 38
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC22만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc22`/code 39
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC22 보관본 일치
- 설치된 Web POC와 RC22 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 시각 이후 AndroidRuntime 로그의 Matholic package 일치 항목: 0

### 미검증

- 실제 A에서 학생 경로 복귀 제어 오류를 확인하는 고의 실패주입
- 실제 포털 리디렉션 복귀, QR→Web→QR 왕복과 관리자 PIN·카메라·PDF
  공유·실물 인쇄 회귀

---

## Web POC RC23 Web 탐색 시작 오류 실패폐쇄 — 2026-07-26

### 재현한 복구 탐색 중단

복구·로그인·학생 탭·Gate 3·로그아웃 흐름의 일반 Web 탐색 시작은
`WebView.loadUrl()`을 직접 호출했다. 이 호출이 동기 예외를 내면 일부
경로에서 잠금 전환 없이 main thread로 전파될 수 있었다.

- 실제 A, 공개 사이트와 자격정보를 사용하지 않음
- Android 13 일회용 에뮬레이터에서 첫 `loadUrl()`이
  `IllegalStateException`을 내는 합성 WebView로 `RECOVERY_REQUIRED`
  복구 시작 실행
- 수정 전 예외가 복구 호출 밖으로 전파되는 대상 계측시험 실패

### 변경

- 구현·회귀시험 커밋: `a569244`
- Web POC `0.4.0-rc23`/code 40과 릴리스 운영 경로 준비 커밋: `f57e873`
- 로그인·복구·학생 탭·Gate 3·로그아웃의 일반 Web 탐색 시작을 공통 오류
  경계로 실행
- 동기 탐색 시작 오류를 `WEB_NAVIGATION` 상태로 저장하고 실패폐쇄 UI로
  전환
- 세션 정리 중의 마지막 로그인 문서 탐색은 기존 `SESSION_CLEAR` 오류
  의미를 보존

### 자동 검증

- 수정 전 신규 대상 계측시험: 합성 첫 `loadUrl()` 예외 전파 재현
- 수정 뒤 대상 계측시험: 예외가 빠져나오지 않고 상태 `LOCKED`, 이유
  `WEB_NAVIGATION` 저장 확인
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 54개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc23-release.apk`
  - 크기: 3,092,240 bytes
  - SHA-256:
    `22015D506B2FFE4D47313051C1CE634589B6F05948ADF4B8B4259B61E3196C2E`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc22`/code 39
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC23만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc23`/code 40
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC23 보관본 일치
- 설치된 Web POC와 RC23 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 시각 이후 AndroidRuntime 오류 일치 항목: 0

### 미검증

- 실제 A에서 Web 탐색 시작 오류를 확인하는 고의 실패주입
- 실제 QR→Web→QR 왕복과 관리자 PIN·사이트·카메라·PDF 공유·실물 인쇄
  회귀

---

## Web POC RC24 사전점검 DNS 재시도 준비 오류 실패폐쇄 — 2026-07-26

### 재현한 재시도 준비 중단

로그인 호스트의 첫 DNS 오류는 자격정보 입력 전 `PREFLIGHT`에서만 3.5초
뒤 한 번 재시도한다. 기존 경로는 현재 로딩 중지와 지연 예약을 직접
수행해, 죽어가는 WebView의 `stopLoading()`이 동기 예외를 내면 잠금
전환 없이 main thread로 전파될 수 있었다.

- 실제 A, 공개 사이트와 자격정보를 사용하지 않음
- Android 13 일회용 에뮬레이터에서 `stopLoading()`이
  `IllegalStateException`을 내는 합성 WebView 사용
- 수정 전 재시도 준비 안전 경계가 없어 신규 대상 계측시험 실패

### 변경

- 구현·회귀시험 커밋: `02e8be4`
- Web POC `0.4.0-rc24`/code 41과 릴리스 운영 경로 준비 커밋: `188ab55`
- DNS 재시도의 로딩 중지와 Handler 지연 예약을 단일 오류 경계로 실행
- 로딩 중지 예외와 예약 거부는 `WEB_NAVIGATION`으로 저장하고 실패폐쇄
- 예약 성공 뒤에만 재시도 예약 상태를 저장

### 자동 검증

- 수정 전 신규 대상 계측시험: 재시도 준비 안전 경계 부재로 실패
- 수정 뒤 대상 계측시험: 합성 `stopLoading()` 예외가 빠져나오지 않고
  상태 `LOCKED`, 이유 `WEB_NAVIGATION` 저장 확인
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 55개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc24-release.apk`
  - 크기: 3,092,068 bytes
  - SHA-256:
    `5FC92F7DC0781A0614705B0C715D01FEBD7A1396C2318B29574F5DE5B03E16D1`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc23`/code 40
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC24만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc24`/code 41
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC24 보관본 일치
- 설치된 Web POC와 RC24 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 시각 이후 AndroidRuntime 오류 일치 항목: 0

### 미검증

- 실제 A에서 DNS 오류와 WebView 중지 실패를 겹치는 고의 실패주입
- 실제 QR→Web→QR 왕복과 관리자 PIN·사이트·카메라·PDF 공유·실물 인쇄
  회귀

---

## Web POC RC25 Web 보안 거부 콜백 오류 실패폐쇄 — 2026-07-26

### 재현한 보안 거부 중단

TLS 오류와 Safe Browsing 위협은 플랫폼 콜백으로 현재 요청을 거부한 뒤
Web POC를 잠갔다. 기존 경로는 `SslErrorHandler.cancel()` 또는
`SafeBrowsingResponse.backToSafety()`가 동기 예외를 내면 잠금 전환까지
도달하지 못할 수 있었다.

- 실제 A, 공개 사이트와 자격정보를 사용하지 않음
- Android 13 일회용 에뮬레이터에서 거부 콜백이
  `IllegalStateException`을 내는 합성 함수 사용
- 수정 전 공통 보안 거부 실패폐쇄 경계가 없어 신규 대상 계측시험 실패

### 변경

- 구현·회귀시험 커밋: `88995b9`
- Web POC `0.4.0-rc25`/code 42와 릴리스 운영 경로 준비 커밋: `f5acace`
- TLS 취소와 Safe Browsing 안전 복귀 요청을 공통 오류 경계에서 실행
- 플랫폼 거부 콜백의 런타임 예외를 격리하고 각각 `TLS_ERROR`,
  `SAFE_BROWSING`으로 반드시 실패폐쇄

### 자동 검증

- 수정 전 신규 대상 계측시험: 보안 거부 실패폐쇄 경계 부재로 실패
- 수정 뒤 대상 계측시험: 합성 거부 예외가 빠져나오지 않고 상태 `LOCKED`,
  이유 `TLS_ERROR` 저장 확인
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 56개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 84개, 실패·오류·건너뜀 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 75개, 실패·오류·건너뜀 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc25-release.apk`
  - 크기: 3,093,100 bytes
  - SHA-256:
    `5C87CAB5A9A7F0D26E3F133AFA63AF8834E61E138CF81ADE2B8EFE4A8266F7CD`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc24`/code 41
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC25만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc25`/code 42
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC25 보관본 일치
- 설치된 Web POC와 RC25 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 시각 이후 AndroidRuntime 오류 일치 항목: 0

### 미검증

- 실제 A에서 TLS 취소 또는 Safe Browsing 복귀 콜백 오류를 주입하는 고의
  실패주입
- 실제 QR→Web→QR 왕복과 관리자 PIN·사이트·카메라·PDF 공유·실물 인쇄
  회귀
---

## Web POC RC26 프록시 초기화 오류 실패폐쇄 — 2026-07-26

### 재현한 초기화 중단

Web POC는 프로세스 시작 때 loopback CONNECT 프록시를 시작하고 WebView의
process-wide proxy override를 설정한다. 기존 경로는 WebView 기능 지원 확인
예외가 초기화 밖으로 전파될 수 있었고, 프록시 시작 뒤
`ProxyConfig.Builder` 또는 override 적용이 실패하면 부분 시작된 프록시를
남기거나 시작 완료 콜백을 끝내지 못할 수 있었다.

- 실제 A, 공개 사이트와 자격정보를 사용하지 않음
- Android 의존성을 분리한 coordinator 시험을 먼저 추가
- 수정 전 신규 대상 JVM 시험은 coordinator와 platform 경계가 없어 컴파일
  실패

### 변경

- 구현·회귀시험 커밋: `055d0f8`
- Web POC `0.4.0-rc26`/code 43과 릴리스 운영 경로 준비 커밋: `bf84e89`
- 기능 지원 확인, loopback 시작과 proxy override 적용을 하나의 직렬 상태
  기계에서 수행
- 동기 런타임 예외를 `FAILED`로 종결하고 부분 시작된 프록시를 닫음
- 프록시 닫기 자체가 실패해도 실패 결과 전달을 보장
- 준비 중 중복 요청은 큐에 모으고, 완료 뒤 요청은 저장된 종결 결과를 즉시
  전달하며 늦은 준비 완료 콜백은 무시

### 자동 검증

- 대상 coordinator JVM 시험 5개: 지원 확인 예외, override 적용 예외와 부분
  프록시 정리, 미지원, 비동기 성공·중복 요청, 정리 예외를 검증해 모두 통과
- 전체 자동시험 첫 시도는 앞서 시간 제한된 중복 Gradle 프로세스가 계측 결과
  파일을 점유해 실패; 해당 저장소의 중복 프로세스만 종료한 뒤 재실행 통과
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 56개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 89개, 실패·오류 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 80개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc26-release.apk`
  - 크기: 3,095,616 bytes
  - SHA-256:
    `32CFD09F788385A3EF0E78AEB15C44DBC5168CD1A3E1AC2AE277F33E97AFF2FC`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc25`/code 42
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC26만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc26`/code 43
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC26 보관본 일치
- 설치된 Web POC와 RC26 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 시각 이후 AndroidRuntime 오류 일치 항목: 0

### 미검증

- 실제 A에서 WebView 기능 지원 확인·프록시 시작·override 적용 오류를
  고의 주입하는 실패시험
- 실제 QR→Web→QR 왕복과 관리자 PIN·사이트·카메라·PDF 공유·실물 인쇄
  회귀

---

## Web POC RC27 프록시 초기화 완료 timeout — 2026-07-26

### 재현한 무기한 초기화 대기

RC26은 프록시 초기화 API가 동기 예외를 내는 경로를 실패폐쇄했지만,
`ProxyController.setProxyOverride`가 요청을 접수한 뒤 완료 콜백을 전달하지
않으면 coordinator가 계속 `CONFIGURING`에 머물렀다. 이 상태에서는
`MainActivity`가 UI 초기화를 시작하지 못하고 부분 시작된 loopback 프록시와
대기 콜백도 남는다.

- 실제 A, 공개 사이트와 자격정보를 사용하지 않음
- 합성 platform에 시간 제한 계약을 먼저 추가
- 수정 전 신규 대상 JVM 시험은 timeout 손잡이와 예약 계약이 없어 컴파일
  실패

### 변경

- 구현·회귀시험 커밋: `32271c1`
- Web POC `0.4.0-rc27`/code 44와 릴리스 운영 경로 준비 커밋: `3ae6697`
- 프록시 시작 뒤 10초 watchdog을 예약하고 override 성공 때 즉시 취소
- watchdog 예약이 거부되거나 완료 콜백이 10초 안에 오지 않으면 `FAILED`로
  종결하고 부분 프록시를 닫음
- 시간 초과 뒤 늦은 ready 콜백은 이미 확정된 실패 결과를 바꾸지 못함

### 자동 검증

- 대상 coordinator JVM 시험 7개: 기존 5개에 시간 초과 정리·늦은 ready
  무시와 watchdog 예약 실패 정리를 추가해 모두 통과
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 56개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 91개, 실패·오류 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 82개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·signer 1·두 앱 signer 일치·Debug signer 거부와
  zipalign: 통과
- build APK와 저장 artifact 쌍의 이중 검증: 통과

### 릴리스 APK

- Kiosk: `artifacts/matholic-kiosk-0.6.0-rc26-release.apk`
  - 기존 payload 검증본 보존
  - 크기: 34,974,012 bytes
  - SHA-256:
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc27-release.apk`
  - 크기: 3,097,596 bytes
  - SHA-256:
    `58958895AB1DDCEF548252B9F0F4F1E5E4ACBA357D56B1F61CB6CF98234F7D4B`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc26`/code 43
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC27만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc27`/code 44
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC27 보관본 일치
- 설치된 Web POC와 RC27 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 시각 이후 AndroidRuntime 오류 일치 항목: 0

### 미검증

- 실제 A에서 프록시 override 완료 콜백을 고의로 유실시키는 실패시험
- 실제 QR→Web→QR 왕복과 관리자 PIN·사이트·카메라·PDF 공유·실물 인쇄
  회귀
