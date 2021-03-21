package com.pavel.voicedo.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.pavel.voicedo.R


class HelpDialog(c: Activity, _help: List<String>) : Dialog(c) {
    val help : List<String> = _help

    @BindView(R.id.help_root)
    lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.help_dialog)
        ButterKnife.bind(this)

        for(help_text : String in help) {
            val child: View = layoutInflater.inflate(R.layout.help_label, null)
            val label: TextView = child.findViewById(R.id.label)
            label.text = help_text
            root.addView(child)
        }
    }
}