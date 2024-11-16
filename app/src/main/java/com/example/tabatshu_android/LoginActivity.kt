package com.example.tabatshu_android

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import com.example.tabatshu_android.user_id
import com.example.tabatshu_android.retrofit

// 서버로 보낼 로그인 요청 데이터 클래스
data class LoginRequest(val username: String, val password: String)

// 서버로부터 받을 로그인 응답 데이터 클래스
data class LoginResponse(val success: Boolean, val message: String, val role: String)

// API 인터페이스 정의
//interface ApiService {
//
//}

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // 상태바 색상 및 시스템 UI 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#FC9332")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrofit 초기화
//        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:5000")  // 서버 주소
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()

        val api = retrofit.create(ApiService::class.java)

        // 로그인 버튼 클릭 이벤트
        val loginButton = findViewById<ImageButton>(R.id.login_bt)

        loginButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.login_id).text.toString()
            val password = findViewById<EditText>(R.id.login_pw).text.toString()

            // 서버로 전송할 로그인 요청 생성
            val loginRequest = LoginRequest(username, password)

            // 서버에 로그인 요청 보내기
            api.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val loginResponse = response.body()

                    if (loginResponse?.success == true) {
                        user_id = username
                        // 로그인 성공, 역할에 따라 다른 화면으로 이동
                        when (loginResponse.role) {
                            "admin" -> {
                                // 관리자 화면으로 이동
                                val intent = Intent(this@LoginActivity, AdminActivity::class.java)
                                startActivity(intent)
                                Toast.makeText(this@LoginActivity, "관리자 로그인 성공", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            "user" -> {
                                // 일반 사용자 화면으로 이동
                                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                startActivity(intent)
                                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            else -> {
                                Toast.makeText(this@LoginActivity, "잘못된 역할", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
