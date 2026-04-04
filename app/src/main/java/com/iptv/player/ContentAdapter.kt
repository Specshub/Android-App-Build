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

    // ✅ أعدنا هذا السطر المفقود (هو سبب المشكلة في الـ Build)
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
            
            // ✅ ربط الأزرار والضغط المطول
            itemView.setOnClickListener { onItemClick(item) }
            itemView.setOnLongClickListener { onItemLongClick?.invoke(item) ?: false }

            when (item) {
                is ContentItem.Category -> {
                    tvTitle.text = item.name
                    tvMeta.text = "FOLDER"
                    val categoryImg = getSmartCategoryIcon(item.name)
                    loadThumbnail(categoryImg, isCategory = true)
                }
                is ContentItem.Live -> {
                    tvTitle.text = item.stream.name
                    tvMeta.text = "LIVE"
                    loadThumbnail(item.stream.streamIcon)
                }
                is ContentItem.Vod -> {
                    tvTitle.text = item.stream.name
                    tvMeta.text = "MOVIE"
                    loadThumbnail(item.stream.streamIcon)
                }
                is ContentItem.SeriesItem -> {
                    tvTitle.text = item.series.name
                    tvMeta.text = "SERIES"
                    loadThumbnail(item.series.cover)
                }
