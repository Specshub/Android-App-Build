package com.iptv.player
import android.content.Intent
import android.view.View
import android.widget.Toast

fun refreshFavs(a: FavoritesActivity) {
    a.list = FavoritesManager.getFavorites(a)
    a.adapter.submitList(a.list.map { Row(it.streamId.toString(), nm(it.name), it.streamIcon) })
    a.b.tvEmpty.visibility = if (a.list.isEmpty()) View.VISIBLE else View.GONE
}
fun playFav(a: FavoritesActivity, id: String) {
    val s = a.list.firstOrNull { it.streamId.toString() == id } ?: return; val c = a.creds
    a.startActivity(Intent(a, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_STREAM_TITLE, nm(s.name)).putExtra(PlayerActivity.EXTRA_STREAM_URL, "${c.host.trimEnd('/')}/live/${c.username}/${c.password}/${s.streamId}.ts"))
}
fun removeFav(a: FavoritesActivity, id: String): Boolean {
    val s = a.list.firstOrNull { it.streamId.toString() == id } ?: return false
    FavoritesManager.removeFavorite(a, s); Toast.makeText(a, "تمت الإزالة من المفضلة", Toast.LENGTH_SHORT).show(); refreshFavs(a); return true
}
