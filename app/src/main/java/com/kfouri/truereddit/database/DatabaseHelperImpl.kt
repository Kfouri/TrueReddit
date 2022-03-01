package com.kfouri.truereddit.database

import com.kfouri.truereddit.database.model.Post

class DatabaseHelperImpl(private val appDatabase: AppDatabase): DatabaseHelper {

    override suspend fun getPostRead(): List<Post> = appDatabase.postReadDao().getPostRead()

    override suspend fun insertPostRead(post: Post) = appDatabase.postReadDao().insertPostRead(post)
}