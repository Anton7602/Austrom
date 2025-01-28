package com.colleagues.austrom.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.BaseView
import com.colleagues.austrom.extensions.dpToPx
import com.colleagues.austrom.extensions.spToPx
import kotlin.math.min

class ActionButtonView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseView(context, attrs, defStyleAttr) {
    companion object {
        const val DEFAULT_VIEW_SIZE_WIDTH = 75
    }

    private var textPaint: TextPaint = TextPaint()
    private var textPaintChecked: TextPaint = TextPaint()
    private var backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundPaintChecked: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundColor: Int = context.getColor(R.color.backgroundText)
    private var backgroundColorChecked: Int = context.getColor(R.color.selectionColor)
    private var iconDrawable: Drawable? = AppCompatResources.getDrawable(context, R.drawable.ic_placeholder_icon)
    private var iconDrawableChecked: Drawable? = AppCompatResources.getDrawable(context, R.drawable.ic_placeholder_icon)
    private var iconTint: Int = Color.WHITE
    private var iconTintChecked: Int = Color.WHITE
    private var descriptionMargin: Float = context.dpToPx(12)
    private var text: String = ""
    private var textChecked: String = ""
    private var textColor: Int = context.getColor(R.color.secondaryTextColor)
    private var textColorChecked: Int = context.getColor(R.color.secondaryTextColor)
    private var textSize: Float = context.spToPx(12)
    private var iconSizePercent = 0.5f
    private var isCheckable: Boolean = false
    var isChecked: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    init {
        if (attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ActionButtonView)

            backgroundColor = typeArray.getColor(R.styleable.ActionButtonView_backgroundColor, backgroundColor)
            backgroundColorChecked = typeArray.getColor(R.styleable.ActionButtonView_checkedBackgroundColor, backgroundColorChecked)
            iconDrawable = typeArray.getDrawable(R.styleable.ActionButtonView_iconDrawable) ?: iconDrawable
            iconDrawableChecked = typeArray.getDrawable(R.styleable.ActionButtonView_checkedIconDrawable) ?: iconDrawableChecked
            iconTint = typeArray.getColor(R.styleable.ActionButtonView_iconTint, iconTint)
            iconTintChecked = typeArray.getColor(R.styleable.ActionButtonView_checkedIconTint, iconTintChecked)
            descriptionMargin = typeArray.getDimension(R.styleable.ActionButtonView_descriptionMargin, descriptionMargin)
            text = typeArray.getString(R.styleable.ActionButtonView_text) ?: text
            textChecked = typeArray.getString(R.styleable.ActionButtonView_checkedText) ?: textChecked
            textSize = typeArray.getDimension(R.styleable.ActionButtonView_textSize, textSize)
            isCheckable = typeArray.getBoolean(R.styleable.ActionButtonView_checkable, isCheckable)
            isChecked = typeArray.getBoolean(R.styleable.ActionButtonView_isChecked, isChecked)
            iconSizePercent = typeArray.getFloat(R.styleable.ActionButtonView_iconSizePercent, iconSizePercent)

            initPaints(textPaint, textSize, textColor)
            initPaints(backgroundPaint, backgroundColor)

            initPaints(textPaintChecked, textSize, textColorChecked)
            initPaints(backgroundPaintChecked, backgroundColorChecked)

            typeArray.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = min(width / 2f, (height-getTextHeight(text, textPaint)-descriptionMargin)/2f)
        if (!isChecked) {
            canvas.drawCircle(width/2f, radius, radius, backgroundPaint)
        } else {
            canvas.drawCircle(width/2f, radius, radius, backgroundPaintChecked)
        }

        iconDrawable?.let {
            it.setTint(if (isChecked) iconTintChecked else iconTint)
            val iconSize = (2*radius*iconSizePercent).toInt()
            it.setBounds(
                (width - iconSize) / 2,
                (radius.toInt()*2-iconSize)/2,
                (width + iconSize) / 2,
                (radius.toInt()*2+iconSize)/2
            )
            it.draw(canvas)
        }

        if (text.isNotEmpty()) {
            drawText(canvas,radius*2 + getTextHeight(text, textPaint)+descriptionMargin)
        }
//        val textWidth = textPaint.measureText(text)
//        canvas.drawText(text, (width - textWidth) / 2, radius*2 + getTextHeight(text, textPaint)+descriptionMargin, textPaint)
    }

    private fun drawText(canvas: Canvas, baseOffset: Float) {
        val textLines = text.split("\n")
        val paint = if (isChecked) textPaintChecked else textPaint
        var yOffset = baseOffset
        for (line in textLines) {
            val textWidth = paint.measureText(line)
            if (textWidth > width) {
                val words = line.split(" ")
                var currentLine = ""
                for (word in words) {
                    val testLine = "$currentLine $word".trim()
                    if (paint.measureText(testLine) > width) {
                        canvas.drawText(currentLine, (width - paint.measureText(currentLine)) / 2, yOffset, paint)
                        currentLine = word
                        yOffset += paint.textSize
                    } else {
                        currentLine = testLine
                    }
                }
                canvas.drawText(currentLine, (width - paint.measureText(currentLine)) / 2, yOffset, paint)
            } else {
                canvas.drawText(line, (width - textWidth) / 2, yOffset, paint)
            }
            yOffset -= paint.textSize
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val initSizeWidth = resolveDefaultWidth(widthMeasureSpec)
        val initSizeHeight = resolveDefaultHeight(heightMeasureSpec, initSizeWidth)
        setMeasuredDimension(initSizeWidth, initSizeHeight)
    }

    private fun resolveDefaultHeight(spec: Int, sizeWidth: Int): Int {
        return when(MeasureSpec.getMode(spec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            MeasureSpec.AT_MOST -> {
                if (text.isNotEmpty())
                    minOf((context.dpToPx(DEFAULT_VIEW_SIZE_WIDTH)+descriptionMargin+calculateWrappedTextHeight(text,textPaint,sizeWidth)).toInt(), MeasureSpec.getSize(spec))
                else
                    minOf(context.dpToPx(DEFAULT_VIEW_SIZE_WIDTH).toInt(), MeasureSpec.getSize(spec))
            }
            MeasureSpec.UNSPECIFIED -> (context.dpToPx(DEFAULT_VIEW_SIZE_WIDTH)+descriptionMargin+calculateWrappedTextHeight(text,textPaint,sizeWidth)).toInt()
            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun resolveDefaultWidth(spec: Int): Int {
        return when(MeasureSpec.getMode(spec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            MeasureSpec.AT_MOST ->  {minOf(context.dpToPx(DEFAULT_VIEW_SIZE_WIDTH).toInt(), MeasureSpec.getSize(spec)) }
            MeasureSpec.UNSPECIFIED -> context.dpToPx(DEFAULT_VIEW_SIZE_WIDTH).toInt()
            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun calculateWrappedTextHeight(text: String, textPaint: TextPaint, width: Int): Float {
        var height = textPaint.textSize;
        val textWidth = textPaint.measureText(text)
        if (textWidth > width) {
            val words = text.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = "$currentLine $word".trim()
                if (textPaint.measureText(testLine) > width) {
                    currentLine = word
                    height += textPaint.textSize
                } else {
                    currentLine = testLine
                }
            }
        }
        return height
    }

    fun setButtonBackgroundColor(color: Int) {
        backgroundColor = color
        invalidate()
    }

    fun setIconDrawable(drawable: Drawable) {
        iconDrawable = drawable
        invalidate()
    }

    fun setIconTint(color: Int) {
        iconTint = color
        invalidate()
    }

    fun setText(newText: String) {
        text = newText
        invalidate()
    }

    fun setCheckable(checkable: Boolean) {
        isCheckable = checkable
    }
}