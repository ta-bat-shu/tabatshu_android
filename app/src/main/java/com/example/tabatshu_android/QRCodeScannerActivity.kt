package com.example.tabatshu_android

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.media.MediaPlayer
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tabatshu_android.GlobalVariables.user_id
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class QRCodeScannerActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var mediaPlayer: MediaPlayer

    // Bluetooth 관련 변수 설정
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private val BLUETOOTH_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val DEVICE_ADDRESS = "00:22:08:31:20:DF" // 블루투스 모듈의 MAC 주소로 변경

    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true) {
            connectToBluetoothDevice { }
        } else {
            Toast.makeText(this, "블루투스 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scanner)

        imageView = findViewById(R.id.qrCodeImageView)

        // MediaPlayer 초기화
        mediaPlayer = MediaPlayer.create(this, R.raw.alert)

        // QR 코드 스캔 시작
        startQRCodeScan()
    }

    private fun startQRCodeScan() {
        val integrator = IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("기기 대여하기")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
        }
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data) ?: return

        if (result.contents != null) {
            val scannedBikeId = result.contents
            URLValidationTask().execute(scannedBikeId)
        } else {
            Toast.makeText(this, "QR 코드 스캔에 실패했습니다.", Toast.LENGTH_LONG).show()
            navigateToHomeActivity()
        }
    }

    private inner class URLValidationTask : AsyncTask<String, Void, Pair<Int?, String?>>() {
        override fun doInBackground(vararg params: String?): Pair<Int?, String?> {
            val scannedBikeId = params[0] ?: return Pair(null, null)
            val url = URL("http://192.168.1.115:5000/check_bike")

            val jsonObject = JSONObject().apply {
                put("bike_id", scannedBikeId)
            }

            return try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                OutputStreamWriter(connection.outputStream).apply {
                    write(jsonObject.toString())
                    flush()
                }

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }
                Pair(responseCode, responseMessage)
            } catch (e: Exception) {
                Pair(null, null)
            }
        }

        override fun onPostExecute(result: Pair<Int?, String?>) {
            super.onPostExecute(result)
            val (responseCode, responseMessage) = result

            if (responseCode == 200 && responseMessage != null) {
                try {
                    val jsonResponse = JSONObject(responseMessage)
                    val status = jsonResponse.getString("status")
                    val bikeId = jsonResponse.optString("bike_id", null)

                    if (status == "success") {
                        // QR 코드가 정상일 경우 블루투스 연결 후 대여
                        connectToBluetoothDevice {
                            showRentalConfirmationDialog(bikeId ?: "")
                        }
                    } else {
                        handleInvalidQRCode()
                    }
                } catch (e: Exception) {
                    handleInvalidQRCode()
                }
            } else {
                handleInvalidQRCode()
            }
        }
    }

    private fun handleInvalidQRCode() {
        Toast.makeText(this, "잘못된 QR코드입니다(큐싱피해주의).", Toast.LENGTH_LONG).show()
        showReportConfirmationDialog()
    }

    private fun connectToBluetoothDevice(onConnected: () -> Unit) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestBluetoothPermissions()
                    return
                }
            }

            val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(DEVICE_ADDRESS)
            device?.let {
                bluetoothSocket = it.createRfcommSocketToServiceRecord(BLUETOOTH_UUID)
                bluetoothSocket?.connect()
                Toast.makeText(this, "블루투스 연결 성공", Toast.LENGTH_SHORT).show()
                onConnected()
            } ?: Toast.makeText(this, "블루투스 기기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Bluetooth 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBluetoothPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        }
    }

    private fun showRentalConfirmationDialog(bikeId: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("자전거 일련번호: $bikeId\n이 자전거를 대여하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("예") { dialog, _ ->
                RentBikeTask().execute(bikeId)
                dialog.dismiss()
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_LONG).show()
                navigateToHomeActivity()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("대여 확인")
        alert.show()
    }

    private fun showReportConfirmationDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("잘못된 QR 코드입니다. 신고를 접수하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("예") { dialog, _ ->
                dialog.dismiss()
                navigateToReportActivity()
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "홈 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
                navigateToHomeActivity()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("신고 접수 확인")
        alert.show()
    }

    private fun navigateToReportActivity() {
        val intent = Intent(this, ReportActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private inner class RentBikeTask : AsyncTask<String, Void, Int?>() {
        private var bikeId: String? = null // 멤버 변수로 선언

        override fun doInBackground(vararg params: String?): Int? {
            bikeId = params[0] // bikeId 저장
            val userId = GlobalVariables.user_id ?: return null // Null 체크

            val url = URL("http://192.168.1.115:5000/rent_bike")

            val jsonObject = JSONObject().apply {
                put("bike_id", bikeId)
                put("user_id", userId)
            }

            return try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                OutputStreamWriter(connection.outputStream).apply {
                    write(jsonObject.toString())
                    flush()
                }

                connection.responseCode
            } catch (e: Exception) {
                Log.e("QRCodeScannerActivity", "Error in RentBikeTask: ${e.message}")
                null
            }
        }

        override fun onPostExecute(responseCode: Int?) {
            super.onPostExecute(responseCode)
            if (responseCode == 200) {
                GlobalVariables.bike_id = bikeId // bikeId 저장
                Log.d("QRCodeScannerActivity", "bike_id set to: ${GlobalVariables.bike_id}, user_id: ${GlobalVariables.user_id}")
                Toast.makeText(this@QRCodeScannerActivity, "대여가 완료되었습니다.", Toast.LENGTH_LONG).show()
                navigateToRentedActivity()
            } else {
                Toast.makeText(this@QRCodeScannerActivity, "대여 실패: 서버 오류", Toast.LENGTH_LONG).show()
            }
        }

        private fun navigateToRentedActivity() {
            val intent = Intent(this@QRCodeScannerActivity, RentedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
