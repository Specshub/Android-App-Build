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
    private var currentCreds: LoginCredentials? = null

    fun setCredentials(creds: LoginCredentials) { currentCreds = creds }
    fun getCredentials(): LoginCredentials? = currentCreds

    // الـ LiveData الأساسية
    private val _liveStreams = MutableLiveData<Resource<List<LiveStream>>>()
    val liveStreams: LiveData<Resource<List<LiveStream>>> = _liveStreams

    private val _liveCategories = MutableLiveData<Resource<List<LiveCategory>>>()
    val liveCategories: LiveData<Resource<List<LiveCategory>>> = _liveCategories

    // استعادة سرعة جلب القنوات
    fun loadLiveStreams(categoryId: String? = null) {
        val creds = currentCreds ?: return
        _liveStreams.value = Resource.Loading()
        viewModelScope.launch {
            _liveStreams.value = repository.getLiveStreams(creds, categoryId)
        }
    }

    fun loadLiveCategories() {
        val creds = currentCreds ?: return
        _liveCategories.value = Resource.Loading()
        viewModelScope.launch {
            _liveCategories.value = repository.getLiveCategories(creds)
        }
    }

    // بناء الرابط بسرعة .ts (الأسرع في التشغيل)
    fun buildStreamUrl(streamId: Int): String {
        val creds = currentCreds ?: return ""
        val host = creds.host.removeSuffix("/")
        return "$host/${creds.username}/${creds.password}/$streamId.ts"
    }
}
