package com.iptv.player
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.api.Pl
import com.iptv.player.data.api.plsFromJson

private fun title(l: Pl) = l.name.ifBlank { l.host.substringAfter("://").substringBefore("/") }
fun showListsDialog(a: AppCompatActivity) {
    val p = ipPrefs(a); val ls = plsFromJson(p.getString("PLS", null))
    if (ls.isEmpty()) { Toast.makeText(a, a.getString(R.string.lists_none), Toast.LENGTH_SHORT).show(); return }
    val cur = ls.indexOfFirst { it.key() == p.getString("SELK", null) }.coerceAtLeast(0)
    val d = Dialog(a); d.requestWindowFeature(Window.FEATURE_NO_TITLE); d.setContentView(R.layout.dialog_lists)
    d.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val m = a.resources.displayMetrics; d.window?.setLayout(minOf((m.widthPixels * 0.8f).toInt(), (440 * m.density).toInt()), ViewGroup.LayoutParams.WRAP_CONTENT)
    val box = d.findViewById<LinearLayout>(R.id.listBox); var first: View? = null
    ls.forEachIndexed { i, l ->
        val v = LayoutInflater.from(a).inflate(R.layout.item_list_row, box, false)
        v.findViewById<TextView>(R.id.rowAvatar).text = title(l).take(1).uppercase()
        v.findViewById<TextView>(R.id.rowName).text = title(l)
        v.findViewById<TextView>(R.id.rowHost).text = l.host.substringAfter("://").substringBefore("/")
        v.findViewById<View>(R.id.rowCheck).visibility = if (i == cur) View.VISIBLE else View.GONE
        v.isSelected = i == cur
        v.setOnClickListener { d.dismiss(); if (i != cur) switchList(a, l) }
        box.addView(v); if (i == cur) first = v
    }
    d.findViewById<View>(R.id.btnClose).setOnClickListener { d.dismiss() }
    d.show(); first?.requestFocus()
}
private fun switchList(a: AppCompatActivity, l: Pl) {
    ipPrefs(a).edit().putString("HOST", l.host).putString("USER", l.username).putString("PASS", l.password).putString("SELK", l.key()).apply()
    a.startActivity(Intent(a, HomeActivity::class.java).putExtra(EXTRA_HOST, l.host).putExtra(EXTRA_USERNAME, l.username).putExtra(EXTRA_PASSWORD, l.password)); a.finish()
}
