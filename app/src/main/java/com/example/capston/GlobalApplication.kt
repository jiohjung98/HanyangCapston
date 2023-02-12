package com.example.capston

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this,"830d2ef983929904f477a09ea75d91cc")
    }
}