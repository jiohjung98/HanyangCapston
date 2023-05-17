package com.example.capston.data

import com.google.gson.annotations.SerializedName

data class UserModel(
    val data : Data
)

data class Data(
    val avatar: String,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    val id: Int,
    @SerializedName("last_name")
    val lastName: String,
)
