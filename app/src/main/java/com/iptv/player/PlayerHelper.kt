package com.iptv.player
import android.view.View
import androidx.media3.common.*
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

@Suppress("DEPRECATION")
fun hidePlayerBars(a: PlayerActivity) { a.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY }
fun initializePlayer(a: PlayerActivity) {
    if (a.streamUrl.isEmpty()) { showError(a, "الرابط مفقود"); return }
    val ds = DefaultHttpDataSource.Factory().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36").setAllowCrossProtocolRedirects(true).setConnectTimeoutMs(15000).setReadTimeoutMs(20000)
    val lc = DefaultLoadControl.Builder().setBufferDurationsMs(30000, 120000, 1500, 3000).build()
    val exo = ExoPlayer.Builder(a).setLoadControl(lc).setMediaSourceFactory(DefaultMediaSourceFactory(ds)).build()
    a.player = exo; a.binding.playerView.player = exo
    exo.setMediaItem(MediaItem.fromUri(a.streamUrl))
    val pos = a.prefs.getLong(posKey(a), 0L); if (pos > 0) exo.seekTo(pos)
    exo.prepare(); exo.playWhenReady = true
    exo.addListener(object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) { showError(a, "تعذر تشغيل الفيديو (${error.errorCodeName})") }
        override fun onPlaybackStateChanged(s: Int) {
            when (s) { Player.STATE_BUFFERING -> a.binding.progressBar.visibility = View.VISIBLE; Player.STATE_READY -> a.binding.progressBar.visibility = View.GONE
                Player.STATE_ENDED -> { a.prefs.edit().remove(posKey(a)).apply(); a.finish() } else -> {} }
        }
    })
}
