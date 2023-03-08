//package com.example.capston
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.BaseAdapter
//import android.widget.TextView
//import com.example.walkingdog_kotlin.R
//import com.example.walkingdog_kotlin.Walking.Model.CheckItem
//
//class CheckListAdapter (val context: Context, val checkItemList : ArrayList<CheckItem>) : BaseAdapter() {
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view: View = LayoutInflater.from(context).inflate(R.layout.check_list_item, null)
//
//        val item = view.findViewById<TextView>(R.id.check_item)
//
//        val checkItem = checkItemList[position]
//        item.text = checkItem.item
//
//        return view
//    }
//
//    override fun getItem(position: Int): Any {
//        return checkItemList[position]
//    }
//
//    override fun getItemId(position: Int): Long {
//        return 0
//    }
//
//    override fun getCount(): Int {
//        return checkItemList.size
//    }
//
//
//}