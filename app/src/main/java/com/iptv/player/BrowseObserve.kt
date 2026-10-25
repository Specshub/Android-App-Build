package com.iptv.player
import android.view.View
import com.iptv.player.data.model.*

fun observeBrowse(a: BrowseActivity) {
    val kids = listOf("kids", "اطفال", "أطفال", "cartoon", "كرتون", "انمي", "anime")
    fun setCats(rows: List<Row>) {
        val r = if (a.kind == "kids") rows.filter { x -> kids.any { x.title.contains(it, true) } }.ifEmpty { rows } else rows
        a.catAdapter.submitList(r); val f = r.firstOrNull()
        if (f != null) selectBrowseCat(a, f.id) else a.b.progress.visibility = View.GONE
    }
    a.vm.vodCategories.observe(a) { r -> if (r is Resource.Success) setCats(r.data.map { Row(nm(it.categoryId), nm(it.categoryName)) }) else if (r is Resource.Error) browseError(a, r.message) }
    a.vm.seriesCategories.observe(a) { r -> if (r is Resource.Success) setCats(r.data.map { Row(nm(it.categoryId), nm(it.categoryName)) }) else if (r is Resource.Error) browseError(a, r.message) }
    a.vm.vodStreams.observe(a) { r -> if (r is Resource.Success) showItems(a, r.data.map { PosterItem(it.streamId.toString(), nm(it.name), it.streamIcon) }) else if (r is Resource.Error) browseError(a, r.message) }
    a.vm.seriesList.observe(a) { r -> if (r is Resource.Success) showItems(a, r.data.map { PosterItem(it.seriesId.toString(), nm(it.name), it.cover) }) else if (r is Resource.Error) browseError(a, r.message) }
    a.vm.seriesInfo.observe(a) { r -> if (r is Resource.Success && a.pendingSeries != null) showEpisodes(a, r.data) }
}
