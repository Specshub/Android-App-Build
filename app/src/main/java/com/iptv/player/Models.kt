// com/iptvplayer/app/data/model/Models.kt
package com.iptvplayer.app.data.model

import com.google.gson.annotations.SerializedName

// ─── Authentication ───────────────────────────────────────────────────────────

data class LoginCredentials(
    val host: String,
    val username: String,
    val password: String
)

data class UserInfo(
    @SerializedName("username") val username: String = "",
    @SerializedName("password") val password: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("auth") val auth: Int = 0,
    @SerializedName("status") val status: String = "",
    @SerializedName("exp_date") val expDate: String? = null,
    @SerializedName("is_trial") val isTrial: String = "0",
    @SerializedName("active_cons") val activeCons: String = "0",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("max_connections") val maxConnections: String = "1",
    @SerializedName("allowed_output_formats") val allowedOutputFormats: List<String> = emptyList()
)

data class ServerInfo(
    @SerializedName("url") val url: String = "",
    @SerializedName("port") val port: String = "",
    @SerializedName("https_port") val httpsPort: String = "",
    @SerializedName("server_protocol") val serverProtocol: String = "http",
    @SerializedName("rtmp_port") val rtmpPort: String = "",
    @SerializedName("timezone") val timezone: String = "",
    @SerializedName("timestamp_now") val timestampNow: Long = 0L,
    @SerializedName("time_now") val timeNow: String = ""
)

data class AuthResponse(
    @SerializedName("user_info") val userInfo: UserInfo,
    @SerializedName("server_info") val serverInfo: ServerInfo
)

// ─── Live TV ──────────────────────────────────────────────────────────────────

data class LiveCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int = 0
)

data class LiveStream(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("stream_type") val streamType: String = "live",
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("stream_icon") val streamIcon: String? = null,
    @SerializedName("epg_channel_id") val epgChannelId: String? = null,
    @SerializedName("added") val added: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("custom_sid") val customSid: String? = null,
    @SerializedName("tv_archive") val tvArchive: Int = 0,
    @SerializedName("direct_source") val directSource: String? = null,
    @SerializedName("tv_archive_duration") val tvArchiveDuration: Int = 0
)

// ─── VOD (Movies) ────────────────────────────────────────────────────────────

data class VodCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int = 0
)

data class VodStream(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("stream_type") val streamType: String = "movie",
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("stream_icon") val streamIcon: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("rating_5based") val rating5based: Float = 0f,
    @SerializedName("added") val added: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("container_extension") val containerExtension: String = "mp4",
    @SerializedName("custom_sid") val customSid: String? = null,
    @SerializedName("direct_source") val directSource: String? = null
)

// ─── Series ───────────────────────────────────────────────────────────────────

data class SeriesCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int = 0
)

data class Series(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("series_id") val seriesId: Int = 0,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("plot") val plot: String? = null,
    @SerializedName("cast") val cast: String? = null,
    @SerializedName("director") val director: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("last_modified") val lastModified: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("rating_5based") val rating5based: Float = 0f,
    @SerializedName("backdrop_path") val backdropPath: List<String>? = null,
    @SerializedName("youtube_trailer") val youtubeTrailer: String? = null,
    @SerializedName("episode_run_time") val episodeRunTime: String? = null,
    @SerializedName("category_id") val categoryId: String = ""
)

data class SeriesInfo(
    @SerializedName("info") val info: SeriesDetail,
    @SerializedName("episodes") val episodes: Map<String, List<Episode>>
)

data class SeriesDetail(
    @SerializedName("name") val name: String = "",
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("plot") val plot: String? = null,
    @SerializedName("cast") val cast: String? = null,
    @SerializedName("director") val director: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("rating") val rating: String? = null
)

data class Episode(
    @SerializedName("id") val id: String = "",
    @SerializedName("episode_num") val episodeNum: Int = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("container_extension") val containerExtension: String = "mp4",
    @SerializedName("info") val info: EpisodeInfo? = null,
    @SerializedName("custom_sid") val customSid: String? = null,
    @SerializedName("added") val added: String = "",
    @SerializedName("season") val season: Int = 1,
    @SerializedName("direct_source") val directSource: String? = null
)

data class EpisodeInfo(
    @SerializedName("movie_image") val movieImage: String? = null,
    @SerializedName("plot") val plot: String? = null,
    @SerializedName("releasedate") val releaseDate: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("duration_secs") val durationSecs: Int = 0,
    @SerializedName("duration") val duration: String? = null
)

// ─── Generic UI wrapper ───────────────────────────────────────────────────────

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String, val data: T? = null) : Resource<T>()
    class Loading<T> : Resource<T>()
}

// ─── Content type enum ────────────────────────────────────────────────────────

enum class ContentType { LIVE, MOVIE, SERIES }

// ─── Playback info passed to PlayerActivity ───────────────────────────────────

data class PlaybackInfo(
    val title: String,
    val streamUrl: String,
    val streamType: ContentType,
    val thumbnailUrl: String? = null
)
