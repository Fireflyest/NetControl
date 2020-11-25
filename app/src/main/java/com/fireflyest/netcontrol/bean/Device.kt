package com.fireflyest.netcontrol.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Device (
    @PrimaryKey var address: String,
    var name: String?,
    var alias: String?,
    var desc: String?,
    var service: String?,
    var char: String?,
    var display: Boolean?,
    var time: Long = 0
){
    constructor() : this("", "", "", "", "", "", false, 0)
}