package com.iptv.player
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.model.LoginCredentials
import com.iptv.player.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private var tv: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val p = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val host = intent.getStringExtra(EXTRA_HOST) ?: p.getString("HOST", null)
        val user = intent.getStringExtra(EXTRA_USERNAME) ?: p.getString("USER", null)
        val pass = intent.getStringExtra(EXTRA_PASSWORD) ?: p.getString("PASS", null)
        if (host.isNullOrEmpty() || user.isNullOrEmpty() || pass.isNullOrEmpty()) { startActivity(Intent(this, ActivationActivity::class.java)); finish(); return }
        val creds = LoginCredentials(host, user, pass)
        val b = ActivityHomeBinding.inflate(layoutInflater); setContentView(b.root)
        tv = b.tvExpiry; b.tvExpiry.text = subLabel(this)
        setupHomeButtons(this, b.buttonsContainer, creds)
        setupHomeRows(this, b, creds)
        refreshSub(creds) { tv?.text = subLabel(this).ifEmpty { savedLabel(this) } }
    }
    override fun onResume() { super.onResume(); resyncDevice { } }
}
