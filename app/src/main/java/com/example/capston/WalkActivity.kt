package com.example.capston

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.example.capston.databinding.ActivityRegisterBinding
import com.example.capston.databinding.ActivityWalkBinding
import kotlinx.android.synthetic.main.activity_walk.*
import kotlinx.android.synthetic.main.fragment_walk.*
import net.daum.mf.map.api.MapView

class WalkActivity : FragmentActivity() {

    lateinit var viewBinding: ActivityWalkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityWalkBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val fragmentWalk = NaviWalkFragment()


        val mapView = MapView(this)
        val mapViewContainer = kakaoMapView2 as ViewGroup
        mapViewContainer.addView(mapView)

        viewBinding.cameraBtn.setOnClickListener{
//            supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.main_frm, fragmentWalk)
//                .commit()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

//        val mapView = MapView(this)
//        val mapViewContainer: ViewGroup = mapView.findViewById(R.id.kakaoMapView)
//        mapViewContainer.addView(mapView)
    }
}