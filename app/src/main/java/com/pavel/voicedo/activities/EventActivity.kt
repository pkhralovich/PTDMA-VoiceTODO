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

class EventActivity : ListenableActivity() {
    companion object {
        enum class EnumStatus {
            VIEW, EDIT, CREATE
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

    private var event : Event? = null
    private var status : EnumStatus = EnumStatus.VIEW

    override fun getHelpText(): List<String> {
        val list : ArrayList<String> = arrayListOf()
        when (status) {
            EnumStatus.VIEW -> {
                list.add(resources.getString(R.string.remove_event_help))
                list.add(resources.getString(R.string.edit_event_name_help))
                list.add(resources.getString(R.string.edit_event_date_help))
                list.add(resources.getString(R.string.edit_event_back_help))
            }
            else -> { }
            //TODO: Completar
        }
        return list
    }

    override fun onResult(action: ActionParser.Action) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_event)
        ButterKnife.bind(this)

        event = intent.getSerializableExtra(MainActivity.PARAMS.EVENT) as Event

        updateUI()
    }

    override fun updateUI() {
        inputDescription.text = event?.description
        inputTime.text = event?.getStringTime()
        inputDate.text = event?.getStringLongDate()
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