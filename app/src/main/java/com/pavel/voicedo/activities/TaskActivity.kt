package com.pavel.voicedo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.pavel.voicedo.R
import com.pavel.voicedo.models.Task

class TaskActivity : AppCompatActivity() {
    lateinit var task : Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.task_activity)
        ButterKnife.bind(this)

        task = getIntent().getSerializableExtra(MainActivity.PARAMS.TASK) as Task

        updateUI()
    }

    fun updateUI() {
        //TODO: Load task data
    }
}