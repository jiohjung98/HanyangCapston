package com.example.capston

data class UserData(
    val username: String?,
    val email: String?,
    val pet_list: PetListModel,
)

data class PetListModel(
    var pet_list : ArrayList<PetInfo>
)

data class PetInfo(
    var pet_name : String? = null,
    var breed: String? = null,
    var gender : Int? = null, // female = 0, male = 1
    var born : String? = null,
    var image_url : String? = null,
)

data class UserPost(
    val uid: String? = null,
    val category : Int? = null, // 실종 = 0, 발견 = 1
    val pet_info : PetInfo? = null,
    val content : String? = null,
    val image_url: String? = null
)