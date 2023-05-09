//package com.example.capston
//
//import retrofit2.http.GET
//import retrofit2.http.Query
//
//public interface OpenWeatherAPI {
//    //GET 메소드 만들었고 그거에 대한 주소는 아래와 같음
//    @GET("weather")
//    Call<OpenWeather> getWeather(@Query("q") String q, @Query("appid") String appid, @Query("lang")String lang); }
//    //날씨 정보를 가져오는 메소드를 선언만함, 매개변수로는 쿼리들을 받음