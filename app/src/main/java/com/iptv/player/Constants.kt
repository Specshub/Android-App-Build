package com.iptv.player

object Constants {
    const val APP_NAME = "Taj Player"

    const val ACTIVATION_SITE = "https://tajplayer.com"
    const val ACTIVATION_API_URL = "https://tajplayer.com/api/device/check"

    const val SUPPORT_PHONE = "+212772863204"
    const val SUPPORT_WHATSAPP = "https://wa.me/212772863204"

    // فاصل فحص حالة التفعيل (بالمللي ثانية)
    const val POLL_INTERVAL_MS = 10_000L

    // ⚠️ اجعلها false قبل رفع التطبيق إلى Google Play
    const val DEMO_MODE = true
}
