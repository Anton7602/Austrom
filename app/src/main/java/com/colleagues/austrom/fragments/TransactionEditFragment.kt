package com.colleagues.austrom.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.AustromApplication.Companion.activeCategories
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

class TransactionEditFragment(private val transaction: Transaction, private val transactionsToChange: MutableList<Transaction> = mutableListOf()) : Fragment(R.layout.fragment_transaction_edit) {
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
    private lateinit var currencySymbolTextView: TextView
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
        currencySymbolTextView = view.findViewById(R.id.tredit_currencySymbol_txt)
    }
    //endregion
    private var selectedDate: LocalDate? = transaction.transactionDate
    private var selectedCategory: Category? = activeCategories.values
        .filter { l -> l.transactionType==transaction.transactionType() }
        .find { l -> l.categoryId == transaction.categoryId }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpTransaction()
        setUpAutofill()
        cancelButton.setOnClickListener { returnResult(transaction, transactionsToChange); setUpTransaction() }
        saveButton.setOnClickListener { save() }
        nameCheckButtonSingle.setOnClickListener { switchNameChangeSelection(nameCheckButtonSingle) }
        nameCheckButtonMultiple.setOnClickListener { switchNameChangeSelection(nameCheckButtonMultiple) }

        categoryCheckButtonSingle.setOnClickListener { switchCategoryChangeSelection(categoryCheckButtonSingle) }
        categoryCheckButtonMultipleByName.setOnClickListener { switchCategoryChangeSelection(categoryCheckButtonMultipleByName) }
        categoryCheckButtonMultipleByCategory.setOnClickListener { switchCategoryChangeSelection(categoryCheckButtonMultipleByCategory) }

        categorySelector.setOnClickListener { launchCategorySelectionDialog() }
        dateSelector.setOnClickListener { launchDateSelectionDialog() }

        nameTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nameChangeDescription.text = generateNameChangeTip()
                categoryChangeDescription.text = generateCategoryChangeTip()
            }
        })
    }

    private fun updateTransactionsList() {
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
        val dialog = CategorySelectionDialogFragment(transaction.transactionType())
        dialog.setOnDialogResultListener { category ->
            selectedCategory = category
            categorySelector.setFieldValue(category.name)
            categoryChangeDescription.text = generateCategoryChangeTip()
        }
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
        if (!isInputValid()) return
        updateTransactionsList()
        updateTransaction()
        returnResult(transaction, transactionsToChange)
    }

    private fun switchNameChangeSelection(pressedButton: ActionButtonView) {
        nameCheckButtonSingle.isChecked = pressedButton==nameCheckButtonSingle
        nameCheckButtonMultiple.isChecked = pressedButton==nameCheckButtonMultiple
        nameChangeDescription.text = generateNameChangeTip()
    }

    private fun generateNameChangeTip(): String {
        return if(nameCheckButtonSingle.isChecked) "The name of only this transaction will be changed"
        else if (nameCheckButtonMultiple.isChecked) "All ${transaction.transactionName} values will be changed to ${nameTextView.text}"
        else ""
    }

    private fun switchCategoryChangeSelection(pressedButton: ActionButtonView) {
        categoryCheckButtonSingle.isChecked = pressedButton==categoryCheckButtonSingle
        categoryCheckButtonMultipleByName.isChecked = pressedButton==categoryCheckButtonMultipleByName
        categoryCheckButtonMultipleByCategory.isChecked = pressedButton==categoryCheckButtonMultipleByCategory
        categoryChangeDescription.text = generateCategoryChangeTip()
    }

    private fun generateCategoryChangeTip(): String {
        return if (categoryCheckButtonSingle.isChecked) "The name of this category will be changed only"
        else if (categoryCheckButtonMultipleByName.isChecked) "All transaction with name ${nameTextView.text} will change category to ${categorySelector.getValue()}"
        else if (categoryCheckButtonMultipleByCategory.isChecked) "All transactions with category ${activeCategories[transaction.categoryId]?.name ?: transaction.categoryId } will change category to ${categorySelector.getValue()}"
        else ""
    }


    private fun setUpTransaction() {
        amountTextView.setText("%.2f".format(transaction.amount.absoluteValue))
        dateSelector.setFieldValue(transaction.transactionDate.toDayOfWeekAndShortDateFormat())
        nameTextView.setText(transaction.transactionName)
        categorySelector.setFieldValue(activeCategories[transaction.categoryId]?.name.toString())
        currencySymbolTextView.text = AustromApplication.activeAssets[transaction.assetId]?.currencyCode ?: "USD"
    }

    private fun setUpAutofill() {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        localDBProvider.getUniqueTransactionNamesAsync(transaction.transactionType()).observe(viewLifecycleOwner) { names ->
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
                        if (activeCategories.filter { l -> l.value.transactionType==TransactionType.INCOME }.containsKey(categoryId)) {
                            selectedCategory = activeCategories[categoryId]
                            if (selectedCategory!=null) categorySelector.setFieldValue(selectedCategory!!.name)
                        }
                    }
                    TransactionType.EXPENSE -> {
                        if (activeCategories.filter { l -> l.value.transactionType==TransactionType.EXPENSE }.containsKey(categoryId)) {
                            selectedCategory = activeCategories[categoryId]!!
                            if (selectedCategory!=null) categorySelector.setFieldValue(selectedCategory!!.name)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}