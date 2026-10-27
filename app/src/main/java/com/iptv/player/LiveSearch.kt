package com.iptv.player
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.iptv.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun loadAllLive(a: LiveActivity) {
    if (a.loadingAll) return; val c = a.vm.getCredentials() ?: return; a.loadingAll = true; a.b.progress.visibility = View.VISIBLE
    val key = "ls_" + cid(c) + "_null"
    a.lifecycleScope.launch {
        val old: List<LiveStream>? = withContext(Dispatchers.IO) { DiskCache.load<List<LiveStream>>(a, key, DiskCache.typeOf<List<LiveStream>>()) }
        if (old != null) { a.allLive = old; filterLive(a, a.b.etSearch.text.toString()) }
        val r = a.vm.repository.getLiveStreams(c, null)
        if (r is Resource.Success) { a.allLive = r.data; withContext(Dispatchers.IO) { DiskCache.save(a, key, r.data) }; filterLive(a, a.b.etSearch.text.toString()) }
        a.loadingAll = false; a.b.progress.visibility = View.GONE
    }
}
