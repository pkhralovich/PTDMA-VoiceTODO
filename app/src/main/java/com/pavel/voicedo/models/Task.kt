package com.pavel.voicedo.models

import android.content.Context
import com.pavel.voicedo.R
import org.joda.time.DateTime
import java.util.*

class Task : BaseTask {
    enum class EnumTaskState {
        UNDEFINED, TODO, DOING, DONE
    }

    var state : EnumTaskState
    private var stateDate : Date = Date()

    constructor() {
        state = EnumTaskState.UNDEFINED
        stateDate = DateTime.now().toDate()
    }

    constructor(description: String, state: EnumTaskState) : super(EnumTypes.TASK, description) {
        this.state = state
    }

    fun getStringState(c: Context) : String{
        return when (state) {
            EnumTaskState.UNDEFINED -> ""
            EnumTaskState.TODO ->  c.resources.getString(R.string.state_todo)
            EnumTaskState.DOING -> c.resources.getString(R.string.state_doing)
            EnumTaskState.DONE -> c.resources.getString(R.string.state_done)
        }
    }

    fun getStringDate() : String {
        return DateTime(stateDate).toString("dd.MM.YYYY HH:mm:ss")
    }
}