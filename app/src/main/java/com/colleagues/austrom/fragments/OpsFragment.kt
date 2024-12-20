package com.colleagues.austrom.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.TransactionCreationActivity
import com.colleagues.austrom.TransactionPropertiesActivity
import com.colleagues.austrom.adapters.TransactionGroupRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.TransactionFilter
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.TransactionHeaderView

class OpsFragment : Fragment(R.layout.fragment_ops){
    //region Binding
    private lateinit var transactionHolder: RecyclerView
    private lateinit var transactionsHeader: TransactionHeaderView
    private lateinit var createNewTransactionButton: ImageButton
    private fun bindViews(view: View) {
        transactionHolder = view.findViewById(R.id.ops_transactionHolder_rcv)
        transactionsHeader = view.findViewById(R.id.ops_transactionsHeader_trhed)
        createNewTransactionButton = view.findViewById(R.id.ops_createNewTransaction_btn)
    }
    //endregion
    private var transactionList: MutableList<Transaction> = mutableListOf()
    var activeFilter: TransactionFilter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        updateTransactionsList()
        createNewTransactionButton.setOnClickListener { startActivity(Intent(requireActivity(), TransactionCreationActivity::class.java)) }
    }

    private fun updateTransactionsList() {
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
        calculateTransactionsAmountSums(provider)
        transactionsHeader.setCurrencySymbol(AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode]!!.symbol)
    }

    private fun calculateTransactionsAmountSums(dbProvider: LocalDatabaseProvider) {
        var incomeSum = 0.0
        var expenseSum = 0.0
        transactionList.forEach{ transaction ->
            val transactionsAsset = AustromApplication.activeAssets[transaction.assetId]
            if (transaction.transactionType() == TransactionType.EXPENSE) {
                if (transactionsAsset!=null) {
                    expenseSum+= if (transactionsAsset.currencyCode==AustromApplication.appUser!!.baseCurrencyCode) transaction.amount else transaction.amount/(AustromApplication.activeCurrencies[transactionsAsset.currencyCode]?.exchangeRate ?: 1.0)
                }
            }
            if (transaction.transactionType() == TransactionType.INCOME) {
                if (transactionsAsset!=null) {
                    incomeSum+= if (transactionsAsset.currencyCode==AustromApplication.appUser!!.baseCurrencyCode) transaction.amount else transaction.amount/(AustromApplication.activeCurrencies[transactionsAsset.currencyCode]?.exchangeRate ?: 1.0)
                }
            }
        }
        transactionsHeader.setIncome(incomeSum)
        transactionsHeader.setExpense(expenseSum)
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

    private fun setUpRecyclerView(transactionList: MutableList<Transaction>) {
        val groupedTransactions = Transaction.groupTransactionsByDate(transactionList)
        transactionHolder.layoutManager = LinearLayoutManager(activity)
        val adapter = TransactionGroupRecyclerAdapter(groupedTransactions, (requireActivity() as AppCompatActivity))
        adapter.setOnItemClickListener { transaction -> requireActivity().startActivity(Intent(activity, TransactionPropertiesActivity::class.java).putExtra("transactionId", transaction.transactionId)) }
        transactionHolder.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        updateTransactionsList()
    }
}