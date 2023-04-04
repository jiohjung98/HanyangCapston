package com.example.capston

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.SplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*


class SplashActivity : AppCompatActivity() {
    private lateinit var binding: SplashBinding

    private lateinit var auth: FirebaseAuth
    private val database: DatabaseReference = Firebase.database.reference

    lateinit var FadeIn : Animation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 유저 로그인 정보 확인
        auth = FirebaseAuth.getInstance()

        // 유저정보 리로드 - 서버에서 다시 받아옴 - 왜 안돼

        val user = auth.currentUser

        // 로그인 기록 없으면 로그인화면
        if(user == null){
            Handler(Looper.getMainLooper()).postDelayed( {
                val intent: Intent = Intent(applicationContext, StartActivity::class.java)
                startActivity(intent)
                finish()
            }, 2000)
        // 로그인 기록 있으면
        }else{
            Log.d("USER", "${user.email}")
            Handler(Looper.getMainLooper()).postDelayed(
                {
                isInDB(this,user)
                // 이메일 인증 안된 유저면 -> 이메일 인증 화면으로
//                if(!(auth?.currentUser!!.isEmailVerified)) {
//                    Toast.makeText(this, "이메일 인증 후 사용바랍니다.", Toast.LENGTH_LONG).show()
//                    val intent = Intent(this, EmailVerifyActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                // 이메일 인증 되어있다면 -> 메인으로
//                }else {
//                    val intent: Intent = Intent(applicationContext, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                }
            }, 2000)
        }

        FadeIn = AnimationUtils.loadAnimation(this,R.anim.fadein_splash)
        FadeIn.setAnimationListener(SlidingPageAnimationListener())
        binding.logo.startAnimation(FadeIn)

    }


    private fun isInDB(context: Context, user: FirebaseUser) {
        user.reload()
        val uid = database.child("users").child(user.uid)
        uid.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터가 변경되면 리스너가 감지함
                // 최초(아무값도 없을때)로 실행 됐을때도 감지됨

                // 회원가입 중 이메일인증 하지 않고 어플 종료 후 재시작했을때 실행되는 분기
                // auth에는 있으나 db에는 없는 경우
                if(snapshot.value == null){
                    user.delete()
                    val intent = Intent(context, StartActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                // 정상 분기
                else{
                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("DATABASE LOAD ERROR","정보 불러오기 실패")
            }
        })
    }


    private inner class SlidingPageAnimationListener : Animation.AnimationListener {

        override fun onAnimationEnd(animation: Animation?) : Unit {
        }
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationRepeat(animation: Animation?) {}
    }
}