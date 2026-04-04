package com.iptv.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // ✅ ضروري جداً للمشاركة مع الشاشة الرئيسية
import androidx.recyclerview.widget.GridLayoutManager
import com.iptv.player.R
import com.iptv.player.databinding.FragmentLiveTvBinding
import com.iptv.player.data.model.Resource // ✅ استيراد الحالات

class LiveTvFragment : Fragment() {
    private var _binding: FragmentLiveTvBinding? = null
    private val binding get() = _binding!!

    // ✅ استدعاء "المدير" المشترك مع الشاشة الرئيسية (يحتوي على البيانات)
    private val viewModel: MainViewModel by activityViewModels()

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

        // ─── ✅ مراقبة البيانات القادمة من السيرفر ───
        viewModel.liveCategories.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // السيرفر يبحث عن القنوات... يمكنك إظهار دائرة تحميل هنا
                }
                is Resource.Success -> {
                    val categories = resource.data
                    
                    // ✅ مبروك! البيانات وصلت.. سنظهر رسالة للتأكيد!
                    Toast.makeText(context, "تم جلب ${categories.size} باقة بنجاح! 📺", Toast.LENGTH_LONG).show()
                    
                    // 🚨 هنا يجب أن نربط الـ Adapter الخاص بك!
                    // إذا كان لديك CategoryAdapter أو ContentAdapter قم بإلغاء التهميش عن السطر التالي وعدله:
                    // binding.recyclerView.adapter = ContentAdapter(categories) 
                }
                is Resource.Error -> {
                    // فشل جلب القنوات
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
