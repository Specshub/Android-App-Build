package com.iptv.player

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

class MoviesFragment : Fragment() {
    private var _binding: FragmentMoviesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var contentAdapter: ContentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)

        // ─── 1. ماذا يحدث عند الضغط؟ ───
        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    Toast.makeText(context, "جاري جلب الأفلام...", Toast.LENGTH_SHORT).show()
                    viewModel.loadVodStreams(clickedItem.id) // نطلب الأفلام
                }
                is ContentItem.Vod -> {
                    Toast.makeText(context, "جاري تشغيل فيلم: ${clickedItem.stream.name}", Toast.LENGTH_LONG).show()
                    // TODO: فتح شاشة المشغل لاحقاً
                }
                else -> {}
            }
        }
        binding.recyclerView.adapter = contentAdapter

        // ─── 2. مراقبة باقات الأفلام ───
        viewModel.vodCategories.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val categories = resource.data
                val items = categories.map { ContentItem.Category(it.categoryId, it.categoryName) }
                contentAdapter.submitList(items)
            }
        }

        // ─── 3. مراقبة الأفلام الفعلية ───
        viewModel.vodStreams.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val streams = resource.data
                    val items = streams.map { ContentItem.Vod(it) }
                    contentAdapter.submitList(items) // استبدال الباقات بالأفلام
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
