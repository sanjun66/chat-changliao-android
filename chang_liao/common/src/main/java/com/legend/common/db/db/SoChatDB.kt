package com.legend.common.db.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.legend.common.db.dao.ChatListDao
import com.legend.common.db.dao.MsgDao
import com.legend.common.db.dao.UserSimpleInfoDao
import com.legend.common.db.entity.DBEntity

@Database(entities = [DBEntity.ChatMessageEntity::class
    , DBEntity.ChatListEntity::class
    , DBEntity.UserSimpleInfo::class]
    , version = 2, exportSchema = false)
@TypeConverters(com.legend.common.db.TypeConverters::class)
abstract class SoChatDB : RoomDatabase(){
    abstract fun msgDao(): MsgDao

    abstract fun chatListDao(): ChatListDao

    abstract fun userAvatarDao(): UserSimpleInfoDao
}