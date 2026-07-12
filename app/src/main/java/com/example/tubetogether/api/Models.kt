package com.example.tubetogether.api

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class VideoResponse(
    @SerializedName("nb") val nb: String?,
    @SerializedName("videoId") val videoId: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("en_title") val enTitle: String?,
    @SerializedName("ar_title") val arTitle: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("year") val year: String?,
    @SerializedName("kind") val kind: String?, // 2 = series
    @SerializedName("season") val season: String?,
    @SerializedName("episodeNummer") val episodeNumber: String?,
    @SerializedName("imgMediumThumbObjUrl") val posterMedium: String?,
    @SerializedName("imgObjUrl") val posterLarge: String?,
    @SerializedName("ar_content") val arContent: String?,
    @SerializedName("en_content") val enContent: String?,
    @SerializedName("stars") val stars: String?,
    @SerializedName("categories") val categories: List<VideoCategoryResponse>?,
    @SerializedName("actorsInfo") val actorsInfo: List<StaffResponse>?
)

@Immutable
data class StaffResponse(
    @SerializedName("nb") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("staff_img") val staffImg: String?
)

@Immutable
data class VideoCategoryResponse(
    @SerializedName("en_title") val enTitle: String?,
    @SerializedName("ar_title") val arTitle: String?
)

data class VideoGroupResponse(
    @SerializedName("groups") val groups: List<VideoGroup>?
)

@Immutable
data class VideoGroup(
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: List<VideoResponse>?
)
