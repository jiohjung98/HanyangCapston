package com.example.capston

import android.util.Log
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun ReformatDate(format: String, element: Date): String {
    val df: DateFormat = SimpleDateFormat(format)
    return df.format(element)
}