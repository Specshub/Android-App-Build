package com.iptv.player // ✅ الهوية الجديدة الموحدة

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.R // ✅ استيراد الموارد من المسار الصحيح
import com.iptv.player.databinding.FragmentLiveTvBinding // ✅ استيراد الربط من المسار الصحيح

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
        
        // إعداد شبكة العرض (3 أعمدة)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 3)
        // هنا سيتم ربط الـ Adapter لاحقاً
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
