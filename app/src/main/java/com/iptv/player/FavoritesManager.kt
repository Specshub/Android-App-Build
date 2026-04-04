package com.iptv.player

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iptv.player.data.model.LiveStream
import com.iptv.player.data.model.Series
import com.iptv.player.data.model.VodStream

object FavoritesManager {
    private const val PREFS_NAME = "favorites_prefs"
    private const val KEY_LIVE_FAVS = "live_favs"
    private const val KEY_VOD_FAVS = "vod_favs"
    private const val KEY_SERIES_FAVS = "series_favs"

    // ─── البث المباشر (LIVE) ───
    fun getFavorites(context: Context): List<LiveStream> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_LIVE_FAVS, null) ?: return emptyList()
        return Gson().fromJson(json, object : TypeToken<List<LiveStream>>() {}.type)
    }
    fun addFavorite(context: Context, stream: LiveStream): Boolean {
        val favs = getFavorites(context).toMutableList()
        if (favs.any { it.streamId == stream.streamId }) return false
        favs.add(stream)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_LIVE_FAVS, Gson().toJson(favs)).apply()
        return true
    }
    fun removeFavorite(context: Context, stream: LiveStream) {
        val favs = getFavorites(context).toMutableList().apply { removeAll { it.streamId == stream.streamId } }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_LIVE_FAVS, Gson().toJson(favs)).apply()
    }

    // ─── الأفلام (VOD) ───
    fun getVodFavorites(context: Context): List<VodStream> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_VOD_FAVS, null) ?: return emptyList()
        return Gson().fromJson(json, object : TypeToken<List<VodStream>>() {}.type)
    }
    fun addVodFavorite(context: Context, stream: VodStream): Boolean {
        val favs = getVodFavorites(context).toMutableList()
        if (favs.any { it.streamId == stream.streamId }) return false
        favs.add(stream)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_VOD_FAVS, Gson().toJson(favs)).apply()
        return true
    }
    fun removeVodFavorite(context: Context, stream: VodStream) {
        val favs = getVodFavorites(context).toMutableList().apply { removeAll { it.streamId == stream.streamId } }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_VOD_FAVS, Gson().toJson(favs)).apply()
    }

    // ─── المسلسلات (SERIES) ───
    fun getSeriesFavorites(context: Context): List<Series> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_SERIES_FAVS, null) ?: return emptyList()
        return Gson().fromJson(json, object : TypeToken<List<Series>>() {}.type)
    }
    fun addSeriesFavorite(context: Context, series: Series): Boolean {
        val favs = getSeriesFavorites(context).toMutableList()
        if (favs.any { it.seriesId == series.seriesId }) return false
        favs.add(series)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SERIES_FAVS, Gson().toJson(favs)).apply()
        return true
    }
    fun removeSeriesFavorite(context: Context, series: Series) {
        val favs = getSeriesFavorites(context).toMutableList().apply { removeAll { it.seriesId == series.seriesId } }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SERIES_FAVS, Gson().toJson(favs)).apply()
    }
}
