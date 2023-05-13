package com.example.capston

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.capston.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityStartBinding

    private val LOCATION_REQUEST = 1
    private final val FILE_REQUEST = 1010

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityStartBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.startBtn.setOnClickListener {
            val intent = Intent(this, UserAgreeActivity::class.java)
            startActivity(intent)
        }

        viewBinding.loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

}