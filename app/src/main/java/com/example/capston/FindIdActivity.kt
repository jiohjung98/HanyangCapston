package com.example.capston

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.capston.databinding.ActivityFindIdBinding
import kotlinx.android.synthetic.main.activity_find_id.*
import kotlinx.android.synthetic.main.activity_find_id.view.*
import kotlinx.android.synthetic.main.activity_login.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

//class FindIdActivity : AppCompatActivity() {
//    private lateinit var binding : ActivityFindIdBinding
//    private val ACCESS_FINE_LOCATION = 1000     // Request Code
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityFindIdBinding.inflate(layoutInflater)
//        val view = binding.root
//        setContentView(view)
//
//        // 위치추적 버튼
//        binding.startBtn.setOnClickListener {
//            if (checkLocationService()) {
//                // GPS가 켜져있을 경우
//                permissionCheck()
//            } else {
//                // GPS가 꺼져있을 경우
//                Toast.makeText(this, "GPS를 켜주세요", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // 추적중지 버튼
//        binding.stopBtn.setOnClickListener {
//            stopTracking()
//        }
//    }
//
//    // 위치 권한 확인
//    private fun permissionCheck() {
//        val preference = getPreferences(MODE_PRIVATE)
//        val isFirstCheck = preference.getBoolean("isFirstPermissionCheck", true)
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // 권한이 없는 상태
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//                // 권한 거절 (다시 한 번 물어봄)
//                val builder = AlertDialog.Builder(this)
//                builder.setMessage("현재 위치를 확인하시려면 위치 권한을 허용해주세요.")
//                builder.setPositiveButton("확인") { dialog, which ->
//                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION)
//                }
//                builder.setNegativeButton("취소") { dialog, which ->
//
//                }
//                builder.show()
//            } else {
//                if (isFirstCheck) {
//                    // 최초 권한 요청
//                    preference.edit().putBoolean("isFirstPermissionCheck", false).apply()
//                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION)
//                } else {
//                    // 다시 묻지 않음 클릭 (앱 정보 화면으로 이동)
//                    val builder = AlertDialog.Builder(this)
//                    builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
//                    builder.setPositiveButton("설정으로 이동") { dialog, which ->
//                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.example.capston"))
//                        startActivity(intent)
//                    }
//                    builder.setNegativeButton("취소") { dialog, which ->
//
//                    }
//                    builder.show()
//                }
//            }
//        } else {
//            // 권한이 있는 상태
//            startTracking()
//        }
//    }
//
//    // 권한 요청 후 행동
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == ACCESS_FINE_LOCATION) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // 권한 요청 후 승인됨 (추적 시작)
//                Toast.makeText(this, "위치 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()
//                startTracking()
//            } else {
//                // 권한 요청 후 거절됨 (다시 요청 or 토스트)
//                Toast.makeText(this, "위치 권한이 거절되었습니다", Toast.LENGTH_SHORT).show()
//                permissionCheck()
//            }
//        }
//    }
//
//    // GPS가 켜져있는지 확인
//    private fun checkLocationService(): Boolean {
//        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//    }
//
//    // 위치추적 시작
//    @SuppressLint("MissingPermission")
//    private fun startTracking() {
//        binding.kakaoMapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
////        binding.kakaoMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633), true)
//        val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        val userNowLocation: Location? = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
//        //위도 , 경도
//        val uLatitude = userNowLocation?.latitude
//        val uLongitude = userNowLocation?.longitude
//        val uNowPosition = MapPoint.mapPointWithGeoCoord(uLatitude!!, uLongitude!!)
//
//        // 현 위치에 마커 찍기
//        val marker = MapPOIItem()
//        marker.itemName = "현 위치"
//        marker.mapPoint =uNowPosition
//        marker.markerType = MapPOIItem.MarkerType.BluePin
//        marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
//        binding.kakaoMapView.addPOIItem(marker)
//    }
//
//    // 위치추적 중지
//    private fun stopTracking() {
//        binding.kakaoMapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
////        binding.kakaoMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 137.00557633), true)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopTracking()
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
//        return true
//    }


//}

//class FindIdActivity : AppCompatActivity(), MapView.POIItemEventListener,
//    MapView.MapViewEventListener {
//    val currentLocationMarker: MapPOIItem = MapPOIItem()
//    var centerPoint: MapPoint? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_find_id)
//
////         레이아웃 뷰 onClick 설정
//        stop_btn.setOnClickListener {
//            finish()
//        }
//
//        // 카카오 지도 캐시 저장 옵션 활성화
//        if (MapView.isMapTilePersistentCacheEnabled()) {
//            MapView.setMapTilePersistentCacheEnabled(true)
//        }
//
//        // 내 현재 위치 불러오기
//        start_btn.setOnClickListener {
//            val locationRequestCode = 1002
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            )
//                if (ActivityCompat.shouldShowRequestPermissionRationale(
//                        this,
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    )
//                ) {
//                    ActivityCompat.requestPermissions(
//                        this,
//                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                        locationRequestCode
//                    )
//                }
//            // 뷰의 트래킹 모드 설정
//            kakaoMapView.currentLocationTrackingMode =
//                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
//
//            println("eventPoint: $centerPoint")
//
//            // centerPoint null 아니면 좌표값 설정
//            if (centerPoint != null) {
//                val point = MapPoint.mapPointWithGeoCoord(
//                    centerPoint!!.mapPointGeoCoord.latitude,
//                    centerPoint!!.mapPointGeoCoord.longitude
//                )
//                // 좌표값 setter
//                kakaoMapView.setMapCenterPoint(point, false)
//                createCustomMaker(kakaoMapView)
//            }
//        }
//
//        // POI = point of interest : 주변 마커들을 의미함
//        // 맵뷰 리스너, POI
//        kakaoMapView.setMapViewEventListener(this)
//        kakaoMapView.setPOIItemEventListener(this)
//
//
//        // 현재 위치 리스너
//        // 현재 위치 업데이트
//        class CurrentLocationListener :
//            MapView.CurrentLocationEventListener {
//            override fun onCurrentLocationUpdateFailed(p0: MapView?) {
//                Toast.makeText(applicationContext, "위치 정보를 불러오는데 실패했습니다.", Toast.LENGTH_LONG).show()
//            }
//
//            override fun onCurrentLocationUpdate(
//                mapView: MapView?,
//                point: MapPoint?,
//                accuracy: Float
//            ) {
//                println("point: $point")
//                if (point != null) {
//                    // 현재 위치 업데이트
//                    centerPoint = point
//                    currentLocationMarker.moveWithAnimation(point, false)
//                    currentLocationMarker.alpha = 1f
//                    if (mapView != null) {
//                        kakaoMapView.setMapCenterPoint(
//                            MapPoint.mapPointWithGeoCoord(
//                                point.mapPointGeoCoord.latitude,
//                                point.mapPointGeoCoord.longitude
//                            ), false
//                        )
//                    }
//                }
//            }
//
//            override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
//                Toast.makeText(applicationContext, "위치 요청이 취소되었습니다.", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
//                Toast.makeText(applicationContext, "단말의 각도 값 요청", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // 현재 위치 리스너 설정
//        class CustomCalloutBalloonAdapter : CalloutBalloonAdapter {
//            private var mCalloutBalloon: View =
//                layoutInflater.inflate(R.layout.activity_find_id, null)
//
//            // 마커 선택시 값 변경
//            override fun getCalloutBalloon(poiItem: MapPOIItem): View {
//                mCalloutBalloon.activity_map_store_name.text = "가게 이름"
//                return mCalloutBalloon
//            }
//
//            override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View? {
//                return null
//            }
//        }
//
//        // 현재 위치 리스너
//        this.kakaoMapView.setCurrentLocationEventListener(CurrentLocationListener())
//
//        // 구현한 CalloutBalloonAdapter 등록
//        this.kakaoMapView.setCalloutBalloonAdapter(CustomCalloutBalloonAdapter())
//    }
//
//    private fun createCustomMaker(mapView: MapView) {
//        val customMaker = MapPOIItem()
//        customMaker.itemName = "Custom Marker"
//        customMaker.tag = 1
//        println("centerPoint: $centerPoint")
//        customMaker.mapPoint = centerPoint
//        customMaker.markerType = MapPOIItem.MarkerType.BluePin
////        customMaker.customImageResourceId = R.drawable.ic_location_mark_01
//        customMaker.isCustomImageAutoscale = false
//        currentLocationMarker.setCustomImageAnchor(0.5f, 0.5f)
//        kakaoMapView.addPOIItem(currentLocationMarker)
//        kakaoMapView.selectPOIItem(currentLocationMarker, true)
//        kakaoMapView.setMapCenterPoint(centerPoint, false)
//    }
//
//
//    private fun createStoreMaker(mapView: MapView, storePoint: MapPoint) {
//        val customMaker = MapPOIItem()
//        customMaker.itemName = "Custom Marker"
//        customMaker.tag = 1
//        println("centerPoint: $storePoint")
//        customMaker.mapPoint = storePoint
//        customMaker.markerType = MapPOIItem.MarkerType.BluePin
////        customMaker.customImageResourceId = R.drawable.ic_location_mark_01
//        customMaker.isCustomImageAutoscale = false
//        currentLocationMarker.setCustomImageAnchor(0.5f, 0.5f)
//        kakaoMapView.addPOIItem(currentLocationMarker)
//        kakaoMapView.selectPOIItem(currentLocationMarker, true)
//        kakaoMapView.setMapCenterPoint(centerPoint, false)
//    }
//
//    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
//
//    }
//
//    override fun onCalloutBalloonOfPOIItemTouched(
//        p0: MapView?,
//        p1: MapPOIItem?,
//        p2: MapPOIItem.CalloutBalloonButtonType?
//    ) {
//
//    }
//
//    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
//
//    }
//
//    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
//
//    }
//
//    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
//
//    }
//
//    override fun onMapViewInitialized(p0: MapView?) {
//    }
//
//    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
//
//    }
//
//    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
//
//    }
//
//    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
//
//    }
//
//    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
//
//    }
//
//    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
//
//    }
//
//    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
//
//    }
//
//    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
//
//    }
//}
