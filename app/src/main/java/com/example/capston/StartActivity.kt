package com.example.capston

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityStartBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityStartBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.startBtn.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        viewBinding.loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

}