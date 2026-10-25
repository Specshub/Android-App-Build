package com.iptv.player
import android.view.KeyEvent
import android.view.View

fun zap(a: LiveActivity, d: Int) {
    val l = a.shown; if (l.isEmpty()) return
    val i = l.indexOfFirst { it.streamId == a.current?.streamId }.coerceAtLeast(0)
    playStream(a, l[(i + d + l.size) % l.size])
}
fun handleLiveKey(a: LiveActivity, k: Int): Boolean {
    if (a.b.overlay.visibility == View.VISIBLE) return false
    when (k) {
        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> showOverlay(a)
        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> zap(a, -1)
        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> zap(a, 1)
        else -> return false
    }
    return true
}
