package com.fireflyest.netcontrol.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Quick (

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var command: String?,
    var hex: Boolean?

)
