package com.pavel.voicedo.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.pavel.voicedo.R


class HelpDialog(c: Activity, _help: List<String>) : Dialog(c) {
    private val help : List<String> = _help

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.help_root)
    lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.help_dialog)
        ButterKnife.bind(this)

        for(help_text : String in help) {
            val parent: LinearLayout = layoutInflater.inflate(R.layout.help_label, root) as LinearLayout //TextView
            val child : TextView = parent.getChildAt(parent.childCount - 1) as TextView
            child.text = help_text
        }

        val window: Window = this.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}