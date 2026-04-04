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

        // إعداد بيانات الدخول
        val host = intent.getStringExtra("EXTRA_HOST") ?: ""
        val username = intent.getStringExtra("EXTRA_USERNAME") ?: ""
        val password = intent.getStringExtra("EXTRA_PASSWORD") ?: ""

        if (host.isNotEmpty() && username.isNotEmpty()) {
            val creds = LoginCredentials(host, username, password)
            viewModel.setCredentials(creds)
        }

        setSupportActionBar(binding.toolbar)

        // إعداد القائمة الجانبية
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
                    handler.postDelayed(this, 5000)
                }
            }
        }
        handler.post(runnable)
    }

    private fun setupDashboardButtons() {
        val buttonsInfo = listOf(
            Triple(R.id.mainBtnLive, "البث المباشر", android.R.drawable.ic_menu_slideshow),
            Triple(R.id.mainBtnMovies, "الأفلام (VOD)", android.R.drawable.ic_menu_gallery),
            Triple(R.id.mainBtnSeries, "المسلسلات", android.R.drawable.ic_menu_recent_history),
            Triple(R.id.mainBtnSchedule, "جدول المباريات", android.R.drawable.ic_menu_today)
        )

        buttonsInfo.forEach { (id, text, icon) ->
            findViewById<View>(id)?.apply {
                findViewById<TextView>(R.id.btnText).text = text
                findViewById<ImageView>(R.id.btnIcon).setImageResource(icon)
                
                // تفعيل دعم التلفاز (Focus)
                isFocusable = true
                isFocusableInTouchMode = true
                
                setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        // تأثير "أسطوري" عند الوقوف بالريموت
                        v.animate().scaleX(1.1f).scaleY(1.1f).alpha(1.0f).setDuration(200).start()
                        v.elevation = 20f
                    } else {
                        v.animate().scaleX(1.0f).scaleY(1.0f).alpha(0.8f).setDuration(200).start()
                        v.elevation = 4f
                    }
                }

                setOnClickListener { 
                    val navId = when(id) {
                        R.id.mainBtnLive -> R.id.nav_live_tv
                        R.id.mainBtnMovies -> R.id.nav_movies
                        R.id.mainBtnSeries -> R.id.nav_series
                        else -> R.id.nav_schedule
                    }
                    navigateToSection(navId) 
                }
            }
        }
    }

    private fun navigateToSection(itemId: Int) {
        binding.dashboardView.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.navView.setCheckedItem(itemId)

        val fragment: Fragment?
        val title: String

        when (itemId) {
            R.id.nav_live_tv -> { fragment = LiveTvFragment(); title = "Live TV"; viewModel.loadLiveCategories() }
            R.id.nav_movies -> { fragment = MoviesFragment(); title = "Movies"; viewModel.loadVodCategories() }
            R.id.nav_series -> { fragment = SeriesFragment(); title = "Series"; viewModel.loadSeriesCategories() }
            R.id.nav_schedule -> { fragment = MatchScheduleFragment(); title = "جدول المباريات" }
            else -> { fragment = null; title = "" }
        }

        fragment?.let {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, it).commit()
            supportActionBar?.title = title
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId in listOf(R.id.nav_live_tv, R.id.nav_movies, R.id.nav_series, R.id.nav_schedule)) {
            navigateToSection(item.itemId)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return true
        }

        when (item.itemId) {
            R.id.nav_favorites -> {
                binding.dashboardView.visibility = View.GONE
                binding.fragmentContainer.visibility = View.VISIBLE
                loadFragment(FavoritesFragment(), "المفضلة")
            }
            R.id.nav_speed_test -> openUrl("https://fast.com/ar/")
            R.id.nav_support -> openUrl("https://api.whatsapp.com/send?phone=212772863204")
            R.id.nav_language -> showLanguageDialog()
            R.id.nav_theme -> toggleTheme()
            R.id.nav_logout -> performLogout()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
        supportActionBar?.title = title
    }

    private fun openUrl(url: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (e: Exception) {}
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
        val checkedItem = langCodes.indexOf(sharedPref.getString("APP_LANG", "ar")).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("اختر لغة التطبيق")
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                sharedPref.edit().putString("APP_LANG", langCodes[which]).apply()
                dialog.dismiss()
                val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Runtime.getRuntime().exit(0)
            }.show()
    }

    private fun toggleTheme() {
        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val isDark = sharedPref.getBoolean("IS_DARK_MODE", true)
        sharedPref.edit().putBoolean("IS_DARK_MODE", !isDark).apply()
        AppCompatDelegate.setDefaultNightMode(if (!isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        
        val intent = intent
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(intent)
    }

    @Suppress("DEPRECATION")
    private fun applySavedSettings() {
        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val isDark = sharedPref.getBoolean("IS_DARK_MODE", true)
        AppCompatDelegate.setDefaultNightMode(if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        val locale = Locale(sharedPref.getString("APP_LANG", "ar") ?: "ar")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (binding.fragmentContainer.visibility == View.VISIBLE) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (fragment is LiveTvFragment && fragment.isShowingStreams) fragment.goBackToCategories()
            else {
                binding.fragmentContainer.visibility = View.GONE
                binding.dashboardView.visibility = View.VISIBLE
                supportActionBar?.title = getString(R.string.app_name)
            }
        } else super.onBackPressed()
    }
}
