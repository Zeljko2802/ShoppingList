package com.example.shoppinglist.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface PexelsApiService {
    @GET("search")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 1,
        @Query("page") page: Int = 1
    ): PexelsResponse
}