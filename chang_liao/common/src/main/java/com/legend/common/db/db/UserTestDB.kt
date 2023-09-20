package com.legend.common.db.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.legend.common.db.dao.UserTestDao
import com.legend.common.db.entity.UserTest

/**
 * UserDB 用于保存数据库
 * 该类必须@Database注解修饰，该注解包含列出所有与数据库关联的数据实体的entities数组
 * 该类必须是一个抽象类，用于扩展RoomDatabase
 * 对于与数据库关联的每个DAO类，数据库必须定义一个具有零参数的抽象方法，并返回DAO类的实例。
 * 注意：每个RoomDatabase实例的成本相当高，应遵循单例设计模式
 */
// 如果没有 exportSchema = false 则：
// Schema export directory is not provided to the annotation processor so we cannot export the schema. You can either provide `room.schemaLocation` annotation processor argument OR set exportSchema to false.
@Database(entities = [UserTest::class], version = 1, exportSchema = false)
abstract class UserTestDB: RoomDatabase() {
    abstract fun userTestDao(): UserTestDao // 返回DAO实例的抽象方法
}