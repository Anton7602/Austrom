package com.colleagues.austrom.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.dpToPx
import com.colleagues.austrom.extensions.spToPx
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Currency

class MoneyFormatTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {
    companion object {
        const val DEFAULT_VIEW_SIZE_HEIGHT = 20
    }

    private var amountTextSize: Float = 0F
    private var currencyTextSize: Float = 0F
    private var amountMinTextSize: Float = context.spToPx(9)
    private var currencyMinTextSize: Float = context.spToPx(6)
    private var textPadding: Float = context.dpToPx(8)
    private var amountToCurrencyMargin: Float = 0F
    private var percentOfCurrencyTextSize: Float = 0.5F
    private var moneyAmount: Double = 0.0
    private var currencyCode: String = "$"
    private var amountTextPaint: TextPaint = TextPaint()
    private var currencyTextPaint: TextPaint = TextPaint()
    private var textHorizontalAlignment: Int = 1
    private var textVerticalAlignment: Int = 1
    private var xStartPos: Float = 0F
    private var yStartPos: Float= 0F

    init {
        var moneyAmountColor: Int = context.getColor(R.color.primaryTextColor)
        var currencyColor: Int = context.getColor(R.color.secondaryTextColor)

        if (attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.MoneyFormatTextView)

            textPadding = typeArray.getDimension(R.styleable.MoneyFormatTextView_moneyPadding, textPadding)
            amountTextSize = typeArray.getDimension(R.styleable.MoneyFormatTextView_amountSize, amountTextSize)
            currencyTextSize = typeArray.getDimension(R.styleable.MoneyFormatTextView_currencySize, currencyTextSize)
            moneyAmountColor = typeArray.getColor(R.styleable.MoneyFormatTextView_amountColor, moneyAmountColor)
            currencyColor = typeArray.getColor(R.styleable.MoneyFormatTextView_currencyColor, currencyColor)
            amountToCurrencyMargin = typeArray.getDimension(R.styleable.MoneyFormatTextView_currencyMargin, amountToCurrencyMargin)
            textHorizontalAlignment = typeArray.getInt(R.styleable.MoneyFormatTextView_horizontalAlignment, 1)
            textVerticalAlignment = typeArray.getInt(R.styleable.MoneyFormatTextView_verticalAlignment, 1)
            moneyAmount = typeArray.getFloat(R.styleable.MoneyFormatTextView_defaultAmount, 0F).toDouble()
            percentOfCurrencyTextSize = typeArray.getFloat(R.styleable.MoneyFormatTextView_currencyTextSizePercent, 0.5F)
            typeArray.recycle()
        }

        initPaints(amountTextPaint, amountTextSize, moneyAmountColor)
        initPaints(currencyTextPaint, currencyTextSize, currencyColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawText(canvas)
    }

    private fun drawText(canvas: Canvas) {
        if (amountTextPaint.textSize==0F || currencyTextPaint.textSize==0F) {
            calculateTextSizes()
        }
        calculateTextPosition()
        canvas.drawText(moneyAmount.toMoneyFormat(), xStartPos, yStartPos, amountTextPaint)
        canvas.drawText(currencyCode, xStartPos+getTextBounds(moneyAmount.toMoneyFormat(), amountTextPaint).width()+amountToCurrencyMargin, yStartPos, currencyTextPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val initSizeHeight = resolveDefaultHeight(heightMeasureSpec)
        val initSizeWidth = resolveDefaultWidth(widthMeasureSpec, initSizeHeight)
        setMeasuredDimension(initSizeWidth, initSizeHeight)
    }

    private fun resolveDefaultHeight(spec: Int): Int {
        return when(MeasureSpec.getMode(spec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            MeasureSpec.AT_MOST -> minOf(context.dpToPx(DEFAULT_VIEW_SIZE_HEIGHT).toInt(), MeasureSpec.getSize(spec))
            MeasureSpec.UNSPECIFIED -> context.dpToPx(DEFAULT_VIEW_SIZE_HEIGHT).toInt()
            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun resolveDefaultWidth(spec: Int, sizeHeight: Int): Int {
        return when(MeasureSpec.getMode(spec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            MeasureSpec.AT_MOST ->  {
                amountTextPaint.textSize = sizeHeight-textPadding*2
                currencyTextPaint.textSize = amountTextPaint.textSize*percentOfCurrencyTextSize
                if (amountToCurrencyMargin == 0F) {amountToCurrencyMargin = getTextBounds(currencyCode.first().toString(), currencyTextPaint).width()/2F }
                minOf(getMoneyTextWidth()+textPadding.toInt()*2+amountToCurrencyMargin.toInt(), MeasureSpec.getSize(spec))
            }
            MeasureSpec.UNSPECIFIED -> getMoneyTextWidth()
            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun calculateTextSizes() {
        val isAmountTextSizeFixed = amountTextPaint.textSize!=0F
        val isCurrencyTextSizeFixed = currencyTextPaint.textSize!=0F
        if (isAmountTextSizeFixed && isCurrencyTextSizeFixed) return
        if (!isAmountTextSizeFixed) amountTextPaint.textSize = height-textPadding*2
        if (!isCurrencyTextSizeFixed) currencyTextPaint.textSize = amountTextPaint.textSize * percentOfCurrencyTextSize
        while (getMoneyTextWidth()+textPadding*2>width && (amountTextPaint.textSize>amountMinTextSize || currencyTextPaint.textSize>currencyMinTextSize)) {
            if (!isAmountTextSizeFixed && amountTextPaint.textSize>amountTextSize) {
                amountTextPaint.textSize -= 1
                currencyTextPaint.textSize = if (amountTextPaint.textSize/2>currencyMinTextSize) amountTextPaint.textSize*percentOfCurrencyTextSize else currencyMinTextSize
            } else {
                currencyTextPaint.textSize -= 1
            }
        }
        if (amountToCurrencyMargin == 0F) amountToCurrencyMargin = getTextBounds(currencyCode.first().toString(), currencyTextPaint).width()/2F
    }

    private fun calculateTextPosition() {
        xStartPos = when(textHorizontalAlignment) {
            0 -> textPadding
            1 -> width/2F - getMoneyTextWidth()/2F
            2 -> width-textPadding-getMoneyTextWidth()
            else -> 0F
        }
        yStartPos = when(textVerticalAlignment) {
            0 -> textPadding+getMoneyTextHeight()
            1 -> height/2F+getMoneyTextHeight()/2F
            2 -> height-textPadding
            else -> 0F
        }
    }

    private fun initPaints(textPaint: TextPaint, textSize: Float, textColor: Int) {
        textPaint.color = textColor
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize
    }

    private fun getTextBounds(text: String, textPaint: TextPaint): Rect {
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    private fun getMoneyTextWidth(): Int {
        return getTextBounds(moneyAmount.toMoneyFormat(), amountTextPaint).width() + getTextBounds(currencyCode, currencyTextPaint).width()+amountToCurrencyMargin.toInt()
    }

    private fun getMoneyTextHeight(): Int {
        return getTextBounds(moneyAmount.toMoneyFormat(), amountTextPaint).height()
    }

    fun setValue(value: Double) {
        setValue(value, AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode] ?: Currency("USD", "United States Dollar", "$", 0.0))
    }

    fun setValue(value: Double, currency: Currency) {
        moneyAmount = value
        currencyCode = currency.symbol
    }
}