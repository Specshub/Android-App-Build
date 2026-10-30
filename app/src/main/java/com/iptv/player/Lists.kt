package com.iptv.player
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.api.Pl
import com.iptv.player.data.api.plsFromJson

private fun title(l: Pl) = l.name.ifBlank { l.host.substringAfter("://").substringBefore("/") }
fun showListsDialog(a: AppCompatActivity) {
    val p = ipPrefs(a); val ls = plsFromJson(p.getString("PLS", null))
    if (ls.isEmpty()) { Toast.makeText(a, a.getString(R.string.lists_none), Toast.LENGTH_SHORT).show(); return }
    val cur = ls.indexOfFirst { it.key() == p.getString("SELK", null) }.coerceAtLeast(0)
    AlertDialog.Builder(a).setTitle(a.getString(R.string.nav_lists)).setSingleChoiceItems(ls.map { title(it) }.toTypedArray(), cur) { d, w -> d.dismiss(); if (w != cur) switchList(a, ls[w]) }
        .setNegativeButton(a.getString(R.string.cancel), null).show()
}
private fun switchList(a: AppCompatActivity, l: Pl) {
    ipPrefs(a).edit().putString("HOST", l.host).putString("USER", l.username).putString("PASS", l.password).putString("SELK", l.key()).apply()
    a.startActivity(Intent(a, HomeActivity::class.java).putExtra(EXTRA_HOST, l.host).putExtra(EXTRA_USERNAME, l.username).putExtra(EXTRA_PASSWORD, l.password)); a.finish()
}
