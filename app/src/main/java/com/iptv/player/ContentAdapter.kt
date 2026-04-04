package com.iptv.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.iptv.player.data.model.LiveStream
import com.iptv.player.data.model.Series
import com.iptv.player.data.model.VodStream

sealed class ContentItem {
    data class Category(val id: String, val name: String) : ContentItem()
    data class Live(val stream: LiveStream) : ContentItem()
    data class Vod(val stream: VodStream) : ContentItem()
    data class SeriesItem(val series: Series) : ContentItem()
}

class ContentAdapter(
    private val onItemClick: (ContentItem) -> Unit 
) : ListAdapter<ContentItem, ContentAdapter.ContentViewHolder>(ContentDiffCallback()) {

    // 🔍 القائمة الكاملة التي سنستخدمها كمرجع عند البحث لكي لا نفقد البيانات الأصلية
    private var fullList: List<ContentItem> = listOf()

    var onItemLongClick: ((ContentItem) -> Boolean)? = null

    // ✅ دالة جديدة لتحديث البيانات وحفظ نسخة منها للبحث
    fun setAllItems(list: List<ContentItem>) {
        fullList = list
        submitList(list)
    }

    // ✅ دالة البحث السحرية: تقوم بتصفية القائمة بناءً على النص المكتوب
    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullList // إذا كان البحث فارغاً، نعرض كل شيء
        } else {
            fullList.filter { item ->
                when (item) {
                    is ContentItem.Category -> item.name.contains(query, ignoreCase = true)
                    is ContentItem.Live -> item.stream.name?.contains(query, ignoreCase = true) == true
                    is ContentItem.Vod -> item.stream.name?.contains(query, ignoreCase = true) == true
                    is ContentItem.SeriesItem -> item.series.name?.contains(query, ignoreCase = true) == true
                }
            }
        }
        submitList(filteredList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.id.item_content_card, parent, false) // تأكد من وجود تخطيط الكارد
        return ContentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_content_title)
        private val tvMeta: TextView = itemView.findViewById(R.id.tv_content_meta)

        fun bind(item: ContentItem) {
            when (item) {
                is ContentItem.Category -> {
                    tvTitle.text = item.name
                    tvMeta.text = "📁 باقة"
                    ivThumbnail.setImageResource(android.R.drawable.ic_menu_agenda)
                    itemView.setOnClickListener { onItemClick(item) }
                    itemView.setOnLongClickListener { onItemLongClick?.invoke(item) ?: false }
                }
                is ContentItem.Live -> {
                    tvTitle.text = item.stream.name
                    tvMeta.text = "🔴 LIVE"
                    loadThumbnail(item.stream.streamIcon)
                    itemView.setOnClickListener { onItemClick(item) }
                    itemView.setOnLongClickListener { onItemLongClick?.invoke(item) ?: false }
                }
                is ContentItem.Vod -> {
                    tvTitle.text = item.stream.name
                    tvMeta.text = item.stream.rating?.let { "⭐ $it" } ?: ""
                    loadThumbnail(item.stream.streamIcon)
                    itemView.setOnClickListener { onItemClick(item) }
                    itemView.setOnLongClickListener { onItemLongClick?.invoke(item) ?: false }
                }
                is ContentItem.SeriesItem -> {
                    tvTitle.text = item.series.name
                    tvMeta.text = item.series.genre ?: ""
                    loadThumbnail(item.series.cover)
                    itemView.setOnClickListener { onItemClick(item) }
                    itemView.setOnLongClickListener { onItemLongClick?.invoke(item) ?: false }
                }
            }
        }

        private fun loadThumbnail(url: String?) {
            Glide.with(itemView.context)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(ivThumbnail)
        }
    }

    class ContentDiffCallback : DiffUtil.ItemCallback<ContentItem>() {
        override fun areItemsTheSame(old: ContentItem, new: ContentItem) = when {
            old is ContentItem.Category && new is ContentItem.Category -> old.id == new.id
            old is ContentItem.Live && new is ContentItem.Live -> old.stream.streamId == new.stream.streamId
            old is ContentItem.Vod && new is ContentItem.Vod -> old.stream.streamId == new.stream.streamId
            old is ContentItem.SeriesItem && new is ContentItem.SeriesItem -> old.series.seriesId == new.series.seriesId
            else -> false
        }
        override fun areContentsTheSame(old: ContentItem, new: ContentItem) = old == new
    }
}
