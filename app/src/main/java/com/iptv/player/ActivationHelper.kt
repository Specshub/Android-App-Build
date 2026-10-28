package com.iptv.player
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.iptv.player.data.api.ActivationResult

fun saveSession(c: Context, r: ActivationResult) {
    c.getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE).edit().putString("HOST", r.host).putString("USER", r.username).putString("PASS", r.password).putString("EXPIRES", r.expires)
        .putString("STATE", r.state).putBoolean("SRV", r.status.isNotEmpty()).putLong("LAST_OK", System.currentTimeMillis()).apply()
}
fun saveAndOpenHome(a: Activity, r: ActivationResult) {
    saveSession(a, r)
    a.startActivity(Intent(a, HomeActivity::class.java).putExtra(EXTRA_HOST, r.host).putExtra(EXTRA_USERNAME, r.username).putExtra(EXTRA_PASSWORD, r.password).putExtra("EXTRA_EXPIRES", r.expires))
    a.finish()
}
