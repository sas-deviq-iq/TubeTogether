package com.example.tubetogether.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TubeApi {

    @GET("groups")
    suspend fun getVideoGroups(): VideoGroupResponse

    @GET("search")
    suspend fun searchVideos(
        @Query("query") query: String,
        @Query("type") type: String // "movies" or "series"
    ): List<VideoResponse>

    @GET("details/{id}")
    suspend fun getVideoDetails(@Path("id") id: String): VideoResponse

    @GET("episodes/{tvId}")
    suspend fun getSeriesEpisodes(@Path("tvId") tvId: String): List<VideoResponse>
}
