package com.example.capston

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.capston.databinding.ActivityEmailverificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class EmailVerifyFragment : Fragment() {

    // 1. Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    lateinit var infoActivity: InfoActivity
    lateinit var database : DatabaseReference
    lateinit var auth : FirebaseAuth

    private lateinit var viewBinding: ActivityEmailverificationBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        infoActivity = context as InfoActivity
        database = infoActivity.database
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = ActivityEmailverificationBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewBinding.nextBtn.isEnabled = false

        viewBinding.emailBtn.setOnClickListener {
//            createUser(infoActivity.name,infoActivity.email,infoActivity.pw1)
        }

        viewBinding.nextBtn.setOnClickListener {
            val intent = Intent(activity, UserAgreeActivity::class.java)
            startActivity(intent)
        }
    }


    private fun sendEmailVerification(){
        FirebaseAuth.getInstance().currentUser
            ?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "메일을 보냈어요!", Toast.LENGTH_LONG).show()
                    viewBinding.emailBtn.isEnabled = false
                } else {
                    Toast.makeText(activity, task.exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkEmailVerification() {
        if(FirebaseAuth.getInstance().currentUser!!.isEmailVerified){
//            Toast.makeText(this, "이메일 인증이 이미 완료되었습니다", Toast.LENGTH_LONG).show()
            viewBinding.nextBtn.isEnabled = true
        }
    }
}