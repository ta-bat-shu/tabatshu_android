package com.example.tabatshu_android

import android.Manifest
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
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.tabatshu_android.GlobalVariables.retrofit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var reportTypeSpinner: Spinner
    private lateinit var imageSelectEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var currentPhotoPath: String
    private lateinit var photoURI: Uri

    companion object {
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_CAMERA_PERMISSION = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)

        // 상태바 색상 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#FC9332")
        }

        // 시스템 UI 플래그 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // EditText 및 Spinner 초기화
        nameEditText = findViewById(R.id.editTextText)
        emailEditText = findViewById(R.id.editTextText2)
        reportTypeSpinner = findViewById(R.id.reporttypebtn)
        imageSelectEditText = findViewById(R.id.editTextText4)
        messageEditText = findViewById(R.id.editTextText5)

        emailEditText.setText(GlobalVariables.user_id)
        // Spinner 데이터 설정
        // Spinner 데이터 설정
        val reportTypes = arrayOf("신고 유형", "잠금장치 고장", "큐싱 의심", "브레이크 고장", "바퀴 고장", "기타")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, reportTypes) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)

                // 기본값(힌트)일 때 연회색, 선택된 값일 때 #595959
                if (position == 0) {
                    textView.setTextColor(Color.parseColor("#A7A7A7")) // 연회색 (힌트 색상)
                } else {
                    textView.setTextColor(Color.parseColor("#595959")) // 입력된 값 색상
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)

                // 드롭다운에서 기본값(힌트)은 연회색, 나머지는 기본 색상
                if (position == 0) {
                    textView.setTextColor(Color.parseColor("#C0C0C0")) // 연회색 (힌트 색상)
                } else {
                    textView.setTextColor(Color.parseColor("#595959")) // 선택 가능한 값 색상
                }
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reportTypeSpinner.adapter = adapter
        reportTypeSpinner.setSelection(0) // 기본값 설정



        // 메뉴 버튼 및 신고 버튼 설정
        findViewById<ImageButton>(R.id.menu).setOnClickListener { showPopupMenu(it) }
        findViewById<ImageButton>(R.id.report_button).setOnClickListener { handleReportButtonClick() }

        // 사진 촬영 버튼 설정
        findViewById<ImageButton>(R.id.imgphotobtn).setOnClickListener {
            checkCameraPermissionAndDispatchTakePictureIntent()
        }
    }

    private fun handleReportButtonClick() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val reportType = reportTypeSpinner.selectedItem.toString().trim()
        val imageSelect = imageSelectEditText.text.toString().trim()
        val message = messageEditText.text.toString().trim()

        // 입력 검증
        if (name.isEmpty() || email.isEmpty() || reportType == "신고 유형" || imageSelect.isEmpty() || message.isEmpty()) {
            showToast("모든 항목을 입력하세요.")
            return
        }

        // 신고내역 전송 로직
        sendReport(name, email, reportType, message, photoURI)

        // 신고 완료 Toast 메시지 표시
        showToast("신고가 완료되었습니다.")

        // 홈 화면으로 이동
        startActivity(Intent(this, HomeActivity::class.java))
    }

    private fun checkCameraPermissionAndDispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.let {
                    photoURI = FileProvider.getUriForFile(
                        this,
                        "com.example.tabatshu_android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
            Log.d("ReportActivity", "Image file path created: $currentPhotoPath")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Log.d("ReportActivity", "Captured image path: $currentPhotoPath")
            imageSelectEditText.setText(currentPhotoPath)
            imageSelectEditText.setTextColor(Color.parseColor("#595959"))
            showToast("사진이 저장되었습니다: $currentPhotoPath")
        } else {
            Log.e("ReportActivity", "Failed to capture image")
        }
    }

    private fun sendReport(name: String, email: String, reportType: String, contents: String, imageUri: Uri?) {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryPart = reportType.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentsPart = contents.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageUri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val requestBody = inputStream?.readBytes()?.toRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", File(currentPhotoPath).name, requestBody!!)
        }

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.addReport(namePart, emailPart, categoryPart, contentsPart, imagePart)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showToast("신고가 성공적으로 전송되었습니다.")
                    val intent = Intent(this@ReportActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("신고 전송에 실패했습니다.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다.")
            }
        })
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view, 0, 0, R.style.CustomPopupMenu)
        popupMenu.menuInflater.inflate(R.menu.menu_option, popupMenu.menu)

        for (i in 0 until popupMenu.menu.size()) {
            val menuItem = popupMenu.menu.getItem(i)
            val spannableTitle = SpannableString(menuItem.title)
            spannableTitle.setSpan(ForegroundColorSpan(Color.BLACK), 0, spannableTitle.length, 0)
            menuItem.title = spannableTitle
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_login -> {
                    showToast("나의 정보 선택")
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
