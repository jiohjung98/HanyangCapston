package com.example.capston

import android.telecom.Call
import retrofit2.Callback
import retrofit2.http.GET

interface RetrofitService {
    @GET("v3/4c1ead49-5c18-4624-8624-17951e4484f9")
    fun getHouseData() : Callback<HouseDto> //응답받는 데이터 DTO
}