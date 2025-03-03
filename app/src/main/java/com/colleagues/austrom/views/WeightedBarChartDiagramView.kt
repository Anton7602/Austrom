package com.colleagues.austrom.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Transaction
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

class WeightedBarChartDiagramView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {


    private val barPaintNeutral = Paint().apply {color = context.getColor(R.color.secondaryTextColor); style = Paint.Style.FILL; strokeWidth = 2f }
    private val barPaintPositive = Paint().apply {color = context.getColor(R.color.incomeGreenChart); style = Paint.Style.FILL; strokeCap=Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; pathEffect = CornerPathEffect(8F) }
    private val barPaintNegative = Paint().apply {color = context.getColor(R.color.expenseRedChart); style = Paint.Style.FILL; strokeCap=Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; pathEffect = CornerPathEffect(8F) }
    private val axisPaint = Paint().apply { color = context.getColor(R.color.secondaryTextColor); strokeWidth = 4f }
    private val labelPaint = Paint().apply {color = context.getColor(R.color.secondaryTextColor); textSize = 30f}
    private val gridPaint = Paint().apply {color = context.getColor(R.color.secondaryTextColor); strokeWidth = 2f }

    private var transactions: List<Transaction> = emptyList()
    private var startDate: LocalDate = LocalDate.now()
    private var endDate: LocalDate = LocalDate.now()
    private var endNetWorth: Double = 0.0

    private val barWidth = 25f
    private val barSpacing = 10f
    private val padding = 50f

    fun setData(transactions: List<Transaction>, startDate: LocalDate, endDate: LocalDate, endNetWorth: Double) {
        this.transactions = transactions
        this.startDate = startDate
        this.endDate = endDate
        this.endNetWorth = endNetWorth
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val days = (startDate..endDate).toList()
        val netWorthMap = calculateNetWorthPerDay(days)

        val minNetWorth = netWorthMap.values.minOrNull() ?: 0.0
        val maxNetWorth = netWorthMap.values.maxOrNull() ?: 0.0

        val graphHeight = height - 2 * padding
        val graphWidth = days.size * (barWidth + barSpacing)

        // Draw grid and axis
        //drawGridAndAxis(canvas, minNetWorth, maxNetWorth, graphHeight, graphWidth)

        if (transactions.isEmpty()) return
        // Draw bars
        var currentX = padding
        for (day in days) {
            val startNetWorth = netWorthMap[day] ?: 0.0
            val endNetWorth = netWorthMap[day.plusDays(1)] ?: startNetWorth

            val startY = mapValueToY(startNetWorth, minNetWorth, maxNetWorth, graphHeight)
            val endY = mapValueToY(endNetWorth, minNetWorth, maxNetWorth, graphHeight)

            val barPaint = if (endNetWorth >= startNetWorth) barPaintPositive else barPaintNegative

            if (startY==endY) {
                canvas.drawLine(currentX, startY, currentX+barWidth, startY, barPaintNeutral)
            } else {
                canvas.drawRect(
                    currentX,
                    startY,
                    currentX + barWidth,
                    endY,
                    barPaint
                )
            }

            currentX += barWidth + barSpacing
        }
    }

    private fun drawGridAndAxis(canvas: Canvas, minNetWorth: Double, maxNetWorth: Double, graphHeight: Float, graphWidth: Float) {
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint)
        canvas.drawLine(padding, height - padding, width.toFloat(), height - padding, axisPaint)

        val step = (maxNetWorth - minNetWorth) / 5
        for (i in 0..5) {
            val value = minNetWorth + i * step
            val y = mapValueToY(value, minNetWorth, maxNetWorth, graphHeight)

            canvas.drawLine(padding, y, width.toFloat(), y, gridPaint)
            canvas.drawText("%.2f".format(value), 10f, y, labelPaint)
        }
    }

    private fun mapValueToY(value: Double, min: Double, max: Double, graphHeight: Float): Float {
        return (height - padding) - ((value - min) / (max - min) * graphHeight).toFloat()
    }

    private fun calculateNetWorthPerDay(days: List<LocalDate>): Map<LocalDate, Double> {
        val netWorthMap = mutableMapOf<LocalDate, Double>()
        var currentNetWorth = endNetWorth

        for (day in days.reversed()) {
            val dailyTransactions = transactions.filter { it.transactionDate == day }
            val dailyChange = dailyTransactions.sumOf { it.amount }
            currentNetWorth -= dailyChange
            netWorthMap[day] = currentNetWorth
        }

        return netWorthMap
    }

    private operator fun LocalDate.rangeTo(other: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var current = this
        while (current <= other) {
            dates.add(current)
            current = current.plusDays(1)
        }
        return dates
    }
}