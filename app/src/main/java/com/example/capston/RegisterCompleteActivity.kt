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
        viewBinding.receiveNameTxt.text = intent.getStringExtra("UserName").toString()

        viewBinding.agreeBtn.isEnabled = true
        viewBinding.agreeBtn.setOnClickListener {
            val intent = Intent(this, DogRegisterActivity::class.java)
            startActivity(intent)
//            overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit)
        }
    }
}