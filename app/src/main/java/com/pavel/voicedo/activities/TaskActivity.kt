package com.pavel.voicedo.activities

import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ListenableActivity
import com.pavel.voicedo.adapters.TodoAdapter
import com.pavel.voicedo.listeners.HideFabOnScrollListener
import com.pavel.voicedo.models.Task
import com.pavel.voicedo.voice.ActionParser

class TaskActivity : ListenableActivity() {
    lateinit var task : Task

    enum class eStatus {
        VIEW, EDIT, CREATE
    }

    @BindView(R.id.input_description)
    lateinit var input_description: TextView
    @BindView(R.id.input_state)
    lateinit var input_state: TextView
    @BindView(R.id.input_date)
    lateinit var input_state_date: TextView

    var status : eStatus = eStatus.VIEW
    override fun getHelpText(): List<String> {
        val list : ArrayList<String> = arrayListOf<String>()
        when (status) {
            eStatus.VIEW -> {
                list.add(resources.getString(R.string.remove_task_help))
                list.add(resources.getString(R.string.edit_task_name_help))
                list.add(resources.getString(R.string.edit_task_state_help))
                list.add(resources.getString(R.string.edit_task_back_help))
            }
            else -> { }
        }
        return list
    }

    override fun onResult(action: ActionParser.Action) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        ButterKnife.bind(this)

        task = intent.getSerializableExtra(MainActivity.PARAMS.TASK) as Task

        updateUI()
    }

    fun updateUI() {
        input_description.text = task.description
        input_state.text = task.getStringState(this)
        input_state_date.text = task.getStringDate()
    }

    override fun isWaitingInput() : Boolean {
        return false
    }
}