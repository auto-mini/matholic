# Gate 4 구현·검증 기록

검증일: 2026-07-23 (Asia/Seoul)

## 판정

Gate 4의 로컬 보안 기반과 QR 운영 흐름은 `kiosk` 0.4.0-alpha04에 구현했다. A(SM-P610)에서 실제 QR 카메라 인식, 시험계정 로그인·학생 확인·문제 화면·로그아웃 왕복과 필수 QR/Web 실패주입을 통과했다. 학생정보·자격정보 갱신, 비활성화·QR 폐기와 Android 인쇄 서비스용 1페이지 QR 카드 생성도 구현·검증했으므로 정의된 **Gate 4 alpha 범위는 PASS**다.

이 판정은 시험계정 기반 alpha 범위다. Android 인쇄 미리보기까지 확인했지만 실제 프린터 출력, 실제 학생 파일럿, 장시간 성능 통계, 단일 release 패키징과 Device Owner·Lock Task는 완료하지 않았다. Device Owner·Lock Task는 Gate 5이며 이 구현에 포함하지 않는다. 외부 알림은 사용자 결정에 따라 현재 요구사항에서 제외했고 네트워크 outbox도 만들지 않았다. 대신 민감정보 없는 로컬 `AuditEvent`를 사용한다.

## 구현 범위

- 별도 앱 패키지 `com.local.matholickiosk.kiosk`
- Android Views/XML, `minSdk 33`, `compileSdk/targetSdk 37`
- Room 2.8.4 데이터 모델
  - 학생, 반, 반 소속, 활성 수업, 수업 한정 보강생, 감사 이벤트, 관리자 verifier
- 관리자 PIN
  - 숫자 6~12자리
  - 기기별 16바이트 salt
  - PBKDF2-HMAC-SHA256, 600,000회, 256비트 verifier
  - 실패 1초부터 지수형 지연, 최대 300초
  - 기본 PIN·복구 PIN 없음
- 자격정보
  - Android Keystore의 비추출 AES-256 키
  - AES/GCM/NoPadding, 레코드·필드마다 새 IV, 128비트 tag
  - AAD: 학생 내부 UUID + 필드 종류
  - DB에는 암호문·IV·버전만 저장
  - 복호화 결과는 `CharArray`로 반환하고 사용 뒤 덮어씀
- QR
  - `MQR1:` + SecureRandom 32바이트 Base64URL(no padding)
  - Room에는 SHA-256 token hash만 저장
  - ZXing은 QR 카드 생성에만 사용하고 오류정정 H 적용
  - bundled ML Kit Barcode Scanning 17.3.0으로 QR 형식만 분석
  - CameraX 1.6.1 `STRATEGY_KEEP_ONLY_LATEST`
  - 전면 카메라 기본, 화면 버튼으로 전·후면 전환
  - 선호 렌즈가 없는 기기는 사용 가능한 다른 렌즈로 자동 대체
  - 빠른 전환 시 오래된 CameraX bind 요청을 세대 번호로 무효화
  - 일반 QR 무시, malformed/복수 MQR1 거부, 처리 중 분석 중단
  - 재발급은 기존 hash를 원자적으로 교체
  - 학생 비활성화는 기존 hash를 새 무작위 hash로 교체하고 활성 목록·검증 대상에서 제외
  - 현재 표시된 QR만 명시적 경고 뒤 Android PrintManager에 ISO A4 1페이지로 전달
  - 인쇄물에는 마스킹 이름과 QR만 포함하고, 요청을 감사기록에 남긴 뒤 화면 원문·bitmap을 즉시 제거
- 학생 운영
  - 표시명과 마스킹 이름 갱신
  - 새 자격정보를 fresh IV로 재암호화하고 모든 성공·실패 경로에서 입력 `CharArray` 덮어쓰기
  - 확인 대화상자를 거친 논리적 비활성화와 기존 QR 폐기
- 수업
  - 현재 반만 허용
  - 수업 한정 보강 학생은 수업 종료 시 삭제
  - 재시작 시 `QR_READY`는 `RECOVERY_REQUIRED`, 민감 중간 상태는 `LOCKED`
- Web 연동
  - 검증된 `webpoc` 0.3.4 엔진 재사용
  - Intent에는 자격정보를 넣지 않고 192비트 opaque nonce URI만 전달
  - signature 권한 + 호출 UID/package allowlist가 걸린 메모리 전용 ContentProvider
  - payload는 30초 TTL, 한 번 조회하면 즉시 삭제·덮어쓰기
  - 성공은 빈 로그인 화면과 Web 저장소 정리까지 확인한 뒤에만 `QR_READY`
  - 실패는 reason code만 반환하고 수업을 `LOCKED`

## 데이터 경계

소스·fixture·테스트에는 합성 학생과 합성 자격정보만 사용했다. 실제 학생 이름, 계정, QR 원문은 저장소·로그·테스트 보고서에 넣지 않았다.

Room 파일 전체를 별도 SQLCipher 계층으로 암호화한 것은 아니다. 계획서의 생산 요건대로 매쓰홀릭 아이디·비밀번호 필드를 Keystore AES-GCM으로 암호화했다. 학생 표시명, 내부 UUID, 반 관계, token hash와 비민감 감사 이벤트는 앱 private Room DB에 평문 구조로 저장된다. backup과 device transfer는 전부 제외한다.

## 자동 검증

- `kiosk` JVM 단위 테스트 14개: 통과
  - 기존 보안·상태·QR 정책 9개
  - 전면 선호, 렌즈 부재 fallback, 카메라 없음, 전환 정책 4개
  - QR 원문을 반환하지 않는 폐기용 hash 생성 1개
- A(SM-P610, Android 13) `kiosk` 0.4.0-alpha02 계측 테스트 6개: 통과
  - Keystore AES-GCM fresh IV·AAD binding
  - 암호문 저장·복호화 후 메모리 덮어쓰기
  - QR 재발급 이전 카드 폐기
  - 현재 반 거부와 수업 한정 보강/종료
  - 관리자 PIN UI unlock과 `FLAG_SECURE`
  - 1회용 credential bridge 조회·만료·폐기
- A `webpoc` 회귀 계측 테스트 27개: 통과
- `kiosk`·`webpoc` 단위 테스트, lint, debug assemble: 통과
- ADB `screencap`: `FLAG_SECURE`에 의해 0바이트로 차단 확인
- 외부 shell UID의 credential provider 조회: signature permission으로 거부
- 존재하지 않는 bridge handle 주입: `CREDENTIAL_BRIDGE_EMPTY`로 잠금 후 자동 복귀
- A의 운영 DB 보존을 위해 데이터 삭제형 전체 suite 대신 alpha04 대상 계측 4개를 선택 실행: 통과
  - 저장소 3개: 암호문·fresh IV·입력 덮어쓰기·감사기록·논리적 비활성화·QR 폐기
  - 인쇄 1개: ISO A4 1페이지 PDF 생성·렌더링 내용·소유 bitmap 폐기

`0.4.0-alpha03` 설치 뒤에는 시험계정 DB 보존을 위해 DB를 비우는 기존 계측 suite를 다시 실행하지 않았다. 카메라 변경은 JVM 정책 4개, lint, assemble, A 데이터 유지 덮어설치, 양방향 전환과 전면 실제 QR 왕복으로 검증했다.

## A 실제 수동 검증

2026-07-23에 다음을 확인했다.

1. 관리자 PIN 설정·재인증과 화면 이탈 시 재잠금: PASS
2. 합성 `GATE4-TEST` 반과 시험학생의 기기 내 암호화 등록·QR 발급: PASS
3. 후면 실제 QR 인식과 `kiosk` → `webpoc` 실행: PASS
4. 시험계정 로그인 → 정확한 표시명 확인 → 실제 문제 화면 → `채점 끝내기` → 로그아웃 → `QR_READY`: PASS
5. QR 재발급 뒤 이전 카드 거부 및 신 QR 정상 로그인: PASS
6. 구 QR과 신 QR 동시 노출 시 `QR카드는 한 장만 보여주세요`, Web 미실행, 자동 `QR_READY`: PASS
7. 별도 `OUTSIDE-TEST` 반 QR을 활성 `GATE4-TEST` 수업에서 거부, Web 미실행, 자동 `QR_READY`: PASS
8. 현재 반의 합성 `WEBFAIL` QR로 Web 로그인 실패 주입: `WEB_SESSION_NOT_CLEAN` + `LOCKED`, 다음 QR 차단: PASS
9. QR 대기/민감 상태에서 앱 종료 시 `RECOVERY_REQUIRED` 또는 `LOCKED`, 관리자 안전 종료 뒤 복구: PASS
10. `0.4.0-alpha03` 전면 기본, 후면 전환, 전면 복귀와 전면 실제 QR 전체 왕복: PASS
11. `0.4.0-alpha04` 표시명·마스킹 이름 및 자격정보 갱신, fresh IV 재암호화: PASS
12. 합성 학생 3명 비활성화, 활성 목록 제외와 기존 QR 폐기: PASS
13. 인쇄 경고 → Android 인쇄 미리보기 ISO A4 1페이지 → 앱 복귀 즉시 QR 화면 제거: PASS
14. 새 시험 QR 재발급, 이전 QR 폐기, 새 QR로 로그인·학생 확인·문제 화면·로그아웃 전체 왕복: PASS

시험 뒤 합성 `OUTSIDE`, `WEBFAIL`, `PRINT-CHECK` 학생을 비활성화했다. 시험계정의 새 QR은 사용자가 별도 기기로 촬영해 유지했고, 기존 분실 QR은 재발급으로 폐기했다. 의도적으로 남긴 Web POC 잠금 상태를 관리자 안전 확인으로 복구한 뒤 같은 새 QR의 전체 왕복을 통과했다. A는 `GATE4-TEST` 수업의 `QR_READY`, 전면 카메라 상태로 남겼다. USB 연결 중 화면 켜짐 전역 설정은 시험 전 값 `0`으로 복구했다.

## 남은 후속 검증

- 통제된 실제 프린터의 카드 출력·대기열 정리·분실 카드 폐기 운영 검증
- 실제 학생 2명 → 한 타임 6~7명 → 전체 20명 단계 파일럿
- QR 인식·로그인·로그아웃 median/p95와 3시간 장시간 시험
- 다른 기기 모델의 전면 카메라 초점·조명 성능
- 단일 release 패키징·release signing
- Gate 5 Device Owner/Lock Task

## 공식 기술 기준

- Android Keystore: <https://developer.android.com/privacy-and-security/keystore>
- Room 2.8.4: <https://developer.android.com/jetpack/androidx/releases/room>
- CameraX stable releases: <https://developer.android.com/jetpack/androidx/releases/camera>
- bundled ML Kit Barcode Scanning: <https://developers.google.com/ml-kit/vision/barcode-scanning/android>
