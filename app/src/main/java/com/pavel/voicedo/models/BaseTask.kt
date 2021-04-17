package com.pavel.voicedo.models

import com.pavel.voicedo.voice.NumberToWords

abstract class BaseTask() : BaseModel() {
    enum class EnumTypes(val value:Int) {
        UNDEFINED(-1), TASK(0), EVENT(1), LIST(2)
    }

    var type: EnumTypes = EnumTypes.UNDEFINED
    var description: String = ""

    constructor(type: EnumTypes) : this() {
        this.type = type
        description = ""
    }

    companion object {
        fun getTask(items: List<BaseTask>, key: String) : Task? {
            items.forEach {
                if (it.type == EnumTypes.TASK) {
                    val currentKey = NumberToWords.convert(it.id.toLong())
                    if (currentKey.equals(key, ignoreCase = true) || it.id.toString().equals(key, ignoreCase = true))
                        return it as Task
                }
            }

            return null
        }

        fun getEvent(items: List<BaseTask>, key: String) : Event? {
            items.forEach {
                if (it.type == EnumTypes.EVENT) {
                    val currentKey = NumberToWords.convert(it.id.toLong())
                    if (currentKey.equals(key, ignoreCase = true) || it.id.toString().equals(key, ignoreCase = true))
                        return it as Event
                }
            }

            return null
        }

        fun getList(items: List<BaseTask>, key: String) : ShoppingList? {
            items.forEach {
                if (it.type == EnumTypes.LIST) {
                    if (it.description.equals(key, ignoreCase = true) || it.id.toString().equals(key, ignoreCase = true))
                        return it as ShoppingList
                }
            }

            return null
        }
    }
}