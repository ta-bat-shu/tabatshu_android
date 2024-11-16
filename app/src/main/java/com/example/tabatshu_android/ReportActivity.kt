// ReportActivity.kt
package com.example.tabatshu_android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ReportActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var reportTypeEditText: EditText
    private lateinit var imageSelectEditText: EditText
    private lateinit var messageEditText: EditText

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)

        // Toolbar 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar3)
        setSupportActionBar(toolbar)

        // WindowInsets 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // EditText 초기화
        nameEditText = findViewById(R.id.editTextText)
        emailEditText = findViewById(R.id.editTextText2)
        reportTypeEditText = findViewById(R.id.editTextText3)
        imageSelectEditText = findViewById(R.id.editTextText4)
        messageEditText = findViewById(R.id.editTextText5)

        // 메뉴 버튼 설정
        val menuButton = findViewById<ImageButton>(R.id.menu5)
        menuButton.setOnClickListener {
            openOptionsMenu() // 메뉴 열기
        }

        // 이미지 선택 EditText 클릭 이벤트
        imageSelectEditText.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // 신고 버튼 추가
        val reportButton = findViewById<ImageButton>(R.id.report_button)

        reportButton.setOnClickListener {
            // Check for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_CODE)
                } else {
                    submitReport()
                }
            } else {
                // Handle the case for older versions
                submitReport()
            }
        }
    }

    private fun submitReport() {
        // 입력 값 가져오기
        val bikeId = emailEditText.text.toString().trim()
        val reportType = reportTypeEditText.text.toString().trim()
        val imageSelect = imageSelectEditText.text.toString().trim()
        val message = messageEditText.text.toString().trim()

        // 입력 검증
        if (bikeId.isEmpty() || reportType.isEmpty() || imageSelect.isEmpty() || message.isEmpty()) {
            showToast("모든 항목을 입력하세요.")
            return
        }

        // 신고내역 전송 로직 추가해야됨 (예: 서버에 전송)
        sendReport(bikeId, message, reportType, Uri.parse(imageSelect))

        // 신고 완료 Toast 메시지 표시
        showToast("신고가 완료되었습니다.")

        // 홈 화면으로 이동
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val imageUri: Uri? = data?.data
            imageSelectEditText.setText(imageUri.toString())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                submitReport()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendReport(bikeId: String, contents: String, reportType: String, imageUri: Uri?) {
        val bikeIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), bikeId)
        val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), user_id) // user_id는 전역 변수로 가정
        val categoryPart = RequestBody.create("text/plain".toMediaTypeOrNull(), reportType)
        val contentsPart = RequestBody.create("text/plain".toMediaTypeOrNull(), contents)

        var imagePart: MultipartBody.Part? = null
        imageUri?.let { uri ->
            val file = File(getRealPathFromURI(uri))
            if (file.exists()) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                Log.i("ReportActivity", "Image Part: ${file.name}")
            } else {
                Log.e("ReportActivity", "File does not exist: ${file.path}")
            }
        }

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.addReport(bikeIdPart, userIdPart, categoryPart, contentsPart, imagePart)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ReportActivity, "신고가 성공적으로 전송되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ReportActivity, "신고 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("ReportActivity", "Response error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ReportActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("ReportActivity", "Network error: ${t.message}")
            }
        })
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val idx = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val result = cursor?.getString(idx ?: 0)
        cursor?.close()
        return result ?: ""
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}