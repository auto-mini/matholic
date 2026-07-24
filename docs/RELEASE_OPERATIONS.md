# Release 서명·운영 전환

작성일: 2026-07-24 (Asia/Seoul)

## 현재 상태

운영 후보 소스와 APK는 만들었지만 A에는 배포하지 않았다.

- Kiosk: `0.5.0-rc01`/code 6
- Web POC: `0.3.5-rc01`/code 18
- signer SHA-256: `9d5bd7d9c328df2e5c54b67d1aa2d42caef2674eeace0614bfe2d37c7651f5b7`
- 현재 A: 기존 debug signer의 `0.5.0-alpha05`, Device Owner, `QR_READY`, `LOCKED`
- 휴대 가능한 release 키 복구본: **미확정**
- 생산 배포: **미수행**

debug signer와 release signer가 다르므로 현재 Device Owner 앱에 release APK를 덮어쓸 수 없다. release 전환에는 A 공장초기화와 새 Device Owner 등록이 필요하다.

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

로컬 창에 복구 비밀번호가 한 번 표시된다. 키 파일과 다른 장소에 보관한 뒤 체크하고 완료한다. 스크립트는 다음을 확인한다.

- 원본 키와 복사본 SHA-256 일치
- 공개 인증서 signer fingerprint 산출
- 비밀번호 별도 보관 사용자 확인
- 로컬 `portable-recovery-confirmed.json` marker 생성

`provision-release-device-owner.ps1`는 이 marker, 원본 키, 복구본과 release APK signer가 모두 일치하지 않으면 기기를 변경하지 않는다.

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

USB 디버깅을 끄기 전까지 생산 잠금 완료로 판정하지 않는다. 현재 별도 원격 업데이트 채널은 없으므로 향후 APK 업데이트 때는 관리자가 USB 디버깅을 일시 재활성화하거나 별도 MDM/업데이트 채널을 구축해야 한다.

## 실패와 롤백

- release 키 복구 확인 전: A를 건드리지 않고 현재 debug alpha를 유지
- release APK 검증 실패: 설치하지 않고 빌드 원인 수정
- 초기화 후 Device Owner 등록 실패: 계정·사용자 조건을 점검하고 다시 공장초기화
- release 등록 후 debug alpha로 복귀: 서명이 달라 덮어쓰기 불가, 공장초기화 필요
- release 키 또는 복구 비밀번호 분실: 기존 설치본 업데이트 불가, 공장초기화와 새 application/signing 전략 필요
