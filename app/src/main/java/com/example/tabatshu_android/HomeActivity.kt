package com.example.tabatshu_android

import android.content.Intent
import android.widget.TextView
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageButton
import android.view.MenuItem
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Toolbar 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 상태바 색상 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#FC9332") // 원하는 색상으로 변경
        }

        // 시스템 UI 플래그 설정 (아이콘 색상 조정)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 아이콘을 어두운 색으로 설정
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 자전거 찾기 버튼 참조 추가
        val findBikeButton = findViewById<ImageButton>(R.id.find_bike)
        findBikeButton.setOnClickListener {
            val intent = Intent(this, FindBikeActivity::class.java)
            startActivity(intent)
        }

        // QR 코드 버튼 추가
        val qrButton = findViewById<ImageButton>(R.id.rent_bike)
        qrButton.setOnClickListener {
            val intent = Intent(this, QRCodeScannerActivity::class.java)
            startActivity(intent)
        }

        // 메뉴 버튼 추가
        val menuButton = findViewById<ImageButton>(R.id.menu)
        menuButton.setOnClickListener {
            openOptionsMenu() // 메뉴 열기
        }

        // 신고 버튼 추가
        val cmtNotifyTextView = findViewById<TextView>(R.id.cmt_notify)
        cmtNotifyTextView.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
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
                // 신고하기 선택 시 ReportActivity로 이동
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
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
