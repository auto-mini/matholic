# Gate 1 Accessibility Probe

## 목적과 판정 경계

SM-P610의 매쓰홀릭 7.5.1 화면에서 좌표 없이 로그인, 학생 확인, 로그아웃과 빈 로그인 상태를 식별할 수 있는지 조사한다. Probe는 노드를 캡처할 뿐 클릭·입력·로그인·로그아웃을 자동 실행하지 않는다.

실기 조사는 완료됐고 Gate 1은 최종 FAIL이다. 기억하기 상태와 여러 필수 이동 Action이 접근성 트리에 노출되지 않았다. 상세 증거는 [GATE1_REPORT.md](GATE1_REPORT.md)에 있으며 Gate 2는 잠금 상태다.

## 안전장치

- 대상 package는 XML과 런타임 모두 `com.matholic.mathapp` exact match다.
- raw text는 파일에 쓰기 전에 메모리에서 마스킹한다.
- password/editable text는 항상 `<REDACTED_SENSITIVE ...>`다.
- allowlist 밖의 모든 text/description은 `<REDACTED_TEXT len=... fp=...>`다.
- fingerprint salt는 프로세스 메모리에만 있고 보고서에 저장하지 않는다.
- state/role description도 text와 같은 즉시 마스킹을 적용하고 extras는 key 이름만 수집한다.
- checked tri-state는 AndroidX compat extra가 있으면 이를 우선하고, 없을 때 API 36 platform tri-state 또는 구형 boolean으로 fallback한다. 보고서에 source를 함께 남긴다.
- bounds는 진단용이며 향후 selector로 사용할 수 없다.
- 인터넷, 카메라, 일반 overlay 권한은 요청하지 않는다. UI는 `TYPE_ACCESSIBILITY_OVERLAY`를 사용한다.

## 설치와 활성화

1. PC에서 `scripts/build.ps1`을 실행한다.
2. USB 디버깅이 허용된 태블릿을 연결하고 `scripts/install-probe.ps1`을 실행한다.
3. 태블릿에서 **매쓰홀릭 접근성 조사기**를 연다.
4. 서비스 토글이 제한되면 [DEVICE_SETUP.md](DEVICE_SETUP.md)의 **제한된 설정 허용** 절차를 한 번 수행한다.
5. 앱의 **접근성 설정 열기** → **설치된 앱** → **매쓰홀릭 조사 서비스**를 켠다.
6. 앱 상태가 `접근성 설정: 활성`, `서비스 연결: 연결됨`인지 확인한다.

Android 보안 확인 화면을 ADB로 우회하거나 서비스 설정을 강제로 쓰지 않는다.

## 수집 순서

시험계정 값은 태블릿에서 직접 입력한다. 각 상태가 확실할 때 화면 아래의 **노드 캡처**를 한 번 누른다.

1. 로그아웃 상태의 로그인 화면
2. 아이디 입력란(입력 전 또는 시험값 입력 후)
3. 비밀번호 입력란(입력 전 또는 시험값 입력 후)
4. 로그인 정보 기억하기
5. 로그인 버튼
6. 로그인 후 대시보드 학생 표시명
7. 과제 목록
8. 문제풀이
9. 계정·설정 화면
10. 프로필 팝업
11. 로그아웃 동작 직전
12. 로그아웃 후 로그인 화면과 빈 입력란

여러 항목이 같은 접근성 트리에 있으면 캡처 하나로 함께 증명할 수 있다. 실제 학생 계정, 다른 학생 목록 또는 점수·답안 화면은 수집하지 않는다.

연결된 개발 PC에서는 `scripts/capture-redacted.ps1`로 같은 캡처를 요청할 수 있다. 이 요청은 manifest에 공개된 receiver가 아니라 실행 중 서비스가 `RECEIVER_NOT_EXPORTED`로 등록한 receiver에 Probe 앱 UID(`run-as`)로만 전달된다. 매쓰홀릭에 클릭·텍스트 입력·제스처를 보내지 않으며, exact package와 즉시 마스킹 규칙은 수동 버튼과 동일하다.

## 내보내기와 전달

앱으로 돌아가 **최근 redacted 보고서 내보내기**를 눌러 각 JSON을 지정 폴더에 저장한다. 내보낸 파일은 열어서 다음만 육안 확인한다.

- `rawTextStored`가 `false`
- target package가 정확함
- 입력값/학생 실명이 평문으로 없음
- password/editable 노드가 `REDACTED_SENSITIVE`

민감 평문이 보이면 전달하지 말고 앱의 **redacted 보고서 삭제**를 실행한 뒤 중단한다. raw `uiautomator dump`, 화면 녹화, logcat은 만들거나 전달하지 않는다.

## selector 분석 기록

각 필수 동작에 resource ID, content description, role+라벨, 고유 고정문구+계층, 독립 조건 조합 순으로 후보를 기록한다. 안정성, 언어/버전 의존성, WebView 여부, fallback, 오탐 가능성과 화면 fingerprint를 함께 남긴다.

## PASS 조건

다음이 모두 redacted 실기 자료로 확인되어야 한다.

- 아이디·비밀번호 노드에 `ACTION_SET_TEXT`
- 기억하기 상태를 읽고 해제할 수 있는 action
- 로그인 버튼의 의미 기반 실행
- 실제 표시명 읽기 및 가능하면 두 번째 안정 식별자
- 문제풀이에서 상태 기반 대시보드 복귀
- 계정·프로필·로그아웃 탐지
- 로그아웃 후 로그인 화면과 빈 입력란 확인
- exact package allowlist와 화면 fingerprint
- 좌표 selector 0, 개인정보 평문 0

정확한 표시명 외 안정적인 두 번째 식별자를 얻지 못하면 그 사실을 잔여위험으로 평가하고 생산 단계 진행 여부를 다시 판정한다. 하나라도 좌표만 가능하면 Gate 1은 FAIL이다.
