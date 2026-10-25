package com.iptv.player
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.iptv.player.data.api.XtreamUrlBuilder
import com.iptv.player.data.model.*

fun selectBrowseCat(a: BrowseActivity, id: String) {
    a.catAdapter.select(id); a.b.progress.visibility = View.VISIBLE; a.b.etSearch.setText("")
    if (a.kind == "series") a.vm.loadSeries(id) else a.vm.loadVodStreams(id)
}
fun showItems(a: BrowseActivity, list: List<PosterItem>) { a.items = list; a.b.progress.visibility = View.GONE; filterBrowse(a, a.b.etSearch.text.toString()) }
fun filterBrowse(a: BrowseActivity, q: String) {
    val l = if (q.isBlank()) a.items else a.items.filter { it.title.contains(q, true) }
    a.gridAdapter.submitList(l); a.b.tvEmpty.visibility = if (l.isEmpty()) View.VISIBLE else View.GONE
}
fun browseError(a: BrowseActivity, m: String) { a.b.progress.visibility = View.GONE; Toast.makeText(a, m, Toast.LENGTH_LONG).show() }
fun onBrowseItem(a: BrowseActivity, i: PosterItem) {
    val c = a.vm.getCredentials() ?: return
    if (a.kind == "series") { a.pendingSeries = i.id.toInt(); a.vm.loadSeriesInfo(i.id.toInt()); Toast.makeText(a, "جاري جلب الحلقات...", Toast.LENGTH_SHORT).show() }
    else a.startActivity(Intent(a, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_STREAM_TITLE, i.title).putExtra(PlayerActivity.EXTRA_STREAM_URL, XtreamUrlBuilder.vodStreamUrl(c, i.id.toInt())))
}
