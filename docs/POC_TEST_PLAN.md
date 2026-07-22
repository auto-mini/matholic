# Android 접근성 Gate 2 POC 보류 시험계획

## 상태

이 문서는 기존 Android 접근성 `poc` 모듈 전용이다. 해당 Gate 2는 잠겨 있다. Gate 1 실기 결과가 최종 FAIL이므로 로그인·로그아웃 자동화를 구현하거나 실행하지 않는다. 현재 `poc` 모듈은 잠금 안내만 표시하고 승인 상수는 `false`다. 이 문서는 공급사가 필수 접근성 의미노드를 제공해 Gate 1을 새로 PASS한 경우에만 사용할 보류 계획이다. 별도로 승인·구현된 공식 웹 경로 시험은 `WEB_POC_TEST_PLAN.md`를 따른다.

## 범위

승인 후에도 시험계정 한 개만 사용한다. QR, 학생 DB, 반 관리, 외부 알림, Device Owner와 생산 APK는 제외한다. 자격정보는 태블릿에서 런타임 입력해 메모리에만 유지하고 파일, preference, 로그, fixture에 저장하지 않는다.

## 정상 흐름

`PRECHECK → QR_READY(시험 시작) → LOGIN_PREP → LOGIN_SUBMITTING → VERIFYING_STUDENT → STUDENT_ACTIVE → LOGOUT_REQUESTED → VERIFYING_LOGOUT → QR_READY`

알 수 없는 상태, 전환 중 잔류 세션, 예상 학생 불일치는 즉시 `LOCKED`다. 시작 전 package/version/fingerprint 불일치는 `MAINTENANCE_REQUIRED`다. 로그인 실패 시 자동 재시도하지 않는다.

## 시험표

| ID | 시험 | 기대 결과 |
|---|---|---|
| P01 | 정상 전 과정 20회 연속 | 20/20 성공, 오계정·세션 잔류 0 |
| P02 | 기억하기가 체크된 초기 상태 | 해제 검증 후에만 입력 진행 |
| P03 | 잘못된 비밀번호 1회 | 실패 인식, 자동 재시도 0, 안전상태 복귀/잠금 |
| P04 | 예상 표시명 불일치 | `LOCKED`, 학생 사용 허용 0 |
| P05 | Wi-Fi 단절(제출 전/후) | 시간 제한 후 `LOCKED`, 다음 로그인 0 |
| P06 | 매쓰홀릭 force-stop/relaunch | 비로그인 유지 여부를 ADB 시험으로 확인 |
| P07 | POC 프로세스 종료 | 복구 시 미완료 전환을 신뢰하지 않고 재검증 |
| P08 | 각 상태전이 중 프로세스 종료 | 잔류 세션이면 `LOCKED` |
| P09 | 화면 끄기/켜기 | 학생 상태·세션을 다시 검증 |
| P10 | 기기 재부팅 | 자동 로그인 금지, preflight부터 재시작 |
| P11 | 접근성 서비스 해제 | 동작 중단과 관리자 복구 안내 |
| P12 | overlay 사용 불가/제거 | 동작 중단, 학생을 활성 상태로 두지 않음 |
| P13 | 같은 앱 버전의 selector 변화 fixture | fingerprint 불일치 탐지 |
| P14 | 매쓰홀릭 업데이트/version 불일치 | `MAINTENANCE_REQUIRED` |

## PASS 기준

- 정상 20회 연속 성공
- 잘못된 성공 판정, 오계정 허용, 세션 잔류 뒤 다음 로그인 모두 0
- 좌표 selector, 자격정보 로그와 로그인 자동 재시도 모두 0
- 프로세스/기기 재시작 뒤 안전 복구
- K1 범위의 잠금 우회 시험 결과를 사실대로 기록

K1 일반 앱 수준에서 시스템 UI와 프로세스 종료를 완전히 차단할 수 없으므로 이를 완전한 키오스크 PASS로 표현하지 않는다. 생산 요구는 별도 K2 Device Owner/Lock Task 실기 검증이 필요하다.

## 증거

실행별 UTC 시각, 상태전이, 결과코드, redacted selector fingerprint, 소요시간만 기록한다. 자격정보, 학생 실명, QR 원문, 화면 원문과 logcat은 기록하지 않는다. 실패 항목은 재현절차와 실제 결과를 포함하며 성공으로 합산하지 않는다.
