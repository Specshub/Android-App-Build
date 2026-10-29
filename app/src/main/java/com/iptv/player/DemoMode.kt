package com.iptv.player
import android.content.Context
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.api.ActivationResult

fun addDemoButton(a: AppCompatActivity, root: ViewGroup) {
    if (!Constants.DEMO_MODE) return
    val btn = Button(a).apply { text = a.getString(R.string.demo_btn); setTextColor(0xFFFFFFFF.toInt()); backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF444444.toInt()) }
    btn.setOnClickListener { showDemoDialog(a) }
    root.addView(btn)
    (btn.layoutParams as ViewGroup.MarginLayoutParams).topMargin = (8 * a.resources.displayMetrics.density).toInt()
}
private fun field(a: AppCompatActivity, hint: String, value: String?) = EditText(a).apply { this.hint = hint; setText(value ?: ""); setSingleLine() }
fun showDemoDialog(a: AppCompatActivity) {
    val p = a.getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
    val host = field(a, a.getString(R.string.demo_host), p.getString("HOST", null))
    val user = field(a, a.getString(R.string.demo_user), p.getString("USER", null))
    val pass = field(a, a.getString(R.string.demo_pass), p.getString("PASS", null))
    val box = LinearLayout(a).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 16, 48, 0); addView(host); addView(user); addView(pass) }
    AlertDialog.Builder(a).setTitle(a.getString(R.string.demo_title)).setView(box).setNegativeButton(a.getString(R.string.cancel), null).setPositiveButton(a.getString(R.string.demo_ok)) { _, _ ->
        var h = host.text.toString().trim(); val u = user.text.toString().trim(); val w = pass.text.toString().trim()
        if (h.isEmpty() || u.isEmpty() || w.isEmpty()) { Toast.makeText(a, a.getString(R.string.demo_fill), Toast.LENGTH_SHORT).show(); return@setPositiveButton }
        if (!h.startsWith("http")) h = "http://$h"
        saveAndOpenHome(a, ActivationResult(true, null, h, u, w))
    }.show()
}
