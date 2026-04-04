package com.iptv.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.R
import com.iptv.player.databinding.FragmentLiveTvBinding
import com.iptv.player.data.model.Resource

class LiveTvFragment : Fragment() {
    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    
    // ✅ تعريف النادل (Adapter)
    private lateinit var contentAdapter: ContentAdapter

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

        // ✅ تهيئة النادل وتحديد ماذا يحدث عند الضغط على أي باقة
        contentAdapter = ContentAdapter { clickedItem ->
            if (clickedItem is ContentItem.Category) {
                Toast.makeText(context, "جاري فتح: ${clickedItem.name}", Toast.LENGTH_SHORT).show()
                // في الخطوة القادمة سنجعله يفتح قنوات هذه الباقة
            }
        }
        binding.recyclerView.adapter = contentAdapter

        viewModel.liveCategories.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // يمكنك إظهار دائرة تحميل هنا
                }
                is Resource.Success -> {
                    val categories = resource.data
                    
                    // ✅ تحويل البيانات إلى الشكل الذي يفهمه النادل (ContentItem)
                    val items = categories.map { ContentItem.Category(it.categoryId, it.categoryName) }
                    
                    // ✅ تقديم الأطباق (الباقات) للشاشة!
                    contentAdapter.submitList(items)
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
