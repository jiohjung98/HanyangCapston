package com.example.capston

import com.example.capston.data.UserModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface INetworkService {
    @GET("api/users/")
    fun doGetUser(@Query("id") id : String) : Call<UserModel>
}