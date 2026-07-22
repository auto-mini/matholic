# Gate 1 결과 보고서

상태: **최종 FAIL / Gate 2 진입 금지**

## 대상

| 항목 | 기준 | 실측 |
|---|---|---|
| 기기 | SM-P610 / Android 13 / One UI 5.1.1 | 일치 |
| package | `com.matholic.mathapp` | 일치 |
| version | 7.5.1 (`770961`) | 일치 |
| 논리 화면/density/font | 2000×1200 / 240 / 1.1 | 일치 |
| Probe | `com.local.matholickiosk.probe.debug` | 접근성 enabled/bound 확인 |
| Probe version/schema | 0.1.1-gate1-debug / 2 | 일치 |

대체 기기 B(SM-P613 / Android 14 / One UI 6.1 / 1200×2000 / 240 dpi / font scale 1.0)에서도 동일한 매쓰홀릭 7.5.1(`770961`)과 Probe 0.1.1로 기억하기 상태를 단축 재검증했다.

## 수집 무결성

- 유효 로그인 captureId: `3fe962da-d19c-40c7-9d5a-a72cc3d021c5`
- 기기 내부 파일 SHA-256: `0b4ff735e436f0ba4b62dd21828e279abda738736f62442437f5ac008c87943a`
- schema 1, target package/version 일치, APPLICATION window
- node 25개, truncated `false`, rawTextStored `false`, boundsUsedAsSelector `false`
- 비허용 평문 0개. 평문은 고정 UI 문구 `로그인`, `로그인 정보 기억하기`만 존재한다.
- editable/password 값은 비어 있었고 민감값을 입력하거나 PC로 전송하지 않았다.
- 독립 재캡처 `capture_20260721_081621_980_3.json`(SHA-256 `d67c9007b57c8a7d1a5494b5b7d0de831de79c0d0e673fc1a5ddf69487c2e5f0`)에서도 node 25개와 같은 구조 fingerprint가 재현됐다.

첫 capture `ba81cab5-2ace-4174-887e-61192da5d165`은 로딩 중 14개 노드와 클릭 가능한 WebView만 노출했다. 3초 후 같은 화면을 재수집하자 25개 의미 노드가 나타났다. 따라서 운영 fingerprint 검사는 로딩 상태를 즉시 UI 변경으로 판정하지 말고 제한시간 안에서 안정화된 트리를 기다려야 한다. 첫 파일 SHA-256은 `97c39c1080b700c9910f6c6be602cce8743596e9b4ec90a3b9d2d77b61a8e0df`다.

## 화면별 증거

| 화면 | 핵심 노드/Action | 개인정보 평문 | 판정 |
|---|---|---|---|
| 로그인/빈 입력란 | HTML root 아래 EditText 2개, 둘 다 empty | 0 | PASS |
| 아이디 | EditText, password=false, inputType=1, `SET_TEXT` | 0 | PASS |
| 비밀번호 | EditText, password=true, inputType=225, `SET_TEXT` | 0 | PASS |
| 기억하기 | 시각적 체크 전·후 모두 checkable=false, checkedState=FALSE | 0 | **FAIL — 상태 판독 불가** |
| 로그인 버튼 | Button, 고정 text `로그인`, `CLICK` | 0 | PASS |
| 로그인 후 안내 대화상자 | AlertDialog 1개, Button 1개, `CLICK` | 0 | PASS — 사용자가 닫음 |
| 로그인 직후 화면 | TextView 101개를 포함한 187개 노드, `CLICK` 0개 | 0 | **FAIL — 의미 기반 이동 불가** |
| 홈/표시명 | 제목줄 TextView 후보, 길이 3, 프로필 이름과 일치 | 0 | PARTIAL — 의미 속성 없음, 교차계정 미검증 |
| 문제 복귀 후 화면 | EditText 1개와 텍스트 Button 1개만 `CLICK` | 0 | PARTIAL — 구조 안정, 화면 종류 시각 확인 필요 |
| 문제풀이 | Button 13개 모두 `CLICK`; 텍스트 버튼 6개, 무표식 버튼 7개 | 0 | **FAIL — 실제 좌상단 `<` 복귀 아이콘 미노출** |
| 계정·설정 진입 | 좌측 상단 이름을 누르면 프로필 팝업 열림; 이름 노드에 `CLICK` 없음 | 0 | **FAIL — 실제 경로는 발견, 의미 기반 실행 불가** |
| 프로필 팝업 | AlertDialog 1개, 236개 노드, `CLICK` 12개 | 0 | PASS — 구조 안정 |
| 로그아웃 | 고정 text `로그아웃` Button 정확히 1개, `CLICK` | 0 | PASS — 수동 실행 성공 |
| 로그아웃 후 로그인/빈 입력란 | EditText 2개 empty, 로그인 Button 1개, 인증 홈 노드 0개 | 0 | PASS — 재전면화 후에도 유지 |

## 로그인 selector 후보

좌표와 bounds를 제외한 HTML root 하위 10개 노드의 정규화 구조 fingerprint SHA-256은 `D7915EA5FB8CD3D1A8B530C4D3CD559D2F0107A8D16E2399F1715B5769541A3B`다. 세션 salt fingerprint 값과 bounds는 계산에서 제외했다. 서로 다른 두 유효 capture에서 같은 값이 나왔다.

| 동작/검증 | 주 selector | fallback | 안정성/의존성/오탐 |
|---|---|---|---|
| 아이디 입력 | viewId `root`의 direct child 중 EditText + password=false + inputType=1 + `SET_TEXT` | 같은 부모의 password EditText 및 로그인 Button과의 형제 관계 | resource ID/label 없음. 전체 fingerprint 일치가 필수이며 단독 순번 사용 금지 |
| 비밀번호 입력 | EditText + password=true + inputType=225 + `SET_TEXT` | 아이디 EditText와 로그인 Button 사이의 형제 관계 | password flag가 강함. 전체 fingerprint 병행 |
| 기억하기 읽기·해제 | 고정문구 `로그인 정보 기억하기`의 부모 아래 CheckBox + `CLICK` | 없음 | 역할과 클릭은 보이나 상태가 노출되지 않아 selector로 사용할 수 없음 |
| 로그인 실행 | Button + text `로그인` + `CLICK`, 같은 부모에 정확히 1개 | 두 EditText 다음의 유일 Button + 전체 fingerprint | 한국어/서버 UI 의존. 텍스트와 역할을 함께 요구 |
| 로그아웃 상태·빈 필드 | 위 전체 fingerprint + EditText 2개의 empty text + 인증 홈 노드 부재 | 없음 | 실제 로그아웃 직후 및 재전면화 후 확인 |

모든 후보는 exact package와 version check를 선행하고 여러 독립조건을 동시에 만족할 때만 사용한다. 좌표 fallback은 없다.

## 로그인 후 안내 대화상자

로그인 직후 `capture_20260721_083020_145_4.json`과 약 2분 뒤 `capture_20260721_083236_884_5.json`을 수집했다. 각각의 captureId는 `e23f1634-6d91-40a4-898a-9d74d5571455`, `b6cbb938-b8b1-4a68-a434-604b2b95c897`이고 기기 내부 파일 SHA-256은 각각 `7b860f4d217a095893f197ed80b86ab7eb67f96597c232258bb815cebb7072b5`, `9a624549d6ca535da60f8cd9ef99cf50f524fd682f3c020a5ab1c13ed104eaa1`이다.

두 capture 모두 schema 2, node 206개, truncated `false`, rawTextStored `false`, editable 0개, 개인정보 평문 0개였다. 화면에는 AlertDialog 1개와 `CLICK` 가능한 Button 1개가 계속 노출되어 대시보드 상호작용을 막고 있다. bounds와 redacted session fingerprint를 제외하고 Action 이름과 extras key를 정렬한 구조 fingerprint는 두 파일 모두 `AABFEA7F53EA8E8A5BE666394CB178DCC1296616FAE181EB9625479458651545`로 일치했다. 자동 터치나 좌표 입력은 수행하지 않았으며, 사용자가 대화상자를 직접 닫은 후 대시보드 조사를 계속한다.

## 로그인 직후 화면

사용자가 안내 대화상자를 닫은 뒤 `capture_20260721_083702_840_6.json`과 `capture_20260721_083746_357_7.json`을 수집했다. 각각의 captureId는 `39ecd92b-470d-4ca8-bcbe-a5cb8aee9685`, `a232ce88-45d2-49ac-9e7a-78d1dab4ae0e`이고 기기 내부 파일 SHA-256은 각각 `26b71233f8ee909efe7a48c1c9693edf20d6f2f5f2fb7b490c7eaec6b0990282`, `6020e7cbaa1867419a17482d549ea6cb499b3e0440f467d1d1e30ae4bf6bcc86`이다.

두 capture 모두 schema 2, node 187개, truncated `false`, rawTextStored `false`, 개인정보 평문 0개였다. bounds와 redacted session fingerprint를 제외하고 Action 이름과 extras key를 정렬한 구조 fingerprint는 두 파일 모두 `D651041A7C8438C223B450020651375ACBB79140835E4964486B89D372578ADA`로 일치했다.

그러나 187개 노드 중 `clickable=true`와 `CLICK` Action은 모두 0개였다. 노출된 앱 고유 viewId도 `root`, `TabletAppScreenWrapper`, `calendar-month`, `calendar-week`뿐이며 과제나 다른 화면으로 이동할 selector를 만들 수 없다. 따라서 로그인 직후 화면의 의미 기반 이동은 현재 기기·버전에서 FAIL이다. 좌표 fallback은 사용하지 않았다.

사용자 확인 결과 이 화면은 학생 표시명이 보이는 `홈` 화면이 아니다. 따라서 이 캡처로 표시명 selector를 판정하지 않으며, 홈 화면을 별도로 수집한다.

## 문제풀이

사용자가 시험용 과제를 새로 만들어 문제풀이 화면으로 이동한 뒤 `capture_20260721_093013_758_8.json`과 `capture_20260721_093044_890_9.json`을 수집했다. 각각의 captureId는 `071377ae-bb2b-4351-9f8b-ef0b12a4d1c4`, `011091bd-099d-40f6-afde-a1cd0c78203e`이고 기기 내부 파일 SHA-256은 각각 `1433ee12ca79d7bc5b7a02b788988b4900734e2e5516cf0414ddfa6ad7ce28b7`, `7f495baab98ae26ac1e687e18bac61a1c19d0e54d8b2a343e66c8e6ab7c90b74`다.

두 capture 모두 schema 2, node 68개, truncated `false`, rawTextStored `false`, 개인정보 평문 0개였다. bounds와 redacted session fingerprint를 제외하고 Action 이름과 extras key를 정렬한 구조 fingerprint는 두 파일 모두 `D0A3A0368CB7DD617014A7212539F69CB2DCA294159527AA04C8DDDE39C26267`로 일치했다.

`CLICK` 가능한 Button은 13개이며 이 중 텍스트가 있는 Button은 6개다. 나머지 7개는 text, content description, resource ID가 모두 없어 의미 기반 식별이 불가능하다. 문제풀이 화면 자체의 구조 안정성은 확인했지만, 상태 기반 복귀 가능 여부는 사용자가 실제 복귀에 쓰는 버튼과 접근성 노드를 연결한 뒤 판정한다.

사용자는 앱 화면 좌상단의 `<` 아이콘으로 문제풀이에서 복귀했다고 확인했다. 그러나 문제풀이 capture에서 좌상단 영역에 해당하는 Button, `CLICK` Action, resource ID, content description은 노출되지 않았다. 따라서 실제 복귀 수단을 의미 기반으로 탐지·실행할 수 없으며 “문제풀이에서 대시보드로 상태 기반 복귀” 필수조건은 FAIL이다.

## 문제 복귀 후 화면

사용자 복귀 뒤 `capture_20260721_093433_687_10.json`과 `capture_20260721_093459_758_11.json`을 수집했다. 각각의 captureId는 `38a149f2-ba04-4089-86d8-c76c12fc8db2`, `7137705e-488c-45ce-a238-47a29f55a346`이고 기기 내부 파일 SHA-256은 각각 `f8693b489977ef240df5b5a741416dd2b6b59663308206223764c36c9813196d`, `1817ce9bbfbeb2ec90c43a5eac50f26a667390e2bbc86bcb4f10727e5806e0b5`다.

두 capture 모두 schema 2, node 52개, truncated `false`, rawTextStored `false`, 개인정보 평문 0개였다. 동적 workbook 숫자 ID, bounds와 redacted session fingerprint를 제외하고 Action 이름과 extras key를 정렬한 구조 fingerprint는 두 파일 모두 `685600769E95728BE9B6EC7AE940B0206D7488CADF1936288AE377693A3F4F83`으로 일치했다. `CLICK` 가능한 요소는 EditText 1개와 텍스트 Button 1개뿐이고, 좌상단 복귀 아이콘이나 과제 카드에 해당하는 별도 의미 노드는 없다.

## 홈 및 표시명

사용자가 시험계정 이름이 보이는 홈 화면으로 이동한 뒤 `capture_20260721_101046_563_12.json`과 `capture_20260721_101149_433_13.json`을 수집했다. 각각의 captureId는 `56854f9a-9ded-4f8b-8f51-036b778a1925`, `3e9da4c4-c38c-40d3-8c32-1b7ae546b541`이고 기기 내부 파일 SHA-256은 각각 `ab8a659644784e768f842daaff3878ab020a66d55edcac672b22955f665e4b97`, `6570f353f6542b4e5705d08f3c42712812365203f062233833353e350d36b738`이다.

두 capture 모두 schema 2, node 192개, truncated `false`, rawTextStored `false`, 개인정보 평문 0개였다. bounds와 redacted session fingerprint를 제외하고 Action 이름과 extras key를 정렬한 구조 fingerprint는 두 파일 모두 `AD7D635D28FDDEA68735CF5486C9792D7F9D18E2504F4A895CD886D2D2940B67`로 일치했다. 사용자는 실제 이름 표시를 시각 확인했지만 화면 전체에 TextView가 104개이고 모든 요소의 `clickable=true`와 `CLICK` Action은 0개다. 이름 노드의 위치를 사용자 시각 정보와 연결한 뒤 고유 selector 가능 여부를 판정한다.

사용자는 이름이 좌측 상단 제목줄에 있다고 확인했다. 해당 위치와 연결되는 후보는 path `0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.8`의 TextView이며 두 capture에서 동일한 길이 3 redacted session fingerprint가 재현됐다. 다만 resource ID, role, content description과 action이 모두 없고, 직접 자식 순번 외의 의미 속성이 없다. 따라서 exact package/version, 승인된 홈 전체 fingerprint, 부모 계층, TextView 역할 및 후보 유일성을 모두 요구하는 약한 selector 후보로만 기록한다. 다른 계정에서 표시명만 바뀌는지 확인하지 않았으므로 실제 표시명 읽기는 PARTIAL이다.

계획서와 첨부 영상 검토에서 추정했던 “우측 상단 계정·설정” 경로는 현재 시험계정 홈에 존재하지 않는다. 사용자 시각 확인 결과 우측 상단의 아이콘 3개는 왼쪽부터 QR, 종이+돋보기(학습번호 입력), 말풍선(채팅)이다. 접근성 트리에서는 각각 path 끝이 `.10`, `.11`, `.12`인 47×45 영역으로 나타나지만 text, content description, resource ID와 `CLICK` Action이 모두 없다. 따라서 세 기능도 의미 기반으로 구분하거나 실행할 수 없다. 실제 계정·로그아웃 경로는 별도로 찾아야 한다.

사용자가 좌측 상단 이름을 누르자 프로필 팝업이 열렸다. 이로써 실제 계정 진입 경로는 확인했으나, 이름 TextView 자체에는 `clickable=true`나 `CLICK` Action이 없다. 따라서 사용자는 진입할 수 있지만 자동화가 의미 기반으로 실행할 수 없는 경로다.

## 프로필 팝업 및 로그아웃

사용자가 좌측 상단 이름으로 프로필 팝업을 연 뒤 `capture_20260721_103548_978_14.json`과 `capture_20260721_103638_521_15.json`을 수집했다. 각각의 captureId는 `2c4b872b-20f6-4147-a543-1c173e95a4ef`, `0991e487-a0e4-4cb7-8dba-64af169d8bf1`이고 기기 내부 파일 SHA-256은 각각 `85e53155377d941afd1ec1d5ab52978f0a24f0e947efba3932588f5baf680a1a`, `ef54a3e0572da77670c563cd4e84cd25fd955bd7f703a4a1fe18096d319d53c0`이다.

두 capture 모두 schema 2, node 236개, AlertDialog 1개, `CLICK` Action 12개, truncated `false`, rawTextStored `false`였다. bounds와 redacted session fingerprint를 제외하고 Action 이름과 extras key를 정렬한 구조 fingerprint는 두 파일 모두 `233B09B51EF8070FC91306C69CA23980062156F2D48FEFB85E31FD6EF6BB28F9`로 일치했다. 평문은 allowlist의 고정 UI 문구 `학생`, `로그아웃`, `닫기` 3개뿐이고 개인정보 평문은 0개다.

프로필의 이름 후보는 path 끝 `.3`의 길이 3 TextView이며 홈 제목줄 후보와 같은 session fingerprint를 가진다. 사용자는 바로 아래 길이 6과 15의 TextView가 각각 아이디와 학원명이라고 시각 확인했다. 따라서 이름 외 두 번째 학생 식별값 후보는 아이디 TextView다. 다만 세 노드 모두 resource ID나 의미 role이 없고 다른 계정으로 값 변화만 교차검증하지 않았으므로 강한 selector로 확정하지 않는다.

로그아웃 후보는 path 끝 `.31.0`의 Button이며 text `로그아웃`, `clickable=true`, enabled, visible, `CLICK` Action 조건을 모두 만족하고 두 capture에서 정확히 1개다. 같은 부모에 `닫기` Button이 있어 팝업 footer를 교차검증할 수 있다. 사용자가 해당 버튼을 한 번 눌러 즉시 로그인 화면으로 복귀했으므로 로그아웃 실행 경로도 확인됐다.

## 로그아웃 후 상태 검증

로그아웃 직후 `capture_20260721_104306_795_16.json`을 수집했다. captureId는 `4d7a6dfb-7a1b-40f2-a344-f5a90ad03201`, 기기 내부 파일 SHA-256은 `052fb1ac90e87f5c76f77d75b4c530a5693db5ad13642ffdb4fcd83dde89e6b3`이다. node 23개, EditText 2개 모두 empty, 고정 text `로그인` Button 1개, 인증 홈의 `calendar-month`/`calendar-week` 노드 0개, dialog 0개였다.

Android 홈으로 2초간 전환한 뒤 매쓰홀릭 `com.matholic.mathapp/.MainActivity`를 다시 전면 실행하고 `capture_20260721_104342_704_17.json`을 수집했다. captureId는 `35b1966d-e2e7-46bf-a953-dfb148755ea0`, 기기 내부 파일 SHA-256은 `2ba19e60f0c2156c36bb54be6c3d26543e7f9767034aafc574dd98f19ec870a4`다. node 23개와 위 비로그인 조건이 모두 유지됐다.

두 파일은 bounds와 redacted session fingerprint를 제외하고 Action 이름과 extras key를 정렬한 구조 fingerprint `2F18CBC98164EA77D0F2522B2A64D54BAB5C27C21C89D8C25F426986F22EF82E`로 일치했다. 실제 로그아웃 후 UI 기반 비로그인 상태와 빈 입력란 검증은 PASS다. 이는 서버 세션 폐기를 직접 증명하는 것이 아니라 계획서가 정의한 UI 기반 운영상 검증이다.

## 최종 판정

- 좌표 selector: 현재 0
- 개인정보 평문: 현재 0
- 기억하기 TRUE→FALSE: **FAIL**. 사용자가 시각적 TRUE를 확인한 schema 2 capture `capture_20260721_082528_261_1.json`(SHA-256 `39663598769d73e7518641784cabada27b8f4971790c32f4f7e10006e59214fc`)에서도 `checkable=false`, AndroidX compat checked `FALSE`, state/role description 없음. 시각적 체크 전·후 기존 비좌표 필드 차이도 0개였다.
- 실제 표시명은 홈 제목줄과 프로필의 동일 session fingerprint로 연결했다. 두 번째 학생 식별값 후보는 프로필 아이디 TextView지만 두 후보 모두 의미 속성이 없고 교차계정 미검증이므로 PARTIAL
- 문제풀이 상태 기반 복귀: **FAIL** — 실제 좌상단 `<` 복귀 아이콘이 접근성 트리에 미노출
- 프로필 팝업, 로그아웃 탐지, 수동 로그아웃과 로그아웃 후 빈 필드: PASS
- 계정 진입: **FAIL** — 실제 경로인 좌측 상단 이름 TextView가 `CLICK` Action을 제공하지 않음
- 로그인 직후/홈 의미 기반 이동: **FAIL** — `clickable=true`와 `CLICK` Action 0개
- Gate 1: **최종 FAIL** — 기억하기 상태 판독·해제, 문제풀이 복귀, 계정 진입이라는 복수의 필수조건을 좌표 없이 구현할 수 없음
- Gate 2 사용자 승인: **없음**. Gate 1이 FAIL이므로 승인 요청 대상도 아니며 locked POC를 해제하지 않는다.

## 기기 B 단축 재검증

B의 첫 capture `capture_20260721_111939_578_1.json`은 로딩 중 node 14개 상태였다. 6초 뒤 `capture_20260721_112003_145_2.json`에서 A와 같은 로그인 의미 노드 25개가 나타났다. captureId는 `53501dbf-43f4-486b-8a97-5f2fbeb87a75`, 기기 내부 파일 SHA-256은 `f02b41a40a05c460797f9426acad2df0f0fad2a10c289e547dd2fbc037771638`이다. 아이디·비밀번호 EditText는 empty이고 `SET_TEXT`, 로그인 Button은 `CLICK`을 제공했다.

사용자가 기억하기를 시각적으로 체크한 뒤 수집한 `capture_20260721_113350_763_3.json`의 captureId는 `5a0c6d5b-886b-41b4-945b-f0c1d24e14ac`, 기기 내부 파일 SHA-256은 `ec47cb5368e48943631b28d8569050468a34b8183bbe6728689c1db7ce1705d7`이다. 체크 전·후 모두 node 25개이며 checkbox 후보는 `checkable=false`, legacy checked `false`, AndroidX compat `checkedState=FALSE`, state/role description 없음이었다. Action 이름을 정렬해 비교한 비좌표 checkbox 필드 차이는 0개다.

따라서 Android 14/One UI 6.1인 B에서도 기억하기 실제 상태를 읽거나 안전하게 해제됐음을 검증할 수 없다. 하나의 필수조건만 실패해도 Gate 1은 FAIL이므로 시험계정 로그인과 나머지 화면 반복 수집은 불필요한 민감정보 노출과 사용자 작업을 줄이기 위해 생략했다. A의 FAIL 판정과 Gate 2 잠금은 유지된다.

## 공급사 회신 및 공식 웹 대안

2026-07-21 사용자 전달에 따르면 공급사는 요청한 Android 접근성 개선이나 공식 자동화 지원을 제공할 수 없다고 답했다. 따라서 공급사 수정 후 Gate 1 재수행 경로는 현재 사용할 수 없다.

같은 날 공개된 공식 로그인 페이지 `https://login.matholic.com/`을 인증정보 없이 확인했다. 아이디와 비밀번호는 각각 이름이 지정된 textbox이고, `아이디 저장`은 실제 checkbox, `로그인`은 submit button으로 노출된다. form은 HTTPS의 `https://auth.matholic.com/token/signin`으로 제출된다. 공개 로그인 화면의 의미 구조만 보면 Android 앱에서 실패한 기억하기 상태 판독과 의미 기반 조작 문제는 없다.

후속 실기에서 시험계정 로그인, 학습 홈·문제 화면 진입, 뒤로가기 복귀, 계정 메뉴, 로그아웃과 로그아웃 후 학습 주소 차단을 확인했다. Web Gate 1은 조건부가 아닌 기술 타당성 PASS로 판정한다. 다만 로그아웃 후 비밀번호는 비고 저장 checkbox도 해제됐지만 이전 아이디가 새로고침 뒤에도 다시 채워졌다. 페이지 로드 후 의미 기반으로 아이디를 비우자 두 입력칸 empty와 checkbox unchecked가 확인됐다. 따라서 이 정리는 Web POC의 필수 동작이다. 세부 결과는 `docs/WEB_GATE1_REPORT.md`에 기록했으며 기존 Android Gate 2 잠금은 해제하지 않는다.

## 권고

현재 기기·매쓰홀릭 7.5.1 조합에서는 접근성 기반 QR 자동 로그인·자동 로그아웃 구현을 중단한다. 좌표 자동화, 이미지 템플릿, ADB 입력 또는 비공개 API로 전환하지 않는다. 공식 웹 경로의 Web Gate 1은 PASS했으므로 다음 합리적 단계는 사용자 승인 후 별도 Web POC를 만들고 시험계정으로 자동 로그인·로그아웃·잔존값 정리를 검증하는 것이다.
