package com.example.capston.homepackage

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.SplashActivity
import com.example.capston.WalkActivity
import com.example.capston.databinding.CustomdialogWalkFailBinding
import com.example.capston.databinding.CustomdialogWalkResultBinding
import com.example.capston.databinding.CustomdialogWalkStartBinding
import com.example.capston.databinding.WalkdialogBinding

class WalkDialog(private val context : AppCompatActivity) {
    private lateinit var listener : MyDialogOKClickedListener
    private lateinit var binding : WalkdialogBinding
    private val walkDlg = Dialog(context)   //부모 액티비티의 context 가 들어감

    fun show(content : String) {
        binding = WalkdialogBinding.inflate(context.layoutInflater)

        // 다이얼로그 테두리 둥글게 만들기
        walkDlg?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        walkDlg?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        walkDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        walkDlg.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        walkDlg.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        val params: WindowManager.LayoutParams = this.walkDlg.window!!.attributes
        params.y = 500
        this.walkDlg.window!!.attributes = params


        binding.yes.setOnClickListener {
            walkDlg.dismiss()
            goWalk()
        }

        binding.no.setOnClickListener {
            walkDlg.dismiss()
        }

        walkDlg.show()
    }

    fun setOnOKClickedListener(listener: (String) -> Unit) {
        this.listener = object : MyDialogOKClickedListener {
            override fun onOKClicked(content: String) {
                listener(content)
            }
        }
    }
    interface MyDialogOKClickedListener {
        fun onOKClicked(content : String)
    }

    fun goWalk() {
        // 산책 액티비티로 이동(첫화면)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(context, WalkActivity::class.java)
            context.startActivity(intent)
            (context as Activity).finish()
        }, 1000)
    }
}