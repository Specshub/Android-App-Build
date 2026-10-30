package com.iptv.player
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.model.LoginCredentials

fun openMovie(a: AppCompatActivity, c: LoginCredentials, id: Int, title: String, img: String?, ext: String?) {
    a.startActivity(Intent(a, MovieDetailActivity::class.java).putExtra(EXTRA_HOST, c.host).putExtra(EXTRA_USERNAME, c.username).putExtra(EXTRA_PASSWORD, c.password)
        .putExtra("MV_ID", id).putExtra("MV_TITLE", title).putExtra("MV_IMG", img).putExtra("MV_EXT", ext ?: "mp4"))
}
