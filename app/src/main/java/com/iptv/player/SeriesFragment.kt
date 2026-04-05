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

        // ─── إعداد المحول (Adapter) ───
        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    isShowingStreams = true
                    Toast.makeText(context, "جاري جلب المسلسلات...", Toast.LENGTH_SHORT).show()
                    // استدعاء دالة المسلسلات من الـ ViewModel
                    viewModel.loadSeries(clickedItem.id)
                }
                is ContentItem.Series -> {
                    currentSelectedSeriesId = clickedItem.series.seriesId
                    Toast.makeText(context, "جاري جلب الحلقات...", Toast.LENGTH_SHORT).show()
                    // جلب معلومات المسلسل (المواسم والحلقات)
                    viewModel.loadSeriesInfo(clickedItem.series.seriesId)
                }
                else -> {}
            }
        }

        // إعداد المفضلة عند الضغط المطول
        contentAdapter.onItemLongClick = { item ->
            if (item is ContentItem.Series) {
                // ملاحظة: يمكنك برمجة المفضلة للمسلسلات هنا لاحقاً
                Toast.makeText(context, "تم تحديد المسلسل", Toast.LENGTH_SHORT).show()
                true
            } else false
        }

        binding.recyclerView.adapter = contentAdapter

        // ─── المراقبة (Observers) ───

        // مراقبة باقات المسلسلات
        viewModel.seriesCategories.observe(viewLifecycleOwner) { resource ->
            // ✅ تم إضافة <*> لحل خطأ المترجم (Compiler)
            when (resource) {
                is Resource.Success<*> -> {
                    isShowingStreams = false
                    val data = resource.data as? List<SeriesCategory> ?: return@observe
                    val items = data.map { ContentItem.Category(it.categoryId, it.categoryName) }
                    contentAdapter.setAllItems(items)
                }
                is Resource.Error<*> -> {
                    Toast.makeText(context, "خطأ: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading<*> -> {
                    // جاري التحميل
                }
            }
        }

        // مراقبة قائمة المسلسلات
        viewModel.seriesList.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success<*> -> {
                    val data = resource.data as? List<Series> ?: return@observe
                    val items = data.map { ContentItem.Series(it) }
                    contentAdapter.setAllItems(items)
                }
                is Resource.Error<*> -> {
                    Toast.makeText(context, "خطأ: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading<*> -> {
                    // جاري التحميل
                }
            }
        }

        // مراقبة معلومات المسلسل (لإظهار الحلقات)
        viewModel.seriesInfo.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success<*> -> {
                    val data = resource.data as? SeriesInfo ?: return@observe
                    showEpisodesDialog(data.episodes)
                }
                is Resource.Error<*> -> {
                    Toast.makeText(context, "خطأ في جلب الحلقات: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading<*> -> {
                    // جاري التحميل
                }
            }
        }
    }

    // ─── عرض نافذة الحلقات (Dialog) ───
    private fun showEpisodesDialog(episodesMap: Map<String, List<Episode>>) {
        if (episodesMap.isEmpty()) {
            Toast.makeText(context, "لا توجد حلقات متاحة", Toast.LENGTH_SHORT).show()
            return
        }

        // تحويل خريطة الحلقات (حسب المواسم) إلى قائمة واحدة مسطحة
        val allEpisodes = episodesMap.values.flatten()
        val episodeNames = allEpisodes.map { "الموسم ${it.season} - حلقة ${it.episodeNum}: ${it.title}" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("اختر الحلقة")
            .setItems(episodeNames) { _, which ->
                val selectedEp = allEpisodes[which]
                
                // جلب بيانات الاعتماد لبناء رابط المسلسل
                val creds = viewModel.getCredentials()
                val host = creds?.host?.removeSuffix("/") ?: ""
                val user = creds?.username ?: ""
                val pass = creds?.password ?: ""
                
                // تنسيق رابط المسلسلات في Xtream Codes
                val streamUrl = "$host/series/$user/$pass/${selectedEp.id}.${selectedEp.containerExtension}"
                
                val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_STREAM_TITLE, selectedEp.title)
                    putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
                }
                startActivity(intent)
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    fun goBackToCategories() {
        // استدعاء دالة تحميل أقسام المسلسلات
        viewModel.loadSeriesCategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
