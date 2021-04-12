package com.pavel.voicedo.activities

import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ListenableActivity
import com.pavel.voicedo.models.Event
import com.pavel.voicedo.voice.ActionParser

class EventActivity : ListenableActivity() {
    lateinit var event : Event

    @BindView(R.id.input_description)
    lateinit var input_description: TextView
    @BindView(R.id.input_time)
    lateinit var input_time: TextView
    @BindView(R.id.input_date)
    lateinit var input_date: TextView

    enum class eStatus {
        VIEW, EDIT, CREATE
    }

    var status : eStatus = eStatus.VIEW
    override fun getHelpText(): List<String> {
        val list : ArrayList<String> = arrayListOf<String>()
        when (status) {
            eStatus.VIEW -> {
                list.add(resources.getString(R.string.remove_event_help))
                list.add(resources.getString(R.string.edit_event_name_help))
                list.add(resources.getString(R.string.edit_event_date_help))
                list.add(resources.getString(R.string.edit_event_back_help))
            }
            else -> { }
        }
        return list;
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

    fun updateUI() {
        input_description.text = event.description
        input_time.text = event.getStringTime()
        input_date.text = event.getStringLongDate()
    }

    override fun isWaitingInput() : Boolean {
        return false
    }
}