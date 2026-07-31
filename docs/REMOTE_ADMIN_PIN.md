# A 기기 관리자 PIN 임시 보관

관리자 PIN은 저장소 밖의 다음 파일에 Windows DPAPI 암호문으로만 보관한다.

`%LOCALAPPDATA%\MatholicRemote\admin-pin.dpapi`

- 현재 Windows 사용자와 A 기기 serial에 묶여 다른 사용자나 다른 기기에서
  그대로 사용할 수 없다.
- 평문 PIN은 콘솔, Git, 파일명과 ADB 프로세스 명령행에 출력하지 않는다.
- `enter-tablet-admin-pin.ps1 -RequireStoredPin`은 파일이 있을 때만 PIN 화면에
  숫자 키 이벤트를 보내며, 파일이 없거나 복호화에 실패하면 닫힌 상태로
  중단한다.
- Full Access로 같은 Windows 사용자 권한을 공유하므로 Codex와 PIN을
  암호학적으로 완전히 격리하는 장치는 아니다. 운영상 복호화 결과를 출력·
  기록·직접 조회하지 않고 전용 입력 도구로만 사용한다.
- 관리자 PIN 화면을 확인한 경우에만 전송한다. 화면·앱 상태 확인, Lock Task,
  원격 점검 만료와 기존 안전 경계는 그대로 유지한다.

## 보관

```powershell
.\scripts\save-tablet-admin-pin.ps1
```

두 번 입력한 값이 같을 때 기존 보관 파일을 원자적으로 교체한다.

## 자동 입력

```powershell
.\scripts\enter-tablet-admin-pin.ps1 -RequireStoredPin
```

## 즉시 폐기

```powershell
.\scripts\clear-tablet-admin-pin.ps1
```

보관 파일이 없어지면 자동 입력 도구는 PIN을 요구하거나, `-RequireStoredPin`
사용 시 아무 입력도 하지 않고 종료한다.
