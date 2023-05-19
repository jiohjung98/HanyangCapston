package com.example.capston

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.capston.databinding.ActivityTrackingBinding
import com.example.capston.databinding.CustomBalloonLayoutBinding
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_tracking.*
import kotlinx.android.synthetic.main.custom_balloon_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.daum.mf.map.api.*
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

    //firebase
    private var database : DatabaseReference = FirebaseDatabase.getInstance().reference

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

    private lateinit var binding: ActivityTrackingBinding

    private var breed : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        breed = intent.getStringExtra("breed")!!.trim()

        listen = MarkerEventListener(this)

        binding.backButton.setOnClickListener {
            goToMain()
        }

        mapView = MapView(this)
        val mapViewContainer = TrackingMapView as ViewGroup
        mapViewContainer.addView(mapView)

        mapView!!.setMapViewEventListener(this)

        mapView!!.setPOIItemEventListener(listen)

//        isSetLocationPermission()
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

        mapView!!.setCalloutBalloonAdapter(MissingActivity.CustomBalloonAdapter(layoutInflater))
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
//        this._binding = null
    }

    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    // 위치 권한 설정 확인 함수
//    private fun isSetLocationPermission() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermission()
//        }
//    }
//
//    // 위치 권한 설정
//    private fun requestPermission() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//            RequestPermissionCode
//        )
//        this.recreate()
//    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
    }

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
//        val lat = p1!!.mapPointGeoCoord.latitude
//        val lon = p1!!.mapPointGeoCoord.longitude
//
//        route.add(arrayListOf(lat, lon))
//
//        mapPoint = p1
//        polyline!!.addPoint(p1)
//        p0!!.removePolyline(polyline)
//        p0.addPolyline(polyline)

        // 변환 주소 가져오기
        if (!getAddress) {
            findAddress(p1!!)
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
        if (p0!!.currentLocationTrackingMode.toString() != "TrackingModeOnWithoutHeadingWithoutMapMoving") {
            p0!!.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
        }

        if (tapTimer != null) {
            tapTimer!!.cancel()
        }
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

    fun findAddress(p1: MapPoint) {
        val mapReverseGeoCoder =
            MapReverseGeoCoder("830d2ef983929904f477a09ea75d91cc", p1, this, this)
        mapReverseGeoCoder.startFindingAddress()
    }

    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {
    }

    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, p1: String?) {
        getAddress = true
        val address = p1!!.split(" ")
        addressAdmin = address[0]
        addressLocality = address[1]
        addressThoroughfare = address[2]
//        val pref = this.getPreferences(Context.MODE_PRIVATE)
//        val edit = pref.edit()
//        edit.putString("addressAdmin", address[0])
//        edit.putString("addressLocality", address[1])
//        edit.putString("addressThoroughfare", address[2])
//        edit.apply()
        queryMarkers()
    }

    /*
     주소 동일한 witness post 가져오기
     */
    private fun queryMarkers(){
        //witness 중 주소(구) 동일한 post 가져오기
        database.child("post").child("witness").orderByChild("address2")
            .equalTo(addressThoroughfare).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //snapshot : 퀴리결과모두
                //childSnapshot : 결과 중 한개
                for(childSnapshot in snapshot.children){
                    // 가져온 데이터를 활용하여 원하는 작업을 수행합니다.
                    // 견종이 동일한 데이터
//                    Log.d("queryMarker", childSnapshot.child("pet_info").child("breed").getValue(String::class.java)!!.trim())
                    if(childSnapshot.child("pet_info").child("breed").getValue(String::class.java)!!.trim().equals(breed)){
                        // 좌표값 전달
                        Log.d("queryMarker", breed!!)
                        setBalloon(childSnapshot)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    /*
     말풍선에 표시할 정보 세팅
     */
    private fun setBalloon(snapshot: DataSnapshot){

        val imageUrl = snapshot.child("pet_info").child("image_url").getValue(String::class.java)!!
        val date = snapshot.child("date").getValue(String::class.java)
        val time = snapshot.child("time").getValue(String::class.java)

        // set text
//        val view = CustomBalloonLayoutBinding.inflate(layoutInflater)
        val view = layoutInflater.inflate(R.layout.custom_balloon_layout,null)
        view.name_text.text = "알수없음"
        view.time_text.text = date+ " " + time
        view.breed_text.text = snapshot.child("pet_info").child("breed").getValue(String::class.java)
        Log.d("MAKE MAKER", view.width.toString())


        // set image
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                withContext(Dispatchers.IO) {
                    connection.connect()
                }
                // 백그라운드 스레드에서 비트맵을 디코딩합니다.
                val bitmap = BitmapFactory.decodeStream(connection.inputStream)
                // UI 스레드에서 이미지를 설정합니다.
                withContext(Dispatchers.Main) {
                    view.enter_image.setImageBitmap(bitmap)
                    // 마커생성
                    makeMarker(snapshot,view)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
    
    
    /*
     지도에 마커 생성하기
     생성시 해당 마커의 말풍선도 설정함
     */
    private fun makeMarker(snapshot:DataSnapshot, view: View){
        val lat = snapshot.child("latitude").getValue(Double::class.java)!!
        val lon = snapshot.child("longitude").getValue(Double::class.java)!!

        val marker = MapPOIItem().apply {
            markerType = MapPOIItem.MarkerType.CustomImage
            customImageResourceId = R.drawable.marker_spot_64        // 커스텀 마커 이미지
            isCustomImageAutoscale = true
            setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
            itemName = snapshot.key
            mapPoint =
                MapPoint.mapPointWithGeoCoord(lat, lon)
            isShowCalloutBalloonOnTouch = true
            customCalloutBalloon = view
//            Log.d("MAKE MAKER", customCalloutBalloon.width.toString())

//            userObject = markerData
        }
        mapView!!.addPOIItem(marker)
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
    class MarkerEventListener(var context: AppCompatActivity): MapView.POIItemEventListener {
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
}
