package com.pavel.voicedo.models

import android.content.Context
import com.pavel.voicedo.R
import org.joda.time.DateTime
import java.util.*

class Task : BaseTask(EnumTypes.TASK) {
    companion object {
        enum class EnumTaskState {
            UNDEFINED, TODO, DOING, DONE
        }

        fun getStringState(state: EnumTaskState, c: Context) : String {
            return when (state) {
                EnumTaskState.UNDEFINED -> ""
                EnumTaskState.TODO ->  c.resources.getString(R.string.state_todo)
                EnumTaskState.DOING -> c.resources.getString(R.string.state_doing)
                EnumTaskState.DONE -> c.resources.getString(R.string.state_done)
            }
        }
    }


    var state : EnumTaskState = EnumTaskState.UNDEFINED
    var stateDate : Date = DateTime.now().toDate()

    fun getStringState(c: Context) : String{
        return getStringState(state, c)
    }

    fun getStringDate() : String {
        return DateTime(stateDate).toString("dd.MM.YYYY HH:mm:ss")
    }
}