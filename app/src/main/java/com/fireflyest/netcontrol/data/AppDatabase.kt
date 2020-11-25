package com.fireflyest.netcontrol.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fireflyest.netcontrol.bean.Command
import com.fireflyest.netcontrol.bean.Device
import com.fireflyest.netcontrol.bean.Quick
import com.fireflyest.netcontrol.dao.CommandDao
import com.fireflyest.netcontrol.dao.DeviceDao
import com.fireflyest.netcontrol.dao.QuickDao


@Database(
    entities = [Command::class, Device::class, Quick::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
    abstract fun deviceDao(): DeviceDao
    abstract fun quickDao(): QuickDao
}