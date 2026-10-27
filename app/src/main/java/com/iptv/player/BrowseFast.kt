package com.iptv.player
import androidx.lifecycle.lifecycleScope
import com.iptv.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object BrowseMem { val m = HashMap<String, Map<String, List<PosterItem>>>() }
private fun gv(l: List<VodStream>) = l.groupBy { nm(it.categoryId) }.mapValues { e -> e.value.map { PosterItem(it.streamId.toString(), nm(it.name), it.streamIcon, it.containerExtension) } }
private fun gs(l: List<Series>) = l.groupBy { nm(it.categoryId) }.mapValues { e -> e.value.map { PosterItem(it.seriesId.toString(), nm(it.name), it.cover) } }
private fun apply(a: BrowseActivity, key: String, m: Map<String, List<PosterItem>>) {
    a.byCat = m; BrowseMem.m[key] = m; a.all = m.values.flatten(); a.curCat?.let { renderItems(a, m[it] ?: emptyList()) }
}
fun startFast(a: BrowseActivity) {
    val c = a.vm.getCredentials() ?: return; val s = a.kind == "series"; val key = a.kind + cid(c); val dk = (if (s) "sl_" else "vs_") + cid(c) + "_null"
    BrowseMem.m[key]?.let { a.byCat = it; a.all = it.values.flatten() }
    a.lifecycleScope.launch {
        if (a.byCat == null) withContext(Dispatchers.Default) {
            if (s) DiskCache.load<List<Series>>(a, dk, DiskCache.typeOf<List<Series>>())?.let { gs(it) } else DiskCache.load<List<VodStream>>(a, dk, DiskCache.typeOf<List<VodStream>>())?.let { gv(it) }
        }?.let { apply(a, key, it) }
        val r = if (s) a.vm.repository.getSeries(c, null) else a.vm.repository.getVodStreams(c, null)
        if (r is Resource.Success) {
            val m = withContext(Dispatchers.Default) { @Suppress("UNCHECKED_CAST") if (s) gs(r.data as List<Series>) else gv(r.data as List<VodStream>) }
            withContext(Dispatchers.IO) { DiskCache.save(a, dk, r.data) }; apply(a, key, m)
        }
    }
}
