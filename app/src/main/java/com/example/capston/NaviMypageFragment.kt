package com.example.capston

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.capston.databinding.FragmentNaviMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

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
    lateinit var database : DatabaseReference
    lateinit var auth : FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
        database = mainActivity.database
        auth = mainActivity.auth
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

        binding.service.setOnClickListener{
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

        binding.changePw.setOnClickListener{
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.main_frm, changePwFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.dogAdmin.setOnClickListener{
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
        binding.logout.setOnClickListener {
            onClick(view)
        }
        binding.withdraw.setOnClickListener {
            onClickWithdraw(view)
        }
        binding.email.setOnClickListener {
            onClickEmail(view)
        }
        getFromDB()
    }

    // DB에서 받아와서 정보 할당하기
    private fun getFromDB(){
        //카카오로 로그인 시, 마이페이지 프래그먼트에서 이름 띄워주기(무조건 여기(onActivityCreated)에 선언해줘야 오류 안남)
        val nickname = requireView().findViewById<TextView>(R.id.receive_name) // 로그인 버튼
//        val petname = requireView().findViewById<TextView>(R.id.receive_dog_name)
//        val breed = requireView().findViewById<TextView>(R.id.receive_breed)
//        val gender = requireView().findViewById<TextView>(R.id.receive_gender)

        val uid = database.child("users").child(auth.currentUser!!.uid)
        uid.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터가 변경되면 리스너가 감지함
                // 최초(아무값도 없을때)로 실행 됐을때도 감지 됨
                // 유저이름 불러오기
                nickname.text = snapshot.child("username").value.toString()
                // 반려견정보 불러오기 -> 현재 등록된 첫번째 반려견 정보 불러옴, 이후 반려견 추가된다면 변경할 필요O
//                petname.text = snapshot.child("pet_list").child("0").child("pet_name").value.toString()
//                breed.text = snapshot.child("pet_list").child("0").child("breed").value.toString()
//                if(snapshot.child("pet_list").child("0").child("gender").value == 1)
//                    gender.text = "♂"
//                else
//                    gender.text = "♀"
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("DATABASE LOAD ERROR","정보 불러오기 실패")
            }
        })
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