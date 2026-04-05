package com.iptv.player

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.gridlayout.widget.GridLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.iptv.player.databinding.ActivityMultiScreenBinding
import com.iptv.player.data.model.Resource

class MultiScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultiScreenBinding
    private val viewModel: MainViewModel by viewModels()
    private val players = mutableListOf<ExoPlayer>()

    // استخدام نفس الـ Adapter الموجود في مشروعك
    private lateinit var contentAdapter: ContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn2Screens.setOnClickListener { setupScreens(2) }
        binding.btn3Screens.setOnClickListener { setupScreens(3) }
        binding.btn4Screens.setOnClickListener { setupScreens(4) }

        setupScreens(2) 
    }

    private fun setupScreens(count: Int) {
        releasePlayers()
        binding.screensGrid.removeAllViews()
        
        binding.screensGrid.columnCount = 2
        binding.screensGrid.rowCount = if (count <= 2) 1 else 2

        for (i in 0 until count) {
            val playerContainer = createVideoPlayer(i)
            binding.screensGrid.addView(playerContainer)
        }
    }

    private fun createVideoPlayer(index: Int): FrameLayout {
        val container = FrameLayout(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(4, 4, 4, 4)
            }
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        val playerView = PlayerView(this).apply {
            useController = false
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val player = ExoPlayer.Builder(this).build()
        playerView.player = player
        players.add(player)

        val hintText = TextView(this).apply {
            text = "شاشة ${index + 1}\nانقر للاختيار"
            setTextColor(android.graphics.Color.GRAY)
            gravity = Gravity.CENTER
        }

        val closeBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundResource(android.R.drawable.screen_background_dark_transparent)
            layoutParams = FrameLayout.LayoutParams(80, 80).apply {
                gravity = Gravity.TOP or Gravity.END
                setMargins(8, 8, 8, 8)
            }
            setOnClickListener { 
                player.stop()
                container.visibility = View.GONE 
            }
        }

        val muteBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_lock_silent_mode)
            setBackgroundResource(android.R.drawable.screen_background_dark_transparent)
            layoutParams = FrameLayout.LayoutParams(80, 80).apply {
                gravity = Gravity.BOTTOM or Gravity.START
                setMargins(8, 8, 8, 8)
            }
            setOnClickListener {
                if (player.volume == 0f) {
                    players.forEach { it.volume = 0f } 
                    player.volume = 1f 
                    Toast.makeText(context, "صوت الشاشة ${index + 1} يعمل", Toast.LENGTH_SHORT).show()
                } else {
                    player.volume = 0f
                }
            }
        }

        playerView.setOnClickListener {
            showChannelPickerDialog(player, hintText)
        }

        container.addView(playerView)
        container.addView(hintText)
        container.addView(closeBtn)
        container.addView(muteBtn)

        return container
    }

    private fun showChannelPickerDialog(player: ExoPlayer, hint: TextView) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_channel_picker, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvChannelsPicker)

        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // ─── إعداد الـ ContentAdapter المتوافق مع مشروعك ───
        contentAdapter = ContentAdapter { clickedItem ->
            if (clickedItem is ContentItem.Live) {
                // استخدام دالة بناء الرابط الموجودة في MainViewModel
                val streamUrl = viewModel.buildStreamUrl(clickedItem.stream.streamId)
                playVideo(player, streamUrl)
                hint.visibility = View.GONE
                dialog.dismiss()
            }
        }

        recyclerView.adapter = contentAdapter
        
        // مراقبة القنوات المباشرة
        viewModel.liveStreams.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val items = resource.data.map { ContentItem.Live(it) }
                    contentAdapter.setAllItems(items)
                }
                is Resource.Error -> {
                    Toast.makeText(this, "خطأ: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        // طلب جلب كافة القنوات المباشرة
        viewModel.loadAllLiveStreams()

        dialog.setContentView(view)
        dialog.show()
    }

    private fun playVideo(player: ExoPlayer, url: String) {
        if (url.isEmpty()) return
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        player.volume = 0f 
    }

    private fun releasePlayers() {
        players.forEach { it.release() }
        players.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayers()
    }
}
