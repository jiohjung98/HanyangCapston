package com.example.capston

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
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.capston.databinding.ActivityMissingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import net.daum.mf.map.api.*
import net.daum.mf.map.api.MapView
import java.util.*
import kotlin.concurrent.timer
import kotlin.math.*



/*
 * 메인화면, 첫번째 메뉴, 지도
 */
class MissingActivity : AppCompatActivity(), MapView.CurrentLocationEventListener,
    MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    var listen: MarkerEventListener? = null

    internal var kakaoMapViewContainer: FrameLayout? = null

    private var flag : Int = 0 // 실종(기본값) = 0, 목격 = 1

    // Firebase
    internal val database: DatabaseReference = Firebase.database.reference
    internal val auth = FirebaseAuth.getInstance()
    internal val storage = FirebaseStorage.getInstance()
    internal val uid = auth.currentUser!!.uid

    // 갤러리 이미지 가져오기
    internal var launcher: ActivityResultLauncher<Intent>? = null
    private var uri : Uri? = null

    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    private val RequestPermissionCode = 1
    var mapView: MapView? = null
    private var polyline: MapPolyline? = null
    var mapPoint: MapPoint? = null
    private var isStart: Boolean = false
    private var isPause: Boolean = false
    private var tapTimer: Timer? = null
    private val route = ArrayList<ArrayList<Double>>()
    private var getAddress: Boolean = false
    private var addressAdmin: String = ""
    private var addressLocality: String = ""
    private var addressThoroughfare: String = ""

    // Viewbinding
    private var _binding: ActivityMissingBinding? = null
    internal val binding get() = _binding!!

    // 실종 다이얼로그
    private lateinit var lostDialog : DogInfoEnterDialog
    // 목격 다이얼로그
    private lateinit var spotDialog : DogInfoEnterDialog2

    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = ActivityMissingBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.show()

        lostDialog = DogInfoEnterDialog(this)
        lostDialog.setLauncher()

        spotDialog = DogInfoEnterDialog2(this)


        listen = MarkerEventListener(this, lostDialog, spotDialog)

        // 뷰 추가 전 기존 뷰 삭제
        kakaoMapViewContainer?.removeAllViews()

        val kakaoMapView = findViewById<MapView>(R.id.miss_kakaoMapView)
        kakaoMapViewContainer?.addView(kakaoMapView)

        kakaoMapView.setPOIItemEventListener(listen)

        isSetLocationPermission()
        kakaoMapView.setMapViewEventListener(this)
        kakaoMapView.setZoomLevel(0, true)
        kakaoMapView.setCustomCurrentLocationMarkerTrackingImage(
            R.drawable.labrador_icon,
            MapPOIItem.ImageOffset(50, 50)
        )
        kakaoMapView.setCustomCurrentLocationMarkerImage(
            R.drawable.labrador_icon,
            MapPOIItem.ImageOffset(50, 50)
        )
        kakaoMapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        Log.d("트래킹", kakaoMapView.currentLocationTrackingMode.toString())
        kakaoMapView.setCurrentLocationEventListener(this)
        polyline = MapPolyline()
        polyline!!.tag = 1000
        polyline!!.lineColor = Color.argb(255, 103, 114, 241)

        kakaoMapView.setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater))


        // 좌측 뒤로가기 버튼
        binding.backBtn.setOnClickListener {
//            kakaoMapViewContainer?.removeAllViews()
//            val intent = Intent(this, MainActivity::class.java)
//            this.startActivity(intent)
//            this.finish()
            onBackPressed()
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, id ->
            when(id){
                R.id.radio_button_missing -> flag = 0
                R.id.radio_button_spot -> flag = 1
            }
//            Log.d("현재 실종/목격 카테코리","${this.category}")
        }

    }


    fun findAddress() {
        val mapReverseGeoCoder =
            MapReverseGeoCoder("830d2ef983929904f477a09ea75d91cc", mapPoint, this, this)
        mapReverseGeoCoder.startFindingAddress()
    }

    // 메모리 누수 방지
    override fun onDestroy() {
        super.onDestroy()
        kakaoMapViewContainer?.removeAllViews() // 맵뷰가 들어있는 ViewGroup에서 모든 뷰를 제거
        mapView?.onPause() // 맵뷰를 일시정지
        mapView = null // 맵뷰 변수를 null로 설정
    }

    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView!!.onResume()
        }
//        this._binding = null
    }

    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    override fun onBackPressed() {
        val backDialog = BacktoMainDialog(this)
        backDialog.show()
    }

    // 위치 권한 설정 확인 함수
    private fun isSetLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        }
    }

    // 위치 권한 설정
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            RequestPermissionCode
        )
        this.recreate()
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
                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
            cancel()
        }
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

        // 커스텀 이미지 마커
        val marker = MapPOIItem().apply {
            markerType = MapPOIItem.MarkerType.CustomImage
            customImageResourceId = R.drawable.marker_nonclick           // 커스텀 마커 이미지
//            selectedMarkerType = MapPOIItem.MarkerType.CustomImage  // 클릭 시 마커 모양 (커스텀)
//            customSelectedImageResourceId = R.drawable.marker_click    // 클릭 시 커스텀 마커 이미지
            isCustomImageAutoscale = true
            setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
            itemName = "실종견"
            mapPoint = p1
            isShowCalloutBalloonOnTouch = false
            tag = 0
        }

        p0?.removePOIItems(p0.poiItems)
        p0!!.addPOIItem(marker)

        checkMessageVisibility(1)
    }

    /*
     지도위에 나올 툴팁 메시지 표시 체크
     */
    internal fun checkMessageVisibility(msgFlag: Int){
        if(msgFlag==0){ // default
            binding.howtoText2.visibility = View.INVISIBLE
            binding.howtoText.visibility = View.VISIBLE
        } else{ // after marker set
            binding.howtoText.visibility = View.INVISIBLE
            binding.howtoText2.visibility = View.VISIBLE
        }
    }

    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {
    }

    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, p1: String?) {
        getAddress = true
        val address = p1!!.split(" ")
        addressAdmin = address[0]
        addressLocality = address[1]
        addressThoroughfare = address[2]
        val pref = this.getPreferences(Context.MODE_PRIVATE)
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

    // 실종/목격 플래그 반환
    fun getFlag() : Int {
        return this.flag
    }


    // 마커 클릭 이벤트 리스너
    class MarkerEventListener(var context: MissingActivity, val lostDialog: DogInfoEnterDialog, val spotDialog : DogInfoEnterDialog2): MapView.POIItemEventListener {
        override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {
            // 마커 클릭 시
            Log.d("markerClick", "ok")
            when(context.getFlag()){
                0 -> lostDialog.show(mapView,poiItem)
                1 -> spotDialog.show(mapView,poiItem)
            }
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

    // 커스텀 말풍선 클래스
    class CustomBalloonAdapter(inflater: LayoutInflater): CalloutBalloonAdapter {

        private val mCalloutBalloon: View = inflater.inflate(R.layout.custom_balloon_layout, null)

        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            return mCalloutBalloon
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View {
            // 말풍선 클릭 시
//            address.text = "getPressedCalloutBalloon"
            return mCalloutBalloon
        }
    }
}


