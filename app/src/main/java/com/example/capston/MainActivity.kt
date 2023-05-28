package com.example.capston
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.capston.databinding.ActivityMainBinding
import com.example.capston.homepackage.CustomDialog
import com.example.capston.homepackage.NaviHomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.common.util.Utility
import de.hdodenhof.circleimageview.CircleImageView
import jxl.write.BoldStyle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_navi_home.*
import kotlinx.android.synthetic.main.fragment_navi_walk.*


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
//    private var oneFragment: NaviHomeFragment? = null
//    private var twoFragment: NaviWalkFragment? = null
//    private var threeFragment: Calendar_fragment? = null
//    private var fourFragment: NaviMypageFragment? = null

    internal var launcher: ActivityResultLauncher<Intent>? = null
    private var uri : Uri? = null

    private var lastSelectedItemId = 0

    // 현재위치 - 초기값 서울시청
    private var curLat : Double = 37.5667297
    private var curLon : Double = 126.9782551

    // 날씨 관련
    private var temp : Any? = null
    private var weatherCode : Int? = null
    private var isGetWeather : Boolean = false

    // 미세먼지관련 - 주소는 공유변수로 존재
    private var pm25 : Int? = null
    private var pm10 : Int? = null
    private var isGetAir : Boolean = false

    // MainActivity 하위 여러 프래그먼트에서 여러번 사용한다면 여기다 선언하는게 좋을것같음
    internal val database: DatabaseReference = Firebase.database.reference
    internal val auth = FirebaseAuth.getInstance()
    internal var functions : FirebaseFunctions = FirebaseFunctions.getInstance()
    
    // 로컬 저장값들
    internal var sharedPreferences: SharedPreferences? = null
    private lateinit var pref : SharedPreferences

    internal val uid = auth.currentUser!!.uid

    @SuppressLint("MissingInflatedId")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Firebase 앱 초기화
        FirebaseApp.initializeApp(this)

        // 로컬에 저장된 현재 반려견 인덱스
        sharedPreferences = getSharedPreferences("CUR_PET", MODE_PRIVATE);
        // 현재위치 공유변수
        pref = getPreferences(Context.MODE_PRIVATE)

        val keyHash = Utility.getKeyHash(this)
        Log.d("Hash", keyHash)

        replaceFragment(NaviHomeFragment())

        deleteToolbar(NaviMypageFragment())

        val main_bnv = findViewById<BottomNavigationView>(R.id.main_bnv)
        setSupportActionBar(toolbar) // 커스텀한 toolbar를 액션바로 사용
        supportActionBar?.setDisplayShowTitleEnabled(false) // 액션바에 표시되는 제목의 표시유무를 설정합니다. false로 해야 custom한 툴바의 이름이 화면에 보이게 됩니다.

//        var noticeitem = findViewById<ImageView>(R.id.noticeitem)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        var toolbar2 = findViewById<Toolbar>(R.id.toolbar2)
        var toolbar3 = findViewById<Toolbar>(R.id.toolbar3)

        close_notice.setOnClickListener {
            val transaction = supportFragmentManager.popBackStack()
            toolbar.visibility = View.VISIBLE
            toolbar2.visibility = View.INVISIBLE
            toolbar3.visibility = View.INVISIBLE
        }


        btn_back.setOnClickListener {
            val transaction = supportFragmentManager.popBackStack()
            toolbar.visibility = View.VISIBLE
            toolbar2.visibility = View.INVISIBLE
            toolbar3.visibility = View.INVISIBLE
        }


        binding.mainBnv.setOnItemSelectedListener { item ->
            if (item.itemId == lastSelectedItemId) {
                // 이미 선택된 아이템을 다시 선택했을 때 아무 동작도 하지 않음
                return@setOnItemSelectedListener false
            }

            lastSelectedItemId = item.itemId
            main_bnv.itemIconTintList = null

            changeFragment(
                when (item.itemId) {
                    R.id.navigation_home -> {
                        if (kakaoMapView3 != null) {
                            kakaoMapView3.removeAllViews()
                        }
                        item.setIcon(R.drawable.home_color)
                        binding.mainBnv.menu.findItem(R.id.navigation_community)?.setIcon(R.drawable.walk)
                        binding.mainBnv.menu.findItem(R.id.navigation_mypage)?.setIcon(R.drawable.mypage)
                        NaviHomeFragment()
                    }
                    R.id.navigation_community -> {
                        if (kakaoMapView != null) {
                        kakaoMapView!!.removeAllViews()
                        }
                        item.setIcon(R.drawable.walk_color)
                        binding.mainBnv.menu.findItem(R.id.navigation_home)?.setIcon(R.drawable.home)
                        binding.mainBnv.menu.findItem(R.id.navigation_mypage)?.setIcon(R.drawable.mypage)
                        NaviWalkFragment()
                    }
                    else -> {
                        if (kakaoMapView != null) {
                            kakaoMapView!!.removeAllViews()
                        }
                        if (kakaoMapView3 != null) {
                            kakaoMapView3.removeAllViews()
                        }
                        item.setIcon(R.drawable.mypage_color)
                        binding.mainBnv.menu.findItem(R.id.navigation_home)?.setIcon(R.drawable.home)
                        binding.mainBnv.menu.findItem(R.id.navigation_community)?.setIcon(R.drawable.walk)
                        NaviMypageFragment()
                    }
                }
            )
            true
        }

        binding.mainBnv.selectedItemId = R.id.navigation_home
    }

    private fun changeFragment(fragment: Fragment?) {
        supportFragmentManager.popBackStackImmediate()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frm, fragment!!)
//                .addToBackStack(null)
                .commit()
    }


    private fun replaceFragment(naviCommunityFragment: Fragment) {
        if (kakaoMapView != null) {
            kakaoMapView!!.removeAllViews()
        }
        if (kakaoMapView3 != null) {
            kakaoMapView3.removeAllViews()
        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_frm, naviCommunityFragment)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
//        mapView?.removeAllPOIItems()
//        mapView = null
    }

    private fun deleteToolbar(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_frm, fragment!!)
        fragmentTransaction.commit()
    }


    fun getWeather(){
        val data = hashMapOf(
            "lat" to curLat,
            "lon" to curLon
        )
        Log.d("getWeather",data.toString())
        functions
            .getHttpsCallable("getCurrentWeather")
            .call(data)
            .addOnSuccessListener { task->
                val result = task.data as Map<*, *>
                this.temp = result["temp"]
                this.weatherCode = result["weather"] as Int
                this.isGetWeather = true
                setWeatherInfo(this.temp!!, this.weatherCode as Int)
                Log.d("기온",this.temp.toString())
                Log.d("날씨",this.weatherCode.toString())
            }
            .addOnFailureListener {
                Log.d("날씨","FAIL")
            }
    }

    fun getAirQuality(){
        val data = hashMapOf(
            "address" to pref.getString("addressThoroughfare","종로구").toString()
        )
        Log.d("getAirQuality",data.toString())

        functions
            .getHttpsCallable("getCurrentAirQuality")
            .call(data)
            .addOnSuccessListener { task->
                val result = task.data as Map<*, *>
                this.pm25 = Integer.parseInt(result["pm25Value"].toString())
                this.pm10 = Integer.parseInt(result["pm10Value"].toString())
                this.isGetAir = true
                setAirInfo(this.pm25!!,this.pm10!!)
                Log.d("PM2.5",this.pm25.toString())
                Log.d("PM10",this.pm10.toString())
            }
            .addOnFailureListener {
                this.isGetAir = true
                Log.d("미세먼지","FAIL")
            }
    }

    /* ---------------- GET & SET METHODS -----------------------*/

    fun setCurrentLocation(lat:Double, lon:Double){
        this.curLat = lat
        this.curLon = lon
    }

    private fun setWeatherInfo(temp:Any, weather: Int){
        this.temp = temp
        this.weatherCode = weather
    }
    fun getWeatherInfo() : Pair<*,*>{
        return Pair(temp,weatherCode)
    }

    private fun setAirInfo(pm25: Int, pm10: Int){
        this.pm10 = pm10
        this.pm25 = pm25
    }
    fun getAirInfo() : Pair<*,*>{
        return Pair(this.pm10,this.pm25)
    }

    fun getisGetWeather(): Boolean{
        return isGetWeather
    }
    fun getisGetAir(): Boolean{
        return isGetAir
    }
}