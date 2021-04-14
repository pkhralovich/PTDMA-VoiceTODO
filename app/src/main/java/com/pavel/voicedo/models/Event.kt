package com.pavel.voicedo.models

import org.joda.time.DateTime
import java.util.*

class Event : BaseTask(EnumTypes.EVENT) {
    var date: Date = Date()

    fun getStringDate(): String {
        return "${DateTime(date).toString("dd")}\n${DateTime(date).toString("MMM")}"
    }

    fun getStringLongDate() : String {
        return DateTime(date).toString("dd.MM.YYYY")
    }

    fun getStringTime() : String {
        return DateTime(date).toString("HH:mm")
    }
}