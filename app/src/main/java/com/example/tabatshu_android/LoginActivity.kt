package com.example.tabatshu_android

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 서버로 보낼 로그인 요청 데이터 클래스
data class LoginRequest(val username: String, val password: String)

// 서버로부터 받을 로그인 응답 데이터 클래스
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val role: String?,
    @SerializedName("tf_rent") val tfRent: Boolean? // tfRent는 사용자에만 적용되므로 Nullable
)

// Retrofit API 인터페이스 정의
interface LoginApiService {
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}

class LoginActivity : AppCompatActivity() {
    private lateinit var loginApi: LoginApiService

    fun showIconToast(context: Context, message: String, iconResId: Int) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)

        // 기본 레이아웃 가져오기
        val toastView = toast.view

        // 아이콘 추가 작업
        val imageView = ImageView(context)
        imageView.setImageResource(iconResId) // 원하는 아이콘 설정

        // 레이아웃에 아이콘 추가
        if (toastView is LinearLayout) {
            toastView.addView(imageView, 0) // 텍스트 왼쪽에 아이콘 추가
        }

        toast.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.20.10.3:5001") // 서버 주소
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        loginApi = retrofit.create(LoginApiService::class.java)

        val btnLogin = findViewById<ImageButton>(R.id.login_bt)
        val editTextId = findViewById<EditText>(R.id.login_id)
        val editTextPw = findViewById<EditText>(R.id.login_pw)

        btnLogin.setOnClickListener {
            val inputId = editTextId.text.toString().trim()
            val inputPw = editTextPw.text.toString().trim()

            if (inputId.isNotBlank() && inputPw.isNotBlank()) {
                val loginRequest = LoginRequest(username = inputId, password = inputPw)
                Log.d("LoginActivity", "Request body: $loginRequest")

                loginApi.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        val loginResponse = response.body()
                        Log.d("LoginActivity", "Server response: $loginResponse")

                        if (loginResponse?.success == true) {
                            when (loginResponse.role) {
                                "admin" -> {
                                    val intent = Intent(this@LoginActivity, ManagerHomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                "user" -> {
                                    GlobalVariables.user_id = inputId
                                    GlobalVariables.tfRent = loginResponse.tfRent ?: false
                                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                else -> {
                                    Toast.makeText(this@LoginActivity, "Invalid role", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            val errorMsg = loginResponse?.message ?: "Login failed"
                            Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Server error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", "Connection error: ${t.message}")
                    }
                })
            } else {
                showIconToast(this, "Please enter both username and password", R.drawable.toast)
            }
        }
    }
}
