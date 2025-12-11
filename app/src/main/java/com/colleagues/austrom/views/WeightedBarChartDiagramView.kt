package com.colleagues.austrom.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.getLastDayOfMonth
import com.colleagues.austrom.extensions.getListOfDaysTillDate
import com.colleagues.austrom.extensions.getListOfMonthTillDate
import com.colleagues.austrom.extensions.getLocalizedMonthName
import com.colleagues.austrom.extensions.roundToAFirstDigit
import com.colleagues.austrom.extensions.spToPx
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Transaction
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max

class WeightedBarChartDiagramView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {


    private val barPaintNeutral = Paint().apply {color = context.getColor(R.color.secondaryTextColor); style = Paint.Style.FILL; strokeWidth = 2f }
    private val barPaintPositive = Paint().apply {color = context.getColor(R.color.incomeGreenChart); style = Paint.Style.FILL; strokeCap=Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; pathEffect = CornerPathEffect(8F) }
    private val barPaintNegative = Paint().apply {color = context.getColor(R.color.expenseRedChart); style = Paint.Style.FILL; strokeCap=Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; pathEffect = CornerPathEffect(8F) }
    private val axisPaint = Paint().apply { color = context.getColor(R.color.chartGrid); strokeWidth = 4f }
    private val labelPaintX = Paint().apply {color = context.getColor(R.color.secondaryTextColor); textSize = context.spToPx(12)}
    private val labelPaintY = Paint().apply {color = context.getColor(R.color.secondaryTextColor); textSize = context.spToPx(8)}
    private val gridPaint = Paint().apply {color = context.getColor(R.color.chartGrid); strokeWidth = 2f }

    private var minNetWorth = 0.0
    private var maxNetWorth = 0.0

    private var transactions: List<Transaction> = emptyList()
    private var barGroupType: WeightedBarGroupType? = null
    private var startDate: LocalDate = LocalDate.now()
    private var endDate: LocalDate = LocalDate.now()
    private var endNetWorth: Double = 0.0

    private var barWidth = 25f
    private var barSpacing = 8f
    private var verticalAxisStepHeight = 0.0

    private val minBarWidth = 20f
    private val minBarSpacing = 4f
    private val minNumberOfVerticalGridLines = 6

    private val verticalPadding = 50f

    private var animationDrawCoordinate: Int = 0
    private var labelXBound: Rect = Rect()
    private var netWorthMap: MutableMap<LocalDate, Double> = mutableMapOf()

    private var days: List<LocalDate> = mutableListOf()
    private var months: List<YearMonth> = mutableListOf()

    fun setData(transactions: List<Transaction>, startDate: LocalDate, endDate: LocalDate, endNetWorth: Double, weightedBarGroupType: WeightedBarGroupType? = null) {
        this.transactions = transactions
        this.startDate = startDate
        this.endDate = endDate
        this.endNetWorth = endNetWorth
        netWorthMap = calculateNetWorthPerDay(startDate.getListOfDaysTillDate(endDate))
        minNetWorth = netWorthMap.values.minOrNull() ?: 0.0
        maxNetWorth = netWorthMap.values.maxOrNull() ?: 0.0
        barGroupType = weightedBarGroupType
        days = startDate.getListOfDaysTillDate(endDate)
        months = startDate.getListOfMonthTillDate(endDate)
        requestLayout()
        invalidate()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        startAnimation()
    }

    private fun startAnimation() {
        //val days = startDate.getListOfDaysTillDate(endDate)
        val animationSteps = when (barGroupType) {
            WeightedBarGroupType.BY_DAY -> days.size
            WeightedBarGroupType.BY_MONTH -> months.size
            else -> days.size
        }
        val animator = ValueAnimator.ofInt(0, (animationSteps * (barWidth + barSpacing)).toInt()).apply {
            duration = (animationSteps*50).toLong()
            interpolator = LinearInterpolator()
            addUpdateListener { valueAnimator ->
                animationDrawCoordinate = valueAnimator.animatedValue as Int
                invalidate()
            }
        }
        animator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //val days = startDate.getListOfDaysTillDate(endDate)
        if (barGroupType==null) {
            barGroupType = if (days.size<=62) WeightedBarGroupType.BY_DAY else if (months.size<=25) WeightedBarGroupType.BY_MONTH else WeightedBarGroupType.BY_YEAR
        }
        when (barGroupType) {
            WeightedBarGroupType.BY_DAY -> {
                val totalWidth = if (days.size<=31) (this.parent as View).width else (days.size * (minBarWidth + minBarSpacing)).toInt()
                barWidth = max((totalWidth/days.size)*0.8f, minBarWidth)
                barSpacing = max((totalWidth/days.size)*0.2f, minBarSpacing)
                val totalHeight = MeasureSpec.getSize(heightMeasureSpec)
                verticalAxisStepHeight = ((maxNetWorth - minNetWorth) / minNumberOfVerticalGridLines).roundToAFirstDigit()
                setMeasuredDimension(totalWidth, totalHeight)
            }
            WeightedBarGroupType.BY_MONTH -> {
                //val months = startDate.getListOfMonthTillDate(endDate)
                val totalWidth = if (months.size<=25) (this.parent as View).width else (months.size * (minBarWidth + minBarSpacing)).toInt()
                barWidth = max((totalWidth/months.size)*0.8f, minBarWidth)
                barSpacing = max((totalWidth/months.size)*0.2f, minBarSpacing)
                val totalHeight = MeasureSpec.getSize(heightMeasureSpec)
                verticalAxisStepHeight = ((maxNetWorth - minNetWorth) / minNumberOfVerticalGridLines).roundToAFirstDigit()
                setMeasuredDimension(totalWidth, totalHeight)
            }
            WeightedBarGroupType.BY_YEAR -> { }
            else -> { }
        }

        startAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //val days = startDate.getListOfDaysTillDate(endDate)

        val graphHeight = height - 2 * verticalPadding
        val graphWidth = when (barGroupType) {
            WeightedBarGroupType.BY_DAY -> days.size * (barWidth + barSpacing)
            WeightedBarGroupType.BY_MONTH -> months.size * (barWidth + barSpacing)
            //WeightedBarGroupType.BY_YEAR -> days.size * (barWidth + barSpacing)
            else -> days.size * (barWidth + barSpacing)
        }

        drawGridAndAxis(canvas, graphHeight, graphWidth)

        if (transactions.isEmpty()) return
        var currentX = barSpacing

        when (barGroupType) {
            WeightedBarGroupType.BY_DAY -> {
                for (day in days) {
                    if ((day.dayOfMonth-1)%3==0) {
                        val labelText = if (days.first().monthValue==days.last().monthValue) day.dayOfMonth.toString()
                        else if (day.dayOfMonth==1) day.getLocalizedMonthName().substring(0,3)
                        else if (day.dayOfMonth == day.getLastDayOfMonth().dayOfMonth) ""
                        else day.dayOfMonth.toString()
                        if (labelText.isNotEmpty()) {
                            labelPaintX.getTextBounds(labelText, 0, labelText.length, labelXBound)
                            canvas.drawText(labelText,currentX+(barWidth/2)-labelXBound.width()/2, height.toFloat(),labelPaintX)
                            canvas.drawLine(currentX+barWidth/2,0f, currentX+barWidth/2, mapValueToY(minNetWorth, minNetWorth, maxNetWorth, graphHeight), axisPaint)
                        }
                    }
                    val startNetWorth = netWorthMap[day] ?: 0.0
                    val endNetWorth = netWorthMap[day.plusDays(1)] ?: startNetWorth


                    val barPaint = if (endNetWorth >= startNetWorth) barPaintPositive else barPaintNegative
                    val startY = mapValueToY(startNetWorth, minNetWorth, maxNetWorth, graphHeight)
                    val endY = mapValueToY(endNetWorth, minNetWorth, maxNetWorth, graphHeight)

                    if (currentX<animationDrawCoordinate) {
                        if (startY==endY) {
                            canvas.drawLine(currentX, startY, currentX+barWidth, startY, barPaintNeutral)
                        } else {
                            val percent = if (animationDrawCoordinate>currentX && animationDrawCoordinate<currentX+barWidth) (animationDrawCoordinate-currentX)/barWidth else 1f
                            canvas.drawRect(currentX, startY,currentX + barWidth,startY+(endY-startY)*percent,barPaint)
                        }
                    }

                    currentX += barWidth + barSpacing
                }
            }
            WeightedBarGroupType.BY_MONTH -> {
                for (month in months) {
                        val labelText = month.atDay(1).getLocalizedMonthName().substring(0,3)
                        if (labelText.isNotEmpty()) {
                            labelPaintX.getTextBounds(labelText, 0, labelText.length, labelXBound)
                            canvas.drawText(labelText,currentX+(barWidth/2)-labelXBound.width()/2, height.toFloat(),labelPaintX)
                            canvas.drawLine(currentX+barWidth/2,0f, currentX+barWidth/2, mapValueToY(minNetWorth, minNetWorth, maxNetWorth, graphHeight), axisPaint)
                        }
                    val startNetWorth = netWorthMap[month.atDay(1)] ?: netWorthMap[startDate] ?: 0.0
                    val endNetWorth = netWorthMap[month.atEndOfMonth()] ?: netWorthMap[endDate] ?: startNetWorth

                    val barPaint = if (endNetWorth >= startNetWorth) barPaintPositive else barPaintNegative
                    val startY = mapValueToY(startNetWorth, minNetWorth, maxNetWorth, graphHeight)
                    val endY = mapValueToY(endNetWorth, minNetWorth, maxNetWorth, graphHeight)

                    if (currentX<animationDrawCoordinate) {
                        if (startY==endY) {
                            canvas.drawLine(currentX, startY, currentX+barWidth, startY, barPaintNeutral)
                        } else {
                            val percent = if (animationDrawCoordinate>currentX && animationDrawCoordinate<currentX+barWidth) (animationDrawCoordinate-currentX)/barWidth else 1f
                            canvas.drawRect(currentX, startY,currentX + barWidth,startY+(endY-startY)*percent,barPaint)
                        }
                    }

                    currentX += barWidth + barSpacing
                }
            }
            else -> {
                for (day in days) {
                    if ((day.dayOfMonth-1)%3==0) {
                        val labelText = if (days.first().monthValue==days.last().monthValue) day.dayOfMonth.toString()
                        else if (day.dayOfMonth==1) day.getLocalizedMonthName().substring(0,3)
                        else if (day.dayOfMonth == day.getLastDayOfMonth().dayOfMonth) ""
                        else day.dayOfMonth.toString()
                        if (labelText.isNotEmpty()) {
                            labelPaintX.getTextBounds(labelText, 0, labelText.length, labelXBound)
                            canvas.drawText(labelText,currentX+(barWidth/2)-labelXBound.width()/2, height.toFloat(),labelPaintX)
                            canvas.drawLine(currentX+barWidth/2,0f, currentX+barWidth/2, mapValueToY(minNetWorth, minNetWorth, maxNetWorth, graphHeight), axisPaint)
                        }
                    }
                    val startNetWorth = netWorthMap[day] ?: 0.0
                    val endNetWorth = netWorthMap[day.plusDays(1)] ?: startNetWorth


                    val barPaint = if (endNetWorth >= startNetWorth) barPaintPositive else barPaintNegative
                    val startY = mapValueToY(startNetWorth, minNetWorth, maxNetWorth, graphHeight)
                    val endY = mapValueToY(endNetWorth, minNetWorth, maxNetWorth, graphHeight)

                    if (currentX<animationDrawCoordinate) {
                        if (startY==endY) {
                            canvas.drawLine(currentX, startY, currentX+barWidth, startY, barPaintNeutral)
                        } else {
                            val percent = if (animationDrawCoordinate>currentX && animationDrawCoordinate<currentX+barWidth) (animationDrawCoordinate-currentX)/barWidth else 1f
                            canvas.drawRect(currentX, startY,currentX + barWidth,startY+(endY-startY)*percent,barPaint)
                        }
                    }

                    currentX += barWidth + barSpacing
                }
            }
        }

    }

    private fun drawGridAndAxis(canvas: Canvas, graphHeight: Float, graphWidth: Float) {
        canvas.drawLine(0f, height - verticalPadding, width.toFloat(), height - verticalPadding, axisPaint)
        var numberOfSteps = minNumberOfVerticalGridLines
        if (verticalAxisStepHeight==0.0) return
        minNetWorth -= minNetWorth%verticalAxisStepHeight
        while (minNetWorth+numberOfSteps*verticalAxisStepHeight<maxNetWorth) {
            numberOfSteps++
            if (minNetWorth+numberOfSteps*verticalAxisStepHeight>maxNetWorth) maxNetWorth=minNetWorth+numberOfSteps*verticalAxisStepHeight
        }
        for (i in 0..numberOfSteps) {
            val value = minNetWorth + i * verticalAxisStepHeight
            val y = mapValueToY(value, minNetWorth, maxNetWorth, graphHeight)
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            canvas.drawText(value.toMoneyFormat(), 5f, y, labelPaintY)
        }
    }

    private fun mapValueToY(value: Double, min: Double, max: Double, graphHeight: Float): Float {
        return (height - verticalPadding) - ((value - min) / (max - min) * graphHeight).toFloat()
    }

    private fun calculateNetWorthPerDay(days: List<LocalDate>): MutableMap<LocalDate, Double> {
        val netWorthMap = mutableMapOf<LocalDate, Double>()
        var currentNetWorth = endNetWorth

        for (day in days.reversed()) {
            val dailyChange = Transaction.getSumOfTransactions(transactions.filter { it.transactionDate == day })
            currentNetWorth -= dailyChange
            netWorthMap[day] = currentNetWorth
        }
        return netWorthMap
    }
}

enum class WeightedBarGroupType{ BY_DAY, BY_MONTH, BY_YEAR }