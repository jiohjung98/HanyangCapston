package com.example.capston


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.capston.databinding.ActivityDogRegisterEndBinding

class DogRegisterEndActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityDogRegisterEndBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDogRegisterEndBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.backButton.setOnClickListener {
            val intent = Intent(this, DogRegisterActivity::class.java)
            startActivity(intent)
        }

        viewBinding.goHomepageBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


//    viewBinding.nextPageBtn.setOnClickListener {
//        val intent = Intent(this, DogRegisterEndActivity::class.java)
//        startActivity(intent)
//        Toast.makeText(this@DogRegisterActivity, "버튼 클릭 가능", Toast.LENGTH_LONG).show()
//    }
}