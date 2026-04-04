package com.iptv.player

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
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

        val host = intent.getStringExtra(EXTRA_HOST) ?: ""
        val username = intent.getStringExtra(EXTRA_USERNAME) ?: ""
        val password = intent.getStringExtra(EXTRA_PASSWORD) ?: ""

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
            loadFragment(LiveTvFragment())
            binding.navView.setCheckedItem(R.id.nav_live_tv)
            supportActionBar?.title = "Live TV"
            viewModel.loadLiveCategories()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        var title = ""

        when (item.itemId) {
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
            
            // ─── ميزة فحص السرعة الحقيقية ───
            R.id.nav_speed_test -> {
                openSpeedTest()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }

            // ─── ميزة تسريع التطبيق ───
            R.id.nav_clear_cache -> {
                clearAppCache()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }

            // ─── ميزة الدعم الفني المباشر (لطلب الاشتراك) ───
            R.id.nav_support -> {
                openLiveSupport()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }

            // ─── الميزات المعقدة (تحتاج شاشات جديدة سنصنعها معاً) ───
            R.id.nav_favorites, 
            R.id.nav_downloads, 
            R.id.nav_multi_screen, 
            R.id.nav_schedule, 
            R.id.nav_kids_mode -> {
                Toast.makeText(this, "جاري برمجة هذه الميزة وإضافتها للتطبيق...", Toast.LENGTH_LONG).show()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }

            R.id.nav_language -> {
                showLanguageDialog()
                return true
            }
            R.id.nav_theme -> {
                toggleTheme()
                return true
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "الإعدادات قيد التطوير ⚙️", Toast.LENGTH_SHORT).show()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.nav_logout -> {
                performLogout()
                return true
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

    private fun clearAppCache() {
        try {
            cacheDir.deleteRecursively()
            Toast.makeText(this, "تم تنظيف الذاكرة المؤقتة وتسريع التطبيق بنجاح! 🚀", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "حدث خطأ أثناء تنظيف الذاكرة.", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── ✅ دالة فحص السرعة ───
    private fun openSpeedTest() {
        try {
            // نفتح موقع Fast.com الشهير لفحص السرعة مباشرة من التطبيق
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://fast.com/ar/")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "حدث خطأ في فتح متصفح الإنترنت.", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── ✅ دالة الدعم الفني (رقمك الحقيقي) ───
    private fun openLiveSupport() {
        val phoneNumber = "212772863204" // رقمك المغربي لطلب الاشتراكات
        val message = "مرحباً، أريد الاشتراك في تطبيق IPTV الخاص بك 📺"
        val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "تطبيق واتساب غير مثبت على جهازك!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
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
                val selectedLangCode = langCodes[which]
                sharedPref.edit().putString("APP_LANG", selectedLangCode).apply()
                dialog.dismiss()
                val intent = intent
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
        val langCode = sharedPref.getString("APP_LANG", "ar") ?: "ar" // جعلت العربية الافتراضية
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val isDark = sharedPref.getBoolean("IS_DARK_MODE", true)
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
