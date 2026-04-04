package com.iptvplayer.app.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.iptvplayer.app.R
import com.iptvplayer.app.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. إعداد شريط الأدوات (Toolbar)
        setSupportActionBar(binding.toolbar)

        // 2. إعداد القائمة الجانبية (DrawerLayout) وزر التبديل (Toggle)
        val toggle = ActionBarDrawerToggle(
            this, 
            binding.drawerLayout, 
            binding.toolbar,
            R.string.navigation_drawer_open, 
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 3. ربط مستمع النقرات بالقائمة الجانبية
        binding.navView.setNavigationItemSelectedListener(this)

        // 4. تحميل القسم الافتراضي (Live TV) عند فتح التطبيق لأول مرة
        if (savedInstanceState == null) {
            loadFragment(LiveTvFragment())
            binding.navView.setCheckedItem(R.id.nav_live_tv)
            supportActionBar?.title = "Live TV"
        }
    }

    // 5. التعامل مع اختيار الأقسام من القائمة الجانبية
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        var title = ""

        when (item.itemId) {
            R.id.nav_live_tv -> {
                fragment = LiveTvFragment()
                title = "Live TV"
            }
            R.id.nav_movies -> {
                fragment = MoviesFragment() // تم التفعيل بنجاح ✅
                title = "Movies"
            }
            R.id.nav_series -> {
                fragment = SeriesFragment() // تم التفعيل بنجاح ✅
                title = "Series"
            }
            R.id.nav_settings -> {
                // هنا يمكن إضافة شاشة الإعدادات لاحقاً
                title = "Settings"
            }
        }

        if (fragment != null) {
            loadFragment(fragment)
            supportActionBar?.title = title
        }

        // إغلاق القائمة بعد الاختيار
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // دالة مساعدة لتبديل الفراغ (Fragment Container) بالقسم المطلوب
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // التعامل مع زر العودة لإغلاق القائمة الجانبية إذا كانت مفتوحة
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
