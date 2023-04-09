package com.example.capston

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.capston.databinding.LostDogInfoBinding
import com.example.capston.homepackage.NaviHomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_dog_register.imageArea
import kotlinx.android.synthetic.main.activity_dog_register.image_upload_btn
import net.daum.mf.map.api.*
import java.text.SimpleDateFormat
import java.util.*


class DogInfoEnterDialog(private val context : AppCompatActivity): DialogFragment() {

    var listen: NaviHomeFragment.MarkerEventListener? = null
    lateinit var mainActivity: MainActivity

    private lateinit var auth: FirebaseAuth
    lateinit var uri: Uri
    private final val REQUEST_FIRST = 1010

    var lost_pet_info = LostPetInfo()

    private lateinit var binding: LostDogInfoBinding
    private var dogInfoDialog = Dialog(context)   //부모 액티비티의 context 가 들어감

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = LostDogInfoBinding.inflate(inflater, container, false)

        myDlg()
        mainActivity = context as MainActivity

        auth = getInstance()

        initAddImage()



        return binding.root
    }


    fun myDlg() {
        dogInfoDialog.show()
        binding = LostDogInfoBinding.inflate(context.layoutInflater)

        mainActivity = context as MainActivity

        // 다이얼로그 테두리 둥글게 만들기
        dogInfoDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dogInfoDialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dogInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dogInfoDialog.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        dogInfoDialog.setCancelable(true)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함


        // 다이얼로그 뜨는 위치 조정하기
        val params: WindowManager.LayoutParams = this.dogInfoDialog.window!!.attributes
        params.y = 800
        this.dogInfoDialog.window!!.attributes = params
        val info = dogInfoDialog.findViewById<EditText>(R.id.inputInfo)
        val time = dogInfoDialog.findViewById<EditText>(R.id.inputTime)

        listen = NaviHomeFragment.MarkerEventListener(mainActivity)

        binding.yesBtn.setOnClickListener {
            val getInfo: String = info.text.toString()
            val getTime: String = time.text.toString()
            Log.d("info&time 값", "$getInfo $getTime")

            onClickedListener?.onClicked(getInfo, getTime)
            dogInfoDialog.dismiss()
        }

        binding.noBtn.setOnClickListener {
            dogInfoDialog.dismiss()
        }

    }

    interface ButtonClickListener {
        fun onClicked(time: String, info: String)
    }

    private var onClickedListener: ButtonClickListener? = null

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickedListener = listener
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
                    Toast.makeText(context,"권한을 거부했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else -> {
            //Do Nothing
        }
        }
    }

    private fun initAddImage() {
        binding.imageArea.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(context,
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
    * 저장소 접근 권한 설정 팝업
    */
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(context)
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
                    binding.imageArea.setImageURI(uri)

                    // 갤러리에서 이미지 가져오고 등록하기 버튼 활성화
                    image_upload_btn.isEnabled = true
                    image_upload_btn.isClickable = true

                    // Upload
//                    initUploadImage(uri)
                }
            }
        }

    /*
     * 업로드 버튼 클릭 이벤트 설정
     */
//    private fun initUploadImage(uri : Uri){
//        binding.imageUploadBtn.setOnClickListener{
//            // 서버로 업로드
//            uploadImageToStorage(uri)
//        }
//    }

    /*
     * 서버 스토리지로 이미지 업로드
     */
    private fun uploadImageToStorage(uri: Uri) {
        // storage 인스턴스 생성
        val storage = Firebase.storage
        // storage 참조
        val storageRef = storage.getReference("images").child("missing")
        // storage에 저장할 파일명 선언
        val fileName = auth.currentUser!!.uid + "_" + SimpleDateFormat("yyyyMMddHHmm").format(Date())
        val mountainsRef = storageRef.child("${fileName}.jpg")


        val uploadTask = mountainsRef.putFile(uri).addOnCompleteListener {
            if (it.isSuccessful) {
                // 파일 업로드에 성공했기 때문에 스토리지 url을 다시 받아와 DB에 저장
                mountainsRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        lost_pet_info.lost_img = uri.toString()
                    }.addOnFailureListener {
                        Toast.makeText(context, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
            } else {
                Toast.makeText(context, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // 파일 업로드 성공
        uploadTask.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(context, "사진 업로드 성공", Toast.LENGTH_SHORT).show();
            // 성공했으므로 업로드 버튼 비활성화
            image_upload_btn.isEnabled = false
            // 갤러리 불러오기 비활성화
            imageArea.isEnabled = false
            imageArea.isClickable = false

        }   // 파일 업로드 실패
            .addOnFailureListener {
                Toast.makeText(context, "사진 업로드 실패", Toast.LENGTH_SHORT).show();
            }
    }
}

//
//    fun setOnOKClickedListener(listener: (String) -> Unit) {
//        this.listener = object: MyDialogOKClickedListener {
//            override fun onOKClicked(content: String) {
//                listener(content)
//            }
//        }
//    }
//
//    interface MyDialogOKClickedListener {
//        fun onOKClicked(content : String)
//    }


//
//class LogoutDialog(): DialogFragment() {
//
//    private var _binding: LogoutdialogBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = LogoutdialogBinding.inflate(inflater, container, false)
//        val view = binding.root
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
//
//        initDialog()
//        return view
//    }
//
//    fun initDialog() {
//
//        binding.yesBtn.setOnClickListener {
//            buttonClickListener.onButton1Clicked()
//            dismiss()
//        }
//
//        binding.noBtn.setOnClickListener {
//            buttonClickListener.onButton2Clicked()
//            dismiss()
//        }
//
//    }
//
//    interface OnButtonClickListener {
//        fun onButton1Clicked()
//        fun onButton2Clicked()
//    }
//
//    override fun onStart() {
//        super.onStart();
//        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
//        lp.copyFrom(dialog!!.window!!.attributes)
//        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        val window: Window = dialog!!.window!!
//        window.attributes = lp
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    // 클릭 이벤트 설정
//    fun setButtonClickListener(buttonClickListener: OnButtonClickListener) {
//        this.buttonClickListener = buttonClickListener
//    }
//
//    // 클릭 이벤트 실행
//    private lateinit var buttonClickListener: OnButtonClickListener
//}

//lateinit var mainActivity: MainActivity
//
//private fun onClick(view: View?) {
//    val logoutDlg = LogoutDialog(mainActivity)
//    Log.d("gdsa","asdgdsg")
//    logoutDlg.setOnOKClickedListener{ content ->
//        binding.yesBtn.text = content
//    }
//    logoutDlg.show("메인의 내용을 변경할까요?")
//}
