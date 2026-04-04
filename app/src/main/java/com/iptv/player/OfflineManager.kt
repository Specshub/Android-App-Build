package com.iptv.player

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

// كلاس صغير لحفظ معلومات الفيلم المحمل
data class DownloadedItem(val id: String, val title: String, val localPath: String, val posterUrl: String?)

object OfflineManager {
    private const val PREFS_NAME = "downloads_prefs"
    private const val KEY_DOWNLOADS = "saved_downloads"

    // دالة بدء التحميل
    fun startDownload(context: Context, url: String, title: String, id: String, posterUrl: String?) {
        // تنظيف اسم الملف من الرموز الممنوعة
        val cleanTitle = title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val fileName = "${cleanTitle}_$id.mp4"
        
        // مسار الحفظ داخل الهاتف (مجلد الأفلام الخاص بالتطبيق)
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), fileName)

        if (file.exists()) {
            Toast.makeText(context, "هذا الفيلم محمل مسبقاً! ⬇️", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // توجيه أمر التحميل لنظام الأندرويد
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setDescription("جاري تحميل الفيلم للمشاهدة بدون إنترنت...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MOVIES, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(context, "بدأ التحميل... يمكنك متابعة التقدم في إشعارات الهاتف ⬇️", Toast.LENGTH_LONG).show()

            // حفظ بيانات الفيلم في الذاكرة لكي نعرضه لاحقاً في شاشة التحميلات
            val newItem = DownloadedItem(id, title, file.absolutePath, posterUrl)
            saveDownloadMetadata(context, newItem)

        } catch (e: Exception) {
            Toast.makeText(context, "حدث خطأ أثناء بدء التحميل", Toast.LENGTH_SHORT).show()
        }
    }

    // جلب قائمة الأفلام المحملة
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

    // حذف الفيلم من الهاتف لتفريغ المساحة
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
