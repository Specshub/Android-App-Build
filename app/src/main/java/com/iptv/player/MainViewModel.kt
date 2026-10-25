package com.iptv.player
import android.app.Application
import androidx.lifecycle.*
import com.iptv.player.data.api.RetrofitClient
import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.model.*
import com.iptv.player.data.repository.XtreamRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = XtreamRepository(RetrofitClient.instance.create(XtreamApiService::class.java))
    private var currentCreds: LoginCredentials? = null
    fun setCredentials(creds: LoginCredentials) { currentCreds = creds }
    fun getCredentials(): LoginCredentials? = currentCreds
    val _liveCategories = MutableLiveData<Resource<List<LiveCategory>>>(); val liveCategories: LiveData<Resource<List<LiveCategory>>> = _liveCategories
    val _liveStreams = MutableLiveData<Resource<List<LiveStream>>>(); val liveStreams: LiveData<Resource<List<LiveStream>>> = _liveStreams
    val _vodCategories = MutableLiveData<Resource<List<VodCategory>>>(); val vodCategories: LiveData<Resource<List<VodCategory>>> = _vodCategories
    val _vodStreams = MutableLiveData<Resource<List<VodStream>>>(); val vodStreams: LiveData<Resource<List<VodStream>>> = _vodStreams
    val _seriesCategories = MutableLiveData<Resource<List<SeriesCategory>>>(); val seriesCategories: LiveData<Resource<List<SeriesCategory>>> = _seriesCategories
    val _seriesList = MutableLiveData<Resource<List<Series>>>(); val seriesList: LiveData<Resource<List<Series>>> = _seriesList
    val _seriesInfo = MutableLiveData<Resource<SeriesInfo>>(); val seriesInfo: LiveData<Resource<SeriesInfo>> = _seriesInfo
    fun loadLiveCategories() { val c = currentCreds ?: return; cached(_liveCategories, "lc_${cid(c)}", DiskCache.typeOf<List<LiveCategory>>()) { repository.getLiveCategories(c) } }
    fun loadLiveStreams(categoryId: String? = null) { val c = currentCreds ?: return; cached(_liveStreams, "ls_${cid(c)}_$categoryId", DiskCache.typeOf<List<LiveStream>>()) { repository.getLiveStreams(c, categoryId) } }
    fun loadAllLiveStreams() { loadLiveStreams(null) }
    fun loadVodCategories() { val c = currentCreds ?: return; cached(_vodCategories, "vc_${cid(c)}", DiskCache.typeOf<List<VodCategory>>()) { repository.getVodCategories(c) } }
    fun loadVodStreams(categoryId: String? = null) { val c = currentCreds ?: return; cached(_vodStreams, "vs_${cid(c)}_$categoryId", DiskCache.typeOf<List<VodStream>>()) { repository.getVodStreams(c, categoryId) } }
    fun loadSeriesCategories() { val c = currentCreds ?: return; cached(_seriesCategories, "sc_${cid(c)}", DiskCache.typeOf<List<SeriesCategory>>()) { repository.getSeriesCategories(c) } }
    fun loadSeries(categoryId: String? = null) { val c = currentCreds ?: return; cached(_seriesList, "sl_${cid(c)}_$categoryId", DiskCache.typeOf<List<Series>>()) { repository.getSeries(c, categoryId) } }
    fun loadSeriesInfo(seriesId: Int) { val c = currentCreds ?: return; _seriesInfo.value = Resource.Loading(); viewModelScope.launch { _seriesInfo.value = repository.getSeriesInfo(c, seriesId) } }
    fun buildStreamUrl(streamId: Int, extension: String = "ts"): String { val c = currentCreds ?: return ""; return "${c.host.removeSuffix("/")}/live/${c.username}/${c.password}/$streamId.$extension" }
}
