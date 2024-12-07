package com.colleagues.austrom.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.TransactionCreationActivity
import com.colleagues.austrom.adapters.TransactionGroupRecyclerAdapter
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.TransactionCreationDialogFragment
import com.colleagues.austrom.dialogs.TransactionFilter
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.TransactionHeaderView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OpsFragment : Fragment(R.layout.fragment_ops), IDialogInitiator {
    private lateinit var transactionHolder: RecyclerView
    private lateinit var transactionsHeader: TransactionHeaderView
    private lateinit var addNewTransactionButton: FloatingActionButton
    private lateinit var addIncomeButton: FloatingActionButton
    private lateinit var addExpenseButton: FloatingActionButton
    private lateinit var addTransferButton: FloatingActionButton
    private lateinit var createNewTransactionButton: ImageButton
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
            //TransactionCreationDialogFragment2(TransactionType.INCOME, this, ).show(requireActivity().supportFragmentManager, "Transaction Creation Dialog")
            TransactionCreationDialogFragment(this, TransactionType.INCOME).show(requireActivity().supportFragmentManager, "Transaction Creation Dialog")
        }

        addExpenseButton.setOnClickListener {
            switchTransactionTypesButtonsVisibility()
            //TransactionCreationDialogFragment2(TransactionType.EXPENSE, this).show(requireActivity().supportFragmentManager, "Transaction Creation Dialog")
            TransactionCreationDialogFragment(this, TransactionType.EXPENSE).show(requireActivity().supportFragmentManager, "Transaction Creation Dialog")
        }

        addTransferButton.setOnClickListener {
            switchTransactionTypesButtonsVisibility()
            //TransactionCreationDialogFragment2(TransactionType.TRANSFER, this).show(requireActivity().supportFragmentManager, "Transaction Creation Dialog")
            TransactionCreationDialogFragment(this, TransactionType.TRANSFER).show(requireActivity().supportFragmentManager, "Transaction Creation Dialog")
        }

        createNewTransactionButton.setOnClickListener {
            startActivity(Intent(requireActivity(), TransactionCreationActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateTransactionsList()
    }

    fun updateTransactionsList() {
        val provider = LocalDatabaseProvider(requireActivity())
        val user = AustromApplication.appUser
        if (user !=null) {
            transactionList = if (user.activeBudgetId != null) {
                //val budget = provider.getBudgetById(user.activeBudgetId!!)
                val budget: Budget? = null
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
        var incomeSum = 0.0
        var expenseSum = 0.0
        transactionList.forEach{ transaction ->
            if (transaction.transactionType() == TransactionType.EXPENSE) {
                val transactionsAsset = provider.getAssetById(transaction.assetId)
                if (transactionsAsset!=null) {
                    expenseSum+= if (transactionsAsset.currencyCode==AustromApplication.appUser!!.baseCurrencyCode) transaction.amount else transaction.amount/(AustromApplication.activeCurrencies[transactionsAsset.currencyCode]?.exchangeRate ?: 1.0)
                }
            }
            if (transaction.transactionType() == TransactionType.INCOME) {
                val transactionsAsset = provider.getAssetById(transaction.assetId)
                if (transactionsAsset!=null) {
                    incomeSum+= if (transactionsAsset.currencyCode==AustromApplication.appUser!!.baseCurrencyCode) transaction.amount else transaction.amount/(AustromApplication.activeCurrencies[transactionsAsset.currencyCode]?.exchangeRate ?: 1.0)
                }
            }
        }
        transactionsHeader.setIncome(incomeSum)
        transactionsHeader.setExpense(expenseSum)
        transactionsHeader.setCurrencySymbol(AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode]!!.symbol)
    }

    fun filterTransactions(filter: TransactionFilter) {
        activeFilter = filter
        var filteredTransactions: MutableList<Transaction> = mutableListOf()
        if (filter.transactionTypes.contains(TransactionType.INCOME))
            filteredTransactions.addAll(transactionList.filter { entry -> entry.transactionType()==TransactionType.INCOME })
        if (filter.transactionTypes.contains(TransactionType.TRANSFER))
            filteredTransactions.addAll(transactionList.filter  {entry -> entry.transactionType()==TransactionType.TRANSFER })
        if (filter.transactionTypes.contains(TransactionType.EXPENSE))
            filteredTransactions.addAll(transactionList.filter { entry -> entry.transactionType() == TransactionType.EXPENSE})
        filteredTransactions = (filteredTransactions.filter { entry -> filter.categories.contains(entry.categoryId) }).toMutableList()
        if (filter.dateFrom!=null) {
            filteredTransactions = (filteredTransactions.filter { entry -> entry.transactionDate >= filter.dateFrom }).toMutableList()
        }
        if (filter.dateTo!=null) {
            filteredTransactions = (filteredTransactions.filter { entry -> entry.transactionDate <= filter.dateTo }).toMutableList()
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

    override fun receiveValue(value: String, valueType: String) {
        TODO("Not yet implemented")
    }

    private fun bindViews(view: View) {
        transactionHolder = view.findViewById(R.id.ops_transactionHolder_rcv)
        addNewTransactionButton = view.findViewById(R.id.ops_addNew_fab)
        addIncomeButton = view.findViewById(R.id.ops_income_fab)
        addExpenseButton = view.findViewById(R.id.ops_expense_fab)
        addTransferButton = view.findViewById(R.id.ops_transfer_fab)
        transactionsHeader = view.findViewById(R.id.ops_transactionsHeader_trhed)
        createNewTransactionButton = view.findViewById(R.id.ops_createNewTransaction_btn)
    }
}