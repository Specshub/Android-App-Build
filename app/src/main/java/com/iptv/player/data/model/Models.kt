package com.iptv.player.data.model // ✅ الحزمة الموحدة الجديدة

import com.google.gson.annotations.SerializedName

// ─── 1. بيانات الدخول (Authentication) ───────────────────────────────────────────

data class LoginCredentials(
    val host: String,     // ✅ تم التصحيح ليكون "host" ليطابق طلبات الـ API والـ Builder
    val username: String,
    val password: String
)

data class UserInfo(
    @SerializedName("username") val username: String = "",
    @SerializedName("password") val password: String = "",
    @SerializedName("auth") val auth: Int = 0,
    @SerializedName("status") val status: String = "",
    @SerializedName("exp_date") val expDate: String? = null,
    @SerializedName("is_trial") val isTrial: String = "0",
    @SerializedName("active_cons") val activeCons: String = "0",
    @SerializedName("max_connections") val maxConnections: String = "1"
)

data class ServerInfo(
    @SerializedName("url") val url: String = "",
    @SerializedName("port") val port: String = "",
    @SerializedName("server_protocol") val serverProtocol: String = "http",
    @SerializedName("timezone") val timezone: String = ""
)

data class AuthResponse(
    @SerializedName("user_info") val userInfo: UserInfo,
    @SerializedName("server_info") val serverInfo: ServerInfo
)

// ─── 2. القنوات الحية (Live TV) ──────────────────────────────────────────────────

data class LiveCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String
)

data class LiveStream(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("stream_icon") val streamIcon: String? = null,
    @SerializedName("category_id") val categoryId: String = ""
)

// ─── 3. الأفلام (VOD) ────────────────────────────────────────────────────────────

data class VodCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String
)

data class VodStream(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("stream_icon") val streamIcon: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("category_id") val categoryId: String = ""
)

// ─── 4. المسلسلات (Series) ───────────────────────────────────────────────────────

data class SeriesCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String
)

data class Series(
    @SerializedName("num") val num: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("series_id") val seriesId: Int = 0,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("plot") val plot: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("category_id") val categoryId: String = ""
)

data class SeriesInfo(
    @SerializedName("info") val info: SeriesDetail,
    @SerializedName("episodes") val episodes: Map<String, List<Episode>>
)

data class SeriesDetail(
    @SerializedName("name") val name: String = "",
    @SerializedName("plot") val plot: String? = null,
    @SerializedName("genre") val genre: String? = null
)

data class Episode(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("season") val season: Int = 1
)

// ─── 5. نظام إدارة الحالات (UI Wrapper) ──────────────────────────────────────────
// ✅ وجود هذا الكلاس هنا يحل مشكلة Unresolved reference: Loading/Success في كل المشروع

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String, val data: T? = null) : Resource<T>()
    class Loading<T> : Resource<T>()
}

enum class ContentType { LIVE, MOVIE, SERIES }

data class PlaybackInfo(
    val title: String,
    val streamUrl: String,
    val streamType: ContentType,
    val thumbnailUrl: String? = null
)
