package com.iptv.player
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.model.LoginCredentials
import com.iptv.player.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private var tv: TextView? = null; private var refreshCont: (() -> Unit)? = null; private var empty = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("EXTRA_EMPTY", false)) {
            empty = true
            val e = ActivityHomeBinding.inflate(layoutInflater); setContentView(e.root)
            e.tvExpiry.text = getString(R.string.empty_banner); val dc = demoCreds(); setupHomeButtons(this, e.buttonsContainer, dc, demo = true); setupHomeRows(this, e, dc); return
        }
        val p = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val host = intent.getStringExtra(EXTRA_HOST) ?: p.getString("HOST", null)
        val user = intent.getStringExtra(EXTRA_USERNAME) ?: p.getString("USER", null)
        val pass = intent.getStringExtra(EXTRA_PASSWORD) ?: p.getString("PASS", null)
        if (host.isNullOrEmpty() || user.isNullOrEmpty() || pass.isNullOrEmpty()) { startActivity(Intent(this, ActivationActivity::class.java)); finish(); return }
        val creds = LoginCredentials(host, user, pass)
        val b = ActivityHomeBinding.inflate(layoutInflater); setContentView(b.root)
        tv = b.tvExpiry; b.tvExpiry.text = subLabel(this)
        setupHomeButtons(this, b.buttonsContainer, creds)
        setupHomeRows(this, b, creds); refreshCont = setupContinue(this, b)
        refreshSub(creds) { tv?.text = subLabel(this).ifEmpty { savedLabel(this) } }
    }
    override fun onResume() { super.onResume(); refreshCont?.invoke(); if (!empty) resyncDevice { } }
}
