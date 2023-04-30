package com.example.capston

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.ActivityMissingAfterBinding
import kotlinx.android.synthetic.main.activity_dog_register.*

@Suppress("DEPRECATION")
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

        Handler().postDelayed(Runnable {
            val intent = Intent(this, TrackingActivity::class.java)
            startActivity(intent)
        }, 3000)
    }
}