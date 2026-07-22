# Web POC 설계

## 범위

`webpoc`은 Web Gate 1 승인 뒤 만든 시험계정 단일 POC다. 기존 Android 접근성 `poc` 모듈은 Gate 1 FAIL 잠금 상태로 보존한다. QR, 실제 학생 DB, 외부 알림, 관리자 PIN, Device Owner와 생산 배포는 포함하지 않는다.

## 공식 웹 경계

상위 문서 탐색은 다음 HTTPS host만 허용한다.

- `login.matholic.com`
- `auth.matholic.com`
- `im.matholic.com`

HTTP, 사용자정보가 포함된 URL, 비표준 port와 다른 host로의 상위 탐색은 잠금 처리한다. 로그인·학습 페이지가 사용하는 CDN·글꼴 같은 하위 resource는 사이트 정상동작을 위해 WebView가 처리하지만, 외부 링크를 상위 화면으로 열지는 않는다. TLS 오류, Safe Browsing 경고, 주 프레임 네트워크/HTTP 오류와 WebView renderer 종료는 실패 폐쇄한다.

## 자격정보

- 태블릿 POC 화면에서 시험계정 표시명·아이디·비밀번호를 매 실행 입력한다.
- View state, SharedPreferences, 파일, 로그, saved instance state와 autofill에 저장하지 않는다.
- 아이디·비밀번호는 제출 직후 보유한 `CharArray`를 0으로 덮어쓰고 참조를 제거한다.
- WebView debugging, console 전달, 디스크 cache, form data 저장, backup, 화면 캡처와 최근 앱 미리보기를 차단한다.
- JVM·WebView 내부에 전송 과정의 일시적 문자열 복사본이 생길 수 있다는 한계는 남는다.

## DOM 계약

로그인 fingerprint `web-2026-07-21.1`:

- `input[name=username]` 정확히 1개
- `input[name=password][type=password]` 정확히 1개
- checkbox 정확히 1개
- `button[type=submit]` 정확히 1개
- form 정확히 1개
- form action은 `https://auth.matholic.com/token/signin`

로그인 페이지가 로드될 때마다 native input setter와 input/change event로 두 입력값을 비우고 checkbox를 해제한 뒤 실제 상태를 다시 읽는다. 자동 입력도 같은 selector와 form submit 의미를 사용한다. 좌표·bounds·OCR·이미지 템플릿은 사용하지 않는다.

학습 fingerprint의 링크는 현재 `im.matholic.com`과 같은 origin인 경우에만 인정한다.

학습 fingerprint:

- `/course` 링크 1개 이상
- `/userInfo` 링크 정확히 1개
- `/userAccessLog` 링크 정확히 1개
- 두 계정 링크의 공통 submenu와 그 형제인 닉네임·SVG trigger
- 닉네임이 비어 있지 않음

표시명은 Unicode NFKC, trim, 연속 공백 축약만 적용한 뒤 완전 일치한다. 불일치 시 학습을 허용하지 않고 안전 로그아웃을 시도한 뒤 잠근다.

로그아웃은 `/course`로 상태 기반 복귀하고 위 구조로 계정 메뉴를 연 뒤, 정확한 텍스트가 `로그아웃`인 최하위 요소가 정확히 하나일 때만 실행한다. 실제 A의 메뉴는 DOM trigger click 뒤에도 시각 표시 상태가 바뀌지 않았지만 검증된 submenu 안의 유일한 부모·자식 텍스트 구조는 유지됐다. 따라서 표시 요소가 없고 최하위 exact 요소가 정확히 하나인 경우에 한해 그 요소의 bubbling click을 허용한다. 중복·부재는 계속 실패 폐쇄하며 1회 실패 시 한 번만 재시도한다. 로그인 화면 fingerprint, 빈 입력값, checkbox 해제까지 확인한 다음 Cookie/WebStorage/form data/cache를 삭제하고 새 로그인 페이지에서 같은 검증을 다시 통과해야 다음 시험을 허용한다.

## 재시작 복구

비민감 상태 enum과 일반화된 종료 사유 코드만 SharedPreferences에 동기 기록한다. 로그인 제출, ACTIVE, 로그아웃 등 민감 상태에서 프로세스가 끝난 뒤 재시작하면 학습 주소를 열어 인증 상태를 확인한다. 인증 상태면 안전 로그아웃하고, 로그인 상태면 입력 정리와 WebView session 삭제를 수행한다. 실제 안전 상태를 검증하지 못하면 `LOCKED`다. 종료 상태에 도달한 뒤 늦게 도착한 WebView callback은 무시하며, 로그인 준비와 session 삭제 callback에도 독립 timeout을 둔다. 복구 버튼은 새 Activity/WebView를 만든 뒤 상태 검증을 다시 시작할 뿐 PIN이나 버튼만으로 잠금을 해제하지 않는다.

## Gate 3 확장

0.2.0은 기존 단일계정 흐름을 보존하면서 시험계정 A·B를 프로세스 메모리에만 두는 100회 교차 runner를 추가한다. 계정 슬롯은 성공한 cycle 수의 parity로 결정해 A/B를 정확히 교대한다. 표시명 검증과 로그아웃 뒤 빈 로그인 화면·session 삭제 재검증까지 끝나야 완료 회수를 증가시킨다. 중단·terminal 실패·Activity 종료 시 두 계정의 메모리 배열을 덮어쓰며 자동 재개하지 않는다. preferences에는 `RUNNING/PASSED/FAILED/ABORTED`, 완료·목표 회수와 소요시간만 저장한다. 0.2.1은 시도 1의 연속 로그인 timeout 뒤 인증 요청 부담을 낮추기 위해 검증 완료 cycle 사이 휴지시간을 0.75초에서 5초로 늘렸다. 같은 `LOGIN_TIMEOUT`이 시도 2에서도 재현된 뒤 0.2.2는 기존 단일계정과 Gate 3 로그인 페이지 준비 제한 30초를 유지하고, Gate 3의 로그인 제출 후 결과 확인 제한만 60초로 분리한다. 표시명 검증, 자동 재시도 0과 실패 폐쇄 기준은 바꾸지 않는다.

0.2.3은 Activity가 가시성을 잃는 즉시 여섯 입력란을 비운다. 진행 중인 Gate 3 또는 단일계정 민감 상태에서 `onStop`이 발생하면 Gate 3는 `ABORTED`로 기록하고 런타임 자격정보를 지운 뒤 recovery 로그아웃·session 정리를 시작한다. 정상 A/B 선택, 로그인과 검증 로직은 변경하지 않는다.

0.2.4는 재시작 시 저장된 `MAINTENANCE_REQUIRED`를 `LOCKED`로 축약하던 상태 의미 손실을 수정한다. 기존 잠금은 `LOCKED / PREVIOUS_LOCK`, 유지보수 상태는 `MAINTENANCE_REQUIRED / PREVIOUS_MAINTENANCE`로 실패 폐쇄를 유지한다.

0.2.5는 재부팅 직후 preflight가 `IDLE`을 확인한 뒤 이전 메인 프레임 요청의 늦은 network/HTTP callback이 도착해 다시 잠글 수 있던 경합을 수정한다. WebView가 숨겨지고 다음 인증에서 로그인 URL을 다시 검증하는 `IDLE`과 이미 terminal인 상태에서는 늦은 callback을 무시한다. PREFLIGHT·로그인·ACTIVE·로그아웃·recovery 등 WebView 동작 중 메인 프레임 오류는 계속 실패 폐쇄한다.

0.2.6은 WebView의 상세 오류 메시지나 URL을 저장하지 않으면서 메인 프레임 transport 실패를 `NETWORK_DNS`, `NETWORK_CONNECT`, `NETWORK_TIMEOUT`, `NETWORK_RATE_LIMIT`, `NETWORK_TLS_HANDSHAKE`, 기타 `NETWORK_ERROR`로만 분류한다. 실패 폐쇄 동작은 바꾸지 않는다.

0.2.7은 A 기기에서 일반 앱 DNS·HTTPS는 정상이지만 JavaScript WebView의 첫 로그인 호스트 조회만 실패하고 지연 재로드는 성공하는 공급자 동작을 확인한 결과를 반영한다. 자격정보 입력 전 `PREFLIGHT`의 메인 프레임 `NETWORK_DNS_LOGIN`에만 1회, 3.5초 지연 재로드를 허용한다. 두 번째 실패, 다른 호스트·오류·상태는 즉시 기존 실패 폐쇄 경로를 따른다.

0.2.8은 A 기기의 WebView 150 공급자가 생성 직후 네트워크를 사용할 때 빈 오류 문서를 반환하지만, 네트워크 요청 없이 10초간 초기화한 뒤에는 실제 로그인 DOM을 로드하는 현상을 반영한다. 새 Activity의 최초 `PREFLIGHT`에서만 10초 워밍업한 뒤 20초 페이지 제한시간 안에서 로그인 페이지를 연다. 워밍업 뒤에도 발생하는 로그인 호스트 DNS 실패에는 0.2.7의 단 1회 재시도와 실패 폐쇄를 유지한다.

0.2.9는 로그인 호스트 DNS 재시도가 예약된 뒤 WebView가 오류 문서에도 보낸 `onPageFinished`가 빈 DOM 지문 검사와 유지보수 상태를 먼저 유발하던 경합을 막는다. 재시도가 아직 시작되지 않은 `PREFLIGHT`에서만 해당 완료 신호를 무시하며, 재시도 시작 뒤의 정상 완료 및 두 번째 오류 처리는 기존 경로를 따른다.

0.3.0은 A의 WebView 150에서만 지속되는 DNS 실패를 우회하기 위해 프로세스 전용 공식 WebView 프록시 override와 루프백 CONNECT 터널을 사용한다. 터널은 임의의 `127.0.0.1` 포트에만 열리고, `*.matholic.com:443`, Google Tag Manager와 네이버 WCS의 HTTPS만 허용하며 TLS를 종료·복호화·기록하지 않는다. 목적지 이름 해석과 TCP 연결만 정상 동작하는 앱 Java 네트워크 계층이 수행한다. 프록시 기능 미지원·설정 실패 또는 터널 연결 실패는 잠금으로 종료한다. 프록시 적용 완료 콜백 전에는 WebView 요청을 시작하지 않는다.

0.3.1은 A의 공급자에서 이미 생성된 WebView에 프록시를 적용하면 메인 프레임이 timeout 되는 초기화 순서 문제를 피한다. 프로세스 프록시 적용 완료 뒤에만 Activity 레이아웃과 WebView를 생성한다. 콜백 전 Activity가 종료돼도 초기화되지 않은 UI를 참조하지 않도록 lifecycle 종료 경로를 함께 방어한다.

0.3.2는 A의 WebView 150에서 프록시 적용 완료 콜백 직후 생성한 WebView에도 timeout이 발생하지만, 콜백이 끝난 뒤 별도 Activity를 시작한 진단은 통과한 차이를 반영한다. 적용 완료 뒤 2초 안정화한 다음 WebView를 생성하며 그 전에는 네트워크 요청이나 UI 입력을 시작하지 않는다.

## 알려진 비생산 한계

- 일반 앱 WebView POC이므로 홈·최근 앱·시스템 UI를 완전히 차단하지 않는다.
- QR·학생 DB가 없어 시험 자격정보를 수동 입력한다.
- Gate 2는 실제 정상 cycle 20/20과 필수 실패주입을 통과했다. Gate 3는 실제 두 계정 100회와 실패주입 전까지 PASS가 아니다.
- 매쓰홀릭의 DOM 또는 인증 흐름이 바뀌면 fingerprint 불일치로 중단한다.
