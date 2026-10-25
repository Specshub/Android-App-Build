package com.iptv.player
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

fun showLanguageDialog(a: AppCompatActivity) {
    val names = arrayOf("العربية", "English", "Français"); val codes = arrayOf("ar", "en", "fr")
    val p = a.getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
    val checked = codes.indexOf(p.getString("APP_LANG", "ar")).coerceAtLeast(0)
    AlertDialog.Builder(a).setTitle("اختر لغة التطبيق").setSingleChoiceItems(names, checked) { d, which ->
        p.edit().putString("APP_LANG", codes[which]).apply(); d.dismiss()
        a.startActivity(Intent(a, ActivationActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }.show()
}
