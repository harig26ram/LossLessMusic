package com.example.paytag.util

import android.content.Context
import android.os.Environment
import com.example.paytag.data.CategoryTotal
import com.example.paytag.data.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object CsvHelper {

    fun generateReport(
        context: Context,
        expenses: List<Expense>,
        categoryTotals: List<CategoryTotal>,
        monthTotal: Double,
        monthLabel: String
    ): File {
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "PayTag"
        )
        dir.mkdirs()

        val file = File(dir, "PayTag_Report_${System.currentTimeMillis()}.csv")
        val sb = StringBuilder()

        sb.appendLine("PayTag - Expense Report")
        sb.appendLine("Month,$monthLabel")
        sb.appendLine("Generated,${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}")
        sb.appendLine()

        sb.appendLine("Category-wise Summary")
        sb.appendLine("Category,Amount (Rs.),Percentage")
        categoryTotals.forEach { cat ->
            val percent = if (monthTotal > 0) (cat.total / monthTotal * 100) else 0.0
            sb.appendLine("${cat.category},${String.format("%.2f", cat.total)},${String.format("%.1f%%", percent)}")
        }
        sb.appendLine("Total,${String.format("%.2f", monthTotal)},100.0%")
        sb.appendLine()

        sb.appendLine("Transactions")
        sb.appendLine("Note,Category,Amount (Rs.),Date")
        val sdf = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault())
        expenses.forEach { expense ->
            val note = expense.note.replace(",", ";")
            sb.appendLine("$note,${expense.category},${String.format("%.2f", expense.amount)},${sdf.format(Date(expense.timestamp))}")
        }

        FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }
        return file
    }
}
