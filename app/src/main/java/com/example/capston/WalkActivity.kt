package com.example.capston

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.capston.databinding.ActivityWalkBinding
import kotlinx.android.synthetic.main.activity_walk.*
import net.daum.mf.map.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.timer
import kotlin.math.*

/*
 * 다이얼로그에서 산책 시작하고나서 나오는 화면, 지도
 */
class WalkActivity : AppCompatActivity(), MapView.CurrentLocationEventListener,
    MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    private val RequestPermissionCode = 1
    private var mapView: MapView? = null
    private var polyline: MapPolyline? = null
    private var mapPoint: MapPoint? = null
    private var prevLat: Double? = null
    private var prevLon: Double? = null
    private var walkingDistance: Double = 0.0
    private var walkingCalorie: Double = 0.0
    private var isStart: Boolean = false
    private var isPause: Boolean = false
    private var tapTimer: Timer? = null
    private val route = ArrayList<ArrayList<Double>>()
    private val toiletLoc = ArrayList<ArrayList<Double>>()
    private val walkingAmounts = ArrayList<Double>()
    private var walkingTimer: Timer? = null
    private var runningDogImageTimer: Timer? = null
    private var runningDogImageCounter: Int = 1
    private var time = 0
    private var isTimerRunning: Boolean = false
    private var getAddress: Boolean = false
    private var addressAdmin: String = ""
    private var addressLocality: String = ""
    private var addressThoroughfare: String = ""
    private var animal = ArrayList<String>()
    private var fullAmount = ArrayList<Double>()

    lateinit var viewBinding: ActivityWalkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityWalkBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        pauseFab.visibility = View.GONE
        toiletFab.visibility = View.GONE

        // 현재 위치
        initView()

        playFab.setOnClickListener {
            if (!isPause) {
                isStart = true
            } else {
                isPause = false
            }
            timerSet()
            runningDog()
            pauseFab.visibility = View.VISIBLE
            toiletFab.visibility = View.VISIBLE
            playFab.visibility = View.GONE
            resetFab.visibility = View.GONE
        }
        pauseFab.setOnClickListener {
            isPause = true
            stopRunningDog()
            timerSet()
            pauseFab.visibility = View.GONE
            toiletFab.visibility = View.GONE
            playFab.visibility = View.VISIBLE
            resetFab.visibility = View.VISIBLE
//            pauseWalking()

        }
        resetFab.setOnClickListener {
            isStart = false
            if (walkingDistance != 0.0) {
                finishWalking()
                submitResult()
            }
            pauseFab.visibility = View.GONE
            toiletFab.visibility = View.GONE
            playFab.visibility = View.VISIBLE
            resetFab.visibility = View.VISIBLE

        }
        toiletFab.setOnClickListener {
            if (isStart && mapPoint != null) {
                Log.d("goooood", "hihihi")
                toiletActivity()
            }
        }
        camera_btn.setOnClickListener {
            goToMain()
        }
    }

    fun findAddress() {
        val mapReverseGeoCoder =
            MapReverseGeoCoder("830d2ef983929904f477a09ea75d91cc", mapPoint, this, this)
        mapReverseGeoCoder.startFindingAddress()
    }

    private fun initView() {
        // 위치 권한 설정 확인
        isSetLocationPermission()

        mapView = MapView(this)
        val mapViewContainer = kakaoMapView2 as ViewGroup
        mapViewContainer.addView(mapView)

        mapView!!.setMapViewEventListener(this)

        // 현위치 트래킹 모드 ON
        mapView!!.setZoomLevel(0, true)
        mapView!!.setCustomCurrentLocationMarkerTrackingImage(
            R.drawable.lovely_dog_icon_64,
            MapPOIItem.ImageOffset(50, 50)
        )
        mapView!!.setCustomCurrentLocationMarkerImage(
            R.drawable.lovely_dog_icon_64,
            MapPOIItem.ImageOffset(50, 50)
        )
        mapView!!.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
        Log.d("트래킹", mapView!!.currentLocationTrackingMode.toString())
        mapView!!.setCurrentLocationEventListener(this)
        polyline = MapPolyline()
        polyline!!.tag = 1000
        polyline!!.lineColor = Color.argb(255, 221, 135, 69)
    }

    // 일시 정지
    private fun pauseWalking() {
        stopRunningDog()
        if (mapView != null) {
            mapView!!.currentLocationTrackingMode =
                MapView.CurrentLocationTrackingMode.TrackingModeOff
            isPause = true
        }
    }

    // 재시작
    private fun restartWalking() {
        mapView!!.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
    }

    // 산책 종료
    private fun finishWalking() {
        stopRunningDog()
        mapView!!.addPolyline(polyline)
        mapView!!.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
        val mapPointBounds = MapPointBounds(polyline!!.mapPoints)
        mapView!!.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, 100))

        // 시작 지점 표시
        val startMarker = MapPOIItem()
        startMarker.itemName = "시작점"
        startMarker.mapPoint = polyline!!.getPoint(0)
        startMarker.markerType = MapPOIItem.MarkerType.CustomImage
        startMarker.customImageResourceId =
            R.drawable.start_walking_icon
        startMarker.setCustomImageAnchor(0.5f, 0.5f)
        startMarker.isCustomImageAutoscale = false
        startMarker.isShowCalloutBalloonOnTouch = false
        mapView!!.addPOIItem(startMarker)

        // 산책 충족량 확인
        fullAmount.forEach(fun(amount) {
            walkingAmounts.add((walkingCalorie / amount) * 100)
        })
    }

    private fun submitResult() {
        val pref = getSharedPreferences("pref", MODE_PRIVATE)
        val userToken = pref.getString("userToken", "")
        val walkingRetrofit = WalkingRetrofitCreators(this).WalkingRetrofitCreator()
        walkingRetrofit.createWalking(
            userToken!!, walkingCalorie, walkingDistance, time, walkingAmounts,
            addressAdmin, addressLocality, addressThoroughfare, animal, route, toiletLoc
        ).enqueue(object : Callback<CreateWalkingResultModel> {
            override fun onFailure(call: Call<CreateWalkingResultModel>, t: Throwable) {
                Log.d("DEBUG", " Walking Retrofit failed!!")
                Toast.makeText(
                    this@WalkActivity,
                    "산책 등록에 실패하였습니다. 네트워크를 확인해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onResponse(
                call: Call<CreateWalkingResultModel>,
                response: Response<CreateWalkingResultModel>
            ) {
                val error = response.body()?.error
                val walkingId = response.body()?.walkingId
                Log.d("ERROR", error.toString())

                // 등록에 실패했을 때 후 처리
                if (error == 1) {
                    Toast.makeText(
                        this@WalkActivity,
                        "산책 등록에 실패하였습니다. 네트워크를 확인해주세요.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // 산책 등록 후 처리 - 액티비티 이동
                if (error == null || error == 0) {
                    Toast.makeText(
                        this@WalkActivity,
                        "3초 후 산책이 종료됩니다.",
                        Toast.LENGTH_LONG
                    ).show()
                    timer(period = 3000, initialDelay = 3000) {
                        val intent = Intent(this@WalkActivity, Statics::class.java)
                        intent.putExtra("walkingId", walkingId)
                        startActivity(intent)
                        finish()
                        cancel()
                    }
                }
            }
        })
    }

    // 배변활동 표시
    private fun toiletActivity() {
        val marker = MapPOIItem()
        marker.itemName = ""
        marker.isShowCalloutBalloonOnTouch = false
        marker.mapPoint = mapPoint
        marker.markerType = MapPOIItem.MarkerType.CustomImage
        marker.customImageResourceId =
            R.drawable.ddong_32
        marker.isCustomImageAutoscale = false
        marker.setCustomImageAnchor(0.5f, 1.0f)
        mapView!!.addPOIItem(marker)
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

        if (prevLat == null && prevLon == null) {
            prevLat = lat
            prevLon = lon
            return
        } else {
            val distance = haversine(prevLat!!, prevLon!!, lat, lon)

            // 이동 거리 표시
            walkingDistance += distance
            if (walkingDistance < 1000) {
                distanceId.text = String.format("%.1f", walkingDistance)
            } else {
                digitId.text = "km"
                distanceId.text = String.format("%.3f", meterToKillo(walkingDistance))
            }

            // 소모 칼로리 표시
            walkingCalorie += distance * 0.026785714  // 1m당 소모 칼로리
            Log.d("태그", "칼로리: " + walkingCalorie)
            calorieView.text = String.format("%.2f", walkingCalorie)
                prevLat = lat
                prevLon = lon
        }

        // 변환 주소 가져오기
        if (!getAddress) {
            findAddress()
        }
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {

    }

    // 위도, 경도를 거리로 변환 - 리턴 값: Meter 단위
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6372.8
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(rLat1) * cos(rLat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c * 1000
    }

    private fun meterToKillo(meter: Double): Double {
        return meter / 1000
    }


    private fun timerSet() {
        isTimerRunning = !isTimerRunning
        if (isTimerRunning)
            startTimer()
        else
            pauseTimer()
    }

    private fun startTimer() {
        walkingTimer = timer(period = 10) {
            time++
//            val hour = (time / 144000) % 24 // 1시간
            val min = (time / 6000) % 60 // 1분
            val sec = (time / 100) % 60 // 1초
            val milli = time % 100 // 0.01초

            runOnUiThread {
                if (min < 10) { // 분
                    minTextView.text = "0$min"
                } else {
                    minTextView.text = "$min"
                }

                if (sec < 10) { // 초
                    secTextView.text = "0$sec"
                } else {
                    secTextView.text = "$sec"
                }

                if (milli < 10) {
                    milliTextView.text = "0$milli"
                } else {
                    milliTextView.text = "$milli"
                }
            }
        }
    }

    private fun pauseTimer() {
        walkingTimer?.cancel()
    }

    private fun runningDog() {
        runningDogImageTimer = timer(period = 100) {
            runOnUiThread {
//                runningDogImg.setBackgroundResource(runningDogImage[runningDogImageCounter++])
                if (runningDogImageCounter > 7) {
                    runningDogImageCounter = 0
                }
            }
        }
    }

    private fun stopRunningDog() {
        runningDogImageTimer!!.cancel()
//        runningDogImg.setBackgroundResource(runningDogImage[0])
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
    }

    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {
    }

    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, p1: String?) {
        getAddress = true
        val address = p1!!.split(" ")
        addressAdmin = address[0]
        addressLocality = address[1]
        addressThoroughfare = address[2]
        val pref = getSharedPreferences("pref", MODE_PRIVATE)
        val edit = pref.edit()
        edit.putString("addressAdmin", address[0])
        edit.putString("addressLocality", address[1])
        edit.putString("addressThoroughfare", address[2])
        edit.apply()
    }

    override fun onBackPressed() {
        goToMain()
    }

    fun goToMain() {
        isPause = true
        pauseTimer()
        pauseFab.visibility = View.GONE
        toiletFab.visibility = View.GONE
        playFab.visibility = View.VISIBLE
        resetFab.visibility = View.VISIBLE


        val dialog = Dialog(this)
        // 다이얼로그 테두리 둥글게 만들기
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dialog.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함
        dialog.setContentView(R.layout.walk_end_alert_dialog)

        val btnOk = dialog.findViewById<TextView>(R.id.yes_btn)
        val btnCancel = dialog.findViewById<TextView>(R.id.no_btn)


        btnOk.setOnClickListener {
            // 아래 removeAllViews() 안넣어주면 튕김
            kakaoMapView2.removeAllViews()
            val dialog2 = Dialog(this)
            // 다이얼로그 테두리 둥글게 만들기
            dialog2?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog2?.window?.requestFeature(Window.FEATURE_NO_TITLE)
            dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
            dialog2.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함
            dialog2.setContentView(R.layout.walk_end_dialog)

            // 산책 폴리라인 받아오기
            val dialogMapViewContainer = dialog2.findViewById<FrameLayout>(R.id.polylineView)
            val dialogMapView = MapView(this)
            dialogMapViewContainer.addView(dialogMapView)

            val dialogPolyline = MapPolyline()
            dialogPolyline.lineColor = Color.argb(255, 221, 135, 69)
            dialogPolyline.tag = 1000

            // 폴리라인 좌표 가져오기
            val polylinePoints = polyline?.mapPoints
            dialogPolyline.addPoints(polylinePoints)

            dialogMapView.addPolyline(dialogPolyline)

            // 다이얼로그 크기 조정
            val params: WindowManager.LayoutParams = dialog2.window!!.attributes
            params?.width = WindowManager.LayoutParams.MATCH_PARENT
            params?.y = 500
            dialog2.window?.attributes = params as WindowManager.LayoutParams


            val distanceView = dialog2.findViewById<TextView>(R.id.distance)
            val calorieView = dialog2.findViewById<TextView>(R.id.calorie)
            val minuteView = dialog2.findViewById<TextView>(R.id.minute)
            val secondView = dialog2.findViewById<TextView>(R.id.second)
            val millisecView = dialog2.findViewById<TextView>(R.id.millisec)

            val minute = (time / 6000) % 60 // 1분
            val second = (time / 100) % 60 // 1초
            val millisec = time % 100 // 0.01초

            minuteView.text = if (minute < 10) "0$minute" else "$minute"
            secondView.text = if (second < 10) "0$second" else "$second"
            millisecView.text = if (millisec < 10) "0$millisec" else "$millisec"


            distanceView.text = String.format("%.2f", walkingDistance)
            calorieView.text = String.format("%.2f", walkingCalorie)

            dialog2.show()

//                액티비티로 이동(첫화면)
            Handler(Looper.getMainLooper()).postDelayed({
                // 다이얼로그 카카오맵뷰 제거
                dialogMapViewContainer.removeAllViews()

                val intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
                (this as Activity).finish()
            }, 4000)

            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            startTimer()
            runningDog()
            pauseFab.visibility = View.VISIBLE
            toiletFab.visibility = View.VISIBLE
            playFab.visibility = View.GONE
            resetFab.visibility = View.GONE
            dialog.dismiss()
        }

        dialog.show()
    }

}