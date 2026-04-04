package com.iptv.player.data.repository // ✅ الهوية الموحدة

import com.iptv.player.data.api.XtreamApiService
import com.iptv.player.data.api.XtreamUrlBuilder
import com.iptv.player.data.model.* // ✅ استيراد الموديلات وكلاس Resource من Models.kt
import retrofit2.Response

class XtreamRepository(private val api: XtreamApiService) {

    /**
     * ── 1. المصادقة (Login)
     */
    suspend fun authenticate(creds: LoginCredentials): Resource<AuthResponse> =
        safeApiCall { api.authenticate(XtreamUrlBuilder.authUrl(creds)) }

    /**
     * ── 2. القنوات الحية (Live TV)
     */
    suspend fun getLiveCategories(creds: LoginCredentials): Resource<List<LiveCategory>> =
        safeApiCall { api.getLiveCategories(XtreamUrlBuilder.liveCategories(creds)) }

    suspend fun getLiveStreams(creds: LoginCredentials, categoryId: String? = null): Resource<List<LiveStream>> =
        safeApiCall { 
            if (categoryId != null) {
                api.getLiveStreamsByCategory(XtreamUrlBuilder.liveStreamsByCategory(creds, categoryId), categoryId)
            } else {
                api.getLiveStreams(XtreamUrlBuilder.liveStreams(creds))
            }
        }

    /**
     * ── 3. الأفلام (VOD)
     */
    suspend fun getVodCategories(creds: LoginCredentials): Resource<List<VodCategory>> =
        safeApiCall { api.getVodCategories(XtreamUrlBuilder.vodCategories(creds)) }

    suspend fun getVodStreams(creds: LoginCredentials, categoryId: String? = null): Resource<List<VodStream>> =
        safeApiCall {
            if (categoryId != null) {
                api.getVodStreamsByCategory(XtreamUrlBuilder.vodStreamsByCategory(creds, categoryId), categoryId)
            } else {
                api.getVodStreams(XtreamUrlBuilder.vodStreams(creds))
            }
        }

    /**
     * ── 4. المسلسلات (Series)
     */
    suspend fun getSeriesCategories(creds: LoginCredentials): Resource<List<SeriesCategory>> =
        safeApiCall { api.getSeriesCategories(XtreamUrlBuilder.seriesCategories(creds)) }

    suspend fun getSeries(creds: LoginCredentials, categoryId: String? = null): Resource<List<Series>> =
        safeApiCall {
            if (categoryId != null) {
                api.getSeriesByCategory(XtreamUrlBuilder.seriesByCategory(creds, categoryId), categoryId)
            } else {
                api.getSeries(XtreamUrlBuilder.series(creds))
            }
        }

    suspend fun getSeriesInfo(creds: LoginCredentials, seriesId: Int): Resource<SeriesInfo> =
        safeApiCall { api.getSeriesInfo(XtreamUrlBuilder.seriesInfo(creds, seriesId)) }

    /**
     * 🛡️ دالة المعالجة الآمنة (The Safe Call)
     * تقوم بالتعامل مع الأخطاء لضمان عدم انهيار التطبيق
     */
    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Resource.Success(body)
                else Resource.Error("لا توجد بيانات (Empty Body)")
            } else {
                Resource.Error("خطأ من السيرفر: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "حدث خطأ غير متوقع في الاتصال")
        }
    }
}
