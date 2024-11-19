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

class RescuecheckActivity : AppCompatActivity() {
    private lateinit var constraintLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rescuecheck)
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

        // 서버에서 신고가 들어온 개수 확인 후 컴포넌트 추가
        val reportCount = checkForReportFromServer() // 서버에서 신고 개수를 받아오는 메서드
        addRescueComponents(reportCount)  // 신고 개수만큼 컴포넌트 생성

        // 메뉴 버튼 설정
        val menuButton: ImageButton = findViewById(R.id.menu3)
        menuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    private fun checkForReportFromServer(): Int {
        // 서버에서 신고 개수를 받아오는 로직 (실제 서버 호출로 대체)
        return 3 // 예시로 3개의 신고가 들어온 경우
    }

    private fun addRescueComponents(reportCount: Int) {
        for (i in 0 until reportCount) {
            val rescue = ImageView(this).apply {
                id = View.generateViewId()
                setImageResource(R.drawable.rescue1)
            }

            val params = ConstraintLayout.LayoutParams(
                750, // width (픽셀 단위)
                450  // height (픽셀 단위)
            )

            rescue.layoutParams = params
            constraintLayout.addView(rescue)

            val constraintSet = ConstraintSet().apply {
                clone(constraintLayout)
                connect(rescue.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
                connect(rescue.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)

                // 수평 위치 조정 (중앙으로 설정)
                setHorizontalBias(rescue.id, 0.56f)

                // 첫 번째 컴포넌트는 부모의 TOP에 연결, 그 이후는 이전 컴포넌트의 BOTTOM에 연결
                if (i == 0) {
                    connect(rescue.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 120.dpToPx())
                } else {
                    connect(rescue.id, ConstraintSet.TOP, constraintLayout.getChildAt(i - 1).id, ConstraintSet.BOTTOM, 60.dpToPx())
                }

                // 수직 위치를 더 아래로 배치 (verticalBias 값을 더 낮게 설정)
                val verticalBias = 0.95f - (i * 0.05f) // 더 낮은 값으로 설정하여 컴포넌트를 더 아래로 배치
                setVerticalBias(rescue.id, verticalBias)
            }

            constraintSet.applyTo(constraintLayout)

            // 클릭 리스너 설정
            rescue.setOnClickListener {
                showReportDialog()  // 신고 데이터 팝업 다이얼로그 띄우기
            }
        }
    }

    private fun showReportDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog) // Custom 스타일 적용
        builder.setTitle("신고 상세 정보")
        builder.setMessage(
            "날짜 시간: 2024-11-16 12:30\n" +
                    "자전거 고유번호: ABC1234\n" +
                    "신고 유형: 잠금장치 고장\n"
        )
        builder.setPositiveButton("확인") { dialog, _ ->
            dialog.dismiss()  // 확인 버튼을 누르면 팝업창 닫기
        }

        val dialog = builder.create()

        // 배경 색상 변경
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // 투명 배경 설정
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFD0A4"))) // 원하는 배경 색상으로 설정

        dialog.show()
    }


    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
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

