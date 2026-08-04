# Sol Max 연속 리뷰·개발 누적 보고서

## 현재 상태

- `last_updated`: 2026-08-04 19:07:16 +09:00
- 현재 branch: `codex/sol-continuous-development-20260804`
- 현재 HEAD / upstream: `f4d380537d912729b8fa0a00254b165280972f39` /
  `origin/codex/sol-continuous-development-20260804`
- 마지막 push 성공 commit: `f4d380537d912729b8fa0a00254b165280972f39`
- 최초 보존 기준선: `master`의
  `ccf410d6b9758c7594a07e94e459bf7e83c554bc`; 당시 `origin/master`보다
  24 commits ahead
- 현재 보존 대상: Goal 시작 전부터 있던 미추적 `outputs/`. 수정·stage·삭제하지
  않는다.
- 현재 작업 중: `SOL-0001` — release 운영 문서의 현재 버전 기준선 불일치
- 다음 우선 큐:
  1. Kiosk↔Web launch/result의 session ID 결합, 중복 완료·늦은 결과 상태 전이
     독립 재감사
  2. 네트워크 단절 overlay의 WebView·IME·hardware key·DOM 입력 차단 재검증
  3. PC receiver의 frame deadline·동시 연결·queue·shutdown resource bound 재감사
  4. 실제 A의 관리자→QR→시험계정→문제→종료 흐름과 화면 품질 재검증

### 열린 finding과 제약

- 열린 P0/P1/P2: 없음. 과거 보고서의 후보는 현재 source와 독립 재검증 전에는
  열린 결함으로 승격하지 않는다.
- 현재 검토 P3/P4: `SOL-0001` 1건.
- 사용자 판단 대기: 없음.
- 현재 제약:
  - 비민감 화면 확인을 위해 원격 지원 Start→Capture를 시도했으나 태블릿이
    screenshot을 거부했다. 실패 직후 Stop을 실행해 `REMOTE_SUPPORT=INACTIVE`를
    확인했다. 화면 내용과 세션 상태는 미검증이며 성공으로 기록하지 않는다.
  - 설치 APK signer 대조용으로 만든 로컬 임시 디렉터리
    `%LOCALAPPDATA%\Temp\MatholicSolSignerCheck-019fcc38`의 삭제 명령이 실행
    정책에 의해 거부됐다. 내부에는 A에서 읽기 전용으로 가져온 현재 설치 APK
    두 개만 있으며 저장소 밖이다. 정책을 우회해 삭제하지 않았다.

## 시작 기준선 — 2026-08-04 19:07 +09:00

### 지침·자료 확인

- 현재 대화에서 사용자가 제공한 `AGENTS.md`와
  `docs/SOL_MAX_CONTINUOUS_REVIEW_AND_DEVELOPMENT_GOAL.md` 전체를 다시 읽었다.
- 저장소 안에는 별도 `AGENTS.md`가 없다.
- 제목이 정확히 `생성 파일 검토 및 역질문`인 과거 Codex 작업을 읽기 전용으로
  확인했다. 최근 핵심 정정은 시험 답안을 화면에서만 해제하지 말고 공식
  `unanswered` 저장, 완전 종료, 동일 과제 재진입과 최소 60초 무복원까지
  확인해야 한다는 점이다.
- `README.md`, `SECURITY.md`, `docs/PRODUCT_DECISIONS.md`,
  `docs/KNOWN_LIMITATIONS.md`, `docs/DEVICE_A_WEBPOC_HANDOFF.md`,
  `docs/REMOTE_ADMIN_PIN.md`, `docs/CONTINUOUS_DEVELOPMENT_GOAL_PROMPT.md`,
  `docs/RELEASE_OPERATIONS.md`, 최신 `docs/BUILD_VERIFICATION.md` 구간과
  `docs/CONTINUOUS_DEVELOPMENT_GOAL.md`의 현행 운영 구간을 확인했다.
- `reports/LUNA_MAX_READ_ONLY_REVIEW_2026-08-02.md`는 상단 최신 정정·요약·ID별
  처리 결과만 과거 감사 자료로 확인했다. 오래된 후보는 현재 수정 근거로
  사용하지 않는다.

### Git·원격

- 시작 status: 전용 branch가 같은 이름의 origin branch를 추적하며 ahead/behind
  `0/0`; 미추적 `outputs/` 외 tracked diff 없음.
- `git fetch --prune origin`: 성공. pull·merge·rebase는 수행하지 않았다.
- `master`, `origin/master`와 공유 이력은 변경하지 않았다.

### 실제 A와 artifact

- 마지막 확인: 2026-08-04 19:04~19:07 +09:00
- 승인 ADB device: 정확히 한 대, serial `R54TB029FHZ`, model `SM-P610`,
  Android 13 / SDK 33.
- Kiosk: `0.6.0-rc69`/code 74, UID 10288, first install
  `2026-07-24 12:52:28`, dataDir 유지.
- Web POC: `0.4.0-rc132`/code 149, UID 10293, first install
  `2026-07-28 13:12:16`, dataDir 유지.
- 두 설치 APK와 대응 artifact의 signer SHA-256은 모두
  `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`로
  일치했다.
- Device Owner:
  `com.local.matholickiosk.kiosk/.admin.KioskDeviceAdminReceiver`.
- preferred HOME: `com.local.matholickiosk.kiosk/.MainActivity`.
- `topResumedActivity`: Kiosk `MainActivity`; Lock Task: `LOCKED`.
- 원격 지원: 최종 `INACTIVE`.
- 화면 내용, 현재 DB session state, 시험계정 상태는 변경 없이 증명하지 못해
  미검증으로 남긴다.

## Finding

### SOL-0001 — release 운영 문서의 현재 버전 기준선 불일치

- 영역: 빌드·릴리스·운영 문서
- 심각도: P3
- 신뢰도: 높음
- 상태: 확정
- 사용자 영향: 운영자가 `docs/RELEASE_OPERATIONS.md`만 따르면 현재 A와 보관
  artifact를 RC67/RC125로 오인해 설치·검증·롤백 대상을 잘못 선택할 수 있다.
- 재현 조건: `docs/RELEASE_OPERATIONS.md`의 `현재 상태`를 현재 source, artifact,
  실제 A와 대조한다.
- 기대 결과: 운영 문서의 현재 A·검증 묶음이 실제 설치본과 최신 검증 artifact를
  가리킨다.
- 실제 결과: 문서는 Kiosk RC67/code 72와 Web RC125/code 142를 현재값으로
  기록하지만 source, release script, checksum, 최신 빌드 기록과 실제 A는
  Kiosk RC69/code 74와 Web RC132/code 149다.
- 정적 근거:
  - `kiosk/build.gradle.kts`: RC69/code 74
  - `webpoc/build.gradle.kts`: RC132/code 149
  - `scripts/build-release.ps1`, `scripts/verify-release-apks.ps1`: RC69/RC132
  - `artifacts/RELEASE_SHA256SUMS.txt`: RC69/RC132
  - `docs/RELEASE_OPERATIONS.md`: RC67/RC125
- 동적 근거: 승인 A의 package dump에서 RC69/code 74와 RC132/code 149 확인;
  설치 APK와 보관 artifact signer 일치.
- 반대 근거: `README.md`와 `docs/BUILD_VERIFICATION.md` 최신 절은 이미 실제값을
  기록해 모든 문서가 잘못된 것은 아니다.
- 원인·결정: RC69/RC132 배포 뒤 운영 문서 상단이 함께 갱신되지 않은 문서 drift로
  판단한다. 현재 사실만 최소 교정하고 과거 release 이력은 바꾸지 않는다.
- 변경 파일: 아직 없음.
- 관련 commit: 아직 없음.
- 실행한 검증:
  - source·release script·checksum의 버전 문자열 대조
  - A package version/UID/firstInstallTime, Device Owner, HOME, Lock Task 확인
  - 설치 APK와 artifact signer SHA-256 대조
- 수행하지 않은 검증: 새 build·APK 설치·실제 화면 조작은 이 문서 drift 확정에
  필요하지 않아 수행하지 않았다.
- rollback: 구현 commit 생성 뒤 `git revert <commit>`과 문서 문자열 재대조.
  A 설치본에는 영향을 주지 않는다.

## 최근 변경·검증·전달

- 아직 Sol 구현 commit 없음.
- 원격 branch는 시작 HEAD까지 동기화돼 있다.
- rollback 수행 없음.
