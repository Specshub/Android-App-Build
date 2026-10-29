package com.iptv.player
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.*

private fun prefs(a: Context) = a.getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
fun openUrl(a: Context, url: String) { runCatching { a.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }
fun applySavedSettings(a: AppCompatActivity) {
    val p = prefs(a)
    AppCompatDelegate.setDefaultNightMode(if (p.getBoolean("IS_DARK_MODE", true)) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
}
fun performLogout(a: AppCompatActivity) {
    prefs(a).edit().remove("HOST").remove("USER").remove("PASS").remove("EXPIRES").apply()
    a.startActivity(Intent(a, ActivationActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)); a.finish()
}
fun toggleTheme(a: AppCompatActivity) {
    val dark = prefs(a).getBoolean("IS_DARK_MODE", true)
    prefs(a).edit().putBoolean("IS_DARK_MODE", !dark).apply()
    AppCompatDelegate.setDefaultNightMode(if (!dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    a.finish(); a.startActivity(a.intent)
}
