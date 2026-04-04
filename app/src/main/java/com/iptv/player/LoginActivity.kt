package com.iptv.player

import android.content.Context // ✅ استيراد ضروري للذاكرة
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

const val EXTRA_HOST = "EXTRA_HOST"
const val EXTRA_USERNAME = "EXTRA_USERNAME"
const val EXTRA_PASSWORD = "EXTRA_PASSWORD"

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ─── 1. التحقق السريع: هل المستخدم مسجل دخوله مسبقاً؟ ───
        val sharedPref = getSharedPreferences("IPTV_PREFS", Context.MODE_PRIVATE)
        val savedHost = sharedPref.getString("HOST", null)
        val savedUser = sharedPref.getString("USER", null)
        val savedPass = sharedPref.getString("PASS", null)

        // إذا وجدنا البيانات محفوظة، نفتح الشاشة الرئيسية فوراً ونغلق شاشة الدخول
        if (!savedHost.isNullOrEmpty() && !savedUser.isNullOrEmpty() && !savedPass.isNullOrEmpty()) {
            openMainActivity(savedHost, savedUser, savedPass)
            return // إيقاف تنفيذ باقي الكود هنا
        }

        // ─── 2. إذا لم يكن مسجلاً، نظهر له الشاشة العادية ───
        setContentView(R.layout.activity_login)

        val etHost = findViewById<EditText>(R.id.et_host)
        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        viewModel.authState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    btnLogin.isEnabled = false
                    progressBar?.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    btnLogin.isEnabled = true
                    progressBar?.visibility = View.GONE

                    val host = etHost?.text.toString().trim()
                    val username = etUsername?.text.toString().trim()
                    val password = etPassword?.text.toString().trim()

                    // ─── 3. الدخول ناجح؟ إذن احفظ البيانات للمرات القادمة ───
                    sharedPref.edit()
                        .putString("HOST", host)
                        .putString("USER", username)
                        .putString("PASS", password)
                        .apply() // حفظ!

                    openMainActivity(host, username, password)
                }
                is Resource.Error -> {
                    btnLogin.isEnabled = true
                    progressBar?.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        btnLogin?.setOnClickListener {
            val host = etHost?.text?.toString()?.trim() ?: ""
            val username = etUsername?.text?.toString()?.trim() ?: ""
            val password = etPassword?.text?.toString()?.trim() ?: ""
            viewModel.login(host, username, password)
        }
    }

    // دالة مساعدة لفتح الشاشة الرئيسية وإغلاق شاشة الدخول تماماً
    private fun openMainActivity(host: String, user: String, pass: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_HOST, host)
            putExtra(EXTRA_USERNAME, user)
            putExtra(EXTRA_PASSWORD, pass)
        }
        startActivity(intent)
        finish() // ✅ هذه الكلمة تمنع المستخدم من العودة لشاشة الدخول عند ضغط زر "الرجوع"
    }
}
