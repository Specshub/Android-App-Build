package com.iptv.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.databinding.ActivityMultiScreenBinding

class MultiScreenActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMultiScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "شاشات متعددة / Multi-Screen"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
