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
import com.example.capston.databinding.ActivityRegister1Binding

class Register1Activity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityRegister1Binding
    private val binding get() = viewBinding!!

    private var validName: Boolean = false
    private lateinit var userName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegister1Binding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(viewBinding.name.length() > 0){
                    validName = true
//                    checkValid(validName, validId, validPw, validPw2)
                    if (validName){
                        viewBinding.nextBtn.isClickable = true
                        viewBinding.nextBtn.isEnabled = true
                        // 이름 변수 세팅
                        userName = binding.name.text.toString().trim()
                    }
                } else{
                    validName = false
                    viewBinding.nextBtn.isClickable = false
                    viewBinding.nextBtn.isEnabled = false
                }
            }
        })

        viewBinding.nextBtn.setOnClickListener {
            val intent = Intent(this, Register2Activity::class.java)
            // 레지스터2에 이름전달
            intent.putExtra("UserName",userName)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        viewBinding.name.clearFocus()
        return true
    }
}