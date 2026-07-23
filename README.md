# 매쓰홀릭 채점 키오스크

학생 개인계정의 로그인·학생 확인·로그아웃을 보조하는 Android 앱의 단계별 검증 저장소다. Gate 0은 완료됐고 Android 앱 Gate 1 실기 조사는 최종 FAIL이다. 공식 웹 경로의 Web Gate 1과 Web POC 구현 승인은 완료됐다. Web POC의 비자격정보 검증, A의 실제 시험계정 정상 cycle 20/20과 필수 실패주입이 통과해 정의된 POC 범위의 Web Gate 2는 PASS다. Gate 3의 메모리 전용 2계정 A↔B 100회 runner가 구현돼 있으며 0.2.2·0.3.2·최종 0.3.3 실제 시험은 각각 100/100(A·B 각 50회)으로 G301을 통과했다. 실제 표시명 교차 G302, 과거 비밀번호 G303, 실제 session 기반 G304·G305, Wi-Fi 단절 G306, 화면 off/on·background/foreground G307과 완료 뒤 앱·기기 재시작 G308도 통과했다. 최신 0.3.3은 로그인 거부 시 Gate 3 실패 결과보다 세션을 먼저 지우던 결함을 수정했고 전체 빌드 150/150·A 계측 27/27·G303~G305 실기 재검증과 최종 실제 A↔B 100/100 정상 회귀를 통과했다. 따라서 정의된 Web Gate 3 범위는 PASS다. 실제 학생 DB, QR, Device Owner와 생산 자동화는 아직 범위가 아니다.

## 현재 Gate

- `probe`: 확인된 매쓰홀릭 패키지의 접근성 트리를 민감정보 없이 조사한다.
- `poc`: Gate 1 FAIL로 기능이 잠긴 안내 앱이다. 승인 상수는 `false`다.
- `webpoc`: 공식 웹에서 단일 시험계정 Gate 2와 두 시험계정 교차 Gate 3를 검증하는 별도 POC다.
- 실제 학생 DB, QR, 외부 알림, Device Owner, 생산 자동화는 구현하지 않는다.

## 확인된 대상

- 기기: Samsung SM-P610, Android 13, One UI 5.1.1
- 대체 기기 확인: Samsung SM-P613, Android 14, One UI 6.1에서도 기억하기 상태 판독 FAIL 재현
- 매쓰홀릭 package: `com.matholic.mathapp`
- 매쓰홀릭 versionName/versionCode: `7.5.1` / `770961`
- 현재 가로 논리 화면: 2000×1200, 앱 영역 2000×1128
- density/font scale: 240 dpi / 1.1

이 값은 2026-07-21 실기 ADB 결과이며 selector로 좌표를 사용한다는 의미가 아니다. B의 단축 재검증 근거는 Gate 1 최종 보고서에 포함했다.

## 고정 도구 버전

- JDK 17
- Android Gradle Plugin 9.3.0
- Gradle 9.6.1
- compileSdk/targetSdk 37
- minSdk 33
- Android Build Tools 37.0.0

## 빌드

PowerShell에서 JDK와 SDK 경로를 설정한 뒤 실행한다.

```powershell
$env:JAVA_HOME='C:\Users\user\AppData\Local\Android\jdks\jdk-17.0.19+10'
$env:ANDROID_HOME='C:\Users\user\AppData\Local\Android\Sdk'
.\gradlew.bat clean :probe:testDebugUnitTest :probe:assembleDebug :poc:testDebugUnitTest :poc:assembleDebug :webpoc:testDebugUnitTest :webpoc:assembleDebug
```

생성물은 OneDrive reparse point 잠금과 한글 classpath 문제를 피하기 위해 기본적으로
`%LOCALAPPDATA%\CodexBuild\matholic-kiosk`에 둔다. 필요하면 `MATHOLIC_BUILD_ROOT`로 바꿀 수 있다.

한글 경로용 ASCII junction과 lint까지 포함한 재현 명령은 다음 하나로 실행할 수 있다.

```powershell
.\scripts\build.ps1
```

Probe 설치:

```powershell
$adb='C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$apk="$env:LOCALAPPDATA\CodexBuild\matholic-kiosk\probe\outputs\apk\debug\probe-debug.apk"
& $adb install -r $apk
```

또는 빌드 후 `.\scripts\install-probe.ps1`을 사용한다. 설치 뒤 접근성 허용은 Android 설정 화면에서 사용자가 직접 수행한다.

Web POC 설치와 실행:

```powershell
.\scripts\install-webpoc.ps1
```

Web POC에는 시험계정만 태블릿 화면에서 입력한다. 입력값은 파일·설정·로그에 저장하지 않는다.

기기 A(`SM-P610`)를 연결한 뒤 전체 빌드→비민감 기준정보→25개 계측시험→재설치→`IDLE` 확인을 한 번에 수행하려면:

```powershell
.\scripts\prepare-a-webpoc.ps1
```

이 명령은 A와 B가 함께 연결돼도 모델로 A를 선택하며, A가 없거나 잘못된 모델이면 설치 전에 중단한다. 상세 절차는 [docs/DEVICE_A_WEBPOC_HANDOFF.md](docs/DEVICE_A_WEBPOC_HANDOFF.md)에 기록했다.

B 기기 가상 DOM·복구 계측시험 후 Web POC를 다시 설치하려면:

```powershell
.\scripts\test-webpoc-device.ps1
```

## 보안 원칙

- 자격정보, 실제 학생 이름, QR 원문을 파일·Git·로그에 저장하지 않는다.
- Probe는 인터넷 권한이 없고 `com.matholic.mathapp` 외 앱의 노드를 처리하지 않는다.
- 원문 노드 덤프를 만들지 않는다. 민감 가능 텍스트는 메모리에서 즉시 마스킹한 뒤 redacted report만 앱 내부에 저장한다.
- 좌표, bounds, OCR, 이미지 템플릿은 자동 동작 selector로 사용하지 않는다.
- Android 접근성 Gate 1은 FAIL 상태로 유지하며 기존 `poc`의 잠금을 해제하지 않는다. 승인된 `webpoc`만 공식 웹 DOM 의미 구조를 사용한다.

세부 절차는 [docs/ACCESSIBILITY_PROBE.md](docs/ACCESSIBILITY_PROBE.md)와 [docs/DEVICE_SETUP.md](docs/DEVICE_SETUP.md)를 따른다.
실행된 테스트와 APK 검사는 [docs/BUILD_VERIFICATION.md](docs/BUILD_VERIFICATION.md)에 기록했다.
최종 실기 증거와 판정은 [docs/GATE1_REPORT.md](docs/GATE1_REPORT.md)에 기록했다.
공식 웹 대안의 실기 결과와 필수 안전조건은 [docs/WEB_GATE1_REPORT.md](docs/WEB_GATE1_REPORT.md)에 기록했다.
Web POC 설계와 시험 항목은 [docs/WEB_POC_DESIGN.md](docs/WEB_POC_DESIGN.md), [docs/WEB_POC_TEST_PLAN.md](docs/WEB_POC_TEST_PLAN.md)에 기록했다.
Web POC의 실제 빌드·B 기기 계측·A 이관 준비·남은 실기는 [docs/WEB_POC_VERIFICATION.md](docs/WEB_POC_VERIFICATION.md)에 기록했다.
Gate 3의 2계정 교차 절차와 준비 검증은 [docs/WEB_GATE3_TEST_PLAN.md](docs/WEB_GATE3_TEST_PLAN.md), [docs/WEB_GATE3_VERIFICATION.md](docs/WEB_GATE3_VERIFICATION.md)에 기록했다.
