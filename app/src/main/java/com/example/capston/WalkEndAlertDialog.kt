package com.example.capston

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.DeletecompletedialogBinding
import com.example.capston.databinding.DeletedialogBinding
import com.example.capston.databinding.WalkEndAlertDialogBinding
import com.example.capston.databinding.WalkEndDialogBinding
import kotlinx.android.synthetic.main.activity_walk.*

class WalkEndAlertDialog(private val context : AppCompatActivity) {
    private lateinit var  listner: MyDialogOKClickedListener
    private lateinit var binding : WalkEndAlertDialogBinding
    private lateinit var binding2 : WalkEndDialogBinding
    private val dlg = Dialog(context)

    fun show(content: String) {
        binding = WalkEndAlertDialogBinding.inflate(context.layoutInflater)

        // 다이얼로그 테두리 둥글게 만들기
        dlg?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dlg?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        val params: WindowManager.LayoutParams = this.dlg.window!!.attributes
        params.y = 500
        this.dlg.window!!.attributes = params

        //ok 버튼 동작
        binding.yesBtn.setOnClickListener {
            Log.d("dd","click ok")
            dlg.dismiss()
            walkComplete()
            // 어플 종료
//            ActivityCompat.finishAffinity(context)
        }

        //cancel 버튼 동작
        binding.noBtn.setOnClickListener {
            dlg.dismiss()
        }

        dlg.show()
    }

    fun setOnOKClickedListener(listener: (String) -> Unit) {
        this.listner = object: MyDialogOKClickedListener {
            override fun onOKClicked(content: String) {
                listener(content)
            }
        }
    }

    interface MyDialogOKClickedListener {
        fun onOKClicked(content : String)
    }

    fun walkComplete() {
        binding2 = WalkEndDialogBinding.inflate(context.layoutInflater)

        dlg.setContentView(binding2.root)
        dlg.setCancelable(false)
        dlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val params: WindowManager.LayoutParams = this.dlg.window!!.attributes
        params.y = 500
        this.dlg.window!!.attributes = params

        dlg.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dlg.show()
//      액티비티로 이동(첫화면)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            (context as Activity).finish()
        }, 3000)
    }

}