package com.iptv.player.data.api // ✅ الهوية الجديدة الموحدة

import com.iptv.player.data.model.* // ✅ استيراد الموديلات من المسار الجديد
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface XtreamApiService {

    // ── Authentication ──────────────────────────────────────────────────────
    @GET
    suspend fun authenticate(
        @Url url: String
    ): Response<AuthResponse>

    // ── Live TV ─────────────────────────────────────────────────────────────
    @GET
    suspend fun getLiveCategories(
        @Url url: String
    ): Response<List<LiveCategory>>

    @GET
    suspend fun getLiveStreams(
        @Url url: String
    ): Response<List<LiveStream>>

    @GET
    suspend fun getLiveStreamsByCategory(
        @Url url: String,
        @Query("category_id") categoryId: String
    ): Response<List<LiveStream>>

    // ── VOD (Movies) ────────────────────────────────────────────────────────
    @GET
    suspend fun getVodCategories(
        @Url url: String
    ): Response<List<VodCategory>>

    @GET
    suspend fun getVodStreams(
        @Url url: String
    ): Response<List<VodStream>>

    @GET
    suspend fun getVodStreamsByCategory(
        @Url url: String,
        @Query("category_id") categoryId: String
    ): Response<List<VodStream>>

    // ── Series ──────────────────────────────────────────────────────────────
    @GET
    suspend fun getSeriesCategories(
        @Url url: String
    ): Response<List<SeriesCategory>>

    @GET
    suspend fun getSeries(
        @Url url: String
    ): Response<List<Series>>

    @GET
    suspend fun getSeriesByCategory(
        @Url url: String,
        @Query("category_id") categoryId: String
    ): Response<List<Series>>

    @GET
    suspend fun getSeriesInfo(
        @Url url: String
    ): Response<SeriesInfo>
}
