# 🚲 큐싱 방지 자전거 대여 앱 제작

- 이 문서는 자전거 대여 및 관리 시스템을 사용자가 보다 쉽게 사용할 수 있도록 구현된 어플리케이션입니다.
- Kotlin을 사용하여 코드 작성되었습니다.

---

## 📖 목차

### **기능 및 사용 라이브러리**
1. Zxing 라이브러리
2. Mediaplayer
3. PopupMenu
4. Toast 메시지
5. AlertDialog
6. 사진 촬영 기능

---

## 기능 및 사용 라이브러리

### **Zxing 라이브러리**
- QR 코드를 스캔하여 데이터를 처리
- QR 스캔 기능을 `IntentIntegrator`를 사용해 구현
- 유효성 검사 및 결과에 따라 화면 이동 및 경고음 재생
- **주요 함수**:
  - `startQRCodeScan()`: QR 코드 스캔 시작
  - `isFakeQRCode()`: QR 코드의 유효성 판단

---
### **Mediaplayer**
- QR 코드가 유효하지 않을 경우 경고음을 재생
- `Mediaplayer.create(Context, Int)`로 경고음 파일 호출
- 경고음 재생, 일시 중지, 해제 기능
- **주요 함수**:
  - `start()`: 경고음 재생
  - `pause()`: 재생 일시 중지
  - `stop()`: 재생 중단
  - `release()`: 리소스 해제

---

### **PopupMenu**
- 메뉴 버튼 및 팝업 기능을 제공
- 메뉴 항목 선택에 따른 동작 정의
- UI 텍스트 및 색상 커스터 마이징

---

### **Toast 메시지**
- 알림 메시지를 화면에 표시
- `Toast.makeText()`를 통해 간단한 메시지 출력

---

### **AlertDialog**
- 사용자 확인 요청 및 경고 메시지 제공
- 커스텀 스타일 및 확인 버튼 설정

---

### **사진 촬영 기능**
- 카메라를 이용한 사진 촬영 및 경로 저장
- `dispatchTakePictureIntent()`를 사용하여 사진 촬영 Intent 구현
