# 매쓰홀릭 채점 키오스크

학생 개인계정의 로그인·학생 확인·로그아웃을 보조하는 Android 앱의 단계별 검증 저장소다. Android 앱 Gate 1 실기 조사는 최종 FAIL이고, 공식 웹 경로의 Web Gate 2·3과 QR 운영 Gate 4 alpha는 PASS다. Gate 5에는 Device Owner, 전용 HOME, 두 앱 allowlist와 Lock Task 잠금이 구현됐다. A 기기(SM-P610)는 release signer 전용 Device Owner 기기다. RC02에서 QR→Web 문제 화면→로그아웃 자동 복귀, 비정상 Web 세션의 관리자 자체 복구, 잠금과 재부팅 복구, 120분 연속 운전, 실제 프린터 출력과 종이 QR 왕복을 통과했다. 현재는 기존 앱 데이터를 지우지 않고 Kiosk RC30과 Web POC RC32를 설치했으며 자동검증과 설치 후 보존 경계 검사를 통과했다. 2026-07-27 실물 회귀에서 기존 학생·반 보존, 반 소속 QR 차단, 관리자 뒤로가기 유지, 자동 학습지 진입, 두 가로 방향 회전과 학생 상단 메뉴 차단은 통과했다. RC30/RC32는 후속 실물 회귀에서 남은 Web 동영상·필기·풀이 업로드, 전체답안 이동, 25문항 결과 수집과 QR 안내 문제를 보강한다. 별도 release signer는 Android 폰과 별도 USB에 암호화 키 복구본을 보관한다.

## 현재 Gate

- `probe`: 확인된 매쓰홀릭 패키지의 접근성 트리를 민감정보 없이 조사한다.
- `poc`: Gate 1 FAIL로 기능이 잠긴 안내 앱이다. 승인 상수는 `false`다.
- `webpoc`: 공식 웹에서 단일 시험계정 Gate 2와 두 시험계정 교차 Gate 3를 검증하는 별도 POC다.
- `kiosk`: Gate 4 기능과 Gate 5 Device Owner·전용 HOME·Lock Task를 제공한다. 현재 소스와 A 설치본은 `0.6.0-rc30`이며 관리자 화면에서 시스템 뒤로가기를 소비해 현재 입력과 화면을 유지한다. 카메라 영상은 표시·저장하지 않고 매쓰홀릭 QR의 경계 좌표만 이용해 중앙·거리·이동 방향을 안내하며, 화면의 목표 사각형이 카메라 영상이 아니라는 점을 명시한다. 좌우 허용 범위는 상하보다 넓게 조정했다. 관리자 비동기 단일 실행, 목록 조회 실패 복구, 반 소속 안전성, Web 결과 저장과 복구, QR 재발급 확인, 다중 반·보강 학생 원자성, secure session handle, QR PDF 공유와 65×90mm·30×30mm 좌표 보정을 포함한다.
- `webpoc`: 화면 꺼짐을 막고 Lock Task allowlist 안에서 실행된다. 현재 소스와 A 설치본은 `0.4.0-rc32`이다. 학생 화면은 학습지·진단평가 전환만 남기고 Matholic 상단 메뉴, 문제지·오류신고·동영상·답안필기·풀이 업로드를 숨긴다. 주관식 입력은 수식 입력기로 전환하되 분수 입력은 유지하고 `기본` 전환만 막는다. 전체답안 내부 스크롤을 끝으로 이동하고 제출 버튼을 고정한다. 결과 페이지는 풀이·정답을 먼저 차폐한 뒤 내부 스크롤의 지연 생성 문제 카드를 순차 로딩하고, 표시된 문항수와 1번부터 끝까지의 분류가 일치할 때만 오답 목록을 표시한다. 불완전하면 확인된 문항 수와 `RESULT_INCOMPLETE`를 표시해 거짓 결과를 막는다. 브라우저 주소 머리말이 붙는 JavaScript 확인창은 허용된 학생 문서에서만 앱 확인창으로 대체한다. 기존 origin·경로·호출자·프록시·세션 실패폐쇄 경계는 유지한다.
- 외부 알림은 현재 요구사항에서 제외했다. release RC02는 정의된 운영 인수시험을 완료했고 Kiosk RC30/Web POC RC32 조합은 자동검증·보존형 설치까지 완료했다. 최신 Web·QR 수정의 실제 사이트 실기는 재검증 대기다.

RC03부터 학생 마스터 목록과 다중 반 소속, 학생을 보존하는 반 삭제,
65×90mm 세로 카드·30×30mm QR PDF 공유, 카메라 미리보기 비표시,
학습지 자동 진입·진단평가 전환, 문제 입력 확대와 오답 번호 전용 결과 화면이
포함된다. RC03 이력은 [docs/RC03_USABILITY.md](docs/RC03_USABILITY.md)에,
RC04·RC05와 이후 소스 검증은 [docs/BUILD_VERIFICATION.md](docs/BUILD_VERIFICATION.md)에
구분해 기록한다.

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
.\gradlew.bat clean :probe:testDebugUnitTest :probe:assembleDebug :poc:testDebugUnitTest :poc:assembleDebug :webpoc:testDebugUnitTest :webpoc:assembleDebug :kiosk:testDebugUnitTest :kiosk:assembleDebug
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

기존 비-Device Owner 기기의 Gate 4 alpha 덮어쓰기 설치:

```powershell
.\scripts\install-gate4.ps1
```

관리자 PIN과 학생 자격정보는 태블릿 화면에서만 입력한다. 상세 구현 범위와 실제 수동 시험 결과는 [docs/GATE4_IMPLEMENTATION.md](docs/GATE4_IMPLEMENTATION.md)에 기록했다.

공장초기화한 A를 Gate 5 Device Owner로 최초 등록:

```powershell
.\scripts\provision-gate5-device-owner.ps1 -Serial R54TB029FHZ
```

등록 뒤 읽기 전용 정책 확인은 `.\scripts\verify-gate5-device-owner.ps1 -Serial R54TB029FHZ`로 수행한다. 관리자 PIN, 학생 자격정보와 QR 원문은 태블릿 밖으로 내보내지 않는다. 초기화 조건, 잠금 경계, 복구와 실기 결과는 [docs/GATE5_IMPLEMENTATION.md](docs/GATE5_IMPLEMENTATION.md)에 기록했다.

Release RC 빌드:

```powershell
.\scripts\build-release.ps1
```

release 키 복구 확인, 공장초기화·운영 프로비저닝·관리자 Web 세션 복구 절차는 [docs/RELEASE_OPERATIONS.md](docs/RELEASE_OPERATIONS.md)를 따른다.
사용자가 단독으로 수행한 120분 연속 운전과 실제 프린터 시험 절차·결과는 [docs/OPERATOR_ACCEPTANCE_CHECKLIST.md](docs/OPERATOR_ACCEPTANCE_CHECKLIST.md)에 기록했다.

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
Gate 5 전용기기 등록·운영·복구와 실제 잠금 검증은 [docs/GATE5_IMPLEMENTATION.md](docs/GATE5_IMPLEMENTATION.md)에 기록했다.
Release 서명키·복구·RC 빌드·운영 전환 절차는 [docs/RELEASE_OPERATIONS.md](docs/RELEASE_OPERATIONS.md)에 기록했다.
