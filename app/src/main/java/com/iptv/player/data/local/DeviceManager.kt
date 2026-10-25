package com.iptv.player.data.local
import android.content.Context
import java.util.UUID

object DeviceManager {
    private fun prefs(c: Context) = c.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
    fun getMac(c: Context): String {
        prefs(c).getString("device_mac", null)?.let { return it }
        val mac = UUID.randomUUID().toString().replace("-", "").take(12).chunked(2).joinToString(":").uppercase()
        prefs(c).edit().putString("device_mac", mac).apply()
        return mac
    }
    fun getKey(c: Context): String {
        prefs(c).getString("device_key", null)?.let { return it }
        val pin = (100000..999999).random().toString()
        prefs(c).edit().putString("device_key", pin).apply()
        return pin
    }
}
