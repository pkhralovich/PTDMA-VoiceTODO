package com.pavel.voicedo.models

import android.content.Context
import com.pavel.voicedo.R
import org.joda.time.DateTime

class Task : BaseTask {
    enum class eTaskState {
        UNDEFINED, TODO, DOING, DONE
    }

    var state : eTaskState
    var state_date : DateTime = DateTime.now()

    constructor(id: Int, description: String, state: eTaskState) : super(id, eTypes.TASK, description) {
        this.state = state
    }

    fun getStringState(c: Context) : String{
        return when (state) {
            eTaskState.UNDEFINED -> ""
            eTaskState.TODO ->  c.resources.getString(R.string.state_todo)
            eTaskState.DOING -> c.resources.getString(R.string.state_doing)
            eTaskState.DONE -> c.resources.getString(R.string.state_done)
        }
    }

    fun getStringDate() : String {
        return this.state_date.toString("dd.MM.YYYY HH:mm:ss")
    }
}