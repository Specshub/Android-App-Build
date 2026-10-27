package com.iptv.player
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RowAdapter(private val numbered: Boolean, private val layoutRes: Int = R.layout.item_row, private val onClick: (Row, Int) -> Unit) : ListAdapter<Row, RowAdapter.VH>(RowDiff()) {
    var onLong: ((Row) -> Boolean)? = null
    var selectedId: String? = null
    fun select(id: String?) { val o = currentList.indexOfFirst { it.id == selectedId }; selectedId = id; val n = currentList.indexOfFirst { it.id == id }; if (o >= 0) notifyItemChanged(o); if (n >= 0) notifyItemChanged(n) }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position), position)
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val num: TextView = v.findViewById(R.id.tvNum)
        private val icon: ImageView = v.findViewById(R.id.ivIcon)
        private val title: TextView = v.findViewById(R.id.tvTitle)
        fun bind(r: Row, pos: Int) {
            title.text = r.title; num.visibility = if (numbered) View.VISIBLE else View.GONE; num.text = String.format(java.util.Locale.US, "%02d", pos + 1)
            icon.visibility = if (r.icon.isNullOrEmpty()) View.GONE else View.VISIBLE
            if (!r.icon.isNullOrEmpty()) Glide.with(itemView.context).load(r.icon).into(icon)
            itemView.isSelected = r.id == selectedId; itemView.setOnClickListener { onClick(r, bindingAdapterPosition) }; itemView.setOnLongClickListener { onLong?.invoke(r) ?: false }
        }
    }
}
