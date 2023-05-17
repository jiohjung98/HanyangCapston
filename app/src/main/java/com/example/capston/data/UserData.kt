package com.example.capston.data

data class UserData(
    val username: String?,
    val email: String?,
    val pet_list: Any? = null,
    var pet_cnt : Int
)

data class PetInfo(
    var pet_name : String? = null,
    var breed: String? = null,
    var gender : Int? = null, // female = 0, male = 1
    var born : String? = null,
    var image_url : String? = null,
)

data class UserPost(
    var uid: String? = null,
    var category : Int? = null, // 실종 = 0, 발견 = 1
    var pet_info : PetInfo? = null,
    var content : String? = null,
    var latitude : Double? = null,
    var longitude : Double? = null,
    var address1 : String? = null,
    var address2 : String? = null,
    var date : String? = null,
    var time : String? = null,
)