package com.example.tabatshu_android

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView


class ManagerHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_home)

        // 상태바 색상 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#FC9332") // 원하는 색상으로 변경
        }

        // 시스템 UI 플래그 설정 (아이콘 색상 조정)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 아이콘을 어두운 색으로 설정
        }

        // 시스템 바 Insets 처리
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 각 TextView를 찾아서 클릭 리스너 설정
        val loginCheckTextView: TextView = findViewById(R.id.Logincheck)
        val objectCheckTextView: TextView = findViewById(R.id.objectcheck)
        val rescueCheckTextView: TextView = findViewById(R.id.rescuecheck)
        val logoutTextView: TextView = findViewById(R.id.logout)

        loginCheckTextView.setOnClickListener {
            val intent = Intent(this, LogincheckActivity::class.java)
            startActivity(intent)
        }

        objectCheckTextView.setOnClickListener {
            val intent = Intent(this, ObjectcheckActivity::class.java)
            startActivity(intent)
        }

        rescueCheckTextView.setOnClickListener {
            val intent = Intent(this, RescuecheckActivity::class.java)
            startActivity(intent)
        }

        logoutTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val menuButton: ImageButton = findViewById(R.id.menu2)

        menuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed() // 기본 뒤로가기 기능 호출
        finish() // 현재 액티비티를 종료하여 앱을 종료
    }
    // PopupMenu를 표시하는 메서드
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view, 0, 0, R.style.CustomPopupMenu)
        popupMenu.menuInflater.inflate(R.menu.adminmenu_option, popupMenu.menu)

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


    // 관리자 메뉴 항목 선택 시 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_login_manage -> {
                showToast("로그인 관리")
                val intent = Intent(this, LogincheckActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_object_manage -> {
                showToast("물품 관리")
                val intent = Intent(this, ObjectcheckActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_rescue_data_manage -> {
                showToast("신고 데이터 조회")
                val intent = Intent(this, RescuecheckActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_admin_logout -> {
                showToast("로그아웃")
                val intent = Intent(this, LoginActivity::class.java)
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


