package com.iptv.player
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.iptv.player.data.api.XtreamUrlBuilder
import com.iptv.player.data.model.*

fun observeSeries(a: SeriesActivity) {
    a.vm.seriesInfo.observe(a) { r ->
        if (r is Resource.Success) {
            a.b.progress.visibility = View.GONE; a.seasons = r.data.episodes.toList().sortedBy { it.first.toIntOrNull() ?: 0 }.toMap()
            a.b.tvSeriesPlot.text = nm(r.data.info.plot); a.b.tvSeriesGenre.text = nm(r.data.info.genre)
            a.seasonAdapter.submitList(a.seasons.keys.map { Row(it, "الموسم $it") })
            a.seasons.keys.firstOrNull()?.let { selectSeason(a, it) } ?: run { a.b.tvEmpty.visibility = View.VISIBLE }
        } else if (r is Resource.Error) { a.b.progress.visibility = View.GONE; Toast.makeText(a, r.message, Toast.LENGTH_LONG).show() }
    }
}
fun selectSeason(a: SeriesActivity, key: String) {
    a.seasonAdapter.select(key); a.epAdapter.submitList((a.seasons[key] ?: emptyList()).map { Row(it.id, nm(it.title)) })
}
fun playEpisode(a: SeriesActivity, id: String) {
    val c = a.vm.getCredentials() ?: return; val e = a.seasons.values.flatten().firstOrNull { it.id == id } ?: return
    a.startActivity(Intent(a, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_STREAM_TITLE, a.title + " - " + e.title).putExtra(PlayerActivity.EXTRA_STREAM_URL, XtreamUrlBuilder.episodeStreamUrl(c, e.id, e.containerExtension ?: "mp4")))
}
