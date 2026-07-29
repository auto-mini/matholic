# 매쓰홀릭 채점 키오스크 연속 개발 Goal 헌장

작성일: 2026-07-25 (Asia/Seoul)

이 문서는 대화가 압축되거나 작업이 여러 차례 이어져도 사용자의 최초 의도와
확정 결정을 잃지 않기 위한 지속 작업 기준이다. 연속 작업을 시작하거나 재개할
때마다 이 파일 전체와 `AGENTS.md` 지침, 현재 Git 상태를 먼저 확인한다.

## 최종 목적

시험계정과 사용자가 소유한 전용 Android 태블릿에서 매쓰홀릭 채점 키오스크를
소규모 운영에 실제로 사용할 수 있는 수준으로 개선한다. 아래 우선 요구사항을
먼저 A 기기에서 검증하고, 이후 합리적인 조사 → 구현 → 자동시험 → A 기기시험
→ 검토 → 개선 사이클로 현재 범위의 알려진 결함과 미검증 항목을 해소한다.

시간을 채우기 위한 변경, 요청과 관계없는 기능 추가, 장식적 리팩터링은 하지
않는다. 코드·시험·실물·운영 문서를 요구사항별로 감사하고, 합리적으로 발견
가능한 중요 결함이 남지 않았으며 완료 기준을 증명하면 Goal을 완료한다.

## 최우선 사용자 요구사항

### 학생·반 관리

1. 학생 마스터 목록을 먼저 만들고 목록의 학생을 각 반에 추가한다.
2. 한 학생은 여러 반에 동시에 포함될 수 있고 동일 QR로 모든 소속 반에
   접근할 수 있어야 한다.
3. 반 삭제는 반과 해당 소속 관계만 실제 삭제한다. 학생 데이터, 자격정보와
   QR에는 영향을 주지 않는다.
4. 반을 선택하면 소속 학생 명단을 볼 수 있고 학생을 반에서 제거할 수 있어야
   한다.
5. 학생 등록·이름 변경·로그인 정보 변경을 알아보기 쉬운 별도 화면으로
   제공한다. 로그인 정보 변경은 새 아이디, 새 비밀번호와 확인값을 명시적으로
   입력한다.
6. 수업 시작 전에도 이번 수업에만 추가할 보강 학생을 선택할 수 있어야 한다.

### QR 카드·인쇄

7. 카드 케이스는 `65×90mm` 세로형이고, 삽입 종이는 상하좌우 5mm 여유를 둔
   `55×80mm`이다.
8. 카드 안 순수 QR 크기는 정확히 `30×30mm`이고 QR을 기존보다 약 10mm
   아래로 내린다. 학생 전체 이름은 QR 아래에 표시하고 `매쓰홀릭 채점 QR`
   문구는 표시하지 않는다.
9. 마스킹 표시명 기능과 입력 UI는 제거한다. 기존 DB 열은 무중단 호환을 위해
   내부에 남길 수 있지만 사용자 기능으로 사용하지 않는다.
10. Android 직접 인쇄가 실패하는 원인을 가능한 범위에서 조사한다.
11. 직접 인쇄 외에 태블릿에서 PDF를 Quick Share 등으로 PC에 간편하게
    전달하는 경로를 제공한다.
12. PDF·인쇄물의 실제 카드와 QR 크기, 종이 QR 재인식을 검증한다.

### 카메라·키오스크

13. 전면과 후면 카메라를 모두 선택할 수 있어야 한다.
14. QR 분석 중 카메라 미리보기는 화면에 표시하거나 저장하지 않는다.
15. 미리보기 없는 상태에서도 QR 위치 안내와 인식 상태를 명확히 표시한다.
16. 학생 구간에서는 홈·최근 앱·알림창·허용되지 않은 Android UI와 Web
    뒤로가기로 이탈하지 못해야 한다.

### Web 사용성

17. 학생 확인이 끝나면 `학습지` 페이지를 자동으로 연다.
18. 학생에게 보이는 상위 이동 기능은 `학습지`와 `진단평가`만 제공한다.
    두 페이지에서 서로 전환할 수 있어야 한다.
19. 매쓰홀릭의 다른 기능 주소로 이동하지 못하게 허용 경로를 제한한다.
20. 중앙 상단의 `시험계정 확인 완료` 상태 표시는 제거한다.
21. 로그인·로그아웃·안전정리 중에는 뒤의 사이트 내용을 완전 불투명하게
    가린다.
22. Web 학생 화면에서 Android 뒤로가기를 소비하고 가능한 경우 시스템
    뒤로가기 표시도 숨긴다. 뒤로가기로 Web 세션 잠금이 발생해서는 안 된다.
23. 문제 화면을 조금 아래로 이동하고 불필요한 아래 여백을 줄인다.
24. 답안 입력칸, 수식·루트·분수·파이 입력, `모름`, 다음 문제, 문제 화면의
    답안 제출과 전체답안 화면의 최종 제출 버튼 터치 영역을 키운다.
25. 전체답안 화면의 최종 제출 버튼은 끝까지 스크롤하지 않아도 보여야 한다.
26. 최종 제출 뒤 풀이과정과 정답은 학생에게 보여주지 않고 틀린 문제 번호
    목록만 표시한다.
27. 틀린 문제 목록 확인 뒤 자동 로그아웃하고 Kiosk `QR_READY`로 복귀한다.

### 수업·복구 흐름

28. 정상 수업 시작은 남은 Web 세션 안전정리를 먼저 수행하고 성공한 경우에만
    수업을 시작한다.
29. 정상 수업 종료는 Web 로그아웃과 안전정리를 먼저 수행하고 성공한 경우에만
    수업과 일회성 보강 명단을 종료한다.
30. 정상 흐름에서 별도 `Web 세션 안전 정리` 버튼을 한 번 더 누르게 하지
    않는다. 비정상 복구는 실패폐쇄 상태와 명확한 안내를 유지한다.

## 현재 기준 상태

- 저장소: `https://github.com/auto-mini/matholic`
- 작업 브랜치: `codex/rc03-usability`
- RC02 기준 커밋: `18dd1ca`
- 최초 RC03 구현 커밋: `2e4a14d`
- 현재 A: Samsung SM-P610, serial `R54TB029FHZ`
- A Device Owner:
  `com.local.matholickiosk.kiosk/.admin.KioskDeviceAdminReceiver`
- A 설치본:
  - Kiosk `0.6.0-rc33`/code 38
  - Web POC `0.4.0-rc35`/code 52
- ADB: 2026-07-27 현재 기존 PC 승인이 유지된 `device` 상태
- A의 Kiosk RC32/Web POC RC35 설치: 2026-07-27
  `adb install -r --no-streaming`으로 완료
  - 설치 전후 package UID `10288`/`10287`, firstInstallTime, dataDir 유지
  - RC10 설치에서는 앱 프로세스 종료로 Lock Task가 일시 `NONE`이었으나
    화면을 깨우지 않는 명시적 HOME 시작으로 `LOCKED`를 복구
  - RC11 설치 후 최종 `LOCKED`·전용 HOME은 독립 확인했으나 설치 스크립트
    최종 출력 오타로 설치 직후 일시 상태는 기록되지 않음
  - release signer, Device Owner와 전용 HOME 유지
  - RC32/RC35 설치 전 상태는 잘못된 학생 로그인 시험의 안전 종료를 마친
    Kiosk `ADMIN_IDLE`, 활성 수업 없음
  - RC32/RC35 보존형 설치 뒤 화면은 꺼진 상태에서 전용 HOME과 `LOCKED`
    복구를 확인. 다음 화면 점등 시 업데이트 실패폐쇄 복구와 실물 시험 필요
- 내부 보관 현재 검증 묶음:
  - `artifacts/matholic-kiosk-0.6.0-rc33-release.apk`
  - `artifacts/matholic-webpoc-0.4.0-rc35-release.apk`
- A 설치 APK SHA-256:
  - Kiosk:
    `85BFEEEA5BFCBEE54413E8367E4FD8A92C496F5A24EE0611C403555176B3F82A`
  - Web POC:
    `D8B653C51F7B6917B98D8794A89A2710AB98030077507A1C44A27F9AF4BFA496`
- 최신 전체 자동 회귀: Android 13 Web 계측 68개·Kiosk 34개, Web JVM
  45개·Kiosk JVM 48개, release 158 tasks와 APK 이중 검증 통과
- Android 13 일회용 에뮬레이터 계측시험:
  - RC03 A 설치 시점 기준 Web 30개, Kiosk 10개, 실패 0
  - RC04 기준 Web 33개, Kiosk 11개, 실패 0
  - RC05 Kiosk 14개, 실패 0
  - RC06 Kiosk 16개, 실패 0
  - Web POC RC05 34개, 실패 0
  - secure session 전송 복구 기준 Web 34개, Kiosk 16개, 실패 0
  - QR PDF 공유 권한 보강 기준 Kiosk 17개, 실패 0
  - fragment SPA 경로 차단 기준 Web 37개, 실패 0
  - 로그인 form endpoint 검증 기준 Web 39개, 실패 0
  - DOM origin 일치 보강 기준 Web 43개, 실패 0
  - 포털 `/course` 경로 제한 기준 Web 44개, 실패 0
  - 로그인 `/` 경로 제한 기준 Web 45개, 실패 0
  - Web renderer 종료 복구 기준 Web 46개, 실패 0
  - QR PDF 실물 좌표계 보강 기준 Kiosk 18개, 실패 0
- 결과 페이지 선차폐, 비대칭 프린터 DPI와 SPA 인코딩 경로 차단 보강은
  RC04에 포함해 A에 설치했다. 실제 사이트·카메라·PDF·인쇄 실기는
  사용자 복귀 뒤 수행한다.
- RC05에 RC04 설치 뒤의 관리자 안전성 보강을 포함해 A에 설치했다.
  - 관리자 비동기 안전성 커밋 `206b6e7`
  - 반 전환 즉시 이전 명단 제거, 최신 조회 세대만 반영
  - Web 안전정리와 후속 수업 DB 반영의 중복 실행 차단
  - QR 폐기 확인과 학생 변경 단일 실행 커밋 `ea6ad72` 추가
  - 활성 반 이름 중복 차단 커밋 `461ccd3` 추가
  - 수업 저장소 불변조건 커밋 `8881cd8` 추가
  - 최신 소스 전체 debug 204 tasks, JVM 57개, Kiosk 계측 14개, 실패 0
  - 실제 관리자 화면과 빠른 연속 탭·수업 시작/종료 실기는 사용자 복귀 뒤 수행
- RC05 설치 뒤 Web 결과 상태 전이 보강 커밋 `4526072`를 추가했다.
  - 활성 수업과 기대 이전 상태가 일치할 때만 Web 결과를 저장
  - 프로세스 재시작으로 `LOCKED`가 된 뒤 늦게 도착한 이전 Web 성공 결과와
    이미 완료한 Web 결과의 중복 반영을 거부
  - 수정 전 신규 회귀시험 실패를 재현하고 수정 뒤 단일 시험 1개와 Kiosk
    전체 계측 15개 통과
  - 최신 소스 전체 debug 204 tasks, JVM 57개, 실패 0
  - RC06에 포함해 A에 설치했으며 재시작 중 늦은 Web 결과 실기는 사용자
    복귀 뒤 수행
- 현재 수업 보강 학생 배치 원자성 커밋 `6da40a0`을 추가했다.
  - 학생별 반복 저장 중 후반 실패 시 앞 학생만 남는 부분 반영을 회귀시험으로
    재현
  - 현재 session ID·`QR_READY`·선택 학생 전체의 활성 상태를 한 Room
    트랜잭션에서 확인하고 전부 성공하거나 전부 롤백
  - 수정 전 신규 회귀시험 실패, 수정 뒤 단일 시험과 Kiosk 전체 계측 16개 통과
  - 최신 소스 전체 debug 204 tasks, JVM 57개, 실패 0
  - RC06에 포함해 A에 설치했으며 현재 수업의 복수 보강 학생 추가 실기는
    사용자 복귀 뒤 수행
- Web secure session 호출자 경계 커밋 `ce6458f`를 추가하고 Web POC RC05로
  A에 설치했다.
  - credential URI를 읽기 전에 호출 package가 정확한 Kiosk package인지,
    Web POC와 같은 signer인지 검사
  - 비신뢰 명시적 호출은 `SECURE_SESSION_CALLER`로 취소하고 영속 상태를
    `IDLE`로 유지
  - 수정 전 신규 회귀시험에서 기존 `CREDENTIAL_BRIDGE_EMPTY` 경로를 재현하고
    수정 뒤 단일 시험과 Web 전체 계측 34개 통과
  - 전체 debug 회귀 204 tasks, JVM 57개, release 158 tasks와 APK 검증 통과
  - Web POC RC05 보관본 크기 3,080,528 bytes, SHA-256
    `E09399F0F90A8353D2EBD396647D4436394CA739BCFD65107ABBD66E4E161A0C`
  - 설치 전 RC04/code 21에서 RC05/code 22로 `adb install -r`; UID `10287`,
    firstInstallTime, dataDir, Device Owner, HOME, `LOCKED`, Kiosk RC06과
    화면 `Dozing` 유지
  - 실제 Kiosk→Web 정상 호출과 QR→Web→QR 왕복은 사용자 복귀 뒤 수행
- secure session 전송 계약 복구 커밋 `adf8545`를 추가하고 Kiosk RC07/Web
  POC RC06으로 A에 설치했다.
  - 기존 custom action과 보호된 `content://` data URI 조합이 Android 13
    교차 앱 실행에서 `ActivityNotFoundException`으로 실패함을 debug
    Kiosk→Web 시험으로 재현
  - 1회용 handle ID만 explicit intent extra로 전달하고 Web이 고정 credential
    authority URI를 내부 구성하도록 변경
  - handle은 정확히 32자 Base64URL 형식만 허용하며 secure/recovery action을
    Web manifest 계약에 명시
  - 수정 뒤 debug Kiosk→Web secure 호출이 credential bridge 단계의
    `CREDENTIAL_BRIDGE_EMPTY`까지 도달하고 trusted recovery `RESULT_OK` 통과
  - 최종 버전 커밋 `f0fe25a`; 전체 debug 204 tasks, JVM 58개,
    Web 계측 34개, Kiosk 계측 16개, release 158 tasks와 APK 검증 통과
  - Kiosk RC07 보관본 크기 34,957,624 bytes, SHA-256
    `7B423D6AFC1ECB7F53805259F218DBDB0DC689E822F83E4637273BA76B8451E4`
  - Web POC RC06 보관본 크기 3,081,400 bytes, SHA-256
    `ECFA865715427B32D5308B92135A75D8652811AFB3813C63FE47D7B6AED55544`
  - A 설치 전후 두 package UID, firstInstallTime, dataDir, signer, Device Owner,
    HOME, `LOCKED`, 화면 `Dozing` 유지; crash buffer의 Matholic 일치 항목 0
  - 실제 QR→Web→QR 왕복은 사용자 복귀 뒤 수행
- QR PDF 공유 권한 보강 커밋 `f0d3e1a`를 추가하고 Kiosk RC08으로 A에
  설치했다.
  - `ACTION_SEND`의 FileProvider URI를 `EXTRA_STREAM`뿐 아니라 `ClipData`에도
    넣어 `FLAG_GRANT_READ_URI_PERMISSION`이 chooser와 수신 앱에 적용되게 함
  - Android 13 일회용 에뮬레이터에서 source intent와 chooser의 URI·권한,
    Kiosk 전체 계측 17개 통과
  - 전체 clean debug 204 tasks, release 158 tasks와 APK 검증 통과
  - 같은 Web RC06 재빌드에서 Git 메타데이터만 달라진 APK가 기존 버전 파일을
    덮어쓰는 운영 결함을 확인하고 커밋 `a1c8159`에서 보강
  - 같은 버전 artifact는 `META-INF/version-control-info.textproto`를 제외한
    전체 payload가 같을 때만 기존 검증본을 보존하고, 다르면 버전 상향 전까지
    release publishing을 실패시킴
  - Kiosk RC08 보관본 크기 34,957,624 bytes, SHA-256
    `509919229E1230E6E7F28BEED46362D8EF67502A3ACF01E161F7DA4152B0E998`
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Device Owner,
    HOME, 화면 `Dozing` 유지; 명시적 HOME 시작 뒤 `LOCKED` 복구
  - Web RC06 설치본과 보관본은 기존 SHA-256
    `ECFA865715427B32D5308B92135A75D8652811AFB3813C63FE47D7B6AED55544`
    그대로 유지
  - 실제 A의 Quick Share 전송과 수신 PC 열기·인쇄는 사용자 복귀 뒤 수행
- 학생 Web fragment SPA 경로 차단 커밋 `86d6cc5`를 추가하고 Web POC
  RC07으로 A에 설치했다.
  - `/workbook#/course`처럼 허용 path를 유지한 채 fragment로 다른 SPA 화면을
    선택하는 URL이 기존 네이티브 정책에서 허용됨을 신규 JVM 시험으로 재현
  - 학생 URL·`pathOf`, 현재 DOM 화면 검사, 링크 클릭 가드와 오답 번호 추출이
    fragment 존재 시 모두 실패폐쇄하도록 변경
  - 수정 전 신규 JVM 시험 실패, 수정 뒤 전체 JVM 59개 통과
  - Android 13 일회용 에뮬레이터에서 Web DOM 대상 18개와 전체 계측 37개 통과
  - 전체 clean debug 204 tasks, release 158 tasks와 APK 이중 검증 통과
  - Web RC07 보관본 크기 3,081,628 bytes, SHA-256
    `1B7995D52FBF969757BE0E29258A38862CB1ADB449B8939601AD5ACFF3B3731A`
  - Kiosk RC08 artifact는 payload 동일 검사를 거쳐 기존 해시를 보존
  - A 설치 전후 Web UID `10287`, firstInstallTime, dataDir, Kiosk RC08,
    Device Owner, HOME, `LOCKED`, 화면 `Dozing` 유지; crash 일치 항목 0
  - 실제 사이트의 정상 query 경로와 fragment 차단 실기는 사용자 복귀 뒤 수행
- Web 로그인 form endpoint 검증 커밋 `95c7d23`을 추가하고 Web POC RC08로
  A에 설치했다.
  - 기존 DOM sanitizer와 실제 submit은 protocol·hostname·path만 확인해
    비표준 port, userinfo, query와 fragment가 붙은 인증 endpoint도 허용
  - 수정 전 신규 Web DOM 계측 20개 중 변형 endpoint 거부와 자격정보
    미입력 시험 2개 실패를 재현
  - 정확한 HTTPS 인증 host의 기본 port·`/token/signin`, 빈
    username·password·query·fragment일 때만 form을 사용하도록 실패폐쇄
  - 수정 뒤 대상 계측 20개와 Web 전체 계측 39개, JVM 59개, 전체 clean
    debug 204 tasks, release 158 tasks와 APK 이중 검증 통과
  - Web RC08 보관본 크기 3,082,472 bytes, SHA-256
    `AEFE475F1B9AED658FDE6F2DDC32D4CC1DDAEC5DE83E34DACA48C8384BC8E3C1`
  - A 설치 전후 Web UID `10287`, firstInstallTime, dataDir, Kiosk RC08,
    release signer, Device Owner, HOME, `LOCKED`, 화면 `Dozing` 유지;
    crash 일치 항목 0
  - 실제 공개 사이트의 로그인 form endpoint와 QR→Web→QR 왕복은 사용자
    복귀 뒤 수행
- Web DOM origin 일치 보강 커밋 `e6919e8`을 추가하고 Web POC RC09로
  A에 설치했다.
  - 포털 지문·로그아웃 동작, 학생 UI·결과 요약과 링크 가드가 hostname
    중심으로 검사해 비표준 port와 userinfo 변형을 DOM 자체에서 허용
  - 수정 전 신규 Web DOM 계측 24개 중 변형 current origin, 자격정보 포함
    의미 링크와 학생 링크 거부 시험 4개 실패를 재현
  - 기본 HTTPS `im.matholic.com` origin, 빈 userinfo·fragment 조건을 모든
    관련 DOM 보조가 직접 검사하고 학생 링크는 비표준 port도 즉시 차단
  - 수정 뒤 대상 계측 24개와 Web 전체 계측 43개, JVM 59개, 전체 clean
    debug 204 tasks 통과
  - 첫 release 158 tasks는 성공했으나 검증기의 RC08 기대값이 RC09 APK를
    거부해 artifact 게시 전 실패; 커밋 `f8fc9e3`에서 build·verify·초기
    provisioning 기본 경로를 RC09로 맞추고 재실행해 158 tasks와 APK 이중
    검증 통과
  - Web RC09 보관본 크기 3,083,756 bytes, SHA-256
    `EE3349ED05D371FBF172A9221D6A0CE14F1DC5C6E642E79C987CD0EB415DEBDD`
  - A 설치 전후 Web UID `10287`, firstInstallTime, dataDir, Kiosk RC08,
    release signer, Device Owner, HOME, `LOCKED`, 화면 `Dozing` 유지;
    crash 일치 항목 0
  - 실제 공개 사이트의 포털·학생 DOM origin과 QR→Web→QR 왕복은 사용자
    복귀 뒤 수행
- Web 포털 경로 제한 커밋 `02550ca`를 추가하고 Web POC RC10으로 A에
  설치했다.
  - 기존 `isPortalUrl`은 `im.matholic.com`의 모든 경로를 포털로 분류했고
    포털 DOM도 공통 메뉴가 있으면 `/userInfo` 같은 문서에서 통과
  - 수정 전 신규 JVM 정책 6개 중 1개와 Web DOM 대상 25개 중 1개 실패를
    재현
  - 학습 host 판정과 포털 문서 판정을 분리하고 query를 허용한 정확한
    `/course`·fragment 없음 조건을 네이티브 상태 전이와 DOM에 동시 적용
  - 로그인 확인 중 비-course 문서는 `PORTAL_ROUTE`로 실패폐쇄하고,
    로그아웃 중에는 기존 제한 재시도 정책으로 처리
  - 수정 뒤 대상 JVM 6개, DOM 25개, Web 전체 계측 44개, 전체 JVM 60개,
    clean debug 204 tasks와 release 158 tasks·APK 이중 검증 통과
  - Web RC10 보관본 크기 3,084,104 bytes, SHA-256
    `5D5503D4EE5D63B1EB872C27B9A12516177C7A56C9ED502BD8F3A66A7D723814`
  - A 설치 전후 Web UID `10287`, firstInstallTime, dataDir, Kiosk RC08,
    release signer, Device Owner, HOME, `LOCKED`, 화면 `Dozing` 유지;
    crash 일치 항목 0
  - 실제 공개 사이트의 로그인 후 `/course` 경로와 query 사용 여부,
    QR→Web→QR 왕복은 사용자 복귀 뒤 수행
- 반 삭제 데이터 보존 회귀시험 커밋 `17535cb`를 추가했다.
  - 기존 시험은 삭제한 반과 학생 QR 해시 보존만 직접 확인했으나, 다른 반
    소속과 암호화 자격정보까지 그대로인지 명시적으로 검증하지 않았다.
  - 반 A 삭제 뒤 반 B 소속, 학생 활성 상태, QR 해시, 아이디·비밀번호
    ciphertext와 IV, 원래 자격정보의 복호화 가능성, 반 B의 동일 QR 인증을
    한 시험에서 모두 확인하도록 보강
  - Android 13 일회용 에뮬레이터에서 대상 시험 1개와 Kiosk 전체 계측
    17개 통과
  - 운영 코드와 A 설치본은 변경하지 않았으며 실제 관리자 화면에서의 반
    삭제·다중 반 소속 실기는 사용자 복귀 뒤 수행
- Web 로그인 문서 경로 제한 커밋 `e67b655`을 추가하고 Web POC RC11로
  A에 설치했다.
  - 기존 native 로그인 판정과 DOM 자격정보 입력은 `login.matholic.com`
    host만 확인해, 같은 host의 임의 경로가 같은 form을 노출하면 로그인
    문서로 처리
  - 공개 서버의 `/not-a-login-document`도 HTTP 200으로 응답함을 자격정보
    없이 확인하고, 수정 전 신규 JVM 정책 7개 중 1개 실패를 재현
  - 정확한 기본 HTTPS 로그인 origin의 루트 `/`와 fragment 없음 조건을
    native 상태 전이와 DOM sanitizer·submit에 함께 적용하고 query는 허용
  - 수정 뒤 대상 JVM 7개, 신규 DOM 1개, Web 전체 계측 45개, 전체 JVM
    61개, clean debug 204 tasks 통과
  - 첫 release 158 tasks와 APK 검증은 성공했으나 artifact payload 비교가
    Windows PowerShell에 없는 `Convert.ToHexString` 호출로 게시 전에 실패
  - 커밋 `37deb21`에서 `BitConverter` 기반 대문자 hex 변환으로 호환성을
    복구하고 구문 분석·실제 SHA-256 비교 뒤 release 158 tasks와 원본·보관
    APK 이중 검증을 재실행해 통과
  - Web RC11 보관본 크기 3,084,304 bytes, SHA-256
    `2376DE8B4D68FCC9F40A9D3E2D0CC1D66743A3347ABB2731AB6769BEDC0CA37B`
  - A 설치 전후 Web UID `10287`, firstInstallTime, dataDir, Kiosk RC08,
    release signer, Device Owner, HOME, `LOCKED`, 화면 `Dozing` 유지;
    crash 일치 항목 0
  - 실제 공개 사이트의 로그인 루트·query와 QR→Web→QR 왕복은 사용자 복귀
    뒤 수행
- QR 카드 PDF 실물 좌표계 수정 커밋 `c5acc5a`를 추가하고 Kiosk RC09으로
  A에 설치했다.
  - Android `PrintedPdfDocument` 캔버스는 프린터 DPI가 아니라 72
    PostScript point/inch인데 기존 renderer는 300·600 DPI를 좌표 단위로
    사용해 카드와 QR을 과대 확대
  - 수정 전 신규 계측 3개 중 2개 실패: 계산 카드 폭 541.7mm, 실제 PDF
    외곽선 폭 약 172.5mm를 재현
  - 모든 카드·QR·여백·텍스트 좌표를 72 point/inch로 변환하고, 인쇄 가능
    영역이 65×90mm보다 작으면 조용히 축소하지 않고 명시적 오류로 거부
  - 수정 뒤 PDF 대상 계측 4개와 Kiosk 전체 계측 18개, 전체 JVM 61개,
    clean debug 204 tasks, release 158 tasks와 APK 이중 검증 통과
  - Kiosk RC09 보관본 크기 34,957,624 bytes, SHA-256
    `68DB8304B87528DDE006B86A27E9B282899069D3F3F74165718455F2640A967D`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    전용 HOME 시작으로 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC11,
    release signer, Device Owner, HOME, 화면 `Dozing` 유지; crash 일치
    항목 0
  - 실제 인쇄물 자 측정·Quick Share·종이 QR 재인식은 사용자 복귀 뒤 수행
- 오래된 QR 분석 결과 차단 커밋 `32e41b9`를 추가하고 Kiosk RC10으로 A에
  설치했다.
  - 관리자 PIN 대화상자를 열 때 분석기를 중지해도 이미 ML Kit에서 처리
    중이던 프레임 결과는 뒤늦게 도착할 수 있었고, 기존 UI 전달부는 scanner
    화면 표시 여부만 검사해 관리자 화면 위에서 Web 로그인을 시작할 수 있었음
  - 각 처리 프레임에 분석 세대를 부여하고 분석 중지·재개 때 세대를 바꿔
    이전 결과를 폐기하며, 폐기한 승인 결과의 QR token hash를 즉시 덮어씀
  - UI 전달 직전에도 분석 활성·화면·Activity 생명주기를 다시 확인하고
    조건이 바뀌었으면 민감 결과를 폐기
  - 신규 JVM 회귀시험 3개, 네 모듈 JVM 64개, clean debug 204 tasks,
    Android 13 일회용 에뮬레이터 Kiosk 전체 계측 18개, release 158 tasks와
    APK 이중 검증 통과
  - Kiosk RC10 준비 커밋 `4e9fe30`
  - Kiosk RC10 보관본 크기 34,957,624 bytes, SHA-256
    `7BFF17A3D74C9DB2C699BE4EE4F3AEE3175F4A39C72B5032D590EAE4C7870187`
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC11,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing` 유지
  - 설치 직후 Lock Task `NONE`을 화면을 깨우지 않는 명시적 HOME 시작으로
    `LOCKED` 복구했으며 최근 5분 Matholic AndroidRuntime 일치 항목 0
  - 실제 QR 분석과 관리자 PIN 화면 진입을 겹치는 실기는 사용자 복귀 뒤 수행
- Web renderer 종료 복구 커밋 `4d54dee`를 추가하고 Web POC RC12로 A에
  설치했다.
  - 기존 `onRenderProcessGone`은 `WEB_PROCESS_GONE`으로 잠그고 `true`를
    반환하면서 사용할 수 없는 WebView를 뷰 계층과 Activity 필드에 남겨
    Android의 renderer 종료 처리 계약을 위반
  - 일회용 에뮬레이터에서 공식 시험용 `chrome://crash`로 수정 전 상태 잠금
    뒤에도 죽은 WebView가 남는 실패를 재현
  - renderer 종료 시 해당 WebView를 계층에서 제거·파기하고 참조를 해제한
    뒤 실패폐쇄하며, 복구 버튼의 Activity 재생성에서 새 WebView를 생성
  - 대상 충돌·복구 계측 1개, Web 전체 계측 46개, 네 모듈 JVM 64개,
    clean debug 204 tasks, release 158 tasks와 APK 이중 검증 통과
  - Web RC12 준비 커밋 `0695240`
  - Web RC12 보관본 크기 3,084,508 bytes, SHA-256
    `469493E02F1554279F3C6F7ACFBA0A149568225524013A55FA5CA20CDC45C880`
  - A 설치 전후 Web UID `10287`, firstInstallTime, dataDir, Kiosk RC10,
    release signer, Device Owner, 전용 HOME, `LOCKED`, 화면 `Dozing` 유지;
    최근 5분 Matholic AndroidRuntime 일치 항목 0
  - A에는 고의 renderer 충돌을 주입하지 않았으며 실제 QR→Web→QR 왕복과
    renderer 종료 뒤 관리자 복구 실기는 사용자 복귀 뒤 수행
- 준비 완료 Web 세션의 화면 전환 뒤 실행 차단 커밋 `f613467`을 추가하고
  Kiosk RC11으로 A에 설치했다.
  - QR 검증 뒤 DB를 `PRELOGIN_CHECK`로 바꾸고 자격정보 handle을 준비하는
    동안 교사 관리로 전환하면 `scannerVisible=false`가 되지만, 기존 완료
    콜백은 Activity 파기 여부만 검사해 PIN 화면 위로 Web 세션을 시작 가능
  - 수정 전 신규 정책 JVM 시험 3개 중 scanner 이탈 취소 시험 1개 실패
  - Activity 파기 시 handle만 폐기해 재시작 실패폐쇄에 맡기고, 살아 있는
    Activity가 scanner를 떠났으면 handle을 폐기한 뒤 현재 상태가 정확히
    `PRELOGIN_CHECK`일 때만 `QR_READY`로 복원
  - 대상 정책 JVM 3개, 네 모듈 JVM 67개, clean debug 204 tasks,
    Android 13 일회용 에뮬레이터 Kiosk 전체 계측 18개, release 158 tasks와
    APK 이중 검증 통과
  - Kiosk RC11 준비 커밋 `a82f606`
  - Kiosk RC11 보관본 크기 34,957,624 bytes, SHA-256
    `E7E094EA353E924E151885C068559BD61FDC7DCAA8B2A314686F982CCD9788A9`
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC12,
    release signer, Device Owner, 전용 HOME, 최종 `LOCKED`, 화면 `Dozing`
    유지; 최근 5분 Matholic AndroidRuntime 일치 항목 0
  - 설치 스크립트 최종 출력 오타로 RC11 설치 직후의 일시 Lock Task 값은
    기록되지 않았고 최종 상태만 독립 확인
  - 실제 QR 승인 직후 교사 관리 전환 경합 실기는 사용자 복귀 뒤 수행
- 오래된 Web 문서 완료 콜백 차단 커밋 `8895d5f`를 추가하고 Web POC
  RC13으로 A에 설치했다.
  - 새 최상위 문서 이동이 시작된 뒤 이전 로그인·포털 문서의
    `onPageFinished`가 늦게 도착하면 기존 코드는 현재 문서 확인 없이 로그인
    또는 로그아웃 상태 전이를 실행할 수 있었음
  - 수정 전 신규 정책 JVM 시험에서 현재 포털 위의 이전 로그인 완료와 현재
    학습지 위의 이전 포털 완료를 모두 처리하는 실패를 재현
  - 콜백 URL과 WebView의 현재 최상위 URL이 정확히 일치할 때만 완료 콜백을
    처리하고, 다르면 상태와 DOM을 변경하지 않고 폐기
  - 네 모듈 JVM 68개, clean debug 204 tasks, Android 13 일회용
    에뮬레이터 Web 전체 계측 46개, release 158 tasks와 APK 이중 검증 통과
  - Web RC13 준비 커밋 `1ae4b6c`
  - Web RC13 보관본 크기 3,084,708 bytes, SHA-256
    `377C824C3900CA05F96F5C5A8C9F6FA2A85EA8AB8B94B07379F2BBA1869B4BDD`
  - A 설치 전후 Web UID `10287`, firstInstallTime, dataDir, Kiosk RC11,
    release signer, Device Owner, 전용 HOME, `LOCKED`, 화면 `Dozing` 유지;
    설치된 base APK와 보관본 해시 일치, 최근 5분 Matholic AndroidRuntime
    일치 항목 0
  - 실제 빠른 페이지 전환·로그아웃과 QR→Web→QR 왕복은 사용자 복귀 뒤 수행
- QR PDF 지연 삭제 수명주기 보강 커밋 `acae022`를 추가하고 Kiosk RC12로
  A에 설치했다.
  - PDF 공유 화면에서 복귀하면 기존 코드는 30초 삭제를 Activity의 공용
    UI Handler에 예약한 뒤 파일 참조를 해제했고, 그 사이 Activity가
    파기되면 `onDestroy`가 예약까지 취소해 로그인 가능한 QR PDF가 캐시에
    만료 정리 시점까지 남을 수 있었음
  - 지연 삭제를 Activity를 캡처하지 않는 process 범위 Handler로 옮기고
    앱 캐시의 `qr_exports` 직계 파일만 대상으로 허용
  - 합성 PDF 지연 삭제와 export 디렉터리 외부 삭제 거부 계측 2개를 추가해
    Android 13 일회용 에뮬레이터 Kiosk 전체 계측 20개 통과
  - 네 모듈 JVM 68개, clean debug 204 tasks, release 158 tasks와 APK
    이중 검증 통과
  - Kiosk RC12 준비 커밋 `4ca2048`
  - Kiosk RC12 보관본 크기 34,957,624 bytes, SHA-256
    `64E28E61822D5C5A01887DA47C7ECBAEB3C9D99358D7EF52F1DC5CCD19626D71`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작으로 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC13,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing` 유지; 설치된
    base APK와 보관본 해시 일치, 최근 5분 Matholic AndroidRuntime 일치
    항목 0
  - 실제 Quick Share 전달 뒤 30초 삭제와 수신 PC 열기·인쇄는 사용자 복귀
    뒤 수행
- QR PDF 내보내기 전 복제 bitmap 정리 보강 커밋 `0fc357c`를 추가하고
  Kiosk RC13으로 A에 설치했다.
  - 기존 코드는 QR bitmap 복제 뒤 감사기록을 먼저 저장하고 PDF exporter를
    호출했으며, 감사기록이 실패하고 Activity까지 파기되면 어느 쪽도 복제본을
    덮어쓰거나 폐기하지 않아 GC 전까지 로그인 가능한 QR 이미지가 메모리에
    남을 수 있었음
  - exporter 진입 전 작업까지 포함하는 단일 소유권 경계를 추가하고 성공·실패
    여부와 관계없이 mutable bitmap을 흰색으로 덮어쓴 뒤 recycle하도록 통합
  - 실제 QR 원문 없이 합성 bitmap과 고의 예외를 사용한 계측시험을 먼저
    추가했으며 수정 전 누락된 소유권 계약으로 컴파일 실패, 수정 뒤 통과
  - Android 13 일회용 에뮬레이터 Kiosk 전체 계측 21개, 네 모듈 JVM 68개,
    clean debug 204 tasks, release 158 tasks와 APK 이중 검증 통과
  - Kiosk RC13 준비 커밋 `9c94991`
  - Kiosk RC13 보관본 크기 34,957,624 bytes, SHA-256
    `8DA89E1A4086CDAB2CF4BA2E676DD04E44053F9F1197947C35696E833E68C9CC`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작으로 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC13,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing` 유지; 설치된
    base APK와 보관본 해시 일치, 최근 5분 Matholic AndroidRuntime 일치
    항목 0
  - 실제 QR PDF 내보내기·Quick Share·실물 인쇄는 사용자 복귀 뒤 수행
- Activity 종료 시 대기 중 민감 작업 정리 커밋 `bfbfaf4`를 추가하고 Kiosk
  RC14로 A에 설치했다.
  - 기존 `ioExecutor.shutdownNow()`는 아직 시작하지 않은 작업을 반환하지만
    무시해, 대기 Runnable에 캡처된 관리자 PIN·학생 아이디/비밀번호·QR
    token hash·PDF용 QR bitmap의 작업 내부 정리 코드가 실행되지 않을 수
    있었음
  - 실행과 폐기 중 먼저 소유권을 얻은 한 경로만 작업하거나 정리하는
    `SensitiveTask`를 추가하고, 종료 시 반환된 대기 작업을 즉시 폐기
  - PIN 2개 경로, 학생 자격정보 2개 경로, QR 검증 hash와 PDF 복제 bitmap에
    적용했으며 정상 실행·예외·대기열 폐기 모두 정확히 한 번 정리
  - 수정 전 신규 JVM 계약은 클래스 부재로 컴파일 실패, 수정 뒤 대상 2개와
    네 모듈 JVM 70개 통과
  - Android 13 일회용 에뮬레이터 Kiosk 전체 계측 21개, clean debug
    204 tasks, release 158 tasks와 APK 이중 검증 통과
  - Kiosk RC14 준비 커밋 `5a99e73`
  - Kiosk RC14 보관본 크기 34,957,624 bytes, SHA-256
    `1F2D2EF7F124DCAE6F91C5A6132C8F135CDD5E1D5F11642509C4278532C6CFED`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작으로 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC13,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing` 유지; 설치된
    base APK와 보관본 해시 일치, 최근 5분 Matholic AndroidRuntime 일치
    항목 0
  - 실제 Activity 종료와 민감 작업 대기를 겹치는 기기 실패주입, 실제
    QR·관리자 PIN·PDF 흐름은 사용자 복귀 뒤 수행
- Web 로그아웃 재시도 세대 차단 커밋 `1b2bc92`를 추가하고 Web POC RC14로
  A에 설치했다.
  - 첫 로그아웃 시도의 DOM 평가·지연·timeout 콜백이 20초 timeout 뒤 같은
    `LOGOUT_NAVIGATE`/`LOGOUT_SUBMIT` 상태의 두 번째 시도에서 늦게 돌아오면
    기존 상태 검사만 통과해 새 문서의 계정 메뉴·로그아웃 동작을 중복 실행하거나
    정상 재시도를 잠글 수 있었음
  - 각 로그아웃 시도에 증가 세대를 부여하고 fingerprint, 계정 메뉴,
    로그아웃 클릭·재탐색, timeout과 cookie 삭제 완료 콜백이 상태와 세대를
    모두 만족할 때만 동작
  - 수정 전 신규 JVM 계약은 정책 부재로 컴파일 실패, 수정 뒤 대상 정책과
    네 모듈 JVM 71개 통과
  - Android 13 일회용 에뮬레이터 Web 전체 계측 46개, clean debug
    204 tasks, release 158 tasks와 APK 이중 검증 통과
  - Web RC14 준비 커밋 `ad7edbb`
  - Web RC14 보관본 크기 3,085,508 bytes, SHA-256
    `476BAC1C0546EE9876F7B7985E567E596E478838A0D01327C974193335492EFD`
  - A 설치 전후 Web UID `10287`, firstInstallTime, dataDir, Kiosk RC14,
    release signer, Device Owner, 전용 HOME, `LOCKED`, 화면 `Dozing` 유지;
    설치된 base APK와 보관본 해시 일치, 최근 5분 Matholic AndroidRuntime
    일치 항목 0
  - 실제 공개 사이트에서 timeout과 빠른 로그아웃 재시도를 겹치는 실기,
    QR→Web→QR 왕복은 사용자 복귀 뒤 수행
- 방치된 공유 QR PDF 수명 상한 커밋 `0b9b746`을 추가하고 Kiosk RC15로
  A에 설치했다.
  - 기존 30초 삭제는 공유 화면에서 Kiosk로 정상 복귀한 `onStart`에서만
    예약되어, 공유 중 Activity가 파기되면 파일 참조를 잃고 다음 1시간
    만료 정리까지 로그인 가능한 PDF가 캐시에 남을 수 있었음
  - 공유 선택기를 열기 전에 process 범위 Handler에 기존 보존 한도와 같은
    최대 1시간 삭제를 예약하고, 정상 복귀하면 기존 30초 삭제도 추가 예약
  - 실제 QR 원문 없이 합성 PDF를 사용한 계측시험을 먼저 추가했으며 수정 전
    `scheduleSharedFileExpiry` 부재로 컴파일 실패, 수정 뒤 대상 1개 통과
  - Android 13 일회용 에뮬레이터 Kiosk 전체 계측 22개, 네 모듈 JVM
    71개, clean debug 204 tasks, release JVM 62개와 release 158 tasks·APK
    이중 검증 통과
  - Kiosk RC15 준비 커밋 `17992f0`
  - Kiosk RC15 보관본 크기 34,957,624 bytes, SHA-256
    `7F56D96A6C1EF3A54B58AA07EB75829C2392BE5A1DC1F22E409C79A10D5AB92B`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작으로 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC14,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing` 유지; 설치된
    base APK와 보관본 해시 일치, 최근 5분 Matholic AndroidRuntime 일치
    항목 0
  - 실제 Quick Share 중 Activity 파기와 수신 PC 열기·실물 인쇄는 사용자
    복귀 뒤 수행
- Web 로그인 확인 콜백 세대 차단 커밋 `9dabba0`을 추가하고 Web POC
  RC15로 A에 설치했다.
  - 같은 `/course` 문서가 연속 완료되면 이전 portal fingerprint 평가와 지연
    재시도 콜백이 새 확인 시도와 같은 `LOGIN_VERIFY` 상태를 통과해, 이전
    문서의 이름·fingerprint 결과로 정상 로그인을 잠그거나 잘못 진행할 수
    있었음
  - 포털 로그인 확인 시작마다 기존 login probe 세대를 증가시키고 fingerprint
    평가 전·후와 모든 지연 재시도에서 상태와 세대를 함께 검사
  - 수정 전 신규 JVM 계약은
    `shouldProcessStateGenerationCallback` 부재로 컴파일 실패, 수정 뒤 대상
    정책과 네 모듈 JVM 72개 통과
  - Android 13 일회용 에뮬레이터 Web 전체 계측 46개, clean debug
    204 tasks, release JVM 63개와 release 158 tasks·APK 이중 검증 통과
  - Web RC15 준비 커밋 `5b531dc`
  - Web RC15 보관본 크기 3,085,840 bytes, SHA-256
    `3BBFFACA2AB6F9A48FFC4F5D34E9559B2B87053CEF1BDF54E844D46169C9993B`
  - A 설치 전후 Web UID `10287`, firstInstallTime, dataDir, Kiosk RC15,
    release signer, Device Owner, 전용 HOME, `LOCKED`, 화면 `Dozing` 유지;
    설치된 base APK와 보관본 해시 일치, 최근 5분 Matholic AndroidRuntime
    일치 항목 0
  - 실제 공개 사이트에서 같은 `/course` 연속 완료와 fingerprint 경합,
    QR→Web→QR 왕복은 사용자 복귀 뒤 수행
- 관리자 새로고침의 더 새로운 반 선택 보존 커밋 `30ec5e3`을 추가하고
  Kiosk RC16으로 A에 설치했다.
  - 반 생성·삭제나 Web 정리 뒤 목록을 읽는 동안 교사가 다른 반을 선택하면
    기존 완료 콜백이 새로고침 시작 때 캡처한 반과 명단을 무조건 다시 적용해
    방금 선택한 반이 되돌아갈 수 있었음
  - 새로고침 시작 때 반 선택 revision을 캡처하고, 활성 수업이 없으며 완료
    시점의 더 새로운 선택 반이 여전히 존재하면 이전 목록 결과가 선택과 명단
    로딩 상태를 덮지 않도록 변경
  - 활성 수업 반은 계속 강제하고 새 선택 반이 삭제된 경우에는 새로 읽은
    유효 반으로 안전하게 fallback
  - 수정 전 신규 JVM 계약은 `snapshotSelection`·`resolveRefresh` 부재로
    컴파일 실패, 수정 뒤 관리자 비동기 상태 시험 9개와 네 모듈 JVM 75개 통과
  - Android 13 일회용 에뮬레이터 Kiosk 전체 계측 22개, clean debug
    204 tasks, release JVM 66개와 release 158 tasks·APK 이중 검증 통과
  - Kiosk RC16 준비 커밋 `ee87b98`
  - Kiosk RC16 보관본 크기 34,957,624 bytes, SHA-256
    `080FE9AF7865740551B7D2FFF6A0788C9FECDC3261C5F5901CFDA8838ADA1A87`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작으로 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing` 유지; 설치된
    base APK와 보관본 해시 일치, 설치 시점 이후 Matholic AndroidRuntime
    일치 항목 0
  - 실제 관리자 화면의 목록 새로고침과 빠른 반 전환 경합, QR→Web→QR
    왕복은 사용자 복귀 뒤 수행
- 관리자 새로고침의 더 새로운 학생 선택 보존 커밋 `984e3fb`을 추가하고
  Kiosk RC17로 A에 설치했다.
  - Web 정리 등으로 학생 목록을 읽는 동안 교사가 다른 학생을 선택하면,
    기존 완료 콜백이 새로고침 시작 때 캡처한 학생을 다시 선택해 방금 선택한
    학생이 되돌아갈 수 있었음
  - 학생 Spinner의 실제 선택 ID와 revision을 추적하고, 완료 시점의 더
    새로운 선택 학생이 여전히 존재하면 이전 선호값이 선택을 덮지 않도록 변경
  - 학생 등록·이름/계정정보 수정은 기존 명시적 대상 학생을 계속 선택하고,
    선택 학생이 비활성화돼 사라지면 남은 첫 학생으로 안전하게 fallback
  - 수정 전 신규 JVM 계약은 `RefreshableSelectionState` 부재로 컴파일 실패,
    수정 뒤 관리자 비동기 상태 시험 12개와 네 모듈 JVM 78개 통과
  - Android 13 일회용 에뮬레이터 Kiosk 전체 계측 22개, clean debug
    204 tasks, release JVM 69개와 release 158 tasks·APK 이중 검증 통과
  - Kiosk RC17 준비 커밋 `7f3e924`
  - Kiosk RC17 보관본 크기 34,957,624 bytes, SHA-256
    `CCCAC3270E8C0D879330A7EC2EF33EA4C1B033EDD0A4BAB6145BD2522EF1B352`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작으로 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing` 유지; 설치된
    base APK와 보관본 해시 일치, 설치 시점 이후 Matholic AndroidRuntime
    일치 항목 0
  - 실제 관리자 화면의 목록 새로고침과 빠른 학생 전환 경합, QR→Web→QR
    왕복은 사용자 복귀 뒤 수행
- 관리자 목록 새로고침 실패 복구 커밋 `7786ee0`을 추가하고 Kiosk RC18로
  A에 설치했다.
  - 기존 `refreshAdminData`의 DB 읽기 예외가 executor 밖으로 빠져 프로세스를
    종료하거나, 저장에 성공한 학생 변경의 단일 실행 gate와 관리자 UI를
    영구 대기 상태로 남길 수 있었음
  - Android 13 일회용 에뮬레이터에서 저장소를 고의로 사용할 수 없게 만든
    신규 계측시험으로 수정 전 `UninitializedPropertyAccessException`과
    프로세스 종료를 재현
  - 전체 목록 snapshot 읽기를 단일 실패 경계로 묶고, 실패 시 이전 목록과
    수업·선택 상태를 유지하면서 학생 변경 gate를 해제하고 명시적인 재시도
    안내를 표시
  - 수정 뒤 Kiosk 전체 계측 23개, 네 모듈 JVM 78개, clean debug 204 tasks,
    release JVM 69개와 release 158 tasks·APK 이중 검증 통과
  - Kiosk RC18 준비 커밋 `8fc6e97`
  - Kiosk RC18 보관본 크기 34,974,012 bytes, SHA-256
    `48E03E8CBD14016C337A9DC9548139B4BD0F49FC4149015255504F601E274CDE`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작 뒤 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing` 유지; 설치된
    base APK와 보관본 해시 일치, 설치 시점 이후 Matholic AndroidRuntime
    일치 항목 0
  - 실제 관리자 화면의 DB 실패·재시도, QR→Web→QR 왕복과 카메라·PDF·인쇄
    실기는 사용자 복귀 뒤 수행
- 반 소속 명단 조회 실패의 실패폐쇄 커밋 `74549c4`를 추가하고 Kiosk
  RC19로 A에 설치했다.
  - 기존 개별 반 조회는 DB 예외를 빈 `Set`으로 바꿔 실제 빈 반처럼 표시하고
    반 학생 구성·삭제·보강·수업 시작을 활성화했으며, 교사가 그대로 저장하면
    기존 소속 관계를 모두 지울 수 있었음
  - 실패와 빈 명단을 별도 상태로 관리하고 현재 요청의 실패만 반영하며, 이전
    요청의 늦은 실패는 현재 반 상태를 바꾸지 않도록 변경
  - 실패 시 명시적 오류와 재시도 안내를 표시하고 반 구성·삭제·보강·수업
    시작을 모두 차단
  - 수정 전 신규 JVM 계약은 `hasLoadFailure`·`fail` 부재로 컴파일 실패,
    수정 뒤 네 모듈 JVM 80개, Kiosk 전체 계측 24개, clean debug 204 tasks,
    release JVM 71개와 release 158 tasks·APK 이중 검증 통과
  - Kiosk RC19 준비 커밋 `2cb6617`
  - Kiosk RC19 보관본 크기 34,974,008 bytes, SHA-256
    `13BAD1F3408B355B25A5CCC021B8EA6C9483F6E2B6CABA1EE7B64D29B524624F`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작 뒤 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing` 유지; 설치된
    base APK와 보관본 해시 일치, 설치 시점 이후 Matholic AndroidRuntime
    일치 항목 0
  - 실제 관리자 화면의 반 소속 조회 실패와 재시도, QR→Web→QR 왕복과
    카메라·PDF·인쇄 실기는 사용자 복귀 뒤 수행
- 앱 시작 초기 상태 확인 실패 복구 커밋 `e4f9ee7`을 추가하고 Kiosk RC20로
  A에 설치했다.
  - 기존 `loadInitialState`는 관리자 PIN 등록 여부 조회와 재시작 정책 적용의
    예외를 처리하지 않아 `초기 상태 확인 중`에서 앱 프로세스가 종료될 수
    있었음
  - Android 13 일회용 에뮬레이터에서 저장소를 고의로 사용할 수 없게 해 수정
    전 `UninitializedPropertyAccessException`과 프로세스 종료를 재현
  - 초기 확인 중 PIN 입력을 숨기고 다른 화면을 차단하며, 실패 시 저장
    데이터를 변경하지 않았다는 안내와 `다시 시도`만 전용 잠금 안에서 제공
  - 수정 뒤 같은 계측시험에서 실패 화면과 저장소 복원 후 재시도 성공을 확인
  - Kiosk 전체 계측 25개, 네 모듈 JVM 80개, clean debug 204 tasks,
    release JVM 71개와 release 158 tasks·APK 이중 검증 통과
  - Kiosk RC20 준비 커밋 `3af93e9`
  - Kiosk RC20 보관본 크기 34,974,008 bytes, SHA-256
    `313B01CCE83ECD6C7FF480C1013F36DFB2098E6E551B809B081F55EC65FEFC61`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작 뒤 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing` 유지; 설치된
    base APK와 보관본 해시 일치, 설치 시점 이후 Matholic AndroidRuntime
    일치 항목 0
  - A에는 초기 상태 고의 실패를 주입하지 않았으며 실제 실패·재시도 화면,
    QR→Web→QR 왕복과 카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- Web 복귀 결과 확인 실패의 실패폐쇄 커밋 `14a939d`를 추가하고 Kiosk
  RC21로 A에 설치했다.
  - 기존 복귀 처리는 수업 상태 변경 뒤 현재 세션 재조회 실패를 `null`로
    버리고 성공 화면 전환을 계속해 저장 상태와 화면 상태가 어긋날 수 있었음
  - 상태 변경과 현재 세션 재조회를 하나의 성공 조건으로 묶고, 어느 단계든
    실패하거나 활성 세션이 없으면 스캐너를 열지 않고 관리자 PIN 복구 화면을
    표시하도록 변경
  - 신규 JVM 계약 4개를 먼저 추가해 구현 부재 컴파일 실패를 확인하고 수정
    뒤 통과
  - 저장소 접근을 고의로 사용할 수 없게 한 첫 계측시험에서 실패 경계 밖
    메서드 참조 평가로 `UninitializedPropertyAccessException`과 프로세스
    종료를 재현하고, 접근을 경계 안으로 이동한 뒤 같은 시험에서 닫힌 복구
    화면을 확인
  - Kiosk 전체 계측 26개, 네 모듈 JVM 84개, clean debug 204 tasks,
    release JVM 75개와 release 158 tasks·APK 이중 검증 통과
  - Kiosk RC21 준비 커밋 `fc81f9e`
  - Kiosk RC21 보관본 크기 34,974,008 bytes, SHA-256
    `2C2938FC960173DE2EF1FF86065322B0FBE16F2EACB4E8757C1EEC7985C77081`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작 3초 뒤 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing`, USB 화면 유지
    설정 0 유지; 설치된 base APK와 보관본 해시 일치, Matholic
    AndroidRuntime 일치 항목 0
  - A에는 고의 저장소 실패를 주입하지 않았으며 실제 복귀 실패·관리자 복구,
    QR→Web→QR 왕복과 카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- 취소된 Web 준비 상태 복구 실패의 실패폐쇄 커밋 `a970c7d`를 추가하고
  Kiosk RC22로 A에 설치했다.
  - QR 승인 뒤 자격정보 준비 중 관리자 화면으로 전환하면 Web을 실행하지
    않고 `PRELOGIN_CHECK`를 `QR_READY`로 되돌리는데, 기존 복구 예외는
    `null`로 버려져 오류 안내 없이 오래된 화면과 준비 상태가 남을 수 있었음
  - Android 13 일회용 에뮬레이터에서 저장소를 고의로 사용할 수 없게 한
    계측시험을 먼저 추가했고 수정 전 3초 안에 복구 오류 안내가 나타나지
    않는 실패를 재현
  - 상태 전이와 현재 세션 재조회를 하나의 실패 경계로 묶고, 활성 session
    ID와 정확한 `QR_READY` 상태를 모두 확인한 경우에만 복구 성공으로 처리
  - 예외·세션 부재·잘못된 상태에서는 현재 세션 참조를 제거하고 스캐너를
    열지 않은 채 관리자 PIN 복구 화면과 안내를 표시
  - 수정 뒤 대상 시험과 Kiosk 전체 계측 27개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Kiosk RC22 준비 커밋 `555f4ec`
  - Kiosk RC22 보관본 크기 34,974,008 bytes, SHA-256
    `F391504A1B1FAFAB309C43EE3F345D1FF6784EEEDD0400E0C4F2386D8556B5E4`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작 3초 뒤 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing`, USB 화면 유지
    설정 0 유지; 설치된 base APK와 보관본 해시 일치, Matholic
    AndroidRuntime 일치 항목 0
  - A에는 고의 준비 상태 복구 실패를 주입하지 않았으며 실제 QR 준비 취소
    경합·관리자 복구, QR→Web→QR 왕복과 카메라·PDF·인쇄는 사용자 복귀 뒤
    수행
- QR 검증 오류 뒤 스캐너 재활성화 차단 커밋 `bad5d03`을 추가하고 Kiosk
  RC23으로 A에 설치했다.
  - 기존 QR 검증 실패 분기는 `LOCKED`를 표시한 직후 정상적인 미사용 카드와
    같은 cooldown 재개를 예약해 약 1.5초 뒤 `QR_READY`와 QR 분석을 다시
    활성화했음
  - Android 13 일회용 에뮬레이터에서 앱 시작·PIN 인증 뒤 합성 반·학생·수업을
    만들고 합성 QR hash 검증 시 저장소를 고의로 사용할 수 없게 한 계측시험
    으로 수정 전 관리자 복구 안내 미표시 실패를 재현
  - 오류 분기에서 재개 예약을 제거하고 현재 화면 세션 참조를 지운 뒤
    카메라·스캐너를 중지한 관리자 PIN 복구 화면으로 전환
  - 수정 뒤 대상 시험에서 추가 2초 대기 후에도 `LOCKED`, 관리자 PIN 화면,
    스캐너 숨김이 유지됨을 확인
  - Kiosk 전체 계측 28개, 네 모듈 JVM 84개, clean debug 204 tasks,
    release JVM 75개와 release 158 tasks·APK 이중 검증 통과
  - Kiosk RC23 준비 커밋 `fd12b23`
  - Kiosk RC23 보관본 크기 34,974,008 bytes, SHA-256
    `647CB5932F503DF5AB312AD994C9F18C2488A09F7C798C43F78B96B20564CB97`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작 3초 뒤 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing`, USB 화면 유지
    설정 0 유지; 설치된 base APK와 보관본 해시 일치, Matholic
    AndroidRuntime 일치 항목 0
  - A에는 고의 QR 검증 오류를 주입하지 않았으며 실제 QR 오류 뒤 복구,
    QR→Web→QR 왕복과 카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- 카메라 시작 오류 실패폐쇄 커밋 `90c50fa`를 추가하고 Kiosk RC24로 A에
  설치했다.
  - 카메라 제공자 future 조회, 카메라 유무 확인, 분석기 구성, unbind·
    lifecycle bind가 예외 경계 밖에 있어 CameraX 오류가 main executor에서
    앱 종료로 전파되고 QR 스캐너의 안전 복구가 보장되지 않았음
  - Android 13 일회용 에뮬레이터에서 합성 PIN·반·학생·수업으로 스캐너를
    연 뒤 카메라 바인딩 실패 복구 경로를 호출하는 계측시험을 먼저 추가했고,
    수정 전 복구 메서드 부재 실패를 재현
  - 현재 바인딩 세대의 카메라 시작 예외를 포착하고, 카메라 해제 예외와
    무관하게 QR 분석기·스캐너를 닫은 관리자 PIN 복구 화면과
    `CAMERA_ERROR`를 표시
  - 수정 뒤 대상 시험과 Kiosk 전체 계측 29개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Kiosk RC24 준비 커밋 `3ec467b`
  - Kiosk RC24 보관본 크기 34,974,008 bytes, SHA-256
    `2B7D7B95746E906D4E32D598C3CF673B85BE208F12EE55F5FDFB67F180488950`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작 3초 뒤 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing`, USB 화면 유지
    설정 0 유지; 설치된 base APK와 보관본 해시 일치, Matholic
    AndroidRuntime 일치 항목 0
  - A에는 고의 카메라 오류를 주입하지 않았으며 실제 카메라 전환·QR→Web→QR
    왕복과 관리자 PIN·PDF·인쇄는 사용자 복귀 뒤 수행
- QR 거부 감사기록 실패의 실패폐쇄 커밋 `5026daf`를 추가하고 Kiosk RC25로
  A에 설치했다.
  - 잘못된 QR 또는 복수 QR을 인식하면 감사기록을 백그라운드 DB에 저장했는데,
    이 작업만 예외 경계가 없어 저장소 오류가 앱 프로세스를 종료했고 스캐너
    cooldown은 이미 예약됐음
  - 첫 계측 fixture는 에뮬레이터 카메라 분석기 활성화를 기다리다 목표 분기
    전에 timeout되어, 합성 활성 분석기와 scanner UI 상태로 카메라 의존성을
    제거
  - 저장소를 고의로 사용할 수 없게 한 뒤 잘못된 QR 결정을 전달해 수정 전
    `UninitializedPropertyAccessException`과 프로세스 종료를 정확히 재현
  - 감사기록 성공 뒤에만 cooldown 재개를 예약하고, 저장 실패나 executor
    예약 실패에는 현재 화면 세션 참조를 지우고 스캐너를 닫은 관리자 PIN
    복구 화면을 표시
  - 수정 뒤 대상 시험에서 추가 2초 뒤에도 `LOCKED`, 관리자 PIN 화면,
    스캐너 숨김을 확인
  - Kiosk 전체 계측 30개, 네 모듈 JVM 84개, clean debug 204 tasks,
    release JVM 75개와 release 158 tasks·APK 이중 검증 통과
  - Kiosk RC25 준비 커밋 `abbd256`
  - Kiosk RC25 보관본 크기 34,974,012 bytes, SHA-256
    `6DEC70E58BE3B61500E92671301670296CBCCF412C439C720F568C2D900DF4B9`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작 3초 뒤 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing`, USB 화면 유지
    설정 0 유지; 설치된 base APK와 보관본 해시 일치, Matholic
    AndroidRuntime 일치 항목 0
  - A에는 고의 감사기록 실패를 주입하지 않았으며 실제 잘못된·복수 QR 거부,
    QR→Web→QR 왕복과 관리자 PIN·카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- QR 분석기 프레임 오류 복구 커밋 `4609e2f`를 추가하고 Kiosk RC26으로 A에
  설치했다.
  - QR 분석기의 Android 이미지 조회, InputImage 생성이나 ML Kit 처리 시작이
    동기 예외를 내면 기존 코드는 processing 플래그를 해제하지 않고
    `ImageProxy`도 닫지 않아 분석이 멈추며 예외가 분석 executor로 전파됐음
  - Android 13 일회용 에뮬레이터에서 이미지 조회가 예외를 내고 닫기 횟수를
    세는 합성 `ImageProxy`로 수정 전 예외 전파 실패를 재현
  - 프레임별 일회성 완료 경계를 두고 동기 준비·비동기 ML 결과 처리·프레임
    닫기 예외에서 processing 상태와 프레임 소유권을 정확히 한 번 해제
  - 수정 뒤 연속 실패 프레임 2개가 모두 닫히고 두 번째 프레임도 처리됨을 확인
  - Kiosk 전체 계측 31개, 네 모듈 JVM 84개, clean debug 204 tasks,
    release JVM 75개와 release 158 tasks·APK 이중 검증 통과
  - Kiosk RC26 준비 커밋 `60b8304`
  - Kiosk RC26 보관본 크기 34,974,012 bytes, SHA-256
    `B8F69578B94F733BAA91788702D9F71B329BBB2A35493CCC016C96DA1B3112C4`
  - A 설치 직후 HOME 종료로 Lock Task가 `NONE`이었으나 화면을 깨우지 않는
    명시적 HOME 시작 3초 뒤 `LOCKED` 복구
  - A 설치 전후 Kiosk UID `10288`, firstInstallTime, dataDir, Web RC15,
    release signer, Device Owner, 전용 HOME, 화면 `Dozing`, USB 화면 유지
    설정 0 유지; 설치된 base APK와 보관본 해시 일치, Matholic
    AndroidRuntime 일치 항목 0
  - A에는 고의 카메라 프레임 오류를 주입하지 않았으며 실제 전면·후면 QR
    인식, QR→Web→QR 왕복과 관리자 PIN·PDF·인쇄는 사용자 복귀 뒤 수행
- Web 자바스크립트 실행 시작 오류의 실패폐쇄 커밋 `e793539`를 추가하고
  Web POC RC16으로 A에 설치했다.
  - WebView가 종료되거나 renderer 경계에서 `evaluateJavascript`가 동기
    예외를 내면 기존 코드는 예외를 main thread로 전파해 Web 프로세스를
    종료할 수 있었음
  - Android 13 일회용 에뮬레이터에서 자바스크립트 평가 시작 시 예외를 내는
    합성 WebView로 수정 전 `IllegalStateException` 전파를 재현
  - 동기 평가 시작 실패를 `WEB_EVALUATION` 잠금 상태로 실패폐쇄하고
    프로세스 밖으로 예외를 내보내지 않음
  - 수정 뒤 대상 시험, Web POC 전체 계측 47개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC16 준비 커밋 `fe22659`
  - Web POC RC16 보관본 크기 3,085,908 bytes, SHA-256
    `0072A825EB1B3F488F6C5FA58DF35FCABBDAAFC95E8A48723638D71970347291`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 최근 Matholic AndroidRuntime 일치 항목 0
  - A에서 고의 WebView 평가 오류를 주입하지 않았으며 실제 QR→Web→QR 왕복,
    관리자 PIN·카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- Web 자바스크립트 평가 완료 콜백 오류의 실패폐쇄 커밋 `d0497be`를
  추가하고 Web POC RC17로 A에 설치했다.
  - `evaluateJavascript` 호출이 성공한 뒤 WebView가 main thread에서
    비동기로 전달하는 결과 콜백의 후속 DOM 처리 예외는 평가 시작의 바깥
    예외 경계로 잡을 수 없어 프로세스 종료로 전파될 수 있었음
  - Android 13 일회용 에뮬레이터에서 평가 결과 콜백을 저장했다가 호출
    완료 뒤 별도로 전달하는 합성 WebView로 수정 전 콜백 예외 전파를 재현
  - 결과 파싱·후속 콜백을 별도 예외 경계로 감싸고
    `WEB_CALLBACK` 잠금 상태로 실패폐쇄
  - 수정 뒤 대상 시험, Web POC 전체 계측 48개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC17 준비 커밋 `27c5fb1`
  - Web POC RC17 보관본 크기 3,085,980 bytes, SHA-256
    `1B3B53CA99270BAACD7B47F3D676CDF7C5EBE99CED334EC107F6085CD93B6BF4`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 최근 Matholic AndroidRuntime 일치 항목 0
  - A에서 고의 Web 콜백 오류를 주입하지 않았으며 실제 QR→Web→QR 왕복,
    관리자 PIN·카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- Web renderer 정리 오류 격리 커밋 `868705f`를 추가하고 Web POC RC18로
  A에 설치했다.
  - renderer 종료 시 죽은 WebView의 뷰 제거 또는 `destroy()`가 예외를
    내면 기존 코드는 `WEB_PROCESS_GONE` 잠금 전환 전에 종료될 수 있었음
  - Android 13 일회용 에뮬레이터에서 `destroy()`가 예외를 내는 합성
    WebView로 수정 전 정리 예외 전파와 잠금 미전환을 재현
  - Activity 참조 해제, 뷰 제거, WebView 파기를 독립 정리 단계로 분리해
    제거·파기 오류가 renderer 실패폐쇄를 막지 않도록 변경
  - 수정 뒤 대상 시험, Web POC 전체 계측 49개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC18 준비 커밋 `43dd12d`
  - Web POC RC18 보관본 크기 3,086,020 bytes, SHA-256
    `5A05BB0AD7ADFEBB2F203E9AE109C4AD2B0B496B2AC3FA756D7259D4596EC433`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 최근 Matholic AndroidRuntime 일치 항목 0
  - A에서 고의 renderer 정리 오류를 주입하지 않았으며 실제 renderer 종료
    복구·QR→Web→QR 왕복, 관리자 PIN·카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- Activity 종료 WebView 정리 오류 격리 커밋 `e9a017e`를 추가하고 Web POC
  RC19로 A에 설치했다.
  - 기존 `onDestroy`는 `stopLoading`, 빈 문서 전환, 기록·캐시·SSL 정리와
    `destroy()`를 연속 호출해 앞 단계 하나가 예외를 내면 나머지 정리와
    상위 Activity 생명주기를 건너뛰고 Web 프로세스를 종료할 수 있었음
  - Android 13 일회용 에뮬레이터에서 `stopLoading()`이 예외를 내는 합성
    WebView로 수정 전 Activity 종료 시 프로세스 crash를 재현
  - 각 WebView 정리 단계를 독립 예외 경계로 나누고 `super.onDestroy()`는
    `finally`에서 항상 호출하도록 변경
  - 수정 뒤 대상 시험, Web POC 전체 계측 50개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC19 준비 커밋 `d93a4dd`
  - Web POC RC19 보관본 크기 3,088,588 bytes, SHA-256
    `77176D0778A71E98DEABF453F2615781C5D7CC30DEA1FA31206E8D66EA1B7531`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 설치 시각 이후 Matholic AndroidRuntime 일치 항목 0
  - A에서 고의 Activity 종료 정리 오류를 주입하지 않았으며 실제
    QR→Web→QR 왕복, 관리자 PIN·카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- Web 세션 정리 오류 실패폐쇄 커밋 `d9601b7`을 추가하고 Web POC RC20으로
  A에 설치했다.
  - 로그아웃 확인 뒤 Web 기록·form·cache·SSL·WebStorage와 cookie를
    연속 정리하는 중 한 단계가 예외를 내면 뒤 정리와 오류 잠금을 건너뛰고
    main thread로 예외가 전파될 수 있었음
  - Android 13 일회용 에뮬레이터에서 `clearHistory()`가 예외를 내는 합성
    WebView로 수정 전 예외 전파와 후속 `clearCache()` 미실행을 재현
  - 동기 정리 단계와 cookie 삭제 요청을 독립 경계에서 가능한 만큼 모두
    시도하고 어느 단계든 실패하면 `SESSION_CLEAR`로 실패폐쇄
  - cookie flush·로그인 재로딩의 비동기 오류와 Handler 예약 거부도 현재
    로그아웃 세대에서만 `SESSION_CLEAR`로 잠그도록 보강
  - 수정 뒤 대상 시험, Web POC 전체 계측 51개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC20 준비 커밋 `e08a32e`
  - Web POC RC20 보관본 크기 3,091,724 bytes, SHA-256
    `862FD9F6173531A14D49FD2F9F35727C40DE5C65BD9CBE4B06302322F5C518AF`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 설치 시각 이후 Matholic AndroidRuntime 일치 항목 0
  - A에서 실제 세션 정리 실패를 주입하지 않았으며 실제 QR→Web→QR 왕복,
    관리자 PIN·카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- 허용되지 않은 Web 이동 중지 오류 실패폐쇄 커밋 `ce95b42`를 추가하고
  Web POC RC21로 A에 설치했다.
  - 허용되지 않은 최상위 문서가 시작될 때 죽어가는 WebView의
    `stopLoading()`이 동기 예외를 내면 기존 코드는 `NAVIGATION_BLOCKED`
    잠금 전환 전에 예외를 main thread로 전파할 수 있었음
  - Android 13 일회용 에뮬레이터에서 `stopLoading()`이 예외를 내는 합성
    WebView로 수정 전 콜백 예외 전파와 잠금 미전환을 재현
  - 중지 요청 오류를 격리하고 성공 여부와 무관하게
    `NAVIGATION_BLOCKED`로 실패폐쇄
  - 수정 뒤 대상 시험, Web POC 전체 계측 52개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC21 준비 커밋 `f4469bf`
  - Web POC RC21 보관본 크기 3,091,972 bytes, SHA-256
    `182743349523E8A7353BE12193DC5B9F45BA5376B245A4E639FBF4E2CDD2AF7A`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 설치 시각 이후 Matholic AndroidRuntime 일치 항목 0
  - A에서 고의 WebView 중지 오류를 주입하지 않았으며 실제 허용 외 이동,
    QR→Web→QR 왕복과 관리자 PIN·카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- 학생 학습지 복귀 제어 오류 실패폐쇄 커밋 `084d6b9`를 추가하고 Web POC
  RC22로 A에 설치했다.
  - 학생 `ACTIVE` 상태에서 포털 등 학생용이 아닌 허용 문서가 시작되면
    마지막 학습지로 복귀시키는데, 기존 `stopLoading()` 또는 `loadUrl()`이
    동기 예외를 내면 잠금 없이 main thread로 전파될 수 있었음
  - Android 13 일회용 에뮬레이터에서 첫 `loadUrl()`이 예외를 내는 합성
    WebView로 수정 전 콜백 예외 전파와 잠금 미전환을 재현
  - 학생 페이지 복귀의 로딩 중지·재로딩을 하나의 오류 경계로 묶고 어느
    단계든 실패하면 `NAVIGATION_BLOCKED`로 실패폐쇄
  - 수정 뒤 대상 시험, Web POC 전체 계측 53개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC22 준비 커밋 `4548069`
  - Web POC RC22 보관본 크기 3,092,092 bytes, SHA-256
    `3EAA30E6BC793A0FB1896F09B043387ABFD1082FA65E08E931BF7C80A5297E43`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 설치 시각 이후 Matholic AndroidRuntime 일치 항목 0
  - A에서 고의 학생 페이지 복귀 오류를 주입하지 않았으며 실제 포털
    리디렉션 복귀, QR→Web→QR 왕복과 관리자 PIN·카메라·PDF·인쇄는 사용자
    복귀 뒤 수행
- Web 탐색 시작 오류 실패폐쇄 커밋 `a569244`를 추가하고 Web POC RC23으로
  A에 설치했다.
  - 복구·로그인·학생 탭·Gate 3·로그아웃 흐름의 일반 `loadUrl()` 호출이
    동기 예외를 내면 일부 경로에서 잠금 전환 없이 main thread로 전파될 수
    있었음
  - Android 13 일회용 에뮬레이터에서 첫 `loadUrl()`이 예외를 내는 합성
    WebView로 수정 전 복구 탐색 예외 전파와 잠금 미전환을 재현
  - 일반 Web 탐색 시작을 공통 오류 경계로 묶어 실패 시
    `WEB_NAVIGATION`으로 실패폐쇄
  - 수정 뒤 대상 시험, Web POC 전체 계측 54개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC23 준비 커밋 `f57e873`
  - Web POC RC23 보관본 크기 3,092,240 bytes, SHA-256
    `22015D506B2FFE4D47313051C1CE634589B6F05948ADF4B8B4259B61E3196C2E`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 설치 시각 이후 AndroidRuntime 오류 일치 항목 0
  - A에서 고의 Web 탐색 시작 오류를 주입하지 않았으며 실제 QR→Web→QR
    왕복과 관리자 PIN·사이트·카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- 로그인 사전점검 DNS 재시도 준비 오류 실패폐쇄 커밋 `02e8be4`를 추가하고
  Web POC RC24로 A에 설치했다.
  - 로그인 호스트의 첫 DNS 오류만 3.5초 뒤 한 번 재시도하는 경로에서
    기존 `stopLoading()`과 Handler 예약이 예외 경계 밖에 있어, 죽어가는
    WebView가 예외를 내면 잠금 없이 main thread로 전파될 수 있었음
  - Android 13 일회용 에뮬레이터에서 `stopLoading()`이 예외를 내는 합성
    WebView로 안전한 재시도 준비 경계 부재를 수정 전 대상시험 실패로 확인
  - 로딩 중지와 지연 예약을 단일 오류 경계로 묶고 예외·예약 거부 시
    `WEB_NAVIGATION`으로 실패폐쇄
  - 수정 뒤 대상 시험, Web POC 전체 계측 55개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC24 준비 커밋 `188ab55`
  - Web POC RC24 보관본 크기 3,092,068 bytes, SHA-256
    `5FC92F7DC0781A0614705B0C715D01FEBD7A1396C2318B29574F5DE5B03E16D1`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 설치 시각 이후 AndroidRuntime 오류 일치 항목 0
  - A에서 DNS 재시도 준비 오류를 고의 주입하지 않았으며 실제 QR→Web→QR
    왕복과 관리자 PIN·사이트·카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- Web 보안 거부 콜백 오류 실패폐쇄 커밋 `88995b9`를 추가하고 Web POC
  RC25로 A에 설치했다.
  - TLS 오류의 `SslErrorHandler.cancel()`과 Safe Browsing의
    `backToSafety()`가 예외를 내면 기존 코드는 잠금 전환 전에 main thread로
    전파될 수 있었음
  - Android 13 일회용 에뮬레이터에서 거부 콜백이 예외를 내는 합성 함수로
    공통 실패폐쇄 경계 부재를 수정 전 대상시험 실패로 확인
  - 플랫폼 거부 호출을 공통 오류 경계에서 시도하고 예외와 무관하게
    `TLS_ERROR` 또는 `SAFE_BROWSING`으로 실패폐쇄
  - 수정 뒤 대상 시험, Web POC 전체 계측 56개, 네 모듈 JVM 84개,
    clean debug 204 tasks, release JVM 75개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC25 준비 커밋 `f5acace`
  - Web POC RC25 보관본 크기 3,093,100 bytes, SHA-256
    `5C87CAB5A9A7F0D26E3F133AFA63AF8834E61E138CF81ADE2B8EFE4A8266F7CD`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 설치 시각 이후 AndroidRuntime 오류 일치 항목 0
  - A에서 TLS·Safe Browsing 콜백 오류를 고의 주입하지 않았으며 실제
    QR→Web→QR 왕복과 관리자 PIN·사이트·카메라·PDF·인쇄는 사용자 복귀 뒤
    수행
- Web 프록시 초기화 오류 실패폐쇄 커밋 `055d0f8`을 추가하고 Web POC
  RC26으로 A에 설치했다.
  - 기존 프록시 초기화는 WebView 기능 지원 확인 예외가 빠져나올 수 있었고,
    loopback 프록시 시작 뒤 설정 생성·override 적용이 실패하면 부분
    프록시나 대기 콜백을 남길 수 있었음
  - Android 의존성을 분리한 직렬 coordinator와 합성 platform을 추가해 지원
    확인 예외, override 예외, 미지원, 비동기 성공·중복 요청, 정리 예외를
    JVM 시험 5개로 검증
  - 초기화 런타임 예외를 `FAILED`로 종결하고 부분 프록시를 정리하며, 정리
    예외와 늦은 준비 콜백이 종결 결과를 바꾸지 못하게 함
  - Web POC 전체 계측 56개, 네 모듈 JVM 89개, clean debug 204 tasks,
    release JVM 80개와 release 158 tasks·APK 이중 검증 통과
  - Web POC RC26 준비 커밋 `bf84e89`
  - Web POC RC26 보관본 크기 3,095,616 bytes, SHA-256
    `32CFD09F788385A3EF0E78AEB15C44DBC5168CD1A3E1AC2AE277F33E97AFF2FC`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 설치 시각 이후 AndroidRuntime 오류 일치 항목 0
  - A에서 프록시 초기화 오류를 고의 주입하지 않았으며 실제 QR→Web→QR
    왕복과 관리자 PIN·사이트·카메라·PDF·인쇄는 사용자 복귀 뒤 수행
- Web 프록시 초기화 완료 timeout 커밋 `32271c1`을 추가하고 Web POC
  RC27로 A에 설치했다.
  - RC26은 proxy override 요청이 예외 없이 접수된 뒤 완료 콜백이 오지
    않으면 UI 초기화 전 `CONFIGURING` 상태와 부분 프록시를 무기한 유지했음
  - 프록시 시작 뒤 10초 watchdog을 예약해 성공 때 취소하고, 예약 거부·
    시간 초과 시 `FAILED`로 종결해 부분 프록시를 닫음
  - 합성 platform으로 시간 초과 정리, 늦은 ready 무시와 watchdog 예약
    실패 정리를 추가해 coordinator JVM 시험 7개 통과
  - Web POC 전체 계측 56개, 네 모듈 JVM 91개, clean debug 204 tasks,
    release JVM 82개와 release 158 tasks·APK 이중 검증 통과
  - Web POC RC27 준비 커밋 `3ae6697`
  - Web POC RC27 보관본 크기 3,097,596 bytes, SHA-256
    `58958895AB1DDCEF548252B9F0F4F1E5E4ACBA357D56B1F61CB6CF98234F7D4B`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 설치 시각 이후 AndroidRuntime 오류 일치 항목 0
  - A에서 프록시 완료 콜백 유실을 고의 주입하지 않았으며 실제
    QR→Web→QR 왕복과 관리자 PIN·사이트·카메라·PDF·인쇄는 사용자 복귀 뒤
    수행
- A 인쇄 읽기 전용 진단:
  - Android 내장 IPP 서비스는 설치·활성·바인딩 상태
  - 이전 QR 인쇄 작업 하나가 `STATE_STARTED`에서 취소 요청 중인 채 장시간
    정체됐고 스풀 PDF가 남아 있음
  - A와 PC 모두 프린터 IPP TCP 포트 연결 성공
  - 서비스 로그에는 원인을 확정할 오류가 남아 있지 않음
  - 사용자 복귀 전에는 대기열 취소·스풀러 초기화·새 출력 작업을 수행하지 않음
- Web 프록시 종료 경합 소켓 정리 커밋 `a212198`을 추가하고 Web POC
  RC28로 A에 설치했다.
  - 종료 snapshot과 새 client 등록이 겹치거나 종료된 executor가 client·
    역방향 tunnel 작업을 거부하면 소켓이 남을 수 있었음
  - 등록·종료를 직렬화하고 종료 뒤 늦게 온 소켓과 거부된 작업의 client·
    upstream 소켓을 즉시 닫도록 변경
  - 대상 JVM 3개, Web POC 전체 계측 56개, 네 모듈 JVM 94개,
    clean debug 204 tasks, release JVM 85개와 release 158 tasks·APK 이중
    검증 통과
  - Web POC RC28 준비 커밋 `5bc8f98`
  - Web POC RC28 보관본 크기 3,101,640 bytes, SHA-256
    `19A6D31C92483E890FE9A2CA91909AFDFA17C58DA2FFC48A383ECF52917B7520`
  - A 설치 전후 Web POC UID `10287`, firstInstallTime, dataDir,
    Kiosk RC26, release signer, Device Owner, 전용 HOME, `LOCKED`, 화면
    `Dozing`, USB 화면 유지 설정 0 유지; 설치된 base APK와 보관본 해시
    일치, 설치 시각 이후 AndroidRuntime 오류 일치 항목 0
  - A에서 고의 종료 경합을 주입하지 않았으며 실제 QR→Web→QR 왕복,
    관리자 PIN·사이트·카메라·PDF·인쇄는 사용자 복귀 뒤 수행

## 연속 개발 사이클

각 사이클은 가능한 범위에서 다음 순서를 따른다.

1. 이 파일, 적용 지침, `git status`, 최근 커밋과 현재 A 상태를 읽는다.
2. 직전 사이클의 미검증 항목과 최우선 요구사항을 먼저 선택한다.
3. 기존 동작과 데이터를 읽기 전용으로 확인해 재현 가능한 기준을 남긴다.
4. 가장 가까운 자동시험이나 합성 fixture를 먼저 추가하거나 보강한다.
5. 한 가지 목적에 집중한 최소 변경을 구현한다.
6. 관련 컴파일·단위시험·lint·APK 빌드와 가능한 A 기기시험을 수행한다.
7. 실패하면 원인을 수정하고 같은 검증을 다시 수행한다.
8. 검증된 작은 단위를 설명적인 커밋으로 남기고 작업 브랜치에 푸시한다.
9. 검증 결과·미검증·새 제한을 관련 문서에 사실대로 갱신한다.
10. 다음으로 위험도와 운영 효과가 큰 항목을 선택해 반복한다.

서로 독립적인 변경은 가능한 한 별도 커밋으로 나눈다. 한 커밋을 되돌리면
무관한 개선까지 함께 사라지는 구조를 피한다. 공유 이력을 rewrite하거나
force-push하지 않는다.

## 우선 검증 순서

1. A의 RC02 기준정보와 Device Owner를 다시 기록한다.
2. Web POC RC03, Kiosk RC03 순으로 `adb install -r`하고 버전·signer·
   Device Owner·기존 데이터 보존을 확인한다.
3. 신규 계측시험을 가능한 안전한 상태에서 실행한다.
4. 학생 마스터, 다중 반 소속, 반 삭제 후 학생·QR 보존을 시험계정으로 확인한다.
5. 미리보기 없는 전면·후면 QR 인식과 정상 왕복을 확인한다.
6. 자동 학습지 진입, 두 탭 전환, 허용 외 이동·뒤로가기 차단을 확인한다.
7. 문제 입력 UI, 두 제출 버튼과 오답 번호 전용 결과를 실제 시험문제로
   확인한다.
8. PDF Quick Share와 실제 인쇄 크기를 확인한다.
9. Android 인쇄 서비스 실패를 재현할 수 있으면 print service 상태와
   비민감 logcat을 수집해 원인을 좁힌다.
10. 재부팅·Web 강제종료·네트워크 단절·중복 QR·폐기 QR 등 안전한 실패주입과
    정상 복구를 확인한다.

## 테스트·보안 기준

- 실제 학생 대신 사용자가 허용한 시험계정과 합성 데이터를 우선 사용한다.
- 자격정보, 관리자 PIN, QR 원문, 답안과 학생 실명을 파일·Git·로그에 남기지
  않는다.
- 원문 UI dump, 트래픽 복호화, 루팅, 인증 우회와 좌표 기반 운영 자동화를
  사용하지 않는다.
- DOM 보조는 공식 공개 Web UI의 의미 구조를 사용하고 계약 불일치 시
  실패폐쇄한다.
- 데이터 마이그레이션과 업데이트는 기존 학생·반·Device Owner를 보존해야
  한다.
- destructive 시험은 재생성 가능한 시험 데이터만 대상으로 한다.
- 프린터·공유 테스트의 QR은 로그인 가능한 비밀이므로 통제된 대상만 사용하고
  임시 파일·대기열·불필요한 출력물을 정리한다.
- A 기기시험에서 사용자 손이 필요한 물리 조작이 나오면 그 시점까지 가능한
  비대면 작업을 모두 수행하고 정확히 한 번에 할 일을 보고한다.

## 사용자 부재 중 안전 경계

현재 사용자는 당분간 A 기기와 PC를 직접 조작할 수 없다. 사용자가 다시
기기를 만질 수 있다고 명시하기 전에는 다음 작업을 실행하지 않는다.

- A 기기 공장초기화, Device Owner 제거·교체, 사용자·계정 초기화
- Kiosk/Web POC 삭제, 앱 데이터 삭제, Keystore·DB·관리자 PIN 초기화
- A 기기 재부팅·종료, PC 재부팅·종료
- USB 디버깅·개발자 옵션 변경 또는 기존 ADB 승인 취소
- 실제 프린터 작업 전송, QR 재발급, 기존 실물·사진 QR을 무효화하는 작업
- 관리자 PIN이나 물리 조작 없이는 복구할 수 없는 고의 잠금·실패주입
- Wi-Fi·잠금화면·접근통제 등 현재 원격 연결을 잃을 수 있는 설정 변경

사용자 부재 중 허용되는 기기 변경은 다음 조건을 모두 충족해야 한다.

1. 같은 release signer와 더 높은 versionCode의 검증된 APK를 `adb install -r`
   방식으로 덮어써 앱 데이터와 Device Owner를 보존한다.
2. 실행 전에 ADB `device`, 정확한 serial·모델, 배터리·전원, 설치 버전,
   signer와 Device Owner를 읽기 전용으로 확인한다.
3. 실행 뒤 같은 정보를 다시 확인하고 앱이 전용 HOME으로 복귀했는지 검사한다.
4. 실패해도 공장초기화나 사용자 PIN 입력 없이 기존 설치본 또는 검증된
   동일 signer APK로 복구할 수 있어야 한다.

물리 조작이 필요한 검증은 미검증으로 분리하고, 그 때문에 다른 코드 검토,
합성 시험, 정적 분석, 문서 검증과 안전한 ADB 검사를 중단하지 않는다. 사용자
부재 자체를 Goal 완료나 차단 사유로 보지 않는다.

## 커밋·배포 원칙

- 작업은 `codex/rc03-usability` 또는 그 후속 `codex/` 브랜치에서 수행한다.
- 의미 있는 검증 통과 단위마다 커밋하고 원격 브랜치에 푸시한다.
- 커밋 전 현재 작업에서 만든 파일만 포함됐는지 확인한다.
- release APK는 로컬 `artifacts/`에 내부 보관하고 Git에 포함하지 않는다.
- 사용자의 명시적 출시 지시 전에는 GitHub Release, master 병합 또는 공개
  배포를 하지 않는다.
- A 설치는 같은 release signer의 검증된 APK만 사용하며 설치 전후
  versionName, versionCode, signer, Device Owner와 핵심 상태를 확인한다.

## 멈춤 조건

다음 중 하나일 때만 연속 진행을 멈춘다.

1. 사용자가 중단 또는 방향 변경을 지시한다.
2. 사용자 물리 조작이나 새로운 고위험 승인이 없이는 의미 있는 진행이
   불가능하다.
3. 같은 외부 차단 조건이 반복되고 안전한 대안·추가 검증도 모두 소진됐다.

어려움, 긴 실행시간, 한 번의 시험 실패, 개선 아이디어 부족만으로 완료 또는
차단 처리하지 않는다. 실패는 재현·원인분석·수정·회귀시험의 다음 입력이다.
사용자 부재 중 금지된 작업이 필요해져도 다른 안전한 작업이 남아 있으면
차단하지 않고 다음 우선순위로 진행한다.

## 보고 기준

중간 보고와 최종 보고에는 사실을 다음처럼 구분한다.

- 완료: 실제로 구현하고 검증한 것
- 자동검증만 완료: 기기 실기는 아직인 것
- 미검증: 실행하지 않은 것
- 실패: 재현된 증상과 증거
- 추론: 증거에서 합리적으로 예상한 원인

각 주요 체크포인트에는 커밋 해시, 실행한 시험, A 설치 버전, 남은 제한과
다음 우선순위를 기록한다.

## 종료 기록 — 2026-07-26

사용자가 PC 재부팅 뒤 진행 중이던 프록시 종료 경합 한 건까지만 마무리하고
연속 개발 Goal을 종료하라고 명시했다. 위 RC28 구현·자동검증·A 보존형 설치와
문서화를 마지막 사이클로 삼으며 새로운 개선 사이클은 시작하지 않는다.

- 종료 시 A: Kiosk `0.6.0-rc26`/code 31,
  Web POC `0.4.0-rc28`/code 45
- 종료 시 작업 브랜치: `codex/rc03-usability`
- 최신 실물 관리자 화면·사이트·카메라·PDF·인쇄 회귀와 고의 프록시 종료
  경합 시험은 미검증으로 남긴다.
- 다시 작업할 때는 이 기록을 기준으로 새 사용자 지시와 안전 경계를 먼저
  확인한다.

## Goal 종료 후 실물 회귀 후속 — 2026-07-27

Goal 종료 뒤 사용자가 직접 RC26/RC28 실물 회귀를 시작했다.

- 기존 학생·반 목록 보존: 통과
- 반에 소속되지 않은 학생 QR 로그인 차단: 통과
- 관리자 화면의 시스템 뒤로가기가 PIN 화면으로 전환되는 문제 재현
- 기존 학생 로그인 뒤 자동 학습지 진입 전에 `PORTAL_ROUTE` 잠금 재현
- 관리자 뒤로가기를 소비하고, 고정 `/course`보다 정확한 Matholic 출처·
  계정 메뉴 구조·학생 이름의 의미 계약으로 포털을 검증하도록 `55cc1ac`에서
  수정
- Kiosk RC27/Web POC RC29 릴리스 준비 커밋 `a552aa6`
- Kiosk 31개·Web 56개 전체 계측, 네 모듈 JVM 94개, clean debug 204 tasks,
  release JVM 85개와 release 158 tasks·APK 이중 검증 통과
- A에 두 앱을 같은 signer의 상위 versionCode로 보존형 설치
- UID·firstInstallTime·dataDir, Device Owner·전용 HOME·`LOCKED` 유지,
  설치본과 보관본 해시 일치, 설치 시각 이후 AndroidRuntime 오류 0
- 관리자 뒤로가기와 자동 학습지 진입 수정 결과는 사용자 재검증 대기

## Goal 종료 후 학생 사용성·결과·QR 후속 — 2026-07-27

- RC27/RC29 사용자 실물 확인:
  - 관리자 뒤로가기 뒤 현재 화면·입력 유지: 통과
  - 기존 학생 QR 로그인 뒤 `PORTAL_ROUTE` 없이 학습지 자동 진입: 통과
- 학생이 확인한 상단 Matholic 메뉴 노출, 문제 화면의 불필요한 기능,
  수식 입력기·제출 화면 불편, 확인창 주소 머리말과 오답 목록 오류를
  `40a5667`에서 보강
- 공개 Matholic JavaScript를 자격정보 없이 읽기 전용으로 확인해 결과
  5번째 이후가 스크롤할 때 지연 생성되는 구조를 확인
- 풀이·정답 차폐 뒤 결과 페이지를 순차 이동해 모든 번호의 완전성을 증명한
  경우만 오답 목록을 표시하고 불완전하면 `RESULT_INCOMPLETE`
- 전면·후면 카메라 영상은 표시·저장하지 않은 채 매쓰홀릭 QR 경계 좌표로
  중앙·거리·상하좌우를 안내하고 중앙 판독 범위만 인증하는 변경을
  `88c2536`에 추가
- Kiosk RC28/Web POC RC30 준비 커밋 `9eb83f6`
- Web 전체 계측 64개, Kiosk 전체 계측 31개, 네 모듈 JVM 101개,
  clean debug 204 tasks, release 158 tasks와 APK 이중 검증 통과
- A에 두 앱을 같은 release signer의 상위 versionCode로 `adb install -r`
  보존형 설치
- UID·firstInstallTime·dataDir·카메라 권한, Device Owner·전용 HOME·
  `LOCKED` 유지, 설치된 base APK와 artifact SHA-256 일치, 설치 뒤
  Matholic crash buffer 일치 항목 0
- 최신 학생 Web 사용성·전체 결과·QR 위치 안내 실물 재검증은 사용자 수행
  대기

## Goal 종료 후 역방향 가로 회전·USB 키 백업 후속 — 2026-07-27

- Kiosk와 Web POC의 고정 가로 선언을 `sensorLandscape`로 변경해 세로는
  계속 금지하고 0°↔180° 두 가로 방향만 센서 회전하도록 함
- Kiosk·Web manifest 계측시험을 각각 추가하고 Android 13 에뮬레이터에서
  Web 65개, Kiosk 32개 전체 계측시험 통과
- release 검증에서 두 APK의 `sensorLandscape` 선언도 필수 확인
- 회전 구현 커밋 `12508a7`, Kiosk RC29/Web POC RC31 준비 커밋
  `d2cfb26`
- release 단위시험·lint·두 APK assemble 158 tasks, APK build/stored
  이중 검증과 체크섬 대조 통과
- A에 Kiosk `0.6.0-rc29`/code 34, Web POC `0.4.0-rc31`/code 48을 같은
  release signer로 `adb install -r` 보존형 설치
- UID `10288`/`10287`, firstInstallTime·dataDir·카메라 권한,
  Device Owner·전용 HOME·`LOCKED` 유지, 설치 뒤 AndroidRuntime 오류 0
- 앱 업데이트에 따른 실패폐쇄로 A는 `RECOVERY_REQUIRED` 관리자 PIN 화면에
  남김. 사용자가 기존 수업 안전 종료·세션 정리 후 QR 대기로 복구해야 함
- 별도 SanDisk USB
  `MatholicKioskSigningBackup/matholic-kiosk-release.p12`에 release 키
  두 번째 오프라인 복구본을 추가하고 원본 SHA-256 일치 확인
- USB에는 암호화된 PKCS12 키만 복사. 복구 비밀번호와 Windows DPAPI
  자격정보는 복사하지 않았고 기존 Android 폰 marker도 보존
- A의 실제 180° 물리 회전과 QR→Web 전환 중 방향 유지 실기는 사용자 확인
  대기

## Goal 종료 후 RC30/RC32 실물 결함 보강 — 2026-07-27

- 사용자 실물 확인에서 QR·Web 180° 회전, 최상단·계정 메뉴 숨김,
  오류신고·해설 문구 숨김, 제출 간격, 수식 자동 전환과 확인창 주소 머리말
  제거는 통과
- 학습지→진단평가 지연, 동영상·필기·풀이 업로드 잔류, 전체답안 자동 이동
  실패, 분수 오삭제, QR 목표 안내 불명확과 25문항 `RESULT_INCOMPLETE` 재현
- 분수 오삭제는 사용자 요구가 아니었으며 기존 구현 판단 오류로 확인하고
  RC32에서 복원
- 공개 Matholic 자산의 실제 구조를 읽기 전용으로 확인해 동일 SPA 링크,
  이미지형 동영상 패널, input suffix 필기 제어, Ant 전체답안 modal과 내부
  결과 스크롤에 맞춰 수정
- 25문항 결과는 표의 `문항수`와 실제 1..N 분류가 모두 일치하고 내부
  스크롤·카드 수가 안정된 뒤에만 확정
- 구현 커밋 `1679a5d`, QR 안내 커밋 `c74b993`, RC30/RC32 준비 커밋
  `68fed79`를 원격 `codex/rc03-usability`에 푸시
- 관련 Web DOM 계측 36개와 Android 13 에뮬레이터 전체 Web 67개·Kiosk
  32개, Web JVM 45개, Kiosk JVM 48개, debug assemble 84 tasks,
  release 158 tasks와 APK 이중 검증 통과
- A에 Kiosk `0.6.0-rc30`/code 35와 Web POC `0.4.0-rc32`/code 49를 같은
  release signer로 보존형 설치
- UID·firstInstallTime·dataDir, Device Owner·전용 HOME·두 앱 allowlist·
  `LOCKED` 유지, 설치된 base APK와 artifact 해시 일치, 설치 뒤 두 앱의
  `FATAL EXCEPTION`/process crash 일치 항목 0
- A는 업데이트 실패폐쇄의 `RECOVERY_REQUIRED` 상태이며 관리자 PIN 복구와
  최신 Web·QR 수정 실물 재검증 대기
- 후속 실물 확인에서 관리자 복구·Web 로그인과 현재 수업 안전 종료,
  기존 `토요일2` 반·학생 3명 보존, 같은 반 재시작·`QR_READY`와 실제
  25문항 오답 목록 완전성 통과
- 화면 중앙 목표 영역은 물리 렌즈 위치와 무관한 잘못된 표현임을 확인하고
  `fed3fe5`에서 제거. 디버그 단위시험·APK·계측시험 소스 컴파일은 통과했고
  A 설치는 다음 수정 묶음까지 보류

## Goal 종료 후 RC31/RC33 초기 노출·직접 입력 후속 — 2026-07-27

- 학생 Web 화면에서 숨길 메뉴와 제어가 먼저 보인 뒤 사라지고, 파란색
  직접 필기 입력 버튼은 눌러야 사라지는 실물 결함을 사용자에게서 확인
- 학생 페이지와 학습지↔진단평가 전환 때 WebView를 먼저 가리고 DOM 계약이
  목표 경로에서 두 번 연속 적용된 뒤에만 공개하도록 변경
- 공개 Matholic 자산의 실제 수식 입력 DOM을 읽기 전용으로 확인해,
  우측 상단 8px 위치에 지연 생성되는 빈 아이콘 버튼을 구조적으로 숨김
- 늦게 생성되는 직접 입력 버튼도 `MutationObserver`가 즉시 숨기며, 루트·
  분수·파이 도구는 유지
- 구현 커밋 `de38b08`, RC31/RC33 준비 커밋 `606240c`, 중앙 QR 목표 제거
  커밋 `fed3fe5`를 원격 `codex/rc03-usability`에 푸시
- Android 13 에뮬레이터 Web 67개와 Kiosk 33개 전체 계측, release
  단위시험·lint·두 APK assemble 158 tasks, APK 이중 검증 통과
- A에 Kiosk `0.6.0-rc31`/code 36과 Web POC `0.4.0-rc33`/code 50을
  보존형 설치. UID·firstInstallTime·Device Owner·전용 HOME·`LOCKED`
  유지, 설치된 base APK와 artifact SHA-256 일치
- A는 업데이트 실패폐쇄의 `RECOVERY_REQUIRED` 관리자 PIN 화면이며,
  안전 복구와 최신 Web 화면 실물 확인 대기

## Goal 종료 후 Web RC34 로그인 실패 안전 종료 후속 — 2026-07-27

- 매쓰홀릭 미등록 아이디 QR 로그인 실패 뒤 안전 종료가
  `LOGIN_FINGERPRINT_U1_P1_C1_B1_F1_A0_E11_R0_V1`로 거부되는 실물
  결함을 확인
- 플래그상 로그인 구조·빈 입력·체크박스·계약 버전은 정상이었고, 공식
  로그인 실패 리디렉션이 폼 action에 안전한 `url=`을 붙여 기존 `A0`
  검사를 유발
- 전송 주소 허용 범위를 넓히지 않고, 로그아웃·복구 상태에서만 공식 기본
  로그인 URL을 다시 열어 오류·복귀 매개변수를 제거한 뒤 기존 strict
  fingerprint와 Web 데이터 정리를 수행하도록 수정
- 실제 공개 로그인 실패 리디렉션을 사용하는 신규 계측시험과 Web 전체
  계측 68개 통과
- 구현 커밋 `bde7842`, Web RC34 준비 커밋 `331ca9a`를 원격
  `codex/rc03-usability`에 푸시
- Web POC `0.4.0-rc34`/code 51만 A에 보존형 설치. UID
  `10287`, firstInstallTime, Device Owner와 전용 HOME 유지, 설치 APK
  SHA-256과 artifact 일치
- 실물 재검증:
  - 기존 실패 상태에서 안전 종료→수업 재시작→QR 화면: 통과
  - 같은 미등록 아이디 실패 재현→안전 종료→수업 안전 시작 가능 상태:
    통과
  - 독립 ADB 확인 최종 상태는 `ADMIN_IDLE`, 활성 수업 없음. 운영 재개 시
    선택한 반 수업 안전 시작 필요

## Goal 종료 후 Kiosk RC32/Web RC35 이름·안내·인쇄 후속 — 2026-07-27

- QR 승인 직후 Kiosk에 학생 전체 이름과 로그인 중 상태를 함께 표시
- Web의 로그인 차폐 화면부터 학습지·진단평가·문제 화면까지 네이티브 학생
  이름 배지를 유지하고 로그아웃·복구 때 즉시 제거
- QR 위치 안내를 0.4초 간격으로 갱신하고 새 QR 감지가 0.9초 동안 없으면
  이전 방향 문구를 지워 화면에 남지 않도록 변경
- 늦게 생성되는 사이트 상단 메뉴, 오류신고·문제지·필기·동영상·풀이 업로드
  제어를 `MutationObserver`에서 다시 숨기고, 수식 자동 선택 뒤 약 3초
  잔존하던 Ant `수식` tooltip/popover도 생성 즉시 숨김
- 카드 삽입 종이를 `55×80mm`, QR을 정확히 `30×30mm`, QR 상단을 카드
  상단에서 `28mm`, 이름 기준선을 `68mm`로 고정
- 학생 전체 이름은 QR 아래로 이동하고 `매쓰홀릭 채점 QR` 인쇄 문구 제거
- Android가 직접 생성한 A4 PDF를
  `output/pdf/matholic-qr-card-55x80-preview.pdf`로 꺼내 Poppler로 PNG
  렌더링해 외곽선·QR·이름 정렬과 잘림 없음 확인
- 구현 커밋:
  - Web 이름과 늦은 제어 제거 `e44fd08`
  - QR 이름·안내 만료 `42f45d8`
  - 55×80mm 카드 레이아웃 `3dc2fe1`
  - 늦은 상단 메뉴 제거 `ef74681`
  - RC32/RC35 버전 준비 `8a97bdf`
- Android 13 에뮬레이터 전체 계측 Web 68개·Kiosk 34개, 실패 0
- 전체 debug 단위시험·lint·assemble 204 tasks, release 158 tasks와 APK
  이중 검증 통과
- A에 Kiosk `0.6.0-rc32`/code 37과 Web POC `0.4.0-rc35`/code 52를
  `adb install -r --no-streaming`으로 보존형 설치
- 설치 전후 UID `10288`/`10287`, firstInstallTime, Device Owner, 전용 HOME
  유지. 설치 APK와 보관 artifact SHA-256 일치, 설치 뒤 두 앱 관련
  `FATAL EXCEPTION` 없음
- 앱 업데이트로 잠시 `LockTask NONE`이 된 뒤 화면을 깨우지 않는 HOME
  시작으로 `LOCKED` 복구. 화면은 꺼진 상태
- 미검증 실물 항목:
  - 관리자 PIN 복구 뒤 기존 반·학생 보존
  - 학생 이름이 QR 승인→로그인→Web 전 구간에 표시되는지
  - QR을 렌즈 밖으로 빼면 0.9초 안에 방향 안내가 사라지는지
  - 문제·전체답안 전환에서 사이트 UI와 `수식` 도움말이 보이지 않는지
  - 새 55×80mm PDF 실물 인쇄 크기와 PVC 케이스 삽입·QR 재인식

## 사용자 동석 연속 개발 Goal — 2026-07-28

이 절은 위의 2026-07-26 종료 기록보다 최신 사용자 지시이며, 사용자가 다시
Goal 실행을 요청한 현재 운영 기준이다. 사용자는 A 기기 옆에 있으며, 물리
조작이나 판단이 필요하면 우회하여 다른 작업으로 넘어가지 말고 즉시 사용자에게
요청한다. 작업을 시작하거나 자동 재개할 때마다 이 파일 전체와 사용자 제공
`AGENTS.md`, 현재 Git 상태를 먼저 읽고 이 최신 절을 적용한다.

### 재개 시점 실물 결과와 현재 A

- 유광 코팅한 새 카드도 실제 전면 카메라 QR 인식 통과. 무광 전환은 추후
  운영 구상이 확정된 뒤 선택하며 현재 개발 차단 요인이 아님
- Kiosk RC32/Web RC35 실물:
  - Web 로그인 중 학생 이름 표시: 통과
  - Web 진입 뒤 학생 이름 유지: 통과
  - QR 인증 직후 이름은 전환이 너무 빨라 사람이 확인하기 어려움: 실패
- QR 성공 뒤 학생 이름과 `QR 인증이 완료되었습니다`를 0.9초간 고정하고,
  이후 이름과 `로그인 중입니다`로 바꿔 Web을 시작하도록 `4903de9`에서 수정
- Kiosk RC33 준비 커밋 `01c8f16`
- Kiosk 단위시험·debug APK·계측시험 소스 컴파일, release 158 tasks와
  APK 이중 검증 통과
- A에 Kiosk `0.6.0-rc33`/code 38을 동일 signer로 보존형 설치
  - UID `10288`, firstInstallTime, Device Owner와 전용 HOME 유지
  - 설치 APK와 artifact SHA-256 일치:
    `85BFEEEA5BFCBEE54413E8367E4FD8A92C496F5A24EE0611C403555176B3F82A`
  - 설치 뒤 `LOCKED` 복구, 관련 `FATAL EXCEPTION` 없음
- 사용자 실물 재검증:
  - 앱 업데이트 실패폐쇄 복구와 수업 재시작: 통과
  - QR 인증 완료 이름 0.9초 표시: 통과
  - Web 로그인 중·진입 뒤 이름 유지: 통과
  - QR을 렌즈에서 치운 뒤 방향 안내가 약 1초 안에 사라짐: 통과
  - 정상 채점 종료 왕복과 최종 `QR_READY`: 통과
  - A USB 전원과 PC 무절전 준비: 통과

### 동석 Goal 개발 우선순위

1. 사용자가 처음 제시한 요구사항과 아직 미검증인 실물 항목을 먼저 완료한다.
2. 현재 상태기계·DB 불변조건·비동기 경합·Web 경로 제한·DOM 지연 생성·
   민감정보 수명·QR/인쇄 좌표를 전방위적으로 검토한다.
3. 발견한 문제는 가능하면 수정 전 실패하는 자동 회귀시험을 먼저 추가하고,
   작은 구현·관련 시험·전체 회귀·검토 순으로 완료한다.
4. 정상·경계·복구·재시작·중복 입력·지연 콜백·잘못된 QR/계정 등 실패주입은
   에뮬레이터와 합성 데이터에서 적극 수행한다.
5. 각 의미 있는 검증 단위는 독립적으로 되돌릴 수 있는 작은 커밋으로 만들고
   `codex/rc03-usability`에 푸시한다.
6. 릴리스 후보가 실제 개선을 포함할 때만 versionCode를 올리고 서명·artifact
   검증을 수행한다. 시간 채우기용 버전 상승이나 무의미한 변경은 하지 않는다.

### 이번 동석 Goal 운영 규칙

- 파괴적·비가역적 작업에는 기존 “사용자 부재 중 안전 경계” 이상의 주의를
  적용한다.
- 사용자 PIN이나 물리 복구가 필요한 A 설치는 여러 개발 단위마다 반복하지
  않는다. 에뮬레이터·정적 검토·자동시험으로 가능한 작업을 먼저 묶어 완료하고,
  실제로 검증할 가치가 있는 안정된 릴리스 하나와 물리 시험 목록을 준비한다.
- A가 이미 `RECOVERY_REQUIRED`, 활성 Web 세션 또는 불명확한 상태라면 새
  설치·실패주입을 연쇄 실행하지 않는다.
- 현재 최우선 작업에 사용자 조작·관찰·선택이 필요해지는 즉시 작업을 멈추고
  필요한 행동과 관찰 결과를 명확히 요청한다. 이를 피하려고 덜 중요한 다른
  작업으로 우회하지 않는다.
- 명백한 버그, 보안·복구·데이터 무결성 결함, 이미 확정된 사용자 요구의
  누락은 합리적으로 판단해 구현한다. 반면 서로 다른 운영 결과를 만드는
  주관적 UI·업무 흐름·데이터 정책 선택은 한 방향을 몰래 확정하지 않는다.
  근거·대안·권장안을 제시하고 사용자 선택을 요청한다.
- 미검증 항목은 미검증으로 남기며, 사용자 응답을 기다리는 상태를 완료로
  보고하지 않는다.
- 최초 요구사항과 이번 후속 요구를 요구사항별로 검증하고, 전방위 감사에서
  발견한 중요 결함을 처리하고, 관련 자동시험·안전한 A 실물시험·릴리스 검증·
  운영 문서를 동기화했으며 알려진 실행 가능한 중요 문제가 남지 않으면
  사용자의 별도 중단 지시 없이 Goal을 정상 완료 처리한다.

## 사용자 동석 Web RC41/RC42 주관식·키보드 후속 — 2026-07-28

- RC41에서 문제마다 주관식 첫 입력 저장 여부가 다른 실물 결함을 재현했다.
- 공개 Matholic 자산에서 각 MathQuill 인스턴스가 최초 편집 알림 두 번을
  무시하고, 답 저장 뒤 나타나는 회색 16px 제어가 전체 답 삭제 버튼임을 확인했다.
- RC41의 화면 틀 단위 준비 표시 때문에 React가 같은 틀에서 새 편집기를
  생성한 문제와 처음부터 수식형인 문제가 누락됐다.
- 실제 편집기 DOM 인스턴스마다 최초 알림을 안전하게 소진하고, 값 복원 확인
  뒤에만 학생 입력을 허용하도록 `5ffff56`에서 수정했다.
- 회색 전체삭제 버튼을 생성 시점부터 숨기고, 숫자형 키패드는 유지했다.
- 키보드가 올라올 때 네이티브 `채점 끝내기`가 입력칸 위로 이동하지 않도록
  앱 창 resize를 금지했다.
- 수정 전 신규 시험에서 첫 숫자 저장값 `null`과 soft input adjust 값 `0`
  실패를 각각 재현하고 수정 뒤 통과했다.
- Android 13 Web 전체 계측 75개, Web 단위시험·lint·debug assemble,
  release 158 tasks와 APK 이중 검증을 통과했다.
- Web RC42 준비 커밋 `e86cc81`; A에 `0.4.0-rc42`/code 59를 동일 signer로
  보존형 설치했다.
- Web UID `10293`, firstInstallTime, Kiosk DB, Device Owner·전용 HOME·
  `LOCKED` 유지, 설치 APK와 artifact SHA-256 일치, 설치 후 crash 없음.
- 사용자 실물 검증:
  - 여러 주관식 문제의 한 자리·세 자리·일부 삭제 후 문제 왕복 보존: 통과
  - 회색 전체삭제 제어 제거, 숫자 키패드 크기, 키보드 겹침 방지: 통과
  - 학생 Web 시스템 뒤로가기 무반응·세션 유지: 통과
  - 회색 제어가 숨겨질 때 약 0.2초 음영이 보이나 사용상 불편 없음
- 실물 시험 종료 뒤 독립 ADB 확인 최종 상태: Kiosk `QR_READY`,
  Device Owner·전용 HOME·`LOCKED` 유지

## 사용자 동석 Web RC46 수식 방향 패드 후속 — 2026-07-29

- 역 T자 수식 방향 패드를 사용자 요청에 따라 최종적으로 오른쪽 10mm,
  아래 10mm 더 이동했다.
- 구현 커밋 `041aeb1`, Web POC RC46 준비 커밋 `ac98a54`.
- A에 Web POC `0.4.0-rc46`/code 63을 동일 signer로 보존형 설치했다.
- artifact/설치 APK SHA-256:
  `F499459F0690536F9217294D36DB9D6BEA3460789CA7CB23820B40E19FB4A2CA`
- UID `10293`, firstInstallTime `2026-07-28 13:12:16`, Device Owner·전용
  HOME·`LOCKED` 유지.
- 사용자 실물 확인에서 `뒤로`, 문제와 입력칸을 가리지 않고 네 방향 커서
  이동도 정상으로 통과했다.

## 지정 PC 암호화 PDF 전송 완료 — 2026-07-29

- 계정 로그인이나 주변 모두 공개가 필요한 Quick Share 대신, 같은 사설
  Wi-Fi에서 물리 페어링한 지정 PC 한 대로만 카드 PDF를 보내는 로컬
  수신기와 Kiosk 송신 기능을 구현했다.
- PC 수신기 `0.1.0`:
  - 128비트 receiver ID와 256비트 페어링 비밀키
  - AES-256-GCM PDF 암호화·인증, HMAC-SHA256 응답 인증
  - 5분 시간창, 5MiB 제한, request ID 재전송 차단
  - Private 프로필 TCP 48129와 설치 EXE만 허용하는 Windows 방화벽
  - 사용자 다운로드의 `Matholic QR Cards` 폴더에 저장
- A에서는 페어링 비밀을 Android Keystore로 보호하며 인터넷·외부 서버를
  사용하지 않는다.
- 주요 커밋:
  - `0b0df69` PC 수신기
  - `ba4ee4d` Python 생성 캐시 제외
  - `474132b` Kiosk 페어링·전송
  - `f5fd36a` Kiosk RC36 준비
  - `d225ed6` 패키징 EXE 진입점 오류 수정·스모크 검증
  - `c08dc80` 설치본 암호화 왕복 검증
  - `eda9987` 관리자 작은 화면의 PC 전송 제어 접근성 보강
- 자동 검증:
  - PC 수신기 시험 7개 통과
  - 패키징된 EXE 스모크와 설치본 합성 암호화 왕복 통과
  - Kiosk JVM 시험 통과
  - Android 13 Kiosk 전체 계측 38개, 실패 0
  - release 158 tasks와 APK 이중 검증 통과
- A 설치:
  - Kiosk `0.6.0-rc37`/code 42
  - SHA-256
    `8F7B59DEBCBC1A276A35A8A9606E187A13926FB3392F16916CB0BE54F2B2460B`
  - UID `10288`, firstInstallTime `2026-07-24 12:52:28`, 기존 DB,
    Device Owner·전용 HOME·`LOCKED` 유지
- PC 설치:
  - `%LOCALAPPDATA%\MatholicPdfReceiver\app\MatholicPdfReceiver.exe`
  - artifact/설치본 SHA-256
    `EBAB8CE69F1AB29D1F80DC84B0BB2EF5806A0BAC037FC30E846C09B1144C9EAE`
  - 자동 시작 바로가기, Private 방화벽 규칙, TCP 48129 대기 확인
  - Google Quick Share PC 앱은 비교 뒤 제거
- 사용자 실물 종단간 검증:
  - A와 `DESKTOP-D4AGJI7` 재페어링: 통과
  - A 암호화 전송 상태 표시: 통과
  - PC의 `20260729-112723_테스트 QR.pdf` 저장: 통과
  - 수신 PDF 신규 QR 로그인→문제 화면→채점 끝내기→`QR_READY`: 통과
  - 이전 QR 무효화: 통과
- 최종 독립 확인:
  - A `QR_READY`, Device Owner·전용 HOME·`LOCKED`
  - `RECOVERY_REQUIRED`·채점기 잠금 표시 없음
  - 최근 Matholic 관련 fatal 일치 항목 0
  - PC 수신기 실행·TCP 48129 대기
- 미검증:
  - 자동 시작 바로가기와 직접 실행은 확인했으나 PC 완전 재부팅 뒤 자동
    실행은 이번 작업에서 시험하지 않았다.
- 사용자가 직접 프린터 수정을 필수로 하지 않기로 결정했으므로 Android
  직접 인쇄는 현재 운영 범위에서 보류하고 지정 PC 전송을 기본 경로로 삼는다.
