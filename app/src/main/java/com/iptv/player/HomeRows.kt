package com.iptv.player
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.iptv.player.data.api.*
import com.iptv.player.data.model.*
import com.iptv.player.databinding.ActivityHomeBinding

fun setupHomeRows(a: AppCompatActivity, b: ActivityHomeBinding, c: LoginCredentials) {
    Prefetch.warm(a, c)
    val movies = PosterAdapter { i -> a.startActivity(Intent(a, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_STREAM_TITLE, i.title).putExtra(PlayerActivity.EXTRA_STREAM_URL, XtreamUrlBuilder.vodStreamUrl(c, i.id.toInt(), i.ext ?: "mp4"))) }
    val series = PosterAdapter { openSection(a, c, "series") }
    b.rvMovies.layoutManager = LinearLayoutManager(a, LinearLayoutManager.HORIZONTAL, false); b.rvMovies.adapter = movies
    b.rvSeries.layoutManager = LinearLayoutManager(a, LinearLayoutManager.HORIZONTAL, false); b.rvSeries.adapter = series
    loadRow(a, "hm_${cid(c)}", movies) { Prefetch.vod(a, c)?.sortedByDescending { it.num }?.take(30)?.map { PosterItem(it.streamId.toString(), it.name, it.streamIcon, it.containerExtension) } }
    loadRow(a, "hs_${cid(c)}", series) { Prefetch.series(a, c)?.sortedByDescending { it.num }?.take(30)?.map { PosterItem(it.seriesId.toString(), it.name, it.cover) } }
}
