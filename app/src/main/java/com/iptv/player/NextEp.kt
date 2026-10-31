package com.iptv.player
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import com.iptv.player.data.api.XtreamUrlBuilder
import com.iptv.player.data.model.LoginCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

private val nxHttp = OkHttpClient.Builder().connectTimeout(12, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build()
private fun findNext(a: PlayerActivity, c: LoginCredentials, w: WEntry): WEntry? {
    val u = c.host.trimEnd('/') + "/player_api.php?username=" + URLEncoder.encode(c.username, "UTF-8") + "&password=" + URLEncoder.encode(c.password, "UTF-8") + "&action=get_series_info&series_id=" + w.sid
    val body = nxHttp.newCall(Request.Builder().url(u).header("User-Agent", "Mozilla/5.0").build()).execute().use { it.body?.string() } ?: return null
    val eps = JSONObject(body).optJSONObject("episodes") ?: return null
    val flat = ArrayList<Triple<String, Int, JSONObject>>()
    for (s in eps.keys().asSequence().toList().sortedBy { it.toIntOrNull() ?: 0 }) { val arr = eps.optJSONArray(s) ?: continue; for (i in 0 until arr.length()) flat.add(Triple(s, i, arr.getJSONObject(i))) }
    val idx = flat.indexOfFirst { it.third.optString("id") == w.k.drop(1) }; if (idx < 0 || idx + 1 >= flat.size) return null
    val t = flat[idx + 1]; val e = t.third; val eid = e.optString("id")
    return WEntry("E$eid", e.optString("title"), w.img, XtreamUrlBuilder.episodeStreamUrl(c, eid, e.optString("container_extension").ifEmpty { "mp4" }), 0, 0, 0, w.sid, w.sTitle, w.sCover, a.getString(R.string.ep_label, t.first, t.second + 1))
}
fun prefetchNext(a: PlayerActivity) {
    a.nextEntry = null; if (!a.prefs.getBoolean("AUTO_NEXT", true)) return; val w = a.watch ?: return; if (w.sid <= 0 || !w.k.startsWith("E")) return
    val p = ipPrefs(a); val c = LoginCredentials(p.getString("HOST", null) ?: return, p.getString("USER", null) ?: return, p.getString("PASS", null) ?: return)
    a.lifecycleScope.launch {
        val n = withContext(Dispatchers.IO) { runCatching { findNext(a, c, w) }.getOrNull() }
        if (a.watch?.k == w.k) a.nextEntry = n
    }
}
fun playNextEpisode(a: PlayerActivity): Boolean {
    if (!a.prefs.getBoolean("AUTO_NEXT", true)) return false; val n = a.nextEntry ?: return false; val exo = a.player ?: return false
    a.watch = n; a.streamUrl = n.url; a.streamTitle = n.sTitle + " - " + n.label; a.binding.tvTitle.text = a.streamTitle
    exo.setMediaItem(MediaItem.fromUri(n.url)); exo.prepare(); exo.playWhenReady = true
    wsSave(a, n.copy(ts = System.currentTimeMillis()))
    Toast.makeText(a, a.getString(R.string.cont_series, n.label), Toast.LENGTH_SHORT).show()
    prefetchNext(a); return true
}
