package com.iptv.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.databinding.FragmentLiveTvBinding 

class FavoritesFragment : Fragment() {
    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!
    private lateinit var contentAdapter: ContentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // نستخدم نفس تصميم شاشة المباشر لأنه جاهز وأنيق
        _binding = FragmentLiveTvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)

        // 1. الضغطة العادية لتشغيل القناة
        contentAdapter = ContentAdapter { clickedItem ->
            if (clickedItem is ContentItem.Live) {
                playChannel(clickedItem)
            }
        }

        // 2. الضغطة المطولة هنا تقوم بـ "الحذف" من المفضلة
        contentAdapter.onItemLongClick = { item ->
            if (item is ContentItem.Live) {
                FavoritesManager.removeFavorite(requireContext(), item.stream)
                loadFavorites() // تحديث الشاشة فوراً لإخفاء القناة المحذوفة
                Toast.makeText(context, "🗑️ تم الحذف من المفضلة", Toast.LENGTH_SHORT).show()
                true
            } else false
        }

        binding.recyclerView.adapter = contentAdapter
        loadFavorites()
    }

    private fun loadFavorites() {
        val favs = FavoritesManager.getFavorites(requireContext())
        val items = favs.map { ContentItem.Live(it) }
        contentAdapter.submitList(items)
        
        if (favs.isEmpty()) {
            Toast.makeText(context, "المفضلة فارغة حالياً 🌟", Toast.LENGTH_LONG).show()
        }
    }

    private fun playChannel(item: ContentItem.Live) {
        val sharedPref = requireContext().getSharedPreferences("IPTV_PREFS", android.content.Context.MODE_PRIVATE)
        val host = sharedPref.getString("HOST", "") ?: ""
        val user = sharedPref.getString("USER", "") ?: ""
        val pass = sharedPref.getString("PASS", "") ?: ""

        val baseUrl = if (host.startsWith("http")) host else "http://$host"
        val streamUrl = "$baseUrl/$user/$pass/${item.stream.streamId}"
        
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_STREAM_TITLE, item.stream.name)
            putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
