package com.pavel.voicedo.activities.base

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import com.pavel.voicedo.R

abstract class ToolbarActivity : AppCompatActivity() {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.toolbar_label)
    lateinit var toolbarLabel : TextView

    open fun getTitleResource() : Int {
        return -1
    }

    open fun hasCustomTitle() : Boolean {
        return getTitleResource() > 0
    }

    override fun onStart() {
        super.onStart()
        updateTitle()
    }

    fun updateTitle() {
        if (hasCustomTitle()) {
            toolbarLabel.text = resources.getString(getTitleResource())
        }
    }

    abstract fun updateUI()
}