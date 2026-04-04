package com.iptv.player

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class DownloadedItem(val id: String, val title: String, val localPath: String, val posterUrl: String?)

object OfflineManager {
    private const val PREFS_NAME = "downloads_prefs"
    private const val KEY_DOWNLOADS = "saved_downloads"

    fun startDownload(context: Context, url: String, title: String, id: String, posterUrl: String?) {
        val cleanTitle = title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val fileName = "${cleanTitle}_$id.mp4"
        
        // ─── 🚀 التغيير 1: الحفظ في مجلد التنزيلات العام المسموح به في كل الهواتف ───
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, fileName)

        if (file.exists()) {
            Toast.makeText(context, "هذا الفيلم محمل مسبقاً! ⬇️", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setDescription("جاري تحميل الفيلم للمشاهدة أوفلاين...")
                // ─── 🚀 التغيير 2: حذفنا قيود نوع الملف ليتقبل السيرفر أي صيغة (mkv, avi, mp4) ───
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                // ─── 🚀 التغيير 3: التخفي كمشغل ExoPlayer الحقيقي (الذي تستخدمه للتطبيق) ───
                .addRequestHeader("User-Agent", "ExoPlayer/2.18.1 (Linux; Android 11)") 
                .addRequestHeader("Icy-MetaData", "1") // بعض السيرفرات تطلب هذا

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(context, "بدأ التحميل... راجع شريط الإشعارات ⬇️", Toast.LENGTH_LONG).show()

            val newItem = DownloadedItem(id, title, file.absolutePath, posterUrl)
            saveDownloadMetadata(context, newItem)

        } catch (e: Exception) {
            Toast.makeText(context, "حدث خطأ أثناء بدء التحميل", Toast.LENGTH_SHORT).show()
        }
    }

    fun getDownloadedItems(context: Context): List<DownloadedItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_DOWNLOADS, null) ?: return emptyList()
        val type = object : TypeToken<List<DownloadedItem>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun saveDownloadMetadata(context: Context, item: DownloadedItem) {
        val items = getDownloadedItems(context).toMutableList()
        items.removeAll { it.id == item.id }
        items.add(item)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DOWNLOADS, Gson().toJson(items)).apply()
    }

    fun removeDownload(context: Context, item: DownloadedItem) {
        val file = File(item.localPath)
        if (file.exists()) {
            file.delete()
        }
        val items = getDownloadedItems(context).toMutableList()
        items.removeAll { it.id == item.id }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DOWNLOADS, Gson().toJson(items)).apply()
    }
}
