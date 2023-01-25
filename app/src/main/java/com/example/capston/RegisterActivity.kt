package com.example.capston

import android.app.Activity
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
import com.example.capston.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityRegisterBinding
    var DB: DBHelper? = null

    var validName: Boolean = false
    var validId: Boolean= false
    var validPw: Boolean= false
    var validPw2: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        DB = DBHelper(this)


        viewBinding.registerBtn.setOnClickListener {
            val user = viewBinding.inputID.text.toString()
            val pass = viewBinding.inputPW.text.toString()
            val repass = viewBinding.reInputPW.text.toString()
            if (user == "" || pass == "" || repass == "") Toast.makeText(
                this@RegisterActivity,
                "회원정보를 전부 입력해주세요.",
                Toast.LENGTH_SHORT
            ).show() else {
                if (pass == repass) {
                    val checkUsername = DB!!.checkUsername(user)
                    if (!checkUsername) {
                        val insert = DB!!.insertData(user, pass)
                        if (insert) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "가입되었습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(applicationContext, LoginActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                "비밀번호가 일치하지 않습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "이미 가입된 회원입니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }   else {
                    viewBinding.registerBtn.isEnabled = false
                    Toast.makeText(
                        this@RegisterActivity,
                        "비밀번호가 일치하지 않습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val intent = Intent(this, RegisterCompleteActivity::class.java)
            intent.putExtra("username", viewBinding.inputName.text.toString())
            startActivity(intent)
        }

        viewBinding.inputName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                validName = editable.isNotEmpty()
                checkValid(validName, validId, validPw, validPw2)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                p0?.let { highlightText(it as Editable) }
            }
        } )

        viewBinding.inputID.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                validId = editable.isNotEmpty()
                checkValid(validName, validId, validPw, validPw2)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                p0?.let { highlightText(it as Editable) }
            }
        } )

        viewBinding.inputPW.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                validPw = editable.isNotEmpty()
                checkValid(validName, validId, validPw, validPw2)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                p0?.let { highlightText(it as Editable) }
            }
        } )

        viewBinding.reInputPW.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                validPw2 = editable.isNotEmpty()
                checkValid(validName, validId, validPw, validPw2)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                p0?.let { highlightText(it as Editable) }
            }
        } )
    }

    private fun checkValid(v1:Boolean, v2:Boolean, v3:Boolean, v4:Boolean){
        Log.d("Valid", (v1 && v2 && v3 && v4).toString())
        if(v1 && v2 && v3 && v4){
            viewBinding.registerBtn.isEnabled = true
            viewBinding.registerBtn.isClickable = true
            viewBinding.registerBtn.setBackgroundResource(R.drawable.start_button)
        } else {
            viewBinding.registerBtn.isEnabled = false
            viewBinding.registerBtn.isClickable = false
            viewBinding.registerBtn.setBackgroundResource(R.drawable.disabled_button)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }
}