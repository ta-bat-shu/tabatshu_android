package com.example.tabatshu_android

import android.graphics.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FindBikeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<StationClusterItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_bike)

        // Initialize MapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Set initial camera position
        val initialPosition = LatLng(36.35101, 127.3011)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 16f))

        // Set up cluster manager
        setUpClusterManager()

        // Fetch and display station data
        fetchStationsAndDisplayMarkers()
    }

    private fun setUpClusterManager() {
        clusterManager = ClusterManager(this, googleMap)
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)

        // Custom renderer for clusters
        clusterManager.renderer = object : DefaultClusterRenderer<StationClusterItem>(this, googleMap, clusterManager) {
            override fun onBeforeClusterRendered(cluster: Cluster<StationClusterItem>, markerOptions: com.google.android.gms.maps.model.MarkerOptions) {
                val totalParkingCount = cluster.items.sumOf { it.parkingCount }
                markerOptions.icon(createCustomClusterIcon(totalParkingCount))
            }

            override fun onBeforeClusterItemRendered(item: StationClusterItem, markerOptions: com.google.android.gms.maps.model.MarkerOptions) {
                markerOptions.icon(createCustomMarkerIcon(item.parkingCount))
            }
        }
    }

    private fun fetchStationsAndDisplayMarkers() {
        val apiService = ApiClient.apiService
        val call = apiService.getStations("ewx30wm5s727k596") // API Key

        call.enqueue(object : Callback<StationResponse> {
            override fun onResponse(call: Call<StationResponse>, response: Response<StationResponse>) {
                if (response.isSuccessful) {
                    val stations = response.body()?.results
                    stations?.forEach { station ->
                        clusterManager.addItem(
                            StationClusterItem(
                                lat = station.latitude,
                                lng = station.longitude,
                                name = station.name,
                                address = station.address,
                                parkingCount = station.parkingCount
                            )
                        )
                    }
                    clusterManager.cluster()
                } else {
                    Log.e("TashuAPI", "API 호출 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<StationResponse>, t: Throwable) {
                Log.e("TashuAPI", "API 호출 실패: ${t.message}")
            }
        })
    }

    private fun createCustomClusterIcon(totalParkingCount: Int): BitmapDescriptor {
        val markerSize = 150
        val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background circle
        val paint = Paint()
        paint.color = Color.parseColor("#FFA500") // Orange
        paint.style = Paint.Style.FILL
        canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2.5f, paint)

        // Draw text
        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(totalParkingCount.toString(), markerSize / 2f, markerSize / 1.8f, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun createCustomMarkerIcon(parkingCount: Int): BitmapDescriptor {
        val markerSize = 100
        val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background circle
        val paint = Paint()
        paint.color = Color.parseColor("#FFA500") // Orange
        paint.style = Paint.Style.FILL
        canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2.5f, paint)

        // Draw text
        paint.color = Color.WHITE
        paint.textSize = 30f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(parkingCount.toString(), markerSize / 2f, markerSize / 1.8f, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

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

data class StationClusterItem(
    private val lat: Double,
    private val lng: Double,
    private val name: String,
    private val address: String,
    val parkingCount: Int
) : ClusterItem {

    override fun getPosition(): LatLng {
        return LatLng(lat, lng)
    }

    override fun getTitle(): String {
        return name
    }

    override fun getSnippet(): String {
        return "대여 가능: ${parkingCount}대"
    }
}
