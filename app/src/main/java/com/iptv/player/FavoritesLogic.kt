package com.iptv.player
import android.content.Intent
import android.view.View
import android.widget.Toast

private fun histTitle(e: WEntry): String { val t = if (e.sTitle.isNotEmpty()) e.sTitle + " - " + e.label else e.title; return if (e.dur > 0 && !e.fin()) t + "  •  " + (e.pos * 100 / e.dur) + "%" else t }
fun refreshFavs(a: FavoritesActivity) {
    val rows = if (a.history) wsAll(a).map { Row(it.k, histTitle(it), it.img ?: it.sCover) }
    else FavoritesManager.getFavorites(a).map { Row("L${it.streamId}", nm(it.name), it.streamIcon) } + FavoritesManager.getVodFavorites(a).map { Row("M${it.streamId}", nm(it.name), it.streamIcon) } + FavoritesManager.getSeriesFavorites(a).map { Row("S${it.seriesId}", nm(it.name), it.cover) }
    a.adapter.submitList(rows); a.b.tvEmpty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
}
fun playFav(a: FavoritesActivity, id: String) {
    val c = a.creds
    if (a.history) { val e = wsAll(a).firstOrNull { it.k == id } ?: return; a.startActivity(watchIntent(a, e, if (e.fin()) 0L else e.pos)); return }
    val n = id.drop(1).toIntOrNull() ?: return
    when (id[0]) {
        'L' -> { val s = FavoritesManager.getFavorites(a).firstOrNull { it.streamId == n } ?: return
            a.startActivity(Intent(a, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_STREAM_TITLE, nm(s.name)).putExtra(PlayerActivity.EXTRA_STREAM_URL, "${c.host.trimEnd('/')}/live/${c.username}/${c.password}/${s.streamId}.ts")) }
        'M' -> { val v = FavoritesManager.getVodFavorites(a).firstOrNull { it.streamId == n } ?: return; openMovie(a, c, n, nm(v.name), v.streamIcon, v.containerExtension) }
        else -> { val s = FavoritesManager.getSeriesFavorites(a).firstOrNull { it.seriesId == n } ?: return; openSeries(a, c, n, nm(s.name), s.cover) }
    }
}
fun removeFav(a: FavoritesActivity, id: String): Boolean {
    val n = id.drop(1).toIntOrNull()
    if (a.history) wsRemove(a, id)
    else when (id[0]) {
        'L' -> FavoritesManager.getFavorites(a).firstOrNull { it.streamId == n }?.let { FavoritesManager.removeFavorite(a, it) }
        'M' -> FavoritesManager.getVodFavorites(a).firstOrNull { it.streamId == n }?.let { FavoritesManager.removeVodFavorite(a, it) }
        else -> FavoritesManager.getSeriesFavorites(a).firstOrNull { it.seriesId == n }?.let { FavoritesManager.removeSeriesFavorite(a, it) }
    }
    Toast.makeText(a, a.getString(R.string.fav_removed), Toast.LENGTH_SHORT).show(); refreshFavs(a); return true
}
