package com.example.tabatshu_android

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.tabatshu_android.GlobalVariables.retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RescuecheckActivity : AppCompatActivity() {
    private lateinit var reportContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rescuecheck)

        reportContainer = findViewById(R.id.reportContainer)
        fetchReportsFromServer()
    }

    // 서버에서 신고 데이터를 가져오는 함수
    private fun fetchReportsFromServer() {
        val apiService = retrofit.create(ApiService::class.java)
        apiService.getReports().enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                if (response.isSuccessful) {
                    val reports = response.body()
                    if (!reports.isNullOrEmpty()) {
                        displayReports(reports)
                    } else {
                        Log.d("RescuecheckActivity", "No reports found")
                    }
                } else {
                    Log.e("RescuecheckActivity", "Failed to fetch reports: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Log.e("RescuecheckActivity", "Network error: ${t.message}")
            }
        })
    }

    // 신고 데이터를 UI에 표시하는 함수
    private fun displayReports(reports: List<Report>) {
        reportContainer.removeAllViews() // 기존 뷰 제거

        for (report in reports) {
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

            // 카드 내부 컨테이너
            val innerContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
            }

            // 이미지 뷰 생성 및 추가
            if (!report.image.isNullOrEmpty()) {
                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        200.dpToPx() // 화면 높이에 맞는 이미지 크기
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }

                Glide.with(this)
                    .load(report.image)
                    .into(imageView)

                innerContainer.addView(imageView)
            }

            // 신고 정보 텍스트 추가
            val infoTextView = TextView(this).apply {
                text = """
                날짜: ${report.date}
                자전거 ID: ${report.bikeId}
                신고 유형: ${report.category}
            """.trimIndent()
                setTextColor(Color.BLACK)
                textSize = 16f
                setPadding(0, 16.dpToPx(), 0, 10.dpToPx())
            }
            innerContainer.addView(infoTextView)

            // 신고 내용 부분만 흰색 배경을 가진 컨테이너
            val contentContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.WHITE) // 하얀색 배경
                setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 0) // 상단 여백 추가
                }
            }

            // 신고 내용 텍스트 추가
            val contentTextView = TextView(this).apply {
                text = "내용: ${report.contents}" // 내용 추가
                setTextColor(Color.BLACK)
                textSize = 16f
            }

            contentContainer.addView(contentTextView) // 흰색 배경 안에 내용 추가
            innerContainer.addView(contentContainer) // 흰색 배경 컨테이너를 내부 컨테이너에 추가

            // 내부 컨테이너를 CardView에 추가
            cardView.addView(innerContainer)

            // CardView를 컨테이너에 추가
            reportContainer.addView(cardView)
        }
    }


    // dp를 px로 변환하는 확장 함수
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
