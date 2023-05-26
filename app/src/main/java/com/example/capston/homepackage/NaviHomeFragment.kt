package com.example.capston.homepackage

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.bumptech.glide.request.target.Target
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import androidx.viewbinding.ViewBindings
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.capston.*
import com.example.capston.R
import com.example.capston.data.UserPost
import com.example.capston.databinding.BallonLayoutBinding
import com.example.capston.databinding.FragmentNaviHomeBinding
import com.example.capston.databinding.LostBalloonLayoutBinding
import com.example.capston.databinding.SpotBalloonLayoutBinding
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.fragment_navi_home.*
import net.daum.mf.map.api.*
import net.daum.mf.map.api.MapView
import java.util.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_missing.*
import kotlinx.android.synthetic.main.ballon_layout.view.*
import kotlinx.android.synthetic.main.lost_balloon_layout.view.*
import kotlinx.android.synthetic.main.fragment_navi_home.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.coroutines.resumeWithException
import kotlin.math.*

/*
 * 메인화면, 첫번째 메뉴, 지도
 */
class NaviHomeFragment : Fragment(), MapView.CurrentLocationEventListener,
    MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    // 카카오맵
    var listen: MarkerEventListener? = null
    private var kakaoMapViewContainer: FrameLayout? = null
    var mapView: MapView? = null
    private var polyline: MapPolyline? = null
    private var getAddress: Boolean = false
    private var addressAdmin: String = ""
    private var addressLocality: String = ""
    private var addressThoroughfare: String = ""

    // 권한
    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    private val RequestPermissionCode = 1

    private var isStart: Boolean = false
    private var isPause: Boolean = false
    private var tapTimer: Timer? = null
    private val route = ArrayList<ArrayList<Double>>()

    private var _binding: FragmentNaviHomeBinding? = null
    private val binding get() = _binding!!

    lateinit var mainActivity: MainActivity
    private val auth get() = mainActivity.auth
    private var database : DatabaseReference = FirebaseDatabase.getInstance().reference
    private var witnessDB : DatabaseReference? = database.child("post").child("witness")
    private var lostDB : DatabaseReference? = database.child("post").child("lost")

    // GeoFire
    private var geoFire : GeoFire? = null
    private var geoQuery : GeoQuery? = null
    private var geoQueryListener : GeoQueryEventListener? = null

    // 1. currentLocation 변수 정의 및 MapView.CurrentLocationEventListener 인터페이스 구현
    private var currentLocation: MapPoint? = null
    private var initCurLocUpdate: Boolean = false
    private var firstMoveFinished : Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNaviHomeBinding.inflate(inflater, container, false)

        binding.lostBtn.setOnClickListener {
            val intent = Intent(context, MissingActivity::class.java)
            mapView?.removeAllPOIItems()
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
            activity?.startActivity(intent)
        }

        binding.locationBtn.setOnClickListener {
            mapView?.setMapCenterPoint(currentLocation,true)
        }

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
        geoFire = GeoFire(database.child("geofire"))

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        listen = MarkerEventListener(mainActivity, this)

        mapView = MapView(mainActivity)
        val mapViewContainer = kakaoMapView as ViewGroup
        mapViewContainer.addView(mapView)

        mapView!!.setPOIItemEventListener(listen)

        isSetLocationPermission()
        mapView!!.setMapViewEventListener(this)


        mapView!!.setZoomLevel(0, true)
        mapView!!.setCustomCurrentLocationMarkerTrackingImage(
            R.drawable.normal_dog_icon_64,
            MapPOIItem.ImageOffset(50, 50)
        )
        mapView!!.setCustomCurrentLocationMarkerImage(
            R.drawable.normal_dog_icon_64,
            MapPOIItem.ImageOffset(50, 50)
        )
        mapView!!.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        Log.d("트래킹", mapView!!.currentLocationTrackingMode.toString())
        mapView!!.setCurrentLocationEventListener(this)
        polyline = MapPolyline()
        polyline!!.tag = 1000
        polyline!!.lineColor = Color.argb(255, 103, 114, 241)

        mapView!!.setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater,this))

        geoQueryListener = object : GeoQueryEventListener {
            // 아래 구현 - 쿼리로 키가 검색되면 실행됨
            override fun onKeyEntered(key: String, location: GeoLocation) {
                // DB의 마커정보들 불러오기 ->
                witnessDB!!.child(key).get().addOnSuccessListener { task ->
                    if(task.value != null) {    // 들어온 key가 목격이 아닌 실종정보의 key일수도 있으므로
                        val markerData = task.getValue(UserPost::class.java)!!

                        val marker = MapPOIItem().apply {
                            markerType = MapPOIItem.MarkerType.CustomImage
                            customImageResourceId = R.drawable.marker_spot_yellow_64        // 커스텀 마커 이미지
                            isCustomImageAutoscale = true
                            setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
                            itemName = key
                            mapPoint =
                                MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude)
                            isShowCalloutBalloonOnTouch = true
                            tag = key.hashCode()  // 삭제 할때 위해 key 대한 해시 정수로 저장
                            userObject = markerData
                        }
                        mapView!!.addPOIItem(marker)
                        //                    kakaoMapView.findPOIItemByTag(key.hashCode()).customCalloutBalloon = balloonBinding.root
                    }
                }
                lostDB!!.child(key).get().addOnSuccessListener { task->
                    if(task.value != null) {    // 들어온 key가 실종이 아닌 목격정보의 key일수도 있으므로
                        Log.d("lostDB get key", task.toString())
                        val markerData = task.getValue(UserPost::class.java)!!

                        val marker = MapPOIItem().apply {
                            markerType = MapPOIItem.MarkerType.CustomImage
                            customImageResourceId = R.drawable.marker_missing_orange_64         // 커스텀 마커 이미지
                            isCustomImageAutoscale = true
                            setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
                            itemName = key
                            mapPoint =
                                MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude)
                            isShowCalloutBalloonOnTouch = true
                            tag = key.hashCode()  // 삭제 할때 위해 key 대한 해시 정수로 저장
                            userObject = markerData
                        }
                        mapView!!.addPOIItem(marker)
                    }
                }
            }

            override fun onKeyExited(key: String) {
                // 현재 범위 이탈시 맵에서 삭제
                Log.d("마커삭제 전",mapView?.poiItems?.size.toString())
                mapView?.removePOIItem(mapView!!.findPOIItemByTag(key.hashCode()))
                Log.d("마커삭제 후",mapView?.poiItems?.size.toString())
            }
            override fun onKeyMoved(key: String, location: GeoLocation) {
                // 위치 데이터가 이동했을 때의 코드 작성
            }
            override fun onGeoQueryReady() {
                // 초기 데이터 검색이 완료되었을 때 호출됩니다.

            }
            override fun onGeoQueryError(error: DatabaseError) {
                // 위치 데이터 검색 중 에러가 발생했을 때 호출됩니다.
            }
        }
    }

    // 메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
        mapView!!.removeAllPOIItems()
        kakaoMapView!!.removeAllViews()
        kakaoMapViewContainer?.removeAllViews() // 맵뷰가 들어있는 ViewGroup에서 모든 뷰를 제거
//        mapView?.onPause() // 맵뷰를 일시정지
//        mapView = null // 맵뷰 변수를 null로 설정
    }

    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView!!.onResume()
        }
//        binding.kakaoMapViewContainer.addView(mapView)
        this._binding = null
    }

    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    override fun onDestroy() {
        kakaoMapViewContainer?.removeAllViews()
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        kakaoMapView!!.removeAllViews()
//        binding.kakaoMapViewContainer?.removeAllViews()
    }

    fun findAddress(p1:MapPoint) {
        val mapReverseGeoCoder =
            MapReverseGeoCoder("830d2ef983929904f477a09ea75d91cc", p1, this, requireActivity())
        mapReverseGeoCoder.startFindingAddress()
    }

    // findAddress 에서 실행시킴
    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, p1: String?) {
        getAddress = true
        val address = p1!!.split(" ")
        addressAdmin = address[0]
        addressLocality = address[1]
        addressThoroughfare = address[2]
        val pref = this.requireActivity().getPreferences(Context.MODE_PRIVATE)
        val edit = pref.edit()
        edit.putString("addressAdmin", address[0]) // 경기도
        edit.putString("addressLocality", address[1]) // 안산시
        edit.putString("addressThoroughfare", address[2])  // 상록구
        edit.apply()
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

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {

        if(!initCurLocUpdate) {
            initCurLocUpdate = true
            mainActivity.getWeather()
            mainActivity.getAirQuality()
        } else{
            if (p0!!.currentLocationTrackingMode.toString() != "TrackingModeOnWithoutHeadingWithoutMapMoving") {
                p0!!.currentLocationTrackingMode =
                    MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
            }
        }

        currentLocation = p1

//        if (!isStart || isPause) {
//            return
//        }
//        val lat = p1!!.mapPointGeoCoord.latitude
//        val lon = p1!!.mapPointGeoCoord.longitude

        mainActivity.setCurrentLocation(p1!!.mapPointGeoCoord.latitude, p1.mapPointGeoCoord.longitude)

//        route.add(arrayListOf(lat, lon))
//
//        polyline!!.addPoint(p1)
//        p0!!.removePolyline(polyline)
//        p0.addPolyline(polyline)

//         변환 주소 가져오기
        if (!getAddress) {
            findAddress(p1)
        }
    }

    private fun setMarker(p0: MapView?){

        val bounds = p0?.mapPointBounds!!
        val center = p0.mapCenterPoint

        val leftX = bounds.bottomLeft.mapPointGeoCoord.longitude // 좌측 경도
        val leftY = bounds.bottomLeft.mapPointGeoCoord.latitude // 좌측 위도

        val rightX = bounds.topRight.mapPointGeoCoord.longitude // 우측 경도
        val rightY = bounds.topRight.mapPointGeoCoord.latitude // 우측 위도

        val radius = haversine(leftY,leftX,rightY,rightX)/2.0  // 좌하단~우상단 대각선 거리의 절반

//        Log.i("좌하단","${leftX} + ${leftY}")
//        Log.i("우상단","${rightX} + ${rightY}")

        setGeoQuery(p0,center,radius)
    }

    private fun setGeoQuery(mapView: MapView?, center: MapPoint, radius : Double){

        mapView?.removeAllPOIItems()

        // center(화면중심)에서 radius(km단위)에 속하는 위치데이터들 검색함
        geoQuery = geoFire?.queryAtLocation(GeoLocation(center.mapPointGeoCoord.latitude,center.mapPointGeoCoord.longitude),meterToKillo(radius))
//        geoQuery?.removeAllListeners()

//        Log.d("CENTER",center.mapPointGeoCoord.latitude.toString() +" "+center.mapPointGeoCoord.longitude.toString())
//        Log.d("RADIUS",meterToKillo(radius).toString())

        geoQuery?.addGeoQueryEventListener(geoQueryListener)
    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
//        setMarker(p0)
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
        if (p0!!.currentLocationTrackingMode.toString() != "TrackingModeOnWithoutHeadingWithoutMapMoving") {
            p0!!.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
        }

        if (tapTimer != null) {
            tapTimer!!.cancel()
        }
        setMarker(p0)
    }


    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
        setMarker(p0)
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

    // 커스텀 말풍선 클래스
    class CustomBalloonAdapter(val inflater: LayoutInflater, val fragment : NaviHomeFragment): CalloutBalloonAdapter {

        private val lostBinding = LostBalloonLayoutBinding.inflate(inflater)
        private val witBinding = SpotBalloonLayoutBinding.inflate(inflater)
        private var viewBinding : ViewBinding? = null

        // 오버라이드는 코루틴 쓸 수가 없다. - suspend 불가
        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            val data : UserPost = poiItem?.userObject as UserPost
            val category = data.category!!
            setBalloon(data,category)

            return viewBinding!!.root
        }

        private fun setBalloon(data : UserPost, category : Int) {
            when(category){
                0 -> { // lost
                    lostBinding.timeText.text = "시간: " + (data.date + " " + data.time)
                    lostBinding.nameText.text = "이름: " + data.pet_info?.pet_name
                    lostBinding.breedText.text = "견종: " + data.pet_info?.breed
                    val url = URL(data.pet_info?.image_url.toString()).openConnection() as HttpURLConnection
                    url.doInput = true
                    url.connect()
                    val bitmap = BitmapFactory.decodeStream(url.inputStream)

                    lostBinding.enterImage.setImageBitmap(bitmap)
                    viewBinding = lostBinding
                }
                1 -> { // witness
                    witBinding.timeText.text = "시간: " + (data.date + " " + data.time)
                    witBinding.breedText.text = "견종: " + data.pet_info?.breed
                    val url = URL(data.pet_info?.image_url.toString()).openConnection() as HttpURLConnection
                    url.doInput = true
                    url.connect()
                    val bitmap = BitmapFactory.decodeStream(url.inputStream)

                    witBinding.enterImage.setImageBitmap(bitmap)
                    viewBinding = witBinding
                }
            }
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View {
            // 말풍선 클릭 시
            return viewBinding!!.root
        }
    }


    // 마커 클릭 이벤트 리스너
    class MarkerEventListener(val mainActivity: MainActivity, val fragment: NaviHomeFragment): MapView.POIItemEventListener {

        override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {
            // 마커 클릭 시
            Log.d("MARKER CLICK", "ok")

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
            mapView?.context?.let { context ->
                val dialog = Dialog(context)
                // 다이얼로그 테두리 둥글게 만들기
                dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
                dialog.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함
                dialog.setContentView(R.layout.markerclick_lostdog)

                val userPost = poiItem?.userObject as UserPost
                if (userPost.category == 0) {
                    dialog.setContentView(R.layout.markerclick_lostdog)
                    // 실종 마커에 대한 다이얼로그 설정
                    // ...
                } else if (userPost.category == 1) {
                    dialog.setContentView(R.layout.markerclick_spotdog)
                    // 목격 마커에 대한 다이얼로그 설정
                    // ...
                }
                val btnCall = dialog.findViewById<TextView>(R.id.call_btn)
                val receiveNum = dialog.findViewById<TextView>(R.id.phone_receive)

                // 전화번호 표시로 변경
                receiveNum.addTextChangedListener(PhoneNumberFormattingTextWatcher())

                // 전화걸기 화면 이동
                btnCall.setOnClickListener {
                    val telNumber = "${receiveNum.text}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:$telNumber"))
                    context.startActivity(intent)
                    dialog.dismiss()
                }

                val btnClose = dialog.findViewById<TextView>(R.id.close_btn)
                btnClose.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
        }


        override fun onDraggablePOIItemMoved(
            mapView: MapView?,
            poiItem: MapPOIItem?,
            mapPoint: MapPoint?
        ) {
            // 마커의 속성 중 isDraggable = true 일 때 마커를 이동시켰을 경우
        }
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

    }

    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
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
        Log.d("onMapViewMoveFinished","CALL")
        if(!firstMoveFinished){
            setMarker(p0)
            firstMoveFinished = true
        }
    }
}