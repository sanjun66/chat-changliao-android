package com.legend.common.db.dao

import androidx.room.*
import com.legend.common.db.entity.DBEntity

/**
 * 消息聊天列表
 */
@Dao
interface ChatListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg chatEntity: DBEntity.ChatListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chatList: List<DBEntity.ChatListEntity>)

    @Update
    suspend fun update(vararg chatEntity: DBEntity.ChatListEntity)

    @Delete
    suspend fun delete(vararg chatEntity: DBEntity.ChatListEntity)

    @Query("SELECT * FROM chat_list ORDER BY timestamp DESC")
    suspend fun getAllChatList(): List<DBEntity.ChatListEntity>?

    @Query("SELECT * FROM chat_list WHERE session_id = :sessionId")
    suspend fun getItemBySessionId(sessionId: String): DBEntity.ChatListEntity?

    @Query("DELETE FROM chat_list WHERE session_id = :sessionId")
    suspend fun deleteItemBySessionId(sessionId: String)

    @Query("SELECT * FROM chat_list WHERE message_id = :messageId")
    suspend fun getItemByMessageId(messageId: String): DBEntity.ChatListEntity?

}