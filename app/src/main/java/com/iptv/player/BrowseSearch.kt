package com.iptv.player
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.iptv.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun filterBrowse(a: BrowseActivity, q: String) {
    val t = q.trim(); val src = if (t.length >= 2) { if (a.all == null) loadAllBrowse(a); a.all ?: a.items } else a.items
    val l = if (t.isEmpty()) a.items else src.filter { it.title.contains(t, true) }
    a.gridAdapter.submitList(l); a.b.tvEmpty.visibility = if (l.isEmpty() && !a.loadingAll) View.VISIBLE else View.GONE
}
private fun fromVod(l: List<VodStream>) = l.map { PosterItem(it.streamId.toString(), nm(it.name), it.streamIcon, it.containerExtension) }
private fun fromSeries(l: List<Series>) = l.map { PosterItem(it.seriesId.toString(), nm(it.name), it.cover) }
fun loadAllBrowse(a: BrowseActivity) {
    if (a.loadingAll) return; val c = a.vm.getCredentials() ?: return; a.loadingAll = true; a.b.progress.visibility = View.VISIBLE
    fun done(l: List<PosterItem>) { a.all = l; filterBrowse(a, a.b.etSearch.text.toString()) }
    a.lifecycleScope.launch {
        if (a.kind == "series") {
            val key = "sl_" + cid(c) + "_null"
            withContext(Dispatchers.IO) { DiskCache.load<List<Series>>(a, key, DiskCache.typeOf<List<Series>>()) }?.let { done(fromSeries(it)) }
            val r = a.vm.repository.getSeries(c, null)
            if (r is Resource.Success) { withContext(Dispatchers.IO) { DiskCache.save(a, key, r.data) }; done(fromSeries(r.data)) }
        } else {
            val key = "vs_" + cid(c) + "_null"
            withContext(Dispatchers.IO) { DiskCache.load<List<VodStream>>(a, key, DiskCache.typeOf<List<VodStream>>()) }?.let { done(fromVod(it)) }
            val r = a.vm.repository.getVodStreams(c, null)
            if (r is Resource.Success) { withContext(Dispatchers.IO) { DiskCache.save(a, key, r.data) }; done(fromVod(r.data)) }
        }
        a.loadingAll = false; a.b.progress.visibility = View.GONE
    }
}
