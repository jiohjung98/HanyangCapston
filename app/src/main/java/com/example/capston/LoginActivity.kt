package com.example.capston

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.capston.databinding.ActivityLoginBinding
import kotlin.math.log2

class LoginActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityLoginBinding

    var DB: DBHelper? = null

    var validId: Boolean= false
    var validPw: Boolean= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        DB = DBHelper(this)

        viewBinding.loginBtn!!.setOnClickListener {
            val user = viewBinding.etId!!.text.toString()
            val pass = viewBinding.etPw!!.text.toString()
            if (user == "" || pass == "") Toast.makeText(
                this@LoginActivity,
                "회원정보를 전부 입력해주세요",
                Toast.LENGTH_SHORT
            ).show() else {
                val checkUserpass = DB!!.checkUserPass(user, pass)
                if (checkUserpass) {
                    Toast.makeText(this@LoginActivity, "로그인 되었습니다", Toast.LENGTH_SHORT).show()
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                }
                else {
                    Toast.makeText(this@LoginActivity, "회원정보가 존재하지 않습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewBinding.registerBtn.setOnClickListener {
            val loginIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(loginIntent)
        }

        viewBinding.etId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                validId = editable.isNotEmpty()
                checkValid(validId, validPw)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                p0?.let { highlightText(it as Editable) }
            }
        } )

        viewBinding.etPw.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                validPw = editable.isNotEmpty()
                checkValid(validId, validPw)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                p0?.let { highlightText(it as Editable) }
            }
        })
    }

    private fun checkValid(v1:Boolean, v2:Boolean){
        Log.d("Valid", (v1 && v2).toString())
        if(v1 && v2){
            viewBinding.loginBtn.isClickable = true
            viewBinding.loginBtn.isEnabled = true
            viewBinding.loginBtn.setBackgroundResource(R.drawable.start_button)
        }
        else {
            viewBinding.loginBtn.isEnabled = false
            viewBinding.loginBtn.isClickable = false
            viewBinding.loginBtn.setBackgroundResource(R.drawable.disabled_button)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }
}