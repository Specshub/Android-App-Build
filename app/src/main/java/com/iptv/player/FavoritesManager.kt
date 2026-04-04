package com.iptv.player

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iptv.player.data.model.LiveStream

// 🌟 هذا الملف هو "الخزنة" التي سنحفظ فيها قنوات الزبون المفضلة
object FavoritesManager {
    private const val PREFS_NAME = "favorites_prefs"
    private const val KEY_LIVE_FAVS = "live_favs"

    // جلب كل القنوات المفضلة
    fun getFavorites(context: Context): List<LiveStream> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_LIVE_FAVS, null) ?: return emptyList()
        val type = object : TypeToken<List<LiveStream>>() {}.type
        return Gson().fromJson(json, type)
    }

    // إضافة قناة للمفضلة (ترجع true إذا تمت الإضافة، و false إذا كانت موجودة مسبقاً)
    fun addFavorite(context: Context, stream: LiveStream): Boolean {
        val favs = getFavorites(context).toMutableList()
        if (favs.any { it.streamId == stream.streamId }) return false 
        
        favs.add(stream)
        saveFavorites(context, favs)
        return true
    }

    // حذف قناة من المفضلة
    fun removeFavorite(context: Context, stream: LiveStream) {
        val favs = getFavorites(context).toMutableList()
        favs.removeAll { it.streamId == stream.streamId }
        saveFavorites(context, favs)
    }

    private fun saveFavorites(context: Context, favs: List<LiveStream>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(favs)
        prefs.edit().putString(KEY_LIVE_FAVS, json).apply()
    }
}
