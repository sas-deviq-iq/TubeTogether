package com.example.tubetogether.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TubeApi {



    @GET("api/android/newlyVideosItems/level/0/offset/{offset}/")
    suspend fun getNewlyVideos(@Path("offset") offset: Int): List<VideoResponse>

    @GET("api/android/videoGroups")
    suspend fun getVideoGroups(): VideoGroupResponse

    @GET("api/android/AdvancedSearch")
    suspend fun searchVideos(
        @Query("videoTitle") query: String,
        @Query("type") type: String,
        @Query("staffTitle") staffTitle: String = query,
        @Query("level") level: String = "0",
        @Query("page") page: String = "0",
        @Query("year") year: String = "1900,2026",
        @Query("categories") categories: String? = null
    ): List<VideoResponse>

    @GET("api/android/AdvancedSearch")
    suspend fun searchVideosByNb(@Query("nb") nb: String): List<VideoResponse>

    @GET("api/android/allVideoInfo/id/{id}")
    suspend fun getVideoDetails(@Path("id") id: String): VideoResponse

    @GET("api/android/videoSeason/id/{id}")
    suspend fun getSeriesEpisodes(@Path("id") id: String): List<VideoResponse>

    @GET("api/android/transcoddedFiles/id/{id}")
    suspend fun getStreamSources(@Path("id") id: String): List<StreamSourceResponse>

    @GET("api/android/banner/level/0")
    suspend fun getBanners(): List<BannerResponse>

    @GET("api/android/videoSeason/id/{id}")
    suspend fun getSeasons(@Path("id") id: String): List<SeasonResponse>
}
