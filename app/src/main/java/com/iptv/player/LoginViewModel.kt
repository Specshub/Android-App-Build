package com.iptv.player // ✅ الحزمة الموحدة الجديدة

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.api.RetrofitClient 
import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.model.* // ✅ استيراد الموديلات وكلاس Resource
import com.iptv.player.data.repository.XtreamRepository
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // ✅ إنشاء واجهة الـ API عبر الـ RetrofitClient
    private val apiService = RetrofitClient.instance.create(XtreamApiService::class.java)
    
    // ✅ تمرير الـ apiService للمستودع (Repository)
    private val repository = XtreamRepository(apiService)

    private val _authState = MutableLiveData<Resource<AuthResponse>>()
    val authState: LiveData<Resource<AuthResponse>> = _authState

    /**
     * تنفيذ عملية تسجيل الدخول
     */
    fun login(host: String, username: String, password: String) {
        // التحقق الأولي من الحقول
        if (host.isBlank() || username.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("الرجاء إدخال جميع البيانات")
            return
        }

        // إرسال حالة التحميل للـ UI
        _authState.value = Resource.Loading() 

        viewModelScope.launch {
            try {
                // طلب المصادقة من المستودع
                val result = repository.authenticate(username, password)
                _authState.value = result
            } catch (e: Exception) {
                _authState.value = Resource.Error(e.localizedMessage ?: "حدث خطأ أثناء الاتصال")
            }
        }
    }
}
