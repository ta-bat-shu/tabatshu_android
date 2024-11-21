# 🚲 큐싱 방지 자전거 대여 앱 제작
<ul>
 <li> 이 문서는 자전거 대여 및 관리 시스템을 사용자가 보다 쉽게 사용할 수 있도록 구현된 어플리케이션입니다.</li>
 <li>Kotlin을 사용하여 코드 작성되었습니다.</li>
</ul>
<h2>📖 목차</h2>
<h3>기능 및 사용 라이브러리</h3>
<ol>
 <li>Zxing 라이브러리</li>
 <li>Mediaplayer</li>
 <li>PopupMenu</li>
 <li>Toast 메세지</li>
 <li>AlertDialog</li>
 <li>사진 촬영 기능</li>
</ol>
<h2>기능 및 사용 라이브러리</h2>
<h3>Zxing 라이브러리</h3>
<ul>
 <li>QR 코드를 스캔하여 데이터를 처리</li>
 <li>QR 스캔 기능을 IntentIntegrator를 사용해 구현</li>
 <li>유효성 검사 및 결과에 따라 화면 이동 및 경고음 재생</li>
 <li>startQRCodeScan() : QR 코드 스캔 시작, isFakeQRCode() : QR 코드의 유효성 판단</li>
</ul>
<h3>Mediaplayer</h3>
<ul>
 <li>QR 코드가 유효하지 않을 경우, 경고음을 재생</li>
 <li>Mediaplayer.create(Context, Int)로 경고음 파일 호출</li>
 <li>경고음 재생, 일시 중지, 해제 기능</li>
 <li>start(), pause(), stop(), release()</li>
</ul>
<h3>PopupMenu</h3>
<ul>
 <li>메뉴 버튼 및 팝업 기능을 제공</li>
 <li>메뉴 항목 선택에 따른 동작 정의</li>
 <li>UI 텍스트 및 색상 커스터 마이징</li>
</ul>
<h3>Toast 메세지</h3>
<ul>
 <li>알림 메세지를 화면에 표시</li>
 <li>Toast.makeText()를 통해 간단한 메세지 출력</li>
</ul>
<h3>AlertDialog</h3>
<ul>
 <li>사용자 확인 요청 및 경고 메세지 제공</li>
 <li>커스텀 스타일 및 확인 버튼 설정</li>
</ul>
<h3>사진 촬영 기능</h3>
<ul>
 <li>카메라를 이용한 사진 촬영 및 경로 저장</li>
 <li>dispatchTakePictureIntent()를 사용하여 사진 촬영 Intent 구현</li>
</ul>

