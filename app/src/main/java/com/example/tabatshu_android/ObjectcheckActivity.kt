package com.example.tabatshu_android

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.widget.TextView

class ObjectcheckActivity : AppCompatActivity() {
    private lateinit var constraintLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_objectcheck)
        constraintLayout = findViewById(R.id.main)

        // 상태바 색상 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#FC9332") // 원하는 색상으로 변경
        }

        // 시스템 UI 플래그 설정 (아이콘 색상 조정)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 아이콘을 어두운 색으로 설정
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 서버에서 자전거 이미지 및 대여 정보를 가져오기
        // fetchBikeInfo()

        // 메뉴 버튼 설정
        val menuButton: ImageButton = findViewById(R.id.menu4)
        menuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }
    /*
    // Retrofit 설정
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://your.api.base.url") // 실제 API URL로 변경
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    // 자전거 정보 가져오기
    private fun fetchBikeInfo() {
        apiService.getBikeInfo().enqueue(object : Callback<BikeInfo> {
            override fun onResponse(call: Call<BikeInfo>, response: Response<BikeInfo>) {
                if (response.isSuccessful) {
                    val bikeInfo = response.body()
                    if (bikeInfo != null) {
                        displayBikeInfo(bikeInfo)
                    }
                } else {
                    showToast("Failed to get bike info")
                }
            }

            override fun onFailure(call: Call<BikeInfo>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    // 자전거 정보 화면에 표시
    private fun displayBikeInfo(bikeInfo: BikeInfo) {
        // 자전거 이미지 로드 (Glide 사용)
        Glide.with(this)
            .load(bikeInfo.imageUrl) // 서버에서 자전거 이미지 URL 가져오기
            .into(bikeImageView)

        // 반납 기간 및 대여반납 D-18 표시
        returnDateTextView.text = "반납기간: ${bikeInfo.returnDate}"
        rentalCountdownTextView.text = "대여반납 D-${bikeInfo.daysUntilReturn}"
    }

    // Toast 메시지 표시
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }*/

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
    // API 인터페이스 정의
    /*interface ApiService {
        @GET("path/to/bike/info") // 실제 엔드포인트로 변경
        fun getBikeInfo(): Call<BikeInfo>
    }

    // 데이터 모델
    data class BikeInfo(
        val imageUrl: String, // 자전거 이미지 URL
        val returnDate: String, // 반납일 (예: "24/11/20")
        val daysUntilReturn: Int // 대여 반납 D-18 (예: 18)
    )*/
}