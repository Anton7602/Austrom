package com.colleagues.austrom.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristic
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.dpToPx
import com.colleagues.austrom.extensions.draw
import com.colleagues.austrom.extensions.spToPx
import com.colleagues.austrom.extensions.toMoneyFormat

class PieChartDiagramView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    data class PieChartArc(var percentOfCircle: Float = 0F, var percentToStartAt: Float = 0F, var colorOfLine: Int = 0, var stroke: Float = 0F, var paint: Paint = Paint(), var paintRound: Boolean = true) {
        init {
            if (percentOfCircle < 0 || percentOfCircle > 100)  percentOfCircle = 100F
            percentOfCircle = 360 * percentOfCircle / 100
            if (percentToStartAt < 0 || percentToStartAt > 100) { percentToStartAt = 0F }
            percentToStartAt = 360 * percentToStartAt / 100
            if (colorOfLine == 0) { colorOfLine = Color.parseColor("#000000") }

            paint = Paint()
            paint.color = colorOfLine
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = stroke
            paint.isDither = true;

            if (paintRound) {
                paint.strokeJoin = Paint.Join.ROUND;
                paint.strokeCap = Paint.Cap.ROUND;
                paint.pathEffect = CornerPathEffect(8F);
            }
        }
    }

    companion object {
        private const val DEFAULT_MARGIN_TEXT_1 = 2
        private const val DEFAULT_MARGIN_TEXT_2 = 10
        private const val DEFAULT_MARGIN_TEXT_3 = 2
        private const val DEFAULT_MARGIN_SMALL_CIRCLE = 12
        private const val TEXT_WIDTH_PERCENT = 0.05
        private const val CIRCLE_WIDTH_PERCENT = 0.95
        const val DEFAULT_VIEW_SIZE_HEIGHT = 150
        const val DEFAULT_VIEW_SIZE_WIDTH = 250
    }

    private var marginTextFirst: Float = context.dpToPx(DEFAULT_MARGIN_TEXT_1)
    private var marginTextSecond: Float = context.dpToPx(DEFAULT_MARGIN_TEXT_2)
    private var marginTextThird: Float = context.dpToPx(DEFAULT_MARGIN_TEXT_3)
    private var marginSmallCircle: Float = context.dpToPx(DEFAULT_MARGIN_SMALL_CIRCLE)
    private val marginText: Float = marginTextFirst + marginTextSecond
    private val circleRect = RectF()
    private var circleStrokeWidth: Float = context.dpToPx(6)
    private var circleRadius: Float = 0F
    private var circlePadding: Float = context.dpToPx(8)
    private var circlePaintRoundSize: Boolean = true
    private var circleSectionSpace: Float = 3F
    private var circleCenterX: Float = 0F
    private var circleCenterY: Float = 0F
    private var numberTextPaint: TextPaint = TextPaint()
    private var descriptionTextPain: TextPaint = TextPaint()
    private var amountTextPaint: TextPaint = TextPaint()
    private var textStartX: Float = 0F
    private var textStartY: Float = 0F
    private var textHeight: Int = 0
    private var textCircleRadius: Float = context.dpToPx(4)
    private var textAmountStr: String = ""
    private var textAmountY: Float = 0F
    private var textAmountXNumber: Float = 0F
    private var textAmountXDescription: Float = 0F
    private var textAmountYDescription: Float = 0F
    private var totalAmount: Double = 0.0
    private var pieChartColors: List<Int> = listOf(context.getColor(R.color.diagramColor1), context.getColor(R.color.diagramColor2), context.getColor(R.color.diagramColor3),
        context.getColor(R.color.diagramColor4), context.getColor(R.color.diagramColor5))
    private var percentageCircleList: List<PieChartArc> = listOf()
    private var textRowList: MutableList<StaticLayout> = mutableListOf()
    private var dataList: List<Pair<Double, String>> = listOf()
    private var animationSweepAngle: Int = 0

    init {
        var textAmountSize: Float = context.spToPx(22)
        var textNumberSize: Float = context.spToPx(20)
        var textDescriptionSize: Float = context.spToPx(14)
        var textAmountColor: Int = Color.WHITE
        var textNumberColor: Int = Color.WHITE
        var textDescriptionColor: Int = Color.GRAY

        if (attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartDiagramView)

            //val colorResId = typeArray.getResourceId(R.styleable.PieChartDiagramView_pieChartColors, 0)
            //pieChartColors = typeArray.resources.getStringArray(colorResId).toList()

            // Секция отступов
            marginTextFirst = typeArray.getDimension(R.styleable.PieChartDiagramView_pieChartMarginTextFirst, marginTextFirst)
            marginTextSecond = typeArray.getDimension(R.styleable.PieChartDiagramView_pieChartMarginTextSecond, marginTextSecond)
            marginTextThird = typeArray.getDimension(R.styleable.PieChartDiagramView_pieChartMarginTextThird, marginTextThird)
            marginSmallCircle = typeArray.getDimension(R.styleable.PieChartDiagramView_pieChartMarginSmallCircle, marginSmallCircle)

            // Секция круговой диаграммы
            circleStrokeWidth = typeArray.getDimension(R.styleable.PieChartDiagramView_pieChartCircleStrokeWidth, circleStrokeWidth)
            circlePadding = typeArray.getDimension(R.styleable.PieChartDiagramView_pieChartCirclePadding, circlePadding)
            circlePaintRoundSize = typeArray.getBoolean(R.styleable.PieChartDiagramView_pieChartCirclePaintRoundSize, circlePaintRoundSize)
            circleSectionSpace = typeArray.getFloat(R.styleable.PieChartDiagramView_pieChartCircleSectionSpace, circleSectionSpace)

            // Секция текста
            textCircleRadius = typeArray.getDimension(R.styleable.PieChartDiagramView_pieChartTextCircleRadius, textCircleRadius)
            textAmountSize = typeArray.getDimension(R.styleable.PieChartDiagramView_pieChartTextAmountSize, textAmountSize)
            textNumberSize = typeArray.getDimension(R.styleable.PieChartDiagramView_pieChartTextNumberSize, textNumberSize)
            textDescriptionSize = typeArray.getDimension(R.styleable.PieChartDiagramView_pieChartTextDescriptionSize, textDescriptionSize)
            textAmountColor = typeArray.getColor(R.styleable.PieChartDiagramView_pieChartTextAmountColor, textAmountColor)
            textNumberColor = typeArray.getColor(R.styleable.PieChartDiagramView_pieChartTextNumberColor, textNumberColor)
            textDescriptionColor = typeArray.getColor(R.styleable.PieChartDiagramView_pieChartTextDescriptionColor, textDescriptionColor)
            textAmountStr = typeArray.getString(R.styleable.PieChartDiagramView_pieChartTextAmount) ?: ""

            typeArray.recycle()
        }

        circlePadding += circleStrokeWidth

        // Инициализация кистей View
        initPains(amountTextPaint, textAmountSize, textAmountColor)
        initPains(numberTextPaint, textNumberSize, textNumberColor)
        initPains(descriptionTextPain, textDescriptionSize, textDescriptionColor, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        startAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawCircle(canvas)
        drawText(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
//        if (animationSweepAngle==0) {
//            canvas.drawArc(circleRect, 0F, 359F, false ,descriptionTextPain)
//        }
        for(percent in percentageCircleList) {
            if (animationSweepAngle > percent.percentToStartAt + percent.percentOfCircle){
                canvas.drawArc(circleRect, percent.percentToStartAt, percent.percentOfCircle, false, percent.paint)
            } else if (animationSweepAngle > percent.percentToStartAt) {
                canvas.drawArc(circleRect, percent.percentToStartAt, animationSweepAngle - percent.percentToStartAt, false, percent.paint)
            }
        }
    }

    private fun drawText(canvas: Canvas) {
        canvas.drawText(totalAmount.toMoneyFormat()+" "+AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode]?.symbol,
            textAmountXNumber, textAmountY, amountTextPaint)
        canvas.drawText(textAmountStr, textAmountXDescription, textAmountYDescription, descriptionTextPain)
    }

    fun setChartData(list: List<Pair<Double, String>>) {
        dataList = list
        calculatePercentageOfData()
    }

    /**
     * Метод заполнения поля [percentageCircleList]
     */
    private fun calculatePercentageOfData() {
        totalAmount = 0.0
        dataList.forEach { entry -> totalAmount += entry.first }

        var startAt = circleSectionSpace
        percentageCircleList = dataList.mapIndexed { index, pair ->
            var percent = pair.first * 100 / totalAmount.toFloat() - circleSectionSpace
            percent = if (percent < 0.0) 0.0 else percent

            val resultModel = PieChartArc(
                percentOfCircle = percent.toFloat(),
                percentToStartAt = startAt,
                colorOfLine = pieChartColors[index % pieChartColors.size],
                stroke = circleStrokeWidth,
                paintRound = circlePaintRoundSize
            )
            if (percent != 0.0) startAt += percent.toFloat() + circleSectionSpace
            resultModel
        }
    }

    fun startAnimation() {
        // Проход значений от 0 до 360 (целый круг), с длительностью - 1.5 секунды
        val animator = ValueAnimator.ofInt(0, 360).apply {
            duration = 1500 // длительность анимации в миллисекундах
            interpolator = FastOutSlowInInterpolator() // интерпретатор анимации
            addUpdateListener { valueAnimator ->
                // Обновляем значение для отрисовки диаграммы
                animationSweepAngle = valueAnimator.animatedValue as Int
                // Принудительная перерисовка
                invalidate()
            }
        }
        animator.start()
    }

    private fun initPains(textPaint: TextPaint, textSize: Float, textColor: Int, isDescription: Boolean = false) {
        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true

        if (!isDescription) textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun resolveDefaultSize(spec: Int, defValue: Int): Int {
        return when(MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> context.dpToPx(defValue).toInt() // Размер не определен parent layout
            else -> MeasureSpec.getSize(spec) // Слушаемся parent layout
        }
    }

    private fun getMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        textDir: TextDirectionHeuristic = TextDirectionHeuristics.LTR,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f) : StaticLayout {

        return StaticLayout.Builder
            .obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(spacingAdd, spacingMult)
            .build()
    }

    private fun getTextViewHeight(maxWidth: Int): Int {
        var textHeight = 0
        // Проходимся по всем данным, которые передали в View
        dataList.forEach {
            // Создаем объект StaticLayout для значения данных
            val textLayoutNumber = getMultilineText(
                text = it.first.toString(),
                textPaint = numberTextPaint,
                width = maxWidth
            )
            // Создаем объект StaticLayout для описания значения данных
            val textLayoutDescription = getMultilineText(
                text = it.second,
                textPaint = descriptionTextPain,
                width = maxWidth
            )
            // Сохраняем объекты в список для отрисовки
            textRowList.apply {
                add(textLayoutNumber)
                add(textLayoutDescription)
            }
            // Складываем высоты текстов
            textHeight += textLayoutNumber.height + textLayoutDescription.height
        }
        return textHeight
    }

    private fun calculateViewHeight(heightMeasureSpec: Int, textWidth: Int): Int {
        // Получаем высоту, которую нам предлагает parent layout
        val initSizeHeight = resolveDefaultSize(heightMeasureSpec, DEFAULT_VIEW_SIZE_HEIGHT)
//        // Высчитываем высоту текста с учетом отступов
//        textHeight = (dataList.size * marginText + getTextViewHeight(textWidth)).toInt()
//
//        // Добавляем к значению высоты вертикальные padding View
//        val textHeightWithPadding = textHeight + paddingTop + paddingBottom
        //return if (textHeightWithPadding > initSizeHeight) textHeightWithPadding else initSizeHeight
        return initSizeHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // Очищаем список строк для текста
        textRowList.clear()

        // Получаем ширину View
        val initSizeWidth = resolveDefaultSize(widthMeasureSpec, DEFAULT_VIEW_SIZE_WIDTH)

        // Высчитываем ширину, которую будет занимать текст
        val textTextWidth = (initSizeWidth * TEXT_WIDTH_PERCENT)

        // Вычисляем необходимую высоту для нашей View
        val initSizeHeight = calculateViewHeight(heightMeasureSpec, textTextWidth.toInt())

        // Координаты X и Y откуда будет происходить отрисовка текста
        textStartX = initSizeWidth - textTextWidth.toFloat()
        textStartY = initSizeHeight.toFloat() / 2 - textHeight / 2

        calculateCircleRadius(initSizeWidth, initSizeHeight)
        setMeasuredDimension(initSizeWidth, initSizeHeight)
    }

    private fun calculateCircleRadius(width: Int, height: Int) {
        circleRadius = if (width > height) (height.toFloat() - circlePadding) / 2 else (width.toFloat() - circlePadding) / 2
        circleCenterX = width/2F
        circleCenterY = height/2F
        with(circleRect) {
            left = circleCenterX-circleRadius
            top = circleCenterY-circleRadius
            right = circleCenterX+circleRadius
            bottom = circleCenterY+circleRadius
        }

        textAmountY = circleCenterY
        // Создаем контейнер для отображения текста в центре круговой диаграммы
        val sizeTextAmountNumber = getWidthOfAmountText(totalAmount.toMoneyFormat()+" "+AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode]?.symbol, amountTextPaint )

        // Расчет координат для отображения текста в центре круговой диаграммы
        textAmountXNumber = circleCenterX -  sizeTextAmountNumber.width() / 2
        textAmountXDescription = circleCenterX - getWidthOfAmountText(textAmountStr, descriptionTextPain).width() / 2
        textAmountYDescription = circleCenterY + sizeTextAmountNumber.height() + marginTextThird
    }

    private fun getWidthOfAmountText(text: String, textPaint: TextPaint): Rect {
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }
}