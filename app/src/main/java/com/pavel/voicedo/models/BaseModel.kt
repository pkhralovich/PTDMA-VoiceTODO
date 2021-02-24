package com.pavel.voicedo.models

abstract class BaseModel {
    enum class eTypes {
        UNDEFINED, TASK, EVENT, LIST
    }

    var id: Int
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