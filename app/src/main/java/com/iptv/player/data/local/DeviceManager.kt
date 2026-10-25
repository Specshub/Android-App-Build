package com.iptv.player.data.local

import android.content.Context
import java.util.UUID

/**
 * يولّد معرّف الجهاز (بصيغة MAC) ومفتاحًا من 6 أرقام ويحفظهما محليًا.
 * ملاحظة: عنوان MAC الحقيقي غير متاح منذ Android 10، لذلك نولّد معرّفًا خاصًا بالتطبيق.
 */
object DeviceManager {
    private const val PREFS = "device_prefs"
    private const val KEY_MAC = "device_mac"
    private const val KEY_PIN = "device_key"

    fun getMac(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_MAC, null)?.let { return it }

        val bytes = UUID.randomUUID().toString().replace("-", "").take(12)
        val mac = bytes.chunked(2).joinToString(":").uppercase()
        prefs.edit().putString(KEY_MAC, mac).apply()
        return mac
    }

    fun getKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_PIN, null)?.let { return it }

        val pin = (100000..999999).random().toString()
        prefs.edit().putString(KEY_PIN, pin).apply()
        return pin
    }
}
