package com.example.capston

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.capston.databinding.ActivityLoginBinding
import com.example.capston.databinding.ActivityTrackingBinding
import com.example.capston.databinding.CustomBalloonLayoutBinding
import com.example.capston.homepackage.NaviHomeFragment
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_missing.*
import kotlinx.android.synthetic.main.activity_tracking.*
import net.daum.mf.map.api.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.math.*

class TrackingActivity : AppCompatActivity(), MapView.CurrentLocationEventListener,
    MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener  {

    var listen: MarkerEventListener? = null

    var mapView: MapView? = null
    private var polyline: MapPolyline? = null
    var mapPoint: MapPoint? = null

    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    private val RequestPermissionCode = 1
    private var isStart: Boolean = false
    private var isPause: Boolean = false
    private var tapTimer: Timer? = null
    private val route = ArrayList<ArrayList<Double>>()
    private var getAddress: Boolean = false
    private var addressAdmin: String = ""
    private var addressLocality: String = ""
    private var addressThoroughfare: String = ""

    // GeoFire
    private var geoFire : GeoFire? = null
    private var geoQuery : GeoQuery? = null
    private var geoQueryListener : GeoQueryEventListener? = null
    private lateinit var database : DatabaseReference

    private lateinit var binding: ActivityTrackingBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listen = MarkerEventListener(this)

        /* database 변수를 Firebase 데이터베이스의 레퍼런스로 초기화하고, 그 후에 geoFire 객체를 생성하도록 변경.
        이제 database 변수가 초기화되어 오류가 발생하지 않을 것. */
        database = FirebaseDatabase.getInstance().reference
        geoFire = GeoFire(database.child("geofire"))

        database = database.child("post").child("witness")

        binding.backButton.setOnClickListener {
            goToMain()
        }

        mapView = MapView(this)
        val mapViewContainer = TrackingMapView as ViewGroup
        mapViewContainer.addView(mapView)

        mapView!!.setMapViewEventListener(this)

        mapView!!.setPOIItemEventListener(listen)

        isSetLocationPermission()
        mapView!!.setMapViewEventListener(this)
        mapView!!.setZoomLevel(0, true)
        mapView!!.setCustomCurrentLocationMarkerTrackingImage(
            R.drawable.sad_dog_icon_64,
            MapPOIItem.ImageOffset(50, 50)
        )
        mapView!!.setCustomCurrentLocationMarkerImage(
            R.drawable.sad_dog_icon_64,
            MapPOIItem.ImageOffset(50, 50)
        )
        mapView!!.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        Log.d("트래킹", mapView!!.currentLocationTrackingMode.toString())
        mapView!!.setCurrentLocationEventListener(this)
        polyline = MapPolyline()
        polyline!!.tag = 1000
        polyline!!.lineColor = Color.argb(255, 103, 114, 241)

//        mapView!!.setCalloutBalloonAdapter(TrackingActivity.CustomBalloonAdapter(layoutInflater))

        val balloonAdapter = CustomBalloonAdapter(layoutInflater, findViewById(R.id.balloonContainer))
        mapView!!.setCalloutBalloonAdapter(balloonAdapter)

        geoQueryListener = object : GeoQueryEventListener {
            // 아래 구현 - 쿼리로 키가 검색되면 실행됨
            override fun onKeyEntered(key: String, location: GeoLocation) {
                // DB의 마커정보들 불러오기 ->
                database.child(key).get().addOnSuccessListener { task ->
                    val markerData = task.getValue(UserPost::class.java)!!

//                    var balloonBinding= setBalloon(markerData)

                    val marker = MapPOIItem().apply {
                        markerType = MapPOIItem.MarkerType.CustomImage
                        customImageResourceId = R.drawable.marker_click           // 커스텀 마커 이미지
                        isCustomImageAutoscale = true
                        setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
                        itemName = key
                        mapPoint = MapPoint.mapPointWithGeoCoord(location.latitude,location.longitude)
                        isShowCalloutBalloonOnTouch = true
                        tag = key.hashCode()  // 삭제 할때 위해 key 대한 해시 정수로 저장
                        userObject = markerData
                    }

//                    Log.d("customCalloutBalloon",balloonView.toString())

                    mapView!!.addPOIItem(marker)
//                    kakaoMapView.findPOIItemByTag(key.hashCode()).customCalloutBalloon = balloonBinding.root
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

    fun findAddress() {
        val mapReverseGeoCoder =
            MapReverseGeoCoder("830d2ef983929904f477a09ea75d91cc", mapPoint, this, this)
        mapReverseGeoCoder.startFindingAddress()
    }

    // 메모리 누수 방지
    override fun onDestroy() {
        super.onDestroy()
        mapView?.onPause() // 맵뷰를 일시정지
        mapView = null // 맵뷰 변수를 null로 설정
    }

    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView!!.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
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

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

//        // 커스텀 이미지 마커
//        val marker = MapPOIItem().apply {
//            markerType = MapPOIItem.MarkerType.CustomImage
//            customImageResourceId = R.drawable.marker_nonclick           // 커스텀 마커 이미지
////            selectedMarkerType = MapPOIItem.MarkerType.CustomImage  // 클릭 시 마커 모양 (커스텀)
////            customSelectedImageResourceId = R.drawable.marker_click    // 클릭 시 커스텀 마커 이미지
//            isCustomImageAutoscale = true
//            setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
//            itemName = "실종견"
//            mapPoint = p1
//            isShowCalloutBalloonOnTouch = false
//            tag = 0
//        }

//        p0?.removePOIItems(p0.poiItems)
//        p0!!.addPOIItem(marker)
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


    fun goToMain() {
        val dialog = Dialog(this)
        // 다이얼로그 테두리 둥글게 만들기
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dialog.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함
        dialog.setContentView(R.layout.backtomain_dialog)

        val btnOk = dialog.findViewById<TextView>(R.id.yes_btn)
        val btnCancel = dialog.findViewById<TextView>(R.id.no_btn)

        btnOk.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            TrackingMapView.removeAllViews()
            startActivity(intent)
            finish()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    class CustomBalloonAdapter(val inflater: LayoutInflater, private val parent: ViewGroup): CalloutBalloonAdapter {

//        private var viewBinding = CustomBalloonLayoutBinding.inflate(inflater)

        private var viewBinding: CustomBalloonLayoutBinding? = null

        // 오버라이드는 코루틴 쓸 수가 없다. - suspend 불가
        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            if (viewBinding == null) {
                viewBinding = CustomBalloonLayoutBinding.inflate(inflater, parent, false)
            }

            val data : UserPost = poiItem?.userObject as UserPost

            setBalloon(data)

            return viewBinding!!.root
        }

        private fun setBalloon(data : UserPost) {
            viewBinding!!.timeText.text = (data.date + " " + data.time)
            viewBinding!!.nameText.text = "알 수 없음"
            viewBinding!!.breedText.text = data.pet_info?.breed

//            if(fragment.isAdded()) {
//                GlideApp.with(fragment)
//                    .load(Uri.parse(data.pet_info?.image_url.toString()))
//                    .into(viewBinding.enterImage)
//            }

            val url = URL(data.pet_info?.image_url.toString()).openConnection() as HttpURLConnection
            url.doInput = true
            url.connect()
            val bitmap = BitmapFactory.decodeStream(url.inputStream)

            viewBinding!!.enterImage?.setImageBitmap(bitmap)
        }
        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View {
//            // 말풍선 클릭 시
//            return viewBinding!!.root

            // Return the pressed callout balloon view
            return viewBinding?.root ?: getCalloutBalloon(poiItem)
        }
    }


    // 마커 클릭 이벤트 리스너
    class MarkerEventListener(var context: TrackingActivity): MapView.POIItemEventListener {
        override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {

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
