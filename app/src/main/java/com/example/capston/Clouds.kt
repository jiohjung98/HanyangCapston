package com.example.capston

import com.google.gson.annotations.SerializedName


class Clouds {
    @SerializedName("all")
    val all = 0f

    override fun toString(): String {
        return "Clouds{" +
                "all=" + all +
                '}'
    }
}