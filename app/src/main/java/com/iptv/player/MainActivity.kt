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
        // يجب استدعاء تطبيق الإعدادات قبل super.onCreate لضمان رسم الواجهة بالثيم واللغة الصحيحة
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
            supportActionBar?.title = getString(R.string.app_name)
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
                if (!isFinishing) {
                    Glide.with(this@MainActivity)
                        .load(banners[currentIndex])
                        .transition(DrawableTransitionOptions.withCrossFade(1000))
                        .centerCrop()
                        .into(bannerImage)

                    currentIndex = (currentIndex + 1) % banners.size
                    handler.postDelayed(this, 4000)
                }
            }
        }
        handler.post(runnable)
    }

    private fun setupDashboardButtons() {
        findViewById<View>(R.id.mainBtnLive).apply {
            findViewById<TextView>(R.id.btnText).text = "البث المباشر"
            findViewById<ImageView>(R.id.btnIcon).setImageResource(android.R.drawable.ic_menu_slideshow)
            setOnClickListener { navigateToSection(R.id.nav_live_tv) }
        }

        findViewById<View>(R.id.mainBtnMovies).apply {
            findViewById<TextView>(R.id.btnText).text = "الأفلام (VOD)"
            findViewById<ImageView>(R.id.btnIcon).setImageResource(android.R.drawable.ic_menu_gallery)
            setOnClickListener { navigateToSection(R.id.nav_movies) }
        }

        findViewById<View>(R.id.mainBtnSeries).apply {
            findViewById<TextView>(R.id.btnText).text = "المسلسلات"
            findViewById<ImageView>(R.id.btnIcon).setImageResource(android.R.drawable.ic_menu_recent_history)
            setOnClickListener { navigateToSection(R.id.nav_series) }
        }

        findViewById<View>(R.id.mainBtnSchedule).apply {
            findViewById<TextView>(R.id.btnText).text = "جدول المباريات"
            findViewById<ImageView>(R.id.btnIcon).setImageResource(android.R.drawable.ic_menu_today)
            setOnClickListener { navigateToSection(R.id.nav_schedule) }
        }
    }

    private fun navigateToSection(itemId: Int) {
        binding.dashboardView.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.navView.setCheckedItem(itemId)

        var fragment: Fragment? = null
        var title = ""

        when (itemId) {
            R.id.nav_live_tv -> { fragment = LiveTvFragment(); title = "Live TV"; viewModel.loadLiveCategories() }
            R.id.nav_movies -> { fragment = MoviesFragment(); title = "Movies"; viewModel.loadVodCategories() }
            R.id.nav_series -> { fragment = SeriesFragment(); title = "Series"; viewModel.loadSeriesCategories() }
            R.id.nav_schedule -> { fragment = MatchScheduleFragment(); title = "جدول المباريات" }
        }

        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
            supportActionBar?.title = title
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId in listOf(R.id.nav_live_tv, R.id.nav_movies, R.id.nav_series, R.id.nav_schedule)) {
            navigateToSection(item.itemId)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }

        var fragment: Fragment? = null
        var title = ""

        when (item.itemId) {
            R.id.nav_favorites -> {
                binding.dashboardView.visibility = View.GONE
                binding.fragmentContainer.visibility = View.VISIBLE
                fragment = FavoritesFragment()
                title = "المفضلة / Favorites"
            }
            R.id.nav_speed_test -> { openSpeedTest(); return true }
            R.id.nav_clear_cache -> { clearAppCache(); return true }
            R.id.nav_support -> { openLiveSupport(); return true }
            R.id.nav_multi_screen -> {
                startActivity(Intent(this, MultiScreenActivity::class.java))
                return true
            }
            R.id.nav_language -> { showLanguageDialog(); return true }
            R.id.nav_theme -> { toggleTheme(); return true }
            R.id.nav_settings -> {
                Toast.makeText(this, "الإعدادات قيد التطوير ⚙️", Toast.LENGTH_SHORT).show()
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
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun openSpeedTest() {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://fast.com/ar/"))) } catch (e: Exception) {}
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun openLiveSupport() {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=212772863204&text=مرحباً"))) } catch (e: Exception) {}
        binding.drawerLayout.closeDrawer(GravityCompat.START)
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
            .setTitle("اختر لغة التطبيق / Choose Language")
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val newLang = langCodes[which]
                sharedPref.edit().putString("APP_LANG", newLang).apply()
                dialog.dismiss()
                
                // الحل النهائي: إعادة تشغيل النشاط بـ Intent جديد وتنظيف الـ Task
                val restartIntent = Intent(this, MainActivity::class.java)
                restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(restartIntent)
                finish()
                // إضافة خروج اختياري لضمان قتل العملية القديمة
                Runtime.getRuntime().exit(0)
            }
            .show()
    }

    private fun toggleTheme() {
        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val isDark = sharedPref.getBoolean("IS_DARK_MODE", true)
        val newMode = !isDark
        
        sharedPref.edit().putBoolean("IS_DARK_MODE", newMode).apply()
        
        // تطبيق المظهر فوراً قبل الـ recreate
        AppCompatDelegate.setDefaultNightMode(
            if (newMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        
        // استخدام recreate بشكل آمن
        val intent = intent
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(intent)
    }

    @Suppress("DEPRECATION")
    private fun applySavedSettings() {
        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        
        // ضبط المظهر
        val isDark = sharedPref.getBoolean("IS_DARK_MODE", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        // ضبط اللغة وتحديث الـ Configuration بشكل كامل
        val localeCode = sharedPref.getString("APP_LANG", "ar") ?: "ar"
        val locale = Locale(localeCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale) // مهم جداً للأجهزة الحديثة لضبط اتجاه الواجهة
        
        // تحديث المصادر
        resources.updateConfiguration(config, resources.displayMetrics)
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
                    supportActionBar?.title = getString(R.string.app_name)
                }
            }
        } else {
            super.onBackPressed()
        }
    }
}
