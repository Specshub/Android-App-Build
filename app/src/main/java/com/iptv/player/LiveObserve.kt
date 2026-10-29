package com.iptv.player
import android.view.View
import android.widget.Toast
import com.iptv.player.data.model.*

fun observeLive(a: LiveActivity) {
    a.vm.liveCategories.observe(a) { r ->
        if (r is Resource.Success && !a.catsLoaded) {
            a.catsLoaded = true
            val rows = listOf(Row("fav", a.getString(R.string.cat_fav))) + r.data.map { Row(nm(it.categoryId), nm(it.categoryName)) }
            a.catAdapter.submitList(rows)
            val saved = a.prefs.getString("LAST_LIVE_CAT", null)
            selectLiveCat(a, rows.firstOrNull { it.id == saved }?.id ?: rows.getOrNull(1)?.id ?: "fav")
        } else if (r is Resource.Error) liveError(a, r.message)
    }
    a.vm.liveStreams.observe(a) { r -> if (r is Resource.Success) setStreams(a, r.data) else if (r is Resource.Error) liveError(a, r.message) }
}
fun liveError(a: LiveActivity, m: String) { a.b.progress.visibility = View.GONE; Toast.makeText(a, m, Toast.LENGTH_LONG).show() }
fun toggleFav(a: LiveActivity, r: Row): Boolean {
    val s = a.streams.firstOrNull { it.streamId.toString() == r.id } ?: return false
    val fav = FavoritesManager.getFavorites(a).any { it.streamId == s.streamId }
    if (fav) FavoritesManager.removeFavorite(a, s) else FavoritesManager.addFavorite(a, s)
    Toast.makeText(a, a.getString(if (fav) R.string.fav_removed else R.string.fav_added), Toast.LENGTH_SHORT).show(); return true
}
