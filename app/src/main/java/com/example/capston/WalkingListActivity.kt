package com.example.capston

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_walking_list.*

class WalkingListActivity : AppCompatActivity() {
    var walkingInfoList = arrayListOf<WalkingInfo>(
        WalkingInfo("1","2","3","4","5"),
        WalkingInfo("a","d","g","j","m"),
        WalkingInfo("b","e","h","k","o"),
        WalkingInfo("c","f","i","l","p")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walking_list)

        val mAdapter = WalkingInfoRvAdapter(this, walkingInfoList)
        sWRecyclerView.adapter = mAdapter

        val lm = LinearLayoutManager(this)
        sWRecyclerView.layoutManager = lm
        sWRecyclerView.setHasFixedSize(true)
    }
}