package com.iptv.player
import android.content.Context
import android.content.res.Configuration
import android.os.*
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import com.iptv.player.data.model.*
import com.iptv.player.databinding.ActivityLiveBinding

class LiveActivity : AppCompatActivity() {
    lateinit var b: ActivityLiveBinding; lateinit var catAdapter: RowAdapter; lateinit var chAdapter: RowAdapter
    val vm: MainViewModel by viewModels()
    var player: ExoPlayer? = null; var url: String? = null; var current: LiveStream? = null; var altTried = false
    var streams: List<LiveStream> = emptyList(); var shown: List<LiveStream> = emptyList(); var catsLoaded = false
    val handler = Handler(Looper.getMainLooper()); val hideNow = Runnable { b.tvNow.visibility = View.GONE }
    val epgCache = HashMap<Int, List<EpgItem>>(); val hideEpg = Runnable { b.epgBar.visibility = View.GONE }
    var allLive: List<LiveStream>? = null; var loadingAll = false; var videoRatio = 0f; val portrait by lazy { resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
    val prefs by lazy { getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLiveBinding.inflate(layoutInflater); setContentView(b.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); hideSystemBars(this)
        vm.setCredentials(LoginCredentials(intent.getStringExtra(EXTRA_HOST) ?: "", intent.getStringExtra(EXTRA_USERNAME) ?: "", intent.getStringExtra(EXTRA_PASSWORD) ?: ""))
        setupLive(this); setupTouch(this); observeLive(this); b.progress.visibility = View.VISIBLE; vm.loadLiveCategories()
    }
    override fun onStart() { super.onStart(); initPlayer(this); url?.let { startUrl(this, it) } }
    override fun onStop() { super.onStop(); releasePlayer(this) }
    override fun onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy() }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = if (handleLiveKey(this, keyCode)) true else super.onKeyDown(keyCode, event)
}
