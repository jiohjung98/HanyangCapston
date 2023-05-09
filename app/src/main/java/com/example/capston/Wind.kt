package com.example.capston

import com.google.gson.annotations.SerializedName


class Wind {
    @SerializedName("speed")
    val speed = 0f

    @SerializedName("deg")
    val deg = 0f

    override fun toString(): String {
        return "Wind{" +
                "speed=" + speed +
                ", deg=" + deg +
                '}'
    }
}