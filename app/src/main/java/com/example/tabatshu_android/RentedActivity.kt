package com.example.tabatshu_android

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.concurrent.thread

class RentedActivity : AppCompatActivity() {

    // Bluetooth 관련 변수 설정
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private val BLUETOOTH_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rented)

        // 상태바 색상 변경
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#FC9332")
        }

        // 시스템 UI 플래그 설정 (아이콘 색상 조정)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 자전거 찾기 버튼 참조 추가
        val findBikeButton = findViewById<ImageButton>(R.id.find_bike)
        findBikeButton.setOnClickListener {
            val intent = Intent(this, FindBikeActivity::class.java)
            startActivity(intent)
        }

        // 메뉴 버튼 추가
        val menuButton = findViewById<ImageButton>(R.id.menu)
        menuButton.setOnClickListener {
            showPopupMenu(it) // PopupMenu 열기
        }

        // 신고 버튼 추가
        val cmtNotifyTextView = findViewById<TextView>(R.id.cmt_notify)
        cmtNotifyTextView.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }
        // 반납 버튼 설정
        val returnButton = findViewById<ImageButton>(R.id.rented_bike)
        returnButton.setOnClickListener {
            returnBikeToServer()
        }
    }
    // PopupMenu를 표시하는 메서드
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view, 0, 0, R.style.CustomPopupMenu)
        popupMenu.menuInflater.inflate(R.menu.menu_option, popupMenu.menu)

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

    // 메뉴 항목 선택 시 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_login -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_charge -> {
                val intent = Intent(this, ChargeActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_service -> {
                val intent = Intent(this, ServiceActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_report -> {
                // 신고하기 선택 시 ReportActivity로 이동
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_item_help -> {
                val intent = Intent(this, HelpActivity::class.java)
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


    // 자전거 반납 요청
    private fun returnBikeToServer() {
        val bikeId = GlobalVariables.bike_id
        val userId = GlobalVariables.user_id

        if (bikeId.isNullOrBlank() || userId.isNullOrBlank()) {
            Toast.makeText(this, "자전거 또는 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        thread {
            try {
                val url = URL("http://192.168.1.115:5000/return_bike")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                val jsonObject = JSONObject().apply {
                    put("bike_id", bikeId)
                    put("user_id", userId)
                }

                OutputStreamWriter(connection.outputStream).apply {
                    write(jsonObject.toString())
                    flush()
                }

                val responseCode = connection.responseCode
                runOnUiThread {
                    if (responseCode == 200) {
                        sendDataToArduino("R") // 반납 완료 후 "R" 전송
                        disconnectBluetooth() // 블루투스 연결 해제
                        Toast.makeText(this, "자전거가 성공적으로 반납되었습니다.", Toast.LENGTH_SHORT).show()
                        GlobalVariables.bike_id = null
                        finish()
                    } else {
                        Toast.makeText(this, "반납 실패: 서버 오류 ($responseCode)", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "반납 중 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 아두이노로 데이터 전송
    private fun sendDataToArduino(data: String) {
        try {
            // 기존 블루투스 연결을 활용하여 데이터 전송
            if (bluetoothSocket?.isConnected == true) {
                bluetoothSocket?.outputStream?.write(data.toByteArray())
                Log.d("RentedActivity", "Data sent to Arduino: $data")
            } else {
                Log.e("RentedActivity", "Bluetooth socket is not connected")
            }
        } catch (e: IOException) {
            Log.e("RentedActivity", "Error sending data to Arduino: ${e.message}")
            Toast.makeText(this, "아두이노로 데이터 전송 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 블루투스 연결 해제
    private fun disconnectBluetooth() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
            Log.d("RentedActivity", "Bluetooth connection closed")
        } catch (e: IOException) {
            Log.e("RentedActivity", "Error disconnecting Bluetooth: ${e.message}")
        }
    }
}
