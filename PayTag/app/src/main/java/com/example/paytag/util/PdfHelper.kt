package com.example.paytag.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.paytag.data.CategoryTotal
import com.example.paytag.data.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfHelper {

    fun generateReport(
        context: Context,
        expenses: List<Expense>,
        categoryTotals: List<CategoryTotal>,
        monthTotal: Double,
        monthLabel: String
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#6C3CE1")
            textSize = 24f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#1A1A2E")
            textSize = 14f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#4A4A5C")
            textSize = 12f
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 1f
        }

        var y = 50f
        canvas.drawText("PayTag - Expense Report", 50f, y, titlePaint)
        y += 25f
        canvas.drawText(monthLabel, 50f, y, bodyPaint)
        y += 15f
        val dateStr = SimpleDateFormat("'Generated:' dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText(dateStr, 50f, y, bodyPaint)
        y += 25f

        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 30f

        canvas.drawText("Category-wise Summary", 50f, y, headerPaint)
        y += 25f

        val colors = intArrayOf(
            Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4"),
            Color.parseColor("#6C3CE1"), Color.parseColor("#FFD93D"),
            Color.parseColor("#A855F7"), Color.parseColor("#00D68F"),
            Color.parseColor("#4DABF7"), Color.parseColor("#6C6C80")
        )

        categoryTotals.forEachIndexed { index, cat ->
            val percent = if (monthTotal > 0) (cat.total / monthTotal * 100) else 0.0
            val barWidth = (percent / 100 * 300).toFloat()

            canvas.drawRect(50f, y - 10f, 60f, y + 4f, Paint().apply {
                color = colors[index % colors.size]
            })
            canvas.drawText(
                "${cat.category}: Rs.${String.format("%.0f", cat.total)} (${String.format("%.1f", percent)}%)",
                70f, y, bodyPaint
            )

            if (barWidth > 0) {
                canvas.drawRect(50f, y + 8f, 50f + barWidth, y + 14f, Paint().apply {
                    color = colors[index % colors.size]
                    alpha = 100
                })
            }
            y += 30f
        }

        y += 5f
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 25f

        val totalPaint = Paint().apply {
            color = Color.parseColor("#6C3CE1")
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText("Total Spent: Rs.${String.format("%.2f", monthTotal)}", 50f, y, totalPaint)
        y += 35f

        canvas.drawText("Transactions (${expenses.size} total)", 50f, y, headerPaint)
        y += 20f

        val colWidths = floatArrayOf(160f, 90f, 90f, 150f)
        val colHeaders = arrayOf("Note", "Category", "Amount", "Date")
        var x = 50f
        colHeaders.forEachIndexed { i, header ->
            canvas.drawText(header, x, y, headerPaint)
            x += colWidths[i]
        }
        y += 5f
        canvas.drawLine(50f, y, 545f, y, linePaint)
        y += 18f

        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        expenses.take(28).forEach { expense ->
            if (y > 790f) return@forEach
            x = 50f
            val cols = arrayOf(
                expense.note.take(20),
                expense.category,
                String.format("Rs.%.0f", expense.amount),
                sdf.format(Date(expense.timestamp))
            )
            cols.forEachIndexed { i, text ->
                canvas.drawText(text, x, y, bodyPaint)
                x += colWidths[i]
            }
            y += 18f
        }

        if (expenses.size > 28) {
            y += 5f
            canvas.drawText("... and ${expenses.size - 28} more transactions", 50f, y, bodyPaint)
        }

        document.finishPage(page)

        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "PayTag"
        )
        dir.mkdirs()
        val file = File(dir, "PayTag_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}
