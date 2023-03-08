package com.example.capston

import android.content.Context
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WalkingRetrofitCreators(val context: Context) {

    val serverUrl = context.getString(R.string.server_url)

    fun WalkingRetrofitCreator(): WalkingRetrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val walkingRetrofit = retrofit.create(WalkingRetrofit::class.java)

        return walkingRetrofit
    }
}