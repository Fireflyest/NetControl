package com.fireflyest.netcontrol.dao

import androidx.room.*
import com.fireflyest.netcontrol.bean.Quick


@Dao
interface QuickDao {

    @Query("SELECT * FROM quick")
    fun queryAll(): Array<Quick>

    @Query("SELECT * FROM quick WHERE id IN (:id)")
    fun loadById(id: Int): Quick

    @Query("SELECT * FROM quick WHERE num IN (:num)")
    fun loadByNum(num: Int): Quick

    @Insert
    fun insertAll(vararg quick: Quick)

    @Update
    fun update(quick: Quick)

    @Delete
    fun delete(quick: Quick)
}