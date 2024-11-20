package com.example.tabatshu_android

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ObjectcheckActivity : AppCompatActivity() {
    private lateinit var bikeContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_objectcheck)

        bikeContainer = findViewById(R.id.bikeContainer)
        fetchBikesFromServer()
    }

    // 서버에서 자전거 데이터를 가져오는 함수
    private fun fetchBikesFromServer() {
        val apiService = retrofit.create(ApiService::class.java)
        apiService.getBikes().enqueue(object : Callback<List<Bike>> {
            override fun onResponse(call: Call<List<Bike>>, response: Response<List<Bike>>) {
                if (response.isSuccessful) {
                    val bikes = response.body()
                    if (!bikes.isNullOrEmpty()) {
                        displayBikes(bikes)
                    } else {
                        Log.d("ObjectcheckActivity", "No bikes found")
                    }
                } else {
                    Log.e("ObjectcheckActivity", "Failed to fetch bikes: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Bike>>, t: Throwable) {
                Log.e("ObjectcheckActivity", "Network error: ${t.message}")
            }
        })
    }

    // 자전거 데이터를 UI에 표시하는 함수
    private fun displayBikes(bikes: List<Bike>) {
        bikeContainer.removeAllViews() // 기존 뷰 제거

        for (bike in bikes) {
            // CardView 생성
            val cardView = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16.dpToPx()) // 카드 간격 설정
                }
                radius = 16f
                setCardBackgroundColor(Color.parseColor("#FFD0A4")) // 연주황색 배경
                cardElevation = 8f
            }

            // 내부 텍스트 뷰
            val textView = TextView(this).apply {
                text = """
                    자전거 ID: ${bike.bikeId}
                    상태: ${bike.status}
                """.trimIndent()
                setTextColor(Color.BLACK)
                textSize = 16f
                setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
            }

            // 텍스트 뷰를 CardView에 추가
            cardView.addView(textView)

            // CardView를 컨테이너에 추가
            bikeContainer.addView(cardView)
        }
    }

    // dp를 px로 변환하는 확장 함수
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
