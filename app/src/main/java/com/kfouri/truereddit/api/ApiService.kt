package com.kfouri.truereddit.api

import com.kfouri.truereddit.api.model.PostResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {

    @GET("top.json?limit=50&t=day")
    suspend fun getPosts(@Query("after") after: String): PostResponse
}