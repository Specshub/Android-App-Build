package com.iptv.player
import android.view.View
import android.widget.Toast
import androidx.media3.common.*
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.iptv.player.data.model.LiveStream

fun initPlayer(a: LiveActivity) {
    val ds = DefaultHttpDataSource.Factory().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36").setAllowCrossProtocolRedirects(true).setConnectTimeoutMs(15000).setReadTimeoutMs(15000)
    val lc = DefaultLoadControl.Builder().setBufferDurationsMs(15000, 50000, 1000, 2500).build()
    val rf = DefaultRenderersFactory(a).setEnableDecoderFallback(true)
    val exo = ExoPlayer.Builder(a, rf).setLoadControl(lc).setMediaSourceFactory(DefaultMediaSourceFactory(ds)).build()
    exo.addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) { a.b.progress.visibility = if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE; if (state == Player.STATE_READY) a.retries = 0 }
        override fun onPlayerError(error: PlaybackException) {
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) { exo.seekToDefaultPosition(); exo.prepare(); exo.playWhenReady = true } else onPlayError(a)
        }
        override fun onVideoSizeChanged(v: VideoSize) { a.videoRatio = if (v.height > 0) v.width * v.pixelWidthHeightRatio / v.height else 0f; applyVideoMode(a) }
    })
    applyVideoMode(a)
    a.player = exo; a.b.playerView.player = exo
}
fun releasePlayer(a: LiveActivity) { a.b.playerView.player = null; a.player?.release(); a.player = null }
fun startUrl(a: LiveActivity, u: String) { a.url = u; a.b.progress.visibility = View.VISIBLE; a.player?.let { it.setMediaItem(MediaItem.fromUri(u)); it.prepare(); it.playWhenReady = true } }
fun playStream(a: LiveActivity, s: LiveStream) {
    a.current = s; a.altTried = false; a.retries = 0; a.prefs.edit().putInt("LAST_LIVE_ID", s.streamId).apply(); a.chAdapter.select(s.streamId.toString()); loadEpg(a, s)
    startUrl(a, a.vm.buildStreamUrl(s.streamId, liveExt(a.vm.getCredentials()?.host ?: "")))
}
fun onPlayError(a: LiveActivity) {
    val s = a.current ?: return; val c = a.vm.getCredentials() ?: return
    if (!a.altTried) { a.altTried = true; startUrl(a, "${c.host.trimEnd('/')}/${c.username}/${c.password}/${s.streamId}.${liveExt(c.host)}"); return }
    if (a.retries < 3) {
        a.retries++
        // alternate between the main format and the other one (ts <-> m3u8) on retries 1 and 3; never for the built-in demo server
        val main = liveExt(c.host); val other = if (main == "ts") "m3u8" else "ts"; val ext = if (a.retries % 2 == 1 && !c.host.startsWith(DEMO_HOST)) other else main
        a.handler.postDelayed({ if (a.current?.streamId == s.streamId) startUrl(a, a.vm.buildStreamUrl(s.streamId, ext)) }, 2000L * a.retries); return
    }
    a.b.progress.visibility = View.GONE; Toast.makeText(a, a.getString(R.string.play_channel_fail), Toast.LENGTH_SHORT).show()
}
