package com.iptv.player // ✅ الهوية الموحدة

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.api.RetrofitClient // ✅ استيراد المحرك
import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.model.* // ✅ استيراد الموديلات الجديدة
import com.iptv.player.data.repository.Resource // ✅ استيراد كلاس النتائج
import com.iptv.player.data.repository.XtreamRepository
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // ✅ تحديث طريقة إنشاء المستودع ليتناسب مع الكود الجديد
    private val apiService = RetrofitClient.instance.create(XtreamApiService::class.java)
    private val repository = XtreamRepository(apiService)

    private val _authState = MutableLiveData<Resource<AuthResponse>>()
    val authState: LiveData<Resource<AuthResponse>> = _authState

    fun login(host: String, username: String, password: String) {
        val trimmedHost = host.trim().let {
            if (!it.startsWith("http://") && !it.startsWith("https://")) "http://$it" else it
        }

        if (trimmedHost.isEmpty() || username.isEmpty() || password.isEmpty()) {
            _authState.value = Resource.Error("الرجاء ملء جميع الحقول")
            return
        }

        _authState.value = Resource.Loading()

        viewModelScope.launch {
            // ✅ استخدام الدالة المحدثة في المستودع
            val result = repository.authenticate(username, password)
            _authState.value = result
        }
    }
}
