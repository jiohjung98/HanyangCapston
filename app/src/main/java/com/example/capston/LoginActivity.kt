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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var auth: FirebaseAuth? = null
    var DB: DBHelper? = null

    var validId: Boolean= false
    var validPw: Boolean= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        DB = DBHelper(this)
        auth = Firebase.auth

//        // 카카오 로그인 정보 확인
//        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
//            if (error != null) {
//                Toast.makeText(this, "토큰 정보 보기 실패", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this, LoginActivity::class.java)
//                startActivty(intent.addFlags((Intent.FLAG_ACTIVITY_CLEAR_TOP)))
//                finish()
//            }
//            else if (tokenInfo != null) {
//                Toast.makeText(this, "토큰 정보 보기 성공", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                finish()
//            }
//        }
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "접근이 거부 됨(동의 취소)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Toast.makeText(this, "유효하지 않은 앱", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Toast.makeText(this, "인증 수단이 유효하지 않아 인증할 수 없는 상태", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Toast.makeText(this, "요청 파라미터 오류", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Toast.makeText(this, "유효하지 않은 scope ID", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Toast.makeText(this, "설정이 올바르지 않음(android key hash)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Toast.makeText(this, "서버 내부 에러", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Toast.makeText(this, "앱이 요청 권한이 없음", Toast.LENGTH_SHORT).show()
                    }
                    else -> { // Unknown
                        Toast.makeText(this, "기타 에러", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            else if (token != null) {
                Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                finish()
            }
        }


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
            val intent = Intent(this, InfoActivity::class.java)
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
                        moveMainPage(auth?.currentUser)
                    } else {
                        binding.warningText.visibility = View.VISIBLE
                    }
                }
        }
    }

    fun login(email:String,pw1:String){
        auth?.signInWithEmailAndPassword(email,pw1) // 로그인
            ?.addOnCompleteListener { result->
                if(result.isSuccessful){
                    var intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
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