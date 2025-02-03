package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.bottomsheetdialogs.CategorySelectionDialogFragment
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.ActionButtonView
import com.colleagues.austrom.views.SelectorButtonView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.absoluteValue

class TransactionEditFragment(private val transaction: Transaction? =null, private val transactionsToChange: MutableList<Transaction> = mutableListOf()) : Fragment(R.layout.fragment_transaction_edit) {
    constructor() : this(null)
    fun setOnDialogResultListener(l: ((transaction: Transaction, transactionList: List<Transaction>)->Unit)) { returnResult = l }
    private var returnResult: (Transaction, List<Transaction>)->Unit = {_,_ ->}
    //region Binding
    private lateinit var amountTextView: TextInputEditText
    private lateinit var dateSelector: SelectorButtonView
    private lateinit var nameTextView: AutoCompleteTextView
    private lateinit var nameChangeDescription: TextView
    private lateinit var nameCheckButtonSingle: ActionButtonView
    private lateinit var nameCheckButtonMultiple: ActionButtonView
    private lateinit var categorySelector: SelectorButtonView
    private lateinit var categoryChangeDescription: TextView
    private lateinit var categoryCheckButtonSingle: ActionButtonView
    private lateinit var categoryCheckButtonMultipleByName: ActionButtonView
    private lateinit var categoryCheckButtonMultipleByCategory: ActionButtonView
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private fun bindViews(view: View) {
        amountTextView = view.findViewById(R.id.tredit_amount_txt)
        dateSelector = view.findViewById(R.id.tredit_date_sel)
        nameTextView = view.findViewById(R.id.tredit_name_txt)
        nameChangeDescription = view.findViewById(R.id.tredit_nameChangeDescription_txt)
        nameCheckButtonSingle = view.findViewById(R.id.tredit_nameSingle_abtn)
        nameCheckButtonMultiple = view.findViewById(R.id.tredit_nameMultiple_abtn)
        categorySelector = view.findViewById(R.id.tredit_category_sel)
        categoryChangeDescription = view.findViewById(R.id.tredit_categoryChangeDescription_txt)
        categoryCheckButtonSingle = view.findViewById(R.id.tredit_categorySingle_abtn)
        categoryCheckButtonMultipleByName = view.findViewById(R.id.tredit_categoryMultipleByName_abtn)
        categoryCheckButtonMultipleByCategory = view.findViewById(R.id.tredit_categoryMultipleByCategory_abtn)
        cancelButton = view.findViewById(R.id.tredit_cancel_btn)
        saveButton = view.findViewById(R.id.tredit_save_btn)

    }
    //endregion
    private var selectedDate: LocalDate? = transaction?.transactionDate
    private var selectedCategory: Category? = AustromApplication.activeCategories.values
        .filter { l -> l.transactionType==transaction?.transactionType() }
        .find { l -> l.categoryId == transaction?.categoryId }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpTransaction()
        setUpAutofill()
        cancelButton.setOnClickListener { if (transaction!=null) returnResult(transaction, transactionsToChange) }
        saveButton.setOnClickListener {save()}
        nameCheckButtonSingle.setOnClickListener { switchNameChangeSelection(nameCheckButtonSingle) }
        nameCheckButtonMultiple.setOnClickListener { switchNameChangeSelection(nameCheckButtonMultiple) }

        categoryCheckButtonSingle.setOnClickListener { switchCategoryChangeSelection(categoryCheckButtonSingle) }
        categoryCheckButtonMultipleByName.setOnClickListener { switchCategoryChangeSelection(categoryCheckButtonMultipleByName) }
        categoryCheckButtonMultipleByCategory.setOnClickListener { switchCategoryChangeSelection(categoryCheckButtonMultipleByCategory) }

        categorySelector.setOnClickListener { launchCategorySelectionDialog() }
        dateSelector.setOnClickListener { launchDateSelectionDialog() }
    }

    private fun updateTransactionsList() {
        if (transaction==null) return
        val transactionName = transaction.transactionName
        val transactionCategoryId = transaction.categoryId
        transactionsToChange.forEach { massEditedTransaction ->
            if (nameCheckButtonMultiple.isChecked && massEditedTransaction.transactionName == transactionName) {
                massEditedTransaction.transactionName = nameTextView.text.toString()
            }
            if (categoryCheckButtonMultipleByCategory.isChecked && selectedCategory!=null && massEditedTransaction.categoryId == transactionCategoryId) {
                massEditedTransaction.categoryId = selectedCategory!!.categoryId
            }
            if (categoryCheckButtonMultipleByName.isChecked && selectedCategory!=null && massEditedTransaction.transactionName == transactionName) {
                massEditedTransaction.categoryId = selectedCategory!!.categoryId
            }
        }
    }

    private fun updateTransaction() {
        if (transaction==null) return
        val amount = amountTextView.text.toString().parseToDouble()?.absoluteValue ?: 0.0
        transaction.amount = if (transaction.transactionType()==TransactionType.EXPENSE) -amount else amount
        transaction.transactionName = nameTextView.text.toString()
        if (selectedDate!=null) transaction.transactionDate = selectedDate!!
        if (selectedCategory!=null) transaction.categoryId = selectedCategory!!.categoryId
    }

    private fun isInputValid(): Boolean {
        val amount = amountTextView.text.toString().parseToDouble()
        if (amount==null || amount>Double.MAX_VALUE) { amountTextView.error = requireActivity().getString(R.string.invalid_transaction_amount_provided); return false; }
        if (amount==0.0) { amountTextView.error = requireActivity().getString(R.string.transaction_amount_cannot_be_zero); return false; }
        val name = nameTextView.text.toString()
        if (name.isEmpty())  { nameTextView.error = getString(R.string.name_cannot_be_empty); return false }
        if (name.isEmpty())  { nameTextView.error = getString(R.string.transaction_name_is_too_long); return false }
        if (selectedDate==null) {Toast.makeText(requireActivity(), "Invalid Date Provided", Toast.LENGTH_LONG).show(); return false}
        if (selectedCategory==null) {Toast.makeText(requireActivity(), "Invalid Category Provided", Toast.LENGTH_LONG).show(); return false}
        return true
    }

    private fun launchCategorySelectionDialog() {
        if (transaction==null) return
        val dialog = CategorySelectionDialogFragment(transaction.transactionType())
        dialog.setOnDialogResultListener { category -> selectedCategory = category; categorySelector.setFieldValue(category.name) }
        dialog.show(requireActivity().supportFragmentManager, "CategorySelectionDialog")
    }

    private fun launchDateSelectionDialog() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(getString(R.string.choose_transaction_date)).setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            if (selectedDate!=null) {
                dateSelector.setFieldValue(selectedDate!!.toDayOfWeekAndShortDateFormat())
            }
        }
        datePicker.show(requireActivity().supportFragmentManager, "DatePicker Dialog")
    }

    private fun save() {
        if (transaction==null || !isInputValid()) return
        updateTransactionsList()
        updateTransaction()
        returnResult(transaction, transactionsToChange)
    }



    private fun switchNameChangeSelection(pressedButton: ActionButtonView) {
        if (transaction==null) return
        nameCheckButtonSingle.isChecked = pressedButton==nameCheckButtonSingle
        nameCheckButtonMultiple.isChecked = pressedButton==nameCheckButtonMultiple
        nameChangeDescription.text = when (pressedButton.id) {
            nameCheckButtonSingle.id -> { "The name of only this transaction will be changed"}
            nameCheckButtonMultiple.id -> { "All ${transaction.transactionName} values will be changed to ${nameTextView.text.toString()}" }
            else -> ""
        }
    }

    private fun switchCategoryChangeSelection(pressedButton: ActionButtonView) {
        if (transaction==null) return
        categoryCheckButtonSingle.isChecked = pressedButton==categoryCheckButtonSingle
        categoryCheckButtonMultipleByName.isChecked = pressedButton==categoryCheckButtonMultipleByName
        categoryCheckButtonMultipleByCategory.isChecked = pressedButton==categoryCheckButtonMultipleByCategory
        categoryChangeDescription.text = when(pressedButton.id) {
            categoryCheckButtonSingle.id -> "The name of this category will be changed only"
            categoryCheckButtonMultipleByName.id -> "All transaction with name ${nameTextView.text.toString()} will change category to ${categorySelector.getValue()}"
            categoryCheckButtonMultipleByCategory.id -> "All transactions with category ${transaction.categoryId} will change category to ${categorySelector.getValue()}"
            else -> ""
        }
    }


    private fun setUpTransaction() {
        if (transaction==null) return
        amountTextView.setText(transaction.amount.absoluteValue.toString())
        dateSelector.setFieldValue(transaction.transactionDate.toDayOfWeekAndShortDateFormat())
        nameTextView.setText(transaction.transactionName)
        categorySelector.setFieldValue(AustromApplication.activeCategories[transaction.categoryId]?.name.toString())
    }

    private fun setUpAutofill() {
        if (transaction==null) return
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        localDBProvider.getUniqueTransactionNamesAsync(transaction.transactionType()).observe(requireActivity()) { names ->
            try {
                val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_dropdown_item_1line, names)
                nameTextView.setAdapter(adapter)
                nameTextView.threshold = 2
            } catch (e: IllegalStateException) {
                return@observe
            }
            nameTextView.setOnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                val categoryId = localDBProvider.getTransactionNameMostUsedCategory(selectedItem)
                when (transaction.transactionType()) {
                    TransactionType.INCOME -> {
                        if (AustromApplication.activeCategories.filter { l -> l.value.transactionType==TransactionType.INCOME }.containsKey(categoryId)) {
                            selectedCategory = AustromApplication.activeCategories[categoryId]
                            if (selectedCategory!=null) categorySelector.setFieldValue(selectedCategory!!.name)
                        }
                    }
                    TransactionType.EXPENSE -> {
                        if (AustromApplication.activeCategories.filter { l -> l.value.transactionType==TransactionType.EXPENSE }.containsKey(categoryId)) {
                            selectedCategory = AustromApplication.activeCategories[categoryId]!!
                            if (selectedCategory!=null) categorySelector.setFieldValue(selectedCategory!!.name)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}