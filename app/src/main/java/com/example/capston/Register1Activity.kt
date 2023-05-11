package com.example.capston

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.capston.databinding.ActivityRegister1Binding
import com.example.capston.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class Register1Activity : AppCompatActivity() {

    lateinit var viewBinding: ActivityRegister1Binding
    private val binding get() = viewBinding!!
    var DB: DBHelper? = null

    private lateinit var auth: FirebaseAuth

    var validName: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegister1Binding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        DB = DBHelper(this)

        auth = FirebaseAuth.getInstance()

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
                    }
                } else{
                    validName = false
                    viewBinding.nextBtn.isClickable = false
                    viewBinding.nextBtn.isEnabled = false
                }
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        viewBinding.name.clearFocus()
        return true
    }
}