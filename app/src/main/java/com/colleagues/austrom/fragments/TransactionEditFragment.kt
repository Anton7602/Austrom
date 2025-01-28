package com.colleagues.austrom.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.views.ActionButtonView
import com.colleagues.austrom.views.SelectorButtonView
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.absoluteValue

class TransactionEditFragment(val transaction: Transaction? =null, val transactionsToChange: List<Transaction>? = null) : Fragment(R.layout.fragment_transaction_edit) {
    constructor() : this(null, null)
    fun setOnDialogResultListener(l: ((Boolean)->Unit)) { returnResult = l }
    private var returnResult: (Boolean)->Unit = {}
    //region Binding
    private lateinit var amountTextView: TextInputEditText
    private lateinit var dateSelector: SelectorButtonView
    private lateinit var nameTextView: TextInputEditText
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpTransaction()
        cancelButton.setOnClickListener { returnResult(false) }
        saveButton.setOnClickListener {save()}
        nameCheckButtonSingle.setOnClickListener { switchNameChangeSelection(nameCheckButtonSingle) }
        nameCheckButtonMultiple.setOnClickListener { switchNameChangeSelection(nameCheckButtonMultiple) }

        categoryCheckButtonSingle.setOnClickListener { switchCategoryChangeSelection(categoryCheckButtonSingle) }
        categoryCheckButtonMultipleByName.setOnClickListener { switchCategoryChangeSelection(categoryCheckButtonMultipleByName) }
        categoryCheckButtonMultipleByCategory.setOnClickListener { switchCategoryChangeSelection(categoryCheckButtonMultipleByCategory) }
    }

    private fun save() {
        returnResult(true)
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
        categorySelector.setFieldValue(transaction.categoryId)
    }
}