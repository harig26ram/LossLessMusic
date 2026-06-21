package com.example.paytag.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.paytag.R
import com.example.paytag.data.Expense

class CategoryGroupAdapter(
    private val onEdit: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : RecyclerView.Adapter<CategoryGroupAdapter.CategoryGroupViewHolder>() {

    private var groups = listOf<CategoryGroup>()

    data class CategoryGroup(
        val category: String,
        val total: Double,
        val expenses: List<Expense>
    )

    private val categoryColors = mapOf(
        "Food" to Color.parseColor("#EF4444"),
        "Transport" to Color.parseColor("#14B8A6"),
        "Bill" to Color.parseColor("#6366F1"),
        "Entertainment" to Color.parseColor("#F59E0B"),
        "Shopping" to Color.parseColor("#8B5CF6"),
        "Health" to Color.parseColor("#10B981"),
        "Education" to Color.parseColor("#0EA5E9"),
        "Other" to Color.parseColor("#64748B")
    )

    fun submitList(expenses: List<Expense>) {
        groups = expenses
            .groupBy { it.category }
            .map { (cat, list) ->
                CategoryGroup(
                    category = cat,
                    total = list.sumOf { it.amount },
                    expenses = list.sortedByDescending { it.timestamp }
                )
            }
            .sortedByDescending { it.total }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryGroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_group, parent, false)
        return CategoryGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryGroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount() = groups.size

    inner class CategoryGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryDot: View = itemView.findViewById(R.id.categoryDot)
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        private val categoryTotal: TextView = itemView.findViewById(R.id.categoryTotal)
        private val expensesRecycler: RecyclerView = itemView.findViewById(R.id.expensesInCategory)

        fun bind(group: CategoryGroup) {
            val color = categoryColors[group.category] ?: Color.parseColor("#64748B")
            categoryDot.setBackgroundColor(color)
            categoryName.text = group.category
            categoryName.setTextColor(color)
            categoryTotal.text = String.format("Rs.%.0f (%d items)", group.total, group.expenses.size)

            val expenseAdapter = ExpenseAdapter(onEdit, onDelete)
            expensesRecycler.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = expenseAdapter
            }
            expenseAdapter.submitList(group.expenses)
        }
    }
}
