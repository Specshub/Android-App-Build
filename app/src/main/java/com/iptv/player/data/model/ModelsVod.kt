package com.iptv.player.data.model
import com.google.gson.annotations.SerializedName

data class VodCategory(@SerializedName("category_id") val categoryId: String, @SerializedName("category_name") val categoryName: String)
data class VodStream(
    @SerializedName("num") val num: Int = 0, @SerializedName("name") val name: String = "",
    @SerializedName("stream_id") val streamId: Int = 0, @SerializedName("stream_icon") val streamIcon: String? = null,
    @SerializedName("rating") val rating: String? = null, @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("container_extension") val containerExtension: String? = null, @SerializedName("added") val added: String? = null
)
