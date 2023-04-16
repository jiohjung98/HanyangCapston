package com.example.capston

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capston.databinding.ActivityMissingAfterBinding
import kotlinx.android.synthetic.main.activity_dog_register.*

class MissingAfterActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMissingAfterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMissingAfterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        viewBinding.goNextBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}