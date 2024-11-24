package com.example.tabatshu_android

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ArrayAdapter


class ReportActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var reportTypeSpinner: Spinner
    private lateinit var imageSelectEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var currentPhotoPath: String

    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_CAMERA_PERMISSION = 2
        const val REQUEST_SELECT_PHOTO = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)

        // 상태바 색상 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#FC9332") // 원하는 색상으로 변경
        }

        // 시스템 UI 플래그 설정 (아이콘 색상 조정)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 아이콘을 어두운 색으로 설정
        }

        // EditText 초기화
        nameEditText = findViewById(R.id.editTextText)
        emailEditText = findViewById(R.id.editTextText2)
        reportTypeSpinner = findViewById(R.id.reporttypebtn) // Spinner 초기화
        imageSelectEditText = findViewById(R.id.editTextText4)
        messageEditText = findViewById(R.id.editTextText5)

        // Spinner에 데이터 설정
        val reportTypes = arrayOf("신고 유형", "잠금장치 고장", "단말기 고장", "브레이크 고장", "바퀴고장", "기타")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reportTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reportTypeSpinner.adapter = adapter

        reportTypeSpinner.setSelection(0)  // 첫 번째 항목인 "신고 유형"을 기본값으로 선택

        // 메뉴 버튼 추가
        val menuButton = findViewById<ImageButton>(R.id.menu)
        menuButton.setOnClickListener {
            showPopupMenu(it) // PopupMenu 열기
        }

        // 신고 버튼 추가
        val reportButton = findViewById<ImageButton>(R.id.report_button)
        reportButton.setOnClickListener {
            // 입력 값 가져오기
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val reportType = reportTypeSpinner.selectedItem.toString().trim() // Spinner에서 선택한 값
            val imageSelect = imageSelectEditText.text.toString().trim()
            val message = messageEditText.text.toString().trim()

            // 입력 검증
            if (name.isEmpty() || email.isEmpty() || reportType.isEmpty() || imageSelect.isEmpty() || message.isEmpty()) {
                showToast("모든 항목을 입력하세요.")
                return@setOnClickListener
            }

            // 신고내역 전송 로직 추가해야됨 (예: 서버에 전송)

            // 신고 완료 Toast 메시지 표시
            showToast("신고가 완료되었습니다.")

            // 홈 화면으로 이동
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // 사진 촬영 버튼 추가
        val imgPhotoButton = findViewById<ImageButton>(R.id.imgphotobtn)
        imgPhotoButton.setOnClickListener {
            checkCameraPermissionAndDispatchTakePictureIntent()
        }

        // 사진 선택 버튼 추가
        val selectPhotoButton = findViewById<ImageButton>(R.id.imgphotobtn)
        selectPhotoButton.setOnClickListener {
            openGalleryForImage()
        }
    }

    private fun checkCameraPermissionAndDispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            // 권한이 이미 부여된 경우
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                // 사진을 저장할 파일 생성
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // 파일 생성 중 오류 발생시
                    null
                }
                // 파일이 정상적으로 생성된 경우만 계속 진행
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.tabatshu_android.fileprovider", // 패키지 이름으로 대체
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 이미지 파일 이름 생성
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        //디렉토리가 존재하지 않으면 생성
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File.createTempFile(
            "JPEG_${timeStamp}_", //접두어
            ".jpg", // 접미어
            storageDir // 디렉토리
        ).apply {
            // 파일 경로 지정 (ACTION_VIEW 인텐트에서 사용)
            currentPhotoPath = absolutePath
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_SELECT_PHOTO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 부여된 경우
                    dispatchTakePictureIntent()
                } else {
                    // 권한이 거부된 경우
                    showToast("카메라 권한이 필요합니다.")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                if (resultCode == RESULT_OK) {
                    // 사진이 성공적으로 촬영된 경우, 경로를 EditText에 설정
                    findViewById<EditText>(R.id.editTextText4).setText(currentPhotoPath)
                    showToast("사진이 저장되었습니다: $currentPhotoPath")
                }
            }
            REQUEST_SELECT_PHOTO -> {
                if (resultCode == RESULT_OK && data != null) {
                    val selectedImageUri: Uri? = data.data
                    // 선택된 사진의 URI를 EditText에 표시
                    findViewById<EditText>(R.id.editTextText4).setText(selectedImageUri.toString())
                    showToast("사진이 선택되었습니다.")
                }
            }
        }
    }

    // PopupMenu를 표시하는 메서드
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view, 0, 0, R.style.CustomPopupMenu)
        popupMenu.menuInflater.inflate(R.menu.menu_option, popupMenu.menu)

        // 메뉴 항목들의 텍스트 색상을 검정색으로 변경
        for (i in 0 until popupMenu.menu.size()) {
            val menuItem = popupMenu.menu.getItem(i)
            val spannableTitle = SpannableString(menuItem.title)
            spannableTitle.setSpan(ForegroundColorSpan(Color.BLACK), 0, spannableTitle.length, 0)
            menuItem.title = spannableTitle // 텍스트 색상을 검정으로 설정
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_login -> {
                    showToast("나의 정보 선택")
                    true
                }
                R.id.menu_item_charge -> {
                    showToast("충전하기 선택")
                    true
                }
                R.id.menu_item_service -> {
                    showToast("서비스 안내 선택")
                    true
                }
                R.id.menu_item_report -> {
                    // 신고하기 선택 시 ReportActivity로 이동
                    showToast("신고하기 선택")
                    val intent = Intent(this, ReportActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_item_help -> {
                    showToast("고객센터 선택")
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // 토스트 메시지를 띄우는 메서드
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}



