package com.iptv.player
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.iptv.player.data.model.*
import com.iptv.player.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    lateinit var b: ActivitySearchBinding; lateinit var adapter: RowAdapter
    val vm: MainViewModel by viewModels()
    var live: List<LiveStream> = emptyList(); var vod: List<VodStream> = emptyList(); var series: List<Series> = emptyList(); var pendingSeries = false
    val handler = Handler(Looper.getMainLooper()); val run = Runnable { runSearch(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySearchBinding.inflate(layoutInflater); setContentView(b.root)
        vm.setCredentials(LoginCredentials(intent.getStringExtra(EXTRA_HOST) ?: "", intent.getStringExtra(EXTRA_USERNAME) ?: "", intent.getStringExtra(EXTRA_PASSWORD) ?: ""))
        adapter = RowAdapter(false) { r, _ -> onSearchClick(this, r.id) }
        b.rvResults.layoutManager = LinearLayoutManager(this); b.rvResults.adapter = adapter; b.rvResults.itemAnimator = null
        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { handler.removeCallbacks(run); handler.postDelayed(run, 250) }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, n: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, bf: Int, c: Int) {}
        })
        observeSearch(this); vm.loadLiveStreams(null); vm.loadVodStreams(null); vm.loadSeries(null); runSearch(this)
    }
    override fun onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy() }
}
