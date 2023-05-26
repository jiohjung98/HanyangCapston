package com.example.capston

import MarkerAdapter
import OnItemClickListener
import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.data.UserPost
import com.example.capston.databinding.ActivityExpectingBinding
import com.example.capston.databinding.ActivityTrackingBinding
import com.example.capston.databinding.MarkerclickSpotdogBinding
import com.example.capston.databinding.SpotBalloonLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.activity_expecting.*
import kotlinx.android.synthetic.main.activity_tracking.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.daum.mf.map.api.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class ExpectingActivity : AppCompatActivity(), MapView.CurrentLocationEventListener,
    MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener{

//    var listen: MarkerEventListener? = null

    var mapView: MapView? = null
    val _mapView get() = mapView
    private var polyline: MapPolyline? = null
    var mapPoint: MapPoint? = null
    var currentLocation: MapPoint? = null

    // firebase
    private var auth : FirebaseAuth = FirebaseAuth.getInstance()
    private var database : DatabaseReference = FirebaseDatabase.getInstance().reference
    private var functions : FirebaseFunctions = FirebaseFunctions.getInstance()
    private lateinit var uid : String

    private var tapTimer: Timer? = null

//    private var getAddress: Boolean = false
//    private var addressAdmin: String = ""
//    private var addressLocality: String = ""
//    private var addressThoroughfare: String = ""

    private lateinit var binding: ActivityExpectingBinding

    // 이전 액티비티 전달값
    private var breed : String? = null
    private var imageUrl : String? = null
    private var address2 : String? = null

    // 로딩 다이얼로그
    var loading : LoadingDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpectingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.uid = auth.currentUser!!.uid

        breed = intent.getStringExtra("breed")!!.trim()
        imageUrl = intent.getStringExtra("imageUrl")!!.trim()
        address2 = intent.getStringExtra("address2")!!.trim()

        binding.locationBtn.setOnClickListener {
            mapView?.setMapCenterPoint(currentLocation,true)
        }

//        listen = MarkerEventListener(this)

        binding.backButton.setOnClickListener {
            goToMain()
        }

        mapView = MapView(this)
        val mapViewContainer = ExpectingMapView as ViewGroup
        mapViewContainer.addView(mapView)

        mapView!!.setMapViewEventListener(this)

//        mapView!!.setPOIItemEventListener(listen)

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

        loading = LoadingDialog(this@ExpectingActivity)
        loading!!.show()

        expectLocation()
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
//        if (!getAddress) {
//            findAddress(p1!!)
//        }
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
//        getAddress = true
//        val address = p1!!.split(" ")
//        addressAdmin = address[0]
//        addressLocality = address[1]
//        addressThoroughfare = address[2]
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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    private fun expectLocation(){
        database.child("route").child(this.uid).get().addOnSuccessListener{ task ->
            val childCount = task.childrenCount
            val data = hashMapOf(
                "uid" to this.uid,
                "lastIndex" to childCount.minus(1),
            )
            functions.getHttpsCallable("expectedLocation")
                .call(data)
                .addOnSuccessListener { task->
                    Log.d("expectLocation",task.data.toString()) // {similarArray=[1, 0]}
                    loading!!.dismiss()
                    val result = task.data as Map<*,*>
                    val top5 = result["top5Pairs"]
                    Log.d("top5",top5.toString())
                }
                .addOnFailureListener {
                    Log.d("expectLocation","FAIL")
                }
        }
    }
}