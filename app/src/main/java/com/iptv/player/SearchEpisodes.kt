package com.iptv.player
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.iptv.player.data.api.XtreamUrlBuilder
import com.iptv.player.data.model.*

fun showSeriesEpisodes(a: SearchActivity, info: SeriesInfo) {
    val c = a.vm.getCredentials() ?: return; val eps = mutableListOf<Episode>()
    info.episodes.entries.sortedBy { it.key.toIntOrNull() ?: 0 }.forEach { eps.addAll(it.value) }
    if (eps.isEmpty()) { Toast.makeText(a, a.getString(R.string.no_episodes), Toast.LENGTH_SHORT).show(); return }
    AlertDialog.Builder(a).setTitle(info.info.name).setItems(eps.mapIndexed { n, e -> a.getString(R.string.episode_n, n + 1, e.title) }.toTypedArray()) { _, w ->
        a.startActivity(Intent(a, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_STREAM_TITLE, eps[w].title).putExtra(PlayerActivity.EXTRA_STREAM_URL, XtreamUrlBuilder.episodeStreamUrl(c, eps[w].id, eps[w].containerExtension ?: "mp4")))
    }.setNegativeButton(a.getString(R.string.cancel), null).show()
}
