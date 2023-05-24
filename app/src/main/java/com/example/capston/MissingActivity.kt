package com.example.capston

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.capston.databinding.ActivityMissingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_missing.*
import net.daum.mf.map.api.*
import net.daum.mf.map.api.MapView
import java.util.*
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
    internal var lostLauncher: ActivityResultLauncher<Intent>? = null
    internal var witLauncher: ActivityResultLauncher<Intent>? = null
    private var uri : Uri? = null

    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    private val RequestPermissionCode = 1
    var mapView: MapView? = null
    private var polyline: MapPolyline? = null
    var mapPoint: MapPoint? = null
    private var currentLocation: MapPoint? = null

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
    private lateinit var witDialog : DogInfoEnterDialog2

    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = ActivityMissingBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.show()

        lostDialog = DogInfoEnterDialog(this)
        lostDialog.setLauncher()

        witDialog = DogInfoEnterDialog2(this)
        witDialog.setLauncher()


        listen = MarkerEventListener(this, lostDialog, witDialog)

        // 뷰 추가 전 기존 뷰 삭제
        kakaoMapViewContainer?.removeAllViews()

        mapView = MapView(this)
        val mapViewContainer = kakaoMapView4 as ViewGroup
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


        // 좌측 뒤로가기 버튼
        binding.backBtn.setOnClickListener {
            goToMain()
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, id ->
            when(id){
                R.id.radio_button_missing -> flag = 0
                R.id.radio_button_spot -> flag = 1
            }
            Log.d("FLAG","${this.flag}")
        }
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
    }

    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    override fun onBackPressed() {
        goToMain()
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
        createMarker(p0, p1)
        checkMessageVisibility(1)
        findAddress(p1!!)
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

    fun findAddress(p1:MapPoint) {
        val mapReverseGeoCoder =
            MapReverseGeoCoder("830d2ef983929904f477a09ea75d91cc", p1, this, this)
        mapReverseGeoCoder.startFindingAddress()
    }

    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, p1: String?) {
        getAddress = true
        val address = p1!!.split(" ")
        addressAdmin = address[0]
        addressLocality = address[1]
        addressThoroughfare = address[2]
    }

    // 실종/목격 플래그 반환
    fun getFlag() : Int {
        return this.flag
    }

    private fun createMarker(p0: MapView?, p1: MapPoint?) {
        // 커스텀 이미지 마커
        val marker = MapPOIItem().apply {
            markerType = MapPOIItem.MarkerType.CustomImage
            customImageResourceId = R.drawable.marker_nonclick           // 커스텀 마커 이미지
            isCustomImageAutoscale = true
            setCustomImageAnchor(0.5f, 1.0f)    // 마커 이미지 기준점
            itemName = "실종견"
            mapPoint = p1
            isShowCalloutBalloonOnTouch = false
            tag = 0
        }

        p0?.removePOIItems(p0.poiItems)
        p0!!.addPOIItem(marker)
    }


    // 마커 클릭 이벤트 리스너
    class MarkerEventListener(var context: MissingActivity, val lostDialog: DogInfoEnterDialog, val spotDialog : DogInfoEnterDialog2): MapView.POIItemEventListener {
        override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {
            // 마커 클릭 시
//            Log.d("markerClick", "ok")
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
            kakaoMapView4.removeAllViews()
            startActivity(intent)
            finish()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun getAddressLocality() : String{
        return addressLocality
    }

    fun getAddressThoroughfare() : String{
        return addressThoroughfare
    }

}

