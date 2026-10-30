package com.iptv.player
import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject

data class WEntry(val k: String, val title: String, val img: String?, val url: String, val pos: Long, val dur: Long, val ts: Long, val sid: Int = 0, val sTitle: String = "", val sCover: String? = null, val label: String = "") {
    fun fin() = dur > 0 && pos >= dur * 95 / 100
    fun toJson(): JSONObject = JSONObject().put("k", k).put("t", title).put("i", img ?: "").put("u", url).put("p", pos).put("d", dur).put("s", ts).put("sid", sid).put("st", sTitle).put("sc", sCover ?: "").put("l", label)
}
private fun fromJson(o: JSONObject) = WEntry(o.optString("k"), o.optString("t"), o.optString("i").ifEmpty { null }, o.optString("u"), o.optLong("p"), o.optLong("d"), o.optLong("s"), o.optInt("sid"), o.optString("st"), o.optString("sc").ifEmpty { null }, o.optString("l"))
fun entryFromJson(s: String?): WEntry? { if (s == null) return null; return try { fromJson(JSONObject(s)) } catch (e: Exception) { null } }
private fun wsPrefs(c: Context) = c.getSharedPreferences("WATCH_PREFS", Context.MODE_PRIVATE)
private fun wsKey(c: Context): String { val p = ipPrefs(c); return "W_" + (p.getString("SELK", null) ?: ((p.getString("HOST", "") ?: "") + "|" + (p.getString("USER", "") ?: ""))).hashCode() }
fun wsAll(c: Context): List<WEntry> = try { val a = JSONArray(wsPrefs(c).getString(wsKey(c), "[]")); (0 until a.length()).map { fromJson(a.getJSONObject(it)) } } catch (e: Exception) { emptyList() }
private fun wsWrite(c: Context, l: List<WEntry>) { val a = JSONArray(); l.forEach { a.put(it.toJson()) }; wsPrefs(c).edit().putString(wsKey(c), a.toString()).apply() }
fun wsSave(c: Context, e: WEntry) = wsWrite(c, (listOf(e) + wsAll(c).filter { it.k != e.k }).take(60))
fun wsRemove(c: Context, k: String) = wsWrite(c, wsAll(c).filter { it.k != k })
fun wsSeries(c: Context, sid: Int): WEntry? = if (sid <= 0) null else wsAll(c).firstOrNull { it.sid == sid }
fun wsContinue(c: Context): List<WEntry> = wsAll(c).distinctBy { if (it.sid > 0) "s" + it.sid else it.k }.filter { !it.fin() && it.pos > 10_000 }
fun watchIntent(a: Context, e: WEntry, start: Long): Intent = Intent(a, PlayerActivity::class.java)
    .putExtra(PlayerActivity.EXTRA_STREAM_TITLE, if (e.sTitle.isNotEmpty()) e.sTitle + " - " + e.label else e.title).putExtra(PlayerActivity.EXTRA_STREAM_URL, e.url)
    .putExtra("W_JSON", e.toJson().toString()).putExtra("START_POS", start)
