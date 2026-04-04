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
import com.iptv.player.R 
import com.iptv.player.data.model.LiveStream
import com.iptv.player.data.model.Series
import com.iptv.player.data.model.VodStream

sealed class ContentItem {
    // ✅ أضفنا هذا السطر لكي يفهم النادل معنى كلمة "باقة"
    data class Category(val id: String, val name: String) : ContentItem()
    data class Live(val stream: LiveStream) : ContentItem()
    data class Vod(val stream: VodStream) : ContentItem()
    data class SeriesItem(val series: Series) : ContentItem()
}

class ContentAdapter(
    private val onItemClick: (ContentItem) -> Unit // ✅ حددنا نوع العنصر بدقة
) : ListAdapter<ContentItem, ContentAdapter.ContentViewHolder>(ContentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content_card, parent, false)
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
                    tvMeta.text = "📁 باقة قنوات"
                    // نضع أيقونة مجلد أو ملف افتراضية للباقات
                    ivThumbnail.setImageResource(android.R.drawable.ic_menu_agenda)
                    itemView.setOnClickListener { onItemClick(item) }
                }
                is ContentItem.Live -> {
                    tvTitle.text = item.stream.name
                    tvMeta.text = "🔴 LIVE"
                    loadThumbnail(item.stream.streamIcon)
                    itemView.setOnClickListener { onItemClick(item) }
                }
                is ContentItem.Vod -> {
                    tvTitle.text = item.stream.name
                    tvMeta.text = item.stream.rating?.let { "⭐ $it" } ?: ""
                    loadThumbnail(item.stream.streamIcon)
                    itemView.setOnClickListener { onItemClick(item) }
                }
                is ContentItem.SeriesItem -> {
                    tvTitle.text = item.series.name
                    tvMeta.text = item.series.genre ?: ""
                    loadThumbnail(item.series.cover)
                    itemView.setOnClickListener { onItemClick(item) }
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
