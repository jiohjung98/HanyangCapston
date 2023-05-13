package com.example.capston

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.ActivityStartBinding
import com.example.capston.Register.Register1Activity

class StartActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityStartBinding

    private val LOCATION_REQUEST = 1
    private final val FILE_REQUEST = 1010

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityStartBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.startBtn.setOnClickListener {
            val intent = Intent(this, Register1Activity::class.java)
            startActivity(intent)
        }

        viewBinding.loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

}