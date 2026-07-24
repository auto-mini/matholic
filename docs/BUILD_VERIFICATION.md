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
- 최종 상태: `QR_READY`, 전면 카메라, `LOCKED`

### 판정과 남은 항목

release RC02의 정의된 자동 검증, 핵심 정상 왕복, 비정상 Web 세션 자체 복구와 재부팅 복구는 **PASS**다. 실제 프린터 1장 출력·재인식, 1~2시간 연속 운전, USB 디버깅 제거와 ADB 없는 물리 버튼 최종 실기는 수행하지 않았다.
