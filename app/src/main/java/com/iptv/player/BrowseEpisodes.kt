package com.iptv.player
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.iptv.player.data.api.XtreamUrlBuilder
import com.iptv.player.data.model.*

fun showEpisodes(a: BrowseActivity, info: SeriesInfo) {
    a.pendingSeries = null; val c = a.vm.getCredentials() ?: return
    val eps = mutableListOf<Episode>(); info.episodes.entries.sortedBy { it.key.toIntOrNull() ?: 0 }.forEach { eps.addAll(it.value) }
    if (eps.isEmpty()) { Toast.makeText(a, "لا توجد حلقات متاحة", Toast.LENGTH_SHORT).show(); return }
    val names = eps.mapIndexed { n, e -> "الحلقة ${n + 1}: ${e.title}" }.toTypedArray()
    AlertDialog.Builder(a).setTitle(info.info.name).setItems(names) { _, w ->
        a.startActivity(Intent(a, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_STREAM_TITLE, eps[w].title).putExtra(PlayerActivity.EXTRA_STREAM_URL, XtreamUrlBuilder.episodeStreamUrl(c, eps[w].id)))
    }.setNegativeButton("إلغاء", null).show()
}
