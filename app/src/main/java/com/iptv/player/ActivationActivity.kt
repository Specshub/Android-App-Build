package com.iptv.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.iptv.player.data.api.ActivationResult
import com.iptv.player.data.api.DeviceApi
import com.iptv.player.data.local.DeviceManager
import com.iptv.player.databinding.ActivityActivationBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActivationBinding
    private lateinit var mac: String
    private lateinit var key: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActivationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mac = DeviceManager.getMac(this)
        key = DeviceManager.getKey(this)

        binding.tvAppName.text = Constants.APP_NAME.uppercase()
        binding.tvSite.text = Constants.ACTIVATION_SITE
        binding.tvMac.text = mac
        binding.tvKey.text = key
        binding.tvPhone.text = Constants.SUPPORT_PHONE
