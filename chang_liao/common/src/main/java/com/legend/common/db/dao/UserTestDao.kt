package com.legend.common.db.dao

import androidx.room.*
import com.legend.common.db.entity.UserTest

/**
 * 使用@Dao注解，定义为一个接口或者抽象类。DAO不具有属性，但他们定义了一个或多个方法，可用于与应用数据库中的数据进行交互
 */
@Dao
interface UserTestDao {
    /**
     * @insert 可为1个参数;可为多个参数;可变参数（多个）
     */
    @Insert
    suspend fun insetAll(userTest: List<UserTest>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(vararg userTests: UserTest)

    @Insert
    suspend fun insertBothUser(userTest1: UserTest, userTest2: UserTest)

    @Insert
    suspend fun insertUserAndFriends(userTest: UserTest, friends: List<UserTest>)

    /**
     * Room使用主键传递的实体实例与数据库中的行进行匹配，佩佩上后才会进行更改
     */
    @Update
    suspend fun updateUser(vararg userTest: UserTest)

    @Delete
    suspend fun deleteUser(vararg userTest: UserTest)

    /**
     * 借助@Query注解，可以便携SQL语句并将其作为DAO的方法公开。使用这些查询方法从应用的数据库查询数据，或者需要执行复杂的插入、更新和删除操作。
     * Room会在变异时验证SQL查询。这意味着，如果查询出现问题，则会出现编译错误，而不是运行时失败。
     */
    @Query("SELECT * FROM userTest")
    suspend fun getAllUser(): MutableList<UserTest>

    @Query("SELECT * FROM userTest WHERE uid = :uid")
    suspend fun getUserById(uid: Int): UserTest?

    /**
     * 如果是数组或者结合类型参数需要用小括号括起来
     */
    @Query("SELECT * FROM userTest WHERE uid IN (:userIds)")
    suspend fun getUsersByIds(userIds: IntArray): MutableList<UserTest>


    @Query("DELETE FROM userTest WHERE uid = :uid")
    suspend fun deleteUserById(uid: Int)

//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
//    fun getUserByName(first: String, last: String): User?

    /**
     * https://developer.android.com/training/data-storage/room/accessing-data?hl=zh-cn
     *
     * 部分查询可能需要访问多个表才能计算出结果，可以在SQL查询中使用JOIN子句饮用多个表
     * 以下定义了一种方法将三个表联接在一起，以便将当前已出借的图书返回给特定用户
     */
//    @Query(
//        "SELECT * FROM book" +
//                "INNER JOIN loan ON loan.book_id = book.id" +
//                "INNER JOIN user ON user.id = loan.user_id" +
//                "WHTER user.name LIKE :userName"
//    )
//    fun findBooksBorrowedByNameSync(userName: String): List<Book>
}