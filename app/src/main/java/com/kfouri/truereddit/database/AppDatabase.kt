package com.kfouri.truereddit.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kfouri.truereddit.database.dao.PostReadDao
import com.kfouri.truereddit.database.model.Post

@Database(entities = [Post::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postReadDao(): PostReadDao
}