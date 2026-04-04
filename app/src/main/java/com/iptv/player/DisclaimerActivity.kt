package com.iptv.player // ✅ الهوية الجديدة الموحدة

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
// ✅ تصحيح استدعاء الربط ليتناسب مع النطاق الجديد
import com.iptv.player.databinding.ActivityDisclaimerBinding 

// ملاحظة: تأكد أن ملف PrefsManager موجود في الحزمة com.iptv.player
// أو قم بتعديل سطر الاستدعاء الخاص به ليكون كالتالي:
// import com.iptv.player.data.local.PrefsManager 

class DisclaimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisclaimerBinding
    // استبدلنا PrefsManager مؤقتاً بـ Any إذا لم نكن قد أصلحنا ملفه بعد لتجاوز خطأ البناء
    // ولكن الأفضل أن تتأكد من وجوده في المسار الصحيح
    private var prefsManager: Any? = null 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisclaimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // prefsManager = PrefsManager(this)

        binding.btnAccept.setOnClickListener {
            // prefsManager?.setDisclaimerAccepted(true)
            finish()
        }

        binding.btnDecline.setOnClickListener {
            // منطق الخروج البسيط لتجنب الأخطاء حالياً
            finishAffinity() 
        }
    }
}
