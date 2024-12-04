package com.example.tabatshu_android

import com.google.gson.annotations.SerializedName

data class StationResponse(
    val count: Int,
    val results: List<Station>
)

data class Station(
    val id: String,
    val name: String,
    @SerializedName("x_pos") val latitude: Double,
    @SerializedName("y_pos") val longitude: Double,
    val address: String,
    @SerializedName("parking_count") val parkingCount: Int
)
