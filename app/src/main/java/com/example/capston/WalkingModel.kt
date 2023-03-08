package com.example.capston

import java.util.*
import kotlin.collections.ArrayList

data class CreateWalkingResultModel (
    val error: Int,
    val walkingId: String
)

data class MyPetListModel (
    val error: Int,
    val myPetList: ArrayList<MyPetModel>
)

data class MyPetModel (
    val animalName: String,
    val age: Int,
    val weight: Double,
    val breed: String
)

data class MyWalkingStaticModel (
    val error: Int,
    val walkings: ArrayList<WalkingResultModel> = arrayListOf()
)

data class WalkingResultModel (
    val _id: String,
    val calories: Number,
    val distance: Number,
    val walkingTime: Int,
    val animal: ArrayList<String> = arrayListOf(),
//    val walkingAmount: ArrayList<WalkingAmountModel> = arrayListOf(),
    val addressAdmin: String,
    val addressLocality: String,
    val date: Date
)
