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

data class PosterItem(val id: String, val title: String, val imageUrl: String?)

class PosterAdapter(
    private val onClick: (PosterItem) -> Unit
) : ListAdapter<PosterItem, PosterAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_poster, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val iv: ImageView = view.findViewById(R.id.ivPoster)
        private val tv: TextView = view.findViewById(R.id.tvPosterTitle)

        fun bind(item: PosterItem) {
            tv.text = item.title
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(iv)
