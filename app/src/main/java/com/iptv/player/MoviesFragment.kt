package com.iptv.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.content.Intent
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
    
    // 🚩 متغير لمتابعة الحالة: هل نحن داخل قسم أم في القائمة الرئيسية؟
    var isShowingStreams = false 

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)

        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    isShowingStreams = true // انتقلنا لعرض الأفلام
                    viewModel.loadVodStreams(clickedItem.id)
                }
                is ContentItem.Vod -> {
                    val host = activity?.intent?.getStringExtra(EXTRA_HOST) ?: ""
                    val username = activity?.intent?.getStringExtra(EXTRA_USERNAME) ?: ""
                    val password = activity?.intent?.getStringExtra(EXTRA_PASSWORD) ?: ""
                    val cleanHost = if (host.endsWith("/")) host.dropLast(1) else host
                    val baseUrl = if (cleanHost.startsWith("http")) cleanHost else "http://$cleanHost"
                    val streamUrl = "$baseUrl/movie/$username/$password/${clickedItem.stream.streamId}.mp4"

                    val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                        putExtra(PlayerActivity.EXTRA_STREAM_TITLE, clickedItem.stream.name)
                        putExtra(PlayerActivity.EXTRA_STREAM_URL, streamUrl)
                    }
                    startActivity(intent)
                }
                else -> {}
            }
        }

        binding.recyclerView.adapter = contentAdapter

        viewModel.vodCategories.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                isShowingStreams = false // نحن الآن في القائمة الرئيسية للأقسام
                contentAdapter.submitList(resource.data.map { ContentItem.Category(it.categoryId, it.categoryName) })
            }
        }

        viewModel.vodStreams.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                contentAdapter.submitList(resource.data.map { ContentItem.Vod(it) })
            }
        }
    }

    // 🔙 دالة العودة للأقسام سنستدعيها من MainActivity
    fun goBackToCategories() {
        viewModel.loadVodCategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
