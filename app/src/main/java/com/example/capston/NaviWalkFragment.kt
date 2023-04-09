package com.example.capston

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_dog_register.*
import kotlinx.android.synthetic.main.fragment_navi_walk.*
import java.text.SimpleDateFormat
import java.util.*

/*
 *  두번째 메뉴, 산책하기
 */
@Suppress("DEPRECATION")
class NaviWalkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val binding get() = _binding!!
    private var _binding: FragmentNaviWalkBinding? = null

    private final val REQUEST_FIRST = 1010
    var pet_info = PetInfo()
    lateinit var uri: Uri

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

        if (binding == null) {
            return
        }
        binding.walkBtn.setOnClickListener {
            onClickWalk(view)
        }

        binding.registerBtn.setOnClickListener {
            onClickRegister(view)
        }

        getImageFromStore()

        initAddImage()

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


    private fun validDog(snapshot: DataSnapshot) {
        Log.d("등록 있음", "${snapshot}")
            binding.walkBtn.isEnabled = true
            binding!!.walkBtn.visibility = View.VISIBLE

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
                if (MainActivity() == null) {
                    return;
                }
                Log.d("IMAGE URL","${Uri.parse(snapshot.value.toString())}")
                if (isAdded()) {
                    GlideApp.with(this@NaviWalkFragment).load(Uri.parse(snapshot.value.toString()))
                        .into(binding.profile)
                }
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

    /*
    * 갤러리 접근 권한 획득 후 작업
    */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1010 -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // 권한 허가
                    // 허용 클릭 시 갤러리에서 이미지 가져오기
                    getImageFromAlbum()
                } else {
                    // 거부 클릭시
                    Toast.makeText(mainActivity,"권한을 거부했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else -> {
            //Do Nothing
        }
        }
    }

    private fun initAddImage() {
        binding.camera.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(mainActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                -> {
                    // 권한이 존재하는 경우
                    // TODO 이미지를 가져옴
                    getImageFromAlbum()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    // 권한이 거부 되어 있는 경우
                    showPermissionContextPopup()
                }
                else -> {
                    // 처음 권한을 시도했을 때 띄움
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),REQUEST_FIRST)
                }
            }
        }
    }

    /*
     * 갤러리에서 이미지 가져와서 image area에 띄움
     */
    private fun getImageFromAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        getImageActivity.launch(intent)
    }
    private val getImageActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    // 전달 받은 이미지 uri를 넣어준다.
                    uri = result.data?.data!!
                    // 이미지를 ImageView에 표시한다.
                    binding.profile.setImageURI(uri)
                    // Upload
                    initUploadImage(uri)
                }
            }
        }

    /*
     * 저장소 접근 권한 설정 팝업
     */
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(mainActivity)
            .setTitle("권한이 필요합니다")
            .setMessage("갤러리에서 사진을 선택하려면 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_FIRST)
            }
            .setNegativeButton("취소하기",{ _,_ ->})
            .create()
            .show()
    }

    /*
     * 업로드 버튼 클릭 이벤트 설정
     */
    private fun initUploadImage(uri : Uri){
            // 서버로 업로드
            uploadImageToStorage(uri)
    }

    /*
     * 서버 스토리지로 이미지 업로드
     */
    private fun uploadImageToStorage(uri: Uri) {
        // storage 인스턴스 생성
        val storage = Firebase.storage
        // storage 참조
        val storageRef = storage.getReference("images").child("users")
        // storage에 저장할 파일명 선언
        val fileName = auth.currentUser!!.uid + "_" + SimpleDateFormat("yyyyMMddHHmm").format(Date())
        val mountainsRef = storageRef.child("${fileName}.jpg")


        val uploadTask = mountainsRef.putFile(uri).addOnCompleteListener {
            if (it.isSuccessful) {
                // 파일 업로드에 성공했기 때문에 스토리지 url을 다시 받아와 DB에 저장
                mountainsRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        pet_info.image_url = uri.toString()
                    }.addOnFailureListener {
                        Toast.makeText(mainActivity, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
            } else {
                Toast.makeText(mainActivity, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // 파일 업로드 성공
        uploadTask.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(mainActivity, "사진 업로드 성공", Toast.LENGTH_SHORT).show();

        }   // 파일 업로드 실패
            .addOnFailureListener {
                Toast.makeText(mainActivity, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
            }
    }
}