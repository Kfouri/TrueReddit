package com.kfouri.truereddit.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kfouri.truereddit.database.model.Post

@Dao
interface PostReadDao {

    @Query("SELECT * FROM post")
    suspend fun getPostRead(): List<Post>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPostRead(post: Post)

}