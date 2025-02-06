package com.colleagues.austrom.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.getFirstDayOfMonth
import com.colleagues.austrom.extensions.getFirstDayOfWeek
import com.colleagues.austrom.extensions.getFirstDayOfYear
import com.colleagues.austrom.extensions.getLastDayOfMonth
import com.colleagues.austrom.extensions.getLastDayOfWeek
import com.colleagues.austrom.extensions.getLastDayOfYear
import com.colleagues.austrom.extensions.getLocalizedMonthName
import com.colleagues.austrom.extensions.getLocalizedWeekName
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateControllerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    //region Binding
    private lateinit var periodTypeTextView: TextView
    private lateinit var periodNameTextView: TextView
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private fun bindViews(view: View) {
        periodTypeTextView = view.findViewById(R.id.datecontview_periodType_txt)
        periodNameTextView = view.findViewById(R.id.datecontview_periodValue_txt)
        previousButton = view.findViewById(R.id.datecontview_prev_btn)
        nextButton = view.findViewById(R.id.datecontview_next_btn)
    }
    //endregion
    private var selectedStartDate: LocalDate = LocalDate.now()
    private var selectedEndDate: LocalDate = LocalDate.now()
    private var selectedPeriodType: PeriodType = PeriodType.MONTH

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.view_date_controller, this, true)
        bindViews(view)

        val attributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.DateControllerView)
        selectedPeriodType = PeriodType.entries.getOrNull(attributes.getInt(R.styleable.DateControllerView_periodType, 1)) ?: selectedPeriodType

        attributes.recycle()

        setPeriodType(selectedPeriodType)
        setDate(selectedStartDate)
        previousButton.setOnClickListener { decreaseDate(selectedPeriodType) }
        nextButton.setOnClickListener { increaseDate(selectedPeriodType) }
    }

    fun setDate(newStartDate: LocalDate, newEndDate: LocalDate? = null) {
        selectedStartDate = newStartDate
        setUpDateVisibleName()
    }

    fun setPeriodType(periodType: PeriodType) {
        when (periodType) {
            PeriodType.WEEK -> { periodTypeTextView.text = context.getString(R.string.week) }
            PeriodType.MONTH -> { periodTypeTextView.text = context.getString(R.string.month) }
            PeriodType.YEAR -> { periodTypeTextView.text = context.getString(R.string.year) }
            PeriodType.CUSTOM -> { periodTypeTextView.text = context.getString(R.string.period) }
        }
    }

    fun getSelectedDatesRange(): Pair<LocalDate, LocalDate> {
        return when(selectedPeriodType) {
            PeriodType.WEEK -> { Pair(selectedStartDate.getFirstDayOfWeek(), selectedStartDate.getLastDayOfWeek()) }
            PeriodType.MONTH -> { Pair(selectedStartDate.getFirstDayOfMonth(), selectedStartDate.getLastDayOfMonth()) }
            PeriodType.YEAR -> { Pair(selectedStartDate.getFirstDayOfYear(), selectedStartDate.getLastDayOfYear()) }
            PeriodType.CUSTOM -> { Pair(selectedStartDate, selectedEndDate) }
        }
    }

    private fun setUpDateVisibleName() {
        when (selectedPeriodType) {
            PeriodType.WEEK -> { periodNameTextView.text = selectedStartDate.getLocalizedWeekName(context) }
            PeriodType.MONTH -> { periodNameTextView.text = selectedStartDate.getLocalizedMonthName() }
            PeriodType.YEAR -> { periodNameTextView.text = selectedStartDate.format(DateTimeFormatter.ofPattern("yyyy")) }
            PeriodType.CUSTOM -> { periodNameTextView.text = "" }
        }
    }

    private fun decreaseDate(periodType: PeriodType) {
        selectedStartDate = when(periodType) {
            PeriodType.WEEK -> { selectedStartDate.minusWeeks(1) }
            PeriodType.MONTH -> { selectedStartDate.minusMonths(1) }
            PeriodType.YEAR -> { selectedStartDate.minusYears(1) }
            PeriodType.CUSTOM -> { selectedStartDate }
        }
        setDate(selectedStartDate)
    }

    private fun increaseDate(periodType: PeriodType) {
        selectedStartDate = when(periodType) {
            PeriodType.WEEK -> { selectedStartDate.plusWeeks(1) }
            PeriodType.MONTH -> { selectedStartDate.plusMonths(1) }
            PeriodType.YEAR -> { selectedStartDate.plusYears(1) }
            PeriodType.CUSTOM -> { selectedStartDate }
        }
        setDate(selectedStartDate)
    }
}

enum class PeriodType{
    WEEK, MONTH, YEAR, CUSTOM
}