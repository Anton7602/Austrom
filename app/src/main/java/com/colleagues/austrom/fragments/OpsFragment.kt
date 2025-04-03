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
import com.colleagues.austrom.AustromApplication.Companion.activeCategories
import com.colleagues.austrom.R
import com.colleagues.austrom.TransactionCreationActivity
import com.colleagues.austrom.TransactionPropertiesActivityNew
import com.colleagues.austrom.adapters.TransactionDetailAsTransactionGroupRecyclerAdapter
import com.colleagues.austrom.adapters.TransactionGroupRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.bottomsheetdialogs.PeriodTypeSelectionDialogFragment
import com.colleagues.austrom.dialogs.bottomsheetdialogs.TransactionTypeSelectionDialogFragment
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.TransactionFilter
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.DateControllerView
import com.colleagues.austrom.views.TransactionHeaderView
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.absoluteValue

class OpsFragment : Fragment(R.layout.fragment_ops){
    fun setOnNavigationDrawerOpenCalled(l: ()->Unit) { requestNavigationDrawerOpen = l }
    private var requestNavigationDrawerOpen: ()->Unit = {}
    //region Binding
    private lateinit var transactionHolder: RecyclerView
    private lateinit var transactionsHeader: TransactionHeaderView
    private lateinit var createNewTransactionButton: ImageButton
    private lateinit var callNavDrawerButton: ImageButton
    private lateinit var switchListDisplayModeButton: ImageButton
    private lateinit var dateController: DateControllerView
    private fun bindViews(view: View) {
        transactionHolder = view.findViewById(R.id.ops_transactionHolder_rcv)
        transactionsHeader = view.findViewById(R.id.ops_transactionsHeader_trhed)
        createNewTransactionButton = view.findViewById(R.id.ops_createNewTransaction_btn)
        callNavDrawerButton = view.findViewById(R.id.ops_navDrawer_btn)
        switchListDisplayModeButton = view.findViewById(R.id.ops_switchListMode_btn)
        dateController = view.findViewById(R.id.ops_dateController_dctr)
    }
    //endregion
    private var lastSelectedIndex: Int = 0
    private var isListShowsTransactionDetails = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpDateController()
        setUpTransactionHeader()
        createNewTransactionButton.setOnClickListener { launchNewTransactionCreationDialog() }
        callNavDrawerButton.setOnClickListener { requestNavigationDrawerOpen() }
        switchListDisplayModeButton.setOnSafeClickListener { isListShowsTransactionDetails = !isListShowsTransactionDetails; applyTransactionFilter(transactionsHeader.getTransactionFilter()) }
    }

    private fun setUpDateController() {
        dateController.setDatesRangeChangedListener { dateRange ->
            val transactionFilter = transactionsHeader.getTransactionFilter()
            transactionFilter.dateFrom = dateRange.first
            transactionFilter.dateTo = dateRange.second
            applyTransactionFilter(transactionFilter)
        }
        dateController.setPeriodTypeChangeRequestedListener { launchPeriodTypeSelectionDialog() }
        dateController.setDate(LocalDate.now())
    }

    private fun launchPeriodTypeSelectionDialog() {
        val dialog = PeriodTypeSelectionDialogFragment()
        dialog.setOnDialogResultListener { periodType -> dateController.setPeriodType(periodType) }
        dialog.show(requireActivity().supportFragmentManager, "Date Period Type Selection")
    }

    private fun launchNewTransactionCreationDialog() {
        val dialog = TransactionTypeSelectionDialogFragment()
        dialog.setOnDialogResultListener { transactionType ->
            startActivity(Intent(requireActivity(), TransactionCreationActivity::class.java).putExtra("TransactionType", transactionType.toString()))
            dialog.dismiss()
        }
        dialog.show(requireActivity().supportFragmentManager, "AssetTypeSelectionDialog")
    }

    private fun setUpTransactionHeader() {
        transactionsHeader.setOnFilterChangedListener { transactionFilter ->applyTransactionFilter(transactionFilter) }
        transactionsHeader.setOnDatesRequestedListener { setUpDatePicker() }
        transactionsHeader.setCurrencySymbol(AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode]!!.symbol)
        transactionsHeader.setRequestDialogCall { dialog -> dialog.show(requireActivity().supportFragmentManager, "TransactionHeaderPickerDialog") }
        applyTransactionFilter(transactionsHeader.getTransactionFilter())
    }

    private fun setUpDatePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .build()
        dateRangePicker.show(requireActivity().supportFragmentManager, "DATE_PICKER")
        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = Instant.ofEpochMilli(selection.first)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val endDate = Instant.ofEpochMilli(selection.second)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            transactionsHeader.setFilterDates(startDate, endDate)
        }
    }

    private fun applyTransactionFilter(transactionFilter: TransactionFilter) {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        localDBProvider.getTransactionsByTransactionFilterAsync(transactionFilter).observe(viewLifecycleOwner) {transactionList ->
            if (!isListShowsTransactionDetails) {
                val filteredTransaction = filterTransactionByNames(transactionFilter, transactionList)
                setUpTransactionRecyclerView(filteredTransaction.toMutableList())
                calculateTransactionsAmountSums(filteredTransaction)
                if (lastSelectedIndex!=0 && (transactionHolder.adapter as TransactionGroupRecyclerAdapter).itemCount>lastSelectedIndex) {
                    transactionHolder.scrollToPosition(lastSelectedIndex)
                }
            }
        }

        localDBProvider.getTransactionWithTransactionDetailsByTransactionFilter(transactionFilter).observe(viewLifecycleOwner) { transactionDetailsMap ->
            if (isListShowsTransactionDetails) {
                val filteredTransactionDetailsMap = filterTransactionDetailsByName(transactionFilter, transactionDetailsMap)
                setUpTransactionDetailsRecyclerView(filteredTransactionDetailsMap)
                calculateTransactionsAmountSums(filteredTransactionDetailsMap)
                if (lastSelectedIndex!=0 && (transactionHolder.adapter as TransactionDetailAsTransactionGroupRecyclerAdapter).itemCount>lastSelectedIndex) {
                    transactionHolder.scrollToPosition(lastSelectedIndex)
                }
            }
        }
    }

    private fun filterTransactionByNames(transactionFilter: TransactionFilter, transactionList: List<Transaction>): List<Transaction> {
        if (transactionFilter.names.isEmpty()) return transactionList
        val filteredTransactions = mutableListOf<Transaction>()
        transactionList.forEach { transaction -> for (name in transactionFilter.names) { if (transaction.transactionName.lowercase().contains(name.lowercase())) {filteredTransactions.add(transaction); break; }}}
        return filteredTransactions
    }

    private fun filterTransactionDetailsByName(transactionFilter: TransactionFilter, transactionDetailsMap: Map<Transaction, List<TransactionDetail>>) : Map<Transaction, List<TransactionDetail>> {
        if (transactionFilter.names.isEmpty()) return transactionDetailsMap
        val filteredTransactionDetails = mutableMapOf<Transaction, MutableList<TransactionDetail>>()
        transactionDetailsMap.forEach { entry ->
            entry.value.forEach { transactionDetail ->
                for (name in transactionFilter.names) {
                    if (entry.key.transactionName.lowercase().contains(name.lowercase()) || transactionDetail.name.lowercase().contains(name.lowercase())) {
                        if (filteredTransactionDetails.containsKey(entry.key)) {
                            filteredTransactionDetails[entry.key]!!.add(transactionDetail)
                        } else {
                            filteredTransactionDetails[entry.key] = mutableListOf(transactionDetail)
                        }
                        break
                    }
                }
            }
        }
        return filteredTransactionDetails
    }

    private fun calculateTransactionsAmountSums(transactionList: List<Transaction>) {
        var incomeSum = 0.0
        var expenseSum = 0.0
        transactionList.forEach{ transaction ->
            val transactionsAsset = AustromApplication.activeAssets[transaction.assetId]
            if (transaction.transactionType() == TransactionType.EXPENSE) {
                if (transactionsAsset!=null) {
                    expenseSum+= transaction.amountInBaseCurrency()
                }
            }
            if (transaction.transactionType() == TransactionType.INCOME) {
                if (transactionsAsset!=null) {
                    incomeSum+= transaction.amountInBaseCurrency()
                }
            }
        }
        transactionsHeader.setIncome(incomeSum)
        transactionsHeader.setExpense(expenseSum)
    }

    private fun calculateTransactionsAmountSums(transactionDetailsMap: Map<Transaction, List<TransactionDetail>>) {
        var incomeSum = 0.0
        var expenseSum = 0.0
        transactionDetailsMap.forEach{ transaction ->
            transaction.value.forEach { transactionDetail ->
                val transactionsAsset = AustromApplication.activeAssets[transaction.key.assetId]
                if (transaction.key.transactionType() == TransactionType.EXPENSE) {
                    if (transactionsAsset!=null) {
                        expenseSum-= transactionDetail.costInBaseCurrency(transaction.key).absoluteValue
                    }
                }
                if (transaction.key.transactionType() == TransactionType.INCOME) {
                    if (transactionsAsset!=null) {
                        incomeSum+= transactionDetail.costInBaseCurrency(transaction.key).absoluteValue
                    }
                }
            }
        }
        transactionsHeader.setIncome(incomeSum)
        transactionsHeader.setExpense(expenseSum)
    }

    private fun setUpTransactionRecyclerView(transactionList: MutableList<Transaction>) {
        val groupedTransactions = Transaction.groupTransactionsByDate(transactionList)
        transactionHolder.layoutManager = LinearLayoutManager(activity)
        val adapter = TransactionGroupRecyclerAdapter(groupedTransactions, (requireActivity() as AppCompatActivity))
        adapter.setOnItemClickListener { transaction, index ->
            lastSelectedIndex = index
            requireActivity().startActivity(Intent(requireActivity(), TransactionPropertiesActivityNew::class.java).putExtra("transactionId", transaction.transactionId))
        }
        transactionHolder.adapter = adapter
    }

    private fun setUpTransactionDetailsRecyclerView(transactionDetailsMap: Map<Transaction, List<TransactionDetail>>) {
        transactionHolder.layoutManager = LinearLayoutManager(activity)
        val adapter = TransactionDetailAsTransactionGroupRecyclerAdapter(groupUpTransactionDetails(transactionDetailsMap), (requireActivity() as AppCompatActivity))
        adapter.setOnItemClickListener { transactionDetail, index ->
            lastSelectedIndex = index
            requireActivity().startActivity(Intent(requireActivity(), TransactionPropertiesActivityNew::class.java).putExtra("transactionId", transactionDetail.transactionId))
        }
        transactionHolder.adapter = adapter
    }

    private fun groupUpTransactionDetails(transactionDetailsMap: Map<Transaction, List<TransactionDetail>>): Map<LocalDate, Map<Transaction, List<TransactionDetail>>> {
        val result = mutableMapOf<LocalDate, MutableMap<Transaction, List<TransactionDetail>>>()
        transactionDetailsMap.forEach { transactionMap ->
            if (!result.containsKey(transactionMap.key.transactionDate)) {
                result[transactionMap.key.transactionDate] = mutableMapOf(Pair(transactionMap.key, transactionMap.value))
            } else {
                result[transactionMap.key.transactionDate]!![transactionMap.key] = transactionMap.value
            }
        }
        return result
    }


}