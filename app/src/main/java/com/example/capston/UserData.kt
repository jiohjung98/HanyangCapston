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