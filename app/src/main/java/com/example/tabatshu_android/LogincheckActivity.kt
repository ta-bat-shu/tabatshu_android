package com.example.tabatshu_android

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.widget.TextView
import com.example.tabatshu_android.GlobalVariables.retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogincheckActivity : AppCompatActivity() {
    private lateinit var userContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logincheck)

        userContainer = findViewById(R.id.userContainer)
        fetchLoginRecordsFromServer()
    }

    private fun fetchLoginRecordsFromServer() {
        val apiService = retrofit.create(ApiService::class.java)
        apiService.getLoginRecords().enqueue(object : Callback<List<LoginRecord>> {
            override fun onResponse(call: Call<List<LoginRecord>>, response: Response<List<LoginRecord>>) {
                if (response.isSuccessful) {
                    val loginRecords = response.body()
                    if (!loginRecords.isNullOrEmpty()) {
                        displayLoginRecords(loginRecords)
                    } else {
                        Log.d("LogincheckActivity", "No login records found")
                    }
                } else {
                    Log.e("LogincheckActivity", "Failed to fetch login records: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<LoginRecord>>, t: Throwable) {
                Log.e("LogincheckActivity", "Network error: ${t.message}")
            }
        })
    }

    private fun displayLoginRecords(records: List<LoginRecord>) {
        userContainer.removeAllViews() // 기존 뷰 제거

        for (record in records) {
            // CardView 생성
            val cardView = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 0)
                }
                radius = 16f
                setCardBackgroundColor(Color.parseColor("#FFD0A4")) // 연주황색 배경
                cardElevation = 8f
            }

            // TextView 생성
            val textView = TextView(this).apply {
                text = "사용자 ID: ${record.userId}" // 사용자 ID만 표시
                setTextColor(Color.BLACK)
                textSize = 16f
                setPadding(24.dpToPx(), 24.dpToPx(), 24.dpToPx(), 24.dpToPx())
            }

            // TextView를 CardView에 추가
            cardView.addView(textView)

            // CardView를 컨테이너에 추가
            userContainer.addView(cardView)
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
