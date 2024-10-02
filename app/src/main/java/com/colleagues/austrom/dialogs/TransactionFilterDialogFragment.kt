package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import com.colleagues.austrom.R
import com.colleagues.austrom.fragments.OpsFragment
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup

class TransactionFilterDialogFragment(private val filteredFragment: OpsFragment) : BottomSheetDialogFragment() {
    private lateinit var transactionTypeGroup: MaterialButtonToggleGroup
    private lateinit var incomeButton: Button
    private lateinit var transferButton: Button
    private lateinit var expenseButton: Button



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        val filterChangedListener = OnClickListener{
            filteredFragment.filterTransactions(generateFilter())
        }

        incomeButton.setOnClickListener(filterChangedListener)
        transferButton.setOnClickListener(filterChangedListener)
        expenseButton.setOnClickListener(filterChangedListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_transaction_filter, container, false)
    }

    private fun bindViews(view: View) {
        transactionTypeGroup = view.findViewById(R.id.trfildial_transactionType_tgr)
        incomeButton = view.findViewById(R.id.trfildial_income_btn)
        transferButton = view.findViewById(R.id.trfildial_transfer_btn)
        expenseButton = view.findViewById(R.id.trfildial_expense_btn)
        transactionTypeGroup.check(R.id.trfildial_income_btn)
        transactionTypeGroup.check(R.id.trfildial_transfer_btn)
        transactionTypeGroup.check(R.id.trfildial_expense_btn)
    }

    private fun generateFilter(): TransactionFilter {
        val availableTransactionTypes: MutableList<TransactionType> = mutableListOf()
        for (item in transactionTypeGroup.checkedButtonIds) {
            when (item) {
                R.id.trfildial_income_btn -> availableTransactionTypes.add(TransactionType.INCOME)
                R.id.trfildial_transfer_btn -> availableTransactionTypes.add(TransactionType.TRANSFER)
                R.id.trfildial_expense_btn -> availableTransactionTypes.add(TransactionType.EXPENSE)
            }
        }
        return TransactionFilter(
            transactionTypes = availableTransactionTypes
        )
    }
}

class TransactionFilter(val transactionTypes: List<TransactionType>) {

}