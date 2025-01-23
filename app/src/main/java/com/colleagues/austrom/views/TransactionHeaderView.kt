package com.colleagues.austrom.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.colleagues.austrom.R

class TransactionHeaderView (context: Context, attrs: AttributeSet) : CardView(context, attrs) {
    //region Binding
    private lateinit var incomeSumMoneyFormatTextView: MoneyFormatTextView
    private lateinit var expenseSumMoneyFormatTextView: MoneyFormatTextView
    private lateinit var filterButton: ImageButton
    private lateinit var holderCardView: CardView
    private lateinit var mainLayoutCardView: CardView
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var filterLayout: ConstraintLayout
    private lateinit var confirmFilterButton: ActionButtonView
    private fun bindViews(view: View) {
        incomeSumMoneyFormatTextView = view.findViewById(R.id.trlistheadview_income_monf)
        expenseSumMoneyFormatTextView = view.findViewById(R.id.trlistheadview_expense_monf)
        holderCardView = view.findViewById(R.id.trlistheadview_holder_crd)
        filterButton = view.findViewById(R.id.trlistheadview_filterButton_btn)
        mainLayout = view.findViewById(R.id.trlistheadview_mainLayout_cly)
        filterLayout = view.findViewById(R.id.trlistheadview_filterLayout_cly)
        confirmFilterButton = view.findViewById(R.id.trlistheadview_confirmFilter_abtn)
        mainLayoutCardView = view.findViewById(R.id.trlistheadview_mainHolder_cdv)
    }
    //endregion
    private var incomeSum: Double = 0.0
    private var expenseSum: Double = 0.0
    private var currencySymbol: String = "$"
    private var collapsedHeight: Int = 0

    companion object {
        private const val ANIMATION_DURATION_MS: Long = 300
    }

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.view_transaction_list_header, this, true)
        bindViews(view)

        val attributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.TransactionHeaderView)
        incomeSum = attributes.getFloat(R.styleable.TransactionHeaderView_IncomeSum, 0f).toDouble()
        expenseSum = attributes.getFloat(R.styleable.TransactionHeaderView_ExpenseSum, 0f).toDouble()
        currencySymbol = attributes.getString(R.styleable.TransactionHeaderView_CurrencySymbol) ?: "$"

        incomeSumMoneyFormatTextView.setValue(incomeSum, currencySymbol)
        expenseSumMoneyFormatTextView.setValue(expenseSum, currencySymbol)

        //holderCardView.setBackgroundResource(R.drawable.img_transaction_header_card_background)
        holderCardView.setBackgroundResource(R.drawable.sh_transaction_header_background)
        filterButton.setOnClickListener { switchToFilterMode() }
        confirmFilterButton.setOnClickListener { switchToMainMode() }

        attributes.recycle()
    }

    private fun switchToMainMode() {
        mainLayout.visibility = View.VISIBLE
        filterLayout.visibility = View.GONE
        animateHeightChange(holderCardView.height, collapsedHeight, true)
    }

    private fun switchToFilterMode() {
        collapsedHeight = holderCardView.height
        mainLayout.visibility = View.GONE
        filterLayout.visibility = View.VISIBLE
        animateHeightChange(collapsedHeight, mainLayout.height*4, false)
    }

    private fun animateHeightChange(startHeight: Int, endHeight: Int, isSwitchingToMainMode: Boolean) {
        val resizeAnimator = ValueAnimator.ofInt(startHeight, endHeight)
        resizeAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            val layoutParams = holderCardView.layoutParams
            layoutParams.height = animatedValue
            holderCardView.layoutParams = layoutParams
        }
        resizeAnimator.duration = ANIMATION_DURATION_MS
        resizeAnimator.interpolator = AccelerateDecelerateInterpolator()
        resizeAnimator.start()
    }

    fun setIncome(value: Double) {
        incomeSum = value
        incomeSumMoneyFormatTextView.setValue(incomeSum, currencySymbol)
    }

    fun setExpense(value: Double) {
        expenseSum = value
        expenseSumMoneyFormatTextView.setValue(expenseSum, currencySymbol)
    }

    fun setCurrencySymbol(value: String) {
        currencySymbol = value
        incomeSumMoneyFormatTextView.setValue(incomeSum, currencySymbol)
        expenseSumMoneyFormatTextView.setValue(expenseSum, currencySymbol)
    }
}