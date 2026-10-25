package com.iptv.player

object Constants {
    const val APP_NAME = "Aurum Player"

    // ⚠️ قيم مؤقتة: بدّلها بموقعك الحقيقي لاحقًا
    const val ACTIVATION_SITE = "https://aurum-player.example.com"
    const val ACTIVATION_API_URL = "https://aurum-player.example.com/api/device/check"

    const val SUPPORT_PHONE = "+212772863204"
    const val SUPPORT_WHATSAPP = "https://wa.me/212772863204"

    // فاصل فحص حالة التفعيل (بالمللي ثانية)
    const val POLL_INTERVAL_MS = 10_000L
}
