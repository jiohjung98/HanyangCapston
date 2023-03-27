package com.example.capston

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

/*
 *  두번째 메뉴, 산책하기 -> 다이얼로그 -> 산책지도
 */
class NaviWalkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentNaviWalkBinding

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
        // Inflate the layout for this fragment
        binding = FragmentNaviWalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.walkBtn.setOnClickListener {
            onClickWalk(view)
        }
        getImageFromStore()

        val petname = requireView().findViewById<TextView>(R.id.walk_name)
        val breed = requireView().findViewById<TextView>(R.id.walk_breed)
        val gender = requireView().findViewById<TextView>(R.id.walk_gender)
        val age = requireView().findViewById<TextView>(R.id.walk_age)

        // DB에서 받아와서 정보 할당하기
        val uid = database.child("users").child(auth.currentUser!!.uid).child("pet_list").child("0")
        uid.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터가 변경되면 리스너가 감지함
                // 최초(아무값도 없을때)로 실행 됐을때도 감지 됨
                // 반려견정보 불러오기 -> 현재 등록된 첫번째 반려견 정보 불러옴, 이후 반려견 추가된다면 변경할 필요O
                petname.text = snapshot.child("pet_name").value.toString()
                breed.text = snapshot.child("breed").value.toString()
                age.text = snapshot.child("born").value.toString() + "년생"
                if(snapshot.child("gender").value == 1)
                    gender.text = "♂"
                else
                    gender.text = "♀"
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // fragment 액션바 보여주기(선언안해주면 다른 프레그먼트에서 선언한 .hide() 때문인지 모든 프레그먼트에서 액션바 안보임
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    private fun onClickWalk(view: View?) {
        val walkDlg = WalkDialog(mainActivity)
        walkDlg.setOnOKClickedListener { content ->
            binding.walkBtn.text = content
        }
        walkDlg.show("산책")
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


}