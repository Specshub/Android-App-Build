package com.iptv.player

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.databinding.ActivityChannelListBinding

class ChannelListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelListBinding
    private lateinit var adapter: ContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. إعداد شريط الأدوات (Toolbar)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 2. استقبال البيانات من القسم المختار
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Channels"
        supportActionBar?.title = categoryName

        // 3. إعداد القائمة (RecyclerView)
        setupRecyclerView()

        // 4. محاكاة تحميل البيانات (سنربطها بالـ API لاحقاً)
        binding.progressBar.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        // نستخدم نفس المحول (Adapter) الذي أصلحناه سابقاً
        adapter = ContentAdapter { item ->
            // عند الضغط على قناة، ننتقل للمشغل (PlayerActivity)
            // ستحتاج لإضافة منطق الانتقال هنا لاحقاً
        }
        
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@ChannelListActivity, 3)
            adapter = this@ChannelListActivity.adapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // العودة للخلف عند ضغط سهم الرجوع
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
