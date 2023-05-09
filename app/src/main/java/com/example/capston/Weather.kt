package com.example.capston

import com.google.gson.annotations.SerializedName


class Weather {
    @SerializedName("id")
    val id = 0

    @SerializedName("main")
    val main: String? = null

    @SerializedName("description")
    val description: String? = null

    @SerializedName("icon")
    val icon: String? = null

    override fun toString(): String {
        return "Weather{" +
                "id=" + id +
                ", main='" + main + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                '}'
    }
}
