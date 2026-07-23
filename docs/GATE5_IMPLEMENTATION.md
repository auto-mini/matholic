# Gate 5 전용기기 잠금 구현·운영

검증일: 2026-07-24 (Asia/Seoul)

## 목적

Gate 5는 수업 중 학생이 홈, 최근 앱, 알림창, 설정 또는 허용되지 않은 앱으로 이동하지 못하게 한다. Android의 화면 고정이 아니라 **Device Owner가 허용 목록을 관리하는 Lock Task mode**를 사용한다.

## 잠금 경계

- Device Policy Controller: `com.local.matholickiosk.kiosk`
- Device Admin receiver: `.admin.KioskDeviceAdminReceiver`
- Lock Task 허용 패키지:
  - `com.local.matholickiosk.kiosk`
  - `com.local.matholickiosk.webpoc`
- 전용 HOME: `kiosk/.MainActivity`
- Lock Task 기능: `LOCK_TASK_FEATURE_NONE`
- 다른 앱의 overlay 창: 수업 잠금 중 `DISALLOW_CREATE_WINDOWS`

QR 대기, 관리자 PIN 입력과 Web 채점 세션에서는 잠금을 유지한다. QR 화면의 **관리자** 버튼은 잠금을 풀지 않는다. 올바른 관리자 PIN을 확인한 뒤에만 `stopLockTask()`를 호출해 학생관리와 QR 인쇄를 허용한다. 관리자 화면을 이탈하면 PIN 화면으로 재잠그고 Lock Task를 다시 시작한다.

## 공장초기화가 필요한 이유

Device Owner는 전용기기 초기 등록 단계에서 설정해야 한다. 기존 사용자 계정과 앱 데이터가 있는 기기에 사후 추가하는 운영 방식은 사용하지 않는다. 공장초기화하면 다음 값은 복구되지 않는다.

- 앱 private Room DB
- Android Keystore 키와 암호화된 학생 자격정보
- 관리자 PIN verifier
- 기존 QR 카드의 서버 역할을 하던 로컬 token hash

따라서 초기화 전 QR은 전부 폐기된 것으로 취급하고, 초기화 후 학생을 다시 등록해 새 QR을 발급한다. 앱 DB나 Keystore 키를 외부로 내보내는 우회 백업은 만들지 않는다.

## 프로비저닝

1. 대상 기기를 공장초기화한다.
2. 초기 설정에서 백업을 복원하지 않고 Google/Samsung 계정을 추가하지 않는다.
3. 단일 Android 사용자만 만든다.
4. 개발자 옵션과 USB 디버깅을 켜고 PC를 허용한다.
5. 전체 빌드 후 다음 명령을 실행한다.

```powershell
.\scripts\provision-gate5-device-owner.ps1 -Serial R54TB029FHZ
```

스크립트는 모델, 사용자 수와 계정 수를 확인하고 `webpoc`·`kiosk`를 설치한다. 카메라 권한을 부여한 뒤 `kiosk`를 Device Owner로 설정하고 실행한다. 실행된 앱은 전용 HOME과 두 패키지 allowlist를 설정하고 관리자 PIN 설정 화면을 Lock Task로 잠근다.

읽기 전용 상태 확인:

```powershell
.\scripts\verify-gate5-device-owner.ps1 -Serial R54TB029FHZ
```

## 복구와 해제

- 정상 운영 해제: 앱의 **관리자** → 관리자 PIN 성공
- 수업 복귀: 반 수업 시작 또는 QR 대기 복귀
- 관리자 PIN 분실: 기본 PIN과 복구 PIN이 없으므로 공장초기화
- Device Owner 제거: 부분 해제 명령을 운영 절차로 제공하지 않으며 공장초기화
- 앱 업데이트: 같은 applicationId와 서명으로 `adb install -r`

## 자동 검증

- Gate 5 JVM 정책 테스트 3개
  - `kiosk`와 신뢰된 `webpoc`만 allowlist
  - 잠금 상태와 관리자 해제 상태 구분
  - Device Owner 부재를 잠금 활성으로 오인하지 않음
- Gate 5 manifest 계측 1개
  - Device Admin receiver의 `BIND_DEVICE_ADMIN` 보호
  - `android.app.device_admin` metadata
  - exported 전용 HOME Activity
- 초기화 전 A 전체 `kiosk` 계측 9/9 통과
- 네 모듈 clean build 204 tasks, 단위 테스트 45/45·lint 오류 0·debug assemble 통과

## A 실제 전용기기 검증

대상은 공장초기화한 SM-P610, Android 13/API 33, 사용자 1명, Android 계정 0개다. `kiosk` `0.5.0-alpha05`/code 5와 `webpoc` `0.3.5`/code 17을 설치했다.

- `dpm set-device-owner`: 성공
- Device Owner: `kiosk/.admin.KioskDeviceAdminReceiver`
- Lock Task allowlist: `kiosk`, `webpoc`만 포함
- `LOCK_TASK_FEATURE_NONE`, 전용 HOME과 keyguard 비활성화: 적용
- QR·PIN·Web 구간 Lock Task: `LOCKED`
- 홈·최근 앱·알림창 조작: 키오스크/Web 밖으로 전환되지 않음
- 잠금 중 Android 설정 실행: 차단
- 관리자 PIN 성공: Lock Task `NONE`, 관리자 화면 유지
- 관리자 화면에서 뒤로 가기: PIN 화면과 `LOCKED`로 복귀
- 전면 새 QR → Web `시험계정 확인 완료` → 실제 문제 화면: 통과
- `채점 끝내기` → 약 8초 뒤 `QR_READY`: 통과
- Web 왕복 전체에서 Lock Task, 화면 켜짐과 keyguard 비활성 상태 유지
- 재부팅 뒤 Device Owner·전용 HOME·자동 실행·`LOCKED`: 유지
- 재부팅의 `RECOVERY_REQUIRED` → 관리자 PIN → 기존 수업 안전 종료 → 새 수업 시작: 통과
- 최종 상태: `GATE5-TEST`, `QR_READY`, 전면 카메라, `LOCKED`, 화면 켜짐

실기 중 두 결함을 발견해 수정했다. 관리자 인증 직후 Lock Task 종료가 만든 lifecycle 정지 이벤트가 관리자를 즉시 다시 잠그던 경쟁 조건은 1회 정지 이벤트 억제와 짧은 grace 구간으로 수정했다. 카메라 정지 시 `KEEP_SCREEN_ON`이 제거되어 화면과 keyguard가 나타나던 문제는 kiosk/Web 전체 세션의 화면 켜짐 유지와 Device Owner의 keyguard 비활성화로 수정했다. 두 수정 모두 A에서 재검증했다.

## Gate 5 alpha 판정

정의된 시험계정 기반 **Gate 5 alpha 범위는 PASS**다. 이 판정은 A 단일 모델, debug signing과 USB 디버깅이 유지된 개발용 전용기기 구성이다. 실제 학생 파일럿, 실제 프린터 출력, 장시간 무인 운전, release signing·업데이트 채널과 생산 배포는 포함하지 않는다.
