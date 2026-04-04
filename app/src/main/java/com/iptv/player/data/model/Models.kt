package com.iptv.player.data.model // ✅ الهوية الموحدة والمضمونة

import com.google.gson.annotations.SerializedName

// ── 1. بيانات تسجيل الدخول (Authentication) ──────────────────────────────
data class LoginCredentials(
    val username: String,
    val password: String,
    val serverUrl: String
)

data class AuthResponse(
    @SerializedName("user_info") val userInfo: UserInfo?,
    @SerializedName("server_info") val serverInfo: ServerInfo?
)

data class UserInfo(
    @SerializedName("username") val username: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("exp_date") val expDate: String?,
    @SerializedName("is_trial") val isTrial: String?,
    @SerializedName("active_cons") val activeCons: String?,
    @SerializedName("max_connections") val maxConnections: String?
)

data class ServerInfo(
    @SerializedName("url") val url: String?,
    @SerializedName("port") val port: String?,
    @SerializedName("server_protocol") val protocol: String?
)

// ── 2. القنوات الحية (Live TV) ───────────────────────────────────────────
data class LiveCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String
)

data class LiveStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("stream_id") val streamId: String,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("category_id") val categoryId: String?
)

// ── 3. الأفلام (VOD) ────────────────────────────────────────────────────
data class VodCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String
)

data class VodStream(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("stream_id") val streamId: String,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("category_id") val categoryId: String?
)

// ── 4. المسلسلات (Series) ────────────────────────────────────────────────
data class SeriesCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String
)

data class Series(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("series_id") val seriesId: String,
    @SerializedName("cover") val cover: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("category_id") val categoryId: String?
)

data class SeriesInfo(
    @SerializedName("info") val info: SeriesDetails?,
    @SerializedName("episodes") val episodes: Map<String, List<Episode>>?
)

data class SeriesDetails(
    @SerializedName("plot") val plot: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("genre") val genre: String?
)

data class Episode(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("container_extension") val containerExtension: String?
)
