package com.iptv.player.data.local
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest

object DeviceManager {
    @SuppressLint("HardwareIds")
    private fun androidId(c: Context): String = Settings.Secure.getString(c.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    private fun hash(s: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
    fun getMac(c: Context): String {
        val h = hash("aurum-mac:" + androidId(c)); h[0] = ((h[0].toInt() and 0xFE) or 0x02).toByte()
        return h.take(6).joinToString(":") { "%02X".format(it) }
    }
    fun getKey(c: Context): String {
        val h = hash("aurum-key:" + getMac(c)); val n = ((h[0].toLong() and 0xFF) shl 24) or ((h[1].toLong() and 0xFF) shl 16) or ((h[2].toLong() and 0xFF) shl 8) or (h[3].toLong() and 0xFF)
        return (n % 900000 + 100000).toString()
    }
    fun info(c: Context): Map<String, String> = mapOf("model" to Build.MODEL, "brand" to Build.MANUFACTURER, "android" to Build.VERSION.RELEASE, "sdk" to Build.VERSION.SDK_INT.toString(), "device_id" to androidId(c))
}
