package com.pavel.voicedo.models

import com.orm.query.Condition
import com.orm.query.Select

class Product : BaseModel {
    var list : Long = 0
    var description : String = ""
    var bought : Boolean = false

    constructor() {
        this.list = 0
        this.description = ""
        this.bought = false
    }

    constructor(description: String, bought: Boolean) : super() {
        this.description = description
        this.bought = bought
    }

    companion object {
        fun getByList(list_id: Long): List<Product> {
            return Select.from(Product::class.java)
                .where(
                    Condition.prop("list")
                        .eq(list_id)
                )
                .list()
        }
    }
}