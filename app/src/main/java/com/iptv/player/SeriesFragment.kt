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
import com.iptv.player.data.model.Series
import com.iptv.player.data.model.SeriesCategory
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

        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    isShowingStreams = true // ⬅️ تحديث الحالة: دخلنا إلى قسم المسلسلات
                    Toast.makeText(context, "جاري جلب المسلسلات...", Toast.LENGTH_SHORT).show()
                    viewModel.loadSeries(clickedItem.id)
                }
                is ContentItem.SeriesItem -> { // ✅ تم استخدام الاسم الصحيح من مشروعك
                    currentSelectedSeriesId = clickedItem.series.seriesId
                    Toast.makeText(context, "جاري جلب الحلقات...", Toast.LENGTH_SHORT).show()
                    viewModel.loadSeriesInfo(clickedItem.series.seriesId)
                }
                else -> {}
            }
        }

        binding.recyclerView.adapter = contentAdapter

        // ✅ إضافة <*> لحل خطأ المترجم مع استخدام submitList الأصلية الخاصة بك
        viewModel.seriesCategories.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success<*>) {
                isShowingStreams = false // ⬅️ تحديث الحالة: عدنا إلى أقسام المسلسلات
                val data = resource.data as? List<SeriesCategory> ?: return@observe
                contentAdapter.submitList(data.map { ContentItem.Category(it.categoryId, it.categoryName) })
            }
        }

        viewModel.seriesList.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success<*>) {
                val data = resource.data as? List<Series> ?: return@observe
                contentAdapter.submitList(data.map { ContentItem.SeriesItem(it) }) // ✅ SeriesItem
            }
        }

        viewModel.seriesInfo.observe(viewLifecycleOwner) { resource ->
            if (currentSelectedSeriesId != null && resource is Resource.Success<*>) {
                val data = resource.data as? SeriesInfo ?: return@observe
                showEpisodesDialog(data)
            }
        }
    }

    // 🔙 دالة العودة للأقسام (تستدعى من MainActivity عند ضغط زر الرجوع)
    fun goBackToCategories() {
        viewModel.loadSeriesCategories()
    }

    // عرض نافذة الحلقات
    private fun showEpisodesDialog(seriesInfo: SeriesInfo) {
        val allEpisodes = mutableListOf<Episode>()
        seriesInfo.episodes.toSortedMap().forEach { (_, episodes) -> allEpisodes.addAll(episodes) }

        if (allEpisodes.isEmpty()) {
            Toast.makeText(context, "لا توجد حلقات متاحة", Toast.LENGTH_SHORT).show()
            return
        }

        // استخدام index لترقيم الحلقات لتجنب أي أخطاء في المتغيرات
        val episodeNames = allEpisodes.mapIndexed { index, ep -> 
            "الحلقة ${index + 1}: ${ep.title ?: "بدون عنوان"}" 
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("اختر الحلقة")
            .setItems(episodeNames) { _, which ->
                val selectedEp = allEpisodes[which]
                
                val creds = viewModel.getCredentials()
                val host = creds?.host?.removeSuffix("/") ?: ""
                val user = creds?.username ?: ""
                val pass = creds?.password ?: ""
                
                // استخدام صيغة .mp4 كصيغة افتراضية للمسلسلات لتجنب خطأ containerExtension
                val streamUrl = "$host/series/$user/$pass/${selectedEp.id}.mp4"
                
                val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_STREAM_TITLE, selectedEp.title)
                    putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
                }
                startActivity(intent)
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
