//package com.example.capston
//
//import android.util.Log
//
//import androidx.lifecycle.MutableLiveData
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//import retrofit2.Retrofit
//
//
//class OpenWeatherRepos {
//    //이 클래스에서는 API 통신을 통해서 데이터를 가져와야 한다.
//    private val TAG = "OpenWeatherRepository"
//    private var retrofit: Retrofit? = null
//    private var opwAPI: OpenWeatherAPI? = null
//    private var opw: OpenWeather? = null//Retrofit 객체 생성 생성
//
//    //인터페이스 객체 생성
//
//    //API와 통신을 하는 함수 호출
//    //날씨 정보를 직접적으러 받아와야 하는 부분
//    val weather: MutableLiveData<OpenWeather>
//        get() {
//
//            //Retrofit 객체 생성 생성
//            retrofit = RetrofitService.getRetroInstance(BASE_URL)
//
//            //인터페이스 객체 생성
//            opwAPI = retrofit!!.create(OpenWeatherAPI::class.java)
//
//            //API와 통신을 하는 함수 호출
//            opw = OpenWeather()
//            val data = MutableLiveData<OpenWeather>()
//            callWeatherAPI(data)
//            return data
//        }
//
//    private fun callWeatherAPI(data: MutableLiveData<OpenWeather>) {
//
//        //응답을 받아오는 부분
//        val call: Call<OpenWeather> = opwAPI.getWeather("seoul", "api키", "kr")
//        call.enqueue(object : Callback<OpenWeather?>() {
//            //아래 함수들은 callback 함수들로써 연결이 완료되면 호출이 되는 부분이다.
//            fun onResponse(call: Call<OpenWeather?>?, response: Response<OpenWeather?>) {
//                data.postValue(response.body())
//                Log.i(TAG, "API CONNECT SUCCESS")
//            }
//
//            fun onFailure(call: Call<OpenWeather?>?, t: Throwable) {
//                Log.d(TAG, "onFailure : " + t.message)
//            }
//        })
//    }
//
//    companion object {
//        private const val BASE_URL =
//            "https://api.openweathermap.org/data/2.5/" //api의 baseURL을 상수로 적어서 올려놨다.
//        private var instance: OpenWeatherRepos? = null
//
//        //OpenWeatherRepos 인스턴스 반환
//        val inStance: OpenWeatherRepos?
//            get() {
//                if (instance == null) {
//                    instance = OpenWeatherRepos()
//                }
//                return instance
//            }
//    }
//}