package com.iptv.player
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

fun setupBrowse(a: BrowseActivity) {
    a.catAdapter = RowAdapter(false) { r, _ -> selectBrowseCat(a, r.id) }
    a.gridAdapter = PosterAdapter(R.layout.item_grid_poster) { i -> onBrowseItem(a, i) }
    val span = ((a.resources.configuration.screenWidthDp - 250) / 110).coerceIn(2, 8)
    a.b.rvCats.layoutManager = LinearLayoutManager(a); a.b.rvCats.adapter = a.catAdapter; a.b.rvCats.itemAnimator = null
    a.b.rvGrid.layoutManager = GridLayoutManager(a, span); a.b.rvGrid.adapter = a.gridAdapter
    a.b.etSearch.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { filterBrowse(a, s?.toString() ?: "") }
        override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, n: Int) {}
        override fun onTextChanged(s: CharSequence?, st: Int, bf: Int, c: Int) {}
    })
}
fun loadCategories(a: BrowseActivity) { a.b.progress.visibility = View.VISIBLE; if (a.kind == "series") a.vm.loadSeriesCategories() else a.vm.loadVodCategories() }
