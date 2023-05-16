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

    private var imageUrl : String? = null
    private var enteredBreed : String? = null
    private var functions : FirebaseFunctions = FirebaseFunctions.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMissingAfterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        imageUrl = intent.getStringExtra("url")
        enteredBreed = intent.getStringExtra("EnteredBreed")?.trim()

        viewBinding.goNextBtn.setOnClickListener {
            val intent = Intent(this, TrackingActivity::class.java)
            startActivity(intent)
            finish()
        }

//        Handler().postDelayed(Runnable {
//            val intent = Intent(this, TrackingActivity::class.java)
//            startActivity(intent)
//        }, 3000)

        if(imageUrl != null)
            sendToServer(imageUrl!!)
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
                checkEqual(result)
            }
            .addOnFailureListener {
                Log.d("인공지능 결과","FAIL")
            }
    }

    private fun checkEqual(result : String){
        if(enteredBreed.equals(result.trim()) == true){
            val intent = Intent(this, TrackingActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{

        }
    }
}