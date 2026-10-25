package com.iptv.player.data.model

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String, val data: T? = null) : Resource<T>()
    class Loading<T> : Resource<T>()
}
enum class ContentType { LIVE, MOVIE, SERIES }
data class PlaybackInfo(val title: String, val streamUrl: String, val streamType: ContentType, val thumbnailUrl: String? = null)
