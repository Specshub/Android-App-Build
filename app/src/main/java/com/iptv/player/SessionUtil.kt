package com.iptv.player
import android.app.Activity
import android.content.Context
import android.content.Intent

const val OFFLINE_LIMIT = 7 * 86_400_000L
fun ipPrefs(c: Context) = c.getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)

fun expiryLabel(c: Context, srv: Boolean, state: String?, e: String?): String { val exp = e?.let { "⁦$it⁩" }; return when {
    !srv -> if (e.isNullOrEmpty()) "" else c.getString(R.string.exp_until, exp)
    state == "trial" -> c.getString(R.string.exp_trial, exp)
    e.isNullOrEmpty() -> c.getString(R.string.exp_lifetime)
    else -> c.getString(R.string.exp_ends, exp)
} }
fun savedLabel(c: Context): String { val p = ipPrefs(c); return expiryLabel(c, p.getBoolean("SRV", false), p.getString("STATE", null), p.getString("EXPIRES", null)) }
fun endSession(a: Activity) {
    ipPrefs(a).edit().remove("HOST").remove("USER").remove("PASS").remove("EXPIRES").apply()
    a.startActivity(Intent(a, ActivationActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)); a.finish()
}
fun openSavedIfOffline(a: Activity): Boolean {
    val p = ipPrefs(a); if (!p.getBoolean("SRV", false) || p.getString("HOST", null).isNullOrEmpty()) return false
    if (System.currentTimeMillis() - p.getLong("LAST_OK", 0) > OFFLINE_LIMIT) return false
    a.startActivity(Intent(a, HomeActivity::class.java)); a.finish(); return true
}
