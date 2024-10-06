package com.example.tabatshu_android

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.example.tabatshu_android.R


class QRCodeScannerActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scanner)

        imageView = findViewById(R.id.qrCodeImageView)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) // 상위 클래스 메서드 호출

        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // 스캔 취소
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                // 스캔한 QR 코드의 내용
                Toast.makeText(this, "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()
                // QR 코드 내용을 ImageView에 표시할 수 있음
                // imageView.setImageBitmap(result.bitmap) // Bitmap을 사용하고 싶으면 추가
            }
        }
    }
}
