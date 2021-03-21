package com.pavel.voicedo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.OnClick
import com.pavel.voicedo.R

class ListenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listen)

        ButterKnife.bind(this)
    }

    @OnClick(R.id.btn_cancel)
    fun onCancel() {
        this.finish()
    }
}