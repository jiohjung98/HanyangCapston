package com.example.capston

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.ActivityMissingAfterBinding
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.activity_dog_register.*

@Suppress("DEPRECATION")
class MissingAfterActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMissingAfterBinding


    private var functions : FirebaseFunctions = FirebaseFunctions.getInstance()

    private var breed : String? = null
    private var imageUrl : String? = null
    private var address2 : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMissingAfterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        breed = intent.getStringExtra("breed")!!.trim()
        imageUrl = intent.getStringExtra("imageUrl")!!.trim()
        address2 = intent.getStringExtra("address2")!!.trim()

        viewBinding.goNextBtn.setOnClickListener {
            val intent = Intent(this, TrackingActivity::class.java)
            intent.putExtra("breed",breed)
            intent.putExtra("imageUrl",imageUrl)
            intent.putExtra("address2",address2)
            startActivity(intent)
            finish()
        }

//        Handler().postDelayed(Runnable {
//            val intent = Intent(this, TrackingActivity::class.java)
//            startActivity(intent)
//        }, 3000)

    }

    /*
    인공지능서버에 이미지url 보냄
    */
    private fun sendToServer(uri: String) {
        val data = hashMapOf(
            "imageUrl" to uri,
        )

        Log.d("sendToServer", data.toString())

        functions.getHttpsCallable("sendImageToServer")
            .call(data)
            .addOnSuccessListener { task->
                val result = task.data.toString()
                Log.d("인공지능 결과",result)
            }
            .addOnFailureListener {
                Log.d("인공지능 결과","FAIL")
            }
    }


}