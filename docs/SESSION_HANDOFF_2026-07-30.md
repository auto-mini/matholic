# 2026-07-30 세션 인계

## 현재 결론

- 브랜치: `codex/fix-submit-recovery-timeout`
- 기능 코드 체크포인트: `045ab1c`
- A 기기 `R54TB029FHZ`:
  - Kiosk `0.6.0-rc43`/code 48
  - Web `0.4.0-rc65`/code 82
  - Web은 release 서명·비디버그 빌드
  - Device Owner 유지
  - Lock Task `LOCKED`
  - 현재 전경은 `com.local.matholickiosk.kiosk/.MainActivity`
- 작업 트리에는 사용자 소유 untracked `output/`, `tmp/`가 있다.
  삭제·이동·stage하지 않는다.

## 이번 세션 반영 범위

기준 시작점은 `3702c43` 이후이며 주요 기능 커밋은 다음과 같다.

1. `f487a6c`, `93f3989`
   - 이전·다음 문제 버튼 112×96px
   - 실제 라벨 없는 SVG 버튼 탐지
   - 좌상단 진한 문제번호 배지와 기존 번호 직접 선택 유지
   - ACTIVE 세션 앱 창 밝기 80%, 비활성 상태에서 이전 밝기 복원
2. `e2ab082`
   - 화면 앱명 `채점 관리`, `학습`
   - 관리자 헤더·시험 모드·보안 상태의 제품명과 개발 단계 문구 중립화
   - QR 모양 앱 아이콘을 일반 문서 확인 아이콘으로 교체
   - 내부 package, Device Owner와 allowlist는 호환성을 위해 유지
3. `630fce1`
   - `월1`~`토2` 고정 12개 반 빠른선택
   - 별도 테스트반 생성·삭제
   - 수업 시작 사전점검
   - PIN 후 현재 수업 소속·보강 학생 수동 선택
   - 선택 반 QR 전체 원자적 재발급과 A4 최대 9장 배치 인쇄
   - 접이식 답안 현황 지도
   - `모름`을 완료 답변으로 집계하고 빈칸 대신 `모름` 입력 안내
   - 연결 실패용 답안 보호 화면과 비식별 상태 코드
4. `66ebd42`~`c6ca306`
   - 사용자 검수용 `docs/KEYPAD_DESIGN_REVIEW.svg`
   - 4×3 숫자 배열, 구역 제목선, 좌우 정렬과 세 구역 동일 간격 확정
5. `6819057`, `045ab1c`
   - 검수안을 실제 하단 주관식 수식 키패드로 구현
   - `입력기` 버튼이 없는 MathQuill 편집기도 준비
   - 중첩 스크롤의 끝에서도 입력칸이 키패드 위로 올라오도록 보강

## 하단 수식 키패드 계약

- 숫자 4×3:
  - `1 2 3 부호`
  - `4 5 6 .`
  - `7 8 9 0`
- 수식: 루트, 분수, 파이
- 편집: 위·왼쪽·아래·오른쪽, 한 칸 삭제, 전체 지움
- 전체 지움은 2초 안에 같은 버튼을 두 번 눌러야 실행된다.
- `×`, `÷`, 괄호, 제곱, 더하기와 별도 빼기 키는 제공하지 않는다.
- 사용자가 수식 편집기를 직접 터치할 때만 연다.
- 프로그램 focus, 답안제출 진입만으로는 열지 않는다.
- 바깥 또는 답안제출 터치 시 닫는다.
- custom keypad 생성이 실패하면 기본 입력을 억제하지 않는다.
- 원래 루트·분수·파이 toolbar는 custom keypad 생성 성공 뒤에만 숨긴다.
- 키패드는 body 직속 fixed panel이며 실제 입력칸과 최소 15px 간격을
  확보하도록 내부·상위·페이지 스크롤을 순서대로 사용한다.

## 최종 자동 검증

| 검증 | 결과 |
|---|---|
| Android 13 Web DOM 계약 | 56/56 통과 |
| JVM 단위시험 | 113/113 통과: kiosk 56, webpoc 48, probe 8, poc 1 |
| 전체 debug 작업 | `scripts/build.ps1`, 204 tasks 통과 |
| release 작업 | `scripts/build-release.ps1`, 158 tasks 통과 |
| debug lint | 네 모듈 통과, 오류 0 |
| release lint·APK 이중 검증 | 통과 |
| 밝기 적용·복원 Activity 계측 | 1/1 통과 |
| 편의성 구현 시점 Kiosk 계측 | 42/42 통과 |
| 편의성 구현 시점 Web 전체 계측 | 92/92 통과 |

완료로 간주하면 안 되는 검증:

- 전체 `RecoveryInstrumentedTest`는 실제 프록시/WebView 초기화 대기로
  제한시간 안에 끝나지 않았다. assertion 실패가 확인된 것은 아니지만
  전체 통과도 아니다.
- 최종 A 설치 단계에서는 실제 Room 학생·반 데이터를 보호하기 위해
  Kiosk 계측을 다시 실행하지 않았다.
- 실제 Matholic 학습지와 실제 프린터를 사용하는 현장 시험은 사용자
  부재로 실행하지 않았다.

## 릴리스 산출물

- `artifacts/matholic-kiosk-0.6.0-rc43-release.apk`
  - SHA-256
    `3EDD580A3FDD1ECCF367F39C40F2F010F9063328004B57CDA287E1E8D85F6AD4`
- `artifacts/matholic-webpoc-0.4.0-rc65-release.apk`
  - SHA-256
    `58F92D218A1757259CAD21CCC9A74B288CE9B4489F32875FCC73299B1F2736AD`
- signer SHA-256
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- 체크섬 파일: `artifacts/RELEASE_SHA256SUMS.txt`

A 보존 설치 전후:

- Web firstInstallTime:
  `2026-07-28 13:12:16` 유지
- Web `ceDataInode=28569` 유지
- 설치 전 임시 계측 앱의 `DEBUGGABLE` flag는 최종 release에서 제거
- 계측 패키지 `com.local.matholickiosk.webpoc.test` 제거
- Device Owner와 Lock Task `LOCKED` 유지

## 다음 현장 확인 순서

1. 주관식이 포함된 학습지를 로그인 직후 연다.
2. 문제 화면에서 답안을 건드리지 않고 바로 전체답안을 연다.
3. 모든 주관식 입력칸이 최소 높이를 유지하고 저장 답안이 보이는지 본다.
4. 입력칸 직접 터치에서만 하단 키패드가 열리는지 확인한다.
5. 숫자·부호·소수점·루트·분수·파이와 네 방향·삭제를 실제 입력한다.
6. 전체 지움 1회는 경고만, 2회는 삭제인지 확인한다.
7. 임시저장→나가기→재진입 후 문제 화면과 전체답안에 같은 답이 보이고
   수정 가능한지 확인한다.
8. 답안제출 진입 때 키패드가 자동으로 뜨지 않고, 열려 있었다면 닫히는지
   확인한다.
9. 112×96px 이전·다음 버튼, 좌상단 문제번호 배지와 번호 직접 선택,
   답안 현황 지도와 `모름` 완료 집계를 확인한다.
10. 관리자에서 고정 12개 반, 테스트반, 사전점검, 수동 학생 선택과
    반 전체 QR 9장 배치 실제 출력물을 확인한다.
11. 앱 목록·관리자 화면·잠금/오류 화면에 “매쓰홀릭 키오스크” 또는
    개발 단계명이 보이지 않는지 확인한다.

## 다음 작업자가 지켜야 할 점

- Web 코드를 더 변경하면 `rc65` APK를 덮어쓰지 말고 versionCode와
  versionName을 올린다. 다음 값은 code 83 이상의 새 RC다.
- 버전 변경 시 다음 네 곳을 같이 수정한다.
  - `webpoc/build.gradle.kts`
  - `scripts/build-release.ps1`
  - `scripts/provision-release-device-owner.ps1`
  - `scripts/verify-release-apks.ps1`
- 한글·공백 OneDrive 경로의 APK를 ADB `--no-streaming`으로 직접 설치하면
  원격 임시 경로 오류가 날 수 있다. 다음 ASCII junction을 사용한다.
  `C:\Users\user\AppData\Local\CodexWorkspaces\matholic-kiosk`
- A에서 Web 계측을 수행하면 release signer로 app/test debug APK를 모두
  다시 서명해야 한다. 끝난 뒤 반드시 최종 release APK를 보존 설치하고
  test package를 제거한다.
- Recovery 계측은 Web preferences를 초기화할 수 있다.
- Kiosk 계측은 실제 Room 학생·반 데이터에 영향을 줄 수 있으므로 A 대신
  격리된 에뮬레이터나 테스트 DB를 우선 사용한다.
- `output/`, `tmp/`는 사용자 작업물이다.
- 실제 사이트 DOM은 외부 상태이므로 현장 실패가 나오면 사진과 해당 문제의
  문제 화면/전체답안 차이를 먼저 확보한다.

## 재현 명령

```powershell
.\scripts\build.ps1
.\scripts\build-release.ps1
```

기기 상태 확인:

```powershell
$adb='C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe'
& $adb -s R54TB029FHZ shell dumpsys package com.local.matholickiosk.webpoc
& $adb -s R54TB029FHZ shell dumpsys device_policy
& $adb -s R54TB029FHZ shell dumpsys activity activities
```

## 롤백

- 소스 롤백은 공유 이력을 되쓰지 말고 해당 기능 커밋을 `git revert`한 뒤
  새 Web 버전으로 빌드하는 방법이 안전하다.
- A에는 낮은 versionCode APK를 즉시 강제 설치하지 않는다. 먼저 현재
  release APK와 앱 데이터 보존 상태를 기록하고, 필요하면 되돌린 소스를
  더 높은 versionCode로 다시 release한다.
- Kiosk 데이터와 Device Owner를 지우는 초기화는 이 작업의 롤백 절차가
  아니다.
