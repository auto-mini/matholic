# Web Gate 3 남은 실계정 시험

사람 손 없이 가능한 검증은 완료했다. 최종 0.3.2 정상 회귀도 완료했으며, 아래 G302~G305는 자격정보를 A 태블릿 화면에 다시 입력해야 하므로 미수행이다. 자격정보를 채팅, 파일, ADB와 로그로 전달하지 않는다.

## 최종 0.3.2 정상 회귀 — 완료

2026-07-23 최종 0.3.2에서 정상 A·B 계정으로 100/100(A·B 각 50회)을 완료했다. 결과는 `PASSED / IDLE / 사유 없음`, 소요시간 1,653,263 ms였고 앱 프로세스 재시작 뒤에도 동일 결과와 비민감 저장 키 5개만 유지됐다.

## G302 — 표시명 교차 매핑

1. A 슬롯에는 A의 아이디·비밀번호와 **B의 실제 표시명**을 입력한다.
2. B 슬롯에는 B의 아이디·비밀번호와 **A의 실제 표시명**을 입력한다.
3. `watch-gate3-outcome.ps1 -ExpectedStatus FAILED -ExpectedReason STUDENT_MISMATCH -ExpectedCompleted 0`을 먼저 실행한다.
4. 교차시험을 시작한다.

기대값은 A 로그인 1회 뒤 `STUDENT_MISMATCH`, 완료 0, B 로그인 0이다.

## G303 — 과거 비밀번호

1. A 슬롯에는 A의 실제 표시명·아이디와 더 이상 유효하지 않은 과거 비밀번호를 입력한다.
2. B 슬롯에는 정상 B 시험계정을 입력한다.
3. `watch-gate3-outcome.ps1 -ExpectedStatus FAILED -ExpectedCompleted 0`을 먼저 실행한다.
4. 교차시험을 시작한다.

기대값은 A 제출 1회, 로그인 자동 재시도 0, 완료 0, B 로그인 0이다. 실제 종료 사유는 서버 응답 형태에 따라 `LOGIN_NOT_VERIFIED`, `LOGIN_TIMEOUT` 또는 제한된 fingerprint 사유일 수 있어 실행 증거로 확정한다.

## G304·G305 — 실제 session과 프로세스 종료

각 시험마다 정상 A·B 계정을 다시 입력한다. Codex가 아래 도구를 먼저 실행한 뒤 사용자는 시작만 누른다.

- 로그인 제출 중 종료: `stop-gate3-at-state.ps1 -AtState LOGIN_SUBMIT`
- 실제 학생 확인 뒤 종료: `stop-gate3-at-state.ps1 -AtState ACTIVE`
- 로그아웃 제출 중 종료: `stop-gate3-at-state.ps1 -AtState LOGOUT_SUBMIT`

기대값은 해당 순간 프로세스 종료, Gate 3 `ABORTED`, 완료 회수 불변, 자동 재개 0, recovery 전 다음 계정 로그인 0이다.

## 완료 경계

G301은 0.2.2와 최종 0.3.2에서, G306·G307·G308은 실제 기기 시험으로 통과했다. G304·G305의 비자격정보 상태 계약도 통과했지만 G302·G303과 실계정 session 기반 G304·G305 전에는 전체 Gate 3 PASS로 판정하지 않는다.
