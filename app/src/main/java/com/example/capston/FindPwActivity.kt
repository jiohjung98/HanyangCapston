package com.example.capston

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.capston.databinding.ActivityFindPwBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dog_register.*
import kotlinx.android.synthetic.main.activity_find_pw.*
import kotlinx.android.synthetic.main.activity_find_pw.next_page_btn
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.fragment_navi_mypage.*
import net.daum.mf.map.api.MapView
import java.util.regex.Pattern

class FindPwActivity : AppCompatActivity() {

    var validEditText1: Boolean = false
    var validEditText2: Boolean = false

    lateinit var auth : FirebaseAuth

    private lateinit var binding: ActivityFindPwBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindPwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth =FirebaseAuth.getInstance()

        binding.nameEdtText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(name_edt_text.length() > 0){
                    validEditText1 = true
                    if (validEditText1 && validEditText2){
                        next_page_btn.isClickable = true
                        next_page_btn.isEnabled = true
                    }
                } else{
                    validEditText1 = false
                    next_page_btn.isClickable = false
                    next_page_btn.isEnabled = false
                }
            }
        })

        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(email_edit_text.length() > 0){
                    validEditText2 = true
                    if (validEditText1 && validEditText2){
                        next_page_btn.isClickable = true
                        next_page_btn.isEnabled = true
                    }
                } else{
                    validEditText2 = false
                    next_page_btn.isClickable = false
                    next_page_btn.isEnabled = false
                }
            }
        })

        binding.backButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.nextPageBtn.setOnClickListener {
            var name = binding.nameEdtText.text.toString() // 이름
            var email = binding.emailEditText.text.toString() // 이메일

            if(email.isEmpty()){
                binding.emailLayout.visibility = View.VISIBLE
                next_btn.isEnabled = false
            } else if (!email.contains("@")){
                binding.emailLayout.visibility = View.VISIBLE
            }

            if((email.contains("@")) && (name.isNotEmpty())) {
                findPassword()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        name_edt_text.clearFocus()
        email_edit_text.clearFocus()
        return true
    }

    fun findPassword() {
        val auth = FirebaseAuth.getInstance()
        val email = binding.emailEditText.text.toString()
        if (email.length != 0) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, FindPwSuccessActivity::class.java)
                        startActivity(intent)
                    }
                }
        }
        else {
            val intent = Intent(this, FindPwFailActivity::class.java)
            startActivity(intent)
        }
    }

    // 이메일 없는 경우 처리해야되는데 아직 안함

//    // 사용자 이메일 가져오기
//    private fun getEmail () : String? {
//        val user = Firebase.auth.currentUser
//        if (user != null) {
//            user?.let {
//                val email = user.email
//                return email.toString()
//            }
//        } else {
//            // No user is signed in
//            return null
//        }
//    }
//        FirebaseAuth.getInstance().sendPasswordResetEmail(email_edit_text.text.toString())
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // db에 회원정보있으면 find_pw_success로, 없으면 find_pw_fail로
//                    val intent = Intent(this, FindPwSuccessActivity::class.java)
//                    startActivity(intent)
//                } else {
//                    val intent = Intent(this, FindPwFailActivity::class.java)
//                    startActivity(intent)
//                }
//            }

}
