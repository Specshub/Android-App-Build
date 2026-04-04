package com.iptv.player // ✅ تم تصحيح اسم الحزمة ليتطابق مع المجلد

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
// ملاحظة: تأكد من صحة مسارات الـ imports التالية بناءً على مشروعك
import com.iptv.player.data.model.Resource
import com.iptv.player.databinding.ActivityLoginBinding
import com.iptv.player.ui.viewmodel.LoginViewModel
import com.iptv.player.util.AdManager

// ✅ تعريف الثوابت هنا ينهي مشكلة Unresolved reference للأبد
const val EXTRA_HOST = "EXTRA_HOST"
const val EXTRA_USERNAME = "EXTRA_USERNAME"
const val EXTRA_PASSWORD = "EXTRA_PASSWORD"

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdManager.initialize(this)

        if (!viewModel.isDisclaimerAccepted()) {
            startActivity(Intent(this, DisclaimerActivity::class.java))
        }

        setupBannerAd()
        prefillSavedCredentials()
        setupObservers()
        setupClickListeners()
    }

    private fun prefillSavedCredentials() {
        viewModel.getSavedCredentials()?.let { creds ->
            binding.etHost.setText(creds.host)
            binding.etUsername.setText(creds.username)
            binding.etPassword.setText(creds.password)
            binding.cbRememberLogin.isChecked = true
        }
    }

    private fun setupBannerAd() {
        adView = AdView(this).apply {
            adUnitId = AdManager.BANNER_AD_UNIT_ID
            setAdSize(AdSize.BANNER)
        }
        binding.adContainer.addView(adView)
        AdManager.loadBannerAd(adView!!)
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "Connecting…"
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Connect"

                    AdManager.loadInterstitialAd(this)

                    val intent = Intent(this, MainActivity::class.java).apply {
                        // ✅ نستخدم الثوابت المعرفة بالأعلى مباشرة
                        putExtra(EXTRA_HOST, binding.etHost.text.toString().trim())
                        putExtra(EXTRA_USERNAME, binding.etUsername.text.toString().trim())
                        putExtra(EXTRA_PASSWORD, binding.etPassword.text.toString().trim())
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Connect"
                    showError(resource.message)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val host = binding.etHost.text.toString()
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val remember = binding.cbRememberLogin.isChecked
            viewModel.login(host, username, password, remember)
        }

        binding.tvDisclaimer.setOnClickListener {
            startActivity(Intent(this, DisclaimerActivity::class.java))
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(android.R.color.holo_red_dark))
            .setTextColor(getColor(android.R.color.white))
            .show()
    }

    override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView?.resume()
    }
}
