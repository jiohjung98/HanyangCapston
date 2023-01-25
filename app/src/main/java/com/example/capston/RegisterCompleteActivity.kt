package com.example.capston

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capston.databinding.ActivityLoginBinding
import com.example.capston.databinding.ActivityRegisterCompleteBinding

class RegisterCompleteActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityRegisterCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegisterCompleteBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
//
        viewBinding.receiveNameTxt.text = intent.getStringExtra("username")
        val message = intent.getStringExtra("username")
        intent.getStringExtra("전달했던 데이터의 이름표")

        viewBinding.receiveNameTxt.text = message // TextView에 message 표현


        viewBinding.agreeBtn.isEnabled = true
        viewBinding.agreeBtn.setOnClickListener {
            val intent = Intent(this, DogRegisterActivity::class.java)
            startActivity(intent)
//            overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit)
        }
    }
}