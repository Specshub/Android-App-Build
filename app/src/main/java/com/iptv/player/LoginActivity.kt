package com.iptv.player

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.iptv.player.data.model.Resource

// ثوابت نقل البيانات للشاشة الرئيسية
const val EXTRA_HOST = "EXTRA_HOST"
const val EXTRA_USERNAME = "EXTRA_USERNAME"
const val EXTRA_PASSWORD = "EXTRA_PASSWORD"

class LoginActivity : AppCompatActivity() {

    // ✅ استدعاء العقل المدبر لتسجيل الدخول
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. ربط الحقول بالواجهة (تأكد أن هذه الـ IDs مطابقة لملف activity_login.xml)
        val etHost = findViewById<EditText>(R.id.et_host) 
        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        
        // إذا كان لديك شريط تحميل (أضفه في الـ XML إن لم يكن موجوداً أو تجاهل هذا السطر)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar) 

        // 2. مراقبة "رد السيرفر" من الـ ViewModel
        viewModel.authState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // السيرفر يفكر.. نعطل الزر ونظهر التحميل
                    btnLogin.isEnabled = false
                    progressBar?.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    // ✅ نجاح! بيانات صحيحة، نفتح الباب
                    btnLogin.isEnabled = true
                    progressBar?.visibility = View.GONE

                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra(EXTRA_HOST, etHost?.text.toString().trim())
                        putExtra(EXTRA_USERNAME, etUsername?.text.toString().trim())
                        putExtra(EXTRA_PASSWORD, etPassword?.text.toString().trim())
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
                is Resource.Error -> {
                    // ❌ فشل! نعيد تفعيل الزر ونظهر رسالة الخطأ
                    btnLogin.isEnabled = true
                    progressBar?.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // 3. ماذا يحدث عند الضغط على زر Connect؟
        btnLogin?.setOnClickListener {
            val host = etHost?.text?.toString()?.trim() ?: ""
            val username = etUsername?.text?.toString()?.trim() ?: ""
            val password = etPassword?.text?.toString()?.trim() ?: ""

            // نأمر الـ ViewModel ببدء التحقق الفعلي من السيرفر
            viewModel.login(host, username, password)
        }
    }
}
