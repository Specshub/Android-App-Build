package com.iptv.player
import android.view.View
import android.widget.Toast
import androidx.media3.common.*
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.iptv.player.data.model.LiveStream

fun initPlayer(a: LiveActivity) {
    val ds = DefaultHttpDataSource.Factory().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36").setAllowCrossProtocolRedirects(true).setConnectTimeoutMs(15000).setReadTimeoutMs(15000)
    val lc = DefaultLoadControl.Builder().setBufferDurationsMs(15000, 50000, 1000, 2500).build()
    val exo = ExoPlayer.Builder(a).setLoadControl(lc).setMediaSourceFactory(DefaultMediaSourceFactory(ds)).build()
    exo.addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) { a.b.progress.visibility = if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE }
        override fun onPlayerError(error: PlaybackException) { onPlayError(a) }
    })
    a.player = exo; a.b.playerView.player = exo
}
fun releasePlayer(a: LiveActivity) { a.b.playerView.player = null; a.player?.release(); a.player = null }
fun startUrl(a: LiveActivity, u: String) { a.url = u; a.b.progress.visibility = View.VISIBLE; a.player?.let { it.setMediaItem(MediaItem.fromUri(u)); it.prepare(); it.playWhenReady = true } }
fun playStream(a: LiveActivity, s: LiveStream) {
    a.current = s; a.altTried = false; a.prefs.edit().putInt("LAST_LIVE_ID", s.streamId).apply(); a.chAdapter.select(s.streamId.toString()); showNow(a, nm(s.name))
    startUrl(a, a.vm.buildStreamUrl(s.streamId, "ts"))
}
fun onPlayError(a: LiveActivity) {
    val s = a.current ?: return; val c = a.vm.getCredentials() ?: return
    if (!a.altTried) { a.altTried = true; startUrl(a, "${c.host.trimEnd('/')}/${c.username}/${c.password}/${s.streamId}.ts"); return }
    a.b.progress.visibility = View.GONE; Toast.makeText(a, "تعذر تشغيل القناة", Toast.LENGTH_SHORT).show()
}
fun showNow(a: LiveActivity, name: String) { a.b.tvNow.text = name; a.b.tvNow.visibility = View.VISIBLE; a.handler.removeCallbacks(a.hideNow); a.handler.postDelayed(a.hideNow, 3000) }
