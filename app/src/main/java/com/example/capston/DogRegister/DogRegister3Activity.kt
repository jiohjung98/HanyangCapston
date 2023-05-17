package com.example.capston.DogRegister

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.*
import com.example.capston.databinding.ActivityDogRegister3Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_dog_register.*
import kotlinx.android.synthetic.main.activity_dog_register2.*
import java.text.SimpleDateFormat
import android.util.Base64
import com.example.capston.data.PetInfo
import java.io.ByteArrayOutputStream
import java.util.*

class DogRegister3Activity : AppCompatActivity(), BreedItemClick  {

    private final val REQUEST_FIRST = 1010

    // checkValid
    var validSpinner: Boolean= false
    var validImage: Boolean= false

    // Breed
    lateinit var breed_recycleR: RecyclerView
    lateinit var breedAdapter: BreedAdapter
    lateinit var breed: ArrayList<BreedDTO>
    lateinit var BreedSearch: SearchView

    private lateinit var uri: Uri

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private var functions : FirebaseFunctions = FirebaseFunctions.getInstance()

    // 공유변수
    private var sharedPreferences: SharedPreferences? = null

    private var pet_info = PetInfo()

    private lateinit var viewBinding: ActivityDogRegister3Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDogRegister3Binding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        BreedSearch = viewBinding.breedSearch
        breed_recycleR = viewBinding.rvPhoneBook

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // 이전 액티비티에서 받은걸 pet_info 에 설정
        pet_info.pet_name = intent.getStringExtra("DogName")
        pet_info.born = intent.getStringExtra("Born")
        pet_info.gender = intent.getIntExtra("Gender",0)

        // 우측 위 건너뛰기 버튼 누르면 메인으로 넘어가기 = 뒤로가기와 동일
        viewBinding.skipBtn.setOnClickListener {
            onBackPressed()
        }

        viewBinding.background.setOnClickListener {
            breed_recycleR.visibility= View.INVISIBLE
            BreedSearch.clearFocus()
        }

        viewBinding.nextBtn.setOnClickListener {
            uploadImageToStorage()
        }

        viewBinding.backButton.setOnClickListener {
            super.onBackPressed()
        }

        BreedSearch.setOnQueryTextListener(searchViewTextListener)
        breed = tempPersons()
        setAdapter()
        setupBreedData()

        initAddImage()
    }

    /*
     * 이후 반려견 추가등록하는 기능 넣으려면 수정할 필요 있음
     * 현재 pet_list 배열을 새로 만들어서 저장하므로 추가 등록 시 기존 값 지워지고 새로운 배열 등록되는게 문제가 될듯
     */
    private fun addDogToDB(pet_info : PetInfo){
        val database: DatabaseReference =
            Firebase.database.reference.child("users").child(auth.currentUser!!.uid)

        var next_pet_num : Int? = null

        // 로컬에 저장된 현재 반려견 인덱스 접근
        sharedPreferences = getSharedPreferences("CUR_PET", MODE_PRIVATE);

        // 현재 반려견 등록수 확인
        database.child("pet_cnt").get().addOnSuccessListener{ task ->
            if (task.value != null){
                // 새로 등록할 반려견의 인덱스 = 현재 등록수+1
                next_pet_num = Integer.parseInt(task.value.toString()).plus(1)
                // 로컬 현재 반려견 인덱스 변경
                sharedPreferences?.edit()?.putInt("cur_pet", next_pet_num!!)?.apply()
                // db의 현재 반려견 수 변경
                database.child("pet_cnt").setValue(next_pet_num)
                // db에 새 반려견 등록
                database.child("pet_list").child(next_pet_num.toString()).setValue(pet_info)
                    .addOnSuccessListener {
                        val intent = Intent(this, DogRegisterEndActivity::class.java)
                        startActivity(intent)
                        finish()
                }
            }
            else{
                Log.d("DB LOAD FAIL","현재 반려견 인덱스 불러오기 실패")
            }
        }
    }

    private fun setAdapter(){
        //리사이클러뷰에 리사이클러뷰 어댑터 부착
        breed_recycleR.layoutManager = LinearLayoutManager(this)
        breedAdapter = BreedAdapter(this,breed, this)
        breed_recycleR.adapter = breedAdapter
    }

    private fun tempPersons(): ArrayList<BreedDTO> {
        val breedArray: Array<String> = resources.getStringArray(R.array.spinner_breed)
        var tempPersons = ArrayList<BreedDTO>()
        for(i in breedArray.indices){
            tempPersons.add(BreedDTO((breedArray[i])))
        }
        return tempPersons
    }

    //서치뷰 관련 인터렉션
    private fun setupBreedData() {
        BreedSearch.setOnQueryTextFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                BreedSearch.isSelected =  hasFocus
                BreedSearch.isIconified = !hasFocus
                if(breed_search.isSelected) {
                    breed_recycleR.visibility = View.VISIBLE
                }else if(!breed_search.isSelected){
                    breed_recycleR.visibility = View.INVISIBLE
                }
            }

        })
    }

    var searchViewTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            //텍스트 입력/수정시에 호출
            override fun onQueryTextChange(s: String): Boolean {
                breedAdapter.filter.filter(s)
                Log.d("gd", "SearchVies Text is changed : $s")
                return false
            }
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
                    Toast.makeText(this,"권한을 거부했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else -> {
            //Do Nothing
        }
        }
    }

    private fun initAddImage() {
        viewBinding.imageArea.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                -> {
                    // 권한이 존재하는 경우
                    // TODO 이미지를 가져옴
                    getImageFromAlbum()

                    click_upload_text.setVisibility(View.INVISIBLE);
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
                    this.uri = result.data?.data!!
                    // 이미지를 ImageView에 표시한다.
                    viewBinding.imageArea.setImageURI(this.uri)

                    // 갤러리에서 이미지 가져오고 등록하기 버튼 활성화
//                    image_upload_btn.isEnabled = true
//                    image_upload_btn.isClickable = true

                    // 다음버튼 활성화
//                    initUploadImage(uri)
                    validImage = true
                    checkValid(validSpinner, validImage)
                    predictBreed(encodeImageToBase64(this.uri))
                }
            }
        }

    // 이미지 데이터를 Base64로 인코딩하는 함수
    private fun encodeImageToBase64(imageUri: Uri): String {
        val inputStream = contentResolver.openInputStream(imageUri)
        val buffer = ByteArray(8192)
        val output = ByteArrayOutputStream()
        var bytesRead: Int
        while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
            output.write(buffer, 0, bytesRead)
        }
        val imageBytes = output.toByteArray()
        Log.d("encodeImageToBase64", Base64.encodeToString(imageBytes, Base64.DEFAULT))
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }


    override fun onClick(value: String?) {
        if (!breedAdapter.choose_breed.isEmpty()) {
            BreedSearch.queryHint = breedAdapter.choose_breed
            val editText =
                findViewById<SearchView>(androidx.appcompat.R.id.search_src_text) as EditText
            editText.setHintTextColor(Color.BLACK)
            BreedSearch.clearFocus()// 포커스 초기화
            // 견종 저장
            pet_info.breed = breedAdapter.choose_breed
            breed_recycleR.visibility=View.INVISIBLE
            validSpinner = true
            checkValid(validSpinner, validImage)
        }
    }

    private fun predictBreed(base64uri : String){
        val data = hashMapOf(
            "image" to base64uri,
        )

        Log.d("predictBreed", data.toString())

        functions.getHttpsCallable("sendBase64ToServer")
            .call(data)
            .addOnSuccessListener { task->
                val result = task.data as? Map<*, *>
                Log.d("인공지능 결과",result?.get("breed").toString())
                setBreed(result?.get("breed").toString())
            }
            .addOnFailureListener {
                Log.d("인공지능 결과","FAIL")
            }
    }

    private fun setBreed(result : String){
        breedAdapter.setBreed(result)
        onClick(breedAdapter.choose_breed).also {
            Log.d("setBreed", pet_info.breed.toString())
        }
        viewBinding.afterPredict.visibility = View.VISIBLE
    }

    /*
     * 서버 스토리지로 이미지 업로드
     */
    private fun uploadImageToStorage() {
        // storage 참조
        val storageRef = storage.getReference("images").child("users").child(auth.currentUser!!.uid)
        // storage에 저장할 파일명 선언
        val fileName = auth.currentUser!!.uid + "_" + SimpleDateFormat("yyyyMMddHHmm").format(Date())
        val mountainsRef = storageRef.child("${fileName}.jpg")

        val uploadTask = mountainsRef.putFile(uri).addOnCompleteListener {
            if (it.isSuccessful) {
                // 파일 업로드에 성공했기 때문에 스토리지 url을 다시 받아와 DB에 저장
                mountainsRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        pet_info.image_url = uri.toString()
                        // DB 등록
                        addDogToDB(pet_info)
                    }.addOnFailureListener {
                        Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
            } else {
                Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // 파일 업로드 성공
//        uploadTask.addOnSuccessListener { taskSnapshot ->
//            Toast.makeText(this, "사진 업로드 성공", Toast.LENGTH_SHORT).show();
//            // 성공했으므로 업로드 버튼 비활성화
//            image_upload_btn.isEnabled = false
//            // 갤러리 불러오기 비활성화
//            imageArea.isEnabled = false
//            imageArea.isClickable = false
//
//            // validImage 변경 후 다음으로 버튼 활성화 검사
//            validImage = true
//            checkValid(validSpinner3, validImage)
//        }   // 파일 업로드 실패
    }

    /*
     * 저장소 접근 권한 설정 팝업
     */
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
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
    * 이미지 업로드 검사 추가 - v5 = validImage
    */
    private fun checkValid(v1:Boolean, v2:Boolean){
        Log.d("Valid", (v1 && v2).toString())
        if(v1 && v2){
            next_btn.isEnabled = true
            next_btn.isClickable = true

        } else {
            next_btn.isEnabled = false
            next_btn.isClickable = false
        }
    }

    // 화면 클릭하여 키보드 숨기기 및 포커스 제거
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action === MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    // 뒤로가기 -> 건너뛰기
    override fun onBackPressed() {
        val skipDialog = SkipDialog(this)
        skipDialog.setOnOKClickedListener { content ->
        }
        skipDialog.show("초기화면")
    }
}