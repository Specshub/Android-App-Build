package com.iptv.player
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.iptv.player.data.api.XtreamUrlBuilder
import com.iptv.player.data.model.LoginCredentials
import com.iptv.player.data.model.VodStream
import com.iptv.player.databinding.ActivityMovieDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

private val mdHttp = OkHttpClient.Builder().connectTimeout(12, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS).build()
private fun JSONObject.s(vararg k: String): String = k.firstNotNullOfOrNull { n -> optString(n, "").trim().takeIf { it.isNotEmpty() && it != "null" } } ?: ""

class MovieDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityMovieDetailBinding.inflate(layoutInflater); setContentView(b.root)
        val c = LoginCredentials(intent.getStringExtra(EXTRA_HOST) ?: "", intent.getStringExtra(EXTRA_USERNAME) ?: "", intent.getStringExtra(EXTRA_PASSWORD) ?: "")
        val id = intent.getIntExtra("MV_ID", 0); val title = intent.getStringExtra("MV_TITLE") ?: ""; val img = intent.getStringExtra("MV_IMG"); val ext = intent.getStringExtra("MV_EXT") ?: "mp4"
        b.tvTitle.text = title; b.tvPlot.text = ""; b.tvMeta.visibility = View.GONE; b.tvCredits.visibility = View.GONE
        Glide.with(this).load(thumb(img)).centerCrop().into(b.ivPoster); Glide.with(this).load(thumb(img)).centerCrop().into(b.ivBack)
        b.btnBack.setOnClickListener { finish() }
        val entry = WEntry("M$id", title, img, XtreamUrlBuilder.vodStreamUrl(c, id, ext), 0, 0, 0)
        val h = wsAll(this).firstOrNull { it.k == entry.k }?.takeIf { !it.fin() && it.pos > 10_000 }
        if (h != null) b.btnPlay.text = getString(R.string.md_resume, (h.pos / 1000).let { String.format(java.util.Locale.US, "%d:%02d", it / 60, it % 60) })
        b.btnPlay.setOnClickListener { startActivity(watchIntent(this, entry, h?.pos ?: 0L)) }
        val vs = VodStream(name = title, streamId = id, streamIcon = img, containerExtension = ext)
        fun favOn() = FavoritesManager.getVodFavorites(this).any { it.streamId == id }
        fun favUpd() { b.btnFav.text = getString(if (favOn()) R.string.fav_in else R.string.fav_add) }
        favUpd(); b.btnFav.setOnClickListener { if (favOn()) { FavoritesManager.removeVodFavorite(this, vs); Toast.makeText(this, getString(R.string.fav_removed), Toast.LENGTH_SHORT).show() } else { FavoritesManager.addVodFavorite(this, vs); Toast.makeText(this, getString(R.string.fav_added), Toast.LENGTH_SHORT).show() }; favUpd() }
        b.btnPlay.requestFocus()
        lifecycleScope.launch {
            val i = withContext(Dispatchers.IO) { runCatching {
                val u = c.host.trimEnd('/') + "/player_api.php?username=" + URLEncoder.encode(c.username, "UTF-8") + "&password=" + URLEncoder.encode(c.password, "UTF-8") + "&action=get_vod_info&vod_id=" + id
                mdHttp.newCall(Request.Builder().url(u).header("User-Agent", "Mozilla/5.0").build()).execute().use { JSONObject(it.body?.string() ?: "{}").optJSONObject("info") }
            }.getOrNull() }
            b.progress.visibility = View.GONE; b.tvPlotLabel.visibility = View.VISIBLE
            if (i == null) { b.tvPlot.text = getString(R.string.md_no_plot); return@launch }
            b.tvPlot.text = i.s("plot", "description").ifEmpty { getString(R.string.md_no_plot) }
            val bd = i.optJSONArray("backdrop_path")?.optString(0, "") ?: i.s("backdrop_path")
            if (bd.isNotEmpty()) Glide.with(this@MovieDetailActivity).load(bd).centerCrop().into(b.ivBack)
            if (img.isNullOrEmpty()) i.s("movie_image", "cover_big").takeIf { it.isNotEmpty() }?.let { Glide.with(this@MovieDetailActivity).load(it).centerCrop().into(b.ivPoster) }
            val rate = i.s("rating").ifEmpty { i.s("rating_5based") }.takeIf { it != "0" && it != "0.0" }
            val meta = listOfNotNull(i.s("releasedate", "release_date").take(4).ifEmpty { null }, i.s("duration").ifEmpty { null }, i.s("genre").ifEmpty { null }, rate?.let { "★ $it" })
            if (meta.isNotEmpty()) { b.tvMeta.text = meta.joinToString("  •  "); b.tvMeta.visibility = View.VISIBLE }
            val cr = listOfNotNull(i.s("director").ifEmpty { null }?.let { getString(R.string.md_director, it) }, i.s("cast", "actors").ifEmpty { null }?.let { getString(R.string.md_cast, it) })
            if (cr.isNotEmpty()) { b.tvCredits.text = cr.joinToString("\n"); b.tvCredits.visibility = View.VISIBLE }
        }
    }
}
