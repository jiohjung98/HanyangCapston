package com.example.capston

class dogInfoAdapter {
    interface ButtonClickListener {
        fun checkedCount(checkedCount : Int)
    }

    private lateinit var onClickedListener: ButtonClickListener

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickedListener = listener
    }
}