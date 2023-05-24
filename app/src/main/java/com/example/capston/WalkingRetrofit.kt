package com.example.capston

import retrofit2.Call
import retrofit2.http.*

interface WalkingRetrofit {
    @FormUrlEncoded
    @POST("/walking/create")
    fun createWalking(@Field("userToken") userToken: String,
                      @Field("calorie") calorie: Double,
                      @Field("distance") distance: Double,
                      @Field("walkingTime") walkingTime: Int,
                      @Field("walkingAmounts") walkingAmounts: ArrayList<Double>,
                      @Field("addressAdmin") addressAdmin: String,
                      @Field("addressLocality") addressLocality: String,
                      @Field("addressThoroughfare") addressThoroughfare: String,
                      @Field("animal") animal: ArrayList<String>,
                      @Field("route") route: ArrayList<Pair<Double,Double>>,
                      @Field("toiletLoc") toiletLoc: ArrayList<ArrayList<Double>>
    ): Call<CreateWalkingResultModel>

    @GET("/animal/mypet")
    fun getMyPet(@Query("userToken") userToken: String): Call<MyPetListModel>

    @GET("/walking/static")
    fun getMyWalkingStatic(@Query("userToken") userToken: String): Call<MyWalkingStaticModel>
}