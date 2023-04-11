package com.example.capston

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.BacktomainDialogBinding


class BacktoMainDialog(private val context : AppCompatActivity) {
    private lateinit var listener : MyDialogOKClickedListener
    private lateinit var binding : BacktomainDialogBinding
    private var BackMainDialog = Dialog(context)   //부모 액티비티의 context 가 들어감

    fun show() {
        binding = BacktomainDialogBinding.inflate(context.layoutInflater)

        // 다이얼로그 테두리 둥글게 만들기
        BackMainDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        BackMainDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        BackMainDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        BackMainDialog.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        BackMainDialog.setCancelable(true)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        val params: WindowManager.LayoutParams = this.BackMainDialog.window!!.attributes
        params.y = 500
        this.BackMainDialog.window!!.attributes = params

        //ok 버튼 동작
        binding.yesBtn.setOnClickListener {
            BackMainDialog.dismiss()
            backProcess()
        }

        //cancel 버튼 동작
        binding.noBtn.setOnClickListener {
            BackMainDialog.dismiss()
        }

        BackMainDialog.show()
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
        context.finish()
    }

    interface MyDialogOKClickedListener {
        fun onOKClicked(content : String)
    }

}