package com.iptv.player
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.iptv.player.databinding.ActivityHomeBinding

fun setupContinue(a: AppCompatActivity, b: ActivityHomeBinding): () -> Unit {
    val ad = PosterAdapter { i -> wsAll(a).firstOrNull { it.k == i.id }?.let { a.startActivity(watchIntent(a, it, it.pos)) } }
    b.rvContinue.layoutManager = LinearLayoutManager(a, LinearLayoutManager.HORIZONTAL, false); b.rvContinue.adapter = ad
    return {
        val l = wsContinue(a)
        ad.submitList(l.map { PosterItem(it.k, if (it.sTitle.isNotEmpty()) it.sTitle + "\n" + it.label else it.title, it.img ?: it.sCover) })
        val v = if (l.isEmpty()) View.GONE else View.VISIBLE; b.rvContinue.visibility = v; b.tvContLabel.visibility = v
    }
}
