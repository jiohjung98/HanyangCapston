package com.example.capston

import com.google.gson.annotations.SerializedName


class OpenWeather {
    @SerializedName("coord")
    val coord: Coord? = null

    @SerializedName("weather")
    val weather: List<Weather>? = null

    @SerializedName("base")
    val base: String? = null

    @SerializedName("main")
    val main: Main? = null

    @SerializedName("wind")
    val wind: Wind? = null

    @SerializedName("clouds")
    val clouds: Clouds? = null

    @SerializedName("rain")
    val rain: Rain? = null

    @SerializedName("snow")
    val snow: Snow? = null

    @SerializedName("sys")
    val sys: Sys? = null

    @SerializedName("visibility")
    val visibility //가시성
            = 0

    @SerializedName("dt")
    val dt //데이터 계산 시간
            : Long = 0

    @SerializedName("timezone")
    val timezone //UTC에서 초단위로 이동
            = 0

    @SerializedName("id")
    val id //도시 id
            : Long = 0

    @SerializedName("name")
    val name //도시이름
            : String? = null

    @SerializedName("cod")
    val cod //내부 매개변수
            = 0

    override fun toString(): String {
        return "OpenWeather{" +
                "coord=" + coord +
                ", weather=" + weather +
                ", base='" + base + '\'' +
                ", main=" + main +
                ", wind=" + wind +
                ", clouds=" + clouds +
                ", rain=" + rain +
                ", snow=" + snow +
                ", sys=" + sys +
                ", visibility=" + visibility +
                ", dt=" + dt +
                ", timezone=" + timezone +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", cod=" + cod +
                '}'
    }
}