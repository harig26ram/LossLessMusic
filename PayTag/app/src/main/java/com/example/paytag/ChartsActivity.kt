package com.example.paytag

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paytag.adapter.CategoryAdapter
import com.example.paytag.data.AppDatabase
import com.example.paytag.data.CategoryTotal
import com.example.paytag.data.Expense
import com.example.paytag.databinding.ActivityChartsBinding
import kotlinx.coroutines.launch
import java.util.*

class ChartsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChartsBinding
    private val db by lazy { AppDatabase.getDatabase(this) }
    private lateinit var categoryAdapter: CategoryAdapter
    private var currentMonthOffset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_up, 0)
        binding = ActivityChartsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarCharts)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Charts & Analytics"
        binding.toolbarCharts.setNavigationOnClickListener { finish() }

        categoryAdapter = CategoryAdapter()
        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChartsActivity)
            adapter = categoryAdapter
        }

        binding.prevMonth.setOnClickListener {
            currentMonthOffset--
            updateMonthTitle()
            loadData()
        }
        binding.nextMonth.setOnClickListener {
            currentMonthOffset++
            updateMonthTitle()
            loadData()
        }

        updateMonthTitle()
        loadData()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_up)
    }

    private fun updateMonthTitle() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, currentMonthOffset)
        binding.monthTitle.text = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(cal.time)
    }

    private fun loadData() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, currentMonthOffset)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis

        lifecycleScope.launch {
            db.expenseDao().getExpensesBetween(start, end).collect { list ->
                val dailySpend = list.groupBy {
                    Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.DAY_OF_MONTH)
                }.mapValues { entry -> entry.value.sumOf { it.amount } }
                binding.barChart.setData(dailySpend)
            }
        }

        lifecycleScope.launch {
            db.expenseDao().getCategoryTotals(start, end).collect { list ->
                binding.horizontalBarChart.setData(list)
                val total = list.sumOf { it.total }
                categoryAdapter.submitList(list, total)
                binding.pieChart.setData(list)
            }
        }
    }
}
