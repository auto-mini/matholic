# 매쓰홀릭 채점 키오스크

학생 개인계정의 로그인·학생 확인·로그아웃을 보조하는 Android 앱의 단계별 검증 저장소다. Android 앱 Gate 1 실기 조사는 최종 FAIL이고, 공식 웹 경로의 Web Gate 2·3과 QR 운영 Gate 4 alpha는 PASS다. Gate 5에는 Device Owner, 전용 HOME, 두 앱 allowlist와 Lock Task 잠금이 구현됐다. A 기기(SM-P610)는 release signer 전용 Device Owner 기기다. RC02에서 QR→Web 문제 화면→로그아웃 자동 복귀, 비정상 Web 세션의 관리자 자체 복구, 잠금과 재부팅 복구, 120분 연속 운전, 실제 프린터 출력과 종이 QR 왕복을 통과했다. 현재 A 기기에는 기존 앱 데이터를 보존한 Kiosk RC68과 Web POC RC130이 설치되어 있다. 화면에 보이는 앱명·관리자 헤더·시험 모드명·보안 상태는 `채점 관리`와 `학습` 중심의 중립 문구로 바꾸고 QR 모양 앱 아이콘도 일반 문서 확인 아이콘으로 교체했다. 학습 화면에는 112×96px 문제 이동 버튼, 읽기 전용 현재 문제번호 배지, 답안 현황 지도, `모름` 안내와 답안 현황 바로 아래에 열리는 수식 키패드를 제공한다. QR 대기부터 학습지·진단평가 목록, 객관식·주관식 문제, 전체답안 확인과 결과까지 학생이 조작하는 각 화면에는 해당 화면용 큰 도움말을 여는 `?` 버튼을 제공한다. ACTIVE 학생 세션 동안 앱 창 밝기를 80%로 적용하고 로그아웃·복구·종료 때 이전 밝기로 복원한다. 별도 release signer는 Android 폰과 별도 USB에 암호화 키 복구본을 보관한다.

## 현재 Gate

- `probe`: 확인된 매쓰홀릭 패키지의 접근성 트리를 민감정보 없이 조사한다.
- `poc`: Gate 1 FAIL로 기능이 잠긴 안내 앱이다. 승인 상수는 `false`다.
- `webpoc`: 공식 웹에서 단일 시험계정 Gate 2와 두 시험계정 교차 Gate 3를 검증하는 별도 POC다.
- `kiosk`: Gate 4 기능과 Gate 5 Device Owner·전용 HOME·Lock Task를 제공한다. 현재 소스는 `0.6.0-rc69`이다. 학생 CSV 가져오기, 신규·변경 QR 카드 PDF 생성, 카드 상태·이력, 관리자 30초 실행취소, 운영 자가진단·원버튼 복구, PC 상태·암호화 전송과 수식 키패드 배치 설정을 제공한다. Android 직접 인쇄는 제거됐고 기존 반·학생·QR 데이터와 secure session 경계는 유지한다.
- `webpoc`: 화면 꺼짐을 막고 Lock Task allowlist 안에서 실행된다. 현재 소스는 `0.4.0-rc131`이다. 네트워크 단절 안전 대기, 10분 무입력 경고, 미입력 문항 순회, 수식 입력 실행취소·다시실행, 삭제 길게 누르기, 답 입력칸 아래쪽 수식 키패드와 가변 터치영역을 제공한다. 사용자에게 노출되지 않는 제한된 진단 로그에는 중단 단계·느린 처리·자동 재시도·결과 판독 실패의 비식별 구조 정보만 남긴다. 기존 문제 번호·답안 현황·MathQuill 복구·제출 연타 차단·오답 추출과 실패폐쇄 경계는 유지한다.
- 외부 알림은 현재 요구사항에서 제외했다. release RC02는 정의된 운영 인수시험을 완료했다. 현재 보관 검증 묶음과 A 기기는 Kiosk RC68/Web POC RC130/PC 수신기 0.1.5를 기준으로 한다. RC61 QR 카드의 실제 출력·절단·카메라 인식까지 통과했다.

RC03부터 학생 마스터 목록과 다중 반 소속, 학생을 보존하는 반 삭제,
65×90mm 카드 케이스용 55×80mm 삽입 종이·40×40mm QR PDF 공유, 카메라 미리보기 비표시,
학습지 자동 진입·진단평가 전환, 문제 입력 확대와 오답 번호 전용 결과 화면이
포함된다. RC03 이력은 [docs/RC03_USABILITY.md](docs/RC03_USABILITY.md)에,
RC04·RC05와 이후 소스 검증은 [docs/BUILD_VERIFICATION.md](docs/BUILD_VERIFICATION.md)에
구분해 기록한다.

사용자가 채택·배제한 후속 편의성 개선안과 재추천 금지 범위는
[docs/PRODUCT_DECISIONS.md](docs/PRODUCT_DECISIONS.md)를 기준으로 한다.

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

승인된 USB ADB로 관리자 원격 점검을 시작하고 현재 화면을 PC 임시 경로에
캡처하려면 다음을 사용한다. 별도 네트워크 포트를 열지 않으며 기본 60분,
최대 120분 뒤 자동으로 `FLAG_SECURE`가 복원된다.

```powershell
.\scripts\remote-tablet.ps1 -Action Start -Minutes 60
.\scripts\remote-tablet.ps1 -Action Capture
.\scripts\remote-tablet.ps1 -Action Stop
```

관리자 화면에서도 30분 원격 점검을 시작·종료할 수 있다. 점검 중에는 화면
오른쪽 위에 `원격 점검 중` 배지가 보이며 관리자 PIN이나 비밀번호를 입력할
때는 사용하지 않는다.
승인 ADB의 실기 회귀에는 `TestQr` 동작으로 카메라가 이미 계산했을 때와 같은
32바이트 QR 해시를 한 번 전달할 수 있다. 원격 점검이 활성이고 현재 수업이
정확히 `QR_READY`일 때만 처리하며 QR 원문·계정정보는 전달하거나 기록하지 않는다.

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
