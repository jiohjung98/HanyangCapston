package com.example.capston

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.capston.databinding.ActivityEmailverificationBinding
import com.google.firebase.auth.FirebaseAuth

class EmailVerifyActivity : AppCompatActivity() {

    // 1. Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    private lateinit var auth : FirebaseAuth

    private lateinit var viewBinding: ActivityEmailverificationBinding

    private var name : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityEmailverificationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        auth = FirebaseAuth.getInstance()

        name = intent.getStringExtra("name")

        viewBinding.nextBtn.isEnabled = false

        // 이메일 전송 클릭 시 -> 이메일보내고 인증확인,안왔는지,재전송 버튼 활성화
        viewBinding.emailBtn.setOnClickListener {
            sendEmailVerification()
            viewBinding.emailError.visibility = View.VISIBLE
            viewBinding.emailResend.visibility = View.VISIBLE
            viewBinding.checkEmail.visibility = View.VISIBLE
            viewBinding.emailResend.isEnabled = true
            viewBinding.checkEmail.isEnabled = true
        }

        // 인증확인 클릭 시
        viewBinding.checkEmail.setOnClickListener {
            onClickCheckEmail()
        }

        // 재전송 버튼 - 한번만
        viewBinding.emailResend.setOnClickListener {
            sendEmailVerification()
            viewBinding.emailResend.isEnabled = false
        }

        viewBinding.nextBtn.setOnClickListener {
            val intent = Intent(this, UserAgreeActivity::class.java)
            intent.putExtra("name", name)
            startActivity(intent)
        }
    }

    // 뒤로가기 -> 시작화면
    override fun onBackPressed() {
        val backtoStartDialog = BacktoStartDialog(this)
        backtoStartDialog.setOnOKClickedListener { content ->
        }
        backtoStartDialog.show("초기화면")
    }


    private fun sendEmailVerification(){
        auth.currentUser
            ?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "메일을 보냈어요!", Toast.LENGTH_LONG).show()
                    viewBinding.emailBtn.isEnabled = false
                } else {
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkEmailVerification() {
        if(!(FirebaseAuth.getInstance().currentUser!!.isEmailVerified)){
            Toast.makeText(this, "메일함을 확인해주세요.", Toast.LENGTH_LONG).show()
        }
    }

    private fun onClickCheckEmail(){
        auth.currentUser!!.reload()
        if (auth.currentUser?.isEmailVerified == true) {
            Log.d("valid", "메일인증확인")
            viewBinding.emailResend.isEnabled = false
            viewBinding.emailError.visibility = View.INVISIBLE
            viewBinding.emailResend.visibility = View.INVISIBLE
            viewBinding.checkEmail.setTextColor(ContextCompat.getColor(this, R.color.gray))
            viewBinding.checkEmail.isEnabled = false
            viewBinding.nextBtn.isEnabled = true
        } else {
            Log.d("invalid", "${auth.currentUser?.isEmailVerified}")
            Toast.makeText(this, "메일함을 확인해주세요.", Toast.LENGTH_LONG).show()
        }
    }

}