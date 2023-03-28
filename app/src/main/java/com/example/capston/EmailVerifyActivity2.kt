package com.example.capston

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.ActivityEmailverificationBinding
import com.google.firebase.auth.FirebaseAuth

class EmailVerifyActivity : AppCompatActivity() {

    // 1. Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    private lateinit var auth : FirebaseAuth

    private lateinit var viewBinding: ActivityEmailverificationBinding

    private var name : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityEmailverificationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        auth = FirebaseAuth.getInstance()

        name = intent.getStringExtra("name")

        viewBinding.nextBtn.isEnabled = false

        viewBinding.emailBtn.setOnClickListener {
            sendEmailVerification()
            viewBinding.emailError.visibility = View.VISIBLE
            viewBinding.emailResend.visibility = View.VISIBLE
            viewBinding.emailResend.isEnabled = true
        }

        viewBinding.emailResend.setOnClickListener {
            sendEmailVerification()
            viewBinding.emailResend.isEnabled = false
        }

        viewBinding.nextBtn.setOnClickListener {
            val intent = Intent(this, UserAgreeActivity::class.java)
            intent.putExtra("name",name)
            startActivity(intent)
        }
    }


    private fun sendEmailVerification(){
        FirebaseAuth.getInstance().currentUser
            ?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "메일을 보냈어요!", Toast.LENGTH_LONG).show()
                    viewBinding.emailBtn.isEnabled = false
                    viewBinding.nextBtn.isEnabled = true
                } else {
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

}