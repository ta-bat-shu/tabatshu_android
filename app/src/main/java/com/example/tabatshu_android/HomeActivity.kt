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
import android.widget.PopupMenu
import android.widget.Toast
import android.text.SpannableString
import android.text.style.ForegroundColorSpan


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

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
            showPopupMenu(it) // PopupMenu 열기
        }

        // 신고 버튼 추가
        val cmtNotifyTextView = findViewById<TextView>(R.id.cmt_notify)
        cmtNotifyTextView.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
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
            onOptionsItemSelected(item)
        }
        popupMenu.show()
    }



    // 메뉴 항목 선택 시 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_login -> {
                showToast("나의 정보 선택")
                val intent = Intent(this, MyProfileActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_charge -> {
                showToast("충전하기 선택")
                val intent = Intent(this, ChargeActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_service -> {
                showToast("서비스 안내 선택")
                val intent = Intent(this, ServiceActivity::class.java)
                startActivity(intent)
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
                val intent = Intent(this, HelpActivity::class.java)
                startActivity(intent)
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

