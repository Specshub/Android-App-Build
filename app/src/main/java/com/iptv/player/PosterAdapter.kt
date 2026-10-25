package com.iptv.player
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

data class PosterItem(val id: String, val title: String, val imageUrl: String?, val ext: String? = null)

class PosterAdapter(private val layoutRes: Int = R.layout.item_poster, private val onClick: (PosterItem) -> Unit) : ListAdapter<PosterItem, PosterAdapter.VH>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val iv: ImageView = view.findViewById(R.id.ivPoster)
        private val tv: TextView = view.findViewById(R.id.tvPosterTitle)
        fun bind(item: PosterItem) {
            tv.text = item.title
            Glide.with(itemView.context).load(item.imageUrl).diskCacheStrategy(DiskCacheStrategy.ALL).override(240, 360).centerCrop().into(iv)
            itemView.setOnClickListener { onClick(item) }
            itemView.setOnFocusChangeListener { v, f -> val s = if (f) 1.08f else 1f; v.animate().scaleX(s).scaleY(s).setDuration(150).start() }
        }
    }
    class Diff : DiffUtil.ItemCallback<PosterItem>() {
        override fun areItemsTheSame(a: PosterItem, b: PosterItem) = a.id == b.id
        override fun areContentsTheSame(a: PosterItem, b: PosterItem) = a == b
    }
}
