package com.pavel.voicedo.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ListenableActivity
import com.pavel.voicedo.models.Task
import com.pavel.voicedo.voice.ActionParser

class TaskActivity : ListenableActivity() {
    enum class EnumStatus {
        VIEW, SAY_NAME, SAY_STATUS
    }

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.input_description)
    lateinit var inputDescription: TextView

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.input_state)
    lateinit var inputState: TextView

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.input_date)
    lateinit var inputStateDate: TextView

    private lateinit var task : Task
    private var status : EnumStatus = EnumStatus.VIEW

    override fun getHelpText(): List<String> {
        val list : ArrayList<String> = arrayListOf()
        when (status) {
            EnumStatus.VIEW -> {
                list.add(resources.getString(R.string.remove_task_help))
                list.add(resources.getString(R.string.edit_task_name_help))
                list.add(resources.getString(R.string.edit_task_state_help))
                list.add(resources.getString(R.string.edit_task_back_help))
            }
            else -> { }
            //TODO: Completar
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

        if (intent.hasExtra(MainActivity.PARAMS.LIST)) {
            task = intent.getSerializableExtra(MainActivity.PARAMS.TASK) as Task
            status = EnumStatus.VIEW
        } else {
            task = Task()
            status = EnumStatus.SAY_NAME
        }
        updateUI()
    }

    override fun updateUI() {
        inputDescription.text = task.description
        inputState.text = task.getStringState(this)
        inputStateDate.text = task.getStringDate()
    }

    override fun isWaitingInput() : Boolean {
        return false
    }

    override fun hasCustomTitle() : Boolean {
        return true
    }

    override fun getTitleResource() : Int {
        //TODO: Pendent
        return R.string.app_name
    }
}