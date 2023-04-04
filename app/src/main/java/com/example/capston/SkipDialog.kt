package com.example.capston


import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.BacktostartDialogBinding
import com.example.capston.databinding.SkipDialogBinding
import com.google.firebase.auth.FirebaseAuth

/*
 * 마이페이지 이메일 문의 누르면 이메일 보내겠냐는 다이얼로그
 * -> dogplanet@dogplanet.com 으로 메일 보내도록 외부 프로그램 연결
 */
class SkipDialog(private val context : AppCompatActivity) {
    private lateinit var listener : MyDialogOKClickedListener
    private lateinit var binding : SkipDialogBinding
    private var BackStartDialog = Dialog(context)   //부모 액티비티의 context 가 들어감

    fun show(content : String) {
        binding = SkipDialogBinding.inflate(context.layoutInflater)

        // 다이얼로그 테두리 둥글게 만들기
        BackStartDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        BackStartDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        BackStartDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        BackStartDialog.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        BackStartDialog.setCancelable(true)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        val params: WindowManager.LayoutParams = this.BackStartDialog.window!!.attributes
        params.y = 500
        this.BackStartDialog.window!!.attributes = params

        //ok 버튼 동작
        binding.yesBtn.setOnClickListener {
            BackStartDialog.dismiss()
            backProcess()
        }

        //cancel 버튼 동작
        binding.noBtn.setOnClickListener {
            BackStartDialog.dismiss()
        }

        BackStartDialog.show()
    }

    fun setOnOKClickedListener(listener: (String) -> Unit) {
        this.listener = object: MyDialogOKClickedListener {
            override fun onOKClicked(content: String) {
                listener(content)
            }
        }
    }

    private fun backProcess(){
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        (context as Activity).finish()
    }

    interface MyDialogOKClickedListener {
        fun onOKClicked(content : String)
    }

}