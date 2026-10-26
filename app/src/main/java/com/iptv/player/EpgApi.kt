package com.iptv.player
import android.util.Base64
import com.iptv.player.data.model.LoginCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class EpgItem(val title: String, val start: Long, val end: Long)
private val epgClient = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS).build()
private fun dec(s: String): String = try { if (s.length % 4 == 0 && s.matches(Regex("[A-Za-z0-9+/=]+"))) String(Base64.decode(s, Base64.DEFAULT), Charsets.UTF_8).ifBlank { s } else s } catch (e: Exception) { s }
suspend fun fetchEpg(c: LoginCredentials, id: Int): List<EpgItem> = withContext(Dispatchers.IO) {
    try {
        val u = "${c.host.trimEnd('/')}/player_api.php?username=${URLEncoder.encode(c.username, "UTF-8")}&password=${URLEncoder.encode(c.password, "UTF-8")}&action=get_short_epg&stream_id=$id&limit=4"
        val body = epgClient.newCall(Request.Builder().url(u).build()).execute().use { it.body?.string() } ?: return@withContext emptyList()
        val arr = JSONObject(body).optJSONArray("epg_listings") ?: return@withContext emptyList()
        (0 until arr.length()).mapNotNull { i -> val o = arr.getJSONObject(i); val e = o.optString("stop_timestamp").toLongOrNull() ?: 0L
            if (e > 0) EpgItem(dec(o.optString("title")), o.optString("start_timestamp").toLongOrNull() ?: 0L, e) else null }
    } catch (e: Exception) { emptyList() }
}
