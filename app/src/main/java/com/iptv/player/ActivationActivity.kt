package com.iptv.player
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
        b.tvMac.text = mac; b.tvKey.text = key
        addDemoButton(this, b.demoHolder)
        b.btnRefresh.setOnClickListener { lifecycleScope.launch { checkOnce() } }
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { while (true) { checkOnce(); delay(Constants.POLL_INTERVAL_MS) } } }
    }
    private suspend fun checkOnce() {
        b.progress.visibility = View.VISIBLE; val r = DeviceApi.check(mac, key, DeviceManager.info(this)); b.progress.visibility = View.GONE
        if (r.active && r.host != null && r.username != null && r.password != null) saveAndOpenHome(this, r)
        else if (r.error != null) { if (!openSavedIfOffline(this)) b.tvStatus.text = "تعذر الاتصال بالسيرفر" }
        else b.tvStatus.text = when (r.status) { "expired" -> "انتهى الاشتراك. فعّله من الموقع"; "banned" -> "هذا الجهاز موقوف"; "pending" -> "أضف قائمتك من الموقع للبدء"; else -> "في انتظار التفعيل..." }
    }
}
