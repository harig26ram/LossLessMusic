package com.example.paytag.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class BarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var dailyData = mapOf<Int, Double>()
    private var maxAmount = 0.0
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6C6C80")
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }

    fun setData(data: Map<Int, Double>) {
        dailyData = data
        maxAmount = data.values.maxOrNull() ?: 0.0
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.parseColor("#141420"))

        if (dailyData.isEmpty()) {
            val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#6C6C80")
                textSize = 20f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("No data", width / 2f, height / 2f, emptyPaint)
            return
        }

        val daysInMonth = (dailyData.keys.maxOrNull() ?: 30).coerceAtLeast(28)
        val padding = 16f
        val bottomPadding = 36f
        val topPadding = 12f
        val chartWidth = width - padding * 2
        val chartHeight = height - bottomPadding - topPadding
        val barWidth = chartWidth / daysInMonth * 0.6f
        val gap = chartWidth / daysInMonth

        for (day in 1..daysInMonth) {
            val amount = dailyData[day] ?: 0.0
            val barHeight = if (maxAmount > 0) (amount / maxAmount * chartHeight).toFloat() else 0f
            val x = padding + (day - 1) * gap + gap / 2

            if (amount > 0) {
                barPaint.shader = LinearGradient(
                    x, topPadding + chartHeight - barHeight,
                    x, topPadding + chartHeight,
                    Color.parseColor("#6C3CE1"),
                    Color.parseColor("#B04AE8"),
                    Shader.TileMode.CLAMP
                )
                val rect = RectF(
                    x - barWidth / 2,
                    topPadding + chartHeight - barHeight,
                    x + barWidth / 2,
                    topPadding + chartHeight
                )
                canvas.drawRoundRect(rect, 4f, 4f, barPaint)
            }

            if (daysInMonth <= 15 || day % 2 == 1 || day == daysInMonth) {
                canvas.drawText(day.toString(), x, height - 8f, labelPaint)
            }
        }
    }
}
