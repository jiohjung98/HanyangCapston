package com.example.capston

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.example.capston.databinding.BacktomainDialogBinding
import kotlinx.android.synthetic.main.activity_tracking.*

class ExpectingDialog(context: Context, val activity: TrackingActivity) : Dialog(context){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = BacktomainDialogBinding.inflate(layoutInflater)

        view.backText.text = "추정위치를 확인하시겠어요?"
        view.yesBtn.setOnClickListener {
            activity.gotoExpect()
        }

        view.noBtn.setOnClickListener {
            this.dismiss()
        }

        setContentView(view.root)

        // 취소 불가능
        setCancelable(true)

        // 배경 투명하게 바꿔줌
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}