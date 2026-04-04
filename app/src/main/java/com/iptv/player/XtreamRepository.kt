package com.iptv.player.data.repository // ✅ الهوية الجديدة الموحدة

import com.iptv.player.RetrofitClient // ✅ استيراد المحرك الصحيح
import com.iptv.player.data.model.* // ✅ استيراد الموديلات الجديدة
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

// ✅ تعريف فئة Resource (ضرورية جداً لكي يعمل الكود بدون أخطاء)
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}

class XtreamRepository(private val api: XtreamApiService) { // ✅ تمرير الـ API عبر المنشئ أفضل

    // ── Authentication ──────────────────────────────────────────────────────
    suspend fun authenticate(username: String, password: String): Resource<AuthResponse> =
        withContext(Dispatchers.IO) {
            safeApiCall { api.authenticate("get_live_streams", username, password) }
        }

    // ── Live TV ─────────────────────────────────────────────────────────────
    suspend fun getLiveStreams(): Resource<List<LiveStream>> =
        withContext(Dispatchers.IO) {
            safeApiCall { api.getLiveStreams("get_live_streams") }
        }

    // ── VOD ─────────────────────────────────────────────────────────────────
    suspend fun getVodStreams(): Resource<List<VodStream>> =
        withContext(Dispatchers.IO) {
            safeApiCall { api.getVodStreams("get_vod_streams") }
        }

    // ── Series ──────────────────────────────────────────────────────────────
    suspend fun getSeries(): Resource<List<Series>> =
        withContext(Dispatchers.IO) {
            safeApiCall { api.getSeries("get_series") }
        }

    // ── Helper (المنقذ من الانهيار) ──────────────────────────────────────────
    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Resource.Success(body)
                else Resource.Error("Empty response body")
            } else {
                Resource.Error("Error code: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }
}
