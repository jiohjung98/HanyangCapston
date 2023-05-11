package com.example.capston

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.example.capston.databinding.ActivityRegister2Binding
import com.example.capston.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class Register2Activity : AppCompatActivity() {

    lateinit var viewBinding: ActivityRegister2Binding
    private val binding get() = viewBinding!!
    var DB: DBHelper? = null

    private lateinit var auth: FirebaseAuth

    var ValidEmail: Boolean= false
    var emailOk: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegister2Binding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        DB = DBHelper(this)

        auth = FirebaseAuth.getInstance()

        viewBinding.email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(viewBinding.email.length() > 0){
                    ValidEmail = true
                    if (ValidEmail)
                        viewBinding.nextBtn.isClickable = true
                        viewBinding.nextBtn.isEnabled = true
                    }
                else{
                ValidEmail = false
                viewBinding.nextBtn.isClickable = false
                viewBinding.nextBtn.isEnabled = false
            }
            }
        })

        viewBinding.nextBtn!!.setOnClickListener {
            var email = binding.email.text.toString().trim() // 이메일

            if(email.isEmpty()){
                viewBinding.emailLayout.visibility = View.VISIBLE
                viewBinding.nextBtn.isEnabled = false
            } else if (!email.contains("@")){
                viewBinding.emailLayout.visibility = View.VISIBLE
            }
            else {
                val intent = Intent(this, EmailVerifyActivity::class.java)
                startActivity(intent)
            }
        }
    }
}