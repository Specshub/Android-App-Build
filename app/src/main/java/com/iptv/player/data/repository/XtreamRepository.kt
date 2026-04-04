package com.iptv.player.data.repository

import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.model.*
import retrofit2.Response

class XtreamRepository(private val api: XtreamApiService) {

    suspend fun authenticate(username: String, password: String): Resource<AuthResponse> =
        safeApiCall { api.authenticate("get_live_streams", username, password) }

    // قنوات البث المباشر
    suspend fun getLiveCategories(): Resource<List<LiveCategory>> =
        safeApiCall { api.getLiveCategories("get_live_categories") }

    suspend fun getLiveStreams(categoryId: String? = null): Resource<List<LiveStream>> =
        safeApiCall { 
            if (categoryId != null) api.getLiveStreamsByCategory("get_live_streams", categoryId)
            else api.getLiveStreams("get_live_streams")
        }

    // الأفلام VOD
    suspend fun getVodCategories(): Resource<List<VodCategory>> =
        safeApiCall { api.getVodCategories("get_vod_categories") }

    suspend fun getVodStreams(categoryId: String? = null): Resource<List<VodStream>> =
        safeApiCall {
            if (categoryId != null) api.getVodStreamsByCategory("get_vod_streams", categoryId)
            else api.getVodStreams("get_vod_streams")
        }

    // المسلسلات
    suspend fun getSeriesCategories(): Resource<List<SeriesCategory>> =
        safeApiCall { api.getSeriesCategories("get_series_categories") }

    suspend fun getSeries(categoryId: String? = null): Resource<List<Series>> =
        safeApiCall {
            if (categoryId != null) api.getSeriesByCategory("get_series", categoryId)
            else api.getSeries("get_series")
        }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) Resource.Success(response.body()!!)
            else Resource.Error("خطأ في السيرفر: ${response.code()}")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "حدث خطأ غير متوقع")
        }
    }
}
