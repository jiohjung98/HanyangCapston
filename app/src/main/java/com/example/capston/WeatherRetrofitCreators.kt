package com.example.capston

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRetrofitCreators() {

    val serverUrl = "https://api.openweathermap.org/data/2.5/"

    fun WeatherRetrofitCreator(): WeatherRetrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherRetrofit = retrofit.create(WeatherRetrofit::class.java)

        return weatherRetrofit
    }
}