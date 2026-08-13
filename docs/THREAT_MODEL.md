# 위협 모델

기준일: 2026-08-13

## 범위와 보호 자산

현재 범위는 생산 Kiosk, Web POC, PC 수신기, Room 데이터, QR 카드, 자동 시간표,
Device Owner/Lock Task, 제한 시간 원격 지원과 운영 스크립트다. 과거 Gate 0~4 Probe와
접근성 POC는 역사적 검증 도구이며 생산 학생 흐름이 아니다.

보호 대상은 다음과 같다.

- 학생 표시명, 반 소속, 수업·보강 상태와 채점 결과
- 매쓰홀릭 아이디·비밀번호와 Web 인증 세션
- QR 원문, QR hash, 생성 PDF와 인쇄된 카드
- 관리자 PIN verifier, release signer와 PC 페어링 비밀
- PC로 오가는 PDF·CSV·상태 정보와 Windows 알림
- Device Owner, Lock Task, 원격 지원과 자동 시간표의 운영 통제

## 신뢰 경계

- Kiosk와 Web POC는 별도 Android UID다. 자격정보는 signature 권한, 신뢰 package와
  signer 검사, 1회용 192비트 handle, 30초 TTL의 메모리 provider를 통해서만 전달한다.
- Kiosk Room DB는 앱 private storage에 있고 backup·device transfer를 금지한다.
  아이디·비밀번호는 Android Keystore AES-GCM으로 필드별 암호화하지만 표시명, 반 관계,
  QR SHA-256 hash와 비민감 audit는 구조상 평문이다.
- QR 원문은 PDF 생성·카메라 검증에 필요한 짧은 시간만 메모리에 존재한다. 저장 DB에는
  hash만 남으며 QR 재발급·비활성화 후 과거 PDF와 실물 카드는 회수할 수 없다.
- Web POC는 승인 HTTPS host만 상위 탐색하고 TLS 오류, Safe Browsing 경고, download,
  file/content 접근과 변형된 로그인·학생 경로를 실패 폐쇄한다. 허용 사이트가 불러오는
  제3자 하위 resource와 공급사 서버 자체는 앱의 통제 밖이다.
- Kiosk와 PC 수신기는 같은 사설망에서 challenge-response로 페어링한다. PDF·CSV·상태
  요청은 인증·무결성·timestamp/replay 제한과 전체 연결 제한을 적용한다. PC 파일 저장,
  Windows 계정, 프린터·대기열과 출력물은 Android 밖의 신뢰 경계다.
- Kiosk는 Device Owner와 Lock Task를 운영 경계로 사용한다. 관리자 PIN, release signer,
  승인 ADB PC를 가진 주체는 운영 관리자다. PIN 분실이나 Device Owner 제거는 공장초기화가
  필요하며 앱 DB·Keystore·기존 QR을 복구하지 않는다.
- 기본 화면은 `FLAG_SECURE`다. 제한 시간 원격 지원 중에만 승인 PC가 비민감 화면을
  확인할 수 있으며, 관리자 PIN·비밀번호 입력 화면에서는 지원을 시작하지 않는다.
- 자동 시간표는 기기 wall clock, 저장된 주간/오늘 설정과 현재 Room 수업 상태를 신뢰한다.
  학생 채점, 관리자 화면과 데이터 작업 중에는 전환을 보류하고 실행 직전 상태를 재검증한다.

## 주요 위협과 현재 통제

| 위협 | 현재 통제 | 잔여위험·운영 책임 |
|---|---|---|
| 자격정보가 source·Git·로그에 남음 | 런타임 입력, 비밀 파일 ignore, redacted 진단, `CharArray` 사용 후 덮어쓰기 | JVM/WebView 메모리의 일시적 사본을 완전 제거했다고 증명할 수 없음 |
| 다른 앱이 자격정보 bridge 호출 | signature 권한, package·signer 검사, 1회 handle·TTL·read-once | release signer 또는 승인 기기가 침해되면 경계가 무너짐 |
| Web가 다른 학생·변형 페이지에서 자동화 | 표시명 완전 일치, origin/path/DOM fingerprint, 알 수 없는 상태 실패 폐쇄 | 공급사가 같은 구조의 의미를 바꾸면 실기 재검증 필요 |
| Web 세션이 다음 학생에게 남음 | 로그아웃과 빈 로그인 화면 확인 후 Cookie/WebStorage/form/cache 정리 | 서버 세션 폐기를 앱 밖에서 직접 증명할 수 없음 |
| Web renderer·network 장애 중 민감 상태 재개 | 상태 machine 영속화, 재시작 복구, 사용할 수 없는 WebView 폐기, 배경 전환 잠금 | 실제 A에서 renderer crash를 반복 강제한 장시간 시험은 없음 |
| QR 원문·과거 카드 재사용 | DB에는 hash만 저장, 재발급·비활성화 시 hash 원자 교체 | 배포된 종이·코팅 카드와 외부 PDF 복사본은 앱이 회수하지 못함 |
| 학생·반·수업 동시 변경 | 공통 관리자 gate, 단일 IO executor, Room transaction, session ID·상태 재검증 | process 강제종료와 모든 빠른 연속 탭 조합은 별도 fault-injection 대상 |
| 자동 시간표가 학생·관리자 작업을 침범 | 학생 busy·관리자 화면·공통 gate에서 보류, 적용 직전 UI/session 재검증 | 기기 시각을 바꿀 수 있는 관리자·승인 ADB는 시간표 결과도 바꿀 수 있음 |
| 자동 시간표 오류가 운영자를 과도하게 알림 | 최초 즉시, 같은 오류 10분 cooldown, 오류 변경·정상화 즉시 알림 | PC가 꺼진 동안의 Windows 알림 전달은 보장하지 않음 |
| LAN의 비지정 PC가 PDF·CSV·상태 요청 | 페어링 비밀 기반 인증, request ID·hash ACK, replay 제한, 사설 subnet 제한 | 승인 PC와 DPAPI 사용자 계정이 침해되면 저장 파일·알림이 노출될 수 있음 |
| DHCP 변경 뒤 다른 PC로 연결 | 같은 사설 `/24` 후보만 조사하고 기존 pairing 인증 성공 대상만 채택 | 다른 subnet 또는 수신기 재설치 후에는 수동 재페어링 필요 |
| PC 수신기 DoS·부분 연결 | 연결 수·payload·event queue 제한, connect/read/write/전체 deadline, 종료 시 socket 회수 | 같은 LAN의 반복 연결은 제한 안에서 PC 자원을 소비할 수 있음 |
| Windows 알림에 학생 이름 노출 | 지정 PC의 로컬 알림에만 필요한 최소 상태 전송 | 잠금 화면 알림 공개 여부는 Windows 사용자 설정에 따름 |
| CSV가 기존 학생·반을 잘못 변경 | 크기·행 수·형식 제한, 미리보기, 기존 반 검증, 적용 중 전역 data gate | 승인 PC에서 작성한 잘못된 CSV 내용은 관리자가 미리보기에서 확인해야 함 |
| 직접 인쇄·프린터로 데이터 유출 | Android 직접 인쇄 제거, 지정 PC PDF 저장 ACK 뒤 PC에서 검토·인쇄 | PC 파일, 프린터 메모리·대기열과 폐기 출력물은 운영자가 관리함 |
| 선택적 Android PDF 공유의 외부 복사 | private cache, 만료 예약, FileProvider의 제한 URI | 읽은 외부 앱 복사본과 process가 종료된 동안 남은 cache를 회수하지 못함 |
| 스크린샷·최근 앱·키오스크 이탈 | `FLAG_SECURE`, 최근 앱 제외, Device Owner·Lock Task·전용 HOME | 공장초기화·물리 접근·OS 취약점과 승인 ADB는 별도 신뢰 경계 |
| 원격 지원 중 민감 화면 노출 | 같은 signer/ADB 권한, 활성 시간 제한, 상태 배지, 종료 시 임시 캡처 정리 | 지원 중 화면은 승인 PC와 Codex 화면에 보일 수 있음 |
| release 또는 의존성 공급망 변조 | 별도 signer, APK/EXE SHA-256, Gradle distribution checksum, 고정 런타임 버전 | build tool과 전이 의존성 검증 metadata를 계속 유지해야 함 |

## 실패 시 원칙

학생·반·세션 ID, package·signer, Web origin/path/DOM, PC 인증·ACK 또는 자동 시간표
상태가 기대와 다르면 변경을 진행하지 않는다. 민감 중간 상태는 `RECOVERY_REQUIRED` 또는
`LOCKED`, 시작 전 호환성 불일치는 `MAINTENANCE_REQUIRED`로 처리한다. 알 수 없는 학생으로
자동 재로그인하거나 좌표만으로 Web 동작을 계속하지 않는다.

## 사고 대응

- 자격정보·PIN·페어링 비밀이 노출되면 공유를 중단하고 해당 비밀을 교체한다.
- release signer 노출이 의심되면 설치·배포를 중지하고 기기별 signer 상태와 배포 경로를
  별도 확인한다.
- QR 또는 PDF 유출은 QR 재발급으로 과거 카드를 무효화하고 외부 파일·출력물은 운영자가
  회수·폐기한다.
- 잘못된 학생·반·수업 변경은 audit와 transaction 결과를 보존한 채 안전한 복원 절차를
  사용한다. 공유 Git 이력의 파괴적 재작성은 별도 승인 없이는 하지 않는다.
