package com.iptv.player.data.model
import com.google.gson.annotations.SerializedName

data class LiveCategory(@SerializedName("category_id") val categoryId: String, @SerializedName("category_name") val categoryName: String)
data class LiveStream(
    @SerializedName("num") val num: Int = 0, @SerializedName("name") val name: String = "",
    @SerializedName("stream_id") val streamId: Int = 0, @SerializedName("stream_icon") val streamIcon: String? = null,
    @SerializedName("category_id") val categoryId: String = ""
)
