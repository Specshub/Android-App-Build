// com/iptvplayer/app/ui/adapter/ContentAdapter.kt
package com.iptvplayer.app.ui.adapter

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
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.LiveStream
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.data.model.VodStream

sealed class ContentItem {
    data class Live(val stream: LiveStream) : ContentItem()
    data class Vod(val stream: VodStream) : ContentItem()
    data class SeriesItem(val series: Series) : ContentItem()
}

class ContentAdapter(
    private val onItemClick: (Any) -> Unit
) : ListAdapter<ContentItem, ContentAdapter.ContentViewHolder>(ContentDiffCallback()) {

    private var originalList: List<ContentItem> = emptyList()

    fun submitLiveStreams(streams: List<LiveStream>) {
        val items = streams.map { ContentItem.Live(it) }
        originalList = items
        submitList(items)
    }

    fun submitVodStreams(streams: List<VodStream>) {
        val items = streams.map { ContentItem.Vod(it) }
        originalList = items
        submitList(items)
    }

    fun submitSeriesList(series: List<Series>) {
        val items = series.map { ContentItem.SeriesItem(it) }
        originalList = items
        submitList(items)
    }

    fun filter(query: String) {
        if (query.isBlank()) {
            submitList(originalList)
            return
        }
        val filtered = originalList.filter { item ->
            val name = when (item) {
                is ContentItem.Live -> item.stream.name
                is ContentItem.Vod -> item.stream.name
                is ContentItem.SeriesItem -> item.series.name
            }
            name.contains(query, ignoreCase = true)
        }
        submitList(filtered)
    }

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
        private val ivPlayIcon: ImageView = itemView.findViewById(R.id.iv_play_icon)

        fun bind(item: ContentItem) {
            when (item) {
                is ContentItem.Live -> {
                    tvTitle.text = item.stream.name
                    tvMeta.text = "🔴 LIVE"
                    loadThumbnail(item.stream.streamIcon)
                    itemView.setOnClickListener { onItemClick(item.stream) }
                }
                is ContentItem.Vod -> {
                    tvTitle.text = item.stream.name
                    val rating = item.stream.rating?.let { "⭐ $it" } ?: ""
                    tvMeta.text = rating
                    loadThumbnail(item.stream.streamIcon)
                    itemView.setOnClickListener { onItemClick(item.stream) }
                }
                is ContentItem.SeriesItem -> {
                    tvTitle.text = item.series.name
                    tvMeta.text = item.series.genre ?: ""
                    loadThumbnail(item.series.cover)
                    itemView.setOnClickListener { onItemClick(item.series) }
                }
            }
        }

        private fun loadThumbnail(url: String?) {
            Glide.with(itemView.context)
                .load(url)
                .placeholder(R.drawable.placeholder_dark)
                .error(R.drawable.placeholder_dark)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(ivThumbnail)
        }
    }

    class ContentDiffCallback : DiffUtil.ItemCallback<ContentItem>() {
        override fun areItemsTheSame(old: ContentItem, new: ContentItem): Boolean {
            return when {
                old is ContentItem.Live && new is ContentItem.Live ->
                    old.stream.streamId == new.stream.streamId
                old is ContentItem.Vod && new is ContentItem.Vod ->
                    old.stream.streamId == new.stream.streamId
                old is ContentItem.SeriesItem && new is ContentItem.SeriesItem ->
                    old.series.seriesId == new.series.seriesId
                else -> false
            }
        }

        override fun areContentsTheSame(old: ContentItem, new: ContentItem) = old == new
    }
}
