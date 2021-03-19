package com.pavel.voicedo.models

abstract class BaseTask : BaseModel {
    enum class eTypes {
        UNDEFINED, TASK, EVENT, LIST
    }

    var type: eTypes
    var description: String

    constructor() {
        id = -1
        type = eTypes.UNDEFINED
        description = ""
    }

    constructor(id: Int, type: eTypes, description: String) {
        this.id = id
        this.type = type
        this.description = description
    }
}