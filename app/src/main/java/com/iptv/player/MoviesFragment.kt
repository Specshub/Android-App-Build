package com.iptv.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.databinding.FragmentMoviesBinding
import com.iptv.player.data.model.Resource
import com.iptv.player.FavoritesManager

class MoviesFragment : Fragment() {
    private var _binding: FragmentMoviesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var contentAdapter: ContentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)

        // 1. الضغطة العادية (تشغيل الفيلم مباشرة بدون نوافذ مزعجة)
        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    Toast.makeText(context, "جاري جلب الأفلام...", Toast.LENGTH_SHORT).show()
                    viewModel.loadVodStreams(clickedItem.id)
                }
                is ContentItem.Vod -> {
                    val host = activity?.intent?.getStringExtra(EXTRA_HOST) ?: ""
                    val username = activity?.intent?.getStringExtra(EXTRA_USERNAME) ?: ""
                    val password = activity?.intent?.getStringExtra(EXTRA_PASSWORD) ?: ""
                    
                    val cleanHost = if (host.endsWith("/")) host.dropLast(1) else host
                    val baseUrl = if (cleanHost.startsWith("http")) cleanHost else "http://$cleanHost"
                    val streamUrl = "$baseUrl/movie/$username/$password/${clickedItem.stream.streamId}.mp4"

                    // ─── 🚀 تشغيل الفيلم فوراً ───
                    val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                        putExtra(PlayerActivity.EXTRA_STREAM_TITLE, clickedItem.stream.name)
                        putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
                    }
                    startActivity(intent)
                }
                else -> {}
            }
        }

        // 2. الضغطة المطولة (حفظ أو إزالة الفيلم من المفضلة - ميزة ناجحة ومستقرة)
        contentAdapter.onItemLongClick = { item ->
            if (item is ContentItem.Vod) {
                val isAlreadyFav = FavoritesManager.getVodFavorites(requireContext()).any { it.streamId == item.stream.streamId }
                if (isAlreadyFav) {
                    FavoritesManager.removeVodFavorite(requireContext(), item.stream)
                    Toast.makeText(context, "🗑️ تم إزالة ${item.stream.name}", Toast.LENGTH_SHORT).show()
                } else {
                    FavoritesManager.addVodFavorite(requireContext(), item.stream)
                    Toast.makeText(context, "🌟 تم إضافة ${item.stream.name} للمفضلة", Toast.LENGTH_SHORT).show()
                }
                true
            } else false
        }

        binding.recyclerView.adapter = contentAdapter

        viewModel.vodCategories.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) contentAdapter.submitList(resource.data.map { ContentItem.Category(it.categoryId, it.categoryName) })
        }
        viewModel.vodStreams.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) contentAdapter.submitList(resource.data.map { ContentItem.Vod(it) })
            else if (resource is Resource.Error) Toast.makeText(context, "خطأ: ${resource.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
