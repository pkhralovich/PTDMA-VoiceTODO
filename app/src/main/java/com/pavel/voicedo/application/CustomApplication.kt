package com.pavel.voicedo.application

import com.orm.SugarApp
import com.orm.SugarContext

class CustomApplication : SugarApp() {

    override fun onCreate() {
        super.onCreate()
        SugarContext.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        SugarContext.terminate()
    }
}