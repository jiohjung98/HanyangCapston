package com.example.capston

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherRetrofit {
    @GET("weather")
    fun getWeatherInfo(@Query("lat") lat: String,
                       @Query("lon") lon: String,
                       @Query ("appid") appid: String): Call<WeatherAPIModel>

}