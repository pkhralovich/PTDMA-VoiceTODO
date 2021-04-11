package com.pavel.voicedo.models

import com.orm.SugarRecord
import java.io.Serializable
import java.util.*

abstract class BaseModel : SugarRecord(), Serializable {
    var creationDate : Date = Date()
}