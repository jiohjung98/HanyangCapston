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
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.capston.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityRegisterBinding
    private val binding get() = viewBinding!!
    var DB: DBHelper? = null

    private lateinit var auth: FirebaseAuth

    var validName: Boolean = false
    var validId: Boolean= false
    var validPw: Boolean= false
    var validPw2: Boolean = false
    var emailOk: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        DB = DBHelper(this)

        auth = FirebaseAuth.getInstance()

        viewBinding.inputName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(viewBinding.inputName.length() > 0){
                    validName = true
//                    checkValid(validName, validId, validPw, validPw2)
                    if (validName && validId && validPw && validPw2){
                        viewBinding.registerBtn.isClickable = true
                        viewBinding.registerBtn.isEnabled = true
                    }
                } else{
                    validName = false
                    viewBinding.registerBtn.isClickable = false
                    viewBinding.registerBtn.isEnabled = false
                }
            }
        })

        viewBinding.inputID.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(viewBinding.inputID.length() > 0){
                    validId = true
//                    checkValid(validName, validId, validPw, validPw2)
                    if (validName && validId && validPw && validPw2){
                        viewBinding.registerBtn.isClickable = true
                        viewBinding.registerBtn.isEnabled = true
                    }
                } else{
                    validId = false
                    viewBinding.registerBtn.isClickable = false
                    viewBinding.registerBtn.isEnabled = false
                }
            }
        })

        viewBinding.inputPW.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(viewBinding.inputPW.length() > 0){
                    validPw = true
//                    checkValid(validName, validId, validPw, validPw2)
                    if (validName && validId && validPw && validPw2){
                        viewBinding.registerBtn.isClickable = true
                        viewBinding.registerBtn.isEnabled = true
                    }
                } else{
                    validPw = false
                    viewBinding.registerBtn.isClickable = false
                    viewBinding.registerBtn.isEnabled = false
                }
            }
        })

        viewBinding.reInputPW.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(viewBinding.reInputPW.length() > 0){
                    validPw2 = true
//                    checkValid(validName, validId, validPw, validPw2)
                    if (validName && validId && validPw && validPw2){
                        viewBinding.registerBtn.isClickable = true
                        viewBinding.registerBtn.isEnabled = true
                    }
                } else{
                    validPw2 = false
                    viewBinding.registerBtn.isClickable = false
                    viewBinding.registerBtn.isEnabled = false
                }
            }
        })

        viewBinding.registerBtn!!.setOnClickListener {
            var name = viewBinding.inputName.text.toString() // 이름
            var email = binding.inputID.text.toString().trim() // 이메일
            var pw1 = binding.inputPW.text.toString().trim() // 비밀번호
            var pw2 = viewBinding.reInputPW.text.toString() // 비밀번호 확인



            if(email.isEmpty()){
                viewBinding.emailLayout.visibility = View.VISIBLE
                viewBinding.registerBtn.isEnabled = false
            } else if (!email.contains("@")){
                viewBinding.emailLayout.visibility = View.VISIBLE
            }

            if(pw1.isEmpty()){
                viewBinding.pw1Layout.visibility = View.VISIBLE
                viewBinding.registerBtn.isEnabled = false
            } else if (!Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$", pw1)) {
                viewBinding.pw1Layout.visibility = View.VISIBLE
            }

            if(pw2.isEmpty()){
                viewBinding.pw2Layout.visibility = View.VISIBLE
                viewBinding.registerBtn.isEnabled = false
            } else if (pw1 != pw2){
                viewBinding.pw2Layout.visibility = View.VISIBLE
            }

            if(Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$", pw1) && (pw1 == pw2) && (email.contains("@")) && (name.isNotEmpty()) && emailOk) {
                val intent = Intent(this, RegisterCompleteActivity::class.java)
                intent.putExtra("username", viewBinding.inputName.text.toString())
                startActivity(intent)
            }
//           else if (Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$", pw1) && (pw1 == pw2) && (email.contains("@")) && (name.isNotEmpty()) && !emailOk) {
//                Toast.makeText(this, "이미 가입된 이메일입니다.", Toast.LENGTH_SHORT).show()
//                }

            createUser(email, pw1)
        }

    }

    private fun createUser(email: String, pw1: String) {
        auth.createUserWithEmailAndPassword(email, pw1)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    emailOk = true
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    emailOk = false
                    updateUI(null)
                }
            }
            .addOnFailureListener {
//                Toast.makeText(this, "회원가입 실패ss", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
//            binding.txtResult.text = "Email: ${user.email}\nUid: ${user.uid}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        viewBinding.emailLayout.visibility = View.INVISIBLE
        viewBinding.pw1Layout.visibility = View.INVISIBLE
        viewBinding.pw2Layout.visibility = View.INVISIBLE
        viewBinding.inputName.clearFocus()
        viewBinding.inputID.clearFocus()
        viewBinding.inputPW.clearFocus()
        viewBinding.reInputPW.clearFocus()
        return true
    }

//    private fun checkValid(v1:Boolean, v2:Boolean, v3:Boolean, v4:Boolean){
//        Log.d("Valid", (v1 && v2 && v3 && v4).toString())
//        if(v1 && v2 && v3 && v4){
////            viewBinding.registerBtn.isEnabled = true
//            viewBinding.registerBtn.isClickable = true
//            viewBinding.registerBtn.setBackgroundResource(R.drawable.start_button)
//        } else {
//            viewBinding.registerBtn.isEnabled = false
//            viewBinding.registerBtn.isClickable = false
//            viewBinding.registerBtn.setBackgroundResource(R.drawable.disabled_button)
//        }
//    }
}

//
//        viewBinding.registerBtn.setOnClickListener {
//            val user = viewBinding.inputID.text.toString()
//            val pass = viewBinding.inputPW.text.toString()
//            val repass = viewBinding.reInputPW.text.toString()
//            if (user == "" || pass == "" || repass == "") Toast.makeText(
//                this@RegisterActivity,
//                "회원정보를 전부 입력해주세요.",
//                Toast.LENGTH_SHORT
//            ).show() else {
//                if (pass == repass) {
//                    val checkUsername = DB!!.checkUsername(user)
//                    if (!checkUsername) {
//                        val insert = DB!!.insertData(user, pass)
//                        if (insert) {
//                            Toast.makeText(
//                                this@RegisterActivity,
//                                "가입되었습니다.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            val intent = Intent(applicationContext, LoginActivity::class.java)
//                            startActivity(intent)
//                        } else {
//                            Toast.makeText(
//                                this@RegisterActivity,
//                                "비밀번호가 일치하지 않습니다.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    } else {
//                        Toast.makeText(
//                            this@RegisterActivity,
//                            "이미 가입된 회원입니다.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }   else {
//                    viewBinding.registerBtn.isEnabled = false
//                    Toast.makeText(
//                        this@RegisterActivity,
//                        "비밀번호가 일치하지 않습니다.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//
//            val intent = Intent(this, RegisterCompleteActivity::class.java)
//            intent.putExtra("username", viewBinding.inputName.text.toString())
//            startActivity(intent)
//        }
//    }
//
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
//        return true
//    }
//}