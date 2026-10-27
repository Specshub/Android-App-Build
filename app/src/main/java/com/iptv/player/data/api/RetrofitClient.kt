package com.iptv.player.data.api
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://localhost/"
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES)).retryOnConnectionFailure(true)
        .addInterceptor { ch -> val rq = ch.request().newBuilder().header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36").build(); var n = 0; var res: okhttp3.Response? = null; var err: java.io.IOException? = null
            while (n < 3 && res == null) { try { res = ch.proceed(rq) } catch (e: java.io.IOException) { err = e; n++; if (n < 3) Thread.sleep(400L * n) } }
            res ?: throw (err ?: java.io.IOException("network")) }
        .dispatcher(okhttp3.Dispatcher().apply { maxRequests = 32; maxRequestsPerHost = 8 }).build()
    val instance: Retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build()
    }
}
