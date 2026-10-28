package com.iptv.player
import android.content.Context
import com.bumptech.glide.Glide
import com.iptv.player.data.model.LoginCredentials
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withPermit
import kotlin.math.abs

object BrowseWarm { var job: Job? = null }
private fun preloadImages(x: Context, key: String) { BrowseMem.cat[key]?.take(18)?.forEach { p -> if (!p.imageUrl.isNullOrBlank()) Glide.with(x).downloadOnly().load(p.imageUrl).submit() } }
private suspend fun warmOne(x: Context, c: LoginCredentials, s: Boolean, id: String) {
    val key = (if (s) "s" else "v") + cid(c) + id; if (BrowseMem.cat[key] != null) return
    if (s) Prefetch.seriesCat(x, c, id) else Prefetch.vodCat(x, c, id); preloadImages(x, key)
}
fun warmAround(a: BrowseActivity, id: String) {
    val c = a.vm.getCredentials() ?: return; val s = a.kind == "series"; val x = a.applicationContext
    val ids = a.catAdapter.currentList.map { it.id }; val at = ids.indexOf(id); if (at < 0) return
    val near = ids.withIndex().filter { it.index - at in -2..6 && it.index != at }.sortedBy { abs(it.index - at) * 2 + (if (it.index < at) 1 else 0) }.map { it.value }
    BrowseWarm.job?.cancel()
    BrowseWarm.job = Prefetch.scope.launch { near.forEach { k -> launch { Prefetch.sem.withPermit { warmOne(x, c, s, k) } } } }
}
