# 위협 모델

## 범위와 자산

현재 범위는 Gate 0 저장소, Gate 1 접근성 Probe와 승인된 시험계정 단일 Web POC다. 보호 대상은 시험계정 자격정보, 학생 식별정보, 매쓰홀릭 세션, QR 원문, 조사 보고서와 태블릿의 다른 앱 데이터다. 실제 학생 DB, QR 처리, 외부 알림, Device Owner와 생산 자동화는 범위 밖이다.

## 신뢰 경계

- Probe와 매쓰홀릭은 서로 다른 Android 앱/UID다.
- Probe는 Android 접근성 서비스가 제공하는 `com.matholic.mathapp` 창만 읽는다.
- redacted 보고서는 앱 private storage에 먼저 저장되며, 사용자가 Storage Access Framework로 선택한 위치에만 내보낸다.
- PC/ADB는 설치와 비민감 기기 기준정보 확인에만 사용한다. 계정, 화면 원문, 로그캣, `uiautomator dump`를 수집하지 않는다.
- Web POC의 WebView는 별도 앱 UID 안에서 공식 웹에 접속한다. 상위 탐색은 승인된 세 host만 허용하고 다른 링크, TLS 오류와 Safe Browsing 경고는 실패 폐쇄한다.
- 사용자 확인에 따르면 공급사에 자동화 사용 가능 여부를 문의해 허용 답변을 받았다. 답변 원본은 별도로 보관하는 것이 좋다.

## 위협과 통제

| 위협 | 현재 통제 | 잔여위험 |
|---|---|---|
| 자격정보가 소스·Git·로그에 남음 | 런타임 직접 입력, 비밀 파일 ignore, 로그 호출 금지, editable/password 즉시 마스킹 | 대화에 노출된 이전 시험 비밀번호는 변경해 무효화 완료. 향후 노출도 즉시 변경 필요 |
| 다른 앱의 노드 수집 | 서비스 XML package 제한 + 런타임 exact package 검사 | OS/대상 앱 결함은 통제 밖 |
| 학생명 등 접근성 text 유출 | 고정 UI 문구 allowlist 외 길이+세션 salt fingerprint로 즉시 치환 | 길이와 동일 세션 내 동일성은 진단 목적으로 남음 |
| 보고서 외부 유출 | 앱 private storage, 명시적 내보내기, 인터넷 권한 없음 | 사용자가 내보낸 파일의 이후 취급은 별도 통제 필요 |
| ADB 캡처 요청의 외부 악용 | 동적 receiver를 `RECEIVER_NOT_EXPORTED`로 등록하고 debug 앱 UID의 `run-as` 요청만 사용 | USB 디버깅이 허용된 신뢰 PC는 redacted 캡처를 유발할 수 있음 |
| 스크린샷/최근 앱 미리보기 | Probe Activity에 `FLAG_SECURE` | 매쓰홀릭 자체 화면은 Probe가 통제하지 못함 |
| UI 변경으로 오계정/오동작 | version과 의미 기반 fingerprint를 함께 요구, 알 수 없는 상태는 중단 | 같은 버전의 서버 UI 변경 가능 |
| 좌표 오작동 | bounds는 진단 전용, selector 좌표 사용 금지 | 의미 노드가 없으면 Gate 1 FAIL |
| 세션 잔류 후 다음 학생 로그인 | 향후 상태 머신에서 로그아웃 및 빈 로그인 화면 검증 전 다음 로그인 금지 | Gate 1 Probe는 자동화를 수행하지 않음 |
| 일반 앱 키오스크 우회 | K1 한계를 명시하고 생산판은 검증된 Device Owner/Lock Task 요구 | Gate 1/2는 완전한 키오스크가 아님 |
| 외부 알림에 개인정보 전송 | 사용자 결정에 따라 외부 알림 기능을 현재 계획에서 제거 | 로컬 화면/소리/audit 설계는 이후 Gate 대상 |
| Web 로그인 값·token 잔류 | 매 페이지 입력 정리, autofill/form/cache 저장 차단, 로그아웃 뒤 Cookie/WebStorage/form/cache 삭제와 재검증 | JVM/WebView 메모리의 일시적 문자열과 인증 중 token은 완전 제거를 증명할 수 없음 |
| 공식 웹 구조 변경으로 오동작 | login/portal DOM fingerprint와 selector 개수 검사, 불일치 시 `MAINTENANCE_REQUIRED` | 서버가 같은 구조로 의미만 바꾸는 경우는 반복 실기 필요 |
| 다른 학생으로 로그인 | 공식 웹 닉네임을 예상 표시명과 NFKC·공백 정규화 후 완전 일치, 불일치 시 즉시 로그아웃 후 잠금 | 동명이인은 매쓰홀릭 표시명 자체를 구분해야 함 |
| Web POC 외부 탐색 | HTTPS host·port·userinfo allowlist, 외부 상위 탐색과 download 차단 | 허용 사이트의 제3자 하위 resource는 사이트 정상동작을 위해 로드될 수 있음 |

## 실패 시 원칙

package, version, 접근성 fingerprint 또는 로그인 상태가 예상과 다르면 진행하지 않는다. 전환 중 잔류 세션이나 알 수 없는 화면은 `LOCKED`, 시작 전 version/fingerprint 불일치는 `MAINTENANCE_REQUIRED`로 취급한다. 자동 재로그인은 하지 않는다.

## 대응

민감값이 파일에 남은 경우 공유를 중단하고 자격정보를 변경한다. 원문 보고서는 만들지 않으므로 삭제 대상은 내보낸 redacted 자료와 앱 private report다. 공유 Git 이력의 파괴적 재작성은 별도 승인 후 수행한다.
