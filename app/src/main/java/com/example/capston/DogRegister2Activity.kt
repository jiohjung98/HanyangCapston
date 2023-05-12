package com.example.capston

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.databinding.ActivityDogRegister2Binding
import com.example.capston.databinding.ActivityDogRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dog_register.*
import kotlinx.android.synthetic.main.activity_dog_register.breed_search
import kotlinx.android.synthetic.main.activity_dog_register2.*
import java.util.ArrayList

class DogRegister2Activity : AppCompatActivity(), BreedItemClick {

    private final val REQUEST_FIRST = 1010

    var validSpinner1: Boolean= false
    var validButton1: Boolean = false
    var validButton2: Boolean = false

    lateinit var breed_recycleR: RecyclerView
    lateinit var breedAdapter: BreedAdapter
    lateinit var breed: ArrayList<BreedDTO>
    lateinit var BreedSearch: SearchView

    lateinit var uri: Uri

    private lateinit var auth: FirebaseAuth

    private var sharedPreferences: SharedPreferences? = null

    private var pet_info = PetInfo()

    private lateinit var viewBinding: ActivityDogRegister2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDogRegister2Binding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        BreedSearch = findViewById(R.id.breed_search)
        breed_recycleR = findViewById(R.id.rv_phone_book)

        auth = FirebaseAuth.getInstance()

        // 우측 위 건너뛰기 버튼 누르면 메인으로 넘어가기 = 뒤로가기와 동일
        viewBinding.skipBtn.setOnClickListener {
            onBackPressed()
        }

        //배경 클릭시 포커스해제
        viewBinding.background.setOnClickListener {
            breed_recycleR.visibility= View.INVISIBLE
            breed_search.clearFocus()
        }

        // 성별 버튼
        viewBinding.genderBoy.setOnClickListener{
            validButton1 = true
            checkValid(validSpinner1, validButton1, validButton2)
        }

        viewBinding.genderGirl.setOnClickListener{
            validButton2 = true
            checkValid(validSpinner1, validButton1, validButton2)
        }



        BreedSearch.setOnQueryTextListener(searchViewTextListener)
        breed = tempPersons()
        setAdapter()
        setupBreedData()

//        setupGenderData()
//        setupGenderHandler()

//        setupAgeData()
//        setupAgeHandler()

//        initAddImage()
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

//    * 이후 반려견 추가등록하는 기능 넣으려면 수정할 필요 있음
//    * 현재 pet_list 배열을 새로 만들어서 저장하므로 추가 등록 시 기존 값 지워지고 새로운 배열 등록되는게 문제가 될듯
//    */
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

    override fun onClick(value: String?) {
        val breedSearchView = findViewById<SearchView>(R.id.breed_search)
        if (!breedAdapter.choose_breed.isEmpty()) {
            breedSearchView.queryHint = breedAdapter.choose_breed
            val editText =
                findViewById<SearchView>(androidx.appcompat.R.id.search_src_text) as EditText
            editText.setHintTextColor(Color.BLACK)
            breedSearchView.clearFocus() // 포커스 초기화
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
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun checkValid(v1:Boolean, v2:Boolean, v3:Boolean){
        Log.d("Valid", (v1 && v2 && v3).toString())
        if(v1 && v2){
            next_btn.isEnabled = true
            next_btn.isClickable = true
        }
        else if (v1 && v3){
            next_btn.isEnabled = true
            next_btn.isClickable = true
        }
        else {
            next_btn.isEnabled = false
            next_btn.isClickable = false
        }
    }

    // 뒤로가기 -> 건너뛰기
    override fun onBackPressed() {
        val skipDialog = SkipDialog(this)
        skipDialog.setOnOKClickedListener { content ->
        }
        skipDialog.show("초기화면")
    }
}