package com.iptv.player
import android.widget.Toast
import androidx.media3.ui.AspectRatioFrameLayout

private fun defMode(a: LiveActivity) = a.prefs.getInt("VIDEO_MODE", if (a.prefs.getBoolean("FILL_SCREEN", true)) 0 else 1)
fun applyVideoMode(a: LiveActivity) {
    val pv = a.b.playerView; val w = pv.width.toFloat(); val h = pv.height.toFloat(); val vr = a.videoRatio
    val smart = if (vr > 0f && w > 0f && h > 0f && maxOf((w / h) / vr, vr / (w / h)) <= 1.15f) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT
    pv.resizeMode = when (defMode(a)) { 1 -> AspectRatioFrameLayout.RESIZE_MODE_FIT; 2 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM; 3 -> AspectRatioFrameLayout.RESIZE_MODE_FILL; else -> smart }
}
fun cycleVideoMode(a: LiveActivity) {
    val n = (defMode(a) + 1) % 4; a.prefs.edit().putInt("VIDEO_MODE", n).apply(); applyVideoMode(a)
    Toast.makeText(a, "حجم الصورة: " + listOf("تلقائي ذكي", "الحجم الأصلي", "تكبير", "تمدد")[n], Toast.LENGTH_SHORT).show()
}
