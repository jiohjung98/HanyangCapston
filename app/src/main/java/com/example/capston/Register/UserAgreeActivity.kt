package com.example.capston.Register;
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import com.example.capston.BacktoStartDialog
import com.example.capston.R
import com.example.capston.data.UserData
import com.example.capston.databinding.ActivityAgreeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class UserAgreeActivity : AppCompatActivity() {
    private lateinit var binding: com.example.capston.databinding.ActivityAgreeBinding
    var isPageOpen : Boolean = false
    lateinit var DownAnim : Animation
    lateinit var UptAnim : Animation

    private var name : String? = null
    private var email : String? = null
    private var pw : String? = null

    private val database: DatabaseReference = Firebase.database.reference
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgreeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        auth.currentUser!!.reload()

        //
        UptAnim = AnimationUtils.loadAnimation(this, R.anim.agree_translate_down)
        DownAnim = AnimationUtils.loadAnimation(this, R.anim.agree_translate_up)
        UptAnim.setAnimationListener(SlidingPageAnimationListener())
        DownAnim.setAnimationListener(SlidingPageAnimationListener())
        //
        binding.allAgree.setOnClickListener { onCheckChanged(binding.allAgree) }
        binding.agree1.setOnClickListener { onCheckChanged(binding.agree1) }
        binding.agree2.setOnClickListener { onCheckChanged(binding.agree2) }
        binding.agree3.setOnClickListener { onCheckChanged(binding.agree3) }


        name = intent.getStringExtra("UserName")
        email = intent.getStringExtra("Email")
        pw = intent.getStringExtra("PW")

        binding.contentbtn1.setOnClickListener {
            if (isPageOpen) {
                binding.content.startAnimation(DownAnim)
            }
            else{
                binding.content.visibility = View.VISIBLE
                binding.content.startAnimation(DownAnim)
            }
        }
        binding.contentbtn2.setOnClickListener {
            if (isPageOpen) {
                binding.content.startAnimation(DownAnim)
            }
            else{
                binding.content.visibility = View.VISIBLE
                binding.content.startAnimation(DownAnim)
            }
        }
        binding.contentbtn3.setOnClickListener {
            if (isPageOpen) {
                binding.content.startAnimation(DownAnim)
            }
            else{
                binding.content.visibility = View.VISIBLE
                binding.content.startAnimation(DownAnim)
            }
        }


        binding.agreeBtn.setOnClickListener {
            if (isPageOpen) {
                binding.content.startAnimation(UptAnim)
            }
            else{
                addNewUserToDB(auth.currentUser!!.uid, name, email)
            }
        }

        // 좌측 위 뒤로가기 버튼 누르면 실행되는것
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    // 유저 db 등록
    private fun addNewUserToDB(userId: String, name: String?, email: String?) {
        // 현재 반려견 없음 = 0번 인덱스
        val user_data = UserData(name,email,null,0)
        database.child("users").child(userId).setValue(user_data).addOnSuccessListener {
            val intent = Intent(this, RegisterCompleteActivity::class.java)
            intent.putExtra("UserName",name)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit)
            finish()
        }
    }

    private inner class SlidingPageAnimationListener : Animation.AnimationListener {

        override fun onAnimationEnd(animation: Animation?) : Unit {
            if(isPageOpen){
                binding.content.visibility=View.INVISIBLE
                isPageOpen = false
                binding.agreeBtn.isEnabled = false
                binding.agreeBtn.text = "다음으로"
                binding.allAgree.isChecked = false
                binding.agree1.isChecked = false
                binding.agree2.isChecked = false
                binding.agree3.isChecked = false
            }
            else{
                isPageOpen = true
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.agreeBtn.isEnabled = true
                    binding.agreeBtn.text = "확인"
                }, 0)
            }
        }
        override fun onAnimationStart(animation: Animation?) {}
        override fun onAnimationRepeat(animation: Animation?) {}
    }



    private fun onCheckChanged(compoundButton: CompoundButton) {
        when(compoundButton.id) {
            R.id.all_agree -> {
                if (binding.allAgree.isChecked) {
                    binding.agree1.isChecked = true
                    binding.agree2.isChecked = true
                    binding.agree3.isChecked = true
                }else {
                    binding.agree1.isChecked = false
                    binding.agree2.isChecked = false
                    binding.agree3.isChecked = false
                }
            }
            else -> {
                binding.allAgree.isChecked = (binding.agree1.isChecked && binding.agree2.isChecked && binding.agree3.isChecked)
            }
        }
        binding.agreeBtn.isEnabled = binding.agree1.isChecked && binding.agree2.isChecked
        if(binding.agree1.isChecked){
            //권한 넣기
        }
        if(binding.agree2.isChecked){
            //권한 넣기
        }
        if(binding.agree3.isChecked){
            //권한 넣기
        }
    }

    // 뒤로가기 -> 시작화면
    override fun onBackPressed() {
        val backtoStartDialog = BacktoStartDialog(this)
        backtoStartDialog.setOnOKClickedListener { content ->
        }
        backtoStartDialog.show("초기화면")
    }

}