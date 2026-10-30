package com.iptv.player
import android.view.View
import android.widget.Toast
import com.iptv.player.data.model.*

fun selectBrowseCat(a: BrowseActivity, id: String) { a.catAdapter.select(id); a.curCat = id; a.b.etSearch.setText(""); loadCategoryFast(a, id); warmAround(a, id) }
fun renderItems(a: BrowseActivity, list: List<PosterItem>) { a.items = list; a.b.progress.visibility = View.GONE; filterBrowse(a, a.b.etSearch.text.toString()) }
fun browseError(a: BrowseActivity, m: String) { a.b.progress.visibility = View.GONE; Toast.makeText(a, m, Toast.LENGTH_LONG).show() }
fun onBrowseItem(a: BrowseActivity, i: PosterItem) {
    val c = a.vm.getCredentials() ?: return
    if (a.kind == "series") openSeries(a, c, i.id.toInt(), i.title, i.imageUrl)
    else openMovie(a, c, i.id.toInt(), i.title, i.imageUrl, i.ext)
}
