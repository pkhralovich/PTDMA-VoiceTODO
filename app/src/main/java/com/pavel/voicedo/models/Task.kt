package com.pavel.voicedo.models

class Task : BaseTask {
    enum class eTaskState {
        UNDEFINED, TODO, DOING, DONE
    }

    var state : eTaskState

    constructor(id: Int, description: String, state: eTaskState) : super(id, eTypes.TASK, description) {
        this.state = state
    }
}