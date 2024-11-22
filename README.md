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


## MVP 기능


### 1. 로그인 기능

- **기능 요약**: Retrofit을 사용하여 서버에 로그인 요청을 보내고, 사용자 역할에 따라 화면을 전환합니다. 입력값 검증 및 실패 시 토스트 메시지를 표시합니다.

#### 핵심 코드 (`LoginActivity.kt`)

```kotlin
// Retrofit 초기화
val retrofit = Retrofit.Builder()
    // 개인 PC에서 서버를 실행 중이며, 같은 Wi-Fi 네트워크로 연결된 클라이언트만 접속 가능
    // 인터넷 URL이 아니라 로컬 네트워크 IP를 사용 (외부 네트워크 접속 불가)
    .baseUrl("http://192.168.1.115:5000") // 서버의 주소
    .addConverterFactory(GsonConverterFactory.create()) // JSON 형식으로 변환
    .build()

loginApi = retrofit.create(LoginApiService::class.java)

// 로그인 요청 처리
btnLogin.setOnClickListener {
    val inputId = editTextId.text.toString().trim() // 입력된 아이디(학번) 가져오기
    val inputPw = editTextPw.text.toString().trim() // 입력된 비밀번호 가져오기

    // 입력값 검증
    if (inputId.isNotBlank() && inputPw.isNotBlank()) {
        val loginRequest = LoginRequest(username = inputId, password = inputPw) // 서버에 요청할 데이터 생성
        loginApi.login(loginRequest).enqueue(object : Callback<LoginResponse> { // 서버로 로그인 요청
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.body()?.success == true) {
                    when (response.body()?.role) { // 로그인 성공 시 사용자 역할에 따라 화면 전환
                        "admin" -> startActivity(Intent(this@LoginActivity, ManagerHomeActivity::class.java))
                        "user" -> startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    }
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) { // 예외처리
                Toast.makeText(this@LoginActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    } else {
        Toast.makeText(this, "아이디와 비밀번호를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
    }
}
```


### 2-1. 자전거 대여 기능 구현

- **기능 요약**: 
  - QR 코드 스캐너로 자전거 ID를 스캔하고, 데이터베이스에 저장된 정보와 비교하여 유효성을 검증합니다.
  - 잘못된 QR 코드 감지 시 사용자에게 알림을 제공하고, 악성 데이터 추가를 방지하기 위해 데이터베이스에 없는 데이터는 모두 무효로 처리합니다.
  - 성공 시 블루투스 장치를 통해 자전거 잠금을 해제하고 데이터베이스에서 사용자의 tf_rent와 자전거의 status가 대여중으로 바뀝니다.
    

#### 핵심 코드(`QRCodeScannerActicity.kt`)

```kotlin
// QR 코드 스캔 시작
private fun startQRCodeScan() {
    val integrator = IntentIntegrator(this).apply {
        setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // QR 코드만 스캔
        setPrompt("기기 대여하기") // 스캔 화면에 표시할 텍스트
        setCameraId(0) // 기본 카메라 사용
        setBeepEnabled(false) // 스캔 성공 시 비프음 비활성화
        setBarcodeImageEnabled(true) // 스캔한 QR 코드 이미지 저장
    }
    integrator.initiateScan()
}

// QR 코드 스캔 결과 처리
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data) ?: return

    if (result.contents != null) {
        val scannedBikeId = result.contents // 스캔된 QR 코드 데이터 (자전거 ID)
        URLValidationTask().execute(scannedBikeId) // QR 코드 검증 작업 시작
    } else {
        Toast.makeText(this, "QR 코드 스캔에 실패했습니다.", Toast.LENGTH_LONG).show()
        navigateToHomeActivity() // 홈 화면으로 복귀
    }
}

// QR 코드 유효성 검증 (서버와 통신)
private inner class URLValidationTask : AsyncTask<String, Void, Pair<Int?, String?>>() {
    override fun doInBackground(vararg params: String?): Pair<Int?, String?> {
        val scannedBikeId = params[0] ?: return Pair(null, null) // 스캔된 자전거 ID
        val url = URL("http://192.168.1.115:5000/check_bike") // 서버 URL

        val jsonObject = JSONObject().apply {
            put("bike_id", scannedBikeId) // 요청 데이터에 자전거 ID 추가
        }

        return try {
            // 서버와 HTTP 연결 설정
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST" // POST 요청
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json") // 요청 데이터 타입 설정

            // JSON 데이터를 서버로 전송
            OutputStreamWriter(connection.outputStream).apply {
                write(jsonObject.toString())
                flush()
            }

            // 서버 응답 코드와 메시지 반환
            val responseCode = connection.responseCode
            val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }
            Pair(responseCode, responseMessage)
        } catch (e: Exception) {
            Pair(null, null) // 연결 실패 시 null 반환
        }
    }

    override fun onPostExecute(result: Pair<Int?, String?>) {
        val (responseCode, responseMessage) = result

        if (responseCode == 200 && responseMessage != null) {
            try {
                val jsonResponse = JSONObject(responseMessage)
                val status = jsonResponse.getString("status") // 서버에서 반환한 상태
                val bikeId = jsonResponse.optString("bike_id", null) // 자전거 ID

                if (status == "success") {
                    // 유효한 QR 코드일 경우
                    connectToBluetoothDevice { showRentalConfirmationDialog(bikeId ?: "") }
                } else {
                    handleInvalidQRCode() // 유효하지 않은 QR 코드 처리
                }
            } catch (e: Exception) {
                handleInvalidQRCode() // JSON 파싱 오류 시 처리
            }
        } else {
            handleInvalidQRCode() // 서버 응답 실패 시 처리
        }
    }
}

// 유효하지 않은 QR 코드 처리
private fun handleInvalidQRCode() {
    Toast.makeText(this, "잘못된 QR코드입니다(큐싱피해주의).", Toast.LENGTH_LONG).show()
    showReportConfirmationDialog() // 신고 다이얼로그 표시
}

// 대여 확인 다이얼로그
private fun showRentalConfirmationDialog(bikeId: String) {
    val dialogBuilder = AlertDialog.Builder(this)
    dialogBuilder.setMessage("자전거 일련번호: $bikeId\n이 자전거를 대여하시겠습니까?")
        .setCancelable(false)
        .setPositiveButton("예") { dialog, _ ->
            RentBikeTask().execute(bikeId) // 대여 요청 작업 시작
            dialog.dismiss()
        }
        .setNegativeButton("아니오") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_LONG).show()
            navigateToHomeActivity() // 홈 화면으로 복귀
        }

    val alert = dialogBuilder.create()
    alert.setTitle("대여 확인")
    alert.show()
}

// 자전거 대여 요청 (서버와 통신)
private inner class RentBikeTask : AsyncTask<String, Void, Int?>() {
    override fun doInBackground(vararg params: String?): Int? {
        val bikeId = params[0] // 대여할 자전거 ID
        val userId = GlobalVariables.user_id ?: return null // 현재 사용자 ID
        val url = URL("http://192.168.1.115:5000/rent_bike") // 서버 URL

        val jsonObject = JSONObject().apply {
            put("bike_id", bikeId) // 자전거 ID 추가
            put("user_id", userId) // 사용자 ID 추가
        }

        return try {
            // 서버와 HTTP 연결 설정
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST" // POST 요청
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json") // 요청 데이터 타입 설정

            // JSON 데이터를 서버로 전송
            OutputStreamWriter(connection.outputStream).apply {
                write(jsonObject.toString())
                flush()
            }

            connection.responseCode // 서버 응답 코드 반환
        } catch (e: Exception) {
            null // 예외처리
        }
    }

    override fun onPostExecute(responseCode: Int?) {
        if (responseCode == 200) {
            // 대여 성공 시
            Toast.makeText(this@QRCodeScannerActivity, "대여가 완료되었습니다.", Toast.LENGTH_LONG).show()
            navigateToRentedActivity() // 대여 완료 화면으로 이동
        } else {
            // 대여 실패 시
            Toast.makeText(this@QRCodeScannerActivity, "대여 실패: 서버 오류", Toast.LENGTH_LONG).show()
        }
    }
}
```

### 2-2. 블루투스 연결 기능

- **기능 요약**: 
  - 자전거 대여 과정에서 블루투스를 통해 자전거의 잠금을 해제합니다.
  - 사용자의 스마트폰과 자전거의 블루투스 모듈 간 연결을 설정하며, 연결 실패 시 적절한 오류 메시지를 제공합니다.

#### 핵심 코드(`QRCodeScannerActicity.kt`)

```kotlin
// 블루투스 연결 시도
private fun connectToBluetoothDevice(onConnected: () -> Unit) {
    try {
        // Android 12 이상에서 블루투스 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothPermissions()
                return
            }
        }

        // 블루투스 디바이스 가져오기
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(DEVICE_ADDRESS)
        device?.let {
            bluetoothSocket = it.createRfcommSocketToServiceRecord(BLUETOOTH_UUID) // 소켓 생성
            bluetoothSocket?.connect() // 연결 시도
            Toast.makeText(this, "블루투스 연결 성공", Toast.LENGTH_SHORT).show()
            onConnected() // 연결 성공 시 실행할 콜백 호출
        } ?: Toast.makeText(this, "블루투스 기기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        Toast.makeText(this, "Bluetooth 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
    }
}

// 블루투스 권한 요청
private fun requestBluetoothPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        requestBluetoothPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        )
    }
}

// 아두이노로 데이터 전송
private fun sendDataToArduino(data: String) {
    try {
        bluetoothSocket?.outputStream?.write(data.toByteArray()) // 데이터 전송
        Log.d("QRCodeScannerActivity", "Data sent to Arduino: $data")
    } catch (e: IOException) {
        Toast.makeText(this, "아두이노로 데이터 전송 실패", Toast.LENGTH_SHORT).show()
        Log.e("QRCodeScannerActivity", "Error sending data to Arduino: ${e.message}")
    }
}
```

### 3. 자전거 반납 기능 구현

- **기능 요약**:
  - 사용자와 자전거 정보를 서버에 전송하여 반납 요청을 처리합니다.
  - 서버가 요청을 성공적으로 처리하면 블루투스를 통해 자전거 잠금을 해제하고 반납 완료 상태를 아두이노로 전송합니다.
  - 반납 완료 후 블루투스 연결을 해제합니다.

#### 핵심 코드(`RentedActivity.kt`)

```kotlin
// 자전거 반납 요청
private fun returnBikeToServer() {
    val bikeId = GlobalVariables.bike_id // 대여 중인 자전거 ID
    val userId = GlobalVariables.user_id // 현재 사용자 ID

    if (bikeId.isNullOrBlank() || userId.isNullOrBlank()) {
        Toast.makeText(this, "자전거 또는 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        return
    }

    // 서버로 반납 요청 비동기 처리
    thread {
        try {
            val url = URL("http://192.168.1.115:5000/return_bike") // 서버 URL
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST" // POST 요청
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json") // JSON 요청 타입

            // 서버로 전송할 데이터 생성
            val jsonObject = JSONObject().apply {
                put("bike_id", bikeId)
                put("user_id", userId)
            }

            // JSON 데이터 전송
            OutputStreamWriter(connection.outputStream).apply {
                write(jsonObject.toString())
                flush()
            }

            val responseCode = connection.responseCode // 서버 응답 코드 확인

            // UI 스레드에서 결과 처리
            runOnUiThread {
                if (responseCode == 200) {
                    sendDataToArduino("R") // 반납 완료 후 아두이노로 "R" 전송
                    disconnectBluetooth() // 블루투스 연결 해제
                    Toast.makeText(this, "자전거가 성공적으로 반납되었습니다.", Toast.LENGTH_SHORT).show()
                    GlobalVariables.bike_id = null // 대여 중인 자전거 정보 초기화
                    finish() // 현재 화면 종료
                } else {
                    Toast.makeText(this, "반납 실패: 서버 오류 ($responseCode)", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            // 오류 발생 시 처리
            runOnUiThread {
                Toast.makeText(this, "반납 중 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// 아두이노로 데이터 전송
private fun sendDataToArduino(data: String) {
    try {
        if (bluetoothSocket?.isConnected == true) { // 블루투스 연결 상태 확인
            bluetoothSocket?.outputStream?.write(data.toByteArray()) // 데이터 전송
            Log.d("RentedActivity", "Data sent to Arduino: $data")
        } else {
            Log.e("RentedActivity", "Bluetooth socket is not connected")
        }
    } catch (e: IOException) {
        Log.e("RentedActivity", "Error sending data to Arduino: ${e.message}")
        Toast.makeText(this, "아두이노로 데이터 전송 실패", Toast.LENGTH_SHORT).show()
    }
}

// 블루투스 연결 해제
private fun disconnectBluetooth() {
    try {
        bluetoothSocket?.close() // 블루투스 소켓 닫기
        bluetoothSocket = null
        Log.d("RentedActivity", "Bluetooth connection closed")
    } catch (e: IOException) {
        Log.e("RentedActivity", "Error disconnecting Bluetooth: ${e.message}")
    }
}
```

### 4. 관리자 화면에서 데이터 조회 기능 구현

- **기능 요약**:
  - 서버에서 신고 데이터, 사용자 정보, 자전거 상태 정보 등을 불러와 관리자 화면에서 조회할 수 있습니다.
  - 데이터를 보기 쉽게 CardView 형태로 표현하며, 신고 정보에는 이미지와 텍스트 정보를 포함합니다.
  - 이와 동일한 로직을 다른 데이터(예: 사용자 정보, 자전거 대여/반납 기록)에도 재활용할 수 있습니다.

#### 핵심 코드(`RescuecheckActivity.kt`)
#### 신고 데이터 예시
```kotlin
// 서버에서 신고 데이터를 가져오는 함수
private fun fetchReportsFromServer() {
    val apiService = retrofit.create(ApiService::class.java)
    apiService.getReports().enqueue(object : Callback<List<Report>> {
        override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
            if (response.isSuccessful) {
                val reports = response.body()
                if (!reports.isNullOrEmpty()) {
                    displayReports(reports) // 신고 데이터 표시
                } else {
                    Log.d("RescuecheckActivity", "No reports found")
                }
            } else {
                Log.e("RescuecheckActivity", "Failed to fetch reports: ${response.errorBody()?.string()}")
            }
        }

        override fun onFailure(call: Call<List<Report>>, t: Throwable) {
            Log.e("RescuecheckActivity", "Network error: ${t.message}")
        }
    })
}

// 신고 데이터를 UI에 표시하는 함수
private fun displayReports(reports: List<Report>) {
    reportContainer.removeAllViews() // 기존 뷰 제거

    for (report in reports) {
        // CardView 생성
        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16.dpToPx()) // 카드 간격 설정
            }
            radius = 16f
            setCardBackgroundColor(Color.parseColor("#FFD0A4")) // 연주황색 배경
            cardElevation = 8f
        }

        // 카드 내부 컨테이너
        val innerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }

        // 이미지 뷰 생성 및 추가
        if (!report.image.isNullOrEmpty()) {
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    200.dpToPx() // 화면 높이에 맞는 이미지 크기
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            Glide.with(this)
                .load(report.image) // 신고 이미지 로드
                .into(imageView)

            innerContainer.addView(imageView)
        }

        // 신고 정보 텍스트 추가
        val infoTextView = TextView(this).apply {
            text = """
                날짜: ${report.date}
                자전거 ID: ${report.bikeId}
                신고 유형: ${report.category}
            """.trimIndent()
            setTextColor(Color.BLACK)
            textSize = 16f
            setPadding(0, 16.dpToPx(), 0, 10.dpToPx())
        }
        innerContainer.addView(infoTextView)

        // 신고 내용 컨테이너 추가
        val contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE) // 하얀색 배경
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 0)
            }
        }

        // 신고 내용 텍스트 추가
        val contentTextView = TextView(this).apply {
            text = "내용: ${report.contents}" // 내용 추가
            setTextColor(Color.BLACK)
            textSize = 16f
        }
        contentContainer.addView(contentTextView)
        innerContainer.addView(contentContainer)

        // 내부 컨테이너를 CardView에 추가
        cardView.addView(innerContainer)

        // CardView를 컨테이너에 추가
        reportContainer.addView(cardView)
    }
}

// dp를 px로 변환하는 확장 함수
private fun Int.dpToPx(): Int {
    return (this * resources.displayMetrics.density).toInt()
}

