package com.iptv.player

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer

class MultiScreenActivity : AppCompatActivity() {

    private var player1: ExoPlayer? = null
    private var player2: ExoPlayer? = null
    private var player3: ExoPlayer? = null
    private var player4: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_screen)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        findViewById<ImageView>(R.id.btnAdd1).setOnClickListener {
            Toast.makeText(this, "جاري برمجة نافذة القنوات للشاشة 1...", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageView>(R.id.btnAdd2).setOnClickListener {
            Toast.makeText(this, "جاري برمجة نافذة القنوات للشاشة 2...", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageView>(R.id.btnAdd3).setOnClickListener {
            Toast.makeText(this, "جاري برمجة نافذة القنوات للشاشة 3...", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageView>(R.id.btnAdd4).setOnClickListener {
            Toast.makeText(this, "جاري برمجة نافذة القنوات للشاشة 4...", Toast.LENGTH_SHORT).show()
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
