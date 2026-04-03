// com/iptvplayer/app/ui/PlayerActivity.kt
package com.iptvplayer.app.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import com.iptvplayer.app.data.local.PrefsManager
import com.iptvplayer.app.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STREAM_TITLE   = "extra_stream_title"
        const val EXTRA_STREAM_URL     = "extra_stream_url"
        const val EXTRA_CONTENT_TYPE   = "extra_content_type"
        const val EXTRA_THUMBNAIL      = "extra_thumbnail"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private lateinit var prefsManager: PrefsManager

    private var streamUrl: String = ""
    private var streamTitle: String = ""

    private val aspectRatios = listOf(
        PrefsManager.ASPECT_16_9,
        PrefsManager.ASPECT_4_3,
        PrefsManager.ASPECT_STRETCH
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fullscreen flags
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )

        prefsManager = PrefsManager(this)
        streamUrl   = intent.getStringExtra(EXTRA_STREAM_URL) ?: ""
        streamTitle = intent.getStringExtra(EXTRA_STREAM_TITLE) ?: "Now Playing"

        binding.tvTitle.text = streamTitle
        setupBackButton()
        setupAspectRatioToggle()
        initializePlayer()
    }

    private fun initializePlayer() {
        if (streamUrl.isEmpty()) {
            showError("Stream URL is missing.")
            return
        }

        player = ExoPlayer.Builder(this).build().also { exo ->
            binding.playerView.player = exo

            applyAspectRatio(prefsManager.getAspectRatio())

            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("IPTVPlayer/1.0")
                .setAllowCrossProtocolRedirects(true)

            val mediaSource: MediaSource = when {
                streamUrl.contains(".m3u8", ignoreCase = true) ||
                streamUrl.contains("/live/", ignoreCase = true) -> {
                    HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(streamUrl))
                }
                else -> {
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(streamUrl))
                }
            }

            exo.setMediaSource(mediaSource)
            exo.prepare()
            exo.playWhenReady = true

            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    showError(
                        when (error.errorCode) {
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                                "Network error. Check your connection."
                            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                                "Stream unavailable (HTTP error). The channel may be offline."
                            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ->
                                "Unsupported stream format."
                            else -> "Playback error: ${error.localizedMessage}"
                        }
                    )
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> binding.progressBar.visibility = View.VISIBLE
                        Player.STATE_READY     -> binding.progressBar.visibility = View.GONE
                        Player.STATE_ENDED     -> finish()
                        else -> {}
                    }
                }
            })
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupAspectRatioToggle() {
        updateAspectRatioIcon(prefsManager.getAspectRatio())
        binding.btnAspectRatio.setOnClickListener {
            val current = prefsManager.getAspectRatio()
            val nextIndex = (aspectRatios.indexOf(current) + 1) % aspectRatios.size
            val next = aspectRatios[nextIndex]
            prefsManager.saveAspectRatio(next)
            applyAspectRatio(next)
            updateAspectRatioIcon(next)
            Toast.makeText(this, "Aspect: $next", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyAspectRatio(ratio: String) {
        binding.playerView.resizeMode = when (ratio) {
            PrefsManager.ASPECT_16_9  -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            PrefsManager.ASPECT_4_3   -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            PrefsManager.ASPECT_STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    private fun updateAspectRatioIcon(ratio: String) {
        binding.btnAspectRatio.text = when (ratio) {
            PrefsManager.ASPECT_16_9   -> "16:9"
            PrefsManager.ASPECT_4_3    -> "4:3"
            PrefsManager.ASPECT_STRETCH -> "Fill"
            else -> "16:9"
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        binding.btnRetry.visibility = View.VISIBLE

        binding.btnRetry.setOnClickListener {
            binding.tvError.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
            releasePlayer()
            initializePlayer()
        }
    }

    private fun releasePlayer() {
        player?.let {
            it.stop()
            it.release()
        }
        player = null
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
    }
}
