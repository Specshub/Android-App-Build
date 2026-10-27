package com.iptv.player
import android.view.View
import com.iptv.player.data.model.*

fun observeBrowse(a: BrowseActivity) {
    val kids = listOf("kids", "اطفال", "أطفال", "cartoon", "كرتون", "انمي", "anime")
    fun setCats(rows: List<Row>) {
        val ok = rows.filter { !adultCat(it.title) }; val r = if (a.kind == "kids") ok.filter { x -> kids.any { x.title.contains(it, true) } }.ifEmpty { ok } else ok
        a.catAdapter.submitList(r); val f = r.firstOrNull()
        if (f != null) { if (a.curCat == null) selectBrowseCat(a, f.id) } else a.b.progress.visibility = View.GONE
    }
    a.vm.vodCategories.observe(a) { r -> if (r is Resource.Success) setCats(r.data.map { Row(nm(it.categoryId), nm(it.categoryName)) }) else if (r is Resource.Error) browseError(a, r.message) }
    a.vm.seriesCategories.observe(a) { r -> if (r is Resource.Success) setCats(r.data.map { Row(nm(it.categoryId), nm(it.categoryName)) }) else if (r is Resource.Error) browseError(a, r.message) }
}
