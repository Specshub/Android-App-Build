package com.iptv.player
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

fun <T : Any> MainViewModel.cached(live: MutableLiveData<Resource<T>>, key: String, type: Type, fetch: suspend () -> Resource<T>) {
    viewModelScope.launch {
        val old: T? = withContext(Dispatchers.IO) { DiskCache.load<T>(getApplication<android.app.Application>(), key, type) }
        live.value = if (old != null) Resource.Success(old) else Resource.Loading()
        val r = fetch()
        if (r is Resource.Success) { if (r.data != old) live.value = r; withContext(Dispatchers.IO) { DiskCache.save(getApplication<android.app.Application>(), key, r.data) } } else if (old == null) live.value = r
    }
}
fun cid(c: LoginCredentials) = "${c.host.hashCode()}_${c.username}"
