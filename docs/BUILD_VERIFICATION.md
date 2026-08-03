# 빌드·보안 검증 기록

## Kiosk RC63 요일 반 빠른 선택 레이아웃 고정 — 2026-08-03

### 실기 재현·원인·교정

- RC62를 Samsung SM-P610 `R54TB029FHZ`에서 12개 빠른 선택 버튼 모두 직접
  눌러 확인했다. 선택 반에 따라 빠른 선택 영역 높이가 172px에서 178px로
  6px 늘었고, 반 학생 구성·삭제 등 아래 컨트롤도 6px 이동했다. 단순한 눌림
  착시가 아니라 선택 완료 뒤 접근성 경계 좌표가 달라지는 실제 레이아웃 결함이다.
- 선택 버튼에 적용한 굵은 Typeface가 `GridLayout` 행 측정값을 바꾸는 것이
  원인이었다. 선택 표시는 글꼴 굵기 변경 없이 alpha `1.0`, 비선택은 alpha
  `0.72`만 사용하도록 고정했다. Android 기본 버튼의 눌림 elevation 이동도
  없애도록 `stateListAnimator`를 비활성화했다. 반 선택·명단 조회·수업 상태
  변경 로직은 바꾸지 않았다.
- 계측 회귀시험은 `월1 → 월2` 선택 전후 12개 버튼 경계가 동일하고, 모든
  버튼이 보통 굵기·`stateListAnimator == null`을 유지하며 선택 alpha만
  바뀌는지 확인한다.

### 자동·릴리스 검증

- `:kiosk:compileDebugAndroidTestKotlin`: PASS.
- `scripts/build-release.ps1`: **158 tasks PASS**. Kiosk/Web unit, release lint,
  signed assemble, version·non-debuggable·동일 signer 검증을 포함한다.
- Kiosk `0.6.0-rc63`/code 68, 35,225,680 bytes:
  `E889B8593B104E566B8A6F3834ADE4186FB9917E09F378B9DD8CCBC6F5676CC3`.
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.

### 실제 태블릿 12개 전수 검증·최종 상태

- RC63을 같은 기기에 `adb install -r`로 보존형 설치했다. Kiosk UID 10288,
  firstInstallTime `2026-07-24 12:52:28`, dataDir와 Device Owner를 유지했다.
- `월1`, `월2`, `화1`, `화2`, `수1`, `수2`, `목1`, `목2`, `금1`, `금2`,
  `토1`, `토2`를 실제 기기에서 하나씩 눌렀다. 매 탭 직전에 새 접근성 경계를
  읽어 해당 버튼 중심을 계산했으며, 12건 모두 선택 반이 정확히 바뀌었다.
  빠른 선택 영역 `[64,817][1195,981]`, 12개 버튼과 화면에 보이는 주변
  컨트롤 경계는 전 건 동일했다. 검증 뒤 선택 반은 시작 상태 `목2`로 복원했다.
- 앱의 `FLAG_SECURE` 때문에 ADB 화면 캡처는 0-byte로 차단됐다. 보안 설정을
  우회하지 않고 실기 탭과 접근성 경계 좌표로 검증했으므로 화면 캡처 증적은 없다.
- 검증 뒤 재부팅해 Kiosk RC63/code 68, 기본 HOME·전면 `MainActivity`,
  `관리자 인증`, Device Owner와 Lock Task `LOCKED`를 다시 확인했다.

## Kiosk RC62 요일 반 빠른 선택 표시 복원 — 2026-08-03

### 결함·원인·교정

- `요일 반 빠른 선택`에서 한 번 선택해 굵어진 버튼이 다른 반을 선택한 뒤에도
  굵게 남았다. 선택된 반 하나를 굵기와 alpha `1.0`으로 표시하고 나머지를
  alpha `0.72`로 낮추는 의도는 맞지만, 비선택 버튼의 글꼴 복원이 실패했다.
- 원인은 `setTypeface(button.typeface, NORMAL)`이 이미 굵어진 현재 Typeface를
  다시 기준으로 사용한 것이다. `NORMAL`을 적용해도 굵은 Typeface 객체 자체가
  남아 선택 이력이 시각적으로 누적됐다.
- 버튼 생성 시 원래 Typeface를 반별로 보존한다. 선택 버튼은 그 원본에서
  `BOLD`를 만들고, 비선택 버튼은 원본 Typeface 객체로 직접 복원한다. 실제 반
  선택·명단 조회·수업 잠금과 활성화 조건은 바꾸지 않았다.

### 검증·산출물

- 새 Android 회귀시험은 `월1 → 월2`를 순서대로 선택하고 이전 `월1`이
  `isBold == false`, alpha `0.72`로 돌아오며 `월2`만 `isBold == true`, alpha
  `1.0`인지 확인한다. 교정 전 이 시험은 이전 버튼의 `assertFalse(isBold)`에서
  실패했고, 교정 후 단독 실행은 PASS했다.
- Android 13 `matholic_rc03_api33` Kiosk 전체 계측시험:
  **62 tests, 0 failures, 0 errors, 0 skipped**.
- `scripts/build-release.ps1`: **158 tasks PASS**. Kiosk/Web unit, release lint,
  signed assemble, version·non-debuggable·동일 signer 검증을 포함한다. Kiosk
  JVM 단위시험은 JUnit XML 합계 **84 tests, 0 failures, 0 errors, 0 skipped**다.
- Kiosk `0.6.0-rc62`/code 67, 35,225,680 bytes:
  `70341C86E59CEDD4BF4E69986742B69455B47D4C59E0947D09DB3F1056FC277F`.
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.
- Samsung SM-P610 `R54TB029FHZ`에 `adb install -r`로 보존형 설치했다.
  Kiosk UID 10288, firstInstallTime `2026-07-24 12:52:28`, dataDir와 Device Owner를
  유지했다. 설치 뒤 RC62/code 67과 기본 HOME·전면 `MainActivity`를 확인했다.
  이번 변경은 관리자 화면의 선택 표시만 바꾸며 데이터·세션 형식 변경은 없다.

## PR #1 clean clone 병합 전 감사 — 2026-08-03

### clean clone 재현 검증

- 원본 workspace의 ignored 파일·기존 artifact·Gradle source cache를 사용하지
  않도록 `%LOCALAPPDATA%\CodexAudits` 아래에 PR branch를 새로 clone했다.
- 기존 스크립트는 전역 ASCII junction 하나를 원본 workspace에 고정해 다른
  clean clone 실행을 거부했다. ASCII 경로에서는 clone 자체를 사용하고 한글
  경로에서만 기존 junction을 쓰도록 `build.ps1`, `build-release.ps1`과 Kiosk/Web
  emulator 시험 스크립트를 교정했다.
- clean clone `scripts/build-release.ps1`: **158 tasks PASS**. unit, release lint,
  signed assemble, version·non-debuggable·동일 signer 검증을 포함한다.
- PC receiver: `python -m pytest pc_receiver/tests -q` **17 passed**.
- Android 13 `matholic_rc03_api33` Web 계측시험은 JUnit XML 기준
  **110 tests, 0 failures, 0 errors, 0 skipped**다.
- Android 13 Kiosk 전체 계측시험은 JUnit XML 기준
  **61 tests, 0 failures, 0 errors, 0 skipped**다. 최초 전체 실행에서 발견한
  obsolete private method reflection 2건과 main-thread Room 조회 1건을 현재
  session-ID·thread 계약에 맞춰 보정한 뒤 세 단독 시험과 전체 61개를 다시
  통과했다.
- clean clone에서 새로 생성한 Kiosk RC61과 Web RC121 APK는 설치 검증본과
  full-file SHA-256이 다르다. 현재 Git commit을 담는
  `META-INF/version-control-info.textproto`와 이에 대한 APK 서명이 달라지는 정상
  결과이며, 해당 entry를 제외한 모든 APK entry 이름·길이·내용 SHA-256으로
  계산한 payload fingerprint는 Kiosk와 Web 모두 설치 검증본과 일치했다.
- release signer SHA-256은
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`로
  동일하다.

### 민감정보·생성물 감사

- Gitleaks 8.30.1로 `origin/master..HEAD`의 409개 commit을 검사했고 finding은
  **0건**이다.
- 최종 tree의 generic API key 후보 1건은 `master`에도 존재하는
  SharedPreferences 키 이름 `gate3_duration_ms`로, credential이 아닌 오탐임을
  source와 기준선으로 확인했다.
- PR 이력과 최종 tracked tree에 `diagnostics/`, `output/`, `tmp/`, `artifacts/`,
  APK, PDF, log, keystore, DPAPI credential과 일반적인 고신뢰 token 패턴은
  **0건**이다. 로컬 민감·재생성 산출물은 `.gitignore`로 제외했고 삭제하거나
  업로드하지 않았다.

관련 교정 commit은 `951bc5c`, 시험 계약 보정 commit은 `6e4ba90`이다.
두 변경은 build/test infrastructure에만 한정되므로 실제 태블릿 RC61/RC121의
재설치는 필요하지 않다.

## Web RC121 답안 현황판 터치 관통 교정 — 2026-08-03

### 결함·원인·교정

- 답안 현황판이 열린 상태에서 현황판 밖의 답안 영역을 누르면 `pointerdown`
  단계에서 현황판이 먼저 사라졌다. 같은 손가락의 뒤이은 `pointerup`/`click`이
  새로 노출된 객관식 답이나 `모름` 버튼에 전달되어 의도하지 않은 답안 변경이
  가능했다.
- 사용자의 답안을 조용히 바꿀 수 있는 핵심 채점 흐름 결함이므로 **P2**로
  판정했다.
- 바깥 `pointerdown`부터 해당 포인터의 `pointerup`과 호환 `click`까지 하나의
  닫기 제스처로 소유·소비한다. 현황판은 손가락을 누르는 동안 유지되고 손을
  뗄 때 닫힌다. 그 제스처는 아래 답안 요소에 전달되지 않으며, 다음 독립
  터치부터만 정상 입력된다. `pointercancel`과 키보드 `Escape` 종료도 별도로
  처리한다.

### 자동·릴리스 검증

- 새 Android DOM 회귀시험은 바깥 요소에 `pointerdown`/`pointerup`/`click`을
  연속 전달해 첫 제스처의 세 이벤트가 모두 0회임을 확인하고, 다음 독립
  제스처는 각각 1회 도달함을 확인한다.
- Android 13 `matholic_rc03_api33` 전체 Web 계측시험은 JUnit XML 기준
  **110 tests, 0 failures, 0 errors, 0 skipped**로 PASS했다.
- 전체 실행에서 발견한 제품 외 시험 결함도 함께 바로잡았다. DOM storage를
  실제 설정과 맞추고, `requestAnimationFrame` 반영을 기다리며, WebView 포함
  layout inflate를 main thread에서 실행했다. 수식 키패드 검사는 실제 태블릿
  크기에 맞는 1200dp viewport와 완전한 화살표 좌표를 사용하고, 현황판 전환
  종료는 고정 sleep 대신 제품의 4초 deadline을 포함하는 5초 bounded polling으로
  확인한다.
- `scripts/build-release.ps1`: **158 tasks PASS**. Kiosk/Web unit, release lint,
  signed assemble, version·non-debuggable·동일 signer 검증을 포함한다.
- Web `0.4.0-rc121`/code 138, 3,329,878 bytes:
  `66E953D4395D61D7ACC443249B44CAF7F09B449978C50E3205DDF557F42AF4C7`
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.

### 실제 태블릿 검증·최종 상태

- Samsung SM-P610 `R54TB029FHZ`에 `adb install -r`로 보존형 설치했다.
  Web UID 10293, firstInstallTime `2026-07-28 13:12:16`, Device Owner와 앱
  데이터가 유지됐다.
- 활성 Web session 중 업데이트로 Web 프로세스가 종료됐을 때 Kiosk는
  `WEB_SESSION_FAILED`로 실패 폐쇄했다. 관리자 PIN과 기존 원버튼 복구로
  학생·반·QR을 삭제하지 않고 안전 종료한 뒤 새 session의 공식 Web 로그인에
  성공했다.
- 객관식 답 2번과 `모름`이 각각 현황판 아래에 있는 위치에서 같은 좌표를 길게
  눌렀다. 두 경우 모두 누르는 동안 현황판이 유지되고 손을 뗄 때 닫혔으며,
  답안 현황은 **0/10**으로 유지됐다. 시험 답안은 입력·제출하지 않았다.
- 최종 확인은 Web RC121/code 138, Device Owner 유지, Kiosk `QR_READY`,
  LockTask `LOCKED`, 원격 지원 `INACTIVE`다. 실기 확인용 임시 화면 캡처 4개는
  검증 뒤 삭제했다.

교정 commit은 `de93987`, 버전·운영 정렬은 `0ce5b9c`, 시험 하니스 보정은
`b7e1119`다.

## RC61 QR 카드 절단용 배치 교정 — 2026-08-03

### 제공 PDF 독립 비교

- `20260803-105709_3419dc14a19a786e_신규 변경 학생 QR.pdf`는 A4 1페이지에
  55×80mm 카드 2장을 서로 붙여 배치했다. QR은 RC60의 40×40mm였지만 카드
  사이 절단 여백이 없고 전체 3×3 절단 격자를 사용하지 않았다.
- `매쓰홀릭_QR카드_통합_절단용_20260729.pdf`는 A4 2페이지이며 첫 페이지
  9장, 둘째 페이지 5장을 왼쪽 위부터 배치했다. 각 카드는 55×80mm이고
  가로·세로 5mm 간격, 좌우 17.5mm, 상하 23.5mm, 68% 회색 0.25pt 절단선을
  사용했다.
- 두 파일은 Poppler로 모든 페이지를 렌더링하고 pdfplumber의 page object
  좌표로 카드 사각형·간격·페이지 크기를 독립 확인했다. 실제 QR 원문은
  추출하거나 기록하지 않았다.

### 교정

- 여러 장 PDF를 A4 무여백 좌표계에서 3×3, 카드 55×80mm, 카드 간격 5mm로
  배치한다. 마지막 페이지는 기준 파일처럼 필요한 카드만 왼쪽 위부터 채운다.
- 절단선은 기준 파일의 68% 회색·0.25pt를 적용했다.
- RC60에서 확대한 40×40mm QR과 QR 아래 학생 이름 위치는 유지했다.
- 단건 PDF는 기존 A4 중앙 카드 배치를 유지하고, 신규·변경 카드 묶음과 반
  일괄 PDF에 절단용 격자를 적용한다.

### 검증·설치

- `:kiosk:testDebugUnitTest :kiosk:compileDebugAndroidTestKotlin`: PASS.
- Android 13 `matholic_rc03_api33`에서
  `QrPrintDocumentAdapterInstrumentedTest` **12 tests PASS**. 210×297mm A4,
  55×80mm 카드, 5mm 간격, 17.5/23.5mm 시작점, 3×3 페이지와 마지막 페이지
  잔여 카드만 렌더하는 동작을 검증했다.
- 실제 Android `PrintedPdfDocument`로 비로그인 합성 카드 10장의 2페이지
  미리보기를 생성했다. Poppler 150/300dpi 렌더에서 1페이지 9장, 2페이지
  1장의 절단선·QR·이름에 잘림이나 겹침이 없었다. ZXing 3.5.4로 렌더 PNG의
  합성 QR 10개를 모두 정확히 재판독했다.
- 제공된 실제 2장 PDF도 QR을 재발급하거나 해석하지 않고 기존 55×80mm 카드
  영역을 A4의 첫 행 1·2열에 5mm 간격으로 재배치했다. 입력·교정본의 두 QR
  image pixmap SHA-256이 각각 일치하고, Poppler 300dpi 렌더에서 잘림·겹침이
  없었다. 실제 로그인 QR을 포함하므로 이 교정본은 Git에 추가하지 않았다.
- 실제 2장 절단용 교정본 640,275 bytes:
  `A26D94ECBA56FE30A91C50D71E1319E8148EDFCCE43E49280928DDF9F1266757`.
- 합성 미리보기 368,858 bytes:
  `BBF31CB72129AA724C147288E144146DA06B16A349C14E90189E519CC3F4C1BC`.
- `scripts/build-release.ps1`: **158 tasks PASS**. unit, release lint, signed
  assemble, version·non-debuggable·동일 signer 검증을 포함한다.
- Kiosk `0.6.0-rc61`/code 66, 35,225,680 bytes:
  `498683E87EAC8F3A520A7A20202A64B09DBBC3D37DBEE21026460F89277E4B4F`
- Samsung SM-P610 `R54TB029FHZ`에 `adb install -r`로 보존형 설치했다.
  Kiosk UID 10288, firstInstallTime `2026-07-24 12:52:28`, Device Owner와 앱
  데이터 영역을 유지했다. 기존 실제 QR은 재발급·무효화하지 않았다.
- 최종 상태는 `ADMIN_IDLE`, LockTask `NONE`, 원격 점검 비활성, Wi-Fi
  validated다.
- 사용자가 RC61 교정본을 실제 프린터로 출력하고 절단한 뒤 태블릿 카메라로
  다시 인식했다. 출력 배치·절단과 모든 실물 QR 인식은 **PASS**였고 의도하지
  않은 잘림·간격 문제나 인식 실패는 없었다.

코드 commit은 `7a32b4e`, 버전·운영 문서 정렬 commit은 `a57ec16`이다.
RC61 QR 확대·절단용 교정에는 남은 현장 검증 항목이 없다.

## RC60 QR 카드 PDF 확대 — 2026-08-03

### 변경

- 65×90mm 카드 케이스와 55×80mm 삽입 종이 크기는 유지했다.
- QR을 30×30mm에서 40×40mm로 확대했다. 가로·세로는 각각 33.3%, 면적은
  77.8% 증가한다.
- QR 상단 위치를 삽입 종이 상단 기준 28mm에서 18mm로 옮겨 QR 하단과 학생
  이름 기준선 사이 10mm 간격을 유지했다. 학생 이름 기준선 68mm와 좌우 이름
  여백은 바꾸지 않았다.
- 단건, 신규·변경 카드 묶음과 반 일괄 PDF가 같은
  `QrPrintCardRenderer`를 사용하므로 세 경로에 동일하게 적용된다.

### 검증

- `:kiosk:testDebugUnitTest :kiosk:compileDebugAndroidTestKotlin`: PASS.
- Android 13 `matholic_rc03_api33`에서
  `QrPrintDocumentAdapterInstrumentedTest` **10 tests PASS**. 55×80mm 카드,
  40×40mm QR, 상단 18mm, 이름 간격과 실제 Android PDF 1페이지 렌더를
  검증했다.
- A4 합성 미리보기를 Poppler 150dpi PNG로 렌더링해 테두리·QR·이름의
  겹침·잘림이 없음을 확인했다. 렌더 PNG의 비로그인용 합성 QR은 ZXing
  3.5.4로 원문을 정확히 재판독했다.
- `scripts/build-release.ps1`: **158 tasks PASS**. unit, release lint, signed
  assemble, version·non-debuggable·동일 signer 검증을 포함한다.
- Kiosk `0.6.0-rc60`/code 65, 35,225,680 bytes:
  `DEC2E07149E3D77EA19F5CBE3970BFD0CFD8FD2E28EB5C0104BF2C910FBB6D11`
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.
- Samsung SM-P610 `R54TB029FHZ`에 `adb install -r`로 보존형 설치했다.
  Kiosk UID 10288, firstInstallTime `2026-07-24 12:52:28`, Device Owner와
  앱 데이터 영역이 유지됐고 기존 반 데이터가 관리자 화면에 표시됐다.
- 최종 상태는 `ADMIN_IDLE`, LockTask `NONE`, 원격 점검 비활성이다.

코드 commit은 `108a54a`, 버전·운영 문서 정렬 commit은 `e0bda43`이다.
실제 프린터 종이 출력과 종이를 태블릿 카메라로 읽는 물리 시험은 수행하지
않았다.

## RC59 반 학생 구성 현장 결함 교정 — 2026-08-03

### 결함·원인·교정

- `토요일2` 반의 학생 구성을 저장하면
  `Inactive or unknown student selected`가 발생해 소속 변경 전체가 차단됐다.
- 원인은 과거 버전에서 비활성화된 학생의 `class_memberships` 행이었다. 관리자
  대화상자는 활성 학생만 표시했지만 초기 선택 집합에는 비활성 학생 ID도
  포함해, 화면에서 해제할 수 없는 ID를 저장 요청에 다시 보냈다.
- 심각도는 **P3 확정**으로 판정했다. 특정 기존 데이터가 있는 반의 핵심 관리자
  작업을 막지만, repository의 활성 학생 검사가 실패 폐쇄했고 잘못된 소속이나
  데이터 손실은 발생하지 않았다.
- 활성 학생 소속만 조회하도록 DAO 경계를 좁히고, 학생 비활성화 transaction에서
  기존 반 소속을 함께 제거했다. 활성 학생만 허용하는 repository 안전 검사는
  제거하지 않았다. 사용자 오류 문구는 한국어로 구체화했다.
- 회귀시험은 비활성화 시 소속 행 제거, 구버전의 잔존 행을 강제로 재현했을 때
  관리자 선택에서 제외, 다음 정상 저장에서 잔존 행 제거를 모두 검증한다.

### 자동·릴리스·실기 검증

- `:kiosk:testDebugUnitTest :kiosk:compileDebugAndroidTestKotlin`: PASS. Kotlin
  증분 캐시 등록 경고 뒤 비증분 컴파일로 자동 전환해 최종 성공했다.
- `scripts/build-release.ps1`: **158 tasks PASS**. unit, release lint, signed
  assemble, version·non-debuggable·동일 signer 검증을 포함한다.
- Kiosk `0.6.0-rc59`/code 64, 35,225,680 bytes:
  `CD389A9BB75D2EAE835EBF22CCB107E1B65A145DE711C66EBBED9E58FE82CC53`
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.
- Samsung SM-P610 `R54TB029FHZ`에 `adb install -r`로 보존형 설치했다.
  Kiosk UID 10288, firstInstallTime `2026-07-24 12:52:28`, Device Owner와 기존
  반·학생 데이터가 유지됐다.
- 실제 문제 반 `토요일2`의 기존 3명 구성을 그대로 저장해 종전 오류가 사라진
  것을 확인했다. 이어 `이하윤`을 추가해 4명 저장, 다시 제외해 원래
  `강기환, 강호영, 테스트` 3명으로 복구했다. 최종 상태는 `ADMIN_IDLE`,
  LockTask `NONE`, Device Owner 유지다.

관련 교정 commit은 `a97d695`, 버전 정렬 commit은 `d1a4a08`이다.

## RC58·RC120·PC 수신기 0.1.5 최종 현장 교정 검증 — 2026-08-03

### 현장에서 추가로 확인·교정한 결함

- Kiosk RC57의 기존 DB 초기화가 Android 13에서
  `PRAGMA secure_delete=ON`의 `execSQL` 호출 때문에 실패했다. PRAGMA를
  SQLite query 경로로 바꾸고, Room `onOpen` 안의 audit DELETE를 repository
  초기화 이후 DAO 단계로 옮겼다. 기존 DB는 삭제·재생성하지 않고 정상 열렸다.
- Samsung SM-P610에서 `startLockTask()`/ `stopLockTask()` 반영이 비동기라
  즉시 판정한 수업 사전점검이 정상 기기를 실패 처리했다. 1.5초 bounded polling과
  사전점검 전 restricted mode 진입·실패 시 해제를 적용했다.
- Web RC119의 loopback CONNECT proxy가 총 8 tunnel에서 즉시 503을 반환해
  공식 로그인 TLS가 반복 실패했다. backlog 64, 동시 tunnel 32로 보정한 뒤 같은
  기기·계정·네트워크에서 로그인에 성공했다. 실패 진단에는 이름·본문·자격정보
  없이 구조 count/flag만 기록한다.
- 네트워크 단절 패널은 실제로 답안 WebView를 완전 불투명하게 가렸으므로 기존
  보고서의 “불투명 차폐가 아닐 수 있다”는 추정은 과장이었다. 다만 Android
  elevation 때문에 종료 버튼 배경과 학생 이름 badge가 패널 위로 남는 실제
  반증을 발견해, 단절 중 종료 버튼·학생 이름·학생 navigation을 숨기고 복구 뒤
  원래 상태만 복원하도록 교정했다.
- 원격 점검 PowerShell이 Android broadcast의 정상 `Activity.RESULT_OK=-1`을
  실패로 보던 판정을 고쳤다.

관련 교정 commit은 `e283f81`, `a11489c`, `fc03216`, `22d8eb4`,
`8c8cb24`, `28f3cfb`이며, 최종 버전 정렬은 `dd0e300`이다.

### 자동·릴리스 검증

- `scripts/build-release.ps1`: **158 tasks PASS**. Kiosk/Web JVM 시험,
  release lint, signed assemble, version·`debuggable=false`·동일 signer
  검증을 포함한다.
- Web `testDebugUnitTest`, `compileDebugAndroidTestKotlin`,
  `assembleDebug`: PASS. 최종 차폐 보강 뒤 unit test와 AndroidTest source
  compile도 다시 PASS했다.
- PC 수신기: `python -m pytest pc_receiver/tests -q` **17 passed**.
- 임시 직접 release 명령 한 회는 APK package 이후 Gradle configuration cache
  저장 오류로 실패 처리됐다. `--no-configuration-cache` 재실행과 최종
  `build-release.ps1`은 모두 PASS했다.
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`.

### 최종 산출물·설치

- Kiosk `0.6.0-rc58`/code 63, 35,225,680 bytes:
  `41D84501B50DB96BD9CFE2249319119EAEF8A5051054AE622BA4A13BF0958705`
- Web POC `0.4.0-rc120`/code 137, 3,327,770 bytes:
  `6BA591FCF892D231AA61EBE97C615A0FEE7A89EFCD02B4533B94BB445F773E25`
- PC 수신기 `0.1.5`, 21,925,051 bytes:
  `BA94DCC1ADA65383C7F9EFD515B79C4CC4B273BA8FABC880E348A59BEEE1B136`

A에는 두 APK를 `adb install -r`로 보존형 설치했다. Kiosk UID 10288와
firstInstallTime `2026-07-24 12:52:28`, Web UID 10293와 firstInstallTime
`2026-07-28 13:12:16`이 유지됐고 Device Owner도 Kiosk signer package로
유지됐다. PC 실행 파일은 0.1.5 artifact와 SHA-256이 일치한다.

### 실제 기기·PC 결과

- 기존 DB 초기화·관리자 인증·원버튼 안전 복구·운영 자가진단: PASS.
  Device Owner, 잠금 허용, Web 보호, 카메라, 인터넷, 저장공간과 지정 PC 연결이
  모두 정상이다.
- `토요일2`의 기존 학생 3명과 QR 데이터: 업데이트·복구·두 차례 재부팅 뒤
  보존 확인.
- 수동 학생 선택과 표시명이 정확히 `테스트`인 기존 QR PDF의 제한된 원격
  시험 입력: 각각 공식 Web 로그인 PASS. QR 원문·해시는 출력·파일 기록하지
  않았다.
- 정상 학생 종료→QR 대기 복귀→현재 수업 안전 종료: PASS. 시험 답안을
  입력하거나 제출하지 않았다.
- Wi-Fi 단절: 답안·학생 이름·학생 navigation·종료 버튼을 가리는 불투명
  fail-closed 화면과 입력 focus 차단 확인. Wi-Fi 복구 뒤 같은 학생·공식 목록
  화면으로 자동 복귀하고 LockTask `LOCKED` 유지.
- ADB HOME/RECENTS fault: launcher·다른 앱으로 이탈하지 않고 Web 세션을
  관리자 PIN 잠금으로 실패 폐쇄, LockTask `LOCKED` 유지.
- 최종 RC58/RC120 재부팅: Kiosk 자동 foreground, LockTask `LOCKED`,
  Device Owner·UID·최초 설치일·학생 데이터 유지, 원격 점검 same-boot
  상태 자동 해제 확인. 관리자 인증 후 `ADMIN_IDLE`로 종료했다.
- Android 직접 인쇄 버튼·경로는 현재 UI에서 보이지 않았다.
- PC 수신기 update 중 방화벽 규칙 재생성은 비관리자 PowerShell에서 access
  denied였다. 기존 `Private / Inbound / Allow / TCP 48129` 규칙이 같은
  0.1.5 실행 경로를 계속 가리키고 port listen 및 태블릿 지정 PC 자가진단이
  정상이라 규칙은 변경하지 않았다. 이전 실행 파일은
  `%LOCALAPPDATA%\MatholicPdfReceiver\backup`에 rollback용으로 보존했다.

### 남은 현장 범위

실제 프린터의 물리 출력만 수행하지 않았다. Android 직접 인쇄는 제품에서
제거됐으므로 남은 확인은 PC 수신 폴더의 PDF를 운영자가 프린터로 출력하는
외부 장치·용지 결과에 한정된다. 그 밖의 채택된 리뷰 교정에는 추가 사용자
결정이 필요하지 않다.

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

---

## Web POC RC28 프록시 종료 경합 소켓 정리 — 2026-07-26

### 재현한 종료 경합

RC27 loopback CONNECT 프록시는 종료할 때 등록된 소켓 목록을 복사해 닫았지만,
서버가 연결을 수락한 직후 목록 등록 전 종료 snapshot이 만들어지거나,
executor 종료와 작업 제출이 겹쳐 `RejectedExecutionException`이 발생하면
해당 client 소켓을 정리하지 못할 수 있었다.

- 실제 A, 공개 사이트와 자격정보를 사용하지 않음
- 종료와 등록을 직렬화하는 registry와 작업 거부 정리 계약 시험을 먼저 추가
- 수정 전 신규 대상 JVM 시험은 필요한 registry와 제출 경계가 없어 컴파일
  실패
- 첫 구현 뒤 Kotlin collection 초기화 표현의 컴파일 오류를 확인하고
  명시적인 snapshot 생성·목록 초기화로 수정

### 변경

- 구현·회귀시험 커밋: `a212198`
- Web POC `0.4.0-rc28`/code 45와 릴리스 운영 경로 준비 커밋: `5bc8f98`
- 소켓 등록과 종료 상태 전환을 하나의 동기화 경계로 직렬화
- 종료 뒤 도착한 소켓은 등록을 거부하고 즉시 닫음
- client 처리 또는 역방향 tunnel 작업이 executor 종료로 거부되면 소유한
  client·upstream 소켓을 즉시 닫음
- 서버 소켓, 등록된 소켓과 executor를 반복 호출에 안전하게 순서대로 종료

### 자동 검증

- 대상 JVM 시험 3개: 종료 뒤 늦은 등록 즉시 정리, executor 거부 시 cleanup,
  정상 작업 제출 시 cleanup 미실행을 검증해 모두 통과
- 첫 전체 회귀 시도는 재부팅 뒤 짧은 제한시간으로 시작했던 Gradle 하위
  프로세스와 재시도가 빌드 캐시를 동시에 사용하고 한글 경로 해석까지 겹쳐
  실패
- 남은 프로세스가 없음을 확인하고 영문 작업 경로에서 configuration cache를
  끈 단일 실행으로 재시험 통과
- Android 13 일회용 에뮬레이터 Web POC 전체 계측 56개,
  실패·오류·건너뜀 0
- 네 모듈 JVM 단위시험 총 94개, 실패·오류 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 85개, 실패·오류 0
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
- Web POC: `artifacts/matholic-webpoc-0.4.0-rc28-release.apk`
  - 크기: 3,101,640 bytes
  - SHA-256:
    `19A6D31C92483E890FE9A2CA91909AFDFA17C58DA2FFC48A383ECF52917B7520`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 업데이트와 설치 후 검사

- 설치 전 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc27`/code 44
- 물리 ADB `device`, 정확한 serial·SM-P610, 배터리 100%·USB 전원,
  UID·firstInstallTime·dataDir, 설치본·신규 artifact 해시와 release signer,
  Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0을
  확인
- Web POC RC28만 `adb install -r`: 성공
- 설치 후 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc28`/code 45
- 두 package UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir 유지
- 설치된 Web POC base APK SHA-256과 RC28 보관본 일치
- 설치된 Web POC와 RC28 artifact의 signer SHA-256 일치
- Device Owner·전용 HOME·`LOCKED`, 화면 `Dozing`, USB 화면 유지 설정 0
  유지
- 설치 시각 이후 AndroidRuntime 오류 일치 항목: 0

### 미검증

- 실제 A에서 accept·작업 제출과 프록시 종료를 고의로 경합시키는 실패시험
- 실제 QR→Web→QR 왕복과 관리자 PIN·사이트·카메라·PDF 공유·실물 인쇄
  회귀

---

## Kiosk RC27 / Web POC RC29 실물 회귀 결함 수정 — 2026-07-27

### 실물 재현

- 기존 학생·반 목록 보존: 통과
- 반에 소속되지 않은 학생 QR의 로그인 차단: 통과
- 관리자 화면에서 시스템 뒤로가기를 누르면 PIN 입력 화면으로 전환: 재현
- 기존 학생 로그인 뒤 자동 학습지 진입 전에
  `채점기가 잠겼습니다 PORTAL_ROUTE`: 재현
- `PORTAL_ROUTE`는 로그인 뒤 정확한 `im.matholic.com`에 도착했지만 기존
  정책이 `/course` 문서만 포털로 인정해, 실제 로그인 완료 경로를 DOM
  확인 전에 거부한 것이 원인

### 변경

- 구현·회귀시험 커밋: `55cc1ac`
- 릴리스 준비 커밋: `a552aa6`
- 관리자 화면의 시스템 뒤로가기는 항상 소비하고 현재 입력·화면을 유지
- 로그인 완료 포털은 정확한 `im.matholic.com` HTTPS 출처와 fragment 없음,
  개인정보·로그인정보·학습실 링크, 계정 메뉴 구조와 실제 학생 전체 이름의
  의미 계약이 모두 일치해야 인정
- 주소 범위를 임의의 외부 사이트로 넓히지 않았고 비표준 port·userinfo·
  fragment·교차 출처 링크·모호한 계정/로그아웃 구조는 계속 거부

### 자동 검증

- 수정 전 Web 정책 시험에서 `/course` 외 의미 포털 계약 실패 재현
- 수정 뒤 Web 포털 정책 JVM 시험 7개 통과
- 실제 WebView DOM 계약 계측 26개 통과
- 관리자 PIN 해제 뒤 시스템 뒤로가기 계측 1개 통과
- Android 13 일회용 에뮬레이터 전체 Kiosk 31개, Web 56개 통과
- 네 모듈 JVM 단위시험 총 94개, 실패·오류 0
- 네 모듈 clean debug 회귀: `BUILD SUCCESSFUL`, 204 tasks
- release Kiosk/Web JVM 보고서 85개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: `BUILD SUCCESSFUL`, 158 tasks
- release applicationId·versionName·권한·`debuggable=false`, v2 단일
  signer·두 앱 signer 일치·Debug signer 거부·zipalign과 build/stored APK
  이중 검증: 통과

### 릴리스 APK와 A 보존형 설치

- Kiosk `0.6.0-rc27`/code 32
  - 크기: 34,974,008 bytes
  - SHA-256:
    `8A638F8D9494079165E908644092F8B8A35EE00F41F5055B64709F833C37446C`
- Web POC `0.4.0-rc29`/code 46
  - 크기: 3,101,512 bytes
  - SHA-256:
    `8D688F201B81614D127F9F01DF9C38A7636CF6491CC05FA3CEEDC72BF9E59B1C`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- 두 앱 `adb install -r`: 성공
- UID `10288`/`10287`, firstInstallTime와 dataDir 유지
- 설치본 base APK와 보관 artifact 해시 일치
- Device Owner·전용 HOME·`LOCKED`, USB 화면 유지 설정 0 유지
- 설치 시각 이후 AndroidRuntime 오류 일치 항목: 0

### 재검증 대기

- 관리자 화면에서 뒤로가기 뒤 현재 화면·입력 유지
- 기존 학생 QR 로그인 뒤 `PORTAL_ROUTE` 없이 학습지 자동 진입
- 이후 학습지↔진단평가, 문제 입력·제출·오답 번호·QR 복귀 회귀

---

## Kiosk RC28 / Web POC RC30 학생 사용성·결과 완전성 보강 — 2026-07-27

### 실물 재현과 원인

- RC27/RC29에서 관리자 뒤로가기 현재 화면·입력 유지와 기존 학생의 자동
  학습지 진입은 사용자 실물 재검증 통과
- Matholic 최상단 메뉴와 학생 계정 메뉴, 문제지·오류신고·동영상·
  답안필기·풀이 업로드가 학생 화면에 남고, 주관식 입력기와 제출 화면이
  불편한 문제 재현
- 25문제를 모두 틀렸는데 `5번` 하나만 표시한 결과 오류 재현
- 공개 배포된 2026-07-08 Matholic JavaScript를 자격정보 없이 읽기 전용으로
  확인한 결과, 결과 화면은 5번째 이후 문제를 IntersectionObserver로
  스크롤 시점에 지연 생성함. 기존 구현은 현재 생성된 alert 카드만 전체로
  오인

### 변경

- 학생 상단 메뉴의 의미 링크 묶음을 숨기고 학습지·진단평가 앱 버튼만 유지
- 두 페이지 전환은 정확히 하나인 공식 내부 링크를 우선 클릭해 SPA 전환하고,
  계약 불일치 시 기존 안전한 전체 로딩으로 대체
- 문제지·오류신고·해설 동영상과 문구·답안 필기 입력·풀이 업로드를 숨김
- 입력기 메뉴를 수식으로 전환하고 기본·분수 선택과 입력기 변경 버튼을 숨김
- 주관식 입력 폭·공통 box sizing을 보정하고 제출 동작 줄에 19px 간격 추가
- 전체답안 모달을 내부 끝까지 자동 이동하고 최종 제출 버튼을 계속 노출
- 허용 학생 문서의 JavaScript alert/confirm을 주소 머리말 없는 앱 확인창으로
  대체
- 결과 상세를 먼저 차폐한 채 페이지를 위에서 아래로 순차 이동해 지연 카드를
  모두 생성한 뒤, 1번부터 마지막 번호까지 완전한 정답·오답·모름·제외
  분류가 있을 때만 오답 목록 표시. 불완전하면 거짓 목록 대신
  `RESULT_INCOMPLETE`
- QR 이미지는 계속 표시·저장하지 않고 QR 형식·`MQR1:` 접두 경계를 유지.
  QR 경계 좌표로 중앙·크기·상하좌우를 안내하고 중앙 판독 범위일 때만 인증
  결정을 전달

### 자동 검증

- Web DOM 계약 34개와 최종 전체 Web 계측 64개 통과
- Kiosk 전체 계측 31개 통과
- 네 모듈 JVM 시험 101개, 실패·오류 0
- clean debug 단위시험·lint·APK: 204 tasks 통과
- release 단위시험·lint·두 APK assemble: 158 tasks 통과
- release applicationId·versionName·versionCode·권한·`debuggable=false`,
  APK Signature Scheme v2·두 앱 동일 release signer·Debug signer 거부,
  zipalign과 build/stored APK 이중 검증 통과

### 릴리스 APK와 A 보존형 설치

- Kiosk `0.6.0-rc28`/code 33
  - SHA-256:
    `DFA76863C027A8D31E951FE972943E0A12982E0C4FFC0280E346705DD8E1CB7B`
- Web POC `0.4.0-rc30`/code 47
  - SHA-256:
    `571D4C3DAEA56469A28ACC4D6840089ACDFD28B1B2B11D2A5D2434106FE4A80C`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- A는 SM-P610·Android 13·배터리 100%·USB 전원·ADB `device` 확인 후 두 앱
  `adb install -r` 성공
- UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`, dataDir와 카메라 권한 유지
- 설치된 두 base APK SHA-256과 보관 artifact 일치
- Device Owner·전용 HOME·`LOCKED` 유지, 설치 뒤 Matholic crash buffer
  일치 항목 0

### 실물 재검증 대기

- QR 위치 방향·거리 안내와 전면 카메라 좌우 방향
- 최상단 메뉴와 문제지·오류신고·동영상·답안필기·풀이 업로드 비표시
- 수식 기본 전환·주관식 잘림·제출 간격·전체답안 자동 이동·확인창 머리말 제거
- 25문제 전부 오답 및 정답·오답·모름 혼합 결과의 전체 번호 정확성
- 학습지↔진단평가 SPA 우선 전환 체감 속도와 안전 fallback

---

## Kiosk RC29 / Web POC RC31 역방향 가로 회전 — 2026-07-27

### 변경

- Kiosk QR·관리자 화면과 Web 학생 화면 모두 `landscape` 고정 대신
  `sensorLandscape`를 사용
- 세로 방향은 계속 허용하지 않고 케이블 위치에 따라 두 가로 방향을
  0°↔180°로 전환
- release APK 검증에 두 앱의 `sensorLandscape` 선언 확인을 추가

### 자동 검증

- Android 13 에뮬레이터 전체 계측:
  - Web POC 65개, 실패·오류 0
  - Kiosk 32개, 실패·오류 0
- release 단위시험·lint·두 APK assemble: 158 tasks 통과
- release applicationId·versionName·권한·`debuggable=false`, v2 단일
  signer·두 앱 signer 일치·Debug signer 거부·zipalign·회전 선언과
  build/stored APK 이중 검증 통과
- 보관 체크섬 재계산·대조 통과

### 릴리스 APK와 A 보존형 설치

- Kiosk `0.6.0-rc29`/code 34
  - SHA-256:
    `56B84978035389E903F3CE4C9982A2F672B97166B6ED5A35EC8F017A643C0761`
- Web POC `0.4.0-rc31`/code 48
  - SHA-256:
    `75D6830EB4BA37E9F05BD139E9CB55782554C9BFA653E524B5ED84EB3F26FA36`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- 두 앱 `adb install -r`: 성공
- UID `10288`/`10287`, firstInstallTime, dataDir와 카메라 권한 유지
- Device Owner·전용 HOME·`LOCKED` 유지
- 설치 직후 최근 5분 Matholic AndroidRuntime 오류 일치 항목 0
- 업데이트 뒤 실패폐쇄 상태는 `RECOVERY_REQUIRED`; 관리자 PIN과 기존
  수업 안전 종료·세션 정리가 필요한 정상 복구 경계

### 미검증

- A를 실제로 180° 뒤집었을 때 QR 대기·Web 문제 화면 모두 반대 가로
  방향으로 전환되는지

---

## Kiosk RC30 / Web POC RC32 실물 회귀 결함 보강 — 2026-07-27

### RC29/RC31 실물 결과

- 통과:
  - QR·Web 화면의 두 가로 방향 회전
  - Matholic 최상단·계정 메뉴 비표시
  - 오류신고·해설 문구 비표시
  - 제출 줄과 문제 이동 영역 간격
  - 수식 입력기 자동 전환과 모드 변경 숨김
  - 미입력 확인창의 사이트 주소 머리말 제거
- 실패 재현:
  - 학습지→진단평가 전환 때 전체 로딩 차단 화면과 긴 지연
  - 문제 화면의 동영상 패널 잔류
  - 요청하지 않은 분수 선택 제거
  - 답안칸을 누르기 전 필기 입력 제어 잔류
  - 전체답안 내부 자동 이동과 풀이 업로드 제거 실패
  - QR 목표 사각형의 역할이 불명확하고 좌우 안내가 과민
  - 25문항에서 `RESULT_INCOMPLETE`

### 원인과 변경

- 학습지 페이지에 반응형 레이아웃용 동일 `/diagnostic` 링크가 여러 개 있어
  기존 “정확히 하나” 조건이 실패하고 전체 문서 로딩 fallback을 탔다. 동일
  HTTPS origin·정확한 고정 경로 계약을 모두 만족한 후보 중 의미·가시성이
  가장 높은 링크를 클릭하도록 수정했다.
- 공개 Matholic 자산의 실제 DOM을 읽기 전용으로 확인했다. 동영상은
  `video`/`iframe`이 아니라 `문항 동영상`·`대표 유형 동영상` alt를 가진
  이미지 패널이고, 필기 입력은 주관식 input suffix 첫 제어이며, 전체답안은
  Ant modal title/body 내부 스크롤 구조였다.
- 동영상 이미지 패널과 필기 suffix 제어를 구조로 숨기고, 분수는 복원하며
  `기본` 선택만 숨긴다.
- 실제 전체답안 모달의 내부 스크롤을 끝으로 이동하고 풀이과정·업로드
  컨테이너를 숨긴다.
- 종합분석의 가장 가까운 내부 스크롤 컨테이너를 점진 이동한다. 카드 수와
  최대 스크롤이 네 번 연속 안정된 뒤에만 수집 완료로 판정한다.
- 결과 표의 `문항수`를 기대값으로 읽고, 1..N 분류 수가 일치해야만 결과를
  표시한다. 진행 중 상태는 별도 60초 한도를 사용하고 최종 실패에는
  `확인된 문항: X/N`을 표시한다.
- QR 중앙 사각형에 “카메라 화면이 아닌 판독 목표 영역”임을 명시하고,
  좌우 중앙 허용 범위를 30%~70%로 넓혔다. 상하는 35%~65%를 유지한다.

### 자동 검증과 릴리스

- 관련 Web DOM 계측 36개: 실패·오류 0
- 25문항 내부 스크롤 fixture에서 25개 전체 지연 생성, 기대 문항수와
  분류 수 일치 확인
- Android 13 에뮬레이터 전체 계측:
  - Web POC 67개, 실패·오류 0
  - Kiosk 32개, 실패·오류 0
- Web JVM 45개, Kiosk JVM 48개: 실패·오류 0
- debug 단위시험·두 APK assemble: 84 tasks, 통과
- release 단위시험·lint·두 APK assemble: 158 tasks, 통과
- build/stored APK의 applicationId·versionName·권한·
  `debuggable=false`·`sensorLandscape`, v2 단일 동일 release signer,
  Debug signer 거부와 zipalign 이중 검증: 통과
- 구현 커밋:
  - `1679a5d` Web 제어·전환·25문항 수집
  - `c74b993` QR 안내·좌우 허용 범위
  - `68fed79` Kiosk RC30/Web POC RC32 준비

### APK와 A 보존형 설치

- Kiosk `0.6.0-rc30`/code 35
  - 크기: 34,990,520 bytes
  - SHA-256:
    `33BFC1BD52E2CDAA8A4CF3DA7256A92C51C690FB38B0B95D851FA3516C9DF005`
- Web POC `0.4.0-rc32`/code 49
  - 크기: 3,128,208 bytes
  - SHA-256:
    `2ACF12895A3AD77C6C2D4F30CC20F9D8D1CD905383811949B35B66997383E425`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- Web POC, Kiosk 순으로 `adb install -r --no-streaming`: 성공
- 설치 전후 UID `10288`/`10287`, firstInstallTime
  `2026-07-24 12:52:28`/`2026-07-24 12:52:24`와 dataDir 유지
- 설치된 두 base APK의 크기·SHA-256이 보관 artifact와 일치
- Device Owner·전용 HOME·두 앱 allowlist·`LOCKED` 유지
- 설치 시각 이후 두 앱의 `FATAL EXCEPTION`/process crash 일치 항목 0
- 업데이트에 따른 정상 실패폐쇄 상태: `RECOVERY_REQUIRED`

### 재검증 대기

- 관리자 PIN→현재 수업 안전 종료→Web 로그인 정리→같은 반 수업 시작→
  `QR_READY`: 실물 통과
- 기존 `토요일2` 반과 소속 학생 3명 목록 UI 보존: 실물 통과
- 학습지→진단평가 SPA 전환 속도와 차단 화면 비표시
- 문제 동영상·입력 전 필기 제어 비표시와 분수 입력 유지
- 전체답안 자동 이동·풀이 업로드 비표시
- 실제 사이트 25문항 오답 목록 완전성: 실물 통과
- 화면 중앙 목표 영역은 물리 렌즈 위치와 무관해 오해를 만든다는 사용자
  지적을 반영해 `fed3fe5`에서 완전히 제거. 다음 Kiosk 배포에 포함 예정

---

## Kiosk RC31 / Web POC RC33 초기 노출·직접 입력 제거 — 2026-07-27

### 결함과 수정

- 숨김 대상인 Matholic 상단·문제 제어가 화면 공개 뒤 잠시 보이는 문제를
  재현했다. 학생 페이지 및 고정 메뉴 전환은 WebView를 가린 상태로 DOM
  계약을 적용하고 목표 경로에서 2회 연속 성공한 뒤에만 공개한다.
- 파란 직접 필기 입력 버튼은 수식 입력 영역의 우측 상단에 늦게 생성되는
  빈 아이콘 버튼이었다. 루트·분수·파이 도구 모음을 기준으로 해당 8px
  절대 위치 버튼만 숨기고, 이후 생성되는 동일 버튼도 DOM 감시로 숨긴다.
- QR 화면의 물리 렌즈 위치와 무관한 중앙 목표 사각형을 제거했다.

### 자동 검증과 릴리스

- Web POC Android 13 전체 계측 67개: 실패·오류 0
- Kiosk Android 13 전체 계측 33개: 실패·오류 0
- 늦게 추가한 직접 입력 버튼이 별도 재적용 없이 숨겨지는 회귀시험 포함
- release 단위시험·lint·두 APK assemble 158 tasks: 통과
- build/stored APK 버전·권한·`debuggable=false`·`sensorLandscape`,
  v2 단일 동일 release signer, Debug signer 거부, zipalign 이중 검증: 통과
- Kiosk `0.6.0-rc31`/code 36, 34,990,580 bytes
  - SHA-256:
    `C5CBB48D5B5EA6D5A29EDF35C110FB68D9B96CD37F652F4D5DADA45B3045AC1E`
- Web POC `0.4.0-rc33`/code 50, 3,131,404 bytes
  - SHA-256:
    `D8AFB0F4D7FD79202D572D0F38617DF9FF650D355C9A5347CEAA22D3480CCCBC`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존형 설치

- Web POC, Kiosk 순서로 `adb install -r --no-streaming`: 성공
- UID `10287`/`10288`, firstInstallTime
  `2026-07-24 12:52:24`/`2026-07-24 12:52:28` 유지
- Device Owner·전용 HOME·`LOCKED` 유지
- 설치된 두 base APK의 SHA-256이 보관 artifact와 일치
- 현재 상태: 업데이트 실패폐쇄의 `RECOVERY_REQUIRED`

### 실물 재검증 대기

- 관리자 PIN 복구→현재 수업·Web 세션 안전 종료→수업 재시작→`QR_READY`
- QR 중앙 목표 사각형 비표시
- QR 로그인 뒤 학생 화면 공개 시 숨김 대상이 먼저 보이지 않는지
- 답안 입력칸을 누르기 전부터 파란 직접 입력 버튼이 보이지 않는지

---

## Web POC RC34 미등록 로그인 실패 안전 종료 — 2026-07-27

### 재현과 원인

- 미등록 아이디 QR 로그인 실패 뒤 관리자 안전 종료가
  `LOGIN_FINGERPRINT_U1_P1_C1_B1_F1_A0_E11_R0_V1`로 실패
- `U/P/C/B/F=1`, `E11`, `R0`, `V1`은 로그인 입력 구조와 빈 값,
  체크박스·계약 버전이 정상임을 뜻하며 `A0`만 실패
- 공식 로그인 JavaScript는 실패 리디렉션의 `url` 값을
  `https://auth.matholic.com/token/signin?url=...` action에 보존한다.
  기존 복구기는 이 정상 실패 페이지를 정리용 기본 로그인 페이지로
  canonicalize하기 전에 action query를 거부했다.

### 변경과 검증

- 로그아웃·복구 상태에서 query가 있는 공식 로그인 루트가 완료되면
  `https://login.matholic.com/`을 새로 열어 query를 제거한다.
- canonical 페이지에서 기존 action origin/path, 입력 구조, 빈 값과
  체크박스 검증을 그대로 수행한다. 일반 로그인 상태에는 적용하지 않는다.
- 실제 공개 실패 리디렉션→canonical 로그인→`IDLE` 복구 계측: 통과
- Web JVM·debug APK·AndroidTest APK: 통과
- Android 13 Web 전체 계측 68개: 실패·오류 0
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증: 통과

### Web RC34와 A 설치

- Web POC `0.4.0-rc34`/code 51, 3,132,564 bytes
- SHA-256:
  `798C59546E49EA320ADA7F47EF85E0ABBCC39FE5B1BAEAB04BC4879DFF981331`
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- A에 Web POC만 `adb install -r --no-streaming`: 성공
- UID `10287`, firstInstallTime `2026-07-24 12:52:24`, Device Owner와
  전용 HOME 유지
- 설치된 base APK SHA-256이 artifact와 일치
- 실물 재검증:
  - 기존 실패 상태의 안전 종료→수업 재시작→QR 화면: 통과
  - 같은 미등록 아이디 로그인 실패 재현→안전 종료→수업 안전 시작 가능:
    통과
- 검증 직후 독립 UI 확인: `ADMIN_IDLE`, `선택한 반 수업 안전 시작`,
  활성 수업 없음. 따라서 최종 상태를 `QR_READY`로 기록하지 않음

---

## Web POC RC40 주관식 최초 입력 보존 / Kiosk RC34 앱 제거 차단 — 2026-07-28

### 선행 실물 결과와 결함

- RC39 전체답안과 결과 흐름:
  - 최초 자동 이동과 이후 사용자 스크롤 위치 유지: 통과
  - 풀이·정답 비표시, 정확한 오답 번호만 표시: 통과
  - 확인 후 자동 로그아웃, `QR_READY` 복귀, 오류 코드 없음: 통과
- 이후 주관식에서 첫 입력이 보이지만 문제를 이동했다 돌아오면 사라지는
  결함을 재현했다.
- 숫자를 지우거나 3자리 이상 입력한 뒤 나타나는 회색 원형은 수식
  입력기의 지우기 버튼이며, 이 상태부터 답이 보존됐다.

### 원인과 수정

- 2026-07-28 공개 Matholic 학습 자산을 읽기 전용으로 확인했다.
- 공식 입력기 메뉴는 `수식`을 고르면 기존 `ONE` 입력 컴포넌트를 `EQ`
  수식 컴포넌트로 교체한다. 자동 선택과 학생의 첫 입력이 겹치면 이전
  컴포넌트의 값이 교체 과정에서 유실됐다.
- 답안 모달이 나타나면 수식 컴포넌트와 MathQuill 편집기의
  `.mq-editable-field`가 실제 준비될 때까지만 해당 답안 영역의 터치를
  차단한다. 준비 완료 뒤 기존 pointer/접근성 상태를 복원한다.
- 루트·분수·파이는 유지하고 기본 입력기와 직접 필기 입력만 계속 숨긴다.
- 계약 버전을 `web-2026-07-28.5`로 올렸다.

### 회귀시험과 릴리스

- 새 회귀시험은 수정 전 `mathModePending` 계약 부재로 실패했고 수정 후 통과
- 같은 회귀시험 연속 3회: 통과
- Web DOM 계약 39개: 통과
- Web 전체 Android 13 계측 71개: 실패·오류 0
- Web/Kiosk 단위시험·lint·debug assemble: 통과
- Kiosk 전체 Android 13 계측 34개: 실패·오류 0
- release 단위시험·lint·두 APK assemble 158 tasks: 통과
- applicationId·version·권한·`debuggable=false`, v2 단일 동일 release
  signer, Debug signer 거부, zipalign과 저장 artifact 이중 검증: 통과
- 구현 커밋:
  - `a071f17` 수식 입력 준비 완료 전 입력 차단
  - `168ae3f` Web POC RC40 준비

### A 연결 계측 사고와 재발 방지

- 12:50 에뮬레이터와 A가 동시에 연결된 상태에서 raw Gradle Web 계측 명령을
  실행했다.
- A의 release 앱에 debug 시험 앱 설치는 signer 불일치로 실패했지만 시험
  도구 정리 단계가 12:50:20 Web POC를 제거했다. Android
  `PACKAGE_REMOVED` 로그와 기존 UID `10287` 제거를 확인했다.
- 영향:
  - Kiosk의 반·학생·QR·수업 DB와 Keystore 자격정보는 별도 package라 보존
  - Web POC의 WebView 로그인 세션과 앱 안전상태 SharedPreferences는 삭제
  - Web POC는 자격증명을 지속 저장하지 않으므로 저장 자격증명 손실은 없음
- 재발 방지:
  - 물리 serial을 거부하고 Android emulator 여부를 이중 확인하는
    `scripts/test-webpoc-emulator.ps1` 추가
  - Kiosk Device Owner가 Web POC에 `setUninstallBlocked(..., true)` 적용
  - 정책이 적용되지 않으면 Kiosk 상태에 `전용기기 정책 오류` 표시
- 구현 커밋:
  - `3c847d8` Web POC 제거 차단과 에뮬레이터 전용 시험 경로
  - `1d366fe` Kiosk RC34 준비

### APK와 A 설치

- Kiosk `0.6.0-rc34`/code 39
  - SHA-256:
    `C27FB8D86628924172538D87451A3C292E0BCA2F8C88431C174DDB3C072A36D4`
- Web POC `0.4.0-rc40`/code 57
  - SHA-256:
    `021C2DFFA3BAEFA1C27D4FB6859661B95958B64BF960C71358BA8A01388D1839`
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- Web POC는 제거 사고 뒤 신규 설치되어 UID `10293`, firstInstallTime
  `2026-07-28 13:12:16`으로 변경
- Kiosk는 보존형 설치 성공. UID `10288`, firstInstallTime
  `2026-07-24 12:52:28`, dataDir와 Kiosk DB 유지
- 두 설치본 base APK SHA-256과 보관 artifact 일치
- Device Owner·전용 HOME·`LOCKED` 유지, 정책 오류 표시 없음
- 설치 시각 이후 AndroidRuntime 오류 0

### 남은 실물 확인

- 관리자 PIN→기존 수업/Web 상태 안전 종료→같은 반 수업 시작→`QR_READY`
- 주관식 1자리 입력 직후 문제 이동·복귀 시 값 유지
- 삭제 후 재입력, 3자리 이상, 여러 주관식 문제와 전체답안 동기화
- 회색 지우기 버튼 출현 여부와 무관하게 입력값 유지
- 최종 제출·오답 번호·자동 로그아웃·`QR_READY`

---

## Web POC RC41/RC42 문제별 주관식 저장·키보드·뒤로가기 — 2026-07-28

### RC41 재현과 원인

- RC41에서 일부 주관식 문제는 첫 자리부터 저장됐지만, 다른 문제는 이전처럼
  세 자리까지 입력해야 저장되는 실물 편차를 확인했다.
- 공개 Matholic 입력기 구현은 각 MathQuill 인스턴스의 최초 편집 알림 두
  번을 무시한다. RC41은 기본 입력기에서 수식 입력기로 전환한 한 화면 틀만
  준비 완료로 표시했다.
- React가 같은 답안 화면 틀을 재사용하면서 문제별 MathQuill DOM만 교체하면
  새 편집기를 건너뛰었고, 처음부터 수식형인 문제도 준비 대상에서 빠졌다.
- 답이 저장되면 나타나는 회색 음영은 사이트의 16px 전체 답 삭제 버튼이며,
  누르면 해당 답이 지워지는 동작임을 공개 자산에서 확인했다.
- 키보드가 올라오면 기본 창 resize로 네이티브 `채점 끝내기` 버튼이 키보드
  위로 이동해 답안 입력 영역을 가렸다.

### RC42 수정

- 화면 틀이 아니라 실제 `.mq-editable-field` DOM 인스턴스마다 최초 편집
  알림 두 번을 안전하게 소진한다. 새 문제·재생성·처음부터 수식형인 문제를
  모두 같은 규칙으로 처리한다.
- MathQuill 값은 준비 전후 동일함을 확인한 뒤에만 학생 입력을 허용한다.
- 회색 전체삭제 버튼은 생성 시점부터 CSS로 숨긴다. 학생은 숫자 키패드의
  삭제 키로 필요한 만큼 지울 수 있다.
- `windowSoftInputMode=adjustNothing`으로 키보드가 앱 창을 줄이지 않게 해
  `채점 끝내기` 버튼이 입력칸 위로 이동하지 않도록 했다.
- Web 뒤로가기는 최고 우선순위 콜백과 구형 보조 경로에서 모두 소비한다.
- 계약 버전: `web-2026-07-28.7`

### 수정 전 실패와 자동 검증

- 새 수식 입력기·같은 화면 틀의 재생성 회귀시험:
  수정 전 첫 숫자의 저장값 `null`로 실패, 수정 후 통과
- 키보드 resize 방지 manifest 회귀시험:
  수정 전 soft input adjust 값 `0`으로 실패, 수정 후 `ADJUST_NOTHING` 통과
- 기존 자동 전환 주관식 첫 입력 보존 회귀: 통과
- 실제 시스템 뒤로가기 키 주입 회귀: 통과
- Web Android 13 전체 계측 75개: 실패·오류 0
- Web 단위시험·lint·debug assemble: 통과
- release 단위시험·lint·두 APK assemble 158 tasks: 통과
- applicationId·version·권한·`debuggable=false`, v2 단일 동일 release
  signer, Debug signer 거부, zipalign과 저장 artifact 이중 검증: 통과
- 구현 커밋:
  - `8b7e0d1` 주관식 최초 입력과 시스템 뒤로가기 보강
  - `5ffff56` 모든 수식 편집기 인스턴스 안정화·회색 삭제 제어·키보드 겹침
  - `e86cc81` Web POC RC42 준비

### RC42 A 설치와 실물 결과

- Web POC `0.4.0-rc42`/code 59
- artifact/설치 APK SHA-256:
  `B7FE1E365110425F2A0BD4219EECEBD9C7FB60746A741E059BD81255443526AF`
- A에 Web POC만 `adb install -r --no-streaming`: 성공
- UID `10293`, firstInstallTime `2026-07-28 13:12:16` 유지
- Device Owner·전용 HOME·`LOCKED` 유지, 설치 후 관련 앱 crash 없음
- 사용자 실물 확인:
  - 서로 다른 주관식 문제에서 한 자리 입력 후 문제 왕복·값 유지: 통과
  - 세 자리 입력·일부 삭제 후 문제 왕복·값 유지: 통과
  - 회색 전체삭제 제어의 지속 노출·터치 가능 상태 제거: 통과
  - 숫자형 키패드와 버튼 크기: 통과
  - 키보드 표시 중 `채점 끝내기`의 입력칸 겹침 방지: 통과
  - 학생 Web 시스템 뒤로가기 무반응·세션 유지: 통과
- 회색 제어가 숨겨질 때 약 0.2초 음영이 보이지만 불편하지 않고 기능 문제는
  없다는 사용자 확인을 받아 경미한 잔상으로 기록한다.
- 실물 시험 종료 뒤 독립 ADB 확인: Kiosk `QR_READY`, 전용 HOME·`LOCKED`

---

## Web POC RC46 수식 방향 패드 최종 배치 — 2026-07-29

- 수식 입력용 역 T자 방향 패드를 이전 기준에서 오른쪽 10mm, 아래 10mm 더
  이동해 사이트의 `뒤로` 버튼, 문제와 답안 입력칸을 가리지 않게 했다.
- 구현 커밋 `041aeb1`, Web POC RC46 준비 커밋 `ac98a54`.
- Web POC `0.4.0-rc46`/code 63의 artifact/설치 APK SHA-256:
  `F499459F0690536F9217294D36DB9D6BEA3460789CA7CB23820B40E19FB4A2CA`
- A에 같은 signer로 보존형 설치했고 UID `10293`, firstInstallTime
  `2026-07-28 13:12:16`, Device Owner·전용 HOME·`LOCKED`를 유지했다.
- 사용자 실물 확인:
  - 방향 패드가 `뒤로`, 문제와 입력칸을 가리지 않음: 통과
  - 스크롤 없이 전체 패드 표시: 통과
  - 루트·분수 등에서 네 방향 커서 이동: 통과

## 지정 PC 암호화 PDF 전송과 Kiosk RC37 — 2026-07-29

### 구현과 보안 경계

- 인터넷·클라우드 계정·USB 연결 없이 같은 사설 Wi-Fi의 지정 PC로만 QR
  카드 PDF를 보내는 Windows 수신기 `0.1.0`과 Kiosk 송신 기능을 추가했다.
- 관리자 화면에서 PC 수신기의 페어링 QR을 물리적으로 한 번 촬영한다.
  페어링 정보는 128비트 수신기 ID와 256비트 비밀키를 포함하며 A에서는
  Android Keystore로 보호한다.
- PDF와 학생 이름은 HKDF로 파생한 키의 AES-256-GCM으로 암호화·인증하고,
  수신 확인 응답도 HMAC-SHA256으로 인증한다.
- 5분을 벗어난 요청, 5MiB 초과 PDF, 위조 요청과 처리한 request ID의
  재전송을 거부한다.
- Windows 방화벽은 Private 프로필의 TCP 48129와 설치된 수신 EXE 한 개만
  허용한다. 수신 파일은
  `%USERPROFILE%\Downloads\Matholic QR Cards`에 저장한다.
- Google Quick Share PC 앱은 비교 시험 뒤 계정·주변 공개 범위 면에서 이번
  운영에 이점이 없어 제거했다. Android의 기존 일반 공유 경로는 비상
  대안으로 남겼다.

### 자동 검증

- PC 수신기 Python 시험 7개: 통과
- 패키징된 EXE import·시작 스모크 시험: 통과
  - 첫 패키징에서 상대 import 오류를 실제 EXE 실행으로 발견
  - `d225ed6`에서 수정하고 빌드가 이 스모크 시험을 필수로 수행하게 보강
- 설치된 수신기를 대상으로 한 합성 암호화 전송 왕복·파일 저장 확인: 통과
  - 시험용 PDF는 확인 뒤 정리
- Kiosk JVM 단위시험: 통과
- Android 13 에뮬레이터 Kiosk 전체 계측 38개: 실패·오류 0
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증: 통과
- 주요 커밋:
  - `0b0df69` 지정 PC 암호화 수신기
  - `ba4ee4d` Python 생성 캐시 제외
  - `474132b` Kiosk 페어링·전송 기능
  - `f5fd36a` Kiosk RC36 준비
  - `d225ed6` 패키징된 Windows 진입점 검증
  - `c08dc80` 설치본 암호화 전송 스모크 시험
  - `eda9987` 작은 화면에서도 PC 전송 제어가 보이도록 관리자 우측 패널
    스크롤 보강

### 배포본과 설치 상태

- Kiosk `0.6.0-rc37`/code 42
  - artifact/설치 APK SHA-256:
    `8F7B59DEBCBC1A276A35A8A9606E187A13926FB3392F16916CB0BE54F2B2460B`
- Web POC `0.4.0-rc46`/code 63
  - artifact/설치 APK SHA-256:
    `F499459F0690536F9217294D36DB9D6BEA3460789CA7CB23820B40E19FB4A2CA`
- 두 Android APK의 release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- Windows 수신기 `0.1.0`
  - 보관본과 설치본 SHA-256:
    `EBAB8CE69F1AB29D1F80DC84B0BB2EF5806A0BAC037FC30E846C09B1144C9EAE`
  - 설치 경로:
    `%LOCALAPPDATA%\MatholicPdfReceiver\app\MatholicPdfReceiver.exe`
  - 자동 시작 바로가기와 Private 전용 방화벽 규칙 존재
  - 수신 프로세스와 TCP 48129 수신 대기 확인
- RC37 설치 전후 Kiosk UID `10288`, firstInstallTime
  `2026-07-24 12:52:28`, Device Owner·전용 HOME·`LOCKED`를 유지했다.
- RC37에서 관리자 우측 패널 스크롤 보강 후 `현재 카드 지정 PC로 보내기`와
  `지정 PC 다시 페어링`이 실물 화면에 함께 보임을 확인했다.

### 사용자 실물 종단간 검증

- A에서 지정 PC `DESKTOP-D4AGJI7` 재페어링: 통과
- A의 `지정 PC로 카드 PDF를 암호화해 보내는 중` 상태 표시: 통과
- PC 수신기의
  `20260729-112723_테스트 QR.pdf 저장 완료` 표시와 실제 파일 저장:
  통과
  - 파일 크기 616,701 bytes
- 수신 PDF의 QR로 정상 로그인 → 문제 화면 → 채점 끝내기 →
  Kiosk `QR_READY`: 통과
- 재발급 전 기존 QR 무효화: 통과
- 최종 독립 확인:
  - A Kiosk `0.6.0-rc37`, Web POC `0.4.0-rc46`
  - `QR_READY`, Device Owner·전용 HOME·`LOCKED`
  - `RECOVERY_REQUIRED`와 채점기 잠금 표시 없음
  - 최근 Matholic 관련 `FATAL EXCEPTION` 일치 항목 0
  - PC 수신기 TCP 48129 대기 중

### PC 재부팅 자동 시작 실기

- PC 부팅 시각 `2026-07-29 11:50:42` 뒤 수신기가
  `11:52:55`부터 자동 실행됐다.
- 설치 경로의 수신 프로세스와 TCP 48129 `LISTEN`을 확인했고, 설치본
  SHA-256도 보관본과 계속 일치했다.
- 같은 시점 A는 Kiosk 전용 HOME, `LOCKED`, `QR_READY`를 유지했고
  `RECOVERY_REQUIRED`와 최근 Matholic 관련 fatal 일치 항목은 없었다.

### 남은 제한

- Windows 수신 EXE는 로컬 빌드의 무서명 실행 파일이다. 보관본과 설치본
  SHA-256은 일치하지만 다른 PC에 새로 설치할 때 SmartScreen 경고가 나올
  수 있다.
- Android 직접 프린터 전송 문제는 사용자가 현재 운영에서 굳이 건드리지
  않기로 결정해 이번 변경 범위에서 제외했다.

---

## Kiosk RC39 활성 카메라 안내와 QR 대기 화면 — 2026-07-29

- QR 대기 화면의 상단 제목·잠금 상태·중복 안내를 제거하고 위치 안내를
  중앙으로 통합했다. 카메라 전환과 관리자 진입은 아이콘으로 변경했다.
- 실제 활성 렌즈에 따라 중앙 문구가 전면·후면 카메라 안내로 바뀌며,
  PC 페어링 안내에도 같은 규칙을 적용했다.
- Kiosk 단위시험·debug assemble·계측시험 소스 컴파일·lint: 통과
- Android 13 Kiosk 전체 계측 39개: 실패·오류 0
- release 단위시험·lint·두 APK assemble 158 tasks와 APK 이중 검증: 통과
- Kiosk `0.6.0-rc39`/code 44를 A에 동일 signer로 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `7FC1163E4F2C7215351FDE6D833A0578074FADB2DF077B003B301DFA9CA2948A`
- UID `10288`, firstInstallTime `2026-07-24 12:52:28`, dataDir, 카메라
  권한, Device Owner와 전용 HOME 유지. 최종 Lock Task `LOCKED`,
  재시작 뒤 관련 fatal 0건.
- 사용자 실물 확인에서 전면·후면 안내 전환, 상단 중복 제거, 중앙 위치 안내,
  우측 하단 카메라·관리자 아이콘을 모두 통과했다.
- 검증 완료 후 자동시험용 에뮬레이터를 종료했다.

---

## Web POC RC47 제출 연타·복구 렌더러 보강 — 2026-07-30

### 장애 원인

- 전체답안 화면을 자동으로 최종 제출 위치까지 이동시키는 동안 답안 제출
  계열 버튼에 재진입 방지가 없었다. 따라서 같은 위치를 연속 터치하면 새로
  노출된 최종 제출 동작까지 전달될 수 있었다.
- 제출 후 결과를 수집하는 동안 전체 화면 결과 차폐막이 Web 터치를 막는다.
  이 상태에서 WebView 렌더러가 응답하지 않으면 화면은 멎은 것처럼 보이지만
  별도 네이티브 UI인 `채점 끝내기`는 계속 동작한다.
- 기존 로그아웃과 관리자 복구는 같은 WebView 렌더러에 `loadUrl`을 다시
  요청했다. 렌더러 자체가 멎은 경우 로그아웃 시간 초과와 복구 시간 초과가
  같은 원인으로 연속 발생했다.
- 실제 장애 당시 터치 이벤트와 WebView stack은 보존되지 않아 최초 불필요
  터치의 정확한 좌표까지 사후 확정할 수는 없다. 다만 제출 관통 경로와
  동일 렌더러 복구의 구조적 결함은 코드와 계측시험으로 확인했다.

### 수정

- 의미상 제출 버튼(`답안제출`, `답안 제출`, `완료하기`)의 두 번째 클릭을
  1.5초 동안 capture 단계에서 차단한다.
- 앱이 표시하는 JavaScript 확인창은 바깥 터치로 닫히지 않게 하고, 열린 뒤
  0.8초 동안 확인·취소 버튼을 비활성화한다.
- `WebViewRenderProcessClient`로 렌더러 무응답을 감시한다.
  - 일반 학생 세션에서는 렌더러를 종료하고 기존 실패폐쇄 잠금으로 전환한다.
  - 로그아웃·관리자 복구에서는 멎은 렌더러를 폐기하고 새 WebView로 한 번만
    복구를 재시도한다.
  - 복구 시간 초과도 같은 새 렌더러 1회 재시도를 거친 뒤에만 잠근다.
- Web POC 계약을 `web-2026-07-30.1`, 버전을
  `0.4.0-rc47`/code 64로 올렸다.

### 자동 검증

- Web POC JVM 단위시험·debug assemble·lint: 통과
- Android 13 에뮬레이터:
  - 전체 DOM 계약 계측시험 44개: 실패·오류 0
  - 전체 복구 계측시험 35개: 실패·오류 0
  - 제출 직후 같은 위치의 최종 제출이 차단되고 지연 후 정상 제출되는
    회귀시험: 통과
  - 복구 시 Activity와 WebView를 새로 만드는 회귀시험: 통과
  - 렌더러 무응답 callback에서 기존 WebView를 폐기하고 실패폐쇄하는
    결정론적 회귀시험: 통과
- release 단위시험·lint·두 APK assemble 158 tasks: 통과
- release APK의 버전, 권한, `debuggable=false`, v2 signer, 동일 signer,
  debug signer 거부와 zipalign 이중 검증: 통과
- 합성 페이지에서 플랫폼의 실제 renderer-unresponsive callback과 네이티브
  JavaScript 확인창을 안정적으로 발생시키지는 못했다. 무응답 처리 자체는
  결정론적 callback 시험으로, 제출 관통은 실제 WebView 터치 계측으로
  검증했다.

### A 기기 보존형 설치

- 설치 전:
  - Kiosk `0.6.0-rc39`/code 44
  - Web POC `0.4.0-rc46`/code 63
- 설치 후:
  - Kiosk `0.6.0-rc39`/code 44
  - Web POC `0.4.0-rc47`/code 64
- Web POC artifact/설치 APK SHA-256:
  `A1AB886B7F372AA5DFEC84B1D009F73B89C4B074D43A4640F47E3CBB325B1ED2`
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- `adb install -r` 성공. firstInstallTime과 앱 데이터, credential bridge
  권한, Kiosk Device Owner와 전용 HOME을 보존했다.
- 업데이트 과정에서 기존 Web POC와 멎은 sandbox renderer 프로세스가
  종료됐고, 설치 뒤에는 Kiosk만 전경에서 동작함을 확인했다.
- 실제 학생 계정의 제출→결과→안전 종료 종단간 확인은 PIN과 계정 정보를
  다루지 않고 사용자 실물 재확인으로 남겼다.

---

## Web POC RC48 주관식 입력 터치 복구 — 2026-07-30

### 확인된 원인

- RC47까지는 주관식 수식 편집기 준비 과정에서 다음 조건 중 하나라도
  충족되지 않으면 답안 scope 전체에
  `pointer-events: none !important`를 적용했다.
  - MathQuill 편집기 DOM 생성
  - 루트·분수·파이 toolbar와 방향 키패드 연결
  - 첫 입력 보존을 위한 MathQuill 안정화
- 실제 사이트는 수식 메뉴, toolbar, 편집기 DOM이 비동기로 순차 생성된다.
  합성시험과 다른 시점에 안정화 검사가 실행되면 scope 전체가 무기한
  터치 불가로 남았다.
- 같은 scope 안의 입력창, 루트·분수·파이와 입력기 버튼이 모두 영향을
  받으므로 사용자가 보고한 주관식 전용 장애와 일치한다.
- `.mq-editable-field ~ button` 규칙도 편집기 뒤의 모든 형제 버튼을 숨기는
  과도한 선택자였다. 사이트 DOM 배치에 따라 정상 수식 버튼까지 숨길 수
  있었다.
- 기존 계측시험은 앱과 같은 순서로 준비되는 합성 MathQuill만 사용했고
  “준비 중에는 답안 scope가 터치 불가여야 한다”를 정상으로 단정해 실제
  비동기 DOM 실패를 가렸다.

### 수정

- 수식 편집기 준비 실패가 사용자 입력을 차단하지 않도록 전체
  `pointer-events: none` 설정을 제거했다.
- RC47이 이미 남긴 pending marker, `pointer-events`와 `aria-busy`는 새
  스크립트 실행 시 즉시 원래 값으로 복구한다.
- 수식 편집기가 실제로 준비되기 전에는 `기본`과 `입력기` 경로를 유지한다.
  준비가 확인된 뒤에만 기존 수식 전용 화면 정리를 적용한다.
- `.mq-editable-field ~ button`과 절대 위치 형제 버튼 전체 숨김 CSS를
  제거해 루트·분수·파이 및 사이트 자체 보조 버튼을 보존한다.
- MathQuill 편집기는 최소 높이 56px, 최소 폭 220px, 22px 글자와
  `pointer-events: auto`를 적용한다.
- toolbar가 편집기 scope 밖에 지연 생성되는 경우에도 보이는
  루트·분수·파이 묶음을 찾아 방향 키패드를 연결한다.
- Web 계약을 `web-2026-07-30.2`, 버전을
  `0.4.0-rc48`/code 65로 올렸다.

### 검증

- Web POC JVM 단위시험·debug assemble·lint: 통과
- Android 13 DOM 계약 계측시험 45개: 실패·오류 0
  - 수식 편집기 준비 중에도 scope 터치 유지
  - RC47 pending scope의 pointer/ARIA 원복
  - 편집기 최소 높이 56px
  - 루트·분수·파이 표시·pointer·click 유지
  - 방향 키패드 생성
- Android 13 복구 계측시험 35개: `OK`
  - 최초 도구 호출은 3분 제한을 넘겼으나 기기 실행은 계속됐고
    245.392초에 35개 전부 통과한 결과를 확인했다.
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과

### A 기기 보존형 설치

- 설치 전 Web POC: `0.4.0-rc47`/code 64
- 설치 후 Web POC: `0.4.0-rc48`/code 65
- artifact/설치 APK SHA-256:
  `939351DAA3F6E6EC822000CD704CD489A09700BF5613068776E9F19E706FA9AF`
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- `adb install -r` 성공. firstInstallTime `2026-07-28 13:12:16`, 앱 데이터,
  credential bridge 권한, Kiosk Device Owner와 전용 HOME을 보존했다.
- 설치 APK를 기기에서 직접 `sha256sum`한 값이 artifact와 일치했다.
- 설치 뒤 Kiosk가 전경이고 기존 Web POC 프로세스가 종료됐음을 확인했다.
- 실제 사이트의 주관식 숫자·루트·분수·파이·방향 이동과 제출→안전 종료
  종단간 동작은 사용자가 실물에서 재확인한다.

---

## Web POC RC50 주관식 보조 UI 노출 조건 — 2026-07-30

### 사용자 확인과 원인

- RC48 설치 뒤 주관식 터치 불가와 제출 장애는 재현되지 않았다.
- RC48에서 과도한 형제 버튼 숨김 규칙을 제거하면서 숫자 입력 뒤 Matholic이
  편집기 옆에 생성하는 회색 지우기 버튼까지 다시 노출됐다.
- 방향 키패드는 수식 편집기 DOM이 존재하기만 하면 `display: grid`로
  생성됐다. 답안제출로 전체답안 화면이 열리면서 다른 번호의 주관식
  편집기가 DOM에 나타나도 키패드가 표시되는 조건이었다.

### 수정

- 수식 편집기의 뒤쪽 형제 범위에서만 다음 입력 지우기 control을 식별해
  숨긴다.
  - `×`, `✕`, `지우기`, `삭제`, `입력 지우기`
  - clear/close/remove/delete 계열 aria-label, title 또는 class
  - 편집기 바로 뒤의 빈 로컬 보조 버튼
- 루트·분수·파이, 입력기와 제출 버튼은 명시적으로 제외한다.
- 방향 키패드는 기본 `display: none`으로 생성한다.
- 사용자가 `.mq-editable-field`를 직접 pointerdown, click 또는 focusin한
  경우에만 활성화한다.
- 수식 편집기와 키패드 밖을 pointerdown하면 즉시 숨긴다. 따라서 답안제출
  터치 시 전체답안에 주관식이 포함되어 있어도 키패드는 나타나지 않는다.
- Web 계약을 `web-2026-07-30.3`, 버전을
  `0.4.0-rc50`/code 67로 올렸다.

### 검증과 설치

- Web POC JVM 단위시험·debug assemble·lint: 통과
- Android 13 DOM 계약 계측시험 46개: 실패·오류 0
  - 회색 지우기 버튼 국소 숨김
  - 루트·분수·파이 보존
  - 직접 입력 전 키패드 숨김
  - 직접 입력 중 키패드 표시
  - 바깥 영역과 답안제출 터치 시 키패드 숨김
- 복구 코드는 변경하지 않았으며 RC48에서 통과한 복구 계측시험 35개는
  이번 변경에서 재실행하지 않았다.
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- RC49 설치 뒤 지우기 control 탐색 범위를 한 번 더 좁혔다. 기존 RC49
  artifact와 payload가 달라 릴리스 불변성 검사가 덮어쓰기를 차단했으며,
  버전을 RC50으로 올려 새 artifact로 검증·설치했다.
- A에 `adb install -r`로 Web POC `0.4.0-rc50`/code 67을 설치했다.
- artifact/설치 APK SHA-256:
  `DC5BBAD1F94201BC740BBA72283874D131931F80351F433179981654D0794EFF`
- firstInstallTime, 앱 데이터, credential bridge 권한, Kiosk Device Owner와
  전용 HOME을 보존했다. 설치 뒤 Kiosk 전경을 확인했다.
- 실제 사이트 화면은 사용자가 주관식 숫자 입력과 답안제출에서 재확인한다.

---

## Web POC RC52 미초기화 전체답안 주관식 크기 — 2026-07-30

### 사용자 확인과 원인

- RC50에서 주관식 터치 불가, 회색 지우기 control과 방향 키패드 노출 조건은
  대체로 해결됐다.
- 일부 주관식은 문제 화면에서 한 번도 입력하지 않은 채 전체답안을 열면
  입력란이 좁고 터치되지 않았다.
- 같은 문제를 문제 화면에서 입력하거나 입력 후 지우면 전체답안 입력란도
  정상화됐다. 이는 MathQuill 편집기 클래스와 display 초기화가 문제 화면의
  첫 상호작용에 지연되어 있음을 보여준다.
- RC50의 220×56px 규칙은 `.mq-editable-field`에만 적용됐다. 초기화 전
  편집기가 `.mq-math-mode` 또는 내부 `.mq-textarea`만 가진 인라인
  요소이면 `min-height`가 실제 터치 박스 높이에 반영되지 않았다.

### 수정

- `.mq-textarea`를 기준으로 초기화 전 편집기 root를 역추적한다.
- `.mq-editable-field`, `.mq-math-mode`, MathQuill class 및 주관식
  placeholder input에 다음 값을 적용한다.
  - `display: inline-block`
  - 최소 폭 220px, 최소 높이 56px
  - `pointer-events: auto`, `touch-action: manipulation`
  - 22px 글자와 1.4 line-height
- Ant input wrapper는 기존 display 방식을 보존하면서 최소 220×56px와
  visible overflow만 보장한다.
- 가장 가까운 `answer-input-form-*` scope는 최소 높이 64px와 visible
  overflow를 보장한다.
- 초기 DOM뿐 아니라 MutationObserver가 감지한 지연 생성·교체 편집기에도
  같은 보정을 반복 적용한다.
- 표시 중인 편집기만 `inline-block`으로 전환한다. 원래 `display:none` 또는
  `visibility:hidden`인 비활성 편집기는 노출하지 않는다.
- Web 계약을 `web-2026-07-30.4`, 버전을
  `0.4.0-rc52`/code 69로 올렸다.

### 검증과 설치

- 높이 4px, 폭 12px, `display:inline`인 미초기화 전체답안 주관식 fixture를
  추가했다. 문제 화면 선행 입력 없이 실제 크기 220×56px 이상, scope
  64px 이상, pointer와 click 동작을 확인했다.
- Web POC JVM 단위시험·debug assemble·lint: 통과
- Android 13 DOM 계약 계측시험 47개: 실패·오류 0
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- 복구 코드는 변경하지 않아 RC48에서 통과한 복구 35개는 재실행하지 않았다.

## 수업 편의성 묶음 RC43/RC63 — 2026-07-30

### 반영

- `월1`~`토2` 고정 12개 반을 누락 시 자동 생성하고 2×6 빠른선택 버튼으로
  표시한다. 고정 반은 삭제하지 못하며 별도 테스트반은 기존 방식으로
  생성·삭제할 수 있다.
- 수업 시작 전 Device Owner·Lock Task 정책, 카메라, 배터리, 저장 공간,
  프린터 확인 안내를 표시한다. 보안 정책 실패는 차단하고 카메라 실패는
  PIN 인증 후 현재 수업 소속·보강 학생만 수동 선택할 수 있다.
- 선택 반 학생 전원의 QR을 원자적으로 회전하고 A4 한 장에 55×80mm 카드
  최대 9장을 배치하는 일괄 재발급·인쇄를 추가했다. 실행 전 기존 카드
  무효화를 명시적으로 확인한다.
- 문제 번호 배지 옆에 접이식 답안 현황 지도를 추가했다. 답변, `모름`,
  현재 문제를 구분하고 `모름`은 완료 답변으로 집계한다.
- 답안 제출 앞에 “풀지 못한 문제는 빈칸으로 두지 말고 ‘모름’으로
  입력” 안내를 표시한다.
- 네트워크·TLS·HTTP 연결 실패는 전용 답안 보호 화면과 비식별 상태 코드로
  처리한다. 수업 시작 전 기존 Web 안전정리를 공식 접속·DOM 구조·로그인
  잔여 상태 사전점검 및 WebView 준비 단계에 사용한다.
- 주관식 키패드는 앱에 구현하지 않고
  `docs/KEYPAD_DESIGN_REVIEW.svg` 검수안만 추가했다.

### 검증과 A 설치

- Kiosk/Web JVM 단위시험: 통과
- Kiosk/Web Android Lint debug: 통과
- Android 13 Kiosk 전체 계측 42/42: 통과
- Android 13 Web 전체 계측 92/92: 통과
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- 릴리스 서명 SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- Kiosk APK SHA-256:
  `3EDD580A3FDD1ECCF367F39C40F2F010F9063328004B57CDA287E1E8D85F6AD4`
- Web APK SHA-256:
  `443CFD50280670D4B2D508124374EF1771E00808A8FDC62661F3CF8ABA82D316`
- A에 Kiosk `0.6.0-rc43`/code 48, Web `0.4.0-rc63`/code 80을
  `adb install -r`로 보존형 설치했다.
- Kiosk firstInstallTime `2026-07-24 12:52:28`, Web firstInstallTime
  `2026-07-28 13:12:16`을 보존했다.
- 설치 후 Device Owner, 전용 HOME, Lock Task `LOCKED`, Kiosk/Web
  allowlist와 Kiosk 전경을 확인했다.
- 설치 직후 FATAL EXCEPTION은 없었다. exit-info의 최신 Kiosk 종료는
  APK 교체 과정의 `USER REQUESTED/FORCE STOP`이다.
- 실제 학습지에서 답안 현황 지도, 저장 답안 재판독, 수동 학생 선택,
  반 전체 QR의 실제 프린터 출력은 현장 확인이 남는다.

## 화면 제품명 중립화 RC41/RC62 — 2026-07-30

### 변경

- Android 앱 라벨을 Kiosk는 `채점 관리`, Web은 `학습`으로 바꿨다.
- 관리자 헤더의 제품명과 `Gate 5`, Web 설정 화면의 `Web POC`와
  `Gate 3` 표기를 제거했다.
- `전용기기 잠금`과 `채점기가 잠겼습니다`처럼 키오스크 운용을 직접
  드러내는 상태 문구를 `보안 적용`, `화면이 잠겼습니다` 등으로 중립화했다.
- QR 모양 앱 아이콘을 일반 문서 확인 아이콘으로 교체했다.
- 패키지명, Device Owner, 전용 HOME, allowlist와 Lock Task 보안 경계는
  변경하지 않았다.
- release 검증이 APK 리소스의 `app_name`을 직접 읽어 Kiosk `채점 관리`,
  Web `학습`을 강제하도록 보강했다. Windows CP949 환경에서도 한글 라벨을
  정확히 검사하도록 `apkanalyzer` 출력 인코딩을 UTF-8로 고정했다.

### 검증과 A 설치

- 중립화 1차 변경 기준 Android 13 Kiosk 계측 39개와 Web 계측 91개,
  합계 130개: 실패·오류 0.
- 마지막 상태 문구 중립화 후 release 단위시험·lint·두 APK assemble
  158 tasks와 저장 전·후 APK 서명/버전/권한/앱 라벨 검증: 통과.
- Kiosk `0.6.0-rc41`/code 46, Web `0.4.0-rc62`/code 79를 A에
  보존형 설치했다.
- Kiosk firstInstallTime `2026-07-24 12:52:28`, Web firstInstallTime
  `2026-07-28 13:12:16`과 credential bridge 권한을 보존했다.
- A에서 Kiosk Device Owner, 두 앱 allowlist와 Lock Task `LOCKED`를
  재확인했다.
- 최종 release SHA-256:
  - Kiosk:
    `2F6388ECA4E60748186321E3A73C862CD2325450569708B5716B655B90916BC0`
  - Web:
    `F39A3F083B1D6C8E136ABA8F9CC6CC7BBB409CF87658522401F10BEE68E2DBA6`
- A에서 다시 추출한 설치 APK 두 개의 SHA-256이 각 release artifact와
  일치했고 Kiosk 전경도 확인했다.
- `FLAG_SECURE` 때문에 화면 캡처는 검게 저장되며, 에뮬레이터 UI 계층에서
  관리자 헤더 `채점 관리`와 기존 제품명 부재를 확인했다.

## Web POC RC59 문제 이동 UI와 학생 세션 밝기 — 2026-07-30

### 변경

- 학습 페이지의 이전·다음 문제 버튼을 각각 112×96px로 확대하고 아이콘도
  42px로 키웠다.
- 버튼 사이의 기존 Ant Select 번호 선택기를 제거하지 않고 좌상단의 진한
  `문제 n / 전체` 배지로 재배치했다. 배지 전체를 눌러 기존 번호 선택기를
  열 수 있으므로 직접 문제 이동 기능은 유지된다.
- 전체답안 모달이 열려 있는 동안 문제번호 배지를 숨겨 제출 화면을 가리지
  않게 했다.
- `ACTIVE` 학생 세션 진입 시 앱 창 밝기를 0.8로 적용한다. 로그아웃,
  잠금, 복구, 관리자 상태와 Activity 종료 때 앱 진입 전 창 밝기로
  복원한다. Android 시스템 밝기 설정은 변경하지 않는다.
- Web 계약을 `web-2026-07-30.11`, 버전을
  `0.4.0-rc59`/code 76으로 올렸다.
- A 설치 점검 중 삼성 Android 13의 `device_policy` dump에 Lock Task
  패키지 목록이 나오지 않아 생기던 검증 오탐을 수정했다. 검증 스크립트는
  실제 `LockTaskController.mLockTaskPackages`를 제공하는 activity dump를
  사용한다.

### 검증과 A 설치

- JVM 단위시험, debug AndroidTest compile, debug lint: 통과
- 신규 DOM 계측: 양쪽 112×96px, 좌상단 고정 번호 배지, 배지 터치로 기존
  selector 열기 확인
- 신규 Activity 계측: ACTIVE에서 0.8, LOCKED 전환 후 기존 밝기 복원 확인
- Android 13 Web POC 전체 계측 91개: 실패·오류·생략 0
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- A에 Web POC `0.4.0-rc59`/code 76을 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `790B6AA4D037BD723E9E3059864846B189508AAAC9310FD259741844D9A1BA23`
- firstInstallTime `2026-07-28 13:12:16`, credential bridge 권한,
  Kiosk RC39 Device Owner와 전용 HOME을 보존했다.
- 재부팅 후 Lock Task `LOCKED`와 allowlist
  `[com.local.matholickiosk.kiosk, com.local.matholickiosk.webpoc]`를
  확인했다.
- 실제 학습지에서 버튼 위치·문제번호 가시성·로그인/로그아웃 밝기 체감은
  사용자 실물 재확인 대상으로 둔다.

## Web POC RC60 실제 SVG 문제 이동 버튼 탐지 — 2026-07-30

### RC59 실기 실패와 원인

- 밝기 80% 적용·복원은 A 실기에서 정상 확인됐다.
- 문제 이동 버튼과 좌상단 번호 배지는 아무 변화가 없었다.
- 최신 공식 자산
  `/assets-260728/chunk-CNGK4PO3MhEoWkxF.js`의 `Gi` 컴포넌트를
  확인했다.
- 실제 이전·다음 Ant Button은 textContent, title과 aria-label이 모두
  없고 SVG 아이콘만 자식으로 가진다.
- RC59는 `<`, `>`, `이전 문제`, `다음 문제` 중 하나가 있어야 버튼으로
  인식했으므로 실제 컴포넌트에서는 탐지가 0건이었다.

### 수정

- 표시 중인 숫자 `.ant-select-selection-item`을 먼저 찾는다.
- 현재 번호와 전체 문제 수를 함께 가진 직접 조상에서 숫자 선택기 앞뒤의
  형제 요소를 따라가, 내부의 표시 중인 SVG 버튼을 각각 이전·다음으로
  식별한다.
- 식별 후 기존과 동일하게 112×96px 버튼과 좌상단 문제번호 배지를
  적용하며 aria-label도 이때 보강한다.
- 라벨 기반 탐지는 하위 호환 fallback으로 유지한다.
- Web 계약을 `web-2026-07-30.12`, 버전을
  `0.4.0-rc60`/code 77로 올렸다.

### 검증과 A 설치

- 실제 구조와 같은 라벨 없는 SVG 버튼, 숫자 Ant Select, 전체 문제 수
  fixture에서 양쪽 112×96px, 좌상단 번호 배지와 selector 열기: 통과
- JVM 단위시험, debug AndroidTest compile, debug lint: 통과
- Android 13 Web POC 전체 계측 91개: 실패·오류·생략 0
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- A에 Web POC `0.4.0-rc60`/code 77을 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `A4C23DFCBCECF7C6CE3076092F7F391CC770321B08CE6D632254D3B5DEA3F03F`
- firstInstallTime `2026-07-28 13:12:16`, credential bridge 권한,
  Kiosk RC39 Device Owner, Lock Task `LOCKED`와 두 앱 allowlist를
  보존했다.
- 실제 학습 페이지의 버튼·문제번호 표시는 사용자 실물 재확인 대상으로
  둔다.

## Web POC RC56 실패 편집기 React 답안 재연결 — 2026-07-30

### 사용자 재확인과 남은 원인

- RC55에서 답안제출창의 얇은 칸은 정상 크기로 복구됐다.
- 임시저장 후 재진입하면 문제 화면에는 기존 답안이 보이지만 답안제출창에는
  보이지 않고, 넓어진 칸에도 입력할 수 없었다.
- 이는 크기 문제가 아니라 제출창의 React 수식 컴포넌트가 존재한 상태에서
  내부 MathQuill ref만 `null`로 고정된 별도 장애다.
- RC55의 runtime 선로딩은 이후 mount 경쟁을 예방하지만 이미 mount에
  실패한 컴포넌트의 effect를 다시 실행시키지는 못했다.

### 수정

- 초기화 전 공식 `span`에서 React Fiber 상위 컴포넌트의 현재 `latex`와
  `onLatexChange` binding을 확인한다.
- MathQuill runtime이 준비된 뒤 실패한 `span`에만 새 MathField를 생성하고
  기존 임시답안을 `latex`로 복원한다.
- 새 편집기의 edit를 원래 React `onLatexChange`로 전달해 삭제와 입력이
  임시저장 대상 답안 상태에 반영되게 한다.
- 원래 ref가 `null`인 루트·분수·파이 버튼도 복구 MathField의 `cmd`와
  `focus`에 연결한다.
- 정상 `.mq-editable-field`, 숨겨진 편집기, 객관식과 일반 입력에는
  적용하지 않는다.
- Web 계약을 `web-2026-07-30.8`, 버전을
  `0.4.0-rc56`/code 73으로 올렸다.

### 검증과 A 설치

- 초기화 실패 편집기와 기존 임시답안 `28` fixture에서 다음을 확인했다.
  - MathField 생성 후 `28` 표시
  - 삭제 edit가 React 답안 상태 `null`로 전달
  - 루트 입력이 새 답안으로 전달되고 focus 복구
- JVM 단위시험·debug 앱·계측 APK compile: 통과
- Android 13 DOM 계약 계측시험 52개: 실패·생략 0
- Android 13 전체 계측시험 89개: 실패·생략 0
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- A에 Web POC `0.4.0-rc56`/code 73을 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `04E6E9520176D8917A9FC8D886AB6D2C456CE40C7CC5F2421320A9739CCA9F06`
- firstInstallTime `2026-07-28 13:12:16`, credential bridge 권한,
  Kiosk RC39 Device Owner와 전용 HOME을 보존했다.
- 설치 APK 해시가 artifact와 일치했고 Kiosk가 전경으로 복귀했다.

## Web POC RC58 답안창 초기 렌더 유예 — 2026-07-30

### RC57 실기와 원인

- 답안제출 버튼 직후 화면 구조가 비정상적으로 변했다.
- 창 계층을 읽은 결과 Web POC는 전경에서 살아 있었고 별도 시스템 팝업이나
  키보드가 화면을 덮은 상태는 아니었다.
- RC57은 답안창이 mount된 직후 MathQuill effect가 실행되기 전의 정상
  `span`까지 실패로 판정해 여러 수식 입력기를 동시에 재마운트했다.

### 수정과 검증

- 초기 공식 `span`마다 최초 관찰 시각을 기록한다.
- 1.5초 동안은 크기와 터치 영역만 보장하고 type 변경은 하지 않는다.
- 그 안에 `.mq-editable-field`가 생기면 실패 시각을 제거하고 끝낸다.
- 1.5초 뒤에도 표시 중이며 class 없는 개별 입력기만 RC57의 공식
  `EQ → ONE → EQ` 재마운트를 실행한다.
- Web 계약을 `web-2026-07-30.10`, 버전을
  `0.4.0-rc58`/code 75로 올렸다.
- 유예 직후 재마운트 0회, 지속 실패 뒤 재마운트 1회: 통과
- 기존 28 유지, 35 수정과 빈 값 삭제의 임시저장 배열 반영: 통과
- JVM 단위시험·debug 앱·계측 APK compile: 통과
- Android 13 DOM 계약 계측시험 52개: 실패·생략 0
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- A에 Web POC `0.4.0-rc58`/code 75를 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `B92DBC3900EADFF4855673B70F2945C4929577423B5B76895B5F7A228C7374DD`
- firstInstallTime `2026-07-28 13:12:16`, credential bridge 권한,
  Kiosk RC39 Device Owner와 전용 HOME을 보존했다.
- 설치 APK 해시가 artifact와 일치했고 Kiosk가 전경으로 복귀했다.

## Web POC RC57 공식 컴포넌트 재마운트와 임시저장 — 2026-07-30

### RC56 실기 결과와 확정된 상태 경로

- RC56에서도 임시저장이 정상 동작하지 않았다.
- 공식 최신 자산에서 문제 화면과 전체답안은 동일한 `userAnswers`와
  `updateUserAnswer`를 사용함을 확인했다.
- 정상 변경 경로는 `onChange → setUserAnswers → setTemp`이며 임시저장은
  이 `setTemp` 배열을 `saveTempAnswer`에 전달한다.
- RC56은 실패 DOM에 별도 MathField를 붙였기 때문에 공식 컴포넌트의
  내부 ref와 전체 생명주기를 되살리지 못했다.

### 수정

- 초기화 실패 `span`의 React 상위 컴포넌트에서 공식 `userAnswer`와
  `onChange`를 찾는다.
- 기존 `number`와 `value`를 그대로 유지한 채 `type: ONE`으로 전환한다.
- 실패 `span`이 실제 unmount된 것을 확인한 뒤 같은 값으로 `type: EQ`를
  적용해 공식 수식 컴포넌트를 새로 mount한다.
- 새 공식 컴포넌트가 MathQuill ref, 수식 toolbar, `userAnswers`와
  임시저장용 `setTemp` 배열을 모두 원래 경로로 연결한다.
- Web 계약을 `web-2026-07-30.9`, 버전을
  `0.4.0-rc57`/code 74로 올렸다.

### 검증과 A 설치

- 기존 답안 `28`을 유지한 공식 `EQ → ONE → EQ` 재마운트: 통과
- 재마운트 뒤 `userAnswers`와 임시저장 배열에 `28` 유지: 통과
- 이후 `35` 수정과 빈 값 삭제가 두 상태에 동일하게 반영: 통과
- JVM 단위시험·debug 앱·계측 APK compile: 통과
- Android 13 DOM 계약 계측시험 52개: 실패·생략 0
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- A에 Web POC `0.4.0-rc57`/code 74를 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `2972A4B7F378FCA1946EC481124D3A22A914733AC3DD02301A87D773FF2D16EF`
- firstInstallTime `2026-07-28 13:12:16`, credential bridge 권한,
  Kiosk RC39 Device Owner와 전용 HOME을 보존했다.
- 설치 APK 해시가 artifact와 일치했고 Kiosk가 전경으로 복귀했다.

## Web POC RC55 MathQuill 동시 초기화와 기존 답안 복원 — 2026-07-30

### 확정 원인

- 2026-07-30 현재 공식 학습 페이지의 수식 입력기는 MathQuill 초기화 전
  폭 160px, padding 8px인 일반 `span`이다. 이 단계에는 `.mq-*` class와
  내부 textarea가 모두 없어 RC54 selector가 문제 4를 찾을 수 없었다.
- 공식 MathQuill wrapper 여러 개가 동시에 mount되면 첫 wrapper가 jQuery
  또는 MathQuill script element를 만든 직후 다른 wrapper가 같은 id를
  발견한다. 공식 loader는 script의 실제 load 완료를 기다리지 않고 성공한
  것으로 처리해 일부 wrapper가 MathQuill instance 없이 영구 정지했다.
- 공식 wrapper는 편집기마다 첫 두 `edit` notification을 무시한다. 정상
  instance에는 기존 안정화 단계가 이 두 notification을 먼저 소비하지만,
  초기화되지 않은 문제 4에는 적용되지 않았다.
- 문제 4의 `28`은 키오스크가 주입한 값이 아니라 기존 Matholic
  `userAnswer.value`다. 초기화 실패로 사용자의 삭제가 React 답안 상태에
  전달되지 않아 기존 `28`이 다시 화면에 투영되고 임시저장에도 그대로
  남았다.

### 수정

- 학습 페이지에서 공식 `/js/mathquill/jquery-3.2.1.min.js` 로드 완료 후
  `/js/mathquill/mathquill.min.js`를 순차 로드한다.
- script element 존재가 아니라 `window.jQuery`와 `window.MathQuill`
  실제 생성을 확인하며, 페이지 전체가 하나의 선로딩 Promise를 재사용한다.
- 공식 초기화 전 일반 `span`의 inline style, 상대 위치 wrapper와
  루트·분수·파이 toolbar 조합을 식별해 최소 220×56px 터치 영역을
  즉시 보장한다.
- 초기화 전 shell에 겹친 회색 clear control도 숨긴다.
- Web 계약을 `web-2026-07-30.7`, 버전을
  `0.4.0-rc55`/code 72로 올렸다.

### 검증과 A 설치

- 공식 초기화 전 class/textarea 없는 160px `span` fixture가 실제
  220×56px 이상으로 확장되고 clear control이 숨겨짐: 통과
- jQuery→MathQuill 순차 선로딩과 Promise 재사용 회귀시험: 통과
- 기존 답안 `28`을 보존한 채 무시되는 첫 두 edit를 소비하고, 이후 사용자
  삭제가 React 답안 상태를 빈 값으로 갱신하는 회귀시험: 통과
- JVM 단위시험·debug assemble·계측 APK compile: 통과
- Android 13 DOM 계약 계측시험 51개: 실패·오류·생략 0
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- A에 Web POC `0.4.0-rc55`/code 72를 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `165C3C0E15483C548E46F674229521050ADCDE793246FF569B3E045C141FA359`
- firstInstallTime `2026-07-28 13:12:16`, credential bridge 권한,
  Kiosk RC39 Device Owner와 전용 HOME을 보존했다.
- 설치 APK 해시가 artifact와 일치했다.
- 복구 코드는 변경하지 않아 RC48에서 통과한 복구 35개는 재실행하지 않았다.

## Web POC RC54 textarea 전 MathQuill 중간 상태 — 2026-07-30

### 사진으로 확인한 현상

- 문제 4는 루트·분수·파이 도구가 보이지만 입력칸이 한 줄 높이로 접혀
  있었고 회색 반원형 지우기 버튼이 아래로 겹쳤다.
- 바로 아래 문제 5는 caret와 충분한 높이를 가진 정상 MathQuill 입력기였다.
- 문제 4는 `.mq-math-mode` shell은 생성됐지만 내부 `.mq-textarea`가 아직
  없는 초기화 중간 상태로 판단했다.

### 수정과 검증

- `.mq-textarea` 역추적에 의존하지 않고 모든 `.mq-math-mode`와
  `.mq-editable-field`를 직접 주관식 터치 대상으로 수집한다.
- textarea가 없는 shell도 `inline-block`, 최소 220×56px,
  pointer-events auto를 적용한다.
- 같은 중간 상태의 editor 뒤에 붙은 clear control도 제거 대상으로
  확장한다.
- 사진 상태와 같은 12×4px `.mq-math-mode`, textarea 없음, 겹친 clear
  control fixture를 추가했다.
- 신규 fixture가 실제 220×56px 이상으로 확장되고 clear control이
  숨겨지는 것을 확인했다.
- JVM 단위시험·debug assemble·계측 APK compile: 통과
- Android 13 DOM 계약 계측시험 49개: 실패·오류 0
- Web 계약은 `web-2026-07-30.6`, 버전은
  `0.4.0-rc54`/code 71이다.
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- A에 Web POC `0.4.0-rc54`/code 71을 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `D8B6BC4F030594AFFAC41F6C21EAF258EB4274D8517C5A826A396159EB21B741`
- firstInstallTime `2026-07-28 13:12:16`, credential bridge 권한,
  Kiosk RC39 Device Owner와 전용 HOME을 보존했다.
- 설치 후 Kiosk가 전경으로 정상 복귀했고 설치 APK 해시가 artifact와
  일치했다.
- 복구 코드는 변경하지 않아 RC48에서 통과한 복구 35개는 재실행하지 않았다.

- A에 Web POC `0.4.0-rc52`/code 69을 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `A03F189545EAFC133821DC6EC54C3C94215AC01337E2CC1C5910C3266CAC1A34`
- firstInstallTime, 앱 데이터, credential bridge 권한, Kiosk Device Owner와
  전용 HOME을 보존했다. 설치 APK 해시 일치와 Kiosk 전경을 확인했다.
- 실제 사이트에서는 문제 화면에서 건드리지 않은 주관식이 포함된 학습지로
  전체답안을 바로 열어 입력란 크기와 터치를 재확인한다.

## Web POC RC53 특정 전체답안 주관식 지연 초기화 — 2026-07-30

### 현상과 원인

- 전체답안의 일부 문제는 `answer-input-form-*` 자식 입력기가 아직 생성되지
  않은 상태로 표시되어 RC52의 자식 기반 크기 보정을 받지 못했다.
- DOM mutation 없이 스크롤·CSS 표시 상태만 바뀌는 경우 MutationObserver가
  재실행되지 않아 뒤늦게 표시된 입력기 메뉴와 수식 편집기가 초기화되지
  않았다.

### 수정

- 모든 `answer-input-form-*` 자체에 입력기 자식 생성 전부터 box sizing,
  최소 220px 폭·64px 높이, 최대 폭 100%, visible overflow를 적용한다.
- answer scope 안의 일반 input·textarea·contenteditable·textbox도
  placeholder 문구에 의존하지 않고 주관식 터치 대상으로 보정한다.
- 문서 scroll, viewport resize, orientationchange를 requestAnimationFrame으로
  묶어 표시 상태가 바뀐 답안칸을 재처리한다.
- 숨겨진 편집기와 MathQuill 내부의 의도적으로 작은 textarea는 확대하지
  않는다.
- Web 계약을 `web-2026-07-30.5`, 버전을
  `0.4.0-rc53`/code 70으로 올렸다.

### 검증

- CSSOM으로 숨김 상태를 해제해 MutationObserver가 감지하지 못하는 fixture를
  추가했다. scroll 재처리 후 입력기 메뉴가 초기화되고 편집기가 실제
  220×56px 이상, pointer-events auto가 되는 것을 확인했다.
- JVM 단위시험·debug assemble·계측 APK compile: 통과
- Android 13 신규 회귀시험: 통과
- Android 13 DOM 계약 계측시험 48개: 실패·오류 0
- release 단위시험·lint·두 APK assemble 158 tasks 및 APK 이중 검증:
  통과
- A에 Web POC `0.4.0-rc53`/code 70을 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `2CF1078E942B3181EA28490EFE09CD1E85F1F3E9DD9B8579E7A6366240D88653`
- firstInstallTime `2026-07-28 13:12:16`, credential bridge 권한,
  Kiosk `0.6.0-rc39` Device Owner와 전용 HOME을 보존했다.
- 설치 후 Kiosk가 전경으로 정상 복귀했고 설치 APK 해시가 artifact와
  일치했다.
- 복구 코드는 변경하지 않아 RC48에서 통과한 복구 35개는 재실행하지 않았다.

## Web POC RC65 하단 주관식 수식 키패드 — 2026-07-30

### 반영

- 검수안 `docs/KEYPAD_DESIGN_REVIEW.svg`의 세 구역을 실제 학습 페이지
  하단 고정 패널로 구현했다.
- 숫자 구역은 가운데 정렬된 4×3 배열
  `1 2 3 부호 / 4 5 6 . / 7 8 9 0`을 유지한다.
- 수식 구조는 루트·분수·파이만 제공하고, 편집 구역은 상하좌우 이동,
  한 칸 삭제와 2회 확인형 전체 지움을 제공한다.
- 세 구역의 제목 폭과 본문 폭을 일치시키고 구역 사이 간격을 같은 값으로
  유지한다. 숫자·수식·편집 구역 비율은 검수안의 480:350:250이다.
- 편집기를 사용자가 직접 터치했을 때만 열고, 프로그램 focus·답안제출
  진입만으로는 열지 않는다. 바깥 영역 또는 답안제출을 터치하면 닫힌다.
- 키패드가 정상 생성된 경우에만 Android 기본 키보드를 억제한다. 공식
  toolbar 또는 MathQuill 연결을 찾지 못하면 기존 입력 방식을 유지한다.
- `입력기` 버튼 유무에 의존하지 않고, 표시 중인 MathQuill 편집기와
  루트·분수·파이 도구만 있어도 키패드를 준비한다.
- 키패드가 입력칸을 가리면 가까운 내부 스크롤부터 이동하고, 그 영역의
  끝에 도달하면 남은 이동량을 상위 스크롤과 페이지에 전달한다.
- Web DOM 계약은 `web-2026-07-30.13`, 버전은
  `0.4.0-rc65`/code 82이다.

### 자동 검증

- Android 13 DOM 계약 계측시험 56/56 통과:
  - 키 21개와 4×3 숫자 배열
  - 세 구역의 좌우 간격 동일
  - 숫자 제목과 숫자 grid 좌우선 일치
  - 직접 터치 전 숨김, 직접 터치 시 표시, 바깥·제출 터치 시 숨김
  - 숫자·부호·소수점·루트·분수·파이·방향·삭제·전체 지움 동작
  - 키패드 생성 실패 시 기본 입력 유지
  - 중첩 스크롤의 끝에서도 편집기와 키패드 사이 15px 이상 확보
- `scripts/build.ps1`: 네 모듈 JVM 113개, debug lint와 네 APK assemble,
  204 tasks 통과. JVM 실패·오류·생략 0, lint 오류 0.
- `scripts/build-release.ps1`: Kiosk/Web JVM 시험, release lint,
  release APK assemble와 저장 전·후 APK 검증, 158 tasks 통과.
- 릴리스 signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- Kiosk APK SHA-256:
  `3EDD580A3FDD1ECCF367F39C40F2F010F9063328004B57CDA287E1E8D85F6AD4`
- Web APK SHA-256:
  `58F92D218A1757259CAD21CCC9A74B288CE9B4489F32875FCC73299B1F2736AD`

### A 설치 상태

- A에 Web `0.4.0-rc65`/code 82 release APK를 `adb install -r`로
  보존형 설치했다.
- 설치 전 임시 계측용 RC64 debug의 `DEBUGGABLE` flag가 설치 후
  제거됐음을 확인했다.
- Web firstInstallTime `2026-07-28 13:12:16`과
  `ceDataInode=28569`를 보존했다.
- Kiosk `0.6.0-rc43`, Device Owner, 전용 HOME과 Lock Task
  `LOCKED`를 유지했고 Kiosk가 전경임을 확인했다.
- 계측시험 패키지 `com.local.matholickiosk.webpoc.test`는 제거했다.

### 제한과 현장 확인

- 전체 `RecoveryInstrumentedTest`는 실제 프록시/WebView 준비 단계가 길어
  제한시간 안에 완료되지 않았다. 밝기 적용·복원 단일 계측은 통과했으며,
  전체 복구 suite 통과로 보고하지 않는다.
- 실제 학습지에서 주관식 입력칸 직접 터치, 모든 키, 임시저장 후 재진입,
  전체답안의 저장 답안 표시·수정과 키패드 위치는 사용자 확인이 남는다.
- Kiosk 계측시험은 실제 Room 학생·반 데이터를 건드릴 수 있어 최종 설치
  단계에서 A에 재실행하지 않았다. 편의성 구현 시점의 Kiosk 42/42 통과와
  최종 JVM·lint·release 빌드 통과를 유지 근거로 사용한다.

## 승인된 편의성 19개 묶음 RC44/RC66 — 2026-07-30

### 구현

- PC 실시간 상태·로컬 알림과 암호화 CSV 전달
- 운영 자가진단·원버튼 복구, QR 품질 진단과 인증 취소
- 10분 무입력 경고와 통신 단절 시 답안 유지 안전 대기
- 미입력 문항 순회, 수식 입력 실행취소·다시실행, 삭제 길게 누르기
- 오른손·왼손·하단 중앙 키패드 프리셋과 가변 터치영역
- 소리·진동 피드백, CSV 학생 등록·수정, 관리자 30초 실행취소
- 신규·변경 카드만 PDF 생성, 재발급 영향 미리보기와 카드 상태·이력
- 사용자 UI나 내보내기 기능이 없는 크기 제한 private 진단 로그

### 자동 검증

- Android 13 에뮬레이터 Web 전체 계측: 94/94 통과
- Kiosk 전체 계측 첫 실행: 44개 중 43개 통과, 새
  `ACCESS_NETWORK_STATE` 권한을 기존 테스트가 금지해 1개 실패
- 권한 계약 수정 후 해당 Kiosk 계측: 1/1 통과
- 권한 계약 수정 후 Kiosk 전체 재실행: 44/44 통과
- Kiosk/Web JVM 단위시험과 debug APK assemble: 통과
- PC 수신기 프로토콜·서버 시험: 10/10 통과
- `scripts/build-release.ps1`: 158 tasks 통과
  - Kiosk/Web JVM 시험
  - 양쪽 release lint
  - 서명 release APK assemble
  - 저장 전·후 버전·권한·비디버그·서명 검증
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- Kiosk `0.6.0-rc44` SHA-256:
  `2E4DB988487ED536583273D331DF3C85575D28BBD8BD2D9F34F24B8D8AB2137B`
- Web `0.4.0-rc66` SHA-256:
  `CD488C8CB441B3792762CB443D34552E841A776F4DC2E7F1E4E4471BC8768CCF`

### 미검증·설치 상태

- A 기기에는 rc44/rc66을 설치하지 않았다. 기존 앱·학생·반·QR 데이터와
  Device Owner 상태를 건드리지 않았다.
- 실제 Matholic 학습지에서 통신 단절·10분 경고·미입력 순회·길게 삭제·
  세 키패드 프리셋의 현장 조작은 사용자 접근 가능 시 확인이 필요하다.
- 실물 QR 인쇄와 실제 프린터 시험은 수행하지 않았다.

## 배제 기능용 최소 진단 정책 RC67 — 2026-07-30

### 변경

- 6·7·10·29번은 사용자 기능으로 계속 배제한다.
- 앱 private 진단 로그에 다음 비식별 구조 정보만 추가했다.
  - 비정상 재시작에서 발견한 Web 세션 단계와 복구 시작
  - 8초 이상 걸린 로그인·준비·로그아웃·복구 단계
  - 기존 자동 재시도 단계·횟수와 타임아웃
  - `RESULT_INCOMPLETE`의 허용된 실패 유형, 예상 문항 수와 판독 수
- 실제 문제 풀이 `ACTIVE` 시간은 느린 단계 기록에서 제외한다.
- 답안·정답·점수·틀린 문제 번호·문항 내용·학생 식별정보는 기록하지
  않는다.

### 검증

- 신규 진단 정책 JVM 시험:
  - 중단 단계는 복구 대상 상태만 허용
  - 8초 미만 처리와 실제 문제 풀이 시간은 기록하지 않음
  - 결과 실패 사유 allowlist와 구조적 개수 0~999 제한
- Web 전체 JVM 시험과 debug APK assemble: 통과
- `scripts/build-release.ps1`: 158 tasks 통과
  - Kiosk/Web JVM 시험
  - 양쪽 release lint
  - 서명 release APK assemble
  - 저장 전·후 버전·권한·비디버그·서명 검증
- Kiosk `0.6.0-rc44` payload는 이전 릴리스와 동일하여 기존 산출물을
  보존했다.
- Web `0.4.0-rc67` SHA-256:
  `54059A07FE76B563AF857AEC9CB28E1C6DB18206A56FA604A2A4FBD2F23857E2`
- signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### 미검증

- A 기기에는 rc67을 설치하지 않았다.
- 실제 장애를 강제로 발생시키는 네트워크 차단·프로세스 중단·결과 판독
  실패주입은 사용자 부재와 기존 수업 데이터 보호를 위해 실행하지 않았다.

## A 기기 보존 설치 RC44/RC67 — 2026-07-30

### 설치

- 대상: A / SM-P610 / `R54TB029FHZ`
- Web `0.4.0-rc67`(code 84)을 `adb install --no-streaming -r`로 설치했다.
- Kiosk `0.6.0-rc44`(code 49)을 `adb install --no-streaming -r`로 설치했다.
- 앱 데이터 삭제, Device Owner 변경, 기기 재부팅과 QR 재발급은 수행하지
  않았다.

### 설치 직후 자동 검증

- 두 APK 모두 설치 명령이 `Success`로 완료됐다.
- Kiosk:
  - `firstInstallTime` `2026-07-24 12:52:28` 유지
  - `ceDataInode` `3236` 유지
- Web:
  - `firstInstallTime` `2026-07-28 13:12:16` 유지
  - `ceDataInode` `28569` 유지
- Kiosk Device Owner, 전용 HOME, `LOCKED` Lock Task가 유지됐다.
- Kiosk가 최상위 resumed activity로 복귀했다.
- 설치에 따른 Kiosk 프로세스 교체 기록은 Android의
  `USER REQUESTED / FORCE STOP`이며 앱 crash 기록이 아니다.

### 현재 상태와 미검증

- 설치 직후 상태는 `RECOVERY_REQUIRED`다. 실행 중 Kiosk 패키지를 보존
  설치하면서 프로세스가 교체된 데 따른 예상 안전 복구 상태다.
- 관리자 PIN을 입력하는 수동 복구는 사용자 부재 중 수행하지 않았다.
- 기존 학생·반·QR의 화면상 보존과 신규 편의 기능의 실제 현장 동작은
  [현장 검증 체크리스트](RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md)에 따라
  검증해야 한다.

## 자동/현장 검증 분리와 RC45/RC68 교정 — 2026-07-31

### 검증 책임 분리

- 현장 체크리스트 168개 항목을 빠짐없이 분류했다.
  - `자동` 13개
  - `공동` 49개
  - `사용자` 106개
  - 담당 미지정 0개
- 자동 13개 중 현재 상태에서 판정 가능한 12개를 완료했다.
- 남은 자동 1개는 모든 현장 흐름 종료 뒤의 `최종 오류 코드 없음`이므로
  사용자·공동 흐름을 마치기 전에는 완료로 표시하지 않는다.
- 사용자 항목 106개는 서로 독립된 106번 조작이 아니라 여섯 개 실제
  사용 흐름에서 묶어 확인하도록 재구성했다.

### 발견·교정한 결함

- 설치돼 있던 Windows PDF 수신기가 현재 소스보다 오래된 산출물이었다.
  최신 산출물로 보존 업데이트하고 기존 receiver ID, 페어링 secret, 포트와
  수신 폴더를 유지했다.
- 수신기 EXE의 `--smoke-check`가 즉시 종료할 뿐 실제 전송을 검사하지 않는
  결함을 수정했다. 이제 암호화 PDF 전송, 인증 ACK, 저장 내용과 임시 파일
  정리까지 실제 실행한다.
- 수신기 버전을 `0.1.1`로 올리고 엔트리포인트 회귀시험을 추가했다.
- Kiosk와 Web의 private 진단 로그는 사용자 UI가 없고 release 앱에
  `run-as`도 사용할 수 없어 Codex가 장애 분석에 회수할 통로가 없었다.
  시스템 `android.permission.DUMP`로 보호된 ADB shell 전용 receiver를
  추가했다.
- 진단 출력은 대문자 16진수 nonce, 허용된 구조의 최근 최대 200줄/파일만
  통과시킨다. 학생 식별정보·자격정보·QR·답안·점수·오답 번호·문항
  내용은 구조적으로 출력 대상에서 제외한다.
- 릴리스 검사기에 두 receiver의 존재, `exported=true`,
  `android.permission.DUMP`와 정확한 action 이름을 강제했다.
- 운영 문서의 진단 action 이름과 PowerShell nonce 예시를 실제 계약과
  일치하도록 교정했다.

### 자동 검증

- `scripts/build.ps1`: 204 tasks, PASS
  - 네 Android 모듈 JVM 시험
  - debug lint
  - debug APK assemble
- `scripts/build-release.ps1`: 158 tasks, PASS
  - Kiosk/Web JVM 시험
  - release lint
  - 서명 release APK assemble
  - 산출물 저장 전·후 이중 검증
- Android 13 에뮬레이터 전체 계측:
  - Kiosk 44/44, PASS
  - Web 94/94, PASS
- Windows PDF 수신기:
  - pytest 11/11, PASS
  - versioned EXE와 설치 EXE의 실제 smoke check, PASS
  - smoke PDF 잔류 없음
- release 검증:
  - version, permission, `debuggable=false`, zipalign, v2 서명과 signer 1,
    두 APK 동일 signer, ADB 진단 receiver 보안 계약, PASS
  - signer SHA-256:
    `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- 첫 릴리스 검증 시 XML 속성 순서에 의존한 신규 검사식이 실패했다.
  receiver 블록을 분리해 각 속성을 독립 검사하도록 수정한 뒤 전체 릴리스
  빌드에서 통과했다.

### A·PC 비파괴 검증

- A 설치본:
  - Kiosk `0.6.0-rc45` / code 50
  - Web `0.4.0-rc68` / code 85
- artifact와 A 설치 APK SHA-256:
  - Kiosk:
    `A882C4D371D45B927E67CF7B1B011D6B590EDC4BFFE963D425B23347E9077A83`
  - Web:
    `D04F149D8B1A4708719BD0E9678980E507B07477AA8EF4C5FE0333B91CF08B3A`
- `firstInstallTime`과 `ceDataInode`가 설치 전후 동일하다.
  - Kiosk: `2026-07-24 12:52:28`, inode `3236`
  - Web: `2026-07-28 13:12:16`, inode `28569`
- Device Owner, 전용 HOME, `LOCKED`, Kiosk top resumed activity 유지: PASS
- 카메라 2개, Kiosk 카메라 권한, USB 전원과 배터리 기준 상태: PASS
- ADB private 진단:
  - Kiosk/Web 각각 `BEGIN`·`END`, 총 경계 4개
  - 현재 저장된 허용 이벤트 0개
  - 금지 정보 패턴 0개
- Windows PDF 수신기:
  - TCP 48129 listen, Startup 바로가기, 실행 경로: PASS
  - 방화벽 `Private`/Inbound/TCP 48129/해당 EXE만 허용: PASS
  - artifact와 설치 EXE SHA-256:
    `8F1F95543C76AEDE16CF845760B123743693467F78B273A6D37C4B38695F50B4`

### 남은 현장 검증

- A는 보존 설치 뒤 예상된 `RECOVERY_REQUIRED` 상태다.
- 관리자 PIN 안전 복구, 기존 학생·반·QR 화면 확인, Matholic 실서버
  로그인·풀이·제출, 실물 QR·소리·진동·터치 체감은 자동 판정하지 않았다.
- 다음 사용자 접근 시
  [RC47/RC68 현장 검증 체크리스트](RC47_RC68_FIELD_VERIFICATION_CHECKLIST.md)의
  네 흐름을 수행한다.
- 공장초기화, 앱 데이터 삭제, Device Owner 변경, QR 재발급, 실물 인쇄와
  실패주입은 이번 자동 검증에서 수행하지 않았다.

## Kiosk RC47·Windows 수신기 0.1.3와 S1 현장 검증 — 2026-07-31

### DHCP 주소 변경 뒤 지정 PC 복구

- PC 재부팅 뒤 주소가 기존 `192.168.0.224`에서 `192.168.0.230`으로
  바뀌어 A의 지정 PC 연결이 실패하는 현상을 실제 확인했다.
- 저장된 PC와 같은 사설 `/24` 범위에서 인증된 상태 요청에 성공한
  endpoint만 다시 저장하도록 Kiosk를 보강했다.
- 탐색 상태에는 학생 이름과 알림 요청을 싣지 않고, 저장된 pairing secret의
  인증에 성공한 수신기만 채택한다.
- A에서 `PC_ENDPOINT_RECOVERED` 진단과 `.230` endpoint 복구를 확인했고,
  운영 준비 자가진단 8개 항목이 모두 통과했다.
- 구현 커밋: `ca32740`

### Windows 수신기 1080p UI

- 1080p 화면에서 큰 QR 미리보기 때문에 CSV 대기열 버튼이 창 아래로
  잘리는 현상을 확인했다.
- QR 미리보기를 240px로 줄이고 CSV 제어를 수신 폴더·상태보다 위로
  이동했다.
- Windows 수신기 `0.1.3`을 설치했고 receiver ID, pairing secret,
  포트와 수신 폴더는 유지했다.
- pytest 11/11과 PyInstaller 실행 파일 smoke check를 통과했다.
- source/설치 EXE SHA-256:
  `48E9DCF5182F15084DF48E53DB6FA9479F60A5938B32CD4B6EDACF508CB48DE3`
- 구현 커밋: `c1785cc`

### CSV 버튼 상태 경쟁과 Kiosk RC47

- 자가진단 성공 뒤에도 CSV 가져오기 버튼이 비활성으로 남는 현상을
  재현했다.
- 관리자 데이터 조회와 PC pairing 상태 조회의 완료 순서가 바뀌면
  pairing 성공 뒤 학생 관리 control을 다시 계산하지 않는 경쟁 조건이
  원인이었다.
- pairing 조회 성공·실패 뒤 control 상태를 다시 계산하도록 수정했다.
- A에 Kiosk `0.6.0-rc47`/code 52를 보존 설치했다.
  - `firstInstallTime` `2026-07-24 12:52:28` 유지
  - `ceDataInode` `3236` 유지
  - Device Owner, 전용 HOME, release signer와 설치 데이터 유지
- artifact/설치 APK SHA-256:
  `76BF27EFD4EF1F867B1B0CD213B345FCCC526817AAD98CAB06FCE54C2D97AAB3`
- release 158 tasks와 APK 이중 검증을 통과했다.
- 구현 커밋: `9bb44e1`

### S1 현장 결과

- 안전 복구, 기존 학생·반·소속·QR 이력, 다중 반 소속, 관리자 뒤로가기와
  운영 준비 자가진단: PASS
- 존재하지 않는 반 CSV 사전 거부: PASS
- 일회용 CSV 학생 신규 생성·동일 아이디 갱신·T1/T2 다중 소속과
  민감정보 비노출: PASS
- 학생 추가·제거·이름 변경·테스트반 삭제의 실행취소와 30초 만료: PASS
- 일회용 학생의 비밀번호 변경·비활성화에 실행취소가 없는 것: PASS
- 짧은 확인음: A의 알림·벨소리 음량을 켠 뒤 크기·길이 PASS
- 오른손 키패드 프리셋 저장: PASS
- `릴리스 테스트` 반 Web 검사 후 안전 시작과 `QR_READY`: PASS
- S1 완료 직후 ADB:
  - Kiosk MainActivity 전경·resumed: PASS
  - Lock Task `LOCKED`: PASS
  - Web private 진단 이벤트와 Matholic 관련 crash: 없음
- 아직 확인하지 않은 항목:
  - CSV 변경 학생 카드의 `출력 필요`
  - 기본 피드백값과 QR·완료·오류별 실제 피드백
- 현재 private 진단에는 DHCP 복구의 `PC_ENDPOINT_RECOVERED`만 있으며,
  Matholic 관련 crash 일치 항목은 없다.

## Web POC RC102 학생 영역 탭과 실제 25문항 전수 검토 — 2026-08-01

### 변경과 설치

- 학습지·진단평가 상단 전환에서 현재 영역이 비활성 회색처럼 보이던 상태를
  파란 배경·흰 글씨의 선택 상태로 바꿨다. 이동 가능한 다른 영역은 흰 배경과
  짙은 글씨를 유지한다.
- Web POC `0.4.0-rc102`/code 119를 동일 signer의 상위 버전으로 A에
  보존 설치했다.
- artifact와 A 설치 APK SHA-256은 모두
  `C36306468FBB72E08E925F144B459C4FB94CD35BDBE060F5D2E7439E685E56E7`로
  일치했다.
- Web UID `10293`, firstInstallTime `2026-07-28 13:12:16`, Kiosk Device
  Owner와 Lock Task `LOCKED`를 유지했다.

### 자동·A 실기 검증

- 정식 release 빌드 158 tasks, Web/Kiosk JVM 시험, release lint, 두 release
  APK assemble, 저장 전·후 버전·비디버그·서명 검증: PASS
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- 정확히 검증된 `테스트` QR만 사용해 로그인하고 학습지·진단평가를 양방향
  전환했다. 선택 탭 강조, 빈 진단평가의 한글 안내, 동일 세션 유지와 정상
  로그아웃을 실제 화면으로 확인했다.
- 최초 25개 캡처 묶음은 파일명만 순번이었고 일부 다음 버튼 터치가 누락되어
  실제 2번·6번 등이 중복된 사실을 발견했다. 이 묶음은 전수 검토 근거에서
  제외했다.
- 재수집은 DOM의 실제 현재 번호, 이동 controller 종료와 숨김 selector 종료가
  모두 일치한 뒤에만 캡처하도록 했다. 1~25번 각각 한 장씩 중복·누락 없이
  확인했다.
- 25문항 전체의 최초 표시 화면에서 문제 본문, 문항 번호와 방향 버튼, 답안
  현황, 객관식·주관식 답안 영역, 답안 제출과 채점 끝내기 사이의 겹침·가로
  잘림을 발견하지 않았다.
- 긴 15번은 여러 번 아래로 이동해 표 끝까지 확인한 뒤 다시 상단으로
  복귀했다. 이동 중 우측 제어가 고정되고, 이어 16번으로 전환하면 이전
  스크롤을 남기지 않고 새 문제 상단에서 시작함을 RC102에서 확인했다.
- 답안 입력·제출은 하지 않았고 문제 간 이동만 수행했다. 종료 뒤 A는
  `QR_READY`, 원격 점검은 비활성 상태다.
- 이번 실행 뒤 새 ANR, fatal exception, WebView 불응과 비정상 종료는 없다.
  exit-info의 최신 Web POC 종료는 RC102 설치 시 Android의
  `installPackageLI / FORCE STOP` 기록이다.

## Web POC RC102 과제 수별 화면·주관식 키패드 실기 — 2026-08-01

- 정확히 검증된 표시명 `테스트` QR만 사용했다. 진행 중인 10문항·5문항
  과제와 완료된 1문항 과제의 오답학습을 차례로 열어 실제 A 화면을
  캡처했다.
- 과제 목록, 1·5·10문항의 최초 문제 화면에서 제목, 문제 번호, 이전·다음,
  답안 현황, 답안 영역, 답안 제출과 채점 끝내기의 겹침·가로 잘림을
  발견하지 않았다. 1문항 화면은 불필요한 이전·다음과 답안 현황을 표시하지
  않고 `1/1`만 표시했다.
- 1문항 주관식 입력칸을 직접 눌러 자체 키패드를 열고 숫자·루트·분수·파이를
  연속 입력했다. 세 입력 구역, 역T 방향키, 실행 취소·다시 실행·한 칸 삭제와
  전체 지움이 화면 안에 들어오며 문제·답안칸과 겹치지 않았다.
- `채점 끝내기`는 키패드 오른쪽의 별도 여백에 완전히 분리돼 있었다. 넓은
  전체 캡처를 축소해 볼 때 일부만 보이는 듯했으나 우측 영역을 원본 크기로
  다시 확인해 겹침이 아님을 확인했다.
- 전체 지움은 첫 터치에서 `다시 눌러 지움`으로 바뀌고 2초가 지나면
  초기화됐다. 제한시간 안에 두 번 눌렀을 때만 입력값이 지워졌다.
- 답안은 제출하지 않고 비운 뒤 목록으로 복귀해 정상 로그아웃했다. A는
  `QR_READY`이며 이번 조작에서 잠금·오류 코드·ANR·crash는 발생하지 않았다.

## Web POC RC102~RC107 주관식 전환 잔상 제거 — 2026-08-01

### 프레임 단위 재현과 수정

- 정확히 검증된 표시명 `테스트` QR의 10문항 과제만 사용했다.
- RC102 화면을 약 38fps로 녹화해 객관식에서 주관식으로 바뀔 때 매쓰홀릭의
  기본 입력 placeholder와 `입력기` 버튼이 약 0.15초 먼저 나타나는 현상을
  재현했다. 정지 화면만으로는 보이지 않던 실제 잔상이었다.
- RC103은 답안 영역을 수식 편집기 준비 전까지 가리는 방식으로 잔상을 한
  프레임, 약 0.025초로 줄였으나 완전히 없애지 못해 채택하지 않았다.
- RC104는 기본 입력 요소를 최초 paint 전부터 `visibility:hidden`으로
  가렸다. 잔상은 없어졌지만 매쓰홀릭의 수식 모드 전환 코드도 그 요소를
  비가시 상태로 판단해 수식 편집기를 만들지 못하는 회귀가 A에서 확인되어
  채택하지 않았다.
- RC105는 기본 입력 요소의 레이아웃과 자동 전환 대상 판정은 유지하면서
  `opacity:0`과 입력 차단만 적용한다. 수식 편집기가 준비되지 않는 경우에는
  500ms 뒤 기존 기본 입력기를 다시 사용할 수 있게 하는 fallback도 유지했다.

### 자동·릴리스 검증

- Web JVM 단위시험과 Android instrumentation test 소스 컴파일 30 tasks:
  PASS
- 정식 release 빌드 158 tasks, Web/Kiosk JVM 시험, release lint, 두 release
  APK assemble, 저장 전·후 버전·비디버그·서명 검증: PASS
- Android instrumentation test APK를 A에서 실행하는 검증은 release 앱을
  debug signer 앱으로 교체할 수 있어 수행하지 않았다. 해당 fixture는
  컴파일했으며 실제 정상 경로는 아래 A 실기 검증으로 확인했다.
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존 설치와 실기 결과

- Web POC `0.4.0-rc105`/code 122를 동일 signer의 상위 버전으로 A에
  보존 설치했다.
- artifact와 A에서 다시 추출한 설치 APK SHA-256은 모두
  `247E74C776A0826C29D7DF0C7F1F688813CEF2291131D03912C19777E0D7D6E9`로
  일치했다.
- Web UID `10293`, firstInstallTime `2026-07-28 13:12:16`, Kiosk Device
  Owner와 Lock Task `LOCKED`를 유지했다.
- RC105에서 36.491fps, 203프레임, 5.563초를 녹화했다. 1→2→3→4번 이동과
  3.206~3.645초의 객관식→주관식 전환 전 프레임을 확인했으며 기본 입력
  placeholder와 `입력기` 버튼의 선행 노출은 0회였다.
- 최종 MathQuill 편집기가 정상 생성됐고 직접 눌렀을 때 자체 숫자·수식·이동
  키패드가 열렸다. 숫자 `1` 입력으로 답안 현황이 2/10→3/10이 된 뒤 한 칸
  삭제로 빈 값과 2/10을 복원했다.
- 답안은 제출하지 않고 `채점 끝내기`로 정상 종료했으며 A는 `QR_READY`로
  복귀했다. 관련 fatal exception, ANR, Web POC process crash는 없었다.
- 원격 점검과 CDP 전달은 종료했고 기기의 임시 screenrecord도 삭제했다.

### RC106 범위 축소 회귀와 RC107 최종 보강

- RC105의 입력 메뉴 정적 selector가 페이지 상위 `div`까지 일치할 수 있어
  RC106에서 답안 폼 ID 내부로만 제한했다. 그러나 실제 최초 입력 DOM에는
  그 ID가 없었고, 36.20fps 녹화의 3.343초 한 프레임에서 `입력기` 버튼이
  다시 나타났다. RC106은 채택하지 않았다.
- 새 세션의 1번 문제에서 CDP MutationObserver를 먼저 설치한 뒤 4번
  주관식까지 이동해 최초 DOM을 캡처했다. 실제 구조는 Ant 입력 wrapper와
  `입력기` 버튼이 같은 `div`의 직계 자식인 형태였다.
- RC107은 이 공통 부모 한 단계만 일치시키도록 selector를 제한했다. 페이지
  전체 드롭다운에는 번지지 않으면서 초기 `입력기`만 paint 전에 가린다.
- instrumentation fixture도 실제 Ant 입력 wrapper·형제 버튼 구조로 바꾸고,
  입력칸은 자동 수식 전환 대상 판정이 가능하며 버튼은 opacity 0인지 함께
  검사한다.

### RC107 자동·A 실기 검증

- Web JVM 단위시험과 Android instrumentation test 소스 컴파일 30 tasks:
  PASS
- 정식 release 빌드 158 tasks, Web/Kiosk JVM 시험, release lint, 두 release
  APK assemble, 저장 전·후 버전·비디버그·서명 검증: PASS
- Web POC `0.4.0-rc107`/code 124를 동일 signer의 상위 버전으로 A에
  보존 설치했다.
- artifact와 A에서 다시 추출한 설치 APK SHA-256은 모두
  `A7E97C00A851FF925F864B220236150CCA27A72DE7C21C2C18D2AC1F08690397`로
  일치했다.
- Web UID `10293`, firstInstallTime `2026-07-28 13:12:16`, Kiosk Device
  Owner와 Lock Task `LOCKED`를 유지했다.
- RC107에서 39.768fps, 216프레임, 5.431초를 녹화해 1→2→3→4번 이동 전
  프레임을 확인했다. 기본 입력 placeholder와 `입력기`의 선행 노출은 0회였고
  최종 수식 편집기와 자체 키패드가 정상 생성됐다.

## Web POC RC108 전체답안 비활성 커서 정리 — 2026-08-01

### 재현과 수정

- 정확히 검증된 표시명 `테스트` QR의 10문항 과제만 사용했다.
- 전체답안 확인창을 열면 실제 포커스가 없는 빈 주관식 칸 6개에 MathQuill
  커서가 동시에 깜박였다. DOM에서는 `document.activeElement`가 해당 칸이
  아니고 `.mq-focused`도 없었지만, 입력 안정화 과정 뒤 각 칸에
  `.mq-hasCursor > .mq-cursor`가 남아 있었다.
- 확인창의 `.mq-editable-field:not(.mq-focused) .mq-cursor`만 숨겼다. 일반
  문제풀이 화면의 편집기는 범위 밖이며, 확인창에서도 실제로 누른 칸은
  `.mq-focused`가 붙으므로 커서와 자체 수식 키패드를 그대로 사용할 수 있다.

### 자동·릴리스 검증

- Web JVM 단위시험과 Android instrumentation test 소스 컴파일 30 tasks:
  PASS
- 새 fixture에서 확인창의 비활성 커서는 `hidden`, 일반 문제풀이 커서는
  `visible`, 확인창 편집기에 `.mq-focused`를 적용한 뒤에는 다시 `visible`인
  것을 검증했다.
- 정식 release 빌드 158 tasks, Web/Kiosk JVM 시험, release lint, 두 release
  APK assemble, 저장 전·후 버전·비디버그·서명 검증: PASS
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존 설치와 실기 결과

- Web POC `0.4.0-rc108`/code 125를 동일 signer의 상위 버전으로 A에
  `adb install -r` 보존 설치했다.
- Web UID `10293`, firstInstallTime `2026-07-28 13:12:16`, dataDir, Kiosk
  Device Owner와 `LockTaskModeState=LOCKED`가 유지됐다.
- artifact와 A에서 다시 추출한 설치 APK SHA-256은 모두
  `529C9CAB007D0F05C08ABDE730E1E5D6A964B7D2B8BDF75981DB5FCEF36C20BA`로
  일치했다.
- 전체답안 초기 화면에서 비활성 주관식 칸의 커서는 모두 보이지 않았다.
  4번 칸을 누르면 해당 칸 하나만 커서와 자체 키패드가 나타났고, 다른 칸은
  계속 깨끗하게 유지됐다.
- 자동 하단 이동 후 위로 스크롤했으며 3초 뒤에도 상단 위치가 유지되어
  미입력 문제를 확인할 수 있었다.
- 별도 후속 문제로, 확인창을 연 직후 약 1초 이내 화면에서 기존 `입력기`
  드롭다운이 먼저 나타난 뒤 수식 입력기로 교체되는 잔상을 실제 캡처했다.
  RC108의 비활성 커서 수정과 분리해 다음 변경에서 처리한다.
- 정확한 `테스트` 계정의 10문항 현황판에서 번호 4 직접 이동, 4→5 다음
  미입력, 5→4 이전 미입력을 확인했다. 25문항 현황판에서는 1~25 전체 표시,
  25번 직접 이동, 24↔25 미입력 이동과 25→1 순환 이동을 확인했다.
- 번호·답변·모름·현재 문제 상태는 서로 구분됐고 현황판이 답안 제출이나
  `채점 끝내기`를 가리지 않았다.
- 답안은 제출하거나 변경하지 않고 종료했다. 원격 점검 만료 뒤 QR 복귀
  확인을 위해 5분만 다시 열었다가 즉시 비활성화했으며 최종 상태는
  `QR_READY`다. 관련 fatal exception, ANR과 Web POC process crash는 없었다.

## Kiosk RC54 · Web POC RC109 입력·현황 UI 교정 — 2026-08-01

### 변경 범위

- 관리자 PIN은 6~12자리 입력이 잠시 멈추면 기존 인증·실패 횟수·잠금 정책을
  그대로 거쳐 자동 제출한다. 키보드 완료나 별도 확인 버튼은 필요하지 않다.
- 문제번호는 이동 방향이나 펼침 기호 없이 항상 `현재/전체`만 표시하며 클릭
  기능을 제거했다. 답안현황은 별도의 고정형 가로 패널로 열리고 본문 배치를
  밀어내지 않는다.
- 객관식 숫자 답안 컨트롤의 터치 가능 상태를 보장했다. 답안현황 번호는 원형
  중앙 정렬, 미입력 이동은 `이전/미입력`과 `다음/미입력` 한 줄 표기로 바꿨다.
- 수식 키패드의 섹션 제목을 제거하고 세 블록의 아래끝을 맞췄다. 루트·분수와
  방향키는 중앙 정렬 SVG로 바꾸고, `부호`는 문자를 덧붙이는 대신 전체
  MathQuill 값의 음수를 설정·해제한다.
- 문제 전환 전 숨김과 입력기 준비 과정에서 기존 입력기·`수식` 드롭다운이
  먼저 그려지지 않도록 비가시 상태와 투명도를 함께 관리했다.

### 자동·릴리스 검증

- Web Android instrumentation test `104/104`: PASS
- Kiosk Android instrumentation test `47/47`: PASS
- 정식 release 빌드 158 tasks, Web/Kiosk JVM 시험, release lint, 두 release
  APK assemble, 저장 전·후 버전·비디버그·서명 검증: PASS
- Kiosk artifact SHA-256:
  `DBB7D741D3DAF14C5C885F9F97ED7331BB1DC147B943476375BC103CF7FF8221`
- Web artifact SHA-256:
  `6F6B0DE7AAD19C1F032707334A4496F6F0260A7AA969933A5254A5A986CD96DE`
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

### A 보존 설치와 실기 결과

- Kiosk `0.6.0-rc54`/code 59와 Web POC `0.4.0-rc109`/code 126을 A에
  `adb install -r`로 보존 설치했다. 기존 UID, firstInstallTime, Device Owner와
  `LockTaskModeState=LOCKED`를 유지했다.
- DPAPI 저장 PIN을 숫자만 입력하는 보조 경로로 전송했으며 키보드 완료나 확인
  버튼 입력 없이 관리자 화면으로 자동 전환됐다.
- 정확히 검증된 표시명 `테스트` QR의 10문항 과제에서 실제 숫자 답안 `2`를
  눌러 선택 전환을 확인했다. 문제번호는 `1/10`, `10/10`, `9/10`으로만
  표시됐고 문제번호 자체를 눌러도 현황판이 열리지 않았다.
- 답안현황은 문제 본문 위에 고정된 가로 패널로 열렸으며 원형 번호 1~10,
  한 줄 미입력 이동, 범례가 잘림 없이 표시됐다. 10번 직접 이동도 정상이다.
- 주관식 자체 키패드에서 섹션 제목 제거, 세 블록 아래끝 정렬, 루트·분수 SVG,
  역T 방향키를 육안 확인했다. `12` 입력 뒤 `부호`를 누르면 `-12`로 바뀌어
  전체값 토글 동작을 확인했다.
- 10→9 문제 전환을 약 22.63fps, 139프레임으로 녹화해 전환 전후 프레임을
  확인했다. 문제번호 방향기호, 기존 `수식`·입력기 표시의 선행 노출은 없었다.
- 시험 답안은 제출하지 않고 `채점 끝내기`로 정상 종료했으며 최종 화면은
  QR 대기 상태다.

## Kiosk RC55 · Web POC RC112 PIN·답안 조작 후속 — 2026-08-01

### 변경 범위

- 기존 PIN hash로는 길이를 알 수 없으므로 관리자 인증 성공 시 비밀이 아닌
  PIN 길이만 DB에 기록한다. 이후 PIN 입력이 등록 길이에 도달하면 즉시
  인증하며, 접두 길이에서는 제출하지 않는다. 기존 인증 버튼과 키보드 완료는
  복구 경로로 유지한다.
- DB v2→v3 마이그레이션은 기존 관리자 hash·salt와 학생·반 데이터를 그대로
  보존하고 `pinLength=0`을 추가한다. 첫 성공 인증 뒤 길이가 학습된다.
- 객관식은 실제 문제 이미지 위의 인쇄된 ①~⑤를 눌러도 공식 답안 버튼과 같은
  선택을 수행한다. 이미지 위 오버레이가 터치를 받는 실제 사이트 구조도
  capture 단계에서 처리한다.
- 답안 현황은 `이전 미입력`, `다음 미입력`을 줄바꿈 없이 표시하며, 패널 밖을
  누르거나 Escape를 누르면 닫힌다.
- 주관식 `모름`은 실제 답안이 생기면 파란 답변 상태가 우선한다. 공식 사이트가
  `모름`을 가상 요소로만 표시하는 상태도 해제할 수 있게 탐지한다.
- 자체 수식 키패드는 숫자·기호와 방향키를 정사각형 중심으로 축소하고, 역T
  방향키를 SVG로 정렬했다. 루트·분수·긴 실행 제어만 필요한 폭을 사용한다.
  `전체 지움`은 한 번에 지우고 `다시 실행`으로 직전 값을 복원한다.

### 자동·릴리스 검증

- Kiosk/Web JVM 단위시험, debug lint, debug·AndroidTest 소스 컴파일: PASS
- 정식 release 빌드 158 tasks, release lint, 두 APK assemble, 버전·
  비디버그·서명 검증: PASS
- Android instrumentation 실행은 PC 재시작 위험 때문에 금지된 에뮬레이터를
  시작하지 않았고, release 앱 데이터와 Device Owner를 가진 A에 debug test
  APK를 설치하지 않기 위해 이번 사이클에서는 생략했다. 소스 컴파일은 PASS다.
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- Kiosk artifact SHA-256:
  `1ED95AB63BD3C3DDEC0B330E981D7D36A8BCD93D826891E430B85EF4FBEC4959`
- Web artifact SHA-256:
  `A24E419C3E639F4A4E04EA7A8FC9C2FE073E29E602EA4F3FE72B3230E77242EF`

### A 보존 설치와 실기 결과

- Kiosk `0.6.0-rc55`/code 60과 Web POC `0.4.0-rc112`/code 129를 동일
  signer의 상위 버전으로 `adb install -r` 보존 설치했다.
- Kiosk/Web UID `10288`/`10293`, firstInstallTime, Device Owner와
  `LockTaskModeState=LOCKED`를 유지했다. 두 설치 APK의 SHA-256은 위 artifact와
  각각 일치했다.
- 최초 버튼 인증 한 번으로 기존 DB의 PIN 길이를 학습한 뒤, QR 화면 관리자
  대화상자에서 저장 PIN의 숫자 입력만 수행해 인증 버튼 없이 관리자 화면으로
  전환됨을 확인했다.
- 정확히 검증된 표시명 `테스트` QR의 실제 10문항 과제에서 인쇄된 ①·②와
  5지선다 ④를 직접 눌러 해당 공식 답안이 선택됨을 확인했다.
- 답안 현황의 원형 번호·한 줄 미입력 이동을 육안 확인했고, 패널 밖 터치로
  닫힘을 DOM 상태로 확인했다.
- 주관식 2번에서 `모름` 선택 시 회색, 해제 시 흰색, `13` 입력 시 파란색으로
  전환되는 것을 실제 현황판에서 확인했다.
- 축소된 자체 키패드의 정렬·잘림·겹침 없음을 실제 화면에서 확인했다.
  `13`을 한 번의 `전체 지움`으로 삭제하고 `다시 실행`으로 복원했으며 답안
  현황 수도 2→1→2로 동기화됐다.
- 시험은 `채점 끝내기`로 정상 종료했고 최종 화면은 전면 카메라 QR 대기다.

## Web POC RC116 미입력 이동·객관식·3×3 키패드 교정 — 2026-08-01

### 변경 범위

- `다시 실행`을 제거하고 오른쪽 편집 블록을 빈 열 없는 3열×3행으로
  재배치했다. 첫 행은 `실행 취소`·`한 칸 삭제`·`전체 지움`, 아래 두 행은
  역T 방향키이며 모두 숫자키와 같은 48×48 정사각형이다.
- `전체 지움`은 한 번에 동작하며, 지운 직전 값은 `실행 취소`로 복원한다.
- 답안 현황의 문제·미입력 이동 직후에는 대상 문제가 안정되고 0.7초간 새
  입력이 없을 때까지 터치를 차단한다. 차단 중 연속 터치가 들어오면 무입력
  구간을 다시 계산해 다음 문제의 답으로 전달되지 않게 했다.
- 실제 문제 그림의 객관식 보기는 인쇄된 번호뿐 아니라 보기 문장 전체를
  눌러도 공식 답안 컨트롤을 선택하며, 선택한 보기 전체를 파란색으로 표시한다.
- 객관식 좌표 처리기는 클릭 좌표만 보지 않고 이벤트가 실제 해당 문제 그림
  내부에서 시작했는지 확인한다. 답안 현황처럼 문제 위에 겹친 UI를 누른 것을
  아래 객관식 보기 선택으로 오인하던 원인을 제거했다.

### 자동·릴리스 검증

- Web JVM 단위시험, AndroidTest 소스 컴파일, debug lint 39 tasks: PASS
- 정식 release 빌드 158 tasks, Web/Kiosk JVM 시험, release lint, 두 APK
  assemble, 버전·비디버그·서명 검증: PASS
- Android instrumentation 실행은 PC 재시작 위험 때문에 금지된 에뮬레이터를
  시작하지 않았고, A의 release 앱 데이터에 debug test APK를 설치하지 않기
  위해 생략했다. 신규 계측 회귀시험의 소스 컴파일은 PASS다.
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- Web artifact SHA-256:
  `2E5AD309E7D83EDE844654AED0359702EBA04C6C2A345FDF38CE9CF3B6CDA495`

### A 보존 설치와 실기 결과

- Web POC `0.4.0-rc116`/code 133을 동일 signer로 `adb install -r` 보존
  설치했다. UID `10293`, firstInstallTime `2026-07-28 13:12:16`, dataDir,
  Device Owner와 `mLockTaskModeState=LOCKED`를 유지했다.
- 설치된 base APK와 artifact SHA-256이 일치했고 최근 AndroidRuntime 표본의
  Matholic 관련 fatal exception은 0건이다.
- 수정 전 A의 10문항 과제에서 답안 현황 이동을 반복하자 클릭이 아래 객관식
  보기로 전달돼 답안 수가 `1/10→2/10`, 다시 `2/10→3/10`으로 늘어나는 것을
  실제 재현했다. 원인은 문서 전체 좌표만 보던 객관식 터치 처리기였다.
- 수정 후 오염되지 않은 25문항 과제에서 답안 현황 열기와 `다음 미입력`을
  같은 빠른 간격으로 15회 반복했다. 답안 현황은 계속 `0/25`, 체크된 객관식
  0건, 값이 든 주관식 0건을 유지했다.
- 7번 객관식의 보기 문장 끝부분을 눌러 3번 공식 답안과 보기 전체 파란 강조가
  함께 선택되는 것을 실제 화면과 DOM 상태로 확인했다.
- 13번 주관식에서 3×3 편집 블록의 정렬·잘림·겹침 없음과 각 짧은 버튼의
  48×48 크기를 실제 화면과 DOM 치수로 확인했다. 세 자리 입력을 한 번에
  지우고 `실행 취소`로 복원한 뒤 다시 지워 주관식 시험 값을 남기지 않았다.
- 답안을 제출하지 않고 `채점 끝내기`로 종료했으며 최종 상태는 QR 대기다.

## Web POC RC117 객관식 행·문제 전환·키패드 경계 교정 — 2026-08-02

### 변경 범위

- 문제 그림의 객관식 표식을 y좌표 기준 행으로 먼저 묶고 각 행 안에서만
  보기의 좌우 경계를 계산한다. `1·2·3 / 4·5`처럼 두 줄인 보기에서 동일한
  x좌표를 한 줄로 합쳐 선택 배경이 문제 제목과 다른 행까지 덮던 결함을
  제거했다. 한 줄 가로형과 세로형 보기 계산은 유지한다.
- 공식 이전·다음 버튼도 답안 현황 직접 이동과 동일한 문제 전환 보호에
  포함했다. 클릭 직전 문제번호를 보존하고 전환 뒤 대상 번호가 안정될 때까지
  답안 영역과 수식 입력기 자동 활성화를 막아 이전 객관식 값이 새 주관식
  입력기로 전달되지 않게 했다.
- 자체 수식 키패드는 패딩 20px와 테두리 4px를 포함한 574px 바깥 폭으로
  계산하고 내부 세 열을 실제 가용 폭 안에서 배치한다. 우측 버튼 돌출을
  차단하고, 표시 중에는 `body`와 `#root`에 하단 스크롤 여유를 추가한다.
  키패드와 교차하는 문제 그림·표가 있으면 입력칸과 함께 위로 이동시킨다.

### 자동·릴리스 검증

- Web JVM 단위시험, AndroidTest 소스 컴파일, debug lint 39 tasks: PASS
- 정식 release 빌드 158 tasks, Web/Kiosk JVM 시험, release lint, 두 APK
  assemble, 버전·비디버그·서명 이중 검증: PASS
- 신규 계측 회귀시험은 두 줄 객관식 행 비중첩, 공식 문제 전환 중 낡은 값의
  수식 입력기 커밋 금지, 키패드 모든 버튼의 바깥 경계 비돌출을 고정한다.
  Android instrumentation 실행은 금지된 에뮬레이터를 시작하지 않고 A의
  release 데이터에 debug test APK를 설치하지 않기 위해 생략했다. 소스
  컴파일은 PASS다.
- release signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- Web artifact SHA-256:
  `5E1BF06B2DE354947CEC265F132F208421759DB0486362CE32D8C2ABBEDE735D`

### A 보존 설치와 실기 결과

- Web POC `0.4.0-rc117`/code 134를 동일 signer로 `adb install -r` 보존
  설치했다. UID `10293`, firstInstallTime `2026-07-28 13:12:16`, dataDir,
  Kiosk Device Owner와 `mLockTaskModeState=LOCKED`를 유지했다.
- 설치된 base APK와 rc117 artifact의 SHA-256은
  `5E1BF06B2DE354947CEC265F132F208421759DB0486362CE32D8C2ABBEDE735D`로
  일치했고 최근 AndroidRuntime 표본의 Matholic 관련 fatal 줄은 0건이다.
- 업데이트로 중단된 기존 Web 세션은 저장 PIN과 원버튼 안전 복구로 정리한 뒤
  기존 반 수업을 웹 검사 후 재시작했다. 학생·반·QR 데이터는 삭제하지 않았다.
- 표시명이 정확히 `테스트`인 PDF 카드의 10문항 과제 q5에서 두 줄 보기 영역을
  실측했다. 1행 3번 영역은 y `208.71`, 높이 `43.84` CSS px이고 2행은
  y `252.56`부터 시작해 겹침이 없었다. 3번 선택 배경은 3번 보기 전체만
  표시하고 문제 제목·4·5번을 덮지 않았다.
- rc116에서 이미 남아 있던 q8 주관식 값 `3`을 자체 키패드로 지운 뒤 공식
  q8→q7→q8 왕복을 수행했다. 복귀 후 MathQuill 값은 빈 문자열이고 답안
  현황은 q5의 한 답만 남아 새 주관식 오염이 재발하지 않았다.
- q8 실제 키패드의 바깥 경계는 x `14`, 폭 `574`, 오른쪽 `588` CSS px이고
  가장 오른쪽 버튼은 x `576`에서 끝나 12px 안쪽 여백을 유지했다. 우측 잘림,
  채점 끝내기와의 겹침, 패널 밖 버튼은 실제 화면에서 없었다.
- 시험 답안은 제출하지 않았고 q8 값을 지우고 q5 선택을 해제한 뒤
  `채점 끝내기`로 정상 종료했다. 최종 화면은 전면 카메라 QR 대기이며
  LockTask는 `LOCKED`다.

### RC117 실기 답안 정리 정정 — 2026-08-02

- 위 최초 실기에서 q5의 3번은 화면상 체크만 잠시 해제됐고 공식 임시답안
  저장에는 남아 있었다. 세션을 종료하고 사용자가 다시 과제에 들어가자 3번이
  지연 복원됐다. 앱이 새 답을 임의 입력한 것이 아니라 Codex 실기값을 완전히
  정리하지 못한 운영 오류이며, 최초 정리 완료 보고는 잘못이었다.
- 공식 `모름` 선택 뒤 React가 교체한 최신 `모름` 제어를 다시 찾아 해제해
  q5의 공식 상태와 키오스크 상태를 모두 `unanswered`로 저장했다.
- q4→q5 왕복 뒤 빈 상태를 확인하고 세션을 완전히 종료했다. 정확한 `테스트`
  QR로 같은 과제에 재진입해 아무 답도 누르지 않고 q5로 이동한 뒤 60초 이상
  관찰했다. 3번은 재복원되지 않았고 답안 현황은 계속 `0/10`이었다.
- 이후 원격 실기 답안 정리는 화면 DOM만 보지 않고 공식 상태 저장, 완전 종료,
  같은 과제 재진입과 60초 지연 복원 부재까지 완료 기준으로 사용한다.

## RC56·RC118·PC 수신기 0.1.4 교정 검증 — 2026-08-02

### 교정 범위

- Android `PrintManager` 직접 인쇄 제거, 지정 PC PDF 저장 경로 단일화
- 학생·반·CSV·QR·카드 PDF 작업 공통 gate와 Web recovery 상호 차단
- legacy DB 열 `needsPrint`의 제품 의미를 `새 카드 PDF 생성 필요`로 정정
- Web SPA 문항 보조 상태를 URL·과제 표식별 최근 8개 scope로 격리
- Web 로그아웃에서 origin storage 전체 삭제를 제거하고 공식 로그아웃·cookie
  제거·로그인 재검증은 유지
- PC 수신기 설정 v2의 pairing secret을 Windows 사용자 DPAPI로 보호하고
  v1 설정을 최초 load에서 원자적으로 이전

RC117 실기 답안 정정은 Codex가 만든 시험 답안을 현장에서 완전히 되돌렸는지
확인하는 운영 절차다. 제품 로그아웃이 모든 학생의 서버 임시답안을 삭제해야
한다는 요구로 해석하지 않는다.

### 자동 검증

- Kiosk `compileDebugKotlin`, `compileDebugAndroidTestKotlin`,
  `testDebugUnitTest`: PASS, 33 tasks
- Web `compileDebugKotlin`, `compileDebugAndroidTestKotlin`,
  `testDebugUnitTest`: PASS, 30 tasks
- PC 수신기 pytest: **14 passed**. v2가 평문 `secret`을 쓰지 않는지, v1 자동
  이전, Windows 실제 DPAPI round-trip을 포함한다.
- `scripts/build-release.ps1`: 정식 release 158 tasks, Kiosk/Web JVM 시험,
  release lint, assemble, version·비디버그·동일 signer 검증 PASS
- `pc_receiver/build-receiver.ps1`: pytest, PyInstaller packaging, 인증된 합성
  PDF 전송·ACK·파일 정리 smoke check PASS
- Android instrumentation은 연결된 A의 RC55/RC117 release 설치·데이터를
  debug signer APK로 교체하지 않기 위해 실행하지 않았다. 신규 계측 source
  compile은 PASS다.

### 미배포 산출물

- Kiosk `0.6.0-rc56`/code 61:
  `75440E6C8FB7F1574A48D5DD33C8F0C4F021B82E210D432FD447949FC03AB822`
- Web POC `0.4.0-rc118`/code 135:
  `F884C7BB0DE10B8FB9481B20F64F9BA3C59B1E1782BBD4213A88A11C9F493F1E`
- PC 수신기 `0.1.4`:
  `59E715E8D48D58484BCC78362B1782AB914665A8486622836F0CFB75F3AAF566`
- APK signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

태블릿과 현재 PC 수신기에는 설치하지 않았다. 따라서 A는 계속 RC55/RC117,
실행 중 PC 수신기는 0.1.3이며, PC 설정의 v1→v2 영구 이전은 0.1.4 설치·재시작
때 수행된다.

## RC57·RC119·PC 수신기 0.1.5 LUNA 전체 교정 검증 — 2026-08-03

### 추가 교정 범위

- QR·수동 학생 선택·Web 결과 callback을 단일 학생 launch gate와 시작 당시
  session ID에 결합하고 replacement session의 stale callback을 거부
- audit 90일/10,000행 retention과 장기 실행 중 256건 주기 정리, private 진단
  로그 90일·크기 상한과 bounded tail dump 적용
- QR renderer 크기 상한과 임시 pixel/BitMatrix cleanup
- 관리자 PIN scalar 조회와 verifier/Room 배열 cleanup
- Web loopback proxy 8 tunnel 상한·60초 idle 회수·listener 실패 1회 재시작
- PC pairing 저장·복구 byte 경로와 queued raw secret String 제거
- CSV partial-row·preview/apply queue·Activity 종료의 mutable credential owner 정리
- 직접 인쇄 제거 후 남아 있던 preflight·보안·제한 문서의 현재형 안내 정정

### 자동·릴리스 검증

- `scripts/build-release.ps1`: 정식 release **158 tasks PASS**. Kiosk/Web JVM
  시험, release lint, signed assemble, version·`debuggable=false`·동일 signer
  검증을 포함한다.
- Kiosk/Web AndroidTest source `compileDebugAndroidTestKotlin`:
  **50 tasks PASS**. 설치된 A의 release 앱·데이터를 debug test APK로 바꾸지
  않기 위해 instrumentation 실행은 생략했다.
- `pc_receiver/build-receiver.ps1`: **17 passed**, PyInstaller packaging,
  실행 파일 `--smoke-check` PASS.
- PowerShell release/provision/receiver script parser와 최종 `git diff --check`:
  PASS. 최초 Kiosk 단위시험 재실행 1회는 `ANDROID_HOME`을 누락해 build 설정
  단계에서 실패했고, SDK 경로를 지정한 동일 명령은 PASS했다.

### 미배포 산출물

- Kiosk `0.6.0-rc57`/code 62, 35,225,680 bytes:
  `F5A460C8475FDC251293D69FDFD9516F9E78AF9A4AC5DAEBD6253A7E8478F04B`
- Web POC `0.4.0-rc119`/code 136, 3,326,858 bytes:
  `B5B6F257779636718C210E08087ED299F4757D3DC2BB9A8F0DEF20E954687B24`
- PC 수신기 `0.1.5`, 21,925,051 bytes:
  `BA94DCC1ADA65383C7F9EFD515B79C4CC4B273BA8FABC880E348A59BEEE1B136`
- APK signer SHA-256:
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`

태블릿과 운영 PC에는 설치하지 않았다. 실제 네트워크 단절·접근성·원격 지원,
Lock Task fault, 수동 학생 선택과 PC 물리 인쇄는 별도 현장 검증 범위다.
