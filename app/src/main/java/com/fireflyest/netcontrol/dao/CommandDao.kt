package com.fireflyest.netcontrol.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.fireflyest.netcontrol.bean.Command


@Dao
interface CommandDao {
    @Query("SELECT * FROM command")
    fun queryAll(): Array<Command>

    @Query("SELECT * FROM command WHERE id IN (:id)")
    fun loadAllByIds(id: IntArray): Array<Command>

    @Query("SELECT * FROM command WHERE time > :time")
    fun findByTime(time: Long): Array<Command>

    @Query("SELECT * FROM command WHERE address like :address")
    fun findByAddress(address: String): Array<Command>

    @Insert
    fun insertAll(vararg command: Command)

    @Delete
    fun delete(command: Command)
}