package com.iptv.player
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.iptv.player.data.api.ActivationResult
import com.iptv.player.data.api.Pl
import com.iptv.player.data.api.plsToJson

fun listsOf(r: ActivationResult): List<Pl> = r.lists.ifEmpty { if (r.host != null && r.username != null && r.password != null) listOf(Pl("", r.host, r.username, r.password)) else emptyList() }
fun saveSession(c: Context, r: ActivationResult) {
    val p = c.getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE); val ls = listsOf(r)
    val s = ls.firstOrNull { it.key() == p.getString("SELK", null) } ?: ls.firstOrNull()
    p.edit().putString("HOST", s?.host).putString("USER", s?.username).putString("PASS", s?.password).putString("SELK", s?.key()).putString("PLS", plsToJson(ls))
        .putString("EXPIRES", r.expires).putString("STATE", r.state).putBoolean("SRV", r.status.isNotEmpty()).putLong("LAST_OK", System.currentTimeMillis()).apply()
}
fun saveAndOpenHome(a: Activity, r: ActivationResult) {
    saveSession(a, r); val p = a.getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
    a.startActivity(Intent(a, HomeActivity::class.java).putExtra(EXTRA_HOST, p.getString("HOST", null)).putExtra(EXTRA_USERNAME, p.getString("USER", null)).putExtra(EXTRA_PASSWORD, p.getString("PASS", null)).putExtra("EXTRA_EXPIRES", r.expires))
    a.finish()
}
