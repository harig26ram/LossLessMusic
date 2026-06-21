package com.example.paytag.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.paytag.data.CategoryTotal

class HorizontalBarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var data = listOf<CategoryTotal>()
    private var maxAmount = 0.0

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A2E")
    }
    private val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        textSize = 22f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6C3CE1")
        textSize = 20f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.RIGHT
    }

    private val barColors = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#6C3CE1"),
        Color.parseColor("#FFD93D"),
        Color.parseColor("#A855F7"),
        Color.parseColor("#00D68F"),
        Color.parseColor("#4DABF7"),
        Color.parseColor("#6C6C80")
    )

    fun setData(newData: List<CategoryTotal>) {
        data = newData
        maxAmount = newData.maxOfOrNull { it.total } ?: 0.0
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.parseColor("#141420"))

        if (data.isEmpty()) {
            val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#6C6C80")
                textSize = 22f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("No data", width.toFloat() / 2f, height.toFloat() / 2f, emptyPaint)
            return
        }

        val w = width.toFloat()
        val h = height.toFloat()
        val rowCount = data.size.coerceAtLeast(1)
        val rowHeight = h / rowCount.toFloat()
        val barMaxWidth = w * 0.55f
        val leftPad = 130f
        val barH = rowHeight * 0.4f

        for (i in data.indices) {
            val item = data[i]
            val yCenter = i.toFloat() * rowHeight + rowHeight / 2f
            val color = barColors[i % barColors.size]

            namePaint.textSize = 20f
            canvas.drawText(item.category, 12f, yCenter + 8f, namePaint)

            val ratio = if (maxAmount > 0) (item.total / maxAmount).toFloat() else 0f
            val barWidth = ratio * barMaxWidth

            val bgRect = RectF(leftPad, yCenter - barH / 2f, leftPad + barMaxWidth, yCenter + barH / 2f)
            canvas.drawRoundRect(bgRect, 8f, 8f, bgPaint)

            if (barWidth > 0f) {
                barPaint.shader = LinearGradient(
                    leftPad, yCenter,
                    leftPad + barWidth, yCenter,
                    color,
                    Color.parseColor("#B04AE8"),
                    Shader.TileMode.CLAMP
                )
                val barRect = RectF(leftPad, yCenter - barH / 2f, leftPad + barWidth, yCenter + barH / 2f)
                canvas.drawRoundRect(barRect, 8f, 8f, barPaint)
            }

            canvas.drawText(String.format("Rs.%.0f", item.total), w - 12f, yCenter + 8f, valuePaint)
        }
    }
}
