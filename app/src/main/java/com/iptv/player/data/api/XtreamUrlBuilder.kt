package com.iptv.player.data.api // ✅ الهوية الجديدة الموحدة

import com.iptv.player.data.model.LoginCredentials // ✅ استيراد الموديل من المسار الجديد

object XtreamUrlBuilder {

    private fun base(credentials: LoginCredentials): String {
        val host = credentials.host.trimEnd('/')
        return "$host/player_api.php?username=${credentials.username}&password=${credentials.password}"
    }

    fun authUrl(credentials: LoginCredentials) = base(credentials)

    // Live TV
    fun liveCategories(c: LoginCredentials) = "${base(c)}&action=get_live_categories"
    fun liveStreams(c: LoginCredentials) = "${base(c)}&action=get_live_streams"
    fun liveStreamsByCategory(c: LoginCredentials, catId: String) =
        "${base(c)}&action=get_live_streams&category_id=$catId"

    // VOD
    fun vodCategories(c: LoginCredentials) = "${base(c)}&action=get_vod_categories"
    fun vodStreams(c: LoginCredentials) = "${base(c)}&action=get_vod_streams"
    fun vodStreamsByCategory(c: LoginCredentials, catId: String) =
        "${base(c)}&action=get_vod_streams&category_id=$catId"

    // Series
    fun seriesCategories(c: LoginCredentials) = "${base(c)}&action=get_series_categories"
    fun series(c: LoginCredentials) = "${base(c)}&action=get_series"
    fun seriesByCategory(c: LoginCredentials, catId: String) =
        "${base(c)}&action=get_series&category_id=$catId"
    fun seriesInfo(c: LoginCredentials, seriesId: Int) =
        "${base(c)}&action=get_series_info&series_id=$seriesId"

    // Stream URLs
    fun liveStreamUrl(c: LoginCredentials, streamId: Int, ext: String = "m3u8"): String {
        val host = c.host.trimEnd('/')
        return "$host/${c.username}/${c.password}/$streamId.$ext"
    }

    fun vodStreamUrl(c: LoginCredentials, streamId: Int, ext: String = "mp4"): String {
        val host = c.host.trimEnd('/')
        return "$host/movie/${c.username}/${c.password}/$streamId.$ext"
    }

    fun episodeStreamUrl(c: LoginCredentials, episodeId: String, ext: String = "mp4"): String {
        val host = c.host.trimEnd('/')
        return "$host/series/${c.username}/${c.password}/$episodeId.$ext"
    }
}
