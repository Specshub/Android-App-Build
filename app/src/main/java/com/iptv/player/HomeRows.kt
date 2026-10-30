package com.iptv.player
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.iptv.player.data.model.*
import com.iptv.player.databinding.ActivityHomeBinding

fun setupHomeRows(a: AppCompatActivity, b: ActivityHomeBinding, c: LoginCredentials) {
    Prefetch.warm(a, c)
    val movies = PosterAdapter { i -> openMovie(a, c, i.id.toInt(), i.title, i.imageUrl, i.ext) }
    val series = PosterAdapter { openSection(a, c, "series") }
    b.rvMovies.layoutManager = LinearLayoutManager(a, LinearLayoutManager.HORIZONTAL, false); b.rvMovies.adapter = movies
    b.rvSeries.layoutManager = LinearLayoutManager(a, LinearLayoutManager.HORIZONTAL, false); b.rvSeries.adapter = series
    loadRow(a, "hm2_${cid(c)}", movies) { latestMovies(a, c) }
    loadRow(a, "hs2_${cid(c)}", series) { latestSeries(a, c) }
}
