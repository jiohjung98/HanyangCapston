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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.capston.databinding.ActivityPermissionBinding

class PermissionActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPermissionBinding

    private val LOCATION_REQUEST = 1
    private final val FILE_REQUEST = 1010

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityPermissionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.permissionAgreeBtn.setOnClickListener {
            requestPermission()
        }
    }


    // 위치 권한 설정 확인 함수
    private fun isSetPermission() : Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    // 위치 권한 설정
    private fun requestPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), FILE_REQUEST)
        }


        if(isSetPermission()) {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }
        else{
            Toast.makeText(this,"권한설정에 동의해주세요", Toast.LENGTH_SHORT)
            requestPermission()
        }
    }
}