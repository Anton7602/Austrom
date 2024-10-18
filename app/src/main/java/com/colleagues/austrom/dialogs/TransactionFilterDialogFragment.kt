package com.colleagues.austrom.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.fragments.OpsFragment
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TransactionFilterDialogFragment(private val filteredFragment: OpsFragment) : BottomSheetDialogFragment() {
    private lateinit var dialogHolder: CardView
    private lateinit var transactionTypeGroup: MaterialButtonToggleGroup
    private lateinit var incomeButton: Button
    private lateinit var transferButton: Button
    private lateinit var expenseButton: Button
    private lateinit var expenseCategories: ChipGroup
    private lateinit var transferCategories: ChipGroup
    private lateinit var incomeCategories: ChipGroup
    private lateinit var chipFrom: Chip
    private lateinit var chipTo: Chip

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        setUpCategoriesInChips(AustromApplication.getActiveExpenseCategories(), expenseCategories)
        setUpCategoriesInChips(AustromApplication.getActiveTransferCategories(), transferCategories)
        setUpCategoriesInChips(AustromApplication.getActiveIncomeCategories(), incomeCategories)
        matchFilter()

        incomeButton.setOnClickListener {
            incomeCategories.isEnabled = (transactionTypeGroup.checkedButtonIds.contains(incomeButton.id))
            switchChipsEnabledState(incomeCategories)
            filteredFragment.filterTransactions(generateFilter())
        }

        transferButton.setOnClickListener {
            transferCategories.isEnabled = (transactionTypeGroup.checkedButtonIds.contains(transferButton.id))
            switchChipsEnabledState(transferCategories)
            filteredFragment.filterTransactions(generateFilter())
        }
        expenseButton.setOnClickListener {
            expenseCategories.isEnabled = (transactionTypeGroup.checkedButtonIds.contains(expenseButton.id))
            switchChipsEnabledState(expenseCategories)
            filteredFragment.filterTransactions(generateFilter())
        }
        chipFrom.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(getString(R.string.choose_period_start_date))
                .setSelection(if (chipFrom.tag ==null) MaterialDatePicker.todayInUtcMilliseconds() else
                    (chipFrom.tag as LocalDate).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                chipFrom.text = "${getString(R.string.from)} ${selectedDate.toDayOfWeekAndShortDateFormat()}"
                chipFrom.tag = selectedDate
                compareDates()
                filteredFragment.filterTransactions(generateFilter())
            }
            datePicker.show(requireActivity().supportFragmentManager, "DatePicker Dialog")
        }
        chipTo.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(getString(R.string.choose_period_end_date))
                .setSelection(if (chipTo.tag ==null) MaterialDatePicker.todayInUtcMilliseconds() else
                        (chipTo.tag as LocalDate).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                chipTo.text = "${getString(R.string.to)} ${selectedDate.toDayOfWeekAndShortDateFormat()}"
                chipTo.tag = selectedDate
                compareDates()
                filteredFragment.filterTransactions(generateFilter())
            }
            datePicker.show(requireActivity().supportFragmentManager, "DatePicker Dialog")}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_transaction_filter, container, false)
    }

    private fun switchChipsEnabledState(chipGroup: ChipGroup) {
        for (child in chipGroup.children) child.isEnabled = chipGroup.isEnabled
    }

    @SuppressLint("SetTextI18n")
    private fun matchFilter() {
        if (filteredFragment.activeFilter==null) {
            for (button in transactionTypeGroup.children) transactionTypeGroup.check(button.id)
            return
        }
        for (transactionType in filteredFragment.activeFilter!!.transactionTypes) {
            when (transactionType) {
                TransactionType.INCOME -> transactionTypeGroup.check(incomeButton.id)
                TransactionType.TRANSFER -> transactionTypeGroup.check(transferButton.id)
                TransactionType.EXPENSE -> transactionTypeGroup.check(expenseButton.id)
            }
        }
        incomeCategories.isEnabled = (transactionTypeGroup.checkedButtonIds.contains(incomeButton.id))
        switchChipsEnabledState(incomeCategories)
        for (child in incomeCategories.children) {
            (child as Chip).isChecked = filteredFragment.activeFilter?.categories!!.contains(child.text)
        }
        transferCategories.isEnabled = (transactionTypeGroup.checkedButtonIds.contains(transferButton.id))
        switchChipsEnabledState(transferCategories)
        for (child in transferCategories.children) {
            (child as Chip).isChecked = filteredFragment.activeFilter?.categories!!.contains(child.text)
        }
        expenseCategories.isEnabled = (transactionTypeGroup.checkedButtonIds.contains(expenseButton.id))
        switchChipsEnabledState(expenseCategories)
        for (child in expenseCategories.children) {
            (child as Chip).isChecked = filteredFragment.activeFilter?.categories!!.contains(child.text)
        }
        if (filteredFragment.activeFilter!!.dateFrom!=null) {
            chipFrom.tag  = filteredFragment.activeFilter!!.dateFrom
            chipFrom.text = "${getString(R.string.from)}: ${filteredFragment.activeFilter!!.dateFrom!!.toDayOfWeekAndShortDateFormat()}"
        }
        if (filteredFragment.activeFilter!!.dateTo!=null) {
            chipTo.tag  = filteredFragment.activeFilter!!.dateTo
            chipTo.text = "${getString(R.string.to)}: ${filteredFragment.activeFilter!!.dateTo!!.toDayOfWeekAndShortDateFormat()}"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun compareDates() {
        if (chipFrom.tag==null || chipTo.tag==null) return
        val dateFrom = (chipFrom.tag as LocalDate)
        val dateTo = (chipTo.tag as LocalDate)
        if (dateFrom > dateTo) {
            chipFrom.tag = dateTo
            chipFrom.text = "${getString(R.string.from)}: ${dateTo.toDayOfWeekAndShortDateFormat()}"
            chipTo.tag = dateFrom
            chipTo.text = "${getString(R.string.to)}: ${dateFrom.toDayOfWeekAndShortDateFormat()}"
        }
    }

    private fun generateFilter(): TransactionFilter {
        val availableTransactionTypes: MutableList<TransactionType> = mutableListOf()
        for (item in transactionTypeGroup.checkedButtonIds) {
            when (item) {
                incomeButton.id -> availableTransactionTypes.add(TransactionType.INCOME)
                transferButton.id -> availableTransactionTypes.add(TransactionType.TRANSFER)
                expenseButton.id -> availableTransactionTypes.add(TransactionType.EXPENSE)
            }
        }
        val categories: MutableList<String> = mutableListOf()
        for (child in expenseCategories.children) {
            val chip = child as Chip
            if (chip.isEnabled && chip.isChecked) {
                categories.add(chip.text.toString())
            }
        }
        for (child in transferCategories.children) {
            val chip = child as Chip
            if (chip.isEnabled && chip.isChecked) {
                categories.add(chip.text.toString())
            }
        }
        for (child in incomeCategories.children) {
            val chip = child as Chip
            if (chip.isEnabled && chip.isChecked) {
                categories.add(chip.text.toString())
            }
        }
        return TransactionFilter(
            transactionTypes = availableTransactionTypes,
            categories = categories,
            dateFrom = if (chipFrom.tag!=null) (chipFrom.tag as LocalDate) else null,
            dateTo = if (chipTo.tag!=null) (chipTo.tag as LocalDate) else null
        )
    }

    private fun setUpCategoriesInChips(categories: List<Category>, chipGroup: ChipGroup) {
        for (category in categories) {
            val chip = Chip(requireActivity())
            chip.text = category.name
            chip.chipIcon = ContextCompat.getDrawable(requireActivity(), category.imgReference?.resourceId ?: R.drawable.ic_placeholder_icon)
            chip.setEnsureMinTouchTargetSize(false)
            chip.isCheckable = true
            chip.isChecked = true
            chip.setOnClickListener {
                filteredFragment.filterTransactions(generateFilter())
            }
            chipGroup.addView(chip)
        }
    }

    private fun bindViews(view: View) {
        transactionTypeGroup = view.findViewById(R.id.trfildial_transactionType_tgr)
        incomeButton = view.findViewById(R.id.trfildial_income_btn)
        transferButton = view.findViewById(R.id.trfildial_transfer_btn)
        expenseButton = view.findViewById(R.id.trfildial_expense_btn)
        expenseCategories = view.findViewById(R.id.trfildial_expenseCat_cgr)
        transferCategories = view.findViewById(R.id.trfildial_transferCat_cgr)
        incomeCategories = view.findViewById(R.id.trfildial_incomeCat_cgr)
        chipFrom = view.findViewById(R.id.trfildial_dateFrom_chp)
        chipTo = view.findViewById(R.id.trfildial_dateTo_chp)
        dialogHolder = view.findViewById(R.id.trfildial_holder_crv)
    }
}

class TransactionFilter(val transactionTypes: List<TransactionType>,
                        val categories: List<String>,
                        val dateFrom: LocalDate?,
                        val dateTo: LocalDate?)