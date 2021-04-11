package com.pavel.voicedo.models

import com.pavel.voicedo.voice.NumberToWords

abstract class BaseTask : BaseModel {
    enum class eTypes(val value:Int) {
        UNDEFINED(-1), TASK(0), EVENT(1), LIST(2)
    }

    var type: eTypes
    var description: String

    constructor() : super(){
        id = -1
        type = eTypes.UNDEFINED
        description = ""
    }

    constructor(type: eTypes, description: String) : super() {
        this.type = type
        this.description = description
    }

    companion object {
        fun getTask(items: List<BaseTask>, key: String) : Task? {
            items.forEach {
                if (it.type == eTypes.TASK) {
                    val currentKey = NumberToWords.convert(it.id.toLong())
                    if (currentKey.equals(key, ignoreCase = true)) return it as Task
                }
            }

            return null
        }

        fun getEvent(items: List<BaseTask>, key: String) : Event? {
            items.forEach {
                if (it.type == eTypes.EVENT) {
                    val currentKey = NumberToWords.convert(it.id.toLong())
                    if (currentKey.equals(key, ignoreCase = true)) return it as Event
                }
            }

            return null
        }

        fun getList(items: List<BaseTask>, key: String) : ShoppingList? {
            items.forEach {
                if (it.type == eTypes.EVENT) {
                    if (it.description.equals(key, ignoreCase = true))
                        return it as ShoppingList
                }
            }

            return null
        }
    }
}