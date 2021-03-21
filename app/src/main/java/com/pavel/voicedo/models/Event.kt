package com.pavel.voicedo.models

import org.joda.time.DateTime

class Event : BaseTask {
    var date: DateTime

    constructor (id: Int, description: String, date: DateTime) : super(id, eTypes.EVENT, description) {
        this.date = date;
    }

    public fun getStringDate(): String {
        return "${date.toString("DD")}\n${date.toString("MMM")}"
    }
}