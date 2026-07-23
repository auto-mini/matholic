# Web Gate 3 남은 실계정 시험

사람 손 없이 가능한 검증, 최종 0.3.2 정상 회귀와 G302~G305 실계정 실패주입은 완료했다. G303 결함 수정으로 새 최종 빌드가 0.3.3이 됐으므로 이 빌드 자체의 정상 100회 회귀만 남았다. 자격정보를 채팅, 파일, ADB와 로그로 전달하지 않는다.

## 최종 0.3.3 정상 회귀 — 미완료

정상 A·B 계정으로 100/100(A·B 각 50회)을 수행한다. 기대값은 `PASSED / IDLE / 사유 없음`, 자격정보 저장 키 0이며 앱 프로세스 재시작 뒤에도 동일 결과를 유지해야 한다.

## 최종 0.3.2 정상 회귀 — 완료

2026-07-23 최종 0.3.2에서 정상 A·B 계정으로 100/100(A·B 각 50회)을 완료했다. 결과는 `PASSED / IDLE / 사유 없음`, 소요시간 1,653,263 ms였고 앱 프로세스 재시작 뒤에도 동일 결과와 비민감 저장 키 5개만 유지됐다.

## G302 — 표시명 교차 매핑 — 완료

1. A 슬롯에는 A의 아이디·비밀번호와 **B의 실제 표시명**을 입력한다.
2. B 슬롯에는 B의 아이디·비밀번호와 **A의 실제 표시명**을 입력한다.
3. `watch-gate3-outcome.ps1 -ExpectedStatus FAILED -ExpectedReason STUDENT_MISMATCH -ExpectedCompleted 0`을 먼저 실행한다.
4. 교차시험을 시작한다.

2026-07-23 A 슬롯의 기대 표시명을 B로, B 슬롯의 기대 표시명을 A로 교차해 실행했다. A 로그인 1회 뒤 `FAILED / LOCKED / STUDENT_MISMATCH`, 완료 0/100으로 종료됐고 B 로그인은 실행되지 않았다. 3초 뒤에도 상태가 고정됐으며 저장 키는 비민감 결과 6개뿐이었다. 따라서 G302는 PASS다.

## G303 — 과거 비밀번호 — 완료

1. A 슬롯에는 A의 실제 표시명·아이디와 더 이상 유효하지 않은 과거 비밀번호를 입력한다.
2. B 슬롯에는 정상 B 시험계정을 입력한다.
3. `watch-gate3-outcome.ps1 -ExpectedStatus FAILED -ExpectedCompleted 0`을 먼저 실행한다.
4. 교차시험을 시작한다.

0.3.2 첫 시험은 `LOCKED / LOGIN_NOT_VERIFIED`, 완료 0, B 로그인 0으로 안전 차단됐지만 Gate 3 status가 `RUNNING`으로 남는 결함을 발견해 PASS로 계상하지 않았다. 0.3.3에서 실패 결과를 먼저 저장하도록 수정하고 전체 build 150/150·A 계측 27/27 뒤 같은 조건으로 재시험했다. 재시험은 6,137 ms 뒤 `FAILED / LOCKED / LOGIN_NOT_VERIFIED`, 완료 0/100, B 로그인 0으로 종료됐고 3초 뒤에도 고정돼 G303은 PASS다.

## G304·G305 — 실제 session과 프로세스 종료 — 완료

각 시험마다 정상 A·B 계정을 다시 입력한다. Codex가 아래 도구를 먼저 실행한 뒤 사용자는 시작만 누른다.

- 로그인 제출 중 종료: `stop-gate3-at-state.ps1 -AtState LOGIN_SUBMIT`
- 실제 학생 확인 뒤 종료: `stop-gate3-at-state.ps1 -AtState ACTIVE`
- 로그아웃 제출 중 종료: `stop-gate3-at-state.ps1 -AtState LOGOUT_SUBMIT`

0.3.3에서 다음 세 상태를 실제 정상 계정 session으로 검증했다.

- `LOGIN_SUBMIT`: 종료 전후 완료 0, 재실행 후 `ABORTED / IDLE`
- `ACTIVE`: 종료 전후 완료 4, 재실행 후 실제 session 복구를 거쳐 `ABORTED / IDLE`
- `LOGOUT_SUBMIT`: 종료 전후 완료 0, 재실행 후 `ABORTED / IDLE`

세 시험 모두 자동 재개와 다음 계정 로그인이 없고 오류 로그가 비어 있어 G304·G305는 PASS다.

## 완료 경계

G301은 0.2.2와 0.3.2에서, G302~G308은 실제 계정·기기 시험으로 통과했다. 최종 0.3.3 자체의 정상 100회 회귀 전에는 전체 Gate 3 PASS로 판정하지 않는다.
