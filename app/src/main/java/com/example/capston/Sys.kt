package com.example.capston

import com.google.gson.annotations.SerializedName


class Sys {
    @SerializedName("country")
    var country: String? = null

    @SerializedName("sunrise")
    var sunrise: Long = 0

    @SerializedName("sunset")
    var sunset: Long = 0

    @SerializedName("type")
    private val type: String? = null

    @SerializedName("id")
    private val id: String? = null

    @SerializedName("message")
    private val message: String? = null
}