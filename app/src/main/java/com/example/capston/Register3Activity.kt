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
import com.example.capston.databinding.ActivityRegister3Binding
import com.example.capston.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_info.*
import java.util.regex.Pattern

class Register3Activity : AppCompatActivity() {

    // 에딧텍스트에 값 4개 다 들어가야 버튼 활성화되는 코드
    var validEditText1: Boolean = false
    var validEditText2: Boolean = false
    var validEditText3: Boolean = false
    var validEditText4: Boolean = false

    lateinit var viewBinding: ActivityRegister3Binding
    private val binding get() = viewBinding!!
    var DB: DBHelper? = null

    private lateinit var auth: FirebaseAuth

    private lateinit var name : String
    private lateinit var email : String
    private lateinit var pw1 : String
    private lateinit var pw2 : String

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityRegister3Binding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        DB = DBHelper(this)

        auth = FirebaseAuth.getInstance()

        binding.editTextTextPassword1.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(editTextTextPassword1.length() > 0){
                    validEditText3 = true
                    if (validEditText3 && validEditText4){
                        next_btn.isClickable = true
                        next_btn.isEnabled = true
                    }
                } else{
                    validEditText3 = false
                    next_btn.isClickable = false
                    next_btn.isEnabled = false
                }
            }
        })

        binding.editTextTextPassword2.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun afterTextChanged(p0: Editable?) {
                if(editTextTextPassword2.length() > 0){
                    validEditText4 = true
                    if (validEditText3 && validEditText4){
                        next_btn.isClickable = true
                        next_btn.isEnabled = true
                    }
                } else{
                    validEditText4 = false
                    next_btn.isClickable = false
                    next_btn.isEnabled = false
                }
            }
        })

        binding.backButton.setOnClickListener {
            val intent = Intent(this, Register2Activity::class.java)
            startActivity(intent)
        }

        viewBinding.nextBtn!!.setOnClickListener {
            // 이름, 이메일 받아오는 것 해주세요
            pw1 = binding.editTextTextPassword1.text.toString().trim() // 비밀번호
            pw2 = binding.editTextTextPassword2.text.toString() // 비밀번호 확인

            if(pw1.isEmpty()){
                viewBinding.pw1Layout.visibility = View.VISIBLE
                viewBinding.nextBtn.isEnabled = false
            } else if (!Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$", pw1)) {
                viewBinding.pw1Layout.visibility = View.VISIBLE
            }

            if(pw2.isEmpty()){
                viewBinding.pw2Layout.visibility = View.VISIBLE
                viewBinding.nextBtn.isEnabled = false
            } else if (pw1 != pw2){
                viewBinding.pw2Layout.visibility = View.VISIBLE
            }

            if(Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$", pw1) && (pw1 == pw2)) {
                val intent = Intent(this, RegisterCompleteActivity::class.java)
//                intent.putExtra("username", viewBinding.inputName.text.toString())
                startActivity(intent)
            }
//            createUser(name, email, pw1)
        }
    }
//
//    private fun createUser(name: String, email: String, password: String) {
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
////                    addNewUserToDB(task.result.user!!.uid,name,email)
//                    updateUI(user)
//                } else {
//                    updateUI(null)
//                }
//            }
//            .addOnFailureListener {
//            }
//    }

    internal fun updateUI(user: FirebaseUser?) {
        user?.let {
//            binding.txtResult.text = "Email: ${user.email}\nUid: ${user.uid}"
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
                binding.pw1Layout.visibility = View.INVISIBLE
                binding.pw2Layout.visibility = View.INVISIBLE
                binding.editTextTextPassword1.clearFocus()
                binding.editTextTextPassword2.clearFocus()
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}