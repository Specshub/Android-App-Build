package com.iptv.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView // ✅ استيراد ضروري
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.databinding.FragmentLiveTvBinding
import com.iptv.player.data.model.Resource
import com.iptv.player.FavoritesManager

class LiveTvFragment : Fragment() {
    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var contentAdapter: ContentAdapter

    var isShowingStreams = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveTvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)

        // ─── إعداد المحول (Adapter) ───
        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    isShowingStreams = true
                    Toast.makeText(context, "جاري جلب القنوات...", Toast.LENGTH_SHORT).show()
                    viewModel.loadLiveStreams(clickedItem.id)
                }
                is ContentItem.Live -> {
                    val host = activity?.intent?.getStringExtra(EXTRA_HOST) ?: ""
                    val username = activity?.intent?.getStringExtra(EXTRA_USERNAME) ?: ""
                    val password = activity?.intent?.getStringExtra(EXTRA_PASSWORD) ?: ""

                    val baseUrl = if (host.startsWith("http")) host else "http://$host"
                    // أضفنا .ts لضمان تشغيل القناة كما في الخطوة السابقة
                    val streamUrl = "$baseUrl/$username/$password/${clickedItem.stream.streamId}.ts"

                    val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                        putExtra(PlayerActivity.EXTRA_STREAM_TITLE, clickedItem.stream.name)
                        putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
                    }
                    startActivity(intent)
                }
                else -> {}
            }
        }

        contentAdapter.onItemLongClick = { item ->
            if (item is ContentItem.Live) {
                val currentFavs = FavoritesManager.getFavorites(requireContext())
                val isAlreadyFav = currentFavs.any { it.streamId == item.stream.streamId }

                if (isAlreadyFav) {
                    FavoritesManager.removeFavorite(requireContext(), item.stream)
                    Toast.makeText(context, "🗑️ تم إزالة ${item.stream.name}", Toast.LENGTH_SHORT).show()
                } else {
                    FavoritesManager.addFavorite(requireContext(), item.stream)
                    Toast.makeText(context, "🌟 تم إضافة ${item.stream.name}", Toast.LENGTH_SHORT).show()
                }
                true
            } else false
        }

        binding.recyclerView.adapter = contentAdapter

        // ─── 🔍 برمجة منطق البحث ───
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // استدعاء دالة الفلترة في الـ Adapter (تأكد من إضافتها في ContentAdapter كما شرحنا سابقاً)
                contentAdapter.filter(newText ?: "")
                return true
            }
        })

        viewModel.liveCategories.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                isShowingStreams = false
                val items = resource.data.map { ContentItem.Category(it.categoryId, it.categoryName) }
                // نستخدم setAllItems لدعم البحث (يجب أن تكون موجودة في الـ Adapter)
                contentAdapter.setAllItems(items) 
            }
        }

        viewModel.liveStreams.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val items = resource.data.map { ContentItem.Live(it) }
                    contentAdapter.setAllItems(items)
                }
                is Resource.Error -> {
                    Toast.makeText(context, "خطأ: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    fun goBackToCategories() {
        viewModel.loadLiveCategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
