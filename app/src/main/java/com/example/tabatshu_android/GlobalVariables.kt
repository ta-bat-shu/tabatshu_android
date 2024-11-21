package com.example.tabatshu_android

import retrofit2.Retrofit

object GlobalVariables {
    var tfRent: Boolean? = null // 대여 상태
    var user_id: String? = null // 사용자 ID
    var bike_id: String? = null
    var retrofit: Retrofit = RetrofitClient.getInstance()
}
