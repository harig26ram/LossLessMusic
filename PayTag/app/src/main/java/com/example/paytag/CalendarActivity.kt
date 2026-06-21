package com.example.paytag

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.paytag.data.AppDatabase
import com.example.paytag.databinding.ActivityCalendarBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private val db by lazy { AppDatabase.getDatabase(this) }
    private var currentCal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarCal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Monthly View"
        binding.toolbarCal.setNavigationOnClickListener { finish() }

        binding.prevMonth.setOnClickListener {
            currentCal.add(Calendar.MONTH, -1)
            loadCalendar()
        }
        binding.nextMonth.setOnClickListener {
            currentCal.add(Calendar.MONTH, 1)
            loadCalendar()
        }

        loadCalendar()
    }

    private fun loadCalendar() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.monthTitle.text = sdf.format(currentCal.time)

        val cal = currentCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val monthStart = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis

        val daysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = (currentCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }.get(Calendar.DAY_OF_WEEK).let { dow ->
            (dow + 5) % 7
        }

        lifecycleScope.launch {
            val expenses = db.expenseDao().getExpensesBetween(monthStart, monthEnd).first()

            val dailySpend = mutableMapOf<Int, Double>()
            val sdfDay = SimpleDateFormat("dd", Locale.getDefault())
            expenses.forEach { expense ->
                val day = sdfDay.format(Date(expense.timestamp)).toIntOrNull()
                if (day != null) {
                    dailySpend[day] = (dailySpend[day] ?: 0.0) + expense.amount
                }
            }

            val totalMonth = expenses.sumOf { it.amount }
            binding.monthTotal.text = String.format("Total: Rs.%.0f", totalMonth)

            val grid = binding.calendarGrid
            grid.removeAllViews()
            grid.columnCount = 7

            val dayHeaders = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            dayHeaders.forEach { day ->
                val tv = TextView(this@CalendarActivity).apply {
                    text = day
                    textSize = 12f
                    setTextColor(Color.parseColor("#9B6AFF"))
                    gravity = Gravity.CENTER
                    setPadding(8, 16, 8, 8)
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                grid.addView(tv, params)
            }

            repeat(firstDayOfWeek) {
                val empty = View(this@CalendarActivity)
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = dpToPx(60)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                grid.addView(empty, params)
            }

            val today = Calendar.getInstance()
            val isCurrentMonth = currentCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    currentCal.get(Calendar.MONTH) == today.get(Calendar.MONTH)

            for (day in 1..daysInMonth) {
                val dayView = createDayView(day, dailySpend[day], isCurrentMonth && day == today.get(Calendar.DAY_OF_MONTH))
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = dpToPx(64)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(1, 1, 1, 1)
                }
                grid.addView(dayView, params)
            }
        }
    }

    private fun createDayView(day: Int, amount: Double?, isToday: Boolean): View {
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(4, 8, 4, 4)
            if (isToday) {
                setBackgroundColor(Color.parseColor("#1A1A2E"))
            } else {
                setBackgroundColor(Color.parseColor("#141420"))
            }
        }

        val dayText = TextView(this).apply {
            text = day.toString()
            textSize = 14f
            gravity = Gravity.CENTER
            if (isToday) {
                setTextColor(Color.parseColor("#6C3CE1"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            } else {
                setTextColor(Color.parseColor("#FFFFFF"))
            }
        }
        container.addView(dayText)

        if (amount != null && amount > 0) {
            val amountText = TextView(this).apply {
                text = String.format("Rs.%.0f", amount)
                textSize = 8f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#FF4D6A"))
                maxLines = 1
            }
            container.addView(amountText)
        }

        return container
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
