package com.iptv.player
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun loadRow(a: AppCompatActivity, key: String, ad: PosterAdapter, fetch: suspend () -> List<PosterItem>?) {
    a.lifecycleScope.launch {
        val old = withContext(Dispatchers.IO) { DiskCache.load<List<PosterItem>>(a, key, DiskCache.typeOf<List<PosterItem>>()) }
        if (old != null) ad.submitList(old)
        val n = fetch() ?: return@launch
        if (n != old) { ad.submitList(n); withContext(Dispatchers.IO) { DiskCache.save(a, key, n) } }
    }
}
