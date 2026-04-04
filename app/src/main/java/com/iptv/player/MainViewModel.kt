package com.iptv.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.api.RetrofitClient
import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.model.*
import com.iptv.player.data.repository.XtreamRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.instance.create(XtreamApiService::class.java)
    private val repository = XtreamRepository(apiService)

    // ✅ متغير لحفظ بيانات الدخول لاستخدامها في الطلبات
    private var currentCreds: LoginCredentials? = null

    fun setCredentials(creds: LoginCredentials) {
        currentCreds = creds
    }

    // ── Live TV ─────────────────────────────────────────────────────
    private val _liveCategories = MutableLiveData<Resource<List<LiveCategory>>>()
    val liveCategories: LiveData<Resource<List<LiveCategory>>> = _liveCategories

    fun loadLiveCategories() {
        val creds = currentCreds ?: return // لا نفعل شيئاً إذا لم تتوفر البيانات
        _liveCategories.value = Resource.Loading()
        viewModelScope.launch {
            // ✅ تمرير الـ creds للمستودع
            _liveCategories.value = repository.getLiveCategories(creds)
        }
    }

    fun loadLiveStreams(categoryId: String? = null) {
        val creds = currentCreds ?: return
        viewModelScope.launch {
            // ✅ الترتيب الصحيح: الـ creds أولاً ثم الـ categoryId
            _liveStreams.value = repository.getLiveStreams(creds, categoryId)
        }
    }

    // ... كرر نفس النمط (تمرير creds أولاً) في كل من:
    // loadVodCategories, loadVodStreams, loadSeriesCategories, loadSeries
}
