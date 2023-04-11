package com.example.capston

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.databinding.LostDogInfoBinding
import java.text.SimpleDateFormat
import java.util.*


class DogInfoEnterDialog(private val activity: MainActivity) : BreedItemClick  {

    private final val REQUEST_FIRST = 1010

    private lateinit var listener : MyDialogOKClickedListener
    private var _binding : LostDogInfoBinding? = null
    private val binding get() = _binding!!

    private var _dlg : Dialog? = null   //부모 액티비티의 context 가 들어감
    private val dlg get() = _dlg!!

    // 확인버튼 활성화 확인
    private var validName: Boolean= false
    private var validBreed: Boolean= false
    private var validTime: Boolean= false
    private var validGender : Boolean = false
    private var validBorn : Boolean = false
    private var validContent: Boolean= false
    private var validImage: Boolean= false

    // 견종선택 리사이클러뷰
    private lateinit var breed_recycleR: RecyclerView
    private lateinit var breedAdapter: BreedAdapter
    private lateinit var breed:ArrayList<BreedDTO>
    private lateinit var BreedSearch: SearchView

    private var _uri : Uri? = null
    private val uri get() = _uri!!

    private lateinit var pet_info : PetInfo
    private lateinit var post : UserPost

    fun initialize(){
        _dlg = Dialog(activity)
        _uri = null

        validName = false
        validBreed = false
        validTime = false
        validGender = false
        validBorn = false
        validContent = false
        validImage = false

        // 포스트 데이터 초기화
        pet_info = PetInfo()
        post = UserPost()

    }

    // 갤러리 불러오기 이후 수행할 동작 설정
    fun setLauncher() {
        activity.launcher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    _uri = result.data?.data
                    setImageArea(_uri)
                }
            }
        }
    }

    fun show(content : String) {
        initialize()
        _binding = LostDogInfoBinding.inflate(activity.layoutInflater)

        // 다이얼로그 테두리 둥글게 만들기
        dlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dlg.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dlg.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        dlg.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        BreedSearch = binding.breedInput
        breed_recycleR = binding.breedRecycle

        val params: WindowManager.LayoutParams = dlg.window!!.attributes
        params.y = 500
        dlg.window!!.attributes = params


        // ↓↓↓ 버튼 및 텍스트리스너 설정

        binding.nameInput.setOnTouchListener(View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    breed_recycleR.visibility = View.INVISIBLE
                    BreedSearch.clearFocus()
                }
            }
            false
        })

        // 이름 텍스트 리스너
        binding.nameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                // 이름값 할당
                pet_info.pet_name = binding.nameInput.text.toString()
                validName = editable.isNotEmpty()
                checkValid(validName, validBreed, validTime,  validGender, validBorn, validContent, validImage)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        // 시간 텍스트 리스너
        binding.timeInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                validTime = true
                checkValid(validName, validBreed, validTime,  validGender, validBorn, validContent, validImage)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        // 상세내용 텍스트 리스너
        binding.contentInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                validContent = true
                checkValid(validName, validBreed, validTime,  validGender, validBorn, validContent, validImage)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })


        // 확인 버튼 동작
        binding.yesBtn.setOnClickListener {
            uploadImageToStorage(uri)
            dlg.dismiss()
        }

        //cancel 버튼 동작
        binding.noBtn.setOnClickListener {
            _binding = null
            dlg.dismiss()
        }

        BreedSearch.setOnQueryTextListener(searchViewTextListener)
        breed = loadDogList()
        setAdapter()
        setupBreedData()

        setupGenderData()
        setupGenderHandler()

        setupAgeData()
        setupAgeHandler()

        initAddImage()

        dlg.show()

    }

    // 리사이클러뷰 어댑터
    fun setAdapter(){
        //리사이클러뷰에 리사이클러뷰 어댑터 부착
        breed_recycleR.layoutManager = LinearLayoutManager(activity)
        breedAdapter = BreedAdapter(this,breed, activity)
        breed_recycleR.adapter = breedAdapter
    }

    // 개 품종 불러오기
    fun loadDogList(): ArrayList<BreedDTO> {
        val breedArray: Array<String> = activity.resources.getStringArray(R.array.spinner_breed)
        var dogList = ArrayList<BreedDTO>()
        dogList.add(BreedDTO(""))
        for(i in breedArray.indices){
            dogList.add(BreedDTO((breedArray[i])))
        }
        return dogList
    }

    //서치뷰 관련 인터렉션
    private fun setupBreedData() {
        BreedSearch.setOnQueryTextFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                BreedSearch.isSelected =  hasFocus
                BreedSearch.isIconified = !hasFocus
                if(BreedSearch.isSelected) {
                    breed_recycleR.visibility = View.VISIBLE
                }else if(!BreedSearch.isSelected){
                    breed_recycleR.visibility = View.INVISIBLE

                }
            }
        })
    }

    // 품종 어댑터 - 품종 선택 시 설정
    override fun onClick(value: String?) {
        if (!breedAdapter.choose_breed.isEmpty()) {
            BreedSearch.queryHint = breedAdapter.choose_breed
            // 선택 후 글자 색상 변경
            val editText = binding.breedInput.findViewById<SearchView>(androidx.appcompat.R.id.search_src_text) as EditText
            editText.setHintTextColor(Color.BLACK)
            // 포커스 초기화
            BreedSearch.clearFocus()
            binding.nameInput.clearFocus()
            Log.d("견종이름",breedAdapter.choose_breed)
            // 견종 저장
            pet_info.breed = breedAdapter.choose_breed
            breed_recycleR.visibility=View.INVISIBLE
            // 확인버튼 체크
            validBreed = true
        }
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

    // 성별 데이터
    private fun setupGenderData() {
        val genderData = activity.resources.getStringArray(R.array.spinner_gender)
        val genderAdapter = object : ArrayAdapter<String>(activity,R.layout.gender_spinner) {

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

        binding.genderSpinner.adapter = genderAdapter

        binding.genderSpinner.setSelection(genderAdapter.count)
        binding.genderSpinner.dropDownVerticalOffset = dipToPixels(50f).toInt()
    }


    private fun setupGenderHandler() {
        binding.genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when(position) {
                    0 -> {
                        validGender = true
                        //이거 무슨 코드죠? -> 원래 포지션 0 일 때 성별을 선택해주세요 를 넣어둬서 이걸 선택하면 선택처리 안한다는 코드였습니다.
                    }
                    else -> {
                        validGender = true
                        Log.d("스피너2", "$validGender")
                    }
                }
                checkValid(validName, validBreed, validTime, validGender, validBorn, validContent, validImage)
                // 성별 저장
//                Log.d("GENDER", "${position.toString()}")
                if(position==1) // 수
                    pet_info.gender = 1
                else            // 암
                    pet_info.gender = 0
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                validGender = false
            }
        }
    }

    private fun setupAgeData() {
        val ageData = activity.resources.getStringArray(R.array.spinner_age)
        val ageAdapter = object : ArrayAdapter<String>(activity, R.layout.breed_spinner) {

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

        binding.bornSpinner.adapter = ageAdapter

        binding.bornSpinner.setSelection(ageAdapter.count)
        binding.bornSpinner.dropDownVerticalOffset = dipToPixels(50f).toInt()
    }

    private fun setupAgeHandler() {
        binding.bornSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                breed_recycleR.visibility= View.INVISIBLE
                BreedSearch.clearFocus()
                when (position) {
                    0 -> {
                        validBorn = true
                    }
                    else -> {
                        validBorn = true
                        Log.d("스피너3", "$validBorn")
                    }
                }
                checkValid(validName, validBreed, validTime, validGender, validBorn, validContent, validImage)
                // 출생년도 저장
                pet_info.born = binding.bornSpinner.selectedItem.toString()
//                Log.d("BORN YEAR", "${pet_info.born}")
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                validBorn = false
            }
        }
    }

    private fun dipToPixels(dipValue: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dipValue,
            activity.resources.displayMetrics
        )
    }


    fun setOnOKClickedListener(listener: (String) -> Unit) {
        this.listener = object: DogInfoEnterDialog.MyDialogOKClickedListener {
            override fun onOKClicked(content: String) {
                listener(content)
            }
        }
    }

    interface MyDialogOKClickedListener {
        fun onOKClicked(content : String)
    }


    private fun initAddImage() {
        binding.lostImageArea.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                -> {
                    // 권한이 존재하는 경우
                    // TODO 이미지를 가져옴
                    getImageFromAlbum()
                }
                activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    // 권한이 거부 되어 있는 경우
                    showPermissionContextPopup()
                }
                else -> {
                    // 처음 권한을 시도했을 때 띄움
                    activity.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),REQUEST_FIRST)
                }
            }
        }
    }

    /*
    * 저장소 접근 권한 설정 팝업
    */
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(activity)
            .setTitle("권한이 필요합니다")
            .setMessage("갤러리에서 사진을 선택하려면 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                activity.requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_FIRST)
            }
            .setNegativeButton("취소하기",{ _,_ ->})
            .create()
            .show()
    }

    // 갤러리에서 가져온 이미지 보여주기
    private fun setImageArea(uri : Uri?){
        binding.lostImageArea.setImageURI(uri)
        binding.clickUploadText.visibility = View.INVISIBLE
        // 이미지 불러오기 성공했으므로
        validImage = true
        checkValid(validName, validBreed, validTime, validGender, validBorn, validContent, validImage)
    }

    /*
     * 갤러리에서 이미지 가져와서 image area에 띄움
     */
    private fun getImageFromAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        activity.getLauncher()!!.launch(intent)
    }


    /*
     * 서버 스토리지로 이미지 업로드
     */
    private fun uploadImageToStorage(uri: Uri) {
        // storage 참조
        val storageRef = activity.storage.getReference("images").child("lost")
        // storage에 저장할 파일명 선언
        val fileName = activity.auth.currentUser!!.uid + "_" + SimpleDateFormat("yyyyMMddHHmm").format(Date())
        val mountainsRef = storageRef.child("${fileName}.jpg")
        val uploadTask = mountainsRef.putFile(uri).addOnCompleteListener {
            if (it.isSuccessful) {
                // 파일 업로드에 성공했기 때문에 스토리지 url을 다시 받아와 DB에 저장
                mountainsRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        pet_info.image_url = uri.toString()
                    }.addOnFailureListener {
                        Toast.makeText(activity, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
            } else {
                Toast.makeText(activity, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // 파일 업로드 성공
        uploadTask.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(activity, "사진 업로드 성공", Toast.LENGTH_SHORT).show();
            // 갤러리 불러오기 비활성화
            binding.lostImageArea.isEnabled = false
            binding.lostImageArea.isClickable = false

        }   // 파일 업로드 실패
            .addOnFailureListener {
                Toast.makeText(activity, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
            }
    }

    /*
     * 확인버튼 검사
     */
    private fun checkValid(v1:Boolean, v2:Boolean, v3:Boolean, v4:Boolean, v5:Boolean, v6:Boolean, v7:Boolean){
        Log.d("Valid", (v1 && v2 && v3 && v4 && v5 && v6 && v7).toString())
        if(v1 && v2 && v3 && v4 && v5 && v6 && v7){
            binding.yesBtn.isEnabled = true
            binding.yesBtn.isClickable = true
        } else {
            binding.yesBtn.isEnabled = false
            binding.yesBtn.isClickable = false
        }
    }
}