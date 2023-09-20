package com.legend.common.db.dao

import androidx.room.*
import com.legend.common.db.entity.DBEntity

@Dao
interface UserSimpleInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg avatarInfo: DBEntity.UserSimpleInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(avatarInfoList: List<DBEntity.UserSimpleInfo>)

    @Update
    suspend fun update(vararg avatarInfo: DBEntity.UserSimpleInfo)

    @Delete
    suspend fun delete(vararg avatarInfo: DBEntity.UserSimpleInfo)

    @Query("SELECT * FROM user_simple_info")
    suspend fun getAll(): List<DBEntity.UserSimpleInfo>?

    @Query("SELECT * FROM user_simple_info WHERE uid = :strUid")
    suspend fun get(strUid: String): DBEntity.UserSimpleInfo?

    @Query("DELETE FROM user_simple_info WHERE uid = :strUid")
    suspend fun deleteItemById(strUid: String)
}