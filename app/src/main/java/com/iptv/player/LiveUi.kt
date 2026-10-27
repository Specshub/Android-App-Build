package com.iptv.player
import android.view.KeyEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager

fun showOverlay(a: LiveActivity) {
    a.b.overlay.visibility = View.VISIBLE
    val i = a.shown.indexOfFirst { it.streamId == a.current?.streamId }
    a.b.rvChannels.post {
        if (i >= 0) (a.b.rvChannels.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(i, 200)
        a.b.rvChannels.postDelayed({ (if (i >= 0) a.b.rvChannels.findViewHolderForAdapterPosition(i)?.itemView else null)?.requestFocus() ?: a.b.rvChannels.requestFocus() }, 150)
    }
}
fun hideOverlay(a: LiveActivity) { if (!a.portrait) a.b.overlay.visibility = View.GONE }
fun setupTouch(a: LiveActivity) {
    a.b.touchLayer.setOnClickListener { if (a.portrait) return@setOnClickListener; if (a.b.overlay.visibility == View.VISIBLE) hideOverlay(a) else showOverlay(a) }
    a.onBackPressedDispatcher.addCallback(a, object : OnBackPressedCallback(true) { override fun handleOnBackPressed() { if (!a.portrait && a.b.overlay.visibility == View.VISIBLE) hideOverlay(a) else a.finish() } })
}
@Suppress("DEPRECATION")
fun hideSystemBars(a: LiveActivity) { if (a.portrait) return; a.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY }
