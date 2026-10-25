package com.iptv.player
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.iptv.player.data.api.*
import com.iptv.player.data.model.*
import com.iptv.player.data.repository.XtreamRepository
import com.iptv.player.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch

fun setupHomeRows(a: AppCompatActivity, b: ActivityHomeBinding, c: LoginCredentials) {
    val repo = XtreamRepository(RetrofitClient.instance.create(XtreamApiService::class.java))
    val movies = PosterAdapter { i -> a.startActivity(Intent(a, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_STREAM_TITLE, i.title).putExtra(PlayerActivity.EXTRA_STREAM_URL, XtreamUrlBuilder.vodStreamUrl(c, i.id.toInt()))) }
    val series = PosterAdapter { openSection(a, c, "series") }
    b.rvMovies.layoutManager = LinearLayoutManager(a, LinearLayoutManager.HORIZONTAL, false); b.rvMovies.adapter = movies
    b.rvSeries.layoutManager = LinearLayoutManager(a, LinearLayoutManager.HORIZONTAL, false); b.rvSeries.adapter = series
    a.lifecycleScope.launch {
        val r = repo.getVodStreams(c)
        if (r is Resource.Success) movies.submitList(r.data.sortedByDescending { it.num }.take(30).map { PosterItem(it.streamId.toString(), it.name, it.streamIcon) })
    }
    a.lifecycleScope.launch {
        val r = repo.getSeries(c)
        if (r is Resource.Success) series.submitList(r.data.sortedByDescending { it.num }.take(30).map { PosterItem(it.seriesId.toString(), it.name, it.cover) })
    }
}
