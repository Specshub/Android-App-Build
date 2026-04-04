package com.iptv.player.data.model

import com.google.gson.annotations.SerializedName

data class VodStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("stream_id") val streamId: String,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("rating") val rating: String?
)
