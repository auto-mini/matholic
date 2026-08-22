# Release 서명·운영 전환

작성일: 2026-07-24, 갱신일: 2026-08-22 (Asia/Seoul)

## 현재 상태

RC02는 A에 release Device Owner로 배포해 핵심 실기를 완료했다. 현재 A에는
같은 signer의 Kiosk RC96·Web POC RC139가 보존형 설치돼 있다. Kiosk DB, Device
Owner와 전용 HOME을 보존했고 두 설치본은 서명·버전·보관 artifact 검증을
통과했다. 현재 보관 검증 묶음은 Kiosk RC96, Web POC RC139와 PC 수신기
0.1.8이다.
Kiosk·Web의 정확한 자동·릴리스 검증과 설치 여부는
`docs/BUILD_VERIFICATION.md`의 최신 절, PC 수신기 0.1.8의 검증·설치 여부는
`reports/SOL_MAX_CONTINUOUS_REVIEW_AND_DEVELOPMENT_2026-08-04.md`의
  `SOL-0016`을 기준으로 한다.

현재 소스 작업본의 Kiosk RC96은 반별 시작 시각만 설정하는 3시간 자동 반 시간표,
학생 채점·관리자 작업 중 전환 지연, 종료 뒤 관리자 인증 복귀와 오늘만 끄기/임시
시간표를 제공한다. 적용 직전에 상태를 다시 읽어 오래된 판정으로 반을 바꾸지 않고,
같은 시간표 오류의 PC 알림은 10분 간격으로 제한하며 가려진 시간표 버튼 터치를
거부한다. 수업 종료 뒤 관리자 화면에서 다음 자동 시작과 즉시 수동 시작 절차를
보여주고, 상단 버튼으로 관리자 화면을 잠가 자동전환을 방해하지 않는 PIN 대기로
돌아간다. 또한
예약 반에 활성 소속 학생이 한 명도 없으면 Web 점검과 세션 생성을 시작하지 않고
그 수업을 건너뛴 뒤 다음 시간 경계까지 기다린다. 현재 수업에서 다음 빈 반으로
전환할 때는 현재 수업만 안전 종료하고 빈 반은 시작하지 않는다. 빈 반에 학생을
추가하고 관리자 작업을 닫으면 남은 수업 시간 안에 자동 시작을 다시 확인한다. 또한
선택적 공유 QR PDF가 process 재시작을 거쳐도
남은 수명 안에 정리되도록 예약을 복원하고, 전용 FileProvider가 만료 파일을
URI로 제공하기 전에 정리한다. 최초 준비 시 신규카드1~4 더미 QR을 자동
준비해 지정 PC로 전송한다. 자동 PDF 전송은 핵심 초기화와 별도 executor에서
실행하므로 지정 PC가 응답하지 않아도 관리자 인증·관리자 데이터 준비를 막지
않는다. TCP 연결 뒤 PDF를 읽지 않는 지정 PC의 write도 제한 시간 뒤 channel을
닫아 executor를 무기한 점유하지 않는다. 더미의 아이디·비밀번호는 빈 값이며,
학생 배정은 현재 수업 보강에 자동 추가하지 않는다. 재시작, 이름·CSV 갱신과
일반 QR 재발급은 재사용 카드 QR을
바꾸지 않는다. 학생 CSV 가져오기는 PC 요청·parse·미리보기·적용 전체 수명 동안
공통 관리자 데이터 gate를 유지해 수업 시작과 다른 학생·반 작업을 차단한다.
실제 QR 카드로 전환하면 새 일반 학생 QR을 발급하고 기존 더미
슬롯을 빈 값으로 되돌린다. 저장 미확인 슬롯을 수동 교체할 때는 기존 QR과 이전
인쇄물 무효화를 확인한 뒤 새 QR을 발급한다. 과거 비활성 재사용 슬롯이 있으면
같은 라벨의 새 QR·빈 자격정보로 복구하고 목표 4장을 보충한다. 복구 뒤에는 새
PDF를 저장·출력해야 한다.

현재 Web POC RC139는 secure renderer 복구 재생성을 백그라운드 잠금으로 오인하지
않는다. loopback CONNECT proxy의 32 tunnel 상한과 60초 idle 회수,
accept listener 1회 bounded restart를 유지하고 upstream 연결 실패 때 아직 반환되지
않은 socket도 즉시 닫는다. 정확한 자동·설치 검증과 실제 Web 로그인 미수행 경계는
`docs/BUILD_VERIFICATION.md`의 최신 절을 기준으로 한다.

- 현재 A: Kiosk `0.6.0-rc96`/code 101, Web POC `0.4.0-rc139`/code 156
- 내부 보관 현재 검증 묶음:
  Kiosk `0.6.0-rc96`/code 101, Web POC `0.4.0-rc139`/code 156,
  PC 수신기 `0.1.8`
- 현재 운영 PC 설치본: PC 수신기 `0.1.8`
- signer SHA-256: `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- Kiosk RC96 APK: 36,901,893 bytes,
  `061862C1C59F43D83A121A3F4D15B21427CC9294A7C47D6B91B43474F58A69B3`
- Web POC RC139 APK: 3,396,754 bytes,
  `2967F5F2011E870B0C6429797055082118955B3D990CF411F751DC791497132E`
- 현재 A: release signer의 Kiosk RC96/Web POC RC139, 기존 Device Owner·전용
  HOME·Kiosk/Web UID·firstInstallTime·dataDir 유지, 관리자 PIN 대기·Lock Task
  `LOCKED`, 원격 점검 `INACTIVE`
- Kiosk RC55는 Device Owner 정책으로 Web POC 제거를 차단한다.
- 휴대 가능한 release 키 복구본: **SM-S918N Android 폰에서 SHA-256 일치 확인**
- 두 번째 오프라인 release 키 복구본: **별도 SanDisk USB에서 SHA-256 일치 확인**
- 복구 비밀번호 분리 보관: **사용자 확인 완료**
- 두 번째 공장초기화·release 프로비저닝: **완료**
- RC02 정상 왕복·비정상 Web 세션 자체 복구·재부팅 복구: **완료**
- RC02 개발자 옵션·USB 디버깅 제거 및 ADB 없는 물리 실기: **완료**
- RC02 실제 프린터 출력·종이 QR 왕복·120분 연속 운전: **완료**
- RC27/RC29 관리자 뒤로가기·자동 학습지 진입 재검증: **통과**
- RC42 문제별 주관식 입력·숫자 키패드·뒤로가기·키보드 겹침 실기: **통과**
- 수식 답 저장 뒤 회색 삭제 제어가 숨겨질 때 약 0.2초 음영이 보일 수 있으나
  기능·오입력 문제는 없고 사용자도 불편하지 않음을 확인

debug signer에서 release signer로의 전환은 공장초기화와 새 Device Owner 등록으로 완료했다. 앞으로 같은 release signer와 더 높은 versionCode의 APK는 앱 데이터와 Device Owner를 보존해 덮어쓸 수 있다.

RC74/RC135부터 채점 결과 요약을 Web POC에서 Kiosk로 전달하므로 두 APK를
같은 회차에 함께 설치한다. 한쪽만 올린 상태를 운영 배포 완료로 판정하지 않는다.

## 서명키 경계

실제 키와 비밀번호는 저장소 밖에 있다.

- 키: `%LOCALAPPDATA%\MatholicKiosk\release-signing\matholic-kiosk-release.p12`
- 로컬 자격정보: 같은 폴더의 `matholic-kiosk-release.credential.clixml`
- 비밀번호 보호: 현재 Windows 사용자 DPAPI
- Git 포함: 금지
- 로그·대화 출력: 금지

DPAPI 자격정보는 다른 PC나 Windows 사용자 프로필에서 복구할 수 없다. 따라서 배포 전 반드시 키 파일을 별도 매체에 복사하고, 복구 비밀번호를 키 파일과 다른 비밀번호 관리자 또는 오프라인 기록에 보관해야 한다. 둘 중 하나를 잃으면 같은 applicationId의 업데이트 APK를 만들 수 없고 기기 공장초기화가 필요하다.

## 휴대 가능한 복구 준비

사용자가 관리하는 별도 디렉터리를 정해 다음 명령을 직접 실행한다. 회사 클라우드나 공유 폴더에 둘지는 사용자가 보안 정책에 따라 결정하며 스크립트가 임의 업로드하지 않는다.

```powershell
.\scripts\confirm-release-signing-recovery.ps1 -BackupDirectory 'D:\MatholicKioskSigningBackup'
```

ADB가 허용된 개인 Android 폰을 복구 매체로 쓸 수도 있다.

```powershell
.\scripts\confirm-release-signing-recovery.ps1 -PhoneSerial '<폰 ADB serial>'
```

기본 위치는 폰의 `Documents/MatholicKioskSigningBackup/matholic-kiosk-release.p12`다. 폰을 초기화·분실하면 복구본도 사라지므로 장기적으로는 별도 USB나 오프라인 매체에도 한 부를 추가한다.

로컬 창에 복구 비밀번호가 한 번 표시된다. 키 파일과 다른 장소에 보관한 뒤 체크하고 완료한다. 스크립트는 다음을 확인한다.

- 원본 키와 복사본 SHA-256 일치
- 공개 인증서 signer fingerprint 산출
- 비밀번호 별도 보관 사용자 확인
- 로컬 `portable-recovery-confirmed.json` marker 생성

`provision-release-device-owner.ps1`는 이 marker, 원본 키, 복구본과 release APK signer가 모두 일치하지 않으면 기기를 변경하지 않는다.

2026-07-24 실제 확인 결과:

- 폰 경로: `Documents/MatholicKioskSigningBackup/matholic-kiosk-release.p12`
- 폰에는 암호화된 PKCS12 키만 복사
- DPAPI 자격정보와 복구 비밀번호: 폰 미복사
- PC 원본·폰 복사본·marker SHA-256: 일치
- marker signer·release APK signer: 일치

폰은 임시 휴대 복구본으로 인정하지만 유일한 장기 백업으로 보지 않는다. production 운영이 안정된 뒤 별도 USB 또는 오프라인 매체 한 부를 추가한다.

2026-07-27 별도 SanDisk USB의
`MatholicKioskSigningBackup/matholic-kiosk-release.p12`에 두 번째 복구본을
추가하고 PC 원본과 SHA-256
`81B543E21DB56707A122125BC1A99E47C17462DF2ED091BA7E1CF83E3391B111`
일치를 확인했다. USB에는 암호화된 PKCS12 키만 복사했고 복구 비밀번호와
Windows DPAPI 자격정보는 복사하지 않았다. 기존 Android 폰 복구 marker는
변경하지 않았다.

## Release 빌드

최초 한 번만 키를 만든다. 기존 키가 있으면 스크립트는 덮어쓰지 않는다.

```powershell
.\scripts\new-release-signing.ps1
.\scripts\build-release.ps1
```

빌드 스크립트는 DPAPI 비밀번호를 프로세스 메모리에서 읽고 Gradle 환경으로만 전달한다. configuration cache와 장기 daemon을 끄며 빌드 뒤 환경변수를 제거한다. 다음을 자동 실행한다.

- Kiosk/Web JVM 단위 테스트
- Kiosk/Web release lint
- 서명된 두 release APK assemble
- applicationId·versionName·권한 검사
- `debuggable=false`
- APK Signature Scheme v2, signer 1
- 두 APK signer 일치 및 Android Debug signer 거부
- zipalign 검사
- `artifacts/RELEASE_SHA256SUMS.txt` 생성

서명 환경이 없는 직접 release 빌드는 `Release signing is required`로 실패한다.

## Android 계측시험 안전 경계

A와 에뮬레이터가 동시에 연결된 PC에서 raw
`:webpoc:connectedDebugAndroidTest`를 실행하지 않는다. debug 시험 APK는
release Web POC와 applicationId가 같고 signer가 달라, 시험 도구의 설치·정리
동작이 생산 앱에 영향을 줄 수 있다.

Web POC 계측시험은 반드시 아래 에뮬레이터 전용 스크립트로 실행한다.

```powershell
.\scripts\test-webpoc-emulator.ps1
```

스크립트는 `emulator-` serial과 `ro.kernel.qemu=1`을 모두 확인하고
`ANDROID_SERIAL`을 고정한다. 물리 serial을 주면 시험 전에 실패한다.
Kiosk RC34는 이 절차상의 경계와 별도로 Device Owner의
`setUninstallBlocked` 정책을 적용해 Web POC 제거를 차단한다.

## Release 프로비저닝

아래 조건을 모두 충족하기 전에는 실행하지 않는다.

1. 휴대 가능한 키 복구 확인 완료
2. 기존 시험 QR 폐기 준비
3. A 공장초기화 승인
4. 초기 설정에서 복원·Google/Samsung 계정·보조 사용자 생략
5. 임시 USB 디버깅과 해당 PC 승인

초기화 후:

```powershell
.\scripts\provision-release-device-owner.ps1 -Serial R54TB029FHZ
```

스크립트는 release APK 검증과 복구본 일치를 먼저 확인하고, A 모델·소유자 없음·사용자 1명·계정 0개일 때만 두 APK 설치와 Device Owner 등록을 수행한다.

## 배포 후 필수 실기

1. 관리자 PIN 신규 설정
2. 시험 반·시험 학생 신규 등록 및 새 QR 발급
3. QR → Web 시험계정 확인 → 문제 화면 → 채점 끝내기 → `QR_READY`
4. 홈·최근 앱·뒤로·알림창·설정 차단
5. 전원 껐다 켜기와 완전 재부팅
6. `RECOVERY_REQUIRED` 관리자 복구
7. 실제 프린터 QR 1장 출력·재인식
8. 최소 1~2시간 시험계정 연속 운전
9. 관리자 화면에서 개발자 옵션과 USB 디버깅 끄기
10. ADB가 끊긴 상태에서 홈·최근 앱·재부팅을 물리 버튼으로 최종 확인

2026-07-24 RC02 실기에서 1~10을 모두 통과했다. 추가로 Web POC 강제 종료를 주입해 `WEB_SESSION_FAILED` 잠금, 관리자 PIN, 아래 자체 복구 절차와 정상 왕복 회귀를 확인했다. USB 디버깅을 끈 뒤 ADB 목록에서 A가 사라졌고, 케이블을 분리해 물리 재부팅·자동 실행·복구, 알림창 차단, 홈·최근 앱 버튼 제거와 뒤로가기 무반응을 확인했다. 120분 동안 네 번의 관찰 시점에서 화면·`QR_READY`·카메라·오류 없음이 유지됐고, 종료 왕복도 통과했다. 실제 프린터로 두 가지 크기를 출력하고 종이 QR 왕복을 통과했다.

### 잠긴 Web 세션 자체 복구

`WEB_SESSION_FAILED`, `WEB_SESSION_NOT_CLEAN` 또는 재부팅 복구가 나타나면 QR을 반복 촬영하지 않는다.

1. 관리자 PIN으로 반 관리 화면 진입
2. `Web 세션 안전 정리`
3. 확인창의 `정리 시작`
4. 성공 메시지 확인
5. `현재 수업 안전 종료`
6. 같은 반의 새 수업 시작

복구 호출은 정확한 Kiosk package와 같은 signer인 Web POC만 허용한다. Web 로그아웃·쿠키·저장정보를 정리하지만 학생 등록과 QR은 바꾸지 않는다.

### QR 대기에서 다음 반·보충 학생 바로 준비

수업을 마치고 QR 대기로 돌아온 뒤 관리자 전체 화면을 열 필요가 없는 경우:

1. QR 화면 오른쪽 아래 관리자 아이콘을 누른다.
2. 관리자 PIN을 입력한다.
3. 다음 수업이면 `다음 수업 반으로 바로 변경`→고정 반→`반 변경`을 누른다.
4. 현재 반에 보충 학생을 더 받을 때는 `현재 반에 보충 인원 추가 (이번
   수업만)`→학생 선택→`현재 수업에 추가`를 누른다.

반 변경은 이전 수업의 임시 보충 명단을 종료하고 새 수업을 만든다. 보충 학생
추가는 영구 반 소속을 바꾸지 않는다. 반에 활성 소속 학생이 없다는 안내가
나오면 관리자 전체 화면에서 해당 반 소속을 먼저 설정한다.

A의 개발자 옵션과 USB 디버깅은 껐고 생산 잠금 물리 실기를 통과했다. 현재 별도 원격 업데이트 채널은 없으므로 향후 APK 업데이트 때는 관리자가 USB 디버깅을 일시 재활성화하거나 별도 MDM/업데이트 채널을 구축해야 한다.

## 실패와 롤백

- release 키 복구 확인 전: A를 건드리지 않고 현재 debug alpha를 유지
- release APK 검증 실패: 설치하지 않고 빌드 원인 수정
- 초기화 후 Device Owner 등록 실패: 계정·사용자 조건을 점검하고 다시 공장초기화
- release 등록 후 debug alpha로 복귀: 서명이 달라 덮어쓰기 불가, 공장초기화 필요
- release 키 또는 복구 비밀번호 분실: 기존 설치본 업데이트 불가, 공장초기화와 새 application/signing 전략 필요
- Web 세션 잠금: 관리자 자체 복구 후 기존 수업 안전 종료·재시작

## 지정 PC로 QR 카드 PDF 보내기

2026-07-29부터 A와 같은 사설 Wi-Fi에 있는 지정 PC로 카드 PDF를 암호화해
직접 보낼 수 있다. 인터넷 서비스, 클라우드 계정, USB 연결과 주변 모든
사용자에게 보이는 공유 모드를 사용하지 않는다.

### 현재 PC 설치 상태

- 수신기 이름: `매쓰홀릭 PDF 수신기` `0.1.8`
- 설치 파일:
  `%LOCALAPPDATA%\MatholicPdfReceiver\app\MatholicPdfReceiver.exe`
- 수신 폴더:
  `%USERPROFILE%\Downloads\Matholic QR Cards`
- 수신 포트: TCP 48129
- 방화벽: `Matholic PDF Receiver (Private)`, Private 프로필만 허용
- 자동 시작:
  `%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup\Matholic PDF Receiver.lnk`
- 지정 PC 표시 이름: `DESKTOP-D4AGJI7`

### 최초 연결 또는 PC 재설치 뒤

1. PC에서 `매쓰홀릭 PDF 수신기`를 연다.
2. A의 관리자 PIN으로 반 관리 화면에 들어간다.
3. `지정 PC 다시 페어링`을 누른다.
4. PC 수신기에 표시된 페어링 QR을 A로 촬영한다.
5. A에서 PC 이름이 `DESKTOP-D4AGJI7`로 표시되는지 확인한다.

페어링 QR에는 이 PC로 암호화해 보낼 수 있는 비밀키가 들어 있으므로 학생
QR과 마찬가지로 촬영본을 외부에 공유하지 않는다. PC를 교체하거나 수신기
설정을 새로 만들었을 때만 다시 페어링한다. Kiosk RC46부터 같은 사설
Wi-Fi 안에서 DHCP 때문에 지정 PC의 IPv4 주소만 바뀐 경우에는 기존
페어링 키로 수신기를 인증해 새 주소를 자동 복구하므로 다시 페어링하지
않아도 된다.

### 카드 발급·전송

1. 관리자 화면에서 학생을 선택한다.
2. `QR 폐기 및 재발급`으로 새 카드를 만든다.
3. `현재 카드 지정 PC로 보내기`를 누른다.
4. A의 `지정 PC로 카드 PDF를 암호화해 보내는 중` 표시를 확인한다.
5. PC 수신기의 `...pdf 저장 완료` 표시를 확인한다.
6. PC의 `다운로드\Matholic QR Cards` 폴더에서 PDF를 열어 인쇄한다.

QR 재발급은 기존 QR을 즉시 무효화한다. 전송만 다시 해야 한다면 새 QR을
불필요하게 재발급하지 말고 현재 카드 전송 기능을 사용한다.

### 신규용 QR 카드 4장 미리 준비·재사용

Kiosk 첫 실행 때 신규용 더미 카드 4장을 자동으로 만들고 `신규카드1`~
`신규카드4` QR PDF를 지정 PC로 전송한다. 더미 행의 아이디·비밀번호는 빈
값으로 암호화해 로그인되지 않으며, PC 저장 ACK가 확인된 카드만 학생에게
배정하거나 분실 임시카드로 대여할 수 있다. 재전송이 필요한 경우 관리자 화면의
신규용 카드 관리에서 `미출력 더미 QR 다시 지정 PC 전송`을 실행한다.

새 학생이 들어오면 관리자 화면의 신규용 카드 관리에서 출력이 확인된 무료
카드를 선택하고 학생 이름·학습 ID·PW를 입력한다. QR은 재발급하지 않고
기존 hash를 유지한다. 이 배정은 현재 수업의 임시 보충 명단에 학생을
자동으로 추가하지 않는다.

기존 학생이 QR 카드를 잃어버렸다면 관리자 화면 또는 QR 대기의 관리자 작업에서
`분실한 기존 학생에게 임시카드 대여`를 선택한다. 출력이 확인된 무료 신규용 카드와
학생을 선택해 확정하면 학생의 계정·반·채점 이력은 바꾸지 않고 해당 신규용 QR만
학생 신원으로 판정한다. 대여 상태는 수업 종료와 앱 재시작 뒤에도 유지되며,
관리자가 `분실 임시카드 회수`를 확정하기 전까지 학생의 기존 QR과 그 사이 재발급한
일반 QR은 모두 차단된다. 실제로 카드를 회수한 뒤에만 회수 처리를 한다. 기존 카드를
영구 분실했다면 새 일반 QR을 먼저 재발급하고, 임시카드를 회수하면 새 QR이 활성화된다.

학생이 실제 QR 카드를 받게 되면 해당 학생을 선택해 `실제 QR 카드로 전환·
더미 초기화`를 실행한다. 앱은 새 일반 학생 행과 새 QR을 만들고 기존 반
소속을 새 학생으로 옮긴 뒤 새 QR PDF를 지정 PC로 전송한다. 기존
`신규카드1`~`신규카드4` 슬롯은 기존 더미 QR을 유지한 빈 계정으로 자동
초기화되므로 다음 신규 학생에게 재사용할 수 있다.

학생 사용이 끝난 뒤 수업을 종료하고 `사용 중 카드 회수·초기화`를 실행하면
계정정보와 반 소속을 제거하고 같은 QR을 무료 슬롯으로 돌린다. 수업 중에는
회수·초기화를 거부한다. 출력에 실패한 무료 슬롯은 다음 준비 때 기존 토큰을
교체하므로, PC 저장 ACK가 확인되기 전에는 학생에게 배정하지 않는다.

### 장애 확인

- PC가 표시되지 않거나 전송되지 않으면 먼저 A와 PC가 같은 사설 Wi-Fi인지,
  PC 수신기 창이 실행 중인지 확인한다.
- 수신기 0.1.8은 사설 LAN 주소가 아직 없어도 TCP 서버와 트레이를 먼저 시작하고
  5초마다 주소를 다시 확인한다. 주소가 생기거나 바뀌면 현재 주소의 페어링 QR을
  자동으로 표시·갱신한다.
- Kiosk RC46 이상은 저장된 주소 연결이 실패하면 현재 Wi-Fi의 같은
  `/24` 사설망에서 수신기를 찾고, 기존 페어링의 challenge-response 인증에
  성공한 PC만 새 주소로 저장한다. 자동 복구용 상태에는 학생 이름을 싣지
  않고 PC 알림도 만들지 않는다.
- 다른 Wi-Fi, 다른 `/24` 망, PC 방화벽 차단 또는 수신기 중지 상태에서는
  자동 복구하지 않는다. 이 경우 네트워크를 바로잡고 다시 시도하며, PC가
  실제로 교체됐거나 수신기 설정이 초기화됐다면 수동 재페어링한다.
- 0.1.8 수신기는 주소 변경 시 새 QR 확인을 알린다. 같은 `/24`에서 Kiosk의
  인증된 자동 복구가 성공하면 수동 재페어링은 필요 없고, 자동 복구가 되지
  않을 때만 새 QR로 다시 페어링한다.
- PC 교체·수신기 설정 초기화 뒤에는 `지정 PC 다시 페어링`을 수행한다.
- 방화벽을 전체 네트워크에 개방하거나 공용 네트워크 프로필을 허용하지 않는다.
- 수신기를 제거할 때는 `pc_receiver\uninstall-receiver.ps1`을 사용한다.
  실행 파일·자동 시작·방화벽 규칙은 제거하지만 이미 받은 PDF와 페어링
  설정은 보존한다.
- 2026-07-29 PC 완전 재부팅 뒤 수신기 자동 실행과 TCP 48129 대기를
  확인했다. 별도로 수신기를 수동 실행할 필요가 없다.

2026-07-29 실물 검증에서는 수신 PDF의 신규 QR로 로그인·문제 화면·채점
끝내기·`QR_READY` 왕복을 통과했고, 재발급 전 QR 무효화도 통과했다.

## ADB 전용 비공개 진단 로그

Kiosk rc45와 Web rc68부터 사용자 화면이나 공유 저장소에 진단 로그를
노출하지 않으면서, USB 디버깅을 승인한 관리 PC에서만 구조화된 최근
기록을 확인할 수 있다. receiver는 시스템 `android.permission.DUMP`로
보호되므로 일반 앱 UID의 요청은 Android가 거부한다.

```powershell
$nonce = [guid]::NewGuid().ToString('N').ToUpperInvariant()
adb -s R54TB029FHZ shell am broadcast `
  -a com.local.matholickiosk.kiosk.action.DUMP_PRIVATE_DIAGNOSTICS `
  --es nonce $nonce `
  -n com.local.matholickiosk.kiosk/.AdbDiagnosticDumpReceiver

$nonce = [guid]::NewGuid().ToString('N').ToUpperInvariant()
adb -s R54TB029FHZ shell am broadcast `
  -a com.local.matholickiosk.webpoc.action.DUMP_PRIVATE_DIAGNOSTICS `
  --es nonce $nonce `
  -n com.local.matholickiosk.webpoc/.AdbDiagnosticDumpReceiver
```

출력은 nonce가 포함된 `BEGIN`/`END` 경계 안의 최근 최대 200줄로 제한된다.
학생 이름·아이디·비밀번호·QR 원문·답안·점수·오답 번호·문항 내용이
나오면 정상 동작으로 간주하지 말고 해당 출력을 공유하지 않은 채 릴리스를
중단한다. 실제 장애를 만들기 위한 실패주입은 이 조회 절차에 포함되지 않는다.
