package com.example.tabatshu_android

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_help)

        // 전화 걸기 버튼 설정
        val callbtn: ImageButton = findViewById(R.id.callbtn)
        callbtn.setOnClickListener {
            // 전화 걸기 전에 팝업 창 띄우기
            showConfirmationDialog()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#FC9332") // 상태바 색상 변경
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 메뉴 버튼 클릭 이벤트 설정
        val menuButton = findViewById<ImageButton>(R.id.menu)
        menuButton.setOnClickListener {
            showPopupMenu(it) // PopupMenu 열기
        }
    }

    // 전화 걸기 전 확인 팝업을 표시하는 메서드
    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("관리자에게 전화하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("확인") { _, _ ->
                // 전화번호를 "tel:"로 시작하는 문자열로 설정
                val phoneNumber = "1655"  // 임시로 프론트담당 번호 씀

                // 전화 걸기 인텐트 생성
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))

                // 전화 걸기 권한 확인
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent)  // 전화를 걸기
                } else {
                    // 권한이 없으면 권한 요청
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.CALL_PHONE), 1)
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()  // 팝업 닫기
            }

        val alert = builder.create()
        alert.show()
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
            menuItem.title = spannableTitle
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
                val intent = Intent(this, HelpActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_report -> {
                showToast("신고하기 선택")
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_help -> {
                showToast("고객센터 선택")
                val intent = Intent(this, ServiceImformationActivity::class.java)
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

