package com.example.capston

//data class MarkerData(
//    val date: String,
//    val time: String,
//    val breed: String,
//    val imageUrl: String, // imageUrl 필드 추가
//    val latitude: Double, // 위도 정보 추가
//    val longitude: Double // 경도 정보 추가
//)

data class MarkerData(
    val date: String,
    val time: String,
    val breed: String,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val likeAs: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MarkerData

        if (date != other.date) return false
        if (time != other.time) return false
        if (breed != other.breed) return false
        if (imageUrl != other.imageUrl) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (phone != other.phone) return false
        if (likeAs != other.likeAs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date?.hashCode() ?: 0
        result = 31 * result + (time?.hashCode() ?: 0)
        result = 31 * result + (breed?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (phone?.hashCode() ?: 0)
        result = 31 * result + (likeAs?.hashCode() ?: 0)
        return result
    }
}
