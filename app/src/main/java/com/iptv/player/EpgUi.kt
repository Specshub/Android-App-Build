package com.iptv.player
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.iptv.player.data.model.LiveStream
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private fun hm(t: Long) = SimpleDateFormat("HH:mm", Locale.US).format(Date(t * 1000))
fun loadEpg(a: LiveActivity, s: LiveStream) {
    val c = a.vm.getCredentials() ?: return; val b = a.b
    b.tvEpgChannel.text = nm(s.name); b.tvEpgNow.text = "جاري تحميل دليل البرامج..."; b.tvEpgNext.text = ""; b.tvEpgTime.text = ""; b.pbEpg.progress = 0
    b.epgBar.visibility = View.VISIBLE; a.handler.removeCallbacks(a.hideEpg); a.handler.postDelayed(a.hideEpg, 7000)
    a.lifecycleScope.launch {
        val l = a.epgCache[s.streamId] ?: fetchEpg(c, s.streamId).also { if (it.isNotEmpty()) a.epgCache[s.streamId] = it }
        if (a.current?.streamId == s.streamId) renderEpg(a, l)
    }
}
fun renderEpg(a: LiveActivity, l: List<EpgItem>) {
    val b = a.b; val now = System.currentTimeMillis() / 1000
    val cur = l.firstOrNull { now >= it.start && now < it.end }
    if (cur == null) { b.tvEpgNow.text = "لا توجد معلومات عن البرنامج"; b.tvEpgNext.text = ""; b.tvEpgTime.text = ""; return }
    b.tvEpgNow.text = cur.title; b.tvEpgTime.text = hm(cur.start) + " - " + hm(cur.end)
    b.pbEpg.progress = ((now - cur.start) * 100 / (cur.end - cur.start).coerceAtLeast(1)).toInt()
    val nx = l.firstOrNull { it.start >= cur.end }; b.tvEpgNext.text = if (nx != null) "التالي  ${hm(nx.start)}  ${nx.title}" else ""
}
