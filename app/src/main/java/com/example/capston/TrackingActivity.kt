package com.example.capston

import MarkerAdapter
import OnItemClickListener
import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capston.data.UserPost
import com.example.capston.databinding.ActivityTrackingBinding
import com.example.capston.databinding.MarkerclickSpotdogBinding
import com.example.capston.databinding.SpotBalloonLayoutBinding
import com.example.capston.homepackage.NaviHomeFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.type.LatLng
import kotlinx.android.synthetic.main.activity_tracking.*
import kotlinx.android.synthetic.main.layout_community_rc_view_item.*
import kotlinx.android.synthetic.main.layout_community_rc_view_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.daum.mf.map.api.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

class TrackingActivity : AppCompatActivity(), MapView.CurrentLocationEventListener,
    MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener{

    var listen: MarkerEventListener? = null

    var mapView: MapView? = null
    val _mapView get() = mapView
    private var polyline: MapPolyline? = null
    var mapPoint: MapPoint? = null
    private var currentLocation: MapPoint? = null

    //firebase
    private var database : DatabaseReference = FirebaseDatabase.getInstance().reference
    private var functions : FirebaseFunctions = FirebaseFunctions.getInstance()

    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)

    private var tapTimer: Timer? = null

    private var getAddress: Boolean = false
    private var addressAdmin: String = ""
    private var addressLocality: String = ""
    private var addressThoroughfare: String = ""

    private lateinit var binding: ActivityTrackingBinding

    // 이전 액티비티 전달값
    private var breed : String? = null
    private var imageUrl : String? = null
    private var address2 : String? = null

    // 로딩 다이얼로그
    var loading : LoadingDialog? = null


    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var recyclerView: RecyclerView
    private lateinit var markerList: MutableList<MarkerData>
    val _markerList get() = markerList
    private lateinit var markerAdapter: MarkerAdapter

    private var postImages = ArrayList<String>()
    private var postKeys = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        breed = intent.getStringExtra("breed")!!.trim()
        imageUrl = intent.getStringExtra("imageUrl")!!.trim()
        address2 = intent.getStringExtra("address2")!!.trim()

        val bottomSheetView = findViewById<View>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)

        // 리사이클러뷰 초기화
        recyclerView = findViewById(R.id.dogList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        markerList = mutableListOf()

        binding.locationBtn.setOnClickListener {
            mapView?.setMapCenterPoint(currentLocation,true)
        }

        // itemClickListener를 생성하여 MarkerAdapter에 전달
        markerAdapter = MarkerAdapter(markerList, object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val selectedMarker = markerList[position]
                val selectedMapPoint = MapPoint.mapPointWithGeoCoord(selectedMarker.latitude, selectedMarker.longitude)
                mapView?.setMapCenterPoint(selectedMapPoint, true)

                // 바텀시트를 닫기 위해 state를 STATE_COLLAPSED로 변경
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        })
        recyclerView.adapter = markerAdapter

        // 바텀시트 상태 변경 콜백 메소드 예시 (onStateChanged 또는 onSlide 등)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
//                    // 바텀시트가 전체 화면으로 확장된 상태일 때
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        val recyclerView = bottomSheet.findViewById<RecyclerView>(R.id.dogList)
                        recyclerView.layoutManager = LinearLayoutManager(bottomSheet.context)
                        recyclerView.adapter = markerAdapter
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 바텀시트 슬라이드 시 호출됩니다.
            }
        })

        listen = MarkerEventListener(this)

        binding.backButton.setOnClickListener {
            goToMain()
        }

        mapView = MapView(this)
        val mapViewContainer = TrackingMapView as ViewGroup
        mapViewContainer.addView(mapView)

        mapView!!.setMapViewEventListener(this)

        mapView!!.setPOIItemEventListener(listen)

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

        loading = LoadingDialog(this@TrackingActivity)
        loading!!.show()
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
        // 변환 주소 가져오기
        if (!getAddress) {
            findAddress(p1!!)
        }
        currentLocation = p1
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
    private fun queryMarkers() {
        markerList.clear() // 기존 마커 정보를 모두 제거

        // witness 중 주소(구) 동일한 post 가져오기
        database.child("post").child("witness").orderByChild("address2")
            .equalTo(addressThoroughfare).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val existingCoordinates = mutableSetOf<String>() // 중복 체크를 위한 Set

                    for (childSnapshot in snapshot.children) {
                        // 견종이 동일한 데이터인지 확인
                        if (childSnapshot.child("pet_info").child("breed").getValue(String::class.java)!!.trim()
                                .equals(breed)
                        ) {
                            // 좌표값 전달
                            Log.d("queryMarker", breed!!)

                            // 마커 데이터 생성
                            val date = childSnapshot.child("date").getValue(String::class.java)
                            val time = childSnapshot.child("time").getValue(String::class.java)
                            val breed = childSnapshot.child("pet_info").child("breed").getValue(String::class.java)
                            val imageUrl = childSnapshot.child("pet_info").child("image_url").getValue(String::class.java)
                            val lat = childSnapshot.child("latitude").getValue(Double::class.java)
                            val lon = childSnapshot.child("longitude").getValue(Double::class.java)
                            val phone = childSnapshot.child("contact").getValue(String::class.java)

                            // 쿼리한 포스트의 이미지url 저장
                            postImages.add(imageUrl!!)
                            postKeys.add(childSnapshot.key!!)

                            if (date != null && time != null && breed != null && imageUrl != null
                                    && lat != null && lon != null && phone != null) {
                                val coordinate = "$lat,$lon"
                                // 중복 체크
                                if (!existingCoordinates.contains(coordinate)) {
                                    setBalloon(childSnapshot)
                                    markerList.add(
                                        MarkerData(
                                            date = date,
                                            time = time,
                                            breed = breed,
                                            imageUrl = imageUrl,
                                            latitude = lat,
                                            longitude = lon,
                                            phone = phone
                                        )
                                    )
                                    existingCoordinates.add(coordinate)
                                }
                            }
                        }
                    }
                    // 마커 데이터가 변경되었으므로, 어댑터에 변경 내용을 알려줍니다.
                    markerAdapter.notifyDataSetChanged()
                    // for문 종료 = 모든 쿼리 끝난 뒤 유사도 체크
                    similarity()
                }
                override fun onCancelled(error: DatabaseError) {
                    // 오류 처리
                }
            })
    }

    /*
     말풍선에 표시할 정보 세팅
     */
    fun setBalloon(snapshot: DataSnapshot){

        val imageUrl = snapshot.child("pet_info").child("image_url").getValue(String::class.java)!!
        val date = snapshot.child("date").getValue(String::class.java)
        val time = snapshot.child("time").getValue(String::class.java)

        // set text
        val view = SpotBalloonLayoutBinding.inflate(layoutInflater)
        view.timeText.text = "시간: " + date+ " " + time
        view.breedText.text = "견종: " + snapshot.child("pet_info").child("breed").getValue(String::class.java)
//        Log.d("MAKE MAKER", view.toString())

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

        val marker = MapPOIItem().apply {
            markerType = MapPOIItem.MarkerType.CustomImage
            customImageResourceId = R.drawable.marker_spot_yellow_64        // 커스텀 마커 이미지
            isCustomImageAutoscale = true
            setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
            itemName = snapshot.key
            userObject = snapshot.getValue(UserPost::class.java)!!
            mapPoint = MapPoint.mapPointWithGeoCoord(lat, lon)
            isShowCalloutBalloonOnTouch = true
            customCalloutBalloon = view
            tag = (snapshot.key).hashCode()
//            Log.d("MAKE MAKER", customCalloutBalloon.width.toString())

//            userObject = markerData
        }
        mapView!!.addPOIItem(marker)
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
            val dialog = Dialog(context)
            // 다이얼로그 테두리 둥글게 만들기
            val binding = MarkerclickSpotdogBinding.inflate(context.layoutInflater)
            val userPost = poiItem?.userObject as UserPost
            val petInfo = userPost.pet_info!!

            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
            dialog.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

            binding.breedTitle.text = petInfo.breed
            binding.genderText.text = if(petInfo.gender==0) "여아" else "남아"
            binding.timeReceive.text = userPost.date + " " + userPost.time
            binding.contentReceive.text = userPost.content
            binding.phoneReceive.text = userPost.contact

            GlideApp.with(context).load(petInfo.image_url).into(binding.imageArea)

            dialog.setContentView(binding.root)

            val btnCall = binding.callBtn

            val receiveNum = binding.phoneReceive
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

    override fun onBackPressed() {
        goToMain()
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
            dialog.dismiss()
            startActivity(intent)
            finish()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun similarity(){
        val data = hashMapOf(
            "target_image" to this.imageUrl,
            "query_images" to postImages
        )
        functions.getHttpsCallable("similarityCheck")
            .call(data)
            .addOnSuccessListener { task->
                Log.d("similarity",task.data.toString())
                loading!!.dismiss()
            }
            .addOnFailureListener {
                Log.d("similarity","FAIL")
            }
    }
}