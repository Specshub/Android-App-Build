package com.iptv.player

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
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

    // المشغلات الأربعة
    private var player1: ExoPlayer? = null
    private var player2: ExoPlayer? = null
    private var player3: ExoPlayer? = null
    private var player4: ExoPlayer? = null

    // استدعاء المدير لنجلب منه القنوات
    private val viewModel: MainViewModel by viewModels()

    private var host = ""
    private var username = ""
    private var password = ""

    // متغير ذكي لمعرفة أي شاشة ضغط عليها المستخدم (1, 2, 3, أو 4)
    private var selectedScreenIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_screen)

        // الشاشة السينمائية الكاملة
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        // ─── 1. سحب بيانات الدخول من الذاكرة ───
        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        host = sharedPref.getString("HOST", "") ?: ""
        username = sharedPref.getString("USER", "") ?: ""
        password = sharedPref.getString("PASS", "") ?: ""

        if (host.isNotEmpty() && username.isNotEmpty()) {
            viewModel.setCredentials(LoginCredentials(host, username, password))
            // نأمر المدير بتحميل الباقات فور فتح الشاشة لتكون جاهزة
            viewModel.loadLiveCategories() 
        }

        // ─── 2. برمجة أزرار (+) ───
        findViewById<ImageView>(R.id.btnAdd1).setOnClickListener { openCategoryDialog(1) }
        findViewById<ImageView>(R.id.btnAdd2).setOnClickListener { openCategoryDialog(2) }
        findViewById<ImageView>(R.id.btnAdd3).setOnClickListener { openCategoryDialog(3) }
        findViewById<ImageView>(R.id.btnAdd4).setOnClickListener { openCategoryDialog(4) }

        // ─── 3. مراقبة القنوات عند اختيار باقة ───
        viewModel.liveStreams.observe(this) { resource ->
            // نتحقق أننا نحن من ضغطنا وأن البيانات نجحت
            if (resource is Resource.Success && selectedScreenIndex != -1) {
                showChannelsDialog(resource.data)
            } else if (resource is Resource.Error) {
                Toast.makeText(this, "خطأ في جلب القنوات", Toast.LENGTH_SHORT).show()
                selectedScreenIndex = -1
            }
        }
    }

    // ─── 4. نافذة اختيار الباقة ───
    private fun openCategoryDialog(screenIndex: Int) {
        selectedScreenIndex = screenIndex
        
        val categoriesResource = viewModel.liveCategories.value
        if (categoriesResource is Resource.Success) {
            val categories = categoriesResource.data
            // نستخرج أسماء الباقات لعرضها في القائمة
            val categoryNames = categories.map { it.categoryName }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("اختر الباقة للشاشة رقم $screenIndex")
                .setItems(categoryNames) { _, which ->
                    val selectedCategory = categories[which]
                    Toast.makeText(this, "جاري جلب القنوات...", Toast.LENGTH_SHORT).show()
                    // نطلب القنوات الخاصة بهذه الباقة
                    viewModel.loadLiveStreams(selectedCategory.categoryId)
                }
                .show()
        } else {
            Toast.makeText(this, "جاري تحميل الباقات، يرجى الانتظار ثواني...", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── 5. نافذة اختيار القناة ───
    private fun showChannelsDialog(channels: List<LiveStream>) {
        // حيلة ذكية: نعرض أول 150 قناة فقط لتجنب تشنج الهاتف إذا كانت الباقة ضخمة
        val displayChannels = channels.take(150)
        val channelNames = displayChannels.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("اختر القناة لتشغيلها")
            .setItems(channelNames) { _, which ->
                val selectedChannel = displayChannels[which]
                playChannelOnScreen(selectedScreenIndex, selectedChannel)
                selectedScreenIndex = -1 // تصفير الاختيار بعد التشغيل
            }
            .setOnCancelListener {
                selectedScreenIndex = -1 // تصفير الاختيار إذا ألغى المستخدم
            }
            .show()
    }

    // ─── 6. سحر التشغيل الفعلي في المربع ───
    private fun playChannelOnScreen(screenIndex: Int, channel: LiveStream) {
        val baseUrl = if (host.startsWith("http")) host else "http://$host"
        val streamUrl = "$baseUrl/$username/$password/${channel.streamId}"

        // نحدد أي شاشة وأي زر سنقوم بتعديله
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

        // إخفاء زر (+) وإظهار مشغل الفيديو
        btnAdd.visibility = View.GONE
        playerView.visibility = View.VISIBLE

        // إيقاف المشغل القديم إذا كان هناك قناة تعمل مسبقاً في هذا المربع
        when (screenIndex) {
            1 -> player1?.release()
            2 -> player2?.release()
            3 -> player3?.release()
            4 -> player4?.release()
        }

        // إنشاء مشغل جديد وبدء البث
        val exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
        exoPlayer.setMediaItem(MediaItem.fromUri(streamUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true // تشغيل تلقائي

        // حفظ المشغل في المتغير الخاص به لكي نتمكن من إيقافه عند الخروج
        when (screenIndex) {
            1 -> player1 = exoPlayer
            2 -> player2 = exoPlayer
            3 -> player3 = exoPlayer
            4 -> player4 = exoPlayer
        }
    }

    // تنظيف الذاكرة وإيقاف كل القنوات عند إغلاق شاشة العرض المتعدد
    override fun onDestroy() {
        super.onDestroy()
        player1?.release()
        player2?.release()
        player3?.release()
        player4?.release()
    }
}
