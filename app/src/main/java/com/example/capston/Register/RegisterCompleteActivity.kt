package com.example.capston.Register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capston.DogRegister.DogRegister1Activity
import com.example.capston.SkipDialog
import com.example.capston.databinding.ActivityRegisterCompleteBinding

class RegisterCompleteActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityRegisterCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegisterCompleteBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
//
        viewBinding.receiveNameTxt.text = intent.getStringExtra("UserName").toString()

        viewBinding.agreeBtn.isEnabled = true
        viewBinding.agreeBtn.setOnClickListener {
            val intent = Intent(this, DogRegister1Activity::class.java)
            startActivity(intent)
//            overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit)
        }

        viewBinding.skipBtn.setOnClickListener {
            onBackPressed()
        }
    }

    // 뒤로가기 -> 건너뛰기
    override fun onBackPressed() {
        val skipDialog = SkipDialog(this)
        skipDialog.setOnOKClickedListener { content ->
        }
        skipDialog.show("초기화면")
    }
}