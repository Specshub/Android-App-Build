package com.iptv.player
import android.view.View
import android.widget.Toast
import com.iptv.player.data.api.XtreamUrlBuilder
import com.iptv.player.data.model.*

fun observeSeries(a: SeriesActivity) {
    a.vm.seriesInfo.observe(a) { r ->
        if (r is Resource.Success) {
            a.b.progress.visibility = View.GONE; a.seasons = r.data.episodes.toList().sortedBy { it.first.toIntOrNull() ?: 0 }.toMap()
            a.b.tvSeriesPlot.text = nm(r.data.info.plot); a.b.tvSeriesGenre.text = nm(r.data.info.genre)
            a.seasonAdapter.submitList(a.seasons.keys.map { Row(it, a.getString(R.string.season_n, it)) })
            findContinue(a); val first = a.seasons.keys.firstOrNull()
            if (first == null) a.b.tvEmpty.visibility = View.VISIBLE else selectSeason(a, a.contSeason ?: first, a.contEp?.id)
        } else if (r is Resource.Error) { a.b.progress.visibility = View.GONE; Toast.makeText(a, r.message, Toast.LENGTH_LONG).show() }
    }
}
fun selectSeason(a: SeriesActivity, key: String, focusId: String? = null) {
    a.seasonAdapter.select(key); val rows = (a.seasons[key] ?: emptyList()).map { Row(it.id, nm(it.title)) }
    a.epAdapter.submitList(rows) { focusId?.let { id -> val i = rows.indexOfFirst { it.id == id }; if (i >= 0) { a.epAdapter.select(id); a.b.rvEpisodes.scrollToPosition(i) } } }
}
private fun findContinue(a: SeriesActivity) {
    val last = wsSeries(a, a.sid) ?: return; val flat = a.seasons.entries.flatMap { en -> en.value.map { en.key to it } }
    val idx = flat.indexOfFirst { it.second.id == last.k.drop(1) }; if (idx < 0) return
    val ti = if (last.fin() && idx + 1 < flat.size) idx + 1 else idx; val t = flat[ti]
    a.contEp = t.second; a.contSeason = t.first; a.contStart = if (ti == idx && !last.fin()) last.pos else 0L
    a.b.btnContinue.text = a.getString(R.string.cont_series, epLabel(a, t.second.id)); a.b.btnContinue.visibility = View.VISIBLE
}
private fun epLabel(a: SeriesActivity, id: String): String { for ((k, l) in a.seasons) { val i = l.indexOfFirst { it.id == id }; if (i >= 0) return a.getString(R.string.ep_label, k, i + 1) }; return "" }
fun setupSeriesButtons(a: SeriesActivity) {
    fun on() = FavoritesManager.getSeriesFavorites(a).any { it.seriesId == a.sid }
    fun upd() { a.b.btnSFav.text = a.getString(if (on()) R.string.fav_in else R.string.fav_add) }
    upd(); a.b.btnSFav.setOnClickListener { val s = Series(name = a.title, seriesId = a.sid, cover = a.cover)
        if (on()) { FavoritesManager.removeSeriesFavorite(a, s); Toast.makeText(a, a.getString(R.string.fav_removed), Toast.LENGTH_SHORT).show() } else { FavoritesManager.addSeriesFavorite(a, s); Toast.makeText(a, a.getString(R.string.fav_added), Toast.LENGTH_SHORT).show() }; upd() }
    a.b.btnContinue.setOnClickListener { a.contEp?.let { playEpisode(a, it.id, a.contStart) } }
}
fun playEpisode(a: SeriesActivity, id: String, start: Long = -1L) {
    val c = a.vm.getCredentials() ?: return; val e = a.seasons.values.flatten().firstOrNull { it.id == id } ?: return
    val h = wsAll(a).firstOrNull { it.k == "E$id" }; val st = if (start >= 0) start else if (h != null && !h.fin()) h.pos else 0L
    val w = WEntry("E$id", e.title, a.cover, XtreamUrlBuilder.episodeStreamUrl(c, e.id, e.containerExtension ?: "mp4"), 0, 0, 0, a.sid, a.title, a.cover, epLabel(a, id))
    a.startActivity(watchIntent(a, w, st))
}
