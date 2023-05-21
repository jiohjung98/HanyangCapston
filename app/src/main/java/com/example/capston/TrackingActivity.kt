package com.example.capston

import MarkerAdapter
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.databinding.ActivityTrackingBinding
import com.example.capston.databinding.SpotBalloonLayoutBinding
import com.example.capston.homepackage.NaviHomeFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.type.LatLng
import kotlinx.android.synthetic.main.activity_tracking.*
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
    MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener{

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

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var recyclerView: RecyclerView
    private lateinit var markerList: MutableList<MarkerData>
    private lateinit var markerAdapter: MarkerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomSheetView = findViewById<View>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)

        // 리사이클러뷰 초기화
        recyclerView = findViewById(R.id.dogList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        markerList = mutableListOf()
        markerAdapter = MarkerAdapter(markerList)
        recyclerView.adapter = markerAdapter

        // 바텀시트 상태 변경 콜백 메소드 예시 (onStateChanged 또는 onSlide 등)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // 바텀시트가 전체 화면으로 확장된 상태일 때

                    // 바텀시트의 리사이클러뷰 요소 찾기
                    val recyclerView = bottomSheet.findViewById<RecyclerView>(R.id.dogList)

                    // 마커 데이터 설정
                    recyclerView.layoutManager = LinearLayoutManager(bottomSheet.context)
                    recyclerView.adapter = MarkerAdapter(markerList)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 바텀시트 슬라이드 시 호출됩니다.
            }
        })


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
                        if(childSnapshot.child("pet_info").child("breed").getValue(String::class.java)!!.trim().equals(breed)) {
                            // 좌표값 전달
                            Log.d("queryMarker", breed!!)
                            setBalloon(childSnapshot)
                            // 마커 데이터 생성
                            val date = childSnapshot.child("date").getValue(String::class.java)
                            val time = snapshot.child("time").getValue(String::class.java)
                            val breed = childSnapshot.child("pet_info").child("breed").getValue(String::class.java)
                            val imageUrl = childSnapshot.child("pet_info").child("image_url").getValue(String::class.java)
                            val lat = snapshot.child("latitude").getValue(Double::class.java)!!
                            val lon = snapshot.child("longitude").getValue(Double::class.java)!!


                            if (date != null && breed != null && imageUrl != null && lat != null && lon != null) {
                                val markerData = MarkerData(
                                    time = date,
                                    breed = breed,
                                    imageUrl = imageUrl,
                                    latitude = lat,
                                    longitude = lon
                                )
                                markerList.add(markerData)
                            }
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
        val view = SpotBalloonLayoutBinding.inflate(layoutInflater)
//        val view: View = layoutInflater.inflate(R.layout.custom_balloon_layout, null)
//        view.blank.text = "이름: 알 수 없음"
        view.timeText.text = "시간: " + date+ " " + time
        view.breedText.text = "견종: " + snapshot.child("pet_info").child("breed").getValue(String::class.java)
        Log.d("MAKE MAKER", view.toString())

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
                    view.enterImage.setImageBitmap(bitmap)
                    // 마커생성
                    makeMarker(snapshot,view.root)
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

//        view.tag = MapPoint.mapPointWithGeoCoord(lat, lon) // 마커의 위도와 경도 값을 view에 저장
//        view.setOnClickListener { itemView ->
//            val mapPoint = itemView.tag as MapPoint
//            moveMapToMarker(mapPoint.mapPointGeoCoord.latitude, mapPoint.mapPointGeoCoord.longitude)
//        }

        val marker = MapPOIItem().apply {
            markerType = MapPOIItem.MarkerType.CustomImage
            customImageResourceId = R.drawable.marker_spot_yellow_64        // 커스텀 마커 이미지
            isCustomImageAutoscale = true
            setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
            itemName = snapshot.key
            mapPoint = MapPoint.mapPointWithGeoCoord(lat, lon)
            isShowCalloutBalloonOnTouch = true
            customCalloutBalloon = view
        }
        mapView!!.addPOIItem(marker)

//        // View 클릭 이벤트 처리
//        view.setOnClickListener {
//            moveMapToMarker(marker.mapPoint.mapPointGeoCoord.latitude, marker.mapPoint.mapPointGeoCoord.longitude)
//        }
        // View 클릭 이벤트 처리
        view.setOnClickListener {
            moveMapToMarker(lat, lon)
        }
    }

    private fun moveMapToMarker(lat: Double, lon: Double) {
        val mapPoint = MapPoint.mapPointWithGeoCoord(lat, lon)
        mapView?.setMapCenterPoint(mapPoint, true)
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

        private val mCalloutBalloon: View = inflater.inflate(R.layout.spot_balloon_layout, null)

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