package com.iptv.player
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.iptv.player.data.api.DeviceApi
import com.iptv.player.data.local.DeviceManager
import kotlinx.coroutines.launch

private const val EVERY = 15 * 60_000L

fun AppCompatActivity.resyncDevice(onLabel: (String) -> Unit) {
    val p = ipPrefs(this); if (!p.getBoolean("SRV", false)) return
    val now = System.currentTimeMillis(); if (now - p.getLong("LAST_TRY", 0) < EVERY) return
    p.edit().putLong("LAST_TRY", now).apply()
    lifecycleScope.launch {
        val r = DeviceApi.check(DeviceManager.getMac(this@resyncDevice), DeviceManager.getKey(this@resyncDevice), DeviceManager.info(this@resyncDevice))
        if (r.error != null) { if (now - p.getLong("LAST_OK", now) > OFFLINE_LIMIT) endSession(this@resyncDevice); return@launch }
        if (!r.active || r.host == null || r.username == null || r.password == null) { endSession(this@resyncDevice); return@launch }
        val same = r.host == p.getString("HOST", null) && r.username == p.getString("USER", null) && r.password == p.getString("PASS", null)
        if (!same) { saveAndOpenHome(this@resyncDevice, r); return@launch }
        saveSession(this@resyncDevice, r); onLabel(savedLabel(this@resyncDevice))
    }
}
