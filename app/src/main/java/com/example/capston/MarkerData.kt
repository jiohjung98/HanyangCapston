package com.example.capston

data class MarkerData(
    val date: String,
    val time: String,
    val breed: String,
    val imageUrl: String, // imageUrl 필드 추가
    val latitude: Double, // 위도 정보 추가
    val longitude: Double // 경도 정보 추가
)