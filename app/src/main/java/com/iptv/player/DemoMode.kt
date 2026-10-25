package com.iptv.player
import android.content.Context
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.api.ActivationResult

fun addDemoButton(a: AppCompatActivity, root: ViewGroup) {
    if (!Constants.DEMO_MODE) return
    val btn = Button(a).apply { text = "دخول تجريبي (للاختبار)"; setTextColor(0xFFFFFFFF.toInt()); backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF444444.toInt()) }
    btn.setOnClickListener { showDemoDialog(a) }
    root.addView(btn)
    (btn.layoutParams as ViewGroup.MarginLayoutParams).topMargin = (8 * a.resources.displayMetrics.density).toInt()
}
private fun field(a: AppCompatActivity, hint: String, value: String?) = EditText(a).apply { this.hint = hint; setText(value ?: ""); setSingleLine() }
fun showDemoDialog(a: AppCompatActivity) {
    val p = a.getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
    val host = field(a, "رابط السيرفر مثل http://example.com:8080", p.getString("HOST", null))
    val user = field(a, "اسم المستخدم", p.getString("USER", null))
    val pass = field(a, "كلمة المرور", p.getString("PASS", null))
    val box = LinearLayout(a).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 16, 48, 0); addView(host); addView(user); addView(pass) }
    AlertDialog.Builder(a).setTitle("دخول تجريبي").setView(box).setNegativeButton("إلغاء", null).setPositiveButton("دخول") { _, _ ->
        var h = host.text.toString().trim(); val u = user.text.toString().trim(); val w = pass.text.toString().trim()
        if (h.isEmpty() || u.isEmpty() || w.isEmpty()) { Toast.makeText(a, "املأ كل الحقول", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
        if (!h.startsWith("http")) h = "http://$h"
        saveAndOpenHome(a, ActivationResult(true, null, h, u, w))
    }.show()
}
