package com.example.capston

import java.io.FileDescriptor

data class WeatherAPIModel(
    val weather: List<WeatherModel> = listOf(),
    val main: MainModel
)

data class WeatherModel(
    val main: String,
    val description: String
)

data class MainModel (
    val temp: Double
)
