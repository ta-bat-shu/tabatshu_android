package com.example.tabatshu_android

import android.animation.ObjectAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 전체 화면 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 애니메이션 시작
        startBikeAnimation()
    }

    private fun startBikeAnimation() {
        // bike_w 이미지 애니메이션 설정
        val bikeImage = findViewById<ImageView>(R.id.bike_w)
        val bikeAnimator = ObjectAnimator.ofFloat(bikeImage, "translationX", 500f)
        bikeAnimator.duration = 3000  // 3초 동안 오른쪽으로 500px 이동
        bikeAnimator.startDelay = 1500 // 1.5초 딜레이

        // 애니메이션 리스너 추가 - 애니메이션 끝난 후 로그인 화면으로 전환
        bikeAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // 애니메이션이 끝난 후 LoginActivity로 이동
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish() // MainActivity 종료
            }
        })

        // 애니메이션 시작
        bikeAnimator.start()
    }
}
