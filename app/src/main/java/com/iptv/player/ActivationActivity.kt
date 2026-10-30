package com.iptv.player
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.iptv.player.data.api.DeviceApi
import com.iptv.player.data.local.DeviceManager
import com.iptv.player.databinding.ActivityActivationBinding
import kotlinx.coroutines.*

class ActivationActivity : AppCompatActivity() {
    private lateinit var b: ActivityActivationBinding; private lateinit var mac: String; private lateinit var key: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sp = ipPrefs(this); if (sp.getBoolean("MANUAL", false) && !sp.getString("HOST", null).isNullOrEmpty()) { startActivity(Intent(this, HomeActivity::class.java)); finish(); return }
        b = ActivityActivationBinding.inflate(layoutInflater); setContentView(b.root)
        mac = DeviceManager.getMac(this); key = DeviceManager.getKey(this)
        b.tvAppName.text = Constants.APP_NAME.uppercase(); b.tvSite.text = Constants.ACTIVATION_SITE
        b.tvMac.text = mac; b.tvKey.text = key
        showQr(Constants.ACTIVATION_SITE + "/login.php?mac=" + mac + "#k=" + key)
        findViewById<LinearLayout>(R.id.demoHolder)?.let { h -> h.removeAllViews(); val ok = layoutInflater.inflate(R.layout.item_ok_btn, h, false); ok.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java).putExtra("EXTRA_EMPTY", true)) }; h.addView(ok) }
        b.btnRefresh.setOnClickListener { lifecycleScope.launch { checkOnce() } }
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { while (true) { checkOnce(); delay(Constants.POLL_INTERVAL_MS) } } }
    }
    private suspend fun checkOnce() {
        b.progress.visibility = View.VISIBLE; val r = DeviceApi.check(mac, key, DeviceManager.info(this)); b.progress.visibility = View.GONE
        if (r.active && r.host != null && r.username != null && r.password != null) saveAndOpenHome(this, r)
        else if (r.error != null) { if (!openSavedIfOffline(this)) b.tvStatus.text = getString(R.string.status_connect_fail) }
        else b.tvStatus.text = when (r.status) { "expired" -> getString(R.string.status_expired); "banned" -> getString(R.string.status_banned); "pending" -> getString(R.string.status_pending); else -> getString(R.string.status_waiting) }
    }
}
