package com.pavel.voicedo.models

import com.orm.query.Condition
import com.orm.query.Select

class Product() : BaseModel() {
    var list : Long = 0
    var description : String = ""
    var bought : Boolean = false

    constructor(description: String, bought: Boolean) : this() {
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

        fun find(list: List<Product>, name: String) : Product? {
            var oRes : Product? = null
            list.forEach {
                if (it.description.equals(name, ignoreCase = true)) {
                    oRes = it
                    return oRes
                }
            }

            return oRes
        }
    }
}