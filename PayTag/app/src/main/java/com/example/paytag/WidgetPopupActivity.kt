package com.example.paytag

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.paytag.data.AppDatabase
import com.example.paytag.data.Expense
import com.example.paytag.databinding.WidgetAddPopupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetPopupActivity : AppCompatActivity() {

    private lateinit var binding: WidgetAddPopupBinding
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val categories = listOf("Food", "Transport", "Bill", "Entertainment", "Shopping", "Health", "Education", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WidgetAddPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(android.view.Gravity.CENTER)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.widgetCategorySpinner.adapter = adapter

        binding.widgetSaveBtn.setOnClickListener {
            val note = binding.widgetNoteEdit.text.toString().trim()
            val amountStr = binding.widgetAmountEdit.text.toString().trim()
            val category = binding.widgetCategorySpinner.selectedItem.toString()

            if (note.isEmpty()) {
                Toast.makeText(this, "Enter a note", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                db.expenseDao().insert(Expense(note = note, category = category, amount = amount))
                launch(Dispatchers.Main) {
                    Toast.makeText(this@WidgetPopupActivity, "Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        binding.widgetCancelBtn.setOnClickListener { finish() }
    }
}
