package com.example.capston

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.capston.databinding.SplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.user.UserApiClient
import java.util.*


class SplashActivity : AppCompatActivity() {
    private lateinit var binding: SplashBinding

    private var auth: FirebaseAuth? = null

    lateinit var FadeIn : Animation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 유저 로그인 정보 확인
        auth = FirebaseAuth.getInstance()

        // 로그인 기록 있으면 자동 로그인 -> 메인액티비티로
        if(auth?.currentUser == null){
            Handler(Looper.getMainLooper()).postDelayed( {
                val intent: Intent = Intent(applicationContext, StartActivity::class.java)
                startActivity(intent)
                finish()
            }, 2000)
        }else{
            Handler(Looper.getMainLooper()).postDelayed( {
                val intent: Intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 2000)
        }

        FadeIn = AnimationUtils.loadAnimation(this,R.anim.fadein_splash)
        FadeIn.setAnimationListener(SlidingPageAnimationListener())
        binding.logo.startAnimation(FadeIn)
        // 일정 시간 지연 이후 실행하기 위한 코드
//        Handler(Looper.getMainLooper()).postDelayed({
//            // 일정 시간이 지나면 StartActivity로 이동
//            val intent= Intent( this,StartActivity::class.java)
//            startActivity(intent)
//            // 이전 키를 눌렀을 때 스플래스 스크린 화면으로 이동을 방지하기 위해
//            // 이동한 다음 사용안함으로 finish 처리
//            finish()
//
//        }, 2000) // 시간 2초 이후 실행

    }




    private inner class SlidingPageAnimationListener : Animation.AnimationListener {

        override fun onAnimationEnd(animation: Animation?) : Unit {
        }
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationRepeat(animation: Animation?) {}
    }
}