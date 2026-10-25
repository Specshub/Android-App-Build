package com.iptv.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.iptv.player.data.api.RetrofitClient
import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.api.XtreamUrlBuilder
import com.iptv.player.data.model.LoginCredentials
import com.iptv.player.data.model.Resource
import com.iptv.player.data.repository.XtreamRepository
import com.iptv.player.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var creds: LoginCredentials
    private val repository by lazy {
        XtreamRepository(RetrofitClient.instance.create(XtreamApiService::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val host = intent.getStringExtra(EXTRA_HOST) ?: prefs.getString("HOST", null)
        val user = intent.getStringExtra(EXTRA_USERNAME) ?: prefs.getString("USER", null)
        val pass = intent.getStringExtra(EXTRA_PASSWORD) ?: prefs.getString("PASS", null)
