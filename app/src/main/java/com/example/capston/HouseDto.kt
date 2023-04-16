package com.example.capston

import com.google.gson.annotations.SerializedName

data class HouseDto(
    @SerializedName("items") val items : List<HouseData>
)