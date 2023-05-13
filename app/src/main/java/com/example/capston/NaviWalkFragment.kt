package com.example.capston

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.capston.DogRegister.DogRegister1Activity
import com.example.capston.databinding.FragmentNaviWalkBinding
import com.example.capston.homepackage.NaviHomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_dog_register.*
import kotlinx.android.synthetic.main.fragment_navi_home.*
import kotlinx.android.synthetic.main.fragment_navi_walk.*
import kotlinx.android.synthetic.main.lost_dog_info.*
import net.daum.mf.map.api.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*


/*
 *  두번째 메뉴, 산책하기
 */
@Suppress("DEPRECATION")
class NaviWalkFragment : Fragment(), MapView.CurrentLocationEventListener,
    MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    // 카카오맵
    var listen: NaviHomeFragment.MarkerEventListener? = null
    private var kakaoMapViewContainer: FrameLayout? = null
    var mapView: MapView? = null
    private var polyline: MapPolyline? = null
    var mapPoint: MapPoint? = null
    private var getAddress: Boolean = false
    private var addressAdmin: String = ""
    private var addressLocality: String = ""
    private var addressThoroughfare: String = ""

    // 권한
    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    private val RequestPermissionCode = 1

    private var _binding: FragmentNaviWalkBinding? = null
    private val binding get() = _binding!!

    private final val REQUEST_FIRST = 1010
    private var pet_info = PetInfo()
    lateinit var uri: Uri

    // DB에서 불러올 정보들
    private lateinit var DBpet : DatabaseReference
    private var _cur_pet_num : String? = null
    private val cur_pet_num get() = _cur_pet_num!!

    // 1. Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    private lateinit var mainActivity: MainActivity
    private lateinit var database : DatabaseReference
    private lateinit var auth : FirebaseAuth
    private lateinit var functions : FirebaseFunctions

    private lateinit var petname : TextView
    private lateinit var breed : TextView
    private lateinit var gender : TextView
    private lateinit var age : TextView

    // 1. currentLocation 변수 정의 및 MapView.CurrentLocationEventListener 인터페이스 구현

    // 초기값 서울시청
    private var curLat : Double = 37.5667297
    private var curLon : Double = 126.9782551

    // 날씨 관련
    private var temp : Any? = null
    private var weather : Int? = null
    private lateinit var weatherImage : ImageView
    private lateinit var weatherLocaction : TextView
    private lateinit var temperature : TextView
    private var isGetWeather : Boolean = false


    // 미세먼지관련
    private var pm25 : Int? = null
    private var pm10 : Int? = null
    private var isGetAir : Boolean = false

    // 공유 변수
    private lateinit var pref : SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
        database = mainActivity.database
        auth = mainActivity.auth
        functions = FirebaseFunctions.getInstance()
        pref = this.requireActivity().getPreferences(Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//         Inflate the layout for this fragment
        _binding = FragmentNaviWalkBinding.inflate(inflater, container, false)
        weatherImage = binding.img1
        temperature = binding.temperatureTv
        weatherLocaction = binding.locTxt
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // fragment 액션바 보여주기(선언안해주면 다른 프레그먼트에서 선언한 .hide() 때문인지 모든 프레그먼트에서 액션바 안보임
        (activity as AppCompatActivity).supportActionBar?.show()

        mapView = MapView(mainActivity)
        val mapViewContainer = kakaoMapView3 as ViewGroup
        mapViewContainer.addView(mapView)

        mapView!!.setPOIItemEventListener(listen)

        isSetLocationPermission()
        mapView!!.setMapViewEventListener(this)


        mapView!!.setZoomLevel(0, true)
        mapView!!.setCustomCurrentLocationMarkerTrackingImage(
            R.drawable.happy_dog_icon_64,
            MapPOIItem.ImageOffset(50, 50)
        )
        mapView!!.setCustomCurrentLocationMarkerImage(
            R.drawable.happy_dog_icon_64,
            MapPOIItem.ImageOffset(50, 50)
        )
        mapView!!.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        Log.d("트래킹", mapView!!.currentLocationTrackingMode.toString())
        mapView!!.setCurrentLocationEventListener(this)
        polyline = MapPolyline()
        polyline!!.tag = 1000
        polyline!!.lineColor = Color.argb(255, 103, 114, 241)


//        listen = NaviHomeFragment.MarkerEventListener(mainActivity)
//
//        // 뷰 추가 전 기존 뷰 삭제
//        homecontainer?.removeAllViews()
//
//        val kakaoMapView = view.findViewById<MapView>(R.id.walk_map_view)
//
//        homecontainer?.addView(kakaoMapView)
//
//        kakaoMapView.setPOIItemEventListener(listen)
//
//        isSetLocationPermission()
//        kakaoMapView.setMapViewEventListener(this)

//
//        kakaoMapView.setZoomLevel(0, true)
//        kakaoMapView.setCustomCurrentLocationMarkerTrackingImage(
//            R.drawable.dog_icon_64,
//            MapPOIItem.ImageOffset(50, 50)
//        )
//        kakaoMapView.setCustomCurrentLocationMarkerImage(
//            R.drawable.dog_icon_64,
//            MapPOIItem.ImageOffset(50, 50)
//        )
//        kakaoMapView.currentLocationTrackingMode =
//            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
//        Log.d("트래킹", kakaoMapView.currentLocationTrackingMode.toString())
//        kakaoMapView.setCurrentLocationEventListener(this)
//        polyline = MapPolyline()
//        polyline!!.tag = 1000
//        polyline!!.lineColor = Color.argb(255, 103, 114, 241)


////       binding?.NaviWalkFragment = this
//        _binding?.NaviWalkFragment = this
//        binding?.apply {
//            temperature = temperatureTv
////            weatherState = weatherTv
////            weatherTip = weatherTipTv
////            weatherIcon = weatherIc
//        }

        binding.walkBtn.setOnClickListener {
            goToWalk()
        }

        binding.registerBtn.setOnClickListener {
            onClickRegister(view)
        }

        binding.addingBtn.setOnClickListener {
            onClickRegister(view)
        }

        // 현재 반려견 인덱스 불러오기
        loadCurrentDog(0)

        initAddImage()

        petname = binding.walkName
        breed = binding.walkBreed
        gender = binding.walkGender
        age = binding.walkAge

        setupData()


//        setupStatusHandler()
    }

//    companion object {
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            NaviWalkFragment().apply {
//            }
//    }

    // 위치 권한 설정 확인 함수
    private fun isSetLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        }
    }

    // 위치 권한 설정
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            mainActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            RequestPermissionCode
        )
        mainActivity.recreate()
    }


    // 메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
        kakaoMapView3.removeAllViews()
//        homecontainer?.removeAllViews() // 맵뷰가 들어있는 ViewGroup에서 모든 뷰를 제거
//        mapView?.onPause() // 맵뷰를 일시정지
//        mapView = null // 맵뷰 변수를 null로 설정
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        kakaoMapView3.removeAllViews()
    }

    fun goToWalk() {
        val dialog = Dialog(mainActivity)
        // 다이얼로그 테두리 둥글게 만들기
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dialog.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함
        dialog.setContentView(R.layout.walkdialog)

        val btnOk = dialog.findViewById<TextView>(R.id.yes_btn)
        val btnCancel = dialog.findViewById<TextView>(R.id.no_btn)

        btnOk.setOnClickListener {
            dialog.dismiss()
            // 산책 액티비티로 이동(첫화면)
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(context, WalkActivity::class.java)
                context?.startActivity(intent)
                (context as Activity).finish()
            }, 10)
            kakaoMapView3.removeAllViews()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun onClickRegister(view: View?) {
        (context as MainActivity).supportFragmentManager.beginTransaction().remove(this).commit()
        (context as MainActivity).startActivity(Intent(mainActivity, DogRegister1Activity::class.java))
        (context as MainActivity).finish()
    }

    /*
    현재 반려견 인덱스 불러오기
    */
    private fun loadCurrentDog(flag : Int){
        // 현재 반려견 인덱스 불러오기
        // 로컬에 해당 key 없으면 1 -> 아직 등록안했거나, 등록한건 있는데 앱 데이터 초기화
        // 등록안해서 1이면 아래서 null -> invalid() 들어감
        var cur_pet = mainActivity.sharedPreferences?.getInt("cur_pet",1)

//        Log.d("loadCurrentDog 반려견 인덱스",cur_pet.toString())

        DBpet = database.child("users").child(auth.currentUser!!.uid).child("pet_list").child(cur_pet.toString())
        DBpet.get().addOnSuccessListener { snapshot ->
//            Log.d("리스너","DBPet addOnSuccessListener listener called")
            if (snapshot.value == null){
//                    Log.d("snapshot", "null")
                invalidDog()
            }
            // 등록된 반려견 있음
            else {
                validDog(snapshot)
                setupMyDogList(cur_pet!!)
                setupMyDogHandler()
            }
        }
    }

    private fun validDog(snapshot: DataSnapshot) {
        Log.d("등록 있음", "${snapshot}")

        getImageFromStore(snapshot)

        binding.registerBtn.visibility = View.GONE
        binding.walkBtn.isEnabled = true
        binding.walkBtn.visibility = View.VISIBLE

        // 데이터가 변경되면 리스너가 감지함
        // 최초(아무값도 없을때)로 실행 됐을때도 감지 됨
        // 반려견정보 불러오기 -> 현재 등록된 첫번째 반려견 정보 불러옴, 이후 반려견 추가된다면 변경할 필요O
        petname.text = snapshot.child("pet_name").value.toString()
        breed.text = snapshot.child("breed").value.toString()
        age.text = snapshot.child("born").value.toString() + "년생"
        if (snapshot.child("gender").value == 1)
            gender.text = "남아"
        else
            gender.text = "여아"
    }

    private fun invalidDog(){
        Log.d("등록 없음","")
        binding.registerBtn.visibility = View.VISIBLE
        binding.walkAgeSlash.visibility = View.INVISIBLE
        binding.walkBreedSlash.visibility = View.INVISIBLE
        binding.addingBtn.visibility = View.INVISIBLE
        binding.addingBtn.isEnabled = false
        binding.camera.visibility = View.INVISIBLE
        petname.text = "등록된 반려견이 없습니다"
    }

    /*
     * 갤러리에서 이미지 가져와서 image area에 띄움
     */
    private fun getImageFromStore(snapshot: DataSnapshot) {
        // database.child("users").child(auth.currentUser!!.uid).child("pet_list").child(cur_pet_num)
        val url = snapshot.child("image_url").value.toString()
        if (isAdded()) {
//            Log.d("IMAGE URL",url)
            GlideApp.with(this@NaviWalkFragment).load(Uri.parse(url))
                .into(binding.profile)
        }
    }

    /*
     반려견 선택 스피너 설정
     */
    private fun setupMyDogList(cur_pet : Int){
        // 스피너보이게
//        binding.petSelectBox.visibility = View.VISIBLE
        val DogArray = ArrayList<String>()

        // 이름 읽어오기
        database.child("users").child(auth.currentUser!!.uid).get().addOnSuccessListener { result ->
            // DogArray 배열에 DB상 반려견 이름들 추가
            for (item in result.child("pet_list").children) {
//                Log.d("pet_list", item.child("pet_name").value.toString())
                DogArray.add(item.child("pet_name").value.toString())
            }
            val statusAdapter =
                object : ArrayAdapter<String>(requireContext(), R.layout.gender_spinner) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val v = super.getView(position, convertView, parent)
                        var tv = v as TextView
                        tv.setTextSize(/* size = */ 12f)
//                        tv.gravity = Gravity.CENTER
                        if (position == count) {
                            (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).text = ""
//                            (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).hint = "선택"
                        }
                        return v
                    }

                    override fun getCount(): Int {
                        return super.getCount() - 1
                    }
                }
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            statusAdapter.addAll(DogArray.toMutableList())
            statusAdapter.add(" 선택")


            _binding!!.petSelectSpinner.adapter = statusAdapter

            // 스피너 onItemSelectedListener 호출됨
            pet_select_spinner.setSelection(cur_pet.minus(1))
//            Log.d("setupMyDogList 받은 cur_pet",cur_pet.toString())
            pet_select_spinner.dropDownVerticalOffset = dipToPixels(15f).toInt()
        }
    }

    private fun setupMyDogHandler() {
        val uid = database.child("users").child(auth.currentUser!!.uid)
        binding.petSelectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                Log.d("onItemSelected listener called position",position.toString())

                // NaviWalk 진입 때마다 setupMyDogList에서 꼭 한번씩 호출됨

                // 반려견 사진, 정보 reload
                database.child("users").child(auth.currentUser!!.uid).child("pet_list").child(position.plus(1).toString())
                    .get().addOnSuccessListener { snapshot ->
                        validDog(snapshot)
                    }

                mainActivity.sharedPreferences?.edit()?.putInt("cur_pet", position.plus(1))?.apply()
//                Log.d("변경된 반려견 인덱스", mainActivity.sharedPreferences?.getInt("cur_pet", -1).toString())

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun setupData() {
        val statusData = resources.getStringArray(R.array.spinner_ddong)
        val statusAdapter = object : ArrayAdapter<String>(requireContext(),R.layout.gender_spinner) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                var tv = v as TextView
                tv.setTextSize(/* size = */ 12f)
                if (position == count) {
                    (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).text = ""
//                    (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).hint = " 선택"
                }
                return v
            }
            override fun getCount(): Int {
                return super.getCount() - 1
            }
        }

        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        statusAdapter.addAll(statusData.toMutableList())
        statusAdapter.add(" 선택")

//        binding.dogDdongSpinner.adapter = statusAdapter
//
//        dog_ddong_spinner.setSelection(statusAdapter.count)
//        dog_ddong_spinner.dropDownVerticalOffset = dipToPixels(15f).toInt()
    }


//    private fun setupStatusHandler() {
//        _binding?.dogDdongSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                when(position) {
//                    0 -> {
//                    }
//                    else -> {
//                    }
//                }
//            }
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//            }
//        }
//    }


    private fun dipToPixels(dipValue: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dipValue,
            resources.displayMetrics
        )
    }
//
//    override fun onResume() {
//        super.onResume()
//        getWeatherInCurrentLocation()
//    }
//
//    private fun getWeatherInCurrentLocation(){
//        mLocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//        mLocationListener = LocationListener { p0 ->
//            val params: RequestParams = RequestParams()
//            params.put("lat", p0.latitude)
//            params.put("lon", p0.longitude)
//            params.put("appid", Companion.API_KEY)
//            doNetworking(params)
//        }
//
//
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(requireActivity(), arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), WEATHER_REQUEST)
//            return
//        }
//        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener)
//        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener)
//    }
//
//    private fun doNetworking(params: RequestParams) {
//        var client = AsyncHttpClient()
//        client.get(WEATHER_URL, params, object: JsonHttpResponseHandler(){
//            override fun onSuccess(
//                statusCode: Int,
//                headers: Array<out cz.msebera.android.httpclient.Header>?,
//                response: JSONObject?
//            ) {
//                val weatherData = WeatherData().fromJson(response)
//                if (weatherData != null) {
//                    updateWeather(weatherData)
//                }
//            }
//        })
//    }
//
//
//    private fun updateWeather(weather: WeatherData) {
//        temperature.setText(weather.tempString+" ℃")
//        weatherState.setText(weather.weatherType)
//        val resourceID = resources.getIdentifier(weather.icon, "drawable", activity?.packageName)
//        weatherIcon.setImageResource(resourceID)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        if(mLocationManager!=null){
//            mLocationManager.removeUpdates(mLocationListener)
//        }
//    }

    /*
    * 갤러리 접근 권한 획득 후 작업
    */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1010 -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // 권한 허가
                    // 허용 클릭 시 갤러리에서 이미지 가져오기
                    getImageFromAlbum()
                } else {
                    // 거부 클릭시
                    Toast.makeText(mainActivity,"권한을 거부했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else -> {
            //Do Nothing
        }
        }
    }

    /*
     카메라 버튼 클릭 이벤트 설정
     */
    private fun initAddImage() {
        binding.camera.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(mainActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                -> {
                    // 권한이 존재하는 경우
                    // TODO 이미지를 가져옴
                    getImageFromAlbum()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    // 권한이 거부 되어 있는 경우
                    showPermissionContextPopup()
                }
                else -> {
                    // 처음 권한을 시도했을 때 띄움
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),REQUEST_FIRST)
                }
            }
        }
    }

    /*
     * 갤러리에서 이미지 가져와서 image area에 띄움
     */
    private fun getImageFromAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        getImageActivity.launch(intent)
    }
    private val getImageActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    // 전달 받은 이미지 uri를 넣어준다.
                    uri = result.data?.data!!
                    // 이미지를 ImageView에 표시한다.
                    binding.profile.setImageURI(uri)
                    // Upload
                    initUploadImage(uri)
                }
            }
        }

    /*
     * 저장소 접근 권한 설정 팝업
     */
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(mainActivity)
            .setTitle("권한이 필요합니다")
            .setMessage("갤러리에서 사진을 선택하려면 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_FIRST)
            }
            .setNegativeButton("취소하기",{ _,_ ->})
            .create()
            .show()
    }

    /*
     * 업로드 버튼 클릭 이벤트 설정
     */
    private fun initUploadImage(uri : Uri){
        // 서버로 업로드
        uploadImageToStorage(uri)
    }

    /*
     * 서버 스토리지로 이미지 업로드
     */
    private fun uploadImageToStorage(uri: Uri) {
        // storage 인스턴스 생성
        val storage = Firebase.storage
        // storage 참조
        val storageRef = storage.getReference("images").child("users").child(auth.currentUser!!.uid)
        // storage에 저장할 파일명 선언
        val fileName = auth.currentUser!!.uid + "_" + SimpleDateFormat("yyyyMMddHHmm").format(Date())
        val mountainsRef = storageRef.child("${fileName}.jpg")


        val uploadTask = mountainsRef.putFile(uri).addOnCompleteListener {
            if (it.isSuccessful) {
                // 파일 업로드에 성공했기 때문에 스토리지 url을 다시 받아와 DB에 저장
                mountainsRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        pet_info!!.image_url = uri.toString()
                    }.addOnFailureListener {
                        Toast.makeText(mainActivity, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
            } else {
                Toast.makeText(mainActivity, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // 파일 업로드 성공
        uploadTask.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(mainActivity, "사진 업로드 성공", Toast.LENGTH_SHORT).show();

        }   // 파일 업로드 실패
            .addOnFailureListener {
                Toast.makeText(mainActivity, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
            }
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
    }

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
//        Log.i("onCurrentLocationUpdate","Call")

        this.curLat = p1?.mapPointGeoCoord!!.latitude
        this.curLon = p1.mapPointGeoCoord!!.longitude

        if (!getAddress)
            findAddress(p1)



        if(!isGetWeather)
            setWeatherInfo()
        if(!isGetAir)
            setAirInfo()
//
//        if (!isStart || isPause) {
//            return
//        }
//        val lat = p1!!.mapPointGeoCoord.latitude
//        val lon = p1!!.mapPointGeoCoord.longitude
//
//        route.add(arrayListOf(lat, lon))
//
//        mapPoint = p1
//        polyline!!.addPoint(p1)
//        p0!!.removePolyline(polyline)
//        p0.addPolyline(polyline)
//
//        // 변환 주소 가져오기

    }


    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewInitialized(p0: MapView?) {
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
        if (p0!!.currentLocationTrackingMode.toString() != "TrackingModeOnWithoutHeadingWithoutMapMoving") {
            p0!!.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
        }
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

    }

    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {
    }

    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, p1: String?) {
        getAddress = true
        val address = p1!!.split(" ")
        addressAdmin = address[0]
        addressLocality = address[1]
        addressThoroughfare = address[2]
//        val pref = this.requireActivity().getPreferences(Context.MODE_PRIVATE)
        val edit = pref.edit()
        edit.putString("addressAdmin", address[0])
        edit.putString("addressLocality", address[1])
        edit.putString("addressThoroughfare", address[2])
        edit.apply()
    }

    private fun findAddress(p1: MapPoint) {
        val mapReverseGeoCoder =
            MapReverseGeoCoder("830d2ef983929904f477a09ea75d91cc", p1, this, requireActivity())
        mapReverseGeoCoder.startFindingAddress()
    }

    // 위도, 경도를 거리로 변환 - 리턴 값: Meter 단위
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6372.8;
        val dLat = Math.toRadians(lat2 - lat1);
        val dLon = Math.toRadians(lon2 - lon1);
        val rLat1 = Math.toRadians(lat1);
        val rLat2 = Math.toRadians(lat2);
        var dist = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(rLat1) * cos(rLat2);
        dist = 2 * asin(sqrt(dist))

        return r * dist * 1000
    }

    private fun meterToKillo(meter: Double): Double {
        return meter / 1000
    }


    // 마커 클릭 이벤트 리스너
    class MarkerEventListener(var context: Context): MapView.POIItemEventListener {

        lateinit var mainActivity: MainActivity

        override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {
            // 마커 클릭 시
//            Log.d("markerClick", "ok")

        }

        override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, poiItem: MapPOIItem?) {
            // 말풍선 클릭 시 (Deprecated)
            // 이 함수도 작동하지만 그냥 아래 있는 함수에 작성하자
        }

        override fun onCalloutBalloonOfPOIItemTouched(
            mapView: MapView?,
            poiItem: MapPOIItem?,
            buttonType: MapPOIItem.CalloutBalloonButtonType?
        ) {
        }

        override fun onDraggablePOIItemMoved(
            mapView: MapView?,
            poiItem: MapPOIItem?,
            mapPoint: MapPoint?
        ) {
            // 마커의 속성 중 isDraggable = true 일 때 마커를 이동시켰을 경우
        }
    }


    private fun setWeatherInfo(){
        val weatherInfo = mainActivity.getWeatherInfo()
        val temp : Int = ((weatherInfo.first) as Double).minus(273.15).roundToInt()
        val weatherCode = weatherInfo.second
        temperature.text = temp.toString() + "°C"
        weatherLocaction.text = pref.getString("addressLocality","서울시") + " " + pref.getString("addressThoroughfare","종로구")


        val imageResource = when (weatherCode) {
            in 300..322 -> R.drawable.weather_drizzle
            in 500..532 -> R.drawable.weather_rain
            in 600..622 -> R.drawable.weather_snow
            in 700..790 -> R.drawable.weather_dust
            in 801..805 -> R.drawable.weather_cloud
            else -> R.drawable.weather_clear
        }
        weatherImage?.setImageResource(imageResource)
        weatherImage!!.invalidate()

        this.isGetWeather = true
    }



    private fun setAirInfo(){

        val airInfo = mainActivity.getAirInfo()


        // 미세
        val pm10Resource = when (airInfo.first) {
            in 0..30 -> Pair(R.drawable.dust_perfect,"좋음")// 좋음
            in 31..60 -> Pair(R.drawable.dust_good,"보통") // 보통
            in 61..90 -> Pair(R.drawable.dust_bad,"나쁨") // 나쁨
            in 91..150 -> Pair(R.drawable.dust_verybad,"매우나쁨") // 매우나쁨
            else -> Pair(R.drawable.dust_worst,"최악") // 최악
        }

        // 초미세
        val pm25Resource = when (airInfo.second) {
            in 0..15 -> Pair(R.drawable.dust_perfect,"좋음") // 좋음
            in 16..35 -> Pair(R.drawable.dust_good,"보통") // 보통
            in 36..75 ->  Pair(R.drawable.dust_bad,"나쁨") // 나쁨
            in 76..150 -> Pair(R.drawable.dust_verybad,"매우나쁨")// 매우나쁨
            else -> Pair(R.drawable.dust_worst,"최악") // 최악
        }

        binding.img2.apply {
            setImageResource(pm10Resource.first)
            invalidate()
        }

        binding.img3.apply {
            setImageResource(pm25Resource.first)
            invalidate()
        }

        binding.dust1Txt.apply {
            setText(pm10Resource.second)
            invalidate()
        }

        binding.dust2Txt.apply {
            setText(pm25Resource.second)
            invalidate()
        }

        this.isGetAir = true
    }
}