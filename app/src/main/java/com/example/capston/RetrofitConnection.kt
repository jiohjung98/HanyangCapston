package com.example.capston

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitConnection {
    companion object {
        private var retrofit: Retrofit? = null
        private const val BASE_URL = "https://reqres.in/"

        fun getRetrofit(): Retrofit {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!
        }
    }
}