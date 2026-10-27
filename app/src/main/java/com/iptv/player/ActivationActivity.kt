package com.iptv.player
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
        b = ActivityActivationBinding.inflate(layoutInflater); setContentView(b.root)
        mac = DeviceManager.getMac(this); key = DeviceManager.getKey(this)
        b.tvAppName.text = Constants.APP_NAME.uppercase(); b.tvSite.text = Constants.ACTIVATION_SITE
        b.tvMac.text = mac; b.tvKey.text = key; b.tvPhone.text = Constants.SUPPORT_PHONE; b.btnContact.text = "تواصل عبر واتساب"
        b.tvPhone.textDirection = View.TEXT_DIRECTION_LTR
        addDemoButton(this, b.demoHolder)
        b.btnRefresh.setOnClickListener { lifecycleScope.launch { checkOnce() } }
        b.btnContact.setOnClickListener { runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SUPPORT_WHATSAPP))) } }
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { while (true) { checkOnce(); delay(Constants.POLL_INTERVAL_MS) } } }
    }
    private suspend fun checkOnce() {
        b.progress.visibility = View.VISIBLE; val r = DeviceApi.check(mac, key, DeviceManager.info(this)); b.progress.visibility = View.GONE
        if (r.active && r.host != null && r.username != null && r.password != null) saveAndOpenHome(this, r)
        else b.tvStatus.text = if (r.error != null) "تعذر الاتصال بالسيرفر" else "في انتظار التفعيل..."
    }
}
