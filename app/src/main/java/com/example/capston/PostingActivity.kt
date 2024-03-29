package com.example.capston

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import com.example.capston.databinding.ActivityPostingBinding
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_posting.*

class PostingActivity : AppCompatActivity() {

    var validEditText1: Boolean = false
    var validEditText2: Boolean = false

    private lateinit var binding: ActivityPostingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closePosting.setOnClickListener {  }

        binding.okPosting.setOnClickListener {  }

        binding.postingTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(posting_title.length() > 0){
                    validEditText1 = true
                    if (validEditText1 && validEditText2){
                        // 완료 버튼의 색상을 바꾼다
                        binding.okPosting.setTextColor(Color.parseColor("#D19200"))
                    }
                } else{
                    validEditText1 = false
                    // 완료 버튼의 색상을 바꾸지 않는다
                    binding.okPosting.setTextColor(Color.parseColor("#999999"))
                }
            }
        })

        binding.postingMain.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(posting_main.length() > 0){
                    validEditText2 = true
                    if (validEditText1 && validEditText2){
                        // 완료 버튼의 색상을 바꾼다
                        binding.okPosting.setTextColor(Color.parseColor("#D19200"))
                    }
                } else{
                    validEditText2 = false
                    // 완료 버튼의 색상을 바꾸지 않는다
                    binding.okPosting.setTextColor(Color.parseColor("#999999"))
                }
            }
        })

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }
}