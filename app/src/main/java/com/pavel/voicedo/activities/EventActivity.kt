package com.pavel.voicedo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.pavel.voicedo.R
import com.pavel.voicedo.models.Event

class EventActivity : AppCompatActivity() {
    lateinit var event : Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_event)
        ButterKnife.bind(this)

        event = getIntent().getSerializableExtra(MainActivity.PARAMS.EVENT) as Event

        updateUI()
    }

    fun updateUI() {
        //TODO: Load task data
    }
}