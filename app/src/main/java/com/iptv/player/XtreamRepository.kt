// com/iptvplayer/app/data/repository/XtreamRepository.kt
package com.iptvplayer.app.data.repository

import com.iptvplayer.app.data.api.RetrofitClient
import com.iptvplayer.app.data.api.XtreamUrlBuilder
import com.iptvplayer.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class XtreamRepository {

    private val api = RetrofitClient.apiService

    // ── Authentication ──────────────────────────────────────────────────────

    suspend fun authenticate(credentials: LoginCredentials): Resource<AuthResponse> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.authenticate(XtreamUrlBuilder.authUrl(credentials))
            }
        }

    // ── Live TV ─────────────────────────────────────────────────────────────

    suspend fun getLiveCategories(credentials: LoginCredentials): Resource<List<LiveCategory>> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.getLiveCategories(XtreamUrlBuilder.liveCategories(credentials))
            }
        }

    suspend fun getLiveStreams(credentials: LoginCredentials): Resource<List<LiveStream>> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.getLiveStreams(XtreamUrlBuilder.liveStreams(credentials))
            }
        }

    suspend fun getLiveStreamsByCategory(
        credentials: LoginCredentials,
        categoryId: String
    ): Resource<List<LiveStream>> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.getLiveStreams(XtreamUrlBuilder.liveStreamsByCategory(credentials, categoryId))
            }
        }

    // ── VOD ─────────────────────────────────────────────────────────────────

    suspend fun getVodCategories(credentials: LoginCredentials): Resource<List<VodCategory>> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.getVodCategories(XtreamUrlBuilder.vodCategories(credentials))
            }
        }

    suspend fun getVodStreams(credentials: LoginCredentials): Resource<List<VodStream>> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.getVodStreams(XtreamUrlBuilder.vodStreams(credentials))
            }
        }

    suspend fun getVodStreamsByCategory(
        credentials: LoginCredentials,
        categoryId: String
    ): Resource<List<VodStream>> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.getVodStreams(XtreamUrlBuilder.vodStreamsByCategory(credentials, categoryId))
            }
        }

    // ── Series ──────────────────────────────────────────────────────────────

    suspend fun getSeriesCategories(credentials: LoginCredentials): Resource<List<SeriesCategory>> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.getSeriesCategories(XtreamUrlBuilder.seriesCategories(credentials))
            }
        }

    suspend fun getSeries(credentials: LoginCredentials): Resource<List<Series>> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.getSeries(XtreamUrlBuilder.series(credentials))
            }
        }

    suspend fun getSeriesByCategory(
        credentials: LoginCredentials,
        categoryId: String
    ): Resource<List<Series>> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.getSeries(XtreamUrlBuilder.seriesByCategory(credentials, categoryId))
            }
        }

    suspend fun getSeriesInfo(
        credentials: LoginCredentials,
        seriesId: Int
    ): Resource<SeriesInfo> =
        withContext(Dispatchers.IO) {
            safeApiCall {
                api.getSeriesInfo(XtreamUrlBuilder.seriesInfo(credentials, seriesId))
            }
        }

    // ── Helper ──────────────────────────────────────────────────────────────

    private suspend fun <T> safeApiCall(
        call: suspend () -> retrofit2.Response<T>
    ): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Empty response body")
                }
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Invalid credentials. Please check your username and password."
                    403 -> "Access forbidden. Your account may be expired."
                    404 -> "Server not found. Please check the host URL."
                    500 -> "Server error. Please try again later."
                    else -> "Request failed with code: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: java.net.UnknownHostException) {
            Resource.Error("Cannot reach server. Check the host URL and your internet connection.")
        } catch (e: java.net.SocketTimeoutException) {
            Resource.Error("Connection timed out. Server may be slow or unreachable.")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred.")
        }
    }
}
