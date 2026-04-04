package com.iptv.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.databinding.FragmentSeriesBinding
import com.iptv.player.data.model.Resource
import com.iptv.player.data.model.Episode
import com.iptv.player.data.model.SeriesInfo
import com.iptv.player.FavoritesManager

class SeriesFragment : Fragment() {
    private var _binding: FragmentSeriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var contentAdapter: ContentAdapter
    private var currentSelectedSeriesId: Int? = null

    // 🚩 متغير لمتابعة الحالة: هل نحن نعرض مسلسلات أم باقات/أقسام؟
    var isShowingStreams = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSeriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)

        // 1. الضغطة العادية (جلب الحلقات)
        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    isShowingStreams = true // ⬅️ تحديث الحالة: دخلنا إلى قسم المسلسلات
                    Toast.makeText(context, "جاري جلب المسلسلات...", Toast.LENGTH_SHORT).show()
                    viewModel.loadSeries(clickedItem.id)
                }
                is ContentItem.SeriesItem -> {
                    Toast.makeText(context, "جاري جلب الحلقات...", Toast.LENGTH_SHORT).show()
                    currentSelectedSeriesId = clickedItem.series.seriesId
                    viewModel.loadSeriesInfo(clickedItem.series.seriesId)
                }
                else -> {}
            }
        }

        // 2. ✅ الضغطة المطولة (حفظ أو إزالة المسلسل)
        contentAdapter.onItemLongClick = { item ->
            if (item is ContentItem.SeriesItem) {
                val isAlreadyFav = FavoritesManager.getSeriesFavorites(requireContext()).any { it.seriesId == item.series.seriesId }
                if (isAlreadyFav) {
                    FavoritesManager.removeSeriesFavorite(requireContext(), item.series)
                    Toast.makeText(context, "🗑️ تم إزالة ${item.series.name}", Toast.LENGTH_SHORT).show()
                } else {
                    FavoritesManager.addSeriesFavorite(requireContext(), item.series)
                    Toast.makeText(context, "🌟 تم إضافة ${item.series.name} للمفضلة", Toast.LENGTH_SHORT).show()
                }
                true
            } else false
        }

        binding.recyclerView.adapter = contentAdapter

        viewModel.seriesCategories.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                isShowingStreams = false // ⬅️ تحديث الحالة: عدنا إلى أقسام المسلسلات
                contentAdapter.submitList(resource.data.map { ContentItem.Category(it.categoryId, it.categoryName) })
            }
        }
        
        viewModel.seriesList.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) contentAdapter.submitList(resource.data.map { ContentItem.SeriesItem(it) })
        }
        
        viewModel.seriesInfo.observe(viewLifecycleOwner) { resource ->
            if (currentSelectedSeriesId != null && resource is Resource.Success) {
                showEpisodesDialog(resource.data)
                currentSelectedSeriesId = null
            } else if (currentSelectedSeriesId != null && resource is Resource.Error) {
                Toast.makeText(context, "خطأ في جلب الحلقات", Toast.LENGTH_SHORT).show()
                currentSelectedSeriesId = null
            }
        }
    }

    // 🔙 دالة العودة للأقسام (تستدعى من MainActivity عند ضغط زر الرجوع)
    fun goBackToCategories() {
        viewModel.loadSeriesCategories()
    }

    private fun showEpisodesDialog(seriesInfo: SeriesInfo) {
        val allEpisodes = mutableListOf<Episode>()
        seriesInfo.episodes.toSortedMap().forEach { (_, episodes) -> allEpisodes.addAll(episodes) }
        if (allEpisodes.isEmpty()) return
        val episodeTitles = allEpisodes.map { "الموسم ${it.season} | الحلقة ${it.id} - ${it.title}" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle(seriesInfo.info.name ?: "اختر الحلقة")
            .setItems(episodeTitles) { _, which -> playEpisode(allEpisodes[which]) }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun playEpisode(episode: Episode) {
        val host = activity?.intent?.getStringExtra(EXTRA_HOST) ?: ""
        val username = activity?.intent?.getStringExtra(EXTRA_USERNAME) ?: ""
        val password = activity?.intent?.getStringExtra(EXTRA_PASSWORD) ?: ""
        val baseUrl = if (host.startsWith("http")) host else "http://$host"
        val streamUrl = "$baseUrl/series/$username/$password/${episode.id}.mp4"

        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_STREAM_TITLE, episode.title)
            putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
