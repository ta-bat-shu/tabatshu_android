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

    @Headers("Content-Type: application/json")
    @POST("/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}