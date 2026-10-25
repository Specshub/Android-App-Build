package com.iptv.player.data.api

import com.iptv.player.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ActivationResult(
    val active: Boolean,
    val expires: String? = null,
    val host: String? = null,
    val username: String? = null,
    val password: String? = null,
    val error: String? = null
)

/**
 * عقد الـ API المتوقع من السيرفر:
 * POST {mac, key}  ->  {"status":"active","expires":"2026-11-25","host":"...","username":"...","password":"..."}
 *                  أو  {"status":"inactive"}
 */
object DeviceApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun check(mac: String, key: String): ActivationResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().put("mac", mac).put("key", key).toString()
            val request = Request.Builder()
                .url(Constants.ACTIVATION_API_URL)
