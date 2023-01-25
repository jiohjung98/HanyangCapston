package com.example.capston

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context?):
    SQLiteOpenHelper(context, "Login.db", null, 1) {

    // onCreate에서 새로운 테이블 형성
    override fun onCreate(MyDB: SQLiteDatabase) {
        MyDB.execSQL("create Table users(username TEXT primary key, password TEXT)")
    }

    // onUpgrade에서 정보를 갱신
    override fun onUpgrade(MyDB: SQLiteDatabase, i: Int, i1: Int) {
        MyDB.execSQL("drop Table if exists users")
    }

    // insertData에서 username, password 정보를 받아 DB에 삽입. 삽입이 성공하면 true, 실패하면 false 리턴
    fun insertData(username: String?, password: String?): Boolean {
    val MyDB = this.writableDatabase
    val contentValues = ContentValues()
    contentValues.put("username", username)
    contentValues.put("password", password)
    val result = MyDB.insert("users", null, contentValues)
        return result != -1L
    }

    // checkUsername에서 사용자 입력을 감지해 입력이 없을 경우 false, 아닐경우 true를 리턴
    fun checkUsername(username: String): Boolean {
        val MyDB = this.writableDatabase
        var res = true
        val cursor = MyDB.rawQuery("Select * from users where username = ?", arrayOf(username))
        if (cursor.count <= 0 ) res = false
        return res
    }

    // checkUserPass에서 username이 존재하는지, password가 입력되었는지 확인. 입력 안됐을 경우 false 리턴
    fun checkUserPass(username: String, password: String): Boolean {
        val MyDB = this.writableDatabase
        var res = true
        val cursor = MyDB.rawQuery(
            "Select * from users where username = ? and password = ?",
            arrayOf(username, password)
        )
        if (cursor.count <= 0) res = false
        return res
    }

    // object를 통해 DB NAME을 Login.db로 설정
    companion object {
        const val DBNAME = "Login.db"
    }
}