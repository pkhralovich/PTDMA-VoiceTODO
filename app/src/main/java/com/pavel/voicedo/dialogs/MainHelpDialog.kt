package com.pavel.voicedo.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Window
import com.pavel.voicedo.R

class MainHelpDialog(c: Activity) : Dialog(c) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.main_help_dialog)
    }
}