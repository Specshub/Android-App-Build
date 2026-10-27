package com.iptv.player
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.iptv.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun loadCategoryFast(a: BrowseActivity, id: String) {
    val c = a.vm.getCredentials() ?: return; val s = a.kind == "series"; val key = (if (s) "s" else "v") + cid(c) + id; val dk = (if (s) "sl_" else "vs_") + cid(c) + "_" + id
    fun show(l: List<PosterItem>) { if (a.curCat == id) renderItems(a, l) }
    a.lifecycleScope.launch {
        var shown = BrowseMem.cat[key]
        if (shown == null) shown = withContext(Dispatchers.Default) { if (s) DiskCache.load<List<Series>>(a, dk, DiskCache.typeOf<List<Series>>())?.let { toS(it) } else DiskCache.load<List<VodStream>>(a, dk, DiskCache.typeOf<List<VodStream>>())?.let { toV(it) } }
        if (shown != null) { BrowseMem.cat[key] = shown; show(shown) } else if (a.curCat == id) { a.items = emptyList(); a.gridAdapter.submitList(emptyList()); a.b.tvEmpty.visibility = View.GONE; a.b.progress.visibility = View.VISIBLE }
        val ok = if (s) Prefetch.seriesCat(a, c, id) != null else Prefetch.vodCat(a, c, id) != null
        val fresh = BrowseMem.cat[key]
        if (fresh != null && fresh != shown) show(fresh) else if (!ok && shown == null && a.curCat == id) browseError(a, "تعذر تحميل القائمة")
        if (a.curCat == id) a.b.progress.visibility = View.GONE
    }
}
