package com.example.capston.DogRegister

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
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
import com.example.capston.*
import com.example.capston.databinding.ActivityDogRegister2Binding
import kotlinx.android.synthetic.main.activity_dog_register.breed_search
import kotlinx.android.synthetic.main.activity_dog_register2.*
import java.util.ArrayList

class DogRegister2Activity : AppCompatActivity(), BreedItemClick {

    private final val REQUEST_FIRST = 1010

    var validSpinner: Boolean= false
    var validGender: Boolean = false

    lateinit var breed_recycleR: RecyclerView
    lateinit var breedAdapter: BreedAdapter
    lateinit var breed: ArrayList<BreedDTO>
    lateinit var BreedSearch: SearchView

    private lateinit var dogName : String
    private var dogGender : Int? = null
    private lateinit var dogBreed : String

    private lateinit var viewBinding: ActivityDogRegister2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDogRegister2Binding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        BreedSearch = viewBinding.breedSearch
        breed_recycleR = viewBinding.rvPhoneBook

        dogName = intent.getStringExtra("DogName")!!

        // 우측 위 건너뛰기 버튼 누르면 메인으로 넘어가기 = 뒤로가기와 동일
        viewBinding.skipBtn.setOnClickListener {
            onBackPressed()
        }

        viewBinding.nextBtn.setOnClickListener {
            val intent = Intent(this, DogRegister3Activity::class.java)
            intent.putExtra("DogName", dogName)
            intent.putExtra("Gender",dogGender)
            intent.putExtra("Breed", dogBreed)
            startActivity(intent)
            finish()
        }

        viewBinding.backButton.setOnClickListener {
            val intent = Intent(this, DogRegister1Activity::class.java)
            startActivity(intent)
            finish()
        }

        //배경 클릭시 포커스해제
        viewBinding.background.setOnClickListener {
            breed_recycleR.visibility= View.INVISIBLE
            BreedSearch.clearFocus()
        }

        // 성별 남 버튼
        viewBinding.genderBoy.setOnClickListener{
            validGender = true
            checkValid(validSpinner, validGender)
            dogGender = 0
        }
        // 성별 여 버튼
        viewBinding.genderGirl.setOnClickListener{
            validGender = true
            checkValid(validSpinner, validGender)
            dogGender = 1
        }

        BreedSearch.setOnQueryTextListener(searchViewTextListener)
        breed = tempPersons()
        setAdapter()
        setupBreedData()

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
        tempPersons.add(BreedDTO(""))
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

    override fun onClick(value: String?) {
        if (!breedAdapter.choose_breed.isEmpty()) {
            BreedSearch.queryHint = breedAdapter.choose_breed
            val editText =
                findViewById<SearchView>(androidx.appcompat.R.id.search_src_text) as EditText
            editText.setHintTextColor(Color.BLACK)
            BreedSearch.clearFocus()// 포커스 초기화
            // 견종 저장
            dogBreed = breedAdapter.choose_breed
            breed_recycleR.visibility=View.INVISIBLE
            validSpinner = true
            checkValid(validGender,validSpinner)
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

    private fun checkValid(v1:Boolean, v2:Boolean){
//        Log.d("Valid", (v1 && v2).toString())
        if(v1 && v2){
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