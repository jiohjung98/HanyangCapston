package com.example.capston

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.capston.databinding.FragmentNaviHomeBinding
import com.example.capston.databinding.FragmentWalkBinding
import com.example.capston.homepackage.walkFragment
import kotlinx.android.synthetic.main.fragment_walk.*
import net.daum.mf.map.api.MapView
import net.daum.mf.map.api.MapPoint

class WalkFragment : Fragment() {

    val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    private val ACCESS_FINE_LOCATION = 1000

    private var _binding: FragmentWalkBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWalkBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    // fragment 액션바 보여주기(선언안해주면 다른 프레그먼트에서 선언한 .hide() 때문인지 모든 프레그먼트에서 액션바 안보임
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

//        val mapView = MapView(activity)
//        val mapViewContainer: ViewGroup = view.findViewById(R.id.kakaoMapView)
//        mapViewContainer.addView(mapView)


        binding.startBtn.setOnClickListener {
            Log.d("text", "dd")
            if (checkLocationService()) {
                permissionCheck()
            }
        }
//        val mapView = MapView(context)
//        binding.kakaoMapView.addView

//        val mapView = MapView(requireActivity())
//        binding.kakaoMapView.(mapView)

    }

    lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }

    //메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WalkFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
    // 위치 권한 확인
    private fun permissionCheck() {
        val preference = this.requireActivity().getPreferences(MODE_PRIVATE)
        val isFirstCheck = preference.getBoolean("isFirstPermissionCheck", true)
        if (ContextCompat.checkSelfPermission(
                context as Activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없는 상태
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // 권한 거절 (다시 한 번 물어봄)
                val builder = AlertDialog.Builder(context as Activity)
                builder.setMessage("현재 위치를 확인하시려면 위치 권한을 허용해주세요.")
                builder.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                }
                builder.setNegativeButton("취소") { dialog, which ->

                }
                builder.show()
            } else {
                if (isFirstCheck) {
                    // 최초 권한 요청
                    preference.edit().putBoolean("isFirstPermissionCheck", false).apply()
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                } else {
                    // 다시 묻지 않음 클릭 (앱 정보 화면으로 이동)
                    val builder = AlertDialog.Builder(context as Activity)
                    builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { dialog, which ->
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("http://www.google.com")
                        )
                        startActivity(intent)
                    }
                    builder.setNegativeButton("취소") { dialog, which ->
                    }
                    builder.show()
                }
            }
        } else {
            // 권한이 있는 상태
            startTracking()
        }
    }
    // 권한 요청 후 행동
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 요청 후 승인됨 (추적 시작)
                Toast.makeText(context, "위치 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()
                startTracking()
            } else {
                // 권한 요청 후 거절됨 (다시 요청 or 토스트)
                Toast.makeText(context, "위치 권한이 거절되었습니다", Toast.LENGTH_SHORT).show()
                permissionCheck()
            }
        }
    }

    // GPS가 켜져있는지 확인
    private fun checkLocationService(): Boolean {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

//     위치추적 시작
    private fun startTracking() {
        binding.kakaoMapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
    }

//     위치추적 중지
    private fun stopTracking() {
        binding.kakaoMapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff

    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }
}
