package com.kfouri.truereddit.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private var retrofit: Retrofit? = null

    private const val BASE_URL = "https://www.reddit.com/"

    val apiService: APIService?
        get() = getClient()?.create(APIService::class.java)

    private fun getClient(): Retrofit? {

        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .build()
            chain.proceed(newRequest)
        }.build()

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }
}