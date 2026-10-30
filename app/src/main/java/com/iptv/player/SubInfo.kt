package com.iptv.player
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.iptv.player.data.model.LoginCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val http = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build()

fun subLabel(c: Context): String {
    val t = ipPrefs(c).getString("SUBTXT", null)?.takeIf { it.isNotEmpty() } ?: return ""
    return if (t == "∞") c.getString(R.string.sub_unlimited) else c.getString(R.string.exp_ends, "⁦$t⁩")
}
fun AppCompatActivity.refreshSub(c: LoginCredentials, onDone: () -> Unit) {
    val p = ipPrefs(this); val k = "${c.host}|${c.username}"
    if (p.getString("SUBKEY", null) != k) p.edit().remove("SUBTXT").putString("SUBKEY", k).apply()
    lifecycleScope.launch {
        val t = withContext(Dispatchers.IO) {
            runCatching {
                val u = c.host.trimEnd('/') + "/player_api.php?username=" + java.net.URLEncoder.encode(c.username, "UTF-8") + "&password=" + java.net.URLEncoder.encode(c.password, "UTF-8")
                http.newCall(Request.Builder().url(u).header("User-Agent", "Mozilla/5.0").build()).execute().use { r ->
                    val e = JSONObject(r.body?.string() ?: "{}").getJSONObject("user_info").optString("exp_date", ""); val ts = e.toLongOrNull()
                    if (ts == null || ts <= 0L) "∞" else SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(ts * 1000))
                }
            }.getOrNull()
        }
        if (t != null && p.getString("SUBKEY", null) == k) p.edit().putString("SUBTXT", t).apply()
        onDone()
    }
}
