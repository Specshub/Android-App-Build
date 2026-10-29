package com.iptv.player
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.model.LoginCredentials
import com.iptv.player.databinding.ActivityBrowseBinding

class BrowseActivity : AppCompatActivity() {
    lateinit var b: ActivityBrowseBinding; lateinit var catAdapter: RowAdapter; lateinit var gridAdapter: PosterAdapter
    val vm: MainViewModel by viewModels()
    lateinit var kind: String
    var items: List<PosterItem> = emptyList(); var pendingSeries: Int? = null; var all: List<PosterItem>? = null; var loadingAll = false; var curCat: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityBrowseBinding.inflate(layoutInflater); setContentView(b.root)
        kind = intent.getStringExtra("EXTRA_SECTION") ?: "movies"
        vm.setCredentials(LoginCredentials(intent.getStringExtra(EXTRA_HOST) ?: "", intent.getStringExtra(EXTRA_USERNAME) ?: "", intent.getStringExtra(EXTRA_PASSWORD) ?: ""))
        b.tvHeader.text = when (kind) { "series" -> getString(R.string.sec_series); "kids" -> getString(R.string.sec_kids); else -> getString(R.string.sec_movies) }
        setupBrowse(this); observeBrowse(this); loadCategories(this)
    }
}
