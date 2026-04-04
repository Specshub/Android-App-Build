package com.iptv.player // ✅ الهوية الجديدة الموحدة

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.R // ✅ استيراد الموارد من المسار الصحيح
import com.iptv.player.databinding.FragmentMoviesBinding // ✅ استيراد الربط من المسار الصحيح

class MoviesFragment : Fragment() {
    private var _binding: FragmentMoviesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // إعداد شبكة العرض للأفلام (3 أعمدة)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        // سيتم إضافة الـ Adapter لاحقاً
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
