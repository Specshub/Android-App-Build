package com.iptv.player
import android.content.Context
import com.iptv.player.data.model.*
import kotlinx.coroutines.*

private val adult = listOf("xxx", "adult", "+18", "18+", "porn", "sex", "للكبار", "بالغين")
private val kids = listOf("anime", "cartoon", "animation", "kids", "children", "toon", "disney", "pixar", "انمي", "أنمي", "كرتون", "اطفال", "أطفال", "رسوم", "انيميشن", "أنيميشن")
private val good = listOf("movie", "film", "vod", "cinema", "series", "show", "drama", "فيلم", "افلام", "أفلام", "مسلسل", "دراما")
fun adultCat(n: String?) = adult.any { nm(n).contains(it, true) }
private fun pick(l: List<Pair<String, String>>): List<String> {
    val ok = l.filter { p -> !adultCat(p.second) && kids.none { p.second.contains(it, true) } }
    return (ok.filter { p -> good.any { p.second.contains(it, true) } } + ok).distinct().take(3).map { it.first }
}
suspend fun latestMovies(x: Context, c: LoginCredentials): List<PosterItem>? {
    val cats = Prefetch.vodCats(x, c) ?: return null
    val l = coroutineScope { pick(cats.map { Pair(nm(it.categoryId), nm(it.categoryName)) }).map { id -> async { Prefetch.vodCat(x, c, id) ?: emptyList() } }.awaitAll().flatten() }
    return toV(l.distinctBy { it.streamId }.sortedByDescending { it.added?.toLongOrNull() ?: it.num.toLong() }.take(30))
}
suspend fun latestSeries(x: Context, c: LoginCredentials): List<PosterItem>? {
    val cats = Prefetch.seriesCats(x, c) ?: return null
    val l = coroutineScope { pick(cats.map { Pair(nm(it.categoryId), nm(it.categoryName)) }).map { id -> async { Prefetch.seriesCat(x, c, id) ?: emptyList() } }.awaitAll().flatten() }
    return toS(l.distinctBy { it.seriesId }.sortedByDescending { it.lastModified?.toLongOrNull() ?: it.num.toLong() }.take(30))
}
