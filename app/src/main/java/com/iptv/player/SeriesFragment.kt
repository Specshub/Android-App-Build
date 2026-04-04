package com.iptv.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // ✅ استيراد القائمة المنبثقة
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.databinding.FragmentSeriesBinding
import com.iptv.player.data.model.Resource
import com.iptv.player.data.model.Episode
import com.iptv.player.data.model.SeriesInfo

class SeriesFragment : Fragment() {
    private var _binding: FragmentSeriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var contentAdapter: ContentAdapter

    // متغير ذكي لمنع تكرار ظهور قائمة الحلقات عند الرجوع من شاشة المشغل
    private var currentSelectedSeriesId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)

        // ─── 1. ماذا يحدث عند الضغط؟ ───
        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    Toast.makeText(context, "جاري جلب المسلسلات...", Toast.LENGTH_SHORT).show()
                    viewModel.loadSeries(clickedItem.id)
                }
                is ContentItem.SeriesItem -> {
                    Toast.makeText(context, "جاري جلب الحلقات...", Toast.LENGTH_SHORT).show()
                    // نحفظ ID المسلسل ونطلب الحلقات من السيرفر
                    currentSelectedSeriesId = clickedItem.series.seriesId
                    viewModel.loadSeriesInfo(clickedItem.series.seriesId)
                }
                else -> {}
            }
        }
        binding.recyclerView.adapter = contentAdapter

        // ─── 2. مراقبة باقات المسلسلات ───
        viewModel.seriesCategories.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val categories = resource.data
                val items = categories.map { ContentItem.Category(it.categoryId, it.categoryName) }
                contentAdapter.submitList(items)
            }
        }

        // ─── 3. مراقبة المسلسلات الفعلية ───
        viewModel.seriesList.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val seriesList = resource.data
                val items = seriesList.map { ContentItem.SeriesItem(it) }
                contentAdapter.submitList(items)
            }
        }

        // ─── 4. مراقبة الحلقات (التحديث الجديد) ───
        viewModel.seriesInfo.observe(viewLifecycleOwner) { resource ->
            // نتحقق أننا نحن من طلبنا الحلقات (عن طريق المتغير الذكي)
            if (currentSelectedSeriesId != null && resource is Resource.Success) {
                showEpisodesDialog(resource.data)
                // نصفر المتغير لكي لا تظهر القائمة تلقائياً عند الرجوع للشاشة
                currentSelectedSeriesId = null 
            } else if (currentSelectedSeriesId != null && resource is Resource.Error) {
                Toast.makeText(context, "خطأ في جلب الحلقات", Toast.LENGTH_SHORT).show()
                currentSelectedSeriesId = null
            }
        }
    }

    // ─── 5. نافذة اختيار الحلقة ───
    private fun showEpisodesDialog(seriesInfo: SeriesInfo) {
        val allEpisodes = mutableListOf<Episode>()
        
        // تجميع كل الحلقات من كل المواسم في قائمة واحدة مرتبة
        seriesInfo.episodes.toSortedMap().forEach { (seasonNum, episodes) ->
            allEpisodes.addAll(episodes)
        }

        if (allEpisodes.isEmpty()) {
            Toast.makeText(context, "لا توجد حلقات متاحة", Toast.LENGTH_SHORT).show()
            return
        }

        // تجهيز نصوص القائمة (مثال: الموسم 1 - الحلقة 1: اسم الحلقة)
        val episodeTitles = allEpisodes.map { "الموسم ${it.season} | الحلقة ${it.id} - ${it.title}" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle(seriesInfo.info.name ?: "اختر الحلقة")
            .setItems(episodeTitles) { _, which ->
                val selectedEpisode = allEpisodes[which]
                playEpisode(selectedEpisode)
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    // ─── 6. تشغيل الحلقة المختارة ───
    private fun playEpisode(episode: Episode) {
        val host = activity?.intent?.getStringExtra(EXTRA_HOST) ?: ""
        val username = activity?.intent?.getStringExtra(EXTRA_USERNAME) ?: ""
        val password = activity?.intent?.getStringExtra(EXTRA_PASSWORD) ?: ""

        val baseUrl = if (host.startsWith("http")) host else "http://$host"
        // الصيغة القياسية للحلقات: host/series/user/pass/id.mp4
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
