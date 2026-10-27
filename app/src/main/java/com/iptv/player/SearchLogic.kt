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
    if (q.length < 2) { a.adapter.submitList(emptyList()); a.b.tvEmpty.text = "اكتب حرفين على الأقل"; a.b.tvEmpty.visibility = View.VISIBLE; return }
    val rows = a.live.filter { nm(it.name).contains(q, true) }.take(40).map { Row("L${it.streamId}", "📺 " + nm(it.name), it.streamIcon) } +
        a.vod.filter { nm(it.name).contains(q, true) }.take(40).map { Row("M${it.streamId}", "🎬 " + nm(it.name), it.streamIcon) } +
        a.series.filter { nm(it.name).contains(q, true) }.take(40).map { Row("S${it.seriesId}", "📚 " + nm(it.name), it.cover) }
    a.adapter.submitList(rows); a.b.tvEmpty.text = "لا توجد نتائج"; a.b.tvEmpty.visibility = if (rows.isEmpty() && loaded) View.GONE.let { View.GONE } else View.GONE
}
