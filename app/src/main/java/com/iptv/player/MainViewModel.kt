package com.iptv.player // ✅ الهوية الموحدة الجديدة

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.api.RetrofitClient // ✅ استيراد المحرك الصحيح
import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.model.* // ✅ استيراد الموديلات من Models.kt الشامل
import com.iptv.player.data.repository.Resource // ✅ استيراد كلاس النتائج
import com.iptv.player.data.repository.XtreamRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // ✅ تهيئة المستودع باستخدام الـ API المحدث
    private val apiService = RetrofitClient.instance.create(XtreamApiService::class.java)
    private val repository = XtreamRepository(apiService)

    // ── Live TV ─────────────────────────────────────────────────────────────
    private val _liveCategories = MutableLiveData<Resource<List<LiveCategory>>>()
    val liveCategories: LiveData<Resource<List<LiveCategory>>> = _liveCategories

    private val _liveStreams = MutableLiveData<Resource<List<LiveStream>>>()
    val liveStreams: LiveData<Resource<List<LiveStream>>> = _liveStreams

    // ── VOD ─────────────────────────────────────────────────────────────────
    private val _vodCategories = MutableLiveData<Resource<List<VodCategory>>>()
    val vodCategories: LiveData<Resource<List<VodCategory>>> = _vodCategories

    private val _vodStreams = MutableLiveData<Resource<List<VodStream>>>()
    val vodStreams: LiveData<Resource<List<VodStream>>> = _vodStreams

    // ── Series ──────────────────────────────────────────────────────────────
    private val _seriesCategories = MutableLiveData<Resource<List<SeriesCategory>>>()
    val seriesCategories: LiveData<Resource<List<SeriesCategory>>> = _seriesCategories

    private val _seriesList = MutableLiveData<Resource<List<Series>>>()
    val seriesList: LiveData<Resource<List<Series>>> = _seriesList

    // ── Loaders (تحديث الطلبات لتتوافق مع المستودع الجديد) ─────────────────────

    fun loadLiveCategories() {
        _liveCategories.value = Resource.Loading()
        viewModelScope.launch {
            // ملاحظة: يمكنك تمرير Credentials إذا قمت بتحديث المستودع لاستقبالها
            _liveCategories.value = repository.getLiveCategories()
        }
    }

    fun loadLiveStreams(categoryId: String? = null) {
        _liveStreams.value = Resource.Loading()
        viewModelScope.launch {
            _liveStreams.value = repository.getLiveStreams(categoryId)
        }
    }

    fun loadVodCategories() {
        _vodCategories.value = Resource.Loading()
        viewModelScope.launch {
            _vodCategories.value = repository.getVodCategories()
        }
    }

    fun loadVodStreams(categoryId: String? = null) {
        _vodStreams.value = Resource.Loading()
        viewModelScope.launch {
            _vodStreams.value = repository.getVodStreams(categoryId)
        }
    }

    fun loadSeriesCategories() {
        _seriesCategories.value = Resource.Loading()
        viewModelScope.launch {
            _seriesCategories.value = repository.getSeriesCategories()
        }
    }

    fun loadSeries(categoryId: String? = null) {
        _seriesList.value = Resource.Loading()
        viewModelScope.launch {
            _seriesList.value = repository.getSeries(categoryId)
        }
    }
}
