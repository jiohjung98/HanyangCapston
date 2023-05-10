package com.example.capston
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.capston.databinding.ActivityMainBinding
import com.example.capston.homepackage.CustomDialog
import com.example.capston.homepackage.NaviHomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.common.util.Utility
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_navi_home.*
import kotlinx.android.synthetic.main.fragment_navi_walk.*


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
//    private var oneFragment: NaviHomeFragment? = null
//    private var twoFragment: NaviWalkFragment? = null
//    private var threeFragment: Calendar_fragment? = null
//    private var fourFragment: NaviMypageFragment? = null

    internal var launcher: ActivityResultLauncher<Intent>? = null
    private var uri : Uri? = null

    private var lastSelectedItemId = 0

    // MainActivity 하위 여러 프래그먼트에서 여러번 사용한다면 여기다 선언하는게 좋을것같음
    // 현재 사용 : 마이페이지 유저정보, DogInfoEnterDialog
    internal val database: DatabaseReference = Firebase.database.reference
    internal val auth = FirebaseAuth.getInstance()
    
    // 로컬 저장값
    internal var sharedPreferences: SharedPreferences? = null

    internal val uid = auth.currentUser!!.uid

    @SuppressLint("MissingInflatedId")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로컬에 저장된 현재 반려견 인덱스
        sharedPreferences = getSharedPreferences("CUR_PET", MODE_PRIVATE);


        val keyHash = Utility.getKeyHash(this)
        Log.d("Hash", keyHash)

        replaceFragment(NaviHomeFragment())

        deleteToolbar(NaviMypageFragment())

        val main_bnv = findViewById<BottomNavigationView>(R.id.main_bnv)
        setSupportActionBar(toolbar) // 커스텀한 toolbar를 액션바로 사용
        supportActionBar?.setDisplayShowTitleEnabled(false) // 액션바에 표시되는 제목의 표시유무를 설정합니다. false로 해야 custom한 툴바의 이름이 화면에 보이게 됩니다.

//        var noticeitem = findViewById<ImageView>(R.id.noticeitem)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        var toolbar2 = findViewById<Toolbar>(R.id.toolbar2)
        var toolbar3 = findViewById<Toolbar>(R.id.toolbar3)

        close_notice.setOnClickListener {
            val transaction = supportFragmentManager.popBackStack()
            toolbar.visibility = View.VISIBLE
            toolbar2.visibility = View.INVISIBLE
            toolbar3.visibility = View.INVISIBLE
        }


        btn_back.setOnClickListener {
            val transaction = supportFragmentManager.popBackStack()
            toolbar.visibility = View.VISIBLE
            toolbar2.visibility = View.INVISIBLE
            toolbar3.visibility = View.INVISIBLE
        }


        binding.mainBnv.setOnItemSelectedListener { item ->
            if (item.itemId == lastSelectedItemId) {
                // 이미 선택된 아이템을 다시 선택했을 때 아무 동작도 하지 않음
                return@setOnItemSelectedListener false
            }

            lastSelectedItemId = item.itemId
            main_bnv.itemIconTintList = null

            changeFragment(
                when (item.itemId) {
                    R.id.navigation_home -> {
                        if (kakaoMapView3 != null) {
                            kakaoMapView3.removeAllViews()
                        }
                        item.setIcon(R.drawable.lost_color)
                        binding.mainBnv.menu.findItem(R.id.navigation_community)?.setIcon(R.drawable.paw1)
                        binding.mainBnv.menu.findItem(R.id.navigation_mypage)?.setIcon(R.drawable.set)
                        NaviHomeFragment()
                    }
                    R.id.navigation_community -> {
                        if (kakaoMapView != null) {
                        kakaoMapView!!.removeAllViews()
                        }
                        item.setIcon(R.drawable.paw1_color)
                        binding.mainBnv.menu.findItem(R.id.navigation_home)?.setIcon(R.drawable.lost)
                        binding.mainBnv.menu.findItem(R.id.navigation_mypage)?.setIcon(R.drawable.set)
                        NaviWalkFragment()
                    }
                    else -> {
                        if (kakaoMapView != null) {
                            kakaoMapView!!.removeAllViews()
                        }
                        if (kakaoMapView3 != null) {
                            kakaoMapView3.removeAllViews()
                        }
                        item.setIcon(R.drawable.set_color)
                        binding.mainBnv.menu.findItem(R.id.navigation_home)?.setIcon(R.drawable.lost)
                        binding.mainBnv.menu.findItem(R.id.navigation_community)?.setIcon(R.drawable.paw1)
                        NaviMypageFragment()
                    }
                }
            )
            true
        }

        binding.mainBnv.selectedItemId = R.id.navigation_home
    }

    private fun changeFragment(fragment: Fragment?) {
        supportFragmentManager.popBackStackImmediate()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frm, fragment!!)
//                .addToBackStack(null)
                .commit()
    }


    private fun replaceFragment(naviCommunityFragment: Fragment) {
        if (kakaoMapView != null) {
            kakaoMapView!!.removeAllViews()
        }
        if (kakaoMapView3 != null) {
            kakaoMapView3.removeAllViews()
        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_frm, naviCommunityFragment)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
//        mapView?.removeAllPOIItems()
//        mapView = null
    }

    private fun deleteToolbar(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_frm, fragment!!)
        fragmentTransaction.commit()
    }
}