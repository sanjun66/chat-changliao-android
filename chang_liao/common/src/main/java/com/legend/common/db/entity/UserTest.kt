package com.legend.common.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 *  一个Entity实例代表一个表
 *  1、@Entity：Room实体类注解 ---> 默认情况下，Room 将【类名称】用作数据库【表名称】
 *      注:如果您希望【数据库表】具有不同的名称，请设置 @Entity 注解的 tableName 属性
 *  2、PrimaryKey: 主键（ autoGenerate = true 主键自增 ），作用：唯一标识相应数据库表中的每一行
 *  3、@ColumnInfo：数据库表字段---> 默认情况下，使用【字段名称】作为数据库中的【列名称】
 *      注：如果您希望【数据表里的列】具有不同的名称，请将 @ColumnInfo 注解添加到该字段并设置 name 属性
 *  4、Ignore: 默认情况下，Room 会为实体中定义的每个字段创建一个列。 如果您不想保留该字段，则可以使用 @Ignore 为字段添加注解
 */
@Entity/*(tableName = "loginUser")*/
data class UserTest (
    @PrimaryKey var uid: Int = 0,
    @ColumnInfo(name = "fist_name") var firstName: String? = "",
    @ColumnInfo(name = "last_name") var lastName: String? = "",
    @Ignore var avatar: String? = ""
    )