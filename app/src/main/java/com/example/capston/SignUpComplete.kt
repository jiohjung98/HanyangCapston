package com.example.capston

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.data.UserData
import com.example.capston.databinding.ActivitySignupcompleteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dog_register.*

class SignUpComplete : AppCompatActivity() {
    private lateinit var viewBinding: ActivitySignupcompleteBinding

    private val database: DatabaseReference =
        Firebase.database.reference
    private lateinit var auth : FirebaseAuth

    private var name : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySignupcompleteBinding.inflate(layoutInflater)
        setContentView(viewBinding.root);

        auth = FirebaseAuth.getInstance()
        name = intent.getStringExtra("name")

        auth.currentUser?.reload()

        // 유저정보 DB에 등록
        addNewUserToDB(auth.currentUser!!.uid,name,auth.currentUser!!.email)

        viewBinding.agreeBtn.isEnabled = true

        viewBinding.agreeBtn.setOnClickListener {
            val intent = Intent(this, DogRegisterActivity::class.java)
            startActivity(intent)
//            overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit)
        }
    }

    // 뒤로가기 -> 건너뛰기
    override fun onBackPressed() {
        val skipDialog = SkipDialog(this)
        skipDialog.setOnOKClickedListener { content ->
        }
        skipDialog.show("초기화면")
    }

    private fun addNewUserToDB(userId: String, name: String?, email: String?) {
        // 현재 반려견 없음 = 0번 인덱스
        val user_data = UserData(name,email,null,0)
        database.child("users").child(userId).setValue(user_data)
    }
}