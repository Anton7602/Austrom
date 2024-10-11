package com.colleagues.austrom.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.TransactionGroupRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.TransactionCreationDialogFragment
import com.colleagues.austrom.dialogs.TransactionFilter
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OpsFragment : Fragment(R.layout.fragment_ops) {
    private lateinit var transactionHolder: RecyclerView
    private lateinit var addNewTransactionButton: FloatingActionButton
    private lateinit var addIncomeButton: FloatingActionButton
    private lateinit var addExpenseButton: FloatingActionButton
    private lateinit var addTransferButton: FloatingActionButton
    private var transactionList: MutableList<Transaction> = mutableListOf()
    var activeFilter: TransactionFilter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        updateTransactionsList()

        addNewTransactionButton.setOnClickListener {
            switchTransactionTypesButtonsVisibility()
        }

        addIncomeButton.setOnClickListener {
            switchTransactionTypesButtonsVisibility()
            TransactionCreationDialogFragment(this, TransactionType.INCOME).show(requireActivity().supportFragmentManager, "Transaction Creation Dialog")
        }

        addExpenseButton.setOnClickListener {
            switchTransactionTypesButtonsVisibility()
            TransactionCreationDialogFragment(this, TransactionType.EXPENSE).show(requireActivity().supportFragmentManager, "Transaction Creation Dialog")
        }

        addTransferButton.setOnClickListener {
            switchTransactionTypesButtonsVisibility()
            TransactionCreationDialogFragment(this, TransactionType.TRANSFER).show(requireActivity().supportFragmentManager, "Transaction Creation Dialog")
        }
    }

    override fun onResume() {
        super.onResume()
        updateTransactionsList()
    }

    fun updateTransactionsList() {
        val provider : IDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
        val user = AustromApplication.appUser
        if (user !=null) {
            transactionList = if (user.activeBudgetId != null) {
                val budget = provider.getBudgetById(user.activeBudgetId!!)
                if (budget!=null) {
                    provider.getTransactionsOfBudget(budget)
                } else {
                    provider.getTransactionsOfUser(user)
                }
            } else {
                provider.getTransactionsOfUser(user)
            }
            if (transactionList.isNotEmpty()) {
                setUpRecyclerView(transactionList)
            }
        }
    }

    fun filterTransactions(filter: TransactionFilter) {
        activeFilter = filter
        var filteredTransactions: MutableList<Transaction> = mutableListOf()
        if (filter.transactionTypes.contains(TransactionType.INCOME))
            filteredTransactions.addAll(transactionList.filter { entry -> entry.sourceId==null })
        if (filter.transactionTypes.contains(TransactionType.TRANSFER))
            filteredTransactions.addAll(transactionList.filter  {entry -> entry.sourceId!=null && entry.targetId!=null })
        if (filter.transactionTypes.contains(TransactionType.EXPENSE))
            filteredTransactions.addAll(transactionList.filter { entry -> entry.targetId==null })
        filteredTransactions = (filteredTransactions.filter { entry -> filter.categories.contains(entry.categoryId) }).toMutableList()
        if (filter.dateFrom!=null) {
            filteredTransactions = (filteredTransactions.filter { entry -> entry.transactionDate!! >= filter.dateFrom }).toMutableList()
        }
        if (filter.dateTo!=null) {
            filteredTransactions = (filteredTransactions.filter { entry -> entry.transactionDate!! <= filter.dateTo }).toMutableList()
        }
        setUpRecyclerView(filteredTransactions)
    }

    private fun switchTransactionTypesButtonsVisibility() {
        addIncomeButton.visibility = if (addIncomeButton.visibility==View.VISIBLE) {View.GONE} else {View.VISIBLE}
        addExpenseButton.visibility = if (addExpenseButton.visibility==View.VISIBLE) {View.GONE} else {View.VISIBLE}
        addTransferButton.visibility = if (addTransferButton.visibility==View.VISIBLE) {View.GONE} else {View.VISIBLE}
        if (addIncomeButton.visibility == View.VISIBLE) {
            addNewTransactionButton.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_navigation_close_temp))
        } else {
            addNewTransactionButton.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_navigation_add_temp))
        }
    }

    private fun setUpRecyclerView(transactionList: MutableList<Transaction>) {
        val groupedTransactions = Transaction.groupTransactionsByDate(transactionList)
        transactionHolder.layoutManager = LinearLayoutManager(activity)
        transactionHolder.adapter = TransactionGroupRecyclerAdapter(groupedTransactions, (requireActivity() as AppCompatActivity))
    }

    private fun bindViews(view: View) {
        transactionHolder = view.findViewById(R.id.ops_transactionHolder_rcv)
        addNewTransactionButton = view.findViewById(R.id.ops_addNew_fab)
        addIncomeButton = view.findViewById(R.id.ops_income_fab)
        addExpenseButton = view.findViewById(R.id.ops_expense_fab)
        addTransferButton = view.findViewById(R.id.ops_transfer_fab)
    }
}