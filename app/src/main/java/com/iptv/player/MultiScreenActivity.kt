package com.iptv.player

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.iptv.player.data.model.LoginCredentials
import com.iptv.player.data.model.Resource
import com.iptv.player.data.model.LiveStream

class MultiScreenActivity : AppCompatActivity() {

    private var player1: ExoPlayer? = null
    private var player2: ExoPlayer? = null
    private var player3: ExoPlayer? = null
    private var player4: ExoPlayer? = null

    private val viewModel: MainViewModel by viewModels()

    private var host = ""
    private var username = ""
    private var password = ""
    private var selectedScreenIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_screen)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        host = sharedPref.getString("HOST", "") ?: ""
        username = sharedPref.getString("USER", "") ?: ""
        password = sharedPref.getString("PASS", "") ?: ""

        if (host.isNotEmpty() && username.isNotEmpty()) {
            viewModel.setCredentials(LoginCredentials(host, username, password))
            viewModel.loadLiveCategories()
        }

        findViewById<ImageView>(R.id.btnAdd1).setOnClickListener { openCategoryDialog(1) }
        findViewById<ImageView>(R.id.btnAdd2).setOnClickListener { openCategoryDialog(2) }
        findViewById<ImageView>(R.id.btnAdd3).setOnClickListener { openCategoryDialog(3) }
        findViewById<ImageView>(R.id.btnAdd4).setOnClickListener { openCategoryDialog(4) }

        viewModel.liveStreams.observe(this) { resource ->
            if (resource is Resource.Success && selectedScreenIndex != -1) {
                showChannelsDialog(resource.data)
            } else if (resource is Resource.Error) {
                Toast.makeText(this, "خطأ في جلب القنوات", Toast.LENGTH_SHORT).show()
                selectedScreenIndex = -1
            }
        }

        // ─── 🚀 هنا نسأل المستخدم عن عدد الشاشات فور فتح النافذة ───
        askUserForScreenCount()
    }

    private fun askUserForScreenCount() {
        val options = arrayOf("شاشتين (2)", "ثلاث شاشات (3)", "أربع شاشات (4)")

        AlertDialog.Builder(this)
            .setTitle("كم عدد الشاشات التي تريد مشاهدتها؟")
            .setCancelable(false) // لمنع إغلاق النافذة بدون اختيار
            .setItems(options) { _, which ->
                setupGridLayout(which + 2) // which=0 يعني شاشتين، which=1 يعني 3 وهكذا
            }
            .show()
    }

    private fun setupGridLayout(count: Int) {
        val row2 = findViewById<LinearLayout>(R.id.row2)
        val frame3 = findViewById<FrameLayout>(R.id.frame3)
        val frame4 = findViewById<FrameLayout>(R.id.frame4)

        when (count) {
            2 -> {
                // شاشتين: نخفي الصف الثاني بالكامل، فيتمدد الصف الأول ليأخذ الشاشة كاملة
                row2.visibility = View.GONE
            }
            3 -> {
                // 3 شاشات: نظهر الصف الثاني، لكن نخفي المربع الرابع، فيتمدد المربع الثالث
                row2.visibility = View.VISIBLE
                frame3.visibility = View.VISIBLE
                frame4.visibility = View.GONE
            }
            4 -> {
                // 4 شاشات: كل شيء يظهر كالعادة
                row2.visibility = View.VISIBLE
                frame3.visibility = View.VISIBLE
                frame4.visibility = View.VISIBLE
            }
        }
    }

    private fun openCategoryDialog(screenIndex: Int) {
        selectedScreenIndex = screenIndex
        
        val categoriesResource = viewModel.liveCategories.value
        if (categoriesResource is Resource.Success) {
            val categories = categoriesResource.data
            val categoryNames = categories.map { it.categoryName }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("اختر الباقة للشاشة رقم $screenIndex")
                .setItems(categoryNames) { _, which ->
                    val selectedCategory = categories[which]
                    Toast.makeText(this, "جاري جلب القنوات...", Toast.LENGTH_SHORT).show()
                    viewModel.loadLiveStreams(selectedCategory.categoryId)
                }
                .show()
        } else {
            Toast.makeText(this, "جاري تحميل الباقات، يرجى الانتظار...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChannelsDialog(channels: List<LiveStream>) {
        val displayChannels = channels.take(150)
        val channelNames = displayChannels.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("اختر القناة لتشغيلها")
            .setItems(channelNames) { _, which ->
                val selectedChannel = displayChannels[which]
                playChannelOnScreen(selectedScreenIndex, selectedChannel)
                selectedScreenIndex = -1
            }
            .setOnCancelListener {
                selectedScreenIndex = -1
            }
            .show()
    }

    private fun playChannelOnScreen(screenIndex: Int, channel: LiveStream) {
        val baseUrl = if (host.startsWith("http")) host else "http://$host"
        val streamUrl = "$baseUrl/$username/$password/${channel.streamId}"

        val playerView = when (screenIndex) {
            1 -> findViewById<PlayerView>(R.id.player1)
            2 -> findViewById<PlayerView>(R.id.player2)
            3 -> findViewById<PlayerView>(R.id.player3)
            4 -> findViewById<PlayerView>(R.id.player4)
            else -> return
        }

        val btnAdd = when (screenIndex) {
            1 -> findViewById<ImageView>(R.id.btnAdd1)
            2 -> findViewById<ImageView>(R.id.btnAdd2)
            3 -> findViewById<ImageView>(R.id.btnAdd3)
            4 -> findViewById<ImageView>(R.id.btnAdd4)
            else -> return
        }

        btnAdd.visibility = View.GONE
        playerView.visibility = View.VISIBLE

        when (screenIndex) {
            1 -> player1?.release()
            2 -> player2?.release()
            3 -> player3?.release()
            4 -> player4?.release()
        }

        val exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
        exoPlayer.setMediaItem(MediaItem.fromUri(streamUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        when (screenIndex) {
            1 -> player1 = exoPlayer
            2 -> player2 = exoPlayer
            3 -> player3 = exoPlayer
            4 -> player4 = exoPlayer
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player1?.release()
        player2?.release()
        player3?.release()
        player4?.release()
    }
}
