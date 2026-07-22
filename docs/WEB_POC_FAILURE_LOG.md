# Web POC 실패주입 기록

개인정보·자격정보·화면 내용 없이 시험 ID, 시각, 주입 조건과 비민감 상태 결과만 기록한다.

| 시험 | 검증 시각 (Asia/Seoul) | 주입 | 결과 | 판정 |
|---|---|---|---|---|
| W03 | 2026-07-22 11:07:08 | 정상 표시명·아이디, 잘못된 비밀번호, 제출 1회 | `LOCKED / LOGIN_NOT_VERIFIED`; 자동 재시도·학생 화면 진입 없음 | PASS |
| W03 반복 | 2026-07-22 11:24:08 | 로그인 DOM 안정화 수정 뒤 동일 조건 재시험 | `LOCKED / LOGIN_NOT_VERIFIED`; 안전 복구 뒤 11:24:36 `IDLE`, 사유 없음 | PASS |
| W04 | 2026-07-22 | 실제 계정과 다른 예상 표시명, 정상 아이디·비밀번호 | 학습 허용 전 자동 로그아웃, `LOCKED / STUDENT_MISMATCH`; 안전 복구 뒤 안정 `IDLE` | PASS |
| W05 | 2026-07-22 | `ACTIVE`에서 Wi-Fi 차단 뒤 `채점 끝내기` | `LOCKED / NETWORK_ERROR`; Wi-Fi 재활성화와 안전 복구 뒤 안정 `IDLE` | PASS |
| W06 | 2026-07-22 | 실제 로그인 제출 상태 `LOGIN_SUBMIT`에서 POC 강제 종료·재실행 | `RECOVERY_REQUIRED → LOGOUT_VERIFY → IDLE`; 자동 재로그인 없음 | PASS |
| W07 | 2026-07-22 | 실제 `ACTIVE`에서 POC 강제 종료·재실행 | `RECOVERY_REQUIRED → LOGOUT_NAVIGATE → LOGOUT_SUBMIT → LOGOUT_VERIFY → IDLE` | PASS |
| W08 | 2026-07-22 | 실제 `LOGOUT_NAVIGATE`에서 POC 강제 종료·재실행 | 미완료 전환을 신뢰하지 않고 복구 로그아웃 재검증 뒤 `IDLE` | PASS |
| W09 | 2026-07-22 | 실제 `ACTIVE`에서 화면 off/on과 background/foreground | 잠금 해제·재전면화 뒤 웹 화면 유지, 내부 `ACTIVE`, 자격정보 입력 화면 재노출 없음 | PASS |
| W10 | 2026-07-22 | 단일 Web POC 격리 렌더러 PID에 Activity Manager VM crash | `LOCKED / WEB_PROCESS_GONE`; 안전 복구 뒤 안정 `IDLE` | PASS |
| W15 | 2026-07-22 13:23:50 | A 태블릿 전체 재부팅·잠금 해제·Web POC 실행 | 부팅 경과 359초, 앱 실행 중, `IDLE`, 종료 사유 없음 | PASS |

W03 후속 복구에서 명시적 안전 확인 2회가 `MAINTENANCE_REQUIRED / LOGIN_FINGERPRINT`로 실패 폐쇄됐다. 진단판의 2026-07-22 11:10:37 `IDLE`은 이후 사용자가 실제로는 운영상 안정 복구가 아니었다고 정정했다. 다음 잘못된 비밀번호 시험에서 11:13:21 `LOGIN_FINGERPRINT_U0_P0_C0_B0_F0_A0_E00_R0_V1`이 재현되어, 계약 스크립트는 실행됐지만 전환 중인 빈 로그인 DOM을 읽은 것으로 판별했다. 현재 화면을 그대로 평가하지 않고 로그인 URL을 새로 로드한 뒤 800 ms 간격으로 동일한 유효 fingerprint를 2회 확인해야 제출·복구를 진행하도록 수정했다. 수정 뒤 W03 반복과 2초 안정 복구가 통과했으며 자격정보 재사용이나 학생 화면 진입은 없었다.

W05의 첫 `LOGIN_SUBMIT` 동시 주입 시도는 60초 감시 구간 안에 해당 전환을 포착하지 못해 Wi-Fi를 끄지 않았고 시험으로 계상하지 않았다. 당시 최종 상태는 정상 `ACTIVE`였다. 같은 세션에서 Wi-Fi를 먼저 차단하고 다음 필수 네트워크 이동인 `채점 끝내기`를 수행하는 방식으로 다시 주입해 위 PASS 결과를 얻었다.

W10에서 shell `kill -9`은 Android 격리 프로세스 권한으로 거부되어 주입으로 계상하지 않았다. Android Activity Manager가 공식 지원하는 `crash <PID>`를 사용했고, 당시 WebView 격리 렌더러가 정확히 하나임을 확인한 뒤 그 PID만 대상으로 했다.
