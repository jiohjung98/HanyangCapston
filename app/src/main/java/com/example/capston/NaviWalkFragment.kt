package com.example.capston

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.FragmentNaviWalkBinding
import com.example.capston.homepackage.WalkDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_navi_walk.*

/*
 *  두번째 메뉴, 산책하기
 */
class NaviWalkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentNaviWalkBinding?= null
    private val binding get() = _binding!!

    // 1. Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    private lateinit var mainActivity: MainActivity
    private lateinit var database : DatabaseReference
    private lateinit var auth : FirebaseAuth

    private lateinit var petname : TextView
    private lateinit var breed : TextView
    private lateinit var gender : TextView
    private lateinit var age : TextView

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
        // Inflate the layout for this fragment
        _binding = FragmentNaviWalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.walkBtn.setOnClickListener {
            onClickWalk(view)
        }

        binding.registerBtn.setOnClickListener {
            onClickRegister(view)
        }

        getImageFromStore()

        petname = requireView().findViewById<TextView>(R.id.walk_name)
        breed = requireView().findViewById<TextView>(R.id.walk_breed)
        gender = requireView().findViewById<TextView>(R.id.walk_gender)
        age = requireView().findViewById<TextView>(R.id.walk_age)

        // DB에서 받아와서 정보 할당하기
        val uid = database.child("users").child(auth.currentUser!!.uid).child("pet_list").child("0")
        uid.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 등록된 반려견없음 (건너뛰기 등)
                if(snapshot.value == null)
                    invalidDog()
                // 등록된 반려견 있음
                else
                    validDog(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("DATABASE LOAD ERROR","정보 불러오기 실패")
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NaviWalkFragment().apply {
            }
    }

    private fun validDog(snapshot: DataSnapshot){
        Log.d("등록 있음","${snapshot}")
        binding.walkBtn.isEnabled = true
        binding.walkBtn.visibility = View.VISIBLE
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
//        Log.d("등록 없음","${snapshot}")
        binding.registerBtn.visibility = View.VISIBLE
        binding.walkAgeSlash.visibility = View.INVISIBLE
        binding.walkBreedSlash.visibility = View.INVISIBLE
        petname.text = "등록된 반려견이 없습니다"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // fragment 액션바 보여주기(선언안해주면 다른 프레그먼트에서 선언한 .hide() 때문인지 모든 프레그먼트에서 액션바 안보임
        (activity as AppCompatActivity).supportActionBar?.show()
        setupData()
        setupStatusHandler()
    }

    //메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickWalk(view: View?) {
        val walkDlg = WalkDialog(mainActivity)
        walkDlg.setOnOKClickedListener { content ->
            binding.walkBtn.text = content
        }
        walkDlg.show("산책")
    }

    private fun onClickRegister(view: View?) {
        mainActivity.startActivity(Intent(mainActivity,DogRegisterActivity::class.java))
        mainActivity.finish()
    }

    /*
     * 갤러리에서 이미지 가져와서 image area에 띄움
     */
    private fun getImageFromStore() {
        val url = database.child("users").child(auth.currentUser!!.uid).child("pet_list").child("0").child("image_url")
        url.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("IMAGE URL","${Uri.parse(snapshot.value.toString())}")
                GlideApp.with(this@NaviWalkFragment).load(Uri.parse(snapshot.value.toString())).into(binding.profile)
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    private fun setupData() {

        val statusData = resources.getStringArray(R.array.spinner_ddong)

        val statusAdapter = object : ArrayAdapter<String>(requireContext(),R.layout.gender_spinner) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

                val v = super.getView(position, convertView, parent)
                var tv = v as TextView
                tv.setTextSize(/* size = */ 12f)
                if (position == count) {

                    (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).text = ""
//                    (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).hint = " 선택"

                }
                return v
            }

            override fun getCount(): Int {
                return super.getCount() - 1
            }

        }

        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        statusAdapter.addAll(statusData.toMutableList())
        statusAdapter.add(" 선택")

        _binding!!.dogDdongSpinner.adapter = statusAdapter

        dog_ddong_spinner.setSelection(statusAdapter.count)
        dog_ddong_spinner.dropDownVerticalOffset = dipToPixels(15f).toInt()
    }


    private fun setupStatusHandler() {
        _binding?.dogDdongSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when(position) {
                    0 -> {

                    }
                    else -> {

                    }
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }


    private fun dipToPixels(dipValue: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dipValue,
            resources.displayMetrics
        )
    }
}