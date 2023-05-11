package com.example.capston

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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

    // DB에서 불러올 정보들
    private lateinit var DBpet : DatabaseReference
    private var _cur_pet_num : String? = null
    private val cur_pet_num get() = _cur_pet_num!!

    private lateinit var petname : TextView
    private lateinit var breed : TextView
    private lateinit var gender : TextView
    private lateinit var age : TextView

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
    /*
    현재 반려견 인덱스 불러오기
    */
    private fun loadCurrentDog(flag : Int){
        // 현재 반려견 인덱스 불러오기
        // 로컬에 해당 key 없으면 1 -> 아직 등록안했거나, 등록한건 있는데 앱 데이터 초기화
        // 등록안해서 1이면 아래서 null -> invalid() 들어감
        var cur_pet = mainActivity.sharedPreferences?.getInt("cur_pet",1)

//        Log.d("loadCurrentDog 반려견 인덱스",cur_pet.toString())

        DBpet = database.child("users").child(auth.currentUser!!.uid).child("pet_list").child(cur_pet.toString())
        DBpet.get().addOnSuccessListener { snapshot ->
//            Log.d("리스너","DBPet addOnSuccessListener listener called")
            if (snapshot.value == null){
//                    Log.d("snapshot", "null")
                invalidDog()
            }
            // 등록된 반려견 있음
            else {
                validDog(snapshot)
                setupMyDogHandler()
            }
        }

    }

    private fun validDog(snapshot: DataSnapshot) {
        Log.d("등록 있음", "${snapshot}")

        getImageFromStore(snapshot)


        // 데이터가 변경되면 리스너가 감지함
        // 최초(아무값도 없을때)로 실행 됐을때도 감지 됨
        // 반려견정보 불러오기 -> 현재 등록된 첫번째 반려견 정보 불러옴, 이후 반려견 추가된다면 변경할 필요O
        petname.text = snapshot.child("pet_name").value.toString()
        breed.text = snapshot.child("breed").value.toString()
        age.text = snapshot.child("born").value.toString() + "년생"
        if (snapshot.child("gender").value == 1)
            gender.text = "♂"
        else
            gender.text = "♀"
    }

    private fun invalidDog(){
        Log.d("등록 없음","")
        binding.walkAgeSlash.visibility = View.INVISIBLE
        binding.walkBreedSlash.visibility = View.INVISIBLE
        petname.text = "등록된 반려견이 없습니다"
    }

    /*
     * 갤러리에서 이미지 가져와서 image area에 띄움
     */
    private fun getImageFromStore(snapshot: DataSnapshot) {
        // database.child("users").child(auth.currentUser!!.uid).child("pet_list").child(cur_pet_num)
        val url = snapshot.child("image_url").value.toString()
        if (isAdded()) {
//            Log.d("IMAGE URL",url)
            GlideApp.with(this@NaviMypageFragment).load(Uri.parse(url))
                .into(binding.profile)
        }
    }

    private fun setupMyDogHandler() {
        val uid = database.child("users").child(auth.currentUser!!.uid)
//        binding.petSelectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
////                Log.d("onItemSelected listener called position",position.toString())
//
//                // NaviWalk 진입 때마다 setupMyDogList에서 꼭 한번씩 호출됨
//
//                // 반려견 사진, 정보 reload
//                database.child("users").child(auth.currentUser!!.uid).child("pet_list").child(position.plus(1).toString())
//                    .get().addOnSuccessListener { snapshot ->
//                        validDog(snapshot)
//                    }
//
//                mainActivity.sharedPreferences?.edit()?.putInt("cur_pet", position.plus(1))?.apply()
////                Log.d("변경된 반려견 인덱스", mainActivity.sharedPreferences?.getInt("cur_pet", -1).toString())
//
//            }
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//            }
//        }
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
//        (activity as AppCompatActivity).supportActionBar?.hide()

        // 현재 반려견 인덱스 불러오기
        loadCurrentDog(0)

        petname = binding.walkName
        breed = binding.walkBreed
        gender = binding.walkGender
        age = binding.walkAge
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