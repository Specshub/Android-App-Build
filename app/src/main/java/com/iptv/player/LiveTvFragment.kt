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
import com.iptv.player.databinding.FragmentLiveTvBinding
import com.iptv.player.data.model.Resource
import com.iptv.player.FavoritesManager

class LiveTvFragment : Fragment() {
    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var contentAdapter: ContentAdapter

    // 🚩 متغير لمتابعة الحالة: هل نحن نعرض قنوات أم باقات؟
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

        // ─── 1. الضغطة العادية (الدخول للباقة أو تشغيل القناة) ───
        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    isShowingStreams = true // ⬅️ تحديث الحالة: دخلنا إلى باقة
                    Toast.makeText(context, "جاري جلب القنوات...", Toast.LENGTH_SHORT).show()
                    viewModel.loadLiveStreams(clickedItem.id)
                }
                is ContentItem.Live -> {
                    // ─── ✅ هنا نصنع سحر التشغيل ───
                    val host = activity?.intent?.getStringExtra(EXTRA_HOST) ?: ""
                    val username = activity?.intent?.getStringExtra(EXTRA_USERNAME) ?: ""
                    val password = activity?.intent?.getStringExtra(EXTRA_PASSWORD) ?: ""

                    // تركيب الرابط الخاص بسيرفرات Xtream
                    val baseUrl = if (host.startsWith("http")) host else "http://$host"
                    val streamUrl = "$baseUrl/$username/$password/${clickedItem.stream.streamId}"

                    // إرسال الرابط لشاشة المشغل
                    val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                        putExtra(PlayerActivity.EXTRA_STREAM_TITLE, clickedItem.stream.name)
                        putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
                    }
                    startActivity(intent)
                }
                else -> {}
            }
        }

        // ─── 2. ✅ الضغطة المطولة (إضافة أو إزالة من المفضلة بذكاء) ───
        contentAdapter.onItemLongClick = { item ->
            if (item is ContentItem.Live) {
                val currentFavs = FavoritesManager.getFavorites(requireContext())
                val isAlreadyFav = currentFavs.any { it.streamId == item.stream.streamId }

                if (isAlreadyFav) {
                    FavoritesManager.removeFavorite(requireContext(), item.stream)
                    Toast.makeText(context, "🗑️ تم إزالة ${item.stream.name} من المفضلة", Toast.LENGTH_SHORT).show()
                } else {
                    FavoritesManager.addFavorite(requireContext(), item.stream)
                    Toast.makeText(context, "🌟 تم إضافة ${item.stream.name} للمفضلة", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        binding.recyclerView.adapter = contentAdapter

        viewModel.liveCategories.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                isShowingStreams = false // ⬅️ تحديث الحالة: عدنا إلى الباقات
                val categories = resource.data
                val items = categories.map { ContentItem.Category(it.categoryId, it.categoryName) }
                contentAdapter.submitList(items)
            }
        }

        viewModel.liveStreams.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val streams = resource.data
                    val items = streams.map { ContentItem.Live(it) }
                    contentAdapter.submitList(items)
                }
                is Resource.Error -> {
                    Toast.makeText(context, "خطأ: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 🔙 دالة العودة للباقات (تستدعى من MainActivity عند ضغط زر الرجوع)
    fun goBackToCategories() {
        viewModel.loadLiveCategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
