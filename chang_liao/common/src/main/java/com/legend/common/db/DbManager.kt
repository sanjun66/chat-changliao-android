package com.legend.common.db

import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.legend.base.Applications
import com.legend.common.ApplicationConst
import com.legend.common.db.db.SoChatDB
import com.legend.common.db.db.UserTestDB

/**
 * 数据库管理工具
 */
object DbManager {
    private const val TAG = "DbManager"
    private const val userDbName = "userTestDb"
    private const val db_name_format = "user_%s"
    private var soChatDB: SoChatDB? = null

    fun closeSoChatDB() {
        soChatDB?.close()
        soChatDB = null
    }

    @Synchronized
    fun getSoChatDB(): SoChatDB {
        if (soChatDB != null) return soChatDB!!

        return Room.databaseBuilder(Applications.getCurrent().applicationContext
            , SoChatDB::class.java, String.format(db_name_format, ApplicationConst.getUserId()))
            .addCallback(DbCreateCallBack)
            .fallbackToDestructiveMigration()   // 如果没有提供足够的迁移来从当前版本移动到最新版本，Room 将清除数据库并重新创建
            .addMigrations(MIGRATION_1_2)
//            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    // 同时修改SoChatDB 的version
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE chat_list ADD COLUMN `message_type` INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE user_simple_info ADD COLUMN `is_disturb` INTEGER NOT NULL DEFAULT 0")
        }

    }









    val userTestDB: UserTestDB by lazy {
        Room.databaseBuilder(Applications.getCurrent().applicationContext
            , UserTestDB::class.java, userDbName)
            .addCallback(DbCreateCallBack)  // 增加回调监听
//            .addMigrations(UserTestMigration1to2)      // 增加数据迁移
            .build()
    }

    private object DbCreateCallBack: RoomDatabase.Callback() {
        //第一次创建数据库时调用
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
//            Log.d(TAG, "first onCreate db name: ($db.name), db version: $db.version")
            Log.e(TAG, "first onCreate db version: " + db.version)
        }
    }

    /**
     * 数据库升级
     * 如果更改了数据库的架构，但没有更新数据库的版本 --- app崩溃
     * 如果更新了数据库版本，但没有提供任何迁移策略   --- app崩溃
     * https://blog.csdn.net/u013762572/article/details/106315045
     */
    private object UserTestMigration1to2 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            Log.e(TAG, "执行数据库升级: ")
            // user表新增一个last_update 字段
//            database.execSQL("ALTER TABLE users ADD COLUMN last_update INTEGER");
        }
    }



}