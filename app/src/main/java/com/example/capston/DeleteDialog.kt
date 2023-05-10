package com.example.capston

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.DeletecompletedialogBinding
import com.example.capston.databinding.DeletedialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage


class DeleteDialog(private val context : MainActivity) {
    private lateinit var listener : MyDialogOKClickedListener
    private lateinit var binding : DeletedialogBinding
    private lateinit var binding2 : DeletecompletedialogBinding
    private val deleteDlg = Dialog(context)   //부모 액티비티의 context 가 들어감

    private val auth get() = context.auth
    private val database get() = context.database.child("users")
    private val storage = FirebaseStorage.getInstance()

    fun show(content : String) {
        binding = DeletedialogBinding.inflate(context.layoutInflater)

        // 다이얼로그 테두리 둥글게 만들기
        deleteDlg?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        deleteDlg?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        deleteDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        deleteDlg.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        deleteDlg.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        val params: WindowManager.LayoutParams = this.deleteDlg.window!!.attributes
        params.y = 500
        this.deleteDlg.window!!.attributes = params

        //ok 버튼 동작
        binding.yesBtn.setOnClickListener {
            Log.d("dd","click ok")
            deleteDlg.dismiss()
            deleteComplete()
            // 어플 종료
//            ActivityCompat.finishAffinity(context)
        }

        //cancel 버튼 동작
        binding.noBtn.setOnClickListener {
            deleteDlg.dismiss()
        }

        deleteDlg.show()
    }

    fun setOnOKClickedListener(listener: (String) -> Unit) {
        this.listener = object: MyDialogOKClickedListener {
            override fun onOKClicked(content: String) {
                listener(content)
            }
        }
    }

    interface MyDialogOKClickedListener {
        fun onOKClicked(content : String)
    }

    fun deleteComplete() {

        // 데이터베이스 users 에서 삭제
        database.child(auth.currentUser!!.uid).removeValue()
        // 스토리지 이미지 삭제
        val ref = storage.getReference("images").child("users").child(auth.currentUser!!.uid)
        // 폴더 내의 모든 파일과 폴더에 대한 참조 객체 목록 가져오기
        ref.listAll().addOnSuccessListener { listResult ->
            // 폴더 내의 모든 파일과 폴더에 대한 참조 객체를 삭제
            listResult.items.forEach { item ->
                item.delete().addOnSuccessListener {
                    // 파일 또는 폴더 삭제 성공
                }.addOnFailureListener { e ->
                    // 파일 또는 폴더 삭제 실패
                }
            }

            // 폴더 자체를 삭제
            ref.delete().addOnSuccessListener {
                // 폴더 삭제 성공
            }.addOnFailureListener { e ->
                // 폴더 삭제 실패
            }
        }.addOnFailureListener { e ->
            // 폴더 내의 파일과 폴더 목록 가져오기 실패
        }

        binding2 = DeletecompletedialogBinding.inflate(context.layoutInflater)

        deleteDlg.setContentView(binding2.root)
        deleteDlg.setCancelable(false)
        deleteDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val params: WindowManager.LayoutParams = this.deleteDlg.window!!.attributes
        params.y = 500
        this.deleteDlg.window!!.attributes = params

        deleteDlg.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        deleteDlg.show()
        auth.currentUser?.delete()
//      액티비티로 이동(첫화면)
        Handler(Looper.getMainLooper()).postDelayed({
            // 파이어베이스 auth 삭제
            val intent = Intent(context, SplashActivity::class.java)
            context.startActivity(intent)
            deleteDlg.dismiss()
            (context as Activity).finish()
        }, 3000)
    }
}
