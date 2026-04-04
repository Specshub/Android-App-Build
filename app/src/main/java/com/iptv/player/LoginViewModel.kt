package com.iptv.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.api.RetrofitClient // ✅ تأكد من وجوده في data/api
import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.model.* import com.iptv.player.data.repository.XtreamRepository
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.instance.create(XtreamApiService::class.java)
    private val repository = XtreamRepository(apiService)

    private val _authState = MutableLiveData<Resource<AuthResponse>>()
    val authState: LiveData<Resource<AuthResponse>> = _authState

    fun login(host: String, username: String, password: String) {
        if (host.isBlank() || username.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("الرجاء ملء جميع الحقول")
            return
        }

        // ✅ إنشاء كائن البيانات المطلوب
        val creds = LoginCredentials(host, username, password)

        _authState.value = Resource.Loading()
        viewModelScope.launch {
            // ✅ تمرير الكائن بدلاً من النصوص المنفردة
            _authState.value = repository.authenticate(creds)
        }
    }
}
