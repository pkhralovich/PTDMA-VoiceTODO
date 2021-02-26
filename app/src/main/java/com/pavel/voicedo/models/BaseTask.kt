package com.pavel.voicedo.models

class BaseTask : BaseModel {
    enum class eTypes {
        UNDEFINED, TASK, EVENT, LIST
    }

    var type: eTypes

    constructor() {
        id = -1
        type = eTypes.UNDEFINED
    }

    constructor(id: Int, type: eTypes) {
        this.id = id
        this.type = type
    }
}