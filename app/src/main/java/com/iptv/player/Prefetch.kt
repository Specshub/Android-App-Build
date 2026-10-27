package com.iptv.player
import android.content.Context
import com.iptv.player.data.api.*
import com.iptv.player.data.model.*
import com.iptv.player.data.repository.XtreamRepository
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

object Prefetch {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repo by lazy { XtreamRepository(RetrofitClient.instance.create(XtreamApiService::class.java)) }
    private val jobs = ConcurrentHashMap<String, Pair<Long, Deferred<Any?>>>()
    val liveMem = ConcurrentHashMap<String, List<LiveStream>>()
    private fun job(k: String, f: suspend () -> Any?): Deferred<Any?> {
        jobs[k]?.let { if (System.currentTimeMillis() - it.first < 600000) return it.second }
        val d = scope.async(start = CoroutineStart.LAZY) { f().also { if (it == null) jobs.remove(k) } }; jobs[k] = Pair(System.currentTimeMillis(), d); d.start(); return d
    }
    private fun <T> save(x: Context, k: String, r: Resource<T>): T? = if (r is Resource.Success) { DiskCache.save(x.applicationContext, k, r.data); r.data } else null
    @Suppress("UNCHECKED_CAST")
    suspend fun vod(x: Context, c: LoginCredentials) = job("v" + cid(c)) { save(x, "vs_${cid(c)}_null", repo.getVodStreams(c, null))?.also { val m = gv(it); BrowseMem.m["movies" + cid(c)] = m; BrowseMem.m["kids" + cid(c)] = m } }.await() as List<VodStream>?
    @Suppress("UNCHECKED_CAST")
    suspend fun series(x: Context, c: LoginCredentials) = job("s" + cid(c)) { save(x, "sl_${cid(c)}_null", repo.getSeries(c, null))?.also { BrowseMem.m["series" + cid(c)] = gs(it) } }.await() as List<Series>?
    @Suppress("UNCHECKED_CAST")
    suspend fun live(x: Context, c: LoginCredentials) = job("l" + cid(c)) { save(x, "ls_${cid(c)}_null", repo.getLiveStreams(c, null))?.also { liveMem[cid(c)] = it } }.await() as List<LiveStream>?
    fun warm(x: Context, c: LoginCredentials) {
        scope.launch {
            launch { job("vc" + cid(c)) { save(x, "vc_${cid(c)}", repo.getVodCategories(c)) } }; launch { job("sc" + cid(c)) { save(x, "sc_${cid(c)}", repo.getSeriesCategories(c)) } }
            launch { job("lc" + cid(c)) { save(x, "lc_${cid(c)}", repo.getLiveCategories(c)) } }; launch { live(x, c) }; launch { vod(x, c) }; launch { series(x, c) }
        }
    }
}
