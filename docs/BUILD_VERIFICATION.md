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
