package com.iptvplayer.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.iptvplayer.app.R
import com.iptvplayer.app.databinding.FragmentLiveTvBinding // This requires a new XML

class LiveTvFragment : Fragment() {
    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveTvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup RecyclerView for Live TV grid
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        // Add your adapter and load data here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
