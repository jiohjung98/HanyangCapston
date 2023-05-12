package com.example.capston

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.example.capston.databinding.ActivityRegister2Binding
import com.example.capston.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_info.*
import java.util.regex.Pattern

class Register2Activity : AppCompatActivity() {

    lateinit var viewBinding: ActivityRegister2Binding
    private val binding get() = viewBinding!!
    var DB: DBHelper? = null
    private lateinit var auth: FirebaseAuth

    var ValidEmail: Boolean= false
    var emailOk: Boolean = false

    private lateinit var name : String
    private lateinit var email : String
    private lateinit var pw1 : String
    private lateinit var pw2 : String

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegister2Binding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        DB = DBHelper(this)

        auth = FirebaseAuth.getInstance()

        viewBinding.editTextTextEmailAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(viewBinding.editTextTextEmailAddress.length() > 0){
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
            email = binding.editTextTextEmailAddress.text.toString().trim() // 이메일

            if(email.isEmpty()){
                viewBinding.emailLayout.visibility = View.VISIBLE
                viewBinding.nextBtn.isEnabled = false
            } else if (!email.contains("@") || !(email.contains("."))){
                viewBinding.emailLayout.visibility = View.VISIBLE
            }
            else {
                val intent = Intent(this, EmailVerifyActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // 화면 클릭하여 키보드 숨기기 및 포커스 제거
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action === MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
                binding.emailLayout.visibility = View.INVISIBLE
                binding.editTextTextEmailAddress.clearFocus()
            }
        }
        return super.dispatchTouchEvent(event)
    }
}