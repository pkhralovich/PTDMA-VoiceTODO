package com.pavel.voicedo.models

import org.joda.time.DateTime

class Event : BaseTask {
    var date: DateTime

    constructor (id: Int, description: String, date: DateTime) : super(id, eTypes.EVENT, description) {
        this.date = date;
    }

    fun getStringDate(): String {
        return "${date.toString("dd")}\n${date.toString("MMM")}"
    }

    fun getStringLongDate() : String {
        return date.toString("dd.MM.YYYY")
    }

    fun getStringTime() : String {
        return date.toString("HH:mm")
    }
}