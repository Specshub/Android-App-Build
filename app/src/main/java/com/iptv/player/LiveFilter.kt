package com.iptv.player
import android.view.View
import com.iptv.player.data.model.*

fun setStreams(a: LiveActivity, list: List<LiveStream>) {
    a.streams = list; a.b.progress.visibility = View.GONE; filterLive(a, a.b.etSearch.text.toString())
    if (a.current == null && list.isNotEmpty() && a.prefs.getBoolean("AUTO_PLAY", true)) { val last = a.prefs.getInt("LAST_LIVE_ID", -1); playStream(a, list.firstOrNull { it.streamId == last } ?: list.first()) }
}
fun filterLive(a: LiveActivity, q: String) {
    val t = q.trim(); val src = if (t.length >= 2) { if (a.allLive == null) loadAllLive(a); a.allLive ?: a.streams } else a.streams
    a.shown = if (t.isEmpty()) a.streams else src.filter { nm(it.name).contains(t, true) }
    a.chAdapter.submitList(a.shown.map { Row(it.streamId.toString(), nm(it.name), it.streamIcon) }); a.chAdapter.select(a.current?.streamId?.toString())
}
