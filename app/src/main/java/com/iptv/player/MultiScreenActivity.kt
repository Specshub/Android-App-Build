package com.iptv.player

import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.gridlayout.widget.GridLayout
import com.iptv.player.databinding.ActivityMultiScreenBinding

class MultiScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // تأكد من استخدام Binding الصحيح
    private val players = mutableListOf<ExoPlayer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn2Screens.setOnClickListener { setupScreens(2) }
        binding.btn3Screens.setOnClickListener { setupScreens(3) }
        binding.btn4Screens.setOnClickListener { setupScreens(4) }

        setupScreens(2) // البدء بشاشتين افتراضياً
    }

    private fun setupScreens(count: Int) {
        // تنظيف المشغلات القديمة لتوفير الذاكرة (Memory Management)
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

        // 1. إنشاء PlayerView (الشاشة التي يعرض فيها الفيديو)
        val playerView = PlayerView(this).apply {
            useController = false // إخفاء أزرار التحكم التقليدية لشاشة أنظف
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // 2. إنشاء الـ ExoPlayer وربطه بالشاشة
        val player = ExoPlayer.Builder(this).build()
        playerView.player = player
        players.add(player)

        // 3. إضافة زر الإغلاق (إلغاء القناة)
        val closeBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundResource(android.R.drawable.screen_background_dark_transparent)
            layoutParams = FrameLayout.LayoutParams(70, 70).apply {
                gravity = Gravity.TOP or Gravity.END
                setMargins(10, 10, 10, 10)
            }
            setOnClickListener { 
                player.stop()
                container.visibility = View.GONE 
            }
        }

        container.addView(playerView)
        container.addView(closeBtn)

        // تجربة تشغيل رابط بث تلقائي (كمثال فقط)
        // في التطبيق الحقيقي، هنا ستفتح Dialog لاختيار قناة المستخدم
        val testUrl = "http://YOUR_SERVER_URL/live/user/pass/123.m3u8"
        playVideo(player, testUrl)

        return container
    }

    private fun playVideo(player: ExoPlayer, url: String) {
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        player.volume = 0f // كتم الصوت افتراضياً لكي لا تتداخل الأصوات
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
