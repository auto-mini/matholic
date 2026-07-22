# Web POC 시험계획

## 현재 상태

Web Gate 1과 Web POC 구현 승인은 완료됐다. `webpoc`은 시험계정 하나만 사용한다. 단위시험 6개, A 기기 계측시험 15개와 B 기기 계측시험 12개, 공개 로그인 preflight, 오프라인 실패 폐쇄, IDLE 재시작 검증, 실제 시험계정 정상 20회와 필수 실패주입을 통과했다. 정의된 POC 범위의 Web Gate 2는 PASS다.

## 정상 시험

1. 앱 시작 뒤 공식 로그인 fingerprint와 빈 입력값 확인
2. 태블릿에서 예상 표시명·시험 아이디·시험 비밀번호 입력
3. 자동 재시도 없이 form 1회 제출
4. 공식 학습 웹에서 닉네임을 읽어 예상 표시명과 정확히 비교
5. 문제풀이 화면 진입
6. `채점 끝내기`
7. 입력 차단, `/course` 복귀, 계정 메뉴, 로그아웃
8. 로그인 화면·빈 두 입력칸·저장 checkbox 해제 확인
9. WebView session 삭제와 재검증
10. 다음 시험 입력 화면 표시

## 필수 시험

| ID | 시험 | 기대 결과 |
|---|---|---|
| W01 | 정상 20회 연속 | 20/20, 오계정·세션 잔류 0 |
| W02 | 로그아웃 뒤 아이디 재등장 | 페이지 로드 정리 후 empty |
| W03 | 잘못된 비밀번호 | 제출 1회, 자동 재시도 0, `LOCKED` |
| W04 | 예상 표시명 불일치 | 학습 허용 전 로그아웃 후 `LOCKED` |
| W05 | Wi-Fi 단절 | timeout 후 `LOCKED`, 다음 입력 차단 |
| W06 | 로그인 제출 중 POC 종료 | 재시작 시 복구 검사, 안전상태 전 다음 입력 차단 |
| W07 | ACTIVE 중 POC 종료 | 재시작 시 기존 세션 로그아웃 또는 `LOCKED` |
| W08 | 로그아웃 중 POC 종료 | 재시작 시 안전상태 재검증 |
| W09 | 화면 off/on·background/foreground | 세션과 UI 유지, 자격정보 화면 재노출 0 |
| W10 | WebView renderer 종료 | `LOCKED` |
| W11 | 승인 외 상위 링크 | 탐색 차단과 `LOCKED` |
| W12 | login DOM fingerprint fixture 변경 | `MAINTENANCE_REQUIRED` |
| W13 | portal DOM fingerprint fixture 변경 | `MAINTENANCE_REQUIRED` |
| W14 | 로그아웃 컨트롤 탐지 실패 | 1회 재시도 뒤 `LOCKED` |
| W15 | A 기기 전체 재부팅 | 저장 자격정보 0, 앱 실행 뒤 안전상태 검사 |

현재 완료: W01 정상 20/20, W02 실제 cycle 뒤 빈 입력·checkbox 해제 검증, W03 실제 잘못된 비밀번호와 안정 복구, W04 실제 표시명 불일치, W05 실제 `ACTIVE` Wi-Fi 단절, W06 실제 `LOGIN_SUBMIT` 종료, W07 실제 `ACTIVE` 종료, W08 실제 `LOGOUT_NAVIGATE` 종료, W09 실제 화면 off/on·background/foreground, W10 실제 WebView 렌더러 VM crash, W11~W13의 allowlist/fingerprint fixture, W14 실제 최초 실패 폐쇄와 수정 후 복구, W15의 무자격정보·최종 정상 cycle 직후 앱 재시작·A 기기 전체 재부팅. 필수 시험의 미완료 항목은 없다.

## PASS 기준

- 정상 20회 연속 성공
- 오계정 허용, 잘못된 성공판정, 세션 잔류 뒤 다음 입력 모두 0
- 로그인 자동 재시도 0, 로그아웃 재시도 최대 1
- 좌표·bounds selector, 자격정보 로그·파일 저장 0
- 민감 상태의 프로세스 종료 뒤 검증 없는 자동 복귀 0

K1 일반 앱 수준이므로 시스템 전체 키오스크 PASS와 구분한다. Device Owner/Lock Task는 별도 승인 전 수행하지 않는다.
