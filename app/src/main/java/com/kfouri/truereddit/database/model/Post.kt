package com.kfouri.truereddit.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Post(
    @PrimaryKey val id: String
)