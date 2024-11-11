package com.example.tabatshu_android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.widget.Toolbar

class ReportActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var reportTypeEditText: EditText
    private lateinit var imageSelectEditText: EditText
    private lateinit var messageEditText: EditText

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

        // 신고 버튼 추가
        val reportButton = findViewById<ImageButton>(R.id.report_button) // 수정된 부분

        reportButton.setOnClickListener {
            // 입력 값 가져오기
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val reportType = reportTypeEditText.text.toString().trim()
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
    }

    // 메뉴 생성
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_option, menu) // 메뉴 XML 파일 연결
        return true
    }

    // 메뉴 항목 선택 시 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
                showToast("신고하기 선택")
                true
            }
            R.id.menu_item_help -> {
                showToast("고객센터 선택")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Toast 메시지 표시
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
