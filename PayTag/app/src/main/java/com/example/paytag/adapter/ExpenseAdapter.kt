package com.example.paytag.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.paytag.R
import com.example.paytag.data.Expense
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private val onEdit: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    private val categoryColors = mapOf(
        "Food" to Color.parseColor("#FF6B6B"),
        "Transport" to Color.parseColor("#4ECDC4"),
        "Bill" to Color.parseColor("#6C3CE1"),
        "Entertainment" to Color.parseColor("#FFD93D"),
        "Shopping" to Color.parseColor("#A855F7"),
        "Health" to Color.parseColor("#00D68F"),
        "Education" to Color.parseColor("#4DABF7"),
        "Other" to Color.parseColor("#6C6C80")
    )

    private val categoryIcons = mapOf(
        "Food" to R.drawable.ic_category_food,
        "Transport" to R.drawable.ic_category_transport,
        "Bill" to R.drawable.ic_category_bill,
        "Entertainment" to R.drawable.ic_category_entertainment,
        "Shopping" to R.drawable.ic_category_shopping,
        "Health" to R.drawable.ic_category_health,
        "Education" to R.drawable.ic_category_education
    )

    private var expandedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
        animateItem(holder.itemView, position)
    }

    private fun animateItem(view: View, position: Int) {
        view.alpha = 0f
        view.translationY = 50f
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setStartDelay((position * 50).toLong())
            .setInterpolator(OvershootInterpolator(1.1f))
            .start()
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val noteText: TextView = itemView.findViewById(R.id.noteText)
        private val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        private val amountText: TextView = itemView.findViewById(R.id.amountText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        private val categoryIndicator: View = itemView.findViewById(R.id.categoryIndicator)
        private val categoryIcon: ImageView? = itemView.findViewById(R.id.categoryIcon)

        fun bind(expense: Expense) {
            noteText.text = expense.note
            categoryText.text = expense.category
            amountText.text = String.format("-Rs.%.0f", expense.amount)

            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            dateText.text = sdf.format(Date(expense.timestamp))

            val color = categoryColors[expense.category] ?: Color.parseColor("#6C6C80")
            categoryIndicator.setBackgroundColor(color)

            val bg = categoryText.background as? GradientDrawable
            bg?.setStroke(2, color)
            bg?.setColor(Color.parseColor("#1A1A2E"))
            categoryText.setTextColor(color)

            categoryIcon?.let { icon ->
                val iconRes = categoryIcons[expense.category]
                if (iconRes != null) {
                    icon.setImageResource(iconRes)
                    icon.visibility = View.VISIBLE
                    icon.setColorFilter(color)
                } else {
                    icon.visibility = View.GONE
                }
            }

            val isExpanded = bindingAdapterPosition == expandedPosition
            editButton.visibility = if (isExpanded) View.VISIBLE else View.GONE
            deleteButton.visibility = if (isExpanded) View.VISIBLE else View.GONE

            if (isExpanded) {
                editButton.alpha = 0f
                editButton.translationX = 20f
                editButton.animate().alpha(1f).translationX(0f).setDuration(200).start()
                deleteButton.alpha = 0f
                deleteButton.translationX = 20f
                deleteButton.animate().alpha(1f).translationX(0f).setDuration(200).setStartDelay(50).start()
            }

            itemView.setOnLongClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnLongClickListener true
                val old = expandedPosition
                expandedPosition = if (expandedPosition == pos) -1 else pos
                if (old >= 0) notifyItemChanged(old)
                notifyItemChanged(pos)
                true
            }

            editButton.setOnClickListener { onEdit(expense) }
            deleteButton.setOnClickListener { onDelete(expense) }
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Expense, newItem: Expense) = oldItem == newItem
    }
}
