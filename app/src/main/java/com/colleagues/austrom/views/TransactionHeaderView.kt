package com.colleagues.austrom.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
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
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEach
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.dialogs.bottomsheetdialogs.AssetPickerDialogFragment
import com.colleagues.austrom.dialogs.bottomsheetdialogs.CategoryPickerDialogFragment
import com.colleagues.austrom.dialogs.bottomsheetdialogs.GroupBySelectionDialogFragment
import com.colleagues.austrom.dialogs.bottomsheetdialogs.NamePickerDialogFragment
import com.colleagues.austrom.extensions.dpToPx
import com.colleagues.austrom.extensions.toDayAndShortMonthNameFormat
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.TransactionFilter
import com.colleagues.austrom.models.TransactionGroupByType
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.shape.ShapeAppearanceModel
import java.time.LocalDate
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@SuppressLint("SetTextI18n")
class TransactionHeaderView (context: Context, attrs: AttributeSet) : CardView(context, attrs) {
    fun setOnDatesRequestedListener(l: (()->Unit)) { requestDates = l }
    private var requestDates: ()->Unit = {}
    fun setOnFilterChangedListener(l: ((TransactionFilter)->Unit)) { returnFilter = l }
    private var returnFilter: (TransactionFilter)->Unit = {}
    fun setOnGroupByTypeChangedListener(l: ((TransactionGroupByType)->Unit)) { returnGroupByType = l }
    private var returnGroupByType: (TransactionGroupByType)->Unit = {}

    fun setOnSortByTypeChangedListener(l: ((Boolean)->Unit)) { returnSortByType = l }
    private var returnSortByType: (Boolean)->Unit = {}

    fun setRequestDialogCall(l: ((BottomSheetDialogFragment)->Unit)) { showDialog = l }
    private var showDialog: (BottomSheetDialogFragment)->Unit = {}



    //region Binding
    private lateinit var incomeSumMoneyFormatTextView: MoneyFormatTextView
    private lateinit var expenseSumMoneyFormatTextView: MoneyFormatTextView
    private lateinit var holderCardView: CardView
    private lateinit var mainLayoutCardView: CardView
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var frameLayout: FrameLayout

    private lateinit var datesHeaderChip: Chip
    private lateinit var expenseHeaderChip: Chip
    private lateinit var incomeHeaderChip: Chip
    private lateinit var transferHeaderChip: Chip
    private lateinit var assetHeaderChip: Chip
    private lateinit var groupByChip: Chip
    private lateinit var sortByChip: Chip
    private lateinit var nameHeaderChip: Chip
    private fun bindViews(view: View) {
        incomeSumMoneyFormatTextView = view.findViewById(R.id.trlistheadview_income_monf)
        expenseSumMoneyFormatTextView = view.findViewById(R.id.trlistheadview_expense_monf)
        holderCardView = view.findViewById(R.id.trlistheadview_holder_crd)
        mainLayout = view.findViewById(R.id.trlistheadview_mainLayout_cly)
        mainLayoutCardView = view.findViewById(R.id.trlistheadview_mainHolder_cdv)
        frameLayout = view.findViewById(R.id.trlistheadview_frameLayout_fly)
        datesHeaderChip = view.findViewById(R.id.trlistheadview_dateHeader_chp)
        expenseHeaderChip = view.findViewById(R.id.trlistheadview_expenseHeader_chp)
        incomeHeaderChip = view.findViewById(R.id.trlistheadview_incomeHeader_chp)
        groupByChip = view.findViewById(R.id.trlistheadview_groupByHeader_chp)
        sortByChip = view.findViewById(R.id.trlistheadview_sortByHeader_chp)
        transferHeaderChip = view.findViewById(R.id.trlistheadview_transferHeader_chp)
        assetHeaderChip = view.findViewById(R.id.trlistheadview_assetHeader_chp)
        nameHeaderChip = view.findViewById(R.id.trlistheadview_nameHeader_chp)
    }
    //endregion
    private var incomeSum: Double = 0.0
    private var expenseSum: Double = 0.0
    private var currencySymbol: String = "$"
    private var transactionFilter: TransactionFilter = TransactionFilter(
        categories = mutableListOf(),
        assets = mutableListOf(),
        names = mutableListOf(),
        dateFrom = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()),
        dateTo = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())
    )

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

        mainLayoutCardView.setBackgroundResource(R.drawable.sh_transaction_header_background)

        datesHeaderChip.isCheckable = false
        datesHeaderChip.text = "${transactionFilter.dateFrom?.toDayAndShortMonthNameFormat()} - ${transactionFilter.dateTo?.toDayAndShortMonthNameFormat()}"
        datesHeaderChip.setOnClickListener { requestDates() }

        attributes.recycle()
        transactionFilter.categories.addAll(AustromApplication.activeCategories.values.map{ l-> l.categoryId})
        transactionFilter.assets.addAll(AustromApplication.activeAssets.values.map { l -> l.assetId })
        transactionFilter.assets.add(" ")

        expenseHeaderChip.setOnClickListener { chip -> launchCategoryPickerDialog(AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.EXPENSE }, chip as Chip)}
        incomeHeaderChip.setOnClickListener { chip -> launchCategoryPickerDialog(AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.INCOME }, chip as Chip)}
        transferHeaderChip.setOnClickListener { chip -> handleTransferHeaderChipClick() }
        assetHeaderChip.setOnClickListener { chip -> launchAssetPickerDialog() }
        groupByChip.setOnClickListener { chip -> launchGroupByPickerDialog() }
        sortByChip.setOnClickListener { chip -> returnSortByType(!sortByChip.isChecked) }
        nameHeaderChip.setOnClickListener { chip -> launchNamePickerDialog() }
    }

    private fun handleTransferHeaderChipClick() {
        if (transferHeaderChip.isChecked) transactionFilter.categories.add(AustromApplication.activeCategories.values.first { l -> l.transactionType==TransactionType.TRANSFER }.categoryId)
        else transactionFilter.categories.remove(AustromApplication.activeCategories.values.first { l -> l.transactionType==TransactionType.TRANSFER }.categoryId)
        returnFilter(transactionFilter)
    }

    private fun launchGroupByPickerDialog() {
        groupByChip.isChecked = true
        val dialog = GroupBySelectionDialogFragment()
        dialog.setOnDialogResultListener { groupByType -> returnGroupByType(groupByType) }
        showDialog(dialog)
    }

    private fun launchNamePickerDialog() {
        nameHeaderChip.isChecked = transactionFilter.names.isNotEmpty()
        val dialog = NamePickerDialogFragment(transactionFilter.names)
        dialog.setOnSelectionChangedListener { isSelected, name ->
            if (isSelected) transactionFilter.names.add(name) else transactionFilter.names.remove(name)
            nameHeaderChip.isChecked = transactionFilter.names.isNotEmpty()
            returnFilter(transactionFilter)
        }
        showDialog(dialog)
    }

    private fun launchAssetPickerDialog() {
        assetHeaderChip.isChecked = transactionFilter.assets.count()>1
        val dialog = AssetPickerDialogFragment(AustromApplication.activeAssets.values.associateBy({it}, {transactionFilter.assets.contains(it.assetId)}))
        dialog.setOnSelectionChangedListener { isSelected, asset ->
            if (isSelected)  transactionFilter.assets.add(asset.assetId) else transactionFilter.assets.remove(asset.assetId)
            assetHeaderChip.isChecked = transactionFilter.assets.count()>1
            returnFilter(transactionFilter)
        }
        showDialog(dialog)
    }

    private fun launchCategoryPickerDialog(categoriesList: List<Category>, headerChip: Chip) {
        headerChip.isChecked=evaluateCategoryHeaderChipState(categoriesList)
        val dialog = CategoryPickerDialogFragment(categoriesList.associateBy({it}, {transactionFilter.categories.contains(it.categoryId)}))
        dialog.setOnSelectionChangedListener { isSelected, category ->
            if (isSelected) transactionFilter.categories.add(category.categoryId) else transactionFilter.categories.remove(category.categoryId)
            headerChip.isChecked = evaluateCategoryHeaderChipState(categoriesList)
            returnFilter(transactionFilter)
        }
        showDialog(dialog)
    }

    private fun evaluateCategoryHeaderChipState(categoriesList: List<Category>): Boolean {
        val state = false
        categoriesList.forEach { category -> if (transactionFilter.categories.contains(category.categoryId)) return true }
        return state
    }

    fun setFilterDates(startDate: LocalDate, endDate: LocalDate) {
        transactionFilter.dateFrom = startDate
        transactionFilter.dateTo = endDate
        datesHeaderChip.text = "${transactionFilter.dateFrom?.toDayAndShortMonthNameFormat()} - ${transactionFilter.dateTo?.toDayAndShortMonthNameFormat()}"
        datesHeaderChip.isChecked = true
        returnFilter(transactionFilter)
    }

    fun getTransactionFilter(): TransactionFilter { return  transactionFilter }

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