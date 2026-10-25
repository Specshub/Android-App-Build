package com.iptv.player
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    lateinit var b: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedSettings(this); super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater); setContentView(b.root)
        setupSettings(this)
    }
}
