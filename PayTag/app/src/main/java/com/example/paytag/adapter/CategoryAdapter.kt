package com.example.paytag.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.paytag.R
import com.example.paytag.data.CategoryTotal

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var items = listOf<CategoryTotal>()
    private var totalAmount = 0.0

    private val colors = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#6C3CE1"),
        Color.parseColor("#FFD93D"),
        Color.parseColor("#A855F7"),
        Color.parseColor("#00D68F"),
        Color.parseColor("#4DABF7"),
        Color.parseColor("#6C6C80"),
        Color.parseColor("#FFB547"),
        Color.parseColor("#E94E7A")
    )

    fun submitList(list: List<CategoryTotal>, total: Double) {
        items = list
        totalAmount = total
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(items[position], totalAmount, colors[position % colors.size])
    }

    override fun getItemCount() = items.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        private val categoryAmount: TextView = itemView.findViewById(R.id.categoryAmount)
        private val categoryPercent: TextView = itemView.findViewById(R.id.categoryPercent)

        fun bind(cat: CategoryTotal, total: Double, color: Int) {
            categoryName.text = cat.category
            categoryName.setTextColor(color)
            categoryAmount.text = String.format("Rs.%.0f", cat.total)
            val percent = if (total > 0) (cat.total / total * 100) else 0.0
            categoryPercent.text = String.format("%.1f%%", percent)
        }
    }
}
