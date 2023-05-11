package com.example.capston


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.databinding.ActivityDogRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_dog_register.*
import java.text.SimpleDateFormat
import java.util.*


class DogRegisterActivity : AppCompatActivity(),BreedItemClick  {

    private final val REQUEST_FIRST = 1010

    var validEditText: Boolean= false
    var validSpinner1: Boolean= false
    var validSpinner2: Boolean= false
    var validSpinner3: Boolean= false
    var validImage: Boolean= false

    lateinit var breed_recycleR: RecyclerView
    lateinit var breedAdapter: BreedAdapter
    lateinit var breed:ArrayList<BreedDTO>
    lateinit var BreedSearch: SearchView

    lateinit var uri: Uri

    private lateinit var auth: FirebaseAuth

    private var sharedPreferences: SharedPreferences? = null

    private var pet_info = PetInfo()

    private lateinit var viewBinding: ActivityDogRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDogRegisterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        BreedSearch = findViewById(R.id.breed_search)
        breed_recycleR = findViewById(R.id.rv_phone_book)

        auth = FirebaseAuth.getInstance()

        // 우측 위 건너뛰기 버튼 누르면 메인으로 넘어가기 = 뒤로가기와 동일
        viewBinding.skipBtn.setOnClickListener {
            onBackPressed()
        }

        viewBinding.dogNameEdtText.setOnTouchListener(OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    breed_recycleR.visibility = View.INVISIBLE
                    breed_search.clearFocus()
                }
            }
            false
        })

        viewBinding.dogNameEdtText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

                validEditText = editable.isNotEmpty()
                checkValid(validEditText, validSpinner1, validSpinner2, validSpinner3, validImage)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                p0?.let { highlightText(it as Editable) }
            }
        })


        viewBinding.nextPageBtn.setOnClickListener {
            val intent = Intent(this, DogRegisterEndActivity::class.java)
//            intent.putExtra("dogname", dog_name_edt_text.text.toString())

            // 이름값 할당
            pet_info.pet_name = viewBinding.dogNameEdtText.text.toString()
//            Log.d("개이름 ", viewBinding.dogNameEdtText.text.toString())
            addDogToDB(pet_info)

            startActivity(intent)
            finish()
        }

        //배경 클릭시 포커스해제
        viewBinding.background.setOnClickListener {
            breed_recycleR.visibility= View.INVISIBLE
            breed_search.clearFocus()
        }


        BreedSearch.setOnQueryTextListener(searchViewTextListener)
        breed = tempPersons()
        setAdapter()
        setupBreedData()

        setupGenderData()
        setupGenderHandler()

        setupAgeData()
        setupAgeHandler()

        initAddImage()

//        limitDropHeight(dog_age_spinner)

    }


    fun setAdapter(){
        //리사이클러뷰에 리사이클러뷰 어댑터 부착
        breed_recycleR.layoutManager = LinearLayoutManager(this)
        breedAdapter = BreedAdapter(this,breed, this)
        breed_recycleR.adapter = breedAdapter
    }

    fun tempPersons(): ArrayList<BreedDTO> {
        val breedArray: Array<String> = resources.getStringArray(R.array.spinner_breed)
        var tempPersons = ArrayList<BreedDTO>()
        tempPersons.add(BreedDTO(""))
        for(i in breedArray.indices){
            tempPersons.add(BreedDTO((breedArray[i])))
        }
        return tempPersons
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
            }
            else{
                Log.d("DB LOAD FAIL","현재 반려견 인덱스 불러오기 실패")
            }
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
                    Toast.makeText(this,"권한을 거부했습니다.",Toast.LENGTH_SHORT).show()
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
                    viewBinding.imageArea.setImageURI(uri)

                    // 갤러리에서 이미지 가져오고 등록하기 버튼 활성화
                    image_upload_btn.isEnabled = true
                    image_upload_btn.isClickable = true

                    // Upload
                    initUploadImage(uri)
                }
            }
        }

    /*
     * 업로드 버튼 클릭 이벤트 설정
     */
    private fun initUploadImage(uri : Uri){
        viewBinding.imageUploadBtn.setOnClickListener{
            // 서버로 업로드
            uploadImageToStorage(uri)
        }
    }

    /*
     * 서버 스토리지로 이미지 업로드
     */
    private fun uploadImageToStorage(uri: Uri) {
        // storage 인스턴스 생성
        val storage = Firebase.storage
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
                    }.addOnFailureListener {
                        Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
            } else {
                Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // 파일 업로드 성공
        uploadTask.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(this, "사진 업로드 성공", Toast.LENGTH_SHORT).show();
            // 성공했으므로 업로드 버튼 비활성화
            image_upload_btn.isEnabled = false
            // 갤러리 불러오기 비활성화
            imageArea.isEnabled = false
            imageArea.isClickable = false

            // validImage 변경 후 다음으로 버튼 활성화 검사
            validImage = true
            checkValid(validEditText, validSpinner1, validSpinner2, validSpinner3, validImage)
        }   // 파일 업로드 실패
            .addOnFailureListener {
            Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
        }
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

    // -- 스피너 높이 조절 코드인데 잘 안되네요 --
//    private fun limitDropHeight(breed_spinner: Spinner) {
//            val popup = Spinner::class.java.getDeclaredField("good")
//            popup.isAccessible = true
//
//            val popupWindow = popup.get(breed_spinner) as ListPopupWindow
//            popupWindow.height = (50 * resources.displayMetrics.density).toInt()
//        }





    // edittext 글자 입력할 때 하이라이트- 마지막 글자만 안바뀜(해결 아직 못함)
//    private fun highlightText(text: Editable) {
//        viewBinding.dogNameEdtText.text?.let { editable ->
//            val spans = editable.getSpans(0, editable.length,
//                ForegroundColorSpan::class.java)
//
//            spans.forEach { span ->
//                editable.removeSpan(span)
//            }
//        }
//
//        val endIndex = text.length
//        val startIndex = if (endIndex < 1) 0 else (endIndex-1)
//
//        viewBinding.dogNameEdtText.text?.setSpan(
//            ForegroundColorSpan(Color.BLACK),
//            startIndex,
//            endIndex,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//
//    }

    //서치뷰 관련 인터렉션
    private fun setupBreedData() {
        val breedSearchView = findViewById<SearchView>(R.id.breed_search)
        breedSearchView.setOnQueryTextFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                breedSearchView.isSelected =  hasFocus
                breedSearchView.isIconified = !hasFocus
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

    private fun setupGenderData() {
        val genderData = resources.getStringArray(R.array.spinner_gender)
        val genderAdapter = object : ArrayAdapter<String>(this,R.layout.gender_spinner) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                if (position == count) {

                    (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).text = ""
                    (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).hint = "성별을 선택해주세요."

                }
                return v
            }
            override fun getCount(): Int {
                return super.getCount() - 1
            }

        }
        genderAdapter.addAll(genderData.toMutableList())
        genderAdapter.add("성별을 선택해주세요.")

        viewBinding.dogGenderSpinner.adapter = genderAdapter

        dog_gender_spinner.setSelection(genderAdapter.count)
        dog_gender_spinner.dropDownVerticalOffset = dipToPixels(50f).toInt()
    }


    private fun setupGenderHandler() {
        viewBinding.dogGenderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when(position) {
                    0 -> {
                        validSpinner2 = true
                        //이거 무슨 코드죠? -> 원래 포지션 0 일 때 성별을 선택해주세요 를 넣어둬서 이걸 선택하면 선택처리 안한다는 코드였습니다.
                    }
                    else -> {
                        validSpinner2 = true
                        Log.d("스피너2", "$validSpinner2")
                    }
                }
                checkValid(validEditText, validSpinner1, validSpinner2, validSpinner3, validImage)
                // 성별 저장
//                Log.d("GENDER", "${position.toString()}")
                if(position==1) // 수
                    pet_info.gender = 1
                else            // 암
                    pet_info.gender = 0
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                validSpinner2 = false
            }
        }
    }


    private fun setupAgeData() {
        val ageData = resources.getStringArray(R.array.spinner_age)
        val ageAdapter = object : ArrayAdapter<String>(this, R.layout.breed_spinner) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                if (position == count) {
                    (v.findViewById<View>(R.id.tvBreedSpinner) as? TextView)?.text = ""
                    (v.findViewById<View>(R.id.tvBreedSpinner) as? TextView)?.hint = getItem(count)
                }
                return v
            }
            override fun getCount(): Int {
                //마지막 아이템은 힌트용으로만 사용하기 때문에 getCount에 1을 빼줍니다.
                return super.getCount() - 1
            }
        }
        ageAdapter.addAll(ageData.toMutableList())
        ageAdapter.add("출생연도를 선택해주세요.")

        viewBinding.dogAgeSpinner.adapter = ageAdapter

        dog_age_spinner.setSelection(ageAdapter.count)
        dog_age_spinner.dropDownVerticalOffset = dipToPixels(50f).toInt()
    }

    private fun setupAgeHandler() {
        viewBinding.dogAgeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                breed_recycleR.visibility= View.INVISIBLE
                breed_search.clearFocus()
                when (position) {
                    0 -> {
                        validSpinner3 = true
                    }
                    else -> {
                        validSpinner3 = true
                        Log.d("스피너3", "$validSpinner3")
                    }
                }
                checkValid(validEditText, validSpinner1, validSpinner2, validSpinner3, validImage)
                // 출생년도 저장
                pet_info.born = dog_age_spinner.selectedItem.toString()
//                Log.d("BORN YEAR", "${pet_info.born}")
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                validSpinner3 = false
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
     * 이미지 업로드 검사 추가 - v5 = validImage
     */
    private fun checkValid(v1:Boolean, v2:Boolean, v3:Boolean, v4:Boolean, v5:Boolean){
        Log.d("Valid", (v1 && v2 && v3 && v4 && v5).toString())
        if(v1 && v2 && v3 && v4 && v5){
            next_page_btn.isEnabled = true
            next_page_btn.isClickable = true

        } else {
            next_page_btn.isEnabled = false
            next_page_btn.isClickable = false
        }
    }

    override fun onClick(value: String?) {
        val breedSearchView = findViewById<SearchView>(R.id.breed_search)
        if (!breedAdapter.choose_breed.isEmpty()) {
            breedSearchView.queryHint = breedAdapter.choose_breed
            val editText =
                findViewById<SearchView>(androidx.appcompat.R.id.search_src_text) as EditText
            editText.setHintTextColor(Color.BLACK)
            breedSearchView.clearFocus() // 포커스 초기화
            viewBinding.dogNameEdtText.clearFocus()
            Log.d("견종이름",breedAdapter.choose_breed)
            //견종 저장
            pet_info.breed = breedAdapter.choose_breed
            breed_recycleR.visibility=View.INVISIBLE
            validSpinner1 = true
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
                    dog_name_edt_text.clearFocus()
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