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
    suspend fun check(mac: String, key: String, device: Map<String, String> = emptyMap()): ActivationResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().put("mac", mac).put("key", key).put("device", JSONObject(device)).toString()
            val request = Request.Builder().url(Constants.ACTIVATION_API_URL).post(json.toRequestBody("application/json".toMediaType())).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use ActivationResult(false, error = "HTTP ${response.code}")
                val body = JSONObject(response.body?.string() ?: "{}")
                if (body.optString("status") == "active") ActivationResult(true, body.optString("expires").ifEmpty { null }, body.optString("host").ifEmpty { null }, body.optString("username").ifEmpty { null }, body.optString("password").ifEmpty { null })
                else ActivationResult(false)
            }
        } catch (e: Exception) { ActivationResult(false, error = e.localizedMessage) }
    }
}
