package com.iptv.player
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

object DiskCache {
    private val gson = Gson()
    inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type
    private fun file(c: Context, key: String) = File(File(c.cacheDir, "lists").apply { mkdirs() }, key.replace(Regex("[^A-Za-z0-9_]"), "_") + ".json")
    fun <T> load(c: Context, key: String, type: Type): T? = try { file(c, key).takeIf { it.exists() }?.bufferedReader()?.use { gson.fromJson<T>(it, type) } } catch (e: Exception) { null }
    fun save(c: Context, key: String, data: Any?) { try { file(c, key).bufferedWriter().use { gson.toJson(data, it) } } catch (e: Exception) { } }
}
