package com.iptv.player

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory // 🚀 السحر الجديد هنا
import androidx.media3.ui.AspectRatioFrameLayout
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
    
    private var streamUrl: String = ""
    private var streamTitle: String = ""

    private val aspectRatios = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_FILL,
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    )
    private var currentAspectRatioIndex = 0

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
            showError("الرابط مفقود (Stream URL is missing)")
            return
        }

        // 1. التخفي كمتصفح جوجل كروم لتخطي حماية السيرفر
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        // 2. 🚀 المصنع الذكي: يكتشف نوع البث تلقائياً (TS, M3U8, MP4) بدون الحاجة لصيغة في الرابط
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        // 3. بناء المشغل
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().also { exo ->
            
            binding.playerView.player = exo

            // 4. وضع الرابط وتشغيله
            exo.setMediaItem(MediaItem.fromUri(streamUrl))
            exo.prepare()
            exo.playWhenReady = true

            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    // 🔍 إظهار الخطأ التقني بدقة لنعرف سبب المشكلة الحقيقي لو فشل التشغيل
                    val cause = error.cause?.message ?: error.errorCodeName
                    showError("الخطأ: $cause\nالرابط: $streamUrl")
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
            currentAspectRatioIndex = (currentAspectRatioIndex + 1) % aspectRatios.size
            binding.playerView.resizeMode = aspectRatios[currentAspectRatioIndex]
            
            val ratioName = when(currentAspectRatioIndex) {
                0 -> "تلقائي (Fit)"
                1 -> "تمدد (Stretch)"
                else -> "تكبير (Zoom)"
            }
            Toast.makeText(this, "وضع الشاشة: $ratioName", Toast.LENGTH_SHORT).show()
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
            binding.progressBar.visibility = View.VISIBLE
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
