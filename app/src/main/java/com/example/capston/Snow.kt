package com.example.capston

import com.google.gson.annotations.SerializedName


class Snow {
    @SerializedName("3h")
    val h3 = 0f

    @SerializedName("1h")
    val h1 = 0f

    override fun toString(): String {
        return "Snow{" +
                "h3=" + h3 +
                ", h1=" + h1 +
                '}'
    }
}