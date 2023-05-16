package com.example.capston.DogRegister

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.*
import com.example.capston.databinding.ActivityDogRegister2Binding
import kotlinx.android.synthetic.main.activity_dog_register.*
import kotlinx.android.synthetic.main.activity_dog_register.breed_search
import kotlinx.android.synthetic.main.activity_dog_register2.*
import java.util.ArrayList

class DogRegister2Activity : AppCompatActivity() {

    private final val REQUEST_FIRST = 1010

    var validSpinner: Boolean= false
    var validGender: Boolean = false

    private lateinit var dogName : String
    private var dogGender : Int? = null
    private lateinit var dogAge : String

    private lateinit var viewBinding: ActivityDogRegister2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDogRegister2Binding.inflate(layoutInflater)
        setContentView(viewBinding.root)
//        BreedSearch = viewBinding.breedSearch
//        breed_recycleR = viewBinding.rvPhoneBook

        dogName = intent.getStringExtra("DogName")!!

        // 우측 위 건너뛰기 버튼 누르면 메인으로 넘어가기 = 뒤로가기와 동일
        viewBinding.skipBtn.setOnClickListener {
            onBackPressed()
        }

        viewBinding.nextBtn.setOnClickListener {
            val intent = Intent(this, DogRegister3Activity::class.java)
            intent.putExtra("DogName", dogName)
            intent.putExtra("Gender",dogGender)
            intent.putExtra("Born", dogAge)
            startActivity(intent)
        }

        viewBinding.backButton.setOnClickListener {
            super.onBackPressed()
        }

//        //배경 클릭시 포커스해제
//        viewBinding.background.setOnClickListener {
//            breed_recycleR.visibility= View.INVISIBLE
//            BreedSearch.clearFocus()
//        }

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

        setupAgeData()
        setupAgeHandler()

//        BreedSearch.setOnQueryTextListener(searchViewTextListener)
//        breed = tempPersons()
//        setAdapter()
//        setupBreedData()

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

        viewBinding.dogAgeSpinner.setSelection(ageAdapter.count)
        viewBinding.dogAgeSpinner.dropDownVerticalOffset = dipToPixels(90f).toInt()

    }

    private fun setupAgeHandler() {
        viewBinding.dogAgeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        validSpinner = true
                    }
                    else -> {
                        validSpinner = true
                        Log.d("스피너3", "$validSpinner")
                    }
                }
                checkValid(validSpinner, validGender)
                // 출생년도 저장
                dogAge = viewBinding.dogAgeSpinner.selectedItem.toString()
//                Log.d("BORN YEAR", "${pet_info.born}")
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                validSpinner = false
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