package com.iptv.player // ✅ تم تصحيح اسم الحزمة

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iptv.player.R // تأكد من المسار الصحيح للـ R

// ✅ تعريف الفئة مباشرة لحل المشكلة
data class GenericCategory(
    val categoryId: String,
    val categoryName: String
)

class CategoryAdapter(
    private val onCategoryClick: (GenericCategory) -> Unit
) : ListAdapter<GenericCategory, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: String? = null

    fun clearSelection() {
        selectedCategoryId = null
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_chip, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category_name)

        fun bind(category: GenericCategory) {
            tvCategory.text = category.categoryName
            val isSelected = category.categoryId == selectedCategoryId

            if (isSelected) {
                tvCategory.setBackgroundResource(R.drawable.bg_chip_selected)
                tvCategory.setTextColor(ContextCompat.getColor(itemView.context, R.color.bg_primary))
            } else {
                tvCategory.setBackgroundResource(R.drawable.bg_chip_unselected)
                tvCategory.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            }

            itemView.setOnClickListener {
                selectedCategoryId = category.categoryId
                notifyDataSetChanged()
                onCategoryClick(category)
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<GenericCategory>() {
        override fun areItemsTheSame(old: GenericCategory, new: GenericCategory) =
            old.categoryId == new.categoryId
        override fun areContentsTheSame(old: GenericCategory, new: GenericCategory) =
            old == new
    }
}
