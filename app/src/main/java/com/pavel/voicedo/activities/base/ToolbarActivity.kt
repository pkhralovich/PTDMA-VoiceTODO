package com.pavel.voicedo.activities.base

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import com.pavel.voicedo.R

abstract class ToolbarActivity : AppCompatActivity() {
    @BindView(R.id.toolbar_label)
    lateinit var toolbar_label : TextView

    fun getTitleResource() : Int {
        return -1;
    }

    private fun hasCustomTitle() : Boolean {
        return getTitleResource() > 0;
    }

    override fun onStart() {
        super.onStart()
        if (hasCustomTitle()) {
            toolbar_label.text = resources.getString(getTitleResource())
        }
    }
}