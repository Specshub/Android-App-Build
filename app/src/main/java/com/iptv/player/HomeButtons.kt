package com.iptv.player
import android.content.Intent
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.model.LoginCredentials

fun openSection(a: AppCompatActivity, c: LoginCredentials, section: String) {
    a.startActivity(Intent(a, MainActivity::class.java).putExtra(EXTRA_HOST, c.host).putExtra(EXTRA_USERNAME, c.username).putExtra(EXTRA_PASSWORD, c.password).putExtra("EXTRA_SECTION", section))
}
fun setupHomeButtons(a: AppCompatActivity, box: LinearLayout, c: LoginCredentials) {
    val items = listOf(Triple("القنوات", android.R.drawable.ic_menu_slideshow, "live"), Triple("أفلام", android.R.drawable.ic_menu_gallery, "movies"),
        Triple("مسلسلات", android.R.drawable.ic_menu_recent_history, "series"), Triple("اطفال", android.R.drawable.ic_menu_myplaces, ""),
        Triple("الأكثر مشاهدة", android.R.drawable.ic_menu_sort_by_size, ""), Triple("جدول المباريات", android.R.drawable.ic_menu_today, "schedule"),
        Triple("المفضلة", android.R.drawable.btn_star_big_on, "favorites"), Triple("إعدادات", android.R.drawable.ic_menu_preferences, "settings"))
    items.forEach { (label, icon, section) ->
        val v = LayoutInflater.from(a).inflate(R.layout.item_home_button, box, false)
        v.findViewById<TextView>(R.id.btnText).text = label
        v.findViewById<ImageView>(R.id.btnIcon).apply { setImageResource(icon); setColorFilter(0xFFFFFFFF.toInt()) }
        v.setOnClickListener { if (section.isNotEmpty()) openSection(a, c, section) else Toast.makeText(a, "قريبًا", Toast.LENGTH_SHORT).show() }
        v.setOnFocusChangeListener { view, f -> val s = if (f) 1.12f else 1f; view.animate().scaleX(s).scaleY(s).setDuration(150).start() }
        box.addView(v)
    }
}
