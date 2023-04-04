//package com.example.capston
//
//import android.content.Context
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.BaseAdapter
//import android.widget.CheckBox
//import android.widget.Toast
//
//class SelectDogAdapter (val context: Context, val selectDogList: ArrayList<SelectDog>) : BaseAdapter() {
//    private var checkedName = arrayListOf<String>()
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view: View = LayoutInflater.from(context).inflate(R.layout.select_dog_item, null)
//
//        val dogName = view.findViewById<CheckBox>(R.id.select_dog_checkbox)
//        val selectDog = selectDogList[position]
//
//        dogName.text = selectDog.name
//
//        dogName.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                if (checkedName.indexOf(selectDog.name) == -1)
//                    checkedName.add(selectDog.name)
//            } else {
//                checkedName.remove(selectDog.name)
//            }
//        }
//        dogName.isChecked = true
//
//        return view
//    }
//
//    override fun getItem(position: Int): Any {
//        return selectDogList[position]
//    }
//
//    override fun getItemId(position: Int): Long {
//        return 0
//    }
//
//    override fun getCount(): Int {
//        return selectDogList.size
//    }
//
//    fun getCheckedName(): ArrayList<String> {
//        return checkedName
//    }
//
//}