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

data class ActivationResult(val active: Boolean, val expires: String? = null, val host: String? = null, val username: String? = null, val password: String? = null, val error: String? = null)

object DeviceApi {
    private val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build()
    private fun JSONObject.str(k: String): String? = optString(k).ifEmpty { null }
    suspend fun check(mac: String, key: String): ActivationResult = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().put("mac", mac).put("key", key).toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(Constants.ACTIVATION_API_URL).post(body).build()
            client.newCall(request).execute().use { r ->
                if (!r.isSuccessful) return@use ActivationResult(false, error = "HTTP ${r.code}")
                val j = JSONObject(r.body?.string() ?: "{}")
                if (j.optString("status") == "active") ActivationResult(true, j.str("expires"), j.str("host"), j.str("username"), j.str("password")) else ActivationResult(false)
            }
        } catch (e: Exception) { ActivationResult(false, error = e.localizedMessage ?: "error") }
    }
}
