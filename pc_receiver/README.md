# 매쓰홀릭 PDF 수신기

A 태블릿과 물리적으로 한 번 페어링한 이 PC만 QR 카드 PDF를 받을 수 있게 하는
로컬 네트워크 수신기다. 인터넷이나 외부 서버를 사용하지 않는다.

## 보안 경계

- 페어링 QR에 256비트 비밀키와 128비트 수신기 ID가 포함된다.
- PDF와 학생 이름은 AES-256-GCM으로 암호화·인증한다.
- ACK도 HMAC-SHA256으로 인증한다.
- PC의 `config.json`에는 페어링 비밀 원문이나 Base64 원문을 저장하지 않고
  현재 Windows 사용자 범위의 DPAPI 암호문만 저장한다. 기존 설정 v1은 새
  수신기가 처음 읽을 때 검증 후 v2로 원자적 교체한다.
- 5분을 벗어난 요청, 5MiB 초과 PDF, 위조 요청과 처리한 request ID의 재전송을
  거부한다.
- Windows 방화벽은 `Private` 프로필의 TCP 48129를 이 수신 실행 파일에만
  허용한다.
- 설치·제거 시 이 수신 실행 파일에 대해 Windows가 자동 생성한 광범위한
  인바운드 규칙도 제거한다. 설치 뒤에는 `Private`/TCP 48129 단일 규칙을
  다시 검증한다.
- 수신 파일은 `%USERPROFILE%\Downloads\Matholic QR Cards`에 저장한다.

페어링 QR은 로그인 가능한 학생 QR과 마찬가지로 외부에 공유하면 안 된다.
DPAPI 설정은 다른 Windows 사용자나 다른 PC에서 복호화할 수 없으므로 계정·PC를
바꾼 경우 수신기 설정을 새로 만들고 태블릿과 다시 페어링해야 한다.

## 네트워크 시작·주소 변경

- 0.1.6부터 사설 LAN 주소가 아직 없어도 수신 서버와 트레이를 먼저 시작한다.
  페어링 QR 영역은 연결 대기 상태를 표시하며 5초마다 주소를 다시 확인한다.
- 사설 IPv4 주소가 생기거나 실행 중 주소가 바뀌면 현재 주소로 페어링 QR을
  자동 생성·갱신한다. 주소가 바뀌면 수신기 창과 Windows 알림으로 새 QR 확인을
  안내한다.
- Kiosk RC46 이상은 같은 `/24` 사설망에서 기존 비밀키로 수신기를 인증해 주소를
  자동 복구할 수 있다. 이 복구가 성공하면 다시 페어링할 필요가 없고, 자동
  복구가 되지 않을 때만 새 QR을 촬영한다.
- 서버는 `0.0.0.0:48129`에 수신 대기하지만 페어링 QR에는 현재 선택된 사설
  IPv4 주소 하나만 들어간다. 다른 Wi-Fi·VPN·다중 어댑터 환경에서는 A와 PC가
  같은 사설망인지 확인한다.

## 개발 검증

```powershell
$env:PYTHONPATH = (Resolve-Path '.\src').Path
python -m pytest .\tests -q
```

독립 실행 파일은 OneDrive 밖의 임시 경로에서 만들고, 실행 중인 수신기에
합성 PDF를 암호화 전송해 인증 ACK·저장·정리까지 확인한 뒤 `artifacts`에
보관한다.

```powershell
.\build-receiver.ps1
```

설치와 방화벽 변경은 관리자 권한 PowerShell에서 실행한다.

```powershell
.\install-receiver.ps1 -ExecutablePath .\dist\MatholicPdfReceiver.exe
```

제거 스크립트는 실행 파일·자동 시작·방화벽 규칙을 제거하되 수신 PDF와
페어링 설정은 보존한다.
