// ApiService.kt
package com.example.tabatshu_android

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.GET

interface ApiService {
    @Multipart
    @POST("/reports")
    fun addReport(
        @Part("bikeId") bikeId: RequestBody,
        @Part("userId") userId: RequestBody,
        @Part("category") category: RequestBody,
        @Part("contents") contents: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<ResponseBody>

    @GET("/reports")
    fun getReports(): Call<List<Report>>

    @Headers("Content-Type: application/json")
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("/login_records")
    fun getLoginRecords(): Call<List<LoginRecord>>

    @GET("/bikes")
    fun getBikes(): Call<List<Bike>>
}
data class Report(
    val bikeId: String,
    val category: String,
    val contents: String,
    val date: String,
    val image: String? = null
)

data class LoginRecord(
    val userId: String
)

data class Bike(
    val bikeId: String,
    val status: String
)