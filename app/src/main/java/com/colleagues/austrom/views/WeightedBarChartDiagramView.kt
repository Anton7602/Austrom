package com.colleagues.austrom.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.roundToAFirstDigit
import com.colleagues.austrom.extensions.toDayAndShortMonthNameFormat
import com.colleagues.austrom.models.Transaction
import java.time.LocalDate

class WeightedBarChartDiagramView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {


    private val barPaintNeutral = Paint().apply {color = context.getColor(R.color.secondaryTextColor); style = Paint.Style.FILL; strokeWidth = 2f }
    private val barPaintPositive = Paint().apply {color = context.getColor(R.color.incomeGreenChart); style = Paint.Style.FILL; strokeCap=Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; pathEffect = CornerPathEffect(8F) }
    private val barPaintNegative = Paint().apply {color = context.getColor(R.color.expenseRedChart); style = Paint.Style.FILL; strokeCap=Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; pathEffect = CornerPathEffect(8F) }
    private val axisPaint = Paint().apply { color = context.getColor(R.color.secondaryTextColor); strokeWidth = 4f }
    private val labelPaint = Paint().apply {color = context.getColor(R.color.secondaryTextColor); textSize = 30f}
    private val gridPaint = Paint().apply {color = context.getColor(R.color.secondaryTextColor); strokeWidth = 2f }

    private var minNetWorth = 0.0
    private var maxNetWorth = 0.0

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
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val days = (startDate..endDate).toList()
        val totalWidth = (days.size * (barWidth + barSpacing) + padding * 2).toInt()
        val totalHeight = MeasureSpec.getSize(heightMeasureSpec)

        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val days = (startDate..endDate).toList()
        val netWorthMap = calculateNetWorthPerDay(days)

        minNetWorth = netWorthMap.values.minOrNull() ?: 0.0
        maxNetWorth = netWorthMap.values.maxOrNull() ?: 0.0

        val graphHeight = height - 2 * padding
        val graphWidth = days.size * (barWidth + barSpacing)

        // Draw grid and axis
        drawGridAndAxis(canvas, graphHeight, graphWidth)

        if (transactions.isEmpty()) return
        // Draw bars
        var currentX = barSpacing
        for (day in days) {
            if ((day.dayOfMonth-1)%4==0) {
                canvas.drawText(day.dayOfMonth.toString(),currentX,height.toFloat(),labelPaint)
                canvas.drawLine(currentX+barWidth/2,0f, currentX+barWidth/2, mapValueToY(minNetWorth, minNetWorth, maxNetWorth, graphHeight), axisPaint)
            }
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

    private fun drawGridAndAxis(canvas: Canvas, graphHeight: Float, graphWidth: Float) {
        canvas.drawLine(0f, height - padding, width.toFloat(), height - padding, axisPaint)

        val stepHeight = ((maxNetWorth - minNetWorth) / 5).roundToAFirstDigit()
        if (stepHeight==0.0) return
        minNetWorth -= minNetWorth%stepHeight
        var numberOfSteps = 4
        while (minNetWorth+numberOfSteps*stepHeight<maxNetWorth) {
            numberOfSteps++
            if (minNetWorth+numberOfSteps*stepHeight>maxNetWorth) maxNetWorth=minNetWorth+numberOfSteps*stepHeight
        }
        for (i in 0..numberOfSteps) {
            val value = minNetWorth + i * stepHeight
            val y = mapValueToY(value, minNetWorth, maxNetWorth, graphHeight)

            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            canvas.drawText("%.2f".format(value), 100f, y, labelPaint)
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
            var dailyChange = 0.0
            dailyTransactions.forEach { val transactionCurrency = AustromApplication.activeAssets[it.assetId]?.currencyCode ?: return@forEach
                dailyChange+=if (transactionCurrency==AustromApplication.appUser!!.baseCurrencyCode) it.amount else
                it.amount/(AustromApplication.activeCurrencies[transactionCurrency]?.exchangeRate ?: 1.0)
            }
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