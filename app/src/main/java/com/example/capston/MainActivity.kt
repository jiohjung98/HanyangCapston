package com.example.capston
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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


class MainActivity : AppCompatActivity(), CustomDialog {
    lateinit var binding: ActivityMainBinding


    private var oneFragment: NaviHomeFragment? = null
    private var twoFragment: NaviWalkFragment? = null
    private var threeFragment: Calendar_fragment? = null
    private var fourFragment: NaviMypageFragment? = null

    internal var launcher: ActivityResultLauncher<Intent>? = null
    private var uri : Uri? = null

    private var lastSelectedItemId = 0

    // MainActivity 하위 여러 프래그먼트에서 여러번 사용한다면 여기다 선언하는게 좋을것같음
    // 현재 사용 : 마이페이지 유저정보, DogInfoEnterDialog
    internal val database: DatabaseReference = Firebase.database.reference
    internal val auth = FirebaseAuth.getInstance()
    internal val storage = FirebaseStorage.getInstance()

    internal val uid = auth.currentUser!!.uid

    @SuppressLint("MissingInflatedId")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        //Doginfo다이얼로그에서 사용하기위함, registerForActivityResult 결과 후 실행할 행동 정의
//        launcher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                when (result.resultCode) {
//                    AppCompatActivity.RESULT_OK -> {
//                        // 전달 받은 이미지 uri를 넣어준다.
//                        this.uri = result.data?.data
//                        Log.d("uri변경직후",this.uri.toString())
////                        Image.setImageURI(this.uri)
//                    }
//                }
//            }

        val keyHash = Utility.getKeyHash(this)
        Log.d("Hash", keyHash)

        replaceFragment(NaviWalkFragment())

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
                        NaviHomeFragment()
                    }
                    R.id.navigation_community -> {
                        NaviWalkFragment()
                    }
                    R.id.navigation_calendar -> {
                        Calendar_fragment()
                    }
                    else -> {
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

    override fun onLikedBtnClicked() {
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
    }

    override fun onSubscribeBtnClicked() {
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
    }


}