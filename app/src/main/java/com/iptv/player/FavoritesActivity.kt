package com.iptv.player
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.iptv.player.data.model.*
import com.iptv.player.databinding.ActivityFavoritesBinding

class FavoritesActivity : AppCompatActivity() {
    lateinit var b: ActivityFavoritesBinding; lateinit var adapter: RowAdapter; lateinit var creds: LoginCredentials
    var history = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityFavoritesBinding.inflate(layoutInflater); setContentView(b.root)
        creds = LoginCredentials(intent.getStringExtra(EXTRA_HOST) ?: "", intent.getStringExtra(EXTRA_USERNAME) ?: "", intent.getStringExtra(EXTRA_PASSWORD) ?: "")
        history = intent.getStringExtra("EXTRA_SECTION") == "history"
        if (history) { b.tvHead.text = getString(R.string.nav_history); b.tvHead.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_nav_history, 0, 0, 0); b.tvEmpty.text = getString(R.string.hist_empty) }
        b.tvHint.text = getString(R.string.hist_hint)
        adapter = RowAdapter(true) { r, _ -> playFav(this, r.id) }; adapter.onLong = { r -> removeFav(this, r.id) }
        b.rvFavs.layoutManager = LinearLayoutManager(this); b.rvFavs.adapter = adapter; b.rvFavs.itemAnimator = null
    }
    override fun onResume() { super.onResume(); refreshFavs(this) }
}
