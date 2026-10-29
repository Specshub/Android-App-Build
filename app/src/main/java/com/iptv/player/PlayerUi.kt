package com.iptv.player
import android.view.View
import android.widget.Toast
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

fun setupPlayerUi(a: PlayerActivity) {
    val modes = listOf(AspectRatioFrameLayout.RESIZE_MODE_FIT, AspectRatioFrameLayout.RESIZE_MODE_FILL, AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
    a.binding.playerView.setControllerShowTimeoutMs(3000); a.binding.playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { v -> a.binding.topBar.visibility = v })
    a.binding.btnBack.setOnClickListener { a.finish() }; a.binding.btnTracks.setOnClickListener { showTracks(a) }
    a.binding.btnAspectRatio.setOnClickListener {
        a.ratioIdx = (a.ratioIdx + 1) % modes.size; a.binding.playerView.resizeMode = modes[a.ratioIdx]
        Toast.makeText(a, a.getString(R.string.screen_mode, a.getString(listOf(R.string.sm_fit, R.string.sm_stretch, R.string.sm_zoom)[a.ratioIdx])), Toast.LENGTH_SHORT).show()
    }
}
fun showError(a: PlayerActivity, message: String) {
    val b = a.binding; b.progressBar.visibility = View.GONE; b.tvError.text = message; b.tvError.visibility = View.VISIBLE; b.btnRetry.visibility = View.VISIBLE
    b.btnRetry.setOnClickListener { b.tvError.visibility = View.GONE; b.btnRetry.visibility = View.GONE; b.progressBar.visibility = View.VISIBLE; releasePlayer(a); initializePlayer(a) }
}
