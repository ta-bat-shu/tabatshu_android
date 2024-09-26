package com.example.tabatshu_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanContract
import android.widget.Button
import android.widget.Toast

class RentBikeActivity : AppCompatActivity() {

    // QR 스캔 런처 설정
    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            // QR 코드 결과 처리
            Toast.makeText(this, "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()
        } else {
            // 취소 시 처리
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rent_bike)

        val btnScan = findViewById<Button>(R.id.btn_scan_qr)

        // 버튼 클릭 시 QR 스캔 시작
        btnScan.setOnClickListener {
            val options = ScanOptions()
            options.setPrompt("QR 코드를 스캔하세요")
            options.setBeepEnabled(true)
            options.setOrientationLocked(true)
            qrLauncher.launch(options)
        }
    }
}
