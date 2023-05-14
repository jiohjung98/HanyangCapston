package com.example.capston.DogRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capston.MainActivity
import com.example.capston.databinding.ActivityDogRegisterEndBinding

class DogRegisterEndActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityDogRegisterEndBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDogRegisterEndBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        viewBinding.nextBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}