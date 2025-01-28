package com.colleagues.austrom.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEach
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.dpToPx
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.TransactionFilter
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.ShapeAppearanceModel
import java.time.LocalDate
import java.util.Locale

class TransactionHeaderView (context: Context, attrs: AttributeSet) : CardView(context, attrs) {
    fun setOnFilterChangedListener(l: ((TransactionFilter)->Unit)) { returnFilter = l }
    private var returnFilter: (TransactionFilter)->Unit = {}
    //region Binding
    private lateinit var incomeSumMoneyFormatTextView: MoneyFormatTextView
    private lateinit var expenseSumMoneyFormatTextView: MoneyFormatTextView
    private lateinit var filterButton: ImageButton
    private lateinit var holderCardView: CardView
    private lateinit var mainLayoutCardView: CardView
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var filterLayout: ConstraintLayout
    private lateinit var confirmFilterButton: ActionButtonView
    private lateinit var expenseChipGroup: ChipGroup
    private lateinit var incomeChipGroup: ChipGroup
    private lateinit var frameLayout: FrameLayout

    private lateinit var expenseChip: Chip
    private lateinit var incomeChip: Chip
    private lateinit var transferChip: Chip
    private fun bindViews(view: View) {
        incomeSumMoneyFormatTextView = view.findViewById(R.id.trlistheadview_income_monf)
        expenseSumMoneyFormatTextView = view.findViewById(R.id.trlistheadview_expense_monf)
        holderCardView = view.findViewById(R.id.trlistheadview_holder_crd)
        filterButton = view.findViewById(R.id.trlistheadview_filterButton_btn)
        mainLayout = view.findViewById(R.id.trlistheadview_mainLayout_cly)
        filterLayout = view.findViewById(R.id.trlistheadview_filterLayout_cly)
        confirmFilterButton = view.findViewById(R.id.trlistheadview_confirmFilter_abtn)
        mainLayoutCardView = view.findViewById(R.id.trlistheadview_mainHolder_cdv)
        expenseChipGroup = view.findViewById(R.id.trlistheadview_expenseCat_chg)
        incomeChipGroup = view.findViewById(R.id.trlistheadview_incomeCat_chg)
        frameLayout = view.findViewById(R.id.trlistheadview_frameLayout_fly)
        expenseChip = view.findViewById(R.id.trlistheadview_expenseFilter_chp)
        incomeChip = view.findViewById(R.id.trlistheadview_incomeFilter_chp)
        transferChip = view.findViewById(R.id.trlistheadview_transferFilter_chp)
    }
    //endregion
    private var incomeSum: Double = 0.0
    private var expenseSum: Double = 0.0
    private var currencySymbol: String = "$"
    private var collapsedHeight: Int = 0
    private var transactionFilter: TransactionFilter = TransactionFilter(mutableListOf(), LocalDate.now(), LocalDate.now().minusMonths(1))

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

        holderCardView.setBackgroundResource(R.drawable.sh_transaction_header_background)
        filterButton.setOnClickListener { switchToFilterMode() }
        confirmFilterButton.setOnClickListener { switchToMainMode() }

        attributes.recycle()
        setUpCategoriesInChips(AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.EXPENSE }, expenseChipGroup, expenseChip)
        setUpCategoriesInChips(AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.INCOME }, incomeChipGroup, incomeChip)
        transactionFilter.categories.add(AustromApplication.activeCategories.values.first{ l-> l.transactionType==TransactionType.TRANSFER}.categoryId)

        expenseChip.setOnClickListener {_ -> expenseChipGroup.children.forEach { view -> if (view is Chip) view.isChecked=expenseChip.isChecked }; returnFilter(transactionFilter)}
        incomeChip.setOnClickListener {_ -> incomeChipGroup.children.forEach { view -> if (view is Chip) view.isChecked=incomeChip.isChecked }; returnFilter(transactionFilter)}
    }

    fun getTransactionFilter(): TransactionFilter { return  transactionFilter }

    private fun setUpCategoriesInChips(categories: List<Category>, chipGroup: ChipGroup, chipHeader: Chip) {
        for (category in categories) {
            chipGroup.addView(Chip(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(context.dpToPx(8)).build()
                chipIcon = ContextCompat.getDrawable(context, category.imgReference.resourceId)
                chipBackgroundColor = (context.getColorStateList(R.color.chip_filter_background_color))
                setChipIconTintResource(R.color.chip_filter_text_color)
                setTextColor(context.getColorStateList(R.color.chip_filter_text_color))
                setPadding(0,context.dpToPx(0).toInt(),0,context.dpToPx(0).toInt())
                textAlignment = TEXT_ALIGNMENT_CENTER
                setEnsureMinTouchTargetSize(false)
                minimumHeight = 0
                text = category.name
                isChipIconVisible = true
                isCheckable = true
                isChecked = true
                tag = category.categoryId
                setOnClickListener { chip ->
                    if (this.isChecked) {
                        transactionFilter.categories.add(chip.tag.toString())
                        chipHeader.isChecked = true
                    } else {
                        transactionFilter.categories.remove(chip.tag.toString())
                        chipHeader.isChecked = false
                        chipGroup.children.forEach { view -> if (view is Chip && view.isChecked) chipHeader.isChecked = true; }
                    }
                    returnFilter(transactionFilter)
                }
            })
            transactionFilter.categories.add(category.categoryId)
        }
    }

    private fun switchToMainMode() {
        mainLayout.visibility = View.VISIBLE
        filterLayout.visibility = View.GONE
        animateHeightChange(holderCardView.height, collapsedHeight, true)
        returnFilter(transactionFilter)
    }

    private fun switchToFilterMode() {
        collapsedHeight = holderCardView.height
        mainLayout.visibility = View.GONE
        filterLayout.visibility = View.VISIBLE
        animateHeightChange(holderCardView.height, ((frameLayout.parent as ViewGroup).parent as ViewGroup).height, false)
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
        incomeSumMoneyFormatTextView.setValue(incomeSum, currencySymbol, true)
    }

    fun setExpense(value: Double) {
        expenseSum = value
        expenseSumMoneyFormatTextView.setValue(expenseSum, currencySymbol, true)
    }

    fun setCurrencySymbol(value: String) {
        currencySymbol = value
        incomeSumMoneyFormatTextView.setValue(incomeSum, currencySymbol)
        expenseSumMoneyFormatTextView.setValue(expenseSum, currencySymbol)
    }
}