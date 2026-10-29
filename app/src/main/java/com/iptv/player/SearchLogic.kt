package com.iptv.player
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.iptv.player.data.api.XtreamUrlBuilder
import com.iptv.player.data.model.*

fun observeSearch(a: SearchActivity) {
    a.vm.liveStreams.observe(a) { r -> if (r is Resource.Success) { a.live = r.data; runSearch(a) } }
    a.vm.vodStreams.observe(a) { r -> if (r is Resource.Success) { a.vod = r.data; runSearch(a) } }
    a.vm.seriesList.observe(a) { r -> if (r is Resource.Success) { a.series = r.data; runSearch(a) } }
    a.vm.seriesInfo.observe(a) { r -> if (r is Resource.Success && a.pendingSeries) { a.pendingSeries = false; showSeriesEpisodes(a, r.data) } }
}
fun runSearch(a: SearchActivity) {
    val q = a.b.etSearch.text.toString().trim(); val loaded = a.live.isNotEmpty() || a.vod.isNotEmpty() || a.series.isNotEmpty()
    a.b.progress.visibility = if (loaded) View.GONE else View.VISIBLE
    if (q.length < 2) { a.adapter.submitList(emptyList()); a.b.tvEmpty.text = a.getString(R.string.search_min); a.b.tvEmpty.visibility = View.VISIBLE; return }
    val rows = a.live.filter { nm(it.name).contains(q, true) }.take(40).map { Row("L${it.streamId}", nm(it.name), it.streamIcon) } +
        a.vod.filter { nm(it.name).contains(q, true) }.take(40).map { Row("M${it.streamId}", nm(it.name), it.streamIcon) } +
        a.series.filter { nm(it.name).contains(q, true) }.take(40).map { Row("S${it.seriesId}", nm(it.name), it.cover) }
    a.adapter.submitList(rows); a.b.tvEmpty.text = a.getString(R.string.no_results); a.b.tvEmpty.visibility = if (rows.isEmpty() && loaded) View.VISIBLE else View.GONE
}
fun onSearchClick(a: SearchActivity, id: String) {
    val c = a.vm.getCredentials() ?: return; val n = id.drop(1).toInt()
    fun play(t: String, u: String) = a.startActivity(Intent(a, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_STREAM_TITLE, t).putExtra(PlayerActivity.EXTRA_STREAM_URL, u))
    when (id[0]) {
        'L' -> play(nm(a.live.firstOrNull { it.streamId == n }?.name), a.vm.buildStreamUrl(n, "ts"))
        'M' -> { val v = a.vod.firstOrNull { it.streamId == n }; play(nm(v?.name), XtreamUrlBuilder.vodStreamUrl(c, n, v?.containerExtension ?: "mp4")) }
        else -> { val s = a.series.firstOrNull { it.seriesId == n }; openSeries(a, c, n, nm(s?.name), s?.cover) }
    }
}
