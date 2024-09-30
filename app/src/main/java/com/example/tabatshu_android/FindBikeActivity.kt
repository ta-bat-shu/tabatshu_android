package com.example.tabatshu_android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageButton
import android.app.Dialog
import android.view.Window
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class FindBikeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 특정 위치 설정 (예: 서울)
        val hanbat = LatLng(36.35101, 127.3011) // 위도, 경도
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanbat, 16f))


        // 커스텀 이미지 마커 추가
        val spot0 = LatLng(36.353508, 127.300284)
        val markerOptions0 = MarkerOptions().position(spot0).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.num_0)) // 커스텀 이미지
        // 마커 지도에 추가
        googleMap.addMarker(markerOptions0)


        // 커스텀 이미지 마커 추가
        val spot1 = LatLng(36.35108, 127.298410)
        val markerOptions1 = MarkerOptions().position(spot1).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.num_1)) // 커스텀 이미지

        // 마커 지도에 추가
        googleMap.addMarker(markerOptions1)


        // 커스텀 이미지 마커 추가
        val spot2 = LatLng(36.351616, 127.301843)
        val markerOptions2 = MarkerOptions().position(spot2).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.num_2)) // 커스텀 이미지

        // 마커 지도에 추가
        googleMap.addMarker(markerOptions2)

        // 커스텀 이미지 마커 추가
        val spot4 = LatLng(36.348577, 127.300821)
        val markerOptions4 = MarkerOptions().position(spot4).title("").icon(BitmapDescriptorFactory.fromResource(R.drawable.num_4)) // 커스텀 이미지

        // 마커 지도에 추가
        googleMap.addMarker(markerOptions4)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_find_bike)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // MapView 초기화
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        // 지도가 준비되면 콜백 호출
        mapView.getMapAsync(this)
    }
}
