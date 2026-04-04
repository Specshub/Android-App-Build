package com.iptv.player

import android.content.Intent
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

        contentAdapter = ContentAdapter { clickedItem ->
            when (clickedItem) {
                is ContentItem.Category -> {
                    Toast.makeText(context, "جاري جلب الأفلام...", Toast.LENGTH_SHORT).show()
                    viewModel.loadVodStreams(clickedItem.id)
                }
                is ContentItem.Vod -> {
                    // ─── ✅ تشغيل الفيلم ───
                    val host = activity?.intent?.getStringExtra(EXTRA_HOST) ?: ""
                    val username = activity?.intent?.getStringExtra(EXTRA_USERNAME) ?: ""
                    val password = activity?.intent?.getStringExtra(EXTRA_PASSWORD) ?: ""

                    val baseUrl = if (host.startsWith("http")) host else "http://$host"
                    // سيرفرات الأي بي تي في للأفلام تكون بصيغة: host/movie/user/pass/id.mp4
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
                val categories = resource.data
                val items = categories.map { ContentItem.Category(it.categoryId, it.categoryName) }
                contentAdapter.submitList(items)
            }
        }

        viewModel.vodStreams.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    val streams = resource.data
                    val items = streams.map { ContentItem.Vod(it) }
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
