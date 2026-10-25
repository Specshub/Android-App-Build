package com.iptv.player
import android.view.View
import com.iptv.player.data.model.*

fun setStreams(a: LiveActivity, list: List<LiveStream>) {
    a.streams = list; a.b.progress.visibility = View.GONE; filterLive(a, a.b.etSearch.text.toString())
    if (a.current == null && list.isNotEmpty() && a.prefs.getBoolean("AUTO_PLAY", true)) { val last = a.prefs.getInt("LAST_LIVE_ID", -1); playStream(a, list.firstOrNull { it.streamId == last } ?: list.first()) }
}
fun filterLive(a: LiveActivity, q: String) {
    a.shown = if (q.isBlank()) a.streams else a.streams.filter { nm(it.name).contains(q, true) }
    a.chAdapter.submitList(a.shown.map { Row(it.streamId.toString(), nm(it.name), it.streamIcon) }); a.chAdapter.select(a.current?.streamId?.toString())
}
