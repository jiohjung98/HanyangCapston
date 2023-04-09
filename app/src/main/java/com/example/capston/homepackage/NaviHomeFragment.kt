package com.example.capston.homepackage

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.capston.*
import com.example.capston.databinding.FragmentNaviHomeBinding
import com.example.capston.databinding.LostDogInfoBinding
import kotlinx.android.synthetic.main.activity_walk.*
import kotlinx.android.synthetic.main.ballon_layout.*
import kotlinx.android.synthetic.main.fragment_navi_home.*
import kotlinx.android.synthetic.main.lost_dog_info.*
import net.daum.mf.map.api.*
import net.daum.mf.map.api.MapView
import java.util.*
import kotlin.concurrent.timer
import kotlin.math.*


var RecordPage = HomeRecord()

/*
 * 메인화면, 첫번째 메뉴, 지도
 */
class NaviHomeFragment : Fragment(), MapView.CurrentLocationEventListener,
    MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    var listen: MarkerEventListener? = null

    private var kakaoMapViewContainer: FrameLayout? = null

    //내 반려견 실종 버튼
    var validLostBtn: Boolean = false

    //실종 반려견 발견 버튼
    var validFindBtn: Boolean = false


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

    private var _binding: FragmentNaviHomeBinding? = null
    private val binding get() = _binding!!

    private var binding2: LostDogInfoBinding? = null

    private lateinit var mainActivity: MainActivity

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
    ): View? {
            _binding = FragmentNaviHomeBinding.inflate(inflater, container, false)

        binding.lostBtn.setOnClickListener {
            validLostBtn = true
            dog_lost_txt.visibility = View.VISIBLE
            dog_find_txt.visibility = View.INVISIBLE
        }

        binding.findBtn.setOnClickListener {
            validFindBtn = true
            dog_find_txt.visibility = View.VISIBLE
            dog_lost_txt.visibility = View.INVISIBLE
        }

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity

        val view = binding.root


        return view
    }




    fun findAddress() {
        val mapReverseGeoCoder =
            MapReverseGeoCoder("830d2ef983929904f477a09ea75d91cc", mapPoint, this, requireActivity())
        mapReverseGeoCoder.startFindingAddress()
    }

//    // fragment 액션바 보여주기(선언안해주면 다른 프레그먼트에서 선언한 .hide() 때문인지 모든 프레그먼트에서 액션바 안보임
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        (activity as AppCompatActivity).supportActionBar?.show()
//
//        kakaoMapView = MapView(mainActivity)
//        kakaoMapViewContainer.addView(kakaoMapView)
//
//        listen = MarkerEventListener(mainActivity)
//
//        kakaoMapView.setPOIItemEventListener(listen)
//
//        isSetLocationPermission()
//        kakaoMapView!!.setMapViewEventListener(this)
//        kakaoMapView!!.setZoomLevel(0, true)
//        kakaoMapView!!.setCustomCurrentLocationMarkerTrackingImage(
//            R.drawable.labrador_icon,
//            MapPOIItem.ImageOffset(50, 50)
//        )
//        kakaoMapView!!.setCustomCurrentLocationMarkerImage(
//            R.drawable.labrador_icon,
//            MapPOIItem.ImageOffset(50, 50)
//        )
//        kakaoMapView!!.currentLocationTrackingMode =
//            MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
//        Log.d("트래킹", kakaoMapView!!.currentLocationTrackingMode.toString())
//        kakaoMapView!!.setCurrentLocationEventListener(this)
//        polyline = MapPolyline()
//        polyline!!.tag = 1000
//        polyline!!.lineColor = Color.argb(255, 103, 114, 241)
//
//        kakaoMapView.setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater))
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        listen = MarkerEventListener(mainActivity)

        // 뷰 추가 전 기존 뷰 삭제
        kakaoMapViewContainer?.removeAllViews()

        val kakaoMapView = view.findViewById<MapView>(R.id.kakaoMapView)
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
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
        Log.d("트래킹", kakaoMapView.currentLocationTrackingMode.toString())
        kakaoMapView.setCurrentLocationEventListener(this)
        polyline = MapPolyline()
        polyline!!.tag = 1000
        polyline!!.lineColor = Color.argb(255, 103, 114, 241)

        kakaoMapView.setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater))
    }

    // 커스텀 말풍선 클래스
    class CustomBalloonAdapter(inflater: LayoutInflater): CalloutBalloonAdapter {

        var mainActivity: MainActivity? = null


        private val mCalloutBalloon: View = inflater.inflate(R.layout.ballon_layout, null)
        val name: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_name)

        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            // 마커 클릭 시 나오는 말풍선
            name.text = poiItem?.itemName   // 해당 마커의 정보 이용 가능

            return mCalloutBalloon
            }


        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View {
            // 말풍선 클릭 시
//            address.text = "getPressedCalloutBalloon"
            return mCalloutBalloon
        }
    }

    // 메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
        kakaoMapViewContainer?.removeAllViews() // 맵뷰가 들어있는 ViewGroup에서 모든 뷰를 제거
        mapView?.onPause() // 맵뷰를 일시정지
        mapView = null // 맵뷰 변수를 null로 설정
    }

    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView!!.onResume()
        }
        this._binding = null
    }

    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
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

        dog_lost_txt.visibility = View.INVISIBLE
        dog_find_txt.visibility = View.INVISIBLE

        if (validLostBtn) {

            val dialog = DogInfoEnterDialog()
            dialog.show(childFragmentManager, "CustomDialog")
        }

        if (validFindBtn) {

            val dialog = Dialog(requireContext())
            // 다이얼로그 테두리 둥글게 만들기
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
            dialog.setCancelable(true)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함
            dialog.setContentView(R.layout.lost_dog_info)

            val btnSave = dialog.findViewById<TextView>(R.id.yes_btn)
            val btnCancel = dialog.findViewById<TextView>(R.id.no_btn)
            val receiveTime = dialog.findViewById<EditText>(R.id.time_input)
            val receiveInfo = dialog.findViewById<EditText>(R.id.content_input)

            btnSave.setOnClickListener {
                val marker = MapPOIItem().apply {
                    markerType = MapPOIItem.MarkerType.YellowPin
                    itemName = "실종견"
                    mapPoint = p1
                    tag = 0
                }
                kakaoMapView!!.addPOIItem(marker)

                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

            validFindBtn = false
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
        Log.d("markerClick", "ok")

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

