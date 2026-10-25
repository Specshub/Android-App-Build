package com.iptv.player
import android.view.MenuItem
import androidx.core.view.GravityCompat

fun handleNavItem(a: MainActivity, item: MenuItem): Boolean {
    when (item.itemId) {
        R.id.nav_live_tv, R.id.nav_movies, R.id.nav_series -> navigateToSection(a, item.itemId)
        R.id.nav_favorites -> loadFragment(a, FavoritesFragment(), "المفضلة")
        R.id.nav_speed_test -> openUrl(a, "https://fast.com/ar/")
        R.id.nav_support -> openUrl(a, Constants.SUPPORT_WHATSAPP)
        R.id.nav_language -> showLanguageDialog(a)
        R.id.nav_theme -> toggleTheme(a)
        R.id.nav_logout -> performLogout(a)
    }
    a.binding.drawerLayout.closeDrawer(GravityCompat.START)
    return true
}
fun handleBack(a: MainActivity) {
    val d = a.binding.drawerLayout
    if (d.isDrawerOpen(GravityCompat.START)) { d.closeDrawer(GravityCompat.START); return }
    val f = a.supportFragmentManager.findFragmentById(R.id.fragment_container)
    if (f is LiveTvFragment && f.isShowingStreams) f.goBackToCategories()
    else if (f is MoviesFragment && f.isShowingStreams) f.goBackToCategories()
    else if (f is SeriesFragment && f.isShowingStreams) f.goBackToCategories()
    else a.finish()
}
