package com.iptv.player

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels // ✅ استيراد ضروري لتشغيل الـ ViewModel
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.iptv.player.R
import com.iptv.player.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.iptv.player.data.model.LoginCredentials // ✅ استيراد موديل البيانات

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    
    // ✅ استدعاء "القائد" الذي سيوزع البيانات على الشاشات
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ─── 1. استلام بيانات الدخول من شاشة Login ───
        val host = intent.getStringExtra(EXTRA_HOST) ?: ""
        val username = intent.getStringExtra(EXTRA_USERNAME) ?: ""
        val password = intent.getStringExtra(EXTRA_PASSWORD) ?: ""

        // ─── 2. تسليم البيانات للقائد (ViewModel) ───
        if (host.isNotEmpty() && username.isNotEmpty()) {
            val creds = LoginCredentials(host, username, password)
            viewModel.setCredentials(creds)
            
            // 💡 اختياري: نأمره بتحميل أقسام البث المباشر فوراً كبداية
            viewModel.loadLiveCategories() 
        }

        // ─── 3. إعدادات الواجهة (الدرج الجانبي) ───
        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, 
            binding.drawerLayout, 
            binding.toolbar,
            R.string.navigation_drawer_open, 
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            loadFragment(LiveTvFragment())
            binding.navView.setCheckedItem(R.id.nav_live_tv)
            supportActionBar?.title = "Live TV"
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        var title = ""

        when (item.itemId) {
            R.id.nav_live_tv -> {
                fragment = LiveTvFragment()
                title = "Live TV"
                viewModel.loadLiveCategories() // ✅ تحديث البيانات عند فتح القسم
            }
            R.id.nav_movies -> {
                fragment = MoviesFragment()
                title = "Movies"
                viewModel.loadVodCategories() // ✅ تحميل الأفلام عند فتح القسم
            }
            R.id.nav_series -> {
                fragment = SeriesFragment()
                title = "Series"
                viewModel.loadSeriesCategories() // ✅ تحميل المسلسلات عند فتح القسم
            }
            R.id.nav_settings -> {
                title = "Settings"
            }
        }

        if (fragment != null) {
            loadFragment(fragment)
            supportActionBar?.title = title
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
