package com.iptv.player

object Constants {
    const val APP_NAME = "Taj Player"

    const val ACTIVATION_SITE = "https://tajplayer.com"
    // الصفحة التي تظهر للمستخدم في شاشة التفعيل (صفحة الدخول مباشرة، وليست الصفحة الرئيسية)
    const val ACTIVATION_PAGE = "https://tajplayer.com/login.php"
    const val ACTIVATION_API_URL = "https://tajplayer.com/api/check.php"

    const val SUPPORT_WHATSAPP = "https://wa.me/212772863204"

    // فاصل فحص حالة التفعيل (بالمللي ثانية)
    const val POLL_INTERVAL_MS = 10_000L

    const val PRIVACY_URL = "https://tajplayer.com/privacy.php"
}
