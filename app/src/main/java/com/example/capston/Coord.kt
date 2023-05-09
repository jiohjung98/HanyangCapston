package com.example.capston

import com.google.gson.annotations.SerializedName


class Coord {
    @SerializedName("lon")
    val lon = 0f

    @SerializedName("lat")
    val lat = 0f

    override fun toString(): String {
        return "Coord{" +
                "lon=" + lon +
                ", lat=" + lat +
                '}'
    }
}