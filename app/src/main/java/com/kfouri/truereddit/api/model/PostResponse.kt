package com.kfouri.truereddit.api.model

import com.google.gson.annotations.SerializedName

data class PostResponse(
    @SerializedName("data") val data: Data,
)

data class Data(
    @SerializedName("after") val after: String,
    @SerializedName("children") val children: List<Children>,
)

data class Children(
    @SerializedName("data") val dataChildren: DataChildren,
)

data class DataChildren(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("author_fullname") val authorFullName: String,
    @SerializedName("created") val created: Long,
    @SerializedName("num_comments") val numComments: Int,
    @SerializedName("thumbnail") val thumbnail: String?,
    var isRead: Boolean = false
)