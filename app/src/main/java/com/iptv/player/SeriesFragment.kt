package com.iptv.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.databinding.FragmentSeriesBinding
import com.iptv.player.data.model.Resource

class SeriesFragment : Fragment() {
    private var _binding: FragmentSeriesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var contentAdapter: ContentAdapter

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
                    viewModel.loadSeries(clickedItem.id) // نطلب المسلسلات
                }
                is ContentItem.SeriesItem -> {
                    Toast.makeText(context, "جاري فتح مسلسل: ${clickedItem.series.name}", Toast.LENGTH_LONG).show()
                    // TODO: فتح تفاصيل المسلسل والحلقات لاحقاً
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
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val seriesList = resource.data
                    val items = seriesList.map { ContentItem.SeriesItem(it) }
                    contentAdapter.submitList(items) // استبدال الباقات بالمسلسلات
                }
                is Resource.Error -> {
                    Toast.makeText(context, "خطأ: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
