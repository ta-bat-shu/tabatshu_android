package com.example.tabatshu_android

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class QRSuccessActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Layout 설정
        setContentView(R.layout.activity_qrsuccess)

        // MapView 초기화
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        // Vector 이미지 설정
        val myVectorImageView: ImageView = findViewById(R.id.qrsuccessdrawable)
        myVectorImageView.setImageResource(R.drawable.qrsuccessdrawable)

        // 지도가 준비되면 콜백 호출
        mapView.getMapAsync(this)

        // WindowInsets 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 특정 위치 설정 (예: 서울)
        val hanbat = LatLng(36.35101, 127.3011) // 위도, 경도
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanbat, 16f))

        // 커스텀 이미지 마커 추가
        val spots = listOf(
            LatLng(36.353508, 127.300284) to R.drawable.num_0,
            LatLng(36.35108, 127.298410) to R.drawable.num_1,
            LatLng(36.351616, 127.301843) to R.drawable.num_2,
            LatLng(36.348577, 127.300821) to R.drawable.num_4
        )

        for ((position, drawable) in spots) {
            val markerOptions = MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(drawable))
            googleMap.addMarker(markerOptions)
        }
    }

    // MapView의 생명주기 메서드 구현
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

