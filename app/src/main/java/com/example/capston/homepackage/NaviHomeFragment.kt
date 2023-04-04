package com.example.capston.homepackage

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.capston.*
import com.example.capston.databinding.FragmentNaviHomeBinding
import com.example.capston.databinding.FragmentWalkBinding
import com.example.capston.databinding.LostDogInfoBinding
import kotlinx.android.synthetic.main.fragment_navi_home.*
import net.daum.mf.map.api.*
import java.util.*
import kotlin.concurrent.timer
import kotlin.math.*


var RecordPage = HomeRecord()
val walkFragment = WalkFragment()

/*
 * 메인화면, 첫번째 메뉴, 지도
 */
class NaviHomeFragment : Fragment(), MapView.CurrentLocationEventListener,
    MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    var listen: MarkerEventListener? = null

    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    private val RequestPermissionCode = 1
    var mapView: MapView? = null
    private var polyline: MapPolyline? = null
    var mapPoint: MapPoint? = null
    private var prevLat: Double? = null
    private var prevLon: Double? = null
    private var walkingDistance: Double = 0.0
    private var walkingCalorie: Double = 0.0
    private var isStart: Boolean = false
    private var isPause: Boolean = false
    private var tapTimer: Timer? = null
    private val route = ArrayList<ArrayList<Double>>()
    private val toiletLoc = ArrayList<ArrayList<Double>>()
    private val walkingAmounts = ArrayList<Double>()
    private var walkingTimer: Timer? = null
    private var runningDogImageTimer: Timer? = null
    private var runningDogImage = arrayOf(
        R.drawable.running_dog_1, R.drawable.running_dog_2
        , R.drawable.running_dog_3, R.drawable.running_dog_4, R.drawable.running_dog_5
        , R.drawable.running_dog_6, R.drawable.running_dog_7, R.drawable.running_dog_8
    )
    private var runningDogImageCounter: Int = 1
    private var time = 0
    private var isTimerRunning: Boolean = false
    private var getAddress: Boolean = false
    private var addressAdmin: String = ""
    private var addressLocality: String = ""
    private var addressThoroughfare: String = ""
    private var animal = ArrayList<String>()
    private var fullAmount = ArrayList<Double>()

    private var _binding: FragmentNaviHomeBinding? = null
    private val binding get() = _binding!!

    private var binding2: LostDogInfoBinding? = null

    var info = binding2?.inputInfo?.editableText.toString().trim()
    val time1 = binding2?.inputTime?.editableText.toString().trim()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        try {
            _binding = FragmentNaviHomeBinding.inflate(inflater, container, false)
//        } catch (Exception ) {
//            Log.e(TAG, "onCreateView", e);
//            throw e
//        }

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity

        val view = binding.root


        return view
    }

    lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }

    fun findAddress() {
        val mapReverseGeoCoder =
            MapReverseGeoCoder("830d2ef983929904f477a09ea75d91cc", mapPoint, this, requireActivity())
        mapReverseGeoCoder.startFindingAddress()
    }

    // fragment 액션바 보여주기(선언안해주면 다른 프레그먼트에서 선언한 .hide() 때문인지 모든 프레그먼트에서 액션바 안보임
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

//        val mapView = MapView(activity)
//        val mapViewContainer = kakaoMapView as ViewGroup
//        mapViewContainer.addView(mapView)

        listen = MarkerEventListener(mainActivity)
        kakaoMapView.setPOIItemEventListener(listen)

        isSetLocationPermission()
        kakaoMapView!!.setMapViewEventListener(this)
        kakaoMapView!!.setZoomLevel(0, true)
        kakaoMapView!!.setCustomCurrentLocationMarkerTrackingImage(
            R.drawable.labrador_icon,
            MapPOIItem.ImageOffset(50, 50)
        )
        kakaoMapView!!.setCustomCurrentLocationMarkerImage(
            R.drawable.labrador_icon,
            MapPOIItem.ImageOffset(50, 50)
        )
        kakaoMapView!!.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
        Log.d("트래킹", kakaoMapView!!.currentLocationTrackingMode.toString())
        kakaoMapView!!.setCurrentLocationEventListener(this)
        polyline = MapPolyline()
        polyline!!.tag = 1000
        polyline!!.lineColor = Color.argb(255, 103, 114, 241)

        kakaoMapView.setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater))
    }


    // 커스텀 말풍선 클래스
    class CustomBalloonAdapter(inflater: LayoutInflater): CalloutBalloonAdapter {

        var mainActivity: MainActivity? = null

        val mCalloutBalloon: View = inflater.inflate(R.layout.ballon_layout, null)
        var receiveInfo: View = inflater.inflate(R.layout.lost_dog_info,null)
        val name: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_name)
        var info3: TextView = mCalloutBalloon.findViewById(R.id.receiveInfo)
        var time3: TextView = mCalloutBalloon.findViewById(R.id.receiveTime)


//        lateinit var mainActivity: MainActivity
//        private val dogInfoDialog = DogInfoEnterDialog(mainActivity)

//        private lateinit var onClickedListener: ButtonClickListener
//
//        fun setOnClickedListener(listener: ButtonClickListener) {
//            onClickedListener = listener
//        }
//
//        //호출할 리스너
//        fun setDialogListener(customDialogListener: DogInfoEnterDialog.ButtonClickListener) {
//            val customDialogListener = customDialogListener
//        }


        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            // 마커 클릭 시 나오는 말풍선
            name.text = poiItem?.itemName   // 해당 마커의 정보 이용 가능

//            // mainActivity 변수 초기화(안해주면 사용 못함)
//            mainActivity = context as MainActivity
//
//
//            val dogInfoDialog = DogInfoEnterDialog(mainActivity!!)
//            dogInfoDialog.setOnClickedListener(object : DogInfoEnterDialog.ButtonClickListener {
//                override fun onClicked(time: String, info: String) {
//                    Log.d("전달 ㅇㅇ", "ㅇㅇㅇㅇㅇ")
//                    info3.text = info
//                    time3.text = time
//                    Log.d("info3", "$info3")
//                }
//            })

//            //다이얼로그에서 정의한 interface를 통해 데이터를 받아온다.
//            dogInfoDialog.setOnClickedListener(object : DogInfoEnterDialog.ButtonClickListener {
//                override fun onClicked(time: String, info: String) {
//                    time1.text = time
//                    time1.text = time
//                    info1.text = info
//                }
//            })

//
//            dogInfoDialog.setOnClickedListener(object: DogInfoEnterDialog.ButtonClickListener{
//                override fun onClicked(time: String, info: String) {
//
//                }
//            }

//
//            val dialog = dogInfoDialog(this)
////            dogInfoDialog.saveInfo()
//            dogInfoDialog.setOnClickedListener(object : DogInfoEnterDialog.ButtonClickListener {
//                override fun onClicked(myName: String) {
//                    time.text = myName
//                }
//
//            })


//            address.text = "getCalloutBalloon"
            return mCalloutBalloon
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View {
            // 말풍선 클릭 시
//            address.text = "getPressedCalloutBalloon"

            return mCalloutBalloon
        }
    }

    //메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
        this._binding = null
    }

    override fun onResume() {
        super.onResume()
        this._binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WalkFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onDestroy() {
//        mapView.removeView(kakaoMapView)
        super.onDestroy()
    }

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

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
    }

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {

        if (!isStart || isPause) {
            return
        }
        val lat = p1!!.mapPointGeoCoord.latitude
        val lon = p1!!.mapPointGeoCoord.longitude

        route.add(arrayListOf(lat, lon))

        mapPoint = p1
        polyline!!.addPoint(p1)
        p0!!.removePolyline(polyline)
        p0.addPolyline(polyline)

//        if (prevLat == null && prevLon == null) {
//            prevLat = lat
//            prevLon = lon
//            return
//        } else {
//            val distance = haversine(prevLat!!, prevLon!!, lat, lon)
//            // 이동 거리 표시
//            walkingDistance += distance
//            if (walkingDistance < 1000) {
//                distanceId.text = String.format("%.1f", walkingDistance)
//            } else {
//                digitId.text = "km"
//                distanceId.text = String.format("%.3f", meterToKillo(walkingDistance))
//            }
//            // 소모 칼로리 표시
//            walkingCalorie += distance * 0.026785714  // 1m당 소모 칼로리
//            calorieView.text = String.format("%.2f", walkingCalorie)
//            // 충족량 표시
//            if (walkingCalorie != 0.0 && fullAmount[0] != 0.0) {
//                amountView.text = String.format("%.1f", walkingCalorie / fullAmount[0] * 100)
//            }
//
//            if (prevLat != 0.0) {
//                prevLat = lat
//                prevLon = lon
//            }
//        }
        // 변환 주소 가져오기
        if (!getAddress) {
            findAddress()
        }
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
        //마커 생성
        Log.d("gooooooo", p1.toString())
//        val marker = MapPOIItem()
//        marker.itemName = "실종"
//        marker.mapPoint = mapPoint
//        marker.markerType = MapPOIItem.MarkerType.BluePin
//        marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
//
//        kakaoMapView.addPOIItem(marker)

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
        if (p0!!.currentLocationTrackingMode.toString() == "TrackingModeOff") {
            return
        }
        p0!!.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving

        if (tapTimer != null) {
            tapTimer!!.cancel()
        }
        tapTimer = timer(period = 3000, initialDelay = 3000) {
            p0!!.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
            cancel()
        }
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

        // mainActivity 변수 초기화(안해주면 사용 못함)
        mainActivity = context as MainActivity

        val dogInfoDialog = DogInfoEnterDialog(mainActivity)
        dogInfoDialog.myDlg()
//
//        val info = binding2?.inputInfo?.text.toString().trim()
//        val time = binding2?.inputTime?.text.toString().trim()

//        binding2 = LostDogInfoBinding.inflate((context as MainActivity).layoutInflater)

        kakaoMapView.setMapCenterPoint(p1,true)
        val marker = MapPOIItem()
        marker.itemName = "실종견"
        marker.mapPoint = p1
        marker.markerType =MapPOIItem.MarkerType.BluePin
//
//        if (info.isNotEmpty() && time.isNotEmpty() ) {
//            Log.d("인포값", "${info}")
//            Log.d("타임값", "${time}")
//            kakaoMapView!!.addPOIItem(marker)
//        }

        if (info != null) {
            kakaoMapView!!.addPOIItem(marker)
            Log.d("인포값", "${info}")
            Log.d("타임값", "${time1}")

        }

        else {
            kakaoMapView!!.removePOIItem(marker)
        }
        Log.d("인포값", "${info}")


//        this.binding2.yesBtn.setOnClickListener {
//            Log.d("버튼눌려요", "dddddd")
//            kakaoMapView!!.addPOIItem(marker)
//        }


//        marker.itemName = "배변"
//        marker.isShowCalloutBalloonOnTouch = false
//        marker.mapPoint = mapPoint
//        marker.markerType = MapPOIItem.MarkerType.BluePin
//        marker.customImageResourceId =
//            R.drawable.toilet_activity
//        marker.isCustomImageAutoscale = false
//        marker.setCustomImageAnchor(0.5f, 1.0f)
//        kakaoMapView!!.addPOIItem(marker)
    }


    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {
    }

    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, p1: String?) {
        getAddress = true
        val address = p1!!.split(" ")
        addressAdmin = address[0]
        addressLocality = address[1]
        addressThoroughfare = address[2]
        val pref = this.requireActivity().getPreferences(Context.MODE_PRIVATE)
//        val pref = getSharedPreferences("pref", AppCompatActivity.MODE_PRIVATE)
        val edit = pref.edit()
        edit.putString("addressAdmin", address[0])
        edit.putString("addressLocality", address[1])
        edit.putString("addressThoroughfare", address[2])
        edit.apply()
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
        Log.d("markerClick", "ok")

//        // mainActivity 변수 초기화(안해주면 사용 못함)
//        mainActivity = context as MainActivity
//
//        val dogInfoDialog = DogInfoEnterDialog(mainActivity)
//
//        dogInfoDialog.setOnClickedListener(object: DogInfoEnterDialog.ButtonClickListener {
//            override fun onClicked(time: String, info: String) {
//                Log.d("연결 가능", "성공")
//            }
//        })


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
        // 말풍선 클릭 시
        // mainActivity 변수 초기화(안해주면 사용 못함)
        mainActivity = context as MainActivity

        val dogInfoDialog = DogInfoEnterDialog(mainActivity)
        dogInfoDialog.myDlg()

    }


        // 기본 다이얼로그 생성 방법
//        val builder = Builder(context)
//        val itemList = arrayOf("정보", "마커 삭제", "취소")
//        builder.setTitle("${poiItem?.itemName}")
//        mainActivity = context as MainActivity
//        builder.setItems(itemList) { dialog, which ->
//            when(which) {
//                0 -> Toast.makeText(context, "토스트", Toast.LENGTH_SHORT).show()  // 토스트
//                1 -> mapView?.removePOIItem(poiItem)    // 마커 삭제
//                2 -> dialog.dismiss()   // 대화상자 닫기
//            }
//        }
//        builder.show()

    override fun onDraggablePOIItemMoved(
        mapView: MapView?,
        poiItem: MapPOIItem?,
        mapPoint: MapPoint?
    ) {
        // 마커의 속성 중 isDraggable = true 일 때 마커를 이동시켰을 경우
    }
}
}



//    // 위치 권한 확인
//    private fun permissionCheck() {
//        val preference = this.requireActivity().getPreferences(Context.MODE_PRIVATE)
//        val isFirstCheck = preference.getBoolean("isFirstPermissionCheck", true)
//        if (ContextCompat.checkSelfPermission(
//                context as Activity,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // 권한이 없는 상태
//            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                    context as Activity,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                )
//            ) {
//                // 권한 거절 (다시 한 번 물어봄)
//                val builder = AlertDialog.Builder(context as Activity)
//                builder.setMessage("현재 위치를 확인하시려면 위치 권한을 허용해주세요.")
//                builder.setPositiveButton("확인") { dialog, which ->
//                    ActivityCompat.requestPermissions(
//                        context as Activity,
//                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                        ACCESS_FINE_LOCATION
//                    )
//                }
//                builder.setNegativeButton("취소") { dialog, which ->
//
//                }
//                builder.show()
//            } else {
//                if (isFirstCheck) {
//                    // 최초 권한 요청
//                    preference.edit().putBoolean("isFirstPermissionCheck", false).apply()
//                    ActivityCompat.requestPermissions(
//                        context as Activity,
//                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                        ACCESS_FINE_LOCATION
//                    )
//                } else {
//                    // 다시 묻지 않음 클릭 (앱 정보 화면으로 이동)
//                    val builder = AlertDialog.Builder(context as Activity)
//                    builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
//                    builder.setPositiveButton("설정으로 이동") { dialog, which ->
//                        val intent = Intent(
//                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                            Uri.parse("http://www.google.com")
//                        )
//                        startActivity(intent)
//                    }
//                    builder.setNegativeButton("취소") { dialog, which ->
//                    }
//                    builder.show()
//                }
//            }
//        } else {
//            // 권한이 있는 상태
//            startTracking()
//        }
//    }
//    // 권한 요청 후 행동
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == ACCESS_FINE_LOCATION) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // 권한 요청 후 승인됨 (추적 시작)
//                Toast.makeText(context, "위치 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()
//                startTracking()
//            } else {
//                // 권한 요청 후 거절됨 (다시 요청 or 토스트)
//                Toast.makeText(context, "위치 권한이 거절되었습니다", Toast.LENGTH_SHORT).show()
//                permissionCheck()
//            }
//        }
//    }
//
//    // GPS가 켜져있는지 확인
//    private fun checkLocationService(): Boolean {
//        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//    }
//
//    //     위치추적 시작
//    private fun startTracking() {
//        binding.kakaoMapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
//    }
//
//    //     위치추적 중지
//    private fun stopTracking() {
//        binding.kakaoMapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
//
//    }
//
//    override fun onDestroy() {
//        stopTracking()
////        binding.kakaoMapView.remove(mapView)
//        super.onDestroy()
//    }

