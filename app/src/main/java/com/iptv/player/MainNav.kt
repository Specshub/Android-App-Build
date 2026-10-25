package com.iptv.player
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment

fun loadFragment(a: MainActivity, f: Fragment, title: String) {
    a.binding.dashboardView.visibility = android.view.View.GONE; a.binding.fragmentContainer.visibility = android.view.View.VISIBLE
    a.supportFragmentManager.beginTransaction().replace(R.id.fragment_container, f).commit(); a.supportActionBar?.title = title
}
fun navigateToSection(a: MainActivity, id: Int) {
    a.binding.navView.setCheckedItem(id)
    when (id) {
        R.id.nav_live_tv -> { a.viewModel.loadLiveCategories(); loadFragment(a, LiveTvFragment(), "Live TV") }
        R.id.nav_movies -> { a.viewModel.loadVodCategories(); loadFragment(a, MoviesFragment(), "Movies") }
        R.id.nav_series -> { a.viewModel.loadSeriesCategories(); loadFragment(a, SeriesFragment(), "Series") }
    }
}
fun openSectionByName(a: MainActivity, s: String) {
    val map = mapOf("live" to R.id.nav_live_tv, "movies" to R.id.nav_movies, "series" to R.id.nav_series)
    if (s == "favorites") loadFragment(a, FavoritesFragment(), "المفضلة")
    else if (s == "settings") a.binding.drawerLayout.openDrawer(GravityCompat.START)
    else map[s]?.let { navigateToSection(a, it) }
}
