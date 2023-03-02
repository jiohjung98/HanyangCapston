package com.example.capston

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.FragmentNaviMypageBinding
import com.kakao.sdk.user.UserApiClient

val serviceFragment = ServiceFragment()
val changePwFragment = ChangePwFragment()
val wroteFragment = WroteFragment()
//val questionFragment = ManyQuestionFragment()


class NaviMypageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentNaviMypageBinding

    // 1. Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    lateinit var mainActivity: MainActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        _binding = FragmentNaviMypageBinding.inflate(inflater, container, false)
//        val view = binding.root
//        return view
        binding = FragmentNaviMypageBinding.inflate(inflater, container, false)

        binding.serviceDetailBtn.setOnClickListener{
//            childFragmentManager.beginTransaction().apply {
//                replace(R.id.my_page_fr, serviceFragment)
//                addToBackStack(null)
//                commit()
//            }
            parentFragmentManager.beginTransaction().apply {
                    replace(R.id.main_frm, serviceFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.changePwBtn.setOnClickListener{
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.main_frm, changePwFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.writeDetailBtn.setOnClickListener{
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.main_frm, wroteFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

//        binding.questionBtn.setOnClickListener{
//            parentFragmentManager.beginTransaction().apply {
//                replace(R.id.main_frm, questionFragment)
//                    .addToBackStack(null)
//                    .commit()
//            }
//        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.logout.setOnClickListener{
            onClick(view)
        }
        binding.withdraw.setOnClickListener{
            onClickWithdraw(view)
        }
        binding.email.setOnClickListener{
            onClickEmail(view)
        }

        //카카오로 로그인 시, 마이페이지 프래그먼트에서 이름 띄워주기(무조건 여기(onActivityCreated)에 선언해줘야 오류 안남)
        val nickname = requireView().findViewById<TextView>(R.id.receive_name) // 로그인 버튼

        UserApiClient.instance.me { user, error ->
            nickname!!.text = "${user?.kakaoAccount?.profile?.nickname}"
        }
    }

    // fragment 액션바 없애주기
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    private fun onClick(view: View?) {
        val logoutDlg = LogoutDialog(mainActivity)
        Log.d("gdsa","asdgdsg")
        logoutDlg.setOnOKClickedListener{ content ->
            binding.logout.text = content
        }
        logoutDlg.show("메인의 내용을 변경할까요?")
    }

    private fun onClickWithdraw(view: View?) {
        val deleteDlg = DeleteDialog(mainActivity)
        deleteDlg.setOnOKClickedListener{ content ->
            binding.withdraw.text = content
        }
        deleteDlg.show("회원탈퇴")
    }

    private fun onClickEmail(view: View?) {
        val emailDlg = EmailDialog(mainActivity)
        emailDlg.setOnOKClickedListener { content ->
            binding.email.text = content
        }
        emailDlg.show("이메일")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NaviMypageFragment().apply {
            }
    }
}