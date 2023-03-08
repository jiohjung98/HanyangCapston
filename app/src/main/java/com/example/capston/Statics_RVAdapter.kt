package com.example.capston

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class Statics_RVAdapter(val mContext: Context, val staticsItem: ArrayList<StaticsItem>,
                        val itemClick: (StaticsItem) -> Unit):
    RecyclerView.Adapter<Statics_RVAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.statics_history_item, parent, false)
        return Holder(view, itemClick)
    }

    override fun getItemCount(): Int {
        return staticsItem.size
    }

    override fun onBindViewHolder(holder: Statics_RVAdapter.Holder, position: Int) {
        holder?.bind(staticsItem [position], mContext)
    }

    inner class Holder(itemView: View?, itemClick: (StaticsItem) -> Unit) : RecyclerView.ViewHolder(itemView!!) {
        val history_date = itemView?.findViewById<TextView>(R.id.history_date)
        val history_cal = itemView?.findViewById<TextView>(R.id.history_cal)
        val history_hour = itemView?.findViewById<TextView>(R.id.history_hour)
        val history_minutes = itemView?.findViewById<TextView>(R.id.history_minutes)
        val history_sec = itemView?.findViewById<TextView>(R.id.history_sec)
        val writeBtn = itemView?.findViewById<ImageView>(R.id.feedWriteIcon)


        fun bind (staticsItem: StaticsItem, context: Context) {
            /* 나머지 TextView와 String 데이터를 연결한다. */
            history_date?.text = staticsItem.date
            history_cal?.text = staticsItem.cal
            history_hour?.text = staticsItem.hour
            history_minutes?.text = staticsItem.minutes
            history_sec?.text = staticsItem.sec


            writeBtn?.setOnClickListener { itemClick(staticsItem)

            }




        }
    }
}



