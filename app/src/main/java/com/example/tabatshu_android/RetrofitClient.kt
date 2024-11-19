package com.example.tabatshu_android

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
//        .baseUrl("http://192.168.0.57:5000")
        .baseUrl("http://10.0.2.2:5000")  // 서버 주소
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getInstance(): Retrofit {
        return retrofit
    }
}