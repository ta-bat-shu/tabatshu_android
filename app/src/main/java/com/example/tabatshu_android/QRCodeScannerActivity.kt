package com.example.tabatshu_android

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    // Bluetooth 관련 변수 설정
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private val BLUETOOTH_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val DEVICE_ADDRESS = "00:22:08:31:20:DF" // 블루투스 모듈의 MAC 주소로 변경

    // 런타임 권한 요청을 위한 요청 코드
    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true) {
            connectToBluetoothDevice()
        } else {
            Toast.makeText(this, "블루투스 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scanner)

        imageView = findViewById(R.id.qrCodeImageView)

        // 권한 요청 및 Bluetooth 연결
        if (checkBluetoothPermissions()) {
            connectToBluetoothDevice()
        } else {
            requestBluetoothPermissions()
        }

        // QR 코드 스캔 시작
        val integrator = IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("URL을 스캔하세요")
            setCameraId(0)
            setBeepEnabled(false)
            setBarcodeImageEnabled(true)
        }
        integrator.initiateScan()
    }

    private fun checkBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 12 이하에서는 해당 권한이 필요하지 않음
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

    private fun connectToBluetoothDevice() {
        try {
            // Android 12 이상인 경우 Bluetooth 연결 권한 확인
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                if (!permissionGranted) {
                    Toast.makeText(this, "Bluetooth 연결 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    requestBluetoothPermissions() // 권한 요청 메서드 호출
                    return
                }
            }

            // 권한이 확인되면 Bluetooth 연결 시도
            val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(DEVICE_ADDRESS)
            device?.let {
                bluetoothSocket = it.createRfcommSocketToServiceRecord(BLUETOOTH_UUID)
                bluetoothSocket?.connect()
                Toast.makeText(this, "블루투스 연결 성공", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "블루투스 기기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()

        } catch (e: SecurityException) {
            // 권한이 거부되었을 때 예외 처리
            Log.e("BluetoothConnection", "Bluetooth 권한이 필요합니다.", e)
            Toast.makeText(this, "Bluetooth 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            // Bluetooth 연결 실패 시 예외 처리
            Log.e("BluetoothConnection", "Bluetooth 연결 실패", e)
            Toast.makeText(this, "Bluetooth 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data) ?: return

        if (result.contents != null) {
            val scannedBikeId = result.contents
            Log.d("QRCodeScanner", "Scanned Bike ID: $scannedBikeId")

            URLValidationTask().execute(scannedBikeId)
        } else {
            Toast.makeText(this, "QR 코드 스캔에 실패했습니다.", Toast.LENGTH_LONG).show()
        }
    }

    private inner class URLValidationTask : AsyncTask<String, Void, Pair<Int?, String?>>() {
        override fun doInBackground(vararg params: String?): Pair<Int?, String?> {
            val scannedBikeId = params[0] ?: return Pair(null, null)
            val url = URL("http://192.168.1.115:5000/check_bike")

            val jsonObject = JSONObject()
            jsonObject.put("bike_id", scannedBikeId)

            return try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                val outputStreamWriter = OutputStreamWriter(connection.outputStream)
                outputStreamWriter.write(jsonObject.toString())
                outputStreamWriter.flush()

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                Log.d("QRCodeScanner", "Response Code: $responseCode, Message: $responseMessage")

                Pair(responseCode, responseMessage)
            } catch (e: Exception) {
                Log.e("QRCodeScanner", "Error during URL validation", e)
                Pair(null, null)
            }
        }

        override fun onPostExecute(result: Pair<Int?, String?>) {
            super.onPostExecute(result)
            val responseCode = result.first
            val responseMessage = result.second

            if (responseCode == 200 && responseMessage != null) {
                try {
                    val jsonResponse = JSONObject(responseMessage)
                    val bikeId = jsonResponse.optString("bike_id", null)
                    if (bikeId != null) {
                        showRentalConfirmationDialog(bikeId)
                    } else {
                        Toast.makeText(this@QRCodeScannerActivity, "자전거 ID를 받을 수 없습니다.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("QRCodeScanner", "Error parsing JSON response", e)
                    Toast.makeText(this@QRCodeScannerActivity, "응답 처리 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this@QRCodeScannerActivity, "비정상적 QR코드입니다.", Toast.LENGTH_LONG).show()
                val intent = Intent(this@QRCodeScannerActivity, HomeActivity::class.java)
                startActivity(intent)
            }
        }

        private fun showRentalConfirmationDialog(bikeId: String) {
            val dialogBuilder = AlertDialog.Builder(this@QRCodeScannerActivity)
            dialogBuilder.setMessage("자전거 일련번호: $bikeId\n이 자전거를 대여하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("예") { dialog, _ ->
                    RentBikeTask().execute(bikeId)
                    dialog.dismiss()
                }
                .setNegativeButton("아니오") { dialog, _ ->
                    dialog.cancel()
                    Toast.makeText(this@QRCodeScannerActivity, "취소되었습니다.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@QRCodeScannerActivity, HomeActivity::class.java)
                    startActivity(intent)
                }

            val alert = dialogBuilder.create()
            alert.setTitle("대여 확인")
            alert.show()
        }
    }

    private inner class RentBikeTask : AsyncTask<String, Void, Int?>() {
        override fun doInBackground(vararg params: String?): Int? {
            val bikeId = params[0] ?: return null
            val url = URL("http://192.168.1.115:5000/rent_bike")

            val jsonObject = JSONObject()
            jsonObject.put("bike_id", bikeId)

            return try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                val outputStreamWriter = OutputStreamWriter(connection.outputStream)
                outputStreamWriter.write(jsonObject.toString())
                outputStreamWriter.flush()

                Log.d("QRCodeScanner", "Rent Response Code: ${connection.responseCode}")
                connection.responseCode
            } catch (e: Exception) {
                Log.e("QRCodeScanner", "Error during bike rental", e)
                null
            }
        }

        override fun onPostExecute(responseCode: Int?) {
            super.onPostExecute(responseCode)
            when (responseCode) {
                200 -> {
                    try {
                        bluetoothSocket?.outputStream?.write("U".toByteArray())
                        Toast.makeText(this@QRCodeScannerActivity, "대여가 완료되었습니다.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@QRCodeScannerActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@QRCodeScannerActivity, "대여 실패", Toast.LENGTH_LONG).show()
                    }
                }
                else -> Toast.makeText(this@QRCodeScannerActivity, "대여 실패", Toast.LENGTH_LONG).show()
            }
        }
    }
}