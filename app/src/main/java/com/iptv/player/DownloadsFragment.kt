package com.iptv.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iptv.player.data.model.VodStream
import java.io.File

class DownloadsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: ContentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_downloads, container, false)
        recyclerView = view.findViewById(R.id.rv_downloads)
        tvEmpty = view.findViewById(R.id.tv_empty_msg)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = GridLayoutManager(context, 3)

        // إعداد "النادل" (Adapter)
        adapter = ContentAdapter { clickedItem ->
            if (clickedItem is ContentItem.Vod) {
                playOfflineVideo(clickedItem)
            }
        }

        // ميزة الحذف: ضغطة مطولة لحذف الملف من الهاتف
        adapter.onItemLongClick = { item ->
            if (item is ContentItem.Vod) {
                deleteDownload(item)
                true
            } else false
        }

        recyclerView.adapter = adapter
        loadDownloadedFiles()
    }

    private fun loadDownloadedFiles() {
        val savedItems = OfflineManager.getDownloadedItems(requireContext())
        
        if (savedItems.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            adapter.submitList(emptyList())
        } else {
            tvEmpty.visibility = View.GONE
            // تحويل البيانات المحفوظة إلى شكل يفهمه النادل (ContentItem)
            val items = savedItems.map { 
                ContentItem.Vod(VodStream(
                    streamId = it.id.toInt(),
                    name = it.title,
                    streamIcon = it.posterUrl,
                    containerExtension = "mp4"
                ))
            }
            adapter.submitList(items)
        }
    }

    private fun playOfflineVideo(item: ContentItem.Vod) {
        val savedItems = OfflineManager.getDownloadedItems(requireContext())
        val downloadData = savedItems.find { it.id == item.stream.streamId.toString() }

        if (downloadData != null) {
            val file = File(downloadData.localPath)
            if (file.exists()) {
                // ─── 🚀 سحر التشغيل بدون إنترنت ───
                val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_STREAM_TITLE, downloadData.title)
                    putExtra(PlayerActivity.EXTRA_STREAM_URL, file.absolutePath) // نرسل مسار الملف بدلاً من الرابط
                }
                startActivity(intent)
            } else {
                Toast.makeText(context, "الملف غير موجود أو تم حذفه!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteDownload(item: ContentItem.Vod) {
        val savedItems = OfflineManager.getDownloadedItems(requireContext())
        val downloadData = savedItems.find { it.id == item.stream.streamId.toString() }
        
        if (downloadData != null) {
            OfflineManager.removeDownload(requireContext(), downloadData)
            loadDownloadedFiles() // تحديث القائمة
            Toast.makeText(context, "تم حذف الملف لتوفير المساحة 🗑️", Toast.LENGTH_SHORT).show()
        }
    }
}
