package com.fireflyest.netcontrol.dao

import androidx.room.*
import com.fireflyest.netcontrol.bean.Device


@Dao
interface DeviceDao {

    @Query("SELECT * FROM device")
    fun queryAll(): Array<Device>

    @Query("SELECT * FROM device WHERE address LIKE :address LIMIT 1")
    fun findByAddress(address: String): Device

    @Update
    fun updateAll(vararg devices: Device)

    @Insert
    fun insert(device: Device)

    @Delete
    fun delete(device: Device)
}