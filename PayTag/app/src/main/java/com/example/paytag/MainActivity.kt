package com.example.paytag

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paytag.adapter.CategoryGroupAdapter
import com.example.paytag.adapter.ExpenseAdapter
import com.example.paytag.data.AppDatabase
import com.example.paytag.data.CategoryTotal
import com.example.paytag.data.Expense
import com.example.paytag.data.GroupRepository
import com.example.paytag.databinding.ActivityMainBinding
import com.example.paytag.util.CsvHelper
import com.example.paytag.util.PdfHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val groupRepo = GroupRepository()
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryGroupAdapter: CategoryGroupAdapter

    private val categories = mutableListOf(
        "Food", "Transport", "Bill", "Entertainment",
        "Shopping", "Health", "Education", "Other"
    )

    private var currentMonthOffset = 0
    private var currentMonthExpenses = listOf<Expense>()
    private var currentCatTotals = listOf<CategoryTotal>()
    private var currentMonthTotal = 0.0
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        groupRepo.init(this)
        applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        updateToolbarSubtitle()

        setupRecyclerViews()
        setupFab()
        setupMonthNavigation()
        setupViewToggle()
        setupSearch()
        setupChartsButton()
        observeData()
    }

    private fun applyTheme() {
        val mode = groupRepo.getThemeMode()
        when (mode) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareReport()
                true
            }
            R.id.action_calendar -> {
                startActivity(Intent(this, CalendarActivity::class.java))
                true
            }
            R.id.action_group -> {
                showGroupInfo()
                true
            }
            R.id.action_theme -> {
                toggleTheme()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleTheme() {
        val current = groupRepo.getThemeMode()
        val newMode = if (current == "dark") "light" else "dark"
        groupRepo.setThemeMode(newMode)
        AppCompatDelegate.setDefaultNightMode(
            if (newMode == "light") AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        )
        recreate()
    }

    private fun updateToolbarSubtitle() {
        val group = groupRepo.getGroup()
        if (group != null) {
            binding.toolbar.subtitle = "Group: ${group.memberNames.joinToString(" & ")}"
        } else {
            binding.toolbar.subtitle = "Track your spending"
        }
    }

    private fun showGroupInfo() {
        val group = groupRepo.getGroup()
        if (group != null) {
            AlertDialog.Builder(this)
                .setTitle("Your Group")
                .setMessage(
                    "Group Code: ${group.code}\n\n" +
                    "Members:\n${group.memberNames.joinToString("\n")}\n\n" +
                    "Share this code with your partner to let them join."
                )
                .setPositiveButton("OK", null)
                .setNeutralButton("Leave Group") { _, _ ->
                    leaveGroup()
                }
                .show()
        } else {
            startActivity(Intent(this, GroupActivity::class.java))
        }
    }

    private fun leaveGroup() {
        AlertDialog.Builder(this)
            .setTitle("Leave Group?")
            .setMessage("Your local data will remain.")
            .setPositiveButton("Leave") { _, _ ->
                groupRepo.leaveGroup()
                Toast.makeText(this, "Left group", Toast.LENGTH_SHORT).show()
                updateToolbarSubtitle()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out?")
            .setPositiveButton("Sign Out") { _, _ ->
                groupRepo.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupRecyclerViews() {
        expenseAdapter = ExpenseAdapter(
            onEdit = { showEditDialog(it) },
            onDelete = { showDeleteDialog(it) }
        )
        binding.expenseRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = expenseAdapter
        }

        categoryGroupAdapter = CategoryGroupAdapter(
            onEdit = { showEditDialog(it) },
            onDelete = { showDeleteDialog(it) }
        )
        binding.categoryWiseRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = categoryGroupAdapter
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val scaleX = ObjectAnimator.ofFloat(it, "scaleX", 1f, 0.9f, 1f)
            val scaleY = ObjectAnimator.ofFloat(it, "scaleY", 1f, 0.9f, 1f)
            val rotation = ObjectAnimator.ofFloat(it, "rotation", 0f, -15f, 0f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY, rotation)
                duration = 400
                interpolator = OvershootInterpolator(2f)
                start()
            }
            showAddDialog()
        }
    }

    private fun setupMonthNavigation() {
        updateMonthTitle()
        binding.prevMonth.setOnClickListener {
            currentMonthOffset--
            updateMonthTitle()
            observeData()
        }
        binding.nextMonth.setOnClickListener {
            currentMonthOffset++
            updateMonthTitle()
            observeData()
        }
    }

    private fun updateMonthTitle() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, currentMonthOffset)
        binding.monthTitle.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    private fun setupViewToggle() {
        binding.btnRecentView.setOnClickListener {
            binding.recentSection.visibility = android.view.View.VISIBLE
            binding.categorySection.visibility = android.view.View.GONE
            binding.btnRecentView.setTextColor(getColor(R.color.white))
            binding.btnRecentView.setBackgroundResource(R.drawable.card_gradient)
            binding.btnCategoryView.setTextColor(getColor(R.color.teal_700))
            binding.btnCategoryView.background = null
        }
        binding.btnCategoryView.setOnClickListener {
            binding.recentSection.visibility = android.view.View.GONE
            binding.categorySection.visibility = android.view.View.VISIBLE
            binding.btnCategoryView.setTextColor(getColor(R.color.white))
            binding.btnCategoryView.setBackgroundResource(R.drawable.card_gradient)
            binding.btnRecentView.setTextColor(getColor(R.color.teal_700))
            binding.btnRecentView.background = null
        }
    }

    private fun setupSearch() {
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim()
                observeData()
            }
        })
    }

    private fun setupChartsButton() {
        binding.btnCharts.setOnClickListener {
            startActivity(Intent(this, ChartsActivity::class.java))
        }
    }

    private fun getMonthRange(): Pair<Long, Long> {
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
        return start to end
    }

    private fun observeData() {
        val (start, end) = getMonthRange()

        lifecycleScope.launch {
            val flow = if (searchQuery.isNotEmpty()) {
                db.expenseDao().searchExpenses(start, end, searchQuery)
            } else {
                db.expenseDao().getExpensesBetween(start, end)
            }
            flow.collect { list ->
                currentMonthExpenses = list
                expenseAdapter.submitList(list)
                categoryGroupAdapter.submitList(list)
            }
        }

        lifecycleScope.launch {
            db.expenseDao().getTotalSpent(start, end).collect { total ->
                val t = total ?: 0.0
                binding.totalAmountText.text = String.format("Rs.%.2f", t)
                currentMonthTotal = t
            }
        }

        lifecycleScope.launch {
            db.expenseDao().getCategoryTotals(start, end).collect { list ->
                currentCatTotals = list
            }
        }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.tag_dialog, null)
        val noteEdit = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.noteEdit)
        val amountEdit = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.amountEdit)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val customTagButton = dialogView.findViewById<Button>(R.id.customTagButton)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        customTagButton.setOnClickListener {
            val input = EditText(this)
            input.hint = "Enter new category"
            AlertDialog.Builder(this)
                .setTitle("Add Custom Category")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val newCat = input.text.toString().trim()
                    if (newCat.isNotEmpty() && !categories.contains(newCat)) {
                        categories.add(categories.size - 1, newCat)
                        val newAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
                        newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        categorySpinner.adapter = newAdapter
                        categorySpinner.setSelection(categories.indexOf(newCat))
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val note = noteEdit.text.toString().trim()
                val amountStr = amountEdit.text.toString().trim()
                val category = categorySpinner.selectedItem.toString()
                if (note.isEmpty()) {
                    Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    db.expenseDao().insert(Expense(note = note, category = category, amount = amount))
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.window?.setWindowAnimations(R.style.DialogAnimation)
    }

    private fun showEditDialog(expense: Expense) {
        val dialogView = layoutInflater.inflate(R.layout.tag_dialog, null)
        val noteEdit = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.noteEdit)
        val amountEdit = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.amountEdit)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val customTagButton = dialogView.findViewById<Button>(R.id.customTagButton)

        noteEdit.setText(expense.note)
        amountEdit.setText(expense.amount.toLong().toString())

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        val catIndex = categories.indexOf(expense.category)
        if (catIndex >= 0) categorySpinner.setSelection(catIndex)

        customTagButton.setOnClickListener {
            val input = EditText(this)
            input.hint = "Enter new category"
            AlertDialog.Builder(this)
                .setTitle("Add Custom Category")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val newCat = input.text.toString().trim()
                    if (newCat.isNotEmpty() && !categories.contains(newCat)) {
                        categories.add(categories.size - 1, newCat)
                        val newAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
                        newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        categorySpinner.adapter = newAdapter
                        categorySpinner.setSelection(categories.indexOf(newCat))
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Expense")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val note = noteEdit.text.toString().trim()
                val amountStr = amountEdit.text.toString().trim()
                val category = categorySpinner.selectedItem.toString()
                if (note.isEmpty()) {
                    Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    db.expenseDao().update(expense.copy(note = note, category = category, amount = amount))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Delete \"${expense.note}\" (Rs.${expense.amount})?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch { db.expenseDao().delete(expense) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareReport() {
        if (currentMonthExpenses.isEmpty()) {
            Toast.makeText(this, "No expenses to share", Toast.LENGTH_SHORT).show()
            return
        }
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, currentMonthOffset) }
        val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

        lifecycleScope.launch {
            try {
                val file = PdfHelper.generateReport(this@MainActivity, currentMonthExpenses, currentCatTotals, currentMonthTotal, monthLabel)
                val uri = FileProvider.getUriForFile(this@MainActivity, "${packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "PayTag Report - $monthLabel")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Report via"))
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun exportCsv() {
        if (currentMonthExpenses.isEmpty()) {
            Toast.makeText(this, "No expenses to export", Toast.LENGTH_SHORT).show()
            return
        }
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, currentMonthOffset) }
        val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

        lifecycleScope.launch {
            try {
                val file = CsvHelper.generateReport(this@MainActivity, currentMonthExpenses, currentCatTotals, currentMonthTotal, monthLabel)
                val uri = FileProvider.getUriForFile(this@MainActivity, "${packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "PayTag Report - $monthLabel")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Export CSV via"))
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
