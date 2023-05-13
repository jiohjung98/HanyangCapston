package com.example.capston

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.ActivityLoginBinding
import com.example.capston.register.Register1Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var auth: FirebaseAuth? = null
    private val database: DatabaseReference = Firebase.database.reference

    var validId: Boolean= false
    var validPw: Boolean= false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()


        binding.loginBtn.setOnClickListener {

            var id = binding.etId.text.toString() // 아이디
            var pw = binding.etPw.text.toString() // 비번
//            if(id.isNotEmpty() && id.isNotBlank() && pw.isNotEmpty() && pw.isNotBlank()){
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//            }else{
//                binding.warningText.visibility = View.VISIBLE
//            }
            signIn(id, pw)
        }

        binding.infoBtn.setOnClickListener {
            val intent = Intent(this, Register1Activity::class.java)
            startActivity(intent)
        }

        binding.findBtn.setOnClickListener {
            val intent = Intent(this, FindPwActivity::class.java)
            startActivity(intent)
        }

        binding.etId.addTextChangedListener(object : TextWatcher {
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

        binding.etPw.addTextChangedListener(object : TextWatcher {
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

    private fun signIn(id: String, pw: String) {
        if ((id.isNotEmpty() && id.isNotBlank() && pw.isNotEmpty() && pw.isNotBlank())) {
            auth?.signInWithEmailAndPassword(id, pw)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        auth?.currentUser!!.reload()
                        isInDB(this, auth?.currentUser!!)
                    } else {
                        binding.warningText.visibility = View.VISIBLE
                    }
                }
        }
    }

    private fun movePage() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }

    private fun isInDB(context: Context, user: FirebaseUser) {
        user.reload()
        val uid = database.child("users").child(user.uid)
        uid.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터가 변경되면 리스너가 감지함
                // 최초(아무값도 없을때)로 실행 됐을때도 감지됨

                // 회원가입-이메일인증화면에서 그냥 종료후 앱데이터 삭제, 이후 로그인하면 실행되는 분기
                if(snapshot.value == null){
                    user.delete()
                    binding.warningText.visibility = View.VISIBLE
                }
                else{
                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("DATABASE LOAD ERROR","정보 불러오기 실패")
            }
        })
    }


    private fun checkValid(v1:Boolean, v2:Boolean){
        Log.d("Valid", (v1 && v2).toString())
        if(v1 && v2){
            binding.loginBtn.isClickable = true
            binding.loginBtn.isEnabled = true
            binding.loginBtn.setBackgroundResource(R.drawable.start_button)
        }
        else {
            binding.loginBtn.isEnabled = false
            binding.loginBtn.isClickable = false
            binding.loginBtn.setBackgroundResource(R.drawable.disabled_button)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        et_id.clearFocus()
        et_pw.clearFocus()
        binding.warningText.visibility = View.INVISIBLE
        return true
    }


}