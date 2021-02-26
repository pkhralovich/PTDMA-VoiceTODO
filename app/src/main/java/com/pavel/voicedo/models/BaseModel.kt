package com.pavel.voicedo.models

abstract class BaseModel {
    var id: Int

    constructor() {
        id = -1
    }

    constructor(id: Int) {
        this.id = id
    }
}