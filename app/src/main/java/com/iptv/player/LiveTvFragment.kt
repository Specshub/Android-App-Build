package com.iptv.player

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

class LiveTvFragment : Fragment() {
    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
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

        // ─── 1. ماذا يحدث عند الضغط؟ ───
        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    // إذا ضغط على "باقة"، نطلب من المدير جلب قنوات هذه الباقة!
                    Toast.makeText(context, "جاري جلب القنوات...", Toast.LENGTH_SHORT).show()
                    viewModel.loadLiveStreams(clickedItem.id) 
                }
                is ContentItem.Live -> {
                    // إذا ضغط على "قناة فعلية"، هنا سنقوم بتشغيل الفيديو لاحقاً!
                    Toast.makeText(context, "جاري تشغيل قناة: ${clickedItem.stream.name}", Toast.LENGTH_LONG).show()
                    // TODO: فتح شاشة مشغل الفيديو (PlayerActivity)
                }
                else -> {}
            }
        }
        binding.recyclerView.adapter = contentAdapter

        // ─── 2. مراقبة الباقات (Categories) ───
        viewModel.liveCategories.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val categories = resource.data
                val items = categories.map { ContentItem.Category(it.categoryId, it.categoryName) }
                // عرض الباقات عند فتح الشاشة
                contentAdapter.submitList(items)
            }
        }

        // ─── 3. مراقبة القنوات الفعلية (Live Streams) ───
        // ✅ هذا هو الجزء الجديد الذي سيرسم القنوات عند جلبها!
        viewModel.liveStreams.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // يمكنك إظهار شريط تحميل هنا
                }
                is Resource.Success -> {
                    val streams = resource.data
                    // تحويل البيانات إلى قنوات ليفهمها النادل
                    val items = streams.map { ContentItem.Live(it) }
                    
                    // استبدال الباقات بالقنوات في نفس الشاشة!
                    contentAdapter.submitList(items)
                }
                is Resource.Error -> {
                    Toast.makeText(context, "خطأ في جلب القنوات: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
