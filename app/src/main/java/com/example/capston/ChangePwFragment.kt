package com.example.capston

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.ChangePwCompleteDialogBinding
import com.example.capston.databinding.FragmentChangePwBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_change_pw.*
import java.util.regex.Pattern

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ChangePwFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var curPw : String
    private lateinit var pw1 : String
    private lateinit var pw2 : String
    private lateinit var email : String

    private lateinit var user : FirebaseUser

    private var validCur: Boolean = false
    private var validPw1: Boolean = false
    private var validPw2: Boolean = false
    private var validAll: Boolean = false

    lateinit var binding: FragmentChangePwBinding

    private lateinit var mainActivity: MainActivity
    private lateinit var database : DatabaseReference
    private lateinit var auth : FirebaseAuth

    private lateinit var dialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mainActivity = context as MainActivity
        database = mainActivity.database
        auth = mainActivity.auth
        user = auth.currentUser!!
        email = user.email.toString()

        dialog = initDialog()

        // Inflate the layout for this fragment
        binding = FragmentChangePwBinding.inflate(inflater, container, false)

        binding.changePwBtn.isClickable = false
        binding.changePwBtn.isEnabled = false

        binding.curPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(cur_pw.length() > 0){
                    validCur = true
                    if (validCur && validPw1 && validPw2){
                        binding.changePwBtn.isClickable = true
                        binding.changePwBtn.isEnabled = true
                    }
                } else{
                    validCur = false
                }
            }
        })


        binding.newPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(new_pw.length() > 0){
                    validPw1 = true
                    if (validCur && validPw1 && validPw2){
                        binding.changePwBtn.isClickable = true
                        binding.changePwBtn.isEnabled = true
                    }
                } else{
                    validPw1 = false
                }
            }
        })

        binding.rePw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(re_pw.length() > 0){
                    validPw2 = true
                    if (validCur && validPw1 && validPw2){
                        binding.changePwBtn.isClickable = true
                        binding.changePwBtn.isEnabled = true
                    }
                } else{
                    validPw2 = false
                }
            }
        })

        // 뒤로가기
        binding.backButton.setOnClickListener{
            parentFragmentManager.beginTransaction().remove(this).apply {
                replace(R.id.main_frm, MypageFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        // 변경 버튼
        binding.changePwBtn.setOnClickListener {
            validAll = true

            curPw = binding.curPw.text.toString()
            pw1 = binding.newPw.text.toString() // 새 비밀번호
            pw2 = binding.rePw.text.toString() // 비밀번호 확인

            binding.pw1Layout.visibility = View.INVISIBLE
            binding.pw2Layout.visibility = View.INVISIBLE
            binding.pw3Layout.visibility = View.INVISIBLE

            if(curPw.isEmpty() ||
                (!Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$", pw1))) {
                binding.pw1Layout.visibility = View.VISIBLE
                validAll = false
            }


            if(pw1.isEmpty() || !Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{8,}\$", pw1)){
                binding.pw2Layout.visibility = View.VISIBLE
                validAll = false
            }

            if(pw2.isEmpty() || pw1 != pw2){
                binding.pw3Layout.visibility = View.VISIBLE
                validAll = false
            }

            Log.d("VALID",validAll.toString())

            if(validAll){
                changePassWord(pw1)
            }
        }

        return binding.root
    }

    private fun changePassWord(new_pw: String){
        curPw = binding.curPw.text.toString()
        // 현재 비밀번호 확인
        user.reauthenticate(EmailAuthProvider.getCredential(email, curPw)).addOnSuccessListener {
            binding.changePwBtn.isEnabled = false
            // 비밀번호 변경 수행
            user.updatePassword(new_pw).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d("PASSWORD","비밀번호변경성공")
//                Toast.makeText(mainActivity, "비밀번호가 변경되었습니다.", Toast.LENGTH_LONG).show()
                    backProcess(setDialog(0))
                }else{
                    Log.d("PASSWORD","비밀번호변경실패: ${task.exception.toString()}")
//                Toast.makeText(mainActivity, "비밀번호 변경 실패.", Toast.LENGTH_LONG).show()
                    backProcess(setDialog(1))
                }
            }
        }.addOnFailureListener{
            binding.pw1Layout.visibility = View.VISIBLE
            validAll = false
        }


    }

    private fun initDialog() : Dialog{
        val dialog = Dialog(mainActivity)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거

        return dialog
    }

    private fun setDialog(flag : Int) : Dialog{
        val viewBinding = ChangePwCompleteDialogBinding.inflate(mainActivity.layoutInflater)

        when(flag){
            0 -> {
                viewBinding.contextText.text = "비밀번호가 변경되었습니다."
            }
            1 -> {
                viewBinding.contextText.text = "변경 실패하였습니다.\n 입력하신 정보를 다시 확인해주세요"
            }
        }

        dialog.setContentView(viewBinding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        dialog.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        return dialog
    }

    private fun backProcess(dialog : Dialog) {
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            parentFragmentManager.beginTransaction().remove(this@ChangePwFragment).commit()
            parentFragmentManager.popBackStack()
        }, 2000)


    }


    // fragment 액션바 없애주기
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
    }


    override fun onResume() {
        super.onResume()
        binding.curPw.setText("") // EditText 초기화
        binding.newPw.setText("")
        binding.rePw.setText("")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChangePwFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChangePwFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}