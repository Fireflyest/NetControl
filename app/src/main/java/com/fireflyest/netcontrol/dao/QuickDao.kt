package com.fireflyest.netcontrol.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.fireflyest.netcontrol.bean.Quick


@Dao
interface QuickDao {

    @Query("SELECT * FROM quick")
    fun queryAll(): Array<Quick>

    @Query("SELECT * FROM quick WHERE id IN (:id)")
    fun loadAllByIds(id: IntArray): Array<Quick>

    @Insert
    fun insertAll(vararg quick: Quick)

    @Delete
    fun delete(quick: Quick)
}