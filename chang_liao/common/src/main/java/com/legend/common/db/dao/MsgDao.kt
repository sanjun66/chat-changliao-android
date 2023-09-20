package com.legend.common.db.dao

import androidx.room.*
import com.legend.common.db.entity.DBEntity

@Dao
interface MsgDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg msg: DBEntity.ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(msgList: List<DBEntity.ChatMessageEntity>)

    @Update
    suspend fun update(vararg msg: DBEntity.ChatMessageEntity)

    @Delete
    suspend fun delete(vararg msg: DBEntity.ChatMessageEntity)

    @Query("SELECT * FROM chat_message WHERE id = :msgId")
    suspend fun getMsgById(msgId: String): DBEntity.ChatMessageEntity?

    @Query("SELECT * FROM chat_message WHERE session_id = :sessionId ORDER BY timestamp DESC,id DESC")
    suspend fun getAllMsgFromSessionId(sessionId: String): List<DBEntity.ChatMessageEntity>?

    // 单聊
    @Query("SELECT * FROM chat_message WHERE (talk_type = 1 AND from_uid = :fromUid AND to_uid = :toUid) " +
            "OR (talk_type = 1 AND from_uid = :toUid AND to_uid = :fromUid) " +
            "ORDER BY id DESC,timestamp DESC LIMIT :fromIndex,:toIndex")
    suspend fun getMsgList(toUid: Int, fromUid: Int, fromIndex: Int, toIndex: Int): List<DBEntity.ChatMessageEntity>?

    @Query("SELECT * FROM chat_message WHERE session_id = :sessionId ORDER BY timestamp DESC,id DESC LIMIT :fromIndex,:toIndex")
    suspend fun getMsgFromSessionId(sessionId: String, fromIndex: Int, toIndex: Int): List<DBEntity.ChatMessageEntity>?

    @Query("DELETE FROM chat_message WHERE session_id = :sessionId")
    suspend fun deleteMsgBySessionId(sessionId: String)

    @Query("SELECT * FROM chat_message WHERE session_id = :sessionId AND is_read = 0 AND to_uid = :receiveUid ORDER BY timestamp DESC")
    suspend fun getReceivedMsgUnread(sessionId: String, receiveUid: String): List<DBEntity.ChatMessageEntity>?

    @Query("SELECT * FROM chat_message WHERE session_id = :sessionId AND is_read = 0 AND from_uid <> :selfUid ORDER BY timestamp DESC")
    suspend fun getReceivedGroupMsgUnread(sessionId: String, selfUid: String): List<DBEntity.ChatMessageEntity>?

    @Query("UPDATE chat_message SET is_read = 1 WHERE session_id = :sessionId AND to_uid = :receiveUid AND is_read = 0")
    suspend fun updateReceivedMsgRead(sessionId: String, receiveUid: String)

    @Query("UPDATE chat_message SET is_read = 1 WHERE session_id = :sessionId AND from_uid <> :selfUid AND is_read = 0")
    suspend fun updateReceivedGroupMsgRead(sessionId: String, selfUid: String)

    @Query("UPDATE chat_message SET is_read = 1 WHERE id in (:ids)")
    suspend fun updateSendMsgReadList(ids: List<String>): Int

    @Query("SELECT * FROM chat_message WHERE id in (:ids)")
    suspend fun getSpecifyIdMsg(ids: List<String>): List<DBEntity.ChatMessageEntity>?

}