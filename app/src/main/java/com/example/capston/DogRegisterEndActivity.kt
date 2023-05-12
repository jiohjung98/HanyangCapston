package com.example.capston

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.capston.DogRegisterActivity
import com.example.capston.databinding.ActivityDogRegisterEndBinding
import kotlinx.android.synthetic.main.activity_dog_register.*

class DogRegisterEndActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityDogRegisterEndBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDogRegisterEndBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.backButton.setOnClickListener {
            val intent = Intent(this, DogRegister3Activity::class.java)
            startActivity(intent)
        }

        if(intent.hasExtra("dogname")) {
            receive_name_txt.text = intent.getStringExtra("dogname")
        }

        viewBinding.goHomepageBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}