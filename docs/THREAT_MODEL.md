# 위협 모델

## 범위와 자산

현재 범위는 Gate 0 저장소, Gate 1 접근성 Probe와 승인된 시험계정 단일 Web POC다. 보호 대상은 시험계정 자격정보, 학생 식별정보, 매쓰홀릭 세션, QR 원문, 조사 보고서와 태블릿의 다른 앱 데이터다. 실제 학생 DB, QR 처리, 외부 알림, Device Owner와 생산 자동화는 범위 밖이다.

## 신뢰 경계

- Probe와 매쓰홀릭은 서로 다른 Android 앱/UID다.
- Probe는 Android 접근성 서비스가 제공하는 `com.matholic.mathapp` 창만 읽는다.
- redacted 보고서는 앱 private storage에 먼저 저장되며, 사용자가 Storage Access Framework로 선택한 위치에만 내보낸다.
- PC/ADB는 설치, 비민감 기기 기준정보 확인과 구조화된 비공개 진단 로그
  회수에 사용한다. 기본 상태에서는 계정, 화면 원문과 `uiautomator dump`를
  수집하지 않는다. 사용자가 승인한 시간 제한 원격 점검 중에는 승인 ADB가
  현재 화면 한 장을 PC 로컬 임시 경로로 캡처할 수 있다.
  진단 로그는 `android.permission.DUMP`가 있는 ADB shell만 요청할 수 있고
  학생 식별정보·자격정보·QR·답안·점수·문항 내용을 구조적으로 거부한다.
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
| 비공개 운영 로그의 일반 앱 노출 | release receiver에 시스템 `android.permission.DUMP` 강제, 허용 필드·형식 검사, 파일별 최근 200줄 제한, 사용자 UI·공유 기능 없음 | USB 디버깅을 승인한 PC의 ADB shell은 구조화된 로그를 읽을 수 있으므로 승인 PC를 신뢰 경계로 관리해야 함 |
| 원격 점검 화면의 민감정보 노출 | 기본 `FLAG_SECURE`, 관리자 경고, ADB receiver의 `android.permission.DUMP`, 같은 signer 앱 간 제어, 동일 부팅·최대 2시간 만료, 화면 상태 배지, 종료 시 PC 임시 캡처 삭제 | 점검 중 QR·학생 이름·학습 내용이 승인 PC와 Codex 화면에 보일 수 있으므로 관리자 PIN·비밀번호 입력 중에는 켜지 않고 승인 PC의 ADB 키를 신뢰 경계로 관리해야 함 |
| DHCP 주소 변경 뒤 지정 PC 오인 연결 | 저장 주소 실패 시 현재 Wi-Fi의 RFC1918 주소와 같은 `/24`의 최대 254개 후보만 조사하고, 기존 페어링 키의 challenge-response 인증에 성공한 수신기만 채택한다. 복구 상태에는 학생 이름을 넣지 않고 PC 알림도 만들지 않으며 새 주소는 기존 암호화 저장소에 갱신한다. | 현재 사설 `/24`의 TCP 48129 후보에는 연결 시도가 발생한다. 네트워크가 다른 `/24`로 바뀌었거나 PC·수신기 설정이 교체된 경우에는 자동 복구하지 못하므로 수동 재페어링이 필요하다. |
| 스크린샷/최근 앱 미리보기 | 운영 앱은 기본 `FLAG_SECURE`; 원격 점검 중에만 일시 해제하고 최근 앱 제외는 유지 | 원격 점검을 시작한 승인 PC는 만료 전 현재 화면을 볼 수 있음 |
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
