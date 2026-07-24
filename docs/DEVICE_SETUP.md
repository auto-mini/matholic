# 기기 A 설치·설정 절차

## 확인된 기준값

2026-07-21 ADB 실측 기준이다.

- Samsung SM-P610, Android 13, One UI 5.1.1
- 매쓰홀릭 `com.matholic.mathapp`, 7.5.1 (`770961`)
- 물리 화면 1200×2000, 현재 가로 논리 화면 2000×1200
- 앱 영역 2000×1128, density 240 dpi, font scale 1.1
- 화면 자동 꺼짐 30분, 가로 회전 고정
- 부트로더 잠김, verified boot green

기준값은 환경 변경 감지용이며 좌표 selector가 아니다. `scripts/collect-device-baseline.ps1`은 계정·화면·로그를 수집하지 않는다.

## USB/설치

1. 개발자 옵션에서 USB 디버깅을 켠다.
2. USB 사용 목적은 **파일 전송**이어도 된다. ADB 인증이 더 중요하다.
3. PC 인증 지문 대화상자가 나타나면 이 PC 허용을 선택한다.
4. PC에서 `adb devices -l`의 상태가 `device`인지 확인한다.
5. `scripts/build.ps1`, 이어서 `scripts/install-probe.ps1`을 실행한다.

`unauthorized`면 케이블을 다시 연결하고 태블릿 잠금을 해제한다. 계속 대화상자가 없으면 개발자 옵션의 **USB 디버깅 권한 승인 취소** 후 USB 디버깅을 껐다 켜고 다시 연결한다.

## 제한된 설정 허용

Android 13에서 sideload 앱의 접근성 토글이 회색이거나 “보안을 위해 현재 사용할 수 없음”이 표시될 때만 수행한다.

1. **설정 → 애플리케이션 → 매쓰홀릭 접근성 조사기**로 이동한다.
2. 오른쪽 위 **⋮** 메뉴를 연다.
3. **제한된 설정 허용**을 누르고 기기 잠금 확인을 완료한다.
4. Probe로 돌아가 **접근성 설정 열기**를 누른다.
5. **설치된 앱 → 매쓰홀릭 조사 서비스 → 사용**을 켠다.
6. Android 경고 내용을 읽고 사용자 본인이 허용한다.

메뉴가 없고 접근성 토글이 정상 작동하면 이 단계는 필요 없다. ADB로 `settings secure`를 쓰거나 보안 검사를 비활성화하지 않는다.

## Gate 1 운영

- 시험계정만 사용하고 값은 태블릿에 직접 입력한다.
- 다른 학생 목록, 점수, 답안이 보이는 화면은 캡처하지 않는다.
- Probe 하단 버튼으로 redacted 노드만 캡처한다.
- 조사 후 접근성 서비스를 끄고, 내보낸 보고서 확인 뒤 앱 내부 보고서를 삭제할 수 있다.

## Web POC 이관

공식 웹 경로의 Gate 1과 Web POC 구현은 별도로 승인됐다. A를 연결한 뒤 다음 한 명령으로 모델 확인, 전체 빌드, 비민감 기준정보, 계측시험 15개, 재설치와 `IDLE` 확인을 수행한다.

```powershell
.\scripts\prepare-a-webpoc.ps1
```

Web POC에는 접근성 권한이나 overlay가 필요 없다. 시험 자격정보는 스크립트가 성공한 뒤 A 화면에서만 직접 입력한다. 세부사항은 `DEVICE_A_WEBPOC_HANDOFF.md`를 따른다.

## Android 접근성 POC 설정

Android 앱 접근성 Gate 1은 최종 FAIL이므로 기존 `poc` 잠금을 해제하지 않는다.

## Gate 5 전용기기 등록

Gate 5는 사용자 승인으로 A를 공장초기화한 뒤 Device Owner로 등록한다. 초기 설정에서는 백업 복원, Google/Samsung 계정 추가와 보조 사용자를 모두 건너뛴다. 홈 화면 도달 뒤 개발자 옵션과 USB 디버깅을 다시 켜고 실행한다.

```powershell
.\scripts\provision-gate5-device-owner.ps1 -Serial R54TB029FHZ
```

스크립트가 `Lock Task mode: LOCKED`를 출력하면 태블릿에서 관리자 PIN을 새로 설정한다. 학생과 시험계정도 새로 등록하고 이전 QR은 사용하지 않는다. 상세 경계와 복구 절차는 `GATE5_IMPLEMENTATION.md`를 따른다.

## Release 운영 전환

현재 debug Device Owner 설치본과 release signer는 다르므로 덮어쓰지 않는다. release 키 복구 확인과 RC APK 검증을 완료한 뒤 A를 다시 공장초기화하고 `scripts\provision-release-device-owner.ps1`로 등록한다. release signer, 복구본, 초기화 조건과 USB 디버깅 제거 절차는 `RELEASE_OPERATIONS.md`를 따른다.
