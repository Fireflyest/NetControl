package com.fireflyest.netcontrol.bean

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * 指令发送与接收记录
 */
@Entity
class Command {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var address: String? = null
    var time: Long = 0
    var type: String? = null
    var text: String? = null
    var isSuccess = false

}