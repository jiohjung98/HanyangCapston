//package com.example.capston
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.google.protobuf.Internal
//
//class HouseViewPagerAdapter(val pageClickedCallback : (HouseData)-> Unit) : Internal.ListAdapter<HouseData, HouseViewPagerAdapter.ItemViewHolder>(differ){
//    inner class ItemViewHolder(val view : View) : RecyclerView.ViewHolder(view){
//        fun bind(house : HouseData){
//            val title = view.findViewById<TextView>(R.id.titleTextView)
//            val price = view.findViewById<TextView>(R.id.priceTextView)
//            val image = view.findViewById<ImageView>(R.id.thumbnailImageView)
//
//            view.setOnClickListener {
//                pageClickedCallback(house)
//            }
//
//            title.text = house.title
//            price.text = house.price
//            Glide.with(image.context)
//                .load(house.imgUrl)
//                .into(image)
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        return ItemViewHolder(inflater.inflate(R.layout.item_house_viewpager,parent,false))
//    }
//
//    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
//        holder.bind(currentList[position])
//    }
//
//    companion object {
//        val differ = object : DiffUtil.ItemCallback<HouseData>(){
//            override fun areItemsTheSame(oldItem: HouseData, newItem: HouseData): Boolean {
//                return oldItem.id == newItem.id
//            }
//
//            override fun areContentsTheSame(oldItem: HouseData, newItem: HouseData): Boolean {
//                return oldItem == newItem
//            }
//
//        }
//    }
//}