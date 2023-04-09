package com.example.capston
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.capston.databinding.ActivityMainBinding
import com.example.capston.homepackage.CustomDialog
import com.example.capston.homepackage.NaviHomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.common.util.Utility
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_navi_home.*
import net.daum.mf.map.api.MapView


class MainActivity : AppCompatActivity(), CustomDialog {
    lateinit var binding: ActivityMainBinding

//    private var mapView: MapView? = null

    private var oneFragment: NaviHomeFragment? = null
    private var twoFragment: NaviWalkFragment? = null
    private var threeFragment: Calendar_fragment? = null
    private var fourFragment: NaviMypageFragment? = null

    public var toolbar3_menu: Menu? = null

    // MainActivity 하위 여러 프래그먼트에서 여러번 사용한다면 여기다 선언하는게 좋을것같음
    // 현재 사용 : 마이페이지 유저정보
    internal val database: DatabaseReference = Firebase.database.reference
    internal val auth = FirebaseAuth.getInstance()

    private val fl: FrameLayout by lazy {
        findViewById(R.id.main_frm)
    }

    private var backPressedTime: Long = 0
    // 액션버튼 메뉴 액션바에 집어넣기

    // 이 부분은 뒤로가기 이벤트 처리용 코드. 후에 사용 할듯
//    interface onBackPressedListener {
//        fun onBackPressed()
//    }
//    override fun onBackPressed(){
//        Log.d("sad","dsad")
//        // 해당 엑티비티에서 띄운 프래그먼트에서 뒤로가기를 누르게 되면 프래그먼트에서 구현한 onBackPressed 함수가 실행되게 된다.
//        val fragmentList = supportFragmentManager.fragments
//        // 플래그먼트에서 뒤로가기 구현
//        for (fragment in fragmentList) {
//            if (fragment is onBackPressedListener) {
//                (fragment as onBackPressedListener).onBackPressed()
//                return
//            }else{
//                // 네비게이션에 있는 메이들로 가면 꺼짐
//                finish()
//            }
//        }
//    }

    // 액션버튼 클릭 했을 때
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item?.itemId){
//            R.id.action_notice -> {
//                // 알림 버튼 눌렀을 때
//                Toast.makeText(applicationContext, "알림 이벤트 실행", Toast.LENGTH_LONG).show()
//                supportFragmentManager.beginTransaction().replace(R.id.main_frm, NoticeFragment()).commit()
//                var cv = findViewById<Toolbar>(R.id.toolbar2) = View.VISIBLE
//
//                return super.onOptionsItemSelected(item)
//            }
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }

    @SuppressLint("MissingInflatedId")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        mapView = getMapView()
//        val mapViewContainer = findViewById<ViewGroup>(R.id.main_frm)
//        mapViewContainer.addView(mapView)

//        mapView = MapView(this)
//        val mapViewContainer = findViewById<ViewGroup>(R.id.main_frm)
//        mapViewContainer.addView(mapView)

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

//        noticeitem.setOnClickListener {
//            val transaction = supportFragmentManager.beginTransaction()
//                .replace(R.id.main_frm, NoticeFragment())
//                .addToBackStack(null)
//            transaction.commit()
//            toolbar.visibility = View.INVISIBLE
//            toolbar2.visibility = View.VISIBLE
//            toolbar3.visibility = View.INVISIBLE
//
//        }

        btn_back.setOnClickListener {
            val transaction = supportFragmentManager.popBackStack()
            toolbar.visibility = View.VISIBLE
            toolbar2.visibility = View.INVISIBLE
            toolbar3.visibility = View.INVISIBLE
        }

        binding.mainBnv.setOnItemSelectedListener { item ->
            changeFragment(
                when (item.itemId) {
                    R.id.navigation_home -> {
                        main_bnv.itemIconTintList = null
                        NaviHomeFragment()
                        // Respond to navigation item 1 click
                    }
                    R.id.navigation_community -> {
                        main_bnv.itemIconTintList = null
                        NaviWalkFragment()
                        // Respond to navigation item 2 click
                    }
                    R.id.navigation_calendar -> {
                        main_bnv.itemIconTintList = null
                        Calendar_fragment()
                    }
                    else -> {
                        main_bnv.itemIconTintList = null
                        NaviMypageFragment()
                    }
                }
            )
            true
        }
        binding.mainBnv.selectedItemId = R.id.navigation_home
//        initNavigation()
    }

//    binding.mainBnv.setOnNavigationItemSelectedListener { item ->
//        if (item.itemId == binding.mainBnv.selectedItemId) {
//            // 아이콘을 누른 프래그먼트와 이미 선택된 프래그먼트가 같을 경우
//            // 아무런 동작도 하지 않음
//            return@setOnNavigationItemSelectedListener false
//        }
//        changeFragment(
//            when (item.itemId) {
//                R.id.navigation_home -> {
//                    main_bnv.itemIconTintList = null
//                    NaviHomeFragment()
//                    // Respond to navigation item 1 click
//                }
//                R.id.navigation_community -> {
//                    main_bnv.itemIconTintList = null
//                    NaviWalkFragment()
//                    // Respond to navigation item 2 click
//                }
//                R.id.navigation_calendar -> {
//                    main_bnv.itemIconTintList = null
//                    Calendar_fragment()
//                }
//                else -> {
//                    main_bnv.itemIconTintList = null
//                    NaviMypageFragment()
//                }
//            }
//        )
//        true
//    }

//    fun getMapView(): MapView {
//        val mapView = MapView(this)
//        return mapView
//    }

//        binding.mainBnv.setOnItemSelectedListener { item ->
//            val fragment: Fragment = when (item.itemId) {
//                R.id.navigation_home -> NaviHomeFragment()
//                R.id.navigation_community -> NaviWalkFragment()
//                R.id.navigation_calendar -> Calendar_fragment()
//                R.id.navigation_mypage -> NaviMypageFragment()
//                else -> throw IllegalArgumentException("Invalid navigation item ID")
//            }
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.main_frm, fragment, fragment.javaClass.simpleName)
//                .addToBackStack(fragment.javaClass.simpleName)
//                .commit()
//            true
//        }
//    }


    private fun changeFragment(fragment: Fragment?) {
        supportFragmentManager.popBackStackImmediate()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frm, fragment!!)
//                .addToBackStack(null)
                .commit()
    }

//    private fun changeFragment(fragment: Fragment) {
//        val tag = fragment.javaClass.simpleName
//        val fragmentPopped = supportFragmentManager.popBackStackImmediate(tag, 0)
//        val fragmentTransaction = supportFragmentManager.beginTransaction()
//        if (!fragmentPopped && supportFragmentManager.findFragmentByTag(tag) == null) {
//            fragmentTransaction
//                .replace(R.id.main_frm, fragment, tag)
//                .addToBackStack(tag) // back stack에 fragment를 추가합니다.
//        } else {
//            fragmentTransaction
//                .replace(R.id.main_frm, fragment, tag)
//        }
//        fragmentTransaction.commit()
//    }

//    private fun initNavigation() {
//        binding.mainBnv.itemIconTintList = null
////        binding.mainBottomNavigation.setupWithNavController(navController)
////        binding.mainBottomNavigation.itemIconTintList = null
//    }


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

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.toolbar3_menu, menu)
//        toolbar3_menu = menu
//
//        if (toolbar3.visibility == View.VISIBLE) {
//            toolbar3_menu!!.findItem(R.id.item1).setVisible(true)
//            toolbar3_menu!!.findItem(R.id.item2).setVisible(true)
//        }
//
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle item selection
//        return when (item.itemId) {
//            R.id.item1 -> {
//                Toast.makeText(this@MainActivity, "수정 클릭", Toast.LENGTH_SHORT).show()
//                true
//            }
//            R.id.item2 -> {
//                Toast.makeText(this@MainActivity, "삭제 클릭", Toast.LENGTH_SHORT).show()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

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