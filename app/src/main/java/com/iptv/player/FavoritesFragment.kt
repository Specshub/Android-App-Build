package com.iptv.player

import android.content.Context
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
import com.iptv.player.databinding.FragmentLiveTvBinding
import com.iptv.player.data.model.Episode
import com.iptv.player.data.model.Resource
import com.iptv.player.data.model.SeriesInfo

class FavoritesFragment : Fragment() {
    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!
    private lateinit var contentAdapter: ContentAdapter

    // نحتاج المدير لأن تشغيل المسلسلات يتطلب جلب الحلقات
    private val viewModel: MainViewModel by activityViewModels()
    private var currentSelectedSeriesId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLiveTvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)

        // 1. الضغطة العادية (لتشغيل أي نوع من المفضلة)
        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Live -> playLive(clickedItem)
                is ContentItem.Vod -> playVod(clickedItem)
                is ContentItem.SeriesItem -> {
                    Toast.makeText(context, "جاري جلب الحلقات...", Toast.LENGTH_SHORT).show()
                    currentSelectedSeriesId = clickedItem.series.seriesId
                    viewModel.loadSeriesInfo(clickedItem.series.seriesId)
                }
                else -> {}
            }
        }

        // مراقبة حلقات المسلسلات إذا ضغط على مسلسل في المفضلة
        viewModel.seriesInfo.observe(viewLifecycleOwner) { resource ->
            if (currentSelectedSeriesId != null && resource is Resource.Success) {
                showEpisodesDialog(resource.data)
                currentSelectedSeriesId = null
            } else if (currentSelectedSeriesId != null && resource is Resource.Error) {
                Toast.makeText(context, "خطأ في جلب الحلقات", Toast.LENGTH_SHORT).show()
                currentSelectedSeriesId = null
            }
        }

        // 2. الضغطة المطولة (حذف العنصر من المفضلة وتحديث الشاشة)
        contentAdapter.onItemLongClick = { item ->
            when (item) {
                is ContentItem.Live -> FavoritesManager.removeFavorite(requireContext(), item.stream)
                is ContentItem.Vod -> FavoritesManager.removeVodFavorite(requireContext(), item.stream)
                is ContentItem.SeriesItem -> FavoritesManager.removeSeriesFavorite(requireContext(), item.series)
                else -> {}
            }
            Toast.makeText(context, "🗑️ تم الحذف من المفضلة", Toast.LENGTH_SHORT).show()
            loadFavorites() // تحديث الشاشة فوراً
            true
        }

        binding.recyclerView.adapter = contentAdapter
        loadFavorites()
    }

    // جلب كل الأنواع ودمجها في قائمة واحدة
    private fun loadFavorites() {
        val liveFavs = FavoritesManager.getFavorites(requireContext()).map { ContentItem.Live(it) }
        val vodFavs = FavoritesManager.getVodFavorites(requireContext()).map { ContentItem.Vod(it) }
        val seriesFavs = FavoritesManager.getSeriesFavorites(requireContext()).map { ContentItem.SeriesItem(it) }

        val allFavs = liveFavs + vodFavs + seriesFavs
        contentAdapter.submitList(allFavs)

        if (allFavs.isEmpty()) {
            Toast.makeText(context, "المفضلة فارغة حالياً 🌟", Toast.LENGTH_LONG).show()
        }
    }

    // دوال التشغيل
    private fun getCredentials(): Triple<String, String, String> {
        val prefs = requireContext().getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val host = prefs.getString("HOST", "") ?: ""
        val user = prefs.getString("USER", "") ?: ""
        val pass = prefs.getString("PASS", "") ?: ""
        val baseUrl = if (host.startsWith("http")) host else "http://$host"
        return Triple(baseUrl, user, pass)
    }

    private fun playLive(item: ContentItem.Live) {
        val (baseUrl, user, pass) = getCredentials()
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_STREAM_TITLE, item.stream.name)
            putExtra(PlayerActivity.EXTRA_STREAM_URL, "$baseUrl/$user/$pass/${item.stream.streamId}")
        }
        startActivity(intent)
    }

    private fun playVod(item: ContentItem.Vod) {
        val (baseUrl, user, pass) = getCredentials()
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_STREAM_TITLE, item.stream.name)
            putExtra(PlayerActivity.EXTRA_STREAM_URL, "$baseUrl/movie/$user/$pass/${item.stream.streamId}.mp4")
        }
        startActivity(intent)
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
        val (baseUrl, user, pass) = getCredentials()
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_STREAM_TITLE, episode.title)
            putExtra(PlayerActivity.EXTRA_STREAM_URL, "$baseUrl/series/$user/$pass/${episode.id}.mp4")
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
