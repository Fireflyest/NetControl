package com.fireflyest.netcontrol.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Quick (

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var num: Int = 0,
    var type: Int = 0,
    var colorR: Int = 255,
    var colorG: Int = 255,
    var colorB: Int = 255,
    var command: String?,
    var display: String?

)
