package com.example.capston

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.LostDogInfoBinding
import com.example.capston.homepackage.NaviHomeFragment
import com.jakewharton.threetenabp.AndroidThreeTen.init


class DogInfoEnterDialog(private val context : AppCompatActivity) {

    private lateinit var binding: LostDogInfoBinding
    private var dogInfoDialog = Dialog(context)   //부모 액티비티의 context 가 들어감


    fun myDlg() {
        dogInfoDialog.show()
        binding = LostDogInfoBinding.inflate(context.layoutInflater)

        // 다이얼로그 테두리 둥글게 만들기
        dogInfoDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dogInfoDialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dogInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dogInfoDialog.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        dogInfoDialog.setCancelable(true)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함


        // 다이얼로그 뜨는 위치 조정하기
        val params: WindowManager.LayoutParams = this.dogInfoDialog.window!!.attributes
        params.y = 800
        this.dogInfoDialog.window!!.attributes = params
        val info = dogInfoDialog.findViewById<EditText>(R.id.receiveInfo)
        val time = dogInfoDialog.findViewById<EditText>(R.id.receiveTime)

        binding.yesBtn.setOnClickListener {
//            onClickedListener.onClicked(time.text.toString(), info.text.toString())
            dogInfoDialog.dismiss()
        }

        binding.noBtn.setOnClickListener {
            dogInfoDialog.dismiss()

        }
    }

    interface ButtonClickListener {
        fun onClicked(time: String, info: String)
    }

    private lateinit var onClickedListener: ButtonClickListener

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickedListener = listener
    }
}

//
//    fun setOnOKClickedListener(listener: (String) -> Unit) {
//        this.listener = object: MyDialogOKClickedListener {
//            override fun onOKClicked(content: String) {
//                listener(content)
//            }
//        }
//    }
//
//    interface MyDialogOKClickedListener {
//        fun onOKClicked(content : String)
//    }


//
//class LogoutDialog(): DialogFragment() {
//
//    private var _binding: LogoutdialogBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = LogoutdialogBinding.inflate(inflater, container, false)
//        val view = binding.root
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
//
//        initDialog()
//        return view
//    }
//
//    fun initDialog() {
//
//        binding.yesBtn.setOnClickListener {
//            buttonClickListener.onButton1Clicked()
//            dismiss()
//        }
//
//        binding.noBtn.setOnClickListener {
//            buttonClickListener.onButton2Clicked()
//            dismiss()
//        }
//
//    }
//
//    interface OnButtonClickListener {
//        fun onButton1Clicked()
//        fun onButton2Clicked()
//    }
//
//    override fun onStart() {
//        super.onStart();
//        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
//        lp.copyFrom(dialog!!.window!!.attributes)
//        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        val window: Window = dialog!!.window!!
//        window.attributes = lp
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    // 클릭 이벤트 설정
//    fun setButtonClickListener(buttonClickListener: OnButtonClickListener) {
//        this.buttonClickListener = buttonClickListener
//    }
//
//    // 클릭 이벤트 실행
//    private lateinit var buttonClickListener: OnButtonClickListener
//}

//lateinit var mainActivity: MainActivity
//
//private fun onClick(view: View?) {
//    val logoutDlg = LogoutDialog(mainActivity)
//    Log.d("gdsa","asdgdsg")
//    logoutDlg.setOnOKClickedListener{ content ->
//        binding.yesBtn.text = content
//    }
//    logoutDlg.show("메인의 내용을 변경할까요?")
//}
