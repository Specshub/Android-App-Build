package com.iptv.player
import android.content.Context
import android.view.View
import com.iptv.player.data.local.DeviceManager

fun showPane(a: SettingsActivity, i: Int) {
    listOf(a.b.paneAccount, a.b.paneGeneral, a.b.panePlayer).forEachIndexed { k, v -> v.visibility = if (k == i) View.VISIBLE else View.GONE }
    listOf(a.b.tabAccount, a.b.tabGeneral, a.b.tabPlayer).forEachIndexed { k, t -> t.isSelected = k == i }
}
fun setupSettings(a: SettingsActivity) {
    val p = a.getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
    a.b.vExpiry.text = p.getString("EXPIRES", null)?.takeIf { it.isNotEmpty() } ?: "—"
    a.b.vKey.text = DeviceManager.getKey(a); a.b.vMac.text = DeviceManager.getMac(a); a.b.vPhone.text = Constants.SUPPORT_PHONE
    a.b.vVersion.text = a.packageManager.getPackageInfo(a.packageName, 0).versionName ?: "1.0"
    a.b.tabAccount.setOnClickListener { showPane(a, 0) }; a.b.tabGeneral.setOnClickListener { showPane(a, 1) }; a.b.tabPlayer.setOnClickListener { showPane(a, 2) }
    a.b.btnLang.setOnClickListener { showLanguageDialog(a) }; a.b.btnTheme.setOnClickListener { toggleTheme(a) }
    a.b.btnSupport.setOnClickListener { openUrl(a, Constants.SUPPORT_WHATSAPP) }; a.b.btnLogout.setOnClickListener { performLogout(a) }
    a.b.swAuto.isChecked = p.getBoolean("AUTO_PLAY", true); a.b.swAuto.setOnCheckedChangeListener { _, on -> p.edit().putBoolean("AUTO_PLAY", on).apply() }
    showPane(a, 0)
}
