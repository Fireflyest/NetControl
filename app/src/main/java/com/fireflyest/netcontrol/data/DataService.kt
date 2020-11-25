package com.fireflyest.netcontrol.data

import android.content.Context
import androidx.room.Room
import com.fireflyest.netcontrol.dao.CommandDao
import com.fireflyest.netcontrol.dao.DeviceDao
import com.fireflyest.netcontrol.dao.QuickDao


class DataService private constructor() {
    private var appDatabase: AppDatabase? = null

    @Synchronized
    fun initDataService(context: Context) {
        if (null != appDatabase) return
        Thread(Runnable {
            appDatabase = Room.databaseBuilder(
                context,
                AppDatabase::class.java, database
            )
                .fallbackToDestructiveMigration()
                .build()
        }).start()
    }

    val deviceDao: DeviceDao
        get() = appDatabase!!.deviceDao()

    val commandDao: CommandDao
        get() = appDatabase!!.commandDao()

    val quickDao: QuickDao
        get() = appDatabase!!.quickDao()

    companion object {
        val instance = DataService()

        private const val database = "net-control"
    }
}