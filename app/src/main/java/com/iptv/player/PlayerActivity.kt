package com.iptv.player // ✅ الهوية الجديدة الموحدة

import android.os.Bundle
import android.view.View
import android.view.WindowManager
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
// ✅ تأكد من تعديل حزمة PrefsManager لتكون هكذا:
// import com.iptv.player.data.local.PrefsManager
import com.iptv.player.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STREAM_TITLE   = "extra_stream_title"
        const val EXTRA_STREAM_URL     = "extra_stream_url"
        const val EXTRA_CONTENT_TYPE   = "extra_content_type"
        const val EXTRA_THUMBNAIL      = "extra_thumbnail"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    
    // ملاحظة: إذا لم تنقل PrefsManager بعد، سنستخدم قيماً ثابتة لتجاوز خطأ البناء حالياً
    private var streamUrl: String = ""
    private var streamTitle: String = ""

    // قيم افتراضية لتجاوز أخطاء PrefsManager إذا لم يكن جاهزاً
    private val aspectRatios = listOf("16:9", "4:3", "Stretch")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // إعدادات الشاشة الكاملة
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

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

            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("IPTVPlayer/1.0")
                .setAllowCrossProtocolRedirects(true)

            val mediaSource: MediaSource = if (streamUrl.contains(".m3u8", ignoreCase = true) || streamUrl.contains("/live/", ignoreCase = true)) {
                HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(streamUrl))
            } else {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(streamUrl))
            }

            exo.setMediaSource(mediaSource)
            exo.prepare()
            exo.playWhenReady = true

            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    showError("Playback error: ${error.localizedMessage}")
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
        binding.btnAspectRatio.setOnClickListener {
            // منطق تبديل بسيط لتجاوز أخطاء الحزمة حالياً
            Toast.makeText(this, "Changing Aspect Ratio...", Toast.LENGTH_SHORT).show()
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
}
