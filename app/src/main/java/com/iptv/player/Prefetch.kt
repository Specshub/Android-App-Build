package com.iptv.player
import android.content.Context
import com.iptv.player.data.api.*
import com.iptv.player.data.model.*
import com.iptv.player.data.repository.XtreamRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentHashMap

object BrowseMem { val cat = ConcurrentHashMap<String, List<PosterItem>>() }
fun toV(l: List<VodStream>) = l.map { PosterItem(it.streamId.toString(), nm(it.name), thumb(it.streamIcon), it.containerExtension) }
fun toS(l: List<Series>) = l.map { PosterItem(it.seriesId.toString(), nm(it.name), thumb(it.cover)) }
object Prefetch {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO); val sem = Semaphore(3)
    private val repo by lazy { XtreamRepository(RetrofitClient.instance.create(XtreamApiService::class.java)) }
    private val jobs = ConcurrentHashMap<String, Pair<Long, Deferred<Any?>>>()
    private fun job(k: String, f: suspend () -> Any?): Deferred<Any?> {
        jobs[k]?.let { if (System.currentTimeMillis() - it.first < 600000) return it.second }
        val d = scope.async(start = CoroutineStart.LAZY) { f().also { if (it == null) jobs.remove(k) } }; jobs[k] = Pair(System.currentTimeMillis(), d); d.start(); return d
    }
    fun <T> save(x: Context, k: String, r: Resource<T>): T? = if (r is Resource.Success) { DiskCache.save(x.applicationContext, k, r.data); r.data } else null
    @Suppress("UNCHECKED_CAST") suspend fun <T> go(k: String, f: suspend () -> T?): T? = job(k, f).await() as T?
    suspend fun vodCats(x: Context, c: LoginCredentials) = go<List<VodCategory>>("vc" + cid(c)) { save(x, "vc_${cid(c)}", repo.getVodCategories(c)) }
    suspend fun seriesCats(x: Context, c: LoginCredentials) = go<List<SeriesCategory>>("sc" + cid(c)) { save(x, "sc_${cid(c)}", repo.getSeriesCategories(c)) }
    suspend fun vodCat(x: Context, c: LoginCredentials, id: String) = go<List<VodStream>>("v" + cid(c) + id) { save(x, "vs_${cid(c)}_$id", repo.getVodStreams(c, id))?.also { BrowseMem.cat["v" + cid(c) + id] = toV(it) } }
    suspend fun seriesCat(x: Context, c: LoginCredentials, id: String) = go<List<Series>>("s" + cid(c) + id) { save(x, "sl_${cid(c)}_$id", repo.getSeries(c, id))?.also { BrowseMem.cat["s" + cid(c) + id] = toS(it) } }
    suspend fun liveCats(x: Context, c: LoginCredentials) = go<List<LiveCategory>>("lc" + cid(c)) { save(x, "lc_${cid(c)}", repo.getLiveCategories(c)) }
    suspend fun liveCat(x: Context, c: LoginCredentials, id: String) = go<List<LiveStream>>("l" + cid(c) + id) { save(x, "ls_${cid(c)}_$id", repo.getLiveStreams(c, id)) }
}
