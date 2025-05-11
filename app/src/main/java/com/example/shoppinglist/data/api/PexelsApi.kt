package com.example.shoppinglist.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PexelsApi {
    private const val BASE_URL = "https://api.pexels.com/v1/"


    private const val API_KEY = "ZgyLcA7fhTDPU8CbCfDrP8lmP9PWYZ2ktEAAEZkyqQQNqUc4AL37Moo1"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", API_KEY)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: PexelsApiService = retrofit.create(PexelsApiService::class.java)
}