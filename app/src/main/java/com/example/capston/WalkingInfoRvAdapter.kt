package com.example.capston

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WalkingInfoRvAdapter (val context: Context, val walkingInfoList:ArrayList<WalkingInfo>) :
        RecyclerView.Adapter<WalkingInfoRvAdapter.Holder>() {

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val walkingDate = itemView?.findViewById<TextView>(R.id.walkingDateTv)


        fun bind (walkingInfo: WalkingInfo, context: Context) {
            walkingDate?.text = walkingInfo.date

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.walking_info_rv_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return walkingInfoList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(walkingInfoList[position], context)
    }
}