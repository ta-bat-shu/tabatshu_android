package com.example.tabatshu_android

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface TashuApiService {
    @GET("v1/openapi/station")
    fun getStations(
        @Header("api-token") apiToken: String
    ): Call<StationResponse>
}
