package com.example.tabatshu_android

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class QRCodeScannerActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scanner)

        imageView = findViewById(R.id.qrCodeImageView)

        // MediaPlayer 초기화
        mediaPlayer = MediaPlayer.create(this, R.raw.alert)

        // QR 코드 스캔 시작
        val integrator = IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("기기 대여하기")
            setCameraId(0) // 카메라 ID
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
        }
        integrator.initiateScan()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data) ?: return

        if (result.contents != null) {
            // QR 코드 내용 확인
            if (isFakeQRCode(result.contents)) {
                // 가짜 QR 코드인 경우
                Toast.makeText(this, "비정상적 QR코드입니다 (신종 사기 큐싱 피해 유의)", Toast.LENGTH_LONG).show()
                mediaPlayer.start() // 경고음 재생
            } else {
                // 스캔 성공
                Toast.makeText(this, "대여가 완료되었습니다.", Toast.LENGTH_LONG).show()

                // FindBikeActivity로 돌아가기
                val intent = Intent(this, FindBikeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // 플래그 추가
                startActivity(intent)
                finish() // 현재 액티비티 종료
            }
        } else {
            // 스캔 실패
            Toast.makeText(this, "비정상적 QR코드입니다 (신종 사기 큐싱 피해 유의)", Toast.LENGTH_LONG).show()
            mediaPlayer.start() // 경고음 재생
        }

        // FindBikeActivity로 돌아가기
        val intent = Intent(this, FindBikeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // 플래그 추가
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }

    private fun isFakeQRCode(contents: String): Boolean {
        // "FAKE"라는 문자열이 포함된 경우 가짜로 판단
        return contents.contains("FAKE")
    }

    override fun onDestroy() {
        super.onDestroy()
        // MediaPlayer 리소스 해제
        mediaPlayer.release()
    }
}


