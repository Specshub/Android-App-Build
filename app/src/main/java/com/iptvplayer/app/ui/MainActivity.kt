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

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)

        // Setup DrawerLayout and NavigationView
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // Load default fragment (Live TV)
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
            }
            R.id.nav_movies -> {
                // fragment = MoviesFragment()
                // title = "Movies"
            }
            R.id.nav_series -> {
                // fragment = SeriesFragment()
                // title = "Series"
            }
            R.id.nav_settings -> {
                // Start SettingsActivity or load fragment
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
