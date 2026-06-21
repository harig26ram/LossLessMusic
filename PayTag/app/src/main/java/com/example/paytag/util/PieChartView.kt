package com.example.paytag.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.paytag.data.CategoryTotal

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var data = listOf<CategoryTotal>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val colors = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#6C3CE1"),
        Color.parseColor("#FFD93D"),
        Color.parseColor("#A855F7"),
        Color.parseColor("#00D68F"),
        Color.parseColor("#4DABF7"),
        Color.parseColor("#6C6C80"),
        Color.parseColor("#FFB547"),
        Color.parseColor("#E94E7A")
    )

    fun setData(newData: List<CategoryTotal>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#141420"))

        if (data.isEmpty()) {
            val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#6C6C80")
                textSize = 20f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("No data", width / 2f, height / 2f, emptyPaint)
            return
        }

        val total = data.sumOf { it.total }.toFloat()
        if (total <= 0) return

        val cx = width / 2f
        val cy = height / 2f
        val radius = (minOf(cx, cy) * 0.78f)
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        var startAngle = -90f
        data.forEachIndexed { index, item ->
            val sweepAngle = ((item.total / total) * 360f).toFloat()
            paint.color = colors[index % colors.size]
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)

            if (sweepAngle > 25) {
                val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                val labelRadius = radius * 0.62f
                val lx = cx + (labelRadius * Math.cos(midAngle)).toFloat()
                val ly = cy + (labelRadius * Math.sin(midAngle)).toFloat()
                val percent = (item.total / total * 100).toInt()
                canvas.drawText("${percent}%", lx, ly + 10, textPaint)
            }
            startAngle += sweepAngle
        }

        paint.color = Color.parseColor("#141420")
        canvas.drawCircle(cx, cy, radius * 0.38f, paint)

        paint.color = Color.parseColor("#1A1A2E")
        canvas.drawCircle(cx, cy, radius * 0.36f, paint)

        val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF")
            textSize = 26f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Total", cx, cy - 12, centerTextPaint)
        centerTextPaint.textSize = 20f
        centerTextPaint.color = Color.parseColor("#6C3CE1")
        canvas.drawText(String.format("Rs.%.0f", total), cx, cy + 18, centerTextPaint)
    }
}
