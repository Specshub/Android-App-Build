package com.iptv.player.data.model
import com.google.gson.annotations.SerializedName

data class SeriesCategory(@SerializedName("category_id") val categoryId: String, @SerializedName("category_name") val categoryName: String)
data class Series(
    @SerializedName("num") val num: Int = 0, @SerializedName("name") val name: String = "",
    @SerializedName("series_id") val seriesId: Int = 0, @SerializedName("cover") val cover: String? = null,
    @SerializedName("plot") val plot: String? = null, @SerializedName("genre") val genre: String? = null,
    @SerializedName("rating") val rating: String? = null, @SerializedName("category_id") val categoryId: String = "", @SerializedName("last_modified") val lastModified: String? = null
)
data class SeriesInfo(@SerializedName("info") val info: SeriesDetail, @SerializedName("episodes") val episodes: Map<String, List<Episode>>)
data class SeriesDetail(@SerializedName("name") val name: String = "", @SerializedName("plot") val plot: String? = null, @SerializedName("genre") val genre: String? = null)
data class Episode(
    @SerializedName("id") val id: String = "", @SerializedName("title") val title: String = "", @SerializedName("season") val season: Int = 1,
    @SerializedName("container_extension") val containerExtension: String? = null
)
