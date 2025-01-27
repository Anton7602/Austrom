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
import com.colleagues.austrom.TransactionPropertiesActivityNew
import com.colleagues.austrom.adapters.TransactionGroupRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.TransactionTypeSelectionDialogFragment
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionFilter
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpTransactionHeader()
        createNewTransactionButton.setOnClickListener { launchNewTransactionCreationDialog() }
    }

    private fun launchNewTransactionCreationDialog() {
        val dialog = TransactionTypeSelectionDialogFragment()
        dialog.setOnDialogResultListener { transactionType -> startActivity(Intent(requireActivity(), TransactionCreationActivity::class.java).putExtra("TransactionType", transactionType.toString())) }
        dialog.show(requireActivity().supportFragmentManager, "AssetTypeSelectionDialog")
    }

    private fun setUpTransactionHeader() {
        transactionsHeader.setOnFilterChangedListener { transactionFilter ->applyTransactionFilter(transactionFilter) }
        transactionsHeader.setCurrencySymbol(AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode]!!.symbol)
        applyTransactionFilter(transactionsHeader.getTransactionFilter())
    }

    private fun applyTransactionFilter(transactionFilter: TransactionFilter) {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        localDBProvider.getTransactionsByTransactionFilterAsync(transactionFilter).observe(requireActivity()) {transactionList ->
            if (transactionList.isNotEmpty()) {
                setUpRecyclerView(transactionList.toMutableList())
                calculateTransactionsAmountSums(transactionList)
            }
        }
    }

    private fun calculateTransactionsAmountSums(transactionList: List<Transaction>) {
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

    private fun setUpRecyclerView(transactionList: MutableList<Transaction>) {
        val groupedTransactions = Transaction.groupTransactionsByDate(transactionList)
        transactionHolder.layoutManager = LinearLayoutManager(activity)
        val adapter = TransactionGroupRecyclerAdapter(groupedTransactions, (requireActivity() as AppCompatActivity))
        adapter.setOnItemClickListener { transaction -> requireActivity().startActivity(Intent(requireActivity(), TransactionPropertiesActivityNew::class.java).putExtra("transactionId", transaction.transactionId)) }
        transactionHolder.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        setUpTransactionHeader()
        applyTransactionFilter(transactionsHeader.getTransactionFilter())
    }
}