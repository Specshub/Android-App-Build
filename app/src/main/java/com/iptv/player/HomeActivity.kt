package com.iptv.player
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.model.LoginCredentials
import com.iptv.player.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val p = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val host = intent.getStringExtra(EXTRA_HOST) ?: p.getString("HOST", null)
        val user = intent.getStringExtra(EXTRA_USERNAME) ?: p.getString("USER", null)
        val pass = intent.getStringExtra(EXTRA_PASSWORD) ?: p.getString("PASS", null)
        if (host.isNullOrEmpty() || user.isNullOrEmpty() || pass.isNullOrEmpty()) { startActivity(Intent(this, ActivationActivity::class.java)); finish(); return }
        val creds = LoginCredentials(host, user, pass)
        val b = ActivityHomeBinding.inflate(layoutInflater); setContentView(b.root)
        val exp = intent.getStringExtra("EXTRA_EXPIRES") ?: p.getString("EXPIRES", null)
        b.tvExpiry.text = if (exp.isNullOrEmpty()) "" else "سينتهي الاشتراك خلال: $exp"
        setupHomeButtons(this, b.buttonsContainer, creds)
        setupHomeRows(this, b, creds)
    }
}
