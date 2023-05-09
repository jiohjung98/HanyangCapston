package com.example.capston

import com.google.gson.annotations.SerializedName


class Main {
    @SerializedName("temp")
    val temp = 0f

    @SerializedName("humidity")
    val humidity = 0f

    @SerializedName("pressure")
    val pressure = 0f

    @SerializedName("temp_min")
    val temp_min = 0f

    @SerializedName("temp_max")
    val temp_max = 0f

    override fun toString(): String {
        return "Main{" +
                "temp=" + temp +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                ", temp_min=" + temp_min +
                ", temp_max=" + temp_max +
                '}'
    }
}