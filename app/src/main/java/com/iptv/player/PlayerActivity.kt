package com.iptv.player
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import com.iptv.player.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_STREAM_TITLE = "extra_stream_title"
        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_CONTENT_TYPE = "extra_content_type"
        const val EXTRA_THUMBNAIL = "extra_thumbnail"
    }
    lateinit var binding: ActivityPlayerBinding
    var player: ExoPlayer? = null; var streamUrl = ""; var streamTitle = ""; var ratioIdx = 0; var watch: WEntry? = null; var nextEntry: WEntry? = null
    val prefs by lazy { getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater); setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); hidePlayerBars(this)
        streamUrl = intent.getStringExtra(EXTRA_STREAM_URL) ?: ""; streamTitle = intent.getStringExtra(EXTRA_STREAM_TITLE) ?: "Now Playing"
        watch = entryFromJson(intent.getStringExtra("W_JSON"))
        binding.tvTitle.text = streamTitle
        setupPlayerUi(this); initializePlayer(this); prefetchNext(this)
    }
    override fun onStop() { super.onStop(); savePosition(this); player?.pause() }
    override fun onDestroy() { releasePlayer(this); super.onDestroy() }
}
