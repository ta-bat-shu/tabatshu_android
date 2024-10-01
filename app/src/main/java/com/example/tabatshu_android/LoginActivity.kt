package com.example.tabatshu_android

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import android.widget.ImageButton
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Headers

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String)

interface ApiService{
    @Headers("Content-Type: application/json")
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}


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

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.115:5000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiService::class.java)

        val loginButton = findViewById<ImageButton>(R.id.login_bt)

        loginButton.setOnClickListener {
            val username = findViewById<EditText>(R.id.login_id).text.toString()
            val password = findViewById<EditText>(R.id.login_pw).text.toString()
            // Create the login request
            val loginRequest = LoginRequest(username, password)

            // Make the API call
            api.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val loginResponse = response.body()
                if (loginResponse?.success == true) {
                    // Login successful, navigate to the next screen
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                    finish()
                    } else {
                    // Show error message
                    Toast.makeText(this@LoginActivity, loginResponse?.message?: "로그인 실패", Toast.LENGTH_SHORT).show()
                }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Failed to connect", Toast.LENGTH_SHORT).show()        }
            })}}}