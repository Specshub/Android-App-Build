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
import com.bumptech.glide.load.engine.DiskCacheStrategy
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

    private var fullList: List<ContentItem> = listOf()
    var onItemLongClick: ((ContentItem) -> Boolean)? = null

    fun setAllItems(list: List<ContentItem>) {
        fullList = list
        submitList(list)
    }

    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) fullList
        else fullList.filter { item ->
            when (item) {
                is ContentItem.Category -> item.name.contains(query, ignoreCase = true)
                is ContentItem.Live -> item.stream.name?.contains(query, ignoreCase = true) == true
                is ContentItem.Vod -> item.stream.name?.contains(query, ignoreCase = true) == true
                is ContentItem.SeriesItem -> item.series.name?.contains(query, ignoreCase = true) == true
            }
        }
        submitList(filteredList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_content_card, parent, false)
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
            ivThumbnail.setPadding(0, 0, 0, 0)
            
            when (item) {
                is ContentItem.Category -> {
                    tvTitle.text = item.name
                    tvMeta.text = "FOLDER"
                    // استخدام محاولة آمنة لتعيين الخلفية
                    try { tvMeta.setBackgroundResource(R.drawable.bg_tag_blue) } catch (e: Exception) {}
                    
                    val categoryImg = getSmartCategoryIcon(item.name)
                    loadThumbnail(categoryImg, isCategory = true)
                    itemView.setOnClickListener { onItemClick(item) }
                }
                is ContentItem.Live -> {
                    tvTitle.text = item.stream.name
                    tvMeta.text = "LIVE"
                    try { tvMeta.setBackgroundResource(R.drawable.bg_tag_red) } catch (e: Exception) {}
                    loadThumbnail(item.stream.streamIcon)
                    itemView.setOnClickListener { onItemClick(item) }
                    itemView.setOnLongClickListener { onItemLongClick?.invoke(item) ?: false }
                }
                is ContentItem.Vod -> {
                    tvTitle.text = item.stream.name
                    tvMeta.text = "MOVIE"
                    try { tvMeta.setBackgroundResource(R.drawable.bg_tag_purple) } catch (e: Exception) {}
                    loadThumbnail(item.stream.streamIcon)
                    itemView.setOnClickListener { onItemClick(item) }
                    itemView.setOnLongClickListener { onItemLongClick?.invoke(item) ?: false }
                }
                is ContentItem.SeriesItem -> {
                    tvTitle.text = item.series.name
                    tvMeta.text = "SERIES"
                    try { tvMeta.setBackgroundResource(R.drawable.bg_tag_orange) } catch (e: Exception) {}
                    loadThumbnail(item.series.cover)
                    itemView.setOnClickListener { onItemClick(item) }
                    itemView.setOnLongClickListener { onItemLongClick?.invoke(item) ?: false }
                }
            }
        }

        private fun loadThumbnail(url: String?, isCategory: Boolean = false) {
            Glide.with(itemView.context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.bg_category_gradient)
                .error(if (isCategory) R.drawable.ic_folder_modern else R.drawable.bg_category_gradient)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(ivThumbnail)
        }

        private fun getSmartCategoryIcon(name: String): String {
            val n = name.lowercase()
            return when {
                n.contains("bein") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/a/af/BeIN_Sports_logo.svg/512px-BeIN_Sports_logo.svg.png"
                n.contains("osn") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/OSN_logo.svg/512px-OSN_logo.svg.png"
                n.contains("netflix") -> "https://upload.wikimedia.org/wikipedia/commons/0/08/Netflix_2015_logo.svg"
                n.contains("shahid") -> "https://upload.wikimedia.org/wikipedia/ar/0/0e/Shahid_Logo.png"
                n.contains("kids") || n.contains("اطفال") -> "https://cdn-icons-png.flaticon.com/512/3050/3050031.png"
                n.contains("sport") || n.contains("رياضة") -> "https://cdn-icons-png.flaticon.com/512/857/857418.png"
                n.contains("movie") || n.contains("افلام") -> "https://cdn-icons-png.flaticon.com/512/4221/4221419.png"
                else -> ""
            }
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
