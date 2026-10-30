package com.iptv.player
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.iptv.player.data.api.Pl
import com.iptv.player.data.api.plsToJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

private val subHttp = OkHttpClient.Builder().connectTimeout(12, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS).build()
private fun normHost(s: String): String { val t = s.trim(); val x = if (t.contains("://")) t else "http://$t"; return Regex("^(https?://[^/\\s]+)").find(x)?.groupValues?.get(1) ?: "" }
private fun subCheck(h: String, u: String, p: String): Boolean {
    val url = h + "/player_api.php?username=" + URLEncoder.encode(u, "UTF-8") + "&password=" + URLEncoder.encode(p, "UTF-8")
    subHttp.newCall(Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()).execute().use { r -> return JSONObject(r.body?.string() ?: "{}").optJSONObject("user_info")?.optString("auth") == "1" }
}
fun showAddSubDialog(a: AppCompatActivity) {
    val d = Dialog(a); d.requestWindowFeature(Window.FEATURE_NO_TITLE); d.setContentView(R.layout.dialog_addsub)
    d.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)); d.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    val m = a.resources.displayMetrics; d.window?.setLayout(minOf((m.widthPixels * 0.85f).toInt(), (460 * m.density).toInt()), ViewGroup.LayoutParams.WRAP_CONTENT)
    val url = d.findViewById<EditText>(R.id.etUrl); val usr = d.findViewById<EditText>(R.id.etUser); val pwd = d.findViewById<EditText>(R.id.etPass)
    val st = d.findViewById<TextView>(R.id.tvSubStatus); val go = d.findViewById<View>(R.id.btnSubGo)
    d.findViewById<View>(R.id.btnSubCancel).setOnClickListener { d.dismiss() }
    go.setOnClickListener {
        val h = normHost(url.text.toString()); val u = usr.text.toString().trim(); val p = pwd.text.toString().trim()
        if (h.isEmpty() || u.isEmpty() || p.isEmpty()) { st.text = a.getString(R.string.sub_fail); return@setOnClickListener }
        st.text = a.getString(R.string.sub_checking); go.isEnabled = false
        a.lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) { runCatching { subCheck(h, u, p) }.getOrDefault(false) }
            if (!ok) { st.text = a.getString(R.string.sub_fail); go.isEnabled = true; return@launch }
            val l = Pl("", h, u, p)
            ipPrefs(a).edit().putString("HOST", h).putString("USER", u).putString("PASS", p).putString("SELK", l.key()).putString("PLS", plsToJson(listOf(l))).putString("STATE", "manual").putBoolean("SRV", false).putBoolean("MANUAL", true).remove("EXPIRES").putLong("LAST_OK", System.currentTimeMillis()).apply()
            d.dismiss(); a.startActivity(Intent(a, HomeActivity::class.java).putExtra(EXTRA_HOST, h).putExtra(EXTRA_USERNAME, u).putExtra(EXTRA_PASSWORD, p).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }
    d.show()
}
