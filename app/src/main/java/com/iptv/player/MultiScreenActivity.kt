package com.iptv.player

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import com.iptv.player.databinding.ActivityMultiScreenBinding

class MultiScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultiScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // إعداد أزرار الاختيار
        binding.btn2Screens.setOnClickListener { setupScreens(2) }
        binding.btn3Screens.setOnClickListener { setupScreens(3) }
        binding.btn4Screens.setOnClickListener { setupScreens(4) }

        // البدء بـ 2 شاشة كوضع افتراضي
        setupScreens(2)
    }

    private fun setupScreens(count: Int) {
        binding.screensGrid.removeAllViews()
        
        // ضبط عدد الأعمدة بناءً على العدد
        binding.screensGrid.columnCount = if (count <= 2) 2 else 2
        binding.screensGrid.rowCount = if (count <= 2) 1 else 2

        for (i in 1..count) {
            val screenContainer = createPlayerContainer(i)
            binding.screensGrid.addView(screenContainer)
        }
    }

    private fun createPlayerContainer(index: Int): FrameLayout {
        val container = FrameLayout(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(2, 2, 2, 2)
            }
            setBackgroundColor(android.graphics.Color.DKGRAY)
        }

        // نص توضيحي (سيتم استبداله بـ ExoPlayer لاحقاً)
        val textView = TextView(this).apply {
            text = "شاشة $index\nانقر لإضافة قناة"
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
        }
        container.addView(textView)

        // زر الإغلاق (إلغاء القناة)
        val closeButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            layoutParams = FrameLayout.LayoutParams(80, 80).apply {
                gravity = Gravity.TOP or Gravity.END
            }
            setOnClickListener { 
                container.visibility = View.GONE // إخفاء الشاشة بسهولة
            }
        }
        container.addView(closeButton)

        // عند النقر على الحاوية، نفتح قائمة القنوات لاختيار بث
        container.setOnClickListener {
            Toast.makeText(this, "فتح قائمة القنوات للشاشة $index", Toast.LENGTH_SHORT).show()
            // هنا سنقوم لاحقاً بفتح Dialog لاختيار القناة
        }

        return container
    }
}
