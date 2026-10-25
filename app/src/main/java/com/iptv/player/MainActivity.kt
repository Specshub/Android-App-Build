package com.iptv.player
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.*
import com.google.android.material.navigation.NavigationView
import com.iptv.player.data.model.LoginCredentials
import com.iptv.player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityMainBinding
    val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedSettings(this); super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root)
        val h = intent.getStringExtra("EXTRA_HOST") ?: ""; val u = intent.getStringExtra("EXTRA_USERNAME") ?: ""; val p = intent.getStringExtra("EXTRA_PASSWORD") ?: ""
        if (h.isNotEmpty() && u.isNotEmpty()) viewModel.setCredentials(LoginCredentials(h, u, p))
        setSupportActionBar(binding.toolbar); binding.navView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggle); toggle.syncState()
        binding.dashboardView.visibility = View.GONE; binding.fragmentContainer.visibility = View.VISIBLE
        val section = intent.getStringExtra("EXTRA_SECTION")
        if (section != null && savedInstanceState == null) openSectionByName(this, section)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean = handleNavItem(this, item)
    @Suppress("DEPRECATION")
    override fun onBackPressed() { handleBack(this) }
}
