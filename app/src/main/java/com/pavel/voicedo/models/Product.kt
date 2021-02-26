package com.pavel.voicedo.models

class Product : BaseModel {
    var description : String = ""
    var bought : Boolean = false

    constructor(description: String, bought: Boolean) : super() {
        this.description = description
        this.bought = bought
    }

    constructor(id: Int, description: String, bought: Boolean) : super(id) {
        this.description = description
        this.bought = bought
    }
}