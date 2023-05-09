package com.example.capston.homepackage

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.capston.*
import com.example.capston.databinding.CustomBalloonLayoutBinding
import com.example.capston.databinding.FragmentNaviHomeBinding
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.fragment_navi_home.*
import net.daum.mf.map.api.*
import net.daum.mf.map.api.MapView
import java.util.*
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
    var mapPoint: MapPoint? = null
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

    private lateinit var mainActivity: MainActivity
    private val auth get() = mainActivity.auth
    private val database get() = mainActivity.database

    // GeoFire
    private var geoFire : GeoFire? = null
    private var geoQuery : GeoQuery? = null

    // 1. currentLocation 변수 정의 및 MapView.CurrentLocationEventListener 인터페이스 구현
    private var currentLocation: MapPoint? = null

    private val currentLocationEventListener = object : MapView.CurrentLocationEventListener {
        override fun onCurrentLocationUpdate(mapView: MapView, mapPoint: MapPoint, v: Float) {
            // 현재 위치 갱신 시 호출되는 콜백 함수
            currentLocation = mapPoint
        }
        override fun onCurrentLocationDeviceHeadingUpdate(mapView: MapView, v: Float) {}
        override fun onCurrentLocationUpdateFailed(mapView: MapView) {}
        override fun onCurrentLocationUpdateCancelled(mapView: MapView) {}
    }

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
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
            activity?.startActivity(intent)
        }

        binding.locationBtn.setOnClickListener {
            mapView!!.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        }

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
        geoFire = GeoFire(database.child("geofire"))

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        listen = MarkerEventListener(mainActivity)

//        // 뷰 추가 전 기존 뷰 삭제
//        kakaoMapViewContainer?.removeAllViews()
//
//
//
//        val kakaoMapView = view.findViewById<MapView>(R.id.kakaoMapView)
//
//        kakaoMapViewContainer?.addView(kakaoMapView)

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
    }

    // 메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
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
//        binding.kakaoMapViewContainer?.removeAllViews()
    }

    // 커스텀 말풍선 클래스
    class CustomBalloonAdapter(inflater: LayoutInflater, val fragment: NaviHomeFragment): CalloutBalloonAdapter {

        var mainActivity : MainActivity? = null
        private var database = fragment.database.child("post").child("witness")
        //        private var mCalloutBalloon: View = inflater.inflate(R.layout.custom_balloon_layout, null)
        private var viewBinding = CustomBalloonLayoutBinding.inflate(inflater)

        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            // 마커 클릭 시 나오는 말풍선

            database.child(poiItem?.itemName!!).get().addOnSuccessListener { task ->
                viewBinding.timeText.text = (task.child("date").value.toString() + " " + task.child("time").value.toString())
                viewBinding.nameText.text = "알 수 없음"
                viewBinding.breedText.text = task.child("pet_info").child("breed").value.toString()

//                mCalloutBalloon.findViewById<TextView>(R.id.name_text).text = "알 수 없음"
//                mCalloutBalloon.findViewById<TextView>(R.id.breed_text).text =
//                    task.child("pet_info").child("breed").value.toString()
//                mCalloutBalloon.findViewById<TextView>(R.id.time_text).text =
//                    (task.child("date").value.toString() + " " + task.child("time").value.toString())
                if(fragment.isAdded()) {
                    GlideApp.with(fragment)
                        .load(Uri.parse(task.child("pet_info").child("image_url").value.toString()))
                        .into(viewBinding.enterImage)
                }
            }

            return viewBinding.root
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View {
            // 말풍선 클릭 시
            return viewBinding.root
        }
    }

    fun findAddress() {
        val mapReverseGeoCoder =
            MapReverseGeoCoder("830d2ef983929904f477a09ea75d91cc", mapPoint, this, requireActivity())
        mapReverseGeoCoder.startFindingAddress()
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
//        if (!getAddress) {
//            findAddress()
//        }
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
        if(geoQuery == null)
            geoQuery = geoFire?.queryAtLocation(GeoLocation(center.mapPointGeoCoord.latitude,center.mapPointGeoCoord.longitude),meterToKillo(radius))
        else{
            geoQuery?.center = GeoLocation(center.mapPointGeoCoord.latitude,center.mapPointGeoCoord.longitude)
            geoQuery?.radius = meterToKillo(radius)
        }


//        Log.d("center", "${center.mapPointGeoCoord.latitude} + ${center.mapPointGeoCoord.longitude}")
//        Log.d("radius",radius.toString())

        // 검색쿼리의 이벤트 리스너
        geoQuery!!.addGeoQueryEventListener(object : GeoQueryEventListener {
            // 아래 구현 - 쿼리로 키가 검색되면 실행됨
            override fun onKeyEntered(key: String, location: GeoLocation) {
                // 해당 위치 데이터의 정보를 가져오는 코드 작성
//                Log.d("=Entered Key", key)

//                Log.d("마커위치정보",location.latitude.toString() + " "+ location.longitude.toString())
                val markerPoint = MapPoint.mapPointWithGeoCoord(location.latitude,location.longitude)

                val marker = MapPOIItem().apply {
                    markerType = MapPOIItem.MarkerType.CustomImage
                    customImageResourceId = R.drawable.marker_click           // 커스텀 마커 이미지
                    isCustomImageAutoscale = true
                    setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
                    itemName = key
                    mapPoint = markerPoint
                    isShowCalloutBalloonOnTouch = true
                    tag = key.hashCode()  // 삭제 할때 위해 key 대한 해시 정수로 저장
                }
                mapView!!.addPOIItem(marker)
            }

            override fun onKeyExited(key: String) {
                // 위치 데이터가 범위 밖으로 이탈했을 때의 코드 작성
                // 이탈시 맵에서 삭제
//                Log.d("Exited Key", key)
//                Log.d("마커삭제 전",mapView?.poiItems?.size.toString())
//                mapView?.removePOIItem(mapView.findPOIItemByTag(key.hashCode()))
//                Log.d("마커삭제 후",mapView?.poiItems?.size.toString())
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
        })
    }


    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
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

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
        setMarker(p0)
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
        val pref = this.requireActivity().getPreferences(Context.MODE_PRIVATE)
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
}