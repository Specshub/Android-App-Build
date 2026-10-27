package com.iptv.player
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.iptv.player.data.model.*
import com.iptv.player.databinding.ActivitySeriesBinding

fun openSeries(a: AppCompatActivity, c: LoginCredentials, id: Int, title: String, cover: String?) {
    a.startActivity(Intent(a, SeriesActivity::class.java).putExtra(EXTRA_HOST, c.host).putExtra(EXTRA_USERNAME, c.username).putExtra(EXTRA_PASSWORD, c.password).putExtra("S_ID", id).putExtra("S_TITLE", title).putExtra("S_COVER", cover))
}
class SeriesActivity : AppCompatActivity() {
    lateinit var b: ActivitySeriesBinding; lateinit var seasonAdapter: RowAdapter; lateinit var epAdapter: RowAdapter; lateinit var title: String
    val vm: MainViewModel by viewModels()
    var seasons: Map<String, List<Episode>> = emptyMap()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySeriesBinding.inflate(layoutInflater); setContentView(b.root)
        vm.setCredentials(LoginCredentials(intent.getStringExtra(EXTRA_HOST) ?: "", intent.getStringExtra(EXTRA_USERNAME) ?: "", intent.getStringExtra(EXTRA_PASSWORD) ?: ""))
        title = intent.getStringExtra("S_TITLE") ?: ""; b.tvSeriesTitle.text = title
        Glide.with(this).load(intent.getStringExtra("S_COVER")).override(300, 450).centerCrop().into(b.ivCover)
        seasonAdapter = RowAdapter(false, R.layout.item_chip) { r, _ -> selectSeason(this, r.id) }; epAdapter = RowAdapter(true, R.layout.item_episode) { r, _ -> playEpisode(this, r.id) }
        b.rvSeasons.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false); b.rvSeasons.adapter = seasonAdapter; b.rvSeasons.itemAnimator = null
        b.rvEpisodes.layoutManager = LinearLayoutManager(this); b.rvEpisodes.adapter = epAdapter; b.rvEpisodes.itemAnimator = null
        observeSeries(this); vm.loadSeriesInfo(intent.getIntExtra("S_ID", 0))
    }
}
