package com.example.capston.DogRegister

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.example.capston.SkipDialog
import com.example.capston.databinding.ActivityDogRegister1Binding
import kotlinx.android.synthetic.main.activity_dog_register.*

class DogRegister1Activity : AppCompatActivity() {

    var validName: Boolean = false

    private lateinit var viewBinding: ActivityDogRegister1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDogRegister1Binding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // 우측 위 건너뛰기 버튼 누르면 메인으로 넘어가기 = 뒤로가기와 동일
        viewBinding.skipBtn.setOnClickListener {
            onBackPressed()
        }

        viewBinding.dogNameEdtText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if(viewBinding.dogNameEdtText.length() > 0){
                    validName = true
                    viewBinding.nextBtn.isClickable = true
                    viewBinding.nextBtn.isEnabled = true

                } else{
                    validName = false
                    viewBinding.nextBtn.isClickable = false
                    viewBinding.nextBtn.isEnabled = false
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                p0?.let { highlightText(it as Editable) }
            }
        })

        viewBinding.nextBtn.setOnClickListener {
            val intent = Intent(this, DogRegister2Activity::class.java)
            // 개이름전달
            intent.putExtra("DogName", dog_name_edt_text.text.toString().trim())
            startActivity(intent)
        }
    }

//    private fun addDogToDB(pet_info : PetInfo){
//        val database: DatabaseReference =
//            Firebase.database.reference.child("users").child(auth.currentUser!!.uid)
//
//        var next_pet_num : Int? = null
//
//        // 로컬에 저장된 현재 반려견 인덱스 접근
//        sharedPreferences = getSharedPreferences("CUR_PET", MODE_PRIVATE);
//
//        // 현재 반려견 등록수 확인
//        database.child("pet_cnt").get().addOnSuccessListener{ task ->
//            if (task.value != null){
//                // 새로 등록할 반려견의 인덱스 = 현재 등록수+1
//                next_pet_num = Integer.parseInt(task.value.toString()).plus(1)
//                // 로컬 현재 반려견 인덱스 변경
//                sharedPreferences?.edit()?.putInt("cur_pet", next_pet_num!!)?.apply()
//                // db의 현재 반려견 수 변경
//                database.child("pet_cnt").setValue(next_pet_num)
//                // db에 새 반려견 등록
//                database.child("pet_list").child(next_pet_num.toString()).setValue(pet_info)
//            }
//            else{
//                Log.d("DB LOAD FAIL","현재 반려견 인덱스 불러오기 실패")
//            }
//        }
//    }

    // 화면 클릭하여 키보드 숨기기 및 포커스 제거
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action === MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
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