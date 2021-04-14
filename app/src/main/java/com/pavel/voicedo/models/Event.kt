package com.pavel.voicedo.models

import org.joda.time.DateTime
import java.util.*

class Event : BaseTask {
    var date: Date

    constructor() {
        this.date = Date()
    }

    constructor (description: String, date: DateTime) : super(EnumTypes.EVENT, description) {
        this.date = date.toDate()
    }

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