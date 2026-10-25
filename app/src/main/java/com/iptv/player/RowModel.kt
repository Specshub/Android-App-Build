package com.iptv.player
import androidx.recyclerview.widget.DiffUtil

fun nm(s: String?): String = s ?: ""
data class Row(val id: String, val title: String, val icon: String? = null)
class RowDiff : DiffUtil.ItemCallback<Row>() {
    override fun areItemsTheSame(a: Row, b: Row) = a.id == b.id
    override fun areContentsTheSame(a: Row, b: Row) = a == b
}
