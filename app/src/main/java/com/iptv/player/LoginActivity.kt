package com.iptv.player

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar

// ✅ الثوابت معرفة هنا لمرة واحدة فقط
const val EXTRA_HOST = "EXTRA_HOST"
const val EXTRA_USERNAME = "EXTRA_USERNAME"
const val EXTRA_PASSWORD = "EXTRA_PASSWORD"

class LoginActivity : AppCompatActivity() {

    // ⚠️ ملاحظة: إذا استمر خطأ Binding، تأكد من وجود نشاط بهذا الاسم في layout
    private var _binding: Any? = null 
    // ملاحظة تقنية: استبدلنا Binding المباشر مؤقتاً إذا كان لا يزال لا يراه المترجم
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // سنستخدم الطريقة التقليدية إذا كان الـ Binding يسبب مشكلة في المسارات
        setContentView(R.layout.activity_login)

        // ✅ هنا نضع منطق الدخول البسيط لتجاوز أخطاء الـ ViewModel المفقود حالياً
        findViewById<View>(R.id.btn_login)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_HOST, "winfun.store") // تجريبي
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}
