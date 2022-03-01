package com.kfouri.truereddit.database

import com.kfouri.truereddit.database.model.Post

interface DatabaseHelper {

    suspend fun getPostRead(): List<Post>

    suspend fun insertPostRead(post: Post)
}