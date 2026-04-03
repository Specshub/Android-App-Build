// com/iptvplayer/app/ui/viewmodel/MainViewModel.kt
package com.iptvplayer.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.local.PrefsManager
import com.iptvplayer.app.data.model.*
import com.iptvplayer.app.data.repository.XtreamRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val prefsManager = PrefsManager(application)
    private val repository = XtreamRepository()

    private var credentials: LoginCredentials? = null

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

    // ── Series Detail ────────────────────────────────────────────────────────
    private val _seriesInfo = MutableLiveData<Resource<SeriesInfo>>()
    val seriesInfo: LiveData<Resource<SeriesInfo>> = _seriesInfo

    fun setCredentials(c: LoginCredentials) {
        credentials = c
    }

    fun getCredentials() = credentials ?: prefsManager.getCredentials()

    // ── Loaders ─────────────────────────────────────────────────────────────

    fun loadLiveCategories() {
        val creds = getCredentials() ?: return
        _liveCategories.value = Resource.Loading()
        viewModelScope.launch {
            _liveCategories.value = repository.getLiveCategories(creds)
        }
    }

    fun loadLiveStreams(categoryId: String? = null) {
        val creds = getCredentials() ?: return
        _liveStreams.value = Resource.Loading()
        viewModelScope.launch {
            _liveStreams.value = if (categoryId != null) {
                repository.getLiveStreamsByCategory(creds, categoryId)
            } else {
                repository.getLiveStreams(creds)
            }
        }
    }

    fun loadVodCategories() {
        val creds = getCredentials() ?: return
        _vodCategories.value = Resource.Loading()
        viewModelScope.launch {
            _vodCategories.value = repository.getVodCategories(creds)
        }
    }

    fun loadVodStreams(categoryId: String? = null) {
        val creds = getCredentials() ?: return
        _vodStreams.value = Resource.Loading()
        viewModelScope.launch {
            _vodStreams.value = if (categoryId != null) {
                repository.getVodStreamsByCategory(creds, categoryId)
            } else {
                repository.getVodStreams(creds)
            }
        }
    }

    fun loadSeriesCategories() {
        val creds = getCredentials() ?: return
        _seriesCategories.value = Resource.Loading()
        viewModelScope.launch {
            _seriesCategories.value = repository.getSeriesCategories(creds)
        }
    }

    fun loadSeries(categoryId: String? = null) {
        val creds = getCredentials() ?: return
        _seriesList.value = Resource.Loading()
        viewModelScope.launch {
            _seriesList.value = if (categoryId != null) {
                repository.getSeriesByCategory(creds, categoryId)
            } else {
                repository.getSeries(creds)
            }
        }
    }

    fun loadSeriesInfo(seriesId: Int) {
        val creds = getCredentials() ?: return
        _seriesInfo.value = Resource.Loading()
        viewModelScope.launch {
            _seriesInfo.value = repository.getSeriesInfo(creds, seriesId)
        }
    }

    fun logout() {
        prefsManager.clearCredentials()
        credentials = null
    }
}
