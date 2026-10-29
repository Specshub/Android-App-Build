package com.iptv.player
import android.content.Intent
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.model.LoginCredentials

fun openSection(a: AppCompatActivity, c: LoginCredentials, section: String) {
    val target = when (section) { "live" -> LiveActivity::class.java; "movies", "series", "kids" -> BrowseActivity::class.java; "settings" -> SettingsActivity::class.java; "search" -> SearchActivity::class.java; "favorites" -> FavoritesActivity::class.java; else -> MainActivity::class.java }
    a.startActivity(Intent(a, target).putExtra(EXTRA_HOST, c.host).putExtra(EXTRA_USERNAME, c.username).putExtra(EXTRA_PASSWORD, c.password).putExtra("EXTRA_SECTION", section))
}
fun setupHomeButtons(a: AppCompatActivity, box: LinearLayout, c: LoginCredentials) {
    val items = listOf(Triple(a.getString(R.string.nav_live), R.drawable.ic_nav_live, "live"), Triple(a.getString(R.string.nav_movies), R.drawable.ic_nav_movies, "movies"), Triple(a.getString(R.string.nav_series), R.drawable.ic_nav_series, "series"),
        Triple(a.getString(R.string.nav_search), R.drawable.ic_nav_search, "search"), Triple(a.getString(R.string.nav_fav), R.drawable.ic_nav_fav, "favorites"), Triple(a.getString(R.string.nav_settings), R.drawable.ic_nav_settings, "settings"))
    items.forEach { (label, icon, section) ->
        val v = LayoutInflater.from(a).inflate(R.layout.item_home_button, box, false)
        v.findViewById<TextView>(R.id.btnText).text = label
        v.findViewById<ImageView>(R.id.btnIcon).setImageResource(icon)
        v.setOnClickListener { openSection(a, c, section) }
        v.setOnFocusChangeListener { view, f -> val s = if (f) 1.12f else 1f; view.animate().scaleX(s).scaleY(s).setDuration(150).start() }
        box.addView(v)
    }
}
