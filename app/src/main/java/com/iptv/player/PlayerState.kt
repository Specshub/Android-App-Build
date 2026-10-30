package com.iptv.player
import androidx.media3.common.Player

fun posKey(a: PlayerActivity) = "POS_" + a.streamUrl.hashCode()
fun savePosition(a: PlayerActivity) {
    val p = a.player ?: return; if (p.playbackState == Player.STATE_ENDED) return
    val pos = p.currentPosition; val dur = p.duration
    a.watch?.let { if (dur > 0 && pos > 3000) wsSave(a, it.copy(pos = pos, dur = dur, ts = System.currentTimeMillis())) }
    if (dur > 0 && pos > 10000 && pos < dur - 30000) a.prefs.edit().putLong(posKey(a), pos).apply() else a.prefs.edit().remove(posKey(a)).apply()
}
fun releasePlayer(a: PlayerActivity) { a.player?.let { it.stop(); it.release() }; a.player = null }
