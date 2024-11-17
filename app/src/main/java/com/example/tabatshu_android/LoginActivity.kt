package com.example.tabatshu_android

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import android.widget.ImageButton
import android.widget.EditText


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

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

        // btn_login, EditText 참조 추가
        val btnLogin = findViewById<ImageButton>(R.id.login_bt)
        val editTextId = findViewById<EditText>(R.id.login_id)
        val editTextPw = findViewById<EditText>(R.id.login_pw)

        // 로그인 버튼 클릭 시 동작
        btnLogin.setOnClickListener {
            val inputId = editTextId.text.toString()
            val inputPw = editTextPw.text.toString()

            // admin/adminpassword로 로그인 시 ManagerHomeActivity로 이동
            if (inputId == "admin" && inputPw == "adminpassword") {
                val intent = Intent(this, ManagerHomeActivity::class.java)
                startActivity(intent)
            }
            // username/userpassword로 로그인 시 HomeActivity로 이동
            else if (inputId == "username" && inputPw == "userpassword") {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else {
                // 로그인 실패 시 Toast 메시지 표시
                val toast = Toast.makeText(applicationContext, "로그인 실패", Toast.LENGTH_SHORT)
                toast.setGravity(android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL, 0, 100)
                toast.show()
            }
        }
    }
}


