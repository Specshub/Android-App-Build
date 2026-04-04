package com.iptv.player // ✅ الهوية الجديدة الموحدة لضمان رؤية MainActivity لها

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.R // ✅ استيراد الموارد من المسار الصحيح
import com.iptv.player.databinding.FragmentSeriesBinding // ✅ استيراد الربط من المسار الصحيح

class SeriesFragment : Fragment() {
    private var _binding: FragmentSeriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // عرض المسلسلات في شبكة من 3 أعمدة
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
