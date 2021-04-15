package com.pavel.voicedo.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ListenableActivity
import com.pavel.voicedo.models.Event
import com.pavel.voicedo.voice.ActionParser
import com.pavel.voicedo.voice.Speaker

class EventActivity : ListenableActivity() {
    companion object {
        enum class EnumStatus {
            VIEW, SAY_NAME, SAY_DATE, WAITING_CONFIRMATION
        }
    }

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.input_description)
    lateinit var inputDescription: TextView

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.input_time)
    lateinit var inputTime: TextView

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.input_date)
    lateinit var inputDate: TextView

    private lateinit var event : Event
    private var status : EnumStatus = EnumStatus.VIEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_event)
        ButterKnife.bind(this)

        if (intent.hasExtra(MainActivity.PARAMS.EVENT)) {
            event = intent.getSerializableExtra(MainActivity.PARAMS.EVENT) as Event
            event.id = intent.getLongExtra(MainActivity.PARAMS.EVENT_ID, -1)
        } else {
            event = Event()
            status = EnumStatus.VIEW
        }
    }

    override fun onInit(status: Int) {
        if (Speaker.onInit(status)) {
            updateUI()
        }
    }

    override fun onNoOrderFound() {
        //TODO: Afegir quan la data esta o no buida
        if (this.isWaitingInput() && event.description.isNotEmpty())
            this.status = EnumStatus.VIEW
        super.onNoOrderFound()
    }

    override fun onBackPressed() {
        if (event.id == null && event.description.isNotEmpty() && this.status != EnumStatus.WAITING_CONFIRMATION) {
            this.status = EnumStatus.WAITING_CONFIRMATION
            showListenable(false)
            Speaker.speak(R.string.response_confirm_exit, listenerLabel, true)
        } else super.onBackPressed()
    }

    override fun getHelpText(): List<String> {
        val list : ArrayList<String> = arrayListOf()
        when (status) {
            EnumStatus.VIEW -> {
                list.add(resources.getString(R.string.remove_event_help))
                list.add(resources.getString(R.string.edit_event_name_help))
                list.add(resources.getString(R.string.edit_event_date_help))
                list.add(resources.getString(R.string.edit_event_back_help))
            }
            EnumStatus.WAITING_CONFIRMATION -> {
                list.add(resources.getString(R.string.confirmation_help))
                list.add(resources.getString(R.string.cancelation_help))
            }
            EnumStatus.SAY_NAME -> list.add(resources.getString(R.string.say_event_name_help))
            else -> list.add(resources.getString(R.string.say_event_date_help))
        }
        return list
    }

    override fun onResult(action: ActionParser.Action) {
        if (this.status == EnumStatus.WAITING_CONFIRMATION) {
            when (action.action) {
                ActionParser.Action.ActionType.CONFIRMATION -> this.finish()
                else -> {
                    this.status = EnumStatus.VIEW
                    updateUI()
                }
            }
        } else {
            when (action.action) {
                ActionParser.Action.ActionType.CHANGE_TASK_NAME -> {
                    status = EnumStatus.SAY_NAME
                    updateUI()
                }
                ActionParser.Action.ActionType.CHANGE_TASK_STATUS -> {
                    status = EnumStatus.SAY_DATE
                    updateUI()
                }
                ActionParser.Action.ActionType.FINISH_EDITION -> {
                    event.save()
                    status = EnumStatus.VIEW
                    Speaker.speak(R.string.event_saved, null, false)
                    hideListenable()
                    updateUI()
                }
                ActionParser.Action.ActionType.BACK,
                ActionParser.Action.ActionType.CANCELLATION -> {
                    //TODO
                }
                ActionParser.Action.ActionType.INPUT -> {
                    when (status) {
                        EnumStatus.SAY_NAME -> onInputName(action)
                        EnumStatus.SAY_DATE -> onInputDate(action)
                        else -> onInvalidAction()
                    }
                }
                else -> onInvalidAction()
            }
        }
    }

    fun onInputName(action: ActionParser.Action) {
        //TODO: Completar
    }

    fun onInputDate(action: ActionParser.Action) {
        //TODO: Completar
    }

    override fun updateUI() {
        inputDescription.text = event.description
        inputTime.text = event.getStringTime()
        inputDate.text = event.getStringLongDate()

        //TODO: Completar
    }

    override fun isWaitingInput() : Boolean {
        return status != EnumStatus.VIEW && status != EnumStatus.WAITING_CONFIRMATION
    }

    override fun hasCustomTitle() : Boolean {
        return true
    }

    override fun getTitleResource() : Int {
        return if (event.id != null) R.string.view_event
        else R.string.create_event
    }

    override fun onClickListen() {
        showListenable(true)
        when (this.status) {
            EnumStatus.SAY_NAME -> Speaker.speak(R.string.ask_event_name, listenerLabel)
            EnumStatus.SAY_DATE -> Speaker.speak(R.string.ask_event_date, listenerLabel)
            EnumStatus.WAITING_CONFIRMATION -> Speaker.speak(R.string.response_confirm_exit, listenerLabel, true)
            else -> Speaker.speak(R.string.response_how_can_help, listenerLabel)
        }
    }
}