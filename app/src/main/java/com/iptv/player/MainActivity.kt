package com.iptv.player

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.iptv.player.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.iptv.player.data.model.LoginCredentials
import java.util.Locale

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedSettings()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val host = intent.getStringExtra("EXTRA_HOST") ?: ""
        val username = intent.getStringExtra("EXTRA_USERNAME") ?: ""
        val password = intent.getStringExtra("EXTRA_PASSWORD") ?: ""

        if (host.isNotEmpty() && username.isNotEmpty()) {
            val creds = LoginCredentials(host, username, password)
            viewModel.setCredentials(creds)
        }

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            binding.dashboardView.visibility = View.VISIBLE
            binding.fragmentContainer.visibility = View.GONE
            supportActionBar?.title = "الرئيسية / Dashboard"
        }

        setupDashboardButtons()
        setupAnimatedBanner()
    }

    private fun setupAnimatedBanner() {
        val bannerImage = findViewById<ImageView>(R.id.bannerImage) ?: return
        val banners = listOf(
            "https://image.tmdb.org/t/p/original/8Y43POKjjKDGI9MH89NW0NAzzp8.jpg", 
            "https://image.tmdb.org/t/p/original/t5zCBSB5xMDKcDqe91qahCOUYVV.jpg",
            "https://image.tmdb.org/t/p/original/1X7vow16X7CnCoexXh4H4F2yDJv.jpg"
        )
        val handler = Handler(Looper.getMainLooper())
        var currentIndex = 0

        val runnable = object : Runnable {
            override fun run() {
                Glide.with(this@MainActivity)
                    .load(banners[currentIndex])
                    .transition(DrawableTransitionOptions.withCrossFade(1000))
                    .centerCrop()
                    .into(bannerImage)

                currentIndex = (currentIndex + 1) % banners.size
                handler.postDelayed(this, 4000)
            }
        }
        handler.post(runnable)
    }

    // ─── إعداد الأزرار والأيقونات الجديدة ───
    private fun setupDashboardButtons() {
        findViewById<View>(R.id.mainBtnLive).apply {
            findViewById<TextView>(R.id.btnText).text = "البث المباشر"
            findViewById<ImageView>(R.id.btnIcon).setImageResource(android.R.drawable.ic_menu_slideshow) // أيقونة عرض
            setOnClickListener { navigateToSection(R.id.nav_live_tv) }
        }

        findViewById<View>(R.id.mainBtnMovies).apply {
            findViewById<TextView>(R.id.btnText).text = "الأفلام (VOD)"
            findViewById<ImageView>(R.id.btnIcon).setImageResource(android.R.drawable.ic_menu_gallery) // أيقونة صور/أفلام
            setOnClickListener { navigateToSection(R.id.nav_movies) }
        }

        findViewById<View>(R.id.mainBtnSeries).apply {
            findViewById<TextView>(R.id.btnText).text = "المسلسلات"
            findViewById<ImageView>(R.id.btnIcon).setImageResource(android.R.drawable.ic_menu_recent_history) // أيقونة متسلسلة
            setOnClickListener { navigateToSection(R.id.nav_series) }
        }

        findViewById<View>(R.id.mainBtnSchedule).apply {
            findViewById<TextView>(R.id.btnText).text = "جدول المباريات"
            findViewById<ImageView>(R.id.btnIcon).setImageResource(android.R.drawable.ic_menu_today) // أيقونة جدول/تقويم
            setOnClickListener { navigateToSection(R.id.nav_schedule) }
        }
    }

    // ─── الدالة القوية للانتقال ───
    private fun navigateToSection(itemId: Int) {
        binding.dashboardView.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.navView.setCheckedItem(itemId)

        var fragment: Fragment? = null
        var title = ""

        when (itemId) {
            R.id.nav_live_tv -> {
                fragment = LiveTvFragment()
                title = "Live TV"
                viewModel.loadLiveCategories()
            }
            R.id.nav_movies -> {
                fragment = MoviesFragment()
                title = "Movies"
                viewModel.loadVodCategories()
            }
            R.id.nav_series -> {
                fragment = SeriesFragment()
                title = "Series"
                viewModel.loadSeriesCategories()
            }
            R.id.nav_schedule -> {
                fragment = MatchScheduleFragment()
                title = "جدول المباريات"
            }
        }

        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
            supportActionBar?.title = title
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // إذا كان الاختيار من الأقسام الأساسية، نستخدم دالة الانتقال القوية
        if (item.itemId in listOf(R.id.nav_live_tv, R.id.nav_movies, R.id.nav_series, R.id.nav_schedule)) {
            navigateToSection(item.itemId)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }

        // للأزرار الأخرى في القائمة الجانبية
        var fragment: Fragment? = null
        var title = ""

        when (item.itemId) {
            R.id.nav_favorites -> {
                binding.dashboardView.visibility = View.GONE
                binding.fragmentContainer.visibility = View.VISIBLE
                fragment = FavoritesFragment()
                title = "المفضلة / Favorites"
            }
            R.id.nav_speed_test -> { openSpeedTest(); binding.drawerLayout.closeDrawer(GravityCompat.START); return true }
            R.id.nav_clear_cache -> { clearAppCache(); binding.drawerLayout.closeDrawer(GravityCompat.START); return true }
            R.id.nav_support -> { openLiveSupport(); binding.drawerLayout.closeDrawer(GravityCompat.START); return true }
            R.id.nav_multi_screen -> {
                startActivity(Intent(this, MultiScreenActivity::class.java))
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_language -> { showLanguageDialog(); return true }
            R.id.nav_theme -> { toggleTheme(); return true }
            R.id.nav_settings -> {
                Toast.makeText(this, "الإعدادات قيد التطوير ⚙️", Toast.LENGTH_SHORT).show()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_logout -> { performLogout(); return true }
        }

        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
            supportActionBar?.title = title
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun clearAppCache() {
        try {
            cacheDir.deleteRecursively()
            Toast.makeText(this, "تم تنظيف الذاكرة المؤقتة بنجاح! 🚀", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {}
    }

    private fun openSpeedTest() {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://fast.com/ar/"))) } catch (e: Exception) {}
    }

    private fun openLiveSupport() {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=212772863204&text=مرحباً"))) } catch (e: Exception) {}
    }

    private fun performLogout() {
        getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("العربية", "English", "Français")
        val langCodes = arrayOf("ar", "en", "fr")
        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val currentLang = sharedPref.getString("APP_LANG", "ar")
        val checkedItem = langCodes.indexOf(currentLang).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this)
            .setTitle("اختر لغة التطبيق")
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                sharedPref.edit().putString("APP_LANG", langCodes[which]).apply()
                dialog.dismiss()
                finish()
                startActivity(intent)
            }
            .show()
    }

    private fun toggleTheme() {
        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val isDark = sharedPref.getBoolean("IS_DARK_MODE", true)
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPref.edit().putBoolean("IS_DARK_MODE", false).apply()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPref.edit().putBoolean("IS_DARK_MODE", true).apply()
        }
    }

    @Suppress("DEPRECATION")
    private fun applySavedSettings() {
        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val locale = Locale(sharedPref.getString("APP_LANG", "ar") ?: "ar")
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        AppCompatDelegate.setDefaultNightMode(
            if (sharedPref.getBoolean("IS_DARK_MODE", true)) AppCompatDelegate.MODE_NIGHT_YES 
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (binding.fragmentContainer.visibility == View.VISIBLE) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

            when {
                fragment is LiveTvFragment && fragment.isShowingStreams -> fragment.goBackToCategories()
                fragment is MoviesFragment && fragment.isShowingStreams -> fragment.goBackToCategories()
                fragment is SeriesFragment && fragment.isShowingStreams -> fragment.goBackToCategories()
                else -> {
                    binding.fragmentContainer.visibility = View.GONE
                    binding.dashboardView.visibility = View.VISIBLE
                    supportActionBar?.title = "الرئيسية / Dashboard"
                }
            }
        } else {
            super.onBackPressed()
        }
    }
}
