package com.pavel.voicedo.models

import java.io.Serializable

abstract class BaseModel : Serializable {
    var id: Int

    constructor() {
        id = -1
    }

    constructor(id: Int) {
        this.id = id
    }
}