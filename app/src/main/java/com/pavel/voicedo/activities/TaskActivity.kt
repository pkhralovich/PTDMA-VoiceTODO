package com.pavel.voicedo.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.orm.SugarRecord
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ListenableActivity
import com.pavel.voicedo.models.BaseTask
import com.pavel.voicedo.models.Task
import com.pavel.voicedo.voice.ActionParser
import com.pavel.voicedo.voice.Speaker
import java.util.*
import kotlin.collections.ArrayList

class TaskActivity : ListenableActivity() {
    enum class EnumStatus {
        VIEW, SAY_NAME, SAY_STATUS, WAITING_CONFIRMATION
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        ButterKnife.bind(this)

        if (intent.hasExtra(MainActivity.PARAMS.TASK)) {
            task = intent.getSerializableExtra(MainActivity.PARAMS.TASK) as Task
            task.id = intent.getLongExtra(MainActivity.PARAMS.TASK_ID, -1)

            status = EnumStatus.VIEW
        } else {
            task = Task()
            status = EnumStatus.SAY_NAME
        }
    }

    override fun onInit(status: Int) {
        if (Speaker.onInit(status)) {
            updateUI()
        }
    }

    override fun onNoOrderFound() {
        if (this.isWaitingInput() && task.description.isNotEmpty())
            this.status = EnumStatus.VIEW
        super.onNoOrderFound()
    }

    override fun onBackPressed() {
        if (task.id == null && task.description.isNotEmpty() && this.status != EnumStatus.WAITING_CONFIRMATION) {
            this.status = EnumStatus.WAITING_CONFIRMATION
            showListenable(false)
            Speaker.speak(R.string.response_confirm_exit, listenerLabel, true)
        } else super.onBackPressed()
    }

    override fun getHelpText(): List<String> {
        val list : ArrayList<String> = arrayListOf()
        when (status) {
            EnumStatus.VIEW -> {
                list.add(resources.getString(R.string.remove_task_help))
                list.add(resources.getString(R.string.edit_task_name_help))
                list.add(resources.getString(R.string.edit_task_state_help))
                list.add(resources.getString(R.string.edit_task_back_help))
            }
            EnumStatus.WAITING_CONFIRMATION -> {
                list.add(resources.getString(R.string.confirmation_help))
                list.add(resources.getString(R.string.cancelation_help))
            }
            EnumStatus.SAY_NAME -> list.add(resources.getString(R.string.say_task_name_help))
            else -> list.add(resources.getString(R.string.say_task_status_name_help))
        }
        return list
    }

    override fun onResult(action: ActionParser.Action) {
        if (this.status == EnumStatus.WAITING_CONFIRMATION) {
            when (action.action) {
                ActionParser.Action.ActionType.CONFIRMATION -> this.finish()
                else -> {
                    hideListenable()
                    this.status = EnumStatus.VIEW
                    updateUI()
                }
            }
        } else {
            when (action.action) {
                ActionParser.Action.ActionType.HELP -> onClickHelp()
                ActionParser.Action.ActionType.CHANGE_TASK_NAME -> {
                    status = EnumStatus.SAY_NAME
                    updateUI()
                }
                ActionParser.Action.ActionType.CHANGE_TASK_STATUS -> {
                    status = EnumStatus.SAY_STATUS
                    updateUI()
                }
                ActionParser.Action.ActionType.FINISH_EDITION -> {
                    task.save()
                    status = EnumStatus.VIEW
                    Speaker.speak(R.string.task_saved, null, false)
                    hideListenable()
                    updateUI()
                }
                ActionParser.Action.ActionType.BACK,
                ActionParser.Action.ActionType.CANCELLATION -> {
                    if (isWaitingInput() && task.description.isNotEmpty()) {
                        if (task.state != Task.Companion.EnumTaskState.UNDEFINED) this.status = EnumStatus.VIEW
                        else this.status = EnumStatus.SAY_STATUS

                        updateUI()
                    } else this.onBackPressed()
                }
                ActionParser.Action.ActionType.INPUT -> {
                    when (status) {
                        EnumStatus.SAY_NAME -> onInputName(action)
                        EnumStatus.SAY_STATUS -> onInputStatus(action)
                        else -> onInvalidAction()
                    }
                }
                ActionParser.Action.ActionType.DELETE_TASK -> {
                    task.delete()
                    Speaker.speak(R.string.response_removing_task, null)
                    finish()
                }
                else -> onInvalidAction()
            }
        }
    }

    private fun onInputName(action: ActionParser.Action) {
        val tasks = SugarRecord.listAll(Task::class.java)
        val aux = BaseTask.getTask(tasks, action.param!!)

        if (aux == null) {
            task.description = action.param

            if (task.state == Task.Companion.EnumTaskState.UNDEFINED) {
                this.task.state = Task.Companion.EnumTaskState.TODO
                this.task.stateDate = Date()
            }

            this.status = EnumStatus.VIEW
            if (task.id != null) task.save()

            hideListenable()
            updateUI()
        } else Speaker.speak(R.string.list_already_exists, listenerLabel, true)
    }

    private fun onInputStatus(action: ActionParser.Action) {
        var actionState : Task.Companion.EnumTaskState = Task.Companion.EnumTaskState.UNDEFINED
        when (action.param?.toUpperCase(Locale.ROOT)?.trim()) {
            Task.getStringState(Task.Companion.EnumTaskState.DOING, this) -> actionState = Task.Companion.EnumTaskState.DOING
            Task.getStringState(Task.Companion.EnumTaskState.DONE, this) -> actionState = Task.Companion.EnumTaskState.DONE
            Task.getStringState(Task.Companion.EnumTaskState.TODO, this) -> actionState = Task.Companion.EnumTaskState.TODO
        }

        hideListenable()
        if (actionState != Task.Companion.EnumTaskState.UNDEFINED) {
            task.state = actionState
            task.stateDate = Date()

            if (task.id != null) task.save()
            status = EnumStatus.VIEW
            updateUI()
        } else {
            status = EnumStatus.VIEW
            Speaker.speak(R.string.response_not_understand, listenerLabel, false)
        }
    }

    override fun updateUI() {
        inputDescription.text = task.description

        if (task.state != Task.Companion.EnumTaskState.UNDEFINED) {
            inputStateDate.text = task.getStringDate()
            inputState.text = task.getStringState(this)
        }
        else {
            inputState.text = ""
            inputStateDate.text = ""
        }

        updateTitle()

        when (status) {
            EnumStatus.SAY_NAME -> {
                showListenable(false)
                Speaker.speak(R.string.ask_task_name, listenerLabel)
            }
            EnumStatus.SAY_STATUS -> {
                showListenable(false)
                Speaker.speak(R.string.ask_task_status, listenerLabel)
            }
            else -> {}
        }
    }

    override fun isWaitingInput() : Boolean {
        return status != EnumStatus.VIEW && status != EnumStatus.WAITING_CONFIRMATION
    }

    override fun hasCustomTitle() : Boolean {
        return true
    }

    override fun getTitleResource() : Int {
        return if (task.id != null) R.string.view_task
        else R.string.create_task
    }

    override fun onClickListen() {
        showListenable(true)
        when (this.status) {
            EnumStatus.SAY_NAME -> Speaker.speak(R.string.ask_task_name, listenerLabel)
            EnumStatus.SAY_STATUS -> Speaker.speak(R.string.ask_task_status, listenerLabel)
            EnumStatus.WAITING_CONFIRMATION -> Speaker.speak(R.string.response_confirm_exit, listenerLabel, true)
            else -> Speaker.speak(R.string.response_how_can_help, listenerLabel)
        }
    }
}