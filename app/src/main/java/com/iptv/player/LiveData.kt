package com.iptv.player
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.iptv.player.data.model.*

fun setupLive(a: LiveActivity) {
    a.catAdapter = if (a.portrait) RowAdapter(false, R.layout.item_chip) { r, _ -> selectLiveCat(a, r.id) } else RowAdapter(true) { r, _ -> selectLiveCat(a, r.id) }
    a.chAdapter = RowAdapter(true) { r, _ -> a.shown.firstOrNull { it.streamId.toString() == r.id }?.let { playStream(a, it); hideOverlay(a) } }
    a.chAdapter.onLong = { r -> toggleFav(a, r) }
    a.b.rvCats.layoutManager = LinearLayoutManager(a, if (a.portrait) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL, false); a.b.rvCats.adapter = a.catAdapter; a.b.rvCats.itemAnimator = null
    a.b.rvChannels.layoutManager = LinearLayoutManager(a); a.b.rvChannels.adapter = a.chAdapter; a.b.rvChannels.itemAnimator = null
    a.b.etSearch.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { filterLive(a, s?.toString() ?: "") }
        override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, n: Int) {}
        override fun onTextChanged(s: CharSequence?, st: Int, bf: Int, c: Int) {}
    })
}
fun selectLiveCat(a: LiveActivity, id: String) {
    a.catAdapter.select(id); a.prefs.edit().putString("LAST_LIVE_CAT", id).apply(); a.b.etSearch.setText("")
    if (id == "fav") setStreams(a, FavoritesManager.getFavorites(a)) else { a.b.progress.visibility = View.VISIBLE; a.vm.loadLiveStreams(id) }
}
