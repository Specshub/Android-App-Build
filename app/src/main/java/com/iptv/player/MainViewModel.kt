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

    // ✅ حفظ بيانات الدخول الحالية لاستخدامها في بناء الروابط وجلب البيانات
    private var currentCreds: LoginCredentials? = null

    fun setCredentials(creds: LoginCredentials) {
        currentCreds = creds
    }

    // دالة لاسترجاع بيانات الدخول عند الحاجة (مثل الشاشات المتعددة)
    fun getCredentials(): LoginCredentials? = currentCreds

    // ── Live TV (البث المباشر) ─────────────────────────────────────────────
    private val _liveCategories = MutableLiveData<Resource<List<LiveCategory>>>()
    val liveCategories: LiveData<Resource<List<LiveCategory>>> = _liveCategories

    private val _liveStreams = MutableLiveData<Resource<List<LiveStream>>>()
    val liveStreams: LiveData<Resource<List<LiveStream>>> = _liveStreams

    // ── VOD (الأفلام) ──────────────────────────────────────────────────────
    private val _vodCategories = MutableLiveData<Resource<List<VodCategory>>>()
    val vodCategories: LiveData<Resource<List<VodCategory>>> = _vodCategories

    private val _vodStreams = MutableLiveData<Resource<List<VodStream>>>()
    val vodStreams: LiveData<Resource<List<VodStream>>> = _vodStreams

    // ── Series (المسلسلات) ─────────────────────────────────────────────────
    private val _seriesCategories = MutableLiveData<Resource<List<SeriesCategory>>>()
    val seriesCategories: LiveData<Resource<List<SeriesCategory>>> = _seriesCategories

    private val _seriesList = MutableLiveData<Resource<List<Series>>>()
    val seriesList: LiveData<Resource<List<Series>>> = _seriesList

    private val _seriesInfo = MutableLiveData<Resource<SeriesInfo>>()
    val seriesInfo: LiveData<Resource<SeriesInfo>> = _seriesInfo

    // ── الدوال البرمجية (Loaders) ──────────────────────────────────────────

    fun loadLiveCategories() {
        val creds = currentCreds ?: return
        _liveCategories.value = Resource.Loading()
        viewModelScope.launch {
            _liveCategories.value = repository.getLiveCategories(creds)
        }
    }

    fun loadLiveStreams(categoryId: String? = null) {
        val creds = currentCreds ?: return
        _liveStreams.value = Resource.Loading()
        viewModelScope.launch {
            _liveStreams.value = repository.getLiveStreams(creds, categoryId)
        }
    }

    // دالة خاصة لميزة الشاشات المتعددة لجلب كل القنوات
    fun loadAllLiveStreams() {
        loadLiveStreams(null)
    }

    fun loadVodCategories() {
        val creds = currentCreds ?: return
        _vodCategories.value = Resource.Loading()
        viewModelScope.launch {
            _vodCategories.value = repository.getVodCategories(creds)
        }
    }

    fun loadVodStreams(categoryId: String? = null) {
        val creds = currentCreds ?: return
        _vodStreams.value = Resource.Loading()
        viewModelScope.launch {
            _vodStreams.value = repository.getVodStreams(creds, categoryId)
        }
    }

    fun loadSeriesCategories() {
        val creds = currentCreds ?: return
        _seriesCategories.value = Resource.Loading()
        viewModelScope.launch {
            _seriesCategories.value = repository.getSeriesCategories(creds)
        }
    }

    fun loadSeries(categoryId: String? = null) {
        val creds = currentCreds ?: return
        _seriesList.value = Resource.Loading()
        viewModelScope.launch {
            _seriesList.value = repository.getSeries(creds, categoryId)
        }
    }

    fun loadSeriesInfo(seriesId: Int) {
        val creds = currentCreds ?: return
        _seriesInfo.value = Resource.Loading()
        viewModelScope.launch {
            _seriesInfo.value = repository.getSeriesInfo(creds, seriesId)
        }
    }

    // ── أدوات المساعدة (Helpers) ──────────────────────────────────────────

    /**
     * بناء رابط البث المباشر بصيغة Xtream
     * التنسيق: http://domain:port/live/username/password/stream_id.m3u8
     */
    fun buildStreamUrl(streamId: Int, extension: String = "m3u8"): String {
        val creds = currentCreds ?: return ""
        val host = creds.host.removeSuffix("/")
        return "$host/live/${creds.username}/${creds.password}/$streamId.$extension"
    }
}
