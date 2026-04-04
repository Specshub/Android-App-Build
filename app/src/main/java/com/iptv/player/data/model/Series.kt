package com.iptv.player.data.model

import com.google.gson.annotations.SerializedName

data class Series(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("series_id") val seriesId: String,
    @SerializedName("cover") val cover: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("genre") val genre: String?
)
