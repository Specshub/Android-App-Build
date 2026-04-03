// com/iptvplayer/app/ui/DisclaimerActivity.kt
package com.iptvplayer.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iptvplayer.app.data.local.PrefsManager
import com.iptvplayer.app.databinding.ActivityDisclaimerBinding

class DisclaimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisclaimerBinding
    private lateinit var prefsManager: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisclaimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PrefsManager(this)

        binding.btnAccept.setOnClickListener {
            prefsManager.setDisclaimerAccepted(true)
            finish()
        }

        binding.btnDecline.setOnClickListener {
            // On first launch, user must accept to use the app
            if (!prefsManager.isDisclaimerAccepted()) {
                finishAffinity() // close entire app
            } else {
                finish()
            }
        }
    }
}
